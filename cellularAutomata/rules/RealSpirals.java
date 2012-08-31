/*
 RealSpirals -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2008  David B. Bahr (http://academic.regis.edu/dbahr/)

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

import cellularAutomata.rules.templates.RealRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;

/**
 * Cells always increase in value, but they increase by double the normal amount
 * if surrounded by 2 or more neighbors with larger values. Modulos by the
 * maximum value.
 * 
 * @author David Bahr
 */
public class RealSpirals extends RealRuleTemplate
{
    // The maximum allowed value. DO NOT recommend using Double.MAX_VALUE
    // unless carefully checking for infinity.
    private static final double MAX_VALUE = 1000.0;

    // The minimum allowed value. DO NOT recommend using Double.MIN_VALUE
    // unless carefully checking for infinity.
    private static final double MIN_VALUE = 0.0;

    // a display name for this class
    private static final String RULE_NAME = "Real Spirals";

    // a description of property choices that give the best results for this
    // rule (e.g., which lattice, how many states, etc.)
    private static final String BEST_RESULTS = "<html> <body><b>"
        + RULE_NAME
        + ".</b>"
        + "<p> "
        + "<b>For best results</b>, try a 90% random initial population on a 50 by "
        + "50 square (8 neighbor) lattice with the Fire color scheme. Also try a "
        + "100 by 100 square (24 next-nearest) lattice.  A 14 by 14 filled rectangle " +
                "creates interesting patterns." + leftClickInstructions
        + rightClickInstructions + "</body></html>";

    // a tooltip description for this class
    private String TOOLTIP = "<html> <body><b>" + RULE_NAME
        + ".</b> Spirals, similar to a cyclic CA but built from real "
        + "numbers.</body></html>";

    /**
     * Cells always increase in value, but they increase by double the normal
     * amount if surrounded by 2 or more neighbors with larger values. Modulos
     * by the maximum value.
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
    public RealSpirals(boolean minimalOrLazyInitialization)
    {
        super(MIN_VALUE, MAX_VALUE, minimalOrLazyInitialization);
    }

    /**
     * Cells always increase in value, but they increase by double the normal
     * amount if surrounded by 2 or more neighbors with larger values. Modulos
     * by the maximum value.
     * 
     * @see cellularAutomata.rules.templates.RealRuleTemplate#doubleRule(
     *      double, double[], int)
     */
    public double doubleRule(double cell, double[] neighbors, int generation)
    {
        // AWESOME SPIRALS!
        double returnValue = cell;

        // trigger value
        int n = 2;

        // only increase the value of the cell if surrounded by N with bigger
        // values
        int count = 0;
        for(int i = 0; i < neighbors.length; i++)
        {
            if(neighbors[i] > cell + 20.0)
            {
                count++;
            }
        }

        if(count > n)
        {
            returnValue = cell + 20.0;
        }
        else
        {
            returnValue = cell + 10.0;
        }

        // modulo the max value
        if(returnValue > MAX_VALUE)
        {
            returnValue = ((returnValue / MAX_VALUE) - Math.floor(returnValue
                / MAX_VALUE))
                * MAX_VALUE;
        }

        return returnValue;
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
     * @see cellularAutomata.rules.Rule#getDisplayName()
     */
    public String getDisplayName()
    {
        return RULE_NAME;
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
        String[] folders = {RuleFolderNames.REAL_VALUED_FOLDER};

        return folders;
    }

    /**
     * Gets the maximum permissible value for each element of the vector. Do not
     * recommend using Double.MAX_VALUE unless your code very carefully checks
     * for instances of Infinity. <br>
     * This method is used by the RealVectorRuleTemplate class to properly
     * construct a RealValuedVectorState.
     * 
     * @return The maximum permissible value.
     * @see cellularAutomata.rules.templates.RealVectorRuleTemplate#getMaximumPermissibleValue()
     */
    public double getMaximumPermissibleValue()
    {
        return MAX_VALUE;
    }

    /**
     * Gets the minimum permissible value for each element of the vector. Do not
     * recommend using Double.MIN_VALUE unless your code very carefully checks
     * for instances of Infinity. <br>
     * This method is used by the RealVectorRuleTemplate class to properly
     * construct a RealValuedVectorState.
     * 
     * @return The minimum permissible value.
     * @see cellularAutomata.rules.templates.RealVectorRuleTemplate#getMinimumPermissibleValue()
     */
    public double getMinimumPermissibleValue()
    {
        return MIN_VALUE;
    }

    /**
     * A brief description (written in HTML) that describes this rule. The
     * description will be displayed as a tooltip. Using html permits line
     * breaks, font colors, etcetera, as described in HTML resources. Regular
     * line breaks will not work.
     * 
     * @return An HTML string describing this rule.
     * @see cellularAutomata.rules.Rule#getToolTipDescription()
     */
    public String getToolTipDescription()
    {
        return TOOLTIP;
    }
}
