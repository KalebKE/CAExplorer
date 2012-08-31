/*
 * AllPanel -- a class within the Cellular Automaton Explorer. Copyright (C)
 * 2007 David B. Bahr (http://academic.regis.edu/dbahr/) This program is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version. This program
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package cellularAutomata.graphics;

import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cellularAutomata.util.Fonts;

/**
 * Creates the display area for all of the buttons and other controls.
 * 
 * @author David Bahr
 */
public class AllPanel extends JPanel
{
	// the panel with additional properties specified by the rule (if any)
	private AdditionalPropertiesTabPanel additionalPropertiesTabPanel = null;

	// Controls interactions and values on the start, status, and properties
	// panels.
	private AllPanelController controller = null;

	// A listener for components on this control panel.
	private AllPanelListener listener = null;

	// the JPanel containing the analysis check boxes.
	private AnalysisPanel analysisPanel = null;

	// The CAFrame that creates the JFrame, menubar, toolbar, etc.
	private CAFrame caFrame = null;

	// The tab that holds the description of the currently active rule
	private DescriptionPanel descriptionPanel = null;

	// The tab that holds all the initial state controls
	private InitialStatesPanel initialStatesPanel = null;

	// The JFrame to which this panel is being added.
	private JFrame frame = null;

	// a tabbed pane to hold start-stop panel, properties panel, analysis
	// panel, etc.
	private JTabbedPane tabbedPane = null;

	// the JPanel containing the start and stop buttons, etc.
	private PropertiesPanel propertiesPanel = null;

	// the JPanel containing the rule tree, rule description, etc.
	private RulePanel rulePanel = null;

	// the JPanel containing the start and stop buttons, etc.
	private StartPanel startPanel = null;

	// the JPanel containing the status messages.
	private StatusPanel statusPanel = null;

	/**
	 * Creates a panel of the specified height and width.
	 * 
	 * @param frame
	 *            The JFrame to which this panel is being added.
	 * @param caFrame
	 *            The cellular automata's frame.
	 */
	public AllPanel(JFrame frame, CAFrame caFrame)
	{
		// necessary to add new components
		this.frame = frame;

		// the CA frame
		this.caFrame = caFrame;

		// get a listener for this panel
		listener = new AllPanelListener(this);

		this.setOpaque(true);

		// set the layout manager
		this.setLayout(new FlowLayout(FlowLayout.LEFT));

		// add the buttons and other controls
		createTabbedPane();

		// Controls interactions and values on the start, status, and properties
		// panels. Must be instantiated after the panels are created (in
		// createControls()).
		controller = new AllPanelController(this);

		// set the controller for the listener.
		listener.setController(controller);

		// Doing this preloads the JOptionPane error and warning message
		// resources and makes the response time much, much, much faster
		// when a JOptionPane is called later.
		JOptionPane pane = new JOptionPane("", JOptionPane.ERROR_MESSAGE,
				JOptionPane.YES_NO_CANCEL_OPTION);
		JOptionPane pane2 = new JOptionPane("", JOptionPane.WARNING_MESSAGE,
				JOptionPane.YES_NO_CANCEL_OPTION);
	}

	/**
	 * React to any specified keystrokes, like the enter key.
	 */
	private void bindKeystrokes()
	{
		// the action to take when the enter key is released
		Action submitTheCARuleAction = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				// only react to the "enter" key if it is one of the specified
				// tabs
				if(tabbedPane.getSelectedComponent().equals(initialStatesPanel)
						|| tabbedPane.getSelectedComponent().equals(rulePanel)
						|| tabbedPane.getSelectedComponent().equals(
								propertiesPanel))
				{
					if(rulePanel != null)
					{
						rulePanel.getSubmitButton().doClick();
					}
				}
			}
		};

		this.getInputMap(
				javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0),
				"submitTheCARule");
		this.getActionMap().put("submitTheCARule", submitTheCARuleAction);
	}

	/**
	 * Creates each of the tabbed panes and their associated panels. Adds the
	 * various components to the panels (such as the Start and other buttons).
	 */
	private void createTabbedPane()
	{
		// create a color for the borders
		Color borderColor = Color.BLUE.darker();

		// create layouts for each panel. Must be in this order!
		analysisPanel = new AnalysisPanel(this);
		startPanel = new StartPanel(this);
		propertiesPanel = new PropertiesPanel(this);
		rulePanel = new RulePanel(this);
		initialStatesPanel = new InitialStatesPanel(this);
		descriptionPanel = new DescriptionPanel(this);
		additionalPropertiesTabPanel = new AdditionalPropertiesTabPanel(this);

		// unused, but here for legacy reasons (the getStatusPanel() needs it
		// and is used all over the place).
		statusPanel = new StatusPanel(this, borderColor);

		// create a JTabbedPane to hold the property panel, control panel, and
		// analysis panel
		tabbedPane = new JTabbedPane();

		tabbedPane.setOpaque(true);

		// listen for changes to the selected tab
		tabbedPane.addChangeListener(new TabbedPaneListener());

		// set visual appearance
		tabbedPane.setUI(new CATabbedPaneUI());
		tabbedPane.updateUI();
		Font tabFont = new Font(tabbedPane.getFont().getFontName(), Font.BOLD
				| Font.ITALIC, tabbedPane.getFont().getSize() + 1);
		tabbedPane.setFont(tabFont);
		tabbedPane.setForeground(borderColor);

		// add the tabs
		tabbedPane.addTab(RulePanel.RULE_TAB_TITLE, null, rulePanel,
				RulePanel.TOOL_TIP);
		tabbedPane.addTab(DescriptionPanel.DESCRIPTION_TAB_TITLE, null,
				descriptionPanel, DescriptionPanel.TOOL_TIP);
		tabbedPane.addTab(StartPanel.START_PANEL_TITLE, null, startPanel,
				StartPanel.TOOL_TIP);
		tabbedPane.addTab(PropertiesPanel.PROPERTY_PANEL_TITLE, null,
				propertiesPanel, PropertiesPanel.TOOL_TIP);
		tabbedPane.addTab(InitialStatesPanel.INIT_STATES_TAB_TITLE, null,
				initialStatesPanel, InitialStatesPanel.TOOL_TIP);
		tabbedPane.addTab(AnalysisPanel.ANALYSIS_PANEL_TITLE, null,
				analysisPanel, AnalysisPanel.TOOL_TIP);
		tabbedPane.addTab(
				AdditionalPropertiesTabPanel.ADDITIONAL_PROPERTIES_TAB_TITLE,
				null, additionalPropertiesTabPanel,
				AdditionalPropertiesTabPanel.TOOL_TIP);

		// always open with the start panel.
		tabbedPane.setSelectedComponent(startPanel);

		// Make the font one size smaller than the default JTabbedPane font.
		// This ensures that the More Properties tab gets it's own line which
		// ensures that when a user selects a rule, the list of rules doesn't
		// suddenly shift downwards (because the longer "More Properties..."
		// description on the tab made it jump up to a new line).
		Fonts fonts = new Fonts(tabbedPane);
		tabbedPane.setFont(fonts.getBoldSmallerFont());

		// the title and tooltip of this panel includes the rule name. So reset
		// it.
		additionalPropertiesTabPanel.setTabTitle();
		additionalPropertiesTabPanel.setTabTooltip();

		// enable or disable the additional properties tab
		additionalPropertiesTabPanel
				.enableAdditionalPropertiesTab(additionalPropertiesTabPanel
						.doesAdditionalPropertiesPanelExist());

		// make sure the additional properties buttons are flashing, if
		// appropriate
		additionalPropertiesTabPanel.resetAdditionalPropertiesButtons();

		// add the panels to the AllPanel
		this.add(tabbedPane);

		// react to specified keystrokes
		bindKeystrokes();
	}

	/**
	 * Gets the JPanel that holds the additional properties (if any) specified
	 * by the rule.
	 * 
	 * @return The panel that holds the additional properties.
	 */
	public AdditionalPropertiesTabPanel getAdditionalPropertiesPanel()
	{
		return additionalPropertiesTabPanel;
	}

	/**
	 * Gets the JPanel that holds the analysis check boxes.
	 * 
	 * @return The panel that holds the analysis check boxes.
	 */
	public AnalysisPanel getAnalysisPanel()
	{
		return analysisPanel;
	}

	/**
	 * Gets the controller of interactions between the start, status, and
	 * properties panels. Also sets values on these panels
	 * 
	 * @return The controller.
	 */
	public AllPanelController getController()
	{
		return controller;
	}

	/**
	 * The control panel listener that listens for changes to the GUI (like a
	 * "Start" button being pressed).
	 * 
	 * @return The listener.
	 */
	public AllPanelListener getAllPanelListener()
	{
		return listener;
	}

	/**
	 * Gets the CAFrame that creates the menubar, toolbar, and holds the JFrame.
	 * 
	 * @return The object that creates the JFrame and menubars.
	 */
	public CAFrame getCAFrame()
	{
		return caFrame;
	}

	/**
	 * Gets the JPanel that holds the the description of the currently active CA
	 * rule.
	 * 
	 * @return The panel that holds the description of the currently active CA
	 *         rule.
	 */
	public DescriptionPanel getDescriptionPanel()
	{
		return descriptionPanel;
	}

	/**
	 * Gets the JFrame that holds/encompasses this JPanel.
	 * 
	 * @return The frame that holds this panel.
	 */
	public JFrame getFrame()
	{
		return frame;
	}

	/**
	 * Gets the JPanel that holds the the initial state controls.
	 * 
	 * @return The panel that holds the initial states like blank, random, etc.
	 */
	public InitialStatesPanel getInitialStatesPanel()
	{
		return initialStatesPanel;
	}

	/**
	 * Gets the JPanel that holds the lattice chooser, rule chooser, and other
	 * components related to the CA properties.
	 * 
	 * @return The panel that holds the lattice and rule chooser among others.
	 */
	public PropertiesPanel getPropertiesPanel()
	{
		return propertiesPanel;
	}

	/**
	 * Gets the JPanel that holds the the rule tree and description.
	 * 
	 * @return The panel that holds the rule tree and description.
	 */
	public RulePanel getRulePanel()
	{
		return rulePanel;
	}

	/**
	 * Gets the JPanel that holds the start and stop buttons, along with other
	 * components.
	 * 
	 * @return The panel that holds the start and stop buttons.
	 */
	public StartPanel getStartPanel()
	{
		return startPanel;
	}

	/**
	 * Gets the JPanel that holds the status messages (generation, current
	 * rule).
	 * 
	 * @return The panel that holds the status messages.
	 */
	public StatusPanel getStatusPanel()
	{
		return statusPanel;
	}

	/**
	 * Gets the tabbed pane that holds the the start-stop, properties, and
	 * analysis, and other panels.
	 * 
	 * @return The tabbed pane that holds the start-stop, properties, and
	 *         analysis, and other panels.
	 */
	public JTabbedPane getTabbedPane()
	{
		return tabbedPane;
	}

	/**
	 * Listens for changes in the selected tab on the tabbed pane.
	 * 
	 * @author David Bahr
	 */
	private class TabbedPaneListener implements ChangeListener
	{
		public void stateChanged(ChangeEvent e)
		{
			if(tabbedPane.getSelectedComponent().equals(initialStatesPanel))
			{
				initialStatesPanel.getMostCommonInitialStatesPanel()
						.startPulsing(4);
			}
			else
			{
				initialStatesPanel.getMostCommonInitialStatesPanel()
						.stopPulsing();
			}

			if(tabbedPane.getSelectedComponent().equals(propertiesPanel))
			{
				int pulseAmount = 4;

				propertiesPanel.getBestResultsEditorPane().startPulsing(
						pulseAmount);
			}
			else
			{
				propertiesPanel.getBestResultsEditorPane().stopPulsing();
			}

			// only make the Property Panel's "more properties" button
			// pulsate if the button is enabled and the *properties tab* is
			// selected
			if(tabbedPane.getSelectedComponent().equals(propertiesPanel)
					&& propertiesPanel.getAdditionalPropertiesButton()
							.isEnabled())
			{
				int pulseAmount = 5;

				propertiesPanel.getAdditionalPropertiesButton().startPulsing(
						pulseAmount);
			}
			else
			{
				propertiesPanel.getAdditionalPropertiesButton().stopPulsing();
			}

			// only make the Rule Panel's "more properties" button pulsate
			// if the button is enabled and the *rule tab* is selected
			if(tabbedPane.getSelectedComponent().equals(rulePanel)
					&& propertiesPanel.getAdditionalPropertiesButton()
							.isEnabled())
			{
				int pulseAmount = 5;

				rulePanel.getAdditionalPropertiesButton().startPulsing(
						pulseAmount);
			}
			else
			{
				rulePanel.getAdditionalPropertiesButton().stopPulsing();
			}
		}
	}
}
