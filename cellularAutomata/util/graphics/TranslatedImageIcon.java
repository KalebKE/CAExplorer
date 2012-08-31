/*
 TranslatedImageIcon -- a class within the Cellular Automaton Explorer. 
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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * Creates an ImageIcon with its upper left corner positioned at the specified
 * coordinate.
 * 
 * @author David Bahr
 */
public class TranslatedImageIcon extends ImageIcon
{
	// the position of the icon.
	private int xposition = 0;

	/**
	 * Create an ImageIcon that is painted in a translated position. The default
	 * translation is 0, but can be reset via the setRotation() method.
	 */
	public TranslatedImageIcon(URL location)
	{
		super(location);
	}

	/**
	 * Paints the icon.
	 */
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		Image image = this.getImage();
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(image, x + getXposition(), y, null);
	}

	/**
	 * Get x position of the icon.
	 * 
	 * @return The  position of the upper left corner of the icon.
	 */
	public int getXposition()
	{
		return xposition;
	}

	/**
	 * Set the x position of the upper left corner of the icon.
	 * 
	 * @param xposition
	 *            The x position of the icon.
	 */
	public void setXposition(int xposition)
	{
		this.xposition = xposition;
	}
}
