/*
 ThreeXPlusOne -- a class within the Cellular Automaton Explorer. 
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

package userRules;

import java.awt.Color;

import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.TriangleHexagonCellStateView;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.rules.templates.IntegerRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.Coordinate;

/**
 * A rule which uses the 3x+1 problem to calculate the next state of a cell. x
 * is assumed to be the average of the cell and it's neighboring cells.
 * 
 * @author David Bahr
 */
public class ThreeXPlusOne extends IntegerRuleTemplate
{
	// a display name for this class
	private static final String RULE_NAME = "3x+1";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, try a single seed with 10 states and a 200 by 200 "
			+ "with a hexagonal lattice.  This simulation is slow because it uses hexagonal "
			+ "graphics, but the results are one of the most <i>stunning</i> snowflakes that a "
			+ "CA can produce!"
			+ "<p> Also start from a single seed with 13 states on a 200 by 200 "
			+ "one-dimensional (2-neighbor) lattice so that the fractal behavior is "
			+ "obvious.  Do not use a small number of states (boring).  Instead, use at least 5 "
			+ "states. Then try a progressively larger number of states like, "
			+ "130 or 1300. "
			+ "<p>"
			+ "Large neighborhoods also give \"smoother\" results.  For example, if you "
			+ "have enough computer memory, try starting from a single seed with 10 states "
			+ "and a 200 by 200 square (24 next-nearest) lattice. "
			+ "<p>"
			+ "To emphasize the structure, try choosing alternate color schemes like \"Fire\". "
			+ leftClickInstructions + rightClickInstructions + "</body></html>";

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b> Stunning images showing fractal structures inherent in the 3x+1 problem."
			+ "</body></html>";

	/**
	 * Create the rule corresponding to the 3x+1 problem using the given
	 * cellular automaton properties.
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
	public ThreeXPlusOne(boolean minimalOrLazyInitialization)
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
	 * @return A new state for the cell.
	 */
	protected int integerRule(int cell, int[] neighbors, int numStates,
			int generation)
	{
		// add up the value of all the cells
		int sum = cell;
		for(int i = 0; i < neighbors.length; i++)
		{
			sum += neighbors[i];
		}

		// divide by the number of neighbors (and the cell), and then use
		// ceil to ensure it is an integer
		sum = (int) Math.ceil((double) sum / (double) neighbors.length);

		// apply the "3x+1" rules
		if(sum % 2 == 0)
		{
			sum /= 2;
		}
		else
		{
			sum = 3 * sum + 1;
		}

		// THIS WORKS WELL!
		// and make sure doesn't exceed the max number of states
		if(sum >= numStates - 1)
		{
			// states go from 0 to numStates-1, so this sets it to the max
			sum = sum % numStates;
		}

		// THIS ALSO WORKS WELL!
		// and make sure doesn't exceed the max number of states
		// if(sum >= numStates-1)
		// {
		// //states go from 0 to numStates-1, so this sets it to the max
		// sum = numStates - 1;
		// }

		// THIS ALSO WORKS WELL!
		// while(sum >= numStates-1)
		// {
		// // states go from 0 to numStates-1, so this resets it to be below the
		// max
		// sum /= 2;
		// }

		// THIS ALSO WORKS WELL!
		// and make sure doesn't exceed the max number of states
		// if(sum >= numStates - 1)
		// {
		// // states go from 0 to numStates-1, so this sets it to the max
		// sum = numStates - 1;
		// }

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
		return new ThreeXPlusOneView();
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
        String[] folders = {RuleFolderNames.PRETTY_FOLDER};

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
	 * A view that is specific to the cells being displayed for this rule. Tells
	 * the graphics how to display the integer stored by a cell. Uses shades of
	 * the same color (rather than different colors) for each state value.
	 * 
	 * @author David Bahr
	 */
	private class ThreeXPlusOneView extends TriangleHexagonCellStateView
	{
		/**
		 * Create a view for the 3x+1 problem that uses appropriate shading for
		 * colors.
		 */
		public ThreeXPlusOneView()
		{
			super();
		}

		/**
		 * Creates a display color based on the value of the cell. Assumes the
		 * state is an integer between between 0 and numStates. Creates a
		 * fractional shading between the default filled and empty color.
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
			// the number of colors
			int numPossibleColors = 2;

			// this should always work because we are using an integer based
			// rule
			if(numStates != null)
			{
				numPossibleColors = numStates;
			}
			else
			{
				numPossibleColors = CurrentProperties.getInstance().getNumStates();
			}

			// the value of the cell.
			int intValue = ((IntegerCellState) state).getState();

			Color filledColor = ColorScheme.FILLED_COLOR;
			Color emptyColor = ColorScheme.EMPTY_COLOR;

			double redDiff = filledColor.getRed() - emptyColor.getRed();
			double greenDiff = filledColor.getGreen() - emptyColor.getGreen();
			double blueDiff = filledColor.getBlue() - emptyColor.getBlue();

			double redDelta = redDiff / (numPossibleColors - 1);
			double greenDelta = greenDiff / (numPossibleColors - 1);
			double blueDelta = blueDiff / (numPossibleColors - 1);

			int red = (int) Math.floor(emptyColor.getRed()
					+ (intValue * redDelta));
			int green = (int) Math.floor(emptyColor.getGreen()
					+ (intValue * greenDelta));
			int blue = (int) Math.floor(emptyColor.getBlue()
					+ (intValue * blueDelta));
			
			//just to be safe
			if(red < 0)
			{
				red = 0;
			}
			else if(red > 255)
			{
				red = 255;
			}
			
			if(green < 0)
			{
				green = 0;
			}
			else if(green > 255)
			{
				green = 255;
			}
			
			if(blue < 0)
			{
				blue = 0;
			}
			else if(blue > 255)
			{
				blue = 255;
			}

			return new Color(red, green, blue);
		}
	}
}
