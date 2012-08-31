/*
 ObjectRuleTemplate -- a class within the Cellular Automaton Explorer. 
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
import cellularAutomata.cellState.model.ObjectState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.DefaultCellStateView;
import cellularAutomata.rules.Rule;

/**
 * A convenience class/template for any rules that need cell states based on an
 * Object. (In other words, this class uses the cell state
 * cellularAutomaton.cellState.model.ObjectState.)
 * <p>
 * This class uses the Template Method design pattern. Subclasses implement the
 * abstract methods objectRule(), getAlternateState(), getEmptyState(), and
 * getFullState() which are called by the template method calculateNewState().
 * 
 * @author David Bahr
 */
public abstract class ObjectRuleTemplate extends Rule
{
	/**
	 * Create a rule based on cells that use an Object for a state value.
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
	public ObjectRuleTemplate(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		// check for error
		if(getAlternateState() == null)
		{
			// Failed because the state can't be null
			String warning = "For any child class of ObjectRuleTemplate, \n"
					+ "the alternate state cannot be null.";
			JOptionPane.showMessageDialog(null, warning, "Developer Warning",
					JOptionPane.WARNING_MESSAGE);
		}
		else if(getEmptyState() == null)
		{
			// Failed because the state can't be null
			String warning = "For any child class of ObjectRuleTemplate, \n"
					+ "the empty state cannot be null.";
			JOptionPane.showMessageDialog(null, warning, "Developer Warning",
					JOptionPane.WARNING_MESSAGE);
		}
		else if(getFullState() == null)
		{
			// Failed because the state can't be null
			String warning = "For any child class of ObjectRuleTemplate, \n"
					+ "the full state cannot be null.";
			JOptionPane.showMessageDialog(null, warning, "Developer Warning",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Creates a new ObjectState based on an Object provided by the method
	 * objectRule(). The method objectRule() is where the actual CA rule is
	 * written. <br>
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
		Object cellObject = (Object) cell.getState(generation).getValue();

		// get the state for each neighbor
		Object[] neighborObjects = new Object[neighbors.length];
		for(int i = 0; i < neighbors.length; i++)
		{
			neighborObjects[i] = (Object) neighbors[i].getState(generation)
					.getValue();
		}

		// apply the rule that gives a cell state as an Object.
		Object cellObjectState = objectRule(cellObject, neighborObjects,
				generation);

		// create a new state from the value returned by objectRule
		ObjectState state = new ObjectState(cellObjectState,
				getAlternateState(), getEmptyState(), getFullState());

		return state;
	}

	/**
	 * Given a string, this method should construct an object of the same class
	 * and type as returned by getFullState(). (That same type is also returned
	 * by getEmptyState() and getAlternateState()).
	 * <p>
	 * This method is necessary to properly reload data when a simulation is
	 * saved as a file.
	 * <p>
	 * If you are using this ObjectRuleTemplate class, then the cell's state is
	 * an Object of some kind. When reading and writing data from a file, the
	 * Object is stored as a string (it is stored using the toString() method).
	 * This method is called when reading the data back from the file.
	 * Therefore, this method should be able to reconstruct the Object from that
	 * string.
	 * <p>
	 * This method may return null. In that case, the cell's state can be saved
	 * in a file but it cannot be reread from that file. (If null, attempting to
	 * reload a simulation will warn the user that this is not possible.) Some
	 * arbitrary objects cannot be reconstructed from a string, so returning
	 * null may be necessary.
	 * 
	 * @param state
	 *            The Object's state represented as a string.
	 * @return An Object that will be used as a cell state. May be null.
	 */
	public abstract Object createStateFromString(String state);

	/**
	 * Gets an Object that represents the alternate state (a state that is drawn
	 * with a right mouse click). If no alternate state is desired, recommended
	 * to return getFullState()). Implementations should be careful to return a
	 * new instance each time this is called. Otherwise, multiple CA cells may
	 * be sharing the same instance and a change to one cell could change many
	 * cells.
	 * 
	 * @return The alternate state.
	 */
	public abstract Object getAlternateState();

	/**
	 * @see cellularAutomata.rules.Rule#getCompatibleCellState()
	 */
	public final CellState getCompatibleCellState()
	{
		return new ObjectState(getEmptyState(), getAlternateState(),
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
		return new DefaultCellStateView();
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
	 * Gets an Object that represents the empty state. Implementations should be
	 * careful to return a new instance each time this is called. Otherwise,
	 * multiple CA cells may be sharing the same instance and a change to one
	 * cell could change many cells.
	 * 
	 * @return The empty state.
	 */
	public abstract Object getEmptyState();

	/**
	 * Gets an Object that represents the full or filled state. Implementations
	 * should be careful to return a new instance each time this is called.
	 * Otherwise, multiple CA cells may be sharing the same instance and a
	 * change to one cell could change many cells.
	 * 
	 * @return The full state.
	 */
	public abstract Object getFullState();

	/**
	 * The rule for the cellular automaton which will be a function of the cell
	 * and its neighbors. The cell and its neighbors are Objects. <br>
	 * By convention the neighbors' geometry should be indexed clockwise
	 * starting to the northwest of the cell.
	 * 
	 * @param cellObject
	 *            The current value of the cell being updated.
	 * @param neighborObjects
	 *            The current values of each neighbor. In other words, the array
	 *            index is the number of the neighbor. By convention the
	 *            neighbors' geometry should be indexed clockwise starting to
	 *            the northwest of the cell.
	 * @param generation
	 *            The current generation of the CA.
	 * @return A new state for the cell which may be an arbitrary Object.
	 */
	protected abstract Object objectRule(Object cellObject,
			Object[] neighborObjects, int generation);
}
