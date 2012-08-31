/*
 * IsingModel -- a class within the Cellular Automaton Explorer. Copyright (C)
 * 2008 David B. Bahr (http://academic.regis.edu/dbahr/) This program is free
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
import java.awt.Font;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cellularAutomata.Cell;
import cellularAutomata.lattice.FourNeighborSquareLattice;
import cellularAutomata.rules.templates.IntegerRuleWithCellsTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MinMaxIntPair;
import cellularAutomata.util.math.RandomSingleton;

/**
 * The classic two-state Ising Model from physics. Instead of a Monte Carlo or
 * Metropolis simulation, the cells are divided into a checkerboard von Neumman
 * lattice as described in Cellular Automata Machines by Toffoli and Margolus pg
 * 188. Half of the cells are updated at each time step -- first the "red" sites
 * are on the checkerboard are updated, and then at the next time step the
 * "black" sites on the checkerboard are updated. This conserves energy (in the
 * canonical ensemble -- in other words, in a heat bath).
 * 
 * @author David Bahr
 */
public class IsingModel extends IntegerRuleWithCellsTemplate
{
	/**
	 * A display name for this class.
	 */
	public static final String RULE_NAME = "Ising Model";

	// a tooltip description for this rule
	private static final String TOOLTIP = "<html> <body><b>Ising Model.</b> The classic "
			+ "Ising model from physics, used to study magnetization.</body></html>";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>Ising Model.</b>"
			+ "<p> <b>For best results</b>, try a large lattice (e.g., 200 by 200) "
			+ "with a 50% random initial state. While it is running, try changing "
			+ "the temperature, interaction strength, and magnetic field in "
			+ "the More Properties panel."
			+ leftClickInstructions
			+ rightClickInstructions + "</body></html>";

	// tool tip for the energy or interaction strength
	private static final String ENERGY_TIP = "The energy or strength of spin-spin "
			+ "interaction between adjacent particles.";

	// A pattern used to display decimals.
	private static final String LONG_DECIMAL_PATTERN = "0.000";

	// tool tip for the magnetic field
	private static final String MAGNETIC_FIELD_TIP = "The external magnetic field "
			+ "being applied to the system.";

	// max value of the sliders (they return integer values that are
	// subdivided
	// into values for the min and max temperature and magnetic field
	private static final int MAX_SLIDER_VALUE = 1000;

	// The max energy (or spin-spin interaction strength).
	private static final double MAX_ENERGY = 2.0;

	// The max magnetic field.
	private static final double MAX_MAGNETIC_FIELD = 3.0;

	// The max temperature.
	private static final double MAX_TEMPERATURE = 10.0;

	// The min energy (or spin-spin interaction strength).
	private static final double MIN_ENERGY = -2.0;

	// The min magnetic field.
	private static final double MIN_MAGNETIC_FIELD = -3.0;

	// The min temperature.
	private static final double MIN_TEMPERATURE = 0.0;

	// the current generation being processed by the rule
	private static volatile int currentGeneration = -1;

	// A pattern used to display decimals.
	private static final String SHORT_DECIMAL_PATTERN = "0.0";

	// tool tip for the temperature
	private static final String TEMPERATURE_TIP = "The temperature of the system. "
			+ "Low temperatures are more stable.";

	// the magnetic field, B
	private static double magneticField = 0.0;

	// a local copy of the mafnetic field, updated once per generation
	private static double magneticFieldLocalCopy = 0.0;

	// Temperature, T. Technically, in this simulation, k*T where k is the
	// Boltzmann constant. In other words, I am normalizing temperature by
	// k. i.e., everywhere that uses temperature actually uses kT. So I
	// just replace kT with T. This is typical in Ising models, in
	// particularly because they are non-dimensional.
	private static volatile double temperature = 1.0;

	// a local copy of the temperature, updated once per generation
	private static volatile double temperatureLocalCopy = 1.0;

	// the exchange energy or spin-spin interaction strength. When J is
	// positive, energy is lower when spins are aligned in the same
	// direction (ferromagnetic). When J is negative, energy is lower when
	// spins are aligned in the opposite direction (anti-ferromagnetic)
	private static double spinSpinInteractionStrengthOrEnergy = 1.0;

	// a local copy of the energy, updated once per generation
	private static double spinSpinInteractionStrengthOrEnergyLocalCopy = 1.0;

	// label that displays the energy
	private static JLabel energySliderLabel = null;

	// label that displays the magnetic field
	private static JLabel magneticFieldSliderLabel = null;

	// label that displays the temperature
	private static JLabel temperatureSliderLabel = null;

	// slider that sets the magnetic field
	private static JSlider energySlider = null;

	// slider that sets the magnetic field
	private static JSlider magneticFieldSlider = null;

	// the additional properties panel
	private static JPanel additionalPropertiesPanel = null;

	// slider that sets the temperature
	private static JSlider temperatureSlider = null;

	// formats decimals for display
	private DecimalFormat longDecimalFormatter = new DecimalFormat(
			LONG_DECIMAL_PATTERN);

	// formats decimals for display
	private DecimalFormat shortDecimalFormatter = new DecimalFormat(
			SHORT_DECIMAL_PATTERN);

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
	public IsingModel(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);
	}

	/**
	 * Updates the cell using Ising model rules (see class description).
	 * 
	 * @param cell
	 *            The values of the cell being updated.
	 * @param neighbors
	 *            The values of the neighbors.
	 * @param generation
	 *            The current generation of the CA.
	 * @return A new state for the cell.
	 */
	protected int integerRule(int cell, int[] neighbors, int numStates,
			int generation, Cell theCell, Cell[] cellNeighors)
	{
		// only update slider parameters (like temp) at the beginning
		// of each generation. This prevents some cells from seeing one
		// temperature and other cells seeing another temperature.
		if(currentGeneration != generation)
		{
			currentGeneration = generation;

			// The temperature used by the rule. Once per generation it
			// is reset to the socialTemperature selected by the user from
			// the "more properties panel."
			temperatureLocalCopy = temperature;

			// ditto magnetic field
			magneticFieldLocalCopy = magneticField;

			// ditto energy
			spinSpinInteractionStrengthOrEnergyLocalCopy = spinSpinInteractionStrengthOrEnergy;
		}

		// the current spin of the cell (-1 or 1). Converts "0's and 1's" to
		// "-1's and 1's" which are traditionally used in Ising models.
		int currentSpin = 2 * cell - 1;

		// The new spin of the cell. Unless it is "flipped" below, this makes
		// the new spin the same as the current spin
		int newSpin = currentSpin;

		// The von Neumann neighborhood is broken into an alternating
		// checkerboard pattern. Each time step, uses the complement
		// checkerboard. This prevents an unphysical oscillation of cell values.
		// See Cellular Automata Machines by Toffoli and Margolus pg. 188 for an
		// explanation of why this is necessary.
		int cellRow = theCell.getCoordinate().getRow();
		int cellCol = theCell.getCoordinate().getColumn();
		if((generation % 2 == 0 && (cellRow + cellCol) % 2 == 0)
				|| (generation % 2 == 1 && (cellRow + cellCol) % 2 == 1))
		{
			// At even time steps, this will only use the "red" cells on a
			// checkerboard overlaid on the von Neumann lattice. At odd time
			// steps this will only use cells on the "black" checkerboard
			// squares.

			// Converts "0's and 1's" to "-1's and 1's" which are
			// traditionally used in Ising models.
			int[] currentNeighborSpins = new int[neighbors.length];
			int totalNeighborhoodSpin = 0;
			for(int i = 0; i < currentNeighborSpins.length; i++)
			{
				currentNeighborSpins[i] = 2 * neighbors[i] - 1;
				totalNeighborhoodSpin += currentNeighborSpins[i];
			}

			// Boltzmann constant. I assume that temperature is normalized
			// with respect to k, so it is assigned a value of 1.0. i.e.,
			// temperature in this simulation is actually kT.
			double k = 1.0;

			// calculate energy change that would result from a flip
			double energyChange = 2.0
					* currentSpin
					* (magneticFieldLocalCopy + spinSpinInteractionStrengthOrEnergyLocalCopy
							* totalNeighborhoodSpin);

			// flip the spin if the energy decreases (energy change is
			// negative) or flip with Boltzmann probability
			if(energyChange < 0.0)
			{
				// then flip
				newSpin = -currentSpin;
			}
			else
			{
				// flip with a probability given by the Boltzmann distribution
				double boltzmannExponential = Math.pow(Math.E, -energyChange
						/ (k * temperatureLocalCopy));

				if(boltzmannExponential > RandomSingleton.getInstance()
						.nextDouble())
				{
					// then flip
					newSpin = -currentSpin;
				}
			}
		}

		return (int) (0.5 * (newSpin + 1.0));
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
		Fonts fonts = new Fonts();

		// slider labels
		String output = longDecimalFormatter.format(temperature);
		temperatureSliderLabel = new JLabel("temperature, T = " + output);
		temperatureSliderLabel.setFont(fonts.getBoldFont());

		output = longDecimalFormatter
				.format(spinSpinInteractionStrengthOrEnergy);
		energySliderLabel = new JLabel("interaction strength, J = " + output);
		energySliderLabel.setFont(fonts.getBoldFont());

		output = longDecimalFormatter.format(magneticField);
		magneticFieldSliderLabel = new JLabel("magnetic field, B = " + output);
		magneticFieldSliderLabel.setFont(fonts.getBoldFont());

		// create the slider for temperature
		temperatureSlider = createTemperatureSlider();

		// create the slider for the energy or spin-spin interaction strength
		energySlider = createEnergySlider();

		// create the slider for the magnetic field
		magneticFieldSlider = createMagneticFieldSlider();

		// create panel for the temperature
		Font titleFont = fonts.getMorePropertiesDescriptionFont();
		Color titleColor = Color.BLUE;
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Temperature", TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		JPanel temperaturePanel = new JPanel(new GridBagLayout());
		temperaturePanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder, titledBorder));
		int row = 0;
		temperaturePanel.add(temperatureSlider, new GBC(1, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		row++;
		temperaturePanel.add(temperatureSliderLabel, new GBC(1, row).setSpan(5,
				1).setFill(GBC.VERTICAL).setWeight(1.0, 1.0).setAnchor(
				GBC.CENTER).setInsets(1));

		// create panel for the energy
		Border outerEmptyBorder2 = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder2 = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(),
				"Spin-Spin Interaction Strength, or Energy", TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		JPanel energyPanel = new JPanel(new GridBagLayout());
		energyPanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder2, titledBorder2));
		row = 0;
		energyPanel.add(energySlider, new GBC(1, row).setSpan(5, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		row++;
		energyPanel.add(energySliderLabel, new GBC(1, row).setSpan(5, 1)
				.setFill(GBC.VERTICAL).setWeight(1.0, 1.0)
				.setAnchor(GBC.CENTER).setInsets(1));

		// create panel for the magnetic field
		Border outerEmptyBorder3 = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder3 = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Magnetc Field", TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		JPanel magneticFieldPanel = new JPanel(new GridBagLayout());
		magneticFieldPanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder3, titledBorder3));
		row = 0;
		magneticFieldPanel.add(magneticFieldSlider, new GBC(1, row).setSpan(5,
				1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		row++;
		magneticFieldPanel.add(magneticFieldSliderLabel, new GBC(1, row)
				.setSpan(5, 1).setFill(GBC.VERTICAL).setWeight(1.0, 1.0)
				.setAnchor(GBC.CENTER).setInsets(1));

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

		// energy
		row++;
		allComponentsPanel.add(energyPanel, new GBC(1, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// magnetic field
		row++;
		allComponentsPanel.add(magneticFieldPanel, new GBC(1, row)
				.setSpan(5, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(
						GBC.WEST).setInsets(1));

		// expands to fill extra space
		row++;
		allComponentsPanel.add(new JLabel(" "), new GBC(1, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(5.0, 5.0).setAnchor(GBC.WEST)
				.setInsets(1));

		return allComponentsPanel;
	}

	/**
	 * Create the slider that controls the magnetic field.
	 */
	private JSlider createEnergySlider()
	{
		// create a slider for magnetic field
		int numTickMarks = 10;
		int majorTickSpacing = (int) Math
				.round(MAX_SLIDER_VALUE / numTickMarks);

		JSlider energySlider = new JSlider(
				0,
				MAX_SLIDER_VALUE,
				(int) (MAX_SLIDER_VALUE
						* (spinSpinInteractionStrengthOrEnergy - MIN_ENERGY) / (MAX_ENERGY - MIN_ENERGY)));
		energySlider.addChangeListener(new SliderListener());
		energySlider.setToolTipText(ENERGY_TIP);

		// set tick marks and labels for the slider
		energySlider.setMajorTickSpacing(majorTickSpacing);
		energySlider.setPaintTicks(true);
		energySlider.setSnapToTicks(false);

		// the hash table of labels
		Hashtable sliderLabelTable = new Hashtable();
		double maxLabelValue = MAX_ENERGY;
		double minLabelValue = MIN_ENERGY;
		for(int i = 0; i <= numTickMarks; i++)
		{
			double labelValue = minLabelValue
					+ (i * ((maxLabelValue - minLabelValue) / numTickMarks));
			sliderLabelTable.put(new Integer(i * majorTickSpacing), new JLabel(
					"" + shortDecimalFormatter.format(labelValue)));
		}
		energySlider.setLabelTable(sliderLabelTable);
		energySlider.setPaintLabels(true);

		return energySlider;
	}

	/**
	 * Create the slider that controls the magnetic field.
	 */
	private JSlider createMagneticFieldSlider()
	{
		// create a slider for magnetic field
		int numTickMarks = 10;
		int majorTickSpacing = (int) Math
				.round(MAX_SLIDER_VALUE / numTickMarks);

		JSlider magneticFieldSlider = new JSlider(
				0,
				MAX_SLIDER_VALUE,
				(int) (MAX_SLIDER_VALUE * (magneticField - MIN_MAGNETIC_FIELD) / (MAX_MAGNETIC_FIELD - MIN_MAGNETIC_FIELD)));
		magneticFieldSlider.addChangeListener(new SliderListener());
		magneticFieldSlider.setToolTipText(MAGNETIC_FIELD_TIP);

		// set tick marks and labels for the slider
		magneticFieldSlider.setMajorTickSpacing(majorTickSpacing);
		magneticFieldSlider.setPaintTicks(true);
		magneticFieldSlider.setSnapToTicks(false);

		// the hash table of labels
		Hashtable sliderLabelTable = new Hashtable();
		double maxLabelValue = MAX_MAGNETIC_FIELD;
		double minLabelValue = MIN_MAGNETIC_FIELD;
		for(int i = 0; i <= numTickMarks; i++)
		{
			double labelValue = minLabelValue
					+ (i * ((maxLabelValue - minLabelValue) / numTickMarks));
			sliderLabelTable.put(new Integer(i * majorTickSpacing), new JLabel(
					"" + shortDecimalFormatter.format(labelValue)));
		}
		magneticFieldSlider.setLabelTable(sliderLabelTable);
		magneticFieldSlider.setPaintLabels(true);

		return magneticFieldSlider;
	}

	/**
	 * Create the slider that controls temperature.
	 */
	private JSlider createTemperatureSlider()
	{
		// create a slider for temperature
		int numTickMarks = 10;
		int majorTickSpacing = (int) Math
				.round(MAX_SLIDER_VALUE / numTickMarks);

		JSlider temperatureSlider = new JSlider(
				0,
				MAX_SLIDER_VALUE,
				(int) (MAX_SLIDER_VALUE * (temperature - MIN_TEMPERATURE) / (MAX_TEMPERATURE - MIN_TEMPERATURE)));
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
		String[] lattices = {FourNeighborSquareLattice.DISPLAY_NAME};
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
		String[] folders = {RuleFolderNames.PHYSICS_FOLDER,
				RuleFolderNames.PROBABILISTIC_FOLDER,
				RuleFolderNames.CLASSICS_FOLDER};

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
	 * Returns null to disable the "Number of States" text field.
	 */
	protected MinMaxIntPair getMinMaxAllowedStates(String latticeDescription)
	{
		return null;
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
	 * The value that will be displayed for the state. Should always be a 2 for
	 * the standard Ising model.
	 */
	protected Integer stateValueToDisplay(String latticeDescription)
	{
		return new Integer(2);
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
			if(e.getSource().equals(temperatureSlider)
					&& temperatureSlider != null
					&& temperatureSliderLabel != null)
			{
				// get the temperature value in *arbitrary units provided by the
				// slider* that's why I divide by MAX_SLIDER_VALUE
				double updatedSliderValue = MIN_TEMPERATURE
						+ ((temperatureSlider.getValue() / (double) MAX_SLIDER_VALUE) * (MAX_TEMPERATURE - MIN_TEMPERATURE));

				// make sure the value changed
				if(temperature != updatedSliderValue)
				{
					temperature = updatedSliderValue;

					// change the display
					String output = longDecimalFormatter.format(temperature);
					temperatureSliderLabel
							.setText("temperature, T = " + output);
				}
			}
			else if(e.getSource().equals(magneticFieldSlider)
					&& magneticFieldSlider != null
					&& magneticFieldSliderLabel != null)
			{
				// get the magnetic field value in *arbitrary units provided by
				// the slider* that's why I divide by MAX_SLIDER_VALUE
				double updatedSliderValue = MIN_MAGNETIC_FIELD
						+ ((magneticFieldSlider.getValue() / (double) MAX_SLIDER_VALUE) * (MAX_MAGNETIC_FIELD - MIN_MAGNETIC_FIELD));

				// make sure the value changed
				if(magneticField != updatedSliderValue)
				{
					magneticField = updatedSliderValue;

					// change the display
					String output = longDecimalFormatter.format(magneticField);
					magneticFieldSliderLabel.setText("magnetic field, B = "
							+ output);
				}
			}
			else if(e.getSource().equals(energySlider) && energySlider != null
					&& energySliderLabel != null)
			{
				// get the magnetic field value in *arbitrary units provided by
				// the slider* that's why I divide by MAX_SLIDER_VALUE
				double updatedSliderValue = MIN_ENERGY
						+ ((energySlider.getValue() / (double) MAX_SLIDER_VALUE) * (MAX_ENERGY - MIN_ENERGY));

				// make sure the value changed
				if(spinSpinInteractionStrengthOrEnergy != updatedSliderValue)
				{
					spinSpinInteractionStrengthOrEnergy = updatedSliderValue;

					// change the display
					String output = longDecimalFormatter
							.format(spinSpinInteractionStrengthOrEnergy);
					energySliderLabel.setText("interaction strength, J = "
							+ output);
				}
			}
		}
	}
}
