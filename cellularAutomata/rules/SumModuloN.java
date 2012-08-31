/*
 SumModuloN -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.rules.templates.IntegerRuleTemplate;

/**
 * A rule which calculates the sum modulo N of the neighbors.
 * 
 * @author David Bahr
 */
public class SumModuloN extends IntegerRuleTemplate
{
    // a display name for this class
    private static final String RULE_NAME = "Sum Modulo N";

    // a description of property choices that give the best results for this
    // rule (e.g., which lattice, how many states, etc.)
    private static final String BEST_RESULTS = "<html> <body><b>"
        + RULE_NAME
        + ".</b>"
        + "<p> "
        + "<b>For best results</b>, try 5 states with a single seed on a 100 by 100 "
        + "or larger square (8 neighbor) lattice.  Also try different numbers of states "
        + "like 10, 20, and 50. Try taking the running average "
        + "over 20 or 50 time steps.  Try different color schemes and "
        + "less common lattices like the Margolus (turn the grid lines on "
        + "to notice some interesting blocking behavior). "
        + leftClickInstructions + rightClickInstructions + "</body></html>";

    // a tooltip description for this class
    private String TOOLTIP = "<html> <body><b>" + RULE_NAME
        + ".</b> Cool fractals generated from the sum of all the cell's "
        + "neighbors (modulo the number of states)." + "</body></html>";

    /**
     * Create the rule corresponding that takes a sum modulo N using the given
     * cellular automaton properties.
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
    public SumModuloN(boolean minimalOrLazyInitialization)
    {
        super(minimalOrLazyInitialization);
    }

    /**
     * Calculates the sum modulo N of the cell and all its neighbors.
     * 
     * @param cell
     *            The value of the cell being updated.
     * @param neighbors
     *            The value of the neighbors.
     * @param numStates
     *            The value of N.
     * @param generation
     *            The current generation of the CA.
     * 
     * @return A new state for the cell.
     */
    protected int integerRule(int cell, int[] neighbors, int numStates,
        int generation)
    {
        int sum = cell;

        // add up the value of the cells
        for(int i = 0; i < neighbors.length; i++)
        {
            sum += neighbors[i];
        }

        // and take modulo N
        sum %= numStates;

        return sum;
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
