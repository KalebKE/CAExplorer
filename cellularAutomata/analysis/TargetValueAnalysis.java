/*
 TargetValueAnalysis -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.analysis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import cellularAutomata.CAController;
import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.model.RealValuedState;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.OneDimensionalLattice;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.CAFileChooser;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.ImagePreviewer;
import cellularAutomata.util.MultilineLabel;
import cellularAutomata.util.PreviewPanel;
import cellularAutomata.util.files.AllImageFilter;
import cellularAutomata.util.files.AllImageTypeReader;
import cellularAutomata.util.files.CAImageIconLoader;
import cellularAutomata.util.math.Complex;
import cellularAutomata.util.math.RandomSingleton;

/**
 * Compares the cell's current value to a target state. If the cell's old value
 * is closer to the target state than the cell's current value (by some metric),
 * then this analysis changes the current value of the cell to the old value.
 * Effectively, this analysis only allows a rule to update a cell's state if the
 * update is "closer" to the target value.
 * <p>
 * For more information about target values, see article by Joy Hughes in the
 * book "New Constructions in Cellular Automata", 2003 (edited by Griffeath and
 * Moore).
 * 
 * @author David Bahr
 */
public class TargetValueAnalysis extends Analysis implements ActionListener
{
	// a choice of the metric distance function.
	private static final byte DIFFERENCE_METRIC = 1;

	// a choice of the metric distance function.
	private static final byte DIFFERENCE_METRIC_WITH_WRAPAROUND = 2;

	// a choice of the metric distance function.
	private static final byte NO_METRIC = 0;

	// a choice of the metric distance function.
	private static final byte RANDOM_METRIC = 3;

	// a choice of the target image.
	private static final byte NO_IMAGE = 0;

	// a choice of the target image.
	private static final byte BLANK_IMAGE = 1;

	// a choice of the target image.
	private static final byte FILE_IMAGE = 2;

	// a choice of the target image.
	private static final byte ISIN_TRANSFORMED_IMAGE = 7;

	// a choice of the target image.
	private static final byte RADIAL_IMAGE = 4;

	// a choice of the target image.
	private static final byte REFLECTION_IMAGE = 5;

	// a choice of the target image.
	private static final byte SIN_TRANSFORMED_IMAGE = 6;

	// a brief display name for this class
	private static final String ANALYSIS_NAME = "Target Image";

	// a title for the whole analysis panel
	private static final String ATTENTION_PANEL_TITLE = "Target an Image";

	// unicode for a round bullet
	private static final String BULLET_UNICODE = "\u2022";

	// a title for the radio button box that lets them choose the metric
	private static final String METRIC_PANEL_TITLE = "Choose a metric";

	// label for a radio button
	private static final String NO_METRIC_LABEL = "no metric (all distances 0)";

	// label for a radio button
	private static final String DIFFERENCE_METRIC_LABEL = "difference, |x-y|     "
			+ "(see tooltip)";

	// tooltip for a radio button
	private static final String DIFFERENCE_METRIC_TOOLTIP = "For example, if there "
			+ "are 4 states (given by 0, 1, 2, and 3), then 0 and 4 are a distance of 4.";

	// label for a radio button
	private static final String DIFFERENCE_WRAPAROUND_METRIC_LABEL = "difference,"
			+ " |x-y| with wrap-around    (see tooltip)";

	// tooltip for a radio button
	private static final String DIFFERENCE_WRAPAROUND_METRIC_TOOLTIP = "For example, if there "
			+ "are 4 states (given by 0, 1, 2, and 3), then 0 and 4 \"wrap around\" and "
			+ "are a distance of 1.";

	// path to an image displayed in the description
	private static final String FRACTAL_IMAGE_PATH = "../images/misc/spiralFractal.png";

	// label for a radio button
	private static final String RANDOM_METRIC_LABEL = "random metric   ";

	// tooltip for random metric radio button
	private static final String RANDOM_METRIC_TOOLTIP = "Only available "
			+ "with 2 or more states (otherwise there is only one possible metric).";

	// label for a push button that resets the random metric
	private static final String RESET_METRIC_LABEL = "randomize";

	// label for a push button that resets the random metric
	private static final String RESET_METRIC_LABEL_TOOLTIP = "resets to another "
			+ "random metric";

	// display info for this class
	private static final String INFO_MESSAGE = "Actively alters the cellular "
			+ "automata rule by only allowing a cell to update its value when it brings "
			+ "it \"closer\" to a target value. \n\n"
			+ "The target values are specified as a picture which can be read from a "
			+ "selectedFile.  Or the target values can be a transformed version of the "
			+ "CA image itself: for example, the image reflected about a "
			+ "line, reflected radially through the origin, or transformed "
			+ "using a complex-valued function like (1 + i) * sin(z) where z = row + column i.\n\n"
			+ "The concept of \"closer\" is specified by a selected distance metric, "
			+ "such as |x - y| where x is the cell's value and y is the target image "
			+ "value.  The cell's current value and proposed new value are both compared "
			+ "to the target using the metric.  Whichever is smaller becomes the cell's "
			+ "new value. \n\n"
			+ BULLET_UNICODE
			+ " For best results, try rules like Growing Seed, Majority Probably Wins, Majority "
			+ "Wins, and Diffusion with random initial states and large lattices. "
			+ "Use any number of states. \n\n"
			+ BULLET_UNICODE
			+ " Try loading an image as the initial state, and then specify "
			+ "a different image as the target.  One image will morph into the other. \n\n"
			+ BULLET_UNICODE
			+ " Try matching an image with the |x-y| metric, and then change the "
			+ "metric to a random new metric (works for integer valued rules)."
			+ "The colors will blend and morph into new positions. \n\n"
			+ BULLET_UNICODE
			+ " Try the spiral fractal with the Obesity rule and two states.  Change the "
			+ "temperature to 1.0 (in More Properties) and while the CA is evolving, very "
			+ "slowly decrease the temperature to 0.0.  With care a fractal image will "
			+ "result.  For example, ";

	// text for the button that lets the user select the target image.
	private static final String SELECT_IMAGE = "Select image";

	// tooltip for the button that lets the target image be selected
	private static final String SELECT_IMAGE_TOOLTIP = "Select an image which the "
			+ "cells will try to match (i.e., target).";

	// title for the subpanel that lets the user select the target image
	private static final String TARGET_IMAGE_PANEL_TITLE = "Choose target image";

	// a brief tooltip description for this class
	private static final String TOOLTIP = "<html><body>only update cells if they "
			+ "are closer to a target image</body></html>";

	// label for a radio button
	private static final String ISIN_TRANSFORMED_SELF_IMAGE = "square fractal (see tooltip)";

	// tooltip for a radio button
	private static final String ISIN_TRANSFORMED_SELF_IMAGE_TOOLTIP = "<html><body>Treats each "
			+ "cell as a complex coordinate and makes the target value <br>"
			+ "the cell at i * sin(z) where z = row + column i.  Some rules like <br>"
			+ "Majority Probably Wins will create square fractals.</body></html>";

	// label for a radio button
	private static final String SIN_TRANSFORMED_SELF_IMAGE = "spiral fractal (see tooltip)";

	// tooltip for a radio button
	private static final String SIN_TRANSFORMED_SELF_IMAGE_TOOLTIP = "<html><body>Treats each "
			+ "cell as a complex coordinate and makes the target value <br>"
			+ "the cell at (1 + i) * sin(z) where z = row + column i.  Some rules will <br>"
			+ "create spiral fractals.</body></html>";

	// display info/warning for this class when not an integer based rule
	private static final String WARNING_MESSAGE = "Warning, this analysis only works "
			+ "with integer and real number based rules.  The current rule is neither integer "
			+ "nor real number based.";

	// set to false after the analyze method has been run once
	private boolean firstTimeThrough = true;

	// true if this is an integer-based rule
	private boolean isIntegerCompatibleRule = true;

	// true if this is a real-number based rule
	private boolean isRealCompatibleRule = false;

	// when true, analyze() will recalculate the transformed lattice
	private boolean recalculateTransformedLattice = true;

	// suppresses the image from being loaded from a selectedFile. Only true
	// when the
	// analysis has been reset. See the reset() method.
	private boolean supressImageLoading = false;

	// the choice of the metric distance function. The default is
	// DIFFERENCE_METRIC.
	private byte metricFunction = DIFFERENCE_METRIC_WITH_WRAPAROUND;

	// the choice of the target image. The default is
	// REFLECTION_IMAGE.
	private byte selectedImage = REFLECTION_IMAGE;

	// the cell's transformed to their new positions by the functions for
	// the "spiral fractal", reflection, and radial reflection images.
	private Cell[][] transformedLattice = null;

	// the target image as an array of CellStates. If the image is
	// two-dimensional, then image[col][row] is stored in the array
	// as array[row * width + col].
	private CellState[] targetImage = null;

	// color of titles of sections
	private Color titleColor = Color.BLUE;

	// the maximum state value for real-valued rules
	private double maxState = 0.0;

	// the minimum state value for real-valued rules
	private double minState = 0.0;

	// a value used for "wrapping around" in the wrap-around metric
	private double wrapValue = 0.0;

	// the selectedFile selected by the user
	private File selectedFile = null;

	// fonts for display
	private Fonts fonts = new Fonts();

	// title font (for titles of sections)
	private Font titleFont = new Fonts().getItalicSmallerFont();

	// will equal JFileChooser.APPROVE_OPTION when the user has
	// selected a file for loading
	private int approval = JFileChooser.CANCEL_OPTION;

	// the height of the current simulation
	private int height = 3;

	// the number of states in the current simulation
	private int numStates = 2;

	// the width of the current simulation
	private int width = 3;

	// each element maps to one of the state values -- the array can then be
	// used to map to other state values and |randomMetric[i] - randomMetric[j]|
	// becomes a random metric between the states i and j.
	private int[] randomMetric = null;

	// the button for resetting the random metric
	private JButton resetRandomMetricButton = null;

	// the button for selecting the state to be analyzed
	private JButton selectImageButton = null;

	// the panel where results are displayed
	private JPanel displayPanel = null;

	// radio button for choosing a target image
	private JRadioButton blankImageButton = null;

	// radio button for choosing a target image
	private JRadioButton iSinTransformedImageButton = null;

	// radio button for choosing a target image
	private JRadioButton radialImageButton = null;

	// radio button for choosing a target image
	private JRadioButton reflectionImageButton = null;

	// radio button for choosing a target image
	private JRadioButton sinTransformedImageButton = null;

	// radio button for choosing no target image
	private JRadioButton noImageButton = null;

	// radio button for choosing a target image
	private JRadioButton fileImageButton = null;

	// radio button for choosing a metric
	private JRadioButton differenceMetricButton = null;

	// radio button for choosing a metric
	private JRadioButton differenceWrapAroundMetricButton = null;

	// radio button for choosing a metric
	private JRadioButton noMetricButton = null;

	// radio button for choosing a metric
	private JRadioButton randomMetricButton = null;

	// the current rule being analyzed
	private Rule rule = null;

	/**
	 * Create a metric and target image which the CA will attempt to match.
	 * <p>
	 * When building child classes, the minimalOrLazyInitialization parameter
	 * must be included but may be ignored. However, the boolean is intended to
	 * indicate when the child's constructor should build an analysis with as
	 * small a footprint as possible. In order to load analyses by reflection,
	 * the application must query the child classes for information like their
	 * display names, tooltip descriptions, etc. At these times it makes no
	 * sense to build the complete analysis which may have a large footprint in
	 * memory.
	 * <p>
	 * It is recommended that the child's constructor and instance variables do
	 * not initialize any variables and that variables be initialized only when
	 * first needed (lazy initialization). Or all initializations in the
	 * constructor may be placed in an <code>if</code> statement (as
	 * illustrated in the parent constructor and in most other analyses designed
	 * by David Bahr).
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
	 *            small a footprint as possible. When false, the analysis is
	 *            fully constructed, complete with close buttons, display
	 *            panels, etc. If uncertain, set this variable to false.
	 */
	public TargetValueAnalysis(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			setUpAnalysis();
		}
	}

	/**
	 * Makes the target image empty/blank (all 0's).
	 */
	private void createBlankImage()
	{
		if(IntegerCellState.isCompatibleRule(rule))
		{
			int[] target = new int[width * height];
			Arrays.fill(target, 0);

			// copy the int array into a CellState array
			targetImage = new CellState[target.length];
			for(int i = 0; i < target.length; i++)
			{
				IntegerCellState state = ((IntegerCellState) rule
						.getCompatibleCellState());
				state.setState(target[i]);
				targetImage[i] = state;
			}
		}
		else if(RealValuedState.isCompatibleRule(rule))
		{
			double[] target = new double[width * height];
			Arrays.fill(target, 0.0);

			// copy the int array into a CellState array
			targetImage = new CellState[target.length];
			for(int i = 0; i < target.length; i++)
			{
				RealValuedState state = ((RealValuedState) rule
						.getCompatibleCellState());
				state.setState(target[i]);
				targetImage[i] = state;
			}
		}
	}

	/**
	 * Create the panel used to display the radio button choices.
	 */
	private void createDisplayPanel()
	{
		int displayWidth = CAFrame.tabbedPaneDimension.width;
		int displayHeight = 1315;

		// create the display panel
		if(displayPanel == null)
		{
			displayPanel = new JPanel(new GridBagLayout());
			displayPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			displayPanel.setPreferredSize(new Dimension(displayWidth,
					displayHeight));
		}
		else
		{
			displayPanel.removeAll();
		}

		if(isIntegerCompatibleRule || isRealCompatibleRule)
		{
			// create a panel that displays messages
			JPanel messagePanel = createMessagePanel();

			// options for selecting pinned states (random or by position)
			JPanel targetImagePanel = createTargetImageRadioButtonPanel();

			// create a panel that holds the select state radio buttons
			JPanel metricPanel = createMetricRadioButtonPanel();

			// add all the components to the panel
			int row = 0;
			displayPanel.add(messagePanel, new GBC(1, row).setSpan(4, 1)
					.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
					.setInsets(1));

			row++;
			displayPanel.add(targetImagePanel, new GBC(1, row).setSpan(4, 1)
					.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
					.setInsets(1));

			row++;
			displayPanel.add(metricPanel, new GBC(1, row).setSpan(4, 1)
					.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
					.setInsets(1));

		}
		else
		{
			int row = 0;
			displayPanel.add(createWarningMessagePanel(), new GBC(1, row)
					.setSpan(4, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0)
					.setAnchor(GBC.WEST).setInsets(1));
		}
	}

	/**
	 * Makes the target image the picture held by the file instance variable.
	 */
	private void createFileImage()
	{
		if((selectedFile != null) && (approval == JFileChooser.APPROVE_OPTION))
		{
			// reset
			approval = JFileChooser.CANCEL_OPTION;

			// Now read the selectedFile into an array
			if(IntegerCellState.isCompatibleRule(rule))
			{
				int[] target = null;
				try
				{
					target = AllImageTypeReader.readToIntegerArray(selectedFile
							.getPath(), width, height, numStates);
				}
				catch(Throwable e)
				{
					// warn the user of the error and then create an empty image
					String message = "There has been an error loading the image: "
							+ e.getMessage()
							+ "\nAn empty image will be used instead.";

					JOptionPane.showMessageDialog(CAController.getCAFrame()
							.getFrame(), message, "Import image error",
							JOptionPane.ERROR_MESSAGE);

					// create an empty image
					target = new int[width * height];
					Arrays.fill(target, 0);
				}

				// copy the int array into a CellState array
				targetImage = new CellState[target.length];
				for(int i = 0; i < target.length; i++)
				{
					IntegerCellState targetState = ((IntegerCellState) rule
							.getCompatibleCellState());
					targetState.setState(target[i]);
					targetImage[i] = targetState;
				}
			}
			else if(RealValuedState.isCompatibleRule(rule))
			{
				double[] target = null;
				try
				{
					RealValuedState sampleState = ((RealValuedState) rule
							.getCompatibleCellState());
					double min = sampleState.getEmptyState();
					double max = sampleState.getFullState();

					target = AllImageTypeReader.readToDoubleArray(selectedFile
							.getPath(), width, height, min, max);
				}
				catch(Exception e)
				{
					// warn the user of the error and then create an empty image
					String message = "There has been an error loading the image: "
							+ e.getMessage()
							+ "\nAn empty image will be used instead.";

					JOptionPane.showMessageDialog(CAController.getCAFrame()
							.getFrame(), message, "Import image error",
							JOptionPane.ERROR_MESSAGE);

					// create an empty image
					target = new double[width * height];
					Arrays.fill(target, 0.0);
				}

				// copy the int array into a CellState array
				targetImage = new CellState[target.length];
				for(int i = 0; i < target.length; i++)
				{
					RealValuedState targetState = ((RealValuedState) rule
							.getCompatibleCellState());
					targetState.setState(target[i]);
					targetImage[i] = targetState;
				}
			}
		}
	}

	/**
	 * Creates a panel that displays messages.
	 * 
	 * @return A panel containing messages.
	 */
	private JPanel createMessagePanel()
	{
		// a "grab their attention" panel
		AttentionPanel attentionPanel = new AttentionPanel(
				ATTENTION_PANEL_TITLE);

		MultilineLabel messageLabel = new MultilineLabel(INFO_MESSAGE);
		messageLabel.setFont(fonts.getAnalysesDescriptionFont());
		messageLabel.setMargin(new Insets(6, 10, 2, 16));

		ImageIcon fractalImage = CAImageIconLoader
				.loadImage(FRACTAL_IMAGE_PATH);
		JLabel label = new JLabel(fractalImage, JLabel.CENTER);

		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		messagePanel.add(attentionPanel, BorderLayout.NORTH);
		messagePanel.add(messageLabel, BorderLayout.CENTER);
		messagePanel.add(label, BorderLayout.SOUTH);

		return messagePanel;
	}

	/**
	 * Creates radio buttons to choose the metric.
	 */
	private JPanel createMetricRadioButtonPanel()
	{
		noMetricButton = new JRadioButton(NO_METRIC_LABEL);
		noMetricButton.setFont(fonts.getPlainFont());
		noMetricButton.addItemListener(new MetricChoiceListener());
		noMetricButton.setSelected(false);

		differenceMetricButton = new JRadioButton(DIFFERENCE_METRIC_LABEL);
		differenceMetricButton.setToolTipText(DIFFERENCE_METRIC_TOOLTIP);
		differenceMetricButton.setFont(fonts.getPlainFont());
		differenceMetricButton.addItemListener(new MetricChoiceListener());
		differenceMetricButton.setSelected(false);

		differenceWrapAroundMetricButton = new JRadioButton(
				DIFFERENCE_WRAPAROUND_METRIC_LABEL);
		differenceWrapAroundMetricButton
				.setToolTipText(DIFFERENCE_WRAPAROUND_METRIC_TOOLTIP);
		differenceWrapAroundMetricButton.setFont(fonts.getPlainFont());
		differenceWrapAroundMetricButton
				.addItemListener(new MetricChoiceListener());
		differenceWrapAroundMetricButton.setSelected(false);

		randomMetricButton = new JRadioButton(RANDOM_METRIC_LABEL);
		randomMetricButton.setFont(fonts.getPlainFont());
		randomMetricButton.addItemListener(new MetricChoiceListener());
		randomMetricButton.setSelected(false);
		randomMetricButton.setToolTipText(RANDOM_METRIC_TOOLTIP);

		// a button to reset the random metric
		resetRandomMetricButton = new JButton(RESET_METRIC_LABEL);
		resetRandomMetricButton.setActionCommand(RESET_METRIC_LABEL);
		resetRandomMetricButton.setToolTipText(RESET_METRIC_LABEL_TOOLTIP);
		resetRandomMetricButton.setFont(fonts.getBoldSmallerFont());
		resetRandomMetricButton.addActionListener(this);
		resetRandomMetricButton.setEnabled(false);
		resetRandomMetricButton.setToolTipText(RANDOM_METRIC_TOOLTIP);

		// put them in a group so that they behave as radio buttons
		ButtonGroup group = new ButtonGroup();
		group.add(noMetricButton);
		group.add(differenceMetricButton);
		group.add(differenceWrapAroundMetricButton);
		group.add(randomMetricButton);

		// now put all the graphics together
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		int row = 0;
		buttonPanel.add(noMetricButton, new GBC(0, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		buttonPanel.add(differenceMetricButton, new GBC(0, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		buttonPanel.add(differenceWrapAroundMetricButton, new GBC(0, row)
				.setSpan(4, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(
						GBC.WEST).setInsets(1));

		row++;
		buttonPanel.add(randomMetricButton, new GBC(0, row).setSpan(2, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		buttonPanel.add(resetRandomMetricButton, new GBC(2, row).setSpan(2, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// create a JPanel for the radio buttons
		JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 5);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), METRIC_PANEL_TITLE, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		Border compoundBorder = BorderFactory.createCompoundBorder(
				titledBorder, emptyBorder);
		radioPanel.setBorder(compoundBorder);
		radioPanel.add(buttonPanel);

		return radioPanel;
	}

	/**
	 * Makes the target image a radial reflection function transform of the
	 * current cell positions.
	 */
	private void createRadialLattice(Lattice lattice)
	{
		Iterator cellIterator = lattice.iterator();

		Cell[][] target = new Cell[height][width];
		for(int i = 0; i < height; i++)
		{
			for(int j = 0; j < width; j++)
			{
				target[i][j] = (Cell) cellIterator.next();
			}
		}

		transformedLattice = new Cell[height][width];
		for(int i = 0; i < height; i++)
		{
			for(int j = 0; j < width; j++)
			{
				// each cell is tied to the cell reflected about the y-axis
				transformedLattice[i][j] = target[height - 1 - i][width - 1 - j];
			}
		}
	}

	/**
	 * Creates a random metric by assigning each integer state value an element
	 * in an array. The array values store another state value in a bijective
	 * mapping. Therefore |randomArray[i] - randomArray[j]| becomes a random
	 * metric between integer states.
	 */
	private void createRandomMetric()
	{
		// create a list of the state values
		ArrayList<Integer> stateList = new ArrayList<Integer>(numStates);
		for(int i = 0; i < numStates; i++)
		{
			stateList.add(new Integer(i));
		}

		// remove the state values one at a time in a random order
		Random random = RandomSingleton.getInstance();
		randomMetric = new int[numStates];
		for(int i = 0; i < numStates; i++)
		{
			int randomPosition = random.nextInt(stateList.size());
			Integer state = stateList.remove(randomPosition);

			// this creates the random metric
			randomMetric[i] = state.intValue();
		}
	}

	/**
	 * Makes the target image a reflection transform of the current cell
	 * positions.
	 */
	private void createReflectionLattice(Lattice lattice)
	{
		Iterator cellIterator = lattice.iterator();
		Cell[][] target = new Cell[height][width];
		for(int i = 0; i < height; i++)
		{
			for(int j = 0; j < width; j++)
			{
				target[i][j] = (Cell) cellIterator.next();
			}
		}

		transformedLattice = new Cell[height][width];
		for(int i = 0; i < height; i++)
		{
			for(int j = 0; j < width; j++)
			{
				// each cell is tied to the cell reflected about the y-axis
				transformedLattice[i][j] = target[i][width - 1 - j];
			}
		}
	}

	/**
	 * Given the transformed lattice, makes the target image from the current CA
	 * states.
	 */
	private void createTargetImage()
	{
		// copy the values of the cells in the transformedLattice array into a
		// CellState array
		targetImage = new CellState[width * height];
		for(int row = 0; row < height; row++)
		{
			for(int col = 0; col < width; col++)
			{
				targetImage[width * row + col] = transformedLattice[row][col]
						.getState().clone();
			}
		}
	}

	/**
	 * Creates radio buttons to choose the target image.
	 */
	private JPanel createTargetImageRadioButtonPanel()
	{
		noImageButton = new JRadioButton("no image");
		noImageButton.setFont(fonts.getPlainFont());
		noImageButton.addItemListener(new TargetImageChoiceListener());
		noImageButton.setSelected(false);

		blankImageButton = new JRadioButton("blank image");
		blankImageButton.setFont(fonts.getPlainFont());
		blankImageButton.addItemListener(new TargetImageChoiceListener());
		blankImageButton.setSelected(false);

		reflectionImageButton = new JRadioButton("vertical reflection of self");
		reflectionImageButton.setFont(fonts.getPlainFont());
		reflectionImageButton.addItemListener(new TargetImageChoiceListener());
		reflectionImageButton.setSelected(false);

		radialImageButton = new JRadioButton("radial reflection of self");
		radialImageButton.setFont(fonts.getPlainFont());
		radialImageButton.addItemListener(new TargetImageChoiceListener());
		radialImageButton.setSelected(false);

		iSinTransformedImageButton = new JRadioButton(
				ISIN_TRANSFORMED_SELF_IMAGE);
		iSinTransformedImageButton
				.setToolTipText(ISIN_TRANSFORMED_SELF_IMAGE_TOOLTIP);
		iSinTransformedImageButton.setFont(fonts.getPlainFont());
		iSinTransformedImageButton
				.addItemListener(new TargetImageChoiceListener());
		iSinTransformedImageButton.setSelected(false);

		sinTransformedImageButton = new JRadioButton(SIN_TRANSFORMED_SELF_IMAGE);
		sinTransformedImageButton
				.setToolTipText(SIN_TRANSFORMED_SELF_IMAGE_TOOLTIP);
		sinTransformedImageButton.setFont(fonts.getPlainFont());
		sinTransformedImageButton
				.addItemListener(new TargetImageChoiceListener());
		sinTransformedImageButton.setSelected(false);

		fileImageButton = new JRadioButton("select image from file");
		fileImageButton.setFont(fonts.getPlainFont());
		fileImageButton.addItemListener(new TargetImageChoiceListener());
		fileImageButton.setSelected(false);

		// put them in a group so that they behave as radio buttons
		ButtonGroup group = new ButtonGroup();
		group.add(noImageButton);
		group.add(blankImageButton);
		group.add(reflectionImageButton);
		group.add(radialImageButton);
		group.add(iSinTransformedImageButton);
		group.add(sinTransformedImageButton);
		group.add(fileImageButton);

		// create a "select target image" button
		selectImageButton = new JButton(SELECT_IMAGE);
		selectImageButton.setActionCommand(SELECT_IMAGE);
		selectImageButton.setToolTipText(SELECT_IMAGE_TOOLTIP);
		selectImageButton.setFont(fonts.getBoldSmallerFont());
		selectImageButton.addActionListener(this);
		selectImageButton.setEnabled(false);

		// now put all the graphics together
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		int row = 0;
		buttonPanel.add(noImageButton, new GBC(0, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		buttonPanel.add(blankImageButton, new GBC(0, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// row++;
		// buttonPanel.add(previousImageButton, new GBC(0, row).setSpan(4, 1)
		// .setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
		// .setInsets(1));

		row++;
		buttonPanel.add(reflectionImageButton, new GBC(0, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		buttonPanel.add(radialImageButton, new GBC(0, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		buttonPanel.add(iSinTransformedImageButton, new GBC(0, row).setSpan(4,
				1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		buttonPanel.add(sinTransformedImageButton, new GBC(0, row)
				.setSpan(4, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(
						GBC.WEST).setInsets(1));

		row++;
		buttonPanel.add(fileImageButton, new GBC(0, row).setSpan(2, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		buttonPanel.add(selectImageButton, new GBC(2, row).setSpan(2, 1)
				.setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// create a JPanel for the radio buttons and their containing box
		JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 5);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), TARGET_IMAGE_PANEL_TITLE,
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		Border compoundBorder = BorderFactory.createCompoundBorder(
				titledBorder, emptyBorder);
		radioPanel.setBorder(compoundBorder);
		radioPanel.add(buttonPanel);

		return radioPanel;
	}

	/**
	 * Makes the target image a complex function transform (1 + i)*sin(z) of the
	 * current cell positions.
	 */
	private void createSinTransformedLattice(Lattice lattice)
	{
		Iterator cellIterator = lattice.iterator();

		Cell[][] target = new Cell[height][width];
		for(int i = 0; i < height; i++)
		{
			for(int j = 0; j < width; j++)
			{
				target[i][j] = (Cell) cellIterator.next();
			}
		}

		transformedLattice = new Cell[height][width];
		for(int row = 0; row < height; row++)
		{
			for(int col = 0; col < width; col++)
			{
				// values > 1.0 zoom out. values < 1.0 zoom in
				double zoom = 1.0;

				// convert grid coordinates to complex number coordinates that
				// vary from +-1 +- i, multiplied by the zoom factor
				double real = zoom
						* (2.0 * (double) col / (double) (width - 1)) - 1.0;
				double img = zoom
						* (1.0 - 2.0 * ((double) row / (double) (height - 1)));
				Complex coordinate = new Complex(real, img);

				// apply the complex function (transformation)

				// coordinate =
				// Complex.add(Complex.multiply(coordinate,
				// coordinate), new Complex(-0.9, -0.4));

				// coordinate = Complex.add(Complex.multiply(coordinate,
				// coordinate), new Complex(coordinate));

				// Complex.add(Complex.multiply(Complex.multiply(coordinate,
				// coordinate), coordinate), new Complex(coordinate));

				// EXCELLENT!
				coordinate = Complex.multiply(new Complex(1, 1), Complex
						.sin(coordinate));

				// SUPER EXCELLENT!
				// coordinate = Complex.multiply(new Complex(0, 1), Complex
				// .sin(coordinate));

				// rescale the result to the width and height
				double transformedReal = coordinate.real;
				double transformedImaginary = coordinate.imaginary;
				int newCol = (int) Math
						.round(((((transformedReal / zoom) + 1.0) / 2.0) * (width - 1)));
				int newRow = (int) Math
						.round((((1.0 - (transformedImaginary / zoom)) / 2.0) * (height - 1)));

				// make sure not out of bounds
				if((newCol > width - 1) || (newRow > height - 1)
						|| (newCol < 0) || (newRow < 0))
				{
					newCol = col;
					newRow = row;
				}

				transformedLattice[row][col] = target[newRow][newCol];
			}
		}
	}

	/**
	 * Makes the target image a complex function transform i*sin(z) of the
	 * current cell positions.
	 */
	private void createISinTransformedLattice(Lattice lattice)
	{
		Iterator cellIterator = lattice.iterator();

		Cell[][] target = new Cell[height][width];
		for(int i = 0; i < height; i++)
		{
			for(int j = 0; j < width; j++)
			{
				target[i][j] = (Cell) cellIterator.next();
			}
		}

		transformedLattice = new Cell[height][width];
		for(int row = 0; row < height; row++)
		{
			for(int col = 0; col < width; col++)
			{
				// values > 1.0 zoom out. values < 1.0 zoom in
				double zoom = 1.0;

				// convert grid coordinates to complex number coordinates that
				// vary from +-1 +- i, multiplied by the zoom factor
				double real = zoom
						* (2.0 * (double) col / (double) (width - 1)) - 1.0;
				double img = zoom
						* (1.0 - 2.0 * ((double) row / (double) (height - 1)));
				Complex coordinate = new Complex(real, img);

				// apply the complex function (transformation)

				// coordinate =
				// Complex.add(Complex.multiply(coordinate,
				// coordinate), new Complex(-0.9, -0.4));

				// coordinate = Complex.add(Complex.multiply(coordinate,
				// coordinate), new Complex(coordinate));

				// Complex.add(Complex.multiply(Complex.multiply(coordinate,
				// coordinate), coordinate), new Complex(coordinate));

				// EXCELLENT!
				// coordinate = Complex.multiply(new Complex(1, 1), Complex
				// .sin(coordinate));

				// SUPER EXCELLENT!
				coordinate = Complex.multiply(new Complex(0, 1), Complex
						.sin(coordinate));

				// rescale the result to the width and height
				double transformedReal = coordinate.real;
				double transformedImaginary = coordinate.imaginary;
				int newCol = (int) Math
						.round(((((transformedReal / zoom) + 1.0) / 2.0) * (width - 1)));
				int newRow = (int) Math
						.round((((1.0 - (transformedImaginary / zoom)) / 2.0) * (height - 1)));

				// make sure not out of bounds
				if((newCol > width - 1) || (newRow > height - 1)
						|| (newCol < 0) || (newRow < 0))
				{
					newCol = col;
					newRow = row;
				}

				transformedLattice[row][col] = target[newRow][newCol];
			}
		}
	}

	/**
	 * Creates a warning message.
	 * 
	 * @return A panel containing the warning message.
	 */
	private JPanel createWarningMessagePanel()
	{
		// a "grab their attention" panel
		AttentionPanel attentionPanel = new AttentionPanel(
				ATTENTION_PANEL_TITLE);

		MultilineLabel messageLabel = new MultilineLabel(WARNING_MESSAGE);

		messageLabel.setForeground(Color.RED);
		messageLabel.setFont(fonts.getBoldFont());
		messageLabel.setMargin(new Insets(6, 10, 2, 16));
		messageLabel.setColumns(40);

		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		messagePanel.add(attentionPanel, BorderLayout.NORTH);
		messagePanel.add(messageLabel, BorderLayout.CENTER);

		return messagePanel;
	}

	/**
	 * Measures the distance between a specified state and the target state
	 * using some metric.
	 * <p>
	 * Some rules will be modified by a target value. If the cell's new state
	 * value is further away (by some metric) than the cell's original state
	 * value, then the cell's state is not updated.
	 * <p>
	 * For more information about target values, see article by Joy Hughes in
	 * the book "New Constructions in Cellular Automata", 2003 (edited by
	 * Griffeath and Moore).
	 * 
	 * @param state
	 *            The state being compared to the target state (by some metric).
	 * @param targetState
	 *            The target state to which the other state is compared (by some
	 *            metric).
	 * @return The distance to the cell's target value (by some metric).
	 */
	private double getDistance(CellState state, CellState targetState)
	{
		double value = 0.0;
		double targetValue = 0.0;

		if(IntegerCellState.isCompatibleRule(rule))
		{
			value = ((IntegerCellState) state).getState();
			targetValue = ((IntegerCellState) targetState).getState();
		}
		else if(RealValuedState.isCompatibleRule(rule))
		{
			value = ((RealValuedState) state).getState();
			targetValue = ((RealValuedState) targetState).getState();
		}

		double distance = 0.0;
		switch (metricFunction)
		{
		case NO_METRIC:
			distance = 0.0;
			break;
		case DIFFERENCE_METRIC:
			distance = Math.abs(value - targetValue);
			break;
		case DIFFERENCE_METRIC_WITH_WRAPAROUND:

			distance = Math.abs(value - targetValue);
			double difference2 = Math.abs((value - wrapValue) - targetValue);
			double difference3 = Math.abs(value - (targetValue - wrapValue));

			// get the smallest one
			distance = Math.min(Math.min(distance, difference2), difference3);
			break;
		case RANDOM_METRIC:
			// each integer state is a random distance from the other states
			distance = Math.abs(randomMetric[(int) value] - targetValue);
			break;
		default:
			distance = Math.abs(value - targetValue);
			break;
		}

		return distance;
	}

	/**
	 * Finds the image to load when the "select image" radio button is selected.
	 * 
	 * @param width
	 *            The width of the CA lattice.
	 * @param height
	 *            The height of the CA lattice.
	 */
	private void loadImage(int width, int height)
	{
		// pause the simulation.
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));

		// make the JFrame look disabled
		CAController.getCAFrame().setViewDisabled(true);

		// get the folder where files will be loaded
		File startDirectory = new File(CurrentProperties.getInstance()
				.getSaveDataFilePath());

		JFileChooser fileChooser = new CAFileChooser(startDirectory);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle("Load Image");

		// add a preview panel
		final ImagePreviewer imagePreviewer = new ImagePreviewer();
		PreviewPanel previewPanel = new PreviewPanel(imagePreviewer);
		fileChooser.setAccessory(previewPanel);
		fileChooser.addPropertyChangeListener(new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent e)
			{
				if(e.getPropertyName().equals(
						JFileChooser.SELECTED_FILE_CHANGED_PROPERTY))
				{
					File f = (File) e.getNewValue();
					String extension = AllImageFilter.getExtension(f);

					if(AllImageTypeReader.isPermittedImageType(extension))
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

		// open the dialog, and let it know which frame is the parent --
		// this lets it inherit the top-left icon.
		approval = fileChooser.showOpenDialog(CAController.getCAFrame()
				.getFrame());
		selectedFile = fileChooser.getSelectedFile();

		// now create the targetImage array while the simulation is still paused
		createFileImage();

		// make the JFrame look enabled
		CAController.getCAFrame().setViewDisabled(false);

		// restart the simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(false)));
	}

	/**
	 * Select the appropriate radio buttons for the metric and the target image.
	 */
	private void setSelectedRadioButtons()
	{
		if(isIntegerCompatibleRule || isRealCompatibleRule)
		{
			// select the metric
			switch (metricFunction)
			{
			case NO_METRIC:
				noMetricButton.setSelected(true);
				break;
			case DIFFERENCE_METRIC:
				differenceMetricButton.setSelected(true);
				break;
			case DIFFERENCE_METRIC_WITH_WRAPAROUND:
				differenceWrapAroundMetricButton.setSelected(true);
				break;
			case RANDOM_METRIC:
				if(IntegerCellState.isCompatibleRule(rule))
				{
					// only allowed with integer rules
					randomMetricButton.setSelected(true);
				}
				else
				{
					differenceWrapAroundMetricButton.setSelected(true);
				}
				break;
			default:
				differenceWrapAroundMetricButton.setSelected(true);
				break;
			}

			// select the image
			switch (selectedImage)
			{
			case NO_IMAGE:
				noImageButton.setSelected(true);
				break;
			case RADIAL_IMAGE:
				radialImageButton.setSelected(true);
				break;
			case REFLECTION_IMAGE:
				reflectionImageButton.setSelected(true);
				break;
			case FILE_IMAGE:
				fileImageButton.setSelected(true);
				break;
			case SIN_TRANSFORMED_IMAGE:
				sinTransformedImageButton.setSelected(true);
				break;
			case ISIN_TRANSFORMED_IMAGE:
				iSinTransformedImageButton.setSelected(true);
				break;
			case BLANK_IMAGE:
				blankImageButton.setSelected(true);
				break;
			default:
				reflectionImageButton.setSelected(true);
				break;
			}

			// the random metric doesn't do anything if there are only 2 states
			if(!IntegerCellState.isCompatibleRule(rule) || numStates == 2)
			{
				// reset the metric selection to a default
				if(randomMetricButton.isSelected())
				{
					differenceWrapAroundMetricButton.setSelected(true);
				}

				// disable the random button
				randomMetricButton.setEnabled(false);
				resetRandomMetricButton.setEnabled(false);
			}
			else
			{
				// enable the random button
				randomMetricButton.setEnabled(true);
				resetRandomMetricButton.setEnabled(true);
			}
		}
	}

	/**
	 * Called by the constructor.
	 */
	private void setUpAnalysis()
	{
		// make sure is a compatible rule
		String ruleClassName = CurrentProperties.getInstance()
				.getRuleClassName();
		rule = ReflectionTool.instantiateFullRuleFromClassName(ruleClassName);

		// decide what kind of rule this is
		if(IntegerCellState.isCompatibleRule(rule))
		{
			isIntegerCompatibleRule = true;
		}
		else
		{
			isIntegerCompatibleRule = false;
		}

		if(RealValuedState.isCompatibleRule(rule))
		{
			isRealCompatibleRule = true;
		}
		else
		{
			isRealCompatibleRule = false;
		}

		// get the CA width and height
		width = CurrentProperties.getInstance().getNumColumns();

		if(!OneDimensionalLattice.isCurrentLatticeOneDim())
		{
			height = CurrentProperties.getInstance().getNumRows();
		}
		else
		{
			height = 1;
		}

		// update, because this could change
		numStates = CurrentProperties.getInstance().getNumStates();

		if(RealValuedState.isCompatibleRule(rule))
		{
			maxState = ((RealValuedState) rule.getCompatibleCellState())
					.getFullState();
			minState = ((RealValuedState) rule.getCompatibleCellState())
					.getEmptyState();
		}

		// set a value used for the wrap-around metric Do this here (and only
		// once) because the call to isCompatibleRule is very slow
		wrapValue = numStates;
		if(RealValuedState.isCompatibleRule(rule))
		{
			wrapValue = maxState - minState;
		}

		// update because this could change with the numStates
		createRandomMetric();

		// this is the panel that will be displayed (getDisplayPanel() will
		// return the panel that this creates)
		createDisplayPanel();

		// select the appropriate image and metric radio buttons
		setSelectedRadioButtons();

		// whenever the analysis is setup, indicate that this
		// is the first time through
		firstTimeThrough = true;

		// no longer need to suppress -- the setSelectedRadioButtons() has been
		// called
		supressImageLoading = false;
	}

	/**
	 * Decides if the current cell state or the previous cell state is closer to
	 * a target value. The cell's current state is rest to the closer value. In
	 * other words, if the cell's current state value is further away (by some
	 * metric) than the cell's previous state value, then the cell's state is
	 * reset so that effectively the cell is "not updated".
	 * <p>
	 * For more information about target values, see article by Joy Hughes in
	 * the book "New Constructions in Cellular Automata", 2003 (edited by
	 * Griffeath and Moore).
	 * 
	 * @param lattice
	 *            The CA lattice.
	 * @param rule
	 *            The CA rule.
	 * @param generation
	 *            The current generation of the CA. There is no requirement that
	 *            this be the generation analyzed, but typically, this will be
	 *            the generation analyzed.
	 */
	protected void analyze(Lattice lattice, Rule rule, int generation)
	{
		// make sure it is an integer based rule
		if(IntegerCellState.isCompatibleRule(rule)
				|| RealValuedState.isCompatibleRule(rule))
		{
			if(firstTimeThrough)
			{
				// fill the target image if it is empty
				if(targetImage == null)
				{
					createBlankImage();
				}
			}

			// transform the lattice into a target lattice (using
			// reflection or a complex function, for example)
			if(transformedLattice == null || recalculateTransformedLattice)
			{
				if(sinTransformedImageButton.isSelected())
				{
					createSinTransformedLattice(lattice);
				}
				else if(iSinTransformedImageButton.isSelected())
				{
					createISinTransformedLattice(lattice);
				}
				else if(reflectionImageButton.isSelected())
				{
					createReflectionLattice(lattice);
				}
				else if(radialImageButton.isSelected())
				{
					createRadialLattice(lattice);
				}
				else if(blankImageButton.isSelected())
				{
					createBlankImage();

					// for, blank image, don't need to do this
					// calculation every time
					recalculateTransformedLattice = false;
				}
				// else if(fileImageButton.isSelected())
				// {
				// createFileImage();
				//
				// // for selectedFile image, don't need to do this
				// // calculation every time
				// recalculateTransformedLattice = false;
				// }
				// else
				// {
				// // default
				// createReflectionLattice(lattice);
				// }
			}

			// only do the following if an image has been selected
			if(!noImageButton.isSelected())
			{
				// In these cases, the target image has to be
				// recreated at every time step (because it
				// uses the CA image itself as the target)
				// Note that the lattice has been transformed
				// earlier.
				if(sinTransformedImageButton.isSelected()
						|| iSinTransformedImageButton.isSelected()
						|| reflectionImageButton.isSelected()
						|| radialImageButton.isSelected())
				{
					createTargetImage();
				}

				// compare each cell to the target
				Iterator cellIterator = lattice.iterator();
				int targetCellNumber = 0;
				while(cellIterator.hasNext())
				{
					Cell cell = (Cell) cellIterator.next();

					CellState targetState = targetImage[targetCellNumber];
					CellState previousCellState = cell.getPreviousState();
					if(previousCellState != null)
					{
						CellState currentCellState = cell.getState();
						// use the previous state if it is closer to the
						// target
						if(getDistance(currentCellState, targetState) > getDistance(
								previousCellState, targetState))
						{
							// replace the cell's current state with a clone
							// of its previous state
							cell.resetState(previousCellState.clone());
						}
					}

					// increment the cell we are getting from the target
					// image
					targetCellNumber++;
				}

				if(firstTimeThrough)
				{
					refreshGraphics();
				}

				// Tell the CA to update the view. This *must* be called at
				// every time step or the graphics will show the rule before
				// it is actively changed by this analysis. Causes an
				// annoying
				// flicker sometimes.
				// refreshGraphics();
			}

			// only true the first time the analyze method is called
			firstTimeThrough = false;
		}
	}

	/**
	 * Performs any desired operations when the analysis is stopped (closed) by
	 * the user. For example, you might write the results to a selectedFile at
	 * this time. Or you might dispose of any windows that you opened. May do
	 * nothing.
	 */
	protected void stopAnalysis()
	{
	}

	/**
	 * Reacts to buttons.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();

		if(command.equals(SELECT_IMAGE))
		{
			// use a selectedFile chooser to load an image
			loadImage(width, height);
		}
		else if(command.equals(RESET_METRIC_LABEL))
		{
			// resets the random metric
			createRandomMetric();
		}
	}

	/**
	 * A list of lattices with which this Analysis will work. In order for an
	 * analysis to display and be used, it must be compatible with both the
	 * lattice and the rule currently selected by a user (see
	 * getCompatibleRules).
	 * <p>
	 * Well-designed Analyses should work with any lattice, but some may require
	 * particular topological or geometrical information. Appropriate strings to
	 * return in the array include SquareLattice.DISPLAY_NAME,
	 * HexagonalLattice.DISPLAY_NAME,
	 * StandardOneDimensionalLattice.DISPLAY_NAME, etc. Return null if
	 * compatible with all lattices.
	 * 
	 * @return A list of lattices compatible with this Analysis (returns the
	 *         display names for the lattices). Returns null if compatible with
	 *         all lattices.
	 */
	public String[] getCompatibleLattices()
	{
		String[] lattices = null;
		return lattices;
	}

	/**
	 * A list of Rules with which this Analysis will work. In order for an
	 * analysis to display and be used, it must be compatible with both the
	 * lattice and the rule currently selected by a user (see
	 * getCompatibleLattices).
	 * <p>
	 * Well-designed Analyses should work with any rule, but some may require
	 * particular rule-specific information. Appropriate strings to return in
	 * the array include the display names for any rule: for example, "Life", or
	 * "Majority Rules". These names can be accessed from the getDisplayName()
	 * method of each rule. For example,
	 * 
	 * <pre>
	 * new Life(super.getProperties()).getDisplayName()
	 * </pre>
	 * 
	 * Return null if compatible with all rules.
	 * 
	 * @return A list of rules compatible with this Analysis (returns the
	 *         display names for the rules). Returns null if compatible with all
	 *         lattices.
	 */
	public String[] getCompatibleRules()
	{
		String[] rules = null;
		return rules;
	}

	/**
	 * A brief one or two-word string describing the analysis, appropriate for
	 * display in a drop-down list.
	 * 
	 * @return A string no longer than 15 characters.
	 */
	public String getDisplayName()
	{
		return ANALYSIS_NAME;
	}

	/**
	 * Gets a JPanel that displays results of the population analysis.
	 * 
	 * @return A display for the population analysis results.
	 */
	public JPanel getDisplayPanel()
	{
		return displayPanel;
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
	 * Performs any necessary operations to reset the analysis. This method is
	 * called if the user resets the cellular automata, or selects a new
	 * simulation.
	 */
	public void reset()
	{
		// makes sure that we don't try to reload the image
		if(selectedImage == FILE_IMAGE)
		{
			supressImageLoading = true;
		}
		else
		{
			supressImageLoading = false;
		}

		setUpAnalysis();
	}

	/**
	 * If returns true, then the analysis is forced to size its width to fit
	 * within the visible width of the tabbed pane where it is displayed. If
	 * false, then a horizontal scroll bar is added so that the analysis can be
	 * wider than the displayed space.
	 * <p>
	 * Recommend returning true. If your graphics look lousy within that space,
	 * then return false. (In other words, try both and see which is better.)
	 * 
	 * @return true if the graphics should be forced to size its width to fit
	 *         the display area.
	 */
	public boolean restrictDisplayWidthToVisibleSpace()
	{
		return true;
	}

	/**
	 * Decides what to do when the user selects a radio button for the metric.
	 * The metric gives the distance between two states.
	 * 
	 * @author David Bahr
	 */
	private class MetricChoiceListener implements ItemListener
	{
		public void itemStateChanged(ItemEvent event)
		{
			if(noMetricButton.isSelected())
			{
				metricFunction = NO_METRIC;
			}
			else if(differenceMetricButton.isSelected())
			{
				metricFunction = DIFFERENCE_METRIC;
			}
			else if(differenceWrapAroundMetricButton.isSelected())
			{
				metricFunction = DIFFERENCE_METRIC_WITH_WRAPAROUND;
			}
			else if(randomMetricButton.isSelected())
			{
				metricFunction = RANDOM_METRIC;

				// now create the random metric
				createRandomMetric();
			}
			else
			{
				// default (just in case)
				metricFunction = DIFFERENCE_METRIC_WITH_WRAPAROUND;
			}

			// decide whether or not to enable the random reset button
			if(resetRandomMetricButton != null)
			{
				if(metricFunction == RANDOM_METRIC)
				{
					// enable the reset button
					resetRandomMetricButton.setEnabled(true);
				}
				else
				{
					// disable the reset button
					resetRandomMetricButton.setEnabled(false);
				}
			}
		}
	}

	/**
	 * Decides what to do when the user selects a target image radio button.
	 * 
	 * @author David Bahr
	 */
	private class TargetImageChoiceListener implements ItemListener
	{
		public void itemStateChanged(ItemEvent event)
		{
			// set to true so that the analyze method will recalculate the
			// transformed lattice's value
			recalculateTransformedLattice = true;

			if(sinTransformedImageButton.isSelected())
			{
				selectedImage = SIN_TRANSFORMED_IMAGE;
			}
			else if(iSinTransformedImageButton.isSelected())
			{
				selectedImage = ISIN_TRANSFORMED_IMAGE;
			}
			else if(blankImageButton.isSelected())
			{
				selectedImage = BLANK_IMAGE;
			}
			else if(reflectionImageButton.isSelected())
			{
				selectedImage = REFLECTION_IMAGE;
			}
			else if(radialImageButton.isSelected())
			{
				selectedImage = RADIAL_IMAGE;
			}
			else if(noImageButton.isSelected())
			{
				selectedImage = NO_IMAGE;
			}
			else if(fileImageButton.isSelected())
			{
				selectedImage = FILE_IMAGE;

				// load the image unless it has been supressed
				// because the analysis is being reset and that
				// would be annoying
				if(!supressImageLoading)
				{
					// stops the simulation before loading the image, so this
					// won't cause a null pointer error by modifying
					// transformImage while the analyze() method is running.
					// Other selected images would cause that problem.
					loadImage(width, height);

					// for selectedFile image, don't need to do this
					// calculation every time
					recalculateTransformedLattice = false;
				}
			}
			else
			{
				// default (just in case)
				selectedImage = REFLECTION_IMAGE;
			}

			// decide whether or not to enable the image selection button
			if(selectImageButton != null && fileImageButton != null)
			{
				if(fileImageButton.isSelected())
				{
					// enable the reset button
					selectImageButton.setEnabled(true);
				}
				else
				{
					// disable the reset button
					selectImageButton.setEnabled(false);
				}
			}
		}
	}
}
