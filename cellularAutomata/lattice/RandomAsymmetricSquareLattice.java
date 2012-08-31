/*
 RandomAsymmetricSquareLattice -- a class within the Cellular Automaton Explorer. 
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
import java.util.Random;

import cellularAutomata.Cell;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.math.RandomSingleton;
import cellularAutomata.lattice.view.LatticeView;
import cellularAutomata.lattice.view.SquareLatticeView;

/**
 * A simple two-dimensional square lattice with random neighbors for each cell.
 * The neighborhoods are asymmetric. Cell x may see cell y as a neighbor, but
 * cell y may not see cell x as a neighbor. The neighborhoods behave "one way"
 * or as a directed graph.
 * <p>
 * The wrap around/reflection boundary conditions and choice of neighbors are
 * implemented in the getNeighbors() method.
 * 
 * @author David Bahr
 */
public class RandomAsymmetricSquareLattice extends SquareLattice
{
    // Note that the following constant is public so that the PropertyReader
    // Class can set a default value for PropertyReader.LATTICE.
    /**
     * The display name for this lattice.
     */
    public final static String DISPLAY_NAME = "square (random asymmetric)";

    /**
     * The maximum number of neighbors allowed for each cell.
     */
    public final static int MAX_NUMBER_OF_NEIGHBORS = 50;

    // Tells the lattice how to display its graphics.
    private LatticeView view = null;

    // The tooltip for this lattice
    private static final String TOOLTIP = "<html> <body>"
        + "<b>An uncommon topology:</b> a square lattice with each cell <br>"
        + "having random neighbors.  In other words, each cell will have <br>"
        + "some random number of neighbors positioned in random places. <br>"
        + "The maximum number of neighbors allowed each cell is "
        + MAX_NUMBER_OF_NEIGHBORS
        + " or <br>"
        + "(rows * cols) - 1, whichever is less. All cells have at least <br>"
        + "one neighbor. <br><br>"
        + "Note that the neighborhoods are asymmetric.  Cell x may see <br>"
        + "cell y as a neighbor, but cell y may not see cell x as a neighbor. <br>"
        + "The neighborhoods behave \"one way\" or as a directed graph. <br><br>"
        + "<b>NOTE:</b> Unlike other lattices, the neighbors are <i>not</i> guaranteed <br>"
        + "to be in any particular order. <br><br>"
        + "To see the neighborhood associated with a cell, use the <br>"
        + "\"Show Neighborhood\" analysis." + "</body></html>";

    // a random number generator
    private Random random = null;

    /**
     * Default constructor required of all Lattices (for reflection). Typically
     * not used to build the lattice, but instead used to gain access to methods
     * such as getDisplayName() and getNumberOfNeighbors().
     */
    public RandomAsymmetricSquareLattice()
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
    public RandomAsymmetricSquareLattice(String initialStateFilePath,
        Rule rule, int maxHistory)
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
        if(random == null)
        {
            random = RandomSingleton.getInstance();
        }

        int maxNumNeighbors = MAX_NUMBER_OF_NEIGHBORS;
        if(maxNumNeighbors > getHeight() * getWidth())
        {
            // don't let the number of neighbors be larger than the lattice
            maxNumNeighbors = getHeight() * getWidth() - 1;
        }

        // How many neighbors will there be? (between 1 and
        // MAX_NUMBER_OF_NEIGHBORS inclusive)
        int numberOfNeighbors = 1 + random.nextInt(MAX_NUMBER_OF_NEIGHBORS);

        // the neighbors we will return
        Cell[] neighbors = new Cell[numberOfNeighbors];

        // fill a list with the neighbors
        HashMap<Integer, Cell> neighborList = new HashMap<Integer, Cell>();
        for(int num = 0; num < numberOfNeighbors; num++)
        {
            int neighborRow = random.nextInt(getHeight());
            int neighborCol = random.nextInt(getWidth());

            // exclude the cell itself and any previously selected cells, but
            // allow all other cells
            while(((neighborRow == row) && (neighborCol == col))
                || neighborList.containsValue(cells[neighborRow][neighborCol]))
            {
                neighborRow = random.nextInt(getHeight());
                neighborCol = random.nextInt(getWidth());
            }

            neighborList.put(new Integer(num), cells[neighborRow][neighborCol]);
        }

        // convert the neighbor list to an array
        neighbors = neighborList.values().toArray(neighbors);

        // return the random neighbors to the cell
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
