/*
 Waves -- a class within the Cellular Automaton Explorer. 
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
 * Wave functions, like a square wave and a sawtooth.
 * 
 * @author David Bahr
 */
public class Waves
{
	/**
	 * A square wave with a wavelength of 2*Pi and values that range from -1.0
	 * to 1.0. The wave is 1.0 at 0 and changes to -1.0 at Pi.
	 * 
	 * @param x
	 *            Position along the wave.
	 * @return The value of the square wave (-1.0 to 1.0).
	 */
	public static double squareWave(double x)
	{
		return DiscontinuousFunctions.squareWave(x);
	}

	/**
	 * A sawtooth wave with a wavelength of 1.0 centered at 0.0. In other words,
	 * increases linearly from -1.0 to 1.0 on the range [-0.5, 0.5).
	 * 
	 * @param x
	 *            Position along the wave.
	 * @return The value of the sawtooth wave (-1.0 to 1.0).
	 */
	public static double sawtoothWave(double x)
	{
		// shift right by 0.5
		x += 0.5;

		double value = 2.0 * (x - Math.floor(x)) - 1.0;

		return value;
	}

	/**
	 * A triangle wave with a wavelength of 4.0 and values that range from y =
	 * -1.0 to y = 1.0. At x = 0.0, the wave is y = 0.0. From x = 0.0 to x = 1.0
	 * the wave increases linearly from y = 0.0 at y = 1.0. From x = 1.0 to x =
	 * 3.0 the value decreases linearly from y = 1.0 to y= -1.0. From x = 3.0 to
	 * x = 4.0, the value increases linearly to y = 0.0.
	 * 
	 * @param x
	 *            Position along the wave.
	 * @return The value of the triangle wave (-1.0 to 1.0).
	 */
	public static double triangleWave(double x)
	{
		// the function value
		double value = 0.0;

		// use reflection for negative values
		boolean reflect = false;
		if(x < 0.0)
		{
			x = -x;
			reflect = true;
		}

		if((int) Math.floor(x) % 4 == 0)
		{
			// shift the value
			double z = (x - Math.floor(x));

			value = z;
		}
		else if((int) Math.floor(x) % 4 == 1)
		{
			// shift the value
			double z = (x - Math.floor(x)) + 1.0;

			value = 2.0 - z;
		}
		else if((int) Math.floor(x) % 4 == 2)
		{
			// shift the value
			double z = (x - Math.floor(x)) + 2.0;

			value = 2.0 - z;
		}
		else if((int) Math.floor(x) % 4 == 3)
		{
			// shift the value
			double z = (x - Math.floor(x)) + 3.0;

			value = -4.0 + z;
		}

		if(reflect)
		{
			value = -value;
		}

		return value;
	}
}
