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

package cellularAutomata.rules;

import cellularAutomata.neuron.Neuron;
import cellularAutomata.neuron.NeuronTransferFunction;
import cellularAutomata.neuron.SigmoidTransferFunction;
import cellularAutomata.rules.templates.IntegerRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;

/**
 * A rule that treats each cell as a neuron. The neighbors are the inputs for
 * the neuron.
 * 
 * @author David Bahr
 */
public class NeuralNet extends IntegerRuleTemplate
{
    // a display name for this class
    private static final String RULE_NAME = "Neural Net";

    // a description of property choices that give the best results for this
    // rule (e.g., which lattice, how many states, etc.)
    private static final String BEST_RESULTS = "<html> <body><b>"
        + RULE_NAME
        + ".</b>"
        + "<p> "
        + "<b>For best results</b>, try 5 states with an 80% random initial population "
        + "on a 200 by 200 square (8 neighbor) lattice. "
        + "<p>"
        + "Also try larger numbers of states, BUT keep in mind that the "
        + "results are very sensitive to the % random initial condition. Ideally "
        + "there should be an equal number of every state.  Therefore, keep the % "
        + "random near the value 100 - (100/N) where N is the number of states. "
        + "<p>"
        + "After achieving equilibrium with a larger numbers of states (like 10), "
        + "try drawing empty and filled cells in the colored areas.  These will grow "
        + "to fill all available space." + "<p>"
        + "Fewer states are less interesting. " + leftClickInstructions
        + rightClickInstructions + "</body></html>";

    // a tooltip description for this class
    private String TOOLTIP = "<html> <body><b>"
        + RULE_NAME
        + ".</b> Creates a neural net where each cell is a neuron.</body></html>";

    /**
     * Create a neural net using the given cellular automaton properties.
	 * <p>
	 * When calling the parent constructor, the minimalOrLazyInitialization
	 * parameter must be included as
	 * <code>super(minimalOrLazyInitialization);</code>. The boolean is
	 * intended to indicate when the constructor should build a rule with as
	 * small a footprint as possible. In order to load rules by reflection, the
	 * application must query this class for information like the display name,
	 * tooltip description, etc. At these times it makes no sense to build the
	 * complete rule which may have a large footprint in memory.
	 * <p>
	 * It is recommended that the constructor and instance variables do not
	 * initialize any memory intensive variables and that variables be
	 * initialized only when first needed (lazy initialization). Or all
	 * initializations in the constructor may be placed in an <code>if</code>
	 * statement.
	 * 
	 * <pre>
	 * if(!minimalOrLazyInitialization)
	 * {
	 *     ...initialize
	 * }
	 * </pre>
	 * 
	 * @param minimalOrLazyInitialization
	 *            When true, the constructor instantiates an object with as
	 *            small a footprint as possible. When false, the rule is fully
	 *            constructed. This variable should be passed to the super
	 *            constructor <code>super(minimalOrLazyInitialization);</code>,
	 *            but if uncertain, you may safely ignore this variable.
     */
    public NeuralNet(boolean minimalOrLazyInitialization)
    {
        super(minimalOrLazyInitialization);
    }

    /**
     * Each neighbor is treated as an input to a neuron with a sigmoid transfer
     * function with slope 1. the output of the neuron is rounded to the nearest
     * integer and returned as the new cell value.
     * <p>
     * Each neighbor has a value between 0 and numStates-1. The sigmoid is
     * centered around the origin, so each input (neighbor value) is rescaled to
     * be between -(numStates-1)/2 and (numStates-1)/2.
     * 
     * @param cell
     *            The value of the cell being updated.
     * @param neighbors
     *            The value of the neighbors.
     * @param generation
     *            The current generation of the CA.
     * 
     * @return A new state for the cell.
     */
    protected int integerRule(int cell, int[] neighbors, int numStates,
        int generation)
    {
        // sets the neuron's weights
        double[] weights = new double[neighbors.length];
        for(int i = 0; i < weights.length; i++)
        {
            weights[i] = 1.0;
            // weights[i] = random.nextDouble() * 2.0 - 1.0;
        }

        // rescales the inputs to be between
        // -(numStates-1)/2 and (numStates-1)/2.
        double[] rescaledNeighbors = new double[neighbors.length];
        for(int i = 0; i < rescaledNeighbors.length; i++)
        {
            rescaledNeighbors[i] = (double) neighbors[i]
                - ((numStates - 1.0) / 2.0);
        }

        // The slope of the transfer function at the origin.
        // The following empirically derived formula seems to keep the slope
        // close to a critical value with the most interesting behavior.
        double slope = 1.0 / ((neighbors.length / 2.0) * (double) numStates);

        // Good with random weights: double slope = 0.1425;

        // a transfer function for a neuron.
        NeuronTransferFunction transferFunction = new SigmoidTransferFunction(
            slope);

        // the neuron for this cell (created with the specified weights, the
        // transfer function, and a threshold of 0.0)
        Neuron neuron = new Neuron(weights, 0.0, transferFunction);

        // calculate the output of the neuron with the neighbors used as inputs
        double neuronOutput = neuron.calculateOutput(rescaledNeighbors);

        // scale the result to be between 0 and numStates - 1
        neuronOutput *= (numStates - 1);

        // round to the nearest integer (0.0 < neuronOutput < numStates-1, but
        // will never be equal to the bounds).
        int cellValue = (int) Math.round(neuronOutput);

        return cellValue;
    }

    /**
     * A brief description (written in HTML) that describes what parameters will
     * give best results for this rule (which lattice, how many states, etc).
     * The description will be displayed on the properties panel. Using html
     * permits line breaks, font colors, etcetera, as described in HTML
     * resources. Regular line breaks will not work.
     * <p>
     * Recommend starting with the title of the rule followed by "For best
     * results, ...". See Rule 102 for an example.
     * 
     * @return An HTML string describing how to get best results from this rule.
     *         May be null.
     */
    public String getBestResultsDescription()
    {
        return BEST_RESULTS;
    }

    /**
     * When displayed for selection, the rule will be listed under specific
     * folders specified here. The rule will always be listed under the "All
     * rules" folder. And if the rule is contributed by a user and is placed in
     * the userRules folder, then it will also be shown in a folder called "User
     * rules". Any strings may be used; if the folder does not exist, then one
     * will be created with the specified name. If the folder already exists,
     * then that folder will be used.
     * <p>
     * By default, this returns null so that the rule is only placed in the
     * default folder(s).
     * <p>
     * Child classes should override this method if they want the rule to appear
     * in a specific folder. The "All rules" and "User rules" folder are
     * automatic and do not need to be specified; they are always added.
     * 
     * @return A list of the folders in which rule will be displayed for
     *         selection. May be null.
     */
    public String[] getDisplayFolderNames()
    {
        String[] folders = {RuleFolderNames.OUTER_TOTALISTIC_FOLDER};

        return folders;
    }

    /**
     * A brief one or two-word string describing the rule, appropriate for
     * display in a drop-down list.
     * 
     * @return A string no longer than 15 characters.
     */
    public String getDisplayName()
    {
        return RULE_NAME;
    }

    /**
     * A brief description (written in HTML) that describes this rule. The
     * description will be displayed as a tooltip. Using html permits line
     * breaks, font colors, etcetera, as described in HTML resources. Regular
     * line breaks will not work.
     * 
     * @return An HTML string describing this rule.
     */
    public String getToolTipDescription()
    {
        return TOOLTIP;
    }
}
