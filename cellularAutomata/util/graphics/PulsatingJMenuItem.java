/*
 PulsatingJMenuItem -- a class within the Cellular Automaton Explorer. 
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

import javax.swing.JMenuItem;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

/**
 * Creates a JMenuItem that pulsates.
 * 
 * @author David Bahr
 */
public class PulsatingJMenuItem extends JMenuItem
{
    /**
     * Constant used to indicate that the pulsations should repeat an infinite
     * number of times.
     */
    public final int INFINITE = Animator.INFINITE;

    // the class that animates the pulsing
    private Animator animator = null;

    // the color that will pulse. Set in the constructor.
    private Color color = Color.RED;

    // the degree of transparency of the background color. Not used, but
    // necessary so that the PropertySetter can use this parameter as a
    // property.
    private float transparency = 0.0f;

    /**
     * Creates a pulsating menu item with the specified text displayed on the
     * button.
     * 
     * @param s
     *            The displayed text.
     * @param color
     *            The color of the text that will pulse.
     */
    public PulsatingJMenuItem(String s, Color color)
    {
        super(s);

        this.color = color;
    }

    /**
     * Starts the pulsating effect.
     * 
     * @param repeatCount
     *            The number of times that the button will pulse. May be
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

        // This keeps changing the transparency of the background.
        PropertySetter setter = new PropertySetter(this, "transparency", 1.0f,
            0.0f);

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
        }
    }

    /**
     * Set the degree of transparency of the background color.
     * 
     * @param transparency
     *            The alpha value between 0.0 and 1.0.
     */
    public void setTransparency(float transparency)
    {
        this.transparency = transparency;

        float[] colorComponents = new float[4];
        colorComponents = color.getRGBColorComponents(colorComponents);

        Color foregroundColor = new Color(colorComponents[0],
            colorComponents[1], colorComponents[2], transparency);
        this.setForeground(foregroundColor);

        repaint();
    }
}
