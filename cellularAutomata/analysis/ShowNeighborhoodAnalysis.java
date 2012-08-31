/*
 ShowNeighborhoodAnalysis -- a class within the Cellular Automaton Explorer. 
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
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
import cellularAutomata.rules.Rule;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;

/**
 * Shows the neighborhood of any given cell.
 * 
 * @author David Bahr
 */
public class ShowNeighborhoodAnalysis extends Analysis implements
		ActionListener
{
	// a brief display name for this class
	private static final String ANALYSIS_NAME = "Show Neighborhood";

	// the colored title on the display
	private static final String ATTENTION_PANEL_TITLE = "Show Neighbors";

	// The tooltip for the column position
	private static final String COLUMN_TIP = "<html>Choose the column of the "
			+ "cell <br> whose neighbors will be shown.</html>";

	// display info for this class
	private static final String INFO_MESSAGE = "Shows the neighborhood of any "
			+ "selected cell.  For most lattices, the nieghborhood has the same"
			+ "\"shape\" for all cells.  However, for some lattices the "
			+ "neighborhood changes with time and position.  For example, try "
			+ "the Margolus, random Gaussian, and small-world lattices.";

	// Display title for the row-column panel.
	private static final String ROW_COL_PANEL_TITLE = "Choose a cell";

	// The tooltip for the row position
	private static final String ROW_TIP = "<html>Choose the row of the "
			+ "cell <br> whose neighbors will be shown.</html>";

	// the display name and action command for the showCell check box
	private static final String SHOW_CELL = "Show cell";

	// tool tip for the check box that shows cthe ell
	private static final String SHOW_CELL_TOOLTIP = "When selected, will show "
			+ "the cell";

	// the display name and action command for the showNeighbors check box
	private static final String SHOW_NEIGHBORS = "Show neighbors";

	// tool tip for the check box that shows the cell
	private static final String SHOW_NEIGHBORS_TOOLTIP = "When selected, will show "
			+ "the neighbors of the cell";

	// a brief tooltip description for this class
	private static final String TOOLTIP = "<html><body>show the neighbors of any "
			+ "selected cell</body></html>";

	// only true the first time the analyze method is called
	private boolean firstTimeThrough = true;

	// true if the showCellCheckbox is checked
	private boolean showCell = true;

	// true if the showNeighborsCheckbox is checked
	private boolean showNeighbors = true;

	// the cell being used
	private Cell cellBeingAnalyzed = null;

	// the neighbors of the cell
	private Cell[] neighbors = null;

	// color of titles of sections
	private Color titleColor = Color.BLUE;

	// column position of the cell being analyzed
	private int column = 0;

	// row position of the cell being analyzed
	private int row = 0;

	// fonts for display
	private Fonts fonts = new Fonts();

	// title font (for titles of sections)
	private Font titleFont = new Fonts().getItalicSmallerFont();

	// the check box for showing the cell
	private JCheckBox showCellCheckbox = null;

	// the check box for showing the cell
	private JCheckBox showNeighborsCheckbox = null;

	// the panel where results are displayed
	private JPanel displayPanel = null;

	// selects the column position for which the neighbors will be shown
	private JSpinner colSpinner = null;

	// selects the row position for which the neighbors will be shown
	private JSpinner rowSpinner = null;

	/**
	 * Create an analysis that shows the neighborhood of any given cell.
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
	public ShowNeighborhoodAnalysis(boolean minimalOrLazyInitialization)
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
		int displayHeight = 400;

		// create the display panel
		if(displayPanel == null)
		{
			displayPanel = new JPanel(new GridBagLayout());
			displayPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			displayPanel.setPreferredSize(new Dimension(displayWidth,
					displayHeight));

			// create a panel that displays messages
			JPanel messagePanel = createMessagePanel();

			// panel to select a cell
			JPanel selectCellPanel = createSpinnersPanel();

			// add all the components to the panel
			int row = 0;
			displayPanel.add(messagePanel, new GBC(1, row).setSpan(4, 1)
					.setFill(GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(
							GBC.WEST).setInsets(1));

			row++;
			displayPanel.add(selectCellPanel, new GBC(1, row).setSpan(4, 1)
					.setFill(GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(
							GBC.WEST).setInsets(1));

			// this will fill empty space
			row++;
			displayPanel.add(new JLabel(" "), new GBC(1, row).setSpan(4, 1)
					.setFill(GBC.BOTH).setWeight(20.0, 20.0).setAnchor(
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
		// create a check box for showing neighbors
		showCellCheckbox = new JCheckBox(SHOW_CELL);
		showCellCheckbox.setSelected(true);
		showCellCheckbox.setToolTipText(SHOW_CELL_TOOLTIP);
		showCellCheckbox.setActionCommand(SHOW_CELL);
		showCellCheckbox.addActionListener(this);
		showCellCheckbox.setFont(fonts.getPlainFont());

		// create a check box for showing neighbors
		showNeighborsCheckbox = new JCheckBox(SHOW_NEIGHBORS);
		showNeighborsCheckbox.setSelected(true);
		showNeighborsCheckbox.setToolTipText(SHOW_NEIGHBORS_TOOLTIP);
		showNeighborsCheckbox.setActionCommand(SHOW_NEIGHBORS);
		showNeighborsCheckbox.addActionListener(this);
		showNeighborsCheckbox.setFont(fonts.getPlainFont());

		// panel for the checkboxes
		JPanel checkboxPanel = new JPanel(new GridBagLayout());
		int row = 0;
		checkboxPanel.add(showCellCheckbox, new GBC(0, row).setSpan(2, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1, 1, 1, 1));

		row++;
		checkboxPanel.add(showNeighborsCheckbox, new GBC(0, row).setSpan(2, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1, 1, 1, 1));

		// width and height of the lattice
		int width = CurrentProperties.getInstance().getNumColumns();
		int height = CurrentProperties.getInstance().getNumRows();

		// create spinners for the row and height
		SpinnerNumberModel colModel = new SpinnerNumberModel(1, 1, width, 1);
		colSpinner = new JSpinner(colModel);
		colSpinner.setToolTipText(COLUMN_TIP);
		colSpinner.addChangeListener(new RowColListener());

		SpinnerNumberModel rowModel = new SpinnerNumberModel(1, 1, height, 1);
		rowSpinner = new JSpinner(rowModel);
		rowSpinner.setToolTipText(ROW_TIP);
		rowSpinner.addChangeListener(new RowColListener());

		// is lattice one-dimensional?
		boolean isOneDimensional = OneDimensionalLattice
				.isCurrentLatticeOneDim();
		if(isOneDimensional)
		{
			// has to be a 1 if one-dimensional
			rowSpinner.setEnabled(false);
		}

		// create a label for the starting column position
		JLabel colLabel = new JLabel("Column: ");
		colLabel.setFont(fonts.getPlainFont());

		// create a label for the row position
		JLabel rowLabel = new JLabel("Row: ");
		rowLabel.setFont(fonts.getPlainFont());

		// a Box uses the BoxLayout, so it is handy for laying out components
		Box boxOfSpinners = Box.createHorizontalBox();

		// the amount of horizontal space to put between components
		int horizontalSpace1 = 1;
		int horizontalSpace2 = 20;

		// add the spinners to the box
		boxOfSpinners.add(rowLabel);
		boxOfSpinners.add(Box.createHorizontalStrut(horizontalSpace1));
		boxOfSpinners.add(rowSpinner);
		boxOfSpinners.add(Box.createHorizontalStrut(horizontalSpace2));
		boxOfSpinners.add(colLabel);
		boxOfSpinners.add(Box.createHorizontalStrut(horizontalSpace1));
		boxOfSpinners.add(colSpinner);

		// add all components to a single panel
		JPanel selectCellsPanel = new JPanel(new GridBagLayout());
		Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 5);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), ROW_COL_PANEL_TITLE, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		Border compoundBorder = BorderFactory.createCompoundBorder(
				titledBorder, emptyBorder);
		selectCellsPanel.setBorder(compoundBorder);

		int row2 = 0;
		selectCellsPanel.add(boxOfSpinners, new GBC(0, row2).setSpan(2, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
				.setInsets(1, 1, 20, 1));

		row2++;
		selectCellsPanel.add(checkboxPanel, new GBC(0, row2).setSpan(2, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
				.setInsets(1, 1, 1, 1));

		return selectCellsPanel;
	}

	/**
	 * Gets the cell from the lattice that is being used to generate the random
	 * number.
	 * 
	 * @param lattice
	 *            The CA lattice containing the CA cells.
	 */
	private Cell loadCellBeingAnalyzed(Lattice lattice)
	{
		// the width of the lattice
		int width = CurrentProperties.getInstance().getNumColumns();

		// get an iterator over the lattice
		Iterator cellIterator = lattice.iterator();

		// Ignore cells until we get to the correct position.
		// Go to the correct row.
		for(int i = 0; i < row * width + column; i++)
		{
			// ignore these cells
			cellIterator.next();
		}

		// Now get this cell!
		Cell cellBeingAnalyzed = (Cell) cellIterator.next();

		return cellBeingAnalyzed;
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

		// unmark the old cell because we have now selected a new cell
		untagCells();

		// force the cell to be reloaded
		cellBeingAnalyzed = null;

		// rerun the analysis so it grabs the new tagged cell
		rerunAnalysis();

		// and repaint so the new tagged cell is shown
		refreshGraphics();
	}

	/**
	 * Untags the currently tagged cells.
	 */
	private void untagCells()
	{
		// untag the cell so no longer has extra visibility
		if(cellBeingAnalyzed != null)
		{
			cellBeingAnalyzed.setTagged(false, this);

			// untag the neighbors
			if(neighbors != null && neighbors.length > 0)
			{
				for(Cell neighbor : neighbors)
				{
					neighbor.setTagged(false, this);
				}
			}
		}
	}

	/**
	 * Displays cells in the neighborhood of the selected cell.
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
		if(cellBeingAnalyzed == null)
		{
			cellBeingAnalyzed = loadCellBeingAnalyzed(lattice);
		}

		// save the old neighbors so we can compare them to the new ones in a
		// few lines
		Cell[] oldNeighbors = null;
		if(neighbors != null)
		{
			oldNeighbors = new Cell[neighbors.length];
			for(int i = 0; i < oldNeighbors.length; i++)
			{
				oldNeighbors[i] = neighbors[i];
			}
		}

		// get the new neighbors (could change every time step)
		neighbors = lattice.getNeighbors(cellBeingAnalyzed);

		// Are the new neighbors different than the old neighbors? Then untag
		// the old neighbors. (The neighboring cells may change every time step
		// for some lattices.)
		boolean untag = false;
		if((oldNeighbors != null) && (neighbors != null))
		{
			// see if there are more or fewer neighbors
			if(oldNeighbors.length != neighbors.length)
			{
				untag = true;
			}
			else
			{
				// see if they are the same neighbors
				for(int i = 0; i < oldNeighbors.length; i++)
				{
					if(!oldNeighbors[i].equals(neighbors[i]))
					{
						untag = true;
					}
				}
			}

			if(untag)
			{
				// untag the old neighboring cells because they have changed.
				if((oldNeighbors != null) && (oldNeighbors.length > 0))
				{
					for(Cell oldNeighbor : oldNeighbors)
					{
						oldNeighbor.setTagged(false, this);
					}
				}
			}
		}

		// tag the new neighbors (but only if showNeighbors is true -- otherwise
		// untag)
		if(neighbors != null && neighbors.length > 0)
		{
			for(Cell neighbor : neighbors)
			{
				neighbor.setTagged(showNeighbors, this);
			}
		}

		// tag this cell for extra visibility (but don't tag if showCell =
		// false). Note: this line has to happen AFTER the oldNeighbors are
		// untagged. Otherwise, one of the old neighbors might be this cell and
		// would therefore untag this cell! So we have to retag it here. And
		// this has to happen every time this method is called for in case the
		// nieghbors have changed and unwittingly untagged this cell.
		cellBeingAnalyzed.setTagged(showCell, this);

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
		// untag the cell so no longer extra visibility
		untagCells();

		// and refresh the graphics so don't show the cells as tagged anymore
		refreshGraphics();
	}

	/**
	 * Reacts to the "show neighbors" and "show cell" check boxes.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();

		if(command.equals(SHOW_NEIGHBORS))
		{
			showNeighbors = showNeighborsCheckbox.isSelected();

			// tag or untag the neighbors
			if(neighbors != null && neighbors.length > 0)
			{
				for(Cell neighbor : neighbors)
				{
					neighbor.setTagged(showNeighbors, this);
				}
			}
		}
		else if(command.equals(SHOW_CELL))
		{
			showCell = showCellCheckbox.isSelected();

			// tag or untag the cell
			if(cellBeingAnalyzed != null)
			{
				cellBeingAnalyzed.setTagged(showCell, this);
			}
		}

		// and repaint so the new tagged cells are shown
		refreshGraphics();
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
		cellBeingAnalyzed = null;

		// new width and height of the lattice
		int width = CurrentProperties.getInstance().getNumColumns();
		int height = CurrentProperties.getInstance().getNumRows();

		// reset the max values on the row and col spinners
		SpinnerNumberModel colModel = new SpinnerNumberModel(1, 1, width, 1);
		SpinnerNumberModel rowModel = new SpinnerNumberModel(1, 1, height, 1);
		colSpinner.setModel(colModel);
		rowSpinner.setModel(rowModel);

		row = 0;
		column = 0;

		// if one-dimensional, then disable the row field, otherwise enable
		boolean isOneDimensional = OneDimensionalLattice
				.isCurrentLatticeOneDim();
		if(isOneDimensional)
		{
			// because must be the default value of 1
			rowSpinner.setEnabled(false);
		}
		else
		{
			rowSpinner.setEnabled(true);
		}

		// force it to redisplay
		firstTimeThrough = true;
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
			submitRowColChanges();
		}
	}
}
