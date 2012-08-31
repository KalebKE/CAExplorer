/*
 Hexagon -- a class within the Cellular Automaton Explorer. 
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

import java.awt.Polygon;

/**
 * A hexagon that can be drawn by a Graphics object (using the Graphics method
 * drawPolygon or fillPolygon).
 * 
 * @author David Bahr
 */
public class Hexagon extends Polygon
{
    /**
     * Create a hexagon with the given center and side length
     * 
     * @param xCenter
     *            The x coordinate of the center of the hexagon.
     * @param yCenter
     *            The y coordinate of the center of the hexagon.
     * @param sideLength
     *            The length of a side of the hexagon.
     */
    public Hexagon(int xCenter, int yCenter, int sideLength)
    {
        super();

        //30 degrees in radians
        double radians = 30.0 * (Math.PI / 180.0);

        //distance from center of hexagon to center of a side
        int d = (int) Math.round((double) sideLength * Math.cos(radians));

        //half a side length
        int halfSide = (int) Math.round((double) sideLength / 2.0);

        //length from center to a vertex
        int centerToVertex = sideLength;

        //the x and y coordinates of each vertex
        int[] xVertices = new int[6];
        int[] yVertices = new int[6];

        xVertices[0] = xCenter - d;
        yVertices[0] = yCenter + halfSide;

        xVertices[1] = xCenter;
        yVertices[1] = yCenter + centerToVertex;

        xVertices[2] = xCenter + d;
        yVertices[2] = yCenter + halfSide;

        xVertices[3] = xCenter + d;
        yVertices[3] = yCenter - halfSide;

        xVertices[4] = xCenter;
        yVertices[4] = yCenter - centerToVertex;

        xVertices[5] = xCenter - d;
        yVertices[5] = yCenter - halfSide;

        //now set the coordinates of the vertices of the polygon
        this.xpoints = xVertices;
        this.ypoints = yVertices;

        //sets the number of vertices
        this.npoints = 6;
    }
}
