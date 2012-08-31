/*
 SigmoidTransferFunction -- a class within the Cellular Automaton Explorer. 
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
 * A sigmoid function given by 1.0/(1.0+exp(-transferSlope*summation)). (Used as
 * the transfer function for a NeuralNet).
 * 
 * @author David Bahr
 */
public final class SigmoidTransferFunction extends NeuronTransferFunction
{
    /**
     * Builds a sigmoid transfer function with a default slope of 1.0 at the
     * origin.
     */
    public SigmoidTransferFunction()
    {
        super(1.0);
    }

    /**
     * Builds a sigmoid transfer function with the specified slope at the
     * origin. (In other words, the value of the derivative at the origin is
     * given by the transferSlope).
     * 
     * @param transferSlope
     *            The slope at the origin.
     */
    public SigmoidTransferFunction(double transferSlope)
    {
        super(transferSlope);
    }

    /**
     * Using a sigmoid function, calculates the neuron's output value for a
     * given summation value. Assumes that the input summation can be both
     * positive or negative.
     * 
     * @param summation
     *            The weighted sum of the neuron's inputs (including the
     *            threshold) which can be both positive or negative.
     * 
     * @return The neuron's output value (between 0.0 and 1.0).
     */
    public double transferFunction(double summation)
    {
        // the 4.0 ensures that the slope at the origin is given by the transfer
        // slope. If s(sum) is the sigmoid function, then the derivative is
        // given by "4.0 * transferSlope * s(sum) * (1 - s(sum))". At sum = 0,
        // this is 4.0 * transferSlope * 1/2 * 1/2 = transferSlope.
        return 1.0 / (1.0 + StrictMath.exp(-4.0 * getTransferSlope()
            * summation));
    }
}