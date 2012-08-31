/*
 Lambda -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2007  David B. Bahr (http://academic.regis.edu/dbahr/)

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

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Random;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import cellularAutomata.CurrentProperties;
import cellularAutomata.lattice.*;
import cellularAutomata.rules.templates.IntegerRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MinMaxIntPair;
import cellularAutomata.util.MultilineLabel;
import cellularAutomata.util.math.RandomSingleton;

/*
 * This class is a little ugly, sorry. The static variables are to blame. They
 * were created so that a change in one cell's rule (specifically changes in
 * lambda via the display panel) will affect all other cell's rules.
 */

/**
 * Randomly generates a rule with the given value of lambda (provided through an
 * "additional properties panel"). Lambda is the parameter based on Langton's
 * analysis.
 * 
 * @author David Bahr
 */
public class Lambda extends IntegerRuleTemplate
{
	// Each rule has some number of transitions -- this places a limit on the
	// number of transitions before converting it to a rule number takes too
	// long and is too big to display. Without this max, the transitions can get
	// soo large that the code effectively hangs.
	private static final int MAX_RULE_LENGTH_FOR_DISPLAY = 3000;

	/**
	 * The default value of lambda.
	 */
	public final static double LAMBDA = 0.5;

	/**
	 * A unicode specified string for displaying lambda
	 */
	public final static String LAMBDA_UNICODE = "\u03BB";

	/**
	 * A display name for this class.
	 */
	public static final String RULE_NAME = "Langton's " + LAMBDA_UNICODE;

	// The maximum length of a rule number that will be displayed
	// before scientific notation is used instead.
	private static final int MAX_RULENUMBER_LENGTH = 40;

	// The pattern used to display rule numbers in scientific notation
	private static final String SCIENTIFIC_NOTATION_PATTERN = "0.##########E0";

	/**
	 * The pattern used to display decimals, particularly for lambda.
	 */
	public static final String LONG_DECIMAL_PATTERN = "0.0000";

	/**
	 * The pattern used to display decimals, particularly for lambda.
	 */
	public static final String SHORT_DECIMAL_PATTERN = "0.0";

	// a tooltip description for choosing lambda
	private static final String LAMBDA_TIP = "This selects a rule.";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, try using 3 states on a one-dimensional (2 neighbor) "
			+ "lattice with a 50% random initial state.  Now using the \"More Properties\" "
			+ "panel, adjust &#955; and watch the rule change. "
			+ "<p>"
			+ "If the rule evolves to a "
			+ "boring homogeneous state, adjust &#955; and draw a few random sites."
			+ leftClickInstructions + rightClickInstructions + "</body></html>";

	private static final String TOO_BIG_TO_DISPLAY = "too big to display (try fewer states)";

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>" + RULE_NAME
			+ ".</b> A rule that changes from ordered to chaotic behavior by "
			+ "adjusting a slider.</body></html>";

	// false if the lattice is two-dimensional
	private static boolean isOneDimensional = true;

	// the value of lambda set by the user using the slider
	private static double lambda = LAMBDA;

	// The value of lambda currently used by the rule (changed once per
	// generation to match the lambda set by the user using the slider).
	private static double currentLambdaUsedByRule = LAMBDA;

	// the maximum value of the JSlider for lambda
	private int maxValue = 10000;

	// the minimum value of the JSlider for lambda
	private int minValue = 0;

	// the current generation being processed by the rule
	private static volatile int currentGeneration = -1;

	// the number of neighbors for each cell
	private static int numberOfNeighbors = 2;

	// the rule number that was selected for the given lambda (as a String)
	private static String ruleNumberString = "";

	// look-up table that maps neighbors values to a new value
	// for example rule[0][0][1] = 1 would map 001 to 1. But we save this as a
	// 1-d array by putting 000 first, 001 second, 010 third, etc.
	// In general, each rule is given by its base-numStates equivalent.
	// For numStates = 2, the first rule is 000 and this is stored at
	// position 0 in the array. The second rule is 001, and this is stored
	// at position 1. In general, the rule is simple binary arithmetic. For
	// example, 110 is stored at array position 1 * 2^2 + 1 * 2^1 + 0 * 1^0
	// = 6. In base 3, the rules may be like 021 which is
	// stored at array position 0 * 3^2 + 2 * 3^1 + 1 * 3^0 = 7. However, the
	// order is actually unimportant because the rules are generated randomly.
	private static volatile int[] rule = null;

	// fonts for display
	private Fonts fonts = null;

	// the value of lambda is displayed in this label
	private JLabel lambdaValueLabel = null;

	// the actual rule number selected by lambda is shown here
	private JLabel ruleNumberLabel = null;

	// the JPanel that is returned by getAdditionalPropertiesPanel()
	private JPanel panel = null;

	// a slider that lets you pick lambda
	private JSlider lambdaSlider = null;

	/**
	 * Gets a number from the properties and sets up an appropriate cellular
	 * automaton rule based on that number.
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
	public Lambda(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		// only do this if the properties are not null
		if(!minimalOrLazyInitialization)
		{
			fonts = new Fonts();

			lambda = CurrentProperties.getInstance().getLambda();

			// if lambda is larger than 1.0 or less than 0.0, then set at
			// default LAMBDA
			lambda = (lambda > 1.0 || lambda < 0.0) ? LAMBDA : lambda;

			// only do this if the rule hasn't already been instantiated, or
			// if the number of states has changed. "rule.length" gives the
			// current number of transitions in this class, and
			// getTotalNumberOfTransitions() is the number of transitions that
			// would need to be used for the number of states set in the
			// property panel (which may have recently changed).
			if((rule == null)
					|| (rule.length != getTotalNumberOfTransitions(getNumStates())))
			{
				createLookUpTable(lambda);
			}
		}
	}

	/**
	 * Create the look-up table from lambda by setting the majority of
	 * transitions to be non-quiescent (map to non-0). Then 0 states are added.
	 * This method call is appropriate and faster when lambda is > 0.5.
	 * 
	 * @param lambda
	 *            Langston's lambda parameter.
	 * @numStates The number of CA states.
	 */
	private void buildNonZeroMajorityRuleArray(double lambda, int numStates)
	{
		// the number of different neighborhood arrangements
		int totalNumberOfTransitions = getTotalNumberOfTransitions(numStates);

		// the number of rules depends on the number of states
		rule = new int[totalNumberOfTransitions];

		// create a random number generator
		Random random = RandomSingleton.getInstance();

		// now randomly assign states to each possible configuration of the
		// neighborhood and assign state 0 with probability lambda.
		for(int i = 0; i < totalNumberOfTransitions; i++)
		{
			// There are more non-0 states than 0 states. So set the
			// majority to 0. Non-0 states are handled below.
			// Note that I use a random number between 1 and numState-1
			// (inclusive). 0 is not included because that is handled
			// below.
			rule[i] = 1 + random.nextInt(numStates - 1);
		}

		// replace some of the non-0 states with the 0 state (quiescent states)
		int numQuiescentTransitions = 0;
		double currentLambda = 1.0 - (numQuiescentTransitions / (double) totalNumberOfTransitions);
		while((currentLambda > lambda)
				&& (numQuiescentTransitions <= totalNumberOfTransitions))
		{
			// choose a random site to make 0
			int i = random.nextInt(totalNumberOfTransitions);
			while(rule[i] == 0)
			{
				// choose another site -- we already set that site to 0
				i = random.nextInt(totalNumberOfTransitions);
			}

			// the quiescent state
			rule[i] = 0;

			// there is a new quiescent transition.
			numQuiescentTransitions++;

			currentLambda = 1.0 - (numQuiescentTransitions / (double) totalNumberOfTransitions);
		}
	}

	/**
	 * Create the look-up table from lambda by setting the majority of
	 * transitions to be quiescent (map to 0). Then non-0 states are added. This
	 * method call is appropriate and faster when lambda is < 0.5.
	 * 
	 * @param lambda
	 *            Langston's lambda parameter.
	 * @numStates The number of CA states.
	 */
	private void buildZeroMajorityRuleArray(double lambda, int numStates)
	{
		// the number of different neighborhood arrangements
		int totalNumberOfTransitions = getTotalNumberOfTransitions(numStates);

		// the number of rules depends on the number of states
		rule = new int[totalNumberOfTransitions];

		// create a random number generator
		Random random = RandomSingleton.getInstance();

		// now randomly assign states to each possible configuration of the
		// neighborhood and assign state 0 with probability lambda.
		for(int i = 0; i < totalNumberOfTransitions; i++)
		{
			// Set each transition to quiesence.
			// Non-zero states are handled below.
			rule[i] = 0;
		}

		// replace some of the 0 states (quiescent states) with the non-0 state
		int numQuiescentTransitions = totalNumberOfTransitions;
		double currentLambda = 1.0 - (numQuiescentTransitions / (double) totalNumberOfTransitions);
		while((currentLambda < lambda) && (numQuiescentTransitions >= 0))
		{
			// choose a random site to make non-0
			int i = random.nextInt(totalNumberOfTransitions);
			while(rule[i] != 0)
			{
				// choose another site -- we already set that site to a non-0
				// value
				i = random.nextInt(totalNumberOfTransitions);
			}

			// the non-quiescent state (excluding 0)
			rule[i] = 1 + random.nextInt(numStates - 1);

			// there is one less quiescent transition.
			numQuiescentTransitions--;

			currentLambda = 1.0 - (numQuiescentTransitions / (double) totalNumberOfTransitions);
		}
	}

	/**
	 * Calculates the rule number in base 10 for the current rule.
	 * 
	 * @param numStates
	 *            The number states allowed for each cell in the CA.
	 * @return The rule number, or a phrase indicating if the rule number is too
	 *         big to calculate.
	 */
	private String calculateRuleNumber(int numStates)
	{
		// the string with the number that we will return
		String ruleDisplay = "";

		// start at 0 and add in values
		BigInteger ruleNumber = BigInteger.ZERO;
		try
		{
			if(rule != null)
			{
				// don't try to display something that is crazy huge
				if(rule.length > MAX_RULE_LENGTH_FOR_DISPLAY)
				{
					throw new Exception();
				}

				BigInteger numberOfStates = BigInteger.valueOf(numStates);
				for(int i = 0; i < rule.length; i++)
				{
					// ruleNumber += rule[i] * (int) Math.pow(numStates, i);
					BigInteger pow = numberOfStates.pow(i);
					BigInteger arrayPosition = BigInteger.valueOf(rule[i]);
					BigInteger product = arrayPosition.multiply(pow);
					ruleNumber = ruleNumber.add(product);
				}
			}

			ruleDisplay = ruleNumber.toString();

			// now make it look pretty
			NumberFormat formatter = new DecimalFormat(
					SCIENTIFIC_NOTATION_PATTERN);
			if(ruleDisplay.length() > MAX_RULENUMBER_LENGTH)
			{
				ruleDisplay = formatter.format(ruleNumber);
			}
		}
		catch(Throwable t)
		{
			// number was waaay to big
			ruleDisplay = TOO_BIG_TO_DISPLAY;
		}

		return ruleDisplay;
	}

	/**
	 * Creates the additional properties display panel.
	 * 
	 * @return A panel that will be displayed as the "additional properties
	 *         panel".
	 */
	private JPanel createDisplayPanel()
	{
		// the panel on which we add the controls
		JPanel innerPanel = new JPanel(new GridBagLayout());

		// a "grab their attention" panel
		AttentionPanel attentionPanel = new AttentionPanel("Choose "
				+ LAMBDA_UNICODE);

		// labels
		String description = "Choose a rule by selecting a value of "
				+ LAMBDA_UNICODE + " between 0 and 1. \n\n" + "Roughly, "
				+ LAMBDA_UNICODE
				+ " is the percentage of neighborhood configurations "
				+ "that map to 0. This slider chooses a value for "
				+ LAMBDA_UNICODE
				+ " and then randomly selects one of many possible rules "
				+ "that has that percentage. \n\n"
				+ "See this rule's description for more details.";
		MultilineLabel descriptionLabel = new MultilineLabel(description);
		descriptionLabel.setFont(fonts.getMorePropertiesDescriptionFont());
		descriptionLabel.setMargin(new Insets(0, 2, 0, 0));

		// rule number indicator
		ruleNumberLabel = new JLabel("Rule number = " + ruleNumberString);
		ruleNumberLabel.setFont(fonts.getBoldFont());

		// lambda value indicator
		DecimalFormat myFormatter = new DecimalFormat(LONG_DECIMAL_PATTERN);
		String output = myFormatter.format(lambda);
		lambdaValueLabel = new JLabel(LAMBDA_UNICODE + " = " + output);
		lambdaValueLabel.setFont(fonts.getBoldFont());

		// the lambda slider
		int numTickMarks = 10;
		int majorTickSpacing = (int) Math.round(maxValue / numTickMarks);

		lambdaSlider = new JSlider(minValue, maxValue,
				(int) (maxValue * (lambda)));
		lambdaSlider.addChangeListener(new LamdaSliderListener());
		lambdaSlider.setToolTipText(LAMBDA_TIP);

		// set tick marks and labels for the slider
		lambdaSlider.setMajorTickSpacing(majorTickSpacing);
		lambdaSlider.setPaintTicks(true);
		lambdaSlider.setSnapToTicks(false);

		// the hash table of labels
		Hashtable sliderLabelTable = new Hashtable();
		for(int i = 0; i <= numTickMarks; i++)
		{
			double labelValue = i * (1.0 / numTickMarks);
			DecimalFormat formatter = new DecimalFormat(SHORT_DECIMAL_PATTERN);
			sliderLabelTable.put(new Integer(i * majorTickSpacing), new JLabel(
					"" + formatter.format(labelValue)));
		}
		lambdaSlider.setLabelTable(sliderLabelTable);
		lambdaSlider.setPaintLabels(true);

		// add everything to a panel
		int row = 0;
		innerPanel.add(attentionPanel, new GBC(1, row).setSpan(10, 1).setFill(
				GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0));

		row++;
		innerPanel.add(descriptionLabel, new GBC(1, row).setSpan(10, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(10));

		// lambda label
		row++;
		innerPanel.add(lambdaValueLabel, new GBC(3, row).setSpan(1, 1).setFill(
				GBC.VERTICAL).setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
				.setInsets(1));

		// lambda slider
		row++;
		innerPanel.add(lambdaSlider, new GBC(1, row).setSpan(10, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
				.setInsets(1));

		// rule number label
		row++;
		innerPanel.add(ruleNumberLabel, new GBC(3, row).setSpan(1, 1).setFill(
				GBC.VERTICAL).setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
				.setInsets(1));

		// expands to fill remaining space
		row++;
		innerPanel.add(new JLabel(" "), new GBC(3, row).setSpan(1, 1).setFill(
				GBC.BOTH).setWeight(50.0, 50.0).setAnchor(GBC.CENTER)
				.setInsets(1));

		return innerPanel;
	}

	/**
	 * Create the look-up table from lambda and the number of states.
	 * 
	 * @param lambda
	 *            Langston's lambda parameter.
	 */
	private void createLookUpTable(double lambda)
	{
		int numStates = getNumStates();

		// There are two cases: Either there are more 0 states than non-0
		// states (lambda < 0.5), 0r vice versa (lambda >= 0.5).
		if(lambda < 0.5)
		{
			// speeds up this method by setting the majority of array sites
			// as 0's, and then replacing some with non-0 states
			buildZeroMajorityRuleArray(lambda, numStates);
		}
		else
		{
			// speeds up this method by setting the majority of array sites
			// as non-0's, and then replacing some with 0 states
			buildNonZeroMajorityRuleArray(lambda, numStates);
		}

		// is it a 1-d or 2-d lattice?
		isOneDimensional = OneDimensionalLattice.isCurrentLatticeOneDim();

		// get the rule number (for display)
		ruleNumberString = calculateRuleNumber(numStates);
	}

	/**
	 * Calculates the minimum and maximum number of allowed states for the given
	 * lattice and the lambda rule.
	 * 
	 * @param latticeDescription
	 *            The display name of the lattice.
	 * @return The minimum and maximum allowed number of states.
	 */
	protected MinMaxIntPair getMinMaxAllowedStates(String latticeDescription)
	{
		int minNumStates = 2;
		int maxNumStates = 2;
		if(latticeDescription
				.equals(StandardOneDimensionalLattice.DISPLAY_NAME))
		{
			maxNumStates = 100; // really 200, but I want to be safe
		}
		else if(latticeDescription.equals(TriangularLattice.DISPLAY_NAME))
		{
			maxNumStates = 25; // really 52, but I want to be safe
		}
		else if(latticeDescription
				.equals(NextNearestOneDimLattice.DISPLAY_NAME))
		{
			maxNumStates = 12; // really 24, but I want to be safe
		}
		else if(latticeDescription
				.equals(FourNeighborSquareLattice.DISPLAY_NAME))
		{
			maxNumStates = 12; // really 24, but I want to be safe
		}
		else if(latticeDescription.equals(HexagonalLattice.DISPLAY_NAME))
		{
			maxNumStates = 4; // really 9, but I want to be safe
		}
		else if(latticeDescription.equals(SquareLattice.DISPLAY_NAME))
		{
			maxNumStates = 3; // really 6, but I want to be safe
		}
		else if(latticeDescription
				.equals(TwelveNeighborTriangularLattice.DISPLAY_NAME))
		{
			maxNumStates = 2; // really is 3, but I want to be safe
		}

		return new MinMaxIntPair(minNumStates, maxNumStates);
	}

	/**
	 * Gets the number of possible states that each CA cell can have.
	 * 
	 * @return The number of states allowed for each CA cell.
	 */
	private int getNumStates()
	{
		int numStates = CurrentProperties.getInstance().getNumStates();

		String latticeName = CurrentProperties.getInstance()
				.getLatticeDisplayName();

		// make sure the number of states isn't too big
		MinMaxIntPair minMaxStates = getMinMaxAllowedStates(latticeName);
		if(numStates > minMaxStates.max)
		{
			// then reset to the minimum
			numStates = 2;
		}

		return numStates;
	}

	/**
	 * Calculates the number of possible transitions from the neighbor states to
	 * the new state. For example,with nearest neighbors, 000 to 0, 001 to 1,
	 * and 010 to 1 are all possible transitions (out of 8 for numStates = 2).
	 * 
	 * @param numStates
	 *            The number of possible states that each cell can have. For
	 *            example, 2 for binary CA.
	 * @return The total number of rules or transitions from neighbor states to
	 *         the new state.
	 */
	private int getTotalNumberOfTransitions(int numStates)
	{
		// note that we add 1 to numberOfNeighbors because we also have to count
		// the cell itself
		int numTransitions = 1;
		for(int i = 0; i < numberOfNeighbors + 1; i++)
		{
			numTransitions *= numStates;
		}

		return numTransitions;
	}

	// a warning message when the requested array size is too large
	// private void printWarning()
	// {
	// String message = "You have requested too many states with too many \n"
	// + "neighbors on the lattice. The gigantic array necessary \n"
	// + "to hold the rules cannot be created. As a result, "
	// + LAMBDA_UNICODE + "\n"
	// + "will only sample a subset of the possible rules.\n\n"
	// + "If this is unacceptable please reduce the number of \n"
	// + "neighbors or the number of states.";
	//
	// JOptionPane.showMessageDialog(null, message, "Warning",
	// JOptionPane.WARNING_MESSAGE);
	//
	// hasPrintedTooBigWarning = true;
	// }

	/**
	 * Updates the cell based on the rule provided by the constructor.
	 * 
	 * @param cell
	 *            The values of the cell being updated.
	 * @param neighbors
	 *            The values of the neighbors.
	 * @param the
	 *            number of CA states.
	 * @param generation
	 *            The current generation of the CA.
	 * @return A new state for the cell.
	 */
	protected synchronized int integerRule(int cell, int[] neighbors,
			int numStates, int generation)
	{
		// First make sure the lattice has not changed.
		// This will also get entered once at the very beginning if the lattice
		// has more than the default number of neighbors stored in
		// numberOfNeighbors (currently 2).
		if(neighbors.length != numberOfNeighbors)
		{
			numberOfNeighbors = neighbors.length;
			createLookUpTable(lambda);
		}

		// only update slider parameter (lambda) at the beginning
		// of each generation. This prevents some cells from seeing one
		// lambda and other cells seeing another lambda.
		if(currentGeneration != generation)
		{
			currentGeneration = generation;

			// The current lambda used by the rule. Once per generation it
			// is reset to the lambda selected by the user from the "more
			// properties panel."
			if(currentLambdaUsedByRule != lambda)
			{
				createLookUpTable(lambda);
				currentLambdaUsedByRule = lambda;
			}
		}

		// each rule is given by its base-numStates equivalent. For example,
		// for numStates = 2, the first rule is 000 and this is stored at
		// position 0 in the array. The second rule is 001, and this is stored
		// at position 1. In general, the rule is simple binary arithmetic. For
		// example, 110 is stored at array position 1 * 2^2 + 1 * 2^1 + 0 * 1^0
		// = 6. In base 3, the rules may be like 021 which is
		// stored at array position 0 * 3^2 + 2 * 3^1 + 1 * 3^0 = 7.

		// so let's convert from base-numStates to a position in the rule array
		// which is in base-ten.

		// First create an array that also holds both the neighbors and
		// the cell -- the cell's value is in the middle of the array if it is
		// 1-d. The cell's value is at the end if it is 2-d.
		int[] allCells = new int[neighbors.length + 1];

		if(isOneDimensional)
		{
			// one-dimensional case
			for(int i = 0; i < neighbors.length + 1; i++)
			{
				if(i < (neighbors.length + 1) / 2)
				{
					// add the neighbors to the new array
					allCells[i] = neighbors[i];
				}
				else if(i == (neighbors.length + 1) / 2)
				{
					// put the cell being updated in the middle of the array
					allCells[i] = cell;
				}
				else
				{
					// add the rest of the neighbors to the new array (note i-1)
					allCells[i] = neighbors[i - 1];
				}
			}
		}
		else
		{
			// two-dimensional case
			for(int i = 0; i < neighbors.length; i++)
			{
				allCells[i] = neighbors[i];
			}
			allCells[neighbors.length] = cell;
		}

		// now the conversion to base-ten from the base-numStates value
		int baseTenValue = 0;
		for(int i = 0; i < allCells.length; i++)
		{
			baseTenValue += allCells[i]
					* (int) Math.pow(numStates, allCells.length - 1 - i);
		}

		// just to be safe
		if(baseTenValue > rule.length - 1 || baseTenValue < 0)
		{
			// trying to create too large of a number so use default instead.
			baseTenValue = rule.length - 1;
		}

		return rule[baseTenValue];
	}

	/**
	 * Gets a JPanel that may request specific input information that the rule
	 * needs to operate correctly. Should be overridden by child classes that
	 * desire to input any specific information. <br>
	 * Note that if returns null, then the panel is not displayed by the current
	 * version of the CA ControlPanel class. This null behavior is the default.
	 * 
	 * @return A JPanel requesting specific input information that the rule
	 *         needs to operate correctly. May be null.
	 */
	public JPanel getAdditionalPropertiesPanel()
	{
		if(this.panel == null)
		{
			this.panel = createDisplayPanel();
		}

		return this.panel;
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
	 * A list of lattices with which this Rule will work; in this case, returns
	 * all lattices by default, though child classes may wish to override this
	 * and restrict the lattices with which the child rule will work.
	 * <p>
	 * Well-designed Rules should work with any lattice, but some may require
	 * particular topological or geometrical information (like the lattice gas).
	 * Appropriate strings to return in the array include
	 * SquareLattice.DISPLAY_NAME, HexagonalLattice.DISPLAY_NAME,
	 * StandardOneDimensionalLattice.DISPLAY_NAME, etc. If null, will be
	 * compatible with all lattices.
	 * 
	 * @return A list of lattices compatible with this Rule (returns the display
	 *         names for the lattices). Returns null if compatible with all
	 *         lattices.
	 */
	public String[] getCompatibleLattices()
	{
		// Don't allow lattices with too many neighbors or the rule array gets
		// too large. So this list is restrictive. If this list is changed
		// please also change the method maxNumStatesAllowed().
		String[] lattices = {SquareLattice.DISPLAY_NAME,
				HexagonalLattice.DISPLAY_NAME,
				NextNearestOneDimLattice.DISPLAY_NAME,
				StandardOneDimensionalLattice.DISPLAY_NAME,
				TriangularLattice.DISPLAY_NAME,
				TwelveNeighborTriangularLattice.DISPLAY_NAME,
				FourNeighborSquareLattice.DISPLAY_NAME};

		return lattices;
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
		String[] folders = {RuleFolderNames.INSTRUCTIONAL_FOLDER};

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

	/**
	 * A listener for the lamda slider.
	 * 
	 * @author David Bahr
	 */
	private class LamdaSliderListener implements ChangeListener
	{
		/**
		 * Listens for changes to the lambda slider.
		 * 
		 * @param e
		 */
		public void stateChanged(ChangeEvent e)
		{
			if((lambdaSlider != null) && (lambdaValueLabel != null)
					&& (ruleNumberLabel != null))
			{
				// get the lambda value in *arbitrary units provided by the
				// slider* that's why I divide by maxValue
				double updatedLambdaValue = lambdaSlider.getValue()
						/ (double) maxValue;

				// make sure the value changed
				if(lambda != updatedLambdaValue)
				{
					lambda = updatedLambdaValue;

					// change the display
					ruleNumberLabel
							.setText("Rule number = " + ruleNumberString);
					lambdaValueLabel.setText(LAMBDA_UNICODE + " = " + lambda);

					// and update the properties
					CurrentProperties.getInstance().setLambda(lambda);
				}
			}
		}
	}
}

// OLD but potentially useful code RADIX size was too limited

// // now the conversion to base-ten from the base-numStates value
// String numberString = "";
// if(isOneDimensional)
// {
// // one-dimensional case
// for(int i = 0; i < neighbors.length; i++)
// {
// if(i == (neighbors.length + 1) / 2)
// {
// // put the cell being updated in the middle of the string
// numberString += cell;
// }
// numberString += neighbors[i];
// }
// }
// else
// {
// //two-dimensional case
// for(int i = 0; i < neighbors.length; i++)
// {
// numberString += neighbors[i];
// }
// numberString += cell;
// }
//
// int baseTenValue = Integer.parseInt(numberString, numStates);
