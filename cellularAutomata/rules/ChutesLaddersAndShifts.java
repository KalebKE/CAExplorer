/*
 ChutesLaddersAndShifts -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.rules.templates.IntegerRuleTemplate;
import cellularAutomata.util.MinMaxBigIntPair;
import cellularAutomata.util.MinMaxIntPair;

/**
 * A rule which produces chutes, ladders, ray guns, and a wide variety of other
 * behaviors depending on the number of states. For some of the best ladders,
 * choose a square (8-neighbor) 100 by 100 lattice with 35 states and a 50%
 * random initial state. Run this simulation for 600 or more time steps to see
 * ever changing chutes and ladders. Other choices for the number of states look
 * like Majority Probably Wins, simple translations, or premature death. In
 * one-dimension, the rule produces complex streams of colliding data that
 * usually end in Class II cycles.
 * </p>
 * <p>
 * How's the rule work? The neighbors (including the cell itself) are
 * concatenated into a single value. This value is converted to a base 10
 * integer. The neighbor that corresponds to that integer (modulo the number of
 * neighbors) is selected as the cell's new value.
 * </p>
 * <p>
 * For example, in one dimension with nearest neighbors and two states (0 and
 * 1), the cell may have value 0 and the left and right neighbors may each have
 * values 1. These concatenate to 101 in base 2, which is the integer 5 in base
 * 10. There are three neighbors (including the cell itself) which can be
 * numbered 0, 1, and 2. Because 5 mod 3 = 2, the 2nd cell is selected. The 2nd
 * cell is the right neighbor which has value 1. Therefore, the cell's new value
 * will be 1.
 * </p>
 * <p>
 * In two dimensions, the cells are numbered clockwise starting at the upper
 * left (see the tooltip descriptions of the lattices). However, as in the
 * one-dimensional example above, the cell itself is inserted into the middle of
 * the neighbors. For example, on a square (8 neighbor) lattice, the neighbors
 * are numbered as
 * 
 * <pre>
 *  012
 *  7x3
 *  654
 * </pre>
 * 
 * with the cell x in the middle. When concatenated the cells are arranged as
 * 0123x4567. In the one-dimensional nearest-neighbor lattice, the neighbors are
 * arranged as 0x1 and when concatenated, they are still arranges as 0x1. For
 * obvious reasons, this rule is \"more natural\" on one-dimensional lattices.
 * </p>
 * 
 * @author David Bahr
 */
public class ChutesLaddersAndShifts extends IntegerRuleTemplate
{
	/**
	 * A display name for this class.
	 */
	public static final String RULE_NAME = "Chutes, Ladders, and Shifts";

	// a tooltip description for this rule
	private static final String TOOLTIP = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b> Produces chutes, ladders, ray guns, and a wide variety of other "
			+ "behaviors depending on the number of states.</body></html>";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> <b>For best results</b>, try a square (8-neighbor) 100 by 100 lattice with 35 "
			+ "states and a 50% random initial state. Run this simulation for 600 or more time "
			+ "steps to see ever changing chutes and ladders. "
			+ "<p>"
			+ "The behavior changes dramatically with the "
			+ "number of states, but is generally more interesting for large numbers of states. "
			+ "Compare setting the number of states to 2, 10, 15 (with 20% random), 34, 35, and 36. "
			+ "<p> "
			+ "Also try one-dimensional lattices.  For example, try a \"one-dim (radius r)\" "
			+ "lattice with radius 4, 11 states, a 400 by 400 grid, and a 50% random initial state. "
			+ "Ditto with the number of states set to 13, 15, 23, 29, and other values "
			+ "(experiment!)."
			+ "<p>"
			+ "The maximum number of allowed states is 36.  Concatenating the neighbors "
			+ "to create a number is easy using 0-9 and a-z. The letter \"a\" represents 10, "
			+ "b = 11, c = 12, etc.  After exhausting the alphabet, it gets more annoying to "
			+ "create alternate symbols for concatenation."
			+ leftClickInstructions + rightClickInstructions + "</body></html>";

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
	public ChutesLaddersAndShifts(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);
	}

	/**
	 * Updates the cell based on the selfish rule (see class description).
	 * 
	 * @param cell
	 *            The values of the cell being updated.
	 * @param neighbors
	 *            The values of the neighbors.
	 * @param generation
	 *            The current generation of the CA.
	 * @return A new state for the cell.
	 */
	protected int integerRule(int cellValue, int[] neighbors, int numStates,
			int generation)
	{
		// make a new array out of the neighbors with the cell stuck in the
		// middle. i.e., if have cell = x and neighbors = {1, 0}, then the
		// combined array is 1x0.
		int middlePosition = (int) (neighbors.length / 2.0);
		int[] combinedArray = new int[neighbors.length + 1];
		for(int i = 0; i < middlePosition; i++)
		{
			combinedArray[i] = neighbors[i];
		}
		combinedArray[middlePosition] = cellValue;
		for(int i = middlePosition + 1; i < combinedArray.length; i++)
		{
			combinedArray[i] = neighbors[i - 1];
		}

		// get the neighbors to the cell and put the cell in the middle.
		// Combine as a base-numStates number.
		String neighborsAsBaseNumStatesNumber = "";
		for(int i : combinedArray)
		{
			char charEquivalentInBaseNumStates = Character.forDigit(i,
					numStates);
			neighborsAsBaseNumStatesNumber += charEquivalentInBaseNumStates;
		}

		// convert to an integer in base numStates.
		BigInteger neighborNumber = new BigInteger(
				neighborsAsBaseNumStatesNumber, numStates);

		// the number of neighbors (including the cell itself) as a BigInteger
		BigInteger numberOfNeighbors = new BigInteger("" + combinedArray.length);

		// take modulo the number of neighbors to select that neighbor.
		int neighborPosition = neighborNumber.mod(numberOfNeighbors).intValue();

		// get that neighbor, counting from the left, modulo in case there are
		// not enough neighbors (and there usually will not be)
		int returnValue = combinedArray[neighborPosition];

		return returnValue;
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
	 * A list of lattices with which this Rule will work; in this case, returns
	 * all lattices by default, though child classes may wish to override this
	 * and restrict the lattices with which the child rule will work.
	 * <p>
	 * Well-designed Rules should work with any lattice, but some may require
	 * particular topological or geometrical information (like the lattice gas).
	 * Appropriate strings to return in the array include
	 * SquareLattice.DISPLAY_NAME, HexagonalLattice.DISPLAY_NAME,
	 * StandardOneDimensionalLattice.DISPLAY_NAME, etc. If null, will be
	 * compatible with all lattices.
	 * 
	 * @return A list of lattices compatible with this Rule (returns the display
	 *         names for the lattices). Returns null if compatible with all
	 *         lattices.
	 */
	public String[] getCompatibleLattices()
	{
		return null;
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
		return null;
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
		return null;
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
