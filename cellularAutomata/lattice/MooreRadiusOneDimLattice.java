/*
 MooreRadiusOneDimLattice -- a class within the Cellular Automaton Explorer. 
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
import cellularAutomata.lattice.view.OneDimensionalLatticeView;
import cellularAutomata.lattice.view.SquareLatticeView;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.math.Modulus;

/**
 * A one-dimensional square lattice with wrap around boundary conditions and a
 * Moore neighborhood of radius r. Radius 1 corresponds to a nearest-neighbor
 * "one-dim (2 neighbor)". The number of neighbors (excluding the central cell)
 * is 2r. The wrap around conditions and neighbors are implemented in the
 * getNeighbors() method.
 * <p>
 * By convention, the neighbors start at the far left and move to the right.
 * 
 * @author David Bahr
 */
public class MooreRadiusOneDimLattice extends OneDimensionalLattice
{
    // Note that the following constant is public so that the PropertyReader
    // class can set a default value for PropertyReader.LATTICE.
    /**
     * The display name for this lattice.
     */
    public final static String DISPLAY_NAME = "one-dim (radius r)";

    /**
     * The radius of the neighborhood (radius = 1 corresponds to the
     * nearest-neighbor lattice). This value may be reset by the PropertyPanel.
     */
    public static int radius = 2;

    // Tells the lattice how to display its graphics.
    private LatticeView view = null;

    // The tooltip for this lattice
    private static final String TOOLTIP = "<html><body> <b>An uncommon one-dimensional "
        + "geometry:</b> a linear lattice <br>"
        + "with neighbors out to some radius r.  Sometimes called a <br>"
        + "one-dimensional Moore neighborhood with radius r."
        + "<br><br>"
        + "A radius of 1 is the standard nearest-neighbor geometry -- see<br>"
        + " \""
        + StandardOneDimensionalLattice.DISPLAY_NAME
        + "\". A radius of 2 is the standard <br>"
        + " next-nearest neighbor geometry -- see \""
        + NextNearestOneDimLattice.DISPLAY_NAME
        + "\". "
        + "<br><br>"
        + "Consider the following cells with central cell x. <br>"
        + "<pre>"
        + "          ...1234x5678... <br>"
        + "</pre>"
        + "The radius 1 neighborhood is given by 4 and 5. <br>"
        + "<pre>"
        + "          ...4x5... <br>"
        + "</pre>"
        + "The radius 2 neighborhood is given by 3, 4, 5, and 6. <br>"
        + "<pre>"
        + "          ...34x56... <br>"
        + "</pre>"
        + "The radius 3 neighborhood is given by 2, 3, 4, 5, 6, <br>"
        + "and 7. <br>"
        + "<pre>"
        + "          ...234x567... <br>"
        + "</pre>"
        + "Etc. <br><br>"
        + "To see the neighborhood associated with a cell, use the <br>"
        + "\"Show Neighborhood\" analysis.</body><html>";

    /**
     * Default constructor required of all Lattices (for reflection). Typically
     * not used to build the lattice, but instead used to gain access to methods
     * such as getDisplayName() and getNumberOfNeighbors().
     */
    public MooreRadiusOneDimLattice()
    {
        super();
    }

    /**
     * Create a one-dimensional cellular automaton with the same rule at all
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
    public MooreRadiusOneDimLattice(String initialStateFilePath, Rule rule,
        int maxHistory)
    {
        super(initialStateFilePath, rule, maxHistory);

        view = new OneDimensionalLatticeView(this);

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
     * Finds the neighbors to a cell at the specified index. Note the wrap
     * around boundary conditions.
     * 
     * @param index
     *            The cell's index.
     * @param boundaryType
     *            A constant indicating the type of boundary (wrap-around,
     *            reflection, etc). Acceptable constants are specified in the
     *            Lattice class.
     * 
     * @return An array of neighboring cells.
     */
    protected Cell[] getNeighboringCells(int index, int boundaryType)
    {
        // get the total number of columns from the super class
        int numCols = getWidth();

        int numNeighbors = getNumberOfNeighbors();

        // create an array of neighbors
        Cell[] neighboringCells = new Cell[numNeighbors];

        // get each of the neighbors, from left to right
        int neighborNumber = 0;
        for(int r = -radius; r <= radius; r++)
        {
            // don't include the cell itself
            if(r != 0)
            {
                // wrap around boundary condition (the default)
                // assign the neighbor by grabbing it from the arrayList
                neighboringCells[neighborNumber] = (Cell) this.get(Modulus.mod(
                    index + r, numCols));

                // other boundary conditions (e.g., reflection)
                if(boundaryType == Lattice.REFLECTION_BOUNDARY)
                {
                    if(index + r < 0 || index + r >= numCols)
                    {
                        neighboringCells[neighborNumber] = (Cell) this
                            .get(Modulus.mod(index - r, numCols));
                    }
                }

                neighborNumber++;
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
        return 2 * radius;
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
