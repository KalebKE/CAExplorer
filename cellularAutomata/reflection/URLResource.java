/*
 URLResource -- a class within the Cellular Automaton Explorer. 
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

import java.lang.reflect.Constructor;
import java.net.URL;

import cellularAutomata.rules.Life;

/**
 * Finds a resource (file) with the given name.
 * 
 * @author David Bahr
 */
public class URLResource
{
    // the name of the class in the usersOtherClasses folder that is used to
    // load file resources.
    private static String resourceLoaderClassName = "usersOtherClasses.ExternalResourceLoader";

    /**
     * Finds the resource with the given name.
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
     * This description copied from the Classloader API.
     * 
     * @param name
     *            The resource name.
     * 
     * @return The URL of the resource. Null if there was a problem loading the
     *         resource.
     */
    public static URL getResource(String name)
    {
        // get the URL (searches the classpath to find the
        // file). Note: must use ExternalResourceLoader so that it uses
        // CAClassLoader rather than the system default class loader. It is
        // forced to use CAClassLoader because ExternalResourceLoader lives
        // outside the jar and can't be found by the standard class loader.
        URL url = null;
        try
        {
            // RuleHash ruleHash = new RuleHash(null);
            // Rule rule102 =
            // ReflectionTool.instantiateRuleFromClassName(ruleHash
            // .get(Rule102.RULE_NAME), null);
            //             
            // javax.swing.JOptionPane.showMessageDialog(null, "URLResource:
            // "+ruleHash
            // .get(Rule102.RULE_NAME));
            //            
            // url = rule102.getClass().getResource(name);

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

        return url;
    }
}
