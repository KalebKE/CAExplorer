/*
 RulePanel -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2007  David B. Bahr (http://academic.regis.edu/dbahr/)

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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.text.html.HTMLDocument;

import cellularAutomata.CurrentProperties;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.reflection.RuleHash;
import cellularAutomata.rules.Rule;
import cellularAutomata.rules.WolframRuleNumber;
import cellularAutomata.util.CAHyperlinkListener;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.ToolTipLatticeComboBox;
import cellularAutomata.util.browser.CABrowser;
import cellularAutomata.util.files.CARuleDescriptionLoader;
import cellularAutomata.util.graphics.PulsatingTextJButton;
import cellularAutomata.util.graphics.PulsatingJTextField;

/**
 * The tab that contains the rule and its description.
 * 
 * @author David Bahr
 */
public class RulePanel extends JPanel implements ActionListener,
		DocumentListener, TreeSelectionListener, ChangeListener
{
	/**
	 * Title for the rule panel.
	 */
	public static final String RULE_TAB_TITLE = "Rule";

	/**
	 * String used for text display on the submit button and for setting its
	 * action command.
	 */
	public static final String SUBMIT_PROPERTIES = "Submit Changes";

	/**
	 * String used for text display on the enlarge button and for setting its
	 * action command.
	 */
	public static final String ENLARGE_HTML = "Enlarge Description";

	/**
	 * String used for text display on the "choose a random rule number" button
	 * and also used for setting its action command.
	 */
	public static final String RANDOM_NUMBER = "Choose Randomly";

	/**
	 * A tool tip for the rule panel.
	 */
	public static final String TOOL_TIP = "<html><body>select a rule</body></html>";

	// height of the tree panel
	private static final int HEIGHT_TREE_SCROLL_PANEL = 300;

	// height of the rule description panel
	private static final int HEIGHT_RULE_DESCRIPTION_PANEL = 200;

	// width of the rule description panel
	private static final int WIDTH_RULE_DESCRIPTION = 350;

	// the title of the rule description section
	private static final String DESCRIPTION_PANEL_TITLE = "Highlighted Rule's Description";

	// The tool tip for the random number button
	private static final String RANDOM_BUTTON_TIP = "<html>"
			+ "Selects a random rule number within the allowed range. <br><br>"
			+ "(In practice, the random numbers are limited to "
			+ AllPanelController.MAX_RANDOM_NUM_BITS + " bits <br> "
			+ "to prevent problems displaying.)</html>";

	// The title for the rule number panel
	private static final String RULE_NUMBER_PANEL_TITLE = "Rule number";

	// the minimum width of the rule number text field
	private static final int RULE_NUMBER_TEXT_FIELD_MIN_WIDTH = 5;

	// The tooltip for the rule number
	private static final String RULE_NUMBER_TIP = "<html>Choose a rule by "
			+ "its number." + "</html>";

	// the title of the rule section
	private static final String RULE_PANEL_TITLE = "Rule";

	// The encompassing panel onto which this one will be added.
	private AllPanel outerPanel = null;

	// A listener for components on this panel.
	private AllPanelListener listener = null;

	// default color for the submit button text
	private Color defaultSubmitButtonColor = Color.gray;

	// color for the titles of sections
	private Color titleColor = Color.BLUE;

	// title font (for titles of sections)
	private Font titleFont = null;

	// fonts for display
	private Fonts fonts = null;

	// the button that enlarges the html
	private JButton enlargeButton = null;

	// The random rule number buttom
	private JButton randomButton = null;

	// Submit rule choice
	private JButton submitButton = null;

	// the editor pane used to display the html description of the rule
	private JEditorPane editorPane = null;

	// the frame used to display a larger version of the html.
	private CABrowser enlargedFrame = null;

	// the rule's description panel
	private JPanel descriptionPanel = null;

	// The inner panel that holds all of the buttons
	private JPanel innerRaisedPanel = null;

	// the scroll panel that holds the rule description
	private JScrollPane editorScrollPane = null;

	// the scroll panel that holds the rule menu tree
	private JScrollPane ruleTreeScrollPanel = null;

	// set the rule number for the CA.
	private PulsatingJTextField ruleNumberField = null;

	// the tree used to select rules
	private JTree treeOfRules = null;

	// button for additional properties
	private PulsatingTextJButton additionalPropertiesButton = null;

	// the class which builds the tree used to select the rules
	private RuleTree ruleTree = null;

	// the rule name that was selected before the current selection
	private String previouslySelectedRuleName = null;

	/**
	 * The panel containing the rule, and rule description.
	 * 
	 * @param outerPanel
	 *            The encompassing panel onto which this one will be added.
	 */
	public RulePanel(AllPanel outerPanel)
	{
		super();

		this.outerPanel = outerPanel;
		this.listener = outerPanel.getAllPanelListener();

		this.setOpaque(true);

		// fonts for the components (buttons, etc.)
		fonts = new Fonts();
		titleFont = new Fonts().getItalicSmallerFont();

		// add the components
		addComponents();

		// set the color of the submit button (it may have been changed during
		// start-up by some component listeners)
		submitButton.setForeground(this.defaultSubmitButtonColor);

		// react to specified keystrokes
		bindKeystrokes();
	}

	/**
	 * Create the panel that holds the lattice chooser, rule chooser, etc.
	 */
	private void addComponents()
	{
		// in case this has been called before, clear it out.
		this.removeAll();

		// create the inner panel
		innerRaisedPanel = createInnerPanel();
		JScrollPane innerScrollPanel = new JScrollPane(innerRaisedPanel);
		innerScrollPanel.setBorder(BorderFactory.createEmptyBorder());
		innerScrollPanel
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		int width = CAFrame.tabbedPaneDimension.width
				- innerScrollPanel.getInsets().left
				- innerScrollPanel.getInsets().right;
		int height = innerRaisedPanel.getMinimumSize().height;
		innerRaisedPanel.setPreferredSize(new Dimension(width, height));

		// create a button for submitting properties
		JPanel submitPanel = createSubmitButtonPanel();

		// create a layout
		this.setLayout(new GridBagLayout());
		this.add(innerScrollPanel, new GBC(0, 1).setSpan(1, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		this.add(submitPanel, new GBC(0, 2).setSpan(1, 1).setFill(
				GBC.HORIZONTAL).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));
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
				submitButton.doClick();
			}
		};

		this.getInputMap(
				javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0),
				"submitTheCARule");
		this.getActionMap().put("submitTheCARule", submitTheCARuleAction);
	}

	/**
	 * Creates a panel holding the rule's description.
	 * 
	 * @return A panel holding an html description of the rule.
	 */
	private JPanel createRuleDescriptionPanel()
	{
		// get the url to the description text
		URL url = CARuleDescriptionLoader.getURLFromRuleName(ruleTree
				.getSelectedRuleName());

		if(url != null)
		{
			try
			{
				editorPane = new JEditorPane(url);
			}
			catch(Exception error)
			{
				url = null;
			}
		}

		if(url == null)
		{
			// oops. Use the tooltip instead.

			// get the description text from the tooltip
			String description = getRuleDescriptionFromToolTip();
			// put it in an editor pane
			editorPane = new JEditorPane("text/html", description);
		}

		// add a hyperlink listener (it's a private inner class)
		editorPane.addHyperlinkListener(new CAHyperlinkListener(editorPane));

		// put editor pane in a scroll bar
		editorPane.setEditable(false);
		editorScrollPane = new JScrollPane(editorPane);
		editorScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		// and set to the beginning of the text so the scroll bar is
		// positioned properly
		editorPane.setCaretPosition(0);

		// set the size -- the editor pane has to be slightly larger than the
		// scroll pane or the scroll bars won't show up.
		editorPane.setPreferredSize(new Dimension(WIDTH_RULE_DESCRIPTION,
				HEIGHT_RULE_DESCRIPTION_PANEL));
		Dimension scrollPaneDimension = new Dimension(editorPane
				.getPreferredScrollableViewportSize().width,
				HEIGHT_RULE_DESCRIPTION_PANEL);
		editorScrollPane.setPreferredSize(scrollPaneDimension);
		editorScrollPane.setMinimumSize(scrollPaneDimension);
		editorScrollPane.setMaximumSize(scrollPaneDimension);

		// create button to show enlarged editor pane
		enlargeButton = new JButton(ENLARGE_HTML);
		enlargeButton.setFont(fonts.getBoldSmallerFont());
		enlargeButton.setActionCommand(ENLARGE_HTML);
		enlargeButton.addActionListener(listener);

		// create the panel that holds the editor pane
		JPanel descriptionPanel = new JPanel(new GridBagLayout());
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		Border innerEmptyBorder = BorderFactory.createEmptyBorder(2, 6, 3, 2);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), DESCRIPTION_PANEL_TITLE,
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		descriptionPanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder, (BorderFactory.createCompoundBorder(
						titledBorder, innerEmptyBorder))));
		descriptionPanel.setBorder(titledBorder);

		// add the scroll pane to the JPanel
		int row = 0;
		descriptionPanel.add(editorScrollPane, new GBC(1, row).setSpan(3, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
				.setInsets(1));
		row++;
		descriptionPanel.add(enlargeButton, new GBC(2, row).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(0.0, 0.0).setAnchor(GBC.CENTER)
				.setInsets(1));

		return descriptionPanel;
	}

	/**
	 * Create an additional properties button and put in a JPanel.
	 * 
	 * @return contains the additional properties button.
	 */
	private JPanel createAdditionalPropertiesButton()
	{
		// add button for additional properties
		additionalPropertiesButton = new PulsatingTextJButton(
				Rule.ADDITIONAL_PROPERTIES, Color.RED);
		additionalPropertiesButton.setFont(fonts.getBoldSmallerFont());
		additionalPropertiesButton.setActionCommand(Rule.ADDITIONAL_PROPERTIES);
		additionalPropertiesButton.addActionListener(listener);

		// enabling and disabling the additionalPropertiesButton is handled by
		// the AdditionalPropertiesTabPanel reset() method and the AllPanel
		// createTabbedPane() method. Ditto for the tool tip.

		// create a panel that holds the button
		FlowLayout innerLayout = new FlowLayout(FlowLayout.RIGHT);
		JPanel innerPanel = new JPanel(innerLayout);
		innerPanel.add(additionalPropertiesButton);

		return innerPanel;
	}

	/**
	 * Create and arrange the raised inner panel that holds the rule tree and
	 * description.
	 * 
	 * @param propertiesPanel
	 *            The panel holding the rule tree and description.
	 */
	private JPanel createInnerPanel()
	{
		// the panel on which we add the controls
		JPanel innerPanel = new JPanel();
		innerPanel.setBorder(BorderFactory.createRaisedBevelBorder());

		GridBagLayout layout = new GridBagLayout();
		innerPanel.setLayout(layout);

		// create panels that group them together
		JPanel rulePanel = createRulePanel();
		JPanel ruleNumberPanel = createRuleNumberPanel();
		descriptionPanel = createRuleDescriptionPanel();

		// now that we created the description panel, we can do this.
		Dimension treeScrollPanelDimension = new Dimension(editorScrollPane
				.getPreferredSize().width, HEIGHT_TREE_SCROLL_PANEL);
		ruleTreeScrollPanel.setPreferredSize(treeScrollPanelDimension);
		ruleTreeScrollPanel.setMinimumSize(treeScrollPanelDimension);
		ruleTreeScrollPanel.setMaximumSize(treeScrollPanelDimension);

		// When creating constraints for the grid bag layout, the GBC takes as
		// arguments "new GBC(gridx, gridy)" where
		// gridx = column position (for the component being added)
		// gridy = row position
		//
		// Use .setSpan(gridwidth, gridheight) to set span of a component where
		// gridwidth = number of columns the component should span
		// gridheight = number of rows the component should span
		//
		// Use .setFill to set a constant that decides whether the component
		// should span/fill the cell. Can make it span vertically, horizontally,
		// both or neither.
		//
		// Use setWeight(weightx, weighty) to specify how much an area can grow
		// or shrink when resizing. 0 means no resizing of the cell area. If
		// all cells have the same weight, then they resize proportionally.
		// If one cell has half of the total weight in a whole row, then that
		// cell will resize twice as much as the other cells. If three cells
		// have weights x, y, z, then cell x will resize proportionally as
		// x/(x+y+z), and cell y will resize proportionally as y/(x+y+z), etc.

		// lattice panel
		int row = 0;
		innerPanel.add(rulePanel, new GBC(1, row).setSpan(8, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		innerPanel.add(ruleNumberPanel, new GBC(1, row).setSpan(8, 1).setFill(
				GBC.BOTH).setWeight(1.0, 0.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		innerPanel.add(descriptionPanel, new GBC(1, row).setSpan(8, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		return innerPanel;
	}

	/**
	 * Create a panel holding the rule tree .
	 * 
	 * @return The panel with the rule tree.
	 */
	private JPanel createRulePanel()
	{
		// component
		ruleTreeScrollPanel = createRuleTree();

		JPanel rulePanel = new JPanel();
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		Border innerEmptyBorder = BorderFactory.createEmptyBorder(2, 6, 3, 2);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), RULE_PANEL_TITLE, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		rulePanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder, (BorderFactory.createCompoundBorder(
						titledBorder, innerEmptyBorder))));
		rulePanel.setLayout(new GridBagLayout());

		// rule tree
		int row = 0;
		rulePanel.add(ruleTreeScrollPanel, new GBC(1, row).setSpan(8, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		return rulePanel;
	}

	/**
	 * The menu tree used to select rules. (Note that the RuleTree class has to
	 * create the scroll pane so that the tree is registered with the scroll
	 * panel and knows how to automatically scroll.)
	 * 
	 * @return a scroll pane holding the menu tree used to select the rule.
	 */
	private JScrollPane createRuleTree()
	{
		ruleTree = new RuleTree(listener);
		treeOfRules = ruleTree.getTree();

		// needed when listening for tree selections so don't highlight the
		// submit button in red when the same rule was selected
		previouslySelectedRuleName = ruleTree.getSelectedRuleName();

		// listen for changes in the selection so can highlight the submit
		// button in red
		treeOfRules.addTreeSelectionListener(this);

		return ruleTree.getRuleTreeAsScrollPane();
	}

	/**
	 * Create text field for choosing a one-dimensional rule (a la Wolfram).
	 * 
	 * @return Field for the rule number.
	 */
	private PulsatingJTextField createRuleNumberField()
	{
		// add text field for choosing the rule number
		// First get the rule number
		String ruleNumber = CurrentProperties.getInstance().getRuleNumber()
				.toString();
		if(ruleNumber == null)
		{
			ruleNumber = "" + WolframRuleNumber.DEFAULT_RULE;
		}

		// now set the text field (with a minimum size -- the layout may scale
		// it larger)
		PulsatingJTextField ruleNum = new PulsatingJTextField(ruleNumber);
		ruleNum.setColumns(RULE_NUMBER_TEXT_FIELD_MIN_WIDTH);
		ruleNum.setHorizontalAlignment(JTextField.RIGHT);
		ruleNum.setToolTipText(RULE_NUMBER_TIP);
		ruleNum.setForeground(Color.RED);

		// add listeners
		ruleNum.getDocument().addDocumentListener(listener);
		ruleNum.getDocument().addDocumentListener(this);

		return ruleNum;
	}

	// /**
	// * Create a spinner for choosing a rule by its number (a la Wolfram).
	// *
	// * @return a spinner for the rule number.
	// */
	// private JSpinner createRuleNumberSpinner()
	// {
	// // get the rule number
	// String ruleNumber = properties
	// .getProperty(CAPropertyReader.RULE_NUMBER);
	// if(ruleNumber == null)
	// {
	// ruleNumber = "" + WolframRuleNumber.DEFAULT_RULE;
	// }
	//
	// MinMaxBigIntPair minmaxRule = getMinMaxAllowedRuleNumbers();
	//
	// BigInteger startValue = new BigInteger(ruleNumber);
	// BigInteger maxRule = null;
	// BigInteger minRule = null;
	//
	// if(minmaxRule != null)
	// {
	// maxRule = minmaxRule.max;
	// minRule = minmaxRule.min;
	// }
	//
	// // make sure the minRule <= currentValue <= maxRule
	// if(maxRule != null && startValue.compareTo(minmaxRule.max) > 0)
	// {
	// startValue = new BigInteger(maxRule.toString());
	// }
	// else if(minRule != null && startValue.compareTo(minmaxRule.min) < 0)
	// {
	// startValue = new BigInteger(minRule.toString());
	// }
	//
	// // now set the spinner
	// SpinnerNumberModel ruleModel = new BigIntegerSpinnerModel(startValue,
	// minRule, maxRule, BigInteger.ONE);
	// BigIntegerSpinner ruleNumberField = new BigIntegerSpinner(ruleModel);
	// ruleNumberField.setToolTipText(RULE_NUMBER_TIP);
	//
	// // limit the size
	// // ((JSpinner.NumberEditor) ruleNumberField.getEditor()).getTextField()
	// // .setColumns(14);
	// ((BigIntegerSpinner.DefaultEditor) ruleNumberField.getEditor())
	// .getTextField().setColumns(14);
	//
	// // add listeners
	// ruleNumberField.addChangeListener(listener);
	// ruleNumberField.addChangeListener(this);
	// // ((JSpinner.NumberEditor) ruleNumberField.getEditor()).getTextField()
	// // .getDocument().addDocumentListener(listener);
	// // ((JSpinner.NumberEditor) ruleNumberField.getEditor()).getTextField()
	// // .getDocument().addDocumentListener(this);
	// ((BigIntegerSpinner.DefaultEditor) ruleNumberField.getEditor())
	// .getTextField().getDocument().addDocumentListener(listener);
	// ((BigIntegerSpinner.DefaultEditor) ruleNumberField.getEditor())
	// .getTextField().getDocument().addDocumentListener(this);
	//
	// ruleNumberField.setForeground(Color.RED);
	//
	// return ruleNumberField;
	// }

	/**
	 * Create a panel holding rule number field.
	 * 
	 * @return The panel with the rule number field.
	 */
	private JPanel createRuleNumberPanel()
	{
		// components
		ruleNumberField = createRuleNumberField();
		randomButton = createRuleNumberRandomButton();

		// label for the rule number
		JLabel ruleNumLabel = new JLabel("Rule number:   ");

		JPanel ruleNumberPanel = new JPanel();
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		Border innerEmptyBorder = BorderFactory.createEmptyBorder(2, 6, 3, 2);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), RULE_NUMBER_PANEL_TITLE,
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		ruleNumberPanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder, (BorderFactory.createCompoundBorder(
						titledBorder, innerEmptyBorder))));
		ruleNumberPanel.setLayout(new GridBagLayout());

		int row = 0;
		ruleNumberPanel.add(ruleNumLabel, new GBC(1, row).setSpan(1, 1)
				.setFill(GBC.BOTH).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));
		ruleNumberPanel.add(ruleNumberField, new GBC(2, row).setSpan(6, 1)
				.setFill(GBC.HORIZONTAL).setWeight(1.0, 1.0)
				.setAnchor(GBC.EAST).setInsets(1));
		row++;
		ruleNumberPanel.add(randomButton, new GBC(2, row).setSpan(6, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		return ruleNumberPanel;
	}

	/**
	 * Creates a button that chooses a random rule number.
	 * 
	 * @return The button that chooses a random rule number.
	 */
	private JButton createRuleNumberRandomButton()
	{
		// add button for selecting random rule number
		JButton randomNumberButton = new JButton(RANDOM_NUMBER);
		randomNumberButton.setFont(fonts.getBoldVerySmallFont()); // getSmallerBoldFont());
		randomNumberButton.setToolTipText(RANDOM_BUTTON_TIP);
		randomNumberButton.setActionCommand(RANDOM_NUMBER);
		randomNumberButton.addActionListener(listener);

		// this lets the submit button know that it needs to change color after
		// this random button is pressed
		randomNumberButton.addActionListener(this);

		return randomNumberButton;
	}

	/**
	 * Create a submit button and puts in a JPanel.
	 * 
	 * @return contains the submit button.
	 */
	private JPanel createSubmitButton()
	{
		// add button for submitting properties
		submitButton = new JButton(SUBMIT_PROPERTIES);
		submitButton.setFont(fonts.getBoldSmallerFont());
		submitButton.setActionCommand(SUBMIT_PROPERTIES);
		submitButton.addActionListener(listener);

		// save the default color for later
		defaultSubmitButtonColor = submitButton.getForeground();

		// create a panel that holds the button
		FlowLayout innerLayout = new FlowLayout(FlowLayout.RIGHT);
		JPanel innerPanel = new JPanel(innerLayout);
		innerPanel.add(submitButton);

		return innerPanel;
	}

	/**
	 * Create a submit button for rule changes.
	 * 
	 * @return The submit button in a panel.
	 */
	private JPanel createSubmitButtonPanel()
	{
		// create a JPanel with a submit button for submitting property changes
		JPanel submitPanel = createSubmitButton();

		// create a JPanel with an additional properties button
		JPanel morePropertiesPanel = createAdditionalPropertiesButton();

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(submitPanel, BorderLayout.EAST);
		bottomPanel.add(morePropertiesPanel, BorderLayout.WEST);

		return bottomPanel;
	}

	/**
	 * Finds the min and max allowed rule numbers for the currently selected
	 * rule, lattice, and number of states.
	 * 
	 * @return the min and max rule numbers.
	 */
	// private MinMaxBigIntPair getMinMaxAllowedRuleNumbers()
	// {
	// // get the lattice
	// JComboBox latticeChooser = outerPanel.getPropertiesPanel()
	// .getLatticeChooser();
	// String latticeChoice = (String) latticeChooser.getSelectedItem();
	//
	// // get the number of states
	// JTextField numStates = outerPanel.getPropertiesPanel()
	// .getNumStatesField();
	// int numOfStates = 2;
	// try
	// {
	// numOfStates = Integer.parseInt(numStates.getText());
	// if(numOfStates < IntegerCellState.MIN_NUM_STATES
	// || numOfStates > IntegerCellState.MAX_NUM_STATES)
	// {
	// throw new NumberFormatException();
	// }
	// }
	// catch(Exception e)
	// {
	// numOfStates = 2;
	// }
	//
	// // get the min and max allowed values
	// MinMaxBigIntPair minmaxRule = IntegerRule.getMinMaxRuleNumberAllowed(
	// latticeChoice, ruleTree.getSelectedRuleName(), numOfStates,
	// properties);
	//
	// return minmaxRule;
	// }
	/**
	 * Gets the rule description from the rule.
	 * 
	 * @return The html description.
	 */
	private String getRuleDescriptionFromToolTip()
	{
		// get the rule
		String ruleClassName = CurrentProperties.getInstance()
				.getRuleClassName();
		if(ruleTree != null)
		{
			String ruleDisplayName = ruleTree.getSelectedRuleName();

			if(ruleDisplayName != null && !ruleDisplayName.equals(""))
			{
				RuleHash ruleHash = new RuleHash();
				ruleClassName = ruleHash.get(ruleDisplayName);
			}
		}
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(ruleClassName);

		// get the tooltip
		String text = null;

		if(rule != null)
		{
			text = rule.getToolTipDescription();
		}

		if(text == null)
		{
			text = "<html><body>No description available.</body></html>";
		}

		return text;
	}

	/**
	 * Decides if a spinner has changed it's text value. This only works when
	 * the user has typed a change. If the user has pushed the up or down
	 * buttons, then that must be caught with a stateChanged method.
	 * <p>
	 * Note that this method is necessary because a simple cursor being placed
	 * in the text field will fire an event, but obviously that shouldn't
	 * indicate a change in the text.
	 * 
	 * @param spinner
	 *            The spinner that may have changed.
	 * @return true if the spinner has changed it's value.
	 */
	// private boolean hasSpinnerChanged(JSpinner spinner)
	// {
	// // the value that will be returned (will be set to false if no change
	// // has occurred)
	// boolean hasChanged = true;
	//
	// JTextField textField = ((BigIntegerSpinner.DefaultEditor) spinner
	// .getEditor()).getTextField();
	// // JFormattedTextField textField = ((JSpinner.NumberEditor) spinner
	// // .getEditor()).getTextField();
	//
	// // make sure the spinner value has changed -- this deals with the
	// // annoying possibility that a DocumentEvent was fired just because a
	// // cursor was placed in the text field of the JSpinner.
	// if(textField.isFocusOwner())
	// {
	// String displayedValue = textField.getText();
	// String lastValidValue = spinner.getValue().toString();
	//
	// if(displayedValue == null || lastValidValue == null
	// || displayedValue.equals("") || lastValidValue.equals("")
	// || displayedValue.equals(lastValidValue))
	// {
	// // value has not changed, so do nothing
	// hasChanged = false;
	// }
	// }
	// else
	// {
	// hasChanged = false;
	// }
	//
	// return hasChanged;
	// }
	/**
	 * Creates an enlarged view of the rule's html description. This method is
	 * called by the listener to the enlarge button (AllPanelListener).
	 */
	public void createEnlargedHTMLView()
	{
		// don't create the frame if already have one
		if(enlargedFrame == null)
		{
			if(editorPane.getPage() != null)
			{
				enlargedFrame = new CABrowser(editorPane.getPage(), false,
						editorScrollPane)
				{
					// override this method
					public void actionAfterShrinking()
					{
						// unpause the simulation.
						outerPanel.getCAFrame().getMenuBar()
								.firePropertyChangeEvent(
										new PropertyChangeEvent(this,
												CurrentProperties.PAUSE, null,
												new Boolean(false)));
					}

					// override this method
					public void actionBeforeShrinking()
					{
						// pause the simulation.
						outerPanel.getCAFrame().getMenuBar()
								.firePropertyChangeEvent(
										new PropertyChangeEvent(this,
												CurrentProperties.PAUSE, null,
												new Boolean(true)));
					}
				};
			}
			else
			{
				enlargedFrame = new CABrowser(editorPane.getText(), false,
						editorScrollPane)
				{
					// override this method
					public void actionAfterShrinking()
					{
						// unpause the simulation.
						outerPanel.getCAFrame().getMenuBar()
								.firePropertyChangeEvent(
										new PropertyChangeEvent(this,
												CurrentProperties.PAUSE, null,
												new Boolean(false)));
					}

					// override this method
					public void actionBeforeShrinking()
					{
						// pause the simulation.
						outerPanel.getCAFrame().getMenuBar()
								.firePropertyChangeEvent(
										new PropertyChangeEvent(this,
												CurrentProperties.PAUSE, null,
												new Boolean(true)));
					}
				};
			}

			// make the animation for closing the frame take this long (in
			// milliseconds)
			enlargedFrame.setAnimationLength(500);

			enlargedFrame.setTitle(DESCRIPTION_PANEL_TITLE);
		}
		else
		{
			enlargedFrame.setVisible(true);

			// give it focus if it already exists
			enlargedFrame.requestFocus();
		}

		// set the position at the top of the page
		enlargedFrame.setScrollBarToTop();
	}

	/**
	 * Gets the additional properties button.
	 * 
	 * @return The button that displays the additional properties.
	 */
	public PulsatingTextJButton getAdditionalPropertiesButton()
	{
		return additionalPropertiesButton;
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
	 * Only some rules will work with a given lattice.
	 * 
	 * @param currentLattice
	 *            The currently selected lattice. May be null if unknown.
	 * @return A list of rules that work with the current lattice.
	 */
	public String[] getPermissableRules(String currentLattice)
	{
		// the currently selected lattice
		if(currentLattice == null)
		{
			currentLattice = (String) outerPanel.getPropertiesPanel()
					.getLatticeChooser().getSelectedItem();
		}

		// will be a list of the rules compatible with this lattice
		// depends on the CellState returned by the rule.
		ArrayList ruleList = new ArrayList();

		// all the possible rules
		RuleHash ruleHash = new RuleHash();
		Iterator values = ruleHash.valuesIterator();

		// now see which ones are compatible with the lattice
		while(values.hasNext())
		{
			String nextRule = (String) values.next();

			// instantiate the rule using reflection
			Rule rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(nextRule);

			// make sure it really was instantiated!
			if(rule != null)
			{
				String[] lattice = rule.getCompatibleLattices();
				String description = rule.getDisplayName();

				// null is normal
				if(lattice == null)
				{
					// when null, is compatible with all lattices
					ruleList.add(description);
				}
				else
				{
					// If not null, then is only compatible with a subset of the
					// lattices. See if rule is compatible with this lattice.
					ArrayList latticeList = new ArrayList(Arrays
							.asList(lattice));
					if(latticeList.contains(currentLattice))
					{
						ruleList.add(description);
					}
				}
			}
		}

		// convert to an array
		String[] rules = new String[ruleList.size()];
		for(int i = 0; i < ruleList.size(); i++)
		{
			rules[i] = (String) ruleList.get(i);
		}

		// sort the array (looks better)
		Arrays.sort(rules);

		return rules;
	}

	/**
	 * Gets the wrapper class for the rule tree menu (for choosing the current
	 * CA rule).
	 * 
	 * @return The wrapper class for the tree used to select the current rule.
	 */
	public RuleTree getRuleTree()
	{
		return ruleTree;
	}

	/**
	 * Gets the button that generfrates a random rule number.
	 * 
	 * @return The rule number random button.
	 */
	public JButton getRuleNumberRandomButton()
	{
		return randomButton;
	}

	/**
	 * Gets the spinner holding the rule number.
	 * 
	 * @return The rule number spinner.
	 */
	public PulsatingJTextField getRuleNumberTextField()
	{
		return ruleNumberField;
	}

	/**
	 * Gets the submit button.
	 * 
	 * @return The submit button.
	 */
	public JButton getSubmitButton()
	{
		return submitButton;
	}

	/**
	 * Resets the value of every component on the panel by reading their values
	 * from the properties.
	 */
	public void reset()
	{
		// now reset each element one by one
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(CurrentProperties
						.getInstance().getRuleClassName());

		// scroll to the rule that was imported, unless it has already been
		// selected on the rule tree.
		if(ruleTree.getSelectedRuleName() == null)
		{
			ruleTree.setSelectedRule(rule.getDisplayName());
		}
		else if(!ruleTree.getSelectedRuleName().equals(rule.getDisplayName()))
		{
			ruleTree.setSelectedRule(rule.getDisplayName());
		}

		ruleNumberField.setText(CurrentProperties.getInstance().getRuleNumber()
				.toString());

		outerPanel.getController().disableRuleNumberField(true);

		// reset the color of the submit button
		resetSubmitButtonColorToDefault();
	}

	/**
	 * Sets the submit button to its default color.
	 */
	public void resetSubmitButtonColorToDefault()
	{
		if(submitButton != null)
		{
			submitButton.setForeground(this.defaultSubmitButtonColor);
		}
	}

	/* ********************************************************************* */
	/*
	 * THE FOLLOWING METHODS REACT TO CHANGES IN PROPERTIES BY TELLING THE
	 * SUBMIT BUTTON TO CHANGE COLORS.
	 */

	/**
	 * Reacts to a change in any registered component.
	 */
	public void actionPerformed(ActionEvent e)
	{
		Object oSource = e.getSource();
		if(oSource != null && submitButton != null && randomButton != null)
		{
			submitButton.setForeground(Color.RED);
		}
	}

	/**
	 * Reacts to a change in any registered component.
	 */
	public void insertUpdate(DocumentEvent e)
	{
		// do one of the components have focus?
		boolean hasFocus = ruleNumberField.hasFocus();

		Object oSource = e.getDocument();
		if(oSource != null && submitButton != null && hasFocus)
		{
			submitButton.setForeground(Color.RED);
		}
	}

	/**
	 * Reacts to a change in any registered component.
	 */
	public void removeUpdate(DocumentEvent e)
	{
		insertUpdate(e);
	}

	/**
	 * Reacts to a change in any registered component.
	 */
	public void changedUpdate(DocumentEvent e)
	{
		// Plain text components do not fire these events
	}

	/**
	 * Reacts to a change in spinners caused by pressing the up or down arrows.
	 */
	public void stateChanged(ChangeEvent e)
	{
		Object oSource = e.getSource();

		if(oSource != null && submitButton != null)
		{
			submitButton.setForeground(Color.RED);
		}
	}

	// /**
	// * Updates the min and max allowed rule numbers on the spinner. Makes sure
	// * that the value shown on the spinner is between these min and max
	// values.
	// */
	// public void updateRuleSpinnerMinMax()
	// {
	// MinMaxBigIntPair minmaxRule = getMinMaxAllowedRuleNumbers();
	// if(minmaxRule != null)
	// {
	// // make sure the min <= currentValue <= max
	// BigInteger currentValue = new BigInteger(ruleNumberField.getText());
	// if(currentValue.compareTo(minmaxRule.max) > 0)
	// {
	// ((BigIntegerSpinnerModel) ruleNumberField.getModel())
	// .setValue(minmaxRule.max);
	// }
	// else if(currentValue.compareTo(minmaxRule.min) < 0)
	// {
	// ((BigIntegerSpinnerModel) ruleNumberField.getModel())
	// .setValue(minmaxRule.min);
	// }
	//
	// // change the min and max
	// ((BigIntegerSpinnerModel) ruleNumberField.getModel())
	// .setMinimum(minmaxRule.min);
	// ((BigIntegerSpinnerModel) ruleNumberField.getModel())
	// .setMaximum(minmaxRule.max);
	//
	// // // make sure the min <= currentValue <= max
	// // BigInteger currentValue = (BigInteger) ruleNumberField.getValue();
	// // if(currentValue.compareTo(minmaxRule.max) > 0)
	// // {
	// // currentValue = new BigInteger(minmaxRule.max.toString());
	// // }
	// // else if(currentValue.compareTo(minmaxRule.min) < 0)
	// // {
	// // currentValue = new BigInteger(minmaxRule.min.toString());
	// // }
	// //
	// // // change the min and max
	// // ((BigIntegerSpinnerModel) ruleNumberField.getModel())
	// // .setMinimum(minmaxRule.min);
	// // ((BigIntegerSpinnerModel) ruleNumberField.getModel())
	// // .setMaximum(minmaxRule.max);
	// //
	// // // change the whole model
	// // SpinnerNumberModel ruleModel = new BigIntegerSpinnerModel(
	// // currentValue, minmaxRule.min, minmaxRule.max, BigInteger.ONE);
	// //
	// // ruleNumberField.setModel(ruleModel);
	// }
	// }

	/**
	 * Reacts to a change in the tree.
	 * 
	 * @param e
	 *            The tree selection event which may be null.
	 */
	public void valueChanged(TreeSelectionEvent e)
	{
		if(ruleTree != null && ruleTree.getSelectedRuleName() != null)
		{
			// only do this if they have selected a different rule
			if(!ruleTree.getSelectedRuleName().equals(
					previouslySelectedRuleName))
			{
				// change the submit button color
				submitButton.setForeground(Color.RED);
				previouslySelectedRuleName = ruleTree.getSelectedRuleName();

				// and update the description
				URL url = CARuleDescriptionLoader.getURLFromRuleName(ruleTree
						.getSelectedRuleName());

				if(url != null)
				{
					try
					{
						editorPane.setPage(url);
					}
					catch(Exception error)
					{
						url = null;
					}

					if(enlargedFrame != null)
					{
						enlargedFrame.setURL(url);
					}
				}

				if(url == null)
				{
					// this forces a new page to load rather than just
					// replacing the text of the old one
					editorPane.setDocument(editorPane.getEditorKit()
							.createDefaultDocument());

					editorPane.setText(getRuleDescriptionFromToolTip());

					if(enlargedFrame != null)
					{
						enlargedFrame.setText(getRuleDescriptionFromToolTip());
					}
				}

				// and set to the beginning of the text so the scrollbar is
				// positioned properly
				editorPane.setCaretPosition(0);
				if(enlargedFrame != null)
				{
					enlargedFrame.setScrollBarToTop();
				}

				// and update the "for best results" panel on the Properties tab
				outerPanel.getPropertiesPanel().setForBestResultsText(
						ruleTree.getSelectedRuleName());

				// and update the list of enabled lattices on the properties
				// panel. In other words, disable lattices that are incompatible
				// with the rule selected on the rule tree
				RuleHash ruleHash = new RuleHash();
				String ruleClassName = ruleHash.get(ruleTree
						.getSelectedRuleName());
				ToolTipLatticeComboBox latticeChooser = (ToolTipLatticeComboBox) outerPanel
						.getPropertiesPanel().getLatticeChooser();
				latticeChooser.enableOnlyCompatibleLattices(ruleClassName);

				// and update the initial states panel
				outerPanel.getInitialStatesPanel().resetActiveRadioButton();

				// and update the "more properties panel"
				// i.e., reset the tabbed pane for any additional properties
				// requested by the rule (and turn on the additional
				// properties button). (Also turns on the additional
				// properties button on the rule panel.)
				AdditionalPropertiesTabPanel additionalPropertiesPanel = outerPanel
						.getAdditionalPropertiesPanel();
				additionalPropertiesPanel.reset();

				// in EZ facade mode, disable the additional properties
				// panel and disable the rule number
				if(CurrentProperties.getInstance().isFacadeOn())
				{
					// disable the additional properties
					for(int index = 0; index < outerPanel.getTabbedPane()
							.getTabCount(); index++)
					{
						if(outerPanel
								.getTabbedPane()
								.getTitleAt(index)
								.contains(
										AdditionalPropertiesTabPanel.ADDITIONAL_PROPERTIES_TAB_TITLE))
						{
							outerPanel.getTabbedPane().setEnabledAt(index,
									false);
						}
					}

					// disable the rule number and random rule number button
					getRuleNumberTextField().setEnabled(false);
					getRuleNumberRandomButton().setEnabled(false);
				}

				// Now fire a mousePressed event so that the
				// AllPanelListener.mousePressed() method does its stuff (like
				// disable numStates, enable rule number, etc.).
				// Programmatically "single clicks" on the tree (the -1's for
				// the x and y position of the click let the receiving listener
				// know that this was a programmatic event.
				//
				// NOTE: without this, a down arrow that selects a rule won't
				// correctly disable and enable various things like the rule #.
				// Unfortunately, if it is a mouse pressed event instead of a up
				// or down arrow selection, then this will call the
				// mousePressed() method twice (once here and once for the
				// actual mouse pressed event). If I ever feel like refactoring,
				// I can move the mousePressed code into here, but I'll need to
				// check for other places that call mousePressed
				// programmatically because they would need to call this method
				// instead.
				MouseEvent mouseEvent = new MouseEvent(ruleTree.getTree(),
						MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
						MouseEvent.BUTTON1_MASK, -1, -1, 1, false);
				outerPanel.getAllPanelListener().mousePressed(mouseEvent);
			}
		}
	}

	/**
	 * Reacts to a change in the tree.
	 */
	public void treeValueChanged()
	{
		valueChanged(null);
	}

	/* ********************************************************************* */

	/**
	 * A listener for hyperlink events. Used by the JEditorPanes that hold the
	 * rule's html description.
	 */
	private class RuleDescriptionHyperLinkListener implements HyperlinkListener
	{
		/**
		 * What to do when a hyperlink is clicked.
		 * 
		 * @param e
		 */
		public void hyperlinkUpdate(HyperlinkEvent e)
		{
			if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
			{
				JEditorPane pane = (JEditorPane) e.getSource();
				if(e instanceof HTMLFrameHyperlinkEvent)
				{
					HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
					HTMLDocument doc = (HTMLDocument) pane.getDocument();
					doc.processHTMLFrameHyperlinkEvent(evt);
				}
				else
				{
					try
					{
						pane.setPage(e.getURL());
					}
					catch(Throwable t)
					{
						// do nothing
					}
				}
			}
		}
	}
}
