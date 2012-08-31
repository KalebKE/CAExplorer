/*
 MultiGenerationIntegerRuleTemplate -- a class within the Cellular Automaton Explorer. 
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
// import cellularAutomata.lattice.FourNeighborSquareLattice;
// import cellularAutomata.lattice.HexagonalLattice;
// import cellularAutomata.lattice.SquareLattice;
// import cellularAutomata.lattice.StandardOneDimensionalLattice;
// import cellularAutomata.lattice.TriangularLattice;
// import cellularAutomata.lattice.TwelveNeighborTriangularLattice;
import cellularAutomata.rules.IntegerRule;
// import cellularAutomata.rules.Rule;

/**
 * A convenience class/template for all rules that behave as an integer function
 * of their neighbors, both at the current time step and for an arbitrary number
 * of previous time steps. This class handles conversion of the neighbors as
 * Cells to neighbors as integer values so that the subclass only has to worry
 * about specifying the integer function. This conversion makes sure that the
 * cells all return their state values for the same generation as the current
 * cell. <br>
 * 
 * This class uses the cell state
 * cellularAutomaton.cellState.model.IntegerCellState. <br>
 * The number of integer states is set on the GUI and defaults to 2. Neither
 * this class nor its children can change that value directly.
 * <p>
 * This class uses the Template Method design pattern. Subclasses implement the
 * abstract integerRule() method which is called by the template method
 * calculateNewState().
 * 
 * @author David Bahr
 */
public abstract class MultiGenerationIntegerRuleTemplate extends IntegerRule
{
    // the number of generations needed for the calculation
    private int numPreviousGenerations = 1;

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
    public MultiGenerationIntegerRuleTemplate(boolean minimalOrLazyInitialization)
    {
        super(minimalOrLazyInitialization);

        // the number of generations needed for the calculation
        this.numPreviousGenerations = getRequiredNumberOfGenerations();
    }

    /**
     * Calculates a new state for the cell by finding values for it and its
     * neighbors at the current and at previous generations. The number of
     * previous generations is specified by the method
     * getRequiredNumberOfGenerations(). The abstract integer function is then
     * called to calculate a new state. By convention the neighbors should be
     * indexed clockwise starting to the northwest of the cell.
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
        // get the neighbor's values (will be 0's and 1's)
        int[][] neighborValues = new int[neighbors.length][numPreviousGenerations];

        // get the generation of this cell
        int generation = cell.getGeneration();

        // get neighbor values
        for(int i = 0; i < neighbors.length; i++)
        {
            for(int n = 0; n < numPreviousGenerations; n++)
            {
                int state = neighbors[i].toInt(generation - n);
                    
                neighborValues[i][n] = state;
            }
        }

        // get cell's values
        int[] cellState = new int[numPreviousGenerations];
        for(int n = 0; n < numPreviousGenerations; n++)
        {
            cellState[n] = cell.toInt(generation - n);
        }

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
	public  CellStateView getCompatibleCellStateView()
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
     * The rule for the cellular automaton which will be an integer function of
     * the neighbors.
     * 
     * @param cellValues
     *            The values of the cell at the current generation (array index
     *            0) and at previous generations. For example, cellValues[0] is
     *            the current generation, cellValues[1] is the value at the
     *            previous generation, and cellValues[2] is the value two
     *            generations back.
     * @param neighbors
     *            The values of the neighbors at the current generation (array
     *            indices [i][0] where i is the neighbor) and at previous
     *            generations. For example, neighbor 0 at the previous
     *            generation is neighbors[0][1].
     * @param numStates
     *            The number of states. In other words, the returned state can
     *            only have values between 0 and numStates - 1.
     * @param generation
     *            The current generation of the CA.
     * 
     * @return A new state for the cell with a value between 0 and numStates -
     *         1.
     */
    protected abstract int integerRule(int[] cellValues, int[][] neighbors,
        int numStates, int generation);
}
