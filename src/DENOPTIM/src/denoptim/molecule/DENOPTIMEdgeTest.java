package denoptim.molecule;

/*
 *   DENOPTIM
 *   Copyright (C) 2019 Vishwesh Venkatraman <vishwesh.venkatraman@ntnu.no>
 *   and Marco Foscato <marco.foscato@uib.no>
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import denoptim.molecule.DENOPTIMEdge.BondType;

/**
 * Unit test for DENOPTIMEdge
 * 
 * @author Marco Foscato
 */

public class DENOPTIMEdgeTest
{
	private StringBuilder reason = new StringBuilder();
	
//------------------------------------------------------------------------------
	
    @Test
    public void testSameAs_Equal() throws Exception
    {
    	DENOPTIMEdge eA = new DENOPTIMEdge(0, 1, 0, 0, BondType.UNDEFINED);
    	DENOPTIMEdge eB = new DENOPTIMEdge(0, 1, 0, 0, BondType.UNDEFINED);

    	assertTrue(eA.sameAs(eB, reason));	
    }
    
//------------------------------------------------------------------------------
	
    @Test
    public void testSameAs_DiffAtm() throws Exception
    {
    	DENOPTIMEdge eA = new DENOPTIMEdge(0, 1, 0, 0, BondType.UNDEFINED);
    	DENOPTIMEdge eB = new DENOPTIMEdge(0, 1, 1, 0, BondType.UNDEFINED);
    	DENOPTIMEdge eC = new DENOPTIMEdge(0, 1, 0, 1, BondType.UNDEFINED);

    	assertFalse(eA.sameAs(eB, reason));	
    	assertFalse(eA.sameAs(eC, reason));	
    }
    
//------------------------------------------------------------------------------
	
    @Test
    public void testSameAs_DiffBndTyp() throws Exception
    {
    	DENOPTIMEdge eA = new DENOPTIMEdge(0, 1, 0, 0, BondType.SINGLE);
    	DENOPTIMEdge eB = new DENOPTIMEdge(0, 1, 0, 0, BondType.DOUBLE);
    	
    	assertFalse(eA.sameAs(eB, reason));	
    }
    
//------------------------------------------------------------------------------
	
    @Test
    public void testSameAs_SameAPClass() throws Exception
    {
    	DENOPTIMEdge eA = new DENOPTIMEdge(0, 1, 0, 0, BondType.UNDEFINED);
    	eA.setSourceReaction("classA");
    	DENOPTIMEdge eB = new DENOPTIMEdge(0, 1, 0, 0, BondType.UNDEFINED);
    	eB.setSourceReaction("classA");
    	
    	assertTrue(eA.sameAs(eB, reason));	
    }
    
//------------------------------------------------------------------------------
	
    @Test
    public void testSameAs_DiffAPClass() throws Exception
    {
    	DENOPTIMEdge eA = new DENOPTIMEdge(0, 1, 0, 0, BondType.UNDEFINED);
    	eA.setSourceReaction("classA");
    	DENOPTIMEdge eB = new DENOPTIMEdge(0, 1, 0, 0, BondType.UNDEFINED);
    	eB.setSourceReaction("classB");
    	
    	assertFalse(eA.sameAs(eB, reason));	
    }
    
//------------------------------------------------------------------------------
}
