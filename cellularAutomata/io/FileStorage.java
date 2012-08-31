/*
 FileStorage -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;

import cellularAutomata.CAConstants;
import cellularAutomata.CurrentProperties;
import cellularAutomata.lattice.Lattice;

/**
 * Saves cellular automaton data in a file. Child classes will need to handle
 * what data is stored and when (the same data may not be saved every time).
 * This super class opens a file and creates a PrintWriter for writing to the
 * file. The close() method should be called when file writing is complete.
 * 
 * @author David Bahr
 */
public abstract class FileStorage
{
	/**
	 * The string used to denote theend of the properties and the beginning of
	 * the data in the file.
	 */
	public final static String END_PROPERTIES_STRING = "END PROPERTIES";

	// the output stream
	private FileOutputStream outputStream = null;

	// allows writing to the specified file
	private PrintWriter fileWriter = null;

	/**
	 * Creates a file where data will be stored.
	 */
	public FileStorage()
	{
	}

	/**
	 * Gets a file writer to the file specified in the constructor.
	 * 
	 * @return A writer to the file.
	 */
	protected PrintWriter getFileWriter()
	{
		if(fileWriter == null)
		{
			try
			{
				String filePath = CurrentProperties.getInstance()
						.getSaveDataFilePath();
				outputStream = new FileOutputStream(filePath);
				fileWriter = new PrintWriter(outputStream);
			}
			catch(IOException e)
			{
				System.out.println("Class: DataStorage. "
						+ "Constructor. Couldn’t open or create the file.");
			}
		}

		return fileWriter;
	}

	/**
	 * Saves the properties as comments in a file.
	 */
	protected void saveProperties()
	{
		// make sure we have a fileWriter
		if(fileWriter == null)
		{
			getFileWriter();
		}

		Properties properties = CurrentProperties.getInstance().getProperties();

		// get all the keys
		Enumeration propEnum = properties.keys();

		// for each key, save the property
		while(propEnum.hasMoreElements())
		{
			// get the property
			String key = (String) propEnum.nextElement();
			String value = properties.getProperty(key);

			// if we are saving to the facadeSimulations folder, then do not
			// save the save_data_file_path value. This would be confusing
			// because the user could inadvertently overwrite a facade
			// simulation the next time they save a simulation (this facade
			// folder would become the default location). The developer is the
			// only one likely to be saving to this location.
			if(value.contains(CAConstants.FACADE_SIMULATIONS_FOLDER_NAME))
			{
				value = "";
			}

			// escape spaces in the key (this is necessary so that the
			// properties could be loaded by the Properties "load" method.
			// (though the "//" would have to be removed from the front of
			// each line)
			// NO, DON"T DO THIS! I CAN'T READ WITH A NORMAL FILE READER, AND
			// OTHER CHARACTERS LIKE ":" AND "\" IN FILE PATHS ALSO NEED
			// ESCAPING.
			// NIGHTMARISH.
			// key = key.replaceAll(" ", "\\\\ ");

			// save the property
			fileWriter.println("//" + key + "=" + value);
		}

		// extra lines for readability
		fileWriter.println("//");
		fileWriter.println("//" + END_PROPERTIES_STRING);
		fileWriter.println("//");
	}

	/**
	 * Close the file. Any further attempt to write to the file will throw an
	 * exception unless re-opened with getFileWriter().
	 */
	public void close()
	{
		if(fileWriter != null)
		{
			fileWriter.close();
		}

		// set to null, so can be re-opened later (by getFileWriter())
		fileWriter = null;
	}

	/**
	 * Saves cellular automaton data to the file specified in the constructor.
	 * 
	 * @param lattice
	 *            The CA lattice of cells to be saved.
	 */
	public abstract void save(Lattice lattice);
}
