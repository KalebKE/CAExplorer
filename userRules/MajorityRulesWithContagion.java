package userRules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cellularAutomata.CAController;
import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.rules.templates.IntegerRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;
import cellularAutomata.util.math.RandomSingleton;
import cellularAutomata.util.Coordinate;

/**
 * Rule for assets markets based on your neighbors asset. Each cell represents a
 * trader/investor who trades in only one asset. Each state represents an asset.
 * There can be an infinite number of assets. In this model each person looks at
 * all of their neighbors and is assigned a probability of buying (or selling)
 * an asset based on the percentage of their neighbors who own that asset. For
 * example, if there are four 0's, two 1's, and three 4's, then the odds are 4/9
 * that the cell becomes a 0, 2/9 that the cell becomes a 1, and 3/9 that the
 * cell becomes a 4. Note that the value of the cell itself is excluded.
 * 
 * <p>
 * In addition, this model includes social temperature, forces, and noise. See
 * papers by Bahr and Passerini for details.
 * 
 * <p>
 * Lastly, this model includes social temperatures, forces, and noise for
 * opinion leaders, which are cells with large neighborhood sizes that tend to
 * influence the actions of its many neighboring cells.
 */
public class MajorityRulesWithContagion extends IntegerRuleTemplate
{

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

	private static int NUM_ATTITUDES;

	// A pattern used to display decimals.
	private static final String LONG_DECIMAL_PATTERN = "0.000";

	// A pattern used to display decimals.
	private static final String SHORT_DECIMAL_PATTERN = "0.0";

	// tool tip for the social force
	private static final String FORCE_TIP = "The social force "
			+ "that pushes individuals towards trading or not trading. "
			+ "See rule description.";

	// tool tip for the noise
	private static final String NOISE_TIP = "Probability that individual will "
			+ "randomly change assets. The amout of noise in the system.";

	// label for the check box that restricts movement up or down one unit
	// at a time
	// ***NOT USED IN MARKET MODEL***
	private static final String RESTRICT_MOVEMENT = "Not applicable to Market Model";

	// tool tip for the check box that restricts movement up or down one
	// unit at a time
	private static final String RESTRICT_MOVEMENT_TOOLTIP = "<html><body>// "
			+ "***NOT USED IN MARKET MODEL***</body></html>";

	// tool tip for the social temperature
	private static final String TEMPERATURE_TIP = "The social temperature "
			+ "(volatility/irrationality).  Low temperatures are more stable.  "
			+ "See rule description.";

	// tool tip for the "up-down by one unit" noise
	private static final String UPDOWN_NOISE_TIP = "Probability that an individual "
			+ "will randomly increase or decrease assets by one unit.";

	// a display name for this class
	private static final String RULE_NAME = "Market Model Contagion";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, try a small world lattice with 100 by "
			+ "100 neighborhood size. Use 10 states to represent ten major assets "
			+ "being traded. Enter even probabilities for each state and let the "
			+ "model run for five hundred generations before manipulating any "
			+ "opinion leaders. " + leftClickInstructions
			+ rightClickInstructions + "</body></html>";

	// when true, each cell is clamped so that it can only move up or down
	// one weight (or BMI) unit at a time. The cell decides on a new weight
	// independantly of the clamp (as if free to move up 2 or more weight
	// units), and then the clamp acts afterwords to restrict the movement
	// to one unit up or down.
	// ***NOT USED IN MARKET MODEL***
	private static volatile boolean restrictToMovementOfOneUnit = false;

	// a local copy of the restricted movement, updated once per generation
	private static volatile boolean restrictToMovementOfOneUnitLocalCopy = false;

	// indicates if the number of neighbors has been initialized and filled
	private static volatile boolean filled = false;

	// indicates if the number of neighbor is sorted
	private static volatile boolean sorted = false;

	// probability of random opnion changes (noise in the system).
	private static volatile double noiseThreshold = 0.0;

	// probability of random opinion changes of opinion leaders (noise in the
	// opinion leaders)
	private static volatile double[] noiseThresholdOL = null;

	// a local copy of the random weight changes (noise in the system),
	// updated once per generation
	private static volatile double noiseThresholdLocalCopy = 0.0;

	// probability of random opinion changes of opinion leaders (noise in the
	// opinion leaders)
	// updated once per generation
	private static volatile double[] noiseThresholdOLLocalCopy = null;

	// the temperature of the group of all individuals
	private static volatile double socialTemperature = DEFAULT_TEMPERATURE;

	// the temperature of each states opinion leaders
	private static volatile double[] socialTemperatureOL = null;

	// a local copy of the social temperature, updated once per generation
	private static volatile double socialTemperatureLocalCopy = DEFAULT_TEMPERATURE;

	// the temperature of each states opinion leaders, updated once per
	// generation
	private static volatile double[] socialTemperatureOLLocalCopy = null;

	// probability of random opinion increase by +-1 unit.
	private static volatile double upDownNoiseThreshold = 0.0;

	// a local copy of the threshold, updated once per generation
	private static volatile double upDownNoiseThresholdLocalCopy = 0.0;

	// the force applied toward each state
	private static volatile double[] socialForce = null;

	// the force applied toward each states opinion leaders
	private static volatile double[][] socialForceOL = null;

	// a local copy of the social force, updated once per generation
	private static volatile double[] socialForceLocalCopy = null;

	// the force applied toward each states opinion leaders, updated once per
	// generation
	private static volatile double[][] socialForceOLLocalCopy = null;

	// the current generation being processed by the rule
	private static volatile int currentGeneration = -1;

	// the current number of opinion leaders selected
	private static volatile int numOpinionLeaders;

	private static volatile int numLinkedStates = 5;

	// array that stores the fundamental value of each state
	private static double fundamentalValue[];

	// array that stores the current value of the state
	private static double currentValue[];

	// array that stores the change in quantity of each state
	private static int transactionQuantity[];

	// array that stores the volume of each state at the moment
	private static int currentQuantity[];

	// array that stores the volume of each state for the previous generation
	private static int previousQuantity[];

	// array that stores the previous value of the state
	private static double previousValue[];

	// Variable that stores the current generation, updated once per generation
	private static int temporaryGeneration;

	// scaler for the quantity, needs to be updated current time its the Cp
	// it should be a value between 0 and 1
	private static double quantityScaler;

	// the number of states -- reset by the integerRule method
	private static int numStates;

	// array to store the number of neighbors for each cell
	private static volatile ArrayList<Integer> numNeighbors;

	// label that displays the noise
	private static JLabel noiseSliderLabel = null;

	// label that displays the noise
	private static JLabel[] noiseSliderLabelOL = null;

	// label that displays the number of opinion leaders
	private static JLabel numOpinionLeadersLabel = null;

	// label that displays the probability of gaining weight
	private static JLabel[] probabilitySliderLabelOL = null;

	// label that displays the temperature
	private static JLabel temperatureSliderLabel = null;

	// label that displays the temperature
	private static JLabel[] temperatureSliderLabelOL = null;

	// label that displays the noise that moves up or down one unit
	private static JLabel upDownNoiseSliderLabel = null;

	// lable for the opinionLeadersRank JSpinner
	private static JLabel opinionLeaderRankLabel = null;

	// labels for the social forces
	private static JLabel[] forceSliderLabel = null;

	// labels for the opinion leader social forces
	private static JLabel[][] forceSliderLabelOL = null;

	// sliders that set the social forces
	private static JSlider[] forceSlider = null;

	// sliders that set the opinion leader social forces
	private static JSlider[][] forceSliderOL = null;

	// slider that sets the noise level
	private static JSlider noiseSlider = null;

	// contains the sliders that set the noise level for opinion leaders
	private static JSlider[] noiseSliderOL = null;

	// slider that sets the temperature
	private static JSlider temperatureSlider = null;

	// slider that sets the temperature of opinion leaders
	private static JSlider[] temperatureSliderOL = null;

	// slider that sets the "up-down by one unit" noise level
	private static JSlider upDownNoiseSlider = null;

	// Spinner box to set the rank of the opinion leaders
	private static JSpinner rankOpinionLeaders = null;

	// the additional properties panel
	private static JPanel additionalPropertiesPanel = null;

	private static SpinnerNumberModel model;

	// an array that stores the all the cells, and sorts them by neighborhood
	// size, the last one being the cell with the greatest number of neighbors
	private volatile int[] opinionLeaders = null;

	// formats decimals for display
	private DecimalFormat longDecimalFormatter = new DecimalFormat(
			LONG_DECIMAL_PATTERN);

	// formats decimals for display
	private DecimalFormat shortDecimalFormatter = new DecimalFormat(
			SHORT_DECIMAL_PATTERN);

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b> A model of a financial market and how the opinion of "
			+ "influential people spreads through market networks.</body></html>";

	/**
	 * Create the market rule using the given cellular automaton properties.
	 * <p>
	 * When calling the parent constructor, the minimalOrLazyInitialization
	 * parameter must be included as
	 * <code>super(minimalOrLazyInitialization);</code>. The boolean is intended
	 * to indicate when the constructor should build a rule with as small a
	 * footprint as possible. In order to load rules by reflection, the
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
	public MajorityRulesWithContagion(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		// sets the variable filled to false
		filled = false;

		// the array is initialized to 0
		numNeighbors = new ArrayList(0);

		if (!minimalOrLazyInitialization)
		{
			// set by the user
			numStates = CurrentProperties.getInstance().getNumStates();
			NUM_ATTITUDES = numStates;

			// initialize variable to 0
			temporaryGeneration = 0;

			// fill array with indexes according to the number of states
			// Chosen by the user
			fundamentalValue = new double[numStates];

			// fill array with indexes according to the number of states
			// Chosen by the user
			currentValue = new double[numStates];

			// fill array with indexes according to the number of states
			// Chosen by the user
			transactionQuantity = new int[numStates];

			// fill array with indexes according to the number of states
			// Chosen by the user
			previousQuantity = new int[numStates];

			previousValue = new double[numStates];

			Random r = new Random();

			// initializing the fundamental value and the current value,
			// transaction
			// quantity and previous quantity
			for (int i = 0; i < numStates; i++)
			{
				fundamentalValue[i] = 30;
				currentValue[i] = 30;
				transactionQuantity[i] = 0;
				previousQuantity[i] = 0;
				previousValue[i] = 30;
			}

			// create the social forces (only if necessary)
			if (forceSliderLabel == null
					|| forceSliderLabel.length != numStates)
			{
				// force the additional properties panel to be recreated
				additionalPropertiesPanel = null;

				forceSliderLabel = new JLabel[numStates];
				socialForce = new double[numStates];
				for (int i = 0; i < socialForce.length; i++)
				{
					socialForce[i] = 0.0;
				}

				socialForceLocalCopy = new double[numStates];
				for (int i = 0; i < socialForceLocalCopy.length; i++)
				{
					socialForceLocalCopy[i] = 0.0;
				}
			}

			// create the opinion leader social forces (only if necessary)
			if (forceSliderLabelOL == null
					|| forceSliderLabelOL.length != NUM_ATTITUDES)
			{
				// force the additional properties panel to be recreated
				additionalPropertiesPanel = null;

				// initialize the social forces for the opinion leaders
				// double array [number of investing attitudes][number of assets
				// available]
				socialForceOL = new double[NUM_ATTITUDES][numStates];

				// ditto local copy
				socialForceOLLocalCopy = new double[NUM_ATTITUDES][numStates];

				// ditto Labels
				forceSliderLabelOL = new JLabel[NUM_ATTITUDES][numStates];

				for (int i = 0; i < NUM_ATTITUDES; i++)
				{
					for (int j = 0; j < numStates; j++)
					{
						socialForceOL[i][j] = 0.0;
						socialForceOLLocalCopy[i][j] = 0.0;
					}
				}

				// create the opinion leader noise threshold (only if necessary)
				if (noiseSliderLabelOL == null
						|| noiseSliderLabelOL.length != NUM_ATTITUDES)
				{
					// force the additional properties panel to be recreated
					additionalPropertiesPanel = null;

					noiseSliderLabelOL = new JLabel[NUM_ATTITUDES];
					noiseThresholdOL = new double[NUM_ATTITUDES];
					noiseThresholdOLLocalCopy = new double[NUM_ATTITUDES];

					for (int i = 0; i < NUM_ATTITUDES; i++)
					{
						noiseThresholdOL[i] = 0.0;
						noiseThresholdOLLocalCopy[i] = 0.0;
					}
				}

				// create the opinion leader temperatures
				if (temperatureSliderLabelOL == null
						|| temperatureSliderLabelOL.length != NUM_ATTITUDES)
				{
					// force the additional properties panel to be recreated
					additionalPropertiesPanel = null;

					temperatureSliderLabelOL = new JLabel[NUM_ATTITUDES];
					socialTemperatureOL = new double[NUM_ATTITUDES];
					socialTemperatureOLLocalCopy = new double[NUM_ATTITUDES];

					for (int i = 0; i < NUM_ATTITUDES; i++)
					{
						socialTemperatureOL[i] = DEFAULT_TEMPERATURE;
						socialTemperatureOLLocalCopy[i] = DEFAULT_TEMPERATURE;
					}
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
		for (int state = 0; state < numStates; state++)
		{
			// spacing above each label, used to separate the sliders
			int verticalSpace = 12;
			if (state == 0)
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
	 * Create scroll panels that hold all the force sliders for the opinion
	 * leaders.
	 * 
	 * @param width
	 *            The width of the additional properties panel.
	 * @param height
	 *            The height of the additional properties panel.
	 * @return scrollPane of sliders.
	 */
	private JScrollPane[] createForceSliderPanelOL(int width, int height)
	{
		JPanel[] sliderPanel = new JPanel[numStates];
		JScrollPane[] forceScroller = new JScrollPane[numStates];

		// initialize the JPanels and the JScollPanes for each states opinion
		// leaders
		for (int i = 0; i < numStates; i++)
		{
			sliderPanel[i] = new JPanel(new GridBagLayout());
			forceScroller[i] = new JScrollPane(sliderPanel[i]);
		}

		int row = 0;
		// outer loop fills the JScollPane with the JPanels
		for (int state = 0; state < NUM_ATTITUDES; state++)
		{
			// inner loop fills the JPanels with the sliders and label or each
			// states opinion leaders
			for (int i = 0; i < numStates; i++)
			{
				// spacing above each label, used to separate the sliders
				int verticalSpace = 12;
				if (state == 0)
				{
					verticalSpace = 1;
				}

				// force label
				row++;
				sliderPanel[state].add(forceSliderLabelOL[state][i], new GBC(1,
						row).setSpan(5, 1).setFill(GBC.VERTICAL).setWeight(1.0,
						1.0).setAnchor(GBC.CENTER).setInsets(verticalSpace, 1,
						0, 1));

				// force slider
				row++;
				sliderPanel[state].add(forceSliderOL[state][i], new GBC(1, row)
						.setSpan(5, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0)
						.setAnchor(GBC.WEST).setInsets(1));
			}

			// expands to fill all extra space
			row++;
			sliderPanel[state].add(new JLabel(" "), new GBC(1, row).setSpan(5,
					1).setFill(GBC.BOTH).setWeight(20.0, 20.0).setAnchor(
					GBC.WEST).setInsets(0));

			int scrollPaneWidth = width;
			int scrollPaneHeight = (int) (height * 0.275);
			forceScroller[state].setPreferredSize(new Dimension(
					scrollPaneWidth, scrollPaneHeight));
			forceScroller[state].setMinimumSize(new Dimension(scrollPaneWidth,
					scrollPaneHeight));
			forceScroller[state].setMaximumSize(new Dimension(scrollPaneWidth,
					scrollPaneHeight));
			forceScroller[state]
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		}

		return forceScroller;
	}

	/**
	 * Create the sliders for the social forces (one slider per state).
	 */
	private JSlider[] createForceSliders()
	{
		JSlider[] allForceSliders = new JSlider[numStates];
		SliderListener[] listeners = new SliderListener[numStates];

		for (int i = 0; i < numStates; i++)
		{
			listeners[i] = new SliderListener();
		}

		// create one slider for each state
		for (int state = 0; state < numStates; state++)
		{
			// create a slider for force
			int numTickMarks = 8;
			int majorTickSpacing = (int) Math.round(MAX_SLIDER_VALUE
					/ numTickMarks);

			JSlider slider = new JSlider(
					0,
					MAX_SLIDER_VALUE,
					(int) (MAX_SLIDER_VALUE * (socialForce[state] - MIN_FORCE) / (MAX_FORCE - MIN_FORCE)));
			slider.addChangeListener(listeners[state]);
			slider.setToolTipText(FORCE_TIP);

			// set tick marks and labels for the slider
			slider.setMajorTickSpacing(majorTickSpacing);
			slider.setPaintTicks(true);
			slider.setSnapToTicks(false);

			// the hash table of labels
			Hashtable sliderLabelTable = new Hashtable();
			double maxLabelValue = MAX_FORCE;
			double minLabelValue = MIN_FORCE;
			for (int i = 0; i <= numTickMarks; i++)
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
	 * Create the sliders for the opinion leader's social forces (one slider per
	 * state).
	 */
	private JSlider[][] createForceSlidersOL()
	{
		JSlider[][] allForceSliders = new JSlider[NUM_ATTITUDES][numStates];

		// create one slider for each state
		for (int attitude = 0; attitude < NUM_ATTITUDES; attitude++)
		{
			for (int i = 0; i < numStates; i++)
			{
				// create a slider for force
				int numTickMarks = 8;
				int majorTickSpacing = (int) Math.round(MAX_SLIDER_VALUE
						/ numTickMarks);

				JSlider slider = new JSlider(
						0,
						MAX_SLIDER_VALUE,
						(int) (MAX_SLIDER_VALUE
								* (socialForceOL[attitude][i] - MIN_FORCE) / (MAX_FORCE - MIN_FORCE)));
				slider.addChangeListener(new SliderListenerOL());
				slider.setToolTipText(FORCE_TIP);

				// set tick marks and labels for the slider
				slider.setMajorTickSpacing(majorTickSpacing);
				slider.setPaintTicks(true);
				slider.setSnapToTicks(false);

				// the hash table of labels
				Hashtable sliderLabelTable = new Hashtable();
				double maxLabelValue = MAX_FORCE;
				double minLabelValue = MIN_FORCE;
				for (int j = 0; j <= numTickMarks; j++)
				{
					double labelValue = minLabelValue
							+ (j * ((maxLabelValue - minLabelValue) / numTickMarks));
					sliderLabelTable
							.put(new Integer(j * majorTickSpacing), new JLabel(
									""
											+ shortDecimalFormatter
													.format(labelValue)));
				}
				slider.setLabelTable(sliderLabelTable);
				slider.setPaintLabels(true);

				allForceSliders[attitude][i] = slider;
			}
		}

		return allForceSliders;
	}

	/**
	 * Create a slider for random noise.
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
		for (int i = 0; i <= numTickMarks; i++)
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
	 * Create a slider for random noise for opinion leaders (random opinion
	 * changes).
	 */
	private JSlider[] createNoiseSliderOL()
	{
		JSlider[] noiseSliders = new JSlider[NUM_ATTITUDES];

		for (int i = 0; i < NUM_ATTITUDES; i++)
		{
			// create a slider for noise
			int numTickMarks = 5;
			int majorTickSpacing = (int) Math.round(MAX_SLIDER_VALUE
					/ numTickMarks);

			JSlider noiseSlider = new JSlider(
					0,
					MAX_SLIDER_VALUE,
					(int) (MAX_SLIDER_VALUE * (noiseThresholdOL[i] - MIN_NOISE) / (MAX_NOISE - MIN_NOISE)));
			noiseSlider.addChangeListener(new SliderListenerOL());
			noiseSlider.setToolTipText(NOISE_TIP);

			// set tick marks and labels for the slider
			noiseSlider.setMajorTickSpacing(majorTickSpacing);
			noiseSlider.setPaintTicks(true);
			noiseSlider.setSnapToTicks(false);

			// the hash table of labels
			Hashtable sliderLabelTable = new Hashtable();
			double maxLabelValue = MAX_NOISE;
			double minLabelValue = MIN_NOISE;
			for (int j = 0; j <= numTickMarks; j++)
			{
				double labelValue = minLabelValue
						+ (j * ((maxLabelValue - minLabelValue) / numTickMarks));
				sliderLabelTable.put(new Integer(j * majorTickSpacing),
						new JLabel(""
								+ shortDecimalFormatter.format(labelValue)));
			}
			noiseSlider.setLabelTable(sliderLabelTable);
			noiseSlider.setPaintLabels(true);
			noiseSliders[i] = noiseSlider;
		}

		return noiseSliders;
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
		for (int i = 0; i <= numTickMarks; i++)
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
	 * Create the slider that controls social temperature for opinion leaders
	 * (volatility/irrationality).
	 */
	private JSlider[] createTemperatureSliderOL()
	{
		JSlider[] allTemperatureSliders = new JSlider[NUM_ATTITUDES];

		for (int i = 0; i < NUM_ATTITUDES; i++)
		{
			// create a slider for social temperature
			int numTickMarks = 10;
			int majorTickSpacing = (int) Math.round(MAX_SLIDER_VALUE
					/ numTickMarks);

			JSlider temperatureSlider = new JSlider(
					0,
					MAX_SLIDER_VALUE,
					(int) (MAX_SLIDER_VALUE
							* (socialTemperatureOL[i] - MIN_TEMPERATURE) / (MAX_TEMPERATURE - MIN_TEMPERATURE)));
			temperatureSlider.addChangeListener(new SliderListenerOL());
			temperatureSlider.setToolTipText(TEMPERATURE_TIP);

			// set tick marks and labels for the slider
			temperatureSlider.setMajorTickSpacing(majorTickSpacing);
			temperatureSlider.setPaintTicks(true);
			temperatureSlider.setSnapToTicks(false);

			// the hash table of labels
			Hashtable sliderLabelTable = new Hashtable();
			double maxLabelValue = MAX_TEMPERATURE;
			double minLabelValue = MIN_TEMPERATURE;
			for (int j = 0; j <= numTickMarks; j++)
			{
				double labelValue = minLabelValue
						+ (j * ((maxLabelValue - minLabelValue) / numTickMarks));
				sliderLabelTable.put(new Integer(j * majorTickSpacing),
						new JLabel(""
								+ shortDecimalFormatter.format(labelValue)));
			}
			temperatureSlider.setLabelTable(sliderLabelTable);
			temperatureSlider.setPaintLabels(true);
			allTemperatureSliders[i] = temperatureSlider;
		}

		return allTemperatureSliders;
	}

	/**
	 * Create a slider for random noise that moves up or down only one unit at a
	 * time.
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
		for (int i = 0; i <= numTickMarks; i++)
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
	 * Rule for majority finance.
	 * 
	 * @param cell
	 *            The value of the cell being updated.
	 * @param neighbors
	 *            The value of the neighbors.
	 * @param generation
	 *            The current generation of the CA.
	 * @return A new state for the cell.
	 */
	protected synchronized int integerRule(int cell, int[] neighbors,
			int numStates, int generation)
	{
		// If not the 0 state, then set the currentQuantiy (from the previous
		// generation) to previousQuantity. Do this only once at the beginning
		// of each generation.
		if (generation != 0 && currentGeneration != generation)
		{
			// finally, make the previous quantity the current quantity
			previousQuantity = currentQuantity;
		}

		// only update slider parameters (like temp and force) at the beginning
		// of each generation. This prevents some cells from seeing one
		// temperature and other cells seeing another temperature.
		if (currentGeneration != generation)
		{
			currentGeneration = generation;

			calcLinkedSocialForce();

			// The temperature used by the rule. Once per generation it
			// is reset to the socialTemperature selected by the user from
			// the "more properties panel."
			socialTemperatureLocalCopy = socialTemperature;

			// ditto noises
			upDownNoiseThresholdLocalCopy = upDownNoiseThreshold;
			noiseThresholdLocalCopy = noiseThreshold;

			// ditto restricted movement
			restrictToMovementOfOneUnitLocalCopy = restrictToMovementOfOneUnit;

			// ditto social forces and opinion leader temp and noise
			for (int i = 0; i < socialForceLocalCopy.length; i++)
			{
				socialTemperatureOLLocalCopy[i] = socialTemperatureOL[i];
				noiseThresholdOLLocalCopy[i] = noiseThresholdOL[i];

				// average investor social force
				socialForceLocalCopy[i] = socialForce[i];
			}

			// ditto opinion social forces
			for (int i = 0; i < NUM_ATTITUDES; i++)
			{
				for (int j = 0; j < socialForceLocalCopy.length; j++)
				{
					socialForceOLLocalCopy[i][j] = socialForceOL[i][j];
				}
			}

			// update the current number of assets (states) on the lattice
			updateQuantities(generation);

			// calculate the pricing of the assets for this generation
			calculatePricing();

			// indicate the opinion leaders array has been filled
			filled = true;

			// sort the number of neighbors at the beginning of the second
			// iteration to determine which cells are opinion leaders
			if (filled && !sorted)
			{
				// initialize opinion leaders
				initializeOpinionLeaders();

				// indicate the opinion leaders have been sorted and fully
				// initialized
				sorted = true;
			}

			// in the first generation previous quantity will be equal to
			// current
			// quantity
			if (previousQuantity[0] == 0)
			{
				Lattice lattice = CAController.getInstanceOfCAController()
						.getCAFrame().getLattice();

				// get an iterator over the lattice
				Iterator cellIterator = lattice.iterator();

				// get each cell on the lattice
				Cell cellTemp = null;
				int totalNumberOfCells = 0;

				while (cellIterator.hasNext())
				{
					// add one more to the total number of cells
					totalNumberOfCells++;

					// get the cell
					cellTemp = (Cell) cellIterator.next();

					// get its state.
					IntegerCellState state = (IntegerCellState) cellTemp
							.getState(0);

					// increment the number of states
					previousQuantity[state.toInt()]++;
				}
			}
		}

		initializeVariables();

		// store how many cells have each state
		int[] numberOfEachState = new int[numStates];

		// initialize
		Arrays.fill(numberOfEachState, 0);

		// figure out how many cells have each state
		for (int i = 0; i < neighbors.length; i++)
		{
			int state = neighbors[i];

			numberOfEachState[state]++;
		}

		// don't forget the cell itself
		numberOfEachState[cell]++;

		// the value that is returned
		int cellValue = cell;

		// ***Rule for Opinion Leaders***
		// Uses the socialTemperature of the opinion leaders
		// 
		if (opinionLeaders.length > 0
				&& numOpinionLeaders != 0
				&& neighbors.length >= opinionLeaders[(opinionLeaders.length)
						- numOpinionLeaders]
				&& socialTemperatureLocalCopy != 0.0)
		{
			// System.out.println("opinion leader");

			for (int i = 0; i < numStates; i++)
			{
				if (cell == i)
				{
					// System.out.println("A");
					// get probability (percent) of each state, including
					// the social force and temperature. (We divide by the
					// partition function later.)
					double[][] prob = new double[numStates][numStates];

					for (int j = 0; j < numStates; j++)
					{
						// a threshold rule version
						// prob[i] = Math.pow(Math.E, socialForce[i] /
						// socialTemperature)
						// * Math.pow(Math.atan((((double)
						// numberOfEachState[i])
						// / (double) neighbors.length) -
						// 2.0)+(Math.PI/2.0),
						// 1.0 /
						// socialTemperature);

						// the majority probably rules version
						prob[i][j] = Math.pow(Math.E,
								(socialForceOLLocalCopy[i][j])
										/ (socialTemperatureOLLocalCopy[i]))
								* Math
										.pow(
												((double) numberOfEachState[j])
														/ ((double) neighbors.length + 1),
												1.0 / (socialTemperatureOLLocalCopy[i]));
					}

					// calculate the partition function
					double z = 0.0;

					for (int j = 0; j < numStates; j++)
					{
						z += prob[i][j];
					}

					// now divide each probability by the partition
					// function (so scaled properly)
					for (int j = 0; j < numStates; j++)
					{
						prob[i][j] /= z;
					}

					// get cumulative probability of each state
					double[] cumProb = new double[numStates];
					cumProb[0] = prob[i][0];
					for (int j = 1; j < numStates; j++)
					{
						cumProb[j] = cumProb[j - 1] + prob[i][j];
					}

					// Now get a random number between 0 and 1
					double randomNumber = random.nextDouble();

					// use the random number to choose a state (j is the
					// state)
					int j = 0;
					while ((randomNumber > cumProb[j]) && (j < numStates))
					{
						j++;
					}

					cellValue = j;
				}
			}
		}

		// *** RULE FOR IMMITATORS***
		// if the temp is 0, then the cell's follow majority rules
		else if (socialTemperatureLocalCopy != 0.0)
		{
			// get probability (percent) of each state, including the social
			// force and temperature. (We divide by the partition function
			// later.)
			double[] prob = new double[numStates];

			for (int i = 0; i < numStates; i++)
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

//			// ***** Contagion happens here!!!****
//			// Linked states
//			// Outer loop counts through the probabilities for each state
//			for (int i = 0; i < numStates / 2; i++)
//			{
//				for (int j = 0; j < numStates / 2; j++)
//				{
//					if (numberOfEachState[j] != 0)
//					{
//						prob[i] = prob[i] - (1 / numberOfEachState[j]);
//					}
//				}
//			}
//
//			// Everyone else
//			// Outer loop counts through the probabilities for each state
//			for (int i = numStates / 2; i < numStates; i++)
//			{
//				for (int j = 0; j < numStates / 2; j++)
//				{
//					if (numberOfEachState[j] != 0)
//					{
//						prob[i] = prob[i] + (1 / numberOfEachState[j]);
//					}
//				}
//			}

			// calculate the partition function
			double z = 0.0;
			for (int i = 0; i < numStates; i++)
			{
				z += prob[i];
			}

			// now divide each probability by the partition function (so
			// scaled
			// properly)
			for (int i = 0; i < numStates; i++)
			{
				prob[i] /= z;
			}

			// get cumulative probability of each state
			double[] cumProb = new double[numStates];
			cumProb[0] = prob[0];
			for (int i = 1; i < numStates; i++)
			{
				cumProb[i] = cumProb[i - 1] + prob[i];
			}

			// Now get a random number between 0 and 1
			double randomNumber = random.nextDouble();

			// use the random number to choose a state (j is the state)
			int j = 0;
			while ((randomNumber > cumProb[j]) && (j < numStates))
			{
				j++;
			}

			cellValue = j;
		}

		// now add a random noise that moves the cell value up or down only one
		// unit. In other words, with a certain probability, the cell increases
		// or decreases weight by one unit.
		if (random.nextDouble() < upDownNoiseThresholdLocalCopy)
		{
			// change the state randomly
			if (random.nextBoolean())
			{
				// only increase if not already at the maximum
				if (cellValue < numStates - 1)
				{
					cellValue += 1;
				}
			} else
			{
				// only decrease if not already at the minimum
				if (cellValue > 0)
				{
					cellValue -= 1;
				}
			}
		}

		// now add unbiased noise -- this changes the state randomly. The
		// probabilityOfSpontaneousWeightGain
		// works like a biased noise that moves toward greater weight. This is
		// just a general noise that changes weights to a random new value.
		if (random.nextDouble() < noiseThresholdLocalCopy)
		{
			// change the state randomly
			cellValue = random.nextInt(numStates);
		}

		// finally, check to see if the cell has moved up or down more than one
		// weight (BMI) unit. If it has, restrict the movement to only one unit.
		// But only do this if a flag has been set in the More Properties panel.
		if (restrictToMovementOfOneUnitLocalCopy)
		{
			if (cellValue - cell > 1)
			{
				// only move up one unit
				cellValue = cell + 1;
			} else if (cell - cellValue > 1)
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
		if (additionalPropertiesPanel == null)
		{
			// create the social forces (only if necessary)
			if (forceSliderLabel == null
					|| forceSliderLabel.length != numStates)
			{
				forceSliderLabel = new JLabel[numStates];
				socialForce = new double[numStates];
				for (int i = 0; i < socialForce.length; i++)
				{
					socialForce[i] = 0.0;
				}

				socialForceLocalCopy = new double[numStates];
				for (int i = 0; i < socialForceLocalCopy.length; i++)
				{
					socialForceLocalCopy[i] = 0.0;
				}
			}

			// create the opinion leader social forces (only if necessary)
			if (forceSliderLabelOL == null
					|| forceSliderLabelOL.length != NUM_ATTITUDES)
			{

				for (int i = 0; i < NUM_ATTITUDES; i++)
				{
					// force the additional properties panel to be recreated
					additionalPropertiesPanel = null;
					socialForceOL = new double[NUM_ATTITUDES][numStates];
					socialForceOLLocalCopy = new double[NUM_ATTITUDES][numStates];
					forceSliderLabelOL = new JLabel[NUM_ATTITUDES][numStates];

					for (int j = 0; j < numStates; j++)
					{
						socialForceOL[i][j] = 0.0;
						socialForceOLLocalCopy[i][j] = 0.0;
					}
				}
			}

			// create the opinion leader noise threshold (only if necessary)
			if (noiseSliderLabelOL == null
					|| noiseSliderLabelOL.length != numStates)
			{
				// force the additional properties panel to be recreated
				additionalPropertiesPanel = null;

				noiseSliderLabelOL = new JLabel[numStates];
				noiseThresholdOL = new double[numStates];
				for (int i = 0; i < noiseThresholdOL.length; i++)
				{
					noiseThresholdOL[i] = 0.0;
				}

				noiseThresholdOLLocalCopy = new double[numStates];
				for (int i = 0; i < noiseThresholdOLLocalCopy.length; i++)
				{
					noiseThresholdOLLocalCopy[i] = 0.0;
				}
			}

			// create the opinion leader temperatures
			if (temperatureSliderLabelOL == null
					|| temperatureSliderLabelOL.length != numStates)
			{
				// force the additional properties panel to be recreated
				additionalPropertiesPanel = null;

				temperatureSliderLabelOL = new JLabel[numStates];
				socialTemperatureOL = new double[numStates];
				for (int i = 0; i < socialTemperatureOL.length; i++)
				{
					socialTemperatureOL[i] = DEFAULT_TEMPERATURE;
				}

				socialTemperatureOLLocalCopy = new double[numStates];
				for (int i = 0; i < socialTemperatureOLLocalCopy.length; i++)
				{
					socialTemperatureOLLocalCopy[i] = DEFAULT_TEMPERATURE;
				}
			}

			additionalPropertiesPanel = createAdditionalPropertiesPanel();
		}

		return additionalPropertiesPanel;
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

		model = new SpinnerNumberModel(0.0, 0.0, 1000.0, 1.0);
		rankOpinionLeaders = new JSpinner(model);
		rankOpinionLeaders.setName("rankOpinionLeaders");
		rankOpinionLeaders.addChangeListener(new SpinnerListener());
		numOpinionLeaders = model.getNumber().intValue();

		// the 5 is a cheat that handles an unknown scroll bar width and border
		// inset
		int width = CAFrame.tabbedPaneDimension.width - 30;
		int height = 1210;

		Fonts fonts = new Fonts();

		String temperatureDescription = "Social temperature is a measure of the "
				+ "volatility or irrationality of a group.  Higher temperatures mean that each "
				+ "individual is uneasy and more likely to sell the asset at the "
				+ "slightest provocation.  A low temperature means that "
				+ "individuals are comfortable with their current asset and are more likely to "
				+ "keep it. Risk Adverse Traders are more comfortable with their asset when it is "
				+ "underpriced and uneasy with their asset when it is over priced. Risk Taking "
				+ "Traders are more comfortable with assets that are increasing in price and are"
				+ "uneasy holding assets that are decreasing in price. Risk Neutral Traders average the two."
				+ "This value will be used as a basis for the simulation to modify as required.";

		MultilineLabel temperatureDescriptionLabel = new MultilineLabel(
				temperatureDescription);
		temperatureDescriptionLabel.setFont(fonts
				.getMorePropertiesDescriptionFont());

		String forceDescription = "Social force is a measure of external influences "
				+ "such as advertising, government incentives, punishments, etc. that will "
				+ "modify behavior. In the market model, it mostly reflects what assets opinion leaders are "
				+ "attracted too and what assets they avoid. Risk Adverse Traders are influenced to hold assets that are under priced and"
				+ "avoid assets that are over priced. Risk Taking Traders are influenced to hold assets that are increasing in price and"
				+ "to avoid assets that are going down in price. Risk Neutral Traders average the other two. "
				+ "This value will be used as a basis for the simulation to modify as required";
		MultilineLabel forceDescriptionLabel = new MultilineLabel(
				forceDescription);
		forceDescriptionLabel.setFont(fonts.getMorePropertiesDescriptionFont());

		String noiseDescription = "Random and unknown factors may contribute to seemingly "
				+ "random changes in asset distribution.  This sets the level of randomness. "
				+ "Each individual will change to another state with the probability "
				+ "set below.  A value of 0.0 means there is no noise.  A value of 1.0 is "
				+ "all noise and creates a completely random simulation.";
		MultilineLabel noiseDescriptionLabel = new MultilineLabel(
				noiseDescription);
		noiseDescriptionLabel.setFont(fonts.getMorePropertiesDescriptionFont());

		// slider descriptions
		String opinionLeaderDescription = "Each individual has certain number of "
				+ "neighbors that is determined by the lattice. On a lattice that uses "
				+ "power-law connectivity to randomly assign neighbors to cells (small-world lattice), "
				+ "a few cells have a large number of neighbors relative to average cells. "
				+ "The cells with a large number of neighbors are opinion leaders. Opinion leaders "
				+ "are people who have a great influence over a soceity. They can be experts, "
				+ "leaders, celebrities, athletes, etc... In the market model, they are the very"
				+ "influential traders who buy a large portion of the assets for a "
				+ "bank, mutal fund, hedge fund, investmenst company, etc... "
				+ "This sets the minimum population percentile a cell must be in to qualify "
				+ "as an opinion leader";
		MultilineLabel opinionLeaderDescriptionLabel = new MultilineLabel(
				opinionLeaderDescription);
		opinionLeaderDescriptionLabel.setFont(fonts
				.getMorePropertiesDescriptionFont());

		String output = longDecimalFormatter.format(socialTemperature);
		temperatureSliderLabel = new JLabel("temperature = " + output);
		temperatureSliderLabel.setFont(fonts.getBoldFont());

		output = longDecimalFormatter.format(noiseThreshold);
		noiseSliderLabel = new JLabel("noise = " + output);
		noiseSliderLabel.setFont(fonts.getBoldFont());

		output = longDecimalFormatter.format(upDownNoiseThreshold);
		upDownNoiseSliderLabel = new JLabel("noise = " + output);
		upDownNoiseSliderLabel.setFont(fonts.getBoldFont());

		for (int state = 0; state < numStates; state++)
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
		// probSliderOL = createRandomOpinionSlider();

		// create the slider for social temperature
		temperatureSlider = createTemperatureSlider();

		// create the slider for noise
		noiseSlider = createNoiseSlider();

		// create the slider for up-down noise
		upDownNoiseSlider = createUpDownNoiseSlider();

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

		// create a scroll panel for all the noise components
		JPanel allNoisePanels = new JPanel(new GridBagLayout());
		row = 0;
		allNoisePanels.add(noisePanel, new GBC(1, row).setSpan(5, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1,
				3, 3, 3));

		JScrollPane noiseComponentsScroller = new JScrollPane(allNoisePanels);
		int scrollPaneWidth = width;
		int scrollPaneHeight = height / 6;
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

		row++;
		allComponentsPanel.add(new JLabel("Select # of Opinion Leaders:"),
				new GBC(1, row).setSpan(5, 1).setFill(GBC.BOTH).setWeight(1.0,
						1.0).setAnchor(GBC.WEST).setInsets(1));
		row++;
		allComponentsPanel.add(rankOpinionLeaders, new GBC(1, row)
				.setSpan(5, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(
						GBC.WEST).setInsets(1));

		JPanel[] allOpinionPanel = createOpinionPanel();

		for (int i = 0; i < NUM_ATTITUDES; i++)
		{

			// movement restriction
			row++;
			allComponentsPanel.add(allOpinionPanel[i], new GBC(1, row).setSpan(
					5, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(
					GBC.WEST).setInsets(1));
		}

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
		String[] folders =
		{ RuleFolderNames.PROBABILISTIC_FOLDER, RuleFolderNames.SOCIAL_FOLDER,
				RuleFolderNames.OBESITY_RESEARCH_FOLDER };

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
		if (event.getPropertyName().equals(CurrentProperties.COLORS_CHANGED))
		{
			if (this.getCompatibleCellStateView() != null
					&& forceSliderLabel != null
					&& additionalPropertiesPanel != null)
			{
				// colors have changed, so reset the colors
				for (int state = 0; state < NUM_ATTITUDES; state++)
				{
					// set the color of the label to be the same as the
					// color of the state it represents
					Color stateColor = this.getCompatibleCellStateView()
							.getDisplayColor(new IntegerCellState(state), null,
									new Coordinate(0, 0));
					forceSliderLabel[state].setForeground(stateColor);
				}
				for (int i = 0; i < NUM_ATTITUDES; i++)
				{// colors have changed, so reset the colors
					for (int state = 0; state < numStates; state++)
					{
						if (this.getCompatibleCellStateView() != null
								&& forceSliderLabelOL[i][state] != null
								&& additionalPropertiesPanel != null)
						{

							// set the color of the label to be the same as the
							// color of the state it represents
							Color stateColor = this
									.getCompatibleCellStateView()
									.getDisplayColor(
											new IntegerCellState(state), null,
											new Coordinate(0, 0));
							forceSliderLabelOL[i][state]
									.setForeground(stateColor);
						}
					}
				}

				// and update the display
				additionalPropertiesPanel.repaint();
			}
		} else if (event.getPropertyName().equals(CurrentProperties.SETUP))
		{
			additionalPropertiesPanel = null;
		}
	}

	/**
	 * Method to calculate the current pricing of the assets on the lattice.
	 * Each cell on the lattice trades in a single asset which is the cells
	 * current state. Pricing of each asset is calculated by taking the current
	 * number of cells on the lattice with a given state (currentQuantity) and
	 * subtracting the previous number of cells on the lattice with a given
	 * state (previousQuantity). This value is then multiplied by a scaler that
	 * is between 0 and 1. The value is then added (or subtracted) to the price
	 * at the previous generation.
	 */
	public void calculatePricing()
	{

		transactionQuantity = new int[numStates];

		for (int i = 0; i < currentValue.length; i++)
		{
			previousValue[i] = currentValue[i];
		}

		for (int i = 0; i < currentQuantity.length; i++)
		{
			// System.out.println("Previous " + previousValue[i]);

			// finds the change (derivative) in the number of each asset
			transactionQuantity[i] = currentQuantity[i] - previousQuantity[i];
			// transactionQuantity[i] = (int) (fundamentalValue[i] -
			// currentValue[i]);

			Random r = new Random();

			// multiply by a scaler that reflects how much the
			// price reacts to a change in the derivative of the asset
			currentValue[i] = currentValue[i] + (r.nextDouble() * .05)
					* ((transactionQuantity[i]));

			// System.out.println("Current " + currentValue[i]);

			if (currentValue[i] < 0)
			{
				currentValue[i] = 0;
			}
		}
	}

	public static double[] getPrice()
	{
		return currentValue;
	}

	public void calcLinkedSocialForce()
	{
		double averageLinkedVolatility = 0;

		for (int i = 0; i < numLinkedStates; i++)
		{
			averageLinkedVolatility += .9 * Math.log(currentValue[i]
					/ previousValue[i]);
		}

		averageLinkedVolatility = averageLinkedVolatility / numLinkedStates;

		for (int i = 0; i < numLinkedStates; i++)
		{
			for (int j = 0; j < numStates; j++)
			{
				socialForceOL[i][j] = socialForceOL[i][j]
						+ averageLinkedVolatility;
			}
		}
	}

	/**
	 * Method that sorts the number of neighbors into an integer array
	 * (opinionLeaders). The largest opinion leaders can be found by indexing
	 * backwards through the array. A second String array is indexed with
	 * opinionLeaders. It contains a value of A (risk Adverse), N (risk
	 * Neutral), or T (risk Taking) that is associated with each opinion leader
	 * on the lattice.
	 */
	public void initializeOpinionLeaders()
	{

		// sort the number of neighbors
		Collections.sort(numNeighbors);

		// temp object used to parse to an array
		Object[] temporaryObject = numNeighbors.toArray();

		// initialize the opinion leaders array
		opinionLeaders = new int[temporaryObject.length];

		for (int i = 0; i < opinionLeaders.length; i++)
		{
			// transfer the number of neighbors into a sorted array
			opinionLeaders[i] = (Integer) temporaryObject[i];
		}
	}

	/**
	 * Method to ensure that all sliders, labels, arrays, working arrays, etc...
	 * are initialized.
	 */
	public void initializeVariables()
	{
		if (socialForce == null || socialForceLocalCopy == null
				|| MajorityRulesWithContagion.numStates != numStates)
		{
			// so we can use this elsewhere
			MajorityRulesWithContagion.numStates = numStates;

			// recreate the social forces
			forceSliderLabel = new JLabel[numStates];
			socialForce = new double[numStates];
			for (int i = 0; i < socialForce.length; i++)
			{
				socialForce[i] = 0.0;
			}
			socialForceLocalCopy = new double[numStates];
			for (int i = 0; i < socialForceLocalCopy.length; i++)
			{
				socialForceLocalCopy[i] = 0.0;
			}
		}

		if (socialForceOL == null || socialForceOLLocalCopy == null
				|| MajorityRulesWithContagion.numStates != numStates)
		{
			for (int i = 0; i < NUM_ATTITUDES; i++)
			{
				// so we can use this elsewhere
				MajorityRulesWithContagion.numStates = numStates;

				// force the additional properties panel to be recreated
				additionalPropertiesPanel = null;
				socialForceOL = new double[NUM_ATTITUDES][numStates];
				socialForceOLLocalCopy = new double[NUM_ATTITUDES][numStates];
				forceSliderLabelOL = new JLabel[NUM_ATTITUDES][numStates];

				for (int j = 0; j < numStates; j++)
				{
					socialForceOL[i][j] = 0.0;
					socialForceOLLocalCopy[i][j] = 0.0;
				}
			}
		}

		if (noiseThresholdOL == null || noiseThresholdOLLocalCopy == null
				|| MajorityRulesWithContagion.numStates != numStates)
		{
			// so we can use this elsewhere
			MajorityRulesWithContagion.numStates = numStates;

			noiseSliderLabelOL = new JLabel[NUM_ATTITUDES];
			noiseThresholdOL = new double[NUM_ATTITUDES];
			noiseThresholdOLLocalCopy = new double[NUM_ATTITUDES];

			for (int i = 0; i < NUM_ATTITUDES; i++)
			{
				noiseThresholdOL[i] = 0.0;
				noiseThresholdOLLocalCopy[i] = 0.0;
			}
		}

		if (socialTemperatureOL == null || socialTemperatureOLLocalCopy == null
				|| MajorityRulesWithContagion.numStates != numStates)
		{
			// so we can use this elsewhere
			MajorityRulesWithContagion.numStates = numStates;

			temperatureSliderLabelOL = new JLabel[NUM_ATTITUDES];
			socialTemperatureOL = new double[NUM_ATTITUDES];
			socialTemperatureOLLocalCopy = new double[NUM_ATTITUDES];

			for (int i = 0; i < NUM_ATTITUDES; i++)
			{
				socialTemperatureOL[i] = DEFAULT_TEMPERATURE;
				socialTemperatureOLLocalCopy[i] = DEFAULT_TEMPERATURE;
			}
		}
	}

	/**
	 * Method that finds the current number of each assets (cell with a given
	 * state) on the lattice. Uses a lattice iterator to parse through the
	 * lattice, finding the state of each cell and the number of neighbors that
	 * each cell has. The latter only occurs once per simulation. The former
	 * occurs once every generation.
	 * 
	 */
	public void updateQuantities(int generation)
	{
		// initialize array
		currentQuantity = new int[numStates];

		// instantiate a lattice and get the lattice
		Lattice lattice = CAController.getCAFrame().getLattice();

		// get each cell on the lattice
		Cell c = null;

		// get an iterator over the lattice
		Iterator cellIterator = lattice.iterator();
		int totalNumberOfCells = 0;

		while (cellIterator.hasNext())
		{
			// add one more to the total number of cells
			totalNumberOfCells++;

			// get the cell
			c = (Cell) cellIterator.next();

			// get its state.
			IntegerCellState state = (IntegerCellState) c.getState(generation);

			// increment the number of states
			currentQuantity[state.toInt()]++;

			// find the neighborhood size for each cell
			if (!filled)
			{
				numNeighbors.add(CAController.getCAFrame().getLattice()
						.getNeighbors(c).length);
			}
		}
	}

	/**
	 * Creates a JPanel containing all of the labels, sliders, comboBoxes,
	 * etc... required for the user to interact with the model.
	 * 
	 * @return JPanel array containing opinion leaders panels
	 */
	public JPanel[] createOpinionPanel()
	{
		Fonts fonts = new Fonts();

		// create the slider for social temperature for opinion leaders
		temperatureSliderOL = createTemperatureSliderOL();
		// create the slider for noise for opinion leaders
		noiseSliderOL = createNoiseSliderOL();
		// create the sliders for social forces for opinion leaders
		forceSliderOL = createForceSlidersOL();

		// ****OPINION LEADERS STARTS HERE****
		for (int state = 0; state < numStates; state++)
		{
			String output = longDecimalFormatter.format(socialTemperature);
			temperatureSliderLabelOL[state] = new JLabel("temperature = "
					+ output);
			temperatureSliderLabelOL[state].setFont(fonts.getBoldFont());

			output = longDecimalFormatter.format(noiseThreshold);
			noiseSliderLabelOL[state] = new JLabel("noise = " + output);
			noiseSliderLabelOL[state].setFont(fonts.getBoldFont());
		}

		for (int state = 0; state < NUM_ATTITUDES; state++)
		{
			for (int i = 0; i < numStates; i++)
			{
				String output = longDecimalFormatter.format(socialTemperature);
				output = longDecimalFormatter.format(socialForce[state]);
				forceSliderLabelOL[state][i] = new JLabel("force = " + output);
				forceSliderLabelOL[state][i].setFont(fonts.getBoldFont());

				// set the color of the label to be the same as the color of the
				// state it represents
				Color stateColor = this.getCompatibleCellStateView()
						.getDisplayColor(new IntegerCellState(i), null,
								new Coordinate(0, 0));
				forceSliderLabelOL[state][i].setForeground(stateColor);
			}
		}

		// ***all of the opinion leader social forces get initialized here***

		// get the size of the CA frame
		int width = CAFrame.tabbedPaneDimension.width - 30;
		int height = 1210;
		int row = 0;

		// initialize variables for opinion leader forces
		Border[] outerEmptyBorderForce = new Border[numStates];
		Border[] titledBorderForce = new Border[numStates];

		// Color for the opinion leaders social forces title
		Color titleColor = Color.BLUE;

		// Font for the opinion leaders social forces title
		Font titleFont = new Fonts().getItalicSmallerFont();

		// JPanels for the opinion leaders social forces
		JPanel[] opinionPanel = new JPanel[numStates];
		JPanel[] allOpinionPanels = new JPanel[numStates];

		// ScrollPane to hold all the opinion leader force sliders
		JScrollPane[] opinionComponentsScroller = new JScrollPane[numStates];
		JScrollPane[] opinionScrollPane = createForceSliderPanelOL(width - 30,
				height);

		// initialize boarders, panels and scrollers for opinion leader force
		for (int i = 0; i < numStates; i++)
		{
			outerEmptyBorderForce[i] = BorderFactory.createEmptyBorder(0, 3, 2,
					3);
			titledBorderForce[i] = BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(), "Social Force",
					TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION,
					titleFont, titleColor);
			opinionPanel[i] = new JPanel(new GridBagLayout());
			allOpinionPanels[i] = new JPanel(new GridBagLayout());
			opinionComponentsScroller[i] = new JScrollPane(allOpinionPanels[i]);
		}

		// create panel and scroller for the opinion leaders force
		for (int i = 0; i < numStates; i++)
		{
			opinionPanel[i].setBorder(BorderFactory.createCompoundBorder(
					outerEmptyBorderForce[i], titledBorderForce[i]));
			row = 0;

			opinionPanel[i].add(opinionScrollPane[i], new GBC(1, row).setSpan(
					5, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(
					GBC.WEST).setInsets(10, 1, 10, 1));

			// create a scroll panel for all the opinion components
			row = 0;
			allOpinionPanels[i].add(opinionPanel[i], new GBC(1, row).setSpan(5,
					1).setFill(GBC.BOTH).setWeight(1.0, 1.0)
					.setAnchor(GBC.WEST).setInsets(1, 3, 3, 3));

			int opinionScrollPaneWidth = width;
			int opinionScrollPaneHeight = height / 4;
			opinionComponentsScroller[i].setPreferredSize(new Dimension(
					opinionScrollPaneWidth, opinionScrollPaneHeight));
			opinionComponentsScroller[i].setMinimumSize(new Dimension(
					opinionScrollPaneWidth, opinionScrollPaneHeight));
			opinionComponentsScroller[i].setMaximumSize(new Dimension(
					opinionScrollPaneWidth, opinionScrollPaneHeight));
			opinionComponentsScroller[i]
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			opinionComponentsScroller[i]
					.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		}

		// ***all of the opinion leader temperature variables***

		// Borders for the opinion leader temperature panels
		Border[] outerEmptyBorderTemp = new Border[NUM_ATTITUDES];
		Border[] titledBorderTemp = new Border[NUM_ATTITUDES];

		// Color for opinion leader temperature title
		Color titleColorOL = Color.BLUE;

		// Font for opinion leader temperature title
		Font titleFontOL = new Fonts().getItalicSmallerFont();

		// Panels for opinion leader temperature
		JPanel[] temperaturePanel = new JPanel[NUM_ATTITUDES];
		JPanel[] allTempPanels = new JPanel[NUM_ATTITUDES];

		// ScrollPane for opinion leader temperature
		JScrollPane[] tempComponentsScroller = new JScrollPane[NUM_ATTITUDES];

		// initialize boarders, panels and scrollers for opinion leader
		// temperature
		for (int i = 0; i < NUM_ATTITUDES; i++)
		{
			outerEmptyBorderTemp[i] = BorderFactory.createEmptyBorder(0, 3, 2,
					3);
			titledBorderTemp[i] = BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(), "Temperature",
					TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION,
					titleFontOL, titleColorOL);
			temperaturePanel[i] = new JPanel(new GridBagLayout());
			allTempPanels[i] = new JPanel(new GridBagLayout());
			tempComponentsScroller[i] = new JScrollPane(allTempPanels[i]);
		}

		// create panel for the opinion leader temperature
		for (int i = 0; i < NUM_ATTITUDES; i++)
		{
			temperaturePanel[i].setBorder(BorderFactory.createCompoundBorder(
					outerEmptyBorderTemp[i], titledBorderTemp[i]));
			row = 0;

			temperaturePanel[i].add(temperatureSliderOL[i], new GBC(1, row)
					.setSpan(5, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0)
					.setAnchor(GBC.WEST).setInsets(1));
			row++;
			temperaturePanel[i].add(temperatureSliderLabelOL[i],
					new GBC(1, row).setSpan(5, 1).setFill(GBC.VERTICAL)
							.setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
							.setInsets(1));

			// create a scroll panel for all the opinion components
			row = 0;
			allTempPanels[i].add(temperaturePanel[i], new GBC(1, row).setSpan(
					5, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(
					GBC.WEST).setInsets(1, 3, 3, 3));

			int tempScrollPaneWidth = width;
			int tempScrollPaneHeight = height / 10;
			tempComponentsScroller[i].setPreferredSize(new Dimension(
					tempScrollPaneWidth, tempScrollPaneHeight));
			tempComponentsScroller[i].setMinimumSize(new Dimension(
					tempScrollPaneWidth, tempScrollPaneHeight));
			tempComponentsScroller[i].setMaximumSize(new Dimension(
					tempScrollPaneWidth, tempScrollPaneHeight));
			tempComponentsScroller[i]
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		}

		// ***all the variables for the opinion leaders noise panels***

		// Borders for opinion leaders noise
		Border[] outerEmptyBorderNoise = new Border[NUM_ATTITUDES];
		Border[] titledBorderNoise = new Border[NUM_ATTITUDES];
		Border[] outerEmptyBorderAllNoisePanel = new Border[NUM_ATTITUDES];
		Border[] titledBorderAllNoisePanel = new Border[NUM_ATTITUDES];

		// Panels for opinion leaders noise
		JPanel[] noisePanel = new JPanel[NUM_ATTITUDES];
		JPanel[] upDownNoisePanel = new JPanel[NUM_ATTITUDES];
		JPanel[] allNoisePanels = new JPanel[NUM_ATTITUDES];
		JPanel[] allNoisePanel = new JPanel[NUM_ATTITUDES];

		// ScrollPane for opinion leader noise
		JScrollPane[] noiseComponentsScroller = new JScrollPane[NUM_ATTITUDES];

		// initialize variables for opinion leaders noise
		for (int i = 0; i < NUM_ATTITUDES; i++)
		{
			allNoisePanels[i] = new JPanel(new GridBagLayout());
			noiseComponentsScroller[i] = new JScrollPane(allNoisePanels[i]);
			noisePanel[i] = new JPanel(new GridBagLayout());
			upDownNoisePanel[i] = new JPanel(new GridBagLayout());

			titledBorderNoise[i] = BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(), "Unbiased Noise",
					TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION,
					titleFont, titleColor);

			titledBorderAllNoisePanel[i] = BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(), "Noise",
					TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION,
					titleFont, titleColor);

			allNoisePanel[i] = new JPanel(new GridBagLayout());
		}

		// create panel for the noise
		for (int i = 0; i < NUM_ATTITUDES; i++)
		{
			noisePanel[i].setBorder(BorderFactory.createCompoundBorder(
					outerEmptyBorderNoise[i], titledBorderNoise[i]));
			row = 0;

			noisePanel[i].add(noiseSliderOL[i], new GBC(1, row).setSpan(5, 1)
					.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
					.setInsets(1));
			row++;
			noisePanel[i].add(noiseSliderLabelOL[i], new GBC(1, row).setSpan(5,
					1).setFill(GBC.VERTICAL).setWeight(1.0, 1.0).setAnchor(
					GBC.CENTER).setInsets(1));

			// create a scroll panel for all the noise components
			row = 0;
			allNoisePanels[i].add(noisePanel[i], new GBC(1, row).setSpan(5, 1)
					.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
					.setInsets(1, 3, 3, 3));

			int scrollPaneWidth = width;
			int scrollPaneHeight = height / 12;
			noiseComponentsScroller[i].setPreferredSize(new Dimension(
					scrollPaneWidth, scrollPaneHeight));
			noiseComponentsScroller[i].setMinimumSize(new Dimension(
					scrollPaneWidth, scrollPaneHeight));
			noiseComponentsScroller[i].setMaximumSize(new Dimension(
					scrollPaneWidth, scrollPaneHeight));
			noiseComponentsScroller[i]
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

			allNoisePanel[i].setBorder(BorderFactory.createCompoundBorder(
					outerEmptyBorderAllNoisePanel[i],
					titledBorderAllNoisePanel[i]));
			row = 0;

			allNoisePanel[i].add(noiseComponentsScroller[i], new GBC(1, row)
					.setSpan(5, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0)
					.setAnchor(GBC.WEST).setInsets(1, 3, 3, 3));
		}

		// ////////////////////////////////////////////////////////////////////
		// *** Create the main opinion leader panel here***

		// ***all the variables for the main opinion leader panels get
		// initialized here***

		// Borders for the main panel
		Border[] outerEmptyBorderAllOpinionPanel = new Border[NUM_ATTITUDES];
		Border[] titledBorderAllOpinionPanel = new Border[NUM_ATTITUDES];

		// Panel for the main opinion leader panel
		JPanel[] allOpinionPanel = new JPanel[NUM_ATTITUDES];

		// initialize variables
		for (int i = 0; i < NUM_ATTITUDES; i++)
		{
			outerEmptyBorderAllOpinionPanel[i] = BorderFactory
					.createEmptyBorder(0, 3, 2, 3);
			titledBorderAllOpinionPanel[i] = BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(),
					"Opinion Leaders For State #" + Integer.toString(i),
					TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION,
					titleFont, titleColor);
			allOpinionPanel[i] = new JPanel(new GridBagLayout());
		}

		// add components to panel
		for (int i = 0; i < NUM_ATTITUDES; i++)
		{
			allOpinionPanel[i].setBorder(BorderFactory.createCompoundBorder(
					outerEmptyBorderAllOpinionPanel[i],
					titledBorderAllOpinionPanel[i]));
			row = 0;
			allOpinionPanel[i]
					.add(opinionComponentsScroller[i], new GBC(1, row).setSpan(
							5, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0)
							.setAnchor(GBC.WEST).setInsets(1, 3, 3, 3));
			row++;
			allOpinionPanel[i].add(tempComponentsScroller[i], new GBC(1, row)
					.setSpan(5, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0)
					.setAnchor(GBC.WEST).setInsets(1, 3, 3, 3));
			row++;
			allOpinionPanel[i].add(noiseComponentsScroller[i], new GBC(1, row)
					.setSpan(5, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0)
					.setAnchor(GBC.WEST).setInsets(1, 3, 3, 3));
		}

		return allOpinionPanel;
	}

	private class SpinnerListener implements ChangeListener
	{
		/**
		 * Listens for changes to the probability slider.
		 * 
		 * @param e
		 */
		public void stateChanged(ChangeEvent e)
		{
			if (e.getSource().equals(rankOpinionLeaders.getName()))
				;
			{
				numOpinionLeaders = (((SpinnerNumberModel) rankOpinionLeaders
						.getModel()).getNumber()).intValue();
			}
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
			if (e.getSource().equals(temperatureSlider)
					&& temperatureSlider != null
					&& temperatureSliderLabel != null)
			{
				// get the temperature value in *arbitrary units provided by the
				// slider* that's why I divide by MAX_SLIDER_VALUE
				double updatedSliderValue = MIN_TEMPERATURE
						+ ((temperatureSlider.getValue() / (double) MAX_SLIDER_VALUE) * (MAX_TEMPERATURE - MIN_TEMPERATURE));

				// make sure the value changed
				if (socialTemperature != updatedSliderValue)
				{
					socialTemperature = updatedSliderValue;

					// change the display
					String output = longDecimalFormatter
							.format(socialTemperature);
					temperatureSliderLabel.setText("temperature = " + output);
				}
			} else if (e.getSource().equals(noiseSlider) && noiseSlider != null
					&& noiseSliderLabel != null)
			{
				// get the noise value in *arbitrary units provided by the
				// slider* that's why I divide by MAX_SLIDER_VALUE
				double updatedSliderValue = MIN_NOISE
						+ ((noiseSlider.getValue() / (double) MAX_SLIDER_VALUE) * (MAX_NOISE - MIN_NOISE));

				// make sure the value changed
				if (noiseThreshold != updatedSliderValue)
				{
					noiseThreshold = updatedSliderValue;

					// change the display
					String output = longDecimalFormatter.format(noiseThreshold);
					noiseSliderLabel.setText("noise = " + output);
				}
			} else if (e.getSource().equals(upDownNoiseSlider)
					&& upDownNoiseSlider != null
					&& upDownNoiseSliderLabel != null)
			{
				// get the noise value in *arbitrary units provided by the
				// slider* that's why I divide by MAX_SLIDER_VALUE
				double updatedSliderValue = MIN_NOISE
						+ ((upDownNoiseSlider.getValue() / (double) MAX_SLIDER_VALUE) * (MAX_NOISE - MIN_NOISE));

				// make sure the value changed
				if (upDownNoiseThreshold != updatedSliderValue)
				{
					upDownNoiseThreshold = updatedSliderValue;

					// change the display
					String output = longDecimalFormatter
							.format(upDownNoiseThreshold);
					upDownNoiseSliderLabel
							.setText("noise (single unit up or down) = "
									+ output);
				}
			} else
			{
				for (int state = 0; state < numStates; state++)
				{

					if (e.getSource().equals(forceSlider[state])
							&& forceSlider[state] != null
							&& forceSliderLabel[state] != null)
					{
						// get the force value in *arbitrary units
						// provided by the slider* that's why I divide by
						// MAX_SLIDER_VALUE
						double updatedSliderValue = MIN_FORCE
								+ ((forceSlider[state].getValue() / (double) MAX_SLIDER_VALUE) * (MAX_FORCE - MIN_FORCE));

						// make sure the value changed
						if (socialForce[state] != updatedSliderValue)
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

	private class SliderListenerOL implements ChangeListener
	{
		/**
		 * Listens for changes to the probability slider.
		 * 
		 * @param e
		 */
		public void stateChanged(ChangeEvent e)
		{
			for (int state = 0; state < NUM_ATTITUDES; state++)
			{
				if (e.getSource().equals(temperatureSliderOL[state])
						&& temperatureSliderOL[state] != null
						&& temperatureSliderLabelOL[state] != null)
				{
					// System.out.println(e.getSource());
					// get the temperature value in *arbitrary units
					// provided by
					// the
					// slider* that's why I divide by MAX_SLIDER_VALUE
					double updatedSliderValue = MIN_TEMPERATURE
							+ ((temperatureSliderOL[state].getValue() / (double) MAX_SLIDER_VALUE) * (MAX_TEMPERATURE - MIN_TEMPERATURE));

					// make sure the value changed
					if (socialTemperatureOL[state] != updatedSliderValue)
					{
						socialTemperatureOL[state] = updatedSliderValue;

						// change the display
						String output = longDecimalFormatter
								.format(socialTemperatureOL[state]);
						temperatureSliderLabelOL[state]
								.setText("temperature = " + output);
					}
				}

				if (e.getSource().equals(noiseSliderOL[state])
						&& noiseSliderOL[state] != null
						&& noiseSliderLabelOL[state] != null)
				{
					// get the noise value in *arbitrary units provided by the
					// slider* that's why I divide by MAX_SLIDER_VALUE
					double updatedSliderValue = MIN_NOISE
							+ ((noiseSliderOL[state].getValue() / (double) MAX_SLIDER_VALUE) * (MAX_NOISE - MIN_NOISE));

					// make sure the value changed
					if (noiseThresholdOL[state] != updatedSliderValue)
					{
						noiseThresholdOL[state] = updatedSliderValue;

						// change the display
						String output = longDecimalFormatter
								.format(noiseThresholdOL[state]);
						noiseSliderLabelOL[state].setText("noise = " + output);
					}
				}
			}

			for (int i = 0; i < NUM_ATTITUDES; i++)
			{
				for (int j = 0; j < numStates; j++)
				{
					if (e.getSource().equals(forceSliderOL[i][j])
							&& forceSliderOL[i][j] != null
							&& forceSliderLabelOL[i][j] != null)
					{
						// get the force value in *arbitrary units
						// provided by the slider* that's why I divide
						// by
						// MAX_SLIDER_VALUE
						double updatedSliderValue = MIN_FORCE
								+ ((forceSliderOL[i][j].getValue() / (double) MAX_SLIDER_VALUE) * (MAX_FORCE - MIN_FORCE));

						// make sure the value changed
						if (socialForceOL[i][j] != updatedSliderValue)
						{
							socialForceOL[i][j] = updatedSliderValue;

							// change the display
							String output = longDecimalFormatter
									.format(socialForceOL[i][j]);
							forceSliderLabelOL[i][j].setText("force = "
									+ output);
						}
					}
				}
			}
		}

	}
}
