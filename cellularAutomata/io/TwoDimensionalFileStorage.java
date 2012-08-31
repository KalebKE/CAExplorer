/*
 TwoDimensionalFileStorage -- a class within the Cellular Automaton Explorer. 
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
package cellularAutomata.io;

import java.io.PrintWriter;
import java.util.Iterator;

import cellularAutomata.CurrentProperties;
import cellularAutomata.Cell;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.TwoDimensionalLattice;

/**
 * Writes the final state of a two-dimensional cellular automaton to a file.
 * 
 * @author David Bahr
 */
public class TwoDimensionalFileStorage extends FileStorage
{
    // the number of generations that will be saved
    int numGenerationsToSave = 1;

    // file data delimiters
    private String delimeters = null;

    /**
     * Creates a file where data will be stored.
     * 
     * @param numGenerationsToSave
     *            The number of generations that will be saved.
     */
    public TwoDimensionalFileStorage(
        int numGenerationsToSave)
    {
        super();

        this.numGenerationsToSave = numGenerationsToSave;
    }

    /**
     * Saves two-dimensional cellular automaton data.
     * 
     * @param lattice
     *            The CA lattice of cells to be saved.
     */
    public void save(Lattice lattice)
    {
        // save all the properties into the file
        saveProperties();
        
        //now save the data
        PrintWriter fileWriter = getFileWriter();

        // get an iterator over the lattice
        Iterator cellIterator = lattice.iterator();

        TwoDimensionalLattice twoLattice = (TwoDimensionalLattice) lattice;

        // must do this here, not the constructor, because the delimiters may
        // change.
        delimeters = CurrentProperties.getInstance().getDataDelimiters();

        // the generation being saved
        int firstGen = 0;

        // the last generation to save
        int lastGeneration = 0;

        // get values for the first and last generations to save
        Iterator iter = lattice.iterator();
        lastGeneration = ((Cell) iter.next()).getGeneration();
        firstGen = lastGeneration - (numGenerationsToSave - 1);

        // write the data once for each generation
        for(int generation = firstGen; generation <= lastGeneration; generation++)
        {
            fileWriter.println("//generation " + generation);

            // get each cell
            for(int i = 0; i < twoLattice.getHeight(); i++)
            {
                for(int j = 0; j < twoLattice.getWidth(); j++)
                {
                    Cell cell = (Cell) cellIterator.next();

                    // a 1 or a 0 for that position in the current generation
                    String state = "";
                    try
                    {
                        CellState cellState = cell.getState(generation);
                        if(state != null)
                        {
                            state = cellState.toString();
                        }
                    }
                    catch(Exception e)
                    {
                        // do nothing
                    }

                    // write the value in the state
                    fileWriter.print(state);

                    // add delimiters
                    if(cellIterator.hasNext())
                    {
                        fileWriter.print(delimeters);
                    }
                }

                // add a new line so the next row is printed on the next line
                fileWriter.println();
            }

            // need to iterate over the lattice again
            if(numGenerationsToSave > 1)
            {
                // reset the iterator
                cellIterator = lattice.iterator();
            }
        }

        close();
    }
}
