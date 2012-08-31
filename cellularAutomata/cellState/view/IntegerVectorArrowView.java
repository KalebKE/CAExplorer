/*
 IntegerVectorArrowView -- a class within the Cellular Automaton Explorer. 
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
import java.awt.geom.Point2D;

import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.IntegerVectorState;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.util.Arrow;
import cellularAutomata.util.Coordinate;

/**
 * Provides vector (arrow) graphics for the IntegerVectorState class. These
 * graphics are suitable for simulations where each component of the vector
 * state indicates the maginitude of a vector along a lattice direction. This
 * class is not useful for lattice gas simulations because the last element of
 * the vector represents the presence or absence of a wall rather than the
 * magnitude of a vector.
 * <p>
 * Also see IntegerVectorDefaultView.
 * 
 * @author David Bahr
 */
public class IntegerVectorArrowView extends CellStateView
{
	/**
	 * Returns the endpoint of the vector representing the given state.
	 * 
	 * @param The
	 *            state for which the vector position is beiong calculated.
	 * @param The
	 *            width of the bounding box that conpatins the vector.
	 * @return The x and y coordinates of the endpoint of the vector state.
	 * @see cellularAutomaton.cellState.IntegerVectorState#getDisplayShape
	 */
	private Point2D.Double getLineCoordinates(IntegerVectorState vectorState,
			int width)
	{
		// each array index is a vector with a magnitude given by the array
		// value
		int[] state = (int[]) vectorState.getValue();

		// the change in angle created by each array indices' vector.
		double deltaDegrees = 360.0 / state.length;
		double deltaRadians = deltaDegrees * (Math.PI / 180.0);

		// x is the adjacent side of the triangle created by each
		// array indices' vector.
		double x = 0.0;

		// y is the opposite side of the triangle created by each
		// array indices' vector.
		double y = 0.0;

		for(int i = 0; i < state.length; i++)
		{
			// The angle created by each array indices' vector.
			// (Note we subtract from 0.0*deltaRadians because the first array
			// position is the vector pointing along the x-axis.)
			double thetaRadians = (0.0 * deltaRadians) - (i * deltaRadians);

			// the magnitude is the hypotenuse of the triangle created by each
			// array indices' vector.
			double hyp = state[i];

			// but this magnitude (hypotenuse) needs to be rescaled. A value of
			// 1 really means that the vector spans the distance from a cell to
			// its neighbor. This span is given by "width".
			hyp *= width;

			// We add the contribution of each vector.
			// (Actually, we subtract y, so that the coordinates are consistent
			// with the Java graphics coordinates that point y downwards.)
			x += hyp * Math.cos(thetaRadians);
			y -= hyp * Math.sin(thetaRadians);
		}

		return new Point2D.Double(x, y);
	}

	/**
	 * Creates an average of all the vectors represented by the cell states.
	 * <br>
	 * Child classes should override this method if they need a different
	 * average shape.
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
		// the shape we will return
		Shape arrow = null;

		if(states != null)
		{
			// get the average vector
			double x = 0;
			double y = 0;
			for(int i = 0; i < states.length; i++)
			{
				// get the end point of the vector/arrow
				Point2D.Double endPoint = getLineCoordinates(
						(IntegerVectorState) states[i], width);

				x += endPoint.x;
				y += endPoint.y;
			}

			x /= states.length;
			y /= states.length;

			arrow = Arrow.createArrow(0.0, 0.0, x, y);
		}

		return arrow;
	}

	/**
	 * Get a color for the arrows.
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
		return ColorScheme.FILLED_COLOR;
	}

	/**
	 * Creates a two-dimensional vector from the state array (of length N). This
	 * is not the normal way of mapping arrays to vectors, but this classes'
	 * array has to be meaningful in two dimensions (not N dimensions for an
	 * N-dimensional array). Assumes that each array position is a vector of the
	 * given magnitude (the array value). The vector points in a direction that
	 * increases by 360/N degrees for each index. Each of these vectors are
	 * summed together. <br>
	 * Child classes should override this method if they need different
	 * graphics. In fact this code assumes that the first coordinate of the
	 * vector corresponds to a direction along the x-axis. This may not be the
	 * case for a child class (such as the hexagonal lattice gas which assumes
	 * that the first index points to the neighbor in the northwest direction.).
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
	 * @return The vector to be displayed. May be null (in which case the CA
	 *         graphics should use a default shape).
	 */
	public Shape getDisplayShape(CellState state, int width, int height,
			Coordinate rowAndCol)
	{
		// Each array index is a vector with a magnitude given by the array
		// value. Get the end point of the vector/arrow
		Point2D.Double endPoint = getLineCoordinates(
				(IntegerVectorState) state, width);

		// create the vector/arrow shape
		Shape arrow = Arrow.createArrow(0.0, 0.0, endPoint.x, endPoint.y);

		return arrow;
	}
}
