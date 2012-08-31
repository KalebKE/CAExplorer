/*
 HexagonalIntegerCellStateView -- a class within the Cellular Automaton Explorer. 
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

import java.awt.Shape;

import cellularAutomata.cellState.model.CellState;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.Hexagon;

/**
 * A view that displays hexagons for the cell state.
 * 
 * @author David Bahr
 */
public class HexagonalIntegerCellStateView extends IntegerCellStateView
{
	// the shape displayed
	private Hexagon hexagon = null;

	// the original width of the triangle (before any rescaling of the panel
	// size)
	private int oldWidth = 0;

	/**
	 * Creates a hexagon and stores as an instance variable.
	 * 
	 * @param width
	 *            The width of the rectangle that bounds this Shape.
	 * @param height
	 *            The height of the rectangle that bounds this Shape.
	 */
	private void createHexagon(int width, int height)
	{
		// 30 degrees in radians
		double radians = 30.0 * (Math.PI / 180.0);

		// length of the side of a hexagon
		double sideLength = width / (2.0 * Math.cos(radians));

		// hexagon = new Hexagon(0, 0, (int) Math.ceil(sideLength));
		hexagon = new Hexagon(0, 0, (int) Math.round(sideLength));
	}

	/**
	 * Creates a hexagonal Shape appropriate for displaying as the average of
	 * the states given in the array. Overrides parent class.
	 * 
	 * @param states
	 *            The states that are being averaged, and for which an averaged
	 *            display shape is needed.
	 * @param width
	 *            The width of the rectangle that bounds this Shape. May be
	 *            ignored.
	 * @param height
	 *            The height of the rectangle that bounds this Shape. May be
	 *            ignored.
	 * @param rowAndCol
	 *            The row and col of the shape being displayed. May be ignored.
	 * @return The hexagonal shape to be displayed.
	 * @see cellularAutomata.cellState.view.CellStateView#getAverageDisplayShape(
	 *      cellularAutomata.cellState.model.CellState[], int, int, Coordinate)
	 */
	public Shape getAverageDisplayShape(CellState[] states, int width,
			int height, Coordinate rowAndCol)
	{
		// check to see if the width changed (happens when panel is resized)
		boolean widthChanged = false;
		if(oldWidth != width)
		{
			widthChanged = true;

			// set to the new value
			oldWidth = width;
		}

		if(widthChanged || hexagon == null)
		{
			createHexagon(width, height);
		}

		return hexagon;
	}

	/**
	 * Creates a hexagon to display. Overrides parent class.
	 * 
	 * @param state
	 *            The cell state that will be displayed.
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
	 * @see cellularAutomata.cellState.view.CellStateView#getDisplayShape(
	 *      cellularAutomata.cellState.model.CellState, int, int, Coordinate)
	 */
	public Shape getDisplayShape(CellState state, int width, int height,
			Coordinate rowAndCol)
	{
		// check to see if the width changed (happens when panel is resized)
		boolean widthChanged = false;
		if(oldWidth != width)
		{
			widthChanged = true;

			// set to the new value
			oldWidth = width;
		}

		if(widthChanged || hexagon == null)
		{
			createHexagon(width, height);
		}

		return hexagon;
	}

}
