/*
 Rule -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.rules;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import cellularAutomata.CAConstants;
import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.error.ErrorHandler;
import cellularAutomata.error.exceptions.BinaryStateOutOfBoundsException;
import cellularAutomata.error.exceptions.IntegerStateOutOfBoundsException;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.reflection.LatticeHash;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.util.files.CAImageIconLoader;

/**
 * A function that changes a cell's state based on the states of neighboring
 * cells. <br>
 * By contract, all child classes <b>must</b> have a constructor with a boolean
 * as a parameter because all rules are loaded dynamically using reflection.
 * <br>
 * Code in the CA lattice warns the user/developer if a child of Rule has been
 * written without the required constructor (with properties as a parameter).
 * This way the contract is enforced "nicely" by the CA code.
 * 
 * @author David Bahr
 */
public abstract class Rule extends RuleActions implements ActionListener,
		PropertyChangeListener
{
	/**
	 * String used as an action command for the JPanel created by the method
	 * getAdditionalPropertiesPanel().
	 */
	public static final String ADDITIONAL_PROPERTIES = "More Properties";

	/**
	 * The path of the default icon displayed with this rule.
	 */
	public static final String DEFAULT_ICON_PATH = "/"
			+ CAConstants.APPLICATION_MINI_ICON_IMAGE_PATH;

	/**
	 * Useful for the getCompatibleLattices method.
	 */
	protected String[] allLatticeNames = null;

	/**
	 * An array of only those lattice names that do not return -1 for the number
	 * of neighbors. Useful for the getCompatibleLattices method.
	 */
	protected static String[] allLatticeNamesWithConstantNumbersOfNeighbors = null;

	/**
	 * For convenience, instructions for left clicking. Appropriate for display
	 * in the tooltip description.
	 */
	protected static String leftClickInstructions = "<p>"
			+ "<b>Left-click</b> the grid to draw cells (set the color in the View menu).";

	/**
	 * For convenience, instructions for right clicking. Appropriate for display
	 * in the tooltip description.
	 */
	protected static String rightClickInstructions = "<p>"
			+ "<b>Right-click</b> the grid to erase cells or to draw in an alternate "
			+ "color (set the color in the View menu).";

	/**
	 * Create a cellular automaton rule.
	 * <p>
	 * When building child classes, the minimalOrLazyInitialization parameter
	 * must be included but may be ignored. However, the boolean is intended to
	 * indicate when the child's constructor should build a rule with as small a
	 * footprint as possible. In order to load rules by reflection, the
	 * application must query the child classes for information like their
	 * display names, tooltip descriptions, etc. At these times it makes no
	 * sense to build the complete rule which may have a large footprint in
	 * memory.
	 * <p>
	 * It is recommended that the child's constructor and instance variables do
	 * not initialize any variables and that variables be initialized only when
	 * first needed (lazy initialization). Or all initializations in the
	 * constructor may be placed in an <code>if</code> statement.
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
	 *            constructed. If uncertain, set this variable to false.
	 */
	public Rule(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		// revise the text to make this mac friendly
		if(CAConstants.MAC_OS)
		{
			// only use these revised strings on systems that are NOT guaranteed
			// to have a right-clicking mouse
			leftClickInstructions = "<br><br>"
					+ "<b>Click</b> the grid to draw cells (set the color in the View menu).";
			rightClickInstructions = "<br><br>"
					+ "<b>Ctrl-click</b> the grid to erase cells or to draw in an alternate <br>"
					+ "color (set the color in the View menu).";
		}

		// now get lists of lattice names for convenience (useful in the
		// getCompatibleLattices() method).
		if(allLatticeNamesWithConstantNumbersOfNeighbors == null)
		{
			LatticeHash lattices = new LatticeHash();
			allLatticeNames = lattices.toArray();
			ArrayList<String> latticesWithSteadyNeighbors = new ArrayList<String>();

			for(String latticeName : allLatticeNames)
			{
				// will be -1 if the number of neighbors is variable or unknown
				if(ReflectionTool
						.getNumberOfNeighborsFromLatticeDescription(latticeName) != -1)
				{
					latticesWithSteadyNeighbors.add(latticeName);
				}
			}
			allLatticeNamesWithConstantNumbersOfNeighbors = new String[latticesWithSteadyNeighbors
					.size()];
			allLatticeNamesWithConstantNumbersOfNeighbors = latticesWithSteadyNeighbors
					.toArray(allLatticeNamesWithConstantNumbersOfNeighbors);
		}
	}

	/**
	 * Reacts to any actions on the JPanel GUI created in
	 * getAdditionalPropertiesPanel(). Should be overridden by child classes
	 * that want to request any information from the user. Note that any
	 * information from the GUI should be stored in static variables so that it
	 * is accessible to all instances. <br>
	 * Do not forget to add the rule as an action listener to any buttons. For
	 * example, from within the overridden getAdditionalPropertiesPanel()
	 * method, use <code>
	 * submitButton.addActionListener(this);
	 * </code> <br>
	 * Closing the dialog box can be tricky, but a simple way to do this in the
	 * actionPerformed() method is to use <code>
	 * //close dialog box
	 * JDialog dialog = (JDialog) panel.getTopLevelAncestor();
	 * dialog.dispose();
	 * </code>
	 * where panel is the JPanel created in the getAdditionalPropertiesPanel()
	 * method. The panel can be made accessible as an instance variable of the
	 * child class.
	 */
	public void actionPerformed(ActionEvent e)
	{
	}

	/**
	 * Calculates the new state of a cell based on the values of neighboring
	 * cells and the value of the cell itself. By convention the neighbors
	 * should be indexed clockwise starting to the northwest of the cell.
	 * <p>
	 * WARNING: Generally (but not always), the rule is intended to apply to the
	 * cell and the neighbors at the same generation. The only way to guarantee
	 * that the cell and neighbors are from the same generation is to use
	 * <code>cell.getState(generation)</code> and
	 * <code>neighbors[i].getState(generation)</code> at the desired
	 * generation. The cell's current generation can be determined from
	 * <code>cell.getGeneration()</code>.
	 * <p>
	 * For integer valued cells, using the same generation can also be
	 * guaranteed with <code>cell.toInt(generation)</code> and
	 * <code>neighbors[i].toInt(generation)</code>
	 * 
	 * @param cell
	 *            The cell being updated.
	 * @param neighbors
	 *            The cells on which the update is based (usually neighboring
	 *            cells). By convention the neighbors should be indexed
	 *            clockwise starting to the northwest of the cell. May be null
	 *            if want this method to find the "neighboring" cells.
	 * @return A new state for the cell.
	 */
	public abstract CellState calculateNewState(Cell cell, Cell[] neighbors);

	/**
	 * Using the Template Design Pattern, calculates the new state of a cell
	 * based on the values of neighboring cells and the value of the cell
	 * itself. By convention the neighbors should be indexed clockwise starting
	 * to the northwest of the cell.
	 * <p>
	 * WARNING: Generally (but not always), the rule is intended to apply to the
	 * cell and the neighbors at the same (current) generation. The only way to
	 * guarantee that the cell and neighbors are from the same generation is to
	 * use <code>cell.getState(generation)</code> and
	 * <code>neighbors[i].getState(generation)</code> at the desired
	 * generation. The cell's current generation can be determined from
	 * <code>cell.getGeneration()</code>.
	 * <p>
	 * For integer valued cells, using the same generation can also be
	 * guaranteed with <code>cell.toInt(generation)</code> and
	 * <code>neighbors[i].toInt(generation)</code>
	 * 
	 * @param cell
	 *            The cell being updated.
	 * @param neighbors
	 *            The cells on which the update is based (usually neighboring
	 *            cells). By convention the neighbors should be indexed
	 *            clockwise starting to the northwest of the cell. May be null
	 *            if want this method to find the "neighboring" cells.
	 * @return A new state for the cell.
	 */
	public final CellState calculateNewStateForCell(Cell cell, Cell[] neighbors)
	{
		// the new cell state that will be returned
		CellState cellState = null;

		// most of this method deals with any errors that might happen while
		// calculating the new state. For example the rule might divide by 0 or
		// access an out-of-bounds array element, or generate a cell state that
		// is too big (bigger than the number of states).
		try
		{
			// call the template method that calculates the new state
			cellState = calculateNewState(cell, neighbors);
		}
		catch(IntegerStateOutOfBoundsException e)
		{
			String warning = "The rule has created an inappropriate state of "
					+ e.getOutOfBoundsValue()
					+ " that is not \n"
					+ "between the required values of 0 and "
					+ (e.getNumStates() - 1)
					+ ". \n\n"
					+ "If you are the author of the rule, you should ensure that all states \n"
					+ "are between 0 and the value of the variable \"numStates\". \n\n"
					+ "The application will close.\n\n";

			// end the program
			ErrorHandler.endProgramWithWarningUnlessDebugging(e, warning,
					"Cell state out of bounds (integer).");
		}
		catch(BinaryStateOutOfBoundsException e)
		{
			String warning = "The rule has created an inappropriate state with value "
					+ e.getOutOfBoundsValue()
					+ ". \n"
					+ "Binary rules can only create states with values 0 or 1. \n\n"
					+ "If you are the author of the rule, you should ensure that \n"
					+ "all states are between 0 and 1. \n\n"
					+ "The application will close.\n\n";

			// end the program
			ErrorHandler.endProgramWithWarningUnlessDebugging(e, warning,
					"Cell state out of bounds (binary).");
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			String errorMessage = e.getMessage();
			if((errorMessage == null) || errorMessage.equals(""))
			{
				errorMessage = "unknown";
			}

			String warning = "The rule has attempted to access an array element that does not \n"
					+ "exist: element number "
					+ errorMessage
					+ ".\n\n"
					+ "If you are the author of the rule, you should check for bugs in \n"
					+ "your code. \n\n" + "The application will close.\n\n";

			// end the program
			ErrorHandler.endProgramWithWarningUnlessDebugging(e, warning,
					"The selected rule has crashed (array out of bounds).");
		}
		catch(NullPointerException e)
		{
			String errorMessage = e.getMessage();
			if((errorMessage == null) || errorMessage.equals(""))
			{
				errorMessage = "Sorry, no additional details are available.";
			}

			String warning = "The rule has thrown a null pointer exception.  In other words, \n"
					+ "the rule has attempted to use an object that was null.  Details: \n\n"
					+ errorMessage
					+ "\n\n"
					+ "If you are the author of the rule, you should check for bugs in \n"
					+ "your code. \n\n" + "The application will close.\n\n";

			// end the program
			ErrorHandler.endProgramWithWarningUnlessDebugging(e, warning,
					"The selected rule has crashed (null pointer).");
		}
		catch(Throwable t)
		{
			String errorMessage = t.getMessage();
			if((errorMessage == null) || errorMessage.equals(""))
			{
				errorMessage = "Sorry, no details are available.";
			}

			String warning = "The rule has crashed with the following message: \n\n"
					+ errorMessage
					+ "\n\n"
					+ "If you are the author of the rule, you should check for bugs in \n"
					+ "your code. \n\n" + "The application will close.\n\n";

			// end the program
			ErrorHandler.endProgramWithWarningUnlessDebugging(t, warning,
					"The selected rule has crashed (no details).");
		}

		return cellState;
	}

	/**
	 * Make sure that getCompatibleCellState() returns a new instance every time
	 * that it is called. If it does not, then each cell may hold the exact same
	 * state. This is a utility method called by other classes (for example,
	 * CellStateFactory) that want to ensure the CellState is compatible with a
	 * particular rule. Used to warn developers of potential errors.
	 */
	public boolean checkCompatibleCellState()
	{
		boolean compatible = true;

		if(getCompatibleCellState().hashCode() == getCompatibleCellState()
				.hashCode())
		{
			String warning = "Warning: The method getCompatibleCellState() "
					+ "in the \n"
					+ "class "
					+ this.getClass().getName()
					+ " must return a \n"
					+ "different CellState instance every time it is called. \n"
					+ "Your method returns the same instance each time.  To \n"
					+ "avoid this problem, do not return an instance variable \n"
					+ "in getCompatibleCellState(). Instead return a non-static \n"
					+ "CellState created within the method.  For example, \n\n"
					+ "             return new IntegerCellState(2, 0, view);\n\n"
					+ "where \"view\" is a CellStateView.";
			JOptionPane.showMessageDialog(null, warning, "Developer Warning",
					JOptionPane.WARNING_MESSAGE);
			compatible = false;
		}

		return compatible;
	}

	/**
	 * Tells the graphics whether or not the "Running average" text field should
	 * be enabled. By default, is true and the running average is enabled. This
	 * method should be overriden by child classes if they desire non-default
	 * behavior.
	 * 
	 * @return true if the running average should be enabled, and false if it
	 *         should be disabled.
	 */
	public boolean enableRunningAverage()
	{
		return true;
	}

	/**
	 * Gets a JPanel that may request specific input information that the rule
	 * needs to operate correctly. Should be overridden by child classes that
	 * desire to input any specific information.
	 * <p>
	 * Note that if returns null, then the panel is not displayed by the current
	 * version of the CA ControlPanel class. This null behavior is the default.
	 * 
	 * @return A JPanel requesting specific input information that the rule
	 *         needs to operate correctly. May be null.
	 */
	public JPanel getAdditionalPropertiesPanel()
	{
		return null;
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
		return null;
	}

	/**
	 * Gets an instance of the CellState class that is compatible with this rule
	 * (must be the same as the type returned by the method
	 * calculateNewState()). The values assigned to this instance (via its
	 * constructor) will be used by all the CellStates -- this is the proper way
	 * to set static instance variables in the CellState class. Appropriate
	 * CellStates to return include BinaryCellState, IntegerCellState,
	 * IntegerVectorState, and RealValuedVectorState among others.
	 * <p>
	 * This method must return a different cellState instance every time it is
	 * called. If not, will print a warning, and all the cells may hold the
	 * exact same state. For example, the following would be appropriate "return
	 * new IntegerCellState(2, 0, view);" where "view" is a CellStateView.
	 * 
	 * @return An instance of the CellState (any values passed to the
	 *         constructor of the CellState should match those values needed by
	 *         this rule).
	 */
	public abstract CellState getCompatibleCellState();

	/**
	 * Gets an instance of the CellStateView class that will be used to display
	 * cells being updated by this rule. Note: This method must return a view
	 * that is able to display cell states of the type returned by the method
	 * getCompatibleCellState(). Appropriate CellStatesViews to return include
	 * BinaryCellStateView, IntegerCellStateView, HexagonalIntegerCellStateView,
	 * IntegerVectorArrowView, IntegerVectorDefaultView, and
	 * RealValuedDefaultView among others. the user may also create their own
	 * views (see online documentation).
	 * <p>
	 * Any values passed to the constructor of the CellStateView should match
	 * those values needed by this rule.
	 * 
	 * @return An instance of the CellStateView (any values passed to the
	 *         constructor of the CellStateView should match those values needed
	 *         by this rule).
	 */
	public abstract CellStateView getCompatibleCellStateView();

	/**
	 * A list of lattices with which this Rule will work. Well-designed Rules
	 * should work with any lattice, but some may require particular topological
	 * or geometrical information (like the lattice gas). Appropriate strings to
	 * return in the array include SquareLattice.DISPLAY_NAME,
	 * HexagonalLattice.DISPLAY_NAME,
	 * StandardOneDimensionalLattice.DISPLAY_NAME, etc. If null, will be
	 * compatible with all lattices.
	 * 
	 * @return A list of lattices compatible with this Rule (returns the display
	 *         names for the lattices). Returns null if compatible with all
	 *         lattices.
	 */
	public abstract String[] getCompatibleLattices();

	/**
	 * The view used to display values generated by the rule.
	 * 
	 * @return The view used to display values generated by the rule.
	 */
	public static CellStateView getCurrentView()
	{
		String ruleClassName = CurrentProperties.getInstance()
				.getRuleClassName();
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(ruleClassName);
		return rule.getCompatibleCellStateView();
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
		return null;
	}

	/**
	 * When displayed for selection, the rule will be shown with the icon
	 * specified here. A default icon is specified, but child classes should
	 * override this method if they want to use an icon tailored to this rule.
	 * <p>
	 * The icons should be stored in the images folder. For example, an
	 * appropriate path is "ca_icon_mini.gif" for an image that lives in the
	 * "images" folder. Or "ruleIcons/ca_icon_mini.gif" for an image in the
	 * "images/ruleIcons" sub-folder.
	 * <p>
	 * A 6 by 6mm (roughly 16 by 16 pixels) image is preferred. Yes, that is
	 * very small.
	 * 
	 * @return An icon used when displaying this rule.
	 */
	public ImageIcon getDisplayIcon()
	{
		// the icon we will return
		ImageIcon caIcon = CAImageIconLoader.loadImage("ruleIcons/"
				+ this.getClass().getSimpleName() + ".png");

		return caIcon;
	}

	/**
	 * A brief one or two-word string describing the rule, appropriate for
	 * display in a drop-down list. (The rule is instantiated reflectively to
	 * get the display name.)
	 * 
	 * @return The display name of the rule, or null if no rule can be found for
	 *         the specified class name.
	 */
	public static String getCurrentRuleDisplayName()
	{
		String ruleClassName = CurrentProperties.getInstance()
				.getRuleClassName();
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(ruleClassName);

		return rule.getDisplayName();
	}

	/**
	 * A brief one or two-word string describing the rule, appropriate for
	 * display in a drop-down list. (The rule is instantiated reflectively to
	 * get the display name.)
	 * 
	 * @param ruleClassName
	 *            The name of the class (including package). Must end in
	 *            ".class".
	 * @return The display name of the rule, or null if no rule can be found for
	 *         the specified class name.
	 */
	public static String getRuleDisplayNameFromClassName(String ruleClassName)
	{
		String displayName = null;
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(ruleClassName);
		if(rule != null)
		{
			displayName = rule.getDisplayName();
		}

		return displayName;
	}

	/**
	 * A brief one or two-word string describing the rule, appropriate for
	 * display in a drop-down list.
	 * 
	 * @return A string no longer than 15 characters.
	 */
	public abstract String getDisplayName();

	/**
	 * The path for an html file that describes this rule. The file path only
	 * needs to be relative to the "ruleDescriptions" folder. For example,
	 * "userRules/Rule102.html" indicates that the file lives in
	 * "ruleDescriptions/userRules/Rule102.html". And "Rule102.html" indicates
	 * that the file lives in "ruleDescriptions/Rule102.html".
	 * <p>
	 * Child classes may wish to override this method. By default, the file name
	 * is the same as the class name. For example, the file name for the class
	 * "BriansBrain.java" would be "BriansBrain.html". If this file does not
	 * exist, then it will display the rule's tooltip instead.
	 * 
	 * @return A file path. May be null if there is no file.
	 */
	public String getHTMLFilePath()
	{
		// this makes the file name the same as the class name. For example, the
		// file name for the class "BriansBrain.java" would be
		// "BriansBrain.html". If this file does not exist, then it will use the
		// tooltip instead.
		return this.getClass().getSimpleName() + ".html";
	}

	/**
	 * Gets an array of names for any initial states defined by the rule. In
	 * other words, if the rule has two initial states that it would like to
	 * define, then they are each given names and placed in an array of size 2
	 * that is returned by this method. The names are then displayed on the
	 * Properties panel. The CA will always have "single seed", "random", and
	 * "blank" for initial states. This array specifies additional initial
	 * states that should be displayed. The names should be unique.
	 * <p>
	 * By default this returns null. Child classes should override the method if
	 * they wish to specify initial states that will appear on the Properties
	 * panel.
	 * <p>
	 * Note: This method should be used in conjunction with the setInitialState
	 * method, also in this class.
	 * 
	 * @return An array of names for initial states that are specified by the
	 *         rule.
	 */
	public String[] getInitialStateNames()
	{
		return null;
	}

	/**
	 * Optional. Gets an array of JPanels that will be displayed as part of this
	 * rule's initial states. The order and size should be the same as the the
	 * initial states in the getInitialStateNames() method. There is no
	 * requirement that any panel be displayed, and any of the array elements
	 * may be null. The entire array may be null.
	 * <p>
	 * Note: This method should be used in conjunction with the
	 * getInitialStateNames and setInitialState methods, also in this class.
	 * <p>
	 * Child classes should override the default behavior which returns null,
	 * which displays no panel. See DiffusionLimitedAggregation as an example
	 * which uses this panel. See Fractal for an example that specifies initial
	 * states but does not use this panel.
	 * <p>
	 * Values for components in the panel can be saved and reset from the
	 * properties. When creating the initial state in setInitialState(), just
	 * save the values using "properties.setProperty(key, value)". The next time
	 * the application is started, the values can be read in this method and
	 * used to set the components initial value. Read the value using
	 * "properties.getProperty(key);". This allows persistence across sessions.
	 * In other words, if the user closes the application, it can be reopened
	 * with the same values by reading the previously saved property values. To
	 * prevent property naming conflicts, please start every key with the class
	 * name of the rule. For example, key = "DiffusionLimitedAggregation:
	 * random" (see the DiffusionLimitedAggregation class).
	 * 
	 * @return An array indicating what panels should be displayed as part of
	 *         the initial states. If null, no panel will be displayed for any
	 *         initial states that are specified by this rule. If any element of
	 *         the arry is null, then no panel will be displayed for that
	 *         corresponding initial state.
	 */
	public JPanel[] getInitialStateJPanels()
	{
		return null;
	}

	/**
	 * Gets tool tips for any initial states defined by the rule. The tool tips
	 * should be given in the same order as the initial state names in the
	 * method getInitialStateNames. The tool tip array must be null or the same
	 * length as the array of initial state names.
	 * <p>
	 * Strongly recommended that graphics components on the JPanels be static so
	 * that they apply to all cells. Otherwise, the graphics displayed on the
	 * Initial State tab will not be the same graphics associated with each
	 * cell.
	 * <p>
	 * By default this returns null. Child classes should override the method if
	 * they wish to specify initial state tool tips that will appear on the
	 * Properties panel.
	 * <p>
	 * Note: This method should be used in conjunction with the
	 * getInitialStateNames and setInitialState method, also in this class.
	 * 
	 * @return An array of tool tips for initial states that are specified by
	 *         the rule. May be null. Any element of the array may also be null,
	 *         but if the length of the array is non-zero, then the length must
	 *         be the same as the array returned by getInitialStateNames.
	 */
	public String[] getInitialStateToolTips()
	{
		return null;
	}

	/**
	 * The number of generations that a Cell must store in order for this rule
	 * to work properly. For example, the rule might need to use states from the
	 * current generation as well as 4 previous generations to calculate the
	 * next generation's state. That means this method should return 5. If only
	 * the current generation is required, then this method should return 1.
	 * <br>
	 * This method returns a default value of 1, but child classes may override
	 * this method to change its value. <br>
	 * Every rule must at least use the current generation to calculate the next
	 * generation, so a value of at least 1 is necessary. The Cell class will
	 * enforce this, with a warning.
	 * 
	 * @return The number of generations that each cell must save.
	 */
	public int getRequiredNumberOfGenerations()
	{
		return 1;
	}

	/**
	 * Tells the graphics what value should be displayed for the "Running
	 * average" text field. Should be an integer greater than or equal to 1. By
	 * default, returns null, which keeps the text field's current value. This
	 * method should be overriden by child classes if they desire non-default
	 * behavior.
	 * 
	 * @return The running average that will be displayed. By default returns
	 *         null, which keeps the text field's current value.
	 */
	public Integer getRunningAverageToDisplay()
	{
		return null;
	}

	/**
	 * A very brief ONE LINE description (written in html) that describes this
	 * rule. The description will be displayed as a tooltip.
	 * <p>
	 * I recommend starting with the rule name in bold, followed by a one line
	 * description. The first line of the extended html description is usually
	 * appropriate as a one line description (see getHTMLFilePath()).
	 * 
	 * @return An one line string describing this rule.
	 */
	public abstract String getToolTipDescription();

	/**
	 * Handles notification of any changes in properties.
	 * <p>
	 * By default does nothing. Override to handle any notifications. For
	 * example, if notified that the color scheme changes, may want to change
	 * state colors that are displayed on the additional properties panel.
	 * <p>
	 * The rule may be notified by events that happen in the menu, toolbar, or
	 * any of the display panels (property panel, initial state panel,
	 * etcetera).
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		// Does nothing by default. Override to handle any notifications. For
		// example, if notified that the color scheme changes, may want to
		// change state colors that are displayed on the additional properties
		// panel.
	}

	/**
	 * Gets an initial state that corresponds to the specified initialStateName.
	 * This method is optional and sets no values by default.
	 * <p>
	 * If a rule wishes to define one or more initial states (different from the
	 * "single seed", "random", and "blank" that are already provided), then the
	 * name of the state is specified in the getInitialStateNames() method. The
	 * name is displayed on the Initial States tab, and if it is selected, then
	 * this method is called with that name. Based on the name, this method can
	 * then specify an initial state.
	 * <p>
	 * By default this method sets no values. Child classes should override the
	 * method if they wish to specify initial states that will appear on the
	 * Initial States tab.
	 * <p>
	 * The lattice parameter is used to retrieve an iterator over the cells and
	 * assign the initial values to the cells. An example is <code>
	 *   //assigns the same double value to each cell.
	 *   Iterator cellIterator = lattice.iterator();
	 *   while(cellIterator.hasNext())
	 *   {
	 *       Cell cell = (Cell) cellIterator.next();
	 *
	 *       // assign the value
	 *       cell.getState().setValue(new Double(3.4));
	 *   }
	 * </code>
	 * Here is another example that assigns values to the cell at both the
	 * current generation and previous generations. The number of generations in
	 * the stateHistory variable (below) would be determined by the method
	 * getRequiredNumberOfGenerations in this class. <code>
	 *   //assign the same double value to each cell
	 *   Double double = new Double(9.3);
	 *   Iterator cellIterator = lattice.iterator();
	 *   while(cellIterator.hasNext())
	 *   {
	 *       Cell cell = (Cell) cellIterator.next();
	 *
	 *       // get the list of states for each cell
	 *       ArrayList stateHistory = cell.getStateHistory();
	 *       int historySize = stateHistory.size();
	 *
	 *       // there may be more than one state required as initial conditions
	 *       for(int i = 0; i < historySize; i++)
	 *       {
	 *            ((CellState) stateHistory.get(i)).setValue(double);
	 *       }
	 *  }
	 * </code>
	 * Another example is given in the Fractal rule. That example figures out
	 * the row and column number of each cell and assigns values based on their
	 * row and column.
	 * <p>
	 * Note: This method should be used in conjunction with the getInitialState
	 * method, also in this class.
	 * <p>
	 * The method getInitialStateJPanels() will create a panel (with components
	 * if desired) that is displayed on the Initial States tab. See
	 * DiffusionLimitedAggregation for an example. Values for components in this
	 * panel can be read and used by this method when setting the initial state.
	 * Values for components in the panel can also be saved in the properties.
	 * In this method, save the values using "properties.setProperty(key,
	 * value)". The next time the application is started, the values can be read
	 * in the method getInitialStateJPanels() and used to set the components
	 * initial value. Read the value using "properties.getProperty(key);". This
	 * allows persistence across sessions. In other words, if the user closes
	 * the application, it can be reopened with the same values by reading the
	 * previously saved property values. To prevent property naming conflicts,
	 * please start every key with the class name of the rule. For example, key =
	 * "DiffusionLimitedAggregation: random" (see the
	 * DiffusionLimitedAggregation class).
	 * <p>
	 * By default this method does nothing.
	 * 
	 * @param initialStateName
	 *            The name of the initial state (will be one of the names
	 *            specified in the getInitialStateNames method).
	 * @param lattice
	 *            The CA lattice. This will either be a one or two-dimensional
	 *            lattice which holds the cells. The cells should be collected
	 *            from the lattice and assigned initial values.
	 */
	public void setInitialState(String initialStateName, Lattice lattice)
	{
	}

	/**
	 * Gets the display name of the rule.
	 * 
	 * @return the display name.
	 */
	public String toString()
	{
		return getDisplayName();
	}
}
