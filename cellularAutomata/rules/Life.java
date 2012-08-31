/*
 Life -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.rules.templates.IntegerRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;

/**
 * Rules for the game of life. If a cell is occupied and has 0 or 1 occupied
 * neighbors, then the cell dies of loneliness (becomes a 0). If a cell is
 * occupied and has 4 or more occupied neighbors, then the cell dies of
 * overcrowding (becomes a 0). If a cell is occupied and has 2 or 3 occupied
 * neighbors, then the cell survives (stays a 1). If a cell is not occupied and
 * has exactly 3 occupied neighbors, then the cell births (becomes a 1).
 * 
 * @author David Bahr
 */
public class Life extends IntegerRuleTemplate
{
	/**
	 * A display name for this class. (This is not normally public, but Life is
	 * used frequently in the CA Explorer, and this is a handy shortcut. When
	 * writing your own rule, I'd suggest making this variable private.)
	 */
	public static final String RULE_NAME = "Life";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, use a square (8 neighbor) grid with two states and a "
			+ "50% random initial state.  Look for stable squares, blinkers, moving gliders, "
			+ "and other interesting forms.  Now try clearing the graphics and drawing your "
			+ "own shapes. On smaller lattices, the generations can update too quickly "
			+ "(faster than the eye can follow), so you may want to slow down the graphics "
			+ "(add a delay on the \"Controls\" panel) to see the variety of fascinating "
			+ "\"life\" forms. On grids with "
			+ "more than 8 neighbors, keep the initial population density small to avoid "
			+ "instant deaths. On grids with fewer than 8 neighbors, note the dramatic loss "
			+ "of complexity. "
			+ "<p>"
			+ "For an interesting twist, try taking running averages of 10, 15, or 20 time "
			+ "steps." + leftClickInstructions + rightClickInstructions
			+ "</body></html>";

	// loneliness (classic version's value is 1)
	private static final int LONELINESS = 1;

	// overcrowding (classic version's value is 4)
	private static final int OVERCROWDING = 4;

	// births (classic version's value is 3)
	private static final int BIRTH = 3;

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b> The all-time classic must-see CA with cool blinkers, gliders, "
			+ "and a host of other structures.</body></html>";

	/**
	 * Create the Game of Life rule.
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
	public Life(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);
	}

	/**
	 * Rules for the game of life.
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
		// add all the neighboring sites (excluding the cell)
		int sum = 0;
		for(int i = 0; i < neighbors.length; i++)
		{
			sum += neighbors[i];
		}

		// dies of loneliness or overcrowding (survives with 2 or 3 neighbors)
		boolean dies = (sum <= LONELINESS) || (sum >= OVERCROWDING);

		// births
		boolean procreate = (sum == BIRTH);

		if(cell >= 1 && dies)
		{
			cell = 0;
		}
		else if(cell == 0 && procreate)
		{
			cell = numStates - 1;
		}

		return cell;
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
				RuleFolderNames.KNOWN_UNIVERSAL_FOLDER,
				RuleFolderNames.OUTER_TOTALISTIC_FOLDER,
				RuleFolderNames.LIFE_LIKE_FOLDER,
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
