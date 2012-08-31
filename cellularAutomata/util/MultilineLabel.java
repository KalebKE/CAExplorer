/*
 MultilineLabel -- a class within the Cellular Automaton Explorer. 
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

import javax.swing.LookAndFeel;
import javax.swing.JTextArea;

/**
 * Same as a label, but works for multiple lines with auto-wrapping. Has some
 * display bugs; does not want to size correctly within JPanels. These bugs will
 * be addressed at a later time.
 * 
 * @author David Bahr
 */
public class MultilineLabel extends JTextArea
{
    // whether or not to use the label border
    private boolean useLabelBorder = false;

    /**
     * Create a multiline label from the string. Does not specify a border.
     * 
     * @param s
     *            The label.
     */
    public MultilineLabel(String s)
    {
        super(s);
    }

    /**
     * Create a multiline label from the string.
     * 
     * @param s
     *            The label.
     * @param useLabelBorder
     *            When true, uses the border that looks like a "JLabel".
     */
    public MultilineLabel(String s, boolean useLabelBorder)
    {
        super(s);

        this.useLabelBorder = useLabelBorder;
    }

    /**
     * Alter the look and feel so that the underlying JTextArea looks like a
     * JLabel.
     */
    public void updateUI()
    {
        super.updateUI();

        setLineWrap(true);
        setWrapStyleWord(true);
        // setHighLighter(null);
        setEditable(false);

        if(useLabelBorder)
        {
            LookAndFeel.installBorder(this, "Label.Border");
        }

        LookAndFeel.installColorsAndFont(this, "Label.background",
            "Label.Foreground", "Label.font");
    }
}
