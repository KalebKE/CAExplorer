/*
 MusicAnalysis -- a class within the Cellular Automaton Explorer. 
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Iterator;

import javax.sound.midi.Instrument;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.OneDimensionalLattice;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;
import cellularAutomata.util.music.MusicalInstrument;
import cellularAutomata.util.music.MusicalScales;

/**
 * Takes a slice of cells, treats occupied cells as notes on a musical staff,
 * and then plays that music.
 * 
 * @author David Bahr
 */
public class MusicAnalysis extends Analysis implements ActionListener
{
	// /**
	// * A String containing an eighth note. Doesn't seem to be supported.
	// */
	// public static final String EIGHTH_NOTE = "\u1D160";
	/**
	 * String used for text display on the submite button and for setting its
	 * action command.
	 */
	public static final String MUTE_MUSIC = " mute music";

	/**
	 * String used for text display on the submite button and for setting its
	 * action command.
	 */
	public static final String SUBMIT_CHANGES = "Submit";

	// /**
	// * A String containing a treble clef. Doesn't seem to be supported.
	// */
	// public static final String TREBLE_CLEF = "\u1D120";
	// the color for error messages (light red)
	private static final Color ERROR_COLOR = new Color(255, 0, 0);

	// a brief display name for this class
	private static final String ANALYSIS_NAME = "Music";

	// Action command for the combo box that chooses an instrument
	private static final String CHANGE_INSTRUMENT = "Change instrument";

	// Action command for the combo box that chooses an octave (or vocal range).
	private static final String CHANGE_OCTAVE = "Change octave";

	// The tooltip for the column position where the music will start
	private static final String COLUMN_TIP = "<html>Choose the column for the "
			+ "first <br> cell played in the music.</html>";

	// display info for this class
	private static final String INFO_MESSAGE = "This analysis turns a "
			+ "cellular automaton into music. How? It takes a "
			+ "set of cells (highlighted on the display), and each cell "
			+ "corresponds to a note in a musical "
			+ "scale. A synthesizer plays the note when the cell's state is "
			+ "full, and stops playing the note when the cell is empty (or in "
			+ "an inbetween state).\n\n"
			+ "The music can sound interesting or horrible depending upon "
			+ "the selected CA rule, but even in bad music, the human ear is "
			+ "remarkably good at discerning patterns. Try a one-dimensional "
			+ "CA with random initial conditions, and slow down the simulation "
			+ "to 0.1 so you can hear all of the notes.\n\n"
			+ "To hear patterns, sometimes only percussion is necessary.  Try "
			+ "the \"Tenor\" setting with Taiko Drums, Melodic Toms, or Synthetic Drums.";

	private static final String INSTRUMENT_TIP = "<html>Choose an instrument. "
			+ "Your computer's <br> synthesizer may not play all instruments.</html>";

	// a brief tooltip description for the mute button
	private static final String MUTE_TIP = "<html>Horrible \"music\"? "
			+ "This mutes it.</html>";

	// a brief tooltip description for the volume slider
	private static final String VOLUME_TIP = "<html>Volume is also "
			+ "controlled by your speakers.</html>";

	// the available octaves
	private static final String[] octaves = {"Soprano", "Mezzo-Soprano",
			"Alto", "Tenor", "Baritone", "Basso", "Basso Profundo"};

	// the available octave values
	private static final int[] octaveValues = {MusicalScales.SOPRANO,
			MusicalScales.MEZZO_SOPRANO, MusicalScales.ALTO,
			MusicalScales.TENOR, MusicalScales.BARITONE, MusicalScales.BASSO,
			MusicalScales.BASSO_PROFUNDO

	};

	// a brief tooltip description for the mute button
	private static final String OCTAVE_TIP = "<html>Select an approximate "
			+ "range <BR> for the instrument.</html>";

	// The tooltip for the row position where the music will start
	private static final String ROW_TIP = "<html>Choose the row for the first <br>"
			+ "cell played in the music.</html>";

	// a brief tooltip description for this class
	private static final String TOOLTIP = "<html>plays music</html>";

	// only true the first time the analyze method is called
	private boolean firstTimeThrough = true;

	// if the user has been warned that there is no synthesizer (prevents
	// multiple messages)
	private boolean haveWarnedUserThereIsNoSynth = false;

	// if could not create a synthesizer
	private boolean noSynthesizer = false;

	// mutes the music when false (and plays it when true)
	private boolean play = true;

	// true when there has been a user input error
	private boolean userError = false;

	// starting column position
	private int column = 0;

	// the number of notes in the scale (e.g., 8 for a C major scale)
	private int numNotesInScale = MusicalScales.getCBluesScaleOneOctaveLength();

	// starting row position
	private int row = 0;

	// the selected octave or vocal range of the instrument
	private int vocalRange = MusicalScales.TENOR;

	// the selected volume
	private int volume = 60;

	// the cells for which music is generated
	private Cell[] musicCells = null;

	// fonts for display
	private Fonts fonts = new Fonts();

	// a hash table mapping vocal ranges to their numerical values.
	private Hashtable<String, Integer> vocalRangeHash = new Hashtable<String, Integer>(
			7, 1.0f);

	// The check box that mutes and unmutes the music
	private JCheckBox muteCheckBox = null;

	// The combo box for choosing an instrument
	private JComboBox instrumentChooser = null;

	// The combo box for choosing an octave (or vocal range)
	private JComboBox octaveChooser = null;

	// the panel where info is requested
	private JPanel displayPanel = null;

	// selects the starting column position for which music will be played
	private JSpinner colSpinner = null;

	// selects the starting row position for which music will be played
	private JSpinner rowSpinner = null;

	// radio button for a C blues scale
	private JRadioButton cBluesScaleButton = null;

	// radio button for a C harmonic minor scale
	private JRadioButton cHarmonicMinorScaleButton = null;

	// radio button for a C major scale
	private JRadioButton cMajorScaleButton = null;

	// radio button for a C pentatonic scale
	private JRadioButton cPentatonicScaleButton = null;

	// sets the volume
	private JSlider volumeSlider = null;

	// a label for displaying message
	private MultilineLabel errorMessageLabel = null;

	// the piano synthesizer
	private MusicalInstrument piano = new MusicalInstrument();

	// holds messages about synthesizer errors
	private String synthesizerErrorMessage = "";

	/**
	 * Create an analyzer that plays notes when a cell is full (occupied).
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
	public MusicAnalysis(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			// if already open, this does nothing
			openPianoSynthesizer();

			createVocalRangeHash();

			createDisplayPanel();

			// whenever the analysis is setup, make it redraw the tagged cells
			firstTimeThrough = true;
		}
	}

	/**
	 * Converts the cell number (between 0 and numNotesInScale-1) to a key
	 * number on the piano.
	 * 
	 * @param cellNumber
	 *            The cell's numbered position on the scale. O is the lowest
	 *            position and numNotesInScale-1 is the highest position on the
	 *            scale.
	 * @return A key number on the piano (a 0 is the lowest note and an 87 is
	 *         the highest note, and a 40 is middle C).
	 */
	private int convertCellNumberToNote(int cellNumber)
	{
		if(cMajorScaleButton.isSelected())
		{
			return MusicalScales
					.getCMajorScaleOneOctave(cellNumber, vocalRange);
		}
		else if(cHarmonicMinorScaleButton.isSelected())
		{
			return MusicalScales.getCHarmonicMinorScaleOneOctave(cellNumber,
					vocalRange);
		}
		else if(cPentatonicScaleButton.isSelected())
		{
			return MusicalScales.getCPentatonicScaleOneOctave(cellNumber,
					vocalRange);
		}
		else
		{
			return MusicalScales
					.getCBluesScaleOneOctave(cellNumber, vocalRange);
		}
	}

	/**
	 * Create the panel used to display the population statistics.
	 */
	private void createDisplayPanel()
	{
		if(displayPanel == null)
		{
			// the panel that will be returned
			displayPanel = new JPanel();
			displayPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			displayPanel.setLayout(new BorderLayout());
			displayPanel.setPreferredSize(new Dimension(
					CAFrame.tabbedPaneDimension.width, 800));

			if(piano.isOpen())
			{
				// create a panel that displays messages
				JPanel messagePanel = createMessagePanel();

				// get the panel holding properties that will be submitted
				JPanel propertyPanel = createPropertyPanel();
				JPanel finalPropertyPanel = new JPanel(new FlowLayout(
						FlowLayout.CENTER));
				finalPropertyPanel.add(propertyPanel);

				// add components to the panel
				displayPanel.add(messagePanel, BorderLayout.NORTH);
				displayPanel.add(finalPropertyPanel, BorderLayout.WEST);
			}
			else
			{
				JLabel noSynthLabel = new JLabel(
						"Sorry, no synthesizer is available \n on your computer.");

				displayPanel.add(noSynthLabel, BorderLayout.CENTER);
			}
		}
	}

	/**
	 * Creates a drop-down list of instruments that can be used.
	 * 
	 * @return
	 */
	private JPanel createInstrumentChoicePanel()
	{
		// the panel that will be returned
		JPanel instrumentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		// get the available instruments
		Instrument[] instruments = piano.getLoadedInstruments();

		if((instruments != null) && (instruments.length > 0))
		{
			String[] instrumentNames = new String[instruments.length];
			for(int i = 0; i < instruments.length; i++)
			{
				instrumentNames[i] = instruments[i].getName();
			}

			// create a combo box with the available instruments
			instrumentChooser = new JComboBox(instrumentNames);
			instrumentChooser.setActionCommand(CHANGE_INSTRUMENT);
			instrumentChooser.addActionListener(this);
			instrumentChooser.setSelectedItem("Piano");
			instrumentChooser.setFont(fonts.getBoldSmallerFont());
			instrumentChooser.setToolTipText(INSTRUMENT_TIP);

			// put combo box in the JPanel
			instrumentPanel.add(instrumentChooser);
		}

		return instrumentPanel;
	}

	/**
	 * Creates a panel that displays messages.
	 * 
	 * @return A panel containing messages.
	 */
	private JPanel createMessagePanel()
	{
		// a "grab their attention" panel
		AttentionPanel attentionPanel = new AttentionPanel("Play CA Music!");

		MultilineLabel messageLabel = new MultilineLabel(INFO_MESSAGE);
		messageLabel.setFont(fonts.getAnalysesDescriptionFont());
		messageLabel.setMargin(new Insets(6, 10, 2, 16));

		// create empty error message -- will be filled later as necessary
		errorMessageLabel = new MultilineLabel("");
		errorMessageLabel.setFont(fonts.getItalicSmallerFont());
		errorMessageLabel.setMargin(new Insets(0, 10, 0, 16));
		errorMessageLabel.setColumns(40);

		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.add(attentionPanel, BorderLayout.NORTH);
		messagePanel.add(messageLabel, BorderLayout.CENTER);
		messagePanel.add(errorMessageLabel, BorderLayout.SOUTH);

		return messagePanel;
	}

	/**
	 * Creates a mute checkbox inside of a panel.
	 * 
	 * @return A panel containing the checkbox.
	 */
	private JPanel createMuteCheckBoxPanel()
	{
		muteCheckBox = new JCheckBox("");
		muteCheckBox.setToolTipText(MUTE_TIP);
		muteCheckBox.setActionCommand(MUTE_MUSIC);
		muteCheckBox.addActionListener(this);

		JPanel mutePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		mutePanel.add(muteCheckBox);

		return mutePanel;
	}

	/**
	 * Create menu for choosing the octave.
	 * 
	 * @return contains the octave choice.
	 */
	private JPanel createOctavePanel()
	{
		// the panel that will be returned
		JPanel octavePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		// create a combo box with the available instruments
		octaveChooser = new JComboBox(octaves);
		octaveChooser.setActionCommand(CHANGE_OCTAVE);
		octaveChooser.addActionListener(this);
		octaveChooser.setFont(fonts.getBoldSmallerFont());
		octaveChooser.setToolTipText(OCTAVE_TIP);

		// This try clause is oddly necessary to catch an unfathomable error
		// when this class is packaged in a jar
		try
		{
			octaveChooser.setSelectedItem(octaves[4]);
		}
		catch(Exception e)
		{

		}

		// put combo box in the JPanel
		octavePanel.add(octaveChooser);

		return octavePanel;
	}

	/**
	 * Create a panel that holds all the properties that will be submitted (like
	 * row, column, etcetera).
	 * 
	 * @return a panel holding all the property components, like the row and
	 *         column text fields.
	 */
	private JPanel createPropertyPanel()
	{
		// create a panel that asks the user for the row and column position
		// where the music starts
		JPanel rowAndColumnPanel = createRowAndColumnPanel();

		// create the radio buttons for the choice of scale
		JPanel scalePanel = createScaleButtons();

		// create a menu to choose the instrument
		JPanel instrumentPanel = createInstrumentChoicePanel();

		// create a panel for choosing a vocal range (approximate)
		JPanel octavePanel = createOctavePanel();

		// create panel with a check box for muting the music
		JPanel mutePanel = createMuteCheckBoxPanel();

		// create a panel with the volume control
		JPanel volumePanel = createVolumeControlPanel();

		// create labels for each of the above
		JLabel rowColLabel = new JLabel("Position: ");
		JLabel octaveLabel = new JLabel("Range: ");
		JLabel instrumentLabel = new JLabel("Instrument: ");
		JLabel scaleLabel = new JLabel("Scale: ");
		JLabel volumeLabel = new JLabel("Volume: ");
		JLabel muteLabel = new JLabel("Mute: ");

		// put box in a panel
		JPanel comboPanel = new JPanel(new GridBagLayout());
		comboPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));

		// instrument chooser
		int row = 0;
		comboPanel.add(instrumentLabel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		comboPanel.add(instrumentPanel, new GBC(5, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		// octave chooser
		row++;
		comboPanel.add(octaveLabel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		comboPanel.add(octavePanel, new GBC(5, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		// scale chooser
		row++;
		comboPanel.add(scaleLabel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		comboPanel.add(scalePanel, new GBC(5, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		// row and col chooser
		row++;
		comboPanel.add(rowColLabel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		comboPanel.add(rowAndColumnPanel, new GBC(5, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// volume chooser
		row++;
		comboPanel.add(volumeLabel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		comboPanel.add(volumePanel, new GBC(5, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		// mute chooser
		row++;
		comboPanel.add(muteLabel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		comboPanel.add(mutePanel, new GBC(5, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

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
	 * Create radio buttons to decide upon a scale.
	 * 
	 * @return a panel containing the buttons.
	 */
	private JPanel createScaleButtons()
	{
		cMajorScaleButton = new JRadioButton("C major scale");
		cMajorScaleButton.setFont(fonts.getPlainFont());
		cMajorScaleButton.addItemListener(new ScaleChoiceListener());
		cMajorScaleButton.setSelected(false);

		cHarmonicMinorScaleButton = new JRadioButton("C harmonic minor scale");
		cHarmonicMinorScaleButton.setFont(fonts.getPlainFont());
		cHarmonicMinorScaleButton.addItemListener(new ScaleChoiceListener());
		cHarmonicMinorScaleButton.setSelected(false);

		cPentatonicScaleButton = new JRadioButton("C pentatonic scale");
		cPentatonicScaleButton.setFont(fonts.getPlainFont());
		cPentatonicScaleButton.addItemListener(new ScaleChoiceListener());
		cPentatonicScaleButton.setSelected(false);

		cBluesScaleButton = new JRadioButton("C blues scale");
		cBluesScaleButton.setFont(fonts.getPlainFont());
		cBluesScaleButton.addItemListener(new ScaleChoiceListener());
		cBluesScaleButton.setSelected(true);

		// put them in a group so that they behave as radio buttons
		ButtonGroup group = new ButtonGroup();
		group.add(cMajorScaleButton);
		group.add(cHarmonicMinorScaleButton);
		group.add(cPentatonicScaleButton);
		group.add(cBluesScaleButton);

		// the amount of vertical space to put between components
		int verticalSpace = 5;

		// create a box holding the buttons
		Box buttonBox = Box.createVerticalBox();
		buttonBox.add(cMajorScaleButton);
		buttonBox.add(Box.createVerticalStrut(verticalSpace));
		buttonBox.add(cHarmonicMinorScaleButton);
		buttonBox.add(Box.createVerticalStrut(verticalSpace));
		buttonBox.add(cPentatonicScaleButton);
		buttonBox.add(Box.createVerticalStrut(verticalSpace));
		buttonBox.add(cBluesScaleButton);

		// now add to a JPanel
		JPanel radioPanel = new JPanel();
		radioPanel.add(buttonBox);

		return radioPanel;
	}

	/**
	 * Creates a hash mapping between vocal ranges (as integers) and their
	 * names.
	 */
	private void createVocalRangeHash()
	{
		for(int i = 0; i < octaves.length; i++)
		{
			vocalRangeHash.put(octaves[i], new Integer(octaveValues[i]));
		}
	}

	/**
	 * Create a control for the volume.
	 * 
	 * @return A panel containing the volume control.
	 */
	private JPanel createVolumeControlPanel()
	{
		// the panel that will be returned
		JPanel volumePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		int maxValue = 10;
		int minValue = 0;

		volumeSlider = new JSlider(minValue, maxValue, maxValue / 2);
		volumeSlider.addChangeListener(new VolumeListener());
		volumeSlider.setToolTipText(VOLUME_TIP);

		// set tick marks and labels for the slider
		int majorTickSpacing = (int) Math.round(maxValue / 5);
		int minorTickSpacing = (int) Math.round(maxValue / 10);
		volumeSlider.setMajorTickSpacing(majorTickSpacing);
		volumeSlider.setMinorTickSpacing(minorTickSpacing);
		volumeSlider.setPaintTicks(true);
		volumeSlider.setPaintLabels(true);
		volumeSlider.setSnapToTicks(false);

		volumePanel.add(volumeSlider);

		return volumePanel;
	}

	/**
	 * Gets the music cells from the lattice (the cells are a subset).
	 * 
	 * @param lattice
	 *            The CA lattice containing the CA cells.
	 */
	private void loadMusicCells(Lattice lattice)
	{
		// the width of the lattice
		int width = CurrentProperties.getInstance().getNumColumns();

		// the lattice may be too small for the full scale
		if(width < numNotesInScale)
		{
			// can only do part of the scale
			musicCells = new Cell[width];
		}
		else
		{
			musicCells = new Cell[numNotesInScale];
		}

		// the last column played (highest note)
		int lastColumn = column + (musicCells.length - 1);

		// the selected column might also push it past the edge of the lattice
		if(lastColumn >= width)
		{
			// then too big, so redo
			musicCells = new Cell[width - column];
			lastColumn = width - 1;
		}

		// get an iterator over the lattice
		Iterator cellIterator = lattice.iterator();

		// Ignore cells until we get to the correct position.
		// Go to the correct row.
		for(int i = 0; i < row; i++)
		{
			// ignore every cell in the column
			for(int j = 0; j < width; j++)

			{
				// ignore these cells
				cellIterator.next();
			}
		}

		// now that we are at the correct row, ignore cells until we are at the
		// correct column position
		for(int i = 0; i < column; i++)
		{
			// ignore these cells
			cellIterator.next();
		}

		// now get these cells!
		for(int i = column; i <= lastColumn; i++)
		{
			musicCells[i - column] = (Cell) cellIterator.next();
		}
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
	 * Reads the column position where the music starts.
	 * 
	 * @return The column.
	 * @throws NumberFormatException
	 */
	private int getColumnPosition() throws NumberFormatException
	{
		// read the column number
		Integer colInteger = (Integer) ((SpinnerNumberModel) colSpinner
				.getModel()).getNumber();

		// subtract 1 so that they can enter values from 1 to width (rather than
		// 0 to width-1)
		int cols = colInteger.intValue() - 1;

		if(!isColumnPositionOk(cols))
		{
			throw new NumberFormatException();
		}

		return cols;
	}

	/**
	 * Reads the row position where the music starts.
	 * 
	 * @return The row.
	 * @throws NumberFormatException
	 */
	private int getRowPosition() throws NumberFormatException
	{
		// read the row number
		Integer rowInteger = (Integer) ((SpinnerNumberModel) rowSpinner
				.getModel()).getNumber();

		// subtract 1 so that they can enter values from 1 to width (rather than
		// 0 to width-1)
		int row = rowInteger.intValue() - 1;
		if(!isRowPositionOk(row))
		{
			throw new NumberFormatException();
		}

		return row;
	}

	/**
	 * Open a piano synthesizer to play the music.
	 */
	private void openPianoSynthesizer()
	{
		try
		{
			if(piano != null && !piano.isOpen())
			{
				piano.openSynthesizer();
			}
		}
		catch(javax.sound.midi.MidiUnavailableException e)
		{
			// the reason this was thrown -- used later to tell the user what
			// happened
			synthesizerErrorMessage = e.getMessage();

			noSynthesizer = true;
		}
	}

	/**
	 * Prints an error message onto the analysis screen. Use this when the user
	 * has input incorrect analysis values.
	 * 
	 * @param message
	 *            The message that will be printed.
	 */
	private void printUserErrorMessage(String message)
	{
		if(message != null)
		{
			errorMessageLabel.setText(message);
			if(message.length() > 0)
			{
				errorMessageLabel.setBackground(ERROR_COLOR);
				errorMessageLabel.setMargin(new Insets(8, 10, 8, 16));
			}
			else
			{
				errorMessageLabel.setBackground(displayPanel.getBackground());
				errorMessageLabel.setMargin(new Insets(0, 10, 0, 16));
			}
		}
	}

	/**
	 * Prints an informative message when the synthesizer has failed through no
	 * fault of the user.
	 */
	private void printSynthesizerErrorMessage()
	{
		if(!haveWarnedUserThereIsNoSynth)
		{
			haveWarnedUserThereIsNoSynth = true;

			// default error message
			String errorMessage = "Sorry, your computer's synthesizer could "
					+ "not be opened, contains \n"
					+ "no instruments, or does not exist.";

			// decide what message to display to the user
			if(synthesizerErrorMessage
					.equals(MusicalInstrument.NO_INSTRUMENTS_MESSAGE))
			{
				errorMessage = "Sorry, your computer's synthesizer contains \n"
						+ "no instruments.";
			}
			else if(synthesizerErrorMessage
					.equals(MusicalInstrument.NO_SOUND_BANK_MESSAGE))
			{
				errorMessage = "Sorry, your computer's default synthesizer contains no musical \n"
						+ "sounds (a \"Soundbank\").  You can install a free Soundbank by \n"
						+ "going to the URL below and following the instructions. \n\n"
						+ "http://java.sun.com/products/java-media/sound/soundbanks.html \n\n"
						+ "If the instructions tell you to navigate to the \"JavaSoft\" \n"
						+ "directory, and you do not have that directory, then just \n"
						+ "navigate to your \"Java\" installation directory.  You may \n"
						+ "need to install the soundbank in both directories.  If you \n"
						+ "have previously installed a soundbank and have automatic \n"
						+ "Java updates, then you will just need to copy the soundbank \n"
						+ "from your old version of the jre to your most recent version \n"
						+ "of the jre.";
			}

			JOptionPane.showMessageDialog(null, errorMessage, "No synthesizer",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Handles a submission for a new row and column and other properties.
	 */
	private void submitRowColChanges()
	{
		// width and height of the lattice
		int width = CurrentProperties.getInstance().getNumColumns();
		int height = CurrentProperties.getInstance().getNumRows();

		// is lattice one-dimensional?
		boolean isOneDimensional = OneDimensionalLattice
				.isCurrentLatticeOneDim();
		if(isOneDimensional)
		{
			// on one dimensional lattices, only let them specify the first row
			height = 1;
		}

		// if there is an error in the submitted properties, this message will
		// be printed
		String errorMessage = "";

		// no error so far (this allows the music to play)
		userError = false;

		// only play music if a good column was specified
		try
		{
			// read the number of cols
			this.column = getColumnPosition();
		}
		catch(Exception error)
		{
			// must send them a message
			if(errorMessage.equals(""))
			{
				errorMessage = "Cannot play the music.  ";
			}
			errorMessage += "The column must "
					+ "be a positive integer between 1 and " + width + ". ";

			// user made mistake
			userError = true;
		}
		// only play music if a good row was specified
		try
		{
			// read the number of rows
			this.row = getRowPosition();
		}
		catch(Exception error)
		{
			// must send them a message
			if(errorMessage.equals(""))
			{
				errorMessage = "Cannot play the music.  ";
			}
			if(height != 1)
			{
				errorMessage += "The starting row must "
						+ "be a positive integer between 1 and " + height
						+ ".  ";
			}
			else
			{
				errorMessage += "The starting row must be 1.  ";
			}

			// user made mistake
			userError = true;
		}

		// print error message (which may be "", which clears any previous
		// message)
		printUserErrorMessage(errorMessage);

		if(!userError)
		{
			// this forces a reload of the cells
			untagMusicCells();
			musicCells = null;

			// rerun the analysis so it grabs new cells
			rerunAnalysis();

			// and repaint so the new tagged cells are shown
			refreshGraphics();
		}
	}

	/**
	 * Tags each cell's state for special display.
	 */
	private void tagMusicCells()
	{
		if(musicCells != null)
		{
			for(int i = 0; i < musicCells.length; i++)
			{
				// may be null if the lattice was reset
				if(musicCells[i] != null)
				{
					// tag this cell for extra visibility
					musicCells[i].setTagged(true, this);
				}
			}
		}
	}

	/**
	 * Resets the tagged setting so that each cell's state is no longer tagged
	 * for special display.
	 */
	private void untagMusicCells()
	{
		if(musicCells != null)
		{
			for(int i = 0; i < musicCells.length; i++)
			{
				// may be null if the lattice was reset
				if(musicCells[i] != null)
				{
					musicCells[i].setTagged(false, this);
				}
			}
		}
	}

	/**
	 * Counts and displays the number of occupied cells.
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
		try
		{
			if(!userError && piano.isOpen() && !noSynthesizer)
			{
				// get a slice of the cells
				if(musicCells == null)
				{
					loadMusicCells(lattice);
				}

				// do this every time the analyze method is called because some
				// other rule or analysis may attempt to untag the cells
				tagMusicCells();

				// only do this the first time
				if(firstTimeThrough)
				{
					// tell the CA to show the tagged cells
					refreshGraphics();
				}

				// only true the first time the analyze method is called
				firstTimeThrough = false;

				if(play)
				{
					// play the notes
					for(int i = 0; i < musicCells.length; i++)
					{
						try
						{
							// get the note
							int note = convertCellNumberToNote(i);

							// is the note supposed to be played?
							if(musicCells[i].getState(generation).isFull())
							{
								// then play the corresponding note

								// only play the note if it isn't already being
								// played
								if(!piano.isNoteOn(note))
								{
									piano.noteOn(note, volume);
								}
							}
							else
							{
								// then stop playing the corresponding note
								piano.noteOff(note);
							}

							// also play a chord on appropriate beats
							// int[] chordNotes =
							// MusicalScales.getCBluesChord(generation);
							//
							// if(chordNotes != null)
							// {
							// for(int n = 0; n < chordNotes.length; n++)
							// {
							// piano.noteOn(chordNotes[n]);
							// }
							// }
						}
						catch(javax.sound.midi.MidiUnavailableException e)
						{
							// continue -- not fatal
						}
					}
				}
			}
			else if(noSynthesizer)
			{
				printSynthesizerErrorMessage();
			}
		}
		catch(Exception e)
		{
			// nothing mission critical will happen if this fails, so ignore it
		}
	}

	/**
	 * Reacts to the buttons.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();

		if(command.equals(MUTE_MUSIC))
		{
			if(muteCheckBox.isSelected())
			{
				// turn off all previous notes
				piano.allNotesOff();

				// don't play any more notes
				play = false;
			}
			else
			{
				play = true;
			}
		}
		else if(command.equals(CHANGE_INSTRUMENT))
		{
			// change the instrument
			String instrumentName = (String) instrumentChooser
					.getSelectedItem();
			piano.setInstrument(instrumentName);
		}
		else if(command.equals(CHANGE_OCTAVE))
		{
			// turn off all previous notes
			piano.allNotesOff();

			// change the vocal range (or octave) of the instrument
			vocalRange = ((Integer) vocalRangeHash.get(octaveChooser
					.getSelectedItem())).intValue();
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
	 * Performs any necessary operations to reset the analysis.
	 */
	public void reset()
	{
		// new width and height of the lattice
		int width = CurrentProperties.getInstance().getNumColumns();
		int height = CurrentProperties.getInstance().getNumRows();

		// reset the max values on the row and col spinners
		SpinnerNumberModel colModel = new SpinnerNumberModel(1, 1, width, 1);
		SpinnerNumberModel rowModel = new SpinnerNumberModel(1, 1, height, 1);
		colSpinner.setModel(colModel);
		rowSpinner.setModel(rowModel);

		// forces cells to be reloaded, which is necessary because we have a new
		// simulation
		untagMusicCells();
		musicCells = null;

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

		// and clear any errors
		userError = false;
		printUserErrorMessage("");

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
	 * Performs any desired operations when the analysis is stopped (closed) by
	 * the user. For example, you might write the results to a file at this
	 * time. Or you might dispose of any windows that you opened. May do
	 * nothing.
	 */
	protected void stopAnalysis()
	{
		untagMusicCells();
		piano.closeSynthesizer();

		// and refresh the graphics so don't show the cells as tagged anymore
		refreshGraphics();
	}

	/**
	 * Decides what to do when the user selects a scale.
	 * 
	 * @author David Bahr
	 */
	private class ScaleChoiceListener implements ItemListener
	{
		public void itemStateChanged(ItemEvent event)
		{
			// force it to get new cells
			untagMusicCells();
			musicCells = null;

			if(cMajorScaleButton.isSelected())
			{
				numNotesInScale = MusicalScales.getCMajorScaleOneOctaveLength();
			}
			else if(cHarmonicMinorScaleButton.isSelected())
			{
				numNotesInScale = MusicalScales
						.getCHarmonicMinorScaleOneOctaveLength();
			}
			else if(cPentatonicScaleButton.isSelected())
			{
				numNotesInScale = MusicalScales
						.getCPentatonicScaleOneOctaveLength();
			}
			else if(cBluesScaleButton.isSelected())
			{
				numNotesInScale = MusicalScales.getCBluesScaleOneOctaveLength();
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

	/**
	 * listens for changes to the volume slider.
	 */
	private class VolumeListener implements ChangeListener
	{

		/**
		 * Listens for changes to the volume.
		 * 
		 * @param e
		 */
		public void stateChanged(ChangeEvent e)
		{
			// get the volume in *arbitrary units provided by the slider*
			int vol = volumeSlider.getValue();

			// get the delay in *milliseconds*
			volume = convertSliderValueToVolume(vol);
		}

		/**
		 * The slider adjusts between 0 and 10. But the synthesizer uses 0 to
		 * 127.
		 * 
		 * @param volume
		 *            The arbitrary slider value.
		 * @return A volume between 0 and 127 that can be interpreted by the
		 *         synthesizer.
		 */
		private int convertSliderValueToVolume(int volume)
		{
			volume = 127 * (volume - volumeSlider.getMinimum())
					/ (volumeSlider.getMaximum() - volumeSlider.getMinimum());

			return volume;
		}
	}
}
