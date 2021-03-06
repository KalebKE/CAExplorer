/*
MovieMaker -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.movie;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import cellularAutomata.CAController;
import cellularAutomata.error.WarningManager;
import cellularAutomata.graphics.CAMenuBar;
import cellularAutomata.reflection.URLResource;

/**
 * Convenience methods for making a movie from images generated by the CA
 * Explorer. With this class, only one movie can be made at a time. A facade for
 * the QuickTimeOutputStream.
 * 
 * @author David Bahr
 */
public class MovieMaker
{
	// the movie output stream
	private static QuickTimeOutputStream out = null;

	// display panel for getting the frame rate
	private static FrameRatePanel frameRatePanel = new FrameRatePanel();

	/**
	 * A 32 by 32 pixel icon that looks like a movie.
	 */
	public static ImageIcon movieIcon32by32 = null;

	/**
	 * A 24 by 24 pixel icon that looks like a movie.
	 */
	public static ImageIcon movieIcon24by24 = null;

	/**
	 * A 24 by 24 pixel icon that looks like a movie being cut.
	 */
	public static ImageIcon cutMovieIcon24by24 = null;

	/**
	 * A 16 by 16 pixel icon that looks like a movie being cut.
	 */
	public static ImageIcon cutMovieIcon16by16 = null;

	static
	{
		// instantiate the 32 by 32 icon
		URL movieIcon32by32Url = URLResource
				.getResource("/images/movieGray32by32.gif");
		if(movieIcon32by32Url != null)
		{
			movieIcon32by32 = new ImageIcon(movieIcon32by32Url);
		}

		// instantiate the 24 by 24 icon
		URL movieIcon24by24Url = URLResource
				.getResource("/images/movieGray24by24.gif");
		if(movieIcon24by24Url != null)
		{
			movieIcon24by24 = new ImageIcon(movieIcon24by24Url);
		}

		// instantiate the 24 by 24 icon
		URL cutMovieIcon24by24Url = URLResource
				.getResource("/images/cutMovie24by24.gif");
		if(cutMovieIcon24by24Url != null)
		{
			cutMovieIcon24by24 = new ImageIcon(cutMovieIcon24by24Url);
		}

		// instantiate the 24 by 24 icon
		URL cutMovieIcon16by16Url = URLResource
				.getResource("/images/cutMovie16by16.gif");
		if(cutMovieIcon16by16Url != null)
		{
			cutMovieIcon16by16 = new ImageIcon(cutMovieIcon16by16Url);
		}
	}

	/**
	 * Closes the movie (output stream) when done creating the movie.
	 */
	public static void closeMovie()
	{
		try
		{
			out.close();
			out = null;

			// let the user know that the movie is done
			String message = "The movie is finished and has been saved.";
			WarningManager.displayWarningWithMessageDialog(message, 10000,
					CAController.getCAFrame().getFrame(), "Movie Finished",
					JOptionPane.INFORMATION_MESSAGE, movieIcon32by32);
		}
		catch(Exception e)
		{
			// do nothing
		}
	}

	/**
	 * Displays a pane that lets the user know the movie is starting.
	 */
	private static void displayMovieStartingPane()
	{
		String message1 = "<html><body>A QuickTime movie is now being made. Every "
				+ "time <BR>"
				+ "increment will add a frame to the movie. <BR><BR></BODY></HTML>";
		JLabel label1 = new JLabel(message1);

		String message2 = "<html><body>To end the movie press the \"CUT\" button."
				+ "<BR><BR></BODY></HTML>";
		JLabel label2 = new JLabel(message2, MovieMaker.cutMovieIcon16by16,
				JLabel.LEFT);
		label2.setHorizontalTextPosition(JLabel.LEFT);
		label2.setVerticalTextPosition(JLabel.TOP);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(label1, BorderLayout.NORTH);
		panel.add(label2, BorderLayout.SOUTH);

		// make the JFrame look disabled
		if(CAController.getCAFrame() != null)
		{
			CAController.getCAFrame().setViewDisabled(true);
		}

		JOptionPane.showMessageDialog(CAController.getCAFrame().getFrame(),
				panel, "Movie Starting", JOptionPane.INFORMATION_MESSAGE,
				movieIcon32by32);

		// make the JFrame look enabled
		if(CAController.getCAFrame() != null)
		{
			CAController.getCAFrame().setViewDisabled(false);
		}
	}

	/**
	 * Finds out if movie making is in progress (open), or if it is done
	 * (closed). With this class, only one movie can be created at a time.
	 */
	public static boolean isOpen()
	{
		boolean open = false;
		if(out != null)
		{
			open = true;
		}

		return open;
	}

	/**
	 * A convenience method for creating a Quicktime movie. After creating the
	 * movie with this method, add the movie one frame at a time by calling
	 * writeFrame(img) where img is a BufferedImage. This method uses a default
	 * frame rate of 30fps.
	 * 
	 * @param file
	 *            The output file name.
	 * @throws IOException
	 *             If could not create the specified output file.
	 */
	public static void makeMovieWithDefaultFrameRate(File file)
			throws IOException
	{
		// use a default frane rate of 30 fps.
		makeMovie(file, 30);
	}

	/**
	 * A convenience method for creating a Quicktime movie. After creating the
	 * movie with this method, add the movie one frame at a time by calling
	 * writeFrame(img) where img is a BufferedImage. This method pops up a
	 * window that asks the user for the frame rate.
	 * 
	 * @param file
	 *            The output file name.
	 * @throws IOException
	 *             If could not create the specified output file.
	 */
	public static void makeMovie(File file) throws IOException
	{
		String title = "Select frame rate";

		Object[] button = new Object[] {"OK"};

		JOptionPane.showOptionDialog(CAController.getCAFrame().getFrame(),
				frameRatePanel, title, JOptionPane.DEFAULT_OPTION,
				JOptionPane.PLAIN_MESSAGE, movieIcon32by32, button, button[0]);

		// get the frame rate from the user
		makeMovie(file, frameRatePanel.getFrameRate());
	}

	/**
	 * A convenience method for creating a Quicktime movie. After creating the
	 * movie with this method, add the movie one frame at a time by calling
	 * writeFrame(img) where img is a BufferedImage.
	 * 
	 * @param file
	 *            The output file name.
	 * @param frameRate
	 *            The number of frames per second (30 or 60 are good defaults).
	 * @throws IOException
	 *             If could not create the specified output file.
	 */
	public static void makeMovie(File file, int frameRate) throws IOException
	{
		// let the user know that the movie is starting
		displayMovieStartingPane();

		// make a PNG movie (looks better than JPG)
		QuickTimeOutputStream.VideoFormat format = QuickTimeOutputStream.VideoFormat.PNG;

		// Between 0.0f and 1.0f. Irrelevant for PNG movies.
		float quality = 1.0f;

		out = new QuickTimeOutputStream(file, format);
		out.setVideoCompressionQuality(quality);
		out.setFrameRate(frameRate);
	}

	/**
	 * Writes an image as a frame on the movie.
	 * 
	 * @param image
	 *            The image that will become a frame of the movie.
	 */
	public static void writeFrame(BufferedImage image)
	{
		if(out != null)
		{
			try
			{
				out.writeFrame(image, 1);
			}
			catch(Exception e)
			{
				// do nothing
			}
		}
	}
}
