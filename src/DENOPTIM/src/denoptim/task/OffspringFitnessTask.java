/*
 *   DENOPTIM
 *   Copyright (C) 2019 Vishwesh Venkatraman <vishwesh.venkatraman@ntnu.no> and
 *   Marco Foscato <marco.foscato@uib.no>
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package denoptim.task;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtomContainer;

import denoptim.exception.DENOPTIMException;
import denoptim.fitness.FitnessParameters;
import denoptim.io.DenoptimIO;
import denoptim.logging.DENOPTIMLogger;
import denoptim.molecule.DENOPTIMGraph;
import denoptim.molecule.DENOPTIMMolecule;
import denoptim.utils.DENOPTIMMoleculeUtils;
import denoptim.utils.TaskUtils;

/**
 * Task that calls the fitness provider for a candidate population member.
 */

public class OffspringFitnessTask extends FitnessTask
{
    private final String molName;
    private final String molsmiles;
    private final IAtomContainer molInit;
    private String molinchi;
    private volatile ArrayList<DENOPTIMMolecule> curPopln;
    private volatile Integer numtry;
    private String errMsg = "";
    private final String fileUID;

//------------------------------------------------------------------------------
    
    /**
     * 
     * @param m_molName
     * @param m_molGraph
     * @param m_inchi
     * @param m_smiles
     * @param m_iac
     * @param m_dir
     * @param m_Id
     * @param m_popln reference to the current population. Can be null, in which
     * case this task will not add its entity to the population
     * @param m_try
     * @param m_fileUID
     */
    public OffspringFitnessTask(String m_molName, DENOPTIMGraph m_molGraph, String m_inchi,
            String m_smiles, IAtomContainer m_iac, String m_dir,
            ArrayList<DENOPTIMMolecule> m_popln, Integer m_try, String m_fileUID)
    {
    	super(m_molGraph);
        molName = m_molName;
        workDir = m_dir;
        molinchi = m_inchi;
        molInit = m_iac;
        molsmiles = m_smiles;
        molinchi = m_inchi;
        curPopln = m_popln;
        fileUID = m_fileUID;
        numtry = m_try;
        
        molInit.setProperty(CDKConstants.TITLE, molName);
        molInit.setProperty("GCODE", molGraph.getGraphId());
        molInit.setProperty("InChi", molinchi);
        molInit.setProperty("SMILES", molsmiles);
        molInit.setProperty("GraphENC", molGraph.toString());
        if (molGraph.getMsg() != null)
        {
            molInit.setProperty("GraphMsg", molGraph.getMsg());
        }
    }

//------------------------------------------------------------------------------
    
    @Override
    public Object call() throws DENOPTIMException, Exception
    {
        DENOPTIMMolecule result = new DENOPTIMMolecule();
        result.setName(molName);
        result.setMoleculeGraph(molGraph);
        result.setMoleculeUID(molinchi);
        result.setMoleculeSmiles(molsmiles);
        
        String finalFitFile = workDir + SEP + molName + "_FIT.sdf";
        String initialFile = workDir + SEP + molName + "_I.sdf";
        String pictureFile = workDir + SEP + molName + ".png";
        
        result.setMoleculeFile(finalFitFile);

        //TODO change to allow other kinds of external tools (probably merge FitnessTask and FTask and put it under denoptim.fitness package
        // write the input for the fitness provider
        DenoptimIO.writeMolecule(initialFile, molInit, false);

        try
        {
            // Here we run the subprocess that executed the fitness provider
            executeFitnessProvider(initialFile,finalFitFile,fileUID);
        }
        catch (Throwable ex)
        {
            hasException = true;
            throw new DENOPTIMException(ex);
        }

        // read the molecular model returned by the fitness provider
        IAtomContainer processedMol = new AtomContainer();
        boolean unreadable = false;
        try
        {
            processedMol = DenoptimIO.readSingleSDFFile(finalFitFile);
            if (processedMol.isEmpty())
            {
                unreadable=true;
            }
        }
        catch (Throwable t)
        {
            unreadable=true;
        }

        String msg;
        if (unreadable)
        {
            msg = "Unreadable FIT file for " + molName;
            DENOPTIMLogger.appLogger.log(Level.WARNING, msg);
            
            // make a copy of the unreadable FIT file that will be replaced
            String fitFileCp = workDir + SEP + molName 
                                                      + "_UnreadbleFIT.sdf";
            FileUtils.copyFile(new File(finalFitFile), 
                               new File(fitFileCp));
            FileUtils.deleteQuietly(new File(finalFitFile));
            
            String err = "#FTask: Unable to retrive data. See " + fitFileCp;

            // make a readable FIT file with minimal data
            processedMol = new AtomContainer();
            processedMol.addAtom(new Atom("H"));
            processedMol.setProperty(CDKConstants.TITLE, molName);
            processedMol.setProperty("MOL_ERROR", err);
            processedMol.setProperty("GCODE", molGraph.getGraphId());
            processedMol.setProperty("GraphENC", molGraph.toString());
            DenoptimIO.writeMolecule(finalFitFile, processedMol, false);
            
            result.setError(err);
            completed = true;
            return result;
        }
        
        if (processedMol.getProperty("UID") != null)
        {
            result.setMoleculeUID(
            		processedMol.getProperty("UID").toString());
        } else
        {
            result.setMoleculeUID(molinchi);
        }

        if (processedMol.getProperty("MOL_ERROR") != null)
        {
        	String err = processedMol.getProperty("MOL_ERROR").toString();
            msg = "Structure " + molName + " has an error ("+err+")";
            DENOPTIMLogger.appLogger.info(msg);
            completed = true;

            synchronized (numtry)
            {
                numtry++;
            }
            result.setError(err);
            completed = true;
            return result;
        }

        if (processedMol.getProperty("FITNESS") != null)
        {
            String fitprp = processedMol.getProperty("FITNESS").toString();
            double fitVal = 0.0;
            try
            {
                fitVal = Double.parseDouble(fitprp);
            }
            catch (Throwable t)
            {
                hasException = true;
                msg = "Fitness value '" + fitprp + "' of " + molName 
                      + " could not be converted to double.";
                errMsg = msg;
                DENOPTIMLogger.appLogger.severe(msg);
                molGraph.cleanup();
                throw new DENOPTIMException(msg);
            }

            if (Double.isNaN(fitVal))
            {
                hasException = true;
                msg = "Fitness value is NaN for " + molName;
                errMsg = msg;
                DENOPTIMLogger.appLogger.severe(msg);
                molGraph.cleanup();
                throw new DENOPTIMException(msg);
            }

            processedMol.setProperty("GCODE", molGraph.getGraphId());                
            processedMol.setProperty("SMILES", molsmiles);
            processedMol.setProperty("GraphENC", molGraph.toString());
            if (molGraph.getMsg() != null)
            {
                processedMol.setProperty("GraphMsg", molGraph.getMsg());
            }
            DenoptimIO.writeMolecule(finalFitFile, processedMol, false);

            result.setMoleculeFitness(fitVal);

            // add the newmol to the population list
            if (curPopln != null)
            {
                synchronized (curPopln)
                {
                	DENOPTIMLogger.appLogger.log(Level.INFO, 
                			"Adding {0} to population", molName);
                    curPopln.add(result);
                }
                synchronized (numtry)
                {
                    numtry--;
                }
            }
            
            // image creation
            if (FitnessParameters.makePictures())
            {
                try
                {
                    DENOPTIMMoleculeUtils.moleculeToPNG(processedMol, pictureFile);
                    result.setImageFile(pictureFile);
                }
                catch (Exception ex)
                {
                    result.setImageFile(null);
                    DENOPTIMLogger.appLogger.log(Level.WARNING, 
                        "Unable to create image.{0}", ex.getMessage());
                }
            }
        }
        else
        {
            hasException = true;
            msg = "Could not find \"FITNESS\" tag in file: " + finalFitFile;
            errMsg = msg;
            DENOPTIMLogger.appLogger.severe(msg);
            throw new DENOPTIMException(msg);
        }

        completed = true;
        return result;
    }

//------------------------------------------------------------------------------

}
