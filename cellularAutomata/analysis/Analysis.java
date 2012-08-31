/*
 Analysis -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.analysis;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.EventListenerList;

import cellularAutomata.CAConstants;
import cellularAutomata.CurrentProperties;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.view.listener.AnalysisDrawingListener;
import cellularAutomata.lattice.view.listener.AnalysisDrawingListener.MouseState;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.files.CAImageIconLoader;
import cellularAutomata.util.graphics.HalfTransparentGlassPane;
import cellularAutomata.util.graphics.ShrinkingJFrame;

/**
 * Analyzes cellular automaton data. For example, might calculate fractal
 * dimension, or might calculate cycle length.
 * <p>
 * All child classes are loaded dynamically using reflection.
 * <p>
 * Code in the CA lattice warns the user/developer if a child of Analysis has
 * been written without the required constructor (with properties as a
 * parameter). This way the contract is enforced "nicely" by the CA code.
 * <p>
 * Note that this class follows the Observer design pattern. All Analysis
 * classes are observers. The CAController is the observable class. Observers
 * are attached to the observable with the CAController's startAnalysis()
 * method, and removed with the stopAnalysis() method. The CAController notifies
 * the observers by calling its analyzeData() method. Rather than an "update"
 * method, the concrete subclasses of the Analysis class (observer classes) have
 * an analyze() method.
 * <p>
 * Also note that the classic Observer pattern has been modified to include a
 * Template Method pattern. The "update" method is actually replaced with a
 * concrete analyzeData() method (the template method) that performs error
 * checking. The subclasses of Analysis implement the abstract analyze() method
 * that is called by analyzeData().
 * <p>
 * AND, this class also implements the Observer interface so that it can be
 * informed of any changes in the AnalysisDrawingListener (the Observable).
 * 
 * @author David Bahr
 */
public abstract class Analysis implements Observer, PropertyChangeListener
{
	/**
	 * String used for the text display on the close button and for setting its
	 * action command (also used when the user selects the close "X" button on
	 * the analysis' JFrame).
	 */
	public static final String CLOSE_ANALYSIS = "Close analysis";

	/**
	 * Tooltip for the "close" button.
	 */
	private static final String CLOSE_TIP = "Stops and closes this analysis.";

	/**
	 * String used for text display on the "show as frame" button and for
	 * setting its action command.
	 */
	public static final String SHOW_AS_FRAME = "Undock"; // "Show as frame"

	/**
	 * String used for text display on the "show as tab" button and for setting
	 * its action command.
	 */
	public static final String SHOW_AS_TAB = "Dock"; // "Show as tab"

	// the time in milliseconds that the closing animation runs
	private static final int ANIMATION_LENGTH = 300;

	/**
	 * Message displayed when the analysis has no display component.
	 */
	private static final String EMPTY_DISPLAY_MESSAGE = "There is no display "
			+ "associated with this analysis.";

	/**
	 * Tooltip for the "show as frame" button.
	 */
	private static final String SHOW_AS_FRAME_TIP = "Detaches tab and "
			+ "shows as frame.";

	/**
	 * Tooltip for the "show as tab" button.
	 */
	private static final String SHOW_AS_TAB_TIP = "Shows analysis as a "
			+ "tab instead of separate frame.";

	// An action listener for the buttons that convert the display between a
	// frame and a tab (and vice-versa)
	private ActionListener actionListener = null;

	// this object
	private Analysis analysis = null;

	// the dimensions of the tab panel (defaults to 100 by 100)
	private Dimension scrollViewPortSize = new Dimension(100, 100);

	// Warns the user that the Analyzer is incompatible with the selected rule.
	private boolean hasPrintedCompatibilityWarning = false;

	// allows us to add PropertyChangeListeners listeners to this class
	private EventListenerList listenerList = new EventListenerList();

	// fonts for display
	private Fonts fonts = new Fonts();

	// the generation being analyzed
	private int generation = 0;

	// button for killing the analysis frame
	private JButton closeFrameButton = null;

	// button for killing the analysis tab
	private JButton closeTabButton = null;

	// button for detaching and showing tabbed panel as a frame
	private JButton showAsFrameButton = null;

	// button for closing the analysis frame and showing as a tab instead
	private JButton showAsTabButton = null;

	// the analysis display frame
	private ShrinkingJFrame displayFrame = null;

	// the analysis panel (created by a subclass)
	private JPanel analysisPanel = null;

	// the panel that holds the "return to tab" button
	private JPanel showAsTabPanel = null;

	// the panel that holds the "show as frame" button
	private JPanel showAsFramePanel = null;

	// the analysis display panel for the tabbed pane
	private JPanel tabPanel = null;

	// the scroll pane for the display panel (inside a JPanel)
	private JScrollPane displayScrollPanel = null;

	// the actual scrollPane
	private JScrollPane scrollPane = null;

	// the lattice being analyzed
	private Lattice lattice = null;

	// the rule being analyzed
	private Rule rule = null;

	/**
	 * Creates an analyzer for the CA data. By contract, all child classes must
	 * have a constructor with a single boolean parameter, similar to this
	 * constructor.
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
	public Analysis(boolean minimalOrLazyInitialization)
	{
		if(!minimalOrLazyInitialization)
		{
			createCloseFrameButton();
			createCloseTabButton();

			this.analysis = this;
		}
	}

	/**
	 * An action listener for the buttons that convert the display between a
	 * frame and a tab (and vice-versa), and also for the button that closes the
	 * analysis.
	 */
	public void addFrameButtonListener(ActionListener listener)
	{
		this.actionListener = listener;

		if(listener != null)
		{
			// create the buttons if necessary
			if(showAsFrameButton == null)
			{
				createShowAsFrameButton();
			}
			if(showAsTabButton == null)
			{
				createShowAsTabButton();
			}

			showAsFrameButton.addActionListener(actionListener);
			showAsTabButton.addActionListener(actionListener);
			closeFrameButton.addActionListener(actionListener);
			closeTabButton.addActionListener(actionListener);
		}
	}

	/**
	 * Adds a change listener. Should be used with great care.
	 * 
	 * @param listener
	 *            the listener to add.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		listenerList.add(PropertyChangeListener.class, listener);
	}

	/**
	 * Analyzes the cellular automaton data. This method is called once per
	 * generation, after the cells have been updated for that generation. Also
	 * called when the analysis is first started up.
	 * 
	 * @param lattice
	 *            The CA lattice.
	 * @param rule
	 *            The CA rule.
	 * @param generation
	 *            The current generation of the CA. There is no requirement that
	 *            this be the generation analyzed, but typically, this will be
	 *            the generation analyzed.
	 * @return true if successfully analyzed the data.
	 */
	public boolean analyzeData(Lattice lattice, Rule rule, int generation)
	{
		// store this data in case we want to rerun the analysis
		this.lattice = lattice;
		this.rule = rule;
		this.generation = generation;

		boolean successful = false;

		try
		{
			// this avoids problems if the analysis is rerun (see
			// rerunAnalysis() method).
			synchronized(this)
			{
				analyze(lattice, rule, generation);
			}

			// completed the analysis successfully
			successful = true;
		}
		catch(Exception e)
		{
			// Failed due to exception in analysis.
			if(!hasPrintedCompatibilityWarning)
			{
				// print this warning (details added below).
				String warning = "";

				// the folder that has user properties
				String analysisPackageName = "package "
						+ CurrentProperties.getInstance()
								.getUserAnalysisPackage();

				if(this.getClass().getPackage().toString().equals(
						analysisPackageName))
				{
					warning = "The analysis \""
							+ getDisplayName()
							+ "\" was unable to complete its task.  \n\n"
							+ "If this analysis was added by the user, it may have \n"
							+ "programming errors. The following message and stack \n"
							+ "trace was generated: \n" + e.getMessage() + "\n"
							+ e.getStackTrace();
				}
				else
				{
					warning = "Sorry, the analysis \"" + getDisplayName()
							+ "\" was unable to complete its task.";
				}

				JOptionPane.showMessageDialog(null, warning, "Warning",
						JOptionPane.INFORMATION_MESSAGE);
			}
			hasPrintedCompatibilityWarning = true;
		}
		catch(Throwable error)
		{
			// Failed due to exception in user's analysis.
			if(!hasPrintedCompatibilityWarning)
			{
				String warning = "The selected analysis \"" + getDisplayName()
						+ "\" has an error from which \n it cannot recover. "
						+ "Details follow: \n\n" + error.getMessage() + "\n\n"
						+ error.getStackTrace();
				JOptionPane.showMessageDialog(null, warning,
						"Developer Warning", JOptionPane.WARNING_MESSAGE);
			}
			hasPrintedCompatibilityWarning = true;
		}

		return successful;
	}

	/**
	 * Analyzes the cellular automaton data. This method is called once per
	 * generation, after the cells have been updated for that generation. Also
	 * called when the analysis is first started up.
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
	protected abstract void analyze(Lattice lattice, Rule rule, int generation);

	/**
	 * Gets a JPanel that may display results and/or request specific input
	 * information that the analysis needs to operate correctly. Should be
	 * overridden by child classes. This panel will automatically be displayed
	 * in a separate frame inside of a scroll pane. <br>
	 * Note that if returns null, then no panel is displayed.
	 * 
	 * @return A display for the analysis results and/or for requesting specific
	 *         input information that the analysis needs to operate correctly.
	 *         May return null if no display is desired.
	 */
	protected abstract JPanel getDisplayPanel();

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
	 * Gets the button that is used to close the analysis.
	 * 
	 * @return close button.
	 */
	public JButton getCloseFrameButton()
	{
		return closeFrameButton;
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
	public abstract String[] getCompatibleLattices();

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
	 * Return null if compatible with all rules.
	 * 
	 * @return A list of rules compatible with this Analysis (returns the
	 *         display names for the rules). Returns null if compatible with all
	 *         rules.
	 */
	public abstract String[] getCompatibleRules();

	/**
	 * Gets a JFrame that displays results and/or requests specific input
	 * information that the analysis needs to operate correctly. This frame will
	 * wrap the display inside of a scroll pane.
	 * 
	 * @param scrollViewSize
	 *            The dimensions of the scroll pane's view port. In other words,
	 *            the size of the display area for the analysis.
	 * @return A display for the analysis results and/or for requesting specific
	 *         input information that the analysis needs to operate correctly.
	 */
	public ShrinkingJFrame getDisplayFrame(Dimension scrollViewSize)
	{
		if(displayFrame == null)
		{
			if(tabPanel != null)
			{
				// remove the displayScrollPane from the tabPanel
				// so that we can add it to the frame instead (can't
				// be in both places)
				tabPanel.remove(displayScrollPanel);
			}

			displayFrame = createDisplayFrame(scrollViewSize);
		}
		else
		{
			// displayFrame already exists, so just add the scrollPane.
			// But don't want to accidentally display it twice,
			// so first remove it
			Container contentPane = displayFrame.getContentPane();
			contentPane.removeAll();

			// now add the components back
			contentPane.add(layoutTopAndBottomFramePanels(displayScrollPanel,
					showAsTabPanel));
		}

		return displayFrame;
	}

	/**
	 * Gets a JPanel that displays results and/or requests specific input
	 * information that the analysis needs to operate correctly. This panel is
	 * designed for display in a tabbed pane, and will wrap the display inside
	 * of a scroll pane.
	 * 
	 * @param scrollViewSize
	 *            The dimensions of the scroll pane's view port. In other words,
	 *            the size of the display area for the analysis.
	 * @return A display for the analysis results and/or for requesting specific
	 *         input information that the analysis needs to operate correctly.
	 */
	public JPanel getDisplayPanelForTabbedPane(Dimension scrollViewSize)
	{
		this.scrollViewPortSize = scrollViewSize;

		if(displayScrollPanel == null)
		{
			displayScrollPanel = this.createDisplayScrollPane(scrollViewSize);
		}

		if(tabPanel == null)
		{
			if(displayFrame != null)
			{
				// remove the displayScrollPane from the displayFrame
				// so that we can add it to the tabPanel instead (can't
				// be in both places)
				displayFrame.remove(displayScrollPanel);
			}

			// get the bottom panel with a "show as frame" button
			showAsFramePanel = createShowAsFramePanel();

			tabPanel = new JPanel();
			tabPanel.setLayout(new GridBagLayout());

			// add items to the properties panel
			tabPanel.add(displayScrollPanel, new GBC(0, 1).setSpan(1, 1)
					.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
					.setInsets(1));
			tabPanel.add(showAsFramePanel, new GBC(0, 2).setSpan(1, 1).setFill(
					GBC.BOTH).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
					.setInsets(1));
		}
		else
		{
			// tabPanel already exists, so just add the scrollPane.
			// But don't want to accidentally display it twice,
			// so first remove it
			tabPanel.removeAll();

			// now add the components back
			tabPanel.add(displayScrollPanel, new GBC(0, 1).setSpan(1, 1)
					.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
					.setInsets(1));
			tabPanel.add(showAsFramePanel, new GBC(0, 2).setSpan(1, 1).setFill(
					GBC.BOTH).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
					.setInsets(1));
		}

		return tabPanel;
	}

	/**
	 * Creates a JFrame that displays results and/or requests specific input
	 * information that the analysis needs to operate correctly. This frame will
	 * wrap the display inside of a scroll pane.
	 * 
	 * @param scrollViewSize
	 *            The dimensions of the scroll pane's view port. In other words,
	 *            the size of the display area for the analysis.
	 * @return A Jframe holding the analysis.
	 */
	private ShrinkingJFrame createDisplayFrame(Dimension scrollViewSize)
	{
		this.scrollViewPortSize = scrollViewSize;

		if(displayScrollPanel == null)
		{
			displayScrollPanel = this.createDisplayScrollPane(scrollViewSize);
		}
		else if(tabPanel != null)
		{
			// remove the displayScrollPane from the tabPanel
			// so that we can add it to the frame instead (can't
			// be in both places)
			tabPanel.remove(displayScrollPanel);
		}

		// get the bottom panel with a "show as tab" button
		showAsTabPanel = createShowAsTabPanel();

		// put top and bottom together
		JPanel framePanel = layoutTopAndBottomFramePanels(displayScrollPanel,
				showAsTabPanel);

		// create the frame
		PauseSimulationShrinkingJFrame frame = new PauseSimulationShrinkingJFrame(
				getDisplayName());
		frame.setAnimationLength(ANIMATION_LENGTH);

		// set an image for the frame
		ImageIcon icon = CAImageIconLoader
				.loadImage(CAConstants.APPLICATION_ALTERNATIVE_ICON_IMAGE_PATH);
		frame.setIconImage(icon.getImage());

		// don't let them close with the "X" button
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		// instead close it my way
		frame.addWindowListener(new AnalysisFrameListener());

		// and take actions when resized (empty by default)
		frame.addComponentListener(new AnalysisFrameComponentListener());

		// get content pane
		Container contentPane = frame.getContentPane();
		contentPane.add(framePanel);

		// set the preferred size of the frame
		int slop = 50;
		Dimension frameSize = new Dimension(displayScrollPanel.getWidth()
				+ slop, displayScrollPanel.getHeight()
				+ showAsTabPanel.getPreferredSize().height + slop);
		frame.setPreferredSize(frameSize);

		// sizes the frame to the components
		frame.pack();

		// This looks like a strange place to set the preferred size of the
		// analysis panel. But it has to follow the frame.pack().
		// Now that the frame has been packed, we can get the original size of
		// the analysis panel. And then we can set its preferred size. This
		// means that when the analysis panel is redisplayed (for example in the
		// tabbed pane), then it will fit itself to this preferred size. The
		// slop probably isn't necessary, but I like to be safe.
		int moreSlop = 10;
		Dimension analysisSize = new Dimension(
				analysisPanel.getPreferredSize().width, analysisPanel
						.getPreferredSize().height
						+ moreSlop);
		analysisPanel.setPreferredSize(analysisSize);

		return frame;
	}

	/**
	 * Gets a JScrollPane (inside a JPanel) that displays results and/or
	 * requests specific input information that the analysis needs to operate
	 * correctly.
	 * 
	 * @param scrollViewSize
	 *            The dimensions of the scroll pane's view port. In other words,
	 *            the size of the display area for the analysis.
	 * @return A display for the analysis results and/or for requesting specific
	 *         input information that the analysis needs to operate correctly.
	 */
	private JScrollPane createDisplayScrollPane(Dimension scrollViewSize)
	{
		// create a raised inner panel to hold the scroll pane
		JPanel raisedPanel = new JPanel(new BorderLayout());
		raisedPanel.setBorder(BorderFactory.createRaisedBevelBorder());

		// get the top panel with the analysis display (this has to happen after
		// changing the scrollViewPortSize, in case the getDisplayPanel method
		// calls the getPreferredScrollViewPortSize() method).
		analysisPanel = getDisplayPanel();

		// if there is no display, then create an empty one
		if(analysisPanel == null)
		{
			JLabel emptyLabel = new JLabel(EMPTY_DISPLAY_MESSAGE);
			JPanel emptyMessagePanel = new JPanel(new FlowLayout());
			emptyMessagePanel.add(emptyLabel);

			JPanel northPanel = new JPanel(new FlowLayout());
			northPanel.add(new JLabel(" "));

			JPanel southPanel = new JPanel(new FlowLayout());
			southPanel.add(new JLabel(" "));

			analysisPanel = new JPanel(new BorderLayout());
			analysisPanel.add(northPanel, BorderLayout.NORTH);
			analysisPanel.add(southPanel, BorderLayout.SOUTH);
			analysisPanel.add(emptyMessagePanel, BorderLayout.CENTER);
		}

		// put the analysis in a raised inner panel
		raisedPanel.add(analysisPanel, BorderLayout.CENTER);

		// put the display in a scroll pane
		scrollPane = new JScrollPane(raisedPanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		// reduce the size of the scrollViewSize to account for the raised panel
		// borders. Start by finding how big is the border of the raised panel?
		// And the scrollPane?
		Insets raisedPanelInsets = raisedPanel.getInsets();
		int totalInsetWidth = raisedPanelInsets.left + raisedPanelInsets.right
				+ scrollPane.getInsets().left + scrollPane.getInsets().right
				+ scrollPane.getVerticalScrollBar().getWidth();
		int totalInsetHeight = raisedPanelInsets.top + raisedPanelInsets.bottom
				+ scrollPane.getInsets().top + scrollPane.getInsets().bottom;
		Dimension actualViewSize = new Dimension(scrollViewSize.width
				- totalInsetWidth, scrollViewSize.height - totalInsetHeight);
		this.scrollViewPortSize = actualViewSize;

		// adjust the size of the scroll pane
		scrollPane.getViewport().setPreferredSize(actualViewSize);

		// adjust the size of the JPanel
		int analysisHeight = analysisPanel.getPreferredSize().height;
		if(analysisHeight == 0)
		{
			analysisHeight = actualViewSize.height;
		}

		// don't let the analysis get too small (smaller than the visible space)
		analysisPanel.setMinimumSize(new Dimension(actualViewSize.width,
				analysisHeight));

		// force the analysis to fit in the visible width
		if(restrictDisplayWidthToVisibleSpace())
		{
			analysisPanel.setPreferredSize(new Dimension(actualViewSize.width,
					analysisHeight));
		}

		// so can reference elsewhere
		displayScrollPanel = scrollPane;

		return displayScrollPanel;
	}

	/**
	 * Creates a "close" button for the tab.
	 */
	private void createCloseFrameButton()
	{
		if(closeFrameButton == null)
		{
			// add button for undocking the tabbed pane
			closeFrameButton = new JButton(CLOSE_ANALYSIS);
			closeFrameButton.setToolTipText(CLOSE_TIP);
			closeFrameButton.setFont(fonts.getBoldSmallerFont());

			// the action command has to change depending on the class since
			// more than one may be used hence the use of getDisplayName()
			closeFrameButton
					.setActionCommand(CLOSE_ANALYSIS + getDisplayName());
			// the listener is added in the method addFrameButtonListener()
		}
	}

	/**
	 * Creates a "close" button for the tab.
	 */
	private void createCloseTabButton()
	{
		if(closeTabButton == null)
		{
			// add button for undocking the tabbed pane
			closeTabButton = new JButton(CLOSE_ANALYSIS);
			closeTabButton.setToolTipText(CLOSE_TIP);
			closeTabButton.setFont(fonts.getBoldSmallerFont());

			// the action command has to change depending on the class since
			// more than one may be used hence the use of getDisplayName()
			closeTabButton.setActionCommand(CLOSE_ANALYSIS + getDisplayName());
			// the listener is added in the method addFrameButtonListener()
		}
	}

	/**
	 * Creates a "show as frame" button.
	 */
	private void createShowAsFrameButton()
	{
		if(showAsFrameButton == null)
		{
			// add button for undocking the tabbed pane
			showAsFrameButton = new JButton(SHOW_AS_FRAME);
			showAsFrameButton.setToolTipText(SHOW_AS_FRAME_TIP);
			showAsFrameButton.setFont(fonts.getBoldSmallerFont());

			// the action command has to change depending on the class since
			// more than one may be used hence the use of getDisplayName()
			showAsFrameButton
					.setActionCommand(SHOW_AS_FRAME + getDisplayName());
			// the listener is added in the method addFrameButtonListener()
		}
	}

	/**
	 * Creates a "show as tab" button.
	 */
	private void createShowAsTabButton()
	{
		if(showAsTabButton == null)
		{
			// add button for undocking the tabbed pane
			showAsTabButton = new JButton(SHOW_AS_TAB);
			showAsTabButton.setToolTipText(SHOW_AS_TAB_TIP);
			showAsTabButton.setFont(fonts.getBoldSmallerFont());

			// the action command has to change depending on the class since
			// more than one may be used hence the use of getDisplayName()
			showAsTabButton.setActionCommand(SHOW_AS_TAB + getDisplayName());
			// the listener is added in the method addFrameButtonListener()
		}
	}

	/**
	 * Creates a "show as frame" button and puts in a JPanel.
	 * 
	 * @return contains the "show as frame" button.
	 */
	private JPanel createShowAsFramePanel()
	{
		createShowAsFrameButton();

		// create a panel that holds the buttons
		BorderLayout innerLayout = new BorderLayout();
		JPanel innerPanel = new JPanel(innerLayout);
		innerPanel.add(closeTabButton, BorderLayout.WEST);
		innerPanel.add(showAsFrameButton, BorderLayout.EAST);

		return innerPanel;
	}

	/**
	 * Creates a "show as tab" button and puts in a JPanel.
	 * 
	 * @return contains the "show as tab" button.
	 */
	private JPanel createShowAsTabPanel()
	{
		createShowAsTabButton();

		// create a panel that holds the buttons
		BorderLayout innerLayout = new BorderLayout();
		JPanel innerPanel = new JPanel(innerLayout);
		innerPanel.add(closeFrameButton, BorderLayout.WEST);
		innerPanel.add(showAsTabButton, BorderLayout.EAST);

		return innerPanel;
	}

	/**
	 * When creating the frame, this lays out the two components (bottom button
	 * panel and the top analysis scroll pane).
	 * 
	 * @param displayScrollPanel
	 *            The analysis scroll pane.
	 * @param bottomPanel
	 *            The bottom panel that holds the buttons to close the analysis.
	 * @return A panel that combines the frame's bottom button panel and the top
	 *         analysis scroll pane.
	 */
	private JPanel layoutTopAndBottomFramePanels(
			JScrollPane displayScrollPanel, JPanel bottomPanel)
	{
		JPanel framePanel = new JPanel();
		framePanel.setLayout(new GridBagLayout());

		// scroll panel
		int row = 0;
		framePanel.add(displayScrollPanel, new GBC(1, row).setSpan(4, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// extra space
		row = 1;
		framePanel.add(new JLabel(" "), new GBC(1, row).setSpan(4, 1).setFill(
				GBC.HORIZONTAL).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));

		// bottom panel
		row = 2;
		framePanel.add(bottomPanel, new GBC(1, row).setSpan(4, 1).setFill(
				GBC.HORIZONTAL).setWeight(0.0, 0.0).setAnchor(GBC.WEST)
				.setInsets(1));

		return framePanel;
	}

	/**
	 * A very brief one or two-word string describing the analysis, appropriate
	 * for display as the title of a tabbed pane.
	 * 
	 * @return A string no longer than 15 characters.
	 */
	public abstract String getDisplayName();

	/**
	 * Get the CA lattice at the time analyze() was last called.
	 * 
	 * @return The CA lattice. May be null if analyze() has never been called.
	 */
	public Lattice getLattice()
	{
		return lattice;
	}

	/**
	 * Gets the size of the tab panel that holds the analysis. Developers may
	 * find this useful when laying out the display for their analyses.
	 * 
	 * @return The dimension of the tab panel.
	 */
	public Dimension getPreferredScrollViewportSize()
	{
		return scrollViewPortSize;
	}

	/**
	 * Get the CA rule at the time analyze() was last called.
	 * 
	 * @return The CA rule. May be null if analyze() has never been called.
	 */
	public Rule getRule()
	{
		return rule;
	}

	/**
	 * A brief description (written in HTML) that describes this analysis. The
	 * description will be displayed as a tooltip. Using html permits line
	 * breaks, font colors, etcetera, as described in HTML resources. Regular
	 * line breaks will not work.
	 * 
	 * @return An HTML string describing this analysis.
	 */
	public abstract String getToolTipDescription();

	/**
	 * This method can be overridden to handle mouse events on the CA graphics
	 * (on the Lattice). This method is automatically called by the update
	 * method (which is called by the AnalysisDrawingListener class -- an
	 * Observable class that listens to the mouse).
	 * <p>
	 * For example, see the PinCellValueAnalysis class.
	 * <p>
	 * By default, this method does nothing.
	 * <p>
	 * WARNING! This method is not thread safe. This method is called by a
	 * different thread than the thread which calls the analyze() method.
	 * Therefore, this method could potentially add or remove elements from a
	 * collection being iterated by the analyze() method. This will cause a
	 * concurrent modification error. The solution is to ensure that (1) this
	 * method never alters a collection of elements while the analyze() method
	 * is running, or (2) make this method (or the analyze() method) use a copy
	 * of the collection. See the PinCellValueAnalysis class for an example
	 * where the analyze() method uses a copy of a LinkedList to prevent a
	 * concurrent modification error.
	 * 
	 * @param listener
	 *            The mouse listener class that called this method.
	 * @param mouseState
	 *            The mouse state that existed when a mouse event forced this
	 *            method to be automatically called. Note that a MouseState is
	 *            an inner class of the AnalysisDrawingListener.
	 */
	protected void handleMouseEvents(AnalysisDrawingListener listener,
			MouseState mouseState)
	{
		// Override this method to implement specific behaviors. For example,
		// see the PinCellValueAnalysis class.

		// the following lines of code can ensure that the analysis redraws any
		// cells quickly (if it was changing the state of some cells on the
		// lattice)
		//
		// int xPos = mouseState.getXPos();
		// int yPos = mouseState.getYPos();
		// listener.drawCell(cell, xPos, yPos);
		// listener.updateGraphics();

		// IN THE FUTURE, I'LL MAKE THE GRAPHICS PANEL DIRECTLY ACCESSIBLE FROM
		// THE ANALYSIS CLASS SO THAT THE updateGraphics() METHOD CAN BE
		// CALLED FROM ANYWHERE, EVEN WHEN THE LISTENER ISN'T BEING USED. JUST
		// NEED TIME TO IMPLEMENT...
	}

	/**
	 * Checks if the given analysis is compatible with the current CA lattice
	 * and rule.
	 * 
	 * @param analysis
	 *            The analysis being checked for compatibility.
	 *            
	 * @return true if compatible.
	 */
	public static boolean isCompatibleAnalysis(Analysis analysis)
	{
		// Set to true if the analysis is compatible with BOTH the
		// selected rule AND the lattice.
		boolean okWithLatticeAndRule = false;

		if(analysis != null)
		{
			// Set to true if the analysis is compatible with the selected
			// lattice.
			boolean okWithLattice = false;

			String[] compatibleLattices = analysis.getCompatibleLattices();
			String[] compatibleRules = analysis.getCompatibleRules();

			if(compatibleLattices == null)
			{
				okWithLattice = true;
			}
			else
			{
				// check to see if the analysis works with the current
				// lattice
				String latticeDescription = CurrentProperties.getInstance()
						.getLatticeDisplayName();

				int i = 0;
				while(!okWithLattice && (i < compatibleLattices.length))
				{
					if(latticeDescription.equals(compatibleLattices[i]))
					{
						okWithLattice = true;
					}

					// try the next one
					i++;
				}
			}

			// if the lattice is ok, then make sure the rule is ok
			if(okWithLattice)
			{
				if(compatibleRules == null)
				{
					okWithLatticeAndRule = true;
				}
				else
				{
					// check to see if the analysis works with the current
					// rule

					// get the rule's description
					String ruleDescription = Rule.getCurrentRuleDisplayName();

					int i = 0;
					while(!okWithLatticeAndRule && (i < compatibleRules.length))
					{
						if(ruleDescription.equals(compatibleRules[i]))
						{
							okWithLatticeAndRule = true;
						}

						// try the next one
						i++;
					}
				}
			}
		}

		return okWithLatticeAndRule;
	}

	/**
	 * Override this method to handle the notification of any changes in
	 * properties. The default behavior is to do nothing.
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
	}

	/**
	 * Refresh the graphics. Useful for example, if the analysis tags cells for
	 * different display. Note that the graphics will NOT update if the controls
	 * have been set to display graphics only at the end of a simulation (or if
	 * set to display every nth time step when this isn't the nth time step).
	 * <p>
	 * WARNING! This method is slow. It's best to only call this method when the
	 * CA is stopped and when it's not being called repeatedly. If using a mouse
	 * listener, it's far better to refresh the graphics using the technique
	 * shown in the comments in the update method (below).
	 */
	public void refreshGraphics()
	{
		// this tells the CA it is time to refresh the graphics
		firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.REFRESH_GRAPHICS, CurrentProperties.TRUE,
				CurrentProperties.FALSE));
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
	 * Reruns the analysis on the same data.
	 * 
	 * @return tru if the analysis was rerun successfully.
	 */
	public boolean rerunAnalysis()
	{
		boolean success = false;

		if(lattice != null && rule != null && generation >= 0)
		{
			success = analyzeData(lattice, rule, generation);
		}

		return success;
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
	public abstract boolean restrictDisplayWidthToVisibleSpace();

	/**
	 * Performs any necessary functions to reset the analysis to its original
	 * state without restarting the analysis. This method will be called when a
	 * new CA simulation is started by the user.
	 */
	public abstract void reset();

	/**
	 * May be overriden by child classes that want to take some action when the
	 * analysis frame is resized. This is called when the JFrame holding the
	 * analysis is resized.
	 * <p>
	 * By default, this method does nothing. Child classes (analyses) may
	 * override if desired. For example, the SliceAnalysis overrides this
	 * method.
	 * <p>
	 * This method is called automatically when the analysis frame is resized
	 * and when it is converted between a tab and a pane. It is not recommended
	 * that this method be called directly.
	 * 
	 * @param e
	 *            The event that triggered this action. May be null.
	 */
	public void resizeActions(ComponentEvent e)
	{
		// Does nothing.
		// Child classes can override.
	}

	/**
	 * Override this method if the analysis wants to suspend regular mouse
	 * drawing behavior (the drawing and erasing of cells on the CA lattice).
	 * Sometimes, drawing events might interfere with an analysis. Sometimes
	 * regular mouse drawing might interfere with special drawing that is done
	 * by this analysis.
	 * <p>
	 * This method is only queried once when the analysis first starts.
	 * Therefore, changing the value while the analysis is running will have no
	 * effect.
	 * 
	 * @return false by default.
	 */
	public boolean shouldSuspendMouseDrawing()
	{
		return false;
	}

	/**
	 * Performs any necessary operations to stop the analysis. Developers should
	 * implement the abstract method stopAnalysis() rather than overriding this
	 * method.
	 */
	public void stop()
	{
		stop(null);
	}

	/**
	 * Performs any necessary operations to stop the analysis and then, if the
	 * analysis is displayed in a frame, this shrinks the frame to the specified
	 * point. Developers should implement the abstract method stopAnalysis()
	 * rather than overriding this method.
	 * 
	 * @param p
	 *            The point in space to which the frame will be shrunk. May be
	 *            null.
	 */
	public void stop(Point p)
	{
		// NOTE: the stopAnalysis() method is called from within the
		// actionBeforeShrinking() method of the inner class
		// PauseSimulationShrinkingJFrame. It is called there so that it happens
		// after the simulation has been paused. Happening before the
		// simulation is paused could cause a concurrent modification error.

		if(displayFrame != null)
		{
			// the amount of time it takes to shrink the frame
			// displayFrame.setAnimationLength(300);

		    	if(CAConstants.WINDOWS_XP_OS)
		    	{
		    	    	// only do this fancy closing for XP.  Other operating 
		    	    	// systems have nicer closing graphics and this isn't 
		    	    	// necessary.
        			if(p != null)
        			{
        				// This shrinks the frame, while moving it to the specified
        				// point. Then disposes of the frame.
        				displayFrame.shrink(p, true);
        			}
        			else
        			{
        				// this shrinks the frame, then disposes of the frame
        				displayFrame.shrink(true);
        			}
		    	}
		    	else
		    	{
		    	    // close the frame without the "shrinking graphics" above.  The 
		    	    // shrinking graphics aren't necessary for non XP operating 
		    	    // systems because they already close graphics nicely.
		    	    displayFrame.dispose();
		    	}
		}
		else
		{
			// pause the simulation.
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.PAUSE, null, new Boolean(true)));

			// This is called after the simulation has been paused. Happening
			// before the simulation is paused could cause a concurrent
			// modification error.
			//
			// Notify any non-local class that they need to
			// stop this analysis (i.e., notify the CAController).
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.STOP_ANALYSIS, null, analysis));

			// This is called after the simulation has been paused. Happening
			// before the simulation is paused could cause a concurrent
			// modification error.
			stopAnalysis();

			// unpause the simulation.
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.PAUSE, null, new Boolean(false)));
		}

		// release any tagged color that has been held by the analysis
		ColorScheme.releaseTaggedColor(this);
	}

	/**
	 * Performs any desired operations when the analysis is stopped (closed) by
	 * the user. For example, you might write the results to a file at this
	 * time. Or you might dispose of any windows that you opened. May do
	 * nothing.
	 */
	protected abstract void stopAnalysis();

	/**
	 * This method handles mouse "drawing" events on the CA graphics (drawn on
	 * the Lattice). This method is automatically called by the
	 * AnalysisDrawingListener class (an Observable class which listens to mouse
	 * events).
	 * <p>
	 * This method is required as part of the Observer design pattern. The
	 * Analysis class is the Observer which watches the Observable
	 * AnalysisDrawingListener.
	 * <p>
	 * Developers that wish to take actions based on mouse drawing events should
	 * override the handleMouseEvents() method.
	 * 
	 * @param o
	 *            The observable (AnalysisDrawingListener) class that called
	 *            this method.
	 * @param arg
	 *            The MouseState that existed when a mouse event forced this
	 *            method to be automatically called.
	 */
	public void update(Observable o, Object arg)
	{
		// casts the arguments into more useful forms
		handleMouseEvents(((AnalysisDrawingListener) o), (MouseState) arg);
	}

	/**
	 * Behavior when trying to close the analysis window.
	 * 
	 * @author David Bahr
	 */
	private class AnalysisFrameListener extends WindowAdapter
	{
		/**
		 * Create the listener.
		 */
		public AnalysisFrameListener()
		{
		}

		/**
		 * Sends an event to close the window.
		 */
		public void windowClosing(WindowEvent e)
		{
			// this fires an action event to close the analysis
			closeFrameButton.doClick();
		}
	}

	/**
	 * Behavior when resizing the analysis window.
	 * 
	 * @author David Bahr
	 */
	private class AnalysisFrameComponentListener extends ComponentAdapter
	{
		/**
		 * Create the listener.
		 */
		public AnalysisFrameComponentListener()
		{
		}

		/**
		 * Called when the analysis frame is resized.
		 */
		public void componentResized(ComponentEvent e)
		{
			// calls this method which does nothing. Can be overriden by child
			// classes if desired. For example, the SliceAnalysis overrides the
			// resizeActions method.
			resizeActions(e);
		}
	}

	/**
	 * Overrides the actionBeforeShrinking() and actionAfterShrinking() so that
	 * the frame pauses the simulation when closing.
	 * 
	 * @author David Bahr
	 */
	public class PauseSimulationShrinkingJFrame extends ShrinkingJFrame
	{
		/**
		 * Create a frame that pauses the simulation before closing.
		 * 
		 * @param title
		 *            The title displayed on the frame.
		 */
		public PauseSimulationShrinkingJFrame(String title)
		{
			super(title);
		}

		// override this method
		public void actionAfterShrinking()
		{
			// unpause the simulation.
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.PAUSE, null, new Boolean(false)));
		}

		// override this method
		public void actionBeforeShrinking()
		{
			// pause the simulation.
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.PAUSE, null, new Boolean(true)));

			// This is called after the simulation has been paused. Happening
			// before the simulation is paused could cause a concurrent
			// modification error.
			//
			// Notify any non-local class that they need to
			// stop this analysis (i.e., notify the CAController).
			firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.STOP_ANALYSIS, null, analysis));

			// This is called after the simulation has been paused. Happening
			// before the simulation is paused could cause a concurrent
			// modification error.
			stopAnalysis();
		}
	}
}
