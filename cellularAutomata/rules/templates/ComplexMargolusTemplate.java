/*
 ComplexMargolusTemplate -- a class within the Cellular Automaton Explorer. 
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

import javax.swing.JOptionPane;

import cellularAutomata.Cell;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.ComplexState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.ComplexModulusView;
import cellularAutomata.lattice.MargolusLattice;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.math.Complex;

/**
 * A convenience class/template for all rules that use the Margolus neighborhood
 * with complex number states. This class handles conversion of the neighbors as
 * Cells to neighbors as complex numbers so that the subclass only has to worry
 * about specifying the complex-valued function. This conversion makes sure that
 * the cells all return their state values for the same generation as the
 * current cell.
 * <p>
 * This class uses the cell state
 * cellularAutomaton.cellState.model.IntegerCellState.
 * <p>
 * This class uses the Template Method design pattern. Subclasses implement the
 * abstract blockRule() method which is called by the template method
 * calculateNewState().
 * 
 * @author David Bahr
 */
public abstract class ComplexMargolusTemplate extends Rule
{
	// the Margolus neighborhood (block) keyed by the cell in the northwest
	// corner.
	private static Hashtable<Cell, Complex[]> blockMap = new Hashtable<Cell, Complex[]>();

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
	public ComplexMargolusTemplate(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		// check for error
		if(getAlternateState() == null)
		{
			// Failed because the state can't be null
			String warning = "For any child class of ComplexRuleTemplate, \n"
					+ "the alternate state cannot be null.";
			JOptionPane.showMessageDialog(null, warning, "Developer Warning",
					JOptionPane.WARNING_MESSAGE);
		}
		else if(getEmptyState() == null)
		{
			// Failed because the state can't be null
			String warning = "For any child class of ComplexRuleTemplate, \n"
					+ "the empty state cannot be null.";
			JOptionPane.showMessageDialog(null, warning, "Developer Warning",
					JOptionPane.WARNING_MESSAGE);
		}
		else if(getFullState() == null)
		{
			// Failed because the state can't be null
			String warning = "For any child class of ComplexRuleTemplate, \n"
					+ "the full state cannot be null.";
			JOptionPane.showMessageDialog(null, warning, "Developer Warning",
					JOptionPane.WARNING_MESSAGE);
		}
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
		// get the neighbor's values (will be complex numbers arranged clockwise
		// starting with the neighbor closest to the northwest)
		Complex[] neighborValues = new Complex[neighbors.length];

		// get the generation of this cell
		int generation = cell.getGeneration();

		// reset the blockMap at every new generation (the block map holds the
		// values for each block at a given time step)
		if(generation != currentGeneration)
		{
			currentGeneration = generation;
			blockMap = new Hashtable<Cell, Complex[]>();
		}

		// get neighbor values
		for(int i = 0; i < neighbors.length; i++)
		{
			Complex state = (Complex) neighbors[i].getState(generation)
					.getValue();

			neighborValues[i] = state;
		}

		// get cell's value
		Complex cellValue = getEmptyState();
		try
		{
			cellValue = (Complex) cell.getState(generation).getValue();
		}
		catch(Exception e)
		{
			// do nothing
		}

		// the 2 by 2 Margolus block
		Complex[] block = null;

		// the cell's array position in the block (0 is northwest, 1 is
		// northeast, 2 is southeast, 3 is southwest -- see the constants in the
		// MargolusLattice class)
		int arrayPosition = MargolusLattice.NORTHWEST;
		Hashtable cellPosition = cell.getOtherCellInformation();
		if(cellPosition != null)
		{
			arrayPosition = ((Integer) cellPosition
					.get(MargolusLattice.DISPLAY_NAME)).intValue();
		}

		// if cell is in the NORTHWEST, then save its resulting block in a hash
		// table. Then if the cell is in any other corner, just (1) get the cell
		// in the northwest corner and use it as a key to get the same block.
		// Why do this? So that the block can return a random result, and all 4
		// neighbors in that block will get the *same* random result. i.e.,
		// otherwise the block might return a different random result for each
		// neighbor. That would not conserve quantities and would not be a true
		// Margolus neighborhood.
		//
		// first see if we already saved the block
		if(arrayPosition == MargolusLattice.NORTHWEST)
		{
			block = (Complex[]) blockMap.get(cell);
		}
		else
		{
			block = (Complex[]) blockMap.get(neighbors[0]);
		}

		// if did not save the block already, then get it.
		//
		// HERE'S THE ACTUAL RULE!
		// find the block rule with the cellState and neighbors inserted in the
		// correct positions (NW, NE, SE, or SW)
		if(block == null)
		{
			if(arrayPosition == MargolusLattice.NORTHWEST)
			{
				block = blockRule(cellValue, neighborValues[0],
						neighborValues[1], neighborValues[2], cell
								.getCoordinate().getRow(), cell.getCoordinate()
								.getColumn(), generation);

				// now save the block in a hashMap (using the NORTHWEST cell as
				// the key)
				blockMap.put(cell, block);
			}
			else if(arrayPosition == MargolusLattice.NORTHEAST)
			{
				block = blockRule(neighborValues[0], cellValue,
						neighborValues[1], neighborValues[2], neighbors[0]
								.getCoordinate().getRow(), neighbors[0]
								.getCoordinate().getColumn(), generation);

				// now save the block in a hashMap (using the NORTHWEST cell as
				// the key)
				blockMap.put(neighbors[0], block);
			}
			else if(arrayPosition == MargolusLattice.SOUTHEAST)
			{
				block = blockRule(neighborValues[0], neighborValues[1],
						cellValue, neighborValues[2], neighbors[0]
								.getCoordinate().getRow(), neighbors[0]
								.getCoordinate().getColumn(), generation);

				// now save the block in a hashMap (using the NORTHWEST cell as
				// the key)
				blockMap.put(neighbors[0], block);
			}
			else if(arrayPosition == MargolusLattice.SOUTHWEST)
			{
				block = blockRule(neighborValues[0], neighborValues[1],
						neighborValues[2], cellValue, neighbors[0]
								.getCoordinate().getRow(), neighbors[0]
								.getCoordinate().getColumn(), generation);

				// now save the block in a hashMap (using the NORTHWEST cell as
				// the key)
				blockMap.put(neighbors[0], block);
			}
		}

		// the block is an array of values -- we just want the value in the
		// NW, NE, SE, or SW that corresponds to the position of the cell.
		return new ComplexState(block[arrayPosition], getAlternateState(),
				getEmptyState(), getFullState());
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
		return new ComplexState(getEmptyState(), getAlternateState(),
				getEmptyState(), getFullState());
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
		return new ComplexModulusView(getEmptyState(), getFullState());
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
		String[] lattices = {MargolusLattice.DISPLAY_NAME};

		return lattices;
	}

	/**
	 * The rule for the cellular automaton which will be an complex-valued
	 * function of the neighbors in a Margolus neighborhood. Returns an array of
	 * four values where each value corresponds to a position in the 2 by 2
	 * Margolus neighborhood. The array positions are stored in constants in
	 * this class. For example, array[NORTHWEST], array[NORTHEAST],
	 * array[SOUTHEAST], and array[SOUTHWEST].
	 * 
	 * @param northWestCellValue
	 *            The current value of the northwest cell.
	 * @param northEastCellValue
	 *            The current value of the northeast cell.
	 * @param southEastCellValue
	 *            The current value of the southeast cell.
	 * @param southWestCellValue
	 *            The current value of the southwest cell.
	 * @param northwestCellsFixedRowPosition
	 *            The indexed row position of the northwest cell (0 to
	 *            numRows-1).
	 * @param northwestCellsFixedColumnPosition
	 *            The indexed column position of the northwest cell (0 to
	 *            numColumns-1).
	 * @param generation
	 *            The current generation of the CA.
	 * @return An array of states that corresponds to the 2 by 2 Margolus block.
	 *         Array[0] is the northwest corner of the block, array[1] is the
	 *         northeast corner of the block, array[2] is the southeast corner
	 *         of the block, array[3] is the southwest corner of the block.
	 */
	protected abstract Complex[] blockRule(Complex northWestCellValue,
			Complex northEastCellValue, Complex southEastCellValue,
			Complex southWestCellValue, int northwestCellsFixedRowPosition,
			int northwestCellsFixedColumnPosition, int generation);

	/**
	 * Gets a complex number that represents the alternate state (a state that
	 * is drawn with a right mouse click). If no alternate state is desired,
	 * recommended to return getEmptyState()). Implementations should be careful
	 * to return a new instance each time this is called. Otherwise, multiple CA
	 * cells may be sharing the same instance and a change to one cell could
	 * change many cells.
	 * 
	 * @return The alternate state.
	 */
	public abstract Complex getAlternateState();

	/**
	 * Gets a complex number that represents the empty state. This value is
	 * intended to represent the minimum possible modulus. So if the returned
	 * value is value is 0+i, then the intention is that the maximum possible
	 * modulus allowed in this rule will be mod(0+i) or 1.0. However, every rule
	 * is free to ignore or interpret this value as desired. (For example, in
	 * some cases it may be simpler to interpret the value as the minimum real
	 * and imaginary values respectively -- effectively a square in the complex
	 * plane instead of a circle.) Together with getFullState(), these two
	 * methods defines an annulus or ring of acceptable complex values.
	 * <p>
	 * Implementations should be careful to return a new instance each time this
	 * is called. Otherwise, multiple CA cells may be sharing the same instance
	 * and a change to one cell could change many cells.
	 * 
	 * @return The empty state.
	 */
	public abstract Complex getEmptyState();

	/**
	 * Gets a complex number that represents the full or filled state. This
	 * value is intended to represent the maximum possible modulus. So if the
	 * returned value is value is 1+i, then the intention is that the maximum
	 * possible modulus allowed in this rule will be mod(1+i) or sqrt(2).
	 * However, every rule is free to ignore or interpret this value as desired.
	 * (For example, in some cases it may be simpler to interpret the value as
	 * the maximum real and imaginary values respectively -- effectively a
	 * square in the complex plane instead of a circle.) Together with
	 * getEmptyState(), these two methods defines an annulus or ring of
	 * acceptable complex values.
	 * <p>
	 * Implementations should be careful to return a new instance each time this
	 * is called. Otherwise, multiple CA cells may be sharing the same instance
	 * and a change to one cell could change many cells.
	 * 
	 * @return The full state.
	 */
	public abstract Complex getFullState();
}
