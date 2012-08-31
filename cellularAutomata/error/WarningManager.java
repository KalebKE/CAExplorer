/*
 WarningManager -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2008  David B. Bahr (http://academic.regis.edu/dbahr/)

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

package cellularAutomata.error;

import java.awt.Component;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import cellularAutomata.CAController;

/**
 * This class limits the number of warnings that are displayed by rules or any
 * other class. This is necessary because some classes are instantiated multiple
 * times and might try to throw the same warning many times. For example, each
 * rule can be instantiated multiple times (once per cell) and this class helps
 * ensure that the warnings are not unnecessarily repeated.
 * <p>
 * All warnings are suppressed while the application opens (during the start-up
 * procedure).
 * 
 * @author David Bahr
 */
public class WarningManager
{
	// keeps track of how many times a warning has been shown
	private static Hashtable<String, Integer> warningHash = new Hashtable<String, Integer>();

	// keeps track of their previous response to a confirmation dialogue warning
	private static Hashtable<String, Integer> confirmationResponseHash = new Hashtable<String, Integer>();

	/**
	 * If the specified warning has been displayed fewer than the specified
	 * number of times, then this indicates that the given warning should be
	 * displayed. When counting the number of times that a warning has been
	 * displayed, this method only knows about displayed warnings that have been
	 * queried through this method or one of the displayWarning methods. Each
	 * query to this method (and the displayWarning methods) increments the
	 * number of times that the warning has been displayed.
	 * <p>
	 * All warnings are suppressed while the application opens (during the
	 * start-up procedure).
	 * 
	 * @param warningMessage
	 *            The warning that will be displayed.
	 * @param numberOfTimes
	 *            The number of times that the warning will be displayed.
	 * @return true if the warning should be displayed.
	 */
	public synchronized static boolean showWarning(String warningMessage,
			int numberOfTimes)
	{
		boolean showWarning = false;

		// number of times displayed before this time
		Integer numberOfTimesPreviouslyDisplayed = warningHash
				.get(warningMessage);

		// number of times displayed, if displayed this time
		int incrementedValue = 1;

		if(numberOfTimesPreviouslyDisplayed != null)
		{
			incrementedValue = numberOfTimesPreviouslyDisplayed.intValue() + 1;
		}

		// have we exceeded the allowed number of warnings?
		if(incrementedValue <= numberOfTimes)
		{
			// is this call being made during the start up or after the start
			// up?
			if(CAController.doneStartingTheApplication)
			{
				showWarning = true;

				// and update the number of times it has been displayed
				Integer numTimes = new Integer(incrementedValue);
				warningHash.put(warningMessage, numTimes);
			}
			else
			{
				// reset to null the number of times it has been displayed. We
				// don't want to count errors during the startup
				warningHash.remove(warningMessage);
			}
		}

		return showWarning;
	}

	/**
	 * Displays a confirmation dialog only if the specified warning has not
	 * already appeared the specified number of times. When counting the number
	 * of times that a warning has been displayed, this method only knows about
	 * displayed warnings that have been queried through this method, the show
	 * warning method, or one of the other displayWarning methods.
	 * 
	 * @param warningMessage
	 *            The warning that will be displayed.
	 * @param numberOfTimes
	 *            The number of times that the warning will be displayed.
	 * @param parentComponent
	 *            Parent of the displayed JOptionPane. May be null.
	 * @param title
	 *            Title of the JOptionPane.
	 * @param yesNoCancelOrOtherResponseOption
	 *            The type of response options, for example
	 *            JOptionPane.YES_NO_CANCEL_OPTION.
	 * @param messageType
	 *            The type of display message, for example
	 *            JOptionPane.WARNING_MESSAGE.
	 * @param icon
	 *            An icon to be displayed in the JOptionPane. May be null.
	 * @return returns the response to the confirmation dialog (for example,
	 *         JOptionPane.YES_OPTION); or -1 if the warning is not displayed
	 *         and has never been displayed; or returns the previous response if
	 *         the warning is not displayed, but the user has responded to this
	 *         warning in the past.
	 */
	public synchronized static int displayWarningWithConfirmDialog(String warningMessage,
			int numberOfTimes, Component parentComponent, String title,
			int yesNoCancelOrOtherResponseOption, int messageType, Icon icon)
	{
		// get the previous answer in case the message isn't displayed this
		// time.
		int answer = -1;
		Integer previousResponse = confirmationResponseHash.get(warningMessage);
		if(previousResponse != null)
		{
			answer = previousResponse.intValue();
		}

		if(showWarning(warningMessage, numberOfTimes))
		{
			// make the JFrame look disabled
			if(CAController.getCAFrame() != null)
			{
				CAController.getCAFrame().setViewDisabled(true);
			}

			answer = JOptionPane.showConfirmDialog(parentComponent,
					warningMessage, title, yesNoCancelOrOtherResponseOption,
					messageType, icon);

			// make the JFrame look enabled
			if(CAController.getCAFrame() != null)
			{
				CAController.getCAFrame().setViewDisabled(false);
			}

			// and save the response
			confirmationResponseHash.put(warningMessage, new Integer(answer));
		}

		return answer;
	}

	/**
	 * Displays a confirmation dialog only if the specified warning has not
	 * already appeared the specified number of times. When counting the number
	 * of times that a warning has been displayed, this method only knows about
	 * displayed warnings that have been queried through this method, the show
	 * warning method, or one of the other displayWarning methods.
	 * <p>
	 * This method assigns the CAFrame as a parent component to the the warning
	 * message. The message will be a warning message.
	 * 
	 * @param warningMessage
	 *            The warning that will be displayed.
	 * @param numberOfTimes
	 *            The number of times that the warning will be displayed.
	 * @param title
	 *            Title of the JOptionPane.
	 * @return true if the warning is displayed, and false otherwise.
	 */
	public synchronized static boolean displayWarningWithMessageDialog(
			String warningMessage, int numberOfTimes, String title)
	{
		return displayWarningWithMessageDialog(warningMessage, numberOfTimes,
				CAController.getCAFrame().getFrame(), title,
				JOptionPane.WARNING_MESSAGE, null);
	}

	/**
	 * Displays a message dialog only if the specified warning has not already
	 * appeared the specified number of times. When counting the number of times
	 * that a warning has been displayed, this method only knows about displayed
	 * warnings that have been queried through this method, the show warning
	 * method, or one of the other displayWarning methods.
	 * 
	 * @param warningMessage
	 *            The warning that will be displayed.
	 * @param numberOfTimes
	 *            The number of times that the warning will be displayed.
	 * @param parentComponent
	 *            Parent of the displayed JOptionPane. May be null.
	 * @param title
	 *            Title of the JOptionPane.
	 * @param messageType
	 *            The type of display message, for example
	 *            JOptionPane.WARNING_MESSAGE.
	 * @param icon
	 *            An icon to be displayed in the JOptionPane. May be null.
	 * @return true if the warning is displayed, and false otherwise.
	 */
	public synchronized static boolean displayWarningWithMessageDialog(
			String warningMessage, int numberOfTimes,
			Component parentComponent, String title, int messageType, Icon icon)
	{
		boolean showedWarning = false;

		if(showWarning(warningMessage, numberOfTimes))
		{
			// make the JFrame look disabled
			if(CAController.getCAFrame() != null)
			{
				CAController.getCAFrame().setViewDisabled(true);
			}

			JOptionPane.showMessageDialog(parentComponent, warningMessage,
					title, messageType, icon);

			// make the JFrame look enabled
			if(CAController.getCAFrame() != null)
			{
				CAController.getCAFrame().setViewDisabled(false);
			}

			showedWarning = true;
		}

		return showedWarning;
	}
}
