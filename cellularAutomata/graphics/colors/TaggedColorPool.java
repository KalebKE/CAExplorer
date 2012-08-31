/*
 TaggedColorPool -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.graphics.colors;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;

import cellularAutomata.graphics.colors.Colors;

/**
 * Keeps track of colors used to tag cells for extra visibility. Each color is
 * assigned to particular objects that can reserve (and release) those colors.
 * For example, an analysis might wish to reserve a color. When the analysis
 * closes, it can release that color.
 * 
 * @author David Bahr
 */
public abstract class TaggedColorPool
{
	/**
	 * The default color used for tagging a cell for visibility.
	 */
	private final static Color DEFAULT_TAGGED_COLOR = Color.RED;

	// the default pool of tagged colors (using the linked list as a stack
	// because the Java Stack has been deprecated)
	private final static LinkedList<Color> DEFAULT_TAGGED_COLOR_POOL = new LinkedList<Color>();

	/**
	 * If true, tagged cells should be displayed as translucent.
	 */
	public static boolean taggedCellsTranslucent = true;

	/**
	 * If true, tagged cells should be displayed with no extra color
	 * (invisible).
	 */
	public static boolean taggedCellsNoExtraColor = false;

	// a hash table that keeps track of which tagged colors have been claimed by
	// particular tagging objects (usually analyses)
	private static HashMap<Object, Color> taggedHashTable = new HashMap<Object, Color>();

	// the pool of tagged colors (using the linked list as a stack because the
	// Java Stack has been deprecated)
	private static LinkedList<Color> taggedColorPool = new LinkedList<Color>();

	// create default tagged colors
	static
	{
		// the default colors
		DEFAULT_TAGGED_COLOR_POOL.push(Color.RED);
		DEFAULT_TAGGED_COLOR_POOL.push(Color.YELLOW);
		DEFAULT_TAGGED_COLOR_POOL.push(Colors.DARK_VIOLET);
		DEFAULT_TAGGED_COLOR_POOL.push(Colors.DARK_ORANGE);
		DEFAULT_TAGGED_COLOR_POOL.push(Color.GRAY);
		DEFAULT_TAGGED_COLOR_POOL.push(Colors.HOT_PINK);
		DEFAULT_TAGGED_COLOR_POOL.push(Color.CYAN);
		DEFAULT_TAGGED_COLOR_POOL.push(Colors.PURPLE);
		DEFAULT_TAGGED_COLOR_POOL.push(Colors.DARK_GRAY);
		DEFAULT_TAGGED_COLOR_POOL.push(Color.GREEN);
		DEFAULT_TAGGED_COLOR_POOL.push(Color.BLUE);
		DEFAULT_TAGGED_COLOR_POOL.push(Colors.AQUAMARINE);
		DEFAULT_TAGGED_COLOR_POOL.push(Colors.CHARTREUSE);
		DEFAULT_TAGGED_COLOR_POOL.push(Colors.FOREST_GREEN);
		DEFAULT_TAGGED_COLOR_POOL.push(Colors.NAVY_BLUE);
		DEFAULT_TAGGED_COLOR_POOL.push(Colors.LIME_GREEN);
		DEFAULT_TAGGED_COLOR_POOL.push(Colors.TURQUOISE);
	}

	/**
	 * Create a list of tagged colors that can be "checked out" by other
	 * classes.
	 */
	public TaggedColorPool()
	{
		// resets the colors in the taggedColorPool and rebuilds the
		// taggedHashTable (replacing its colors with the new taggedColors)
		resetTaggedColors();
	}

	/**
	 * Resets the colors in the taggedHashTable and the taggedColorPool.
	 */
	private void resetTaggedColors()
	{
		Color[] allTaggedColors = getAllTaggingColors();
		if(allTaggedColors == null)
		{
			// use the default tagging colors
			for(int i = 0; i < DEFAULT_TAGGED_COLOR_POOL.size(); i++)
			{
				taggedColorPool.push(DEFAULT_TAGGED_COLOR_POOL.get(i));
			}
		}
		else
		{
			// use the tagging colors specified by a child class
			// (load in reverse order so pops off the stack in the correct
			// order).
			for(int i = allTaggedColors.length - 1; i >= 0; i--)
			{
				taggedColorPool.push(allTaggedColors[i]);
			}
		}

		// make sure the tagged hashtable colors are switched to the new colors
		// specified above
		if(!taggedHashTable.isEmpty())
		{
			Object[] keys = taggedHashTable.keySet().toArray();
			taggedHashTable.clear();
			for(int i = 0; i < keys.length; i++)
			{
				reserveTaggedColor(keys[i]);
			}
		}
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
		return null;
	}

	/**
	 * Gets a tagged color which depends on the object (usually an analysis)
	 * that tagged the cell. Each object is assigned a different tagged color.
	 * If there are not enough tagged colors for all of the tagging objects,
	 * then the default tagged color is used.
	 * 
	 * @param taggingObject
	 *            The object that tagged the cell (usually an analysis).
	 * @return The tagged color unique to the taggingObject (or if runs out of
	 *         tagged colors, then the default tagged color is returned).
	 */
	public Color getTaggedColor(Object taggingObject)
	{
		// the tagged color that will be returned
		Color taggedColor = taggedHashTable.get(taggingObject);

		// will be null if the taggingObject wasn't a key in the hashtable
		if(taggedColor == null)
		{
			// so try to add the taggingObject to the hashtable
			reserveTaggedColor(taggingObject);

			// now get that color
			taggedColor = taggedHashTable.get(taggingObject);
		}

		// should never be null, but to be safe...
		if(taggedColor == null)
		{
			taggedColor = DEFAULT_TAGGED_COLOR;
		}

		return taggedColor;
	}

	/**
	 * Releases the tagged color that has been assigned to the taggingObject.
	 * This method is part of an "object pool" for tagged colors.
	 * 
	 * @param taggingObject
	 *            The object that was previously assigned a tagged color.
	 */
	public static void releaseTaggedColor(Object taggingObject)
	{
		// release the color associated with the taggingObject
		Color taggedColor = taggedHashTable.remove(taggingObject);

		// return that color to the pool (the stack)
		if(taggedColor != null)
		{
			taggedColorPool.push(taggedColor);
		}
	}

	/**
	 * Reserves a tagged color that will be associated with the taggingObject.
	 * Reserved colors must later be returned or released with the
	 * releaseTaggedColor() method. This method is part of an "object pool" for
	 * tagged colors.
	 * 
	 * @param taggingObject
	 *            The object that will be assigned a tagged color.
	 */
	public static void reserveTaggedColor(Object taggingObject)
	{
		// get a tagged color that has not yet been selected from the pool (the
		// stack)
		Color taggedColor = null;

		// make sure a color has not already been reserved
		if(taggedHashTable.get(taggingObject) == null)
		{
			if(!taggedColorPool.isEmpty())
			{
				taggedColor = taggedColorPool.pop();
			}

			if(taggedColor == null)
			{
				taggedColor = DEFAULT_TAGGED_COLOR;
			}

			taggedHashTable.put(taggingObject, taggedColor);
		}
	}
}
