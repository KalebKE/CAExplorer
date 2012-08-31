/*
 AnalysisPanel -- a class within the Cellular Automaton Explorer. 
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
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import cellularAutomata.analysis.Analysis;
import cellularAutomata.reflection.AnalysisHash;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;

/**
 * The panel that contains the analysis selections.
 * 
 * @author David Bahr
 */
public class AnalysisPanel extends JPanel
{
	/**
	 * Display title for this panel.
	 */
	public static final String ANALYSIS_PANEL_TITLE = "Analyses";

	/**
	 * A tool tip for the analysis panel.
	 */
	public static final String TOOL_TIP = "<html><body>choose "
			+ "analyses</body></html>";

	/**
	 * The string used to describe the analysis choices.
	 */
	private static final String ANALYSIS_TITLE = "Select analyses";

	/**
	 * The string used to describe the analysis choices.
	 */
	private static final String WARNING_LABEL_TEXT = "Warning: Some analyses may "
			+ "significantly slow the simulation.";

	// The encompassing panel onto which this one will be added.
	private AllPanel outerPanel = null;

	// A listener for components on this panel.
	private AllPanelListener listener = null;

	// color for the titles of sections
	private Color titleColor = Color.BLUE;

	// title font (for titles of sections)
	private Font titleFont = null;

	// fonts for display
	private Fonts fonts = null;

	// the analysis check boxes.
	private JCheckBox[] checkBox = null;

	// the warning.
	private JLabel warningLabel = null;

	// The analysis descriptions
	private String[] analysisDescription = null;

	// The tooltips for the clear button
	private Hashtable<String, String> analysisTipList = null;

	/**
	 * The panel containing the start and stop buttons.
	 * 
	 * @param outerPanel
	 *            The encompassing panel onto which this one will be added.
	 */
	public AnalysisPanel(AllPanel outerPanel)
	{
		super();

		this.outerPanel = outerPanel;
		this.listener = outerPanel.getAllPanelListener();

		this.setOpaque(true);

		// fonts for the components (labels, etc.)
		fonts = new Fonts();
		titleFont = new Fonts().getItalicSmallerFont();

		// add the components
		addComponents();
	}

	/**
	 * Adds the start, stop, and clear buttons along with other components.
	 */
	private void addComponents()
	{
		// panel for the analysis check buttons
		JPanel analysisChoicePane = createAnalysisCheckBoxes();
		analysisChoicePane.setOpaque(true);

		// enable and disable analyses that are compatible/incompatible with the
		// selected lattice and rule
		enableCompatibleAnalyses();

		// create a warning label
		warningLabel = new JLabel(WARNING_LABEL_TEXT);
		warningLabel.setForeground(Color.RED);
		warningLabel.setFont(fonts.getItalicSmallerFont());

		// scrollPane for seeing all of the possible analysis choices
		JScrollPane analysisScrollPanel = new JScrollPane(analysisChoicePane);
		analysisScrollPanel.setBorder(BorderFactory.createEmptyBorder());
		analysisScrollPanel
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		analysisScrollPanel.setOpaque(true);

		// create a layout
		this.setLayout(new GridBagLayout());
		this.add(analysisScrollPanel, new GBC(0, 1).setSpan(1, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		this.add(warningLabel, new GBC(0, 2).setSpan(1, 1).setFill(
				GBC.HORIZONTAL).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));
	}

	/**
	 * Create check boxes for selecting analyses.
	 * 
	 * @return contains the check boxes for selecting analyses.
	 */
	private JPanel createAnalysisCheckBoxes()
	{
		// Create the arrays of analysis descriptions and tips.
		// Must come before create the layout for the analysisPanel.
		createAnalysesAndTips();

		// panel for the analysis check boxes
		JPanel analysisPanel = new JPanel(new GridBagLayout());
		analysisPanel.setOpaque(true);

		// create border
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), ANALYSIS_TITLE, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		analysisPanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder, titledBorder));

		// create check boxes
		checkBox = new JCheckBox[analysisDescription.length];
		for(int i = 0; i < checkBox.length; i++)
		{
			// ImageIcon icon = CAImageIconLoader
			// .loadImage(CAConstants.APPLICATION_ALTERNATIVE_ICON_IMAGE_PATH);

			checkBox[i] = new JCheckBox(analysisDescription[i]);
			checkBox[i].setToolTipText((String) analysisTipList
					.get(analysisDescription[i]));
			checkBox[i].setFont(fonts.getPlainFont());
			checkBox[i].setActionCommand(analysisDescription[i]);
			checkBox[i].addActionListener(listener);

			// add check box to the layout
			analysisPanel.add(checkBox[i], new GBC(0, i).setSpan(2, 1).setFill(
					GBC.BOTH).setWeight(1.0, 0.0).setAnchor(GBC.NORTHWEST)
					.setInsets(1));
		}
		analysisPanel.add(new JLabel(" "), new GBC(0, checkBox.length).setSpan(
				2, 1).setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(
				GBC.NORTHWEST).setInsets(1));

		// create a raised panel
		JPanel raisedPanel = new JPanel(new GridBagLayout());
		raisedPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		raisedPanel.add(analysisPanel, new GBC(0, 0).setSpan(2, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		raisedPanel.setOpaque(true);

		return raisedPanel;
	}

	/**
	 * Creates the list of analyses and their tool tips.
	 */
	private void createAnalysesAndTips()
	{
		// a list of the analyses that will be displayed
		ArrayList<String> analysisList = new ArrayList<String>();

		// a list of the analyses tool tips that will be displayed
		analysisTipList = new Hashtable<String, String>();

		// all the possible analyses
		AnalysisHash analysesHash = new AnalysisHash();
		Iterator values = analysesHash.valuesIterator();

		// list the analyses
		while(values.hasNext())
		{
			String nextAnalysis = (String) values.next();

			// instantiate the analysis using reflection
			Analysis analysis = ReflectionTool
					.instantiateMinimalAnalysisFromClassName(nextAnalysis);

			// make sure it really was instantiated!
			if(analysis != null)
			{
				String name = analysis.getDisplayName();
				analysisList.add(name);
				analysisTipList.put(name, analysis.getToolTipDescription());
			}
			else
			{
				// make the JFrame look disabled
				outerPanel.getCAFrame().setViewDisabled(true);

				// warn the developer
				String warning = "A developer has added an analysis called \n "
						+ "\"" + nextAnalysis + "\" \n"
						+ "that has failed to properly instantiate. \n"
						+ "Using this analysis may cause errors.";

				JOptionPane.showMessageDialog(outerPanel.getCAFrame()
						.getFrame(), warning, "Developer Warning",
						JOptionPane.WARNING_MESSAGE);

				// make the JFrame look enabled
				outerPanel.getCAFrame().setViewDisabled(false);
			}
		}

		// convert to array
		this.analysisDescription = new String[analysisList.size()];
		for(int i = 0; i < analysisList.size(); i++)
		{
			analysisDescription[i] = (String) analysisList.get(i);
		}

		// sort the array (looks better)
		Arrays.sort(analysisDescription);
	}

	/**
	 * Enables analyses that are compatible with the currently selected lattice
	 * and rule. Also closes analyses that are open but not compatible.
	 */
	public void enableCompatibleAnalyses()
	{
		// don't do anything if not yet instantiated
		if(checkBox != null)
		{
			// loop over all of the analyses
			for(int i = 0; i < checkBox.length; i++)
			{
				String analysisDescription = (String) checkBox[i]
						.getActionCommand();

				AnalysisHash analysesHash = new AnalysisHash();
				String analysisClassName = analysesHash
						.get(analysisDescription);

				Analysis analysis = ReflectionTool
						.instantiateMinimalAnalysisFromClassName(analysisClassName);

				// make sure it really was instantiated!
				if(analysis != null)
				{
					if(Analysis.isCompatibleAnalysis(analysis))
					{
						// it is compatible, so enable that checkBox
						checkBox[i].setEnabled(true);
					}
					else
					{
						// it is not compatible, so close the analysis if it is
						// open
						if(checkBox[i].isSelected())
						{
							// this makes sure that action events get fired
							checkBox[i].doClick();
						}

						// it is not compatible, so disable that checkBox
						checkBox[i].setEnabled(false);
					}
				}
			}
		}
	}

	/**
	 * Gets the array of analysis descriptions.
	 * 
	 * @return The analysis descriptions.
	 */
	public String[] getAnalysisDescriptions()
	{
		return analysisDescription;
	}

	/**
	 * Gets the array of checkBoxes.
	 * 
	 * @return The check boxes.
	 */
	public JCheckBox[] getCheckBoxes()
	{
		return checkBox;
	}

	/**
	 * Unchecks the specified analysis without firing an action event. Changes
	 * nothing if the analysis is already unchecked.
	 * 
	 * @param displayName
	 *            The display name of the analysis that will be unchecked.
	 */
	public void uncheck(String displayName)
	{
		// don't do anything if not yet instantiated
		if(checkBox != null)
		{
			// loop over all of the analyses
			for(int i = 0; i < checkBox.length; i++)
			{
				String analysisDescription = (String) checkBox[i]
						.getActionCommand();

				if(analysisDescription.equals(displayName))
				{
					// uncheck without firing an action event
					checkBox[i].setSelected(false);
				}
			}
		}
	}
}
