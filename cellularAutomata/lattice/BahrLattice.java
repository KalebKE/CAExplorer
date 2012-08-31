/*
 BahrLattice -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.CurrentProperties;
import cellularAutomata.Cell;
import cellularAutomata.rules.Rule;

/**
 * A two-dimensional square lattice with wrap around boundary conditions and a
 * nearest-neighbor neighborhood that varies with time. The wrap around
 * conditions and neighbors are implemented in the getNeighbors() method.
 * <p>
 * Consider the cells shown below. An Odd/Even neighborhood is given by cells 1,
 * 2, 8 at even time steps and cells 4, 5, 6 at odd time steps.
 * 
 * <pre>
 *                                      123
 *                                      8x4
 *                                      765
 * </pre>
 * 
 * In other words, at even time steps the neighborhood is
 * 
 * <pre>
 *                                      12
 *                                      8x
 * </pre>
 * 
 * and at odd time steps the neighborhood is
 * 
 * <pre>
 *                                       x4
 *                                       65
 * </pre>
 * 
 * @author David Bahr
 */
public class BahrLattice extends SquareLattice
{
	// Note that the following constant is public so that the PropertyReader
	// Class can set a default value for PropertyReader.LATTICE.
	/**
	 * The display name for this lattice.
	 */
	public final static String DISPLAY_NAME = "square (Odd/Even, time dep.)";

	// a hash map of neighbors at ODD time steps (a Cell[]) keyed by a cell
	// protected HashMap neighborsOdd = new HashMap();

	// an array of neighbors at ODD time steps (used for quick access)
	protected Cell[][] neighborsOdd = null;

	// The tooltip for this lattice
	private static final String TOOLTIP = "<html><body> <b>A very, very uncommon "
			+ "geometry:</b> a square lattice with an <br>"
			+ "unusual neighborhood that changes with odd and even <br>"
			+ "generations. "
			+ "<br><br>"
			+ "Consider the cells shown below. <br>"
			+ "<pre>"
			+ "                123 <br>"
			+ "                8x4 <br>"
			+ "                765 <br>"
			+ "</pre>"
			+ "An Odd/Even neighborhood is given by cells 1, 2, 8 at <br>"
			+ "even time steps and cells 4, 5, 6 at odd time steps. <br>"
			+ "In other words, at even time steps the neighborhood is <br>"
			+ "<pre>"
			+ "                12 <br>"
			+ "                8x <br>"
			+ "</pre>"
			+ "and at odd time steps the neighborhood is <br>"
			+ "<pre>"
			+ "                 x4 <br>"
			+ "                 65 <br>"
			+ "</pre>"
			+ "Note: this is not the same as a Margolus neighborhood <br>"
			+ "which creates a different neighborhood at each time <br>"
			+ "step <i>and also</i> creates a neighborhood that depends <br>"
			+ "on the cell's position.<br><br>"
			+ "To see the neighborhood associated with a cell, use the <br>"
			+ "\"Show Neighborhood\" analysis.</body><html>";

	/**
	 * Default constructor required of all Lattices (for reflection). Typically
	 * not used to build the lattice, but instead used to gain access to methods
	 * such as getDisplayName() and getNumberOfNeighbors().
	 */
	public BahrLattice()
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
	 *            remembered by the Cells on the lattice.
	 */
	public BahrLattice(String initialStateFilePath, Rule rule, int maxHistory)
	{
		super(initialStateFilePath, rule, maxHistory);

		// what kind of boundary condition?
		int boundaryType = CurrentProperties.getInstance()
				.getBoundaryCondition();

		// NOTE: The Bahr neighborhood is different at even and odd time
		// steps. The super constructor uses the method getNeighboringCells() to
		// find the neighbors at even time steps. The result is stored in the
		// hashtable "neighbors". Below, this constructor uses the method
		// getOtherNeighboringCells() to find the neighbors at odd time steps.
		// The result is stored in the hashtable "neighbors2".

		// for fast access, the following finds every cell's neighbors in
		// advance and stores them in an array. Note that this has to be a
		// different loop from the above because I have not yet instantiated
		// every cell in the previous loop.
		int numCols = super.getWidth();
		int numRows = super.getHeight();
		neighborsOdd = new Cell[numRows * numCols][];
		for(int i = 0; i < super.getHeight(); i++)
		{
			for(int j = 0; j < super.getWidth(); j++)
			{
				neighborsOdd[i * numCols + j] = getOtherNeighboringCells(i, j,
						boundaryType);

				// hashmap(key, value) as (cell, neighbors)
				// neighborsOdd.put(cells[i][j], getOtherNeighboringCells(i, j,
				// boundaryType));
			}
		}
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
	 * For EVEN time steps: On a square lattice, finds the nearest neighbors to
	 * a cell at the specified row and col position.
	 * <p>
	 * Consider the cells shown below. A Bahr neighborhood is given by cells 1,
	 * 2, 8 at even time steps and cells 4, 5, 6 at odd time steps.
	 * 
	 * <pre>
	 *                                     123
	 *                                     8x4
	 *                                     765
	 * </pre>
	 * 
	 * This method gets the neighbors for the even time step
	 * 
	 * <pre>
	 *                                     12
	 *                                     8x
	 * </pre>
	 * 
	 * The super class constructor stores these neighbors in a hashtable for
	 * fast access (called neighbors).
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
		int up = ((row - 1) + numRows) % numRows;

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

			// are we going off the top of the grid?
			if(row - 1 < 0)
			{
				// then reflect back down (i.e., the neighbor up above is
				// actually the neighbor down below)
				up = (row + 1) % numRows;
			}
		}

		// the nearest neighbors, excluding itself
		Cell[] neighboringCells = {cells[up][left], cells[up][col],
				cells[row][left]};

		return neighboringCells;
	}

	/**
	 * For ODD time steps: On a square lattice, finds the nearest neighbors to a
	 * cell at the specified row and col position.
	 * <p>
	 * Consider the cells shown below. A Bahr neighborhood is given by cells 1,
	 * 2, 3 at even time steps and cells 4, 5, 6 at odd time steps.
	 * 
	 * <pre>
	 *                                     123
	 *                                     8x4
	 *                                     765
	 * </pre>
	 * 
	 * This method gets the neighbors for the odd time step
	 * 
	 * <pre>
	 *                                      x4
	 *                                      65
	 * </pre>
	 * 
	 * The constructor stores these neighbors in a hashtable for
	 * fast access (called neighborsOdd).
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
	protected Cell[] getOtherNeighboringCells(int row, int col, int boundaryType)
	{
		// get the total number of rows and columns from the super class
		int numRows = getHeight();
		int numCols = getWidth();

		// the default wrap-around boundary (usually used)
		// no negative indices (add arrayLength)
		int right = (col + 1) % numCols;
		int down = (row + 1) % numRows;

		// use a different boundary if requested (e.g., reflection)
		if(boundaryType == Lattice.REFLECTION_BOUNDARY)
		{
			// are we going off the right side of the grid?
			if(col + 1 >= numCols)
			{
				// then reflect back to the left (i.e., right neighbor is
				// actually the left neighbor)
				right = (col - 1) % numCols;
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
		Cell[] neighboringCells = {cells[row][right], cells[down][right],
				cells[down][col]};

		return neighboringCells;
	}

	/**
	 * Returns an array of all cells that are connected to the specified cell,
	 * excluding itself.
	 * <p>
	 * Consider the cells shown below. A Bahr neighborhood is given by cells 1,
	 * 2, 3 at even time steps and cells 4, 5, 6 at odd time steps.
	 * 
	 * <pre>
	 *                                      123
	 *                                      8x4
	 *                                      765
	 * </pre>
	 * 
	 * In other words, at even time steps the neighborhood is
	 * 
	 * <pre>
	 *                                      12
	 *                                      8x
	 * </pre>
	 * 
	 * and at odd time steps the neighborhood is
	 * 
	 * <pre>
	 *                                       x4
	 *                                       65
	 * </pre>
	 * 
	 * @param cell
	 *            The specified cell.
	 * @return The neighbors of the specified cell.
	 */
	public Cell[] getNeighbors(Cell cell)
	{
		// the cell's current generation
		int generation = cell.getGeneration();

		// the array of neighbors
		Cell[] neighboringCells = null;

		// get the time dependent array of neighbors
		if(generation % 2 == 0)
		{
			// fast access to the neighbors provided by the array
			neighboringCells = neighbors[cell.getCoordinate().getRow()
					* super.getWidth() + cell.getCoordinate().getColumn()];

			// fast access to the neighbors provided by the hash map
			// neighboringCells = (Cell[]) neighbors.get(cell);
		}
		else
		{
			// fast access to the neighbors provided by the array
			neighboringCells = neighborsOdd[cell.getCoordinate().getRow()
					* super.getWidth() + cell.getCoordinate().getColumn()];
			// fast access to the neighbors provided by the hash map
			// neighboringCells = (Cell[]) neighborsOdd.get(cell);
		}

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
		return 3;
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
}
