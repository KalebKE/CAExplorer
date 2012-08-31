/*
 LatticeGasCellStateView -- a class within the Cellular Automaton Explorer. 
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.LatticeGasState;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.util.Arrow;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.Hexagon;

/**
 * Graphics information for the display of hexagonal lattice gas states
 * (LatticeGasState).
 * 
 * @author David Bahr
 */
public class LatticeGasCellStateView extends CellStateView
{
	// thickness of the line on the arrow
	private float LINE_THICKNESS = 0.08f;

	/**
	 * Returns the endpoint of the vector representing the given state.
	 * 
	 * @param gasState
	 *            The state for which the vector position is being calculated.
	 * @param width
	 *            The width of the bounding box that contains the vector.
	 * @return The x and y coordinates of the endpoint of the vector state.
	 * @see cellularAutomaton.cellState.IntegerVectorState#getDisplayShape
	 */
	private Point2D.Double getLineCoordinates(LatticeGasState gasState,
			int width)
	{
		// each array index is a vector with a magnitude given by the array
		// value
		int[] state = (int[]) gasState.getValue();

		// the change in angle created by each array indices' vector.
		double deltaDegrees = 360.0 / 6.0;
		double deltaRadians = deltaDegrees * (Math.PI / 180.0);

		// x is the adjacent side of the triangle created by each
		// array indices' vector.
		double x = 0.0;

		// y is the opposite side of the triangle created by each
		// array indices' vector.
		double y = 0.0;

		for(int i = 0; i < 6; i++)
		{
			// The angle created by each array indices' vector.
			// (Note we subtract from 2.0*deltaRadians because the first array
			// position is the vector pointing northwest.)
			double thetaRadians = (2.0 * deltaRadians) - (i * deltaRadians);

			// the magnitude is the hypotenuse of the triangle created by each
			// array indices' vector.
			double hyp = state[i];

			// but this magnitude (hypotenuse) needs to be rescaled. A value of
			// 1 really means that the vector spans the distance from a cell to
			// its neighbor. This span is given by "width". (See "horizDelta" in
			// the HexagonmalImagePanel class.)
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
	 * @return The shape to be displayed. May be null (in which case the CA
	 *         graphics should use a default shape).
	 * @see cellularAutomata.cellState.view.CellStateView#getAverageDisplayShape(
	 *      cellularAutomata.cellState.model.CellState[], int, int, Coordinate)
	 */
	public Shape getAverageDisplayShape(CellState[] states, int width,
			int height, Coordinate rowAndCol)
	{
		// the shape we will return
		Shape arrow = new Ellipse2D.Double(0.0, 0.0, 0.0, 0.0); // null;

		if(states != null)
		{
			// get the average vector
			double x = 0;
			double y = 0;
			for(int i = 0; i < states.length; i++)
			{
				// get the end point of the vector/arrow
				Point2D.Double endPoint = getLineCoordinates(
						(LatticeGasState) states[i], width);

				x += endPoint.x;
				y += endPoint.y;
			}

			x /= states.length;
			y /= states.length;

			arrow = Arrow.createArrow(0.0, 0.0, x, y);

			// check if it is a wall
			int[] state = (int[]) ((LatticeGasState) states[0]).getValue();

			// if is a wall
			if(state[6] == 1)
			{
				// 30 degrees in radians
				double radians = 30.0 * (Math.PI / 180.0);

				// length of the side of a hexagon
				double sideLength = width / (2.0 * Math.cos(radians));

				// the wall shape
				Shape wall = new Hexagon(0, 0, (int) Math.ceil(sideLength));

				arrow = wall;

				// NOTE: GeneralPath.append has a bug in java 1.6! Not safe. So
				// don't use the code below. Looks better without it anyway.

				// combine the arrow and the wall
				// GeneralPath wallAndArrowCombo = new GeneralPath(arrow);

				// add the wall shape
				// wallAndArrowCombo.append(wall, false);

				// arrow = wallAndArrowCombo;
			}
		}

		if(arrow == null)
		{
			// IMPORTANT:
			// there should always be a non-null shape to avoid
			// problems with erasing old shapes (and other display
			// problems). So make a zero-sized circle. Effectively
			// this is an empty shape.
			arrow = new Ellipse2D.Double(0.0, 0.0, 0.0, 0.0);
		}

		return arrow;
	}

	/**
	 * Get a color for displaying the arrow.
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
	 * graphics.
	 * 
	 * @param state
	 *            The cell state that will be displayed.
	 * @param width
	 *            The width of the rectangle that bounds this Shape. May be
	 *            ignored.
	 * @param height
	 *            The height of the rectangle that bounds this Shape. May be
	 *            ignored.
	 * @return The arrow (vector) to be displayed. May be null (in which case
	 *         the CA graphics should use a default shape).
	 */
	public Shape getDisplayShape(CellState state, int width, int height,
			Coordinate rowAndCol)
	{
		// the shape to return
		Shape arrow = null;

		// the cell state
		int[] latticeState = (int[]) state.getValue();

		// make sure is not a wall
		// Each array index is a vector with a magnitude given by the array
		// value. Get the end point of the vector/arrow
		Point2D.Double endPoint = getLineCoordinates((LatticeGasState) state,
				width);

		// create the vector/arrow shape
		arrow = Arrow.createArrow(0.0, 0.0, endPoint.x, endPoint.y);

		// if is a wall
		if(latticeState[6] == 1)
		{
			// 30 degrees in radians
			double radians = 30.0 * (Math.PI / 180.0);

			// length of the side of a hexagon
			double sideLength = width / (2.0 * Math.cos(radians));

			// the wall shape
			Shape wall = new Hexagon(0, 0, (int) Math.round(sideLength));

			arrow = wall;

			// NOTE: GeneralPath.append has a bug in java 1.6! Not safe. So
			// don't use the code below. Looks better without it anyway.

			// combine the arrow and the wall
			// GeneralPath wallAndArrowCombo = new GeneralPath(arrow);

			// add the wall shape
			// wallAndArrowCombo.append(wall, false);

			// arrow = wallAndArrowCombo;
		}

		if(arrow == null)
		{
			// IMPORTANT:
			// there should always be a non-null shape to avoid
			// problems with erasing old shapes (and other display
			// problems). So make a zero-sized circle. Effectively
			// this is an empty shape.
			arrow = new Ellipse2D.Double(0.0, 0.0, 0.0, 0.0);
		}

		return arrow;
	}

	/**
	 * Thickness of the shapes.
	 */
	public Stroke getStroke(CellState state, int width, int height,
			Coordinate rowAndCol)
	{
		// graphics can't handle a stroke width less than about 1.0. So this
		// fixes that. Why 1.0? Don't know. But I made the minimum stroke 1.2 to
		// be safe.
		float strokeWidth = LINE_THICKNESS * (float) width;
		if(strokeWidth < 1.2f)
		{
			strokeWidth = 1.2f;
		}

		BasicStroke stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER);
		return stroke;
	}
}
