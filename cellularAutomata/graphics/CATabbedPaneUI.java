/*
 CATabbedPaneUI -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2007  David B. Bahr (http://academic.regis.edu/dbahr/)

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

import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * A UI for tabbed panes. The same as a BasicTabbedPaneUI but with an accessor
 * for getting the tabbed pane height.
 * 
 * @author David Bahr
 */
public class CATabbedPaneUI extends BasicTabbedPaneUI
{
    /**
     * Create the UI for the tabbed pane.
     */
    public CATabbedPaneUI()
    {
        super();
    }

    /**
     * The maximum height assigned to a tab.
     * 
     * @return the maximum height of a tab.
     */
    public int getTabHeight()
    {
        return maxTabHeight;
    }
}
