/*
 IntegerStateOutOfBoundsException -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.error.exceptions;

/**
 * An exception for when an integer state is out of bounds (not between 0 and
 * numStates-1).
 * 
 * @author David Bahr
 */
public class IntegerStateOutOfBoundsException extends RuntimeException
{
    // the number of allowed states
    private int numStates = 0;

    // the out of bounds value
    private int outOfBoundsValue = -1;

    /**
     * Creates an exception for when an integer state is out of bounds (not
     * between 0 and numStates-1).
     * 
     * @param message
     *            A message detailing the error.
     * @param numStates
     *            The number of allowed states (allowed states are 0 to
     *            numStates-1 inclusive).
     * @param outOfBoundsValue
     *            The incorrect value.
     */
    public IntegerStateOutOfBoundsException(String message, int numStates,
        int outOfBoundsValue)
    {
        super(message);

        this.numStates = numStates;
        this.outOfBoundsValue = outOfBoundsValue;
    }

    /**
     * Get the number of integer states allowed (allowed states are 0 to
     * numStates-1).
     * 
     * @return The number of allowed states.
     */
    public int getNumStates()
    {
        return numStates;
    }

    /**
     * The value that caused the error.
     * 
     * @return The value that was not between 0 (inclusive) and numStates-1
     *         (inclusive).
     */
    public int getOutOfBoundsValue()
    {
        return outOfBoundsValue;
    }
}