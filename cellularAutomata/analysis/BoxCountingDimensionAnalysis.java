/*
 BoxCountingDimensionAnalysis -- a class within the Cellular Automaton Explorer. 
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
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import cellularAutomata.CAController;
import cellularAutomata.CurrentProperties;
import cellularAutomata.Cell;
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
import cellularAutomata.util.SimplePlot;
import cellularAutomata.util.files.FileWriter;

/**
 * Estimates the fractal dimension of a CA using the box-counting dimension.
 * 
 * @author David Bahr
 */
public class BoxCountingDimensionAnalysis extends Analysis implements
		ActionListener
{
	// the constant representing all non-empty states. Should not be any integer
	// between 0 and numStates (inclusive). Used to identify the user's choice
	// of state(s) to analyze.
	private static final int ALL_NON_EMPTY_STATES_CHOICE = -1;

	// the constant representing the empty state. Should not be any integer
	// between 0 and numStates (inclusive). Used to identify the user's choice
	// of state(s) to analyze.
	private static final int EMPTY_STATE_CHOICE = -2;

	// the maximum number of elements that will be plotted
	private static final int MAX_NUMBER_TO_PLOT = 100;

	// a brief display name for this class
	private static final String ANALYSIS_NAME = "Fractal Box-Counting Dimension";

	// the colored title on the display
	private static final String ATTENTION_PANEL_TITLE = "Fractal Box Count";

	// title for the subpanel that displays the data
	private static final String DATA_PANEL_TITLE = "Data";

	// The pattern used to display decimals, particularly for the fractal
	// dimension.
	private static final String DECIMAL_PATTERN = "0.000";

	// title for the subpanel that lets the user select the state to be analyzed
	private static final String RADIO_BUTTON_PANEL_TITLE = "Select state to analyze";

	// text for the button that lets the user select the state for which the
	// fractal dimension will be calculated.
	private static final String SELECT_STATE = "Select state";

	// tooltip for the button that lets the state be selected
	private static final String SELECT_STATE_TOOLTIP = "Select a state for which the \n"
			+ "fractal dimension will be calculated.";

	// display info for this class
	private static final String INFO_MESSAGE = "Estimates the fractal dimension of the "
			+ "cellular automata using the box-counting dimension. The dimension is "
			+ "calculated for the image as show on the screen (without consideration for "
			+ "wrap-around or other boundary conditions). Larger lattices are more accurate.\n\n"
			+ "The plot shows the fractal dimension at each generation. The dimension D "
			+ "is calculated from the number of squares N(s) of side s required to cover the "
			+ "set. In particular, D = log(N)/log(1/s).  \n\n"
			+ "In one-dimensional simulations, the estimated dimension may show transients "
			+ "until the CA fills the screen. \n\n"
			+ "The box-counting (or similarity) dimension is most accurate for self-similar "
			+ "cellular automata like \"Rule 102\" started from a single seed.";

	// the action command for saving the data and the label used by the "save
	// data" check box
	private static final String SAVE_DATA = "   Save the data";

	// a tooltip for the save data check box
	private static final String SAVE_DATA_TOOLTIP = "<html>"
			+ "Saves fractal dimension data to a file (saves <br> "
			+ "every generation while the box is checked).</html>";

	// the action command for the state chooser
	private static final String STATE_CHOOSER = "state chooser";

	// a brief tooltip description for this class
	private static final String TOOLTIP = "<html>estimates fractal dimension using <br>"
			+ "the box-counting technique</html>";

	// the list of cells (used in the one-dimensional analysis
	private static ArrayList<CellValue> cellValueList = null;

	// true if the lattice is one dimensional -- set in the constructor
	private boolean isOneDimensional = false;

	// the current view for the rule
	private CellStateView view = null;

	// the color of the current state
	private Color currentColor = Color.GRAY;

	// color of titles of sections
	private Color titleColor = Color.BLUE;

	// a color patch so the user can see the color being analyzed
	private ColorPatch colorPatch = null;

	// the estimated fractal dimension
	private double fractalDimension = 0.0;

	// If the user wants to save the data to a file, this will be instantiated
	private FileWriter fileWriter = null;

	// fonts for display
	private Fonts fonts = new Fonts();

	// title font (for titles of sections)
	private Font titleFont = new Fonts().getItalicSmallerFont();

	// the state that was last selected by the user (used in the graphics)
	private int lastSelectedState = ALL_NON_EMPTY_STATES_CHOICE;

	// the total number of cells in the fractal
	private int numCellsInFractal = 0;

	// number of columns in the CA
	private int numCols = 0;

	// number of rows in the CA
	private int numRows = 0;

	// the number of states in the current simulation
	private int numStates = 2;

	// the state that has been selected for analysis
	private int selectedState = 0;

	// the state used in the analysis at the last generation
	private int selectedStateAtLastTimeStep = 0;

	// the total number of cells
	private int totalNumberOfCellsInCA = 0;

	// used to select the state that will be analyzed
	private IntegerStateColorChooser integerColorChooser = null;

	// the button for selecting the state to be analyzed
	private JButton selectStateButton = null;

	// The check box that lets the user save the data
	private JCheckBox saveDataCheckBox = null;

	// label for the fractal dimension
	private JLabel fractalDimensionDataLabel = null;

	// label for the current generation
	private JLabel generationDataLabel = null;

	// label for the number of points on the fractal
	private JLabel numPointsInFractalDataLabel = null;

	// the panel where results are displayed
	private JPanel displayPanel = null;

	// radio button for choosing the empty state (that will be used to
	// calculate the fractal dimension)
	private JRadioButton emptyStateButton = null;

	// radio button for choosing the non-empty states (that will be used to
	// calculate the fractal dimension)
	private JRadioButton nonEmptyStatesButton = null;

	// radio button for choosing a particular state (that will be used to
	// calculate the fractal dimension)
	private JRadioButton particularStateButton = null;

	// the list of the fractal dimension points that will be drawn on the
	// fractalDimensionPlot
	private LinkedList fractalDimensionList = new LinkedList();

	// the list of colors for the fractal dimension points that will be drawn on
	// the fractalDimensionPlot
	private LinkedList<Color> fractalDimensionColorList = new LinkedList<Color>();

	// the current rule
	private Rule rule = null;

	// a panel that plots the correlation function data
	private SimplePlot fractalDimensionPlot = null;

	// a delimiter for spacing data in the data file
	private String delimiter = null;

	// the data that will be saved to a file
	private String[] data = new String[3];

	/**
	 * Create an analyzer that estimates the fractal dimension of a CA.
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
	public BoxCountingDimensionAnalysis(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			setUpTheAnalysis();
		}
	}

	/**
	 * To the total count, this adds the number of cells that are on the fractal
	 * in the latest (last) row.
	 */
	private void countCellsInLastRow()
	{
		// add the count from the cells on the last row
		for(int cellNumber = cellValueList.size() - numCols; cellNumber < cellValueList
				.size(); cellNumber++)
		{
			// if the cell is occupied, then remove it from the count
			if((selectedState == ALL_NON_EMPTY_STATES_CHOICE && (cellValueList
					.get(cellNumber).value != 0))
					|| (selectedState == EMPTY_STATE_CHOICE && (cellValueList
							.get(cellNumber).value == 0))
					|| (selectedState != ALL_NON_EMPTY_STATES_CHOICE
							&& selectedState != EMPTY_STATE_CHOICE && cellValueList
							.get(cellNumber).value == selectedState))
			{
				numCellsInFractal++;
			}
		}
	}

	/**
	 * Finds the number of cells that are on the fractal.
	 */
	private void countCellsInFractal()
	{
		// reset so we can count the number of cells in the fractal
		numCellsInFractal = 0;

		for(int cellNumber = 0; cellNumber < cellValueList.size(); cellNumber++)
		{
			// if the cell is occupied, then count it
			if((selectedState == ALL_NON_EMPTY_STATES_CHOICE && (cellValueList
					.get(cellNumber).value != 0))
					|| (selectedState == EMPTY_STATE_CHOICE && (cellValueList
							.get(cellNumber).value == 0))
					|| (selectedState != ALL_NON_EMPTY_STATES_CHOICE
							&& selectedState != EMPTY_STATE_CHOICE && cellValueList
							.get(cellNumber).value == selectedState))
			{
				// we have another "occupied" cell that is in the fractal
				numCellsInFractal++;
			}
		}
	}

	/**
	 * Takes the list of one-dimensional cells and adds a new row of cells to
	 * the list.
	 * 
	 * @param lattice
	 *            The CA lattice.
	 * @param generation
	 *            The current generation.
	 * @param row
	 *            The row of cells that will be added to the CA. Starts counting
	 *            from 0.
	 * @param numberOfRows
	 *            The current number of rows displayed by the CA. Starts
	 *            counting from 1.
	 */
	private void addToTheListAllCellsInARowOfOneDimCA(Lattice lattice,
			int generation, int row, int numberOfRows)
	{
		// we will go through every cell
		Iterator cellIterator = lattice.iterator();

		for(int col = 0; col < numCols; col++)
		{
			Cell cell = (Cell) cellIterator.next();

			// get the row position of the cell. Note: the row position is
			// the generation at which the cell was created.
			int rowPosition = (generation - numberOfRows) + (row + 1);

			// have to get the cell state from the history given by
			// the row
			CellState state = cell.getState(rowPosition);

			try
			{
				// get the integer value. Handy if we later want to
				// distinguish fractals for each state value (in
				// addition to just occupied versus empty).
				IntegerCellState intCellState = (IntegerCellState) state;

				cellValueList.add(new CellValue(intCellState.getState()));
			}
			catch(Exception e)
			{
				// not an IntegerCellState, so base this on whether the
				// cell is empty or occupied by some value
				if(cell.getState(generation).isEmpty())
				{
					// cell is not occupied, so store a 0.
					cellValueList.add(new CellValue(0));
				}
				else
				{
					// cell is occupied, so store a 1.
					cellValueList.add(new CellValue(1));
				}
			}
		}
	}

	/**
	 * Estimates the fractal dimension of a one-dimensional CA. Uses an
	 * algorithm that is faster than the two-dimensional algorithm.
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
	private void analyzeOneDim(Lattice lattice, Rule rule, int generation)
	{
		// get the number of rows calculated by the CA (in one-dim is given by
		// the number of saved generations)
		int numberOfRows = numRows;
		Iterator cellIterator = lattice.iterator();
		numberOfRows = ((Cell) cellIterator.next()).getStateHistory().size();
		if(numberOfRows > numRows)
		{
			numberOfRows = numRows;
		}

		// this is only necessary in one-dimension. In two-dim, the analysis
		// always does a complete recount anyway.
		if(selectedState != selectedStateAtLastTimeStep)
		{
			// then force a total recount
			cellValueList = null;
		}

		// revise data if the user drew on the lattice
		if(cellValueList != null && userDrewNewCells(lattice, generation))
		{
			// remove last row count (because it is now incorrect)
			removeLastRowCount();

			// remove the last row (it has changed and needs replacing)
			for(int col = 0; col < numCols; col++)
			{
				cellValueList.remove(cellValueList.size() - 1);
			}

			// and add back the revised row (it's now two rows back)
			addToTheListAllCellsInARowOfOneDimCA(lattice, generation,
					numberOfRows - 2, numberOfRows);

			// finally, take the previous count and add the count from the last
			// row.
			countCellsInLastRow();
		}

		// the total number of cells
		totalNumberOfCellsInCA = numberOfRows * numCols;

		// get every cell value and put in an ArrayList.
		if(cellValueList == null)
		{
			cellValueList = new ArrayList<CellValue>();

			// arrayList is empty so we need to fill it with whatever data is
			// available
			for(int row = 0; row < numberOfRows; row++)
			{
				addToTheListAllCellsInARowOfOneDimCA(lattice, generation, row,
						numberOfRows);
			}

			// count cells.
			countCellsInFractal();
		}
		else if(cellValueList.size() < numRows * numCols)
		{
			// then the image hasn't filled the screen yet and we just want to
			// add the last (newest) row of cells to the
			// cellValueList
			addToTheListAllCellsInARowOfOneDimCA(lattice, generation,
					numberOfRows - 1, numberOfRows);

			// count cells from the new row
			countCellsInLastRow();
		}
		else
		{
			// remove the count due to the first row, which is about to
			// disappear.
			removeFirstRowCount();

			// Now remove the first row (it has disappeared off the top of the
			// screen)
			for(int col = 0; col < numCols; col++)
			{
				cellValueList.remove(0);
			}

			// and add the new (last) row
			addToTheListAllCellsInARowOfOneDimCA(lattice, generation,
					numberOfRows - 1, numberOfRows);

			// finally, take the previous count and add the count from the last
			// row.
			countCellsInLastRow();
		}

		// now calculate the fractal dimension from a linear regression of the
		// function C(r)
		calculateFractalDimension(generation);

		// now plot and save the fractal data
		plotAndSaveFractalData(generation);

		// remember the state we used this time (we check it next time to
		// see if it changed)
		selectedStateAtLastTimeStep = selectedState;
	}

	/**
	 * Estimates the fractal dimension of a two-dimensional CA. Uses an
	 * algorithm that is specific to two-dimensional CA.
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
	private void analyzeTwoDim(Lattice lattice, Rule rule, int generation)
	{
		// the total number of cells in the CA
		totalNumberOfCellsInCA = numRows * numCols;

		// create a new arrayList (must be recreated every time step
		cellValueList = new ArrayList<CellValue>();

		// get every cell value and put in the arrayList.
		Iterator cellIterator = lattice.iterator();
		for(int row = 0; row < numRows; row++)
		{
			for(int col = 0; col < numCols; col++)
			{
				Cell cell = (Cell) cellIterator.next();
				CellState state = cell.getState(generation);

				try
				{
					// get the integer value. Handy if we later want to
					// distinguish fractals for each state value (in addition to
					// just occupied versus empty).
					IntegerCellState intCellState = (IntegerCellState) state;

					cellValueList.add(new CellValue(intCellState.getState()));
				}
				catch(Exception e)
				{
					// not an IntegerCellState, so base this on whether the cell
					// is empty or occupied by some value
					if(cell.getState(generation).isEmpty())
					{
						// cell is not occupied, so store a 0
						cellValueList.add(new CellValue(0));
					}
					else
					{
						// cell is occupied, so store a 1
						cellValueList.add(new CellValue(1));
					}
				}
			}
		}

		// get the radius between each pair of cells
		countCellsInFractal();

		// now calculate the fractal dimension
		calculateFractalDimension(generation);

		// now plot and save the fractal data
		plotAndSaveFractalData(generation);
	}

	/**
	 * Calculates the fractal dimension from the box-counting dimension.
	 * 
	 * @param generation
	 *            The CA's current generation.
	 */
	private void calculateFractalDimension(int generation)
	{
		if((numCellsInFractal == totalNumberOfCellsInCA)
				|| (numCellsInFractal == totalNumberOfCellsInCA - 1))
		{
			// then two-dim
			fractalDimension = 2.0;
		}
		else if((numCellsInFractal == 0) || (numCellsInFractal == 1))
		{
			// then zero-dim
			fractalDimension = 0.0;
		}
		else
		{
			// not zero- or two-dim, so use box-counting dimension to estimate
			// the fractal dimension
			double sideOfBox = 1.0 / Math.max(numRows, numCols);
			fractalDimension = Math.log(numCellsInFractal)
					/ Math.log(1.0 / sideOfBox);
		}

		// save fractal dimension in a linked list for plotting
		fractalDimensionList.add(new Point2D.Double(generation,
				fractalDimension));
		if(fractalDimensionList.size() > MAX_NUMBER_TO_PLOT)
		{
			fractalDimensionList.removeFirst();
		}

		// save the color to be plotted
		Color stateColor = Color.BLACK;
		if(selectedState != ALL_NON_EMPTY_STATES_CHOICE)
		{
			int stateWeArePlotting = selectedState;
			if(selectedState == EMPTY_STATE_CHOICE)
			{
				stateWeArePlotting = 0;
			}
			String ruleClassName = CurrentProperties.getInstance()
					.getRuleClassName();
			Rule rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(ruleClassName);
			CellStateView view = rule.getCompatibleCellStateView();
			if(IntegerCellState.isCompatibleRule(rule))
			{
				stateColor = view.getDisplayColor(new IntegerCellState(
						stateWeArePlotting), null, new Coordinate(0, 0));
			}
		}
		fractalDimensionColorList.add(stateColor);
		if(fractalDimensionColorList.size() > MAX_NUMBER_TO_PLOT)
		{
			fractalDimensionColorList.removeFirst();
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
	 * Create labels used to display the data for the fractal statistics.
	 */
	private void createDataDisplayLabels()
	{
		// if one is null, then they all are
		if(generationDataLabel == null)
		{
			generationDataLabel = new JLabel("");
			numPointsInFractalDataLabel = new JLabel("");
			fractalDimensionDataLabel = new JLabel("");

			// make the dimension stand out
			fractalDimensionDataLabel.setForeground(Color.RED);
		}
	}

	/**
	 * Create panel that holds the data for the fractal statistics.
	 */
	private JPanel createDataPanel()
	{
		// create the labels for the display
		createDataDisplayLabels();
		JLabel generationLabel = new JLabel("Generation:   ");
		JLabel numPointsInFractalLabel = new JLabel("# of cells on fractal:   ");
		JLabel fractalDimensionLabel = new JLabel(
				"Estimated fractal dimension:   ");

		// create boxes for each column of the display (a Box uses the
		// BoxLayout, so it is handy for laying out components)
		Box boxOfNameLabels = Box.createVerticalBox();
		Box boxOfDataLabels = Box.createVerticalBox();

		// the amount of vertical and horizontal space to put between components
		int verticalSpace = 5;
		int horizontalSpace = 50;

		// add the name labels to the first vertical box
		boxOfNameLabels.add(generationLabel);
		boxOfNameLabels.add(Box.createVerticalStrut(verticalSpace));
		boxOfNameLabels.add(numPointsInFractalLabel);
		boxOfNameLabels.add(Box.createVerticalStrut(verticalSpace));
		boxOfNameLabels.add(fractalDimensionLabel);

		// add the data labels to the second vertical box
		boxOfDataLabels.add(generationDataLabel);
		boxOfDataLabels.add(Box.createVerticalStrut(verticalSpace));
		boxOfDataLabels.add(numPointsInFractalDataLabel);
		boxOfDataLabels.add(Box.createVerticalStrut(verticalSpace));
		boxOfDataLabels.add(fractalDimensionDataLabel);

		// create another box that holds both of the label boxes
		Box boxOfLabels = Box.createHorizontalBox();
		boxOfLabels.add(boxOfNameLabels);
		boxOfLabels.add(Box.createHorizontalStrut(horizontalSpace));
		boxOfLabels.add(boxOfDataLabels);

		JPanel dataPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		dataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), DATA_PANEL_TITLE, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor));
		dataPanel.add(boxOfLabels);

		return dataPanel;
	}

	/**
	 * Create the panel used to display the fractal statistics.
	 */
	private void createDisplayPanel()
	{
		if(displayPanel == null)
		{
			// create the display panel
			displayPanel = new JPanel(new GridBagLayout());
			displayPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			displayPanel.setPreferredSize(new Dimension(
					CAFrame.tabbedPaneDimension.width, 850));

			// create a panel that displays messages
			JPanel messagePanel = createMessagePanel();

			// create the panel that holds the fractal statistics
			JPanel dataPanel = createDataPanel();

			// create a panel that holds the select state radio buttons
			JPanel stateSelectionPanel = createStateRadioButtonPanel();

			// create a "save data" check box
			saveDataCheckBox = new JCheckBox(SAVE_DATA);
			saveDataCheckBox.setToolTipText(SAVE_DATA_TOOLTIP);
			saveDataCheckBox.setActionCommand(SAVE_DATA);
			saveDataCheckBox.addActionListener(this);
			JPanel saveDataPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			saveDataPanel
					.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
			saveDataPanel.add(saveDataCheckBox);

			// create a panel that holds both the boxOfLabels and the save
			// and selectState button
			JPanel saveAndLabelsPanel = new JPanel(new BorderLayout());
			saveAndLabelsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5,
					5, 5));
			saveAndLabelsPanel.add(dataPanel, BorderLayout.NORTH);
			saveAndLabelsPanel.add(stateSelectionPanel, BorderLayout.CENTER);
			saveAndLabelsPanel.add(saveDataPanel, BorderLayout.SOUTH);

			// get the data plots
			JPanel plotPanel = createPlotPanel();

			// add everything to the display (using BorderLayout)
			// displayPanel.add(messagePanel, BorderLayout.NORTH);
			// displayPanel.add(plotPanel, BorderLayout.CENTER);
			// displayPanel.add(saveAndLabelsPanel, BorderLayout.SOUTH);

			displayPanel.setLayout(new GridBagLayout());
			int row = 0;
			displayPanel.add(messagePanel, new GBC(0, row).setSpan(1, 1)
					.setFill(GBC.BOTH).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
					.setInsets(1));
			row++;
			displayPanel.add(plotPanel, new GBC(0, row).setSpan(1, 1).setFill(
					GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
					.setInsets(1));
			row++;
			displayPanel.add(saveAndLabelsPanel, new GBC(0, row).setSpan(1, 1)
					.setFill(GBC.BOTH).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
					.setInsets(1));
		}
	}

	/**
	 * Creates radio buttons to choose which state(s) will be used to calculate
	 * the fractal dimension.
	 */
	private JPanel createStateRadioButtonPanel()
	{
		nonEmptyStatesButton = new JRadioButton("non-empty states");
		nonEmptyStatesButton.setFont(fonts.getPlainFont());
		nonEmptyStatesButton.addItemListener(new StateChoiceListener());
		nonEmptyStatesButton.setSelected(true);

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

		// create boxes for each column of the display (a Box uses the
		// BoxLayout, so it is handy for laying out components)
		Box boxOfRadioButtons = Box.createVerticalBox();
		Box boxWithColorPatch = Box.createVerticalBox();

		// the amount of vertical and horizontal space to put between components
		int verticalSpace = 5;
		int horizontalSpace = 0;

		// add the radio buttons to the first vertical box
		boxOfRadioButtons.add(nonEmptyStatesButton);
		boxOfRadioButtons.add(Box.createVerticalStrut(verticalSpace));
		boxOfRadioButtons.add(emptyStateButton);
		boxOfRadioButtons.add(Box.createVerticalStrut(verticalSpace));
		boxOfRadioButtons.add(particularStateButton);

		// add the color patch to the second vertical box
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
		radioPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), RADIO_BUTTON_PANEL_TITLE,
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor));
		radioPanel.add(boxOfLabels);

		return radioPanel;
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
			String[] header = {"Generation: ", "# of cells on the fractal",
					"Fractal dimension: "};
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
	 * Create a JPanel that holds all of the plots.
	 * 
	 * @return panel holding the data plots.
	 */
	private JPanel createPlotPanel()
	{
		// create a panel that plots the fractal dimension at each time step
		fractalDimensionPlot = new SimplePlot();

		// put the above in a single panel
		JPanel plotPanel = new JPanel(new GridBagLayout());
		plotPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		// correlation function plot
		int row = 0;
		plotPanel.add(fractalDimensionPlot, new GBC(0, row).setSpan(10, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		return plotPanel;
	}

	/**
	 * Plots and saves all of the fractal dimension data.
	 * 
	 * @param generation
	 *            The current generation.
	 */
	private void plotAndSaveFractalData(int generation)
	{
		// set the text for the labels
		generationDataLabel.setText("" + generation);
		numPointsInFractalDataLabel.setText("" + numCellsInFractal);

		// and set the text for the fractalDimension label, but format!
		DecimalFormat myFormatter = new DecimalFormat(DECIMAL_PATTERN);
		String fractalOutput = myFormatter.format(fractalDimension);
		fractalDimensionDataLabel.setText(fractalOutput);

		// create an array of data to be saved
		data[0] = "" + generation;
		data[1] = "" + numCellsInFractal;
		data[2] = "" + fractalDimension;

		// see if user wants to save the data
		if(fileWriter != null)
		{
			// save it
			saveData(data);
		}

		// plot the fractal dimension data
		plotFractalDimensionData();
	}

	/**
	 * Plots the fractal dimension data.
	 */
	private void plotFractalDimensionData()
	{
		if(fractalDimensionList.size() > 0)
		{
			// set the min and max X values on the fractalDimensionPlot
			Point2D firstPoint = (Point2D) fractalDimensionList.getFirst();

			fractalDimensionPlot.setMinimumXValue(firstPoint.getX());
			fractalDimensionPlot.setMaximumXValue(firstPoint.getX()
					+ MAX_NUMBER_TO_PLOT - 1);
		}
		else
		{
			// default value when there is no data
			fractalDimensionPlot.setMaximumXValue(MAX_NUMBER_TO_PLOT - 1);
			fractalDimensionPlot.setMinimumXValue(0.0);
		}

		// set the min and max Y values on the fractalDimensionPlot
		fractalDimensionPlot.setMaximumYValue(2.0);
		fractalDimensionPlot.setMinimumYValue(0.0);

		// set the axes labels
		fractalDimensionPlot.setXAxisLabel("generation");
		fractalDimensionPlot.setYAxisLabel("est. dimension");

		// set the colors of the points that will be plotted
		fractalDimensionPlot.setPointDisplayColors(fractalDimensionColorList);

		fractalDimensionPlot.drawPoints(fractalDimensionList);
	}

	/**
	 * Takes cells on the first row and removes their count from the total.
	 */
	private void removeFirstRowCount()
	{
		// remove the count due to the first row
		for(int cellNumber = 0; cellNumber < numCols; cellNumber++)
		{
			// if the cell is occupied, then remove it from the count
			if((selectedState == ALL_NON_EMPTY_STATES_CHOICE && (cellValueList
					.get(cellNumber).value != 0))
					|| (selectedState == EMPTY_STATE_CHOICE && (cellValueList
							.get(cellNumber).value == 0))
					|| (selectedState != ALL_NON_EMPTY_STATES_CHOICE
							&& selectedState != EMPTY_STATE_CHOICE && cellValueList
							.get(cellNumber).value == selectedState))
			{
				numCellsInFractal--;
			}
		}
	}

	/**
	 * Takes cells on the last row and removes their count from the total.
	 */
	private void removeLastRowCount()
	{
		// remove the count due to the last row
		for(int cellNumber = cellValueList.size() - numCols; cellNumber < cellValueList
				.size(); cellNumber++)
		{
			// if the cell is occupied, then remove it from the count
			if((selectedState == ALL_NON_EMPTY_STATES_CHOICE && (cellValueList
					.get(cellNumber).value != 0))
					|| (selectedState == EMPTY_STATE_CHOICE && (cellValueList
							.get(cellNumber).value == 0))
					|| (selectedState != ALL_NON_EMPTY_STATES_CHOICE
							&& selectedState != EMPTY_STATE_CHOICE && cellValueList
							.get(cellNumber).value == selectedState))
			{
				numCellsInFractal--;
			}
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
	 * Set up the key parameters and graphics for the analysis.
	 */
	private void setUpTheAnalysis()
	{
		// Set all static variables to null. If this isn't done, then restarting
		// the analysis will cause problems.
		cellValueList = null;

		isOneDimensional = OneDimensionalLattice.isCurrentLatticeOneDim();

		numRows = CurrentProperties.getInstance().getNumRows();
		numCols = CurrentProperties.getInstance().getNumColumns();

		numStates = CurrentProperties.getInstance().getNumStates();

		// get the current view
		String classNameOfRule = CurrentProperties.getInstance()
				.getRuleClassName();
		rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(classNameOfRule);
		view = rule.getCompatibleCellStateView();

		// use all occupied states
		selectedState = ALL_NON_EMPTY_STATES_CHOICE;

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
			lastSelectedState = ALL_NON_EMPTY_STATES_CHOICE;
		}

		// this is the panel that will be displayed (getDisplayPanel() will
		// return the panel that this creates)
		if(displayPanel == null)
		{
			createDisplayPanel();
		}

		// select the "all non-empty states" radio button. This must happen
		// after the display panel is created.
		nonEmptyStatesButton.setSelected(true);

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

		// this is essential -- otherwise when reset, these retain their data
		// and the rest doesn't always work.
		fractalDimensionList = new LinkedList();
		fractalDimensionColorList = new LinkedList<Color>();
	}

	/**
	 * Check to see if the user drew anything on the one-dimensional lattice
	 * since the cells were last used to calculate the fractal dimension. (If
	 * so, we will have to adjust the fractal dimension by recounting that row.)
	 * 
	 * @param lattice
	 *            The CA lattice.
	 * @param generation
	 *            The current generation.
	 * @return true if the user drew on the last row.
	 */
	private boolean userDrewNewCells(Lattice lattice, int generation)
	{
		boolean hasDrawn = false;

		if(cellValueList != null)
		{
			// we will go through every cell
			Iterator cellIterator = lattice.iterator();

			for(int col = 0; col < numCols; col++)
			{
				Cell cell = (Cell) cellIterator.next();

				// have to get the cell state from the history given by
				// the row
				CellState state = cell.getState(generation - 1);

				int size = cellValueList.size();

				try
				{
					// get the integer value.
					int intCellState = ((IntegerCellState) state).getState();

					if(cellValueList.get(size - numCols + col).value != intCellState)
					{
						hasDrawn = true;
					}
				}
				catch(Exception e)
				{
					// not an IntegerCellState, so base this on whether the
					// cell is empty or occupied by some value
					if(state.isEmpty()
							&& (cellValueList.get(size - numCols + col).value != 0))
					{
						hasDrawn = true;
					}
					else if(!state.isEmpty()
							&& (cellValueList.get(size - numCols + col).value != 1))
					{
						hasDrawn = true;
					}
				}
			}
		}

		return hasDrawn;
	}

	/**
	 * Estimates the fractal dimension of the CA.
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
		if(isOneDimensional)
		{
			analyzeOneDim(lattice, rule, generation);
		}
		else
		{
			analyzeTwoDim(lattice, rule, generation);
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
		// If the user has been saving data, then close that data file when the
		// analysis is stopped.
		if(fileWriter != null)
		{
			fileWriter.close();
		}

		// Set all static variables to null. If this isn't done, then restarting
		// the analysis will cause problems.
		cellValueList = null;
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
		else if(command.equals(SELECT_STATE))
		{
			chooseAnalysisState();
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

			// replot in the new colors
			plotFractalDimensionData();
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

		// empty the fractalDimensionList (so that old data doesn't get
		// plotted again when the new simulation starts)
		fractalDimensionList.clear();
		fractalDimensionColorList.clear();

		// reset the analysis parameters
		setUpTheAnalysis();
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
	 * A convenience class for holding a cell's value.
	 * 
	 * @author David Bahr
	 */
	private class CellValue
	{
		/**
		 * The value of the cell.
		 */
		public int value = 0;

		/**
		 * Stores the value as instance variables for easy access later.
		 * 
		 * @param value
		 *            The value of the cell.
		 */
		public CellValue(int value)
		{
			this.value = value;
		}

		/**
		 * The value as a String.
		 */
		public String toString()
		{
			return "" + value;
		}
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
			stateValue = ALL_NON_EMPTY_STATES_CHOICE;
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

			// we need to recalculate the dimension -- so start by removing the
			// last data point
			if(fractalDimensionList != null && fractalDimensionList.size() > 0)
			{
				fractalDimensionList.removeLast();
				fractalDimensionColorList.removeLast();
			}

			// rerun so the changes are shown on the plot
			rerunAnalysis();
		}
	}

	/**
	 * Decides what to do when the user selects a the empty, non-empty, or
	 * particular states (to be analyzed as a fractal).
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
						&& selectedState != ALL_NON_EMPTY_STATES_CHOICE)
				{
					lastSelectedState = selectedState;
				}

				// use all occupied states
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
						&& selectedState != ALL_NON_EMPTY_STATES_CHOICE)
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
			else if(particularStateButton.isSelected())
			{
				// use the last selected state
				if(lastSelectedState != EMPTY_STATE_CHOICE
						&& lastSelectedState != ALL_NON_EMPTY_STATES_CHOICE
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

			// we need to recalculate the dimension -- so start by removing the
			// last data point
			if(fractalDimensionList != null && fractalDimensionList.size() > 0)
			{
				fractalDimensionList.removeLast();
				fractalDimensionColorList.removeLast();
			}

			// rerun so the changes are shown on the plot
			rerunAnalysis();
		}
	}
}
