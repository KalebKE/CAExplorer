/*
 EquilateralTriangle -- a class within the Cellular Automaton Explorer. 
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
 * An equilateral triangle that can be drawn by a Graphics object (using the
 * Graphics method drawPolygon or fillPolygon).
 * 
 * @author David Bahr
 */
public class EquilateralTriangle extends Polygon
{
    /**
     * Create an equilateral triangle with the given center and side length,
     * rotated by the given angle.
     * 
     * @param xCenter
     *            The x coordinate of the center of the triangle.
     * @param yCenter
     *            The y coordinate of the center of the triangle.
     * @param sideLength
     *            The length of a side of the triangle.
     * @param angle
     *            The angle in degrees by which the tip of the triangle is
     *            rotated (clockwise).
     */
    public EquilateralTriangle(int xCenter, int yCenter, int sideLength,
        double angle)
    {
        super();

        //the angle of rotation converted to radians
        double theta = angle * (Math.PI / 180.0);

        //30 degrees in radians
        double radians30 = 30.0 * (Math.PI / 180.0);

        //half a side length
        double halfSide = (double) sideLength / 2.0;

        //length from center to a vertex
        double centerToVertex = (double) sideLength * Math.tan(radians30);

        //half of length from center to a vertex
        double halfCenterToVertex = centerToVertex / 2.0;

        //the x and y coordinates of each vertex
        double[] dxVertices = new double[3];
        double[] dyVertices = new double[3];

        //build the triangle centered at the origin
        dxVertices[0] = 0.0;
        dyVertices[0] = -centerToVertex;

        dxVertices[1] = halfSide;
        dyVertices[1] = halfCenterToVertex;

        dxVertices[2] = -halfSide;
        dyVertices[2] = halfCenterToVertex;

        //Need to rotate and translate. Use rotation matrix.
        for(int i = 0; i < dxVertices.length; i++)
        {
            //rotate
            double tempX = dxVertices[i];
            double tempY = dyVertices[i];
            dxVertices[i] = (tempX * Math.cos(theta))
                + (-tempY * Math.sin(theta));
            dyVertices[i] = (tempX * Math.sin(theta))
                + (tempY * Math.cos(theta));

            //now translate to the correct position
            dxVertices[i] += xCenter;
            dyVertices[i] += yCenter;
        }

        //Now convert to an array of ints.
        int[] xVertices = new int[3];
        int[] yVertices = new int[3];
        for(int i = 0; i < xVertices.length; i++)
        {
            xVertices[i] = (int) Math.round(dxVertices[i]);
            yVertices[i] = (int) Math.round(dyVertices[i]);
        }
        
        //Now convert to an array of ints.Note that we always round in the
        //direction that would make the triangle bigger. Makes the lattice
        //graphics prettier.
        /*for(int i = 0; i < xVertices.length; i++)
        {
            if(dxVertices[i] > 0.0)
            {
                xVertices[i] = (int) Math.ceil(dxVertices[i]);
            }
            else
            {
                xVertices[i] = (int) Math.floor(dxVertices[i]);
            }

            if(dyVertices[i] > 0.0)
            {
                yVertices[i] = (int) Math.ceil(dyVertices[i]);
            }
            else
            {
                yVertices[i] = (int) Math.floor(dyVertices[i]);
            }
        }*/

        //now set the coordinates of the vertices of the polygon
        this.xpoints = xVertices;
        this.ypoints = yVertices;

        //sets the number of vertices
        this.npoints = 3;
    }
}
