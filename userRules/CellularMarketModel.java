package userRules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import userRules.cellularMarketModel.model.MarketValue;
import userRules.cellularMarketModel.model.FundamentalValue;
import userRules.cellularMarketModel.model.PreviousTransactionHolding;
import userRules.cellularMarketModel.model.StoichasticCommodity;
import userRules.cellularMarketModel.model.StoichasticTransaction;
import userRules.cellularMarketModel.model.TransactionQuantity;

import cellularAutomata.CAController;
import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.rules.templates.IntegerRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;

public class CellularMarketModel extends IntegerRuleTemplate
{

	// the additional properties panel
	private static JPanel additionalPropertiesPanel = null;

	// a display name for this class
	private static final String RULE_NAME = "Cellular Market Model";

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

	// The default temperature of the group of all individuals. A
	// temperature of 1.0 corresponds to "majority probably rules" (when the
	// forces are also 0.0). Values can be 0 to infinity. Values of 0 to 1 are
	// most interesting.
	private static final double DEFAULT_TEMPERATURE = 1.0;

	// sliders that set the social forces
	private static JSlider[] forceSlider = null;

	// labels for the social forces
	private static JLabel[] forceSliderLabel = null;

	// The current generation being processed by the rule. Since the rule is
	// called once for every cell on the lattice per generation, we have to keep
	// track of when the rule is finished with the cells so the transaction
	// quantities are updated at the correct time (only once per generation).
	private static int currentGeneration = -1;

	// Array that current holdings of each state/commodity for the current
	// generation.
	private static int currentHoldings[];

	// Array that stores the current (for this generation) market value of the
	// state. The intrinsic value may be significantly different from the market
	// value or price of the investment. The market price is the price you can
	// buy and sell the asset. Buyers and sellers have many different ways of
	// measuring value and various reasons for buying and selling an asset. The
	// result is an asset may sell at a price significantly below or above its
	// perceived intrinsic or fundamental value.
	private static double currentMarketValue[];

	// Indicates if the data structure that contains the number of neighbors for
	// each cell has been initialized and filled. The value will be false for
	// the first generation only. The value will then remain true for the rest
	// of the simulation.
	private static boolean filled = false;

	// tool tip for the social force
	private static final String FORCE_TIP = "The social force "
			+ "that pushes individuals towards this BMI.  See rule description.";

	// Array that stores the fundamental value of each state/commodity
	// Intrinsic or fundamental value is the perceived value of an investment’s
	// future cash flows, expected growth, and risk. The goal of the value
	// investor is to purchase assets at prices lower than the intrinsic or
	// fundamental value.
	private static double fundamentalValue[];

	// A pattern used to display decimals.
	private static final String LONG_DECIMAL_PATTERN = "0.000";

	// formats decimals for display
	private static DecimalFormat longDecimalFormatter = new DecimalFormat(
			LONG_DECIMAL_PATTERN);

	// the max force. Values can be -infinity to infinity. Values of -1 to 1
	// are most interesting.
	private static final double MAX_FORCE = 2.0;

	// the max noise. Values can be 0 to 1.
	private static final double MAX_NOISE = 1.0;

	// max value of the sliders
	private static final int MAX_SLIDER_VALUE = 1000;

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

	// slider that sets the noise level
	private static JSlider noiseSlider = null;

	// label that displays the noise
	private static JLabel noiseSliderLabel = null;

	// Application state probability of random opinion changes (noise in the
	// system).
	// This value is set by the user else where and updated to the local copy
	// once
	// at the beginning of each generation so it can be used by the rule.
	private static double noiseThreshold = 0.0;

	// A local copy of the random weight changes (noise in the system),
	// updated once per generation at the beginning of each new generation.
	private static double noiseThresholdLocalCopy = 0.0;

	// tool tip for the noise
	private static final String NOISE_TIP = "Probability that individual will "
			+ "randomly change weight.  The amout of noise in the system.";

	// Array to store the number of neighbors for each cell.
	private static ArrayList<Integer> numNeighbors;

	private static int numOpinionLeaders = 0;

	// the number of states -- reset by the integerRule method
	private static int numStates = 2;

	// an array that stores the all the cells, and sorts them by neighborhood
	// size, the last one being the cell with the greatest number of neighbors
	private static int[] opinionLeaders = null;

	// Array that stores the quantity of each state/commodity from the previous
	// generation.
	// It can be thought of as the total number of holdings in each commodity in
	// the previous generation. The previousQuantity value is used to determine
	// the transactionQuanity or holdings derivative for the current generation.
	// The rule: transactionQuantity[i] = currentHoldings[i] -
	// previousHoldings[i];
	private static int previousHoldings[];

	// Array that stores the previous (for the last generation) market value of
	// the
	// state. The intrinsic value may be significantly different from the market
	// value or price of the investment. The market price is the price you can
	// buy and sell the asset. Buyers and sellers have many different ways of
	// measuring value and various reasons for buying and selling an asset. The
	// result is an asset may sell at a price significantly below or above its
	// perceived intrinsic or fundamental value.
	private static double previousMarketValue[];

	// A pattern used to display decimals.
	private static final String SHORT_DECIMAL_PATTERN = "0.0";

	// formats decimals for display
	private DecimalFormat shortDecimalFormatter = new DecimalFormat(
			SHORT_DECIMAL_PATTERN);

	// Application state of the force applied toward each state.
	// The value is defined elsewhere by the user and updated to
	// the local copy once at the beginning of each new generation.
	private static double[] socialForce = null;

	// A local copy of the social force, updated once per generation
	// from the application state at the beginning of each generation.
	private static double[] socialForceLocalCopy = null;

	// The application state of the social temperature of the group of all
	// individuals.
	// This value is set elsewhere by the user and is updated to the local copy
	// once at the beginning of each generation.
	private static double socialTemperature = DEFAULT_TEMPERATURE;

	// A local copy of the social temperature, updated once per generation from
	// the
	// social temperature application state.
	private static double socialTemperatureLocalCopy = DEFAULT_TEMPERATURE;

	// Indicates if the number of cell neighbor hood connections
	// for each cell has been sorted. This value will be false
	// for the first generation only, and will remain true for the
	// remainder of the simulation.
	private static boolean sorted = false;

	// slider that sets the temperature
	private static JSlider temperatureSlider = null;

	// label that displays the temperature
	private static JLabel temperatureSliderLabel = null;

	// tool tip for the social temperature
	private static final String TEMPERATURE_TIP = "The social temperature "
			+ "(volatility/irrationality).  Low temperatures are more stable.  See rule description.";

	// a tooltip description for this class
	private static String TOOLTIP = "<html> <body><b>" + RULE_NAME
			+ ".</b> A model of a commodities market. </body></html>";

	// Array that stores the change in quantity of each state/commodity. It can
	// be thought of as a derivative of total holdings in each commodity per
	// generation.
	// The rule: transactionQuantity[i] = currentQuantity[i] -
	// previousQuantity[i];
	// The transactionQuantity is calculated once at the
	// beginning of each generation to determine the derivative (change) in the
	// number of commodities being held by cells/investors in the model.
	private static int transactionQuantity[];

	private StoichasticCommodity fv = new FundamentalValue();
	private StoichasticCommodity mv = new MarketValue();
	private StoichasticTransaction tq = new TransactionQuantity();
	private StoichasticTransaction ph = new PreviousTransactionHolding();

	/**
	 * Create the commodities market rule using the given cellular automaton
	 * properties.
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
	public CellularMarketModel(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if (!minimalOrLazyInitialization)
		{
			// The array list is initialized to a size of 0.
			numNeighbors = new ArrayList<Integer>(0);

			// The number of states is set by the user once per instance.
			numStates = CurrentProperties.getInstance().getNumStates();

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

			// Create an array for the commodities fundamental values
			// that is the size of the number of states/commodities in the
			// model.
			fundamentalValue = new double[numStates];

			// Create an array for the commodities fundamental values
			// that is the size of the number of states/commodities in the
			// model.
			currentMarketValue = new double[numStates];

			// Create an array for the transactionQuantity that is the size
			// of the number of states/commodities in the model.
			// NOTE: This can be a negative value!
			transactionQuantity = new int[numStates];

			// Create an array to store the previous generations commodity
			// holdings.
			previousHoldings = new int[numStates];

			// Create an array for the commodities fundamental values for
			// the previous generation. The array should be the size of
			// the number of states/commodities in the model.
			previousMarketValue = new double[numStates];

			StoichasticCommodity fv = new FundamentalValue();
			StoichasticCommodity mv = new MarketValue();
			StoichasticTransaction tq = new TransactionQuantity();
			StoichasticTransaction ph = new PreviousTransactionHolding();

			// Initialize the state
			for (int i = 0; i < numStates; i++)
			{
				fundamentalValue[i] = fv.nextValue();
				currentMarketValue[i] = mv.nextValue();
				transactionQuantity[i] = tq.nextValue();
				previousHoldings[i] = ph.nextValue();
				previousMarketValue[i] = mv.nextValue();
			}
		}
	}

	/**
	 * Basic trading logic. Fundamentalist basic traders are informed of the
	 * nature of the asset being traded and are in agreement with regard to the
	 * fundamental value. They believe that the price of the stock may
	 * temporarily deviate from, but will eventually return to the fundamental
	 * value. They therefore buy (sell) the asset whenever its price is lower (higher)
	 * than its fundamental value percieved by them.
	 * 
	 * 
	 * @param cell
	 * @return
	 */
	public int levelOneModel(int cell)
	{
		double[] value = new double[fundamentalValue.length];

		// Only sell if the held commodity is over priced.
		if (fundamentalValue[cell] < currentMarketValue[cell])
		{
			// Find the difference between the fundamental value of the
			// commodity and the current market value of the commodity.
			// If the value is negative, the commodity is over priced.
			// If the value is positive, the commodity is under priced.
			for (int i = 0; i < value.length; i++)
			{
				value[i] = fundamentalValue[i] - currentMarketValue[i];
			}

			// Find the most under valued commodity.
			double min = getMaxValue(value);

			// Put a value on the most under valued commodity
			double sellValue = Math.abs(min);
			
			// Put a value on the held commodity
			double holdValue = Math.abs(fundamentalValue[cell]
					- currentMarketValue[cell]);

			// Create a partition function so the decision
			// to sell or hold the commodity is stochastic.
			double partition = sellValue + holdValue;

			// Determine the probability of selling the commodity
			double sellProb = sellValue / partition;

			Random r = new Random();

			// Use a random process to determine if the commodity
			// is sold or held.
			if ((1 - sellProb) >= r.nextDouble())
			{
				for (int i = 0; i < value.length; i++)
				{
					if (value[i] == min)
					{
						return i;
					}
				}
			}
		} else
		{
			Random r = new Random();

			if (r.nextDouble() >= .9)
			{
				return r.nextInt(numStates);
			}
		}

		return cell;
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

		// Save the current market market values as the previous market values
		// before we make changes the currentMarketValue.
		for (int i = 0; i < currentMarketValue.length; i++)
		{
			previousMarketValue[i] = currentMarketValue[i];
		}

		// Calculate the new current market values.
		for (int i = 0; i < currentHoldings.length; i++)
		{
			// finds the change (derivative) in the number of each asset
			transactionQuantity[i] = currentHoldings[i] - previousHoldings[i];

			Random r = new Random();
			
			double reaction =  r.nextDouble();

			// Multiply by a random scaler that reflects how much the
			// price reacts to a change in the derivative of the asset.
			// The derivative may, or may not have been, expected by
			// the market and therefore may react differently to
			// changes.
			if(reaction%2 == 0)
			{
			currentMarketValue[i] = currentMarketValue[i]
					+ ((0.05) * reaction) * ((transactionQuantity[i]));
			}
			else
			{
				currentMarketValue[i] = currentMarketValue[i]
						- ((0.05) * reaction) * ((transactionQuantity[i]));
			}

			// Sometimes we end up with a value that is less than 0,
			// but we round up to 0 to create a floor.
			if (currentMarketValue[i] < 0)
			{
				currentMarketValue[i] = 0;
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

		String output = longDecimalFormatter.format(socialTemperature);
		temperatureSliderLabel = new JLabel("temperature = " + output);
		temperatureSliderLabel.setFont(fonts.getBoldFont());

		output = longDecimalFormatter.format(noiseThreshold);
		noiseSliderLabel = new JLabel("noise = " + output);
		noiseSliderLabel.setFont(fonts.getBoldFont());

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

		// create the slider for social temperature
		temperatureSlider = createTemperatureSlider();

		// create the slider for noise
		noiseSlider = createNoiseSlider();

		// create the sliders for social forces
		forceSlider = createForceSliders();

		// create a scroll pane to hold all the force sliders
		JScrollPane forceScrollPane = createForceSliderPanel(width, height);

		// create panel for the temperature
		Font titleFont = new Fonts().getItalicSmallerFont();
		Color titleColor = Color.BLUE;
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder = BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Temperature",
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		JPanel temperaturePanel = new JPanel(new GridBagLayout());
		temperaturePanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder, titledBorder));
		int row = 0;
		temperaturePanel.add(
				temperatureDescriptionLabel,
				new GBC(1, row).setSpan(5, 1).setFill(GBC.BOTH)
						.setWeight(1.0, 1.0).setAnchor(GBC.WEST)
						.setInsets(1, 3, 3, 3));
		row++;
		temperaturePanel.add(temperatureSlider, new GBC(1, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		row++;
		temperaturePanel
				.add(temperatureSliderLabel,
						new GBC(1, row).setSpan(5, 1).setFill(GBC.VERTICAL)
								.setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
								.setInsets(1));

		// create panel for the forces
		Border outerEmptyBorder2 = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder2 = BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Social Forces",
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		JPanel forcePanel = new JPanel(new GridBagLayout());
		forcePanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder2, titledBorder2));
		row = 0;
		forcePanel.add(forceDescriptionLabel, new GBC(1, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1, 3, 3, 3));
		row++;
		forcePanel.add(
				forceScrollPane,
				new GBC(1, row).setSpan(5, 1).setFill(GBC.BOTH)
						.setWeight(1.0, 1.0).setAnchor(GBC.WEST)
						.setInsets(10, 1, 10, 1));

		// create panel for the probability
		Border outerEmptyBorder3 = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder3 = BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				"Innate Weight Gain (Biased Noise)", TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		JPanel probabilityPanel = new JPanel(new GridBagLayout());
		probabilityPanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder3, titledBorder3));
		row = 0;
		probabilityPanel.add(
				probWeightGainDescriptionLabel,
				new GBC(1, row).setSpan(5, 1).setFill(GBC.BOTH)
						.setWeight(1.0, 1.0).setAnchor(GBC.WEST)
						.setInsets(1, 3, 3, 3));

		// create panel for the noise
		Border outerEmptyBorder4 = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder4 = BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Unbiased Noise",
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		JPanel noisePanel = new JPanel(new GridBagLayout());
		noisePanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder4, titledBorder4));
		row = 0;
		noisePanel.add(noiseDescriptionLabel, new GBC(1, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1, 3, 3, 3));
		row++;
		noisePanel.add(
				noiseSlider,
				new GBC(1, row).setSpan(5, 1).setFill(GBC.BOTH)
						.setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		row++;
		noisePanel
				.add(noiseSliderLabel,
						new GBC(1, row).setSpan(5, 1).setFill(GBC.VERTICAL)
								.setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
								.setInsets(1));

		// create panel for the up-down noise
		Border outerEmptyBorder6 = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder6 = BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Up-Down Noise",
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		JPanel upDownNoisePanel = new JPanel(new GridBagLayout());
		upDownNoisePanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder6, titledBorder6));
		row = 0;
		upDownNoisePanel.add(
				upDownNoiseDescriptionLabel,
				new GBC(1, row).setSpan(5, 1).setFill(GBC.BOTH)
						.setWeight(1.0, 1.0).setAnchor(GBC.WEST)
						.setInsets(1, 3, 3, 3));

		// create a scroll panel for all the noise components
		JPanel allNoisePanels = new JPanel(new GridBagLayout());
		row = 0;
		allNoisePanels.add(
				noisePanel,
				new GBC(1, row).setSpan(5, 1).setFill(GBC.BOTH)
						.setWeight(1.0, 1.0).setAnchor(GBC.WEST)
						.setInsets(1, 3, 3, 3));
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
		Border titledBorder5 = BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Noise", TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		JPanel allNoisePanel = new JPanel(new GridBagLayout());
		allNoisePanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder5, titledBorder5));
		row = 0;
		allNoisePanel.add(
				noiseComponentsScroller,
				new GBC(1, row).setSpan(5, 1).setFill(GBC.BOTH)
						.setWeight(1.0, 1.0).setAnchor(GBC.WEST)
						.setInsets(1, 3, 3, 3));

		// create panel for the "restrict movement"
		Border outerEmptyBorder7 = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder7 = BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				"Restrict movement to one unit", TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		JPanel restrictPanel = new JPanel(new GridBagLayout());
		restrictPanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder7, titledBorder7));
		row = 0;
		restrictPanel.add(
				restrictMovementDescriptionLabel,
				new GBC(1, row).setSpan(5, 1).setFill(GBC.BOTH)
						.setWeight(1.0, 1.0).setAnchor(GBC.WEST)
						.setInsets(1, 3, 3, 3));

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
		additionalPropPanel.add(
				allComponentsPanel,
				new GBC(1, row).setSpan(5, 1).setFill(GBC.NONE)
						.setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(0));
		row++;
		additionalPropPanel.add(new JLabel(" "), new GBC(1, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(5.0, 5.0).setAnchor(GBC.WEST)
				.setInsets(0));

		return additionalPropPanel;
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
			sliderPanel.add(forceSliderLabel[state],
					new GBC(1, row).setSpan(5, 1).setFill(GBC.VERTICAL)
							.setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
							.setInsets(verticalSpace, 1, 0, 1));

			// force slider
			row++;
			sliderPanel.add(forceSlider[state], new GBC(1, row).setSpan(5, 1)
					.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
					.setInsets(1));
		}

		// expands to fill all extra space
		row++;
		sliderPanel
				.add(new JLabel(" "),
						new GBC(1, row).setSpan(5, 1).setFill(GBC.BOTH)
								.setWeight(20.0, 20.0).setAnchor(GBC.WEST)
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
			for (int i = 0; i <= numTickMarks; i++)
			{
				double labelValue = minLabelValue
						+ (i * ((maxLabelValue - minLabelValue) / numTickMarks));
				sliderLabelTable.put(
						new Integer(i * majorTickSpacing),
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

			additionalPropertiesPanel = createAdditionalPropertiesPanel();
		}

		return additionalPropertiesPanel;
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

	public static double[] getCurrentMarketValue()
	{
		return currentMarketValue;
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
		{ RuleFolderNames.PROBABILISTIC_FOLDER, RuleFolderNames.SOCIAL_FOLDER };

		return folders;
	}

	@Override
	public String getDisplayName()
	{
		return RULE_NAME;
	}
	
	public double getMaxValue(double[] numbers)
	{
		double maxValue = numbers[0];
		for (int i = 1; i < numbers.length; i++)
		{
			if (numbers[i] > maxValue)
			{
				maxValue = numbers[i];
			}
		}
		return maxValue;
	}


	public double getMinValue(double[] numbers)
	{
		double minValue = numbers[0];
		for (int i = 1; i < numbers.length; i++)
		{
			if (numbers[i] < minValue)
			{
				minValue = numbers[i];
			}
		}
		return minValue;
	}

	@Override
	public String getToolTipDescription()
	{
		return TOOLTIP;
	}

	/**
	 * Method that sorts the number of neighbors into an integer array
	 * (opinionLeaders). The largest opinion leaders can be found by indexing
	 * backwards through the array.
	 */
	public void initializeOpinionLeaders()
	{
		// used to randomly select an attitude for an opinion leader
		Random r = new Random();

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

	@Override
	protected int integerRule(int cell, int[] neighbors, int numStates,
			int generation)
	{
		try{
		if (socialForce == null || socialForceLocalCopy == null
				|| CellularMarketModel.numStates != numStates)
		{
			// so we can use this elsewhere
			CellularMarketModel.numStates = numStates;

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

		// If not the first generation (generation = 0), then set the
		// currentQuantiy (from the previous
		// generation) to previousQuantity. Do this only once at the
		// beginning
		// of each generation.
		if (generation != 0 && currentGeneration != generation)
		{
			// finally, make the previous quantity the current quantity
			previousHoldings = currentHoldings;
		}

		// Only update certain parameters (like temp and force) at the
		// beginning
		// of each generation. This prevents some cells from seeing one
		// temperature and other cells seeing another temperature.
		if (currentGeneration != generation)
		{
			// Update the current generation (local state, used by the rule
			// to update state) to the generation (application state, used
			// by the CAExplorer to update the rule) once at the beginning
			// of each new generation.
			// NOTE: This will lock the conditional statement until the next
			// generation.
			currentGeneration = generation;

			// The temperature used by the rule. Once per generation it
			// is reset to the socialTemperature selected by the user from
			// the "more properties panel."
			socialTemperatureLocalCopy = socialTemperature;

			noiseThresholdLocalCopy = noiseThreshold;

			// Ditto social forces
			for (int i = 0; i < socialForceLocalCopy.length; i++)
			{
				// average investor social force
				socialForceLocalCopy[i] = socialForce[i];
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
		}

		// Array that will store how many cells have each state/commodity,
		// or it can be thought of as an array to store the current
		// holdings.
		int[] numberOfEachState = new int[numStates];

		// initialize
		Arrays.fill(numberOfEachState, 0);

		// Figure out how many cells have each state
		for (int i = 0; i < neighbors.length; i++)
		{
			int state = neighbors[i];

			numberOfEachState[state]++;
		}

		// don't forget the cell itself
		numberOfEachState[cell]++;

		Random random = new Random();

		if (filled && sorted)
		{
			// Use the 1% rule for opinion leaders
			numOpinionLeaders = (int) (opinionLeaders.length * 0.01);
		}

		// the value that is returned
		int cellValue = cell;

		// ***Rule for Opinion Leaders***
		if (opinionLeaders != null
				&& opinionLeaders.length > 0
				&& numOpinionLeaders != 0
				&& neighbors.length >= opinionLeaders[(opinionLeaders.length)
						- numOpinionLeaders])
		{
			// Buy low and sell high (BLASH) trading logic.
			return levelOneModel(cell);
		}

		// *** RULE FOR IMMITATORS***
		// if the temp is 0, then the cell's follow majority rules
		if (socialTemperatureLocalCopy != 0.0)
		{
			// get probability (percent) of each state, including the
			// social
			// force and temperature. (We divide by the partition
			// function
			// later.)
			double[] prob = new double[numStates];

			for (int i = 0; i < numStates; i++)
			{
				// a threshold rule version
				// prob[i] = Math.pow(Math.E, socialForce[i] /
				// socialTemperature)
				// * Math.pow(Math.atan((((double) numberOfEachState[i])
				// / (double) neighbors.length) - 2.0)+(Math.PI/2.0),
				// 1.0 /
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

		// now add unbiased noise -- this changes the state randomly.
		// The
		// probabilityOfSpontaneousWeightGain
		// works like a biased noise that moves toward greater weight.
		// This
		// is
		// just a general noise that changes weights to a random new
		// value.
		if (random.nextDouble() < noiseThresholdLocalCopy)
		{
			// change the state randomly
			cellValue = random.nextInt(numStates);
		}
		
		return cellValue;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}	
		
		return cell;
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
		currentHoldings = new int[numStates];

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
			currentHoldings[state.toInt()]++;

			// find the neighborhood size for each cell
			if (!filled)
			{
				numNeighbors.add(CAController.getCAFrame().getLattice()
						.getNeighbors(c).length);
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

}
