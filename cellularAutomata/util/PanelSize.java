/*
 PanelSize -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2005  David B. Bahr (http://academic.regis.edu/dbahr/)

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

package cellularAutomata.util;

/**
 * Holds a pair of points representing the width and height of a JPanel.
 * 
 * @author David Bahr
 */
public class PanelSize
{
    private int width = 0;
    private int height = 0;
    
    /**
     * Create a pair of points representing the width and height of a JPanel.
     * 
     * @param width The width of a JPanel.
     * @param height The height of a JPanel.
     */
    public PanelSize(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    /**
     * Get the height.
     */
    public int getHeight()
    {
        return height;
    }
    
    /**
     * Get the width.
     */
    public int getWidth()
    {
        return width;
    }
}
