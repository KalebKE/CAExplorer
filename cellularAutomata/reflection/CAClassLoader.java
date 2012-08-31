/*
 CAClassLoader -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.reflection;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import cellularAutomata.CAConstants;

/**
 * Loads CA classes and in particular, loads classes that are in the
 * userAnalyses and userRules folder. First delegates to the default class
 * loader, then if that doesn't work, it uses the methods below.
 * <p>
 * Uses the singleton design pattern to ensure that there is only one class
 * loader. If there are multiple class loaders, then loading the same exact file
 * will create two classes of entirely different types. For example, static
 * variables will not be static across what should have been instances of the
 * same class. And class casting exceptions can occur. For example, "TheClass
 * class1 = (TheClass) class2;" will fail even though class1 and class2 were
 * loaded from the exact same ".class" file. It fails because each class loader
 * will believe that it has loaded a different class.
 * 
 * @author David Bahr
 */
public class CAClassLoader extends ClassLoader
{
    // the single instance
    private static CAClassLoader classLoader = null;

    /**
     * Asks the default class loader to try and load the class.
     */
    private CAClassLoader()
    {
        super();
    }

    /**
     * Gets the single instance of CAClassLoader (uses the singleton design
     * pattern).
     * 
     * @return The single instance of CAClassLoader.
     */
    public static CAClassLoader getCAClassLoader()
    {
        if(classLoader == null)
        {
            classLoader = new CAClassLoader();
        }

        return classLoader;
    }

    /**
     * Create the Class from it's bytes.
     */
    public Class<?> findClass(String name)
    {
        byte[] b = loadClassData(name);
        return defineClass(name, b, 0, b.length);
    }

    /**
     * Load the class as bytes from the specified path to the class.
     * 
     * @param name
     *            Path name of the class.
     * @return The class as a byte stream.
     */
    private byte[] loadClassData(String name)
    {
        byte[] classBytes = null;

        // load the class data from the folder(s)
        try
        {
            // get the complete path
            File f = new File(name);
            String pathName = f.getAbsolutePath();
            pathName = pathName.replace('.', '/');
            pathName += ".class";

            // create a byte array of the correct length
            File classFile = new File(pathName);
            
            long lengthOfFile = classFile.length();

            classBytes = new byte[(int) lengthOfFile];

            // read the data
            FileInputStream inputStream = new FileInputStream(pathName);
            BufferedInputStream bufferedStream = new BufferedInputStream(
                inputStream);
            for(int i = 0; i < lengthOfFile; i++)
            {
                classBytes[i] = (byte) bufferedStream.read();
            }
            bufferedStream.close();
            inputStream.close();
        }
        catch(Exception bummer)
        {
            // do nothing -- couldn't load the class
        }

        return classBytes;
    }

    /**
     * When the default ClassLoader fails to get a file resource, it calls this
     * loader as a last ditch attempt.
     * 
     * @return The URL of the specified file resource (data file, image file,
     *         etc) or null if the file does not exist.
     */
    protected URL findResource(String name)
    {
        URL url = null;
        try
        {
            // get the complete path
            File f = new File(name);
            String pathName = "file:" + f.getAbsolutePath();

            if(f.exists())
            {
                url = new URL(pathName);
            }
        }
        catch(Exception e)
        {
            // do nothing
        }

        return url;
    }
}
