/*
 IntegerCellStateView -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.cellState.view;

import java.awt.Color;
import java.awt.Shape; // import java.util.Hashtable;

import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.Coordinate;

// import cellularAutomata.graphics.colors.ColorScheme;
// import cellularAutomata.graphics.colors.ColorTools;

/**
 * Displays integer-based cells as colors.
 * 
 * @author David Bahr
 */
public class IntegerCellStateView extends CellStateView
{
	/**
	 * Determines if the currently active rule is compatible with this class,
	 * IntegerCellStateView.
	 * 
	 * @return true if the currently active rule is compatible with this class,
	 *         IntegerCellStateView.
	 */
	public static boolean isCurrentRuleCompatible()
	{
		boolean isCompatible = false;

		String ruleClassName = CurrentProperties.getInstance()
				.getRuleClassName();
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(ruleClassName);

		if(rule != null)
		{
			// find out what cell state works with this rule
			CellStateView cellStateView = rule.getCompatibleCellStateView();

			// if IntegerCellState works with this rule, then is compatible
			// i.e., next line checks to see if the cellState could be cast to a
			// IntegerCellState without throwing an exception
			try
			{
				// try casting
				IntegerCellStateView view = (IntegerCellStateView) cellStateView;

				// it worked, so compatible
				isCompatible = true;
			}
			catch(Exception e)
			{
				// not compatible
			}
		}

		return isCompatible;
	}

	/**
	 * Should create a Shape appropriate for displaying the average of the
	 * states given in the array. For example, each state might be a vector, and
	 * the average shape would be an arrow indicating the average of the
	 * vectors. For most cellular automaton, the shapes are identical for all
	 * states (for example, a square for the "game of life"), so the average is
	 * just that shape. If the default shape is appropriate, this method may
	 * return null.
	 * 
	 * @param width
	 *            The width of the rectangle that bounds this Shape. May be
	 *            ignored.
	 * @param height
	 *            The height of the rectangle that bounds this Shape. May be
	 *            ignored.
	 * @param rowAndCol
	 *            The row and col of the shape being displayed. May be ignored.
	 * @return The shape to be displayed. May be null (in which case the CA
	 *         graphics should use a default shape).
	 * @see cellularAutomata.cellState.view.CellStateView#getAverageDisplayShape(
	 *      cellularAutomata.cellState.model.CellState[], int, int, Coordinate)
	 */
	public Shape getAverageDisplayShape(CellState[] states, int width,
			int height, Coordinate rowAndCol)
	{
		return null;
	}

	/**
	 * Returns a color appropriate to the current color scheme.
	 * 
	 * @param state
	 *            The cell state whose color we are finding.
	 * @param numStates
	 *            The number of possible states, which may not be the same as
	 *            the currently active number of states -- may be null which
	 *            indicates that the currently active number of states will be
	 *            used. (See for example, createProbabilityChoosers() method in
	 *            InitialStatesPanel class.)
	 * @param rowAndCol
	 *            The row and col of the cell being displayed. May be ignored.
	 * @see cellularAutomata.cellState.view.CellStateView#getColor(
	 *      cellularAutomata.cellState.model.CellState, Integer, Coordinate)
	 */
	public Color getColor(CellState state, Integer numStates,
			Coordinate rowAndCol)
	{
		// use the color scheme to get the appropriate color for the state value
		IntegerCellState nState = (IntegerCellState) state;
		int stateValue = nState.getState();

		if(numStates == null)
		{
			return colorScheme.getColor(stateValue, CurrentProperties
					.getInstance().getNumStates(), this);
		}
		else
		{
			return colorScheme.getColor(stateValue, numStates.intValue(), this);
		}
	}

	/**
	 * Returns a color appropriate to the current color scheme.
	 * 
	 * @param state
	 *            The cell state whose color we are finding.
	 * @param numStates
	 *            The number of possible states, which may not be the same as
	 *            the currently active number of states. (See for example,
	 *            createProbabilityChoosers() method in InitialStatesPanel
	 *            class.)
	 * @param rowAndCol
	 *            The row and col of the cell being displayed. May be ignored.
	 */
	public Color getColor(CellState state, int numStates, Coordinate rowAndCol)
	{
		IntegerCellState nState = (IntegerCellState) state;
		int stateValue = nState.getState();

		// use the color scheme to get the appropriate color
		return colorScheme.getColor(stateValue, numStates, this);
	}

	/**
	 * Creates a color in an intermediate shade between the colors associated
	 * with the maximum and minimum values of the cell state.
	 * 
	 * @see cellularAutomata.cellState.view.CellStateView#getColor(
	 *      cellularAutomata.cellState.model.CellState, Object[])
	 */
	// public Color getColor(CellState state, Object[] parameters)
	// {
	// IntegerCellState nState = (IntegerCellState) state;
	// int numStates = nState.getNumStates();
	// int stateValue = nState.getState();
	//
	// return ColorTools.getColorFromSingleValue(stateValue, numStates);
	// }
	/**
	 * @see cellularAutomata.cellState.view.CellStateView#getDisplayShape(
	 *      cellularAutomata.cellState.model.CellState, int, int, Coordinate)
	 */
	public Shape getDisplayShape(CellState state, int width, int height,
			Coordinate rowAndCol)
	{
		return null;
	}
}
