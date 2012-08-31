/*
 ColorVectorView -- a class within the Cellular Automaton Explorer. 
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
import cellularAutomata.util.Coordinate;

/**
 * @author David Bahr
 */
public class ColorVectorView extends CellStateView
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
	 * Assumes that the cell state is an array of size three (an
	 * IntegerVectorState) where each element is an integer between 0 and 255.
	 * This triplet is converted to a Color.
	 * 
	 * @param state
	 *            An IntegerVectorState storing a vector of length three.
	 * @param numStates
	 *            If relevant, the number of possible states (which may not be
	 *            the same as the currently active number of states) -- may be
	 *            null which indicates that the number of states is inapplicable
	 *            or that the currently active number of states should be used.
	 *            (See for example, createProbabilityChoosers() method in
	 *            InitialStatesPanel class.)
	 * @param rowAndCol
	 *            The row and col of the cell being displayed. May be ignored.
	 * @return A color based on the triplet of integers in the provided state.
	 * @see cellularAutomata.cellState.view.CellStateView#getColor(
	 *      cellularAutomata.cellState.model.CellState, Integer, Coordinate)
	 */
	public Color getColor(CellState state, Integer numStates,
			Coordinate rowAndCol)
	{
		int[] triplet = (int[]) ((IntegerVectorState) state).getValue();

		return new Color(triplet[0], triplet[1], triplet[2]);
	}

	/**
	 * @see cellularAutomata.cellState.view.CellStateView#getDisplayShape(
	 *      cellularAutomata.cellState.model.CellState, int, int, Coordinate)
	 */
	public Shape getDisplayShape(CellState state, int width, int height,
			Coordinate rowAndCol)
	{
		return null;
	}
}
