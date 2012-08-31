/*
 IntegerVectorRuleTemplate -- a class within the Cellular Automaton Explorer. 
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

import javax.swing.JOptionPane;

import cellularAutomata.Cell;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.IntegerVectorState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.IntegerVectorDefaultView;
import cellularAutomata.rules.Rule;

/**
 * A convenience class/template for any rules that need cell states based on an
 * integer array of values. (In other words, this class uses the cell state
 * cellularAutomaton.cellState.model.IntegerVectorState.)
 * <p>
 * This class uses the Template Method design pattern. Subclasses implement the
 * abstract methods integerVectorRule(), getMaximumPermissableValue(), and
 * getMaximumPermissableValue() which are called by the template method
 * calculateNewState().
 * 
 * @author David Bahr
 */
public abstract class IntegerVectorRuleTemplate extends Rule
{
	/**
	 * Create a rule based on an integer array of values.
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
	public IntegerVectorRuleTemplate(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		// check for error
		if(getMaximumPermissibleValue() < getMinimumPermissibleValue())
		{
			// Failed because the max and min values are wrong
			String warning = "For any child class of IntegerVectorRuleTemplate, \n"
					+ "the maximum permissible value must be less than the \n"
					+ "minimum permissible value.";
			JOptionPane.showMessageDialog(null, warning, "Developer Warning",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Creates a new IntegerVectorState based on an array provided by the method
	 * integerVectorRule(). <br>
	 * By convention the neighbors should be indexed clockwise starting to the
	 * northwest of the cell.
	 * 
	 * @see cellularAutomata.rules.Rule#calculateNewState(cellularAutomata.Cell,
	 *      cellularAutomata.Cell[])
	 */
	public CellState calculateNewState(Cell cell, Cell[] neighbors)
	{
		// the current generation
		int generation = cell.getGeneration();

		// get the state array for the cell
		int[] cellArray = (int[]) cell.getState(generation).getValue();

		// get the state array for each neighbor
		int[][] neighborArrays = new int[neighbors.length][cellArray.length];
		for(int i = 0; i < neighbors.length; i++)
		{
			neighborArrays[i] = (int[]) neighbors[i].getState(generation)
					.getValue();
		}

		// apply the rule that gives a vector (array)
		int[] vector = integerVectorRule(cellArray, neighborArrays, generation);

		// create a new state from the array
		IntegerVectorState state = new IntegerVectorState(vector,
				getMinimumPermissibleValue(), getMaximumPermissibleValue());

		return state;
	}

	/**
	 * @see cellularAutomata.rules.Rule#getCompatibleCellState()
	 */
	public final CellState getCompatibleCellState()
	{
		// create a suitable array for the CellState
		int[] state = new int[getVectorLength()];
		for(int i = 0; i < getVectorLength(); i++)
		{
			state[i] = this.getMinimumPermissibleValue();
		}

		return new IntegerVectorState(state, getMinimumPermissibleValue(),
				getMaximumPermissibleValue());
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
		return new IntegerVectorDefaultView();
	}

	/**
	 * A list of lattices with which this Rule will work; in this case, returns
	 * all lattices by default, though child classes may wish to override this
	 * and restrict the lattices with which the child rule will work. <br>
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
	 * Gets the maximum permissible value for each element of the vector. Do not
	 * recommend using Integer.MAX_VALUE unless your code very carefully checks
	 * for instances of Infinity. <br>
	 * This method is used by the AbstractIntegerVectorRule class to properly
	 * construct an IntegerVectorState.
	 * 
	 * @return The maximum permissible value.
	 */
	public abstract int getMaximumPermissibleValue();

	/**
	 * Gets the minimum permissible value for each element of the vector. Do not
	 * recommend using Integer.MIN_VALUE unless your code very carefully checks
	 * for instances of Infinity. <br>
	 * This method is used by the AbstractIntegerVectorRule class to properly
	 * construct an IntegerVectorState.
	 * 
	 * @return The minimum permissible value.
	 */
	public abstract int getMinimumPermissibleValue();

	/**
	 * Gets the length of the vectors (arrays) that will be used by the Rule.
	 * The length must be the same for all cells.
	 * 
	 * @return The length of the vector stored by each cell.
	 */
	public abstract int getVectorLength();

	/**
	 * The rule for the cellular automaton which will be a function of the cell
	 * and its neighbors. The cell and its neighbors are integer arrays. <br>
	 * By convention the neighbors' geometry should be indexed clockwise
	 * starting to the northwest of the cell.
	 * 
	 * @param cellArray
	 *            The current value of the cell being updated. The value is an
	 *            integer array.
	 * @param neighborArrays
	 *            The current values of each neighbor. The values are integer
	 *            arrays. In other words, the first index is the number of the
	 *            neighbor, and the second index is the array position for the
	 *            that neighbor's array. By convention the neighbors' geometry
	 *            should be indexed clockwise starting to the northwest of the
	 *            cell.
	 * @param generation
	 *            The current generation of the CA.
	 * @return A new state for the cell. Each element of the array must be
	 *         between the values provided by getMinimumPermissibleValue() and
	 *         getMaximumPermissibleValue().
	 */
	public abstract int[] integerVectorRule(int[] cellArray,
			int[][] neighborArrays, int generation);
}
