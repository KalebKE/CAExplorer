/*
 LatticeMouseListener -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2007  David B. Bahr (http://academic.regis.edu/dbahr/)

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

package cellularAutomata.lattice.view.listener;

import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputListener;

import cellularAutomata.Cell;
import cellularAutomata.lattice.view.LatticeView;
import cellularAutomata.util.Coordinate;

/**
 * A mouse listener for the lattice.
 * 
 * @author David Bahr
 */
public abstract class LatticeMouseListener implements MouseInputListener
{
    // the CA graphics panel with an update method that can be called by this
    // Listener
    private LatticeView graphics;

    /**
     * Create a listener for the mouse that will color cells when they are
     * clicked.
     * 
     * @param graphics
     *            A graphics panel with an update that can be called by this
     *            listener.
     */
    public LatticeMouseListener(LatticeView graphics)
    {
        this.graphics = graphics;
    }

    /**
     * Draws the specified cell at the specified position on the graphics.
     * 
     * @param cell
     *            The CA cell that will be drawn.
     * @param xPos
     *            The x-position of the cursor. The cell is placed in the
     *            lattice position that falls under the cursor.
     * @param yPos
     *            The y-position of the cursor. The cell is placed in the
     *            lattice position that falls under the cursor.
     */
    public void drawCell(Cell cell, int xPos, int yPos)
    {
        // draw the cell on the graphics
        graphics.drawCell(cell, xPos, yPos);
    }

    /**
     * Gets the cell clicked or underneath the cursor. A convenience method.
     * 
     * @param event
     *            The mouse event.
     * 
     * @return The cell under the cursor. May be null if outside the lattice.
     */
    protected Cell getCellUnderCursor(MouseEvent event)
    {
        // find where the mouse was clicked on the panel
        int xPos = event.getX();
        int yPos = event.getY();

        return getCellUnderCursor(xPos, yPos);
    }

    /**
     * Gets the cell clicked or underneath the cursor. A convenience method.
     * 
     * @param xPos
     *            The x-position of the cursor.
     * @param yPos
     *            The y-position of the cursor.
     */
    protected Cell getCellUnderCursor(int xPos, int yPos)
    {
        // get the cell that was selected by the cursor
        return graphics.getCellUnderCursor(xPos, yPos);
    }

    /**
     * Gets the generation of the cell clicked or underneath the cursor.
     * Sometimes (for example, in one-dimensional lattices) may be drawing on
     * previous generations.
     * <p>
     * A convenience method.
     * 
     * @param event
     *            The mouse event.
     * 
     * @return The generation of the cell under the cursor.
     */
    protected int getGenerationUnderCursor(MouseEvent event)
    {
        // find where the mouse was clicked on the panel
        int xPos = event.getX();
        int yPos = event.getY();

        return getGenerationUnderCursor(xPos, yPos);
    }

    /**
     * Gets the generation of the cell clicked or underneath the cursor.
     * Sometimes (for example, in one-dimensional lattices) may be drawing on
     * previous generations.
     * <p>
     * A convenience method.
     * 
     * @param xPos
     *            The x-position of the cursor.
     * @param yPos
     *            The y-position of the cursor.
     */
    protected int getGenerationUnderCursor(int xPos, int yPos)
    {
        // in some cases, like 1-d, may be drawing on a previous state
        return graphics.getGenerationUnderCursor(xPos, yPos);
    }

    /**
     * Gets the number of columns in the lattice.
     * 
     * @return The number of columns (-1 if has no meaning for the given
     *         lattice).
     */
    protected int getNumColumns()
    {
        return graphics.getNumColumns();
    }

    /**
     * Gets the number of rows in the lattice.
     * 
     * @return The number of rows (-1 if has no meaning for the given lattice).
     */
    protected int getNumRows()
    {
        return graphics.getNumRows();
    }

    /**
     * Gets the row and col of the cell under the cursor.
     * 
     * @param xPos
     *            The x-position of the cursor.
     * @param yPos
     *            The y-position of the cursor.
     * 
     * @return The row and column as a coordinate pair, or null if rows and
     *         columns do not make sense for the given lattice (for example, a
     *         tree).
     */
    protected Coordinate getRowCol(int xPos, int yPos)
    {
        return graphics.getRowCol(xPos, yPos);
    }

    /**
     * Makes sure that the JPanel is updated to reflect any changes.
     */
    public void updateGraphics()
    {
        // make sure it is displayed
        graphics.update();
    }

    /**
     * Gets the graphics (lattice panel).
     * 
     * @return The lattice panel.
     */
    public LatticeView getLatticePanel()
    {
        return graphics;
    }
}
