/*
 CorrelationDimensionAnalysis -- a class within the Cellular Automaton Explorer. 
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
import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.IntegerCellStateView;
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
import cellularAutomata.util.math.LeastSquaresFit;

/**
 * Estimates the fractal dimension of a CA using the correlation dimension. To
 * understand this code, I recommend first understanding the code in the
 * BoxCountingDimensionAnalysis class. It is a simpler version of the roughly
 * same thing.
 * 
 * @author David Bahr
 */
public class CorrelationDimensionAnalysis extends Analysis implements
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
	private static final String ANALYSIS_NAME = "Fractal Correlation Dimension";

	// the colored title on the display
	private static final String ATTENTION_PANEL_TITLE = "Fractal Correlation";

	// title for the subpanel that lets the user select the state to be analyzed
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

	// a warning for when the calculation may take a long time due to a drawing
	// event which forces a recalculation.
	private static final String DRAWING_WARNING = "After drawing, "
			+ "the fractal dimension calculation \nmay take a long time.";

	// display info for this class
	private static final String INFO_MESSAGE = "Estimates the fractal dimension of the "
			+ "cellular automata using the correlation dimension. The dimension is "
			+ "calculated for the image as show on the screen (without consideration for "
			+ "wrap-around or other boundary conditions). This analysis is computationally "
			+ "intensive and may be slow for large lattices.  Larger lattices are more accurate.\n\n"
			+ "The first plot shows the correlation function C(r), which gives the number "
			+ "of points that are separated by a distance of r or less.  This plot "
			+ "should be linear or the estimated fractal dimension will be poor. (The "
			+ "standard deviation is calculated from the variance of the slope of a "
			+ "linear regression on C(r).) \n\n"
			+ "The second plot shows the fractal dimension at each generation. The dimension "
			+ "is calculated as the slope of the C(r) plot. In one-dimensional simulations, "
			+ "the estimated dimension may show transients until the CA fills the screen. \n\n";

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
	private static final String TOOLTIP = "<html>estimates fractal dimension "
			+ "using point correlations <br>"
			+ "(warning: time intensive calculation)</html>";

	// a warning that the fractal is too small for accurate calculations
	private static final String WARNING_MESSAGE = "The number of cells in the fractal "
			+ "is too small for an accurate calculation. Roughly 650 cells are needed "
			+ "(based on the Tsonis criterion). Try choosing a larger lattice.";

	// the list of cells (used in the one-dimensional analysis
	private static ArrayList<CellPositionAndValue> cellPositionAndValueList = null;

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

	// the maximum y-axis value for the correlation function. Used when
	// plotting.
	private double maxYValueForCorrelationFunction = 0;

	// the linear correlation coefficient for the fit to C(r) that is used to
	// calculate the fractal dimension
	private double rSquared = 0.0;

	// the standard deviation of the fractal dimension (calculated from the
	// linear fit to the C(r) function data).
	private double fractalDimensionStandardDeviation = 0.0;

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

	// the maximum distance (squared) that is possible on the lattice
	private int maxDistanceSquared = 0;

	// the state that has been selected for analysis
	private int selectedState = 0;

	// the state used in the analysis at the last generation
	private int selectedStateAtLastTimeStep = 0;

	// the total number of cells
	private int totalNumberOfCellsInCA = 0;

	// this is the number of pairs of cells that have the radius given by the
	// index of this array. Used to build the correlation function C(r).
	private static int[] radiusBin = null;

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

	// label for the linear correlation coefficient of the fit to C(r) used to
	// calculate the fractal dimension.
	private JLabel rSquaredDataLabel = null;

	// label for the standard deviation of the fractal dimension
	private JLabel standardDeviationDataLabel = null;

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

	// the list of correlation function points that will be drawn on the
	// correlationFunctionPlot
	private LinkedList correlationFunctionList = new LinkedList();

	// the list of colors for the fractal dimension points that will be drawn on
	// the fractalDimensionPlot
	private LinkedList<Color> fractalDimensionColorList = new LinkedList<Color>();

	// the list of the fractal dimension points that will be drawn on the
	// fractalDimensionPlot
	private LinkedList fractalDimensionList = new LinkedList();

	// a warning label for when the CA is too small for an accurate fractal
	// dimension calculation
	private MultilineLabel warningLabel = null;

	// the current rule
	private Rule rule = null;

	// a panel that plots the correlation function data
	private SimplePlot correlationFunctionPlot = null;

	// a panel that plots the correlation function data
	private SimplePlot fractalDimensionPlot = null;

	// an empty message used when there is no warning
	private String emptyMessage = "";

	// a delimiter for spacing data in the data file
	private String delimiter = null;

	// the data that will be saved to a file
	private String[] data = new String[4];

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
	public CorrelationDimensionAnalysis(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			setUpTheAnalysis();

			// Make an empty message of roughly the same length as the warning
			// message. This keeps the display from bouncing around.
			for(int i = 0; i < Math.round(1.5 * WARNING_MESSAGE.length()); i++)
			{
				emptyMessage += " ";
			}
		}
	}

	/**
	 * Finds the distance between pairs of the new cells and all other cells, as
	 * well as the distance between pairs of the new cells and other new cells.
	 * The result is added to the radiusBin array.
	 */
	private void addRadiiBetweenNewCellsAndOldCells()
	{
		// synchronize because the color chooser might interrupt
		synchronized(cellPositionAndValueList)
		{
			// reset so we can count the number of cells in the fractal
			numCellsInFractal = 0;

			// get the radius between the pairs of new cells and old cells (and
			// also
			// pairs of new cells with themselves)
			for(int cellNumber = 0; cellNumber < cellPositionAndValueList
					.size(); cellNumber++)
			{
				// if the cell is occupied, then get it's distance to all the
				// new cells
				if((selectedState == ALL_NON_EMPTY_STATES_CHOICE && (cellPositionAndValueList
						.get(cellNumber).value != 0))
						|| (selectedState == EMPTY_STATE_CHOICE && (cellPositionAndValueList
								.get(cellNumber).value == 0))
						|| (selectedState != ALL_NON_EMPTY_STATES_CHOICE
								&& selectedState != EMPTY_STATE_CHOICE && cellPositionAndValueList
								.get(cellNumber).value == selectedState))
				{
					// we have another "occupied" cell that is in the fractal
					numCellsInFractal++;

					// get distance to the new occupied cells. Don't recount
					// cells that we've already visited.
					int startPosition = cellPositionAndValueList.size()
							- numCols;
					if(cellNumber >= cellPositionAndValueList.size() - numCols)
					{
						startPosition = cellNumber + 1;
					}

					for(int cellNumber2 = startPosition; cellNumber2 < cellPositionAndValueList
							.size(); cellNumber2++)
					{
						// if the cell is occupied
						if((selectedState == ALL_NON_EMPTY_STATES_CHOICE && (cellPositionAndValueList
								.get(cellNumber2).value != 0))
								|| (selectedState == EMPTY_STATE_CHOICE && (cellPositionAndValueList
										.get(cellNumber2).value == 0))
								|| (selectedState != ALL_NON_EMPTY_STATES_CHOICE
										&& selectedState != EMPTY_STATE_CHOICE && cellPositionAndValueList
										.get(cellNumber2).value == selectedState))
						{
							// the row and col position of the two cells
							int row = cellPositionAndValueList.get(cellNumber).row;
							int col = cellPositionAndValueList.get(cellNumber).col;
							int row2 = cellPositionAndValueList
									.get(cellNumber2).row;
							int col2 = cellPositionAndValueList
									.get(cellNumber2).col;

							// the two cells' distance apart (squared)
							int distanceSquared = (row - row2) * (row - row2)
									+ (col - col2) * (col - col2);

							// increment the number of pairs that have
							// this distance (squared) between them
							radiusBin[distanceSquared]++;
						}
					}
				}
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

				cellPositionAndValueList.add(new CellPositionAndValue(
						rowPosition, col, intCellState.getState()));
			}
			catch(Exception e)
			{
				// not an IntegerCellState, so base this on whether the
				// cell is empty or occupied by some value
				if(cell.getState(generation).isEmpty())
				{
					// cell is not occupied, so store a 0.
					cellPositionAndValueList.add(new CellPositionAndValue(
							rowPosition, col, 0));
				}
				else
				{
					// cell is occupied, so store a 1.
					cellPositionAndValueList.add(new CellPositionAndValue(
							rowPosition, col, 1));
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
			cellPositionAndValueList = null;
			for(int i = 0; i < radiusBin.length; i++)
			{
				radiusBin[i] = 0;
			}
		}

		// the total number of cells
		totalNumberOfCellsInCA = numberOfRows * numCols;

		// revise data if the user drew on the lattice
		if(cellPositionAndValueList != null
				&& userDrewNewCells(lattice, generation))
		{
			// warn the user that this might take a long time
			if(numCellsInFractal > 3000)
			{
				JOptionPane.showMessageDialog(null, DRAWING_WARNING,
						"Time delay warning", JOptionPane.WARNING_MESSAGE);
			}

			// reset! This forces a complete recalculation
			cellPositionAndValueList = null;
			for(int i = 0; i < radiusBin.length; i++)
			{
				radiusBin[i] = 0;
			}
		}

		// get every cell value and put in an ArrayList.
		if(cellPositionAndValueList == null)
		{
			cellPositionAndValueList = new ArrayList<CellPositionAndValue>();

			// arrayList is empty so we need to fill it with whatever data is
			// available
			for(int row = 0; row < numberOfRows; row++)
			{
				addToTheListAllCellsInARowOfOneDimCA(lattice, generation, row,
						numberOfRows);
			}

			// now get the distance between *all* pairs of cells. This is stored
			// in the variable "radiusBin".
			getRadiusBetweenAllPairsOfCells();
		}
		else if(cellPositionAndValueList.size() < numRows * numCols)
		{
			// then the image hasn't filled the screen yet and we just want to
			// add the last (newest) row of cells to the
			// cellPositionAndValueList
			addToTheListAllCellsInARowOfOneDimCA(lattice, generation,
					numberOfRows - 1, numberOfRows);

			// now take the previous list of distances, and add the distance
			// between the new cells and all the other cells.
			addRadiiBetweenNewCellsAndOldCells();
		}
		else
		{
			// the image already fills the screen. We will soon get rid of the
			// first row. So remove the distances between the first row and the
			// other rows.
			removeDistanceBetweenCellsInFirstRowAndOtherRows();

			// Now remove the first row (it has disappeared off the top of the
			// screen)
			for(int col = 0; col < numCols; col++)
			{
				cellPositionAndValueList.remove(0);
			}

			// and add the new (last) row
			addToTheListAllCellsInARowOfOneDimCA(lattice, generation,
					numberOfRows - 1, numberOfRows);

			// finally, take the previous list of distances, and add the
			// distance between the new cells and all the other cells.
			addRadiiBetweenNewCellsAndOldCells();
		}

		// now calculate the correlation function C(r) from the binned radii
		calculateCorrelationFunction();

		// now calculate the fractal dimension from a linear regression of the
		// function C(r)
		calculateFractalDimension(generation);

		// now plot and save the fractal data
		plotAndSaveFractalAndCorrelationData(generation);

		// if necessary, display warning that CA is too small.
		displayWarning();

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

		// In two dimensions, this must be recreated every time step.
		// Gives bins of distances (radii) between two points. A bin will be
		// incremented when distance between two points is less than the radius
		// (given as the index of the array).
		radiusBin = new int[maxDistanceSquared + 1];
		for(int radius = 0; radius < radiusBin.length; radius++)
		{
			radiusBin[radius] = 0;
		}

		// create a new arrayList (must be recreated every time step
		cellPositionAndValueList = new ArrayList<CellPositionAndValue>();

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

					cellPositionAndValueList.add(new CellPositionAndValue(row,
							col, intCellState.getState()));
				}
				catch(Exception e)
				{
					// not an IntegerCellState, so base this on whether the cell
					// is empty or occupied by some value
					if(cell.getState(generation).isEmpty())
					{
						// cell is not occupied, so store a 0
						cellPositionAndValueList.add(new CellPositionAndValue(
								row, col, 0));
					}
					else
					{
						// cell is occupied, so store a 1
						cellPositionAndValueList.add(new CellPositionAndValue(
								row, col, 1));
					}
				}
			}
		}

		// get the radius between each pair of cells
		getRadiusBetweenAllPairsOfCells();

		// now calculate the correlation function from the binned radii
		calculateCorrelationFunction();

		// now calculate the fractal dimension from a linear regression of the
		// function C(r)
		calculateFractalDimension(generation);

		// now plot and save the fractal data
		plotAndSaveFractalAndCorrelationData(generation);

		// if necessary, display warning that CA is too small.
		displayWarning();
	}

	/**
	 * Uses the radiusBin to calculate the correlation function. The radiusBin
	 * holds the distance between each pair of points, arranged into bins. This
	 * finds C(r) by calculating the number of cells that are less than that
	 * radius. The result is stored in correlationFunctionList as log(r) and
	 * log(C(r).
	 */
	private void calculateCorrelationFunction()
	{
		// now calculate the correlation function C(r). Start at 2 because want
		// to count the number of pairs of points with distance *less* than the
		// radius. First, reset the correlationFunctionList so doesn't hold any
		// old data.
		correlationFunctionList = new LinkedList();
		for(int radius = 2; radius <= maxDistanceSquared; radius *= 2)
		{
			// divide by 2 because we really stored the radius^2, and log(r^2) =
			// 2 log(r)
			double logRadius = Math.log(radius) / 2.0;

			int numPairsOfPointsWithDistanceLessThanRadius = 0;
			for(int distance = 1; distance < radius; distance++)
			{
				numPairsOfPointsWithDistanceLessThanRadius += radiusBin[distance];
			}

			// don't take the log of 0
			if(numPairsOfPointsWithDistanceLessThanRadius > 0)
			{
				// convert to a double and take the log -- that's the log of the
				// correlation function, C(radius)
				double logCorrelationFunction = Math
						.log(numPairsOfPointsWithDistanceLessThanRadius);

				// used when plotting
				if(logCorrelationFunction > maxYValueForCorrelationFunction)
				{
					maxYValueForCorrelationFunction = Math
							.ceil(logCorrelationFunction);
				}

				// save correlation function in a linked list for plotting
				correlationFunctionList.add(new Point2D.Double(logRadius,
						logCorrelationFunction));
			}
		}

		// now get rid of the largest points -- finite size effects skew the
		// result. This removes 25% of the points.
		double percent = 0.25;
		int thirtyPercentOfSize = (int) (correlationFunctionList.size() * percent);
		for(int i = 0; i < thirtyPercentOfSize; i++)
		{
			// don't let it get too small or the plotting will fail
			if(correlationFunctionList.size() > 3)
			{
				correlationFunctionList.removeLast();
			}
		}
	}

	/**
	 * Calculates the fractal dimension from the slope of the linear regression
	 * of the correlation function.
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
			fractalDimensionStandardDeviation = 0.0;
			rSquared = 1.0;
		}
		else if((numCellsInFractal == 0) || (numCellsInFractal == 1))
		{
			// then zero-dim
			fractalDimension = 0.0;
			fractalDimensionStandardDeviation = 0.0;
			rSquared = 1.0;
		}
		else
		{
			// not zero- or two-dim, so use a linear regression to estimate the
			// fractal dimension
			LeastSquaresFit linearFit = new LeastSquaresFit();
			linearFit.fit(correlationFunctionList);
			fractalDimension = linearFit.getSlope();
			fractalDimensionStandardDeviation = linearFit
					.getStandardDeviationSlope();
			rSquared = linearFit.getRSquared();
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

			if(IntegerCellStateView.isCurrentRuleCompatible()
					&& IntegerCellState.isCurrentRuleCompatible())
			{
				CellStateView view = Cell.getView();
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
			rSquaredDataLabel = new JLabel("");
			standardDeviationDataLabel = new JLabel("");

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
		// JLabel rSquaredLabel = new JLabel("R squared: ");
		JLabel standardDeviationLabel = new JLabel("Standard deviation:   ");
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
		// boxOfNameLabels.add(rSquaredLabel);
		// boxOfNameLabels.add(Box.createVerticalStrut(verticalSpace));
		boxOfNameLabels.add(standardDeviationLabel);
		boxOfNameLabels.add(Box.createVerticalStrut(verticalSpace));
		boxOfNameLabels.add(fractalDimensionLabel);

		// add the data labels to the second vertical box
		boxOfDataLabels.add(generationDataLabel);
		boxOfDataLabels.add(Box.createVerticalStrut(verticalSpace));
		boxOfDataLabels.add(numPointsInFractalDataLabel);
		boxOfDataLabels.add(Box.createVerticalStrut(verticalSpace));
		// boxOfDataLabels.add(rSquaredDataLabel);
		// boxOfDataLabels.add(Box.createVerticalStrut(verticalSpace));
		boxOfDataLabels.add(standardDeviationDataLabel);
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
			displayPanel = new JPanel(new BorderLayout());
			displayPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			displayPanel.setPreferredSize(new Dimension(
					CAFrame.tabbedPaneDimension.width, 1100));

			// create a panel that displays messages
			JPanel messagePanel = createMessagePanel();

			// create the panel that holds the fractal statistics
			JPanel dataPanel = createDataPanel();

			// create a panel that holds the select state radio buttons
			JPanel stateSelectionPanel = createStateRadioButtonPanel();

			// create a warning panel
			warningLabel = createWarningMessage();
			warningLabel.setText(emptyMessage);

			// create a "save data" check box
			saveDataCheckBox = new JCheckBox(SAVE_DATA);
			saveDataCheckBox.setToolTipText(SAVE_DATA_TOOLTIP);
			saveDataCheckBox.setActionCommand(SAVE_DATA);
			saveDataCheckBox.addActionListener(this);
			JPanel saveDataPanel = new JPanel();
			saveDataPanel
					.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			saveDataPanel.add(saveDataCheckBox);

			// get the data plots
			JPanel plotPanel = createPlotPanel();

			// add everything to the display
			displayPanel.setLayout(new GridBagLayout());
			int row = 0;
			displayPanel.add(messagePanel, new GBC(0, row).setSpan(1, 1)
					.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
					.setInsets(1));
			row++;
			displayPanel.add(plotPanel, new GBC(0, row).setSpan(1, 1).setFill(
					GBC.BOTH).setWeight(2.0, 2.0).setAnchor(GBC.WEST)
					.setInsets(1));
			row++;
			displayPanel.add(dataPanel, new GBC(0, row).setSpan(1, 1).setFill(
					GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
					.setInsets(1));
			row++;
			displayPanel.add(stateSelectionPanel, new GBC(0, row).setSpan(1, 1)
					.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
					.setInsets(1));
			row++;
			displayPanel.add(warningLabel, new GBC(0, row).setSpan(1, 1)
					.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
					.setInsets(1));
			row++;
			displayPanel.add(saveDataPanel, new GBC(0, row).setSpan(1, 1)
					.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
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
					"Fractal dimension: ", "Standard deviation:"};
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
		messagePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
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
		// create a panel that plots the correlation function data
		correlationFunctionPlot = new SimplePlot();

		// create a panel that plots the fractal dimension at each time step
		fractalDimensionPlot = new SimplePlot();

		// put the above in a single panel
		JPanel plotPanel = new JPanel(new GridBagLayout());
		plotPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 3, 3));

		// correlation function plot
		int row = 0;
		plotPanel.add(correlationFunctionPlot, new GBC(0, row).setSpan(10, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0, 1, 1, 1));

		// fractal dimension plot
		row = 2;
		plotPanel.add(fractalDimensionPlot, new GBC(0, row).setSpan(10, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		return plotPanel;
	}

	/**
	 * Creates a panel that displays a warning message about the size of the
	 * fractal being too small.
	 * 
	 * @return A panel containing a warning message.
	 */
	private MultilineLabel createWarningMessage()
	{
		MultilineLabel messageLabel = new MultilineLabel(WARNING_MESSAGE);
		messageLabel.setFont(fonts.getItalicSmallerFont());
		messageLabel.setForeground(Color.RED);
		messageLabel.setMargin(new Insets(6, 10, 2, 16));
		messageLabel.setColumns(40);

		return messageLabel;
	}

	/**
	 * If the CA is too small, display a warning.
	 */
	private void displayWarning()
	{
		// Tsonis criterion for enough points to get valid correlation
		// dimension. I assume the dimension is 2.0 which gives an upper bound.
		// (int) Math.pow(10.0, 2.0 + 0.4 * fractalDimension);
		int tsonis = 630;

		if(numCellsInFractal < tsonis)
		{
			// create a warning panel
			warningLabel.setText(WARNING_MESSAGE);
		}
		else
		{
			warningLabel.setText(emptyMessage);
		}
	}

	/**
	 * Gets the radii between all pairs of cells held in the
	 * cellPositionAndValueList.
	 */
	private void getRadiusBetweenAllPairsOfCells()
	{
		// synchronize because the color chooser might interrupt
		synchronized(cellPositionAndValueList)
		{
			// reset so we can recount
			numCellsInFractal = 0;

			// get the radius between each pair of cells
			for(int cellNumber = 0; cellNumber < cellPositionAndValueList
					.size(); cellNumber++)
			{
				// if the cell is occupied, then get it's distance to all the
				// other cells
				if((selectedState == ALL_NON_EMPTY_STATES_CHOICE && (cellPositionAndValueList
						.get(cellNumber).value != 0))
						|| (selectedState == EMPTY_STATE_CHOICE && (cellPositionAndValueList
								.get(cellNumber).value == 0))
						|| (selectedState != ALL_NON_EMPTY_STATES_CHOICE
								&& selectedState != EMPTY_STATE_CHOICE && cellPositionAndValueList
								.get(cellNumber).value == selectedState))
				{
					// another "occupied" cell, so add it to the total
					numCellsInFractal++;

					// get distance to every other occupied cell. Don't recount
					// cells that we've already visited.
					for(int cellNumber2 = cellNumber + 1; cellNumber2 < cellPositionAndValueList
							.size(); cellNumber2++)
					{
						// if the cell is occupied
						if((selectedState == ALL_NON_EMPTY_STATES_CHOICE && (cellPositionAndValueList
								.get(cellNumber2).value != 0))
								|| (selectedState == EMPTY_STATE_CHOICE && (cellPositionAndValueList
										.get(cellNumber2).value == 0))
								|| (selectedState != ALL_NON_EMPTY_STATES_CHOICE
										&& selectedState != EMPTY_STATE_CHOICE && cellPositionAndValueList
										.get(cellNumber2).value == selectedState))
						{
							// the row and col position of the two cells
							int row = cellPositionAndValueList.get(cellNumber).row;
							int col = cellPositionAndValueList.get(cellNumber).col;
							int row2 = cellPositionAndValueList
									.get(cellNumber2).row;
							int col2 = cellPositionAndValueList
									.get(cellNumber2).col;

							// the two cells' distance apart (squared)
							int distanceSquared = (row - row2) * (row - row2)
									+ (col - col2) * (col - col2);

							// increment the number of pairs that have
							// this distance (squared) between them
							radiusBin[distanceSquared]++;
						}
					}
				}
			}
		}
	}

	/**
	 * Plots and saves all of the correlation and fractal dimension data.
	 * 
	 * @param generation
	 *            The current generation.
	 */
	private void plotAndSaveFractalAndCorrelationData(int generation)
	{
		// set the text for the labels
		generationDataLabel.setText("" + generation);
		numPointsInFractalDataLabel.setText("" + numCellsInFractal);

		// and set the text for the fractalDimension label, but format!
		DecimalFormat myFormatter = new DecimalFormat(DECIMAL_PATTERN);
		String fractalOutput = myFormatter.format(fractalDimension);
		fractalDimensionDataLabel.setText(fractalOutput);
		String rSquaredOutput = myFormatter.format(rSquared);
		rSquaredDataLabel.setText(rSquaredOutput);
		String standardDeviationOutput = myFormatter
				.format(fractalDimensionStandardDeviation);
		standardDeviationDataLabel.setText(standardDeviationOutput);

		// create an array of data to be saved
		data[0] = "" + generation;
		data[1] = "" + numCellsInFractal;
		data[2] = "" + fractalDimension;
		data[3] = "" + fractalDimensionStandardDeviation;

		// see if user wants to save the data
		if(fileWriter != null)
		{
			// save it
			saveData(data);
		}

		// correlationFunctionPlot the correlation function data
		plotCorrelationFunctionData();

		// correlationFunctionPlot the fractal dimension data
		plotFractalDimensionData();
	}

	/**
	 * Plots the correlation function data.
	 */
	private void plotCorrelationFunctionData()
	{
		if(correlationFunctionList.size() > 0)
		{
			// set the min and max X values on the correlationFunctionPlot
			Point2D lastPoint = (Point2D) correlationFunctionList.getLast();
			correlationFunctionPlot.setMaximumXValue(lastPoint.getX());
		}
		else
		{
			// clear the plot if there is no data
			correlationFunctionPlot.clearPlot();

			// default value when there is no data
			correlationFunctionPlot.setMaximumXValue(1.0);
		}
		correlationFunctionPlot.setMinimumXValue(0.0);

		// set the min and max Y values on the correlationFunctionPlot
		correlationFunctionPlot
				.setMaximumYValue(maxYValueForCorrelationFunction);
		correlationFunctionPlot.setMinimumYValue(0.0);

		// set axes labels
		correlationFunctionPlot.setXAxisLabel("log(r)");
		correlationFunctionPlot.setYAxisLabel("log(C(r))");

		// specify colors for the points
		if(correlationFunctionList.size() > 0)
		{
			Color stateColor = Color.BLACK;
			if(selectedState != ALL_NON_EMPTY_STATES_CHOICE)
			{
				int stateWeArePlotting = selectedState;
				if(selectedState == EMPTY_STATE_CHOICE)
				{
					stateWeArePlotting = 0;
				}

				if(IntegerCellStateView.isCurrentRuleCompatible()
						&& IntegerCellState.isCurrentRuleCompatible())
				{
					CellStateView view = Cell.getView();
					stateColor = view.getDisplayColor(new IntegerCellState(
							stateWeArePlotting), null, new Coordinate(0, 0));
				}
			}
			Color[] colorArray = new Color[correlationFunctionList.size()];
			for(int point = 0; point < colorArray.length; point++)
			{
				colorArray[point] = stateColor;
			}
			correlationFunctionPlot.setPointDisplayColors(colorArray);
		}
		else
		{
			correlationFunctionPlot.setPointDisplayColorsToDefault();
		}

		correlationFunctionPlot.drawPoints(correlationFunctionList);
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

		fractalDimensionPlot.setXAxisLabel("generation");
		fractalDimensionPlot.setYAxisLabel("est. dimension");

		// set the colors of the points that will be plotted
		fractalDimensionPlot.setPointDisplayColors(fractalDimensionColorList);

		fractalDimensionPlot.drawPoints(fractalDimensionList);
	}

	/**
	 * Takes cells on the first row and removes their distance to each of the
	 * other occupied cells. In other words, those distances are removed from
	 * the radiusBin array.
	 */
	private void removeDistanceBetweenCellsInFirstRowAndOtherRows()
	{
		// get the radius between the pairs of first row cells and all other
		// cells (and also pairs of first row cells with themselves)
		for(int cellNumber = 0; cellNumber < numCols; cellNumber++)
		{
			// if the cell is occupied, then get it's distance to all the
			// other cells
			if((cellPositionAndValueList.get(cellNumber).value == selectedState)
					|| (selectedState == ALL_NON_EMPTY_STATES_CHOICE && (cellPositionAndValueList
							.get(cellNumber).value != 0))
					|| (selectedState == EMPTY_STATE_CHOICE && (cellPositionAndValueList
							.get(cellNumber).value == 0)))
			{
				// get distance to the new occupied cells. Don't recount
				// cells that we've already visited.
				for(int cellNumber2 = cellNumber + 1; cellNumber2 < cellPositionAndValueList
						.size(); cellNumber2++)
				{
					// if the cell is occupied
					if((cellPositionAndValueList.get(cellNumber2).value == selectedState)
							|| (selectedState == ALL_NON_EMPTY_STATES_CHOICE && (cellPositionAndValueList
									.get(cellNumber2).value != 0))
							|| (selectedState == EMPTY_STATE_CHOICE && (cellPositionAndValueList
									.get(cellNumber2).value == 0)))
					{
						// the row and col position of the two cells
						int row = cellPositionAndValueList.get(cellNumber).row;
						int col = cellPositionAndValueList.get(cellNumber).col;
						int row2 = cellPositionAndValueList.get(cellNumber2).row;
						int col2 = cellPositionAndValueList.get(cellNumber2).col;

						// the two cells' distance apart (squared)
						int distanceSquared = (row - row2) * (row - row2)
								+ (col - col2) * (col - col2);

						// decrement the number of pairs that have
						// this distance (squared) between them
						radiusBin[distanceSquared]--;
					}
				}
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
		radiusBin = null;
		cellPositionAndValueList = null;

		isOneDimensional = OneDimensionalLattice.isCurrentLatticeOneDim();

		numRows = CurrentProperties.getInstance().getNumRows();
		numCols = CurrentProperties.getInstance().getNumColumns();

		numStates = CurrentProperties.getInstance().getNumStates();

		// the maximum distance (squared) between two points on the lattice
		maxDistanceSquared = (numRows - 1) * (numRows - 1) + (numCols - 1)
				* (numCols - 1);

		// in two dimensions this must be done at every time step, so don't
		// bother if 2-d (in 2-d it will be done where needed).
		if(isOneDimensional)
		{
			// bins of distances (radii) between two points. A bin will be
			// incremented when distance between two points is less than the
			// radius (given as the index of the array).
			// Have to add one because distances can be between 0 and
			// maxDistanceSquared inclusive.
			radiusBin = new int[maxDistanceSquared + 1];
			for(int radius = 0; radius < radiusBin.length; radius++)
			{
				radiusBin[radius] = 0;
			}
		}

		// get the current view
		String ruleClassName = CurrentProperties.getInstance()
				.getRuleClassName();
		rule = ReflectionTool.instantiateFullRuleFromClassName(ruleClassName);
		view = Cell.getView();

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
		correlationFunctionList = new LinkedList();
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

		if(cellPositionAndValueList != null)
		{
			// we will go through every cell
			Iterator cellIterator = lattice.iterator();

			for(int col = 0; col < numCols; col++)
			{
				Cell cell = (Cell) cellIterator.next();

				// have to get the cell state from the history given by
				// the row
				CellState state = cell.getState(generation - 1);

				int size = cellPositionAndValueList.size();

				try
				{
					// get the integer value.
					int intCellState = ((IntegerCellState) state).getState();

					if(cellPositionAndValueList.get(size - numCols + col).value != intCellState)
					{
						hasDrawn = true;
					}
				}
				catch(Exception e)
				{
					// not an IntegerCellState, so base this on whether the
					// cell is empty or occupied by some value
					if(state.isEmpty()
							&& (cellPositionAndValueList.get(size - numCols
									+ col).value != 0))
					{
						hasDrawn = true;
					}
					else if(!state.isEmpty()
							&& (cellPositionAndValueList.get(size - numCols
									+ col).value != 1))
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
		radiusBin = null;
		cellPositionAndValueList = null;
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
			plotCorrelationFunctionData();
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

		// empty the correlationFunctionPlot lists (so that old data doesn't get
		// plotted again when
		// the new simulation starts)
		correlationFunctionList.clear();
		fractalDimensionList.clear();
		fractalDimensionColorList.clear();

		// reset the correlationFunctionPlot
		correlationFunctionPlot.clearPlot();

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
	 * A convenience class for holding a cell's value and row and col positions.
	 * 
	 * @author David Bahr
	 */
	private class CellPositionAndValue
	{
		/**
		 * The row position of the cell.
		 */
		public int row = 0;

		/**
		 * The col position of the cell.
		 */
		public int col = 0;

		/**
		 * The value of the cell.
		 */
		public int value = 0;

		/**
		 * Stores the row, col and value as instance variables for easy access
		 * later.
		 * 
		 * @param row
		 *            The row position of the cell.
		 * @param col
		 *            The col position of the cell.
		 * @param value
		 *            The value of the cell.
		 */
		public CellPositionAndValue(int row, int col, int value)
		{
			this.row = row;
			this.col = col;
			this.value = value;
		}
	}

	/**
	 * A patch of color displayed on the JPanel. Has a mouse listener to detect
	 * selection events.
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
	 * Decides what to do when the user selects the empty, non-empty, or
	 * particular states (to be analyzed as a fractal)..
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
