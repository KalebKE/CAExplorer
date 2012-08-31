/*
 AverageValueAnalysis -- a class within the Cellular Automaton Explorer. 
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

import java.awt.Color;
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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import cellularAutomata.CAController;
import cellularAutomata.CurrentProperties;
import cellularAutomata.Cell;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.view.CellStateView;
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
import cellularAutomata.util.math.Complex;

/**
 * Finds the average value of all sites. If the analysis is not numerically
 * based, then it tells the user that an average is not possible.
 * 
 * @author David Bahr
 */
public class AverageValueAnalysis extends Analysis implements ActionListener
{
	// the maximum number of elements that will be plotted
	private static final int MAX_NUMBER_TO_PLOT = 100;

	// a brief display name for this class
	private static final String ANALYSIS_NAME = "Average Value";

	// title for the subpanel that displays the data
	private static final String DATA_PANEL_TITLE = "Data";

	// The pattern used to display decimals, particularly for the percent
	// population.
	private static final String DECIMAL_PATTERN = "0.000";

	// display info for this class
	private static final String INFO_MESSAGE = "Finds the average value of all cells.  "
			+ "In one-dimensional simulations, only the last row is averaged.  If the "
			+ "cell values are complex numbers, then the complex modulus is plotted.";

	// displayed (and saved) when the cell's do not hold numbers.
	private static final String NOT_A_NUMBER_TEXT = "Cells are not #'s.";

	// the action command for saving the data and the label used by the "save
	// data" check box
	private static final String SAVE_DATA = "   Save the data";

	// a tooltip for the save data check box
	private static final String SAVE_DATA_TOOLTIP = "<html>Saves average value data "
			+ "to a file (saves <br> every generation while the box is checked).</html>";

	// a brief tooltip description for this class
	private static final String TOOLTIP = "<html>finds average value of all cells</html>";

	// true if the cell's hold any standard kind of number or complex
	// number
	private boolean isANumber = true;

	// true if the CA is complex number based (usually false)
	private boolean isComplexState = false;

	// true if the CA is integer based
	private boolean isIntegerState = true;

	// color of titles of sections
	private Color titleColor = Color.BLUE;

	// the maximum Y value on the plot
	private double maxYValue = 1.0;

	// the minimum Y value on the plot
	private double minYValue = 0.0;

	// If the user wants to save the data to a file, this will be instantiated
	private FileWriter fileWriter = null;

	// fonts for display
	private Fonts fonts = new Fonts();

	// title font (for titles of sections)
	private Font titleFont = new Fonts().getItalicSmallerFont();

	// the number of states
	private int numStates = 2;

	// The check box that lets the user save the data
	private JCheckBox saveDataCheckBox = null;

	// label for the current generation
	private JLabel generationDataLabel = null;

	// label for the average value of the cells
	private JLabel averageValueDataLabel = null;

	// the panel where results are displayed
	private JPanel displayPanel = null;

	// the list of points that will be drawn on the plot
	private LinkedList<Point2D.Double> averageValueList = new LinkedList<Point2D.Double>();

	// a panel that plots the average value data
	private SimplePlot plot = null;

	// a delimiter for spacing data in the data file
	private String delimiter = null;

	// the data that will be saved to a file
	private String[] data = new String[2];

	/**
	 * Create an analyzer that finds the average value of all cells.
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
	public AverageValueAnalysis(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			numStates = CurrentProperties.getInstance().getNumStates();

			// this is the panel that will be displayed (getDisplayPanel() will
			// return the panel that this creates)
			createDisplayPanel();
		}
	}

	/**
	 * Create labels used to display the data for the average value.
	 */
	private void createDataDisplayLabels()
	{
		// if one is null, then they all are
		if(generationDataLabel == null)
		{
			generationDataLabel = new JLabel("");
			averageValueDataLabel = new JLabel("");
		}
	}

	/**
	 * Create the panel used to display the average value info.
	 */
	private void createDisplayPanel()
	{
		if(displayPanel == null)
		{
			// create the display panel
			displayPanel = new JPanel(new GridBagLayout());
			displayPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			// create a panel that displays messages
			JPanel messagePanel = createMessagePanel();

			// create the labels for the display
			createDataDisplayLabels();
			JLabel generationLabel = new JLabel("Generation:   ");
			JLabel averageValueLabel = new JLabel(
					"Average value of all cells:   ");

			// create boxes for each column of the display (a Box uses the
			// BoxLayout, so it is handy for laying out components)
			Box boxOfNameLabels = Box.createVerticalBox();
			Box boxOfDataLabels = Box.createVerticalBox();

			// the amount of vertical space to put between components
			int verticalSpace = 5;

			// add the name labels to the first vertical box
			boxOfNameLabels.add(generationLabel);
			boxOfNameLabels.add(Box.createVerticalStrut(verticalSpace));
			boxOfNameLabels.add(averageValueLabel);

			// add the data labels to the second vertical box
			boxOfDataLabels.add(generationDataLabel);
			boxOfDataLabels.add(Box.createVerticalStrut(verticalSpace));
			boxOfDataLabels.add(averageValueDataLabel);

			// create another box that holds both of the label boxes
			Box boxOfLabels = Box.createHorizontalBox();
			boxOfLabels.add(boxOfNameLabels);
			boxOfLabels.add(boxOfDataLabels);
			boxOfLabels.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createCompoundBorder(BorderFactory
							.createEtchedBorder(), BorderFactory
							.createEmptyBorder(7, 7, 7, 7)), DATA_PANEL_TITLE,
					TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION,
					titleFont, titleColor));

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
			String[] header = {"Generation: ", "Average value: "};
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
			// example, the user canceled and did not choose any file when
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
		AttentionPanel attentionPanel = new AttentionPanel("Average Value");

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
		if(isIntegerState)
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
			upColor = view.getDisplayColor(state, null, null);

			if(roundUp != roundDown || cellState == 0.0)
			{
				state.setToEmptyState();
			}
			downColor = view.getDisplayColor(state, null, null);
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
	 * Plots the average value data.
	 */
	private void plotData()
	{
		// set the min and max values
		if(averageValueList == null || averageValueList.size() == 0)
		{
			plot.setMaximumXValue(MAX_NUMBER_TO_PLOT - 1);
			plot.setMinimumXValue(0.0);
			plot.setMaximumYValue(1.0);
			plot.setMinimumYValue(0.0);
		}
		else
		{
			// set the min and max values on the plot
			Point2D firstPoint = (Point2D) averageValueList.getFirst();
			plot.setMaximumXValue(firstPoint.getX() + MAX_NUMBER_TO_PLOT - 1);
			plot.setMinimumXValue(firstPoint.getX());

			if(isIntegerState)
			{
				plot.setMaximumYValue(numStates - 1.0);
				plot.setMinimumYValue(0.0);

				int numExtraValues = 3;
				double[] yValues = new double[numExtraValues];
				for(int i = 0; i < yValues.length; i++)
				{
					yValues[i] = (i + 1)
							* ((numStates - 1.0) / (numExtraValues + 1));
				}
				plot.setExtraYAxisValues(yValues);

				// and lets add some color
				Color[] colorArray = new Color[averageValueList.size()];
				String ruleClassName = CurrentProperties.getInstance()
						.getRuleClassName();
				Rule rule = ReflectionTool
						.instantiateMinimalRuleFromClassName(ruleClassName);

				int position = 0;
				Iterator<Point2D.Double> iterator = averageValueList.iterator();
				while(iterator.hasNext())
				{
					// get the state value
					double cellState = iterator.next().y;

					Color stateColor = getColorFromFractionalStateValue(
							cellState, rule);

					// assign the average color
					colorArray[position] = stateColor;

					// go to the next array element
					position++;
				}
				plot.setPointDisplayColors(colorArray);
			}
			else
			{
				// get the min and max (and base it on previous values as well)
				Iterator iterator = averageValueList.iterator();
				while(iterator.hasNext())
				{
					double y = ((Point2D) iterator.next()).getY();
					maxYValue = Math.max(maxYValue, y);
					minYValue = Math.min(minYValue, y);
				}

				plot.setMaximumYValue(Math.ceil(maxYValue));
				plot.setMinimumYValue(Math.floor(minYValue));
			}
		}

		plot.setXAxisLabel("generation");
		if(!isComplexState)
		{
			plot.setYAxisLabel("average");
		}
		else
		{
			plot.setYAxisLabel("modulus of avg.");
		}

		plot.drawPoints(averageValueList);
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
	 * Finds the average value of the cells.
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
		// the total number of cells
		int totalNumberOfCells = 0;

		// the average value of all the cells
		double averageValue = 0.0;

		// the average imaginary value of all the cells. THis will always be 0.0
		// unless the cell's hold complex numbers.
		double averageImaginaryValue = 0.0;

		// get an iterator over the lattice
		Iterator cellIterator = lattice.iterator();

		// get each cell on the lattice
		Cell cell = null;
		while(cellIterator.hasNext() && isANumber)
		{
			// add one more to the total number of cells
			totalNumberOfCells++;

			// get the cell
			cell = (Cell) cellIterator.next();

			// get its state value
			Object stateValue = ((CellState) cell.getState(generation))
					.getValue();

			// keep a running total
			if(stateValue != null)
			{
				// ok, I don't like these instanceof's any more than you do. But
				// it seems straightforward in this case. Another option is to
				// use getString and parse to a number, but that will be really
				// slow.
				if(stateValue instanceof Number)
				{
					averageValue += ((Number) stateValue).doubleValue();
				}
				else if(stateValue instanceof Complex)
				{
					averageValue += ((Complex) stateValue).getReal();
					averageImaginaryValue += ((Complex) stateValue)
							.getImaginary();

					isComplexState = true;
				}
				else
				{
					// it isn't any standard kind of number
					isANumber = false;
				}

				// used when finding min/max values for plotting
				if(stateValue instanceof Integer)
				{
					isIntegerState = true;
				}
				else
				{
					isIntegerState = false;
				}
			}
		}

		// calculate the average value
		if(isANumber)
		{
			averageValue /= (double) totalNumberOfCells;
			averageImaginaryValue /= (double) totalNumberOfCells;
		}

		// save average value in a linked list for plotting
		if(isANumber && !isComplexState)
		{
			averageValueList.add(new Point2D.Double(generation, averageValue));
		}
		else if(isComplexState)
		{
			// for complex numbers, plot the modulus of the average
			averageValueList.add(new Point2D.Double(generation, new Complex(
					averageValue, averageImaginaryValue).modulus()));
		}

		if(averageValueList.size() > MAX_NUMBER_TO_PLOT)
		{
			averageValueList.removeFirst();
		}

		// set the text for the labels
		generationDataLabel.setText("" + generation);

		// and set the text for the average value label, but format!
		DecimalFormat myFormatter = new DecimalFormat(DECIMAL_PATTERN);
		String averageValueText = myFormatter.format(averageValue);
		String averageImaginaryValueText = myFormatter
				.format(averageImaginaryValue);
		if(isComplexState)
		{
			String averageComplexText = new Complex(Double
					.parseDouble(averageValueText), Double
					.parseDouble(averageImaginaryValueText)).toString();
			averageValueDataLabel.setText(averageComplexText);
			data[1] = averageComplexText;
		}
		else if(isANumber)
		{
			averageValueDataLabel.setText(averageValueText);
			data[1] = "" + averageValue;
		}
		else
		{
			averageValueDataLabel.setText(NOT_A_NUMBER_TEXT);
			data[1] = NOT_A_NUMBER_TEXT;
		}

		// create an array of data to be saved
		data[0] = "" + generation;

		// see if user wants to save the data
		if(fileWriter != null)
		{
			// save it
			saveData(data);
		}

		// plot the average value data
		plotData();
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
	 * Overrides the parent's method to handle the notification of a change in
	 * color.
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		if(event.getPropertyName().equals(CurrentProperties.COLORS_CHANGED))
		{
			// replot in the new colors
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

		// reset the number of states
		numStates = CurrentProperties.getInstance().getNumStates();

		// empty the plot list (so that old data doesn't get plotted again when
		// the new simulation starts)
		averageValueList.clear();

		// reset the plot
		plot.clearPlot();
		minYValue = 0.0;
		maxYValue = 1.0;

		// reset booleans
		isIntegerState = true;
		isComplexState = false;
		isANumber = true;
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
