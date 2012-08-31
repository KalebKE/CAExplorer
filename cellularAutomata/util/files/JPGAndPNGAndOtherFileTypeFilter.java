/*
 JPGAndPNGFilter -- a class within the Cellular Automaton Explorer. 
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

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * A file filter that only accepts files with the specified suffix, for example
 * JPG and PNG suffixes.
 */
public class JPGAndPNGAndOtherFileTypeFilter extends FileFilter
{
	// the suffix being permitted by the filter
	private String suffixType = "";

	// description of this filter
	private String description = "All files (*.*)";

	/**
	 * Creates a filter for the provided suffix.
	 * 
	 * @param suffixType
	 *            The suffix such as "JPG" or "PNG" (should not include a ".").
	 * @param description
	 *            A description of the file filter. For example "Images (.jpg)".
	 */
	public JPGAndPNGAndOtherFileTypeFilter(String suffixType, String description)
	{
		if(suffixType != null)
		{
			this.suffixType = suffixType;
		}

		if(description != null)
		{
			this.description = description;
		}
	}

	/**
	 * Accepts the file if it is a JPG or PNG.
	 */
	public boolean accept(File f)
	{
		boolean accept = f.isDirectory();

		if(!accept)
		{
			String suffix = getSuffix(f);

			if(!suffix.equals(""))
			{
				accept = f.isDirectory() || suffix.equals(suffixType);
			}
		}

		return accept;
	}

	/**
	 * Gets the suffix of the path for the given file.
	 * 
	 * @param file
	 *            The file whose suffix will be determined.
	 * @return The suffix, or an empty string if there is no suffix.
	 */
	public static String getSuffix(File file)
	{
		String suffix = "";

		if(file != null)
		{
			String path = file.getPath();
			int i = path.lastIndexOf('.');

			if(i > 0 && i < path.length() - 1)
			{
				suffix = path.substring(i + 1).toLowerCase();
			}
		}

		return suffix;
	}

	/**
	 * Gets the suffix of the path for the given file.
	 * 
	 * @param filePath
	 *            The file whose suffix will be determined.
	 * @return The suffix, or an empty string if there is no suffix.
	 */
	public static String getSuffix(String filePath)
	{
		String suffix = "";
		int i = filePath.lastIndexOf('.');

		if(i > 0 && i < filePath.length() - 1)
		{
			suffix = filePath.substring(i + 1).toLowerCase();
		}

		return suffix;
	}

	/**
	 * A description of the suffix.
	 * 
	 * @return a description of the suffix (not the suffix itself).
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * The suffix, like "JPG".
	 * 
	 * @return the suffix.
	 */
	public String getSuffix()
	{
		return suffixType;
	}
}
