/*
 ChainLinkFence -- a class within the Cellular Automaton Explorer. 
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

/**
 * From a single initial sead, his rule will build a square lattice.
 * 
 * @author David Bahr
 */
public class ChainLinkFence extends IntegerRuleTemplate
{
	/**
	 * A display name for this class.
	 */
	private static final String RULE_NAME = "Chain Link Fence";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, use a single seed on a "
			+ "large lattice (100 by 100 or bigger) with 10 or more states. The number of "
			+ "states determines the distance between links on the fence. "
			+ "<p>"
			+ "A perfect fence will be constructed if the single seed has a state value of 1. "
			+ "A single seed with any other state values will build an imperfect fence with "
			+ "\"knots\".  The default single seed is set at the maximum number of states "
			+ "and, therefore, will build a slightly imperfect fence."
			+ "<p>"
			+ "Also try a 1% random initial state with 50 or more states.  Or "
			+ "try various shaped rectangles and ellipses as initial states.  Simple "
			+ "free-hand shapes also give interesting results."
			+ leftClickInstructions + rightClickInstructions + "</body></html>";

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b> Creates a chain link fence from a single seed.</body></html>";

	/**
	 * Create a chain link fence.
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
	public ChainLinkFence(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);
	}

	/**
	 * Rules for building a chain link fence.
	 * 
	 * @param cell
	 *            The value of the cell being updated.
	 * @param neighbors
	 *            The value of the neighbors.
	 * @param generation
	 *            The current generation of the CA.
	 * @return A new state for the cell.
	 */
	protected final int integerRule(int cell, int[] neighbors, int numStates,
			int generation)
	{
		int northNeighbor = neighbors[1];
		int eastNeighbor = neighbors[3];
		int southNeighbor = neighbors[5];
		int westNeighbor = neighbors[7];

		int[] neumannNeighbors = {northNeighbor, eastNeighbor, southNeighbor,
				westNeighbor};

		// Find min of N, E, S, W neighbors (beside 0).
		// Note the min is guaranteed to be smaller than numStates.
		int minOfNESW = numStates;
		for(int i = 0; i < neumannNeighbors.length; i++)
		{
			if(neumannNeighbors[i] < minOfNESW && neumannNeighbors[i] != 0)
			{
				minOfNESW = neumannNeighbors[i];
			}
		}

		// make sure the min isn't too big (i.e., assign a 0 if all the
		// neighbors are 0)
		minOfNESW %= numStates;

		// find min of all neighbors
		int minOfAllNeighbors = numStates;
		for(int i = 0; i < neighbors.length; i++)
		{
			if(neighbors[i] < minOfAllNeighbors && neighbors[i] != 0)
			{
				minOfAllNeighbors = neighbors[i];
			}
		}

		// make sure the min isn't too big (i.e., assign a 0 if all the
		// neighbors are 0)
		minOfAllNeighbors %= numStates;

		// the new value of the cell
		int newValue = 0;
		if(cell == 1)
		{
			// stay that way
			newValue = 1;
		}
		else if(minOfNESW != 0 && minOfAllNeighbors >= minOfNESW)
		{
			newValue = minOfNESW + 1;
			if(newValue == numStates)
			{
				newValue = 1;
			}
		}
		else if(minOfAllNeighbors < minOfNESW)
		{
			// if sees a smaller neighbor, then becomes an "empty space" (a 0)
			newValue = 0;
		}

		return newValue;
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
	 * A list of lattices with which this Rule will work, which in this case, is
	 * only the hexagonal lattice. <br>
	 * Well-designed Rules should work with any lattice, but some may require
	 * particular topological or geometrical information (like the lattice gas).
	 * Appropriate strings to return in the array include
	 * SquareLattice.DISPLAY_NAME, HexagonalLattice.DISPLAY_NAME,
	 * StandardOneDimensionalLattice.DISPLAY_NAME, etc.
	 * 
	 * @return A list of lattices compatible with this Rule.
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
		String[] folders = null;

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
