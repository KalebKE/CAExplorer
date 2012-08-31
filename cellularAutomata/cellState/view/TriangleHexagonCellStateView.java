/*
 TriangleHexagonCellStateView -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.lattice.view.TriangularLatticeView;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.EquilateralTriangle;
import cellularAutomata.util.Hexagon;

/**
 * A view that displays squares on square lattices, hexagons on hexagonal
 * lattices, and triangles on triangular lattices. The user only has to specify
 * the color by implementing the abstract method getColor. The color of tagged
 * cells is specified by this class, but can be overridden by a child class.
 * <br>
 * This view will slow down the graphics because the triangles and hexagons are
 * expensive to draw relative to simple squares. If triangles and squares aren't
 * necessary for the triangular and hexagonal lattices, then recommend using a
 * different view.
 * 
 * @author David Bahr
 */
public abstract class TriangleHexagonCellStateView extends CellStateView
{
	// true if the lattice is hexagonal
	private boolean hexagonal = false;

	// true if the lattice is triangular
	private boolean triangular = false;

	// a shape that might be displayed
	private Hexagon hexagon = null;

	// the shape displayed when the triangle should point to the right
	private EquilateralTriangle triangle90 = null;

	// the shape displayed when the triangle should point to the left
	private EquilateralTriangle triangleNeg90 = null;

	// the original height of the triangle (before any rescaling of the
	// panel
	// size)
	private int oldHeight = 0;

	// the original width of the hexagon (before any rescaling of the panel
	// size)
	private int oldWidth = 0;

	/**
	 * Creates a view that uses squares, hexagons, or triangles, as appropriate.
	 */
	public TriangleHexagonCellStateView()
	{
		String latticeName = CurrentProperties.getInstance()
				.getLatticeDisplayName();

		triangular = false;
		hexagonal = false;
		if(latticeName.indexOf("triang") != -1)
		{
			// then triangular
			triangular = true;
			hexagonal = false;
		}
		else if(latticeName.indexOf("hex") != -1)
		{
			// then hexagonal
			triangular = false;
			hexagonal = true;
		}
	}

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

		hexagon = new Hexagon(0, 0, (int) Math.round(sideLength));
	}

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
	 * Returns null so that the default shape is used (a square), or returns a
	 * hexagon or triangle for those lattices.
	 * 
	 * @see cellularAutomata.cellState.view.CellStateView#getAverageDisplayShape(
	 *      cellularAutomata.cellState.model.CellState[], int, int, Coordinate)
	 */
	public Shape getAverageDisplayShape(CellState[] states, int width,
			int height, Coordinate rowAndCol)
	{
		Shape shape = null;

		if(triangular)
		{
			// check to see if the height changed (happens when panel is
			// resized)
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

			// The row and the col determine the orientation of the
			// triangle.
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

			shape = triangle;
		}
		else if(hexagonal)
		{
			// check to see if the width changed (happens when panel is
			// resized)
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

			shape = hexagon;
		}

		return shape;
	}

	/**
	 * Should create a Color appropriate for displaying the current state of
	 * this cellState. For example, if the state is 1, then an appropriate color
	 * might be Color.BLACK.
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
	protected abstract Color getColor(CellState state, Integer numStates,
			Coordinate rowAndCol);

	/**
	 * Returns null so that the default shape is used (a square), or returns a
	 * hexagon or triangle for those lattices.
	 * 
	 * @see cellularAutomata.cellState.view.CellStateView#getDisplayShape(
	 *      cellularAutomata.cellState.model.CellState, int, int, Coordinate)
	 */
	public Shape getDisplayShape(CellState state, int width, int height,
			Coordinate rowAndCol)
	{
		Shape shape = null;

		if(triangular)
		{
			// check to see if the height changed (happens when panel is
			// resized)
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

			// The row and the col determine the orientation of the
			// triangle.
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

			shape = triangle;
		}
		else if(hexagonal)
		{
			// check to see if the width changed (happens when panel is
			// resized)
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

			shape = hexagon;
		}

		return shape;
	}
}