/*
 imagePreviewer -- a class within the Cellular Automaton Explorer. 
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

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * Displays an image file in a JLabel.
 */
public class ImagePreviewer extends JLabel
{
    public void configure(File f)
    {
        if(f != null)
        {
            //get size of the space where we will stuff the image
	        Dimension size = getSize();
	        Insets insets = getInsets();
	        int width = size.width - insets.left - insets.right;
	        int height = size.height - insets.top - insets.bottom;
	        
            //get the image
	        Image image = null;
	        try
	        {
	           image = ImageIO.read(f);
	        }
	        catch(IOException e)
	        {
	        }
	        
	        //rescale the image
            image = image.getScaledInstance(width, height,
                BufferedImage.SCALE_REPLICATE);
	
	        setIcon(new ImageIcon(image));
        }
        else
        {
            //display nothing
            setIcon(null);
        }
    }
}
