/*
 SquareLatticeView -- a class within the Cellular Automaton Explorer. 
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

// import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D; // import java.awt.Stroke;

import cellularAutomata.Cell;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.lattice.SquareLattice;
import cellularAutomata.util.PanelSize;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.math.MathFunctions;

/**
 * Creates the display area for a square-grid cellular automaton.
 * 
 * @author David Bahr
 */
public class SquareLatticeView extends TwoDimensionalLatticeView
{
	// the height and width of each rectangle (cell) displayed on the panel
	private double cellHeight;

	private double cellWidth;

	// width and height of a cell as an integer
	private int rectHeight;

	private int rectWidth;

	/**
	 * Creates a panel of the specified height and width.
	 * 
	 * @param lattice
	 *            The square lattice.
	 */
	public SquareLatticeView(SquareLattice lattice)
	{
		super(lattice);

		setSizeParameters();
	}

	/**
	 * Set parameters that determine the size of the squares.
	 */
	protected void setSizeParameters()
	{
		// the size of each square lattice cell on the panel
		// (watch that integer division)
		cellWidth = (double) getWidth() / (double) getNumColumns();
		cellHeight = (double) getHeight() / (double) getNumRows();

		// make sure the cell's width and height is not less
		// than one pixel (for display purposes).
		rectWidth = (int) Math.ceil(cellWidth);
		rectHeight = (int) Math.ceil(cellHeight);
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
		int xPos = getCellXCoordinate(row, col)
				- getCellWidthInPixels(row, col) / 2;
		int yPos = getCellYCoordinate(row, col)
				- getCellHeightInPixels(row, col) / 2;

		// fillRect() is relatively fast!
		g.fillRect(xPos, yPos, getCellWidthInPixels(row, col),
				getCellHeightInPixels(row, col));
	}

	/**
	 * Draws a grid mesh on the graphics.
	 */
	public void drawGrid(Graphics2D g)
	{
		g.setColor(Color.BLACK);

		// set the stroke
		// Stroke oldStroke = g.getStroke();
		// g.setStroke(new BasicStroke(2.0f));

		int numRows = super.getNumRows();
		int numCols = super.getNumColumns();

		// draw the horizontal grid lines
		for(int row = 0; row <= numRows; row++)
		{
			int x1 = 0;

			int y1 = (int) Math.floor(row * getCellHeight(row, 0));

			int x2 = width;

			int y2 = y1;

			g.drawLine(x1, y1, x2, y2);
		}

		// draw the vertical grid lines
		for(int col = 0; col <= numCols; col++)
		{
			int x1 = (int) Math.floor(col * getCellWidth(0, col));

			int y1 = 0;

			int x2 = x1;

			int y2 = height;

			g.drawLine(x1, y1, x2, y2);
		}

		// reset the stroke to its original value
		// g.setStroke(oldStroke);
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
		return cellHeight;
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
	 */
	public Cell getCellUnderCursor(int xPos, int yPos)
	{
		// get the row and column corresponding to xPos and yPos
		Coordinate coordinate = getRowCol(xPos, yPos);
		int row = coordinate.getRow();
		int col = coordinate.getColumn();

		// just in case
		if(col >= getNumColumns() || row >= getNumRows() || col < 0 || row < 0)
		{
			return null;
		}

		return getCellArray()[row][col];
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
		return cellWidth;
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
		return rectHeight;
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
	 * @return The width.
	 */
	public int getCellWidthInPixels(int row, int col)
	{
		return rectWidth;
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
		int xPos = (int) Math.ceil(col * getCellWidth(row, col)
				+ getCellWidth(row, col) / 2.0);

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
		int yPos = (int) Math.ceil(row * getCellHeight(row, col)
				+ getCellHeight(row, col) / 2.0);

		return yPos;
	}

	/**
	 * Given x and y coordinates on the graphics, returns the row and column of
	 * the corresponding lattice position.
	 * 
	 * @param xPos
	 *            The horizontal position in pixels on the graphics panel.
	 * @param yPos
	 *            The vertical position in pixels on the graphics panel.
	 * @return The row and column as a coordinate pair.
	 */
	public Coordinate getRowCol(int xPos, int yPos)
	{
		// transform to a cell's array indices
		int row = (int) Math.floor((double) yPos / cellWidth);
		int col = (int) Math.floor((double) xPos / cellHeight);

		return new Coordinate(row, col);
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
		int numCols = getNumColumns();
		int numRows = getNumRows();

		int gcd = MathFunctions.gcd(numCols, numRows);
		int panelWidth = numCols / gcd;
		int panelHeight = numRows / gcd;

		return new PanelSize(panelWidth, panelHeight);

		// OLD CODE THAT FOUND THE MAX SIZE FOR THE AVAILABLE DISPLAY SPACE
		// // constants we'll need
		// int max_height = CAFrame.MAX_CA_HEIGHT;
		// int max_width = CAFrame.MAX_CA_WIDTH;
		//
		// int numCols = getNumColumns();
		// int numRows = getNumRows();
		//
		// // figure out how big the display panel should be
		// int panelHeight;
		// int panelWidth;
		// int cellSize;
		// if(numCols > numRows)
		// {
		// // scale the height to the width
		// cellSize = (int) Math.ceil(((double) max_width / (double) numCols));
		// }
		// else
		// {
		// // scale the width to the height
		// cellSize = (int) Math
		// .ceil(((double) max_height / (double) numRows));
		// }
		// panelHeight = cellSize * numRows;
		// panelWidth = cellSize * numCols;
		//
		// // if display panel is too big, then scale its dimensions to the max
		// // allowed
		// if(panelWidth > max_width)
		// {
		// // then set width to max allowed, and scale height to the width
		// // (order is important -- do panelHeight first)
		// panelHeight = (int) (((double) max_width / (double) panelWidth) *
		// ((double) panelHeight));
		// panelWidth = max_width;
		// }
		//
		// if(panelHeight > max_height)
		// {
		// // then set height to max allowed, and scale width to the height
		// // (order is important -- do panelWidth first)
		// panelWidth = (int) (((double) max_height / (double) panelHeight) *
		// ((double) panelWidth));
		// panelHeight = max_height;
		// }
		//
		// return new PanelSize(panelWidth, panelHeight);
	}
}
