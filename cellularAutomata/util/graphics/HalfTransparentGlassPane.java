/*
 HalfTransparentGlassPane -- a class within the Cellular Automaton Explorer. 
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
//import java.awt.GradientPaint;
import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.Paint;
import java.awt.Rectangle;

import javax.swing.JComponent;

/**
 * Graphics that cover the entire CAFrame. Is half transparent, half opaque.
 * Useful to make the frame appear disabled.
 * 
 * @author David Bahr
 */
public class HalfTransparentGlassPane extends JComponent
{
    // When true, makes the glass pane half opaque, half transparent, giving the
    // underlying frame a disabled appearance.
    private boolean viewDisabled = false;

    // the percent of the glass pane that is transparent.
    private static final float percentTransparent = 0.6f;

    /**
     * Paints the glass pane with appropriate colors, etc. Do not call this
     * method directly. It is called by components as needed. If necessary, this
     * method will be invoked when calling repaint().
     */
    protected void paintComponent(Graphics g)
    {
        if(viewDisabled)
        {
            Rectangle clip = g.getClipBounds();
            Color alphaWhite = new Color(1.0f, 1.0f, 1.0f,
                1.0f - percentTransparent);
            g.setColor(alphaWhite);
            g.fillRect(clip.x, clip.y, clip.width, clip.height);
        }

        // Graphics2D g2 = (Graphics2D) g;
        //
        // // Creates a two-stops gradient
        // GradientPaint p;
        // Color topColor = new Color(0.0f, 0.0f, 0.0f,
        // 0.9f);
        // Color bottomColor = new Color(1.0f, 1.0f, 1.0f,
        // 0.9f);
        // p = new GradientPaint(0, 0, topColor, 0, getHeight(),
        // bottomColor);
        //
        // // Saves the state
        // Paint oldPaint = g2.getPaint();
        //
        // // Paints the background
        // g2.setPaint(p);
        // g2.fillRect(0, 0, getWidth(), getHeight());
        //
        // // Restores the state
        // g2.setPaint(oldPaint);
        //
        // // Paints borders, text...
        // super.paintComponent(g);
    }

    /**
     * Makes the glass pane half visible and half opaque. Used to make the
     * underlying JFrame appear disabled. This does not actually disable the
     * components and only makes them appear that way.
     * 
     * @param disabled
     *            When true, makes the frame appear disabled. When false makes
     *            the frame appear enabled.
     */
    public void setViewDisabled(boolean disabled)
    {
        viewDisabled = disabled;
    }
}
