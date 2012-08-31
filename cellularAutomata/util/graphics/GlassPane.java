/*
 GlassPane -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.util.graphics;

import java.awt.Color;
// import java.awt.GradientPaint;
import java.awt.Graphics;
// import java.awt.Graphics2D;
// import java.awt.Paint;
import java.awt.Rectangle;

import javax.swing.JComponent;

/**
 * Graphics that cover the entire CAFrame with a specified transparency.
 * 
 * @author David Bahr
 */
public class GlassPane extends JComponent
{
    // the glass pane transparency (0.0f to 1.0f).
    private float percentTransparent = 1.0f;

    /**
     * Create a glass pane with the given transparency.
     * 
     * @param percentTransparent
     */
    public GlassPane(float percentTransparent)
    {
        this.percentTransparent = percentTransparent;
    }

    /**
     * Paints the glass pane with appropriate transparency. Do not call this
     * method directly. It is called by components as needed. If necessary, this
     * method will be invoked when calling repaint().
     */
    protected void paintComponent(Graphics g)
    {
        Rectangle clip = g.getClipBounds();
        Color alphaWhite = new Color(1.0f, 1.0f, 1.0f,
            1.0f - percentTransparent);
        g.setColor(alphaWhite);
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
    }
}
