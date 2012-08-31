package userRules;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cellularAutomata.CAController;
import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.SquareLattice;
import cellularAutomata.rules.templates.IntegerRuleTemplate;
import cellularAutomata.rules.templates.MultiGenerationIntegerRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;

/**
 * 
 * A rule that creates a Turning Machine from a cellular automata.
 * 
 * Wikipedia description: The "Turing" machine was described by Alan Turing in
 * 1937, who called it an "a(utomatic)-machine". Turing machines are not
 * intended as a practical computing technology, but rather as a thought
 * experiment representing a computing machine. They help computer scientists
 * understand the limits of mechanical computation. Alan Turing gave a succinct
 * definition of the experiment in his 1948 essay, "Intelligent Machinery".
 * Referring to his 1936 publication, Turing wrote that the Turing machine, here
 * called a Logical Computing Machine, consisted of: ...an infinite memory
 * capacity obtained in the form of an infinite tape marked out into squares, on
 * each of which a symbol could be printed. At any moment there is one symbol in
 * the machine; it is called the scanned symbol. The machine can alter the
 * scanned symbol and its behavior is in part determined by that symbol, but the
 * symbols on the tape elsewhere do not affect the behavior of the machine.
 * However, the tape can be moved back and forth through the machine, this being
 * one of the elementary operations of the machine. Any symbol on the tape may
 * therefore eventually have an innings.
 * 
 * Rule description: The infinite tape is the lattice and the symbols are the
 * cell states. The cell state with the largest numerical value is the tape
 * head. The finite controller states are defined by the user. The scanned
 * symbol is the previous state of the cell currently containing the tape head.
 * 
 * The Turing Machine is controlled by the user via the finite state panels. The
 * finite state panels are each their own object containing their own data
 * structures. The panels are stored in an array and called as needed.
 * 
 * @author Kaleb Kircher
 */
public class TuringMachine extends IntegerRuleTemplate
{
	// boolean used to update the head only once per generation
	private static volatile boolean headMove = false;

	// boolean used to write a new state only once per generation
	private static volatile boolean tapeWrite = false;

	// boolean used to read a new state only once per generation
	private static volatile boolean stateRead = false;

	// a variable used to update the currentState only once per generation
	private static int counter = 0;

	// variable to keep track of the tape heads current State
	private static int currentState = 0;

	// the current generation being processed by the rule
	private static int currentGeneration = 0;

	// used to update the readState only once per generation
	private static int newReadState = 0;

	// number of available finite states
	private static int numFiniteStates = 20;

	// current number of states
	private static int numStates;

	// a variable used to update the currentState only once per generation
	private static int readState = 0;

	// the actionListener class for the TM
	private HeadListener hl = new HeadListener();

	// a button used to randomly set each state to the same read and write
	// options
	private static JButton unify;

	// combo box to select a preset program
	private static JComboBox selectProgram;

	// the JPanel that is returned by getAdditionalPropertiesPanel()
	private static JPanel panel = null;

	// an array used to store all of the StatePanels
	private static StatePanel[] statePanels = null;

	// a one line tooltip description for this rule
	private static final String TOOLTIP = "<html> <body><b>Turing Machine</b> Creates a Turing"
			+ "Machine algorithm that has user states that can be defined by the user. Alan Turing defines a Turing machines as "
			+ "an infinite memory capacity obtained in the form of an infinite tape marked out into squares, "
			+ "on each of which a symbol could be printed. "
			+ "At any moment there is one symbol in the machine; it is called the scanned symbol. "
			+ "The machine can alter the scanned symbol and its behavior is in part determined by that symbol, "
			+ "but the symbols on the tape elsewhere do not affect the behavior of the machine ."
			+ "However, the tape can be moved back and forth through the machine, this being one of the elementary operations of the machine. "
			+ "Any symbol on the tape may therefore eventually have an innings "
			+ " "
			+ "This Turing Machine is implemented as a Cellular Automata. The rule makes it behave as a Turing Machine. "
			+ "The lattice is the infinite tape and the states are the symbols. The highest state is the tape head. "
			+ "The read state is the previous state of the tape head. The tape head moves, writes a new state, and changes "
			+ "finite state based on the current read state and the current finite state. "
			+ "There are four preset programs: Counting, Subtraction, Busy Beaver #3, and Busy Beaver #4. "
			+ "Counting: "
			+ " Use three states. State 0 and 1 represent binary numbers. State 2 is a blank character. "
			+ " When the tape head encounters a blank character it will begin to count in binary. "
			+ "Subtraction:"
			+ "Use three states. State 0 and 1 represent binary numbers. Construct two binary numbers"
			+ "from 0's and 1's seperated by a blank chatacter. Start the tape head at the beginning of the first number."
			+ "It wil subtract the two numbers and the result will be on the left hand side of the equation. "
			+ "Busy Beaver:"
			+ "Used three states. Select a lattice of all blank characters. </body></html>";

	/**
	 * A display name for this class. (This is not normally public, but Rule102
	 * is used frequently in the CA Explorer, and this is a handy shortcut. When
	 * writing your own rule, I'd suggest making this variable private.)
	 */
	private static final String RULE_NAME = "Turing Machine";

	// a description of property choices that give the best results for this
	// rule (e.g., which lattice, how many states, etc.)
	private static final String BEST_RESULTS = "<html> <body><b>"
			+ RULE_NAME
			+ ".</b>"
			+ "<p> "
			+ "<b>For best results</b>, try a small lattice (around 50 by 50). If the unify "
			+ "button is pressed, the TM will create random cyclical patterns. If the"
			+ "unify button is pressed while the TM is running, the algorithm will change,"
			+ "and the TM will do new things. Each state can also be randomized. Finally, each "
			+ "state can be individually defined by the user. Programs can be written in this way."
			+ "Pre-written programs can be loaded by using the Select Program drop-down box. </body></html>";

	/**
	 * Create the rule corresponding to the number 102 (a la Wolfram) using the
	 * given cellular automaton properties.
	 * <p>
	 * When calling the parent constructor, the minimalOrLazyInitialization
	 * parameter must be included as shown. The boolean is intended to indicate
	 * when the constructor should build a rule with as small a footprint as
	 * possible. In order to load rules by reflection, the application must
	 * query this class for information like the display name, tooltip
	 * description, etc. At these times it makes no sense to build the complete
	 * rule which may have a large footprint in memory.
	 * <p>
	 * It is recommended that the constructor and instance variables do not
	 * initialize any memory intensive variables and that variables be
	 * initialized only when first needed (lazy initialization). Or all
	 * initializations in the constructor may be placed in an <code>if</code>
	 * statement.
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
	 *            small a footprint as possible. When false, the rule is fully
	 *            constructed. This variable should be passed to the super
	 *            constructor <code>super(minimalOrLazyInitialization);</code>,
	 *            but if uncertain, you may safely ignore this variable.
	 */
	public TuringMachine(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if (!minimalOrLazyInitialization)
		{
			CurrentProperties.getInstance().setNumStates(4);
			// reset the panel only if the number of states changed
			if (numStates != CurrentProperties.getInstance().getNumStates())
			{
				panel = null;
			}

			getAdditionalPropertiesPanel();

			// read a 0 by default
			currentState = 0;
			readState = 0;
			newReadState = 0;
		}
	}

	/**
	 * 
	 * Creates the panel for the MoreProperties tab that allows the user to
	 * manipulate the state definitions.
	 * 
	 * @return a JPanel allowing the user to manipulate state definitions
	 */
	public JPanel createConstantPanel()
	{
		int row = 0;
		// add the components to a JPanel
		JPanel constantPanel = new JPanel(new GridBagLayout());

		// add descriptive labels
		JPanel descriptionPanel = createDescriptionPanel();

		statePanels = new StatePanel[numFiniteStates];

		for (int i = 0; i < numFiniteStates; i++)
		{
			statePanels[i] = new StatePanel();
			statePanels[i].createStateDefinitionsPanel(hl);
		}

		// JButton for to unify the states
		unify = new JButton("Unify");
		unify.addActionListener(hl);

		String[] programNames =
		{ "Counting", "Subtraction", "Busy Beaver #1", "Busy Beaver #2" };

		selectProgram = new JComboBox(programNames);
		selectProgram.addActionListener(new ProgramListener());

		Font titleFont = new Fonts().getItalicSmallerFont();
		Color titleColor = Color.BLUE;

		Border outerEmptyBorder5 = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder5 = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Finite State Control",
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		JPanel allNoisePanel = new JPanel(new GridBagLayout());
		constantPanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder5, titledBorder5));

		constantPanel.add(descriptionPanel, new GBC(0, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(0));

		row++;
		constantPanel.add(new JLabel(" "), new GBC(0, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(5.0, 5.0).setAnchor(GBC.WEST)
				.setInsets(0));
		row++;
		constantPanel.add(new JLabel(
				"There are twenty available Finite States (FS):"), new GBC(0,
				row).setSpan(1, 1).setFill(GBC.EAST).setWeight(1.0, 1.0)
				.setAnchor(GBC.WEST).setInsets(1));

		row++;
		constantPanel.add(new JLabel(" "), new GBC(0, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(5.0, 5.0).setAnchor(GBC.WEST)
				.setInsets(0));
		row++;
		constantPanel.add(new JLabel(
				"Select a preset binary program (use 3 states):"), new GBC(0,
				row).setSpan(1, 1).setFill(GBC.EAST).setWeight(1.0, 1.0)
				.setAnchor(GBC.WEST).setInsets(1));

		row++;
		constantPanel.add(selectProgram, new GBC(0, row).setSpan(2, 1).setFill(
				GBC.EAST).setWeight(5.0, 5.0).setAnchor(GBC.WEST).setInsets(0));
		row++;
		constantPanel.add(new JLabel(" "), new GBC(0, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(5.0, 5.0).setAnchor(GBC.WEST)
				.setInsets(0));

		row++;
		constantPanel.add(new JLabel(
				"Click to unify all the states to a random program:"), new GBC(
				0, row).setSpan(1, 1).setFill(GBC.EAST).setWeight(1.0, 1.0)
				.setAnchor(GBC.WEST).setInsets(1));

		row++;
		constantPanel.add(unify, new GBC(0, row).setSpan(2, 1)
				.setFill(GBC.EAST).setWeight(5.0, 5.0).setAnchor(GBC.WEST)
				.setInsets(0));

		row++;
		constantPanel.add(new JLabel(" "), new GBC(0, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(5.0, 5.0).setAnchor(GBC.WEST)
				.setInsets(0));

		for (int i = 0; i < numFiniteStates; i++)
		{
			row++;
			constantPanel.add(new JLabel("State #" + i), new GBC(0, row)
					.setSpan(4, 1).setFill(GBC.BOTH).setWeight(5.0, 5.0)
					.setAnchor(GBC.WEST).setInsets(0));
			row++;
			constantPanel.add(statePanels[i].getStateDefinitionsPanel(),
					new GBC(0, row).setSpan(4, 1).setFill(GBC.WEST).setWeight(
							1.0, 1.0).setAnchor(GBC.WEST)
							.setInsets(0, 0, 10, 0));

			row++;
			constantPanel.add(new JLabel(" "), new GBC(0, row).setSpan(4, 1)
					.setFill(GBC.BOTH).setWeight(5.0, 5.0).setAnchor(GBC.WEST)
					.setInsets(0));

		}

		// set the default program to counting
		selectProgram.setSelectedIndex(0);

		return constantPanel;
	}

	/**
	 * Creates a panel that displays a message about the More Properties panel.
	 * 
	 * @return A panel containing messages.
	 */
	private JPanel createDescriptionPanel()
	{
		// a "grab their attention" panel
		AttentionPanel attentionPanel = new AttentionPanel("Turing Machine");

		String functionDescription = "These controls allow you to define the finite states "
				+ "of the Turing Machine. For each state, you can define what the head "
				+ "writes and where the tape head moves next based on what the tape head is "
				+ "currently reading.";

		MultilineLabel messageLabel = new MultilineLabel(functionDescription);

		messageLabel.setMargin(new Insets(2, 6, 2, 2));

		JPanel messagePanel = new JPanel(new GridBagLayout());
		int row = 0;
		messagePanel.add(attentionPanel, new GBC(0, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(0));

		row++;
		messagePanel.add(messageLabel, new GBC(0, row).setSpan(4, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(0,
				0, 1, 0));

		return messagePanel;
	}

	/**
	 * Finds the tape head, the current state, and the read state. Based on
	 * these inputs, a new state is written and the head moves in a new
	 * direction.
	 * 
	 * @param cell
	 *            The value of the cell being updated.
	 * @param neighbors
	 *            The value of the neighbors.
	 * @param generation
	 *            The current generation of the CA.
	 * 
	 * @return A new state for the cell.
	 */
	@Override
	protected int integerRule(int cell, int[] neighbors, int numStates,
			int generation)
	{
		// define the tape head to be the state with largest numerical value
		int tapeHead = numStates - 1;

		// this value will be returned
		// set it to the current cell value as a default
		int newCell = cell;

		// make sure the head moves, writes, and updates finite state
		// only once per generation
		if (currentGeneration != generation)
		{
			currentGeneration = generation;

			// indicate the head has not been moved on this generation
			headMove = false;
			// indicate the head has not written on this generation
			tapeWrite = false;
			// indicate that the head has not read on this generation
			stateRead = false;

			// update the tape heads readState to the current generation
			// do this only once per generation
			readState = newReadState;
		}

		// update the currentState only *AFTER* the tape head has written and
		// moved
		// do this only once per generation
		if (!stateRead && tapeWrite && headMove)
		{
			stateRead = true;

			// System.out.println("read " + readState + " " + generation);
			currentState = statePanels[currentState].finiteStateDirections[readState];
			// System.out.println("state " + currentState + " " + generation);
		}

		// call the move head method only if program isn't halted
		if (statePanels[currentState].haltState[readState].getSelectedIndex() != 1)
		{
			// move the tape head
			newCell = moveHead(cell, neighbors);
		}

		// write the tape head only if the cell isn't the new tape head
		if (newCell == 99 && !tapeWrite)
		{
			newCell = writeTape(cell, neighbors);
		}
		// if the tape has already been written and the cell isn't the tape head
		// return the current cell value
		if (newCell == 99 && tapeWrite)
		{
			return cell;
		}

		return newCell;
	}

	/**
	 * 
	 * Moves the tape head in the direction defined by the current and read
	 * states. Finds the tape heads neighbor that is supposed to become the tape
	 * head on the next generation based on the current readState and
	 * finiteState.
	 * 
	 * @param cellValues
	 *            The value of the cell being updated.
	 * @param neighbors
	 *            The value of the neighbors.
	 * @return A new state for the cell.
	 */
	protected int moveHead(int cell, int[] neighbors)
	{

		int state = cell;

		// default state used to call writeTape if the cell is not currently the
		// tape head
		// don't call writeTape if the tape has already been written
		if (!tapeWrite)
		{
			state = 99;
		}

		// the state of the tape head
		final int tapeHead = numStates - 1;

		// only if the head hasn't been moved
		if (!headMove)
		{
			// is the current cell the correct neighbor to be the new tape head?
			if (neighbors[statePanels[currentState].moveHeadDirections[readState]] == tapeHead)
			{
				// System.out.println(currentState);

				// define the new read state
				// it is the previous state of the tape head
				// but we declare it before it becomes the tape head
				// readState will be set to newReadState on the next generation
				newReadState = cell;

				// indicate the tape head has been moved on this generation
				headMove = true;
				return state = tapeHead;
			}
		}

		return state;
	}

	/**
	 * 
	 * Writes the state for the tape head. Finds the cell that is currently the
	 * tape head. The cells state on the next generation depends on the current
	 * readState and finiteState.
	 * 
	 * @param cellValues
	 *            The value of the cell being updated.
	 * @param neighbors
	 *            The value of the neighbors.
	 * @return A new state for the cell.
	 */
	protected int writeTape(int cell, int[] neighbors)
	{
		// default state if the cell is not the tape head and is not
		// being written to by the tape head
		int state = cell;

		// default state of the tape head
		final int tapeHead = numStates - 1;

		// if the cell is currently the tape head
		// change it to the writeState for the current readState and finiteState
		if (cell == tapeHead)
		{
			// indicate the tape has been written on this generation
			tapeWrite = true;
			return state = statePanels[currentState].stateWriterDirections[readState];
		}

		return state;
	}

	/**
	 * Gets a JPanel that may request specific input information that the rule
	 * needs to operate correctly. Should be overridden by child classes that
	 * desire to input any specific information. <br>
	 * Note that if returns null, then the panel is not displayed by the current
	 * version of the CA ControlPanel class. This null behavior is the default.
	 * 
	 * @return A JPanel requesting specific input information that the rule
	 *         needs to operate correctly. May be null.
	 */
	public JPanel getAdditionalPropertiesPanel()
	{
		// only recreate the panel if necessary
		if (panel == null)
		{
			// set by the user
			numStates = CurrentProperties.getInstance().getNumStates();
			panel = createConstantPanel();
			panel.repaint();
		}

		return panel;
	}

	/**
	 * A brief description (written in HTML) that describes what parameters will
	 * give best results for this rule (which lattice, how many states, etc).
	 * The description will be displayed on the properties panel. Using html
	 * permits line breaks, font colors, etcetera, as described in HTML
	 * resources. Regular line breaks will not work.
	 * <p>
	 * Recommend starting with the title of the rule followed by "For best
	 * results, ...". See Rule 102 for an example.
	 * 
	 * @return An HTML string describing how to get best results from this rule.
	 *         May be null.
	 */
	public String getBestResultsDescription()
	{
		return BEST_RESULTS;
	}

	/**
	 * A list of lattices with which this Rule will work. This rule won't work
	 * with lattices that have a variable or unknown number of neighbors.
	 * 
	 * @return A list of lattices compatible with this Rule (returns the display
	 *         names for the lattices). Returns null if compatible with all
	 *         lattices.
	 */
	public String[] getCompatibleLattices()
	{
		String[] lattices =
		{ SquareLattice.DISPLAY_NAME };

		return lattices;
	}

	/**
	 * When displayed for selection, the rule will be listed under specific
	 * folders specified here. The rule will always be listed under the "All
	 * rules" folder. And if the rule is contributed by a user and is placed in
	 * the userRules folder, then it will also be shown in a folder called "User
	 * rules". Any strings may be used; if the folder does not exist, then one
	 * will be created with the specified name. If the folder already exists,
	 * then that folder will be used.
	 * <p>
	 * By default, this returns null so that the rule is only placed in the
	 * default folder(s).
	 * <p>
	 * Child classes should override this method if they want the rule to appear
	 * in a specific folder. The "All rules" and "User rules" folder are
	 * automatic and do not need to be specified; they are always added.
	 * 
	 * @return A list of the folders in which rule will be displayed for
	 *         selection. May be null.
	 */
	public String[] getDisplayFolderNames()
	{
		String[] folders =
		{ RuleFolderNames.INSTRUCTIONAL_FOLDER, RuleFolderNames.PRETTY_FOLDER };

		return folders;
	}

	/**
	 * A brief one or two-word string describing the rule, appropriate for
	 * display in a drop-down list.
	 * 
	 * @return A string no longer than 15 characters.
	 */
	public String getDisplayName()
	{
		return RULE_NAME;
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

	// inner class used to create state definition panel objects
	// each object contains its own arrays to store the state definitions
	// for the writeHead and moveHead methods
	private static class StatePanel
	{
		// the variables to store the head states
		private int[] moveHeadDirections = new int[numStates - 1];

		// the write variable for state writer in state 0
		private int[] stateWriterDirections = new int[numStates - 1];

		private int[] finiteStateDirections = new int[numStates - 1];

		private JButton random = new JButton("Randomize");

		// array to store the moveHead comboBoxes
		private JComboBox[] moveHeads = null;

		// array to store the writerHead comboBoxes
		private JComboBox[] writerHeads = null;

		// array to store the finiteState comboBoxes
		private JComboBox[] finiteState = null;

		// array to store the haltState comboBoxes
		private JComboBox[] haltState = null;

		// The label for the moveHead ComboBoxes
		private JLabel moveLabel = null;

		// The label for the readHead ComboBoxes
		private JLabel readLabel = null;

		// The label for the finiteState comboBoxes
		private JLabel finiteStateLabel = null;

		// The labels for the states
		private JLabel[] states;

		// The labels for the halting state
		private JLabel haltingLabel;

		// The label for the state the user is modifying
		private JLabel stateLabel = null;

		// The label for the writeHead ComboBoxes
		private JLabel writeLabel = null;

		private JPanel stateDefinitionsPanel;

		// lists states for jcombo box
		private String[] stateStrings;

		private String[] finiteStrings;

		private Boolean[] haltingBools =
		{ false, true };

		// lists states for jcombo box
		private String[] directionStrings =
		{ "SE", "S", "SW", "W", "NW", "N", "NE", "E" };

		public JPanel getStateDefinitionsPanel()
		{
			return stateDefinitionsPanel;
		}

		/**
		 * Creates state definition panels.
		 * 
		 * @return A panel holding the state definition panels.
		 */
		private JPanel createStateDefinitionsPanel(HeadListener hl)
		{
			// add a label for the moveHead comboBoxes
			moveLabel = new JLabel("Move:");

			// add a label for the current read states
			readLabel = new JLabel("Read:");

			// add a label for the state panel
			stateLabel = new JLabel("State:");

			finiteStateLabel = new JLabel("FS:");

			haltingLabel = new JLabel("Halt:");

			stateStrings = new String[numStates - 1];

			finiteStrings = new String[numFiniteStates];

			moveHeads = new JComboBox[numStates - 1];
			writerHeads = new JComboBox[numStates - 1];
			finiteState = new JComboBox[numStates - 1];
			haltState = new JComboBox[numStates - 1];

			states = new JLabel[numStates - 1];

			for (int i = 0; i < numStates - 1; i++)
			{
				// state labels for each state definitions panel
				states[i] = new JLabel("#" + i);
				stateStrings[i] = ("#" + i);

			}

			for (int i = 0; i < numFiniteStates; i++)
			{
				finiteStrings[i] = ("#" + i);
			}

			// add a label for the slope
			writeLabel = new JLabel("Write:");

			// loop to add the writerHead comboBoxes to the state definitions
			// panel
			for (int i = 0; i < writerHeads.length; i++)
			{
				// Create the comboBox
				writerHeads[i] = new JComboBox(stateStrings);

				// Add actionListener
				writerHeads[i].addActionListener(hl);
			}

			// loop to add the moveHead comboBoxes to the state definitions
			// panel
			for (int i = 0; i < moveHeads.length; i++)
			{
				// Create the combo box
				moveHeads[i] = new JComboBox(directionStrings);

				// Add actionListner
				moveHeads[i].addActionListener(hl);
			}

			// loop to add the moveHead comboBoxes to the state definitions
			// panel
			for (int i = 0; i < finiteState.length; i++)
			{
				// Create the combo box
				finiteState[i] = new JComboBox(finiteStrings);

				// Add actionListner
				finiteState[i].addActionListener(hl);
			}

			// loop to add the moveHead comboBoxes to the state definitions
			// panel
			for (int i = 0; i < haltState.length; i++)
			{
				// Create the combo box
				haltState[i] = new JComboBox(haltingBools);
			}

			// Add an actionListener to the random JButton
			random.addActionListener(hl);

			// JPanel containing all of the comboBoxes
			stateDefinitionsPanel = new JPanel(new GridBagLayout());

			Font titleFont = new Fonts().getItalicSmallerFont();
			Color titleColor = Color.BLUE;

			// create panel for the noise
			Border outerEmptyBorder4 = BorderFactory.createEmptyBorder(0, 3, 2,
					3);
			Border titledBorder4 = BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(), "State Control",
					TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION,
					titleFont, titleColor);
			JPanel noisePanel = new JPanel(new GridBagLayout());
			stateDefinitionsPanel.setBorder(BorderFactory.createCompoundBorder(
					outerEmptyBorder4, titledBorder4));

			int row = 0;

			stateDefinitionsPanel.add(readLabel, new GBC(0, row).setSpan(1, 1)
					.setFill(GBC.NONE).setWeight(0.2, 1.0).setAnchor(GBC.EAST)
					.setInsets(0));

			stateDefinitionsPanel.add(writeLabel, new GBC(1, row).setSpan(1, 1)
					.setFill(GBC.NONE).setWeight(0.2, 1.0).setAnchor(GBC.EAST)
					.setInsets(0));

			stateDefinitionsPanel.add(moveLabel, new GBC(2, row).setSpan(1, 1)
					.setFill(GBC.NONE).setWeight(0.2, 1.0).setAnchor(GBC.EAST)
					.setInsets(0));

			stateDefinitionsPanel.add(finiteStateLabel, new GBC(3, row)
					.setSpan(1, 1).setFill(GBC.NONE).setWeight(0.2, 1.0)
					.setAnchor(GBC.EAST).setInsets(0));

			stateDefinitionsPanel.add(haltingLabel, new GBC(4, row).setSpan(1,
					1).setFill(GBC.NONE).setWeight(0.2, 1.0)
					.setAnchor(GBC.EAST).setInsets(0));

			for (int i = 0; i < numStates - 1; i++)
			{
				row++;
				stateDefinitionsPanel.add(states[i], new GBC(0, row).setSpan(1,
						1).setFill(GBC.NONE).setWeight(0.2, 1.0).setAnchor(
						GBC.EAST).setInsets(1));

				stateDefinitionsPanel.add(writerHeads[i], new GBC(1, row)
						.setSpan(1, 1).setFill(GBC.NONE).setWeight(0.2, 1.0)
						.setAnchor(GBC.EAST).setInsets(1));

				stateDefinitionsPanel.add(moveHeads[i], new GBC(2, row)
						.setSpan(1, 1).setFill(GBC.NONE).setWeight(0.2, 1.0)
						.setAnchor(GBC.EAST).setInsets(1));

				stateDefinitionsPanel.add(finiteState[i], new GBC(3, row)
						.setSpan(1, 1).setFill(GBC.NONE).setWeight(0.2, 1.0)
						.setAnchor(GBC.EAST).setInsets(1));

				stateDefinitionsPanel.add(haltState[i], new GBC(4, row)
						.setSpan(1, 1).setFill(GBC.NONE).setWeight(0.2, 1.0)
						.setAnchor(GBC.EAST).setInsets(1));
			}

			row++;
			stateDefinitionsPanel.add(random, new GBC(0, row).setSpan(5, 1)
					.setFill(GBC.NONE).setWeight(1.0, 1.0)
					.setAnchor(GBC.CENTER).setInsets(0));

			return stateDefinitionsPanel;
		}
	}

	/**
	 * Method to load a counting program.
	 */
	public static void countingProgram()
	{
		for (int i = 0; i < statePanels.length; i++)
		{
			for (int j = 0; j < statePanels[i].haltState.length; j++)
			{
				statePanels[i].haltState[j].setSelectedIndex(0);
			}

			// finite state conditions for writerHeads
			for (int j = 0; j < statePanels[i].writerHeads.length; j++)
			{
				// finite state panel 0
				if (i == 0)
				{
					statePanels[i].writerHeads[j].setSelectedIndex(j);
				}

				// finite state panel 1
				if (i == 1)
				{
					if (j == 0 || j == 2)
					{
						statePanels[i].writerHeads[j].setSelectedIndex(1);
					}
					if (j == 1)
					{
						statePanels[i].writerHeads[j].setSelectedIndex(0);
					}
				}
			}

			// finite state conditions for moveHeads
			for (int j = 0; j < statePanels[i].moveHeads.length; j++)
			{
				// finite state panel 0
				if (i == 0)
				{
					if (j == 0 || j == 1)
					{
						statePanels[i].moveHeads[j].setSelectedIndex(7);
					}
					if (j == 2)
					{
						statePanels[i].moveHeads[j].setSelectedIndex(3);
					}
				}

				// finite state panel 1
				if (i == 1)
				{
					if (j == 0 || j == 2)
					{
						statePanels[i].moveHeads[j].setSelectedIndex(7);
					}
					if (j == 1)
					{
						statePanels[i].moveHeads[j].setSelectedIndex(3);
					}
				}
			}

			// finite state conditions for finite states
			for (int j = 0; j < statePanels[i].finiteState.length; j++)
			{
				// state panel 0
				if (i == 0)
				{
					if (j == 0 || j == 1)
					{
						statePanels[i].finiteState[j].setSelectedIndex(0);
					}
					if (j == 2)
					{
						statePanels[i].finiteState[j].setSelectedIndex(1);
					}
				}

				// state panel 1
				if (i == 1)
				{
					if (j == 0 || j == 2)
					{
						statePanels[i].finiteState[j].setSelectedIndex(0);
					}
					if (j == 1)
					{
						statePanels[i].finiteState[j].setSelectedIndex(1);
					}
				}
			}
		}
	}

	/**
	 * Method to load a subtraction program.
	 */
	public static void subtractionProgram()
	{
		for (int i = 0; i < statePanels.length; i++)
		{
			for (int j = 0; j < statePanels[i].haltState.length; j++)
			{
				statePanels[i].haltState[j].setSelectedIndex(0);
			}

			// finite state conditions for writerHeads
			for (int j = 0; j < statePanels[i].writerHeads.length; j++)
			{
				// finite state panel 0
				if (i == 0)
				{
					statePanels[i].writerHeads[j].setSelectedIndex(j);
				}

				// finite state panel 1
				if (i == 1)
				{
					statePanels[i].writerHeads[j].setSelectedIndex(j);
				}

				// finite state panel 2
				if (i == 2)
				{
					if (j == 0 || j == 1)
					{
						statePanels[i].writerHeads[j].setSelectedIndex(0);
					}
					if (j == 2)
					{
						statePanels[i].writerHeads[j].setSelectedIndex(2);
					}
				}

				// finite state panel 3
				if (i == 3)
				{
					statePanels[i].writerHeads[j].setSelectedIndex(j);
				}

				// finite state panel 4
				if (i == 4)
				{
					statePanels[i].writerHeads[j].setSelectedIndex(j);
				}

				// finite state panel 5
				if (i == 5)
				{
					statePanels[i].writerHeads[j].setSelectedIndex(j);
				}

				// finite state panel 6
				if (i == 6)
				{
					if (j == 0 || j == 2)
					{
						statePanels[i].writerHeads[j].setSelectedIndex(2);
					}
					if (j == 1)
					{
						statePanels[i].writerHeads[j].setSelectedIndex(1);
					}
				}

				// finite state panel 7
				if (i == 7)
				{
					statePanels[i].writerHeads[j].setSelectedIndex(j);
				}

				// finite state panel 8
				if (i == 8)
				{
					if (j == 0 || j == 1)
					{
						statePanels[i].writerHeads[j].setSelectedIndex(0);
					}
					if (j == 2)
					{
						statePanels[i].writerHeads[j].setSelectedIndex(2);
					}
				}

				// finite state panel 9
				if (i == 9)
				{
					if (j == 0 || j == 2)
					{
						statePanels[i].writerHeads[j].setSelectedIndex(0);
					}
					if (j == 1)
					{
						statePanels[i].writerHeads[j].setSelectedIndex(1);
					}
				}
			}

			// finite state conditions for moveHeads
			for (int j = 0; j < statePanels[i].moveHeads.length; j++)
			{
				// finite state panel 0
				if (i == 0)
				{
					statePanels[i].moveHeads[j].setSelectedIndex(7);

				}

				// finite state panel 1
				if (i == 1)
				{
					if (j == 0 || j == 1)
					{
						statePanels[i].moveHeads[j].setSelectedIndex(7);
					}
					if (j == 2)
					{
						statePanels[i].moveHeads[j].setSelectedIndex(3);
					}
				}

				// finite state panel 2
				if (i == 2)
				{
					if (j == 0 || j == 1)
					{
						statePanels[i].moveHeads[j].setSelectedIndex(3);
					}
					if (j == 2)
					{
						statePanels[i].moveHeads[j].setSelectedIndex(7);
					}
				}

				// finite state panel 3
				if (i == 3)
				{
					statePanels[i].moveHeads[j].setSelectedIndex(3);
				}

				// finite state panel 4
				if (i == 4)
				{
					statePanels[i].moveHeads[j].setSelectedIndex(7);
				}

				// finite state panel 5
				if (i == 5)
				{
					if (j == 0 || j == 1)
					{
						statePanels[i].moveHeads[j].setSelectedIndex(7);
					}
					if (j == 2)
					{
						statePanels[i].moveHeads[j].setSelectedIndex(3);
					}
				}

				// finite state panel 6
				if (i == 6)
				{
					statePanels[i].moveHeads[j].setSelectedIndex(3);
				}

				// finite state panel 2
				if (i == 7)
				{
					if (j == 0 || j == 1)
					{
						statePanels[i].moveHeads[j].setSelectedIndex(3);
					}
					if (j == 2)
					{
						statePanels[i].moveHeads[j].setSelectedIndex(7);
					}
				}

				// finite state panel 8
				if (i == 8)
				{
					if (j == 1 || j == 2)
					{
						statePanels[i].moveHeads[j].setSelectedIndex(7);
					}
					if (j == 0)
					{
						statePanels[i].moveHeads[j].setSelectedIndex(3);
					}
				}

				// finite state panel 9
				if (i == 9)
				{
					statePanels[i].moveHeads[j].setSelectedIndex(7);
					statePanels[i].haltState[j].setSelectedIndex(1);
				}

			}

			// finite state conditions for finite states
			for (int j = 0; j < statePanels[i].finiteState.length; j++)
			{
				// state panel 0
				if (i == 0)
				{
					if (j == 0 || j == 1)
					{
						statePanels[i].finiteState[j].setSelectedIndex(0);
					}
					if (j == 2)
					{
						statePanels[i].finiteState[j].setSelectedIndex(1);
					}
				}

				// state panel 1
				if (i == 1)
				{
					if (j == 0 || j == 1)
					{
						statePanels[i].finiteState[j].setSelectedIndex(1);
					}
					if (j == 2)
					{
						statePanels[i].finiteState[j].setSelectedIndex(2);
					}
				}

				// state panel 2
				if (i == 2)
				{
					if (j == 0)
					{
						statePanels[i].finiteState[j].setSelectedIndex(2);
					}
					if (j == 1)
					{
						statePanels[i].finiteState[j].setSelectedIndex(3);
					}
					if (j == 2)
					{
						statePanels[i].finiteState[j].setSelectedIndex(5);
					}
				}

				// state panel 3
				if (i == 3)
				{
					if (j == 0)
					{
						statePanels[i].finiteState[j].setSelectedIndex(3);
					}
					if (j == 1)
					{
						statePanels[i].finiteState[j].setSelectedIndex(3);
					}
					if (j == 2)
					{
						statePanels[i].finiteState[j].setSelectedIndex(8);
					}
				}

				// state panel 4
				if (i == 4)
				{
					if (j == 0)
					{
						statePanels[i].finiteState[j].setSelectedIndex(4);
					}
					if (j == 1)
					{
						statePanels[i].finiteState[j].setSelectedIndex(4);
					}
					if (j == 2)
					{
						statePanels[i].finiteState[j].setSelectedIndex(5);
					}
				}

				// state panel 5
				if (i == 5)
				{
					if (j == 0)
					{
						statePanels[i].finiteState[j].setSelectedIndex(5);
					}
					if (j == 1)
					{
						statePanels[i].finiteState[j].setSelectedIndex(5);
					}
					if (j == 2)
					{
						statePanels[i].finiteState[j].setSelectedIndex(6);
					}
				}

				// state panel 6
				if (i == 6)
				{
					if (j == 0)
					{
						statePanels[i].finiteState[j].setSelectedIndex(6);
					}
					if (j == 1)
					{
						statePanels[i].finiteState[j].setSelectedIndex(6);
					}
					if (j == 2)
					{
						statePanels[i].finiteState[j].setSelectedIndex(7);
					}
				}

				// state panel 7
				if (i == 7)
				{
					if (j == 0)
					{
						statePanels[i].finiteState[j].setSelectedIndex(7);
					}
					if (j == 1)
					{
						statePanels[i].finiteState[j].setSelectedIndex(7);
					}
					if (j == 2)
					{
						statePanels[i].finiteState[j].setSelectedIndex(9);
					}
				}

				// state panel 8
				if (i == 8)
				{
					if (j == 0)
					{
						statePanels[i].finiteState[j].setSelectedIndex(8);
					}
					if (j == 1)
					{
						statePanels[i].finiteState[j].setSelectedIndex(0);
					}
					if (j == 2)
					{
						statePanels[i].finiteState[j].setSelectedIndex(4);
					}
				}

				// state panel 9
				if (i == 9)
				{
					if (j == 0)
					{
						statePanels[i].finiteState[j].setSelectedIndex(9);
					}
					if (j == 1)
					{
						statePanels[i].finiteState[j].setSelectedIndex(9);
					}
					if (j == 2)
					{
						statePanels[i].finiteState[j].setSelectedIndex(9);
					}
				}
			}
		}
	}

	/**
	 * Method to load a three state halting program.
	 */
	public static void haltingProgramThreeState()
	{
		for (int i = 0; i < statePanels.length; i++)
		{
			for (int j = 0; j < statePanels[i].haltState.length; j++)
			{
				statePanels[i].haltState[j].setSelectedIndex(0);
			}

			// finite state conditions for writerHeads
			for (int j = 0; j < statePanels[i].writerHeads.length; j++)
			{
				// finite state panel 0
				if (i == 0)
				{
					statePanels[i].writerHeads[j].setSelectedIndex(1);
				}

				// finite state panel 1
				if (i == 1)
				{
					if (j == 0 || j == 2)
					{
						statePanels[i].writerHeads[j].setSelectedIndex(0);
					}
					if (j == 1)
					{
						statePanels[i].writerHeads[j].setSelectedIndex(1);
					}
				}

				// finite state panel 2
				if (i == 2)
				{
					statePanels[i].writerHeads[j].setSelectedIndex(1);
				}
			}

			// finite state conditions for moveHeads
			for (int j = 0; j < statePanels[i].moveHeads.length; j++)
			{
				// finite state panel 0
				if (i == 0)
				{
					if (j == 0 || j == 2)
					{
						statePanels[i].moveHeads[j].setSelectedIndex(7);
					}
					if (j == 1)
					{
						statePanels[i].moveHeads[j].setSelectedIndex(7);
						statePanels[i].haltState[j].setSelectedIndex(1);
					}

				}

				// finite state panel 1
				if (i == 1)
				{
					statePanels[i].moveHeads[j].setSelectedIndex(7);
				}

				// finite state panel 2
				if (i == 2)
				{
					statePanels[i].moveHeads[j].setSelectedIndex(3);
				}
			}

			// finite state conditions for finite states
			for (int j = 0; j < statePanels[i].finiteState.length; j++)
			{
				// state panel 0
				if (i == 0)
				{
					if (j == 0 || j == 2)
					{
						statePanels[i].finiteState[j].setSelectedIndex(1);
					}
					if (j == 1)
					{
						statePanels[i].finiteState[j].setSelectedIndex(0);
					}
				}

				// state panel 1
				if (i == 1)
				{
					if (j == 0 || j == 2)
					{
						statePanels[i].finiteState[j].setSelectedIndex(2);
					}
					if (j == 1)
					{
						statePanels[i].finiteState[j].setSelectedIndex(1);
					}
				}

				// state panel 2
				if (i == 2)
				{
					if (j == 0)
					{
						statePanels[i].finiteState[j].setSelectedIndex(2);
					}
					if (j == 1)
					{
						statePanels[i].finiteState[j].setSelectedIndex(0);
					}
					if (j == 2)
					{
						statePanels[i].finiteState[j].setSelectedIndex(2);
					}
				}
			}
		}
	}

	/**
	 * Method to load a four state haling program.
	 */
	public static void haltingProgramFourState()
	{
		for (int i = 0; i < statePanels.length; i++)
		{
			for (int j = 0; j < statePanels[i].haltState.length; j++)
			{
				statePanels[i].haltState[j].setSelectedIndex(0);
			}

			// finite state conditions for writerHeads
			for (int j = 0; j < statePanels[i].writerHeads.length; j++)
			{
				// finite state panel 0
				if (i == 0)
				{
					statePanels[i].writerHeads[j].setSelectedIndex(1);
				}

				// finite state panel 1
				if (i == 1)
				{
					if (j == 0 || j == 2)
					{
						statePanels[i].writerHeads[j].setSelectedIndex(1);
					}
					if (j == 1)
					{
						statePanels[i].writerHeads[j].setSelectedIndex(0);
					}
				}

				// finite state panel 2
				if (i == 2)
				{
					statePanels[i].writerHeads[j].setSelectedIndex(1);
				}

				// finite state panel 3
				if (i == 3)
				{
					if (j == 0 || j == 2)
					{
						statePanels[i].writerHeads[j].setSelectedIndex(1);
					}
					if (j == 1)
					{
						statePanels[i].writerHeads[j].setSelectedIndex(0);
					}
				}
			}

			// finite state conditions for moveHeads
			for (int j = 0; j < statePanels[i].moveHeads.length; j++)
			{
				// finite state panel 0
				if (i == 0)
				{
					if (j == 0 || j == 2)
					{
						statePanels[i].moveHeads[j].setSelectedIndex(7);
					}
					if (j == 1)
					{
						statePanels[i].moveHeads[j].setSelectedIndex(3);
					}

				}

				// finite state panel 1
				if (i == 1)
				{
					statePanels[i].moveHeads[j].setSelectedIndex(3);
				}

				// finite state panel 2
				if (i == 2)
				{
					statePanels[i].moveHeads[j].setSelectedIndex(3);
					statePanels[i].haltState[0].setSelectedIndex(1);
					statePanels[i].haltState[2].setSelectedIndex(1);
				}

				// finite state panel 2
				if (i == 3)
				{
					statePanels[i].moveHeads[j].setSelectedIndex(7);
				}
			}

			// finite state conditions for finite states
			for (int j = 0; j < statePanels[i].finiteState.length; j++)
			{
				// state panel 0
				if (i == 0)
				{
					statePanels[i].finiteState[j].setSelectedIndex(1);
				}

				// state panel 1
				if (i == 1)
				{
					if (j == 0 || j == 2)
					{
						statePanels[i].finiteState[j].setSelectedIndex(0);
					}
					if (j == 1)
					{
						statePanels[i].finiteState[j].setSelectedIndex(2);
					}
				}

				// state panel 2
				if (i == 2)
				{
					if (j == 0)
					{
						statePanels[i].finiteState[j].setSelectedIndex(2);
					}
					if (j == 1)
					{
						statePanels[i].finiteState[j].setSelectedIndex(3);
					}
					if (j == 2)
					{
						statePanels[i].finiteState[j].setSelectedIndex(2);
					}
				}

				// state panel 3
				if (i == 3)
				{
					if (j == 0)
					{
						statePanels[i].finiteState[j].setSelectedIndex(3);
					}
					if (j == 1)
					{
						statePanels[i].finiteState[j].setSelectedIndex(0);
					}
					if (j == 2)
					{
						statePanels[i].finiteState[j].setSelectedIndex(3);
					}
				}
			}
		}
	}

	public static class ProgramListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == selectProgram)
			{
				// counting program
				if (selectProgram.getSelectedIndex() == 0)
				{
					countingProgram();
				}

				// subtraction program
				if (selectProgram.getSelectedIndex() == 1)
				{
					subtractionProgram();
				}

				// three state halting program
				if (selectProgram.getSelectedIndex() == 2)
				{
					haltingProgramThreeState();
				}

				// three state halting program
				if (selectProgram.getSelectedIndex() == 3)
				{
					haltingProgramFourState();
				}
			}
		}
	}

	/**
	 * Inner class that creates the actionListener used by the state definitions
	 * panel.
	 * 
	 */
	public static class HeadListener implements ActionListener
	{
		Random s = new Random();

		@Override
		public void actionPerformed(ActionEvent e)
		{
			for (int i = 0; i < statePanels.length; i++)
			{
				if (e.getSource() == unify)
				{
					Random r = new Random(s.nextInt());
					if (i == 0)
					{
						for (int l = 0; l < statePanels[0].moveHeads.length; l++)
						{
							statePanels[0].moveHeads[l].setSelectedIndex(r
									.nextInt(8));
							statePanels[0].writerHeads[l].setSelectedIndex(r
									.nextInt(numStates - 1));
							statePanels[0].finiteState[l].setSelectedIndex(r
									.nextInt(numFiniteStates));
						}
					}

					for (int l = 0; l < statePanels[i].moveHeads.length; l++)
					{
						statePanels[i].moveHeads[l]
								.setSelectedIndex(statePanels[0].moveHeads[l]
										.getSelectedIndex());
						statePanels[i].writerHeads[l]
								.setSelectedIndex(statePanels[0].writerHeads[l]
										.getSelectedIndex());
						statePanels[i].finiteState[l]
								.setSelectedIndex(statePanels[0].finiteState[l]
										.getSelectedIndex());
					}
				}

				for (int j = 0; j < statePanels[i].writerHeads.length; j++)
				{
					if (e.getSource() == statePanels[i].writerHeads[j])
					{
						statePanels[i].writerHeads[j] = (JComboBox) e
								.getSource();
						statePanels[i].stateWriterDirections[j] = statePanels[i].writerHeads[j]
								.getSelectedIndex();
					}
					if (e.getSource() == statePanels[i].moveHeads[j])
					{
						statePanels[i].moveHeads[j] = (JComboBox) e.getSource();

						statePanels[i].moveHeadDirections[j] = statePanels[i].moveHeads[j]
								.getSelectedIndex();
					}
					if (e.getSource() == statePanels[i].finiteState[j])
					{
						statePanels[i].finiteState[j] = (JComboBox) e
								.getSource();
						statePanels[i].finiteStateDirections[j] = statePanels[i].finiteState[j]
								.getSelectedIndex();
					}

					if (e.getSource() == statePanels[i].random)
					{
						Random r = new Random();
						for (int l = 0; l < statePanels[i].writerHeads.length; l++)
						{
							statePanels[i].moveHeads[l].setSelectedIndex(r
									.nextInt(8));
							statePanels[i].writerHeads[l].setSelectedIndex(r
									.nextInt(numStates - 1));
							statePanels[i].finiteState[l].setSelectedIndex(r
									.nextInt(numFiniteStates));
						}
					}

				}
			}
		}
	}
}
