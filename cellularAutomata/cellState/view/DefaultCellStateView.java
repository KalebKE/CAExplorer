/*
 DefaultCellStateView -- a class within the Cellular Automaton Explorer. 
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
import java.awt.Shape;

import cellularAutomata.cellState.model.CellState;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.util.Coordinate;

/**
 * Provides default graphics for all cell states. This class will work with any
 * and all CellState classes.
 * 
 * @author David Bahr
 */
public class DefaultCellStateView extends CellStateView
{

	/**
	 * @see cellularAutomata.cellState.view.CellStateView#getAverageDisplayShape(cellularAutomata.cellState.model.CellState[],
	 *      int, int, Coordinate)
	 */
	public Shape getAverageDisplayShape(CellState[] states, int width,
			int height, Coordinate rowAndCol)
	{
		return null;
	}

	/**
	 * Get a color appropriate for displaying the given state.
	 * 
	 * @param state
	 *            The cell state that will be displayed.
	 * @param numStates
	 *            If relevant, the number of possible states (which may not be
	 *            the same as the currently active number of states) -- may be
	 *            null which indicates that the number of states is inapplicable
	 *            or that the currently active number of states should be used.
	 *            (See for example, createProbabilityChoosers() method in
	 *            InitialStatesPanel class.)
	 * @param rowAndCol
	 *            The row and col of the cell being displayed. May be ignored.
	 * @return The color to be displayed.
	 */
	public Color getColor(CellState state, Integer numStates,
			Coordinate rowAndCol)
	{
		Color color = null;
		if(state.isFull())
		{
			color = ColorScheme.FILLED_COLOR;
		}
		else
		{
			color = ColorScheme.EMPTY_COLOR;
		}

		return color;
	}

	/**
	 * @see cellularAutomata.cellState.view.CellStateView#getDisplayShape(CellState,
	 *      int, int, Coordinate)
	 */
	public Shape getDisplayShape(CellState state, int width, int height,
			Coordinate rowAndCol)
	{
		return null;
	}
}
