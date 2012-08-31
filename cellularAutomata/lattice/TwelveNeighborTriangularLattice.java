/*
 TwelveNeighborTriangularLattice -- a class within the Cellular Automaton Explorer. 
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
import cellularAutomata.lattice.view.TriangularLatticeView;
import cellularAutomata.rules.Rule;

/**
 * A simple two-dimensional triangular lattice with wrap around boundary
 * conditions and 12 nearest neighbors. The wrap around conditions and neighbors
 * are implemented in the getNeighbors() method. Same as a regular triangular
 * lattice, but any cell that touches a vertex or edge is considered a neighbor.
 * (This is the same as the square lattice which traditionally has 8 nearest
 * neighbors, 4 that touch edges and 4 that touch vertices.)
 * 
 * @author David Bahr
 */
public class TwelveNeighborTriangularLattice extends TriangularLattice
{
    // Note that the following constant is public so that the PropertyReader
    // Class can set a default value for PropertyReader.LATTICE.
    /**
     * The display name for this lattice.
     */
    public final static String DISPLAY_NAME = "triangular (12 next-nearest)";

    // The tooltip for this lattice
    private static final String TOOLTIP = "<html><body> <b>A very uncommon "
        + "geometry:</b> a triangular lattice<br>"
        + "with a neighborhood constructed from all 12 cells <br>"
        + "that share either an edge or a vertex with the <br>"
        + "central cell. " + "<br><br>"
        + "In a triangular lattice, a cell \"x\" can have two <br>"
        + "different geometries depending its location: <br>" + "<pre>"
        + "               3 4   <br>" + "           1 2     5 <br>"
        + "         c     x 6   <br>" + "           b a     7 <br>"
        + "               9 8   <br>" + "</pre>" + "or <br>" + "<pre>"
        + "               2 3      <br>" + "             1     4 5  <br>"
        + "               c x    6 <br>" + "             b     8 7  <br>"
        + "               a 9     <br>" + "</pre>"
        + "In either case, the neighborhood includes all <br>"
        + "surrounding cells 1 through 9 and \"a\" through \"c\". "
        + "<br><br>"
        + "This neighborhood can be difficult to visualize.  To <br>"
        + "see the neighborhood associated with a cell, use the <br>"
        + "\"Show Neighborhood\" analysis. <br></body><html>";

    /**
     * Default constructor required of all Lattices (for reflection). Typically
     * not used to build the lattice, but instead used to gain access to methods
     * such as getDisplayName() and getNumberOfNeighbors().
     */
    public TwelveNeighborTriangularLattice()
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
    public TwelveNeighborTriangularLattice(String initialStateFilePath,
        Rule rule, int maxHistory)
    {
        super(initialStateFilePath, rule, maxHistory);

        super.setView(new TriangularLatticeView(this));
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
     * On a triangular lattice, finds the 12 nearest neighbors to a cell at the
     * specified index. Note the wrap around boundary conditions. The neighbors
     * are ordered clockwise, <b>starting </b>with the neighbor <b>closest </b>
     * to the northwest.
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
        int up2 = ((row - 2) + numRows) % numRows;
        int down2 = (row + 2) % numRows;

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

            if(row - 2 < 0)
            {
                up2 = (row + 2) % numRows;
            }
            else if(row + 2 >= numRows)
            {
                down2 = (row - 2) % numRows;
            }
        }

        // the nearest neighbors
        Cell[] neighboringCells = new Cell[12];

        // with triangular grid, the neighbors depend on the row and column
        if((row + col) % 2 == 0)
        {
            // triangle points left
            neighboringCells[0] = cells[up][left];
            neighboringCells[1] = cells[up][col];
            neighboringCells[2] = cells[up2][col];
            neighboringCells[3] = cells[up2][right];
            neighboringCells[4] = cells[up][right];
            neighboringCells[5] = cells[row][right];
            neighboringCells[6] = cells[down][right];
            neighboringCells[7] = cells[down2][right];
            neighboringCells[8] = cells[down2][col];
            neighboringCells[9] = cells[down][col];
            neighboringCells[10] = cells[down][left];
            neighboringCells[11] = cells[row][left];
        }
        else
        {
            // triangle points right
            neighboringCells[0] = cells[up][left];
            neighboringCells[1] = cells[up2][left];
            neighboringCells[2] = cells[up2][col];
            neighboringCells[3] = cells[up][col];
            neighboringCells[4] = cells[up][right];
            neighboringCells[5] = cells[row][right];
            neighboringCells[6] = cells[down][right];
            neighboringCells[7] = cells[down][col];
            neighboringCells[8] = cells[down2][col];
            neighboringCells[9] = cells[down2][left];
            neighboringCells[10] = cells[down][left];
            neighboringCells[11] = cells[row][left];
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
        return 12;
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