/*
 LatticeHash -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.reflection;

import java.io.File;
import java.io.FileFilter;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;

import cellularAutomata.CAConstants;
import cellularAutomata.lattice.StandardOneDimensionalLattice;
import cellularAutomata.util.PackageNameTools;

/**
 * Keeps a hash table that maps description of Lattices to their class names.
 * Used to instantiate classes (using reflection) after a user chooses the class
 * by its description from combo box).
 * 
 * @author David Bahr
 */
public class LatticeHash implements FileFilter
{
	// the hash of descriptions to class names
	private static Hashtable<String, String> latticeHash = null;

	// the folder holding the Lattice classes
	private File latticeFolder = null;

	// the package name
	private String packageName = null;

	/**
	 * Create a hash table that maps description of Lattices to their class
	 * names.
	 */
	public LatticeHash()
	{
		// only do this if haven't already
		if(latticeHash == null)
		{
			// set the instance variable for the package containing the lattices
			createDefaultPackageName();

			// create the hash table
			latticeHash = new Hashtable<String, String>();

			// load the Lattice classes from the standard Lattice package.
			loadHash();
		}
	}

	/**
	 * Get the default package name for Lattices.
	 */
	private void createDefaultPackageName()
	{
		// get the package name
		try
		{
			Class c = StandardOneDimensionalLattice.class;
			packageName = c.getPackage().getName();
		}
		catch(Exception e)
		{
			System.out.println("Class: LatticeHash.  Method: "
					+ "createDefaultPackageName. Error getting package "
					+ "name for Lattices.");
		}
	}

	/**
	 * Fill the hashtable with description/name pairs for Lattice classes in the
	 * standard default package.
	 */
	private void loadHash()
	{
		// get the folder path (same as package name but with file separator
		// rather than a ".")
		String folderPath = PackageNameTools
				.packageNameToRelativePath(packageName);

		// now create the hash with lattice classes from this folder
		loadHash(folderPath);
	}

	/**
	 * Fill the hashtable with description/name pairs.
	 * 
	 * @param folderPath
	 *            The folder containing Lattice classes.
	 */
	private void loadHash(String folderPath)
	{
		if(folderPath != null)
		{
			// the folder containing Lattice classes
			latticeFolder = new File(folderPath);

			if(latticeFolder.isDirectory())
			{
				// get a list of classes in package/folder
				File[] latticeList = latticeFolder.listFiles(this);

				// instantiate so we can get their descriptions
				for(int i = 0; i < latticeList.length; i++)
				{
					// class name
					String className = latticeList[i].getName();

					// convert to a class name with the package attached
					if((packageName != null) && !packageName.equals(""))
					{
						className = packageName
								+ "."
								+ className
										.substring(0, className.indexOf("."));
					}
					else
					{
						className = className.substring(0, className
								.indexOf("."));
					}

					// get the class description using reflection
					String description = ReflectionTool
							.getLatticeDescriptionFromClassName(className);

					if(description != null)
					{
						// and the mapping
						latticeHash.put(description, className);
					}
				}
			}
			else
			{
				// must not be a package in a directory, must be a package
				// inside of a jar.
				try
				{
					// gets the path from the jar name. Searches the classpath
					// to find the jar file.
					// URL jarURL = LatticeHash.class.getResource("/"
					// + CAConstants.JAR_NAME);
					// String jarPath = jarURL.getPath();

					File f = new File(CAConstants.JAR_NAME);
					String jarPath = f.getAbsolutePath();

					// replaces any %xy that are in the path with their
					// associated character (for example, %20 represents a
					// space)
					jarPath = URLDecoder.decode(jarPath, "UTF-8");

					// open and read the jar file (which is really just a zip
					// file)
					ZipFile zipFile = new ZipFile(jarPath);
					Enumeration e = zipFile.entries();
					while(e.hasMoreElements())
					{
						ZipEntry zipEntry = (ZipEntry) e.nextElement();
						String entryName = zipEntry.getName();
						if(entryName.startsWith(folderPath)
								&& entryName.lastIndexOf("/") <= folderPath
										.length()
								&& entryName.endsWith(".class"))
						{
							String className = entryName.substring(0, entryName
									.length() - 6);
							className = PackageNameTools
									.javaPathToPackageName(className);

							// now the real business
							// get the class description using reflection
							String description = ReflectionTool
									.getLatticeDescriptionFromClassName(className);

							if(description != null)
							{
								// and the mapping
								latticeHash.put(description, className);
							}
						}
					}
				}
				catch(Exception e)
				{
					// invalid folder path
					String warning = "The specified Lattice directory "
							+ latticeFolder
							+ "\ndoes not exist. Please change this path.";
					JOptionPane.showMessageDialog(null, warning,
							"Developer Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
		}
		else
		{
			// null folder path
			String warning = "The specified Lattice directory is null. "
					+ "\nPlease change this path.";
			JOptionPane.showMessageDialog(null, warning, "Developer Warning",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Decides whether a given file should be accepted for the LatticeHash.
	 * 
	 * @param pathName
	 *            The path to the file.
	 */
	public boolean accept(File pathName)
	{
		boolean ok = false;

		if(pathName.isFile())
		{
			String fileName = pathName.getName();

			// make sure it is a java file
			ok = fileName.endsWith(".class");

			if(ok)
			{
				if((packageName != null) && !packageName.equals(""))
				{
					fileName = packageName + "."
							+ fileName.substring(0, fileName.indexOf("."));
				}
				else
				{
					fileName = fileName.substring(0, fileName.indexOf("."));
				}

				// make sure it is a Lattice class.
				if(ReflectionTool.getLatticeDescriptionFromClassName(fileName) == null)
				{
					ok = false;
				}
			}
		}

		return ok;
	}

	/**
	 * Returns true if one or more keys (class descriptions) map to the given
	 * value (a class name).
	 * 
	 * @param className
	 *            The class name (value) whose presence is being tested.
	 * @return true if one or more keys (class descriptions) map to the class
	 *         name.
	 */
	public boolean containsValue(String className)
	{
		return latticeHash.containsValue(className);
	}

	/**
	 * Maps the description of a class (from the Lattice method
	 * getDisplayName()) to the name of the class.
	 * 
	 * @param classDescription
	 *            The description of the class.
	 * @return The class name.
	 */
	public String get(String classDescription)
	{
		return (String) latticeHash.get(classDescription);
	}

	/**
	 * Creates an array of the lattice descriptions (the keys).
	 * 
	 * @return array of lattice descriptions.
	 */
	public String[] toArray()
	{
		String[] stringArrayType = new String[latticeHash.size()];
		return (String[]) latticeHash.keySet().toArray(stringArrayType);
	}

	/**
	 * Creates a list of the lattice descriptions (the keys).
	 * 
	 * @return list of lattice descriptions.
	 */
	public List<String> toList()
	{
		String[] latticeNames = new String[latticeHash.size()];
		latticeHash.keySet().toArray(latticeNames);
		LinkedList<String> list = new LinkedList<String>();

		for(String latticeName : latticeNames)
		{
			list.add(latticeName);
		}

		return list;
	}

	/**
	 * Gets an iterator over the values of the hash table.
	 * 
	 * @return Iterator of values.
	 */
	public Iterator valuesIterator()
	{
		return latticeHash.values().iterator();
	}
}