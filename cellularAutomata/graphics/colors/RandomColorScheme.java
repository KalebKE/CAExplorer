/*
 RandomColorScheme -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2006  David B. Bahr (http://academic.regis.edu/dbahr/)

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
import java.util.Random;

import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.util.math.RandomSingleton;

/**
 * A random color scheme for displaying cells in the CA. Every time it is
 * called, the colors will be different. Overrides the getColor() methods of the
 * parent class.
 * 
 * @author David Bahr
 */
public class RandomColorScheme extends ColorScheme
{
	/**
	 * The display name of this color scheme.
	 */
	public static final String schemeName = "Random";

	// a random number generator
	private static Random random = RandomSingleton.getInstance();

	// The view when the getColor() method was first created by the cell. This
	// is used to check when this view is being called by something else. Used
	// to prevent the colors from being changed randomly.
	private CellStateView currentView = null;

	// the number of times the getColor method has been called. Used to make
	// sure that the currentView is only set the first time the getColor()
	// method is called.
	private int numCalls = 0;

	// the number of states used by the simulation that first called the
	// getColor() method. Used by the setFilledColor() method.
	private int numStates = 2;

	// an array of random Colors
	private Color[] randomColors = null;

	/**
	 * Create a random color scheme.
	 */
	public RandomColorScheme()
	{
		// the random colors
		super(new Color(random.nextInt(256), random.nextInt(256), random
				.nextInt(256)), new Color(random.nextInt(256), random
				.nextInt(256), random.nextInt(256)));
	}

	/**
	 * Overrides the parent class to return random colors. Only valid for cells
	 * with integer states, this gets a color corresponding to the given state.
	 * 
	 * @param state
	 *            The current cell state.
	 * @param numStates
	 *            The number of states used in the simulation.
	 * @param currentView
	 *            The view used by the cell.
	 * @return A color appropriate for the given state.
	 */
	public Color getColor(int state, int numStates, CellStateView currentView)
	{
		// the number of times this method has been called
		numCalls++;

		// create an array of random colors, but only when necessary. Why this
		// complicated scheme? Because the properties may change the number of
		// states. And if the number of states changes, the initial states panel
		// will draw random colors for that many states. But that may be more
		// than the number of states being used by the current simulation. So
		// the initial states panel would inadvertently change the colors of the
		// current simulation. Very annoying. The "else-if" code prevents that.
		if(randomColors == null)
		{
			randomColors = new Color[numStates];
			for(int i = 0; i < numStates; i++)
			{
				randomColors[i] = new Color(random.nextInt(256), random
						.nextInt(256), random.nextInt(256));
			}

			super.setEmptyColor(randomColors[0]);
			super.setFilledColor(randomColors[numStates - 1]);
		}
		else if(randomColors.length < numStates)
		{
			// copy the original colors (so they are not lost)
			Color[] newRandomColors = new Color[numStates];
			for(int i = 0; i < randomColors.length; i++)
			{
				newRandomColors[i] = randomColors[i];
			}

			// now create new colors (tack them onto the end of the original
			// colors)
			for(int i = randomColors.length; i < numStates; i++)
			{
				newRandomColors[i] = new Color(random.nextInt(256), random
						.nextInt(256), random.nextInt(256));
			}

			randomColors = newRandomColors;

			// and reset the empty and filled colors, but only if this is the
			// same view (otherwise this is being called by the initial state
			// tab, and we don't want to change the simulation; we only want
			// to change the initial state which is trying to set up for a
			// different number of cell states.)
			if(this.currentView == currentView)
			{
				super.setEmptyColor(randomColors[0]);
				super.setFilledColor(randomColors[numStates - 1]);
			}
		}

		// only set the current view when first called
		if(numCalls == 1)
		{
			this.numStates = numStates;
			this.currentView = currentView;
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
		else
		{
			theColor = randomColors[state];
		}

		return theColor;
	}

	/**
	 * Overrides the parent so that the random array is also given a new empty
	 * color.
	 */
	public void setEmptyColor(Color color)
	{
		super.setEmptyColor(color);
		randomColors[0] = color;
	}

	/**
	 * Overrides the parent so that the random array is also given a new filled
	 * color.
	 */
	public void setFilledColor(Color color)
	{
		super.setFilledColor(color);
		randomColors[numStates - 1] = color;
	}
}
