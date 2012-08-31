/*
 GreenOceanColorScheme -- a class within the Cellular Automaton Explorer. 
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

/**
 * An ocean blue and green color scheme for displaying cells in the CA.
 * 
 * @author David Bahr
 */
public class GreenOceanColorScheme extends ColorScheme
{
    /**
     * The display name of this color scheme.
     */
    public static final String schemeName = "Green Ocean";

    private static final Color OCEAN_GREEN = new Color(102, 255, 0);

    /**
     * Create a color scheme with ocean blue and green colors.
     */
    public GreenOceanColorScheme()
    {
        // the ocean colors
        super(Color.BLACK, OCEAN_GREEN, Color.BLUE, OCEAN_GREEN);
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
            Colors.HOT_PINK, Color.MAGENTA,
            Colors.PURPLE, Color.YELLOW.darker(), Colors.NAVY_BLUE,
            Colors.DARK_VIOLET, Color.PINK};

        return taggingColors;
    }
}
