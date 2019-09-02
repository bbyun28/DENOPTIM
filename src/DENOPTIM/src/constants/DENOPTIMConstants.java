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

package constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;


/**
 * General set of constants used in DENOPTIM
 * @author Vishwesh Venkatraman
 * @author Marco Foscato
 */

public final class DENOPTIMConstants 
{
    /**
     * new line character
     */
    public static final String EOL = System.getProperty("line.separator");

    /**
     * file separator  
     */
    public static final String FSEP = System.getProperty("file.separator");

 
    public static final Map<String, Integer> VALENCE_MAP = createMap();
    private static Map<String, Integer> createMap()
    {
        Map<String, Integer> result = new HashMap<>();
        result.put("C", 4);
        result.put("N", 3);
        result.put("S", 6);
        result.put("As", 5);
        result.put("Cd", 2);
        result.put("Hg", 2);
        result.put("Zn", 2);
        result.put("B", 3);
        result.put("Si", 4);
        result.put("Ge", 4);
        result.put("Se", 6);
        result.put("Te", 6);
        result.put("Sb", 5);
        result.put("Sn", 4);
        result.put("Cl", 1);
        result.put("Br", 1);
        result.put("F", 1);
        result.put("I", 1);
        result.put("P", 5);
        result.put("O", 2);
        return Collections.unmodifiableMap(result);
    };
    
    public static final Set<String> ORGANIC_SUBSET_ELEMENTS = 
                    new HashSet<>(Arrays.asList(new String[] {"C",  
                        "N", "S", "O", "P", "F", "Br", "Cl", "I", "B"}
                    ));

    public static final Set<String> ALL_ELEMENTS = 
                    new HashSet<>(Arrays.asList(new String[] {"H", "He",
			        "Li", "Be", "B", "C", "N", "O", "F", "Ne",
			        "Na", "Mg", "Al", "SiP", "S", "Cl", "Ar",
			        "K", "Ca", "Ga", "Ge", "As", "Se", "Br", "Kr",
			        "Rb", "Sr", "In", "Sn", "Sb", "Te", "I", "Xe",
			        "Cs", "Ba", "Tl", "Pb", "Bi", "Po", "At", "Rn",
			        "Fr", "Ra",
			        "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni",
				"Cu", "Zn",
			        "Y", "Zr", "Nb", "Mo", "Tc", "Ru", "Rh", "Pd", 
				"Ag", "Cd",
			        "La", "Hf", "Ta", "W", "Re", "Os", "Ir", "Pt", 
				"Au", "Hg",
			        "Ac", "Ce", "Pr", "Nd", "Pm", "Sm", "Eu", "Gd",
				"Tb", "Dy",
			        "Ho", "Er", "Tm", "Yb", "Lu", "Th", "Pa", "U", 
				"Np", "Pu",
			        "Am", "Cm", "Bk", "Cf", "Es", "Fm", "Md", "No",
				"Lr"}));
    
    public static final Set<String> ALLOWED_ELEMENTS = 
                    new HashSet<>(Arrays.asList(new String[] {"C", "H", 
                        "N", "S", "O", "P", "F", "Br", "Cl", "I", "B", "As", 
                        "Si", "Sn", "Cd", "Hg", "Zn", "Te", "Ge", "Sb", "Se"}
                    ));

    public static final Set<String> ALLOWED_EXTENSIONS = 
                    new HashSet<>(Arrays.asList(new String[] {".txt", 
			".sdf", ".mol"}
                    ));

    
    
    public static final double INVPI = 1.0/Math.sqrt(Math.PI * 2);
    
    
    public static final int MOLDIGITS = 8;

    /**
     * SDF tag defining attachment points 
     */
    public static final String APTAG = "ATTACHMENT_POINT";

    /**
     * SDF tag defining attachment points (APs) with AP class and AP vector
     */
    public static final String APCVTAG = "CLASS";

    /**
     * Symbol of dummy atom
     */
    public static final String DUMMYATMSYMBOL = "Du";

    /**
     * String tag of <code>Atom</code> property used to store the unique ID of
     * the <code>DENOPTIMVertex</code> corresponding to the molecular fragment
     * to which the atom belongs.
     */
    public static final String ATMPROPVERTEXID = "DENOPTIMVertexID";

    /**
     * String tag of <code>Bond</code>'s property used to store the 
     * property of being rotatable.
     */
    public static final String BONDPROPROTATABLE = "DENOPTIMRotable";

    /**
     * Recognized types of RingClosingAttractor and compatible types
     */
    public static final Map<String,String> RCATYPEMAP =
		    new HashMap<String,String>() 
    {
        {
            put("ATP", "ATM");
            put("ATM", "ATP");
            put("ATN", "ATN");
        };
    };

    /**
     * Recognized attachment point classes of RingClosingAttractor
     */
    public static final Set<String> RCAAPCLASSSET = 
		    new HashSet<>(Arrays.asList(new String[] {"ATplus:0",
			"ATminus:0", "ATneutral:0"}
		    ));

    /**
     * Smallest difference for comparison of double and float numbers.
     */
    public static final double FLOATCOMPARISONTOLERANCE = 0.000000001;

}
