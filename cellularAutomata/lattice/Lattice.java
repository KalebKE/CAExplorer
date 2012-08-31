/*
 Lattice -- a class within the Cellular Automaton Explorer. 
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
import cellularAutomata.io.FileStorage;
import cellularAutomata.lattice.view.LatticeView;

/**
 * A lattice is a grid or topology of connected Cells designed with a data
 * structure such as a linked list, array, tree, etc. Typical lattices are
 * one-dimensional lines, two-dimensional square lattices, and two-dimensional
 * hexagonal lattices. The lattice embodies the concept of a "neighborhood",
 * because each cell's neighbors are the ones to which it is connected. In fact,
 * classes that implement this interface may extend other data structures (like
 * a LinkedList or an ArrayList), but the actual lattice topology is controlled
 * by the method getNeighbors(Cell cell). <br>
 * Note that by contract, all Lattices must have a constructor with the
 * parameters String, Rule, int, and Properties. These are necessary for using
 * reflection properly. There must also be a default constructor (with no
 * parameters). This is enforced nicely by the reflection code, which will warn
 * a developer if they break this rule.
 * 
 * @author David Bahr
 */
public interface Lattice extends Iterable<Cell>
{
	/**
	 * The maximum recommended number of neighbors in a cell's neighborhood.
	 * Exceeding this value may cause memory problems on a typical computer.
	 */
	public static final int MAX_RECOMMENDED_NEIGHBORS = 500;

	/**
	 * The constant specifying a wrap around boundary condition.
	 */
	public static final int WRAP_AROUND_BOUNDARY = 0;

	/**
	 * The constant specifying a reflection boundary condition.
	 */
	public static final int REFLECTION_BOUNDARY = 1;

	/**
	 * Gets all of the cells that make up this lattice. No order is guaranteed
	 * but is generally obvious. One-dimensional lattices are generally returned
	 * left to right starting with array element 0. Generally, for
	 * two-dimensional square lattices, the element array[row][col] is accessed
	 * at array[row * width + col].
	 * 
	 * @return The array of cells that make up this lattice.
	 */
	public abstract Cell[] getCells();

	/**
	 * A brief one or two-word string describing the rule, appropriate for
	 * display in a drop-down list.
	 * <p>
	 * NOTE: By contract every lattice class should also have a static variable
	 * called DISPLAY_NAME. This is the variable that will be referenced by Rule
	 * classes to indicate that they are compatible with this lattice. I don't
	 * like this -- should find a better way (perhaps by having the Rule classes
	 * reference the lattice class name?).
	 * 
	 * @return A string no longer than 25 characters.
	 */
	public abstract String getDisplayName();

	/**
	 * Gets the class that tells the lattice how to store its data in a file.
	 * 
	 * @return The storage class that tells the lattice how to save to a file.
	 */
	public abstract FileStorage getFileStorage();

	/**
	 * The height (or number of rows) of the lattice. In one-dimensional
	 * lattices, this is always 1.
	 * 
	 * @return the number of rows in the lattice.
	 */
	public abstract int getHeight();

	/**
	 * A tooltip that describes the lattice.
	 * 
	 * @return The tooltip.
	 */
	public abstract String getToolTipDescription();

	/**
	 * Gets the graphics that tells the lattice how to display itself.
	 * 
	 * @return The graphics view that tells this Lattice how to display.
	 */
	public abstract LatticeView getView();

	/**
	 * Returns an array of all cells that are connected to the specified cell,
	 * excluding itself. By convention the neighbors should be indexed clockwise
	 * starting to the northwest of the cell.
	 * 
	 * @param cell
	 *            The specified cell.
	 * @return The neighbors of the specified cell (by convention the neighbors
	 *         should be indexed clockwise starting to the northwest of the
	 *         cell).
	 */
	public abstract Cell[] getNeighbors(Cell cell);

	/**
	 * Returns the number of neighbors of a cell on the lattice (excluding the
	 * cell). If the cells have a varying number of neighbors, then this returns
	 * -1, and the user should use <code>getNeighbors(Cell cell)</code> to
	 * find the number of neighbors for any particular cell.
	 * 
	 * @return The number of neighbors for each cell, or -1 if that number is
	 *         variable.
	 */
	public abstract int getNumberOfNeighbors();

	/**
	 * The width (or number of columns, or length) of the lattice. In
	 * one-dimensional lattices, this is the only meaningful dimension.
	 * 
	 * @return the number of columns in the lattice.
	 */
	public abstract int getWidth();

	/**
	 * Sets the class that tells the lattice how to store its data in a file.
	 * Not necessary to set unless desired because a default is always provided.
	 * 
	 * @param storage
	 *            The storage class that tells the lattice how to save to a
	 *            file.
	 */
	public abstract void setFileStorage(FileStorage storage);

	/**
	 * Sets the state of all the cells on the lattice by reading values from a
	 * file. The data in the file should be ordered in the same sequence as the
	 * Iterator returned by the lattice.
	 * 
	 * @param filePath
	 *            The path to the file containing the initial state of the
	 *            automaton. For example, "C:/initial.data".
	 */
	public abstract void setInitialState(String filePath);

	/**
	 * Sets the graphics that tells the Lattice how to display itself. Unless a
	 * non-default is desired, the view does not have to be set by the user
	 * (because a default is always used).
	 * 
	 * @param view
	 *            The graphics view that tells this Lattice how to display.
	 */
	public abstract void setView(LatticeView view);
}
