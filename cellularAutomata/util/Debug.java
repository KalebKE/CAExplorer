/*
 Debug -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.util;

import javax.swing.JOptionPane;

/**
 * Strictly for debugging.
 * 
 * @author David Bahr
 */
public class Debug
{
    /**
     * If true turns on the debug messages.
     */
    public final static boolean DEBUG = true;

    /**
     * If true prints to the command line. If false prints to a JOptionPane.
     */
    public final static boolean SYSTEM = false;

    /**
     * Prints a message.
     * 
     * @param thisClass
     *            The class printing the message
     * @param message
     *            The message.
     */
    public static void print(Object thisClass, String message)
    {
        if(DEBUG)
        {
            if(SYSTEM)
            {
                systemPrint(thisClass, message);
            }
            else
            {
                JOptionPane.showMessageDialog(null, thisClass.getClass()
                    .getName()
                    + ": " + message, "Debug", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * Prints a message.
     * 
     * @param thisClass
     *            The class printing the message
     * @param message
     *            The message.
     */
    public static void systemPrint(Object thisClass, String message)
    {
        if(DEBUG)
        {
            System.out.println(thisClass.getClass().getName() + ": " + message);
        }
    }
}
