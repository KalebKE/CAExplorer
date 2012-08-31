/*
 CellStateFactory -- a class within the Cellular Automaton Explorer. 
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

import java.util.HashMap;

import cellularAutomata.rules.Rule;

/**
 * Creates flyweights for the CellState values.
 * <p>
 * Also creates cell states by collecting them from the correct Rule via the
 * Rule method getCompatibleCellState(). Checks for compatibility.
 * 
 * @author David Bahr
 */
public class CellStateFactory
{
	/**
	 * Prevents repeatedly printing the same warning in createNewCellState
	 * method.
	 */
	private static boolean hasPrintedCellStateWarning = false;

	// These are the flyweights, the objects that are used as state values.
	private HashMap cellStateValuePool = null;

	/**
	 * Creates a Flyweight pool for cell states.
	 */
	public CellStateFactory()
	{
		cellStateValuePool = new HashMap();
	}

	/**
	 * Creates a new CellState instance of the type given by the rule.
	 * 
	 * @param rule
	 *            The rule with which the CellState is going to be used.
	 * @return A new instance of a CellState, of a type compatible with the
	 *         rule.
	 */
	public static CellState createNewCellState(Rule rule)
	{
		// create a new cell state
		CellState state = rule.getCompatibleCellState();

		// prints warning if the cellState is not compatible.
		if(!hasPrintedCellStateWarning)
		{
			if(!rule.checkCompatibleCellState())
			{
				hasPrintedCellStateWarning = true;
			}
		}

		return state;
	}

	/**
	 * UNUSED! Gets an Integer value for a cell state based on the supplied
	 * integer. If the given integer has already been used, then this returns
	 * the previously used Integer value. Otherwise, this int is stored in a
	 * "pool" as an Integer so that it can be reused later.
	 * <p>
	 * Note: this is an implementation of the Flyweight design pattern.
	 * 
	 * @param value
	 *            The value that we want to use as a cell state value.
	 * @return An object that can be used as the cell state value.
	 */
	public Integer getCellStateValue(int value)
	{
		// use the string representation of the value as the pool's hashmap key
		Object stateValue = cellStateValuePool.get("" + value);

		// if it's not already in the pool add it, and then make it the value
		// that will be returned
		if(stateValue == null)
		{
			stateValue = new Integer(value);
			cellStateValuePool.put("" + value, stateValue);
		}

		return (Integer) stateValue;
	}
}
