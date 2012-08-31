/*
 House -- a rule class within the Cellular Automaton Explorer. 
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

import cellularAutomata.lattice.SquareLattice;
import cellularAutomata.rules.templates.IntegerRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.MinMaxIntPair;

/**
 * This surprising CA uses many states to create a house shape.
 * 
 * @author David Bahr
 */
public class House extends IntegerRuleTemplate
{
	/**
	 * A display name for this class.
	 */
	public static final String RULE_NAME = "House";

	// The number of states for this rule (0 to 56)
	private static final int NUMBER_OF_STATES = 57;

	// private static final int[][] houseArray = {
	// {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
	// {0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0},
	// {0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0},
	// {0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0},
	// {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
	// {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
	// {1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 1, 1},
	// {0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0},
	// {0, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 1, 0},
	// {0, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 1, 0},
	// {0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0},
	// {0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0},
	// {0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0},
	// {0, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0},};
	//
	// private static final int[][] houseArray = {
	// {00, 00, 00, 00, 00, 00, 56, 00, 00, 00, 00, 00, 00},
	// {00, 00, 00, 00, 00, 55, 00, 54, 00, 00, 00, 00, 00},
	// {00, 00, 00, 00, 51, 00, 00, 00, 52, 00, 53, 00, 00},
	// {00, 00, 00, 50, 00, 00, 00, 00, 00, 49, 48, 00, 00},
	// {00, 00, 46, 00, 00, 00, 00, 00, 00, 00, 47, 00, 00},
	// {00, 45, 00, 00, 00, 00, 00, 00, 00, 00, 00, 44, 00},
	// {37, 38, 00, 00, 39, 40, 41, 00, 00, 00, 00, 42, 43},
	// {00, 36, 00, 00, 35, 00, 34, 00, 00, 00, 00, 33, 00},
	// {00, 28, 00, 00, 29, 30, 31, 00, 00, 00, 00, 32, 00},
	// {00, 27, 00, 00, 00, 00, 00, 26, 25, 24, 00, 23, 00},
	// {00, 19, 00, 00, 00, 00, 00, 20, 00, 21, 00, 22, 00},
	// {00, 18, 00, 00, 00, 00, 00, 17, 00, 16, 00, 15, 00},
	// {00, 11, 00, 00, 00, 00, 00, 12, 00, 13, 00, 14, 00},
	// {00, 10, 09, 08, 07, 06, 05, 04, 00, 03, 02, 01, 00}};

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, try a 20% random "
			+ "initial state on a 150 by 150 lattice. Also try a single seed "
			+ "for an initial state. In a blank open space, try drawing a single "
			+ "cell with the mouse.<p>"
			+ "Also try drawing a line nine cells wide. This will create a "
			+ "two-dimensional house." + leftClickInstructions
			+ rightClickInstructions + "</body></html>";

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>" + RULE_NAME
			+ ".</b> Perhaps surprisingly, creates house shapes by "
			+ "using a large number of states.</body></html>";

	/**
	 * Create a rule that builds a house.
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
	public House(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);
	}

	/**
	 * Updates the cell based on rules that create a house shape.
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
		if(cell == 56)
		{
			return 56;
		}
		else if(neighbors[2] == 56)
		{
			return 55;
		}
		else if(neighbors[0] == 56)
		{
			return 54;
		}
		else if(neighbors[5] == 48)
		{
			return 53;
		}
		else if(neighbors[0] == 54)
		{
			return 52;
		}
		else if(neighbors[2] == 55)
		{
			return 51;
		}
		else if(neighbors[2] == 51)
		{
			return 50;
		}
		else if(neighbors[0] == 52)
		{
			return 49;
		}
		else if(neighbors[7] == 49)
		{
			return 48;
		}
		else if(neighbors[0] == 49 && neighbors[1] == 48)
		{
			return 47;
		}
		else if(neighbors[2] == 50)
		{
			return 46;
		}
		else if(neighbors[2] == 46)
		{
			return 45;
		}
		else if(neighbors[0] == 47)
		{
			return 44;
		}
		else if(neighbors[0] == 44)
		{
			return 43;
		}
		else if(neighbors[1] == 44)
		{
			return 42;
		}
		else if(neighbors[5] == 34)
		{
			return 41;
		}
		else if(neighbors[3] == 41)
		{
			return 40;
		}
		else if(neighbors[3] == 40)
		{
			return 39;
		}
		else if(neighbors[1] == 45)
		{
			return 38;
		}
		else if(neighbors[2] == 45)
		{
			return 37;
		}
		else if(neighbors[0] == 37 && neighbors[1] == 38)
		{
			return 36;
		}
		else if(neighbors[1] == 39 && neighbors[2] == 40)
		{
			return 35;
		}
		else if(neighbors[5] == 31)
		{
			return 34;
		}
		else if(neighbors[1] == 42 && neighbors[2] == 43)
		{
			return 33;
		}
		else if(neighbors[1] == 33)
		{
			return 32;
		}
		else if(neighbors[4] == 26)
		{
			return 31;
		}
		else if(neighbors[3] == 31)
		{
			return 30;
		}
		else if(neighbors[3] == 30)
		{
			return 29;
		}
		else if(neighbors[1] == 36)
		{
			return 28;
		}
		else if(neighbors[1] == 28)
		{
			return 27;
		}
		else if(neighbors[3] == 25)
		{
			return 26;
		}
		else if(neighbors[3] == 24)
		{
			return 25;
		}
		else if(neighbors[5] == 21)
		{
			return 24;
		}
		else if(neighbors[1] == 32)
		{
			return 23;
		}
		else if(neighbors[1] == 23)
		{
			return 22;
		}
		else if(neighbors[5] == 16)
		{
			return 21;
		}
		else if(neighbors[1] == 26)
		{
			return 20;
		}
		else if(neighbors[1] == 27)
		{
			return 19;
		}
		else if(neighbors[1] == 19)
		{
			return 18;
		}
		else if(neighbors[1] == 20)
		{
			return 17;
		}
		else if(neighbors[5] == 13)
		{
			return 16;
		}
		else if(neighbors[1] == 22)
		{
			return 15;
		}
		else if(neighbors[1] == 15)
		{
			return 14;
		}
		else if(neighbors[5] == 3)
		{
			return 13;
		}
		else if(neighbors[1] == 17)
		{
			return 12;
		}
		else if(neighbors[1] == 18)
		{
			return 11;
		}
		else if(neighbors[1] == 11)
		{
			return 10;
		}
		else if(neighbors[0] == 11)
		{
			return 9;
		}
		else if(neighbors[7] == 9)
		{
			return 8;
		}
		else if(neighbors[7] == 8)
		{
			return 7;
		}
		else if(neighbors[7] == 7)
		{
			return 6;
		}
		else if(neighbors[7] == 6)
		{
			return 5;
		}
		else if(neighbors[1] == 12)
		{
			return 4;
		}
		else if(neighbors[3] == 2)
		{
			return 3;
		}
		else if(neighbors[2] == 14)
		{
			return 2;
		}
		else if(neighbors[1] == 14)
		{
			return 1;
		}
		// else if(neighbors[5] == 53 && cell != 57 && generation % 5 == 0)
		// {
		// // smoke
		// return 57;
		// }
		// else if(neighbors[5] == 57 && cell != 58)
		// {
		// // smoke
		// return 58;
		// }
		// else if(neighbors[5] == 58 && cell != 59)
		// {
		// // smoke
		// return 59;
		// }
		// else if(neighbors[5] == 59 && cell != 60)
		// {
		// // smoke
		// return 60;
		// }
		else
		{
			return 0;
		}
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
		String[] lattices = {SquareLattice.DISPLAY_NAME};

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
		String[] folders = {RuleFolderNames.UNUSUAL_SHAPES};

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
		// disables the “Number of States” text field
		return null;
	}

	/**
	 * Finds the value of the state that will be displayed in the "Number of
	 * States" text field.
	 * 
	 * @param latticeDescription
	 *            The display name of the lattice for which the state value will
	 *            be determined.
	 * @param ruleDescription
	 *            The display name of the rule for which the state value will be
	 *            determined.
	 * @return The state value should be displayed for the "Number of States"
	 *         text field. When null, will display the value currently in the
	 *         text field.
	 */
	protected Integer stateValueToDisplay(String latticeDescription)
	{
		// fixes the state value at NUMBER_OF_STATES
		return new Integer(NUMBER_OF_STATES);
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
