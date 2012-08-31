/*
 ColorTools -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.graphics.colors;

import java.awt.Color;
import java.util.Hashtable;

import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.graphics.colors.colorChooser.IntegerStateColorChooserPanel;
import cellularAutomata.util.Coordinate;

/**
 * A collection of tools for handling color.
 * 
 * @author David Bahr
 */
public class ColorTools
{
	// the current view being used to display the cells.
	private static CellStateView view = null;

	// The maximum value for the brightness (which is between 0 and 1 in
	// Java.awt.Color)
	private static float MAX_BRIGHTNESS = 1.0f;

	// The maximum value for the hue (which is between 0 and 1 in
	// Java.awt.Color)
	private static float MAX_HUE = 1.0f;

	// The maximum value for the saturation (which is between 0 and 1 in
	// Java.awt.Color)
	private static float MAX_SATURATION = 1.0f;

	// A hash table that hashes colors to their corresponding states.
	private static Hashtable<Color, Integer> colorHash = null;

	// A hash table that hashes states to their corresponding colors.
	private static Hashtable<Integer, Color> stateHash = null;

	// The number of states after which the hue is adjusted so that colors start
	// from the empty state.
	private static int NUMSTATES_TO_ADJUST_HUE = 24;

	/**
	 * Creates a hash table of colors that correspond to the states. The colors
	 * are requested from the view using view.getDisplayColor().
	 * 
	 * @param numStates
	 *            The total number of states.
	 */
	private static void createColorAndStateHashTablesFromTheView(int numStates)
	{
		// instantiate the hash
		colorHash = new Hashtable<Color, Integer>(numStates);
		stateHash = new Hashtable<Integer, Color>(numStates);

		for(int i = 0; i < numStates; i++)
		{
			// get the color
			Color color = view.getDisplayColor(new IntegerCellState(i), null,
					new Coordinate(0, 0));

			// make sure we haven't chosen it before
			int attempts = 0;
			while(colorHash.containsKey(color) && attempts < 256)
			{
				// create a new but nearby color
				color = new Color((color.getRed() + 1) % 256, color.getGreen(),
						color.getBlue());

				attempts++;
			}

			// try again, and make sure we haven't chosen it before
			attempts = 0;
			while(colorHash.containsKey(color) && attempts < 256)
			{
				// create a new but nearby color
				color = new Color(color.getRed(), (color.getGreen() + 1) % 256,
						color.getBlue());

				attempts++;
			}

			// try yet again, and make sure we haven't chosen it before
			attempts = 0;
			while(colorHash.containsKey(color) && attempts < 256)
			{
				// create a new but nearby color
				color = new Color(color.getRed(), color.getGreen(), (color
						.getBlue() + 1) % 256);

				attempts++;
			}

			colorHash.put(color, new Integer(i));
			stateHash.put(new Integer(i), color);
		}
	}

	/**
	 * Creates a hash table of colors that correspond to the states. The view is
	 * not used to construct the colors for the hashtable. Instead the colors
	 * are selected from the getColorFromState method.
	 * 
	 * @param numStates
	 *            The total number of states.
	 */
	private static void createColorHashtableWithoutTheView(int numStates)
	{
		// instantiate the hash
		colorHash = new Hashtable<Color, Integer>(numStates);
		stateHash = new Hashtable<Integer, Color>(numStates);

		// fill with the empty and filled colors
		colorHash.put(ColorScheme.EMPTY_COLOR, new Integer(0));
		colorHash.put(ColorScheme.FILLED_COLOR, new Integer(numStates - 1));
		stateHash.put(new Integer(0), ColorScheme.EMPTY_COLOR);
		stateHash.put(new Integer(numStates - 1), ColorScheme.FILLED_COLOR);

		for(int i = 1; i < numStates - 1; i++)
		{
			// get the color
			Color color = getColorFromState(i, numStates);

			// make sure we haven't chosen it before
			int attempts = 0;
			while(colorHash.containsKey(color) && attempts < 256)
			{
				// create a new but nearby color
				color = new Color((color.getRed() + 1) % 256, color.getGreen(),
						color.getBlue());

				attempts++;
			}

			// try again, and make sure we haven't chosen it before
			attempts = 0;
			while(colorHash.containsKey(color) && attempts < 256)
			{
				// create a new but nearby color
				color = new Color(color.getRed(), (color.getGreen() + 1) % 256,
						color.getBlue());

				attempts++;
			}

			// try yet again, and make sure we haven't chosen it before
			attempts = 0;
			while(colorHash.containsKey(color) && attempts < 256)
			{
				// create a new but nearby color
				color = new Color(color.getRed(), color.getGreen(), (color
						.getBlue() + 1) % 256);

				attempts++;
			}

			colorHash.put(color, new Integer(i));
			stateHash.put(new Integer(i), color);
		}
	}

	/**
	 * Creates a different color for each possible state, 0 to numStates-1.
	 * 
	 * @param state
	 *            The state being mapped to a color.
	 * @param numStates
	 *            The number of states possible for the rule.
	 * @return A color.
	 */
	private static Color getColorFromState(int state, int numStates)
	{
		// the color that will be returned
		Color color = null;

		if(state == 0)
		{
			color = ColorScheme.EMPTY_COLOR;
		}
		else if(state == numStates - 1)
		{
			color = ColorScheme.FILLED_COLOR;
		}
		else
		{

			// number of rows that will appear on the color chooser.
			float numberOfColorPatchRows = IntegerStateColorChooserPanel
					.calculateNumberOfRows(numStates);

			// number of columns that will appear on the color chooser.
			float numberOfColorPatchColumns = numStates
					/ numberOfColorPatchRows;

			// get the change in hue saturation and delta with each incremental
			// increase in the state.
			float[] hsbDelta = getHSBDelta(numStates);
			float hueDelta = hsbDelta[0];
			float saturationDelta = hsbDelta[1];
			float brightnessDelta = hsbDelta[2];

			// the new hue
			float hue = state * hueDelta;
			if(numStates > NUMSTATES_TO_ADJUST_HUE)
			{
				// adjust to center near the empty state
				hue = ColorTools.getHue(ColorScheme.EMPTY_GRADIENT_COLOR)
						+ (state * hueDelta);
			}

			// translate/rescale as necessary
			while(hue > MAX_HUE)
			{
				hue -= MAX_HUE;
			}

			// the new saturation and brightness
			float saturation = MAX_SATURATION;
			float brightness = MAX_BRIGHTNESS;
			saturation -= (state % (int) (numberOfColorPatchColumns + 1))
					* saturationDelta;
			brightness -= Math.round((double) state
					/ (double) (numberOfColorPatchColumns + 1.0))
					* (double) brightnessDelta;

			color = Color.getHSBColor(hue, saturation, brightness);
		}

		return color;
	}

	/**
	 * Calculates the change in hue, saturation, and brightness for each
	 * incremental change in state.
	 * 
	 * @param numStates
	 *            The number of cell states.
	 * @return An array containing the hue, saturation, and brightness in that
	 *         order.
	 */
	private static float[] getHSBDelta(int numStates)
	{
		// number of rows that will appear on the color chooser.
		float numberOfColorPatchRows = IntegerStateColorChooserPanel
				.calculateNumberOfRows(numStates);

		// number of columns that will appear on the color chooser.
		float numberOfColorPatchColumns = numStates / numberOfColorPatchRows;

		// the change in hue for each unit increase in state
		float hueDelta = MAX_HUE / ((float) numStates - 1.0f);
		float saturationDelta = MAX_SATURATION
				/ (numberOfColorPatchColumns * 1.5f); // 1.2f
		float brightnessDelta = MAX_BRIGHTNESS
				/ (numberOfColorPatchRows * 3.0f); // 3.0f, 100000.0f, 1.7f

		float[] hsbDelta = new float[] {hueDelta, saturationDelta,
				brightnessDelta};

		return hsbDelta;
	}

	/**
	 * Gets the brightness of the specified color.
	 * 
	 * @param color
	 *            The specified color.
	 * @return The brightness of the color.
	 */
	public static float getBrightness(Color color)
	{
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color
				.getBlue(), null);
		return hsb[2];
	}

	/**
	 * Creates a different color for each possible state, 0 to numStates-1, and
	 * stores the value in a hashtable. The table is returned. Use this method
	 * when you don't want to use the view to request colors (for example, it
	 * might be the view itself that is requesting the colors --
	 * ColorNCellStateView does this). Note that if a view uses this to
	 * construct its colors, then the view may have to call
	 * isNewHashTableNecessary to make sure it doesn't need to change.
	 * 
	 * @param numStates
	 *            The number of states.
	 * @param currentView
	 *            The view currently used to display the cells in the
	 *            simulation.
	 * @return A hash table of colors.
	 */
	public static Hashtable getColorHashtable(int numStates,
			CellStateView currentView)
	{
		// recreate the hash table if necessary
		if(isNewHashTableNecessary(numStates, currentView))
		{
			createColorHashtableWithoutTheView(numStates);
		}

		// the color that will be returned
		return stateHash;
	}

	/**
	 * Creates a color from a single double value. The color is a "greyscale"
	 * tone between the default filled and the default empty color.
	 * 
	 * @param value
	 *            The value being mapped to a color.
	 * @return A color.
	 */
	public static Color getColorFromSingleValue(double value)
	{
		return getColorFromSingleValue(value, 2);
	}

	/**
	 * Creates a color from a single double value. The color is a "greyscale"
	 * tone between the default filled and the default empty color.
	 * 
	 * @param value
	 *            The value being mapped to a color.
	 * @param numStates
	 *            The number of states permitted by the current cell state.
	 * @return A color.
	 */
	public static Color getColorFromSingleValue(double value, int numStates)
	{
		Color filledColor = ColorScheme.FILLED_GRADIENT_COLOR;
		Color emptyColor = ColorScheme.EMPTY_GRADIENT_COLOR;

		double redDiff = filledColor.getRed() - emptyColor.getRed();
		double greenDiff = filledColor.getGreen() - emptyColor.getGreen();
		double blueDiff = filledColor.getBlue() - emptyColor.getBlue();

		double redDelta = redDiff / (numStates - 1);
		double greenDelta = greenDiff / (numStates - 1);
		double blueDelta = blueDiff / (numStates - 1);

		int red = (int) Math.floor(emptyColor.getRed() + (value * redDelta));
		int green = (int) Math.floor(emptyColor.getGreen()
				+ (value * greenDelta));
		int blue = (int) Math.floor(emptyColor.getBlue() + (value * blueDelta));

		// to be safe
		if(red > 255)
		{
			red = 255;
		}
		else if(red < 0)
		{
			red = 0;
		}
		if(green > 255)
		{
			green = 255;
		}
		else if(green < 0)
		{
			green = 0;
		}
		if(blue > 255)
		{
			blue = 255;
		}
		else if(blue < 0)
		{
			blue = 0;
		}

		return new Color(red, green, blue);
	}

	/**
	 * Use this method to create a different color for each possible state, 0 to
	 * numStates-1, with the colors requested from the current view.
	 * 
	 * @param state
	 *            The state being mapped to a color.
	 * @param currentView
	 *            The view currently used to display the cells in the
	 *            simulation.
	 * @return A color.
	 */
	public static Color getColorFromStateValue(int state, int numStates,
			CellStateView currentView)
	{
		// recreate the hash table if necessary
		if(isNewHashTableNecessary(numStates, currentView))
		{
			createColorAndStateHashTablesFromTheView(numStates);
		}

		// the color that will be returned
		return (Color) stateHash.get(new Integer(state));
	}

	/**
	 * Gets the hue of the specified color.
	 * 
	 * @param color
	 *            The specified color.
	 * @return The hue of the color.
	 */
	public static float getHue(Color color)
	{
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color
				.getBlue(), null);
		return hsb[0];
	}

	/**
	 * Gets the saturation of the specified color.
	 * 
	 * @param color
	 *            The specified color.
	 * @return The saturation of the color.
	 */
	public static float getSaturation(Color color)
	{
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color
				.getBlue(), null);
		return hsb[1];
	}

	/**
	 * Gets the state (0 to numStates-1) associated with a particular color.
	 * 
	 * @param color
	 *            The color being mapped to a state.
	 * @param numStates
	 *            The maximum number of states possible for this simulation.
	 * @param currentView
	 *            The view currently used to display the cells in the
	 *            simulation.
	 * @return A state value (0 to numStates-1).
	 */
	public static int getStateValueFromColor(Color color, int numStates,
			CellStateView currentView)
	{
		// the state we will return
		int state = numStates - 1;

		// recreate the hash table if necessary
		if(isNewHashTableNecessary(numStates, currentView))
		{
			createColorAndStateHashTablesFromTheView(numStates);
		}

		Object objectState = colorHash.get(color);

		// might be null if there is more than one color with the same value. In
		// that case stick with the default value "numStates - 1" specified
		// above.
		if(objectState != null)
		{
			state = colorHash.get(color).intValue();
		}
		else if(color.equals(ColorScheme.EMPTY_COLOR))
		{
			// except in this case return the "empty state"
			state = 0;
		}

		// return the state value
		return state;
	}

	/**
	 * Checks to see if the hash tables exist or if they are out of date.
	 * 
	 * @param numStates
	 *            The maximum number of states possible for this simulation.
	 * @param currentView
	 *            The view currently used to display the cells in the
	 *            simulation.
	 * @return true if new color (and state) hash tables need to be created.
	 */
	public static boolean isNewHashTableNecessary(int numStates,
			CellStateView currentView)
	{
		// true if we need new hash tables for the color and state
		boolean needNewHash = false;

		// if the view has changed, then recreate the color hash
		if(view == null || view != currentView)
		{
			view = currentView;
			needNewHash = true;
		}
		else if(colorHash == null || colorHash.size() != numStates)
		{
			// create the colorHash if it is non-existent or the wrong size
			needNewHash = true;
		}
		else if(colorHash.get(ColorScheme.EMPTY_COLOR) == null
				|| colorHash.get(ColorScheme.FILLED_COLOR) == null
				|| !((Integer) colorHash.get(ColorScheme.EMPTY_COLOR))
						.equals(new Integer(0))
				|| !((Integer) colorHash.get(ColorScheme.FILLED_COLOR))
						.equals(new Integer(numStates - 1)))
		{
			// redo the hash if the EMPTY or FILLED colors
			// have changed
			needNewHash = true;
		}

		return needNewHash;
	}
}
