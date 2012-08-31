/*
 NeuralNet -- a class within the Cellular Automaton Explorer. 
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
 * The fundamental unit of a neural net. The neuron has a set of inputs, with
 * each input assigned a weight. The sum of the weighted inputs is added to a
 * threshold value and passed to a transfer function. The transfer function
 * determines what values are output by the neuron. The default transfer
 * function is a sigmoid given by 1.0/(1.0+exp(-transferSlope*summation)).
 * Alternate transfer functions may be specified in a constructor.
 * 
 * @author David Bahr
 */
public class Neuron
{
    /**
     * The default threshold assigned to the neuron if no other is specified.
     * Typically 0.0.
     */
    private final static double DEFAULT_THRESHOLD = 0.0;

    /**
     * The default transfer function for the neuron if no other is specified.
     */
    public static NeuronTransferFunction DEFAULT_TRANSFER_FUNCTION = new SigmoidTransferFunction();

    // the threshold that the neuron must exceed in order to fire.
    private double threshold;

    // the weights assigned to each input.
    private double[] weights = null;

    // the number of inputs (assigned to be the length of the weights array)
    private int numInputs;

    // the neuron's transfer function (e.g., a sigmoid, step function, etc.).
    private NeuronTransferFunction theTransferFunction = null;

    /**
     * NeuralNet with a default sigmoid transfer function (having slope 1.0 at
     * the origin) and a default threshold of 0.0. The number of inputs is
     * determined by the number of weights.
     * 
     * @param weights
     *            The array of weights used with inputs to the neuron.
     */
    public Neuron(double[] weights)
    {
        this(weights, DEFAULT_THRESHOLD);
    }

    /**
     * NeuralNet with a default sigmoid transfer function. The number of inputs
     * is determined by the number of weights.
     * 
     * @param weights
     *            The array of weights used with inputs to the neuron.
     * @param threshold
     *            The bias assigned to the neuron's summation function. In other
     *            words, if the (weighted) sum of the inputs is zero, then the
     *            neuron passes the threshold value to the transfer function.
     */
    public Neuron(double[] weights, double threshold)
    {
        this(weights, threshold, DEFAULT_TRANSFER_FUNCTION);
    }

    /**
     * The neuron has N inputs where N is the size of the array of weights. Each
     * input has an associated weight and a threshold value.
     * 
     * @param weights
     *            The array of weights assigned to the inputs of the neuron.
     * @param threshold
     *            The bias assigned to the neuron's summation function. In other
     *            words, if the (weighted) sum of the inputs is zero, then the
     *            neuron passes the threshold value to the transfer function.
     * @param theTransferFunction
     *            The function that converts the weighted sum of the inputs into
     *            the neuron's output.
     */
    public Neuron(double[] weights, double threshold,
        NeuronTransferFunction theTransferFunction)
    {
        this.weights = weights;
        this.numInputs = weights.length;
        this.theTransferFunction = theTransferFunction;

        // Look carefully at the following!
        // Why negative? Because the threshold is the value
        // that the sum of the inputs must exceed before the
        // neuron can fire. If the threshold is 0.75, then
        // the sum must be greater than 0.75. In actual code,
        // the threshold is *added* to the summation. So if
        // we use -0.75, then the inputs will exceed it and fire
        // the neuron when their total value is 0.75.
        this.threshold = -threshold;
    }

    /**
     * Calculates the output for any array of inputs. Sums the inputs (along
     * with the threshold) and then applies the transfer function.
     * 
     * @param input
     *            An array of neural inputs.
     * 
     * @return The output value of the neuron.
     */
    public double calculateOutput(double[] input)
        throws IndexOutOfBoundsException
    {
        // make sure the number of inputs is the same size as the
        // weights
        if(input.length != numInputs)
        {
            throw new IndexOutOfBoundsException(
                "Class: NeuralNet. Method: calculateOutput. "
                    + "The input array must have " + numInputs
                    + " elements so that it matches the size of the "
                    + "weights array.");
        }

        // weighted sum of the inputs (biased by the threshold)
        double summation = threshold;

        for(int i = 0; i < input.length; i++)
        {
            summation += input[i] * weights[i];
        }

        return theTransferFunction.transferFunction(summation);
    }

    /**
     * Calculates the output for any array of inputs. Sums the inputs (along
     * with the threshold) and then applies the transfer function.
     * 
     * @param input
     *            An array of neural inputs.
     * 
     * @return The output value of the neuron.
     */
    public double calculateOutput(int[] input) throws IndexOutOfBoundsException
    {
        // cast the integers to doubles
        double[] doubleInputs = new double[input.length];
        for(int i = 0; i < doubleInputs.length; i++)
        {
            doubleInputs[i] = (double) input[i];
        }

        return calculateOutput(doubleInputs);
    }

    /**
     * The number of inputs to the neuron.
     * 
     * @return The number of inputs.
     */
    public int getNumberOfInputs()
    {
        return numInputs;
    }

    /**
     * The threshold assigned to the neuron.
     * 
     * @return The neuron's threshold.
     */
    public double getThreshold()
    {
        return threshold;
    }

    /**
     * The transfer function assigned to the neuron.
     * 
     * @return The neuron's transfer function.
     */
    public NeuronTransferFunction getTransferFunction()
    {
        return theTransferFunction;
    }

    /**
     * The transferSlope assigned to the neuron. This is the slope of the
     * transfer function at the origin.
     * 
     * @return The neuron's transfer slope.
     */
    public double getTransferSlope()
    {
        return theTransferFunction.getTransferSlope();
    }

    /**
     * The weights assigned to the neuron's inputs.
     * 
     * @return The weights on the neuron's inputs.
     */
    public double[] getWeights()
    {
        return weights;
    }
}