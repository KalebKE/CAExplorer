/*
 LifeExtensionsTemplate -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.rules.templates;

import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.IntegerCellStateView;
import cellularAutomata.rules.IntegerRule;
import cellularAutomata.rules.util.RuleFolderNames;

/**
 * A convenience class/template for all rules that behave similar to the Life
 * rule with births occurring when a certain number of neighbors are occupied,
 * and survivals occurring when a certain number of neighbors are occupied. For
 * example, the author of a new rule may choose to make a cell be born when
 * there are 2 or 3 or 6 surrounding cells; and the cell may survive if there
 * are 1, 7, or 8 neighboring cells.
 * <p>
 * With two states (i.e., 0 and 1), this template creates standard outer
 * totalistic rules. If there are N states (greater than 2), then only the
 * single 1 state is considered when birthing new cells. If a cell with value 1
 * does not survive, then it slowly dies. In subsequent generations, the dying
 * cell is incremented to a 2, 3, 4, ... , N-1. After reaching N-1, the cell
 * dies (becomes 0). Why on earth do we do this weird and slow dying? Well, it
 * is an odd but popular choice that produces many "pretty" CA.
 * <p>
 * This template assumes that all states are integers. This class handles
 * conversion of the neighbors as Cells to neighbors as integer values so that
 * the subclass only has to worry about specifying what number of cells causes a
 * birth and what number of cells will cause a death. Note: this conversion
 * makes sure that the cells all return their state values for the same
 * generation as the current cell.
 * <p>
 * This class uses the cell state
 * cellularAutomaton.cellState.model.IntegerCellState.
 * <p>
 * The number of integer states is set on the GUI and defaults to 2. This may be
 * overriden by a child class (for example, see the Spirals rule).
 * <p>
 * This class uses the Template Method design pattern. Subclasses implement the
 * abstract getBirthValues() and getSurvivalValues() methods which are called by
 * the template method integerRule(). These methods return integer arrays
 * indicating how many neighbors will cause a birth or survival.
 * 
 * @author David Bahr
 */
public abstract class LifeExtensionsTemplate extends IntegerRule
{
    /**
     * Create a rule using the given cellular automaton properties.
     * <p>
     * When building child classes, the minimalOrLazyInitialization parameter
     * must be included but may be ignored. However, the boolean is intended to
     * indicate when the child's constructor should build a rule with as small a
     * footprint as possible. In order to load rules by reflection, the
     * application must query the child classes for information like their
     * display names, tooltip descriptions, etc. At these times it makes no
     * sense to build the complete rule which may have a large footprint in
     * memory.
     * <p>
     * It is recommended that the child's constructor and instance variables do
     * not initialize any variables and that variables be initialized only when
     * first needed (lazy initialization). Or all initializations in the
     * constructor may be placed in an <code>if</code> statement.
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
     *            constructed. If uncertain, set this variable to false.
     */
    public LifeExtensionsTemplate(boolean minimalOrLazyInitialization)
    {
        super(minimalOrLazyInitialization);
    }

    /**
     * The rule for the cellular automaton which will be an integer function of
     * the neighbors.
     * 
     * @param cellValue
     *            The current value of the cell being updated.
     * @param neighbors
     *            The neighbors as their integer values.
     * @param numStates
     *            The number of states. In other words, the returned state can
     *            only have values between 0 and numStates - 1.
     * @param generation
     *            The current generation of the CA.
     * 
     * @return A new state for the cell with a value between 0 and numStates -
     *         1.
     */
    private int integerRule(int cellValue, int[] neighbors, int numStates,
        int generation)
    {
        // the integer that is returned
        int returnValue = 0;

        // find how many neighbors are 1's. This value may cause a birth if the
        // cell is empty (0).
        int numberOfOnes = 0;
        for(int i = 0; i < neighbors.length; i++)
        {
            if(neighbors[i] == 1)
            {
                numberOfOnes++;
            }
        }

        // if the cell is empty, then we may need to birth a cell (turn it to a
        // 1)
        if(cellValue == 0)
        {
            // get the number of neighbors that will cause a birth (there may be
            // many values that cause a birth)
            int[] valuesThatCauseBirth = getBirthValues();
            if(valuesThatCauseBirth != null)
            {
                for(int i = 0; i < valuesThatCauseBirth.length; i++)
                {
                    // if the neighbors have the same number of 1's that are
                    // required for a birth, then the cell is turned into a 1
                    // (births)
                    if(numberOfOnes == valuesThatCauseBirth[i])
                    {
                        returnValue = 1;
                    }
                }
            }
        }
        else if(cellValue == 1)
        {
            // get the number of neighbors that will cause a survival (there may
            // be many values that cause a survival)
            int[] valuesThatCauseSurvival = getSurvivalValues();
            if(valuesThatCauseSurvival != null)
            {
                for(int i = 0; i < valuesThatCauseSurvival.length; i++)
                {
                    // if the neighbors have the same number of 1's that are
                    // required for a survival, then the cell is maintained as a
                    // 1 (survival)
                    if(numberOfOnes == valuesThatCauseSurvival[i])
                    {
                        returnValue = 1;
                    }
                }
            }

            // did the cell survive? If returnValue is still 0, then no.
            if(returnValue == 0)
            {
                // so begin the death process (add 1 to it's value)
                returnValue = (cellValue + 1) % numStates;
            }
        }
        else
        {
            // continue the death process (add 1 to it's value)
            returnValue = (cellValue + 1) % numStates;
        }

        return returnValue;
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
     * <p>
     * Note that the cell itself is NOT counted as a neighbor.
     * 
     * @return An array of values that indicates how many neighbors must have
     *         value 1 in order for the cell to birth (in other words, be
     *         assigned a value of 1). May be an empty array or null.
     */
    protected abstract int[] getBirthValues();

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
     * <p>
     * Note that the cell itself is NOT counted as a neighbor.
     * 
     * @return An array of values that indicates how many neighbors must have
     *         value 1 in order for the cell to survive. May be an empty array
     *         or null.
     */
    protected abstract int[] getSurvivalValues();

    /**
     * Calculates a new state for the cell by finding its current generation and
     * then requesting the state values for its neighbors for the same
     * generation. The integerRule function is then called to calculate a new
     * state. By convention the neighbors should be indexed clockwise starting
     * to the northwest of the cell.
     * 
     * @param cell
     *            The cell being updated.
     * @param neighbors
     *            The cells on which the update is based (usually neighboring
     *            cells). By convention the neighbors should be indexed
     *            clockwise starting to the northwest of the cell. May be null
     *            if want this method to find the "neighboring" cells.
     * 
     * @return A new state for the cell.
     */
    public final CellState calculateNewState(Cell cell, Cell[] neighbors)
    {
        // get the neighbor's values (will be 0's, 1's, etc.)
        int[] neighborValues = new int[neighbors.length];

        // get the generation of this cell
        int generation = cell.getGeneration();

        // get neighbor values
        for(int i = 0; i < neighbors.length; i++)
        {
            int state = neighbors[i].toInt(generation);

            neighborValues[i] = state;
        }

        // get cell's value
        int cellState = cell.toInt(generation);

        // the number of states that can be represented by the cell.
        int numStates = CurrentProperties.getInstance().getNumStates();

        // HERE'S THE ACTUAL RULE!
        int answer = integerRule(cellState, neighborValues, numStates,
            generation);

        return new IntegerCellState(answer);
    }

    /**
     * Gets an instance of the CellState class that is compatible with this rule
     * (must be the same as the type returned by the method
     * calculateNewState()). The values assigned to this instance are
     * unimportant because it will only be used to construct instances of this
     * class type using reflection. Appropriate cellStates to return include
     * HexagonalBinaryCellState and SquareBinaryCellState.
     * 
     * @return An instance of the CellState (its state values are unimportant).
     */
    public final CellState getCompatibleCellState()
    {
        return new IntegerCellState(0);
    }

    /**
     * Gets an instance of the CellStateView class that will be used to display
     * cells being updated by this rule. Note: This method must return a view
     * that is able to display cell states of the type returned by the method
     * getCompatibleCellState(). Appropriate CellStatesViews to return include
     * BinaryCellStateView, IntegerCellStateView, HexagonalIntegerCellStateView,
     * IntegerVectorArrowView, IntegerVectorDefaultView, and
     * RealValuedDefaultView among others. the user may also create their own
     * views (see online documentation).
     * <p>
     * Any values passed to the constructor of the CellStateView should match
     * those values needed by this rule.
     * 
     * @return An instance of the CellStateView (any values passed to the
     *         constructor of the CellStateView should match those values needed
     *         by this rule).
     */
    public CellStateView getCompatibleCellStateView()
    {
        return new IntegerCellStateView();
    }

    /**
     * A list of lattices with which this Rule will work; in this case, returns
     * all lattices by default, though child classes may wish to override this
     * and restrict the lattices with which the child rule will work.
     * <p>
     * 
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
        // String[] lattices = {SquareLattice.DISPLAY_NAME,
        // HexagonalLattice.DISPLAY_NAME,
        // StandardOneDimensionalLattice.DISPLAY_NAME,
        // TriangularLattice.DISPLAY_NAME,
        // TwelveNeighborTriangularLattice.DISPLAY_NAME,
        // FourNeighborSquareLattice.DISPLAY_NAME};

        return null;
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
        String[] folders = {RuleFolderNames.LIFE_LIKE_FOLDER};

        return folders;
    }
}
