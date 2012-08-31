/*
 OneDimensionalFileStorage -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.lattice.Lattice;

/**
 * Writes a one-dimensional cellular automaton to a file. The two-dimensional
 * grid represents the CA as a function of time. The top row is the initial
 * state, and subsequent rows are subsequent states.
 * 
 * @author David Bahr
 */
public class OneDimensionalFileStorage extends FileStorage
{
    // file data delimiters
    private String delimiters = null;

    /**
     * Creates a file where data will be stored.
     */
    public OneDimensionalFileStorage()
    {
        super();
    }

    /**
     * Saves one-dimensional cellular automaton data.
     * 
     * @param lattice
     *            The CA lattice of cells to be saved.
     */
    public void save(Lattice lattice)
    {
        // save all the properties into the file
        saveProperties();

        // now save the data
        PrintWriter fileWriter = getFileWriter();

        // must do this here, not the constructor, because the delimiters may
        // change.
        delimiters = CurrentProperties.getInstance().getDataDelimiters();

        Iterator tempIterator = lattice.iterator();
        Cell tempCell = (Cell) tempIterator.next();
        int currentGeneration = tempCell.getGeneration();
        int maxNumStatesStored = tempCell.getStateHistory().size();

        // indicate what generations are saved
        fileWriter.println("//first generation stored: "
            + (currentGeneration - maxNumStatesStored + 1));
        fileWriter.println("//last generation stored: " + currentGeneration);

        for(int generation = currentGeneration - maxNumStatesStored + 1; generation <= currentGeneration; generation++)
        {
            // get an iterator over the lattice
            Iterator cellIterator = lattice.iterator();

            // save the value of each cell on the lattice
            while(cellIterator.hasNext())
            {
                Cell cell = (Cell) cellIterator.next();
                String state = "";
                try
                {
                    state = cell.getState(generation).toString();
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
                    fileWriter.print(delimiters);
                }
            }

            // add a new line so the next generation is printed on the next line
            fileWriter.println();
        }

        close();
    }
}
