/*
 RockPaperScissors -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.rules.templates.FiniteObjectRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.math.RandomSingleton;

/**
 * Rule that plays the game rock-paper-scissors. Each cell chooses a random
 * neighbor and plays the game. If the cell wins or ties it keeps its current
 * value. Otherwise it adopts its neighbor's value.
 * 
 * @author David Bahr
 */
public class RockPaperScissorsRule extends FiniteObjectRuleTemplate
{
    // a display name for this class
    private static final String RULE_NAME = "Rock/Paper/Scissors";

    // a description of property choices that give the best results for this
    // rule (e.g., which lattice, how many states, etc.)
    private static final String BEST_RESULTS = "<html> <body><b>" + RULE_NAME
        + ".</b>" + "<p> "
        + "<b>For best results</b>, try a 10% random initial population on a "
        + "200 by 200 (or larger) square (8 neighbor) lattice. The Population "
        + "analysis is particularly fun to watch with this simulation."
        + leftClickInstructions + rightClickInstructions + "</body></html>";

    // a tooltip description for this class
    private String TOOLTIP = "<html> <body><b>"
        + RULE_NAME
        + ".</b> Each cell plays rock/paper/scissors with a random neighbor.</body></html>";

    private static Random random = RandomSingleton.getInstance();

    /**
     * Create a Rock/Paper/Scissors game using the given cellular automaton
     * properties.
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
    public RockPaperScissorsRule(boolean minimalOrLazyInitialization)
    {
        super(minimalOrLazyInitialization);
    }

    /**
     * Returns a list of permissable states. Each one has a unique string
     * representation of "rock", "paper", or "scissors".
     * 
     * @param properties
     *            The CA properties.
     * 
     * @return An array of allowed states for the cells.
     */
    protected Object[] getObjectArray()
    {
        RockPaperScissor paper = new RockPaperScissor(
            RockPaperScissor.PAPER_STATE);
        RockPaperScissor rock = new RockPaperScissor(
            RockPaperScissor.ROCK_STATE);
        RockPaperScissor scissors = new RockPaperScissor(
            RockPaperScissor.SCISSOR_STATE);

        // the list of possible states
        Object[] listOfObjects = {rock, paper, scissors};

        return listOfObjects;
    }

    /**
     * Rules for rock/paper/scissors. The cell takes its state (rock, paper, or
     * scissors) and compares it to the state of a random neighbor. One of the
     * states will win the game. The cell now becomes the winning state.
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
    protected Object objectRule(Object cell, Object[] neighbors, int generation)
    {
        // get a random neighbor
        int randomNeighbor = random.nextInt(neighbors.length);

        // get the values of the cell and a neighbor
        RockPaperScissor cellValue = (RockPaperScissor) cell;
        RockPaperScissor neighborValue = (RockPaperScissor) neighbors[randomNeighbor];

        // the new value that we will return
        RockPaperScissor newValue = new RockPaperScissor(cellValue);

        // now play the game
        if(neighborValue.winsAgainst(cellValue))
        {
            newValue.setStateToSameValue(neighborValue);
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
        String[] folders = {RuleFolderNames.CYCLIC_RULES_FOLDER,
            RuleFolderNames.PROBABILISTIC_FOLDER};

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

    /**
     * An object that represents a rock, paper or scissors.
     * 
     * @author David Bahr
     */
    private class RockPaperScissor
    {
        /**
         * The state of a rock.
         */
        public static final String ROCK_STATE = "Rock";

        /**
         * The state of a paper.
         */
        public static final String PAPER_STATE = "Paper";

        /**
         * The state of scissors.
         */
        public static final String SCISSOR_STATE = "Scissors";

        // the current state of this object
        private String currentState = ROCK_STATE;

        /**
         * Create a rock, paper, or scissors state corresponding to the
         * specified string. Valid strings are ROCK_STATE, PAPER_STATE, or
         * SCISSOR_STATE.
         */
        public RockPaperScissor(String state)
        {
            if(state.equals(ROCK_STATE) || state.equals(PAPER_STATE)
                || state.equals(SCISSOR_STATE))
            {
                currentState = state;
            }
            else
            {
                currentState = ROCK_STATE;
            }
        }

        /**
         * Create a random rock, paper, or scissors state.
         */
        public RockPaperScissor()
        {
            int choice = random.nextInt(3);
            if(choice == 0)
            {
                currentState = ROCK_STATE;
            }
            else if(choice == 1)
            {
                currentState = PAPER_STATE;
            }
            else
            {
                currentState = SCISSOR_STATE;
            }
        }

        /**
         * Create a RockPaperScissor with the same state as the parameter.
         * 
         * @param rps
         *            This new object will be assigned the same state as rps.
         */
        public RockPaperScissor(RockPaperScissor rps)
        {
            this.currentState = rps.getState();
        }

        /**
         * Gets the string representing the current state.
         * 
         * @return The string representing the current state.
         */
        public String getState()
        {
            return currentState;
        }

        /**
         * Sets the state to the value of the specified RockPaperScissors
         * object.
         * 
         * @param rps
         *            The value of this object will be set to the same state as
         *            rps.
         */
        public void setStateToSameValue(RockPaperScissor rps)
        {
            currentState = rps.toString();
        }

        /**
         * Sets the state to be a paper.
         */
        public void setStateToPaper()
        {
            currentState = PAPER_STATE;
        }

        /**
         * Sets the state to be a rock.
         */
        public void setStateToRock()
        {
            currentState = ROCK_STATE;
        }

        /**
         * Sets the state to be a scissors.
         */
        public void setStateToScissors()
        {
            currentState = SCISSOR_STATE;
        }

        /**
         * Gets the string representing the current state.
         * 
         * @return The string representing the current state.
         */
        public String toString()
        {
            return currentState;
        }

        /**
         * Pits this object against the specified competitor to see who wins.
         * 
         * @param competitor
         *            The competitor against which this class competes.
         * 
         * @return true if this object wins or ties, and false otherwise.
         */
        public boolean winsAgainst(RockPaperScissor competitor)
        {
            boolean wins = true;

            String competitorValue = competitor.getState();

            // see if it loses (all cases not shown area win or a tie)
            if(currentState.equals(PAPER_STATE)
                && competitorValue.equals(SCISSOR_STATE))
            {
                wins = false;
            }
            else if(currentState.equals(ROCK_STATE)
                && competitorValue.equals(PAPER_STATE))
            {
                wins = false;
            }
            else if(currentState.equals(SCISSOR_STATE)
                && competitorValue.equals(ROCK_STATE))
            {
                wins = false;
            }

            return wins;
        }
    }
}
