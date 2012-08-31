/*
 Arrow -- a class within the Cellular Automaton Explorer. 
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
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

// import java.awt.geom.Path2D;

/**
 * Creates an arrow Shape.
 * 
 * @author David Bahr
 */
public class Arrow
{
	// length of default arrow in pixels (before rescaling)
	private static int arrowLength = 100;

	// length of the default head of the arrow in pixels (before rescaling)
	private static int arrowHeadLength = arrowLength / 8;

	// The line part of the arrow
	private static Line2D.Double line = new Line2D.Double(0, 0, arrowLength, 0);

	// x-coordinates of points on the triangle
	private static int[] xCoordinates = {arrowLength,
			arrowLength - arrowHeadLength, arrowLength - arrowHeadLength};

	// y-coordinates of points on the triangle
	private static int[] yCoordinates = {0, arrowHeadLength / 2,
			-arrowHeadLength / 2};

	// The triangle part of the arrow
	private static Polygon triangle = new Polygon(xCoordinates, yCoordinates, 3);

	/**
	 * Creates an arrow shape.
	 * 
	 * @param originX
	 *            The starting position's x coordinate.
	 * @param originY
	 *            The ending position's y coordinate.
	 * @param endX
	 *            The starting position's x coordinate.
	 * @param endY
	 *            The ending position's y coordinate.
	 * @return The arrow shape of the correct orientation and length.
	 */
	public static Shape createArrow(double originX, double originY,
			double endX, double endY)
	{
		// create a generic/default arrow from a line and a triangle
		// Path2D.Double arrow = new Path2D.Double(line);
		GeneralPath arrow = new GeneralPath(line);
		arrow.append(triangle, false);

		// now rescale and translate the default arrow to the correct location
		AffineTransform transform = new AffineTransform();

		// scale the length of the original arrow to 1
		double scaleToLengthOne = 1.0 / (double) arrowLength;
		transform.scale(scaleToLengthOne, scaleToLengthOne);

		// now scale the length of the requested arrow
		double length = Math.sqrt(Math.pow(endX - originX, 2.0)
				+ Math.pow(endY - originY, 2.0));
		transform.scale(length, length);

		// rotate to correct angle (note, regular arctan will not work, because
		// arctan range is only +-Pi/2)
		double theta = StrictMath.atan2((double) (endY - originY),
				(double) (endX - originX));
		transform.rotate(theta);

		// translate to correct position
		transform.translate(originX, originY);

		// now do the transformation on the arrow
		Shape finalArrow = arrow.createTransformedShape(transform);

		return finalArrow;

		// return new java.awt.geom.Ellipse2D.Double(0.1,0.1,0.1,0.1);
	}
}
