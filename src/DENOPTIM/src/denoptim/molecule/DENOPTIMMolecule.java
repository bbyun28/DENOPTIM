/*
 *   DENOPTIM
 *   Copyright (C) 2019 Vishwesh Venkatraman <vishwesh.venkatraman@ntnu.no>
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

package denoptim.molecule;

import java.io.Serializable;
import java.io.File;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtomContainer;

import denoptim.constants.DENOPTIMConstants;
import denoptim.exception.DENOPTIMException;
import denoptim.utils.GraphConversionTool;


/**
 * A molecular object with additional data and tags. Additional data includes
 * the DENOPTIM graph representation, fitness/error, and possibly other stuff.
 */
public class DENOPTIMMolecule implements Comparable<DENOPTIMMolecule>, Serializable
{
    /**
	 * Version UID
	 */
	private static final long serialVersionUID = -3132192038061270220L;

	/**
     * molecule representation as a graph of vertices and edges
     */
    private DENOPTIMGraph molGraph;
    
    /**
     * INCHI representation of the generated molecule, we will stick to just the key
     */
    private String molUID;
	
    /**
     * smiles representation
     */
    private String molSmiles;
    
    /**
     * fitness of the molecule
     */
    private double molFitness;
    
    /**
     * name of the optimized molecule filename
     */
    private String molFile;

    /**
     * File containing the picture of the molecule
     */
    private String imgFile;
    
    /**
     * Any comments on the molecule
     */
    private String commments;
    
    /**
     * Error that prevented calculation of the fitness
     */
    private String molError;
    
    /**
     * Flag signaling the presence of a fitness value associated
     */
    private boolean hasFitness;
    
    /**
     * ID of the generation this molecule belong to (or -1)
     */
    private int generationId = -1;
    
    /**
     * Name (not guaranteed to be unique)
     */
    private String molName;
    
    /**
     * Level that generated this graph in fragment space exploration
     */
    private int level;

    
//------------------------------------------------------------------------------

    public DENOPTIMMolecule()
    {
        molUID = "UNDEFINED";
        molSmiles = "UNDEFINED";
        molFitness = 0; //This is stupid... only needed by compareTo. TODO: change!
        hasFitness = false;
    }
    
//------------------------------------------------------------------------------
    
    public DENOPTIMMolecule(DENOPTIMGraph m_molGraph, String m_molUID, 
                            String m_molSmiles, double m_molFitness)
    {
        molGraph = m_molGraph;
        molUID = m_molUID;
        molSmiles = m_molSmiles;
        molFitness = m_molFitness;
        hasFitness = true;
   }

//------------------------------------------------------------------------------
	
    public DENOPTIMMolecule(String m_molFile, DENOPTIMGraph m_molGraph, 
                            String m_molUID, String m_molSmiles, 
                            double m_molFitness)
    {
        molGraph = m_molGraph;
        molUID = m_molUID;
        molSmiles = m_molSmiles;
        molFitness = m_molFitness;
        molFile = m_molFile;
        hasFitness = true;
    }
//------------------------------------------------------------------------------
    
    public DENOPTIMMolecule(IAtomContainer iac, boolean useFragSpace) 
    		throws DENOPTIMException
    {
    	this(iac, useFragSpace, false);
    }
    
//------------------------------------------------------------------------------
    
    public DENOPTIMMolecule(IAtomContainer iac, boolean useFragSpace, 
    		boolean allowNoUID) throws DENOPTIMException
    {
    	// Initialize, then we try to take info from IAtomContainer
        this.molUID = "UNDEFINED";
        this.molSmiles = "UNDEFINED";
        this.molFitness = 0; //This is stupid... only needed by compareTo. TODO: change!
        this.hasFitness = false;
		
		this.molName = "noname";
		if (iac.getProperty(CDKConstants.TITLE) != null)
		{
			this.molName = iac.getProperty(CDKConstants.TITLE).toString();
		}
		
        if (iac.getProperty(DENOPTIMConstants.MOLERRORTAG) != null)
        {
        	this.molError = iac.getProperty(
        			DENOPTIMConstants.MOLERRORTAG).toString();
        }

        if (iac.getProperty(DENOPTIMConstants.FITNESSTAG) != null)
        {
            String fitprp = iac.getProperty(
            		DENOPTIMConstants.FITNESSTAG).toString();
            double fitVal = Double.parseDouble(fitprp);
            if (Double.isNaN(fitVal))
            {
                String msg = "Cannot build DENOPTIMMolecule from "
                		+ "IAtomContainer: Fitness value is NaN!";
                throw new DENOPTIMException(msg);
            }
            this.molFitness = fitVal;
            this.hasFitness = true;
        }
        
        if (iac.getProperty(DENOPTIMConstants.GRAPHLEVELTAG) != null)
        {
        	this.level = Integer.parseInt(iac.getProperty(
        			DENOPTIMConstants.GRAPHLEVELTAG).toString());
        }
        
        if (iac.getProperty(DENOPTIMConstants.SMILESTAG) != null)
        {
        	this.molSmiles = iac.getProperty(
        			DENOPTIMConstants.SMILESTAG).toString();
        }

        try
        {
            this.molUID = iac.getProperty(
            		DENOPTIMConstants.UNIQUEIDTAG).toString();
        } catch (Exception e) {
        	if (allowNoUID)
        	{
        		this.molUID = "noUID";
        	} else {
        		throw new DENOPTIMException("Could not read UID to make "
        				+ "DENOPTIMMolecule.", e);
        	}
        }
        try
        {
            this.molGraph = GraphConversionTool.getGraphFromString(
            		iac.getProperty(DENOPTIMConstants.GRAPHTAG).toString(),
            		useFragSpace);
        } catch (Exception e) {
        	throw new DENOPTIMException("Could not read Graph to make "
        			+ "DENOPTIMMolecule.", e);
        }
        if (iac.getProperty(DENOPTIMConstants.GMSGTAG) != null)
        {
            this.commments = iac.getProperty(
            		DENOPTIMConstants.GMSGTAG).toString();
        }
    }

//------------------------------------------------------------------------------
    
    public void setComments(String m_str)
    {
        commments = m_str;
    }
    
//------------------------------------------------------------------------------
    
    public String getComments()
    {
        return commments;
    }    
	
//------------------------------------------------------------------------------

    public void setMoleculeFile(String m_molFile)
    {
        molFile = m_molFile;
    }	
    
//------------------------------------------------------------------------------

    public void setImageFile(String m_imgFile)
    {
        imgFile = m_imgFile;
    }

//------------------------------------------------------------------------------

    public void setMoleculeGraph(DENOPTIMGraph m_molGraph)
    {
        molGraph = m_molGraph;
    }

//------------------------------------------------------------------------------

    public void setMoleculeUID(String m_UID)
    {
        molUID = m_UID;
    }

//------------------------------------------------------------------------------

    public void setMoleculeSmiles(String m_molSmiles)
    {
        molSmiles = m_molSmiles;
    }
   
//------------------------------------------------------------------------------

    public void setMoleculeFitness(double m_fitness)
    {
        molFitness = m_fitness;
        hasFitness = true;
    }
    
//------------------------------------------------------------------------------

    public void setError(String error)
    {
        molError = error;
    }
    
//------------------------------------------------------------------------------

    public void setName(String name)
    {
        molName = name;
    }
    
//------------------------------------------------------------------------------

    public String getName()
    {
        return molName;
    }
    
//------------------------------------------------------------------------------

    public String getError()
    {
        return molError;
    }
    
//------------------------------------------------------------------------------

    public String getMoleculeUID()
    {
        return molUID;
    }

//------------------------------------------------------------------------------

    public String getMoleculeSmiles()
    {
        return molSmiles;
    }
    
//------------------------------------------------------------------------------

    public String getMoleculeFile()
    {
        return molFile;
    }
    
//------------------------------------------------------------------------------

    public String getImageFile()
    {
        return imgFile;
    }

//------------------------------------------------------------------------------

    public DENOPTIMGraph getMoleculeGraph()
    {
        return molGraph;
    }

//------------------------------------------------------------------------------

    public double getMoleculeFitness()
    {
        return molFitness;
    }
    
//------------------------------------------------------------------------------
    
    public boolean hasFitness()
    {
    	return hasFitness;
    }

//------------------------------------------------------------------------------
    
    public void setGeneration(int genId)
    {
	    generationId = genId;
	}
    
//------------------------------------------------------------------------------
    
    public int getGeneration()
    {
	    return generationId;
	}
    
//------------------------------------------------------------------------------
    
    /**
     * Sets level that generated this graph in a fragment space 
     * exploration experiment.
     * @param lev the level index
     */
    public void setLevel(int lev)
    {
    	level = lev;
    }
    
//------------------------------------------------------------------------------
    
    /**
     * Returns the level that generated this graph in a fragment space 
     * exploration experiment.
     * @return the level that generated this graph in a fragment space 
     * exploration experiment.
     */
	public int getLevel() 
	{
		return level;
	}

//------------------------------------------------------------------------------

    // The compareTo method compares the receiving object with the specified 
    // object and returns a negative integer, 0, or a positive integer depending 
    // on whether the receiving object is less than, equal to, or greater than 
    // the specified object.

    @Override
    public int compareTo(DENOPTIMMolecule other)
    {
        if (this.molFitness > other.molFitness)
            return 1;
        else if (this.molFitness < other.molFitness)
            return -1;
        return 0;
    }

//------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(16);
        String mname = new File(molFile).getName();
        if (mname != null)
            sb.append(String.format("%-20s", mname));
        
        sb.append(String.format("%-20s", this.molGraph.getGraphId()));
        sb.append(String.format("%-30s", molUID));
        sb.append(String.format("%12.3f", molFitness));

        return sb.toString();
    }

//------------------------------------------------------------------------------    
    
    public void cleanup()
    {
        if (molGraph != null)
            molGraph.cleanup();
    }
    
//------------------------------------------------------------------------------        
    
}
