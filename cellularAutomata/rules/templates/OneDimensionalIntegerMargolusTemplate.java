/*
 OneDimensionalntegerMargolusTemplate -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.rules.templates;

import java.util.Hashtable;

import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.IntegerCellStateView;
import cellularAutomata.lattice.MargolusOneDimensionalLattice;
import cellularAutomata.rules.IntegerRule;

/**
 * A convenience class/template for all rules that use the one-dimensional
 * Margolus neighborhood with integer states. This class handles conversion of
 * the neighbors as Cells to neighbors as integer values so that the subclass
 * only has to worry about specifying the integer function. This conversion
 * makes sure that the cells all return their state values for the same
 * generation as the current cell.
 * <p>
 * This class uses the cell state
 * cellularAutomaton.cellState.model.IntegerCellState.
 * <p>
 * The number of integer states is set on the GUI and defaults to 2.
 * <p>
 * This class uses the Template Method design pattern. Subclasses implement the
 * abstract blockRule() method which is called by the template method
 * calculateNewState().
 * 
 * @author David Bahr
 */
public abstract class OneDimensionalIntegerMargolusTemplate extends IntegerRule
{
	// the Margolus neighborhood (block) keyed by the cell.
	private static Hashtable<Cell, int[]> blockMap = new Hashtable<Cell, int[]>();

	// the current generation of the simulation
	private static int currentGeneration = 0;

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
	public OneDimensionalIntegerMargolusTemplate(
			boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);
	}

	/**
	 * Calculates a new state for the cell by finding its current generation and
	 * then requesting the state values for its neighbors for the same
	 * generation. The abstract blockRule() is then called to calculate a new
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
		// get the neighbor's values (will be integers arranged clockwise
		// starting with the neighbor closest to the northwest)
		int[] neighborValues = new int[neighbors.length];

		// get the generation of this cell
		int generation = cell.getGeneration();

		// reset the blockMap at every new generation (the block map holds the
		// values for each block at a given time step)
		if(generation != currentGeneration)
		{
			currentGeneration = generation;
			blockMap = new Hashtable<Cell, int[]>();
		}

		// the number of states that can be represented by the cell.
		int numStates = CurrentProperties.getInstance().getNumStates();

		// get neighbor values
		for(int i = 0; i < neighbors.length; i++)
		{
			int state = neighbors[i].toInt(generation);

			neighborValues[i] = state;
		}

		// get cell's value
		int cellValue = Integer.MIN_VALUE;
		try
		{
			cellValue = cell.toInt(generation);
		}
		catch(Exception e)
		{
			// do nothing
		}

		// the Margolus block
		int[] block = null;

		// the cell's array position in the block (0 is west)
		int arrayPosition = MargolusOneDimensionalLattice.WEST;
		Hashtable cellPosition = cell.getOtherCellInformation();
		if(cellPosition != null)
		{
			arrayPosition = ((Integer) cellPosition
					.get(MargolusOneDimensionalLattice.DISPLAY_NAME))
					.intValue();
		}

		// if cell is in the WEST, then save its resulting block in a hash
		// table. Then if the cell is in the other position, just get the cell
		// to the west and use it as a key to get the same block.
		// Why do this? So that the block can return a random result, and all 4
		// neighbors in that block will get the *same* random result. i.e.,
		// otherwise the block might return a different random result for each
		// neighbor. That would not conserve quantities and would not be a true
		// Margolus neighborhood.
		//
		// first see if we already saved the block
		if(arrayPosition == MargolusOneDimensionalLattice.WEST)
		{
			block = (int[]) blockMap.get(cell);
		}
		else
		{
			block = (int[]) blockMap.get(neighbors[0]);
		}

		// if did not save the block already, then get it.
		//
		// HERE'S THE ACTUAL RULE!
		// find the block rule with the cellState and neighbors inserted in the
		// correct positions (WEST or EAST)
		if(block == null)
		{
			if(arrayPosition == MargolusOneDimensionalLattice.WEST)
			{
				block = blockRule(cellValue, neighborValues[0], cell
						.getCoordinate().getColumn(), numStates, generation);

				// now save the block in a hashMap (using the WEST cell as
				// the key)
				blockMap.put(cell, block);
			}
			else if(arrayPosition == MargolusOneDimensionalLattice.EAST)
			{
				block = blockRule(neighborValues[0], cellValue, neighbors[0]
						.getCoordinate().getColumn(), numStates, generation);

				// now save the block in a hashMap (using the WEST cell as
				// the key)
				blockMap.put(neighbors[0], block);
			}
		}

		// the block is an array of values -- we just want the value in the
		// WEST or EAST that corresponds to the position of the cell.
		return new IntegerCellState(block[arrayPosition]);
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
	 * only the Margolus lattice.
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
		String[] lattices = {MargolusOneDimensionalLattice.DISPLAY_NAME};

		return lattices;
	}

	/**
	 * The rule for the cellular automaton which will be an integer function of
	 * the neighbors in a Margolus neighborhood. Returns an array of two values
	 * where each value corresponds to a position (WEST and EAST) in the
	 * Margolus neighborhood. The array positions are stored in constants in
	 * this class. For example, array[WEST] and array[EAST].
	 * <p>
	 * WARNING: This method will not be called for every index. It will only be
	 * called once per block. There are two cells per block, so only one of the
	 * cells in the block is fed to this method.
	 * 
	 * @param westCellValue
	 *            The current value of the west cell.
	 * @param eastCellValue
	 *            The current value of the east cell.
	 * @param westCellsFixedColumnPosition
	 *            The indexed column position of the west cell (0 to
	 *            numColumns-1).
	 * @param numStates
	 *            The number of states. In other words, the returned state can
	 *            only have values between 0 and numStates - 1.
	 * @param generation
	 *            The current generation of the CA.
	 * @return An array of states that corresponds to the Margolus block.
	 *         Array[0] is the west side of the block and array[1] is the east
	 *         side of the block.
	 */
	protected abstract int[] blockRule(int westCellValue, int eastCellValue,
			int westCellsFixedColumnPosition, int numStates, int generation);
}
