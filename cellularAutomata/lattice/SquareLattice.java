/*
 SquareLattice -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.lattice;

import cellularAutomata.Cell;
import cellularAutomata.rules.Rule;
import cellularAutomata.lattice.view.LatticeView;
import cellularAutomata.lattice.view.SquareLatticeView;

/**
 * A simple two-dimensional square lattice with eight nearest neighbors (Moore
 * neighborhood). The wrap around/reflection boundary conditions and choice of
 * neighbors are implemented in the getNeighbors() method.
 * 
 * @author David Bahr
 */
public class SquareLattice extends TwoDimensionalLattice
{
	// Note that the following constant is public so that the PropertyReader
	// Class can set a default value for PropertyReader.LATTICE.
	/**
	 * The display name for this lattice.
	 */
	public final static String DISPLAY_NAME = "square (8 neighbor)";

	// Tells the lattice how to display its graphics.
	private LatticeView view = null;

	// The tooltip for this lattice
	private static final String TOOLTIP = "<html><body> <b>The most common geometry:</b> "
			+ "a square lattice <br>"
			+ "with all 8 nearest neighbors.  Called a Moore <br>"
			+ "neighborhood. "
			+ "<br><br>"
			+ "Consider the cells below with central cell x. <br>"
			+ "<pre>"
			+ "                123 <br>"
			+ "                8x4 <br>"
			+ "                765 <br>"
			+ "</pre>"
			+ "The Moore neighborhood includes all surrounding <br>"
			+ "cells 1 through 8. <br><br>"
			+ "To see the neighborhood associated with a cell, use the <br>"
			+ "\"Show Neighborhood\" analysis.</body></html>";

	/**
	 * Default constructor required of all Lattices (for reflection). Typically
	 * not used to build the lattice, but instead used to gain access to methods
	 * such as getDisplayName() and getNumberOfNeighbors().
	 */
	public SquareLattice()
	{
		super();
	}

	/**
	 * Create a two-dimensional cellular automaton with the same rule at all
	 * positions. The height and width of the automaton and the initial state of
	 * each cell is specified in a file.
	 * 
	 * @param rule
	 *            The rule applied to all cells.
	 * @param initialStateFilePath
	 *            The path to the file that specifies the initial state of the
	 *            cellular automaton. If null, uses default initial state.
	 * @param maxHistory
	 *            The maximum number of generations (or time steps) that will be
	 *            remembered by the cells on the lattice.
	 */
	public SquareLattice(String initialStateFilePath, Rule rule, int maxHistory)
	{
		super(initialStateFilePath, rule, maxHistory);

		view = new SquareLatticeView(this);
	}

	/**
	 * A brief one or two-word string describing the rule, appropriate for
	 * display in a drop-down list.
	 * 
	 * @return A string no longer than 25 characters.
	 */
	public String getDisplayName()
	{
		return DISPLAY_NAME;
	}

	/**
	 * On a square lattice, finds the nearest neighbors to a cell at the
	 * specified row and col position.
	 * 
	 * @param row
	 *            The cell's vertical position in the array.
	 * @param col
	 *            The cell's horizontal position in the array.
	 * @param boundaryType
	 *            A constant indicating the type of boundary (wrap-around,
	 *            reflection, etc). Acceptable constants are specified in the
	 *            Lattice class.
	 * @return An array of neighboring cells.
	 */
	protected Cell[] getNeighboringCells(int row, int col, int boundaryType)
	{
		// get the total number of rows and columns from the super class
		int numRows = getHeight();
		int numCols = getWidth();

		// the default wrap-around boundary (usually used)
		// no negative indices (add arrayLength)
		int left = ((col - 1) + numCols) % numCols;
		int right = (col + 1) % numCols;
		int up = ((row - 1) + numRows) % numRows;
		int down = (row + 1) % numRows;

		// use a different boundary if requested (e.g., reflection)
		if(boundaryType == Lattice.REFLECTION_BOUNDARY)
		{
			// are we going off the left side of the grid?
			if(col - 1 < 0)
			{
				// then reflect back to the right (i.e., left neighbor is
				// actually the right neighbor)
				left = (col + 1) % numCols;
			}

			// are we going off the right side of the grid?
			if(col + 1 >= numCols)
			{
				// then reflect back to the left (i.e., right neighbor is
				// actually the left neighbor)
				right = (col - 1) % numCols;
			}

			// are we going off the top of the grid?
			if(row - 1 < 0)
			{
				// then reflect back down (i.e., the neighbor up above is
				// actually the neighbor down below)
				up = (row + 1) % numRows;
			}

			// are we going off the bottom of the grid?
			if(row + 1 >= numRows)
			{
				// then reflect back up (i.e., the neighbor down below is
				// actually the neighbor up above)
				down = (row - 1) % numRows;
			}
		}

		// the nearest neighbors, excluding itself
		Cell[] neighboringCells = {cells[up][left], cells[up][col],
				cells[up][right], cells[row][right], cells[down][right],
				cells[down][col], cells[down][left], cells[row][left]};

		return neighboringCells;
	}

	/**
	 * Returns the number of neighbors of a cell on the lattice (excluding the
	 * cell). If the cell's have a varying number of neighbors, then this
	 * returns -1, and the user should use <code>getNeighbors(Cell cell)</code>
	 * to find the number of neighbors for any particular cell.
	 * 
	 * @return The number of neighbors for each cell, or -1 if that number is
	 *         variable.
	 */
	public int getNumberOfNeighbors()
	{
		return 8;
	}

	/**
	 * A tooltip that describes the lattice.
	 * 
	 * @return The tooltip.
	 */
	public String getToolTipDescription()
	{
		return TOOLTIP;
	}

	/**
	 * Gets the graphics that tells the lattice how to display itself.
	 * 
	 * @return The graphics view that tells this Lattice how to display.
	 */
	public LatticeView getView()
	{
		return view;
	}

	/**
	 * Sets the graphics that tells the Lattice how to display itself. Unless a
	 * non-default is desired, the view does not have to be set by the user
	 * (because a default is always used).
	 * 
	 * @param view
	 *            The graphics view that tells this Lattice how to display.
	 */
	public void setView(LatticeView view)
	{
		this.view = view;
	}
}
