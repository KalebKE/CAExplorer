/*
 NeighborhoodSizeAnalysis -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.analysis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import cellularAutomata.CAController;
import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.IntegerCellStateView;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.graphics.colors.colorChooser.IntegerStateColorChooser;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.rules.Rule;
import cellularAutomata.rules.templates.FiniteObjectRuleTemplate;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;
import cellularAutomata.util.SimplePlot;
import cellularAutomata.util.files.FileWriter;

/**
 * Finds the distribution of connectivity of sites with any given state. Most
 * applicable to lattices with variable sized neighborhoods like the small world
 * lattice.
 * 
 * @author David Bahr
 */
public class NeighborhoodSizeAnalysis extends Analysis implements
		ActionListener
{
	// the constant representing all states. Should not be any integer
	// between 0 and numStates (inclusive). Used to identify the user's choice
	// of state(s) to analyze.
	private static final int ALL_STATES_CHOICE = -3;

	// the constant representing all non-empty states. Should not be any integer
	// between 0 and numStates (inclusive). Used to identify the user's choice
	// of state(s) to analyze.
	private static final int ALL_NON_EMPTY_STATES_CHOICE = -1;

	// the constant representing the empty state. Should not be any integer
	// between 0 and numStates (inclusive). Used to identify the user's choice
	// of state(s) to analyze.
	private static final int EMPTY_STATE_CHOICE = -2;

	// the interval of time steps between which plot axes are adjusted. Don't
	// want to adjust too often or the plot "flickers"
	private static final int TIME_TO_ADJUST_AXES = 10;

	// a brief display name for this class
	private static final String ANALYSIS_NAME = "Neighborhood Sizes";

	// display info for this class
	private static final String INFO_MESSAGE = "Plots the distribution of "
			+ "neighborhood sizes. In other words, for each cell on the lattice, this "
			+ "analysis finds the number "
			+ "of neighbors that are connected to that cell. These numbers are collected "
			+ "for all cells and plotted. When a particular state "
			+ "is selected, only cells of that state are plotted. \n"
			+ "This analysis is most useful for lattices with variable-sized "
			+ "neighborhoods. For example, try the small-world lattice or the random "
			+ "asymmetric lattice.";

	// the label for the "plot zero values" check box
	private static final String PLOT_ZERO_VALUES = "   Plot neighborhoods of size 0";

	// the tooltip for the "plot zero values" check box
	private static final String PLOT_ZERO_VALUES_TOOLTIP = "<html><body>"
			+ "When unchecked, this prevents clusters of size 0 from being plotted. <br>"
			+ "The zero values can obscure other data. (Note that the log-log plot <br>"
			+ "never shows clusters of size 0, which would plot at -infinity.)</body></html>";

	// title for the subpanel that lets the user select the state to be analyzed
	private static final String RADIO_BUTTON_PANEL_TITLE = "Select state to analyze";

	// the action command for saving the data and the label used by the "save
	// data" check box
	private static final String SAVE_DATA = "   Save the data";

	// a tooltip for the save data check box
	private static final String SAVE_DATA_TOOLTIP = "<html>Saves neighborhood "
			+ "distribution data to a file (saves <br> every generation while the "
			+ "box is checked).</html>";

	// text for the button that lets the user select the state for which the
	// cluster sizes will be calculated.
	private static final String SELECT_STATE = "Select state";

	// tooltip for the button that lets the state be selected
	private static final String SELECT_STATE_TOOLTIP = "Select a state for which the \n"
			+ "neighborhood sizes will be calculated.";

	// the action command for the state chooser
	private static final String STATE_CHOOSER = "state chooser";

	// a brief tooltip description for this class
	private static final String TOOLTIP = "<html>finds and plots the size of each "
			+ "cell's neighborhood</html>";

	// show the zero values on each plot
	private boolean plotZeroValues = false;

	// the current view for the rule
	private CellStateView view = null;

	// the color of the current state
	private Color currentColor = Color.GRAY;

	// color of titles of sections
	private Color titleColor = Color.BLUE;

	// a color patch so the user can see the color being analyzed
	private ColorPatch colorPatch = null;

	// the max value on the x-axis of the log-log plot at the last time step
	private double oldLogMaxXValue = 1;

	// the max value on the y-axis of the log-log plot at the last time step
	private double oldLogMaxYValue = 1;

	// time steps since last adjusted the log x-axis value
	private int elapsedTimeStepsSinceLastAdjustedLogXAxis = 0;

	// time steps since last adjusted the log y-axis value
	private int elapsedTimeStepsSinceLastAdjustedLogYAxis = 0;

	// time steps since last adjusted the x-axis value
	private int elapsedTimeStepsSinceLastAdjustedXAxis = 0;

	// time steps since last adjusted the y-axis value
	private int elapsedTimeStepsSinceLastAdjustedYAxis = 0;

	// the state that was last selected by the user (used in the graphics)
	private int lastSelectedState = ALL_STATES_CHOICE;

	// the max value on the x-axis at the last time step
	private int oldMaxXValue = 1;

	// the max value on the y-axis at the last time step
	private int oldMaxYValue = 1;

	// the number of states in the current simulation
	private int numStates = 2;

	// the state that has been selected for analysis
	private int selectedState = 1;

	// the number of neighborhoods of each size
	private int[] numberOfNeighborhoodsOfEachSize = null;

	// If the user wants to save the data to a file, this will be instantiated
	private FileWriter fileWriter = null;

	// fonts for display
	private Fonts fonts = new Fonts();

	// title font (for titles of sections)
	private Font titleFont = new Fonts().getItalicSmallerFont();

	// used to select the state that will be analyzed
	private IntegerStateColorChooser integerColorChooser = null;

	// the button for selecting the state to be analyzed
	private JButton selectStateButton = null;

	// label for the current generation
	private JLabel generationDataLabel = null;

	// The check box that lets the user plot values of zero
	private JCheckBox plotZeroValueCheckBox = null;

	// The check box that lets the user save the data
	private JCheckBox saveDataCheckBox = null;

	// the panel where results are displayed
	private JPanel displayPanel = null;

	// radio button for choosing all states (i.e., all will be used to
	// calculate the neighborhood sizes)
	private JRadioButton allStatesButton = null;

	// radio button for choosing the empty state (that will be used to
	// calculate the neighborhood sizes)
	private JRadioButton emptyStateButton = null;

	// radio button for choosing the non-empty states (that will be used to
	// calculate the neighborhood sizes)
	private JRadioButton nonEmptyStatesButton = null;

	// radio button for choosing a particular state (that will be used to
	// calculate the neighborhood sizes)
	private JRadioButton particularStateButton = null;

	// the neighborhood sizes as points of (size, number neighborhoods of that
	// size)
	private Point2D.Double[] neighborhoodNumbers = null;

	// the current rule
	private Rule rule = null;

	// a panel that plots the neighborhood data on log-log axes
	private SimplePlot logPlot = null;

	// a panel that plots the neighborhood data
	private SimplePlot plot = null;

	// a delimiter for spacing data in the data file
	private String delimiter = null;

	// data that will be saved to a file
	private String[] data = null;

	/**
	 * Create an analyzer that finds neighborhood sizes.
	 * <p>
	 * When building child classes, the minimalOrLazyInitialization parameter
	 * must be included but may be ignored. However, the boolean is intended to
	 * indicate when the child's constructor should build an analysis with as
	 * small a footprint as possible. In order to load analyses by reflection,
	 * the application must query the child classes for information like their
	 * display names, tooltip descriptions, etc. At these times it makes no
	 * sense to build the complete analysis which may have a large footprint in
	 * memory.
	 * <p>
	 * It is recommended that the child's constructor and instance variables do
	 * not initialize any variables and that variables be initialized only when
	 * first needed (lazy initialization). Or all initializations in the
	 * constructor may be placed in an <code>if</code> statement (as
	 * illustrated in the parent constructor and in most other analyses designed
	 * by David Bahr).
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
	 *            small a footprint as possible. When false, the analysis is
	 *            fully constructed, complete with close buttons, display
	 *            panels, etc. If uncertain, set this variable to false.
	 */
	public NeighborhoodSizeAnalysis(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			setUpAnalysis();
		}
	}

	/**
	 * Actions to take when "Select state to analyze" is selected.
	 */
	private void chooseAnalysisState()
	{
		// create a state/color chooser
		integerColorChooser = new IntegerStateColorChooser(null, numStates,
				selectedState, currentColor, new OkColorListener(STATE_CHOOSER));

		integerColorChooser.setVisible(true);
	}

	/**
	 * Converts the distribution of sizes to a Point2D array for plotting (not
	 * relevant to the log-log plot -- this is only used by the regular plot).
	 */
	private void convertNeighborhoodSizesToPointArray()
	{
		synchronized(this)
		{
			if(plotZeroValues)
			{
				// convert to a Point2D array
				if(numberOfNeighborhoodsOfEachSize.length > 0)
				{
					neighborhoodNumbers = new Point2D.Double[numberOfNeighborhoodsOfEachSize.length];
					for(int i = 0; i < neighborhoodNumbers.length; i++)
					{
						neighborhoodNumbers[i] = new Point2D.Double(i,
								numberOfNeighborhoodsOfEachSize[i]);
					}
				}
				else
				{
					neighborhoodNumbers = new Point2D.Double[1];
					neighborhoodNumbers[0] = new Point2D.Double(0, 0);
				}
			}
			else
			{
				// convert to a Point2D array
				if(numberOfNeighborhoodsOfEachSize.length > 0)
				{
					// create a list of non-zero values
					LinkedList<Point2D.Double> numList = new LinkedList<Point2D.Double>();
					for(int i = 0; i < numberOfNeighborhoodsOfEachSize.length; i++)
					{
						if(numberOfNeighborhoodsOfEachSize[i] > 0)
						{
							numList.add(new Point2D.Double(i,
									numberOfNeighborhoodsOfEachSize[i]));
						}
					}

					// convert to an array so compatible with other code already
					// written :-(
					neighborhoodNumbers = new Point2D.Double[numList.size()];
					for(int i = 0; i < neighborhoodNumbers.length; i++)
					{
						neighborhoodNumbers[i] = numList.get(i);
					}
				}
				else
				{
					neighborhoodNumbers = new Point2D.Double[1];
					neighborhoodNumbers[0] = new Point2D.Double(0, 0);
				}
			}
		}
	}

	/**
	 * Create labels used to display the data for the population statistics.
	 */
	private void createDataDisplayLabels()
	{
		// if one is null, then they all are
		if(generationDataLabel == null)
		{
			generationDataLabel = new JLabel("");
		}
	}

	/**
	 * Create the panel used to display the population statistics.
	 */
	private void createDisplayPanel()
	{
		int displayWidth = CAFrame.tabbedPaneDimension.width;
		int displayHeight = 920;

		// create the display panel
		if(displayPanel == null)
		{
			displayPanel = new JPanel(new GridBagLayout());
			displayPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			displayPanel.setPreferredSize(new Dimension(displayWidth,
					displayHeight));
		}
		else
		{
			displayPanel.removeAll();
		}

		// create a panel that displays messages
		JPanel messagePanel = createMessagePanel();

		// create a panel that holds the select state radio buttons
		JPanel stateSelectionPanel = createStateRadioButtonPanel();

		// create the labels for the display
		createDataDisplayLabels();

		// create a "save data" check box
		saveDataCheckBox = new JCheckBox(SAVE_DATA);
		saveDataCheckBox.setToolTipText(SAVE_DATA_TOOLTIP);
		saveDataCheckBox.setActionCommand(SAVE_DATA);
		saveDataCheckBox.addActionListener(this);
		JPanel saveDataPanel = new JPanel(new BorderLayout());
		saveDataPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
		saveDataPanel.add(BorderLayout.CENTER, saveDataCheckBox);

		// create a "plot zero values" check box
		plotZeroValueCheckBox = new JCheckBox(PLOT_ZERO_VALUES);
		plotZeroValueCheckBox.setSelected(false);
		plotZeroValueCheckBox.setToolTipText(PLOT_ZERO_VALUES_TOOLTIP);
		plotZeroValueCheckBox.setActionCommand(PLOT_ZERO_VALUES);
		plotZeroValueCheckBox.addActionListener(this);
		JPanel plotZeroValuePanel = new JPanel(new BorderLayout());
		plotZeroValuePanel.setBorder(BorderFactory
				.createEmptyBorder(7, 7, 7, 7));
		plotZeroValuePanel.add(BorderLayout.CENTER, plotZeroValueCheckBox);

		// create a panels that plots the data
		plot = new SimplePlot();
		logPlot = new SimplePlot();

		// add all the components to the panel
		int row = 0;
		displayPanel.add(messagePanel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		displayPanel.add(plot, new GBC(1, row).setSpan(4, 1).setFill(GBC.BOTH)
				.setWeight(10.0, 10.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		displayPanel.add(logPlot, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(10.0, 10.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		displayPanel.add(plotZeroValuePanel, new GBC(1, row).setSpan(4, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
				.setInsets(1));

		row++;
		displayPanel.add(stateSelectionPanel, new GBC(1, row).setSpan(4, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		displayPanel.add(saveDataPanel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
				.setInsets(1));
	}

	/**
	 * This uses a handy file writing utility to create a file writer.
	 */
	private void createFileWriter()
	{
		try
		{
			// This will prompt the user to enter a file. (The save data file
			// path parameter is just the default folder where the file chooser
			// will open.)
			fileWriter = new FileWriter(CurrentProperties.getInstance()
					.getSaveDataFilePath());

			// data delimiters (what string will be used to separate data in the
			// file)
			delimiter = CurrentProperties.getInstance().getDataDelimiters();

			// save a header (have to plan ahead for the max possible cluster
			// size, width * height)
			int width = CurrentProperties.getInstance().getNumColumns();
			int height = CurrentProperties.getInstance().getNumRows();
			String[] header = new String[(width * height) + 1];
			header[0] = "Generation";
			for(int i = 1; i < header.length; i++)
			{
				header[i] = "Neighborhoods of size " + i + ":";
			}

			fileWriter.writeData(header, delimiter);

			// save the initial data (at the generation when the user requested
			// that the data be saved)
			if(data != null)
			{
				fileWriter.writeData(data, delimiter);
			}
		}
		catch(IOException e)
		{
			// This happens if the user did not select a valid file. (For
			// example, the user cancelled and did not choose any file when
			// prompted.) So uncheck the "file save" box
			if(saveDataCheckBox != null)
			{
				saveDataCheckBox.setSelected(false);
			}

			// tell the user that they really should have selected a file
			String message = "A valid file was not selected, so the data \n"
					+ "will not be saved.";
			JOptionPane.showMessageDialog(CAController.getCAFrame().getFrame(),
					message, "Valid file not selected",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Creates a panel that displays messages.
	 * 
	 * @return A panel containing messages.
	 */
	private JPanel createMessagePanel()
	{
		// a "grab their attention" panel
		AttentionPanel attentionPanel = new AttentionPanel("Neighborhood Sizes");

		MultilineLabel messageLabel = new MultilineLabel(INFO_MESSAGE);
		messageLabel.setFont(fonts.getAnalysesDescriptionFont());
		messageLabel.setMargin(new Insets(6, 10, 2, 16));

		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		messagePanel.add(attentionPanel, BorderLayout.NORTH);
		messagePanel.add(messageLabel, BorderLayout.CENTER);

		return messagePanel;
	}

	/**
	 * Creates radio buttons to choose which state(s) will be used to calculate
	 * the fractal dimension.
	 */
	private JPanel createStateRadioButtonPanel()
	{
		allStatesButton = new JRadioButton("all states");
		allStatesButton.setFont(fonts.getPlainFont());
		allStatesButton.addItemListener(new StateChoiceListener());

		nonEmptyStatesButton = new JRadioButton("all non-empty states");
		nonEmptyStatesButton.setFont(fonts.getPlainFont());
		nonEmptyStatesButton.addItemListener(new StateChoiceListener());
		nonEmptyStatesButton.setSelected(false);

		emptyStateButton = new JRadioButton("empty state");
		emptyStateButton.setFont(fonts.getPlainFont());
		emptyStateButton.addItemListener(new StateChoiceListener());
		emptyStateButton.setSelected(false);

		particularStateButton = new JRadioButton("choose state");
		particularStateButton.setFont(fonts.getPlainFont());
		particularStateButton.addItemListener(new StateChoiceListener());
		particularStateButton.setSelected(false);

		// put them in a group so that they behave as radio buttons
		ButtonGroup group = new ButtonGroup();
		group.add(allStatesButton);
		group.add(nonEmptyStatesButton);
		group.add(emptyStateButton);
		group.add(particularStateButton);

		// create a "select state to analyze" button and a color patch that
		// shows the state
		selectStateButton = new JButton(SELECT_STATE);
		selectStateButton.setActionCommand(SELECT_STATE);
		selectStateButton.setToolTipText(SELECT_STATE_TOOLTIP);
		selectStateButton.addActionListener(this);

		// create the selection JButton and color patch that goes next to the
		// particularStateButton
		JPanel stateSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		stateSelectionPanel.add(selectStateButton);
		stateSelectionPanel.add(colorPatch);

		// create combined particularStateButton and colorPatch panel
		// JPanel selectStatePane = new JPanel(new BorderLayout());
		// selectStatePane.add(BorderLayout)

		// create boxes for each column of the display (a Box uses the
		// BoxLayout, so it is handy for laying out components)
		Box boxOfRadioButtons = Box.createVerticalBox();
		Box boxWithColorPatch = Box.createVerticalBox();

		// the amount of vertical and horizontal space to put between components
		int verticalSpace = 5;
		int horizontalSpace = 0;

		// add the radio buttons to the first vertical box
		boxOfRadioButtons.add(allStatesButton);
		boxOfRadioButtons.add(Box.createVerticalStrut(verticalSpace));
		boxOfRadioButtons.add(nonEmptyStatesButton);
		boxOfRadioButtons.add(Box.createVerticalStrut(verticalSpace));
		boxOfRadioButtons.add(emptyStateButton);
		boxOfRadioButtons.add(Box.createVerticalStrut(verticalSpace));
		boxOfRadioButtons.add(particularStateButton);

		// add the color patch to the second vertical box
		boxWithColorPatch.add(new JLabel(" "));
		boxWithColorPatch.add(Box.createVerticalStrut(verticalSpace));
		boxWithColorPatch.add(new JLabel(" "));
		boxWithColorPatch.add(Box.createVerticalStrut(verticalSpace));
		boxWithColorPatch.add(new JLabel(" "));
		boxWithColorPatch.add(Box.createVerticalStrut(verticalSpace + 15));
		boxWithColorPatch.add(stateSelectionPanel);

		// create another box that holds both of the label boxes
		Box boxOfLabels = Box.createHorizontalBox();
		boxOfLabels.add(boxOfRadioButtons);
		boxOfLabels.add(Box.createHorizontalStrut(horizontalSpace));
		boxOfLabels.add(boxWithColorPatch);

		// create a JPanel for the radio buttons and their containing box
		JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 5);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), RADIO_BUTTON_PANEL_TITLE,
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		Border compoundBorder = BorderFactory.createCompoundBorder(
				titledBorder, emptyBorder);
		radioPanel.setBorder(compoundBorder);
		radioPanel.add(boxOfLabels);

		return radioPanel;
	}

	/**
	 * Plots the population data on a regular plot.
	 */
	private void plotData()
	{
		// axes labels
		plot.setXAxisLabel("neighborhood size, S");
		plot.setYAxisLabel("# of neighborhoods, N(S)");

		// the max x and y-values plotted
		int maxYValue = 1;
		int maxXValue = 1;

		// find the max y-value for plotting
		for(int i = 0; i < numberOfNeighborhoodsOfEachSize.length; i++)
		{
			if(numberOfNeighborhoodsOfEachSize[i] > maxYValue)
			{
				maxYValue = numberOfNeighborhoodsOfEachSize[i];
			}
		}
		if(maxYValue < 1)
		{
			maxYValue = 1;
		}

		// find the max x value for plotting
		for(int i = 0; i < neighborhoodNumbers.length; i++)
		{
			if(neighborhoodNumbers[i].x > maxXValue)
			{
				maxXValue = (int) neighborhoodNumbers[i].x;
			}
		}

		// set the min and max x values on the plot
		elapsedTimeStepsSinceLastAdjustedXAxis++;
		plot.setMinimumXValue(0);
		if(neighborhoodNumbers.length > 0)
		{
			if(maxXValue >= oldMaxXValue
					|| elapsedTimeStepsSinceLastAdjustedXAxis == TIME_TO_ADJUST_AXES)
			{
				plot.setMaximumXValue(maxXValue);

				oldMaxXValue = maxXValue;
				elapsedTimeStepsSinceLastAdjustedXAxis = 0;

				// draw some extra points on the x-axis (looks good)
				if(maxXValue > 20)
				{
					int numberOfExtraXPoints = 3;
					double[] xValues = new double[numberOfExtraXPoints];
					for(int i = 0; i < xValues.length; i++)
					{
						xValues[i] = (int) (i + 1.0) * ((double) maxXValue)
								/ (double) (numberOfExtraXPoints + 1);
					}
					plot.setExtraXAxisValues(xValues);
				}
				else if(maxXValue > 1)
				{
					int numberOfExtraXPoints = maxXValue - 1;
					double[] xValues = new double[numberOfExtraXPoints];
					for(int i = 0; i < xValues.length; i++)
					{
						xValues[i] = (int) (i + 1.0) * ((double) maxXValue)
								/ (double) (numberOfExtraXPoints + 1);
					}
					plot.setExtraXAxisValues(xValues);
				}
				plot.showXValuesAsInts(true);
			}
		}

		// set the min y value on the plot
		plot.setMinimumYValue(0);

		// set the max y value on the plot (to nearest 10^nth power)
		elapsedTimeStepsSinceLastAdjustedYAxis++;
		maxYValue = (int) Math.pow(10, Math.ceil(Math.log10(maxYValue)));
		if(maxYValue >= oldMaxYValue
				|| elapsedTimeStepsSinceLastAdjustedYAxis == TIME_TO_ADJUST_AXES)
		{
			plot.setMaximumYValue(maxYValue);

			oldMaxYValue = maxYValue;
			elapsedTimeStepsSinceLastAdjustedYAxis = 0;

			// draw some extra points on the y-axis (looks good)
			if(maxYValue > 1)
			{
				int numberOfExtraYPoints = 9;
				double[] yValues = new double[numberOfExtraYPoints];
				for(int i = 0; i < yValues.length; i++)
				{
					yValues[i] = (i + 1.0) * ((double) maxYValue)
							/ (double) (numberOfExtraYPoints + 1);
				}
				plot.setExtraYAxisValues(yValues);
			}
			plot.showYValuesAsInts(true);
		}

		// specify colors for the points
		if(neighborhoodNumbers.length > 0)
		{
			Color stateColor = Color.BLACK;
			if(selectedState != ALL_NON_EMPTY_STATES_CHOICE
					&& selectedState != ALL_STATES_CHOICE)
			{
				int stateWeArePlotting = selectedState;
				if(selectedState == EMPTY_STATE_CHOICE)
				{
					stateWeArePlotting = 0;
				}

				if(IntegerCellStateView.isCurrentRuleCompatible()
						&& IntegerCellState.isCurrentRuleCompatible())
				{
					CellStateView view = Cell.getView();
					stateColor = view.getDisplayColor(new IntegerCellState(
							stateWeArePlotting), null, new Coordinate(0, 0));
				}
			}
			Color[] colorArray = new Color[neighborhoodNumbers.length];
			for(int point = 0; point < colorArray.length; point++)
			{
				colorArray[point] = stateColor;
			}
			plot.setPointDisplayColors(colorArray);
		}
		else
		{
			plot.setPointDisplayColorsToDefault();
		}

		// finally draw the plot
		plot.drawPoints(neighborhoodNumbers);
	}

	/**
	 * Plots the population data on a log-log plot.
	 */
	private void plotLogLogData()
	{
		// axes labels
		logPlot.setXAxisLabel("log(S)");
		logPlot.setYAxisLabel("log(N(S))");

		// the max x and y-values plotted
		double maxYValue = 1.0;
		double maxXValue = 1.0;

		// convert to a Point2D array
		LinkedList<Point2D.Double> logNumbers = new LinkedList<Point2D.Double>();
		for(int i = 0; i < numberOfNeighborhoodsOfEachSize.length; i++)
		{
			if(numberOfNeighborhoodsOfEachSize[i] > 0)
			{
				logNumbers.add(new Point2D.Double(Math.log(i + 1), Math
						.log(numberOfNeighborhoodsOfEachSize[i])));
			}

			// and keep track of the max value for plotting
			if(numberOfNeighborhoodsOfEachSize[i] > 0)
			{
				maxXValue = i + 1;
			}
			if(numberOfNeighborhoodsOfEachSize[i] > maxYValue)
			{
				maxYValue = numberOfNeighborhoodsOfEachSize[i];
			}
		}

		// round up the max values to the nearest exponent n (of 10^n)
		maxXValue = Math.ceil(Math.log(maxXValue));
		maxYValue = Math.ceil(Math.log(maxYValue));

		if(maxXValue == 0.0)
		{
			maxXValue = 1.0;
		}
		if(maxYValue == 0.0)
		{
			maxYValue = 1.0;
		}

		// make sure the list isn't empty
		if(logNumbers.size() == 0)
		{
			logNumbers.add(new Point2D.Double(0.0, 0.0));
			maxXValue = 1.0;
			maxYValue = 1.0;
		}

		// set the min and max x values on the plot
		elapsedTimeStepsSinceLastAdjustedLogXAxis++;
		logPlot.setMinimumXValue(0);
		if(maxXValue >= oldLogMaxXValue
				|| elapsedTimeStepsSinceLastAdjustedLogXAxis == TIME_TO_ADJUST_AXES)
		{
			logPlot.setMaximumXValue(maxXValue);

			oldLogMaxXValue = maxXValue;
			elapsedTimeStepsSinceLastAdjustedLogXAxis = 0;

			// draw some extra points on the x-axis (looks good)
			if(maxXValue > 20.0)
			{
				int numberOfExtraXPoints = 3;

				double[] logXValues = new double[numberOfExtraXPoints];
				for(int i = 0; i < logXValues.length; i++)
				{
					logXValues[i] = Math.ceil((i + 1.0) * ((double) maxXValue)
							/ (double) (numberOfExtraXPoints + 1));
				}
				logPlot.setExtraXAxisValues(logXValues);
				logPlot.showXValuesAsInts(true);
			}
			else if(maxXValue > 1.0)
			{
				int numberOfExtraXPoints = (int) (maxXValue - 1.0);

				double[] logXValues = new double[numberOfExtraXPoints];
				for(int i = 0; i < logXValues.length; i++)
				{
					logXValues[i] = i + 1;
				}
				logPlot.setExtraXAxisValues(logXValues);
				logPlot.showXValuesAsInts(true);
			}
			else
			{
				double[] logXValues = {0.2, 0.4, 0.6, 0.8};
				logPlot.setExtraXAxisValues(logXValues);
				logPlot.showXValuesAsInts(false);
			}
		}

		// set the min y value on the plot
		logPlot.setMinimumYValue(0);

		// set the max y value on the plot (to nearest 10^nth power)
		elapsedTimeStepsSinceLastAdjustedLogYAxis++;
		if(maxYValue >= oldLogMaxYValue
				|| elapsedTimeStepsSinceLastAdjustedLogYAxis == TIME_TO_ADJUST_AXES)
		{
			logPlot.setMaximumYValue(maxYValue);

			oldLogMaxYValue = maxYValue;
			elapsedTimeStepsSinceLastAdjustedLogYAxis = 0;

			// draw some extra points on the y-axis (looks good)
			if(maxYValue > 20.0)
			{
				int numberOfExtraYPoints = 3;

				double[] logYValues = new double[numberOfExtraYPoints];
				for(int i = 0; i < logYValues.length; i++)
				{
					logYValues[i] = Math.ceil((i + 1.0) * ((double) maxYValue)
							/ (double) (numberOfExtraYPoints + 1));
				}
				logPlot.setExtraYAxisValues(logYValues);
				logPlot.showYValuesAsInts(true);
			}
			if(maxYValue > 1.0)
			{
				int numberOfExtraYPoints = (int) (maxYValue - 1.0);

				double[] logYValues = new double[numberOfExtraYPoints];
				for(int i = 0; i < logYValues.length; i++)
				{
					logYValues[i] = i + 1;
				}
				logPlot.setExtraYAxisValues(logYValues);
				logPlot.showYValuesAsInts(true);
			}
			else if(maxYValue == 1.0)
			{
				double[] logYValues = {0.2, 0.4, 0.6, 0.8};
				logPlot.setExtraYAxisValues(logYValues);
				logPlot.showYValuesAsInts(false);
			}
		}

		// specify colors for the points
		Color stateColor = Color.BLACK;
		if(selectedState != ALL_NON_EMPTY_STATES_CHOICE
				&& selectedState != ALL_STATES_CHOICE)
		{
			int stateWeArePlotting = selectedState;
			if(selectedState == EMPTY_STATE_CHOICE)
			{
				stateWeArePlotting = 0;
			}

			if(IntegerCellStateView.isCurrentRuleCompatible()
					&& IntegerCellState.isCurrentRuleCompatible())
			{
				CellStateView view = Cell.getView();
				stateColor = view.getDisplayColor(new IntegerCellState(
						stateWeArePlotting), null, new Coordinate(0, 0));
			}
		}

		if(logNumbers.size() > 0)
		{
			Color[] colorArray = new Color[logNumbers.size()];
			for(int point = 0; point < colorArray.length; point++)
			{
				colorArray[point] = stateColor;
			}
			logPlot.setPointDisplayColors(colorArray);
		}
		else
		{
			logPlot.setPointDisplayColorsToDefault();
		}

		// finally draw the plot
		if(logNumbers.size() > 0)
		{
			logPlot.drawPoints(logNumbers);
		}
		else
		{
			logPlot.clearPlot();
		}
	}

	/**
	 * Saves the specified data to the file.
	 * 
	 * @param data
	 *            The data that will be saved.
	 */
	private void saveData(String[] data)
	{
		if(fileWriter != null)
		{
			try
			{
				fileWriter.writeData(data, delimiter);
			}
			catch(IOException e)
			{
				// Could not save the data, so close the file
				if(fileWriter != null)
				{
					fileWriter.close();
				}

				// and uncheck the "save data" box
				if(saveDataCheckBox != null)
				{
					saveDataCheckBox.setSelected(false);
				}
			}
		}
	}

	/**
	 * Called by the constructor.
	 */
	private void setUpAnalysis()
	{
		// update, because this could change
		numStates = CurrentProperties.getInstance().getNumStates();

		// get the current view
		String ruleClassName = CurrentProperties.getInstance()
				.getRuleClassName();
		rule = ReflectionTool.instantiateFullRuleFromClassName(ruleClassName);
		view = rule.getCompatibleCellStateView();

		// use all occupied states
		selectedState = ALL_STATES_CHOICE;

		if(colorPatch == null)
		{
			colorPatch = new ColorPatch();
		}
		else
		{
			colorPatch.setDefaultColorAndState();
		}
		colorPatch.repaint();

		// reset which state was last selected
		if(lastSelectedState >= numStates || lastSelectedState < 0)
		{
			lastSelectedState = ALL_STATES_CHOICE;
		}

		// this is the panel that will be displayed (getDisplayPanel() will
		// return the panel that this creates)
		if(displayPanel == null)
		{
			createDisplayPanel();
		}

		// select the "all non-empty states" radio button. This must happen
		// after the display panel is created.
		allStatesButton.setSelected(true);

		// disable the selectState button
		selectStateButton.setEnabled(false);
		colorPatch.setEnabled(false);
		colorPatch.setDefaultColorAndState();
		colorPatch.setToolTipText(null);
		colorPatch.repaint();

		// only integer based rules should be allowed to select a particular
		// state for analysis
		if(IntegerCellState.isCompatibleRule(rule))
		{
			// then let them select a particular state
			particularStateButton.setEnabled(true);
		}
		else
		{
			// don't let the user try to select a state. Won't work.
			particularStateButton.setEnabled(false);
		}
	}

	/**
	 * Counts and displays the number of occupied cells using hte algorithm
	 * outline in Appendix A of "Introduction to Percolation Theory, 2nd
	 * Edition" by Stauffer and Aharony.
	 * 
	 * @param lattice
	 *            The CA lattice.
	 * @param rule
	 *            The CA rule.
	 * @param generation
	 *            The current generation of the CA. There is no requirement that
	 *            this be the generation analyzed, but typically, this will be
	 *            the generation analyzed.
	 */
	protected void analyze(Lattice lattice, Rule rule, int generation)
	{
		// find out if we are dealing with integer cell states
		boolean isIntegerCellState = true;
		try
		{
			Cell c = (Cell) lattice.iterator().next();

			// this will fail if not an integer cell state
			IntegerCellState intCellState = (IntegerCellState) c
					.getState(generation);

			// update, because this could change
			numStates = CurrentProperties.getInstance().getNumStates();
		}
		catch(Exception e)
		{
			isIntegerCellState = false;
		}

		// true if we are looking for all cells rather than a
		// particular state
		boolean countingAllCells = false;
		if(selectedState == ALL_STATES_CHOICE)
		{
			countingAllCells = true;
		}

		// true if we are looking for all occupied cells rather than a
		// particular state
		boolean countingAllOccupiedCells = false;
		if(selectedState == ALL_NON_EMPTY_STATES_CHOICE)
		{
			countingAllOccupiedCells = true;
		}

		// true if the user has selected the empty cell states radio button
		// rather than a particular state
		boolean countingEmptyCells = false;
		if(selectedState == EMPTY_STATE_CHOICE)
		{
			countingEmptyCells = true;
		}

		// get an iterator over the lattice
		Iterator cellIterator = lattice.iterator();

		// create a hashtable to hold the neighborhood size for each cell (key)
		Hashtable<Cell, Integer> hashOfNeighborSizeValues = new Hashtable<Cell, Integer>();

		// the maximum sized neighborhood
		int maxNeighborhoodSize = 0;

		// go through the lattice and find each cell of the specified state
		Cell cell = null;
		while(cellIterator.hasNext())
		{
			// get the cell
			cell = (Cell) cellIterator.next();

			// the cell state
			CellState cellState = cell.getState(generation);

			// find out if the cell is in the state being counted in the
			// neighborhood sizes
			if(countingAllCells
					|| (countingEmptyCells && cellState.isEmpty())
					|| (countingAllOccupiedCells && !cellState.isEmpty())
					|| ((!countingEmptyCells && !countingAllOccupiedCells && !countingAllCells)
							&& isIntegerCellState && (cellState.toInt() == selectedState)))
			{
				// now get the number of neighbors
				Cell[] neighboringCells = lattice.getNeighbors(cell);
				int numNeighbors = neighboringCells.length;

				// keep track of the max neighborhood size
				if(numNeighbors > maxNeighborhoodSize)
				{
					maxNeighborhoodSize = numNeighbors;
				}

				// now save the cluster number
				hashOfNeighborSizeValues.put(cell, new Integer(numNeighbors));
			}
		}

		// now find the number of neighborhoods of each size
		if(maxNeighborhoodSize > 0)
		{
			// init the array
			numberOfNeighborhoodsOfEachSize = new int[maxNeighborhoodSize + 1];
			java.util.Arrays.fill(numberOfNeighborhoodsOfEachSize, 0);

			// fill the array with neighborhood sizes
			Collection<Integer> sizes = hashOfNeighborSizeValues.values();
			for(Integer i : sizes)
			{
				int size = i.intValue();
				numberOfNeighborhoodsOfEachSize[size]++;
			}
		}
		else
		{
			// it's a completely disconnected lattice -- highly unlikely
			numberOfNeighborhoodsOfEachSize = new int[1];
			numberOfNeighborhoodsOfEachSize[0] = 0;
		}

		// convert cluster sizes to a Point2D array for plotting
		convertNeighborhoodSizesToPointArray();

		// set the text for the labels
		generationDataLabel.setText("" + generation);

		// create an array of data to be saved
		data = new String[numberOfNeighborhoodsOfEachSize.length + 1];
		data[0] = "" + generation;
		for(int i = 0; i < numberOfNeighborhoodsOfEachSize.length; i++)
		{
			data[i + 1] = "" + numberOfNeighborhoodsOfEachSize[i];
		}

		// see if user wants to save the data
		if(fileWriter != null)
		{
			// save it
			saveData(data);
		}

		// plot the cluster data
		plotData();
		plotLogLogData();
	}

	/**
	 * Performs any desired operations when the analysis is stopped (closed) by
	 * the user. For example, you might write the results to a file at this
	 * time. Or you might dispose of any windows that you opened. May do
	 * nothing.
	 */
	protected void stopAnalysis()
	{
		// if the user has been saving data, then close that data file when the
		// analysis is stopped.
		if(fileWriter != null)
		{
			fileWriter.close();
		}
	}

	/**
	 * Reacts to the "save data" check box.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();

		if(command.equals(SAVE_DATA))
		{
			if(saveDataCheckBox.isSelected())
			{
				// they want to save data, so open a data file
				createFileWriter();
			}
			else
			{
				// They don't want to save data anymore, so close the file.
				// The synchronized keyword prevents accidental access elsewhere
				// in the code while the file is being closed. Otherwise, other
				// code might try to write to the file while it is being closed.
				synchronized(this)
				{
					if(fileWriter != null)
					{
						fileWriter.close();
						fileWriter = null;
					}
				}
			}
		}
		else if(command.equals(SELECT_STATE))
		{
			chooseAnalysisState();
		}
		else if(command.equals(PLOT_ZERO_VALUES))
		{
			plotZeroValues = plotZeroValueCheckBox.isSelected();

			// rerun so the changes are shown on the plot
			rerunAnalysis();
		}
	}

	/**
	 * A list of lattices with which this Analysis will work. In order for an
	 * analysis to display and be used, it must be compatible with both the
	 * lattice and the rule currently selected by a user (see
	 * getCompatibleRules).
	 * <p>
	 * Well-designed Analyses should work with any lattice, but some may require
	 * particular topological or geometrical information. Appropriate strings to
	 * return in the array include SquareLattice.DISPLAY_NAME,
	 * HexagonalLattice.DISPLAY_NAME,
	 * StandardOneDimensionalLattice.DISPLAY_NAME, etc. Return null if
	 * compatible with all lattices.
	 * 
	 * @return A list of lattices compatible with this Analysis (returns the
	 *         display names for the lattices). Returns null if compatible with
	 *         all lattices.
	 */
	public String[] getCompatibleLattices()
	{
		String[] lattices = null;
		return lattices;
	}

	/**
	 * A list of Rules with which this Analysis will work. In order for an
	 * analysis to display and be used, it must be compatible with both the
	 * lattice and the rule currently selected by a user (see
	 * getCompatibleLattices).
	 * <p>
	 * Well-designed Analyses should work with any rule, but some may require
	 * particular rule-specific information. Appropriate strings to return in
	 * the array include the display names for any rule: for example, "Life", or
	 * "Majority Rules". These names can be accessed from the getDisplayName()
	 * method of each rule. For example,
	 * 
	 * <pre>
	 * new Life(super.getProperties()).getDisplayName()
	 * </pre>
	 * 
	 * Return null if compatible with all lattices.
	 * 
	 * @return A list of lattices compatible with this Analysis (returns the
	 *         display names for the lattices). Returns null if compatible with
	 *         all lattices.
	 */
	public String[] getCompatibleRules()
	{
		String[] rules = null;
		return rules;
	}

	/**
	 * A brief one or two-word string describing the analysis, appropriate for
	 * display in a drop-down list.
	 * 
	 * @return A string no longer than 15 characters.
	 */
	public String getDisplayName()
	{
		return ANALYSIS_NAME;
	}

	/**
	 * Gets a JPanel that displays results of the population analysis.
	 * 
	 * @return A display for the population analysis results.
	 */
	public JPanel getDisplayPanel()
	{
		return displayPanel;
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
	 * Overrides the parent's method to handle the notification of a change in
	 * color, and is used to change the color of the colorPatch.
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		if(event.getPropertyName().equals(CurrentProperties.COLORS_CHANGED))
		{
			// only do this if the color patch is active
			if(colorPatch.isEnabled())
			{
				// get the current color and set the colorPatch to that color
				currentColor = view.getDisplayColor(new IntegerCellState(
						selectedState), null, new Coordinate(0, 0));
				if(colorPatch == null)
				{
					colorPatch = new ColorPatch(currentColor, selectedState);
				}
				else
				{
					colorPatch.setColorAndState(currentColor, selectedState);
				}
				colorPatch.setEnabled(true);
				colorPatch.repaint();
			}

			// replot in the new colors
			plotData();
			plotLogLogData();
		}
	}

	/**
	 * Performs any necessary operations to reset the analysis. this method is
	 * called if the user resets the cellular automata, or selects a new
	 * simulation.
	 */
	public void reset()
	{
		// if the user has been saving data, then close that data file
		synchronized(this)
		{
			if(fileWriter != null)
			{
				fileWriter.close();
				fileWriter = null;
			}
		}

		// and uncheck the "save data" box
		if(saveDataCheckBox != null)
		{
			saveDataCheckBox.setSelected(false);
		}

		// reset the analysis parameters
		setUpAnalysis();

		// force it to redraw
		displayPanel.invalidate();
	}

	/**
	 * If returns true, then the analysis is forced to size its width to fit
	 * within the visible width of the tabbed pane where it is displayed. If
	 * false, then a horizontal scroll bar is added so that the analysis can be
	 * wider than the displayed space.
	 * <p>
	 * Recommend returning true. If your graphics look lousy within that space,
	 * then return false. (In other words, try both and see which is better.)
	 * 
	 * @return true if the graphics should be forced to size its width to fit
	 *         the display area.
	 */
	public boolean restrictDisplayWidthToVisibleSpace()
	{
		return true;
	}

	/**
	 * A patch of color displayed on the JPanel.
	 */
	private class ColorPatch extends JPanel
	{
		// the color of the patch
		private Color colorOfPatch = null;

		private Color defaultColor = new Color(204, 204, 204);

		// the size of the patch
		private Dimension patchSize = new Dimension(25, 25);

		// the state value being displayed. Used when drawing the shape on the
		// patch
		private int stateValue = 0;

		/**
		 * Create the patch with a default color. Useful when the CA isn't an
		 * integer state CA.
		 */
		public ColorPatch()
		{
			this.setPreferredSize(patchSize);
			this.setBackground(defaultColor);
			colorOfPatch = defaultColor;
			this.setBorder(BorderFactory.createRaisedBevelBorder());
			this.setToolTipText(null);

			// should be the constant ALL_NON_EMPTY_STATES_CHOICE in this case.
			stateValue = selectedState;
		}

		/**
		 * Create the patch with the given color and state.
		 */
		public ColorPatch(Color color, int stateValue)
		{
			this.setPreferredSize(patchSize);
			this.setBorder(BorderFactory.createRaisedBevelBorder());

			setColorAndState(color, stateValue);
		}

		/**
		 * Set the color and state of the patch.
		 */
		public void setColorAndState(Color color, int stateValue)
		{
			this.stateValue = stateValue;

			// set a shape and background color
			Shape shape = view.getDisplayShape(
					new IntegerCellState(stateValue), this.getWidth(), this
							.getHeight(), null);
			if(shape == null)
			{
				this.setBackground(color);
			}
			else
			{
				// the color behind the shape
				this.setBackground(ColorScheme.DEFAULT_EMPTY_COLOR);
			}

			colorOfPatch = color;

			// set a tool tip that tells them the state of the cell this color
			// represents
			try
			{
				// first check if it is this special case -- not the best
				// connectivity, but useful for backwards compatibility
				FiniteObjectRuleTemplate theRule = (FiniteObjectRuleTemplate) rule;
				this.setToolTipText("cell state "
						+ theRule.intToObjectState(stateValue).toString());
			}
			catch(Exception e)
			{
				this.setToolTipText("cell state " + stateValue);
			}
		}

		/**
		 * Set a default color and state for the patch;
		 */
		public void setDefaultColorAndState()
		{
			stateValue = ALL_STATES_CHOICE;
			colorOfPatch = defaultColor;
			this.setBackground(defaultColor);
		}

		// draw the correct shape on the patch
		public void paintComponent(Graphics g)
		{
			// Call the JPanel's paintComponent. This ensures
			// that the background is properly rendered.
			super.paintComponent(g);

			if(IntegerCellState.isCompatibleRule(rule))
			{
				// exclude the border (otherwise the stroke of the border is
				// changed)
				Graphics2D g2 = (Graphics2D) g.create(this.getInsets().left,
						this.getInsets().right, this.getWidth()
								- this.getInsets().left
								- this.getInsets().right, this.getHeight()
								- this.getInsets().top
								- this.getInsets().bottom);

				try
				{
					Stroke stroke = view.getStroke(new IntegerCellState(
							stateValue), this.getWidth(), this.getHeight(),
							new Coordinate(0, 0));
					if(stroke != null)
					{
						g2.setStroke(stroke);
					}

					// use insets so fits in the space which is smaller due to
					// the raisedBevelBorder
					Shape shape = view.getDisplayShape(new IntegerCellState(
							stateValue), this.getWidth() - 2
							* this.getInsets().left - 2
							* this.getInsets().right, this.getHeight() - 2
							* this.getInsets().top - 2
							* this.getInsets().bottom, null);

					if(shape != null && colorOfPatch != null)
					{
						// translate the shape to the correct position
						AffineTransform scalingTransform = AffineTransform
								.getTranslateInstance(this.getWidth() / 2.0,
										this.getHeight() / 2.0);
						shape = scalingTransform.createTransformedShape(shape);

						// now draw it
						g2.setColor(colorOfPatch);
						g2.draw(shape);
						g2.fill(shape);
					}
				}
				catch(Exception e)
				{
					// fails if not an IntegerCellState -- do nothing
				}
			}
		}
	}

	/**
	 * Listens for the OK button on the integer color chooser.
	 */
	private class OkColorListener implements ActionListener
	{
		private String actionCommand = null;

		public OkColorListener(String actionCommand)
		{
			this.actionCommand = actionCommand;
		}

		public void actionPerformed(ActionEvent e)
		{
			selectedState = integerColorChooser.getState();
			currentColor = integerColorChooser.getColor();
			colorPatch.setColorAndState(currentColor, selectedState);
			colorPatch.repaint();

			// rerun so the changes are shown on the plot
			rerunAnalysis();
		}
	}

	/**
	 * Decides what to do when the user selects a the empty, non-empty, or
	 * particular states.
	 * 
	 * @author David Bahr
	 */
	private class StateChoiceListener implements ItemListener
	{
		public void itemStateChanged(ItemEvent event)
		{
			if(nonEmptyStatesButton.isSelected())
			{
				// save the last selected state (if it was a particular integer
				// state)
				if(selectedState != EMPTY_STATE_CHOICE
						&& selectedState != ALL_NON_EMPTY_STATES_CHOICE
						&& selectedState != ALL_STATES_CHOICE)
				{
					lastSelectedState = selectedState;
				}

				// use all states
				selectedState = ALL_NON_EMPTY_STATES_CHOICE;

				if(colorPatch == null)
				{
					colorPatch = new ColorPatch();
				}
				else
				{
					colorPatch.setDefaultColorAndState();
				}
				colorPatch.setEnabled(false);
				colorPatch.setToolTipText(null);
				colorPatch.repaint();

				if(selectStateButton != null)
				{
					selectStateButton.setEnabled(false);
				}
			}
			else if(emptyStateButton.isSelected())
			{
				// save the last selected state (if it was a particular integer
				// state)
				if(selectedState != EMPTY_STATE_CHOICE
						&& selectedState != ALL_NON_EMPTY_STATES_CHOICE
						&& selectedState != ALL_STATES_CHOICE)
				{
					lastSelectedState = selectedState;
				}

				// use only empty states
				selectedState = EMPTY_STATE_CHOICE;

				if(colorPatch == null)
				{
					colorPatch = new ColorPatch();
				}
				else
				{
					colorPatch.setDefaultColorAndState();
				}
				colorPatch.setEnabled(false);
				colorPatch.setToolTipText(null);
				colorPatch.repaint();

				if(selectStateButton != null)
				{
					selectStateButton.setEnabled(false);
				}
			}
			else if(allStatesButton.isSelected())
			{
				// save the last selected state (if it was a particular integer
				// state)
				if(selectedState != EMPTY_STATE_CHOICE
						&& selectedState != ALL_NON_EMPTY_STATES_CHOICE
						&& selectedState != ALL_STATES_CHOICE)
				{
					lastSelectedState = selectedState;
				}

				// use only empty states
				selectedState = ALL_STATES_CHOICE;

				if(colorPatch == null)
				{
					colorPatch = new ColorPatch();
				}
				else
				{
					colorPatch.setDefaultColorAndState();
				}
				colorPatch.setEnabled(false);
				colorPatch.setToolTipText(null);
				colorPatch.repaint();

				if(selectStateButton != null)
				{
					selectStateButton.setEnabled(false);
				}
			}
			else if(particularStateButton.isSelected())
			{
				// use the last selected state
				if(lastSelectedState != EMPTY_STATE_CHOICE
						&& lastSelectedState != ALL_NON_EMPTY_STATES_CHOICE
						&& lastSelectedState != ALL_STATES_CHOICE
						&& lastSelectedState < numStates)
				{
					selectedState = lastSelectedState;
				}
				else
				{
					selectedState = numStates - 1;
				}

				// get the current color and set the colorPatch to that color
				currentColor = view.getDisplayColor(new IntegerCellState(
						selectedState), null, new Coordinate(0, 0));
				if(colorPatch == null)
				{
					colorPatch = new ColorPatch(currentColor, selectedState);
				}
				else
				{
					colorPatch.setColorAndState(currentColor, selectedState);
				}
				colorPatch.setEnabled(true);
				colorPatch.repaint();

				if(selectStateButton != null)
				{
					selectStateButton.setEnabled(true);
				}
			}

			// adjust these so that the new plot has reasonable axes.
			elapsedTimeStepsSinceLastAdjustedXAxis = TIME_TO_ADJUST_AXES - 1;
			elapsedTimeStepsSinceLastAdjustedYAxis = TIME_TO_ADJUST_AXES - 1;

			// rerun so the changes are shown on the plot
			rerunAnalysis();
		}
	}
}
