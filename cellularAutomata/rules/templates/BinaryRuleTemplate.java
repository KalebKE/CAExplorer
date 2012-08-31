/*
 BinaryRuleTemplate -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.BinaryCellStateView;
import cellularAutomata.error.exceptions.BinaryStateOutOfBoundsException;
import cellularAutomata.util.MinMaxIntPair;

/**
 * A template/convenience class for all rules that behave as boolean function of
 * their neighbors. This class handles conversion of the neighbors as Cells to
 * neighbors as integer values (0 or 1) so that the subclass only has to worry
 * about specifying the boolean function. This conversion makes sure that the
 * cells all return their state values for the same generation as the current
 * cell. <br>
 * This class is similar to IntegerRuleTemplate which can have N states
 * including N = 2. However, this class checks to be sure that only binary
 * states are allowed, sends warnings if the values are not 0 or 1, and defaults
 * to cell states and graphics most appropriate to binary numbers.
 * <p>
 * This class uses the Template Method design pattern. Subclasses implement the
 * abstract binaryRule() method which is called by the template method
 * integerRule().
 * 
 * @author David Bahr
 */
public abstract class BinaryRuleTemplate extends IntegerRuleTemplate
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
	public BinaryRuleTemplate(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);
	}

	/**
	 * Calculates a new state for the cell by finding its current generation and
	 * then requesting the state values for its neighbors for the same
	 * generation. The abstract boolean function is then called to calculate a
	 * new state. By convention the neighbors should be indexed clockwise
	 * starting to the northwest of the cell.
	 * 
	 * @param cell
	 *            The cell being updated.
	 * @param neighbors
	 *            The cells on which the update is based (usually neighboring
	 *            cells). By convention the neighbors should be indexed
	 *            clockwise starting to the northwest of the cell. May be null
	 *            if want this method to find the "neighboring" cells.
	 * @param numStates
	 *            The number of states. In other words, the returned state can
	 *            only have values between 0 and numStates - 1.
	 * @param generation
	 *            The current generation of the CA.
	 * @return A new state for the cell.
	 */
	protected int integerRule(int cellValue, int[] neighbors, int numStates,
			int generation)
	{
		// call the binary rule
		int answer = binaryRule(cellValue, neighbors, generation);

		if((answer != 0) && (answer != 1))
		{
			throw new BinaryStateOutOfBoundsException("", answer);
		}

		return answer;
	}

	/**
	 * The rule for the cellular automaton which will be a boolean function of
	 * the neighbors.
	 * 
	 * @param cellValue
	 *            The current value of the cell being updated.
	 * @param neighbors
	 *            The neighbors as their integer values (0 or 1).
	 * @param generation
	 *            The current generation of the CA.
	 * @return A new state for the cell.
	 */
	protected abstract int binaryRule(int cellValue, int[] neighbors,
			int generation);

	/**
	 * Returns null to disable the "Number of States" text field.
	 */
	protected MinMaxIntPair getMinMaxAllowedStates(String latticeDescription)
	{
		return null;
	}

	/**
	 * The value that will be displayed for the state. Should always be a 2.
	 */
	protected Integer stateValueToDisplay(String latticeDescription)
	{
		return new Integer(2);
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
		return new BinaryCellStateView();
	}
}