/*
 ContinuousCA -- a class within the Cellular Automaton Explorer. 
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
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import cellularAutomata.rules.templates.RealRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;

/**
 * Takes the average of the cell and its neighbors, plugs into the linear
 * equation "slope * avg + yInterecept", and then keeps only the fractional
 * part.
 * 
 * @author David Bahr
 */
public class ContinuousCA extends RealRuleTemplate
{
	// default slope value for the linear equation that calculates the
	// cell's new value
	private static final double DEFAULT_SLOPE_VALUE = 1.00;

	// default y-intercept value for the linear equation that calculates the
	// cell's new value
	private static final double DEFAULT_Y_INTERCEPT_VALUE = 0.10;

	// The maximum allowed value. DO NOT recommend using Double.MAX_VALUE
	// unless carefully checking for infinity.
	private static final double MAX_VALUE = 1.0;

	// The minimum allowed value. DO NOT recommend using Double.MIN_VALUE
	// unless carefully checking for infinity.
	private static final double MIN_VALUE = 0.0;

	// label for the close button
	private static final String CLOSE = "Close";

	// a display name for this class
	private static final String RULE_NAME = "Continuous CA";

	// tooltip for setting the slope
	private static final String SLOPE_TIP = "<html><body>Sets the slope of the linear equation "
			+ "used to calculate the cell's value. <br><br>"
			+ "Values near 1.0 and -1.0 work best.</body></html>";

	// tooltip for setting the y intercept
	private static final String Y_INTERCEPT_TIP = "<html><body>Sets the y-intercept of the "
			+ "linear equation used to calculate the cell's value.</body></html>";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, try a single seed on a 200 by "
			+ "200 lattice in one dimension.  Open the More Properties box and "
			+ "adjust the values as described.  Also try two-dimensional simulations "
			+ "starting from a 100% random configuration. <br><br>"
			+ "It's way cool to spin through slope values and watch the simulation "
			+ "change (open the More Properties box)." + leftClickInstructions
			+ rightClickInstructions + "</body></html>";

	// y-intercept of the linear equation used to calculate the value of the
	// cell for next generation
	private static volatile double yIntercept = 0.1;

	// slope of the linear equation used to calculate the value of the cell for
	// next generation
	private static volatile double slope = 1.0;

	// the current generation being processed by the rule
	private static volatile int currentGeneration = -1;

	// The label for the slope
	private static JLabel slopeLabel = null;

	// The label for the y intercept
	private static JLabel yInterceptLabel = null;

	// the JPanel that is returned by getAdditionalPropertiesPanel()
	private static JPanel panel = null;

	// selects the slope of the equation
	private static JSpinner slopeSpinner = null;

	// selects the y intercept of the equation
	private static JSpinner yInterceptSpinner = null;

	// fonts for display
	private Fonts fonts = null;

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>" + RULE_NAME
			+ ".</b> Creates triangles similar to the Wolfram rules, but uses "
			+ "real numbers.</body></html>";

	/**
	 * Takes the average of the cell and its neighbors, plugs into the linear
	 * equation "slope * avg + yInterecept", and then keeps only the fractional
	 * part.
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
	public ContinuousCA(boolean minimalOrLazyInitialization)
	{
		super(MIN_VALUE, MAX_VALUE, minimalOrLazyInitialization);

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
		AttentionPanel attentionPanel = new AttentionPanel("CA Equation");

		String functionDescription = "These controls let you set the slope and y-intercept "
				+ "of the equation used to calculate each cell.  The equation is y = mx+b "
				+ "where m is the slope, b is the intercept, and x is the average of the "
				+ "cell and its neighbors.  The cell is assigned the fractional part of y. "
				+ "(In other words, the whole number part is discarded.) \n\n"
				+ "To see the full range of possible behaviors try the following values: \n\n"
				+ "          slope = 1.0 \n"
				+ "          y-intercept = 0.1, 0.2, ..., 1.0 \n\n"
				+ "Notice that these behaviors appear to cover all of the Wolfram classes I, "
				+ "II, III, and IV. \n\n"
				+ "With a one-dim (2 neighbor) lattice, try other values such as \n\n"
				+ "          slope = -1.5 and y-intercept = 0.76 \n"
				+ "          slope = 0.6 and y-intercept = 0.89 \n"
				+ "          slope = 1.0 and y-intercept = 0.408 \n"
				+ "          (hit return after typing 0.408). \n\n"
				+ "With a square (8 neighbor) lattice try: \n\n"
				+ "          slope = 1.0 and y-intercept = 0.01 \n"
				+ "          slope = 1.0 and y-intercept = 0.51 \n"
				+ "          slope = -1.0 and y-intercept = 0.51. \n\n"
				+ "Slope values near 1.0 and -1.0 work best.  Only the fraction part "
				+ "of y is kept, so values of the y-intercept outside of the range 0.0 and 1.0 "
				+ "just repeat behaviors seen within this range. \n\n"
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
	 * @return A panel holding the spinners.
	 */
	private JPanel createAdditionalPropertiesPanel()
	{
		// add a label for the slope
		slopeLabel = new JLabel("Slope: ");
		slopeLabel.setFont(fonts.getBoldFont());

		// add a label for the real part
		yInterceptLabel = new JLabel("Y-intercept: ");
		yInterceptLabel.setFont(fonts.getBoldFont());

		// create a spinner for the slope
		SpinnerNumberModel slopeModel = new SpinnerNumberModel(
				DEFAULT_SLOPE_VALUE, -10000.0, 10000.0, 0.1);
		slopeSpinner = new JSpinner(slopeModel);
		slopeSpinner.setToolTipText(SLOPE_TIP);
		slopeSpinner.setFont(fonts.getPlainFont());
		((JSpinner.DefaultEditor) slopeSpinner.getEditor()).getTextField()
				.setColumns(5);

		// create a spinner for the y intercept
		SpinnerNumberModel yInterceptModel = new SpinnerNumberModel(
				DEFAULT_Y_INTERCEPT_VALUE, 0.00, 1.00, 0.01);
		yInterceptSpinner = new JSpinner(yInterceptModel);
		yInterceptSpinner.setToolTipText(Y_INTERCEPT_TIP);
		yInterceptSpinner.setFont(fonts.getPlainFont());
		((JSpinner.DefaultEditor) yInterceptSpinner.getEditor()).getTextField()
				.setColumns(5);

		// create combo panel
		JPanel slopeAndInterceptPanel = new JPanel(new GridBagLayout());
		int row1 = 0;

		slopeAndInterceptPanel.add(new JLabel(" "), new GBC(1, row1).setSpan(1,
				1).setFill(GBC.HORIZONTAL).setWeight(10.0, 1.0).setAnchor(
				GBC.WEST).setInsets(0));
		slopeAndInterceptPanel.add(slopeLabel, new GBC(2, row1).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0));
		slopeAndInterceptPanel.add(slopeSpinner, new GBC(3, row1).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0));
		slopeAndInterceptPanel.add(new JLabel(" "), new GBC(4, row1).setSpan(1,
				1).setFill(GBC.HORIZONTAL).setWeight(10.0, 1.0).setAnchor(
				GBC.WEST).setInsets(0));
		row1++;
		slopeAndInterceptPanel.add(yInterceptLabel, new GBC(2, row1).setSpan(1,
				1).setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0));
		slopeAndInterceptPanel.add(yInterceptSpinner, new GBC(3, row1).setSpan(
				1, 1).setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0));

		// add descriptive labels
		JPanel descriptionPanel = createDescriptionPanel();

		// add the components to a JPanel
		JPanel constantPanel = new JPanel(new GridBagLayout());

		int row = 0;
		constantPanel.add(descriptionPanel, new GBC(0, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0));

		row++;
		constantPanel.add(slopeAndInterceptPanel, new GBC(0, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0, 0, 10, 0));

		row++;
		constantPanel.add(new JLabel(" "), new GBC(0, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(5.0, 5.0).setAnchor(GBC.WEST)
				.setInsets(0));

		return constantPanel;
	}

	/**
	 * Takes the average of the cell and its neighbors, plugs into the linear
	 * equation "slope * avg + yInterecept", and then keeps only the fractional
	 * part.
	 * 
	 * @see cellularAutomata.rules.templates.RealRuleTemplate#doubleRule(
	 *      double, double[], int)
	 */
	public double doubleRule(double cell, double[] neighbors,
			int generation)
	{
		// Get the values from the More Properties panel. Only update slider
		// parameters (like slope and y-intercept) at the beginning
		// of each generation. This prevents some cells from seeing one
		// slope and other cells seeing another slope.
		if(currentGeneration != generation)
		{
			slope = ((Double) ((SpinnerNumberModel) slopeSpinner.getModel())
					.getNumber()).doubleValue();
			yIntercept = ((Double) ((SpinnerNumberModel) yInterceptSpinner
					.getModel()).getNumber()).doubleValue();
			
			currentGeneration = generation;
		}

		// average the cell and its neighbors
		double avg = cell;
		for(int i = 0; i < neighbors.length; i++)
		{
			avg += neighbors[i];
		}

		// take the average
		avg /= (neighbors.length + 1);

		double returnValue = slope * avg + yIntercept;

		returnValue = returnValue - Math.floor(returnValue);

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
		String[] folders = {RuleFolderNames.REAL_VALUED_FOLDER};

		return folders;
	}

	/**
	 * Gets the maximum permissible value for each element of the vector. Do not
	 * recommend using Double.MAX_VALUE unless your code very carefully checks
	 * for instances of Infinity. <br>
	 * This method is used by the RealVectorRuleTemplate class to properly
	 * construct a RealValuedVectorState.
	 * 
	 * @return The maximum permissible value.
	 * @see cellularAutomata.rules.templates.RealVectorRuleTemplate#getMaximumPermissibleValue()
	 */
	public double getMaximumPermissibleValue()
	{
		return MAX_VALUE;
	}

	/**
	 * Gets the minimum permissible value for each element of the vector. Do not
	 * recommend using Double.MIN_VALUE unless your code very carefully checks
	 * for instances of Infinity. <br>
	 * This method is used by the RealVectorRuleTemplate class to properly
	 * construct a RealValuedVectorState.
	 * 
	 * @return The minimum permissible value.
	 * @see cellularAutomata.rules.templates.RealVectorRuleTemplate#getMinimumPermissibleValue()
	 */
	public double getMinimumPermissibleValue()
	{
		return MIN_VALUE;
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
}
