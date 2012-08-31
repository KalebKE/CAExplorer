/*
 GlobalSquareLattice -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.lattice;

import cellularAutomata.Cell;
import cellularAutomata.rules.Rule;
import cellularAutomata.lattice.view.LatticeView;
import cellularAutomata.lattice.view.SquareLatticeView;

/**
 * A simple two-dimensional square lattice with every cell being a neighbor to
 * every other cell. The wrap around/reflection boundary conditions and choice
 * of neighbors are implemented in the getNeighbors() method.
 * 
 * @author David Bahr
 */
public class GlobalSquareLattice extends SquareLattice
{
	// Note that the following constant is public so that the PropertyReader
	// Class can set a default value for PropertyReader.LATTICE.
	/**
	 * The display name for this lattice.
	 */
	public final static String DISPLAY_NAME = "square (global neighbors)";

	/**
	 * The maximum size of the lattice (width * height) which will preload the
	 * neighbors (for each cell) in a hashmap. If larger than this, neighbors
	 * will be calculated on the fly (slower but less memory intensive).
	 */
	public final static int MAX_NUM_CELLS_TO_PRELOAD_NEIGHBORS = 50 * 50;

	// Tells the lattice how to display its graphics.
	private LatticeView view = null;

	// The tooltip for this lattice
	private static final String TOOLTIP = "<html> <body>"
			+ "<b>An uncommon geometry:</b> a square lattice  with each cell <br>"
			+ "being a neighbor to every other cell.  In other words, all <br>"
			+ "cells can see every other cell on the entire lattice.  This <br>"
			+ "lattice is unusual for cellular automata except in theoretical <br>"
			+ "analyses where it can provide a handy mathematical simplification <br>"
			+ "(like the mean field approximation in physics). <br><br>"
			+ "<b>NOTE: This lattice is a memory hog</b>, so only small numbers of <br>"
			+ "rows and columns are recommended (e.g., 50 by 50 or smaller). <br>"
			+ "Simulations with larger lattices will run very  slowly. <b>Recommend <br>"
			+ "that this lattice be used for research purposes only</b> (rather <br>"
			+ "than visual exploration of the CA). <br><br>"
			+ "<b>NOTE:</b> Unlike other lattices, the neighbors are <i>not</i> guaranteed <br>"
			+ "to be in any particular order.  Generally, the neighbors will be <br>"
			+ "given from the top left to the bottom right.  A spiral arrangement, <br>"
			+ "like the square (8 neighbor) lattice cannot be implemented for this <br>"
			+ "lattice with all grid shapes (for example, rectangular shapes). <br><br>"
			+ "Wrap-around and reflection boundaries are identical in this lattice. "
			+ "</body></html>";

	/**
	 * Default constructor required of all Lattices (for reflection). Typically
	 * not used to build the lattice, but instead used to gain access to methods
	 * such as getDisplayName() and getNumberOfNeighbors().
	 */
	public GlobalSquareLattice()
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
	public GlobalSquareLattice(String initialStateFilePath, Rule rule,
			int maxHistory)
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
	 * On a square lattice, finds all the neighbors to a cell at the specified
	 * row and col position.
	 * <p>
	 * When this returns null, it's because I don't want the neighbors to be
	 * stored in the parent class' hashmap (which is used for fast access). That
	 * hashmap would get too big. Instead, I override the parent class method,
	 * getNeighbors(Cell).
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
		int length = getHeight();
		int width = getWidth();

		// the neighbors we will return
		Cell[] neighbors = null;

		// If bigger than MAX_NUM_CELLS_TO_PRELOAD_NEIGHBORS, then the lattice
		// may be too big for available memory. So return null. In that case, we
		// will be overriding the getNeighbors(Cell cell) method. Otherwise, get
		// the neighbors as below.
		if(length * width <= MAX_NUM_CELLS_TO_PRELOAD_NEIGHBORS)
		{
			// create the neighbor array (-1 because we exclude the cell itself)
			neighbors = new Cell[length * width - 1];

			// fill the neighbor array
			int num = 0;
			for(int i = 0; i < length; i++)
			{
				for(int j = 0; j < width; j++)
				{
					// exclude the cell itself, but include all other cells
					if(!((i == row) && (j == col)))
					{
						neighbors[num] = cells[i][j];
						num++;
					}
				}
			}
		}

		return neighbors;
	}

	/**
	 * Overrides the parent class method and returns an array of all cells that
	 * are connected to the specified cell, excluding itself. This method is
	 * slower than the normal approach which uses getNeighboringCells() to
	 * pre-build an array for fast access. But this approach saves lots of
	 * memory.
	 * 
	 * @param cell
	 *            The specified cell.
	 * @return The neighbors of the specified cell.
	 */
	public Cell[] getNeighbors(Cell cell)
	{
		// fast access to the neighbors provided by the hash map
		Cell[] neighboringCells = super.getNeighbors(cell);

		// no fast access available because it took too much memory
		if(neighboringCells == null)
		{
			// create the neighbor array (-1 because we exclude the cell itself)
			int length = cells.length;
			int width = cells[0].length;
			neighboringCells = new Cell[length * width - 1];

			// fill the neighbor array
			int num = 0;
			for(int i = 0; i < length; i++)
			{
				for(int j = 0; j < width; j++)
				{
					// exclude the cell itself, but include all other cells
					if(!cells[i][j].equals(cell))
					{
						neighboringCells[num] = cells[i][j];
						num++;
					}
				}
			}
		}

		return neighboringCells;
	}

	/**
	 * Returns the number of neighbors of a cell on the lattice (excluding the
	 * cell). If the cells have a varying number of neighbors, then this returns
	 * -1, and the user should use <code>getNeighbors(Cell cell)</code> to
	 * find the number of neighbors for any particular cell.
	 * 
	 * @return The number of neighbors for each cell, or -1 if that number is
	 *         variable.
	 */
	public int getNumberOfNeighbors()
	{
		int numberOfNeighbors = -1;
		if(cells != null)
		{
			int length = cells.length;
			int width = cells[0].length;
			numberOfNeighbors = length * width;
		}
		return numberOfNeighbors;
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
