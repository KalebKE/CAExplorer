/*
 OneDimensionalLatticeView -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.lattice.view;

import java.awt.Color;
import java.util.Iterator;

import cellularAutomata.CurrentProperties;
import cellularAutomata.Cell;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.OneDimensionalLattice;
import cellularAutomata.util.dataStructures.FiniteArrayList;

/*
 * This class is used by the slice analysis. Because it never has to rewind, the
 * "redraw" method is handled differently.
 */

/**
 * Creates the display area for the slice analysis. Essentially a
 * OneDimensionalLatticeView but overrides the redraw() and drawLattice()
 * methods.
 * 
 * @author David Bahr
 */
public class SliceAnalysisOneDimView extends OneDimensionalLatticeView
{
	// the row where the slice (lattice) will be drawn
	private int rowToDraw = -1;

	// how many generations to average when displaying the cell's values
	private int runningAverage = 1;

	/**
	 * Creates a panel to display the slice.
	 * 
	 * @param lattice
	 *            The one-dimensional lattice.
	 */
	public SliceAnalysisOneDimView(OneDimensionalLattice lattice)
	{
		super(lattice);

		// the number of generations that will be averaged for display
		runningAverage = CurrentProperties.getInstance().getRunningAverage();
	}

	/**
	 * Gets the color of a state. If the user has selected "average", then this
	 * will be the average color of many states and or state histories.
	 * <p>
	 * Note that this overrides the child class to ensure that tagged cells are
	 * not drawn.
	 * 
	 * @return The color.
	 */
	protected Color getColor(Cell cell, int row, int col)
	{
		// The color we will return
		Color color = null;

		// get the history of states for the cell
		FiniteArrayList<CellState> history = cell.getStateHistory();
		int historyLength = history.size();

		// get the running average
		try
		{
			// don't bother with the average if the cell is tagged
			// if(!cell.isTagged() && runningAverage > 1 && historyLength > 1)
			if(runningAverage > 1 && historyLength > 1)
			{
				// the number of colors averaged together
				int numColors = 0;

				double red = 0.0;
				double green = 0.0;
				double blue = 0.0;

				int lastRowToAverage = row - runningAverage;
				if(lastRowToAverage < 0)// && !redrawing)
				{
					row = historyLength - 1;
				}

				for(int n = row; (n > lastRowToAverage) && (n >= 0); n--)
				{
					Color stateColor = null;
					CellState state = history.get(n);
					if(state != null)
					{
						stateColor = Cell.getView().getUntaggedColor(state,
								null, cell.getCoordinate());
					}
					else
					{
						// Get here if that cell isn't saving the state for that
						// generation. Give it a default.
						stateColor = ColorScheme.EMPTY_COLOR;
					}
					red += stateColor.getRed();
					green += stateColor.getGreen();
					blue += stateColor.getBlue();

					numColors++;
				}

				// The state history may not be old enough to have
				// runningAverage, so divide by numColors
				red /= (double) numColors;
				green /= (double) numColors;
				blue /= (double) numColors;
				color = new Color((int) Math.round(red), (int) Math
						.round(green), (int) Math.round(blue));
			}
			else
			{
				CellState cellState = history.get(row);

				if(cellState != null)
				{
					color = Cell.getView().getUntaggedColor(cellState,
							null, cell.getCoordinate());
				}
				else
				{
					// Get here if that cell isn't saving the state for that
					// generation. Give it a default of the empty color.
					color = ColorScheme.EMPTY_COLOR;
				}
			}
		}
		catch(Exception e)
		{
			// there was an error, so set the color to empty.
			color = ColorScheme.EMPTY_COLOR;
		}

		return color;
	}

	/**
	 * Add a picture to the background image. This method is called by CAFrame.
	 * 
	 * @param lattice
	 *            The CA lattice.
	 */
	public void drawLattice(Lattice lattice)
	{
		rowToDraw++;

		// Translate the image up one row, before drawing the
		// new row. Only check this once per row.
		if(rowToDraw >= getNumRows())
		{
			translateUp();
		}

		// get the array of cells from the lattice
		Iterator iterator = lattice.iterator();
		for(int col = 0; col < getNumColumns(); col++)
		{
			// this cell[col]
			Cell cell = (Cell) iterator.next();
			if(rowToDraw < getNumRows())
			{
				// draw the new row
				drawSingleCell(rowToDraw, col,
						cell.getStateHistory().size() - 1, cell);
			}
			else
			{
				int displayRow = getNumRows() - 1;

				drawSingleCellWithErase(displayRow, col, cell.getStateHistory()
						.size() - 1, cell);
			}
		}

		// draw grid lines
		// drawGrid();
	}

	/**
	 * Redraws the lattice from the top.
	 */
	public void redraw()
	{
		rowToDraw = -1;
	}
}