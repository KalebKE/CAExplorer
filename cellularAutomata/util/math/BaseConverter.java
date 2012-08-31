/*
 BaseConverter -- converts one base to another for the Cellular Automaton Explorer. 
 Copyright (C) 2005  David B. Bahr (http://academic.regis.edu/dbahr/)

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

import java.math.BigInteger;

/**
 * Converts numbers from one base to another.
 * 
 * @author David Bahr
 */
public class BaseConverter
{

    private BaseConverter()
    {
        super();
    }

    /**
     * Converts the given number in base 10 to a number in the base given by the
     * radix. For example, if radix = 2, and num = 6, then this will return the
     * string "1 1 0". All the digits are given as number. So hexadecimal
     * numbers use the symbols 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
     * 15 rather than the letters a, b, c, d, e. Each digit in the number is
     * separated by a space.
     * 
     * @param num
     *            The base 10 number that will be converted.
     * @param radix
     *            The new base.
     * 
     * @return A representation of num in the base given by radix.
     */
    public static String convertBase(BigInteger num, int radix)
    {
        BigInteger bigIntRadix = BigInteger.valueOf(radix);
        if(num.compareTo(bigIntRadix) < 0)
        {
            return new String("" + num);
        }
        else
        {
            return convertBase(num.divide(bigIntRadix), radix)
                + new String(" " + (num.mod(bigIntRadix)));
        }
    }

    /**
     * Converts the given number in base 10 to a number in the base given by the
     * radix. For example, if radix = 2, and num = 6, then this will return the
     * string "1 1 0". All the digits are given as number. So hexadecimal
     * numbers use the symbols 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
     * 15 rather than the letters a, b, c, d, e. Each digit in the number is
     * separated by a space.
     * 
     * @param num
     *            The base 10 number that will be converted.
     * @param radix
     *            The new base.
     * 
     * @return A representation of num in the base given by radix.
     */
    public static String convertBase(int num, int radix)
    {
        if(num < radix)
        {
            return new String("" + num);
        }
        else
        {
            return convertBase(num / radix, radix)
                + new String(" " + (num % radix));
        }
    }

    /**
     * Converts the given number in base 10 to a number in the base given by the
     * radix. For example, if radix = 2, and num = 6, then this will return the
     * string "1 1 0". All the digits are given as number. So hexadecimal
     * numbers use the symbols 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
     * 15 rather than the letters a, b, c, d, e.
     * 
     * @param num
     *            The base 10 number that will be converted.
     * @param radix
     *            The new base.
     * 
     * @return A representation of num in the base given by radix. Each digit of
     *         the number is in a separate array element. The 0th element
     *         contains the lowest order digit.
     */
    public static int[] convertFromBaseTen(BigInteger num, int radix)
    {
        String newNumber = convertBase(num, radix);
        String[] tokens = newNumber.split("\\s");

        int[] digits = new int[tokens.length];
        for(int i = 0; i < tokens.length; i++)
        {
            digits[i] = Integer.parseInt(tokens[tokens.length - 1 - i]);
        }

        return digits;
    }

    /**
     * Converts the given number in base 10 to a number in the base given by the
     * radix. For example, if radix = 2, and num = 6, then this will return the
     * string "1 1 0". All the digits are given as number. So hexadecimal
     * numbers use the symbols 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
     * 15 rather than the letters a, b, c, d, e.
     * 
     * @param num
     *            The base 10 number that will be converted.
     * @param radix
     *            The new base.
     * 
     * @return A representation of num in the base given by radix. Each digit of
     *         the number is in a separate array element. The 0th element
     *         contains the lowest order digit.
     */
    public static int[] convertFromBaseTen(int num, int radix)
    {
        String newNumber = convertBase(num, radix);
        String[] tokens = newNumber.split("\\s");

        int[] digits = new int[tokens.length];
        for(int i = 0; i < tokens.length; i++)
        {
            digits[i] = Integer.parseInt(tokens[tokens.length - 1 - i]);
        }

        return digits;
    }

    /**
     * Converts a string (in the base given by radix) to a base 10 integer. The
     * radix may be 2 to 36. Each digit in the string is given by 0...9 or a...z
     * (or equivalently A...Z). This code uses the constructor
     * BigInteger(string, radix), and additional details may be found there.
     * 
     * @param theNumber
     *            The number represented as a string. May only contain digits
     *            0...9 and a...z (or equivalently A...Z).
     * @param radix
     *            The base of the number (for example, 2 is binary). The base
     *            may be between 2 and 36.
     * 
     * @return The corresponding number in base 10. If the corresponding number
     *         is bigger than a long, then the output is not guaranteed or
     *         specified. See BigInteger for details.
     */
    public static long convertToBaseTen(String theNumber, int radix)
    {
        BigInteger theInteger = new BigInteger(theNumber, radix);
        return theInteger.longValue();
    }
}
