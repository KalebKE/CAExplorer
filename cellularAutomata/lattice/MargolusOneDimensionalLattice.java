/*
 MargolusOneDimensionalLattice -- a class within the Cellular Automaton Explorer. 
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
import cellularAutomata.lattice.view.LatticeView;
import cellularAutomata.lattice.view.OneDimensionalLatticeView;
import cellularAutomata.rules.Rule;

/**
 * A one-dimensional version of the Margolus lattice.
 * 
 * @author David Bahr
 */
public class MargolusOneDimensionalLattice extends OneDimensionalLattice
{
	/**
	 * Indicates the west position of the Margolus neighborhood. (Can be used to
	 * find the west value of an array representing the Margolus neighborhood.
	 * For example, array[WEST].)
	 */
	public static final int WEST = 0;

	/**
	 * Indicates the east position of the Margolus neighborhood. (Can be used to
	 * find the east value of an array representing the Margolus neighborhood.
	 * For example, array[EAST].)
	 */
	public static final int EAST = 1;

	// Note that the following constant is public so that the PropertyReader
	// Class can set a default value for PropertyReader.LATTICE.
	/**
	 * The display name for this lattice.
	 */
	public final static String DISPLAY_NAME = "one-dim (Margolus)";

	// a hash map of neighbors at ODD time steps (a Cell[]) keyed by a cell
	// protected HashMap neighborsOdd = new HashMap();

	// an array of neighbors at ODD time steps (used for quick access)
	protected Cell[][] neighborsOdd = null;

	// Tells the lattice how to display its graphics.
	private LatticeView view = null;

	// The tooltip for this lattice
	private static final String TOOLTIP = "<html> <body><b>An unusual and uncommon geometry:</b> "
			+ "a one-dimensional lattice with a <br>"
			+ "neighborhood that changes with both position and time. <b>Works best with an <br>"
			+ "even number of rows and columns</b>."
			+ "<br><br>"
			+ "The Margolus neighborhood partitions the lattice into non-overlapping adjacent <br>"
			+ "blocks. When picking neighbors, each cell in a block can only see the <br>"
			+ "other cells in the same block. All cells outside the block are inaccessible <br>"
			+ "and cannot be neighbors. This limited view prevents interactions between <br>"
			+ "blocks.  Therefore, to exchange information, at every time step the blocks <br>"
			+ "shift to the left by one cell. "
			+ "<br><br>"
			+ "So for example, consider the cells shown below. <br>"
			+ "<pre>"
			+ "       01x234 <br>"
			+ "</pre>"
			+ "At even time steps, the Margolus neighborhood for cell x is 2. At odd time steps, <br>"
			+ "the neighborhoods shift left, so now the neighborhood for x is 1. <br><br>"
			+ "In other words, at even time steps the cells are arranged into blocks 01, x2, and <br>"
			+ "34.  At odd time steps the cells are arranged into blocks 1x, 23, and 41."
			+ "<br><br>"
			+ "The Margolus neighborhood is useful for reversible rules and rules that need <br>"
			+ "to obey physical conservation principles.  However, it seems to be arbitrarily <br>"
			+ "imposed on the natural geometry of the lattice (as seen from the cell's point of <br>"
			+ "view).  Fortunately, every rule on a Margolus neighborhood can always be simulated <br>"
			+ "with a Moore neighborhood that has additional states. See <i>Cellular Automata <br>"
			+ "Machines</i> by Toffoli and Margolus for more details (particularly pp. 119-126). <br><br>"
			+ "Note that reflection boundaries are ill-defined and have no effect on Margolus <br>"
			+ "lattices. </body><html>";

	/**
	 * Default constructor required of all Lattices (for reflection). Typically
	 * not used to build the lattice, but instead used to gain access to methods
	 * such as getDisplayName() and getNumberOfNeighbors().
	 */
	public MargolusOneDimensionalLattice()
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
	public MargolusOneDimensionalLattice(String initialStateFilePath,
			Rule rule, int maxHistory)
	{
		super(initialStateFilePath, rule, maxHistory);

		view = new OneDimensionalLatticeView(this);

		// what kind of boundary condition?
		int boundaryType = CurrentProperties.getInstance()
				.getBoundaryCondition();

		// NOTE: The neighborhood is different at even and odd time
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
		for(int index = 0; index < super.getWidth(); index++)
		{
			neighborsOdd[index] = getOddTimeStepNeighboringCells(index,
					boundaryType);
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
	 * For EVEN time steps: finds the nearest neighbors to a cell at the
	 * specified index position.
	 * <p>
	 * The super constructor stores these neighbors in a hashtable for fast
	 * access (called neighbors).
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
	protected Cell[] getNeighboringCells(int index, int boundaryType)
	{
		// the nearest neighbors, excluding itself
		Cell[] neighboringCells = new Cell[1];

		// get the total number of rows and columns from the super class
		int numCols = getWidth();

		// the default wrap-around boundary (usually used)
		// no negative indices (add arrayLength)
		int left = ((index - 1) + numCols) % numCols;
		int right = (index + 1) % numCols;

		// use a different boundary if requested (e.g., reflection)
		// if(boundaryType == Lattice.REFLECTION_BOUNDARY)
		// {
		// // are we going off the left side of the grid?
		// if(index - 1 < 0)
		// {
		// // then reflect back to the right (i.e., left neighbor is
		// // actually the right neighbor)
		// left = (index + 1) % numCols;
		// }
		//
		// // are we going off the right side of the grid?
		// if(index + 1 >= numCols)
		// {
		// // then reflect back to the left (i.e., right neighbor is
		// // actually the left neighbor)
		// right = (index - 1) % numCols;
		// }
		// }

		// find out if a cell is in the WEST or EAST of the two-cell
		// block. (FOR EVEN TIME STEPS)
		if(index % 2 == 0)
		{
			// it's the WEST cell, so the nearest neighbor is
			neighboringCells[0] = oneDimCells[right];
		}
		else
		{
			// it's the EAST cell, so the nearest neighbor is
			neighboringCells[0] = oneDimCells[left];
		}

		return neighboringCells;
	}

	/**
	 * For ODD time steps: finds the nearest neighbors to a cell at the
	 * specified position.
	 * <p>
	 * The constructor stores these neighbors in a hashtable for fast access
	 * (called neighborsOdd).
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
	protected Cell[] getOddTimeStepNeighboringCells(int index, int boundaryType)
	{
		// the nearest neighbors, excluding itself
		Cell[] neighboringCells = new Cell[1];

		// get the total number of rows and columns from the super class
		int numCols = getWidth();

		// the default wrap-around boundary (usually used)
		// no negative indices (add arrayLength)
		int left = ((index - 1) + numCols) % numCols;
		int right = (index + 1) % numCols;

		// use a different boundary if requested (e.g., reflection)
		// if(boundaryType == Lattice.REFLECTION_BOUNDARY)
		// {
		// // are we going off the left side of the grid?
		// if(index - 1 < 0)
		// {
		// // then reflect back to the right (i.e., left neighbor is
		// // actually the right neighbor)
		// left = (index + 1) % numCols;
		// }
		//
		// // are we going off the right side of the grid?
		// if(index + 1 >= numCols)
		// {
		// // then reflect back to the left (i.e., right neighbor is
		// // actually the left neighbor)
		// right = (index - 1) % numCols;
		// }
		// }

		// find out if a cell is in the WEST or EAST of the two-cell
		// block. (FOR ODD TIME STEPS)
		if(index % 2 == 0)
		{
			// it's the WEST cell, so the nearest neighbor is
			neighboringCells[0] = oneDimCells[left];
		}
		else
		{
			// it's the EAST cell, so the nearest neighbor is
			neighboringCells[0] = oneDimCells[right];
		}

		return neighboringCells;
	}

	/**
	 * Returns an array of all cells that are connected to the specified cell,
	 * excluding itself.
	 * <p>
	 * Consider the cells shown below. A one-dimensional Margolus neighborhood
	 * is given by cell 1 at even time steps and cells 3 at odd time steps.
	 * 
	 * <pre>
	 *                                      1x3
	 * </pre>
	 * 
	 * In other words, at even time steps the neighborhood is
	 * 
	 * <pre>
	 *                                      1x
	 * </pre>
	 * 
	 * and at odd time steps the neighborhood is
	 * 
	 * <pre>
	 * x3
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
			neighboringCells = neighbors[cell.getCoordinate().getColumn()];

			// stores the position of the cell on the Margolus lattice
			// (WEST or EAST) and uses the DISPLAY_NAME of the lattice as a key.
			if(cell.getCoordinate().getColumn() % 2 == 0)
			{
				cell.setOtherCellInformation(DISPLAY_NAME,
						MargolusOneDimensionalLattice.WEST);
			}
			else
			{
				cell.setOtherCellInformation(DISPLAY_NAME,
						MargolusOneDimensionalLattice.EAST);
			}
		}
		else
		{
			// fast access to the neighbors provided by the array
			neighboringCells = neighborsOdd[cell.getCoordinate().getColumn()];

			// stores the position of the cell on the Margolus lattice
			// (WEST or EAST) and uses the DISPLAY_NAME of the lattice as a key.
			if(cell.getCoordinate().getColumn() % 2 == 0)
			{
				cell.setOtherCellInformation(DISPLAY_NAME,
						MargolusOneDimensionalLattice.EAST);
			}
			else
			{
				cell.setOtherCellInformation(DISPLAY_NAME,
						MargolusOneDimensionalLattice.WEST);
			}
		}

		return neighboringCells;

		// // the cell's current generation
		// int generation = cell.getGeneration();
		//
		// // the array of neighbors
		// Cell[] neighboringCells = null;
		//
		// // get the time dependent array of neighbors
		// if(generation % 2 == 0)
		// {
		// // fast access to the neighbors provided by the array
		// neighboringCells = neighbors[cell.getCoordinate().getColumn()];
		// }
		// else
		// {
		// // fast access to the neighbors provided by the array
		// neighboringCells = neighborsOdd[cell.getCoordinate().getColumn()];
		// }
		//
		// return neighboringCells;
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
		return 1;
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
