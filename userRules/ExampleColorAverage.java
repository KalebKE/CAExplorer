/*
 ExampleColorAverage -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.ColorVectorView;
import cellularAutomata.lattice.SquareLattice;
import cellularAutomata.lattice.StandardOneDimensionalLattice;
import cellularAutomata.rules.templates.IntegerVectorRuleTemplate;

/**
 * For each of the three color components (red, green, and blue), this rule
 * finds the average red, the average blue, and the average green of a cell and
 * its neighbors. This rather boring class was primarily written as an example
 * of extending the IntegerVectorRuleTemplate class. Note this class needed each
 * cell to store a triplet of integer values. In other words, this class needed
 * each cell to store an array of length 3. So the implementation of the
 * getVectorLength() method returns 3. We wanted the graphics to treat the
 * triplet of integers as the red, green, and blue component of a Color, so we
 * used the setView() method from the parent class to set the "view". See the
 * constructor below. The ColorView class was used to render the graphics.
 * (Incidentally, this "view" is part of the model-view-controller design
 * pattern used for the cell states). <br>
 * We also restricted the lattices for which this rule works by overriding the
 * parent's getCompatibleLattices() method. This rule now only works for
 * one-dimensional and square lattices. There's no reason for this (it would
 * work fine on other lattices), but this illustrates how to restrict rules to
 * certain lattices. Most good rules will not be lattice specific, but a few
 * like the "lattice gas" require specific geometries. <br>
 * This same class could have been written by extending the Rule class, but that
 * would have required additional work. The IntegerVectorRuleTemplate and other
 * "RuleTemplate" classes take care of many details (at the loss of some
 * flexibility).
 * 
 * @author David Bahr
 */
public class ExampleColorAverage extends IntegerVectorRuleTemplate
{
	// a display name for this class
	private static final String RULE_NAME = "Color Average";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, try a random initial population on a 100 by 100 or larger "
			+ "one-dimensional (2 neighbor) lattice. "
			+ "<p><b>Left-click</b> the grid to draw empty cells."
			+ "<p><b>Right-click</b> the grid to draw new cells with random values."
			+ "</body></html>";

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b> Stores colors at each cell and averages them together.</body></html>";

	/**
	 * Create a rule that averages together each color component stored by the
	 * cells.
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
	public ExampleColorAverage(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);
	}

	/**
	 * Calculates the average of the array at each index. Assumes the array
	 * length is three.
	 * 
	 * @see cellularAutomata.rules.templates.IntegerVectorRuleTemplate#integerVectorRule(int[],
	 *      int[][], int)
	 */
	public int[] integerVectorRule(int[] cellArray, int[][] neighborArrays,
			int generation)
	{
		int red = cellArray[0];
		int green = cellArray[1];
		int blue = cellArray[2];

		for(int i = 0; i < neighborArrays.length; i++)
		{
			red += neighborArrays[i][0];
			green += neighborArrays[i][1];
			blue += neighborArrays[i][2];
		}

		// +1 because have the neighbors + 1 central cell
		red = (int) Math.floor(red / (neighborArrays.length + 1));
		green = (int) Math.floor(green / (neighborArrays.length + 1));
		blue = (int) Math.floor(blue / (neighborArrays.length + 1));

		int[] answer = {red, green, blue};

		return answer;
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
		return new MyColorVectorView();
	}
	
	/**
	 * A list of lattices with which this Rule will work (in this case is the
	 * one-dimensional and square lattices). <br>
	 * Well-designed Rules should work with any lattice, but some may require
	 * particular topological or geometrical information (like the latttice
	 * gas). Appropriate strings to return in the array include
	 * SquareLattice.DISPLAY_NAME, HexagonalLattice.DISPLAY_NAME,
	 * StandardOneDimensionalLattice.DISPLAY_NAME, etc. Overrides the method in
	 * the super class.
	 * 
	 * @return A list of compatible lattices.
	 * @see cellularAutomata.rules.templates.IntegerVectorRuleTemplate#getCompatibleLattices()
	 */
	public String[] getCompatibleLattices()
	{
		String[] lattices = {SquareLattice.DISPLAY_NAME,
				StandardOneDimensionalLattice.DISPLAY_NAME};

		return lattices;
	}

	/**
	 * A brief one or two-word string describing the rule, appropriate for
	 * display in a drop-down list.
	 * 
	 * @return A string no longer than 15 characters.
	 * @see cellularAutomata.rules.Rule#getDisplayName()
	 */
	public String getDisplayName()
	{
		return RULE_NAME;
	}

	/**
	 * Gets the maximum permissible value for each element of the vector. Do not
	 * recommend using Integer.MAX_VALUE unless your code very carefully checks
	 * for instances of Infinity. <br>
	 * This method is used by the IntegerVectorRuleTemplate class to properly
	 * construct a IntegerVectorCellState.
	 * 
	 * @return The maximum permissible value.
	 * @see cellularAutomata.rules.templates.IntegerVectorRuleTemplate#getMaximumPermissibleValue()
	 */
	public int getMaximumPermissibleValue()
	{
		return 255;
	}

	/**
	 * Gets the minimum permissible value for each element of the vector. Do not
	 * recommend using Integer.MIN_VALUE unless your code very carefully checks
	 * for instances of Infinity. <br>
	 * This method is used by the IntegerVectorRuleTemplate class to properly
	 * construct an IntegerVectorState.
	 * 
	 * @return The minimum permissible value.
	 * @see cellularAutomata.rules.templates.IntegerVectorRuleTemplate#getMinimumPermissibleValue()
	 */
	public int getMinimumPermissibleValue()
	{

		return 0;
	}

	/**
	 * A brief description (written in HTML) that describes this rule. The
	 * description will be displayed as a tooltip. Using html permits line
	 * breaks, font colors, etcetera, as described in HTML resources. Regular
	 * line breaks will not work.
	 * 
	 * @return An HTML string describing this rule.
	 * @see cellularAutomata.rules.Rule#getToolTipDescription()
	 */
	public String getToolTipDescription()
	{
		return TOOLTIP;
	}

	/**
	 * Gets the length of the vectors (arrays) that will be used by the Rule.
	 * The length must be the same for all cells.
	 * 
	 * @return The length of the vector stored by each cell.
	 * @see cellularAutomata.rules.templates.IntegerVectorRuleTemplate#getVectorLength()
	 */
	public int getVectorLength()
	{
		return 3;
	}

	/**
	 * This class creates an alternate view for displaying the cells. It uses
	 * the existing ColorVectorView, but I override the enableColorSchemes()
	 * method so that the menu won't let you choose any other color schemes
	 * except this one.
	 * 
	 * @author David Bahr
	 */
	public class MyColorVectorView extends ColorVectorView
	{
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
		 * won't change with the scheme).
		 * 
		 * @return true if all color schemes are allowed and will be enabled in
		 *         the menu, and false if the color schemes will be disabled in
		 *         the menu.
		 */
		public boolean enableColorSchemes()
		{
			return false;
		}
	}
}
