/*
 RandomAnalysis -- a class within the Cellular Automaton Explorer. 
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
import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cellularAutomata.CAController;
import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.OneDimensionalLattice;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;
import cellularAutomata.util.SimplePlot;
import cellularAutomata.util.files.FileWriter;

/**
 * Tests for randomness in the bits of a CA. Selects a single site, collects
 * enough bits to generate a 16 bit double, and then repeats. The array of
 * doubles is tested for randomness.
 * 
 * @author David Bahr
 */
public class RandomAnalysis extends Analysis implements ActionListener
{
	// the number of bins (intervals) in the distribution test
	private static final int DEFAULT_NUMBER_OF_INTERVALS = 10;

	// the default number of bits being collected to generate a number
	private static final int DEFAULT_NUMBER_OF_BITS = 8;

	// the maximum number of elements that will be plotted
	private static final int MAX_NUMBER_TO_PLOT = 200;

	// the maximum number of bytes that will be used in the compression test
	// private static final int MAX_NUMBER_OF_BYTES = 1000000;

	// a brief display name for this class
	private static final String ANALYSIS_NAME = "Randomness Test";

	// the colored title on the display
	private static final String ATTENTION_PANEL_TITLE = "Randomness Stats";

	// The tooltip for the column position where the data will be selected
	private static final String COLUMN_TIP = "<html>Choose the column where the "
			+ "data <br> will be collected.</html>";

	// Display title for the data statistics panel.
	private static final String DATA_PANEL_TITLE = "Data";

	// The pattern used to display decimals, particularly for the random number.
	private static final String DECIMAL_PATTERN = "0.000000000000";

	// display info for this class
	private static final String INFO_MESSAGE = "Tests for randomness in the CA by "
			+ "generating numbers from sequences of bits. The bits are collected from "
			+ "the highlighted cell.  Every time a new number is started, the highlight "
			+ "color is changed (red, green, red, green...). \n\n"
			+ "In particular, if a cell is occupied, then a bit value of 1 is collected.  "
			+ "If it is not occupied, then a bit value of 0 is collected.  Every generation "
			+ "another bit is collected. Once enough bits are collected, they are turned "
			+ "into a binary number (two's complement notation so that the high bit "
			+ "indicates the sign).  The binary number will be a byte, short, int, or long "
			+ "(8, 16, 32, or 64 bits respectively) as set by the user below.  For display, "
			+ "this number is also rescaled to be a decimal between 0 and 1. "
			+ "\n\n"
			+ "Two simple tests of randomness are applied to the generated numbers.  The "
			+ "first checks the percentage of 1's versus 0's in the generated bits.  This "
			+ "should remain close to 0.5 if the numbers are random.  The second looks at "
			+ "the distribution of the numbers.  This should be a flat straight line "
			+ "(uniform) if the numbers are random.  For accurate tests, long simulations "
			+ "are necessary."
			+ "\n\n"
			+ "If the CA is random, this analysis acts as a random number generator.";

	// The tooltip for the row position where the data will be selected
	private static final String ROW_TIP = "<html>Choose the row where the "
			+ "data <br> will be collected.</html>";

	// the action command for saving the data and the label used by the "save
	// data" check box
	private static final String SAVE_DATA = "   Save the data";

	// a tooltip for the save data check box
	private static final String SAVE_DATA_TOOLTIP = "<html>Saves the generated numbers "
			+ "to a file (saves <br> every generation while the box is checked).</html>";

	// Display title for the submittable properties panel.
	private static final String SUBMIT_PANEL_TITLE = "Properties";

	// a brief tooltip description for this class
	private static final String TOOLTIP = "<html>test for CA randomness</html>";

	// a tagging object used to reserve a "tagging color" for this analysis
	private static final String TAG1 = "tag1";

	// a tagging object used to reserve a "tagging color" for this analysis
	private static final String TAG2 = "tag2";

	// only true the first time the analyze method is called
	private boolean firstTimeThrough = true;

	// the number of bits collected so far. Will repeatedly cycle between 0 and
	// numBitsToCollect-1.
	private byte numBitsCollectedForGeneratedNumber = 0;

	// the cell being used to generate a number
	private Cell cellBeingAnalyzed = null;

	// color of titles of sections
	private Color titleColor = Color.BLUE;

	// column position of the cell being analyzed
	private int column = 0;

	// the number of 1 bits collected over all time
	private int num1BitsCollectedOverAllTime = 0;

	// the number of numbers that have been generated
	private int numbersCreated = 0;

	// the total number of bits collected over all time
	private int numBitsCollectedOverAllTime = 0;

	// the number of bits being collected to genetrate a number
	private int numBitsWeNeedToCollectForGeneratedNumber = DEFAULT_NUMBER_OF_BITS;

	// row position of the cell being analyzed
	private int row = 0;

	// Bins of the data in intervals between 0.0 and 1.0.
	private int[] bin = new int[DEFAULT_NUMBER_OF_INTERVALS];

	// this stores the number being collected from the CA cells. Use a long so
	// that 64 bits are available, more than or equal to the number of bits that
	// is necessary for a byte, short, int, long, float, or double.
	private long theNumber = 0;

	// If the user wants to save the data to a file, this will be instantiated
	private FileWriter fileWriter = null;

	// fonts for display
	private Fonts fonts = new Fonts();

	// title font (for titles of sections)
	private Font titleFont = new Fonts().getItalicSmallerFont();

	// The check box that lets the user save the data
	private JCheckBox saveDataCheckBox = null;

	// label for the current generation
	private JLabel generationDataLabel = null;

	// label for the generated number as an integer
	private JLabel generatedIntNumberDataLabel = null;

	// label for the generated number as a double
	private JLabel generatedDoubleNumberDataLabel = null;

	// label for the number of points created
	private JLabel numberOfPointsCreatedDataLabel = null;

	// the panel where results are displayed
	private JPanel displayPanel = null;

	// radio button for choosing a byte
	private JRadioButton byteButton = null;

	// radio button for choosing an int
	private JRadioButton intButton = null;

	// radio button for choosing a long
	private JRadioButton longButton = null;

	// radio button for choosing a short
	private JRadioButton shortButton = null;

	// selects the column position for which the number will be collected
	private JSpinner colSpinner = null;

	// selects the row position for which the number will be collected
	private JSpinner rowSpinner = null;

	// the list of bytes used for the compression test
	// private LinkedList byteList = new LinkedList();

	// the list of points that will be drawn on the random number plot
	private LinkedList<Point2D.Double> generatedNumberList = new LinkedList<Point2D.Double>();

	// the list of points that will be drawn on the "percentage of 1's" plot
	private LinkedList<Point2D.Double> percentageOnesList = new LinkedList<Point2D.Double>();

	// a panel that plots the distribution of number data
	private SimplePlot distributionPlot = null;

	// a panel that plots the generated number data
	private SimplePlot numberPlot = null;

	// a panel that plots the percentage of 1's versus 0's
	private SimplePlot percentagePlot = null;

	// a delimiter for spacing data in the data file
	private String delimiter = null;

	// the data that will be saved to a file
	private String[] data = new String[5];

	/**
	 * Create an analysis that tests for the randomness in a CA.
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
	public RandomAnalysis(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			// this is the panel that will be displayed (getDisplayPanel() will
			// return the panel that this creates)
			createFinalDisplayPanel();

			// whenever the analysis is setup, make it redraw the tagged cells
			firstTimeThrough = true;
		}
	}

	/**
	 * Compresses the data using zip technology, then returns the percentage by
	 * which it was compressed.
	 * 
	 * @param data
	 *            The data that will be compressed.
	 * @return The percent reduction in size.
	 */
	private double getCompressedDataSize(byte[] data)
	{
		int compressedSize = 34;
		try
		{
			// create an output stream to hold the compressed (zipped) data
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream(1);
			ZipOutputStream zipOutStream = new ZipOutputStream(byteOut);

			// compress as much as possible
			zipOutStream.setLevel(9);

			// create the fake "file" that will hold the compressed data
			ZipEntry entry = new ZipEntry("temp");
			try
			{
				zipOutStream.putNextEntry(entry);
			}
			catch(Exception e)
			{
				// do nothing
			}

			zipOutStream.write(data);

			// Note that there is a bug in the entry.getCompressedSize() method,
			// so can't use it (even after closing the entry). This gets the
			// number of bytes that have been output.
			compressedSize = byteOut.size();

			zipOutStream.closeEntry();
			zipOutStream.close();

		}
		catch(Exception e)
		{
			// couldn't compress
		}

		// why minus 33? Because that's the length of the header
		double percentCompressed = 1.0 - (((double) compressedSize - 33.0) / (double) data.length);

		// if(data.length > 50)
		// {
		// System.out.println("RandomAnalysis: compressedSize = "
		// + compressedSize);
		// System.out.println("RandomAnalysis: data.length = " + data.length);
		// System.out.println("RandomAnalysis: percentCompressed = "
		// + percentCompressed);
		// }

		// to be safe (in case 33 is incorrect in some instances)
		if(percentCompressed < 0.0 || percentCompressed > 1.0)
		{
			percentCompressed = 0.0;
		}

		return percentCompressed;
	}

	/**
	 * Compresses the data using zip technology, then returns the percentage by
	 * which it was compressed.
	 * 
	 * @param data
	 *            The data that will be compressed.
	 * @return The percent reduction in size.
	 */
	private double getCompressedDataSize(LinkedList data)
	{
		// convert to a byte array
		Object[] oData = data.toArray();
		byte[] byteArrayData = new byte[oData.length];
		for(int i = 0; i < byteArrayData.length; i++)
		{
			byteArrayData[i] = ((Byte) oData[i]).byteValue();
		}

		// call overloaded method
		return getCompressedDataSize(byteArrayData);
	}

	/**
	 * Create labels used to display the data for the generated numbers.
	 */
	private void createDataDisplayLabels()
	{
		// if one is null, then they all are
		if(generationDataLabel == null)
		{
			generationDataLabel = new JLabel("");
			numberOfPointsCreatedDataLabel = new JLabel("");
			generatedIntNumberDataLabel = new JLabel("");
			generatedDoubleNumberDataLabel = new JLabel("");
		}
	}

	/**
	 * Creates a panel holding the data statistics.
	 * 
	 * @return Panel holding current generation, generated number, etc.
	 */
	private JPanel createDataDisplaySubSubPanel()
	{
		// create the labels for the display
		createDataDisplayLabels();
		JLabel generationLabel = new JLabel("Generation:   ");
		JLabel numberOfPointsCreatedLabel = new JLabel(
				"# of points created:   ");
		JLabel generatedIntNumberLabel = new JLabel("The generated number:   ");
		JLabel generatedDoubleNumberLabel = new JLabel(
				"Rescaled from 0.0 to 1.0:   ");

		// now add to a JPanel
		JPanel dataPanel = new JPanel(new GridBagLayout());
		dataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), DATA_PANEL_TITLE, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor));

		// generation label
		int row = 0;
		dataPanel.add(generationLabel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		dataPanel.add(generationDataLabel, new GBC(5, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// number of points generated
		row = 1;
		dataPanel.add(numberOfPointsCreatedLabel, new GBC(1, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		dataPanel.add(numberOfPointsCreatedDataLabel, new GBC(5, row).setSpan(
				4, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// generated int label
		row = 2;
		dataPanel.add(generatedIntNumberLabel, new GBC(1, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		dataPanel.add(generatedIntNumberDataLabel, new GBC(5, row)
				.setSpan(4, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(
						GBC.WEST).setInsets(1));

		// generated decimal
		row = 3;
		dataPanel.add(generatedDoubleNumberLabel, new GBC(1, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
		dataPanel.add(generatedDoubleNumberDataLabel, new GBC(5, row).setSpan(
				4, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// space
		row = 4;
		JLabel space = new JLabel(" ");
		space.setFont(fonts.getPlainMiniFont());
		dataPanel.add(space, new GBC(0, row).setSpan(10, 1).setFill(GBC.BOTH)
				.setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		return dataPanel;
	}

	/**
	 * This uses a handy file writing utility to create a file writer.
	 */
	private void createFileWriter()
	{
		try
		{
			// This will prompt the user to enter a file. (The save data file
			// path parameter is just the default folder where the file chooser
			// will open.)
			fileWriter = new FileWriter(CurrentProperties.getInstance()
					.getSaveDataFilePath());

			// data delimiters (what string will be used to separate data in the
			// file)
			delimiter = CurrentProperties.getInstance().getDataDelimiters();

			// save a header
			String[] header = {"Creation number: ", "Generation: ",
					"Number of bits used: ", "Generated number: ",
					"Rescaled between 0 and 1: "};
			fileWriter.writeData(header, delimiter);

			// save the initial data (at the generation when the user requested
			// that the data be saved)
			if(data != null && data[0] != null)
			{
				fileWriter.writeData(data, delimiter);
			}
		}
		catch(IOException e)
		{
			// This happens if the user did not select a valid file. (For
			// example, the user cancelled and did not choose any file when
			// prompted.) So uncheck the "file save" box
			if(saveDataCheckBox != null)
			{
				saveDataCheckBox.setSelected(false);
			}

			// tell the user that they really should have selected a file
			String message = "A valid file was not selected, so the data \n"
					+ "will not be saved.";
			JOptionPane.showMessageDialog(CAController.getCAFrame().getFrame(),
					message, "Valid file not selected",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Create the panel used to display the generated number statistics.
	 */
	private void createFinalDisplayPanel()
	{
		if(displayPanel == null)
		{
			// create the display panel
			displayPanel = new JPanel(new BorderLayout());
			displayPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			displayPanel.setPreferredSize(new Dimension(
					CAFrame.tabbedPaneDimension.width, 1425));

			// create a panel that displays messages
			JPanel messagePanel = createMessagePanel();

			// create a panel that holds the plots
			JPanel plotPanel = createPlotPanel();

			// the adjustable properties
			JPanel propertyPanel = createPropertySubPanel();

			// the save data check box
			JPanel saveDataPanel = createSaveDataCheckBoxPanel();

			// create a panel that holds both the boxOfLabels, the adjustable
			// properties, and the save button
			JPanel labelsPropertiesAndSavePanel = new JPanel(new BorderLayout());
			labelsPropertiesAndSavePanel.setBorder(BorderFactory
					.createEmptyBorder(5, 5, 5, 5));
			labelsPropertiesAndSavePanel.add(propertyPanel, BorderLayout.NORTH);
			labelsPropertiesAndSavePanel
					.add(saveDataPanel, BorderLayout.CENTER);

			// add everything to the display (using BorderLayout)
			displayPanel.add(messagePanel, BorderLayout.NORTH);
			displayPanel.add(plotPanel, BorderLayout.CENTER);
			displayPanel.add(labelsPropertiesAndSavePanel, BorderLayout.SOUTH);
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

		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		messagePanel.add(attentionPanel, BorderLayout.NORTH);
		messagePanel.add(messageLabel, BorderLayout.CENTER);

		return messagePanel;
	}

	/**
	 * Create radio buttons to decide upon the number of bits.
	 * 
	 * @return a panel containing the buttons.
	 */
	private JPanel createNumberOfBitsButtons()
	{
		byteButton = new JRadioButton("8 bits (byte)");
		byteButton.setFont(fonts.getPlainFont());
		byteButton.addItemListener(new NumBitsChoiceListener());
		byteButton.setSelected(true);

		shortButton = new JRadioButton("16 bits (short)");
		shortButton.setFont(fonts.getPlainFont());
		shortButton.addItemListener(new NumBitsChoiceListener());
		shortButton.setSelected(false);

		intButton = new JRadioButton("32 bits (int)");
		intButton.setFont(fonts.getPlainFont());
		intButton.addItemListener(new NumBitsChoiceListener());
		intButton.setSelected(false);

		longButton = new JRadioButton("64 bits (long)");
		longButton.setFont(fonts.getPlainFont());
		longButton.addItemListener(new NumBitsChoiceListener());
		longButton.setSelected(false);

		// put them in a group so that they behave as radio buttons
		ButtonGroup group = new ButtonGroup();
		group.add(byteButton);
		group.add(shortButton);
		group.add(intButton);
		group.add(longButton);

		// the amount of vertical space to put between components
		int verticalSpace = 5;

		// create a box holding the buttons
		Box buttonBox = Box.createVerticalBox();
		buttonBox.add(byteButton);
		buttonBox.add(Box.createVerticalStrut(verticalSpace));
		buttonBox.add(shortButton);
		buttonBox.add(Box.createVerticalStrut(verticalSpace));
		buttonBox.add(intButton);
		buttonBox.add(Box.createVerticalStrut(verticalSpace));
		buttonBox.add(longButton);

		// now add to a JPanel
		JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		radioPanel.add(buttonBox);

		return radioPanel;
	}

	/**
	 * Create a JPanel that holds all of the plots.
	 * 
	 * @return panel holding the random data plots.
	 */
	private JPanel createPlotPanel()
	{
		// create a panel that plots the number data
		numberPlot = new SimplePlot();

		// create a panel that plots the percentage of 1's (bits) that are
		// generated
		percentagePlot = new SimplePlot();

		// create a panel that plots the distribution of the number data
		distributionPlot = new SimplePlot();

		// put the above in a single panel
		JPanel plotPanel = new JPanel(new GridBagLayout());
		plotPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		// generated number plot
		int row = 0;
		plotPanel.add(numberPlot, new GBC(0, row).setSpan(10, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		// percentage of 1's plot
		row = 2;
		plotPanel.add(percentagePlot, new GBC(0, row).setSpan(10, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		// distribution plot
		row = 3;
		plotPanel.add(distributionPlot, new GBC(0, row).setSpan(10, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		return plotPanel;
	}

	/**
	 * Create a panel that holds all the properties that will be submitted (like
	 * row, column, etcetera) and properties that will be displayed (generation,
	 * etcetera).
	 * 
	 * @return a panel holding all the property components, like the row and
	 *         column text fields.
	 */
	private JPanel createPropertySubPanel()
	{
		// create data statistics display
		JPanel dataDisplayPanel = createDataDisplaySubSubPanel();

		// create a panel with properties that can be changed
		JPanel submittablePropertiesPanel = createSubmitPropertiesSubSubPanel();

		// put the above in a single panel
		JPanel comboPanel = new JPanel(new GridBagLayout());
		comboPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		// space at top
		int row = 0;
		JLabel space = new JLabel(" ");
		space.setFont(fonts.getPlainMiniFont());
		comboPanel.add(space, new GBC(0, row).setSpan(10, 1).setFill(GBC.BOTH)
				.setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		// data display panel
		row = 1;
		comboPanel.add(dataDisplayPanel, new GBC(0, row).setSpan(10, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// row and col choosers
		row = 2;
		comboPanel.add(submittablePropertiesPanel, new GBC(0, row).setSpan(10,
				1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		return comboPanel;
	}

	/**
	 * Create row and column text fields and puts in a JPanel.
	 * 
	 * @return contains the row and column text fields.
	 */
	private JPanel createRowAndColumnPanel()
	{
		// width and height of the lattice
		int width = CurrentProperties.getInstance().getNumColumns();
		int height = CurrentProperties.getInstance().getNumRows();

		// create spinners for the row and height
		SpinnerNumberModel colModel = new SpinnerNumberModel(1, 1, width, 1);
		colSpinner = new JSpinner(colModel);
		colSpinner.setToolTipText(COLUMN_TIP);
		colSpinner.addChangeListener(new RowColListener());

		SpinnerNumberModel rowModel = new SpinnerNumberModel(1, 1, height, 1);
		rowSpinner = new JSpinner(rowModel);
		rowSpinner.setToolTipText(ROW_TIP);
		rowSpinner.addChangeListener(new RowColListener());

		// is lattice one-dimensional?
		boolean isOneDimensional = OneDimensionalLattice
				.isCurrentLatticeOneDim();
		if(isOneDimensional)
		{
			// has to be a 1 if one-dimensional
			rowSpinner.setEnabled(false);
		}

		// create a label for the starting column position
		JLabel colLabel = new JLabel("Col: ");
		colLabel.setFont(fonts.getPlainFont());

		// create a label for the row position
		JLabel rowLabel = new JLabel("Row: ");
		rowLabel.setFont(fonts.getPlainFont());

		JPanel colPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		colPanel.add(colLabel);
		colPanel.add(colSpinner);

		JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		rowPanel.add(rowLabel);
		rowPanel.add(rowSpinner);

		// put the components in a JPanel with some space
		JPanel spacePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		spacePanel.add(colPanel);
		spacePanel.add(rowPanel);

		return spacePanel;
	}

	/**
	 * Creates the panel holding the save data check box.
	 * 
	 * @return a panel holding the save data check box.
	 */
	private JPanel createSaveDataCheckBoxPanel()
	{
		// create a "save data" check box
		saveDataCheckBox = new JCheckBox(SAVE_DATA);
		saveDataCheckBox.setToolTipText(SAVE_DATA_TOOLTIP);
		saveDataCheckBox.setActionCommand(SAVE_DATA);
		saveDataCheckBox.addActionListener(this);
		JPanel saveDataPanel = new JPanel();
		saveDataPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
		saveDataPanel.add(saveDataCheckBox);

		return saveDataPanel;
	}

	/**
	 * Creates a panel holding all the properties that can be submitted.
	 * 
	 * @return
	 */
	private JPanel createSubmitPropertiesSubSubPanel()
	{
		// create a panel that asks the user for the row and column position
		// where the bit data will be collected
		JPanel rowAndColumnPanel = createRowAndColumnPanel();

		// create the radio buttons for the number of bits
		JPanel numBitsPanel = createNumberOfBitsButtons();

		// create labels for each of the above
		JLabel rowColLabel = new JLabel("Position: ");
		JLabel numBitsLabel = new JLabel("Bits in #: ");

		// put the above in a single panel
		JPanel comboPanel = new JPanel(new GridBagLayout());
		comboPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), SUBMIT_PANEL_TITLE, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor));

		// row and col choosers
		int row = 0;
		comboPanel.add(rowColLabel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		comboPanel.add(rowAndColumnPanel, new GBC(5, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// num bits chooser
		row = 1;
		comboPanel.add(numBitsLabel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		comboPanel.add(numBitsPanel, new GBC(5, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		// space
		row = 3;
		JLabel space = new JLabel(" ");
		space.setFont(fonts.getPlainMiniFont());
		comboPanel.add(space, new GBC(0, row).setSpan(10, 1).setFill(GBC.BOTH)
				.setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		return comboPanel;
	}

	/**
	 * Checks if the given column is between 0 and the width of the lattice.
	 * 
	 * @param cols
	 *            The column being checked.
	 * @return true if the column is ok.
	 */
	private boolean isColumnPositionOk(int cols)
	{
		boolean ok = false;

		int width = CurrentProperties.getInstance().getNumColumns();

		if((cols >= 0) && (cols < width))
		{
			ok = true;
		}

		return ok;
	}

	/**
	 * Checks if the given row is between 0 and the height of the lattice.
	 * 
	 * @param row
	 *            The row being checked.
	 * @return true if the column is ok.
	 */
	private boolean isRowPositionOk(int row)
	{
		boolean ok = false;

		int height = CurrentProperties.getInstance().getNumRows();

		boolean isOneDimensional = OneDimensionalLattice
				.isCurrentLatticeOneDim();

		if(isOneDimensional)
		{
			// has to be the first row (i.e., 0) for one-dimensional lattices
			if(row == 0)
			{
				ok = true;
			}
		}
		else
		{
			if((row >= 0) && (row < height))
			{
				ok = true;
			}
		}

		return ok;
	}

	/**
	 * Gets the cell from the lattice that is being used to generate the random
	 * number.
	 * 
	 * @param lattice
	 *            The CA lattice containing the CA cells.
	 */
	private Cell loadCellBeingAnalyzed(Lattice lattice)
	{
		// the width of the lattice
		int numCols = CurrentProperties.getInstance().getNumColumns();

		// get the cells
		Cell cellBeingAnalyzed = lattice.getCells()[row * numCols + column];

		return cellBeingAnalyzed;
	}

	/**
	 * Plots the distribution of the number data.
	 * 
	 * @param binnedData
	 *            The number of points that fall within each bin. The number of
	 *            bins is equal to the length of the array.
	 */
	private void plotDistribution(int[] binnedData)
	{
		// number of bins
		int numberOfBins = binnedData.length;

		// the length of each bin
		double dx = 1.0 / numberOfBins;

		// total number of points collected
		int numberOfDataPoints = 0;
		for(int i = 0; i < numberOfBins; i++)
		{
			numberOfDataPoints += binnedData[i];
		}

		// the maximum percentage (i.e., percentage in the biggest bin)
		double maxPercent = Double.MIN_VALUE;

		// the upper bounds of each bin (except the last upper bound of 1.0)
		double[] upperBounds = new double[numberOfBins - 1];
		for(int i = 0; i < upperBounds.length; i++)
		{
			// the upper bound of the bin
			if(upperBounds.length <= 5)
			{
				// show the upper bound to two decimal places
				upperBounds[i] = Math.round(100.0 * ((i + 1.0) * dx)) / 100.0;
			}
			else if(upperBounds.length <= 10)
			{
				// show the upper bound to one decimal place
				upperBounds[i] = Math.round(10.0 * ((i + 1.0) * dx)) / 10.0;
			}
		}

		// get the percentage in each bin and store in an array
		Point2D[] binPercent = new Point2D[numberOfBins];
		for(int i = 0; i < numberOfBins; i++)
		{
			// the percent in the ith bin
			double percentage = (double) binnedData[i]
					/ (double) numberOfDataPoints;

			// the midpoint of the ith bin
			double midPoint = (i * dx) + (dx / 2.0);

			// create the point for plotting
			binPercent[i] = new Point2D.Double(midPoint, percentage);

			// keep track of the maxPercent
			if(maxPercent < percentage)
			{
				maxPercent = percentage;
			}
		}

		// get min and max x-value
		double xMin = 0.0;
		double xMax = 1.0;

		// get min and max y-value
		double yMin = 0.0;
		double yMax = 1.0;
		if(maxPercent <= 0.2)
		{
			yMax = 0.2;
		}
		else if(maxPercent <= 0.3)
		{
			yMax = 0.3;
		}
		else if(maxPercent <= 0.5)
		{
			yMax = 0.5;
		}

		distributionPlot.showXValuesAsInts(false);
		distributionPlot.setMinimumXValue(xMin);
		distributionPlot.setMaximumXValue(xMax);
		distributionPlot.setMinimumYValue(yMin);
		distributionPlot.setMaximumYValue(yMax);
		distributionPlot.setXAxisLabel("binned values, x");
		distributionPlot.setYAxisLabel("% in each bin, P(x)");
		if(upperBounds.length < 10)
		{
			distributionPlot.setExtraXAxisValues(upperBounds);
		}
		distributionPlot.drawPoints(binPercent);
	}

	/**
	 * Plots the generated number data.
	 */
	private void plotNumberData()
	{
		// get min and max x-value
		double min = 1;
		double max = MAX_NUMBER_TO_PLOT;
		if(generatedNumberList != null && !generatedNumberList.isEmpty())
		{
			// set the min and max values on the plot
			Point2D firstPoint = (Point2D) generatedNumberList.getFirst();
			min = firstPoint.getX();
			max = firstPoint.getX() + (MAX_NUMBER_TO_PLOT - 1);
		}

		numberPlot.setMinimumXValue(min);
		numberPlot.setMaximumXValue(max);
		numberPlot.setMaximumYValue(1.0);
		numberPlot.setMinimumYValue(0.0);
		numberPlot.setXAxisLabel("number of \"random numbers\" created");
		numberPlot.setYAxisLabel("generated number");
		numberPlot.drawPoints(generatedNumberList);
	}

	/**
	 * Plots the percentage of 1's that have been generated (out of all the bits
	 * generated).
	 * 
	 * @param generation
	 *            The current generation.
	 */
	private void plotPercentageOnes(int generation)
	{
		// calculate the percentage of 1's
		double percentageOf1Bits = (double) num1BitsCollectedOverAllTime
				/ (double) numBitsCollectedOverAllTime;

		// save the number in a persistent list
		percentageOnesList
				.add(new Point2D.Double(generation, percentageOf1Bits));
		if(percentageOnesList.size() > MAX_NUMBER_TO_PLOT)
		{
			percentageOnesList.removeFirst();
		}

		// get min and max x-value
		double min = 1;
		double max = MAX_NUMBER_TO_PLOT;
		if(percentageOnesList != null && !percentageOnesList.isEmpty())
		{
			// set the min and max values on the plot
			Point2D firstPoint = (Point2D) percentageOnesList.getFirst();
			min = firstPoint.getX();
			max = firstPoint.getX() + (MAX_NUMBER_TO_PLOT - 1);
		}

		percentagePlot.setMinimumXValue(min);
		percentagePlot.setMaximumXValue(max);
		percentagePlot.setMaximumYValue(1.0);
		percentagePlot.setMinimumYValue(0.0);
		percentagePlot.setXAxisLabel("number of bits generated");
		percentagePlot.setYAxisLabel("percentage of 1's");

		double[] value = {0.5};
		percentagePlot.setExtraYAxisValues(value);

		percentagePlot.drawPoints(percentageOnesList);
	}

	/**
	 * Saves the specified data to the file.
	 * 
	 * @param data
	 *            The data that will be saved.
	 */
	private void saveData(String[] data)
	{
		if(fileWriter != null)
		{
			try
			{
				fileWriter.writeData(data, delimiter);
			}
			catch(IOException e)
			{
				// Could not save the data, so close the file
				if(fileWriter != null)
				{
					fileWriter.close();
				}

				// and uncheck the "save data" box
				if(saveDataCheckBox != null)
				{
					saveDataCheckBox.setSelected(false);
				}
			}
		}
	}

	/**
	 * Handles a submission for a new row and column and other properties.
	 */
	private void submitRowColChanges()
	{
		// read the number of cols
		Integer colInteger = (Integer) ((SpinnerNumberModel) colSpinner
				.getModel()).getNumber();

		// subtract 1 so that they can enter values from 1 to width (rather than
		// 0 to width-1)
		this.column = colInteger.intValue() - 1;

		// read the number of rows
		// read the row number
		Integer rowInteger = (Integer) ((SpinnerNumberModel) rowSpinner
				.getModel()).getNumber();

		// subtract 1 so that they can enter values from 1 to width (rather than
		// 0 to width-1)
		this.row = rowInteger.intValue() - 1;

		// reset the plots and the number being collected
		resetNumberData();

		// rerun the analysis so it grabs the new tagged cell
		rerunAnalysis();

		// and repaint so the new tagged cell is shown
		// refreshGraphics();
	}

	/**
	 * Untags the currently tagged cell.
	 */
	private void untagCell()
	{
		// untag the cell so no longer has extra visibility
		if(cellBeingAnalyzed != null)
		{
			// remove both -- either could be currently in use
			cellBeingAnalyzed.setTagged(false, TAG1);
			cellBeingAnalyzed.setTagged(false, TAG2);
		}
	}

	/**
	 * Generates a number from the CA and tests for randomness.
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
		// reserve two tagging colors
		if(firstTimeThrough)
		{
			ColorScheme.reserveTaggedColor(TAG1);
			ColorScheme.reserveTaggedColor(TAG2);
		}

		// load the cell that needs to be analyzed
		if(cellBeingAnalyzed == null)
		{
			cellBeingAnalyzed = loadCellBeingAnalyzed(lattice);
		}

		// Tag the cell an appropriate color that alternates every time a new
		// number is being created. Do this using two different objects that
		// indicate the tagging. "tag1" will assign one color, and "tag2" will
		// assign another color.
		// 
		// Note that I carefully remove both of these tagging objects when the
		// analysis is being closed.
		if(numbersCreated % 2 == 0)
		{
			cellBeingAnalyzed.setTagged(true, TAG1);
		}
		else
		{
			cellBeingAnalyzed.setTagged(true, TAG2);
		}

		// display the tagged cells
		if(firstTimeThrough)
		{
			refreshGraphics();
		}

		// reset, so only true the first time the analyze method is called
		firstTimeThrough = false;

		// if the state of the cell is full then assign a bit value of 1, else
		// assign a bit value of 0
		if(cellBeingAnalyzed.getState(generation).isFull())
		{
			// Add the bit at the appropriate position. How's this work? The
			// "<<" lefts shifts the "1" a certain number of bits. It is shifted
			// by the "numBitsCollected". E.g., if collecting 8 bits, then it
			// will shift left by 0, then by 1 the next time, then by 2, 3, 4,
			// 5, 6, and finally 7. Then the "|" or OR operator adds that bit to
			// the number. So for example, if the cell generates the bits 1, 0,
			// 0, 1, 1, 0, 1, 0 in that order, then theNumber *in binary* would
			// look like 1, then 01, then 001, then 1001, then 11001, then
			// 011001, then 011001, then 1011001, and finally 01011001. Note
			// that the highest order (leftmost) bit actually indicates the sign
			// of the number. A 0 is positive and a 1 is negative. And to make
			// things slightly more complicated, Java represents numbers in
			// "two's complement" notation to avoid getting both a 0 and a -0.
			// Read about two's complement elsewhere.
			long one = 1;
			theNumber |= (one << numBitsCollectedForGeneratedNumber);

			// WARNING: the "one" has to be defined as a long and not as an int.
			// i.e., can't use a "1" in the formula. If use a "1", then the
			// leftshift will only shift a max of 32 places (for an int).

			// NOTE: bits are generated from low bit to high bit. i.e.,
			// generation 0 gives the 1's place bit, generation 1 gives the 2's
			// place bit, generation 2 gives the 3's place bit, etc.

			// It would have been simpler and easier to understand (but slightly
			// slower) to save the bits in an array and then construct the
			// number from the array. The BitSet class would also help make
			// things simpler.

			// Now, keep track of the number of 1's collected over all time
			num1BitsCollectedOverAllTime++;
		}

		// total number of bits collected over all time
		numBitsCollectedOverAllTime++;

		// DATA COMPRESSION TEST
		// convert the number to a byte (for the data compression test)
		// if(numBitsCollectedOverAllTime % 8 == 0)
		// {
		// // Then we've filled a byte. Cast to throw away the higher bits.
		// byteList.add(new Byte((byte) theNumber));
		//
		// // only keep a certain number of bytes
		// if(byteList.size() > MAX_NUMBER_OF_BYTES)
		// {
		// byteList.removeFirst();
		// }
		//
		// // get the compression
		// double percentageReduction = getCompressedDataSize(byteList);
		// }

		// increment the number of bits that we have collected for building the
		// number (must do this now before the next if statement may set it to
		// 0)
		numBitsCollectedForGeneratedNumber++;

		// have we got all the bits we need to build our number? The > is
		// necessary when the user reduces the number of bits collected for the
		// number.
		if(numBitsCollectedForGeneratedNumber >= numBitsWeNeedToCollectForGeneratedNumber)
		{
			// Then cast the number to the appropriate type!

			// first reset the number of bits collected
			numBitsCollectedForGeneratedNumber = 0;

			// indicate that we have created another number
			numbersCreated++;

			// the min and max possible values for a long, int, short, or byte
			// (whichever is appropriate). Used when rescaling.
			long min = 0;
			long max = 0;

			// Students of software engineering will recognize an opportunity
			// for an abstract factory and/or strategy pattern. Sometimes its
			// just not worth the trouble when you know that flexibility and
			// extensibility will never be an issue (there are no more integer
			// data types, so this won't need expanding for more types)
			Number theNumberCast = null;
			if(numBitsWeNeedToCollectForGeneratedNumber == Byte.SIZE)
			{
				theNumberCast = new Byte((byte) theNumber);
				max = Byte.MAX_VALUE;
				min = Byte.MIN_VALUE;
			}
			else if(numBitsWeNeedToCollectForGeneratedNumber == Short.SIZE)
			{
				theNumberCast = new Short((short) theNumber);
				max = Short.MAX_VALUE;
				min = Short.MIN_VALUE;
			}
			else if(numBitsWeNeedToCollectForGeneratedNumber == Integer.SIZE)
			{
				theNumberCast = new Integer((int) theNumber);
				max = Integer.MAX_VALUE;
				min = Integer.MIN_VALUE;
			}
			else
			{
				theNumberCast = new Long(theNumber);
				max = Long.MAX_VALUE;
				min = Long.MIN_VALUE;
			}

			// rescale as a double between 0 and 1. Note: I did a test, and
			// nothing is gained by using BigDecimal arithmetic, even for the 64
			// bit longs.
			double rescaledNumber = (theNumberCast.doubleValue() - (double) min)
					/ ((double) max - (double) min);

			// save the number in a persistent list
			generatedNumberList.add(new Point2D.Double(numbersCreated,
					rescaledNumber));
			if(generatedNumberList.size() > MAX_NUMBER_TO_PLOT)
			{
				generatedNumberList.removeFirst();
			}

			// DISPLAY CHANGES
			// set the text for the labels
			generationDataLabel.setText("" + generation);
			numberOfPointsCreatedDataLabel.setText("" + numbersCreated);
			generatedIntNumberDataLabel.setText("" + theNumberCast);

			// and set the text for the number as a rescaled double between 0.0
			// and 1.0, but format!
			DecimalFormat myFormatter = new DecimalFormat(DECIMAL_PATTERN);
			String output = myFormatter.format(rescaledNumber);
			generatedDoubleNumberDataLabel.setText(output);

			// DISTRIBUTION TEST
			// Bins the data in intervals between 0.0 and 1.0. Intervals are
			// size dx.
			double dx = 1.0 / DEFAULT_NUMBER_OF_INTERVALS;
			int arrayPosition = 0;

			// find the correct bin (array position)
			while((rescaledNumber > (arrayPosition + 1.0) * dx)
					&& (arrayPosition < DEFAULT_NUMBER_OF_INTERVALS))
			{
				arrayPosition += 1;
			}

			// increase the number of points that fell into this bin
			bin[arrayPosition] += 1;

			// SAVE THE DATA
			// create an array of data to be saved
			data[0] = "" + numbersCreated;
			data[1] = "" + generation;
			data[2] = "" + numBitsWeNeedToCollectForGeneratedNumber;
			data[3] = "" + theNumberCast;
			data[4] = "" + rescaledNumber;

			// see if user wants to save the data
			if(fileWriter != null)
			{
				// save it
				saveData(data);
			}

			// we're done with the number so set it to 0 again to prepare for
			// the next number
			theNumber = 0;
		}

		// plot the generated numbers (always do this, even if there is nothing
		// new)
		plotNumberData();

		// plot the percentage of 0's and 1's
		plotPercentageOnes(generation);

		// plot the distribution
		plotDistribution(bin);
	}

	/**
	 * Performs any desired operations when the analysis is stopped (closed) by
	 * the user. For example, you might write the results to a file at this
	 * time. Or you might dispose of any windows that you opened. May do
	 * nothing.
	 */
	protected void stopAnalysis()
	{
		// if the user has been saving data, then close that data file when the
		// analysis is stopped.
		if(fileWriter != null)
		{
			fileWriter.close();
		}

		// untag the cell so no longer extra visibility
		untagCell();

		// release the colors that were used by this analysis
		ColorScheme.releaseTaggedColor(TAG1);
		ColorScheme.releaseTaggedColor(TAG2);

		// and refresh the graphics so don't show the cells as tagged anymore
		refreshGraphics();
	}

	/**
	 * Reacts to the "save data" check box.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();

		if(command.equals(SAVE_DATA))
		{
			if(saveDataCheckBox.isSelected())
			{
				// they want to save data, so open a data file
				createFileWriter();
			}
			else
			{
				// They don't want to save data anymore, so close the file.
				// The synchronized keyword prevents accidental access elsewhere
				// in the code while the file is being closed. Otherwise, other
				// code might try to write to the file while it is being closed.
				synchronized(this)
				{
					if(fileWriter != null)
					{
						fileWriter.close();
						fileWriter = null;
					}
				}
			}
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
	 * Return null if compatible with all lattices.
	 * 
	 * @return A list of lattices compatible with this Analysis (returns the
	 *         display names for the lattices). Returns null if compatible with
	 *         all lattices.
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
		// reset the plots and the number data
		resetNumberData();

		// if the user has been saving data, then close that data file
		synchronized(this)
		{
			if(fileWriter != null)
			{
				fileWriter.close();
				fileWriter = null;
			}
		}

		// and uncheck the "save data" box
		if(saveDataCheckBox != null)
		{
			saveDataCheckBox.setSelected(false);
		}

		// NOW, a big long check for the row and col spinners

		// new width and height of the lattice
		int width = CurrentProperties.getInstance().getNumColumns();
		int height = CurrentProperties.getInstance().getNumRows();

		// reset the max values on the row and col spinners
		SpinnerNumberModel colModel = new SpinnerNumberModel(1, 1, width, 1);
		SpinnerNumberModel rowModel = new SpinnerNumberModel(1, 1, height, 1);
		colSpinner.setModel(colModel);
		rowSpinner.setModel(rowModel);

		// the new analysis may have a different lattice, so check that the
		// row and col are still ok
		int cols = ((Integer) ((SpinnerNumberModel) colSpinner.getModel())
				.getNumber()).intValue() - 1;
		if(!isColumnPositionOk(cols))
		{
			// give a default value
			((SpinnerNumberModel) colSpinner.getModel())
					.setValue(new Integer(1));
			column = 0;
		}
		else
		{
			column = cols;
		}

		int row = ((Integer) ((SpinnerNumberModel) rowSpinner.getModel())
				.getNumber()).intValue() - 1;
		if(!isRowPositionOk(row))
		{
			// give a default value
			((SpinnerNumberModel) rowSpinner.getModel())
					.setValue(new Integer(1));
			row = 0;
		}
		else
		{
			this.row = row;
		}

		// if one-dimensional, then disable the row field, otherwise enable
		boolean isOneDimensional = OneDimensionalLattice
				.isCurrentLatticeOneDim();
		if(isOneDimensional)
		{
			// because must be the default value of 1
			rowSpinner.setEnabled(false);
		}
		else
		{
			rowSpinner.setEnabled(true);
		}
	}

	/**
	 * Resets the number that is being collected and resets the plots.
	 */
	public synchronized void resetNumberData()
	{
		// method is synchronized so that resetting the generatedNumberList and
		// percentageOnelist don't get emptied at the same time they are being
		// used to plot the data.

		// unmark the old cell because we have now selected a new cell
		untagCell();
		
		// forces it to get the new cell for the new simulation
		cellBeingAnalyzed = null;

		// empty the plot lists (so that old data doesn't get plotted again when
		// the new simulation starts)
		generatedNumberList.clear();
		percentageOnesList.clear();

		// reset the plots
		numberPlot.clearPlot();
		percentagePlot.clearPlot();
		distributionPlot.clearPlot();

		// reset the number!
		theNumber = 0;

		// reset the number of bits collected
		numBitsCollectedForGeneratedNumber = 0;

		// reset the number of numbers that have been generated
		numbersCreated = 0;

		// reset the number of 1 bits collected
		num1BitsCollectedOverAllTime = 0;

		// reset the total number of bits collected over all time
		numBitsCollectedOverAllTime = 0;

		// reset the bins of numbers for the frequency test
		bin = new int[DEFAULT_NUMBER_OF_INTERVALS];

		// reset the info displayed on the JPanel
		generationDataLabel.setText("");
		numberOfPointsCreatedDataLabel.setText("");
		generatedIntNumberDataLabel.setText("");
		generatedDoubleNumberDataLabel.setText("");

		// whenever the analysis is setup, make it redraw the tagged cells
		firstTimeThrough = true;
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
	 * Decides what to do when the user selects the number of bits.
	 * 
	 * @author David Bahr
	 */
	private class NumBitsChoiceListener implements ItemListener
	{
		public void itemStateChanged(ItemEvent event)
		{
			// force it to get a new cell
			untagCell();
			cellBeingAnalyzed = null;

			if(byteButton.isSelected())
			{
				numBitsWeNeedToCollectForGeneratedNumber = Byte.SIZE;
			}
			else if(shortButton.isSelected())
			{
				numBitsWeNeedToCollectForGeneratedNumber = Short.SIZE;
			}
			else if(intButton.isSelected())
			{
				numBitsWeNeedToCollectForGeneratedNumber = Integer.SIZE;
			}
			else if(longButton.isSelected())
			{
				numBitsWeNeedToCollectForGeneratedNumber = Long.SIZE;
			}
		}
	}

	/**
	 * listens for changes to the row spinner.
	 */
	private class RowColListener implements ChangeListener
	{
		/**
		 * Listens for changes to the row and col spinners.
		 * 
		 * @param e
		 */
		public void stateChanged(ChangeEvent e)
		{
			submitRowColChanges();
		}
	}
}
