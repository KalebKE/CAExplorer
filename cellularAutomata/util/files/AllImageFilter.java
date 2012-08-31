/*
 AllImageFilter -- a class within the Cellular Automaton Explorer. 
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

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * A filter (for JFileChoosers) that only allows images.
 */
public class AllImageFilter extends FileFilter
{
    /**
     * One of this filter's supported image types.
     */
    public final static String BMP = "bmp";

    /**
     * One of this filter's supported image types.
     */
    public final static String GIF = "gif";

    /**
     * One of this filter's supported image types.
     */
    public final static String JPEG = "jpeg";

    /**
     * One of this filter's supported image types.
     */
    public final static String JPG = "jpg";

    /**
     * One of this filter's supported image types.
     */
    public final static String PNG = "png";

    // If I want additional supported file types, add them here. The image types
    // must be supported by Java for reading.
    /**
     * Image types accepted by this filter.
     */
    private final static String[] PERMITTED_FILE_TYPES = {BMP, GIF, JPEG, JPG,
        PNG};

    /**
     * Get the extension of a file in lower case.
     * 
     * @param f
     *            The file whose extension will be determined.
     * 
     * @return The extension, or null if there is no extension.
     */
    public static String getExtension(File f)
    {
        String extension = null;
        if(f != null)
        {
            String fileName = f.getName();
            int i = fileName.lastIndexOf('.');

            if(i > 0 && i < fileName.length() - 1)
            {
                extension = fileName.substring(i + 1).toLowerCase();
            }
        }

        return extension;
    }

    /**
     * A list of accepted file types.
     * 
     * @return the accepted file types.
     */
    public static String[] getPermittedFileTypes()
    {
        return PERMITTED_FILE_TYPES;
    }

    /**
     * Accept only BMP, GIF, JPG, JPEG, or PNG files.
     * 
     * @param f
     *            The file.
     * 
     * @return true if the file is one of the supported image types.
     */
    public boolean accept(File f)
    {
        if(f.isDirectory())
        {
            return true;
        }

        String extension = getExtension(f);
        if(extension != null)
        {
            if(extension.equals(BMP) || extension.equals(GIF)
                || extension.equals(JPEG) || extension.equals(JPG)
                || extension.equals(PNG))
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        return false;
    }

    /**
     * Gets the extension of the path for the given file.
     * 
     * @param filePath
     *            The file whose extension will be determined.
     * 
     * @return The extension, or an empty string if there is no extension.
     */
    public static String getExtension(String filePath)
    {
        String extension = "";
        int i = filePath.lastIndexOf('.');

        if(i > 0 && i < filePath.length() - 1)
        {
            extension = filePath.substring(i + 1).toLowerCase();
        }

        return extension;
    }

    /**
     * The description used for display in the JChooser.
     */
    public String getDescription()
    {
        return "All Images (" + getListOfPermittedImageTypes() + ")";
    }

    /**
     * A descriptive list of permitted image file types.
     */
    public static String getListOfPermittedImageTypes()
    {
        String allTypes = "";
        for(String type : PERMITTED_FILE_TYPES)
        {
            allTypes += "." + type + ", ";
        }

        // get rid of the last extraneous ","
        allTypes = allTypes.substring(0, allTypes.lastIndexOf(","));

        return allTypes;
    }

    /**
     * Decides if the file extension is one of the supported image formats for
     * this image reading filter.
     * 
     * @param extension
     *            The extension being tested, which should not include a "."
     * @return true if the extension matches one of the supported image formats.
     */
    public static boolean isPermittedImageType(String extension)
    {
        boolean supported = false;
        if(extension != null)
        {
            for(int i = 0; i < PERMITTED_FILE_TYPES.length; i++)
            {
                if(extension.equalsIgnoreCase(PERMITTED_FILE_TYPES[i]))
                {
                    supported = true;
                }
            }
        }

        return supported;
    }
}
