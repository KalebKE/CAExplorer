/*
 NextNearestOneDimLattice -- a class within the Cellular Automaton Explorer. 
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
import cellularAutomata.lattice.view.OneDimensionalLatticeView;
import cellularAutomata.rules.Rule;

/**
 * A standard one-dimensional lattice with nearest and next-nearest neighbors,
 * and wrap around boundary conditions.
 * 
 * @author David Bahr
 */
public class NextNearestOneDimLattice extends OneDimensionalLattice
{
    // Note that the following constant is public so that the ControlPanel
    // class can set a default value for displaying rules associated with this
    // lattice.
    /**
     * The display name for this lattice.
     */
    public final static String DISPLAY_NAME = "one-dim (4 next-nearest)";

    // Tells the lattice how to display its graphics.
    private LatticeView view = null;

    // The tooltip for this lattice
    private static final String TOOLTIP = "<html><body> <b>A less common one-dimensional "
        + "geometry:</b> a linear lattice <br>"
        + "with four next-nearest neighbors."
        + "<br><br>"
        + "Consider the following cells (from within a line of cells). <br>"
        + "<pre>"
        + "          ...vwxyz... <br>"
        + "</pre>"
        + "The neighborhood of x includes v, w, y, and z. <br><br>"
        + "To see the neighborhood associated with a cell, use the <br>"
        + "\"Show Neighborhood\" analysis.</body><html>";

    /**
     * Default constructor required of all Lattices (for reflection). Typically
     * not used to build the lattice, but instead used to gain access to methods
     * such as getDisplayName() and getNumberOfNeighbors().
     */
    public NextNearestOneDimLattice()
    {
        super();
    }

    /**
     * Create a one-dimensional cellular automaton with nearest and next-nearest
     * neighbors. The length of the automaton and the initial state of each cell
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
    public NextNearestOneDimLattice(String initialStateFilePath, Rule rule,
        int maxHistory)
    {
        super(initialStateFilePath, rule, maxHistory);

        view = new OneDimensionalLatticeView(this);
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
     * Finds the next-nearest and nearest neighbors to a cell at the specified
     * index. Note the wrap around boundary conditions.
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
        // the length should be at least 2.
        int length = getWidth();

        // Wrap around boundary condition (the default)
        //
        // No negative indices. Note that the 2*length is just in case a lattice
        // of length one sneaks in somehow. Ensures a positive value.
        int left = ((index - 1) + length) % length;
        int right = (index + 1) % length;
        int nextLeft = ((index - 2) + 2 * length) % length;
        int nextRight = (index + 2) % length;

        // other boundary conditions (e.g., reflection)
        if(boundaryType == Lattice.REFLECTION_BOUNDARY)
        {
            if(index - 1 < 0)
            {
                left = (index + 1) % length;
            }
            else if(index + 1 >= length)
            {
                right = (index - 1) % length;
            }

            if(index - 2 < 0)
            {
                nextLeft = (index + 2) % length;
            }
            else if(index + 2 >= length)
            {
                nextRight = (index - 2) % length;
            }
        }

        // the nearest neighbors
        Cell[] neighboringCells = {(Cell) this.get(nextLeft),
            (Cell) this.get(left), (Cell) this.get(right),
            (Cell) this.get(nextRight)};

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
        return 4;
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
