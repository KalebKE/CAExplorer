/*
 DiscontinuousFunctions -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.util.math;

/**
 * Discontinuous functions such as the Heaviside step function.
 * 
 * @author David Bahr
 */
public class DiscontinuousFunctions
{
	/**
	 * Gets the value of a rectangle function (1.0 when -0.5 <= x <= 0.5, and
	 * otherwise 0.0).
	 * 
	 * @param x
	 *            The position being evaluated.
	 * @return 1.0 when -0.5 <= x <= 0.5, and otherwise 0.0.
	 */
	public static double rectangleFunction(double x)
	{
		return stepFunction(x + 0.5) - stepFunction(x - 0.5);
	}

	/**
	 * Gets the value of a single point function (1.0 at x = 0.0, and otherwise
	 * 0.0).
	 * 
	 * @param x
	 *            The position being evaluated.
	 * @return 1.0 at x = 0.0, and otherwise 0.0.
	 */
	public static double singlePoint(double x)
	{
		double value = 0.0;

		if(x == 0.0)
		{
			value = 1.0;
		}

		return value;
	}

	/**
	 * A square wave with a wavelength of 2.0 and values that range from y =
	 * -1.0 to y = 1.0. The function is 1.0 from [0, 1) and -1.0 from [1.0,
	 * 2.0). (Note inclusive and exclusive brackets.)
	 * 
	 * @param x
	 *            Position along the wave.
	 * @return The value of the square wave (-1.0 to 1.0).
	 */
	public static double squareWave(double x)
	{
		double value = -1.0;

		if(((int) Math.floor(x)) % 2 == 0)
		{
			value = 1.0;
		}

		return value;
	}

	/**
	 * Gets the value of a Heaviside step function with a discontinuity at 0.0.
	 * 
	 * @param x
	 *            The position being evaluated.
	 * @return A 0.0 (for x less than 0.0) or a 1.0 (for x greater than or equal
	 *         to 0.0).
	 */
	public static double stepFunction(double x)
	{
		double value = 0.0;

		if(x >= 0.0)
		{
			value = 1.0;
		}

		return value;
	}

	/**
	 * Gets the value of a Heaviside step function with a discontinuity at the
	 * specified position.
	 * 
	 * @param discontinuityLocation
	 *            The site of the discontinuity.
	 * @param x
	 *            The position being evaluated.
	 * @return A 0.0 (for x less than the position of the discontinuity) or a
	 *         1.0 (for x greater than or equal the position of the
	 *         discontinuity).
	 */
	public static double stepFunction(double discontinuityLocation, double x)
	{
		double value = 0.0;

		if(x >= discontinuityLocation)
		{
			value = 1.0;
		}

		return value;
	}
}