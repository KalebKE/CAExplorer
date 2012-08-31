/*
 CAToolBar -- a class within the Cellular Automaton Explorer. 
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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;

import javax.swing.border.EmptyBorder;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import cellularAutomata.CurrentProperties;
import cellularAutomata.Cell;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.reflection.URLResource;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.graphics.ShakingIconJButton;
import cellularAutomata.util.graphics.ShimmyingTenTimesIconJButton;
import cellularAutomata.util.graphics.TranslatedImageIcon;

/**
 * A tool bar that displays images for buttons. The most popular menu items are
 * listed on this bar.
 * 
 * @author David Bahr
 */
public class CAToolBar extends JToolBar implements PropertyChangeListener
{
	// will be true when the layout needs to wrap the toolbar because
	// it is too wide
	private boolean isGoingToWrap = false;

	// the size of the separators between icons on the toolbar
	private int separatorSize = 20;

	// the number of separators used on the toolbar
	private int numSeparators = 0;

	// the height of the toolbar in pixels
	private int toolBarHeight = 0;

	private JButton colorChooserButton = null;

	private JButton cutMovieButton = null;

	private JButton fitToSizeButton = null;

	private JButton gridButton = null;

	private JButton helpButton = null;

	private JButton incrementButton = null;

	private JButton moveLeftButton = null;

	private JButton moveRightButton = null;

	private JButton movieButton = null;

	private JButton openButton = null;

	private JButton printButton = null;

	private JButton rabbitButton = null;

	private JButton saveAsImageButton = null;

	private JButton saveButton = null;

	private JButton showFacadeButton = null;

	private JButton showFullInterfaceButton = null;

	private JButton stopButton = null;

	private JButton turtleButton = null;

	private JButton zoomInButton = null;

	private JButton zoomOutButton = null;

	private ShakingIconJButton randomButton = null;

	private ShimmyingTenTimesIconJButton startButton = null;

	/**
	 * Create a tool bar.
	 */
	public CAToolBar(ActionListener listener)
	{
		super();

		this.setOpaque(true);

		// Get URL for location of the icon images.
		// (Searches the classpath to find the image file.)
		URL chooseColorURL = URLResource
				.getResource("/images/ColorChooser.gif");
		URL fitToSizeURL = URLResource.getResource("/images/FitToSize24.gif");
		URL gridURL = URLResource.getResource("/images/GridLines24.gif");
		URL helpURL = URLResource.getResource("/images/help.gif");
		URL incrementURL = URLResource.getResource("/images/Forward1.gif");
		URL moveLeftURL = URLResource.getResource("/images/move_left.gif");
		URL moveRightURL = URLResource.getResource("/images/move_right.gif");
		URL movieURL = URLResource.getResource("/images/movieGray24by24.gif");
		URL cutMovieURL = URLResource.getResource("/images/cutMovie24by24.gif");
		URL openURL = URLResource.getResource("/images/open.gif");
		URL printURL = URLResource.getResource("/images/print.gif");
		URL rabbitURL = URLResource.getResource("/images/rabbit24by24.gif");
		URL randomURL = URLResource.getResource("/images/dice24.gif");
		URL saveAsImageURL = URLResource.getResource("/images/saveAsImage.gif");
		URL saveURL = URLResource.getResource("/images/save.gif");
		URL showFacadeURL = URLResource.getResource("/images/ez.gif");
		URL showFullInterfaceURL = URLResource.getResource("/images/all.gif");
		URL startURL = URLResource.getResource("/images/start.gif");
		URL stopURL = URLResource.getResource("/images/stop.gif");
		URL turtleURL = URLResource.getResource("/images/turtle24by24.gif");
		URL zoomInURL = URLResource.getResource("/images/ZoomIn24.gif");
		URL zoomOutURL = URLResource.getResource("/images/ZoomOut24.gif");

		// create tool bar buttons
		colorChooserButton = new JButton(new ImageIcon(chooseColorURL));
		fitToSizeButton = new JButton(new ImageIcon(fitToSizeURL));
		gridButton = new JButton(new ImageIcon(gridURL));
		helpButton = new JButton(new ImageIcon(helpURL));
		incrementButton = new JButton(new ImageIcon(incrementURL));
		moveLeftButton = new JButton(new ImageIcon(moveLeftURL));
		moveRightButton = new JButton(new ImageIcon(moveRightURL));
		movieButton = new JButton(new ImageIcon(movieURL));
		cutMovieButton = new JButton(new ImageIcon(cutMovieURL));
		openButton = new JButton(new ImageIcon(openURL));
		printButton = new JButton(new ImageIcon(printURL));
		rabbitButton = new JButton(new ImageIcon(rabbitURL));
		randomButton = new ShakingIconJButton(
				new TranslatedImageIcon(randomURL));
		saveAsImageButton = new JButton(new ImageIcon(saveAsImageURL));
		saveButton = new JButton(new ImageIcon(saveURL));
		showFacadeButton = new JButton(new ImageIcon(showFacadeURL));
		showFullInterfaceButton = new JButton(new ImageIcon(
				showFullInterfaceURL));
		startButton = new ShimmyingTenTimesIconJButton(new TranslatedImageIcon(
				startURL));
		stopButton = new JButton(new ImageIcon(stopURL));
		turtleButton = new JButton(new ImageIcon(turtleURL));
		zoomInButton = new JButton(new ImageIcon(zoomInURL));
		zoomOutButton = new JButton(new ImageIcon(zoomOutURL));

		// make them transparent
		colorChooserButton.setOpaque(false);
		fitToSizeButton.setOpaque(false);
		gridButton.setOpaque(false);
		helpButton.setOpaque(false);
		incrementButton.setOpaque(false);
		moveLeftButton.setOpaque(false);
		moveRightButton.setOpaque(false);
		movieButton.setOpaque(false);
		cutMovieButton.setOpaque(false);
		openButton.setOpaque(false);
		printButton.setOpaque(false);
		rabbitButton.setOpaque(false);
		randomButton.setOpaque(false);
		saveAsImageButton.setOpaque(false);
		saveButton.setOpaque(false);
		showFacadeButton.setOpaque(false);
		showFullInterfaceButton.setOpaque(false);
		startButton.setOpaque(false);
		stopButton.setOpaque(false);
		turtleButton.setOpaque(false);
		zoomInButton.setOpaque(false);
		zoomOutButton.setOpaque(false);

		// nothing to save at first
		saveButton.setEnabled(false);

		// no movie yet
		cutMovieButton.setEnabled(false);

		// can't stop until started
		stopButton.setEnabled(false);

		// can't move left until move right
		moveLeftButton.setEnabled(false);

		// decide if the rabbit should be enabled
		if(CurrentProperties.getInstance().getTimeDelay() == 0)
		{
			rabbitButton.setEnabled(false);
		}

		// decide if the turtle should be enabled
		if(CurrentProperties.getInstance().getTimeDelay() == StartPanel.MAX_DELAY)
		{
			turtleButton.setEnabled(false);
		}

		// decide whether or not the color chooser should be enabled.
		String currentRuleClassName = CurrentProperties.getInstance()
				.getRuleClassName();
		Rule currentRule = ReflectionTool
				.instantiateMinimalRuleFromClassName(currentRuleClassName);
		if(IntegerCellState.isCompatibleRule(currentRule))
		{
			colorChooserButton.setEnabled(true);
		}
		else
		{
			colorChooserButton.setEnabled(false);
		}

		// COMMENTED OUT SO THAT THE USER ALWAYS SEES BOTH OF THE BUTTONS
		// ALSO COMMENTED OUT IN THE CAMenuBar.showFacade() METHOD.
		// display the facade button if the full interface is enable
		// showFacadeButton.setEnabled(!CurrentProperties.getInstance()
		// .isFacadeOn());
		// display the full interface button if the facade is enable
		// showFullInterfaceButton.setEnabled(CurrentProperties.getInstance()
		// .isFacadeOn());

		// add tool tips
		colorChooserButton.setToolTipText("drawing color");
		fitToSizeButton.setToolTipText("fit to screen");
		gridButton.setToolTipText("turn grid lines on/off");
		helpButton.setToolTipText("help");
		incrementButton.setToolTipText("increment by one generation");
		moveLeftButton.setToolTipText("move controls to left");
		moveRightButton.setToolTipText("move controls to right");
		movieButton.setToolTipText("make a movie");
		cutMovieButton.setToolTipText("stop/cut the movie");
		openButton.setToolTipText("import data");
		printButton.setToolTipText("print");
		rabbitButton.setToolTipText("speed up simulation");
		randomButton.setToolTipText("choose random rule");
		saveAsImageButton.setToolTipText("save as image");
		saveButton.setToolTipText("save as data");
		showFacadeButton.setToolTipText(CAMenuBar.SHOW_EASY_FACADE_TIP);
		showFullInterfaceButton
				.setToolTipText(CAMenuBar.SHOW_FULL_INTERFACE_TIP);
		startButton.setToolTipText("start");
		stopButton.setToolTipText("stop");
		saveButton.setToolTipText("save as data");
		turtleButton.setToolTipText("slow down simulation");
		zoomInButton.setToolTipText("zoom in");
		zoomOutButton.setToolTipText("zoom out");

		// add action commands to the buttons -- use the CAMenuBar action
		// commands since these buttons do the same things.
		colorChooserButton.setActionCommand(CAMenuBar.CHOOSE_DRAW_COLOR);
		fitToSizeButton.setActionCommand(CAMenuBar.FIT_TO_SCREEN);
		gridButton.setActionCommand(CAMenuBar.TOGGLE_MESH);
		helpButton.setActionCommand(CAMenuBar.HELP);
		incrementButton.setActionCommand(CAMenuBar.STEP1);
		moveLeftButton.setActionCommand(CAMenuBar.MOVE_CONTROLS);
		moveRightButton.setActionCommand(CAMenuBar.MOVE_CONTROLS);
		movieButton.setActionCommand(CAMenuBar.SAVE_AS_MOVIE);
		cutMovieButton.setActionCommand(CAMenuBar.STOP_MOVIE);
		openButton.setActionCommand(CAMenuBar.IMPORT_DATA);
		printButton.setActionCommand(CAMenuBar.PRINT);
		rabbitButton.setActionCommand(CAMenuBar.SPEED_UP);
		randomButton.setActionCommand(CAMenuBar.RANDOM);
		saveAsImageButton.setActionCommand(CAMenuBar.SAVE_AS_IMAGE);
		saveButton.setActionCommand(CAMenuBar.SAVE);
		showFacadeButton.setActionCommand(CAMenuBar.SHOW_EASY_FACADE);
		showFullInterfaceButton.setActionCommand(CAMenuBar.SHOW_FULL_INTERFACE);
		startButton.setActionCommand(CAMenuBar.START);
		stopButton.setActionCommand(CAMenuBar.STOP);
		turtleButton.setActionCommand(CAMenuBar.SLOW_DOWN);
		zoomInButton.setActionCommand(CAMenuBar.ZOOM_IN);
		zoomOutButton.setActionCommand(CAMenuBar.ZOOM_OUT);

		// add listeners to the buttons -- use the CAMenuBar listener since it
		// does the same thing
		colorChooserButton.addActionListener(listener);
		fitToSizeButton.addActionListener(listener);
		gridButton.addActionListener(listener);
		helpButton.addActionListener(listener);
		incrementButton.addActionListener(listener);
		moveLeftButton.addActionListener(listener);
		moveRightButton.addActionListener(listener);
		movieButton.addActionListener(listener);
		cutMovieButton.addActionListener(listener);
		openButton.addActionListener(listener);
		printButton.addActionListener(listener);
		rabbitButton.addActionListener(listener);
		randomButton.addActionListener(listener);
		saveAsImageButton.addActionListener(listener);
		saveButton.addActionListener(listener);
		showFacadeButton.addActionListener(listener);
		showFullInterfaceButton.addActionListener(listener);
		startButton.addActionListener(listener);
		stopButton.addActionListener(listener);
		turtleButton.addActionListener(listener);
		zoomInButton.addActionListener(listener);
		zoomOutButton.addActionListener(listener);

		// add a mouseover listener
		randomButton.addMouseListener(new ShakeDiceListener());

		// create the tool bar look and feel, etc.
		this.setBorderPainted(true);
		this.setFloatable(false);
		this.setRollover(true);

		// the number of separators
		numSeparators = 0;

		// add the buttons to the tool bar
		this.add(openButton);
		this.add(saveButton);
		this.add(saveAsImageButton);
		this.add(movieButton);
		this.add(cutMovieButton);
		this.add(printButton);
		this.addSeparator(new Dimension(separatorSize, 20));
		numSeparators++;
		this.add(moveLeftButton);
		this.add(moveRightButton);
		this.addSeparator(new Dimension(separatorSize, 20));
		numSeparators++;
		this.add(colorChooserButton);
		this.add(gridButton);
		this.addSeparator(new Dimension(separatorSize, 20));
		numSeparators++;
		this.add(zoomInButton);
		this.add(zoomOutButton);
		this.add(fitToSizeButton);
		this.addSeparator(new Dimension(separatorSize, 20));
		numSeparators++;
		this.add(randomButton);
		this.addSeparator(new Dimension(separatorSize, 20));
		numSeparators++;
		this.add(showFacadeButton);
		this.add(showFullInterfaceButton);
		this.addSeparator(new Dimension(separatorSize, 20));
		numSeparators++;
		this.add(turtleButton);
		this.add(rabbitButton);
		this.addSeparator(new Dimension(2 * separatorSize, 20));
		numSeparators++;
		numSeparators++;
		this.add(incrementButton);
		this.add(startButton);
		this.add(stopButton);
		this.addSeparator(new Dimension(2 * separatorSize, 20));
		numSeparators++;
		numSeparators++;
		this.add(helpButton);

		// resize the toolbar as necessary (if the toolbar is too wide)
		resizeToolBar();
	}

	/**
	 * Get the cut movie button.
	 * 
	 * @return the cut movie button.
	 */
	public JButton getCutMovieButton()
	{
		return cutMovieButton;
	}

	/**
	 * get the "fit to size" button.
	 * 
	 * @return the button that makes the CA graphics fit exactly within its
	 *         scroll pane.
	 */
	public JButton getFitToSizeButton()
	{
		return fitToSizeButton;
	}

	/**
	 * Get the help button.
	 * 
	 * @return the help button.
	 */
	public JButton getHelpButton()
	{
		return helpButton;
	}

	/**
	 * Get the increment button.
	 * 
	 * @return the increment button.
	 */
	public JButton getIncrementButton()
	{
		return incrementButton;
	}

	/**
	 * Get the move left button.
	 * 
	 * @return the move left button.
	 */
	public JButton getMoveLeftButton()
	{
		return moveLeftButton;
	}

	/**
	 * Get the move right button.
	 * 
	 * @return the move right button.
	 */
	public JButton getMoveRightButton()
	{
		return moveRightButton;
	}

	/**
	 * Get the movie button.
	 * 
	 * @return the movie button.
	 */
	public JButton getMovieButton()
	{
		return movieButton;
	}

	/**
	 * Get the speed up (rabbit) button.
	 * 
	 * @return the rabbit button.
	 */
	public JButton getRabbitButton()
	{
		return rabbitButton;
	}

	/**
	 * Get the save data button.
	 * 
	 * @return the save data button.
	 */
	public JButton getSaveButton()
	{
		return saveButton;
	}

	/**
	 * Get the show facade button.
	 * 
	 * @return the show facade button.
	 */
	public JButton getShowFacadeButton()
	{
		return showFacadeButton;
	}

	/**
	 * Get the show full interface button.
	 * 
	 * @return the show full interface button.
	 */
	public JButton getShowFullInterfaceButton()
	{
		return showFullInterfaceButton;
	}

	/**
	 * Get the start button.
	 * 
	 * @return the start button.
	 */
	public ShimmyingTenTimesIconJButton getStartButton()
	{
		return startButton;
	}

	/**
	 * Get the stop button.
	 * 
	 * @return the stop button.
	 */
	public JButton getStopButton()
	{
		return stopButton;
	}

	/**
	 * The height of the toolbar.
	 * 
	 * @return the height of the toolbar in pixels.
	 */
	public int getToolBarHeight()
	{
		return toolBarHeight;
	}

	/**
	 * Get the slow down (turtle) button.
	 * 
	 * @return the turtle button.
	 */
	public JButton getTurtleButton()
	{
		return turtleButton;
	}

	/**
	 * Get the "zoom in" button.
	 * 
	 * @return the "zoom in" button.
	 */
	public JButton getZoomInButton()
	{
		return zoomInButton;
	}

	/**
	 * Get the "zoom out" button.
	 * 
	 * @return the "zoom out" button.
	 */
	public JButton getZoomOutButton()
	{
		return zoomOutButton;
	}

	/**
	 * Whether or not the toolbar will have to wrap to fit in the display area.
	 * 
	 * @return true if the layout will make the toolbar wrap.
	 */
	public boolean isGoingToWrap()
	{
		return isGoingToWrap;
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
				saveButton.setEnabled(true);
			}
			else
			{
				saveButton.setEnabled(false);
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
					.instantiateMinimalRuleFromClassName(currentRuleClassName);
			if(IntegerCellState.isCompatibleRule(currentRule))
			{
				colorChooserButton.setEnabled(true);
			}
			else
			{
				colorChooserButton.setEnabled(false);
			}
		}
	}

	/**
	 * Resizes and wraps the toolbar if it is too wide.
	 */
	private void resizeToolBar()
	{
		// resize the toolbar as necessary (if the toolbar is too wide)
		int toolbarWidth = this.getPreferredSize().width;
		toolBarHeight = this.getPreferredSize().height;
		if(toolbarWidth > CAFrame.PREFERRED_FRAME_WIDTH)
		{
			// how many pixels over the frame width is the tool bar?
			int widthOverage = this.getPreferredSize().width
					- CAFrame.PREFERRED_FRAME_WIDTH;

			// calculates how big the separators are allowed to be
			int maxPossibleSizeOfSeparators = (int) Math.floor(widthOverage
					/ (double) numSeparators);

			// find out how many pixels we have removed by shrinking the
			// separators
			int pixelsSaved = (separatorSize * numSeparators)
					- (maxPossibleSizeOfSeparators * numSeparators);

			// did the smaller separators reduce the number of pixels by enough?
			if(toolbarWidth - pixelsSaved > CAFrame.PREFERRED_FRAME_WIDTH)
			{
				// Didn't save enough, so make the tool bar wrap. The default
				// Box layout won't wrap. Note that the FlowLayout does not
				// look as good as the BoxLayout, so FlowLayout should not be
				// the default layout.
				isGoingToWrap = true;
				FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);
				flowLayout.setHgap(0);
				flowLayout.setVgap(0);
				this.setLayout(flowLayout);
				this.setBorder(new EmptyBorder(0, 0, 0, 0));

				// use 2 because the wrap around will make it twice as tall
				toolBarHeight = 2 * this.getPreferredSize().height;
			}
			else
			{
				// did save enough space, so redo the layout with the
				// reduced-size separators
				separatorSize = maxPossibleSizeOfSeparators;

				// start over by removing components so that we can add them
				// again
				this.removeAll();

				// add the buttons to the tool bar
				this.add(openButton);
				this.add(saveButton);
				this.add(saveAsImageButton);
				this.add(movieButton);
				this.add(cutMovieButton);
				this.add(printButton);
				this.addSeparator(new Dimension(separatorSize, 20));
				this.add(moveLeftButton);
				this.add(moveRightButton);
				this.addSeparator(new Dimension(separatorSize, 20));
				this.add(colorChooserButton);
				this.add(gridButton);
				this.addSeparator(new Dimension(separatorSize, 20));
				this.add(zoomInButton);
				this.add(zoomOutButton);
				this.add(fitToSizeButton);
				this.addSeparator(new Dimension(separatorSize, 20));
				this.add(randomButton);
				this.addSeparator(new Dimension(separatorSize, 20));
				this.add(showFacadeButton);
				this.add(showFullInterfaceButton);
				this.addSeparator(new Dimension(separatorSize, 20));
				this.add(turtleButton);
				this.add(rabbitButton);
				this.addSeparator(new Dimension(2 * separatorSize, 20));
				this.add(incrementButton);
				this.add(startButton);
				this.add(stopButton);
				this.addSeparator(new Dimension(2 * separatorSize, 20));
				this.add(helpButton);
			}
		}
	}

	/**
	 * Handles mouse events when the user enters or exits the random button.
	 * makes the dice shake.
	 * 
	 * @author David Bahr
	 */
	private class ShakeDiceListener extends MouseAdapter
	{
		/**
		 * Make the dice start shaking.
		 */
		public void mouseEntered(MouseEvent e)
		{
			// make the dice shake continuously with 100 milliseconds per shake
			randomButton.startShaking(100);
		}

		/**
		 * Make the dice stop shaking.
		 */
		public void mouseExited(MouseEvent e)
		{
			randomButton.stopShaking();
		}

		/**
		 * Make the dice stop shaking.
		 */
		public void mousePressed(MouseEvent e)
		{
			randomButton.stopShaking();
		}
	}

}
