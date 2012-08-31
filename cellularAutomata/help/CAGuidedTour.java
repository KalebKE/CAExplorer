/*
 CAGuidedTour -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.help;

/*
 * import javax.help.*; import java.net.URL; import javax.swing.JFrame; import
 * javax.swing.JOptionPane;
 */

import java.awt.Component;
import java.net.URL;

import cellularAutomata.CAConstants;
import cellularAutomata.CurrentProperties;
import cellularAutomata.graphics.CAMenuBar;
import cellularAutomata.util.browser.PauseSimulationBrowser;

/**
 * Opens a "Guided Tour" browser page for newbies.
 * 
 * @author David Bahr
 */
public class CAGuidedTour
{
    /**
     * Start the guided tour browser.
     * 
     * @param parent
     *            The component that spawned this help browser. May be null. Is
     *            used to determine where the browser should shrink as it
     *            closes.
     * @param menuBar
     *            The menu bar, whose methods are used to pause the simulation
     *            before closing the browser. May be null.
     */
    public CAGuidedTour(Component parent, CAMenuBar menuBar)
    {
        // display in either a separate browser (like IE) or the application
        // browser
        boolean displayInBrowser = CurrentProperties.getInstance()
                .isDisplayHyperLinksInBrowser();

        if(displayInBrowser)
        {
            BrowserLoader.displayURL(CAConstants.GUIDED_TOUR_URL);
        }
        else
        {
            try
            {
                // get the url
                URL url = new URL(CAConstants.GUIDED_TOUR_URL);

                PauseSimulationBrowser browser = new PauseSimulationBrowser(
                        url, true, parent, menuBar);

                // make the animation for closing the frame take this long (in
                // milliseconds)
                browser.setAnimationLength(300);
            }
            catch(Exception e)
            {
                BrowserLoader.displayURL(CAConstants.GUIDED_TOUR_URL);
            }
        }
    }
}
