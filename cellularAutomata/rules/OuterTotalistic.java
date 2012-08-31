/*
 OuterTotalistic -- a rule class within the Cellular Automaton Explorer. 
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

package cellularAutomata.rules;

import java.math.BigInteger;

import javax.swing.JOptionPane;

import cellularAutomata.CAController;
import cellularAutomata.CurrentProperties;
import cellularAutomata.error.WarningManager;
import cellularAutomata.lattice.TwoDimensionalLattice;
import cellularAutomata.rules.templates.IntegerRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.MinMaxBigIntPair;
import cellularAutomata.util.math.BaseConverter;

/**
 * Creates an outer totalistic CA rule for a given lattice and rule "code".
 * 
 * @author David Bahr
 */
public class OuterTotalistic extends IntegerRuleTemplate
{
	/**
	 * The default rule number if no other is specified.
	 */
	public final static int DEFAULT_RULE = 90;

	/**
	 * A display name for this class.
	 */
	public static final String RULE_NAME = "Outer Totalistic";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, try starting with a single seed.  In two dimensions "
			+ "this frequently leads to snowflake-like patterns. Try the two-state rule 746, "
			+ "starting with a row of 7 occupied cells on a two-dimensional nearest-neighbor "
			+ "lattice.  Or try rule 736 or 196623 with random initial conditions to "
			+ "see self-organization at work.  Or try rule 224.  This last one should "
			+ "look very familiar, perhaps even life-like :-). "
			+ "<p>Rules formed from 3 or more states are also interesting."
			+ leftClickInstructions + rightClickInstructions + "</body></html>";

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b> Beautiful and often symmetric rules that depend only on the sum of the "
			+ "neighbors and, separately, the cell's value.</body></html>";

	// the number of neighbors
	private int numberOfNeighbors = -1;

	// the number of states
	private int numStates = 2;

	// the rule (updated from the properties)
	private BigInteger ruleNumber = BigInteger.ZERO;

	// look-up table that maps neighbor's values to a new cell value
	// The rule is based on the outer-totalistic code supplied by the user (via
	// the properties object)
	private int[][] rule = null;

	/**
	 * Gets a number from the properties and sets up an appropriate cellular
	 * automaton rule based on that number.
	 * <p>
	 * When calling the parent constructor, the minimalOrLazyInitialization
	 * parameter must be included as shown. The boolean is intended to indicate
	 * when the constructor should build a rule with as small a footprint as
	 * possible. In order to load rules by reflection, the application must
	 * query this class for information like the display name, tooltip
	 * description, etc. At these times it makes no sense to build the complete
	 * rule which may have a large footprint in memory.
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
	public OuterTotalistic(boolean minimalOrLazyInitialization)
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
			int numberOfTransitions)
	{
		boolean maxSizeIsOk = true;

		// if there is a problem, this warning will be assigned a non-null
		// message.
		String warning = null;

		// the number of digits in the proposed maximum sized rule.
		long numberOfDigitsInMaxRule = Long.MAX_VALUE;
		try
		{
			numberOfDigitsInMaxRule = (long) Math.ceil(numberOfTransitions
					* Math.log((double) numberOfStates));

			if(numberOfDigitsInMaxRule > IntegerRule.maxRuleSize)
			{
				warning = "<html>Wow. Really? You have requested so many states for the given "
						+ "lattice, that <br>"
						+ "the total possible number of rules is huge.  The number of DIGITS in <br>"
						+ "the largest possible rule is "
						+ numberOfDigitsInMaxRule
						+ ".  That will almost certainly <br>"
						+ "crash your computer unless you have tremendous amounts of "
						+ "memory.  <br><br>"
						+ "If it's ok with you, I'm going to limit the largest rule number to <br>"
						+ IntegerRule.maxRuleSize
						+ " total digits. <br><br>"
						+ "Limit the size?</html>";
			}
		}
		catch(Throwable e)
		{
			// formatted slightly different because numberOfDigitsInMaxRule is
			// so big (Long.MAX_VALUE)
			warning = "<html>Wow. Really? You have requested so many states for the given "
					+ "lattice, that <br>"
					+ "the total possible number of rules is huge.  The number of DIGITS in <br>"
					+ "the largest possible rule is "
					+ numberOfDigitsInMaxRule
					+ ".  That will almost <br>"
					+ "certainly crash your computer unless you have tremendous amounts of "
					+ "memory.  <br><br>"
					+ "If it's ok with you, I'm going to limit the largest rule number to <br>"
					+ IntegerRule.maxRuleSize
					+ " total digits. <br><br>"
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
						+ " digits.  <br><br>"
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
	 * Create the look-up table from the rule number.
	 */
	private void createLookUpTable()
	{
		// convert the rule number in "base 10" to "base numStates".
		// Note that this array may not be large enough -- may need leading
		// zeroes
		int[] ruleInNewBase = BaseConverter.convertFromBaseTen(ruleNumber,
				numStates);

		// the maximum total that can be achieved by adding together the
		// neighbor's values (exclusive of the cell itself).
		int maxTotal = numberOfNeighbors * (numStates - 1);

		// create an array to hold the rule (add one because the possibilities
		// go from 0 to maxTotal, not 1 to maxTotal). The first array index is
		// for the sum of the neighbors. The second array index (numStates) is
		// for the value of the cell. (Considering these separately is what
		// makes the rule *outer* totalistic.)
		rule = new int[maxTotal + 1][numStates];

		for(int i = 0; i < maxTotal + 1; i++)
		{
			for(int j = 0; j < numStates; j++)
			{
				// The ruleInNewBase may not be large enough because it will not
				// have leading zeroes. So check that here.
				if((2 * i) + j < ruleInNewBase.length)
				{
					rule[i][j] = ruleInNewBase[(2 * i) + j];
				}
				else
				{
					rule[i][j] = 0;
				}
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

		// maxSummationValue is the max sum that can be achieved by adding
		// together all of the neighbors (excluding the cell itself)
		int maxSummationValue = numberOfNeighbors * (numStates - 1);
		try
		{
			// add 1 because possible values go from 0 to maxSummation value
			// (not 1 to maxSummationValue)
			int maxSum = maxSummationValue + 1;

			// multiply by the number of states, because for each sum of the
			// neighbors, the center cell may have that many possible values
			// as well (that's the *outer* totalistic part -- the center cell is
			// considered separately).
			maxSum *= numStates;

			// check that the size of the number isn't going to be unreasonable
			boolean sizeOk = checkSizeOfMaxRule(numStates, maxSum);

			if(sizeOk)
			{
				// now get the total number of possible rules (for each of the
				// possible pairs of {total, cell value} there are numStates
				// possible values that can be assigned to the cell).
				BigInteger numberOfStates = BigInteger.valueOf(numStates);
				numRules = numberOfStates.pow(maxSum);

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
		// add up the values
		int total = 0;
		for(int i = 0; i < neighbors.length; i++)
		{
			total += neighbors[i];
		}

		return rule[total][cell];
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
		String[] folders = {RuleFolderNames.INSTRUCTIONAL_FOLDER,
				RuleFolderNames.CLASSICS_FOLDER,
				RuleFolderNames.OUTER_TOTALISTIC_FOLDER};

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
	 * Gets the lookup table for this rule. The array elements are given as
	 * rule[sumOfNeighbors][cellValue].
	 * 
	 * @return The array of rule values.
	 */
	public int[][] getRule()
	{
		return rule;
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
