/*
 ExampleVectorAverage -- a class within the Cellular Automaton Explorer. 
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

package userRules;

import cellularAutomata.rules.templates.RealVectorRuleTemplate;

/**
 * Finds the average of a cell and all of its neighbors. Each cell has a real
 * (floating point) value assigned. <br>
 * This rather boring class was primarily written as an example of extending the
 * RealVectorRuleTemplate class. Note this class needed each cell to store a
 * single real value. In other words, this class needed each cell to store an
 * array of length 1. So the implementation of the getVectorLength() method
 * returns 1. If we had desired a different graphical display, the setView()
 * method from the parent could have been called (in the constructor). See the
 * ColorAverage class for an example of this. We could have also restricted the
 * lattices for which this rule works by overriding the getCompatibleLattices
 * (again see the ColorAverage class). <br>
 * This same class could have been written by extending the Rule class, but that
 * would have required additional work. The RealVectorRuleTemplate and other
 * "RuleTemplate" classes take care of many details (at the loss of some
 * flexibility).
 * 
 * @author David Bahr
 */
public class ExampleVectorAverage extends RealVectorRuleTemplate
{
	// The maximum allowed value. DO NOT recommend using Double.MAX_VALUE
	// unless carefully checking for infinity.
	private static final double MAX_VALUE = 1000.0;

	// The minimum allowed value. DO NOT recommend using Double.MIN_VALUE
	// unless carefully checking for infinity.
	private static final double MIN_VALUE = 0.0;

	// a display name for this class
	private static final String RULE_NAME = "Real Vector Average";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, try a random initial population on a 100 by 100 or larger "
			+ "one-dimensional (2 neighbor) lattice.  " + leftClickInstructions
			+ rightClickInstructions + "</body></html>";

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b> Finds the average of a cell and all of its neighbors.</body></html>";

	/**
	 * Create a rule that averages the values of neighboring cells.
	 * <p>
	 * When calling the parent constructor, the minimalOrLazyInitialization
	 * parameter must be included as
	 * <code>super(minimalOrLazyInitialization);</code>. The boolean is
	 * intended to indicate when the constructor should build a rule with as
	 * small a footprint as possible. In order to load rules by reflection, the
	 * application must query this class for information like the display name,
	 * tooltip description, etc. At these times it makes no sense to build the
	 * complete rule which may have a large footprint in memory.
	 * <p>
	 * It is recommended that the constructor and instance variables do not
	 * initialize any memory intensive variables and that variables be
	 * initialized only when first needed (lazy initialization). Or all
	 * initializations in the constructor may be placed in an <code>if</code>
	 * statement.
	 * 
	 * <pre>
	 * if(!minimalOrLazyInitialization)
	 * {
	 *     ...initialize
	 * }
	 * </pre>
	 * 
	 * @param minimalOrLazyInitialization
	 *            When true, the constructor instantiates an object with as
	 *            small a footprint as possible. When false, the rule is fully
	 *            constructed. This variable should be passed to the super
	 *            constructor <code>super(minimalOrLazyInitialization);</code>,
	 *            but if uncertain, you may safely ignore this variable.
	 */
	public ExampleVectorAverage(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);
	}

	/**
	 * Finds the average value of a cell and all of its neighbors.
	 * 
	 * @see cellularAutomata.rules.templates.RealVectorRuleTemplate#doubleVectorRule(
	 *      double[], double[][], int)
	 */
	public double[] doubleVectorRule(double[] cellArray,
			double[][] neighborArrays, int generation)
	{
		double average = cellArray[0];

		// now add in the values of each neighbor
		for(int i = 0; i < neighborArrays.length; i++)
		{
			average += neighborArrays[i][0];
		}

		// now get the average
		average /= (neighborArrays.length + 1);

		// write it as an array
		double[] state = {average};

		return state;
	}

	/**
	 * A brief description (written in HTML) that describes what parameters will
	 * give best results for this rule (which lattice, how many states, etc).
	 * The description will be displayed on the properties panel. Using html
	 * permits line breaks, font colors, etcetera, as described in HTML
	 * resources. Regular line breaks will not work.
	 * <p>
	 * Recommend starting with the title of the rule followed by "For best
	 * results, ...". See Rule 102 for an example.
	 * 
	 * @return An HTML string describing how to get best results from this rule.
	 *         May be null.
	 */
	public String getBestResultsDescription()
	{
		return BEST_RESULTS;
	}

	/**
	 * A brief one or two-word string describing the rule, appropriate for
	 * display in a drop-down list.
	 * 
	 * @return A string no longer than 15 characters.
	 * @see cellularAutomata.rules.Rule#getDisplayName()
	 */
	public String getDisplayName()
	{
		return RULE_NAME;
	}

	/**
	 * Gets the maximum permissible value for each element of the vector. Do not
	 * recommend using Double.MAX_VALUE unless your code very carefully checks
	 * for instances of Infinity. <br>
	 * This method is used by the RealVectorRuleTemplate class to properly
	 * construct a RealValuedVectorState.
	 * 
	 * @return The maximum permissible value.
	 * @see cellularAutomata.rules.templates.RealVectorRuleTemplate#getMaximumPermissibleValue()
	 */
	public double getMaximumPermissibleValue()
	{
		return MAX_VALUE;
	}

	/**
	 * Gets the minimum permissible value for each element of the vector. Do not
	 * recommend using Double.MIN_VALUE unless your code very carefully checks
	 * for instances of Infinity. <br>
	 * This method is used by the RealVectorRuleTemplate class to properly
	 * construct a RealValuedVectorState.
	 * 
	 * @return The minimum permissible value.
	 * @see cellularAutomata.rules.templates.RealVectorRuleTemplate#getMinimumPermissibleValue()
	 */
	public double getMinimumPermissibleValue()
	{
		return MIN_VALUE;
	}

	/**
	 * A brief description (written in HTML) that describes this rule. The
	 * description will be displayed as a tooltip. Using html permits line
	 * breaks, font colors, etcetera, as described in HTML resources. Regular
	 * line breaks will not work.
	 * 
	 * @return An HTML string describing this rule.
	 * @see cellularAutomata.rules.Rule#getToolTipDescription()
	 */
	public String getToolTipDescription()
	{
		return TOOLTIP;
	}

	/**
	 * Gets the length of the vectors (arrays) that will be used by the Rule.
	 * The length must be the same for all cells.
	 * 
	 * @return The length of the vector stored by each cell.
	 * @see cellularAutomata.rules.templates.RealVectorRuleTemplate#getVectorLength()
	 */
	public int getVectorLength()
	{
		return 1;
	}
}
