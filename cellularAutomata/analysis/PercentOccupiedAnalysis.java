/*
 PercentOccupiedAnalysis -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.analysis;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
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

import cellularAutomata.CAController;
import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.MultilineLabel;
import cellularAutomata.util.SimplePlot;
import cellularAutomata.util.files.FileWriter;

/**
 * Finds the percentage of sites that are occupied (full) at each generation.
 * <p>
 * Students of software engineering will notice that this class is not very
 * flexible to changes. Ideally, an arbitrary array of displaying properties
 * should be created. But this class was written to be relatively easy to
 * understand, rather than flexible. In fact, my intent is that this class
 * should be easy to understand with only basic knowledge of OO programming.
 * That way, this class can be used as a template of sorts for anyone that
 * desires to write their own analysis.
 * 
 * @author David Bahr
 */
public class PercentOccupiedAnalysis extends Analysis implements ActionListener
{
	// the maximum number of elements that will be plotted
	private static final int MAX_NUMBER_TO_PLOT = 100;

	// a brief display name for this class
	private static final String ANALYSIS_NAME = "Percent Occupied Sites";

	// The pattern used to display decimals, particularly for the percent
	// population.
	private static final String DECIMAL_PATTERN = "0.000";

	// display info for this class
	private static final String INFO_MESSAGE = "Keeps track of the number "
			+ "of cells that are occupied.  If a cell has any value other than "
			+ "the empty value, then it is counted as occupied.";

	// the action command for saving the data and the label used by the "save
	// data" check box
	private static final String SAVE_DATA = "   Save the data";

	// a tooltip for the save data check box
	private static final String SAVE_DATA_TOOLTIP = "<html>Saves data "
			+ "to a file (saves <br> every generation while the box is checked).</html>";

	// a brief tooltip description for this class
	private static final String TOOLTIP = "<html>count occupied cells</html>";

	// If the user wants to save the data to a file, this will be instantiated
	private FileWriter fileWriter = null;

	// fonts for display
	private Fonts fonts = new Fonts();

	// The check box that lets the user save the data
	private JCheckBox saveDataCheckBox = null;

	// label for the current generation
	private JLabel generationDataLabel = null;

	// label for the number of occupied cells
	private JLabel numberOccupiedDataLabel = null;

	// label for the percentage of occupied cells
	private JLabel percentOccupiedDataLabel = null;

	// the panel where results are displayed
	private JPanel displayPanel = null;

	// the list of points that will be drawn on the plot
	private LinkedList<Point2D.Double> percentOccupiedList = new LinkedList<Point2D.Double>();

	// a panel that plots the population data
	private SimplePlot plot = null;

	// a delimiter for spacing data in the data file
	private String delimiter = null;

	private String[] data = new String[3];

	/**
	 * Create an analyzer that counts the number of occupied site.
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
	public PercentOccupiedAnalysis(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			// this is the panel that will be displayed (getDisplayPanel() will
			// return the panel that this creates)
			createDisplayPanel();
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
			numberOccupiedDataLabel = new JLabel("");
			percentOccupiedDataLabel = new JLabel("");
		}
	}

	/**
	 * Create the panel used to display the population statistics.
	 */
	private void createDisplayPanel()
	{
		if(displayPanel == null)
		{
			// create the display panel
			displayPanel = new JPanel(new BorderLayout());
			displayPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			displayPanel.setPreferredSize(new Dimension(
					CAFrame.tabbedPaneDimension.width, 600));

			// create a panel that displays messages
			JPanel messagePanel = createMessagePanel();

			// create the labels for the display
			createDataDisplayLabels();
			JLabel generationLabel = new JLabel("Generation:   ");
			JLabel numOccupiedLabel = new JLabel("Number of occupied cells:   ");
			JLabel percentOccupiedLabel = new JLabel(
					"Percentage of occupied cells:   ");

			// create boxes for each column of the display (a Box uses the
			// BoxLayout, so it is handy for laying out components)
			Box boxOfNameLabels = Box.createVerticalBox();
			Box boxOfDataLabels = Box.createVerticalBox();

			// the amount of vertical space to put between components
			int verticalSpace = 5;

			// add the name labels to the first vertical box
			boxOfNameLabels.add(generationLabel);
			boxOfNameLabels.add(Box.createVerticalStrut(verticalSpace));
			boxOfNameLabels.add(numOccupiedLabel);
			boxOfNameLabels.add(Box.createVerticalStrut(verticalSpace));
			boxOfNameLabels.add(percentOccupiedLabel);

			// add the data labels to the second vertical box
			boxOfDataLabels.add(generationDataLabel);
			boxOfDataLabels.add(Box.createVerticalStrut(verticalSpace));
			boxOfDataLabels.add(numberOccupiedDataLabel);
			boxOfDataLabels.add(Box.createVerticalStrut(verticalSpace));
			boxOfDataLabels.add(percentOccupiedDataLabel);

			// create another box that holds both of the label boxes
			Box boxOfLabels = Box.createHorizontalBox();
			boxOfLabels.add(boxOfNameLabels);
			boxOfLabels.add(boxOfDataLabels);
			boxOfLabels.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));

			// create a "save data" check box
			saveDataCheckBox = new JCheckBox(SAVE_DATA);
			saveDataCheckBox.setToolTipText(SAVE_DATA_TOOLTIP);
			saveDataCheckBox.setActionCommand(SAVE_DATA);
			saveDataCheckBox.addActionListener(this);
			JPanel saveDataPanel = new JPanel();
			saveDataPanel
					.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
			saveDataPanel.add(saveDataCheckBox);

			// create a panel that plots the data
			plot = new SimplePlot();

			// create a panel that holds both the boxOfLabels and the save
			// button
			JPanel saveAndLabelsPanel = new JPanel(new BorderLayout());
			saveAndLabelsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5,
					5, 5));
			saveAndLabelsPanel.add(boxOfLabels, BorderLayout.NORTH);
			saveAndLabelsPanel.add(saveDataPanel, BorderLayout.CENTER);

			// add everything to the display (using BorderLayout)
			displayPanel.add(messagePanel, BorderLayout.NORTH);
			displayPanel.add(plot, BorderLayout.CENTER);
			displayPanel.add(saveAndLabelsPanel, BorderLayout.SOUTH);
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
			String[] header = {"Generation: ", "Number occupied: ",
					"Percentage occupied: "};
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
		AttentionPanel attentionPanel = new AttentionPanel("Percent Occupied");

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
	 * Plots the population data.
	 */
	private void plotData()
	{
		// set the min and max values on the plot
		Point2D firstPoint = (Point2D) percentOccupiedList.getFirst();
		plot.setMaximumXValue(firstPoint.getX() + MAX_NUMBER_TO_PLOT - 1);
		plot.setMaximumYValue(1.0);
		plot.setMinimumXValue(firstPoint.getX());
		plot.setMinimumYValue(0.0);
		plot.setXAxisLabel("generation");
		plot.setYAxisLabel("% occupied");

		// draw some extra points on the y-axis (looks good)
		double[] yValues = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
		plot.setExtraYAxisValues(yValues);

		plot.drawPoints(percentOccupiedList);
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
		// the total number of cells
		int totalNumberOfCells = 0;

		// the number of occupied cells
		int numOccupied = 0;

		// the percentage of occupied cells
		double percentOccupied = 0;

		// get an iterator over the lattice
		Iterator cellIterator = lattice.iterator();

		// get each cell on the lattice
		Cell cell = null;
		while(cellIterator.hasNext())
		{
			// add one more to the total number of cells
			totalNumberOfCells++;

			// get the cell
			cell = (Cell) cellIterator.next();

			// get its state.
			CellState state = (CellState) cell.getState(generation);

			// is the state "occupied"?
			if(!state.isEmpty())
			{
				numOccupied++;
			}
		}

		// calculate the percent occupied
		percentOccupied = numOccupied / (double) totalNumberOfCells;

		// save percentOccupied in a linked list for plotting
		percentOccupiedList
				.add(new Point2D.Double(generation, percentOccupied));
		if(percentOccupiedList.size() > MAX_NUMBER_TO_PLOT)
		{
			percentOccupiedList.removeFirst();
		}

		// set the text for the labels
		generationDataLabel.setText("" + generation);
		numberOccupiedDataLabel.setText("" + numOccupied);

		// and set the text for the percent label, but format!
		DecimalFormat myFormatter = new DecimalFormat(DECIMAL_PATTERN);
		String output = myFormatter.format(percentOccupied);
		percentOccupiedDataLabel.setText(output);

		// create an array of data to be saved
		data[0] = "" + generation;
		data[1] = "" + numOccupied;
		data[2] = "" + percentOccupied;

		// see if user wants to save the data
		if(fileWriter != null)
		{
			// save it
			saveData(data);
		}

		// plot the percent occupied data
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

		// empty the plot list (so that old data doesn't get plotted again when
		// the new simulation starts)
		percentOccupiedList.clear();

		// reset the plot
		plot.clearPlot();
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
