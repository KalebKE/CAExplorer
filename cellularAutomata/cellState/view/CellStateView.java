/*
 CellStateView -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.cellState.view;

import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;

import cellularAutomata.cellState.model.CellState;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.graphics.colors.TaggedColorPool;
import cellularAutomata.graphics.colors.RainbowColorScheme;
import cellularAutomata.util.Coordinate;

/**
 * Provides a graphics context for CellStates. Tells a CellState how to display
 * on the CA graphics. Subclasses will probably wish to use the colorScheme to
 * help define appropriate colors, particularly when implementing getColor().
 * <p>
 * This class uses the Template Method design pattern. Subclasses implement the
 * abstract methods getColor() and getTaggedColor() which are called by the
 * template method getDisplayColor(). The template returns a color that depends
 * on whether or not the cell state has been "tagged" for visibility.
 * 
 * @author David Bahr
 */
public abstract class CellStateView
{
	/**
	 * The color scheme used to paint cells (rainbow, fire, etc.).
	 */
	public static ColorScheme colorScheme = getDefaultColorScheme();

	/**
	 * The alpha component of the tagged color. Should be between 0 (fully
	 * transparent) and 255 (fully opaque). 200 strikes a nice balance with the
	 * tagged color dominating, but underlying colors showing through as shades.
	 */
	private static final int TAGGED_ALPHA = 200;

	/**
	 * Gets the default color scheme used for drawing cells. This method creates
	 * a new copy of the default color scheme so that any old values are reset.
	 * 
	 * @return the default color scheme.
	 */
	public final static ColorScheme getDefaultColorScheme()
	{
		return new RainbowColorScheme(ColorScheme.DEFAULT_EMPTY_COLOR,
				ColorScheme.DEFAULT_FILLED_COLOR);
	}

	/**
	 * The colors of each cell are usually based on the selected color scheme,
	 * but sometimes the view may wish to prevent the colors from changing. For
	 * example the ForestFire rule wants "tree states" to be green no matter
	 * what.
	 * <p>
	 * The default behavior is to allow all color schemes, but child classes may
	 * override this method to prevent color schemes from being displayed in the
	 * menu. In general, this method should return false if the rule wants to
	 * create a CellStateView that uses fixed colors (that won't change with the
	 * scheme).
	 * 
	 * @return true if all color schemes are allowed and will be enabled in the
	 *         menu, and false if the color schemes will be disabled in the
	 *         menu.
	 */
	public boolean enableColorSchemes()
	{
		return true;
	}

	/**
	 * Should create a Shape appropriate for displaying the average of the
	 * states given in the array. For example, each state might be a vector, and
	 * the average shape could be an arrow indicating the average of the
	 * vectors. For most cellular automaton, the shapes are identical for all
	 * states (for example, a square for the "game of life"), so the average is
	 * just that shape. If the default shape is appropriate, this method may
	 * return null.
	 * 
	 * @param states
	 *            The cell states for which the average shape is being
	 *            calculated.
	 * @param width
	 *            The width of the rectangle that bounds this Shape. May be
	 *            ignored.
	 * @param height
	 *            The height of the rectangle that bounds this Shape. May be
	 *            ignored.
	 * @param rowAndCol
	 *            The row and col of the shape being displayed. May be ignored.
	 * @return The shape to be displayed. May be null (in which case the CA
	 *         graphics should use a default shape).
	 */
	public abstract Shape getAverageDisplayShape(CellState[] states, int width,
			int height, Coordinate rowAndCol);

	/**
	 * Should create a Color appropriate for displaying the current state of
	 * this cellState. For example, if the state is 1, then an appropriate color
	 * might be Color.BLACK.
	 * 
	 * @param state
	 *            The cell state that will be displayed.
	 * @param numStates
	 *            If relevant, the number of possible states (which may not be
	 *            the same as the currently active number of states) -- may be
	 *            null which indicates that the number of states is inapplicable
	 *            or that the currently active number of states should be used.
	 *            (See for example, createProbabilityChoosers() method in
	 *            InitialStatesPanel class.)
	 * @param rowAndCol
	 *            The row and col of the cell being displayed. May be ignored.
	 * @return The color to be displayed.
	 */
	protected abstract Color getColor(CellState state, Integer numStates,
			Coordinate rowAndCol);

	/**
	 * Should create a Color appropriate for displaying the current state of
	 * this cellState. For example, if the state is 1, then an appropriate color
	 * might be Color.BLACK.
	 * 
	 * @param state
	 *            The cell state that will be displayed.
	 * @param numStates
	 *            If relevant, the number of possible states (which may not be
	 *            the same as the currently active number of states) -- may be
	 *            null which indicates that the number of states is inapplicable
	 *            or that the currently active number of states should be used.
	 *            (See for example, createProbabilityChoosers() method in
	 *            InitialStatesPanel class.)
	 * @param rowAndCol
	 *            The row and col of the cell being displayed. May be ignored.
	 * @return The color to be displayed.
	 */
	public Color getDisplayColor(CellState state, Integer numStates,
			Coordinate rowAndCol)
	{
		if(state.isTagged())
		{
			return getTaggedColor(state, numStates, rowAndCol);
		}
		else
		{
			return getColor(state, numStates, rowAndCol);
		}
	}

	/**
	 * Should create a Shape appropriate for displaying the current state of
	 * this cellState. For example, the Shape could be a Rectangle2D, Line2D,
	 * Polygon, or GeneralPath. The position of the Shape is irrelevant because
	 * it will be translated to the correct location by the code in the cellular
	 * automaton's paintComponent.
	 * <p>
	 * This may return null, in which case the CA will use a default shape.
	 * However, to avoid problems with erasing old images (and other display
	 * problems), if any state returns a non-null shape then all states should
	 * return a non-null state. If an empty shape is needed (in place of a
	 * null), then try using the zero-sized circle "new Ellipse2D.Double(0.0,
	 * 0.0, 0.0, 0.0);".
	 * <p>
	 * The display shape will be drawn with the specified line thickness (from
	 * getStroke()). Therefore, a shape may need to account for the extra
	 * thickness of the stroke, particularly if the shape needs to fall entirely
	 * within a grid cell.
	 * 
	 * @param state
	 *            The cell state that will be displayed.
	 * @param width
	 *            The width of the rectangle that bounds this Shape. May be
	 *            ignored.
	 * @param height
	 *            The height of the rectangle that bounds this Shape. May be
	 *            ignored.
	 * @param rowAndCol
	 *            The row and col of the shape being displayed. May be ignored.
	 * @return The shape to be displayed. May be null (in which case the CA
	 *         graphics should use a default shape). However, to avoid problems
	 *         with erasing old images (and other display problems), if any
	 *         state returns a non-null shape then all states should return a
	 *         non-null state. If an empty shape is needed (in place of a null),
	 *         then try using the zero-sized circle "new Ellipse2D.Double(0.0,
	 *         0.0, 0.0, 0.0);".
	 */
	public abstract Shape getDisplayShape(CellState state, int width,
			int height, Coordinate rowAndCol);

	/**
	 * Specifies the line thickness (or stroke) of the display shape.
	 * 
	 * @param state
	 *            The cell state that will be displayed.
	 * @param width
	 *            The width of the rectangle that bounds this Shape. May be
	 *            ignored.
	 * @param height
	 *            The height of the rectangle that bounds this Shape. May be
	 *            ignored.
	 * @param rowAndCol
	 *            The row and col of the shape being displayed. May be ignored.
	 * @return Unless overridden by a child class, this returns null, forcing
	 *         the default line thickness to be used when rendering the display
	 *         shape.
	 */
	public Stroke getStroke(CellState state, int width, int height,
			Coordinate rowAndCol)
	{
		return null;
	}

	/**
	 * Modifies the specified color with the tagged color. Creates a new Color
	 * appropriate for displaying with tagged cells. Tagged cells are modified
	 * for high visibility, usually so that they are easy to pick out from other
	 * colors displayed by the CA.
	 * <p>
	 * Child classes may wish to override this method (for example, see the
	 * ForestFireView in the ForestFire rule).
	 * 
	 * @param originalColor
	 *            The original color that will be modified with the tagged
	 *            color.
	 * @param taggingColor
	 *            The tagging color used to modify the original color.
	 * @return The original color, but modified with the tagged color for high
	 *         visibility.
	 */
	public Color modifyColorWithTaggedColor(Color originalColor,
			Color taggingColor)
	{
		// the tagged color's alpha component between 0 (transparent) and 255
		// (opaque)
		int taggedAlpha = TAGGED_ALPHA;

		// find out if the tagged cells will be translucent or opaque
		if(!TaggedColorPool.taggedCellsTranslucent)
		{
			// then make opaque
			taggedAlpha = 255;
		}

		// find out if the tagged cells will be invisible (no extra color
		// added). i.e., alpha = 0.
		if(TaggedColorPool.taggedCellsNoExtraColor)
		{
			// then make invisible
			taggedAlpha = 0;
		}

		// the tagged color
		int taggedRed = taggingColor.getRed();
		int taggedGreen = taggingColor.getGreen();
		int taggedBlue = taggingColor.getBlue();

		// get the original color of the cell
		int cellsRed = originalColor.getRed();
		int cellsGreen = originalColor.getGreen();
		int cellsBlue = originalColor.getBlue();
		int cellsAlpha = originalColor.getAlpha();

		// the Porter-Duff equations for alpha transparency are
		// Anew = Ao + Au * (1-Ao)
		// Cnew = Co + Cu * (1-Ao) where C is each component (R, G, B) and Co is
		// the tagged (overlying) color and Cu is the (underlying) cell
		// color. Ao is the alpha transparency of the overlying color, and Au is
		// the alpha transparency of the underlying color.
		//
		// In the code below, I convert to a range from 0 to 255 (instead of 0.0
		// to 1.0), and clamp the values in this range. For a reference, see
		// Haase and Guy, "Filthy Rich Clients", 2007, chapter 6.
		//
		// Note: these equations assume that each color component has been
		// premultiplied by the transparency.
		cellsRed = (cellsRed * cellsAlpha) / 255; // premultiply red
		cellsGreen = (cellsGreen * cellsAlpha) / 255; // premultiply green
		cellsBlue = (cellsBlue * cellsAlpha) / 255; // premultiply blue
		taggedRed = (taggedRed * taggedAlpha) / 255; // premultiply red
		taggedGreen = (taggedGreen * taggedAlpha) / 255; // premultiply green
		taggedBlue = (taggedBlue * taggedAlpha) / 255; // premultiply blue

		// int resultA = taggedAlpha + (cellsAlpha * (255 - taggedAlpha)) / 255;
		int resultR = taggedRed + (cellsRed * (255 - taggedAlpha)) / 255;
		int resultG = taggedGreen + (cellsGreen * (255 - taggedAlpha)) / 255;
		int resultB = taggedBlue + (cellsBlue * (255 - taggedAlpha)) / 255;

		// now specify the final tagged color (scaled between 0 and 255)
		originalColor = new Color(resultR, resultG, resultB);

		// should never be null, but to be safe...
		if(originalColor == null)
		{
			originalColor = new Color(255, 0, 0);
		}

		return originalColor;
	}

	/**
	 * Should create a Color appropriate for displaying the current state of a
	 * tagged cellState. Tagged cellStates are ones that are being marked for
	 * high visibility, usually so that they are easy to pick out from other
	 * states. The tagged color is given by the ColorScheme.TAGGED_COLOR which
	 * is typically a color such as Color.RED so that it is easily visible. This
	 * method may modify the tagged color to include an alpha or other
	 * component; cells with different states may display as slightly different
	 * tagged colors. I.e., the tagged color might be modified to depend on the
	 * state's current color.
	 * <p>
	 * This method cannot be modified. To modify it's behavior, instead modify
	 * the method modifyColorWithTaggedColor(), which this calls. (This ensures
	 * that the OneDimensionLatticeView gets the correctly modified tagged
	 * colors when it calls modifyColorWithTaggedColor().)
	 * 
	 * @param state
	 *            The cell state that will be displayed.
	 * @param numStates
	 *            If relevant, the number of possible states (which may not be
	 *            the same as the currently active number of states) -- may be
	 *            null which indicates that the number of states is inapplicable
	 *            or that the currently active number of states should be used.
	 *            (See for example, createProbabilityChoosers() method in
	 *            InitialStatesPanel class.)
	 * @param rowAndCol
	 *            The row and col of the cell being displayed. May be ignored.
	 * @return The color to be displayed.
	 */
	public final Color getTaggedColor(CellState state, Integer numStates,
			Coordinate rowAndCol)
	{
		// get the tagging color
		Color taggingColor = colorScheme.getTaggedColor(state
				.getTaggingObject());

		// get the original color of the cell
		Color originalColor = getColor(state, numStates, rowAndCol);

		return modifyColorWithTaggedColor(originalColor, taggingColor);
	}

	/**
	 * Gets the untagged color for a cell even if that cell has been tagged.
	 * <p>
	 * Not recommended that child classes override this method. Instead override
	 * getTaggedColor() and getColor().
	 * 
	 * @param state
	 *            The cell state that will be displayed.
	 * @param numStates
	 *            If relevant, the number of possible states (which may not be
	 *            the same as the currently active number of states) -- may be
	 *            null which indicates that the number of states is inapplicable
	 *            or that the currently active number of states should be used.
	 *            (See for example, createProbabilityChoosers() method in
	 *            InitialStatesPanel class.)
	 * @param rowAndCol
	 *            The row and col of the cell being displayed. May be ignored.
	 * @return The untagged color for a cell, even if the cell is tagged.
	 */
	public Color getUntaggedColor(CellState state, Integer numStates,
			Coordinate rowAndCol)
	{
		return getColor(state, numStates, rowAndCol);
	}
}
