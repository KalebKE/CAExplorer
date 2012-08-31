/*
 WolframRuleNumber -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.CurrentProperties;
import cellularAutomata.lattice.StandardOneDimensionalLattice;
import cellularAutomata.rules.templates.BinaryRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.MinMaxBigIntPair;

/**
 * Binary rules apply to linear one-dimensional CA. A set of nearest neighbors
 * are mapped to particular bits. In other words, we have to find a mapping for
 * each triplet of bits (the center one would be the current cell to whom the
 * rule applies). For example, 000 might map to 0, 001 to 1, 010 to 1, 011 to 0,
 * 100 to 0, 101 to 1, 110 to 1, and 111 to 0. Taken in order from 111 to 000,
 * the triplets map to 01100110 or the number 102. The number of possible rules
 * is 2^(2^3), or 2^8, or 256.
 * 
 * @author David Bahr
 */
public class WolframRuleNumber extends BinaryRuleTemplate
{
	/**
	 * The maximum allowable rule number with nearest neighbors.
	 */
	public final static int MAX_RULE_NUMBER = 255;

	/**
	 * The minimum allowable rule number with nearest neighbors.
	 */
	public final static int MIN_RULE_NUMBER = 0;

	/**
	 * The default rule number if no other is specified.
	 */
	public final static int DEFAULT_RULE = 90;

	/**
	 * A display name for this class.
	 */
	public static final String RULE_NAME = "Wolfram 1-d Rules";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, try a one-dimensional lattice with rule numbers 30, "
			+ "90, 120, 122, and 126 (and single or random seeds). Large lattices "
			+ "(e.g., 500 x 500) are slow but can produce beautiful fractals. "
			+ leftClickInstructions + rightClickInstructions + "</body></html>";

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b> The classic binary rules numbered by Stephen Wolfram's scheme.</body></html>";

	// the rule
	private int ruleNumber = 0;

	// look-up table that maps neighbor's values to a new value
	// for example rule[0][0][1] = 1 would map 001 to 1.
	private int[][][] rule = new int[2][2][2];

	/**
	 * Gets a number from the properties and sets up an appropriate cellular
	 * automaton rule based on that number.
	 */
	public WolframRuleNumber(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			// get the rule number
			// This may be a narrowing conversion from a BigInteger to
			// an int, but that's ok. We have to restrict the values
			// between 0 and 255 anyway.
			ruleNumber = CurrentProperties.getInstance().getRuleNumber()
					.intValue();

			// if rule number is larger than 255 or less than 0, set at 0
			this.ruleNumber = (ruleNumber > 255 || ruleNumber < 0) ? 0
					: ruleNumber;

			createLookUpTable();
		}
	}

	/**
	 * Create the look-up table from the rule number.
	 */
	private void createLookUpTable()
	{
		rule[0][0][0] = getBit(0);
		rule[0][0][1] = getBit(1);
		rule[0][1][0] = getBit(2);
		rule[0][1][1] = getBit(3);
		rule[1][0][0] = getBit(4);
		rule[1][0][1] = getBit(5);
		rule[1][1][0] = getBit(6);
		rule[1][1][1] = getBit(7);
	}

	/**
	 * Gets the bit of ruleNumber at the specified index.
	 * 
	 * @param index
	 *            The position.
	 * @return A bit.
	 */
	private int getBit(int index)
	{
		String number = Integer.toBinaryString(ruleNumber);

		int length = number.length();

		int bit = 0;

		if(index < length)
		{
			String s = number.substring((length - 1) - index, (length - 1)
					- index + 1);
			bit = Integer.parseInt(s);
		}

		return bit;
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
	protected int binaryRule(int cell, int[] neighbors, int generation)
	{
		int leftValue = neighbors[0];
		int myValue = cell;
		int rightValue = neighbors[1];

		return rule[leftValue][myValue][rightValue];
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
	 * A list of lattices with which this Rule will work. This rule won't work
	 * with lattices that have a variable or unknown number of neighbors.
	 * 
	 * @return A list of lattices compatible with this Rule (returns the display
	 *         names for the lattices). Returns null if compatible with all
	 *         lattices.
	 */
	public String[] getCompatibleLattices()
	{
		String[] lattices = {StandardOneDimensionalLattice.DISPLAY_NAME};

		return lattices;
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
	 *            The number of states allowed for a cell on the lattice.
	 * @return A pair of numbers for the minimum and maximum allowable rule
	 *         numbers.
	 */
	protected MinMaxBigIntPair getMinMaxAllowedRuleNumbers(
			String latticeDescription, int numStates)
	{
		return new MinMaxBigIntPair(0, 255);
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
