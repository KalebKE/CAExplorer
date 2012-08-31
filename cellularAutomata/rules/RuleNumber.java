/*
 RuleNumber -- a rule class within the Cellular Automaton Explorer. 
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

package cellularAutomata.rules;

import java.math.BigInteger;

import javax.swing.JOptionPane;

import cellularAutomata.CAController;
import cellularAutomata.CurrentProperties;
import cellularAutomata.error.WarningManager;
import cellularAutomata.lattice.OneDimensionalLattice;
import cellularAutomata.lattice.TwoDimensionalLattice;
import cellularAutomata.rules.templates.IntegerRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.MinMaxBigIntPair;
import cellularAutomata.util.MinMaxIntPair;
import cellularAutomata.util.math.BaseConverter;

/**
 * Creates an integer-based rule from a rule number specified by the user. The
 * rule numbers are translated to a rule using the standard numbering scheme, a
 * la Wolfram. These rules are not limited to totalistic schemes or one
 * dimension, However, because it is annoying to parse large bases, the number
 * of states has been restricted to be between 2 and 36 inclusive (can use
 * 0-9a-z to create these numbers). Other than that, the rules are only limited
 * by the available memory (which restricts the size of the rule number to 30000
 * digits or another value specified by the user).
 * <p>
 * Cells are ordered according to the standard conventions. In one dimension,
 * the cell is inserted into the middle such as 0110x0101. In two dimensions,
 * the neighbors are arranged in counter-clockwise order starting from the
 * northwest, and then the cell is tacked on at the end. (See the numbering
 * scheme for lattices in the lattice tool tips.)
 * 
 * @author David Bahr
 */
public class RuleNumber extends IntegerRuleTemplate
{
	/**
	 * The default rule number if no other is specified.
	 */
	public final static int DEFAULT_RULE = 90;

	/**
	 * A display name for this class.
	 */
	public static final String RULE_NAME = "Rule Number";

	// a tooltip description for this rule
	private static final String TOOLTIP = "<html><body> <b>" + RULE_NAME
			+ ". </b>"
			+ "Creates a rule from an integer, using a cellular-automata "
			+ "numbering scheme popularized by Stephen Wolfram.</body></html>";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, use a very small number of states like 2 or 3. "
			+ "Large numbers of states lead to astronomical numbers of possible rules.  "
			+ "<p>"
			+ "For a quick introduction to the huge range of possibilities, try rule "
			+ "numbers 30, 90, 120, 122, and 126 on a one-dim (2 neighbor) lattice with "
			+ "two states.  The rule numbers correspond to Stephen Wolfram's "
			+ "well-known numbering scheme." + leftClickInstructions
			+ rightClickInstructions + "</body></html>";

	// the number of neighbors, excluding the cell itself
	private int numberOfNeighbors = -1;

	// the number of states
	private int numStates = 2;

	// the rule (updated from the properties)
	private BigInteger ruleNumber = BigInteger.ZERO;

	// look-up table that maps neighbors values to a new value
	// for example rule[0][0][1] = 1 would map 001 to 1. But we save this as a
	// 1-d array by putting 000 first, 001 second, 010 third, etc.
	// In general, each rule is given by its base-numStates equivalent.
	// For numStates = 2, the first rule is 000 and this is stored at
	// position 0 in the array. The second rule is 001, and this is stored
	// at position 1. In general, the rule is simple binary arithmetic. For
	// example, 110 is stored at array position 1 * 2^2 + 1 * 2^1 + 0 * 1^0
	// = 6. In base 3, the rules may be like 021 which is
	// stored at array position 0 * 3^2 + 2 * 3^1 + 1 * 3^0 = 7.
	private int[] rule = null;

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
	public RuleNumber(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			// creates instance variables for the rule number, number of states,
			// number of neighbors, etc.
			setInitialState();

			// create the rules
			createLookUpTable();
		}
	}

	/**
	 * The number of rules can get ridiculously large, so this makes sure the
	 * maximum rule number a reasonable size. This method is probably only
	 * useful for classes that allow the user to specify a rule number (in the
	 * rule number JTextField). It checks the length of numberOfStates^maxSum.
	 * 
	 * @param numberOfStates
	 *            The number of states in the simulation.
	 * @param numberOfTransitions
	 *            The number of possible transitions like 001->1, 000->0, etc.
	 * @return true if the size of the maximum rule is ok, false otherwise.
	 */
	private boolean checkSizeOfMaxRule(int numberOfStates,
			BigInteger numberOfTransitions)
	{
		boolean maxSizeIsOk = true;

		// if there is a problem, this warning will be assigned a non-null
		// message.
		String warning = null;

		// the number of digits in the proposed maximum sized rule. This number
		// iss purposefully too big.
		long numberOfDigitsInMaxRule = Long.MAX_VALUE;

		// the length of k^(k^N) in base numStates cannot
		// exceed Integer.MAX_VALUE because that's what holds the array of
		// rule transitions (one for each position in the base-numStates
		// number). Of course, the length of k^(k^N) is just k^N in base-k.
		// That's just the numberOfTransitions.
		//
		// This checks to see if the numberOfTransitions is an int, or if it
		// is a long. If it's an int, then the int value and long value
		// should be the same. If it's too big for an int, then it's too big
		// for instantiating an array of that size.
		if(numberOfTransitions.intValue() == numberOfTransitions.longValue())
		{
			numberOfDigitsInMaxRule = numberOfTransitions.intValue();
		}

		if(numberOfDigitsInMaxRule > IntegerRule.maxRuleSize)
		{
			warning = "<html>You have requested so many states for the given "
					+ "lattice, that <br>"
					+ "the total possible number of rules is huge.  The number of DIGITS in <br>"
					+ "the largest possible rule is "
					+ numberOfDigitsInMaxRule
					+ " (in base "
					+ numberOfStates
					+ ").  That will <br>"
					+ "almost certainly crash your computer unless you have tremendous amounts <br>"
					+ "of memory. <br><br>"
					+ "If it's ok with you, I'm going to limit the largest rule number to <br>"
					+ IntegerRule.maxRuleSize + " total digits. <br><br>"
					+ "Limit the size?</html>";
		}

		if(warning != null)
		{
			int answer = WarningManager.displayWarningWithConfirmDialog(
					warning, 1, null, "Too many rules?",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
					null);

			if(answer == JOptionPane.YES_OPTION)
			{
				maxSizeIsOk = false;
			}
			else if(answer != -1)
			{
				String lastChanceMessage = "<html><body>Are you sure?  Your rules will "
						+ "have up to "
						+ numberOfDigitsInMaxRule
						+ " digits (in base "
						+ numberOfStates
						+ ").  <br><br>"
						+ "Do you <it>really</it> want to risk crashing your computer?"
						+ "</body></html>";
				int lastChanceAnswer = WarningManager
						.displayWarningWithConfirmDialog(lastChanceMessage, 1,
								null, "Crash computer?",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE, null);

				if(lastChanceAnswer == JOptionPane.NO_OPTION
						|| lastChanceAnswer == -1)
				{
					maxSizeIsOk = false;
				}
			}
			else
			{
				// At start-up I don't want to risk anything. At start-up the
				// warning won't be displayed, and it will return -1 (which
				// makes it enter this else clause).
				if(!CAController.doneStartingTheApplication)
				{
					maxSizeIsOk = false;
				}
			}
		}

		return maxSizeIsOk;
	}

	/**
	 * Converts a number to it's base 36 equivalent. In other words, 10 = a, 11 =
	 * b, etc.
	 * 
	 * @param digit
	 *            The integer that will be converted.
	 * @return The equivalent ascii letter.
	 */
	private String convertToBaseThirtySixLetters(int digit)
	{
		String answer = "" + digit;

		if(digit > 9)
		{
			int asciiEquivalent = (digit - 10) + 'a';

			char letter = (char) asciiEquivalent;

			answer = "" + letter;
		}

		return answer;
	}

	/**
	 * Create the look-up table from the rule number.
	 */
	private void createLookUpTable()
	{
		// convert the rule number in "base 10" to "base numStates".
		// Note that this array may not be large enough -- may need leading
		// zeroes
		int[] ruleInNewBase = BaseConverter.convertFromBaseTen(ruleNumber,
				numStates);

		// How big is the rule array? This is the number of neighborhood
		// configurations like 000, 001, 010, 011, etc.
		int numberOfConfigurations = maxRuleSize;
		try
		{
			numberOfConfigurations = (int) Math.pow(numStates,
					numberOfNeighbors + 1);

			if(numberOfConfigurations > maxRuleSize)
			{
				numberOfConfigurations = maxRuleSize;
			}
		}
		catch(Exception e)
		{
			// do nothing (leave numberOfConfigurations = maxRuleSize)
		}

		// create an array to hold the rule. The first array index is
		// for the first neighbor. The second array index is
		// for the second neighbor, etc.
		rule = new int[numberOfConfigurations];

		for(int i = 0; i < rule.length; i++)
		{
			// The ruleInNewBase may not be large enough because it will not
			// have leading zeroes. So check that here.
			if(i < ruleInNewBase.length)
			{
				rule[i] = ruleInNewBase[i];
			}
			else
			{
				rule[i] = 0;
			}
		}
	}

	/**
	 * The largest permitted rule number.
	 * 
	 * @param numStates
	 *            The number of states that a cell can have.
	 * @param numberOfNeighbors
	 *            The number of neighbors for a cell.
	 * @param showWarning
	 *            Whether or not to show a warning when the number of possible
	 *            rules is too big.
	 * @return The largest rule number permitted.
	 */
	private BigInteger getMaxRuleNumber(int numStates, int numberOfNeighbors,
			boolean showWarning)
	{
		BigInteger numRules = BigInteger.ZERO;
		BigInteger numberOfStates = BigInteger.valueOf(numStates);

		// the biggest rule is k^(k^N) - 1 where k is numStates and N is the
		// number of neighbors. So first we calculate k^N.
		BigInteger numberOfTransitions = numberOfStates
				.pow(numberOfNeighbors + 1);
		try
		{
			// check that the size of the number isn't going to be unreasonable
			boolean sizeOk = checkSizeOfMaxRule(numStates, numberOfTransitions);

			if(sizeOk)
			{
				// now get the total number of possible rules
				numRules = numberOfStates.pow(numberOfTransitions.intValue());

				// subtract 1 because we start counting rules from 0
				numRules = numRules.subtract(BigInteger.ONE);
			}
			else
			{
				// get a more "reasonable" max rule number
				BigInteger ten = BigInteger.valueOf(10);
				numRules = ten.pow(IntegerRule.maxRuleSize + 1).subtract(
						BigInteger.ONE);
			}
		}
		catch(Throwable t)
		{
			// there was a problem, so make arbitrarily large
			numRules = BigInteger.valueOf(Long.MAX_VALUE);
		}

		return numRules;
	}

	/**
	 * Sets instance variables read from the properties object.
	 */
	private void setInitialState()
	{
		// get the rule number
		ruleNumber = CurrentProperties.getInstance().getRuleNumber();

		// get the number of states

		numStates = CurrentProperties.getInstance().getNumStates();

		// the number of neighbors for the cell
		// NOTE: this means that this rule won't work with lattices that have a
		// variable or unknown number of neighbors
		numberOfNeighbors = TwoDimensionalLattice
				.getNumberOfNeighbors(CurrentProperties.getInstance()
						.getLatticeDisplayName());
	}

	/**
	 * Updates the cell based on the outer-totalistic rule number ("code")
	 * provided by the constructor.
	 * 
	 * @param cell
	 *            The values of the cell being updated.
	 * @param neighbors
	 *            The values of the neighbors.
	 * @param generation
	 *            The current generation of the CA.
	 * @return A new state for the cell.
	 */
	protected int integerRule(int cell, int[] neighbors, int numStates,
			int generation)
	{
		// arrange the values in the conventional order
		String neighborsConcatenated = "";

		if(OneDimensionalLattice.isCurrentLatticeOneDim())
		{
			// concatenate the neighbors to the left of the cell
			for(int i = 0; i < neighbors.length / 2; i++)
			{
				neighborsConcatenated += convertToBaseThirtySixLetters(neighbors[i]);
			}

			// put the cell in the middle
			neighborsConcatenated += convertToBaseThirtySixLetters(cell);

			// add/concatenate the neighbors to the right of the cell
			for(int i = neighbors.length / 2; i < neighbors.length; i++)
			{
				neighborsConcatenated += convertToBaseThirtySixLetters(neighbors[i]);
			}
		}
		else
		{
			// arrange the neighbors counter-clockwise starting from the upper
			// left
			for(int i : neighbors)
			{
				neighborsConcatenated += convertToBaseThirtySixLetters(i);
			}

			// tack on the cell at the end (as per convention)
			neighborsConcatenated += convertToBaseThirtySixLetters(cell);
		}

		// convert this base-numStates number into a base-10 number
		long number = BaseConverter.convertToBaseTen(neighborsConcatenated,
				numStates);

		// The requested position may not exist if the user asked for a huge
		// cell value. (The array only has length = maxRuleSize, so this is
		// expected.)
		int newCellValue = 0;
		if(number < rule.length)
		{
			newCellValue = rule[(int) number];
		}

		return newCellValue;
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
		// this rule won't work with lattices that have a variable or unknown
		// number of neighbors.
		return super.allLatticeNamesWithConstantNumbersOfNeighbors;
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
		String[] folders = {RuleFolderNames.CLASSICS_FOLDER};

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
		// NOTE: this means that this rule won't work with lattices that have a
		// variable or unknown number of neighbors
		BigInteger maxRule = getMaxRuleNumber(numStates, TwoDimensionalLattice
				.getNumberOfNeighbors(latticeDescription), true);

		return new MinMaxBigIntPair(BigInteger.ZERO, maxRule);
	}

	/**
	 * Gets a pair of numbers for the minimum and maximum allowable states for
	 * the specified lattice. Sub-classes should override this method if the
	 * default min and max is inappropriate. If returns null, then the "Number
	 * of States" text field will be disabled; in other words, the user will be
	 * unable to enter the number of states. It is recommended that in that
	 * case, the programmer should also specify the number of states in
	 * stateValueToDisplay(). Also note that the programmer can still alter the
	 * number of states at any time by setting the value within the rule's code.
	 * For example, the "More Properties" button may allow the user to change
	 * the number of states in a separate field placed there. In that case, the
	 * programmer should be careful to set the property value for the new state
	 * value. For example,
	 * properties.setProperty(CAPropertyReader.NUMBER_OF_STATES, numStates);
	 * 
	 * @param latticeDescription
	 *            The display name of the lattice for which a min and max state
	 *            will be specified.
	 * @return A pair of numbers for the minimum and maximum allowable states.
	 *         May be null if there is no maximum, or if the concept of a
	 *         minimum and maximum does not make sense for this rule.
	 */
	protected MinMaxIntPair getMinMaxAllowedStates(String latticeDescription)
	{
		return new MinMaxIntPair(2, Character.MAX_RADIX);
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
