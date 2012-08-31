/*
 GaussianRandom -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.util.math;

import java.util.Random;

/**
 * Generates random numbers with a Guassian/normal distribution. This class does
 * not extend java.util.Random so that there is no potential confusion with the
 * other methods available there.
 * 
 * @author David Bahr
 */
public class GaussianRandom
{
	private Random random = null;

	/**
	 * Create a random number generator.
	 */
	public GaussianRandom()
	{
		random = RandomSingleton.getInstance();
	}

	/**
	 * Generates a random number with a Gaussian distribution that has the
	 * specified mean and standard deviation.
	 * 
	 * @param mean
	 *            The mean of the Gaussian distribution.
	 * @param standardDeviation
	 *            The standard deviation of the Gaussian distribution.
	 * @return a random number with a normal distribution.
	 */
	public double nextGaussian(double mean, double standardDeviation)
	{
		// if X ~ N(mu, sigma^2), then aX+b = N(a mu + b, (a sigma)^2). In
		// Random's nextGaussian() method, mu = 0 and sigma = 1 which is the
		// "standard normal" distribution. So we just transform according to
		// this relationship.

		double standardRandom = random.nextGaussian();

		return standardDeviation * standardRandom + mean;
	}

}
