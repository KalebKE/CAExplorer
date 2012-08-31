/*
 JPGAndPNGImageReadWrite -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.util.files;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.image.RenderedImage;

import java.io.File;
import java.io.IOException;

import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import cellularAutomata.CAController;
import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.lattice.TwoDimensionalLattice;
import cellularAutomata.lattice.view.LatticeView;
import cellularAutomata.cellState.model.IntegerCellState;

/**
 * Reads and writes images (PNG, JPG).
 * 
 * @author David Bahr
 */
public class JPGAndPNGImageReadWrite
{
    // If I want additional file types, add them here. The types must be
    // supported by Java for *BOTH* reading and writing. As of Java 1.4
    // only JPG and PNG are supported for both. Gif is only supported for
    // reading.
    private final static String[] permittedImageTypes = {"jpg", "png"};

    /**
     * Draws the image from the CAPanel onto a BufferedImage.
     * 
     * @return The buffered image containing the CA graphics.
     */
    private static RenderedImage getImage(LatticeView panel)
    {
        // height and width of the image
        int width = panel.getDisplayWidth();
        int height = panel.getDisplayHeight();

        // Create a buffered image in which to draw
        BufferedImage bufferedImage = new BufferedImage(width, height,
            BufferedImage.TYPE_INT_RGB);

        // get graphics from the buffered image so we can draw on it.
        Graphics2D g2d = bufferedImage.createGraphics();

        // Draw graphics
        panel.draw(g2d);

        return bufferedImage;
    }

    /**
     * Appends a valid image suffix (like .jpg) only if the file path does not
     * already have a valid image suffix.
     * 
     * @param filePath
     *            The path to the file where the image will be saved.
     * 
     * @return The file path with a default image suffix appended if a valid
     *         suffix was not present.
     */
    public static String appendDefaultImageSuffix(String filePath)
    {
        // the dot to add before the suffix
        String dot = ".";

        // does the file already end in a "."?
        if(filePath.endsWith("."))
        {
            dot = "";
        }

        // get the file type (suffix), and if there is none, default to a
        // JPG
        String fileType = JPGAndPNGAndOtherFileTypeFilter.getSuffix(filePath);
        if(fileType.equals(""))
        {
            // create a default file type
            fileType = permittedImageTypes[0];

            // add the file type to the file
            filePath += dot + fileType;
        }
        else if(!isPermittedImageType(fileType))
        {
            // not an approved type, so give it this default value
            fileType = permittedImageTypes[0];

            // add the allowed default file type to the file
            filePath += dot + fileType;
        }

        return filePath;
    }

    /**
     * Decides if the file suffix is one of the supported image formats. Only
     * JPG and PNG are supported.
     * 
     * @param suffix
     *            The suffix being tested, which should not include a "."
     * @return true if the suffix matches one of the supported image formats.
     */
    public static boolean isPermittedImageType(String suffix)
    {
        boolean supported = false;
        if(suffix != null)
        {
            for(int i = 0; i < permittedImageTypes.length; i++)
            {
                if(suffix.equalsIgnoreCase(permittedImageTypes[i]))
                {
                    supported = true;
                }
            }
        }

        return supported;
    }

    /**
     * Reads an image from a file and updates the cells to contain this info.
     * Automatically converts the image to grey scale. Also scales the image to
     * the size of the current CA lattice.
     * 
     * @param filePath
     *            The path to the image that will be read.
     * @param lattice
     *            The CA lattice.
     */
    public static void read(String filePath, TwoDimensionalLattice lattice)
    {
        Image image = null;
        try
        {
            // Read from a file
            File file = new File(filePath);
            image = ImageIO.read(file);
        }
        catch(IOException e)
        {
            String message = "There has been an error loading the image: "
                + e.getMessage();
            JOptionPane.showMessageDialog(CAController.getCAFrame().getFrame(),
                message, "Import image error", JOptionPane.ERROR_MESSAGE);
        }

        // resample the image to the correct size
        int height = lattice.getHeight();
        int width = lattice.getWidth();
        image = image.getScaledInstance(width, height,
            BufferedImage.SCALE_REPLICATE);

        // now get pixels
        int[] pixels = new int[width * height];
        PixelGrabber pixelGrabber = new PixelGrabber(image, 0, 0, width,
            height, pixels, 0, width);

        try
        {
            // this puts the pixels into the array called "pixels"
            pixelGrabber.grabPixels();
        }
        catch(Exception e)
        {
            String message = "There has been an error getting pixels from "
                + "the image: " + e.getMessage();
            JOptionPane.showMessageDialog(null, message, "Import image error",
                JOptionPane.ERROR_MESSAGE);
        }

        int pixelNum = 0;
        Iterator iterator = lattice.iterator();
        while(iterator.hasNext())
        {
            // get cell
            Cell cell = (Cell) iterator.next();

            // get the pixel for that cell
            int cellValue = 0;
            int pixelValue = pixels[pixelNum];
            int red = (pixelValue >> 16) & 0xff;
            int green = (pixelValue >> 8) & 0xff;
            int blue = (pixelValue) & 0xff;
            int sum = red + green + blue;

            int numStates = 2;
            if(IntegerCellState.isCompatibleRule(cell.getRule()))
            {
                numStates = CurrentProperties.getInstance().getNumStates();
            }

            // now use the number of states to convert "sum" to a number between
            // 0 and N-1 (inclusive) where N is the number of states. Note that
            // sum has a max value of 3*255, and a minimum value of 0.
            // But N-1 should be black (not white), so will need to subtract the
            // result from N-1. Therefore...
            cellValue = (numStates - 1)
                - (int) Math.round(((double) sum / (3.0 * 255.0))
                    * (numStates - 1));

            cell.getState().setStateFromString("" + cellValue);

            // increment the pixel that we use
            pixelNum++;
        }
    }

    /**
     * Save the cellular automaton graphics to an image (PNG or JPG).
     * 
     * @param panel
     *            The CA panel that draws the CA graphics.
     * @param filePath
     *            The path to the file where the image will be saved.
     */
    public static void save(LatticeView panel, String filePath)
    {
        // Create an image to save
        RenderedImage rendImage = getImage(panel);

        // Write image to a file
        try
        {
            if(filePath == null)
            {
                throw new IOException("Cannot save without valid file path.");
            }

            // make sure the file suffix is a valid image type like ".jpg"
            filePath = appendDefaultImageSuffix(filePath);

            // get the suffix (file type)
            String fileType = JPGAndPNGAndOtherFileTypeFilter.getSuffix(filePath);

            File file = new File(filePath);

            // save the image
            ImageIO.write(rendImage, fileType, file);
        }
        catch(IOException e)
        {
            String message = "There has been an error saving to an image: "
                + e.getMessage();
            JOptionPane.showMessageDialog(null, message, "Save image error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
