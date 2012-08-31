/*
 SpeedOfSimulationAnalysis -- a class within the Cellular Automaton Explorer. 
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import cellularAutomata.CAController;
import cellularAutomata.CurrentProperties;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.graphics.colors.Colors;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;
import cellularAutomata.util.SimplePlot;
import cellularAutomata.util.files.FileWriter;

/**
 * Tracks the speed of the simulation, and plots.
 * 
 * @author David Bahr
 */
public class SpeedOfSimulationAnalysis extends Analysis implements
		ActionListener
{
	// color for the average speed on the plot
	private static final Color AVG_SPEED_COLOR = Colors.FOREST_GREEN;

	// color for the current speed on the plot
	private static final Color SPEED_COLOR = Color.BLUE;

	// the default for the maximum y-axis value
	private static final double DEFAULT_MAX_Y_VALUE = 5.0;

	// the maximum number of elements that will be plotted
	private static final int MAX_NUMBER_TO_PLOT = 100;

	// a brief display name for this class
	private static final String ANALYSIS_NAME = "Speed of Simulation";

	// title for the subpanel that displays the data
	private static final String DATA_PANEL_TITLE = "Data";

	// The pattern used to display decimals, particularly for the percent
	// population.
	private static final String DECIMAL_PATTERN = "0.000";

	// display info for this class
	private static final String INFO_MESSAGE = "Plots the number of generations per "
			+ "second. Use this tool to gauge the speed of a new user-submitted "
			+ "rule or analysis. The speed decreases roughly in proportion to the "
			+ "number of cells. NOTE: Ironically, this analysis "
			+ "slows the simulation (plotting takes time), but the speed values "
			+ "are useful for comparative purposes. \n\n"
			+ "If the plot shows lots of zeroes, then your computer is not capable "
			+ "of nanosecond time precision. \n\n"
			+ "Low lying outliers are generally caused by Java's garbage "
			+ "collection.  This can be confirmed by simultaneously running the "
			+ "Memory Usage analysis and correlating speed drops with sudden drops "
			+ "in memory. \n\n"
			+ "All data is reset each time the simulation is stopped and restarted.";

	// the action command for saving the data and the label used by the "save
	// data" check box
	private static final String SAVE_DATA = "   Save the data";

	// a tooltip for the save data check box
	private static final String SAVE_DATA_TOOLTIP = "<html>Saves speed data "
			+ "to a file (saves <br> every generation while the box is checked).</html>";

	// a brief tooltip description for this class
	private static final String TOOLTIP = "<html><body>tracks generations per second</body></html>";

	// turns false once the analyze method has been called for the first time
	private boolean firstTimeThrough = true;

	// color of titles of sections
	private Color titleColor = Color.BLUE;

	// the max value plotted.
	private double maxYValue = DEFAULT_MAX_Y_VALUE;

	// the generation when the analysis method was first called
	private int startGeneration = 0;

	// the time at the start of the analysis
	private long startTime = System.nanoTime();

	// the time at the previous generation
	private long previousTime = startTime;

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

	// label for the speed
	private JLabel speedDataLabel = null;

	// label for the average speed
	private JLabel averageSpeedDataLabel = null;

	// the panel where results are displayed
	private JPanel displayPanel = null;

	// the list of points that will be drawn on the plot
	private LinkedList<Point2D.Double> speedList = new LinkedList<Point2D.Double>();

	// the array of colors for the points that will be drawn on the plot
	private Color[] plotColors = new Color[2 * MAX_NUMBER_TO_PLOT];

	// a panel that plots the average value data
	private SimplePlot plot = null;

	// a delimiter for spacing data in the data file
	private String delimiter = null;

	// the data that will be saved to a file
	private String[] data = new String[3];

	/**
	 * Build an analysis that tracks the speed of the simulation (generations
	 * per second).
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
	public SpeedOfSimulationAnalysis(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			// this is the panel that will be displayed (getDisplayPanel() will
			// return the panel that this creates)
			createDisplayPanel();

			// first time through the analysis
			firstTimeThrough = true;

			// create an array of colors for plotting
			for(int i = 0; i < plotColors.length; i++)
			{
				// every other point on the plot is average speed, then speed
				if(i % 2 == 0)
				{
					plotColors[i] = AVG_SPEED_COLOR;
				}
				else
				{
					plotColors[i] = SPEED_COLOR;
				}
			}
		}
	}

	/**
	 * Create labels used to display the data for the speed.
	 */
	private void createDataDisplayLabels()
	{
		// if one is null, then they all are
		if(generationDataLabel == null)
		{
			generationDataLabel = new JLabel("");
			speedDataLabel = new JLabel("");
			averageSpeedDataLabel = new JLabel("");
		}
	}

	/**
	 * Create the panel used to display the speed data.
	 */
	private void createDisplayPanel()
	{
		if(displayPanel == null)
		{
			int displayWidth = CAFrame.tabbedPaneDimension.width;
			int displayHeight = 800;

			// create the display panel
			displayPanel = new JPanel(new GridBagLayout());
			displayPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			displayPanel.setPreferredSize(new Dimension(displayWidth,
					displayHeight));

			// create a panel that displays messages
			JPanel messagePanel = createMessagePanel();

			// create the labels for the display
			createDataDisplayLabels();
			JLabel generationLabel = new JLabel("Generation:   ");
			JLabel speedLabel = new JLabel("Speed (generations/second):   ");
			JLabel averageSpeedLabel = new JLabel(
					"Avg. speed (generations/second):   ");

			// set colors for the labels so that they correspond with the plot
			speedLabel.setForeground(SPEED_COLOR);
			averageSpeedLabel.setForeground(AVG_SPEED_COLOR);

			// create boxes for each column of the display (a Box uses the
			// BoxLayout, so it is handy for laying out components)
			Box boxOfNameLabels = Box.createVerticalBox();
			Box boxOfDataLabels = Box.createVerticalBox();

			// the amount of vertical space to put between components
			int verticalSpace = 5;

			// add the name labels to the first vertical box
			boxOfNameLabels.add(generationLabel);
			boxOfNameLabels.add(Box.createVerticalStrut(verticalSpace));
			boxOfNameLabels.add(speedLabel);
			boxOfNameLabels.add(Box.createVerticalStrut(verticalSpace));
			boxOfNameLabels.add(averageSpeedLabel);

			// add the data labels to the second vertical box
			boxOfDataLabels.add(generationDataLabel);
			boxOfDataLabels.add(Box.createVerticalStrut(verticalSpace));
			boxOfDataLabels.add(speedDataLabel);
			boxOfDataLabels.add(Box.createVerticalStrut(verticalSpace));
			boxOfDataLabels.add(averageSpeedDataLabel);

			// create another box that holds both of the label boxes
			Box boxOfLabels = Box.createHorizontalBox();
			boxOfLabels.add(boxOfNameLabels);
			boxOfLabels.add(boxOfDataLabels);
			boxOfLabels.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder(BorderFactory
							.createEtchedBorder(), DATA_PANEL_TITLE,
							TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION,
							titleFont, titleColor), BorderFactory
							.createEmptyBorder(7, 7, 7, 7)));

			// create a "save data" check box
			saveDataCheckBox = new JCheckBox(SAVE_DATA);
			saveDataCheckBox.setToolTipText(SAVE_DATA_TOOLTIP);
			saveDataCheckBox.setActionCommand(SAVE_DATA);
			saveDataCheckBox.addActionListener(this);
			JPanel saveDataPanel = new JPanel();
			saveDataPanel
					.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
			saveDataPanel.add(saveDataCheckBox);

			// create a panel that plots the data (with default parameters)
			plot = new SimplePlot();

			// create a panel that holds both the boxOfLabels and the save
			// button
			JPanel saveAndLabelsPanel = new JPanel(new GridBagLayout());
			saveAndLabelsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5,
					5, 5));
			int rowPos = 0;
			saveAndLabelsPanel.add(boxOfLabels, new GBC(0, rowPos)
					.setSpan(1, 1).setFill(GBC.BOTH).setWeight(0.0, 0.0)
					.setAnchor(GBC.NORTHWEST).setInsets(1));
			rowPos++;
			saveAndLabelsPanel.add(saveDataPanel, new GBC(0, rowPos).setSpan(1,
					1).setFill(GBC.BOTH).setWeight(1.0, 1.0)
					.setAnchor(GBC.WEST).setInsets(1));

			// add everything to the display
			displayPanel.setLayout(new GridBagLayout());
			int row = 0;
			displayPanel.add(messagePanel, new GBC(0, row).setSpan(1, 1)
					.setFill(GBC.BOTH).setWeight(0.0, 0.0).setAnchor(
							GBC.NORTHWEST).setInsets(1));
			row++;
			displayPanel.add(plot, new GBC(0, row).setSpan(1, 1).setFill(
					GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
					.setInsets(1));
			row++;
			displayPanel.add(saveAndLabelsPanel, new GBC(0, row).setSpan(1, 1)
					.setFill(GBC.BOTH).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
					.setInsets(1));
		}
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

			// save a header
			String[] header = {"Generation: ", "Speed (generations/second): ",
					"Avg. speed (generations/second): "};
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
		AttentionPanel attentionPanel = new AttentionPanel("Simulation Speed");

		MultilineLabel messageLabel = new MultilineLabel(INFO_MESSAGE);
		messageLabel.setFont(fonts.getAnalysesDescriptionFont());
		messageLabel.setMargin(new Insets(6, 10, 2, 16));

		JPanel messagePanel = new JPanel(new GridBagLayout());
		messagePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

		int row = 0;
		messagePanel.add(attentionPanel, new GBC(0, row).setSpan(1, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.NORTHWEST)
				.setInsets(1));

		row++;
		messagePanel.add(messageLabel, new GBC(0, row).setSpan(1, 1).setFill(
				GBC.HORIZONTAL).setWeight(0.0, 0.0).setAnchor(GBC.NORTH)
				.setInsets(1));

		return messagePanel;
	}

	/**
	 * Plots the average value data.
	 */
	private void plotData()
	{
		// set the min and max values
		if(speedList == null || speedList.size() == 0)
		{
			plot.setMaximumXValue(MAX_NUMBER_TO_PLOT - 1);
			plot.setMinimumXValue(0.0);
			plot.setMaximumYValue(maxYValue);
			plot.setMinimumYValue(0.0);
		}
		else
		{
			// get the max y value on the plot
			Point2D.Double point = speedList.getLast();
			if(point.y > maxYValue)
			{
				maxYValue = point.y;

				// add 5.0 and round. Prevents y-axis values from changing too
				// often.
				maxYValue = Math.round(maxYValue + 5.0);
			}
			plot.setMaximumYValue(maxYValue);

			Point2D firstPoint = (Point2D) speedList.getFirst();
			plot.setMaximumXValue(firstPoint.getX() + MAX_NUMBER_TO_PLOT - 1);
			plot.setMinimumXValue(firstPoint.getX());
			plot.setMinimumYValue(0.0);
		}

		// plot extra y-axis values
		int numExtraYValues = 9;
		double[] yValues = new double[numExtraYValues];
		for(int i = 0; i < yValues.length; i++)
		{
			// the 10.0 ensures 1 decimal place, but no more.
			yValues[i] = Math.round(10.0 * (i + 1)
					* (maxYValue / (numExtraYValues + 1))) / 10.0;
		}
		plot.setExtraYAxisValues(yValues);

		// plot extra x-axis values
		int numExtraXValues = 1;
		double[] xValues = new double[numExtraXValues];
		for(int i = 0; i < xValues.length; i++)
		{
			xValues[i] = (int) (plot.getMinimumXValue() + (int) Math
					.round((i + 1)
							* (MAX_NUMBER_TO_PLOT / (numExtraXValues + 1))));
		}
		plot.setExtraXAxisValues(xValues);

		plot.setXAxisLabel("generation");

		plot.setYAxisLabel("speed (generations/second)");

		plot.showPlotLines(false);

		plot.setPointDisplayColors(plotColors);

		plot.drawPoints(speedList);
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
	 * Plots the number of generations per second.
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
		// the current time
		long currentTime = System.nanoTime();

		// the start time
		if(firstTimeThrough)
		{
			firstTimeThrough = false;

			// the time at which the analysis started
			startGeneration = generation;
			startTime = currentTime;
			previousTime = startTime;
		}

		// calculate speeds in generations per millisecond, then divide
		// by 1000 to get speed in generations per second
		double speed = 0.0;
		if(currentTime != previousTime)
		{
			speed = 1.0 / (((double) (currentTime - previousTime)) / 1000000000.0);
		}

		double avgSpeed = 0.0;
		if(currentTime != startTime)
		{
			avgSpeed = ((double) (generation - startGeneration))
					/ (((double) (currentTime - startTime)) / 1000000000.0);
		}

		// save average speed and data in a linked list for plotting.
		// The speed data has to come second so that the plot data
		// properly finds the maxYValue (which will always be a
		// speed and never an average speed). Note: don't save the
		// start generation which has value 0.0.
		if(generation != startGeneration)
		{
			speedList.add(new Point2D.Double(generation, avgSpeed));
			speedList.add(new Point2D.Double(generation, speed));
		}

		if(speedList.size() > 2 * MAX_NUMBER_TO_PLOT)
		{
			// remove two elements, one for the speed and the other for the
			// average speed
			speedList.removeFirst();
			speedList.removeFirst();
		}

		// set the text for the labels
		generationDataLabel.setText("" + generation);

		// and set the text for the labels, but format!
		DecimalFormat myFormatter = new DecimalFormat(DECIMAL_PATTERN);
		String speedText = myFormatter.format(speed);
		speedDataLabel.setText(speedText);
		String avgSpeedText = myFormatter.format(avgSpeed);
		averageSpeedDataLabel.setText(avgSpeedText);

		// create an array of data to be saved
		data[0] = "" + generation;
		data[1] = "" + speed;
		data[2] = "" + avgSpeed;

		// see if user wants to save the data
		if(fileWriter != null)
		{
			// save it
			saveData(data);
		}

		// plot the memory usage data
		plotData();

		previousTime = currentTime;
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
	 * Overrides the parent's method to handle the notification of a start or
	 * stop in the simulation.
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		if(event.getPropertyName().equals(CurrentProperties.START))
		{
			boolean start = ((Boolean) event.getNewValue()).booleanValue();
			if(start)
			{
				// if the simulation is starting, then reset the analysis
				// (except for file writing)
				partialReset();
			}
		}
	}

	/**
	 * Performs any necessary operations to reset the analysis with the
	 * exception of closing the file. This method is called if the user resets
	 * the cellular automata, selects a new simulation, or stops and restarts
	 * the simulation.
	 */
	public void partialReset()
	{
		// indicate that this will be the first time through the analyze method
		firstTimeThrough = true;

		// empty the plot list (so that old data doesn't get plotted again when
		// the new simulation starts)
		speedList.clear();

		// reset the y-axis of the plot
		maxYValue = DEFAULT_MAX_Y_VALUE;

		// reset the plot (only remove the data points)
		plot.clearDataPoints();
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

		// all other non-file related resets
		partialReset();
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