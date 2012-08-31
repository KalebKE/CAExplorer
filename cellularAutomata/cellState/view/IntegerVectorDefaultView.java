/*
 IntegerVectorDefaultView -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.IntegerVectorState;
import cellularAutomata.rules.IntegerRule;
import cellularAutomata.graphics.colors.ColorTools;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.math.BaseConverter;

/**
 * Creates graphics to display the IntegerVectorState. The default shapes for
 * the lattice are used (e.g., square for a square lattice), and colors are
 * determined by averaging the values of the vector.
 * <p>
 * Also see IntegerVectorArrowView.
 * 
 * @author David Bahr
 */
public class IntegerVectorDefaultView extends CellStateView
{
	/**
	 * @see cellularAutomata.cellState.view.CellStateView#getAverageDisplayShape(
	 *      cellularAutomata.cellState.model.CellState[], int, int, Coordinate)
	 */
	public Shape getAverageDisplayShape(CellState[] states, int width,
			int height, Coordinate rowAndCol)
	{
		return null;
	}

	/**
	 * Get a color appropriate for displaying the cell's state.
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
		Color color = null;

		IntegerVectorState vectorState = (IntegerVectorState) state;
		int[] stateValues = (int[]) vectorState.getValue();
		int maxValue = vectorState.getMaxValue();
		int minValue = vectorState.getMinValue();

		// convert the array to an integer value by assuming that each array
		// position is part of a number in a different base. The base is given
		// by maxValue - minValue + 1.
		int baseValue = maxValue - minValue + 1;

		// what's the max possible number?
		int numPossibleStates = (int) Math.pow(baseValue, stateValues.length);

		boolean failedInBaseConversion = false;
		if(baseValue <= 36 && baseValue > 2
				&& numPossibleStates < IntegerRule.MAX_NUM_STATES)
		{
			try
			{
				String number = "";
				for(int i = 0; i < stateValues.length; i++)
				{
					// rescale to start from 0.
					int arrayValue = stateValues[i] - minValue;
					if(arrayValue > 9)
					{
						number += (char) ('a' - 10 + arrayValue);
					}
					else
					{
						number += arrayValue;
					}
				}

				long cellNumber = BaseConverter.convertToBaseTen(number,
						baseValue);

				if(cellNumber > Integer.MAX_VALUE)
				{
					failedInBaseConversion = true;
				}
				else
				{
					color = colorScheme.getColor((int) cellNumber,
							numPossibleStates, this);
				}
			}
			catch(Exception e)
			{
				failedInBaseConversion = true;
			}
		}
		else
		{
			failedInBaseConversion = true;
		}

		if(failedInBaseConversion)
		{
			// The base is too big. So just get the average value of the states.
			// Add the states and get their average value.
			double averageValue = 0.0;
			for(int i = 0; i < stateValues.length; i++)
			{
				averageValue += stateValues[i];
			}
			averageValue /= stateValues.length;

			// rescale to a value between 0 and 1
			double rescaledValue = Math.abs((averageValue - minValue)
					/ (double) (maxValue - minValue));

			color = ColorTools.getColorFromSingleValue(rescaledValue);
		}

		return color;
	}

	/**
	 * @see cellularAutomata.cellState.view.CellStateView#getDisplayShape(CellState,
	 *      int, int, Coordinate)
	 */
	public Shape getDisplayShape(CellState state, int width, int height,
			Coordinate rowAndCol)
	{
		return null;
	}
}
