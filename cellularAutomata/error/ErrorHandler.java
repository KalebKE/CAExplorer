/*
 ErrorHandler -- a class within the Cellular Automaton Explorer. 
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

import javax.swing.JOptionPane;

import cellularAutomata.CAConstants;
import cellularAutomata.CAController;

/**
 * A collection of methods for handling errors that happen while the CA is
 * running. Typically notifies the user and then ends the program.
 * 
 * @author David Bahr
 */
public class ErrorHandler
{
	/**
	 * Ends the program "nicely" for users that may not want details of the
	 * error. First says there has been a fatal error and then asks if the user
	 * would like more details about the error (before ending the program).
	 * <p>
	 * However, if the user is debugging (CAConstants.DEBUG = true), then the
	 * error is just rethrown and the program is not ended.
	 * 
	 * @param error
	 *            The fatal error that is making the application close.
	 */
	public static void endProgramNicely(Throwable error)
	{
		if(CAConstants.DEBUG)
		{
			// this throws a runtime exception so that whatever calls this
			// method doesn't have to recatch (or advertise) this rethrow.
			throw new RuntimeException(error.getMessage());
		}
		else
		{
			endProgramNicely(error.getMessage(), "Error");
		}
	}

	/**
	 * Ends the program "nicely" for users that may not want details of the
	 * error. First says there has been a fatal error and then asks if the user
	 * would like more details about the error (before ending the program).
	 * 
	 * @param detailMessage
	 *            A message detailing the error so the user will know why the
	 *            program is ending. This message is only made visible at the
	 *            request of the user.
	 * @param title
	 *            The title of the JOptionPane warning message.
	 */
	public static void endProgramNicely(String detailMessage, String title)
	{
		// not a developer, so be nice.
		String message = "Sorry, there has been an error. "
				+ "The application will close.";
		int answer = JOptionPane.showOptionDialog(null, message, "Error",
				JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null,
				new String[] {"Exit Application", "Show Error Details"}, null);

		if(answer == JOptionPane.NO_OPTION)
		{
			// user wants details about the error
			if((detailMessage == null) || detailMessage.equals(""))
			{
				detailMessage = "Sorry, no details are available.";
			}

			// show them the detailed error message
			JOptionPane.showOptionDialog(null, detailMessage, "Error",
					JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null,
					new String[] {"Exit Application"}, null);
		}

		// all done, so clean up resources
		System.exit(0);
	}

	/**
	 * Ends the program after warning the user.
	 * 
	 * @param warningMessage
	 *            A warning message to the user telling them why the program
	 *            will end.
	 * @param title
	 *            The title of the JOptionPane warning message.
	 */
	public static void endProgramWithWarning(String warningMessage, String title)
	{
		// warn the user
		WarningManager
				.displayWarningWithMessageDialog(warningMessage, 1, title);

		// And then end the program (can't use the ShrinkingJFrame animation to
		// shrink the frame because it spawns a new thread, and the original
		// error could do more damage in the original thread).
		System.exit(0);
	}

	/**
	 * Warns the user of an error, and then ends the program unless the user is
	 * debugging (CAConstants.DEBUG = true), in which case the program
	 * continues.
	 * 
	 * @param t
	 *            The error that caused the program to quit. If the user is
	 *            debugging then this is rethrown.
	 * @param warningMessage
	 *            A warning message to the user telling them why the program
	 *            will end.
	 * @param title
	 *            The title of the JOptionPane warning message.
	 */
	public static void endProgramWithWarningUnlessDebugging(Throwable t,
			String warningMessage, String title)
	{
		// warn the user
		WarningManager
				.displayWarningWithMessageDialog(warningMessage, 1, title);

		// And then end the program unless the user is debugging. Rethrow the
		// error if debugging.
		if(CAConstants.DEBUG)
		{
			throw new RuntimeException(t.getMessage() + "\n" + warningMessage);
		}
		else
		{
			System.exit(0);
		}
	}

	/**
	 * Stops the simulation after warning the user, but does not end the
	 * program. Note that it may take some time for the simulation to end
	 * because it will not stop until all cells in a generation have been
	 * updated.
	 * 
	 * @param warningMessage
	 *            A warning message to the user telling them why the simulation
	 *            will be stopped.
	 * @param title
	 *            The title of the JOptionPane warning message.
	 */
	public static void stopSimulationWithWarning(String warningMessage,
			String title)
	{
		// warn the user
		WarningManager
				.displayWarningWithMessageDialog(warningMessage, 1, title);

		// and then stop the current simulation (but don't end the program)
		CAController.getInstanceOfCAController().stopCA();
	}
}
