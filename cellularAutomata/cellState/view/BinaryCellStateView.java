/*
 BinaryCellStateView -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.util.Coordinate;

/**
 * Provides graphics for cell states that store binary numbers (0 or 1).
 * 
 * @author David Bahr
 */
public class BinaryCellStateView extends CellStateView
{

	/**
	 * @see cellularAutomata.cellState.view.CellStateView#getAverageDisplayShape(
	 *      cellularAutomata.cellState.model.CellState[], int, int, Coordinate)
	 */
	public Shape getAverageDisplayShape(CellState[] states, int width,
			int height, Coordinate rowAndCol)
	{
		return null;
	}

	/**
	 * Assumes that there are only two states, 0 and 1. 0 is given the default
	 * empty color and 1 (or any other value) is given the default filled color.
	 * 
	 * @param state
	 *            The cell state that will be displayed.
	 * @param numStates
	 *            Irrelevant because the number of states is always two for
	 *            binary cells; may be null.
	 * @param rowAndCol
	 *            The row and col of the cell being displayed. May be ignored.
	 * @see cellularAutomata.cellState.view.CellStateView#getColor
	 */
	public Color getColor(CellState state, Integer numStates,
			Coordinate rowAndCol)
	{
		IntegerCellState binaryState = (IntegerCellState) state;

		Color color = null;
		if(binaryState.getState() == 0)
		{
			color = ColorScheme.EMPTY_COLOR;
		}
		else
		{
			color = ColorScheme.FILLED_COLOR;
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
