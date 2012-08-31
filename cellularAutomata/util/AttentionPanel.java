/*
 AttentionPanel -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

/**
 * A JPanel that draws a message with color gradient paint to catch the user's
 * attention.
 * 
 * @author David Bahr
 */
public class AttentionPanel extends JPanel
{
    private static final int DEFAULT_PANEL_HEIGHT = 70;

    private static final int DEFAULT_PANEL_WIDTH = 100;

    // the width and height of the panel (with default values)
    private int heightOfPanel = DEFAULT_PANEL_HEIGHT;

    private int widthOfPanel = DEFAULT_PANEL_WIDTH;

    // the message that will be displayed
    private String message = "";

    /**
     * Create a panel with the given message and a default height and width.
     * 
     * @param message
     *            The message that will be displayed
     */
    public AttentionPanel(String message)
    {
        this(message, DEFAULT_PANEL_WIDTH, DEFAULT_PANEL_HEIGHT);
    }

    /**
     * Create a panel with the given message and a default height and width.
     * 
     * @param message
     *            The message that will be displayed
     * @param widthOfPanel
     *            The preferred width of the panel.
     * @param heightOfPanel
     *            The preferred height of the panel.
     */
    public AttentionPanel(String message, int widthOfPanel, int heightOfPanel)
    {
        super();

        this.message = message;

        this.setPreferredSize(new Dimension(widthOfPanel, heightOfPanel));
        this.setMinimumSize(new Dimension(widthOfPanel, heightOfPanel));
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setPaint(new GradientPaint(10, 0, Color.BLUE, 400, 0, Color.BLACK,
            true));

        Fonts fonts = new Fonts();
        g2d.setFont(fonts.getBoldItalicFont(30));

        g2d.drawString(message, 10, heightOfPanel - 20);
    }
}
