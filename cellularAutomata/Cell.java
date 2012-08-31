/*
 Cell -- a class within the Cellular Automaton Explorer. 
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

import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.CellStateFactory;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.DefaultCellStateView;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.dataStructures.FiniteArrayList;

/**
 * Contains all of the information about the state of a cell, previous states of
 * the cell, rule(s) for changing state, and the current generation or time step
 * (number of updates that have occurred). The states are typically 0 or 1, but
 * they are stored in an object of type CellState. The rule(s) are typically
 * addition modulo 2 of nearest neighbors, but the rule is stored in an object
 * of type Rule.
 * <p>
 * Note that the cell does not keep track of its neighbors. The cellular
 * automaton's lattice keeps track of the cell's neighbors. A cellular automaton
 * lattice is a topology (or data structure) of connected cells.
 * <p>
 * IMPORTANT: most accessors and mutators in this class must be synchronized to
 * be thread safe (or the instance variables must be volatile). For example,
 * addNewState() updates the state and then the generation; between those two
 * updates, a different thread might access the new state with the old
 * generation. This can't happen if the method is synchronized. As importantly,
 * one thread can't try to getGeneration() at the same time that another thread
 * is updating the generation in addNewState(). This is because all the methods
 * are under the same synchronization lock ("this" object), and only one thread
 * is allowed to own a lock at any time.
 * <p>
 * Note that the state of the cell is not thread safe once it is returned by an
 * accessor. Threads that access the state must not alter the state, or if they
 * do, they must be synchronized (with this cell object as the lock).
 * 
 * @author David Bahr
 */
public final class Cell
{
	/**
	 * The number of states stored by the cell as part of the state history. Set
	 * by the constructor.
	 */
	public static int statesStored = 2;

	/**
	 * The maximum number of saved generations.
	 */
	public final static int MAX_HISTORY = 1000;

	/**
	 * The minimum number of saved generations.
	 */
	public final static int MIN_HISTORY = 2;

	/**
	 * String used when firing property change events to indicate that there is
	 * reset data.
	 */
	public final static String RESET_DATA = "reset_data";

	/**
	 * String used when firing property change events to indicate that there is
	 * unsaved data.
	 */
	public final static String UNSAVED_DATA = "unsaved_data";

	/**
	 * String used when firing property change events to indicate that there is
	 * a change in tagged or untagged status.
	 */
	public final static String TAGGED_EVENT = "cell tagged";

	// The history of states of the cell (includes the current state).
	private FiniteArrayList<CellState> stateHistory = null;

	// The maximum number of previous states that will be stored (includes the
	// current state). Must be at least 2. Making this static saves 4 bytes per
	// instance.
	private static int maxNumStates = 2;

	// The minimum number of generations that the CA will be able to go back.
	// This value is reassigned in the constructor. Making this static saves 4
	// bytes per instance.
	private static int numBackStates = 5;

	// the current generation (number of updates that have occurred)
	private int generation = 0;

	// the row, and column position of the cell
	private Coordinate coordinate = null;

	// The current cell state (also stored in the stateHistory arrayList, but
	// put here for faster access). Keeping this separate from the stateHistory
	// makes the code MUCH, MUCH faster. ArrayList get operations are expensive.
	private CellState currentState = null;

	// The previous cell state (also stored in the stateHistory arrayList, but
	// put here for faster access). Keeping this separate from the stateHistory
	// makes the code MUCH, MUCH faster. ArrayList get operations are expensive.
	private CellState previousState = null;

	// REMOVED because profiler says this is very expensive.
	// allows us to add PropertyChangeListeners to this class
	// private EventListenerList listenerList = new EventListenerList();

	// Any additional information that needs to be stored by the cell, separate
	// from the state.
	private Hashtable<Object, Object> otherCellInformation = null;

	// The rule for updating state, usually the same rule for all cells. Making
	// this static saves 4 bytes per instance (on a 32 bit OS) and 8 bytes per
	// instance (on a 64 bit OS).
	private static Rule rule;

	// The graphics (view) used to display the cell state. Making this static
	// saves 4 bytes per instance on a 32 bit OS and saves 8 bytes per instance
	// on a 64 bit OS.
	private static CellStateView view = new DefaultCellStateView();

	/**
	 * Create a cell with rule for changing state. The initial states will be
	 * created as needed.
	 * 
	 * @param rule
	 *            The rule for updating state, usually dependent on the
	 *            neighbors' state.
	 * @param maxNumStates
	 *            The maximum number of states (including the current state)
	 *            that will be remembered by the cell. Must be greater than 1,
	 *            because it will keep track of at least its current state and
	 *            one previous state.
	 * @param coordinate
	 *            The position (row and column) of the cell. In one-dimension,
	 *            the row is always 0.
	 */
	public Cell(Rule rule, int maxNumStates, Coordinate coordinate)
	{
		this(null, null, rule, maxNumStates, coordinate);
	}

	/**
	 * Create a cell with a specific initial state and a rule for changing
	 * state. The cell will only keep track of the current state and the
	 * previous state.
	 * 
	 * @param state
	 *            The initial state of the cell (for example, 0 or 1).
	 * @param view
	 *            The view used to display the cell.
	 * @param rule
	 *            The rule for updating state, usually dependent on the
	 *            neighbors' state.
	 * @param coordinate
	 *            The position (row and column) of the cell. In one-dimension,
	 *            the row is always 0.
	 */
	public Cell(CellState state, CellStateView view, Rule rule,
			Coordinate coordinate)
	{
		// 2 will keep track of this state and one previous state
		this(state, view, rule, 2, coordinate);
	}

	/**
	 * Create a cell with a specific initial state, a rule for changing state,
	 * and the ability to keep track of a certain number of previous states.
	 * 
	 * @param state
	 *            The initial state of the cell (for example, a BinaryCellState
	 *            storing a 0 or 1). If null, an appropriate state will be
	 *            constructed.
	 * @param view
	 *            The view used to display the cell.
	 * @param rule
	 *            The rule for updating state, usually dependent on the
	 *            neighbors' state.
	 * @param maxNumStates
	 *            The maximum number of states (including the current state)
	 *            that will be remembered by the cell. Must be greater than 1,
	 *            because it will keep track of at least its current state and
	 *            one previous state.
	 * @param coordinate
	 *            The position (row and column) of the cell. In one-dimension,
	 *            the row is always 0.
	 */
	public Cell(CellState state, CellStateView view, Rule rule,
			int maxNumStates, Coordinate coordinate)
	{
		Cell.rule = rule;

		this.coordinate = coordinate;

		// set the view
		if(view == null)
		{
			// the user did not provide a view through the constructor
			Cell.view = rule.getCompatibleCellStateView();

			// just in case
			if(Cell.view == null)
			{
				String error = "Class: Cell. Method: constructor. The cell "
						+ "must be given a non-null CellStateView.";
				throw new IllegalArgumentException(error);
			}
		}
		else
		{
			Cell.view = view;
		}

		// how many initial states are required
		int numInitialStates = rule.getRequiredNumberOfGenerations();

		// the number of generations that will be averaged together for
		// displaying this cell. This indicates a minimum size for the
		// state history array list.
		int runningAverage = CurrentProperties.getInstance()
				.getRunningAverage();

		// The number of states that the cell will store. Choose the biggest of
		// the maxNumStates passed into this method, the running average, and
		// the numInitialStates.
		Cell.maxNumStates = Math.max(runningAverage, maxNumStates);
		Cell.maxNumStates = Math.max(numInitialStates, Cell.maxNumStates);
		if(Cell.maxNumStates < 2)
		{
			Cell.maxNumStates = 2;
			String warning = "Class: Cell. Constructor. The required number of "
					+ "generations \n must be greater than 1, not "
					+ maxNumStates
					+ ".  \n\n  The number of stored states has been set to 2.";
			JOptionPane.showMessageDialog(null, warning, "Developer Warning",
					JOptionPane.WARNING_MESSAGE);
		}

		// how many steps we want the CA to be able to go back
		numBackStates = CurrentProperties.getInstance().getRewindSteps();

		// Add in the number of generations that the CA will be able to go back.
		// These states are in addition to the number of states required for
		// running averages, initial conditions, etc.
		Cell.maxNumStates += numBackStates;

		// might be useful elsewhere to know how many states are being stored by
		// this cell.
		statesStored = Cell.maxNumStates;

		// create a list of the correct size to hold the cell's previous
		// states. this will be an array of the specified size, and will never
		// get bigger. Any time a new element is added that makes it exceed the
		// capacity shown here, then the first element is automatically erased
		// to make room for the new element.
		stateHistory = new FiniteArrayList<CellState>(Cell.maxNumStates);

		// create the initial states
		for(int i = 0; i < numInitialStates; i++)
		{
			this.stateHistory.add(CellStateFactory.createNewCellState(rule));
		}

		// set the current state
		if(state == null)
		{
			// the user did not provide an initial state through the constructor
			this.currentState = stateHistory.getLast();
		}
		else
		{
			// the user did provide an initial state through the constructor
			this.currentState = state;
			this.stateHistory.setLast(state);
		}

		// set the previous state (only relevant if there is more than one
		// initial state)
		if(numInitialStates > 1)
		{
			this.previousState = stateHistory.get(numInitialStates - 2);
		}

		// set the current generation. One initial state, means generation = 0.
		// E.g., two initial states means generation = 1.
		this.generation = numInitialStates - 1;
	}

	/**
	 * Check to make sure that state info for the requested generation is held
	 * by the cell.
	 * 
	 * @param generation
	 *            The generation being checked.
	 * @return true if state info is held for that generation, false otherwise.
	 */
	private boolean checkGeneration(int generation)
	{
		if(generation < 0 || (this.generation - generation >= maxNumStates)
				|| (generation > this.generation))
		{
			// failed if got here
			return false;
		}

		// if got here, then state info is held for that generation
		return true;
	}

	/**
	 * Takes a generation and converts it to an array number for the state
	 * history.
	 * 
	 * @param generation
	 *            The generation.
	 * @return A stateHistory array position that gives state for that
	 *         generation.
	 * @throws IllegalArgumentException
	 *             if the cell does not hold information for that generation.
	 */
	private int mapGenerationToArrayNumber(int generation)
			throws IllegalArgumentException
	{
		// make sure the generation is a reasonable value
		// and throw exception if not
		boolean ok = checkGeneration(generation);
		if(!ok)
		{
			throw new IllegalArgumentException(
					"Class: Cell.  Method: mapGenerationToArrayNumber."
							+ "The requested generation (" + generation
							+ ") is not " + "stored by the cell.");
		}

		int arrayNum = stateHistory.size() - (this.generation - generation) - 1;

		return arrayNum;
	}

	/**
	 * Adds a new state to the Cell. The current state of the cell is set to
	 * this state, and the previous state is set to the previously current
	 * state. Also increments the generation.
	 * 
	 * @param state
	 *            The new current state for the cell.
	 */
	public synchronized void addNewState(CellState state)
	{
		// update the previous and current state
		previousState = currentState;
		currentState = state;

		// add the new current state to the state history (this will
		// automatically remove the oldest state if there is no more room in the
		// stateHistory list)
		stateHistory.add(state);

		// tag the current state if anything wants it to be tagged
		LinkedList<Object> taggingObjectList = previousState
				.getAllTaggingObjects();
		if(taggingObjectList != null)
		{
			for(int i = 0; i < taggingObjectList.size(); i++)
			{
				currentState.setTagged(true, taggingObjectList.get(i));
			}
		}

		// Removed the following because profiler says it is expensive.
		// Instead, placed it in CAController.incrementCA() so that I can
		// call it once per generation rather than once per cell.
		// Let the menu know that there is unsaved data
		// firePropertyChangeEvent(new PropertyChangeEvent(this, UNSAVED_DATA,
		// null, PropertyReader.TRUE));

		// update the generation or time step (number of updates that have
		// occurred). This must happen after everything else in this method!
		// Otherwise the cell may get confused about its current generation.
		generation++;
	}

	/**
	 * Get the position (row and column) of the cell. In one dimension, the row
	 * is always 0.
	 * 
	 * @return the position of the cell (row and column).
	 */
	public synchronized Coordinate getCoordinate()
	{
		return coordinate;
	}

	/**
	 * Get the cell's generation (in other words, the time step, or the number
	 * of updates that have occurred).
	 * 
	 * @return Returns the rule.
	 */
	public synchronized int getGeneration()
	{
		return generation;
	}

	/**
	 * Gets any other information that is stored in the cell. For example, the
	 * Margolus neighborhood needs the cell to keep track of its position as the
	 * northwest, northeast, southeast, or southwest corner of the neighborhood.
	 * (This information is not technically part of the cell's state so it is
	 * most appropriately stored in this location, separate from the state.)
	 * <p>
	 * In general, this method should be unnecessary. All information about the
	 * cell should be stored in the state. The Margolus neighborhood is an
	 * exception because that neighborhood imposes a very unnatural structure
	 * that is external to the lattice and the cell's state.
	 * 
	 * @return Any additional information that is stored by the cell. May be
	 *         null if there is no information.
	 */
	public synchronized Hashtable getOtherCellInformation()
	{
		return otherCellInformation;
	}

	/**
	 * Gets the previous state of this cell (the state at the current generation
	 * minus 1).
	 * 
	 * @return Returns the previous state or null if the previous state does not
	 *         yet exist.
	 */
	public synchronized CellState getPreviousState()
	{
		return previousState;
	}

	/**
	 * Get the rule for updating this cell.
	 * 
	 * @return Returns the rule.
	 */
	public synchronized Rule getRule()
	{
		return rule;
	}

	/**
	 * Get the current state of this cell.
	 * 
	 * @return Returns the state.
	 */
	public synchronized CellState getState()
	{
		return currentState;
	}

	/**
	 * Get the state of this cell for a particular generation (or time step).
	 * Returns <code>null</null> if the cell no longer holds that information.  
	 * The number of generations held is specified by the constructor.
	 * 
	 * @param generation
	 *            The time for which the state will be retrieved.
	 * 
	 * @return Returns the state (<code>null</null> if the cell no longer holds 
	 * 		   information for that generation).
	 */
	public synchronized CellState getState(int generation)
	{
		// the if-statement below helps with speed -- don't usually need to
		// go in there

		// most likely request
		CellState state = currentState;

		// next most likely request
		if(generation == (this.generation - 1))
		{
			state = previousState;
		}
		else if(generation != this.generation)
		{
			// get the state (null if it doesn't exist for that generation
			try
			{
				// map the generation to an array position
				int num = mapGenerationToArrayNumber(generation);

				state = stateHistory.get(num);
			}
			catch(Exception e)
			{
				// there is no state for that generation
				state = null;
			}
		}

		return state;
	}

	/**
	 * Get the history of states of this cell, including the current state. The
	 * list may contain empty elements if the cell has not yet been updated
	 * enough times to fill the list.
	 * 
	 * @return Returns a history of this cell's states with the oldest state
	 *         stored as the first element and the current state stored as the
	 *         last element.
	 */
	public synchronized FiniteArrayList<CellState> getStateHistory()
	{
		return stateHistory;
	}

	/**
	 * Gets the view associated with this cell.
	 * 
	 * @return The view used to display the cell on the screen.
	 */
	public synchronized static CellStateView getView()
	{
		return view;
	}

	/**
	 * True when the cell has been tagged for special purposes such as extra
	 * visibility.
	 * 
	 * @return true if the cell is tagged for special purposes such as extra
	 *         visibility.
	 */
	public synchronized boolean isTagged()
	{
		return currentState.isTagged();
	}

	/**
	 * Set the generation of the cell's current state. Under most circumstances,
	 * this method is unnecessary because the cell keeps track of its current
	 * state.
	 * 
	 * @param generation
	 *            The generation that will be used for the cell's current state.
	 */
	public synchronized void setGeneration(int generation)
	{
		this.generation = generation;
	}

	/**
	 * Sets any other information that needs to be stored by the cell. For
	 * example, the Margolus neighborhood needs the cell to keep track of its
	 * position as the northwest, northeast, southeast, or southwest corner of
	 * the neighborhood. (This information is not technically part of the cell's
	 * state so it is most appropriately stored in this location, separate from
	 * the state.)
	 * <p>
	 * In general, this method should be unnecessary. All information about the
	 * cell should be stored in the state. The Margolus neighborhood is an
	 * exception because that neighborhood imposes a very unnatural structure
	 * that is external to the lattice and the cell's state.
	 * 
	 * @param key
	 *            A key for setting (and retrieving) any additional information
	 *            stored by the cell.
	 * @param value
	 *            The value of any additional information that needs to be
	 *            stored by the cell.
	 */
	public synchronized void setOtherCellInformation(Object key, Object value)
	{
		// normally the associated hashmap is null, so we have to create it
		// here. We specify an initial capacity of 2, because it is unlikely
		// that more than one thing will be stored in the hashmap. (An initial
		// capacity of 1 would immediately rehash.)
		if(otherCellInformation == null)
		{
			otherCellInformation = new Hashtable<Object, Object>(2);
		}

		this.otherCellInformation.put(key, value);
	}

	/**
	 * Specify a rule for updating the cell's state.
	 * 
	 * @param rule
	 *            The rule used to update the cell state.
	 */
	public synchronized void setRule(Rule rule)
	{
		Cell.rule = rule;
	}

	/**
	 * When set to true, the cell will be tagged for special purposes (such as
	 * an extra-visible display, or keeping track of the cell in an analysis).
	 * For example, the CellStateView may opt to display this cell's state in
	 * bright red rather than a normal color. The CellStateView is not obligated
	 * to display tagged states in any special color, but usually will. A unique
	 * tagged color is reserved for the specified taggingObject.
	 * 
	 * @param tagged
	 *            true if the cell should be tagged for special purposes such as
	 *            extra-visible display.
	 * @param taggingObject
	 *            The object that is doing the tagging (usually an analysis).
	 */
	public synchronized void setTagged(boolean tagged, Object taggingObject)
	{
		// tag the state
		currentState.setTagged(tagged, taggingObject);

		// untag all previous states as well
		if(!tagged)
		{
			for(int i = 0; i < stateHistory.size(); i++)
			{
				stateHistory.get(i).setTagged(tagged, taggingObject);
			}
		}

		// REMOVED because profiler says this is very expensive. This can be
		// handled instead by the analyses that are doing the tagging.
		// 
		// let the controller know that it needs to repaint
		// firePropertyChangeEvent(new PropertyChangeEvent(this, TAGGED_EVENT,
		// null, CAPropertyReader.TRUE));
	}

	/**
	 * Removes the current state of this cell and decrements the generation. In
	 * other words, replaces the most recent state with the previous state. This
	 * method is necessary when "rewinding" the CA (for example, with the Back
	 * button).
	 * <p>
	 * If there are not enough states to rewind, nothing happens.
	 */
	public synchronized void removeCurrentState()
	{
		// make sure we can rewind (there needs to be at least the
		// requiredNumberOfGenerations)
		int numberOfRequiredGenerations = rule.getRequiredNumberOfGenerations();
		if(stateHistory.size() > numberOfRequiredGenerations)
		{
			// update the current state
			currentState = previousState;

			// update the previous state
			if(stateHistory.size() >= 3)
			{
				// why minus 3? Minus 1 for the last element. Minus another for
				// the 2nd to last element (the old previous state). And minus 3
				// to get what will now become the new previous state.
				previousState = stateHistory.get(stateHistory.size() - 3);
			}
			else
			{
				previousState = null;
			}

			// remove the last element of the state history
			stateHistory.removeLast();

			// decrement the generation
			generation--;

			// REMOVED because profiler says this is very expensive.
			// let the menu know that there is unsaved data
			// firePropertyChangeEvent(new PropertyChangeEvent(this,
			// UNSAVED_DATA,
			// null, CAPropertyReader.TRUE));
		}
	}

	/**
	 * Reset the previous state of this cell without incrementing the history
	 * and without changing the current state. In other words, replace the
	 * previous state with the one provided.
	 * 
	 * @param state
	 *            The state.
	 */
	public synchronized void resetPreviousState(CellState state)
	{
		// get the tagging objects of the previous state before reassigning it
		LinkedList<Object> taggingObjectList = previousState
				.getAllTaggingObjects();

		// update the previous state (don't change the current state)
		previousState = state;

		// tag it if necessary
		if(this.isTagged())
		{
			// if the cell is tagged then the state should be tagged
			if(taggingObjectList != null)
			{
				for(int i = 0; i < taggingObjectList.size(); i++)
				{
					previousState.setTagged(this.isTagged(), taggingObjectList
							.get(i));
				}
			}
		}

		// update the state history
		if(stateHistory.size() >= 2)
		{
			// replace the second to last element with the new state
			stateHistory.set(stateHistory.size() - 2, previousState);
		}

		// REMOVED because profiler says this is very expensive.
		// let the menu know that there is unsaved data
		// firePropertyChangeEvent(new PropertyChangeEvent(this, UNSAVED_DATA,
		// null, CAPropertyReader.TRUE));
	}

	/**
	 * Reset the current state of this cell without incrementing the history. In
	 * other words, replace the most recent state with the one provided.
	 * 
	 * @param state
	 *            The state.
	 */
	public synchronized void resetState(CellState state)
	{
		// save a temporary copy
		CellState previousCurrentState = currentState;

		// get the tagging objects of the current state before reassigning it
		LinkedList<Object> taggingObjectList = currentState
				.getAllTaggingObjects();

		// update the current state (don't change the previous state)
		currentState = state;

		// tag it if necessary
		if(previousCurrentState.isTagged())
		{
			// if the cell is tagged then the state should be tagged
			if(taggingObjectList != null)
			{
				for(int i = 0; i < taggingObjectList.size(); i++)
				{
					currentState.setTagged(previousCurrentState.isTagged(),
							taggingObjectList.get(i));
				}
			}
		}

		// update the state history
		if(stateHistory.isEmpty())
		{
			// add the new state if there isn't one already
			stateHistory.add(currentState);
		}
		else
		{
			// replace the last element with the new state
			stateHistory.setLast(currentState);
		}

		// let the graphics know to redraw
		// firePropertyChangeEvent(new PropertyChangeEvent(this, RESET_DATA,
		// null, PropertyReader.TRUE));

		// REMOVED because profiler says this is very expensive.
		// let the menu know that there is unsaved data
		// firePropertyChangeEvent(new PropertyChangeEvent(this, UNSAVED_DATA,
		// null, CAPropertyReader.TRUE));
	}

	/**
	 * Similar to removeCurrentState, but does not decrement the generation.
	 * This rolls back the states of the cell without changing the generation.
	 * In other words, every state in the cell's history is moved up; the
	 * previous state becomes the current state, for example.
	 */
	public synchronized void rollBackStates()
	{
		// save a temporary copy
		CellState previousCurrentState = currentState;

		// get the tagging objects of the current state before reassigning it
		LinkedList<Object> taggingObjectList = currentState
				.getAllTaggingObjects();

		// update the current state
		currentState = previousState;

		// update the previous state
		if(stateHistory.size() >= 3)
		{
			// why minus 3? Minus 1 for the last element. Minus another for
			// the 2nd to last element (the old previous state). And minus 3
			// to get what will now become the new previous state.
			previousState = stateHistory.get(stateHistory.size() - 3);
		}
		else
		{
			previousState = null;
		}

		// remove the last element of the state history
		stateHistory.removeLast();

		// tag the new current state if necessary
		if(previousCurrentState.isTagged())
		{
			// if the cell is tagged then the state should be tagged
			if(taggingObjectList != null)
			{
				for(int i = 0; i < taggingObjectList.size(); i++)
				{
					currentState.setTagged(previousCurrentState.isTagged(),
							taggingObjectList.get(i));
				}
			}
		}

		// let the graphics know to redraw
		// firePropertyChangeEvent(new PropertyChangeEvent(this, RESET_DATA,
		// null, PropertyReader.TRUE));

		// REMOVED because profiler says this is very expensive.
		// let the menu know that there is unsaved data
		// firePropertyChangeEvent(new PropertyChangeEvent(this, UNSAVED_DATA,
		// null, CAPropertyReader.TRUE));
	}

	/**
	 * The state of the cell cast to an int. If the state is not naturally
	 * represented as an integer, then the hash value for the cell's associated
	 * CellState object is returned (as specified by toInt in the CellState
	 * API).
	 * 
	 * @see CellState toInt()
	 * @return the value of the cell's state as an int.
	 */
	public synchronized int toInt() // throws NullStateException
	{
		// if(currentState == null)
		// {
		// String message = "Class: Cell. Method:toInt. "
		// + "There is no current state.";
		// throw new NullStateException(message);
		// }
		return currentState.toInt();
	}

	/**
	 * The state of the cell (as an int) at the specified generation. If the
	 * state is not naturally represented as an integer, then the hash value for
	 * the cell's associated CellState object is returned (as specified by toInt
	 * in the CellState API).
	 * 
	 * @see CellState toInt()
	 * @return the value of the cell's state for a particular generation.
	 */
	public final synchronized int toInt(int generation) // throws
	// NullStateException
	{
		// the if-statement below helps with speed -- don't usually need to
		// go in there

		// most likely request
		CellState state = currentState;

		// next most likely request
		if(generation == (this.generation - 1))
		{
			state = previousState;
		}
		else if(generation != this.generation)
		{
			state = this.getState(generation);
			// if(state == null)
			// {
			// String message = "Class: Cell. Method:toInt. "
			// + "There is no state for generation " + generation + ".";
			// throw new NullStateException(message);
			// }
		}

		return state.toInt();
	}

	/**
	 * The state of the cell evaluated as a string.
	 * 
	 * @return the value of the cell's state as a string.
	 */
	public synchronized String toString()
	{
		return this.getState().toString();
	}

	/**
	 * Updates the cell's state based on the rule.
	 * <p>
	 * NOTE: this method is not synchronized because it calls addNewState()
	 * which is synchronized. It is not necessary to synchronize twice. In fact,
	 * if this method is synchronized, the code will deadlock. (One thread will
	 * lock this cell while updating, and at the same time another thread has a
	 * lock on updating the neighbor. Each thread has to read the state of the
	 * other as a neighboring cell. Neither thread can continue until the other
	 * thread finishes updating.) Furthermore, the call to
	 * calculateNewStateForCell() is time consuming and therefore is best left
	 * out of the synchronized block.
	 * 
	 * @param neighbors
	 *            Neighbors of the cell, which are usually required in
	 *            calculating the new state. Neighbors typically include the
	 *            cell itself. May be null if the rule does not need neighbors
	 *            or finds them itself.
	 */
	public void updateState(Cell[] neighbors)
	{
		// calculate the new state and add it to the state history
		CellState state = rule.calculateNewStateForCell(this, neighbors);

		// tag it if necessary
		if(this.isTagged())
		{
			// if the cell is tagged then the new state should be tagged
			LinkedList<Object> taggingObjectList = currentState
					.getAllTaggingObjects();
			if(taggingObjectList != null)
			{
				for(int i = 0; i < taggingObjectList.size(); i++)
				{
					state.setTagged(this.isTagged(), taggingObjectList.get(i));
				}
			}
		}

		addNewState(state);
	}

	// /**
	// * Notify listeners of a property change.
	// *
	// * @param event
	// * Holds the changed property.
	// */
	// public void firePropertyChangeEvent(PropertyChangeEvent event)
	// {
	// EventListener[] listener = listenerList
	// .getListeners(PropertyChangeListener.class);
	// for(int i = 0; i < listener.length; i++)
	// {
	// ((PropertyChangeListener) listener[i]).propertyChange(event);
	// }
	// }
	//
	// /**
	// * Adds a change listener.
	// *
	// * @param listener
	// * the listener to add.
	// */
	// public void addPropertyChangeListener(PropertyChangeListener listener)
	// {
	// listenerList.add(PropertyChangeListener.class, listener);
	// }
	//
	// /**
	// * Removes a change listener.
	// *
	// * @param listener
	// * the listener to remove.
	// */
	// public void removePropertyChangeListener(PropertyChangeListener listener)
	// {
	// listenerList.remove(PropertyChangeListener.class, listener);
	// }
}