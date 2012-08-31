/*
 CARuleDescriptionLoader -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.util.files;

import java.io.FileReader;
import java.io.BufferedReader;
import java.net.URL;

import cellularAutomata.CAConstants;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.reflection.RuleHash;
import cellularAutomata.reflection.URLResource;
import cellularAutomata.rules.Rule;

/**
 * Loads html files stored in the CA Explorer's "ruleDescriptions" folder.
 * 
 * @author David Bahr
 */
public class CARuleDescriptionLoader
{
	/**
	 * Using the file name, this adds the correct folder.
	 * 
	 * @return the path with the folder attached.
	 */
	private static String getPathWithFolder(String name)
	{
		if(name != null)
		{
			// start with the more likely option (doesn't begin with a "/")
			if(!name.startsWith("/"))
			{
				// add the "ruleDescriptions" folder name
				name = CAConstants.RULE_DESCRIPTION_FOLDER_NAME + "/" + name;
			}
			else
			{
				// add the "ruleDescriptions" folder name
				name = CAConstants.RULE_DESCRIPTION_FOLDER_NAME + name;
			}
		}

		return name;
	}

	/**
	 * Gets the url to the specified file which lives in the "ruleDescriptions"
	 * folder.
	 * 
	 * @param name
	 *            The name of the html file (if the html file is in a subfolder
	 *            of the "ruleDescriptions" folder, then include the subfolder
	 *            (for example, "subfolder/filename.html").
	 * @return The URL, or null if the path does not work.
	 */
	public static URL getURL(String name)
	{
		URL url = null;
		try
		{
			if(name != null)
			{
				// start with the more likely option (doesn't begin with a "/")
				if(!name.startsWith("/"))
				{
					// add the "ruleDescriptions" folder name
					name = "/" + CAConstants.RULE_DESCRIPTION_FOLDER_NAME + "/"
							+ name;
				}
				else
				{
					// add the "ruleDescriptions" folder name
					name = "/" + CAConstants.RULE_DESCRIPTION_FOLDER_NAME
							+ name;
				}
			}

			// get the URL (searches the classpath to find the
			// file).
			url = URLResource.getResource(name);
		}
		catch(Exception e)
		{
			// will return null
			url = null;
		}

		return url;
	}

	/**
	 * Gets the URL of the html description using the supplied class name for
	 * the rule.
	 * 
	 * @param ruleClassName
	 *            The name of the class of the rule.
	 * @return The URL to the html description of the rule, or null if the path
	 *         does not work.
	 */
	public static URL getURLFromRuleClassName(String ruleClassName)
	{
		// the URL that will be returned
		URL fileURL = null;

		if(ruleClassName != null && !ruleClassName.equals(""))
		{
			Rule rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(ruleClassName);

			if(rule != null)
			{
				// get the path to the html file describing the rule
				String filePath = rule.getHTMLFilePath();

				fileURL = getURL(filePath);
			}
		}

		return fileURL;
	}

	/**
	 * Gets the URL of the html description using the supplied descriptive rule
	 * name (for example, "Life", or "Rule 102", etc).
	 * 
	 * @param ruleDisplayName
	 *            The descriptive name of the rule which is used for display
	 *            purposes (like "Life" or "Rule 102").
	 * @return The URL to the html description of the rule, or null if the path
	 *         does not work.
	 */
	public static URL getURLFromRuleName(String ruleDisplayName)
	{
		// the URL that will be returned
		URL fileURL = null;

		if(ruleDisplayName != null && !ruleDisplayName.equals(""))
		{
			RuleHash ruleHash = new RuleHash();
			String ruleClassName = ruleHash.get(ruleDisplayName);

			fileURL = getURLFromRuleClassName(ruleClassName);
		}

		return fileURL;
	}

	/**
	 * Gets the specified html file associated with the specified rule.
	 * 
	 * @param ruleDisplayName
	 *            The descriptive name of the rule which is used for display
	 *            purposes (like "Life" or "Rule 102").
	 * @return The html description associated with the given rule. Null if not
	 *         available.
	 */
	public static String getHTMLFromRuleDisplayName(String ruleDisplayName)
	{
		// NOTE: THIS METHOD IS A WORKAROUND BECAUSE I CAN'T GET JEDITORPANE TO
		// WORK WITH A URL AS CONSTRUCTED ABOVE.

		String html = "";

		if(ruleDisplayName != null && !ruleDisplayName.equals(""))
		{
			RuleHash ruleHash = new RuleHash();
			String ruleClassName = ruleHash.get(ruleDisplayName);

			Rule rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(ruleClassName);

			if(rule != null)
			{
				// get the path to the html file describing the rule
				String filePath = rule.getHTMLFilePath();

				// add on the folder
				filePath = getPathWithFolder(filePath);

				try
				{
					// add each line one at a time
					FileReader inputStream = new FileReader(filePath);
					BufferedReader fileReader = new BufferedReader(inputStream);
					String lineOfFile = fileReader.readLine();
					while(lineOfFile != null)
					{
						html += lineOfFile;
						lineOfFile = fileReader.readLine();
					}
					fileReader.close();
				}
				catch(Exception bummer)
				{
				}
			}
		}

		// just in case
		if(html != null && html.length() == 0)
		{
			html = null;
		}

		return html;
	}

	/**
	 * Gets the specified html file associated with the specified rule, and it
	 * adds break line tags after every paragraph tag. This allows JLabels (like
	 * tool tips) and JButtons to display html properly.
	 * 
	 * @param ruleDisplayName
	 *            The descriptive name of the rule which is used for display
	 *            purposes (like "Life" or "Rule 102").
	 * @return The html description associated with the given rule, with extra
	 *         line breaks for proper display on JLabels (like tool tips). Null
	 *         if not available.
	 */
	public static String getHTMLWithExtraLineBreaksFromRuleDisplayName(
			String ruleDisplayName)
	{
		String html = getHTMLFromRuleDisplayName(ruleDisplayName);

		// add the extra line breaks
		if(html != null)
		{
			html = html.replace("<p", "<br><p");
		}

		return html;
	}
}