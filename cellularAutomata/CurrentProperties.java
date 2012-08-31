/*
 CurrentProperties -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.Properties;
import javax.swing.JOptionPane;

import cellularAutomata.graphics.AllPanelController;
import cellularAutomata.graphics.StartPanel;
import cellularAutomata.graphics.colors.RainbowColorScheme;
import cellularAutomata.io.FileStorage;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.SquareLattice;
import cellularAutomata.lattice.RandomGaussianLattice;
import cellularAutomata.reflection.AnalysisHash;
import cellularAutomata.reflection.LatticeHash;
import cellularAutomata.reflection.RuleHash;
import cellularAutomata.rules.DiffusionLimitedAggregation;
import cellularAutomata.rules.IntegerRule;
import cellularAutomata.rules.Lambda;
import cellularAutomata.rules.WolframRuleNumber;

/**
 * Stores the properties of the current running simulation. Each time a new
 * simulation is submitted, these properties are updated. Some rules may also
 * (rarely) update the properties while the simulation is running. Properties
 * can be observed from a Properties object or, preferable, from getters.
 * <p>
 * Also, when requested, reads the properties from a file. The file should have
 * the format key = value. Each key and value should be on separate lines. The
 * value may be any length and may have multiple items such as "0 0 1 0". All
 * keys and values are stored as Strings.
 * 
 * @author David Bahr
 */
public class CurrentProperties
{
    // the number of rows in the simulation
    private int numRows = 100;

    // the number of columns in the simulation
    private int numColumns = 100;

    // wrap-around or reflection boundary condition
    private int boundaryCondition = Lattice.WRAP_AROUND_BOUNDARY;

    // color scheme
    private String colorScheme = RainbowColorScheme.schemeName;

    // empty color
    private int emptyColor = RainbowColorScheme.DEFAULT_EMPTY_COLOR.getRGB();

    // filled color
    private int filledColor = RainbowColorScheme.DEFAULT_FILLED_COLOR.getRGB();

    // data separator when reading and writing files
    private String dataDelimiters = "\t";

    // whether or not to display links in a separate browser or within the
    // application
    private boolean displayHyperLinksInBrowser = true;

    // The number of time steps between displaying the CA. For example, a 2
    // would mean display the CA every other time step.
    private int displayStep = 1;

    // the initial state
    private String initialState = STATE_RANDOM;

    // the initial state file path
    private String initialStateDataFilePath = "";

    // whether or not the initial state ellipse is filled
    private boolean initialStateEllipseFilled = false;

    // the initial state ellipse height
    private int initialStateEllipseHeight = 25;

    // the initial state ellipse width
    private int initialStateEllipseWidth = 25;

    // the initial state image file path
    private String initialStateImageFilePath = "";

    // whether or not the initial state rectangle is filled
    private boolean initialStateRectangleFilled = false;

    // the initial state rectangle height
    private int initialStateRectangleHeight = 25;

    // the initial state rectangle width
    private int initialStateRectangleWidth = 25;

    // true if the simplified interface or facade is turned on
    private boolean isFacadeOn = false;

    // langton's lambda, used in the lambda rule
    private double lambda = 0.5;

    // the display name of the lattice
    private String latticeDisplayName = SquareLattice.DISPLAY_NAME;

    // the maximum number of digits that can be used to specify a rule number
    private int maxRuleNumberSizeInDigits = IntegerRule.DEFAULT_MAX_RULE_SIZE;

    // the maximum number of generations before the CA pauses.
    private int maxTime = 1000000000;

    // the number of state used by the CA (applicable to integer-based rules)
    private int numStates = 2;

    // the radius of the neighborhood (when relevant)
    private int neighborhoodRadius = 1;

    // the number of processors that will be used for parallel processing
    private int numberOfProcessors = 1;

    // the random percentage of sites that will be filled when "random" is
    // selected as an initial state. A number from 0 to 100.
    private int randomPercent = 50;

    // the amount of random gas in a diffusion limited aggregation simulation
    private int randomPercentDiffusionGas = DiffusionLimitedAggregation.DEFAULT_RANDOM_PERCENT;

    // when an initial state of "random probabilities" is selected, this
    // specifies the amount assigned to each state. Should be a string of
    // integers, one for each state, each separated by the dataDelimiters, and
    // should all sum to 100.
    private String randomPercentPerState = "100\t0";

    // the number of steps that the CA can rewind
    private int rewindSteps = CAFactory.DEFAULT_REWIND_STEPS;

    // the class name (with package) of the current rule
    private String ruleClassName = "cellularAutomata.rules.Life";

    // the rule number (when relevant)
    private BigInteger ruleNumber = new BigInteger(""
        + WolframRuleNumber.DEFAULT_RULE);

    // the number of time steps averaged together on the display
    private int runningAverage = 1;

    // the file path for saving data
    private String saveDataFilePath = "";

    // the file path for saving the simulation as an image
    private String saveImageFilePath = "";

    // the standard deviation used when setting up the Guassian lattice
    private double standardDeviation = RandomGaussianLattice.DEFAULT_STANDARD_DEVIATION;

    // the delay in milliseconds between each cell update
    private long timeDelay = 100;

    // true if should only update the graphics at the end of the simulation
    private boolean updateAtEnd = false;

    // the folder where users place their own analyses (their .class files)
    private String userAnalysisFolder = AnalysisHash.DEFAULT_USER_ANALYSIS_FOLDER;

    // the package where users place their own analyses (their .class files)
    private String userAnalysisPackage = AnalysisHash.DEFAULT_USER_ANALYSIS_PACKAGE;

    // the folder where users place their own rules (their .class files)
    private String userRuleFolder = RuleHash.DEFAULT_USER_RULE_FOLDER;

    // the package where users place their own rules (their .class files)
    private String userRulePackage = RuleHash.DEFAULT_USER_RULE_PACKAGE;

    // a collection of all the CA properties
    private Properties properties = new Properties();

    /**
     * Key to the property that specifies the graphics should be refreshed, if
     * possible. The CA controller may override and decide not to update the
     * graphics (for example, if the graphics are only supposed to be updated at
     * the end of the simulation). Use UPDATE_GRAPHICS to force an update no
     * matter what.
     */
    public final static String REFRESH_GRAPHICS = "refresh graphics";

    /**
     * Key to the property that specifies the boundary condition.
     */
    public final static String BOUNDARY_CONDITION = "boundary type";

    /**
     * Key to the property that specifies the CA height (number of cells).
     */
    public final static String CA_HEIGHT = "ca_height";

    /**
     * Key to the property that specifies the CA width (number of cells).
     */
    public final static String CA_WIDTH = "ca_width";

    /**
     * Key to the property that specifies whether or not to clear the graphics.
     */
    public final static String CLEAR = "Clear";

    /**
     * Key to the property that says the graphics colors have just changed.
     */
    public final static String COLORS_CHANGED = "colors_changed";

    /**
     * Key to the property that specifies the color scheme.
     */
    public final static String COLOR_SCHEME = "Color scheme";

    /**
     * Key to the property that specifies data delimiters.
     */
    public final static String DATA_DELIMITERS = "delimiters";

    /**
     * The amount of random gas in a diffusion limited aggregation simulation.
     */
    public static final String RANDOM_PERCENT_DIFFUSION = "DiffusionLimitedAggregation random";

    /**
     * Key to the property that specifies whether or not hyperlinks will open a
     * new browser or display within the application.
     */
    public final static String DISPLAY_HYPERLINKS_IN_BROWSER = "display hyperlinks in "
        + "separate browser";

    /**
     * Key to the property that specifies how many time steps will lapse between
     * displaying the CA. For example, a 2 would mean display the CA every other
     * time step.
     */
    public final static String DISPLAY_STEP = "display step";

    /**
     * Key to the property that specifies the empty color (as a single RGB
     * integer).
     */
    public final static String EMPTY_COLOR = "empty color";

    /**
     * Key to the property that specifies the CA should continue or stop.
     */
    public final static String EXIT = "exit";

    /**
     * A possible value for the properties.
     */
    public final static String FALSE = "false";

    /**
     * Key to the property that specifies the filled color (as a single RGB
     * integer).
     */
    public final static String FILLED_COLOR = "filled color";

    /**
     * Key to the property that specifies when a new simulation should be
     * loaded.
     */
    public final static String IMPORT_SIMULATION = "import simulation";

    /**
     * Key to the property that specifies the initial state of the CA.
     */
    public final static String INITIAL_STATE = "initial state";

    /**
     * Key to the property that specifies the initial state data file path of
     * the CA.
     */
    public final static String INITIAL_STATE_DATA_FILE_PATH = "data_file_path";

    /**
     * Key to the property that specifies whether or not the initial state
     * ellipse should be filled.
     */
    public final static String INITIAL_STATE_ELLIPSE_FILLED = "ellipse_filled";

    /**
     * Key to the property that specifies the initial state height of an
     * ellipse.
     */
    public final static String INITIAL_STATE_ELLIPSE_HEIGHT = "ellipse_height";

    /**
     * Key to the property that specifies the initial state width of an ellipse.
     */
    public final static String INITIAL_STATE_ELLIPSE_WIDTH = "ellipse_width";

    /**
     * Key to the property that specifies the initial state image file path of
     * the CA.
     */
    public final static String INITIAL_STATE_IMAGE_FILE_PATH = "image_file_path";

    /**
     * Key to the property that specifies whether or not the initial state
     * rectangle should be filled.
     */
    public final static String INITIAL_STATE_RECTANGLE_FILLED = "rectangle_filled";

    /**
     * Key to the property that specifies the initial state height of a
     * rectangle.
     */
    public final static String INITIAL_STATE_RECTANGLE_HEIGHT = "rectangle_height";

    /**
     * Key to the property that specifies the initial state width of a
     * rectangle.
     */
    public final static String INITIAL_STATE_RECTANGLE_WIDTH = "rectangle_width";

    /**
     * Key to the property that specifies if the simplified interface or facade
     * is on.
     */
    public final static String IS_FACADE_ON = "facade_is_on";

    /**
     * Key to the property that specifies the value of Langton's lambda.
     */
    public final static String LAMBDA = "lambda";

    /**
     * Key to the property that specifies the type of lattice used by the CA.
     */
    public final static String LATTICE = "lattice";

    /**
     * Key to the property that specifies the maximum number of *digits* that
     * can be in a rule number.
     */
    public final static String MAX_RULE_NUMBER_SIZE_IN_DIGITS = "max rule number size in digits";

    /**
     * Key to the property that specifies the maximum number of generations
     * before the CA pauses.
     */
    public final static String MAX_TIME = "maximum_iterations";

    /**
     * Key to the property that specifies the number of states used by the CA.
     */
    public final static String NUMBER_OF_PROCESSORS = "Number of parallel processors";

    /**
     * Key to the property that specifies the number of states used by the CA.
     */
    public final static String NUMBER_OF_STATES = "Number of states";

    /**
     * Key to the property that specifies whether or not the "Number of States"
     * text field is enabled on the Property panel.
     */
    public final static String NUM_STATES_TEXTFIELD_ENABLED = "Number of states enabled";

    /**
     * Key to the property that says to pause or restart the simulation.
     */
    public final static String PAUSE = "pause";

    /**
     * Key to the property that specifies the radius of the neighborhood.
     */
    public final static String RADIUS = "Radius";

    /**
     * Key to the property that specifies the percentage of cells that will be
     * randomly filled.
     */
    public final static String RANDOM_PERCENT = "Random percent";

    /**
     * Key to the property that specifies the percentage of each state that will
     * be randomly placed in the cells. Only used when INITIAL_STATE is set to
     * STATE_PROBABILITY.
     */
    public final static String RANDOM_PERCENT_PER_STATE = "Random percent per state";

    /**
     * Key to the property that specifies the CA should be rewound by one
     * generation (if possible).
     */
    public final static String REWIND = "rewind";

    /**
     * Key to the property that specifies how many steps the CA can be rewound.
     */
    public final static String REWIND_STEPS = "rewind steps";

    /**
     * Key to the property that specifies the cellular automaton rule. Should
     * store a class name.
     */
    public final static String RULE = "rule";

    /**
     * Key to the property that specifies how many generations to average when
     * displaying cell values.
     */
    public final static String RUNNING_AVERAGE = "running average";

    /**
     * Key to the property that specifies the cellular automaton rule number.
     * Only used for one-dimensional rules.
     */
    public final static String RULE_NUMBER = "rule_number";

    /**
     * Key to the property that specifies the file path to which data is saved
     * as an image.
     */
    public final static String SAVE_AS_IMAGE_PATH = "save_as_image_file_path";

    /**
     * Key to the property that specifies the file path to which data is saved.
     */
    public final static String SAVE_DATA_PATH = "save_data_file_path";

    /**
     * Key to the property that specifies whether or not to save the CA data.
     */
    public final static String SAVE_DATA = "save_data";

    /**
     * Key to the property that decides if the CA needs to be set up (for
     * example, the lattice choice may have changed, so if this property is set
     * to "true", then the CA should reset the lattice).
     */
    public final static String SETUP = "setup";

    /**
     * Key to the property that specifies the standard deviation of the
     * distribution of neighbors in the Guassian random lattice.
     */
    public final static String STANDARD_DEVIATION = "standard deviation";

    /**
     * Key to the property that decides whether or not the simulation should
     * start (or resume).
     */
    public final static String START = "Start";

    /**
     * Key to the property that specifies the analysis that should start
     * running.
     */
    public final static String START_ANALYSIS = "start analysis";

    /**
     * A possible value for the INITIAL_STATE property.
     */
    public final static String STATE_BLANK = "blank";

    /**
     * A possible value for the INITIAL_STATE property.
     */
    public final static String STATE_DATA = "data";

    /**
     * A possible value for the INITIAL_STATE property.
     */
    public final static String STATE_ELLIPSE = "ellipse";

    /**
     * A possible value for the INITIAL_STATE property.
     */
    public final static String STATE_IMAGE = "image";

    /**
     * A possible value for the INITIAL_STATE property.
     */
    public final static String STATE_PROBABILITY = "probability";

    /**
     * A possible value for the INITIAL_STATE property.
     */
    public final static String STATE_RANDOM = "random";

    /**
     * A possible value for the INITIAL_STATE property.
     */
    public final static String STATE_RECTANGLE = "rectangle";

    /**
     * A possible value for the INITIAL_STATE property.
     */
    public final static String STATE_SINGLE_SEED = "single seed";

    /**
     * Key to the property that sets the status bar message.
     */
    public final static String STATUS = "status";

    /**
     * Key to the property that specifies the analysis that should stop running.
     */
    public final static String STOP_ANALYSIS = "stop analysis";

    /**
     * Key to the property that says to store the minimum state history
     * possible.
     */
    public final static String STORE_MINIMUM = "store min state history";

    /**
     * Key to the property that sets the delay in milliseconds between each
     * generation.
     */
    public final static String TIME_DELAY = "time delay";

    /**
     * A possible value for the properties.
     */
    public final static String TRUE = "true";

    /**
     * Key to the property that indicates the graphics should be updated. The
     * graphics will be forced to update, no matter what, when this is used. If
     * you'd like the CA to check if it is ok to refresh the graphics, then use
     * REFRESH_GRAPHICS.
     */
    public static final String UPDATE_GRAPHICS = "update graphics";

    /**
     * Key to the property that determines whether the graphics should be
     * updated at the end or more frequently.
     */
    public final static String UPDATE_AT_END = "update_graphics_at_end";

    /**
     * Key to the property that specifies the folder where the user is storing
     * Analysis classes that they created.
     */
    public final static String USER_ANALYSIS_FOLDER = "user_analysis_folder";

    /**
     * Key to the property that specifies the package name for user created
     * Analysis classes.
     */
    public final static String USER_ANALYSIS_PACKAGE = "user_analysis_folder";

    /**
     * Key to the property that specifies the folder where the user is storing
     * Rule classes that they created.
     */
    public final static String USER_RULE_FOLDER = "user_rule_folder";

    /**
     * Key to the property that specifies the package name for user created Rule
     * classes.
     */
    public final static String USER_RULE_PACKAGE = "user_rule_folder";

    // the sole instance of this class
    private static CurrentProperties currentProperties = null;

    /**
     * Make the constructor private so that this class cannot be instantiated.
     * All properties will be assigned the default properties.
     */
    private CurrentProperties()
    {
    }

    /**
     * Gets the sole instance of the current properties. Uses singleton design
     * pattern.
     * 
     * @return The only instance of this class.
     */
    public static CurrentProperties getInstance()
    {
        if(currentProperties == null)
        {
            // first create some default properties. This is necessary to
            // prevent annoying loops where the creation of the properties
            // requires getting the RuleHash which requires rule names, which
            // requires using properties (e.g., in all IntegerRules (see
            // constructors)).
            currentProperties = new CurrentProperties();

            // after first instantiated with the defaults, now read the
            // properties file and get the real properties
            currentProperties
                .updatePropertiesFromFile(CAConstants.DEFAULT_PROPERTIES_FILE);
        }

        return currentProperties;
    }

    /**
     * Gets default properties for the CA.
     * 
     * @return the default properties.
     */
    private static Properties getDefaultProperties()
    {
        Properties defaultProperties = new Properties();
        defaultProperties.setProperty(BOUNDARY_CONDITION, ""
            + Lattice.WRAP_AROUND_BOUNDARY);
        defaultProperties.setProperty(CA_HEIGHT, "50");
        defaultProperties.setProperty(CA_WIDTH, "50");
        defaultProperties.put(COLOR_SCHEME, RainbowColorScheme.schemeName);
        defaultProperties.setProperty(DATA_DELIMITERS, "\t");
        defaultProperties.setProperty(DISPLAY_STEP, "1");
        defaultProperties.setProperty(DISPLAY_HYPERLINKS_IN_BROWSER, TRUE);
        defaultProperties.setProperty(EMPTY_COLOR,
            RainbowColorScheme.DEFAULT_EMPTY_COLOR.getRGB() + "");
        defaultProperties.setProperty(FILLED_COLOR,
            RainbowColorScheme.DEFAULT_FILLED_COLOR.getRGB() + "");
        defaultProperties.setProperty(IMPORT_SIMULATION, "");
        defaultProperties.setProperty(INITIAL_STATE, STATE_RANDOM);
        defaultProperties.setProperty(INITIAL_STATE_DATA_FILE_PATH, "");
        defaultProperties.setProperty(INITIAL_STATE_ELLIPSE_FILLED, FALSE);
        defaultProperties.setProperty(INITIAL_STATE_ELLIPSE_HEIGHT, "25");
        defaultProperties.setProperty(INITIAL_STATE_ELLIPSE_WIDTH, "25");
        defaultProperties.setProperty(INITIAL_STATE_IMAGE_FILE_PATH, "");
        defaultProperties.setProperty(INITIAL_STATE_RECTANGLE_FILLED, FALSE);
        defaultProperties.setProperty(INITIAL_STATE_RECTANGLE_HEIGHT, "1");
        defaultProperties.setProperty(INITIAL_STATE_RECTANGLE_WIDTH, "1");
        defaultProperties.setProperty(IS_FACADE_ON, "" + FALSE);
        defaultProperties.setProperty(LAMBDA, "" + Lambda.LAMBDA);
        defaultProperties.setProperty(LATTICE, SquareLattice.DISPLAY_NAME);
        defaultProperties.setProperty(MAX_RULE_NUMBER_SIZE_IN_DIGITS, ""
            + IntegerRule.DEFAULT_MAX_RULE_SIZE);
        defaultProperties.setProperty(MAX_TIME, "1000000000");
        defaultProperties.setProperty(NUMBER_OF_STATES, "2");
        defaultProperties.setProperty(NUMBER_OF_PROCESSORS, "1");
        defaultProperties.setProperty(RADIUS, "1");
        defaultProperties.setProperty(RANDOM_PERCENT, "50");
        defaultProperties.setProperty(RANDOM_PERCENT_DIFFUSION, ""
            + DiffusionLimitedAggregation.DEFAULT_RANDOM_PERCENT);
        defaultProperties.setProperty(RANDOM_PERCENT_PER_STATE, "100\t0");
        defaultProperties.setProperty(REWIND_STEPS, "10");
        defaultProperties.setProperty(RULE, "cellularAutomata.rules.Life");
        defaultProperties.setProperty(RULE_NUMBER, ""
            + WolframRuleNumber.DEFAULT_RULE);
        defaultProperties.setProperty(RUNNING_AVERAGE, "1");
        defaultProperties.setProperty(SAVE_DATA, FALSE);
        defaultProperties.setProperty(SAVE_DATA_PATH, "");
        defaultProperties.setProperty(STANDARD_DEVIATION, ""
            + RandomGaussianLattice.DEFAULT_STANDARD_DEVIATION);
        defaultProperties.setProperty(STORE_MINIMUM, FALSE);
        defaultProperties.setProperty(TIME_DELAY, "0");
        defaultProperties.setProperty(UPDATE_AT_END, FALSE);
        defaultProperties.setProperty(USER_ANALYSIS_FOLDER,
            AnalysisHash.DEFAULT_USER_ANALYSIS_FOLDER);
        defaultProperties.setProperty(USER_ANALYSIS_PACKAGE,
            AnalysisHash.DEFAULT_USER_ANALYSIS_PACKAGE);
        defaultProperties.setProperty(USER_RULE_FOLDER,
            RuleHash.DEFAULT_USER_RULE_FOLDER);
        defaultProperties.setProperty(USER_RULE_PACKAGE,
            RuleHash.DEFAULT_USER_RULE_PACKAGE);

        return defaultProperties;
    }

    /**
     * Gets the CA properties and returns them as a Properties object.
     * 
     * @return All of the CA properties.
     */
    public Properties getProperties()
    {
        if(properties == null || properties.size() == 0)
        {
            properties = getDefaultProperties();
        }
        return properties;
    }

    /**
     * The number of processors used for parallel processing in the simulation.
     * 
     * @return the number of processors.
     */
    public int getNumberOfProcessors()
    {
        return numberOfProcessors;
    }

    /**
     * The number of rows in the simulation.
     * 
     * @return the number of rows.
     */
    public int getNumRows()
    {
        return numRows;
    }

    /**
     * The number of columns in the simulation.
     * 
     * @return the number of columns.
     */
    public int getNumColumns()
    {
        return numColumns;
    }

    /**
     * Get the boundary condition, either wrap-around or reflection.
     * 
     * @return the boundary condition, specified as Lattice.WRAP_AROUND_BOUNDARY
     *         or Lattice.WRAP_AROUND_BOUNDARY.
     */
    public int getBoundaryCondition()
    {
        return boundaryCondition;
    }

    /**
     * Delimiters used to separate data when reading and writing from files.
     * 
     * @return the data delimiters (may be one, like a tab, or more than one,
     *         like a tab and a space).
     */
    public String getDataDelimiters()
    {
        return dataDelimiters;
    }

    /**
     * Instead of the current properties, this gets the default CA properties
     * and returns them as a Properties object.
     * 
     * @return The default CA properties.
     */
    public Properties getDefaultCAProperties()
    {
        return getDefaultProperties();
    }

    /**
     * Whether or not to display links in a separate browser or in the
     * application.
     * 
     * @return true if should display in a separate browser.
     */
    public boolean isDisplayHyperLinksInBrowser()
    {
        return displayHyperLinksInBrowser;
    }

    /**
     * Whether or not to use the simplified interface (facade) or full
     * interface.
     * 
     * @return true if should us the simplified interface (facade).
     */
    public boolean isFacadeOn()
    {
        return isFacadeOn;
    }

    /**
     * The color scheme.
     * 
     * @return the name of the color scheme, usually given by a static variable
     *         from the color scheme class, like
     *         "RainbowColorScheme.schemeName".
     */
    public String getColorScheme()
    {
        return colorScheme;
    }

    /**
     * The number of time steps between displays of the CA data.
     * 
     * @return the number of steps between displays.
     */
    public int getDisplayStep()
    {
        return displayStep;
    }

    /**
     * The empty color.
     * 
     * @return the current color scheme's empty color.
     */
    public Color getEmptyColor()
    {
        return new Color(emptyColor);
    }

    /**
     * The filled or occupied color.
     * 
     * @return the current color scheme's filled or occupied color.
     */
    public Color getFilledColor()
    {
        return new Color(filledColor);
    }

    /**
     * The type of initial state.
     * 
     * @return the initial state, given by STATE_RANDOM, STATE_BLANK, etc.
     */
    public String getInitialState()
    {
        return initialState;
    }

    /**
     * the file path to the initial state data.
     * 
     * @return the file path to the initial state data.
     */
    public String getInitialStateDataFilePath()
    {
        return initialStateDataFilePath;
    }

    /**
     * Whether or not an initial state ellipse is filled.
     * 
     * @return true if the initial state ellipse is filled.
     */
    public boolean isInitialStateEllipseFilled()
    {
        return initialStateEllipseFilled;
    }

    /**
     * Gets the height of the initial state ellipse.
     * 
     * @return the height of the initial state ellipse.
     */
    public int getInitialStateEllipseHeight()
    {

        return initialStateEllipseHeight;
    }

    /**
     * Gets the width of the initial state ellipse.
     * 
     * @return the width of the initial state ellipse.
     */
    public int getInitialStateEllipseWidth()
    {
        return initialStateEllipseWidth;
    }

    /**
     * The file path to the image used as an initial state.
     * 
     * @return the file path.
     */
    public String getInitialStateImageFilePath()
    {
        return initialStateImageFilePath;
    }

    /**
     * Whether or not an initial state rectangle is filled.
     * 
     * @return true if the initial state rectangle is filled.
     */
    public boolean isInitialStateRectangleFilled()
    {
        return initialStateRectangleFilled;
    }

    /**
     * Gets the height of the initial state rectangle.
     * 
     * @return the height of the initial state rectangle.
     */
    public int getInitialStateRectangleHeight()
    {
        return initialStateRectangleHeight;
    }

    /**
     * Gets the width of the initial state rectangle.
     * 
     * @return the width of the initial state rectangle.
     */
    public int getInitialStateRectangleWidth()
    {
        return initialStateRectangleWidth;
    }

    /**
     * Gets the value of Langton's lambda.
     * 
     * @return the value of lambda, between 0.0 and 1.0.
     */
    public double getLambda()
    {
        return lambda;
    }

    /**
     * The display name of the current lattice.
     * 
     * @return the lattice display name
     */
    public String getLatticeDisplayName()
    {
        return latticeDisplayName;
    }

    /**
     * get the maximum number of digits that can be used to specify a rule
     * number.
     * 
     * @return the number of digits.
     */
    public int getMaxRuleNumberSizeInDigits()
    {
        return maxRuleNumberSizeInDigits;
    }

    /**
     * The maximum number of generations before the simulation automatically
     * pauses.
     * 
     * @return the number of generations before pauses.
     */
    public int getMaxTime()
    {
        return maxTime;
    }

    /**
     * the number of states used in an integer-based simulation.
     * 
     * @return the number of states.
     */
    public int getNumStates()
    {
        return numStates;
    }

    /**
     * The radius of a neighborhood specified when building certain lattices.
     * 
     * @return the neighborhood radius.
     */
    public int getNeighborhoodRadius()
    {
        return neighborhoodRadius;
    }

    /**
     * The percentage of sites that will be randomly filled.
     * 
     * @return a number between 0 and 100.
     */
    public int getRandomPercent()
    {
        return randomPercent;
    }

    /**
     * The percentage of sites that will be randomly filled with a gas in a
     * diffusion limited aggregation simulation.
     * 
     * @return a number between 0 and 100.
     */
    public int getRandomPercentDiffusionGas()
    {
        return randomPercentDiffusionGas;
    }

    /**
     * Gets a string of numbers (separated by the delimiter) indicating the
     * percentage of each state that should be present when the simulation is
     * initialized. the numbers will sum to 100.
     * 
     * @return the percentage of each state.
     */
    public String getRandomPercentPerState()
    {
        return randomPercentPerState;
    }

    /**
     * The number of steps that the simulation will rewind.
     * 
     * @return the number of steps.
     */
    public int getRewindSteps()
    {
        return rewindSteps;
    }

    /**
     * Gets the class name of the rule (like "cellularAutomata.rules.Life").
     * 
     * @return the class name of the rule, including the package structure.
     */
    public String getRuleClassName()
    {
        return ruleClassName;
    }

    /**
     * the current rule number (when applicable).
     * 
     * @return the rule number.
     */
    public BigInteger getRuleNumber()
    {
        return ruleNumber;
    }

    /**
     * The number of time steps that will be averaged for display.
     * 
     * @return the running average.
     */
    public int getRunningAverage()
    {
        return runningAverage;
    }

    /**
     * The file path to which data will be saved.
     * 
     * @return the file path.
     */
    public String getSaveDataFilePath()
    {
        return saveDataFilePath;
    }

    /**
     * Get the file path used to save the simulation as an image.
     * 
     * @return the file path.
     */
    public String getSaveImageFilePath()
    {
        return saveImageFilePath;
    }

    /**
     * The standard deviation used when building Guassian lattices.
     * 
     * @return the standard deviation.
     */
    public double getStandardDeviation()
    {
        return standardDeviation;
    }

    /**
     * The delay in milliseconds between each iteration of the simulation.
     * 
     * @return the time delay in milliseconds.
     */
    public long getTimeDelay()
    {
        return timeDelay;
    }

    /**
     * Whether or not to only update the graphics at the end.
     * 
     * @return true if should only update the graphics at the end of the
     *         simulation.
     */
    public boolean isUpdateAtEnd()
    {
        return updateAtEnd;
    }

    /**
     * The name of the folder where user analyses are saved.
     * 
     * @return the user analysis folder name.
     */
    public String getUserAnalysisFolder()
    {
        return userAnalysisFolder;
    }

    /**
     * The package where user analyses are saved.
     * 
     * @return the user analysis package name.
     */
    public String getUserAnalysisPackage()
    {
        return userAnalysisPackage;
    }

    /**
     * The name of the folder where user rules are saved.
     * 
     * @return the user rules folder name.
     */
    public String getUserRuleFolder()
    {
        return userRuleFolder;
    }

    /**
     * The package where user rules are saved.
     * 
     * @return the user rules package name.
     */
    public String getUserRulePackage()
    {
        return userRulePackage;
    }

    /**
     * Reset all properties to their default values.
     */
    public void resetToDefaultProperties()
    {
        properties = getDefaultProperties();

        transferFilePropertiesToInstanceVariables(properties);
    }

    /**
     * Set the boundary condition, either wrap-around or reflection.
     * 
     * @param boundaryCondition
     *            The boundary condition, specified as
     *            Lattice.WRAP_AROUND_BOUNDARY or Lattice.WRAP_AROUND_BOUNDARY.
     */
    public void setBoundaryCondition(int boundaryCondition)
    {
        if(boundaryCondition == Lattice.WRAP_AROUND_BOUNDARY
            || boundaryCondition == Lattice.REFLECTION_BOUNDARY)
        {
            this.boundaryCondition = boundaryCondition;
            properties.setProperty(CurrentProperties.BOUNDARY_CONDITION, ""
                + boundaryCondition);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the number of rows used by the simulation.
     * 
     * @param numRows
     *            The number of rows.
     */
    public void setNumRows(int numRows)
    {
        if(AllPanelController.isRowsOk(numRows))
        {
            this.numRows = numRows;
            properties.setProperty(CurrentProperties.CA_HEIGHT, "" + numRows);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the number of processors that will be used when parallel processing.
     * 
     * @param numberOfProcessors
     *            The number of processors that will be used when parallel
     *            processing.
     */
    public void setNumberOfParallelProcessors(int numberOfProcessors)
    {
        if(numberOfProcessors > 0
            && numberOfProcessors <= Runtime.getRuntime().availableProcessors())
        {
            this.numberOfProcessors = numberOfProcessors;
            properties.setProperty(CurrentProperties.NUMBER_OF_PROCESSORS, ""
                + numberOfProcessors);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the number of columns used by the simulation.
     * 
     * @param numColumns
     *            The number of columns.
     */
    public void setNumColumns(int numColumns)
    {
        if(AllPanelController.isColumnsOk(numRows))
        {
            this.numColumns = numColumns;
            properties.setProperty(CurrentProperties.CA_WIDTH, "" + numColumns);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the data delimiters. May be more than one. For example, a tab, or a
     * tab and a space.
     * 
     * @param dataDelimiters
     *            The data delimiters.
     */
    public void setDataDelimiters(String dataDelimiters)
    {
        if(dataDelimiters != null && dataDelimiters.length() != 0)
        {
            this.dataDelimiters = dataDelimiters;
            properties.setProperty(CurrentProperties.DATA_DELIMITERS, ""
                + dataDelimiters);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set whether or not to display links in a separate browser or in the
     * application.
     * 
     * @param displayHyperLinksInBrowser
     *            A value of true will display links in a separate browser.
     */
    public void setDisplayHyperLinksInBrowser(boolean displayHyperLinksInBrowser)
    {
        this.displayHyperLinksInBrowser = displayHyperLinksInBrowser;
        properties.setProperty(CurrentProperties.DISPLAY_HYPERLINKS_IN_BROWSER,
            "" + displayHyperLinksInBrowser);
    }

    /**
     * The number of generation between displays of the simulation. For example,
     * a 2 will display every other time step.
     * 
     * @param displayStep
     *            The number of generations between displays of the simulation.
     */
    public void setDisplayStep(int displayStep)
    {
        if(displayStep > 0)
        {
            this.displayStep = displayStep;
            properties.setProperty(CurrentProperties.DISPLAY_STEP, ""
                + displayStep);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the color scheme for the display.
     * 
     * @param colorScheme
     *            The name of the color scheme, usually given by a static
     *            variable from the scheme's class, such as
     *            "RainbowColorScheme.schemeName".
     */
    public void setColorScheme(String colorScheme)
    {
        this.colorScheme = colorScheme;
        properties.setProperty(CurrentProperties.COLOR_SCHEME, colorScheme);
    }

    /**
     * Set the empty color for the display.
     * 
     * @param emptyColor
     *            The empty color displayed for "empty" cells.
     */
    public void setEmptyColor(Color emptyColor)
    {
        if(emptyColor != null)
        {
            this.emptyColor = emptyColor.getRGB();
            properties.setProperty(CurrentProperties.EMPTY_COLOR, ""
                + this.emptyColor);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set facade on or off.
     * 
     * @param isFacadeOn
     *            true to turn on the facade, and false to turn off the facade.
     */
    public void setFacade(boolean isFacadeOn)
    {
        this.isFacadeOn = isFacadeOn;
        properties.setProperty(CurrentProperties.IS_FACADE_ON, "" + isFacadeOn);
    }

    /**
     * Set the filled color for the display.
     * 
     * @param filledColor
     *            The filled color displayed for "filled" cells.
     */
    public void setFilledColor(Color filledColor)
    {
        if(filledColor != null)
        {
            this.filledColor = filledColor.getRGB();
            properties.setProperty(CurrentProperties.FILLED_COLOR, ""
                + this.filledColor);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set a type of initial state.
     * 
     * @param initialState
     *            The initial state, given by STATE_RANDOM, STATE_BLANK, etc.
     */
    public void setInitialState(String initialState)
    {
        // when the CA is initializing, if the initialState is unknown or an
        // incorrect string, then it will default to a blank initial state.
        if(initialState != null)
        {
            this.initialState = initialState;
            properties.setProperty(CurrentProperties.INITIAL_STATE, ""
                + initialState);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * The file path where initial state data is stored.
     * 
     * @param initialStateDataFilePath
     *            The file path.
     */
    public void setInitialStateDataFilePath(String initialStateDataFilePath)
    {
        this.initialStateDataFilePath = initialStateDataFilePath;
        properties.setProperty(CurrentProperties.INITIAL_STATE_DATA_FILE_PATH,
            initialStateDataFilePath);
    }

    /**
     * Set whether or not the initial state ellipse is filled.
     * 
     * @param initialStateEllipseFilled
     *            If true, then fills the ellipse.
     */
    public void setInitialStateEllipseFilled(boolean initialStateEllipseFilled)
    {
        this.initialStateEllipseFilled = initialStateEllipseFilled;
        properties.setProperty(CurrentProperties.INITIAL_STATE_ELLIPSE_FILLED,
            "" + initialStateEllipseFilled);
    }

    /**
     * Set the height of the initial state ellipse.
     * 
     * @param initialStateEllipseHeight
     *            The height of the initial state ellipse.
     */
    public void setInitialStateEllipseHeight(int initialStateEllipseHeight)
    {
        if(initialStateEllipseHeight >= 0)
        {
            this.initialStateEllipseHeight = initialStateEllipseHeight;
            properties.setProperty(
                CurrentProperties.INITIAL_STATE_ELLIPSE_HEIGHT, ""
                    + initialStateEllipseHeight);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the width of the initial state ellipse.
     * 
     * @param initialStateEllipseWidth
     *            The width of the initial state ellipse.
     */
    public void setInitialStateEllipseWidth(int initialStateEllipseWidth)
    {
        if(initialStateEllipseWidth >= 0)
        {
            this.initialStateEllipseWidth = initialStateEllipseWidth;
            properties.setProperty(
                CurrentProperties.INITIAL_STATE_ELLIPSE_WIDTH, ""
                    + initialStateEllipseWidth);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the file path to the image used as an initial state.
     * 
     * @param initialStateImageFilePath
     *            The file path.
     */
    public void setInitialStateImageFilePath(String initialStateImageFilePath)
    {
        this.initialStateImageFilePath = initialStateImageFilePath;
        properties.setProperty(CurrentProperties.INITIAL_STATE_IMAGE_FILE_PATH,
            "" + initialStateImageFilePath);
    }

    /**
     * Set whether or not the initial state rectangle should be filled.
     * 
     * @param initialStateRectangleFilled
     *            true if the rectangle should be filled.
     */
    public void setInitialStateRectangleFilled(
        boolean initialStateRectangleFilled)
    {
        this.initialStateRectangleFilled = initialStateRectangleFilled;
        properties.setProperty(
            CurrentProperties.INITIAL_STATE_RECTANGLE_FILLED, ""
                + initialStateRectangleFilled);
    }

    /**
     * Set the height of the initial state rectangle.
     * 
     * @param initialStateRectangleHeight
     *            The height of the initial state rectangle.
     */
    public void setInitialStateRectangleHeight(int initialStateRectangleHeight)
    {
        if(initialStateRectangleHeight > 0)
        {
            this.initialStateRectangleHeight = initialStateRectangleHeight;
            properties.setProperty(
                CurrentProperties.INITIAL_STATE_RECTANGLE_HEIGHT, ""
                    + initialStateRectangleHeight);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the width of the initial state rectangle.
     * 
     * @param initialStateRectangleWidth
     *            The width of the initial state rectangle.
     */
    public void setInitialStateRectangleWidth(int initialStateRectangleWidth)
    {
        if(initialStateRectangleWidth > 0)
        {
            this.initialStateRectangleWidth = initialStateRectangleWidth;
            properties.setProperty(
                CurrentProperties.INITIAL_STATE_RECTANGLE_WIDTH, ""
                    + initialStateRectangleWidth);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set langton's lambda.
     * 
     * @param lambda
     *            A value between 0.0 and 1.0 inclusive.
     */
    public void setLambda(double lambda)
    {
        if(lambda >= 0.0 && lambda <= 1.0)
        {
            this.lambda = lambda;
            properties.setProperty(CurrentProperties.LAMBDA, "" + lambda);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the display name of the lattice.
     * 
     * @param latticeDisplayName
     *            The display name of the lattice.
     */
    public void setLatticeDisplayName(String latticeDisplayName)
    {
        // Create a list of possible lattices (dynamically using
        // reflection). These are stored statically in the class, ready for
        // later use.
        LatticeHash latticeHash = new LatticeHash();
        String latticeClassName = latticeHash.get(latticeDisplayName);
        if(latticeClassName != null)
        {
            this.latticeDisplayName = latticeDisplayName;
            properties.setProperty(CurrentProperties.LATTICE, ""
                + latticeDisplayName);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the maximum number of digits allowed to specify a rule number.
     * 
     * @param maxRuleNumberSizeInDigits
     *            the number of digits.
     */
    public void setMaxRuleNumberSizeInDigits(int maxRuleNumberSizeInDigits)
    {
        if(maxRuleNumberSizeInDigits > 10)
        {
            this.maxRuleNumberSizeInDigits = maxRuleNumberSizeInDigits;
            properties.setProperty(
                CurrentProperties.MAX_RULE_NUMBER_SIZE_IN_DIGITS, ""
                    + maxRuleNumberSizeInDigits);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the time before the simulation automatically pauses.
     * 
     * @param maxTime
     *            the number of generations.
     */
    public void setMaxTime(int maxTime)
    {
        if(maxTime > 0)
        {
            this.maxTime = maxTime;
            properties.setProperty(CurrentProperties.MAX_TIME, "" + maxTime);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the number of states used by the simulation (for integer-based
     * rules).
     * 
     * @param numStates
     *            the number of states.
     */
    public void setNumStates(int numStates)
    {
        if(AllPanelController.isNumberOfStatesOk(numStates))
        {
            this.numStates = numStates;
            properties.setProperty(CurrentProperties.NUMBER_OF_STATES, ""
                + numStates);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the radius used when creating some lattices.
     * 
     * @param neighborhoodRadius
     *            The radius of the neighborhood (for example, 1 is nearest
     *            neighbor and 2 is next-nearest neighbor).
     */
    public void setNeighborhoodRadius(int neighborhoodRadius)
    {
        if(AllPanelController.isRadiusOk(neighborhoodRadius))
        {
            this.neighborhoodRadius = neighborhoodRadius;
            properties.setProperty(CurrentProperties.RADIUS, ""
                + neighborhoodRadius);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the percentage of cells that will be randomly filled when "random" is
     * selected as an initial state.
     * 
     * @param randomPercent
     *            the percentage between 0 and 100 inclusive.
     */
    public void setRandomPercent(int randomPercent)
    {
        if(AllPanelController.isRandomPercentNumberOk(randomPercent))
        {
            this.randomPercent = randomPercent;
            properties.setProperty(CurrentProperties.RANDOM_PERCENT, ""
                + randomPercent);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the percentage of cells that will be randomly filled with a gas in a
     * diffusion limited aggregation simulation.
     * 
     * @param randomPercentDiffusionGas
     *            the percentage between 0 and 100 inclusive.
     */
    public void setRandomPercentDiffusionGas(int randomPercentDiffusionGas)
    {
        if(randomPercentDiffusionGas >= 0 && randomPercentDiffusionGas <= 100)
        {
            this.randomPercentDiffusionGas = randomPercentDiffusionGas;
            properties.setProperty(CurrentProperties.RANDOM_PERCENT_DIFFUSION,
                "" + randomPercentDiffusionGas);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Sets a string of numbers (separated by the delimiter) indicating the
     * percentage of each state that should be present when the simulation is
     * initialized. The numbers must sum to 100.
     * 
     * @param randomPercentPerState
     *            A string of delimiter separated numbers that sum to 100, with
     *            one number for each state.
     */
    public void setRandomPercentPerState(String randomPercentPerState)
    {
        // Too annoying to check for correctness. The initial states panel will
        // handle this.
        this.randomPercentPerState = randomPercentPerState;
        properties.setProperty(CurrentProperties.RANDOM_PERCENT_PER_STATE, ""
            + randomPercentPerState);
    }

    /**
     * The number of steps that the simulation can rewind (if not changed
     * elsewhere).
     * 
     * @param rewindSteps
     *            the number of steps that the simulation will rewind.
     */
    public void setRewindSteps(int rewindSteps)
    {
        if(rewindSteps >= 0)
        {
            this.rewindSteps = rewindSteps;
            properties.setProperty(CurrentProperties.REWIND_STEPS, ""
                + rewindSteps);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Sets the class name of the rule (like "cellularAutomata.rules.Life").
     * 
     * @param ruleClassName
     *            The class name of the rule, including the package structure.
     */
    public void setRuleClassName(String ruleClassName)
    {
        // Check the Rule.
        // Create a list of possible rules (dynamically using reflection).
        // These are stored statically in the class, ready for later use.
        RuleHash ruleHash = new RuleHash();
        if(ruleHash.containsValue(ruleClassName))
        {
            this.ruleClassName = ruleClassName;
            properties.setProperty(CurrentProperties.RULE, "" + ruleClassName);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the rule number (which is used only when applicable).
     * 
     * @param ruleNumber
     *            The rule number.
     */
    public void setRuleNumber(BigInteger ruleNumber)
    {
        if(ruleNumber.compareTo(BigInteger.ZERO) >= 0)
        {
            this.ruleNumber = ruleNumber;
            properties.setProperty(CurrentProperties.RULE_NUMBER, ""
                + ruleNumber);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the number of generations averaged together for the display.
     * 
     * @param runningAverage
     *            The number of time steps averaged.
     */
    public void setRunningAverage(int runningAverage)
    {
        if(AllPanelController.isRunningAverageOk(runningAverage))
        {
            this.runningAverage = runningAverage;
            properties.setProperty(CurrentProperties.RUNNING_AVERAGE, ""
                + runningAverage);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the file path to which data will be saved.
     * 
     * @param saveDataFilePath
     *            The file path.
     */
    public void setSaveDataFilePath(String saveDataFilePath)
    {
        this.saveDataFilePath = saveDataFilePath;
        properties.setProperty(CurrentProperties.SAVE_DATA_PATH, ""
            + saveDataFilePath);
    }

    /**
     * Set the file path used to save the simulation as an image.
     * 
     * @param saveImageFilePath
     *            The file path.
     */
    public void setSaveImageFilePath(String saveImageFilePath)
    {
        this.saveImageFilePath = saveImageFilePath;
        // properties.setProperty(CurrentProperties.SAVE_AS_IMAGE_PATH, ""
        // + saveImageFilePath);
    }

    /**
     * Set the standard deviation used when building a Guassian lattice.
     * 
     * @param standardDeviation
     *            The standard deviation.
     */
    public void setStandardDeviation(double standardDeviation)
    {
        if(AllPanelController.isStandardDeviationOk(standardDeviation))
        {
            this.standardDeviation = standardDeviation;
            properties.setProperty(CurrentProperties.STANDARD_DEVIATION, ""
                + standardDeviation);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the delay in milliseconds between each iteration of the simulation.
     * 
     * @param timeDelay
     *            Millisecond delay.
     */
    public void setTimeDelay(long timeDelay)
    {
        if((timeDelay < 0) || (timeDelay > StartPanel.MAX_DELAY))
        {
            throw new IllegalArgumentException();
        }
        else
        {
            this.timeDelay = timeDelay;
            properties
                .setProperty(CurrentProperties.TIME_DELAY, "" + timeDelay);
        }
    }

    /**
     * Set whether or not to only update the display at the end of the
     * simulation.
     * 
     * @param updateAtEnd
     *            true if the display should only be updated at the end of the
     *            simulation.
     */
    public void setUpdateAtEnd(boolean updateAtEnd)
    {
        this.updateAtEnd = updateAtEnd;
        properties.setProperty(CurrentProperties.UPDATE_AT_END, ""
            + updateAtEnd);
    }

    /**
     * Convert all of the properties to instance variables.
     * 
     * @param properties
     *            The properties being transferred to the instance variables.
     * @return true if the properties were successfully transferred.
     */
    private boolean transferFilePropertiesToInstanceVariables(
        Properties properties)
    {
        boolean allPropertiesOk = true;

        if(properties.isEmpty())
        {
            allPropertiesOk = false;
        }

        // a warning printed only if there is a problem
        String warning = "";

        // NOTE: most of these try-catch are catching IllegalArgumentExceptions.
        // However, I catch general Exceptions so that I can also handle null
        // pointer issues (which will happen if a line has been deleted from the
        // properties file).
        if(allPropertiesOk)
        {
            try
            {
                setRuleClassName(properties.getProperty(CurrentProperties.RULE));
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "When last run, the "
                    + CAConstants.PROGRAM_TITLE
                    + " used a rule called \n\""
                    + properties.getProperty(CurrentProperties.RULE)
                    + "\" that is no longer available. \n\n"
                    + "The Rule may have been removed or altered by a developer.  "
                    + "Resetting to \n" + "a default rule.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                this.setLatticeDisplayName(properties
                    .getProperty(CurrentProperties.LATTICE));
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "When last run, the " + CAConstants.PROGRAM_TITLE
                    + " used a lattice called \n\""
                    + properties.getProperty(CurrentProperties.LATTICE)
                    + "\" that is no longer available. \n\n"
                    + "The Lattice may have been removed or altered by a "
                    + "developer.  Resetting to \na default lattice.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                int displayStep = Integer.parseInt(properties
                    .getProperty(CurrentProperties.DISPLAY_STEP));

                setDisplayStep(displayStep);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The " + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid display step. \n"
                    + "The properties file may be corrupted.  Resetting to \n"
                    + "default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                boolean isFacadeOn = Boolean.parseBoolean(properties
                    .getProperty(CurrentProperties.IS_FACADE_ON));

                setFacade(isFacadeOn);
            }
            catch(Exception e)
            {
                try
                {
                    // not really fatal, so for backwards compatibility, assign
                    // the facade as false
                    properties.setProperty(IS_FACADE_ON, "" + FALSE);
                    setFacade(false);
                }
                catch(Exception unexpected)
                {
                    allPropertiesOk = false;
                    warning = "The "
                        + CAConstants.PROGRAM_TITLE
                        + " property file contains an invalid facade/interface "
                        + "information. \n"
                        + "The properties file may be corrupted.  Resetting to \n"
                        + "default properties. A new property file will be \n"
                        + "automatically generated.";
                }
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                int filledColorRGB = Integer.parseInt(properties
                    .getProperty(CurrentProperties.FILLED_COLOR));

                setFilledColor(new Color(filledColorRGB));
            }
            catch(Exception e)
            {
                try
                {
                    // not really fatal, so for backwards compatibility, assign
                    // the default filled color
                    properties.setProperty(FILLED_COLOR,
                        RainbowColorScheme.DEFAULT_FILLED_COLOR.getRGB() + "");
                    setFilledColor(new Color(
                        RainbowColorScheme.DEFAULT_FILLED_COLOR.getRGB()));
                }
                catch(Exception unexpected)
                {
                    allPropertiesOk = false;
                    warning = "The "
                        + CAConstants.PROGRAM_TITLE
                        + " property file contains an invalid \"filled\" color. \n"
                        + "The properties file may be corrupted.  Resetting to \n"
                        + "default properties. A new property file will be \n"
                        + "automatically generated.";
                }
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                String colorScheme = properties
                    .getProperty(CurrentProperties.COLOR_SCHEME);

                setColorScheme(colorScheme);
            }
            catch(Exception e)
            {
                try
                {
                    // not really fatal, so for backwards compatibility, assign
                    // the default color scheme
                    properties.put(COLOR_SCHEME, RainbowColorScheme.schemeName);
                    setColorScheme(RainbowColorScheme.schemeName);
                }
                catch(Exception unexpected)
                {
                    allPropertiesOk = false;
                    warning = "The "
                        + CAConstants.PROGRAM_TITLE
                        + " property file contains an invalid color scheme. \n"
                        + "The properties file may be corrupted.  Resetting to \n"
                        + "default properties. A new property file will be \n"
                        + "automatically generated.";
                }
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                int emptyColorRGB = Integer.parseInt(properties
                    .getProperty(CurrentProperties.EMPTY_COLOR));

                setEmptyColor(new Color(emptyColorRGB));
            }
            catch(Exception e)
            {
                try
                {
                    // not really fatal, so for backwards compatibility, assign
                    // the default empty color
                    properties.setProperty(EMPTY_COLOR,
                        RainbowColorScheme.DEFAULT_EMPTY_COLOR.getRGB() + "");
                    setEmptyColor(new Color(
                        RainbowColorScheme.DEFAULT_EMPTY_COLOR.getRGB()));
                }
                catch(Exception unexpected)
                {
                    allPropertiesOk = false;
                    warning = "The "
                        + CAConstants.PROGRAM_TITLE
                        + " property file contains an invalid \"empty\" color. \n"
                        + "The properties file may be corrupted.  Resetting to \n"
                        + "default properties. A new property file will be \n"
                        + "automatically generated.";
                }
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                int radius = Integer.parseInt(properties
                    .getProperty(CurrentProperties.RADIUS));

                setNeighborhoodRadius(radius);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The "
                    + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid neighborhood radius. \n"
                    + "The properties file may be corrupted.  Resetting to \n"
                    + "default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                double stdev = Double.parseDouble(properties
                    .getProperty(CurrentProperties.STANDARD_DEVIATION));

                setStandardDeviation(stdev);
            }
            catch(Exception e)
            {
                // try
                // {
                // // not really fatal, so for backwards compatibility, assign
                // // the default std deviation
                // properties.setProperty(STANDARD_DEVIATION, ""
                // + RandomGaussianLattice.DEFAULT_STANDARD_DEVIATION);
                // setStandardDeviation(RandomGaussianLattice.DEFAULT_STANDARD_DEVIATION);
                // }
                // catch(Exception unexpected)
                // {
                allPropertiesOk = false;
                warning = "The "
                    + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid neighborhood standard \n"
                    + "deviation. The properties file may be corrupted.  Resetting \n "
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
                // }
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                int rows = Integer.parseInt(properties
                    .getProperty(CurrentProperties.CA_HEIGHT));

                setNumRows(rows);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The "
                    + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid number of \n"
                    + "rows. The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                int numProcessors = Integer.parseInt(properties
                    .getProperty(CurrentProperties.NUMBER_OF_PROCESSORS));

                setNumberOfParallelProcessors(numProcessors);
            }
            catch(Exception e)
            {
                // the fix is obvious, so no need to notify the user
                setNumberOfParallelProcessors(1);
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                int cols = Integer.parseInt(properties
                    .getProperty(CurrentProperties.CA_WIDTH));

                setNumColumns(cols);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The "
                    + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid number of \n"
                    + "columns. The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                int randomPercentNumber = Integer.parseInt(properties
                    .getProperty(CurrentProperties.RANDOM_PERCENT));

                setRandomPercent(randomPercentNumber);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The "
                    + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid random percent \n"
                    + "number. The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                int randomPercentDiffusionNumber = Integer.parseInt(properties
                    .getProperty(CurrentProperties.RANDOM_PERCENT_DIFFUSION));

                setRandomPercentDiffusionGas(randomPercentDiffusionNumber);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The "
                    + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid random percent diffusion gas\n"
                    + "number. The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                int runningAverage = Integer.parseInt(properties
                    .getProperty(CurrentProperties.RUNNING_AVERAGE));

                setRunningAverage(runningAverage);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The " + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid running average. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                int maximumIterations = Integer.parseInt(properties
                    .getProperty(CurrentProperties.MAX_TIME));

                setMaxTime(maximumIterations);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The " + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid pause time. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                long timeDelay = Long.parseLong(properties
                    .getProperty(CurrentProperties.TIME_DELAY));

                setTimeDelay(timeDelay);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The " + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid time delay. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                int numberOfStates = Integer.parseInt(properties
                    .getProperty(CurrentProperties.NUMBER_OF_STATES));

                setNumStates(numberOfStates);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The " + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid number of states. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                int boundaryCondition = Integer.parseInt(properties
                    .getProperty(CurrentProperties.BOUNDARY_CONDITION));

                setBoundaryCondition(boundaryCondition);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The "
                    + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid boundary condition. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                setDataDelimiters(properties
                    .getProperty(CurrentProperties.DATA_DELIMITERS));
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The " + CAConstants.PROGRAM_TITLE
                    + " property file contains invalid data delimiters. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                boolean displayHyperLinksInBrowser = Boolean
                    .parseBoolean(properties
                        .getProperty(CurrentProperties.DISPLAY_HYPERLINKS_IN_BROWSER));

                setDisplayHyperLinksInBrowser(displayHyperLinksInBrowser);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The "
                    + CAConstants.PROGRAM_TITLE
                    + " property file contains invalid hyperlink display information. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                setInitialState(properties
                    .getProperty(CurrentProperties.INITIAL_STATE));
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The " + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid initial state. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                setInitialStateDataFilePath(properties
                    .getProperty(CurrentProperties.INITIAL_STATE_DATA_FILE_PATH));
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The "
                    + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid initial state data file path. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                boolean filled = Boolean
                    .parseBoolean(properties
                        .getProperty(CurrentProperties.INITIAL_STATE_ELLIPSE_FILLED));

                this.setInitialStateEllipseFilled(filled);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The "
                    + CAConstants.PROGRAM_TITLE
                    + " property file contains invalid \"ellipse filled\" information. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                int height = Integer
                    .parseInt(properties
                        .getProperty(CurrentProperties.INITIAL_STATE_ELLIPSE_HEIGHT));

                setInitialStateEllipseHeight(height);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The " + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid ellipse height. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                int width = Integer
                    .parseInt(properties
                        .getProperty(CurrentProperties.INITIAL_STATE_ELLIPSE_WIDTH));

                setInitialStateEllipseWidth(width);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The " + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid ellipse width. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                setInitialStateImageFilePath(properties
                    .getProperty(CurrentProperties.INITIAL_STATE_IMAGE_FILE_PATH));
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The "
                    + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid initial state image file path. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                boolean filled = Boolean
                    .parseBoolean(properties
                        .getProperty(CurrentProperties.INITIAL_STATE_RECTANGLE_FILLED));

                this.setInitialStateRectangleFilled(filled);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The "
                    + CAConstants.PROGRAM_TITLE
                    + " property file contains invalid \"rectangle filled\" information. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                int height = Integer
                    .parseInt(properties
                        .getProperty(CurrentProperties.INITIAL_STATE_RECTANGLE_HEIGHT));

                setInitialStateRectangleHeight(height);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The " + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid rectangle height. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                int width = Integer
                    .parseInt(properties
                        .getProperty(CurrentProperties.INITIAL_STATE_RECTANGLE_WIDTH));

                setInitialStateRectangleWidth(width);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The " + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid rectangle width. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                double lambda = Double.parseDouble(properties
                    .getProperty(CurrentProperties.LAMBDA));

                setLambda(lambda);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The " + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid lambda. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                int digits = Integer
                    .parseInt(properties
                        .getProperty(CurrentProperties.MAX_RULE_NUMBER_SIZE_IN_DIGITS));

                setMaxRuleNumberSizeInDigits(digits);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The "
                    + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid maximum number of digits for rule numbers. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                setRandomPercentPerState(properties
                    .getProperty(CurrentProperties.RANDOM_PERCENT_PER_STATE));
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The "
                    + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid random percent per state. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                int rewindSteps = Integer.parseInt(properties
                    .getProperty(CurrentProperties.REWIND_STEPS));

                setRewindSteps(rewindSteps);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The "
                    + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid number of rewind steps. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                BigInteger ruleNumber = new BigInteger(properties
                    .getProperty(CurrentProperties.RULE_NUMBER));

                setRuleNumber(ruleNumber);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The " + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid rule number. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                setSaveDataFilePath(properties
                    .getProperty(CurrentProperties.SAVE_DATA_PATH));
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The "
                    + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid save data file path. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                boolean updateAtEnd = Boolean.parseBoolean(properties
                    .getProperty(CurrentProperties.UPDATE_AT_END));

                this.setUpdateAtEnd(updateAtEnd);
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The "
                    + CAConstants.PROGRAM_TITLE
                    + " property file contains invalid \"update at end\" information. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        if(allPropertiesOk)
        {
            try
            {
                setSaveImageFilePath(properties
                    .getProperty(CurrentProperties.SAVE_AS_IMAGE_PATH));
            }
            catch(Exception e)
            {
                allPropertiesOk = false;
                warning = "The "
                    + CAConstants.PROGRAM_TITLE
                    + " property file contains an invalid \"save as image\" file path. \n"
                    + "The properties file may be corrupted.  Resetting \n"
                    + "to default properties. A new property file will be \n"
                    + "automatically generated.";
            }
        }

        // print a warning if necessary
        if(!allPropertiesOk && warning != null && !warning.isEmpty())
        {
            JOptionPane.showMessageDialog(null, warning, "Warning",
                JOptionPane.WARNING_MESSAGE);
        }

        return allPropertiesOk;
    }

    /**
     * Get a list of properties from a file.
     * 
     * @param filePath
     *            The properties file path.
     */
    public void updatePropertiesFromFile(String filePath)
    {
        // Indicates whether or not a warning has been displayed. Used to
        // prevent multiple warnings.
        boolean haveAlreadyWarned = false;

        // open an input stream to a properties file
        Properties properties = new Properties();

        try
        {
            // an input stream
            FileInputStream inputStream = new FileInputStream(filePath);

            // get the properties (and provide the default properties)
            properties.load(inputStream);

            // close the stream
            inputStream.close();
        }
        catch(Exception propertyError)
        {
            String warning = "The " + CAConstants.PROGRAM_TITLE
                + " property file is non-existant \n"
                + "or unreadable. Using default properties. A new "
                + "property file \nwill be automatically generated.";

            JOptionPane.showMessageDialog(null, warning, "Warning",
                JOptionPane.WARNING_MESSAGE);

            // prevent multiple warnings
            haveAlreadyWarned = true;

            properties = getDefaultProperties();
        }

        boolean transferOk = transferFilePropertiesToInstanceVariables(properties);

        // check that all the properties are ok
        if(!transferOk)
        {
            // if not, then use the defaults (which we must assume are
            // ok)
            properties = getDefaultProperties();

            transferFilePropertiesToInstanceVariables(properties);

            if(!haveAlreadyWarned)
            {
                // warn the user
                String warning = "The " + CAConstants.PROGRAM_TITLE
                    + " property file has been corrupted. \n"
                    + " Using default properties. A new "
                    + "property file will be automatically \ngenerated.";

                JOptionPane.showMessageDialog(null, warning, "Warning",
                    JOptionPane.WARNING_MESSAGE);
            }
        }

        this.properties = properties;
    }

    /**
     * Update properties from a simulation (.ca) file.
     * 
     * @param filePath
     *            The ".ca" simulation file path.
     */
    public void updatePropertiesFromSimulationFile(String filePath)
    {
        // Indicates whether or not a warning has been displayed. Used to
        // prevent multiple warnings.
        boolean haveAlreadyWarned = false;

        Properties properties = new Properties();

        // we are loading a CA Explorer file, so we will need to remove
        // comments. (".ca" files have comments.)
        try
        {
            FileReader inputStream = new FileReader(filePath);
            BufferedReader reader = new BufferedReader(inputStream);
            String line = reader.readLine();
            while(line != null
                && !line.contains(FileStorage.END_PROPERTIES_STRING))
            {
                // get rid of the comment characters "//"
                line = line.substring(line.indexOf("//") + 2);

                // ignore empty lines and property comments "#" and "!"
                if(line.length() > 0 && !line.startsWith("#")
                    && !line.startsWith("!"))
                {
                    // location of the = sign
                    int equalSignLocation = line.indexOf("=");

                    if(equalSignLocation != -1)
                    {
                        // get the key
                        String key = line.substring(0, equalSignLocation);

                        // get the value
                        String value = line.substring(equalSignLocation + 1);

                        // add the property
                        properties.setProperty(key, value);
                    }
                }

                line = reader.readLine();
            }

            // close the stream
            inputStream.close();
            reader.close();
        }
        catch(Exception propertyError)
        {
            String warning = "The properties are corrupt or unreadable. \n"
                + "Using default properties.";

            JOptionPane.showMessageDialog(null, warning, "Warning",
                JOptionPane.WARNING_MESSAGE);

            // prevent multiple warnings
            haveAlreadyWarned = true;

            // create default properties
            properties = getDefaultProperties();
        }

        boolean transferOk = transferFilePropertiesToInstanceVariables(properties);

        // check that all the properties are ok
        if(!transferOk)
        {
            // if not, then use the defaults (which we must assume are
            // ok)
            properties = getDefaultProperties();

            transferFilePropertiesToInstanceVariables(properties);

            if(!haveAlreadyWarned)
            {
                // warn the user
                String warning = "The properties are corrupt or unreadable. \n"
                    + "Using default properties.";

                JOptionPane.showMessageDialog(null, warning, "Warning",
                    JOptionPane.WARNING_MESSAGE);
            }
        }

        this.properties = properties;
    }
}
