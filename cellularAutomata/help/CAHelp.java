/*
 CAHelp -- a class within the Cellular Automaton Explorer. 
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
 * Creates a help browser.
 * 
 * @author David Bahr
 */
public class CAHelp
{
    /**
     * Start the help system.
     * @param parent
     *            The component that spawned this help browser. May be null. Is
     *            used to determine where the browser should shrink as it
     *            closes.
     * @param menuBar
     *            The menu bar, whose methods are used to pause the simulation
     *            before closing the browser. May be null.
     */
    public CAHelp(Component parent, CAMenuBar menuBar)
    {
        // display in either a separate browser (like IE) or the application
        // browser
        boolean displayInBrowser = CurrentProperties.getInstance().isDisplayHyperLinksInBrowser();

        if(displayInBrowser)
        {
            BrowserLoader.displayURL(CAConstants.HELP_URL);
        }
        else
        {
            try
            {
                URL url = new URL(CAConstants.HELP_URL);
                PauseSimulationBrowser browser = new PauseSimulationBrowser(
                    url, true, parent, menuBar);

                // make the animation for closing the frame take this long (in
                // milliseconds)
                browser.setAnimationLength(300);
            }
            catch(Exception e)
            {
                BrowserLoader.displayURL(CAConstants.HELP_URL);
            }
        }

        // load the helpSet (which says what to display in the help)
        /*
         * String helpHS = "help/CAHelp.hs"; HelpSet helpSet = null; ClassLoader
         * cl = CAHelp.class.getClassLoader(); try { URL hsURL =
         * HelpSet.findHelpSet(cl, helpHS); helpSet = new HelpSet(null, hsURL);
         * 
         * //create a HelpBroker (that displays and manages the help browser)
         * HelpBroker hb = helpSet.createHelpBroker();
         * 
         * //display the help JHelp helpViewerComponent = new JHelp(helpSet);
         * 
         * //Create a frame tp hold the help viewer component JFrame frame = new
         * JFrame(); frame.setTitle("Cellular Automaton Explorer Help");
         * frame.setSize(500,500);
         * frame.getContentPane().add(helpViewerComponent);
         * frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         * frame.setVisible(true); } catch(Exception ee) { String message =
         * "Sorry, help contents are unavailable. " + "Please see David Bahr's
         * online \n" + "Regis College class notes for more details about
         * cellular \n" + "automata.";
         * 
         * JOptionPane.showMessageDialog(null, message, "Help",
         * JOptionPane.INFORMATION_MESSAGE);
         * 
         * return; }
         */
    }
}
