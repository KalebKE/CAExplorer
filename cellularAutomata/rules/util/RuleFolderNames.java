/*
 RuleFolderNames -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2007  David B. Bahr (http://academic.regis.edu/dbahr/)

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package cellularAutomata.rules.util;

/**
 * These are a list of commonly used folder names in which the rules are
 * displayed. When creating a rule, override the getDisplayFolderNames() method
 * to specify particular folders in which the rule will be displayed. Note that
 * all rules are displayed in certain default folders (like USER_RULES_FOLDER
 * and ALL_RULES) regardless of how getDisplayFolderNames() is overriden.
 * <p>
 * This class is used primarily by the various Rule classes and the RuleTree
 * class.
 * 
 * @author David Bahr
 */
public class RuleFolderNames
{
    /**
     * All rules are automatically added to this folder. Any rules can be placed
     * into a particular folder by overriding the getDisplayFolderNames()
     * method.
     */
    public static final String ALL_RULES_FOLDER = "All Rules";

    /**
     * This is the name of the folder into which cyclic CA are placed. All rules
     * can be placed into folders by overriding the getDisplayFolderNames()
     * method. (I use this for CA that I deem classics. Sorry if others disagree
     * with my choices!)
     */
    public static final String CLASSICS_FOLDER = "Classics";

    /**
     * Rules that use complex number values (rather than integers) are placed
     * into the folder with this name. All rules can be placed into folders by
     * overriding the getDisplayFolderNames() method.
     */
    public static final String COMPLEX_VALUED_FOLDER = "Complex Numbered";

    /**
     * Computationally intensive rules (that should be avoided by machines with
     * little memory and weak processors) are placed into the folder with this
     * name. All rules can be placed into folders by overriding the
     * getDisplayFolderNames() method.
     */
    public static final String COMPUTATIONALLY_INTENSIVE_FOLDER = "Computationally Intensive";

    /**
     * This is the name of the folder into which cyclic CA are placed. All rules
     * can be placed into folders by overriding the getDisplayFolderNames()
     * method.
     */
    public static final String CYCLIC_RULES_FOLDER = "Cyclic (and Extensions)";

    /**
     * This is the name of the folder into which instructional rules are placed.
     * All rules can be placed into folders by overriding the
     * getDisplayFolderNames() method. (I use this choice for the rules that I
     * demonstrate in class -- sorry if others disagree with my choices!)
     */
    public static final String INSTRUCTIONAL_FOLDER = "Instructional";

    /**
     * This is the name of the folder into which life-based (birth/death) rules
     * are placed. All rules can be placed into folders by overriding the
     * getDisplayFolderNames() method.
     */
    public static final String LIFE_LIKE_FOLDER = "Life-Like (Birth/Death Rules)";

    /**
     * This is the name of the folder into which rules that are known to be
     * universal placed. All rules can be placed into folders by overriding the
     * getDisplayFolderNames() method.
     */
    public static final String KNOWN_UNIVERSAL_FOLDER = "Known to be Universal";

    /**
     * Rules used in obesity research are placed in this folder. All rules can
     * be placed into folders by overriding the getDisplayFolderNames() method.
     */
    public static final String OBESITY_RESEARCH_FOLDER = "Obesity Research";

    /**
     * Outer totalistic rules are placed into the folder with this name. All
     * rules can be placed into folders by overriding the
     * getDisplayFolderNames() method.
     */
    public static final String OUTER_TOTALISTIC_FOLDER = "Outer Totalistic";

    /**
     * Rules obviously applicable to physics are placed into the folder with
     * this name. All rules can be placed into folders by overriding the
     * getDisplayFolderNames() method.
     */
    public static final String PHYSICS_FOLDER = "Physics Applications";

    /**
     * "Pretty" rules are placed into the folder with this name. All rules can
     * be placed into folders by overriding the getDisplayFolderNames() method.
     */
    public static final String PRETTY_FOLDER = "Pretty (To Some Anyway)";

    /**
     * Probabilistic rules are placed into the folder with this name. All rules
     * can be placed into folders by overriding the getDisplayFolderNames()
     * method.
     */
    public static final String PROBABILISTIC_FOLDER = "Probabilistic";

    /**
     * Rules that use real values (rather than integers) are placed into the
     * folder with this name. All rules can be placed into folders by overriding
     * the getDisplayFolderNames() method.
     */
    public static final String REAL_VALUED_FOLDER = "Real Numbered";

    /**
     * Rules obviously applicable to social sciences are placed into the folder
     * with this name. All rules can be placed into folders by overriding the
     * getDisplayFolderNames() method.
     */
    public static final String SOCIAL_FOLDER = "Social Applications";

    /**
     * Rules with very unusual shapes are placed into the folder
     * with this name. All rules can be placed into folders by overriding the
     * getDisplayFolderNames() method.
     */
    public static final String UNUSUAL_SHAPES = "Unusual Shapes";
    
    /**
     * All user contributed rules are automatically added to this folder (if
     * they are in the userRules folder). All rules can be placed into folders
     * by overriding the getDisplayFolderNames() method.
     */
    public static final String USER_RULES_FOLDER = "User Rules";
}
