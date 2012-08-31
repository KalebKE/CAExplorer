/*
 FireColorScheme -- a class within the Cellular Automaton Explorer. 
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
 * A "fire-like" color scheme for displaying cells in the CA. Returns black for
 * empty states and gradations of yellow to red colors for other states. The
 * filled state is red.
 * 
 * @author David Bahr
 */
public class FireColorScheme extends ColorScheme
{
    /**
     * The display name of this color scheme.
     */
    public static final String schemeName = "Fire";

    /**
     * Create a color scheme with a fire-like colors.
     */
    public FireColorScheme()
    {
        // the fire colors
        super(Color.BLACK, Color.RED, Color.YELLOW, Color.RED);
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
        Color[] taggingColors = {new Color(0, 255, 0), Color.BLUE,
            Colors.PURPLE, Colors.TURQUOISE, Colors.NAVY_BLUE,
            Colors.FOREST_GREEN, Colors.DARK_VIOLET, Color.CYAN,
            Colors.AQUAMARINE, Colors.CHARTREUSE, Colors.LIME_GREEN,
            Colors.HOT_PINK};

        return taggingColors;
    }
}
