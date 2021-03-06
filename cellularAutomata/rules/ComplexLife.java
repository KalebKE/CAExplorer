/*
 ComplexLife -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.rules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

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

/**
 * Creates a Life-like simulation. Takes the average of the cell and its
 * neighbors, plugs into the equation "slope * avg^2 + yInterecept", and then
 * keeps only the fractional part. The value of the slope is 0.9+0.9i and the
 * value of the intercept is 0.39+0.98i. The stability of the simulation
 * (whether or not life-forms die out too quickly or proliferate to rapidly
 * depends sensitively on these values).
 * 
 * @author David Bahr
 */
public class ComplexLife extends ComplexRuleTemplate
{
	// default slope value for the linear equation that calculates the
	// cell's new value
	private static final double DEFAULT_SLOPE_VALUE_RE = 0.9;

	// default slope value for the linear equation that calculates the
	// cell's new value
	private static final double DEFAULT_SLOPE_VALUE_IMG = 0.9;

	// default y-intercept value for the linear equation that calculates the
	// cell's new value
	private static final double DEFAULT_Y_INTERCEPT_VALUE_RE = 0.39;

	// default y-intercept value for the linear equation that calculates the
	// cell's new value
	private static final double DEFAULT_Y_INTERCEPT_VALUE_IMG = 0.98;

	// label for the close button
	private static final String CLOSE = "Close";

	// a display name for this class
	private static final String RULE_NAME = "Complex Life";

	// tooltip for setting the slope
	private static final String SLOPE_TIP = "<html><body>Sets the slope of the equation "
			+ "used to calculate the cell's value. <br>"
			+ "Values near 0.9 + 0.9 i work best.</body></html>";

	// tooltip for setting the y intercept
	private static final String Y_INTERCEPT_TIP = "<html><body>Sets the y-intercept of the "
			+ "equation used to calculate the cell's value. <br>Values near 0.39 + 0.98 i work "
			+ "best.</body></html>";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, try a random initial state on a 100 by "
			+ "100 or larger square (8 neighbor) lattice.  Open the More Properties box and "
			+ "adjust the values as described. " + leftClickInstructions
			+ rightClickInstructions + "</body></html>";

	// y-intercept of the linear equation used to calculate the value of the
	// cell for next generation
	private static volatile double yInterceptRe = 0.39;

	// y-intercept of the linear equation used to calculate the value of the
	// cell for next generation
	private static volatile double yInterceptImg = 0.98;

	// slope of the linear equation used to calculate the value of the cell for
	// next generation
	private static volatile double slopeRe = 0.9;

	// slope of the linear equation used to calculate the value of the cell for
	// next generation
	private static volatile double slopeImg = 0.9;

	// the current generation being processed by the rule
	private static volatile int currentGeneration = -1;

	// The label for the slope
	private static JLabel slopeLabel = null;

	// The label for the slope Re
	private static JLabel slopeLabelRe = null;

	// The label for the slope Img
	private static JLabel slopeLabelImg = null;

	// The label for the y intercept
	private static JLabel yInterceptLabel = null;

	// The label for the y intercept Re
	private static JLabel yInterceptLabelRe = null;

	// The label for the y intercept Img
	private static JLabel yInterceptLabelImg = null;

	// the JPanel that is returned by getAdditionalPropertiesPanel()
	private static JPanel panel = null;

	// selects the slope of the equation
	private static JSpinner slopeSpinnerRe = null;

	// selects the slope of the equation
	private static JSpinner slopeSpinnerImg = null;

	// selects the y intercept of the equation
	private static JSpinner yInterceptSpinnerRe = null;

	// selects the y intercept of the equation
	private static JSpinner yInterceptSpinnerImg = null;

	// fonts for display
	private Fonts fonts = null;

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>" + RULE_NAME
			+ ".</b> Creates \"Game of Life\"-like behavior but uses "
			+ "arbitrary non-integer complex numbers.</body></html>";

	/**
	 * Creates a Life-like simulation. Takes the average of the cell and its
	 * neighbors, plugs into the equation "slope * avg^2 + yInterecept", and
	 * then keeps only the fractional part. The value of the slope is 0.9+0.9i
	 * and the value of the intercept is 0.39+0.98i. The stability of the
	 * simulation (whether or not life-forms die out too quickly or proliferate
	 * to rapidly depends sensitively on these values).
	 * <p>
	 * What is meant by fractional part? Think of the complex number in polar
	 * coordinates. The cell is given the same angle, but only the fractional
	 * part of the magnitude complex modulus) is kept. The result is a cell
	 * value that always lies within the unit circle.
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
	public ComplexLife(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			fonts = new Fonts();
		}
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
		AttentionPanel attentionPanel = new AttentionPanel(
				"Slope and Intercept");

		String functionDescription = "These controls let you set the slope and y-intercept "
				+ "of the equation used to calculate each cell.  The equation is y = mx^2+b "
				+ "where m is the slope, b is the intercept, and x is the average of the "
				+ "cell and its neighbors.  The cell is assigned the fractional part of y. "
				+ "In other words, think of the complex number in polar coordinates. The "
				+ "cell is given the same angle, but only the fractional part of the magnitude "
				+ "(complex modulus) is kept. The result is a cell value that always lies within "
				+ "the unit circle.\n\n"
				+ "Life-like behavior happens when \n\n"
				+ "\t slope = 0.9 + 0.9 i \n "
				+ "\t y-intercept = 0.39 + 0.98 i. \n\n"
				+ "The Life-like behavior is sensitive to these values.  Try "
				+ "slowly changing the values to see the sensitivity. Other values will cause "
				+ "the cells to over-proliferate or to die off too quickly. \n\n"
				+ "Only the fraction part of y is kept, so values of the y-intercept outside "
				+ "of the range 0.0 and 1.0 just repeat behaviors seen within this range. \n\n"
				+ "Any changes will show up during the next generation (hit enter).";

		MultilineLabel messageLabel = new MultilineLabel(functionDescription);
		messageLabel.setFont(fonts.getMorePropertiesDescriptionFont());
		messageLabel.setMargin(new Insets(2, 6, 2, 2));

		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		messagePanel.add(attentionPanel, BorderLayout.NORTH);
		messagePanel.add(messageLabel, BorderLayout.CENTER);

		return messagePanel;
	}

	/**
	 * Creates spinners for setting the slope and intercept.
	 * 
	 * @return A panel holding the text fields.
	 */
	private JPanel createAdditionalPropertiesPanel()
	{
		// label for the slope
		slopeLabel = new JLabel("Slope");
		slopeLabel.setFont(fonts.getBoldFont());

		// label for the intercept
		yInterceptLabel = new JLabel("Y-intercept");
		yInterceptLabel.setFont(fonts.getBoldFont());

		// add a label for the slope Re
		slopeLabelRe = new JLabel("Real: ");
		slopeLabelRe.setFont(fonts.getPlainFont());

		// add a label for the slope Img
		slopeLabelImg = new JLabel("Imaginary: ");
		slopeLabelImg.setFont(fonts.getPlainFont());

		// add a label for the real y-intercept
		yInterceptLabelRe = new JLabel("Real: ");
		yInterceptLabelRe.setFont(fonts.getPlainFont());

		// add a label for the imaginary y-intercept
		yInterceptLabelImg = new JLabel("Imaginary: ");
		yInterceptLabelImg.setFont(fonts.getPlainFont());

		// create a spinner for the slope Re
		SpinnerNumberModel slopeModelRe = new SpinnerNumberModel(
				DEFAULT_SLOPE_VALUE_RE, -10000.0, 10000.0, 0.1);
		slopeSpinnerRe = new JSpinner(slopeModelRe);
		slopeSpinnerRe.setToolTipText(SLOPE_TIP);
		slopeSpinnerRe.setFont(fonts.getPlainFont());
		((JSpinner.DefaultEditor) slopeSpinnerRe.getEditor()).getTextField()
				.setColumns(6);

		// create a spinner for the slope Img
		SpinnerNumberModel slopeModelImg = new SpinnerNumberModel(
				DEFAULT_SLOPE_VALUE_IMG, -10000.0, 10000.0, 0.1);
		slopeSpinnerImg = new JSpinner(slopeModelImg);
		slopeSpinnerImg.setToolTipText(SLOPE_TIP);
		slopeSpinnerImg.setFont(fonts.getPlainFont());
		((JSpinner.DefaultEditor) slopeSpinnerImg.getEditor()).getTextField()
				.setColumns(6);

		// create a spinner for the y intercept Re
		SpinnerNumberModel yInterceptModelRe = new SpinnerNumberModel(
				DEFAULT_Y_INTERCEPT_VALUE_RE, 0.000, 1.000, 0.01);
		yInterceptSpinnerRe = new JSpinner(yInterceptModelRe);
		yInterceptSpinnerRe.setToolTipText(Y_INTERCEPT_TIP);
		yInterceptSpinnerRe.setFont(fonts.getPlainFont());
		((JSpinner.DefaultEditor) yInterceptSpinnerRe.getEditor())
				.getTextField().setColumns(6);

		// create a spinner for the y intercept Img
		SpinnerNumberModel yInterceptModelImg = new SpinnerNumberModel(
				DEFAULT_Y_INTERCEPT_VALUE_IMG, 0.000, 1.000, 0.01);
		yInterceptSpinnerImg = new JSpinner(yInterceptModelImg);
		yInterceptSpinnerImg.setToolTipText(Y_INTERCEPT_TIP);
		yInterceptSpinnerImg.setFont(fonts.getPlainFont());
		((JSpinner.DefaultEditor) yInterceptSpinnerImg.getEditor())
				.getTextField().setColumns(6);

		// create combo panel
		JPanel spinnerPanel = new JPanel(new GridBagLayout());
		int row1 = 0;
		spinnerPanel.add(slopeLabel, new GBC(1, row1).setSpan(4, 1).setFill(
				GBC.HORIZONTAL).setWeight(10.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(10));

		row1++;
		spinnerPanel.add(new JLabel(" "), new GBC(1, row1).setSpan(1, 1)
				.setFill(GBC.HORIZONTAL).setWeight(10.0, 1.0).setAnchor(
						GBC.WEST).setInsets(0));
		spinnerPanel.add(slopeLabelRe, new GBC(2, row1).setSpan(1, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(0));
		spinnerPanel.add(slopeSpinnerRe, new GBC(3, row1).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0));
		spinnerPanel.add(new JLabel(" "), new GBC(4, row1).setSpan(1, 1)
				.setFill(GBC.HORIZONTAL).setWeight(10.0, 1.0).setAnchor(
						GBC.WEST).setInsets(0));
		row1++;
		spinnerPanel.add(slopeLabelImg, new GBC(2, row1).setSpan(1, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(0));
		spinnerPanel.add(slopeSpinnerImg, new GBC(3, row1).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0));

		row1++;
		spinnerPanel.add(new JLabel(" "), new GBC(1, row1).setSpan(4, 1)
				.setFill(GBC.HORIZONTAL).setWeight(10.0, 1.0).setAnchor(
						GBC.WEST).setInsets(1));
		row1++;
		spinnerPanel.add(yInterceptLabel, new GBC(1, row1).setSpan(4, 1)
				.setFill(GBC.HORIZONTAL).setWeight(10.0, 1.0).setAnchor(
						GBC.WEST).setInsets(10));

		row1++;
		spinnerPanel.add(yInterceptLabelRe, new GBC(2, row1).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0));
		spinnerPanel.add(yInterceptSpinnerRe, new GBC(3, row1).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0));
		row1++;
		spinnerPanel.add(yInterceptLabelImg, new GBC(2, row1).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0));
		spinnerPanel.add(yInterceptSpinnerImg, new GBC(3, row1).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0));

		// add descriptive labels
		JPanel descriptionPanel = createDescriptionPanel();
		JPanel descriptionLabelPanel = new JPanel(new FlowLayout(
				FlowLayout.LEFT));
		descriptionLabelPanel.add(descriptionPanel);

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
	 * Creates a Life-like simulation. Takes the average of the cell and its
	 * neighbors, plugs into the equation "slope * avg^2 + yInterecept", and
	 * then keeps only the fractional part. The value of the slope is 0.9+0.9i
	 * and the value of the intercept is 0.39+0.98i. The stability of the
	 * simulation (whether or not life-forms die out too quickly or proliferate
	 * to rapidly depends sensitively on these values).
	 * <p>
	 * What is meant by fractional part? Think of the complex number in polar
	 * coordinates. The cell is given the same angle, but only the fractional
	 * part of the magnitude (complex modulus) is kept. The result is a cell
	 * value that always lies within the unit circle.
	 * <p>
	 * The life like behavior is only seen when each cell is assigned a color
	 * depending on whether or not it is on the Mandelbrot set. Dark colors are
	 * on the Mandelbrot set (converge). Increasingly lighter colors represent
	 * faster divergence (from the Mandelbrot set).
	 * 
	 * @see cellularAutomata.rules.templates.ComplexRuleTemplate#complexRule(
	 *      Complex, Complex[], int)
	 */
	public Complex complexRule(Complex cell, Complex[] neighbors,
			int generation)
	{
		// Get the values from the More Properties panel. Only update slider
		// parameters (like slope and y-intercept) at the beginning
		// of each generation. This prevents some cells from seeing one
		// slope and other cells seeing another slope.
		if(currentGeneration != generation)
		{
			slopeRe = ((Double) ((SpinnerNumberModel) slopeSpinnerRe.getModel())
					.getNumber()).doubleValue();
			slopeImg = ((Double) ((SpinnerNumberModel) slopeSpinnerImg
					.getModel()).getNumber()).doubleValue();
			yInterceptRe = ((Double) ((SpinnerNumberModel) yInterceptSpinnerRe
					.getModel()).getNumber()).doubleValue();
			yInterceptImg = ((Double) ((SpinnerNumberModel) yInterceptSpinnerImg
					.getModel()).getNumber()).doubleValue();
			
			currentGeneration = generation;
		}

		Complex slope = new Complex(slopeRe, slopeImg);
		Complex yIntercept = new Complex(yInterceptRe, yInterceptImg);

		// average the cell and its neighbors
		Complex avg = new Complex(cell);
		for(int i = 0; i < neighbors.length; i++)
		{
			avg = new Complex(Complex.add(avg, neighbors[i]));
		}

		// take the average
		avg = new Complex(Complex.divide(avg, new Complex(neighbors.length + 1,
				0.0)));

		// transform with y = m * avg^2 + b
		Complex transformedValue = Complex.add(Complex.multiply(slope, Complex
				.multiply(avg, avg)), yIntercept);

		// remove the integer part greater than 1. In other words, keep the same
		// angle (complex arg) but remove the integer part of complex modulus so
		// that the value falls within the unit circle.
		double theta = transformedValue.arg();
		double newModulus = transformedValue.modulus()
				- Math.floor(transformedValue.modulus());
		double newReal = newModulus
				/ Math.sqrt((Math.tan(theta) * Math.tan(theta)) + 1.0);
		double newImaginary = newReal * Math.tan(theta);

		// IF INCLUDE THE FOLLOWING, THEN CHANGE VALUES TO 0.9+0.9i AND
		// 0.312+1.0i.
		// fix the sign of the real part
		// if((theta > Math.PI / 2.0 && theta < 1.5 * Math.PI)
		// || (theta < -Math.PI / 2.0 && theta > -1.5 * Math.PI))
		// {
		// newReal *= -1.0;
		// }

		Complex returnValue = new Complex(newReal, newImaginary);

		return returnValue;
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
	 * a random number between the full and empty states.
	 * 
	 * @return The alternate state.
	 */
	public Complex getAlternateState()
	{
		Random r = RandomSingleton.getInstance();

		double real = r.nextDouble()
				* (getFullState().real - getEmptyState().real)
				+ getEmptyState().real;
		double imaginary = r.nextDouble()
				* (getFullState().imaginary - getEmptyState().imaginary)
				+ getEmptyState().imaginary;

		return new Complex(real, imaginary);
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
	 * A brief one or two-word string describing the rule, appropriate for
	 * display in a drop-down list.
	 * 
	 * @return A string no longer than 15 characters.
	 * @see cellularAutomata.rules.Rule#getDisplayName()
	 */
	public String getDisplayName()
	{
		return RULE_NAME;
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
		String[] folders = {RuleFolderNames.COMPLEX_VALUED_FOLDER,
				RuleFolderNames.LIFE_LIKE_FOLDER};

		return folders;
	}

	/**
	 * Gets a complex number that represents the empty state. Implementations
	 * should be careful to return a new instance each time this is called.
	 * Otherwise, multiple CA cells may be sharing the same instance and a
	 * change to one cell could change many cells.
	 * 
	 * @return The empty state.
	 */
	public Complex getEmptyState()
	{
		return new Complex(0.0, 0.0);
	}

	/**
	 * Gets a complex number that represents the full or filled state.
	 * Implementations should be careful to return a new instance each time this
	 * is called. Otherwise, multiple CA cells may be sharing the same instance
	 * and a change to one cell could change many cells.
	 * 
	 * @return The full state.
	 */
	public Complex getFullState()
	{
		return new Complex(3.0, 3.0);
	}

	/**
	 * A brief description (written in HTML) that describes this rule. The
	 * description will be displayed as a tooltip. Using html permits line
	 * breaks, font colors, etcetera, as described in HTML resources. Regular
	 * line breaks will not work.
	 * 
	 * @return An HTML string describing this rule.
	 * @see cellularAutomata.rules.Rule#getToolTipDescription()
	 */
	public String getToolTipDescription()
	{
		return TOOLTIP;
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
			Complex cellValue = (Complex) state.getValue();

			double iteration = 0;
			int maxIteration = 40;

			// view as the mandelbrot set
			// i.e., color according to the algorithm z = z^2 + c where
			// c is given by z_0 (the value of the cell), and the
			// rate of divergence dictates the color. If never diverges,
			// then the cell value is on the Mandelbrot set.
			iteration = 0;
			maxIteration = 40;

			// calculate the first iteration
			Complex c = new Complex(cellValue.getReal(), cellValue
					.getImaginary());
			Complex z = new Complex(cellValue.getReal(), cellValue
					.getImaginary());
			z = Complex.plus(Complex.multiply(z, z), c);

			// Now iterate until diverges. The variable "iteration" holds
			// the number of times before diverged. This is used to plot a
			// shade of color.
			while(z.modulus() < 1.75 && iteration < maxIteration)
			{
				z = Complex.plus(Complex.multiply(z, z), c);

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
