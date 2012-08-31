/*
 LargestNeighborhoodAnalysis -- a class within the Cellular Automaton Explorer. 
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.rules.Rule;
import cellularAutomata.rules.templates.FiniteObjectRuleTemplate;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;

/**
 * Finds the largest neighborhoods on the lattice for any particular state. Most
 * applicable to lattices with variable sized neighborhoods like the small world
 * lattice.
 * 
 * @author David Bahr
 */
public class LargestNeighborhoodAnalysis extends Analysis implements
		ActionListener
{
	// the constant representing all states. Should not be any integer
	// between 0 and numStates (inclusive). Used to identify the user's choice
	// of state(s) to analyze.
	private static final int ALL_STATES_CHOICE = -3;

	// the constant representing all non-empty states. Should not be any integer
	// between 0 and numStates (inclusive). Used to identify the user's choice
	// of state(s) to analyze.
	private static final int ALL_NON_EMPTY_STATES_CHOICE = -1;

	// the constant representing the empty state. Should not be any integer
	// between 0 and numStates (inclusive). Used to identify the user's choice
	// of state(s) to analyze.
	private static final int EMPTY_STATE_CHOICE = -2;

	// The max number of cells that will be tagged by default
	private static int DEFAULT_MAX_TAGGED = 100;

	// a brief display name for this class
	private static final String ANALYSIS_NAME = "Largest Neighborhoods";

	// display info for this class
	private static final String INFO_MESSAGE = "Highly connected cells may have a "
			+ "disproportionately large influence.  Therefore, for any selected state, "
			+ "this analysis finds and highlights those cells with the largest "
			+ "neighborhoods.  \n\n"
			+ "This analysis is most useful for lattices with variable-sized "
			+ "neighborhoods. For example, try the small-world lattice or the random "
			+ "asymmetric lattice.";

	// title for panel that lets user choose the number of cells (with largest
	// neighborhoods)
	private static final String NUM_CELLS_TITLE = "Large neighborhoods";

	// tool tip for selecting the number of cells that will be tagged
	private static final String NUM_NEIGHBORHOODS_TIP = "<html><body>Select the number "
			+ "of cells that will tagged. The first cell <br>"
			+ "has the largest neighborhood, the second cell has the <br>"
			+ "second largest neighborhood, etc.</body></html>";

	// title for the subpanel that lets the user select the state to be analyzed
	private static final String RADIO_BUTTON_PANEL_TITLE = "Select state to analyze";

	// text for the button that lets the user select the state for which the
	// cluster sizes will be calculated.
	private static final String SELECT_STATE = "Select state";

	// tooltip for the button that lets the state be selected
	private static final String SELECT_STATE_TOOLTIP = "Select a state for which the \n"
			+ "neighborhood sizes will be calculated.";

	// message giving the size of the neighborhoods
	private static final String SIZE_MESSAGE = "Currently showing cells with "
			+ "neighborhoods of size: ";

	// label for the check box that shows neighbors
	private static final String SHOW_NEIGHBORS = "Show neighbors of the cells";

	// tool tip for the check box that shows neighbors
	private static final String SHOW_NEIGHBORS_TOOLTIP = "When selected, will show "
			+ "the neighbors of the cells";

	// the action command for the state chooser
	private static final String STATE_CHOOSER = "state chooser";

	// a brief tooltip description for this class
	private static final String TOOLTIP = "<html>highlights cell's with the largest "
			+ "neighborhoods</html>";

	// makes the graphics redraw the first time through
	private boolean firstTimeThrough = true;

	// shows the neighbors of the cells when selected
	private boolean showNeighbors = false;

	// cells that will be tagged (they have the largest neighborhoods)
	private CellSizePair[] cellsToTag = null;

	// the current view for the rule
	private CellStateView view = null;

	// the color of the current state
	private Color currentColor = Color.GRAY;

	// color of titles of sections
	private Color titleColor = Color.BLUE;

	// a color patch so the user can see the color being analyzed
	private ColorPatch colorPatch = null;

	// the state that was last selected by the user (used in the graphics)
	private int lastSelectedState = ALL_STATES_CHOICE;

	// the max number of cells that can be tagged
	private int maxTagged = DEFAULT_MAX_TAGGED;

	// number of cells that will be tagged
	private int numberOfCellsWithLargeNeighborhoodsToShow = 1;

	// the number of states in the current simulation
	private int numStates = 2;

	// the state that has been selected for analysis
	private int selectedState = 1;

	// the list of neighbors that have been tagged
	private LinkedList<Cell> neighborList = new LinkedList<Cell>();;

	// fonts for display
	private Fonts fonts = new Fonts();

	// title font (for titles of sections)
	private Font titleFont = new Fonts().getItalicSmallerFont();

	// used to select the state that will be analyzed
	private IntegerStateColorChooser integerColorChooser = null;

	// the button for selecting the state to be analyzed
	private JButton selectStateButton = null;

	// check box for showing the neighbors of the cells with large neighborhoods
	private JCheckBox showNeighborsCheckbox = null;

	// label for the current generation
	private JLabel generationDataLabel = null;

	// the panel where results are displayed
	private JPanel displayPanel = null;

	// radio button for choosing all states (i.e., all will be used to
	// calculate the neighborhood sizes)
	private JRadioButton allStatesButton = null;

	// radio button for choosing the empty state (that will be used to
	// calculate the neighborhood sizes)
	private JRadioButton emptyStateButton = null;

	// radio button for choosing the non-empty states (that will be used to
	// calculate the neighborhood sizes)
	private JRadioButton nonEmptyStatesButton = null;

	// radio button for choosing a particular state (that will be used to
	// calculate the neighborhood sizes)
	private JRadioButton particularStateButton = null;

	// selects the starting column position for which music will be played
	private JSpinner numberOfLargestNeighborhoodsSpinner = null;

	// label saying the size of the neighborhoods of the displayed cells
	private MultilineLabel sizeLabel = null;

	// the current rule
	private Rule rule = null;

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
	public LargestNeighborhoodAnalysis(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			setUpAnalysis();
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
	 * Create labels used to display the data for the population statistics.
	 */
	private void createDataDisplayLabels()
	{
		// if one is null, then they all are
		if(generationDataLabel == null)
		{
			generationDataLabel = new JLabel("");
		}
	}

	/**
	 * Create the panel used to display the population statistics.
	 */
	private void createDisplayPanel()
	{
		int displayWidth = CAFrame.tabbedPaneDimension.width;
		int displayHeight = 775;

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

		// create a panel that holds the select state radio buttons
		JPanel stateSelectionPanel = createStateRadioButtonPanel();

		// create a panel for the number of cells that will be tagged
		JPanel numCellsSpinnerPanel = createNumberOfLargestNeighborhoodsPanel();

		// create the labels for the display
		createDataDisplayLabels();

		// add all the components to the panel
		int row = 0;
		displayPanel.add(messagePanel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		displayPanel.add(numCellsSpinnerPanel, new GBC(1, row).setSpan(4, 1)
				.setFill(GBC.VERTICAL).setWeight(10.0, 10.0)
				.setAnchor(GBC.WEST).setInsets(1));

		row++;
		displayPanel.add(stateSelectionPanel, new GBC(1, row).setSpan(4, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
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
		AttentionPanel attentionPanel = new AttentionPanel("Big Neighborhoods");

		MultilineLabel messageLabel = new MultilineLabel(INFO_MESSAGE);
		messageLabel.setFont(fonts.getAnalysesDescriptionFont());
		messageLabel.setMargin(new Insets(6, 10, 2, 16));

		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		messagePanel.add(attentionPanel, BorderLayout.NORTH);
		messagePanel.add(messageLabel, BorderLayout.CENTER);

		return messagePanel;
	}

	private JPanel createNumberOfLargestNeighborhoodsPanel()
	{
		// create spinner for the col position
		SpinnerNumberModel numberModel = new SpinnerNumberModel(1, 1,
				maxTagged, 1);
		numberOfLargestNeighborhoodsSpinner = new JSpinner(numberModel);
		numberOfLargestNeighborhoodsSpinner
				.setToolTipText(NUM_NEIGHBORHOODS_TIP);
		numberOfLargestNeighborhoodsSpinner
				.addChangeListener(new NumberOfLargestNeighborhoodsListener());

		// info message
		MultilineLabel messageLabel = new MultilineLabel(
				"Shows cells with the largest neighborhoods.  For example, if 5 is selected, "
						+ "then the 5 cells with biggest neighborhoods are displayed. \n\n"
						+ "Note that if many cells have the same sized neighborhoods, then this "
						+ "picks the first ones starting from the bottom right side of the lattice. \n\n"
						+ "Only cells of the specified state are selected.");
		messageLabel.setFont(fonts.getItalicSmallerFont());
		messageLabel.setMargin(new Insets(1, 1, 15, 1));
		messageLabel.setColumns(30);

		// spinner label
		JLabel numCellsLabel = new JLabel("Number of cells:   ");

		// spinner panel
		JPanel spinnerPanel = new JPanel(new GridBagLayout());
		int rowNum = 0;
		spinnerPanel.add(numCellsLabel, new GBC(0, rowNum).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		spinnerPanel.add(numberOfLargestNeighborhoodsSpinner,
				new GBC(1, rowNum).setSpan(1, 1).setFill(GBC.NONE).setWeight(
						1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		// size message
		sizeLabel = new MultilineLabel(SIZE_MESSAGE);
		sizeLabel.setFont(fonts.getItalicSmallerFont());
		sizeLabel.setForeground(Color.RED);
		sizeLabel.setMargin(new Insets(10, 1, 1, 1));
		sizeLabel.setColumns(30);

		// create a check box for showing neighbors
		showNeighborsCheckbox = new JCheckBox(SHOW_NEIGHBORS);
		showNeighborsCheckbox.setSelected(false);
		showNeighborsCheckbox.setToolTipText(SHOW_NEIGHBORS_TOOLTIP);
		showNeighborsCheckbox.setActionCommand(SHOW_NEIGHBORS);
		showNeighborsCheckbox.addActionListener(this);
		JPanel showNeighborsPanel = new JPanel(new BorderLayout());
		showNeighborsPanel.setBorder(BorderFactory
				.createEmptyBorder(7, 7, 7, 7));
		showNeighborsPanel.add(BorderLayout.CENTER, showNeighborsCheckbox);

		// add all components to a single panel
		JPanel selectCellsPanel = new JPanel(new GridBagLayout());
		int row = 0;
		selectCellsPanel.add(messageLabel, new GBC(0, row).setSpan(2, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		selectCellsPanel.add(spinnerPanel, new GBC(0, row).setSpan(2, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1, 1, 10, 1));

		row++;
		selectCellsPanel.add(showNeighborsPanel, new GBC(0, row).setSpan(2, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
				.setInsets(1));

		row++;
		selectCellsPanel.add(sizeLabel, new GBC(0, row).setSpan(2, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		// create a bordered JPanel
		JPanel borderedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 5);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), NUM_CELLS_TITLE, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		Border compoundBorder = BorderFactory.createCompoundBorder(
				titledBorder, emptyBorder);
		borderedPanel.setBorder(compoundBorder);
		borderedPanel.add(selectCellsPanel);

		return borderedPanel;
	}

	/**
	 * Creates radio buttons to choose which state(s) will be used to calculate
	 * the fractal dimension.
	 */
	private JPanel createStateRadioButtonPanel()
	{
		allStatesButton = new JRadioButton("all states");
		allStatesButton.setFont(fonts.getPlainFont());
		allStatesButton.addItemListener(new StateChoiceListener());

		nonEmptyStatesButton = new JRadioButton("all non-empty states");
		nonEmptyStatesButton.setFont(fonts.getPlainFont());
		nonEmptyStatesButton.addItemListener(new StateChoiceListener());
		nonEmptyStatesButton.setSelected(false);

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
		group.add(allStatesButton);
		group.add(nonEmptyStatesButton);
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
		boxOfRadioButtons.add(allStatesButton);
		boxOfRadioButtons.add(Box.createVerticalStrut(verticalSpace));
		boxOfRadioButtons.add(nonEmptyStatesButton);
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
	 * Called by the constructor.
	 */
	private void setUpAnalysis()
	{
		// update, because this could change
		numStates = CurrentProperties.getInstance().getNumStates();

		// update, because this could change
		int width = CurrentProperties.getInstance().getNumColumns();
		int height = CurrentProperties.getInstance().getNumRows();
		if(OneDimensionalLattice.isCurrentLatticeOneDim())
		{
			height = 1;
		}

		// reset the max number of cells that can be selected, if necessary
		int numCells = width * height;
		if(numCells < DEFAULT_MAX_TAGGED)
		{
			maxTagged = numCells;
		}

		// get the current view
		String ruleClassName = CurrentProperties.getInstance()
				.getRuleClassName();
		rule = ReflectionTool.instantiateFullRuleFromClassName(ruleClassName);
		view = rule.getCompatibleCellStateView();

		// use all occupied states
		selectedState = ALL_STATES_CHOICE;

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
			lastSelectedState = ALL_STATES_CHOICE;
		}

		// this is the panel that will be displayed (getDisplayPanel() will
		// return the panel that this creates)
		if(displayPanel == null)
		{
			createDisplayPanel();
		}
		else if(numberOfLargestNeighborhoodsSpinner != null)
		{
			// number of cells that will be tagged
			numberOfCellsWithLargeNeighborhoodsToShow = 1;

			// reset the spinner
			SpinnerNumberModel numberModel = new SpinnerNumberModel(1, 1,
					maxTagged, 1);
			numberOfLargestNeighborhoodsSpinner.setModel(numberModel);
			numberOfLargestNeighborhoodsSpinner.invalidate();
		}

		// select the "all non-empty states" radio button. This must happen
		// after the display panel is created.
		allStatesButton.setSelected(true);

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
	}

	/**
	 * Finds the largest neighborhood.
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
		// find out if we are dealing with integer cell states
		boolean isIntegerCellState = true;
		try
		{
			Cell c = (Cell) lattice.iterator().next();

			// this will fail if not an integer cell state
			IntegerCellState intCellState = (IntegerCellState) c
					.getState(generation);

			// update, because this could change
			numStates = CurrentProperties.getInstance().getNumStates();
		}
		catch(Exception e)
		{
			isIntegerCellState = false;
		}

		// untag all previously tagged cells
		if(cellsToTag != null && cellsToTag.length > 0)
		{
			for(CellSizePair pair : cellsToTag)
			{
				pair.getCell().setTagged(false, this);
			}

			// untag the neighbors
			if(neighborList != null && neighborList.size() > 0)
			{
				for(Cell neighbor : neighborList)
				{
					neighbor.setTagged(false, this);
				}
			}
		}

		// true if we are looking for all cells rather than a
		// particular state
		boolean countingAllCells = false;
		if(selectedState == ALL_STATES_CHOICE)
		{
			countingAllCells = true;
		}

		// true if we are looking for all occupied cells rather than a
		// particular state
		boolean countingAllOccupiedCells = false;
		if(selectedState == ALL_NON_EMPTY_STATES_CHOICE)
		{
			countingAllOccupiedCells = true;
		}

		// true if the user has selected the empty cell states radio button
		// rather than a particular state
		boolean countingEmptyCells = false;
		if(selectedState == EMPTY_STATE_CHOICE)
		{
			countingEmptyCells = true;
		}

		// get an iterator over the lattice
		Iterator cellIterator = lattice.iterator();

		// create a list to hold the neighborhood size for each cell
		LinkedList<CellSizePair> listOfCellAndNeighborhoodSizes = new LinkedList<CellSizePair>();

		// go through the lattice and find each cell of the specified state
		Cell cell = null;
		while(cellIterator.hasNext())
		{
			// get the cell
			cell = (Cell) cellIterator.next();

			// the cell state
			CellState cellState = cell.getState(generation);

			// find out if the cell is in the state being counted in the
			// neighborhood sizes
			if(countingAllCells
					|| (countingEmptyCells && cellState.isEmpty())
					|| (countingAllOccupiedCells && !cellState.isEmpty())
					|| ((!countingEmptyCells && !countingAllOccupiedCells && !countingAllCells)
							&& isIntegerCellState && (cellState.toInt() == selectedState)))
			{
				// now get the number of neighbors
				Cell[] neighboringCells = lattice.getNeighbors(cell);
				int numNeighbors = neighboringCells.length;

				// now save the cluster number
				listOfCellAndNeighborhoodSizes.add(new CellSizePair(cell,
						numNeighbors));
			}
		}

		// now dump to an array
		if(!listOfCellAndNeighborhoodSizes.isEmpty())
		{
			CellSizePair[] cellSizeArray = listOfCellAndNeighborhoodSizes
					.toArray(new CellSizePair[1]);

			// now sort the array (from smallest to largest)
			Arrays.sort(cellSizeArray, null);

			// make an array of the cells that will be tagged (they are at the
			// end of the array because it is sorted from smallest to largest)
			cellsToTag = Arrays.copyOfRange(cellSizeArray, cellSizeArray.length
					- numberOfCellsWithLargeNeighborhoodsToShow,
					cellSizeArray.length);
		}
		else
		{
			cellsToTag = null;
		}

		// now tag the specified cells (and get a list of the sizes)
		String sizes = "";
		if(cellsToTag != null)
		{
			for(CellSizePair pair : cellsToTag)
			{
				pair.getCell().setTagged(true, this);
				if(sizes.equals(""))
				{
					sizes += " " + pair.getNeighborhoodSize();
				}
				else
				{
					sizes += ", " + pair.getNeighborhoodSize();
				}
			}
		}

		if(sizes.equals(""))
		{
			sizes = "none";
		}

		// update the size label
		sizeLabel.setText(SIZE_MESSAGE + sizes);

		// set the text for the labels
		generationDataLabel.setText("" + generation);

		// tag the neighbors
		if(showNeighbors && cellsToTag != null)
		{
			// get all the neighbors (which may change at every time step)
			neighborList.clear();
			for(CellSizePair pair : cellsToTag)
			{
				Cell[] neighbors = lattice.getNeighbors(pair.getCell());

				// get the neighbors
				neighborList.addAll(Arrays.asList(neighbors));
			}

			// tag the neighbors
			if(neighborList != null && neighborList.size() > 0)
			{
				for(Cell neighbor : neighborList)
				{
					neighbor.setTagged(true, this);
				}
			}
		}

		// redisplay
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
		// untag all previously tagged cells
		if(cellsToTag != null && cellsToTag.length > 0)
		{
			for(CellSizePair pair : cellsToTag)
			{
				pair.getCell().setTagged(false, this);
			}

			// untag the neighbors
			if(neighborList != null && neighborList.size() > 0)
			{
				for(Cell neighbor : neighborList)
				{
					neighbor.setTagged(false, this);
				}
			}
		}

		// and refresh the graphics so don't show the cells as tagged anymore
		refreshGraphics();
	}

	/**
	 * Reacts to the "save data" check box.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();

		if(command.equals(SELECT_STATE))
		{
			chooseAnalysisState();
		}
		else if(command.equals(SHOW_NEIGHBORS))
		{
			showNeighbors = showNeighborsCheckbox.isSelected();

			// rerun so the changes are shown on the plot
			rerunAnalysis();

			refreshGraphics();
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
	 * Performs any necessary operations to reset the analysis. this method is
	 * called if the user resets the cellular automata, or selects a new
	 * simulation.
	 */
	public void reset()
	{
		// untags the cells
		stopAnalysis();

		// get rid of the previously existing cells
		cellsToTag = null;

		// reset the analysis parameters
		setUpAnalysis();

		firstTimeThrough = true;

		// reanalyze
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
			Shape shape = view.getDisplayShape(new IntegerCellState(stateValue), this.getWidth(), this.getHeight(), null);
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
			stateValue = ALL_STATES_CHOICE;
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
							stateValue), this.getWidth(), this
							.getHeight(), new Coordinate(0, 0));
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
	 * listens for changes to the NumberOfLargestNeighborhoodsSpinner.
	 */
	private class NumberOfLargestNeighborhoodsListener implements
			ChangeListener
	{
		/**
		 * Listens for changes to the row and col spinners.
		 * 
		 * @param e
		 */
		public void stateChanged(ChangeEvent e)
		{
			if(numberOfLargestNeighborhoodsSpinner != null)
			{
				numberOfCellsWithLargeNeighborhoodsToShow = ((Integer) ((SpinnerNumberModel) numberOfLargestNeighborhoodsSpinner
						.getModel()).getNumber()).intValue();

				// redo analysis with this new number of cells
				rerunAnalysis();
				refreshGraphics();
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

			// rerun so the changes are shown on the plot
			rerunAnalysis();

			refreshGraphics();
		}
	}

	/**
	 * Decides what to do when the user selects a the empty, non-empty, or
	 * particular states.
	 * 
	 * @author David Bahr
	 */
	private class StateChoiceListener implements ItemListener
	{
		public void itemStateChanged(ItemEvent event)
		{
			if(nonEmptyStatesButton.isSelected())
			{
				// save the last selected state (if it was a particular integer
				// state)
				if(selectedState != EMPTY_STATE_CHOICE
						&& selectedState != ALL_NON_EMPTY_STATES_CHOICE
						&& selectedState != ALL_STATES_CHOICE)
				{
					lastSelectedState = selectedState;
				}

				// use all states
				selectedState = ALL_NON_EMPTY_STATES_CHOICE;

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
						&& selectedState != ALL_NON_EMPTY_STATES_CHOICE
						&& selectedState != ALL_STATES_CHOICE)
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
			else if(allStatesButton.isSelected())
			{
				// save the last selected state (if it was a particular integer
				// state)
				if(selectedState != EMPTY_STATE_CHOICE
						&& selectedState != ALL_NON_EMPTY_STATES_CHOICE
						&& selectedState != ALL_STATES_CHOICE)
				{
					lastSelectedState = selectedState;
				}

				// use only empty states
				selectedState = ALL_STATES_CHOICE;

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
						&& lastSelectedState != ALL_NON_EMPTY_STATES_CHOICE
						&& lastSelectedState != ALL_STATES_CHOICE
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

			// rerun so the changes are shown on the plot
			rerunAnalysis();

			refreshGraphics();
		}
	}

	/**
	 * Holds a cell and the size of its neighborhood.
	 * 
	 * @author David Bahr
	 */
	private class CellSizePair implements Comparable<CellSizePair>
	{
		private Cell cell = null;

		private int neighborhoodSize = 0;

		public CellSizePair(Cell cell, int neighborhoodSize)
		{
			this.cell = cell;
			this.neighborhoodSize = neighborhoodSize;
		}

		/**
		 * Compares this object to the object that is passed in.
		 * 
		 * @return 1, 0, or -1 if this object is greater than equal or less than
		 *         the object passed in.
		 */
		public int compareTo(CellSizePair pair)
		{
			// should be 0 if the two objects are equal
			int comparison = 0;
			if(this.neighborhoodSize > pair.getNeighborhoodSize())
			{
				// this object is greater than
				comparison = 1;
			}
			else if(this.neighborhoodSize < pair.getNeighborhoodSize())
			{
				// this object is less than
				comparison = -1;
			}

			return comparison;
		}

		/**
		 * Get the cell.
		 * 
		 * @return The cell.
		 */
		public Cell getCell()
		{
			return cell;
		}

		/**
		 * Get the neighborhood size.
		 * 
		 * @return The size.
		 */
		public int getNeighborhoodSize()
		{
			return neighborhoodSize;
		}
	}
}
