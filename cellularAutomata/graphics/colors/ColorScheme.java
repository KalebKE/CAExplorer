/*
 ColorScheme -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.view.CellStateView;

/**
 * A color scheme for displaying cells in the CA. Has an overloaded method
 * getColor() which is used to retrieve the appropriate color for integer rules,
 * non-integer rules, etc. Child classes should override the getColor() methods
 * as necessary.
 * <p>
 * PLEASE NOTE: I'm still fine tuning this class. My apologies for any poor
 * implementation or poor software engineering. The amount of refactoring has
 * been tremendous. The older ColorTools class has a lot of overlap that needs
 * to be removed.
 * <p>
 * Note, this class extends TaggedColorPool for convenience. Composition might
 * be better because the ColorScheme is not really an extension of the concepts
 * in the ColorScheme. However, the extension is particularly handy for
 * encapsulating the tagged colors in a separate class. In this case,
 * encapsulation seemed more important than composition.
 * 
 * @author David Bahr
 */
public abstract class ColorScheme extends TaggedColorPool
{
    /**
     * The default color used for a filled (occupied) cell when no other is
     * specified.
     */
    public final static Color DEFAULT_FILLED_COLOR = new Color(0, 0, 102);

    /**
     * The default color used for an empty (unoccupied) cell when no other is
     * specified.
     */
    public final static Color DEFAULT_EMPTY_COLOR = new Color(191, 191, 255);

    /**
     * The default color used for drawing a cell with a right-click when no
     * other is specified.
     */
    public final static Color DEFAULT_SECOND_DRAW_COLOR = DEFAULT_EMPTY_COLOR;

    // Note: the following SECOND_DRAW_COLOR must be updated at the same time
    // that IntegerCellState.SECOND_DRAW_STATE is updated.
    /**
     * The default color used for drawing a cell when no other is specified.
     */
    public final static Color DEFAULT_DRAW_COLOR = DEFAULT_FILLED_COLOR;

    /**
     * The color used for a cell drawn with a right-click by the user.
     */
    public static Color SECOND_DRAW_COLOR = DEFAULT_SECOND_DRAW_COLOR;

    // Note: the following DRAW_COLOR must be updated at the same time that
    // IntegerCellState.DRAW_STATE is updated.
    /**
     * The color used for a cell drawn with a left-click by the user.
     */
    public static Color DRAW_COLOR = DEFAULT_DRAW_COLOR;

    /**
     * The color used for an empty (unoccupied) cell.
     */
    public static Color EMPTY_COLOR = ColorScheme.DEFAULT_EMPTY_COLOR;

    /**
     * The color used for a filled (occupied) cell.
     */
    public static Color FILLED_COLOR = ColorScheme.DEFAULT_FILLED_COLOR;

    /**
     * Colors for each state are generated as a gradient between this color and
     * the FILLED_GRADIENT_COLOR. By default, this is the same as the
     * EMPTY_COLOR, but some color schemes (like GreenOceanColorScheme) have a
     * gradient of colors with a separate different color for the empty color.
     * For example, states in the GreenOceanColorScheme are colored as a
     * gradient between blue (EMPTY_GRADIENT_COLOR) and green
     * (FILLED_GRADIENT_COLOR), but the empty state (EMPTY_COLOR) is replaced
     * with black for extra contrast.
     */
    public static Color EMPTY_GRADIENT_COLOR = ColorScheme.DEFAULT_EMPTY_COLOR;

    /**
     * Colors for each state are generated as a gradient between the
     * EMPTY_GRADIENT_COLOR and this color (FILLED_GRADIENT_COLOR). By default,
     * this is the same as the FILLED_COLOR, but some color schemes (like
     * BlueDiamondColorScheme) have a gradient of colors with a separate
     * different color for the filled color. For example, states in the
     * BlueDiamondColorScheme are colored as a gradient between dark grey
     * (EMPTY_GRADIENT_COLOR) and light grey (FILLED_GRADIENT_COLOR), but the
     * filled state (FILLED_COLOR) is replaced with blue for extra contrast.
     */
    public static Color FILLED_GRADIENT_COLOR = ColorScheme.DEFAULT_FILLED_COLOR;

    /**
     * Create a color scheme such as a rainbow of colors, shadings between the
     * filled and empty colors, etc.
     */
    public ColorScheme()
    {
        super();

        // always do this when resetting the empty and filled colors
        checkDrawingColors();
        // setColorsInProperties();
    }

    /**
     * Create a color scheme such as a rainbow of colors, shadings between the
     * filled and empty colors, etc.
     * <p>
     * Colors for each state are generated as a gradient between the
     * EMPTY_GRADIENT_COLOR and the FILLED_GRADIENT_COLOR. In his constructor,
     * these colors are assigned the same values as the EMPTY_COLOR and
     * FILLED_COLOR
     * 
     * @param emptyColor
     *            The color for empty cells (in other words, for integer rules,
     *            cells with state 0).
     * @param filledColor
     *            The color for filled cells (in other words, for integer rules,
     *            cells with state N-1 when there are N states).
     */
    public ColorScheme(Color emptyColor, Color filledColor)
    {
        super();

        EMPTY_COLOR = emptyColor;
        FILLED_COLOR = filledColor;
        EMPTY_GRADIENT_COLOR = emptyColor;
        FILLED_GRADIENT_COLOR = filledColor;

        // always do this when resetting the empty and filled colors
        checkDrawingColors();
        // setColorsInProperties();
    }

    /**
     * Create a color scheme such as a rainbow of colors, shadings between the
     * filled and empty colors, etc.
     * <p>
     * Colors for each state are generated as a gradient between the
     * EMPTY_GRADIENT_COLOR and the FILLED_GRADIENT_COLOR. By default (see other
     * constructor), these colors are the same as the EMPTY_COLOR and
     * FILLED_COLOR, but some color schemes (like GreenOceanColorScheme) have a
     * gradient of colors with separate different colors for the empty and
     * filled colors. For example, states in the GreenOceanColorScheme are
     * colored as a gradient between blue (EMPTY_GRADIENT_COLOR) and green
     * (FILLED_GRADIENT_COLOR), but the empty state (EMPTY_COLOR) is replaced
     * with black for extra contrast.
     * 
     * @param emptyColor
     *            The color for empty cells (in other words, for integer rules,
     *            cells with state 0).
     * @param filledColor
     *            The color for filled cells (in other words, for integer rules,
     *            cells with state N-1 when there are N states).
     * @param emptyGradientColor
     *            The color used to generate the gradient of colors for states
     *            from 1 to N-2, with 1 being close to this color.
     * @param filledGradientColor
     *            The color used to generate the gradient of colors for states
     *            from 1 to N-2, with N-2 being close to this color.
     */
    public ColorScheme(Color emptyColor, Color filledColor,
        Color emptyGradientColor, Color filledGradientColor)
    {
        super();

        EMPTY_COLOR = emptyColor;
        FILLED_COLOR = filledColor;
        EMPTY_GRADIENT_COLOR = emptyGradientColor;
        FILLED_GRADIENT_COLOR = filledGradientColor;

        // always do this when resetting the empty and filled colors
        checkDrawingColors();
        // setColorsInProperties();
    }

    /**
     * Makes sure that the drawing colors are changed to match the filled and
     * empty colors.
     */
    private void checkDrawingColors()
    {
        // might also need to change the drawing colors
        if(SECOND_DRAW_COLOR.equals(EMPTY_COLOR))
        {
            SECOND_DRAW_COLOR = EMPTY_COLOR;
        }
        if(DRAW_COLOR.equals(EMPTY_COLOR))
        {
            DRAW_COLOR = EMPTY_COLOR;
        }

        // might also need to change the drawing colors
        if(SECOND_DRAW_COLOR.equals(FILLED_COLOR))
        {
            SECOND_DRAW_COLOR = FILLED_COLOR;
        }
        if(DRAW_COLOR.equals(FILLED_COLOR))
        {
            DRAW_COLOR = FILLED_COLOR;
        }
    }

    /**
     * Gets a color for the integer state using a gradation of colors between
     * the empty and filled "gradient" colors. Replaces the filled and empty
     * states with their specified colors (which may fall outside the
     * gradation).
     * <p>
     * Colors for each state are generated as a gradient between the
     * EMPTY_GRADIENT_COLOR and the FILLED_GRADIENT_COLOR. By default, this is
     * the same as the FILLED_COLOR, but some color schemes (like
     * BlueDiamondColorScheme) have a gradient of colors with a separate
     * different color for the filled and empty colors. For example, states in
     * the BlueDiamondColorScheme are colored as a gradient between dark grey
     * (EMPTY_GRADIENT_COLOR) and light grey (FILLED_GRADIENT_COLOR), but the
     * filled state (FILLED_COLOR) is replaced with blue for extra contrast.
     * 
     * @param state
     *            The current cell state.
     * @param numStates
     *            The number of states used in the simulation.
     * @param currentView
     *            The view used by the cell.
     * 
     * @return A color appropriate for the given state.
     */
    private Color getColorGradient(int state, int numStates,
        CellStateView currentView)
    {
        Color returnColor = EMPTY_COLOR;

        if(state == numStates - 1)
        {
            returnColor = FILLED_COLOR;
        }
        else if(state != 0)
        {
            returnColor = ColorTools.getColorFromSingleValue(state, numStates);
        }

        return returnColor;
    }

    /**
     * Get a color that is a fractional shading between the empty and the filled
     * color.
     * 
     * @param percent
     *            The percent of the shading between the empty and filled colors
     *            (0.0 to 1.0). A 0.0 is the empty color. A 1.0 is the filled
     *            color.
     * 
     * @return A color that is a shade between the filled and empty color.
     */
    public Color getColor(double percent)
    {
        // make sure is in valid range
        if(percent > 1.0)
        {
            percent = 1.0;
        }
        else if(percent < 0.0)
        {
            percent = 0.0;
        }

        // the red, green, and blue colors that will be returned. Note that the
        // empty (0.0) and filled (1.0) percentages are treated differently than
        // the percentages inbetween. The default is empty.
        int red = EMPTY_COLOR.getRed();
        int green = EMPTY_COLOR.getGreen();
        int blue = EMPTY_COLOR.getBlue();

        if(percent == 1.0)
        {
            red = FILLED_COLOR.getRed();
            green = FILLED_COLOR.getGreen();
            blue = FILLED_COLOR.getBlue();
        }
        else if(percent != 0.0)
        {
            // use the gradient colors, because we are inbetween
            double redDiff = FILLED_GRADIENT_COLOR.getRed()
                - EMPTY_GRADIENT_COLOR.getRed();
            double greenDiff = FILLED_GRADIENT_COLOR.getGreen()
                - EMPTY_GRADIENT_COLOR.getGreen();
            double blueDiff = FILLED_GRADIENT_COLOR.getBlue()
                - EMPTY_GRADIENT_COLOR.getBlue();

            double redDelta = percent * redDiff;
            double greenDelta = percent * greenDiff;
            double blueDelta = percent * blueDiff;

            red = (int) Math.floor(EMPTY_GRADIENT_COLOR.getRed() + redDelta);
            green = (int) Math.floor(EMPTY_GRADIENT_COLOR.getGreen()
                + greenDelta);
            blue = (int) Math.floor(EMPTY_GRADIENT_COLOR.getBlue() + blueDelta);
        }

        return new Color(red, green, blue);
    }

    /**
     * Only valid for cells with integer states, this gets a color corresponding
     * to the given state. Returns a gradient of colors between the
     * EMPTY_GRADIENT_COLOR and the FILLED_GRADIENT_COLOR. The empty state (0)
     * is returned as the EMPTY_COLOR, and the filled state (numStates-1) is
     * returned as the FILLED_COLOR.
     * 
     * @param state
     *            The current cell state.
     * @param numStates
     *            The number of states used in the simulation.
     * @param currentView
     *            The view used by the cell.
     * 
     * @return A color appropriate for the given state.
     */
    public Color getColor(int state, int numStates, CellStateView currentView)
    {
        return getColorGradient(state, numStates, currentView);
    }

    /**
     * Gets the empty color.
     * 
     * @return The empty color.
     */
    public Color getEmptyColor()
    {
        return EMPTY_COLOR;
    }

    /**
     * Gets the filled color.
     * 
     * @return The filled color.
     */
    public Color getFilledColor()
    {
        return FILLED_COLOR;
    }

    /**
     * Gets the empty gradient color.
     * <p>
     * Colors for each state are generated as a gradient between this color and
     * the FILLED_GRADIENT_COLOR. By default, the empty gradient color is the
     * same as the EMPTY_COLOR, but some color schemes (like
     * GreenOceanColorScheme) have a gradient of colors with a separate
     * different color for the empty color. For example, states in the
     * GreenOceanColorScheme are colored as a gradient between blue
     * (EMPTY_GRADIENT_COLOR) and green (FILLED_GRADIENT_COLOR), but the empty
     * state (EMPTY_COLOR) is replaced with black for extra contrast.
     * 
     * @return The empty gradient color.
     */
    public Color getEmptyGradientColor()
    {
        return EMPTY_GRADIENT_COLOR;
    }

    /**
     * Gets the filled gradient color.
     * <p>
     * Colors for each state are generated as a gradient between the
     * EMPTY_GRADIENT_COLOR and this color (FILLED_GRADIENT_COLOR). By default,
     * this is the same as the FILLED_COLOR, but some color schemes (like
     * BlueDiamondColorScheme) have a gradient of colors with a separate
     * different color for the filled color. For example, states in the
     * BlueDiamondColorScheme are colored as a gradient between dark grey
     * (EMPTY_GRADIENT_COLOR) and light grey (FILLED_GRADIENT_COLOR), but the
     * filled state (FILLED_COLOR) is replaced with blue for extra contrast.
     * 
     * @return The filled gradient color.
     */
    public Color getFilledGradientColor()
    {
        return FILLED_GRADIENT_COLOR;
    }

    /**
     * Set the empty color.
     * <p>
     * Colors for each state are generated as a gradient between this color and
     * the FILLED_GRADIENT_COLOR. By default, the empty gradient color is the
     * same as the EMPTY_COLOR, but some color schemes (like
     * GreenOceanColorScheme) have a gradient of colors with a separate
     * different color for the empty color. For example, states in the
     * GreenOceanColorScheme are colored as a gradient between blue
     * (EMPTY_GRADIENT_COLOR) and green (FILLED_GRADIENT_COLOR), but the empty
     * state (EMPTY_COLOR) is replaced with black for extra contrast.
     * 
     * @param emptyColor
     *            The empty color.
     */
    public void setEmptyColor(Color emptyColor)
    {
        EMPTY_COLOR = emptyColor;

        // always do this when resetting the empty and filled colors
        checkDrawingColors();
        // setColorsInProperties();
    }

    /**
     * Set the filled color.
     * 
     * @param filledColor
     *            The filled color.
     */
    public void setFilledColor(Color filledColor)
    {
        FILLED_COLOR = filledColor;

        // always do this when resetting the empty and filled colors
        checkDrawingColors();
        // setColorsInProperties();
    }

    /**
     * Set the empty gradientcolor.
     * 
     * @param emptyGradientColor
     *            The empty gradient color.
     */
    public void setEmptyGradientColor(Color emptyGradientColor)
    {
        EMPTY_GRADIENT_COLOR = emptyGradientColor;

        // always do this when resetting the empty and filled colors
        checkDrawingColors();
        // setColorsInProperties();
    }

    /**
     * Set the filled gradient color.
     * <p>
     * Colors for each state are generated as a gradient between the
     * EMPTY_GRADIENT_COLOR and this color (FILLED_GRADIENT_COLOR). By default,
     * this is the same as the FILLED_COLOR, but some color schemes (like
     * BlueDiamondColorScheme) have a gradient of colors with a separate
     * different color for the filled color. For example, states in the
     * BlueDiamondColorScheme are colored as a gradient between dark grey
     * (EMPTY_GRADIENT_COLOR) and light grey (FILLED_GRADIENT_COLOR), but the
     * filled state (FILLED_COLOR) is replaced with blue for extra contrast.
     * 
     * @param filledGradientColor
     *            The filled gradient color.
     */
    public void setFilledGradientColor(Color filledGradientColor)
    {
        FILLED_GRADIENT_COLOR = filledGradientColor;

        // always do this when resetting the empty and filled colors
        checkDrawingColors();
        // setColorsInProperties();
    }

    /**
     * Resets the drawing colors to the FILLED and EMPTY colors.
     */
    public static void resetDrawingColors()
    {
        DRAW_COLOR = FILLED_COLOR;
        SECOND_DRAW_COLOR = EMPTY_COLOR;

        // also reset the integer values for the states (only applies to
        // IntegerCellState, but safer to always do this).
        IntegerCellState.DRAW_STATE = -1;
        IntegerCellState.SECOND_DRAW_STATE = -1;
    }
}
