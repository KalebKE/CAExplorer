/*
 RandomGaussianLattice -- a class within the Cellular Automaton Explorer. 
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import cellularAutomata.CurrentProperties;
import cellularAutomata.Cell;
import cellularAutomata.lattice.view.LatticeView;
import cellularAutomata.lattice.view.SquareLatticeView;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.math.GaussianRandom;
import cellularAutomata.util.math.Modulus;
import cellularAutomata.util.math.RandomSingleton;

/**
 * A simple two-dimensional square lattice where each cell has random neighbors
 * distributed normally about the cell with a standard deviation of 1. In other
 * words, nearby cells are much more likely to be neighbors than far away cells.
 * The neighborhoods are symmetric. If cell x sees cell y as a neighbor, then
 * cell y will see cell x as a neighbor. The neighborhoods behave as "two way"
 * streets or as a bidirectional graphs.
 * <p>
 * The wrap around/reflection boundary conditions and choice of neighbors are
 * implemented in the getNeighbors() method.
 * 
 * @author David Bahr
 */
public class RandomGaussianLattice extends SquareLattice
{
	// Note that the following constant is public so that the PropertyReader
	// Class can set a default value for PropertyReader.LATTICE.
	/**
	 * The display name for this lattice.
	 */
	public final static String DISPLAY_NAME = "square (random Gaussian)";

	/**
	 * The maximum number of connections (neighbor connections) per cell on
	 * average.
	 */
	public final static int MAX_NUMBER_OF_CONNECTIONS = 50;

	/**
	 * The default value for the standard deviation if no other is specified.
	 */
	public final static double DEFAULT_STANDARD_DEVIATION = 5.0;

	/**
	 * The standard deviation of the normal distribution of the neighbors about
	 * the central cell.
	 */
	public static double standardDeviation = DEFAULT_STANDARD_DEVIATION;

	// a hashmap that will hold the connections between the cells. Each cell
	// is mapped to a linked list of cells that holds the connections.
	private HashMap<Cell, LinkedList<Cell>> connections = null;

	// Tells the lattice how to display its graphics.
	private LatticeView view = null;

	// The tooltip for this lattice
	private static final String TOOLTIP = "<html> <body>"
			+ "<b>An uncommon geometry:</b> a square lattice where each cell <br>"
			+ "has random neighbors distributed normally (Gaussian).  The <br>"
			+ "standard deviation of the normal distribution is set below. <br><br>"
			+ "With a Gaussian, nearby cells are much more likely to be <br>"
			+ "neighbors than far away cells.  Each cell will have a <br>"
			+ "random number of neighbors (positioned in normally distributed <br>"
			+ "random places). <br><br>"
			+ "The maximum number of neighborhood connections allowed for <br>"
			+ "each cell is "
			+ MAX_NUMBER_OF_CONNECTIONS
			+ " or ((rows * cols) - 1) * ((rows * cols) - 1), whichever <br>"
			+ "is less.  Most cells will have fewer neighbors. All cells <br>"
			+ "have at least one neighbor. <br><br>"
			+ "The neighborhoods are symmetric. If cell x sees cell y as a <br>"
			+ "neighbor, then cell y will see cell x as a neighbor. The <br>"
			+ "neighborhoods behave as \"two way\" streets or as <br>"
			+ "bidirectional graphs. <br><br>"
			+ "<b>NOTE:</b> Unlike other lattices, the neighbors are <i>not</i> <br>"
			+ "guaranteed to be in any particular order. <br><br>"
			+ "To see the neighborhood associated with a cell, use the <br>"
			+ "\"Show Neighborhood\" analysis." + "</body></html>";

	// a random number generator (with a guassian distribution)
	private GaussianRandom guassianRandom = null;

	// a random number generator (with a uniform distribution)
	private Random random = null;

	/**
	 * Default constructor required of all Lattices (for reflection). Typically
	 * not used to build the lattice, but instead used to gain access to methods
	 * such as getDisplayName() and getNumberOfNeighbors().
	 */
	public RandomGaussianLattice()
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
	public RandomGaussianLattice(String initialStateFilePath, Rule rule,
			int maxHistory)
	{
		super(initialStateFilePath, rule, maxHistory);

		view = new SquareLatticeView(this);

		// make sure the standardDeviation in the properties is the same as the
		// standardDeviation here.
		standardDeviation = CurrentProperties.getInstance()
				.getStandardDeviation();

		if(standardDeviation <= 0.0)
		{
			standardDeviation = DEFAULT_STANDARD_DEVIATION;
		}
	}

	/**
	 * Makes the connections between cells.
	 * 
	 * @param boundaryType
	 *            A constant indicating the type of boundary (wrap-around,
	 *            reflection, etc). Acceptable constants are specified in the
	 *            Lattice class.
	 * @return a hashmap keyed by cell that gives a linked list of the
	 *         neighboring cells.
	 */
	private HashMap<Cell, LinkedList<Cell>> makeConnections(int boundaryType)
	{
		if(guassianRandom == null)
		{
			guassianRandom = new GaussianRandom();
		}
		if(random == null)
		{
			random = RandomSingleton.getInstance();
		}

		// a hashmap that will hold the connections between the cells. Each cell
		// is mapped to a linked list of cells that holds the connections.
		HashMap<Cell, LinkedList<Cell>> connections = new HashMap<Cell, LinkedList<Cell>>();

		// how many connections should we make
		int maxNumberOfConnections = MAX_NUMBER_OF_CONNECTIONS;
		{
			// limit the size to the number of cells on the lattice
			if(maxNumberOfConnections > ((getHeight() * getWidth()) - 1)
					* ((getHeight() * getWidth()) - 1))
			{
				maxNumberOfConnections = ((getHeight() * getWidth()) - 1)
						* ((getHeight() * getWidth()) - 1);
			}
		}

		// add connections for every cell
		for(int row1 = 0; row1 < getHeight(); row1++)
		{
			for(int col1 = 0; col1 < getWidth(); col1++)
			{
				// the number of connections for each cell. Makes sure every
				// cell has at least one connection
				int numberOfConnections = 1 + random
						.nextInt(maxNumberOfConnections);

				for(int num = 0; num < numberOfConnections; num++)
				{
					int row2 = 0;
					int col2 = 0;
					do
					{
						// get the next cell with a normal distribution centered
						// about the current cell with a standard deviation of
						// 1. Make sure we don't go more than halfway across the
						// grid because that would then be closer to the other
						// side of the cell.
						int rowRadius = 0;
						do
						{
							// if X ~ N(mu, sigma^2), then
							// aX = N(a mu, (a sigma)^2). In our case mu = 0 and
							// sigma = 1, so we set the standard deviation by
							// multiplying by that standard deviation that we
							// desire.
							double r = guassianRandom.nextGaussian(0.0,
									standardDeviation);
							rowRadius = (int) Math.round(r);
						}
						while(rowRadius > getHeight() / 2.0);
						int colRadius = 0;
						do
						{
							double r = guassianRandom.nextGaussian(0.0,
									standardDeviation);
							colRadius = (int) Math.round(r);
						}
						while(colRadius > getWidth() / 2.0);

						// Wrap-around boundary condition. Modulus.mod() deals
						// with negative numbers the way we expect.
						row2 = Modulus.mod(row1 + rowRadius, getHeight());
						col2 = Modulus.mod(col1 + colRadius, getWidth());

						// add reflection boundary condition
						if(boundaryType == Lattice.REFLECTION_BOUNDARY)
						{
							if(row1 + rowRadius < 0)
							{
								row2 = -(row1 + rowRadius);
							}
							if(row1 + rowRadius >= getHeight())
							{
								row2 = (getHeight() - 1)
										- ((row1 + rowRadius) - getHeight());
							}
							if(col1 + colRadius < 0)
							{
								col2 = -(col1 + colRadius);
							}
							if(col1 + colRadius >= getWidth())
							{
								col2 = (getWidth() - 1)
										- ((col1 + colRadius) - getWidth());
							}
						}
					}
					while((row1 == row2) && (col1 == col2));

					// get the list of cells connected to each of (row1, col1)
					// and (row2, col2)
					LinkedList<Cell> listOfConnectionsForCell = connections
							.get(cells[row1][col1]);
					if(listOfConnectionsForCell == null)
					{
						listOfConnectionsForCell = new LinkedList<Cell>();
					}
					LinkedList<Cell> listOfConnectionsForOtherCell = connections
							.get(cells[row2][col2]);
					if(listOfConnectionsForOtherCell == null)
					{
						listOfConnectionsForOtherCell = new LinkedList<Cell>();
					}

					// We have two lattice positions, so now connect those two
					// cells (unless they are already connected or unless there
					// are already the max number of connections).
					if((listOfConnectionsForCell.size() < numberOfConnections)
							&& (listOfConnectionsForOtherCell.size() < numberOfConnections))
					{
						if(!listOfConnectionsForCell
								.contains(cells[row2][col2]))
						{
							listOfConnectionsForCell.add(cells[row2][col2]);
						}
						connections.put(cells[row1][col1],
								listOfConnectionsForCell);

						// And now make the opposite connection as well (the
						// "two-way street"). (Unless they are already connected
						// or unless there are already the max number of
						// connections.)
						if(!listOfConnectionsForOtherCell
								.contains(cells[row1][col1]))
						{
							listOfConnectionsForOtherCell
									.add(cells[row1][col1]);
						}
						connections.put(cells[row2][col2],
								listOfConnectionsForOtherCell);
					}
				}
			}
		}

		return connections;
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
	 * On a square lattice, finds the random neighbors to a cell which is
	 * positioned at the specified row and col position.
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
		if(connections == null)
		{
			connections = makeConnections(boundaryType);
		}

		// the list of neighbors
		LinkedList<Cell> cellneighbors = connections.get(cells[row][col]);

		// the neighbors we will return
		Cell[] neighbors = new Cell[cellneighbors.size()];

		// convert the list of neighbors to an array of neighbors
		neighbors = cellneighbors.toArray(neighbors);

		// count the number of cells that claim to be neighbors to
		// cell[row][col]
		// int num = 0;
		// for(int i = 0; i < getHeight(); i++)
		// {
		// for(int j = 0; j < getHeight(); j++)
		// {
		// LinkedList<Cell> neighb = connections.get(cells[i][j]);
		// java.util.Iterator iter = neighb.iterator();
		// while(iter.hasNext())
		// {
		// Cell c = (Cell) iter.next();
		// if(c.equals(cells[row][col]))
		// {
		// num++;
		// }
		// }
		// }
		// }
		// System.out.println("RandomSym: number of neighbors that reference ["
		// + row + "][" + col + "] = " + num);

		// return the random neighbors
		return neighbors;
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
		return -1;
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
