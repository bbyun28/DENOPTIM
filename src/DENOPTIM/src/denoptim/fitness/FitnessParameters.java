/*
 *   DENOPTIM
 *   Copyright (C) 2019 Marco Foscato <marco.foscato@uib.no>
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

package denoptim.fitness;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.VariableResolver;

import org.apache.commons.el.ExpressionEvaluatorImpl;
import org.openscience.cdk.qsar.DescriptorEngine;
import org.openscience.cdk.qsar.DescriptorSpecification;
import org.openscience.cdk.qsar.IDescriptor;

import denoptim.exception.DENOPTIMException;
import denoptim.io.DenoptimIO;
import denoptim.logging.DENOPTIMLogger;


/**
 * Parameters defining the fitness providers.
 * 
 * @author Marco Foscato
 */

public class FitnessParameters
{
    /**
     * Flag indicating that at least one FS-parameter has been defined
     */
    private static boolean fitParamsInUse = false;
    
    /**
     * Flag indication we want to use external fitness provider
     */
    private static boolean useExternalFitness = true;

    /**
     * Pathname of an external fitness provider executable
     */
    private static String externalExe = "";

    /**
     * Interpreter for the external fitness provider
     */
    private static String interpreterExternalExe = "BASH";

    /**
     * Formulation of the internally provided fitness
     */
    private static String fitnessExpression = "";
    
    /**
     * The list of descriptors needed to calculate the fitness with internal 
     * fitness provider.
     */
    private static List<DescriptorForFitness> descriptors;
    
    /**
     * List of descriptor's short names
     */
    private static List<String> variablesInEquation;
    
    /**
     * Flag controlling production of png graphics for each candidate
     */
    private static boolean makePictures = false;


//------------------------------------------------------------------------------

    public static boolean fitParamsInUse()
    {
        return fitParamsInUse;
    }

//------------------------------------------------------------------------------

    /**
     * @return <code>true</code> if we are asked to execute an external fitness 
     * provider.
     */
    public static boolean useExternalFitness()
    {
        return useExternalFitness;
    }
    
//------------------------------------------------------------------------------

    /**
     * @return <code>true</code> if generation of the candidate's picture is 
     * required.
     */
    public static boolean makePictures()
    {
        return makePictures;
    }

//------------------------------------------------------------------------------

    /**
     * Gets the pathname of the external executable file.
     * @return the pathname to the external fitness provider.
     */
    public static String getExternalFitnessProvider()
    {
        return externalExe;
    }
    
//------------------------------------------------------------------------------

    /**
     * Gets the interpreter used to run the external fitness provider.
     * @return the interpreter name.
     */
    public static String getExternalFitnessProviderInterpreter()
    {
        return interpreterExternalExe;
    }
    
//------------------------------------------------------------------------------
    
    /**
     * @return the expression used to calculate the fitness with the internal
     * fitness provider
     */
    public static String getFitnessExpression()
    {
    	return fitnessExpression;
    }
    
//------------------------------------------------------------------------------
    
    /**
     * @return list of descriptors needed to calculate the fitness
     */
    public static List<DescriptorForFitness> getDescriptors()
    {
    	return descriptors;
    }

//------------------------------------------------------------------------------
    
    public static void interpretKeyword(String line) throws DENOPTIMException
    {
        String key = line.trim();
        String value = "";
        if (line.contains("="))
        {
            key = line.substring(0,line.indexOf("=") + 1).trim();
            value = line.substring(line.indexOf("=") + 1).trim();
        }
        try
        {
            interpretKeyword(key,value);
        }
        catch (DENOPTIMException e)
        {
            throw new DENOPTIMException(e.getMessage()+" Check line "+line);
        }
    }

//------------------------------------------------------------------------------

    public static void interpretKeyword(String key, String value)
                                                      throws DENOPTIMException
    {
        String msg = "";
        switch (key.toUpperCase())
        {
        case "FP-SOURCE=":
        	externalExe = value;
        	fitParamsInUse = true;
            break;
            
        case "FP-INTERPRETER=":
        	interpreterExternalExe = value;
        	fitParamsInUse = true;
            break;
            
        case "FP-EQUATION=":
        	fitnessExpression = value;
        	fitParamsInUse = true;
        	useExternalFitness = false;
            break;
            
        case "FP-MAKEPICTURES":
        	fitParamsInUse = true;
	        makePictures = true;
	        break;

        default:
             msg = "Keyword " + key + " is not a known fitness-related "
             		+ "keyword. Check input files.";
            throw new DENOPTIMException(msg);
        }
    }
    
//------------------------------------------------------------------------------

	private static void parseFitnessExpressionToDefineDescriptors(String value) 
			throws DENOPTIMException 
	{
		// Parse expression to get the names of all variables
		ExpressionEvaluatorImpl extractor = new ExpressionEvaluatorImpl();
		variablesInEquation = new ArrayList<String>();
        VariableResolver collector = new VariableResolver() {
			
			@Override
			public Double resolveVariable(String varName) throws ELException {
				if (!variablesInEquation.contains(varName))
				{
					variablesInEquation.add(varName);
				}
				return 1.0;
			}
		};
		try {
			extractor.evaluate(fitnessExpression, Double.class, collector, null);
		} catch (ELException e) {
			throw new DENOPTIMException("ERROR: unable to parse fitness "
					+ "expression.",e);
		}

		// Collect the descriptors needed to calculate the fitness
		descriptors = DescriptorUtils.findAllDescriptorImplementations(
				variablesInEquation);
	}

//------------------------------------------------------------------------------

    public static void checkParameters() throws DENOPTIMException
    {
        String msg = "";
        if (!fitParamsInUse)
        {
            return;
        }

        if ((externalExe.length() != 0) 
        	&& (!DenoptimIO.checkExists(externalExe)))
        {
            msg = "Cannot find the fitness provider: " + externalExe;
            throw new DENOPTIMException(msg);
        }

        if (interpreterExternalExe.length() != 0)
        {
        	switch (interpreterExternalExe.toUpperCase())
        	{
	        	case "BASH":
	        		break;
/*
//TODO: add these
	        	case "PYTHON":
	        		break;
	        	case "JAVA":
	        		break;
*/
	        	default:
	        		msg = "Interpreter '" + interpreterExternalExe 
	        										 + "' not available.";
	                throw new DENOPTIMException(msg);
        	}
        }
    }

//------------------------------------------------------------------------------

    public static void processParameters() throws DENOPTIMException
    {
    	if (!fitnessExpression.equals(""))
    	{
        	parseFitnessExpressionToDefineDescriptors(fitnessExpression);
    	}
    }

//------------------------------------------------------------------------------

    public static void printParameters()
    {
		if (!fitParamsInUse)
		{
		    return;
		}
        String eol = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder(1024);
        sb.append(" FitnessParameters ").append(eol);
        for (Field f : FitnessParameters.class.getDeclaredFields()) 
        {
        	try
            {
                sb.append(f.getName()).append(" = ").append(
                            f.get(FitnessParameters.class)).append(eol);
            }
            catch (Throwable t)
            {
            	t.printStackTrace();
                sb.append("ERROR! Unable to print FitnessParameters.");
                break;
            }
        }
        DENOPTIMLogger.appLogger.info(sb.toString());
        sb.setLength(0);
    }

//------------------------------------------------------------------------------

}
