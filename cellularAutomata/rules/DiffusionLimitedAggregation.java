/*
 DiffusionLimitedAggregation -- a class within the Cellular Automaton Explorer. 
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

import java.awt.FlowLayout;
import java.util.Iterator;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import cellularAutomata.CurrentProperties;
import cellularAutomata.Cell;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.rules.templates.IntegerMargolusTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.MinMaxIntPair;
import cellularAutomata.util.math.RandomSingleton;

/**
 * Rule for diffusion limited aggregation. Essentially this is just the
 * Diffusion rule, with any number of gasses. But there is also a special state
 * that acts as a solid. Any other state that comes into contact with the solid
 * will freeze in place. Uses the Margolus neighborhood but could be rewritten
 * as a lattice gas with vector states.
 * 
 * @author David Bahr
 */
public class DiffusionLimitedAggregation extends IntegerMargolusTemplate
{
	/**
	 * The random percent of the gas used as an initial value if no other is
	 * specified.
	 */
	public static final int DEFAULT_RANDOM_PERCENT = 25;

	// The state used for empty space
	private static final int EMPTY_STATE = 0;

	// the state used for the gas
	private static final int GAS_STATE = 1;

	// The state used for the solid
	private static final int SOLID_STATE = 2;

	// A string giving the name of an initial state
	private static final String INIT_STATE_RANDOM_WITH_SEED = "random gas with seed";

	// A string giving a tooltip for the INIT_STATE_RANDOM_WITH_SEED initial
	// state
	private static final String INIT_STATE_RANDOM_WITH_SEED_TOOLTIP = "<html>A random "
			+ "distribution of the gas state <br>"
			+ "with a single solid seed in the center. <br>"
			+ "Set the random percent in the text field <br>" + "above.</html>";

	// a label for the initial state panel
	private static final String RANDOM_PERCENT_LABEL = "random percent gas: ";

	// The tooltip for the random percent
	private static final String RANDOM_PERCENT_TIP = "<html>"
			+ "Percentage of the cells that will be randomly filled <br>"
			+ "with non-empty states.  For example, 80% means that <br>"
			+ "20% of the cells will be blank, and the remainder will <br>"
			+ "be filled.  The filled sites are given values selected <br>"
			+ "uniformly (equally) from each of the non-empty states.</html>";

	// a display name for this class
	private static final String RULE_NAME = "Diffusion Limited Aggregation";

	// used to randomly choose a rearrangement
	private static Random random = RandomSingleton.getInstance();

	// selects the random percent
	private static JSpinner randomPercentSpinner = null;

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, try a 200 by 200 or larger lattice with the \"random gas "
			+ "with seed\" initial state. Choose a <b>20% to 30%</b> random percent (though this "
			+ "number can be interesting to vary).  Watch the crystal slowly grow around the seed "
			+ "(the \"solid\").  Alternatively, select \"random gas with seed\" and draw a line "
			+ "along the bottom. This will produce frost, like humid air condensing on a "
			+ "windowpane in the winter. Or draw a few scattered solid seeds and watch them "
			+ "crystallize and attempt to grow together (but look carefully -- they rarely succeed)."
			+ leftClickInstructions + rightClickInstructions + "</body></html>";

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b> Grows crystals that look like frost on a windowpane.</body></html>";

	/**
	 * Create the diffusion limited aggregation rule using the given cellular
	 * automaton properties.
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
	public DiffusionLimitedAggregation(boolean minimalOrLazyInitialization)
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
		int[] block = new int[4];

		// check to see if any of the cells are solid
		if(northWestCellValue == SOLID_STATE
				|| northEastCellValue == SOLID_STATE
				|| southEastCellValue == SOLID_STATE
				|| southWestCellValue == SOLID_STATE)
		{
			// then any occupied cells must be changed to a solid state
			if(northWestCellValue == GAS_STATE)
			{
				// change to the solid state
				northWestCellValue = SOLID_STATE;
			}
			if(northEastCellValue == GAS_STATE)
			{
				// change to the solid state
				northEastCellValue = SOLID_STATE;
			}
			if(southEastCellValue == GAS_STATE)
			{
				// change to the solid state
				southEastCellValue = SOLID_STATE;
			}
			if(southWestCellValue == GAS_STATE)
			{
				// change to the solid state
				southWestCellValue = SOLID_STATE;
			}

			block[0] = northWestCellValue;
			block[1] = northEastCellValue;
			block[2] = southEastCellValue;
			block[3] = southWestCellValue;
		}
		else
		{
			// diffuse the particles!
			// take the original block values (particles) and rearrange them
			// randomly within the block. Conserves the values (particles).
			// i.e., pick one of the 24 random rearrangements. And then we
			// assign
			// the rearrangement to an array representing the new Margolus
			// block.
			block = rearrangeTheBlock(northWestCellValue, northEastCellValue,
					southEastCellValue, southWestCellValue);
		}

		return block;
	}

	/**
	 * Specifies that the number of states field should be disabled
	 */
	protected MinMaxIntPair getMinMaxAllowedStates(String latticeDescription)
	{
		// disables the “Number of States” text field
		return null;
	}

	/**
	 * Specifies the state value that will be displayed
	 */
	protected Integer stateValueToDisplay(String latticeDescription)
	{
		// fixes the state value at 3
		return new Integer(3);
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
	 * Gets an array of names for any initial states defined by the rule. In
	 * other words, if the rule has two initial states that it would like to
	 * define, then they are each given names and placed in an array of size 2
	 * that is returned by this method. The names are then displayed on the
	 * Properties panel. The CA will always have "single seed", "random", and
	 * "blank" for initial states. This array specifies additional initial
	 * states that should be displayed. The names should be unique. By default
	 * this returns null. Child classes should override the method if they wish
	 * to specify initial states that will appear on the Properties panel. Note:
	 * This method should be used in conjuction with the setInitialState method,
	 * also in this class.
	 * 
	 * @return An array of names for initial states that are specified by the
	 *         rule.
	 */
	public String[] getInitialStateNames()
	{
		return new String[] {INIT_STATE_RANDOM_WITH_SEED};
	}

	/**
	 * Gets an array of JPanels that will be displayed as part of this rule's
	 * initial states. The order and size should be the same as the the initial
	 * states in the getInitialStateNames() method. There is no requirement that
	 * any panel be displayed, and any of the array elements may be null. The
	 * entire array may be null.
	 * <p>
	 * Strongly recommended that graphics components on the JPanels be static so
	 * that they apply to all cells. Otherwise, the graphics displayed on the
	 * Initial State tab will not be the same graphics associated with each
	 * cell.
	 * <p>
	 * Note: This method should be used in conjuction with the
	 * getInitialStateNames and setInitialState methods, also in this class.
	 * <p>
	 * Child classes should override the default behavior which returns null,
	 * which displays no panel. See DiffusionLimitedAggregation as an example.
	 * <p>
	 * Values for components in the panel can be saved and reset from the
	 * properties. When creating the initial state in setInitialState(), just
	 * save the values using "properties.setProperty(key, value)". The next time
	 * the application is started, the values can be read in this method and
	 * used to set the components initial value. Read the value using
	 * "properties.getProperty(key);". This allows persistence across sessions.
	 * In other words, if the user closes the application, it can be reopened
	 * with the same values by reading the previously saved property values. To
	 * prevent property naming conflicts, please start every key with the class
	 * name of the rule. For example, key = "DiffusionLimitedAggregation:
	 * random" (see the DiffusionLimitedAggregation class).
	 * 
	 * @return An array indicating what panels should be displayed as part of
	 *         the initial states. If null, no panel will be displayed for any
	 *         initial states that are specified by this rule. If any element of
	 *         the arry is null, then no panel will be displayed for that
	 *         corresponding initial state.
	 */
	public JPanel[] getInitialStateJPanels()
	{
		// get the percentage used the last time the program was run
		int randomPercent = CurrentProperties.getInstance()
				.getRandomPercentDiffusionGas();

		// create a label
		JLabel percentLabel = new JLabel(RANDOM_PERCENT_LABEL);

		// create spinner for the random field
		SpinnerNumberModel randomPercentModel = new SpinnerNumberModel(
				randomPercent, 0, 100, 1);
		randomPercentSpinner = new JSpinner(randomPercentModel);
		randomPercentSpinner.setToolTipText(RANDOM_PERCENT_TIP);

		// add listeners
		// randomPercentSpinner.addChangeListener(listener);
		// randomPercentSpinner.addChangeListener(this);
		// ((JSpinner.NumberEditor) randomPercentSpinner.getEditor())
		// .getTextField().getDocument().addDocumentListener(this);

		// now create the panel that holds the spinner
		JPanel initialStatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		initialStatePanel.add(percentLabel);
		initialStatePanel.add(randomPercentSpinner);

		JPanel[] arrayOfPanels = {initialStatePanel};

		return arrayOfPanels;
	}

	/**
	 * Gets tool tips for any initial states defined by the rule. The tool tips
	 * should be given in the same order as the initial state names in the
	 * method getInitialStateNames. The tool tip array must be null or the same
	 * length as the array of initial state names. By default this returns null.
	 * Child classes should override the method if they wish to specify initial
	 * state tool tips that will appear on the Properties panel. Note: This
	 * method should be used in conjuction with the getInitialStateNames and
	 * setInitialState method, also in this class.
	 * 
	 * @return An array of tool tips for initial states that are specified by
	 *         the rule. May be null. Any element of the array may also be null,
	 *         but if the length of the array is non-zero, then the length must
	 *         be the same as the array returned by getInitialStateNames.
	 */
	public String[] getInitialStateToolTips()
	{
		return new String[] {INIT_STATE_RANDOM_WITH_SEED_TOOLTIP};
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
	 * Gets an initial state that corresponds to the specified initialStateName.
	 * This method is optional and returns null by default.
	 * <p>
	 * If a rule wishes to define one or more initial states (different from the
	 * "single seed", "random", and "blank" that are already provided), then the
	 * name of the state is specified in the getInitialStateNames() method. The
	 * name is displayed on the Initial States tab, and if it is selected, then
	 * this method is called with that name. Based on the name, this method can
	 * then specify an initial state.
	 * <p>
	 * By default this returns null. Child classes should override the method if
	 * they wish to specify initial states that will appear on the Initial
	 * States tab.
	 * <p>
	 * The lattice parameter is used to retrieve an iterator over the cells and
	 * assign the initial values to the cells. An example is <code>
	 *   //assigns the same double value to each cell.
	 *   Iterator cellIterator = lattice.iterator();
	 *   while(cellIterator.hasNext())
	 *   {
	 *       Cell cell = (Cell) cellIterator.next();
	 *
	 *       // assign the value
	 *       cell.getState().setValue(new Double(3.4));
	 *   }
	 * </code>
	 * Here is another example that assigns values to the cell at both the
	 * current generation and previous generations. The number of generations in
	 * the stateHistory variable (below) would be determined by the method
	 * getRequiredNumberOfGenerations in this class. <code>
	 *   //assign the same double value to each cell
	 *   Double double = new Double(9.3);
	 *   Iterator cellIterator = lattice.iterator();
	 *   while(cellIterator.hasNext())
	 *   {
	 *       Cell cell = (Cell) cellIterator.next();
	 *
	 *       // get the list of states for each cell
	 *       ArrayList stateHistory = cell.getStateHistory();
	 *       int historySize = stateHistory.size();
	 *
	 *       // there may be more than one state required as initial conditions
	 *       for(int i = 0; i < historySize; i++)
	 *       {
	 *            ((CellState) stateHistory.get(i)).setValue(double);
	 *       }
	 *  }
	 * </code>
	 * Another example is given in the Fractal rule. That example figures out
	 * the row and column number of each cell and assigns values based on their
	 * row and column.
	 * <p>
	 * Note: This method should be used in conjuction with the getInitialState
	 * method, also in this class.
	 * <p>
	 * The method getInitialStateJPanels() will create a panel (with components
	 * if desired) that is displayed on the Initial States tab. See
	 * DiffusionLimitedAggregation for an example. Values for components in this
	 * panel can be read and used by this method when setting the initial state.
	 * Values for components in the panel can also be saved in the properties.
	 * In this method, save the values using "properties.setProperty(key,
	 * value)". The next time the application is started, the values can be read
	 * in the method getInitialStateJPanels() and used to set the components
	 * initial value. Read the value using "properties.getProperty(key);". This
	 * allows persistence across sessions. In other words, if the user closes
	 * the application, it can be reopened with the same values by reading the
	 * previously saved property values. To prevent property naming conflicts,
	 * please start every key with the class name of the rule. For example, key =
	 * "DiffusionLimitedAggregation: random" (see the
	 * DiffusionLimitedAggregation class).
	 * <p>
	 * By default this method does nothing.
	 * 
	 * @param initialStateName
	 *            The name of the initial state (will be one of the names
	 *            specified in the getInitialStateNames method).
	 * @param lattice
	 *            The CA lattice. This will either be a one or two-dimensional
	 *            lattice which holds the cells. The cells should be collected
	 *            from the lattice and assigned initial values.
	 */
	public void setInitialState(String initialStateName, Lattice lattice)
	{
		// get the number of rows and columns
		int numRows = CurrentProperties.getInstance().getNumRows();
		int numCols = CurrentProperties.getInstance().getNumColumns();

		// instantiate if necessary
		if(randomPercentSpinner == null)
		{
			// spinner is instantiated in here
			getInitialStateJPanels();
		}

		// get the percent that will be random
		double percent = DEFAULT_RANDOM_PERCENT / 100.0;
		try
		{
			randomPercentSpinner.commitEdit();
		}
		catch(Exception e)
		{
			// do nothing
		}

		Integer percentValueFromSpinner = (Integer) randomPercentSpinner
				.getValue();
		percent = percentValueFromSpinner.doubleValue() / 100.0;

		// save the value for future reference (e.g., when the application is
		// restarted)
		CurrentProperties.getInstance().setRandomPercentDiffusionGas(
				percentValueFromSpinner);

		// the number of cells through which we have iterated
		int cellNum = 0;

		// iterate through each cell and set it's value
		Iterator cellIterator = lattice.iterator();
		while(cellIterator.hasNext())
		{
			// get each cell
			Cell cell = (Cell) cellIterator.next();

			// get the row and col of this cell
			int row = cellNum / numRows; // integer division on purpose!
			int col = cellNum % numCols;

			// assign the value to the cell
			if((row == numRows / 2) && (col == numCols / 2)
					&& initialStateName.equals(INIT_STATE_RANDOM_WITH_SEED))
			{
				// then it's the cell in the middle, so assign the solid state
				cell.getState().setValue(new Integer(SOLID_STATE));
			}
			else
			{
				// assign a gas state with the probability given by "percent"
				if(random.nextDouble() < percent)
				{
					cell.getState().setValue(new Integer(GAS_STATE));
				}
				else
				{
					cell.getState().setValue(new Integer(EMPTY_STATE));
				}
			}

			// increments the number of cells through which we have iterated
			cellNum++;
		}
	}
}
