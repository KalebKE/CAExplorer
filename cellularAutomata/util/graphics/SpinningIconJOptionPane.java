/*
 SpinningIconJOptionPane -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2007  David B. Bahr (http://academic.regis.edu/dbahr/)

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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

import cellularAutomata.util.math.RandomSingleton;

/**
 * Creates a JOptionPane with an icon that spins.
 * 
 * @author David Bahr
 */
public class SpinningIconJOptionPane extends JOptionPane implements
    MouseListener
{
    // the number of milliseconds that the icon spins.
    private final static int SPIN_TIME = 6000;

    // the class that animates the spinning
    private Animator animator = null;

    // the degree of spin of the icon.
    private float rotation = 0;

    // used to decide how far the icon should rotate
    private Random random = RandomSingleton.getInstance();

    /**
     * Create a JOptionPane with an icon that spins.
     */
    public SpinningIconJOptionPane(Object message, int messageType,
        int optionType, RotatedImageIcon icon, Object[] options, Object initialValue)
    {
        super(message, messageType, optionType, icon, options, initialValue);

        // so user can click and spin the icon
        this.addMouseListener(this);
    }

    /**
     * Makes the icon start spinning.
     */
    public void startSpin()
    {
        // stop any previous fading
        if(animator != null)
        {
            animator.stop();
        }

        // rotate by 360 degrees plus some random amount (so stops in a random
        // position)
        float amountOfRotation = 360.0f + (360.0f * random.nextFloat());

        // This keeps changing the degree of rotation of the icon.
        PropertySetter setter = new PropertySetter(this, "rotation", rotation,
            rotation + amountOfRotation);

        animator = new Animator(SPIN_TIME, setter);

        // speed up then slow down the spinning
        animator.setAcceleration(0.1f);
        animator.setDeceleration(0.5f);

        // start the animation
        animator.start();
    }

    /**
     * Makes the icon stop spinning.
     */
    public void stopSpin()
    {
        // stop any previous spinning
        if(animator != null)
        {
            animator.stop();
        }
    }

    /**
     * Get the degree of rotation.
     * 
     * @return The degree of rotation (0.0f to 1.0f).
     */
    public float getRotation()
    {
        return rotation;
    }

    /**
     * Set the degree of rotation of the icon.
     * 
     * @param rotation
     *            The rotation in degrees (for example, 0.0 to 360.0).
     */
    public void setRotation(float rotation)
    {
        this.rotation = rotation;

        // this tells the icon to paint itself in this rotated position
        RotatedImageIcon icon = (RotatedImageIcon) this.getIcon();
        icon.setRotation(rotation);

        // and when repainting this JOptionPane, it automatically calls the icon
        // and asks it to repaint itself (by calling paintIcon(), which is
        // overridden in RotatedImageIcon to paint in the rotated position).
        repaint();
    }

    /**
     * Starts spinning the icon if (1) it has stopped and (2) the user clicks on
     * the panel.
     */
    public void mouseClicked(MouseEvent e)
    {
        if((animator != null) && !animator.isRunning())
        {
            startSpin();
        }
    }

    /**
     * Does nothing.
     */
    public void mousePressed(MouseEvent e)
    {
    }

    /**
     * Does nothing.
     */
    public void mouseReleased(MouseEvent e)
    {
    }

    /**
     * Does nothing.
     */
    public void mouseEntered(MouseEvent e)
    {
    }

    /**
     * Does nothing.
     */
    public void mouseExited(MouseEvent e)
    {
    }
}
