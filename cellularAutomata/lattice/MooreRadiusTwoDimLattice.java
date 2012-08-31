/*
 MooreRadiusTwoDimLattice -- a class within the Cellular Automaton Explorer. 
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
import cellularAutomata.lattice.view.SquareLatticeView;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.math.Modulus;

/**
 * A two-dimensional square lattice with wrap around boundary conditions and a
 * Moore neighborhood of radius r. Radius 1 corresponds to a nearest-neighbor
 * "square lattice (8 neighbor)". The number of neighbors (excluding the central
 * cell) is ((2r+1)^2) - 1. The wrap around conditions and neighbors are
 * implemented in the getNeighbors() method.
 * <p>
 * By convention, the neighbors start at the upper left and wrap around
 * clockwise, spiralling inwards.
 * 
 * @author David Bahr
 */
public class MooreRadiusTwoDimLattice extends SquareLattice
{
    // Note that the following constant is public so that the PropertyReader
    // class can set a default value for PropertyReader.LATTICE.
    /**
     * The display name for this lattice.
     */
    public final static String DISPLAY_NAME = "square (Moore, radius r)";

    /**
     * The radius of the neighborhood (radius = 1 corresponds to the
     * nearest-neighbor lattice). This value may be reset by the PropertyPanel.
     */
    public static int radius = 2;

    // Tells the lattice how to display its graphics.
    private LatticeView view = null;

    // The tooltip for this lattice
    private static final String TOOLTIP = "<html> <body> <b>One of the more "
        + "common geometries:</b> a square lattice <br>"
        + "with a square-shaped neighborhood that extends a radius <br>"
        + "r from the central cell. "
        + "<br><br>"
        + "A radius of 1 is the standard nearest-neighbor geometry <br>"
        + "-- see \""
        + SquareLattice.DISPLAY_NAME
        + "\". A radius of 2 is the standard <br>"
        + " next-nearest neighbor geometry -- see <br>"
        + "\""
        + NextNearestSquareLattice.DISPLAY_NAME
        + "\". "
        + "<br><br>"
        + "In general, for a central cell at (i<sub>0</sub>, j<sub>0</sub>), the neighborhood <br>"
        + "is all cells such that |i-i<sub>0</sub>| &lt= r and |j-j<sub>0</sub>| &lt= r. "
        + "<br><br>"
        + "For example, consider the cells shown below where _ is <br>"
        + "the central cell. <br>" + "<pre>" + "                12345 <br>"
        + "                ghij6 <br>" + "                fo_k7 <br>"
        + "                enml8 <br>" + "                dcba9 <br>"
        + "</pre>" + "The Moore neighborhood of radius 1 is given by <br>"
        + "the cells <br>" + "<pre>" + "                 hij  <br>"
        + "                 o_k  <br>" + "                 nml  <br>"
        + "</pre>" + "The Moore neighborhood of radius 2 is given by <br>"
        + "the cells <br>" + "<pre>" + "                12345 <br>"
        + "                ghij6 <br>" + "                fo_k7 <br>"
        + "                enml8 <br>" + "                dcba9 <br>"
        + "</pre>" + "Note the square shapes of the neighborhoods. <br><br>"
        + "To see the neighborhood associated with a cell, use the <br>"
        + "\"Show Neighborhood\" analysis. </body><html>";

    /**
     * Default constructor required of all Lattices (for reflection). Typically
     * not used to build the lattice, but instead used to gain access to methods
     * such as getDisplayName() and getNumberOfNeighbors().
     */
    public MooreRadiusTwoDimLattice()
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
    public MooreRadiusTwoDimLattice(String initialStateFilePath, Rule rule,
        int maxHistory)
    {
        super(initialStateFilePath, rule, maxHistory);

        view = new SquareLatticeView(this);

        // make sure the radius in the properties is the same as the radius
        // here.
        radius = CurrentProperties.getInstance().getNeighborhoodRadius();
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
     * On a square lattice, finds the Moore neighborhood for a cell at position
     * (row, col). Note the wrap around boundary conditions.
     * <p>
     * By convention, the neighbors start at the upper left and wrap around
     * clockwise.
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
        // get the total number of rows and columns from the super class
        int numRows = getHeight();
        int numCols = getWidth();

        int numNeighbors = getNumberOfNeighbors();

        // create an array of neighbors
        Cell[] neighboringCells = new Cell[numNeighbors];

        int neighborNumber = 0;

        // For wrap around boundary conditions...
        //
        // this spirals in, starting on the outside at the upper left. Each
        // decreasing value of r moves the spiral inward one shell.
        // E.g., when r = 2,
        // 12345
        // g___6
        // f_x_7
        // e___8
        // dcba9
        // And, when r = 1,
        // 123
        // 8x4
        // 765
        // Also note that I use % for positive numbers, but use Modulus.mod for
        // negative numbers. This ensures that I get a more typical modulus
        // behavior, rather than "remainder after division" behavior. This works
        // better for wrap around boundary conditions. (I could have used
        // Modulus.mod for positive numbers as well, but I assume that % is
        // faster.)
        for(int r = radius; r > 0; r--)
        {
            // the following "for" loops do one lap around the spiral at a
            // radius of r. Each "for" loop is one side of the spiral
            for(int j = 0; j <= 2 * r; j++)
            {
                neighboringCells[neighborNumber] = cells[Modulus.mod(row - r,
                    numRows)][Modulus.mod(col - r + j, numCols)];
                neighborNumber++;
            }
            for(int i = 1; i <= 2 * r; i++)
            {
                neighboringCells[neighborNumber] = cells[Modulus.mod(row - r
                    + i, numRows)][(col + r) % numCols];
                neighborNumber++;
            }
            for(int j = 1; j <= 2 * r; j++)
            {
                neighboringCells[neighborNumber] = cells[(row + r) % numRows][Modulus
                    .mod(col + r - j, numCols)];
                neighborNumber++;
            }
            for(int i = 1; i <= 2 * r - 1; i++)
            {
                neighboringCells[neighborNumber] = cells[Modulus.mod(row + r
                    - i, numRows)][Modulus.mod(col - r, numCols)];
                neighborNumber++;
            }
        }

        // For other boundary conditions, like reflection...
        //
        // See above loop for description of how this works. The key to
        // understanding this section is understanding the above code first.
        if(boundaryType == Lattice.REFLECTION_BOUNDARY)
        {
            neighborNumber = 0;
            for(int r = radius; r > 0; r--)
            {
                // the following "for" loops do one lap around the spiral at a
                // radius of r. Each "for" loop is one side of the spiral
                for(int j = 0; j <= 2 * r; j++)
                {
                    if(row - r < 0)
                    {
                        if(col - r + j < 0)
                        {
                            // reflected value
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row + r, numRows)][Modulus.mod(
                                col + r - j, numCols)];
                        }
                        else if(col - r + j < numCols)
                        {
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row + r, numRows)][Modulus.mod(
                                col - r + j, numCols)];
                        }
                        else if(col - r + j >= numCols)
                        {
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row + r, numRows)][Modulus.mod(
                                col + r - j, numCols)];
                        }
                    }
                    else
                    {
                        if(col - r + j < 0)
                        {
                            // reflected value
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row - r, numRows)][Modulus.mod(
                                col + r - j, numCols)];
                        }
                        else if(col - r + j < numCols)
                        {
                            // normal scenario w/o reflection
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row - r, numRows)][Modulus.mod(
                                col - r + j, numCols)];
                        }
                        else if(col - r + j >= numCols)
                        {
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row - r, numRows)][Modulus.mod(
                                col + r - j, numCols)];
                        }
                    }
                    neighborNumber++;
                }
                for(int i = 1; i <= 2 * r; i++)
                {
                    if(col + r < numCols)
                    {
                        if(row - r + i < 0)
                        {
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row + r - i, numRows)][(col + r) % numCols];
                        }
                        else if(row - r + i < numRows)
                        {
                            // normal scenario w/o reflection
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row - r + i, numRows)][(col + r) % numCols];
                        }
                        else if(row - r + i >= numRows)
                        {
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row + r - i, numRows)][(col + r) % numCols];
                        }
                    }
                    else
                    {
                        if(row - r + i < 0)
                        {
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row + r - i, numRows)][(col - r) % numCols];
                        }
                        else if(row - r + i < numRows)
                        {
                            // normal scenario w/o reflection
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row - r + i, numRows)][(col - r) % numCols];
                        }
                        else if(row - r + i >= numRows)
                        {
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row + r - i, numRows)][(col - r) % numCols];
                        }
                    }
                    neighborNumber++;
                }
                for(int j = 1; j <= 2 * r; j++)
                {
                    if(row + r < numRows)
                    {
                        if(col + r - j < 0)
                        {
                            // reflected value
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row + r, numRows)][Modulus.mod(
                                col - r + j, numCols)];
                        }
                        else if(col + r - j < numCols)
                        {
                            // normal scenario w/o reflection
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row + r, numRows)][Modulus.mod(
                                col + r - j, numCols)];
                        }
                        else if(col + r - j >= numCols)
                        {
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row + r, numRows)][Modulus.mod(
                                col - r + j, numCols)];
                        }
                    }
                    else
                    {
                        if(col + r - j < 0)
                        {
                            // reflected value
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row - r, numRows)][Modulus.mod(
                                col - r + j, numCols)];
                        }
                        else if(col + r - j < numCols)
                        {
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row - r, numRows)][Modulus.mod(
                                col + r - j, numCols)];
                        }
                        else if(col + r - j >= numCols)
                        {
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row - r, numRows)][Modulus.mod(
                                col - r + j, numCols)];
                        }
                    }
                    neighborNumber++;
                }
                for(int i = 1; i <= 2 * r - 1; i++)
                {
                    if(col - r < 0)
                    {
                        if(row + r - i < 0)
                        {
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row - r + i, numRows)][(col + r) % numCols];
                        }
                        else if(row + r - i < numRows)
                        {
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row + r - i, numRows)][(col + r) % numCols];
                        }
                        else if(row + r - i >= numRows)
                        {
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row - r + i, numRows)][(col + r) % numCols];
                        }
                    }
                    else
                    {
                        if(row + r - i < 0)
                        {
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row - r + i, numRows)][(col - r) % numCols];
                        }
                        else if(row + r - i < numRows)
                        {
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row + r - i, numRows)][(col - r) % numCols];
                        }
                        else if(row + r - i >= numRows)
                        {
                            neighboringCells[neighborNumber] = cells[Modulus
                                .mod(row - r + i, numRows)][(col - r) % numCols];
                        }
                    }
                    neighborNumber++;
                }
            }
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
        return (2 * radius + 1) * (2 * radius + 1) - 1;
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
