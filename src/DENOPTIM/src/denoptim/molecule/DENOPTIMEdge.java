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

import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IBond.Order;

import denoptim.exception.DENOPTIMException;

/**
 * This class represents the edge between two vertices.
 * @author Vishwesh Venkatraman
 * @author Marco Foscato
 */
public class DENOPTIMEdge implements Serializable
{
    /**
     * Attachment point at source end
     */
    private DENOPTIMAttachmentPoint srcAP;

    /**
     * Attachment point at target end
     */
    private DENOPTIMAttachmentPoint trgAP;

    /**
     * The bond type associated with the connection between the fragments
     */
    private BondType bondType = BondType.UNDEFINED;


//------------------------------------------------------------------------------
    
      /**
       * Constructor for an edge that connects two APs. The number of 
       * connections available in the APs is reduced upon creation of the edge 
       * and according the the bond type.
       * @param srcAP attachment point at source end
       * @param trgAP attachment point at target end
       * @param bondType defines what kind of bond type this edge should be 
       * converted to when converting a graph into a chemical representation.
       */
    
      public DENOPTIMEdge(DENOPTIMAttachmentPoint srcAP,
                          DENOPTIMAttachmentPoint trgAP, BondType bondType) {
          this.srcAP = srcAP;
          this.trgAP = trgAP;
          this.bondType = bondType;
          this.srcAP.updateFreeConnections(-bondType.getValence());
          this.trgAP.updateFreeConnections(-bondType.getValence());
      }
      
//------------------------------------------------------------------------------
      
      /**
       * Constructor for an edge that connects two APs. We assume a single bond.
       * The number of 
       * connections available in the APs is reduced upon creation of the edge 
       * and according the the bond type.
       * @param srcAP attachment point at source end
       * @param trgAP attachment point at target end
       */
    
      public DENOPTIMEdge(DENOPTIMAttachmentPoint srcAP,
                          DENOPTIMAttachmentPoint trgAP) {
          this(srcAP, trgAP, BondType.SINGLE);
      }
    
//------------------------------------------------------------------------------

    /**
     * Constructor for an edge
     * @param srcAP attachment point at source end
     * @param trgAP attachment point at target end
     */
     //TODO-M7 del
     @Deprecated
    public DENOPTIMEdge(DENOPTIMAttachmentPoint srcAP,
                        DENOPTIMAttachmentPoint trgAP, int srcVertex,
                        int trgVertex, int srcAPID, int trgAPID) {
        this(srcAP, trgAP, BondType.SINGLE);
    }

//------------------------------------------------------------------------------
    //TODO-V3 constructors for edge will change one ap-ownership is sorted out
    //TODO-V3 remove
   //TODO-M7 del
    @Deprecated
    public DENOPTIMEdge(DENOPTIMAttachmentPoint srcAP,
                        DENOPTIMAttachmentPoint trgAP, int srcVertex,
                        int trgVertex, int srcAPID, int trgAPID,
                        BondType bondType)
    {
        this(srcAP, trgAP, bondType);
    }

//------------------------------------------------------------------------------
   
    /**
     * Constructor for an edge from all parameters
     * @param srcVertex vertex ID of the source vertex
     * @param trgVertex vertex ID of the target vertex
     * @param srcAPID index of the AP on the source vertex
     * @param trgAPID index of the AP on the target vertex
     * @param bondType the bond type
     * @param srcAPClass the AP class on the source attachment point
     * @param trgAPClass the AP class on the target attachment point
     */
    //TODO-V3 constructors for edge will change one ap-ownership is sorted out
    //TODO-V3 remove string-based APClass arguments
  //TODO-M7 del
    @Deprecated
    public DENOPTIMEdge(DENOPTIMAttachmentPoint srcAP,
                        DENOPTIMAttachmentPoint trgAP, int srcVertex,
                        int trgVertex, int srcAPID, int trgAPID,
                        BondType bondType, String srcAPClass,
                        String trgAPClass) {
        this(srcAP, trgAP, bondType);
    }
    
//------------------------------------------------------------------------------

    public DENOPTIMAttachmentPoint getSrcAP()
    {
        return srcAP;
    }
    
//------------------------------------------------------------------------------

    public DENOPTIMAttachmentPoint getTrgAP()
    {
        return trgAP;
    }
    
//------------------------------------------------------------------------------

    public int getSrcVertex()
    {
        return srcAP.getOwner().getVertexId();
    }
    
//------------------------------------------------------------------------------

    public int getSrcAPID()
    {
        return srcAP.getOwner().getIndexOfAP(srcAP);
    }
    
//------------------------------------------------------------------------------

    public int getTrgAPID()
    {
        return trgAP.getOwner().getIndexOfAP(trgAP);
    }        

//------------------------------------------------------------------------------

    public int getTrgVertex()
    {
        return trgAP.getOwner().getVertexId();
    }
    
//------------------------------------------------------------------------------

    //TODO-M7 del
    public void setSrcVertex(int vid)
    {
        srcAP.getOwner().setVertexId(vid);
    }

    public void setTrgVertex(int vid)
    {
        trgAP.getOwner().setVertexId(vid);
    }
//------------------------------------------------------------------------------
    
    public APClass getSrcAPClass()
    {
        return srcAP.getAPClass();
    }
    
//------------------------------------------------------------------------------
    
    public APClass getTrgAPClass()
    {
        return trgAP.getAPClass();
    }

//------------------------------------------------------------------------------

    public BondType getBondType()
    {
        return bondType;
    }
    
//------------------------------------------------------------------------------
    
    /**
     * Compares this and another edge ignoring edge and vertex IDs
     * @param other edge to compare against
     * @param reason string builder used to build the message clarifying the 
     * reason for returning <code>false</code>.
     * @return <code>true</code> if the two edges represent the same connection
     * even if the vertex IDs are different.
     */
    
//TODO-M7: use sameAs for APs
    public boolean sameAs(DENOPTIMEdge other, StringBuilder reason)
    {
    	if (this.getSrcAPID() != other.getSrcAPID())
    	{
    		reason.append("Different source atom ("+this.getSrcAPID()+":"
    						+other.getSrcAPID()+"); ");
    		return false;
    	}
    	if (this.getTrgAPID() != other.getTrgAPID())
    	{
    		reason.append("Different target atom ("+this.getTrgAPID()+":"
					+other.getTrgAPID()+"); ");
    		return false;
    	}
    	if (!this.getSrcAPClass().equals(other.getSrcAPClass()))
    	{
    		reason.append("Different source APClass ("
    				+this.getSrcAPClass()+":"
					+other.getSrcAPClass()+"); ");
    		return false;
    	}
    	if (!this.getTrgAPClass().equals(other.getTrgAPClass()))
    	{
    		reason.append("Different target APClass ("
    				+this.getTrgAPClass()+":"
					+other.getTrgAPClass()+"); ");
    		return false;
    	}
    	if (this.getBondType() != (other.getBondType()))
    	{
    		reason.append("Different bond type ("+this.getBondType()+":"
					+other.getBondType()+"); ");
    		return false;
    	}
    	return true;
    }

//------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        DENOPTIMVertex srcVertex = srcAP.getOwner();
        int srcAPID = this.getSrcAPID();
        DENOPTIMVertex trgVertex = trgAP.getOwner();
        int trgAPID = this.getTrgAPID();
        
        StringBuilder sb = new StringBuilder(64);
        sb.append(srcVertex.getVertexId()).append("_")
                .append(srcAPID).append("_").
                append(trgVertex.getVertexId()).append("_")
                .append(trgAPID).append("_").
                append(bondType.toOldString());
        if (srcAP.getAPClass()!=null && trgAP.getAPClass()!=null)
        {
            sb.append("_").append(srcAP.getAPClass()).append("_").append(
                    trgAP.getAPClass());
        }
        
        return sb.toString();
    }

//------------------------------------------------------------------------------

    /**
     * Possible chemical bond types an edge can represent.
     */
    public enum BondType {

        NONE, UNDEFINED, ANY, SINGLE, DOUBLE, TRIPLE, QUADRUPLE;

        //TODO-V3: this is to be consistent with old "int-based" internal
        // convention. Eventually, we'll not need this anymore.
        private String oldString = "1";

        private int valenceUsed = -1;

        private IBond.Order bo = null;

        static {
            ANY.bo = IBond.Order.SINGLE;
            SINGLE.bo = IBond.Order.SINGLE;
            DOUBLE.bo = IBond.Order.DOUBLE;
            TRIPLE.bo = IBond.Order.TRIPLE;
            QUADRUPLE.bo = IBond.Order.QUADRUPLE;

            SINGLE.valenceUsed = 1;
            DOUBLE.valenceUsed = 2;
            TRIPLE.valenceUsed = 3;
            QUADRUPLE.valenceUsed = 4;

            //TODO-V3 del
            SINGLE.oldString = "1";
            DOUBLE.oldString = "2";
            TRIPLE.oldString = "3";
            QUADRUPLE.oldString = "4";
        }

        /**
         * Checks if it is possible to convert this edge type into a CDK bond.
         * @return <code>true</code> if this can be converted to a CDK bond.
         */
        public boolean hasCDKAnalogue() {
            return (bo != null);
        }

        /**
         * @return the CDK {@link IBond.Order} represented by this edge type.
         */
        public Order getCDKOrder() {
            return bo;
        }

        /**
         * This method exists only to retain compatibility with old int-based
         * notation.
         * @return a string representation of the bond type
         */
        @Deprecated
        public String toOldString() {
            return oldString;
        }

        /**
         * @param i int to be parsed
         * @return the corresponding bond type, if known, or UNDEFINED.
         */
        public static BondType parseInt(int i)
        {
            switch (i)
            {
                case -1:
                    return NONE;
                case 1:
                    return SINGLE;
                case 2:
                    return DOUBLE;
                case 3:
                    return TRIPLE;
                case 4:
                    return QUADRUPLE;
                case 8:
                    return ANY;
                default:
                    return UNDEFINED;
            }
        }

        /**
         * @param string to be parsed
         * @return the corresponding bond type, if known, or UNDEFINED.
         */
        public static BondType parseStr(String string)
        {
            switch (string.trim())
            {
                case "1":
                    return SINGLE;
                case "2":
                    return DOUBLE;
                case "3":
                    return TRIPLE;
                case "4":
                    return QUADRUPLE;
                case "8":
                    return ANY;
                default:
                    return UNDEFINED;
            }
        }

        /**
         * @return the number of valences occupied by the bond analogue
         */
        public int getValence()
        {
            return valenceUsed;
        }
    }
}
