/*
 AnalysisDrawingListener -- a class within the Cellular Automaton Explorer. 
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
import java.util.Observable;

import javax.swing.event.MouseInputListener;

import cellularAutomata.Cell;
import cellularAutomata.CAConstants;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.lattice.view.LatticeView;
import cellularAutomata.util.SwingWorker;

/**
 * A mouse listener that notifies analysis of any cells that are clicked,
 * selected, or "drawn".
 * <p>
 * While this class is listening to the graphics, it also has to notify the
 * analyses. In other words, this class is both a listener and is being
 * "listened to"!
 * <p>
 * This class uses the Observer design pattern so that it can be "listened to".
 * Analyses are attached as observers.
 * 
 * @author David Bahr
 */
public class AnalysisDrawingListener extends Observable implements
    MouseInputListener
{
    // indicates if any mouse button is pressed
    private boolean buttonPressed = false;

    // indicates if the right mouse button is currently being pushed (this will
    // also be true if the ctrl key is selected)
    private boolean rightClicked = false;

    // indicates if the mouse is inside the component during a drag event
    private boolean insideComponent = true;

    // the CA graphics panel with an update method that can be called by this
    // Listener
    private LatticeView graphics;

    /**
     * Create a mouse listener that notifies analysis of any cells that are
     * clicked, selected, or "drawn".
     * 
     * @param graphics
     *            A graphics panel with an update that can be called by this
     *            listener.
     */
    public AnalysisDrawingListener(LatticeView graphics)
    {
        this.graphics = graphics;
    }

    /**
     * Handles the mouse event by calling a method in the analysis.
     * 
     * @param dragging
     *            True if the mouse is being dragged (any mouse button is
     *            pressed while the mouse is moving).
     * @param rightClicked
     *            True if it was a right click (instead of a left click), which
     *            may cause different behavior.
     * @param buttonPressed
     *            True if any mouse button is pressed.
     */
    private void notifyAnalysis(MouseEvent event, boolean dragging,
        boolean rightClicked, boolean buttonPressed)
    {
        if(buttonPressed)
        {
            // find where the mouse was clicked on the panel
            int xPos = event.getX();
            int yPos = event.getY();

            notifyAnalysis(event, xPos, yPos, dragging, rightClicked,
                buttonPressed);
        }
    }

    /**
     * Handles the mouse event by calling a method in the analysis.
     * 
     * @param dragging
     *            True if the mouse is being dragged (any mouse button is
     *            pressed while the mouse is moving).
     * @param rightClicked
     *            True if it was a right click (instead of a left click), which
     *            may cause different behavior.
     * @param buttonPressed
     *            True if any mouse button is pressed.
     */
    private void notifyAnalysis(MouseEvent event, int xPos, int yPos,
        boolean dragging, boolean rightClicked, boolean buttonPressed)
    {
        // get the cell that was selected by the cursor
        Cell cell = graphics.getCellUnderCursor(xPos, yPos);

        // in some cases, like 1-d, may be drawing on a previous state
        int generation = graphics.getGenerationUnderCursor(xPos, yPos);

        // the mouse state at the moment this method was called
        MouseState state = new MouseState(event, xPos, yPos, cell, generation,
            dragging, rightClicked, buttonPressed);
        
        // notify the analyses that are observing this class
        this.setChanged();
        this.notifyObservers(state);
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
     * Does nothing.
     */
    public void mouseClicked(MouseEvent event)
    {
    }

    /**
     * Decides what to do when a mouse moves over a cell.
     */
    public void mouseDragged(MouseEvent event)
    {
        if(buttonPressed && insideComponent)
        {
            // find where the mouse was clicked on the panel
            final int xPos = event.getX();
            final int yPos = event.getY();
            final MouseEvent mouseEvent = event;
            final SwingWorker caWorker = new SwingWorker()
            {
                public Object construct()
                {
                    // act as a right click if (1) is a right click, or (2)
                    // ctrl is pressed
                    notifyAnalysis(mouseEvent, xPos, yPos, true, rightClicked
                        || CAFrame.controlKeyDown, buttonPressed);

                    return null;
                }
            };
            caWorker.start();
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
    }

    /**
     * Handles mouse move events.
     */
    public void mouseMoved(MouseEvent event)
    {
        if(!CAConstants.WINDOWS_OS)
        {
            // there is no such thing as a right click drag even for macs, so we
            // do this to simulate that
            mouseDragged(event);
        }
    }

    /**
     * Decides what to do when a cell is clicked by the mouse.
     */
    public void mousePressed(MouseEvent event)
    {
        buttonPressed = true;

        // indicates if the control key is pressed. If so it acts like a mouse
        // right click (to make this code mac friendly)
        boolean controlIsPressed = CAFrame.controlKeyDown;

        // find out if it was a right click (less common)
        rightClicked = ((event.getButton() == MouseEvent.BUTTON3) || controlIsPressed);

        if(insideComponent)
        {
            notifyAnalysis(event, false, rightClicked, buttonPressed);
        }
    }

    /**
     * Stops any movement from coloring cells.
     */
    public void mouseReleased(MouseEvent event)
    {
        buttonPressed = false;
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
     * The state of the mouse button. Indicates if the mouse is being pressed,
     * dragged, and/or right-clicked. Also indicates which x and y-position is
     * being clicked (or dragged, etc.), and which cell is being selected.
     * 
     * @author David Bahr
     */
    public class MouseState
    {
        // The x position of the mouse.
        private int xPos = 0;

        // The y position of the mouse.
        private int yPos = 0;

        // the generation of the cell under the cursor
        private int generation = 0;

        // The cell that is under the mouse.
        private Cell cell = null;

        // The mouse event
        private MouseEvent event = null;

        // True if the mouse is being dragged (any mouse button is
        // pressed while the mouse is moving).
        private boolean dragging = false;

        // True if it was a right click (instead of a left click),
        // which may cause different behavior.
        private boolean rightClicked = false;

        // True if any mouse button is pressed.
        private boolean buttonPressed = false;

        /**
         * Capture a mouse state.
         * 
         * @param event
         *            The mouse event.
         * @param xPos
         *            The x position of the mouse.
         * @param yPos
         *            The y position of the mouse.
         * @param cell
         *            The cell that is under the mouse.
         * @param generation
         *            The generation of the cell under the cursor (may not be
         *            the last generation in some 1-d simulations).
         * @param dragging
         *            True if the mouse is being dragged (any mouse button is
         *            pressed while the mouse is moving).
         * @param rightClicked
         *            True if it was a right click (instead of a left click),
         *            which may cause different behavior.
         * @param buttonPressed
         *            True if any mouse button is pressed.
         */
        public MouseState(MouseEvent event, int xPos, int yPos, Cell cell,
            int generation, boolean dragging, boolean rightClicked,
            boolean buttonPressed)
        {
            super();
            this.event = event;
            this.xPos = xPos;
            this.yPos = yPos;
            this.cell = cell;
            this.generation = generation;
            this.dragging = dragging;
            this.rightClicked = rightClicked;
            this.buttonPressed = buttonPressed;
        }

        /**
         * @return the cell under the cursor.
         */
        public Cell getCell()
        {
            return cell;
        }

        /**
         * @return the mouse event.
         */
        public MouseEvent getEvent()
        {
            return event;
        }

        /**
         * @return the generation of the cell under the mouse cursor
         */
        public int getGeneration()
        {
            return generation;
        }

        /**
         * @return the xPos of the mouse.
         */
        public int getXPos()
        {
            return xPos;
        }

        /**
         * @return the yPos of the mouse.
         */
        public int getYPos()
        {
            return yPos;
        }

        /**
         * @return true if a button is pressed
         */
        public boolean isButtonPressed()
        {
            return buttonPressed;
        }
        
        /**
         * @return true if the mouse is dragging (with eithher a right or a left
         *         mouse click).
         */
        public boolean isDragging()
        {
            return dragging;
        }

        /**
         * @return true if the mouse was right-clicked
         */
        public boolean isRightClicked()
        {
            return rightClicked;
        }
        
    }
}
