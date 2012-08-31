/*
 RealValuedDefaultView -- a class within the Cellular Automaton Explorer. 
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
import cellularAutomata.cellState.model.RealValuedState;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.util.Coordinate;

/**
 * A view that displays real numbers as a shaded value between a lower and upper
 * bound. Also displays triangles and hexagons on their respective lattices.
 * 
 * @author David Bahr
 */
public class RealValuedDefaultView extends TriangleHexagonCellStateView
{
	// The smaller of two double numbers whose value is used to represent a
	// lighter color.
	private double lowerBound = RealValuedState.DEFAULT_EMPTY_STATE;

	// The larger of two double numbers whose value is used to represent a
	// darker color.
	private double upperBound = RealValuedState.DEFAULT_FULL_STATE;

	/**
	 * Create a view that displays scaled shades between the value of the lower
	 * and upper bound.
	 * 
	 * @param lowerBound
	 *            The smaller of two double numbers whose value is used to
	 *            represent a lighter color.
	 * @param upperBound
	 *            The larger of two double numbers whose value is used to
	 *            represent a darker color.
	 */
	public RealValuedDefaultView(double lowerBound, double upperBound)
	{
		super();

		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	/**
	 * Creates a display color based on the magnitude of the double number in
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
		double cellValue = ((Double) state.getValue()).doubleValue();

		// the scaled value between 0 and 1 (scaled by the difference between
		// the upper and lower bound)
		double scaledValueOfCell = (cellValue - this.lowerBound)
				/ (this.upperBound - this.lowerBound);

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