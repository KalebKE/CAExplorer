/*
 BriansBrain -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2006  David B. Bahr (http://academic.regis.edu/dbahr/)

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

import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.rules.templates.LifeExtensionsTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.MinMaxBigIntPair;
import cellularAutomata.util.MinMaxIntPair;

/**
 * Brian's Brain rule (discovered/popularized by Brian Silverman).
 * 
 * @author David Bahr
 */
public class BriansBrain extends LifeExtensionsTemplate
{
    // The number of states for this rule
    private static final int NUMBER_OF_STATES = 3;

    // a display name for this class
    private static final String RULE_NAME = "Brian's Brain";

    // a description of property choices that give the best results for this
    // rule (e.g., which lattice, how many states, etc.)
    private static final String BEST_RESULTS = "<html> <body><b>"
        + RULE_NAME
        + ".</b>"
        + "<p> "
        + "<b>For best results</b>, try a random initial population on a square (8 neighbor) "
        + "lattice.  Or select the firing state \"1\" from the color menu, and draw two "
        + "adjacent cells. For an interesting twist, try taking running averages of 10, 15, "
        + "or 20 time steps.  Selecting high contrast color schemes works best for running "
        + "averages. " + leftClickInstructions + rightClickInstructions
        + "</body></html>";

    // a tooltip description for this class
    private String TOOLTIP = "<html> <body><b>" + RULE_NAME
        + ".</b> Similar to Life, "
        + "this rule generates gliders and complex patterns.</body></html>";

    /**
     * Creates "Brian's Brain" from a Life-like rule.
     * <p>
     * When calling the parent constructor, the minimalOrLazyInitialization
     * parameter must be included as shown. The boolean is intended to indicate
     * when the constructor should build a rule with as small a footprint as
     * possible. In order to load rules by reflection, the application must
     * query this class for information like the display name, tooltip
     * description, etc. At these times it makes no sense to build the complete
     * rule which may have a large footprint in memory.
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
    public BriansBrain(boolean minimalOrLazyInitialization)
    {
        super(minimalOrLazyInitialization);

        if(!minimalOrLazyInitialization)
        {
            // change the drawing color and state
            ColorScheme colorScheme = CellStateView.colorScheme;
            ColorScheme.DRAW_COLOR = colorScheme.getColor(1, NUMBER_OF_STATES,
                this.getCompatibleCellStateView());
            IntegerCellState.DRAW_STATE = 1;
        }
    }

    /**
     * Returns an array of values that indicates how many neighbors must have
     * value 1 in order for the cell to birth (in other words, be assigned a
     * value of 1). For example, in the game of Life, the cell births if it has
     * exactly 3 neighbors that are 1's. So appropriate code would be <code>
     * protected int[] getBirthValues()
     * {
     *      int[] birthArray = {3};
     *      return birthArray;
     * }
     * </code>
     * 
     * @return An array of values that indicates how many neighbors must have
     *         value 1 in order for the cell to birth (in other words, be
     *         assigned a value of 1).
     */
    protected int[] getBirthValues()
    {
        int[] birthValues = {2};
        return birthValues;
    }

    /**
     * Returns an array of values that indicates how many neighbors must have
     * value 1 in order for the cell to survive. For example, in the game of
     * Life, the cell survives if it has 2 or 3 neighbors that are 1's. So
     * appropriate code would be <code>
     * protected int[] getSurvivalValues()
     * {
     *      int[] survivalArray = {2, 3};
     *      return survivalArray;
     * }
     * </code>
     * 
     * @return An array of values that indicates how many neighbors must have
     *         value 1 in order for the cell to survive.
     */
    protected int[] getSurvivalValues()
    {
        int[] survivalValues = {};
        return survivalValues;
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
        String[] folders = {RuleFolderNames.LIFE_LIKE_FOLDER,
            RuleFolderNames.CLASSICS_FOLDER};

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
     * Gets a pair of numbers for the minimum and maximum allowable states for
     * the specified lattice. Sub-classes should override this method if the
     * default min and max is inappropriate. If returns null, then the "Number
     * of States" text field will be disabled; in other words, the user will be
     * unable to enter the number of states. It is recommended that in that
     * case, the programmer should also specify the number of states in
     * stateValueToDisplay(). Also note that the programmer can still alter the
     * number of states at any time by setting the value within the rule's code.
     * For example, the "More Properties" button may allow the user to change
     * the number of states in a separate field placed there. In that case, the
     * programmer should be careful to set the property value for the new state
     * value. For example,
     * properties.setProperty(CAPropertyReader.NUMBER_OF_STATES, numStates);
     * 
     * @param latticeDescription
     *            The display name of the lattice for which a min and max state
     *            will be specified.
     * @return A pair of numbers for the minimum and maximum allowable states.
     *         May be null if there is no maximum, or if the concept of a
     *         minimum and maximum does not make sense for this rule.
     */
    protected MinMaxIntPair getMinMaxAllowedStates(String latticeDescription)
    {
        // disables the �Number of States� text field
        return null;
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
     * @return A pair of numbers for the minimum and maximum allowable rule
     *         numbers. May be null if the concept of a minimum and maximum does
     *         not make sense for this rule. Null is the default value.
     */
    protected MinMaxBigIntPair getMinMaxAllowedRuleNumbers(
        String latticeDescription, int numStates)
    {
        return null;
    }

    /**
     * Finds the value of the state that will be displayed in the "Number of
     * States" text field.
     * 
     * @param latticeDescription
     *            The display name of the lattice for which the state value will
     *            be determined.
     * @param ruleDescription
     *            The display name of the rule for which the state value will be
     *            determined.
     * @return The state value should be displayed for the "Number of States"
     *         text field. When null, will display the value currently in the
     *         text field.
     */
    protected Integer stateValueToDisplay(String latticeDescription)
    {
        // fixes the state value at NUMBER_OF_STATES
        return new Integer(NUMBER_OF_STATES);
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