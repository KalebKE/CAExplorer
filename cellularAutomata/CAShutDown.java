/*
 * CAShutDown -- a class within the Cellular Automaton Explorer. Copyright (C)
 * 2005 David B. Bahr (http://academic.regis.edu/dbahr/) This program is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version. This program
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package cellularAutomata;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import cellularAutomata.graphics.CAFrame;
import cellularAutomata.movie.MovieMaker;
import cellularAutomata.reflection.URLResource;
import cellularAutomata.util.graphics.ShrinkingJFrame;

/**
 * Handles shutdown operations such as saving properties to a file and cleaning
 * up resources.
 * 
 * @author David Bahr
 */
public class CAShutDown
{
	/**
	 * Saves properties to a file, cleans up resources, and exits the program.
	 * 
	 * @param frame
	 *            The frame containing all he CA graphics.
	 */
	public static void exit(CAFrame frame)
	{
		// get the CA icon image URL (searches the classpath to find the image
		// file).
		URL caIconUrl = URLResource.getResource("/"
				+ CAConstants.APPLICATION_ICON_IMAGE_PATH);
		ImageIcon icon = null;
		if(caIconUrl != null)
		{
			icon = new ImageIcon(caIconUrl);
		}

		// Say Quit for Macs and Exit for everyone else
		String exit = "Exit";
		if(CAConstants.MAC_OS)
		{
			exit = "Quit";
		}

		// make the JFrame look disabled
		if(frame != null)
		{
			frame.setViewDisabled(true);
		}

		// ask for confirmation
		String message = exit + " program?  Any unsaved data will be lost.";
		int answer = JOptionPane.NO_OPTION;
		if(icon != null)
		{
			answer = JOptionPane.showConfirmDialog(frame.getFrame(), message,
					exit, JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, icon);
		}
		else
		{
			answer = JOptionPane.showConfirmDialog(frame.getFrame(), message,
					exit, JOptionPane.YES_NO_CANCEL_OPTION);
		}

		if(answer == JOptionPane.YES_OPTION)
		{
			// stop the simulation
			frame.getControlPanel().getStartPanel().getStopButton().doClick();

			// stop the movie, if it is being created
			if(MovieMaker.isOpen())
			{
				MovieMaker.closeMovie();
			}

			// save the CA properties to a file
			saveProperties(CurrentProperties.getInstance().getProperties());

			// stop all analyses (this cannot be called by the CAController,
			// because this exit method may be called by some other class
			// such as the CAFrameListener).
			CAController.stopAllAnalyses();

			// fade away and exit the application
			if(frame != null)
			{
				frame.setViewDisabled(false);
			}

			// make the animation for closing the frame take this long (in
			// milliseconds)
			((ShrinkingJFrame) frame.getFrame()).setAnimationLength(300);

			// exit the application
			if(CAConstants.WINDOWS_XP_OS)
			{
				// Windows XP has a lame closing, so this is an attempt to
				// make it look better. System.exit(0) is called in this method
				((ShrinkingJFrame) frame.getFrame()).shrinkAndExitApplication();
			}
			else
			{
				// macs and linux look ok when closing a window, so no need to
				// use the shrinking animation
				System.exit(0);
			}
		}
		else
		{
			// make the JFrame look enabled again
			if(frame != null)
			{
				frame.setViewDisabled(false);
			}
		}
	}

	/**
	 * Saves the properties to a file.
	 * 
	 * @param properties
	 *            The CA properties that will be saved.
	 */
	private static void saveProperties(Properties properties)
	{
		try
		{
			// The location of the properties file.
			String filePath = CAConstants.DEFAULT_PROPERTIES_FILE;

			FileOutputStream outputStream = new FileOutputStream(filePath);

			String header = "The following properties dictate the start up "
					+ "conditions for the "
					+ CAConstants.PROGRAM_TITLE
					+ ". These properties may be edited by hand, but that is not "
					+ "recommended. Should this file become corrupted (by "
					+ "improper editing), please delete the file. The "
					+ "Explorer will recreate the file.";
			properties.store(outputStream, header);
		}
		catch(IOException e)
		{
			System.out.println("Class: CAShutDown. "
					+ "Method: saveProperties. Could not open or create the "
					+ "properties file.");
		}
		catch(ClassCastException e)
		{
			// warning for developer
			String warning = "All properties must be strings.  You have "
					+ "created a property \n"
					+ "key or value that is not a string.";

			JOptionPane.showMessageDialog(null, warning, "Developer Warning",
					JOptionPane.WARNING_MESSAGE);
		}
		catch(Exception e)
		{
			System.out.println("Class: CAShutDown. "
					+ "Method: saveProperties. Could not save the "
					+ "properties file.");
		}
	}
}
