/*
 CAFrame -- a class within the Cellular Automaton Explorer. 
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
import java.awt.Dimension;
import java.awt.Container;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import cellularAutomata.CAConstants;
import cellularAutomata.CAController;
import cellularAutomata.CASplash;
import cellularAutomata.CurrentProperties;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.view.LatticeView;
import cellularAutomata.util.GBC;
import cellularAutomata.util.files.CAImageIconLoader;
import cellularAutomata.util.graphics.HalfTransparentGlassPane;
import cellularAutomata.util.graphics.ShimmyingTenTimesIconJButton;
import cellularAutomata.util.graphics.ShrinkingJFrame;

/**
 * The cellular automata graphics. Creates a JFrame with a control panel that
 * updates CA properties. Also incorporates a CA panel where all of the CA
 * graphics are drawn.
 * 
 * @author David Bahr
 */
public class CAFrame implements ActionListener
{
	/*
	 * Is true if the ctrl key is currently down. False otherwise. This is
	 * useful for macs which use the ctrl key in place of a mouse right click.
	 */
	public static boolean controlKeyDown = false;

	// the left inset used by the grid bag layout when positioning the CA
	// graphics scroll pane (when the pane is positioned on the right-hand
	// side)
	private final static int LEFT_INSET = 3;

	// the left inset used by the grid bag layout when positioning the CA
	// graphics scroll pane (when the pane is positioned on the right-hand
	// side)
	private final static int RIGHT_INSET = 1;

	// the CA lattice
	private Lattice lattice = null;

	// The content pane from the JFrame
	private Container contentPane = null;

	// the frame to which the graphics are added
	private ShrinkingJFrame frame = null;

	// the panel that holds all of the JFrame's components
	private JPanel contentPanel = null;

	// the scrollPane for the graphics
	private JScrollPane graphicsScrollPane = null;

	// the menu bar (File, Help, etc.)
	private CAMenuBar menuBar = null;

	// the tool bar (Save image, Print image, Help image, etc.)
	private CAToolBar toolBar = null;

	// the JPanels that will display the graphics in the frame
	private LatticeView graphicsPanel = null;

	// The JPanel that holds all the property, start, and status panels
	private AllPanel controlPanel = null;

	// The JPanel that holds all the status info
	private StatusPanel statusPanel = null;

	// a timer that is used to check when the splash screen is finished so we
	// can safely display this frame
	private Timer timer = null;

	/**
	 * Maximum height of the CA graphics panel within the JFrame.
	 */
	public final static int MAX_CA_HEIGHT = (int) (Toolkit.getDefaultToolkit()
			.getScreenSize().height / 1.8);

	/**
	 * Maximum width of the CA graphics panel within the JFrame.
	 */
	public final static int MAX_CA_WIDTH = (int) (Toolkit.getDefaultToolkit()
			.getScreenSize().width / 1.8);

	/**
	 * Preferred height of the JFrame.
	 */
	public final static int PREFERRED_FRAME_HEIGHT = (int) (Toolkit
			.getDefaultToolkit().getScreenSize().height / 1.15);

	/**
	 * Preferred width of the JFrame.
	 */
	public final static int PREFERRED_FRAME_WIDTH = (int) Math.min(
			PREFERRED_FRAME_HEIGHT * 1.25, Toolkit.getDefaultToolkit()
					.getScreenSize().width / 1.1);

	// public final static int PREFERRED_FRAME_WIDTH = (int) (Toolkit
	// .getDefaultToolkit().getScreenSize().width / 1.1);

	/**
	 * The default width of the tabbed panes. May be smaller if the screen
	 * resolution is smaller than approximately 900.
	 */
	public final static int TABBED_PANE_WIDTH_DEFAULT = 300;

	/**
	 * The dimension of the tabbed pane (control panels). This value is set when
	 * the frame is instantiated and sized (packed).
	 */
	public static Dimension tabbedPaneDimension = new Dimension(Math.min(
			PREFERRED_FRAME_WIDTH / 3, TABBED_PANE_WIDTH_DEFAULT), 600);

	/**
	 * Create the graphics.
	 * 
	 * @param lattice
	 *            The cellular automata lattice.
	 * @param graphicsPanel
	 *            A panel that displays the CA graphics.
	 */
	public CAFrame(Lattice lattice, LatticeView graphicsPanel)
	{
		// make sure we haven't already done this.
		if(frame == null)
		{
			this.lattice = lattice;
			this.graphicsPanel = graphicsPanel;

			// setting the graphics configuration to match the current device
			GraphicsConfiguration graphicsConfiguration = GraphicsEnvironment
					.getLocalGraphicsEnvironment().getDefaultScreenDevice()
					.getDefaultConfiguration();

			// create the Frame
			frame = new ShrinkingJFrame(CAConstants.PROGRAM_TITLE + " "
					+ CAConstants.VERSION, graphicsConfiguration);

			// set an image for the frame
			ImageIcon icon = CAImageIconLoader
					.loadImage(CAConstants.APPLICATION_ALTERNATIVE_ICON_IMAGE_PATH);
			frame.setIconImage(icon.getImage());

			// make the window react appropriately to resizing events
			CAFrameComponentListener frameResizeListener = new CAFrameComponentListener(
					this);
			frame.addComponentListener(frameResizeListener);

			// make the program exit when click on X
			CAFrameListener frameListener = new CAFrameListener(this);
			frame.addWindowListener(frameListener);

			// but won't exit before asking!
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

			// add a glass pane for blurring, etc.
			frame.setGlassPane(new HalfTransparentGlassPane());

			// the JPanel that will hold all of the components
			// GradientPanel contentPanel = new GradientPanel(new
			// GridBagLayout(),
			// new Color(100, 100, 100), new Color(175, 175, 175));
			// new Color(175, 175, 175), new Color(225, 225, 225));
			contentPanel = new JPanel(new GridBagLayout());

			// create a menu bar (and add to the frame)
			menuBar = new CAMenuBar(lattice, this);
			frame.setJMenuBar(menuBar);

			// create a scrollPane
			graphicsScrollPane = new JScrollPane(graphicsPanel);
			Dimension size = new Dimension(graphicsScrollPane.getSize().width,
					graphicsScrollPane.getSize().height);
			graphicsScrollPane.setPreferredSize(size);
			graphicsScrollPane.setMinimumSize(size);
			graphicsScrollPane.setMaximumSize(size);

			// create consistent colors
			graphicsScrollPane.getViewport().setBackground(
					frame.getBackground());
			graphicsPanel.setBackground(frame.getBackground());

			// create a tool bar (and make the menuBar its listener, since they
			// do most of the same things)
			toolBar = new CAToolBar(menuBar);

			// make sure the toolbar is tall enough in case it needs to wrap
			// onto two lines
			if(toolBar.isGoingToWrap())
			{
				toolBar.setMinimumSize(new Dimension(PREFERRED_FRAME_WIDTH,
						toolBar.getToolBarHeight()));
				toolBar.setMaximumSize(new Dimension(PREFERRED_FRAME_WIDTH,
						toolBar.getToolBarHeight()));
				toolBar.setPreferredSize(new Dimension(PREFERRED_FRAME_WIDTH,
						toolBar.getToolBarHeight()));
			}

			// create a JPanel to display all the control buttons
			controlPanel = new AllPanel(frame, this);

			// if the facade is on, then disable most tabs (note that the menu
			// bar and tool bar must be instantiated before this)
			menuBar.showFacade(CurrentProperties.getInstance().isFacadeOn());

			// get the status panel to be displayed along the bottom
			statusPanel = controlPanel.getStatusPanel();

			// bind the ctrl key to the components (so can
			// right click on a mac)
			bindCtrlKey(toolBar, menuBar, graphicsScrollPane, graphicsPanel,
					controlPanel);

			// get content pane
			contentPane = frame.getContentPane();

			// Arranges the components with the controls on the left. Places
			// components on a JPanel called the contentPanel (not the
			// contentPane).
			createLeftLayout();

			// add all of the components onto the contentPane (and therefore the
			// JFrame)
			contentPane.add(contentPanel);

			// set the size of the frame
			setInitialFrameSize();

			// fix the width and height of the tabbed pane (controlPanel). Can
			// only do this after the frame has been sized with
			// setInitialFrameSize().
			// tabbedPaneDimension = new Dimension(frame.getWidth()
			// - graphicsScrollPane.getWidth() - frame.getInsets().left
			// - frame.getInsets().right, graphicsScrollPane.getHeight());
			// controlPanel.setPreferredSize(tabbedPaneDimension);
			// controlPanel.setMinimumSize(tabbedPaneDimension);
			// controlPanel.setMaximumSize(tabbedPaneDimension);

			// make the frame visible -- we need a timer to keep trying this
			// until the splash screen is done.
			timer = new Timer(100, this);
			timer.setInitialDelay(0);
			timer.start();
		}
	}

	/**
	 * Binds the ctrl key to the component (so that a "right-click" can be used
	 * with macs by using a ctrl-click).
	 * 
	 * @param component
	 */
	public static void bindCtrlKey(JComponent component)
	{
		// the action to take when the ctrl key is pressed
		Action control_pressed_action = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				controlKeyDown = true;
			}
		};

		// the action to take when the ctrl key is released
		Action control_released_action = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				controlKeyDown = false;
			}
		};

		// This was a huge pain in the a**. Getting ctrl keys to work as
		// anything other than a mask is annoying. Try not to muck with this
		// code too much unless you get a brilliant idea about how to simplify
		// this.

		component.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL,
						InputEvent.CTRL_MASK, false), "control_pressed_1");
		component.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0, true),
				"control_released_1");
		component.getActionMap().put("control_pressed_1",
				control_pressed_action);
		component.getActionMap().put("control_released_1",
				control_released_action);
	}

	/**
	 * Binds the ctrl key to each component (so that a "right-click" can be used
	 * with macs by using a ctrl-click).
	 * 
	 * @param component1
	 *            A component to which the ctrl key is bound.
	 * @param component2
	 *            A component to which the ctrl key is bound.
	 * @param component3
	 *            A component to which the ctrl key is bound.
	 * @param component4
	 *            A component to which the ctrl key is bound.
	 * @param component5
	 *            A component to which the ctrl key is bound.
	 */
	private void bindCtrlKey(JComponent toolBar, JComponent menuBar,
			JComponent graphicsScrollPane, JComponent graphicsPanel,
			JComponent controlPanel)
	{
		// the action to take when the ctrl key is pressed
		Action control_pressed_action = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				controlKeyDown = true;
			}
		};

		// the action to take when the ctrl key is released
		Action control_released_action = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				controlKeyDown = false;
			}
		};

		// This was a huge pain in the a**. Getting ctrl keys to work as
		// anything other than a mask is annoying. Try not to muck with this
		// code too much unless you get a brilliant idea about how to simplify
		// this.

		toolBar.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL,
						InputEvent.CTRL_MASK, false), "control_pressed_1");
		toolBar.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0, true),
				"control_released_1");
		toolBar.getActionMap().put("control_pressed_1", control_pressed_action);
		toolBar.getActionMap().put("control_released_1",
				control_released_action);

		menuBar.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL,
						InputEvent.CTRL_MASK, false), "control_pressed_2");
		menuBar.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0, true),
				"control_released_2");
		menuBar.getActionMap().put("control_pressed_2", control_pressed_action);
		menuBar.getActionMap().put("control_released_2",
				control_released_action);

		graphicsScrollPane.getInputMap(
				JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL,
						InputEvent.CTRL_MASK, false), "control_pressed_3");
		graphicsScrollPane.getInputMap(
				JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0, true),
				"control_released_3");
		graphicsScrollPane.getActionMap().put("control_pressed_3",
				control_pressed_action);
		graphicsScrollPane.getActionMap().put("control_released_3",
				control_released_action);

		graphicsPanel.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(
						KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL,
								InputEvent.CTRL_MASK, false),
						"control_pressed_4");
		graphicsPanel.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0, true),
						"control_released_4");
		graphicsPanel.getActionMap().put("control_pressed_4",
				control_pressed_action);
		graphicsPanel.getActionMap().put("control_released_4",
				control_released_action);

		controlPanel.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(
						KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL,
								InputEvent.CTRL_MASK, false),
						"control_pressed_5");
		controlPanel.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0, true),
						"control_released_5");
		controlPanel.getActionMap().put("control_pressed_5",
				control_pressed_action);
		controlPanel.getActionMap().put("control_released_5",
				control_released_action);
	}

	/**
	 * Make the CA frame visible and send to the front.
	 * 
	 * @return true if the frame was made visible or if it is already visible.
	 */
	private boolean makeFrameVisible()
	{
		boolean visible = true;

		// only make visible if all of these are satisfied
		if(CAController.doneStartingTheApplication && CASplash.isFinished()
				&& (frame != null) && !frame.isVisible())
		{
			frame.setVisible(true);

			// just in case
			frame.toFront();

			// Sometimes necessary because the controlPanel has been resized.
			// fitGraphicsToScrollPane();

			// fixes a bug where some combinations of graphics cards and OS
			// won't call repaints properly. This forces a paint so that the
			// application starts up ok.
			frame.getGraphics().drawLine(0, 0, 0, 0);
		}
		else
		{
			visible = false;
		}

		return visible;
	}

	/**
	 * Sets the initial size of the JFrame.
	 */
	private void setInitialFrameSize()
	{
		// set the preferred size of the frame
		frame.setBounds(new Rectangle(0, 0, PREFERRED_FRAME_WIDTH,
				PREFERRED_FRAME_HEIGHT));
		frame.setMaximumSize(new Dimension(PREFERRED_FRAME_WIDTH,
				PREFERRED_FRAME_HEIGHT));
		frame.setMinimumSize(new Dimension(PREFERRED_FRAME_WIDTH,
				PREFERRED_FRAME_HEIGHT));
		frame.setPreferredSize(new Dimension(PREFERRED_FRAME_WIDTH,
				PREFERRED_FRAME_HEIGHT));

		// sizes the frame to the components
		frame.pack();

		// make any adjustments to the size of the CA graphics to fit inside the
		// scrollPane
		fitGraphicsToScrollPane();

		// display window at center of monitor screen
		frame.setLocationRelativeTo(null);
	}

	/**
	 * Waits for notification from the splash screen.
	 */
	public void actionPerformed(ActionEvent e)
	{
		// try displaying and check if we succeeded
		if(makeFrameVisible())
		{
			// stop the timer that keeps trying to display the frame -- we have
			// succeeded
			timer.stop();

			// make the start button shimmy so the user knows to go here next
			getToolBar().getStartButton().startShaking(
					ShimmyingTenTimesIconJButton.SUGGESTED_SHIMMYING_TIME);
		}

		// if we did not succeed, then the timer will keep trying
	}

	/**
	 * Sets the JFrame layout so that the controls appear on the left.
	 */
	public void createLeftLayout()
	{
		// add the tool bar
		contentPanel.add(toolBar, new GBC(0, 0).setSpan(2, 1).setFill(GBC.NONE)
				.setWeight(0.0, 0.0).setAnchor(GBC.NORTHWEST));

		// add the CA control panel
		contentPanel.add(controlPanel, new GBC(0, 1).setSpan(1, 1).setFill(
				GBC.VERTICAL).setWeight(0.0, 500.0).setAnchor(GBC.NORTHWEST));

		// add the status panel at the bottom
		contentPanel.add(statusPanel, new GBC(0, 2).setSpan(2, 1).setFill(
				GBC.BOTH).setWeight(0.0, 0.0).setAnchor(GBC.SOUTHWEST));

		// add the CA graphics panel
		contentPanel
				.add(graphicsScrollPane, new GBC(1, 1).setSpan(1, 1).setFill(
						GBC.BOTH).setWeight(500.0, 500.0).setAnchor(GBC.CENTER)
						.setInsets(5, LEFT_INSET, 0, RIGHT_INSET));

		// top,
		// left,
		// bot,
		// right

		// An Attempt to make the JToolBar wrap when the screen is small --
		// didn't work.
		//		
		// JPanel bottomPanel = new JPanel(new GridBagLayout());
		//				
		// // add the CA control panel
		// bottomPanel.add(controlPanel, new GBC(0, 1).setSpan(1, 1).setFill(
		// GBC.VERTICAL).setWeight(0.0, 500.0).setAnchor(GBC.NORTHWEST));
		//		
		// // add the status panel at the bottom
		// bottomPanel.add(statusPanel, new GBC(0, 2).setSpan(2, 1).setFill(
		// GBC.BOTH).setWeight(0.0, 0.0).setAnchor(GBC.SOUTHWEST));
		//		
		// // add the CA graphics panel
		// bottomPanel
		// .add(graphicsScrollPane, new GBC(1, 1).setSpan(1, 1).setFill(
		// GBC.BOTH).setWeight(500.0, 500.0).setAnchor(GBC.CENTER)
		// .setInsets(5, LEFT_INSET, 0, RIGHT_INSET));
		//				
		// // add the tool bar
		// contentPanel.setLayout(new java.awt.BorderLayout());
		// contentPanel.add(toolBar, java.awt.BorderLayout.NORTH);
		// contentPanel.add(bottomPanel, java.awt.BorderLayout.CENTER);
	}

	/**
	 * Sets the JFrame layout so that the controls appear on the right.
	 */
	public void createRightLayout()
	{
		// add the tool bar
		contentPanel.add(toolBar, new GBC(0, 0).setSpan(2, 1).setFill(GBC.NONE)
				.setWeight(0.0, 0.0).setAnchor(GBC.NORTHWEST));

		// add the CA graphics panel
		contentPanel
				.add(graphicsScrollPane, new GBC(0, 1).setSpan(1, 1).setFill(
						GBC.BOTH).setWeight(500.0, 500.0).setAnchor(GBC.CENTER)
						.setInsets(5, RIGHT_INSET, 0, LEFT_INSET));

		// top,
		// left,
		// bot,
		// right

		// add the status panel at the bottom
		contentPanel.add(statusPanel, new GBC(0, 2).setSpan(2, 1).setFill(
				GBC.BOTH).setWeight(0.0, 0.0).setAnchor(GBC.SOUTHWEST));

		// add the CA control panel
		contentPanel.add(controlPanel, new GBC(1, 1).setSpan(1, 1).setFill(
				GBC.VERTICAL).setWeight(0.0, 500.0).setAnchor(GBC.NORTHWEST));
	}

	/**
	 * Resizes the CA graphics panel so that it matches the size of the scroll
	 * pane (maximizes the size of the graphics within the scroll pane).
	 */
	public void fitGraphicsToScrollPane()
	{
		// resize the graphics to match the size of the scrollPane.
		int topInset = graphicsScrollPane.getInsets().top;
		int bottomInset = graphicsScrollPane.getInsets().bottom;
		int rightInset = graphicsScrollPane.getInsets().right;
		int leftInset = graphicsScrollPane.getInsets().left;
		int viewWidth = graphicsScrollPane.getWidth() - rightInset - leftInset;
		int viewHeight = graphicsScrollPane.getHeight() - topInset
				- bottomInset;

		int panelWidth = graphicsPanel.getDisplayWidth();
		int panelHeight = graphicsPanel.getDisplayHeight();
		double widthFactor = (double) viewWidth / (double) panelWidth;
		double heightFactor = (double) viewHeight / (double) panelHeight;
		if(widthFactor < heightFactor)
		{
			graphicsPanel.resizePanel(widthFactor);
		}
		else
		{
			graphicsPanel.resizePanel(heightFactor);
		}
	}

	/**
	 * Gets the left inset used by the grid bag layout when positioning the CA
	 * graphics scroll pane (when the pane is positioned on the right-hand side
	 * of the layout).
	 * 
	 * @return The left inset.
	 */
	public int getLeftInset()
	{
		return LEFT_INSET;
	}

	/**
	 * Gets the right inset used by the grid bag layout when positioning the CA
	 * graphics scroll pane (when the pane is positioned on the right-hand side
	 * of the layout).
	 * 
	 * @return The right inset.
	 */
	public int getRightInset()
	{
		return RIGHT_INSET;
	}

	/**
	 * Gets the cellular automaton's lattice.
	 * 
	 * @return A cellular automaton lattice.
	 */
	public Lattice getLattice()
	{
		return lattice;
	}

	/**
	 * The control panel that displays components to change the CA properties.
	 * 
	 * @return The panel.
	 */
	public AllPanel getControlPanel()
	{
		return controlPanel;
	}

	/**
	 * The frame.
	 * 
	 * @return The frame holding the graphics.
	 */
	public JFrame getFrame()
	{
		return frame;
	}

	/**
	 * The panel on which the graphics are drawn.
	 * 
	 * @return The panel.
	 */
	public LatticeView getGraphicsPanel()
	{
		return graphicsPanel;
	}

	/**
	 * The scroll pane on which the CA graphics are placed.
	 * 
	 * @return The scroll pane.
	 */
	public JScrollPane getGraphicsScrollPane()
	{
		return graphicsScrollPane;
	}

	/**
	 * The menu bar (File, Help, etc.).
	 * 
	 * @return The menu bar.
	 */
	public CAMenuBar getMenuBar()
	{
		return menuBar;
	}

	/**
	 * The scroll pane that holds the CA graphics.
	 * 
	 * @return The scroll pane.
	 */
	public JScrollPane getScrollPane()
	{
		return graphicsScrollPane;
	}

	/**
	 * The tool bar (save image, print image, etc.).
	 * 
	 * @return The tool bar.
	 */
	public CAToolBar getToolBar()
	{
		return toolBar;
	}

	/**
	 * Automatically resizes the CAFrame to the best size.
	 */
	public void resize()
	{
		fitGraphicsToScrollPane();
	}

	/**
	 * Set the background color.
	 * 
	 * @param color
	 *            The color choice.
	 */
	public void setBackgroundColor(Color color)
	{
		graphicsPanel.setBackground(color);
	}

	/**
	 * Set the panel on which the graphics are drawn.
	 */
	public void setGraphicsPanel(LatticeView graphicsPanel)
	{
		// contentPane.remove(this.graphicsPanel);
		// contentPane.add(graphicsPanel);
		this.graphicsPanel = graphicsPanel;
		this.graphicsScrollPane.getViewport().setView(graphicsPanel);
		frame.repaint();
	}

	/**
	 * Set the CA lattice.
	 * 
	 * @param lattice
	 *            The CA lattice.
	 */
	public void setLattice(Lattice lattice)
	{
		this.lattice = lattice;
	}

	/**
	 * Sets a title on the graphics frame.
	 * 
	 * @param title
	 *            The text displayed.
	 */
	public void setTitle(String title)
	{
		frame.setTitle(title);
	}

	/**
	 * Makes the frame appear half visible and half opaque to imply that it is
	 * disabled. This does not actually disable the components and only makes
	 * them appear that way.
	 * 
	 * @param disabled
	 *            When true, makes the frame appear disabled. When false makes
	 *            the frame appear enabled.
	 */
	public void setViewDisabled(boolean disabled)
	{
		if(disabled)
		{
			((HalfTransparentGlassPane) (frame.getGlassPane()))
					.setViewDisabled(true);
			frame.getGlassPane().setVisible(true);
		}
		else
		{
			frame.getGlassPane().setVisible(false);
		}
	}

	/**
	 * Make the frame visible or invisible.
	 * 
	 * @param visible
	 *            Makes graphics visible if true, invisible if false.
	 */
	public void setVisible(boolean visible)
	{
		frame.setVisible(visible);
	}

	/**
	 * Update all of the graphics panels and the frame.
	 */
	public void update()
	{
		// must happen before the repaint
		graphicsPanel.drawLattice(lattice);

		graphicsPanel.repaint();
	}

	/**
	 * Update all of the graphics panels and the frame.
	 * 
	 * @param atEndOfSimulation
	 *            True if this is the update when the simulation is stopped.
	 *            This method may choose to update the graphics differently in
	 *            that case.
	 */
	public void update(boolean atEndOfSimulation)
	{
		// if at the end of the simulation, redraw the whole lattice
		if(atEndOfSimulation)
		{
			// must happen before the repaint
			graphicsPanel.redraw();
		}
		else
		{
			// must happen before the repaint
			graphicsPanel.drawLattice(lattice);
		}

		frame.repaint();
	}
}
