package userRules;

import java.util.Random;

import cellularAutomata.lattice.SquareLattice;
import cellularAutomata.lattice.StandardOneDimensionalLattice;
import cellularAutomata.rules.templates.IntegerRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;

/**
 * A rule which calculates the sum modulo N of a cell and one of its neighbors
 * (the last neighbor in its list of neighbors). For nearest neighbors on a
 * one-dimensional lattice, this is the sum modulo N of the cell and its
 * neighbor to the right. For nearest neighbors on a two-dimensional square
 * lattice, this is the sum modulo N of the cell and its neighbor to the left.
 * 
 * @author David Bahr
 */
public class Dice extends IntegerRuleTemplate
{
	/**
	 * A display name for this class. (This is not normally public, but Rule102
	 * is used frequently in the CA Explorer, and this is a handy shortcut. When
	 * writing your own rule, I'd suggest making this variable private.)
	 */
	public static final String RULE_NAME = "Zilch Dice";

	private int score;

	private int numDice;
	
	private int generation;

	// a one line tooltip description for this rule
	private static final String TOOLTIP = "<html> <body><b>Rule 102.</b> Calculates the sum modulo N of a cell "
			+ "and its neighbor to the right.</body></html>";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>Rule 102.</b>"
			+ "<p> <b>For best results</b>, try a one-dimensional (2 neighbor) lattice with two "
			+ "states, wrap around boundaries, and a single seed (preferably on the right-hand "
			+ "edge of the screen). Also works well with more states on large lattices "
			+ "(for example 10 states on a 200 by 200 lattice)."
			+ leftClickInstructions + rightClickInstructions + "</body></html>";

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
	public Dice(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);
	}

	/**
	 * Calculates the sum modulo two of the cell and the last neighbor in its
	 * list of neighbors.
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
		int cellValue = cell;
		
		this.generation = generation;

		if (generation % 2 == 0)
		{
			for (int i = 0; i < neighbors.length; i++)
			{
				if (neighbors[i] != cell)
				{
					int cellScore = 0;
					int neighborScore = 0;

					while (cellScore <= 3000 || neighborScore <= 3000)
					{
						cellScore += gameLogic(cell);

						neighborScore += gameLogic(neighbors[i]);
					}

					if (cellScore > neighborScore)
					{
						return cell;
					}

					else
					{
						return neighbors[i];
					}
				}
			}
		}
		return cellValue;
	}

	public int[] rollDice(int numDice)
	{
		int[] dice = new int[numDice];

		Random r = new Random();

		// roll the dice
		for (int i = 0; i < dice.length; i++)
		{
			dice[i] = r.nextInt(numDice);
		}

		return dice;
	}

	public int calcScore(int[] dice)
	{
		int[] diceValues = new int[6];

		// count how many of each result occurred
		for (int i = 0; i < dice.length; i++)
		{
			diceValues[dice[i]]++;
		}

		// check for points combinations
		for (int i = 0; i < dice.length; i++)
		{
			if (diceValues[i] == 3)
			{
				if (i == 1)
				{
					score += 1000;

					score += (50 * diceValues[4]);

					numDice -= diceValues[4];
					numDice -= 3;

					return score;
				} else
				{
					score += i * 100;

					score += (100 * diceValues[0]);

					score += (50 * diceValues[4]);

					numDice -= diceValues[0];

					numDice -= diceValues[4];

					numDice -= 3;

					return score;
				}
			}
		}

		// count the number of 1's rolled, calculate score
		if (diceValues[0] != 0)
		{
			score += (100 * diceValues[0]);
			numDice -= diceValues[0];
		}

		// count the number of 5's rolled, calculate score
		if (diceValues[4] != 0)
		{
			score += (50 * diceValues[4]);
			numDice -= diceValues[0];
		}

		if (diceValues[0] == 0 || diceValues[4] == 0)
		{
			score = 1;
		}

		if (numDice == 0)
		{
			numDice = 6;
		}

		return score;
	}

	public int gameLogic(int cell)
	{
		numDice = 6;
		score = 0;
		
		Random r = new Random();
		
		int dice1 = r.nextInt(5);
		
		int dice2 = r.nextInt(6);
		
		int score1 = r.nextInt(1000);

		int score2 = r.nextInt(1000);
		
		
		if (cell % 2 == 0)
		{
			while (numDice > dice1 && score < score1)
			{
				int[] dice = rollDice(numDice);

				score += calcScore(dice);

				if (score % 2 != 0)
				{
					return 0;
				}
			}
		}

		else
		{
			while (numDice > dice2 && score < score2)
			{
				int[] dice = rollDice(numDice);

				score += calcScore(dice);

				if (score % 2 != 0)
				{
					return 0;
				}
			}
		}

		return score;
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
