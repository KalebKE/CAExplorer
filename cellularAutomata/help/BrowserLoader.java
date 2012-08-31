/*
 BrowserLoader -- a class within the Cellular Automaton Explorer. 
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

import java.lang.reflect.Method;
import javax.swing.JOptionPane;

import cellularAutomata.CAController;
import cellularAutomata.CAConstants;

/**
 * Opens Internet Explorer to display help pages.
 * 
 * @author David Bahr
 */
public class BrowserLoader
{
    // The command to open a browser on mac.
    private static final String MAC_COMMAND = "internetexplorer ";

    // The command to open a browser in windows.
    private static final String WINDOWS_COMMAND = "rundll32 url.dll,FileProtocolHandler ";

    // The command to open a browser on solaris.
    private static final String SUN_COMMAND = "/usr/dt/bin/sdtwebclient ";

    // The command to open a browser on unix.
    private static final String UNIX_COMMAND = MAC_COMMAND;

    /**
     * Display a URL in Internet Explorer.
     * 
     * @param url
     *            The URL to be displayed.
     * 
     * @throws IOException
     *             if could not open the browser.
     */
    public static void displayURL(String url)
    {
        try
        {
            if(System.getProperty("os.name").toLowerCase().startsWith("win"))
            {
                String command = WINDOWS_COMMAND + url;
                Process p = Runtime.getRuntime().exec(command);
            }
            else if(System.getProperty("os.name").toLowerCase().startsWith(
                "sunos"))
            {
                String command = SUN_COMMAND + url;
                Process p = Runtime.getRuntime().exec(command);
            }
            else if(System.getProperty("os.name").toLowerCase().startsWith(
                "mac"))
            {
                Class mrjFileUtils = Class
                    .forName("com.apple.mrj.MRJFileUtils");
                Method openURL = mrjFileUtils.getMethod("openURL",
                    new Class[] {Class.forName("java.lang.String")});
                openURL.invoke(null, new Object[] {url});
            }
            else
            {
                String command = UNIX_COMMAND + url;
                Process p = Runtime.getRuntime().exec(command);
            }
        }
        catch(Exception x)
        {
            printWarning();
        }
    }

    /**
     * Lets the user know that they need to use Internet Explorer. (Yes, I know,
     * sucks. But PowerPoint works only with IE.)
     */
    private static void printWarning()
    {
        String warning = "Apologies... but these pages only work with Internet Explorer, \n"
            + "and the Cellular Automaton Explorer could not find that browser.\n\n"
            + "Please open Internet Explorer and go to the following web site: \n\n"
            + CAConstants.HELP_URL + "\n";

        JOptionPane.showMessageDialog(CAController.getCAFrame().getFrame(),
            warning, "Help Browser", JOptionPane.INFORMATION_MESSAGE);
    }
}
