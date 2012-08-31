/*
 OneDimensionalLattice -- a class within the Cellular Automaton Explorer. 
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import cellularAutomata.CAController;
import cellularAutomata.CAStateInitializer;
import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.io.FileStorage;
import cellularAutomata.io.OneDimensionalFileStorage;
import cellularAutomata.reflection.LatticeHash;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.Coordinate;

/**
 * A one-dimensional lattice. Extend this class to create any lattice that can
 * have its associated cells stored in a one-dimensional array. <br>
 * The neighbors (in other words, the getNeighboringCells() method) are what
 * make different one-dimensional lattices unique. For example, wrap around
 * conditions may or may not be implemented in the getNeighboringCells() method.
 * And we can implement lattices with nearest neighbors or with with
 * next-nearest neighbors simply by changing the getNeighboringCells() method.
 * <br>
 * Note that by contract, all Lattices must have a constructor with the
 * parameters String, Rule, int, and Properties. These are necessary for using
 * reflection properly.
 * 
 * @author David Bahr
 */
public abstract class OneDimensionalLattice extends ArrayList<Cell> implements
		Lattice
{
	/**
	 * The maximum number of cells that the one-dimensional simulation should
	 * attempt before memory and speed become a problem.
	 */
	public static final long MAX_RECOMMENDED_CELLS = 750 * 750;

	// the cells as a one-d array for fast access
	protected Cell[] oneDimCells = null;

	// the length of the 1-d CA lattice
	private int length;

	// the class that tells the lattice how to store data in a file
	private FileStorage storage = null;

	// a hash map of neighbors (a Cell[]) keyed by a cell
	// private HashMap<Cell, Cell[]> neighbors = new HashMap<Cell, Cell[]>();

	// an array of neighbors (used for quick access)
	protected Cell[][] neighbors = null;

	/**
	 * Default constructor required of all Lattices (for reflection). Typically
	 * not used to build the lattice, but instead used to gain access to methods
	 * such as getDisplayName() and getNumberOfNeighbors().
	 */
	public OneDimensionalLattice()
	{
		// keep the array list size minimal
		super(0);
	}

	/**
	 * Create a one-dimensional cellular automaton with the same rule at all
	 * positions. The length of the automaton and the initial state of each cell
	 * is specified in a file.
	 * 
	 * @param rule
	 *            The rule applied to all cells.
	 * @param initialStateFilePath
	 *            The path to the file that specifies the initial state of the
	 *            cellular automaton. If null or invalid, a default initial
	 *            state is created.
	 * @param maxHistory
	 *            The maximum number of generations (or time steps) that will be
	 *            remembered by the Cells on the lattice.
	 */
	public OneDimensionalLattice(String initialStateFilePath, Rule rule,
			int maxHistory)
	{
		// also called the width
		this.length = getCALengthFromFile(initialStateFilePath);

		oneDimCells = new Cell[length];

		// Set the length as a property (since the initial state file may have
		// reset its value from the original one read from the property file).
		CurrentProperties.getInstance().setNumColumns(length);

		// fill the 1-d grid with Cells
		for(int i = 0; i < length; i++)
		{
			Cell cell = new Cell(rule, maxHistory, new Coordinate(0, i));
			this.add(cell);

			// for fast access
			oneDimCells[i] = cell;
		}

		// set the state of each cell
		setInitialState(initialStateFilePath);

		// what kind of boundary condition?
		int boundaryType = CurrentProperties.getInstance()
				.getBoundaryCondition();

		// for fast access, the following finds every cell's neighbors in
		// advance and stores them in a hash map. Note that this has to be a
		// different loop from the above because I have not yet instantiated
		// every cell in the previous loop.
		neighbors = new Cell[length][];
		for(int i = 0; i < length; i++)
		{
			neighbors[i] = getNeighboringCells(i, boundaryType);

			// hashmap(key, value) as (cell, neighbors)
			// neighbors.put(oneDimCells[i], getNeighboringCells(i,
			// boundaryType));
		}

		// tell it how to save data in a file
		storage = new OneDimensionalFileStorage();
	}

	/**
	 * Create a one-dimensional lattice from the given cells. Each cell is
	 * assumed to already have an assigned value.
	 * 
	 * @param cells
	 *            The cells used to fill the lattice.
	 */
	public OneDimensionalLattice(Cell[] cells)
	{
		// also called the width
		this.length = cells.length;

		oneDimCells = new Cell[length];

		// fill the 1-d grid with Cells
		for(int i = 0; i < length; i++)
		{
			this.add(cells[i]);

			oneDimCells[i] = cells[i];
		}

		// what kind of boundary condition?
		int boundaryType = CurrentProperties.getInstance()
				.getBoundaryCondition();

		// for fast access, the following finds every cell's neighbors in
		// advance and stores them in a hash map. Note that this has to be a
		// different loop from the above because I have not yet instantiated
		// every cell in the previous loop.
		neighbors = new Cell[length][];
		for(int i = 0; i < length; i++)
		{
			neighbors[i] = getNeighboringCells(i, boundaryType);

			// hashmap(key, value) as (cell, neighbors)
			// neighbors.put(oneDimCells[i], getNeighboringCells(i,
			// boundaryType));
		}

		// tell it how to save data in a file
		storage = new OneDimensionalFileStorage();
	}

	/**
	 * Create a one-dimensional cellular automaton with the same rule at all
	 * positions. The length of the automaton and the initial state of each cell
	 * is specified in a file.
	 * 
	 * @param rule
	 *            The rule applied to all cells.
	 * @param initialStateFilePath
	 *            The path to the file that specifies the initial state of the
	 *            cellular automaton. If null or invalid, a default initial
	 *            state is created.
	 */
	public OneDimensionalLattice(String initialStateFilePath, Rule rule)
	{
		// Sets a default for the maximum number of generations remembered by
		// each cell. For simplicity, kept the same as the height displayed by
		// the CA.
		this(initialStateFilePath, rule, CurrentProperties.getInstance()
				.getNumRows());
	}

	/**
	 * Finds the neighbors to a cell at the specified index. For wrap around
	 * boundary conditions, this method may need to access the method
	 * getLength() which returns the total number of cells on the lattice.
	 * 
	 * @param index
	 *            The cell's index.
	 * @param boundaryType
	 *            A constant indicating the type of boundary (wrap-around,
	 *            reflection, etc). Acceptable constants are specified in the
	 *            Lattice class.
	 * @return An array of neighboring cells.
	 */
	protected abstract Cell[] getNeighboringCells(int index, int boundaryType);

	/**
	 * Opens the initial states file and reads the length of the cellular
	 * automaton. If the file is null or non-existent, then a default length is
	 * assigned.
	 * 
	 * @param filePath
	 *            The file containing the initial states.
	 * @return The length of the cellular automaton.
	 */
	private int getCALengthFromFile(String filePath)
	{
		// length of the CA
		int caLength = CurrentProperties.getInstance().getNumColumns();

		try
		{
			// open the file containing the initial states
			FileReader inputStream = new FileReader(filePath);
			BufferedReader fileReader = new BufferedReader(inputStream);

			// read the file
			String initialStates = fileReader.readLine();

			// skip comments
			while((initialStates != null)
					&& (initialStates.startsWith("//") || (initialStates
							.length() == 0)))
			{
				initialStates = fileReader.readLine();
			}

			if(initialStates != null)
			{
				// extract data one datum at a time
				caLength = 0;
				StringTokenizer tokens = new StringTokenizer(initialStates,
						CurrentProperties.getInstance().getDataDelimiters());
				while(tokens.hasMoreTokens())
				{
					tokens.nextToken();
					caLength++;
				}
			}

			fileReader.close();
		}
		catch(Exception e)
		{
			// We expect this exception if the file is null.
		}
		finally
		{
			// can't be less than 2
			if(caLength < 2)
			{
				caLength = 2;
			}
		}

		return caLength;
	}

	/**
	 * Gets all of the cells that make up this lattice. No order is guaranteed
	 * but is generally obvious. Generally, for two-dimensional square lattices,
	 * the element array[row][col] is accessed at array[row * width + col].
	 * 
	 * @return The array of cells that make up this lattice.
	 */
	public Cell[] getCells()
	{
		return oneDimCells;
	}

	/**
	 * Gets the class that tells the lattice how to store its data in a file.
	 * 
	 * @return The storage class that tells the lattice how to save to a file.
	 */
	public FileStorage getFileStorage()
	{
		return storage;
	}

	/**
	 * The width (or number of columns, or length) of the lattice. In
	 * one-dimensional lattices, this is the only meaningful dimension.
	 * 
	 * @return the number of columns in the lattice.
	 */
	public int getWidth()
	{
		return length;
	}

	/**
	 * The height (or number of rows) of the lattice. In one-dimensional
	 * lattices, this is always 1.
	 * 
	 * @return the number of rows in the lattice.
	 */
	public int getHeight()
	{
		return 1;
	}

	/**
	 * Gets an appropriate value for the number of generations that the state
	 * history should store. At a minimum, must store the current state and the
	 * previous state.
	 * 
	 * @return The size of the state history.
	 */
	public static int getMaxStateHistory(Rule rule)
	{
		// the minimum.
		int maxHistory = rule.getRequiredNumberOfGenerations() + 1;

		// for one-dim, need the runningAverage if it is bigger than the
		// maxHistory (or displays funny)
		maxHistory = Math.max(maxHistory, CurrentProperties.getInstance()
				.getRunningAverage());

		// for one-dim ,need the height if it is bigger than the
		// maxHistory (or displays funny). But only do this if the height isn't
		// crazy-big.
		int width = CurrentProperties.getInstance().getNumColumns();
		int height = CurrentProperties.getInstance().getNumRows();
		if(height * width <= MAX_RECOMMENDED_CELLS)
		{
			maxHistory = Math.max(maxHistory, height);
		}

		return maxHistory;
	}

	/**
	 * Returns an array of all cells that are connected to the specified cell,
	 * excluding itself. For the one-dimensional lattice this is the cell to the
	 * left and the cell to its right.
	 * 
	 * @param cell
	 *            The specified cell.
	 * @return The neighbors of the specified cell.
	 */
	public Cell[] getNeighbors(Cell cell)
	{
		// fast access to the neighbors provided by the array
		Cell[] neighboringCells = neighbors[cell.getCoordinate().getColumn()];

		// fast access to the neighbors provided by the hash map
		// Cell[] neighboringCells = (Cell[]) neighbors.get(cell);

		// just in case the cell is not in the hash map
		if(neighboringCells == null)
		{
			// what kind of boundary condition?
			int boundaryType = CurrentProperties.getInstance()
					.getBoundaryCondition();

			// find the index of the cell (potentially slow)
			neighboringCells = getNeighboringCells(cell.getCoordinate()
					.getColumn(), boundaryType);

			// put it in the hash map, so next time it will be there
			neighbors[cell.getCoordinate().getColumn()] = neighboringCells;

			// put it in the hash map, so next time it will be there
			// neighbors.put(cell, neighboringCells);
		}

		return neighboringCells;
	}

	/**
	 * The number of neighbors for a cell on the current CA lattice. If the
	 * number of neighbors changes with lattice position, then this method
	 * returns -1. This utility method is not specific to one-dimensional
	 * lattices, and works equally well with two-dimensional lattices. It
	 * belongs in a base class, but that base class does not exist.
	 * 
	 * @return The number of neighbors for a cell on the current lattice, or -1
	 *         if the number of neighbors changes with position.
	 */
	public static int getNumberOfNeighbors(String latticeDescription)
	{
		LatticeHash latticeHash = new LatticeHash();
		String latticeClassName = latticeHash.get(latticeDescription);

		// this actually calls the getNumberOfNeighbors() method required of all
		// Lattice classes
		return ReflectionTool
				.getNumberOfNeighborsFromLatticeClassName(latticeClassName);
	}

	/**
	 * Tests if the CA's current lattice (as set in the properties) is
	 * one-dimensional.
	 * 
	 * @return true if the lattice is a one-dimensional lattice.
	 */
	public static boolean isCurrentLatticeOneDim()
	{
		String latticeDescription = CurrentProperties.getInstance()
				.getLatticeDisplayName();

		return isCurrentLatticeOneDim(latticeDescription);
	}

	/**
	 * Tests if the lattice's display description corresponds to a
	 * one-dimensional lattice.
	 * 
	 * @param latticeDescription
	 *            The lattice display description.
	 * @return true if the display name is for a one-dimensional lattice.
	 */
	public static boolean isCurrentLatticeOneDim(String latticeDescription)
	{
		boolean oneDim = false;

		LatticeHash latticeHash = new LatticeHash();
		String latticeClassName = latticeHash.get(latticeDescription);
		String superClass = "";
		try
		{
			superClass = Class.forName(latticeClassName).getSuperclass()
					.getName();
		}
		catch(Exception e)
		{
			// do nothing
		}

		if(superClass.endsWith(".OneDimensionalLattice"))
		{
			oneDim = true;
		}

		return oneDim;
	}

	/**
	 * Sets the class that tells the lattice how to store its data in a file.
	 * Not necessary to set unless desired because a default is always provided.
	 * 
	 * @param storage
	 *            The storage class that tells the lattice how to save to a
	 *            file.
	 */
	public void setFileStorage(FileStorage storage)
	{
		this.storage = storage;
	}

	/**
	 * Sets the state of all the cells on the lattice by reading values from a
	 * file. The data in the file should be ordered linearly.
	 * 
	 * @param filePathOrProperty
	 *            The path to the file containing the initial state of the
	 *            automaton. For example, "C:/initial.data". May be a property
	 *            value instead of a file path such as
	 *            PropertyReader.STATE_BLANK, PropertyReader.STATE_SINGLE_SEED,
	 *            or PropertyReader.STATE_RANDOM.
	 */
	public void setInitialState(String filePathOrProperty)
	{
		CAStateInitializer initializer = new CAStateInitializer(CAController
				.getCAFrame(), this);

		initializer.setInitialState(filePathOrProperty);
	}
}
