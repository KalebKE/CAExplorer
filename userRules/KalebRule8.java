package userRules;

import cellularAutomata.lattice.SquareLattice;
import cellularAutomata.rules.templates.MultiGenerationIntegerRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import java.util.Random;

public class KalebRule8 extends MultiGenerationIntegerRuleTemplate
{
	/**
	 * A display name for this class. (This is not normally public, but Rule102
	 * is used frequently in the CA Explorer, and this is a handy shortcut. When
	 * writing your own rule, I'd suggest making this variable private.)
	 */
	private static final String RULE_NAME = "KalebRule8";

	// a one line tooltip description for this rule
	private static final String TOOLTIP = "<html> <body><b>KalebRule5.</b> A CA that has cells "
			+ "that wants to be like their neighbors, most of the time.</body></html>";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>KalebRule1.</b>"
			+ "<p> <b>For best results</b>, use a square lattice with 4 "
			+ "states, wrap around boundaries, and a probability of .25 for all four initial"
			+ "states. The larger the lattice the better."
			+ leftClickInstructions + rightClickInstructions + "</body></html>";

	// the number of generations required to calculate the next generation's
	// state. When 1, that means just the current generation. When 2, that means
	// both the current generation and the previous generation.
	private final int numRequiredGenerations = 2;

	/**
	 * Create the rule corresponding to the number 102 (a la Wolfram) using the
	 * given cellular automaton properties.
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
	public KalebRule8(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);
	}

	/**
	 * KalebRule7 is based very heavily on Majority Wins and Majority Probably
	 * wins. The code is basically the same, but there are some
	 * major implementation changes that allow it to sit somewhere between
	 * Majority Wins and Minority Wins CA. This is how the model was
	 * constructed: The state of cell represents a behavior. Most of the time,
	 * the cell wants to act like its neighbors. A cell looks at its neighbors
	 * current behavior, and their previous behavior, to decide what its new
	 * state will be. There is a chance that a cell may be defiant and will
	 * chose a completely random state. Randomness is required to drive the
	 * model, otherwise it wants to to find a state of equilibrium, which it
	 * does, and nothing happens. This CA calculates the probabilities for 
	 * the current and previous states. The current and previous number of times
	 * a state occurs as a cells neighbor is also recorded. Then the average 
	 * probabilities for each state are REVERSED and multiplied by the total number
	 * of states.
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
		// store the number of states in the neighborhood
		double zeroCount = 0;
		double oneCount = 0;
		double twoCount = 0;
		double threeCount = 0;
		// weight matrix 1
		double weightA = .25;
		double weightB = .25;
		double weightC = .25;
		double weightD = .25;
		// weight matrix 2
		double weightE = .60;
		double weightF = .10;
		double weightG = .10;
		double weightH = .20;
		// weight matrix 3
		double weightI = .10;
		double weightJ = .20;
		double weightK = .60;
		double weightL = .10;
		// stores the results of alpha (counts*weights)
		double alphaA = 0;
		double alphaB = 0;
		double alphaC = 0;
		double alphaD = 0;
		// stores the largest alpha cell value
		double largest = 0;

		int sum = 0;
		// defines neighbors
		int leftUp = neighbors[0][0];
		int up = neighbors[1][0];
		int rightUp = neighbors[2][0];
		int left = neighbors[7][0];
		int right = neighbors[3][0];
		int downLeft = neighbors[6][0];
		int down = neighbors[5][0];
		int downRight = neighbors[4][0];

		// random number generators (very important)
		Random r = new Random();
		Random q = new Random();

		// array to store counts
		double[] counts =
		{ zeroCount, oneCount, twoCount, threeCount };
		// arrays to store weights, arrays are selected randomly
		double[] weights1 =
		{ weightA, weightB, weightC, weightD };
		double[] weights2 =
		{ weightE, weightF, weightG, weightH };
		double[] weights3 =
		{ weightI, weightJ, weightK, weightL };
		// array to store alphas
		double[] alphas =
		{ alphaA, alphaB, alphaC, alphaD };

		// looks at every neighbor and counts their state
		if (leftUp == 0)
		{
			counts[0]++;
		}
		if (leftUp == 1)
		{
			counts[1]++;
		}
		if (leftUp == 2)
		{
			counts[2]++;
		}
		if (leftUp == 3)
		{
			counts[3]++;
		}
		if (up == 0)
		{
			counts[0]++;
		}
		if (up == 1)
		{
			counts[1]++;
		}
		if (up == 2)
		{
			counts[2]++;
		}
		if (up == 3)
		{
			counts[3]++;
		}
		if (rightUp == 0)
		{
			counts[0]++;
		}
		if (rightUp == 1)
		{
			counts[1]++;
		}
		if (rightUp == 2)
		{
			counts[2]++;
		}
		if (rightUp == 3)
		{
			counts[3]++;
		}
		if (left == 0)
		{
			counts[0]++;
		}
		if (left == 1)
		{
			counts[1]++;
		}
		if (left == 2)
		{
			counts[2]++;
		}
		if (left == 3)
		{
			counts[3]++;
		}
		if (right == 0)
		{
			counts[0]++;
		}
		if (right == 1)
		{
			counts[1]++;
		}
		if (right == 2)
		{
			counts[2]++;
		}
		if (right == 3)
		{
			counts[3]++;
		}
		if (downLeft == 0)
		{
			counts[0]++;
		}
		if (downLeft == 1)
		{
			counts[1]++;
		}
		if (downLeft == 2)
		{
			counts[2]++;
		}
		if (downLeft == 3)
		{
			counts[3]++;
		}
		if (down == 0)
		{
			counts[0]++;
		}
		if (down == 1)
		{
			counts[1]++;
		}
		if (down == 2)
		{
			counts[2]++;
		}
		if (down == 3)
		{
			counts[3]++;
		}
		if (downRight == 0)
		{
			counts[0]++;
		}
		if (downRight == 1)
		{
			counts[1]++;
		}
		if (downRight == 2)
		{
			counts[2]++;
		}
		if (downRight == 3)
		{
			counts[3]++;
		}

		
		// randomly selects a weight matrix and multiplies the counts
		int t = r.nextInt(3);
		if (t == 1)
		{
			for (int i = 0; i < counts.length; i++)
			{
				alphas[i] = counts[i] * weights1[i];
			}
		}
		if (t == 2)
		{
			for (int i = 0; i < counts.length; i++)
			{
				alphas[i] = counts[i] * weights2[i];
			}
		}
		if (t == 3)
		{
			for (int i = 0; i < counts.length; i++)
			{
				alphas[i] = counts[i] * weights3[i];
			}
		} else
		{
			for (int i = 0; i < counts.length; i++)
			{
				alphas[i] = counts[i] * weights1[i];
			}
		}

		// finds the largest alpha
		for (int i = 0; i < counts.length; i++)
		{
			if (largest < alphas[i])
			{
				largest = alphas[i];
			}
		}

		// assigns a state for the cell
		if (largest == (alphas[0]))
		{
			// if two states have the same alpha, a state is randomly selected
			if ((alphas[0]) == (alphas[1]) || (alphas[0]) == (alphas[2])
					|| (alphas[0]) == (alphas[3]))
			{
				System.out.println("Definace!");
				return sum = r.nextInt(4);
			} else
			{
				sum = 0;
			}
		}
		if (largest == (alphas[1]))
		{
			// if two states have the same alpha, a state is randomly selected
			if ((alphas[1]) == (alphas[0]) || (alphas[1]) == (alphas[2])
					|| (alphas[1]) == (alphas[3]))
			{
				System.out.println("Definace!");
				return sum = q.nextInt(4);
			} else
			{
				sum = 1;
			}
		}
		if (largest == (alphas[2]))
		{
			// if two states have the same alpha, a state is randomly selected
			if ((alphas[2]) == (alphas[1]) || (alphas[2]) == (alphas[0])
					|| (alphas[2]) == (alphas[3]))
			{
				System.out.println("Definace!");
				return sum = r.nextInt(4);

			} else
			{
				sum = 2;
			}
		}
		if (largest == (alphas[3]))
		{
			// if two states have the same alpha, a state is randomly selected
			if ((alphas[3]) == (alphas[1]) || (alphas[3]) == (alphas[2])
					|| (alphas[3]) == (alphas[0]))
			{
				System.out.println("Definace!");
				r.nextInt(4);
				return sum = r.nextInt(4);
			} else
			{
				sum = 3;
			}
		}
		// adds another element of randomness, this loop is very important
		if (r.nextInt(5) == counts.length)
		{
			sum = q.nextInt(4);
		}

		return sum;
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
	@Override
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
