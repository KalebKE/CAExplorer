/*
 AllPopulationAnalysis -- a class within the Cellular Automaton Explorer. 
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
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;

import userRules.CellularMarketModel;
import userRules.cellularMarketModel.math.Histogram;
import userRules.cellularMarketModel.math.LogReturn;
import cellularAutomata.CAController;
import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.lattice.Lattice;
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
 * Finds the price of each state (asset) at each generation.
 * 
 */
public class PricingDistributionAnalysis extends Analysis implements
		ActionListener
{
	// the maximum number of elements that will be plotted on the time series
	// plot
	private static final int MAX_NUMBER_TO_PLOT = 20;

	// the maximum number of states that will be allowed for this analysis to
	// run
	private static final int MAX_NUMBER_OF_STATES = 1000;

	// a brief display name for this class
	private static final String ANALYSIS_NAME = "Pricing Distribution Analysis";

	// The pattern used to display decimals, particularly for the percent
	// pricing.
	private static final String DECIMAL_PATTERN = "0.000";

	// display info for this class
	private static final String INFO_MESSAGE = "Calculates the price of assets "
			+ "based on the number of cells that have each state.";

	// the label for the "plot zero state" check box
	private static final String PLOT_ZERO_STATE = "   Plot zero state";

	// the tooltip for the "plot zero state" check box
	private static final String PLOT_ZERO_STATE_TOOLTIP = "For many CA, the "
			+ "number of cells in the zero state will overwhelm the other data.";

	// the action command for saving the data and the label used by the "save
	// data" check box
	private static final String SAVE_DATA = "   Save the data";

	// a tooltip for the save data check box
	private static final String SAVE_DATA_TOOLTIP = "<html>Saves pricing data "
			+ "to a file (saves <br> every generation while the box is checked).</html>";

	// a brief tooltip description for this class
	private static final String TOOLTIP = "<html>calculate price of each cell state</html>";

	// display info/warning for this class when not an integer based rule
	private static final String WARNING_MESSAGE = "Warning, this analysis only works "
			+ "with integer based rules.  The current rule is not integer based.";

	// display info/warning for this class when have too many states
	private static final String WARNING_MESSAGE_STATES = "Warning, this analysis only works "
			+ "with integer based rules using "
			+ MAX_NUMBER_OF_STATES
			+ " or fewer states.";

	// true if this is an integer-based rule
	private boolean isCompatibleRule = true;

	// when true, plots the zero state. Default is true.
	private boolean plotZeroState = true;

	// the percent of each state
	private double[] marketValue = null;

	// if the user wants to save the data to a file, this will be instantiated
	private FileWriter fileWriter = null;

	// fonts for display
	private Fonts fonts = new Fonts();

	// the number of cell states
	private int numberOfStates = 2;

	private int[] previousNumOccupiedByState;

	// The check box that lets the user save the data
	private JCheckBox saveDataCheckBox = null;

	// The check box that lets the user plot the zero state
	private JCheckBox plotZeroStateCheckBox = null;

	// label for the current generation
	private JLabel generationDataLabel = null;

	// labels for the number of cells occupied by each state
	private JLabel[] numberOccupiedByStateDataLabel = null;

	// labels for the percentage of cells occupied by each state
	private JLabel[] percentOccupiedByStateDataLabel = null;

	// the panel where results are displayed
	private JPanel displayPanel = null;

	// the list of points that will be drawn on the plot
	private LinkedList<Point2D.Double> priceList = new LinkedList<Point2D.Double>();

	// the list of points that will be drawn on the time series plot. Note each
	// element of the array is for a different state.
	private LinkedList<Point2D.Double>[] historicalPriceList = null;

	// the list of points that will be drawn on the time series plot. Note each
	// element of the array is for a different state.
	private LinkedList<Point2D.Double>[] timeSeriesOfDistributionList = null;

	// a panel that plots the pricing data
	private SimplePlot plot = null;

	// a panel that plots the pricing data as a time series
	private SimplePlot[] timeSeriesPlot = null;

	// a delimiter for spacing data in the data file
	private String delimiter = null;

	// data that will be saved to a file
	private String[] data = null;

	private Kurtosis kurtosis;

	/**
	 * Create an analyzer that counts the number of cells of every state.
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
	 * constructor may be placed in an <code>if</code> statement (as illustrated
	 * in the parent constructor and in most other analyses designed by David
	 * Bahr).
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
	public PricingDistributionAnalysis(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		// only build the memory intensive parts of the analysis when
		// minimalOrLazyInitialization is false
		if (!minimalOrLazyInitialization)
		{
			// build the analysis
			constructAnalysis();

			previousNumOccupiedByState = new int[numberOfStates];

			Random r = new Random();

			// *initialize the price of the asset here*
			for (int i = 0; i < numberOfStates; i++)
			{
				previousNumOccupiedByState[i] = 0;
			}

			kurtosis = new Kurtosis();
		}
	}

	/**
	 * Builds the analysis. Called by the constructor and the reset method.
	 */
	private void constructAnalysis()
	{
		// make sure is a compatible rule
		String ruleClassName = CurrentProperties.getInstance()
				.getRuleClassName();
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(ruleClassName);
		String ruleDescription = rule.getDisplayName();
		isCompatibleRule = IntegerCellState.isCompatibleRule(ruleDescription);

		if (isCompatibleRule)
		{
			numberOfStates = CurrentProperties.getInstance().getNumStates();

			if (numberOfStates <= MAX_NUMBER_OF_STATES)
			{
				data = new String[numberOfStates + 1];

				// note that you cannot create an array of generics without
				// this workaround :-(
				timeSeriesOfDistributionList = new LinkedList[numberOfStates];
				for (int i = 0; i < timeSeriesOfDistributionList.length; i++)
				{
					timeSeriesOfDistributionList[i] = new LinkedList<Point2D.Double>();
					Point2D.Double point = new Point2D.Double();
					point.setLocation(0, 0);
					timeSeriesOfDistributionList[i].add(point);
				}

				// note that you cannot create an array of generics without
				// this workaround :-(
				historicalPriceList = new LinkedList[numberOfStates];
				for (int i = 0; i < historicalPriceList.length; i++)
				{
					historicalPriceList[i] = new LinkedList<Point2D.Double>();
				}
			}
		}

		timeSeriesPlot = new SimplePlot[numberOfStates];

		// this is the panel that will be displayed (getDisplayPanel() will
		// return the panel that this creates)
		createDisplayPanel();
	}

	/**
	 * Create labels used to display the data for the pricing statistics.
	 */
	private void createDataDisplayLabels()
	{
		// if one is null, then they all are
		if (generationDataLabel == null)
		{
			generationDataLabel = new JLabel("");

			numberOccupiedByStateDataLabel = new JLabel[numberOfStates];
			percentOccupiedByStateDataLabel = new JLabel[numberOfStates];
			for (int i = 0; i < numberOccupiedByStateDataLabel.length; i++)
			{
				numberOccupiedByStateDataLabel[i] = new JLabel("");
				percentOccupiedByStateDataLabel[i] = new JLabel("");
			}
		}
	}

	/**
	 * Create the panel used to display the pricing statistics.
	 */
	private void createDisplayPanel()
	{
		int displayWidth = CAFrame.tabbedPaneDimension.width;
		int displayHeight = 600 + (100 * numberOfStates);

		// create the display panel
		if (displayPanel == null)
		{
			displayPanel = new JPanel(new GridBagLayout());
			displayPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			displayPanel.setPreferredSize(new Dimension(displayWidth,
					displayHeight));
		} else
		{
			displayPanel.removeAll();
		}

		if (isCompatibleRule && (numberOfStates <= MAX_NUMBER_OF_STATES))
		{
			// create a panel that displays messages
			JPanel messagePanel = createMessagePanel();

			// create the labels for the display
			createDataDisplayLabels();
			JLabel generationLabel = new JLabel("Generation:   ");
			JLabel stateLabel = new JLabel("State:");
			JLabel numStateLabel = new JLabel("Number:");
			JLabel percentOccupiedLabel = new JLabel("Kurtosis:");

			// create boxes for each column of the display (a Box uses the
			// BoxLayout, so it is handy for laying out components)
			Box boxOfStateLabels = Box.createVerticalBox();
			Box boxOfNumberLabels = Box.createVerticalBox();
			Box boxOfPercentLabels = Box.createVerticalBox();

			// the amount of vertical space to put between components
			int verticalSpace = 5;

			// add the states to the first vertical box
			boxOfStateLabels.add(stateLabel);
			boxOfStateLabels.add(Box.createVerticalStrut(verticalSpace));
			for (int state = 0; state < numberOfStates; state++)
			{
				boxOfStateLabels.add(new JLabel("" + state));
				boxOfStateLabels.add(Box.createVerticalStrut(verticalSpace));
			}

			// add the numbers (in each state) to the second vertical box
			boxOfNumberLabels.add(numStateLabel);
			boxOfNumberLabels.add(Box.createVerticalStrut(verticalSpace));
			for (int i = 0; i < numberOfStates; i++)
			{
				boxOfNumberLabels.add(numberOccupiedByStateDataLabel[i]);
				boxOfNumberLabels.add(Box.createVerticalStrut(verticalSpace));
			}

			// add the percents (in each state) to the third vertical box
			boxOfPercentLabels.add(percentOccupiedLabel);
			boxOfPercentLabels.add(Box.createVerticalStrut(verticalSpace));
			for (int i = 0; i < numberOfStates; i++)
			{
				boxOfPercentLabels.add(percentOccupiedByStateDataLabel[i]);
				boxOfPercentLabels.add(Box.createVerticalStrut(verticalSpace));
			}

			// create another box that holds all of the label boxes
			Box boxOfLabels = Box.createHorizontalBox();
			boxOfLabels.add(boxOfStateLabels);
			boxOfLabels.add(boxOfNumberLabels);
			boxOfLabels.add(boxOfPercentLabels);
			boxOfLabels.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));

			// put the boxOfLabels in a scrollPane -- with many states, will get
			// very large
			JScrollPane stateScroller = new JScrollPane(boxOfLabels);
			int scrollPaneWidth = (int) (displayWidth * 0.8);
			int scrollPaneHeight = displayHeight / 4;
			stateScroller.setPreferredSize(new Dimension(scrollPaneWidth,
					scrollPaneHeight));
			stateScroller.setMinimumSize(new Dimension(scrollPaneWidth,
					scrollPaneHeight));
			stateScroller.setMaximumSize(new Dimension(scrollPaneWidth,
					scrollPaneHeight));
			stateScroller
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

			// create a "plot zero state" check box
			plotZeroStateCheckBox = new JCheckBox(PLOT_ZERO_STATE);
			plotZeroStateCheckBox.setSelected(true);
			plotZeroStateCheckBox.setToolTipText(PLOT_ZERO_STATE_TOOLTIP);
			plotZeroStateCheckBox.setActionCommand(PLOT_ZERO_STATE);
			plotZeroStateCheckBox.addActionListener(this);
			JPanel plotZeroStatePanel = new JPanel(new BorderLayout());
			plotZeroStatePanel.setBorder(BorderFactory.createEmptyBorder(7, 7,
					7, 7));
			plotZeroStatePanel.add(BorderLayout.CENTER, plotZeroStateCheckBox);

			// create a "save data" check box
			saveDataCheckBox = new JCheckBox(SAVE_DATA);
			saveDataCheckBox.setToolTipText(SAVE_DATA_TOOLTIP);
			saveDataCheckBox.setActionCommand(SAVE_DATA);
			saveDataCheckBox.addActionListener(this);
			JPanel saveDataPanel = new JPanel(new BorderLayout());
			saveDataPanel
					.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
			saveDataPanel.add(BorderLayout.CENTER, saveDataCheckBox);

			// create a panel that plots the data
			plot = new SimplePlot();

			// add all the components to the panel
			int row = 0;
			displayPanel.add(messagePanel, new GBC(1, row).setSpan(4, 1)
					.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
					.setInsets(1));

			row++;
			displayPanel.add(
					plot,
					new GBC(1, row).setSpan(4, 1).setFill(GBC.BOTH)
							.setWeight(10.0, 10.0).setAnchor(GBC.WEST)
							.setInsets(1));

			row++;
			for (int i = 0; i < numberOfStates; i++)
			{
				timeSeriesPlot[i] = new SimplePlot();
				displayPanel.add(timeSeriesPlot[i],
						new GBC(1, row).setSpan(4, 1).setFill(GBC.BOTH)
								.setWeight(10.0, 10.0).setAnchor(GBC.WEST)
								.setInsets(1));

				row++;
			}
			displayPanel.add(plotZeroStatePanel, new GBC(1, row).setSpan(4, 1)
					.setFill(GBC.NONE).setWeight(1.0, 1.0)
					.setAnchor(GBC.CENTER).setInsets(1));

			row++;
			displayPanel.add(generationLabel, new GBC(1, row).setSpan(1, 1)
					.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.EAST)
					.setInsets(1));
			displayPanel.add(generationDataLabel, new GBC(2, row).setSpan(1, 1)
					.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
					.setInsets(1));

			row++;
			displayPanel.add(stateScroller, new GBC(1, row).setSpan(4, 1)
					.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
					.setInsets(1));

			row++;
			displayPanel.add(saveDataPanel, new GBC(1, row).setSpan(4, 1)
					.setFill(GBC.NONE).setWeight(1.0, 1.0)
					.setAnchor(GBC.CENTER).setInsets(1));

		} else
		{
			int row = 0;
			displayPanel.add(createWarningMessagePanel(), new GBC(1, row)
					.setSpan(4, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0)
					.setAnchor(GBC.WEST).setInsets(1));
		}

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
			String[] header = new String[data.length];
			header[0] = "Distribution";
			for (int i = 1; i < header.length; i++)
			{
				header[i] = "Number with state " + i + ":";
			}

			fileWriter.writeData(header, delimiter);

			// save the initial data (at the generation when the user requested
			// that the data be saved)
			if (data != null)
			{
				fileWriter.writeData(data, delimiter);
			}
		} catch (IOException e)
		{
			// This happens if the user did not select a valid file. (For
			// example, the user canceled and did not choose any file when
			// prompted.) So uncheck the "file save" box
			if (saveDataCheckBox != null)
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
		AttentionPanel attentionPanel = new AttentionPanel("Asset Prices");

		MultilineLabel messageLabel = null;
		messageLabel = new MultilineLabel(INFO_MESSAGE);
		messageLabel.setFont(fonts.getItalicSmallerFont());
		messageLabel.setMargin(new Insets(6, 10, 2, 16));
		messageLabel.setColumns(40);

		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		messagePanel.add(attentionPanel, BorderLayout.NORTH);
		messagePanel.add(messageLabel, BorderLayout.CENTER);

		return messagePanel;
	}

	/**
	 * Creates a warning message.
	 * 
	 * @return A panel containing the warning message.
	 */
	private JPanel createWarningMessagePanel()
	{
		// a "grab their attention" panel
		AttentionPanel attentionPanel = new AttentionPanel("Asset Prices");

		MultilineLabel messageLabel = null;
		if (!isCompatibleRule)
		{
			messageLabel = new MultilineLabel(WARNING_MESSAGE);
		} else
		{
			messageLabel = new MultilineLabel(WARNING_MESSAGE_STATES);
		}
		messageLabel.setForeground(Color.RED);
		messageLabel.setFont(fonts.getBoldFont());
		messageLabel.setMargin(new Insets(6, 10, 2, 16));
		messageLabel.setColumns(40);

		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		messagePanel.add(attentionPanel, BorderLayout.NORTH);
		messagePanel.add(messageLabel, BorderLayout.CENTER);

		return messagePanel;
	}

	/**
	 * Plots the pricing data.
	 */
	private void plotData()
	{
		// set the min and max x values on the plot
		plot.setMinimumXValue(0);
		plot.setMaximumXValue(numberOfStates - 1);

		// set the min y value on the plot
		plot.setMinimumYValue(0.0);

		// set the max y value on the plot
		double maxYValue = 0.0;
		Iterator<Point2D.Double> iterator = priceList.iterator();
		while (iterator.hasNext())
		{
			Point2D.Double point = iterator.next();
			if (point.y > maxYValue)
			{
				maxYValue = point.y;
			}
		}

		// now round up to the nearest tenth and add 0.1. This gives some
		// wiggle room before the plot will have to redraw the y-axis. If
		// redraw the y-axis too often, it will look bad. This crazy case
		// statement ensures that we don't get something like 0.7999999 instead
		// of 0.8 (which is what was happening when I would divide by 10.0 using
		// a formula to calculate the maxYValue).
		switch ((int) Math.ceil(maxYValue * 10.0))
		{
		case 0:
			maxYValue = 0.1;
			break;
		case 1:
			maxYValue = 0.2;
			break;
		case 2:
			maxYValue = 0.3;
			break;
		case 3:
			maxYValue = 0.4;
			break;
		case 4:
			maxYValue = 0.5;
			break;
		case 5:
			maxYValue = 0.6;
			break;
		case 6:
			maxYValue = 0.7;
			break;
		case 7:
			maxYValue = 0.8;
			break;
		case 8:
			maxYValue = 0.9;
			break;
		case 9:
			maxYValue = 1.0;
			break;
		case 10:
			maxYValue = 1.0;
			break;
		case 11:
			maxYValue = 1.0;
			break;
		case 12:
			maxYValue = 1.0;
			break;
		}

		if (maxYValue > 1.0)
		{
			maxYValue = 1.0;
		}

		maxYValue = maxYValue * 200;

		// set that max value
		plot.setMaximumYValue(maxYValue);

		// set plot axes labels
		plot.setXAxisLabel("state value");
		plot.setYAxisLabel("% each state");

		// draw some extra points on the x and y axes (looks good)
		int numberOfInteriorYValueLabels = (int) ((maxYValue / 20));
		double[] yValues = new double[numberOfInteriorYValueLabels];
		for (int i = 0; i < yValues.length; i++)
		{
			double answer = (i * 20);

			yValues[i] = answer;
		}
		plot.setExtraYAxisValues(yValues);

		if (numberOfStates > 2)
		{
			if (numberOfStates <= 10)
			{
				double[] xValues = new double[numberOfStates - 2];
				for (int i = 0; i < numberOfStates - 2; i++)
				{
					xValues[i] = i + 1;
				}
				plot.setExtraXAxisValues(xValues);
			}
		}

		// specify colors for the points
		Color[] colorArray = null;
		if (plotZeroState)
		{
			// make sure is a compatible rule
			CellStateView view = Rule.getCurrentView();

			colorArray = new Color[numberOfStates];
			for (int state = 0; state < colorArray.length; state++)
			{
				Color stateColor = view.getDisplayColor(new IntegerCellState(
						state), null, new Coordinate(0, 0));

				colorArray[state] = stateColor;
			}
		} else
		{
			// make sure is a compatible view
			CellStateView view = Cell.getView();

			colorArray = new Color[numberOfStates - 1];
			for (int state = 1; state < numberOfStates; state++)
			{
				Color stateColor = view.getDisplayColor(new IntegerCellState(
						state), null, new Coordinate(0, 0));

				colorArray[state - 1] = stateColor;
			}
		}
		plot.setPointDisplayColors(colorArray);

		plot.drawPoints(priceList);
	}

	/**
	 * Plots the pricing data as a time series.
	 */
	private void plotTimeSeriesData()
	{
		for (int i = 0; i < numberOfStates; i++)
		{
			// create a list of all data points (exclude 0 if necessary)
			LinkedList<Point2D.Double> allPoints = new LinkedList<Point2D.Double>();
			int startPosition = 0;
			if (!plotZeroState)
			{
				startPosition = 1;
			}

			allPoints.addAll(timeSeriesOfDistributionList[i]);

			// set the min and max values on the plot
			Point2D firstPoint = (Point2D) timeSeriesOfDistributionList[i]
					.getFirst();
			timeSeriesPlot[i].setMaximumXValue(firstPoint.getX()
					+ MAX_NUMBER_TO_PLOT - 1);
			timeSeriesPlot[i]
					.setMinimumXValue(0);
			timeSeriesPlot[i].setMinimumYValue(0.0);
			timeSeriesPlot[i].setXAxisLabel("Histogram");
			timeSeriesPlot[i].setYAxisLabel("% bin");
			timeSeriesPlot[i].showPlotLines(false);

			// set the max y-value
			double maxYValue = 0.0;
			Iterator<Point2D.Double> iterator = allPoints.iterator();
			while (iterator.hasNext())
			{
				Point2D.Double point = iterator.next();
				if (point.y > maxYValue)
				{
					maxYValue = point.y;
				}
			}

			// now round up to the nearest tenth and add 0.1. This gives some
			// wiggle room before the plot will have to redraw the y-axis. If
			// redraw the y-axis too often, it will look bad. This crazy case
			// statement ensures that we don't get something like 0.7999999
			// instead
			// of 0.8 (which is what was happening when I would divide by 10.0
			// using
			// a formula to calculate the maxYValue).
			switch ((int) Math.ceil(maxYValue * 10.0))
			{
			case 0:
				maxYValue = 0.1;
				break;
			case 1:
				maxYValue = 0.2;
				break;
			case 2:
				maxYValue = 0.3;
				break;
			case 3:
				maxYValue = 0.4;
				break;
			case 4:
				maxYValue = 0.5;
				break;
			case 5:
				maxYValue = 0.6;
				break;
			case 6:
				maxYValue = 0.7;
				break;
			case 7:
				maxYValue = 0.8;
				break;
			case 8:
				maxYValue = 0.9;
				break;
			case 9:
				maxYValue = 1.0;
				break;
			case 10:
				maxYValue = 1.0;
				break;
			case 11:
				maxYValue = 1.0;
				break;
			case 12:
				maxYValue = 1.0;
				break;
			}

			if (maxYValue > 1.0)
			{
				maxYValue = 1.0;
			}

			maxYValue = maxYValue * 1;

			timeSeriesPlot[i].setMaximumYValue(maxYValue);

			// draw some extra points on the y axes (looks good)
			int numberOfInteriorYValueLabels = (int) ((maxYValue / 20));
			double[] yValues = new double[numberOfInteriorYValueLabels];
			for (int j = 0; j < yValues.length; j++)
			{
				double answer = (j * 20);

				yValues[j] = answer;
			}
			timeSeriesPlot[i].setExtraYAxisValues(yValues);

			// specify colors for the points
			Color[] colorArray = new Color[allPoints.size()];
			CellStateView view = Cell.getView();

			Color stateColor = view.getDisplayColor(
					new IntegerCellState(i), null, new Coordinate(0, 0));

			for (int j = 0; j < timeSeriesOfDistributionList[i].size(); j++)
			{
				colorArray[j] = stateColor;
			}

			timeSeriesPlot[i].setPointDisplayColors(colorArray);

			// draw the points!
			timeSeriesPlot[i].drawPoints(allPoints);
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
		if (fileWriter != null)
		{
			try
			{
				fileWriter.writeData(data, delimiter);
			} catch (IOException e)
			{
				// Could not save the data, so close the file
				if (fileWriter != null)
				{
					fileWriter.close();
				}

				// and uncheck the "save data" box
				if (saveDataCheckBox != null)
				{
					saveDataCheckBox.setSelected(false);
				}
			}
		}
	}

	/**
	 * Tracks the price of an asset in MajorityFinance. Initially, a uniform
	 * price is set for all assets (states) on the lattice; this is Pt. The
	 * difference between the number of cells with a given state on the previous
	 * generation versus the current generation is calculated; this is the
	 * transaction quantity (Qt). That number is multiplied by a positive
	 * number, much smaller than 1, and then added to the current price; this is
	 * Cp.
	 * 
	 * Qt = previousQuantity - currentQuantity
	 * 
	 * Pt+1 = Pt + Cp*Qt
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
		// make sure it is an integer based rule w/o too many states
		if (IntegerCellState.isCompatibleRule(rule)
				&& (numberOfStates <= MAX_NUMBER_OF_STATES))
		{
			// the total number of cells
			int totalNumberOfCells = 0;

			// the number of cells occupied by each state
			int[] numberOccupiedByState = new int[numberOfStates];
			for (int i = 0; i < numberOccupiedByState.length; i++)
			{
				numberOccupiedByState[i] = 0;
			}

			// get an iterator over the lattice
			Iterator cellIterator = lattice.iterator();

			// get each cell on the lattice
			Cell cell = null;
			while (cellIterator.hasNext())
			{
				// add one more to the total number of cells
				totalNumberOfCells++;

				// get the cell
				cell = (Cell) cellIterator.next();

				// get its state.
				IntegerCellState state = (IntegerCellState) cell
						.getState(generation);

				// increment the number of states
				numberOccupiedByState[state.toInt()]++;
			}

			marketValue = new double[numberOfStates];

			if (previousNumOccupiedByState[0] == 0)
			{
				previousNumOccupiedByState = numberOccupiedByState;
			}

			// Long term derivative version
			for (int i = 0; i < marketValue.length; i++)
			{
				// move the price out to an array to graph the data
				marketValue[i] = CellularMarketModel.getCurrentMarketValue()[i];
			}

			// finally, make the current state the previous state
			previousNumOccupiedByState = numberOccupiedByState;

			// save percentOccupiedByState in a linked list for plotting
			int startStateOnPlot = 1;
			if (plotZeroState)
			{
				// don't plot the zero state because it is too big relative to
				// the rest of the states (and dominates the plot)
				startStateOnPlot = 0;
			}

			priceList.clear();
			for (int state = startStateOnPlot; state < marketValue.length; state++)
			{
				priceList.add(new Point2D.Double(state, marketValue[state]));
			}

			// save a time series of the percentOccupied in a linked list for
			// plotting. *Do* include the zero state, even when not plotting it.
			for (int state = 0; state < marketValue.length; state++)
			{

				historicalPriceList[state].add(new Point2D.Double(generation,
						marketValue[state]));
				if (historicalPriceList[state].size() > 1000)
				{
					historicalPriceList[state].removeFirst();
				}
			}

			double historicalPricing[][] = new double[numberOfStates][];

			for (int i = 0; i < historicalPricing.length; i++)
			{
				historicalPricing[i] = new double[historicalPriceList[i].size()];
				for (int j = 0; j < historicalPricing[i].length; j++)
				{
					historicalPricing[i][j] = historicalPriceList[i].get(j)
							.getY();
				}
			}
			try
			{
				double[][] histogram = new double[numberOfStates][];
				for (int i = 0; i < numberOfStates; i++)
				{
					histogram[i] = Histogram
							.createHistogram(historicalPricing[i]);
				}

				for (int i = 0; i < histogram.length; i++)
				{
					if (timeSeriesOfDistributionList[i].size() != 0)
					{
						timeSeriesOfDistributionList[i]
								.removeAll(timeSeriesOfDistributionList[i]);
					}
				}

				for (int i = 0; i < histogram.length; i++)
				{
					if (histogram.length != 0)
					{
						for (int j = 0; j < histogram[i].length; j++)
						{
							timeSeriesOfDistributionList[i]
									.add(new Point2D.Double(j, histogram[i][j]));

							if (timeSeriesOfDistributionList[i].size() > MAX_NUMBER_TO_PLOT)
							{
								timeSeriesOfDistributionList[i].removeFirst();
							}
						}
					}
				}

				// set the text for the labels
				generationDataLabel.setText("" + generation);

				for (int i = 0; i < numberOccupiedByState.length; i++)
				{
					numberOccupiedByStateDataLabel[i].setText(""
							+ numberOccupiedByState[i]);
				}

				double[][] logReturn = LogReturn
						.calcLogReturn(historicalPricing);

				// and set the text for the percent label, but format!
				DecimalFormat percentFormatter = new DecimalFormat(
						DECIMAL_PATTERN);
				for (int i = 0; i < logReturn.length; i++)
				{
					String output = percentFormatter.format(kurtosis.evaluate(
							logReturn[i], 0, logReturn[i].length));
					percentOccupiedByStateDataLabel[i].setText(output);
				}

				// create an array of data to be saved
				data[0] = "" + generation;
				for (int i = 0; i < marketValue.length; i++)
				{
					data[i + 1] = "" + marketValue[i];
				}

				// see if user wants to save the data
				if (fileWriter != null)
				{
					// save it
					saveData(data);
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}

			try
			{
				// plot the percent data
				plotData();
				plotTimeSeriesData();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
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
		if (fileWriter != null)
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

		if (command.equals(SAVE_DATA))
		{
			if (saveDataCheckBox.isSelected())
			{
				// they want to save data, so open a data file
				createFileWriter();
			} else
			{
				// They don't want to save data anymore, so close the file.
				// The synchronized keyword prevents accidental access elsewhere
				// in the code while the file is being closed. Otherwise, other
				// code might try to write to the file while it is being closed.
				synchronized (this)
				{
					if (fileWriter != null)
					{
						fileWriter.close();
						fileWriter = null;
					}
				}
			}
		} else if (command.equals(PLOT_ZERO_STATE))
		{
			if (priceList != null && plotZeroStateCheckBox.isSelected())
			{
				// they want the zero state to be plotted
				plotZeroState = true;
				priceList.addFirst((new Point2D.Double(0, marketValue[0])));
			} else
			{
				// they don't want the zero state to be plotted
				plotZeroState = false;
				if (priceList.size() > 0)
				{
					priceList.removeFirst();
				}
			}

			plotData();
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
	 * Gets a JPanel that displays results of the pricing analysis.
	 * 
	 * @return A display for the pricing analysis results.
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
	 * color, and is used to change the color of the points on the plot.
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		if (event.getPropertyName().equals(CurrentProperties.COLORS_CHANGED))
		{
			plotData();
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
		synchronized (this)
		{
			if (fileWriter != null)
			{
				fileWriter.close();
				fileWriter = null;
			}
		}

		// and uncheck the "save data" box
		if (saveDataCheckBox != null)
		{
			saveDataCheckBox.setSelected(false);
		}

		// reset all the variables
		plotZeroState = true;
		marketValue = null;
		fileWriter = null;
		saveDataCheckBox = null;
		plotZeroStateCheckBox = null;
		generationDataLabel = null;
		numberOccupiedByStateDataLabel = null;
		percentOccupiedByStateDataLabel = null;
		priceList = new LinkedList<Point2D.Double>();
		plot = null;
		timeSeriesPlot = null;
		data = null;

		constructAnalysis();

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
}
