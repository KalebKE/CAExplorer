/*
 InitialStatesPanel -- a class within the Cellular Automaton Explorer. 
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import cellularAutomata.CAController;
import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.graphics.colors.colorChooser.ColorPatch;
import cellularAutomata.lattice.OneDimensionalLattice;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.reflection.RuleHash;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.CAFileChooser;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.ImagePreviewer;
import cellularAutomata.util.PreviewPanel;
import cellularAutomata.util.files.AllImageFilter;
import cellularAutomata.util.files.AllImageTypeReader;
import cellularAutomata.util.files.CAFileFilter;
import cellularAutomata.util.graphics.PulsatingJPanel;

/**
 * The JPanel/tab that contains all of the initial state choices such as blank,
 * random, etc.
 * 
 * @author David Bahr
 */
public class InitialStatesPanel extends JPanel implements ActionListener,
		DocumentListener, ChangeListener
{
	// NOTE: The MAX_NUMSTATES_FOR_PROBABILITY_BY_STATE is necessary because can
	// only have 512 rows in a grid bag layout. And I found that 250 is the max
	// that can be redrawn quickly enough to not be annoying.
	/**
	 * The maximum number of states allowed for the "select random percent by
	 * state" initial state option.
	 */
	public static final int MAX_NUMSTATES_FOR_PROBABILITY_BY_STATE = 250;

	/**
	 * Title for the init states tab.
	 */
	public static final String INIT_STATES_TAB_TITLE = "Initial State";

	/**
	 * String used for text display on the submit button and for setting its
	 * action command.
	 */
	public static final String SUBMIT_PROPERTIES = "Submit Changes";

	/**
	 * A tool tip for the init states tab.
	 */
	public static final String TOOL_TIP = "<html><body>select initial states "
			+ "for each cell</body></html>";

	// the minimum width of file path text fields
	private static final int FILE_PATH_TEXT_FIELD_MIN_WIDTH = 5;

	// width of panels
	private static final int PANEL_WIDTH = CAFrame.tabbedPaneDimension.width; // 340;

	// height of the random percent (by state) panel
	private static final int PROBABILITY_PANEL_HEIGHT = 100;

	// the tooltip for the data initial state browse button
	private static final String DATA_BROWSE_BUTTON_TIP = "<html>"
			+ "Browse to find a data file. The file must have the correct <br>"
			+ "number of rows, columns, and states. If rows or columns <br>"
			+ "are incorrect, the data will be padded when possible. <br>"
			+ "Extra states are ignored when possible. <br><br>"
			+ "For integer based rules, data should be formatted as:"
			+ "<pre>                                       <br>"
			+ "          0 0 0 1 0 0 0                     <br>"
			+ "          0 0 1 0 1 0 0                     <br>"
			+ "          0 1 0 0 0 1 0                     <br>"
			+ "          1 0 1 0 1 0 1                     <br>"
			+ "</pre><br>"
			+ "For non-integer based rules, try saving the simulation <br>"
			+ "and then looking at the format in that file. Data delimeters <br>"
			+ "are specified in the System menu. <br><br>"
			+ "Comments beginning with // are ignored and may be included in <br>"
			+ "the file.  Generally, .ca files are acceptable.</html>";

	// the tooltip for the data initial state file path text field
	private static final String DATA_TEXT_FIELD_TIP = "<html>"
			+ "Path to a data file used as the initial state. The file must <br>"
			+ "have the correct number of rows, columns, and states. If rows or <br>"
			+ "columns are incorrect, the data will be padded when possible. <br>"
			+ "Extra states are ignored when possible. <br><br>"
			+ "For integer based rules, data should be formatted as:"
			+ "<pre>                                       <br>"
			+ "          0 0 0 1 0 0 0                     <br>"
			+ "          0 0 1 0 1 0 0                     <br>"
			+ "          0 1 0 0 0 1 0                     <br>"
			+ "          1 0 1 0 1 0 1                     <br>"
			+ "</pre><br>"
			+ "For non-integer based rules, try saving the simulation <br>"
			+ "and then looking at the format in that file. Data delimeters <br>"
			+ "are specified in the System menu. <br><br>"
			+ "Comments beginning with // are ignored and may be included in <br>"
			+ "the file.  Generally, .ca files are acceptable.</html>";

	// the title of the data initial state section
	private static final String DATA_PANEL_TITLE = "Data as initial state";

	// tooltip for the height of the ellipse initial state
	private static final String ELLIPSE_HEIGHT_TIP = "<html>"
			+ "The number of cells outward from the center <br>"
			+ "along the vertical axis (limited by the <br>"
			+ "height of the lattice). <br><br>"
			+ "In other words, a radius of 3 will have 3 <br>"
			+ "cells above and 3 cells below the center <br>" + "cell.</html>";

	// The title for the ellipse section.
	private static final String ELLIPSE_PANEL_TITLE = "Ellipse shape (and circle)";

	// the tooltip for the ellipse initial state option
	private static final String ELLIPSE_TIP = "<html>"
			+ "Create an ellipse with the given radii in the <br>"
			+ "horizontal (x) and vertical (y) directions.</html>";

	// tooltip for the width of the ellipse initial state
	private static final String ELLIPSE_WIDTH_TIP = "<html>"
			+ "The number of cells outward from the center <br>"
			+ "along the horizontal axis (limited by the <br>"
			+ "width of the lattice). <br><br>"
			+ "In other words, a radius of 3 will have 3 <br>"
			+ "cells to the left and 3 cells to the right <br>"
			+ "of the center cell.</html>";

	// the tooltip for the image initial state browse button
	private static final String IMAGE_BROWSE_BUTTON_TIP = "<html>"
			+ "Browse files to find an image as the initial state.  Colors <br>"
			+ "are reduced to the number of states in the CA and altered <br>"
			+ "to match the CA color scheme.  If the rule is not based <br>"
			+ "on integers, the image may display partially or totally blank.</html>";

	// the tooltip for the image initial state file path text field
	private static final String IMAGE_TEXT_FIELD_TIP = "<html>"
			+ "Path to an image file used as the initial state.  Colors <br>"
			+ "are reduced to the number of states in the CA and altered <br>"
			+ "to match the CA color scheme.  If the rule is not based <br>"
			+ "on integers, the image may display partially or totally blank.</html>";

	// the title of the image initial state section
	private static final String IMAGE_PANEL_TITLE = "Image as initial state";

	// The tooltip for the initial state chooser
	private static final String INITIAL_STATE_TIP_BLANK = "<html>A blank initial "
			+ "configuration.</html>";

	// The tooltip for the initial state chooser
	private static final String INITIAL_STATE_TIP_DATA = "<html>"
			+ "Load a data file as the initial state.  The file must have <br>"
			+ "the correct number of rows, columns, and states. If rows or <br>"
			+ "columns are incorrect, the data will be padded when possible. <br>"
			+ "Extra states are ignored when possible. <br><br>"
			+ "For integer based rules, data should be formatted as:"
			+ "<pre>                                       <br>"
			+ "          0 0 0 1 0 0 0                     <br>"
			+ "          0 0 1 0 1 0 0                     <br>"
			+ "          0 1 0 0 0 1 0                     <br>"
			+ "          1 0 1 0 1 0 1                     <br>"
			+ "</pre><br>"
			+ "For non-integer based rules, try saving the simulation and <br>"
			+ "then looking at the format in that file. Data delimeters <br>"
			+ "are specified in the System menu. <br><br>"
			+ "Comments beginning with // are ignored and may be included in <br>"
			+ "the file.  Generally, .ca files are acceptable.</html>";

	// The tooltip for the initial state chooser
	private static final String INITIAL_STATE_TIP_IMAGE = "<html>"
			+ "Loads an image as the initial state.  Colors are reduced <br>"
			+ "to the number of states in the CA and altered to match the <br>"
			+ "CA color scheme.  If the rule is not based on integers, <br>"
			+ "the image may display partially or totally blank.</html>";

	// The tooltip for the initial state chooser
	private static final String INITIAL_STATE_TIP_SINGLE = "<html>An initial configuration with a "
			+ "single filled site.</html>";

	// The tooltip for the initial state chooser
	private static final String INITIAL_STATE_TIP_RANDOM = "<html>A random initial "
			+ "configuration.</html>";

	// the title of the most common initial state section
	private static final String MOST_COMMON_PANEL_TITLE = "Most common initial states";

	// The message when there is no initial state generated by the rule. This
	// message is incomplete and is finished (to be more precise) when the
	// message is used.
	private static final String NO_RULE_GENERATED_INITIAL_STATES = "<html><body>No "
			+ "special initial states are associated with <br>";

	// The title for the section/panel that lets you choose the
	// probability for each state
	private static final String PROBABILITY_PANEL_TITLE = "Probability of each state "
			+ "in ";

	// The tooltip for the probability that is set for each state
	private static final String PROBABILITY_PER_STATE_TIP = "Probability that a cell will "
			+ "be filled with this state. \n"
			+ "Total for all states must be 100%.";

	// The tooltip for the "probability by state" radio button
	private static final String PROBABILITY_TIP = "<html>"
			+ "Probability that a cell will be filled with the specified <br>"
			+ "state.  For example, 80% assigned to state 1 and 20% to <br>"
			+ "state 5 means that there is an 80% probability that each <br>"
			+ "cell will be filled with state 1 and 20% probability that <br>"
			+ "each cell will be filled with state 5. The total for all <br>"
			+ "states must be 100%.</html>";

	// warning for when the rule is not integer based or there are too many
	// states to use the "probability" option. The warning message is incomplete
	// and is finished (to be more precise) when it is displayed.
	private static final String PROBABILITY_WARNING_MESSAGE = "<html><body>This "
			+ "option available only for integer-based <br> "
			+ "rules with "
			+ MAX_NUMSTATES_FOR_PROBABILITY_BY_STATE + " or fewer states. ";

	// The tooltip for the random percent
	private static final String RANDOM_PERCENT_TIP = "<html>"
			+ "Percentage of the cells that will be randomly filled <br>"
			+ "with non-empty states. <br><br>"
			+ "For example, 80% means that 20% of the cells will <br>"
			+ "be blank, and the remainder will be filled. The filled <br>"
			+ "sites are given values selected uniformly (equally) <br>"
			+ "from each of the non-empty states. <br><br>"
			+ "If you desire an equal distribution of all states, set <br>"
			+ "the percent at 100 - 100/N where N is the number <br>"
			+ "of states. </html>";

	// tooltip for the height of the rectangle initial state
	private static final String RECTANGLE_HEIGHT_TIP = "<html>"
			+ "Height of the rectangle (limited <br>"
			+ "by the height of the lattice).</html>";

	// The title for the rectangle section.
	private static final String RECTANGLE_PANEL_TITLE = "Rectangle shape (and line "
			+ "and square)";

	// the tooltip for the rectangle initial state option
	private static final String RECTANGLE_TIP = "Create a rectangle of the "
			+ "given width and height.";

	// tooltip for the width of the rectangle initial state
	private static final String RECTANGLE_WIDTH_TIP = "<html>"
			+ "Width of the rectangle (limited <br>"
			+ "by the width of the lattice).</html>";

	// the title of the most common initial state section
	private static final String RULE_GENERATED_PANEL_TITLE = "Additional states for ";

	// The encompassing panel onto which this one will be added.
	private AllPanel outerPanel = null;

	// A listener for components on this panel.
	private AllPanelListener listener = null;

	// the group of all initial state buttons
	private ButtonGroup initialStatesButtonGroup = null;

	// the rule's cell state view used to display cell states in the
	// probability panel.
	private CellStateView ruleView = null;

	// default color for the submit button text
	private Color defaultSubmitButtonColor = Color.gray;

	// color for the titles of sections
	private Color titleColor = Color.BLUE;

	// the current color scheme being used to display colors in the probability
	// panel.
	private ColorScheme currentColorScheme = null;

	// The directory where the file chooser will first open. (When null, will be
	// my documents or the equivalent).
	private File startDirectory = null;

	// title font (for titles of sections)
	private Font titleFont = null;

	// fonts for display
	private Fonts fonts = null;

	// hashes the rule's initial state names to the panel it generated.
	private HashMap<String, JPanel> ruleGeneratedHash = null;

	// browse for a data file button
	private JButton dataBrowseButton = null;

	// browse for an image file button
	private JButton imageBrowseButton = null;

	// Submit init states changes
	private JButton submitButton = null;

	// a check box indicating if the rectangle should be filled or not
	private JCheckBox fillRectangleCheckBox = null;

	// a check box indicating if the ellipse should be filled or not
	private JCheckBox fillEllipseCheckBox = null;

	// the panel that holds the initial state for importing data
	private JPanel dataInitialStatePanel = null;

	// the panel that holds the initial state for creating ellipses
	private JPanel ellipseInitStatePanel = null;

	// the panel that holds the initial state for importing images
	private JPanel imageInitialStatePanel = null;

	// The inner panel that holds all of the buttons
	private JPanel initStatesPanel = null;

	// the panel that holds the probabilities set per state
	private JPanel probabilityInitStatePanel = null;

	// the panel that holds the probabilities for each state
	private JPanel probabilityPanel = null;

	// the panel that holds each probability spinner
	private JPanel probabilitySpinnerAllStatesPanel = null;

	// the panel that holds initial states for creating rectangles
	private JPanel rectangleInitStatePanel = null;

	// the panel that holds rule-generated initial states
	private JPanel ruleGeneratedInitStatePanel = null;

	// an array of panels containing init state components generated by the rule
	private JPanel[] ruleGeneratedJPanels = null;

	// allows user to choose a blank initial state
	private JRadioButton blankRadioButton = null;

	// allows user to choose a data file for the initial state
	private JRadioButton dataRadioButton = null;

	// allows user to choose an ellipse initial state
	private JRadioButton ellipseRadioButton = null;

	// allows user to choose an image for the initial state
	private JRadioButton imageRadioButton = null;

	// selects the probability per state option
	private JRadioButton probabilityRadioButton = null;

	// allows user to choose a random initial state
	private JRadioButton randomRadioButton = null;

	// allows user to choose a rectangle initial state
	private JRadioButton rectangleRadioButton = null;

	// allows user to choose a single seed initial state
	private JRadioButton singleSeedRadioButton = null;

	// scroll pane that holds the probability spinners
	private JScrollPane probabilityScroller = null;

	// selects the height of an ellipse
	private JSpinner ellipseHeightSpinner = null;

	// selects the width of an ellipse
	private JSpinner ellipseWidthSpinner = null;

	// selects the probability for each state.
	private JSpinner[] probabilitySpinners = null;

	// selects the random percent
	private JSpinner randomPercentSpinner = null;

	// selects the height of a rectangle
	private JSpinner rectangleHeightSpinner = null;

	// selects the width of a rectangle
	private JSpinner rectangleWidthSpinner = null;

	// a text field for specifying data file paths
	private JTextField dataFilePathTextField = null;

	// a text field for specifying image file paths
	private JTextField imageFilePathTextField = null;

	// a list of radio buttons containing init states generated by the rule
	private List<JRadioButton> ruleGeneratedRadioButtonList = null;

	// the most common initial states are in this panel.
	private PulsatingJPanel mostCommonInitStatePanel = null;

	/**
	 * The panel containing the CA properties such as lattice type, rule, etc.
	 * 
	 * @param outerPanel
	 *            The encompassing panel onto which this one will be added.
	 */
	public InitialStatesPanel(AllPanel outerPanel)
	{
		super();

		this.outerPanel = outerPanel;
		this.listener = outerPanel.getAllPanelListener();

		this.setOpaque(true);

		// get the folder where files will be opened
		startDirectory = new File(CurrentProperties.getInstance()
				.getSaveDataFilePath());

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

		// create all the initial state panels
		initStatesPanel = createAllInitialStatePanels();
		JScrollPane scrollPanel = new JScrollPane(initStatesPanel);
		scrollPanel.setBorder(BorderFactory.createEmptyBorder());
		scrollPanel
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		int width = CAFrame.tabbedPaneDimension.width
				- scrollPanel.getInsets().left - scrollPanel.getInsets().right;
		int height = initStatesPanel.getMinimumSize().height;
		initStatesPanel.setPreferredSize(new Dimension(width, height));

		// disable any components (now that they are created)
		enableDisableComponents();

		// create a button for submitting properties
		JPanel submitPanel = createSubmitButton();

		// create a layout
		this.setLayout(new GridBagLayout());
		this.add(scrollPanel, new GBC(0, 1).setSpan(1, 1).setFill(GBC.BOTH)
				.setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		this.add(submitPanel, new GBC(0, 2).setSpan(1, 1).setFill(
				GBC.HORIZONTAL).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));
	}

	/**
	 * Adds all the radio buttons generated by the rule.
	 * 
	 * @param panel
	 *            The panel to which the radio buttons are added.
	 */
	private void addRadioButtonsGeneratedByRule(JPanel panel)
	{
		// get an iterator over the panels associated with the rule's initial
		// states
		ruleGeneratedJPanels = createRuleGeneratedInitialStateJPanels(null);

		int row = 0;
		if(ruleGeneratedRadioButtonList != null
				&& ruleGeneratedRadioButtonList.size() > 0)
		{
			for(int i = 0; i < ruleGeneratedRadioButtonList.size(); i++)
			{
				// get the radio button
				JRadioButton button = ruleGeneratedRadioButtonList.get(i);

				// add to the button group
				if(initialStatesButtonGroup != null)
				{
					initialStatesButtonGroup.add(button);
				}

				// the panel associated with that rule generated initial state
				// button
				JPanel initStatePanel = null;
				if(ruleGeneratedJPanels != null
						&& ruleGeneratedJPanels.length > i)
				{
					initStatePanel = ruleGeneratedJPanels[i];
				}

				if(panel != null)
				{
					// add the button
					panel.add(button, new GBC(1, row).setSpan(4, 1).setFill(
							GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
							.setInsets(1));

					// move to the next row
					row++;

					// add the panel to the next row if it exists
					if(initStatePanel != null)
					{
						panel.add(initStatePanel, new GBC(1, row).setSpan(4, 1)
								.setFill(GBC.BOTH).setWeight(1.0, 1.0)
								.setAnchor(GBC.WEST).setInsets(1));

						// move to the next row
						row++;
					}
				}
			}
		}
		else
		{
			// the rule name selected on the rule panel
			String ruleName = outerPanel.getRulePanel().getRuleTree()
					.getSelectedRuleName();
			String message = NO_RULE_GENERATED_INITIAL_STATES + ruleName
					+ ".</body></html>";
			JLabel noStatesLabel = new JLabel(message);
			noStatesLabel.setFont(fonts.getPlainFont());

			if(panel != null)
			{
				panel.add(noStatesLabel, new GBC(1, row).setSpan(4, 1).setFill(
						GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
						.setInsets(1));
			}
		}
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
	 * Create and arrange all of the panels that hold initial state controls.
	 */
	private JPanel createAllInitialStatePanels()
	{
		// the panel on which we add the controls
		JPanel innerPanel = new JPanel();
		innerPanel.setBorder(BorderFactory.createRaisedBevelBorder());

		GridBagLayout layout = new GridBagLayout();
		innerPanel.setLayout(layout);

		// create all the controls in the correct order. Create all other
		// components before the radio buttons (which will disable some of those
		// components when they are created).
		randomPercentSpinner = createRandomPercentSpinner();
		createRadioButtons();

		// create panels that group the controls together
		mostCommonInitStatePanel = createMostCommonPanel();
		ruleGeneratedInitStatePanel = createRuleGeneratedInitStatesPanel();
		imageInitialStatePanel = createImageInitialStatePanel();
		dataInitialStatePanel = createDataInitialStatePanel();
		probabilityInitStatePanel = createProbabilityChooserPanel();
		rectangleInitStatePanel = createRectangleInitStatePanel();
		ellipseInitStatePanel = createEllipseInitStatePanel();

		// decide which button should be active (this has to happen after the
		// rule generated init state panel is created)
		selectActiveRadioButton();

		// layout the various panels
		int row = 0;
		innerPanel.add(mostCommonInitStatePanel, new GBC(1, row).setSpan(8, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		innerPanel.add(ruleGeneratedInitStatePanel, new GBC(1, row).setSpan(8,
				1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		innerPanel.add(imageInitialStatePanel, new GBC(1, row).setSpan(8, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		innerPanel.add(dataInitialStatePanel, new GBC(1, row).setSpan(8, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		innerPanel.add(probabilityInitStatePanel, new GBC(1, row).setSpan(8, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		innerPanel.add(rectangleInitStatePanel, new GBC(1, row).setSpan(8, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		innerPanel.add(ellipseInitStatePanel, new GBC(1, row).setSpan(8, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		return innerPanel;
	}

	/**
	 * Create a file browser for setting initial state from a data file.
	 * 
	 * @return Opens a file browser.
	 */
	private JButton createDataInitialStateFileBrowser()
	{
		JButton browseButton = new JButton("Browse");
		browseButton.setFont(fonts.getBoldSmallerFont());
		browseButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// make the JFrame look disabled
				outerPanel.getCAFrame().setViewDisabled(true);

				JFileChooser fileChooser = new CAFileChooser(startDirectory);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setDialogTitle("Initial State Data File");

				// add a preferred file filter
				fileChooser.addChoosableFileFilter(new CAFileFilter());

				int state = fileChooser.showOpenDialog(outerPanel.getCAFrame()
						.getFrame());
				File file = fileChooser.getSelectedFile();

				startDirectory = fileChooser.getCurrentDirectory();

				if((file != null) && (state == JFileChooser.APPROVE_OPTION))
				{
					dataFilePathTextField.setText(file.getPath());

					// and change the color of the submit button
					submitButton.setForeground(Color.RED);
				}

				// make the JFrame look enabled
				outerPanel.getCAFrame().setViewDisabled(false);
			}
		});

		return browseButton;
	}

	/**
	 * Create layout for data initial state choices.
	 * 
	 * @return panel containing choices created by the rule.
	 */
	private JPanel createDataInitialStatePanel()
	{
		// get the data path from the properties
		String filePath = CurrentProperties.getInstance()
				.getInitialStateDataFilePath();
		if(filePath == null)
		{
			filePath = "";
		}

		// create a text field (with a minimum width -- the layout may scale it
		// larger)
		dataFilePathTextField = new JTextField(filePath,
				FILE_PATH_TEXT_FIELD_MIN_WIDTH);
		dataFilePathTextField.setToolTipText(DATA_TEXT_FIELD_TIP);

		// add listeners
		dataFilePathTextField.getDocument().addDocumentListener(listener);
		dataFilePathTextField.getDocument().addDocumentListener(this);

		// create a button for browsing
		dataBrowseButton = createDataInitialStateFileBrowser();
		dataBrowseButton.setToolTipText(DATA_BROWSE_BUTTON_TIP);

		// create a panel on which to layout these components
		JPanel dataPanel = new JPanel(new GridBagLayout());

		// add components to layout
		int row = 0;
		dataPanel
				.add(dataRadioButton, new GBC(1, row).setSpan(2, 1).setFill(
						GBC.BOTH).setWeight(0.05, 0.0).setAnchor(GBC.WEST)
						.setInsets(1));
		dataPanel.add(dataFilePathTextField, new GBC(3, row).setSpan(4, 1)
				.setFill(GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(
						GBC.CENTER).setInsets(1));
		dataPanel
				.add(dataBrowseButton, new GBC(8, row).setSpan(1, 1).setFill(
						GBC.NONE).setWeight(0.05, 0.0).setAnchor(GBC.EAST)
						.setInsets(1));

		// create border
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), DATA_PANEL_TITLE, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		dataPanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder, titledBorder));

		return dataPanel;
	}

	/**
	 * Create layout for the initial state choice that draws an ellipse.
	 * 
	 * @return panel containing the ellipse initial state option.
	 */
	private JPanel createEllipseInitStatePanel()
	{
		JLabel heightLabel = new JLabel(" y-radius:");
		heightLabel.setToolTipText(ELLIPSE_HEIGHT_TIP);

		JLabel widthLabel = new JLabel("x-radius:");
		widthLabel.setToolTipText(ELLIPSE_WIDTH_TIP);

		// get the max height and width (row and col divided by 2)
		int maxHeight = ((Integer) outerPanel.getPropertiesPanel()
				.getNumRowsSpinner().getValue()).intValue() / 2;
		int maxWidth = ((Integer) outerPanel.getPropertiesPanel()
				.getNumColumnsSpinner().getValue()).intValue() / 2;

		if(OneDimensionalLattice.isCurrentLatticeOneDim())
		{
			maxHeight = 0;
		}

		// height spinner
		int initialHeight = CurrentProperties.getInstance()
				.getInitialStateEllipseHeight();
		SpinnerNumberModel heightModel = new SpinnerNumberModel(initialHeight,
				0, maxHeight, 1);
		ellipseHeightSpinner = new JSpinner(heightModel);
		ellipseHeightSpinner.setToolTipText(ELLIPSE_HEIGHT_TIP);
		ellipseHeightSpinner.addChangeListener(listener);
		ellipseHeightSpinner.addChangeListener(this);
		((JSpinner.NumberEditor) ellipseHeightSpinner.getEditor())
				.getTextField().getDocument().addDocumentListener(this);

		// width spinner
		int initialWidth = CurrentProperties.getInstance()
				.getInitialStateEllipseWidth();
		SpinnerNumberModel widthModel = new SpinnerNumberModel(initialWidth, 0,
				maxWidth, 1);
		ellipseWidthSpinner = new JSpinner(widthModel);
		ellipseWidthSpinner.setToolTipText(ELLIPSE_WIDTH_TIP);
		ellipseWidthSpinner.addChangeListener(listener);
		ellipseWidthSpinner.addChangeListener(this);
		((JSpinner.NumberEditor) ellipseWidthSpinner.getEditor())
				.getTextField().getDocument().addDocumentListener(this);

		// "fill" check box
		fillEllipseCheckBox = new JCheckBox("fill ellipse");
		fillEllipseCheckBox.addActionListener(this);
		fillEllipseCheckBox.setSelected(CurrentProperties.getInstance()
				.isInitialStateEllipseFilled());

		// panel for the ellipse init state
		JPanel ellipsePanel = new JPanel(new GridBagLayout());

		// add buttons to layout
		int row = 0;
		ellipsePanel.add(ellipseRadioButton, new GBC(1, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		ellipsePanel.add(widthLabel, new GBC(1, row).setSpan(1, 1).setFill(
				GBC.BOTH).setWeight(0.0, 0.0).setAnchor(GBC.EAST).setInsets(7,
				7, 7, 1));
		ellipsePanel.add(ellipseWidthSpinner, new GBC(2, row).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(1.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(7, 1, 7, 7));
		ellipsePanel.add(heightLabel, new GBC(3, row).setSpan(1, 1).setFill(
				GBC.BOTH).setWeight(0.0, 0.0).setAnchor(GBC.EAST).setInsets(1));
		ellipsePanel.add(ellipseHeightSpinner, new GBC(4, row).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(1.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(7, 1, 7, 7));
		ellipsePanel.add(fillEllipseCheckBox, new GBC(5, row).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));
		ellipsePanel.add(Box.createHorizontalGlue(), new GBC(6, row).setSpan(1,
				1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// create border
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), ELLIPSE_PANEL_TITLE, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		ellipsePanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder, titledBorder));

		return ellipsePanel;
	}

	/**
	 * Create a file browser for setting initial state image file.
	 * 
	 * @return Opens a file browser.
	 */
	private JButton createImageInitialStateFileBrowser()
	{
		JButton browseButton = new JButton("Browse");
		browseButton.setFont(fonts.getBoldSmallerFont());
		browseButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// make the JFrame look disabled
				outerPanel.getCAFrame().setViewDisabled(true);

				JFileChooser fileChooser = new CAFileChooser(startDirectory);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setDialogTitle("Initial State Image");

				// add a preview panel
				final ImagePreviewer imagePreviewer = new ImagePreviewer();
				PreviewPanel previewPanel = new PreviewPanel(imagePreviewer);
				fileChooser.setAccessory(previewPanel);
				fileChooser
						.addPropertyChangeListener(new PropertyChangeListener()
						{
							public void propertyChange(PropertyChangeEvent e)
							{
								if(e
										.getPropertyName()
										.equals(
												JFileChooser.SELECTED_FILE_CHANGED_PROPERTY))
								{
									File f = (File) e.getNewValue();
									String extension = AllImageFilter
											.getExtension(f);

									if(AllImageTypeReader
											.isPermittedImageType(extension))
									{
										imagePreviewer.configure(f);
									}
									else
									{
										imagePreviewer.configure(null);
									}
								}
							}
						});

				// only allow image files
				fileChooser.addChoosableFileFilter(new AllImageFilter());
				fileChooser.setAcceptAllFileFilterUsed(false);

				int state = fileChooser.showOpenDialog(outerPanel.getCAFrame()
						.getFrame());
				File file = fileChooser.getSelectedFile();

				startDirectory = fileChooser.getCurrentDirectory();

				if((file != null) && (state == JFileChooser.APPROVE_OPTION))
				{
					imageFilePathTextField.setText(file.getPath());

					// and change the color of the submit button
					submitButton.setForeground(Color.RED);
				}

				// make the JFrame look enabled
				outerPanel.getCAFrame().setViewDisabled(false);
			}
		});

		return browseButton;
	}

	/**
	 * Create layout for image initial state choices.
	 * 
	 * @return panel containing choices created by the rule.
	 */
	private JPanel createImageInitialStatePanel()
	{
		// get the image path from the properties
		String filePath = CurrentProperties.getInstance()
				.getInitialStateImageFilePath();
		if(filePath == null)
		{
			filePath = "";
		}

		// create a text field (with a minimum width -- the layout may scale it
		// larger)
		imageFilePathTextField = new JTextField(filePath,
				FILE_PATH_TEXT_FIELD_MIN_WIDTH);
		imageFilePathTextField.setToolTipText(IMAGE_TEXT_FIELD_TIP);

		// add listeners
		imageFilePathTextField.getDocument().addDocumentListener(listener);
		imageFilePathTextField.getDocument().addDocumentListener(this);

		// create a button for browsing
		imageBrowseButton = createImageInitialStateFileBrowser();
		imageBrowseButton.setToolTipText(IMAGE_BROWSE_BUTTON_TIP);

		// create a panel on which to layout these image components
		JPanel imagePanel = new JPanel(new GridBagLayout());

		// add components to layout
		int row = 0;
		imagePanel
				.add(imageRadioButton, new GBC(1, row).setSpan(2, 1).setFill(
						GBC.BOTH).setWeight(0.05, 0.0).setAnchor(GBC.WEST)
						.setInsets(1));
		imagePanel.add(imageFilePathTextField, new GBC(3, row).setSpan(4, 1)
				.setFill(GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(
						GBC.CENTER).setInsets(1));
		imagePanel.add(imageBrowseButton, new GBC(8, row).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(0.05, 0.0).setAnchor(GBC.EAST)
				.setInsets(1));

		// create border
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), IMAGE_PANEL_TITLE, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		imagePanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder, titledBorder));

		return imagePanel;
	}

	/**
	 * Gets panels from the rule for each initial state created by the rule.
	 * 
	 * @return A list of panels corresponding to the initial states generated by
	 *         the rule.
	 */
	// private List<JPanel> createJPanelsGeneratedByRule()
	// {
	// // the list of initial states created by the rule
	// LinkedList<JPanel> panelList = new LinkedList<JPanel>();
	//
	// JPanel[] initStatePanels = getRuleGeneratedInitialStateJPanels(null);
	//
	// if(initStatePanels != null)
	// {
	// for(int i = 0; i < initStatePanels.length; i++)
	// {
	// // add the panel to the list
	// panelList.add(initStatePanels[i]);
	// }
	// }
	//
	// return panelList;
	// }
	/**
	 * Create layout for the most common initial state choices.
	 * 
	 * @return the blank, single seed, and random choices.
	 */
	private PulsatingJPanel createMostCommonPanel()
	{
		// panel for the most common init state buttons
		PulsatingJPanel mostCommonInitStatePanel = new PulsatingJPanel(
				new GridBagLayout());
		mostCommonInitStatePanel.setPreferredSize(new Dimension(PANEL_WIDTH,
				100));

		// add buttons to layout
		int row = 0;
		mostCommonInitStatePanel.add(blankRadioButton, new GBC(1, row).setSpan(
				4, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		mostCommonInitStatePanel.add(singleSeedRadioButton, new GBC(1, row)
				.setSpan(4, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(
						GBC.WEST).setInsets(1));

		row++;
		mostCommonInitStatePanel.add(randomRadioButton, new GBC(1, row)
				.setSpan(2, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(
						GBC.WEST).setInsets(1));
		mostCommonInitStatePanel.add(randomPercentSpinner, new GBC(3, row)
				.setSpan(1, 1).setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(
						GBC.WEST).setInsets(1));

		// create border
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		// Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
		// .createEtchedBorder(Color.BLUE, Color.GRAY),
		// MOST_COMMON_PANEL_TITLE, TitledBorder.LEFT,
		// TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), MOST_COMMON_PANEL_TITLE,
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		mostCommonInitStatePanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder, titledBorder));

		return mostCommonInitStatePanel;
	}

	/**
	 * Create a panel that holds the radio button and chooser for selecting the
	 * probability of each state.
	 * 
	 * @return panel for choosing probability for each state.
	 */
	private JPanel createProbabilityChooserPanel()
	{
		probabilitySpinnerAllStatesPanel = createProbabilityChoosers();

		probabilityScroller = new JScrollPane(probabilitySpinnerAllStatesPanel);
		probabilityScroller.setPreferredSize(new Dimension(PANEL_WIDTH,
				PROBABILITY_PANEL_HEIGHT));
		probabilityScroller.setMinimumSize(new Dimension(PANEL_WIDTH,
				PROBABILITY_PANEL_HEIGHT));
		probabilityScroller.setMaximumSize(new Dimension(PANEL_WIDTH,
				PROBABILITY_PANEL_HEIGHT));
		probabilityScroller
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		probabilityPanel = new JPanel(new GridBagLayout());

		int row = 0;
		probabilityPanel.add(probabilityRadioButton, new GBC(1, row).setSpan(2,
				1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		probabilityPanel.add(probabilityScroller, new GBC(1, row).setSpan(2, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// create a title for the panel
		probabilityPanel.setBorder(createProbabilityPanelBorder());

		return probabilityPanel;
	}

	/**
	 * Create a panel that lets the user choose the probability of each state.
	 * 
	 * @return a panel for setting the probability of each state.
	 */
	private JPanel createProbabilityChoosers()
	{
		// the panel that gets returned
		JPanel randomPanel = null;

		// get the rule selected on the rule tree
		String ruleDisplayName = outerPanel.getRulePanel().getRuleTree()
				.getSelectedRuleName();

		// If the user selected a folder or unavailable rule (greyed out), then
		// this could happen. So get the currently active rule instead.
		if(ruleDisplayName == null)
		{
			String ruleClassName = CurrentProperties.getInstance()
					.getRuleClassName();
			Rule rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(ruleClassName);
			ruleDisplayName = rule.getDisplayName();
		}

		RuleHash ruleHash = new RuleHash();
		String classNameOfRule = ruleHash.get(ruleDisplayName);
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(classNameOfRule);

		// make sure it is an integer based rule
		boolean isIntegerCompatible = IntegerCellState
				.isCompatibleRule(ruleDisplayName);

		// get the number of states on the properties panel
		int numStates = getNumStates();

		// don't do this if too resource intensive
		if(isIntegerCompatible
				&& numStates <= MAX_NUMSTATES_FOR_PROBABILITY_BY_STATE)
		{
			ruleView = rule.getCompatibleCellStateView();

			// not necessary?
			currentColorScheme = CellStateView.colorScheme;

			// create the color patches (one for each state)
			ColorPatch[] colorPatch = new ColorPatch[numStates];
			for(int i = 0; i < numStates; i++)
			{
				// get the color from the view
				Color color = ruleView.getDisplayColor(new IntegerCellState(i),
						new Integer(numStates), new Coordinate(0, 0));

				colorPatch[i] = new ColorPatch(rule, color, i, numStates);
			}

			// get the percents (numbers) to fill the spinners
			int[] percents = new int[numStates];
			String percentString = CurrentProperties.getInstance()
					.getRandomPercentPerState();
			String delimiters = CurrentProperties.getInstance()
					.getDataDelimiters();
			StringTokenizer tokens = new StringTokenizer(percentString,
					delimiters);
			int total = 0;
			for(int i = 0; i < percents.length; i++)
			{
				try
				{
					percents[i] = new Integer(tokens.nextToken()).intValue();
				}
				catch(Exception e)
				{
					percents[i] = 0;
				}

				total += percents[i];
			}

			// make sure the total is 100%
			if(total != 100)
			{
				// then too many or too few tokens, so reset to a default
				percents[0] = 100;
				for(int i = 1; i < percents.length; i++)
				{
					percents[i] = 0;
				}
			}

			// create the spinners
			probabilitySpinners = new JSpinner[numStates];
			for(int i = 0; i < probabilitySpinners.length; i++)
			{
				SpinnerNumberModel probabilityModel = new SpinnerNumberModel(
						percents[i], 0, 100, 1);
				probabilitySpinners[i] = new JSpinner(probabilityModel);
				probabilitySpinners[i]
						.setToolTipText(PROBABILITY_PER_STATE_TIP);

				// add listeners
				probabilitySpinners[i].addChangeListener(listener);
				probabilitySpinners[i].addChangeListener(this);
				((JSpinner.NumberEditor) probabilitySpinners[i].getEditor())
						.getTextField().getDocument().addDocumentListener(this);
			}

			// lay out the colors and spinners in two columns
			randomPanel = new JPanel(new GridBagLayout());
			int halfPoint = (int) Math.ceil(numStates / 2.0);
			for(int state = 0; state < halfPoint; state++)
			{
				// column 1
				int row = state;
				randomPanel.add(colorPatch[state], new GBC(1, row)
						.setSpan(1, 1).setFill(GBC.NONE).setWeight(0.0, 0.0)
						.setAnchor(GBC.WEST).setInsets(1));
				randomPanel.add(Box.createHorizontalStrut(10), new GBC(2, row)
						.setSpan(1, 1).setFill(GBC.NONE).setWeight(0.0, 0.0)
						.setAnchor(GBC.WEST).setInsets(1));
				randomPanel.add(probabilitySpinners[state], new GBC(3, row)
						.setSpan(1, 1).setFill(GBC.NONE).setWeight(1.0, 0.0)
						.setAnchor(GBC.WEST).setInsets(1));
				randomPanel.add(Box.createHorizontalStrut(40), new GBC(4, row)
						.setSpan(1, 1).setFill(GBC.NONE).setWeight(0.0, 0.0)
						.setAnchor(GBC.WEST).setInsets(1));
			}

			for(int state = halfPoint; state < numStates; state++)
			{
				// column 2
				int row = state - halfPoint;
				randomPanel.add(colorPatch[state], new GBC(5, row)
						.setSpan(1, 1).setFill(GBC.NONE).setWeight(0.0, 0.0)
						.setAnchor(GBC.WEST).setInsets(1));
				randomPanel.add(Box.createHorizontalStrut(10), new GBC(6, row)
						.setSpan(1, 1).setFill(GBC.NONE).setWeight(0.0, 0.0)
						.setAnchor(GBC.WEST).setInsets(1));
				randomPanel.add(probabilitySpinners[state], new GBC(7, row)
						.setSpan(1, 1).setFill(GBC.NONE).setWeight(1.0, 0.0)
						.setAnchor(GBC.WEST).setInsets(1));
			}
		}
		else
		{
			// just print a "warning" message
			randomPanel = probabilityWarningMessage();
		}

		return randomPanel;
	}

	/**
	 * Create a title for the probability panel.
	 */
	private Border createProbabilityPanelBorder()
	{
		// create border
		String title = PROBABILITY_PANEL_TITLE
				+ outerPanel.getRulePanel().getRuleTree().getSelectedRuleName();
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), title, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		return BorderFactory.createCompoundBorder(outerEmptyBorder,
				titledBorder);
	}

	/**
	 * Creates all the radio buttons for all the initial state choices.
	 */
	private void createRadioButtons()
	{
		// create blank radio button
		blankRadioButton = new JRadioButton(CurrentProperties.STATE_BLANK);
		blankRadioButton.setToolTipText(INITIAL_STATE_TIP_BLANK);
		blankRadioButton.setFont(fonts.getBoldSmallerFont());
		blankRadioButton.setActionCommand(CurrentProperties.STATE_BLANK);
		blankRadioButton.addActionListener(this);
		blankRadioButton.addActionListener(listener);

		// create single seed radio button
		singleSeedRadioButton = new JRadioButton(
				CurrentProperties.STATE_SINGLE_SEED);
		singleSeedRadioButton.setToolTipText(INITIAL_STATE_TIP_SINGLE);
		singleSeedRadioButton.setFont(fonts.getBoldSmallerFont());
		singleSeedRadioButton
				.setActionCommand(CurrentProperties.STATE_SINGLE_SEED);
		singleSeedRadioButton.addActionListener(this);
		singleSeedRadioButton.addActionListener(listener);

		// create random radio button
		randomRadioButton = new JRadioButton(CurrentProperties.STATE_RANDOM);
		randomRadioButton.setToolTipText(INITIAL_STATE_TIP_RANDOM);
		randomRadioButton.setFont(fonts.getBoldSmallerFont());
		randomRadioButton.setActionCommand(CurrentProperties.STATE_RANDOM);
		randomRadioButton.addActionListener(this);
		randomRadioButton.addActionListener(listener);

		// create an array of radio buttons, one for each initial state
		// generated by the rule
		ruleGeneratedRadioButtonList = createRuleGeneratedRadioButtons();

		// create image radio button
		imageRadioButton = new JRadioButton(CurrentProperties.STATE_IMAGE);
		imageRadioButton.setToolTipText(INITIAL_STATE_TIP_IMAGE);
		imageRadioButton.setFont(fonts.getBoldSmallerFont());
		imageRadioButton.setActionCommand(CurrentProperties.STATE_IMAGE);
		imageRadioButton.addActionListener(this);
		imageRadioButton.addActionListener(listener);

		// create data file radio button
		dataRadioButton = new JRadioButton(CurrentProperties.STATE_DATA);
		dataRadioButton.setToolTipText(INITIAL_STATE_TIP_DATA);
		dataRadioButton.setFont(fonts.getBoldSmallerFont());
		dataRadioButton.setActionCommand(CurrentProperties.STATE_DATA);
		dataRadioButton.addActionListener(this);
		dataRadioButton.addActionListener(listener);

		// create probability radio button
		probabilityRadioButton = new JRadioButton(
				CurrentProperties.STATE_PROBABILITY);
		probabilityRadioButton.setToolTipText(PROBABILITY_TIP);
		probabilityRadioButton.setFont(fonts.getBoldSmallerFont());
		probabilityRadioButton
				.setActionCommand(CurrentProperties.STATE_PROBABILITY);
		probabilityRadioButton.addActionListener(this);
		probabilityRadioButton.addActionListener(listener);

		// create rectangle radio button
		rectangleRadioButton = new JRadioButton(
				CurrentProperties.STATE_RECTANGLE);
		rectangleRadioButton.setToolTipText(RECTANGLE_TIP);
		rectangleRadioButton.setFont(fonts.getBoldSmallerFont());
		rectangleRadioButton
				.setActionCommand(CurrentProperties.STATE_RECTANGLE);
		rectangleRadioButton.addActionListener(this);
		rectangleRadioButton.addActionListener(listener);

		// create rectangle radio button
		ellipseRadioButton = new JRadioButton(CurrentProperties.STATE_ELLIPSE);
		ellipseRadioButton.setToolTipText(ELLIPSE_TIP);
		ellipseRadioButton.setFont(fonts.getBoldSmallerFont());
		ellipseRadioButton.setActionCommand(CurrentProperties.STATE_ELLIPSE);
		ellipseRadioButton.addActionListener(this);
		ellipseRadioButton.addActionListener(listener);

		// Group the radio buttons so that only one can be selected.
		initialStatesButtonGroup = new ButtonGroup();
		initialStatesButtonGroup.add(blankRadioButton);
		initialStatesButtonGroup.add(singleSeedRadioButton);
		initialStatesButtonGroup.add(randomRadioButton);
		initialStatesButtonGroup.add(imageRadioButton);
		initialStatesButtonGroup.add(dataRadioButton);
		initialStatesButtonGroup.add(probabilityRadioButton);
		initialStatesButtonGroup.add(rectangleRadioButton);
		initialStatesButtonGroup.add(ellipseRadioButton);

		// DON'T DO THIS HERE -- THEY ARE ADDED ELSEWHERE. WILL GET ADDED TWICE
		// IF DO THIS HERE.
		// add all of the buttons that are associated with initial states
		// that were generated by a rule
		// if(ruleGeneratedRadioButtonList != null
		// && ruleGeneratedRadioButtonList.size() > 0)
		// {
		// for(JRadioButton button : ruleGeneratedRadioButtonList)
		// {
		// // add to the button group
		// initialStatesButtonGroup.add(button);
		// }
		// }

		// decide which button should be active
		// selectActiveRadioButton();
	}

	/**
	 * Create spinner for the percentage of sites that will be filled randomly.
	 * 
	 * @return the spinner for the random percent.
	 */
	private JSpinner createRandomPercentSpinner()
	{
		// get the percentage used the last time the program was run
		int randomPercent = CurrentProperties.getInstance().getRandomPercent();

		// create spinner for the random field
		SpinnerNumberModel randomPercentModel = new SpinnerNumberModel(
				randomPercent, 0, 100, 1);
		randomPercentSpinner = new JSpinner(randomPercentModel);
		randomPercentSpinner.setToolTipText(RANDOM_PERCENT_TIP);

		// add listeners
		randomPercentSpinner.addChangeListener(listener);
		randomPercentSpinner.addChangeListener(this);
		((JSpinner.NumberEditor) randomPercentSpinner.getEditor())
				.getTextField().getDocument().addDocumentListener(this);

		return randomPercentSpinner;
	}

	/**
	 * Create layout for the initial state choice that draws a rectangle.
	 * 
	 * @return panel containing the rectangle initial state option.
	 */
	private JPanel createRectangleInitStatePanel()
	{
		JLabel heightLabel = new JLabel("height:");
		JLabel widthLabel = new JLabel("width:");

		int maxHeight = ((Integer) outerPanel.getPropertiesPanel()
				.getNumRowsSpinner().getValue()).intValue();
		int maxWidth = ((Integer) outerPanel.getPropertiesPanel()
				.getNumColumnsSpinner().getValue()).intValue();

		if(OneDimensionalLattice.isCurrentLatticeOneDim())
		{
			maxHeight = 1;
		}

		// height spinner
		int initialHeight = CurrentProperties.getInstance()
				.getInitialStateRectangleHeight();
		SpinnerNumberModel heightModel = new SpinnerNumberModel(initialHeight,
				1, maxHeight, 1);
		rectangleHeightSpinner = new JSpinner(heightModel);
		rectangleHeightSpinner.setToolTipText(RECTANGLE_HEIGHT_TIP);
		rectangleHeightSpinner.addChangeListener(listener);
		rectangleHeightSpinner.addChangeListener(this);
		((JSpinner.NumberEditor) rectangleHeightSpinner.getEditor())
				.getTextField().getDocument().addDocumentListener(this);

		// width spinner
		int initialWidth = CurrentProperties.getInstance()
				.getInitialStateRectangleWidth();
		SpinnerNumberModel widthModel = new SpinnerNumberModel(initialWidth, 1,
				maxWidth, 1);
		rectangleWidthSpinner = new JSpinner(widthModel);
		rectangleWidthSpinner.setToolTipText(RECTANGLE_WIDTH_TIP);
		rectangleWidthSpinner.addChangeListener(listener);
		rectangleWidthSpinner.addChangeListener(this);
		((JSpinner.NumberEditor) rectangleWidthSpinner.getEditor())
				.getTextField().getDocument().addDocumentListener(this);

		// "fill" check box
		fillRectangleCheckBox = new JCheckBox("fill rectangle");
		fillRectangleCheckBox.addActionListener(this);
		fillRectangleCheckBox.setSelected(CurrentProperties.getInstance()
				.isInitialStateRectangleFilled());

		// panel for the rectangle init state
		JPanel rectanglePanel = new JPanel(new GridBagLayout());

		// add buttons to layout
		int row = 0;
		rectanglePanel.add(rectangleRadioButton, new GBC(1, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		rectanglePanel.add(widthLabel, new GBC(1, row).setSpan(1, 1).setFill(
				GBC.BOTH).setWeight(0.0, 0.0).setAnchor(GBC.EAST).setInsets(7,
				7, 7, 1));
		rectanglePanel.add(rectangleWidthSpinner, new GBC(2, row).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(1.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(7, 1, 7, 7));
		rectanglePanel.add(heightLabel, new GBC(3, row).setSpan(1, 1).setFill(
				GBC.BOTH).setWeight(0.0, 0.0).setAnchor(GBC.EAST).setInsets(1));
		rectanglePanel.add(rectangleHeightSpinner, new GBC(4, row)
				.setSpan(1, 1).setFill(GBC.NONE).setWeight(1.0, 0.0).setAnchor(
						GBC.WEST).setInsets(7, 1, 7, 7));
		rectanglePanel.add(fillRectangleCheckBox, new GBC(5, row).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));
		rectanglePanel.add(Box.createHorizontalGlue(), new GBC(6, row).setSpan(
				1, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// create border
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), RECTANGLE_PANEL_TITLE,
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		rectanglePanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder, titledBorder));

		return rectanglePanel;
	}

	/**
	 * The set of initial state JPanels generated by the selected rule.
	 * 
	 * @param ruleName
	 *            The rule name used to select the initial states.
	 * @return A list of initial state pznels generated by the rule that has
	 *         been selected on the rule tab.
	 */
	private JPanel[] createRuleGeneratedInitialStateJPanels(String ruleName)
	{
		// the array of initial state panels
		JPanel[] initStatePanels = null;

		// the selected rule's name
		if(ruleName == null)
		{
			ruleName = outerPanel.getRulePanel().getRuleTree()
					.getSelectedRuleName();
		}

		// instantiate the rule using reflection
		Rule rule = null;
		if(ruleName != null)
		{
			RuleHash ruleHash = new RuleHash();
			String ruleClassName = ruleHash.get(ruleName);
			rule = ReflectionTool
					.instantiateFullRuleFromClassName(ruleClassName);
		}

		// make sure it really was instantiated!
		if(rule != null)
		{
			initStatePanels = rule.getInitialStateJPanels();

			// create a hash of "init state names" and "associated panels" that
			// we will need to enable and disable components in these panels
			if((initStatePanels != null) && (initStatePanels.length > 0))
			{
				this.ruleGeneratedHash = new HashMap<String, JPanel>();

				// now fill it
				String[] stateNames = rule.getInitialStateNames();
				for(int i = 0; i < initStatePanels.length; i++)
				{
					this.ruleGeneratedHash.put(stateNames[i],
							initStatePanels[i]);
				}
			}
		}

		return initStatePanels;
	}

	/**
	 * Create layout for the initial state choices that are created by the rule.
	 * 
	 * @return panel containing choices created by the rule.
	 */
	private JPanel createRuleGeneratedInitStatesPanel()
	{
		// panel for the most common init state buttons
		JPanel ruleGeneratedInitStatePanel = new JPanel();
		ruleGeneratedInitStatePanel.setLayout(new GridBagLayout());

		// add buttons to layout
		addRadioButtonsGeneratedByRule(ruleGeneratedInitStatePanel);

		// create border
		ruleGeneratedInitStatePanel.setBorder(createRuleGeneratedPanelBorder());

		return ruleGeneratedInitStatePanel;
	}

	/**
	 * Create a border for the rule-generated initial state panel.
	 */
	private Border createRuleGeneratedPanelBorder()
	{
		String title = RULE_GENERATED_PANEL_TITLE
				+ outerPanel.getRulePanel().getRuleTree().getSelectedRuleName();
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), title, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);

		return BorderFactory.createCompoundBorder(outerEmptyBorder,
				titledBorder);
	}

	/**
	 * Creates buttons for each initial state created by the rule. Also adds
	 * tool tips generated by the rule.
	 * 
	 * @return A list of buttons corresponding to the initial states generated
	 *         by the rule.
	 */
	private List<JRadioButton> createRuleGeneratedRadioButtons()
	{
		// the list of initial states created by the rule
		LinkedList<JRadioButton> buttonList = new LinkedList<JRadioButton>();

		String[] buttonNames = getRuleGeneratedInitialStates(null);
		String[] buttonToolTips = getRuleGeneratedInitialStateToolTips(null);

		if(buttonNames != null)
		{
			for(int i = 0; i < buttonNames.length; i++)
			{
				// create a button
				JRadioButton initStateButton = new JRadioButton(buttonNames[i]);
				initStateButton.setFont(fonts.getBoldSmallerFont());
				initStateButton.setActionCommand(buttonNames[i]);
				initStateButton.addActionListener(this);
				initStateButton.addActionListener(listener);
				if(buttonToolTips != null && i < buttonToolTips.length)
				{
					initStateButton.setToolTipText(buttonToolTips[i]);
				}

				// add the button to the list
				buttonList.add(initStateButton);
			}
		}

		return buttonList;
	}

	/**
	 * // * Button and preview for selecting a particular single seed state. // * // *
	 * 
	 * @return //
	 */
	// private JPanel createSelectSingleSeedStateButtonPanel()
	// {
	// JPanel selectStatePanel = new JPanel();
	//        
	// return selectStatePanel;?
	// }
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
	 * Enable and disable components based on the currently selected radio
	 * button.
	 */
	private void enableDisableComponents()
	{
		// start by disabling everything. Then enable the components we want.
		randomPercentSpinner.setEnabled(false);
		imageBrowseButton.setEnabled(false);
		imageFilePathTextField.setEnabled(false);
		dataBrowseButton.setEnabled(false);
		dataFilePathTextField.setEnabled(false);
		rectangleHeightSpinner.setEnabled(false);
		rectangleWidthSpinner.setEnabled(false);
		fillRectangleCheckBox.setEnabled(false);
		ellipseHeightSpinner.setEnabled(false);
		ellipseWidthSpinner.setEnabled(false);
		fillEllipseCheckBox.setEnabled(false);
		if(probabilitySpinners != null)
		{
			for(JSpinner spinner : probabilitySpinners)
			{
				spinner.setEnabled(false);
			}
		}

		if(ruleGeneratedJPanels != null)
		{
			for(JPanel panel : ruleGeneratedJPanels)
			{
				Component[] components = panel.getComponents();
				for(Component c : components)
				{
					c.setEnabled(false);
				}
			}
		}

		// now enable the components we want
		String initState = getInitialState();
		if(initState.equals(CurrentProperties.STATE_RANDOM))
		{
			randomPercentSpinner.setEnabled(true);
		}
		else if(initState.equals(CurrentProperties.STATE_IMAGE))
		{
			imageBrowseButton.setEnabled(true);
			imageFilePathTextField.setEnabled(true);
		}
		else if(initState.equals(CurrentProperties.STATE_DATA))
		{
			dataBrowseButton.setEnabled(true);
			dataFilePathTextField.setEnabled(true);
		}
		else if(initState.equals(CurrentProperties.STATE_RECTANGLE))
		{
			rectangleHeightSpinner.setEnabled(true);
			rectangleWidthSpinner.setEnabled(true);
			fillRectangleCheckBox.setEnabled(true);
		}
		else if(initState.equals(CurrentProperties.STATE_ELLIPSE))
		{
			ellipseHeightSpinner.setEnabled(true);
			ellipseWidthSpinner.setEnabled(true);
			fillEllipseCheckBox.setEnabled(true);
		}
		else if(initState.equals(CurrentProperties.STATE_PROBABILITY))
		{
			// find out if the selected rule is an integer compatible rule
			String ruleDisplayName = outerPanel.getRulePanel().getRuleTree()
					.getSelectedRuleName();

			// If the user selected a folder or unavailable rule (greyed out),
			// then this could happen. So get the currently active rule instead.
			if(ruleDisplayName == null)
			{
				String ruleClassName = CurrentProperties.getInstance()
						.getRuleClassName();
				Rule rule = ReflectionTool
						.instantiateMinimalRuleFromClassName(ruleClassName);
				ruleDisplayName = rule.getDisplayName();
			}

			RuleHash ruleHash = new RuleHash();
			String classNameOfRule = ruleHash.get(ruleDisplayName);
			Rule rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(classNameOfRule);
			boolean isIntegerCompatible = IntegerCellState
					.isCompatibleRule(rule);

			if(isIntegerCompatible
					&& getNumStates() <= MAX_NUMSTATES_FOR_PROBABILITY_BY_STATE)
			{
				// only enable if the rule is integer state compatible and not
				// null
				if(probabilitySpinners != null)
				{
					for(JSpinner spinner : probabilitySpinners)
					{
						spinner.setEnabled(true);
					}
				}
			}
		}
		else if((ruleGeneratedHash != null)
				&& (ruleGeneratedHash.get(initState) != null))
		{
			// a rule generated initial state, so enable that rule panel's
			// components
			JPanel panel = ruleGeneratedHash.get(initState);
			Component[] components = panel.getComponents();
			for(Component c : components)
			{
				c.setEnabled(true);
			}
		}
		// else for all other cases, keep everything disabled.
	}

	/**
	 * Warning message for when there are too many states to display in the
	 * probability panel.
	 * 
	 * @return a warning label.
	 */
	private JPanel probabilityWarningMessage()
	{
		// the rule name selected on the rule panel
		String ruleName = outerPanel.getRulePanel().getRuleTree()
				.getSelectedRuleName();

		// is it an integer based rule
		boolean isIntegerCompatible = IntegerCellState
				.isCompatibleRule(ruleName);

		// get the number of states on the properties panel
		int numStates = getNumStates();

		// create the warning message
		String warningMessage = PROBABILITY_WARNING_MESSAGE;
		if(!isIntegerCompatible)
		{
			warningMessage += "<br><br>" + ruleName
					+ " is not integer based.</body></html>";
		}
		else if(numStates > MAX_NUMSTATES_FOR_PROBABILITY_BY_STATE)
		{
			warningMessage += "<br><br>You have selected " + numStates
					+ " states.</body></html>";
		}

		JPanel randomPanel = new JPanel(new GridBagLayout());
		JLabel warningLabel = new JLabel(warningMessage);
		warningLabel.setFont(fonts.getPlainFont());
		randomPanel.add(warningLabel, new GBC(1, 0).setSpan(1, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		return randomPanel;
	}

	/**
	 * Removes all the radio buttons generated by the rule.
	 */
	private void removeRadioButtonsGeneratedByRule()
	{
		// remove from the radio button group
		for(JRadioButton button : ruleGeneratedRadioButtonList)
		{
			initialStatesButtonGroup.remove(button);
		}

		// remove from the graphics
		ruleGeneratedInitStatePanel.removeAll();
	}

	/**
	 * Selects the active radio button. Only call this method when instantiating
	 * the buttons or when resetting the buttons based on the currently active
	 * rule.
	 */
	private void selectActiveRadioButton()
	{
		// decide which radio button to select
		String initChoice = CurrentProperties.getInstance().getInitialState();
		if(initChoice != null)
		{
			// set to true when an initial state is selected
			boolean set = false;

			// check all the radio buttons
			Enumeration radioButtons = initialStatesButtonGroup.getElements();
			while(radioButtons.hasMoreElements())
			{
				JRadioButton button = (JRadioButton) radioButtons.nextElement();
				String buttonLabel = button.getText();
				if(initChoice.equals(buttonLabel))
				{
					button.setSelected(true);
					set = true;
				}
			}

			if(!set)
			{
				// a default, just in case
				blankRadioButton.setSelected(true);
			}
		}
		else
		{
			blankRadioButton.setSelected(true);
		}
	}

	/**
	 * Update the title of the panel that shows rule-generated initial states.
	 */
	private void updateRuleGeneratedTitle()
	{
		ruleGeneratedInitStatePanel.setBorder(createRuleGeneratedPanelBorder());
		probabilityPanel.setBorder(createProbabilityPanelBorder());
	}

	/**
	 * Gets the file path for the selected data.
	 * 
	 * @return The currently selected data fil;e.
	 */
	public String getDataFilePath()
	{
		return dataFilePathTextField.getText();
	}

	/**
	 * Gets the spinner holding the ellipse height.
	 * 
	 * @return The ellipse height spinner.
	 */
	public JSpinner getEllipseHeightSpinner()
	{
		return ellipseHeightSpinner;
	}

	/**
	 * Gets the spinner holding the ellipse width.
	 * 
	 * @return The ellipse width spinner.
	 */
	public JSpinner getEllipseWidthSpinner()
	{
		return ellipseWidthSpinner;
	}

	/**
	 * The check box indicating whether or not the ellipse initial state should
	 * be filled.
	 * 
	 * @return checkbox that is checked if the initial state ellipse should be
	 *         filled.
	 */
	public JCheckBox getFillEllipseCheckBox()
	{
		return this.fillEllipseCheckBox;
	}

	/**
	 * The check box indicating whether or not the rectangle initial state
	 * should be filled.
	 * 
	 * @return checkbox that is checked if the initial state rectangle should be
	 *         filled.
	 */
	public JCheckBox getFillRectangleCheckBox()
	{
		return this.fillRectangleCheckBox;
	}

	/**
	 * Gets the file path for the selected image.
	 * 
	 * @return The currently selected image.
	 */
	public String getImageFilePath()
	{
		return imageFilePathTextField.getText();
	}

	/**
	 * Gets the string representing the currently selected initial state.
	 * 
	 * @return The currently selected initial states.
	 */
	public String getInitialState()
	{
		// the default
		String initState = CurrentProperties.STATE_BLANK;

		try
		{
			// may fail if the rule was just replaced and the action command was
			// for a rule-generated initial state.
			initState = initialStatesButtonGroup.getSelection()
					.getActionCommand();
		}
		catch(Throwable t)
		{
			// do nothing
		}

		return initState;
	}

	/**
	 * Gets the group of all initial state buttons.
	 * 
	 * @return The group of initial state buttons.
	 */
	public ButtonGroup getInitialStateButtons()
	{
		return initialStatesButtonGroup;
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
		if(initStatesPanel != null)
		{
			size = initStatesPanel.getSize();
		}
		else
		{
			size = new Dimension(0, 0);
		}

		return size;
	}

	/**
	 * Gets the spinners holding the probability for each state.
	 * 
	 * @return The probability spinners.
	 */
	public JSpinner[] getProbabilitySpinners()
	{
		return probabilitySpinners;
	}

	/**
	 * Gets the spinner holding the random percent.
	 * 
	 * @return The random percent spinner.
	 */
	public JSpinner getRandomPercentSpinner()
	{
		return randomPercentSpinner;
	}

	/**
	 * Gets the spinner holding the rectangle height.
	 * 
	 * @return The rectangle height spinner.
	 */
	public JSpinner getRectangleHeightSpinner()
	{
		return rectangleHeightSpinner;
	}

	/**
	 * Gets the spinner holding the rectangle width.
	 * 
	 * @return The rectangle width spinner.
	 */
	public JSpinner getRectangleWidthSpinner()
	{
		return rectangleWidthSpinner;
	}

	/**
	 * The initial state configurations generated by the selected rule.
	 * 
	 * @param ruleName
	 *            The rule name used to select the initial states.
	 * @return A list of initial state configurations generated by the rule
	 *         selected on the rule panel.
	 */
	public String[] getRuleGeneratedInitialStates(String ruleName)
	{
		// the array of initial state names
		String[] initStateNames = null;

		// the selected rule's name
		if(ruleName == null)
		{
			ruleName = outerPanel.getRulePanel().getRuleTree()
					.getSelectedRuleName();
		}

		// instantiate the rule using reflection
		Rule rule = null;
		if(ruleName != null)
		{
			RuleHash ruleHash = new RuleHash();
			String ruleClassName = ruleHash.get(ruleName);
			rule = ReflectionTool
					.instantiateFullRuleFromClassName(ruleClassName);
		}

		// make sure it really was instantiated!
		if(rule != null)
		{
			initStateNames = rule.getInitialStateNames();
		}

		return initStateNames;
	}

	/**
	 * The initial state tool tips generated by the selected rule.
	 * 
	 * @param ruleName
	 *            The rule name used to select the initial states.
	 * @return A list of initial state tool tips that were generated by and work
	 *         with the current rule.
	 */
	public String[] getRuleGeneratedInitialStateToolTips(String ruleName)
	{
		// the array of initial state tool tips
		String[] initStateToolTips = null;

		// the selected rule's name
		if(ruleName == null)
		{
			ruleName = outerPanel.getRulePanel().getRuleTree()
					.getSelectedRuleName();
		}

		// instantiate the rule using reflection
		Rule rule = null;
		if(ruleName != null)
		{
			RuleHash ruleHash = new RuleHash();
			String ruleClassName = ruleHash.get(ruleName);
			rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(ruleClassName);
		}

		// make sure it really was instantiated!
		if(rule != null)
		{
			initStateToolTips = rule.getInitialStateToolTips();
		}

		return initStateToolTips;
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
		CurrentProperties properties = CurrentProperties.getInstance();

		// update the values of components like the random spinner
		randomPercentSpinner.setValue(properties.getRandomPercent());

		rectangleHeightSpinner.setValue(properties
				.getInitialStateRectangleHeight());

		rectangleWidthSpinner.setValue(properties
				.getInitialStateRectangleWidth());

		ellipseHeightSpinner
				.setValue(properties.getInitialStateEllipseHeight());

		ellipseWidthSpinner.setValue(properties.getInitialStateEllipseWidth());

		// reset the spinner models for the rectangle width and the height.
		resetRectangleSpinners();
		fillRectangleCheckBox.setSelected(properties
				.isInitialStateRectangleFilled());

		// reset the spinner models for the ellipse width and the height.
		resetEllipseSpinners();
		fillEllipseCheckBox.setSelected(properties
				.isInitialStateEllipseFilled());

		imageFilePathTextField.setText(properties
				.getInitialStateImageFilePath());

		dataFilePathTextField.setText(properties.getInitialStateDataFilePath());

		// and recreate the random by state panel -- the number of states may
		// have changed
		resetProbabililtyPanel();

		// now reset each element one by one
		selectActiveRadioButton();

		// disable components as necessary
		enableDisableComponents();

		// reset the color of the submit button
		resetSubmitButtonColorToDefault();
	}

	/**
	 * Resets the radio button. For example, a new rule might necessitate this
	 * because some initial states may no longer be accessible.
	 */
	public void resetActiveRadioButton()
	{
		// find out if the selected rule is an integer compatible rule
		String ruleDisplayName = outerPanel.getRulePanel().getRuleTree()
				.getSelectedRuleName();

		// If the user selected a folder or unavailable rule (greyed out), then
		// this could happen. So get the currently active rule instead.
		if(ruleDisplayName == null)
		{
			String ruleClassName = CurrentProperties.getInstance()
					.getRuleClassName();
			Rule rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(ruleClassName);
			ruleDisplayName = rule.getDisplayName();
		}

		if(ruleDisplayName != null)
		{
			RuleHash ruleHash = new RuleHash();
			String classNameOfRule = ruleHash.get(ruleDisplayName);
			Rule rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(classNameOfRule);
			boolean isIntegerCompatible = IntegerCellState
					.isCompatibleRule(rule);

			// get the number of states
			int numStates = getNumStates();

			// if compatible, then enable the probability radio button
			if(isIntegerCompatible
					&& numStates <= MAX_NUMSTATES_FOR_PROBABILITY_BY_STATE)
			{
				this.probabilityRadioButton.setEnabled(true);

				// and reset the number of states, in case the rule specified
				// some number of states (like Cyclic CA does). Only do this if
				// the number of states has changed or the view has changed or
				// the color scheme has changed, or the empty (or filled) color
				// has changed.
				CellStateView newView = rule.getCompatibleCellStateView();
				if((probabilitySpinners == null)
						|| (ruleView == null)
						|| (currentColorScheme == null)
						|| (numStates != probabilitySpinners.length)
						|| !ruleView.getClass().equals(newView.getClass())
						|| !CellStateView.colorScheme.getClass().equals(
								currentColorScheme.getClass())
						|| !ColorScheme.EMPTY_COLOR
								.equals(probabilitySpinners[0])
						|| !ColorScheme.FILLED_COLOR
								.equals(probabilitySpinners[probabilitySpinners.length - 1]))
				{
					resetProbabililtyPanel();
				}
			}
			else
			{
				// first select something else
				if(probabilityRadioButton.isSelected())
				{
					blankRadioButton.setSelected(true);
				}

				// remove old panel
				if(probabilityScroller != null)
				{
					probabilityPanel.remove(probabilityScroller);
					probabilityScroller.removeAll();
				}
				probabilityScroller = null;
				probabilitySpinnerAllStatesPanel = null;

				// create new warning panel
				probabilitySpinnerAllStatesPanel = probabilityWarningMessage();
				// int width = PANEL_WIDTH
				// - new JScrollPane().getVerticalScrollBar().getSize().width;
				// probabilitySpinnerAllStatesPanel.setPreferredSize(new
				// Dimension(width,
				// PROBABILITY_PANEL_HEIGHT));
				// probabilitySpinnerAllStatesPanel.setMaximumSize(new
				// Dimension(width,
				// PROBABILITY_PANEL_HEIGHT));
				// probabilitySpinnerAllStatesPanel.setMinimumSize(new
				// Dimension(width,
				// PROBABILITY_PANEL_HEIGHT));

				// add the new warning panel
				probabilityScroller = new JScrollPane(
						probabilitySpinnerAllStatesPanel);
				int width = PANEL_WIDTH
						- new JScrollPane().getVerticalScrollBar().getSize().width;
				probabilityScroller.setPreferredSize(new Dimension(width,
						PROBABILITY_PANEL_HEIGHT));
				probabilityScroller.setMinimumSize(new Dimension(width,
						PROBABILITY_PANEL_HEIGHT));
				probabilityScroller.setMaximumSize(new Dimension(width,
						PROBABILITY_PANEL_HEIGHT));
				probabilityScroller
						.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				probabilityPanel.add(probabilityScroller, new GBC(1, 1)
						.setSpan(2, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0)
						.setAnchor(GBC.WEST).setInsets(1));

				// then disable
				this.probabilityRadioButton.setEnabled(false);
			}

			// disable the probability components
			enableDisableComponents();
		}
	}

	/**
	 * Resets the panel that displays probability for every state. This changes
	 * the number of colors (states) that are displayed.
	 */
	private void resetProbabililtyPanel()
	{
		// if possible, keep the percentages currently typed into the spinners
		Integer[] percents = null;
		if(probabilitySpinners != null && probabilitySpinners.length > 0)
		{
			percents = new Integer[probabilitySpinners.length];
			for(int i = 0; i < probabilitySpinners.length; i++)
			{
				percents[i] = (Integer) probabilitySpinners[i].getValue();
			}
		}

		// remove the old panel
		if(probabilityScroller != null)
		{
			probabilityPanel.remove(probabilityScroller);
			probabilityScroller.removeAll();
		}
		probabilityScroller = null;
		probabilitySpinnerAllStatesPanel = null;

		// create a new panel
		probabilitySpinnerAllStatesPanel = this.createProbabilityChoosers();

		// update the values on the new panel (assuming there are the same
		// number of states as before)
		if(percents != null && probabilitySpinners != null
				&& probabilitySpinners.length == percents.length)
		{
			for(int i = 0; i < percents.length; i++)
			{
				probabilitySpinners[i].setValue(percents[i]);
			}
		}

		// add the new panel
		probabilityScroller = new JScrollPane(probabilitySpinnerAllStatesPanel);
		probabilityScroller.setPreferredSize(new Dimension(PANEL_WIDTH,
				PROBABILITY_PANEL_HEIGHT));
		probabilityScroller.setMinimumSize(new Dimension(PANEL_WIDTH,
				PROBABILITY_PANEL_HEIGHT));
		probabilityScroller.setMaximumSize(new Dimension(PANEL_WIDTH,
				PROBABILITY_PANEL_HEIGHT));
		probabilityScroller
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		probabilityPanel.add(probabilityScroller, new GBC(1, 1).setSpan(2, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
	}

	/**
	 * Reset the rectangle's width and height spinner models so that they
	 * reflect the current size of the lattice.
	 */
	public void resetRectangleSpinners()
	{
		try
		{
			// keep the old displayed values
			int oldHeight = 1;
			int oldWidth = 1;
			try
			{
				oldHeight = ((Integer) rectangleHeightSpinner.getValue())
						.intValue();
			}
			catch(Exception e)
			{
				// must be a bogus value in the spinner
				oldHeight = 1;
			}
			try
			{
				oldWidth = ((Integer) rectangleWidthSpinner.getValue())
						.intValue();
			}
			catch(Exception e)
			{
				// must be a bogus value in the spinner
				oldWidth = 1;
			}

			// get the new maximum values from the number of rows and cols
			int maxHeight = ((Integer) outerPanel.getPropertiesPanel()
					.getNumRowsSpinner().getValue()).intValue();
			int maxWidth = ((Integer) outerPanel.getPropertiesPanel()
					.getNumColumnsSpinner().getValue()).intValue();

			String latticeDescription = outerPanel.getPropertiesPanel()
					.getLatticeChooser().getSelectedItem().toString();
			if(OneDimensionalLattice.isCurrentLatticeOneDim(latticeDescription))
			{
				maxHeight = 1;
			}

			// can we still use the old display values?
			if(oldHeight > maxHeight)
			{
				// nope, so set to the closest value
				oldHeight = maxHeight;
			}
			if(oldWidth > maxWidth)
			{
				// nope, so set to the closest value
				oldWidth = maxWidth;
			}

			// reset width and height spinners
			SpinnerNumberModel heightModel = new SpinnerNumberModel(oldHeight,
					1, maxHeight, 1);
			rectangleHeightSpinner.setModel(heightModel);
			rectangleHeightSpinner.setFont(fonts.getPlainFont());
			((JSpinner.NumberEditor) rectangleHeightSpinner.getEditor())
					.getTextField().getDocument().addDocumentListener(this);

			SpinnerNumberModel widthModel = new SpinnerNumberModel(oldWidth, 1,
					maxWidth, 1);
			rectangleWidthSpinner.setModel(widthModel);
			rectangleWidthSpinner.setFont(fonts.getPlainFont());
			((JSpinner.NumberEditor) rectangleWidthSpinner.getEditor())
					.getTextField().getDocument().addDocumentListener(this);
		}
		catch(Exception e)
		{
			// do nothing -- it must have been a bogus entry in the row or
			// columns
		}
	}

	/**
	 * Reset the ellipse's width and height spinner models so that they reflect
	 * the current size of the lattice.
	 */
	public void resetEllipseSpinners()
	{
		try
		{
			// keep the old displayed values
			int oldHeight = 1;
			int oldWidth = 1;
			try
			{
				oldHeight = ((Integer) ellipseHeightSpinner.getValue())
						.intValue();
			}
			catch(Exception e)
			{
				// must be a bogus value in the spinner
				oldHeight = 1;
			}
			try
			{
				oldWidth = ((Integer) ellipseWidthSpinner.getValue())
						.intValue();
			}
			catch(Exception e)
			{
				// must be a bogus value in the spinner
				oldWidth = 1;
			}

			// get the new maximum values from the number of rows and cols
			// (row and col divided by 2)
			int maxHeight = ((Integer) outerPanel.getPropertiesPanel()
					.getNumRowsSpinner().getValue()).intValue() / 2;
			int maxWidth = ((Integer) outerPanel.getPropertiesPanel()
					.getNumColumnsSpinner().getValue()).intValue() / 2;

			String latticeDescription = outerPanel.getPropertiesPanel()
					.getLatticeChooser().getSelectedItem().toString();
			if(OneDimensionalLattice.isCurrentLatticeOneDim(latticeDescription))
			{
				maxHeight = 0;
			}

			// can we still use the old display values?
			if(oldHeight > maxHeight)
			{
				// nope, so set to the closest value
				oldHeight = maxHeight;
			}
			if(oldWidth > maxWidth)
			{
				// nope, so set to the closest value
				oldWidth = maxWidth;
			}

			// reset width and height spinners
			SpinnerNumberModel heightModel = new SpinnerNumberModel(oldHeight,
					0, maxHeight, 1);
			ellipseHeightSpinner.setModel(heightModel);
			ellipseHeightSpinner.setFont(fonts.getPlainFont());
			((JSpinner.NumberEditor) ellipseHeightSpinner.getEditor())
					.getTextField().getDocument().addDocumentListener(this);

			SpinnerNumberModel widthModel = new SpinnerNumberModel(oldWidth, 0,
					maxWidth, 1);
			ellipseWidthSpinner.setModel(widthModel);
			ellipseWidthSpinner.setFont(fonts.getPlainFont());
			((JSpinner.NumberEditor) ellipseWidthSpinner.getEditor())
					.getTextField().getDocument().addDocumentListener(this);
		}
		catch(Exception e)
		{
			// do nothing -- it must have been a bogus entry in the row or
			// columns
		}
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

	/**
	 * Selects the radio button that has the supplied name. If that name doesn't
	 * exist, it is set to the default blank initial state.
	 * 
	 * @param initialStateName
	 *            The name of the initial state, for example
	 *            CAPropertyReader.STATE_BLANK or the value stored in
	 *            CAPropertyReader.INIT_STATE.
	 */
	public void setActiveRadioButton(String initialStateName)
	{
		// whether or not we found the specified initial state
		boolean setAnInitialState = false;

		// go through each button until find the button with the
		// initialStateName
		Enumeration allButtons = initialStatesButtonGroup.getElements();
		while(allButtons.hasMoreElements())
		{
			JRadioButton button = (JRadioButton) allButtons.nextElement();
			if(button.getText().equals(initialStateName))
			{
				button.setSelected(true);
				setAnInitialState = true;
			}
		}

		if(!setAnInitialState)
		{
			blankRadioButton.setSelected(true);
		}
	}

	/**
	 * Change the radio buttons to reflect the latest selected rule (each rule
	 * can generate a list of initial states).
	 */
	public void updateRuleGeneratedInitialStates()
	{
		removeRadioButtonsGeneratedByRule();
		ruleGeneratedRadioButtonList = createRuleGeneratedRadioButtons();
		this.ruleGeneratedHash = null;
		ruleGeneratedJPanels = createRuleGeneratedInitialStateJPanels(null);
		addRadioButtonsGeneratedByRule(ruleGeneratedInitStatePanel);

		// and change the title of the panel to the latest rule
		updateRuleGeneratedTitle();

		// we may have changed the components that are present (by selecting a
		// rule that has some rule generated initial state with a JPanel).
		enableDisableComponents();
	}

	/* ********************************************************************* */
	/*
	 * THE FOLLOWING METHODS REACT TO CHANGES IN INIT STATES BY TELLING THE
	 * SUBMIT BUTTON TO CHANGE COLORS AND VARIOUS COMPONENTS TO BE DISABLED OR
	 * ENABLED.
	 */

	/**
	 * Reacts to a change in any registered component.
	 */
	public void actionPerformed(ActionEvent e)
	{
		Object oSource = e.getSource();
		if(oSource != null && submitButton != null)
		{
			submitButton.setForeground(Color.RED);
		}

		enableDisableComponents();
	}

	/**
	 * Reacts to a change in any registered component.
	 */
	public void insertUpdate(DocumentEvent e)
	{
		// has one of the components changed?
		boolean hasChanged = imageFilePathTextField.isFocusOwner()
				|| dataFilePathTextField.isFocusOwner()
				|| hasSpinnerChanged(randomPercentSpinner)
				|| hasSpinnerChanged(ellipseWidthSpinner)
				|| hasSpinnerChanged(ellipseHeightSpinner)
				|| hasSpinnerChanged(rectangleWidthSpinner)
				|| hasSpinnerChanged(rectangleHeightSpinner);

		// include the probability spinners
		if(probabilitySpinners != null)
		{
			for(JSpinner spinner : probabilitySpinners)
			{
				hasChanged = hasChanged || hasSpinnerChanged(spinner);
			}
		}

		Object oSource = e.getDocument();
		if(oSource != null && submitButton != null && hasChanged)
		{
			submitButton.setForeground(Color.RED);
		}
	}

	/**
	 * Gets the panel holding the most common initial state. This panel can
	 * pulsate.
	 * 
	 * @return The panel that holds the most common initial states.
	 */
	public PulsatingJPanel getMostCommonInitialStatesPanel()
	{
		return mostCommonInitStatePanel;
	}

	/**
	 * Move the rule-generated initial states to near the top of the screen.
	 */
	// public void moveRuleInitialStatesUp()
	// {
	// // animates the layout when the control panel is being moved to the
	// // right.
	// Animator animator = new Animator(MOVE_UP_ANIMATION_LENGTH);
	//
	// animator.setAcceleration(0.3f);
	// animator.setDeceleration(0.2f);
	//
	// animator.addTarget(new PropertySetter(controlPanel, "location",
	// new Point(graphicsScrollPane.getX(), controlPanel.getY())));
	//
	// animator.addTarget(new PropertySetter(graphicsScrollPane, "location",
	// new Point(controlPanel.getWidth(), graphicsScrollPane.getY())));
	//
	// // start the animation
	// animator.start();
	//
	// createTopLayout();
	// }
	/**
	 * Reacts to a change in any registered component.
	 */
	public void removeUpdate(DocumentEvent e)
	{
		// has one of the components changed?
		boolean hasChanged = imageFilePathTextField.isFocusOwner()
				|| dataFilePathTextField.isFocusOwner()
				|| hasSpinnerChanged(randomPercentSpinner)
				|| hasSpinnerChanged(ellipseWidthSpinner)
				|| hasSpinnerChanged(ellipseHeightSpinner)
				|| hasSpinnerChanged(rectangleWidthSpinner)
				|| hasSpinnerChanged(rectangleHeightSpinner);

		// include the probability spinners
		if(probabilitySpinners != null)
		{
			for(JSpinner spinner : probabilitySpinners)
			{
				hasChanged = hasChanged || hasSpinnerChanged(spinner);
			}
		}

		Object oSource = e.getDocument();
		if(oSource != null && submitButton != null && hasChanged)
		{
			submitButton.setForeground(Color.RED);
		}
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

	/**
	 * The number of states, usually from the properties panel, but if that is
	 * not legal, then from the CA properties..
	 * 
	 * @return the number of states.
	 */
	private int getNumStates()
	{
		// first get numStates from the properties -- this is the default value
		int numStates = CurrentProperties.getInstance().getNumStates();

		// then get from the actual properties panel -- that's what we really
		// want (but we need the default value in case this one is bogus, like
		// "34a")
		try
		{
			// in CAControlPanel, I set the number of states right before the
			// call to getNumStatesField.setText(). That method call fires an
			// event that calls this method. But in that case, this method calls
			// getText() (inside this if statement) which can cause a crash. So
			// this checks to avoid the crash.
			if(!CAController.currentlyRunningSetup)
			{
				String numberOfStates = outerPanel.getPropertiesPanel()
						.getNumStatesField().getText();
				numStates = Integer.parseInt(numberOfStates);
			}
		}
		catch(Throwable e)
		{
			// do nothing -- we'll use the default.
		}

		// make sure the number is reasonable
		if(numStates <= 1)
		{
			numStates = CurrentProperties.getInstance().getNumStates();
		}

		return numStates;
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
	private boolean hasSpinnerChanged(JSpinner spinner)
	{
		// the value that will be returned (will be set to false if no change
		// has occurred)
		boolean hasChanged = true;

		JFormattedTextField textField = ((JSpinner.NumberEditor) spinner
				.getEditor()).getTextField();

		// make sure the spinner value has changed -- this deals with the
		// annoying possibility that a DocumentEvent was fired just because a
		// cursor was placed in the text field of the JSpinner.
		if(textField.isFocusOwner())
		{
			String displayedValue = textField.getText();
			String lastValidValue = ((Integer) spinner.getValue()).toString();

			if(displayedValue == null || lastValidValue == null
					|| displayedValue.equals("") || lastValidValue.equals("")
					|| displayedValue.equals(lastValidValue))
			{
				// value has not changed, so do nothing
				hasChanged = false;
			}
		}
		else
		{
			hasChanged = false;
		}

		return hasChanged;
	}
}
