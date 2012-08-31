/*
 RotatedImageIcon -- a class within the Cellular Automaton Explorer. 
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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * Creates an ImageIcon that is rotated by the specified number of degrees when
 * painted.
 * 
 * @author David Bahr
 */
public class RotatedImageIcon extends ImageIcon
{
    // the degree of rotation of the icon.
    private float rotation = 0;

    /**
     * Create an ImageIcon that is painted in a rotated position. The default
     * rotation is 0, but can be reset via the setRotation() method.
     */
    public RotatedImageIcon(URL location)
    {
        super(location);
    }

    /**
     * Paints the icon.
     */
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        // rotate the icon
        Image image = this.getImage();
        Graphics2D g2 = (Graphics2D) g;
        double xPos = image.getWidth(null) / 2.0;
        double yPos = image.getHeight(null) / 2.0;
        float radians = (float) ((Math.PI / 180.0) * rotation);
        g2.rotate(radians, xPos, yPos);
        g2.drawImage(image, 0, 0, null);
    }

    /**
     * Get the degree of rotation.
     * 
     * @return The degrees of rotation (0.0f to 360.0f).
     */
    public float getRotation()
    {
        return rotation;
    }

    /**
     * Set the degree of rotation of the icon.
     * 
     * @param rotation
     *            The rotation in degrees (for example, 0.0f to 360.0f).
     */
    public void setRotation(float rotation)
    {
        this.rotation = rotation;
    }
}
