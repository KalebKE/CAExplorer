/*
 CellularAutomaton -- a class within the Cellular Automaton Explorer. 
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

import javax.swing.JOptionPane;

import cellularAutomata.CAConstants;
import cellularAutomata.CAController;
import cellularAutomata.CASplash;
import cellularAutomata.error.ErrorHandler;
import cellularAutomata.mac.MacOSHandler;

/**
 * The primary class for this application. Calculates and displays results for a
 * cellular automaton.
 * 
 * @author David Bahr
 */
public class CellularAutomaton
{
	/**
	 * This is where the application starts. The main just bootstraps and calls
	 * another method run(). This makes it easy to alter this code to execute as
	 * an applet, servlet, or to write a Facade or other wrapper application for
	 * this class. Just write the applet or servlet and then call the run()
	 * method. Nothing necessary happens inside the main.
	 */
	public static void main(String[] args)
	{
		CellularAutomaton ca = new CellularAutomaton();
		ca.run();
	}

	/**
	 * This runs the cellular automaton.
	 */
	public void run()
	{
		try
		{
			// Set OS specific properties. This has to happen first thing
			// in the program (especially before any GUI components)
			setOSSpecificProperties();

			// Test the version of java to make sure program will run properly.
			// Warn the user if they need a newer version of Java.
			testJavaVersion();

			// Set "constants" used by the program. (These are OS specific, so
			// not really constant.)
			CAConstants.setConstants();

			// Display splash during start up. Needs constants from above.
			new CASplash();

			// Take care of OS specific code (e.g., creating Quit and About
			// menus that Mac users expect). Should happen early in the program.
			setOSSpecificFeatures();

			// MAIN PART OF THE CODE!
			// Initializes and runs everything (lattice, cells, graphics, etc.).
			//
			// After creating the controller, everything is controlled from the
			// graphics. That's it! ;-)
			//
			// Ok, ok... To see those graphics controlling everything, try
			// looking inside the CAController class.
			CAController.getInstanceOfCAController();
		}
		catch(Throwable programError)
		{
			// Awwww, crud.
			// Gently end the program with a pleasant warning (unless a
			// developer is working,in which case the error is
			// rethrown so they can see everything that went wrong).
			ErrorHandler.endProgramNicely(programError);
		}
	}

	/**
	 * Handles features that specific OS require (for example, Macs have a
	 * specific way of dealing with Quit and About menus that Mac users will
	 * expect to see).
	 */
	private void setOSSpecificFeatures()
	{
		// deal with the mac About and Quit menu items
		MacOSHandler.setMacOSXSpecificFeatures();
	}

	/**
	 * Set system properties specific to the operating system. This MUST be
	 * called before anything else, particularly before any GUI components.
	 */
	private void setOSSpecificProperties()
	{
		// set system properties for Macs -- doesn't hurt other OS's to have
		// these properties set.
		MacOSHandler.setMacSpecificSystemProperties();
	}

	/**
	 * Tests the version of java being run by the user. If not sufficient, warns
	 * the user.
	 */
	private void testJavaVersion()
	{
		try
		{
			String javaVersion = System.getProperty("java.version");

			// get the position of the dots
			int firstDot = javaVersion.indexOf(".");
			int secondDot = javaVersion.indexOf(".", firstDot + 1);

			// convert the java version into numbers. Will be something like
			// 1.5, 1.6, 2.11 or more generally "version.release".
			int version = Integer.parseInt(javaVersion.substring(0, firstDot));
			int release = Integer.parseInt(javaVersion.substring(firstDot + 1,
					secondDot));

			// check version compatibility
			if(version < CAConstants.COMPATIBLE_JAVA_VERSION
					|| ((version == CAConstants.COMPATIBLE_JAVA_VERSION) && (release < CAConstants.COMPATIBLE_JAVA_RELEASE)))
			{
				String message = "Sorry, the Cellular Automaton Explorer requires Java \n"
						+ "version "
						+ CAConstants.COMPATIBLE_JAVA_VERSION
						+ "."
						+ CAConstants.COMPATIBLE_JAVA_RELEASE
						+ " or a later version.  You have version "
						+ version
						+ "."
						+ release
						+ "\n"
						+ "of the Java runtime environment.  Some features \n"
						+ "may not run properly.\n\n"
						+ "Visit http://java.com to get the latest version of java.";

				JOptionPane.showMessageDialog(null, message,
						"Java version incompatibility",
						JOptionPane.WARNING_MESSAGE);
			}
		}
		catch(Error e)
		{
			// If failed, then there's nothing we can do, so ignore and
			// continue. The user just doesn't get a version check -- no biggee
			// if they followed installation instructions.
		}
	}
}
