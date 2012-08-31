/*
 BinaryStateOutOfBoundsException -- a class within the Cellular Automaton Explorer. 
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
 * An exception for when a binary state is out of bounds (not 0 or 1).
 * 
 * @author David Bahr
 */
public class BinaryStateOutOfBoundsException extends RuntimeException
{
    private int outOfBoundsValue = 0;

    /**
     * Creates an out of bounds exception that is triggered when a value is not
     * a 0 or 1.
     * 
     * @param message
     *            A message associated with the out of bounds exception. May be
     *            null.
     * @param outOfBoundsValue
     *            The value that caused the out of bounds exception (the value
     *            that was not a 0 or a 1). For example, a 3.
     */
    public BinaryStateOutOfBoundsException(String message, int outOfBoundsValue)
    {
        super(message);

        this.outOfBoundsValue = outOfBoundsValue;
    }

    /**
     * Gets the value that caused the out of bounds exception.
     * 
     * @return The value that was out of bounds (not a 0 or a 1).
     */
    public int getOutOfBoundsValue()
    {
        return outOfBoundsValue;
    }
}