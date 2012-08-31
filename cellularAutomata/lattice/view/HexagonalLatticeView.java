/*
 HexagonalLatticeView -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.lattice.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import cellularAutomata.Cell;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.lattice.HexagonalLattice;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.Hexagon;
import cellularAutomata.util.PanelSize;
import cellularAutomata.util.math.MathFunctions;

/**
 * Creates the display area for a hexagonal-lattice cellular automaton.
 * 
 * @author David Bahr
 */
public class HexagonalLatticeView extends TwoDimensionalLatticeView
{
	// the distance between adjacent row points on the lattice
	private double horizDelta;

	// the distance that even numbered rows are horizontally inset on the
	// lattice
	private double insetDistance;

	// 30 degrees in radians
	private static final double radians = 30.0 * (Math.PI / 180.0);

	// the length of a side of the hexagon
	private double sideLength;

	// the distance between adjacent column points on the lattice
	private double vertDelta;

	// The length of a side of the hexagon. Also used as the default cell size
	// in pixels
	private int iSideLength;

	// width and height of the panel
	private int panelHeight;

	private int panelWidth;

	/**
	 * Creates a panel with the correct number of rows and columns.
	 * 
	 * @param lattice
	 *            The Hexagonal lattice.
	 */
	public HexagonalLatticeView(HexagonalLattice lattice)
	{
		super(lattice);

		setSizeParameters();
	}

	/**
	 * Set parameters that determine the size of the hexagons.
	 */
	protected void setSizeParameters()
	{
		panelHeight = this.getHeight();
		panelWidth = this.getWidth();

		// Now get the dimensions of each hexagon (in pixels)
		// for the given panel width and height
		// the side length of each hexagon (cell) painted on the panel
		sideLength = panelHeight / ((getNumRows() * 1.5) + 0.5);

		// The center of a cell on an odd numbered row is horizontally inset
		// by this amount. Even numbered rows are inset by twice this
		// distance.
		insetDistance = sideLength * Math.cos(radians);

		// the horizontal distance between points on a row
		horizDelta = 2.0 * sideLength * Math.cos(radians);

		// The vertical distance between rows
		vertDelta = sideLength + (sideLength / 2.0);

		// length of the side of a hexagon
		// iSideLength = (int) Math.ceil(sideLength);
		iSideLength = (int) Math.round(sideLength);
	}

	/**
	 * Draw a default shape for the lattice. This should be a quickly drawn
	 * shape. The graphics method fillRect() is relatively quick and is a good
	 * choice. Note that this method is only called when the CellStateView
	 * returns null for the shape to draw -- hence this is only a backup and a
	 * default. <br>
	 * This method can be used intentionally for speed. (When drawing a Shape
	 * would take too long, have the CellStateView return null for
	 * getDisplayShape(), then this method will draw quickly.)
	 * 
	 * @param row
	 *            The row position on the lattice.
	 * @param col
	 *            The column position on the lattice.
	 * @param g
	 *            The graphics object onto which the default shape should be
	 *            drawn.
	 */
	public void drawDefaultShapeOnGraphics(int row, int col, Graphics2D g)
	{
		// get the position of the shape
		int xPos = getCellXCoordinate(row, col);
		int yPos = getCellYCoordinate(row, col);

		// but translate
		int x = (int) Math.ceil(xPos - horizDelta / 2.0);
		int y = (int) Math.ceil(yPos - sideLength / 2.0);

		// fillRect() is relatively fast!
		g.fillRect(x, y, iSideLength, iSideLength);
	}

	/**
	 * Draws a grid mesh on the graphics.
	 */
	protected void drawGrid(Graphics2D g)
	{
		g.setColor(Color.BLACK);

		// set the stroke
		Stroke oldStroke = g.getStroke();
		g.setStroke(new BasicStroke(2.0f));

		for(int row = 0; row < super.getNumRows(); row++)
		{
			for(int col = 0; col < super.getNumColumns(); col++)
			{
				Hexagon hexagon = new Hexagon(getCellXCoordinate(row, col),
						getCellYCoordinate(row, col), (int) Math
								.round(sideLength));

				// only draw the exterior of the triangle
				g.draw(hexagon);
			}
		}

		// reset the stroke to its original value
		g.setStroke(oldStroke);
	}

	/**
	 * The height of the bounding rectangle for each cell's graphics (when
	 * displayed on the panel).
	 * 
	 * @param row
	 *            The row position on the lattice.
	 * @param col
	 *            The column position on the lattice.
	 * @return The height.
	 */
	public double getCellHeight(int row, int col)
	{
		return vertDelta;
	}

	/**
	 * The height of the bounding rectangle for each cell's graphics (when
	 * displayed on the panel) in integer pixels. The number of pixels per cell
	 * should never be less that 1, or it will not display.
	 * 
	 * @param row
	 *            The row position on the lattice.
	 * @param col
	 *            The column position on the lattice.
	 * @return The height.
	 */
	public int getCellHeightInPixels(int row, int col)
	{
		return (int) Math.floor(vertDelta);
	}

	/**
	 * Return the cell displayed at the current cursor position xPos and YPos.
	 * This method is useful, for example, when we want to change the appearance
	 * or content of cells (with a mouse listener). This method is currently
	 * used to draw on the lattice.
	 * 
	 * @param xPos
	 *            The horizontal coordinate of the mouse.
	 * @param yPos
	 *            The vertical coordinate of the mouse.
	 * @return The cell displayed at the position xPos, yPos on the graphics.
	 *         May be null if there is no cell under the cursor.
	 */
	public Cell getCellUnderCursor(int xPos, int yPos)
	{
		// the cell under the cursor
		Cell cell = null;

		// get the row and col corresponding to xPos and yPos
		Coordinate coordinate = getRowCol(xPos, yPos);

		if(coordinate != null)
		{
			int row = coordinate.getRow();
			int col = coordinate.getColumn();

			// just in case
			if(col >= getNumColumns() || row >= getNumRows() || col < 0
					|| row < 0)
			{
				return null;
			}

			cell = getCellArray()[row][col];
		}
		// return the cell at position xPos and yPos
		return cell;
	}

	/**
	 * The width of the bounding rectangle for each cell's graphics (when
	 * displayed on the panel).
	 * 
	 * @param row
	 *            The row position on the lattice.
	 * @param col
	 *            The column position on the lattice.
	 * @return The width.
	 */
	public double getCellWidth(int row, int col)
	{
		return horizDelta;
	}

	/**
	 * The width of the bounding rectangle for each cell's graphics (when
	 * displayed on the panel) in integer pixels. The number of pixels per cell
	 * should never be less that 1, or it will not display.
	 * 
	 * @param row
	 *            The row position on the lattice.
	 * @param col
	 *            The column position on the lattice.
	 * @return The width.
	 */
	public int getCellWidthInPixels(int row, int col)
	{
		return (int) Math.floor(horizDelta);
	}

	/**
	 * Get the X coordinate of the center of the cell.
	 * 
	 * @param row
	 *            The cell's row position.
	 * @param col
	 *            The cell's column position.
	 * @return The X coordinate of the center of the cell.
	 */
	public int getCellXCoordinate(int row, int col)
	{
		int xPos;
		if(row % 2 == 0)
		{
			xPos = (int) Math.floor(insetDistance + (col * horizDelta));
		}
		else
		{
			xPos = (int) Math.floor(2.0 * insetDistance + (double) col
					* horizDelta);
		}

		return xPos;
	}

	/**
	 * Get the Y coordinate of the center of the cell.
	 * 
	 * @param row
	 *            The cell's row position.
	 * @param col
	 *            The cell's column position.
	 * @return The Y coordinate of the center of the cell.
	 */
	public int getCellYCoordinate(int row, int col)
	{
		int yPos = (int) Math.floor(sideLength + ((double) row * vertDelta));

		return yPos;
	}

	/**
	 * The horizontal distance between row points.
	 * 
	 * @return Returns the horizontal distance between row points.
	 */
	public double getHorizDelta()
	{
		return horizDelta;
	}

	/**
	 * Every even numbered row is inset a fixed distance.
	 * 
	 * @return Returns the inset distance.
	 */
	public double getInsetDistance()
	{
		return insetDistance;
	}

	/**
	 * The vertical distance between rows.
	 * 
	 * @return Returns the vertical distance between rows.
	 */
	public double getVertDelta()
	{
		return vertDelta;
	}

	/**
	 * Given x and y coordinates on the graphics, returns the row and column of
	 * the corresponding lattice position.
	 * 
	 * @param xPos
	 *            The horizontal position in pixels on the graphics panel.
	 * @param yPos
	 *            The vertical position in pixels on the graphics panel.
	 * @return The row and column as a coordinate pair. May be null if the x and
	 *         y position does not correspond to a row and column.
	 */
	public Coordinate getRowCol(int xPos, int yPos)
	{
		// the row and col corresponding to xPos and yPos
		Coordinate coordinate = null;

		// see which cell it falls into
		for(int row = 0; row < getNumRows(); row++)
		{
			for(int col = 0; col < getNumColumns(); col++)
			{
				// get the position of the hexagon
				int xHexPos;
				if(row % 2 == 0)
				{
					xHexPos = (int) Math.floor(insetDistance
							+ (col * horizDelta));
				}
				else
				{
					xHexPos = (int) Math.floor(2.0 * insetDistance
							+ (double) col * horizDelta);
				}

				int yHexPos = (int) Math.floor(sideLength
						+ ((double) row * vertDelta));

				Hexagon hexagon = new Hexagon(xHexPos, yHexPos, (int) Math
						.round(sideLength));

				// is the mouse click within this hexagon?
				if(hexagon.contains(xPos, yPos))
				{
					// specify the coordinate corresponding to position xPos and
					// yPos
					coordinate = new Coordinate(row, col);

					// exit loops
					row = getNumRows();
					col = getNumColumns();
				}
			}
		}

		return coordinate;
	}

	/**
	 * The length of a side of the hexagon.
	 * 
	 * @return Returns the sideLength.
	 */
	public double getSideLength()
	{
		return sideLength;
	}

	/**
	 * Rescale the width and height of the graphics panel so that it is the
	 * smallest possible with the correct ratio. This size may vary with the
	 * type of lattice. For example, the hexagonal lattice has rows that are
	 * offset (every other row is offset slightly to the right, unlike a square
	 * lattice); this increases the width of the lattice. So the panel's width
	 * and height have to be rescaled to reflect this.
	 * 
	 * @return A pair of integers for the width and height, as small as possible
	 *         that retains the correct ratio.
	 */
	public PanelSize rescaleToMinimumWidthAndHeight()
	{
		// constants we'll need
		int max_height = CAFrame.MAX_CA_HEIGHT;
		int max_width = CAFrame.MAX_CA_WIDTH;

		// Assume each hexagon has a side length of 1.
		// Then can rescale to the panelWidth and panelHeight.
		// The width and height are doubles to keep precision until rescaled.
		double dPanelWidth = (2.0 * getNumColumns() + 1.0) * Math.cos(radians);
		double dPanelHeight = (1.5 * getNumRows()) + 0.5;

		// figure out how big the display panel should be
		if(dPanelWidth > dPanelHeight)
		{
			// scale the height to the maximum width
			panelHeight = (int) Math.ceil(dPanelHeight
					* ((double) max_width / dPanelWidth));
			panelWidth = max_width;
		}
		else
		{
			// scale the width to the maximum height
			panelWidth = (int) Math.ceil(dPanelWidth
					* ((double) max_height / dPanelHeight));
			panelHeight = max_height;
		}

		return new PanelSize(panelWidth, panelHeight);

		// DOESN'T WORK
		// // Assume each hexagon has a side length of 1.
		// // Then can rescale the panelWidth and panelHeight.
		// // The width and height are doubles to keep precision until rescaled.
		// double dPanelWidth = (getNumColumns() + 0.5) * 2.0 *
		// Math.cos(radians);
		// double dPanelHeight = (2.0 * getNumRows()) + (int) (getNumRows() /
		// 2.0);
		//
		// // figure out how big the display panel should be
		// int scaledWidth = 1;
		// int scaledHeight = 1;
		// if(dPanelWidth > dPanelHeight)
		// {
		// // scale the width to a height of 1
		// scaledHeight = 1;
		// scaledWidth = (int) Math.ceil(dPanelWidth / dPanelHeight);
		// }
		// else
		// {
		// // scale the height to a width of 1
		// scaledWidth = 1;
		// scaledHeight = (int) Math.ceil(dPanelHeight / dPanelWidth);
		// }
		//
		// return new PanelSize(scaledWidth, scaledHeight);
	}
}
