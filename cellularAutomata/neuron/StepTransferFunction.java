/*
 StepTransferFunction -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2008  David B. Bahr (http://academic.regis.edu/dbahr/)

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

package cellularAutomata.neuron;

/**
 * A step function (used as the transfer function for a NeuralNet).
 * 
 * @author David Bahr
 */
public final class StepTransferFunction extends NeuronTransferFunction
{
    /**
     * Builds a step function (that steps from 0 to 1 at the origin).
     */
    public StepTransferFunction()
    {
        super();
    }

    /**
     * Using a step function, calculates the neuron's output value for a given
     * summation value. The function steps from 0 to 1 at the origin.
     * 
     * @param summation
     *            The weighted sum of the neuron's inputs (including the
     *            threshold).
     */
    public double transferFunction(double summation)
    {
        return (summation > 0 ? 1.0 : 0.0);
    }
}