/*
 AboutDialog -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.graphics;

import java.net.URL;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import cellularAutomata.CAConstants;
import cellularAutomata.reflection.URLResource;
import cellularAutomata.util.Fonts;

/**
 * Displays an "about" dialog.
 * 
 * @author David Bahr
 */
public class AboutDialog
{
    // license text
    private final static String licenseText = "<html>"
        + CAConstants.PROGRAM_TITLE
        + " -- an educational and research tool for <br>"
        + "investigating cellular automata."
        + "<p><p>"
        + CAConstants.COPYRIGHT
        + "<br> (http://academic.regis.edu/dbahr/)"
        + "<p><p>"
        + "This program is free software; you can redistribute it and/or <br>"
        + "modify it under the terms of the GNU General Public License <br>"
        + "as published by the Free Software Foundation; either version 2 <br>"
        + "of the License, or (at your option) any later version. <br>"
        + "<p><p>"
        + "This program is distributed in the hope that it will be useful, <br>"
        + "but WITHOUT ANY WARRANTY; without even the implied warranty of <br>"
        + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the <br>"
        + "GNU General Public License for more details. <br>"
        + "<p><p>"
        + "To receive a copy of the GNU General Public License write to the <br>"
        + "Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, <br>"
        + "Boston, MA 02110-1301, USA. <br><br> </html>";

    // Warranty info (as html).
    private final static String WARRANTY = "<html>This software comes with "
        + "absolutely no warranty. <br> "
        + "This is free software, and you are welcome to redistribute <br> "
        + "it under certain conditions outlined in the GNU General <br>"
        + "Public License. For more information, select details below.</html>";

    // so cannot instantiate
    private AboutDialog()
    {
        super();
    }

    /**
     * Displays an "About" dialog box with the program title, version number,
     * author name, and license information.
     * 
     * @param frame
     *            The CAFrame that shows all of the CA graphics.
     */
    public static void showAboutDialog(CAFrame frame)
    {
        Fonts fonts = new Fonts();
        JLabel title = new JLabel(CAConstants.PROGRAM_TITLE + ".");
        title.setFont(fonts.getBoldFont());
        JLabel version = new JLabel("Release: " + CAConstants.VERSION + ".");
        JLabel copyright = new JLabel(CAConstants.COPYRIGHT);
        JLabel acknowledgments = new JLabel(CAConstants.ACKNOWLEDGMENTS);
        acknowledgments.setFont(fonts.getPlainFont());
        JLabel space = new JLabel(" ");
        JLabel space2 = new JLabel(" ");
        JLabel space3 = new JLabel(" ");

        JLabel warranty = new JLabel(WARRANTY);
        warranty.setFont(fonts.getItalicSmallerFont());

        JPanel displayPanel = new JPanel();
        Box box = Box.createVerticalBox();
        box.add(title);
        box.add(version);
        box.add(copyright);
        box.add(space);
        box.add(acknowledgments);
        box.add(space2);
        box.add(warranty);
        box.add(space3);
        displayPanel.add(box);

        // get the CA icon image URL (searches the classpath to find the image
        // file).
        URL caIconUrl = URLResource.getResource("/"
            + CAConstants.APPLICATION_ICON_IMAGE_PATH);
        ImageIcon icon = null;
        if(caIconUrl != null)
        {
            icon = new ImageIcon(caIconUrl);
        }

        // show them the message
        // int nowWhat = JOptionPane.showOptionDialog(frame.getFrame(),
        // displayPanel, "About", JOptionPane.YES_NO_OPTION,
        // JOptionPane.INFORMATION_MESSAGE, icon, new String[] {"Details",
        // "Exit"}, null);

        String details = "Details";
        JOptionPane optionDialog = new JOptionPane(displayPanel,
            JOptionPane.INFORMATION_MESSAGE, JOptionPane.YES_NO_OPTION, icon,
            new String[] {details, "Exit"}, null);

        javax.swing.JDialog dialog = optionDialog.createDialog(
            frame.getFrame(), "About");
        dialog.setVisible(true);

        // do they want more details
        if(optionDialog.getValue() != null
            && optionDialog.getValue().equals(details))
        {
            JLabel licenseDetails = new JLabel(licenseText);
            JOptionPane.showMessageDialog(frame.getFrame(), licenseDetails,
                "License details", JOptionPane.INFORMATION_MESSAGE, icon);
        }
    }
}
