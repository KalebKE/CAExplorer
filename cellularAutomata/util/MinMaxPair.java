/*
 MinMaxPair -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2006  David B. Bahr (http://academic.regis.edu/dbahr/)

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

import java.util.Collection;

/**
 * Stores two doubles, a minimum and a maximum.
 * 
 * @author David Bahr
 */
public class MinMaxPair
{

    /**
     * The maximum of the pair.
     */
    public double max = 0;

    /**
     * The minimum of the pair.
     */
    public double min = 0;

    /**
     * Create a pair of numbers for a minimum and a maximum.
     * 
     * @param minimum
     *            The minimum.
     * @param maximum
     *            The maximum.
     */
    public MinMaxPair(double minimum, double maximum)
    {
        min = minimum;
        max = maximum;
    }

    /**
     * Finds the min and max of a pair and returns as a MinMaxPair. Returns null
     * if the specified array is null or empty.
     * 
     * @param numbers
     *            An array of numbers from which the min and max will be found.
     * 
     * @return The minimum and maximum of the array. Returns null if the numbers
     *         array is empty or if the numbers array is null.
     */
    public static MinMaxPair findMinMax(double[] numbers)
    {
        MinMaxPair minMax = null;

        if(numbers != null && numbers.length > 0)
        {
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;

            // finds the min and max
            for(int i = 0; i < numbers.length; i++)
            {
                if(numbers[i] < min)
                {
                    min = numbers[i];
                }
                if(numbers[i] > max)
                {
                    max = numbers[i];
                }
            }

            // saves the min and max
            minMax = new MinMaxPair(min, max);
        }

        return minMax;
    }

    /**
     * Finds the min and max of a pair and returns as a MinMaxPair. Returns null
     * if the specified array is null or empty.
     * 
     * @param numbers
     *            A collection of Double objects from which the min and max will
     *            be found.
     * 
     * @return The minimum and maximum of the array. Returns null if the numbers
     *         array is empty or if the numbers array is null.
     */
    public static MinMaxPair findMinMax(Collection numbers)
    {
        // converts the collection to an array
        double[] doubleNums = null;
        if(numbers != null && !numbers.isEmpty())
        {
            Object[] nums = numbers.toArray();
            doubleNums = new double[nums.length];

            for(int i = 0; i < nums.length; i++)
            {
                // note that there is no provision for catching errors if the
                // collection holds something other than a Double. It will just
                // create a runtime exception (appropriately).
                doubleNums[i] = ((Double) nums[i]).doubleValue();
            }
        }

        // now that its an array, can call the other method
        return findMinMax(doubleNums);
    }

    /**
     * Gets the maximum stored in this pair.
     * 
     * @return The maximum.
     */
    public double getMax()
    {
        return max;
    }

    /**
     * Gets the minimum stored in this pair.
     * 
     * @return The minimum.
     */
    public double getMin()
    {
        return min;
    }
}
