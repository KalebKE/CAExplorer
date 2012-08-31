/*
 CellTrackerAnalysis -- a class within the Cellular Automaton Explorer. 
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
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cellularAutomata.CAController;
import cellularAutomata.CurrentProperties;
import cellularAutomata.Cell;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.OneDimensionalLattice;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;
import cellularAutomata.util.SimplePlot;
import cellularAutomata.util.files.FileWriter;

/**
 * Keeps track of the value at a particular site.
 * 
 * @author David Bahr
 */
public class CellTrackerAnalysis extends Analysis implements ActionListener
{
	// the maximum number of elements that will be plotted
	private static final int MAX_NUMBER_TO_PLOT = 100;

	// a brief display name for this class
	private static final String ANALYSIS_NAME = "Cell Tracker";

	// The tooltip for selecting the column of the tracked cell
	private static final String COL_TIP = "<html><body>The column of the cell "
			+ "that will be tracked. </body>.</html>";

	// The title for the panel containing the cell data
	private static final String DATA_PANEL_TITLE = "Cell data";

	// The pattern used to display decimals, particularly for the percent
	// population.
	private static final String DECIMAL_PATTERN = "0.000";

	// display info for this class
	private static final String INFO_MESSAGE = "Tracks the value of a particular "
			+ "cell. Also keeps a running average of the cell's value (over "
			+ MAX_NUMBER_TO_PLOT
			+ " generations). \n\n"
			+ "If the rule is not integer based, then this keeps track of whether "
			+ "the cell is occupied (plotted as a 1) or unoccupied (plotted as a 0).  "
			+ "In that case, the running average is a measure of the "
			+ "percentage of time that the cell has been occupied.";

	// button for resetting the analysis
	private static final String RESET_ANALYSIS = "Reset analysis";

	// tooltip for the button that resets the analysis
	private static final String RESET_ANALYSIS_TOOLTIP = "Clears the plots and "
			+ "restarts the running average.";

	// The title for the row and col spinner panel
	private static final String ROW_COL_PANEL_TITLE = "Select cell position";

	// The tooltip for selecting the row of the tracked cell
	private static final String ROW_TIP = "<html><body>The row of the cell "
			+ "that will be tracked. </body>.</html>";

	// the action command for saving the data and the label used by the "save
	// data" check box
	private static final String SAVE_DATA = "   Save the data";

	// a tooltip for the save data check box
	private static final String SAVE_DATA_TOOLTIP = "<html>Saves data "
			+ "to a file (saves <br> every generation while the box is checked).</html>";

	// a brief tooltip description for this class
	private static final String TOOLTIP = "<html><body>keep track of a cell's value "
			+ "over time</body></html>";

	// is true when the rule is integer based. Set by the constructor.
	private boolean isIntegerRule = true;

	// the cell being tracked
	private Cell cellBeingTracked = null;

	// color of titles of sections
	private Color titleColor = Color.BLUE;

	// If the user wants to save the data to a file, this will be instantiated
	private FileWriter fileWriter = null;

	// fonts for display
	private Fonts fonts = new Fonts();

	// title font (for titles of sections)
	private Font titleFont = fonts.getItalicSmallerFont();

	// the column of the cell being tracked
	private int col = 0;

	// height of the lattice (set in constructor)
	private int height = 2;

	// the number of states in the rule (set in the constructor)
	private int numberOfStates = 2;

	// the row of the cell being tracked
	private int row = 0;

	// width of the lattice (set in constructor)
	private int width = 2;

	// The button that resets the analysis
	private JButton resetButton = null;

	// The check box that lets the user save the data
	private JCheckBox saveDataCheckBox = null;

	// label for the current generation
	private JLabel generationDataLabel = null;

	// label for the cell's value
	private JLabel cellValueDataLabel = null;

	// label for the running average for the cell
	private JLabel runningAverageDataLabel = null;

	// the panel where results are displayed
	private JPanel displayPanel = null;

	// selects the column of the cell that will be tracked
	private JSpinner colSpinner = null;

	// selects the row of the cell that will be tracked
	private JSpinner rowSpinner = null;

	// the list of cell values that will be drawn on the plot
	private LinkedList<Point2D.Double> cellValueList = new LinkedList<Point2D.Double>();

	// the list of running average values that will be drawn on the plot
	private LinkedList<Point2D.Double> runningAverageList = new LinkedList<Point2D.Double>();

	// a panel that plots the cell's states
	private SimplePlot plot = null;

	// a panel that plots the running average of the cell's state
	private SimplePlot runningAveragePlot = null;

	// a delimiter for spacing data in the data file
	private String delimiter = null;

	private String[] data = new String[3];

	/**
	 * Create an analyzer that tracks the value of a particular cell.
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
	public CellTrackerAnalysis(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			setUpAnalysis();
		}
	}

	/**
	 * Create labels used to display the data for the cell's value.
	 */
	private void createDataDisplayLabels()
	{
		// if one is null, then they all are
		if(generationDataLabel == null)
		{
			generationDataLabel = new JLabel("");
			cellValueDataLabel = new JLabel("");
			runningAverageDataLabel = new JLabel("");
		}
	}

	/**
	 * Create the panel used to display the cell's value.
	 */
	private void createDisplayPanel()
	{
		// create the display panel
		displayPanel = new JPanel(new GridBagLayout());
		displayPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		displayPanel.setPreferredSize(new Dimension(
				CAFrame.tabbedPaneDimension.width, 800));

		// create a panel that displays messages
		JPanel messagePanel = createMessagePanel();

		// the position spinners
		JPanel spinnerPanel = createPositionSpinners();

		// the reset button
		JPanel resetPanel = createResetButton();

		// create the labels for the display
		createDataDisplayLabels();
		JLabel generationLabel = new JLabel("Generation:   ");
		JLabel cellValueLabel = new JLabel("Cell value:   ");
		JLabel runningAverageLabel = new JLabel("Running average:   ");

		// create boxes for each column of the display (a Box uses the
		// BoxLayout, so it is handy for laying out components)
		Box boxOfNameLabels = Box.createVerticalBox();
		Box boxOfDataLabels = Box.createVerticalBox();

		// the amount of space to put between components
		int verticalSpace = 5;
		int horizontalSpace = 10;

		// add the name labels to the first vertical box
		boxOfNameLabels.add(generationLabel);
		boxOfNameLabels.add(Box.createVerticalStrut(verticalSpace));
		boxOfNameLabels.add(cellValueLabel);
		boxOfNameLabels.add(Box.createVerticalStrut(verticalSpace));
		boxOfNameLabels.add(runningAverageLabel);

		// add the data labels to the second vertical box
		boxOfDataLabels.add(generationDataLabel);
		boxOfDataLabels.add(Box.createVerticalStrut(verticalSpace));
		boxOfDataLabels.add(cellValueDataLabel);
		boxOfDataLabels.add(Box.createVerticalStrut(verticalSpace));
		boxOfDataLabels.add(runningAverageDataLabel);

		// create another box that holds both of the label boxes
		Box boxOfLabels = Box.createHorizontalBox();
		boxOfLabels.add(boxOfNameLabels);
		boxOfDataLabels.add(Box.createHorizontalStrut(horizontalSpace));
		boxOfLabels.add(boxOfDataLabels);
		boxOfLabels.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));

		// create border for box
		JPanel borderedLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 5);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), DATA_PANEL_TITLE, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		Border compoundBorder = BorderFactory.createCompoundBorder(
				titledBorder, emptyBorder);
		borderedLabelPanel.setBorder(compoundBorder);
		borderedLabelPanel.add(boxOfLabels);

		// create a "save data" check box
		saveDataCheckBox = new JCheckBox(SAVE_DATA);
		saveDataCheckBox.setToolTipText(SAVE_DATA_TOOLTIP);
		saveDataCheckBox.setActionCommand(SAVE_DATA);
		saveDataCheckBox.addActionListener(this);
		JPanel saveDataPanel = new JPanel();
		saveDataPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
		saveDataPanel.add(saveDataCheckBox);

		// create panels that plots the data
		plot = new SimplePlot();
		runningAveragePlot = new SimplePlot();

		// add everything to the display
		int row = 0;
		displayPanel.add(messagePanel, new GBC(0, row).setSpan(5, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		displayPanel.add(plot, new GBC(0, row).setSpan(5, 1).setFill(GBC.BOTH)
				.setWeight(10.0, 10.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		displayPanel.add(runningAveragePlot, new GBC(0, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(10.0, 10.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		displayPanel.add(borderedLabelPanel, new GBC(0, row).setSpan(5, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		displayPanel.add(spinnerPanel, new GBC(0, row).setSpan(5, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		displayPanel.add(resetPanel, new GBC(0, row).setSpan(5, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
				.setInsets(1));

		row++;
		displayPanel.add(saveDataPanel, new GBC(0, row).setSpan(5, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
	}

	/**
	 * This uses a handy file writing utility to create a file writer.
	 */
	private void createFileWriter()
	{
		try
		{
			// This will prompt the user to enter a file. (The SAVE_DATA_PATH
			// parameter is just the default folder where the file chooser will
			// open.)
			fileWriter = new FileWriter(CurrentProperties.getInstance()
					.getSaveDataFilePath());

			// data delimiters (what string will be used to separate data in the
			// file)
			delimiter = CurrentProperties.getInstance().getDataDelimiters();

			// save a header
			String[] header = {"Generation: ", "Cell value: ",
					"Running average: "};
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
		AttentionPanel attentionPanel = new AttentionPanel("Cell Tracker");

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
	 * Create spinners for choosing the cell position.
	 * 
	 * @return panel containing spinners.
	 */
	private JPanel createPositionSpinners()
	{
		// create spinner for the col position
		SpinnerNumberModel colModel = new SpinnerNumberModel(1, 1, width, 1);
		colSpinner = new JSpinner(colModel);
		colSpinner.setToolTipText(COL_TIP);
		colSpinner.addChangeListener(new RowColListener());

		// create spinner for the col position
		SpinnerNumberModel rowModel = new SpinnerNumberModel(1, 1, height, 1);
		rowSpinner = new JSpinner(rowModel);
		rowSpinner.setToolTipText(ROW_TIP);
		rowSpinner.addChangeListener(new RowColListener());
		if(OneDimensionalLattice.isCurrentLatticeOneDim())
		{
			rowSpinner.setEnabled(false);
		}

		// create a label for the column position of the pinned cell
		JLabel colLabel = new JLabel("Col: ");
		colLabel.setFont(fonts.getPlainFont());

		// create a label for the row position of the pinned cell
		JLabel rowLabel = new JLabel("Row: ");
		rowLabel.setFont(fonts.getPlainFont());

		// panel to hold row spinner
		JPanel rowPanel = new JPanel(new FlowLayout());
		rowPanel.add(rowLabel);
		rowPanel.add(rowSpinner);

		// panel to hold col spinner
		JPanel colPanel = new JPanel(new FlowLayout());
		colPanel.add(colLabel);
		colPanel.add(colSpinner);

		// panel to hold both row and col spinners
		JPanel rowAndColPanel = new JPanel(new FlowLayout());
		rowAndColPanel.add(rowPanel);
		rowAndColPanel.add(colPanel);

		// add all components to a single panel
		JPanel selectCellPanel = new JPanel(new GridBagLayout());
		int row = 0;
		selectCellPanel.add(rowAndColPanel, new GBC(0, row).setSpan(2, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1, 1, 1, 1));

		// create border
		JPanel borderedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 5);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), ROW_COL_PANEL_TITLE, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		Border compoundBorder = BorderFactory.createCompoundBorder(
				titledBorder, emptyBorder);
		borderedPanel.setBorder(compoundBorder);
		borderedPanel.add(selectCellPanel);

		return borderedPanel;
	}

	/**
	 * Create reset button that clears the plots, empties the data, and restarts
	 * the running average.
	 * 
	 * @return panel containing reset button.
	 */
	private JPanel createResetButton()
	{
		// create button for resetting the analysis
		resetButton = new JButton(RESET_ANALYSIS);
		resetButton.setActionCommand(RESET_ANALYSIS);
		resetButton.setToolTipText(RESET_ANALYSIS_TOOLTIP);
		resetButton.addActionListener(this);

		// add all components to a single panel
		JPanel selectCellPanel = new JPanel(new GridBagLayout());
		int row = 0;
		selectCellPanel.add(resetButton, new GBC(0, row).setSpan(2, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.CENTER).setInsets(
				1, 1, 1, 1));

		return selectCellPanel;
	}

	/**
	 * Gets the cell being tracked.
	 */
	private Cell getCellBeingTracked(Lattice lattice)
	{
		Cell trackedCell = null;

		// get the tracked cell based on its row and col position
		Iterator iterator = lattice.iterator();
		for(int i = 0; i < (row * width) + col + 1; i++)
		{
			trackedCell = (Cell) iterator.next();
		}

		return trackedCell;
	}

	/**
	 * Gets the color from a state value, which may be fractionally between
	 * actual state values.
	 * 
	 * @param cellState
	 *            Fractional value between cell states.
	 * @param Rule
	 *            The CA rule.
	 * @return A color shaded between the nearest integer state values.
	 */
	private Color getColorFromFractionalStateValue(double cellState, Rule rule)
	{
		int roundUp = (int) Math.ceil(cellState);
		int roundDown = (int) Math.floor(cellState);

		CellStateView view = rule.getCompatibleCellStateView();

		Color upColor = null;
		Color downColor = null;
		if(isIntegerRule)
		{
			// may be between integer states, so get an inbetween color
			upColor = view.getDisplayColor(new IntegerCellState(roundUp), null,
					new Coordinate(0, 0));
			downColor = view.getDisplayColor(new IntegerCellState(roundDown),
					null, new Coordinate(0, 0));
		}
		else
		{
			// not an integer state
			CellState state = rule.getCompatibleCellState();
			state.setToFullState();
			upColor = view.getDisplayColor(state, null, new Coordinate(0, 0));

			if(roundUp != roundDown || cellState == 0.0)
			{
				state.setToEmptyState();
			}
			downColor = view.getDisplayColor(state, null, new Coordinate(0, 0));
		}

		// the fractional distance between the states
		double fractionalDistance = cellState - roundDown;

		// average color
		int redAverage = (int) Math.round(downColor.getRed()
				+ (upColor.getRed() - downColor.getRed()) * fractionalDistance);
		int greenAverage = (int) Math.round(downColor.getGreen()
				+ (upColor.getGreen() - downColor.getGreen())
				* fractionalDistance);
		int blueAverage = (int) Math.round(downColor.getBlue()
				+ (upColor.getBlue() - downColor.getBlue())
				* fractionalDistance);

		Color stateColor = new Color(redAverage, greenAverage, blueAverage);

		return stateColor;
	}

	/**
	 * Resets the plots, data, and the running average but doesn't reset the
	 * saved data.
	 */
	private void partialReset()
	{
		// force the analysis to change the cell being tracked (and by setting
		// null, the analyze method will reset the plots)
		cellBeingTracked.setTagged(false, this);
		cellBeingTracked = null;
		rerunAnalysis();
		refreshGraphics();
	}

	/**
	 * Plots the population data.
	 */
	private void plotData()
	{
		// set the min and max values on the plot
		Point2D firstPoint = (Point2D) cellValueList.getFirst();
		plot.setMaximumXValue(firstPoint.getX() + MAX_NUMBER_TO_PLOT - 1);
		plot.setMaximumYValue(numberOfStates - 1);
		plot.setMinimumXValue(firstPoint.getX());
		plot.setMinimumYValue(0.0);
		plot.setXAxisLabel("generation");
		plot.setYAxisLabel("cell value");
		plot.showPlotLines(false);

		// draw some extra points on the y-axis (looks good)
		if(numberOfStates <= 10 && numberOfStates > 2)
		{
			double[] yValues = new double[numberOfStates - 2];
			for(int i = 0; i < yValues.length; i++)
			{
				yValues[i] = i + 1;
			}
			plot.setExtraYAxisValues(yValues);
		}
		else if(numberOfStates != 2)
		{
			int numberOfExtraPoints = 4;
			double increment = (double) (numberOfStates - 1)
					/ (double) (numberOfExtraPoints + 1);
			double[] yValues = new double[numberOfExtraPoints];
			for(int i = 0; i < yValues.length; i++)
			{
				yValues[i] = Math.round(increment * (i + 1));
			}
			plot.setExtraYAxisValues(yValues);
		}
		plot.showYValuesAsInts(true);

		// and we need colors for each point
		Color[] colorArray = new Color[cellValueList.size()];
		String ruleClassName = CurrentProperties.getInstance()
				.getRuleClassName();
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(ruleClassName);

		int position = 0;
		Iterator<Point2D.Double> iterator = cellValueList.iterator();
		while(iterator.hasNext())
		{
			// get the state value
			double cellState = iterator.next().y;

			Color stateColor = getColorFromFractionalStateValue(cellState, rule);

			// assign the average color
			colorArray[position] = stateColor;

			// go to the next array element
			position++;
		}
		plot.setPointDisplayColors(colorArray);

		// and plot
		plot.drawPoints(cellValueList);
	}

	/**
	 * Plots the population data.
	 */
	private void plotRunningAverage()
	{
		// set the min and max values on the plot
		Point2D firstPoint = (Point2D) runningAverageList.getFirst();
		runningAveragePlot.setMaximumXValue(firstPoint.getX()
				+ MAX_NUMBER_TO_PLOT - 1);
		runningAveragePlot.setMaximumYValue(numberOfStates - 1);
		runningAveragePlot.setMinimumXValue(firstPoint.getX());
		runningAveragePlot.setMinimumYValue(0.0);
		runningAveragePlot.setXAxisLabel("generation");
		runningAveragePlot.setYAxisLabel("running avg");
		runningAveragePlot.showPlotLines(false);

		// draw some extra points on the y-axis (looks good)
		if(numberOfStates <= 10 && numberOfStates > 2)
		{
			double[] yValues = new double[numberOfStates - 2];
			for(int i = 0; i < yValues.length; i++)
			{
				yValues[i] = i + 1;
			}
			runningAveragePlot.setExtraYAxisValues(yValues);
		}
		else if(numberOfStates != 2)
		{
			int numberOfExtraPoints = 4;
			double increment = (double) (numberOfStates - 1)
					/ (double) (numberOfExtraPoints + 1);
			double[] yValues = new double[numberOfExtraPoints];
			for(int i = 0; i < yValues.length; i++)
			{
				yValues[i] = Math.round(increment * (i + 1));
			}
			runningAveragePlot.setExtraYAxisValues(yValues);
		}
		runningAveragePlot.showYValuesAsInts(true);

		// and we need colors for each point
		Color[] colorArray = new Color[runningAverageList.size()];
		String ruleClassName = CurrentProperties.getInstance()
				.getRuleClassName();
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(ruleClassName);

		int position = 0;
		Iterator<Point2D.Double> iterator = runningAverageList.iterator();
		while(iterator.hasNext())
		{
			// get the state value
			double cellState = iterator.next().y;

			// assign the average color
			colorArray[position] = getColorFromFractionalStateValue(cellState,
					rule);

			// go to the next array element
			position++;
		}
		runningAveragePlot.setPointDisplayColors(colorArray);

		// and plot
		runningAveragePlot.drawPoints(runningAverageList);
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
	 * Instantiates the analysis.
	 */
	private void setUpAnalysis()
	{
		String ruleDescription = Rule.getCurrentRuleDisplayName();
		isIntegerRule = IntegerCellState.isCompatibleRule(ruleDescription);

		if(isIntegerRule)
		{
			numberOfStates = CurrentProperties.getInstance().getNumStates();
		}
		else
		{
			numberOfStates = 2;
		}

		height = CurrentProperties.getInstance().getNumRows();
		width = CurrentProperties.getInstance().getNumColumns();

		// reset the row and col
		if(row >= height)
		{
			row = height - 1;
		}
		if(col >= width)
		{
			col = width - 1;
		}

		if(rowSpinner != null)
		{
			SpinnerNumberModel rowModel = new SpinnerNumberModel(row + 1, 1,
					height, 1);
			rowSpinner.setModel(rowModel);
			if(OneDimensionalLattice.isCurrentLatticeOneDim())
			{
				rowSpinner.setEnabled(false);
			}

			SpinnerNumberModel colModel = new SpinnerNumberModel(col + 1, 1,
					width, 1);
			colSpinner.setModel(colModel);

			rowSpinner.invalidate();
			colSpinner.invalidate();
		}

		// this is the panel that will be displayed (getDisplayPanel() will
		// return the panel that this creates)
		if(displayPanel == null)
		{
			createDisplayPanel();
		}
	}

	/**
	 * Counts and displays the number of occupied cells.
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
		// get the cell being analyzed
		if(cellBeingTracked == null)
		{
			// reset the plots. BTW, can't do this anywhere else or might reset
			// while they are being used. For example, doing this in the
			// RowColListener thread could clear these lists while they are
			// being plotted, causing NoSuchElement exceptions, etc.
			cellValueList.clear();
			runningAverageList.clear();

			cellBeingTracked = getCellBeingTracked(lattice);

			// tag the cell
			cellBeingTracked.setTagged(true, this);

			// and refresh graphics so shows tagged cell
			refreshGraphics();
		}

		// get the cell's state
		CellState state = cellBeingTracked.getState(generation);

		// convert to an integer
		int cellState = 0;
		if(isIntegerRule)
		{
			cellState = state.toInt();
		}
		else
		{
			// is the state "occupied"?
			if(!state.isEmpty())
			{
				cellState = 1;
			}
		}

		// save cell's value in a linked list for plotting
		cellValueList.add(new Point2D.Double(generation, cellState));
		if(cellValueList.size() > MAX_NUMBER_TO_PLOT)
		{
			cellValueList.removeFirst();
		}

		// calculate the running average
		Iterator<Point2D.Double> iterator = cellValueList.iterator();
		double runningAverage = 0.0;
		while(iterator.hasNext())
		{
			runningAverage += iterator.next().y;
		}
		runningAverage /= (double) cellValueList.size();

		// save the running average in a linked list for plotting
		runningAverageList.add(new Point2D.Double(generation, runningAverage));
		if(runningAverageList.size() > MAX_NUMBER_TO_PLOT)
		{
			runningAverageList.removeFirst();
		}

		// set the text for the labels
		generationDataLabel.setText("" + generation);
		cellValueDataLabel.setText("" + cellState);

		// and set the text for the running average label, but format!
		DecimalFormat myFormatter = new DecimalFormat(DECIMAL_PATTERN);
		String output = myFormatter.format(runningAverage);
		runningAverageDataLabel.setText(output);

		// create an array of data to be saved
		data[0] = "" + generation;
		data[1] = "" + cellState;
		data[2] = "" + runningAverage;

		// see if user wants to save the data
		if(fileWriter != null)
		{
			// save it
			saveData(data);
		}

		try
		{
			// plot the cell's data
			plotData();
		}
		catch(Exception e)
		{
			System.out.println("CellTrackerAnaly: plotData: e = " + e);
		}
		try
		{
			plotRunningAverage();
		}
		catch(Exception e)
		{
			System.out.println("CellTrackerAnaly: plotRunningAvg: e = " + e);
		}
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

		// untag the tracked cell
		if(cellBeingTracked != null)
		{
			cellBeingTracked.setTagged(false, this);

			// and refresh the graphics so don't show the cell as tagged anymore
			refreshGraphics();
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
		else if(command.equals(RESET_ANALYSIS))
		{
			partialReset();
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
	 * color.
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		if(event.getPropertyName().equals(CurrentProperties.COLORS_CHANGED))
		{
			// replot in the new colors
			plotData();
			plotRunningAverage();
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

		// untag and remove the tracked cell
		if(cellBeingTracked != null)
		{
			cellBeingTracked.setTagged(false, this);
			cellBeingTracked = null;
		}

		// empty the plot lists (so that old data doesn't get plotted again when
		// the new simulation starts)
		cellValueList.clear();
		runningAverageList.clear();

		// effectively re-instantiate (everything **BUT** the display panel)
		setUpAnalysis();
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
	 * listens for changes to the row spinner.
	 */
	private class RowColListener implements ChangeListener
	{
		/**
		 * Listens for changes to the row and col spinners.
		 * 
		 * @param e
		 */
		public void stateChanged(ChangeEvent e)
		{
			// note the -1 because the spinners start at position 1 not position
			// 0.
			col = ((Integer) ((SpinnerNumberModel) colSpinner.getModel())
					.getNumber()).intValue() - 1;

			if(rowSpinner.isEnabled())
			{
				row = ((Integer) ((SpinnerNumberModel) rowSpinner.getModel())
						.getNumber()).intValue() - 1;
			}
			else
			{
				row = 0;
			}

			// force the analysis to change the cell being tracked and to
			// reset the plots
			partialReset();
		}
	}
}