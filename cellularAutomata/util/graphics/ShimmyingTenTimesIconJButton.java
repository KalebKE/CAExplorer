/*
 ShimmyingIconJButton -- a class within the Cellular Automaton Explorer. 
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

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

/**
 * Creates a JButton with an icon that shimmies 10 times. Why not create a class
 * that arbitrarily sets the number of times? Because this one allows smoother
 * acceleration and deceleration of the shimmying.
 * 
 * @author David Bahr
 */
public class ShimmyingTenTimesIconJButton extends ShakingIconJButton
{
	/**
	 * A suggested number of milliseconds for the animation.
	 */
	public final static int SUGGESTED_SHIMMYING_TIME = 1500;

	/**
	 * Creates a JButton with an icon that shimmies 10 times.
	 */
	public ShimmyingTenTimesIconJButton(TranslatedImageIcon icon)
	{
		super(icon);
	}

	/**
	 * Makes the icon start shimmying.
	 * 
	 * @param shakeTime
	 *            The number of milliseconds for the entire animation.
	 */
	public void startShaking(int shakeTime)
	{
		// the class that animates the shimmying
		Animator animator = super.getAnimator();

		// stop any previous animation
		if(animator != null)
		{
			animator.stop();
		}

		// the distance left and right that it shimmies
		int shimmyDistance = 2;

		// This keeps changing the position of the icon between 0, then
		// -shimmyDistance, then 0, then shimmyDistance, then etc.
		PropertySetter setter = new PropertySetter(this, "xposition", 0,
				-shimmyDistance, 0, shimmyDistance, 0, -shimmyDistance, 0,
				shimmyDistance, 0, -shimmyDistance, 0, shimmyDistance, 0,
				-shimmyDistance, 0, shimmyDistance, 0, -shimmyDistance, 0,
				shimmyDistance, 0, -shimmyDistance, 0, shimmyDistance, 0,
				-shimmyDistance, 0, shimmyDistance, 0, -shimmyDistance, 0,
				shimmyDistance, 0, -shimmyDistance, 0, shimmyDistance, 0,
				-shimmyDistance, 0, shimmyDistance, 0);

		animator = new Animator(shakeTime, setter);

		// speed up and then slow down at the end (makes smoother looking
		// graphics)
		animator.setAcceleration(0.25f);
		animator.setDeceleration(0.5f);

		// start the animation
		animator.start();
	}
}
