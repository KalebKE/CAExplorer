/*
 NeuronTransferFunction -- a class within the Cellular Automaton Explorer. 
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
 * The transfer function required in the construction of a NeuralNet.
 * 
 * @author David Bahr
 */
public abstract class NeuronTransferFunction
{
    /**
     * The default transfer slope assigned to the neuron if no other is
     * specified. Typically 1.0. The slope is only relevant if the transfer
     * function uses a slope.
     */
    private final static double DEFAULT_TRANSFERSLOPE = 1.0;

    // the slope of the transfer function
    private double transferSlope = DEFAULT_TRANSFERSLOPE;

    /**
     * Builds a transfer function with a default slope at the origin.
     */
    public NeuronTransferFunction()
    {
        super();
    }

    /**
     * Builds a transfer function with the specified slope at the origin.
     * 
     * @param transferSlope
     *            The slope of the transfer function at the origin.
     */
    public NeuronTransferFunction(double transferSlope)
    {
        // make sure have valid transfer slope (slope of the
        // transfer function)
        if(transferSlope > 0.0)
        {
            this.transferSlope = transferSlope;
        }
        else
        {
            throw new IllegalArgumentException(
                "NeuronTransferFunction: constructor:: "
                    + " The transferSlope must be greater than 0.0.");
        }
    }

    /**
     * The transfer function calculates the neuron's output value for a given
     * summation value. Typical functions are a step function, tanh, or a
     * sigmoid given by 1.0/(1.0+exp(-transferSlope*summation)). The transfer
     * function's output is never less than 0.0 and never greater than 1.0.
     * 
     * @param summation
     *            The weighted sum of the neuron's inputs (including the
     *            threshold).
     * 
     * @return The neuron's output value.
     */
    public abstract double transferFunction(double summation);

    /**
     * The slope of the transfer function at 0.0. This is the slope of the
     * transfer function at the origin.
     * 
     * @return The neuron's transfer slope.
     */
    public double getTransferSlope()
    {
        return transferSlope;
    }

}