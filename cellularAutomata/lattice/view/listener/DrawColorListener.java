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

import javax.swing.event.MouseInputListener;

import cellularAutomata.Cell;
import cellularAutomata.CAConstants;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.lattice.view.LatticeView;
import cellularAutomata.util.SwingWorker;

/**
 * A mouse listener that colors cells when they are clicked.
 * 
 * @author David Bahr
 */
public class DrawColorListener implements MouseInputListener
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
	 * Create a listener for the mouse that will color cells when they are
	 * clicked.
	 * 
	 * @param graphics
	 *            A graphics panel with an update that can be called by this
	 *            listener.
	 */
	public DrawColorListener(LatticeView graphics)
	{
		this.graphics = graphics;
	}

	/**
	 * Handles the mouse event by changing the color of the cell.
	 * 
	 * @param setBlack
	 *            If true, sets the color to black, but if false toggles the
	 *            color (e.g., black to white).
	 * @param rightClicked
	 *            True if it was a right click (instead of a left click), which
	 *            causes different graphics to be drawn.
	 */
	private void changeColor(MouseEvent event, boolean setBlack,
			boolean rightClicked)
	{
		if(buttonPressed)
		{
			// find where the mouse was clicked on the panel
			int xPos = event.getX();
			int yPos = event.getY();

			changeColor(xPos, yPos, setBlack, rightClicked);
		}
	}

	/**
	 * Handles the mouse event by changing the color of the cell.
	 * 
	 * @param setBlack
	 *            If true, sets the color to black, but if false toggles the
	 *            color (for example, black to white).
	 * @param rightClicked
	 *            True if it was a right click (instead of a left click), which
	 *            causes different graphics to be drawn.
	 */
	private void changeColor(int xPos, int yPos, boolean setBlack,
			boolean rightClicked)
	{
		if(buttonPressed)
		{
			// get the cell that was selected by the cursor
			Cell cell = graphics.getCellUnderCursor(xPos, yPos);

			// in some cases, like 1-d, may be drawing on a previous state
			int generation = graphics.getGenerationUnderCursor(xPos, yPos);

			// might be null if there was no cell under the cursor
			if((cell != null) && (generation >= 0))
			{
				CellState state = cell.getState(generation);
				if(state != null)
				{
					// Find out if the state is the drawing state (by default,
					// the "full" state unless overridden in a child class, like
					// IntegerCellState).
					boolean isDrawState = state.isDrawState();

					// Find out if is an alternate state.
					boolean isAlternate = state.isAlternate();

					// Flip the state (usually 1 to 0, and 0 to 1).
					if(!rightClicked && (setBlack || !isDrawState))
					{
						state.setToDrawingState();
					}
					else if(rightClicked && (setBlack || !isAlternate))
					{
						// with a right click may want to draw something
						// different
						state.setToAlternateState();
					}
					else
					{
						state.setToEmptyState();
					}

					// draw the graphics
					graphics.drawCell(cell, xPos, yPos);

					// make sure it is displayed
					graphics.update();
				}
			}
		}
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
			final SwingWorker caWorker = new SwingWorker()
			{
				public Object construct()
				{
					// draw as a right click if (1) is a right click, or (2)
					// ctrl is pressed
					changeColor(xPos, yPos, true, rightClicked
							|| CAFrame.controlKeyDown);

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
	 * Does nothing.
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
			changeColor(event, false, rightClicked);
		}
	}

	/**
	 * Stops any movement from coloring cells.
	 */
	public void mouseReleased(MouseEvent event)
	{
		buttonPressed = false;
	}
}
