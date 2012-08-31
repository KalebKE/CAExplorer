/*
 AdditionalPropertiesTabPanel -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.reflection.RuleHash;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;
import cellularAutomata.util.graphics.PulsatingTextJButton;

/**
 * The panel that contains any additional properties specified by the author of
 * the CA rule.
 * 
 * @author David Bahr
 */
public class AdditionalPropertiesTabPanel extends JPanel
{
	/**
	 * Title for the additional properties tab.
	 */
	public static final String ADDITIONAL_PROPERTIES_TAB_TITLE = "More Properties";

	/**
	 * A tool tip for the tabbed pane.
	 */
	public static final String TOOL_TIP = "<html><body>additional properties </body></html>";

	/**
	 * Title for the additional properties tab when the tabbed pane is not
	 * enabled. Note this MUST contain the text of
	 * ADDITIONAL_PROPERTIES_TAB_TITLE, or other code won't know which panel
	 * this is (when finding the index of this tab).
	 */
	private static final String NO_ADDITIONAL_PROPERTIES_TAB_TITLE = "No "
			+ ADDITIONAL_PROPERTIES_TAB_TITLE;

	/**
	 * A tool tip for the tabbed pane when it is not enabled.
	 */
	private static final String NOT_ENABLED_TOOL_TIP = "<html><body>there are no "
			+ "additional properties </body></html>";

	// max length for the tab title
	private static final int MAX_TITLE_LENGTH = 48;

	// message for the default panel when there are no additional properties
	// for a rule
	private static final String NO_ADDITIONAL_PROPERTIES_AVAILABLE = "There are no additional "
			+ "properties for the rule ";

	// the inner panel title
	private static final String ADDITIONAL_PROP_PANEL_TITLE = "More Properties for ";

	// The tooltip for the additional properties button
	private static final String MORE_PROPERTIES_DISABLED_TIP = "<html><body>Some rules have "
			+ "additional properties.</body></html>";

	// The encompassing panel onto which this one will be added.
	private AllPanel outerPanel = null;

	// true if the current rule has an additional properties panel
	private boolean additionalPropertiesPanelExists = false;

	// color for the titles of sections
	private Color titleColor = Color.BLUE;

	// title font (for titles of sections)
	private Font titleFont = null;

	// fonts for display
	private Fonts fonts = null;

	// The inner panel that holds the additional properties panel
	private JPanel innerRaisedPanel = null;

	/**
	 * The panel that contains a description of the currently active CA rule.
	 * 
	 * @param outerPanel
	 *            The encompassing panel onto which this one will be added.
	 */
	public AdditionalPropertiesTabPanel(AllPanel outerPanel)
	{
		super();

		this.outerPanel = outerPanel;

		this.setOpaque(true);

		// fonts for the components (buttons, etc.)
		fonts = new Fonts();
		titleFont = new Fonts().getItalicSmallerFont();

		// add the components
		addComponents();
	}

	/**
	 * Create the panel that holds the description.
	 */
	private void addComponents()
	{
		// in case this has been called before, clear it out.
		this.removeAll();

		// create the additional properties panel, set inside a titled border
		innerRaisedPanel = getPanelFromRule();

		// create a raised panel to hold the titled additional properties panel
		JPanel raisedPanel = new JPanel(new GridBagLayout());
		raisedPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		raisedPanel.add(innerRaisedPanel, new GBC(0, 0).setSpan(2, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(0));
		raisedPanel.setOpaque(true);

		// put the raised, titled, additional properties panel inside of a
		// scroll pane
		JScrollPane innerScrollPanel = new JScrollPane(raisedPanel);
		innerScrollPanel.setBorder(BorderFactory.createEmptyBorder());
		innerScrollPanel
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		// this ensures that an extra-wide "additional properties panel" doesn't
		// make the whole tabbed pane really wide. It keeps the tabbed pane at
		// the correct width of CAFrame.tabbedPaneDimension.width
		int width = CAFrame.tabbedPaneDimension.width;
		int height = innerScrollPanel.getPreferredSize().height;
		innerScrollPanel.setPreferredSize(new Dimension(width, height));

		// create a layout
		this.setLayout(new GridBagLayout());
		this.add(innerScrollPanel, new GBC(0, 1).setSpan(1, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
	}

	/**
	 * Creates a panel containing any additional properties relevant to a
	 * particular rule.
	 * 
	 * @param rule
	 *            The rule for which we are getting the additional properties
	 *            panel.
	 * @return The JPanel for the additional properties. May be null.
	 */
	private JPanel createAdditionalPropertiesPanel(Rule rule)
	{
		// Does the additional properties panel exist? This boolean will be
		// reset below if necessary.
		additionalPropertiesPanelExists = true;

		// the additional properties JPanel that we will get from the selected
		// rule
		JPanel panel = null;

		// the additional properties panel
		if(rule != null)
		{
			panel = rule.getAdditionalPropertiesPanel();
		}

		// create a default panel if the above didn't create one
		if(panel == null)
		{
			panel = createDefaultPanel(rule);
			additionalPropertiesPanelExists = false;
		}

		return panel;
	}

	/**
	 * Create a default panel when no other is active.
	 * 
	 * @return A default panel.
	 */
	private JPanel createDefaultPanel(Rule rule)
	{
		// create the default
		JPanel panel = new JPanel(new GridBagLayout());

		// label letting them know that no additional properties are available
		// for the rule
		MultilineLabel messageLabel = new MultilineLabel(
				NO_ADDITIONAL_PROPERTIES_AVAILABLE + rule.getDisplayName()
						+ ".");
		messageLabel.setFont(fonts.getBoldFont());
		messageLabel.setMargin(new Insets(6, 10, 2, 16));
		messageLabel.setColumns(40);

		// add the message to the JPanel
		int row = 0;
		panel.add(messageLabel, new GBC(1, row).setSpan(3, 1).setFill(GBC.BOTH)
				.setWeight(1.0, 1.0).setAnchor(GBC.CENTER).setInsets(1));

		return panel;
	}

	/**
	 * True if the additional properties panel exists for the currently active
	 * rule.
	 * 
	 * @return true if the additional properties panel exists.
	 */
	public boolean doesAdditionalPropertiesPanelExist()
	{
		return additionalPropertiesPanelExists;
	}

	/**
	 * Enables the tabbed pane that holds this panel.
	 * 
	 * @param enabled
	 *            If true, enables the tab. If false disables.
	 */
	public void enableAdditionalPropertiesTab(boolean enabled)
	{
		JTabbedPane tabbedPane = outerPanel.getTabbedPane();
		for(int index = 0; index < tabbedPane.getTabCount(); index++)
		{
			if(tabbedPane
					.getTitleAt(index)
					.contains(
							AdditionalPropertiesTabPanel.ADDITIONAL_PROPERTIES_TAB_TITLE))
			{
				tabbedPane.setEnabledAt(index, enabled);
			}
		}
	}

	/**
	 * Gets the rule currently selected on the rule tree.
	 * 
	 * @return The currently selected rule. May be null.
	 */
	private Rule getCurrentlySelectedRule()
	{
		Rule rule = null;

		// may not have created the rule tree yet, so check
		if(outerPanel.getRulePanel() != null)
		{
			// rule choices from the rule tree
			String ruleSelection = outerPanel.getRulePanel().getRuleTree()
					.getSelectedRuleName();

			if(ruleSelection != null)
			{
				// get the class name from the description
				RuleHash ruleHash = new RuleHash();
				String ruleName = ruleHash.get(ruleSelection);

				// instantiate the rule using reflection
				rule = ReflectionTool
						.instantiateFullRuleFromClassName(ruleName);
			}
		}

		return rule;
	}

	/**
	 * Get the additional properties panel from the rule.
	 * 
	 * @param propertiesPanel
	 *            The panel holding the rule tree and description.
	 */
	private JPanel getPanelFromRule()
	{
		Rule rule = getCurrentlySelectedRule();
		JPanel additionalPropertiesPanel = createAdditionalPropertiesPanel(rule);

		// a titled panel on which we add the additional properties
		JPanel innerPanel = new JPanel();
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), ADDITIONAL_PROP_PANEL_TITLE
				+ rule.getDisplayName(), TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		innerPanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder, titledBorder));
		innerPanel.setBorder(titledBorder);

		innerPanel.setLayout(new GridBagLayout());

		int row = 0;
		innerPanel.add(additionalPropertiesPanel, new GBC(1, row).setSpan(8, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		return innerPanel;
	}

	/**
	 * Gets the size of the inner raised panel so that it can be copied by other
	 * panels.
	 * 
	 * @return The size of the panel, or a size of 0 if the panel doesn't yet
	 *         exist.
	 */
	public Dimension getInnerPanelSize()
	{
		Dimension size = null;
		if(innerRaisedPanel != null)
		{
			size = innerRaisedPanel.getSize();
		}
		else
		{
			size = new Dimension(0, 0);
		}

		return size;
	}

	/**
	 * Resets the panel by reloading the additional properties.
	 */
	public void reset()
	{
		// redraw with the current "additional properties" panel
		addComponents();
		innerRaisedPanel.repaint();
		this.repaint();

		// set a title and tool tip for the tab holding this panel
		setTabTitle();
		setTabTooltip();

		// enable or disable the additional properties tab
		enableAdditionalPropertiesTab(doesAdditionalPropertiesPanelExist());

		// reset the buttons on the properties and rule panel
		resetAdditionalPropertiesButtons();
	}

	/**
	 * Enable or disable the blinking additional properties buttons on the rule
	 * and properties panel.
	 */
	public void resetAdditionalPropertiesButtons()
	{
		// first find out if there is a panel for additional properties
		if(additionalPropertiesPanelExists)
		{
			// activate the "More Properties" button because there is a panel
			// for additional properties.
			if((outerPanel.getPropertiesPanel() != null)
					&& (outerPanel.getPropertiesPanel()
							.getAdditionalPropertiesButton() != null))
			{
				PulsatingTextJButton button = outerPanel.getPropertiesPanel()
						.getAdditionalPropertiesButton();
				button.setEnabled(true);
				button.startPulsing(5);

				// Create a better rule tip.
				//
				// May not have created the rule tree yet, so check.
				if(outerPanel.getRulePanel() != null)
				{
					String ruleTip = MORE_PROPERTIES_DISABLED_TIP;

					String ruleName = outerPanel.getRulePanel().getRuleTree()
							.getSelectedRuleName();
					if(ruleName != null)
					{
						ruleTip = "<html><body>" + ruleName
								+ " has additional properties.</body></html>";
					}

					button.setToolTipText(ruleTip);
				}
			}

			// ditto for the same button on the rule panel
			if((outerPanel.getRulePanel() != null)
					&& (outerPanel.getRulePanel()
							.getAdditionalPropertiesButton() != null))
			{
				PulsatingTextJButton button = outerPanel.getRulePanel()
						.getAdditionalPropertiesButton();
				button.setEnabled(true);
				button.startPulsing(5);

				// Create a better rule tip.
				String ruleTip = MORE_PROPERTIES_DISABLED_TIP;
				String ruleName = outerPanel.getRulePanel().getRuleTree()
						.getSelectedRuleName();
				if(ruleName != null)
				{
					ruleTip = "<html><body>" + ruleName
							+ " has additional properties.</body></html>";
				}
				button.setToolTipText(ruleTip);
			}
		}
		else
		{
			// deactivate the "More Properties" button because there are no
			// additional properties.
			if((outerPanel.getPropertiesPanel() != null)
					&& (outerPanel.getPropertiesPanel()
							.getAdditionalPropertiesButton() != null))
			{
				PulsatingTextJButton button = outerPanel.getPropertiesPanel()
						.getAdditionalPropertiesButton();
				button.setEnabled(false);
				button.stopPulsing();

				// set an appropriate rule tip
				button.setToolTipText(MORE_PROPERTIES_DISABLED_TIP);
			}

			// ditto for the same button on the rule panel
			if((outerPanel.getRulePanel() != null)
					&& (outerPanel.getRulePanel()
							.getAdditionalPropertiesButton() != null))
			{
				PulsatingTextJButton button = outerPanel.getRulePanel()
						.getAdditionalPropertiesButton();
				button.setEnabled(false);
				button.stopPulsing();

				// set an appropriate rule tip
				button.setToolTipText(MORE_PROPERTIES_DISABLED_TIP);
			}
		}
	}

	/**
	 * Sets the tab title using the description from the currently selected
	 * rule.
	 */
	public void setTabTitle()
	{
		String displayName = null;

		// get the display name for the current rule
		if(outerPanel.getRulePanel() != null
				&& outerPanel.getRulePanel().getRuleTree() != null)
		{
			displayName = outerPanel.getRulePanel().getRuleTree()
					.getSelectedRuleName();
		}

		// reset the tab title
		JTabbedPane tabbedPane = outerPanel.getTabbedPane();
		if(tabbedPane != null)
		{
			for(int index = 0; index < tabbedPane.getTabCount(); index++)
			{
				if(tabbedPane.getTitleAt(index).contains(
						ADDITIONAL_PROPERTIES_TAB_TITLE))
				{
					// max length of the title on the tab
					int maxLength = MAX_TITLE_LENGTH;

					String newTitle = ADDITIONAL_PROPERTIES_TAB_TITLE;
					if(!doesAdditionalPropertiesPanelExist())
					{
						// the tabbed pane is disabled, so use a different
						// title
						newTitle = NO_ADDITIONAL_PROPERTIES_TAB_TITLE;

						// adjust the max length for the new title
						maxLength += ADDITIONAL_PROPERTIES_TAB_TITLE.length()
								- NO_ADDITIONAL_PROPERTIES_TAB_TITLE.length();
					}

					if(displayName != null)
					{
						// create a new tab title with the rule name, but don't
						// let it get too long
						newTitle += " for " + displayName;
						if(newTitle.length() > maxLength)
						{
							newTitle = newTitle.substring(0, maxLength - 3)
									+ "...";
						}
					}

					// set the new title
					tabbedPane.setTitleAt(index, newTitle);
				}
			}
		}
	}

	/**
	 * Sets the tab tool tip using the description from the currently selected
	 * rule.
	 */
	public void setTabTooltip()
	{
		String displayName = null;

		// get the display name for the current rule
		if(outerPanel.getRulePanel() != null
				&& outerPanel.getRulePanel().getRuleTree() != null)
		{
			displayName = outerPanel.getRulePanel().getRuleTree()
					.getSelectedRuleName();
		}

		// reset the tab title
		JTabbedPane tabbedPane = outerPanel.getTabbedPane();
		if(tabbedPane != null)
		{
			for(int index = 0; index < tabbedPane.getTabCount(); index++)
			{
				if(tabbedPane.getTitleAt(index).contains(
						ADDITIONAL_PROPERTIES_TAB_TITLE))
				{
					if(displayName != null)
					{
						String toolTip = TOOL_TIP;
						if(!doesAdditionalPropertiesPanelExist())
						{
							// the tabbed pane is disabled, so use a different
							// tool tip
							toolTip = NOT_ENABLED_TOOL_TIP;
						}

						// insert the rule name into the tool tip
						int bodyIndex = toolTip.toLowerCase()
								.indexOf("</body>");
						int htmlIndex = toolTip.toLowerCase()
								.indexOf("</html>");
						if(bodyIndex != -1 && htmlIndex != -1)
						{
							// Contains both html and body tags.
							// Insert the new tool tip inside the html and
							// body tags.
							String newTip = toolTip.substring(0, bodyIndex)
									+ "specific to " + displayName
									+ toolTip.substring(bodyIndex);
							tabbedPane.setToolTipTextAt(index, newTip);

						}
						else if(htmlIndex != -1)
						{
							// Contains only an html tag (and no body tag).
							// This shouldn't happen but it does.
							// Insert the new tool tip inside the html tag.
							String newTip = toolTip.substring(0, htmlIndex)
									+ "specific to " + displayName
									+ toolTip.substring(htmlIndex);
							tabbedPane.setToolTipTextAt(index, newTip);
						}
						else
						{
							// does not contain either html or body tags
							tabbedPane.setToolTipTextAt(index, toolTip
									+ "specific to " + displayName);
						}
					}
					else
					{
						tabbedPane.setToolTipTextAt(index, TOOL_TIP);
					}
				}
			}
		}
	}
}
