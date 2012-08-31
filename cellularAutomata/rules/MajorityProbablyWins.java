/*
 MajorityProbablyWins -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.rules.templates.IntegerRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.math.RandomSingleton;

/**
 * Rule for joining factions based on the percent present. So if there are two
 * 0's and six 1's, then the odds are 6/8 that the cell becomes a 1 and 2/8 that
 * the cell becomes a 0.
 * 
 * @author David Bahr
 */
public class MajorityProbablyWins extends IntegerRuleTemplate
{
    // random number generator
    private static Random random = RandomSingleton.getInstance();

    // a display name for this class
    private static final String RULE_NAME = "Majority Probably Wins";

    // a description of property choices that give the best results for this
    // rule (e.g., which lattice, how many states, etc.)
    private static final String BEST_RESULTS = "<html> <body><b>"
        + RULE_NAME
        + ".</b>"
        + "<p> "
        + "<b>For best results</b>, try two states with a 50% random initial population "
        + "on a square (8 neighbor) lattice.  Try varying the initial population from 40% "
        + "to 50% to 60% to see changes in behavior. Also try 3 states with a 66% initial "
        + "population.  And then try 4 states with 75%, and 5 states with 80%, etc. "
        + leftClickInstructions + rightClickInstructions + "</body></html>";

    // a tooltip description for this class
    private String TOOLTIP = "<html> <body><b>"
        + RULE_NAME
        + ".</b> A model of decision making where folks usually and probably base their "
        + "current opinion on their neighbors' previous opinions.</body></html>";

    /**
     * Create the Majority rule using the given cellular automaton properties.
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
    public MajorityProbablyWins(boolean minimalOrLazyInitialization)
    {
        super(minimalOrLazyInitialization);
    }

    /**
     * Rule for the majority probably wins.
     * 
     * @param cell
     *            The value of the cell being updated.
     * @param neighbors
     *            The value of the neighbors.
     * @param generation
     *            The current generation of the CA.
     * 
     * @return A new state for the cell.
     */
    protected int integerRule(int cell, int[] neighbors, int numStates,
        int generation)
    {
        // store how many cells have each state
        int[] numberOfEachState = new int[numStates];

        // initialize
        for(int i = 0; i < numStates; i++)
        {
            numberOfEachState[i] = 0;
        }

        // figure out how many cells have each state
        for(int i = 0; i < neighbors.length; i++)
        {
            int state = neighbors[i];

            numberOfEachState[state]++;
        }

        // don't forget the cell itself
        numberOfEachState[cell]++;

        // get probability (percent) of each state
        double[] prob = new double[numStates];
        for(int i = 0; i < numStates; i++)
        {
            prob[i] = ((double) numberOfEachState[i])
                / ((double) neighbors.length+1);
        }

        // get cumulative probability of each state
        double[] cumProb = new double[numStates];
        cumProb[0] = prob[0];
        for(int i = 1; i < numStates; i++)
        {
            cumProb[i] = cumProb[i - 1] + prob[i];
        }

        // Now get a random number between 0 and numStates-1
        double randomNumber = random.nextDouble();

        // use the random number to choose a state (j is the state)
        int j = 0;
        while((randomNumber > cumProb[j]) && (j < numStates))
        {
            j++;
        }

        return j;
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
            RuleFolderNames.PROBABILISTIC_FOLDER,
            RuleFolderNames.PHYSICS_FOLDER, RuleFolderNames.SOCIAL_FOLDER};

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
