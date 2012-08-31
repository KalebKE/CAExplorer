/*
 RandomSmallWorldLattice -- a class within the Cellular Automaton Explorer. 
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import cellularAutomata.Cell;
import cellularAutomata.lattice.view.LatticeView;
import cellularAutomata.lattice.view.SquareLatticeView;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.math.RandomSingleton;

/**
 * A simple two-dimensional square lattice where each cell has random neighbors
 * distributed with power-law connectivity. In other words, most cells have very
 * few neighbors, but some cells have very many connections. The numbers of
 * connections follow a power-law distribution. The neighborhoods are symmetric.
 * If cell x sees cell y as a neighbor, then cell y will see cell x as a
 * neighbor. The neighborhoods behave as "two way" streets or as a bidirectional
 * graphs.
 * <p>
 * The wrap around/reflection boundary conditions and choice of neighbors are
 * implemented in the getNeighbors() method.
 * 
 * @author David Bahr
 */
public class RandomSmallWorldLattice extends SquareLattice
{
    // Note that the following constant is public so that the PropertyReader
    // Class can set a default value for PropertyReader.LATTICE.
    /**
     * The display name for this lattice.
     */
    public final static String DISPLAY_NAME = "square (random small world)";

    // the list of cells that have been added (one by one) to the network
    private ArrayList<Cell> alreadyConnectedCells = null;

    // a hashmap that will hold the connections between the cells. Each cell
    // is mapped to a linked list of cells to which it is connected.
    private HashMap<Cell, LinkedList<Cell>> connections = null;

    // Tells the lattice how to display its graphics.
    private LatticeView view = null;

    // The tooltip for this lattice
    private static final String TOOLTIP = "<html> <body>"
        + "<b>An uncommon topology:</b> a two-dimensional square lattice <br>"
        + "where each cell has random neighbors distributed with power- <br>"
        + "law connectivity. Most cells have very few neighbors, but some <br>"
        + "cells have very many connections. The numbers of connections <br>"
        + "follow a power-law distribution. This is an example of a \"small- <br>"
        + "world network\" because most cells will be only a few \"steps\" <br>"
        + "away from any other cell. <br><br>"
        + "Small world networks have many applications, particularly in <br>"
        + "social networks, the structure of the world-wide web, etc. <br><br>"
        + "This particular lattice is constructed with the Barabási-Albert <br>"
        + "algorithm to ensure a scale-free structure.  However, there are <br>"
        + "other \"small-world\" networks that do not have scale-free (power <br>"
        + "law) structures. <br><br>"
        + "The neighborhoods are symmetric. If cell x sees cell y as a <br>"
        + "neighbor, then cell y will see cell x as a neighbor. The <br>"
        + "neighborhoods behave as \"two way\" streets or as a bidirectional <br>"
        + "graphs. Note that the Barabási-Albert algorithm leaves many cells <br>"
        + "unconnected.  From a CA's point of view, this means that the <br>"
        + "cell is it's own neighbor and connects only to itself. <br><br>"
        + "<b>NOTE:</b> The Barabási-Albert algorithm is O(N<sup>2</sup>), so <br>"
        + "large 100 by 100 and bigger lattices are slow to construct.  <br>"
        + "Please be patient, or avoid large grids with this topology. <br><br>"
        + "Also, unlike other lattices, the neighbors are <i>not</i> <br>"
        + "guaranteed to be in any particular order. Wrap-around and<br>"
        + "reflection boundary conditions are identical in this lattice. <br><br>"
        + "To see the neighborhood associated with a cell, use the <br>"
        + "\"Show Neighborhood\" analysis."
        + "</body></html>";

    // a random number generator (with a uniform distribution)
    private Random random = null;

    /**
     * Default constructor required of all Lattices (for reflection). Typically
     * not used to build the lattice, but instead used to gain access to methods
     * such as getDisplayName() and getNumberOfNeighbors().
     */
    public RandomSmallWorldLattice()
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
    public RandomSmallWorldLattice(String initialStateFilePath, Rule rule,
        int maxHistory)
    {
        super(initialStateFilePath, rule, maxHistory);

        view = new SquareLatticeView(this);
    }

    /**
     * Connect two cells.
     */
    private void connect(Cell cell1, Cell cell2)
    {
        // get the list of cells connected to each of cell1 and cell2
        LinkedList<Cell> listOfConnectionsForCell = connections.get(cell1);
        if(listOfConnectionsForCell == null)
        {
            listOfConnectionsForCell = new LinkedList<Cell>();
        }
        LinkedList<Cell> listOfConnectionsForOtherCell = connections.get(cell2);
        if(listOfConnectionsForOtherCell == null)
        {
            listOfConnectionsForOtherCell = new LinkedList<Cell>();
        }

        // We have two lattice positions, so now connect those
        // two cells (unless they are already connected).
        if(!listOfConnectionsForCell.contains(cell2))
        {
            listOfConnectionsForCell.add(cell2);
        }
        connections.put(cell1, listOfConnectionsForCell);

        // And now make the opposite connection as well (the
        // "two-way street"). (Unless they are already
        // connected or unless there are already the max number of
        // connections.)
        if(!listOfConnectionsForOtherCell.contains(cell1))
        {
            listOfConnectionsForOtherCell.add(cell1);
        }
        connections.put(cell2, listOfConnectionsForOtherCell);
    }

    /**
     * Creates a randomized list of the cells (all cells are included, none are
     * listed twice).
     * 
     * @return
     */
    private Iterator<Cell> randomizeCells()
    {
        HashMap<Double, Cell> cellPositions = new HashMap<Double, Cell>();
        for(int i = 0; i < getHeight(); i++)
        {
            for(int j = 0; j < getWidth(); j++)
            {
                double r = random.nextDouble();
                while(cellPositions.containsKey(new Double(r)))
                {
                    r = random.nextDouble();
                }

                cellPositions.put(new Double(r), cells[i][j]);
            }
        }

        // now *order* the random numbers (hence randomizing the cells)
        Object[] keys = cellPositions.keySet().toArray();
        Arrays.sort(keys);

        // the randomized list of cells
        LinkedList<Cell> list = new LinkedList<Cell>();
        for(int i = 0; i < keys.length; i++)
        {
            list.add(cellPositions.get((Double) keys[i]));
        }

        return list.iterator();
    }

    /**
     * Makes the connections between cells.
     * 
     * @param boundaryType
     *            A constant indicating the type of boundary (wrap-around,
     *            reflection, etc). Acceptable constants are specified in the
     *            Lattice class.
     * 
     * @return a hashmap keyed by cell that gives a linked list of the
     *         neighboring cells.
     */
    private HashMap<Cell, LinkedList<Cell>> makeConnections(int boundaryType)
    {
        if(random == null)
        {
            random = RandomSingleton.getInstance();
        }

        // the number of connections between cells (in other words, the degree
        // of every cell summed together)
        int totalConnections = 0;

        // a hashmap that will hold the connections between the cells. Each cell
        // is mapped to a linked list of cells that holds the connections.
        connections = new HashMap<Cell, LinkedList<Cell>>();

        // Create a random ordering for the cells. Otherwise, the first cells
        // have an unfair advantage for being the cells with large connectivity.
        Iterator<Cell> randomCellIterator = randomizeCells();

        // Initialize the network by connecting two random cells (make sure we
        // don't grab the same cell twice)
        Cell cell1 = randomCellIterator.next();
        Cell cell2 = randomCellIterator.next();
        connect(cell1, cell2);
        totalConnections += 2;

        // add these cells to the list of previously connected cells
        if(alreadyConnectedCells == null)
        {
            alreadyConnectedCells = new ArrayList<Cell>();
        }
        else
        {
            alreadyConnectedCells.clear();
        }
        alreadyConnectedCells.add(cell1);
        alreadyConnectedCells.add(cell2);

        // now one at a time add new cells to the network (don't add the
        // initialization cells above)
        int num = 0;
        while(randomCellIterator.hasNext())
        {
            num++;

            // The next cell that will be connected
            Cell nextCell = randomCellIterator.next();

            // connect the cell to the existing cells
            //
            // choose existing cells with a weight that depends on the
            // number of connections they already have
            Iterator<Cell> iterator = alreadyConnectedCells.iterator();
            while(iterator.hasNext())
            {
                Cell previousCell = iterator.next();

                // the list of cells to which this previousCell is connected
                LinkedList<Cell> cellList = connections.get(previousCell);

                // the previously selected cell may not be connected --
                // unusual but possible. (In that case, there is zero
                // probability that a new cell will connect to it.)
                if(cellList != null)
                {
                    int numberOfConnectionsForThisCell = cellList.size();

                    double probOfConnectingToThisCell = (double) numberOfConnectionsForThisCell
                        / (double) totalConnections;

                    if(random.nextDouble() < probOfConnectingToThisCell)
                    {
                        // then connect!
                        connect(nextCell, previousCell);
                        totalConnections += 2;
                    }
                }
            }

            // add cell to list of previously connected cells
            alreadyConnectedCells.add(nextCell);
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
     * 
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
        Cell[] neighbors = null;
        if((cellneighbors == null) || (cellneighbors.size() == 0))
        {
            // all cells must be connected to at least one other cell, so
            // connect to themselves
            neighbors = new Cell[1];
            neighbors[0] = cells[row][col];
        }
        else
        {
            neighbors = new Cell[cellneighbors.size()];

            // convert the list of neighbors to an array of neighbors
            neighbors = cellneighbors.toArray(neighbors);
        }

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
