/*
 FiniteDifferenceTemplate -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.CAController;
import cellularAutomata.Cell;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.RealValuedState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.RealValuedDefaultView;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.Coordinate;

/**
 * A convenience class/template for any rules that need cell states based on a
 * double value. Very similar to the RealRuleTemplate, but includes cell
 * coordinates and other features useful to finite difference models. (In other
 * words, this class uses the cell state
 * cellularAutomaton.cellState.model.RealValuedState.)
 * <p>
 * NOTE: The child classes's default constructor should call the super
 * constructor RealRuleTemplate(double emptyValue, double fullValue, Properties
 * properties) to ensure that the min and max get set properly.
 * <p>
 * This class uses the Template Method design pattern. Subclasses implement the
 * abstract method doubleRule() which is called by the template method
 * calculateNewState().
 * 
 * @author David Bahr
 */
public abstract class FiniteDifferenceTemplate extends Rule
{
	// the empty state (typically the smallest value)
	private double emptyState = RealValuedState.DEFAULT_EMPTY_STATE;

	// The full state (typically the largest value)
	private double fullState = RealValuedState.DEFAULT_FULL_STATE;

	/**
	 * Create a rule based on a double value. DEPRECATED because allows the rule
	 * to instantiate with the child a different empty and full state.
	 * 
	 * @param properties
	 */
	// public RealRuleTemplate(Properties properties)
	// {
	// this(RealValuedState.DEFAULT_EMPTY_STATE,
	// RealValuedState.DEFAULT_FULL_STATE, properties);
	// }
	/**
	 * Create a rule based on a double value, and also specify the empty and
	 * full states (usually the min and max).
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
	 * @param emptyValue
	 *            The empty value that will be used for the cell states.
	 * @param fullValue
	 *            The full value that will be used for the cell states.
	 * @param minimalOrLazyInitialization
	 *            When true, the constructor instantiates an object with as
	 *            small a footprint as possible. When false, the rule is fully
	 *            constructed. If uncertain, set this variable to false.
	 */
	public FiniteDifferenceTemplate(double emptyValue, double fullValue,
			boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		// this must happen even with minimal instantiation so that the view
		// reflects whatever empty and full values are passed into this
		// constructor. When changing color schemes, the rule is instantiated
		// minimally, but the view is used to help set colors.
		this.emptyState = emptyValue;
		this.fullState = fullValue;
	}

	/**
	 * Creates a new RealValuedState based on an value provided by the method
	 * doubleRule().
	 * <p>
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

		// get the state for the cell
		double cellValue = ((Double) cell.getState(generation).getValue())
				.doubleValue();

		// get the state array for each neighbor
		double[] neighborArray = new double[neighbors.length];
		for(int i = 0; i < neighbors.length; i++)
		{
			neighborArray[i] = ((Double) neighbors[i].getState(generation)
					.getValue()).doubleValue();
		}

		// apply the rule that gives a real value
		double doubleValue = doubleRule(cellValue, neighborArray, cell
				.getCoordinate(), generation);

		// create a new state from the doubleValue
		RealValuedState state = new RealValuedState(doubleValue, emptyState,
				fullState);

		return state;
	}

	/**
	 * Gets all of the cells that make up the simulation's lattice. No order is
	 * guaranteed but is generally obvious. One-dimensional lattices are
	 * generally returned left to right starting with array element 0.
	 * Generally, for two-dimensional square lattices, the element
	 * array[row][col] is accessed at array[row * width + col].
	 * 
	 * @return The array of cells that make up the simulation's lattice.
	 */
	public Cell[] getCells()
	{
		return CAController.getCAFrame().getLattice().getCells();
	}

	/**
	 * @see cellularAutomata.rules.Rule#getCompatibleCellState()
	 */
	public final CellState getCompatibleCellState()
	{
		return new RealValuedState(emptyState, emptyState, fullState);
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
		return new RealValuedDefaultView(emptyState, fullState);
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
	 * Gets the full lattice for the simulation.
	 * 
	 * @return The lattice containing all of the cells.
	 */
	public Lattice getLattice()
	{
		return CAController.getCAFrame().getLattice();
	}

	/**
	 * The rule for the cellular automaton which will be a function of the cell
	 * and its neighbors. The cell and its neighbors are double values. <br>
	 * By convention the neighbors' geometry should be indexed clockwise
	 * starting to the northwest of the cell.
	 * 
	 * @param cellArray
	 *            The current value of the cell being updated. The value is a
	 *            double.
	 * @param neighborArrays
	 *            The current values of each neighbor. The values are doubles.
	 *            By convention the neighbors' geometry should be indexed
	 *            clockwise starting to the northwest of the cell.
	 * @param cellCoordinate
	 *            The row and column coordinate of the cell. Useful for finite
	 *            difference style simulations, and otherwise ignored for
	 *            standard cellular automata.
	 * @param generation
	 *            The current generation of the CA.
	 * @return A new state for the cell. The state value must be between the
	 *         values provided by getMinimumPermissibleValue() and
	 *         getMaximumPermissibleValue().
	 */
	protected abstract double doubleRule(double cellValue, double[] neighbors,
			Coordinate cellCoordinate, int generation);
}
