/*
 AnalysisHash -- a class within the Cellular Automaton Explorer. 
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;

import cellularAutomata.CAConstants;
import cellularAutomata.CurrentProperties;
import cellularAutomata.analysis.Analysis;
import cellularAutomata.analysis.PercentOccupiedAnalysis;
import cellularAutomata.util.PackageNameTools;

/**
 * Keeps a hash table that maps description of Analyses to their class names.
 * Used to instantiate classes (using reflection) after a user chooses the class
 * by its description from check boxes. This class can find Analyses placed in a
 * directory specified by the user, as well as analyses in the
 * cellularAutomata.analysis (whether the package is in a jar, or in a
 * directory).
 * 
 * @author David Bahr
 */
public class AnalysisHash implements FileFilter
{
	// the hash of descriptions to class names
	private static Hashtable<String, String> analysisHash = null;

	// the folder holding the Analysis classes
	private File analysisFolder = null;

	// the package name for the analyses
	private String packageName = null;

	// the folder where the user is storing analyses; keeps track of its value
	private static String previousUserAnalysisFolder = null;

	/**
	 * The default folder where users may add their own analyses.
	 */
	public final static String DEFAULT_USER_ANALYSIS_FOLDER = "userAnalyses";

	/**
	 * The default package name for user specified analyses.
	 */
	public final static String DEFAULT_USER_ANALYSIS_PACKAGE = "userAnalyses";

	/**
	 * Create a hash table that maps description of Analyses to their class
	 * names.
	 */
	public AnalysisHash()
	{
		// get the folder containing user's analyses
		String analysisFolder = CurrentProperties.getInstance()
				.getUserAnalysisFolder();

		// only do this if haven't already
		if(analysisHash == null)
		{
			// set the instance variable for the package containing the analyses
			createDefaultPackageName();

			// create the hash table
			analysisHash = new Hashtable<String, String>();

			// load the Analysis classes from the standard Analysis package.
			loadHash();
		}

		// only do this if the user specified folder has changed
		if((analysisFolder != null)
				&& (previousUserAnalysisFolder == null || !analysisFolder
						.equals(previousUserAnalysisFolder)))
		{
			// reset for next time through this constructor
			previousUserAnalysisFolder = analysisFolder;

			// now load Analysis classes from the user folder
			if((analysisFolder != null) && !analysisFolder.equals(""))
			{
				// reset the package name (used in the accept() method)
				packageName = CurrentProperties.getInstance()
						.getUserAnalysisPackage();

				loadHash(analysisFolder);
			}
		}
	}

	/**
	 * Get the default package name for analyses.
	 */
	private void createDefaultPackageName()
	{
		// get the package name
		try
		{
			Analysis analysis = new PercentOccupiedAnalysis(true);
			Class c = analysis.getClass();
			packageName = c.getPackage().getName();
		}
		catch(Exception e)
		{
			System.out.println("Class: AnalysisHash.  Method: "
					+ "createDefaultPackageName. Error getting package "
					+ "name for Analyses.");
		}
	}

	/**
	 * Fill the hashtable with description/name pairs for Analysis classes in
	 * the standard default package.
	 */
	private void loadHash()
	{
		// get the folder path (same as package name but with file separator
		// rather than a ".")
		String folderPath = PackageNameTools
				.packageNameToRelativePath(packageName);

		// now create the hash with analysis classes from this folder
		loadHash(folderPath);
	}

	/**
	 * Fill the hashtable with description/name pairs.
	 * 
	 * @param folderPath
	 *            The folder containing Analysis classes.
	 */
	private void loadHash(String folderPath)
	{
		if(folderPath != null)
		{
			// the folder containing Analysis classes
			analysisFolder = new File(folderPath);

			if(analysisFolder.isDirectory())
			{
				// get a list of classes in package/folder
				File[] analysisList = analysisFolder.listFiles(this);

				// instantiate so we can get their descriptions
				for(int i = 0; i < analysisList.length; i++)
				{
					// class name
					String className = analysisList[i].getName();

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

					// instantiate the analysis using reflection
					Analysis analysis = ReflectionTool
							.instantiateMinimalAnalysisFromClassName(className);

					// description
					if(analysis != null)
					{
						String description = analysis.getDisplayName();

						if(description != null)
						{
							// add the mapping
							analysisHash.put(description, className);
						}
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
					// URL jarURL = AnalysisHash.class.getResource("/"
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
							// instantiate the analysis using reflection
							Analysis analysis = ReflectionTool
									.instantiateMinimalAnalysisFromClassName(className);

							// description
							if(analysis != null)
							{
								String description = analysis.getDisplayName();

								if(description != null)
								{
									// and the mapping
									analysisHash.put(description, className);
								}
							}
						}
					}
				}
				catch(Exception e)
				{
					// invalid folder path
					String warning = "The specified Analysis directory "
							+ analysisFolder
							+ "\ndoes not exist. Please change this path.";
					JOptionPane.showMessageDialog(null, warning,
							"Developer Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
		}
		else
		{
			// null folder path
			String warning = "The specified Analysis directory is null. "
					+ "\nPlease change this path.";
			JOptionPane.showMessageDialog(null, warning, "Developer Warning",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Decides whether a given file should be accepted for the AnalysisHash.
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

				// make sure it is an Analysis class.
				try
				{
					// instantiate the analysis using reflection
					Analysis analysis = ReflectionTool
							.instantiateMinimalAnalysisFromClassName(fileName);

					// this will throw an exception if it is null (not an
					// analysis)
					String description = analysis.getDisplayName();
				}
				catch(Exception e)
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
		return analysisHash.containsValue(className);
	}

	/**
	 * Maps the description of a class (from the Analysis method
	 * getDisplayName()) to the name of the class.
	 * 
	 * @param classDescription
	 *            The description of the class.
	 * @return The class name.
	 */
	public String get(String classDescription)
	{
		return (String) analysisHash.get(classDescription);
	}

	/**
	 * Gets an iterator over the values of the hash table.
	 * 
	 * @return Iterator of values.
	 */
	public Iterator valuesIterator()
	{
		return analysisHash.values().iterator();
	}
}
