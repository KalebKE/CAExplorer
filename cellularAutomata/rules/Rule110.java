/*
 Rule110 -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2005  David B. Bahr (http://academic.regis.edu/dbahr/)

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

package cellularAutomata.rules;

import java.math.BigInteger;

import cellularAutomata.lattice.StandardOneDimensionalLattice;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.MinMaxBigIntPair;

/**
 * The two-state CA rule 110, numbered according to Wolfram's numbering scheme.
 * 
 * @author David Bahr
 */
public class Rule110 extends WolframRuleNumber
{
    // the wolfram rule number we are simulating
    private static final int WOLFRAM_RULE_NUMBER = 110;

    // a display name for this class
    private static final String RULE_NAME = "Rule 110";

    // a description of property choices that give the best results for this
    // rule (e.g., which lattice, how many states, etc.)
    private static final String BEST_RESULTS = "<html> <body><b>" + RULE_NAME
        + ".</b>" + "<p> "
        + "<b>For best results</b>, start from a single seed on a large "
        + "(e.g., 500 by 500) one-dim (2 neighbor) lattice. "
        + leftClickInstructions + rightClickInstructions + "</body></html>";

    // a tooltip description for this class
    private String TOOLTIP = "<html> <body><b>"
        + RULE_NAME
        + ".</b> Wolfram rule 110, capable of universal computation.</body></html>";

    /**
     * Create rule 110.
	 * <p>
	 * When calling the parent constructor, the minimalOrLazyInitialization
	 * parameter must be included as
	 * <code>super(minimalOrLazyInitialization);</code>. The boolean is
	 * intended to indicate when the constructor should build a rule with as
	 * small a footprint as possible. In order to load rules by reflection, the
	 * application must query this class for information like the display name,
	 * tooltip description, etc. At these times it makes no sense to build the
	 * complete rule which may have a large footprint in memory.
	 * <p>
	 * It is recommended that the constructor and instance variables do not
	 * initialize any memory intensive variables and that variables be
	 * initialized only when first needed (lazy initialization). Or all
	 * initializations in the constructor may be placed in an <code>if</code>
	 * statement.
	 * 
	 * <pre>
	 * if(!minimalOrLazyInitialization)
	 * {
	 *     ...initialize
	 * }
	 * </pre>
	 * 
	 * @param minimalOrLazyInitialization
	 *            When true, the constructor instantiates an object with as
	 *            small a footprint as possible. When false, the rule is fully
	 *            constructed. This variable should be passed to the super
	 *            constructor <code>super(minimalOrLazyInitialization);</code>,
	 *            but if uncertain, you may safely ignore this variable.
     */
    public Rule110(boolean minimalOrLazyInitialization)
    {
        super(minimalOrLazyInitialization);
    }

    /**
     * A brief description (written in HTML) that describes what parameters will
     * give best results for this rule (which lattice, how many states, etc).
     * The description will be displayed on the properties panel. Using html
     * permits line breaks, font colors, etcetera, as described in HTML
     * resources. Regular line breaks will not work.
     * <p>
     * Recommend starting with the title of the rule followed by "For best
     * results, ...". See Rule 102 for an example.
     * 
     * @return An HTML string describing how to get best results from this rule.
     *         May be null.
     */
    public String getBestResultsDescription()
    {
        return BEST_RESULTS;
    }
    
    /**
     * A list of lattices with which this Rule will work. This rule won't work
     * with lattices that have a variable or unknown number of neighbors.
     * 
     * @return A list of lattices compatible with this Rule (returns the display
     *         names for the lattices). Returns null if compatible with all
     *         lattices.
     */
    public String[] getCompatibleLattices()
    {
        String[] lattices = {StandardOneDimensionalLattice.DISPLAY_NAME};

        return lattices;
    }

    /**
     * When displayed for selection, the rule will be listed under specific
     * folders specified here. The rule will always be listed under the "All
     * rules" folder. And if the rule is contributed by a user and is placed in
     * the userRules folder, then it will also be shown in a folder called "User
     * rules". Any strings may be used; if the folder does not exist, then one
     * will be created with the specified name. If the folder already exists,
     * then that folder will be used.
     * <p>
     * By default, this returns null so that the rule is only placed in the
     * default folder(s).
     * <p>
     * Child classes should override this method if they want the rule to appear
     * in a specific folder. The "All rules" and "User rules" folder are
     * automatic and do not need to be specified; they are always added.
     * 
     * @return A list of the folders in which rule will be displayed for
     *         selection. May be null.
     */
    public String[] getDisplayFolderNames()
    {
        String[] folders = {RuleFolderNames.CLASSICS_FOLDER,
            RuleFolderNames.INSTRUCTIONAL_FOLDER,
            RuleFolderNames.KNOWN_UNIVERSAL_FOLDER};

        return folders;
    }

    /**
     * A brief one or two-word string describing the rule, appropriate for
     * display in a drop-down list.
     * 
     * @return A string no longer than 15 characters.
     */
    public String getDisplayName()
    {
        return RULE_NAME;
    }

    /**
     * Gets a pair of numbers for the minimum and maximum allowable rule numbers
     * for the specified lattice. When this method returns null (the default
     * value), the "rule number" display field is disabled. Sub-classes should
     * override this method to enable the rule number display field.
     * 
     * @param latticeDescription
     *            The display name of the lattice for which a min and max rule
     *            numbers will be specified.
     * @param numStates
     *            The number of states allowed for a cell on the lattice.
     * 
     * @return A pair of numbers for the minimum and maximum allowable rule
     *         numbers. May be null if the concept of a minimum and maximum does
     *         not make sense for this rule. Null is the default value.
     */
    protected MinMaxBigIntPair getMinMaxAllowedRuleNumbers(
        String latticeDescription, int numStates)
    {
        MinMaxBigIntPair pair = new MinMaxBigIntPair(WOLFRAM_RULE_NUMBER,
            WOLFRAM_RULE_NUMBER);

        return pair;
    }

    /**
     * Tells the graphics what value should be displayed for the "Rule number"
     * text field. By default, the number is whatever value was previously
     * displayed.
     * 
     * @param latticeDescription
     *            The display name of the lattice.
     * 
     * @return The rule number that will be displayed.
     */
    protected BigInteger ruleNumberToDisplay(String latticeDescription)
    {
        // fixes the rule number at RULE_NUMBER
        return new BigInteger("" + WOLFRAM_RULE_NUMBER);
    }

    /**
     * A brief description (written in HTML) that describes this rule. The
     * description will be displayed as a tooltip. Using html permits line
     * breaks, font colors, etcetera, as described in HTML resources. Regular
     * line breaks will not work.
     * 
     * @return An HTML string describing this rule.
     */
    public String getToolTipDescription()
    {
        return TOOLTIP;
    }
}
