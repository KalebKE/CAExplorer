/*
 Modulus -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.util.math;

/**
 * This class handles the modulus of two integers. The % operator in Java
 * returns negative values, which is logically consistent with Java's integer
 * division operator, but it is not the typical definition of modulus. In fact,
 * Java's % operator is really a "remainder after division" operator. This class
 * gives an alternative that always returns positive values. When doing
 * wrap-around boundary conditions, this "positive number" definition is easier.
 * (Note, there are many ways to define the modulus of a negative number. This
 * is just one of many.)
 * 
 * @author David Bahr
 */
public class Modulus
{
    /**
     * Returns a non-negative value for (a mod m) that works particularly well
     * for wrap around boundary conditions. For example, -1 mod 3 == 2, -2 mod 3 =
     * 1, and -3 mod 3 == 0. Also, -4 mod 3 == 3, etc. So for example, if a is
     * ...-6,-5,-4,-3,-2,-1,0,1,2,3,4,5... then this method will return
     * ...0,1,2,0,1,2,0,1,2,0,1,2... when m = 3.
     * 
     * @param a
     *            The number that the modulus will operate upon.
     * @param m
     *            The modulus.
     * @return a mod m.
     */
    public static int mod(int a, int m)
    {
        return (int) (a - Math.floor((double) a / m) * m);
    }

}
