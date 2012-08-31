/*
 DrawColorListener -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.graphics.StatusPanel;
import cellularAutomata.lattice.view.LatticeView;
import cellularAutomata.util.Coordinate;

/**
 * A mouse listener that gets the location of the cell under the cursor.
 * 
 * @author David Bahr
 */
public class PositionListener extends LatticeMouseListener
{
    // indicates if the mouse is inside the component
    private boolean insideComponent = true;

    // the status panel that displays info about the current simulation.
    private StatusPanel statusPanel = null;

    /**
     * Create a listener for the mouse that will get the row and column of the
     * cell under the cursor.
     * 
     * @param graphics
     *            A graphics panel with an update that can be called by this
     *            listener.
     * @param statusPanel
     *            The panel that displays info about the current simulation
     *            (like running, stopped, lattice size, etcetera).
     */
    public PositionListener(LatticeView graphics, StatusPanel statusPanel)
    {
        super(graphics);
        this.statusPanel = statusPanel;
    }

    /**
     * Handles the mouse event by getting the row and col of the cell under the
     * cursor..
     * 
     * @param event
     *            The mouseMoved event that called this method.
     */
    private void getPosition(MouseEvent event)
    {
        Coordinate rowColPosition = null;

        if(insideComponent)
        {
            int xPos = event.getX();
            int yPos = event.getY();

            rowColPosition = super.getRowCol(xPos, yPos);

            // Do this so all lattices behave the same outside their range. the
            // hexagonal and triangular lattices are null outside their
            // range, but the square lattices are not.
            if(rowColPosition != null)
            {
                if(rowColPosition.getRow() >= getNumRows()
                    || rowColPosition.getColumn() >= getNumColumns())
                {
                    rowColPosition = null;
                }
            }
        }

        // set the label on the status panel
        statusPanel.setCurrentCursorPositionLabel(rowColPosition);
    }

    /**
     * Does nothing.
     */
    public void mouseClicked(MouseEvent event)
    {
    }

    /**
     * Gets the position of the cursor.
     */
    public void mouseDragged(MouseEvent event)
    {
        if(insideComponent)
        {
            getPosition(event);
        }
    }

    /**
     * Keep track of when inside the component's boundaries.
     */
    public void mouseEntered(MouseEvent event)
    {
        insideComponent = true;
    }

    /**
     * Keep track of when inside the component's boundaries.
     */
    public void mouseExited(MouseEvent event)
    {
        insideComponent = false;

        // so sets a value of null and displays no value
        getPosition(event);
    }

    /**
     * Gets the position of the cursor.
     */
    public void mouseMoved(MouseEvent event)
    {
        if(insideComponent)
        {
            getPosition(event);
        }
    }

    /**
     * Does nothing
     */
    public void mousePressed(MouseEvent event)
    {
    }

    /**
     * Does nothing.
     */
    public void mouseReleased(MouseEvent event)
    {
    }
}
