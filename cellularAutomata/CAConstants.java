/*
 CAConstants -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata;

/**
 * Constants used by the CA. (Some are not true constants, but instead are
 * loaded from a property file called ca_constants.txt.) If the constants file
 * does not exist, then default values are used.
 * 
 * @author David Bahr
 */
public class CAConstants
{
	/**
	 * The Cellular Automaton Explorer needs this version of Java or a later
	 * version. This must be an integer number, and represents the first number
	 * in the official java version. For example, the 1 in 1.5.3, or the 2 in
	 * 2.0.1.
	 */
	public static final int COMPATIBLE_JAVA_VERSION = 1;

	/**
	 * The Cellular Automaton Explorer needs this release of Java or a later
	 * release. This must be an integer number, and represents the second number
	 * in the official java version. For example, the 5 in 1.5.3, or the 0 in
	 * 2.0.1, or the 12 in 2.12.7.
	 */
	public static final int COMPATIBLE_JAVA_RELEASE = 6;

	/**
	 * The file extension used when saving a Cellular Automaton Explorer
	 * simulation.
	 */
	public static final String CA_FILE_EXTENSION = "ca";

	/**
	 * The folder that contains help pages.
	 */
	private static final String CA_HELP_FOLDER_URL = "http://academic.regis.edu/dbahr/"
			+ "GeneralPages/CellularAutomata/CA_Explorer/helpVersion5_0/";

	/**
	 * The url that details how the properties object has changed.
	 */
	public static final String CA_PROPERTIES_CHANGED_URL = CA_HELP_FOLDER_URL
			+ "CA_PropertiesChanged.html";

	/**
	 * The url that details how constructors for rules should be built.
	 */
	public static final String CA_RULE_CONSTRUCTORS_URL = CA_HELP_FOLDER_URL
			+ "CA_RuleConstructors.html";

	/**
	 * The file path for the properties file.
	 */
	public static final String DEFAULT_PROPERTIES_FILE = "ca_properties.txt";

	/**
	 * The folder name where the "EZ mode" facade simulations are stored..
	 */
	public static final String FACADE_SIMULATIONS_FOLDER_NAME = "facadeSimulations";

	/**
	 * The pattern used to display rule numbers in scientific notation.
	 */
	public static final String SCIENTIFIC_NOTATION_PATTERN = "0.##########E0";

	/**
	 * Will be true if the lattice is selected and then rules are "greyed out".
	 * Should stay false unless a menu item is added to let the user select
	 * rules after the lattice.
	 */
	public static boolean LATTICE_CENTRIC_CHOICES = false;

	/**
	 * Will be true if this is a linux operating system and false otherwise.
	 */
	public static boolean LINUX_OS = false;

	/**
	 * Will be true if this is any Mac operating system and false otherwise.
	 */
	public static boolean MAC_OS = false;

	/**
	 * Will be true if this is specifically a Mac OS X operating system and
	 * false otherwise (true only for OS X and not other Mac operating systems).
	 */
	public static boolean MAC_OS_X = false;

	/**
	 * Will be true if this is *any* windows operating system and false
	 * otherwise.
	 */
	public static boolean WINDOWS_OS = true;

	/**
	 * Will be true if this is a window XP operating system and false otherwise.
	 */
	public static boolean WINDOWS_XP_OS = false;

	/**
	 * The author info, etc.
	 */
	public static final String ACKNOWLEDGMENTS = "<html><body>"
			+ "<b><i>Acknowledgements:</i></b> Special thanks to "
			+ "Michelle Miller for her <br>"
			+ "patience and understanding.  Thanks also to Alby Graham, <br>"
			+ "Mike Buland, Eric Richardson, \"J\" Wetstein, and others for <br>"
			+ "suggestions, bug fixes, and careful QA.</body></html>";

	/**
	 * Path to the application's icon image (png version).
	 */
	public static final String APPLICATION_ICON_IMAGE_PATH = "images/ca_icon.png";

	/**
	 * Path to the application's alternative icon image (png version). Useful
	 * when the regular icon doesn't reduce well.
	 */
	public static final String APPLICATION_ALTERNATIVE_ICON_IMAGE_PATH = "ca_iconMini.png";

	/**
	 * Path to the application's 16 by 16 pixel icon image (png version).
	 */
	public static final String APPLICATION_16by16_ICON_IMAGE_PATH = "ca_icon16by16.png";

	/**
	 * Path to the application's mini icon image (png version).
	 */
	public static final String APPLICATION_MINI_ICON_IMAGE_PATH = "images/ca_iconMini.png";

	// "images/ca_icon_mini.gif";

	/**
	 * The author info, etc.
	 */
	public static final String COPYRIGHT = "\u00a9 David Bahr, 2009. All "
			+ "rights reserved.";

	/**
	 * If true will turn on debugging features. Otherwise gives nice failure
	 * notices.
	 */
	public final static boolean DEBUG = false;

	/**
	 * The URL for online help pages.
	 */
	public static final String HELP_URL = CA_HELP_FOLDER_URL
			+ "CA_HelpFrame.html";

	/**
	 * The URL for "Getting Started" help.
	 */
	public static final String GETTING_STARTED_URL = CA_HELP_FOLDER_URL
			+ "CA_GettingStarted.html";

	/**
	 * The URL for a "Guided Tour" web page.
	 */
	public static final String GUIDED_TOUR_URL = CA_HELP_FOLDER_URL
			+ "CA_GuidedTour.htm";

	/**
	 * The URL for a tutorial on increasing memory and speed (a web page).
	 */
	public static final String MEMORY_AND_SPEED_TUTORIAL_URL = CA_HELP_FOLDER_URL
			+ "CA_MemoryAndSpeed.html";

	/**
	 * The URL for a "Guided Tour" web page.
	 */
	public static final String OBESITY_TOUR_URL = CA_HELP_FOLDER_URL
			+ "CA_ObesityTour.html";

	/**
	 * The URL for online info on writing your own rule.
	 */
	public static final String HOW_MANY_RULES_ARE_THERE_URL = CA_HELP_FOLDER_URL
			+ "CA_HowManyRulesAreThere.html";

	/**
	 * Increment button image path.
	 */
	public static final String INCREMENT_BUTTON_IMAGE_PATH = "images/Forward1.gif";

	/**
	 * Jar name.
	 */
	public static final String JAR_NAME = "CA_Explorer.jar";

	/**
	 * Title for the program and the graphics frame. A default that will be
	 * reset from data in file.
	 */
	public static final String PROGRAM_TITLE = "Cellular Automaton Explorer";

	/**
	 * The name of the folder that stores html files used to describe each rule.
	 */
	public static final String RULE_DESCRIPTION_FOLDER_NAME = "ruleDescriptions";

	/**
	 * The URL for online info on how to press the All button to show hidden
	 * tabs.
	 */
	public static final String SHOW_HIDDEN_TABS_URL = CA_HELP_FOLDER_URL
			+ "CA_ShowHiddenTabs.html";

	/**
	 * The splash image.
	 */
	public static final String SPLASH_IMAGE_NAME = "splashImage/ca_splash.jpg";

	/**
	 * Start button image path.
	 */
	public static final String START_BUTTON_IMAGE_PATH = "images/start.gif";

	/**
	 * Start button image path.
	 */
	public static final String STOP_BUTTON_IMAGE_PATH = "images/stop.gif";

	/**
	 * Release version.
	 */
	public static final String VERSION = "5.0";

	/**
	 * The URL for online info on writing your own rule.
	 */
	public static final String WRITE_YOUR_OWN_RULE_URL = CA_HELP_FOLDER_URL
			+ "CA_WriteYourOwnRuleIndex.html";

	/**
	 * Set some of the "constants" that depend on the operating system.
	 */
	public static void setConstants()
	{
		// decide if this is a Mac OS or not (*any* Mac OS)
		String osName = System.getProperty("os.name").toLowerCase();
		MAC_OS = osName.contains("mac");

		// decide if this is Mac OS X or not (just OS X, and not any other Mac
		// OS)
		MAC_OS_X = osName.startsWith("mac os x");

		// decide if this is *any* windows OS or not
		WINDOWS_OS = osName.contains("windows");

		// decide if this is windows XP or not (NOTE: my 64 bit XP pro reports
		// as "windows 2003"!) At the moment, I'm only using this XP variable in
		// places where windows 2003 would apply as well, so that's ok (for
		// now).
		WINDOWS_XP_OS = osName.contains("xp")
				|| osName.contains("windows 2003");

		// decide if this is a linux OS or not
		LINUX_OS = osName.contains("linux");
	}
}
