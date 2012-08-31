/*
 TwoDimensionalLattice -- a class within the Cellular Automaton Explorer. 
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

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import cellularAutomata.Cell;
import cellularAutomata.CAController;
import cellularAutomata.CAStateInitializer;
import cellularAutomata.CurrentProperties;
import cellularAutomata.io.FileStorage;
import cellularAutomata.io.TwoDimensionalFileStorage;
import cellularAutomata.reflection.LatticeHash;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.files.JPGAndPNGImageReadWrite;
import cellularAutomata.util.files.JPGAndPNGAndOtherFileTypeFilter;

/**
 * A two-dimensional lattice. Extend this class to create a square, hexagonal,
 * triangular, or any other two-dimensional lattice that can have its associated
 * cells stored in a two-dimensional array. <br>
 * The neighbors (in other words, the getNeighboringCells() method) are what
 * make different two-dimensional lattices unique. For example, wrap around
 * conditions may or may not be implemented in the getNeighboringCells() method;
 * and the neighborhood could be set up as triangular (three neighbors) square
 * (4 neighbors), hexagonal (6 neighbors) or anything else (for example,
 * variable numbers of neighbors in a non-regular lattice such as the
 * "square-octagonal" lattice). Also note that we can implement lattices with
 * next-nearest neighbors simply by changing the getNeighboringCells() method.
 * <br>
 * Note that by contract, all Lattices must have a constructor with the
 * parameters String, Rule, int, and Properties. These are necessary for using
 * reflection properly.
 * 
 * @author David Bahr
 */
public abstract class TwoDimensionalLattice extends ArrayList<Cell> implements
		Lattice
{
	/**
	 * The maximum number of cells that the two-dimensional simulation should
	 * attempt before memory and speed become a problem.
	 */
	public final static long MAX_RECOMMENDED_CELLS = 200 * 200;

	// default CA width (used if the initial state file is null)
	private final static int CA_DEFAULT_WIDTH = 50;

	// default CA width (used if the initial state file is null)
	private final static int CA_DEFAULT_HEIGHT = 50;

	// maximum CA width
	private final static int MAX_WIDTH = 200;

	// maximum CA height
	private final static int MAX_HEIGHT = 200;

	// the width of the 2-d CA lattice
	private static int numCols = CA_DEFAULT_WIDTH;

	// the height of the 2-d CA lattice
	private static int numRows = CA_DEFAULT_HEIGHT;

	// The two-d array of cells. Note that they are also specified in
	// an arrayList, but changing a cell in the array will automatically
	// change the cell in the arrayList as well (and vice-versa) because they
	// are the same object in both places.
	protected Cell[][] cells = null;

	// the cells as a one-d array for fast access
	protected Cell[] oneDimCells = null;

	// a hash map of neighbors (a Cell[]) keyed by a cell
	// protected HashMap<Cell, Cell[]> neighbors = new HashMap<Cell, Cell[]>();

	// an array of neighbors (used for quick access)
	protected Cell[][] neighbors = null;

	// the class that tells the lattice how to store data in a file
	private FileStorage storage = null;

	/**
	 * Default constructor required of all Lattices (for reflection). Typically
	 * not used to build the lattice, but instead used to gain access to methods
	 * such as getDisplayName() and getNumberOfNeighbors().
	 */
	public TwoDimensionalLattice()
	{
		// keep the array list size minimal
		super(0);
	}

	/**
	 * Create a two-dimensional cellular automaton with the same rule at all
	 * positions.
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
	public TwoDimensionalLattice(String initialStateFilePath, Rule rule,
			int maxHistory)
	{
		// sets the height and width instance variables.
		// i.e., set numRows and numCols
		getCADimensionsFromFile(initialStateFilePath);

		cells = new Cell[numRows][numCols];
		oneDimCells = new Cell[numRows * numCols];

		// Set the rows and cols as properties since the initial state file may
		// have reset their values (from the original one read from the property
		// file).
		CurrentProperties.getInstance().setNumRows(numRows);
		CurrentProperties.getInstance().setNumColumns(numCols);

		// fill the 2-d grid with Cells
		for(int i = 0; i < numRows; i++)
		{
			for(int j = 0; j < numCols; j++)
			{
				// now create the cell from the new state
				Cell cell = new Cell(rule, maxHistory, new Coordinate(i, j));

				// keep track in the arrayList (needed for its iterator)
				this.add(cell);

				// for simplicity when finding neighbors, also keep track in
				// an array
				cells[i][j] = cell;

				// for fastest access
				oneDimCells[i * numCols + j] = cell;
			}
		}

		// what kind of boundary condition?
		int boundaryType = CurrentProperties.getInstance()
				.getBoundaryCondition();

		// for fast access, the following finds every cell's neighbors in
		// advance and stores them in an array. Note that this has to be a
		// different loop from the above because I have not yet instantiated
		// every cell in the previous loop.
		neighbors = new Cell[numRows * numCols][];
		for(int i = 0; i < numRows; i++)
		{
			for(int j = 0; j < numCols; j++)
			{
				neighbors[i * numCols + j] = getNeighboringCells(i, j,
						boundaryType);

				// hashmap(key, value) as (cell, neighbors)
				// neighbors.put(cells[i][j], getNeighboringCells(i, j,
				// boundaryType));
			}
		}

		// set the state of each cell
		setInitialState(initialStateFilePath);

		// tell it how to save data in a file
		int requiredNumberOfGenerations = rule.getRequiredNumberOfGenerations();
		storage = new TwoDimensionalFileStorage(requiredNumberOfGenerations);
	}

	/**
	 * Finds the nearest neighbors to a cell at the specified index. Note the
	 * wrap around or reflection boundary conditions. For wrap around boundary
	 * conditions, this method may need to access the methods getWidth() and
	 * getHeight() which returns the total number of columns and rows
	 * respectively.
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
	protected abstract Cell[] getNeighboringCells(int row, int col,
			int boundaryType);

	/**
	 * Opens the initial states file and reads the width and length of the
	 * cellular automaton. Assumes that the data is stored in a square
	 * two-dimensional array with each data point separated by a delimiter.
	 * Stores in instance variables. May be overridden if necessary.
	 * 
	 * @param filePath
	 *            The file containing the initial states.
	 */
	protected void getCADimensionsFromFile(String filePath)
	{
		// decide if data file or image file
		String suffix = JPGAndPNGAndOtherFileTypeFilter.getSuffix(filePath);
		if(!JPGAndPNGImageReadWrite.isPermittedImageType(suffix))
		{
			// then it is a data file
			try
			{
				// open the file containing the initial states
				FileReader inputStream = new FileReader(filePath);
				BufferedReader fileReader = new BufferedReader(inputStream);

				// read the file
				String line = fileReader.readLine();

				// skip comments
				while((line != null)
						&& (line.startsWith("//") || (line.length() == 0)))
				{
					line = fileReader.readLine();
				}

				if(line != null)
				{
					int lineCount = 0;

					// get the width
					if(line.length() > 0)
					{
						// width of the CA
						numCols = 0;
						StringTokenizer tokens = new StringTokenizer(line,
								CurrentProperties.getInstance()
										.getDataDelimiters());
						while(tokens.hasMoreTokens())
						{
							tokens.nextToken();
							numCols++;
						}

						while((line != null) && (line.length() > 0))
						{
							// now get the height (it is the lineCount)
							line = fileReader.readLine();

							// stop if reach another comment (or blank line)
							if((line == null) || line.startsWith("//")
									|| (line.length() == 0))
							{
								// make it stop
								line = null;
							}

							lineCount++;
						}

						// height of the CA
						numRows = lineCount;
					}
				}

				fileReader.close();
			}
			catch(IOException e)
			{
				// if here then the file doesn't exist or has a problem
				// so use the default sizes (from the properties file)
				numCols = CurrentProperties.getInstance().getNumColumns();
				numRows = CurrentProperties.getInstance().getNumRows();
			}
		}
		else
		{
			// its an image file
			BufferedImage image = null;
			try
			{
				// Read from a file
				File file = new File(filePath);
				image = ImageIO.read(file);

				numCols = image.getWidth();
				numRows = image.getHeight();

				// rescale to a reasonable size
				if(numCols > numRows)
				{
					double factor = (double) numRows / (double) numCols;
					if(numCols > MAX_WIDTH)
					{
						numCols = MAX_WIDTH;
						numRows = (int) (numCols * factor);
					}
				}
				else
				{
					double factor = (double) numCols / (double) numRows;
					if(numRows > MAX_HEIGHT)
					{
						numRows = MAX_HEIGHT;
						numCols = (int) (numRows * factor);
					}
				}
			}
			catch(IOException e)
			{
				// if here then the file doesn't exist or has a problem
				// so use the default sizes (from the properties file)
				numCols = CurrentProperties.getInstance().getNumColumns();
				numRows = CurrentProperties.getInstance().getNumRows();
			}
		}
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
	 * Get the number of vertical cells in the cellular automaton.
	 * 
	 * @return The height of the cellular automaton.
	 */
	public int getHeight()
	{
		return numRows;
	}

	/**
	 * Get the number of cells along the width of the cellular automaton.
	 * 
	 * @return The width of the cellular automaton.
	 */
	public int getWidth()
	{
		return numCols;
	}

	/**
	 * Returns an array of all cells that are connected to the specified cell,
	 * excluding itself.
	 * 
	 * @param cell
	 *            The specified cell.
	 * @return The neighbors of the specified cell.
	 */
	public Cell[] getNeighbors(Cell cell)
	{
		// fast access to the neighbors provided by the array
		return neighbors[cell.getCoordinate().getRow() * numCols
				+ cell.getCoordinate().getColumn()];

		// fast access to the neighbors provided by the hash map
		// return neighbors.get(cell);
	}

	/**
	 * The number of neighbors for a cell on the current CA lattice. If the
	 * number of neighbors changes with lattice position, then this method
	 * returns -1. This utility method is not specific to two-dimensional
	 * lattices, and works equally well with one-dimensional lattices. It
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
	 * file. Assumes that the data is stored in a square two-dimensional array
	 * with each data point separated by a delimiter. May be overridden if
	 * necessary.
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
