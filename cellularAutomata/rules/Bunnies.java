/*
 Bunnies -- a rule class within the Cellular Automaton Explorer. 
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
 * This surprising CA uses many states to create a bunny rabbit shape.
 * 
 * @author David Bahr
 */
public class Bunnies extends IntegerRuleTemplate
{
	/**
	 * A display name for this class.
	 */
	public static final String RULE_NAME = "Bunnies";

	// The number of states for this rule (0 to 72)
	private static final int NUMBER_OF_STATES = 73;

	// private static final int[][] bunnyArray =
	// {
	// {0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,1,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,1,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,1,1,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,1,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0},
	// {0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0},
	// {0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,1,0,0,1,0,0,0,0},
	// {0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0},
	// {0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0}
	// };

	//
	// private static final int[][] bunnyArray =
	// {
	// {0,0,0,0,0,0,72,71,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,70,0,69,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,67,0,0,68,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,66,0,0,0,65,0,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,63,0,0,64,0,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,0,62,0,61,0,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,0,0,59,60,0,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,58,57,56,55,0,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,52,53,0,0,54,0,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,51,0,0,50,0,0,49,0,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,47,0,0,0,0,0,0,48,0,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,46,45,44,0,0,0,0,43,0,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,0,41,0,0,0,0,0,42,0,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,40,39,0,0,0,0,0,0,38,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,35,36,0,0,0,0,0,0,37,0,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,0,34,0,0,0,0,0,0,0,33,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,0,0,29,30,31,0,0,0,0,32,0,0,0,0,0,0,0},
	// {0,0,0,0,0,0,0,0,28,0,0,0,0,0,0,0,27,26,25,0,0,0,0,0},
	// {0,0,0,0,0,0,0,22,0,0,0,0,0,0,0,0,23,0,0,24,0,0,0,0},
	// {0,0,0,0,0,0,0,0,21,0,0,0,0,0,0,20,19,0,0,18,0,0,0,0},
	// {0,0,0,0,0,0,0,0,0,8,9,10,11,12,13,14,15,16,17,0,0,0,0,0},
	// {0,0,0,0,0,0,0,0,7,6,5,4,3,2,1,0,0,0,0,0,0,0,0,0}
	// };

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>" + RULE_NAME
			+ ".</b>" + "<p> " + "<b>For best results</b>, try a 20% random "
			+ "initial state on a 150 by 150 lattice. Also try a single seed "
			+ "for an initial state. In a blank open space, try drawing a new "
			+ "cell with the mouse." + leftClickInstructions
			+ rightClickInstructions + "</body></html>";

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>" + RULE_NAME
			+ ".</b> Perhaps surprisingly, creates rabbits by "
			+ "using a large number of states.</body></html>";

	/**
	 * Create a rule that builds a bunny shape.
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
	public Bunnies(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);
	}

	/**
	 * Updates the cell based on rules that create a bunny shape.
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
		if(cell == 72)
		{
			return 72;
		}
		else if(neighbors[7] == 72)
		{
			return 71;
		}
		else if(neighbors[1] == 72 && neighbors[2] == 71)
		{
			return 70;
		}
		else if(neighbors[0] == 71)
		{
			return 69;
		}
		else if(neighbors[0] == 69)
		{
			return 68;
		}
		else if(neighbors[1] == 70)
		{
			return 67;
		}
		else if(neighbors[1] == 67)
		{
			return 66;
		}
		else if(neighbors[0] == 68)
		{
			return 65;
		}
		else if(neighbors[1] == 65)
		{
			return 64;
		}
		else if(neighbors[0] == 66)
		{
			return 63;
		}
		else if(neighbors[0] == 63)
		{
			return 62;
		}
		else if(neighbors[1] == 64)
		{
			return 61;
		}
		else if(neighbors[1] == 61)
		{
			return 60;
		}
		else if(neighbors[0] == 62 && neighbors[2] == 61)
		{
			return 59;
		}
		else if(neighbors[3] == 57)
		{
			return 58;
		}
		else if(neighbors[2] == 59)
		{
			return 57;
		}
		else if(neighbors[1] == 59 && neighbors[2] == 60)
		{
			return 56;
		}
		else if(neighbors[0] == 59 && neighbors[1] == 60)
		{
			return 55;
		}
		else if(neighbors[0] == 56 && neighbors[1] == 55)
		{
			return 54;
		}
		else if(neighbors[1] == 58 && neighbors[2] == 57)
		{
			return 53;
		}
		else if(neighbors[2] == 58)
		{
			return 52;
		}
		else if(neighbors[2] == 52)
		{
			return 51;
		}
		else if(neighbors[0] == 53)
		{
			return 50;
		}
		else if(neighbors[0] == 54)
		{
			return 49;
		}
		else if(neighbors[0] == 49)
		{
			return 48;
		}
		else if(neighbors[1] == 51)
		{
			return 47;
		}
		else if(neighbors[0] == 47)
		{
			return 46;
		}
		else if(neighbors[7] == 46)
		{
			return 45;
		}
		else if(neighbors[7] == 45)
		{
			return 44;
		}
		else if(neighbors[0] == 48)
		{
			return 43;
		}
		else if(neighbors[0] == 43)
		{
			return 42;
		}
		else if(neighbors[0] == 45 && neighbors[1] == 44)
		{
			return 41;
		}
		else if(neighbors[2] == 41)
		{
			return 40;
		}
		else if(neighbors[1] == 41)
		{
			return 39;
		}
		else if(neighbors[0] == 42)
		{
			return 38;
		}
		else if(neighbors[1] == 38)
		{
			return 37;
		}
		else if(neighbors[0] == 40 && neighbors[1] == 39)
		{
			return 36;
		}
		else if(neighbors[1] == 40 && neighbors[2] == 39)
		{
			return 35;
		}
		else if(neighbors[0] == 35 && neighbors[1] == 36)
		{
			return 34;
		}
		else if(neighbors[0] == 37)
		{
			return 33;
		}
		else if(neighbors[1] == 33)
		{
			return 32;
		}
		else if(neighbors[7] == 30)
		{
			return 31;
		}
		else if(neighbors[7] == 29)
		{
			return 30;
		}
		else if(neighbors[0] == 34)
		{
			return 29;
		}
		else if(neighbors[2] == 29)
		{
			return 28;
		}
		else if(neighbors[1] == 32)
		{
			return 27;
		}
		else if(neighbors[0] == 32)
		{
			return 26;
		}
		else if(neighbors[7] == 26)
		{
			return 25;
		}
		else if(neighbors[0] == 25)
		{
			return 24;
		}
		else if(neighbors[1] == 27 && neighbors[2] == 26)
		{
			return 23;
		}
		else if(neighbors[2] == 28)
		{
			return 22;
		}
		else if(neighbors[0] == 22)
		{
			return 21;
		}
		else if(neighbors[2] == 23)
		{
			return 20;
		}
		else if(neighbors[1] == 23)
		{
			return 19;
		}
		else if(neighbors[1] == 24)
		{
			return 18;
		}
		else if(neighbors[2] == 18)
		{
			return 17;
		}
		else if(neighbors[0] == 19)
		{
			return 16;
		}
		else if(neighbors[0] == 20 && neighbors[1] == 19)
		{
			return 15;
		}
		else if(neighbors[1] == 20 && neighbors[2] == 19)
		{
			return 14;
		}
		else if(neighbors[2] == 20)
		{
			return 13;
		}
		else if(neighbors[3] == 13)
		{
			return 12;
		}
		else if(neighbors[3] == 12)
		{
			return 11;
		}
		else if(neighbors[3] == 11)
		{
			return 10;
		}
		else if(neighbors[3] == 10)
		{
			return 9;
		}
		else if(neighbors[0] == 21)
		{
			return 8;
		}
		else if(neighbors[2] == 8)
		{
			return 7;
		}
		else if(neighbors[1] == 8 && neighbors[2] == 9)
		{
			return 6;
		}
		else if(neighbors[0] == 8 && neighbors[1] == 9 && neighbors[2] == 10)
		{
			return 5;
		}
		else if(neighbors[0] == 9 && neighbors[1] == 10 && neighbors[2] == 11)
		{
			return 4;
		}
		else if(neighbors[0] == 10 && neighbors[1] == 11 && neighbors[2] == 12)
		{
			return 3;
		}
		else if(neighbors[0] == 11 && neighbors[1] == 12 && neighbors[2] == 13)
		{
			return 2;
		}
		else if(neighbors[0] == 12 && neighbors[1] == 13 && neighbors[2] == 14)
		{
			return 1;
		}
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
