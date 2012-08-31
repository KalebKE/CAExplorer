/*
 StartPanel -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2005  David B. Bahr (http://academic.regis.edu/dbahr/)

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

package cellularAutomata.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import cellularAutomata.CAConstants;
import cellularAutomata.CurrentProperties;
import cellularAutomata.reflection.URLResource;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;

/**
 * The panel that contains the start and stop buttons.
 * 
 * @author David Bahr
 */
public class StartPanel extends JPanel
{
	/**
	 * The maximum delay that can be set by the user (in milliseconds).
	 */
	public static final int MAX_DELAY = 100000;

	/**
	 * The string used to display on the "clear graphics" button and used to set
	 * the button's action command.
	 */
	public static final String CLEAR = "Clear Graphics";

	/**
	 * The pattern used to display decimals, particularly for the time delay.
	 */
	public static final String DECIMAL_PATTERN = "0.000";

	/**
	 * String used for the increment one time step button display and for
	 * setting and retrieving the property associated with the Increment button.
	 */
	public static final String INCREMENT = "Increment";

	/**
	 * The string used to display on the "reset" button and used to set the
	 * button's action command.
	 */
	public static final String RESET_STRING = "Reset Simulation";

	/**
	 * Display title for this start panel.
	 */
	public static final String START_PANEL_TITLE = "Controls";

	/**
	 * String used for Start button display and for setting and retrieving the
	 * property associated with the Start button.
	 */
	public static final String START_STRING = "   Start";

	/**
	 * String used for Step 10 button display and for setting and retrieving the
	 * property associated with the Start button.
	 */
	public static final String STEP10 = "Step 10";

	/**
	 * String used for Step Back button display and for setting and retrieving
	 * the property associated with the Start button.
	 */
	public static final String STEP_BACK = "Step Back";

	/**
	 * String used for Step Fill button display and for setting and retrieving
	 * the property associated with the Start button.
	 */
	public static final String STEP_FILL = "Step Fill";

	/**
	 * String used for Stop button display and for setting and retrieving the
	 * property associated with the Stop button.
	 */
	public static final String STOP_STRING = "   Stop";

	/**
	 * String used for the update at end radio button display and used to set
	 * the button's action command.
	 */
	public static final String UPDATE_AT_END_STRING = "At end";

	/**
	 * String used for the update every generation radio button display and used
	 * to set the button's action command.
	 */
	public static final String UPDATE_EVERY_STEP_STRING = "Every generation";

	/**
	 * String used for the update every nth generation radio button display and
	 * used to set the button's action command.
	 */
	public static final String UPDATE_INCREMENT_STRING = "Specified interval";

	/**
	 * A tool tip for the start and stop panel.
	 */
	public static final String TOOL_TIP = "<html><body>start/stop "
			+ "simulation</body></html>";

	// Display title for the display graphics (at end, every generation, etc.).
	private static final String INCREMENT_PANEL_TITLE = "Step forward and back";

	// Display title for the display graphics (at end, every generation, etc.).
	private static final String DELAY_PANEL_TITLE = "Timing";

	// Display title for the display graphics (at end, every generation, etc.).
	private static final String UPDATE_GRAPHICS_PANEL_TITLE = "Display graphics";

	// the time delay for displaying the graphics
	private int timeDelay = 0;

	// The encompassing panel onto which this one will be added.
	private AllPanel outerPanel = null;

	// A listener for components on this panel.
	private AllPanelListener listener = null;

	// color of titles of sections
	private Color titleColor = Color.BLUE;

	// title font (for titles of sections)
	private Font titleFont = new Fonts().getItalicSmallerFont();

	// The resolution of the time delay JSlider. In other words, "how much" each
	// incremental move increases the slider's returned value. A value of 10.0
	// works well. The number is NOT the actual incremental value -- just a
	// factor. Increase this number for finer resolution.
	private static final double resolution = 10.0;

	// fonts for display
	private Fonts fonts = null;

	// The number of tick marks on the time delay slider.
	private static final int numTickMarks = 6;

	// clear the graphics button
	private JButton clearButton = null;

	// The increment button -- increments the CA by one generation.
	private JButton incrementButton = null;

	// button to reset to the initial state.
	private JButton resetButton = null;

	// The start button -- starts the CA.
	private JButton startButton = null;

	// The step 10 button -- increments the CA by ten generations.
	private JButton step10Button = null;

	// The step back button -- decrements the CA by one generations.
	private JButton stepBackButton = null;

	// The step fill button -- fills the screen in one-dim simulations
	private JButton stepFillButton = null;

	// The stop button -- stops the CA.
	private JButton stopButton = null;

	// the label that shows the current delay
	private JLabel speedLabel = null;

	// the label for choosing the graphics update increment
	private JLabel updateLabel = new JLabel("Update Graphics ");

	// allows user to choose to update the graphics after the simulation has
	// stopped
	private JRadioButton atEndRadioButton = null;

	// allows user to choose to update the graphics every generation
	private JRadioButton everyStepRadioButton = null;

	// allows user to choose to update the graphics every n generations
	private JRadioButton incrementRadioButton = null;

	// a slider for choosing the delay (speeds and slows the CA graphics)
	private JSlider delaySlider = null;

	// selects the update interval for the CA graphics
	private JSpinner graphicUpdateIntervalSpinner = null;

	// Automatically stops at the time set in this JTextField.
	private JTextField stopTimeTextField = null;

	// The tooltip for the clear button
	private static final String CLEAR_TIP = "<html>Sets all cells to "
			+ "their \"blank\" value.</html>";

	// The tooltip for the time delay JSlider
	private static final String DELAY_TOOL_TIP = "<html>Slows the simulation."
			+ " <p><p> The delay time is approximate <br> and depends on the "
			+ "simulation.</html>";

	// The tooltip for the graphics update interval JSpinner
	private static final String GRAPHIC_UPDATE_INTERVAL_TIP = "<html>"
			+ "The CA graphics will be updated only <br> "
			+ "at the interval specified here. For <br> "
			+ "example, 2 means that the graphics <br> "
			+ "will be updated every other generation. <br><br>"
			+ "If typing a value, type enter to commit. </html>";

	// The tooltip for the increment button
	private static final String INCREMENT_TIP = "<html>Increments "
			+ "by one generation.</html>";

	// The tooltip for the pause time
	private static final String PAUSE_TIME_TIP = "<html>Automatically stops the "
			+ "simulation after <br> this many generations (time steps).</html>";

	// The tooltip for the start button
	private static final String RESET_TIP = "<html>Resets the simulation to "
			+ "its initial state.</html>";

	// The tooltip for the start button
	private static final String START_TIP = "<html>Starts the simulation.</html>";

	// The tooltip for the start button
	private static final String STEP10_TIP = "<html>Increments by 10 generations.</html>";

	// The tooltip for the start button
	private static final String STEP_BACK_TIP = "<html>Rewinds one generation. "
			+ "Not always available. <br><br>"
			+ "Most CA are not reversible, so previous states are stored <br>"
			+ "only temporarily in memory. <br><br>"
			+ "Some CA use probabilities. These CA may rewind, but <br>"
			+ "going forward again may return the CA to a different state.</html>";

	// The tooltip for the start button
	private static final String STEP_FILL_TIP = "<html>"
			+ "This <b>F</b>ills the screen in one-dimensional simulations. <br><br>"
			+ "Increments by roughly the number of rows (minus the <br>"
			+ "number of lines used as initial states).</html>";

	// The tooltip for the stop button
	private static final String STOP_TIP = "<html>Stops the simulation "
			+ "(may be restarted).</html>";

	// The tooltip for the update grapics at end
	private static final String UPDATE_AT_END_TIP = "<html>"
			+ "Updates graphics only when the simulation is stopped. (faster) </html>";

	// The tooltip for the update every step
	private static final String UPDATE_EVERY_STEP_TIP = "<html>Update graphics "
			+ "every generation. (slower)" + "</html>";

	// The tooltip for the update every nth step
	private static final String UPDATE_INCREMENT_TIP = "<html>Update graphics "
			+ "when the generation is a multiple of the selected value."
			+ "</html>";

	/**
	 * The panel containing the start and stop buttons.
	 * 
	 * @param outerPanel
	 *            The encompassing panel onto which this one will be added.
	 */
	public StartPanel(AllPanel outerPanel)
	{
		super();

		this.outerPanel = outerPanel;
		this.listener = outerPanel.getAllPanelListener();

		this.setOpaque(true);

		// fonts for the components (buttons, etc.)
		fonts = new Fonts();

		// add the components
		addComponents();
	}

	/**
	 * Adds the start, stop, and clear buttons along with other components.
	 */
	private void addComponents()
	{
		// panel for the start and stop buttons
		JPanel startStopPanel = createStartStopButtons();

		// panel for the increment and back buttons
		JPanel incrementPanel = createIncrementDecrementButtons();

		// panel holding the increment for the graphics update
		JPanel graphicsUpdateIncrementPanel = createGraphicsUpdateIntervalPanel();

		// clear the simulation button panel
		JPanel clearPanel = createClearButton();

		// clear the simulation button panel
		JPanel resetPanel = createResetButton();

		// panel holding pause time and delay (and extra space)
		JPanel timeDelayPanel = createStartInnerPanel();

		// make all of the panels see-through
		startStopPanel.setOpaque(true);
		incrementPanel.setOpaque(true);
		graphicsUpdateIncrementPanel.setOpaque(true);
		clearPanel.setOpaque(true);
		resetPanel.setOpaque(true);
		timeDelayPanel.setOpaque(true);

		// create the top panel (raised)
		// GradientPanel raisedPanel = new GradientPanel(new GridBagLayout(),
		// new Color(175, 175, 175), new Color(225, 225, 225));
		JPanel raisedPanel = new JPanel(new GridBagLayout());
		raisedPanel.setBorder(BorderFactory.createRaisedBevelBorder());

		// add buttons to layout
		int row = 0;
		raisedPanel.add(startStopPanel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(0.0, 0.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		raisedPanel.add(incrementPanel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(0.0, 0.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		raisedPanel.add(timeDelayPanel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(0.0, 0.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		raisedPanel.add(graphicsUpdateIncrementPanel, new GBC(1, row).setSpan(
				4, 1).setFill(GBC.BOTH).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		raisedPanel.add(new JLabel(" "), new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		// create the top scroll panel
		JScrollPane scrollPanel = new JScrollPane(raisedPanel);
		scrollPanel.setBorder(BorderFactory.createEmptyBorder());
		scrollPanel
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		// create the bottom panel (not raised)
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(resetPanel, BorderLayout.WEST);
		bottomPanel.add(clearPanel, BorderLayout.EAST);

		// int width = CAFrame.tabbedPaneDimension.width
		// - raisedPanel.getInsets().left - raisedPanel.getInsets().right;
		// int height = raisedPanel.getMinimumSize().height;
		// raisedPanel.setPreferredSize(new Dimension(width, height));

		// create a layout
		this.setLayout(new GridBagLayout());
		this.add(scrollPanel, new GBC(0, 1).setSpan(1, 1).setFill(GBC.BOTH)
				.setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		this.add(bottomPanel, new GBC(0, 2).setSpan(1, 1).setFill(
				GBC.HORIZONTAL).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));
	}

	/**
	 * The time delay slider is presented in a log scale. But the slider itself
	 * is in a linear scale. Therefore milliseconds must be converted to a
	 * slider's value by adjusting from log-scale to linear-scale.
	 * 
	 * @param milliseconds
	 *            The milliseconds that will be converted to a slider value.
	 * @return A slider value.
	 */
	public int convertMilliSecondsToSliderValue(int milliseconds)
	{
		return (int) Math.round(resolution * (numTickMarks - 1)
				* Math.log10(milliseconds));
	}

	/**
	 * The time delay slider is presented in a log scale. But the slider itself
	 * is in a linear scale. Therefore the slider's value must be converted to
	 * milliseconds by adjusting from linear-scale to log-scale.
	 * 
	 * @param sliderValue
	 *            The value returned by the slider.
	 * @return milliseconds of delay.
	 */
	public int convertSliderValueToMilliSeconds(int sliderValue)
	{
		int milliseconds = 0;
		if(sliderValue > 0)
		{
			milliseconds = (int) Math.round(Math.pow(10.0, (double) sliderValue
					/ (resolution * ((double) numTickMarks - 1.0))));
		}

		// to be safe
		if(milliseconds < 0)
		{
			milliseconds = 0;
		}

		return milliseconds;
	}

	/**
	 * Create a clear button and puts in a JPanel.
	 * 
	 * @return contains the clear button.
	 */
	private JPanel createClearButton()
	{
		clearButton = new JButton(CLEAR);
		clearButton.setToolTipText(CLEAR_TIP);
		clearButton.setFont(fonts.getBoldSmallerFont());
		clearButton.setActionCommand(CLEAR);
		clearButton.addActionListener(listener);

		// create a panel that holds the button
		FlowLayout innerLayout = new FlowLayout(FlowLayout.RIGHT);
		JPanel innerPanel = new JPanel(innerLayout);
		innerPanel.add(clearButton);

		return innerPanel;
	}

	/**
	 * Creates step by 1, 10 and back buttons and puts in a JPanel.
	 * 
	 * @return contains the increment and back buttons.
	 */
	private JPanel createIncrementDecrementButtons()
	{
		URL incrementUrl = URLResource.getResource("/"
				+ CAConstants.INCREMENT_BUTTON_IMAGE_PATH);
		URL step10Url = URLResource.getResource("/images/Forward10.gif");
		URL stepFillUrl = URLResource.getResource("/images/ForwardFill.gif");
		URL stepBackUrl = URLResource.getResource("/images/Back.gif");

		// create increment button
		incrementButton = new JButton(new ImageIcon(incrementUrl));
		incrementButton.setToolTipText(INCREMENT_TIP);
		incrementButton.setActionCommand(INCREMENT);
		incrementButton.addActionListener(listener);

		// create step 10 button
		step10Button = new JButton(new ImageIcon(step10Url));
		step10Button.setToolTipText(STEP10_TIP);
		step10Button.setActionCommand(STEP10);
		step10Button.addActionListener(listener);

		// create step fill button
		stepFillButton = new JButton(new ImageIcon(stepFillUrl));
		stepFillButton.setToolTipText(STEP_FILL_TIP);
		stepFillButton.setActionCommand(STEP_FILL);
		stepFillButton.addActionListener(listener);

		// create step back button
		stepBackButton = new JButton(new ImageIcon(stepBackUrl));
		stepBackButton.setToolTipText(STEP_BACK_TIP);
		stepBackButton.setActionCommand(STEP_BACK);
		stepBackButton.addActionListener(listener);

		// panel for the buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		// buttonPanel.setMinimumSize(new Dimension(390, 100));
		buttonPanel.add(stepBackButton);
		buttonPanel.add(new JLabel("  "));
		buttonPanel.add(incrementButton);
		buttonPanel.add(new JLabel("  "));
		buttonPanel.add(step10Button);
		buttonPanel.add(new JLabel("  "));
		buttonPanel.add(stepFillButton);

		// create border
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), INCREMENT_PANEL_TITLE,
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		buttonPanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder, titledBorder));

		return buttonPanel;
	}

	/**
	 * Create radio buttons for the graphics update interval.
	 * 
	 * @return contains the start and stop buttons.
	 */
	private JPanel createGraphicsUpdateIntervalPanel()
	{
		// create every generation radio button
		everyStepRadioButton = new JRadioButton(UPDATE_EVERY_STEP_STRING);
		everyStepRadioButton.setToolTipText(UPDATE_EVERY_STEP_TIP);
		everyStepRadioButton.setFont(fonts.getBoldSmallerFont());
		everyStepRadioButton.setActionCommand(UPDATE_EVERY_STEP_STRING);
		everyStepRadioButton.addActionListener(listener);

		// create every nth generation radio button
		incrementRadioButton = new JRadioButton(UPDATE_INCREMENT_STRING);
		incrementRadioButton.setToolTipText(UPDATE_INCREMENT_TIP);
		incrementRadioButton.setFont(fonts.getBoldSmallerFont());
		incrementRadioButton.setActionCommand(UPDATE_INCREMENT_STRING);
		incrementRadioButton.addActionListener(listener);

		// create "at end" radio button
		atEndRadioButton = new JRadioButton(UPDATE_AT_END_STRING);
		atEndRadioButton.setToolTipText(UPDATE_AT_END_TIP);
		atEndRadioButton.setFont(fonts.getBoldSmallerFont());
		atEndRadioButton.setActionCommand(UPDATE_AT_END_STRING);
		atEndRadioButton.addActionListener(listener);

		// Group the radio buttons so that only one can be selected.
		ButtonGroup group = new ButtonGroup();
		group.add(everyStepRadioButton);
		group.add(incrementRadioButton);
		group.add(atEndRadioButton);

		// properties tell us what the user selected last time the program was
		// run
		int updateInterval = CurrentProperties.getInstance().getDisplayStep();

		// create spinner for the update interval
		SpinnerNumberModel graphicsUpdateModel = new SpinnerNumberModel(
				updateInterval, 1, 999999, 1);
		graphicUpdateIntervalSpinner = new JSpinner(graphicsUpdateModel);
		graphicUpdateIntervalSpinner
				.setToolTipText(GRAPHIC_UPDATE_INTERVAL_TIP);
		graphicUpdateIntervalSpinner.addChangeListener(listener);

		// decide which radio button to select (and enable or disable the
		// spinner)
		boolean updateAtEnd = CurrentProperties.getInstance().isUpdateAtEnd();
		if(updateAtEnd)
		{
			atEndRadioButton.setSelected(true);
			graphicUpdateIntervalSpinner.setEnabled(false);
		}
		else if(updateInterval == 1)
		{
			everyStepRadioButton.setSelected(true);
			graphicUpdateIntervalSpinner.setEnabled(false);
		}
		else
		{
			incrementRadioButton.setSelected(true);
			graphicUpdateIntervalSpinner.setEnabled(true);
		}

		// panel for the graphic update interval buttons
		JPanel graphicsUpdatePanel = new JPanel();
		graphicsUpdatePanel.setLayout(new GridBagLayout());
		// graphicsUpdatePanel.setMinimumSize(new Dimension(300, 100));

		// When creating constraints for the grid bag layout, the GBC takes as
		// arguments "new GBC(gridx, gridy)" where
		// gridx = column position (for the component being added)
		// gridy = row position
		//
		// Use .setSpan(gridwidth, gridheight) to set span of a component where
		// gridwidth = number of columns the component should span
		// gridheight = number of rows the component should span
		//
		// Use .setFill to set a constant that decides whether the component
		// should span/fill the cell. Can make it span vertically, horizontally,
		// both or neither.
		//
		// Use setWeight(weightx, weighty) to specify how much an area can grow
		// or shrink when resizing. 0 means no resizing of the cell area. If
		// all cells have the same weight, then they resize proportionally.
		// If one cell has half of the total weight in a whole row, then that
		// cell will resize twice as much as the other cells. If three cells
		// have weights x, y, z, then cell x will resize proportionally as
		// x/(x+y+z), and cell y will resize proportionally as y/(x+y+z), etc.

		// add buttons to layout
		int row = 0;
		graphicsUpdatePanel.add(everyStepRadioButton, new GBC(1, row).setSpan(
				4, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		graphicsUpdatePanel.add(atEndRadioButton, new GBC(1, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		graphicsUpdatePanel.add(incrementRadioButton, new GBC(1, row).setSpan(
				2, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		graphicsUpdatePanel.add(graphicUpdateIntervalSpinner, new GBC(3, row)
				.setSpan(1, 1).setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(
						GBC.WEST).setInsets(1));

		// create border
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), UPDATE_GRAPHICS_PANEL_TITLE,
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		graphicsUpdatePanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder, titledBorder));

		return graphicsUpdatePanel;
	}

	/**
	 * Create a reset button and puts in a JPanel.
	 * 
	 * @return contains the reset button.
	 */
	private JPanel createResetButton()
	{
		resetButton = new JButton(RESET_STRING);
		resetButton.setToolTipText(RESET_TIP);
		resetButton.setFont(fonts.getBoldSmallerFont());
		resetButton.setActionCommand(RESET_STRING);
		resetButton.addActionListener(listener);

		// create a panel that holds the button
		FlowLayout innerLayout = new FlowLayout(FlowLayout.RIGHT);
		JPanel innerPanel = new JPanel(innerLayout);
		innerPanel.add(resetButton);

		return innerPanel;
	}

	/**
	 * Create and arrange a panel holding the pause time, delay, etc.
	 * 
	 * @param propertiesPanel
	 *            The panel holding the pause time, delay, etc.
	 */
	private JPanel createStartInnerPanel()
	{
		// stop at this time label
		// JLabel stopLabel = new JLabel("Time steps before pausing: ");
		JLabel pauseLabel = new JLabel("Pause after: ");
		pauseLabel.setToolTipText(PAUSE_TIME_TIP);

		// stop at this time text field
		stopTimeTextField = new JTextField(""
				+ CurrentProperties.getInstance().getMaxTime());
		stopTimeTextField.setToolTipText(PAUSE_TIME_TIP);
		stopTimeTextField.setActionCommand(CurrentProperties.MAX_TIME);
		stopTimeTextField.setColumns(8);

		// slow down the simulation label
		JLabel delayLabel = new JLabel("Delay: ");
		delayLabel.setToolTipText(DELAY_TOOL_TIP);

		// indicate the amount of delay on the graphics (label)
		timeDelay = (int) CurrentProperties.getInstance().getTimeDelay();
		double seconds = timeDelay / 1000.0;
		DecimalFormat myFormatter = new DecimalFormat(DECIMAL_PATTERN);
		String output = myFormatter.format(seconds);
		speedLabel = new JLabel("Approximately " + output + " seconds.");
		speedLabel.setToolTipText(DELAY_TOOL_TIP);
		speedLabel.setFont(fonts.getBoldSmallerFont());

		// Add a slider for speeding and slowing the simulation (set the delay).
		// Note that we convert to a log scale!
		double maxValue = convertMilliSecondsToSliderValue(MAX_DELAY);
		int currentValue = convertMilliSecondsToSliderValue(timeDelay);

		delaySlider = new JSlider(0, (int) maxValue, currentValue);
		delaySlider.addChangeListener(listener);
		delaySlider.setToolTipText(DELAY_TOOL_TIP);

		// set tick marks and labels for the log-scale slider
		int labelSpacing = (int) Math.round(maxValue / (numTickMarks - 1));
		delaySlider.setPaintTicks(true);
		delaySlider.setMajorTickSpacing(labelSpacing);
		delaySlider.setSnapToTicks(false);

		Hashtable sliderLabelTable = new Hashtable();
		sliderLabelTable.put(new Integer(0 * labelSpacing), new JLabel("0.0"));
		for(int i = 1; i < numTickMarks; i++)
		{
			double labelValue = MAX_DELAY
					/ (1000.0 * Math.pow(10.0, (numTickMarks - 1) - i));

			if(labelValue >= 1.0)
			{
				// don't show the decimal place
				sliderLabelTable.put(new Integer(i * labelSpacing), new JLabel(
						"" + (int) labelValue));
			}
			else
			{
				// show decimal place
				sliderLabelTable.put(new Integer(i * labelSpacing), new JLabel(
						"" + labelValue));
			}

		}
		delaySlider.setLabelTable(sliderLabelTable);
		delaySlider.setPaintLabels(true);

		// the panel on which we add the controls
		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new GridBagLayout());

		// pause text field
		int row = 0;
		innerPanel.add(pauseLabel, new GBC(1, row).setSpan(1, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		innerPanel.add(stopTimeTextField, new GBC(2, row).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.EAST)
				.setInsets(1));
		innerPanel.add(new JLabel(" "), new GBC(3, row).setSpan(1, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		// space
		row++;
		innerPanel.add(new JLabel(" "), new GBC(1, row).setSpan(3, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		// delay label
		row++;
		innerPanel.add(delayLabel, new GBC(1, row).setSpan(1, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		innerPanel.add(speedLabel, new GBC(2, row).setSpan(2, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
				.setInsets(1));

		// delay slider
		row++;
		innerPanel.add(new JLabel(" "), new GBC(1, row).setSpan(1, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		innerPanel.add(delaySlider, new GBC(2, row).setSpan(2, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
				.setInsets(1));

		// create border
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border innerEmptyBorder = BorderFactory.createEmptyBorder(2, 6, 3, 2);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), DELAY_PANEL_TITLE, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		innerPanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder, (BorderFactory.createCompoundBorder(
						titledBorder, innerEmptyBorder))));

		return innerPanel;
	}

	/**
	 * Create start and stop buttons and puts in a JPanel.
	 * 
	 * @return contains the start and stop buttons.
	 */
	private JPanel createStartStopButtons()
	{
		// get the image URLs (searches the classpath to find the image file).
		URL startUrl = URLResource.getResource("/"
				+ CAConstants.START_BUTTON_IMAGE_PATH);
		URL stopUrl = URLResource.getResource("/"
				+ CAConstants.STOP_BUTTON_IMAGE_PATH);
		URL incrementUrl = URLResource.getResource("/"
				+ CAConstants.INCREMENT_BUTTON_IMAGE_PATH);
		URL step10Url = URLResource.getResource("/images/Forward10.gif");
		URL stepBackUrl = URLResource.getResource("/images/Back.gif");

		// create start button
		startButton = new JButton(START_STRING, new ImageIcon(startUrl));
		startButton.setToolTipText(START_TIP);
		startButton.setFont(fonts.getBoldBiggerFont());
		startButton.setActionCommand(START_STRING);
		startButton.addActionListener(listener);
		startButton.setOpaque(true);

		// create stop button
		stopButton = new JButton(STOP_STRING, new ImageIcon(stopUrl));
		stopButton.setToolTipText(STOP_TIP);
		stopButton.setFont(fonts.getBoldBiggerFont());
		stopButton.setActionCommand(STOP_STRING);
		stopButton.addActionListener(listener);
		stopButton.setEnabled(false);
		stopButton.setOpaque(true);

		// set sizes for the buttons
		int width = 125;
		int height = width / 3;
		Dimension dim = new Dimension(width, height);
		startButton.setPreferredSize(dim);
		stopButton.setPreferredSize(dim);

		// panel for the start and stop buttons
		JPanel startStopPanel = new JPanel();
		startStopPanel.setLayout(new GridBagLayout());
		int row = 0;
		startStopPanel.add(startButton, new GBC(1, row).setSpan(1, 1).setFill(
				GBC.BOTH).setWeight(1.0, 0.0).setAnchor(GBC.CENTER)
				.setInsets(1));
		startStopPanel.add(stopButton, new GBC(2, row).setSpan(6, 1).setFill(
				GBC.BOTH).setWeight(1.0, 0.0).setAnchor(GBC.CENTER)
				.setInsets(1));

		// startStopPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		// //startStopPanel.setMinimumSize(new Dimension(390, 100));
		// startStopPanel.add(startButton);
		// startStopPanel.add(new JLabel(" "));
		// startStopPanel.add(stopButton);

		// create space
		startStopPanel.setBorder(BorderFactory
				.createEmptyBorder(30, 20, 30, 20));

		return startStopPanel;
	}

	/**
	 * Gets the radio button that says to update the graphics only at the end of
	 * the simulation.
	 * 
	 * @return The "at end" radio button.
	 */
	public JRadioButton getAtEndRadioButton()
	{
		return atEndRadioButton;
	}

	/**
	 * Gets the button that clears the simulation.
	 * 
	 * @return The clear button.
	 */
	public JButton getClearButton()
	{
		return clearButton;
	}

	/**
	 * Gets the slider that set the time delay.
	 * 
	 * @return The delay slider.
	 */
	public JSlider getDelaySlider()
	{
		return delaySlider;
	}

	/**
	 * Gets the radio button that says to update the graphics every generation.
	 * 
	 * @return The "every step" radio button.
	 */
	public JRadioButton getEveryStepRadioButton()
	{
		return everyStepRadioButton;
	}

	/**
	 * Gets the spinner that sets the graphics update increment.
	 * 
	 * @return The increment spinner.
	 */
	public JSpinner getGraphicsUpdateSpinner()
	{
		return graphicUpdateIntervalSpinner;
	}

	/**
	 * Gets the button that increments the simulation.
	 * 
	 * @return The increment button.
	 */
	public JButton getIncrementButton()
	{
		return incrementButton;
	}

	/**
	 * Gets the radio button that says to update the graphics every specified
	 * interval.
	 * 
	 * @return The increment radio button.
	 */
	public JRadioButton getIncrementRadioButton()
	{
		return incrementRadioButton;
	}

	/**
	 * Gets the button that resets the simulation.
	 * 
	 * @return The reset button.
	 */
	public JButton getResetButton()
	{
		return resetButton;
	}

	/**
	 * Gets the button that starts the simulation.
	 * 
	 * @return The start button.
	 */
	public JButton getStartButton()
	{
		return startButton;
	}

	/**
	 * Gets the button that steps the simulation by 10.
	 * 
	 * @return The step10 button.
	 */
	public JButton getStep10Button()
	{
		return step10Button;
	}

	/**
	 * Gets the button that decrements the simulation by 1.
	 * 
	 * @return The step back button.
	 */
	public JButton getStepBackButton()
	{
		return stepBackButton;
	}

	/**
	 * Gets the button that steps the simulation by the number of rows - 1 (this
	 * fills the grid).
	 * 
	 * @return The button that fills a one dim grid by stepping the number of
	 *         rows - 1..
	 */
	public JButton getStepFillButton()
	{
		return stepFillButton;
	}

	/**
	 * Gets the button that stops the simulation.
	 * 
	 * @return The stop button.
	 */
	public JButton getStopButton()
	{
		return stopButton;
	}

	/**
	 * Gets the editable text field that displays the stop time.
	 * 
	 * @return The stop time field.
	 */
	public JTextField getStopTimeField()
	{
		return stopTimeTextField;
	}

	/**
	 * Gets the time delay.
	 * 
	 * @return The time delay.
	 */
	public int getTimeDelay()
	{
		return timeDelay;
	}

	/**
	 * Set a delay time in *seconds* on the speed label.
	 * 
	 * @param timeDelay
	 *            The delay in milliseconds.
	 */
	public void setSpeedLabel(int timeDelay)
	{
		// indicate the amount of delay on the speed label
		double seconds = timeDelay / 1000.0;
		DecimalFormat myFormatter = new DecimalFormat(
				StartPanel.DECIMAL_PATTERN);
		String output = myFormatter.format(seconds);
		speedLabel.setText("Approximately " + output + " seconds.");
	}

	/**
	 * Set a new delay time.
	 * 
	 * @param timeDelay
	 *            The delay in milliseconds.
	 */
	public void setTimeDelay(int timeDelay)
	{
		this.timeDelay = timeDelay;

		// set the speed label
		setSpeedLabel(timeDelay);

		// set a property
		CurrentProperties.getInstance().setTimeDelay(timeDelay);
	}
}
