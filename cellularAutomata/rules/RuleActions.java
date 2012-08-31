/*
 RuleActions -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.rules;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigInteger;
import java.util.EventListener;

import javax.swing.event.EventListenerList;

import cellularAutomata.CAController;
import cellularAutomata.CurrentProperties;
import cellularAutomata.graphics.CAMenuBar;

/**
 * This encompasses actions that a Rule might need, like forcing a refresh of
 * the graphics, pausing the CA, etc. These will be rarely used.
 * 
 * @author David Bahr
 */
public abstract class RuleActions
{
	// Allows us to add PropertyChangeListeners listeners to this class.
	// Must be static so when applied to one rule, works for all rules.
	private static EventListenerList listenerList = new EventListenerList();

	public RuleActions(boolean minimalOrLazyInitialization)
	{
		// the boolean is not currently used, but this constructor is
		// necessary for this class to live in the "rules" package.
	}

	/**
	 * Notify listeners of a property change. Call this method if you want other
	 * classes to know about a property change within your rule. Developers will
	 * rarely need this. It's primary use is as a utility for the
	 * refreshGraphics, stopCA, and other methods in this class.
	 * 
	 * @param event
	 *            Holds the changed property.
	 */
	protected void firePropertyChangeEvent(PropertyChangeEvent event)
	{
		EventListener[] listener = listenerList
				.getListeners(PropertyChangeListener.class);

		for(int i = 0; i < listener.length; i++)
		{
			((PropertyChangeListener) listener[i]).propertyChange(event);
		}
	}

	/**
	 * Adds a change listener. Developers will rarely need his.
	 * 
	 * @param listener
	 *            the listener to add.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		listenerList.add(PropertyChangeListener.class, listener);
	}

	/**
	 * Forces the CA graphics to clear all values. Developers will rarely need
	 * this.
	 */
	protected void clearGraphics()
	{
		// this sets a property that tells the CA it is time to clear the
		// graphics
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.CLEAR, CurrentProperties.FALSE,
				CurrentProperties.TRUE));
	}

	/**
	 * Forces the CA to pause (may be restarted later). Developers will rarely
	 * need this.
	 */
	protected void pauseCA()
	{
		// this sets a property that tells the CA it is time to pause the
		// simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));
	}

	/**
	 * Forces the CA graphics to be refreshed. Developers will rarely need this.
	 */
	protected void refreshGraphics()
	{
		// this sets a property that tells the CA it is time to refresh the
		// graphics
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.UPDATE_GRAPHICS, CurrentProperties.TRUE,
				CurrentProperties.FALSE));
	}

	/**
	 * Removes a change listener. Developers will rarely need this.
	 * 
	 * @param listener
	 *            the listener to remove.
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		listenerList.remove(PropertyChangeListener.class, listener);
	}

	/**
	 * Forces the CA to reset to its original parameters. Developers will rarely
	 * need this.
	 * <p>
	 * WARNING: may cause unexpected results when importing from a data source
	 * (like in the EZ facade mode).
	 */
	protected void resetCA()
	{
		// First, inform non-local classes that they need to
		// stop the simulation if it is running (i.e., notify anyone
		// who cares.)
		CAController.getInstanceOfCAController().stopCA();

		// now inform non-local classes that they need to
		// redo the setup (i.e., notify anyone who cares.)
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.SETUP, null, new Boolean(true)));
	}

	/**
	 * Forces the CA to restart (after it has been stopped). If the CA has not
	 * been previously stopped, then this will have no affect. Developers will
	 * rarely need this.
	 */
	protected void restartCA()
	{
		// this sets a property that tells the CA it is time to restart the
		// simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(false)));
	}

	/**
	 * Forces the CA to save it's current state as a ".ca" file. Note that this
	 * is an asynchronous call, so it is (remotely) possible that the CA might
	 * continue to update its state momentarily before saving. It is best to
	 * stop the CA before calling this method.
	 */
	protected void saveData()
	{
		// this sets a property that tells the CA it is time to save the data
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.SAVE_DATA, CurrentProperties.FALSE,
				CurrentProperties.TRUE));
	}

	/**
	 * Forces the CA to save it's current state as an image. This will pop up a
	 * save dialog. Note that this is an asynchronous call, so it is (remotely)
	 * possible that the CA might continue to update its state momentarily
	 * before saving. It is best to stop the CA before calling this method.
	 */
	protected void saveAsImage()
	{
		// this sets a property that tells the CA it is time to save the
		// graphics as an image
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CAMenuBar.SAVE_AS_IMAGE, CurrentProperties.FALSE,
				CurrentProperties.TRUE));
	}

	/**
	 * Sets the number of states and forces the number of states to be refreshed
	 * on the properties panel and status panel, but WARNING: calling this
	 * method from the constructor of a rule may cause an infinite loop and hang
	 * the program. (The initial states panel reacts to changes in the number of
	 * states, but to decide how to react it has to instantiate the rule. So if
	 * this method is called as part of the instantiation, it will create an
	 * infinite loop.) In general, A BETTER APPROACH is to use the
	 * stateValueToDisplay() method in IntegerRule (and in its subclasses like
	 * IntegerRuleTemplate and FiniteObjectRuleTemplate). stateValueToDisplay()
	 * sets the number of states on the properties panel display. Only call this
	 * method when the number of states will change "on the fly" while the rule
	 * is running.
	 * <p>
	 * Developers should NOT normally call this method.
	 * 
	 * @param numberOfStates
	 *            The number of states that will be set and displayed on the
	 *            properties panel.
	 * @param displayNameOfTheRule
	 *            The display name of the rule that is setting the number of
	 *            states.
	 * @param updateCurrentProperties
	 *            If true, the current properties will be updated along with the
	 *            graphics, but if false, only the graphics will be updated (and
	 *            the properties will not be changed).
	 */
	protected void setNumberOfStates(int numberOfStates,
			String displayNameOfTheRule, boolean updateCurrentProperties)
	{
		// only update the current properties if requested
		if(updateCurrentProperties)
		{
			CurrentProperties.getInstance().setNumStates(numberOfStates);
		}

		// the number of states
		Integer newValue = new Integer(numberOfStates);

		// this lets the property panel know it is time to update (see
		// AllPanelListener)
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.NUMBER_OF_STATES, displayNameOfTheRule,
				newValue));
	}

	/**
	 * Sets the random percent and forces the random percent to be refreshed on
	 * the initial state panel.
	 * 
	 * @param percent
	 *            The random percent that will be set and displayed on the
	 *            initial state panel.
	 * @param displayNameOfTheRule
	 *            The display name of the rule that is setting the random
	 *            percent.
	 * @param updateCurrentProperties
	 *            If true, the current properties will be updated along with the
	 *            graphics, but if false, only the graphics will be updated (and
	 *            the properties will not be changed).
	 */
	protected void setRandomPercent(int percent, String displayNameOfTheRule,
			boolean updateCurrentProperties)
	{
		// only update the current properties if requested
		if(updateCurrentProperties)
		{
			CurrentProperties.getInstance().setRandomPercent(percent);
		}

		// the random percent
		Integer newValue = new Integer(percent);

		// this lets the initial state panel know it is time to update (see
		// AllPanelListener)
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.RANDOM_PERCENT, displayNameOfTheRule,
				newValue));
	}

	/**
	 * Sets the rule number and forces the rule number to be refreshed on the
	 * properties panel and status panel.
	 * 
	 * @param ruleNumber
	 *            The rule number that will be set and displayed on the
	 *            properties panel. Rule numbers may be very large, so this is a
	 *            BigInteger (which may be arbitrarily large).
	 * @param displayNameOfTheRule
	 *            The display name of the rule that is setting the number of
	 *            states.
	 * @param updateCurrentProperties
	 *            If true, the current properties will be updated along with the
	 *            graphics, but if false, only the graphics will be updated (and
	 *            the properties will not be changed).
	 */
	protected void setRuleNumber(BigInteger ruleNumber,
			String displayNameOfTheRule, boolean updateCurrentProperties)
	{
		// only update the current properties if requested
		if(updateCurrentProperties)
		{
			CurrentProperties.getInstance().setRuleNumber(ruleNumber);
		}

		// this lets the rule panel know it is time to update (see
		// AllPanelListener)
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.RULE_NUMBER, displayNameOfTheRule, ruleNumber));
	}

	/**
	 * Forces the CA to reset everything to the values currently selected on the
	 * Properties panel. Effectively, this presses the Submit button on the
	 * Properties panel. Careful, this could really annoy the user and may have
	 * unintended consequences. Developers will rarely need this.
	 */
	protected void setupCA()
	{
		// this sets a property that tells the CA it is time to setup a new
		// simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.SETUP, null, new Boolean(true)));
	}

	/**
	 * Forces the CA to stop. Developers will rarely need this.
	 */
	protected void stopCA()
	{
		// this tells the CA it is time to pause the
		// simulation
		CAController.getInstanceOfCAController().stopCA();
	}
}
