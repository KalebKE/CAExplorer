/*
 StatusPanel -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cellularAutomata.CAConstants;
import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.rules.IntegerRule;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MinMaxBigIntPair;

/**
 * The panel that contains status messages.
 * 
 * @author David Bahr
 */
public class StatusPanel extends JPanel
{
	/**
	 * A string used as a prefix when displaying the current cursor position.
	 */
	private static final String CURSOR_POSITION_STRING = "Row, col: ";

	/**
	 * A string used as a prefix when displaying the value of the cell under the
	 * cursor.
	 */
	private static final String CURSOR_VALUE_STRING = "Value: ";

	/**
	 * String used for generation label display and for setting and retrieving
	 * the property associated with the generation label.
	 */
	public static final String GENERATION = "Generation";

	/**
	 * A string used as a prefix when displaying the current lattice.
	 */
	private static final String DIMENSIONS_STRING = "Dimension: ";

	/**
	 * A string used as a prefix when displaying the current lattice on a small
	 * frame.
	 */
	private static final String DIMENSIONS_STRING_SMALL = "";

	/**
	 * A string used as a prefix when displaying the current lattice.
	 */
	private static final String LATTICE_STRING = "Lattice: ";

	/**
	 * A string used as a prefix when displaying the current lattice on a small
	 * frame.
	 */
	private static final String LATTICE_STRING_SMALL = "";

	/**
	 * A string used as a prefix when displaying the current number of states.
	 */
	private static final String NUM_STATES_STRING = "Number of states: ";

	/**
	 * A string used as a prefix when displaying the current number of states on
	 * a small frame.
	 */
	private static final String NUM_STATES_STRING_SMALL = "Num states: ";

	/**
	 * A string used as a prefix when displaying the current rule number.
	 */
	private static final String RULE_NUMBER_STRING = "with rule number ";

	/**
	 * A string used as a prefix when displaying the current rule.
	 */
	private static final String RULE_STRING = "Rule: ";

	/**
	 * A string used as a prefix when displaying the current rule on a small
	 * frame.
	 */
	private static final String RULE_STRING_SMALL = "";

	/**
	 * A string used as a prefix when displaying the current running average.
	 */
	private static final String RUNNING_AVG_STRING = "Running avg: ";

	/**
	 * A string used as a prefix when displaying the current running average on
	 * a small frame.
	 */
	private static final String RUNNING_AVG_STRING_SMALL = "Run. avg: ";

	/**
	 * Display title for this status panel.
	 */
	public static final String STATUS_PANEL_TITLE = "Status";

	/**
	 * A tool tip for the status panel.
	 */
	public static final String TOOL_TIP = "<html><body>information about "
			+ "the currently active rule</body></html>";

	// The maximum length of a rule number that will be displayed
	// before scientific notation is used instead.
	private static final int MAX_RULENUMBER_LENGTH = 50;

	// if the frame gets smaller than this, then we will use smaller labels
	private static final int SMALL_FRAME_WIDTH = 1025;

	// The encompassing panel onto which this one will be added.
	private AllPanel outerPanel = null;

	// if the frame is too small, then we will shorten some of the labels
	private boolean smallFrame = false;

	// the row and column position of the cursor.
	private JLabel currentCursorPositionLabel = null;

	// the value of the cell under the cursor.
	private JLabel currentCursorValueLabel = null;

	// A label displaying the current dimensions
	private JLabel currentDimensionsLabel = null;

	// A label displaying the current lattice
	private JLabel currentLatticeLabel = null;

	// A label displaying the current number of states
	private JLabel currentNumberOfStatesLabel = null;

	// A label displaying the current rule
	private JLabel currentRuleLabel = null;

	// A label displaying the current rule
	private JLabel currentRunningAverageLabel = null;

	// A generation label
	private JLabel generationLabel = null;

	// The status label.
	private JLabel statusLabel = null;

	// string used for display -- may be changed
	private String dimensionsString = DIMENSIONS_STRING;

	// string used for display -- may be changed
	private String latticeString = LATTICE_STRING;

	// string used for display -- may be changed
	private String numStatesString = NUM_STATES_STRING;

	// string used for display -- may be changed
	private String ruleString = RULE_STRING;

	// string used for display -- may be changed
	private String runningAvgString = RUNNING_AVG_STRING;

	// initial status displayed on the panel
	private String status = "Stopped.";

	/**
	 * The panel containing the status labels.
	 * 
	 * @param outerPanel
	 *            The encompassing panel onto which this one will be added.
	 * @param borderColor
	 *            The color of this panel's border.
	 */
	public StatusPanel(AllPanel outerPanel, Color borderColor)
	{
		super();

		this.outerPanel = outerPanel;

		this.setOpaque(true);

		// is the display too small? (If so will want to shorten some labels.)
		if(CAFrame.PREFERRED_FRAME_WIDTH < SMALL_FRAME_WIDTH)
		{
			smallFrame = true;
		}

		// decide what to use as strings for the labels
		if(smallFrame)
		{
			ruleString = RULE_STRING_SMALL;
			latticeString = LATTICE_STRING_SMALL;
			dimensionsString = DIMENSIONS_STRING_SMALL;
			numStatesString = NUM_STATES_STRING_SMALL;
			runningAvgString = RUNNING_AVG_STRING_SMALL;
		}

		this.setToolTipText(TOOL_TIP);

		addComponents(borderColor);
	}

	/**
	 * Create and arrange a panel holding the CA status messages.
	 * 
	 * @return The panel holding the status messages such as the current rule,
	 *         current generation, my moniker, etc.
	 */
	private void addComponents(Color borderColor)
	{
		// create the labels
		generationLabel = new JLabel(GENERATION + " 0.");
		statusLabel = new JLabel(status);
		currentCursorPositionLabel = new JLabel(CURSOR_POSITION_STRING);
		currentCursorValueLabel = new JLabel(CURSOR_VALUE_STRING);

		String ruleSelection = (String) outerPanel.getRulePanel().getRuleTree()
				.getSelectedRuleName();
		String latticeSelection = (String) outerPanel.getPropertiesPanel()
				.getLatticeChooser().getSelectedItem();
		int numStates = Integer.parseInt(outerPanel.getPropertiesPanel()
				.getNumStatesField().getText());
		MinMaxBigIntPair minmaxRule = IntegerRule.getMinMaxRuleNumberAllowed(
				latticeSelection, ruleSelection, numStates);

		if(minmaxRule != null)
		{
			currentRuleLabel = new JLabel(ruleString
					+ ruleSelection
					+ ", "
					+ StatusPanel.RULE_NUMBER_STRING
					+ outerPanel.getRulePanel().getRuleNumberTextField()
							.getText());
		}
		else
		{
			currentRuleLabel = new JLabel(ruleString + ruleSelection);
		}

		// has many options, so farmed the RuleLabel out to a method
		setCurrentRuleLabel();

		currentLatticeLabel = new JLabel(latticeString + latticeSelection);

		currentDimensionsLabel = new JLabel(dimensionsString
				+ CurrentProperties.getInstance().getNumRows() + " by "
				+ CurrentProperties.getInstance().getNumColumns());

		// has many options, so farmed the NumberOfStatesLabel out to a method
		setCurrentNumberOfStatesLabel();

		currentRunningAverageLabel = new JLabel(runningAvgString
				+ outerPanel.getPropertiesPanel().getRunningAverageField()
						.getText());

		// create the copyright label
		JLabel moniker = new JLabel(CAConstants.COPYRIGHT);

		// set the fonts
		generationLabel.setFont(new Fonts().getBoldVerySmallFont());
		generationLabel.setForeground(Color.BLUE);
		statusLabel.setFont(new Fonts().getBoldVerySmallFont());
		statusLabel.setForeground(Color.BLUE);
		currentRuleLabel.setFont(new Fonts().getPlainVerySmallFont());
		currentLatticeLabel.setFont(new Fonts().getPlainVerySmallFont());
		currentDimensionsLabel.setFont(new Fonts().getPlainVerySmallFont());
		currentNumberOfStatesLabel.setFont(new Fonts().getPlainVerySmallFont());
		currentRunningAverageLabel.setFont(new Fonts().getPlainVerySmallFont());
		moniker.setFont(new Font(moniker.getFont().getFontName(), Font.ITALIC,
				9));
		currentCursorValueLabel.setFont(new Fonts().getPlainVerySmallFont());
		moniker.setFont(new Font(moniker.getFont().getFontName(), Font.ITALIC,
				9));
		currentCursorPositionLabel.setFont(new Fonts().getPlainVerySmallFont());
		moniker.setFont(new Font(moniker.getFont().getFontName(), Font.ITALIC,
				9));

		// create panels for each label.
		JPanel generationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		generationPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		generationPanel.add(generationLabel);

		JPanel statPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		statPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		int height = statusLabel.getPreferredSize().height;
		statusLabel.setMinimumSize(new Dimension(220, height));
		statusLabel.setPreferredSize(new Dimension(220, height));
		statPanel.add(statusLabel);

		JPanel rulePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		rulePanel.setBorder(BorderFactory.createLoweredBevelBorder());
		rulePanel.add(currentRuleLabel);

		JPanel latticePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		latticePanel.setBorder(BorderFactory.createLoweredBevelBorder());
		latticePanel.add(currentLatticeLabel);

		JPanel dimPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		dimPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		// dimPanel.setMinimumSize(new Dimension(80, height));
		// dimPanel.setPreferredSize(new Dimension(80, height));
		dimPanel.add(currentDimensionsLabel);

		JPanel statesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		statesPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		// statesPanel.setMinimumSize(new Dimension(60, height));
		// statesPanel.setPreferredSize(new Dimension(60, height));
		statesPanel.add(currentNumberOfStatesLabel);

		JPanel runAvgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		runAvgPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		// runAvgPanel.setMinimumSize(new Dimension(50, height));
		// runAvgPanel.setPreferredSize(new Dimension(50, height));
		runAvgPanel.add(currentRunningAverageLabel);

		JPanel cursorValuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		cursorValuePanel.setBorder(BorderFactory.createLoweredBevelBorder());
		cursorValuePanel.setMinimumSize(new Dimension(90, height));
		cursorValuePanel.setPreferredSize(new Dimension(90, height));
		cursorValuePanel.add(currentCursorValueLabel);

		JPanel cursorPositionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		cursorPositionPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		cursorPositionPanel.setMinimumSize(new Dimension(95, height));
		cursorPositionPanel.setPreferredSize(new Dimension(95, height));
		cursorPositionPanel.add(currentCursorPositionLabel);

		// add the labels and moniker to a GridBagLayout
		this.setLayout(new GridBagLayout());
		this.setBorder(BorderFactory.createRaisedBevelBorder());

		// generation label
		int col = 0;
		this.add(generationPanel, new GBC(col, 1).setSpan(1, 1).setFill(
				GBC.BOTH).setWeight(0.0, 0.0).setAnchor(GBC.WEST).setInsets(1));

		// status label
		col++;
		this.add(statPanel, new GBC(col, 1).setSpan(2, 1).setFill(GBC.BOTH)
				.setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		// space (incremented twice because above spans 2)
		col++;
		col++;
		JPanel spacePanel = new JPanel();
		spacePanel.add(new JLabel("    "));
		this.add(spacePanel, new GBC(col, 1).setSpan(1, 1).setFill(GBC.BOTH)
				.setWeight(10.0, 10.0).setAnchor(GBC.WEST).setInsets(1));

		// rule label
		col++;
		this.add(rulePanel, new GBC(col, 1).setSpan(1, 1).setFill(GBC.BOTH)
				.setWeight(2.0, 2.0).setAnchor(GBC.WEST).setInsets(1));

		// lattice label
		col++;
		this.add(latticePanel, new GBC(col, 1).setSpan(1, 1).setFill(GBC.BOTH)
				.setWeight(10.0, 10.0).setAnchor(GBC.WEST).setInsets(1));

		// dimensions label
		col++;
		this.add(dimPanel, new GBC(col, 1).setSpan(1, 1).setFill(GBC.BOTH)
				.setWeight(10.0, 10.0).setAnchor(GBC.WEST).setInsets(1));

		// number of states label
		col++;
		this.add(statesPanel, new GBC(col, 1).setSpan(1, 1).setFill(GBC.BOTH)
				.setWeight(10.0, 10.0).setAnchor(GBC.WEST).setInsets(1));

		// running average label
		col++;
		this.add(runAvgPanel, new GBC(col, 1).setSpan(1, 1).setFill(GBC.BOTH)
				.setWeight(10.0, 10.0).setAnchor(GBC.WEST).setInsets(1));

		// value of cell under the cursor label
		col++;
		this.add(cursorValuePanel, new GBC(col, 1).setSpan(1, 1).setFill(
				GBC.BOTH).setWeight(4.0, 4.0).setAnchor(GBC.WEST).setInsets(1));

		// cursor position label
		col++;
		this.add(cursorPositionPanel, new GBC(col, 1).setSpan(1, 1).setFill(
				GBC.BOTH).setWeight(0.0, 0.0).setAnchor(GBC.WEST).setInsets(1));

		// // space
		// col++;
		// this.add(new JLabel(" "), new GBC(col, 1).setSpan(1, 1).setFill(
		// GBC.BOTH).setWeight(10.0, 10.0).setAnchor(GBC.WEST).setInsets(1));
		//
		// // moniker label
		// col++;
		// this.add(moniker, new GBC(col, 1).setSpan(2, 1).setFill(GBC.BOTH)
		// .setWeight(0.0, 0.0).setAnchor(GBC.EAST).setInsets(1));
	}

	/**
	 * Get the label that displays the current CA dimensions.
	 * 
	 * @return The label displaying the current CA dimensions.
	 */
	public JLabel getCurrentDimensionsLabel()
	{
		return currentDimensionsLabel;
	}

	/**
	 * Get the label that displays the current CA lattice.
	 * 
	 * @return The label displaying the current CA lattice.
	 */
	public JLabel getCurrentLatticeLabel()
	{
		return currentLatticeLabel;
	}

	/**
	 * Get the label that displays the current CA number of states.
	 * 
	 * @return The label displaying the current CA number of states.
	 */
	public JLabel getCurrentNumberOfStatesLabel()
	{
		return currentNumberOfStatesLabel;
	}

	/**
	 * Get the label that displays the current CA rule.
	 * 
	 * @return The label displaying the current CA rule.
	 */
	public JLabel getCurrentRuleLabel()
	{
		return currentRuleLabel;
	}

	/**
	 * Get the label that displays the current CA running average.
	 * 
	 * @return The label displaying the current CA running average.
	 */
	public JLabel getCurrentRunningAverageLabel()
	{
		return currentRunningAverageLabel;
	}

	/**
	 * Get the status label on the status panel (for example, "Running" or
	 * Stopped").
	 * 
	 * @return The message to display on the label.
	 */
	public String getStatusLabel()
	{
		return statusLabel.getText();
	}

	/**
	 * Resets the labels by using the values in the properties. Does not reset
	 * the status or the generation label, but resets all other labels.
	 */
	public void resetLabels()
	{
		setCurrentLatticeLabel();
		setCurrentRuleLabel();
		setCurrentNumberOfStatesLabel();
		setCurrentRunningAverageLabel();
		setCurrentDimensionsLabel();
	}

	/**
	 * Sets the label showing the row and column position of the cursor.
	 * 
	 * @param cursorsRowColCoordinate
	 *            The row and column position of the cursor. May be null if the
	 *            cursor is not over a cell or if the row and column do not make
	 *            sense for the lattice (for example, a tree lattice).
	 */
	public void setCurrentCursorPositionLabel(Coordinate cursorsRowColCoordinate)
	{
		String positionLabel = CURSOR_POSITION_STRING;
		if(cursorsRowColCoordinate != null)
		{
			positionLabel += cursorsRowColCoordinate.getRow() + ", "
					+ cursorsRowColCoordinate.getColumn();
		}

		currentCursorPositionLabel.setText(positionLabel);
	}

	/**
	 * Sets the label showing the value of the cell under the cursor.
	 * 
	 * @param value
	 *            The text displayed as the value of the cell.
	 */
	public void setCurrentCursorValueLabel(String value)
	{
		String valueLabel = CURSOR_VALUE_STRING;
		if(value != null)
		{
			valueLabel += value;
		}

		currentCursorValueLabel.setText(valueLabel);
	}

	/**
	 * Set the current dimensions label on the status panel. The method chooses
	 * appropriate text and gets the dimensions from the properties panel.
	 */
	public void setCurrentDimensionsLabel()
	{
		currentDimensionsLabel.setText(dimensionsString
				+ CurrentProperties.getInstance().getNumRows() + " by "
				+ CurrentProperties.getInstance().getNumColumns());
	}

	/**
	 * Set the current lattice label on the status panel. The method chooses
	 * appropriate text and gets the lattice from the properties panel.
	 */
	public void setCurrentLatticeLabel()
	{
		currentLatticeLabel.setText(latticeString
				+ CurrentProperties.getInstance().getLatticeDisplayName());
	}

	/**
	 * Set the current number of states label on the status panel. The method
	 * chooses appropriate text and gets the number of states from the
	 * properties panel.
	 */
	public void setCurrentNumberOfStatesLabel()
	{
		setCurrentNumberOfStatesLabel(Integer.parseInt(outerPanel
				.getPropertiesPanel().getNumStatesField().getText()));
	}

	/**
	 * Set the current number of states label on the status panel. The method
	 * chooses appropriate text but uses the supplied number rather than the
	 * state number on the properties panel.
	 * 
	 * @param numStates
	 *            The number of states to display on the label.
	 */
	public void setCurrentNumberOfStatesLabel(int numStates)
	{
		String ruleSelection = (String) outerPanel.getRulePanel().getRuleTree()
				.getSelectedRuleName();
		String latticeSelection = (String) outerPanel.getPropertiesPanel()
				.getLatticeChooser().getSelectedItem();

		// create the correct text for the label
		String labelText = numStatesString;
		if(IntegerCellState.isCompatibleRule(ruleSelection)
				|| (IntegerRule.getMinMaxStatesAllowed(latticeSelection,
						ruleSelection) != null))
		{
			labelText += numStates;
		}
		else
		{
			labelText += "not applicable";
		}

		// now create the label
		if(currentNumberOfStatesLabel == null)
		{
			currentNumberOfStatesLabel = new JLabel(labelText);
		}
		else
		{
			currentNumberOfStatesLabel.setText(labelText);
		}
	}

	/**
	 * Set the current rule number label on the status panel. The method chooses
	 * appropriate text and gets the rule number from the properties panel.
	 */
	public void setCurrentRuleLabel()
	{
		setCurrentRuleLabel(new BigInteger(outerPanel.getRulePanel()
				.getRuleNumberTextField().getText()));
	}

	/**
	 * Set the rule number label on the status panel. The method chooses
	 * appropriate text but uses the supplied number rather than the number on
	 * the properties panel.
	 * 
	 * @param ruleNumber
	 *            The rule number to display on the label.
	 */
	public void setCurrentRuleLabel(BigInteger ruleNumber)
	{
		String ruleSelection = (String) outerPanel.getRulePanel().getRuleTree()
				.getSelectedRuleName();
		String latticeSelection = (String) outerPanel.getPropertiesPanel()
				.getLatticeChooser().getSelectedItem();
		int numStates = Integer.parseInt(outerPanel.getPropertiesPanel()
				.getNumStatesField().getText());
		MinMaxBigIntPair minmaxRule = IntegerRule.getMinMaxRuleNumberAllowed(
				latticeSelection, ruleSelection, numStates);

		// The pattern used to display the rule number.
		NumberFormat formatter = new DecimalFormat(
				CAConstants.SCIENTIFIC_NOTATION_PATTERN);

		String rule = "" + ruleNumber;
		if(rule.length() > MAX_RULENUMBER_LENGTH)
		{
			rule = formatter.format(ruleNumber);
		}

		// create the text for the label
		String labeltext = ruleString + ruleSelection;
		if(minmaxRule != null)
		{
			labeltext += ", " + StatusPanel.RULE_NUMBER_STRING + rule;
		}
		else
		{
			labeltext = ruleString + ruleSelection;
		}

		// now create the label
		if(currentRuleLabel == null)
		{
			currentRuleLabel = new JLabel(labeltext);
		}
		else
		{
			currentRuleLabel.setText(labeltext);
		}
	}

	/**
	 * Set the current running average label on the status panel. The method
	 * chooses appropriate text and gets the running average from the properties
	 * panel.
	 */
	public void setCurrentRunningAverageLabel()
	{
		currentRunningAverageLabel.setText(runningAvgString
				+ outerPanel.getPropertiesPanel().getRunningAverageField()
						.getText());
	}

	/**
	 * Set the generation label on the status panel (in other words, the number
	 * of the generation).
	 * 
	 * @param message
	 *            The message to display on the label.
	 */
	public void setGenerationLabel(String message)
	{
		generationLabel.setText(message);
	}

	/**
	 * Set the status label on the status panel (for example, "Running" or
	 * Stopped").
	 * 
	 * @param status
	 *            The message to display on the label.
	 */
	public void setStatusLabel(String status)
	{
		statusLabel.setText(status);
	}
}