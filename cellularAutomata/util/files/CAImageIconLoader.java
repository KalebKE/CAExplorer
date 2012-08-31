/*
 CAImageIconLoader -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.util.files;

import java.net.URL;

import javax.swing.ImageIcon;

import cellularAutomata.reflection.URLResource;

/**
 * Loads images stored in the CA Explorer's "images" folder.
 * 
 * @author David Bahr
 */
public class CAImageIconLoader
{
    /**
     * The name of the folder that stores images used by the CA Explorer.
     */
    public static final String IMAGES_FOLDER_NAME = "images";

    /**
     * Loads an image from the "images" folder.
     * 
     * @param name
     *            The name of the image file (if the html file is in a subfolder
     *            of the "image" folder, then include the subfolder (for
     *            example, "subfolder/filename.jpg").
     * 
     * @return The image, or null if the path does not work.
     */
    public static ImageIcon loadImage(String name)
    {
        ImageIcon image = null;
        try
        {
            if(name != null)
            {
                // add the "images" folder name
                name = "/" + IMAGES_FOLDER_NAME + "/" + name;

                // get the image URL (searches the classpath to find the
                // image file).
                URL imageIconUrl = URLResource.getResource(name);

                if(imageIconUrl != null)
                {
                    image = new ImageIcon(imageIconUrl);
                }
            }
        }
        catch(Exception e)
        {
            // will return null
            image = null;
        }

        return image;
    }
}
