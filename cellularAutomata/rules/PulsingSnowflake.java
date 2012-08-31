/*
 PulsingSnowflake -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.Cell;
import cellularAutomata.lattice.FourNeighborSquareLattice;
import cellularAutomata.rules.templates.IntegerRuleWithCellsTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.MinMaxIntPair;

/**
 * Similar to the Q2R Ising rule, but with three states. Each state is mapped to
 * -1, 0, and 1. When the change in energy is zero, the state flips to the
 * negative of its current value.
 * 
 * @author David Bahr
 */
public class PulsingSnowflake extends IntegerRuleWithCellsTemplate
{
    /**
     * A display name for this class.
     */
    public static final String RULE_NAME = "Pulsing Snowflake";

    // a tooltip description for this rule
    private static final String TOOLTIP = "<html> <body><b>Pulsing Snowflake.</b> "
        + "Creates oscillating and laser pulsing snow flakes.</body></html>";

    // a description of property choices that give the best results for this
    // rule (e.g., which lattice, how many states, etc.)
    private static final String BEST_RESULTS = "<html> <body><b>Pulsing Snowflake.</b>"
        + "<p> <b>For best results</b>, try a large lattice with an even number of "
        + "rows and columns (e.g., 150 by 150) and a single seed for an initial "
        + "condition. <br><br>"
        + "As an initial state, also try an unfilled 41 by 41 rectangle on a 150 "
        + "by 150 lattice. Interesting patterns result by varying the rectangle "
        + "and/or lattice dimensions by just 1 unit. "
        + leftClickInstructions
        + rightClickInstructions + "</body></html>";

    /**
     * Gets a number from the properties and sets up an appropriate cellular
     * automaton rule based on that number.
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
    public PulsingSnowflake(boolean minimalOrLazyInitialization)
    {
        super(minimalOrLazyInitialization);
    }

    /**
     * Similar to Q2R, but with three states. Each state is mapped to -1, 0, and
     * 1. When the change in energy is zero, the state flips to the negative of
     * its current value.
     * 
     * @param cell
     *            The values of the cell being updated.
     * @param neighbors
     *            The values of the neighbors.
     * @param generation
     *            The current generation of the CA.
     * @return A new state for the cell.
     */
    protected int integerRule(int cell, int[] neighbors, int numStates,
        int generation, Cell theCell, Cell[] cellNeighors)
    {
        // the current spin of the cell (-1, 0, or 1). Converts "0's and 1's and
        // 2's" to "-1's, 0's, and 1's" which are traditionally used in Ising
        // models.
        int currentSpin = cell - 1;

        // The new spin of the cell. Unless it is "flipped" below, this makes
        // the new spin the same as the current spin
        int newSpin = currentSpin;

        // The von Neumann neighborhood is broken into an alternating
        // checkerboard pattern. Each time step, uses the complement
        // checkerboard. This prevents neighboring spins from both changing and
        // accidentally raising the energy. See Cellular Automata Machines by
        // Toffoli and Margolus pg. 188 for a nice explanation of why this is
        // necessary.
        int cellRow = theCell.getCoordinate().getRow();
        int cellCol = theCell.getCoordinate().getColumn();
        if((generation % 2 == 0 && (cellRow + cellCol) % 2 == 0)
            || (generation % 2 == 1 && (cellRow + cellCol) % 2 == 1))
        {
            // At even time steps, this will only use the "red" cells on a
            // checkerboard overlaid on the von Neumann lattice. At odd time
            // steps this will only use cells on the "black" checkerboard
            // squares.

            // Converts "0's and 1's and 2's" to "-1's, 0's, and 1's" which are
            // traditionally used in Ising models. Then keeps a running total of
            // all the neighbors.
            int totalNeighborhoodSpin = 0;
            for(int i = 0; i < neighbors.length; i++)
            {
                totalNeighborhoodSpin += 2 * neighbors[i] - 1;
            }

            // flip the spin if it conserves energy (same number of neighbors
            // are spin up and spin down). The sum of the neighbors will be 0 if
            // there are the same number of spin up and spin down.
            if(totalNeighborhoodSpin == 0)
            {
                // then flip
                newSpin = -currentSpin;
            }
        }

        // convert back from "-1, 0, and 1" to "0 1, and 2".
        int convertBackValue = newSpin + 1;

        return convertBackValue;
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
     * A list of lattices with which this Rule will work; in this case, returns
     * all lattices by default, though child classes may wish to override this
     * and restrict the lattices with which the child rule will work.
     * <p>
     * Well-designed Rules should work with any lattice, but some may require
     * particular topological or geometrical information (like the lattice gas).
     * Appropriate strings to return in the array include
     * SquareLattice.DISPLAY_NAME, HexagonalLattice.DISPLAY_NAME,
     * StandardOneDimensionalLattice.DISPLAY_NAME, etc. If null, will be
     * compatible with all lattices.
     * 
     * @return A list of lattices compatible with this Rule (returns the display
     *         names for the lattices). Returns null if compatible with all
     *         lattices.
     */
    public String[] getCompatibleLattices()
    {
        String[] lattices = {FourNeighborSquareLattice.DISPLAY_NAME};
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
        String[] folders = {RuleFolderNames.PRETTY_FOLDER};

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
     * Returns null to disable the "Number of States" text field.
     */
    protected MinMaxIntPair getMinMaxAllowedStates(String latticeDescription)
    {
        return null;
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

    /**
     * The value that will be displayed for the state. Should always be a 2 for
     * the standard Ising model.
     */
    protected Integer stateValueToDisplay(String latticeDescription)
    {
        return new Integer(3);
    }
}
