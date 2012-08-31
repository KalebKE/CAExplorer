/*
 CellValueListener -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.CurrentProperties;
import cellularAutomata.Cell;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.ComplexState;
import cellularAutomata.graphics.StatusPanel;
import cellularAutomata.graphics.AllPanelController;
import cellularAutomata.lattice.view.LatticeView;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.rules.Rule;
import cellularAutomata.rules.templates.FiniteObjectRuleTemplate;
import cellularAutomata.util.math.Complex;

/**
 * A mouse listener that gets the value of the cell under the cursor.
 * 
 * @author David Bahr
 */
public class CellValueListener extends LatticeMouseListener
{
	// the number of decimal places shown when displaying complex numbers
	private final static int NUMBER_OF_COMPLEX_DECIMAL_PLACES = 2;

	// indicates if the mouse is inside the component
	private boolean insideComponent = true;

	// true if the rule uses and returns complex numbers
	private boolean complexNumberRule = false;

	// null, unless it is a rule of this type
	private FiniteObjectRuleTemplate finiteObjectRule = null;

	// the last event that occurred
	private MouseEvent lastEvent = null;

	// the status panel that displays info about the current simulation.
	private StatusPanel statusPanel = null;

	/**
	 * Create a listener for the mouse that will get the value of the cell under
	 * the cursor.
	 * 
	 * @param graphics
	 *            A graphics panel with an update that can be called by this
	 *            listener.
	 * @param statusPanel
	 *            The panel that displays info about the current simulation
	 *            (like running, stopped, lattice size, etcetera).
	 */
	public CellValueListener(LatticeView graphics, StatusPanel statusPanel)
	{
		super(graphics);
		this.statusPanel = statusPanel;

		// find out what kind of rule we have
		String classNameOfRule = CurrentProperties.getInstance()
				.getRuleClassName();
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(classNameOfRule);

		try
		{
			// check if this is a finite object template rule -- not the best
			// connectivity, but useful for backwards compatibility
			finiteObjectRule = (FiniteObjectRuleTemplate) rule;
		}
		catch(Exception e)
		{
			// do nothing
		}

		try
		{
			// check if this is a complex number based rule -- not the best
			// connectivity, but useful for special printing needs
			ComplexState test = (ComplexState) rule.getCompatibleCellState();

			complexNumberRule = true;
		}
		catch(Exception e)
		{
			complexNumberRule = false;
		}

	}

	/**
	 * Handles the mouse event by getting the value of the cell under the
	 * cursor. (This method will not get the cell value if the graphics are only
	 * being updated at the end of the simulation.)
	 * 
	 * @param event
	 *            The mouseMoved event that called this method.
	 */
	private void getValue(MouseEvent event)
	{
		getValue(event, false);
	}

	/**
	 * Handles the mouse event by getting the value of the cell under the
	 * cursor.
	 * 
	 * @param event
	 *            The mouseMoved event that called this method.
	 * @param forceUpdate
	 *            Will force the cell value to be updated even if the graphics
	 *            are only being updated at the end of the simulation.
	 */
	private void getValue(MouseEvent event, boolean forceUpdate)
	{
		String cellValue = "";

		// should not get the cell value if the graphics are "updating at the
		// end", and the simulation is running.
		boolean updateAtEnd = false;
		boolean running = false;
		if(statusPanel.getStatusLabel().equals(
				AllPanelController.RUNNING_MESSAGE))
		{
			running = true;

			// don't need to bother with checking this unless the simulation is
			// running
			updateAtEnd = CurrentProperties.getInstance().isUpdateAtEnd();
		}

		if(running && updateAtEnd)
		{
			// let the user know that they can't see the cell value
			cellValue = "not done";
		}

		// keep the last event that occurred. Useful when this method is called
		// by something other than the mouse.
		lastEvent = event;

		if(insideComponent && (event != null) && !(updateAtEnd && running))
		{
			Cell cell = super.getCellUnderCursor(event);

			// Do this so all lattices behave the same outside their range. the
			// hexagonal and triangular lattices are null outside their
			// range, but the square lattices are not.
			if(cell != null)
			{
				int generation = super.getGenerationUnderCursor(event);

				// get the value that will be displayed
				CellState cellState = cell.getState(generation);

				if(cellState != null)
				{
					cellValue = cellState.toString();

					// handle rules with Complex numbers (they need a "pretty
					// string")
					if(complexNumberRule)
					{
						cellValue = ((Complex) ((ComplexState) cellState)
								.getValue())
								.toPrettyString(NUMBER_OF_COMPLEX_DECIMAL_PLACES);
					}

					// handle rules that are finite object templates
					if(finiteObjectRule != null)
					{
						// get the value that will be displayed
						cellValue = finiteObjectRule.intToObjectState(
								cell.toInt(generation)).toString();
					}
				}
			}
		}

		// set the label on the status panel
		statusPanel.setCurrentCursorValueLabel(cellValue);
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
			getValue(event);
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
		getValue(event);
	}

	/**
	 * Gets the position of the cursor.
	 */
	public void mouseMoved(MouseEvent event)
	{
		if(insideComponent)
		{
			getValue(event);
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

	/**
	 * Forces this listener to update the status panel display by using whatever
	 * mouse event last occured.
	 */
	public void updateValue()
	{
		getValue(lastEvent, true);
	}
}
