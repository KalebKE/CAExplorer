/*
 * Obesity -- a class within the Cellular Automaton Explorer. Copyright (C) 2007
 * David B. Bahr (http://academic.regis.edu/dbahr/) This program is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version. This program
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package cellularAutomata.rules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.rules.templates.IntegerRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;
import cellularAutomata.util.math.RandomSingleton;
import cellularAutomata.util.Coordinate;

/**
 * Rule for becoming obese based on your neighbors obesity. Each state
 * represents a range of body mass index BMI. Generally, there are 4 states
 * representing underweight (0), normal (1), overweight (2), and obese (3). In
 * this model each person looks at all of their neighbors and is assigned a
 * probability of taking a state based on the percentage of their neighbors that
 * have that state. For example, if there are four 0's, two 1's, and three 4's,
 * then the odds are 4/9 that the cell becomes a 0, 2/9 that the cell becomes a
 * 1, and 3/9 that the cell becomes a 4. Note that the value of the cell itself
 * is excluded.
 * <p>
 * In addition, this model includes social temperature, forces, and noise. See
 * papers by Bahr and Passerini for details.
 * 
 * @author David Bahr
 */
public class ObesityModel extends IntegerRuleTemplate
{
	// the individual will gain weight with this default probability (after
	// all other social network-based decisions have been made)
	private static final double DEFAULT_PROBABILITY = 0.0;

	// the default temperature of the group of all individuals. A
	// temperature of 1.0 corresponds to "majority probably rules" (when the
	// forces are also 0.0). Values can be 0 to infinity. Values of 0 to 1 are
	// most interesting.
	private static final double DEFAULT_TEMPERATURE = 1.0;

	// the max force. Values can be -infinity to infinity. Values of -1 to 1
	// are most interesting.
	private static final double MAX_FORCE = 2.0;

	// the max noise. Values can be 0 to 1.
	private static final double MAX_NOISE = 1.0;

	// the max temperature of the group of all individuals. A temperature of
	// 1.0 corresponds to "majority probably rules" (when the forces are
	// also 0.0). Values can be 0 to infinity. Values of 0 to 1 are most
	// interesting.
	private static final double MAX_TEMPERATURE = 2.0;

	// the min force. Values can be -infinity to infinity. Values of -1 to 1
	// are most interesting.
	private static final double MIN_FORCE = -2.0;

	// the min noise. Values can be 0 to 1.
	private static final double MIN_NOISE = 0.0;

	// the min temperature of the group of all individuals. A temperature of
	// 1.0 corresponds to "majority probably rules" (when the forces are
	// also 0.0). Values can be 0 to infinity. Values of 0 to 1 are most
	// interesting.
	private static final double MIN_TEMPERATURE = 0.0;

	// random number generator
	private static Random random = RandomSingleton.getInstance();

	// max value of the sliders
	private static final int MAX_SLIDER_VALUE = 1000;

	// A pattern used to display decimals.
	private static final String LONG_DECIMAL_PATTERN = "0.000";

	// A pattern used to display decimals.
	private static final String SHORT_DECIMAL_PATTERN = "0.0";

	// tool tip for the biological weight gain
	private static final String PROBABILITY_TIP = "The probability that an "
			+ "individual gains weight due to biological factors.";

	// tool tip for the social force
	private static final String FORCE_TIP = "The social force "
			+ "that pushes individuals towards this BMI.  See rule description.";

	// tool tip for the noise
	private static final String NOISE_TIP = "Probability that individual will "
			+ "randomly change weight.  The amout of noise in the system.";

	// label for the check box that restricts movement up or down one unit
	// at a time
	private static final String RESTRICT_MOVEMENT = "Restrict movement to one BMI class";

	// tool tip for the check box that restricts movement up or down one
	// unit at a time
	private static final String RESTRICT_MOVEMENT_TOOLTIP = "<html><body>Restricts "
			+ "each individual to moving up or down one <br>weight unit per time step.</body></html>";

	// tool tip for the social temperature
	private static final String TEMPERATURE_TIP = "The social temperature "
			+ "(volatility/irrationality).  Low temperatures are more stable.  See rule description.";

	// tool tip for the "up-down by one unit" noise
	private static final String UPDOWN_NOISE_TIP = "Probability that an individual will "
			+ "randomly increase or decrease weight (or BMI) by one unit.";

	// a display name for this class
	private static final String RULE_NAME = "Obesity Model";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, try a lattice (social network) with a "
			+ "large number of neighbors.  Use 4 states to represent the four major "
			+ "categories of the body mass index (underweight, normal, overweight, "
			+ "obese). Choose a 75% random initial state, or enter probabilities "
			+ "based on actual percentages of observed body mass index categories. "
			+ leftClickInstructions + rightClickInstructions + "</body></html>";

	// when true, each cell is clamped so that it can only move up or down
	// one weight (or BMI) unit at a time. The cell decides on a new weight
	// independantly of the clamp (as if free to move up 2 or more weight
	// units), and then the clamp acts afterwords to restrict the movement
	// to one unit up or down.
	private static volatile boolean restrictToMovementOfOneUnit = false;

	// a local copy of the restricted movement, updated once per generation
	private static volatile boolean restrictToMovementOfOneUnitLocalCopy = false;

	// probability of random weight changes (noise in the system).
	private static volatile double noiseThreshold = 0.0;

	// a local copy of the random weight changes (noise in the system),
	// updated once per generation
	private static volatile double noiseThresholdLocalCopy = 0.0;

	// the individual will gain weight with this probability (after all
	// other social network-based decisions have been made)
	private static volatile double probabilityOfSpontaneousWeightGain = DEFAULT_PROBABILITY;

	// a local copy of the probability, updated once per generation
	private static volatile double probabilityOfSpontaneousWeightGainLocalCopy = DEFAULT_PROBABILITY;

	// the temperature of the group of all individuals
	private static volatile double socialTemperature = DEFAULT_TEMPERATURE;

	// a local copy of the social temperature, updated once per generation
	private static volatile double socialTemperatureLocalCopy = DEFAULT_TEMPERATURE;

	// probability of random weight increase by +-1 unit.
	private static volatile double upDownNoiseThreshold = 0.0;

	// a local copy of the threshold, updated once per generation
	private static volatile double upDownNoiseThresholdLocalCopy = 0.0;

	// the force applied toward each state
	private static volatile double[] socialForce = null;

	// a local copy of the social force, updated once per generation
	private static volatile double[] socialForceLocalCopy = null;

	// the current generation being processed by the rule
	private static volatile int currentGeneration = -1;

	// the number of states -- reset by the integerRule method
	private static int numStates = 2;

	// checkbox for restricting movement up and down by only one weight/BMI
	// unit
	private static JCheckBox restrictMovementCheckBox = null;

	// label that displays the noise
	private static JLabel noiseSliderLabel = null;

	// label that displays the probability of gaining weight
	private static JLabel probabilitySliderLabel = null;

	// label that displays the temperature
	private static JLabel temperatureSliderLabel = null;

	// label that displays the noise that moves up or down one unit
	private static JLabel upDownNoiseSliderLabel = null;

	// the additional properties panel
	private static JPanel additionalPropertiesPanel = null;

	// labels for the social forces
	private static JLabel[] forceSliderLabel = null;

	// sliders that set the social forces
	private static JSlider[] forceSlider = null;

	// slider that sets the noise level
	private static JSlider noiseSlider = null;

	// slider that sets the probability of weight gain
	private static JSlider probSlider = null;

	// slider that sets the temperature
	private static JSlider temperatureSlider = null;

	// slider that sets the "up-down by one unit" noise level
	private static JSlider upDownNoiseSlider = null;

	// formats decimals for display
	private DecimalFormat longDecimalFormatter = new DecimalFormat(
			LONG_DECIMAL_PATTERN);

	// formats decimals for display
	private DecimalFormat shortDecimalFormatter = new DecimalFormat(
			SHORT_DECIMAL_PATTERN);

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b> A model of obesity and how it spreads through social networks.</body></html>";

	/**
	 * Create the obesity rule using the given cellular automaton properties.
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
	public ObesityModel(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			// so we can use this elsewhere
			numStates = CurrentProperties.getInstance().getNumStates();

			// create the social forces (only if necessary)
			if(forceSliderLabel == null || forceSliderLabel.length != numStates)
			{
				// force the additional properties panel to be recreated
				additionalPropertiesPanel = null;

				forceSliderLabel = new JLabel[numStates];
				socialForce = new double[numStates];
				for(int i = 0; i < socialForce.length; i++)
				{
					socialForce[i] = 0.0;
				}

				socialForceLocalCopy = new double[numStates];
				for(int i = 0; i < socialForceLocalCopy.length; i++)
				{
					socialForceLocalCopy[i] = 0.0;
				}
			}
		}
	}

	/**
	 * Create a scroll panel that holds all the force sliders.
	 * 
	 * @param width
	 *            The width of the additional properties panel.
	 * @param height
	 *            The height of the additional properties panel.
	 * @return scrollPane of sliders.
	 */
	private JScrollPane createForceSliderPanel(int width, int height)
	{
		JPanel sliderPanel = new JPanel(new GridBagLayout());

		int row = 0;
		for(int state = 0; state < numStates; state++)
		{
			// spacing above each label, used to separate the sliders
			int verticalSpace = 12;
			if(state == 0)
			{
				verticalSpace = 1;
			}

			// force label
			row++;
			sliderPanel.add(forceSliderLabel[state], new GBC(1, row).setSpan(5,
					1).setFill(GBC.VERTICAL).setWeight(1.0, 1.0).setAnchor(
					GBC.CENTER).setInsets(verticalSpace, 1, 0, 1));

			// force slider
			row++;
			sliderPanel.add(forceSlider[state], new GBC(1, row).setSpan(5, 1)
					.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
					.setInsets(1));
		}

		// expands to fill all extra space
		row++;
		sliderPanel.add(new JLabel(" "), new GBC(1, row).setSpan(5, 1).setFill(
				GBC.BOTH).setWeight(20.0, 20.0).setAnchor(GBC.WEST)
				.setInsets(0));

		JScrollPane forceScroller = new JScrollPane(sliderPanel);
		int scrollPaneWidth = width;
		int scrollPaneHeight = (int) (height * 0.275);
		forceScroller.setPreferredSize(new Dimension(scrollPaneWidth,
				scrollPaneHeight));
		forceScroller.setMinimumSize(new Dimension(scrollPaneWidth,
				scrollPaneHeight));
		forceScroller.setMaximumSize(new Dimension(scrollPaneWidth,
				scrollPaneHeight));
		forceScroller
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		return forceScroller;
	}

	/**
	 * Create the sliders for the social forces (one slider per state).
	 */
	private JSlider[] createForceSliders()
	{
		JSlider[] allForceSliders = new JSlider[numStates];

		// create one slider for each state
		for(int state = 0; state < numStates; state++)
		{
			// create a slider for force
			int numTickMarks = 8;
			int majorTickSpacing = (int) Math.round(MAX_SLIDER_VALUE
					/ numTickMarks);

			JSlider slider = new JSlider(
					0,
					MAX_SLIDER_VALUE,
					(int) (MAX_SLIDER_VALUE * (socialForce[state] - MIN_FORCE) / (MAX_FORCE - MIN_FORCE)));
			slider.addChangeListener(new SliderListener());
			slider.setToolTipText(FORCE_TIP);

			// set tick marks and labels for the slider
			slider.setMajorTickSpacing(majorTickSpacing);
			slider.setPaintTicks(true);
			slider.setSnapToTicks(false);

			// the hash table of labels
			Hashtable sliderLabelTable = new Hashtable();
			double maxLabelValue = MAX_FORCE;
			double minLabelValue = MIN_FORCE;
			for(int i = 0; i <= numTickMarks; i++)
			{
				double labelValue = minLabelValue
						+ (i * ((maxLabelValue - minLabelValue) / numTickMarks));
				sliderLabelTable.put(new Integer(i * majorTickSpacing),
						new JLabel(""
								+ shortDecimalFormatter.format(labelValue)));
			}
			slider.setLabelTable(sliderLabelTable);
			slider.setPaintLabels(true);

			allForceSliders[state] = slider;
		}

		return allForceSliders;
	}

	/**
	 * Create a slider for random noise (random weight or BMI changes).
	 */
	private JSlider createNoiseSlider()
	{
		// create a slider for noise
		int numTickMarks = 5;
		int majorTickSpacing = (int) Math
				.round(MAX_SLIDER_VALUE / numTickMarks);

		JSlider noiseSlider = new JSlider(
				0,
				MAX_SLIDER_VALUE,
				(int) (MAX_SLIDER_VALUE * (noiseThreshold - MIN_NOISE) / (MAX_NOISE - MIN_NOISE)));
		noiseSlider.addChangeListener(new SliderListener());
		noiseSlider.setToolTipText(NOISE_TIP);

		// set tick marks and labels for the slider
		noiseSlider.setMajorTickSpacing(majorTickSpacing);
		noiseSlider.setPaintTicks(true);
		noiseSlider.setSnapToTicks(false);

		// the hash table of labels
		Hashtable sliderLabelTable = new Hashtable();
		double maxLabelValue = MAX_NOISE;
		double minLabelValue = MIN_NOISE;
		for(int i = 0; i <= numTickMarks; i++)
		{
			double labelValue = minLabelValue
					+ (i * ((maxLabelValue - minLabelValue) / numTickMarks));
			sliderLabelTable.put(new Integer(i * majorTickSpacing), new JLabel(
					"" + shortDecimalFormatter.format(labelValue)));
		}
		noiseSlider.setLabelTable(sliderLabelTable);
		noiseSlider.setPaintLabels(true);

		return noiseSlider;
	}

	/**
	 * Create the slider that will increase the individual's weight due to
	 * biological imperatives.
	 */
	private JSlider createRandomWeightGainSlider()
	{
		Fonts fonts = new Fonts();

		// create a slider for the probability of gaining weight (after all
		// other decisions are made). i.e., the bias.
		String output = longDecimalFormatter
				.format(probabilityOfSpontaneousWeightGain);
		probabilitySliderLabel = new JLabel("probability = " + output);
		probabilitySliderLabel.setFont(fonts.getBoldFont());

		// the random weight gain slider
		int numTickMarks = 5;
		int majorTickSpacing = (int) Math
				.round(MAX_SLIDER_VALUE / numTickMarks);

		JSlider probSlider = new JSlider(0, MAX_SLIDER_VALUE,
				(int) (MAX_SLIDER_VALUE * (probabilityOfSpontaneousWeightGain)));
		probSlider.addChangeListener(new SliderListener());
		probSlider.setToolTipText(PROBABILITY_TIP);

		// set tick marks and labels for the slider
		probSlider.setMajorTickSpacing(majorTickSpacing);
		probSlider.setPaintTicks(true);
		probSlider.setSnapToTicks(false);

		// the hash table of labels
		Hashtable sliderLabelTable = new Hashtable();
		for(int i = 0; i <= numTickMarks; i++)
		{
			double labelValue = i * (1.0 / numTickMarks);
			sliderLabelTable.put(new Integer(i * majorTickSpacing), new JLabel(
					"" + shortDecimalFormatter.format(labelValue)));
		}
		probSlider.setLabelTable(sliderLabelTable);
		probSlider.setPaintLabels(true);

		return probSlider;
	}

	/**
	 * Creates a checkbox indicating that the cells can only move up or down one
	 * weight unit at a time.
	 * 
	 * @return The checkbox that restricts weight movement to single step.
	 */
	private JCheckBox createMovementRestrictionCheckBox()
	{
		Fonts fonts = new Fonts();

		JCheckBox restrictMovementCheckBox = new JCheckBox(RESTRICT_MOVEMENT);
		restrictMovementCheckBox.setToolTipText(RESTRICT_MOVEMENT_TOOLTIP);
		restrictMovementCheckBox.setFont(fonts.getBoldFont());
		restrictMovementCheckBox.setActionCommand(RESTRICT_MOVEMENT);
		restrictMovementCheckBox.setSelected(restrictToMovementOfOneUnit);

		restrictMovementCheckBox.addActionListener(this);

		return restrictMovementCheckBox;
	}

	/**
	 * Create the slider that controls social temperature
	 * (volatility/irrationality).
	 */
	private JSlider createTemperatureSlider()
	{
		// create a slider for social temperature
		int numTickMarks = 10;
		int majorTickSpacing = (int) Math
				.round(MAX_SLIDER_VALUE / numTickMarks);

		JSlider temperatureSlider = new JSlider(
				0,
				MAX_SLIDER_VALUE,
				(int) (MAX_SLIDER_VALUE * (socialTemperature - MIN_TEMPERATURE) / (MAX_TEMPERATURE - MIN_TEMPERATURE)));
		temperatureSlider.addChangeListener(new SliderListener());
		temperatureSlider.setToolTipText(TEMPERATURE_TIP);

		// set tick marks and labels for the slider
		temperatureSlider.setMajorTickSpacing(majorTickSpacing);
		temperatureSlider.setPaintTicks(true);
		temperatureSlider.setSnapToTicks(false);

		// the hash table of labels
		Hashtable sliderLabelTable = new Hashtable();
		double maxLabelValue = MAX_TEMPERATURE;
		double minLabelValue = MIN_TEMPERATURE;
		for(int i = 0; i <= numTickMarks; i++)
		{
			double labelValue = minLabelValue
					+ (i * ((maxLabelValue - minLabelValue) / numTickMarks));
			sliderLabelTable.put(new Integer(i * majorTickSpacing), new JLabel(
					"" + shortDecimalFormatter.format(labelValue)));
		}
		temperatureSlider.setLabelTable(sliderLabelTable);
		temperatureSlider.setPaintLabels(true);

		return temperatureSlider;
	}

	/**
	 * Create a slider for random noise (random weight or BMI changes) that
	 * moves up or down only one unit at a time.
	 */
	private JSlider createUpDownNoiseSlider()
	{
		// create a slider for noise
		int numTickMarks = 5;
		int majorTickSpacing = (int) Math
				.round(MAX_SLIDER_VALUE / numTickMarks);

		JSlider upDownNoiseSlider = new JSlider(
				0,
				MAX_SLIDER_VALUE,
				(int) (MAX_SLIDER_VALUE * (upDownNoiseThreshold - MIN_NOISE) / (MAX_NOISE - MIN_NOISE)));
		upDownNoiseSlider.addChangeListener(new SliderListener());
		upDownNoiseSlider.setToolTipText(UPDOWN_NOISE_TIP);

		// set tick marks and labels for the slider
		upDownNoiseSlider.setMajorTickSpacing(majorTickSpacing);
		upDownNoiseSlider.setPaintTicks(true);
		upDownNoiseSlider.setSnapToTicks(false);

		// the hash table of labels
		Hashtable sliderLabelTable = new Hashtable();
		double maxLabelValue = MAX_NOISE;
		double minLabelValue = MIN_NOISE;
		for(int i = 0; i <= numTickMarks; i++)
		{
			double labelValue = minLabelValue
					+ (i * ((maxLabelValue - minLabelValue) / numTickMarks));
			sliderLabelTable.put(new Integer(i * majorTickSpacing), new JLabel(
					"" + shortDecimalFormatter.format(labelValue)));
		}
		upDownNoiseSlider.setLabelTable(sliderLabelTable);
		upDownNoiseSlider.setPaintLabels(true);

		return upDownNoiseSlider;
	}

	/**
	 * Rule for obesity.
	 * 
	 * @param cell
	 *            The value of the cell being updated.
	 * @param neighbors
	 *            The value of the neighbors.
	 * @param generation
	 *            The current generation of the CA.
	 * @return A new state for the cell.
	 */
	protected synchronized int integerRule(int cell, int[] neighbors, int numStates,
			int generation)
	{
		if(socialForce == null || socialForceLocalCopy == null
				|| ObesityModel.numStates != numStates)
		{
			// so we can use this elsewhere
			ObesityModel.numStates = numStates;

			// recreate the social forces
			forceSliderLabel = new JLabel[numStates];
			socialForce = new double[numStates];
			for(int i = 0; i < socialForce.length; i++)
			{
				socialForce[i] = 0.0;
			}
			socialForceLocalCopy = new double[numStates];
			for(int i = 0; i < socialForceLocalCopy.length; i++)
			{
				socialForceLocalCopy[i] = 0.0;
			}
		}

		// only update slider parameters (like temp and force) at the beginning
		// of each generation. This prevents some cells from seeing one
		// temperature and other cells seeing another temperature.
		if(currentGeneration != generation)
		{
			currentGeneration = generation;

			// The temperature used by the rule. Once per generation it
			// is reset to the socialTemperature selected by the user from
			// the "more properties panel."
			socialTemperatureLocalCopy = socialTemperature;

			// ditto forces
			for(int i = 0; i < socialForceLocalCopy.length; i++)
			{
				socialForceLocalCopy[i] = socialForce[i];
			}

			// ditto noise
			probabilityOfSpontaneousWeightGainLocalCopy = probabilityOfSpontaneousWeightGain;
			upDownNoiseThresholdLocalCopy = upDownNoiseThreshold;
			noiseThresholdLocalCopy = noiseThreshold;

			// ditto restricted movement
			restrictToMovementOfOneUnitLocalCopy = restrictToMovementOfOneUnit;
		}

		// store how many cells have each state
		int[] numberOfEachState = new int[numStates];

		// initialize
		Arrays.fill(numberOfEachState, 0);

		// figure out how many cells have each state
		for(int i = 0; i < neighbors.length; i++)
		{
			int state = neighbors[i];

			numberOfEachState[state]++;
		}

		// don't forget the cell itself
		numberOfEachState[cell]++;

		// the value that is returned
		int cellValue = cell;

		// if the temp is 0, then the cell's all keep their current value (no
		// change -- see my online lecture notes)
		if(socialTemperatureLocalCopy != 0.0)
		{
			// get probability (percent) of each state, including the social
			// force and temperature. (We divide by the partition function
			// later.)
			double[] prob = new double[numStates];
			for(int i = 0; i < numStates; i++)
			{
				// a threshold rule version
				// prob[i] = Math.pow(Math.E, socialForce[i] /
				// socialTemperature)
				// * Math.pow(Math.atan((((double) numberOfEachState[i])
				// / (double) neighbors.length) - 2.0)+(Math.PI/2.0), 1.0 /
				// socialTemperature);

				// the majority probably rules version
				prob[i] = Math.pow(Math.E, socialForceLocalCopy[i]
						/ socialTemperatureLocalCopy)
						* Math.pow(((double) numberOfEachState[i])
								/ ((double) neighbors.length + 1),
								1.0 / socialTemperatureLocalCopy);
			}

			// calculate the partition function
			double z = 0.0;
			for(int i = 0; i < numStates; i++)
			{
				z += prob[i];
			}

			// now divide each probability by the partition function (so
			// scaled
			// properly)
			for(int i = 0; i < numStates; i++)
			{
				prob[i] /= z;
			}

			// get cumulative probability of each state
			double[] cumProb = new double[numStates];
			cumProb[0] = prob[0];
			for(int i = 1; i < numStates; i++)
			{
				cumProb[i] = cumProb[i - 1] + prob[i];
			}

			// Now get a random number between 0 and 1
			double randomNumber = random.nextDouble();

			// use the random number to choose a state (j is the state)
			int j = 0;
			while((randomNumber > cumProb[j]) && (j < numStates))
			{
				j++;
			}

			cellValue = j;
		}

		// now bias the result so that even after choosing based on their
		// social network, the individual will sometimes increase their
		// weight regardless of their neighbors. The rational is that
		// environmental and biological factors make it more likely for the
		// individual to gain weight. Make this happen with some (presumably
		// low) probability. This is essentially a "biased noise".
		if(random.nextDouble() < probabilityOfSpontaneousWeightGainLocalCopy)
		{
			// only increase if not already at the maximum
			if(cellValue < numStates - 1)
			{
				cellValue++;
			}
		}

		// now add a random noise that moves the cell value up or down only one
		// unit. In other words, with a certain probability, the cell increases
		// or decreases weight by one unit.
		if(random.nextDouble() < upDownNoiseThresholdLocalCopy)
		{
			// change the state randomly
			if(random.nextBoolean())
			{
				// only increase if not already at the maximum
				if(cellValue < numStates - 1)
				{
					cellValue += 1;
				}
			}
			else
			{
				// only decrease if not already at the minimum
				if(cellValue > 0)
				{
					cellValue -= 1;
				}
			}
		}

		// now add unbiased noise -- this changes the state randomly. The
		// probabilityOfSpontaneousWeightGain
		// works like a biased noise that moves toward greater weight. This is
		// just a general noise that changes weights to a random new value.
		if(random.nextDouble() < noiseThresholdLocalCopy)
		{
			// change the state randomly
			cellValue = random.nextInt(numStates);
		}

		// finally, check to see if the cell has moved up or down more than one
		// weight (BMI) unit. If it has, restrict the movement to only one unit.
		// But only do this if a flag has been set in the More Properties panel.
		if(restrictToMovementOfOneUnitLocalCopy)
		{
			if(cellValue - cell > 1)
			{
				// only move up one unit
				cellValue = cell + 1;
			}
			else if(cell - cellValue > 1)
			{
				// only move up one unit
				cellValue = cell - 1;
			}
		}

		return cellValue;
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
		if(additionalPropertiesPanel == null)
		{
			// create the social forces (only if necessary)
			if(forceSliderLabel == null || forceSliderLabel.length != numStates)
			{
				forceSliderLabel = new JLabel[numStates];
				socialForce = new double[numStates];
				for(int i = 0; i < socialForce.length; i++)
				{
					socialForce[i] = 0.0;
				}

				socialForceLocalCopy = new double[numStates];
				for(int i = 0; i < socialForceLocalCopy.length; i++)
				{
					socialForceLocalCopy[i] = 0.0;
				}
			}

			additionalPropertiesPanel = createAdditionalPropertiesPanel();
		}

		return additionalPropertiesPanel;
	}

	/**
	 * Reacts to the "restrict movement" checkbox.
	 */
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(RESTRICT_MOVEMENT))
		{
			if(restrictMovementCheckBox.isSelected())
			{
				restrictToMovementOfOneUnit = true;
			}
			else
			{
				restrictToMovementOfOneUnit = false;
			}
		}
	}

	/**
	 * Creates a JPanel that may request specific input information that the
	 * rule needs to operate correctly.
	 * 
	 * @return A JPanel requesting specific input information that the rule
	 *         needs to operate correctly. May be null.
	 */
	public JPanel createAdditionalPropertiesPanel()
	{
		// the panel on which we add the controls
		JPanel additionalPropPanel = new JPanel(new GridBagLayout());

		// the 5 is a cheat that handles an unknown scroll bar width and border
		// inset
		int width = CAFrame.tabbedPaneDimension.width - 30;
		int height = 1210;

		Fonts fonts = new Fonts();

		// slider descriptions
		String probWeightGainDescription = "Each individual has a weight "
				+ "(or BMI) that "
				+ "is determined in part by the weights (or BMIs) of other "
				+ "individuals in their social network.  Another component is the "
				+ "biological / environmental \"imperative\" to gain weight. "
				+ "The following sets the probability that an individual gains "
				+ "a unit of weight due to this biological (or other) component. "
				+ "Essentially it is a bias towards gaining (rather than losing) "
				+ "weight. ";
		MultilineLabel probWeightGainDescriptionLabel = new MultilineLabel(
				probWeightGainDescription);
		probWeightGainDescriptionLabel.setFont(fonts
				.getMorePropertiesDescriptionFont());

		String temperatureDescription = "Social temperature is a measure of the "
				+ "volatility or irrationality of a group.  Higher temperatures mean that each "
				+ "individual is more likely to change their weight at the "
				+ "slightest provocation.  A low temperature means that "
				+ "individuals are harder to influence and are more likely to "
				+ "keep their current weight.";
		MultilineLabel temperatureDescriptionLabel = new MultilineLabel(
				temperatureDescription);
		temperatureDescriptionLabel.setFont(fonts
				.getMorePropertiesDescriptionFont());

		String forceDescription = "Social force is a measure of external influences "
				+ "such as advertising, government incentives, punishments, etc. that will "
				+ "modify behavior.  For example, there may be advertisements telling us to "
				+ "move away from obesity and towards normal weight.";
		MultilineLabel forceDescriptionLabel = new MultilineLabel(
				forceDescription);
		forceDescriptionLabel.setFont(fonts.getMorePropertiesDescriptionFont());

		String noiseDescription = "Random and unknown factors may contribute to seemingly "
				+ "random changes in weight (or BMI).  This sets the level of randomness. "
				+ "Each individual will change weight to a new value with the probability "
				+ "set below.  A value of 0.0 means there is no noise.  A value of 1.0 is "
				+ "all noise and creates a completely random simulation.";
		MultilineLabel noiseDescriptionLabel = new MultilineLabel(
				noiseDescription);
		noiseDescriptionLabel.setFont(fonts.getMorePropertiesDescriptionFont());

		String upDownNoiseDescription = "This is similar to the unbiased noise above, except "
				+ "that each individual moves randomly up or down only one unit of weight "
				+ "(or BMI).  As above, the motivation is that random and unknown factors "
				+ "may contribute to seemingly random changes in weight (or BMI). A value "
				+ "of 0.0 means there is no noise.  A value of 1.0 is "
				+ "all noise and means that every individual is moving up and down with random "
				+ "weight gain/loss.  Individuals cannot move below the lowest weight or above "
				+ "the highest weight.";
		MultilineLabel upDownNoiseDescriptionLabel = new MultilineLabel(
				upDownNoiseDescription);
		upDownNoiseDescriptionLabel.setFont(fonts
				.getMorePropertiesDescriptionFont());

		String restrictMovementDescription = "Individuals may be restricted to moving "
				+ "up and down "
				+ "by only one unit of weight (or BMI).  The individual may decide to move "
				+ "to a new weight that is two or more units away, but this adjusts that "
				+ "decision and allows movement of only one step in the given direction. ";
		MultilineLabel restrictMovementDescriptionLabel = new MultilineLabel(
				restrictMovementDescription);
		restrictMovementDescriptionLabel.setFont(fonts
				.getMorePropertiesDescriptionFont());

		// slider labels
		String output = longDecimalFormatter
				.format(probabilityOfSpontaneousWeightGain);
		probabilitySliderLabel = new JLabel("probability = " + output);
		probabilitySliderLabel.setFont(fonts.getBoldFont());

		output = longDecimalFormatter.format(socialTemperature);
		temperatureSliderLabel = new JLabel("temperature = " + output);
		temperatureSliderLabel.setFont(fonts.getBoldFont());

		output = longDecimalFormatter.format(noiseThreshold);
		noiseSliderLabel = new JLabel("noise = " + output);
		noiseSliderLabel.setFont(fonts.getBoldFont());

		output = longDecimalFormatter.format(upDownNoiseThreshold);
		upDownNoiseSliderLabel = new JLabel("noise (single unit up or down) = "
				+ output);
		upDownNoiseSliderLabel.setFont(fonts.getBoldFont());

		for(int state = 0; state < numStates; state++)
		{
			output = longDecimalFormatter.format(socialForce[state]);
			forceSliderLabel[state] = new JLabel("force = " + output);
			forceSliderLabel[state].setFont(fonts.getBoldFont());

			// set the color of the label to be the same as the color of the
			// state it represents
			Color stateColor = this.getCompatibleCellStateView()
					.getDisplayColor(new IntegerCellState(state), null,
							new Coordinate(0, 0));
			forceSliderLabel[state].setForeground(stateColor);
		}

		// create the slider that causes random weight gain due to biological
		// imperatives that make it easier to gain weight.
		probSlider = createRandomWeightGainSlider();

		// create the slider for social temperature
		temperatureSlider = createTemperatureSlider();

		// create the slider for noise
		noiseSlider = createNoiseSlider();

		// create the slider for up-down noise
		upDownNoiseSlider = createUpDownNoiseSlider();

		// create a checkBox for restricting movement up and down by only one
		// unit at a time
		restrictMovementCheckBox = createMovementRestrictionCheckBox();

		// create the sliders for social forces
		forceSlider = createForceSliders();

		// create a scroll pane to hold all the force sliders
		JScrollPane forceScrollPane = createForceSliderPanel(width, height);

		// create panel for the temperature
		Font titleFont = new Fonts().getItalicSmallerFont();
		Color titleColor = Color.BLUE;
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Temperature", TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		JPanel temperaturePanel = new JPanel(new GridBagLayout());
		temperaturePanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder, titledBorder));
		int row = 0;
		temperaturePanel.add(temperatureDescriptionLabel, new GBC(1, row)
				.setSpan(5, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(
						GBC.WEST).setInsets(1, 3, 3, 3));
		row++;
		temperaturePanel.add(temperatureSlider, new GBC(1, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		row++;
		temperaturePanel.add(temperatureSliderLabel, new GBC(1, row).setSpan(5,
				1).setFill(GBC.VERTICAL).setWeight(1.0, 1.0).setAnchor(
				GBC.CENTER).setInsets(1));

		// create panel for the forces
		Border outerEmptyBorder2 = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder2 = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Social Forces", TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		JPanel forcePanel = new JPanel(new GridBagLayout());
		forcePanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder2, titledBorder2));
		row = 0;
		forcePanel.add(forceDescriptionLabel, new GBC(1, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1, 3, 3, 3));
		row++;
		forcePanel.add(forceScrollPane, new GBC(1, row).setSpan(5, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(10,
				1, 10, 1));

		// create panel for the probability
		Border outerEmptyBorder3 = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder3 = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Innate Weight Gain (Biased Noise)",
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		JPanel probabilityPanel = new JPanel(new GridBagLayout());
		probabilityPanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder3, titledBorder3));
		row = 0;
		probabilityPanel.add(probWeightGainDescriptionLabel, new GBC(1, row)
				.setSpan(5, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(
						GBC.WEST).setInsets(1, 3, 3, 3));
		row++;
		probabilityPanel.add(probSlider, new GBC(1, row).setSpan(5, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		row++;
		probabilityPanel.add(probabilitySliderLabel, new GBC(1, row).setSpan(5,
				1).setFill(GBC.VERTICAL).setWeight(1.0, 1.0).setAnchor(
				GBC.CENTER).setInsets(1));

		// create panel for the noise
		Border outerEmptyBorder4 = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder4 = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Unbiased Noise", TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		JPanel noisePanel = new JPanel(new GridBagLayout());
		noisePanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder4, titledBorder4));
		row = 0;
		noisePanel.add(noiseDescriptionLabel, new GBC(1, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1, 3, 3, 3));
		row++;
		noisePanel.add(noiseSlider, new GBC(1, row).setSpan(5, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		row++;
		noisePanel.add(noiseSliderLabel, new GBC(1, row).setSpan(5, 1).setFill(
				GBC.VERTICAL).setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
				.setInsets(1));

		// create panel for the up-down noise
		Border outerEmptyBorder6 = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder6 = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Up-Down Noise", TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		JPanel upDownNoisePanel = new JPanel(new GridBagLayout());
		upDownNoisePanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder6, titledBorder6));
		row = 0;
		upDownNoisePanel.add(upDownNoiseDescriptionLabel, new GBC(1, row)
				.setSpan(5, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(
						GBC.WEST).setInsets(1, 3, 3, 3));
		row++;
		upDownNoisePanel.add(upDownNoiseSlider, new GBC(1, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		row++;
		upDownNoisePanel.add(upDownNoiseSliderLabel, new GBC(1, row).setSpan(5,
				1).setFill(GBC.VERTICAL).setWeight(1.0, 1.0).setAnchor(
				GBC.CENTER).setInsets(1));

		// create a scroll panel for all the noise components
		JPanel allNoisePanels = new JPanel(new GridBagLayout());
		row = 0;
		allNoisePanels.add(noisePanel, new GBC(1, row).setSpan(5, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1,
				3, 3, 3));
		row++;
		allNoisePanels.add(upDownNoisePanel, new GBC(1, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		row++;
		allNoisePanels.add(probabilityPanel, new GBC(1, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		JScrollPane noiseComponentsScroller = new JScrollPane(allNoisePanels);
		int scrollPaneWidth = width;
		int scrollPaneHeight = height / 4;
		noiseComponentsScroller.setPreferredSize(new Dimension(scrollPaneWidth,
				scrollPaneHeight));
		noiseComponentsScroller.setMinimumSize(new Dimension(scrollPaneWidth,
				scrollPaneHeight));
		noiseComponentsScroller.setMaximumSize(new Dimension(scrollPaneWidth,
				scrollPaneHeight));
		noiseComponentsScroller
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		Border outerEmptyBorder5 = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder5 = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Noise", TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		JPanel allNoisePanel = new JPanel(new GridBagLayout());
		allNoisePanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder5, titledBorder5));
		row = 0;
		allNoisePanel.add(noiseComponentsScroller, new GBC(1, row)
				.setSpan(5, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(
						GBC.WEST).setInsets(1, 3, 3, 3));

		// create panel for the "restrict movement"
		Border outerEmptyBorder7 = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder7 = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Restrict movement to one unit",
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		JPanel restrictPanel = new JPanel(new GridBagLayout());
		restrictPanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder7, titledBorder7));
		row = 0;
		restrictPanel.add(restrictMovementDescriptionLabel, new GBC(1, row)
				.setSpan(5, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(
						GBC.WEST).setInsets(1, 3, 3, 3));
		row++;
		restrictPanel.add(restrictMovementCheckBox, new GBC(1, row).setSpan(5,
				1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		//
		//
		//
		//
		//
		// Now create a final panel that holds all of the subpanels
		JPanel allComponentsPanel = new JPanel(new GridBagLayout());

		// temperature
		row = 0;
		allComponentsPanel.add(temperaturePanel, new GBC(1, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// force
		row++;
		allComponentsPanel.add(forcePanel, new GBC(1, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// all noise components
		row++;
		allComponentsPanel.add(allNoisePanel, new GBC(1, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// movement restriction
		row++;
		allComponentsPanel.add(restrictPanel, new GBC(1, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// now put the components in the additional properties panel
		row = 0;
		additionalPropPanel.add(allComponentsPanel, new GBC(1, row).setSpan(5,
				1).setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0));
		row++;
		additionalPropPanel.add(new JLabel(" "), new GBC(1, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(5.0, 5.0).setAnchor(GBC.WEST)
				.setInsets(0));

		return additionalPropPanel;
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
		String[] folders = {RuleFolderNames.PROBABILISTIC_FOLDER,
				RuleFolderNames.SOCIAL_FOLDER,
				RuleFolderNames.OBESITY_RESEARCH_FOLDER};

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
	 * Handles notification of any changes in properties, and by default does
	 * nothing (as written in the parent class). Here I override to handle
	 * notification of changes in the color scheme.
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		// check if the color scheme has changed
		if(event.getPropertyName().equals(CurrentProperties.COLORS_CHANGED))
		{
			if(this.getCompatibleCellStateView() != null
					&& forceSliderLabel != null
					&& additionalPropertiesPanel != null)
			{
				// colors have changed, so reset the colors
				for(int state = 0; state < numStates; state++)
				{
					// set the color of the label to be the same as the
					// color of the state it represents
					Color stateColor = this.getCompatibleCellStateView()
							.getDisplayColor(new IntegerCellState(state), null,
									new Coordinate(0, 0));
					forceSliderLabel[state].setForeground(stateColor);
				}

				// and update the display
				additionalPropertiesPanel.repaint();
			}
		}
		else if(event.getPropertyName().equals(CurrentProperties.SETUP))
		{
			additionalPropertiesPanel = null;
		}
	}

	/**
	 * A listener for slider activity on the additional properties panel.
	 * 
	 * @author David Bahr
	 */
	private class SliderListener implements ChangeListener
	{
		/**
		 * Listens for changes to the probability slider.
		 * 
		 * @param e
		 */
		public void stateChanged(ChangeEvent e)
		{
			if(e.getSource().equals(probSlider) && probSlider != null
					&& probabilitySliderLabel != null)
			{
				// get the probability value in *arbitrary units provided by the
				// slider* that's why I divide by MAX_SLIDER_VALUE
				double updatedSliderValue = probSlider.getValue()
						/ (double) MAX_SLIDER_VALUE;

				// make sure the value changed
				if(probabilityOfSpontaneousWeightGain != updatedSliderValue)
				{
					probabilityOfSpontaneousWeightGain = updatedSliderValue;

					// change the display
					String output = longDecimalFormatter
							.format(probabilityOfSpontaneousWeightGain);
					probabilitySliderLabel.setText("probability = " + output);
				}
			}
			else if(e.getSource().equals(temperatureSlider)
					&& temperatureSlider != null
					&& temperatureSliderLabel != null)
			{
				// get the temperature value in *arbitrary units provided by the
				// slider* that's why I divide by MAX_SLIDER_VALUE
				double updatedSliderValue = MIN_TEMPERATURE
						+ ((temperatureSlider.getValue() / (double) MAX_SLIDER_VALUE) * (MAX_TEMPERATURE - MIN_TEMPERATURE));

				// make sure the value changed
				if(socialTemperature != updatedSliderValue)
				{
					socialTemperature = updatedSliderValue;

					// change the display
					String output = longDecimalFormatter
							.format(socialTemperature);
					temperatureSliderLabel.setText("temperature = " + output);
				}
			}
			else if(e.getSource().equals(noiseSlider) && noiseSlider != null
					&& noiseSliderLabel != null)
			{
				// get the noise value in *arbitrary units provided by the
				// slider* that's why I divide by MAX_SLIDER_VALUE
				double updatedSliderValue = MIN_NOISE
						+ ((noiseSlider.getValue() / (double) MAX_SLIDER_VALUE) * (MAX_NOISE - MIN_NOISE));

				// make sure the value changed
				if(noiseThreshold != updatedSliderValue)
				{
					noiseThreshold = updatedSliderValue;

					// change the display
					String output = longDecimalFormatter.format(noiseThreshold);
					noiseSliderLabel.setText("noise = " + output);
				}
			}
			else if(e.getSource().equals(upDownNoiseSlider)
					&& upDownNoiseSlider != null
					&& upDownNoiseSliderLabel != null)
			{
				// get the noise value in *arbitrary units provided by the
				// slider* that's why I divide by MAX_SLIDER_VALUE
				double updatedSliderValue = MIN_NOISE
						+ ((upDownNoiseSlider.getValue() / (double) MAX_SLIDER_VALUE) * (MAX_NOISE - MIN_NOISE));

				// make sure the value changed
				if(upDownNoiseThreshold != updatedSliderValue)
				{
					upDownNoiseThreshold = updatedSliderValue;

					// change the display
					String output = longDecimalFormatter
							.format(upDownNoiseThreshold);
					upDownNoiseSliderLabel
							.setText("noise (single unit up or down) = "
									+ output);
				}
			}
			else
			{
				for(int state = 0; state < numStates; state++)
				{
					if(e.getSource().equals(forceSlider[state])
							&& forceSlider[state] != null
							&& forceSliderLabel[state] != null)
					{
						// get the force value in *arbitrary units
						// provided by the slider* that's why I divide by
						// MAX_SLIDER_VALUE
						double updatedSliderValue = MIN_FORCE
								+ ((forceSlider[state].getValue() / (double) MAX_SLIDER_VALUE) * (MAX_FORCE - MIN_FORCE));

						// make sure the value changed
						if(socialForce[state] != updatedSliderValue)
						{
							socialForce[state] = updatedSliderValue;

							// change the display
							String output = longDecimalFormatter
									.format(socialForce[state]);
							forceSliderLabel[state]
									.setText("force = " + output);
						}
					}
				}
			}
		}
	}
}
