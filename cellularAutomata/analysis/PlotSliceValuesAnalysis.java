/*
 PlotSliceValuesAnalysis -- a class within the Cellular Automaton Explorer. 
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
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.text.DecimalFormat;
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
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cellularAutomata.CAController;
import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.model.RealValuedState;
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
 * Plots the values of a slice through a one- or two-dimensional analysis.
 * 
 * @author David Bahr
 */
public class PlotSliceValuesAnalysis extends Analysis implements ActionListener
{
	// a brief display name for this class
	private static final String ANALYSIS_NAME = "Graph Values";

	// the colored title on the display
	private static final String ATTENTION_PANEL_TITLE = "Graph Values";

	// The tooltip for the column slice
	private static final String COLUMN_TIP = "<html>Choose the column of the vertical "
			+ "slice.</html>";

	// The text for the horizontal radio button
	private static final String HORIZONTAL_SLICE = "Horizontal " + "slice";

	// The tooltip for the horizontal radio button
	private static final String HORIZONTAL_SLICE_TOOLTIP = "<html>Show a horizontal "
			+ "slice through the CA.</html>";

	// display info for this class
	private static final String INFO_MESSAGE = "Graphs values of a one-dimensional slice "
			+ "through a one- or two-dimensional simulation. The numerical values of "
			+ "the highlighted cells are plotted at each time step. \n\n"
			+ "This analysis is particularly useful when looking at real-valued CA.";

	// the label for the "plot lines only" check box
	private static final String PLOT_LINES_ONLY = "   Plot lines only (no circles for data)";

	// the tooltip for the "plotlines only" check box
	private static final String PLOT_LINES_ONLY_TOOLTIP = "When checked, only plots lines "
			+ "between the data.";

	// Display title for the row-column panel.
	private static final String ROW_COL_PANEL_TITLE = "Choose a slice (2-d only).";

	// The tooltip for the row position
	private static final String ROW_TIP = "<html>Choose the row of the "
			+ "slice.</html>";

	// the action command for saving the data and the label used by the "save
	// data" check box
	private static final String SAVE_DATA = "   Save the data";

	// a tooltip for the save data check box
	private static final String SAVE_DATA_TOOLTIP = "<html>Saves plot data "
			+ "to a file (saves <br> every generation while the box is checked).</html>";

	// a brief tooltip description for this class
	private static final String TOOLTIP = "<html><body>plot values of a slice of "
			+ "cells through a one- or two-dimensional CA</body></html>";

	// The text for the column radio button
	private static final String VERTICAL_SLICE = "Vertical slice";

	// The tooltip for the column radio button
	private static final String VERTICAL_SLICE_TOOLTIP = "<html>Show a vertical "
			+ "slice through the CA.</html>";

	// display info/warning for this class when not a number based rule
	private static final String WARNING_MESSAGE = "Warning, this analysis only works "
			+ "with integer and real-valued rules.  The current rule does not use "
			+ "integers or real values.";

	// only true the first time the analyze method is called
	private boolean firstTimeThrough = true;

	// when true, the plot will not show the data points and will only show the
	// lines between the data points
	private boolean showLinesOnly = false;

	// true when the rule uses integers rather than some other type of number or
	// value
	private boolean isIntegerRule = true;

	// the cells in the slice
	private Cell[] sliceCells = null;

	// color of titles of sections
	private Color titleColor = Color.BLUE;

	// the minimum possible y value on the plot
	private double emptyYValue = 0.0;

	// the maximum possible y value on the plot
	private double fullYValue = 0.0;

	// only true if the rule uses real or integer values
	private boolean isCompatibleRule = true;

	// if the user wants to save the data to a file, this will be instantiated
	private FileWriter fileWriter = null;

	// column of the slice being analyzed
	private int column = 0;

	// row of the slice being analyzed
	private int row = 0;

	// fonts for display
	private Fonts fonts = new Fonts();

	// title font (for titles of sections)
	private Font titleFont = new Fonts().getItalicSmallerFont();

	// The check box that lets the user plot the zero state
	private JCheckBox plotLinesOnlyCheckBox = null;

	// The check box that lets the user save the data
	private JCheckBox saveDataCheckBox = null;

	// the label for the col spinner
	private JLabel colLabel = null;

	// the label for the row spinner
	private JLabel rowLabel = null;

	// the panel where results are displayed
	private JPanel displayPanel = null;

	// button for selecting vertical slices
	private JRadioButton columnSliceButton = null;

	// button for selecting horizontal slices
	private JRadioButton rowSliceButton = null;

	// selects the column position of the slice
	private JSpinner colSpinner = null;

	// selects the row of the slice
	private JSpinner rowSpinner = null;

	// the list of points that will be drawn on the plot
	private LinkedList<Point2D.Double> sliceValueList = new LinkedList<Point2D.Double>();

	// a panel that plots the population data
	private SimplePlot plot = null;

	// a delimiter for spacing data in the data file
	private String delimiter = null;

	// data that will be saved to a file
	private String[] data = null;

	/**
	 * Create an analysis that plots the values of a one-dimensional slice
	 * through a one- or two-dimensional CA.
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
	public PlotSliceValuesAnalysis(boolean minimalOrLazyInitialization)
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
	 * Create the panel used to display the generated number statistics.
	 */
	private void createDisplayPanel()
	{
		// make sure the current rule is a compatible rule
		String ruleClassName = CurrentProperties.getInstance()
				.getRuleClassName();
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(ruleClassName);
		isCompatibleRule = IntegerCellState.isCompatibleRule(rule)
				|| RealValuedState.isCompatibleRule(rule);

		int displayWidth = CAFrame.tabbedPaneDimension.width;
		int displayHeight = 750;

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

		if(isCompatibleRule)
		{
			// create a panel that displays messages
			JPanel messagePanel = createMessagePanel();

			// panel to select a slice
			JPanel selectSlicePanel = createSpinnersPanel();

			// create a "plot lines only" check box
			plotLinesOnlyCheckBox = new JCheckBox(PLOT_LINES_ONLY);
			plotLinesOnlyCheckBox.setSelected(false);
			plotLinesOnlyCheckBox.setToolTipText(PLOT_LINES_ONLY_TOOLTIP);
			plotLinesOnlyCheckBox.setActionCommand(PLOT_LINES_ONLY);
			plotLinesOnlyCheckBox.addActionListener(this);
			JPanel plotLinesOnlyPanel = new JPanel(new BorderLayout());
			plotLinesOnlyPanel.setBorder(BorderFactory.createEmptyBorder(7, 7,
					7, 7));
			plotLinesOnlyPanel.add(BorderLayout.CENTER, plotLinesOnlyCheckBox);

			// create a "save data" check box
			saveDataCheckBox = new JCheckBox(SAVE_DATA);
			saveDataCheckBox.setToolTipText(SAVE_DATA_TOOLTIP);
			saveDataCheckBox.setActionCommand(SAVE_DATA);
			saveDataCheckBox.addActionListener(this);
			JPanel saveDataPanel = new JPanel(new BorderLayout());
			saveDataPanel
					.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
			saveDataPanel.add(BorderLayout.CENTER, saveDataCheckBox);

			// create a panel that plots the slice data
			plot = new SimplePlot();

			// add all the components to the panel
			int row = 0;
			displayPanel.add(messagePanel, new GBC(1, row).setSpan(4, 1)
					.setFill(GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(
							GBC.WEST).setInsets(1));

			row++;
			displayPanel.add(plot, new GBC(1, row).setSpan(4, 1).setFill(
					GBC.BOTH).setWeight(10.0, 10.0).setAnchor(GBC.WEST)
					.setInsets(1));

			row++;
			displayPanel.add(plotLinesOnlyPanel, new GBC(1, row).setSpan(4, 1)
					.setFill(GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(
							GBC.WEST).setInsets(1));

			row++;
			displayPanel.add(selectSlicePanel, new GBC(1, row).setSpan(4, 1)
					.setFill(GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(
							GBC.WEST).setInsets(1));

			row++;
			displayPanel.add(saveDataPanel, new GBC(1, row).setSpan(4, 1)
					.setFill(GBC.NONE).setWeight(1.0, 1.0)
					.setAnchor(GBC.CENTER).setInsets(1));

			// this will fill empty space
			row++;
			displayPanel.add(new JLabel(" "), new GBC(1, row).setSpan(4, 1)
					.setFill(GBC.HORIZONTAL).setWeight(2.0, 2.0).setAnchor(
							GBC.WEST).setInsets(1));
		}
		else
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
			header[0] = "Generation";
			for(int i = 1; i < header.length; i++)
			{
				header[i] = "column " + (i - 1) + ":";
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
		AttentionPanel attentionPanel = new AttentionPanel(
				ATTENTION_PANEL_TITLE);

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
	 * Create row and column text fields and puts in a JPanel.
	 * 
	 * @return contains the row and column text fields.
	 */
	private JPanel createSpinnersPanel()
	{
		// create a radio button for vertical slices
		columnSliceButton = new JRadioButton(VERTICAL_SLICE);
		columnSliceButton.setSelected(true);
		columnSliceButton.setToolTipText(VERTICAL_SLICE_TOOLTIP);
		columnSliceButton.setActionCommand(VERTICAL_SLICE);
		columnSliceButton.addActionListener(this);
		columnSliceButton.setFont(fonts.getPlainFont());

		// create a radio button for horizontal slices
		rowSliceButton = new JRadioButton(HORIZONTAL_SLICE);
		rowSliceButton.setSelected(true);
		rowSliceButton.setToolTipText(HORIZONTAL_SLICE_TOOLTIP);
		rowSliceButton.setActionCommand(HORIZONTAL_SLICE);
		rowSliceButton.addActionListener(this);
		rowSliceButton.setFont(fonts.getPlainFont());

		// create a group of radio buttons
		ButtonGroup group = new ButtonGroup();
		group.add(rowSliceButton);
		group.add(columnSliceButton);
		rowSliceButton.setSelected(true);

		// create a label for the column position
		colLabel = new JLabel("Column: ");
		colLabel.setFont(fonts.getPlainFont());

		// create a label for the row position
		rowLabel = new JLabel("Row: ");
		rowLabel.setFont(fonts.getPlainFont());

		// width and height of the lattice
		int width = CurrentProperties.getInstance().getNumColumns();
		int height = CurrentProperties.getInstance().getNumRows();

		// set the row and column
		boolean isOneDimensional = OneDimensionalLattice
				.isCurrentLatticeOneDim();
		if(!isOneDimensional)
		{
			this.row = height / 2;
			this.column = width / 2;
		}
		else
		{
			this.row = 0;
			this.column = 0;
		}

		// create spinners for the row and column
		SpinnerNumberModel colModel = new SpinnerNumberModel(column + 1, 1,
				width, 1);
		colSpinner = new JSpinner(colModel);
		colSpinner.setToolTipText(COLUMN_TIP);
		colSpinner.addChangeListener(new RowColListener());

		SpinnerNumberModel rowModel = new SpinnerNumberModel(row + 1, 1,
				height, 1);
		rowSpinner = new JSpinner(rowModel);
		rowSpinner.setToolTipText(ROW_TIP);
		rowSpinner.addChangeListener(new RowColListener());

		// is lattice one-dimensional?
		if(isOneDimensional)
		{
			// has to be two-dimensional
			colSpinner.setEnabled(false);
			colLabel.setEnabled(false);
			rowSpinner.setEnabled(false);
			rowLabel.setEnabled(false);

			columnSliceButton.setEnabled(false);
			rowSliceButton.setEnabled(false);
		}
		else
		{
			// make the row spinner enabled as the default. Both radio buttons
			// are enabled
			colSpinner.setEnabled(false);
			colLabel.setEnabled(false);
			rowSpinner.setEnabled(true);
			rowLabel.setEnabled(true);

			columnSliceButton.setEnabled(true);
			rowSliceButton.setEnabled(true);
		}

		// a Box uses the BoxLayout, so it is handy for laying out components
		Box rowBox = Box.createHorizontalBox();
		Box colBox = Box.createHorizontalBox();

		// the amount of horizontal space to put between components
		int horizontalSpace1 = 1;
		int horizontalSpace2 = 20;

		// add the spinners to the boxes
		rowBox.add(rowSliceButton);
		rowBox.add(Box.createHorizontalStrut(horizontalSpace2));
		rowBox.add(rowLabel);
		rowBox.add(Box.createHorizontalStrut(horizontalSpace1));
		rowBox.add(rowSpinner);

		colBox.add(columnSliceButton);
		colBox.add(Box.createHorizontalStrut(horizontalSpace2));
		colBox.add(colLabel);
		colBox.add(Box.createHorizontalStrut(horizontalSpace1));
		colBox.add(colSpinner);

		// add all components to a single panel
		JPanel selectCellsPanel = new JPanel(new GridBagLayout());
		Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 5);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), ROW_COL_PANEL_TITLE, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		Border compoundBorder = BorderFactory.createCompoundBorder(
				titledBorder, emptyBorder);
		selectCellsPanel.setBorder(compoundBorder);

		int row = 0;
		selectCellsPanel.add(rowBox, new GBC(0, row).setSpan(2, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.CENTER).setInsets(
				1, 1, 20, 1));

		row++;
		selectCellsPanel.add(colBox, new GBC(0, row).setSpan(2, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.CENTER).setInsets(
				1, 1, 1, 1));

		return selectCellsPanel;
	}

	/**
	 * Creates a warning message.
	 * 
	 * @return A panel containing the warning message.
	 */
	private JPanel createWarningMessagePanel()
	{
		// a "grab their attention" panel
		AttentionPanel attentionPanel = new AttentionPanel(
				ATTENTION_PANEL_TITLE);

		MultilineLabel messageLabel = null;
		messageLabel = new MultilineLabel(WARNING_MESSAGE);

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
	 * Gets the cells from the lattice that are used in the slice.
	 * 
	 * @param lattice
	 *            The CA lattice containing the CA cells.
	 */
	private void loadCellsBeingAnalyzed(Lattice lattice)
	{
		// get an iterator over the lattice
		Iterator cellIterator = lattice.iterator();

		// Ignore cells until we get to the correct position.
		// Go to the correct row.
		if(rowSliceButton.isSelected())
		{
			// the width of the lattice
			int width = CurrentProperties.getInstance().getNumColumns();

			for(int i = 0; i < row * width; i++)
			{
				// ignore these cells
				cellIterator.next();
			}

			// Now get this row!
			for(int i = 0; i < width; i++)
			{
				this.sliceCells[i] = (Cell) cellIterator.next();
			}
		}
		else
		{
			// the height of the lattice
			int height = CurrentProperties.getInstance().getNumRows();
			int width = CurrentProperties.getInstance().getNumColumns();

			for(int i = 0; i < height; i++)
			{
				// ignore these cells (skip cells in this row until we get to
				// the correct column)
				for(int j = 0; j < column; j++)
				{
					cellIterator.next();
				}

				// now get this cell!
				this.sliceCells[i] = (Cell) cellIterator.next();

				// ignore these cells (skip the rest of the cells in the row)
				for(int j = column + 1; j < width; j++)
				{
					cellIterator.next();
				}
			}

		}
	}

	/**
	 * plots (graphs) the slice data.
	 */
	private void plotData()
	{
		int numberOfCells = 1;
		if(sliceCells != null)
		{
			numberOfCells = sliceCells.length;
		}

		// set the min and max x values on the plot
		plot.setMinimumXValue(0);
		plot.setMaximumXValue(numberOfCells - 1);

		// set the min and max y values on the plot
		double maxYValue = fullYValue;
		double minYValue = emptyYValue;

		// Iterator<Point2D.Double> iterator = sliceValueList.iterator();
		// while(iterator.hasNext())
		// {
		// Point2D.Double point = iterator.next();
		// if(point.y > maxYValue)
		// {
		// maxYValue = point.y;
		// }
		//
		// if(point.y < minYValue)
		// {
		// minYValue = point.y;
		// }
		// }

		// set that max and min value
		plot.setMaximumYValue(maxYValue);
		plot.setMinimumYValue(minYValue);

		// set the point size (minimal or default)
		if(showLinesOnly)
		{
			plot.setRadius(0.0);
		}
		else
		{
			plot.setRadius(plot.getDefaultRadius());
		}

		// set plot axes labels
		plot.setXAxisLabel("position");
		plot.setYAxisLabel("value");

		// draw some extra points on the x and y axes (looks good)
		// make cleaner
		if(isIntegerRule && maxYValue > 1)
		{
			plot.showYValuesAsInts(true);
			int numberOfExtraYPoints = (int) maxYValue - 1;
			double[] yValues = new double[numberOfExtraYPoints];
			for(int i = 0; i < yValues.length; i++)
			{
				yValues[i] = i + 1;
			}
			plot.setExtraYAxisValues(yValues);
		}
		else
		{
			int numberOfExtraYPoints = 9;
			double[] yValues = new double[numberOfExtraYPoints];
			for(int i = 0; i < yValues.length; i++)
			{
				yValues[i] = minYValue + (i + 1.0)
						* ((double) (maxYValue - minYValue))
						/ (double) (numberOfExtraYPoints + 1);
			}

			// round the values to a specified number of decimal places
			int numDecimalPlaces = 2;
			String pattern = "0.";
			for(int n = 0; n < numDecimalPlaces; n++)
			{
				pattern += "0";
			}
			DecimalFormat percentFormatter = new DecimalFormat(pattern);
			for(int i = 0; i < yValues.length; i++)
			{
				String output = percentFormatter.format(yValues[i]);
				yValues[i] = new Double(output).doubleValue();
			}

			plot.setExtraYAxisValues(yValues);
		}
		if(numberOfCells > 2)
		{
			int numberOfExtraXPoints = 4;
			if(numberOfCells > numberOfExtraXPoints)
			{
				double[] xValues = new double[numberOfExtraXPoints];
				for(int i = 0; i < numberOfExtraXPoints; i++)
				{
					xValues[i] = Math.round((i + 1) * numberOfCells
							/ (numberOfExtraXPoints + 1));
				}
				plot.setExtraXAxisValues(xValues);
			}
			else
			{
				double[] xValues = new double[numberOfCells - 2];
				for(int i = 0; i < numberOfCells - 2; i++)
				{
					xValues[i] = i + 1;
				}
				plot.setExtraXAxisValues(xValues);
			}
		}

		// specify colors for the points
		Color[] colorArray = new Color[numberOfCells];
		CellStateView view = Rule.getCurrentView();
		for(int i = 0; i < colorArray.length; i++)
		{
			// if we don't clone the state, then we get the one that is tagged,
			// and the tagged color is displayed
			CellState state = sliceCells[i].getState().clone();

			// get the color
			colorArray[i] = view.getDisplayColor(state, null, new Coordinate(0,
					0));
		}
		plot.setPointDisplayColors(colorArray);

		// finally, plot the data
		plot.drawPoints(sliceValueList);
	}

	/**
	 * Overrides the parent's method to handle the notification of a change in
	 * color, and is used to change the color of the points on the plot.
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		if(event.getPropertyName().equals(CurrentProperties.COLORS_CHANGED))
		{
			plotData();
		}
	}

	/**
	 * Handles a submission for a new row and column and other properties.
	 */
	private void submitRowColChanges()
	{
		// read the number of cols
		Integer colInteger = (Integer) ((SpinnerNumberModel) colSpinner
				.getModel()).getNumber();

		// subtract 1 so that they can enter values from 1 to width (rather than
		// 0 to width-1)
		this.column = colInteger.intValue() - 1;

		// read the number of rows
		// read the row number
		Integer rowInteger = (Integer) ((SpinnerNumberModel) rowSpinner
				.getModel()).getNumber();

		// subtract 1 so that they can enter values from 1 to width (rather than
		// 0 to width-1)
		this.row = rowInteger.intValue() - 1;

		// unmark the old cells because we have now selected a new slice
		untagCells();

		// force the slice of cells to be reloaded
		sliceCells = null;

		// rerun the analysis so it grabs the new tagged cell
		rerunAnalysis();

		// and repaint so the new tagged cell is shown
		refreshGraphics();
	}

	/**
	 * Tags the currently selected cells.
	 */
	private void tagCells()
	{
		// untag the cell so no longer has extra visibility
		if(sliceCells != null)
		{
			for(int i = 0; i < sliceCells.length; i++)
			{
				sliceCells[i].setTagged(true, this);
			}
		}
	}

	/**
	 * Untags the currently tagged cells.
	 */
	private void untagCells()
	{
		// untag the cell so no longer has extra visibility
		if(sliceCells != null)
		{
			for(int i = 0; i < sliceCells.length; i++)
			{
				sliceCells[i].setTagged(false, this);
			}
		}
	}

	/**
	 * Display the cells in the slice.
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
		if(isCompatibleRule)
		{
			// load the cell that needs to be analyzed and do other
			// initialization
			if(sliceCells == null)
			{
				// get the slice of cells
				if(rowSliceButton.isSelected())
				{
					int width = CurrentProperties.getInstance().getNumColumns();
					this.sliceCells = new Cell[width];
				}
				else
				{
					int height = CurrentProperties.getInstance().getNumRows();
					this.sliceCells = new Cell[height];
				}

				loadCellsBeingAnalyzed(lattice);

				// have new cells, so tag them
				tagCells();

				// now that we have the cells, we can get the number of data
				// points that might need to be saved
				data = new String[sliceCells.length + 1];

				// is this an integer rule?
				isIntegerRule = IntegerCellState.isCompatibleRule(rule);

				// get the min and max possible y values
				CellState tempState = rule.getCompatibleCellState();
				tempState.setToFullState();
				Number fullValue = (Number) tempState.getValue();
				fullYValue = fullValue.doubleValue();
				tempState.setToEmptyState();
				Number emptyValue = (Number) tempState.getValue();
				emptyYValue = emptyValue.doubleValue();
			}

			sliceValueList.clear();
			for(int i = 0; i < sliceCells.length; i++)
			{
				sliceValueList.add(new Point2D.Double(i,
						((Number) sliceCells[i].getState().getValue())
								.doubleValue()));
			}

			// create an array of data to be saved
			data[0] = "" + generation;
			int i = 1;
			Iterator<Point2D.Double> iterator = sliceValueList.iterator();
			while(iterator.hasNext())
			{
				data[i] = "" + iterator.next().y;
				i++;
			}

			// see if user wants to save the data
			if(fileWriter != null)
			{
				// save it
				saveData(data);
			}

			// display the values in the slice
			plotData();

			// display the tagged cells at start up
			if(firstTimeThrough)
			{
				refreshGraphics();
			}
			firstTimeThrough = false;
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

		// untag the cells so no longer extra visible
		untagCells();

		// and refresh the graphics so don't show the cells as tagged anymore
		refreshGraphics();
	}

	/**
	 * Reacts to the radio buttons for vertical or horizontal slices.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();

		if(command.equals(VERTICAL_SLICE))
		{
			colSpinner.setEnabled(true);
			rowSpinner.setEnabled(false);

			colLabel.setEnabled(true);
			rowLabel.setEnabled(false);

			// reset the tagged cells
			submitRowColChanges();
		}
		else if(command.equals(HORIZONTAL_SLICE))
		{
			colSpinner.setEnabled(false);
			rowSpinner.setEnabled(true);

			colLabel.setEnabled(false);
			rowLabel.setEnabled(true);

			// reset the tagged cells
			submitRowColChanges();
		}
		else if(command.equals(PLOT_LINES_ONLY))
		{
			if(plotLinesOnlyCheckBox.isSelected())
			{
				// when true, the plot will not show the data points and will
				// only
				// show the lines between the data points
				showLinesOnly = true;
			}
			else
			{
				showLinesOnly = false;
			}

			plotData();
		}
		else if(command.equals(SAVE_DATA))
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
	 * Performs any necessary operations to reset the analysis. This method is
	 * called if the user resets the cellular automata, or selects a new
	 * simulation.
	 */
	public void reset()
	{
		// actually checking to see if the previous rule was compatible. If it
		// wasn't then none of these variable might have been instantiated.
		if(isCompatibleRule)
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
			saveDataCheckBox = null;

			// forces it to get the new cell for the new simulation
			sliceCells = null;

			// if one-dimensional, then disable everything
			boolean isOneDimensional = OneDimensionalLattice
					.isCurrentLatticeOneDim();
			if(isOneDimensional)
			{
				// because must be the default value of 1
				colSpinner.setEnabled(false);
				rowSpinner.setEnabled(false);

				colLabel.setEnabled(false);
				rowLabel.setEnabled(false);

				columnSliceButton.setEnabled(false);
				rowSliceButton.setEnabled(false);
			}
			else
			{
				colSpinner.setEnabled(false);
				rowSpinner.setEnabled(true);

				colLabel.setEnabled(false);
				rowLabel.setEnabled(true);

				columnSliceButton.setEnabled(true);
				rowSliceButton.setEnabled(true);
			}

			// new width and height of the lattice
			int width = CurrentProperties.getInstance().getNumColumns();
			int height = CurrentProperties.getInstance().getNumRows();

			// reset the max values on the row and col spinners
			if(!isOneDimensional)
			{
				this.row = height / 2;
				this.column = width / 2;
			}
			else
			{
				this.row = 0;
				this.column = 0;
			}
			SpinnerNumberModel colModel = new SpinnerNumberModel(column + 1, 1,
					width, 1);
			SpinnerNumberModel rowModel = new SpinnerNumberModel(row + 1, 1,
					height, 1);
			colSpinner.setModel(colModel);
			rowSpinner.setModel(rowModel);

			// select a row slice
			rowSliceButton.setSelected(true);

			sliceValueList = new LinkedList<Point2D.Double>();
			data = null;
			isIntegerRule = true;
			showLinesOnly = false;
			plot.clearPlot();
		}

		createDisplayPanel();

		// force it to redisplay
		firstTimeThrough = true;
	}

	/**
	 * May be overriden by child classes that want to take some action when the
	 * analysis frame is resized. This is called when the JFrame holding the
	 * analysis is resized.
	 * <p>
	 * By default, this method does nothing. Child classes (analyses) may
	 * override if desired.
	 * <p>
	 * This method is called automatically when the analysis frame is resized
	 * and when it is converted between a tab and a pane. It is not recommended
	 * that this method be called directly.
	 * 
	 * @param e
	 *            The event that triggered this action. May be null.
	 */
	public void resizeActions(ComponentEvent e)
	{
		// PROBLEMS WITH THIS CODE: Resets the slice view after resizing.
		// Doesn't show the current slice after resizing. When the analysis is
		// closing it is resizing. This causes the tagged cells to reappear
		// after the rest of the analysis has stopped. Uses a magic number for
		// resizing.

		// // used to resize
		// double magicNumber = 0.5;
		//
		// int borderWidth = displayPanel.getInsets().left
		// + displayPanel.getInsets().right;
		//
		// int displayPanelWidth = displayPanel.getWidth() - borderWidth;
		//
		// // the resize even may be due to converting back to a tabbed pane. So
		// we
		// // need to use the tabbed pane dimensions.
		// if(e == null)
		// {
		// displayPanelWidth = CAFrame.tabbedPaneDimension.width - borderWidth;
		// }
		//
		// // resize the display (it comes with a default that is too large)
		// Dimension originalLatticeViewPanelSize = analysisLattice.getView()
		// .getSize();
		// double scalingFactor = (double) displayPanelWidth
		// / (double) originalLatticeViewPanelSize.width;
		//
		// // when displayed as a frame (resizeEventCall = true), it can get too
		// // large, so we limit that.
		// if((e == null)
		// && originalLatticeViewPanelSize.height * scalingFactor > magicNumber
		// * displayPanelWidth)
		// {
		// scalingFactor = (double) magicNumber * displayPanelWidth
		// / (double) originalLatticeViewPanelSize.height;
		// }
		// else if(originalLatticeViewPanelSize.height * scalingFactor >
		// displayPanelWidth)
		// {
		// scalingFactor = (double) displayPanelWidth
		// / (double) originalLatticeViewPanelSize.height;
		// }
		//
		// analysisLattice.getView().resizePanel(scalingFactor);
		//
		// // force the panel to relayout
		// displayPanel.invalidate();
		// displayPanel.validate();

		// force the slice of cells to be reloaded
		// sliceCells = null;

		// rerun the analysis so it grabs the new tagged cell
		// rerunAnalysis();
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
	 * listens for changes to the row and col spinner.
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
			submitRowColChanges();
		}
	}
}
