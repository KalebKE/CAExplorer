/*
 SelfishCA -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.lattice.MooreRadiusOneDimLattice;
import cellularAutomata.lattice.NextNearestOneDimLattice;
import cellularAutomata.lattice.StandardOneDimensionalLattice;
import cellularAutomata.rules.templates.BinaryRuleTemplate;
import cellularAutomata.util.MinMaxBigIntPair;
import cellularAutomata.util.math.Modulus;

/**
 * The neighbors are used to determine what rule is applied to each cell. Each
 * set of neighbors defines a rule number (a la Wolfram -- see the Wolfram
 * Number rule). This rule number then dictates the cell's next value. These are
 * so-called "selfish" CA.
 * <p>
 * In particular, this rule looks at the nearest neighbors and the cell
 * (inbetween them) to get an integer value. For example, the neighbors and the
 * cell might be 110, which is the number 6. Then starting from the neighbor on
 * the far right, it moves left on the lattice that number of spaces. In other
 * words, with 110, it moves left 6 spaces. If it tries to move further left
 * than the number of neighbors, then it wraps around to the rightmost neighbor.
 * So if the lattice is nearest neighbor, then 110 moves 6 spaces left but wraps
 * around twice and ends on the 0. Whatever value it ends on is the new value of
 * the cell.
 * <p>
 * For the nearest-neighbor lattice, this is the same as rule 168. The most
 * interesting behavior is on a next-nearest neighbor lattice.
 * 
 * @author David Bahr
 */
public class SelfishCA extends BinaryRuleTemplate
{
	/**
	 * A display name for this class.
	 */
	public static final String RULE_NAME = "Mostly Selfish";

	// a tooltip description for this rule
	private static final String TOOLTIP = "<html> <body><b>Mostly Selfish.</b> The neighbors "
			+ "are used to determine what rule is applied to each cell.</body></html>";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>Mostly Selfish.</b>"
			+ "<p> <b>For best results</b>, try a next-nearest neighbor lattice with a "
			+ "single seed on a large lattice (e.g. 200 by 200)."
			+ leftClickInstructions + rightClickInstructions + "</body></html>";

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
	public SelfishCA(boolean minimalOrLazyInitialization)
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
	protected int binaryRule(int cell, int[] neighbors, int generation)
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
		combinedArray[middlePosition] = cell;
		for(int i = middlePosition + 1; i < combinedArray.length; i++)
		{
			combinedArray[i] = neighbors[i - 1];
		}

		// get the nearest neighbors to the cell and put the cell in the middle.
		// Combine as a base 2 number.
		int leftNearestNeighborValue = combinedArray[middlePosition - 1];
		int rightNearestNeighborValue = combinedArray[middlePosition + 1];
		String theNearestNeighborNumberInBase2 = "" + leftNearestNeighborValue
				+ "" + cell + "" + rightNearestNeighborValue;

		// convert to an integer base 10 number
		int baseTenNumber = Integer
				.parseInt(theNearestNeighborNumberInBase2, 2);

		// get that neighbor, counting from the right
		int positionOfValueThatWeWillUse = (combinedArray.length - 1)
				- baseTenNumber;

		// Use modulo in case there are not enough neighbors. I use a modulus
		// that wraps back around and does not return negative numbers.
		positionOfValueThatWeWillUse = Modulus.mod(
				positionOfValueThatWeWillUse, combinedArray.length);

		// get the value that the cell will now have
		int returnValue = combinedArray[positionOfValueThatWeWillUse];

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
		String[] lattices = {StandardOneDimensionalLattice.DISPLAY_NAME,
				NextNearestOneDimLattice.DISPLAY_NAME,
				MooreRadiusOneDimLattice.DISPLAY_NAME};

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
