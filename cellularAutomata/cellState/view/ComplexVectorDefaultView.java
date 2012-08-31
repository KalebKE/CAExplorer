/*
 ComplexVectorDefaultView -- a class within the Cellular Automaton Explorer. 
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
import cellularAutomata.cellState.model.ComplexValuedVectorState;
import cellularAutomata.cellState.view.TriangleHexagonCellStateView;
import cellularAutomata.graphics.colors.ColorTools;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.math.Complex;

/**
 * Creates graphics to display the ComplexValuedVectorState. The appropriate
 * shapes for the lattice are used (e.g., square for a square lattice, triangles
 * for the triangular lattice), and colors are determined by averaging the
 * values of the vector.
 * <p>
 * Also see IntegerVectorArrowView.
 * 
 * @author David Bahr
 */
public class ComplexVectorDefaultView extends TriangleHexagonCellStateView
{
	// The value of the modulus of the complex number that will be displayed as
	// the full color.
	private double maxValue = 1.0;

	// The value of the modulus of the complex number that will be displayed as
	// the empty color.
	private double minValue = 0.0;

	/**
	 * Create this view. Colors are set to vary as shades between the empty and
	 * the full colors. Parameters indicate what values (of the complex modulus)
	 * should be associated with the min and the max. Values outside this min
	 * and max are colored as empty or full respectively. The value (to be
	 * colored) is the average of the complex numbers in the vector.
	 * 
	 * @param minValue
	 *            The value of the modulus of the complex number that will be
	 *            displayed as the empty color. Should be greater than or equal
	 *            to 0.0.
	 * @param maxValue
	 *            The value of the modulus of the complex number that will be
	 *            displayed as the full color.
	 */
	public ComplexVectorDefaultView(double minValue, double maxValue)
	{
		super();

		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	/**
	 * Get the average of the complex numbers in the array. In other words, get
	 * the average of the real and imaginary components.
	 * 
	 * @param complexArray
	 *            The array from which the average will be calculated.
	 * @return The average complex number.
	 */
	private Complex getVectorAverage(Complex[] complexArray)
	{
		// add the elements of the array
		Complex averageValue = new Complex(0.0, 0.0);
		for(int i = 0; i < complexArray.length; i++)
		{
			averageValue = Complex.plus(averageValue, complexArray[i]);
		}

		// now divide by the length of the array to get the average value
		averageValue = Complex.divide(averageValue, new Complex(
				complexArray.length, 0.0));

		return averageValue;
	}

	/**
	 * Get a color appropriate for displaying the cell state.
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
	public Color getColor(CellState state, Integer numStates, Coordinate rowAndCol)
	{
		ComplexValuedVectorState vectorState = (ComplexValuedVectorState) state;
		Complex[] stateValues = (Complex[]) vectorState.getValue();

		// get the average of the states
		Complex averageValue = getVectorAverage(stateValues);

		// get the modulus
		double modulus = averageValue.modulus();

		// rescale to a value between 0 and 1
		double rescaledValue = Math.abs((modulus - minValue)
				/ (maxValue - minValue));

		if(rescaledValue > 1.0)
		{
			rescaledValue = 1.0;
		}
		else if(rescaledValue < 0.0)
		{
			rescaledValue = 0.0;
		}

		return ColorTools.getColorFromSingleValue(rescaledValue);
	}
}
