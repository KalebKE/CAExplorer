/*
 ComplexVectorRuleTemplate -- a class within the Cellular Automaton Explorer. 
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
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.ComplexValuedVectorState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.ComplexVectorDefaultView;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.math.Complex;

/**
 * A convenience class/template for any rules that need cell states based on an
 * array of complex values. (In other words, this class uses the cell state
 * cellularAutomaton.cellState.model.ComplexValuedVectorState.)
 * <p>
 * This class uses the Template Method design pattern. Subclasses implement the
 * abstract methods complexVectorRule(), getAlternateState(), getEmptyState(),
 * and getFullState() which are called by the template method
 * calculateNewState().
 * 
 * @author David Bahr
 */
public abstract class ComplexVectorRuleTemplate extends Rule
{
	// The maximum real and imaginary values that will be returned by a Complex
	// cell state that is created randomly.
	private double maxRandom = 1.0;

	// The minimum real and imaginary values that will be returned by a Complex
	// cell state that is created randomly.
	private double minRandom = -1.0;

	/**
	 * Create a rule based on an array of Complex values.
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
	public ComplexVectorRuleTemplate(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);
	}

	/**
	 * Creates a new ComplexValuedVectorState based on an array provided by the
	 * method complexVectorRule(). <br>
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
		Complex[] cellArray = (Complex[]) cell.getState(generation).getValue();

		// get the state array for each neighbor
		Complex[][] neighborArrays = new Complex[neighbors.length][cellArray.length];
		for(int i = 0; i < neighbors.length; i++)
		{
			neighborArrays[i] = (Complex[]) neighbors[i].getState(generation)
					.getValue();
		}

		// apply the rule that gives a vector (array)
		Complex[] vector = complexVectorRule(cellArray, neighborArrays,
				generation);

		// create a new state from the array
		ComplexValuedVectorState state = new ComplexValuedVectorState(vector,
				getAlternateState(), getEmptyState(), getFullState(),
				minRandom, maxRandom);

		return state;
	}

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
	public abstract Complex[] getAlternateState();

	/**
	 * @see cellularAutomata.rules.Rule#getCompatibleCellState()
	 */
	public final CellState getCompatibleCellState()
	{
		return new ComplexValuedVectorState(getFullState(),
				getAlternateState(), getEmptyState(), getFullState(),
				minRandom, maxRandom);
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
		return new ComplexVectorDefaultView(0.0, 1.0);
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
	 * Gets a complex array that represents the empty state. Implementations
	 * should be careful to return a new instance each time this is called.
	 * Otherwise, multiple CA cells may be sharing the same instance and a
	 * change to one cell could change many cells.
	 * <p>
	 * See ComplexRuleTemplate for an interpretation of the values returned by
	 * this method. In effect, each element of the array describes the inner
	 * radius of an annulus of possible values. However, child classes are free
	 * to interpret the returned value in any manner desired.
	 * 
	 * @return The empty state.
	 */
	public abstract Complex[] getEmptyState();

	/**
	 * Gets a complex array that represents the full or filled state.
	 * Implementations should be careful to return a new instance each time this
	 * is called. Otherwise, multiple CA cells may be sharing the same instance
	 * and a change to one cell could change many cells.
	 * <p>
	 * See ComplexRuleTemplate for an interpretation of the values returned by
	 * this method. In effect, each element of the array describes the outer
	 * radius of an annulus of possible values. However, child classes are free
	 * to interpret the returned value in any manner desired.
	 * 
	 * @return The full state.
	 */
	public abstract Complex[] getFullState();

	/**
	 * Gets the length of the vectors (arrays) that will be used by the Rule.
	 * The length must be the same for all cells.
	 * 
	 * @return The length of the vector stored by each cell.
	 */
	public abstract int getVectorLength();

	/**
	 * The rule for the cellular automaton which will be a function of the cell
	 * and its neighbors. The cell and its neighbors are arrays of Complex
	 * values. <br>
	 * By convention the neighbors' geometry should be indexed clockwise
	 * starting to the northwest of the cell.
	 * 
	 * @param cellArray
	 *            The current value of the cell being updated. The value is an
	 *            array of Complex numbers.
	 * @param neighborArrays
	 *            The current values of each neighbor. The values are arrays of
	 *            Complex numbers. In other words, the first index is the number
	 *            of the neighbor, and the second index is the array position
	 *            for the that neighbor's array. By convention the neighbors'
	 *            geometry should be indexed clockwise starting to the northwest
	 *            of the cell.
	 * @param generation
	 *            The current generation of the CA.
	 * @return A new state for the cell.
	 */
	protected abstract Complex[] complexVectorRule(Complex[] cellArray,
			Complex[][] neighborArrays, int generation);
}
