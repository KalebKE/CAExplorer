/*
 MathFunctions -- a class within the Cellular Automaton Explorer. 
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
 * Commonly used math functions.
 * 
 * @author David Bahr
 */
public class MathFunctions
{
	/**
	 * Finds the greatest common denominator (gcd) using Euclid's algorithm.
	 * 
	 * @param a
	 *            A value for which the gcd will be calculated.
	 * @param b
	 *            A value for which the gcd will be calculated.
	 * @return The greatest common denominator of a and b.
	 */
	public static int gcd(int a, int b)
	{
		return (b != 0 ? gcd(b, a % b) : a);
	}

	/**
	 * A Gaussian function.
	 * 
	 * @param x
	 *            The value being passed into the function.
	 * @param mean
	 *            The mean of the Gaussian.
	 * @param standardDeviation
	 *            The standard deviation of the Gaussian.
	 * @return The value of the function at x.
	 */
	public static double gaussian(double x, double mean,
			double standardDeviation)
	{
		double factor = 1.0 / (standardDeviation * Math.sqrt(2.0 * Math.PI));
		double exponent = -(x - mean) * (x - mean)
				/ (2.0 * standardDeviation * standardDeviation);

		double value = factor * Math.exp(exponent);

		return value;
	}

	/**
	 * The derivative of a Gaussian function.
	 * 
	 * @param x
	 *            The value being passed into the function.
	 * @param mean
	 *            The mean of the Gaussian (from which this derivative is
	 *            derived).
	 * @param standardDeviation
	 *            The standard deviation of the Gaussian (from which this
	 *            derivative is derived).
	 * @return The value of the function at x.
	 */
	public static double gaussianDerivative(double x, double mean,
			double standardDeviation)
	{
		double factor = -(x - mean)
				/ (standardDeviation * standardDeviation * standardDeviation * Math
						.sqrt(2.0 * Math.PI));
		double exponent = -(x - mean) * (x - mean)
				/ (2.0 * standardDeviation * standardDeviation);

		double value = factor * Math.exp(exponent);

		return value;
	}
}
