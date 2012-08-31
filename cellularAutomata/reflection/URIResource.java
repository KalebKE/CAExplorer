/*
 URIResource -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2008  David B. Bahr (http://academic.regis.edu/dbahr/)

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

import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;

import cellularAutomata.rules.Life;

/**
 * Finds a resource (file) with the given name and returns as a URI. A URI is a
 * generalization of a URL that includes additional schemes like "mailto:",
 * "ftp:", etc. See the java URI for details.
 * <p>
 * An advantage of URI's (over URL's) is that the getPath() and similar methods
 * return an unencoded and unescaped version of the path. Escaped characters are
 * illegal characters that are replaced with %. For example, a space in a URL
 * would be replaced with a %20. So for example, the regular URL might be
 * C:/Documents%20and%20Settings/Life.ca but the URI getPath() method will
 * instead return C:/Documents and Settings/Life.ca. Some operating systems will
 * need the unescaped version.
 * 
 * @author David Bahr
 */
public class URIResource
{
    // the name of the class in the usersOtherClasses folder that is used to
    // load file resources.
    private static String resourceLoaderClassName = "usersOtherClasses.ExternalResourceLoader";

    /**
     * Finds the resource with the given name and returns as a URI. A URI is a
     * generalization of a URL that includes additional schemes like "mailto:",
     * "ftp:", etc. See the java URI for details.
     * <p>
     * An advantage of URI's (over URL's) is that the getPath() and similar
     * methods return an unencoded and unescaped version of the path. Escaped
     * characters are illegal characters that are replaced with %. For example,
     * a space in a URL would be replaced with a %20. So for example, the
     * regular URL might be C:/Documents%20and%20Settings/Life.ca but the URI
     * getPath() method will instead return C:/Documents and Settings/Life.ca.
     * Some operating systems will need the unescaped version.
     * <p>
     * If the encoded and escaped version is needed then getRawPath() and smilar
     * methods will return the escaped version of the path. For example,
     * C:/Documents%20and%20Settings/Life.ca.
     * <p>
     * A resource is some data (images, audio, text, etc) that can be accessed
     * by class code in a way that is independent of the location of the code.
     * <p>
     * The name of a resource is a '/'-separated path name that identifies the
     * resource.
     * <p>
     * This method will first search the parent class loader for the resource;
     * if the parent is null the path of the class loader built-in to the
     * virtual machine is searched. That failing, this method will invoke
     * findResource(String) to find the resource.
     * <p>
     * Portions of this description are copied from the Classloader API.
     * 
     * @param name
     *            The resource name.
     * 
     * @return The URI of the resource. Null if there was a problem loading the
     *         resource.
     */
    public static URI getResource(String name)
    {
        // get the URL (searches the classpath to find the
        // file). Note: must use ExternalResourceLoader so that it uses
        // CAClassLoader rather than the system default class loader. It is
        // forced to use CAClassLoader because ExternalResourceLoader lives
        // outside the jar and can't be found by the standard class loader.
        URL url = null;
        try
        {
            // get parameter types for the rule's constructor
            Class[] parameterTypes = {};

            // get the constructor that takes a "Properties" object as a
            // parameter. Note this uses my class loader so can see classes
            // in the userAnalyses and userRules folders
            Class theClass = null;
            theClass = Class.forName(resourceLoaderClassName, true,
                CAClassLoader.getCAClassLoader());
            Constructor constructor = theClass.getConstructor(parameterTypes);

            // Now instantiate the external class.
            Object externalResourceLoader = (Object) constructor
                .newInstance(null);

            url = externalResourceLoader.getClass().getResource(name);
        }
        catch(Error e)
        {
            // ExternalResourceLoader failed, so try this rule instead. It is
            // inside the jar.
            url = Life.class.getResource(name);
        }
        catch(Exception e)
        {
            // ExternalResourceLoader failed, so try this rule instead. It is
            // inside the jar.
            url = Life.class.getResource(name);
        }

        // Get rid of any escaped characters (at this point in the code, the URL
        // contains escaped characters like the %20 for a space).
        URI uri = null;
        if(url != null)
        {
            try
            {
                uri = url.toURI();
            }
            catch(Exception e)
            {
                // do nothing
            }
        }

        return uri;
    }
}
