/*
 ObjectExample -- a class within the Cellular Automaton Explorer. 
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
import java.awt.Shape;

import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.rules.templates.ObjectRuleTemplate;
import cellularAutomata.util.Coordinate;

/**
 * Finds the average of a cell and all of its neighbors. Each cell has an Object
 * assigned as its state. In this case the object is a Double.<br>
 * This rather boring class was primarily written as an example of extending the
 * ObjectRuleTemplate class. Note this class has each cell store a single Double
 * object value.
 * <p>
 * We desired a different graphical display, so the setView() method from the
 * parent is called (in the constructor). We could use an existing view, but in
 * this case we created one appropriate to the objects being used by the this
 * rule. The new view is an inner class at the bottom of this code.
 * <p>
 * We could have also restricted the lattices for which this rule works by
 * overriding the getCompatibleLattices (see the ColorAverage class). <br>
 * This same class could have been written by extending the Rule class, but that
 * would have required additional work. The ObjectRuleTemplate and other
 * "RuleTemplate" classes take care of many details (at the loss of some
 * flexibility).
 * 
 * @author David Bahr
 */
public class ObjectExample extends ObjectRuleTemplate
{
	// a display name for this class
	private static final String RULE_NAME = "Object Average";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, try a random initial population on a 100 by 100 or larger "
			+ "one-dimensional (2 neighbor) lattice. " + leftClickInstructions
			+ rightClickInstructions + "</body></html>";

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b> Finds the average of a cell and all of its neighbors.</body></html>";

	/**
	 * Create a rule that averages the values of neighboring cells.
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
	public ObjectExample(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);
	}

	/**
	 * Given a string, this method should construct an object of the same type
	 * as returned by getFullState(), getEmptyState(), and getAlternateState().
	 * <p>
	 * This method is necessary to properly reload data when a simulation is
	 * saved as a file.
	 * <p>
	 * If you are using this ObjectRuleTemplate class, then the cell's state is
	 * an Object of some kind. When reading and writing data from a file, the
	 * Object is stored as a string (it is stored using the toString() method).
	 * This method is called when reading the data back from the file.
	 * Therefore, this method should be able to reconstruct the Object from that
	 * string.
	 * <p>
	 * This method may return null. In that case, the cell's state can be saved
	 * in a file but it cannot be reread from that file. (If null, attempting to
	 * reload a simulation will warn the user that this is not possible.) Some
	 * arbitrary objects cannot be reconstructed from a string, so returning
	 * null may be necessary.
	 * 
	 * @param state
	 *            The Object's state represented as a string.
	 * @return An Object that will be used as a cell state. May be null.
	 */
	public Object createStateFromString(String state)
	{
		return new Double(state);
	}

	/**
	 * Gets an Object that represents the alternate state. (In this case it is
	 * just the empty State.)
	 * 
	 * @return The alternate state.
	 */
	public Object getAlternateState()
	{
		return getEmptyState();
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
	public CellStateView getCompatibleCellStateView()
	{
		return new ObjectView();
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
	 * Gets an Object that represents the empty state, in this case a Double
	 * with value 0.0.
	 * 
	 * @return The empty state.
	 */
	public Object getEmptyState()
	{
		return new Double(0.0);
	}

	/**
	 * Gets an Object that represents the full or filled state, in this case a
	 * Double with value 255.0.
	 * 
	 * @return The full state.
	 */
	public Object getFullState()
	{
		return new Double(255.0);
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
	 * Finds the average value of a cell and all of its neighbors.
	 * 
	 * @see cellularAutomata.rules.templates.ObjectRuleTemplate#objectRule(
	 *      Object, Object[], int)
	 */
	public Object objectRule(Object cellObject, Object[] neighborObjects,
			int generation)
	{
		// the cells store an object of type Double.
		double average = ((Double) cellObject).doubleValue();

		// now add in the values of each neighbor
		for(int i = 0; i < neighborObjects.length; i++)
		{
			average += ((Double) neighborObjects[i]).doubleValue();
		}

		// now get the average
		average /= (neighborObjects.length + 1);

		// write it as an object
		Double state = new Double(average);

		// return the Object
		return state;
	}

	/**
	 * A view that is specific to the Objects being displayed for this rule.
	 * Tells the graphics how to display the Object stored by a cell.
	 * 
	 * @author David Bahr
	 */
	private class ObjectView extends CellStateView
	{
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
		 * Creates a display color based on the value of the cell. Assumes the
		 * state is a Double between 0 and 255. Creates a fractional color
		 * between the default filled and empty color.
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
			int numPossibleColors = 256;

			// the value of the cell.
			double doubleValue = ((Double) state.getValue()).doubleValue();

			Color filledColor = ColorScheme.FILLED_COLOR;
			Color emptyColor = ColorScheme.EMPTY_COLOR;

			double redDiff = filledColor.getRed() - emptyColor.getRed();
			double greenDiff = filledColor.getGreen() - emptyColor.getGreen();
			double blueDiff = filledColor.getBlue() - emptyColor.getBlue();

			double redDelta = redDiff / (numPossibleColors - 1);
			double greenDelta = greenDiff / (numPossibleColors - 1);
			double blueDelta = blueDiff / (numPossibleColors - 1);

			int red = (int) Math.floor(emptyColor.getRed()
					+ (doubleValue * redDelta));
			int green = (int) Math.floor(emptyColor.getGreen()
					+ (doubleValue * greenDelta));
			int blue = (int) Math.floor(emptyColor.getBlue()
					+ (doubleValue * blueDelta));

			// just to be safe
			if(red > 255)
			{
				red = 255;
			}
			if(green > 255)
			{
				green = 255;
			}
			if(blue > 255)
			{
				blue = 255;
			}
			if(red < 0)
			{
				red = 0;
			}
			if(green < 0)
			{
				green = 0;
			}
			if(blue < 0)
			{
				blue = 0;
			}

			return new Color(red, green, blue);
		}

		/**
		 * Returns null so that the default shape is used (a square).
		 * 
		 * @see cellularAutomata.cellState.view.CellState#getDisplayShapeView(CellState,
		 *      int, int, Coordinate)
		 */
		public Shape getDisplayShape(CellState state, int width, int height,
				Coordinate rowAndCol)
		{
			return null;
		}
	}
}
