/*
 AllPanelController -- a class within the Cellular Automaton Explorer. 
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

import java.awt.Dimension;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Random;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JSpinner;

import cellularAutomata.CAConstants;
import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.analysis.Analysis;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.MooreRadiusOneDimLattice;
import cellularAutomata.lattice.MooreRadiusTwoDimLattice;
import cellularAutomata.lattice.OneDimensionalLattice;
import cellularAutomata.lattice.RandomGaussianLattice;
import cellularAutomata.lattice.TwoDimensionalLattice;
import cellularAutomata.lattice.VonNeumannRadiusLattice;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.reflection.RuleHash;
import cellularAutomata.rules.IntegerRule;
import cellularAutomata.rules.Rule;
import cellularAutomata.rules.WolframRuleNumber;
import cellularAutomata.util.MinMaxBigIntPair;
import cellularAutomata.util.MinMaxIntPair;
import cellularAutomata.util.ToolTipComboBox;
import cellularAutomata.util.graphics.AnalysisButtonTabComponent;
import cellularAutomata.util.graphics.PulsatingJTextField;
import cellularAutomata.util.math.RandomSingleton;

/**
 * Controls interactions between components on the panels. The Panels hold the
 * components (StartPanel, Status Panel, etcetera), the AllPanelListener hears
 * events, and the listener calls methods in this class. This class calls the
 * individual panels to change their appearance. Essentially a Mediator design
 * pattern.
 * 
 * @author David Bahr
 */
public class AllPanelController
{
	/**
	 * The message displayed while the simulation is running.
	 */
	public static String RUNNING_MESSAGE = "Running.";

	/**
	 * The message displayed while the simulation is stopped.
	 */
	public static String STOPPED_MESSAGE = "Stopped.";

	/**
	 * The max number of allowed bits in a randomly generated rule number (is
	 * limited by the practical limits of a JTextField and the properties file
	 * line length).
	 */
	public static final int MAX_RANDOM_NUM_BITS = 2000;

	// The number of times that pulsating components will pulse.
	private static final int NUMBER_OF_PULSES = 4;

	// The maximum length of a rule number that will be displayed in a tooltip
	// before scientific notation is used instead.
	private static final int TOOLTIP_MAX_RULENUMBER_LENGTH = 150;

	// the GUI panels
	private AllPanel allPanels = null;

	// the initial states panel
	private InitialStatesPanel initialStatesPanel = null;

	// the properties panel
	private PropertiesPanel propertiesPanel = null;

	// the rule panel
	private RulePanel rulePanel = null;

	/**
	 * Create the controller that handles changes in appearance of components on
	 * the CA panels.
	 */
	public AllPanelController(AllPanel allPanels)
	{
		this.allPanels = allPanels;
		this.propertiesPanel = allPanels.getPropertiesPanel();
		this.rulePanel = allPanels.getRulePanel();
		this.initialStatesPanel = allPanels.getInitialStatesPanel();

		// do this in constructor because the panels were just built, and these
		// may need disabling
		disableRadiusField();
		disableStandardDeviationField();
		disableRuleNumberField(true); // true because this is the CA setup
		disableNumStatesField(true); // true because this is the CA setup
		disableRunningAverageField(true); // true because this is the CA setup

		// needs to be done after the number of states and the default rule and
		// lattice have been set.
		updateRuleNumberTip();
	}

	/**
	 * Adds the JPanel as a tab.
	 * 
	 * @param analysis
	 *            The analysis being displayed on the tabbed pane.
	 * @param panel
	 *            The panel added to the tabbed pane.
	 * @param title
	 *            The title of the tab.
	 * @param toolTipDescription
	 *            The tool tip for the tab (may be null).
	 */
	public void addTab(Analysis analysis, JPanel panel, String title,
			String toolTipDescription)
	{
		// set a preferred size
		panel.setPreferredSize(allPanels.getTabbedPane().getPreferredSize());

		allPanels.getTabbedPane()
				.addTab(title, null, panel, toolTipDescription);

		// add an x button that allows the tab to be closed
		int indexOfThisTab = allPanels.getTabbedPane().indexOfTab(title);
		allPanels.getTabbedPane().setTabComponentAt(
				indexOfThisTab,
				new AnalysisButtonTabComponent(allPanels.getTabbedPane(),
						analysis));

		// resize the tabbed pane (it needs to resize because the number of rows
		// of tabs may have changed).
		Dimension tabbedPaneSize = allPanels.getTabbedPane().getSize();
		Dimension newSize = new Dimension(tabbedPaneSize.width, allPanels
				.getCAFrame().getScrollPane().getHeight());
		allPanels.getTabbedPane().setPreferredSize(newSize);
		allPanels.getTabbedPane().setMinimumSize(newSize);
		allPanels.getTabbedPane().setMaximumSize(newSize);
	}

	/**
	 * Selects a random rule number for the rule number text field.
	 */
	public void chooseRandomRuleNumber()
	{
		JComboBox latticeChooser = propertiesPanel.getLatticeChooser();
		JTextField numStates = propertiesPanel.getNumStatesField();

		// get the lattice choice
		String latticeChoice = (String) latticeChooser.getSelectedItem();

		// get the rule name
		String ruleName = rulePanel.getRuleTree().getSelectedRuleName();

		// get the number of states
		int minState = IntegerRule.MIN_NUM_STATES;
		int maxState = IntegerRule.MAX_NUM_STATES;
		MinMaxIntPair minmax = IntegerRule.getMinMaxStatesAllowed(
				latticeChoice, ruleName);
		if(minmax != null)
		{
			// assign values specified by the rule, but make sure they don't
			// exceed the maximum allowed
			if(minmax.min >= minState)
			{
				minState = minmax.min;
			}
			if(minmax.max <= maxState)
			{
				maxState = minmax.max;
			}
		}
		int numOfStates = minState;
		try
		{
			if(IntegerCellState.isCompatibleRule(ruleName))
			{
				// read number of states
				String numStatesString = numStates.getText();
				numOfStates = Integer.parseInt(numStatesString);
				if(!isNumberOfStatesOk(numOfStates))
				{
					throw new NumberFormatException();
				}

				// also have to check if the rule choice is lambda, because that
				// restricts the number of allowed states
				if((numOfStates < minState) || (numOfStates > maxState))
				{
					throw new NumberFormatException();
				}
			}
		}
		catch(Exception error)
		{
			// then choose the minimum allowed number of states
			numOfStates = minState;
		}

		// so get the min and max allowed rules
		MinMaxBigIntPair minmaxRule = IntegerRule.getMinMaxRuleNumberAllowed(
				latticeChoice, ruleName, numOfStates);
		BigInteger max = minmaxRule.max;
		BigInteger min = minmaxRule.min;

		// now get a random number between the min and the max,
		// but place some realistic limits on the max
		int numBits = max.bitLength();

		if(numBits > MAX_RANDOM_NUM_BITS)
		{
			numBits = MAX_RANDOM_NUM_BITS;
		}

		Random rnd = RandomSingleton.getInstance();

		// I would prefer to use the following line, but it seems to be biased
		// towards very large numbers with numBits. i.e., the high bit seems to
		// be set disproportionately often. (See illustration commented out
		// below.)
		//
		// DOESN'T WORK, not uniform:
		// BigInteger random = new BigInteger(numBits, rnd);
		//
		// But instead I use this, which is biased toward low numbers.
		// Psychologically, that's what the user expects anyway -- they want to
		// see lots of small numbers that they can recognize.
		BigInteger random = null;
		if(numBits < 10)
		{
			random = new BigInteger(numBits, rnd);
		}
		else
		{
			random = new BigInteger(rnd.nextInt(numBits + 1), rnd);
		}

		// but this random number can be too big or too small, so "scale" it
		while(random.compareTo(max) > 0 || random.compareTo(min) < 0)
		{
			// ok, this is a cheat. Scaling is annoying with BigIntegers
			random = new BigInteger(numBits, rnd);
		}

		// The following seems to illustrate that the random BigIntegers are not
		// uniformly distributed on 0 to 2^100
		// for(int i = 0; i < 100; i++)
		// {
		// BigInteger r = new BigInteger(100, rnd);
		//
		// // The pattern used to display the rule number.
		// NumberFormat formatter = new DecimalFormat(
		// CAConstants.SCIENTIFIC_NOTATION_PATTERN);
		//
		// String rule = "" + r;
		// if(rule.length() > 25)
		// {
		// rule = formatter.format(r);
		// }
		// System.out.println("AllPanelCont: random # = " + rule + ", and "
		// + r);
		// }

		// now set the text field
		rulePanel.getRuleNumberTextField().setText(random.toString());
	}

	/**
	 * Disables the number of states field as necessary, depending on whether
	 * certain rules are selected or not.
	 * 
	 * @param updateProperties
	 *            If true the method will update the NUMBER_OF_STATES in the CA
	 *            properties and display the result on the status panel. (Will
	 *            probably only be true when called from the constructor of this
	 *            class. At other times, we'll usually want to make sure that
	 *            the submit button has been pressed before making these
	 *            changes. This method is called whenever the user selects a new
	 *            rule, even if they have not submitted the rule.)
	 */
	public void disableNumStatesField(boolean updateProperties)
	{
		JTextField numStates = propertiesPanel.getNumStatesField();

		// disable the number of states field as appropriate
		if(numStates != null)
		{
			String ruleSelection = (String) rulePanel.getRuleTree()
					.getSelectedRuleName();

			String latticeSelection = (String) propertiesPanel
					.getLatticeChooser().getSelectedItem();

			if(ruleSelection != null)
			{
				if(IntegerCellState.isCompatibleRule(ruleSelection)
						&& (IntegerRule.getMinMaxStatesAllowed(
								latticeSelection, ruleSelection) != null))
				{
					numStates.setEnabled(true);

					// set the display value
					Integer value = IntegerRule.getStateValueToDisplay(
							latticeSelection, ruleSelection);
					if(value != null)
					{
						numStates.setText(value.toString());

						// if this is called from the constructor then reset the
						// properties and redisplay in the status panel
						if(updateProperties)
						{
							CurrentProperties.getInstance().setNumStates(value);

							allPanels.getStatusPanel()
									.setCurrentNumberOfStatesLabel(value);
							// allPanels.getStatusPanel()
							// .getCurrentNumberOfStatesLabel().setText(
							// StatusPanel.NUM_STATES_STRING
							// + value.toString());
						}
					}
				}
				else
				{
					// set the display value
					Integer value = IntegerRule.getStateValueToDisplay(
							latticeSelection, ruleSelection);
					if(value != null)
					{
						numStates.setText(value.toString());
					}

					numStates.setEnabled(false);

					// if this is called from the constructor then reset the
					// properties but only redisplay in the status panel if
					// it is a rule that uses the number of states
					if(updateProperties && value != null)
					{
						if(IntegerCellState.isCompatibleRule(ruleSelection))
						{
							CurrentProperties.getInstance().setNumStates(value);

							allPanels.getStatusPanel()
									.setCurrentNumberOfStatesLabel(value);
							// allPanels.getStatusPanel()
							// .getCurrentNumberOfStatesLabel().setText(
							// StatusPanel.NUM_STATES_STRING
							// + value.toString());
						}
						else
						{
							CurrentProperties.getInstance().setNumStates(value);
						}
					}

				}
			}
		}
	}

	/**
	 * Disables the radius field if the Moore lattice is not selected.
	 */
	public void disableRadiusField()
	{
		PulsatingJTextField radius = propertiesPanel.getRadiusField();
		JComboBox latticeChooser = propertiesPanel.getLatticeChooser();

		// disable the radius field as appropriate
		if(radius != null && latticeChooser != null)
		{
			String latticeChoice = (String) latticeChooser.getSelectedItem();

			if((latticeChoice != null)
					&& (latticeChoice
							.equals(MooreRadiusOneDimLattice.DISPLAY_NAME)
							|| latticeChoice
									.equals(MooreRadiusTwoDimLattice.DISPLAY_NAME) || latticeChoice
							.equals(VonNeumannRadiusLattice.DISPLAY_NAME)))
			{
				radius.setEnabled(true);
				radius.startPulsing(NUMBER_OF_PULSES);
			}
			else
			{
				radius.setEnabled(false);
				radius.stopPulsing();
			}
		}
	}

	/**
	 * Disables the rule number field as necessary, depending on whether the
	 * ruleNumber class is selected or not.
	 * 
	 * @param updateProperties
	 *            If true the method will update the RULE_NUMBER in the CA
	 *            properties and display the result on the status panel. (Will
	 *            probably only be true when called from the constructor of this
	 *            class or RulePanel's reset(). At other times, we'll usually
	 *            want to make sure that the submit button has been pressed
	 *            before making these changes. This method is called whenever
	 *            the user selects a new rule, even if they have not submitted
	 *            the rule.)
	 */
	public void disableRuleNumberField(boolean updateProperties)
	{
		JComboBox latticeChooser = propertiesPanel.getLatticeChooser();
		RuleTree ruleTree = rulePanel.getRuleTree();
		PulsatingJTextField ruleNum = rulePanel.getRuleNumberTextField();
		JButton ruleNumRandomButton = rulePanel.getRuleNumberRandomButton();
		JTextField numStatesField = propertiesPanel.getNumStatesField();

		// disable the number chooser as appropriate
		if(ruleNum != null && latticeChooser != null && ruleTree != null)
		{
			String latticeSelection = (String) latticeChooser.getSelectedItem();
			String ruleSelection = (String) ruleTree.getSelectedRuleName();

			// get the number of states
			int numStates = 2;
			try
			{
				// make sure they didn't enter something wrong
				numStates = Integer.parseInt(numStatesField.getText());
			}
			catch(Exception e)
			{
				// do nothing
			}

			if(ruleSelection != null)
			{
				MinMaxBigIntPair minmaxRule = IntegerRule
						.getMinMaxRuleNumberAllowed(latticeSelection,
								ruleSelection, numStates);

				// if it's the same number for the min and max, or if it is
				// null, then disables the rule number
				if(minmaxRule != null
						&& (!minmaxRule.min.equals(minmaxRule.max)))
				{
					ruleNum.setEnabled(true);
					ruleNum.startPulsing(NUMBER_OF_PULSES);
				}
				else
				{
					ruleNum.setEnabled(false);
					ruleNum.stopPulsing();
				}

				// set the display value
				BigInteger value = IntegerRule.getRuleNumberToDisplay(
						latticeSelection, ruleSelection);
				if(value != null)
				{
					ruleNum.setText(value.toString());

					// if this is called from the constructor then reset the
					// properties and redisplay in the status panel
					if(updateProperties)
					{
						CurrentProperties.getInstance().setRuleNumber(value);

						// the current label
						String ruleString = allPanels.getStatusPanel()
								.getCurrentRuleLabel().getText();

						// now parse out the rule number
						ruleString = ruleString.substring(0, ruleString
								.lastIndexOf(" ") + 1);

						// now add back the new rule number
						allPanels.getStatusPanel().getCurrentRuleLabel()
								.setText(ruleString + value.toString());
					}
				}
			}
		}

		// make the button mimic the rule number
		if(ruleNumRandomButton != null)
		{
			ruleNumRandomButton.setEnabled(ruleNum.isEnabled());
		}
	}

	/**
	 * Disables the number of states field as necessary, depending on whether
	 * certain rules are selected or not.
	 * 
	 * @param updateProperties
	 *            If true the method will update the NUMBER_OF_STATES in the CA
	 *            properties and display the result on the status panel. (Will
	 *            probably only be true when called from the constructor of this
	 *            class. At other times, we'll usually want to make sure that
	 *            the submit button has been pressed before making these
	 *            changes. This method is called whenever the user selects a new
	 *            rule, even if they have not submitted the rule.)
	 */
	public void disableRunningAverageField(boolean updateProperties)
	{
		JTextField runningAverageField = propertiesPanel
				.getRunningAverageField();

		// disable the running average field as appropriate
		if(runningAverageField != null)
		{
			String ruleSelection = (String) rulePanel.getRuleTree()
					.getSelectedRuleName();

			if(ruleSelection != null)
			{
				RuleHash ruleHash = new RuleHash();
				String ruleClassName = ruleHash.get(ruleSelection);
				Rule rule = ReflectionTool
						.instantiateMinimalRuleFromClassName(ruleClassName);

				// set the running average display value, depending on what the
				// rule wants -- if rule says null, that means don't change the
				// running average value from whatever it says currently
				Integer value = rule.getRunningAverageToDisplay();
				if(value != null)
				{
					// don't change the running average if this method was
					// called by the constructor
					if(!updateProperties)
					{
						runningAverageField.setText(value.toString());
					}
				}

				// enable or disable the display (depending on what the rule
				// wants)
				runningAverageField.setEnabled(rule.enableRunningAverage());
			}
		}
	}

	/**
	 * Disables the standard deviation field if the Guassian Random lattice is
	 * not selected.
	 */
	public void disableStandardDeviationField()
	{
		PulsatingJTextField stdev = propertiesPanel.getStandardDeviationField();
		JComboBox latticeChooser = propertiesPanel.getLatticeChooser();

		// disable the radius field as appropriate
		if(stdev != null && latticeChooser != null)
		{
			String latticeChoice = (String) latticeChooser.getSelectedItem();

			if((latticeChoice != null)
					&& latticeChoice.equals(RandomGaussianLattice.DISPLAY_NAME))
			{
				stdev.setEnabled(true);
				stdev.startPulsing(NUMBER_OF_PULSES);
			}
			else
			{
				stdev.setEnabled(false);
				stdev.stopPulsing();
			}
		}
	}

	/**
	 * Display an analysis as a frame. Closes any related analysis tab that is
	 * open.
	 * 
	 * @param analysis
	 *            The analysis to be displayed as a frame.
	 */
	public void displayAnalysisAsFrame(Analysis analysis)
	{
		// remove the analysis as a tab
		removeTab(analysis.getDisplayName());

		// display the analysis as a frame
		analysis.getDisplayFrame(analysis.getPreferredScrollViewportSize())
				.setVisible(true);

		// this will resize the analysis panel, so let it know (in
		// case it needs to redo the layout).
		analysis.resizeActions(null);
	}

	/**
	 * Display an analysis as a tab. Closes any related analysis frame that is
	 * open.
	 * 
	 * @param analysis
	 *            The analysis to be displayed as a tab.
	 */
	public void displayAnalysisAsTab(Analysis analysis)
	{
		// get a size for the display area
		Dimension size = allPanels.getPropertiesPanel().getInnerPanelSize();

		// remove the analysis as a frame
		analysis.getDisplayFrame(size).setVisible(false);

		// display the analysis as a tab
		JPanel analysisPanel = analysis.getDisplayPanelForTabbedPane(size);
		addTab(analysis, analysisPanel, analysis.getDisplayName(), analysis
				.getToolTipDescription());

		// put the focus on that tab
		allPanels.getTabbedPane().setSelectedComponent(analysisPanel);

		// this will resize the analysis panel, so let it know (in
		// case it needs to redo the layout).
		analysis.resizeActions(null);
	}

	/**
	 * Get the panel that holds the initial states.
	 * 
	 * @return The initial states tab/panel.
	 */
	public InitialStatesPanel getInitialStatesPanel()
	{
		return initialStatesPanel;
	}

	/**
	 * Get the panel that holds graphics to set all of the CA properties.
	 * 
	 * @return The properties panel.
	 */
	public PropertiesPanel getPropertiesPanel()
	{
		return propertiesPanel;
	}

	/**
	 * Get the panel that holds the CA rule.
	 * 
	 * @return The rule panel.
	 */
	public RulePanel getRulePanel()
	{
		return rulePanel;
	}

	/**
	 * Actions to take when the cellular automaton is incremented (for example,
	 * with the increment button).
	 */
	public void incrementActions()
	{
		startActions();
	}

	/**
	 * Make sure the number of columns is ok.
	 * 
	 * @return true if its value is ok.
	 */
	public static boolean isColumnsOk(double cols)
	{
		boolean ok = true;
		if(cols < 2)
		{
			ok = false;
		}

		return ok;
	}

	/**
	 * Make sure the number of states is ok.
	 * 
	 * @return true if its value is ok.
	 */
	public static boolean isNumberOfStatesOk(double numberOfStates)
	{
		boolean ok = true;
		if(numberOfStates < IntegerRule.MIN_NUM_STATES
				|| numberOfStates > IntegerRule.MAX_NUM_STATES)
		{
			ok = false;
		}

		return ok;
	}

	/**
	 * Make sure the neighborhood radius is ok.
	 * 
	 * @return true if its value is ok.
	 */
	public static boolean isRadiusOk(double radius)
	{
		boolean ok = true;
		if(radius < 1)
		{
			ok = false;
		}

		return ok;
	}

	/**
	 * Make sure the neighborhood standard deviation is ok.
	 * 
	 * @return true if its value is ok.
	 */
	public static boolean isStandardDeviationOk(double standardDeviation)
	{
		boolean ok = true;
		if(standardDeviation <= 0.0)
		{
			ok = false;
		}

		return ok;
	}

	/**
	 * Make sure the random percent number is ok.
	 * 
	 * @return true if its value is ok.
	 */
	public static boolean isRandomPercentNumberOk(double randomPercentNumber)
	{
		boolean ok = true;
		if((randomPercentNumber < 0) || (randomPercentNumber > 100))
		{
			ok = false;
		}

		return ok;
	}

	/**
	 * Make sure the number of rows is ok.
	 * 
	 * @return true if its value is ok.
	 */
	public static boolean isRowsOk(double rows)
	{
		boolean ok = true;
		if(rows < 2)
		{
			ok = false;
		}

		return ok;
	}

	/**
	 * Make sure the running average is ok.
	 * 
	 * @return true if its value is ok.
	 */
	public static boolean isRunningAverageOk(double runningAverage)
	{
		boolean ok = true;
		if(runningAverage < (Cell.MIN_HISTORY - 1)
				|| runningAverage > Cell.MAX_HISTORY)
		{
			ok = false;
		}

		return ok;
	}

	/**
	 * Removes the tab with the specified title.
	 * 
	 * @param title
	 *            The title of the tab that will be removed.
	 */
	public void removeTab(String title)
	{
		int i = allPanels.getTabbedPane().indexOfTab(title);

		if(i != -1)
		{
			allPanels.getTabbedPane().remove(i);
		}

		// After closing the tab, the tabbed pane will select another
		// tab. Make sure it does not go to a disabled tab.
		if(!allPanels.getTabbedPane().isEnabledAt(
				allPanels.getTabbedPane().getSelectedIndex()))
		{
			// go to the analysis tab
			int newIndex = allPanels.getTabbedPane().indexOfTab(
					AnalysisPanel.ANALYSIS_PANEL_TITLE);
			allPanels.getTabbedPane().setSelectedIndex(newIndex);
		}

		// make sure the analysis tab isn't disabled as well
		if(!allPanels.getTabbedPane().isEnabledAt(
				allPanels.getTabbedPane().getSelectedIndex()))
		{
			// the selected tab is disabled so find another tab
			int index = 0;
			while(!allPanels.getTabbedPane().isEnabledAt(index))
			{
				index++;
			}
			allPanels.getTabbedPane().setSelectedIndex(index);
		}

		// resize the tabbed pane (it needs to resize because the number of rows
		// of tabs may have changed).
		Dimension tabbedPaneSize = allPanels.getTabbedPane().getSize();
		Dimension newSize = new Dimension(tabbedPaneSize.width, allPanels
				.getCAFrame().getScrollPane().getHeight());
		allPanels.getTabbedPane().setPreferredSize(newSize);
		allPanels.getTabbedPane().setMinimumSize(newSize);
		allPanels.getTabbedPane().setMaximumSize(newSize);
	}

	/**
	 * Resets the initial state chooser so that it includes any initial state
	 * options specified by the rule.
	 * 
	 * @param currentInitialState
	 *            The currently selected initial state. May be null if it is
	 *            unknown.
	 * @param currentRule
	 *            The currently selected rule. May be null if it is unknown.
	 */
	public void resetInitialStateChooser(String currentInitialState,
			String currentRule)
	{
		// save the current choice
		if(currentInitialState == null)
		{
			currentInitialState = initialStatesPanel.getInitialState();
		}

		// remove the old rule's generated initial states and replace with the
		// current rule's
		initialStatesPanel.updateRuleGeneratedInitialStates();

		// if the currentInitialState still exists, set it to be the current
		// choice. Otherwise set to a default.
		initialStatesPanel.setActiveRadioButton(currentInitialState);
	}

	/**
	 * Set the text field to display the number of columns (registered in the
	 * properties).
	 */
	public void resetNumCols()
	{
		int cols = CurrentProperties.getInstance().getNumColumns();
		allPanels.getPropertiesPanel().getNumColumnsSpinner().setValue(
				new Integer(cols));

		// also reset the status panel
		// String rows = properties.getProperty(CAPropertyReader.CA_HEIGHT);
		allPanels.getStatusPanel().setCurrentDimensionsLabel();

		// allPanels.getStatusPanel().getCurrentDimensionsLabel().setText(
		// StatusPanel.DIMENSIONS_STRING + rows + " by " + cols);
	}

	/**
	 * Set the text field to display the number of rows registered in the
	 * properties.
	 */
	public void resetNumRows()
	{
		int rows = CurrentProperties.getInstance().getNumRows();
		allPanels.getPropertiesPanel().getNumRowsSpinner().setValue(
				new Integer(rows));

		// also reset the status panel
		// String cols = properties.getProperty(CAPropertyReader.CA_WIDTH);
		allPanels.getStatusPanel().setCurrentDimensionsLabel();
		// allPanels.getStatusPanel().getCurrentDimensionsLabel().setText(
		// StatusPanel.DIMENSIONS_STRING + rows + " by " + cols);
	}

	/**
	 * Changes the items displayed in the rule combo box so that only rules
	 * compatible with the selected lattice are visible.
	 * 
	 * @param currentInitialState
	 *            The currently selected initial state. May be null if it is
	 *            unknown.
	 * @param currentRule
	 *            The currently selected rule. May be null if it is unknown.
	 * @param currentLattice
	 *            The currently selected lattice. May be null if unknown.
	 */
	// public void resetRuleChooser(String currentInitialState,
	// String currentRule, String currentLattice)
	// {
	// JComboBox latticeChooser = propertiesPanel.getLatticeChooser();
	// RuleTree ruleTree = propertiesPanel.getRuleTree();
	//
	// if(ruleTree != null)
	// {
	// // save the current rule choice
	// if(currentRule == null)
	// {
	// currentRule = (String) ruleTree.getSelectedRuleName();
	// }
	//
	// // save the current lattice choice
	// if(currentLattice == null && latticeChooser != null)
	// {
	// currentLattice = (String) latticeChooser.getSelectedItem();
	// }
	//
	// // keep track of whether or not the current rule choice still exists
	// // with the current lattice
	// boolean currentRuleChoiceStillExists = false;
	//
	// // get the new rules for the rule combo box
	// String[] ruleChoices = propertiesPanel
	// .getPermissableRules(currentLattice);
	//
	// // check to see if the currentRule matches any of these
	// // rules
	// for(int i = 0; i < ruleChoices.length; i++)
	// {
	// if(currentRule != null && ruleChoices[i].equals(currentRule))
	// {
	// currentRuleChoiceStillExists = true;
	// }
	// }
	//
	// // clear the old rules from the rule combo box
	// ruleChooser.removeAllItems();
	//
	// for(int i = 0; i < ruleChoices.length; i++)
	// {
	// ruleChooser.addItem(ruleChoices[i]);
	// }
	//
	// // if the currentRule still exists, set it to be the current choice.
	// // Note, once we set the selected item, this will fire an action
	// // event on the rule.
	// if(currentRuleChoiceStillExists)
	// {
	// ruleChooser.setSelectedItem(currentRule);
	//
	// // do this, otherwise the ruleChooser.removeAllItems() fired a
	// // RULE action that reset the currentInitialState to blank.
	// // Ditto the ruleChooser.addItem() calls (which sets the
	// // selected state to the first added item which is BLANK).
	// resetInitialStateChooser(currentInitialState, currentRule);
	// }
	// else
	// {
	// // currentChoice did not exist, so use the default
	// String lattice = (String) latticeChooser.getSelectedItem();
	// if(lattice.equals(StandardOneDimensionalLattice.DISPLAY_NAME))
	// {
	// ruleChooser.setSelectedItem(new Rule102(null)
	// .getDisplayName());
	// }
	// else if(lattice.equals(SquareLattice.DISPLAY_NAME))
	// {
	// ruleChooser
	// .setSelectedItem(new Life(null).getDisplayName());
	// }
	// else if(lattice.equals(HexagonalLattice.DISPLAY_NAME))
	// {
	// ruleChooser
	// .setSelectedItem(new Life(null).getDisplayName());
	// }
	// else
	// {
	// ruleChooser
	// .setSelectedItem(new Life(null).getDisplayName());
	// }
	//
	// // do this, otherwise the ruleChooser.removeAllItems() fired
	// // a RULE action that reset the currentInitialState to
	// // blank. Ditto the ruleChooser.addItem() calls (which sets
	// // the selected state to the first added item which is BLANK).
	// resetInitialStateChooser(currentInitialState,
	// (String) ruleChooser.getSelectedItem());
	// }
	// }
	// }
	/**
	 * Set the text field to display the number of columns.
	 * 
	 * @param numCols
	 *            The number of columns.
	 */
	/*
	 * public void setNumCols(int numCols) { this.numCols.setText("" + numCols);
	 * //for consistency properties.setProperty(CAPropertyReader.CA_HEIGHT, "" +
	 * numCols); }
	 */
	/**
	 * Set the text field to display the number of rows.
	 * 
	 * @param numRows
	 *            The number of rows.
	 */
	/*
	 * public void setNumRows(int numRows) { this.numRows.setText("" + numRows);
	 * //for consistency properties.setProperty(CAPropertyReader.CA_WIDTH, "" +
	 * numRows); }
	 */

	/**
	 * Check that the user submitted properties have reasonable values.
	 * 
	 * @return Either a table of the properties (keyed by string names that are
	 *         available in the CurrentProperties class), or null if the user
	 *         submitted values are not ok.
	 */
	public Hashtable<String, Object> checkProperties()
	{
		// collect all the user choices, but only return the associated
		// properties (at the bottom of this method) if valuesOk is true.
		boolean valuesOk = true;

		ButtonGroup boundaryChoiceButtonGroup = propertiesPanel
				.getBoundaryConditionRadioButtons();
		JCheckBox fillRectangleCheckBox = initialStatesPanel
				.getFillRectangleCheckBox();
		JCheckBox fillEllipseCheckBox = initialStatesPanel
				.getFillEllipseCheckBox();
		JComboBox latticeChooser = propertiesPanel.getLatticeChooser();
		JSpinner numCols = propertiesPanel.getNumColumnsSpinner();
		JSpinner numRows = propertiesPanel.getNumRowsSpinner();
		JTextField numStates = propertiesPanel.getNumStatesField();
		JTextField radius = propertiesPanel.getRadiusField();
		JTextField standardDeviation = propertiesPanel
				.getStandardDeviationField();
		JSpinner randomPercent = initialStatesPanel.getRandomPercentSpinner();
		JSpinner ellipseHeight = initialStatesPanel.getEllipseHeightSpinner();
		JSpinner ellipseWidth = initialStatesPanel.getEllipseWidthSpinner();
		JSpinner rectangleHeight = initialStatesPanel
				.getRectangleHeightSpinner();
		JSpinner rectangleWidth = initialStatesPanel.getRectangleWidthSpinner();
		JTextField ruleNum = rulePanel.getRuleNumberTextField();
		JTextField runningAverage = propertiesPanel.getRunningAverageField();
		JSpinner[] randomPercentByState = initialStatesPanel
				.getProbabilitySpinners();

		// get the lattice choice (we will check for possible errors with
		// the lattice choice in a moment)
		String latticeChoice = (String) latticeChooser.getSelectedItem();

		String ruleClassName = "";
		String ruleDescription = "";
		if(!CAConstants.LATTICE_CENTRIC_CHOICES)
		{
			// get the rule choice
			try
			{
				ruleDescription = rulePanel.getRuleTree().getSelectedRuleName();
				if(ruleDescription != null)
				{
					RuleHash ruleHash = new RuleHash();
					ruleClassName = ruleHash.get(ruleDescription);
				}
				else
				{
					// don't want null because it is used later (even though it
					// is an incorrectly selected folder).
					ruleDescription = "";

					// make it print the error message below -- they've selected
					// a folder
					throw new Exception();
				}
			}
			catch(Exception e)
			{
				// make the JFrame look disabled
				allPanels.getCAFrame().setViewDisabled(true);

				String errorMessage = "Please select a valid rule and not a folder.";
				JOptionPane.showMessageDialog(allPanels.getFrame(),
						errorMessage, "Input Error", JOptionPane.ERROR_MESSAGE);

				// make the JFrame look enabled
				allPanels.getCAFrame().setViewDisabled(false);

				// user made mistake, so don't continue.
				valuesOk = false;
			}

			// check the lattice choice for errors
			boolean isEnabled = ((ToolTipComboBox) latticeChooser)
					.isSelectedItemEnabled();
			if(!isEnabled)
			{
				// make the JFrame look disabled
				allPanels.getCAFrame().setViewDisabled(true);

				String errorMessage = "Please select a valid lattice. Lattices in grey \n"
						+ "are not compatible with the selected rule.";
				JOptionPane.showMessageDialog(allPanels.getFrame(),
						errorMessage, "Input Error", JOptionPane.ERROR_MESSAGE);

				// make the JFrame look enabled
				allPanels.getCAFrame().setViewDisabled(false);

				// user made mistake, so don't continue.
				valuesOk = false;
			}
		}
		else
		{
			// THIS IS FOR THE LESS COMMON SITUATION WHERE THE RULE IS SELECTED
			// AFTER THE LATTICE -- UNLESS CAConstants.LATTICE_CENTRIC_CHOICES
			// IS ALTERED, WE WILL NEVER REACH THIS. I'M JUST SAVING THIS CODE
			// "IN CASE"

			// get the rule choice
			try
			{
				ruleDescription = rulePanel.getRuleTree().getSelectedRuleName();
				if(ruleDescription != null)
				{
					RuleHash ruleHash = new RuleHash();
					ruleClassName = ruleHash.get(ruleDescription);
				}
				else
				{
					// don't want null because it is used later (even though it
					// is an incorrectly selected folder).
					ruleDescription = "";

					// make it print the error message below -- they've selected
					// a folder
					throw new Exception();
				}
			}
			catch(Exception e)
			{
				// make the JFrame look disabled
				allPanels.getCAFrame().setViewDisabled(true);

				String errorMessage = "Please select a valid rule -- not a folder and \n"
						+ "not a greyed-out rule (which is incompatible \n"
						+ "with the selected lattice).";
				JOptionPane.showMessageDialog(allPanels.getFrame(),
						errorMessage, "Input Error", JOptionPane.ERROR_MESSAGE);

				// make the JFrame look enabled
				allPanels.getCAFrame().setViewDisabled(false);

				// user made mistake, so don't continue.
				valuesOk = false;
			}
		}

		// get the initial state choice
		String initialStateChoice = initialStatesPanel.getInitialState();
		String initialImageFileChoice = initialStatesPanel.getImageFilePath();
		String initialDataFileChoice = initialStatesPanel.getDataFilePath();
		if(initialImageFileChoice == null)
		{
			initialImageFileChoice = "";
		}
		if(initialDataFileChoice == null)
		{
			initialDataFileChoice = "";
		}

		// get the radius
		int radiusNumber = 0;
		try
		{
			// read radius number
			radiusNumber = Integer.parseInt(radius.getText());
			if(!isRadiusOk(radiusNumber))
			{
				throw new NumberFormatException();
			}
		}
		catch(Exception error)
		{
			// make the JFrame look disabled
			allPanels.getCAFrame().setViewDisabled(true);

			String errorMessage = "The \"radius\" must be a whole number\n"
					+ "between 1 and " + Integer.MAX_VALUE + ".";
			JOptionPane.showMessageDialog(allPanels.getFrame(), errorMessage,
					"Input Error", JOptionPane.ERROR_MESSAGE);

			// make the JFrame look enabled
			allPanels.getCAFrame().setViewDisabled(false);

			// user made mistake, so don't continue.
			valuesOk = false;
		}

		// get the standard deviation
		double standardDeviationNumber = 5.0;
		try
		{
			// read radius number
			standardDeviationNumber = Double.parseDouble(standardDeviation
					.getText());
			if(!isStandardDeviationOk(standardDeviationNumber))
			{
				throw new NumberFormatException();
			}
		}
		catch(Exception error)
		{
			// make the JFrame look disabled
			allPanels.getCAFrame().setViewDisabled(true);

			String errorMessage = "The \"standard deviation\" must be a number\n"
					+ "between 0.0 and " + Double.MAX_VALUE + ".";
			JOptionPane.showMessageDialog(allPanels.getFrame(), errorMessage,
					"Input Error", JOptionPane.ERROR_MESSAGE);

			// make the JFrame look enabled
			allPanels.getCAFrame().setViewDisabled(false);

			// user made mistake, so don't continue.
			valuesOk = false;
		}

		// make sure they didn't select too big of a neighborhood
		if(radius.isEnabled())
		{
			int numberofNeighbors = 2 * radiusNumber;
			int maxRadius = Lattice.MAX_RECOMMENDED_NEIGHBORS / 2;
			if(latticeChoice.equals(MooreRadiusTwoDimLattice.DISPLAY_NAME))
			{
				numberofNeighbors = (2 * radiusNumber + 1)
						* (2 * radiusNumber + 1) - 1;

				// solve for the radius
				maxRadius = (int) ((Math
						.sqrt(Lattice.MAX_RECOMMENDED_NEIGHBORS + 1.0) - 1.0) / 2.0);
			}
			else if(latticeChoice.equals(VonNeumannRadiusLattice.DISPLAY_NAME))
			{
				numberofNeighbors = 2 * radiusNumber * (radiusNumber + 1);

				// solve for the radius
				maxRadius = (int) Math
						.round((Math
								.sqrt(4 + 8 * Lattice.MAX_RECOMMENDED_NEIGHBORS) - 4.0) / 4.0);
			}

			if(numberofNeighbors > Lattice.MAX_RECOMMENDED_NEIGHBORS)
			{
				String message = "You have selected a radius that will create a neighborhood \n"
						+ "of "
						+ numberofNeighbors
						+ " cells.  This may cause memory and speed \n"
						+ "problems on a typical computer. \n\n"
						+ "A maximum radius of "
						+ maxRadius
						+ " (with a neighborhood of less than \n"
						+ Lattice.MAX_RECOMMENDED_NEIGHBORS
						+ ") is suggested."
						+ "\n\nClick OK to continue. Cancel to revise.\n";
				Object[] options = {"OK", "CANCEL"};
				int selection = JOptionPane.showOptionDialog(allPanels
						.getFrame(), message, "Warning",
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.WARNING_MESSAGE, null, options, options[1]);
				if(selection == 1)
				{
					valuesOk = false;
				}
			}
		}

		// get the boundary condition choice
		int boundaryType = Integer.parseInt(boundaryChoiceButtonGroup
				.getSelection().getActionCommand());

		// get the random percent
		Integer randomPercentNumber = null;
		try
		{
			// commit the value on the spinner
			randomPercent.commitEdit();

			// read random percent number
			randomPercentNumber = (Integer) randomPercent.getValue();
		}
		catch(Exception error)
		{
			// make the JFrame look disabled
			allPanels.getCAFrame().setViewDisabled(true);

			String errorMessage = "The \"random percent\" must be a number\n"
					+ "between 0 and 100.";
			JOptionPane.showMessageDialog(allPanels.getFrame(), errorMessage,
					"Input Error", JOptionPane.ERROR_MESSAGE);

			// make the JFrame look enabled
			allPanels.getCAFrame().setViewDisabled(false);

			// user made mistake, so don't continue.
			valuesOk = false;
		}

		// get the random percent for each state
		Integer[] randomPercentByStateNumbers = null;
		if(randomPercentByState != null && randomPercentByState[0].isEnabled())
		{
			randomPercentByStateNumbers = new Integer[randomPercentByState.length];
			try
			{
				for(int i = 0; i < randomPercentByState.length; i++)
				{
					// commit the value on the spinner
					randomPercentByState[i].commitEdit();

					// read random percent number
					randomPercentByStateNumbers[i] = (Integer) randomPercentByState[i]
							.getValue();
				}

				int percentSum = 0;
				for(Integer percent : randomPercentByStateNumbers)
				{
					// make sure the sum is 100
					percentSum += percent.intValue();
				}
				if(percentSum != 100)
				{
					// make the JFrame look disabled
					allPanels.getCAFrame().setViewDisabled(true);

					String errorMessage = "The probabilities selected for each state\n"
							+ "must total exactly 100%.  Your total is "
							+ percentSum
							+ "%.\n\n"
							+ "Please re-enter these initial state values.";
					JOptionPane.showMessageDialog(allPanels.getFrame(),
							errorMessage, "Input Error",
							JOptionPane.ERROR_MESSAGE);

					// make the JFrame look enabled
					allPanels.getCAFrame().setViewDisabled(false);

					// user made mistake, so don't continue.
					valuesOk = false;
				}
			}
			catch(Exception error)
			{
				// make the JFrame look disabled
				allPanels.getCAFrame().setViewDisabled(true);

				String errorMessage = "The \"random percent\" must be a number\n"
						+ "between 0 and 100.";
				JOptionPane.showMessageDialog(allPanels.getFrame(),
						errorMessage, "Input Error", JOptionPane.ERROR_MESSAGE);

				// make the JFrame look enabled
				allPanels.getCAFrame().setViewDisabled(false);

				// user made mistake, so don't continue.
				valuesOk = false;
			}
		}
		// Store the random percent as a string
		StringBuffer randomPercentByStateBuffer = new StringBuffer("");
		String randomPercentByStateString = "";
		String delimiters = CurrentProperties.getInstance().getDataDelimiters();
		if(randomPercentByStateNumbers != null
				&& randomPercentByStateNumbers.length > 0)
		{
			for(Integer i : randomPercentByStateNumbers)
			{
				randomPercentByStateBuffer.append(i);
				randomPercentByStateBuffer.append(delimiters);
			}
			randomPercentByStateString = randomPercentByStateBuffer.toString();

			// remove the extraneous last set of delimiters
			randomPercentByStateString = randomPercentByStateString.substring(
					0, randomPercentByStateString.lastIndexOf(delimiters));
		}

		// get the rectangle width and height
		Integer rectangleHeightNumber = null;
		Integer rectangleWidthNumber = null;
		try
		{
			// commit the values on the spinners
			rectangleHeight.commitEdit();
			rectangleWidth.commitEdit();

			// read the height and width
			rectangleHeightNumber = (Integer) rectangleHeight.getValue();
			rectangleWidthNumber = (Integer) rectangleWidth.getValue();
		}
		catch(Exception error)
		{
			// make the JFrame look disabled
			allPanels.getCAFrame().setViewDisabled(true);

			String errorMessage = "The rectangle initial state has an \n"
					+ "invalid width or height.";
			JOptionPane.showMessageDialog(allPanels.getFrame(), errorMessage,
					"Input Error", JOptionPane.ERROR_MESSAGE);

			// make the JFrame look enabled
			allPanels.getCAFrame().setViewDisabled(false);

			// user made mistake, so don't continue.
			valuesOk = false;
		}

		// get whether or not the rectangle is filled
		Boolean fillRectangle = new Boolean(fillRectangleCheckBox.isSelected());

		// get the ellipse width and height
		Integer ellipseHeightNumber = null;
		Integer ellipseWidthNumber = null;
		try
		{
			// commit the values on the spinners
			ellipseHeight.commitEdit();
			ellipseWidth.commitEdit();

			// read the height and width
			ellipseHeightNumber = (Integer) ellipseHeight.getValue();
			ellipseWidthNumber = (Integer) ellipseWidth.getValue();
		}
		catch(Exception error)
		{
			// make the JFrame look disabled
			allPanels.getCAFrame().setViewDisabled(true);

			String errorMessage = "The ellipse initial state has an \n"
					+ "invalid width or height.";
			JOptionPane.showMessageDialog(allPanels.getFrame(), errorMessage,
					"Input Error", JOptionPane.ERROR_MESSAGE);

			// make the JFrame look enabled
			allPanels.getCAFrame().setViewDisabled(false);

			// user made mistake, so don't continue.
			valuesOk = false;
		}

		// get whether or not the ellipse is filled
		Boolean fillEllipse = new Boolean(fillEllipseCheckBox.isSelected());

		// get the number of states allowed
		int minState = IntegerRule.MIN_NUM_STATES;
		int maxState = IntegerRule.MAX_NUM_STATES;
		MinMaxIntPair minmax = IntegerRule.getMinMaxStatesAllowed(
				latticeChoice, ruleDescription);
		if(minmax != null)
		{
			// assign values specified by the rule, but make sure they don't
			// exceed the maximum allowed
			if(minmax.min >= minState)
			{
				minState = minmax.min;
			}
			if(minmax.max <= maxState)
			{
				maxState = minmax.max;
			}
		}
		int numOfStates = minState;
		try
		{
			if(IntegerCellState.isCompatibleRule(ruleDescription))
			{
				// read number of states
				numOfStates = Integer.parseInt(numStates.getText());
				if(!isNumberOfStatesOk(numOfStates))
				{
					throw new NumberFormatException();
				}

				if((numOfStates < minState) || (numOfStates > maxState))
				{
					throw new NumberFormatException();
				}
			}
		}
		catch(Exception error)
		{
			String errorMessage = "";
			errorMessage = "The \"number of states\" must be between "
					+ minState + " and " + maxState + " when \n"
					+ "using the rule \"" + ruleDescription
					+ "\" with the lattice, \n\"" + latticeChoice + "\".";

			if(minState == maxState)
			{
				errorMessage = "The \"number of states\" must be " + maxState
						+ " when using the rule \n" + "\"" + ruleDescription
						+ "\" with the lattice, \n\"" + latticeChoice + "\".";
			}

			// make the JFrame look disabled
			allPanels.getCAFrame().setViewDisabled(true);

			JOptionPane.showMessageDialog(allPanels.getFrame(), errorMessage,
					"Input Error", JOptionPane.ERROR_MESSAGE);

			// make the JFrame look enabled
			allPanels.getCAFrame().setViewDisabled(false);

			// user made mistake, so don't continue.
			valuesOk = false;
		}

		// get the rule number (which requires the min and max allowed rule
		// numbers)
		BigInteger ruleNumber = new BigInteger(""
				+ WolframRuleNumber.DEFAULT_RULE);

		MinMaxBigIntPair minmaxRule = IntegerRule.getMinMaxRuleNumberAllowed(
				latticeChoice, ruleDescription, numOfStates);
		try
		{
			// if minmaxRule is null, then this is irrelevant
			if(minmaxRule != null)
			{
				ruleNumber = new BigInteger(ruleNum.getText());
				if(ruleNumber.compareTo(minmaxRule.max) > 0
						|| ruleNumber.compareTo(minmaxRule.min) < 0)
				{
					throw new NumberFormatException();
				}
			}
		}
		catch(Exception error)
		{
			String errorMessage = "The \"rule number\" is invalid.";
			if(minmaxRule != null)
			{
				String minRule = "" + minmaxRule.min;
				String maxRule = "" + minmaxRule.max;

				// The pattern used to display the rule number.
				NumberFormat formatter = new DecimalFormat(
						CAConstants.SCIENTIFIC_NOTATION_PATTERN);

				if(maxRule.length() > TOOLTIP_MAX_RULENUMBER_LENGTH)
				{
					maxRule = formatter.format(minmaxRule.max);
				}

				if(minRule.length() > TOOLTIP_MAX_RULENUMBER_LENGTH)
				{
					minRule = formatter.format(minmaxRule.min);
				}

				if(maxRule.length() + minRule.length() > 20)
				{
					errorMessage = "<html><body>"
							+ "The \"rule number\" must be an integer between <p>"
							+ minRule + " and " + maxRule + "<p>"
							+ "for the rule \"" + ruleDescription
							+ "\", the lattice \"" + latticeChoice
							+ "\", and the number of states " + numOfStates
							+ ".</body></html>";
				}
				else
				{
					errorMessage = "<html><body>"
							+ "The \"rule number\" must be an integer between "
							+ minRule + " and " + maxRule + "<p>"
							+ "for the rule \"" + ruleDescription
							+ "\", the lattice \"" + latticeChoice
							+ "\", and the number of states " + numOfStates
							+ ".</body></html>";
				}
			}

			// make the JFrame look disabled
			allPanels.getCAFrame().setViewDisabled(true);

			JOptionPane.showMessageDialog(allPanels.getFrame(), errorMessage,
					"Input Error", JOptionPane.ERROR_MESSAGE);

			// make the JFrame look enabled
			allPanels.getCAFrame().setViewDisabled(false);

			// user made mistake, so don't continue.
			valuesOk = false;
		}

		int runningAverageNumber = 1;
		try
		{
			// read running average number
			runningAverageNumber = Integer.parseInt(runningAverage.getText());
			if(!isRunningAverageOk(runningAverageNumber))
			{
				throw new NumberFormatException();
			}
		}
		catch(Exception error)
		{
			// make the JFrame look disabled
			allPanels.getCAFrame().setViewDisabled(true);

			String errorMessage = "The \"running average\" must be a number between "
					+ (Cell.MIN_HISTORY - 1) + " and " + Cell.MAX_HISTORY + ".";
			JOptionPane.showMessageDialog(allPanels.getFrame(), errorMessage,
					"Input Error", JOptionPane.ERROR_MESSAGE);

			// make the JFrame look enabled
			allPanels.getCAFrame().setViewDisabled(false);

			// user made mistake, so don't continue.
			valuesOk = false;
		}

		// rows and cols will be set to the new value (first commit so that any
		// errors are cought by the spinner and replaced with the last valid
		// number)
		int cols = 3;
		int rows = 3;
		try
		{
			numRows.commitEdit();
			rows = ((Integer) numRows.getValue()).intValue();
		}
		catch(Exception e)
		{
			// make the JFrame look disabled
			allPanels.getCAFrame().setViewDisabled(true);

			String errorMessage = "The number of \"rows\" must "
					+ "be a positive integer greater than 1 \n and less than "
					+ PropertiesPanel.MAX_ROW_VALUE + ".";
			JOptionPane.showMessageDialog(allPanels.getFrame(), errorMessage,
					"Input Error", JOptionPane.ERROR_MESSAGE);

			// make the JFrame look enabled
			allPanels.getCAFrame().setViewDisabled(false);

			// user made mistake, so don't continue.
			valuesOk = false;
		}
		try
		{
			numCols.commitEdit();
			cols = ((Integer) numCols.getValue()).intValue();
		}
		catch(Exception e)
		{

			// make the JFrame look disabled
			allPanels.getCAFrame().setViewDisabled(true);

			String errorMessage = "The number of \"columns\" must "
					+ "be a positive integer greater than 1 \n and less than "
					+ PropertiesPanel.MAX_COLUMN_VALUE + ".";
			JOptionPane.showMessageDialog(allPanels.getFrame(), errorMessage,
					"Input Error", JOptionPane.ERROR_MESSAGE);

			// make the JFrame look enabled
			allPanels.getCAFrame().setViewDisabled(false);

			// user made mistake, so don't continue.
			valuesOk = false;
		}

		// make sure they didn't select too big of a grid
		boolean isOneDim = OneDimensionalLattice
				.isCurrentLatticeOneDim(latticeChoice);
		if((rows * cols > OneDimensionalLattice.MAX_RECOMMENDED_CELLS)
				&& isOneDim)
		{
			String message = "You have selected a very large one-dimensional CA with \n"
					+ "rows and columns requiring "
					+ (rows * cols)
					+ " cells. \n\n"
					+ "To improve performance: \n"
					+ "(1) The reverse button will only redraw the last few rows. \n"
					+ "(2) If the graphics are set to update at the end of the \n"
					+ "simulation, then only the last few rows will be displayed. \n"
					+ "(3) If the graphics are set to update at a specified interval \n"
					+ "(greater than 1), then only the last few rows will be displayed. \n"
					+ "(4) The \"zoom\" feature will redraw only the last few rows. \n"
					+ "(5) Changing the \"color scheme\" will redraw only the last \n"
					+ "few rows. \n"
					+ "(6) Other behaviors may be compromised. \n\n"
					+ "If that is unacceptable, cancel and reduce the number of \n"
					+ "rows and columns. \n\n"
					+ "Click OK to continue. Cancel to revise.\n";

			// make the JFrame look disabled
			allPanels.getCAFrame().setViewDisabled(true);

			Object[] options = {"OK", "CANCEL"};
			int selection = JOptionPane.showOptionDialog(allPanels.getFrame(),
					message, "Warning", JOptionPane.DEFAULT_OPTION,
					JOptionPane.WARNING_MESSAGE, null, options, options[1]);

			// make the JFrame look enabled
			allPanels.getCAFrame().setViewDisabled(false);

			if(selection == 1)
			{
				// bail and let them try again
				valuesOk = false;
			}
		}

		if((rows * cols > TwoDimensionalLattice.MAX_RECOMMENDED_CELLS)
				&& !isOneDim)
		{
			String message = "You have selected a two-dimensional CA \n"
					+ "with rows and columns requiring "
					+ (rows * cols)
					+ " \npixels and cells.  This may cause memory and speed \n"
					+ "problems. A maximum of "
					+ TwoDimensionalLattice.MAX_RECOMMENDED_CELLS
					+ " is suggested. \n\nGraphics will be very slow."
					+ "\n\nClick OK to continue. Cancel to revise.\n";

			// make the JFrame look disabled
			allPanels.getCAFrame().setViewDisabled(true);

			Object[] options = {"OK", "CANCEL"};
			int selection = JOptionPane.showOptionDialog(allPanels.getFrame(),
					message, "Warning", JOptionPane.DEFAULT_OPTION,
					JOptionPane.WARNING_MESSAGE, null, options, options[1]);

			// make the JFrame look enabled
			allPanels.getCAFrame().setViewDisabled(false);

			if(selection == 1)
			{
				valuesOk = false;
			}
		}

		// will return this table of properties (will be null if valuesOk ==
		// false)
		Hashtable<String, Object> userSubmittedProperties = null;
		if(valuesOk)
		{
			// create an enumeration of all of the properties (which we just
			// determined are all ok)
			userSubmittedProperties = new Hashtable<String, Object>();
			userSubmittedProperties.put(CurrentProperties.LATTICE,
					latticeChoice);
			userSubmittedProperties.put(CurrentProperties.RANDOM_PERCENT,
					randomPercentNumber);
			userSubmittedProperties
					.put(CurrentProperties.INITIAL_STATE_ELLIPSE_FILLED,
							fillEllipse);
			userSubmittedProperties.put(
					CurrentProperties.INITIAL_STATE_RECTANGLE_FILLED,
					fillRectangle);
			userSubmittedProperties.put(
					CurrentProperties.INITIAL_STATE_ELLIPSE_HEIGHT,
					ellipseHeightNumber);
			userSubmittedProperties.put(
					CurrentProperties.INITIAL_STATE_ELLIPSE_WIDTH,
					ellipseWidthNumber);
			userSubmittedProperties.put(
					CurrentProperties.INITIAL_STATE_RECTANGLE_HEIGHT,
					rectangleHeightNumber);
			userSubmittedProperties.put(
					CurrentProperties.INITIAL_STATE_RECTANGLE_WIDTH,
					rectangleWidthNumber);
			userSubmittedProperties.put(CurrentProperties.RULE, ruleClassName);
			userSubmittedProperties.put(CurrentProperties.INITIAL_STATE,
					initialStateChoice);
			userSubmittedProperties.put(
					CurrentProperties.INITIAL_STATE_IMAGE_FILE_PATH,
					initialImageFileChoice);
			userSubmittedProperties.put(
					CurrentProperties.INITIAL_STATE_DATA_FILE_PATH,
					initialDataFileChoice);
			userSubmittedProperties.put(CurrentProperties.RADIUS, radiusNumber);
			userSubmittedProperties.put(CurrentProperties.STANDARD_DEVIATION,
					standardDeviationNumber);
			userSubmittedProperties.put(CurrentProperties.RUNNING_AVERAGE,
					runningAverageNumber);
			userSubmittedProperties.put(CurrentProperties.RULE_NUMBER,
					ruleNumber);
			userSubmittedProperties.put(CurrentProperties.NUMBER_OF_STATES,
					numOfStates);
			userSubmittedProperties.put(CurrentProperties.CA_HEIGHT, rows);
			userSubmittedProperties.put(CurrentProperties.CA_WIDTH, cols);
			userSubmittedProperties.put(CurrentProperties.BOUNDARY_CONDITION,
					boundaryType);
			userSubmittedProperties.put(
					CurrentProperties.RANDOM_PERCENT_PER_STATE,
					randomPercentByStateString);
		}

		return userSubmittedProperties;
	}

	/**
	 * Set the user submitted properties. Recommended that the properties first
	 * be checked with the method checkProperties().
	 * 
	 * @param userSubmittedProperties
	 *            A hash table of the properties that will be set.
	 */
	public void submitProperties(
			Hashtable<String, Object> userSubmittedProperties)
	{
		// update the properties
		CurrentProperties properties = CurrentProperties.getInstance();

		properties.setLatticeDisplayName((String) userSubmittedProperties
				.get(CurrentProperties.LATTICE));
		properties.setRandomPercent(((Integer) userSubmittedProperties
				.get(CurrentProperties.RANDOM_PERCENT)).intValue());
		properties
				.setInitialStateEllipseFilled(((Boolean) userSubmittedProperties
						.get(CurrentProperties.INITIAL_STATE_ELLIPSE_FILLED))
						.booleanValue());
		properties
				.setInitialStateRectangleFilled(((Boolean) userSubmittedProperties
						.get(CurrentProperties.INITIAL_STATE_RECTANGLE_FILLED))
						.booleanValue());
		properties
				.setInitialStateEllipseHeight(((Integer) userSubmittedProperties
						.get(CurrentProperties.INITIAL_STATE_ELLIPSE_HEIGHT))
						.intValue());
		properties
				.setInitialStateEllipseWidth(((Integer) userSubmittedProperties
						.get(CurrentProperties.INITIAL_STATE_ELLIPSE_WIDTH))
						.intValue());
		properties
				.setInitialStateRectangleHeight(((Integer) userSubmittedProperties
						.get(CurrentProperties.INITIAL_STATE_RECTANGLE_HEIGHT))
						.intValue());
		properties
				.setInitialStateRectangleWidth(((Integer) userSubmittedProperties
						.get(CurrentProperties.INITIAL_STATE_RECTANGLE_WIDTH))
						.intValue());
		properties.setRuleClassName((String) userSubmittedProperties
				.get(CurrentProperties.RULE));
		properties.setInitialState((String) userSubmittedProperties
				.get(CurrentProperties.INITIAL_STATE));
		properties
				.setInitialStateImageFilePath((String) userSubmittedProperties
						.get(CurrentProperties.INITIAL_STATE_IMAGE_FILE_PATH));
		properties.setInitialStateDataFilePath((String) userSubmittedProperties
				.get(CurrentProperties.INITIAL_STATE_DATA_FILE_PATH));
		properties.setNeighborhoodRadius(((Integer) userSubmittedProperties
				.get(CurrentProperties.RADIUS)).intValue());
		properties.setStandardDeviation(((Double) userSubmittedProperties
				.get(CurrentProperties.STANDARD_DEVIATION)).doubleValue());
		properties.setRunningAverage(((Integer) userSubmittedProperties
				.get(CurrentProperties.RUNNING_AVERAGE)).intValue());
		properties.setRuleNumber((BigInteger) userSubmittedProperties
				.get(CurrentProperties.RULE_NUMBER));
		properties.setNumStates(((Integer) userSubmittedProperties
				.get(CurrentProperties.NUMBER_OF_STATES)).intValue());
		properties.setNumRows(((Integer) userSubmittedProperties
				.get(CurrentProperties.CA_HEIGHT)).intValue());
		properties.setNumColumns(((Integer) userSubmittedProperties
				.get(CurrentProperties.CA_WIDTH)).intValue());
		properties.setBoundaryCondition(((Integer) userSubmittedProperties
				.get(CurrentProperties.BOUNDARY_CONDITION)).intValue());
		properties.setRandomPercentPerState((String) userSubmittedProperties
				.get(CurrentProperties.RANDOM_PERCENT_PER_STATE));

		// and update the lattices (this is annoyingly high connectivity,
		// but is a consequence of the lattice classes being instantiated
		// without the properties in some cases -- there is no way for the
		// lattice to read the properties to get the radius)
		MooreRadiusOneDimLattice.radius = ((Integer) userSubmittedProperties
				.get(CurrentProperties.RADIUS)).intValue();
		MooreRadiusTwoDimLattice.radius = ((Integer) userSubmittedProperties
				.get(CurrentProperties.RADIUS)).intValue();
		VonNeumannRadiusLattice.radius = ((Integer) userSubmittedProperties
				.get(CurrentProperties.RADIUS)).intValue();

		// and update the status panel
		allPanels.getStatusPanel().setCurrentRuleLabel(
				(BigInteger) userSubmittedProperties
						.get(CurrentProperties.RULE_NUMBER));
		allPanels.getStatusPanel().setCurrentLatticeLabel();
		allPanels.getStatusPanel().setCurrentDimensionsLabel();
		allPanels.getStatusPanel().setCurrentNumberOfStatesLabel();
		allPanels.getStatusPanel().setCurrentRunningAverageLabel();
	}

	/**
	 * Actions to take when the cellular automaton is started (for example, with
	 * the start button).
	 */
	public void startActions()
	{
		// set a property indicating that the CA should run.
		allPanels.getStatusPanel().setStatusLabel(RUNNING_MESSAGE);

		// disable most controls while running
		allPanels.getStartPanel().getStartButton().setEnabled(false);
		allPanels.getStartPanel().getStopButton().setEnabled(true);
		allPanels.getStartPanel().getStopTimeField().setEnabled(false);
		allPanels.getStartPanel().getIncrementButton().setEnabled(false);
		allPanels.getStartPanel().getStep10Button().setEnabled(false);
		allPanels.getStartPanel().getStepBackButton().setEnabled(false);
		allPanels.getStartPanel().getStepFillButton().setEnabled(false);

		allPanels.getCAFrame().getToolBar().getStartButton().setEnabled(false);
		allPanels.getCAFrame().getToolBar().getStopButton().setEnabled(true);
		allPanels.getCAFrame().getToolBar().getIncrementButton().setEnabled(
				false);

		allPanels.getCAFrame().getToolBar().getMoveLeftButton().setEnabled(
				false);
		allPanels.getCAFrame().getToolBar().getMoveRightButton().setEnabled(
				false);

		allPanels.getCAFrame().getMenuBar().getStartMenuItem()
				.setEnabled(false);
		allPanels.getCAFrame().getMenuBar().getStopMenuItem().setEnabled(true);
		allPanels.getCAFrame().getMenuBar().getIncrementMenuItem().setEnabled(
				false);
		allPanels.getCAFrame().getMenuBar().getStep10MenuItem().setEnabled(
				false);
		allPanels.getCAFrame().getMenuBar().getStepBackMenuItem().setEnabled(
				false);
		allPanels.getCAFrame().getMenuBar().getStepFillMenuItem().setEnabled(
				false);
		allPanels.getCAFrame().getMenuBar().getFlipLayoutMenuItem().setEnabled(
				false);
	}

	/**
	 * Actions to take when the cellular automaton steps back one generation.
	 */
	public void stepBackActions()
	{
		startActions();
	}

	/**
	 * Actions to take when the cellular automaton is stopped (for example, with
	 * the stop button or the submit button).
	 */
	public void stopActions()
	{
		// don't "start" any more
		allPanels.getStatusPanel().setStatusLabel(STOPPED_MESSAGE);

		// enable most controls while stopped
		allPanels.getStartPanel().getStartButton().setEnabled(true);
		allPanels.getStartPanel().getStopButton().setEnabled(false);
		allPanels.getStartPanel().getStopTimeField().setEnabled(true);
		allPanels.getStartPanel().getIncrementButton().setEnabled(true);
		allPanels.getStartPanel().getStep10Button().setEnabled(true);
		allPanels.getStartPanel().getStepBackButton().setEnabled(true);
		allPanels.getStartPanel().getStepFillButton().setEnabled(true);

		allPanels.getCAFrame().getToolBar().getStartButton().setEnabled(true);
		allPanels.getCAFrame().getToolBar().getStopButton().setEnabled(false);
		allPanels.getCAFrame().getToolBar().getIncrementButton().setEnabled(
				true);

		if(allPanels.getCAFrame().getMenuBar().isLeftLayout())
		{
			allPanels.getCAFrame().getToolBar().getMoveLeftButton().setEnabled(
					false);
			allPanels.getCAFrame().getToolBar().getMoveRightButton()
					.setEnabled(true);
		}
		else
		{
			allPanels.getCAFrame().getToolBar().getMoveLeftButton().setEnabled(
					true);
			allPanels.getCAFrame().getToolBar().getMoveRightButton()
					.setEnabled(false);
		}

		allPanels.getCAFrame().getMenuBar().getStartMenuItem().setEnabled(true);
		allPanels.getCAFrame().getMenuBar().getStopMenuItem().setEnabled(false);
		allPanels.getCAFrame().getMenuBar().getIncrementMenuItem().setEnabled(
				true);
		allPanels.getCAFrame().getMenuBar().getStep10MenuItem()
				.setEnabled(true);
		allPanels.getCAFrame().getMenuBar().getStepBackMenuItem().setEnabled(
				true);
		allPanels.getCAFrame().getMenuBar().getStepFillMenuItem().setEnabled(
				true);
		allPanels.getCAFrame().getMenuBar().getFlipLayoutMenuItem().setEnabled(
				true);
	}

	/**
	 * Change the rule number tip (and the random number button tip) so that it
	 * tells the user what numbers are available.
	 */
	public void updateRuleNumberTip()
	{
		// a default
		String toolTip = "<html>Choose a rule by its number.</html>";
		String randomNumberToolTip = "<html>"
				+ "Selects a random rule number within the allowed range. <br><br>"
				+ "(In practice, the random numbers are limited to "
				+ AllPanelController.MAX_RANDOM_NUM_BITS + " bits <br> "
				+ "to prevent problems displaying.)</html>";

		// get info needed to get the rule numbers
		String latticeDescription = (String) propertiesPanel
				.getLatticeChooser().getSelectedItem();
		String ruleDescription = (String) rulePanel.getRuleTree()
				.getSelectedRuleName();

		// get the number of states
		int numStates = 2;
		try
		{
			// make sure they didn't enter something wrong
			JTextField numStatesField = propertiesPanel.getNumStatesField();
			numStates = Integer.parseInt(numStatesField.getText());
		}
		catch(Exception e)
		{
			// do nothing
		}

		if(ruleDescription != null)
		{
			// get the min and max rule numbers allowed
			MinMaxBigIntPair minmaxRule = IntegerRule
					.getMinMaxRuleNumberAllowed(latticeDescription,
							ruleDescription, numStates);

			if(minmaxRule != null)
			{
				String minRule = "" + minmaxRule.min;
				String maxRule = "" + minmaxRule.max;

				// The pattern used to display the rule number.
				NumberFormat formatter = new DecimalFormat(
						CAConstants.SCIENTIFIC_NOTATION_PATTERN);

				if(maxRule.length() > TOOLTIP_MAX_RULENUMBER_LENGTH)
				{
					maxRule = formatter.format(minmaxRule.max);
				}

				if(minRule.length() > TOOLTIP_MAX_RULENUMBER_LENGTH)
				{
					minRule = formatter.format(minmaxRule.min);
				}

				if(!minmaxRule.max.equals(minmaxRule.min))
				{
					toolTip = "<html>For the selected rule, lattice, and number of states, "
							+ "you may choose <br>"
							+ "a rule between "
							+ minRule + " and " + maxRule + ".</html>";

					randomNumberToolTip = "<html>"
							+ "Selects a random rule number within the allowed range, <br>"
							+ minRule + " to " + maxRule + ".</html>";

					if(minmaxRule.max.bitLength() > MAX_RANDOM_NUM_BITS)
					{
						randomNumberToolTip = "<html>"
								+ "Selects a random rule number within the allowed range, <br>"
								+ minRule
								+ " to "
								+ maxRule
								+ ". <br><br>"
								+ "(In practice, the random numbers are limited to "
								+ AllPanelController.MAX_RANDOM_NUM_BITS
								+ " bits <br> "
								+ "to prevent problems displaying.)</html>";
					}
				}
				else
				{
					toolTip = "<html>Only rule number " + minRule
							+ " is allowed with the <br>"
							+ "selected rule and lattice.</html>";

					randomNumberToolTip = toolTip;
				}
			}
			else
			{
				// rule numbers not allowed for this lattice/rule combination
				toolTip = "<html>Cannot choose a rule number with the <br>"
						+ "selected rule and lattice.</html>";
				randomNumberToolTip = toolTip;
			}
		}

		// set the tool tips
		rulePanel.getRuleNumberTextField().setToolTipText(toolTip);
		rulePanel.getRuleNumberRandomButton().setToolTipText(
				randomNumberToolTip);
	}

	/**
	 * Uncheck the specified analysis on the analysis panel.
	 * 
	 * @param analysisDisplayName
	 *            The display name of the analysis that will be unchecked.
	 */
	public void uncheckAnalysis(String analysisDisplayName)
	{
		allPanels.getAnalysisPanel().uncheck(analysisDisplayName);
	}
}

// THIS MIGHT BE USED TO DECIDE HOW MUCH MEMORY IS NECESSARY FOR A GIVEN
// LATTICE. THE LAST IF STATEMENT COULD WARN USERS THAT TOO MUCH MEMORY IS
// NEEDED. PROBLEM IS THAT IT DOESN'T WARN IF THE SIMULATION WILL JUST BE SLOW.
//
// // how much memory does the current simulation use
// long currentLatticeMemory = CAFactory.currentLatticeMemory;
// System.out.println("AllPanelController: currentLatticeMemory = "
// + currentLatticeMemory);
//
// // how many states are being stored in the current simulation
// int numCurrentCols = Integer.parseInt(properties
// .getProperty(CAPropertyReader.CA_WIDTH));
// int numCurrentRows = Integer.parseInt(properties
// .getProperty(CAPropertyReader.CA_HEIGHT));
// int numStatesPerCell = Cell.STATES_STORED;
// int numberOfStatesStored = numCurrentRows * numCurrentCols
// * numStatesPerCell;
// System.out.println("AllPanelController: numberOfStatesStored = "
// + numberOfStatesStored);
//
// // memory per state
// double memoryRequiredPerState = (double) currentLatticeMemory
// / (double) numberOfStatesStored;
// System.out.println("AllPanelController: memoryRequiredPerState = "
// + memoryRequiredPerState);
//
// // a minimum of how many states will be needed for the user's new
// // selection
// long numStatesUserWants = (long) (rows * cols * avgNumber);
// System.out.println("AllPanelController: numStatesUserWants = "
// + numStatesUserWants);
//
// // approximate memory needed for the user's selections
// double memoryNeeded = numStatesUserWants * memoryRequiredPerState;
// System.out.println("AllPanelController: memoryNeeded = "
// + memoryNeeded);
//
// // how much memory is potentially available
// long memoryAvailable = MemoryManagementTools.getMemoryAvailable()
// + MemoryManagementTools.getMemoryCurrentlyInUse();
// System.out.println("AllPanelController: memoryAvailable = "
// + memoryAvailable);
//
// // percent of the memory that the user wants
// double percentageMemoryNeeded = memoryNeeded / (double) memoryAvailable;
// System.out.println("AllPanelController: percentageMemoryNeeded = "
// + percentageMemoryNeeded);
//
// if((percentageMemoryNeeded > 0.75) && !isOneDim)
