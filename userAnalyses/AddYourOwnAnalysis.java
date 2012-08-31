/*
 AddYourOwnAnalysis -- a class within the Cellular Automaton Explorer. 
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

package userAnalyses;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JPanel;

import cellularAutomata.CAConstants;
import cellularAutomata.analysis.Analysis;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.MultilineLabel;

/**
 * Lets the user know that they can add their own analysis.
 * 
 * @author David Bahr
 */
public class AddYourOwnAnalysis extends Analysis
{
	// a brief display name for this class
	private static final String ANALYSIS_NAME = "Add Your Own";

	// a brief tooltip description for this class
	private static final String TOOLTIP = "<html>how to make your own analysis</html>";

	// label for the current generation
	private MultilineLabel addYourOwnLabel = null;

	// the panel where results are displayed
	private JPanel displayPanel = null;

	// instructions for making your own
	private String makeYourOwnText = "You can make your own analysis, "
			+ "just like you can make your own rules.\n\n" + "Look in the "
			+ CAConstants.PROGRAM_TITLE
			+ " installation directory, and you will find a"
			+ " \"userAnalysis\" folder.  Copy the \"PercentOccupiedAnalysis\" class "
			+ "and use its Java code as a template.  "
			+ "Alter the Java as necessary and then compile.  Place your "
			+ "compiled code into the \"userAnalysis\" folder.  And voila!  The "
			+ CAConstants.PROGRAM_TITLE
			+ " will take care of the rest.\n\n"
			+ "Note that the \"analyze\" method is called once per generation.  "
			+ "This method is where you do the real work.  The \"lattice\" "
			+ "(passed into this method) holds a list of all of the cells.  "
			+ "You can get an iterator over the cells and use the values "
			+ "stored in them.  See the \"PercentOccupiedAnalysis\" class as an example.  "
			+ "You can also use the \"getNeighbors\" method to find the "
			+ "neighbors of any particular cell. And if you cast the lattice into a "
			+ "\"OneDimensionalLattice\" or a \"TwoDimensionalLattice\", then you "
			+ "can use methods to get the height and width (or length) of the "
			+ "lattice. \n\n"
			+ "You can put anything you like in the JPanel returned by "
			+ "\"getDisplayPanel\".  If you put nothing, then a default "
			+ "will be created for you.  No matter what you create, your JPanel will "
			+ "be placed automatically into a scroll pane. The \"Close\" button "
			+ "and \"Undock\" buttons will also be added automatically.\n\n"
			+ "If your analysis is \"chopped off \" at the bottom, then set a "
			+ "preferred size.  For example, this class uses "
			+ "\"displayPanel.setPreferredSize( new Dimension( CAFrame.tabbedPaneDimension.width, "
			+ "850));\" where the displayPanel is the JPanel that gets returned. 850 is chosen "
			+ "to be just a little taller than necessary. \n\n"
			+ "Be sure to give your analysis a unique display name.  If you use "
			+ "the same name as another analysis, only one of the analyses will "
			+ "be displayed.";

	/**
	 * Brief advertisement for developers that might want to create their own
	 * analyses.
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
	public AddYourOwnAnalysis(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		if(!minimalOrLazyInitialization)
		{
			createDisplayPanel();
		}
	}

	/**
	 * Create the panel used to display the instructions for adding your own
	 * analysis.
	 */
	private void createDisplayPanel()
	{
		if(displayPanel == null)
		{
			displayPanel = new JPanel(new BorderLayout());
			displayPanel.setPreferredSize(new Dimension(
					CAFrame.tabbedPaneDimension.width, 850));

			// a "grab their attention" panel
			AttentionPanel attentionPanel = new AttentionPanel("Make Your Own");

			// create the label
			addYourOwnLabel = new MultilineLabel(makeYourOwnText);
			addYourOwnLabel.setFont(new Fonts().getAnalysesDescriptionFont());
			addYourOwnLabel.setMargin(new Insets(6, 10, 16, 16));

			// add the labels to the display
			displayPanel.add(attentionPanel, BorderLayout.NORTH);
			displayPanel.add(addYourOwnLabel, BorderLayout.CENTER);
		}
	}

	/**
	 * Does nothing! This analysis just provides information and never changes.
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
		// Do nothing! This analysis just provides information and never
		// changes.

		// But you can do anything you like! For example, the following
		// calculates the number and percentage of occupied cells on the
		// lattice. Once you have that information, you could alter the
		// "createDisplayPanel()" method to display that information.
		// Remember that this analyze method gets called automatically once
		// every generation, so your display panel would get updated every
		// generation!

		// // the total number of cells
		// int totalNumberOfCells = 0;
		//
		// // the number of occupied cells
		// int numOccupied = 0;
		//
		// // the percentage of occupied cells
		// double percentOccupied = 0;
		//
		// // get an iterator over the lattice
		// Iterator cellIterator = lattice.iterator();
		//
		// // get each cell on the lattice
		// Cell cell = null;
		// while(cellIterator.hasNext())
		// {
		// // add one more to the total number of cells
		// totalNumberOfCells++;
		//
		// // get the cell
		// cell = (Cell) cellIterator.next();
		//
		// // get its state.
		// CellState state = (CellState) cell.getState(generation);
		//
		// // is the state "occupied"?
		// if(!state.isEmpty())
		// {
		// numOccupied++;
		// }
		// }
		//
		// // calculate the percent occupied
		// percentOccupied = numOccupied / (double) totalNumberOfCells;
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
	}
}
