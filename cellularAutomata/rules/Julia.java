/*
 Julia -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.rules;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Random;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.TriangleHexagonCellStateView;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.rules.templates.ComplexRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;
import cellularAutomata.util.math.Complex;
import cellularAutomata.util.math.RandomSingleton;

/*
 * NOTE: Why are so many variables static? Because they are for the properties
 * panel which needs to be the same for all instances of the Julia class.
 */

/**
 * A rule which uses a complex number for each cell. The complex number is c in
 * the Julia set equation z = z^2 + c. The rule averages the values of c from
 * the neighboring cells. The value z is a position in the complex plane (the
 * center of the plane is specified in the more properties panel).
 * 
 * @author David Bahr
 */
public class Julia extends ComplexRuleTemplate
{
	// default imaginary value for the Julia Set constant
	private static final double DEFAULT_JULIA_IMAGINARY_VALUE = -0.2321; // -0.123;

	// default real value for the Julia Set constant
	private static final double DEFAULT_JULIA_REAL_VALUE = -0.835; // -0.745;

	// default imaginary value for the complex plane initial state
	private static final double DEFAULT_PLANE_IMAGINARY_VALUE = -0.08;

	// default real value for the complex plane initial state
	private static final double DEFAULT_PLANE_REAL_VALUE = 0.8;

	// the default width of the displayed complex plane
	private static final double DEFAULT_WIDTH = 1.0; // 0.75;

	// default increment value that changes the Julia Set constant c at each
	// time step
	private static final double DEFAULT_INCREMENT_VALUE = 0.001; // 0.0001;

	// default imaginary value for the Julia Set constant
	private static double MAX_JULIA_IMAGINARY_VALUE = DEFAULT_JULIA_IMAGINARY_VALUE + 1.0;

	// default real value for the Julia Set constant
	private static double MAX_JULIA_REAL_VALUE = DEFAULT_JULIA_REAL_VALUE + 1.0;

	// default imaginary value for the Julia Set constant
	private static double MIN_JULIA_IMAGINARY_VALUE = DEFAULT_JULIA_IMAGINARY_VALUE - 1.0;

	// default real value for the Julia Set constant
	private static double MIN_JULIA_REAL_VALUE = DEFAULT_JULIA_REAL_VALUE - 1.0;

	// label for the close button
	private static final String CLOSE = "Close";

	// tooltip for setting the imaginary value of the central position on the
	// complex plane
	private static final String POSITION_IMAGINARY_VALUE_TIP = "Sets the imaginary value "
			+ "for the position of the center of the display.";

	// tooltip for setting the real value of the central position on the complex
	// plane
	private static final String POSITION_REAL_VALUE_TIP = "Sets the real value "
			+ "for the position of the center of the display.";

	// a display name for this class
	private static final String RULE_NAME = "Chinese Dragon (Julia Tornado)";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, use as large a lattice as possible without "
			+ "crashing your computer.  Try 150 by 150 or larger.  Also use large "
			+ "neighborhoods such as \"Moore with radius 5\".  Select a random "
			+ "initial state with a very large percentile such as 100%."
			+ leftClickInstructions + rightClickInstructions + "</body></html>";

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b> A phantasmagorical fractal display that comes from a surprisingly boring CA."
			+ "</body></html>";

	// a tooltip description for the width spinner
	private static final String WIDTH_TIP = "<html> Select the width of the display on "
			+ "the complex plane (between 0.001 and 4.001).<br>"
			+ "Smaller widths zoom the display and show more detail.</html>";

	// The width of the complex plane displayed as an initial state
	private static volatile double widthOfDisplay = DEFAULT_WIDTH;

	// the current generation being processed by the rule
	private static volatile int currentGeneration = -1;

	// the complex value of the cell at the upper left of the grid when the
	// "Complex plane" option is selected
	private static volatile Complex upperLeftCorner = new Complex(
			DEFAULT_PLANE_REAL_VALUE - widthOfDisplay / 2.0,
			DEFAULT_PLANE_IMAGINARY_VALUE - widthOfDisplay / 2.0);

	// The label for editing the imaginary part of the position on the complex
	// plane
	private static JLabel imaginaryPositionLabel = null;

	// The label for editing the real part of the position on the complex plane
	private static JLabel realPositionLabel = null;

	// The label for editing the width of the position on the complex plane
	private static JLabel widthLabel = null;

	// the JPanel that is returned by getAdditionalPropertiesPanel()
	private static JPanel panel = null;

	// selects the imaginary part of the position in the complex plane
	private static JSpinner imaginaryPositionSpinner = null;

	// selects the real part of the position in the complex plane
	private static JSpinner realPositionSpinner = null;

	// selects the width of the display of the complex plane
	private static JSpinner widthSpinner = null;

	// fonts for display
	private Fonts fonts = null;

	/**
	 * Create a rule that displays Julia sets by evolving the Julia constant c.
	 * <p>
	 * When calling the parent constructor, the minimalOrLazyInitialization
	 * parameter must be included as
	 * <code>super(minimalOrLazyInitialization);</code>. The boolean is
	 * intended to indicate when the constructor should build a rule with as
	 * small a footprint as possible. In order to load rules by reflection, the
	 * application must query this class for information like the display name,
	 * tooltip description, etc. At these times it makes no sense to build the
	 * complete rule which may have a large footprint in memory.
	 * <p>
	 * It is recommended that the constructor and instance variables do not
	 * initialize any memory intensive variables and that variables be
	 * initialized only when first needed (lazy initialization). Or all
	 * initializations in the constructor may be placed in an <code>if</code>
	 * statement.
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
	 *            small a footprint as possible. When false, the rule is fully
	 *            constructed. This variable should be passed to the super
	 *            constructor <code>super(minimalOrLazyInitialization);</code>,
	 *            but if uncertain, you may safely ignore this variable.
	 */
	public Julia(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			fonts = new Fonts();
		}
		// set the constant c to a random default value
		// double centerDeviation = 0.0; //0.001;
		// Random r = new Random();
		// double range = 1.0;
		// MAX_JULIA_IMAGINARY_VALUE = DEFAULT_JULIA_IMAGINARY_VALUE
		// + (centerDeviation * r.nextDouble() - centerDeviation / 2.0)
		// + range;
		// MAX_JULIA_REAL_VALUE = DEFAULT_JULIA_REAL_VALUE
		// + (centerDeviation * r.nextDouble() - centerDeviation / 2.0)
		// + range;
		// MIN_JULIA_IMAGINARY_VALUE = DEFAULT_JULIA_IMAGINARY_VALUE
		// + (centerDeviation * r.nextDouble() - centerDeviation / 2.0)
		// - range;
		// MIN_JULIA_REAL_VALUE = DEFAULT_JULIA_REAL_VALUE
		// + (centerDeviation * r.nextDouble() - centerDeviation / 2.0)
		// - range;
	}

	/**
	 * Reacts to any actions on the JPanel GUI created in
	 * getAdditionalPropertiesPanel().
	 */
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(CLOSE))
		{
			// close dialog box
			JDialog dialog = (JDialog) panel.getTopLevelAncestor();
			dialog.dispose();
		}
	}

	/**
	 * Creates a panel that displays a message about the More Properties panel.
	 * 
	 * @return A panel containing messages.
	 */
	private JPanel createDescriptionPanel()
	{
		// a "grab their attention" panel
		AttentionPanel attentionPanel = new AttentionPanel("Complex Plane");

		String functionDescription = "These controls let you scroll around "
				+ "the complex plane.  In other words, you can select your "
				+ "location on the real axis (horizontal) and imaginary axis "
				+ "(vertical).  (The selected location is in the center of the display "
				+ "area.) You can also zoom in and out by setting the width of "
				+ "the displayed area.\n\n"
				+ "Place your cursor in the control box and use the up and "
				+ "down arrows.  This lets you pan and zoom in and out nicely.\n\n"
				+ "Any changes will show up during the next generation.";

		MultilineLabel messageLabel = new MultilineLabel(functionDescription);
		messageLabel.setFont(fonts.getMorePropertiesDescriptionFont());
		messageLabel.setMargin(new Insets(2, 6, 2, 2));

		JPanel messagePanel = new JPanel(new GridBagLayout());
		int row = 0;
		messagePanel.add(attentionPanel, new GBC(0, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(0));
		row++;
		messagePanel.add(messageLabel, new GBC(0, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(0,
				0, 1, 0));

		return messagePanel;
	}

	/**
	 * Creates text fields for setting the real and imaginary components of the
	 * position.
	 * 
	 * @return A panel holding the spinners.
	 */
	private JPanel createAdditionalPropertiesPanel()
	{
		// add a label for the width
		widthLabel = new JLabel("Width: ");
		widthLabel.setFont(fonts.getBoldFont());

		// add a label for the real part
		realPositionLabel = new JLabel("Real axis: ");
		realPositionLabel.setFont(fonts.getBoldFont());

		// add a label for the imaginary part
		imaginaryPositionLabel = new JLabel("Imaginary axis: ");
		imaginaryPositionLabel.setFont(fonts.getBoldFont());

		// create a spinner for the width
		SpinnerNumberModel widthModel = new SpinnerNumberModel(DEFAULT_WIDTH,
				0.001, Math.round(getFullState().modulus()), 0.01);
		widthSpinner = new JSpinner(widthModel);
		widthSpinner.setToolTipText(WIDTH_TIP);
		widthSpinner.setFont(fonts.getPlainFont());
		((JSpinner.DefaultEditor) widthSpinner.getEditor()).getTextField()
				.setColumns(4);

		// create a spinner for the real value
		SpinnerNumberModel realModel = new SpinnerNumberModel(
				DEFAULT_PLANE_REAL_VALUE, -5.001, 5.001, 0.1);
		realPositionSpinner = new JSpinner(realModel);
		realPositionSpinner.setToolTipText(POSITION_REAL_VALUE_TIP);
		realPositionSpinner.setFont(fonts.getPlainFont());
		((JSpinner.DefaultEditor) realPositionSpinner.getEditor())
				.getTextField().setColumns(4);

		// create a spinner for the imaginary value
		SpinnerNumberModel imaginaryModel = new SpinnerNumberModel(
				DEFAULT_PLANE_IMAGINARY_VALUE, -5.001, 5.001, 0.1);
		imaginaryPositionSpinner = new JSpinner(imaginaryModel);
		imaginaryPositionSpinner.setToolTipText(POSITION_IMAGINARY_VALUE_TIP);
		imaginaryPositionSpinner.setFont(fonts.getPlainFont());
		((JSpinner.DefaultEditor) imaginaryPositionSpinner.getEditor())
				.getTextField().setColumns(4);

		// create combo panel
		JPanel spinnerPanel = new JPanel(new GridBagLayout());
		int row1 = 0;
		spinnerPanel.add(new JLabel(" "), new GBC(1, row1).setSpan(1, 1)
				.setFill(GBC.HORIZONTAL).setWeight(10.0, 1.0).setAnchor(
						GBC.WEST).setInsets(0));
		spinnerPanel.add(realPositionLabel, new GBC(2, row1).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0));
		spinnerPanel.add(realPositionSpinner, new GBC(3, row1).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0));
		spinnerPanel.add(new JLabel(" "), new GBC(4, row1).setSpan(1, 1)
				.setFill(GBC.HORIZONTAL).setWeight(10.0, 1.0).setAnchor(
						GBC.WEST).setInsets(0));
		row1++;
		spinnerPanel.add(imaginaryPositionLabel, new GBC(2, row1).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0));
		spinnerPanel.add(imaginaryPositionSpinner, new GBC(3, row1).setSpan(1,
				1).setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0));
		row1++;
		spinnerPanel.add(widthLabel, new GBC(2, row1).setSpan(1, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(0));
		spinnerPanel.add(widthSpinner, new GBC(3, row1).setSpan(1, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(0));

		// add description
		JPanel descriptionPanel = createDescriptionPanel();

		// add the components to a JPanel
		JPanel constantPanel = new JPanel(new GridBagLayout());
		int row = 0;
		constantPanel.add(descriptionPanel, new GBC(0, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0));
		row++;
		constantPanel.add(spinnerPanel, new GBC(0, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(0));
		row++;
		constantPanel.add(new JLabel(" "), new GBC(0, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(10.0, 10.0).setAnchor(GBC.WEST)
				.setInsets(0));

		return constantPanel;
	}

	/**
	 * Gets a complex value for the cell at position (row, col) when the cells
	 * are arranged as on a normal complex plane.
	 * 
	 * @param row
	 *            The cell's row.
	 * @param col
	 *            The cell's row.
	 * @param increment
	 *            The incremental value between adjacent cells.
	 * @return The complex value associated with the cell at position (row,
	 *         col).
	 */
	private Complex getCellValueForComplexPlane(int row, int col,
			double increment)
	{
		// the complex value for the cell.
		Complex cellValue = new Complex(0.0, 0.0);

		cellValue.real = col * increment + upperLeftCorner.real;
		cellValue.imaginary = row * increment + upperLeftCorner.imaginary;

		return cellValue;
	}

	/**
	 * Gets a JPanel that may request specific input information that the rule
	 * needs to operate correctly. Should be overridden by child classes that
	 * desire to input any specific information. <br>
	 * Note that if returns null, then the panel is not displayed by the current
	 * version of the CA ControlPanel class. This null behavior is the default.
	 * 
	 * @return A JPanel requesting specific input information that the rule
	 *         needs to operate correctly. May be null.
	 */
	public JPanel getAdditionalPropertiesPanel()
	{
		// only recreate the panel if necessary
		if(panel == null)
		{
			panel = createAdditionalPropertiesPanel();
		}

		return panel;
	}

	/**
	 * Gets a complex number that represents the alternate state (a state that
	 * is drawn with a right mouse click). In this case, the alternate state is
	 * a random complex number between the full and -full values.
	 * 
	 * @return The alternate state.
	 */
	public Complex getAlternateState()
	{
		Random r = RandomSingleton.getInstance();

		double real = r.nextDouble()
				* (MAX_JULIA_REAL_VALUE - MIN_JULIA_REAL_VALUE)
				+ MIN_JULIA_REAL_VALUE;
		double imaginary = r.nextDouble()
				* (MAX_JULIA_IMAGINARY_VALUE - MIN_JULIA_IMAGINARY_VALUE)
				+ MIN_JULIA_IMAGINARY_VALUE;

		Complex c = new Complex(real, imaginary);

		return c;
	}

	/**
	 * A brief description (written in HTML) that describes what parameters will
	 * give best results for this rule (which lattice, how many states, etc).
	 * The description will be displayed on the properties panel. Using html
	 * permits line breaks, font colors, etcetera, as described in HTML
	 * resources. Regular line breaks will not work.
	 * <p>
	 * Recommend starting with the title of the rule followed by "For best
	 * results, ...". See Rule 102 for an example.
	 * 
	 * @return An HTML string describing how to get best results from this rule.
	 *         May be null.
	 */
	public String getBestResultsDescription()
	{
		return BEST_RESULTS;
	}

	/**
	 * Gets an instance of the CellStateView class that will be used to display
	 * cells being updated by this rule. Note: This method must return a view
	 * that is able to display cell states of the type returned by the method
	 * getCompatibleCellState(). Appropriate CellStatesViews to return include
	 * BinaryCellStateView, IntegerCellStateView, HexagonalIntegerCellStateView,
	 * IntegerVectorArrowView, IntegerVectorDefaultView, and
	 * RealValuedDefaultView among others. the user may also create their own
	 * views (see online documentation).
	 * <p>
	 * Any values passed to the constructor of the CellStateView should match
	 * those values needed by this rule.
	 * 
	 * @return An instance of the CellStateView (any values passed to the
	 *         constructor of the CellStateView should match those values needed
	 *         by this rule).
	 */
	public CellStateView getCompatibleCellStateView()
	{
		return new ComplexView();
	}

	/**
	 * A list of lattices with which this Rule will work. Returns null to
	 * indicate that this rule works with any lattice. <br>
	 * Well-designed Rules should work with any lattice, but some may require
	 * particular topological or geometrical information (like the lattice gas).
	 * Appropriate strings to return in the array include
	 * SquareLattice.DISPLAY_NAME, HexagonalLattice.DISPLAY_NAME,
	 * StandardOneDimensionalLattice.DISPLAY_NAME, etc.
	 * 
	 * @return A list of lattices compatible with this Rule.
	 */
	public String[] getCompatibleLattices()
	{
		return null;
	}

	/**
	 * When displayed for selection, the rule will be listed under specific
	 * folders specified here. The rule will always be listed under the "All
	 * rules" folder. And if the rule is contributed by a user and is placed in
	 * the userRules folder, then it will also be shown in a folder called "User
	 * rules". Any strings may be used; if the folder does not exist, then one
	 * will be created with the specified name. If the folder already exists,
	 * then that folder will be used.
	 * <p>
	 * By default, this returns null so that the rule is only placed in the
	 * default folder(s).
	 * <p>
	 * Child classes should override this method if they want the rule to appear
	 * in a specific folder. The "All rules" and "User rules" folder are
	 * automatic and do not need to be specified; they are always added.
	 * 
	 * @return A list of the folders in which rule will be displayed for
	 *         selection. May be null.
	 */
	public String[] getDisplayFolderNames()
	{
		String[] folders = {RuleFolderNames.PRETTY_FOLDER,
				RuleFolderNames.COMPLEX_VALUED_FOLDER,
				RuleFolderNames.COMPUTATIONALLY_INTENSIVE_FOLDER};

		return folders;
	}

	/**
	 * A brief one or two-word string describing the rule, appropriate for
	 * display in a drop-down list.
	 * 
	 * @return A string no longer than 15 characters.
	 */
	public String getDisplayName()
	{
		return RULE_NAME;
	}

	/**
	 * Gets a complex number that represents the empty state (in this case, a
	 * random complex number).
	 * 
	 * @return The empty state.
	 */
	public Complex getEmptyState()
	{
		return new Complex(0.0, 0.0);
	}

	/**
	 * Gets a complex number that represents the full or filled state.
	 * 
	 * @return The full state.
	 */
	public Complex getFullState()
	{
		return new Complex(3.0, 3.0);
	}

	/**
	 * Sets an initial state that corresponds to the values on a complex plane.
	 * 
	 * @param initialStateName
	 *            The name of the initial state (will be one of the names
	 *            specified in the getInitialStateNames method).
	 * @param lattice
	 *            The CA lattice. This will either be a one or two-dimensional
	 *            lattice which holds the cells. The cells should be collected
	 *            from the lattice and assigned initial values.
	 */
	// public void setInitialState(String initialStateName, Lattice lattice)
	// {
	// Cell cell = null;
	// Iterator cellIterator = lattice.iterator();
	// while(cellIterator.hasNext())
	// {
	// cell = (Cell) cellIterator.next();
	//
	// // get the list of states for each cell
	// cell.getState().setValue(getInitialStateValue(initialStateName));
	// }
	// }
	/**
	 * Gets an array of names for any initial states defined by the rule. In
	 * other words, if the rule has two initial states that it would like to
	 * define, then they are each given names and placed in an array of size 2
	 * that is returned by this method. The names are then displayed on the
	 * Properties panel. The CA will always have "single seed", "random", and
	 * "blank" for initial states. This array specifies additional initial
	 * states that should be displayed. The names should be unique. By default
	 * this returns null. Child classes should override the method if they wish
	 * to specify initial states that will appear on the Properties panel. Note:
	 * This method should be used in conjuction with the getInitialState method,
	 * also in this class.
	 * 
	 * @return An array of names for initial states that are specified by the
	 *         rule.
	 */
	// public String[] getInitialStateNames()
	// {
	// String[] initialStates = {INIT_STATE_COMPLEX_PLANE};
	// return initialStates;
	// }
	/**
	 * Gets tool tips for the initial states.
	 * 
	 * @return An array of tool tips for initial states that are specified by
	 *         the rule.
	 */
	// public String[] getInitialStateToolTips()
	// {
	// String[] initialStateToolTips = {INIT_STATE_COMPLEX_PLANE_TOOLTIP};
	// return initialStateToolTips;
	// }
	/**
	 * The initial state value for the given parameter.
	 * 
	 * @param nameOfinitialState
	 *            The name of the initial state configuration.
	 * @return The value of the cell.
	 */
	// public Complex getInitialStateValue(String nameOfinitialState)
	// {
	// // set the initial state instance variable
	// initialStateName = nameOfinitialState;
	//
	// // the value of the cell
	// Complex cellValue = new Complex(0.0, 0.0);
	//
	// // let's set up the initial fractal by populating the grid with
	// // numbers that fall in their correct location on the complex plane
	// int numRows = Integer.parseInt(super.properties
	// .getProperty(CAPropertyReader.CA_HEIGHT));
	// int numCols = Integer.parseInt(super.properties
	// .getProperty(CAPropertyReader.CA_WIDTH));
	//
	// // reset to the origin when necessary
	// if(cellNum >= numRows * numCols)
	// {
	// cellNum = 0;
	// }
	//
	// // make sure the simulation didn't get reset
	// if(numberOfCols != numCols)
	// {
	// numberOfCols = numCols;
	// cellNum = 0;
	// }
	// if(numberOfRows != numRows)
	// {
	// numberOfRows = numRows;
	// cellNum = 0;
	// }
	//
	// int row = cellNum / numRows; // integer division on purpose!
	// int col = cellNum % numCols;
	//
	// // increase each cell by this amount
	// double increment = widthOfDisplay / (double) numRows;
	//
	// // the normal complex plane
	// cellValue = getCellValueForComplexPlane(row, col, increment);
	//
	// cellNum++;
	//
	// return cellValue;
	// }
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
	 * Gets the length of the vectors (arrays) that will be used by the Rule.
	 * The length must be the same for all cells.
	 * 
	 * @return The length of the vector stored by each cell.
	 */
	public int getVectorLength()
	{
		// One value is the julia constant c. The other is the value z.
		return 2;
	}

	/**
	 * Each cell is a complex number c where z = z^2 + c from the Julia set
	 * equation. The c values are averaged for each cell and it's neighbors. The
	 * z value is the cell's position on the lattice and is only used when
	 * calculating the view.
	 * 
	 * @param cell
	 *            The value of the cell being updated.
	 * @param neighbors
	 *            The value of the neighbors.
	 * @param generation
	 *            The current generation of the CA.
	 * @return A new state for the cell.
	 */
	protected Complex complexRule(Complex cellValue, Complex[] neighborValues,
			int generation)
	{
		// reset these instance variables to reflect the values on the More
		// Properties panel
		// Only update spinner parameters (like realValue) at the beginning
		// of each generation. This prevents some cells from seeing one
		// realValue and other cells seeing another realValue.
		if(currentGeneration != generation)
		{
			currentGeneration = generation;

			widthOfDisplay = ((Double) ((SpinnerNumberModel) widthSpinner
					.getModel()).getNumber()).doubleValue();
			double realValue = ((Double) ((SpinnerNumberModel) realPositionSpinner
					.getModel()).getNumber()).doubleValue();
			double imaginaryValue = ((Double) ((SpinnerNumberModel) imaginaryPositionSpinner
					.getModel()).getNumber()).doubleValue();
			upperLeftCorner = new Complex(realValue - widthOfDisplay / 2.0,
					-imaginaryValue - widthOfDisplay / 2.0);
		}

		// the new c value of the cell that will be returned
		Complex newCValue = new Complex(cellValue.getReal(), cellValue
				.getImaginary());

		// let's average the c values of the cell and it's neighbors
		for(int i = 0; i < neighborValues.length; i++)
		{
			Complex neighborCValue = neighborValues[i];
			newCValue.real += neighborCValue.real;
			newCValue.imaginary += neighborCValue.imaginary;
		}

		// now divide to get the average of the cell and its neighbors
		newCValue.real /= (neighborValues.length + 1.0);
		newCValue.imaginary /= (neighborValues.length + 1.0);

		// add a small increment at each step
		// newCValue.real += DEFAULT_INCREMENT_VALUE;
		// newCValue.imaginary += DEFAULT_INCREMENT_VALUE;
		int cycleLength = 400;
		newCValue.real += DEFAULT_INCREMENT_VALUE
				* Math.sin(2.0 * Math.PI * (generation % cycleLength)
						/ (cycleLength - 1.0));
		newCValue.imaginary += DEFAULT_INCREMENT_VALUE
				* Math.sin(2.0 * Math.PI * (generation % cycleLength)
						/ (cycleLength - 1.0));

		// return the new complex number used as the cell value
		return newCValue;
	}

	/**
	 * A view that displays complex numbers as fractals or simple shades. Tells
	 * the graphics how to display the complexNumber stored by a cell.
	 * 
	 * @author David Bahr
	 */
	private class ComplexView extends TriangleHexagonCellStateView
	{
		/**
		 * Creates a view that displays complex numbers as fractals or simple
		 * shades. Tells the graphics how to display the complexNumber stored by
		 * a cell.
		 */
		public ComplexView()
		{
			super();
		}

		/**
		 * Creates a display color based on the complex number in the cell.
		 * Creates a fractional shading between the default filled and empty
		 * color.
		 * 
		 * @param state
		 *            The cell state that will be displayed.
		 * @param numStates
		 *            If relevant, the number of possible states (which may not
		 *            be the same as the currently active number of states) --
		 *            may be null which indicates that the number of states is
		 *            inapplicable or that the currently active number of states
		 *            should be used. (See for example,
		 *            createProbabilityChoosers() method in InitialStatesPanel
		 *            class.)
		 * @param rowAndCol
		 *            The row and col of the cell being displayed. May be
		 *            ignored.
		 * @return The color to be displayed.
		 */
		public Color getColor(CellState state, Integer numStates,
				Coordinate rowAndCol)
		{
			// the number of rows
			int numRows = CurrentProperties.getInstance().getNumRows();

			// each cell increases by this amount as you move across the grid
			double distance = widthOfDisplay / (double) numRows;

			// ROW and COL ARE NO LONGER PASSED IN!
			// Get the z value from the "parameters" variable which is always
			// passed the row/col info by TwoDimensionalLattice and
			// OneDimensionalLattice in their calls to getDisplayColor().
			// To do this we also need to get the distance between cells.
			int row = 0;
			int col = 0;
			if(rowAndCol != null)
			{
				// then holds the row and col position
				try
				{
					row = rowAndCol.getRow();
					col = rowAndCol.getColumn();
				}
				catch(Exception e)
				{
					// do nothing -- must not be a standard one- or
					// two-dimensional lattice, so must not return the row and
					// col position.
				}
			}
			Complex zValueInitial = getCellValueForComplexPlane(row, col,
					distance);

			// the value evolved by the CA
			Complex juliaConstant = (Complex) state.getValue();

			// can view as a fractal or in normal complex space (as the modulus)
			double iteration = 0;
			int maxIteration = 40;

			// view as a julia set
			// i.e., color according to the algorithm z = z^2 + c where
			// c is a fixed constant (not necessarily z_0, the value of
			// the cell), and the rate of divergence dictates the color.
			// If never diverges, then the cell value is on the Julia set.
			iteration = 0;
			maxIteration = 40;

			// Calculate the first iteration. Note that juliaConstant is the
			// constant "c"
			Complex z = new Complex(zValueInitial.getReal(), zValueInitial
					.getImaginary());
			z = Complex.plus(Complex.multiply(z, z), juliaConstant);

			// Now iterate until diverges. The variable "iteration" holds
			// the number of times before diverged. This is used to plot a
			// shade of color.
			while(z.modulus() < 1.75 && iteration < maxIteration)
			{
				z = Complex.plus(Complex.multiply(z, z), juliaConstant);

				iteration = iteration + 1;
			}

			// now select a color scaled between the empty and filled color
			Color filledColor = ColorScheme.FILLED_COLOR;
			Color emptyColor = ColorScheme.EMPTY_COLOR;

			double redDiff = filledColor.getRed() - emptyColor.getRed();
			double greenDiff = filledColor.getGreen() - emptyColor.getGreen();
			double blueDiff = filledColor.getBlue() - emptyColor.getBlue();

			double redDelta = redDiff / (maxIteration - 1);
			double greenDelta = greenDiff / (maxIteration - 1);
			double blueDelta = blueDiff / (maxIteration - 1);

			int red = (int) Math.floor(emptyColor.getRed()
					+ (iteration * redDelta));
			int green = (int) Math.floor(emptyColor.getGreen()
					+ (iteration * greenDelta));
			int blue = (int) Math.floor(emptyColor.getBlue()
					+ (iteration * blueDelta));

			if(iteration == maxIteration)
			{
				red = 0;
				green = 0;
				blue = 0;
			}

			return new Color(red, green, blue);
		}
	}
}