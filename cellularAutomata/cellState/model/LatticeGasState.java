/*
 LatticeGasState -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.cellState.model;

import cellularAutomata.cellState.model.IntegerVectorState;

/**
 * State for hexagonal lattice gas automatons. Is a vector of length 7. Six
 * array positions are for particles (a 0 means a particle is not present and a
 * 1 means a particle is present). The last array position indicates whether or
 * not the state is a wall (a 0 if it is not a wall and a 1 if it is a wall).
 * 
 * <br>
 * This code assumes hexagonal lattices.
 * 
 * @author David Bahr
 */
public class LatticeGasState extends IntegerVectorState
{
    // the lattice gas vector length
    private static int vectorLength = 7;

    /**
     * Note that this constructor calls the non-default super constructor to
     * ensure that the vector length is correctly set. The program will crash
     * otherwise (when it gets a 0 length vector).
     */
    public LatticeGasState()
    {
        super(createDefaultArray(), 0, 1);
    }

    /**
     * Creates a vector state with permissible values of 0 and 1.
     */
    public LatticeGasState(int[] state)
    {
        super(state, 0, 1);
    }

    /**
     * Returns a random lattice gas state. The first six array positions
     * indicate particles, and the last position indicates if it is a wall. For
     * this method, the returned state is never a wall.
     * 
     * @param probability
     *            The probability that a particle is present in each of the
     *            state's 6 directions.
     * 
     * @return a random lattice gas state.
     */
    protected int[] getRandomState(double probability)
    {
        int[] state = super.getRandomState(probability);

        // not a wall
        for(int i = 6; i < vectorLength; i++)
        {
            state[i] = 0;
        }

        return state;
    }

    /**
     * Creates a simple array of the correct length for use in the constructor.
     */
    private static int[] createDefaultArray()
    {
        int[] array = {0, 0, 0, 0, 0, 0, 0};
        return array;
    }

    /**
     * Checks to see if a state is blank (all 0's).
     * 
     * @param state
     *            The state being checked.
     */
    private boolean isBlank(int[] state)
    {
        int sum = 0;
        for(int i = 0; i < vectorLength; i++)
        {
            sum += state[i];
        }

        boolean isBlank = (sum == 0 ? true : false);

        return isBlank;
    }

    /**
     * Creates a clone of this cellState; this method must return a different
     * instance of the cell state, but with all the same values.
     * <p>
     * The intent is that, for any CellState x, the expression:
     * <code> x.clone() !=  x </code> will be true, and that the expression:
     * <code> x.clone().getClass() == x.getClass() </code> will be true. Also:
     * <code> x.clone().equals(x) </code> will be true.
     * <p>
     * (Note this method is used in places where we need a copy of the cell's
     * state but that the same instance would cause unpredictable or incorrect
     * behavior.)
     * 
     * @return A unique copy of the cell's state (must not return "this"
     *         object).
     */
    public CellState clone()
    {
        // note that this clones the array as well! Very important.
        return new LatticeGasState((int[]) ((int[]) super.getValue()).clone());
    }

    /**
     * Overrides the parent class to test if the CellState is "alternate" which
     * in this case is a wall.
     * 
     * @return true if the state is alternate (a wall).
     * 
     * @see cellularAutomata.cellState.model.CellState#setToAlternateState()
     */
    public boolean isAlternate()
    {
        int[] vector = (int[]) getValue();
        return (vector[6] == 1);
    }

    /**
     * Tests if a given CellState is "full". A full state is anything other than
     * an empty state. This differs somewhat from most other CA, where "full"
     * would indicate a maximum possible value. In this case, if any site has a
     * 1, then it is full (excluding the last array position, the wall site).
     * 
     * @return true if the state is full.
     * 
     * @see cellularAutomata.cellState.model.LatticeGasState#setToFullState()
     */
    public boolean isFull()
    {
        boolean full = false;
        int[] vector = (int[]) getValue();

        // subtract 1 from the length because we aren't interested in the wall
        // site
        for(int i = 0; i < vectorLength - 1; i++)
        {
            if(vector[i] != 0)
            {
                full = true;
            }
        }

        return full;
    }

    /**
     * Overrides the parent class method to sets an alternate state which is a
     * wall.
     */
    public void setToAlternateState()
    {
        // in a lattice gas, the 7th array site indicates the wall.
        int[] wallState = new int[vectorLength];
        for(int i = 0; i < vectorLength; i++)
        {
            wallState[i] = 0;
        }

        // make it a wall
        wallState[6] = 1;

        super.setValue(wallState);
    }

    /**
     * Overrides the parent class method because in a lattice gas, a "full"
     * state just means that a site has at least one particle. So we set
     * particles randomly, but make sure it isn't all 0's.
     */
    public void setToFullState()
    {
        // in a lattice gas, the full state just means that a site has a
        // particle. So we set it randomly, but make sure it isn't all 0's.
        double probability = 0.5;
        int[] fullState = getRandomState(probability);
        while(isBlank(fullState))
        {
            fullState = getRandomState(probability);
        }

        super.setValue(fullState);
    }

    /**
     * Sets a random integer vector for this cell state.
     * 
     * @param probability
     *            The probability that a particle is present in each of the
     *            state's 6 directions.
     */
    public void setToRandomState(double probability)
    {
        int[] randomState = getRandomState(probability);
        super.setValue(randomState);
    }
}
