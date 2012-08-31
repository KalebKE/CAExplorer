/*
 Diffusion -- a class within the Cellular Automaton Explorer. 
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

import java.util.Random;

import cellularAutomata.rules.templates.IntegerMargolusTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.math.RandomSingleton;

/**
 * Rules for diffusion which obey the heat equation dp/dt = del^2 p. Uses the
 * Margolus neighborhood but could be rewritten as a lattice gas with vector
 * states.
 * 
 * @author David Bahr
 */
public class Diffusion extends IntegerMargolusTemplate
{
	// a display name for this class
	private static final String RULE_NAME = "Diffusion";

	// used to randomly choose a rearrangement
	private static Random random = RandomSingleton.getInstance();

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, try a 50 by 50 or larger lattice with a single seed "
			+ "(particle), "
			+ "and observe its random walk. Then draw a tight ball of cells and observe how they "
			+ "diffuse uniformly throughout the lattice.  Then try mutiple states, as if there are "
			+ "multiple gasses; by drawing a ball of cells for each state, the gasses will diffuse "
			+ "and commingle." + leftClickInstructions + rightClickInstructions
			+ "</body></html>";

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b> Models the physics of diffusion by letting each cell take a random walk."
			+ "</body></html>";

	/**
	 * Create the Diffusion rule using the given cellular automaton properties.
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
	public Diffusion(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);
	}

	/**
	 * Takes particles (or lack of) at each quadrant of the Margolus block and
	 * rearranges them randomly. The particles are conserved.
	 * 
	 * @param northWestCellValue
	 *            The current value of the northwest cell.
	 * @param northEastCellValue
	 *            The current value of the northeast cell.
	 * @param southEastCellValue
	 *            The current value of the southeast cell.
	 * @param southWestCellValue
	 *            The current value of the southwest cell.
	 * @return An array of values representing the randomly rearranged
	 *         particles.
	 */
	private int[] rearrangeTheBlock(int northWestCellValue,
			int northEastCellValue, int southEastCellValue,
			int southWestCellValue)
	{
		int[] newBlock = new int[4];

		int randomRearrangement = random.nextInt(24);
		if(randomRearrangement == 0)
		{
			// no rearrangement!
			newBlock[0] = northWestCellValue;
			newBlock[1] = northEastCellValue;
			newBlock[2] = southEastCellValue;
			newBlock[3] = southWestCellValue;
		}
		else if(randomRearrangement == 1)
		{
			newBlock[0] = northWestCellValue;
			newBlock[1] = northEastCellValue;
			newBlock[3] = southEastCellValue;
			newBlock[2] = southWestCellValue;
		}
		else if(randomRearrangement == 2)
		{
			newBlock[0] = northWestCellValue;
			newBlock[2] = northEastCellValue;
			newBlock[1] = southEastCellValue;
			newBlock[3] = southWestCellValue;
		}
		else if(randomRearrangement == 3)
		{
			newBlock[0] = northWestCellValue;
			newBlock[2] = northEastCellValue;
			newBlock[3] = southEastCellValue;
			newBlock[1] = southWestCellValue;
		}
		else if(randomRearrangement == 4)
		{
			newBlock[0] = northWestCellValue;
			newBlock[3] = northEastCellValue;
			newBlock[1] = southEastCellValue;
			newBlock[2] = southWestCellValue;
		}
		else if(randomRearrangement == 5)
		{
			newBlock[0] = northWestCellValue;
			newBlock[3] = northEastCellValue;
			newBlock[2] = southEastCellValue;
			newBlock[1] = southWestCellValue;
		}
		else if(randomRearrangement == 6)
		{
			newBlock[1] = northWestCellValue;
			newBlock[0] = northEastCellValue;
			newBlock[2] = southEastCellValue;
			newBlock[3] = southWestCellValue;
		}
		else if(randomRearrangement == 7)
		{
			newBlock[1] = northWestCellValue;
			newBlock[0] = northEastCellValue;
			newBlock[3] = southEastCellValue;
			newBlock[2] = southWestCellValue;
		}
		else if(randomRearrangement == 8)
		{
			newBlock[1] = northWestCellValue;
			newBlock[2] = northEastCellValue;
			newBlock[0] = southEastCellValue;
			newBlock[3] = southWestCellValue;
		}
		else if(randomRearrangement == 9)
		{
			newBlock[1] = northWestCellValue;
			newBlock[2] = northEastCellValue;
			newBlock[3] = southEastCellValue;
			newBlock[0] = southWestCellValue;
		}
		else if(randomRearrangement == 10)
		{
			newBlock[1] = northWestCellValue;
			newBlock[3] = northEastCellValue;
			newBlock[0] = southEastCellValue;
			newBlock[2] = southWestCellValue;
		}
		else if(randomRearrangement == 11)
		{
			newBlock[1] = northWestCellValue;
			newBlock[3] = northEastCellValue;
			newBlock[2] = southEastCellValue;
			newBlock[0] = southWestCellValue;
		}
		else if(randomRearrangement == 12)
		{
			newBlock[2] = northWestCellValue;
			newBlock[0] = northEastCellValue;
			newBlock[1] = southEastCellValue;
			newBlock[3] = southWestCellValue;
		}
		else if(randomRearrangement == 13)
		{
			newBlock[2] = northWestCellValue;
			newBlock[0] = northEastCellValue;
			newBlock[3] = southEastCellValue;
			newBlock[1] = southWestCellValue;
		}
		else if(randomRearrangement == 14)
		{
			newBlock[2] = northWestCellValue;
			newBlock[1] = northEastCellValue;
			newBlock[0] = southEastCellValue;
			newBlock[3] = southWestCellValue;
		}
		else if(randomRearrangement == 15)
		{
			newBlock[2] = northWestCellValue;
			newBlock[1] = northEastCellValue;
			newBlock[3] = southEastCellValue;
			newBlock[0] = southWestCellValue;
		}
		else if(randomRearrangement == 16)
		{
			newBlock[2] = northWestCellValue;
			newBlock[3] = northEastCellValue;
			newBlock[0] = southEastCellValue;
			newBlock[1] = southWestCellValue;
		}
		else if(randomRearrangement == 17)
		{
			newBlock[2] = northWestCellValue;
			newBlock[3] = northEastCellValue;
			newBlock[1] = southEastCellValue;
			newBlock[0] = southWestCellValue;
		}
		else if(randomRearrangement == 18)
		{
			newBlock[3] = northWestCellValue;
			newBlock[0] = northEastCellValue;
			newBlock[1] = southEastCellValue;
			newBlock[2] = southWestCellValue;
		}
		else if(randomRearrangement == 19)
		{
			newBlock[3] = northWestCellValue;
			newBlock[0] = northEastCellValue;
			newBlock[2] = southEastCellValue;
			newBlock[1] = southWestCellValue;
		}
		else if(randomRearrangement == 20)
		{
			newBlock[3] = northWestCellValue;
			newBlock[1] = northEastCellValue;
			newBlock[0] = southEastCellValue;
			newBlock[2] = southWestCellValue;
		}
		else if(randomRearrangement == 21)
		{
			newBlock[3] = northWestCellValue;
			newBlock[1] = northEastCellValue;
			newBlock[2] = southEastCellValue;
			newBlock[0] = southWestCellValue;
		}
		else if(randomRearrangement == 22)
		{
			newBlock[3] = northWestCellValue;
			newBlock[2] = northEastCellValue;
			newBlock[0] = southEastCellValue;
			newBlock[1] = southWestCellValue;
		}
		else if(randomRearrangement == 23)
		{
			newBlock[3] = northWestCellValue;
			newBlock[2] = northEastCellValue;
			newBlock[1] = southEastCellValue;
			newBlock[0] = southWestCellValue;
		}
		
		return newBlock;
	}

	/**
	 * A rule for diffusion. Takes the occupied sites of the Margolus
	 * neighborhood and rearranges them randomly.
	 * 
	 * @param northWestCellValue
	 *            The current value of the northwest cell.
	 * @param northEastCellValue
	 *            The current value of the northeast cell.
	 * @param southEastCellValue
	 *            The current value of the southeast cell.
	 * @param southWestCellValue
	 *            The current value of the southwest cell.
	 * @param numStates
	 *            The number of states. In other words, the returned state can
	 *            only have values between 0 and numStates - 1.
	 * @param generation
	 *            The current generation of the CA.
	 * @return An array of states that corresponds to the 2 by 2 Margolus block.
	 *         Array[0] is the northwest corner of the block, array[1] is the
	 *         northeast corner of the block, array[2] is the southeast corner
	 *         of the block, array[3] is the southwest corner of the block.
	 */
	protected int[] blockRule(int northWestCellValue, int northEastCellValue,
			int southEastCellValue, int southWestCellValue, int numStates,
			int generation)
	{
		// take the original block values (particles) and rearrange them
		// randomly within the block. Conserves the values (particles).
		// i.e., pick one of the 24 random rearrangements. And then we assign
		// the rearrangement to an array representing the new Margolus block.
		return rearrangeTheBlock(northWestCellValue, northEastCellValue,
				southEastCellValue, southWestCellValue);
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
		String[] folders = {RuleFolderNames.PHYSICS_FOLDER,
				RuleFolderNames.PROBABILISTIC_FOLDER,
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
