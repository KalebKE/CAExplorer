/*
 AbstractCAFactory -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata;

import cellularAutomata.io.FileStorage;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.view.LatticeView;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.rules.Rule;

/**
 * Creates sets of related objects that work together to create a particular
 * type of CA. For example, it might produce a two-dimensional lattice, the
 * "Life" rules, and a two-dimensional graphics engine. Or it might produce a
 * one-dimensional lattice, rule 60, and a one-dimensional graphics engine. <br>
 * Currently (August 2005), there is only one concrete factory, but this may
 * change. This class provides a simple mechanism for adding additional
 * factories.
 * 
 * @author David Bahr
 */
public abstract class AbstractCAFactory
{
	// the CA rules
	private Rule rule = null;

	/**
	 * Create a factory using parameters set from the properties.
	 */
	public AbstractCAFactory()
	{
	}

	/**
	 * Uses reflection to get the rule from a java class that a user can add to
	 * the program. The class name for the rule is set as a property.
	 */
	protected Rule getRuleFromReflection()
	{
		// the selected rule.
		String ruleClassName = CurrentProperties.getInstance()
				.getRuleClassName();

		// instantiate the rule using reflection
		Rule rule = ReflectionTool
				.instantiateFullRuleFromClassName(ruleClassName);

		return rule;
	}

	/**
	 * Gets an appropriate mechanism for storing data.
	 * 
	 * @return An object with data storage routines (for example,
	 *         one-dimensional with each row storing another generation).
	 */
	public abstract FileStorage getDataStorage();

	/**
	 * Gets the CA lattice.
	 * 
	 * @return A lattice (for example, two-dimensional).
	 */
	public abstract Lattice getLattice();

	/**
	 * Gets the panel that draws the CA.
	 * 
	 * @return The panel that draws the CA (does not include any other graphics
	 *         such as the control panel).
	 */
	public abstract LatticeView getGraphicsPanel();

	/**
	 * Gets the CA rules.
	 * 
	 * @return The rules by which a CA evolves (for example, the one-dimensional
	 *         rule number 102).
	 */
	public Rule getRule()
	{
		// may have already loaded the Rule
		if(rule == null)
		{
			// haven't loaded the rule, so get it now
			rule = getRuleFromReflection();

			if(rule == null)
			{
				String message = "The properties file contains a rule name "
						+ "that does\n"
						+ "not exist.  Please update or delete the properties file,\n"
						+ CAConstants.DEFAULT_PROPERTIES_FILE + ".";
				throw new RuntimeException(message);
			}
		}

		return rule;
	}
}
