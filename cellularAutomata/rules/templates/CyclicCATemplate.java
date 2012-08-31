/*
 CyclicCATemplate -- a class within the Cellular Automaton Explorer. 
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
 * A convenience class/template for all rules that behave as so-called Cyclic
 * CA. Cyclic CA have N states, and any cell with state i is only permitted to
 * increment to state i+1. The incrementing occurs only when the cell is
 * surrounded by M or more neighbors of state i+1.
 * <p>
 * Generally, these CA produce spirals and wave-like structures. Generally, they
 * are most interesting with large extended neighborhoods.
 * <p>
 * This template assumes that all states are integers. This class handles
 * conversion of the neighbors as Cells to neighbors as integer values so that
 * the subclass only has to worry about specifying the number of surrounding
 * neighbors M necessary to trigger an increment. Optionally, the child class
 * may also specify the number of states.
 * <p>
 * This class uses the cell state
 * cellularAutomaton.cellState.model.IntegerCellState.
 * <p>
 * The number of integer states is set on the GUI and defaults to 2. This may be
 * overriden by a childclass (for example, see the Spirals rule).
 * <p>
 * This class uses the Template Method design pattern. Subclasses implement the
 * abstract getTriggerNumber() and getSurvivalValues() methods which are called
 * by the template method integerRule(). These methods return integer arrays
 * indicating how many neighbors will cause a birth or survival.
 * <p>
 * For examples see LavaLamp, CollidingCyclones, and Nucleation.
 * 
 * @author David Bahr
 */
public abstract class CyclicCATemplate extends IntegerRule
{
	// the current generation being processed by the rule
	private static volatile int currentGeneration = -1;

	// the trigger number, which is updated by a template call to the child
	// class
	private static volatile int triggerNumber = 0;

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
	public CyclicCATemplate(boolean minimalOrLazyInitialization)
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
	 * @return A new state for the cell with a value between 0 and numStates -
	 *         1.
	 */
	private int integerRule(int cellValue, int[] neighbors, int numStates,
			int generation)
	{
		// Get the trigger number. Only update this number at the beginning
		// of each generation. This prevents some cells from seeing one
		// trigger number and other cells seeing another trigger number. This
		// could happen if getTriggerNumber() is being altered by the child
		// class, particularly in a "More Properties" panel.
		if(currentGeneration != generation)
		{
			triggerNumber = getTriggerNumber();

			currentGeneration = generation;
		}

		// the integer that is returned
		int returnValue = cellValue;

		// the number of neighbors with (cellValue+1) % numStates
		int numberOfNeighborsWithCellValuePlusOne = 0;
		for(int i = 0; i < neighbors.length; i++)
		{
			if(neighbors[i] == ((cellValue + 1) % numStates))
			{
				numberOfNeighborsWithCellValuePlusOne++;
			}
		}

		// increment to the next value
		if(numberOfNeighborsWithCellValuePlusOne >= triggerNumber)
		{
			returnValue = (cellValue + 1) % numStates;
		}

		return returnValue;
	}

	/**
	 * A cell updates it state from i to i+1, but this only happens when there
	 * are M neighbors that already have value i+1. This method gets the trigger
	 * number M.
	 * 
	 * @return The number of neighbors (with value i+1) necessary to trigger an
	 *         update to the next state value (i to i+1).
	 */
	protected abstract int getTriggerNumber();

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

		// create a cell state from the value given by the rule
		IntegerCellState newCellState = new IntegerCellState(answer);

		return newCellState;
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
		String[] folders = {RuleFolderNames.CYCLIC_RULES_FOLDER};

		return folders;
	}
}
