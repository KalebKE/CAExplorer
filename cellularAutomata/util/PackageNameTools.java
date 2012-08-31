/*
 PackageNameTools -- a class within the Cellular Automaton Explorer. 
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

/**
 * Handy tools for working with packages.
 * 
 * @author David Bahr
 */
public class PackageNameTools
{
    /**
     * Converts a package name to a Java file path that uses "/" instead of ".",
     * and prepends a "/" so that the path is absolute.
     * 
     * @param packageName
     *            The name of the package (for example "java.util").
     * 
     * @return the corresponding absolute file path (for example "/java/util").
     */
    public static String packageNameToAbsolutePath(String packageName)
    {
        if(packageName != null)
        {
            if(!packageName.startsWith("/"))
            {
                packageName = "/" + packageName;
            }

            packageName = packageName.replace('.', '/');
        }

        return packageName;
    }

    /**
     * Converts a package name to a Java file path that uses "/" instead of ".".
     * Does not prepend a "/" so that the path is relative.
     * 
     * @param packageName
     *            The name of the package (for example "java.util").
     * 
     * @return the corresponding absolute file path (for example "java/util").
     */
    public static String packageNameToRelativePath(String packageName)
    {
        if(packageName != null)
        {
            packageName = packageName.replace('.', '/');
        }

        return packageName;
    }

    /**
     * Converts a Java path to a package that uses "." instead of "/".
     * 
     * @param path
     *            The path (for example "/java/util").
     * 
     * @return the corresponding package (for example "java.util").
     */
    public static String javaPathToPackageName(String path)
    {
        if(path != null)
        {
            if(path.startsWith("/"))
            {
                path = path.substring(1);
            }

            path = path.replace('/', '.');
        }

        return path;
    }
}
