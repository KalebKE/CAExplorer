/*
 CAFrameComponentListener -- a class within the Cellular Automaton Explorer. 
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

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Reacts to the CA application frame being resized.
 * 
 * @author David Bahr
 */
public class CAFrameComponentListener extends ComponentAdapter
{
    private CAFrame frame = null;

    /**
     * Create the listener.
     * 
     * @param frame
     *            The class that builds the graphics around a JFrame.
     */
    public CAFrameComponentListener(CAFrame frame)
    {
        this.frame = frame;
    }

    /**
     * React to changes in the window size, etcetera, by resizing the tabbed
     * pane holding the start panel, properties panel, etc.
     */
    public void componentResized(ComponentEvent e)
    {
        // resize the tabbed pane (it needs to resize because the frame has
        // changed size).
        Dimension tabbedPaneSize = frame.getControlPanel().getTabbedPane()
            .getSize();
        Dimension newSize = new Dimension(tabbedPaneSize.width, frame
            .getScrollPane().getHeight());
        frame.getControlPanel().getTabbedPane().setPreferredSize(newSize);
        frame.getControlPanel().getTabbedPane().setMinimumSize(newSize);
        frame.getControlPanel().getTabbedPane().setMaximumSize(newSize);

        // This forces the tabbed pane to update its appearance.
        frame.getControlPanel().getTabbedPane().revalidate();
    }
}
