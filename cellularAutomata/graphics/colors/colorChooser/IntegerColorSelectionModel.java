/*
 IntegerColorSelectionModel -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.graphics.colors.colorChooser;

import java.awt.Color;

import javax.swing.colorchooser.DefaultColorSelectionModel;

import cellularAutomata.graphics.colors.ColorScheme;

/**
 * Extends a regular color chooser so that it stores both the currently selected
 * color as well as it's integer equivalent.
 * 
 * @author David Bahr
 */
public class IntegerColorSelectionModel extends DefaultColorSelectionModel
{
    /** The currently selected state value. */
    private int stateValue = 0;

    /**
     * Creates a new color selection model with the default color and state.
     */
    public IntegerColorSelectionModel()
    {
        this(ColorScheme.EMPTY_COLOR, 0);
    }

    /**
     * Creates a new color selection model with a given selected color and an
     * associated state value (an integer).
     * 
     * @param color
     *            The initial color.
     * 
     * @param stateValue
     *            The state value associated with the color.
     * 
     * @throws Error
     *             If the color is null.
     */
    public IntegerColorSelectionModel(Color color, int stateValue)
    {
        super(color);
        this.stateValue = stateValue;
    }

    /**
     * Returns the selected state value.
     * 
     * @return The selected state.
     */
    public int getSelectedState()
    {
        return stateValue;
    }

    /**
     * This method sets the color.
     * 
     * @param color
     *            The color to set.
     * @param stateValue
     *            The cell state value associated with the color.
     * 
     * @throws Error
     *             If the color is set.
     */
    public void setSelectedColorAndState(Color color, int stateValue)
    {

        // get the old color and state
        Color oldColor = getSelectedColor();
        int oldState = this.stateValue;

        // the stateValue line must come before the setSelectedColor() line
        // because setSelectedColor() fires a stateChanged event, and we need
        // the state value to be set first!
        this.stateValue = stateValue;
        setSelectedColor(color);

        if(color.equals(oldColor) && (stateValue != oldState))
        {
            // then no event was fired because the color remained the same, but
            // we *need* to fire an event because the state changed
            fireStateChanged();
        }
    }
}
