/*
 ObjectState -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.cellState.model;

import java.util.Random;

import javax.swing.JOptionPane;

import cellularAutomata.CAController;
import cellularAutomata.CurrentProperties;
import cellularAutomata.graphics.InitialStatesPanel;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.rules.templates.ObjectRuleTemplate;
import cellularAutomata.util.math.RandomSingleton;

/**
 * A cell state for a cellular automaton with arbitrary objects as states.
 * 
 * @author David Bahr
 */
public class ObjectState extends CellState
{
	// random generator
	private static Random random = RandomSingleton.getInstance();

	// the empty state
	private Object emptyState = new Object();

	// The full state
	private Object fullState = new Object();

	// The full state
	private Object alternateState = new Object();

	// for speed we place this here.
	private Object state = new Object();

	/**
	 * Sets an Object as the initial state.
	 * 
	 * @param state
	 *            Any arbitrary Object to be used as the state.
	 * @param alternateState
	 *            The state that is an alternate to the full state (and drawn by
	 *            a right mouse click on the grid). Most commonly is set to the
	 *            same Object as the fullState.
	 * @param emptyState
	 *            The state corresponding to a "blank".
	 * @param fullState
	 *            The state corresponding to a filled cell.
	 */
	public ObjectState(Object state, Object alternateState, Object emptyState,
			Object fullState)
	{
		if(checkState(state) && checkState(emptyState) && checkState(fullState))
		{
			// save here for fast access
			this.state = state;
			this.alternateState = alternateState;
			this.emptyState = emptyState;
			this.fullState = fullState;

			// set state using the method from the super class
			setValue(state);
		}
	}

	/**
	 * Check that the state is not null. If is null, throws an exception.
	 * 
	 * @param state
	 *            The state being checked.
	 * @return true if the state is ok.
	 * @throws IllegalArgumentException
	 *             if is null.
	 */
	private boolean checkState(Object state)
	{
		// check to be sure is not null.
		if(state == null)
		{
			throw new IllegalArgumentException(
					"Class: ObjectState. Method: checkState. The state is null, \n"
							+ "which is not allowed.");
		}

		// if get here then is ok
		return true;
	}

	/**
	 * Creates a SHALLOW copy of this cellState; this method must return a
	 * different instance of the cell state, but with all the same values.
	 * <p>
	 * The intent is that, for any CellState x, the expression:
	 * <code> x.clone() !=  x </code> will be true, and that the expression:
	 * <code> x.clone().getClass() == x.getClass() </code> will be true. Also:
	 * <code> x.clone().equals(x) </code> will be true.
	 * <p>
	 * (Note this method is used in places where we need a copy of the cell's
	 * state but that the same instance would cause unpredictable or incorrect
	 * behavior.)
	 * 
	 * @return A unique copy of the cell's state (must not return "this"
	 *         object).
	 */
	public CellState clone()
	{
		// note, can only create a shallow copy. Oh well.
		return new ObjectState(state, alternateState, emptyState, fullState);
	}

	/**
	 * Tests if the value of two cell states are equal.
	 * 
	 * @return true if the cell states have the same value.
	 */
	public boolean equals(CellState state)
	{
		return this.toString().equals(state.toString());
	}

	/**
	 * The state of the cell.
	 * 
	 * @return The state.
	 */
	public Object getState()
	{
		return state;
	}

	/**
	 * Tests if a given CellState is "alternate" by using the equals method to
	 * compare the state to the alternateState.
	 * 
	 * @return true if the state is alternate.
	 * @see cellularAutomata.cellState.model.CellState#setToAlternateState()
	 */
	public boolean isAlternate()
	{
		return state.equals(alternateState);
	}

	/**
	 * Tests if a given CellState is "empty".
	 * 
	 * @return true if the state is empty.
	 * @see cellularAutomata.cellState.model.IntegerCellState#setToEmptyState()
	 */
	public boolean isEmpty()
	{
		return state.equals(emptyState);
	}

	/**
	 * Tests if a given CellState is "full".
	 * 
	 * @return true if the state is full.
	 * @see cellularAutomata.cellState.model.IntegerCellState#setToFullState()
	 */
	public boolean isFull()
	{
		return state.equals(fullState);
	}

	/**
	 * Sets this cell state to the "alternate" state.
	 */
	public void setToAlternateState()
	{
		super.setValue(alternateState);
		state = alternateState;
	}

	/**
	 * Sets this cell state to the "empty" state.
	 */
	public void setToEmptyState()
	{
		super.setValue(emptyState);
		state = emptyState;
	}

	/**
	 * Sets this cell state to the "full" state.
	 */
	public void setToFullState()
	{
		super.setValue(fullState);
		state = fullState;
	}

	/**
	 * Randomly selects a full or empty state for this cell state.
	 * 
	 * @param probability
	 *            The probability that the cell will be occupied rather than
	 *            blank.
	 */
	public void setToRandomState(double probability)
	{
		if(random.nextDouble() < probability)
		{
			super.setValue(fullState);
			state = fullState;
		}
		else
		{
			super.setValue(emptyState);
			state = emptyState;
		}
	}

	/**
	 * The state of the cell.
	 * 
	 * @param state
	 *            The state, which may be any Object.
	 */
	public void setState(Object state)
	{
		// throw exception if not a valid value
		if(checkState(state))
		{
			super.setValue(state);

			this.state = state;
		}
	}

	/**
	 * Sets a String value for the cell's state. The user may wish to override
	 * this method in a child class.
	 */
	public void setStateFromString(String state)
	{
		// note that this method eventually calls super.setValue(Object) (via
		// the setState() method) as required by the contract with this abstract
		// method.
		if(emptyState.toString().equals(state))
		{
			setState(emptyState);
		}
		else if(fullState.toString().equals(state))
		{
			setState(fullState);
		}
		else if(alternateState.toString().equals(state))
		{
			setState(alternateState);
		}
		else
		{
			// if it is built from the ObjectRuleTemplate, then the method
			// createStateFromString() exists. It tells us how to convert the
			// string to an object.
			try
			{
				// find out what rule we are using
				String ruleClassName = CurrentProperties.getInstance()
						.getRuleClassName();

				// get the rule -- note that if the rule is not extending the
				// ObjectRuleTemplate, then this cast will fail
				ObjectRuleTemplate rule = (ObjectRuleTemplate) ReflectionTool
						.instantiateMinimalRuleFromClassName(ruleClassName);

				// get the state from the string
				Object newState = rule.createStateFromString(state);

				// make sure this method returns a non-null state of the correct
				// type. Should maybe also check if
				// !rule.getFullState().getClass().equals(newState.getClass())???
				if(newState == null)
				{
					throw new Exception();
				}

				// set the state
				setState(newState);
			}
			catch(Exception e)
			{
				// could not cast to an ObjectRuleTemplate or the state was
				// null. So we use a default empty state.
				setState(emptyState);

				// Not possible in general to make the string to object
				// conversion because we don't know in advance what kind of
				// object will be used and whether or not it can be converted
				// from a String. Don't warn if we are in the EZ facade mode.
				if(!havePrintedImportDataWarning
						&& !CurrentProperties.getInstance().isFacadeOn())
				{
					havePrintedImportDataWarning = true;

					// make the JFrame look disabled
					if(CAController.getCAFrame() != null)
					{
						CAController.getCAFrame().setViewDisabled(true);
					}

					// warn the user
					String message = "Please check the "
							+ InitialStatesPanel.INIT_STATES_TAB_TITLE
							+ " tab. \n\n"
							+ "You are importing data from a file. \n\n"
							+ "The current rule is based on generic objects rather than "
							+ "numbers. \n"
							+ "Therefore, it cannot always import numerical images or data. \n"
							+ "When the conversion is not possible, the cell states will be set \n"
							+ "to a blank state instead.\n\n"
							+ "You may resubmit with a different initial state if you prefer.";
					JOptionPane.showMessageDialog(CAController.getCAFrame()
							.getFrame(), message, "Import file warning",
							JOptionPane.WARNING_MESSAGE);

					// make the JFrame look enabled
					if(CAController.getCAFrame() != null)
					{
						CAController.getCAFrame().setViewDisabled(false);
					}
				}
			}
		}
	}

	/**
	 * Sets a value for this cell state. Overrides the parent class
	 * implementation so that we can save the state locally and convert to an
	 * array.
	 * 
	 * @param state
	 *            The new state of the cell.
	 */
	public void setValue(Object state)
	{
		if(checkState(state))
		{
			super.setValue(state);

			this.state = state;
		}
	}

	/**
	 * The state as an integer.
	 * 
	 * @return The hashcode of the state.
	 */
	public int toInt()
	{
		return state.hashCode();
	}
}
