/*
 * CAController -- a class within the Cellular Automaton Explorer. Copyright (C)
 * 2007 David B. Bahr (http://academic.regis.edu/dbahr/) This program is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version. This program
 * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package cellularAutomata;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.LinkedList;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import cellularAutomata.analysis.Analysis;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.graphics.CAMenuBar;
import cellularAutomata.graphics.CAToolBar;
import cellularAutomata.graphics.StatusPanel;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.io.FileStorage;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.view.LatticeView;
import cellularAutomata.lattice.view.listener.AnalysisDrawingListener;
import cellularAutomata.lattice.view.listener.CellValueListener;
import cellularAutomata.lattice.view.listener.DrawColorListener;
import cellularAutomata.lattice.view.listener.PositionListener;
import cellularAutomata.movie.MovieMaker;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.SwingWorker;
import cellularAutomata.util.files.JPGAndPNGImageReadWrite;

/**
 * Controls moment by moment actions of the CA, as directed by user input from
 * the graphics. Uses singleton pattern so that only one instance of the
 * controller may exist.
 * 
 * @author David Bahr
 */
public class CAController
{
	/**
	 * Indicates whether or not we are done with the initial starting of the
	 * application. This is set to true when the initial setup procedure is
	 * completed.
	 */
	public static boolean doneStartingTheApplication = false;

	/**
	 * Indicates when the setupCA method is running
	 */
	public static boolean currentlyRunningSetup = false;

	// CA parameters

	// A gate (or latch) that keeps track of how many parallel processing
	// threads have finished their tasks. (Each thread is updating a subset
	// of all of the CA cells.) The gate ensures that the program will not
	// continue until all threads have finished and called countDown(). The
	// endGate knows how many threads there are and will only open the gate
	// (latch) when the count down reaches zero.
	private CountDownLatch END_GATE = null;

	// The array of threads that do parallel processing on the cells.
	private static ParallelProcessingWorker[] parallelWorkers = null;

	// tells the delay method whether it should keep going or stop (exit)
	private boolean exitDelay = false;

	// a flag that remembers if the CA was running or stopped before it was
	// asked to pause. Volatile, so kept in main memory where all threads
	// can see it.
	private volatile boolean paused = false;

	// When true, the CA steps through generations. When false stops.
	private volatile boolean running = false;

	// When true, the CA steps through generations. When false stops.
	private volatile boolean startButtonPressed = false;

	// determines frequency of updates (once at the end, or every n time
	// steps)
	private boolean update_graphics_at_end = false;

	// true if need to update the view (the fast color array)
	private boolean viewChanged = false;

	// determines frequency of updates (once at the end, or every n time
	// steps)
	private int update_graphics_every_n_steps = 20;

	// number of iterations requested by the user (from graphics)
	private int maxSteps = 0;

	// the number of processors available for computations by the
	// incrementCA() threads. Will be less than the number of total processors
	// because some processors are used for garbage collection, event threads,
	// swing, etc. MAY BE NEGATIVE (likely!), which just means that parallel
	// processing is unlikely to yield many speed benefits because there are not
	// enough processors to out-pace the overhead of running multiple threads.
	private int numberOfAvailableProcessors = CurrentProperties.getInstance()
			.getNumberOfProcessors();

	// the last generation that was completed
	private int lastCompleteGeneration = 0;

	// the number of times we tried to build a lattice, but failed. Reset to
	// 0
	// every time the lattice is built successfully.
	private int numberOfAttempts = 0;

	// the number of states at start up (from the properties file)
	private int numStatesAtStartUp = 2;

	// the number of processors requested by the user (or program) from the
	// menu
	private int userSelectedNumberOfProcessors = numberOfAvailableProcessors;

	// the delay between generation (and graphics) updates
	private long timeDelay = 10;

	// listens to the mouse when on the lattice, and notifies the Analysis
	// classes
	private static AnalysisDrawingListener analysisDrawingListener = null;

	// A list of analyses that get run at every generation
	private static ArrayList<Analysis> analysisList = new ArrayList<Analysis>();

	// the rule number at start up (from the properties file)
	private BigInteger ruleNumberAtStartUp = new BigInteger("90");

	// the instance of this class, used by the singleton pattern
	private static CAController controller = null;

	// Listens for events from the GUI
	private CAControllerListener listener = null;

	// A factory that creates the lattice, graphics, etc.
	private CAFactory factory = null;

	// The graphics that displays the CA.
	private static CAFrame graphics = null;

	// the mouse listener that displays the current cell value under the
	// cursor
	private CellValueListener cellValueListener = null;

	// the mouse listener that responds to drawing events on the lattice
	private DrawColorListener mouseDrawingListener = null;

	// Saves data to a file
	private FileStorage data = null;

	// The CA lattice (linear, square, hexagonal, etc.)
	private Lattice lattice = null;

	// A list of analyses that are currently requesting that normal drawing
	// behavior be suspended. In other words, these analyses don't want the
	// user to draw on the graphics in a normal manner.
	private LinkedList<Analysis> suspendDrawingList = new LinkedList<Analysis>();

	// The CA Rule used to update the cells.
	private Rule rule = null;

	/**
	 * Gets the single instance of this controller (which runs everything).
	 */
	public static CAController getInstanceOfCAController()
	{
		if(controller == null)
		{
			controller = new CAController();
		}

		return controller;
	}

	/**
	 * Initializes and runs everything.
	 */
	private CAController()
	{
		// save the numStates and ruleNumber that was imported at start up
		numStatesAtStartUp = CurrentProperties.getInstance().getNumStates();
		ruleNumberAtStartUp = CurrentProperties.getInstance().getRuleNumber();

		// Instantiate the lattice, rule, cells, graphics, etc. This also starts
		// up the CAControllerListener so that this class can start handling
		// user input from the GUI. To really see how this class works, look at
		// how the CAControllerListener calls methods from this class.
		setupCA();

		// Indicate to anyone who wants to query, that we are done opening
		// the application. This should be the last line of this constructor
		// unless there was a bad error.
		doneStartingTheApplication = true;

		// just in case
		graphics.getFrame().repaint();

		// makes absolutely sure that the CA graphics fit perfectly within
		// their scroll pane. This is not usually necessary, but is on some
		// smaller screens (e.g., 1152 by 864).
		graphics.getToolBar().getFitToSizeButton().doClick();
	}

	/**
	 * Gets the frame that displays the graphics. This method is primarily for
	 * access by warning messages that need a parent component (so that the
	 * warning panels don't get lost).
	 */
	public static CAFrame getCAFrame()
	{
		return graphics;
	}

	/**
	 * Invokes analyses of the CA data. To decide which analyses to run,
	 * iterates over a list of analyses.
	 * 
	 * @param generation
	 *            The generation being analyzed. In fact, there is no
	 *            requirement that this be the generation analyzed, but it is
	 *            the current generation of the CA.
	 */
	public void analyzeData(int generation)
	{
		Iterator<Analysis> iterator = analysisList.iterator();
		while(iterator.hasNext())
		{
			// Note: not instantiating. Just getting the Analysis and
			// telling it to analyze the data.
			Analysis analysis = iterator.next();
			analysis.analyzeData(lattice, rule, generation);
		}
	}

	/**
	 * Clears all values from the lattice and updates the graphics to reflect
	 * this change.
	 */
	public void clearLattice()
	{
		// stop the CA (should have happen already, but just to be safe...)
		stopCA();

		if(lattice != null)
		{
			lattice.setInitialState(CurrentProperties.STATE_BLANK);

			// Redo the color array (used for fast graphics). It needs
			// changing after the initial states are set.
			updateFastDrawingColorArray();

			graphics.getGraphicsPanel().redraw();
			graphics.update();
			graphics.getControlPanel().getInitialStatesPanel()
					.setActiveRadioButton(CurrentProperties.STATE_BLANK);
		}
	}

	/**
	 * Create a thread for each subset of the cells. The size of each subset is
	 * determined by the number of available processors. This method should be
	 * called only if the number of available processors is greater than 1.
	 * 
	 * @param cells
	 *            The CA cells (that will be partitioned).
	 */
	private void createParallelProcessingWorkers(Cell[] cells)
	{
		// only do parallel processing if there are enough
		// processors
		if(numberOfAvailableProcessors >= 1)
		{
			parallelWorkers = new ParallelProcessingWorker[numberOfAvailableProcessors];

			// when we created the lattice (and when we called
			// setUserSelectedNumberOfProcessors()) we guaranteed that
			// numberOfAvailableProcessors >= cells.length, so this will
			// never be smaller than 1.
			int sizeOfEachParallelPartitionOfCells = (int) Math
					.round((double) cells.length
							/ (double) numberOfAvailableProcessors);

			for(int i = 0; i < numberOfAvailableProcessors; i++)
			{
				// grab a part of the lattice so it can be farmed out to
				// each processor
				int from = i * sizeOfEachParallelPartitionOfCells;
				int to = (i + 1) * sizeOfEachParallelPartitionOfCells;
				if(i == numberOfAvailableProcessors - 1)
				{
					// because of rounding (when calculating the size of
					// each lattice partition), need to make sure we always
					// get the last few cells
					to = cells.length;
				}
				parallelWorkers[i] = new ParallelProcessingWorker(cells, from,
						to);
			}
		}
		else
		{
			// reset, in case the number of processors was larger in the
			// previous simulation
			parallelWorkers = null;
		}
	}

	/**
	 * Delays the program for the specified number of milliseconds. Useful when
	 * the graphics are moving or updating too quickly.
	 * 
	 * @param timeDelay
	 *            The delay in milliseconds.
	 */
	public void delay(long timeDelay)
	{
		// Thread.sleep is a very costly call, even for timeDelay = 0
		// so this helps speed this method (when it isn't really being used)
		if(timeDelay != 0)
		{
			// for small time delays (<300 milliseconds), use the whole
			// delay without checking for a directive to "exitDelay". The user
			// won't notice the short wait.
			if(timeDelay < 300)
			{
				try
				{
					Thread.sleep(timeDelay);
				}
				catch(InterruptedException e)
				{
				}
			}
			else
			{
				// For longer delays, the user will notice if they have to wait
				// when the delay is supposed to be interrupted. So...

				// keep it in the loop until somewhere else says to exit
				exitDelay = false;

				int count = 1;
				while((count <= timeDelay) && !exitDelay)
				{
					try
					{
						Thread.sleep(1);
					}
					catch(InterruptedException e)
					{
					}

					count++;
				}
			}
		}
	}

	/**
	 * Stops the simulation immediately, even if it has not yet completed the
	 * update of a full generation. In other words, some cells may not be
	 * properly updated. This method can be very dangerous because restarting
	 * the simulation (with the start button) will probably crash the
	 * simulation.
	 */
	// public void emergencyStop()
	// {
	// caWorker.interrupt();
	// }
	/**
	 * Makes sure the user wants to exit and then ends the program.
	 */
	public void endProgram()
	{
		CAShutDown.exit(graphics);
	}

	/**
	 * Import new graphics. Note that this will means importing the graphics
	 * from a file, which must as a by-product also rebuild the lattice because
	 * the number of rows and columns may change.
	 */
	public void importGraphics()
	{
		// stop the CA (should have happen already, but just to be safe...)
		stopCA();

		if(factory != null)
		{
			// waits to be sure that the CA is no longer running. If it is
			// running and a setup occurs, then the lattice may be null
			// while the program tries to run. Again, threading and
			// synchronizing the lattice would also solve this, but I haven't
			// bothered yet.
			while(running)
			{
				// not an infinite loop -- the running variable is set to false
				// when the startCA() method is stopped.
			}

			// get new lattice that matches the size of the imported file
			Lattice lattice = null;

			// Note: this might fail when creating the lattice. Particularly
			// if the lattice is too large and get a heap error.
			try
			{
				// get the CA lattice (i.e., square, hexagonal, etc.)
				lattice = factory.getLattice();
			}
			catch(Throwable t)
			{
				String warning = "Insufficient memory to create the requested "
						+ "lattice. \nTry importing a different image.";

				if(graphics != null)
				{
					JOptionPane.showMessageDialog(graphics.getFrame(), warning,
							"Insufficient memory",
							JOptionPane.INFORMATION_MESSAGE);
				}
				else
				{
					JOptionPane.showMessageDialog(null, warning,
							"Insufficient memory",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}

			if(lattice != null)
			{
				this.lattice = lattice;

				// update just the CA graphics panel
				updateGraphicsPanel();

				// make sure the properties panel is updated (things like rows
				// and columns) may have changed when importing a file.
				graphics.getControlPanel().getController().resetNumRows();
				graphics.getControlPanel().getController().resetNumCols();

				// redraw the graphics
				graphics.update();
			}
		}
	}

	/**
	 * Steps the CA through a generation (a time step).
	 * 
	 * @return generation The generation after the update is complete.
	 */
	public int incrementCA()
	{
		// Now the actual CA!

		// update each cell on the lattice
		Cell[] cells = lattice.getCells();

		// Use parallel processing if there are enough processors. If there are
		// too few processors, then the overhead of thread management will
		// overwhelm any benefits from the parallel processing.
		if(numberOfAvailableProcessors <= 1)
		{
			// NOT ENOUGH processors for parallel processing
			for(int i = 0; i < cells.length; i++)
			{
				// Update each cell. The update usually depends on the
				// neighbors, but it may feel free to ignore the value of the
				// neighbors. Note that the neighbors does *not* include the
				// cell itself.
				cells[i].updateState(lattice.getNeighbors(cells[i]));

				// Now save the state's color data in a 1d array for faster
				// graphics.
				lattice.getView().setColorPixel(
						i,
						Cell.getView().getDisplayColor(cells[i].getState(),
								null, cells[i].getCoordinate()).getRGB());
			}
		}
		else
		{
			// THERE ARE ENOUGH processors for parallel processing!

			// The following gate (or latch) is used below to prevent the
			// code from going any further until all of the threads have
			// completed their tasks. When a thread has completed it's task it
			// calls END_GATE.countDown() which decrements a counter in
			// END_GATE. The END_GATE knows how many threads there are and waits
			// until the counter reaches zero.
			END_GATE = new CountDownLatch(numberOfAvailableProcessors);

			// start each parallel thread.
			for(int i = 0; i < numberOfAvailableProcessors; i++)
			{
				// The workers process a subset of the cells in parallel. The
				// parallelWorkers were instantiated in the setupCA() method.
				new Thread(parallelWorkers[i]).start();
			}

			try
			{
				// The following is a gate that prevents the code from going any
				// further until all of the threads have completed their tasks.
				// When a thread has completed it's task it calls
				// END_GATE.countDown() which decrements a counter in END_GATE.
				// The END_GATE knows how many threads there are and waits until
				// the counter reaches zero.
				// 
				// i.e., wait until all of the threads have ended their tasks
				END_GATE.await();
			}
			catch(InterruptedException ignore)
			{
			}
		}

		// get the current generation (from the first cell)
		// so it can be displayed in the status panel.
		// Will be displayed next time "graphics.update()" is
		// called.
		int generation = cells[0].getGeneration();

		// message sent as a property change event notifying other classes of
		// this update
		String message = Integer.toString(generation);

		listener.firePropertyChangeEvent(new PropertyChangeEvent(this,
				StatusPanel.GENERATION, null, message));
		listener.firePropertyChangeEvent(new PropertyChangeEvent(this,
				Cell.UNSAVED_DATA, null, CurrentProperties.TRUE));

		// only called if the number of available processors has changed
		if(userSelectedNumberOfProcessors != numberOfAvailableProcessors)
		{
			// set to the new number of processors
			numberOfAvailableProcessors = userSelectedNumberOfProcessors;

			// create a thread for each requested processor
			createParallelProcessingWorkers(cells);
		}

		return generation;
	}

	/**
	 * Indicates whether or not the simulation is running.
	 * 
	 * @return true if the simulation is running.
	 */
	public boolean isRunning()
	{
		return running;
	}

	/**
	 * Load a data file onto the current CA without restarting.
	 */
	public void loadData()
	{
		// load the data
		CAStateInitializer initializer = new CAStateInitializer(graphics,
				lattice);
		initializer.setInitialState(CurrentProperties.STATE_DATA);

		// Redo the color array (used for fast graphics). It needs changing
		// after the initial states are set.
		updateFastDrawingColorArray();

		// update the init states panel
		graphics.getControlPanel().getInitialStatesPanel().reset();

		// update the graphics
		graphics.update();

		// Reset any warnings that were suppressed during the loading. Warnings
		// are suppressed if multiple copies of the same warning would be
		// generated (that the cell state can't be instantiated as desired).
		// This generally happens when loading an invalid data or image file.
		CellState.resetWarnings();
	}

	/**
	 * Load an image onto the current CA without restarting.
	 */
	public void loadImage()
	{
		// load the image
		CAStateInitializer initializer = new CAStateInitializer(graphics,
				lattice);
		initializer.setInitialState(CurrentProperties.STATE_IMAGE);

		// Redo the color array (used for fast graphics). It needs changing
		// after the initial states are set.
		updateFastDrawingColorArray();

		// update the init states panel
		graphics.getControlPanel().getInitialStatesPanel().reset();

		// update the graphics
		graphics.update();

		// Reset any warnings that were suppressed during the loading. Warnings
		// are suppressed if multiple copies of the same warning would be
		// generated (that the cell state can't be instantiated as desired).
		// This generally happens when loading an invalid data or image file.
		CellState.resetWarnings();
	}

	/**
	 * Load a simulation into the CA, replacing the current CA.
	 * 
	 * @param filePath
	 *            The file that stores the new simulation.
	 */
	public void loadSimulation(String filePath)
	{
		// stop the simulation so that there is no chance that we will
		// create a new CellStateView that would conflict with the currently
		// running simulation.
		stopCA();

		// get the properties
		CurrentProperties properties = CurrentProperties.getInstance();

		// keep some of the old properties that we will need momentarily after
		// loading the new properties
		String saveDataFilePath = properties.getSaveDataFilePath();
		String saveImageFilePath = properties.getSaveImageFilePath();
		boolean facadeIsOn = properties.isFacadeOn();

		// load the new properties
		properties.updatePropertiesFromSimulationFile(filePath);

		// reset the data file path (so we can load data)
		properties.setInitialState(CurrentProperties.STATE_DATA);
		properties.setInitialStateDataFilePath(filePath);

		// reset the saveData and saveImage file paths -- we want to ignore the
		// imported paths and keep the ones set by the user during this session
		if(saveDataFilePath != null)
		{
			properties.setSaveDataFilePath(saveDataFilePath);
		}
		if(saveImageFilePath != null)
		{
			properties.setSaveImageFilePath(saveImageFilePath);
		}

		// reset the time delay -- we want to ignore the imported delay and keep
		// the current one.
		properties.setTimeDelay(timeDelay);

		// reset the display interval -- we want to ignore the imported interval
		// and keep the current one (otherwise have to reset the graphics which
		// would be a pain).
		properties.setDisplayStep(update_graphics_every_n_steps);
		properties.setUpdateAtEnd(update_graphics_at_end);

		// Don't change the number of parallel processors from the current
		// value. Otherwise, the .ca file might select a less optimal setting.
		CurrentProperties.getInstance().setNumberOfParallelProcessors(
				userSelectedNumberOfProcessors);

		// when loading a .ca file, it might contain "facade_is_on=false"
		// even though the facade is currently on. Obviously, we don't
		// want that, so this line immediately resets the value to
		// whatever it was before loading the simulation.
		CurrentProperties.getInstance().setFacade(facadeIsOn);

		// reset the properties (including rule, initial state, etc.)
		graphics.getControlPanel().getPropertiesPanel().reset();

		// why do this roundabout submission? Because the submitProperties()
		// method already takes care of everything. No need to do it again.
		graphics.getControlPanel().getAllPanelListener()
				.submitProperties(false);

		// set the color scheme from the properties (doesn't happen anywhere
		// else). This must happen after the cell's are created so that the
		// new CellStateView will be compatible with the new CellState.
		// Otherwise, setColorsFromProperties() might try to create drawing
		// colors for IntegerCellStates when the view is not compatible with
		// integer cell states.
		getCAFrame().getMenuBar().setColorsFromProperties();
	}

	/**
	 * Temporarily stops the CA. Different from stopCA() because this sets a
	 * flag that remembers if the CA was running or stopped before it was asked
	 * to pause.
	 * 
	 * @see #unPauseCA
	 */
	public void pauseCA()
	{
		// if have already paused the simulation, then do nothing
		if(!paused)
		{
			if(running)
			{
				// must be before the call to stopCA, because stopCA() will only
				// end a movie if paused = false (and we don't want the movie to
				// end with a pause)
				paused = true;

				stopCA();

				// AND must also be after the call to stopCA, because stopCA()
				// sets paused = false
				paused = true;
			}
			else
			{
				paused = false;
			}
		}
	}

	/**
	 * Prints the current graphics.
	 */
	public void print()
	{
		pauseCA();

		// make the JFrame look disabled
		graphics.setViewDisabled(true);

		CAPrinter.print(graphics.getGraphicsPanel(), graphics.getScrollPane());

		// make the JFrame look enabled
		graphics.setViewDisabled(false);

		unPauseCA();
	}

	/**
	 * Convenience method that updates the CA graphics, but only if the CA is
	 * stopped or if it is supposed to update at this time step. For example, if
	 * the CA is set to update only at the end, then this won't update the
	 * graphics unless the CA has stopped. <br>
	 * Similar to updateGraphicsPanel(), but updateGraphicsPanel() will always
	 * force an update.
	 */
	public void refreshCAGraphics()
	{
		if(!startButtonPressed
				|| (startButtonPressed && !update_graphics_at_end && (lastCompleteGeneration
						% update_graphics_every_n_steps == 0)))
		{
			updateGraphicsPanel();
		}
	}

	/**
	 * Resets the analyses without stopping them.
	 */
	public void resetAnalyses()
	{
		// make sure each analysis is compatible with the selected lattice and
		// rule. If not, this will stop and close that analysis. (It closes the
		// analysis by programatically clicking the check box. This will then
		// call the stopAnalysis method in his class.)
		graphics.getControlPanel().getAnalysisPanel()
				.enableCompatibleAnalyses();

		// reset the analysis
		Iterator<Analysis> iterator = analysisList.iterator();
		while(iterator.hasNext())
		{
			// reset
			Analysis analysis = iterator.next();
			analysis.reset();

			// now run once on the initial generation of the current
			// simulation
			analysis.analyzeData(lattice, rule, lastCompleteGeneration);
		}

		// so shows any changes made by resetting an analysis
		viewChanged = true;
	}

	/**
	 * If possible, steps the CA backwards by one generation. If not possible,
	 * nothing happens.
	 */
	public void rewindCA()
	{
		// default message to display on status panel
		String stopMessage = "Stopped. Cannot rewind any further.";

		// Make sure we can rewind. If not, do nothing
		Cell[] cells = lattice.getCells();
		int requiredNumberOfGenerations = cells[0].getRule()
				.getRequiredNumberOfGenerations();
		if(cells[0].getStateHistory().size() > requiredNumberOfGenerations)
		{
			// We *can* rewind, but we won't unless all analyses are closed.
			if(analysisList.size() > 0)
			{
				// warn them that they will have to close any open analyses
				String warningMessage = "All analyses must be closed to "
						+ "rewind the cellular automaton.";
				JOptionPane.showMessageDialog(graphics.getControlPanel()
						.getStartPanel(), warningMessage,
						"Must Close Analyses", JOptionPane.INFORMATION_MESSAGE);

				stopMessage = "Did not rewind because analyses were open.";
			}
			else
			{
				// rewind each cell on the lattice
				for(int i = 0; i < cells.length; i++)
				{
					// rewind by removing the current state
					cells[i].removeCurrentState();

					// then save the state's color data in a 1d array
					// for faster graphics.
					lattice.getView().setColorPixel(
							i,
							Cell.getView().getDisplayColor(cells[i].getState(),
									null, cells[i].getCoordinate()).getRGB());
				}

				// get the current generation (from the first cell)
				// so it can be displayed in the status panel.
				// will be displayed next time "graphics.update()" is
				// called.
				int generation = cells[0].getGeneration();
				String message = Integer.toString(generation);

				listener.firePropertyChangeEvent(new PropertyChangeEvent(this,
						StatusPanel.GENERATION, null, message));

				// let the menu know there is unsaved data
				listener.firePropertyChangeEvent(new PropertyChangeEvent(this,
						Cell.UNSAVED_DATA, null, CurrentProperties.TRUE));

				// decrement the generation
				lastCompleteGeneration--;

				// message to display on status panel
				stopMessage = "Paused after rewinding 1 generation.";
			}
		}

		// fire off a message to the control panel (and any other
		// listeners) letting it know we stopped rewinding. This must
		// happen before the status message is displayed. (Otherwise a
		// Stop message is displayed by this line.)
		listener.firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.START, null, new Boolean(false)));

		// fire off a message to the control panel (and any other
		// listeners) with an update for the status panel
		listener.firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.STATUS, null, stopMessage));

		// update all the graphics (control panel, status panel, and the CA
		// graphics panel). The true ensures we get a redraw().
		graphics.update(true);
	}

	/**
	 * Save the graphics image to a file (for example, JPEG).
	 */
	public void saveAsImage()
	{
		// save as an image
		JPGAndPNGImageReadWrite.save(graphics.getGraphicsPanel(),
				CurrentProperties.getInstance().getSaveImageFilePath());
	}

	/**
	 * Save the data, if we've been asked to.
	 */
	public void saveData()
	{
		data.save(lattice);

		// done saving, so disable the Save menu item on the menu bar
		CAMenuBar menuBar = graphics.getMenuBar();
		menuBar.getSaveMenuItem().setEnabled(false);

		// ditto on the tool bar
		CAToolBar toolBar = graphics.getToolBar();
		toolBar.getSaveButton().setEnabled(false);
	}

	/**
	 * Set the maximum number of steps before the CA will stop.
	 * 
	 * @param maxSteps
	 */
	public void setMaxSteps(int maxSteps)
	{
		this.maxSteps = maxSteps;
	}

	/**
	 * Set the delay between calculation of new generations.
	 * 
	 * @param timeDelay
	 */
	public void setTimeDelay(int timeDelay)
	{
		// this helps with threading. A minimal delay lets other threads catch
		// up and prevents tearing of the display etc. Essentially this is a bug
		// workaround.
		// if(timeDelay < 2)
		// {
		// timeDelay = 2;
		// }

		// stop any previous call to the delay() method, if we are getting
		// faster
		if(timeDelay < this.timeDelay - 10)
		{
			exitDelay = true;
		}

		// reset
		this.timeDelay = timeDelay;
	}

	/**
	 * Called when there was a failure building the lattice. Attempts to build a
	 * smaller lattice, or if that has already been tried, builds a default
	 * lattice.
	 */
	private void tryAgainAfterFailure(int numberOfAttempts)
	{
		if(numberOfAttempts == 1)
		{
			// make as much room as possible
			System.gc();

			// couldn't get a lattice, so try a smaller one
			int height = CurrentProperties.getInstance().getNumRows();
			int width = CurrentProperties.getInstance().getNumColumns();
			if(height > 100)
			{
				height = 100;
			}
			else if(height > 4)
			{
				height /= 2;
			}

			if(width > 100)
			{
				width = 100;
			}
			else if(width > 4)
			{
				width /= 2;
			}
			graphics.getControlPanel().getPropertiesPanel()
					.getNumColumnsSpinner().setValue(new Integer(width));
			graphics.getControlPanel().getPropertiesPanel().getNumRowsSpinner()
					.setValue(new Integer(height));
			CurrentProperties.getInstance().setNumRows(height);
			CurrentProperties.getInstance().setNumColumns(width);

			// warn them
			String warning = "Trying a smaller lattice with the same properties.";
			if(graphics != null)
			{
				JOptionPane.showMessageDialog(graphics.getFrame(), warning,
						"Smaller lattice", JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				JOptionPane.showMessageDialog(null, warning, "Smaller lattice",
						JOptionPane.INFORMATION_MESSAGE);
			}

			// try again
			setupCA();

			// reset the status panel
			graphics.getControlPanel().getStatusPanel().resetLabels();
		}
		else if(numberOfAttempts == 2)
		{
			// reset the properties
			CurrentProperties.getInstance().resetToDefaultProperties();

			// update the properties panel
			graphics.getControlPanel().getPropertiesPanel().reset();

			String warning = "Unable to create a smaller lattice, most likely \n"
					+ "due to insufficient memory. Will attempt to \n"
					+ "create a default simulation.";
			if(graphics != null)
			{
				JOptionPane.showMessageDialog(graphics.getFrame(), warning,
						"Insufficient memory", JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				JOptionPane.showMessageDialog(null, warning,
						"Insufficient memory", JOptionPane.INFORMATION_MESSAGE);
			}

			// try again
			setupCA();

			// reset the status panel
			graphics.getControlPanel().getStatusPanel().resetLabels();
		}
		else
		{
			// well, we did the best we could
			String warning = "Unable to continue with this task, most likely \n"
					+ "due to insufficient memory.  Try closing other \n "
					+ "applications and restarting.";
			if(graphics != null)
			{
				JOptionPane.showMessageDialog(graphics.getFrame(), warning,
						"Unable to continue.", JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				JOptionPane.showMessageDialog(null, warning,
						"Unable to continue.", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	/**
	 * Updates the color array that is used for drawing quick graphics. This
	 * method needs to be called whenever the cells change state. Poor
	 * connectivity/cohesion, but it's worth it to get the benefit of much
	 * faster graphics. This method is only relevant for rules that don't have
	 * special shapes for each cell's view. Otherwise the color array is not
	 * used.
	 */
	private void updateFastDrawingColorArray()
	{
		// save the state's color data in a 1d array for faster
		// graphics

		// save the color of each cell on the lattice
		Cell[] cells = lattice.getCells();

		for(int i = 0; i < cells.length; i++)
		{
			// save the state's color data in a 1d array for faster
			// graphics
			lattice.getView().setColorPixel(
					i,
					Cell.getView().getDisplayColor(cells[i].getState(), null,
							cells[i].getCoordinate()).getRGB());
		}

		// reset -- if the view changed, then this has now been handled
		viewChanged = false;
	}

	/**
	 * Set whether or not the graphics will be updated at the end of the
	 * simulation.
	 * 
	 * @param atEnd
	 *            When true, the graphics will be updated only at the end of the
	 *            simulation. When false, the graphics may be updated every
	 *            generation or at a specified interval.
	 */
	public void setUpdateGraphicsAtEnd(boolean atEnd)
	{
		update_graphics_at_end = atEnd;
	}

	/**
	 * Set the frequency with which the graphics will be updates.
	 * 
	 * @param interval
	 *            The graphics will be updated whenever the generation is a
	 *            multiple of this interval.
	 */
	public void setUpdateGraphicsEveryNSteps(int interval)
	{
		update_graphics_every_n_steps = interval;
	}

	/**
	 * Indicates whether or not the view has changed. When true, the graphics
	 * will "updateFastDrawingColorArray" at the next time step.
	 * 
	 * @param viewChanged
	 *            When true, the graphics will "updateFastDrawingColorArray" at
	 *            the next time step.
	 */
	public void setViewChanged(boolean viewChanged)
	{
		this.viewChanged = viewChanged;
	}

	/**
	 * Instantiates everything needed for a simulation. In particular, sets up
	 * the lattice, graphics, rule, etcetera for the cellular automaton.
	 */
	public void setupCA()
	{
		// Indicates that this method is currently running. Prevents errors that
		// might be cause if other classes try to interpret events that are
		// inadvertently fired during this method (for example, see the
		// InitialStatesPanel.getNumStates() method).
		currentlyRunningSetup = true;

		// the CA properties
		CurrentProperties properties = CurrentProperties.getInstance();

		// stop the CA (should have happen already, but just to be safe...)
		stopCA();

		// Get a factory for instantiating the CA rule, lattice, graphics, and
		// data file storage. Must be re-instantiated every time the setup is
		// run, because the factory will change depending on the properties.
		factory = new CAFactory();

		// get the CA lattice (i.e., square, hexagonal, etc.)
		Lattice tempLattice = null;
		try
		{
			tempLattice = factory.getLattice();

			// if the factory couldn't get a lattice, will return null
			if(tempLattice != null)
			{
				lattice = tempLattice;

				// the tempLattice can be a resource hog, so clean up now!
				tempLattice = null;
				System.gc();

				// the amount of delay between steps of the simulation
				timeDelay = properties.getTimeDelay();

				// get the CA rule (i.e., game of life, lattice gas, etc.)
				rule = factory.getRule();

				// get a class for saving the data in a file
				data = factory.getDataStorage();

				// Create graphics for displaying the CA. If graphics exist,
				// then just update the CA graphics panel. This code is only
				// called the first time, when the CA is starting up.
				if(graphics == null)
				{
					// create the whole JFrame (in an event thread). This is
					// important so that all of the graphics happen on the
					// event dispatch thread.
					Runnable createGraphics = new Runnable()
					{
						public void run()
						{
							graphics = new CAFrame(lattice, lattice.getView());
						}
					};
					try
					{
						SwingUtilities.invokeAndWait(createGraphics);
					}
					catch(Exception e)
					{
					}

					// get a listener for events from the GUI
					listener = new CAControllerListener(this);

					// register the CAControllerListener as a listener to
					// the menu bar
					graphics.getMenuBar().addPropertyChangeListener(listener);

					// add the CAControllerListener as a listener to the
					// rule. Ditto the MenuBar and ToolBar (in case they
					// need to update something based on the rule's state --
					// primarily a firing of the SETUP state). Ditto the
					// AllPanelListener in case needs to update the number of
					// states or something that I specify later.
					rule.addPropertyChangeListener(listener);
					rule.addPropertyChangeListener(graphics.getMenuBar());
					rule.addPropertyChangeListener(graphics.getToolBar());
					rule.addPropertyChangeListener(graphics.getControlPanel()
							.getAllPanelListener());

					// register the rule as a listener to the menu, tool
					// bar, property panel, start panel, initial state panel, CA
					// controller listener, etc.
					graphics.getMenuBar().addPropertyChangeListener(rule);
					graphics.getToolBar().addPropertyChangeListener(rule);
					graphics.getControlPanel().getAllPanelListener()
							.addPropertyChangeListener(rule);
					listener.addPropertyChangeListener(rule);

					// register the AllPanelListener class as a listener to
					// the menu bar
					graphics.getMenuBar().addPropertyChangeListener(
							graphics.getControlPanel().getAllPanelListener());

					// register the menu bar as a listener to the
					// AllPanelListener class (necessary so that "Save" menu
					// item can be updated whenever there is a new
					// generation).
					// Ditto the tool bar.
					listener.addPropertyChangeListener(graphics.getMenuBar());
					listener.addPropertyChangeListener(graphics.getToolBar());

					// register the CAControllerListener class as a listener
					// to the AllPanelListener. Ditto the menu and tool bars.
					graphics.getControlPanel().getAllPanelListener()
							.addPropertyChangeListener(listener);
					graphics.getControlPanel().getAllPanelListener()
							.addPropertyChangeListener(graphics.getMenuBar());
					graphics.getControlPanel().getAllPanelListener()
							.addPropertyChangeListener(graphics.getToolBar());

					// And, register the controlPanel as a listener to the
					// AllPanelListener class! That way we can send messages
					// back to the control panel (e.g., to update the
					// statusPanel when something happens here -- which gets
					// passed to the AllPanelListener).
					listener.addPropertyChangeListener(graphics
							.getControlPanel().getAllPanelListener());

					// make sure that the properties panel is displaying the
					// correct rule number and state. When the CA first
					// starts up, the selected rule may specify a rule number
					// and state value. These values may be different from the
					// ones in the properties file. However, we want to reopen
					// with the same state and rule number, so we will override
					// the rule's request and use the value in the properties
					// file. If the value is in the property file then it is a
					// permissable value.
					if(!graphics.getControlPanel().getPropertiesPanel()
							.getNumStatesField().getText().equals(
									"" + numStatesAtStartUp))
					{
						// Do this first because setting the text below
						// fires events that read the properties.
						properties.setNumStates(numStatesAtStartUp);

						graphics.getControlPanel().getPropertiesPanel()
								.getNumStatesField().setText(
										"" + numStatesAtStartUp);
						graphics.getControlPanel().getStatusPanel()
								.setCurrentNumberOfStatesLabel(
										numStatesAtStartUp);
					}
					if(!graphics.getControlPanel().getRulePanel()
							.getRuleNumberTextField().getText().equals(
									ruleNumberAtStartUp.toString()))
					{
						// do this first because setting the text below
						// fires events that read the properties
						properties.setRuleNumber(ruleNumberAtStartUp);

						graphics.getControlPanel().getRulePanel()
								.getRuleNumberTextField().setText(
										ruleNumberAtStartUp.toString());

						graphics.getControlPanel().getStatusPanel()
								.setCurrentRuleLabel(ruleNumberAtStartUp);
					}
				}
				else
				{
					// reset the zoom feature (in case zoom-in is disabled)
					graphics.getMenuBar().enableZoomIn(true);

					// make the new rule a listener to the menu bar, etc so
					// gets notified of color changes, etc. This is
					// especially important when loading simulations (for
					// example, from EZ mode). Without this, the Obesity
					// rule, for example, won't always update its colors
					// on the additional properties panel.
					graphics.getMenuBar().addPropertyChangeListener(rule);
					graphics.getMenuBar().addPropertyChangeListener(rule);
					graphics.getToolBar().addPropertyChangeListener(rule);
					graphics.getControlPanel().getAllPanelListener()
							.addPropertyChangeListener(rule);
					listener.addPropertyChangeListener(rule);

					// Reset colors used to draw cells on the lattice.
					// Prevents an error where the colors from an old rule
					// might correspond to states that no longer exists in the
					// new rule.
					ColorScheme.resetDrawingColors();

					updateGraphicsPanel();
				}

				// register a mouse drawing listener on the latticePanel.
				// Has to be done after the CAFrame creates the
				// latticePanel; and is outside the if-else above,
				// so happens in either case.
				mouseDrawingListener = new DrawColorListener(lattice.getView());
				lattice.getView().addLatticeMouseListener(mouseDrawingListener);

				// register a position listener on the latticePanel.
				// Has to be done after the CAFrame creates the
				// statusPanel; and is outside the if-else above,
				// so happens in either case.
				lattice.getView().addLatticeMouseListener(
						new PositionListener(lattice.getView(), graphics
								.getControlPanel().getStatusPanel()));

				// register a cell value listener on the latticePanel.
				// Has to be done after the CAFrame creates the
				// statusPanel; and is outside the if-else above,
				// so happens in either case.
				cellValueListener = new CellValueListener(lattice.getView(),
						graphics.getControlPanel().getStatusPanel());
				lattice.getView().addLatticeMouseListener(cellValueListener);

				// register an analysis listener on the latticePanel.
				// Has to be done after the CAFrame creates the
				// latticePanel; and is outside the if-else above,
				// so happens in either case.
				analysisDrawingListener = new AnalysisDrawingListener(lattice
						.getView());
				lattice.getView().addLatticeMouseListener(
						analysisDrawingListener);

				// add any open analyses to the listener (as observers)
				if((analysisList != null) && (analysisList.size() > 0))
				{
					Iterator<Analysis> iterator = analysisList.iterator();
					while(iterator.hasNext())
					{
						Analysis analysis = iterator.next();
						analysisDrawingListener.addObserver(analysis);
					}
				}

				// reset the value of the last completed generation.
				// This will normally be 0.
				lastCompleteGeneration = ((Cell) lattice.iterator().next())
						.getGeneration();

				// make sure the status panel is updated to show correct
				// generation
				String message = "" + lastCompleteGeneration;
				listener.firePropertyChangeEvent(new PropertyChangeEvent(this,
						StatusPanel.GENERATION, null, message));

				// make sure the menu and tool bars are updated to show
				// something has changed so it can be saved.
				listener.firePropertyChangeEvent(new PropertyChangeEvent(this,
						Cell.UNSAVED_DATA, null, CurrentProperties.TRUE));

				// the CellStateView may request that the color schemes be
				// disabled in the menu.
				boolean enableColorSchemes = Cell.getView()
						.enableColorSchemes();
				graphics.getMenuBar().enableColorSchemes(enableColorSchemes);

				// make sure all graphics changes are shown
				graphics.update();

				// decide how often to update the graphics
				update_graphics_at_end = properties.isUpdateAtEnd();
				update_graphics_every_n_steps = properties.getDisplayStep();

				// find out how many parallel processors the user wants. Make
				// sure it isn't too many (wouldn't that be a nice problem!).
				this.numberOfAvailableProcessors = CurrentProperties
						.getInstance().getNumberOfProcessors();
				int numberOfCells = lattice.getCells().length;
				if(numberOfAvailableProcessors > numberOfCells)
				{
					numberOfAvailableProcessors = numberOfCells;
				}
				this.userSelectedNumberOfProcessors = numberOfAvailableProcessors;

				// Create a set of threads for parallel processing. This will
				// only happen if there are enough processors to warrant
				// parallel processing.
				createParallelProcessingWorkers(lattice.getCells());

				// reset any analyses that might be running
				resetAnalyses();
			}
			else
			{
				String warning = "Unable to create the requested lattice.  Please \n"
						+ "try again, or try a different lattice.";
				if(graphics != null)
				{
					JOptionPane.showMessageDialog(graphics.getFrame(), warning,
							"Unable to build lattice",
							JOptionPane.INFORMATION_MESSAGE);
				}
				else
				{
					JOptionPane.showMessageDialog(null, warning,
							"Unable to build lattice",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}
		catch(OutOfMemoryError e)
		{
			// keep track of how many times this failed
			numberOfAttempts++;

			// something (like the lattice) is a resource hog, so clean up
			// now!
			System.gc();

			// tell them it failed
			if(numberOfAttempts == 1)
			{
				String warning = e.getMessage();
				if(warning.toLowerCase().contains("heap"))
				{
					warning = "Insufficient memory.  See the help \n"
							+ "page on increasing memory.";
				}
				if(graphics != null)
				{
					JOptionPane.showMessageDialog(graphics.getFrame(), warning,
							"Unable to build lattice",
							JOptionPane.INFORMATION_MESSAGE);
				}
				else
				{
					JOptionPane.showMessageDialog(null, warning,
							"Unable to build lattice",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}

			// try again (will only try twice)
			tryAgainAfterFailure(numberOfAttempts);

			// reset -- we tried all we could, or we succeeded
			numberOfAttempts = 0;
		}
		catch(Exception e)
		{
			// something went wrong.
			String warning = "Unable to create the requested CA.  Please \n"
					+ "try again, try a different lattice, or restart.\n"
					+ "The properties file may be corrupted.  Try \n"
					+ "deleting ca_properties.txt in the program \n"
					+ "folder.  It will be automatically recreated.";
			if(graphics != null)
			{
				JOptionPane.showMessageDialog(graphics.getFrame(), warning,
						"Unable to build CA", JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				JOptionPane.showMessageDialog(null, warning,
						"Unable to build CA", JOptionPane.INFORMATION_MESSAGE);

				// fatal error
				System.exit(0);
			}
		}

		// Indicates that this method is no longer running. Prevents errors that
		// might be cause if other classes try to interpret events that are
		// inadvertently fired during this method (for example, see the
		// InitialStatesPanel.getNumStates() method).
		currentlyRunningSetup = false;
	}

	/**
	 * Starts up the specified analysis (actually adds it to a list of analyses
	 * that are run every time the CA increments to a new generation).
	 * 
	 * @param analysis
	 *            The Analysis that will be started.
	 */
	public void startAnalysis(Analysis analysis)
	{
		analysisList.add(analysis);

		// suspend the regular mouse drawing behavior if requested by the
		// analysis. And add the analysis to a list of analyses that suspend the
		// drawing.
		boolean suspend = analysis.shouldSuspendMouseDrawing();
		if(suspend)
		{
			lattice.getView().removeLatticeMouseListener(
					this.mouseDrawingListener);
			suspendDrawingList.add(analysis);
		}

		// add the analysis as an observer (listener) to the
		// AnalysisDrawingListener class. Used to interpret any drawing done by
		// the mouse.
		analysisDrawingListener.addObserver(analysis);

		// register the analysis as a listener to these various classes.
		// We want these various classes to notify the analysis of relevant
		// events, like changes in the view colors.
		listener.addPropertyChangeListener(analysis);
		graphics.getMenuBar().addPropertyChangeListener(analysis);
		graphics.getToolBar().addPropertyChangeListener(analysis);
		graphics.getControlPanel().getAllPanelListener()
				.addPropertyChangeListener(analysis);

		// and make the CAControllerListener a listener to the analysis.
		// Particularly useful when the analysis is closing and it needs to
		// notify the CAControllerListener to pause the simulation.
		analysis.addPropertyChangeListener(listener);

		// run it once immediately so that it analyzes the current generation
		analysis.analyzeData(lattice, rule, lastCompleteGeneration);

		// so shows any changes made by an analysis
		// viewChanged = true;
	}

	// private long totalTime = 0;

	/**
	 * Steps the CA through the specified number of generations, or until the
	 * stop button is pressed (setting startButtonPressed to false).
	 */
	private void startCA()
	{
		// notify other classes that we have started the simulation
		listener.firePropertyChangeEvent(new PropertyChangeEvent(this,
				CurrentProperties.START, null, new Boolean(true)));

		// gets the CA into the loop below. Once in the loop, this variable may
		// be reset elsewhere (in particular, by the stopCA() method).
		startButtonPressed = true;

		// if the CA was paused before, it isn't now
		paused = false;

		// current number of iterations executed
		int steps = 0;

		// Loop until reach the maximum number of steps, or while the stop
		// button has not been pushed. Not an infinite loop because the
		// startButtonPressed variable may be changed elsewhere (in particular,
		// by the stopCA() method).
		while(startButtonPressed)
		{
			// lets the setup method know that this is running.
			running = true;

			// quit if have done the number of steps requested
			if(steps < maxSteps)
			{
				// we are doing another iteration
				steps++;

				// long startTime = System.nanoTime();

				// calculate values for the new generation
				lastCompleteGeneration = incrementCA();

				// UNCOMMENT TO BENCHMARK THE CODE (ALSO THE VARIABLE DEFINED
				// ABOVE THIS METHOD)
				//
				// long endTime = System.nanoTime();
				// long elapsedTime = endTime - startTime;
				// System.out
				// .println("CAController: timeElapsed in ms for generation "
				// + lastCompleteGeneration + " = "
				// + (long) (elapsedTime / (double) 1000000));
				// totalTime += elapsedTime;
				// System.out.println("CAController: avg time at generation "
				// + lastCompleteGeneration + " = "
				// + (long) ((totalTime / (double) 1000000) / (double) steps));

				// do any necessary analyses
				analyzeData(lastCompleteGeneration);

				// only update graphics each step if property file says to.
				if(!update_graphics_at_end
						&& (lastCompleteGeneration
								% update_graphics_every_n_steps == 0))
				{
					// colors may have changed -- for example an analysis
					// may have tagged some cells
					if(viewChanged)
					{
						updateFastDrawingColorArray();
					}

					// be sure that the status panel receives the most
					// recent cell value from the listener
					cellValueListener.updateValue();

					// update all the graphics (control panel and the CA
					// graphics panel).
					graphics.update();

					// delay in milliseconds (may be updated by the
					// propertyChange() method)
					delay(timeDelay);
				}
			}
			else
			{
				// stop everything -- this forces the while loop to exit
				startButtonPressed = false;

				// fire off a message to the control panel (and any other
				// listeners) letting it know we stopped. This has to happen
				// before the status update because this causes the status panel
				// to display "Stopped." rather than "Paused".
				listener.firePropertyChangeEvent(new PropertyChangeEvent(this,
						CurrentProperties.START, null, new Boolean(false)));

				// fire off a message to the control panel (and any other
				// listeners) with an update for the status panel
				String plural = "generations";
				if(maxSteps == 1)
				{
					plural = "generation";
				}
				String message = "Paused after " + maxSteps + " " + plural
						+ ", as requested.";
				listener.firePropertyChangeEvent(new PropertyChangeEvent(this,
						CurrentProperties.STATUS, null, message));
			}

			// some simulations are so fast (like rule 90 on a 50 by 50
			// lattice) that the simulation can take all the resources
			// inadvertently. This ugly code forces the simulation to take a
			// few millisecond breather and lets the rest of the application
			// catch up (to display the stop button, for example).
			// Basically, this lets assorted listeners "get a chance" to do
			// their thing by making this thread sleep for a few
			// milliseconds.
			if(steps % 10000 == 1)
			{
				// Thread.yield();
				delay(10);
			}

			// some delay is always necessary to give other threads a chance
			// to do their thing. But only do this while graphics are running.
			// if(!update_graphics_at_end)
			// {
			// delay(3);
			// }
		}

		// lets the setup method know that this is no longer running.
		running = false;

		// update the graphics at the end. Will draw the whole lattice if
		// update_graphics_each_step is false. Don't update if have previously
		// updated.
		if(update_graphics_at_end)
		{
			// colors may have changed -- for example an analysis may have
			// tagged some cells
			if(viewChanged)
			{
				updateFastDrawingColorArray();
			}

			// be sure that the status panel receives the most recent cell
			// value from the listener. (Note this must happen after
			// "running =
			// false" has been set.
			cellValueListener.updateValue();

			// update all the graphics
			graphics.update(update_graphics_at_end);
		}
	}

	/**
	 * Starts the CA in a separate thread. Use this when a call to startCA needs
	 * to return immediately so that some other method can return (without
	 * getting hung indefinitely in startCA().
	 */
	public void startCAInThread()
	{
		final SwingWorker caWorker = new SwingWorker()
		{
			public Object construct()
			{
				// keep going until stop button pressed
				startCA();

				return null;
			}
		};
		caWorker.start();
	}

	/**
	 * Stops all of the analyses. Called when the program is exiting.
	 */
	public static void stopAllAnalyses()
	{
		// remove all analyses from the mouse listener
		analysisDrawingListener.deleteObservers();

		// create a copy of the analysis iterator (to prevent concurrency issues
		// -- as we delete the analyses here, they will also be removed from the
		// original iterator)
		ArrayList<Analysis> analyses = new ArrayList<Analysis>();
		Iterator<Analysis> analysisIterator = analysisList.iterator();
		while(analysisIterator.hasNext())
		{
			analyses.add((Analysis) analysisIterator.next());
		}

		Iterator<Analysis> iterator = analyses.iterator();
		while((iterator != null) && iterator.hasNext())
		{
			Analysis analysisFromList = iterator.next();

			// find out to where the analysis frames should shrink
			Point finalClosingPoint = null;
			if(graphics != null && graphics.getFrame().isShowing())
			{
				Point point = graphics.getFrame().getLocationOnScreen();
				finalClosingPoint = new Point(point.x
						+ (int) (graphics.getFrame().getWidth() / 2.0), point.y
						+ (int) (graphics.getFrame().getHeight() / 2.0));
			}

			analysisFromList.stop(finalClosingPoint);
		}
	}

	/**
	 * Stops the specified analysis (actually removes it from a list of analyses
	 * that are run every time the CA increments to a new generation).
	 * 
	 * @param analysis
	 *            The Analysis that will be stopped.
	 */
	public void stopAnalysis(Analysis analysis)
	{
		// remove the analysis from the mouse listener
		analysisDrawingListener.deleteObserver(analysis);

		// the actual object that we will stop
		Analysis stopThisAnalysis = null;

		// find that object
		Iterator<Analysis> iterator = analysisList.iterator();
		while(iterator.hasNext())
		{
			Analysis analysisFromList = iterator.next();
			if(analysisFromList.getClass().equals(analysis.getClass()))
			{
				stopThisAnalysis = analysisFromList;
			}
		}

		// remove that object
		if(stopThisAnalysis != null)
		{
			analysisList.remove(stopThisAnalysis);
		}

		// and if the analysis had requested a suspension of normal drawing
		// behavior, then remove the analysis from the suspension list as well.
		if(suspendDrawingList != null
				&& suspendDrawingList.contains(stopThisAnalysis))
		{
			suspendDrawingList.remove(stopThisAnalysis);

			// now restart normal drawing behavior if the list is suddenly
			// empty
			if(suspendDrawingList.size() == 0)
			{
				lattice.getView().addLatticeMouseListener(
						this.mouseDrawingListener);
			}
		}

		// so shows any changes made by stopping an analysis
		viewChanged = true;
	}

	/**
	 * Stops the simulation.
	 */
	public void stopCA()
	{
		// stop any call to the delay() method (otherwise have to wait for timer
		// to expire)
		exitDelay = true;

		// stop the CA
		startButtonPressed = false;

		// waits to be sure that the CA is no longer running. If it is
		// running and a setup occurs, then the lattice may be null while
		// the program tries to run. A Timer would also
		// solve this, but I haven't bothered yet.
		while(running)
		{
			// not an infinite loop -- the running variable is set to false
			// when the startCA() method is stopped.
		}

		// if the CA was paused before, it isn't now. Now it is stopped.
		paused = false;

		// notify other classes that the CA has stopped
		if(listener != null)
		{
			listener.firePropertyChangeEvent(new PropertyChangeEvent(this,
					CurrentProperties.START, null, new Boolean(false)));
		}
	}

	/**
	 * Restarts the CA, but only if the CA was running when pauseCA() was
	 * called. If the CA was not paused, or if the CA was stopped or started
	 * after it was paused, then this method does nothing.
	 * 
	 * @see #pauseCA
	 */
	public void unPauseCA()
	{
		if(paused)
		{
			startCAInThread();
			paused = false;
		}
	}

	/**
	 * Update only the graphics panel -- but note that this means also setting
	 * the lattice and properties to make sure that CAGraphics isn't using local
	 * copies.
	 */
	public void updateGraphicsPanel()
	{
		// Just assign a new CA graphics panel. Also assign the new lattice,
		// and new properties to make sure that local copies of these aren't
		// being stored. (And don't re-register the listener to the menu
		// bar)
		LatticeView panel = factory.getGraphicsPanel();

		// And redo the color array (used for fast graphics). It needs changing
		// after the initial states are set and the graphics panel has changed.
		updateFastDrawingColorArray();

		graphics.setGraphicsPanel(panel);
		graphics.setLattice(lattice);
		graphics.resize();
	}

	/**
	 * This thread updates a subset of all the cells. The constructor dictates
	 * the range of cells that are updated by this thread. A collection of many
	 * of these threads are used to update all of the cells simultaneously.
	 * 
	 * @author David Bahr
	 */
	private class ParallelProcessingWorker implements Runnable
	{
		// the CA cells being updated
		private Cell[] cells = null;

		// the starting point of this partition (in the array of cells)
		private int from = 0;

		// the ending point (exclusive) of this partition (in the array of
		// cells)
		private int to = 0;

		/**
		 * A thread that updates a subset of the cells beginning at "from"
		 * (inclusive) and ending at "to" (exclusive).
		 * 
		 * @param cells
		 *            The CA cells being updated.
		 * @param from
		 *            The starting point (inclusive) of this partition (in the
		 *            array of cells).
		 * @param to
		 *            The ending point (exclusive) of this partition (in the
		 *            array of cells).
		 */
		public ParallelProcessingWorker(Cell[] cells, int from, int to)
		{
			this.cells = cells;
			this.from = from;
			this.to = to;
		}

		/**
		 * Runs the thread to update the cells in this partition.
		 */
		public void run()
		{
			// update the cells between "from" (inclusive) and "to"
			// (exclusive).
			for(int i = from; i < to; i++)
			{
				// Update the cell. The update usually depends on the
				// neighbors, but it may feel free to ignore the value of the
				// neighbors. Note that the neighbors does *not* include the
				// cell itself.
				cells[i].updateState(lattice.getNeighbors(cells[i]));

				// Now save the state's color data in a 1d array for faster
				// graphics. The variable i has to be converted into a row and
				// column. Note that I want integer division to get the row.
				// For one-d arrays, the row will always be 0.
				lattice.getView().setColorPixel(
						i,
						Cell.getView().getDisplayColor(cells[i].getState(),
								null, cells[i].getCoordinate()).getRGB());
			}

			// let the CountDownLatch know that this thread has finished
			// it's task. The program will not continue until all threads have
			// finished and called countDown(). The endGate knows how many
			// threads there are and will only open the gate (latch) when
			// the count down reaches zero.
			END_GATE.countDown();
		}
	}

	/**
	 * Sets the number of processors that will be used for parallel processing.
	 * Once this is reset, if it does not equal the numberOfAvailableProcessors,
	 * then the code will create a new set of threads with this number.
	 * 
	 * @param numberOfProcessors
	 *            The number of processors selected by the user that will be
	 *            used for parallel processing.
	 */
	public void setUserSelectedNumberOfProcessors(int numberOfProcessors)
	{
		// make sure we don't have more processors than we have cells (wouldn't
		// that be nice!). Note that changing the value here does not affect
		// what is stored in the properties. It can still be a larger number.
		// That way, every time a new lattice is created (see setUpCA()), then
		// the userSelectedNumberOfProcessors will once again attempt to be as
		// large as possible.
		int numberOfCells = lattice.getCells().length;
		if(numberOfProcessors > numberOfCells)
		{
			numberOfProcessors = numberOfCells;
		}

		this.userSelectedNumberOfProcessors = numberOfProcessors;
	}
}