/*
 TriangularLatticeView -- a class within the Cellular Automaton Explorer. 
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
import cellularAutomata.lattice.TriangularLattice;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.EquilateralTriangle;
import cellularAutomata.util.PanelSize;

/**
 * Creates the display area for a triangular-lattice cellular automaton.
 * 
 * @author David Bahr
 */
public class TriangularLatticeView extends TwoDimensionalLatticeView
{
    // The horizontal distances between adjacent row points on the lattice.
    // Depends on the row and col. If (row+col) % 2 = 0, then the cell to the
    // right is at a distance of sideLength*tan(30). If (row+col) % 1 = 0,
    // then the cell to the right is at a distance of 2*sideLength*tan(30).
    // And vice-versa for cells to the left.
    private double horizDeltaShort;

    private double horizDeltaLong;

    // The distance that rows are horizontally inset on the
    // lattice. For even numbered rows, sideLength * tan(30). For odd numbered
    // rows, sideLength * tan(30) / 2.0.
    private double insetDistanceEvenRows;

    private double insetDistanceOddRows;

    // 30 degrees in radians
    private static final double radians30 = 30.0 * (Math.PI / 180.0);

    // the length of a side of the triangle
    private double sideLength;

    // the distance between adjacent column points on the lattice.
    // (sidelength/2)
    private double vertDelta;

    // width of a cell
    private double cellWidth;

    // The length of a side of the triangle rounded up to an integer (for
    // measuring pixels). Roundeed up because we have to ensure that it is at
    // least 1 pixel for display purposes.
    private int iSideLength;

    // width and height of the panel
    private int panelHeight;

    private int panelWidth;

    /**
     * Creates a panel with the specified number of rows and columns.
     * 
     * @param lattice
     *            The triangular lattice.
     */
    public TriangularLatticeView(TriangularLattice lattice)
    {
        super(lattice);

        setSizeParameters();
    }

    /**
     * Set parameters that determine the size of the triangles.
     */
    protected void setSizeParameters()
    {
        panelHeight = this.getHeight();
        panelWidth = this.getWidth();

        // Now get the dimensions of each triangle (in pixels)
        // for the given panel width and height

        // the side length of each triangle painted on the panel
        sideLength = panelHeight / ((getNumRows() / 2.0) + 0.5);

        // The center of a cell on an odd numbered row is horizontally inset
        // by this amount. Even numbered rows are inset by twice this
        // distance.
        insetDistanceEvenRows = sideLength * Math.tan(radians30);
        insetDistanceOddRows = sideLength * Math.tan(radians30) / 2.0;

        // the horizontal distance between points
        horizDeltaLong = 2.0 * sideLength * Math.tan(radians30);
        horizDeltaShort = sideLength * Math.tan(radians30);

        // The vertical distance between rows
        vertDelta = sideLength / 2.0;

        // length of the side of a triangle in pixels (at least 1)
        iSideLength = (int) Math.round(sideLength);

        // the width of a bounding rectangle for each cell
        cellWidth = 1.5 * sideLength * Math.tan(radians30);
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
        int x = (int) Math.ceil(xPos - sideLength / 4.0);
        int y = (int) Math.ceil(yPos - sideLength / 4.0);

        // side length of the default square
        int squareSideLength = (int) Math.ceil(iSideLength / 2.8);

        // fillRect() is relatively fast!
        g.fillRect(x, y, squareSideLength, squareSideLength);
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

        // Unfortunately, this is an N^2 algorithm. Someday when I have time,
        // I'll make it N (similar to drawGrid in the SquareLatticeView class).
        for(int row = 0; row < super.getNumRows(); row++)
        {
            for(int col = 0; col < super.getNumColumns(); col++)
            {
                double angle = 0.0;

                // get the angle of the tip of the triangle, rotated counter
                // clockwise in degrees
                if(row % 2 == 0)
                {
                    if(col % 2 == 0)
                    {
                        angle = -90.0;
                    }
                    else
                    {
                        angle = 90.0;
                    }
                }
                else
                {
                    if(col % 2 == 0)
                    {
                        angle = 90.0;
                    }
                    else
                    {
                        angle = -90.0;
                    }
                }

                EquilateralTriangle triangle = new EquilateralTriangle(
                    getCellXCoordinate(row, col), getCellYCoordinate(row, col),
                    iSideLength, angle);

                // only draw the exterior of the triangle
                g.draw(triangle);
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
     * 
     * @return The height.
     */
    public double getCellHeight(int row, int col)
    {
        // return the height of the triangle when turned on its side, pointing
        // right (or left)
        return sideLength;
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
     * 
     * @return The height.
     */
    public int getCellHeightInPixels(int row, int col)
    {
        return (int) Math.floor(getCellHeight(row, col));
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
     * 
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
            if(col >= getNumColumns() || row >= getNumRows() || col < 0 || row < 0)
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
     * 
     * @return The width.
     */
    public double getCellWidth(int row, int col)
    {
        return cellWidth;
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
     * 
     * @return The width.
     */
    public int getCellWidthInPixels(int row, int col)
    {
        return (int) Math.floor(getCellWidth(row, col));
    }

    /**
     * Get the X coordinate of the center of the cell.
     * 
     * @param row
     *            The cell's row position.
     * @param col
     *            The cell's column position.
     * 
     * @return The X coordinate of the center of the cell.
     */
    public int getCellXCoordinate(int row, int col)
    {
        int xPos;
        if(row % 2 == 0)
        {
            if(col % 2 == 0)
            {
                xPos = (int) Math.floor(insetDistanceEvenRows
                    + (col * 1.5 * sideLength * Math.tan(radians30)));
            }
            else
            {
                xPos = (int) Math.floor(insetDistanceEvenRows
                    + ((col - 1) * 1.5 * sideLength * Math.tan(radians30))
                    + sideLength * Math.tan(radians30));
            }
        }
        else
        {
            if(col % 2 == 0)
            {
                xPos = (int) Math.floor(insetDistanceOddRows
                    + (col * 1.5 * sideLength * Math.tan(radians30)));
            }
            else
            {
                xPos = (int) Math.floor(insetDistanceOddRows
                    + ((col - 1) * 1.5 * sideLength * Math.tan(radians30))
                    + 2.0 * sideLength * Math.tan(radians30));
            }
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
     * 
     * @return The Y coordinate of the center of the cell.
     */
    public int getCellYCoordinate(int row, int col)
    {
        int yPos = (int) Math.floor(vertDelta + ((double) row * vertDelta));

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
     * 
     * @return The row and column as a coordinate pair. May be null if the x and
     *         y position does not correspond to a row and column.
     */
    public Coordinate getRowCol(int xPos, int yPos)
    {
        // the row and col correspoonding to xPos and yPos
        Coordinate coordinate = null;

        // see which cell it falls into
        for(int row = 0; row < getNumRows(); row++)
        {
            for(int col = 0; col < getNumColumns(); col++)
            {
                // get the position of the triangle
                int xTriPos = getCellXCoordinate(row, col);

                int yTriPos = getCellYCoordinate(row, col);

                // the orientation of the tip of the equilateral triangle
                double orientation = getTriangleAngle(row, col);

                EquilateralTriangle triangle = new EquilateralTriangle(xTriPos,
                    yTriPos, (int) Math.round(sideLength), orientation);

                // is the mouse click within this triangle?
                if(triangle.contains(xPos, yPos))
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
     * The length of a side of the triangle.
     * 
     * @return Returns the sideLength.
     */
    public double getSideLength()
    {
        return sideLength;
    }

    /**
     * Gets the orientation (angle) of the tip of the equilateral triangle at
     * each lattice node. Depends on the row and column.
     * 
     * @param row
     *            The lattice row.
     * @param col
     *            The lattice column.
     * @return The angle in degrees.
     */
    public static double getTriangleAngle(int row, int col)
    {
        // the orientation (angle) of the tip of the equilateral triangle
        double orientation = 90.0;
        if((row + col) % 2 == 0)
        {
            orientation = -90.0;
        }

        return orientation;
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

        // Assume each triangle has a side length of 1.
        // Then can rescale the panelWidth and panelHeight.
        // The width and height are doubles to keep precision until rescaled.
        double dPanelWidth = 0.0;
        double dPanelHeight = 0.0;

        dPanelWidth = getNumColumns() * 1.5 * Math.tan(radians30);

        dPanelHeight = 0.5 * getNumRows() + 0.5;

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
    }
}
