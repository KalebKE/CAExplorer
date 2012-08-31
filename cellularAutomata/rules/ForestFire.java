/*
 ForestFire -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2007  David B. Bahr (http://academic.regis.edu/dbahr/)

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

import java.awt.Color;
import java.awt.Shape;
import java.util.Random;

import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.view.CellStateView; // import
// cellularAutomata.cellState.view.TriangleHexagonCellStateView;
import cellularAutomata.rules.templates.FiniteObjectRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.math.RandomSingleton;

/**
 * Rule that mimics a forest fire. Trees are born and mature at a specified
 * rate. Fires start with a certain probability. Mature trees will burn if a
 * neighboring tree is burning.
 * 
 * @author David Bahr
 */
public class ForestFire extends FiniteObjectRuleTemplate
{
	// a display name for this class
	private static final String RULE_NAME = "Forest Fire";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, start with a blank (empty) initial "
			+ "state of all bare ground.  Use a large two-dimensional "
			+ "nearest-neighbor simulation.  For example, try a 150 by 150 or larger "
			+ "square (8 neighbor) lattice.  From the empty bare ground, trees will "
			+ "sprout and mature. Eventually lightning will start a fire. The trees "
			+ "will regrow and the cycle of fire and regrowth will repeat."
			+ "Be patient.  It takes time for the trees to mature and then burn."
			+ leftClickInstructions + rightClickInstructions + "</body></html>";

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b> Forests grow, mature, and then eventually burn.</body></html>";

	private static Random random = RandomSingleton.getInstance();

	// probability that a tree grows from bare ground
	private double newTreeProbabililty = 0.05;

	// probability that a tree matures to the next stage
	private double matureProbabililty = 0.01;

	// probability that a mature tree will die
	private double deathProbabililty = 0.0;

	// probability that a mature tree catches fire (lightning strike)
	private double lightningStrikeProbabililty = 0.00001;

	/**
	 * Create a forest fire using the given cellular automaton properties.
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
	public ForestFire(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);
	}

	/**
	 * Returns a list of permissable states. Each one has a unique string
	 * representation of "Mature tree", "Seedling", "Burning", etc.
	 * 
	 * @param properties
	 *            The CA properties.
	 * @return An array of allowed states for the cells.
	 */
	protected Object[] getObjectArray()
	{
		Tree matureTree = new Tree(Tree.MATURE_TREE_STATE);
		Tree youngTree = new Tree(Tree.YOUNG_TREE_STATE);
		Tree saplingTree = new Tree(Tree.SAPLING_TREE_STATE);
		Tree seedlingTree = new Tree(Tree.SEEDLING_TREE_STATE);
		Tree bareGround = new Tree(Tree.BARE_GROUND_STATE);
		Tree burning = new Tree(Tree.BURNING_STATE);
		Tree smoldering = new Tree(Tree.SMOLDERING_STATE);
		Tree ashes = new Tree(Tree.ASHES_STATE);

		// the list of possible states
		Object[] listOfObjects = {bareGround, seedlingTree, saplingTree,
				youngTree, matureTree, ashes, smoldering, burning};

		return listOfObjects;
	}

	/**
	 * Rules for forest fires. Trees mature in a progression from seedling to
	 * mature and finally dead. At any time, a mature tree may be struck by
	 * lightning and start a fire. Neighboring mature trees will then catch
	 * fire.
	 * 
	 * @param cell
	 *            The value of the cell being updated.
	 * @param neighbors
	 *            The value of the neighbors.
	 * @param generation
	 *            The current generation of the CA.
	 * @return A new state for the cell.
	 */
	protected Object objectRule(Object cell, Object[] neighbors, int generation)
	{
		// get the values of the cell and the neighbors
		Tree theTree = (Tree) cell;
		Tree[] neighborTrees = new Tree[neighbors.length];
		for(int i = 0; i < neighbors.length; i++)
		{
			neighborTrees[i] = (Tree) neighbors[i];
		}

		// the new value that we will return. Note that I am careful not to
		// change the current value because that will mess up calculations done
		// for neighboring states.
		Tree newValue = null;

		// should the tree burn?
		if(theTree.isMature())
		{
			// check to see if lightning strikes
			if(random.nextDouble() < this.lightningStrikeProbabililty)
			{
				// then the tree was hit by lightning and should burn
				newValue = new Tree(Tree.BURNING_STATE);
			}
			else
			{
				// check to see if any neighbors are burning
				boolean burningNeighbor = false;
				for(int i = 0; i < neighborTrees.length && !burningNeighbor; i++)
				{
					if(neighborTrees[i].isBurning())
					{
						burningNeighbor = true;

						// then the tree should burn
						newValue = new Tree(Tree.BURNING_STATE);
					}
				}
			}

			// otherwise the mature tree stays mature or dies (doesn't burn and
			// doesn't
			// grow any further)
			if(newValue == null)
			{
				if(random.nextDouble() < this.deathProbabililty)
				{
					// dies
					newValue = new Tree(Tree.BARE_GROUND_STATE);
				}
				else
				{
					// lives
					newValue = new Tree(Tree.MATURE_TREE_STATE);
				}
			}
		}
		else if(theTree.isBareGround())
		{
			// see if a tree should sprout
			if(random.nextDouble() < this.newTreeProbabililty)
			{
				newValue = new Tree(Tree.SEEDLING_TREE_STATE);
			}
			else
			{
				// stay the same
				newValue = new Tree(theTree);
			}
		}
		else if(theTree.isAlive() && !theTree.isMature())
		{
			// see if the tree should grow up
			if(random.nextDouble() < this.matureProbabililty)
			{
				newValue = new Tree(theTree.getNextState());
			}
			else
			{
				// stay the same
				newValue = new Tree(theTree);
			}
		}
		else
		{
			newValue = new Tree(theTree.getNextState());
		}

		// be safe
		if(newValue == null)
		{
			// stay the same
			newValue = new Tree(theTree);
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
	 * Gets an instance of the CellStateView class that will be used to display
	 * cells being updated by this rule. Note: This method must return a view
	 * that is able to display cell states of the type returned by the method
	 * getCompatibleCellState(). Appropriate CellStatesViews to return include
	 * BinaryCellStateView, IntegerCellStateView, HexagonalIntegerCellStateView,
	 * IntegerVectorArrowView, IntegerVectorDefaultView, and
	 * RealValuedDefaultView among others. the user may also create their own
	 * views (see online documentation).
	 * <p>
	 * Any values passed to the constructor of the CellStateView should match
	 * those values needed by this rule.
	 * 
	 * @return An instance of the CellStateView (any values passed to the
	 *         constructor of the CellStateView should match those values needed
	 *         by this rule).
	 */
	public  CellStateView getCompatibleCellStateView()
	{
		return new ForestFireView();
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

	/**
	 * An object that represents a tree. The tree may be a seedling, sapling,
	 * young, or mature. It may also be burning, smoldering, or dead. It may
	 * also be bare ground (pine cone).
	 * 
	 * @author David Bahr
	 */
	private class Tree
	{
		/**
		 * The state of a mature tree.
		 */
		public static final String MATURE_TREE_STATE = "Mature tree";

		/**
		 * The state of a young tree.
		 */
		public static final String YOUNG_TREE_STATE = "Young tree";

		/**
		 * The state of a sapling tree.
		 */
		public static final String SAPLING_TREE_STATE = "Sapling";

		/**
		 * The state of a seedling tree.
		 */
		public static final String SEEDLING_TREE_STATE = "Seedling";

		/**
		 * The state of bare ground.
		 */
		public static final String BARE_GROUND_STATE = "Bare ground";

		/**
		 * The state of a burning tree.
		 */
		public static final String BURNING_STATE = "Burning";

		/**
		 * The state of a smoldering tree.
		 */
		public static final String SMOLDERING_STATE = "Smoldering";

		/**
		 * The state of a ashes.
		 */
		public static final String ASHES_STATE = "Ashes";

		// the current state of this object
		private String currentState = BARE_GROUND_STATE;

		// An array of all the states
		private String[] allStates = {BARE_GROUND_STATE, SEEDLING_TREE_STATE,
				SAPLING_TREE_STATE, YOUNG_TREE_STATE, MATURE_TREE_STATE,
				BURNING_STATE, SMOLDERING_STATE, ASHES_STATE};

		// An array of non-burning states, listed in order
		private String[] onlyNonBurningStates = {BARE_GROUND_STATE,
				SEEDLING_TREE_STATE, SAPLING_TREE_STATE, YOUNG_TREE_STATE,
				MATURE_TREE_STATE};

		// An array of tree states (no burning or bare ground states), listed in
		// order
		private String[] onlyTreeStates = {SEEDLING_TREE_STATE,
				SAPLING_TREE_STATE, YOUNG_TREE_STATE, MATURE_TREE_STATE};

		// An array of burning states, listed in order. Includes the bare ground
		// state which follows the ashes.
		private String[] onlyBurningStates = {BURNING_STATE, SMOLDERING_STATE,
				ASHES_STATE, BARE_GROUND_STATE};

		/**
		 * Create a tree state corresponding to the specified string. Valid
		 * strings are MATURE_TREE_STATE, YOUND_TREE_STATE, BURNING_STATE, etc.
		 */
		public Tree(String state)
		{
			if(state.equals(MATURE_TREE_STATE)
					|| state.equals(YOUNG_TREE_STATE)
					|| state.equals(SAPLING_TREE_STATE)
					|| state.equals(SEEDLING_TREE_STATE)
					|| state.equals(BARE_GROUND_STATE)
					|| state.equals(BURNING_STATE)
					|| state.equals(SMOLDERING_STATE)
					|| state.equals(ASHES_STATE))
			{
				currentState = state;
			}
			else
			{
				currentState = BARE_GROUND_STATE;
			}
		}

		/**
		 * Create a random tree (but not burning tree) state.
		 */
		public Tree()
		{
			int choice = random.nextInt(onlyNonBurningStates.length);

			currentState = onlyNonBurningStates[choice];
		}

		/**
		 * Create a ChainLinkFence with the same state as the parameter.
		 * 
		 * @param treeState
		 *            This new object will be assigned the same state as
		 *            treeState.
		 */
		public Tree(Tree treeState)
		{
			this.currentState = treeState.getState();
		}

		/**
		 * Gets the string representing the next state that follows the current
		 * state. For example, a seedling matures to a sapling, and a sapling
		 * matures to a young tree, etc. The next state for a mature tree is a
		 * burning tree. The next state for ashes is bare ground. The next state
		 * for bare ground is a seedling.
		 * 
		 * @return The string representing the next state that follows the
		 *         current state.
		 */
		public String getNextState()
		{
			// find the current state position in the "allState" array
			int position = 0;
			while(!allStates[position].toString().equals(currentState))
			{
				position++;
			}

			// so the next state is the next position
			position++;

			// wrap to the beginning if necessary
			position %= allStates.length;

			return allStates[position];
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
		 * True if the "tree" is actualy just bare ground.
		 * 
		 * @return true if bare ground.
		 */
		public boolean isBareGround()
		{
			boolean bare = false;
			if(currentState.equals(Tree.BARE_GROUND_STATE))
			{
				bare = true;
			}

			return bare;
		}

		/**
		 * True if the tree is burning or smoldering.
		 * 
		 * @return true if burning or smoldering.
		 */
		public boolean isBurning()
		{
			boolean burning = false;
			if(currentState.equals(Tree.BURNING_STATE)
					|| currentState.equals(Tree.SMOLDERING_STATE))
			{
				burning = true;
			}

			return burning;
		}

		/**
		 * True if the tree is mature.
		 * 
		 * @return true if mature.
		 */
		public boolean isMature()
		{
			boolean mature = false;
			if(currentState.equals(Tree.MATURE_TREE_STATE))
			{
				mature = true;
			}

			return mature;
		}

		/**
		 * True if the tree is alive.
		 * 
		 * @return true if the tree is alive (not burning and not bare ground).
		 */
		public boolean isAlive()
		{
			boolean alive = false;
			for(int i = 0; i < onlyTreeStates.length; i++)
			{
				if(currentState.equals(onlyTreeStates[i].toString()))
				{
					alive = true;
				}
			}

			return alive;
		}

		/**
		 * Sets the state to the value of the specified ChainLinkFence object.
		 * 
		 * @param treeState
		 *            The value of this object will be set to the same state as
		 *            treeState.
		 */
		public void setStateToSameValue(Tree treeState)
		{
			currentState = treeState.toString();
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
	}

	/**
	 * A view that is specific to the Objects being displayed for this forest
	 * fire rule. Tells the graphics how to display the Object stored by a cell.
	 * 
	 * @author David Bahr
	 */
	private class ForestFireView extends CellStateView
	// extends TriangleHexagonCellStateView
	{
		// colors associated with each state
		private Color[] treeColors = {new Color(162, 128, 94),
				new Color(0, 240, 0), new Color(0, 200, 0),
				new Color(0, 160, 0), new Color(0, 120, 0), Color.BLACK,
				Color.RED, Color.YELLOW.brighter()};

		/**
		 * Create a view for the forest fire.
		 */
		public ForestFireView()
		{
		}

		/**
		 * The colors of each cell are usually based on the selected color
		 * scheme, but sometimes the view may wish to prevent the colors from
		 * changing. For example the ForestFire rule wants "tree states" to be
		 * green no matter what.
		 * <p>
		 * The default behavior is to allow all color schemes, but child classes
		 * may override this method to prevent color schemes from being
		 * displayed in the menu. In general, this method should return false if
		 * the rule wants to create a CellStateView that uses fixed colors (that
		 * won't channge with the scheme).
		 * 
		 * @return true if all color schemes are allowed and will be enabled in
		 *         the menu, and false if the color schemes will be disabled in
		 *         the menu.
		 */
		public boolean enableColorSchemes()
		{
			return false;
		}

		/**
		 * Returns null so that the default shape is used (a square).
		 * 
		 * @see cellularAutomata.cellState.view.CellStateView#getAverageDisplayShape(
		 *      cellularAutomata.cellState.model.CellState[], int, int,
		 *      Coordinate)
		 */
		public Shape getAverageDisplayShape(CellState[] states, int width,
				int height, Coordinate rowAndCol)
		{
			return null;
		}

		/**
		 * Creates a display color based on the maturity of the tree and the
		 * state of the fire.
		 * 
		 * @param state
		 *            The cell state that will be displayed.
		 * @param numStates
		 *            If relevant, the number of possible states (which may not
		 *            be the same as the currently active number of states) --
		 *            may be null which indicates that the number of states is
		 *            inapplicable or that the currently active number of states
		 *            should be used. (See for example,
		 *            createProbabilityChoosers() method in InitialStatesPanel
		 *            class.)
		 * @param rowAndCol
		 *            The row and col of the cell being displayed. May be
		 *            ignored.
		 * @return The color to be displayed.
		 */
		public Color getColor(CellState state, Integer numStates,
				Coordinate rowAndCol)
		{
			int stateNumber = state.toInt();

			// the pink color is irrelevant -- it's in case something weird
			// happens, and the stateNumber is outside its correct range of 0
			// to treeColors.length
			Color color = Color.PINK;
			if(stateNumber >= 0 && stateNumber < treeColors.length)
			{
				color = treeColors[stateNumber];
			}
			return color;
		}

		/**
		 * Returns null so that the default shape is used (a square).
		 * 
		 * @see cellularAutomata.cellState.view.CellStateView#getDisplayShape(CellState,
		 *      int, int, Coordinate)
		 */
		public Shape getDisplayShape(CellState state, int width, int height,
				Coordinate rowAndCol)
		{
			return null;
		}

		/**
		 * When a CellState is "tagged" for extra visibility, this method is
		 * called and creates an appropriate color that stands out.
		 * 
		 * @param originalColor
		 *            The original color that will be modified with the tagged
		 *            color.
		 * @param taggingColor
		 *            The tagging color used to modify the original color.
		 * @return The original color, but modified with the tagged color for
		 *         high visibility.
		 * @see cellularAutomata.cellState.view.CellStateView#modifyColorWithTaggedColor(
		 *      Color, Color)
		 */
		public Color modifyColorWithTaggedColor(Color originalColor,
				Color taggingColor)
		{
			// add blue (and reduce green and red) so is different from the red
			// and green of this rule
			int newRed = (int) (1.0 * taggingColor.getRed());
			int newGreen = (int) (1.0 * taggingColor.getGreen());
			int newBlue = Math.max(180, taggingColor.getBlue());

			taggingColor = new Color(newRed, newGreen, newBlue);

			return taggingColor;
		}
	}
}
