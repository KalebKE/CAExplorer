/*
 ComplexModulusView -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.cellState.model.CellState;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.math.Complex;

/**
 * A view that displays complex numbers as the modulus of their values. The
 * displayed color is a shade between the modulus of a specified lower and upper
 * bound.
 * 
 * @author David Bahr
 */
public class ComplexModulusView extends TriangleHexagonCellStateView
{
	// The smaller of two complex numbers whose modulus is used to represent a
	// lighter color.
	private Complex lowerBound = null;

	// The larger of two complex numbers whose modulus is used to represent a
	// darker color.
	private Complex upperBound = null;

	/**
	 * Create a view that displays scaled shades between the modulus of the
	 * lower and upper bound.
	 * 
	 * @param lowerBound
	 *            The smaller of two complex numbers whose modulus is used to
	 *            represent a lighter color.
	 * @param upperBound
	 *            The larger of two complex numbers whose modulus is used to
	 *            represent a darker color.
	 */
	public ComplexModulusView(Complex lowerBound, Complex upperBound)
	{
		super();

		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	/**
	 * Creates a display color based on the magnitude of the complex number in
	 * the cell. Creates a fractional shading between the default filled and
	 * empty colors.
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
	public Color getColor(CellState state, Integer numStates,
			Coordinate rowAndCol)
	{
		// the cell's value
		Complex cellValue = (Complex) state.getValue();

		// the modulus of the cellValue
		double cellMagnitude = cellValue.modulus();

		// the moduli of the upper and lower bounds
		double upperMagnitude = upperBound.modulus();
		double lowerMagnitude = lowerBound.modulus();

		// the scaled value between 0 and 1 (scaled by the difference between
		// the upper and lower bound)
		double scaledValueOfCell = (cellMagnitude - lowerMagnitude)
				/ (upperMagnitude - lowerMagnitude);

		// make sure we don't exceed the limits
		if(scaledValueOfCell < 0.0)
		{
			scaledValueOfCell = 0.0;
		}
		else if(scaledValueOfCell > 1.0)
		{
			scaledValueOfCell = 1.0;
		}

		// now select a color scaled between the empty and filled color
		Color filledColor = ColorScheme.FILLED_COLOR;
		Color emptyColor = ColorScheme.EMPTY_COLOR;

		double redDiff = filledColor.getRed() - emptyColor.getRed();
		double greenDiff = filledColor.getGreen() - emptyColor.getGreen();
		double blueDiff = filledColor.getBlue() - emptyColor.getBlue();

		int red = (int) Math.floor(emptyColor.getRed()
				+ (scaledValueOfCell * redDiff));
		int green = (int) Math.floor(emptyColor.getGreen()
				+ (scaledValueOfCell * greenDiff));
		int blue = (int) Math.floor(emptyColor.getBlue()
				+ (scaledValueOfCell * blueDiff));

		return new Color(red, green, blue);
	}
}