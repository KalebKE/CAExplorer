/*
 RandomSingleton -- a class within the Cellular Automaton Explorer. 
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

import java.util.Random;

/**
 * Creates a single random number generator using the Singleton design pattern.
 * Always returns the same random number generator.
 * 
 * @author David Bahr
 */
public class RandomSingleton
{
	// the single instance of the random number generator
	private static Random random = new Random();

	/**
	 * Private so cannot be instantiated.
	 */
	private RandomSingleton()
	{
	}

	/**
	 * Gets an instance of a random number generator. It will be the same
	 * instance every time.
	 * 
	 * @return a random number generator.
	 */
	public static Random getInstance()
	{
		return random;
	}

}
