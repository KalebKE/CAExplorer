/*
 CAMenuBar -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.graphics;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.EventListener;
import java.util.Random;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

import cellularAutomata.CAController;
import cellularAutomata.Cell;
import cellularAutomata.CAConstants;
import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.IntegerCellStateView;
import cellularAutomata.error.WarningManager;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.view.LatticeView;
import cellularAutomata.graphics.colors.*;
import cellularAutomata.graphics.colors.colorChooser.IntegerStateColorChooser;
import cellularAutomata.help.CAHelp;
import cellularAutomata.help.CAGettingStarted;
import cellularAutomata.help.CAGuidedTour;
import cellularAutomata.help.CAIncreasingMemoryTutorial;
import cellularAutomata.help.CAObesityTutorial;
import cellularAutomata.help.CARuleConstructors;
import cellularAutomata.help.CAPropertiesChanged;
import cellularAutomata.help.CAHowManyRulesAreThere;
import cellularAutomata.help.CAHowToWriteYourOwnRule;
import cellularAutomata.help.CAShowHiddenTabs;
import cellularAutomata.movie.MovieMaker;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.reflection.RuleHash;
import cellularAutomata.reflection.URLResource;
import cellularAutomata.rules.IntegerRule;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.CAFileChooser;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.ImagePreviewer;
import cellularAutomata.util.MemoryManagementTools;
import cellularAutomata.util.PreviewPanel;
import cellularAutomata.util.SwingWorker;
import cellularAutomata.util.files.AllImageFilter;
import cellularAutomata.util.files.AllImageTypeReader;
import cellularAutomata.util.files.CAFileFilter;
import cellularAutomata.util.files.JPGAndPNGImageReadWrite;
import cellularAutomata.util.files.JPGAndPNGAndOtherFileTypeFilter;
import cellularAutomata.util.graphics.ShimmyingTenTimesIconJButton;
import cellularAutomata.util.graphics.SpinningIconJOptionPane;
import cellularAutomata.util.graphics.RotatedImageIcon;
import cellularAutomata.util.math.RandomSingleton;

/**
 * Creates a menu bar for the CA, including, exit, save, print, etc.
 * 
 * @author David Bahr
 */
public class CAMenuBar extends JMenuBar implements ActionListener,
		PropertyChangeListener
{
	/**
	 * The factor by which the image zooms in (and out) when the zoom buttons
	 * are selected.
	 */
	public final static double ZOOM_FACTOR = 1.5;

	/**
	 * A string used for the "About" selection in the menu and also used to
	 * notify other classes that an "About" event has been fired. In other
	 * words, the "About" button was selected.
	 */
	public final static String ABOUT = "About";

	/**
	 * A string used for the "Choose left-click drawing color" selection in the
	 * menu and also used to notify other classes that a "Choose left-click
	 * drawing color" event has been fired. In other words, the "Choose
	 * left-click drawing color" button was selected.
	 */
	public static String CHOOSE_DRAW_COLOR = "Choose \"Left-Click\" Drawing Color";

	/**
	 * A string used for the "Choose right-click drawing color" selection in the
	 * menu and also used to notify other classes that a "Choose right-click
	 * drawing color" event has been fired. In other words, the "Choose
	 * right-click drawing color" button was selected.
	 */
	public final static String CHOOSE_RIGHTCLICK_DRAW_COLOR = "Choose \"Right-Click\" "
			+ "Drawing Color";

	// A string used for the "Choose ctrl-click drawing color" selection in the
	// menu.
	private final static String CHOOSE_CTRLCLICK_DRAW_COLOR = "Choose \"Ctrl-Click\" drawing "
			+ "color";

	/**
	 * A string used for the default color scheme selection in the menu and also
	 * used to notify other classes that a "Default color scheme" event has been
	 * fired. In other words, the "Default color scheme" button was selected.
	 */
	public final static String CHOOSE_DEFAULT_COLOR_SCHEME = RainbowColorScheme.schemeName;

	/**
	 * A string used for the black and white scheme selection in the menu and
	 * also used to notify other classes that a "Black and White" event has been
	 * fired. In other words, the "Black and White" button was selected.
	 */
	public final static String CHOOSE_BLACK_AND_WHITE_COLOR_SCHEME = BlackAndWhiteColorScheme.schemeName;

	/**
	 * A string used for the blue diamond color scheme selection in the menu and
	 * also used to notify other classes that a "Blue diamond" event has been
	 * fired. In other words, the "Blue diamond" button was selected.
	 */
	public final static String CHOOSE_BLUE_DIAMOND_COLOR_SCHEME = BlueDiamondColorScheme.schemeName;

	/**
	 * A string used for the blue shades color scheme selection in the menu and
	 * also used to notify other classes that a "Blue shades" event has been
	 * fired. In other words, the "Blue shades" button was selected.
	 */
	public final static String CHOOSE_BLUE_SHADES_COLOR_SCHEME = KindOfBluesColorScheme.schemeName;

	/**
	 * A string used for the fire color scheme selection in the menu and also
	 * used to notify other classes that a "Fire" event has been fired. In other
	 * words, the "Fire" button was selected.
	 */
	public final static String CHOOSE_FIRE_COLOR_SCHEME = FireColorScheme.schemeName;

	/**
	 * A string used for the gray smoke color scheme selection in the menu and
	 * also used to notify other classes that a "Gray Smoke" event has been
	 * fired. In other words, the "Gray Smoke" button was selected.
	 */
	public final static String CHOOSE_GRAY_SMOKE_COLOR_SCHEME = ChocolateColorScheme.schemeName;

	/**
	 * A string used for the green ocean color scheme selection in the menu and
	 * also used to notify other classes that a "Green ocean" event has been
	 * fired. In other words, the "Green ocean" button was selected.
	 */
	public final static String CHOOSE_GREEN_OCEAN_COLOR_SCHEME = GreenOceanColorScheme.schemeName;

	/**
	 * A string used for the Water lilies color scheme selection in the menu and
	 * also used to notify other classes that a "Water lilies" event has been
	 * fired. In other words, the "Water lilies" button was selected.
	 */
	public final static String CHOOSE_WATER_LILIES_COLOR_SCHEME = WaterLiliesColorScheme.schemeName;

	/**
	 * A string used for the white and black scheme selection in the menu and
	 * also used to notify other classes that a "White and Black" event has been
	 * fired. In other words, the "White and Black" button was selected.
	 */
	public final static String CHOOSE_WHITE_AND_BLACK_COLOR_SCHEME = WhiteAndBlackColorScheme.schemeName;

	/**
	 * A string used for the Yellow Jacket color scheme selection in the menu
	 * and also used to notify other classes that a "Yellow Jacket" event has
	 * been fired. In other words, the "Yellow Jacket" button was selected.
	 */
	public final static String CHOOSE_YELLOW_JACKET_COLOR_SCHEME = YellowJacketColorScheme.schemeName;

	/**
	 * A string used for the Purple haze color scheme selection in the menu and
	 * also used to notify other classes that a "Purple haze" event has been
	 * fired. In other words, the "Purple haze" button was selected.
	 */
	public final static String CHOOSE_PURPLE_HAZE_COLOR_SCHEME = PurpleHazeColorScheme.schemeName;

	/**
	 * A string used for the random color scheme selection in the menu and also
	 * used to notify other classes that a "Random" event has been fired. In
	 * other words, the "Random" button was selected.
	 */
	public final static String CHOOSE_RANDOM_COLOR_SCHEME = RandomColorScheme.schemeName;

	/**
	 * A string used for the "Choose empty color" selection in the menu and also
	 * used to notify other classes that a "Choose empty color" event has been
	 * fired. In other words, the "Choose empty color" button was selected.
	 */
	public final static String CHOOSE_EMPTY_COLOR = "Choose \"Empty\" Color";

	/**
	 * A string used for the "Choose file delimeter" selection in the menu and
	 * also used to notify other classes that a "Choose file delimeter" event
	 * has been fired. In other words, the "Choose file delimeter" button was
	 * selected.
	 */
	public final static String CHOOSE_FILE_DELIMETER = "Choose File Delimeter";

	/**
	 * A string used for the "Choose filled color" selection in the menu and
	 * also used to notify other classes that a "Choose filled color" event has
	 * been fired. In other words, the "Choose filled color" button was
	 * selected.
	 */
	public final static String CHOOSE_FILLED_COLOR = "Choose \"Occupied\" color";

	/**
	 * A string used for the "Choose rule folder" selection in the menu and also
	 * used to notify other classes that a "Choose rule folder" event has been
	 * fired. In other words, the "Choose rule folder" button was selected.
	 */
	public final static String CHOOSE_RULE_FOLDER = "Choose Rule Folder";

	/**
	 * A string used for the "Open Hyperlinks in This Application" selection in
	 * the menu and also used to notify other classes that a "Open Hyperlinks in
	 * This Application" event has been fired. In other words, the "Open
	 * Hyperlinks in This Application" button was selected.
	 */
	public final static String DISPLAY_LINKS_IN_APPLICATION = "Open Hyperlinks in This "
			+ "Application";

	/**
	 * A string used for the "Open Hyperlinks in Separate CABrowser" selection
	 * in the menu and also used to notify other classes that a "Open Hyperlinks
	 * in Separate CABrowser" event has been fired. In other words, the "Open
	 * Hyperlinks in Separate CABrowser" button was selected.
	 */
	private static String DISPLAY_LINKS_IN_BROWSER = "Open Hyperlinks in Internet Explorer";

	/**
	 * Title for the color chooser for drawing.
	 */
	public final static String DRAW_COLOR_CHOOSER_TITLE = "Choose a color (cell state) to draw.";

	/**
	 * Tooltip for the tabs when the EZ facade option is selected.
	 */
	public final static String EZ_TAB_TOOLTIP = " -- <b> select the \"All\" button to activate "
			+ "this tab </b>";

	/**
	 * Tooltip for the tabs when the EZ facade option is selected.
	 */
	public final static String EZ_TAB_TOOLTIP_WITHOUT_HTML = " -- select the \"All\" button "
			+ "to activate this tab";

	/**
	 * A string used for the "Exit" selection in the menu and also used to
	 * notify other classes that an "Exit" event has been fired. In other words,
	 * the "Exit" button was selected.
	 */
	public final static String EXIT = "Exit";

	/**
	 * A string used for the "Fit to screen" selection in the menu and also used
	 * to notify other classes that an "Fit to screen" event has been fired. In
	 * other words, the "Fit to screen" button was selected.
	 */
	public final static String FIT_TO_SCREEN = "Fit to Screen";

	/**
	 * A string used for the "Getting Started" selection in the menu and also
	 * used to notify other classes that a "Getting Started" event has been
	 * fired. In other words, the "Getting Started" button was selected.
	 */
	public final static String GETTING_STARTED = "Getting Started";

	/**
	 * A string used for the "Guided Tour" selection in the menu and also used
	 * to notify other classes that a "Guided Tour" event has been fired. In
	 * other words, the "Guided Tour" button was selected.
	 */
	public final static String GUIDED_TOUR = "Guided Tour";

	/**
	 * A string used for the "Help contents" selection in the menu and also used
	 * to notify other classes that a "Help contents" event has been fired. In
	 * other words, the "Help contents" button was selected.
	 */
	public final static String HELP = "Help Contents";

	/**
	 * A string displayed in the menu to find information on the number of
	 * rules.
	 */
	public final static String HOW_MANY_RULES_ARE_THERE = "How Many Rules Are "
			+ "There?";

	/**
	 * A string displayed in the menu to find information on the number of
	 * rules.
	 */
	public final static String HAVE_PROPERTIES_CHANGED = "Have Properties Changed?";

	/**
	 * A string displayed in the menu to find information on rule constructors.
	 */
	public final static String CA_RULE_CONSTRUCTORS = "Have Rule Constructors "
			+ "Changed?";

	/**
	 * A string used for the "Import Data" selection in the menu and also used
	 * to notify other classes that an "Import Data" event has been fired. In
	 * other words, the "Import Data" button was selected.
	 */
	public final static String IMPORT_DATA = "Import Data";

	/**
	 * A string used for the "Import Image" selection in the menu and also used
	 * to notify other classes that an "Import Image" event has been fired. In
	 * other words, the "Import Image" button was selected.
	 */
	public final static String IMPORT_DATA_IMAGE = "Import Image";

	/**
	 * A string used for the "Import Simulation" selection in the menu and also
	 * used to notify other classes that an "Import Simulation" event has been
	 * fired. In other words, the "Import Simulation" button was selected.
	 */
	public final static String IMPORT_SIMULATION = "Import Simulation";

	/**
	 * A string used for the memory tutorial selection on the menu and also used
	 * to notify other classes that a memory tutorial event has been fired. In
	 * other words, the memory tutorial button was selected.
	 */
	public final static String MEMORY_TUTORIAL = "Increasing Memory and Speed";

	/**
	 * A string used for the "Move Controls Right" selection in the menu and
	 * also used to notify other classes that an "Move Controls Right" event has
	 * been fired. In other words, the "Move Controls Right" button was
	 * selected.
	 */
	public final static String MOVE_CONTROLS = "Flip Layout";

	/**
	 * A string used for the "Obesity Tutorial" selection in the menu and also
	 * used to notify other classes that a "Obesity Tutorial" event has been
	 * fired. In other words, the "Obesity Tutorial" button was selected.
	 */
	public final static String OBESITY_TUTORIAL = "Obesity Model Tutorial (For Researchers)";

	/**
	 * A string used for the "Print" selection in the menu and also used to
	 * notify other classes that a printer event has been fired. In other words,
	 * the printer button was selected.
	 */
	public final static String PRINT = "Print";

	/**
	 * A string used for the "Random Rule" selection in the menu and also used
	 * to notify other classes that a "random rule" event has been fired. In
	 * other words, the "random rule" button was selected.
	 */
	public final static String RANDOM = "Random Rule";

	/**
	 * A string used for the "Reset Application" selection in the menu and also
	 * used to notify other classes that a "Reset Application" event has been
	 * fired. In other words, the "Reset Application" button was selected.
	 */
	public final static String RESET_APPLICATION = "Reset Application";

	/**
	 * A string used for the "Restore default colors" selection in the menu and
	 * also used to notify other classes that an "Restore default colors" event
	 * has been fired. In other words, the "Restore default colors" button was
	 * selected.
	 */
	public final static String RESTORE_DEFAULT_COLOR = "Restore Default Colors";

	/**
	 * A string used for the "Save" selection in the menu and also used to
	 * notify other classes that a "save" event has been fired. In other words,
	 * the "Save" button was selected.
	 */
	public final static String SAVE = "Save";

	/**
	 * A string used for the "Save as ..." selection in the menu and also used
	 * to notify other classes that a "save as ..." event has been fired. In
	 * other words, the "Save as ..." button was selected.
	 */
	public final static String SAVE_AS = "Save As ...";

	/**
	 * A string used for the "Save as Image" selection in the menu and also used
	 * to notify other classes that a "save as image" event has been fired. In
	 * other words, the "Save as Image" button was selected.
	 */
	public final static String SAVE_AS_IMAGE = "Save As Image";

	/**
	 * A string used for the "Make a Movie" selection in the menu and also used
	 * to notify other classes that a "save as movie" event has been fired. In
	 * other words, the "Make a Movie" button was selected.
	 */
	public final static String SAVE_AS_MOVIE = "Make Movie";

	/**
	 * A string used for the "Stop Movie" selection in the menu and also used to
	 * notify other classes that a "save as movie" event has been fired. In
	 * other words, the "Stop Movie" button was selected.
	 */
	public final static String STOP_MOVIE = "Stop Movie";

	/**
	 * A property string used to identify the path where the image should be
	 * saved.
	 */
	public final static String SAVE_AS_IMAGE_PATH = "Save As Image Path";

	/**
	 * A string used for the "Choose maximum rule size" selection in the menu.
	 * In other words, the "Choose maximum rule size" button was selected.
	 */
	public static String SET_MAX_RULE_DIGITS = "Set Maximum Rule Size";

	/**
	 * A string used as the action command for a button that lets the user
	 * choose the number of parallel processors when there are more than 16
	 * available processors.
	 */
	public final static String SET_NUM_PROCESSORS_FROM_POPUP = "Choose Parallel Processers";

	/**
	 * A string used as the action command for a button that lets the user
	 * choose the number of parallel processors when there are fewer than 17
	 * available processors.
	 */
	public final static String SET_NUMBER_OF_PROCESSORS = "Parallel Processing";

	/**
	 * A string used in the help menu and as the action command for popping up a
	 * help page that says how to use the ALL button.
	 */
	public final static String SHOW_HIDDEN_TABS = "How to Access Grey Tabs (Press \"All\")";

	/**
	 * A string used to indicate that a "slow down" event has been fired..
	 */
	public final static String SLOW_DOWN = "Slow Down";

	/**
	 * A string used to indicate that a "speed up" event has been fired..
	 */
	public final static String SPEED_UP = "Speed Up";

	/**
	 * A string used for the "Start simulation" selection in the menu and also
	 * used to notify other classes that an "Start simulation" event has been
	 * fired. In other words, the "Start simulation" button was selected.
	 */
	public final static String START = "Start Simulation";

	/**
	 * A string used for the "Increment" selection in the menu and also used to
	 * notify other classes that an "Increment" event has been fired. In other
	 * words, the "Increment" button was selected.
	 */
	public final static String STEP1 = "Step 1";

	/**
	 * A string used for the "Start simulation" selection in the menu and also
	 * used to notify other classes that an "Start simulation" event has been
	 * fired. In other words, the "Start simulation" button was selected.
	 */
	public final static String STEP10 = "Step 10";

	/**
	 * A string used for the "Start simulation" selection in the menu and also
	 * used to notify other classes that an "Start simulation" event has been
	 * fired. In other words, the "Start simulation" button was selected.
	 */
	public final static String STEP_BACK = "Rewind";

	/**
	 * A string used for the "Start simulation" selection in the menu and also
	 * used to notify other classes that an "Start simulation" event has been
	 * fired. In other words, the "Start simulation" button was selected.
	 */
	public final static String STEP_FILL = "Fill Lattice";

	/**
	 * A string used for the "Stop simulation" selection in the menu and also
	 * used to notify other classes that an "Stop simulation" event has been
	 * fired. In other words, the "Stop simulation" button was selected.
	 */
	public final static String STOP = "Stop Simulation";

	/**
	 * A property string used to indicate that the lattice mesh should be turned
	 * on or off.
	 */
	public final static String TOGGLE_MESH = "Toggle the Mesh";

	/**
	 * A string displayed in the menu to select help for creating a rule.
	 */
	public final static String WRITE_YOUR_OWN_RULE = "Creating Your Own Rule";

	/**
	 * A string used for the "Zoom In" selection in the menu and also used to
	 * notify other classes that a "zoom in" event has been fired. In other
	 * words, the "zoom in" button was selected.
	 */
	public final static String ZOOM_IN = "Zoom In";

	/**
	 * A string used for the "Zoom Out" selection in the menu and also used to
	 * notify other classes that a "zoom out" event has been fired. In other
	 * words, the "zoom out" button was selected.
	 */
	public final static String ZOOM_OUT = "Zoom Out";

	// the amount by which the simulation speed is increased or decreased
	private final static double SPEED_FACTOR = 2.0;

	// length of time it takes for the animation to swap sides of the layout.
	private final static int FLIP_ANIMATION_LENGTH = 300;

	// String for a tool tip about opening hyperlinks in a separate browser or
	// in this application.
	private final static String DISPLAY_LINKS_TIP = "Hyperlinks may be opened within this "
			+ "application or in a separate browser.";

	// String for a tool tip about loading data
	private final static String IMPORT_DATA_TIP = "<html>"
			+ "Loads data into the current simulation. Simulation properties <br>"
			+ "remain unchanged. <br><br>"
			+ "The data file must have the correct number of rows, columns, <br>"
			+ "and states. If rows or columns are incorrect, the data will be <br>"
			+ "padded when possible. Extra states are ignored when possible. <br><br>"
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

	// String for a tool tip about loading an image
	private final static String IMPORT_IMAGE_TIP = "<html>Loads an image into the current "
			+ "simulation. <br>"
			+ "Simulation properties remain unchanged.</html>";

	// String for a tool tip about importing an entire simulation
	private final static String IMPORT_SIMULATION_TIP = "<html>"
			+ "Replaces the current simulation with a new one. <br><br> "
			+ "All graphics, properties, and data are replaced. <br>"
			+ "Use this to load previously saved simulations.</html>";

	// tool tip for selecting the number of processors that will be used
	public final static String NUMBER_OF_PROCESSORS_TIP = "<html><body>"
			+ "Select the number of processors that will be used for <br>"
			+ "parallel processing. Generally, one or more processors <br>"
			+ "should be set aside for garbage collection, graphics, <br>"
			+ "and the operating system. <br><br>"
			+ "<b>More is NOT always better</b>. For example, a computer <br>"
			+ "with two quad-cores may run fastest with only 4 of the 8 <br>"
			+ "available processors. Cross-talk overhead between the  <br>"
			+ "two chips may make 5 or more run slowly (bummer!). <br><br>"
			+ "Also, small simulations incur greater overhead from <br>"
			+ "thread management, while large simulations have less <br>"
			+ "overhead relative to the number of cells. Therefore, <br>"
			+ "large simulations benefit most from extra processors. <br><br>"
			+ "<b>To select the optimum number of processors</b> for a <br>"
			+ "given simulation, use the Speed of Simulation <br>"
			+ "analysis. Remember to re-evaluate this number any <br>"
			+ "time a simulation parameter is changed. Give the <br>"
			+ "Hotspot JVM time to warm up before changing the <br>"
			+ "number of processors. The JVM gets more efficient <br>"
			+ "after it has a chance to see what code can be optimized. <br><br>"
			+ "Do not expect miracles. Amdahl's law limits the extent <br>"
			+ "to which parallelization can speed the simulation. For <br>"
			+ "fastest simulations, turn off all analyses, and only <br>"
			+ "display graphics at the end of the simulation (see the <br>"
			+ "Controls tab). <br><br>"
			+ "For faster simulations shut down all other applications.</body></html>";

	// tool tip when parallel processing is not available
	private final static String ONLY_ONE_PROCESSOR_TIP = "<html><body>"
			+ "Parallel processing is not available on your computer <br>"
			+ "because it has only one processor. <br><br>"
			+ "For faster simulations shut down all other applications.</body></html>";

	// Informational text displayed when the user selects a random rule. Note
	// that this text is incomplete html which should be completed with the name
	// of the rule and "\".</html>" before displaying.
	private final static String RANDOM_RULE_INFO_TEXT = "<html>"
			+ "Selecting a random rule on a random lattice. <br><br>"
			+ "For best results, you may wish to adjust other properties <br> "
			+ "before submitting. Please see the panel titled \"For best <br> "
			+ "results with ";

	// Informational text displayed when the user selects a random rule. Note
	// that this text is incomplete html which should be completed with the name
	// of the rule and "\".</html>" before displaying.
	private final static String RANDOM_FACADE_RULE_INFO_TEXT = "<html>"
			+ "Selecting a random rule. <br><br>"
			+ "And the winner is... <br><br>";

	// String for a tool tip about "Random Rule" selection.
	private final static String RANDOM_TIP = "Selects a random rule.";

	// String for a tool tip about "Reset Application" selection.
	private final static String RESET_APPLICATION_TIP = "Resets the application to its default "
			+ "state (Life, on a "
			+ CurrentProperties.getInstance().getDefaultCAProperties()
					.getProperty(CurrentProperties.CA_WIDTH)
			+ " by "
			+ CurrentProperties.getInstance().getDefaultCAProperties()
					.getProperty(CurrentProperties.CA_HEIGHT) + " grid, etc.).";

	// String for a tool tip about "Save As..." and "Save".
	private final static String SAVE_AS_TIP = "Saves the simulation in a .ca file, including "
			+ "properties, data, and graphics.";

	/**
	 * String for the menu item that indicates the easy facade should be turned
	 * on. Hides most functionality to make choices easier.
	 */
	public final static String SHOW_EASY_FACADE = "Simplify Interface (\"EZ\")";

	/**
	 * Tool tip for turning on the facade and making the application easier to
	 * use.
	 */
	public final static String SHOW_EASY_FACADE_TIP = "<html><body><b>Simplify the "
			+ "program!</b>"
			+ "<br>Makes the application easier to use."
			+ "<br>Good for sightseeing and newcomers.</body></html>";

	/**
	 * Menu item for turning off the facade and showing all options.
	 */
	public final static String SHOW_FULL_INTERFACE = "Show Full Interface (\"All\")";

	/**
	 * Tool tip for turning off the facade and showing all options.
	 */
	public final static String SHOW_FULL_INTERFACE_TIP = "<html><body><b>Show full "
			+ "program.</b>"
			+ "<br>Allows complete control over a simulation. "
			+ "<br>All menu options are available.</body></html>";

	// String for a tool tip about incrementing the simulation
	private final static String STEP1_TIP = "Increments by one generation.";

	// String for a tool tip about starting the simulation
	private final static String START_TIP = "Starts the simulation.";

	// String for a tool tip about stepping 10 steps forward
	private final static String STEP10_TIP = "Increments by 10 generations.";

	// String for a tool tip about stepping back one generation
	private final static String STEP_BACK_TIP = "Steps one generation backward. "
			+ "Not always available.";

	// String for a tool tip about stepping forward to fill the grid
	private final static String STEP_FILL_TIP = "In one-dimension, fills the "
			+ "grid. Increments by roughly the number of rows.";

	// String for a tool tip about starting the simulation
	private final static String STOP_TIP = "Stops the simulation.";

	// String indicating that tagged cells should be invisible
	private final static String TAGGED_INVISIBLE = "Do not show tagged cells";

	// String indicating that tagged cells should be shown as opaque
	private final static String TAGGED_OPAQUE = "Show tagged cells as opaque";

	// Tool tip that tagged cells should be shown as invisible
	private final static String TAGGED_INVISIBLE_TIP = "No tagging colors will be visible. "
			+ "It will not be possible to see which cells are tagged.";

	// Tool tip that tagged cells should be shown as opaque
	private final static String TAGGED_OPAQUE_TIP = "The cell's color will not shine "
			+ "through the tagged color. Tagged colors will not be transparent.";

	// String indicating that tagged cells should be shown as translucent
	private final static String TAGGED_TRANSLUCENT = "Show tagged cells as translucent";

	// Tool tip that tagged cells should be shown as translucent
	private final static String TAGGED_TRANSLUCENT_TIP = "The cell's color will "
			+ "shine through the tagged color. Tagged colors will be partially transparent.";

	// String indicating that the grid mesh should be turned off
	private final static String TOGGLE_MESH_OFF = "Turn Off Grid Mesh";

	// String indicating that the grid mesh should be turned on
	private final static String TOGGLE_MESH_ON = "Turn On Grid Mesh";

	// String for a tool tip about toggling the grid mesh
	private final static String TOGGLE_MESH_TIP = "Turning on the grid "
			+ "may slow \"drawing\" with the mouse on large grids.";

	// The directory where the file chooser will first open. (When null, will be
	// my documents or the equivalent).
	private static File startDirectory = null;

	// when true, the "toggle mesh" menu item will display the grid mesh. If
	// false, will hide the mesh.
	private boolean meshVisible = false;

	// true if the layout is arranged with the controls on the left.
	private boolean isLeftLayout = true;

	// the CA graphics object with its update() method that can be called from
	// here to refresh the CA graphics (for example, when a new "empty" color is
	// selected)
	private CAFrame graphics = null;

	// a color chooser for selecting integer states (drawing colors)
	private static IntegerStateColorChooser integerColorChooser = null;

	// a color chooser for empty and filled sites
	private static JColorChooser colorChooser = new JColorChooser();

	// the number of times the user has clicked the EZ facade button
	private static int ezClickCount = 0;

	// the number of times the user has clicked the ALL interface button
	private static int fullInterfaceClickCount = 0;

	// the number of times the warnings will be displayed when the user keeps
	// clicking the same facade or full interface button
	private static final int NUM_FACADE_WARNINGS = 5;

	// the number of processors on the computer
	private final static int NUMBER_OF_AVAILABLE_PROCESSORS = Runtime
			.getRuntime().availableProcessors();

	// dialog for the color chooser
	private JDialog colorDialog = null;

	// the menu item for selecting default colors
	private JMenuItem defaultColorSchemeItem = null;

	// menu items for selecting various color schemes
	private JMenuItem fireColorSchemeItem = null;

	private JMenuItem blueDiamondColorSchemeItem = null;

	private JMenuItem blueShadesColorSchemeItem = null;

	private JMenuItem greenOceanColorSchemeItem = null;

	private JMenuItem waterLilliesColorSchemeItem = null;

	private JMenuItem purpleHazeColorSchemeItem = null;

	private JMenuItem graySmokeColorSchemeItem = null;

	private JMenuItem blackAndWhiteColorSchemeItem = null;

	private JMenuItem whiteAndBlackColorSchemeItem = null;

	private JMenuItem randomColorSchemeItem = null;

	private JMenuItem yellowJacketColorSchemeItem = null;

	// whether to display hyperlinks in this application
	private JMenuItem displayLinksInApplicationItem = null;

	// whether to display hyperlinks in a separate browser
	private JMenuItem displayLinksInBrowserItem = null;

	// the menu item for selecting the empty color
	private JMenuItem emptyItem = null;

	// the menu item for selecting the filled color
	private JMenuItem filledItem = null;

	// flip the layout
	private JMenuItem flipLayoutItem = null;

	// the help menu item
	private JMenuItem helpItem = null;

	// the left-click draw color menu item
	private JMenuItem leftDrawItem = null;

	// the turn on/off grid mesh item
	private JMenuItem meshItem = null;

	// reset the application to its "out of the box" settings
	private JMenuItem resetApplicationItem = null;

	// menu item that restores the default colors
	private JMenuItem restoreColorItem = null;

	// the right-click draw color menu item
	private JMenuItem rightDrawItem = null;

	// The stop movie item
	private JMenuItem stopMovieItem = null;

	// the Save menu item
	private JMenuItem saveItem = null;

	// The save as a movie item
	private JMenuItem saveAsMovieItem = null;

	// the "simple facade" menu item
	private JMenuItem showEasyFacadeItem = null;

	// the "show full interface" menu item
	private JMenuItem showFullInterfaceItem = null;

	// the start menu item
	private JMenuItem startItem = null;

	// the increment menu item
	private JMenuItem step1Item = null;

	// the step 10 menu item
	private JMenuItem step10Item = null;

	// the step back menu item
	private JMenuItem stepBackItem = null;

	// the step until lattice is filled menu item
	private JMenuItem stepFillItem = null;

	// the stop menu item
	private JMenuItem stopItem = null;

	// the menu item that gives advice on writing your own rule.
	private JMenuItem writeYourOwnRuleItem = null;

	// the "Zoom in" menu item
	private JMenuItem zoomInItem = null;

	// the "Zoom out" menu item
	private JMenuItem zoomOutItem = null;

	// the sub menu for color schemes
	private JMenu colorSchemeSubMenu = null;

	// the sub menu for choosing the number of processors when there are <= 16
	private JMenu numberOfProcessorsSubMenu = null;

	// an array of items, one for each possible number of processors used in
	// parallel processing
	private JMenuItem[] numberOfProcessorsItem = null;

	// A string used for the "Choose ctrl-click drawing color" selection in the
	// menu.
	private String ctrlClickDrawColorString = CHOOSE_CTRLCLICK_DRAW_COLOR;

	// A string used for the "Choose right-click drawing color" selection in the
	// menu.
	private String rightClickDrawColorString = CHOOSE_RIGHTCLICK_DRAW_COLOR;

	/**
	 * Create a menu bar for the CA.
	 */
	public CAMenuBar(Lattice lattice, CAFrame graphics)
	{
		this.graphics = graphics;

		this.setOpaque(true);

		// get the folder where files will be saved
		startDirectory = new File(CurrentProperties.getInstance()
				.getSaveDataFilePath());

		// change the name for mac systems (because they have no concept
		// of left or right clicking)
		if(CAConstants.MAC_OS)
		{
			CHOOSE_DRAW_COLOR = "Choose Drawing Color";
		}

		// change the name of the browser for Macs.
		if(CAConstants.MAC_OS)
		{
			DISPLAY_LINKS_IN_BROWSER = "Open Hyperlinks in Safari";
		}

		// Create an (unused) file chooser now. Doing this preloads the
		// JFileChooser class and makes the response time much, much, much
		// faster when a JFileChooser is needed later. We don't even have
		// to save a reference to it -- just pre-load the class.
		new CAFileChooser();

		// create the menu bar
		createMenuBar();

		// set the colors from the properties
		setColorsFromProperties();

		// REMOVED because profiler says this is very expensive. Instead,
		// notified by the ControllerListener.
		// 
		// add this class as a listener to each cell -- that way, if
		// the cell changes state, then this class knows to enable the
		// "Save" menu item.
		// Iterator iterator = lattice.iterator();
		// while(iterator.hasNext())
		// {
		// ((Cell) iterator.next()).addPropertyChangeListener(this);
		// }

		// this handles ctrl-click events so that they are not missed. The
		// user selects ctrl-Something to activate a dialog. But if they release
		// the ctrl key while the dialog has focus, then the ctrl-release
		// event is not heard -- unless I bind the ctrl key to the
		// dialog window. This is a work-around for Macs (replaces
		// right-clicks with ctrl-clicks).
		CAFrame.bindCtrlKey(colorChooser);
	}

	/**
	 * Menu that lets the user select the number of processors that will be
	 * used. Used when there are more less than or equal to 16 processors.
	 * 
	 * @return The menu for choosing the number of parallel processors.
	 */
	private JMenu buildNumberOfProcessersMenu()
	{
		JMenu numProcessorsMenu = new JMenu(SET_NUMBER_OF_PROCESSORS);

		numberOfProcessorsItem = new JRadioButtonMenuItem[NUMBER_OF_AVAILABLE_PROCESSORS];

		// ensures that no more than one member of the group can be selected
		ButtonGroup processorButtonGroup = new ButtonGroup();

		// create the submenu items
		for(int i = 0; i < NUMBER_OF_AVAILABLE_PROCESSORS; i++)
		{
			// the name of the menu item.
			// NOTE: IF CHANGE THE itemName, THEN MUST ALSO CHANGE THIS NAME IN
			// THE actionPerformed() METHOD!
			String itemName = "use " + (i + 1) + " processors";
			if(i == 0)
			{
				itemName = "use " + (i + 1) + " processor";
			}

			// create the item
			numberOfProcessorsItem[i] = new JRadioButtonMenuItem(itemName);
			numProcessorsMenu.add(numberOfProcessorsItem[i]);
			numberOfProcessorsItem[i].addActionListener(this);
			numberOfProcessorsItem[i].setToolTipText(NUMBER_OF_PROCESSORS_TIP);

			// add to the button group so that only one of the items can be
			// selected at any time
			processorButtonGroup.add(numberOfProcessorsItem[i]);
		}

		// select the number of processors that was used when the program was
		// last run
		numberOfProcessorsItem[CurrentProperties.getInstance()
				.getNumberOfProcessors() - 1].setSelected(true);

		return numProcessorsMenu;
	}

	/**
	 * Create menu items for the menu bar.
	 */
	private void createMenuBar()
	{
		// the file menu
		JMenu fileMenu = new JMenu("File");

		saveItem = new JMenuItem(SAVE);
		JMenuItem saveAsItem = new JMenuItem(SAVE_AS);
		JMenuItem saveAsImageItem = new JMenuItem(SAVE_AS_IMAGE);
		saveAsMovieItem = new JMenuItem(SAVE_AS_MOVIE);
		stopMovieItem = new JMenuItem(STOP_MOVIE);
		JMenuItem printItem = new JMenuItem(PRINT);
		JMenuItem importSimulationItem = new JMenuItem(IMPORT_SIMULATION);
		JMenuItem importDataItem = new JMenuItem(IMPORT_DATA);
		JMenuItem importImageItem = new JMenuItem(IMPORT_DATA_IMAGE);
		JMenuItem exitItem = new JMenuItem(EXIT);

		// tool tips
		importSimulationItem.setToolTipText(IMPORT_SIMULATION_TIP);
		importDataItem.setToolTipText(IMPORT_DATA_TIP);
		importImageItem.setToolTipText(IMPORT_IMAGE_TIP);
		saveItem.setToolTipText(SAVE_AS_TIP);
		saveAsItem.setToolTipText(SAVE_AS_TIP);

		// nothing to save at first
		saveItem.setEnabled(false);
		stopMovieItem.setEnabled(false);

		// create accelerators
		int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, mask));
		saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, mask
				+ ActionEvent.SHIFT_MASK));
		printItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, mask));

		saveItem.addActionListener(this);
		saveAsItem.addActionListener(this);
		saveAsImageItem.addActionListener(this);
		saveAsMovieItem.addActionListener(this);
		stopMovieItem.addActionListener(this);
		printItem.addActionListener(this);
		importSimulationItem.addActionListener(this);
		importDataItem.addActionListener(this);
		importImageItem.addActionListener(this);
		exitItem.addActionListener(this);

		fileMenu.add(saveItem);
		fileMenu.add(saveAsItem);
		fileMenu.add(saveAsImageItem);
		fileMenu.add(saveAsMovieItem);
		fileMenu.add(stopMovieItem);
		fileMenu.addSeparator();
		fileMenu.add(printItem);
		fileMenu.addSeparator();
		fileMenu.add(importSimulationItem);
		fileMenu.add(importDataItem);
		fileMenu.add(importImageItem);

		// only add these if not on Mac OS X
		if(!CAConstants.MAC_OS_X)
		{
			fileMenu.addSeparator();
			fileMenu.add(exitItem);
		}

		// the view menu
		JMenu viewMenu = new JMenu("View");
		colorSchemeSubMenu = new JMenu("Color Scheme");
		JMenu taggedColorSubMenu = new JMenu("Tagged Colors");

		leftDrawItem = new JMenuItem(CHOOSE_DRAW_COLOR);
		if(!CAConstants.MAC_OS)
		{
			// only use this string on systems that are guaranteed to have a
			// right-clicking mouse
			rightDrawItem = new JMenuItem(rightClickDrawColorString);
		}
		else
		{
			// use this string on systems that are NOT guaranteed to have a
			// right-clicking mouse (macs)
			rightDrawItem = new JMenuItem(ctrlClickDrawColorString);
		}
		rightDrawItem.setActionCommand(CHOOSE_RIGHTCLICK_DRAW_COLOR);
		emptyItem = new JMenuItem(CHOOSE_EMPTY_COLOR);
		filledItem = new JMenuItem(CHOOSE_FILLED_COLOR);
		restoreColorItem = new JMenuItem(RESTORE_DEFAULT_COLOR);
		flipLayoutItem = new JMenuItem(MOVE_CONTROLS);
		zoomInItem = new JMenuItem(ZOOM_IN);
		zoomOutItem = new JMenuItem(ZOOM_OUT);
		JMenuItem fitToScreenItem = new JMenuItem(FIT_TO_SCREEN);
		meshItem = new JMenuItem(TOGGLE_MESH_ON);
		meshItem.setActionCommand(TOGGLE_MESH);
		meshItem.setToolTipText(TOGGLE_MESH_TIP);

		defaultColorSchemeItem = new JRadioButtonMenuItem(
				CHOOSE_DEFAULT_COLOR_SCHEME);
		fireColorSchemeItem = new JRadioButtonMenuItem(CHOOSE_FIRE_COLOR_SCHEME);
		blueDiamondColorSchemeItem = new JRadioButtonMenuItem(
				CHOOSE_BLUE_DIAMOND_COLOR_SCHEME);
		blueShadesColorSchemeItem = new JRadioButtonMenuItem(
				CHOOSE_BLUE_SHADES_COLOR_SCHEME);
		greenOceanColorSchemeItem = new JRadioButtonMenuItem(
				CHOOSE_GREEN_OCEAN_COLOR_SCHEME);
		waterLilliesColorSchemeItem = new JRadioButtonMenuItem(
				CHOOSE_WATER_LILIES_COLOR_SCHEME);
		purpleHazeColorSchemeItem = new JRadioButtonMenuItem(
				CHOOSE_PURPLE_HAZE_COLOR_SCHEME);
		graySmokeColorSchemeItem = new JRadioButtonMenuItem(
				CHOOSE_GRAY_SMOKE_COLOR_SCHEME);
		blackAndWhiteColorSchemeItem = new JRadioButtonMenuItem(
				CHOOSE_BLACK_AND_WHITE_COLOR_SCHEME);
		whiteAndBlackColorSchemeItem = new JRadioButtonMenuItem(
				CHOOSE_WHITE_AND_BLACK_COLOR_SCHEME);
		randomColorSchemeItem = new JRadioButtonMenuItem(
				CHOOSE_RANDOM_COLOR_SCHEME);
		yellowJacketColorSchemeItem = new JRadioButtonMenuItem(
				CHOOSE_YELLOW_JACKET_COLOR_SCHEME);

		ButtonGroup colorButtonGroup = new ButtonGroup();
		colorButtonGroup.add(defaultColorSchemeItem);
		colorButtonGroup.add(fireColorSchemeItem);
		colorButtonGroup.add(blueShadesColorSchemeItem);
		colorButtonGroup.add(greenOceanColorSchemeItem);
		colorButtonGroup.add(waterLilliesColorSchemeItem);
		colorButtonGroup.add(purpleHazeColorSchemeItem);
		colorButtonGroup.add(blueDiamondColorSchemeItem);
		colorButtonGroup.add(graySmokeColorSchemeItem);
		colorButtonGroup.add(blackAndWhiteColorSchemeItem);
		colorButtonGroup.add(whiteAndBlackColorSchemeItem);
		colorButtonGroup.add(randomColorSchemeItem);
		colorButtonGroup.add(yellowJacketColorSchemeItem);

		defaultColorSchemeItem.setSelected(true);

		// submenu for tagged cells being opaque, transparent, or invisible
		JMenuItem taggedTranslucentItem = new JRadioButtonMenuItem(
				TAGGED_TRANSLUCENT);
		taggedTranslucentItem.setToolTipText(TAGGED_TRANSLUCENT_TIP);
		JMenuItem taggedOpaqueItem = new JRadioButtonMenuItem(TAGGED_OPAQUE);
		taggedOpaqueItem.setToolTipText(TAGGED_OPAQUE_TIP);
		JMenuItem taggedInvisibleItem = new JRadioButtonMenuItem(
				TAGGED_INVISIBLE);
		taggedInvisibleItem.setToolTipText(TAGGED_INVISIBLE_TIP);
		ButtonGroup taggedButtonGroup = new ButtonGroup();
		taggedButtonGroup.add(taggedTranslucentItem);
		taggedButtonGroup.add(taggedOpaqueItem);
		taggedButtonGroup.add(taggedInvisibleItem);
		taggedTranslucentItem.setSelected(true);

		// decide whether or not the drawItems should be enabled.
		String currentRuleClassName = CurrentProperties.getInstance()
				.getRuleClassName();
		Rule currentRule = ReflectionTool
				.instantiateMinimalRuleFromClassName(currentRuleClassName);
		if(IntegerCellState.isCompatibleRule(currentRule))
		{
			leftDrawItem.setEnabled(true);
			rightDrawItem.setEnabled(true);
		}
		else
		{
			leftDrawItem.setEnabled(false);
			rightDrawItem.setEnabled(false);
		}

		leftDrawItem.addActionListener(this);
		rightDrawItem.addActionListener(this);
		emptyItem.addActionListener(this);
		filledItem.addActionListener(this);
		restoreColorItem.addActionListener(this);
		zoomInItem.addActionListener(this);
		zoomOutItem.addActionListener(this);
		fitToScreenItem.addActionListener(this);
		meshItem.addActionListener(this);
		defaultColorSchemeItem.addActionListener(this);
		fireColorSchemeItem.addActionListener(this);
		blueDiamondColorSchemeItem.addActionListener(this);
		blueShadesColorSchemeItem.addActionListener(this);
		greenOceanColorSchemeItem.addActionListener(this);
		waterLilliesColorSchemeItem.addActionListener(this);
		purpleHazeColorSchemeItem.addActionListener(this);
		graySmokeColorSchemeItem.addActionListener(this);
		blackAndWhiteColorSchemeItem.addActionListener(this);
		whiteAndBlackColorSchemeItem.addActionListener(this);
		randomColorSchemeItem.addActionListener(this);
		yellowJacketColorSchemeItem.addActionListener(this);
		taggedTranslucentItem.addActionListener(this);
		taggedOpaqueItem.addActionListener(this);
		taggedInvisibleItem.addActionListener(this);
		flipLayoutItem.addActionListener(this);

		viewMenu.addSeparator();
		viewMenu.add(leftDrawItem);
		viewMenu.add(rightDrawItem);
		viewMenu.add(emptyItem);
		viewMenu.add(filledItem);
		viewMenu.add(restoreColorItem);
		viewMenu.addSeparator();
		colorSchemeSubMenu.add(defaultColorSchemeItem);
		colorSchemeSubMenu.add(fireColorSchemeItem);
		colorSchemeSubMenu.add(blueShadesColorSchemeItem);
		colorSchemeSubMenu.add(greenOceanColorSchemeItem);
		colorSchemeSubMenu.add(yellowJacketColorSchemeItem);
		colorSchemeSubMenu.add(waterLilliesColorSchemeItem);
		colorSchemeSubMenu.add(purpleHazeColorSchemeItem);
		colorSchemeSubMenu.add(blueDiamondColorSchemeItem);
		colorSchemeSubMenu.add(blackAndWhiteColorSchemeItem);
		colorSchemeSubMenu.add(whiteAndBlackColorSchemeItem);
		colorSchemeSubMenu.add(graySmokeColorSchemeItem);
		colorSchemeSubMenu.add(randomColorSchemeItem);
		viewMenu.add(colorSchemeSubMenu);
		viewMenu.addSeparator();
		taggedColorSubMenu.add(taggedTranslucentItem);
		taggedColorSubMenu.add(taggedOpaqueItem);
		taggedColorSubMenu.add(taggedInvisibleItem);
		viewMenu.add(taggedColorSubMenu);
		viewMenu.addSeparator();
		viewMenu.add(zoomInItem);
		viewMenu.add(zoomOutItem);
		viewMenu.add(fitToScreenItem);
		viewMenu.addSeparator();
		viewMenu.add(meshItem);
		viewMenu.addSeparator();
		viewMenu.add(flipLayoutItem);

		// the system properties menu
		JMenu systemMenu = new JMenu("System");
		displayLinksInBrowserItem = new JRadioButtonMenuItem(
				DISPLAY_LINKS_IN_BROWSER);
		displayLinksInApplicationItem = new JRadioButtonMenuItem(
				DISPLAY_LINKS_IN_APPLICATION);

		ButtonGroup hyperlinkButtonGroup = new ButtonGroup();
		hyperlinkButtonGroup.add(displayLinksInBrowserItem);
		hyperlinkButtonGroup.add(displayLinksInApplicationItem);

		// select whichever is in the properties file
		boolean displayInBrowser = CurrentProperties.getInstance()
				.isDisplayHyperLinksInBrowser();
		if(displayInBrowser)
		{
			displayLinksInBrowserItem.setSelected(true);
		}
		else
		{
			displayLinksInApplicationItem.setSelected(true);
		}

		displayLinksInBrowserItem.setToolTipText(DISPLAY_LINKS_TIP);
		displayLinksInApplicationItem.setToolTipText(DISPLAY_LINKS_TIP);
		JMenuItem delimeterItem = new JMenuItem(CHOOSE_FILE_DELIMETER);
		JMenuItem maxRuleSize = new JMenuItem(SET_MAX_RULE_DIGITS);
		resetApplicationItem = new JMenuItem(RESET_APPLICATION);
		resetApplicationItem.setToolTipText(RESET_APPLICATION_TIP);

		if(NUMBER_OF_AVAILABLE_PROCESSORS <= 16)
		{
			numberOfProcessorsSubMenu = buildNumberOfProcessersMenu();
			if(NUMBER_OF_AVAILABLE_PROCESSORS <= 1)
			{
				// parallel processing is not available
				numberOfProcessorsSubMenu.setEnabled(false);
				numberOfProcessorsSubMenu
						.setToolTipText(ONLY_ONE_PROCESSOR_TIP);
			}
			else
			{
				numberOfProcessorsSubMenu
						.setToolTipText(NUMBER_OF_PROCESSORS_TIP);
			}
			systemMenu.add(numberOfProcessorsSubMenu);
		}
		else
		{
			JMenuItem numberOfProcessorsPopupItem = new JMenuItem(
					SET_NUM_PROCESSORS_FROM_POPUP);
			numberOfProcessorsPopupItem
					.setToolTipText(NUMBER_OF_PROCESSORS_TIP);
			numberOfProcessorsPopupItem.addActionListener(this);
			systemMenu.add(numberOfProcessorsPopupItem);
		}

		displayLinksInBrowserItem.addActionListener(this);
		displayLinksInApplicationItem.addActionListener(this);
		delimeterItem.addActionListener(this);
		maxRuleSize.addActionListener(this);
		resetApplicationItem.addActionListener(this);
		// ruleFolderItem.addActionListener(this);

		systemMenu.addSeparator();
		systemMenu.add(displayLinksInApplicationItem);
		systemMenu.add(displayLinksInBrowserItem);
		systemMenu.addSeparator();
		systemMenu.add(delimeterItem);
		systemMenu.add(maxRuleSize);
		systemMenu.add(resetApplicationItem);
		// systemMenu.add(ruleFolderItem);

		// the action menu
		JMenu actionMenu = new JMenu("Action");
		startItem = new JMenuItem(START);
		step1Item = new JMenuItem(STEP1);
		stopItem = new JMenuItem(STOP);
		step10Item = new JMenuItem(STEP10);
		stepBackItem = new JMenuItem(STEP_BACK);
		stepFillItem = new JMenuItem(STEP_FILL);
		JMenuItem randomItem = new JMenuItem(RANDOM);

		startItem.setToolTipText(START_TIP);
		stopItem.setToolTipText(STOP_TIP);
		step1Item.setToolTipText(STEP1_TIP);
		step10Item.setToolTipText(STEP10_TIP);
		stepBackItem.setToolTipText(STEP_BACK_TIP);
		stepFillItem.setToolTipText(STEP_FILL_TIP);
		randomItem.setToolTipText(RANDOM_TIP);

		startItem.setAccelerator(KeyStroke
				.getKeyStroke(KeyEvent.VK_RIGHT, mask));
		stopItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, mask));
		step1Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, mask));
		stepBackItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
				mask));

		startItem.addActionListener(this);
		stopItem.addActionListener(this);
		step1Item.addActionListener(this);
		step10Item.addActionListener(this);
		stepBackItem.addActionListener(this);
		stepFillItem.addActionListener(this);
		randomItem.addActionListener(this);

		actionMenu.add(startItem);
		actionMenu.add(stopItem);
		actionMenu.addSeparator();
		actionMenu.add(step1Item);
		actionMenu.add(step10Item);
		actionMenu.add(stepFillItem);
		actionMenu.addSeparator();
		actionMenu.add(stepBackItem);
		actionMenu.addSeparator();
		actionMenu.add(randomItem);

		// nothing to stop until started
		stopItem.setEnabled(false);

		// the "make easy" menu
		JMenu facadeMenu = new JMenu("Difficulty");

		showEasyFacadeItem = new JRadioButtonMenuItem(SHOW_EASY_FACADE);
		showEasyFacadeItem.setToolTipText(SHOW_EASY_FACADE_TIP);
		showFullInterfaceItem = new JRadioButtonMenuItem(SHOW_FULL_INTERFACE);
		showFullInterfaceItem.setToolTipText(SHOW_FULL_INTERFACE_TIP);
		ButtonGroup facadeButtonGroup = new ButtonGroup();
		facadeButtonGroup.add(showEasyFacadeItem);
		facadeButtonGroup.add(showFullInterfaceItem);
		showEasyFacadeItem.setSelected(CurrentProperties.getInstance()
				.isFacadeOn());
		showFullInterfaceItem.setSelected(!CurrentProperties.getInstance()
				.isFacadeOn());

		showEasyFacadeItem.addActionListener(this);
		showFullInterfaceItem.addActionListener(this);

		facadeMenu.add(showEasyFacadeItem);
		facadeMenu.add(showFullInterfaceItem);

		// the help menu
		JMenu helpMenu = new JMenu("Help");

		helpItem = new JMenuItem(HELP);
		JMenuItem gettingStartedItem = new JMenuItem(GETTING_STARTED);
		JMenuItem guidedTourItem = new JMenuItem(GUIDED_TOUR);
		JMenuItem obesityTourItem = new JMenuItem(OBESITY_TUTORIAL);
		JMenuItem memoryTutorialItem = new JMenuItem(MEMORY_TUTORIAL);
		writeYourOwnRuleItem = new JMenuItem(WRITE_YOUR_OWN_RULE);
		JMenuItem howManyRulesItem = new JMenuItem(HOW_MANY_RULES_ARE_THERE);
		JMenuItem showHiddenTabsItem = new JMenuItem(SHOW_HIDDEN_TABS);
		JMenuItem havePropertiesChangedItem = new JMenuItem(
				HAVE_PROPERTIES_CHANGED);
		JMenuItem ruleConstructorsItem = new JMenuItem(CA_RULE_CONSTRUCTORS);
		JMenuItem aboutItem = new JMenuItem(ABOUT);

		helpItem.addActionListener(this);
		gettingStartedItem.addActionListener(this);
		guidedTourItem.addActionListener(this);
		obesityTourItem.addActionListener(this);
		memoryTutorialItem.addActionListener(this);
		writeYourOwnRuleItem.addActionListener(this);
		showHiddenTabsItem.addActionListener(this);
		howManyRulesItem.addActionListener(this);
		havePropertiesChangedItem.addActionListener(this);
		ruleConstructorsItem.addActionListener(this);
		aboutItem.addActionListener(this);

		helpMenu.add(helpItem);
		helpMenu.add(gettingStartedItem);
		helpMenu.add(guidedTourItem);
		helpMenu.addSeparator();
		helpMenu.add(showHiddenTabsItem);
		helpMenu.add(howManyRulesItem);
		helpMenu.add(writeYourOwnRuleItem);
		helpMenu.addSeparator();
		helpMenu.add(obesityTourItem);
		helpMenu.addSeparator();
		helpMenu.add(memoryTutorialItem);
		helpMenu.add(havePropertiesChangedItem);
		helpMenu.add(ruleConstructorsItem);

		// only add these if not on Mac OS X
		if(!CAConstants.MAC_OS_X)
		{
			helpMenu.addSeparator();
			helpMenu.add(aboutItem);
		}

		// add menus to the menu bar
		this.add(fileMenu);
		this.add(viewMenu);
		this.add(systemMenu);
		this.add(actionMenu);
		this.add(facadeMenu);
		this.add(helpMenu);
	}

	/**
	 * Change the simulation speed to the next slower or faster speed. This
	 * method is called by the "rabbit" and turtle" buttons on the tool bar.
	 * 
	 * @param slowDown
	 *            True if the simulation should slow down, and false if the
	 *            simulation should speed up.
	 */
	private void changeSpeed(boolean slowDown)
	{
		// the slider on the control panel that sets the delay.
		JSlider delaySlider = graphics.getControlPanel().getStartPanel()
				.getDelaySlider();

		if(delaySlider != null)
		{
			// The milliseconds that the simulation can be delayed will be set
			// to one of the values in the delayOptions array. The delay will be
			// set to one of these values when the rabbit or turtle buttons are
			// pressed. The maximum delay is added from the slider. I am
			// assuming that the minimum will always be less than the second
			// element of the array (which is 10 at the time this comment was
			// written).
			int maxMilliseconds = graphics.getControlPanel().getStartPanel()
					.convertSliderValueToMilliSeconds(delaySlider.getMaximum());
			int minMilliseconds = graphics.getControlPanel().getStartPanel()
					.convertSliderValueToMilliSeconds(delaySlider.getMinimum());

			int[] delayOptions = {minMilliseconds, 10, 100, 1000, 2000,
					maxMilliseconds};

			// Find the delayOption array element that is closest to the current
			// sliderValue. (The delayOption values are the set of possible
			// delay values that are set by the "rabbit" and "turtle" buttons on
			// the tool bar.)
			int sliderValue = delaySlider.getValue();
			int sliderMillisecondsValue = graphics.getControlPanel()
					.getStartPanel().convertSliderValueToMilliSeconds(
							sliderValue);
			int delayIndex = 0;
			while(delayIndex <= delayOptions.length - 1
					&& delayOptions[delayIndex] < sliderMillisecondsValue)
			{
				delayIndex++;
			}
			if(delayIndex >= delayOptions.length)
			{
				delayIndex = delayOptions.length - 1;
			}
			if(delayIndex != 0)
			{
				// we've selected an index larger than the slider value. Is the
				// slider value closer to this index or the index below it?
				int differenceToAboveIndex = delayOptions[delayIndex]
						- sliderMillisecondsValue;
				int differenceToBelowIndex = sliderMillisecondsValue
						- delayOptions[delayIndex - 1];

				// choose the closer index
				if(differenceToAboveIndex > differenceToBelowIndex)
				{
					delayIndex--;
				}
			}

			// Based on the button pushed (turtle or rabbit) change the index
			// that specifies the array element in the delayOptions array. That
			// array element is the number of milliseconds that the simulation
			// will be delayed at every time step.
			if(slowDown)
			{
				// larger indices have bigger delays
				delayIndex++;
			}
			else
			{
				// smaller indices have shorter delays
				delayIndex--;
			}

			// make sure the array index isn't outside the possible range of
			// indices
			if(delayIndex < 0)
			{
				delayIndex = 0;
			}
			else if(delayIndex > delayOptions.length - 1)
			{
				delayIndex = delayOptions.length - 1;
			}

			// the number of milliseconds that the simulation will be delayed
			int millisecondDelay = delayOptions[delayIndex];

			// get the new slider value for the specified millisecond delay
			int newSliderValue = graphics.getControlPanel().getStartPanel()
					.convertMilliSecondsToSliderValue(millisecondDelay);

			// set the new value on the slider
			delaySlider.setValue(newSliderValue);

			// decide if the rabbit icon should be enabled
			graphics.getToolBar().getRabbitButton().setEnabled(
					delaySlider.getValue() != delaySlider.getMinimum());

			// decide if the turtle icon should be enabled
			boolean enableTurtle = (delaySlider.getValue() != delaySlider
					.getMaximum());
			graphics.getToolBar().getTurtleButton().setEnabled(enableTurtle);
		}
	}

	/**
	 * Change the simulation speed by the given factor.
	 * 
	 * @param factor
	 *            Changes the simulation speed by the specified factor.
	 * @deprecated in favor of changeSpeed(boolean slowDown)
	 */
	private void changeSpeed(double factor)
	{
		JSlider delaySlider = graphics.getControlPanel().getStartPanel()
				.getDelaySlider();

		if(delaySlider != null)
		{
			int sliderValue = delaySlider.getValue();
			int newSliderValue = (int) Math.round(sliderValue / factor);

			// if slowing down, but new value is still 0, then increase to 1.
			if(factor < 1.0 && newSliderValue == 0)
			{
				// slow down to 1 millisecond
				newSliderValue = graphics.getControlPanel().getStartPanel()
						.convertMilliSecondsToSliderValue(1);

				// unlikely but possible depending on the millisecond conversion
				if(newSliderValue == 0)
				{
					newSliderValue = 1;
				}
			}
			else if(factor < 1.0
					&& graphics.getControlPanel().getStartPanel()
							.convertSliderValueToMilliSeconds(sliderValue) < 2
					&& graphics.getControlPanel().getStartPanel()
							.convertSliderValueToMilliSeconds(sliderValue) >= 0)
			{
				newSliderValue = graphics.getControlPanel().getStartPanel()
						.convertMilliSecondsToSliderValue(2);
			}
			else if(factor > 1.0
					&& graphics.getControlPanel().getStartPanel()
							.convertSliderValueToMilliSeconds(sliderValue) < 2)
			{
				// force it to speed up if necessary
				newSliderValue = 0;
			}

			// set the new value on the slider
			delaySlider.setValue(newSliderValue);

			// decide if the rabbit icon should be enabled
			if(delaySlider.getValue() == 0)
			{
				graphics.getToolBar().getRabbitButton().setEnabled(false);
			}
			else
			{
				graphics.getToolBar().getRabbitButton().setEnabled(true);
			}

			// decide if the turtle icon should be enabled
			if(delaySlider.getValue() == delaySlider.getMaximum())
			{
				graphics.getToolBar().getTurtleButton().setEnabled(false);
			}
			else
			{
				graphics.getToolBar().getTurtleButton().setEnabled(true);
			}
		}
	}

	/**
	 * Actions to take when "Choose left-click draw color" is selected.
	 */
	private void chooseDrawColor()
	{
		// pause the simulation.
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));

		int numberOfStates = CurrentProperties.getInstance().getNumStates();

		// the color to display in the preview panel
		Color color = ColorScheme.DRAW_COLOR;

		// get the current state value to display
		int stateValue = IntegerCellState.DRAW_STATE;

		// get the current drawing color (it may not match the DRAW_STATE if the
		// color scheme changed)
		if(stateValue == -1)
		{
			// then we haven't set a color with the chooser. So use the default
			// draw state.
			stateValue = numberOfStates - 1;

			// now set the color
			color = Cell.getView().getDisplayColor(
					new IntegerCellState(stateValue), null,
					new Coordinate(0, 0));
		}
		// else
		// {
		// color = view.getDisplayColor(new IntegerCellState(numberOfStates,
		// stateValue, view), null);
		// }

		integerColorChooser = new IntegerStateColorChooser(graphics.getFrame(),
				numberOfStates, stateValue, color, new OkDrawingColorListener(
						CHOOSE_DRAW_COLOR));

		integerColorChooser.setVisible(true);

		// restart the simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(false)));
	}

	/**
	 * Actions to take when "Choose empty color" is selected.
	 */
	private void chooseEmptyColor()
	{
		// pause the simulation.
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));

		// set the current color
		colorChooser.setColor(ColorScheme.EMPTY_COLOR);

		// a dialog for the color chooser
		colorDialog = JColorChooser.createDialog(this, CHOOSE_EMPTY_COLOR,
				true, colorChooser, new OkFilledAndEmptyColorListener(
						CHOOSE_EMPTY_COLOR), new CancelListener());

		colorDialog.setVisible(true);

		// the following deals with a Java bug in JColorChooser.createDialog().
		recreateColorChooser();

		// let everyone know that we just changed the colors of the graphics
		// (for example, the Analyses may need to respond)
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.COLORS_CHANGED, null, new Boolean(true)));

		// restart the simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(false)));
	}

	/**
	 * Chooses the character used to separate data in a file.
	 */
	private void chooseFileDelimeter()
	{
		// pause the simulation.
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));

		// make the JFrame look disabled
		graphics.setViewDisabled(true);

		// get the currently used delimeters
		String currentDelimeters = CurrentProperties.getInstance()
				.getDataDelimiters();

		// escape any "\t" and "\n" so they show up
		currentDelimeters = currentDelimeters.replaceAll("\\t", "\\\\t");
		currentDelimeters = currentDelimeters.replaceAll("\\n", "\\\\n");

		String message = "Select data delimeter(s) used when reading and "
				+ "writing files. \n\n"
				+ "More than one may be combined.  Typical choices include \n"
				+ "a space \" \", a tab typed as \"\\t\", or a comma \",\". \n\n"
				+ "WARNING: Changing the delimiter may make some \"."
				+ CAConstants.CA_FILE_EXTENSION + "\" \n"
				+ CAConstants.PROGRAM_TITLE + " files unreadable until the \n"
				+ "delimiter is changed back. \n\n"
				+ "If uncertain, use the default \"\\t\".\n";

		Object delimeters = JOptionPane.showInputDialog(graphics.getFrame(),
				message, "Data delimeter(s)", JOptionPane.OK_CANCEL_OPTION,
				null, null, currentDelimeters);

		// if they cancelled, will be null
		if(delimeters != null)
		{
			// unescape any tab and newline, so they work
			String sDelimeters = (String) delimeters;
			sDelimeters = sDelimeters.replaceAll("\\\\t", "\t");
			sDelimeters = sDelimeters.replaceAll("\\\\n", "\n");

			CurrentProperties.getInstance().setDataDelimiters(sDelimeters);
		}

		// make the JFrame look enabled
		graphics.setViewDisabled(false);

		// restart the simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(false)));
	}

	/**
	 * Actions to take when "Choose filled color" is selected.
	 */
	private void chooseFilledColor()
	{
		// pause the simulation.
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));

		// set the current color
		colorChooser.setColor(ColorScheme.FILLED_COLOR);

		// a dialog for the color chooser
		colorDialog = JColorChooser.createDialog(this, CHOOSE_FILLED_COLOR,
				true, colorChooser, new OkFilledAndEmptyColorListener(
						CHOOSE_FILLED_COLOR), new CancelListener());

		colorDialog.setVisible(true);

		// the following deals with a Java bug in JColorChooser.createDialog().
		recreateColorChooser();

		// let everyone know that we just changed the colors of the graphics
		// (for example, the Analyses may need to respond)
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.COLORS_CHANGED, null, new Boolean(true)));

		// restart the simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(false)));
	}

	/**
	 * Menu that lets the user select the number of processors that will be
	 * used. Used when there are more than 16 processors.
	 * 
	 * @return The menu for choosing the number of parallel processors.
	 */
	private void chooseNumberOfProcessors()
	{
		// pause the simulation.
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));

		// make the JFrame look disabled
		graphics.setViewDisabled(true);

		// create a spinner
		int initialNumberOfProcessors = CurrentProperties.getInstance()
				.getNumberOfProcessors();
		SpinnerNumberModel columnModel = new SpinnerNumberModel(
				initialNumberOfProcessors, 1, Runtime.getRuntime()
						.availableProcessors(), 1);
		JSpinner processorSpinner = new JSpinner(columnModel);
		processorSpinner.setToolTipText(NUMBER_OF_PROCESSORS_TIP);

		// create an explanation
		JLabel explanation = new JLabel(
				"<html><body>Select the number of processors that "
						+ "will be used <br> for parallel processing.</body></html>");

		String explanationText = "<html><body>Generally, one or more processors "
				+ "should be set aside <br>"
				+ "for garbage collection, graphics, and the operating <br>"
				+ "system. <br><br>"
				+ "More is NOT always better. For example, a computer <br>"
				+ "with two quad-cores may run fastest with only 4 of the 8 <br>"
				+ "available processors. Cross-talk overhead between the <br>"
				+ "two chips may make 5 or more run slowly (bummer!). <br><br>"
				+ "Also, small simulations incur greater overhead from <br>"
				+ "thread management, while large simulations have less <br>"
				+ "overhead relative to the number of cells. Therefore, <br>"
				+ "large simulations benefit most from extra processors. <br>"
				+ "To select the optimum number of processors for a <br>"
				+ "given simulation, use the Speed of Simulation <br>"
				+ "analysis. Remember to re-evaluate this number any <br>"
				+ "time a simulation parameter is changed. Give the <br>"
				+ "Hotspot JVM time to warm up before changing the <br>"
				+ "number of processors. The JVM gets more efficient <br>"
				+ "after it has a chance to see what code can be optimized. <br><br>"
				+ "Do not expect miracles. Amdahl's law limits the extent <br>"
				+ "to which parallelization can speed the simulation. For <br>"
				+ "fastest simulations, turn off all analyses, and only <br>"
				+ "display graphics at the end of the simulation (see the <br>"
				+ "Controls tab). <br><br>"
				+ "For faster simulations shut down all other applications.</body></html>";

		JLabel moreExplanation = new JLabel(explanationText);
		Fonts fonts = new Fonts();
		moreExplanation.setFont(fonts.getItalicSmallerFont());

		// create a display panel
		JPanel displayPanel = new JPanel(new GridBagLayout());
		int row = 0;
		displayPanel.add(new JLabel(" "), new GBC(1, row).setSpan(8, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		displayPanel.add(explanation, new GBC(1, row).setSpan(8, 1).setFill(
				GBC.BOTH).setWeight(1.0, 0.0).setAnchor(GBC.WEST).setInsets(1));

		row++;
		displayPanel.add(new JLabel(" "), new GBC(1, row).setSpan(8, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		displayPanel.add(moreExplanation, new GBC(1, row).setSpan(8, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		displayPanel.add(new JLabel(" "), new GBC(1, row).setSpan(8, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));

		row++;
		displayPanel.add(processorSpinner, new GBC(1, row).setSpan(8, 1)
				.setFill(GBC.NONE).setWeight(0.5, 0.0).setAnchor(GBC.CENTER)
				.setInsets(1));

		row++;
		displayPanel.add(new JLabel(" "), new GBC(1, row).setSpan(8, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// create an option dialog that displays the panel
		String finished = "Submit";
		JOptionPane optionDialog = new JOptionPane(displayPanel,
				JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null,
				new String[] {finished, "Cancel"}, null);

		javax.swing.JDialog dialog = optionDialog.createDialog(graphics
				.getFrame(), "Choose Number of Parallel Processors");
		dialog.setVisible(true);

		// did they submit a new value
		if(optionDialog.getValue() != null
				&& optionDialog.getValue().equals(finished))
		{
			try
			{
				processorSpinner.commitEdit();

				int numberOfProcessors = ((Integer) processorSpinner.getValue())
						.intValue();

				// submit the value
				CurrentProperties.getInstance().setNumberOfParallelProcessors(
						numberOfProcessors);
			}
			catch(Exception e)
			{
				String warningMessage = "The number of processors must be a number "
						+ "between 1 \n and "
						+ Runtime.getRuntime().availableProcessors()
						+ ", but you entered "
						+ processorSpinner.getValue()
						+ ". \n\nThe number of processors will not be changed.";

				JOptionPane.showMessageDialog(graphics.getFrame(),
						warningMessage, "Warning", JOptionPane.ERROR_MESSAGE);
			}
		}

		// make the JFrame look enabled
		graphics.setViewDisabled(false);

		// restart the simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(false)));
	}

	/**
	 * Actions to take when "Choose rule folder" is selected.
	 */
	private void chooseRuleFolder()
	{
		// pause the simulation.
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));

		// make the JFrame look disabled
		graphics.setViewDisabled(true);

		// change states, but first ask user if they are sure
		String message = "This chooses a folder where you are storing "
				+ "your own rules to be implemented by the \n"
				+ CAConstants.PROGRAM_TITLE
				+ ".  These rules must be written in the Java language \n"
				+ "and extend the existing Rule class.  Please see "
				+ "the help pages on this topic. \n\nContinue?";

		int answer = JOptionPane.showConfirmDialog(graphics.getFrame(),
				message, "Rule Folder Confirmation",
				JOptionPane.YES_NO_CANCEL_OPTION);

		if(answer == JOptionPane.YES_OPTION)
		{
			// keep going until they get a directory or cancel
			boolean quit = false;
			while(!quit)
			{
				// let user choose a file to import
				JFileChooser fileChooser = new CAFileChooser(startDirectory);
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setDialogTitle("Rule Folder");
				fileChooser.setAcceptAllFileFilterUsed(false);
				int state = fileChooser.showDialog(null, "Select Folder");
				File file = fileChooser.getSelectedFile();
				startDirectory = fileChooser.getCurrentDirectory();

				if(state == JFileChooser.APPROVE_OPTION)
				{
					if((file != null) && file.isDirectory())
					{
						// reset the property
						CurrentProperties.getInstance().getProperties()
								.setProperty(
										CurrentProperties.USER_RULE_FOLDER,
										file.getPath());

						// reload the hash (not that the rule hashtable is
						// stored statically, and only reloads when it
						// detects a change in the properties.
						new RuleHash();

						// notify anyone who cares (the CAController
						// class and the ControlPanel class).
						firePropertyChangeEvent(new PropertyChangeEvent(this,
								CurrentProperties.USER_RULE_FOLDER, null, file
										.getPath()));

						// success so exit
						quit = true;
					}
					else
					{
						// warning
						String errorMessage = "Your selection is not a folder. "
								+ "Please choose a folder.";

						JOptionPane.showMessageDialog(graphics.getFrame(),
								errorMessage, "Try Again",
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
				else
				{
					// user wants to exit
					quit = true;
				}
			}
		}

		// make the JFrame look enabled
		graphics.setViewDisabled(false);

		// restart the simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(false)));
	}

	/**
	 * Actions to take when "Choose right-click draw color" is selected.
	 */
	private void chooseSecondDrawColor()
	{
		// pause the simulation.
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));

		int numberOfStates = CurrentProperties.getInstance().getNumStates();

		// the color to display in the preview panel
		Color color = ColorScheme.SECOND_DRAW_COLOR;

		// get the current state value to display
		int stateValue = IntegerCellState.SECOND_DRAW_STATE;
		if(stateValue == -1)
		{
			// then we haven't set a color with the chooser. So use the default
			// second draw state.
			stateValue = 0;

			// now set the color
			color = Cell.getView().getDisplayColor(
					new IntegerCellState(stateValue), null,
					new Coordinate(0, 0));
		}

		integerColorChooser = new IntegerStateColorChooser(graphics.getFrame(),
				numberOfStates, stateValue, color, new OkDrawingColorListener(
						CHOOSE_RIGHTCLICK_DRAW_COLOR));

		integerColorChooser.setVisible(true);

		// restart the simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(false)));
	}

	/**
	 * Sets a property so that hyperlinks are displayed in this application.
	 */
	private void displayLinksInApplication()
	{
		// reset the property
		CurrentProperties.getInstance().setDisplayHyperLinksInBrowser(false);
	}

	/**
	 * Sets a property so that hyperlinks are displayed in a separate local
	 * browser.
	 */
	private void displayLinksInBrowser()
	{
		// reset the property
		CurrentProperties.getInstance().setDisplayHyperLinksInBrowser(true);
	}

	/**
	 * Sets a property so that tagged cells are translucent or opaque.
	 * 
	 * @param translucent
	 *            If true, tagged cells will be translucent. Otherwise the
	 *            tagged cells will be opaque.
	 */
	private void displayTaggedCellsAsTranslucent(boolean translucent)
	{
		TaggedColorPool.taggedCellsTranslucent = translucent;
		TaggedColorPool.taggedCellsNoExtraColor = false;

		// now redraw to show any change
		redrawCAGraphics();
	}

	/**
	 * Sets a property so that tagged cells are not given any color.
	 * 
	 * @param translucent
	 *            If true, tagged cells are not given any color.
	 */
	private void doNotDisplayTaggedCells(boolean noColor)
	{
		TaggedColorPool.taggedCellsNoExtraColor = noColor;

		// now redraw to show any change
		redrawCAGraphics();
	}

	/**
	 * Enables or disables the color schemes in the menu.
	 * 
	 * @param enabled
	 *            true if the color schemes are going to be enabled.
	 */
	public void enableColorSchemes(boolean enabled)
	{
		colorSchemeSubMenu.setEnabled(enabled);
		emptyItem.setEnabled(enabled);
		filledItem.setEnabled(enabled);
		restoreColorItem.setEnabled(enabled);
	}

	/**
	 * Enables or disables the zoom-in menu item, and the zoom-in button.
	 * 
	 * @param enabled
	 *            true if the items are going to be enabled.
	 */
	public void enableZoomIn(boolean enabled)
	{
		zoomInItem.setEnabled(enabled);
		graphics.getToolBar().getZoomInButton().setEnabled(enabled);
	}

	/**
	 * Enables or disables the zoom-out menu item, and the zoom-out button.
	 * 
	 * @param enabled
	 *            true if the items are going to be enabled.
	 */
	public void enableZoomOut(boolean enabled)
	{
		zoomOutItem.setEnabled(enabled);
		graphics.getToolBar().getZoomOutButton().setEnabled(enabled);
	}

	/**
	 * Actions to take when "Fit to Screen" is selected.
	 */
	private void fitToScreen()
	{
		graphics.fitGraphicsToScrollPane();

		if(zoomMemoryOk())
		{
			// enable the zoom buttons and menu
			zoomInItem.setEnabled(true);
			zoomOutItem.setEnabled(true);
			graphics.getToolBar().getZoomInButton().setEnabled(true);
			graphics.getToolBar().getZoomOutButton().setEnabled(true);
		}
	}

	/**
	 * Actions to take when "Import Data" is selected.
	 */
	private void loadData()
	{
		// pause the simulation.
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));

		// make the JFrame look disabled
		graphics.setViewDisabled(true);

		// change states, but first ask user if they are sure
		String message = "Loading data will erase the current \n"
				+ "generation's cell values. \n\nContinue?";

		int answer = JOptionPane.showConfirmDialog(graphics.getFrame(),
				message, "Load Data Confirmation",
				JOptionPane.YES_NO_CANCEL_OPTION);

		if(answer == JOptionPane.YES_OPTION)
		{
			JFileChooser fileChooser = new CAFileChooser(startDirectory);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setDialogTitle("Load Data");

			// add a preferred file filter
			fileChooser.addChoosableFileFilter(new CAFileFilter());

			// open the dialog, and let it know which frame is the parent --
			// this lets it inherit the top-left icon.
			int state = fileChooser.showOpenDialog(graphics.getFrame());
			File file = fileChooser.getSelectedFile();

			startDirectory = fileChooser.getCurrentDirectory();

			if((file != null) && (state == JFileChooser.APPROVE_OPTION))
			{
				// reset the properties
				CurrentProperties.getInstance().setInitialStateDataFilePath(
						file.getPath());
				CurrentProperties.getInstance().setInitialState(
						CurrentProperties.STATE_DATA);

				// first stop, then notify other classes to reload.
				CAController.getInstanceOfCAController().stopCA();
				firePropertyChangeEvent(new PropertyChangeEvent(this,
						CurrentProperties.INITIAL_STATE, null,
						CurrentProperties.STATE_DATA));
			}
		}

		// make the JFrame look enabled
		graphics.setViewDisabled(false);

		// restart the simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(false)));
	}

	/**
	 * Actions to take when "Import Image" is selected.
	 */
	private void loadImage()
	{
		// pause the simulation.
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));

		// make the JFrame look disabled
		graphics.setViewDisabled(true);

		// change states, but first ask user if they are sure
		String message = "Loading an image will erase the current \n"
				+ "generation's cell values. \n\nContinue?";

		int answer = JOptionPane.showConfirmDialog(graphics.getFrame(),
				message, "Load Image Confirmation",
				JOptionPane.YES_NO_CANCEL_OPTION);

		if(answer == JOptionPane.YES_OPTION)
		{
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
			int state = fileChooser.showOpenDialog(graphics.getFrame());
			File file = fileChooser.getSelectedFile();

			startDirectory = fileChooser.getCurrentDirectory();

			if((file != null) && (state == JFileChooser.APPROVE_OPTION))
			{
				// reset the properties
				CurrentProperties.getInstance().setInitialStateImageFilePath(
						file.getPath());
				CurrentProperties.getInstance().setInitialState(
						CurrentProperties.STATE_IMAGE);

				// first stop, then reload.
				CAController.getInstanceOfCAController().stopCA();
				firePropertyChangeEvent(new PropertyChangeEvent(this,
						CurrentProperties.INITIAL_STATE, null,
						CurrentProperties.STATE_IMAGE));
			}
		}

		// make the JFrame look enabled
		graphics.setViewDisabled(false);

		// restart the simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(false)));
	}

	/**
	 * Actions to take when "Import Simulation" is selected.
	 */
	private void loadSimulation()
	{
		// pause the simulation.
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));

		// make the JFrame look disabled
		graphics.setViewDisabled(true);

		// change states, but first ask user if they are sure
		String message = "Loading a new simulation will end and \n"
				+ "erase the current simulation. \n\nContinue?";

		int answer = JOptionPane.showConfirmDialog(graphics.getFrame(),
				message, "New Simulation Confirmation",
				JOptionPane.YES_NO_CANCEL_OPTION);

		if(answer == JOptionPane.YES_OPTION)
		{
			JFileChooser fileChooser = new CAFileChooser(startDirectory);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setDialogTitle("Load Simulation");

			// add a preferred file filter
			fileChooser.addChoosableFileFilter(new CAFileFilter());

			// open the dialog, and let it know which frame is the parent --
			// this lets it inherit the top-left icon.
			int state = fileChooser.showOpenDialog(graphics.getFrame());
			File file = fileChooser.getSelectedFile();

			startDirectory = fileChooser.getCurrentDirectory();

			if((file != null) && (state == JFileChooser.APPROVE_OPTION))
			{
				// first stop, then reload.
				CAController.getInstanceOfCAController().stopCA();
				firePropertyChangeEvent(new PropertyChangeEvent(this,
						CurrentProperties.IMPORT_SIMULATION, null, file
								.getPath()));
			}
		}

		// make the JFrame look enabled
		graphics.setViewDisabled(false);

		// restart the simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(false)));
	}

	/**
	 * Move the control panels to the left in an animated manner.
	 */
	private void moveControlsLeft()
	{
		// pause the simulation.
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));

		JPanel controlPanel = graphics.getControlPanel();
		JScrollPane graphicsScrollPane = graphics.getGraphicsScrollPane();
		int leftInset = graphics.getLeftInset();
		int rightInset = graphics.getRightInset();

		// animates the layout when the control panel is being moved to the
		// right.
		Animator animator = new Animator(FLIP_ANIMATION_LENGTH);

		animator.setAcceleration(0.3f);
		animator.setDeceleration(0.2f);

		animator.addTarget(new PropertySetter(controlPanel, "location",
				new Point(graphicsScrollPane.getX() - rightInset, controlPanel
						.getY())));

		animator.addTarget(new PropertySetter(graphicsScrollPane, "location",
				new Point(controlPanel.getWidth() + leftInset,
						graphicsScrollPane.getY())));

		// start the animation
		animator.start();

		// change the tool bar graphics
		graphics.getToolBar().getMoveLeftButton().setEnabled(false);
		graphics.getToolBar().getMoveRightButton().setEnabled(true);

		// restart the simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(false)));

		graphics.createLeftLayout();
	}

	/**
	 * Move the control panels to the right in an animated manner.
	 */
	private void moveControlsRight()
	{
		// pause the simulation.
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));

		JPanel controlPanel = graphics.getControlPanel();
		JScrollPane graphicsScrollPane = graphics.getGraphicsScrollPane();
		int rightInset = graphics.getRightInset();

		// animates the layout when the control panel is being moved to the
		// right.
		Animator animator = new Animator(FLIP_ANIMATION_LENGTH);

		animator.setAcceleration(0.3f);
		animator.setDeceleration(0.2f);

		animator.addTarget(new PropertySetter(controlPanel, "location",
				new Point(graphicsScrollPane.getX()
						+ graphicsScrollPane.getWidth()
						- controlPanel.getWidth() + rightInset, controlPanel
						.getY())));

		animator.addTarget(new PropertySetter(graphicsScrollPane, "location",
				new Point(controlPanel.getX() + rightInset, graphicsScrollPane
						.getY())));

		// start the animation
		animator.start();

		// change the tool bar graphics
		graphics.getToolBar().getMoveLeftButton().setEnabled(true);
		graphics.getToolBar().getMoveRightButton().setEnabled(false);

		// restart the simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(false)));

		graphics.createRightLayout();
	}

	/**
	 * The following deals with a Java bug in JColorChooser.createDialog() which
	 * seems to sometimes dispose of the JColorChooser() when it shouldn't. This
	 * recreates the JColorChooser.
	 */
	private void recreateColorChooser()
	{
		// Shouldn't have to thread this, but java seems to dispose of the
		// JColorChooser when closing the dialog box, so I recreate it here.
		// Threading this makes the next "Choose filled color" menu selection
		// much, much, much faster.
		final SwingWorker worker = new SwingWorker()
		{
			public Object construct()
			{
				// ...code that might take a while to execute is here...

				// Note that I check for null because it doesn't always dispose
				// of the color chooser. And if you don't check, it causes
				// problems where the recreated chooser always says the selected
				// color is white. Besides, later releases of Java may fix this
				// bug.
				if(colorChooser == null)
				{
					colorChooser = new JColorChooser();
				}

				return null;
			}
		};
		worker.start();

		// this handles ctrl-click events so that they are not missed. The
		// user selects ctrl-Something to activate a dialog. But if they release
		// the ctrl key while the dialog has focus, then the ctrl-release
		// event is not heard -- unless I bind the ctrl key to the
		// dialog window. This is a work-around for Macs (replaces
		// right-clicks with ctrl-clicks).
		CAFrame.bindCtrlKey(colorChooser);
	}

	/**
	 * Redraws the CA graphics (and refreshes the frame graphics).
	 */
	private void redrawCAGraphics()
	{
		graphics.getGraphicsPanel().redraw();
		graphics.getFrame().repaint();
	}

	/**
	 * Resets the application to its original "right out of the box" settings.
	 */
	private void resetApplication()
	{
		// check to see if they really want to reset
		// make the frame look disabled
		graphics.setViewDisabled(true);

		boolean continueSubmission = false;

		String message = "This will reset the application to default settings \n"
				+ "and end the current simulation. \n\nContinue?";

		int answer = JOptionPane.showConfirmDialog(graphics.getFrame(),
				message, "Submit Confirmation",
				JOptionPane.YES_NO_CANCEL_OPTION);

		if(answer == JOptionPane.YES_OPTION)
		{
			continueSubmission = true;
		}

		// make the frame look enabled
		graphics.setViewDisabled(false);

		if(continueSubmission)
		{
			// reset the colors (This must happen before resetting to the
			// default properties (next line) or will crash with non-integer
			// rules. Crashes because tries to set colors for integer-based
			// rules which is the default Life rule, but the color scheme is
			// still not integer based.)
			setColorScheme(CHOOSE_DEFAULT_COLOR_SCHEME);

			// get the default properties
			CurrentProperties.getInstance().resetToDefaultProperties();

			// reset the properties on the graphics (including rule, initial
			// state, etc.)
			graphics.getControlPanel().getPropertiesPanel().reset();

			// why do this roundabout submission? Because the submitProperties()
			// method already takes care of everything. No need to do it again.
			graphics.getControlPanel().getAllPanelListener().submitProperties(
					false);
		}
	}

	/**
	 * Actions to take when "Save As..." is selected.
	 */
	private void saveAs()
	{
		// pause the simulation.
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));

		// make the JFrame look disabled
		graphics.setViewDisabled(true);

		// let user choose a file to save the data
		// This is the default folder that it goes to.
		File file = startDirectory;
		int state = 0;

		boolean chooseAnotherFile = true;
		while(chooseAnotherFile)
		{
			// let user choose a file to save the data
			JFileChooser fileChooser = new CAFileChooser(file);

			fileChooser.setDialogTitle(SAVE_AS);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			// this handles ctrl-click events so that they are not missed. The
			// user selects ctrl-S to activate this box. But if they release the
			// ctrl key while the save dialog has focus, then the ctrl-release
			// event is not heard -- unless I bind the ctrl key to the save
			// dialog window. This is a work-around for Macs (replaces
			// right-clicks with ctrl-clicks).
			CAFrame.bindCtrlKey(fileChooser);

			// only allow these files
			CAFileFilter caFilter = new CAFileFilter();
			fileChooser.addChoosableFileFilter(caFilter);
			fileChooser.setFileFilter(caFilter);
			fileChooser.setAcceptAllFileFilterUsed(false);

			// open the dialog and let it know which frame is the parent -- this
			// lets it inherit the top-left icon.
			state = fileChooser.showSaveDialog(graphics.getFrame());
			file = fileChooser.getSelectedFile();
			startDirectory = fileChooser.getCurrentDirectory();

			if((file != null) && (state == JFileChooser.APPROVE_OPTION))
			{
				// fix the file path (if necessary) so has the correct extension
				String filePath = file.getPath();
				if(!CAFileFilter.getExtension(filePath).equals(
						CAConstants.CA_FILE_EXTENSION))
				{
					filePath += "." + CAConstants.CA_FILE_EXTENSION;
					file = new File(filePath);
				}

				if(file.exists())
				{
					String message = "The file \"" + file.getName()
							+ "\" exists.  Overwrite this file?";
					int answer = JOptionPane.showConfirmDialog(graphics
							.getFrame(), message, "Replace",
							JOptionPane.YES_NO_CANCEL_OPTION);
					if(answer == JOptionPane.YES_OPTION)
					{
						chooseAnotherFile = false;
					}
				}
				else
				{
					chooseAnotherFile = false;
				}
			}
			else
			{
				// user wants to bail
				chooseAnotherFile = false;
			}
		}

		if((file != null) && (state == JFileChooser.APPROVE_OPTION))
		{
			// reset the relevant properties
			CurrentProperties.getInstance().setSaveDataFilePath(file.getPath());

			// Notify anyone who cares. (Note: I only have to tell it to save
			// because the file path is read elsewhere.)
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.SAVE_DATA, CurrentProperties.FALSE,
					CurrentProperties.TRUE));
		}

		// make the JFrame look enabled
		graphics.setViewDisabled(false);

		// restart the simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(false)));
	}

	/**
	 * Actions to take when "Save As Image" is selected.
	 */
	private void saveAsImage()
	{
		// pause the simulation.
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));

		// make the JFrame look disabled
		graphics.setViewDisabled(true);

		File file = startDirectory;
		int state = 0;

		boolean chooseAnotherFile = true;
		while(chooseAnotherFile)
		{
			// let user choose a file to save the data
			JFileChooser fileChooser = new CAFileChooser(file);
			fileChooser.setDialogTitle(SAVE_AS_IMAGE);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			// only allow these files
			JPGAndPNGAndOtherFileTypeFilter jpgFilter = new JPGAndPNGAndOtherFileTypeFilter(
					"jpg", "JPG Image (*.jpg)");
			JPGAndPNGAndOtherFileTypeFilter pngFilter = new JPGAndPNGAndOtherFileTypeFilter(
					"png", "PNG Image (*.png)");
			fileChooser.addChoosableFileFilter(jpgFilter);
			fileChooser.addChoosableFileFilter(pngFilter);
			fileChooser.setFileFilter(jpgFilter);

			// open the dialog and let it know which frame is the parent -- this
			// lets it inherit the top-left icon.
			state = fileChooser.showSaveDialog(graphics.getFrame());
			file = fileChooser.getSelectedFile();
			startDirectory = fileChooser.getCurrentDirectory();
			String usersFileFilterSelection = fileChooser.getFileFilter()
					.getDescription();
			String suffixOnFileChooser = "";
			if(usersFileFilterSelection.equals(jpgFilter.getDescription()))
			{
				suffixOnFileChooser = jpgFilter.getSuffix();
			}
			else if(usersFileFilterSelection.equals(pngFilter.getDescription()))
			{
				suffixOnFileChooser = pngFilter.getSuffix();
			}

			if((file != null) && (state == JFileChooser.APPROVE_OPTION))
			{
				// check if it has an image suffix (like .jpg)
				String suffixFromUser = JPGAndPNGAndOtherFileTypeFilter
						.getSuffix(file);
				if(suffixFromUser.equals(""))
				{
					// user didn't specify a suffix in the file name, but did
					// they specify a suffix on the chooser?
					if(suffixOnFileChooser.equals(""))
					{
						// no specified suffix on chooser, so append a default
						// valid suffix to their file choice (note, this handles
						// adding (or not adding) the "." in this method)
						String path = JPGAndPNGImageReadWrite
								.appendDefaultImageSuffix(file.getPath());

						// replace their file choice with one that has a valid
						// suffix
						file = new File(path);
					}
					else
					{
						// the user didn't type a suffix, but they selected a
						// suffix on the chooser, so append that chooser suffix.
						String selectedFilePath = file.getPath();
						if(selectedFilePath.endsWith("."))
						{
							file = new File(selectedFilePath
									+ suffixOnFileChooser);
						}
						else
						{
							file = new File(selectedFilePath + "."
									+ suffixOnFileChooser);
						}
					}

				}
				else if(!suffixOnFileChooser.equals(suffixFromUser)
						&& !suffixOnFileChooser.equals(""))
				{
					// a suffix exists, but it isn't the same type as the
					// suffix selected on the chooser. So append the suffix on
					// the chooser.
					String selectedFilePath = file.getPath();
					if(selectedFilePath.endsWith("."))
					{
						file = new File(selectedFilePath + suffixOnFileChooser);
					}
					else
					{
						file = new File(selectedFilePath + "."
								+ suffixOnFileChooser);
					}
				}
				else if(suffixOnFileChooser.equals(""))
				{
					// a user specified suffix exists, but the user didn't
					// choose a JPG or PNG on the chooser. So check that their
					// suffix is ok, and if not just add the
					// default.
					String path = JPGAndPNGImageReadWrite
							.appendDefaultImageSuffix(file.getPath());

					// replace their file choice with one that has a valid
					// suffix
					file = new File(path);
				}

				if(file.exists())
				{
					String message = "The file \"" + file.getName()
							+ "\" exists.  Overwrite this file?";
					int answer = JOptionPane.showConfirmDialog(graphics
							.getFrame(), message, "Replace",
							JOptionPane.YES_NO_CANCEL_OPTION);
					if(answer == JOptionPane.YES_OPTION)
					{
						chooseAnotherFile = false;
					}
				}
				else
				{
					chooseAnotherFile = false;
				}
			}
			else
			{
				// user wants to bail
				chooseAnotherFile = false;
			}
		}

		if((file != null) && (state == JFileChooser.APPROVE_OPTION))
		{
			// reset the property
			CurrentProperties.getInstance()
					.setSaveImageFilePath(file.getPath());

			// can't save from here because we don't have the graphics
			// objects. So notify anyone else who cares (CellularAutomaton)
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					SAVE_AS_IMAGE, null, ""));
		}

		// make the JFrame look enabled
		graphics.setViewDisabled(false);

		// restart the simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(false)));
	}

	/**
	 * Actions to take when "Save As Movie" is selected.
	 */
	private void saveAsMovie()
	{
		// pause the simulation.
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));

		// make the JFrame look disabled
		graphics.setViewDisabled(true);

		// is a movie already in progress?
		if(MovieMaker.isOpen())
		{
			String warning = "A movie is already being made. \n\n"
					+ "Press the \"stop\" button to end the current movie \n"
					+ "before beginning a new movie.";
			WarningManager.displayWarningWithMessageDialog(warning, 100,
					"Movie already in progress");
		}
		else
		{
			// no movie is in progress, so get the file
			// name of the new one
			File file = startDirectory;
			int state = 0;

			boolean chooseAnotherFile = true;
			while(chooseAnotherFile)
			{
				// let user choose a file to save the data
				JFileChooser fileChooser = new CAFileChooser(file);
				fileChooser.setDialogTitle(SAVE_AS_MOVIE);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

				// only allow these files
				JPGAndPNGAndOtherFileTypeFilter movFilter = new JPGAndPNGAndOtherFileTypeFilter(
						"mov", "MOV movies (*.mov)");
				fileChooser.addChoosableFileFilter(movFilter);
				fileChooser.setFileFilter(movFilter);

				// open the dialog and let it know which frame is the parent --
				// this lets it inherit the top-left icon.
				state = fileChooser.showSaveDialog(graphics.getFrame());
				file = fileChooser.getSelectedFile();
				startDirectory = fileChooser.getCurrentDirectory();
				String usersFileFilterSelection = fileChooser.getFileFilter()
						.getDescription();
				String suffixOnFileChooser = "";
				if(usersFileFilterSelection.equals(movFilter.getDescription()))
				{
					suffixOnFileChooser = movFilter.getSuffix();
				}

				if((file != null) && (state == JFileChooser.APPROVE_OPTION))
				{
					// check if it has a movie suffix (like .mov)
					String suffixFromUser = JPGAndPNGAndOtherFileTypeFilter
							.getSuffix(file);
					if(suffixFromUser.equals(""))
					{
						// user didn't specify a suffix in the file name, but
						// did they specify a suffix on the chooser?
						if(suffixOnFileChooser.equals(""))
						{
							// no specified suffix on chooser, so append a
							// default valid suffix to their file choice (note,
							// this handles adding (or not adding) the "." in
							// this method)
							String path = JPGAndPNGImageReadWrite
									.appendDefaultImageSuffix(file.getPath());

							// replace their file choice with one that has a
							// valid suffix
							file = new File(path);
						}
						else
						{
							// the user didn't type a suffix, but they selected
							// a
							// suffix on the chooser, so append that chooser
							// suffix.
							String selectedFilePath = file.getPath();
							if(selectedFilePath.endsWith("."))
							{
								file = new File(selectedFilePath
										+ suffixOnFileChooser);
							}
							else
							{
								file = new File(selectedFilePath + "."
										+ suffixOnFileChooser);
							}
						}

					}
					else if(!suffixOnFileChooser.equals(suffixFromUser)
							&& !suffixOnFileChooser.equals(""))
					{
						// a suffix exists, but it isn't the same type as the
						// suffix selected on the chooser. So append the suffix
						// on the chooser.
						String selectedFilePath = file.getPath();
						if(selectedFilePath.endsWith("."))
						{
							file = new File(selectedFilePath
									+ suffixOnFileChooser);
						}
						else
						{
							file = new File(selectedFilePath + "."
									+ suffixOnFileChooser);
						}
					}
					else if(suffixOnFileChooser.equals(""))
					{
						// a user specified suffix exists, but the user didn't
						// choose a MOV on the chooser. So check that their
						// suffix is ok, and if not just add the
						// default.
						String path = JPGAndPNGImageReadWrite
								.appendDefaultImageSuffix(file.getPath());

						// replace their file choice with one that has a valid
						// suffix
						file = new File(path);
					}

					if(file.exists())
					{
						String message = "The file \"" + file.getName()
								+ "\" exists.  Overwrite this file?";
						int answer = JOptionPane.showConfirmDialog(graphics
								.getFrame(), message, "Replace",
								JOptionPane.YES_NO_CANCEL_OPTION);
						if(answer == JOptionPane.YES_OPTION)
						{
							chooseAnotherFile = false;
						}
					}
					else
					{
						chooseAnotherFile = false;
					}
				}
				else
				{
					// user wants to bail
					chooseAnotherFile = false;
				}
			}

			if((file != null) && (state == JFileChooser.APPROVE_OPTION))
			{
				// reset the property
				CurrentProperties.getInstance().setSaveImageFilePath(
						file.getPath());

				// start the movie
				try
				{
					MovieMaker.makeMovie(file);

					// disable the movie buttons
					saveAsMovieItem.setEnabled(false);
					graphics.getToolBar().getMovieButton().setEnabled(false);

					// enable the cut movie buttons
					stopMovieItem.setEnabled(true);
					graphics.getToolBar().getCutMovieButton().setEnabled(true);
				}
				catch(Exception e)
				{
					String warning = "Sorry, the movie could not be opened and created.";
					WarningManager.displayWarningWithMessageDialog(warning,
							100, "Could not create movie");
				}

				// In general, can't add frames to the movie from here because
				// we don't have the graphics objects and don't know when they
				// are being updated. The frames will be added from the
				// LatticeView classes. But we need to add the first frame right
				// away. This forces an update that calls the
				// LatticeCiew.drawLattice(Lattice) method which adds a frame to
				// the movie..
				CAController.getCAFrame().update();
			}
		}

		// make the JFrame look enabled
		graphics.setViewDisabled(false);

		// restart the simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(false)));
	}

	/**
	 * Sets the radio button to the correct value when loading a color scheme.
	 * Used when the radio button is selected programmatically (for example, at
	 * start up when reading the properties file).
	 */
	public void clickColorSchemeRadioButton(String colorScheme)
	{
		if(colorScheme != null)
		{
			if(colorScheme.equals(CHOOSE_DEFAULT_COLOR_SCHEME))
			{
				defaultColorSchemeItem.doClick();
			}
			else if(colorScheme.equals(CHOOSE_FIRE_COLOR_SCHEME))
			{
				fireColorSchemeItem.doClick();
				// fireColorSchemeItem.setSelected(true);
			}
			else if(colorScheme.equals(CHOOSE_BLUE_DIAMOND_COLOR_SCHEME))
			{
				blueDiamondColorSchemeItem.doClick();
			}
			else if(colorScheme.equals(CHOOSE_BLUE_SHADES_COLOR_SCHEME))
			{
				blueShadesColorSchemeItem.doClick();
			}
			else if(colorScheme.equals(CHOOSE_GREEN_OCEAN_COLOR_SCHEME))
			{
				greenOceanColorSchemeItem.doClick();
			}
			else if(colorScheme.equals(CHOOSE_WATER_LILIES_COLOR_SCHEME))
			{
				waterLilliesColorSchemeItem.doClick();
			}
			else if(colorScheme.equals(CHOOSE_PURPLE_HAZE_COLOR_SCHEME))
			{
				purpleHazeColorSchemeItem.doClick();
			}
			else if(colorScheme.equals(CHOOSE_GRAY_SMOKE_COLOR_SCHEME))
			{
				graySmokeColorSchemeItem.doClick();
			}
			else if(colorScheme.equals(CHOOSE_BLACK_AND_WHITE_COLOR_SCHEME))
			{
				blackAndWhiteColorSchemeItem.doClick();
			}
			else if(colorScheme.equals(CHOOSE_WHITE_AND_BLACK_COLOR_SCHEME))
			{
				whiteAndBlackColorSchemeItem.doClick();
			}
			else if(colorScheme.equals(CHOOSE_RANDOM_COLOR_SCHEME))
			{
				randomColorSchemeItem.doClick();
			}
			else if(colorScheme.equals(CHOOSE_YELLOW_JACKET_COLOR_SCHEME))
			{
				yellowJacketColorSchemeItem.doClick();
			}
		}
	}

	/**
	 * Uses values in the properties file to set the color scheme.
	 */
	public void setColorsFromProperties()
	{
		// Set the colors from the properties. Note that we have to save the
		// filled and empty colors because the call to
		// "clickColorSchemeRadioButton()" will call "setColorScheme()" which
		// will reset the filled and empty colors to the color scheme's filled
		// and empty values.
		Color emptyColor = CurrentProperties.getInstance().getEmptyColor();
		Color filledColor = CurrentProperties.getInstance().getFilledColor();
		String colorScheme = CurrentProperties.getInstance().getColorScheme();

		// set the color scheme
		clickColorSchemeRadioButton(colorScheme);

		// reset the filled and empty colors (which might have been
		// reset by the radio click above).
		setEmptyColor(emptyColor);
		setFilledColor(filledColor);

		// set the properties for filled and empty colors (which might have been
		// reset by the radio click above).
		CurrentProperties.getInstance().setEmptyColor(
				CellStateView.colorScheme.getEmptyColor());
		CurrentProperties.getInstance().setFilledColor(
				CellStateView.colorScheme.getFilledColor());
	}

	/**
	 * Sets the color scheme.
	 * 
	 * @param colorScheme
	 *            The color scheme that was selected.
	 */
	private void setColorScheme(String colorScheme)
	{
		if(colorScheme != null)
		{
			if(colorScheme.equals(CHOOSE_DEFAULT_COLOR_SCHEME))
			{
				CellStateView.colorScheme = CellStateView
						.getDefaultColorScheme();
			}
			else if(colorScheme.equals(CHOOSE_FIRE_COLOR_SCHEME))
			{
				CellStateView.colorScheme = new FireColorScheme();
			}
			else if(colorScheme.equals(CHOOSE_BLUE_DIAMOND_COLOR_SCHEME))
			{
				CellStateView.colorScheme = new BlueDiamondColorScheme();
			}
			else if(colorScheme.equals(CHOOSE_BLUE_SHADES_COLOR_SCHEME))
			{
				CellStateView.colorScheme = new KindOfBluesColorScheme();
			}
			else if(colorScheme.equals(CHOOSE_GREEN_OCEAN_COLOR_SCHEME))
			{
				CellStateView.colorScheme = new GreenOceanColorScheme();
			}
			else if(colorScheme.equals(CHOOSE_PURPLE_HAZE_COLOR_SCHEME))
			{
				CellStateView.colorScheme = new PurpleHazeColorScheme();
			}
			else if(colorScheme.equals(CHOOSE_WATER_LILIES_COLOR_SCHEME))
			{
				CellStateView.colorScheme = new WaterLiliesColorScheme();
			}
			else if(colorScheme.equals(CHOOSE_GRAY_SMOKE_COLOR_SCHEME))
			{
				CellStateView.colorScheme = new ChocolateColorScheme();
			}
			else if(colorScheme.equals(CHOOSE_BLACK_AND_WHITE_COLOR_SCHEME))
			{
				CellStateView.colorScheme = new BlackAndWhiteColorScheme();
			}
			else if(colorScheme.equals(CHOOSE_WHITE_AND_BLACK_COLOR_SCHEME))
			{
				CellStateView.colorScheme = new WhiteAndBlackColorScheme();
			}
			else if(colorScheme.equals(CHOOSE_YELLOW_JACKET_COLOR_SCHEME))
			{
				CellStateView.colorScheme = new YellowJacketColorScheme();
			}
			else if(colorScheme.equals(CHOOSE_RANDOM_COLOR_SCHEME))
			{
				CellStateView.colorScheme = new RandomColorScheme();
			}
			else
			{
				// just in case
				CellStateView.colorScheme = CellStateView
						.getDefaultColorScheme();
			}

			// reset the drawing colors (the change in scheme will also change
			// these drawing colors)
			int drawStateValue = IntegerCellState.DRAW_STATE;
			int secondDrawStateValue = IntegerCellState.SECOND_DRAW_STATE;

			// make sure the drawing states have been set previously
			if(drawStateValue == -1)
			{
				// then we haven't set a color with the chooser. So use the
				// default draw state.
				drawStateValue = CurrentProperties.getInstance().getNumStates() - 1;
			}
			if(secondDrawStateValue == -1)
			{
				// then we haven't set a color with the chooser. So use the
				// default draw state.
				secondDrawStateValue = 0;
			}

			// now set the drawing colors
			if(IntegerCellStateView.isCurrentRuleCompatible()
					&& IntegerCellState.isCurrentRuleCompatible())
			{
				ColorScheme.DRAW_COLOR = Cell.getView().getDisplayColor(
						new IntegerCellState(drawStateValue), null,
						new Coordinate(0, 0));
				ColorScheme.SECOND_DRAW_COLOR = Cell.getView().getDisplayColor(
						new IntegerCellState(secondDrawStateValue), null,
						new Coordinate(0, 0));
			}
			else
			{
				// not an IntegerCellState compatible view
				ColorScheme.DRAW_COLOR = ColorScheme.DEFAULT_DRAW_COLOR;
				ColorScheme.SECOND_DRAW_COLOR = ColorScheme.DEFAULT_SECOND_DRAW_COLOR;
			}

			// set the properties
			CurrentProperties.getInstance().setColorScheme(colorScheme);
			CurrentProperties.getInstance().setEmptyColor(
					CellStateView.colorScheme.getEmptyColor());
			CurrentProperties.getInstance().setFilledColor(
					CellStateView.colorScheme.getFilledColor());

			// now redraw
			redrawCAGraphics();

			// let everyone know that we just changed the colors of the graphics
			// (for example, the Analyses may need to respond)
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.COLORS_CHANGED, null, new Boolean(true)));
		}
	}

	/**
	 * Set the empty color (used to display "empty" cells).
	 * 
	 * @param color
	 *            the new empty color
	 */
	public void setEmptyColor(Color color)
	{
		if(color != null)
		{// might also need to change the drawing colors
			if(ColorScheme.SECOND_DRAW_COLOR.equals(ColorScheme.EMPTY_COLOR))
			{
				ColorScheme.SECOND_DRAW_COLOR = color;
			}
			if(ColorScheme.DRAW_COLOR.equals(ColorScheme.EMPTY_COLOR))
			{
				ColorScheme.DRAW_COLOR = color;
			}

			// change the empty color
			ColorScheme.EMPTY_COLOR = color;

			// use the new color
			redrawCAGraphics();

			// let everyone know that we just changed the colors of the
			// graphics (for example, the Analyses may need to respond)
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.COLORS_CHANGED, null, new Boolean(true)));
		}
	}

	/**
	 * Set the filled color (used to display "filled" cells).
	 * 
	 * @param color
	 *            the new filled color
	 */
	public void setFilledColor(Color color)
	{
		if(color != null)
		{
			// / might also need to change the drawing colors
			if(ColorScheme.SECOND_DRAW_COLOR.equals(ColorScheme.FILLED_COLOR))
			{
				ColorScheme.SECOND_DRAW_COLOR = color;
			}
			if(ColorScheme.DRAW_COLOR.equals(ColorScheme.FILLED_COLOR))
			{
				ColorScheme.DRAW_COLOR = color;
			}

			// change the filled color
			ColorScheme.FILLED_COLOR = color;

			// use the new color
			redrawCAGraphics();

			// let everyone know that we just changed the colors of the
			// graphics (for example, the Analyses may need to respond)
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.COLORS_CHANGED, null, new Boolean(true)));
		}
	}

	/**
	 * Sets the maximum size in *digits* for rule numbers. Only rules that check
	 * the IntegerRule.maxRuleSize parameter are affected.
	 */
	private void setMaxRuleSize()
	{
		// pause the simulation.
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));

		// make the JFrame look disabled
		graphics.setViewDisabled(true);

		// get the currently used max size
		int maxSize = CurrentProperties.getInstance()
				.getMaxRuleNumberSizeInDigits();
		if(IntegerRule.maxRuleSize != maxSize)
		{
			IntegerRule.maxRuleSize = maxSize;
		}

		// get the default max size
		int defaultMaxSize = IntegerRule.DEFAULT_MAX_RULE_SIZE;

		String message = "<html><body>"
				+ "Integer-based rules can often be selected by a \"rule number\" <br>"
				+ "or code (for example, see Outer Totalistic and Totalistic rules). <br>"
				+ "With large numbers of states or large neighborhoods, these <br>"
				+ "rule numbers can be tremendously large.  The size can easily <br>"
				+ "exceed the memory capabilities of your computer, with trillions <br>"
				+ "or more DIGITS in the rule number.  <br><br>"
				+ "A maximum of "
				+ defaultMaxSize
				+ " digits is recommended, but with <br>"
				+ "sufficient memory, your computer may be able to handle more. <br>"
				+ " The maximum number of digits is currently set at "
				+ maxSize + ". <br><br>"
				+ "WARNING: Setting this value too large may <br>"
				+ "crash your computer. <br><br>"
				+ "Set the maximum number of digits:</body></html>";

		// repeat until they choose a reasonable number or cancel
		boolean chooseAnotherNumber = true;
		while(chooseAnotherNumber)
		{
			Object inputMaxSize = JOptionPane.showInputDialog(graphics
					.getFrame(), message, "Maximum rule size",
					JOptionPane.OK_CANCEL_OPTION, null, null, maxSize);

			// if they canceled, will be null
			if(inputMaxSize != null)
			{
				try
				{
					int newMaxSize = Integer.parseInt(inputMaxSize.toString());

					// should be greater than 10.
					if(newMaxSize < 10)
					{
						throw new Exception();
					}

					CurrentProperties.getInstance()
							.setMaxRuleNumberSizeInDigits(newMaxSize);

					// can exit because the number was ok
					chooseAnotherNumber = false;
				}
				catch(Exception e)
				{
					// Bad number that couldn't be parsed. Note that we make the
					// max = Integer.MAX_VALUE-1 because we later use
					// BigInteger.pow(max+1). (See the OuterTotalistic class.)
					String errorMessage = "<html>The maximum number of digits must be an integer "
							+ "between 10 and "
							+ (Integer.MAX_VALUE - 1)
							+ ".</html>";

					JOptionPane.showMessageDialog(graphics.getFrame(),
							errorMessage, "Choose another number",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			else
			{
				// can exit because they canceled
				chooseAnotherNumber = false;
			}
		}

		// make the JFrame look enabled
		graphics.setViewDisabled(false);

		// restart the simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(false)));
	}

	/**
	 * Sets the number of processors that will be used for parallel processing.
	 * 
	 * @param numberOfProcessors
	 *            The number of selected processors.
	 */
	private void setNumberOfProcessors(int numberOfProcessors)
	{
		// reset the property
		CurrentProperties.getInstance().setNumberOfParallelProcessors(
				numberOfProcessors);

		// and reset the number in the CAController
		CAController.getInstanceOfCAController()
				.setUserSelectedNumberOfProcessors(numberOfProcessors);
	}

	/**
	 * Chooses a random rule from among the facade simulations and sets the CA.
	 */
	private void setRandomFacadeRule()
	{
		// pause the simulation.
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));

		Random random = RandomSingleton.getInstance();

		// select a random rule
		RuleTree ruleTree = graphics.getControlPanel().getRulePanel()
				.getRuleTree();
		RuleHash ruleHash = new RuleHash();
		String[] ruleNames = ruleHash.toArray();

		// make it look like we are hunting for a rule by selecting a bunch of
		// random rules before settling on one
		for(int i = 0; i < 20; i++)
		{
			String randomRuleName = ruleNames[random.nextInt(ruleNames.length)];
			ruleTree.setSelectedRule(randomRuleName);
			// try
			// {
			// Thread.sleep(10);
			// }
			// catch(Exception e)
			// {
			// // do nothing
			// }
		}

		// keep getting one until the rule has an associated .ca facade file
		String ruleName = null;
		String relativePath = "";
		URL facadeFile = null;
		do
		{
			// get a random rule
			ruleName = ruleNames[random.nextInt(ruleNames.length)];
			ruleTree.setSelectedRule(ruleName);
			String ruleClassName = ruleHash.get(ruleName);

			// get the class name and remove the package info
			ruleClassName = ruleClassName.substring(ruleClassName
					.lastIndexOf(".") + 1);

			// the partial or relative path to the file
			relativePath = "/" + CAConstants.FACADE_SIMULATIONS_FOLDER_NAME
					+ "/" + ruleClassName + "." + CAConstants.CA_FILE_EXTENSION;

			// get the file path from the class name. Note the URLResource makes
			// sure that we can access files outside of the jar.
			facadeFile = URLResource.getResource(relativePath);
		}
		while(facadeFile == null
				|| !facadeFile.getPath().contains(relativePath));

		// Programmatically "single click" on the tree (the -1's for the x and y
		// position of the click let the receiving listener know that this was a
		// programmatic event.
		MouseEvent mouseEvent = new MouseEvent(ruleTree.getTree(),
				MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
				MouseEvent.BUTTON1_MASK, -1, -1, 1, false);
		graphics.getControlPanel().getAllPanelListener().mousePressed(
				mouseEvent);

		// make the JFrame look disabled
		graphics.setViewDisabled(true);

		// let the user know what was selected (with cool spinning roulette
		// wheel
		URL randomURL = URLResource.getResource("/images/roulette200.gif");
		RotatedImageIcon icon = null;
		if(randomURL != null)
		{
			icon = new RotatedImageIcon(randomURL);
		}
		JLabel randomRuleInfo = new JLabel(RANDOM_FACADE_RULE_INFO_TEXT + "\""
				+ ruleName + "\".</html>");
		String continueButtonLabel = "Ok";
		String cancelButtonLabel = "Cancel";
		SpinningIconJOptionPane optionDialog = new SpinningIconJOptionPane(
				randomRuleInfo, JOptionPane.INFORMATION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, icon, new String[] {
						continueButtonLabel, cancelButtonLabel}, null);

		// start spinning the icon on the JOptionPane
		optionDialog.startSpin();

		// display the message
		JDialog dialog = optionDialog.createDialog(graphics.getFrame(),
				"Selecting random rule");
		dialog.setVisible(true);

		// stop the spinning (they are done with the dialog)
		optionDialog.stopSpin();

		// do they want to continue?
		if(optionDialog.getValue() != null
				&& optionDialog.getValue().equals(continueButtonLabel))
		{
			// submit
			graphics.getControlPanel().getAllPanelListener()
					.loadFacadeSimulation();
		}
		else
		{
			// restart the simulation
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.PAUSE, null, new Boolean(false)));
		}

		// make the JFrame look enabled
		graphics.setViewDisabled(false);
	}

	/**
	 * Chooses a random rule and sets the CA.
	 */
	private void setRandomRule()
	{
		// much safer than choosing random parameters which could easily fail
		setRandomFacadeRule();

		// KEEPING THIS CODE JUST "IN CASE"... TO WORK WOULD NEED TO SELECT THE
		// RULE BEFORE THE LATTICE< AND CHOOSE A LATTICE THAT IS COMPATIBLE
		//
		// if(CAConstants.LATTICE_CENTRIC_CHOICES)
		// {
		// // pause the simulation.
		// firePropertyChangeEvent(new PropertyChangeEvent(this,
		// CurrentProperties.PAUSE, null, new Boolean(true)));
		//
		// Random random = RandomSingleton.getInstance();
		//
		// // select a random lattice (biased toward square 8 neighbor lattices
		// // and one-dimensional nearest neighbor lattices)
		// LatticeHash latticeHash = new LatticeHash();
		// String[] latticeNames = latticeHash.toArray();
		// int biasedLength = (int) (1.0 * latticeNames.length);
		// String[] biasedLatticeNames = new String[latticeNames.length
		// + biasedLength];
		// for(int i = 0; i < latticeNames.length; i++)
		// {
		// biasedLatticeNames[i] = latticeNames[i];
		// }
		// for(int i = latticeNames.length; i < latticeNames.length
		// + (int) ((biasedLatticeNames.length - latticeNames.length) / 2.0);
		// i++)
		// {
		// // add the bias toward nearest-neighbor square lattices
		// biasedLatticeNames[i] = SquareLattice.DISPLAY_NAME;
		// }
		// for(int i = latticeNames.length
		// + (int) ((biasedLatticeNames.length - latticeNames.length) / 2.0); i
		// < biasedLatticeNames.length; i++)
		// {
		// // add the bias toward one-dim nearest-neighbor lattices
		// biasedLatticeNames[i] = StandardOneDimensionalLattice.DISPLAY_NAME;
		// }
		// String latticeName = biasedLatticeNames[random
		// .nextInt(biasedLatticeNames.length)];
		// graphics.getControlPanel().getPropertiesPanel().getLatticeChooser()
		// .setSelectedItem(latticeName);
		//
		// // select a random lattice neighborhood radius
		// if(graphics.getControlPanel().getPropertiesPanel().getRadiusField()
		// .isEnabled())
		// {
		// // select a random neighborhood radius from 1 to 10
		// int randomRadius = (random.nextInt(9) + 1);
		// graphics.getControlPanel().getPropertiesPanel()
		// .getRadiusField().setText("" + randomRadius);
		// }
		//
		// // select a random lattice neighborhood standard deviation
		// if(graphics.getControlPanel().getPropertiesPanel()
		// .getStandardDeviationField().isEnabled())
		// {
		// // select a random neighborhood standardDeviation from 1.0 to
		// // 10.0
		// float randomStandardDeviation = (9.0f * random.nextFloat()) + 1.0f;
		// graphics.getControlPanel().getPropertiesPanel()
		// .getStandardDeviationField().setText(
		// "" + randomStandardDeviation);
		// }
		//
		// // get a list of all rules
		// RuleTree ruleTree = graphics.getControlPanel().getRulePanel()
		// .getRuleTree();
		// RuleHash ruleHash = new RuleHash();
		// String[] ruleNames = ruleHash.toArray();
		//
		// // now select a random rule
		// String ruleName = null;
		// do
		// {
		// // keep getting one until the rule isn't null on the RuleTree
		// // (i.e., greyed out)
		// ruleName = ruleNames[random.nextInt(ruleNames.length)];
		// ruleTree.setSelectedRule(ruleName);
		// }
		// while(ruleTree.getSelectedRuleName() == null);
		//
		// // Programmatically "single click" on the tree (the -1's for the x
		// // and y position of the click let the receiving listener know that
		// // this was a programmatic event.
		// MouseEvent mouseEvent = new MouseEvent(ruleTree.getTree(),
		// MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
		// MouseEvent.BUTTON1_MASK, -1, -1, 1, false);
		// graphics.getControlPanel().getAllPanelListener().mousePressed(
		// mouseEvent);
		//
		// // choose a random number of states
		// MinMaxIntPair minmax = IntegerRule.getMinMaxStatesAllowed(
		// latticeName, ruleName);
		// if(minmax != null)
		// {
		// // limit to reasonable values
		// int min = minmax.min;
		// int max = minmax.max;
		// if(max > 25)
		// {
		// max = 25;
		// }
		// int randomStateNumber = random.nextInt((max + 1) - min) + min;
		//
		// // now, heavily bias towards low numbers of states
		// if(random.nextInt() % 2 == 0)
		// {
		// // half the time it will be the minimum value
		// randomStateNumber = min;
		// }
		// else if(random.nextInt() % 2 == 0)
		// {
		// // 1/4 of the time it will be the minimum value + 1
		// if(min + 1 <= max)
		// {
		// randomStateNumber = min + 1;
		// }
		// }
		// else if(random.nextInt() % 2 == 0)
		// {
		// // 1/8 of the time it will be the minimum value + 2
		// if(min + 2 <= max)
		// {
		// randomStateNumber = min + 2;
		// }
		// }
		// else if(random.nextInt() % 2 == 0)
		// {
		// // 1/16 of the time it will be the minimum value + 3
		// if(min + 3 <= max)
		// {
		// randomStateNumber = min + 3;
		// }
		// }
		//
		// graphics.getControlPanel().getPropertiesPanel()
		// .getNumStatesField().setText("" + randomStateNumber);
		// }
		//
		// // choose a random rule number
		// if(graphics.getControlPanel().getRulePanel()
		// .getRuleNumberRandomButton().isEnabled())
		// {
		// graphics.getControlPanel().getRulePanel()
		// .getRuleNumberRandomButton().doClick();
		// }
		//
		// // switch to the Properties panel
		// graphics.getControlPanel().getTabbedPane().setSelectedComponent(
		// graphics.getControlPanel().getPropertiesPanel());
		//
		// // make the "best results" panel pulse
		// graphics.getControlPanel().getPropertiesPanel()
		// .getBestResultsEditorPane().startPulsing(15);
		//
		// // make the JFrame look disabled
		// graphics.setViewDisabled(true);
		//
		// // send a message to the user that they may wish to adjust the
		// // properties or cancel
		// URL randomURL = URLResource.getResource("/images/roulette200.gif");
		// RotatedImageIcon icon = null;
		// if(randomURL != null)
		// {
		// icon = new RotatedImageIcon(randomURL);
		// }
		// JLabel randomRuleInfo = new JLabel(RANDOM_RULE_INFO_TEXT + ruleName
		// + "\".</html>");
		// String continueButtonLabel = "Continue";
		// String stopAndAdjustButtonLabel = "Stop and adjust properties";
		// String cancelButtonLabel = "Cancel";
		// SpinningIconJOptionPane optionDialog = new SpinningIconJOptionPane(
		// randomRuleInfo, JOptionPane.INFORMATION_MESSAGE,
		// JOptionPane.YES_NO_OPTION, icon, new String[] {
		// continueButtonLabel, stopAndAdjustButtonLabel,
		// cancelButtonLabel}, null);
		//
		// // start spinning the icon on the JOptionPane
		// optionDialog.startSpin();
		//
		// // display the message
		// JDialog dialog = optionDialog.createDialog(graphics.getFrame(),
		// "Selecting random rule");
		// dialog.setVisible(true);
		//
		// // stop the spinning (they are done with the dialog)
		// optionDialog.stopSpin();
		//
		// // do they want to continue?
		// if(optionDialog.getValue() != null
		// && optionDialog.getValue().equals(continueButtonLabel))
		// {
		// // submit
		// graphics.getControlPanel().getAllPanelListener()
		// .submitProperties(true);
		// }
		// else
		// {
		// // restart the simulation
		// firePropertyChangeEvent(new PropertyChangeEvent(this,
		// CurrentProperties.PAUSE, null, new Boolean(false)));
		// }
		//
		// // stop pulsing unless they are going to adjust the properties
		// if((optionDialog.getValue() == null)
		// || !optionDialog.getValue()
		// .equals(stopAndAdjustButtonLabel))
		// {
		// // make the "best results" panel stop pulsing
		// graphics.getControlPanel().getPropertiesPanel()
		// .getBestResultsEditorPane().stopPulsing();
		// }
		//
		// // make the JFrame look enabled
		// graphics.setViewDisabled(false);
		// }
	}

	/**
	 * Shows the "about" panel with acknowledgments and license info.
	 */
	private void showAboutInfo()
	{
		// pause the simulation.
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(true)));

		// make the JFrame look disabled
		graphics.setViewDisabled(true);

		AboutDialog.showAboutDialog(graphics);

		// make the JFrame look enabled
		graphics.setViewDisabled(false);

		// restart the simulation
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.PAUSE, null, new Boolean(false)));
	}

	/**
	 * Shows the "easy" facade or the full interface.
	 * 
	 * @param enabled
	 *            When true, shows the facade. Otherwise shows the full
	 *            interface.
	 */
	public void showFacade(boolean enabled)
	{
		// update the properties
		CurrentProperties.getInstance().setFacade(enabled);

		// COMMENTED OUT SO THAT THE USER ALWAYS SEES BOTH OF THE BUTTONS
		// ALSO COMMENTED OUT IN THE CAToolBar.java CONSTRUCTOR.
		// if the facade is enabled, then show the toolbar button that disables
		// the facade, and vice-versa
		// graphics.getToolBar().getShowFacadeButton().setEnabled(!enabled);
		// graphics.getToolBar().getShowFullInterfaceButton().setEnabled(enabled);

		// if the facade is enabled, then select that radio button on the menu
		if(enabled)
		{
			showEasyFacadeItem.setSelected(true);
		}
		else
		{
			// and vice-versa
			showFullInterfaceItem.setSelected(true);
		}

		if(enabled && graphics.getToolBar().getStartButton().isEnabled())
		{
			// make the start button shimmy if the facade is enabled
			graphics.getToolBar().getStartButton().startShaking(
					ShimmyingTenTimesIconJButton.SUGGESTED_SHIMMYING_TIME);
		}
		else
		{
			// make the start button stop shimmying if the facade is disabled
			graphics.getToolBar().getStartButton().stopShaking();
		}

		// enable or disable panels
		JTabbedPane tabbedPane = graphics.getControlPanel().getTabbedPane();
		if(enabled)
		{
			// USE THE FACADE:

			// disable various panels
			for(int index = 0; index < tabbedPane.getTabCount(); index++)
			{
				if(!tabbedPane.getTitleAt(index).equals(
						DescriptionPanel.DESCRIPTION_TAB_TITLE)
						&& !tabbedPane.getTitleAt(index).equals(
								RulePanel.RULE_TAB_TITLE))
				{
					// will prevent disabling the additional properties panel
					// when added to the above if statement
					// && !tabbedPane
					// .getTitleAt(index)
					// .contains(
					// AdditionalPropertiesTabPanel.ADDITIONAL_PROPERTIES_TAB_TITLE)

					tabbedPane.setEnabledAt(index, false);

					// add some extra text to the tooltip that tells the user
					// how to reactivate the tab (but only add this text if we
					// haven't already added it)
					String toolTip = tabbedPane.getToolTipTextAt(index);
					if(!toolTip.contains(EZ_TAB_TOOLTIP)
							&& !toolTip.contains(EZ_TAB_TOOLTIP_WITHOUT_HTML))
					{
						int bodyIndex = toolTip.toLowerCase()
								.indexOf("</body>");
						int htmlIndex = toolTip.toLowerCase()
								.indexOf("</html>");
						if(bodyIndex != -1 && htmlIndex != -1)
						{
							// Contains both html and body tags.
							// Insert the new tool tip inside the html and body
							// tags.
							String newTip = toolTip.substring(0, bodyIndex)
									+ EZ_TAB_TOOLTIP
									+ toolTip.substring(bodyIndex);
							tabbedPane.setToolTipTextAt(index, newTip);

						}
						else if(htmlIndex != -1)
						{
							// Contains only an html tag (and no body tag).
							// This shouldn't happen but it does.
							// Insert the new tool tip inside the html tag.
							String newTip = toolTip.substring(0, htmlIndex)
									+ EZ_TAB_TOOLTIP
									+ toolTip.substring(htmlIndex);
							tabbedPane.setToolTipTextAt(index, newTip);
						}
						else
						{
							// does not contain either html or body tags
							tabbedPane.setToolTipTextAt(index, tabbedPane
									.getToolTipTextAt(index)
									+ EZ_TAB_TOOLTIP_WITHOUT_HTML);
						}
					}
				}
			}

			// select the rule panel
			for(int index = 0; index < tabbedPane.getTabCount(); index++)
			{
				if(tabbedPane.getTitleAt(index)
						.equals(RulePanel.RULE_TAB_TITLE))
				{
					tabbedPane.setSelectedIndex(index);
				}
			}

			// and open the rule tree's "all rules" folder, and scroll to the
			// currently selected rule
			String selectedRuleName = graphics.getControlPanel().getRulePanel()
					.getRuleTree().getSelectedRuleName();
			graphics.getControlPanel().getRulePanel().getRuleTree()
					.setSelectedRule(selectedRuleName);

			// and disable the rule number and random rule number
			// button.
			graphics.getControlPanel().getRulePanel().getRuleNumberTextField()
					.setEnabled(false);
			graphics.getControlPanel().getRulePanel()
					.getRuleNumberRandomButton().setEnabled(false);
		}
		else
		{
			// USE THE FULL INTERFACE:

			// enable all panels
			for(int index = 0; index < tabbedPane.getTabCount(); index++)
			{
				// enable every tab except the "More Properties" tab which is
				// handled on a rule by rule basis
				if(!tabbedPane
						.getTitleAt(index)
						.contains(
								AdditionalPropertiesTabPanel.ADDITIONAL_PROPERTIES_TAB_TITLE))
				{
					// enable the tab
					tabbedPane.setEnabledAt(index, true);

					// replace the tooltip
					String toolTip = tabbedPane.getToolTipTextAt(index);
					if(toolTip.contains(EZ_TAB_TOOLTIP))
					{
						// remove the part of the tool tip that is
						// EZ_TAB_TOOLTIP
						toolTip = toolTip.substring(0, toolTip
								.indexOf(EZ_TAB_TOOLTIP))
								+ toolTip.substring(toolTip
										.indexOf(EZ_TAB_TOOLTIP)
										+ EZ_TAB_TOOLTIP.length());
						tabbedPane.setToolTipTextAt(index, toolTip);
					}
					else if(toolTip.contains(EZ_TAB_TOOLTIP_WITHOUT_HTML))
					{
						// remove the part of the tool tip that is
						// EZ_TAB_TOOLTIP_WITHOUT_HTML
						toolTip = toolTip.substring(0, toolTip
								.indexOf(EZ_TAB_TOOLTIP_WITHOUT_HTML))
								+ toolTip.substring(toolTip
										.indexOf(EZ_TAB_TOOLTIP_WITHOUT_HTML)
										+ EZ_TAB_TOOLTIP_WITHOUT_HTML.length());
						tabbedPane.setToolTipTextAt(index, toolTip);
					}
				}
				else
				{
					// enable the "more properties" tab, if it exists
					if(graphics.getControlPanel()
							.getAdditionalPropertiesPanel()
							.doesAdditionalPropertiesPanelExist())
					{
						tabbedPane.setEnabledAt(index, true);
					}
					else
					{
						tabbedPane.setEnabledAt(index, false);
					}
				}
			}

			// enable the rule number field as necessary. It is always disabled
			// when in EZ mode.
			graphics.getControlPanel().getController().disableRuleNumberField(
					false);
		}
	}

	/**
	 * Turns the grid mesh on or off.
	 */
	private void toggleMesh()
	{
		// toggle the current state of the mesh
		if(meshVisible)
		{
			meshVisible = false;
			meshItem.setText(TOGGLE_MESH_ON);
		}
		else
		{
			meshVisible = true;
			meshItem.setText(TOGGLE_MESH_OFF);
		}

		// toggle the grid on or off
		graphics.getGraphicsPanel().setGridVisible(meshVisible);
	}

	/**
	 * Zooms the image by the specified factor.
	 * 
	 * @param factor.
	 *            The amount by which the image is zoomed.
	 */
	private void zoom(double factor)
	{
		// get the current position of the middle point on the scroll pane.
		// we will try to keep this point in the middle of the zoomed image.
		JScrollPane scrollPane = graphics.getScrollPane();
		JViewport viewport = scrollPane.getViewport();
		int viewWidth = viewport.getWidth();
		int viewHeight = viewport.getHeight();
		Point originalViewPosition = viewport.getViewPosition();
		double xMid = originalViewPosition.x + (viewWidth / 2.0);
		double yMid = originalViewPosition.y + (viewHeight / 2.0);

		// get the new middle and the new position for the top left corner)
		double xMidNew = xMid * factor;
		double yMidNew = yMid * factor;
		int xTopLeft = (int) Math.round(xMidNew - (viewWidth / 2.0));
		int yTopLeft = (int) Math.round(yMidNew - (viewHeight / 2.0));

		// resize the graphics in the scroll pane
		LatticeView graphicsPanel = graphics.getGraphicsPanel();
		try
		{
			// resize!
			graphicsPanel.resizePanel(factor);

			// now that it is resized, move to the correct position
			viewport.setViewPosition(new Point(xTopLeft, yTopLeft));
		}
		catch(Throwable t)
		{
			// if fails for any reason then reset to the original size (by
			// rescaling again).
			graphicsPanel.resizePanel(1.0 / factor);

			// now that it is resized, move to the correct position
			viewport.setViewPosition(originalViewPosition);

			// tell them there isn't enough memory
			zoomWarning();

			// disable the zoom button and menu
			zoomInItem.setEnabled(false);
			graphics.getToolBar().getZoomInButton().setEnabled(false);
		}
	}

	/**
	 * Actions to take when "Zoom In" is selected.
	 */
	private void zoomIn()
	{
		// this is memory intensive, so let's clean out the trash
		System.gc();

		// is there enough memory to zoom in?
		if(zoomMemoryOk())
		{
			// ok to zoom
			zoom(ZOOM_FACTOR);

			// now that I have zoomed-in, will I be able to zoom-in next time?
			if(!zoomMemoryOk())
			{
				// disable the zoom button and menu
				enableZoomIn(false);
			}

			// enable the "zoom out" button and menu
			zoomOutItem.setEnabled(true);
			graphics.getToolBar().getZoomOutButton().setEnabled(true);
		}
		else
		{
			// tell them there isn't enough memory
			zoomWarning();
		}
	}

	/**
	 * Tests if there is enough memory to zoom in.
	 * 
	 * @return boolean true if there are enough memory resources to zoom.
	 */
	private boolean zoomMemoryOk()
	{
		// give ourselves all the memory that we can
		System.gc();

		double percentMemoryMax = 0.5;

		boolean okToZoom = true;

		// find out if there is enough memory to create the bigger image
		try
		{
			LatticeView graphicsPanel = graphics.getGraphicsPanel();

			int newWidth = (int) (ZOOM_FACTOR * graphicsPanel.getWidth());
			int newHeight = (int) (ZOOM_FACTOR * graphicsPanel.getHeight());

			double percentMemoryUsed = MemoryManagementTools
					.getPercentMemoryUsed();
			if(percentMemoryUsed > percentMemoryMax)
			{
				okToZoom = false;
			}
			else
			{
				// this is where it will fail if the heap is too small
				BufferedImage img = GraphicsEnvironment
						.getLocalGraphicsEnvironment().getDefaultScreenDevice()
						.getDefaultConfiguration().createCompatibleImage(
								newWidth, newHeight);

				percentMemoryUsed = MemoryManagementTools
						.getPercentMemoryUsed();

				// release this test resource and garbage collect
				img = null;
				System.gc();

				if(percentMemoryUsed > percentMemoryMax)
				{
					okToZoom = false;
				}
			}

			// this is a hack, but I'm running up against memory limits that I
			// cannot locate
			if(newWidth > 2500 || newHeight > 2500)
			{
				okToZoom = false;
			}
		}
		catch(Throwable t)
		{
			// catch a Throwable because a heap error is a Throwable, not an
			// exception
			okToZoom = false;
		}
		finally
		{
			// release this test resource and garbage collect
			System.gc();
		}

		return okToZoom;
	}

	/**
	 * Actions to take when "Zoom Out" is selected.
	 */
	private void zoomOut()
	{
		double zoomFactor = 1.0 / ZOOM_FACTOR;

		// can I zoom out, or will that obliterate pixels?
		int numRows = CurrentProperties.getInstance().getNumRows();
		int numCols = CurrentProperties.getInstance().getNumColumns();
		LatticeView graphicsPanel = graphics.getGraphicsPanel();
		if(graphicsPanel.getDisplayWidth() * zoomFactor <= numCols
				|| graphicsPanel.getDisplayHeight() * zoomFactor <= numRows)
		{
			// Can't zoom in, so disable the zoom button and menu
			enableZoomOut(false);
		}
		else
		{
			// now zoom
			zoom(zoomFactor);
		}

		// now that I have zoomed out, will I be able to zoom out next time?
		if(graphicsPanel.getDisplayWidth() <= numCols
				|| graphicsPanel.getDisplayHeight() <= numRows)
		{
			// Can't zoom in any further, so disable the zoom button and menu
			enableZoomOut(false);
		}

		// enable the "zoom in" button and menu
		zoomInItem.setEnabled(true);
		graphics.getToolBar().getZoomInButton().setEnabled(true);
	}

	/**
	 * A warning printed when there is insufficient memory to zoom.
	 */
	private void zoomWarning()
	{
		// make the JFrame look disabled
		graphics.setViewDisabled(true);

		String warning = "Insufficient memory to zoom further. \n"
				+ "Try again in a few minutes.";
		JOptionPane.showMessageDialog(null, warning, "Insufficient memory",
				JOptionPane.INFORMATION_MESSAGE);

		// make the JFrame look disabled
		graphics.setViewDisabled(false);
	}

	/**
	 * Handles events when menu items are selected.
	 */
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(EXIT))
		{
			// Notify anyone who cares.
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.EXIT, CurrentProperties.FALSE,
					CurrentProperties.TRUE));
		}
		else if(e.getActionCommand().equals(SAVE))
		{
			String filePath = CurrentProperties.getInstance()
					.getSaveDataFilePath();

			if((filePath == null) || (filePath == ""))
			{
				saveAs();
			}
			else
			{
				// notify anyone who cares
				firePropertyChangeEvent(new PropertyChangeEvent(this,
						CurrentProperties.SAVE_DATA, CurrentProperties.FALSE,
						CurrentProperties.TRUE));
			}
		}
		else if(e.getActionCommand().equals(SAVE_AS))
		{
			saveAs();
		}
		else if(e.getActionCommand().equals(SAVE_AS_IMAGE))
		{
			saveAsImage();
		}
		else if(e.getActionCommand().equals(SAVE_AS_MOVIE))
		{
			saveAsMovie();
		}
		else if(e.getActionCommand().equals(SLOW_DOWN))
		{
			changeSpeed(true);
			// changeSpeed(1.0 / SPEED_FACTOR);
		}
		else if(e.getActionCommand().equals(SPEED_UP))
		{
			changeSpeed(false);
			// changeSpeed(SPEED_FACTOR);
		}
		else if(e.getActionCommand().equals(STOP_MOVIE))
		{
			// stop the movie, if it is being created
			if(MovieMaker.isOpen())
			{
				MovieMaker.closeMovie();
			}

			// enable the movie buttons
			saveAsMovieItem.setEnabled(true);
			graphics.getToolBar().getMovieButton().setEnabled(true);

			// disable the cut movie buttons
			stopMovieItem.setEnabled(false);
			graphics.getToolBar().getCutMovieButton().setEnabled(false);
		}
		else if(e.getActionCommand().equals(IMPORT_SIMULATION))
		{
			loadSimulation();
		}
		else if(e.getActionCommand().equals(IMPORT_DATA))
		{
			loadData();
		}
		else if(e.getActionCommand().equals(IMPORT_DATA_IMAGE))
		{
			loadImage();
		}
		else if(e.getActionCommand().equals(ABOUT))
		{
			showAboutInfo();
		}
		else if(e.getActionCommand().equals(SHOW_EASY_FACADE))
		{
			// display a message if the user keeps clicking the button like they
			// are confused
			ezClickCount++;
			fullInterfaceClickCount = 0;
			if(ezClickCount > 2)
			{
				URL showFacadeURL = URLResource.getResource("/images/ez.gif");
				Icon facadeIcon = new ImageIcon(showFacadeURL);

				String warningMessage = "The easy interface has already been selected.\n\n"
						+ "Click the \"ALL\" button to activate the full interface \n"
						+ "with all tabs.\n";

				WarningManager.displayWarningWithMessageDialog(warningMessage,
						NUM_FACADE_WARNINGS, this, "EZ already activated",
						JOptionPane.INFORMATION_MESSAGE, facadeIcon);
			}

			// make sure all of the analyses are docked
			graphics.getControlPanel().getAllPanelListener().dockAllAnalyses();

			showFacade(true);
		}
		else if(e.getActionCommand().equals(SHOW_FULL_INTERFACE))
		{
			// display a message if the user keeps clicking the button like they
			// are confused
			fullInterfaceClickCount++;
			ezClickCount = 0;
			if(fullInterfaceClickCount > 2)
			{
				URL showFullInterfaceURL = URLResource
						.getResource("/images/all.gif");
				Icon fullInterfaceIcon = new ImageIcon(showFullInterfaceURL);

				String warningMessage = "The full interface has already been selected.\n\n"
						+ "Click the \"EZ\" button to simplify the interface.\n";

				WarningManager.displayWarningWithMessageDialog(warningMessage,
						NUM_FACADE_WARNINGS, this, "ALL already activated",
						JOptionPane.INFORMATION_MESSAGE, fullInterfaceIcon);
			}

			showFacade(false);
		}
		else if(e.getActionCommand().equals(HELP))
		{
			// pause the simulation.
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.PAUSE, null, new Boolean(true)));

			new CAHelp(graphics.getToolBar().getHelpButton(), this);

			// restart the simulation
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.PAUSE, null, new Boolean(false)));
		}
		else if(e.getActionCommand().equals(GETTING_STARTED))
		{
			new CAGettingStarted(graphics.getToolBar().getHelpButton(), this);
		}
		else if(e.getActionCommand().equals(GUIDED_TOUR))
		{
			new CAGuidedTour(graphics.getToolBar().getHelpButton(), this);
		}
		else if(e.getActionCommand().equals(OBESITY_TUTORIAL))
		{
			new CAObesityTutorial(graphics.getToolBar().getHelpButton(), this);
		}
		else if(e.getActionCommand().equals(MEMORY_TUTORIAL))
		{
			new CAIncreasingMemoryTutorial(graphics.getToolBar()
					.getHelpButton(), this);
		}
		else if(e.getActionCommand().equals(WRITE_YOUR_OWN_RULE))
		{
			// pause the simulation.
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.PAUSE, null, new Boolean(true)));

			new CAHowToWriteYourOwnRule(graphics.getToolBar().getHelpButton(),
					this);

			// restart the simulation
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.PAUSE, null, new Boolean(false)));
		}
		else if(e.getActionCommand().equals(HOW_MANY_RULES_ARE_THERE))
		{
			// pause the simulation.
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.PAUSE, null, new Boolean(true)));

			new CAHowManyRulesAreThere(graphics.getToolBar().getHelpButton(),
					this);

			// restart the simulation
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.PAUSE, null, new Boolean(false)));
		}
		else if(e.getActionCommand().equals(SHOW_HIDDEN_TABS))
		{
			// pause the simulation.
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.PAUSE, null, new Boolean(true)));

			new CAShowHiddenTabs(graphics.getToolBar().getHelpButton(), this);

			// restart the simulation
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.PAUSE, null, new Boolean(false)));
		}
		else if(e.getActionCommand().equals(HAVE_PROPERTIES_CHANGED))
		{
			// pause the simulation.
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.PAUSE, null, new Boolean(true)));

			new CAPropertiesChanged(graphics.getToolBar().getHelpButton(), this);

			// restart the simulation
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.PAUSE, null, new Boolean(false)));
		}
		else if(e.getActionCommand().equals(CA_RULE_CONSTRUCTORS))
		{
			// pause the simulation.
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.PAUSE, null, new Boolean(true)));

			new CARuleConstructors(graphics.getToolBar().getHelpButton(), this);

			// restart the simulation
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.PAUSE, null, new Boolean(false)));
		}
		else if(e.getActionCommand().equals(PRINT))
		{
			// can't print from here because we don't have the graphics objects.
			// So notify anyone else who cares (CAControllerListener)
			firePropertyChangeEvent(new PropertyChangeEvent(this, PRINT, null,
					""));

			// This assumes that the user releases the ctrl key after selecting
			// the printer. The user selects ctrl-P to activate the printer
			// dialog (on windows). But if they release the ctrl key while the
			// print dialog has focus, then the ctrl-release event is not heard.
			// This is a work-around necessitated by Macs (replaces right-clicks
			// with ctrl-clicks). NOTE: I'd rather bind the print dialog to the
			// ctrl key (see CAFrame.bindCtrlKey() method) but I don't have
			// access to the print dialog.
			if(Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() == ActionEvent.CTRL_MASK)
			{
				// make it act like the user released the ctrl key (which they
				// almost certainly did). If I don't do this, the ctrl-key
				// release event will probably be missed. See the
				// CAFrame.bindCtrlKey() method.
				CAFrame.controlKeyDown = false;
			}
		}
		else if(e.getActionCommand().equals(CHOOSE_RIGHTCLICK_DRAW_COLOR))
		{
			// choose a color for drawing cells with a right click
			chooseSecondDrawColor();
		}
		else if(e.getActionCommand().equals(CHOOSE_DRAW_COLOR))
		{
			// choose a color for drawing cells with a left click
			chooseDrawColor();
		}
		else if(e.getActionCommand().equals(CHOOSE_EMPTY_COLOR))
		{
			// choose a color for unoccupied cells
			chooseEmptyColor();
		}
		else if(e.getActionCommand().equals(CHOOSE_FILLED_COLOR))
		{
			// choose a color for occupied cells
			chooseFilledColor();
		}
		else if(e.getActionCommand().equals(CHOOSE_DEFAULT_COLOR_SCHEME))
		{
			// set the color scheme
			setColorScheme(CHOOSE_DEFAULT_COLOR_SCHEME);
		}
		else if(e.getActionCommand().equals(CHOOSE_FIRE_COLOR_SCHEME))
		{
			// set the color scheme
			setColorScheme(CHOOSE_FIRE_COLOR_SCHEME);
		}
		else if(e.getActionCommand().equals(CHOOSE_BLUE_DIAMOND_COLOR_SCHEME))
		{
			// set the color scheme
			setColorScheme(CHOOSE_BLUE_DIAMOND_COLOR_SCHEME);
		}
		else if(e.getActionCommand().equals(CHOOSE_BLUE_SHADES_COLOR_SCHEME))
		{
			// set the color scheme
			setColorScheme(CHOOSE_BLUE_SHADES_COLOR_SCHEME);
		}
		else if(e.getActionCommand().equals(CHOOSE_GREEN_OCEAN_COLOR_SCHEME))
		{
			// set the color scheme
			setColorScheme(CHOOSE_GREEN_OCEAN_COLOR_SCHEME);
		}
		else if(e.getActionCommand().equals(CHOOSE_PURPLE_HAZE_COLOR_SCHEME))
		{
			// set the color scheme
			setColorScheme(CHOOSE_PURPLE_HAZE_COLOR_SCHEME);
		}
		else if(e.getActionCommand().equals(CHOOSE_WATER_LILIES_COLOR_SCHEME))
		{
			// set the color scheme
			setColorScheme(CHOOSE_WATER_LILIES_COLOR_SCHEME);
		}
		else if(e.getActionCommand().equals(CHOOSE_GRAY_SMOKE_COLOR_SCHEME))
		{
			// set the color scheme
			setColorScheme(CHOOSE_GRAY_SMOKE_COLOR_SCHEME);
		}
		else if(e.getActionCommand()
				.equals(CHOOSE_BLACK_AND_WHITE_COLOR_SCHEME))
		{
			// set the color scheme
			setColorScheme(CHOOSE_BLACK_AND_WHITE_COLOR_SCHEME);
		}
		else if(e.getActionCommand()
				.equals(CHOOSE_WHITE_AND_BLACK_COLOR_SCHEME))
		{
			// set the color scheme
			setColorScheme(CHOOSE_WHITE_AND_BLACK_COLOR_SCHEME);
		}
		else if(e.getActionCommand().equals(CHOOSE_YELLOW_JACKET_COLOR_SCHEME))
		{
			// set the color scheme
			setColorScheme(CHOOSE_YELLOW_JACKET_COLOR_SCHEME);
		}
		else if(e.getActionCommand().equals(CHOOSE_RANDOM_COLOR_SCHEME))
		{
			// set the color scheme
			setColorScheme(CHOOSE_RANDOM_COLOR_SCHEME);
		}
		else if(e.getActionCommand().equals(RESET_APPLICATION))
		{
			// reset the application to its "out of the box" parameters
			resetApplication();
		}
		else if(e.getActionCommand().equals(CHOOSE_FILE_DELIMETER))
		{
			// choose a file delimiter
			chooseFileDelimeter();
		}
		else if(e.getActionCommand().equals(CHOOSE_RULE_FOLDER))
		{
			// choose a rule folder
			chooseRuleFolder();
		}
		else if(e.getActionCommand().equals(RESTORE_DEFAULT_COLOR))
		{
			// set the color scheme
			setColorScheme(CHOOSE_DEFAULT_COLOR_SCHEME);
			defaultColorSchemeItem.setSelected(true);
		}
		else if(e.getActionCommand().equals(TAGGED_TRANSLUCENT))
		{
			displayTaggedCellsAsTranslucent(true);
		}
		else if(e.getActionCommand().equals(TAGGED_OPAQUE))
		{
			displayTaggedCellsAsTranslucent(false);
		}
		else if(e.getActionCommand().equals(TAGGED_INVISIBLE))
		{
			doNotDisplayTaggedCells(true);
		}
		else if(e.getActionCommand().equals(ZOOM_IN))
		{
			// zoom in
			zoomIn();
		}
		else if(e.getActionCommand().equals(ZOOM_OUT))
		{
			// zoom out
			zoomOut();
		}
		else if(e.getActionCommand().equals(FIT_TO_SCREEN))
		{
			// fit the graphics to the screen
			fitToScreen();
		}
		else if(e.getActionCommand().equals(TOGGLE_MESH))
		{
			// turn on or off the grid mesh
			toggleMesh();
		}
		else if(e.getActionCommand().equals(START))
		{
			// programatically click the start button in the StartPanel.
			graphics.getControlPanel().getStartPanel().getStartButton()
					.doClick();
		}
		else if(e.getActionCommand().equals(STOP))
		{
			// programatically click the stop button in the StartPanel.
			graphics.getControlPanel().getStartPanel().getStopButton()
					.doClick();
		}
		else if(e.getActionCommand().equals(STEP1))
		{
			// programatically click the increment button in the StartPanel.
			graphics.getControlPanel().getStartPanel().getIncrementButton()
					.doClick();
		}
		else if(e.getActionCommand().equals(STEP10))
		{
			// programatically click the increment button in the StartPanel.
			graphics.getControlPanel().getStartPanel().getStep10Button()
					.doClick();
		}
		else if(e.getActionCommand().equals(STEP_BACK))
		{
			// programatically click the increment button in the StartPanel.
			graphics.getControlPanel().getStartPanel().getStepBackButton()
					.doClick();
		}
		else if(e.getActionCommand().equals(STEP_FILL))
		{
			// programatically click the increment button in the StartPanel.
			graphics.getControlPanel().getStartPanel().getStepFillButton()
					.doClick();
		}
		else if(e.getActionCommand().equals(DISPLAY_LINKS_IN_BROWSER))
		{
			// change the property file and the menu
			displayLinksInBrowser();
		}
		else if(e.getActionCommand().equals(DISPLAY_LINKS_IN_APPLICATION))
		{
			// change the property file and the menu
			displayLinksInApplication();
		}
		else if(e.getActionCommand().equals(SET_MAX_RULE_DIGITS))
		{
			// set the maximum size of the rule in digits
			setMaxRuleSize();
		}
		else if(e.getActionCommand().equals(RANDOM))
		{
			if(CurrentProperties.getInstance().isFacadeOn())
			{
				// sets a random rule from among the facade simulations
				setRandomFacadeRule();
			}
			else
			{
				// sets a random rule from all possibilities (not just facade
				// simulations)
				setRandomRule();
			}
		}
		else if(e.getActionCommand().equals(MOVE_CONTROLS))
		{
			// flip the layout
			if(isLeftLayout)
			{
				moveControlsRight();
				isLeftLayout = false;
			}
			else
			{
				moveControlsLeft();
				isLeftLayout = true;
			}
		}
		else if(e.getActionCommand().equals(SET_NUM_PROCESSORS_FROM_POPUP))
		{
			// set the number of parallel processors that will be used
			chooseNumberOfProcessors();
		}
		else
		{
			for(int i = 0; i < NUMBER_OF_AVAILABLE_PROCESSORS; i++)
			{
				// note the change in tense "processor(s)", so must test for
				// both. Must use (i+1) even in second case, so that doesn't
				// give a false positive when i > 1.
				if(e.getActionCommand()
						.equals("use " + (i + 1) + " processors")
						|| e.getActionCommand().equals(
								"use " + (i + 1) + " processor"))
				{
					// set the number of parallel processors that will be used
					setNumberOfProcessors(i + 1);
				}
			}
		}
	}

	/**
	 * Get the menu item that flips the layout by changing the side of the
	 * control panel.
	 */
	public JMenuItem getFlipLayoutMenuItem()
	{
		return flipLayoutItem;
	}

	/**
	 * Get the menu item that increments the simulation.
	 */
	public JMenuItem getIncrementMenuItem()
	{
		return step1Item;
	}

	/**
	 * Get the menu item that Saves data.
	 */
	public JMenuItem getSaveMenuItem()
	{
		return saveItem;
	}

	/**
	 * Get the menu item that starts the simulation.
	 */
	public JMenuItem getStartMenuItem()
	{
		return startItem;
	}

	/**
	 * Get the menu item that steps the simulation by 10 generations.
	 */
	public JMenuItem getStep10MenuItem()
	{
		return step10Item;
	}

	/**
	 * Get the menu item that steps the simulation backwords by 1 generation.
	 */
	public JMenuItem getStepBackMenuItem()
	{
		return stepBackItem;
	}

	/**
	 * Get the menu item that fills the lattice by incrementing by rows - 1.
	 */
	public JMenuItem getStepFillMenuItem()
	{
		return stepFillItem;
	}

	/**
	 * Get the menu item that starts the simulation.
	 */
	public JMenuItem getStopMenuItem()
	{
		return stopItem;
	}

	/**
	 * Whether or not the control panel is on the left side of the layout.
	 * 
	 * @return true if the control panel is on the left.
	 */
	public boolean isLeftLayout()
	{
		return isLeftLayout;
	}

	/**
	 * Handles notification of any changes in properties.
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		// the Cell will notify whenever there is unsaved data
		if(event.getPropertyName().equals(Cell.UNSAVED_DATA))
		{
			// if there is new data in the cell, then enable the Save menu item
			if(event.getNewValue().equals(CurrentProperties.TRUE))
			{
				saveItem.setEnabled(true);
			}
			else
			{
				saveItem.setEnabled(false);
			}
		}
		else if(event.getPropertyName().equals(CurrentProperties.SETUP))
		{
			// Note that a SETUP event occurs when the properties panel submits.
			// That's when we have to check for changes in the "number of
			// states" text field.

			// decide whether or not the drawItems should be enabled.
			String currentRuleClassName = CurrentProperties.getInstance()
					.getRuleClassName();
			Rule currentRule = ReflectionTool
					.instantiateFullRuleFromClassName(currentRuleClassName);
			if(IntegerCellState.isCompatibleRule(currentRule))
			{
				leftDrawItem.setEnabled(true);
				rightDrawItem.setEnabled(true);
			}
			else
			{
				leftDrawItem.setEnabled(false);
				rightDrawItem.setEnabled(false);
			}
		}
	}

	/**
	 * Notify listeners of a property change.
	 * 
	 * @param event
	 *            Holds the changed property.
	 */
	public void firePropertyChangeEvent(PropertyChangeEvent event)
	{
		EventListener[] listener = listenerList
				.getListeners(PropertyChangeListener.class);
		for(int i = 0; i < listener.length; i++)
		{
			((PropertyChangeListener) listener[i]).propertyChange(event);
		}
	}

	/**
	 * Adds a change listener.
	 * 
	 * @param listener
	 *            the listener to add.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		listenerList.add(PropertyChangeListener.class, listener);
	}

	/**
	 * Removes a change listener.
	 * 
	 * @param listener
	 *            the listener to remove.
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		listenerList.remove(PropertyChangeListener.class, listener);
	}

	/**
	 * Listens for the OK button on the color chooser.
	 */
	private class OkFilledAndEmptyColorListener implements ActionListener
	{
		private String actionCommand = null;

		public OkFilledAndEmptyColorListener(String actionCommand)
		{
			this.actionCommand = actionCommand;
		}

		public void actionPerformed(ActionEvent e)
		{
			Color selectedColor = colorChooser.getColor();
			if(selectedColor != null)
			{
				if(actionCommand.equals(CHOOSE_EMPTY_COLOR))
				{
					setEmptyColor(selectedColor);
					CurrentProperties.getInstance()
							.setEmptyColor(selectedColor);
				}
				else if(actionCommand.equals(CHOOSE_FILLED_COLOR))
				{
					setFilledColor(selectedColor);
					CurrentProperties.getInstance().setFilledColor(
							selectedColor);
				}
			}
		}
	}

	/**
	 * Listens for the OK button on the integer color chooser (drawing colors).
	 */
	private class OkDrawingColorListener implements ActionListener
	{
		private String actionCommand = null;

		public OkDrawingColorListener(String actionCommand)
		{
			this.actionCommand = actionCommand;
		}

		public void actionPerformed(ActionEvent e)
		{
			Color selectedColor = integerColorChooser.getColor();
			int selectedState = integerColorChooser.getState();
			if(selectedColor != null)
			{
				if(actionCommand.equals(CHOOSE_DRAW_COLOR))
				{
					// change the drawing color and state
					ColorScheme.DRAW_COLOR = selectedColor;
					IntegerCellState.DRAW_STATE = selectedState;
				}
				else if(actionCommand.equals(CHOOSE_RIGHTCLICK_DRAW_COLOR))
				{
					// change the drawing color
					ColorScheme.SECOND_DRAW_COLOR = selectedColor;
					IntegerCellState.SECOND_DRAW_STATE = selectedState;
				}
			}
		}
	}

	/**
	 * Listens for the Cancel button on the color chooser.
	 */
	private class CancelListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
		}
	}
}
