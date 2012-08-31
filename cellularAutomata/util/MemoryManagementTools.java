/*
 MemoryManagementTools -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.util;

/**
 * A collection of handy methods for memory management.
 * 
 * @author David Bahr
 */
public class MemoryManagementTools
{
	/**
	 * Converts bytes into the equivalent number of gigabytes.
	 * 
	 * @param bytes
	 *            The number of bytes that will be converted.
	 * @return The equivalent number of gigabytes (as a fraction).
	 */
	public static double convertBytesToGigaBytes(long bytes)
	{
		return bytes / 1073741824.0;
	}

	/**
	 * Converts bytes into the equivalent number of megabytes.
	 * 
	 * @param bytes
	 *            The number of bytes that will be converted.
	 * @return The equivalent number of megabytes (as a fraction).
	 */
	public static double convertBytesToMegaBytes(long bytes)
	{
		return bytes / 1048576.0;
	}

	/**
	 * The size of the Java object heap. In other words, this gives the maximum
	 * amount of memory that has been made available to the JVM. See
	 * http://java.sun.com/docs/books/performance/1st_edition/html/JPRAMFootprint.fm.html
	 * for an explanation.
	 * 
	 * @return The maximum memory available to the application in bytes.
	 */
	public static long getMaximumMemoryAvailableToApplication()
	{
		Runtime runtime = Runtime.getRuntime();
		return runtime.maxMemory();
	}

	/**
	 * The amount of memory currently in use by the JVM. Calculated by taking
	 * the difference between the total memory allocated (totalMemory()) minus
	 * the unused memory from within that allocation (freeMemory()).
	 * 
	 * @return The memory currently in use in bytes.
	 */
	public static long getMemoryCurrentlyInUse()
	{
		Runtime runtime = Runtime.getRuntime();
		return runtime.totalMemory() - runtime.freeMemory();
	}

	/**
	 * Estimates the memory still available to the JVM. Estimated by taking the
	 * difference between the maximum memory available to the JVM (maxMemory())
	 * minus the total memory already allocated (totalMemory()). To this, we add
	 * any memory still available from that which was allocated (freeMemory()).
	 * 
	 * @return An estimate of the available memory in bytes .
	 */
	public static long getMemoryAvailable()
	{
		Runtime runtime = Runtime.getRuntime();
		return runtime.freeMemory()
				+ (runtime.maxMemory() - runtime.totalMemory());
	}

	/**
	 * Estimates the percentage of memory in use. Estimated by dividing the
	 * memory currently in use by the maximum total memory that can be made
	 * available.
	 * 
	 * @return A number between 0.0 and 1.0.
	 */
	public static double getPercentMemoryUsed()
	{
		Runtime runtime = Runtime.getRuntime();
		return getMemoryCurrentlyInUse() / (double) runtime.maxMemory();
	}
}
