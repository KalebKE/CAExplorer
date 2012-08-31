/*
 TriLife -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.TriangularIntegerCellStateView;
import cellularAutomata.lattice.TriangularLattice;
import cellularAutomata.lattice.TwelveNeighborTriangularLattice;
import cellularAutomata.rules.util.RuleFolderNames;

/**
 * Same as the class Life, but with triangular graphics. The nicer graphics slow
 * the speed of the simulation. <br>
 * Rules for the game of life. If a cell is occupied and has 0 or 1 occupied
 * neighbors, then the cell dies of loneliness (becomes a 0). If a cell is
 * occupied and has 4 or more occupied neighbors, then the cell dies of
 * overcrowding (becomes a 0). If a cell is occupied and has 2 or 3 occupied
 * neighbors, then the cell survives (stays a 1). If a cell is not occupied and
 * has exactly 3 occupied neighbors, then the cell births (becomes a 1).
 * 
 * @author David Bahr
 */
public class TriLife extends Life
{
	// a display name for this class
	private static final String RULE_NAME = "Triangular Life";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, use a 50% random initial state on a Triangular "
			+ "(12 next-neighbor) lattice. Look for some interesting blinkers and a wide "
			+ "variety of complex structures."
			+ "<p>"
			+ "You might also try a Triangular (3 neighbor) lattice, but don't expect much. "
			+ "There is a wider variety of stable snake-like structures, but on grids with "
			+ "fewer than 8 neighbors, the Game of Life suffers "
			+ "a dramatic loss of complexity. " + leftClickInstructions
			+ rightClickInstructions + "</body></html>";

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>" + RULE_NAME
			+ ".</b> Same as the \"Life\" rule, but uses nicer triangular "
			+ "graphics at the cost of speed.</body></html>";

	/**
	 * Create an instance of the Life rule.
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
	public TriLife(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);
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
		return new TriangularIntegerCellStateView();
	}

	/**
	 * A list of lattices with which this Rule will work, which in this case, is
	 * only the triangular lattice. <br>
	 * Well-designed Rules should work with any lattice, but some may require
	 * particular topological or geometrical information (like the latttice
	 * gas). Appropriate strings to return in the array include
	 * SquareLattice.DISPLAY_NAME, HexagonalLattice.DISPLAY_NAME,
	 * StandardOneDimensionalLattice.DISPLAY_NAME, etc.
	 * 
	 * @return A list of lattices compatible with this Rule.
	 */
	public String[] getCompatibleLattices()
	{
		String[] lattices = {TriangularLattice.DISPLAY_NAME,
				TwelveNeighborTriangularLattice.DISPLAY_NAME};

		return lattices;
	}

	/**
	 * NOTE: I had to override this because it returns inappropriate folders in
	 * the parent Life class.
	 * <p>
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
		String[] folders = {RuleFolderNames.OUTER_TOTALISTIC_FOLDER,
				RuleFolderNames.LIFE_LIKE_FOLDER};

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
