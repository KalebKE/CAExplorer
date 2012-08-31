/*
 SliceAnalysis -- a class within the Cellular Automaton Explorer. 
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
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.OneDimensionalLattice;
import cellularAutomata.lattice.StandardOneDimensionalLattice;
import cellularAutomata.lattice.view.SliceAnalysisOneDimView;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;

/**
 * Shows a slice through a two-dimensional analysis.
 * 
 * @author David Bahr
 */
public class SliceAnalysis extends Analysis implements ActionListener
{
	// a brief display name for this class
	private static final String ANALYSIS_NAME = "Show Slice";

	// the colored title on the display
	private static final String ATTENTION_PANEL_TITLE = "Show Slice";

	// The tooltip for the column slice
	private static final String COLUMN_TIP = "<html>Choose the column of the vertical "
			+ "slice.</html>";

	// The text for the horizontal radio button
	private static final String HORIZONTAL_SLICE = "Horizontal " + "slice";

	// The tooltip for the horizontal radio button
	private static final String HORIZONTAL_SLICE_TOOLTIP = "<html>Show a horizontal "
			+ "slice through the CA.</html>";

	// display info for this class
	private static final String INFO_MESSAGE = "Shows a one-dimensional slice "
			+ "through a two-dimensional simulation. The colored values of the highlighted "
			+ "cells are plotted. The colors (values) of the highlighted slice will change "
			+ "at every time step, so in this analysis, the sequence of values is "
			+ "plotted one below the other. \n\n"
			+ "In other words, each slice is stacked to show how the slices change "
			+ "with time. The slice at time n+1 is shown directly below the previous "
			+ "slice at time n. Or thinking about it another way: a two-dimensional "
			+ "simulation builds a three-dimensional cube through time, and this "
			+ "analysis shows the two-dimensional slice through that cube. \n\n"
			+ "Slices are particularly interesting with Totalistic and Outer "
			+ "Totalistic rules. The Wolfram behavioral class (1, 2, 3, or 4) is "
			+ "sometimes more obvious when viewing a slice. \n\n"
			+ "Note that a one-dimensional slice through a one-dimensional "
			+ "simulation is just the simulation itself. Therefore, this "
			+ "analysis is only meaningful for two-dimensional CA.";

	// Display title for the row-column panel.
	private static final String ROW_COL_PANEL_TITLE = "Choose a slice.";

	// The tooltip for the row position
	private static final String ROW_TIP = "<html>Choose the row of the "
			+ "slice.</html>";

	// a brief tooltip description for this class
	private static final String TOOLTIP = "<html><body>show a slice of cells through "
			+ "two-dimensional CA</body></html>";

	// The text for the column radio button
	private static final String VERTICAL_SLICE = "Vertical slice";

	// The tooltip for the column radio button
	private static final String VERTICAL_SLICE_TOOLTIP = "<html>Show a vertical "
			+ "slice through the CA.</html>";

	// only true the first time the analyze method is called
	private boolean firstTimeThrough = true;

	// the cells in the slice
	private Cell[] sliceCells = null;

	// color of titles of sections
	private Color titleColor = Color.BLUE;

	// dimensions of the original panel that displays the slice. Used when
	// resizing the lattice view panel.
	private Dimension originalSlicePanelDimensions = null;

	// column of the slice being analyzed
	private int column = 0;

	// row of the slice being analyzed
	private int row = 0;

	// fonts for display
	private Fonts fonts = new Fonts();

	// title font (for titles of sections)
	private Font titleFont = new Fonts().getItalicSmallerFont();

	// the label for the col spinner
	private JLabel colLabel = null;

	// the label for the row spinner
	private JLabel rowLabel = null;

	// the panel where results are displayed
	private JPanel displayPanel = null;

	// the panel that displays the slice
	private JPanel sliceView = null;

	// button for selecting vertical slices
	private JRadioButton columnSliceButton = null;

	// button for selecting horizontal slices
	private JRadioButton rowSliceButton = null;

	// selects the column position of the slice
	private JSpinner colSpinner = null;

	// selects the row of the slice
	private JSpinner rowSpinner = null;

	// the lattice used to draw the analysis
	private StandardOneDimensionalLattice analysisLattice = null;

	/**
	 * Create an analysis that shows a one-dimensional slice through
	 * two-dimensional CA.
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
	public SliceAnalysis(boolean minimalOrLazyInitialization)
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
		int displayWidth = CAFrame.tabbedPaneDimension.width;
		int displayHeight = 1000;

		// create the display panel
		if(displayPanel == null)
		{
			displayPanel = new JPanel(new GridBagLayout());
			displayPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			displayPanel.setPreferredSize(new Dimension(displayWidth,
					displayHeight));

			// create a panel that displays messages
			JPanel messagePanel = createMessagePanel();

			// panel to select a slice
			JPanel selectSlicePanel = createSpinnersPanel();

			// create a panel that displays the slice
			sliceView = new JPanel();
			int borderWidth = displayPanel.getInsets().left
					+ displayPanel.getInsets().right;
			originalSlicePanelDimensions = new Dimension(displayWidth
					- borderWidth, (int) (displayWidth * 1.5));
			sliceView.setSize(originalSlicePanelDimensions);

			// add all the components to the panel
			int row = 0;
			displayPanel.add(messagePanel, new GBC(1, row).setSpan(4, 1)
					.setFill(GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(
							GBC.WEST).setInsets(1));

			row++;
			displayPanel.add(sliceView, new GBC(1, row).setSpan(4, 1).setFill(
					GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
					.setInsets(1));

			row++;
			displayPanel.add(selectSlicePanel, new GBC(1, row).setSpan(4, 1)
					.setFill(GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(
							GBC.WEST).setInsets(1));

			// this will fill empty space
			row++;
			displayPanel.add(new JLabel(" "), new GBC(1, row).setSpan(4, 1)
					.setFill(GBC.HORIZONTAL).setWeight(10.0, 10.0).setAnchor(
							GBC.WEST).setInsets(1));
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
	 * Displays the cells in the slice using a one-dimensional lattice panel.
	 */
	private void displaySlice()
	{
		if(analysisLattice != null && analysisLattice.getView() != null)
		{
			analysisLattice.getView().drawLattice(analysisLattice);
			sliceView.repaint();
		}
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

		// now create a new lattice that will be used to draw on the analysis
		analysisLattice = new StandardOneDimensionalLattice(sliceCells);

		// create a special view for the lattice
		analysisLattice.setView(new SliceAnalysisOneDimView(analysisLattice));

		// set the background color
		analysisLattice.getView().setBackground(displayPanel.getBackground());

		// resize the display (it comes with a default that is too large)
		Dimension originalLatticeViewPanelSize = analysisLattice.getView()
				.getSize();
		double scalingFactor = (double) originalSlicePanelDimensions.width
				/ (double) originalLatticeViewPanelSize.width;
		if(originalLatticeViewPanelSize.height * scalingFactor > originalSlicePanelDimensions.width)
		{
			scalingFactor = (double) originalSlicePanelDimensions.height
					/ (double) originalLatticeViewPanelSize.height;
		}
		analysisLattice.getView().resizePanel(scalingFactor);

		// and display the panel
		sliceView.removeAll();
		sliceView.add(analysisLattice.getView());
		// sliceView.repaint();

		// force the panel to relayout
		displayPanel.invalidate();
		displayPanel.validate();
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
		// load the cell that needs to be analyzed
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
		}

		// display the slice
		displaySlice();

		// display the tagged cells at start up
		if(firstTimeThrough)
		{
			refreshGraphics();
		}
		firstTimeThrough = false;
	}

	/**
	 * Performs any desired operations when the analysis is stopped (closed) by
	 * the user. For example, you might write the results to a file at this
	 * time. Or you might dispose of any windows that you opened. May do
	 * nothing.
	 */
	protected void stopAnalysis()
	{
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
		}
		else if(command.equals(HORIZONTAL_SLICE))
		{
			colSpinner.setEnabled(false);
			rowSpinner.setEnabled(true);

			colLabel.setEnabled(false);
			rowLabel.setEnabled(true);
		}

		// reset the tagged cells
		submitRowColChanges();
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
