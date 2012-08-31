/*
 CellState -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.cellState.model;

import java.util.LinkedList;

/**
 * Holds the state for a cell in a cellular automaton. The state may be a binary
 * number, n-nary number, vectors of binary numbers, colors, or anything else.
 * The state may also hold multiple unrelated values. For example, some lattice
 * gasses need to indicate (1) a vector value, and (2) whether or not the cell
 * is on the interior or boundary of the grid.
 * <p>
 * Also, any necessary instance variables that are set through a constructor
 * must be static. That way, they can be set once by reflection.
 * <p>
 * To save space, the view is static. This means that all child classes should
 * only allow constructors that include the view as a parameter. Otherwise, two
 * different constructors might get called with different views and cause
 * different display results (especially when drawing with the mouse).
 * 
 * @author David Bahr
 */
public abstract class CellState
{
	/**
	 * Indicates that the user has been notified that it cannot import the data
	 * from a file because it is not an integer.
	 */
	public static boolean havePrintedImportDataWarning = false;

	/**
	 * Indicates that the user has been notified that it cannot import the data
	 * from a file because it is out of range (e.g., not within 0 to numStates
	 * -1).
	 */
	public static boolean havePrintedDataOutOfRangeWarning = false;

	// A state for a cell in a cellular automaton.
	private Object state = null;

	// indicates if this state has been tagged for visibility. If so, the
	// CellStateView may opt to display this state in a more visible color.
	private boolean isTagged = false;

	// A list of objects that have tagged this cellState for special display.
	// Usually the cellState is tagged by analyses.
	private LinkedList<Object> taggingObjectList = null;

	/**
	 * Creates a cell state.
	 */
	public CellState()
	{
	}

	/**
	 * Warnings are issued only once per instantiation of a simulation, but this
	 * allows the warnings to be reset.
	 */
	public static void resetWarnings()
	{
		havePrintedImportDataWarning = false;
		havePrintedDataOutOfRangeWarning = false;
	}

	/**
	 * Creates a clone of this cellState; this method must return a different
	 * instance of the cell state, but with all the same values.
	 * <p>
	 * The intent is that, for any CellState x, the expression:
	 * <code> x.clone() !=  x </code> will be true, and that the expression:
	 * <code> x.clone().getClass() == x.getClass() </code> will be true. Also:
	 * <code> x.clone().equals(x) </code> will be true.
	 * <p>
	 * (Note this method is used in places where we need a copy of the cell's
	 * state but that the same instance would cause unpredictable or incorrect
	 * behavior.)
	 * 
	 * @return A unique copy of the cell's state (must not return "this"
	 *         object).
	 */
	public abstract CellState clone();

	/**
	 * Tests if the value of two cell states are equal.
	 * 
	 * @return true if the cell states have the same value.
	 */
	public boolean equals(CellState state)
	{
		return this.getValue().equals(state.getValue());
	}

	/**
	 * Gets the list of all objects that tagged the cell's state (usually a list
	 * of analyses).
	 * 
	 * @return The list of all objects that tagged this cell's state. May be
	 *         null.
	 */
	public LinkedList<Object> getAllTaggingObjects()
	{
		return taggingObjectList;
	}

	/**
	 * Gets the object that tagged the cell's state (usually an analysis or
	 * analyses).
	 * 
	 * @return The object that tagged the cell's state. May be null.
	 */
	public Object getTaggingObject()
	{
		// get the tagging object from the list of tagging objects. May be null
		// if nothing has tagged the cell's state.
		Object taggingObject = null;
		if(taggingObjectList != null)
		{
			taggingObject = taggingObjectList.getFirst();
		}

		return taggingObject;
	}

	/**
	 * Gets the state of the cell.
	 * 
	 * @return Returns the current state of the cell.
	 */
	public Object getValue()
	{
		return state;
	}

	/**
	 * Tests if a given CellState is "alternate". The meaning of "alternate
	 * state" may change for different cellular automatons, so this method must
	 * be implemented by the child class. Typically "alternate" means a cell has
	 * a different value other than "full" or "blank", but binary CellStates may
	 * just assign the "full" or "blank" value to the alternate state.
	 * 
	 * @return true if the state is alternate (typically, other than "full" or
	 *         "blank").
	 * @see cellularAutomata.cellState.model.CellState#setToAlternateState()
	 */
	public abstract boolean isAlternate();

	/**
	 * Tests if a given CellState is "empty" or "blank". The meaning of "empty
	 * state" may change for different cellular automatons, so this method must
	 * be implemented by the child class. Typically "empty" means an unoccupied
	 * site (for example, a 0) versus "full" which means an occupied site (for
	 * example, a 1).
	 * 
	 * @return true if the state is empty.
	 * @see cellularAutomata.cellState.model.CellState#setToEmptyState()
	 */
	public abstract boolean isEmpty();

	/**
	 * Tests if a given CellState is the "drawing" state, in other words the
	 * state that can be drawn on the screen by a user with a left click. Called
	 * by LatticeMouseListener when the user draws a cell with a left click. By
	 * default this tests for the full state. May be overridden in child classes
	 * for different behaviors. For example, in IntegerCellState, the drawing
	 * state is set to a color specified by the user (from a color chooser), so
	 * it tests for that color/state.
	 * 
	 * @return true if the state is the drawing state.
	 */
	public boolean isDrawState()
	{
		// the default behavior
		return isFull();
	}

	/**
	 * Tests if a given CellState is "full". The meaning of "full state" may
	 * change for different cellular automatons, so this method must be
	 * implemented by the child class. Typically "full" means an occupied site
	 * (for example, a 1) versus "empty" which means an unoccupied site (for
	 * example, a 0).
	 * 
	 * @return true if the state is full.
	 * @see cellularAutomata.cellState.model.CellState#setToFullState()
	 */
	public abstract boolean isFull();

	/**
	 * True when the state has been tagged for extra visibility.
	 * 
	 * @return true if the state is tagged for extra visibility.
	 */
	public boolean isTagged()
	{
		return isTagged;
	}

	/**
	 * When set to true, the cell state will be tagged for special purposes
	 * (such as an extra-visible display, or keeping track of the cell in an
	 * analysis). For example, the CellStateView may opt to display this cell's
	 * state in bright red rather than a normal color. The CellStateView is not
	 * obligated to display tagged states in any special color, but usually
	 * will. A unique tagged color is reserved for the specified taggingObject.
	 * 
	 * @param tagged
	 *            true if the cell should be tagged for special purposes such as
	 *            extra-visible display.
	 * @param taggingObject
	 *            The object that is doing the tagging.
	 */
	public void setTagged(boolean tagged, Object taggingObject)
	{
		// now keep a list of objects that have tagged this cell.
		if(tagged)
		{
			// add to the list unless it is already there, in which case, move
			// it to the front of the list

			// create the list if necessary
			if(taggingObjectList == null)
			{
				taggingObjectList = new LinkedList<Object>();
			}
			else
			{
				// the object may not be in the list, but if it is, this removes
				// it
				taggingObjectList.removeFirstOccurrence(taggingObject);
			}

			// and then the object is added to the beginning of the list. This
			// guarantees that the last object to tag the cell is the one that
			// gets selected when displaying the cell in a tagged color
			taggingObjectList.addFirst(taggingObject);
		}
		else
		{
			// the object may not be in the list, but if it is, this removes it
			if(taggingObjectList != null)
			{
				taggingObjectList.removeFirstOccurrence(taggingObject);
			}
		}

		// now set the state as tagged (or untagged)
		if((getAllTaggingObjects() != null)
				&& (getAllTaggingObjects().size() > 0))
		{
			// some object is still tagging this cell's state, so make
			// sure this cell stays tagged
			this.isTagged = true;
		}
		else
		{
			// no object is tagging this cell's state, so untag this cell
			this.isTagged = false;
		}
	}

	/**
	 * Sets an alternate state for this cell state. Useful for creating walls,
	 * obstacles, or other states besides the standard "full" or "blank". If no
	 * special state is desired, then recommended that the implementation call
	 * setFullState().
	 */
	public abstract void setToAlternateState();

	/**
	 * Sets an "empty", "blank", "minimum", or "off" value for this cell state.
	 * This may be a 0 (for binary states), a minimum value integer, a minimum
	 * length vector, or anything else appropriate to this particular class.
	 */
	public abstract void setToEmptyState();

	/**
	 * Sets the state to the one that is specified as a "drawing state". Called
	 * by LatticeMouseListener when the user draws a cell with a left click. By
	 * default is set to the full state. May be overridden in child classes for
	 * different behaviors. For example, in IntegerCellState, the drawing state
	 * is set to a color specified by the user (from a color chooser).
	 */
	public void setToDrawingState()
	{
		// the default behavior
		setToFullState();
	}

	/**
	 * Sets a "full", "maximum", or "on" value for this cell state. This may be
	 * a 1 (for binary states), a maximum value integer, a maximum length
	 * vector, or anything else appropriate to this particular class.
	 */
	public abstract void setToFullState();

	/**
	 * Sets a random value for this cell state. This may be a random bit, a
	 * random length length vector in a random direction, or anything else
	 * appropriate to this particular class.
	 * 
	 * @param probability
	 *            The probability that the cell will be occupied rather than
	 *            blank.
	 */
	public abstract void setToRandomState(double probability);

	/**
	 * Sets a value for this cell state by parsing a string. This may parse a
	 * Bit, an Integer, a vector of numbers or anything else appropriate to this
	 * state. By contract, this method should call setValue() from this class
	 * before exiting.
	 */
	public abstract void setStateFromString(String state);

	/**
	 * Sets a value for this cell state.
	 * 
	 * @param stateValue
	 *            The new state of the cell.
	 */
	public void setValue(Object stateValue)
	{
		state = stateValue;
	}

	/**
	 * The integer value of the state if it exists, and otherwise the hashcode
	 * of the object. It is up to the subclass to decide which is most
	 * appropriate. Most cellular automata have states that represent a finite
	 * alphabet of integers (usually 0 and 1).
	 * 
	 * @return The state as an int.
	 */
	public abstract int toInt();

	/**
	 * The state evaluated as a string.
	 * 
	 * @return the state as a string.
	 */
	public String toString()
	{
		return state.toString();
	}
}
