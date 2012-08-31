/*
 MinMaxIntPair -- a class within the Cellular Automaton Explorer. 
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

/**
 * Stores two integers, a minimum and a maximum.
 * 
 * @author David Bahr
 */
public class MinMaxIntPair
{
    public int min = 0;
    public int max = 0;
    
    /**
     * Create a pair of numbers for a minimum and a maximum.
     * 
     * @param minimum The minimum.
     * @param maximum The maximum.
     */
    public MinMaxIntPair(int minimum, int maximum)
    {
        min = minimum;
        max = maximum;
    }
}
