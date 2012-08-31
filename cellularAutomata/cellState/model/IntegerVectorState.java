/*
 IntegerVectorState -- a class within the Cellular Automaton Explorer. 
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

import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import cellularAutomata.CAController;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.graphics.InitialStatesPanel;
import cellularAutomata.util.math.RandomSingleton;

/**
 * A vector state of any length with any integer values, represented as an array
 * of integers.
 * 
 * @author David Bahr
 */
public class IntegerVectorState extends CellState
{
	/**
	 * The default maximum permissible value for a vector state if no other is
	 * specified in a constructor.
	 */
	public static int DEFAULT_MAX_VALUE = 1;

	/**
	 * The default minimum permissible value for a vector state if no other is
	 * specified in a constructor.
	 */
	public static int DEFAULT_MIN_VALUE = 0;

	// random generator
	private static Random random = RandomSingleton.getInstance();

	// the length of the vector
	private static int length = 1;

	// the maximum permissible integer value for this vector state
	private static int maxValue = DEFAULT_MAX_VALUE;

	// the minimum permissible integer value for this vector state
	private static int minValue = DEFAULT_MIN_VALUE;

	/**
	 * Create an integer vector state with a maximum and minimum permissible
	 * value for each position in the vector.
	 * 
	 * @param state
	 *            The vector (as an array).
	 * @param minValue
	 *            The minimum permissible value for each position in the vector.
	 *            Do not recommend using Integer.MIN_VALUE unless your code very
	 *            carefully checks for instances of Infinity.
	 * @param maxValue
	 *            The maximum permissible value for each position in the vector.
	 *            Do not recommend using Integer.MAX_VALUE unless your code very
	 *            carefully checks for instances of Infinity.
	 */
	public IntegerVectorState(int[] state, int minValue, int maxValue)
	{
		if(maxValue > minValue)
		{
			IntegerVectorState.maxValue = maxValue;
			IntegerVectorState.minValue = minValue;
		}
		else
		{
			throw new IllegalArgumentException(
					"Class: IntegerVectorState. Constructor. The maximum "
							+ "permissible value must be greater than the minimum "
							+ "permissible value.");
		}

		// make sure state is ok
		if(checkState(state))
		{
			IntegerVectorState.length = state.length;

			// set state using the method from the super class
			setValue(state);
		}
	}

	/**
	 * Check that the state is not null, not empty, or does not have values
	 * outside the permissible range.
	 * 
	 * @param state
	 *            The state being checked.
	 * @return true if the state is ok.
	 * @throws IllegalArgumentException
	 *             if state is null, empty, or has values outside the
	 *             permissible range.
	 */
	private boolean checkState(int[] state)
	{
		if((state == null) || (state.length < 1))
		{
			throw new IllegalArgumentException(
					"Class: IntegerVectorState. Method: checkState. \n"
							+ "The state must be a non-null and non-empty array.");
		}

		// check to be sure each array position is not outside the permissible
		// bounds.
		for(int i = 0; i < state.length; i++)
		{
			if(state[i] < minValue || state[i] > maxValue)
			{
				throw new IllegalArgumentException(
						"Class: IntegerVectorState. Method: checkNum. According to \n"
								+ "your rule, each element of the state array must be \n"
								+ "an integer between " + minValue + " and "
								+ maxValue + ", but one of the values \n"
								+ "given by your rule is " + state[i] + ".");
			}
		}

		// if get here then is ok
		return true;
	}

	/**
	 * Returns a random integer vector state.
	 * 
	 * @param probability
	 *            The probability that a value is present in each of the
	 *            vector's positions.
	 * @return a random vector state.
	 */
	protected int[] getRandomState(double probability)
	{
		int[] state = new int[length];

		for(int i = 0; i < length; i++)
		{
			if(random.nextDouble() < probability)
			{
				state[i] = random.nextInt(maxValue - minValue + 1) + minValue;
			}
			else
			{
				state[i] = minValue;
			}
		}

		return state;
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
	public CellState clone()
	{
		// note that this clones the array as well! Very important.
		return new IntegerVectorState((int[]) ((int[]) super.getValue())
				.clone(), minValue, maxValue);
	}

	/**
	 * Tests if the value of two cell states are equal.
	 * 
	 * @return true if the cell states have the same value.
	 */
	public boolean equals(CellState state)
	{
		return this.toString().equals(state.toString());
	}

	/**
	 * The maximum permitted value for each component of the vector.
	 * 
	 * @return Returns the maximum permitted value.
	 */
	public int getMaxValue()
	{
		return maxValue;
	}

	/**
	 * The minimum permitted value for each component of the vector.
	 * 
	 * @return Returns the minimum permitted value.
	 */
	public int getMinValue()
	{
		return minValue;
	}

	/**
	 * Tests if a given CellState is "alternate" which in this case is any state
	 * that is not full or blank.
	 * 
	 * @return true if the state is alternate.
	 * @see cellularAutomata.cellState.model.CellState#setToAlternateState()
	 */
	public boolean isAlternate()
	{

		return (!isFull() && !isEmpty());
	}

	/**
	 * Tests if a given CellState is "empty" or "blank". A blank state is one
	 * where every vector index has the minimum possible value.
	 * 
	 * @return true if the state is "empty".
	 * @see cellularAutomata.cellState.model.IntegerVectorState#setToEmptyState()
	 */
	public boolean isEmpty()
	{
		boolean blank = true;
		int[] vector = (int[]) getValue();
		for(int i = 0; i < length; i++)
		{
			if(vector[i] != minValue)
			{
				blank = false;
			}
		}

		return blank;
	}

	/**
	 * Tests if a given CellState is "full". A full state is one where every
	 * vector index has the maximum possible value.
	 * 
	 * @return true if the state is full.
	 * @see cellularAutomata.cellState.model.IntegerVectorState#setToFullState()
	 */
	public boolean isFull()
	{
		boolean full = true;
		int[] vector = (int[]) getValue();
		for(int i = 0; i < length; i++)
		{
			if(vector[i] != maxValue)
			{
				full = false;
			}
		}

		return full;
	}

	/**
	 * Sets an alternate state which is a random vector. (Calls setRandomState
	 * with a probability of 0.5.)
	 */
	public void setToAlternateState()
	{
		setToRandomState(0.5);
	}

	/**
	 * @see cellularAutomata.cellState.model.CellState#setToEmptyState()
	 */
	public void setToEmptyState()
	{
		int[] blankState = new int[length];
		for(int i = 0; i < length; i++)
		{
			blankState[i] = minValue;
		}

		super.setValue(blankState);
	}

	/**
	 * @see cellularAutomata.cellState.model.CellState#setToFullState()
	 */
	public void setToFullState()
	{
		int[] fullState = new int[length];
		for(int i = 0; i < length; i++)
		{
			fullState[i] = maxValue;
		}

		super.setValue(fullState);
	}

	/**
	 * Sets a random vector for this cell state.
	 * 
	 * @param probability
	 *            The probability that a value is present in each of the state's
	 *            vector positions.
	 */
	public void setToRandomState(double probability)
	{
		int[] randomState = getRandomState(probability);
		super.setValue(randomState);
	}

	/**
	 * @see cellularAutomata.cellState.model.CellState#setStateFromString
	 */
	public void setStateFromString(String state)
	{
		int[] arrayState = new int[length];

		// parse the string
		// state string looks like this 0,1,1,0,1,0,0 or this 0 1 1 0 1 0 0
		// Note that this is entirely different from the delimeter that
		// separates states from each other in a data file.
		String delimeters = " ";
		if(state.indexOf(",") != -1)
		{
			delimeters = ", ";
		}

		StringTokenizer tokenizer = new StringTokenizer(state, delimeters);
		for(int i = 0; i < length; i++)
		{
			try
			{
				String nextToken = tokenizer.nextToken();

				int number = Integer.parseInt(nextToken);

				// make sure it is in range
				if(number < minValue)
				{
					number = minValue;
				}
				else if(number > maxValue)
				{
					number = maxValue;
				}

				arrayState[i] = number;
			}
			catch(Exception e)
			{
				// there weren't enough tokens so the nextToken() method failed.
				// Use the minimum allowed value instead.
				arrayState[i] = minValue;

				// warn the user
				if(!havePrintedImportDataWarning)
				{
					havePrintedImportDataWarning = true;

					// make the JFrame look disabled
					if(CAController.getCAFrame() != null)
					{
						CAController.getCAFrame().setViewDisabled(true);
					}

					// warn the user
					String message = "Please check the "
						+ InitialStatesPanel.INIT_STATES_TAB_TITLE
						+ " tab. \n\n"
						+ "You are importing data from a file. \n\n"
						+ "The current rule is based on vectors rather than numbers. \n"
						+ "Therefore, it cannot always import numerical images or data. \n"
						+ "When the conversion is not possible, the cell states will be set \n"
						+ "to a blank state instead.\n\n"
						+ "You may resubmit with a different initial state if you prefer.";
					JOptionPane.showMessageDialog(CAController.getCAFrame()
							.getFrame(), message, "Import file warning",
							JOptionPane.WARNING_MESSAGE);

					// make the JFrame look enabled
					if(CAController.getCAFrame() != null)
					{
						CAController.getCAFrame().setViewDisabled(false);
					}
				}
			}
		}

		super.setValue(arrayState);
	}

	/**
	 * Returns the hash code.
	 * 
	 * @see cellularAutomata.cellState.model.CellState#toInt
	 */
	public int toInt()
	{
		return this.hashCode();
	}

	/**
	 * The state evaluated as a string.
	 * 
	 * @return the state as a string.
	 */
	public String toString()
	{
		String stringState = "";

		Object value = this.getValue();
		if(value != null)
		{
			// get state as an array
			int[] state = (int[]) value;

			// convert state to a string
			// (no space at the beginning)
			stringState = "" + state[0];

			if(state.length > 1)
			{
				for(int i = 1; i < length; i++)
				{
					// space before every number (but the first one)
					stringState += " " + state[i];
				}
			}
		}

		return stringState;
	}
}
