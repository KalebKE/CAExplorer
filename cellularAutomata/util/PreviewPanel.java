/*
 PreviewPanel -- a class within the Cellular Automaton Explorer. 
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

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Creates a panel for previewing an image.
 */
public class PreviewPanel extends JPanel
{
    public PreviewPanel(ImagePreviewer previewer)
    {
        JLabel label = new JLabel("Image Preview", SwingConstants.CENTER);
        setPreferredSize(new Dimension(150, 0));
        setBorder(BorderFactory.createEtchedBorder());

        setLayout(new BorderLayout());

        label.setBorder(BorderFactory.createEtchedBorder());
        add(label, BorderLayout.NORTH);
        add(previewer, BorderLayout.CENTER);
    }
}
