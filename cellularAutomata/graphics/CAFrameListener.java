/*
 CAFrameListener -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.graphics;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import cellularAutomata.CAShutDown;

/**
 * Behavior when the CA window is closed.
 * 
 * @author David Bahr
 */
public class CAFrameListener extends WindowAdapter
{
    // The frame containing all the graphics.
    private CAFrame frame = null;

    /**
     * Create the listener.
     * 
     * @param frame
     *            The frame containing all he CA graphics.
     */
    public CAFrameListener(CAFrame frame)
    {
        this.frame = frame;
    }

    /**
     * Makes sure the user wants to exit and then sets a property that forces
     * the program to end.
     */
    public void closeWindow()
    {
        CAShutDown.exit(frame);
    }

    /**
     * Closes the application by setting the EXIT property to true.
     */
    public void windowClosing(WindowEvent e)
    {
        closeWindow();
    }
}
