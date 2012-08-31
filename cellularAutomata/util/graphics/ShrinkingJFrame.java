/*
 * ShrinkingJFrame -- a class within the Cellular Automaton Explorer. Copyright
 * (C) 2007 David B. Bahr (http://academic.regis.edu/dbahr/) This program is
 * free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package cellularAutomata.util.graphics;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JFrame;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

/**
 * Creates a JFrame that shrinks when exiting.
 * 
 * @author David Bahr
 */
public class ShrinkingJFrame extends JFrame
{
	// the class that animates the shrinking
	private Animator animator = null;

	// when true, will exit the application after shrinking
	private boolean exitApplication = false;

	// when true, will eliminate the frame after shrinking (but won't close
	// the application)
	private boolean dispose = false;

	// all of the components on the frame
	private Component[] components = null;

	// the percent area of the frame's original size
	private float percentOriginalSize = 1.0f;

	// the frame's original size when shrink is first called.
	private Dimension originalSize = null;

	// length of time in milliseconds that it takes for the frame to shrink.
	private int animationLength = 200;

	// the frame's original screen location
	private Point originalScreenLocation = null;

	// the frame's final screen location
	private Point finalScreenLocation = null;

	/**
	 * Creates a frame that shrinks to nothing when requested.
	 */
	public ShrinkingJFrame()
	{
		super();
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}

	/**
	 * Creates a frame that shrinks to nothing when requested.
	 * 
	 * @param title
	 *            The title displayed on the frame.
	 */
	public ShrinkingJFrame(String title)
	{
		super(title);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}

	/**
	 * Creates a frame that shrinks to nothing when requested.
	 * 
	 * @param title
	 *            The title displayed on the frame.
	 * @param graphicsConfiguration
	 *            The graphics configuration, which may be null.
	 */
	public ShrinkingJFrame(String title,
			GraphicsConfiguration graphicsConfiguration)
	{
		super(title, graphicsConfiguration);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}

	/**
	 * Shrinks the frame.
	 * 
	 * @param exit
	 *            When true, exits the application after shrinking.
	 */
	private void shrinkFrame(boolean exit)
	{
		// perform any action desired before the frame shrinks (like stopping
		// the CA)
		actionBeforeShrinking();

		// set instance variable
		exitApplication = exit;

		// get the original size
		originalSize = this.getSize();

		// make the screen look grey -- this minimizes changes in color and
		// helps smooth the animation
		// HalfTransparentGlassPane glassPane = new HalfTransparentGlassPane();
		// this.setGlassPane(glassPane);
		// ((HalfTransparentGlassPane) (this.getGlassPane()))
		// .setViewDisabled(true);
		// this.getGlassPane().setVisible(true);

		// get the original screen location
		try
		{
			// this will fail if the frame is hidden (not likely, since they
			// probably just pushed the close button -- but the close
			// operation
			// could be called programatically)
			originalScreenLocation = this.getLocationOnScreen();
		}
		catch(Exception failed)
		{
			// assume it was in the center of the screen
			originalScreenLocation = new Point(
					(int) (Toolkit.getDefaultToolkit().getScreenSize().width / 2.0),
					(int) (Toolkit.getDefaultToolkit().getScreenSize().height / 2.0));
		}

		// get the final screen location if not specified
		if(finalScreenLocation == null)
		{
			int x = (int) ((originalScreenLocation.x + originalSize.width) / 2.0);
			int y = (int) ((originalScreenLocation.y + originalSize.height) / 2.0);
			finalScreenLocation = new Point(x, y);
		}

		// stop any previous shrinking
		if(animator != null)
		{
			animator.stop();
		}

		// get all of the components on the frame
		components = this.getComponents();

		// hide components for a smoother animation
		//
		// The image reduction is cleaner when it doesn't try to constantly redo
		// the layout.
		for(Component c : components)
		{
			c.setVisible(false);
		}

		// This keeps changing the size of the frame.
		PropertySetter setter = new PropertySetter(this, "percentOriginalSize",
				1.0f, 0.0f);

		animator = new Animator(animationLength, setter);
		// animator = new Animator(8000, setter);

		// make it accelerate its disappearance during the entire animation
		animator.setAcceleration(1.0f);

		// start the animation
		animator.start();
	}

	/**
	 * Should be overridden by child classes to perform any desired action after
	 * the animation finishes shrinking the frame. This method will be called
	 * automatically. Examples might be pausing the CA, or saving data. The
	 * default action (when not overridden) is to do nothing.
	 */
	public void actionAfterShrinking()
	{
	}

	/**
	 * Should be overridden by child classes to perform any desired action prior
	 * to starting the animation that shrinks the frame. This method will be
	 * called automatically. Examples might be pausing the CA, or saving data.
	 * The default action (when not overridden) is to do nothing.
	 */
	public void actionBeforeShrinking()
	{
	}

	/**
	 * Shrinks the frame. After shrinking, hides the frame, but does not exit
	 * the application.
	 * 
	 * @param dispose
	 *            When true, eliminates the window. When false hides the window
	 *            but does not close it. Once eliminated, attempts to redisplay
	 *            the window may cause unpredictable behavior.
	 */
	public void shrink(boolean dispose)
	{
		this.dispose = dispose;
		this.finalScreenLocation = null;

		shrinkFrame(false);
	}

	/**
	 * Shrinks the frame and moves to the specified position as it shrinks.
	 * Designed to make it look like the frame is shrinking into a specific
	 * place. After shrinking, hides the frame, but does not exit the
	 * application.
	 * 
	 * @param finalScreenPosition
	 *            The position to which the frame moves as it shrinks. May be
	 *            null.
	 * @param dispose
	 *            When true, eliminates the window. When false hides the window
	 *            but does not close it. Once eliminated, attempts to redisplay
	 *            the window may cause unpredictable behavior.
	 */
	public void shrink(Point finalScreenPosition, boolean dispose)
	{
		this.dispose = dispose;
		this.finalScreenLocation = finalScreenPosition;
		shrinkFrame(false);
	}

	/**
	 * Makes the frame fade away and then exits the application.
	 */
	public void shrinkAndExitApplication()
	{
		this.finalScreenLocation = null;

		// forces the application to exit, after shrinking
		shrinkFrame(true);
	}

	/**
	 * Get the area of the frame as a percentage of its original size.
	 * 
	 * @return The percentage of it's original size.
	 */
	public float getPercentOriginalSize()
	{
		return percentOriginalSize;
	}

	/**
	 * Sets the length of the animation in milliseconds. Short animations are
	 * less annoying, but longer animations are smoother. The default value is
	 * 200.
	 * 
	 * @param milliseconds
	 *            Length of the animation.
	 */
	public void setAnimationLength(int milliseconds)
	{
		this.animationLength = milliseconds;
	}

	/**
	 * Set the size of the frame as a percentage of the original area.
	 */
	public void setPercentOriginalSize(float percentOriginalSize)
	{
		this.percentOriginalSize = percentOriginalSize;

		// calculate the new position, so shrinks from its initial position to
		// its final position (into itself, if no final position was passed in
		// as a parameter to the shrink() method)
		int newXPos = (int) (percentOriginalSize * originalScreenLocation.x + (1.0f - percentOriginalSize)
				* finalScreenLocation.x);
		int newYPos = (int) (percentOriginalSize * originalScreenLocation.y + (1.0f - percentOriginalSize)
				* finalScreenLocation.y);

		// add a circle, so spirals away (needs a slower animation to be
		// effective)
		// newXPos += (int) (250.0 * Math.cos(percentOriginalSize * 3.0 *
		// Math.PI));
		// newYPos += (int) (250.0 * Math.sin(percentOriginalSize * 3.0 *
		// Math.PI));

		// add a vertical component, so sinks into oblivion
		// int totalSinkAmount = (int) (originalSize.height / 2.0);
		// newYPos += totalSinkAmount - percentOriginalSize * totalSinkAmount;

		// this does the actual shrinking and repositioning
		this.setBounds(newXPos, newYPos,
				(int) (originalSize.width * percentOriginalSize),
				(int) (originalSize.height * percentOriginalSize));

		Dimension size = new Dimension(
				(int) (originalSize.width * percentOriginalSize),
				(int) (originalSize.height * percentOriginalSize));

		// shouldn't have to do this, but necessary in xp when running outside
		// the eclipse environment
		this.setSize(size);
		this.setPreferredSize(size);
		this.setMinimumSize(size);
		this.setMaximumSize(size);

		// vary the color
		// int colorComponent = (int) percentOriginalSize * 256;
		// this.setBackground(new Color(colorComponent, colorComponent,
		// colorComponent));

		// when shrunk to minimum size, exit the application OR hide the frame
		if(percentOriginalSize == 0.0f)
		{
			if(exitApplication)
			{
				System.exit(0);
			}

			// make all the components visible again so they are available
			// next time (otherwise they stay hidden when the frame is restarted
			// -- even disposed frames).
			for(Component c : components)
			{
				c.setVisible(true);
			}

			if(dispose)
			{
				this.setVisible(false);

				// reset to the original size
				this.setSize(originalSize);

				// reset to the original location
				this.setLocation(originalScreenLocation);

				// kills the frame but not the application
				this.dispose();
			}
			else
			{
				this.setVisible(false);

				// reset to the original size
				this.setSize(originalSize);

				// reset to the original location
				this.setLocation(originalScreenLocation);
			}

			// perform any action desired after the frame shrinks (like
			// stopping the CA)
			actionAfterShrinking();
		}
	}
}
