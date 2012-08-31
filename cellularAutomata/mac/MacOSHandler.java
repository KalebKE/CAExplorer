/*
 MacOSHandler -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.mac;

import cellularAutomata.CAConstants;
import cellularAutomata.CurrentProperties;

import java.lang.reflect.Constructor;


/**
 * Handles all Mac specific code, like system properties and menu features.
 * 
 * @author David Bahr
 */
public class MacOSHandler
{
    /**
     * Handles features required by Mac OS X (in particular Mac OS X has a
     * specific way of dealing with Quit and About menus that Mac users will
     * expect to see).
     */
    public static void setMacOSXSpecificFeatures()
    {
        // deal with the mac Quit and About menu items for OS X operating
        // systems
        if(CAConstants.MAC_OS_X)
        {
            // This makes the "About" menu item and the "Quit" menu item
            // behave properly on a Mac. Doesn't affect other operating
            // systems. Note: this requires the ui.jar in the com.apple.eawt.*
            // package from the Mac version of Java. Therefore, this code is
            // called by reflection because on non-Macs it will fail to find
            // that package. (So can't have a direct reference to this class.)
            //
            // Really, all this code does is say
            // "new MacOSXMenuHandler(properties);"
            try
            {
                // get parameter types for the MacOSXMenuHandler constructor
                Class[] parameterTypes = {CurrentProperties.getInstance().getClass()};

                // get the constructor that takes these parameters
                Constructor macConstructor = Class.forName(
                    "cellularAutomata.mac.MacOSXMenuHandler").getConstructor(
                    parameterTypes);

                // Now instantiate the MacOSXMenuHandler.
                Object[] constructorParameters = {CurrentProperties.getInstance()};
                macConstructor.newInstance(constructorParameters);
            }
            catch(Exception e)
            {
                // If it doesn't work, no biggee. Those menu items will still
                // work, but they'll have the default OSX behavior rather than
                // the behavior that I would define.
            }
        }
    }

    /**
     * Set system properties specific to the Mac operating system. This MUST be
     * called before any GUI components. The CA code works without these system
     * properties, but without these properties the CA will have a "Swing" look
     * instead of the "Mac" look that mac users expect.
     * <p>
     * Note that it does not hurt other OS's to have these properties set.
     */
    public static void setMacSpecificSystemProperties()
    {
        // on a mac, make the menu appear at the top of the window, and not in
        // the application (necessary in older Mac Java versions).
        System.setProperty("com.apple.macos.useScreenMenuBar", "true");

        // on a mac, make the menu appear at the top of the window, and not in
        // the application (necessary in newer Mac Java versions).
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        // on a mac, make the "grow box" not intrude on other components (the
        // thing that lets you drag the window to a larger size.) Necessary in
        // older Mac Java versions.
        System.setProperty("com.apple.mrj.application.growbox.intrudes",
            "false");

        // on a mac, make the "grow box" display (the thing that lets you drag
        // the window to a larger size) Necessary in newer Mac Java versions.
        System.setProperty("apple.awt.showGrowBox", "true");
    }
}
