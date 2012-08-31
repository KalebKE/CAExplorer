/*
 BlueShadesColorScheme -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2008  David B. Bahr (http://academic.regis.edu/dbahr/)

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

package cellularAutomata.graphics.colors;

import java.awt.Color;

import cellularAutomata.cellState.view.CellStateView;

/**
 * A "gradation of colors" scheme for displaying cells in the CA. Overrides the
 * getColor() methods of the parent class to return gradations between the empty
 * and the filled state.
 * 
 * @author David Bahr
 */
public class KindOfBluesColorScheme extends ColorScheme
{
	/**
	 * The display name of this color scheme.
	 */
	public static final String schemeName = "Kind of Blue";

	// the default empty color for the blue shades color scheme
	private static final Color DEFAULT_EMPTY_SHADES_COLOR = new Color(191, 191,
			255);

	// the default filled color for the blue shades color scheme
	private static final Color DEFAULT_FILLED_SHADES_COLOR = new Color(0, 0,
			102);

	// an array of blue colors
	private Color[] blueColors = null;

	/**
	 * Create a color scheme with a gradation of colors.
	 */
	public KindOfBluesColorScheme()
	{
		super(DEFAULT_EMPTY_SHADES_COLOR, DEFAULT_FILLED_SHADES_COLOR);
	}

	/**
	 * Create a color scheme with a gradation of colors between the specified
	 * empty and filled colors.
	 * 
	 * @param emptyColor
	 *            The color for empty cells (in other words, for integer rules,
	 *            cells with state 0).
	 * @param filledColor
	 *            The color for filled cells (in other words, for integer rules,
	 *            cells with state N-1 when there are N states).
	 */
	public KindOfBluesColorScheme(Color emptyColor, Color filledColor)
	{
		super(emptyColor, filledColor);
	}

	/**
	 * Only valid for cells with integer states, this gets a color corresponding
	 * to the given state.
	 * 
	 * @param state
	 *            The current cell state.
	 * @param numStates
	 *            The number of states used in the simulation.
	 * @param currentView
	 *            The view used by the cell.
	 * @return A color appropriate for the given state.
	 */
	public synchronized Color getColor(int state, int numStates,
			CellStateView currentView)
	{
		// OLD SLOW CODE
		// return ColorTools.getColorFromSingleValue(state, numStates);

		// -----------------------------------------------------------
		// NEW FAST CODE (arrays are faster)

		// NOTE: This method has to be synchronized because the initial state
		// panel's probability choosers might try to access this array at the
		// same time as one of the many different processor threads. The result
		// can be a crash because the thread might try to use more states than
		// exist in the array. (i.e., array index out of bounds). For example,
		// in EZ facade mode, select Crystal Life. Then while it is running,
		// select Creeping Nucleation. Code will crash if not synchronized.

		// create an array of blue colors, but only when necessary.
		if(blueColors == null || blueColors.length != numStates)
		{
			blueColors = new Color[numStates];
			for(int i = 0; i < numStates; i++)
			{
				blueColors[i] = ColorTools
						.getColorFromSingleValue(i, numStates);
			}

			super.setEmptyColor(blueColors[0]);
			super.setFilledColor(blueColors[numStates - 1]);
		}

		// now get the color that will be returned. Note that this replaces the
		// array color with the filled and empty color as appropriate. Why?
		// Because the filled and empty colors can be reset from the menu by the
		// user. But those reset values are not stored in this class' color
		// array.
		Color theColor = null;
		if(state == 0)
		{
			theColor = getEmptyColor();
		}
		else if(state == numStates - 1)
		{
			theColor = getFilledColor();
		}
		else if(blueColors.length == numStates)
		{
			theColor = blueColors[state];
		}
		else
		{
			// this annoying possibility will happen when the initial states
			// panel asks for a color with numStates larger than the current
			// simulation (it uses the numStates on the properties panel).
			theColor = ColorTools.getColorFromSingleValue(state, numStates);
		}

		return theColor;
	}

	/**
	 * An array of colors that will be used to tag the cells. May be null, in
	 * which case the default tagged colors are used. Child classes may override
	 * this method to provide non-default tagging colors. If overridden, at
	 * least 10 colors should be provided to ensure that there are enough. If
	 * all of the tagged colors from this array are in use, then the default
	 * tagged color will be used.
	 * 
	 * @return An array of colors used to tag the cells for extra visibility.
	 */
	protected Color[] getAllTaggingColors()
	{
		Color[] taggingColors = {Color.RED, Color.YELLOW, Colors.DARK_ORANGE,
				Color.GREEN, Color.GRAY, Colors.HOT_PINK, Colors.FOREST_GREEN,
				Color.CYAN, Colors.AQUAMARINE, Colors.CHARTREUSE,
				Colors.LIME_GREEN, Colors.TURQUOISE, Color.MAGENTA};

		return taggingColors;
	}
}
