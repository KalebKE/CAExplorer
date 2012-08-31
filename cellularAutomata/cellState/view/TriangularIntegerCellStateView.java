/*
 TriangularIntegerCellStateView -- a class within the Cellular Automaton Explorer. 
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
import cellularAutomata.lattice.view.TriangularLatticeView;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.EquilateralTriangle;

/**
 * A view that displays hexagons for the cell state.
 * 
 * @author David Bahr
 */
public class TriangularIntegerCellStateView extends IntegerCellStateView
{
	// the shape displayed when the triangle should point to the right
	private EquilateralTriangle triangle90 = null;

	// the shape displayed when the triangle should point to the left
	private EquilateralTriangle triangleNeg90 = null;

	// the original height of the triangle (before any rescaling of the panel
	// size)
	private int oldHeight = 0;

	/**
	 * Creates an equilateral triangle.
	 * 
	 * @param width
	 *            The width of the rectangle that bounds this Shape.
	 * @param height
	 *            The height of the rectangle that bounds this Shape.
	 * @param angle
	 *            The angle at which the apex of the equilateral triangle
	 *            points.
	 * @return The triangle pointing in the correct direction and of the correct
	 *         length.
	 */
	private EquilateralTriangle createTriangle(int width, int height,
			double angle)
	{
		return new EquilateralTriangle(0, 0, height, angle);
	}

	/**
	 * Creates a triangle Shape appropriate for displaying as the average of the
	 * states given in the array. Overrides parent class.
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
	 * @return The triangle shape to be displayed.
	 * @see cellularAutomata.cellState.view.CellStateView#getAverageDisplayShape(
	 *      cellularAutomata.cellState.model.CellState[], int, int, Coordinate)
	 */
	public Shape getAverageDisplayShape(CellState[] states, int width,
			int height, Coordinate rowAndCol)
	{
		// check to see if the height changed (happens when panel is resized)
		boolean heightChanged = false;
		if(oldHeight != height)
		{
			heightChanged = true;

			// set to the new value
			oldHeight = height;
		}

		if(heightChanged || triangle90 == null)
		{
			triangle90 = createTriangle(width, height, 90.0);
		}
		if(heightChanged || triangleNeg90 == null)
		{
			triangleNeg90 = createTriangle(width, height, -90.0);
		}

		// The row and the col determine the orientation of the triangle.
		// See TriangularLattice class for more details.
		// the orientation of the tip of the equilateral triangle
		int row = rowAndCol.getRow();
		int col = rowAndCol.getColumn();
		double angle = TriangularLatticeView.getTriangleAngle(row, col);

		// determine which triangle to use
		EquilateralTriangle triangle = triangle90;
		if(angle == -90.0)
		{
			triangle = triangleNeg90;
		}

		return triangle;
	}

	/**
	 * Creates a triangle to display. Overrides parent class.
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
		// check to see if the height changed (happens when panel is resized)
		boolean heightChanged = false;
		if(oldHeight != height)
		{
			heightChanged = true;

			// set to the new value
			oldHeight = height;
		}

		if(heightChanged || triangle90 == null)
		{
			triangle90 = createTriangle(width, height, 90.0);
		}
		if(heightChanged || triangleNeg90 == null)
		{
			triangleNeg90 = createTriangle(width, height, -90.0);
		}

		// The row and the col determine the orientation of the triangle.
		// See TriangularLattice class for more details.
		// the orientation of the tip of the equilateral triangle
		int row = rowAndCol.getRow();
		int col = rowAndCol.getColumn();
		double angle = TriangularLatticeView.getTriangleAngle(row, col);

		// determine which triangle to use
		EquilateralTriangle triangle = triangle90;
		if(angle == -90.0)
		{
			triangle = triangleNeg90;
		}

		return triangle;
	}

}
