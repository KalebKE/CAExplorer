/*
 TriangularLattice -- a class within the Cellular Automaton Explorer. 
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
import cellularAutomata.lattice.view.LatticeView;
import cellularAutomata.lattice.view.TriangularLatticeView;
import cellularAutomata.rules.Rule;

/**
 * A simple two-dimensional triangular lattice with wrap around boundary
 * conditions and 3 nearest neighbors. The wrap around conditions and neighbors
 * are implemented in the getNeighbors() method.
 * 
 * @author David Bahr
 */
public class TriangularLattice extends TwoDimensionalLattice
{
    // Note that the following constant is public so that the PropertyReader
    // Class can set a default value for PropertyReader.LATTICE.
    /**
     * The display name for this lattice.
     */
    public final static String DISPLAY_NAME = "triangular (3 neighbor)";

    // Tells the lattice how to display its graphics.
    private LatticeView view = null;

    // The tooltip for this lattice
    private static final String TOOLTIP = "<html> <body><b>An uncommon "
        + "geometry:</b> a triangular lattice with<br>"
        + "all 3 nearest neighbors (the 3 neighboring <br>"
        + "triangles that share an edge with the central <br>"
        + "cell's triangle). " + "<br><br>"
        + "In a triangular lattice, a cell \"x\" can have two <br>"
        + "different geometries depending on its location: <br>" + "<pre>"
        + "             1     <br>" + "               x 2 <br>"
        + "             3     <br>" + "</pre>" + "or <br>" + "<pre>"
        + "                 1 <br>" + "             3 x   <br>"
        + "                 2 <br>" + "</pre>"
        + "In either case, the neighborhood includes all <br>"
        + "surrounding cells 1 through 3. <br><br>"
        + "To see the neighborhood associated with a cell, use the <br>"
        + "\"Show Neighborhood\" analysis.</body><html>";

    /**
     * Default constructor required of all Lattices (for reflection). Typically
     * not used to build the lattice, but instead used to gain access to methods
     * such as getDisplayName() and getNumberOfNeighbors().
     */
    public TriangularLattice()
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
    public TriangularLattice(String initialStateFilePath, Rule rule,
        int maxHistory)
    {
        super(initialStateFilePath, rule, maxHistory);

        view = new TriangularLatticeView(this);
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
     * On a triangular lattice, finds the nearest neighbors to a cell at the
     * specified index. Note the wrap around boundary conditions. The neighbors
     * are ordered clockwise, <b>starting </b>with the neighbor <b>closest </b>
     * to the northwest (for some cells that will be the neighbor to the
     * northeast, and for other cells that will be the neighbor to the
     * northwest).
     * <p>
     * The following diagram depicts a cell and its neighbors. There are two
     * scenarios. The triangle points right and the triangle points left. The
     * following diagram depicts the center points of the cell and its three
     * neighbors.
     * 
     * <pre>
     *               
     *                    Triangle points to the right.
     *                      (neighbor 0)   *
     *                                        *   *  (neighbor 1)
     *                      (neighbor 2)   *
     *                   
     *                    Triangle points to the left.
     *                                            * (neighbor 0)
     *                      (neighbor 2)   *   *  
     *                                            * (neighbor 1)
     *                
     * </pre>
     * 
     * @param row
     *            The cell's horizontal position in the array.
     * @param col
     *            The cell's vertical position in the array.
     * @param boundaryType
     *            A constant indicating the type of boundary (wrap-around,
     *            reflection, etc). Acceptable constants are specified in the
     *            Lattice class.
     * 
     * @return An array of neighboring cells.
     */
    protected Cell[] getNeighboringCells(int row, int col, int boundaryType)
    {
        int numCols = getWidth();
        int numRows = getHeight();

        // wrap around boundary (the default)
        // no negative indices (add arrayLength)
        int left = ((col - 1) + numCols) % numCols;
        int right = (col + 1) % numCols;
        int up = ((row - 1) + numRows) % numRows;
        int down = (row + 1) % numRows;

        // other boundary conditions (e.g., reflection)
        if(boundaryType == Lattice.REFLECTION_BOUNDARY)
        {
            if(col - 1 < 0)
            {
                left = (col + 1) % numCols;
            }
            else if(col + 1 >= numCols)
            {
                right = (col - 1) % numCols;
            }

            if(row - 1 < 0)
            {
                up = (row + 1) % numRows;
            }
            else if(row + 1 >= numRows)
            {
                down = (row - 1) % numRows;
            }
        }

        // the nearest neighbors
        Cell[] neighboringCells = new Cell[3];

        // with triangular grid, the neighbors depend on the row and column
        if((row + col) % 2 == 0)
        {
            // triangle points left (diagram depicts the center points of the
            // cell and its three neighbors as *'s -- the orientation appears
            // different than the actual triangle.)
            // *
            // _* *
            // *
            neighboringCells[0] = cells[up][col];
            neighboringCells[1] = cells[row][right];
            neighboringCells[2] = cells[down][col];
        }
        else
        {
            // triangle points right (diagram depicts the center points of the
            // cell and its three neighbors -- the orientation appears
            // different than the actual triangle.)
            // ___*
            // * *
            // ___*
            neighboringCells[0] = cells[up][col];
            neighboringCells[1] = cells[down][col];
            neighboringCells[2] = cells[row][left];
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