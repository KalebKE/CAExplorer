package userRules;

import cellularAutomata.lattice.SquareLattice;
import cellularAutomata.rules.templates.MultiGenerationIntegerRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class KR7RandomMajorityWins extends MultiGenerationIntegerRuleTemplate
{
	/**
	 * A display name for this class.
	 */
	private static final String RULE_NAME = "KR7RandomMajorityWins";

	// a one line tooltip description for this rule
	private static final String TOOLTIP = "<html> <body><b>KalebRule5.</b> A CA that has cells "
			+ "that want to be like their neighbors, most of the time.</body></html>";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>KalebRule1.</b>"
			+ "<p> <b>For best results</b>, use a square lattice with lots of "
			+ "states, wrap around boundaries, and equal (or close to equal) initial"
			+ "probabilities for each state. The larger the lattice the better."
			+ "To see the rebellious flare ups and grouping, use bright colors"
			+ "with a high contrast. Set the running average to around 5 to see"
			+ "the groupings clearly."
			+ leftClickInstructions
			+ rightClickInstructions + "</body></html>";

	// the number of generations required to calculate the next generation's
	// state. When 1, that means just the current generation. When 2, that means
	// both the current generation and the previous generation.
	private final int numRequiredGenerations = 2;

	/**
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
	public KR7RandomMajorityWins(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);
	}

	/**
	 * KalebRule7 is based very heavily on Majority Wins and Majority Probably
	 * wins by David Bahr. The code itself is basically the same, but there are
	 * some major structure and implementation changes that allow it to sit
	 * somewhere between Majority Probably Wins and Minority Probably Wins CA.
	 * This is how the model was constructed: The state of cell represents a
	 * behavior. Most of the time, the cell wants to act like its neighbors. A
	 * cell looks at its neighbors current behavior, and their previous
	 * behavior, to decide what its new state will be. There is a chance that a
	 * cell may be defiant and will chose a completely random state. This is
	 * accomplished through the random assignment of probabilities to each
	 * state. Note that most of the time, a states "popularity" (number of times
	 * a given state occurs as a cells neighbor) is multiplied by
	 * its own probability of occurring (based on how many times it occurs as a
	 * cells neighbor divided by the number of states). Randomness is required to drive the
	 * model, otherwise it wants to to find a state of equilibrium, which it
	 * does, and usually that is boring. After the "popularity" of each state
	 * is multiplied by its probability of occurring again (either based on its
	 * own popularity, or sometimes at random), the most popular state(s) and 
	 * last popular state(s) are selected. If "state" is in the plural form 
	 * "states" (meaning two states had an equal chance of occurring), then a random
	 * selection of the two states occurs. Finally, ABOUT half the time, maxState
	 * is returned as the new cell state and ABOUT half the time, minState is returned
	 * as the new cell state. Changing these probabilities is what changes who 
	 * wins in this model.
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

	protected int integerRule(int[] cellValues, int[][] neighbors,
			int numStates, int generation)
	{
		Random r = new Random();
		Random q = new Random();
		Random s = new Random();
		// store how many cells have each state
		int[] numberOfEachState = new int[numStates];
		Arrays.fill(numberOfEachState, 0);
		// store how many cells have each previous state
		int[] numberOfEachPreviousState = new int[numStates];
		Arrays.fill(numberOfEachPreviousState, 0);
		// store how many total cells
		int[] totalNumberOfEachState = new int[numStates];
		Arrays.fill(totalNumberOfEachState, 0);
		double[] newState = new double[numStates];
		Arrays.fill(newState, 0);

		// figure out how many cells have each state
		for (int i = 0; i < neighbors.length; i++)
		{
			int state = neighbors[i][0];
			numberOfEachState[state]++;
		}

		// figure out how many cells have each previous state
		for (int i = 0; i < neighbors.length; i++)
		{
			int state = neighbors[i][1];
			numberOfEachPreviousState[state]++;
		}

		// figure out probability of each state
		double[] prob = new double[numStates];
		for (int i = 0; i < numStates; i++)
		{
			prob[i] = ((double) numberOfEachState[i])
					/ ((double) neighbors.length + 1);
		}

		// figure out probability of each previous state
		double[] previousProb = new double[numStates];
		for (int i = 0; i < numStates; i++)
		{
			previousProb[i] = ((double) numberOfEachPreviousState[i])
					/ ((double) neighbors.length + 1);
		}
		// average the probability of both probabilities
		double[] averageProb = new double[numStates];
		for (int i = 0; i < numStates; i++)
		{
			averageProb[i] = ((previousProb[i] + prob[i]) / 2);
		}
		// total number of states
		for (int i = 0; i < numStates; i++)
		{
			totalNumberOfEachState[i] = numberOfEachState[i]
					+ numberOfEachPreviousState[i];
		}
		// multiply each state by a random probability
		for (int i = 0; i < numStates; i++)
		{
			// about half the time, assign a random probability
			if (r.nextInt(2) == 1)
			{
				newState[i] = totalNumberOfEachState[i]
						* (averageProb[q.nextInt(numStates)]*10);
			} 
			// about half the time, assign its own probability
			else
			{
				newState[i] = totalNumberOfEachState[i] * (averageProb[i]*10);
			}
		}
		// this is a list of all the states that have the same maximum value.
		// For example, state 0 and state 2 could both occupy 3 cells (more than
		// any other state). So this list will hold both 0 and 2. Later we will
		// pick between them with equal probability.
		ArrayList<Integer> allTheStatesWithMax = new ArrayList<Integer>(
				numStates);

		// find maximum
		int maxState = 0;
		for (int i = 0; i < numStates; i++)
		{
			if (newState[i] > newState[maxState])
			{
				// empty the list of states that have the same max number
				allTheStatesWithMax.clear();

				// the maximum is the one with more of that state,
				maxState = i;

				// so add this new maximum state
				allTheStatesWithMax.add(new Integer(i));
			} else if (newState[i] == newState[maxState])
			{
				// but if they are equal, then I will need to choose between
				// them with equal probability. So keep a list. Add this new
				// state to the list.
				allTheStatesWithMax.add(new Integer(i));
			}
		}

		// Now, with equal probability, choose between states that have the
		// same maximum number.
		maxState = allTheStatesWithMax.get(
				r.nextInt(allTheStatesWithMax.size())).intValue();

		// this is a list of all the states that have the same minimum value.
		// For example, state 0 and state 2 could both occupy 0 cells (fewer
		// than any other state). So this list will hold both 0 and 2. Later we
		// will pick between them with equal probabililty.
		ArrayList<Integer> allTheStatesWithMin = new ArrayList<Integer>(
				numStates);

		// find minimum
		int minState = 0;
		for (int i = 0; i < numStates; i++)
		{
			if (newState[i] < newState[minState])
			{
				// empty the list of states that have the same min number
				allTheStatesWithMin.clear();

				// the minimum is the one with fewer of that state,
				minState = i;

				// so add this new minimum state
				allTheStatesWithMin.add(new Integer(i));
			} else if (newState[i] == newState[minState])
			{
				// but if they are equal, then I will need to choose between
				// them with equal probability. So keep a list. Add this new
				// state to the list.
				allTheStatesWithMin.add(new Integer(i));
			}
		}
		// Now, with equal probability, choose between states that have the
		// same minimum number.
		minState = allTheStatesWithMin.get(
				s.nextInt(allTheStatesWithMin.size())).intValue();
		// about half the time, return minState instead of maxState
		// the equality comparison allows for a greater probability 
		// of consecutive true or false. 
		// THIS IS WHAT DECIDES WHO PROBABLY WINS!
		if (q.nextInt(numStates) > numStates / 2)
		{
			return minState;
		}

		return maxState;
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
		String[] lattices =
		{ SquareLattice.DISPLAY_NAME };

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
		String[] folders =
		{ RuleFolderNames.INSTRUCTIONAL_FOLDER, RuleFolderNames.PRETTY_FOLDER };

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
	 * The number of generations that a Cell must store in order for this rule
	 * to work properly. For example, the rule might need to use states from the
	 * current generation as well as 4 previous generations to calculate the
	 * next generation's state. That means this method should return 5. If only
	 * the current generation is required, then this method should return 1. <br>
	 * This method returns a default value of 1, but child classes may override
	 * this method to change its value. <br>
	 * Every rule must at least use the current generation to calculate the next
	 * generation, so a value of at least 1 is necessary. The Cell class will
	 * enforce this, with a warning.
	 * 
	 * @return The number of generations that each cell must save.
	 */
	public int getRequiredNumberOfGenerations()
	{
		return numRequiredGenerations;
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
