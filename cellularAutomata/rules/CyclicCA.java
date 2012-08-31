/*
 CyclicCA -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2006  David B. Bahr (http://academic.regis.edu/dbahr/)

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
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.view.TriangleHexagonCellStateView;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.rules.templates.CyclicCATemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MinMaxBigIntPair;
import cellularAutomata.util.MultilineLabel;

/**
 * Create a cyclic CA, inspired/discovered by David Griffeath.
 * 
 * @author David Bahr
 */
public class CyclicCA extends CyclicCATemplate
{
	// an info message for the "More Properties" panel
	private static final String INFO_MESSAGE = "Cells in a Cyclic CA always update their current "
			+ "state from n to n+1 (modulo the number of states).  For example, if there are "
			+ "four states, then a cell that is initially in state 0 will perpetually update "
			+ "as 0, 1, 2, 3, 0, 1, 2, 3, ... \n\n "
			+ "An update only happens when the cell is surrounded by M neighbors that are already "
			+ "in the next state (n+1).  M is called the trigger number. \n\n"
			+ "Try selecting a trigger number while the CA is running.  This will let you tune "
			+ "the CA to find a trigger with interesting properties.";

	// a display name for this class
	private static final String RULE_NAME = "Cyclic CA";

	private static final String TRIGGER_TIP = "<html>Choose the number of neighbors that<br>"
			+ "must have state i+1 in order for the current <br>"
			+ "cell to update its state from i to i+1.</html>";

	// The number of neighbors for each cell.
	private static int numberOfNeighbors = 0;

	// the number of neighbors that must have the next state value in order for
	// the cell to increment
	private static volatile int triggerNumber = 3;

	// fonts for display
	private Fonts fonts = new Fonts();

	// the label for the trigger spinner
	private static JLabel triggerLabel = null;

	// the JPanel that is returned by getAdditionalPropertiesPanel()
	private static JPanel panel = null;

	// selects the trigger number
	private static JSpinner triggerSpinner = null;

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, use large lattices with large neighborhoods like the "
			+ "\"square (24 neighbor)\" lattice and a 100 by 100 grid.  Also start with a "
			+ "random initial state, generally 65% or higher.  A larger percentage works better "
			+ "for larger numbers of states. Larger numbers of states typically give more "
			+ "interesting behaviors.  "
			+ "<p>"
			+ "Using the \"More Properties\" panel, choose a trigger number that is much "
			+ "smaller than the number of neighbors on the lattice. Try selecting a trigger "
			+ "number while the CA is running.  This will let you tune "
			+ "the CA to find a trigger with interesting properties."
			+ "<p>"
			+ "In many cases, the "
			+ "simulation will need to run for many generations to see the evolution of "
			+ "interesting shapes. However, many interesting cyclic CA do not fall into "
			+ "the general guidelines given above."
			+ "<p>"
			+ "<b>For example, try the following</b>: (1) a square (24 neighbor) lattice with "
			+ "3 states, a 70% random initial state, and a trigger number of 6;  (2) a square "
			+ "(8 neighbor) lattice with 5 states, a 65% random initial state, and a trigger "
			+ "number of 3; 2) a square (4 neighbor) lattice with 10 states, a 80% random "
			+ "initial state, and a trigger number of 1;  (4) a triangular (12 neighbor) "
			+ "lattice with 10 states, a 40% random initial state, and a trigger number of 1; "
			+ "(5) a hexagonal (6 neighbor) lattice with 5 states, a 40% random initial state, "
			+ "and a trigger number of 1." + leftClickInstructions
			+ rightClickInstructions + "</body></html>";

	// a tooltip description for this class
	private String TOOLTIP = "<html> <body><b>" + RULE_NAME
			+ ".</b> Produces moving spirals and squares frequently used to "
			+ "model wave-like behavior.</body></html>";

	/**
	 * Create a cyclic CA.
	 * <p>
	 * When calling the parent constructor, the minimalOrLazyInitialization
	 * parameter must be included as shown. The boolean is intended to indicate
	 * when the constructor should build a rule with as small a footprint as
	 * possible. In order to load rules by reflection, the application must
	 * query this class for information like the display name, tooltip
	 * description, etc. At these times it makes no sense to build the complete
	 * rule which may have a large footprint in memory.
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
	public CyclicCA(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);
		// super.setView(new FilledView(properties));

		// get the number of neighbors
		if(!minimalOrLazyInitialization)
		{
			setTriggerNumberAtStartUp();
		}
	}

	/**
	 * Creates the additional properties display panel.
	 * 
	 * @return A panel that will be displayed as the "additional properties
	 *         panel".
	 */
	private JPanel createDisplayPanel()
	{
		// the panel on which we add the controls
		JPanel innerPanel = new JPanel();

		GridBagLayout layout = new GridBagLayout();
		innerPanel.setLayout(layout);

		// info message
		JPanel infoPanel = createMessagePanel();

		// label for the spinner
		setTriggerNumberAtStartUp();
		String description = "Choose a trigger between 1 and "
				+ numberOfNeighbors + ".";
		triggerLabel = new JLabel(description);

		triggerSpinner = new JSpinner();
		SpinnerNumberModel triggerModel = new SpinnerNumberModel(triggerNumber,
				1, numberOfNeighbors, 1);
		triggerSpinner = new JSpinner(triggerModel);
		triggerSpinner.setToolTipText(TRIGGER_TIP);
		triggerSpinner.addChangeListener(new TriggerListener());

		// info message
		int row = 0;
		innerPanel.add(infoPanel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		// space
		row++;
		innerPanel.add(new JLabel(" "), new GBC(1, row).setSpan(1, 1).setFill(
				GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
				.setInsets(1));

		// trigger label
		row++;
		innerPanel.add(triggerLabel, new GBC(1, row).setSpan(1, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
				.setInsets(1));

		// triggerSpinner
		row++;
		innerPanel.add(triggerSpinner, new GBC(1, row).setSpan(1, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
				.setInsets(1));

		// space
		row++;
		innerPanel.add(new JLabel(" "), new GBC(1, row).setSpan(1, 1).setFill(
				GBC.HORIZONTAL).setWeight(100.0, 100.0).setAnchor(GBC.CENTER)
				.setInsets(1));

		return innerPanel;
	}

	/**
	 * Creates a panel that displays messages.
	 * 
	 * @return A panel containing messages.
	 */
	private JPanel createMessagePanel()
	{
		// a "grab their attention" panel
		AttentionPanel attentionPanel = new AttentionPanel("Set Trigger");

		MultilineLabel messageLabel = new MultilineLabel(INFO_MESSAGE);
		messageLabel.setFont(fonts.getMorePropertiesDescriptionFont());
		messageLabel.setMargin(new Insets(2, 6, 2, 2));

		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.add(attentionPanel, BorderLayout.NORTH);
		messagePanel.add(messageLabel, BorderLayout.CENTER);

		return messagePanel;
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
	 * Reads the trigger number from the spinner.
	 * 
	 * @return The trigger number.
	 * @throws NumberFormatException
	 */
	private int getTriggerNumberFromSpinner() throws NumberFormatException
	{
		// read the column number
		Integer triggerInteger = (Integer) ((SpinnerNumberModel) triggerSpinner
				.getModel()).getNumber();

		return triggerInteger.intValue();
	}

	/**
	 * Reacts to any actions on the JPanel GUI created in
	 * getAdditionalPropertiesPanel().
	 */
	public void actionPerformed(ActionEvent e)
	{
	}

	/**
	 * Gets a JPanel that may request specific input information that the rule
	 * needs to operate correctly. Should be overridden by child classes that
	 * desire to input any specific information. Don't forget to set a size for
	 * the JPanel. <br>
	 * Note that if returns null, then the panel is not displayed by the current
	 * version of the CA ControlPanel class. This null behavior is the default.
	 * 
	 * @return A JPanel requesting specific input information that the rule
	 *         needs to operate correctly. May be null.
	 */
	public JPanel getAdditionalPropertiesPanel()
	{
		int numNeighbors = ReflectionTool
				.getNumberOfNeighborsFromLatticeDescription(CurrentProperties
						.getInstance().getLatticeDisplayName());

		// reset if the lattice has changed
		if(panel != null) // && numberOfNeighbors != numNeighbors)
		{
			synchronized(this)
			{
				// the lattice has changed, so reset the spinner and spinner
				// label on the additionalProperties panel
				numberOfNeighbors = numNeighbors;

				// in EZ mode, set to 3
				if(CurrentProperties.getInstance().isFacadeOn())
				{
					// don't reset if the user has adjusted
					if(!CurrentProperties.getInstance().getRuleClassName()
							.contains("CyclicCA"))
					{
						triggerNumber = 3;
					}
					numberOfNeighbors = 24;
				}

				if(triggerNumber > numberOfNeighbors)
				{
					// the trigger number has to be less than or equal to the
					// number
					// of neighbors.
					triggerNumber = numberOfNeighbors;
				}

				SpinnerNumberModel triggerModel = new SpinnerNumberModel(
						triggerNumber, 1, numberOfNeighbors, 1);
				if(triggerSpinner != null)
				{
					triggerSpinner.setModel(triggerModel);
				}

				String description = "Choose a trigger between 1 and "
						+ numberOfNeighbors + ".";
				if(triggerLabel != null)
				{
					triggerLabel.setText(description);
				}
			}
		}
		else
		{
			// beware parallel processing which might try to reset
			// simultaneously
			synchronized(this)
			{
				// the lattice has changed, so reset the spinner and spinner
				// label on the additionalProperties panel
				numberOfNeighbors = numNeighbors;

				// in EZ mode, set to 3
				if(CurrentProperties.getInstance().isFacadeOn())
				{
					// don't reset if the user has adjusted
					if(!CurrentProperties.getInstance().getRuleClassName()
							.contains("CyclicCA"))
					{
						triggerNumber = 3;
					}
					numberOfNeighbors = 24;
				}

				panel = createDisplayPanel();
			}
		}

		return panel;
	}

	/**
	 * A cell updates it state from i to i+1, but this only happens when there
	 * are M neighbors that already have value i+1. This method gets the trigger
	 * number M.
	 * <p>
	 * Note that this method is called at every time step by the
	 * CyclicCATemplate class. No other method in this class is called at every
	 * time step.
	 * 
	 * @return The number of neighbors (with value i+1) necessary to trigger an
	 *         update to the next state value (i to i+1).
	 */
	protected int getTriggerNumber()
	{	
		// did the lattice change?
		int numNeighbors = ReflectionTool
				.getNumberOfNeighborsFromLatticeDescription(CurrentProperties
						.getInstance().getLatticeDisplayName());

		if(numNeighbors == -1)
		{
			triggerNumber = 1;
		}
		else if(triggerNumber > numNeighbors)
		{
			// the trigger number has to be less than or equal to the number
			// of neighbors.
			triggerNumber = numNeighbors;
		}

		// reset if the lattice has changed
		if(numberOfNeighbors != numNeighbors)
		{
			synchronized(this)
			{
				// the lattice has changed, so reset the spinner and spinner
				// label on the additionalProperties panel
				numberOfNeighbors = numNeighbors;

				if(triggerNumber > numberOfNeighbors)
				{
					// the trigger number has to be less than or equal to the
					// number
					// of neighbors.
					triggerNumber = numberOfNeighbors;
				}

				SpinnerNumberModel triggerModel = new SpinnerNumberModel(
						triggerNumber, 1, numberOfNeighbors, 1);
				if(triggerSpinner != null)
				{
					triggerSpinner.setModel(triggerModel);
				}

				String description = "Choose a trigger between 1 and "
						+ numberOfNeighbors + ".";
				if(triggerLabel != null)
				{
					triggerLabel.setText(description);
				}
			}
		}

		return triggerNumber;
	}

	/**
	 * A list of lattices with which this Rule will work. This rule won't work
	 * with lattices that have a variable or unknown number of neighbors.
	 * 
	 * @return A list of lattices compatible with this Rule (returns the display
	 *         names for the lattices). Returns null if compatible with all
	 *         lattices.
	 */
	public String[] getCompatibleLattices()
	{
		// this rule won't work with lattices that have a variable or unknown
		// number of neighbors.
		return Rule.allLatticeNamesWithConstantNumbersOfNeighbors;
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
		String[] folders = {RuleFolderNames.CYCLIC_RULES_FOLDER,
				RuleFolderNames.PRETTY_FOLDER};

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
	 * Gets a pair of numbers for the minimum and maximum allowable rule numbers
	 * for the specified lattice. When this method returns null (the default
	 * value), the "rule number" display field is disabled. Sub-classes should
	 * override this method to enable the rule number display field.
	 * 
	 * @param latticeDescription
	 *            The display name of the lattice for which a min and max rule
	 *            numbers will be specified.
	 * @param numStates
	 *            The number of states allowed for a cell on the lattice.
	 * @return A pair of numbers for the minimum and maximum allowable rule
	 *         numbers. May be null if the concept of a minimum and maximum does
	 *         not make sense for this rule. Null is the default value.
	 */
	protected MinMaxBigIntPair getMinMaxAllowedRuleNumbers(
			String latticeDescription, int numStates)
	{
		return null;
	}

	/**
	 * Set the trigger number based on the number of neighbors.
	 */
	private void setTriggerNumberAtStartUp()
	{
		int numNeighbors = ReflectionTool
				.getNumberOfNeighborsFromLatticeDescription(CurrentProperties
						.getInstance().getLatticeDisplayName());

		// reset if the lattice has changed
		if(panel != null) // && numberOfNeighbors != numNeighbors)
		{
			// beware parallel processing which might try to reset the
			// additional properties panel simultaneously
			synchronized(this)
			{
				// the lattice has changed, so reset the spinner and spinner
				// label on the additionalProperties panel
				numberOfNeighbors = numNeighbors;

				// in EZ mode, set to 3
				if(CurrentProperties.getInstance().isFacadeOn())
				{
					// don't reset if the user has adjusted
					if(!CurrentProperties.getInstance().getRuleClassName()
							.contains("CyclicCA"))
					{
						triggerNumber = 3;
					}
					numberOfNeighbors = 24;
				}

				if(triggerNumber > numberOfNeighbors)
				{
					// the trigger number has to be less than or equal to the
					// number
					// of neighbors.
					triggerNumber = numberOfNeighbors;
				}

				SpinnerNumberModel triggerModel = new SpinnerNumberModel(
						triggerNumber, 1, numberOfNeighbors, 1);
				if(triggerSpinner != null)
				{
					triggerSpinner.setModel(triggerModel);
				}

				String description = "Choose a trigger between 1 and "
						+ numberOfNeighbors + ".";
				if(triggerLabel != null)
				{
					triggerLabel.setText(description);
				}
			}
		}
		else
		{
			// beware parallel processing which might try to reset
			// simultaneously
			synchronized(this)
			{
				// the lattice has changed, so reset the spinner and spinner
				// label on the additionalProperties panel
				numberOfNeighbors = numNeighbors;

				// in EZ mode, set to 3
				if(CurrentProperties.getInstance().isFacadeOn())
				{
					// don't reset if the user has adjusted
					if(!CurrentProperties.getInstance().getRuleClassName()
							.contains("CyclicCA"))
					{
						triggerNumber = 3;
					}
					numberOfNeighbors = 24;
				}
			}
		}

		if(numNeighbors == -1)
		{
			triggerNumber = 1;
		}
		else if(triggerNumber > numNeighbors)
		{
			// the trigger number has to be less than or equal to the number
			// of neighbors.
			triggerNumber = numNeighbors;
		}
	}

	/**
	 * Finds the value of the state that will be displayed in the "Number of
	 * States" text field.
	 * 
	 * @param latticeDescription
	 *            The display name of the lattice for which the state value will
	 *            be determined.
	 * @param ruleDescription
	 *            The display name of the rule for which the state value will be
	 *            determined.
	 * @return The state value should be displayed for the "Number of States"
	 *         text field. When null, will display the value currently in the
	 *         text field.
	 */
	protected Integer stateValueToDisplay(String latticeDescription)
	{
		// fixes the state value at NUMBER_OF_STATES
		return null; // new Integer(NUMBER_OF_STATES);
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
	 * A view that uses squares, hexagons, and triangles rather than just
	 * squares. Makes the hexagonal and triangular lattices look better.
	 * 
	 * @author David Bahr
	 */
	private class FilledView extends TriangleHexagonCellStateView
	{
		/**
		 * Create a view for the Spirals that uses appropriate shading for
		 * colors.
		 */
		public FilledView()
		{
			super();
		}

		/**
		 * Creates a display color based on the value of the cell.
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
			// the number of colors
			int numPossibleColors = CurrentProperties.getInstance()
					.getNumStates();

			// the value of the cell.
			int intValue = ((IntegerCellState) state).getState();

			Color filledColor = ColorScheme.FILLED_COLOR;
			Color emptyColor = ColorScheme.EMPTY_COLOR;

			double redDiff = filledColor.getRed() - emptyColor.getRed();
			double greenDiff = filledColor.getGreen() - emptyColor.getGreen();
			double blueDiff = filledColor.getBlue() - emptyColor.getBlue();

			double redDelta = redDiff / (numPossibleColors - 1);
			double greenDelta = greenDiff / (numPossibleColors - 1);
			double blueDelta = blueDiff / (numPossibleColors - 1);

			int red = (int) Math.floor(emptyColor.getRed()
					+ (intValue * redDelta));
			int green = (int) Math.floor(emptyColor.getGreen()
					+ (intValue * greenDelta));
			int blue = (int) Math.floor(emptyColor.getBlue()
					+ (intValue * blueDelta));

			return new Color(red, green, blue);
		}
	}

	/**
	 * Listens for changes to the trigger spinner.
	 */
	private class TriggerListener implements ChangeListener
	{
		/**
		 * Listens for changes to the trigger spinner.
		 * 
		 * @param e
		 */
		public void stateChanged(ChangeEvent e)
		{
			triggerNumber = getTriggerNumberFromSpinner();
		}
	}
}
