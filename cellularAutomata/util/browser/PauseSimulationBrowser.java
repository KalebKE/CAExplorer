/*
 PauseSimulationBrowser -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.util.browser;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.net.URL;

import cellularAutomata.CurrentProperties;
import cellularAutomata.graphics.CAMenuBar;

/**
 * A very simple html browser that pauses the simulation when closing. (Extends
 * CABrowser and overrides the actionBeforeShrinking() and
 * actionAfterShrinking().)
 * 
 * @author David Bahr
 */
public class PauseSimulationBrowser extends CABrowser
{
	// The menu bar, whose methods are used to pause the simulation
	// before closing the browser. May be null.
	private CAMenuBar menuBar = null;

	/**
	 * Create a browser that pauses the simulation before closing.
	 * 
	 * @param url
	 * @param close
	 * @param parent
	 * @param menuBar
	 *            The menu bar, whose methods are used to pause the simulation
	 *            before closing the browser. May be null.
	 */
	public PauseSimulationBrowser(URL url, boolean close, Component parent,
			CAMenuBar menuBar)
	{
		super(url, close, parent);

		this.menuBar = menuBar;
	}

	// override this method
	public void actionAfterShrinking()
	{
		if(menuBar != null)
		{
			// unpause the simulation.
			menuBar.firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.PAUSE, null, new Boolean(false)));
		}
	}

	// override this method
	public void actionBeforeShrinking()
	{
		if(menuBar != null)
		{// pause the simulation.
			menuBar.firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.PAUSE, null, new Boolean(true)));
		}
	}
}