/*
 RainbowColorScheme -- a class within the Cellular Automaton Explorer. 
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
import java.util.Hashtable;

import cellularAutomata.cellState.view.CellStateView;

/**
 * A "rainbow-like" color scheme for displaying cells in the CA. Uses all of the
 * defaults in the ColorScheme class. This class does not change the behavior of
 * ColorScheme and is simply a concrete implementation.
 * 
 * @author David Bahr
 */
public class RainbowColorScheme extends ColorScheme
{
	/**
	 * The display name of this color scheme.
	 */
	public static final String schemeName = "Default Rainbow";

	// an array of rainbow colors
	private Color[] rainbowColors = null;

	/**
	 * Create a color scheme with a rainbow of colors.
	 */
	public RainbowColorScheme()
	{
		super(DEFAULT_EMPTY_COLOR, DEFAULT_FILLED_COLOR);
	}

	/**
	 * Create a color scheme with a rainbow of colors.
	 * <p>
	 * The colors for the scheme are based upon the supplied empty and filled
	 * colors.
	 * 
	 * @param emptyColor
	 *            The color for empty cells (in other words, for integer rules,
	 *            cells with state 0).
	 * @param filledColor
	 *            The color for filled cells (in other words, for integer rules,
	 *            cells with state N-1 when there are N states).
	 */
	public RainbowColorScheme(Color emptyColor, Color filledColor)
	{
		super(emptyColor, filledColor);
	}

	/**
	 * Uses a rainbow array of colors.
	 * 
	 * @param state
	 *            The current cell state.
	 * @param numStates
	 *            The number of states used in the simulation.
	 * @param currentView
	 *            The view used by the cell.
	 * @return A color appropriate for the given state.
	 */
	private Color getRainbowColors(int state, int numStates,
			CellStateView currentView)
	{
		// the ColorTools class keeps a hash table of colors for us
		Hashtable colorHash = ColorTools.getColorHashtable(numStates,
				currentView);

		return (Color) colorHash.get(new Integer(state));
	}

	/**
	 * Overrides the parent method to return a rainbow of colors. Only valid for
	 * cells with integer states, this gets a color corresponding to the given
	 * state.
	 * 
	 * @param state
	 *            The current cell state.
	 * @param numStates
	 *            The number of states used in the simulation.
	 * @param currentView
	 *            The view used by the cell.
	 * @return A color appropriate for the given state.
	 */
	public synchronized Color getColor(int state, int numStates, CellStateView currentView)
	{
		// OLD SLOW CODE
		// use the default rainbow
		// return getRainbowColors(state, numStates, currentView);

		// -----------------------------------------------------------
		// NEW FAST CODE (arrays are faster)
		
		// NOTE: This method has to be synchronized because the initial state
		// panel's probability choosers might try to access this array at the
		// same time as one of the many different processor threads. The result
		// can be a crash because the thread might try to use more states than
		// exist in the array. (i.e., array index out of bounds).

		// create an array of rainbow colors, but only when necessary.
		if(rainbowColors == null)
		{
			rainbowColors = new Color[numStates];
			for(int i = 0; i < numStates; i++)
			{
				rainbowColors[i] = getRainbowColors(i, numStates, currentView);
			}

			super.setEmptyColor(rainbowColors[0]);
			super.setFilledColor(rainbowColors[numStates - 1]);
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
		else if(rainbowColors.length == numStates)
		{
			theColor = rainbowColors[state];
		}
		else
		{
			// this annoying possibility will happen when the initial states
			// panel asks for a color with numStates larger than the current
			// simulation (it uses the numStates on the properties panel).
			theColor = getRainbowColors(state, numStates, currentView);
		}

		return theColor;
	}
}