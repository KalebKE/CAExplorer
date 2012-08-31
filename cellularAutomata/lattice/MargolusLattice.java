/*
 MargolusLattice -- a class within the Cellular Automaton Explorer. 
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

import java.util.HashMap;

import cellularAutomata.CurrentProperties;
import cellularAutomata.Cell;
import cellularAutomata.rules.Rule;

/**
 * A two-dimensional square lattice with wrap around boundary conditions and a
 * nearest-neighbor neighborhood that varies with time and position. The wrap
 * around conditions and neighbors are implemented in the getNeighbors() method.
 * <p>
 * The Margolus neighborhood divides the lattice into adjacent 2 by 2 blocks.
 * Each cell in a block can only see the other cells in the block as a neighbor.
 * At the next time step, the blocks shift down and to the right by one cell.
 * <p>
 * So for example, consider the cells shown below.
 * 
 * <pre>
 *                                              0123
 *                                              4567
 *                                              89ab
 *                                              cdef
 * </pre>
 * 
 * At even time steps: The Margolus neighborhood for cell 0 is 0, 1, 4, and 5.
 * The Margolus neighborhood for cell 1 is 0, 1, 4, and 5. The Margolus
 * neighborhood for cell 4 is 0, 1, 4, and 5. And the Margolus neighborhood for
 * cell 5 is 0, 1, 4, and 5. In other words, all the cells in the 2 by 2 block
 * 
 * <pre>
 *                                             01
 *                                             45
 * </pre>
 * 
 * see only each other as their neighbors. Similarly the blocks
 * 
 * <pre>
 *                                             23
 *                                             67
 * </pre>
 * 
 * and
 * 
 * <pre>
 *                                             89
 *                                             cd
 * </pre>
 * 
 * and
 * 
 * <pre>
 *                                             ab
 *                                             ef
 * </pre>
 * 
 * are self-contained neighborhoods. Each cell in these blocks can only see
 * other cells in the same block."
 * <p>
 * At odd time steps the neighborhoods shift down and right. Now
 * 
 * <pre>
 *                                             56
 *                                             9a
 * </pre>
 * 
 * is a neighborhood.
 * 
 * @author David Bahr
 */
public class MargolusLattice extends SquareLattice
{
	/**
	 * Indicates the northwest position of the Margolus neighborhood. (Can be
	 * used to find the northwest value of an array representing the Margolus
	 * neighborhood. For example, array[NORTHWEST].)
	 */
	public static final int NORTHWEST = 0;

	/**
	 * Indicates the northeast position of the Margolus neighborhood. (Can be
	 * used to find the northeast value of an array representing the Margolus
	 * neighborhood. For example, array[NORTHEAST].)
	 */
	public static final int NORTHEAST = 1;

	/**
	 * Indicates the southeast position of the Margolus neighborhood. (Can be
	 * used to find the southeast value of an array representing the Margolus
	 * neighborhood. For example, array[SOUTHEAST].)
	 */
	public static final int SOUTHEAST = 2;

	/**
	 * Indicates the southwest position of the Margolus neighborhood. (Can be
	 * used to find the southwest value of an array representing the Margolus
	 * neighborhood. For example, array[SOUTHWEST].)
	 */
	public static final int SOUTHWEST = 3;

	// Note that the following constant is public so that the PropertyReader
	// Class can set a default value for PropertyReader.LATTICE.
	/**
	 * The display name for this lattice.
	 */
	public final static String DISPLAY_NAME = "square (Margolus)";

	// a hash map of neighbors at ODD time steps (a Cell[]) keyed by a cell
	// protected HashMap neighborsOdd = new HashMap();

	// an array of neighbors at ODD time steps (used for quick access)
	protected Cell[][] neighborsOdd = null;

	// a hash map of the cell's position at ODD time steps (an Integer
	// representing NORTHWEST, NORTHEAST, SOUTHEAST, or SOUTHWEST) keyed by a
	// cell
	protected HashMap<Cell, Integer> cellPositionOddTime = new HashMap<Cell, Integer>();

	// The tooltip for this lattice
	private static final String TOOLTIP = "<html><body><b>An unusual and uncommon geometry:</b> a square "
			+ "lattice with a neighborhood <br> "
			+ "that changes with both position and time. <b>Works best with an even number<br>"
			+ "of rows and columns</b>."
			+ "<br><br>"
			+ "The Margolus neighborhood partitions the lattice into non-overlapping adjacent <br>"
			+ "2 by 2 blocks. When picking neighbors, each cell in a block can only see the <br>"
			+ "other cells in the same block. All cells outside the block are inaccessible <br>"
			+ "and cannot be neighbors. This limited view prevents interactions between <br>"
			+ "blocks.  Therefore, to exchange information, at every time step the blocks <br>"
			+ "shift down and to the right by one cell. "
			+ "<br><br>"
			+ "So for example, consider the cells shown below. <br>"
			+ "<pre>"
			+ "                                   ------- <br>"
			+ "        0123                       |01|23| <br>"
			+ "        4567                       |45|67| <br>"
			+ "        89ab    partitioned as     ------- <br>"
			+ "        cdef                       |89|ab| <br>"
			+ "                                   |cd|ef| <br>"
			+ "                                   ------- <br>"
			+ "</pre>"
			+ "At even time steps, the Margolus neighborhood for cell 0 is 0, 1, 4, and 5. <br>"
			+ "The Margolus neighborhood for cell 1 is 0, 1, 4, and 5. Ditto for cells 4 and <br>"
			+ "5. In other words, all the cells in the 2 by 2 block <br>"
			+ "<pre>"
			+ "                            01 <br>"
			+ "                            45 <br>"
			+ "</pre>"
			+ "see only each other as their neighbors. Similarly, the blocks <br>"
			+ "<pre>"
			+ "                23           89           ab<br>"
			+ "                67    and    cd    and    ef<br>"
			+ "</pre>"
			+ "are self-contained neighborhoods.  Each cell in these blocks can only see other <br>"
			+ "cells in the same block."
			+ "<br><br>"
			+ "At odd time steps, the neighborhoods shift down and right. Now the same cells <br>"
			+ "are partitioned as "
			+ "<pre>"
			+ "                                    <br>"
			+ "        0123                       0|12|3 <br>"
			+ "        4567                       ------- <br>"
			+ "        89ab    partitioned as     4|56|7 <br>"
			+ "        cdef                       8|9a|b <br>"
			+ "                                   ------- <br>"
			+ "                                   c|de|f <br>"
			+ "</pre>"
			+ "Therefore 5, 6, 9, and \"a\" are a neighborhood. Etc. "
			+ "<br><br>"
			+ "The Margolus neighborhood is useful for reversible rules and rules that need <br>"
			+ "to obey physical conservation principles.  However, it seems to be arbitrarily <br>"
			+ "imposed on the natural geometry of the lattice (as seen from the cell's point of <br>"
			+ "view).  Fortunately, every rule on a Margolus neighborhood can always be simulated <br>"
			+ "with a Moore neighborhood that has additional states. See <i>Cellular Automata <br>"
			+ "Machines</i> by Toffoli and Margolus for more details (particularly pp. 119-126). "
			+ "<br><br>"
			+ "Note that reflection boundaries are ill-defined and have no effect on Margolus <br>"
			+ "lattices. </body><html>";

	/**
	 * Default constructor required of all Lattices (for reflection). Typically
	 * not used to build the lattice, but instead used to gain access to methods
	 * such as getDisplayName() and getNumberOfNeighbors().
	 */
	public MargolusLattice()
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
	public MargolusLattice(String initialStateFilePath, Rule rule,
			int maxHistory)
	{
		super(initialStateFilePath, rule, maxHistory);

		// what kind of boundary condition?
		int boundaryType = CurrentProperties.getInstance()
				.getBoundaryCondition();

		// NOTE: The Margolus neighborhood is different at even and odd time
		// steps. The super constructor uses the method getNeighboringCells() to
		// find the neighbors at even time steps. The result is stored in the
		// hashtable "neighbors". Below, this constructor uses the method
		// getOddTimeStepNeighboringCells() to find the neighbors at odd time
		// steps. The result is stored in the hashtable "neighbors2".

		// for fast access, the following finds every cell's neighbors in
		// advance and stores them in an array. Note that this has to be a
		// different loop from the above because I have not yet instantiated
		// every cell in the previous loop.
		int numCols = super.getWidth();
		int numRows = super.getHeight();
		neighborsOdd = new Cell[numRows * numCols][];
		for(int i = 0; i < numRows; i++)
		{
			for(int j = 0; j < numCols; j++)
			{
				neighborsOdd[i * numCols + j] = getOddTimeStepNeighboringCells(
						i, j, boundaryType);

				// hashmap(key, value) as (cell, neighbors)
				// neighborsOdd.put(cells[i][j],
				// getOddTimeStepNeighboringCells(i,
				// j, boundaryType));
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
	 * Converts a cell's position at an odd (or even) time to the cell's
	 * position at an even (or odd) time.
	 * 
	 * @param currentPosition
	 *            The position of the cell at an odd (or even) time step.
	 * @return The position of the cell at an even (or odd) time step.
	 */
	private Integer getPositionAtNextTimeStep(Integer currentPosition)
	{
		// converts a cell from its position at an odd (even) time to its
		// position at an even (odd) time
		return new Integer((currentPosition.intValue() + 2) % 4);
	}

	/**
	 * Gets the Margolus neighbors at even time steps. (Also sets the cell's
	 * position at even time steps. For example, NORTHWEST, NORTHEAST,
	 * SOUTHEAST, SOUTHWEST.)
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
		// the nearest neighbors, excluding itself
		Cell[] neighboringCells = new Cell[3];

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
		// if(boundaryType == Lattice.REFLECTION_BOUNDARY)
		// {
		// // are we going off the left side of the grid?
		// if(col - 1 < 0)
		// {
		// // then reflect back to the right (i.e., left neighbor is
		// // actually the right neighbor)
		// left = (col + 1) % numCols;
		// }
		//
		// // are we going off the right side of the grid?
		// if(col + 1 >= numCols)
		// {
		// // then reflect back to the left (i.e., right neighbor is
		// // actually the left neighbor)
		// right = (col - 1) % numCols;
		// }
		//
		// // are we going off the top of the grid?
		// if(row - 1 < 0)
		// {
		// // then reflect back down (i.e., the neighbor up above is
		// // actually the neighbor down below)
		// up = (row + 1) % numRows;
		// }
		//
		// // are we going off the bottom of the grid?
		// if(row + 1 >= numRows)
		// {
		// // then reflect back up (i.e., the neighbor down below is
		// // actually the neighbor up above)
		// down = (row - 1) % numRows;
		// }
		// }

		// find out if a cell is in the NW, NE, SE, or SW corner of the 2 by 2
		// block. (FOR EVEN TIME STEPS)
		if(row % 2 == 0 && col % 2 == 0)
		{
			// it's the NW, so the nearest neighbors, excluding itself
			neighboringCells[0] = cells[row][right];
			neighboringCells[1] = cells[down][right];
			neighboringCells[2] = cells[down][col];
		}
		else if(row % 2 == 0 && col % 2 == 1)
		{
			// it's the NE, so the nearest neighbors, excluding itself
			neighboringCells[0] = cells[row][left];
			neighboringCells[1] = cells[down][col];
			neighboringCells[2] = cells[down][left];
		}
		else if(row % 2 == 1 && col % 2 == 0)
		{
			// it's the SW, so the nearest neighbors, excluding itself
			neighboringCells[0] = cells[up][col];
			neighboringCells[1] = cells[up][right];
			neighboringCells[2] = cells[row][right];
		}
		else if(row % 2 == 1 && col % 2 == 1)
		{
			// it's the SE, so the nearest neighbors, excluding itself
			neighboringCells[0] = cells[up][left];
			neighboringCells[1] = cells[up][col];
			neighboringCells[2] = cells[row][left];
		}

		return neighboringCells;
	}

	/**
	 * Gets the Margolus neighbors at odd time steps. (Also sets the cell's
	 * position at odd time steps. For example, NORTHWEST, NORTHEAST, SOUTHEAST,
	 * SOUTHWEST.)
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
	protected Cell[] getOddTimeStepNeighboringCells(int row, int col,
			int boundaryType)
	{
		// the nearest neighbors, excluding itself
		Cell[] neighboringCells = new Cell[3];

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
		// if(boundaryType == Lattice.REFLECTION_BOUNDARY)
		// {
		// // are we going off the left side of the grid?
		// if(col - 1 < 0)
		// {
		// // then reflect back to the right (i.e., left neighbor is
		// // actually the right neighbor)
		// left = (col + 1) % numCols;
		// }
		//
		// // are we going off the right side of the grid?
		// if(col + 1 >= numCols)
		// {
		// // then reflect back to the left (i.e., right neighbor is
		// // actually the left neighbor)
		// right = (col - 1) % numCols;
		// }
		//
		// // are we going off the top of the grid?
		// if(row - 1 < 0)
		// {
		// // then reflect back down (i.e., the neighbor up above is
		// // actually the neighbor down below)
		// up = (row + 1) % numRows;
		// }
		//
		// // are we going off the bottom of the grid?
		// if(row + 1 >= numRows)
		// {
		// // then reflect back up (i.e., the neighbor down below is
		// // actually the neighbor up above)
		// down = (row - 1) % numRows;
		// }
		// }

		// find out if a cell is in the NW, NE, SE, or SW corner of the 2 by 2
		// block. (FOR ODD TIME STEPS)
		if(row % 2 == 1 && col % 2 == 1)
		{
			// it's the NW, so the nearest neighbors, excluding itself
			neighboringCells[0] = cells[row][right];
			neighboringCells[1] = cells[down][right];
			neighboringCells[2] = cells[down][col];

			// keep track (in a hash map) of the fact that at ODD time steps
			// this cell is in the NORTHWEST corner of the Margolus
			// neighborhood.
			cellPositionOddTime.put(cells[row][col], new Integer(NORTHWEST));
		}
		else if(row % 2 == 1 && col % 2 == 0)
		{
			// it's the NE, so the nearest neighbors, excluding itself
			neighboringCells[0] = cells[row][left];
			neighboringCells[1] = cells[down][col];
			neighboringCells[2] = cells[down][left];

			// keep track (in a hash map) of the fact that at ODD time steps
			// this cell is in the NORTHEAST corner of the Margolus
			// neighborhood.
			cellPositionOddTime.put(cells[row][col], new Integer(NORTHEAST));
		}
		else if(row % 2 == 0 && col % 2 == 1)
		{
			// it's the SW, so the nearest neighbors, excluding itself
			neighboringCells[0] = cells[up][col];
			neighboringCells[1] = cells[up][right];
			neighboringCells[2] = cells[row][right];

			// keep track (in a hash map) of the fact that at ODD time steps
			// this cell is in the SOUTHWEST corner of the Margolus
			// neighborhood.
			cellPositionOddTime.put(cells[row][col], new Integer(SOUTHWEST));
		}
		else if(row % 2 == 0 && col % 2 == 0)
		{
			// it's the SE, so the nearest neighbors, excluding itself
			neighboringCells[0] = cells[up][left];
			neighboringCells[1] = cells[up][col];
			neighboringCells[2] = cells[row][left];

			// keep track (in a hash map) of the fact that at ODD time steps
			// this cell is in the SOUTHEAST corner of the Margolus
			// neighborhood.
			cellPositionOddTime.put(cells[row][col], new Integer(SOUTHEAST));
		}

		return neighboringCells;
	}

	/**
	 * Returns an array of all cells that are connected to the specified cell,
	 * excluding itself.
	 * <p>
	 * The Margolus neighborhood divides the lattice into adjacent 2 by 2
	 * blocks. Each cell in a block can only see the other cells in the block as
	 * a neighbor. At the next time step, the blocks shift down and to the right
	 * by one cell.
	 * <p>
	 * So for example, consider the cells shown below.
	 * 
	 * <pre>
	 *                                              0123
	 *                                              4567
	 *                                              89ab
	 *                                              cdef
	 * </pre>
	 * 
	 * At even time steps: The Margolus neighborhood for cell 0 is 0, 1, 4, and
	 * 5. The Margolus neighborhood for cell 1 is 0, 1, 4, and 5. The Margolus
	 * neighborhood for cell 4 is 0, 1, 4, and 5. And the Margolus neighborhood
	 * for cell 5 is 0, 1, 4, and 5. In other words, all the cells in the 2 by 2
	 * block
	 * 
	 * <pre>
	 *                                             01
	 *                                             45
	 * </pre>
	 * 
	 * see only each other as their neighbors. Similarly the blocks
	 * 
	 * <pre>
	 *                                             23
	 *                                             67
	 * </pre>
	 * 
	 * and
	 * 
	 * <pre>
	 *                                             89
	 *                                             cd
	 * </pre>
	 * 
	 * and
	 * 
	 * <pre>
	 *                                             ab
	 *                                             ef
	 * </pre>
	 * 
	 * are self-contained neighborhoods. Each cell in these blocks can only see
	 * other cells in the same block."
	 * <p>
	 * At odd time steps the neighborhoods shift down and right. Now
	 * 
	 * <pre>
	 *                                             56
	 *                                             9a
	 * </pre>
	 * 
	 * is a neighborhood.
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

			// stores the position of the cell on the Margolus lattice
			// (NORTHEAST, NORTHWEST, SOUTHEAST, or SOUTHWEST) and uses the
			// DISPLAY_NAME of the lattice as a key.
			cell.setOtherCellInformation(DISPLAY_NAME,
					getPositionAtNextTimeStep((Integer) cellPositionOddTime
							.get(cell)));
		}
		else
		{
			// fast access to the neighbors provided by the array
			neighboringCells = neighborsOdd[cell.getCoordinate().getRow()
					* super.getWidth() + cell.getCoordinate().getColumn()];

			// fast access to the neighbors provided by the hash map
			// neighboringCells = (Cell[]) neighborsOdd.get(cell);

			// stores the position of the cell on the Margolus lattice
			// (NORTHEAST, NORTHWEST, SOUTHEAST, or SOUTHWEST) and uses the
			// DISPLAY_NAME of the lattice as a key.
			cell.setOtherCellInformation(DISPLAY_NAME, cellPositionOddTime
					.get(cell));
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
