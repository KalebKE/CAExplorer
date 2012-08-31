/*
 PinCellValueAnalysis -- a class within the Cellular Automaton Explorer. 
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
import java.beans.PropertyChangeEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
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
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.graphics.colors.colorChooser.IntegerStateColorChooser;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.OneDimensionalLattice;
import cellularAutomata.lattice.view.listener.AnalysisDrawingListener;
import cellularAutomata.lattice.view.listener.AnalysisDrawingListener.MouseState;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.rules.Rule;
import cellularAutomata.rules.templates.FiniteObjectRuleTemplate;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;
import cellularAutomata.util.math.RandomSingleton;

/**
 * Pins selected cells to a particular state.
 * 
 * @author David Bahr
 */
public class PinCellValueAnalysis extends Analysis implements ActionListener
{
	// the constant representing filled states. Should not be any integer
	// between 0 and numStates (inclusive). Used to identify the user's choice
	// of state(s) to analyze.
	private static final int FILLED_STATE_CHOICE = -1;

	// the constant representing the empty state. Should not be any integer
	// between 0 and numStates (inclusive). Used to identify the user's choice
	// of state(s) to analyze.
	private static final int EMPTY_STATE_CHOICE = -2;

	// the constant representing no state -- the cells won't be pinned to any
	// value. Should not be any integer between 0 and numStates (inclusive).
	// Used to identify the user's choice of state(s) to analyze.
	private static final int NO_STATE_CHOICE = -3;

	// text for the button that lets the user add a pinned cell
	private static final String ADD_PINNED_CELL = "Add pinned cell";

	// The tooltip for adding a pinned cell
	private static final String ADD_PINNED_CELL_TOOLTIP = "<html><body>Pins a "
			+ "cell at the specified position.</body>.</html>";

	// a brief display name for this class
	private static final String ANALYSIS_NAME = "Pin Values";

	// a title for the radio button box that lets them choose random versus
	// selected cells
	private static final String CHOICE_PANEL_TITLE = "Pick cells that will be pinned";

	// The tooltip for the col of the pinned cell
	private static final String COL_TIP = "<html><body>The column of the selected "
			+ "pinned cell.</body>.</html>";

	// display info for this class
	private static final String INFO_MESSAGE = "This analysis lets you select cells "
			+ "that will be pinned to a particular "
			+ "state value. No matter what value the rule assigns to a cell, this "
			+ "analysis overrides and replaces it with the fixed value specified below. "
			+ "In other words, this rule prevents selected cells from evolving by "
			+ "forcing them to hold a particular state value. \n\n"
			+ "For example, try pinning cells in Life. This can lead to fascinating "
			+ "behaviors. \n\n"
			+ "WARNING: Once you pin a cell, it's value is reset and the old value cannot be "
			+ "retrieved.  The cell can be unpinned so that it evolves again, but the "
			+ "original value is lost. \n\n"
			+ "If you are running more than one analysis, select this one first. "
			+ "That guarantees the other analyses will see the newly pinned values. \n\n"
			+ "This \"analysis\" actively changes data rather than passively "
			+ "interpreting data. It also suspends normal drawing behavior "
			+ "so that pinned cells can be drawn instead.";

	// title for the subpanel that lets the user select the state to be analyzed
	private static final String RADIO_BUTTON_PANEL_TITLE = "Pin cells to this "
			+ "selected state";

	// The tooltip for the percent of cells that will be pinned
	private static final String RANDOM_PERCENT_TIP = "<html><body>The percentage "
			+ "of cells that will be pinned.</body>.</html>";

	// text for the button that lets the user remove all pinned cells
	private static final String REMOVE_ALL_PINNED_CELLS = "Remove all";

	// The tooltip for removing all pinned cells
	private static final String REMOVE_ALL_PINNED_CELLS_TOOLTIP = "<html><body>"
			+ "Remove all pinned cells.</body>.</html>";

	// text for the button that lets the user remove the last pinned cell
	private static final String REMOVE_LAST_PINNED_CELL = "Remove last";

	// The tooltip for removing the last cell that was pinned
	private static final String REMOVE_LAST_PINNED_CELL_TOOLTIP = "<html><body>"
			+ "Removes the last cell that was pinned.</body>.</html>";

	// title of the subpanel that removes pinned cells
	private static final String REMOVE_PANEL_TITLE = "Remove pinned cells";

	// The tooltip for the row of the pinned cell
	private static final String ROW_TIP = "<html><body>The row of the selected "
			+ "pinned cell.</body>.</html>";

	// text for the button that lets the user select the state for the
	// pinned cells.
	private static final String SELECT_STATE = "Select state";

	// tooltip for the button that lets the state be selected
	private static final String SELECT_STATE_TOOLTIP = "Select a state to which the "
			+ "cells will be pinned.";

	// the action command for the state chooser
	private static final String STATE_CHOOSER = "state chooser";

	// the label and action command for the button that submits the random
	// percent
	private static final String SUBMIT_PERCENT = "Set new percent";

	// tooltip for the button that submits the percentage of cells that will be
	// pinned
	private static final String SUBMIT_PERCENT_TOOLTIP = "Set the percentage of "
			+ "cells that will be pinned";

	// a brief tooltip description for this class
	private static final String TOOLTIP = "<html><body>pin selected cells "
			+ "to a selected state</body></html>";

	// true when the drawCells radio button is selected
	private boolean drawPinnedCells = true;

	// only true the first time the analyze method is called
	private boolean firstTimeThrough = true;

	// true when the list of pinned cells should be updated (when random is
	// selected and when the user changes the random percent)
	private boolean randomPinnedCellsNeedUpdating = true;

	// the current view for the rule
	private CellStateView view = null;

	// the color of the current state
	private Color currentColor = Color.GRAY;

	// color of titles of sections
	private Color titleColor = Color.BLUE;

	// a color patch so the user can see the color being analyzed
	private ColorPatch colorPatch = null;

	// the percentage of cells that will be pinned (when random is
	// selected instead of a fixed number of sites)
	private static double randomPercent = 0.01;

	// the col of the cell that will be added and pinned
	private int col = 0;

	// height of the lattice (set in constructor)
	private int height = 0;

	// the state that was last selected by the user (used in the graphics)
	private int lastSelectedState = FILLED_STATE_CHOICE;

	// the number of states in the current simulation
	private int numStates = 2;

	// the row of the cell that will be added and pinned
	private int row = 0;

	// the state that has been selected for analysis
	private static int selectedState = 1;

	// width of the lattice (set in constructor)
	private int width = 10;

	// fonts for display
	private Fonts fonts = new Fonts();

	// title font (for titles of sections)
	private Font titleFont = new Fonts().getItalicSmallerFont();

	// used to select the state that will be analyzed
	private IntegerStateColorChooser integerColorChooser = null;

	// button that lets the user add a pinned cell at the specified location
	// (row and col spinners)
	private JButton addPinnedCellButton = null;

	// button that lets the user remove all pinned cells
	private JButton removeAllPinnedCellsButton = null;

	// button that lets the user remove the last pinned cell
	private JButton removeLastPinnedCellButton = null;

	// the button for selecting the state to be analyzed
	private JButton selectStateButton = null;

	// the button for submitting the random percent of cells that will be pinned
	private JButton submitPercentButton = null;

	// the panel where results are displayed
	private JPanel displayPanel = null;

	// radio button for choosing the empty state
	private JRadioButton emptyStateButton = null;

	// radio button for choosing the filled states
	private JRadioButton filledStateButton = null;

	// radio button for choosing no state
	private JRadioButton noStateButton = null;

	// radio button for choosing a particular state
	private JRadioButton particularStateButton = null;

	// radio button for drawing selected cells with the mouse
	private JRadioButton drawCellsButton = null;

	// radio button for choosing random cells
	private JRadioButton randomCellsButton = null;

	// radio button for choosing selected cells
	private JRadioButton selectCellsButton = null;

	// selects the starting column position for which music will be played
	private JSpinner colSpinner = null;

	// selects the percentage of cells that will be pinned
	private JSpinner percentSpinner = null;

	// selects the starting row position for which music will be played
	private JSpinner rowSpinner = null;

	// the cells that are pinned
	private LinkedList<Cell> listOfPinnedCells = new LinkedList<Cell>();

	// random number generator
	private Random random = null;

	// the current CA rule
	private Rule rule = null;

	/**
	 * Create an analyzer that pins some cells to particular values.
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
	public PinCellValueAnalysis(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			setUpAnalysis();
		}
	}

	/**
	 * Add the specified cell to the list of pinned cells.
	 * 
	 * @param row
	 *            The row of the cell.
	 * @param col
	 *            The column of the cell.
	 */
	private void addCellToPinnedList(Cell cell)
	{
		if(cell != null)
		{
			cell.setTagged(true, this);

			listOfPinnedCells.add(cell);
		}
	}

	/**
	 * Add the specified cell to the list of pinned cells.
	 * 
	 * @param row
	 *            The row of the cell.
	 * @param col
	 *            The column of the cell.
	 */
	private void addCellToPinnedList(int row, int col)
	{
		if(getLattice() != null)
		{
			// get the cell by iterating until it is reached
			Iterator iterator = getLattice().iterator();
			int position = 0;
			for(position = 0; position < (row * width + col); position++)
			{
				iterator.next();
			}

			Cell cell = (Cell) iterator.next();

			cell.setTagged(true, this);

			listOfPinnedCells.add(cell);
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
	 * Create the panel used to display the radio button choices.
	 */
	private void createDisplayPanel()
	{
		int displayWidth = CAFrame.tabbedPaneDimension.width;
		int displayHeight = 975;

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

		// options for selecting pinned states (random or by position)
		JPanel optionsPanel = createOptionsPanel();

		// buttons for removing pinned cells
		JPanel removePanel = createRemovePinnedCellsPanel();

		// create a panel that holds the select state radio buttons
		JPanel stateSelectionPanel = createStateRadioButtonPanel();

		// add all the components to the panel
		int row = 0;
		displayPanel.add(messagePanel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		displayPanel.add(optionsPanel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		displayPanel.add(removePanel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		displayPanel.add(stateSelectionPanel, new GBC(1, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
	}

	/**
	 * Creates a panel that displays messages.
	 * 
	 * @return A panel containing messages.
	 */
	private JPanel createMessagePanel()
	{
		// a "grab their attention" panel
		AttentionPanel attentionPanel = new AttentionPanel("Pin Cell Values");

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
	 * Creates radio buttons to choose random or selected pinned cells.
	 */
	private JPanel createOptionsPanel()
	{
		// details for when the selectCellsButton is selected (must be called
		// before PinnedChoiceListener is instantiated below, because
		// PinnedChoiceListener uses buttons created in here)
		JPanel selectPanel = createPinnedCellsPanel();

		// details for when the randomCellsButton is selected (must be called
		// before PinnedChoiceListener is instantiated below, because
		// PinnedChoiceListener uses buttons created in here)
		JPanel percentPanel = createRandomPercentPanel();

		drawCellsButton = new JRadioButton("Draw cells with mouse");
		drawCellsButton.setFont(fonts.getBoldFont());
		drawCellsButton.addItemListener(new PinnedChoiceListener());
		drawCellsButton.setSelected(true);

		selectCellsButton = new JRadioButton("Select cells by row and col");
		selectCellsButton.setFont(fonts.getBoldFont());
		selectCellsButton.addItemListener(new PinnedChoiceListener());
		selectCellsButton.setSelected(false);

		randomCellsButton = new JRadioButton("Random cells");
		randomCellsButton.setFont(fonts.getBoldFont());
		randomCellsButton.addItemListener(new PinnedChoiceListener());
		randomCellsButton.setSelected(false);

		// put them in a group so that they behave as radio buttons
		ButtonGroup group = new ButtonGroup();
		group.add(drawCellsButton);
		group.add(selectCellsButton);
		group.add(randomCellsButton);

		// now put all the graphics together
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		int row = 0;
		buttonPanel.add(drawCellsButton, new GBC(0, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1,
				1, 10, 1));

		row++;
		buttonPanel.add(new JLabel(" "), new GBC(0, row).setSpan(2, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		buttonPanel.add(selectCellsButton, new GBC(0, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		buttonPanel.add(new JLabel(" "), new GBC(0, row).setSpan(2, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		buttonPanel.add(selectPanel, new GBC(2, row).setSpan(2, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1,
				1, 10, 1));

		row++;
		buttonPanel.add(randomCellsButton, new GBC(0, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		buttonPanel.add(new JLabel("               "), new GBC(0, row).setSpan(
				2, 1).setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		buttonPanel.add(percentPanel, new GBC(2, row).setSpan(2, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		// create a JPanel for the radio buttons
		JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 5);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), CHOICE_PANEL_TITLE, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		Border compoundBorder = BorderFactory.createCompoundBorder(
				titledBorder, emptyBorder);
		radioPanel.setBorder(compoundBorder);
		radioPanel.add(buttonPanel);

		return radioPanel;
	}

	/**
	 * Create spinners for choosing how many cells and their locations.
	 * 
	 * @return panel containing spinners.
	 */
	private JPanel createRemovePinnedCellsPanel()
	{
		// create button for removing the last cell that was pinned
		removeLastPinnedCellButton = new JButton(REMOVE_LAST_PINNED_CELL);
		removeLastPinnedCellButton.setActionCommand(REMOVE_LAST_PINNED_CELL);
		removeLastPinnedCellButton
				.setToolTipText(REMOVE_LAST_PINNED_CELL_TOOLTIP);
		removeLastPinnedCellButton.addActionListener(this);

		// create button for removing all pinned cells
		removeAllPinnedCellsButton = new JButton(REMOVE_ALL_PINNED_CELLS);
		removeAllPinnedCellsButton.setActionCommand(REMOVE_ALL_PINNED_CELLS);
		removeAllPinnedCellsButton
				.setToolTipText(REMOVE_ALL_PINNED_CELLS_TOOLTIP);
		removeAllPinnedCellsButton.addActionListener(this);

		// format the buttons in a box layout
		int horizontalSpace = 65;
		Box boxOfButtons = Box.createHorizontalBox();
		boxOfButtons.add(removeAllPinnedCellsButton);
		boxOfButtons.add(Box.createHorizontalStrut(horizontalSpace));
		boxOfButtons.add(removeLastPinnedCellButton);

		// add all components to a single panel
		JPanel removeCellsPanel = new JPanel(new GridBagLayout());
		int row = 0;
		removeCellsPanel.add(boxOfButtons, new GBC(0, row).setSpan(2, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// create a bordered panel for the buttons
		JPanel borderedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 5);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), REMOVE_PANEL_TITLE, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		Border compoundBorder = BorderFactory.createCompoundBorder(
				titledBorder, emptyBorder);
		borderedPanel.setBorder(compoundBorder);
		borderedPanel.add(removeCellsPanel);

		return borderedPanel;
	}

	/**
	 * Create spinners for choosing how many cells and their locations.
	 * 
	 * @return panel containing spinners.
	 */
	private JPanel createPinnedCellsPanel()
	{
		// create button for adding a cell which will be pinned
		addPinnedCellButton = new JButton(ADD_PINNED_CELL);
		addPinnedCellButton.setActionCommand(ADD_PINNED_CELL);
		addPinnedCellButton.setToolTipText(ADD_PINNED_CELL_TOOLTIP);
		addPinnedCellButton.addActionListener(this);

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

		// the amount of vertical and horizontal space to put between components
		int horizontalSpace = 10;

		// create a box that holds both of the components
		Box boxOfLabelsAndSpinners = Box.createHorizontalBox();
		boxOfLabelsAndSpinners.add(rowLabel);
		boxOfLabelsAndSpinners.add(rowSpinner);
		boxOfLabelsAndSpinners.add(Box.createHorizontalStrut(horizontalSpace));
		boxOfLabelsAndSpinners.add(colLabel);
		boxOfLabelsAndSpinners.add(colSpinner);

		// add all components to a single panel
		JPanel selectCellsPanel = new JPanel(new GridBagLayout());
		int row = 0;
		selectCellsPanel.add(boxOfLabelsAndSpinners, new GBC(0, row).setSpan(2,
				1).setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1, 1, 5, 1));

		row++;
		selectCellsPanel.add(addPinnedCellButton, new GBC(0, row).setSpan(2, 1)
				.setFill(GBC.VERTICAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		return selectCellsPanel;
	}

	/**
	 * Create spinner for a random percent.
	 * 
	 * @return panel with the random percent spinner.
	 */
	private JPanel createRandomPercentPanel()
	{
		// create button for submitting the random percent
		submitPercentButton = new JButton(SUBMIT_PERCENT);
		submitPercentButton.setActionCommand(SUBMIT_PERCENT);
		submitPercentButton.setToolTipText(SUBMIT_PERCENT_TOOLTIP);
		submitPercentButton.addActionListener(this);

		// create spinner for the random percent
		SpinnerNumberModel percentModel = new SpinnerNumberModel(1.0, 0.0,
				100.0, 0.1);
		percentSpinner = new JSpinner(percentModel);
		percentSpinner.setToolTipText(RANDOM_PERCENT_TIP);
		percentSpinner.addChangeListener(new RandomPercentListener());

		// create a label for the random percent
		JLabel percentLabel = new JLabel("Random percent: ");
		percentLabel.setFont(fonts.getPlainFont());

		JPanel percentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		percentPanel.add(percentLabel);
		percentPanel.add(percentSpinner);

		// put all the components together
		JPanel percentSubmitPanel = new JPanel(new GridBagLayout());
		int row = 0;
		percentSubmitPanel.add(submitPercentButton, new GBC(0, row).setSpan(2,
				1).setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		percentSubmitPanel.add(percentPanel, new GBC(0, row).setSpan(2, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1, 1, 1, 1));

		return percentSubmitPanel;
	}

	/**
	 * Creates radio buttons to choose which state(s) will be used for pinning.
	 */
	private JPanel createStateRadioButtonPanel()
	{
		noStateButton = new JRadioButton("do not pin cells");
		noStateButton.setFont(fonts.getPlainFont());
		noStateButton.addItemListener(new StateChoiceListener());
		noStateButton.setSelected(false);

		filledStateButton = new JRadioButton("filled state");
		filledStateButton.setFont(fonts.getPlainFont());
		filledStateButton.addItemListener(new StateChoiceListener());
		filledStateButton.setSelected(true);

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
		group.add(noStateButton);
		group.add(filledStateButton);
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
		boxOfRadioButtons.add(noStateButton);
		boxOfRadioButtons.add(Box.createVerticalStrut(verticalSpace));
		boxOfRadioButtons.add(filledStateButton);
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
	 * Makes sure that an appropriate empty state is selected. (For example,
	 * sets the number of states for integer states.)
	 * 
	 * @return
	 */
	private CellState getEmptyState()
	{
		CellState state = rule.getCompatibleCellState();
		state.setToEmptyState();

		return state;
	}

	/**
	 * Makes sure that an appropriate full state is selected. (For example, sets
	 * the number of states for integer states.)
	 * 
	 * @return
	 */
	private CellState getFullState()
	{
		CellState state = rule.getCompatibleCellState();
		state.setToFullState();

		return state;
	}

	/**
	 * Get a list of cells that are pinned.
	 */
	private LinkedList<Cell> getPinnedCells(Lattice lattice)
	{
		LinkedList<Cell> cellList = new LinkedList<Cell>();

		// the dimensions of the lattice
		int width = lattice.getWidth();
		int height = lattice.getHeight();
		int numberOfCellsOnLattice = width * height;
		if(OneDimensionalLattice.isCurrentLatticeOneDim(lattice
				.getDisplayName()))
		{
			numberOfCellsOnLattice = width;
			height = 1;
		}

		// separate logic so faster when percentage gets higher than 0.5 and
		// cellList.contains will be true too often
		if(randomPercent < 0.5)
		{
			// pin a percentage of random cells
			int numSelectedCells = 0;

			// create a temporary array of cells
			Cell[] cellArray = new Cell[numberOfCellsOnLattice];
			Iterator iterator = lattice.iterator();
			for(int i = 0; i < cellArray.length; i++)
			{
				cellArray[i] = (Cell) iterator.next();
			}

			while(numSelectedCells < randomPercent * numberOfCellsOnLattice)
			{
				// choose a random cell
				int rowPos = random.nextInt(height);
				int colPos = random.nextInt(width);
				if(!cellList.contains(cellArray[width * rowPos + colPos]))
				{
					cellList.add(cellArray[width * rowPos + colPos]);
					numSelectedCells++;
				}
			}
		}
		else
		{
			// create a list of all cells (and we'll remove from that list)
			LinkedList<Cell> tempList = new LinkedList<Cell>();
			Iterator iterator = lattice.iterator();
			while(iterator.hasNext())
			{
				tempList.add((Cell) iterator.next());
			}

			// keep removing cells until we are down to the correct size
			while(tempList.size() > randomPercent * numberOfCellsOnLattice)
			{
				// choose a random cell and remove it
				int randomCellPosition = random.nextInt(tempList.size());
				tempList.remove(randomCellPosition);
			}
			cellList = tempList;
		}

		// don't need to update the cells anymore (unless the user again changes
		// selections)
		randomPinnedCellsNeedUpdating = false;

		// tag all of the cells
		for(Cell cell : cellList)
		{
			cell.setTagged(true, this);
		}

		return cellList;
	}

	/**
	 * Each cell must have a unique state. Therefore, this creates a unique
	 * brand-new state of the correct type and value.
	 * 
	 * @return A cell state with the correct value.
	 */
	private CellState getPinnedState()
	{
		// the state that will be returned
		CellState pinnedState = null;

		// set the pinned state
		if(selectedState == FILLED_STATE_CHOICE)
		{
			pinnedState = getFullState();
		}
		else if(selectedState == EMPTY_STATE_CHOICE)
		{
			pinnedState = getEmptyState();
		}
		else if(selectedState == NO_STATE_CHOICE)
		{
			pinnedState = null;
		}
		else
		{
			pinnedState = getState(selectedState);
		}

		return pinnedState;
	}

	/**
	 * Get an appropriate state with the specified value.
	 * 
	 * @return a cell state with the specified value.
	 */
	private CellState getState(int stateValue)
	{
		CellState state = rule.getCompatibleCellState();
		((IntegerCellState) state).setState(stateValue);

		return state;
	}

	/**
	 * Reads and saves the spinner values for row and col.
	 */
	private void readAndSaveSpinnerValues()
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
	}

	/**
	 * Called by the constructor.
	 */
	private void setUpAnalysis()
	{
		// width and height of the lattice
		width = CurrentProperties.getInstance().getNumColumns();
		height = CurrentProperties.getInstance().getNumRows();

		if(OneDimensionalLattice.isCurrentLatticeOneDim())
		{
			height = 1;
		}

		// by default, draw the pinned cells
		drawPinnedCells = true;

		// don't prematurely select random cells for updating
		randomPinnedCellsNeedUpdating = false;

		// random number generator
		random = RandomSingleton.getInstance();

		// update, because this could change
		numStates = CurrentProperties.getInstance().getNumStates();

		// get the current view
		String ruleClassName = CurrentProperties.getInstance()
				.getRuleClassName();
		rule = ReflectionTool.instantiateFullRuleFromClassName(ruleClassName);
		view = rule.getCompatibleCellStateView();

		// use the occupied state
		selectedState = FILLED_STATE_CHOICE;

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
			lastSelectedState = FILLED_STATE_CHOICE;
		}

		// this is the panel that will be displayed (getDisplayPanel() will
		// return the panel that this creates)
		createDisplayPanel();

		// select the "all non-empty states" radio button. This must happen
		// after the display panel is created.
		filledStateButton.setSelected(true);

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

		// whenever the analysis is setup, make it redraw the tagged cells
		firstTimeThrough = true;
	}

	/**
	 * Pins cells at the specified value.
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
		try
		{
			// find out if we are dealing with integer cell states
			try
			{
				Cell c = (Cell) lattice.iterator().next();

				// this will fail if not an integer cell state
				IntegerCellState intCellState = (IntegerCellState) c
						.getState(generation);

				// update, because this could change
				numStates = CurrentProperties.getInstance().getNumStates();

				if(selectedState > numStates)
				{
					selectedState = numStates - 1;
					lastSelectedState = selectedState;
				}
			}
			catch(Exception e)
			{
				// do nothing -- we won't be using the numStates
			}

			// get the pinned cells (based on the user selection)
			if(randomPinnedCellsNeedUpdating)
			{
				listOfPinnedCells.addAll(getPinnedCells(lattice));
			}

			// this is necessary to avoid a concurrent modification error. The
			// following iteration (in the for loop) could be messed up by the
			// update method which adds and removes cells. This guarantees that
			// the adds and removes on listOfPinnedCells don't interfere.
			LinkedList<Cell> copyOfListofPinnedCells = new LinkedList<Cell>();
			copyOfListofPinnedCells.addAll(listOfPinnedCells);

			// pin each cell in our list
			for(Cell cell : copyOfListofPinnedCells)
			{
				if(cell != null)
				{
					// Don't pin the cell if it's not supposed to be pinned
					// (when it is null).
					//
					// Be sure to get a different state for each cell (otherwise
					// all the pinned cells will share the exact same state --
					// i.e., the exact same object with the exact same
					// hashcode).
					CellState pinnedState = getPinnedState();
					if(pinnedState != null)
					{
						cell.resetState(pinnedState);
					}
				}
			}

			if(firstTimeThrough)
			{
				// tell the CA to show the tagged cells
				refreshGraphics();
			}

			// only true the first time the analyze method is called
			firstTimeThrough = false;
		}
		catch(Exception e)
		{
			// ignore -- it's probably a concurrent modification error that
			// happened while copying the linked list. We can safely ignore
			// because the code will use the correct list of pinned cells at the
			// next time step. The user won't notice because this can only
			// happen if they are drawing while the CA is running.
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
		// untag the cells
		for(Cell c : listOfPinnedCells)
		{
			c.setTagged(false, this);
		}

		// empty the list
		listOfPinnedCells.clear();

		// and refresh the graphics so don't show the cells as tagged anymore
		refreshGraphics();
	}

	/**
	 * Reacts to buttons.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();

		if(command.equals(SELECT_STATE))
		{
			chooseAnalysisState();
		}
		else if(command.equals(ADD_PINNED_CELL))
		{
			// read the row and col from the spinners
			readAndSaveSpinnerValues();

			// add the cell at the row and col which were selected at the time
			// this button was pushed
			addCellToPinnedList(row, col);

			// rerun with the new cell
			rerunAnalysis();

			// and redisplay with the new pinned cell
			refreshGraphics();
		}
		else if(command.equals(REMOVE_LAST_PINNED_CELL))
		{
			if(listOfPinnedCells != null && !listOfPinnedCells.isEmpty())
			{
				// untag
				listOfPinnedCells.getLast().setTagged(false, this);

				// remove from list
				listOfPinnedCells.removeLast();

				// rerun without that last cell
				rerunAnalysis();

				// and redisplay without the last pinned cell
				refreshGraphics();
			}
		}
		else if(command.equals(REMOVE_ALL_PINNED_CELLS))
		{
			if(listOfPinnedCells != null)
			{
				// untag the cells
				for(Cell c : listOfPinnedCells)
				{
					c.setTagged(false, this);
				}

				listOfPinnedCells.clear();

				// rerun without any selected cells
				rerunAnalysis();

				// and redisplay without any pinned cells
				refreshGraphics();
			}
		}
		else if(command.equals(SUBMIT_PERCENT))
		{
			// untag the cells
			for(Cell c : listOfPinnedCells)
			{
				c.setTagged(false, this);
			}

			// now update the selected cells
			randomPinnedCellsNeedUpdating = true;
			firstTimeThrough = true;
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
	 * This method is overridden to handle mouse events on the CA graphics and
	 * interpret them as cells which should be tagged.
	 * <p>
	 * NOTE: this method is dangerous because it is called by a different
	 * thread, and it modifies the listOfPinnedCells from a different drawing
	 * thread. If the listOfPinnedCells is modified here (by an add or remove)
	 * at the same time the listOfPinnedCells is being iterated somewhere else,
	 * this will cause a concurrent modification error. (The problem is that an
	 * iteration fails whenever the elements of the underlying Collection are
	 * changed.)
	 * <p>
	 * So my solution is to only allow the analyze method (above) to iterate
	 * over a copy of the listOfPinnedCells. Then this thread can happily change
	 * the listOfPinnedCells without worrying about the concurrent modification.
	 * 
	 * @param listener
	 *            The mouse listener that called this method.
	 * @param mouseState
	 *            The MouseState that existed when a mouse event forced this
	 *            method to be automatically called. Note that a MouseState is
	 *            an inner class of the AnalysisDrawingListener.
	 */
	public void handleMouseEvents(AnalysisDrawingListener listener,
			MouseState mouseState)
	{
		try
		{
			// only do this if we are drawing the cells by hand
			if(drawPinnedCells)
			{
				// get the state of the mouse that triggered this method call
				boolean buttonPressed = mouseState.isButtonPressed();
				boolean dragging = mouseState.isDragging();
				boolean rightClicked = mouseState.isRightClicked();
				Cell cell = mouseState.getCell();
				int generation = mouseState.getGeneration();

				if(buttonPressed)
				{
					// might be null if there was no cell under the cursor
					if((cell != null) && (generation >= 0))
					{
						if(!rightClicked && dragging
								&& !listOfPinnedCells.contains(cell))
						{
							// separated this from below else-statements so will
							// happen faster

							// add and tag the cell
							addCellToPinnedList(cell);

							CellState pinnedState = getPinnedState();
							if(pinnedState != null)
							{
								cell.resetState(pinnedState);
							}
						}
						else if(rightClicked && dragging
								&& listOfPinnedCells.contains(cell))
						{
							// because the same cell may have been added to the
							// list multiple times
							while(listOfPinnedCells.contains(cell))
							{
								listOfPinnedCells.remove(cell);
							}

							// untag
							cell.setTagged(false, this);
						}
						else if(rightClicked && !dragging
								&& listOfPinnedCells.contains(cell))
						{
							// because the same cell may have been added to the
							// list multiple times
							while(listOfPinnedCells.contains(cell))
							{
								listOfPinnedCells.remove(cell);
							}

							// untag
							cell.setTagged(false, this);
						}
						else if(!rightClicked
								&& !listOfPinnedCells.contains(cell))
						{
							// add and tag the cell
							addCellToPinnedList(cell);

							CellState pinnedState = getPinnedState();
							if(pinnedState != null)
							{
								cell.resetState(pinnedState);
							}
						}

						// these two lines ensure that the graphics are redrawn
						// quickly.
						int xPos = mouseState.getXPos();
						int yPos = mouseState.getYPos();
						listener.drawCell(cell, xPos, yPos);
						listener.updateGraphics();
					}
				}
			}
		}
		catch(Exception e)
		{
			// do nothing if fail
		}
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
		}
	}

	/**
	 * Performs any necessary operations to reset the analysis. This method is
	 * called if the user resets the cellular automata, or selects a new
	 * simulation.
	 */
	public void reset()
	{
		drawCellsButton.setSelected(true);

		// untag the cells
		for(Cell c : listOfPinnedCells)
		{
			c.setTagged(false, this);
		}

		// empty the list of cells
		listOfPinnedCells.clear();

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
	 * Overrides this method so that regular mouse drawing behavior is suspended
	 * (the drawing and erasing of cells on the CA lattice). This analysis
	 * implements its own drawing behavior.
	 * 
	 * @return false by default.
	 */
	public boolean shouldSuspendMouseDrawing()
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
			stateValue = FILLED_STATE_CHOICE;
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

			// rerun so the change in the selected state are shown
			rerunAnalysis();

			// and redisplay with the new cell values
			refreshGraphics();
		}
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
			readAndSaveSpinnerValues();
		}
	}

	/**
	 * Decides what to do when the user selects the draw, random, or selected
	 * radio button options.
	 * 
	 * @author David Bahr
	 */
	private class PinnedChoiceListener implements ItemListener
	{
		public void itemStateChanged(ItemEvent event)
		{
			// remove the pinned cells, but only if the last selection was the
			// random choice
			// if(submitPercentButton.isEnabled())
			// {
			// // untag the cells
			// for(Cell c : listOfPinnedCells)
			// {
			// c.setTagged(false);
			// }
			//
			// listOfPinnedCells.clear();
			// }

			if(drawCellsButton.isSelected())
			{
				submitPercentButton.setEnabled(false);
				// percentSpinner.setEnabled(false);
				addPinnedCellButton.setEnabled(false);
				colSpinner.setEnabled(false);
				rowSpinner.setEnabled(false);

				drawPinnedCells = true;

				randomPinnedCellsNeedUpdating = false;
			}
			else if(selectCellsButton.isSelected())
			{
				submitPercentButton.setEnabled(false);
				// percentSpinner.setEnabled(false);
				addPinnedCellButton.setEnabled(true);
				colSpinner.setEnabled(true);

				if(OneDimensionalLattice.isCurrentLatticeOneDim())
				{
					rowSpinner.setEnabled(false);
				}
				else
				{
					rowSpinner.setEnabled(true);
				}

				drawPinnedCells = false;

				randomPinnedCellsNeedUpdating = false;
			}
			else if(randomCellsButton.isSelected())
			{
				submitPercentButton.setEnabled(true);
				// percentSpinner.setEnabled(true);
				addPinnedCellButton.setEnabled(false);
				colSpinner.setEnabled(false);
				rowSpinner.setEnabled(false);

				drawPinnedCells = false;

				randomPinnedCellsNeedUpdating = true;
			}

			// rerun with the new selection
			rerunAnalysis();

			// and redisplay with the new pinned cells
			refreshGraphics();
		}
	}

	/**
	 * listens for changes to the percent spinner.
	 */
	private class RandomPercentListener implements ChangeListener
	{
		/**
		 * Listens for changes to the random percent spinner.
		 * 
		 * @param e
		 */
		public void stateChanged(ChangeEvent e)
		{
			// read the percent number
			Double percentInteger = (Double) ((SpinnerNumberModel) percentSpinner
					.getModel()).getNumber();
			randomPercent = (percentInteger.doubleValue() / 100.0);
		}
	}

	/**
	 * Decides what to do when the user selects the empty, non-empty, or
	 * particular states.
	 * 
	 * @author David Bahr
	 */
	private class StateChoiceListener implements ItemListener
	{
		public void itemStateChanged(ItemEvent event)
		{
			if(filledStateButton.isSelected())
			{
				// save the last selected state (if it was a particular integer
				// state)
				if(selectedState != EMPTY_STATE_CHOICE
						&& selectedState != FILLED_STATE_CHOICE
						&& selectedState != NO_STATE_CHOICE)
				{
					lastSelectedState = selectedState;
				}

				// use all occupied states
				selectedState = FILLED_STATE_CHOICE;

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
						&& selectedState != FILLED_STATE_CHOICE
						&& selectedState != NO_STATE_CHOICE)
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
			else if(noStateButton.isSelected())
			{
				// save the last selected state (if it was a particular integer
				// state)
				if(selectedState != EMPTY_STATE_CHOICE
						&& selectedState != FILLED_STATE_CHOICE
						&& selectedState != NO_STATE_CHOICE)
				{
					lastSelectedState = selectedState;
				}

				// don't use any state
				selectedState = NO_STATE_CHOICE;

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
						&& lastSelectedState != FILLED_STATE_CHOICE
						&& lastSelectedState != NO_STATE_CHOICE
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

			// rerun so the change in the selected state are shown
			rerunAnalysis();
		}
	}
}
