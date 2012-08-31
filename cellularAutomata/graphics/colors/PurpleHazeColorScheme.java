/*
 PurpleHazeColorScheme -- a class within the Cellular Automaton Explorer. 
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

/**
 * A purple color scheme for displaying cells in the CA. Overrides the
 * getColor() methods of the parent class to return black for empty states and
 * gradations of purple for other states. The filled state is purple.
 * 
 * @author David Bahr
 */
public class PurpleHazeColorScheme extends KindOfBluesColorScheme
{
    /**
     * The display name of this color scheme.
     */
    public static final String schemeName = "Purple Haze";

    // the default empty color for the shades color scheme
    private static final Color DEFAULT_EMPTY_SHADES_COLOR = new Color(75, 0, 75);

    // the default filled color for the shades color scheme
    private static final Color DEFAULT_FILLED_SHADES_COLOR = new Color(230, 0,
        230);

    /**
     * Create a color scheme with a gradation of colors.
     */
    public PurpleHazeColorScheme()
    {
        super(DEFAULT_EMPTY_SHADES_COLOR, DEFAULT_FILLED_SHADES_COLOR);
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
            Colors.CHARTREUSE, Color.GRAY, Colors.FOREST_GREEN,
            Colors.LIME_GREEN, Color.CYAN, Colors.AQUAMARINE, Colors.HOT_PINK,
            Colors.NAVY_BLUE, Color.GREEN, Colors.TURQUOISE, Color.BLUE};

        return taggingColors;
    }
}
