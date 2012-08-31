/*
 ReversibleRuleNumber -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.rules;

import cellularAutomata.rules.templates.MultiGenerationBinaryRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.MinMaxBigIntPair;

/**
 * Same as WolframRuleNumber but XORs the answer with the cell's state from the
 * previous generation as well. This creates a second-order reversible CA,
 * usually indicated by the standard rule number, followed by an R.
 * 
 * @author David Bahr
 */
public class ReversibleRuleNumber extends MultiGenerationBinaryRuleTemplate
{
	/**
	 * A display name for this class.
	 */
	public static final String RULE_NAME = "Reversible";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, try rule numbers 18, 90, and 102, with blank "
			+ "initial states. Draw a random pattern on the second row, and run. "
			+ leftClickInstructions + rightClickInstructions + "</body></html>";

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b> Rules that run the same both forwards and backwards.</body></html>";

	// the number of generations required to calculate the next generation's
	// state. When 1, that means just the current generation. When 2, that means
	// both the current generation and the previous generation.
	private final int numRequiredGenerations = 2;

	// the regular rule upon which this reversible rule is based
	private WolframRuleNumber regularRuleNumber = null;

	/**
	 * Gets a number from the properties and sets up an appropriate cellular
	 * automaton rule based on that number.
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
	public ReversibleRuleNumber(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			regularRuleNumber = new WolframRuleNumber(false);
		}
	}

	/**
	 * Updates the cell based on the rule number provided by the constructor.
	 * 
	 * @param cell
	 *            The values of the cell being updated.
	 * @param neighbors
	 *            The values of the neighbors.
	 * @param generation
	 *            The current generation of the CA.
	 * @return A new state for the cell.
	 */
	protected int binaryRule(int[] cell, int[][] neighbors, int generation)
	{
		// get the values of the neighbors at the current generation
		int[] neighborValues = new int[neighbors.length];
		for(int i = 0; i < neighbors.length; i++)
		{
			neighborValues[i] = neighbors[i][0];
		}

		// get the non-reversible answer
		int regularAnswer = regularRuleNumber.binaryRule(cell[0],
				neighborValues, generation);

		// return the regular answer XORed with the cell's value at the previous
		// state. This makes it reversible.
		return regularAnswer ^ cell[1];
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
	 * When displayed for selection, the rule will be listed under specific
	 * folders specified here. The rule will always be listed under the "All
	 * rules" folder. And if the rule is contributed by a user and is placed in
	 * the userRules folder, then it will also be shown in a folder called "User
	 * rules". Any strings may be used; if the folder does not exist, then one
	 * will be created with the specified name. If the folder already exists,
	 * then that folder will be used.
	 * <p>
	 * By default, this returns null so that the rule is only placed in the
	 * default folder(s).
	 * <p>
	 * Child classes should override this method if they want the rule to appear
	 * in a specific folder. The "All rules" and "User rules" folder are
	 * automatic and do not need to be specified; they are always added.
	 * 
	 * @return A list of the folders in which rule will be displayed for
	 *         selection. May be null.
	 */
	public String[] getDisplayFolderNames()
	{
		String[] folders = {RuleFolderNames.INSTRUCTIONAL_FOLDER,
				RuleFolderNames.CLASSICS_FOLDER};

		return folders;
	}

	/**
	 * A brief one or two-word string describing the rule, appropriate for
	 * display in a drop-down list.
	 * 
	 * @return A string no longer than 15 characters.
	 */
	public String getDisplayName()
	{
		return RULE_NAME;
	}

	/**
	 * Gets a pair of numbers for the minimum and maximum allowable rule numbers
	 * for the specified lattice.
	 * 
	 * @param latticeDescription
	 *            The display name of the lattice for which a min and max rule
	 *            numbers will be specified.
	 * @param numStates
	 *            The number of state valuess that a cell can have.
	 * @return A pair of numbers for the minimum and maximum allowable rule
	 *         numbers.
	 */
	protected MinMaxBigIntPair getMinMaxAllowedRuleNumbers(
			String latticeDescription, int numStates)
	{
		return new MinMaxBigIntPair(0, 255);
	}

	/**
	 * The number of generations that a Cell must store in order for this rule
	 * to work properly. For example, the rule might need to use states from the
	 * current generation as well as 4 previous generations to calculate the
	 * next generation's state. That means this method should return 5. If only
	 * the current generation is required, then this method should return 1.
	 * <br>
	 * This method returns a default value of 1, but child classes may override
	 * this method to change its value. <br>
	 * Every rule must at least use the current generation to calculate the next
	 * generation, so a value of at least 1 is necessary. The Cell class will
	 * enforce this, with a warning.
	 * 
	 * @return The number of generations that each cell must save.
	 */
	public int getRequiredNumberOfGenerations()
	{
		return numRequiredGenerations;
	}

	/**
	 * A brief description (written in HTML) that describes this rule. The
	 * description will be displayed as a tooltip. Using html permits line
	 * breaks, font colors, etcetera, as described in HTML resources. Regular
	 * line breaks will not work.
	 * 
	 * @return An HTML string describing this rule.
	 */
	public String getToolTipDescription()
	{
		return TOOLTIP;
	}
}
