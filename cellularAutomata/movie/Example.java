package cellularAutomata.movie;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Example
{
	// ------------------------------------------------------------------------

	/**
	 * Code example written by Werner Randelshofer and modified by David Bahr.
	 * 
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args)
	{
		try
		{
			example(new File("quicktimedemo-jpg.mov"),
					QuickTimeOutputStream.VideoFormat.JPG, 1f);
			example(new File("quicktimedemo-png.mov"),
					QuickTimeOutputStream.VideoFormat.PNG, 1f);
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Code example written by Werner Randelshofer and modified by David Bahr.
	 * <p>
	 * An example that creates a movie and adds the frames one at a time by
	 * calling out.writeFrame(img, 1) where img is a BufferedImage. the images
	 * are simple rectangles.
	 * 
	 * @param file
	 *            The output file name.
	 * @param format
	 *            Either QuickTimeOutputStream.VideoFormat.PNG or
	 *            QuickTimeOutputStream.VideoFormat.JPG.
	 * @param quality
	 *            Between 0.0f and 1.0f. Irrelevant for PNG movies.
	 * @throws IOException
	 *             If could not create the specified output file.
	 */
	private static void example(File file,
			QuickTimeOutputStream.VideoFormat format, float quality)
			throws IOException
	{
		QuickTimeOutputStream out = null;
		Graphics2D g = null;
		try
		{
			out = new QuickTimeOutputStream(file, format);
			out.setVideoCompressionQuality(quality);
			out.setFrameRate(30); // 30 fps

			Random r = new Random();
			BufferedImage img = new BufferedImage(320, 160,
					BufferedImage.TYPE_INT_RGB);
			g = img.createGraphics();
			g.setBackground(Color.WHITE);
			g.clearRect(0, 0, img.getWidth(), img.getHeight());

			for(int i = 0; i < 100; i++)
			{
				g.setColor(new Color(r.nextInt()));
				g.fillRect(r.nextInt(img.getWidth() - 30), r.nextInt(img
						.getHeight() - 30), 30, 30);
				out.writeFrame(img, 1);
			}

		}
		finally
		{
			if(g != null)
			{
				g.dispose();
			}
			if(out != null)
			{
				out.close();
			}
		}
	}
}
