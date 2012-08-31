/*
 PulsatingBorderJButton -- a class within the Cellular Automaton Explorer. 
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

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.Icon;
import javax.swing.border.CompoundBorder;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

/**
 * Creates a JButton with text that pulsates.
 * 
 * @author David Bahr
 */
public class PulsatingBorderJButton extends JButton
{
	/**
	 * Constant used to indicate that the pulsations should repeat an infinite
	 * number of times.
	 */
	public final int INFINITE = Animator.INFINITE;

	// the class that animates the pulsing
	private Animator animator = null;

    // the pulsating border;
    private PulsatingBorder pulseBorder = null;

	/**
	 * Creates a button with a pulsating border and the specified icon displayed on the
	 * button.
	 * 
	 * @param icon
	 *            The displayed icon.
	 */
	public PulsatingBorderJButton(Icon icon)
	{
		super(icon);
		// add the pulsing border
        pulseBorder = new PulsatingBorder(this);
        this.setBorder(new CompoundBorder(this.getBorder(), pulseBorder));
	}

    /**
     * Starts the pulsating effect.
     * 
     * @param repeatCount
     *            The number of times that the text field will pulse. May be
     *            INFINITE (see constant defined in this class).
     */
    public void startPulsing(int repeatCount)
    {
        // Use the animation code developed by Chet Haase and others.
        //
        // stop any previous pulsing
        if(animator != null)
        {
            animator.stop();
        }

        // This keeps changing the thickness property of the pulseBorder.
        PropertySetter setter = new PropertySetter(pulseBorder, "thickness",
            0.0f, 1.0f);

        // multiply by 2 because with the REVERSE behavior, each pulse is
        // actually composed of two animations: (1) composed of an initial
        // fade-in followed by (2) the reverse fade-out. So we need to specify
        // twice the number of animations to get the specified number of pulses.
        animator = new Animator(900, repeatCount * 2,
            Animator.RepeatBehavior.REVERSE, setter);

        // start the animation
        animator.start();
    }

    /**
     * Stops the pulsating effect.
     */
    public void stopPulsing()
    {
        if(animator != null)
        {
            animator.stop();

            // This tells the animation to reset to a zero thickness pulse
            // border. That way, if the animation is stopped in the middle, it
            // goes back to the initial condition with no extra (pulse) border.
            PropertySetter setter = new PropertySetter(pulseBorder,
                "thickness", 0.0f, 0.0f);
            animator = new Animator(1, setter);
            animator.start();
        }
    }
}