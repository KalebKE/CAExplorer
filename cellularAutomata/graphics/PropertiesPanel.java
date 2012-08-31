/*
 PropertiesPanel -- a class within the Cellular Automaton Explorer. 
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
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

import cellularAutomata.CurrentProperties;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.MooreRadiusOneDimLattice;
import cellularAutomata.lattice.MooreRadiusTwoDimLattice;
import cellularAutomata.lattice.RandomGaussianLattice;
import cellularAutomata.lattice.SquareLattice;
import cellularAutomata.lattice.VonNeumannRadiusLattice;
import cellularAutomata.reflection.LatticeHash;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.reflection.RuleHash;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.ToolTipLatticeComboBox;
import cellularAutomata.util.graphics.PulsatingJEditorPane;
import cellularAutomata.util.graphics.PulsatingTextJButton;
import cellularAutomata.util.graphics.PulsatingJTextField;

/**
 * The JPanel that contains all of the CA Properties such as lattice type,
 * lattice size, etc.
 * 
 * @author David Bahr
 */
public class PropertiesPanel extends JPanel implements ActionListener,
		DocumentListener, ChangeListener
{
	/**
	 * The maximum number of allowed columns on the lattice.
	 */
	public static final int MAX_COLUMN_VALUE = 10000;

	/**
	 * The maximum number of allowed rows on the lattice.
	 */
	public static final int MAX_ROW_VALUE = 10000;

	/**
	 * Title for the properties panel.
	 */
	public static final String PROPERTY_PANEL_TITLE = "Lattice and Properties";

	/**
	 * String used for text display on the submit button and for setting its
	 * action command.
	 */
	public static final String SUBMIT_PROPERTIES = "Submit Changes";

	/**
	 * A tool tip for the properties panel.
	 */
	public static final String TOOL_TIP = "<html><body>select simulation "
			+ "properties</body></html>";

	// width of panels
	private static final int PANEL_WIDTH = 340;

	// The tooltip for the columns
	private static final String COL_TIP = "<html>Number of horizontal cells."
			+ "</html>";

	// the title of the "for best results" section
	private static final String FOR_BEST_RESULTS_PANEL_TITLE = "For best results";

	// the title of the lattice section
	private static final String LATTICE_PANEL_TITLE = "Topology";

	// The tooltip for the number of states
	private static final String NUM_STATES_TIP = "<html>Number of different "
			+ "possible values for a cell.</html>";

	// The tooltip for the radius
	private static final String RADIUS_TIP = "<html>The radius of the neighborhood "
			+ "(radius 1 is nearest neighbor). <br> Typical values are 1 to 10. "
			+ "Large values require lots of memory.</html>";

	// The tooltip for the wrap around boundary condition
	private static final String REFLECTION_BOUNDARY_TIP = "<html>A less common "
			+ "boundary condition. <br><br>"
			+ "Cells \"beyond\" the edge of the grid are <br>"
			+ "reflections of cells within the grid. i.e., <br>"
			+ "a cell sitting on the left edge will have a <br>"
			+ "left neighbor that is the same as (a <br>"
			+ "reflection of) its right neighbor. <br><br>"
			+ "<b>Warning!</b> This boundary condition will <br>"
			+ "not work well with lattice gas and other <br>"
			+ "rules that \"conserve mass\".  Cells on the <br>"
			+ "boundary may generate mass by copying a <br>"
			+ "state seen in the reflection.  Mass may also <br>"
			+ "be lost when it is not seen in any other <br>"
			+ "cell's reflection.</html>";

	// The tooltip for the rows
	private static final String ROW_TIP = "<html>Number of vertical cells."
			+ "</html>";

	// the title of the rule section
	private static final String NUMSTATES_PANEL_TITLE = "States";

	// The tooltip for the running average
	private static final String RUNNING_AVERAGE_TIP = "<html>Average the values "
			+ "from this many generations.</html>";

	// the title of the running average section
	private static final String RUNNING_AVERAGE_PANEL_TITLE = "Averaging";

	// the tooltip for the standard deviation of the Guassian random
	// neighborhood (in the Guassian random lattice)..
	private static final String STANDARD_DEVIATION_TOOLTIP = "<html>"
			+ "Standard deviation of the distribution of neighbors in the <br>"
			+ RandomGaussianLattice.DISPLAY_NAME
			+ " lattice.  Larger numbers mean <br>"
			+ "that a cell's neighbors are more widely dispersed.</html>";

	// The tooltip for the wrap around boundary condition
	private static final String WRAP_AROUND_BOUNDARY_TIP = "<html>The recommended "
			+ "boundary condition. <br><br>"
			+ "The left side is attached to the right <br>"
			+ "side, and the top is attached to the <br>"
			+ "bottom.  E.g., anything moving to the <br>"
			+ "left will reappear on the right.</html>";

	// The encompassing panel onto which this one will be added.
	private AllPanel outerPanel = null;

	// A listener for components on this panel.
	private AllPanelListener listener = null;

	// the border used for the "Best results" panel
	private Border bestResultsTitledBorder = null;

	// Box for the boundary condition radio buttons
	private Box boundaryConditionBox = null;

	// the group of boundary condition radio buttons
	private ButtonGroup boundaryConditionGroup = null;

	// default color for the submit button text
	private Color defaultSubmitButtonColor = Color.gray;

	// color for the titles of sections
	private Color titleColor = Color.BLUE;

	// title font (for titles of sections)
	private Font titleFont = null;

	// fonts for display
	private Fonts fonts = null;

	// Submit property changes
	private JButton submitButton = null;

	// the editor pane used to display the "for best results" html
	private PulsatingJEditorPane editorPane = null;

	// the panel that describes how to get best results
	private JPanel bestResultsPanel = null;

	// The inner panel that holds all of the buttons
	private JPanel innerRaisedPanel = null;

	// radio button for wrap-around boundary conditions
	private JRadioButton wrapAroundBoundaryButton = null;

	// radio button for wrap-around boundary conditions
	private JRadioButton reflectionBoundaryButton = null;

	// the scroll panel that holds the rule description
	private JScrollPane editorScrollPane = null;

	// set the width of the CA (number of cells).
	private JSpinner numColsSpinner = null;

	// set the height of the CA (number of cells).
	private JSpinner numRowsSpinner = null;

	// set the number of states.
	private JTextField numStates = null;

	// the number of cell generations to average.
	private JTextField runningAverage = null;

	// button for additional properties
	private PulsatingTextJButton additionalPropertiesButton = null;

	// set the radius of the neighborhood.
	private PulsatingJTextField radius = null;

	// set the standard deviation of the neighborhood.
	private PulsatingJTextField standardDeviation = null;

	// Choose CA lattice.
	private ToolTipLatticeComboBox latticeChooser = null;

	/**
	 * The panel containing the CA properties such as lattice type, lattice
	 * size, etc.
	 * 
	 * @param outerPanel
	 *            The encompassing panel onto which this one will be added.
	 */
	public PropertiesPanel(AllPanel outerPanel)
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
	}

	/**
	 * Create the panel that holds the lattice chooser, rule chooser, etc.
	 */
	private void addComponents()
	{
		// in case this has been called before, clear it out.
		this.removeAll();

		// create the inner panel
		innerRaisedPanel = createPropertyInnerPanel();
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
		JPanel submitPanel = createPropertyButtons();

		// create a layout
		this.setLayout(new GridBagLayout());
		this.add(innerScrollPanel, new GBC(0, 1).setSpan(1, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		this.add(submitPanel, new GBC(0, 2).setSpan(1, 1).setFill(
				GBC.HORIZONTAL).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));
		
		// react to specified keystrokes
		bindKeystrokes();
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
	 * Create radio buttons to choose a boundary condition.
	 * 
	 * @return a panel containing the buttons.
	 */
	private Box createBoundaryRadioButtons()
	{
		wrapAroundBoundaryButton = new JRadioButton("Wrap-around");
		wrapAroundBoundaryButton.setFont(fonts.getPlainFont());
		wrapAroundBoundaryButton.setActionCommand(""
				+ Lattice.WRAP_AROUND_BOUNDARY);
		wrapAroundBoundaryButton.setToolTipText(WRAP_AROUND_BOUNDARY_TIP);
		wrapAroundBoundaryButton.addActionListener(this);

		reflectionBoundaryButton = new JRadioButton("Reflection");
		reflectionBoundaryButton.setFont(fonts.getPlainFont());
		reflectionBoundaryButton.setActionCommand(""
				+ Lattice.REFLECTION_BOUNDARY);
		reflectionBoundaryButton.setToolTipText(REFLECTION_BOUNDARY_TIP);
		reflectionBoundaryButton.addActionListener(this);

		// set the boundary condition from the properties
		int boundaryType = Lattice.WRAP_AROUND_BOUNDARY;
		try
		{
			boundaryType = CurrentProperties.getInstance()
					.getBoundaryCondition();
		}
		catch(Exception e)
		{
			// do nothing -- just keep the default wrap-around condition
		}
		setBoundaryConditionChoice(boundaryType);

		// put them in a group so that they behave as radio buttons
		boundaryConditionGroup = new ButtonGroup();
		boundaryConditionGroup.add(wrapAroundBoundaryButton);
		boundaryConditionGroup.add(reflectionBoundaryButton);

		// the amount of vertical space to put between components
		int verticalSpace = 0;

		// create a box holding the buttons
		Box buttonBox = Box.createVerticalBox();
		buttonBox.add(wrapAroundBoundaryButton);
		buttonBox.add(Box.createVerticalStrut(verticalSpace));
		buttonBox.add(reflectionBoundaryButton);
		// buttonBox.setBackground(Color.RED);

		// now add to a JPanel
		// JPanel radioPanel = new JPanel();
		// radioPanel.add(buttonBox);

		return buttonBox;
	}

	/**
	 * Create spinner for the number of columns.
	 * 
	 * @return the spinner for columns.
	 */
	private JSpinner createColSpinner()
	{
		// column spinner
		int initialColumns = CurrentProperties.getInstance().getNumColumns();
		SpinnerNumberModel columnModel = new SpinnerNumberModel(initialColumns,
				2, MAX_COLUMN_VALUE, 1);
		JSpinner columnSpinner = new JSpinner(columnModel);
		columnSpinner.setToolTipText(COL_TIP);
		columnSpinner.addChangeListener(listener);
		columnSpinner.addChangeListener(this);
		((JSpinner.NumberEditor) columnSpinner.getEditor()).getTextField()
				.getDocument().addDocumentListener(this);

		return columnSpinner;
	}

	/**
	 * Creates a panel holding the rule's "best results" description.
	 * 
	 * @return A panel holding an html description of how to get the best
	 *         results from the rule.
	 */
	private JPanel createForBestResultsPanel()
	{
		// currently selected rule and description.
		String ruleClassName = CurrentProperties.getInstance()
				.getRuleClassName();
		String ruleDisplayName = "";
		String description = null;

		if(ruleClassName != null && !ruleClassName.equals(""))
		{
			Rule rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(ruleClassName);

			if(rule != null)
			{
				description = rule.getBestResultsDescription();
				ruleDisplayName = rule.getDisplayName();
			}
		}

		if(description == null)
		{
			description = "No suggestions provided.";
		}

		// put it in an editor pane with a scroll bar
		editorPane = new PulsatingJEditorPane("text/html", description);

		// put editor pane in a scroll bar
		editorPane.setEditable(false);
		editorScrollPane = new JScrollPane(editorPane);
		editorScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		// and set to the beginning of the text so the scrollbar is
		// positioned properly
		editorPane.setCaretPosition(0);

		// set the size
		editorPane.setPreferredSize(new Dimension(PANEL_WIDTH, 250));
		Dimension scrollPaneDimension = new Dimension(editorPane
				.getPreferredScrollableViewportSize().width, 200);
		editorScrollPane.setPreferredSize(scrollPaneDimension);
		editorScrollPane.setMaximumSize(scrollPaneDimension);
		editorScrollPane.setMinimumSize(scrollPaneDimension);

		// and set to the beginning of the text so the scrollbar is
		// positioned properly
		editorPane.setCaretPosition(0);

		// create the panel that holds the editor pane
		JPanel descriptionPanel = new JPanel(new GridBagLayout());
		bestResultsTitledBorder = BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				FOR_BEST_RESULTS_PANEL_TITLE + " with " + ruleDisplayName,
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		descriptionPanel.setBorder(bestResultsTitledBorder);

		// add the scroll pane to the JPanel
		int row = 0;
		descriptionPanel.add(editorScrollPane, new GBC(1, row).setSpan(1, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
				.setInsets(1));

		return descriptionPanel;
	}

	/**
	 * Create a combo box for the lattices and put in a JPanel.
	 * 
	 * @return contains the combo box.
	 */
	private ToolTipLatticeComboBox createLatticeChooser()
	{
		// current choice (or default from properties file)
		String latticeChoice = CurrentProperties.getInstance()
				.getLatticeDisplayName();

		// get the available lattices
		ArrayList<String> listOfLattices = new ArrayList<String>();
		LatticeHash hash = new LatticeHash();
		Iterator iterator = hash.valuesIterator();
		while(iterator.hasNext())
		{
			listOfLattices.add(ReflectionTool
					.getLatticeDescriptionFromClassName((String) iterator
							.next()));
		}

		// convert to an array
		String[] latticeChoices = new String[listOfLattices.size()];
		for(int i = 0; i < listOfLattices.size(); i++)
		{
			latticeChoices[i] = (String) listOfLattices.get(i);
		}
		// JLabel[] latticeChoices = new JLabel[listOfLattices.size()];
		// for(int i = 0; i < listOfLattices.size(); i++)
		// {
		// latticeChoices[i] = new JLabel((String) listOfLattices.get(i));
		// }

		// sort the array (looks better)
		Arrays.sort(latticeChoices);

		// create a combo box with the available lattices
		ToolTipLatticeComboBox latticeChooser = new ToolTipLatticeComboBox(
				latticeChoices);

		// color the most widely used lattices gives the user a visual cue.
		latticeChooser.colorMostCommonLattices();

		// disable lattices that are incompatible with the current rule
		latticeChooser.enableOnlyCompatibleLattices(CurrentProperties
				.getInstance().getRuleClassName());

		// set the currently selected lattice
		if(latticeChoice != null)
		{
			// if the value of lattice isn't in the list then leaves as the
			// default
			latticeChooser.setSelectedItem(latticeChoice);
		}
		else
		{
			latticeChooser.setSelectedItem(SquareLattice.DISPLAY_NAME);
		}

		latticeChooser.setActionCommand(CurrentProperties.LATTICE);
		latticeChooser.addActionListener(listener);
		latticeChooser.addActionListener(this);
		latticeChooser.setFont(fonts.getBoldSmallerFont());
		latticeChooser.setToolTipText(ToolTipLatticeComboBox.DEFAULT_TIP);

		return latticeChooser;
	}

	/**
	 * Create a panel holding lattice related stuff.
	 * 
	 * @return The panel with the lattice chooser, etc.
	 */
	private JPanel createLatticePanel()
	{
		// components
		latticeChooser = createLatticeChooser();
		radius = createRadiusField();
		standardDeviation = createStandardDeviationField();
		numColsSpinner = createColSpinner();
		numRowsSpinner = createRowSpinner();
		boundaryConditionBox = createBoundaryRadioButtons();

		// JLabel mostCommonLabel = new JLabel("The most common "
		// + "lattices are highlighted blue (see their tooltips).");
		// mostCommonLabel.setFont(fonts.getItalicSmallerFont());
		// mostCommonLabel.setForeground(Color.BLUE.darker());

		// labels for the lattice panel
		JLabel latticeLabel = new JLabel("Lattice:   ");
		JLabel radiusLabel = new JLabel("Neighborhood radius: ");
		radiusLabel.setFont(fonts.getPlainFont());
		JLabel standardDeviationLabel = new JLabel("Neighborhood std dev: ");
		standardDeviationLabel.setFont(fonts.getPlainFont());
		JLabel rowLabel = new JLabel("Rows (height): ");
		rowLabel.setFont(fonts.getPlainFont());
		JLabel colLabel = new JLabel("Columns (width): ");
		colLabel.setFont(fonts.getPlainFont());
		JLabel boundaryConditionLabel = new JLabel("Boundary condition: ");
		boundaryConditionLabel.setFont(fonts.getPlainFont());

		JPanel latticePanel = new JPanel();
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border innerEmptyBorder = BorderFactory.createEmptyBorder(2, 6, 3, 2);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), LATTICE_PANEL_TITLE, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		latticePanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder, (BorderFactory.createCompoundBorder(
						titledBorder, innerEmptyBorder))));
		latticePanel.setBorder(titledBorder);
		latticePanel.setLayout(new GridBagLayout());

		// lattice chooser
		// int row = 0;
		// latticePanel.add(mostCommonLabel, new GBC(0, row).setSpan(5, 1)
		// .setFill(GBC.NONE).setWeight(0.0, 1.0).setAnchor(GBC.WEST)
		// .setInsets(5, 1, 1, 1));

		// lattice chooser
		int row = 0;
		latticePanel.add(latticeLabel, new GBC(0, row).setSpan(2, 1).setFill(
				GBC.NONE).setWeight(0.0, 1.0).setAnchor(GBC.WEST).setInsets(1,
				1, 15, 1));
		latticePanel.add(latticeChooser, new GBC(2, row).setSpan(3, 1).setFill(
				GBC.NONE).setWeight(0.0, 1.0).setAnchor(GBC.WEST).setInsets(1,
				1, 15, 1));

		// radius text field
		row++;
		latticePanel.add(new JLabel("    "), new GBC(0, row).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));
		latticePanel.add(radiusLabel, new GBC(1, row).setSpan(2, 1).setFill(
				GBC.BOTH).setWeight(1.0, 0.0).setAnchor(GBC.WEST).setInsets(1));
		latticePanel.add(radius, new GBC(3, row).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// standard deviation text field
		row++;
		latticePanel.add(new JLabel("    "), new GBC(0, row).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));
		latticePanel.add(standardDeviationLabel, new GBC(1, row).setSpan(2, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));
		latticePanel.add(standardDeviation, new GBC(3, row).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// row num text field
		row++;
		latticePanel.add(rowLabel, new GBC(1, row).setSpan(2, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		latticePanel.add(numRowsSpinner, new GBC(3, row).setSpan(1, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		// col num text field
		row++;
		latticePanel.add(colLabel, new GBC(1, row).setSpan(2, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		latticePanel.add(numColsSpinner, new GBC(3, row).setSpan(1, 1).setFill(
				GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		// boundary condition buttons
		row++;
		latticePanel.add(boundaryConditionLabel, new GBC(1, row).setSpan(2, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		latticePanel.add(wrapAroundBoundaryButton, new GBC(3, row)
				.setSpan(1, 1).setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(
						GBC.WEST).setInsets(1, 0, 0, 1));

		row++;
		latticePanel.add(reflectionBoundaryButton, new GBC(3, row)
				.setSpan(1, 1).setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(
						GBC.WEST).setInsets(0, 0, 1, 1));

		return latticePanel;
	}

	/**
	 * Create text field for setting the number of CA states.
	 * 
	 * @return Field for setting the number of states.
	 */
	private JTextField createNumStatesField()
	{
		// add text field for choosing the rule number
		JTextField numStates = new JTextField(""
				+ CurrentProperties.getInstance().getNumStates());
		numStates.setColumns(2);
		numStates.setToolTipText(NUM_STATES_TIP);

		// add listeners
		numStates.getDocument().addDocumentListener(listener);
		numStates.getDocument().addDocumentListener(this);

		return numStates;
	}

	/**
	 * Create a panel holding rule related stuff.
	 * 
	 * @return The panel with the rule tree, etc.
	 */
	private JPanel createNumStatesPanel()
	{
		// components
		numStates = createNumStatesField();

		JLabel numStatesLabel = new JLabel("Number of states: ");

		JPanel rulePanel = new JPanel();
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		Border innerEmptyBorder = BorderFactory.createEmptyBorder(2, 0, 3, 2);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), NUMSTATES_PANEL_TITLE,
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		rulePanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder, (BorderFactory.createCompoundBorder(
						titledBorder, innerEmptyBorder))));
		rulePanel.setLayout(new GridBagLayout());

		int row = 0;
		rulePanel.add(numStatesLabel, new GBC(0, row).setSpan(1, 1).setFill(
				GBC.VERTICAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		rulePanel.add(numStates, new GBC(1, row).setSpan(1, 1).setFill(
				GBC.VERTICAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		rulePanel.add(Box.createRigidArea(new Dimension(125, 1)), new GBC(2,
				row).setSpan(1, 1).setFill(GBC.VERTICAL).setWeight(1.0, 1.0)
				.setAnchor(GBC.WEST).setInsets(1));

		return rulePanel;
	}

	/**
	 * Create a submit button for property changes.
	 * 
	 * @return The submit button in a panel.
	 */
	private JPanel createPropertyButtons()
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
	 * Create and arrange the raised inner panel that holds property controls.
	 * 
	 * @param propertiesPanel
	 *            The panel holding the properties controls (lattice choice,
	 *            rule choice, etc.).
	 */
	private JPanel createPropertyInnerPanel()
	{
		// the panel on which we add the controls
		JPanel innerPanel = new JPanel();
		innerPanel.setBorder(BorderFactory.createRaisedBevelBorder());

		GridBagLayout layout = new GridBagLayout();
		innerPanel.setLayout(layout);

		// create panels that group the controls together
		JPanel latticePanel = createLatticePanel();
		JPanel numStatesPanel = createNumStatesPanel();
		JPanel runningAvgPanel = createRunningAveragePanel();
		bestResultsPanel = createForBestResultsPanel();

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
		innerPanel.add(latticePanel, new GBC(1, row).setSpan(8, 1).setFill(
				GBC.BOTH).setWeight(1.0, 0.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		innerPanel.add(numStatesPanel, new GBC(1, row).setSpan(8, 1).setFill(
				GBC.BOTH).setWeight(1.0, 0.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		innerPanel.add(runningAvgPanel, new GBC(1, row).setSpan(8, 1).setFill(
				GBC.BOTH).setWeight(1.0, 0.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		innerPanel.add(bestResultsPanel, new GBC(1, row).setSpan(8, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		return innerPanel;
	}

	/**
	 * Create text field for the radius.
	 * 
	 * @return the text field for the radius.
	 */
	private PulsatingJTextField createRadiusField()
	{
		// add text field for choosing the radius of the neighborhood
		int iRadius = CurrentProperties.getInstance().getNeighborhoodRadius();
		PulsatingJTextField radius = new PulsatingJTextField("" + iRadius);
		radius.setColumns(2);
		radius.setToolTipText(RADIUS_TIP);
		radius.getDocument().addDocumentListener(this);

		// when the radius is created, we need to set its value in the
		// lattices. (This is annoyingly high connectivity,
		// but is a consequence of the lattice classes being instantiated
		// without the properties in some cases -- there is no way for the
		// lattice to read the properties to get the radius.)
		MooreRadiusOneDimLattice.radius = iRadius;
		MooreRadiusTwoDimLattice.radius = iRadius;
		VonNeumannRadiusLattice.radius = iRadius;

		return radius;
	}

	/**
	 * Create spinner for the number of rows.
	 * 
	 * @return the spinner for rows.
	 */
	private JSpinner createRowSpinner()
	{
		// row spinner
		int initialRows = CurrentProperties.getInstance().getNumRows();
		SpinnerNumberModel rowModel = new SpinnerNumberModel(initialRows, 2,
				MAX_ROW_VALUE, 1);
		JSpinner rowSpinner = new JSpinner(rowModel);
		rowSpinner.setToolTipText(ROW_TIP);
		rowSpinner.addChangeListener(listener);
		rowSpinner.addChangeListener(this);
		((JSpinner.NumberEditor) rowSpinner.getEditor()).getTextField()
				.getDocument().addDocumentListener(this);

		return rowSpinner;
	}

	/**
	 * Create text field for the number of generations to average.
	 * 
	 * @return the text field for rows.
	 */
	private JTextField createRunningAverageField()
	{
		// add text field for choosing the row dimension of the graphics
		JTextField runningAverageField = new JTextField(""
				+ CurrentProperties.getInstance().getRunningAverage());
		runningAverageField.setToolTipText(RUNNING_AVERAGE_TIP);
		runningAverageField.setColumns(2);
		runningAverageField.getDocument().addDocumentListener(listener);
		runningAverageField.getDocument().addDocumentListener(this);

		return runningAverageField;
	}

	/**
	 * Create a panel holding running average related stuff.
	 * 
	 * @return The panel with the running average number, etc.
	 */
	private JPanel createRunningAveragePanel()
	{
		// components
		runningAverage = createRunningAverageField();

		// labels for the panel
		JLabel runningAverageLabel = new JLabel("Running average:");

		JPanel avgPanel = new JPanel();
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border innerEmptyBorder = BorderFactory.createEmptyBorder(2, 6, 3, 2);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), RUNNING_AVERAGE_PANEL_TITLE,
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		avgPanel.setBorder(BorderFactory.createCompoundBorder(outerEmptyBorder,
				(BorderFactory.createCompoundBorder(titledBorder,
						innerEmptyBorder))));
		avgPanel.setBorder(titledBorder);
		avgPanel.setLayout(new GridBagLayout());

		// running average field
		int row = 0;
		avgPanel.add(runningAverageLabel, new GBC(0, row).setSpan(1, 1)
				.setFill(GBC.VERTICAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		avgPanel.add(runningAverage, new GBC(1, row).setSpan(1, 1).setFill(
				GBC.VERTICAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		avgPanel.add(Box.createRigidArea(new Dimension(110, 1)),
				new GBC(2, row).setSpan(1, 1).setFill(GBC.VERTICAL).setWeight(
						1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		return avgPanel;
	}

	/**
	 * Create text field for the standard deviation.
	 * 
	 * @return the text field for the standard deviation.
	 */
	private PulsatingJTextField createStandardDeviationField()
	{
		// add text field for choosing the radius of the neighborhood
		double standardDeviation = CurrentProperties.getInstance()
				.getStandardDeviation();
		PulsatingJTextField stdev = new PulsatingJTextField(""
				+ standardDeviation);
		stdev.setColumns(2);
		stdev.setToolTipText(STANDARD_DEVIATION_TOOLTIP);
		stdev.getDocument().addDocumentListener(this);

		// when the standard deviation is created, we need to set its value in
		// the lattices. (This is annoyingly high connectivity,
		// but is a consequence of the lattice classes being instantiated
		// without the properties in some cases -- there is no way for the
		// lattice to read the properties to get the standard deviation.)
		RandomGaussianLattice.standardDeviation = standardDeviation;

		return stdev;
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
	 * Gets the button used to display the tab with additional properties for
	 * the currently selected rule.
	 * 
	 * @return The button used to display the tab with additional properties.
	 */
	public PulsatingTextJButton getAdditionalPropertiesButton()
	{
		return this.additionalPropertiesButton;
	}

	/**
	 * Gets the editor pane that displays the "For best results" text.
	 * 
	 * @return The "For best results" editor pane.
	 */
	public PulsatingJEditorPane getBestResultsEditorPane()
	{
		return editorPane;
	}

	/**
	 * Gets the group of radio buttons for the selected boundary condition.
	 * 
	 * @return The group of boundary condition radio buttons.
	 */
	public ButtonGroup getBoundaryConditionRadioButtons()
	{
		return boundaryConditionGroup;
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
	 * Gets the drop down menu for choosing the current CA lattice.
	 * 
	 * @return The current lattice chooser.
	 */
	public JComboBox getLatticeChooser()
	{
		return latticeChooser;
	}

	/**
	 * Gets the spinner holding the number of columns.
	 * 
	 * @return The number of columns spinner.
	 */
	public JSpinner getNumColumnsSpinner()
	{
		return numColsSpinner;
	}

	/**
	 * Gets the spinner holding the number of rows.
	 * 
	 * @return The number of rows spinner.
	 */
	public JSpinner getNumRowsSpinner()
	{
		return numRowsSpinner;
	}

	/**
	 * Gets the field holding the number of states.
	 * 
	 * @return The number of states field.
	 */
	public JTextField getNumStatesField()
	{
		return numStates;
	}

	/**
	 * Gets the field holding the radius of the neighborhood.
	 * 
	 * @return The radius field.
	 */
	public PulsatingJTextField getRadiusField()
	{
		return radius;
	}

	/**
	 * Gets the field holding the running average number.
	 * 
	 * @return The running average field.
	 */
	public JTextField getRunningAverageField()
	{
		return runningAverage;
	}

	/**
	 * Gets the field holding the standard deviation of the neighborhood.
	 * 
	 * @return The standard deviation field.
	 */
	public PulsatingJTextField getStandardDeviationField()
	{
		return standardDeviation;
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

		// now reset each element one by one
		latticeChooser.setSelectedItem(properties.getLatticeDisplayName());
		radius.setText("" + properties.getNeighborhoodRadius());
		standardDeviation.setText("" + properties.getStandardDeviation());
		numColsSpinner.setValue(new Integer(properties.getNumColumns()));
		numRowsSpinner.setValue(new Integer(properties.getNumRows()));
		numStates.setText("" + properties.getNumStates());
		runningAverage.setText("" + properties.getRunningAverage());

		// Now reset the rule/description/initial state panels as well (this
		// reset is the one that gets called elsewhere, so we just need to
		// forward to the RulePanel. InitialStatesPanel, etc.).
		outerPanel.getRulePanel().reset();
		outerPanel.getInitialStatesPanel().reset();
		outerPanel.getDescriptionPanel().reset();

		int boundaryType = properties.getBoundaryCondition();
		setBoundaryConditionChoice(boundaryType);

		// disable components as necessary
		outerPanel.getController().disableNumStatesField(true);
		outerPanel.getController().disableRunningAverageField(true);
		outerPanel.getController().disableRadiusField();
		outerPanel.getController().disableStandardDeviationField();

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

	/**
	 * Select the correct radio button based on the boundary type.
	 * 
	 * @param boundaryType
	 *            Specifies the type boundary (for example, reflection or
	 *            wrap-around). Appropriate values are specified in the Lattice
	 *            interface.
	 */
	public void setBoundaryConditionChoice(int boundaryType)
	{
		if(reflectionBoundaryButton != null && wrapAroundBoundaryButton != null)
		{
			if(boundaryType == Lattice.REFLECTION_BOUNDARY)
			{
				reflectionBoundaryButton.setSelected(true);
			}
			else
			{
				wrapAroundBoundaryButton.setSelected(true);
			}
		}
	}

	/**
	 * Sets the text and title of the "For best results" panel using text from
	 * the given rule.
	 * 
	 * @param ruleName
	 *            The descriptive name of the rule.
	 */
	public void setForBestResultsText(String ruleName)
	{
		String description = null;
		String ruleDisplayName = null;

		RuleHash ruleHash = new RuleHash();
		String ruleClassName = ruleHash.get(ruleName);

		if(ruleClassName != null && !ruleClassName.equals(""))
		{
			Rule rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(ruleClassName);

			if(rule != null)
			{
				description = rule.getBestResultsDescription();
				ruleDisplayName = rule.getDisplayName();
			}
		}

		if(description == null)
		{
			description = "No suggestions provided.";
		}

		// set the new text
		editorPane.setText(description);

		// and set to the beginning of the text so the scrollbar is
		// positioned properly
		editorPane.setCaretPosition(0);

		// now set the border
		bestResultsTitledBorder = BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				FOR_BEST_RESULTS_PANEL_TITLE + " with " + ruleDisplayName,
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		bestResultsPanel.setBorder(bestResultsTitledBorder);
	}

	/** ********************************************************************* */
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
		if(oSource != null && submitButton != null)
		{
			submitButton.setForeground(Color.RED);
		}

		if(oSource.equals(latticeChooser))
		{
			// update the initial state width/height spinners to reflect
			// the new lattice.
			outerPanel.getInitialStatesPanel().resetRectangleSpinners();
			outerPanel.getInitialStatesPanel().resetEllipseSpinners();
		}
	}

	/**
	 * Reacts to a change in any registered component.
	 */
	public void insertUpdate(DocumentEvent e)
	{
		// do one of the components have focus?
		boolean hasFocus = (hasSpinnerChanged(numColsSpinner)
				|| hasSpinnerChanged(numRowsSpinner)
				|| numStates.isFocusOwner() || radius.isFocusOwner()
				|| standardDeviation.isFocusOwner() || runningAverage
				.isFocusOwner());

		Object oSource = e.getDocument();
		if(oSource != null && submitButton != null && hasFocus)
		{
			submitButton.setForeground(Color.RED);
		}

		if(numStates.isFocusOwner())
		{
			// update the init states panel that shows each state
			outerPanel.getInitialStatesPanel().resetActiveRadioButton();
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
	 * Reacts to a change in spinners caused by pressing the up or down arrows.
	 */
	public void stateChanged(ChangeEvent e)
	{
		Object oSource = e.getSource();

		if(oSource != null && submitButton != null)
		{
			submitButton.setForeground(Color.RED);
		}

		if(oSource.equals(numRowsSpinner) || oSource.equals(numColsSpinner))
		{
			// update the initial state width/height spinners to reflect
			// the new lattice size.
			outerPanel.getInitialStatesPanel().resetRectangleSpinners();
			outerPanel.getInitialStatesPanel().resetEllipseSpinners();
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
			String lastValidValue = spinner.getValue().toString();

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
