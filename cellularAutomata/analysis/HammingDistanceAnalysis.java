/*
 HammingDistanceAnalysis -- a class within the Cellular Automaton Explorer. 
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;
import cellularAutomata.util.SimplePlot;
import cellularAutomata.util.files.FileWriter;

/**
 * Finds the Hamming distance between the initial and current state. Also finds
 * the Hamming distance between the previous and current state.
 * 
 * @author David Bahr
 */
public class HammingDistanceAnalysis extends Analysis implements ActionListener
{
	// TODO: add button for current state (versus initial state)

	// the maximum number of elements that will be plotted
	private static final int MAX_NUMBER_TO_PLOT = 201;

	// a brief display name for this class
	private static final String ANALYSIS_NAME = "Hamming Distance";

	// title for the subpanel that displays the data
	private static final String DATA_PANEL_TITLE = "Data";

	// display info for this class
	private static final String INFO_MESSAGE = "The Hamming distance is the number of cells "
			+ "at a given generation that have different values from some \"previous\" reference "
			+ "generation. For example, if the initial generation is 0001000 and the next "
			+ "generation is 0011100 then the Hamming distance is 2. "
			+ "\n\n"
			+ "In this analysis, the "
			+ "the reference can be (1) fixed at a certain (current) generation or (2) changed "
			+ "at each time step so that it is always the preceding (previous) generation. "
			+ "\n\n"
			+ "Many CA will exhibit trends in Hamming distance, "
			+ "either on a regular plot or on a log-log plot. If the log-log plot shows a "
			+ "linear trend, then the Hamming distance is a power law function of the form "
			+ "distance = c * generation^exponent.  The slope of the line gives the exponent. "
			+ "\n\n"
			+ "This analysis works best for numerical-valued rules.  The \"difference\" "
			+ "between cell values may be poorly defined for some non-numerical rules. "
			+ "\n\n"
			+ "This analysis ignores any running averages (which only affects the display) "
			+ "and plots the Hamming distance between the non-averaged values.  Rules with "
			+ "predefined averaging (such as \"Electric Loops\") will appear to give odd "
			+ "results unless this is kept in mind.";

	// title for the subpanel that lets the user select the initial state or
	// previous state
	private static final String RADIO_BUTTON_PANEL_TITLE = "Compare future generations to";

	// the action command for saving the data and the label used by the "save
	// data" check box
	private static final String SAVE_DATA = "   Save the data";

	// a tooltip for the save data check box
	private static final String SAVE_DATA_TOOLTIP = "<html>Saves Hamming distance data "
			+ "to a file (saves <br> every generation while the box is checked).</html>";

	// a brief tooltip description for this class
	private static final String TOOLTIP = "<html><body>calculates the Hamming distance "
			+ "(number of cells that differ).</body></html>";

	// updates the cells to which the current generation will be compared
	private boolean updateComparisonState = true;

	// color of titles of sections
	private Color titleColor = Color.BLUE;

	// the number of cells in the lattice
	private int numberOfCells = 2;

	// the cell states used for comparison when calculating the Hamming
	// distance
	private LinkedList<CellState> comparisonStates = new LinkedList<CellState>();

	// the Hamming distance at each time step. Listed as a point (time,
	// distance).
	private LinkedList<Point2D.Double> hammingDistanceList = new LinkedList<Point2D.Double>();

	// If the user wants to save the data to a file, this will be instantiated
	private FileWriter fileWriter = null;

	// fonts for display
	private Fonts fonts = new Fonts();

	// title font (for titles of sections)
	private Font titleFont = new Fonts().getItalicSmallerFont();

	// The check box that lets the user save the data
	private JCheckBox saveDataCheckBox = null;

	// label for the current generation
	private JLabel generationDataLabel = null;

	// label for the Hamming distance
	private JLabel hammingDataLabel = null;

	// the panel where results are displayed
	private JPanel displayPanel = null;

	// radio button for choosing the initial state (that will be used to
	// calculate the Hamming distance)
	private JRadioButton currentStateButton = null;

	// radio button for choosing the previous states (that will be used to
	// calculate the Hamming distance)
	private JRadioButton previousStateButton = null;

	// a panel that plots the cluster data on log-log axes
	private SimplePlot logPlot = null;

	// a panel that plots the cluster data
	private SimplePlot plot = null;

	// a delimiter for spacing data in the data file
	private String delimiter = null;

	// data that will be saved to a file
	private String[] data = new String[2];

	/**
	 * Create an analyzer that finds Hamming distances.
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
	public HammingDistanceAnalysis(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			setUpAnalysis();
		}
	}

	/**
	 * Creates radio buttons to choose which state(s) will be used to calculate
	 * the fractal dimension.
	 */
	private JPanel createComparisonStateRadioButtonPanel()
	{
		currentStateButton = new JRadioButton(
				"the current generation (at time selected)");
		currentStateButton.setFont(fonts.getPlainFont());
		currentStateButton.addItemListener(new StateChoiceListener());
		currentStateButton.setSelected(true);

		previousStateButton = new JRadioButton("their previous generation");
		previousStateButton.setFont(fonts.getPlainFont());
		previousStateButton.addItemListener(new StateChoiceListener());
		previousStateButton.setSelected(false);

		// put them in a group so that they behave as radio buttons
		ButtonGroup group = new ButtonGroup();
		group.add(currentStateButton);
		group.add(previousStateButton);

		// create boxes for each column of the display (a Box uses the
		// BoxLayout, so it is handy for laying out components)
		Box boxOfRadioButtons = Box.createVerticalBox();

		// the amount of vertical and horizontal space to put between components
		int verticalSpace = 5;

		// add the radio buttons to the first vertical box
		boxOfRadioButtons.add(currentStateButton);
		boxOfRadioButtons.add(Box.createVerticalStrut(verticalSpace));
		boxOfRadioButtons.add(previousStateButton);

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
		radioPanel.add(boxOfRadioButtons);

		return radioPanel;
	}

	/**
	 * Create labels used to display the data for the statistics.
	 */
	private void createDataDisplayLabels()
	{
		// if one is null, then they all are
		if(generationDataLabel == null)
		{
			generationDataLabel = new JLabel("");
			hammingDataLabel = new JLabel("");
		}
	}

	/**
	 * Create the panel used to display the statistics.
	 */
	private void createDisplayPanel()
	{
		int displayWidth = CAFrame.tabbedPaneDimension.width;
		int displayHeight = 1080;

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

		// create a panel that holds the radio buttons for selecting the
		// comparison state
		JPanel comparisonStateSelectionPanel = createComparisonStateRadioButtonPanel();

		// create the labels for the display
		createDataDisplayLabels();
		JLabel generationLabel = new JLabel("Generation:   ");
		JLabel hammingDistanceLabel = new JLabel("Hamming distance:   ");

		// create boxes for each column of the display (a Box uses the
		// BoxLayout, so it is handy for laying out components)
		Box boxOfNameLabels = Box.createVerticalBox();
		Box boxOfDataLabels = Box.createVerticalBox();

		// the amount of vertical space to put between components
		int verticalSpace = 5;

		// add the name labels to the first vertical box
		boxOfNameLabels.add(generationLabel);
		boxOfNameLabels.add(Box.createVerticalStrut(verticalSpace));
		boxOfNameLabels.add(hammingDistanceLabel);

		// add the data labels to the second vertical box
		boxOfDataLabels.add(generationDataLabel);
		boxOfDataLabels.add(Box.createVerticalStrut(verticalSpace));
		boxOfDataLabels.add(hammingDataLabel);

		// create another box that holds both of the label boxes
		Box boxOfLabels = Box.createHorizontalBox();
		boxOfLabels.add(boxOfNameLabels);
		boxOfLabels.add(boxOfDataLabels);
		boxOfLabels.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createCompoundBorder(BorderFactory.createEtchedBorder(),
						BorderFactory.createEmptyBorder(0, 7, 7, 7)),
				DATA_PANEL_TITLE, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor));

		// create a "save data" check box
		saveDataCheckBox = new JCheckBox(SAVE_DATA);
		saveDataCheckBox.setToolTipText(SAVE_DATA_TOOLTIP);
		saveDataCheckBox.setActionCommand(SAVE_DATA);
		saveDataCheckBox.addActionListener(this);
		JPanel saveDataPanel = new JPanel(new BorderLayout());
		saveDataPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
		saveDataPanel.add(BorderLayout.CENTER, saveDataCheckBox);

		// create panels that plot the data
		plot = new SimplePlot();
		logPlot = new SimplePlot();

		// add all the components to the panel
		int row = 0;
		displayPanel.add(messagePanel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		displayPanel.add(plot, new GBC(1, row).setSpan(4, 1).setFill(GBC.BOTH)
				.setWeight(10.0, 10.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		displayPanel.add(logPlot, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(10.0, 10.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		displayPanel.add(boxOfLabels, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		displayPanel.add(comparisonStateSelectionPanel, new GBC(1, row)
				.setSpan(4, 1).setFill(GBC.HORIZONTAL).setWeight(1.0, 1.0)
				.setAnchor(GBC.WEST).setInsets(1));

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
			// This will prompt the user to enter a file. (The SAVE_DATA_PATH
			// parameter is just the default folder where the file chooser will
			// open.)
			fileWriter = new FileWriter(CurrentProperties.getInstance()
					.getSaveDataFilePath());

			// data delimiters (what string will be used to separate data in the
			// file)
			delimiter = CurrentProperties.getInstance().getDataDelimiters();

			// save a header
			String[] header = new String[2];
			header[0] = "Generation:";
			header[1] = "Hamming distance:";
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
			JOptionPane.showMessageDialog(null, message,
					"Valid file not selected", JOptionPane.INFORMATION_MESSAGE);
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
		AttentionPanel attentionPanel = new AttentionPanel("Hamming Distance");

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
	 * Gets a list of states to which the current states will be compared.
	 * 
	 * @param lattice
	 *            The CA Lattice.
	 * @return The list of states to which the Hamming distance of the current
	 *         states wiull be calculated.
	 */
	private Iterator<CellState> getComparisonStatesIterator(Lattice lattice)
	{
		// get the list of comparison states
		if(previousStateButton.isSelected())
		{
			// empty the comparison states
			comparisonStates.clear();

			// reset the number of cells
			numberOfCells = 0;

			Iterator iterator = lattice.iterator();

			// get the previous state of each cell
			while(iterator.hasNext())
			{
				Cell cell = ((Cell) iterator.next());
				CellState state = cell.getPreviousState();

				// previous state will not yet exist at generation = 0
				if(state == null)
				{
					// so replace with the current state
					state = cell.getState();
				}

				comparisonStates.add(state);

				// another cell will be compared
				numberOfCells++;
			}
		}
		else if(currentStateButton.isSelected() && updateComparisonState)
		{
			// empty the comparison states
			comparisonStates.clear();

			// reset the number of cells
			numberOfCells = 0;

			Iterator iterator = lattice.iterator();

			// get the previous state of each cell
			while(iterator.hasNext())
			{
				CellState state = ((Cell) iterator.next()).getState();

				comparisonStates.add(state);

				// another cell will be compared
				numberOfCells++;
			}
		}

		// we just updated the comparison states, so don't do it again unlesss
		// necessary
		updateComparisonState = false;

		return comparisonStates.iterator();
	}

	/**
	 * Plots the Hamming distance data on a regular plot.
	 */
	private void plotData()
	{
		// axes labels
		plot.setXAxisLabel("generation");
		plot.setYAxisLabel("Hamming distance");

		// the max x and y-values plotted
		Point2D firstPoint = (Point2D) hammingDistanceList.getFirst();
		plot.setMaximumXValue(firstPoint.getX() + MAX_NUMBER_TO_PLOT - 1);
		plot.setMaximumYValue(numberOfCells);
		plot.setMinimumXValue(firstPoint.getX());
		plot.setMinimumYValue(0.0);

		// show extra y-axis values
		if(numberOfCells > 9.0)
		{
			int numExtraValues = 4;
			double[] yValues = new double[numExtraValues];
			for(int i = 0; i < yValues.length; i++)
			{
				yValues[i] = (i + 1) * (numberOfCells / (numExtraValues + 1));
			}
			plot.setExtraYAxisValues(yValues);
		}
		else
		{
			int numExtraValues = numberOfCells - 1;
			double[] yValues = new double[numExtraValues];
			for(int i = 0; i < yValues.length; i++)
			{
				yValues[i] = (i + 1);
			}
			plot.setExtraYAxisValues(yValues);
		}

		plot.setPointDisplayColorsToDefault();

		// finally draw the plot
		plot.drawPoints(hammingDistanceList);
	}

	/**
	 * Plots the population data on a log-log plot.
	 */
	private void plotLogLogData()
	{
		// axes labels
		logPlot.setXAxisLabel("log(generation)");
		logPlot.setYAxisLabel("log(distance)");

		// convert to a Point2D array
		LinkedList<Point2D.Double> logNumbers = new LinkedList<Point2D.Double>();
		Iterator<Point2D.Double> iterator = hammingDistanceList.iterator();
		while(iterator.hasNext())
		{
			Point2D.Double point = iterator.next();
			if((point.getX() > 0.0) && (point.getY() > 0.0))
			{
				logNumbers.add(new Point2D.Double(Math.log(point.getX()), Math
						.log(point.getY())));
			}
		}

		// make sure the list isn't empty
		if(logNumbers.size() == 0)
		{
			logNumbers.add(new Point2D.Double(0.0, 0.0));
		}

		// the max and min x and y-values plotted
		double minXValue = logNumbers.getFirst().getX();
		double maxXValue = Math.max(Math.log(MAX_NUMBER_TO_PLOT), logNumbers
				.getLast().getX());
		double minYValue = 0.0;
		double maxYValue = Math.log(numberOfCells);

		// round the max and min values to the nearest exponent n (of 10^n),
		// with some number of decimal places
		minXValue = Math.floor(10000.0 * minXValue) / 10000.0;
		maxXValue = Math.ceil(10000.0 * maxXValue) / 10000.0;
		maxYValue = Math.ceil(10000.0 * maxYValue) / 10000.0;

		if(maxXValue == 0.0)
		{
			maxXValue = 1.0;
		}
		if(maxYValue == 0.0)
		{
			maxYValue = 1.0;
		}

		// set the max and min x and y values on the plot
		logPlot.setMinimumXValue(minXValue);
		logPlot.setMaximumXValue(maxXValue);
		logPlot.setMinimumYValue(minYValue);
		logPlot.setMaximumYValue(maxYValue);

		// draw some extra points on the x-axis (looks good)
		if(maxXValue > 1.0)
		{
			int numberOfExtraXPoints = 2;

			double[] logXValues = new double[numberOfExtraXPoints];
			for(int i = 0; i < logXValues.length; i++)
			{
				logXValues[i] = minXValue
						+ ((i + 1.0)
								* ((double) maxXValue - (double) minXValue) / (double) (numberOfExtraXPoints + 1));

				// and round to three decimal places
				logXValues[i] = Math.round(10000.0 * logXValues[i]) / 10000.0;

				if(Math.round(10000.0 * logXValues[i]) / 10000.0 > maxXValue)
				{
					logXValues[i] = Math.floor(10000.0 * logXValues[i]) / 10000.0;
				}
				else if(Math.round(10000.0 * logXValues[i]) / 10000.0 < minXValue)
				{
					logXValues[i] = Math.ceil(10000.0 * logXValues[i]) / 10000.0;
				}
				else
				{
					logXValues[i] = Math.round(10000.0 * logXValues[i]) / 10000.0;
				}
			}
			logPlot.setExtraXAxisValues(logXValues);
			logPlot.showXValuesAsInts(false);
		}
		else
		{
			double[] logXValues = {0.2, 0.4, 0.6, 0.8};
			logPlot.setExtraXAxisValues(logXValues);
			logPlot.showXValuesAsInts(false);
		}

		// draw some extra points on the y-axis (looks good)
		if(maxYValue > 1.0)
		{
			int numberOfExtraYPoints = 3;

			double[] logYValues = new double[numberOfExtraYPoints];
			for(int i = 0; i < logYValues.length; i++)
			{
				logYValues[i] = minYValue
						+ ((i + 1.0)
								* ((double) maxYValue - (double) minYValue) / (double) (numberOfExtraYPoints + 1));

				// and round to two decimal places
				logYValues[i] = Math.round(100.0 * logYValues[i]) / 100.0;
			}
			logPlot.setExtraYAxisValues(logYValues);
			logPlot.showXValuesAsInts(false);
		}
		else
		{
			double[] logYValues = {0.2, 0.4, 0.6, 0.8};
			logPlot.setExtraYAxisValues(logYValues);
			logPlot.showYValuesAsInts(false);
		}

		plot.setPointDisplayColorsToDefault();

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

	// private void plotLogLogData()
	// {
	// // axes labels
	// logPlot.setXAxisLabel("log(time)");
	// logPlot.setYAxisLabel("log(distance)");
	//
	// // convert to a Point2D array
	// LinkedList<Point2D.Double> logNumbers = new LinkedList<Point2D.Double>();
	// Iterator<Point2D.Double> iterator = hammingDistanceList.iterator();
	// int xValue = 0;
	// while(iterator.hasNext())
	// {
	// // instead of using the generation for the xValue, we just use
	// // successive points
	// xValue++;
	//
	// Point2D.Double point = iterator.next();
	// if(point.getY() > 0.0)
	// {
	// logNumbers.add(new Point2D.Double(Math.log(xValue), Math
	// .log(point.getY())));
	// }
	// }
	//
	// // the max and min x and y-values plotted
	// logPlot.setMinimumXValue(0);
	// logPlot.setMinimumYValue(0.0);
	//
	// double maxXValue = MAX_NUMBER_TO_PLOT - 1;
	// double maxYValue = numberOfCells;
	//
	// // round the max x-value to the nearest exponent n (of 10^n), with
	// // two decimal places
	// maxXValue = Math.log(maxXValue); // Math.ceil(Math.log(maxXValue));
	// maxXValue = Math.ceil(100.0 * maxXValue) / 100.0;
	//
	// // round up the max y-value to the nearest exponent n (of 10^n)
	// maxYValue = Math.ceil(Math.log(maxYValue));
	//
	// if(maxXValue == 0.0)
	// {
	// maxXValue = 1.0;
	// }
	// if(maxYValue == 0.0)
	// {
	// maxYValue = 1.0;
	// }
	//
	// // make sure the list isn't empty
	// if(logNumbers.size() == 0)
	// {
	// logNumbers.add(new Point2D.Double(0.0, 0.0));
	// }
	//
	// // set the max y value on the plot (to nearest 10^nth power)
	// logPlot.setMaximumXValue(maxXValue);
	//
	// // draw some extra points on the x-axis (looks good)
	// if(maxXValue > 20.0)
	// {
	// int numberOfExtraXPoints = 3;
	//
	// double[] logXValues = new double[numberOfExtraXPoints];
	// for(int i = 0; i < logXValues.length; i++)
	// {
	// logXValues[i] = Math.ceil((i + 1.0) * ((double) maxXValue)
	// / (double) (numberOfExtraXPoints + 1));
	// }
	// logPlot.setExtraXAxisValues(logXValues);
	// logPlot.showXValuesAsInts(false);
	// }
	// else if(maxXValue > 1.0)
	// {
	// int numberOfExtraXPoints = (int) (maxXValue - 1.0);
	//
	// double[] logXValues = new double[numberOfExtraXPoints];
	// for(int i = 0; i < logXValues.length; i++)
	// {
	// logXValues[i] = i + 1;
	// }
	// logPlot.setExtraXAxisValues(logXValues);
	// logPlot.showXValuesAsInts(false);
	// }
	// else
	// {
	// double[] logXValues = {0.2, 0.4, 0.6, 0.8};
	// logPlot.setExtraXAxisValues(logXValues);
	// logPlot.showXValuesAsInts(false);
	// }
	//
	// // set the max y value on the plot (to nearest 10^nth power)
	// logPlot.setMaximumYValue(maxYValue);
	//
	// // draw some extra points on the y-axis (looks good)
	// if(maxYValue > 20.0)
	// {
	// int numberOfExtraYPoints = 3;
	//
	// double[] logYValues = new double[numberOfExtraYPoints];
	// for(int i = 0; i < logYValues.length; i++)
	// {
	// logYValues[i] = Math.ceil((i + 1.0) * ((double) maxYValue)
	// / (double) (numberOfExtraYPoints + 1));
	// }
	// logPlot.setExtraYAxisValues(logYValues);
	// logPlot.showYValuesAsInts(true);
	// }
	// if(maxYValue > 1.0)
	// {
	// int numberOfExtraYPoints = (int) (maxYValue - 1.0);
	//
	// double[] logYValues = new double[numberOfExtraYPoints];
	// for(int i = 0; i < logYValues.length; i++)
	// {
	// logYValues[i] = i + 1;
	// }
	// logPlot.setExtraYAxisValues(logYValues);
	// logPlot.showYValuesAsInts(true);
	// }
	// else if(maxYValue == 1.0)
	// {
	// double[] logYValues = {0.2, 0.4, 0.6, 0.8};
	// logPlot.setExtraYAxisValues(logYValues);
	// logPlot.showYValuesAsInts(false);
	// }
	//
	// plot.setPointDisplayColorsToDefault();
	//
	// // finally draw the plot
	// if(logNumbers.size() > 0)
	// {
	// logPlot.drawPoints(logNumbers);
	// }
	// else
	// {
	// logPlot.clearPlot();
	// }
	// }

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
		// forces the analysis to reset the comparison state to which the
		// Hamming distance will be calculated
		updateComparisonState = true;

		// this is the panel that will be displayed (getDisplayPanel() will
		// return the panel that this creates)
		if(displayPanel == null)
		{
			createDisplayPanel();
		}
	}

	/**
	 * Counts the number of cells that have different values (Hamming distance).
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
		// get an iterator over the current lattice
		Iterator cellIterator = lattice.iterator();

		// Get an iterator over the list of states used for comparison.
		//
		// By getting the iterator at each step, we guarantee that the user
		// can't try to gain access to the iterator by changing the
		// previous/current state radio button at the same time that we are
		// using that iterator (which would cause a concurrent modification
		// error).
		Iterator<CellState> comparisonIterator = getComparisonStatesIterator(lattice);

		// go through the lattice and count each cell that differs from the
		// set of comparison states
		int hammingDistance = 0;
		while(cellIterator.hasNext())
		{
			// get the cell state
			Cell cell = (Cell) cellIterator.next();
			CellState cellState = cell.getState(generation);

			// get the comparison cell state
			CellState comparisonCellState = comparisonIterator.next();

			// is it different from the comparison state?
			if((comparisonCellState != null)
					&& !cellState.equals(comparisonCellState))
			{
				hammingDistance++;
			}
		}

		// save distance in a linked list for plotting
		hammingDistanceList
				.add(new Point2D.Double(generation, hammingDistance));
		if(hammingDistanceList.size() > MAX_NUMBER_TO_PLOT)
		{
			hammingDistanceList.removeFirst();
		}

		// set the text for the labels
		generationDataLabel.setText("" + generation);
		hammingDataLabel.setText("" + hammingDistance);

		// create an array of data to be saved
		data[0] = "" + generation;
		data[1] = "" + hammingDistance;

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

		// reset all the variables
		// displayPanel = null;
		// elapsedTimeStepsSinceLastAdjustedXAxis = 0;
		// elapsedTimeStepsSinceLastAdjustedYAxis = 0;
		// numStates = 2;
		// selectedState = ALL_NON_EMPTY_STATES_CHOICE;
		// lastSelectedState = ALL_NON_EMPTY_STATES_CHOICE;
		// currentColor = Color.GRAY;
		// oldMaxXValue = 1;
		// oldMaxYValue = 1;
		// colorPatch = null;
		// saveDataCheckBox = null;
		// generationDataLabel = null;
		// integerColorChooser = null;
		// numberOfClustersOfEachSize = null;
		// plot = null;
		// data = null;
		// rule = null;
		// view = null;
		// emptyStateButton = null;
		// nonEmptyStatesButton = null;
		// particularStateButton = null;
		// selectStateButton = null;

		// remember which radio button was selected
		boolean previousStateSelected = false;
		if(previousStateButton.isSelected())
		{
			previousStateSelected = true;
		}

		// reset the analysis parameters
		setUpAnalysis();

		// empty the plot list (so that old data doesn't get plotted again when
		// the new simulation starts)
		hammingDistanceList.clear();

		// reset the radio button as appropriate
		if(previousStateSelected)
		{
			previousStateButton.setSelected(true);
		}

		// redraw the plots
		// plotData();
		// plotLogLogData();
		// plot.clearPlot();
		// logPlot.clearPlot();

		// force it to redraw
		if(displayPanel != null)
		{
			displayPanel.invalidate();
		}
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
	 * Decides what to do when the user selects the current or previous state
	 * for comparison.
	 * 
	 * @author David Bahr
	 */
	private class StateChoiceListener implements ItemListener
	{
		public void itemStateChanged(ItemEvent event)
		{
			// if(currentStateButton.isSelected())
			// {
			//                
			// }
			// else if(previousStateButton.isSelected())
			// {
			//                
			// }

			updateComparisonState = true;

			// reset so that starts plotting anew
			// ...
			// empty the plot list (so that old data doesn't get plotted again
			// when the new simulation starts)
			hammingDistanceList.clear();

			// reset the plots
			// if(plot != null)
			// {
			// plot.clearPlot();
			// }
			// if(logPlot != null)
			// {
			// logPlot.clearPlot();
			// }

			rerunAnalysis();

			// force it to redraw
			if(displayPanel != null)
			{
				displayPanel.invalidate();
			}
		}
	}
}
