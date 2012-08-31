/*
 ShakingIconJButton -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.util.graphics;

import javax.swing.JButton;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

/**
 * Creates a JButton with an icon that shakes indefinitely until told to stop.
 * 
 * @author David Bahr
 */
public class ShakingIconJButton extends JButton
{
	// the class that animates the shaking
	private Animator animator = null;

	// the position of the icon.
	private int xposition = 0;

	/**
	 * Creates a JButton with an icon that shakes.
	 */
	public ShakingIconJButton(TranslatedImageIcon icon)
	{
		super(icon);
	}

	/**
	 * Gets the animator class used to do the shaking.
	 * 
	 * @return The class that animates the shaking.
	 */
	protected Animator getAnimator()
	{
		return animator;
	}

	/**
	 * Makes the icon start shaking indefinitely (the shaking repeats until the
	 * stopShaking() method is called).
	 * 
	 * @param shakeTime
	 *            The number of milliseconds per shake (in other words, per
	 *            repetition).
	 */
	public void startShaking(int shakeTime)
	{
		// stop any previous animation
		if(animator != null)
		{
			animator.stop();
		}

		// the distance left and right that it shimmies
		int shimmyDistance = 2;

		// This keeps changing the position of the icon.
		PropertySetter setter = new PropertySetter(this, "xposition", 0,
				-shimmyDistance, 0, shimmyDistance, 0);

		animator = new Animator(shakeTime, Animator.INFINITE,
				Animator.RepeatBehavior.LOOP, setter);

		// start the animation
		animator.start();
	}

	/**
	 * Makes the icon stop shaking.
	 */
	public void stopShaking()
	{
		// stop any previous shaking
		if(animator != null)
		{
			animator.stop();
		}
	}

	/**
	 * Get the x position of the upper left corner of the icon.
	 * 
	 * @return The x position of the upper left corner of the icon.
	 */
	public int getXposition()
	{
		return xposition;
	}

	/**
	 * Set the x position of the icon.
	 * 
	 * @param xposition
	 *            The x position of the upper left corner of the icon.
	 */
	public void setXposition(int xposition)
	{
		this.xposition = xposition;

		// this tells the icon to paint itself in this rotated position
		TranslatedImageIcon icon = (TranslatedImageIcon) this.getIcon();
		icon.setXposition(xposition);

		// and when repainting this JOButton, it automatically calls the icon
		// and asks it to repaint itself (by calling paintIcon(), which is
		// overridden in RotatedImageIcon to paint in the rotated position).
		repaint();
	}
}
