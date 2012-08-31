/*
 ReflectionTool -- a class within the Cellular Automaton Explorer. 
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

import java.lang.reflect.Constructor;

import javax.swing.JOptionPane;

import cellularAutomata.CAConstants;
import cellularAutomata.analysis.Analysis;
import cellularAutomata.graphics.CAMenuBar;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.OneDimensionalLattice;
import cellularAutomata.rules.Life;
import cellularAutomata.rules.Rule;

/**
 * Reflection methods used by the CA code.
 * 
 * @author David Bahr
 */
public class ReflectionTool
{
	/**
	 * Prevents repeatedly printing the same warning in
	 * instantiateAnalysisFromClassName method.
	 */
	private static boolean hasPrintedAnalysisWarning = false;

	/**
	 * Prevents repeatedly printing the same warning in
	 * getLatticeDescriptionFromClassName method.
	 */
	private static boolean hasPrintedLatticeWarning = false;

	/**
	 * Prevents repeatedly printing the same warning in
	 * instantiateRuleFromClassName method.
	 */
	private static boolean hasPrintedRuleWarning = false;

	/**
	 * Creates a class loader capable of dynamically loading classes that exist
	 * outside of the jar and in the userRules and userAnalyses folder.
	 * <p>
	 * This classLoader must be instantiated only once in the entire CA_Explorer
	 * application! Otherwise, when it loads the exact same class, it will
	 * actually think they are different types! (For example, static variables
	 * will not be static across all of the classes.) So this gets the single
	 * instance available (CAClassLoader uses the singleton design pattern).
	 */
	private static CAClassLoader classLoader = CAClassLoader.getCAClassLoader();

	/**
	 * Uses reflection to get the description of a Lattice from its class name.
	 * 
	 * @param className
	 *            The name of the class (including package). Must end in
	 *            ".class".
	 * @return A description of the Lattice class corresponding to the className
	 *         parameter. Null if the name does not correspond to an appropriate
	 *         Lattice or if the Lattice does not have a default constructor.
	 */
	public static String getLatticeDescriptionFromClassName(String className)
	{
		Lattice lattice = getMinimalLatticeFromClassName(className);

		// now get the display name
		String name = null;
		if(lattice != null)
		{
			name = lattice.getDisplayName();
		}

		return name;
	}

	/**
	 * Uses reflection to get the lattice from its class name.
	 * 
	 * @param className
	 *            The name of the class (including package). Must end in
	 *            ".class".
	 * @return A lattice corresponding to the className parameter. Null if the
	 *         name does not correspond to an appropriate Lattice or if the
	 *         Lattice does not have a constructor that take the correct
	 *         parameters.
	 */
	public static Lattice getLatticeFromClassName(String className,
			String initialStateFilePath, Rule rule)
	{
		// 2 is the standard value; the amount returned, plus an extra for the
		// next generation.
		int maxHistory = rule.getRequiredNumberOfGenerations() + 1;

		// But one-dim lattices should store either the height or the
		// value above -- whichever is greater. Otherwise displays funny.
		try
		{
			String superClass = Class.forName(className).getSuperclass()
					.getName();
			if(superClass.endsWith(".OneDimensionalLattice"))
			{
				maxHistory = OneDimensionalLattice.getMaxStateHistory(rule);
			}
		}
		catch(Exception e)
		{
		}

		Lattice lattice = null;

		// get the path for the Rule class (assumes that the Life class is in
		// the same folder)
		Life life = new Life(true);
		String packagePath = life.getClass().getPackage().getName();
		String rulePath = packagePath + ".Rule";

		try
		{
			// get parameter types for the lattice's constructor
			Class[] parameterTypes = {initialStateFilePath.getClass(),
					Class.forName(rulePath), int.class};

			// get all the constructors
			Constructor[] constructors = Class.forName(className)
					.getDeclaredConstructors();

			// only do this if it is not an interface (like the Lattice
			// interface)
			if(constructors.length != 0)
			{
				// get the constructor that takes these parameters
				Constructor latticeConstructor = Class.forName(className)
						.getConstructor(parameterTypes);

				// Now instantiate the Lattice.
				Object[] constructorParameters = {initialStateFilePath, rule,
						new Integer(maxHistory)};
				lattice = (Lattice) latticeConstructor
						.newInstance(constructorParameters);
			}
		}
		catch(NoSuchMethodException e)
		{
			if(!hasPrintedLatticeWarning)
			{
				// Failed because there is no constructor that takes the
				// required parameters. This will only happen to
				// a developer, not a user.
				String warning = "All Lattice classes are required to have a \n"
						+ "constructor that takes four parameters: \n"
						+ "String, Rule, int, Properties.\n"
						+ className
						+ "\ndoes not have that constructor.";
				JOptionPane.showMessageDialog(null, warning,
						"Developer Warning", JOptionPane.WARNING_MESSAGE);
			}
			hasPrintedLatticeWarning = true;
		}
		catch(Exception e)
		{
			// Don't do anything. This method is supposed to return null
			// when passed a name that doesn't correspond to a lattice. So don't
			// want to do a System.print or anything similar.
		}

		return lattice;
	}

	/**
	 * Uses reflection to get the tooltip for a Lattice from its class name.
	 * 
	 * @param className
	 *            The name of the class (including package). Must end in
	 *            ".class".
	 * @return A tooltip for the Lattice class corresponding to the className
	 *         parameter. Null if the name does not correspond to an appropriate
	 *         Lattice or if the Lattice does not have a default constructor.
	 */
	public static String getLatticeToolTipFromClassName(String className)
	{
		Lattice lattice = getMinimalLatticeFromClassName(className);

		// now get the tooltip
		String tooltip = null;
		if(lattice != null)
		{
			tooltip = lattice.getToolTipDescription();
		}

		return tooltip;
	}

	/**
	 * Uses reflection to get the lattice from its class name, but does not
	 * create the actual arrayList that holds the lattice structure. Simply
	 * instantiates the class so that its methods are accessible. Useful for
	 * calling methods such as getDisplayName() and getNumberOfNeighbors().
	 * 
	 * @param className
	 *            The name of the class (including package). Must end in
	 *            ".class".
	 * @return A lattice corresponding to the className parameter. Null if the
	 *         name does not correspond to an appropriate Lattice or if the
	 *         Lattice does not have a default constructor.
	 */
	public static Lattice getMinimalLatticeFromClassName(String className)
	{
		Lattice lattice = null;
		try
		{
			// get parameter types for the lattice's constructor
			Class[] parameterTypes = {};

			// get all the constructors
			Constructor[] constructors = Class.forName(className)
					.getDeclaredConstructors();

			// only do this if it is not an interface (like the Lattice
			// interface)
			if(constructors.length != 0)
			{
				// get the constructor that takes these parameters
				Constructor ruleConstructor = Class.forName(className)
						.getConstructor(parameterTypes);

				// Now instantiate the Lattice.
				Object[] constructorParameters = {};
				lattice = (Lattice) ruleConstructor
						.newInstance(constructorParameters);
			}
		}
		catch(NoSuchMethodException e)
		{
			if(!hasPrintedLatticeWarning)
			{
				// Failed because there is no constructor that takes the
				// required parameters. This will only happen to
				// a developer, not a user.
				String warning = "All Lattice classes are required to have a \n"
						+ "default constructor that takes no parameters.\n"
						+ className + "\ndoes not have that constructor.";
				JOptionPane.showMessageDialog(null, warning,
						"Developer Warning", JOptionPane.WARNING_MESSAGE);
			}
			hasPrintedLatticeWarning = true;
		}
		catch(Exception e)
		{
			// Don't do anything. This method is supposed to return null
			// when passed a name that doesn't correspond to a lattice. So don't
			// want to do a System.print or anything similar.
		}

		return lattice;
	}

	/**
	 * Uses reflection to get the number of neighbors for a cell on the lattice
	 * specified by its class name (in other words, something like
	 * "TriangularLattice.class").
	 * 
	 * @param className
	 *            The name of the class (including package). Must end in
	 *            ".class".
	 * @return The number of neighbors of a cell. Returns -1 if this value
	 *         changes with location on the lattice.
	 */
	public static int getNumberOfNeighborsFromLatticeClassName(String className)
	{
		// get the minimal lattice
		Lattice lattice = getMinimalLatticeFromClassName(className);

		// now get the number of neighbors -- defaults to -1
		int numNeighbors = -1;
		if(lattice != null)
		{
			numNeighbors = lattice.getNumberOfNeighbors();
		}

		return numNeighbors;
	}

	/**
	 * Uses reflection to get the number of neighbors for a cell on the lattice
	 * specified by its display name (in other words, something like "Square (8
	 * neighbor)"). Hint: the display name is easy to get from the properties.
	 * 
	 * @param latticeDescription
	 *            The display name of the lattice which should be something like
	 *            "Square (8 neighbor)".
	 * @return The number of neighbors of a cell. Returns -1 if this value
	 *         changes with location on the lattice.
	 */
	public static int getNumberOfNeighborsFromLatticeDescription(
			String latticeDescription)
	{
		LatticeHash latticeHash = new LatticeHash();
		String latticeClassName = latticeHash.get(latticeDescription);

		// get the minimal lattice
		Lattice lattice = getMinimalLatticeFromClassName(latticeClassName);

		// now get the number of neighbors -- defaults to -1
		int numNeighbors = -1;
		if(lattice != null)
		{
			numNeighbors = lattice.getNumberOfNeighbors();
		}

		return numNeighbors;
	}

	/**
	 * Uses reflection to create an Analysis from its class name.
	 * 
	 * @param className
	 *            The name of the class (including package). Must end in
	 *            ".class".
	 * @param minimalOrLazyInitialization
	 *            When true, the constructor instantiates an object with as
	 *            small a footprint as possible. When false, the analysis is
	 *            fully constructed, complete with close buttons, display
	 *            panels, etc. If uncertain, set this variable to false.
	 * @return The Analysis class corresponding to the className parameter. Null
	 *         if the name does not correspond to an appropriate Analysis or if
	 *         the Analysis does not have a constructor that takes a Properties
	 *         object as a parameter.
	 */
	private static Analysis instantiateAnalysisFromClassName(String className,
			boolean minimalOrLazyInitialization)
	{
		Analysis analysis = null;
		try
		{
			// don't try to find an inner class (which will have a $)
			if(className.indexOf("$") == -1)
			{
				// get parameter types for the analyses' constructor
				Class[] parameterTypes = {boolean.class};

				// get the constructor that takes a boolean as a
				// parameter.
				Class<?> theClass = null;
				theClass = Class.forName(className, true, classLoader);
				Constructor analysisConstructor = theClass
						.getConstructor(parameterTypes);

				// Now instantiate the Analysis.
				Object[] constructorParameters = {new Boolean(
						minimalOrLazyInitialization)};
				analysis = (Analysis) analysisConstructor
						.newInstance(constructorParameters);
			}
		}
		catch(NoSuchMethodException e)
		{
			if(!hasPrintedAnalysisWarning)
			{
				// Failed because there is no constructor that takes a
				// single Properties parameter. This will only happen to
				// a developer, not a user.
				String warning = "All Analysis classes are required to have a \n"
						+ "constructor that takes a single Properties parameter.\n"
						+ className + "\ndoes not have that constructor.";
				JOptionPane.showMessageDialog(null, warning,
						"Developer Warning", JOptionPane.WARNING_MESSAGE);
			}
			hasPrintedAnalysisWarning = true;
		}
		catch(Error e)
		{
			// Failed because of incorrect class name or package name or ...
			String warning = "The user specified Analysis \""
					+ className
					+ "\" has a fatal error. \n\n"
					+ "The package may not match the folder name, or the class \n"
					+ "name may not match the file name.  Other errors in the \n"
					+ "class may also be the problem.\n\n";
			JOptionPane.showMessageDialog(null, warning, "Developer Warning",
					JOptionPane.WARNING_MESSAGE);
		}
		catch(Exception e)
		{
			// Don't do anything. This method is supposed to return null
			// when passed a name that doesn't correspond to an Analysis. So
			// don't want to do a System.print or anything similar.
		}

		return analysis;
	}

	/**
	 * Uses reflection to create an Analysis from its class name with a minimal
	 * memory footprint. This method should be used when we only want to query
	 * the analysis for its name, tooltip, etc and are not planning to run the
	 * analysis.
	 * 
	 * @param className
	 *            The name of the class (including package). Must end in
	 *            ".class".
	 * @return The Analysis class corresponding to the className parameter. Null
	 *         if the name does not correspond to an appropriate Analysis or if
	 *         the Analysis does not have a constructor that takes a Properties
	 *         object as a parameter.
	 */
	public static Analysis instantiateMinimalAnalysisFromClassName(
			String className)
	{
		return instantiateAnalysisFromClassName(className, true);
	}

	/**
	 * Uses reflection to create an Analysis from its class name. Use this
	 * method when the analysis will actually be run and isn't being queried
	 * just for it's display name or tooltip (etc).
	 * 
	 * @param className
	 *            The name of the class (including package). Must end in
	 *            ".class".
	 * @return The Analysis class corresponding to the className parameter. Null
	 *         if the name does not correspond to an appropriate Analysis or if
	 *         the Analysis does not have a constructor that takes a Properties
	 *         object as a parameter.
	 */
	public static Analysis instantiateFullAnalysisFromClassName(String className)
	{
		return instantiateAnalysisFromClassName(className, false);
	}

	/**
	 * Uses reflection to create a Rule from its class name. Use this method
	 * when the rule will actually be run and isn't being queried just for it's
	 * display name or tooltip (etc).
	 * 
	 * @param className
	 *            The name of the class (including package). Must end in
	 *            ".class".
	 * @return The Rule class corresponding to the className parameter. Null if
	 *         the name does not correspond to an appropriate Rule or if the
	 *         Rule does not have a constructor that takes a Properties object
	 *         as a parameter.
	 */
	public static Rule instantiateFullRuleFromClassName(String className)
	{
		return instantiateRuleFromClassName(className, false);
	}

	/**
	 * Uses reflection to create a Rule from its class name with a minimal
	 * memory footprint. This method should be used when we only want to query
	 * the rule for its name, tooltip, etc and are not planning to run the rule.
	 * 
	 * @param className
	 *            The name of the class (including package). Must end in
	 *            ".class".
	 * @return The Rule class corresponding to the className parameter. Null if
	 *         the name does not correspond to an appropriate Rule or if the
	 *         Rule does not have a constructor that takes a Properties object
	 *         as a parameter.
	 */
	public static Rule instantiateMinimalRuleFromClassName(String className)
	{
		return instantiateRuleFromClassName(className, true);
	}

	/**
	 * Uses reflection to create a Rule from its class name.
	 * 
	 * @param className
	 *            The name of the class (including package). Must end in
	 *            ".class".
	 * @param minimalOrLazyInitialization
	 *            When true, the constructor instantiates an object with as
	 *            small a footprint as possible. When false, the rule is fully
	 *            constructed. If uncertain, set this variable to false.
	 * @return The Rule class corresponding to the className parameter. Null if
	 *         the name does not correspond to an appropriate Rule or if the
	 *         Rule does not have a constructor that takes a Properties object
	 *         as a parameter.
	 */
	private static Rule instantiateRuleFromClassName(String className,
			boolean minimalOrLazyInitialization)
	{
		Rule rule = null;
		try
		{
			// don't try to find an inner class (which will have a $)
			if(className != null && className.indexOf("$") == -1)
			{
				// get parameter types for the rule's constructor
				Class[] parameterTypes = {boolean.class};

				// get the constructor that takes a boolean as a
				// parameter. Note this uses my class loader so can see classes
				// in the userAnalyses and userRules folders
				Class<?> theClass = Class.forName(className, true, classLoader);
				Constructor ruleConstructor = theClass
						.getConstructor(parameterTypes);

				// Now instantiate the Rule.
				Object[] constructorParameters = {new Boolean(
						minimalOrLazyInitialization)};
				rule = (Rule) ruleConstructor
						.newInstance(constructorParameters);
			}
		}
		catch(NoSuchMethodException e)
		{
			if(!hasPrintedRuleWarning)
			{
				// What the heck is this? It prevents an annoying error message
				// generated when the user updates from version 4.1 to a newer
				// version without first uninstalling the older version. I
				// changed the name of a userRules class from ExampleAverage to
				// ExampleRealAverage, but the old class does not get deleted.
				// Therefore, it has the old constructor style and throws this
				// exception.
				if(!className.equals("userRules.ExampleAverage"))
				{
					// Failed because there is no constructor that takes a
					// single Properties parameter. This will only happen to
					// a developer, not a user.
					String warning = "All rules in version "
							+ CAConstants.VERSION
							+ " are required to have a constructor \n"
							+ "that takes a single boolean parameter. The rule \n\n"
							+ className
							+ "\n\n"
							+ "does not have that constructor. For more details, see \n"
							+ "the help menu: \""
							+ CAMenuBar.CA_RULE_CONSTRUCTORS + "\"\n\n";
					JOptionPane.showMessageDialog(null, warning,
							"Developer Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
			hasPrintedRuleWarning = true;
		}
		catch(Error e)
		{
			// Failed because of incorrect class name or package name or ...
			String warning = "The user specified Rule \""
					+ className
					+ "\" has a fatal error. \n\n"
					+ "The package may not match the folder name, or the class \n"
					+ "name may not match the file name.  Other errors in the \n"
					+ "class may also be the problem.\n\n";
			JOptionPane.showMessageDialog(null, warning, "Developer Warning",
					JOptionPane.WARNING_MESSAGE);
		}
		catch(Exception e)
		{
			// Don't do anything. This method is supposed to return null
			// when passed a name that doesn't correspond to a Rule. So don't
			// want to do a System.print or anything similar.
		}

		return rule;
	}
}
