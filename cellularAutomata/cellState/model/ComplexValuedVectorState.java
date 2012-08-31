/*
 ComplexValuedVectorState -- a class within the Cellular Automaton Explorer. 
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
import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.graphics.InitialStatesPanel;
import cellularAutomata.util.math.Complex;
import cellularAutomata.util.math.RandomSingleton;

/**
 * A vector state of any length with any Complex values, represented as an array
 * of Complex numbers.
 * 
 * @author David Bahr
 */
public class ComplexValuedVectorState extends CellState
{
	// random generator
	private static Random random = RandomSingleton.getInstance();

	// the length of the vector
	private static int length = 1;

	// the empty state
	private Complex[] emptyState = null;

	// The full state
	private Complex[] fullState = null;

	// The full state
	private Complex[] alternateState = null;

	// The state of the cell
	private Complex[] state = null;

	// The maximum value that each of the real and imaginary components can take
	// when constructing a random vector. Used by the method getRandomState().
	// Reset in the constructor.
	private double maxRandom = 1.0;

	// The minimum value that each of the real and imaginary components can take
	// when constructing a random vector. Used by the method getRandomState().
	// Reset in the constructor.
	private double minRandom = -1.0;

	/**
	 * Create a Complex vector state from an array of Complex numbers, and use
	 * the specified view to display the vectors.
	 * 
	 * @param state
	 *            The vector of complex values (as an array).
	 * @param alternateState
	 *            An alternate state supplied by the user that will be drawn
	 *            with a right click.
	 * @param emptyState
	 *            An empty state supplied by the user. Usually a vector of 0+0i,
	 *            or minimum values, but may be a vector of any complex numbers.
	 * @param fullState
	 *            A vector of usually a maximum values, but may be a vector of
	 *            any complex values.
	 * @param minRandom
	 *            The minimum value that each of the real and imaginary
	 *            components can take when constructing a random vector. Used by
	 *            the method getRandomState().
	 * @param maxRandom
	 *            The maximum value that each of the real and imaginary
	 *            components can take when constructing a random vector. Used by
	 *            the method getRandomState().
	 */
	public ComplexValuedVectorState(Complex[] state, Complex[] alternateState,
			Complex[] emptyState, Complex[] fullState, double minRandom,
			double maxRandom)
	{
		// make sure state is not null
		if(!checkState(state) || !checkState(alternateState)
				|| !checkState(emptyState) || !checkState(fullState))
		{
			throw new IllegalArgumentException(
					"Class: ComplexValuedVectorState. Method: checkState. Each state "
							+ "must be a non-null and non-empty array.");
		}

		length = state.length;

		// make sure the alternate, full, empty states are the same length as
		// the state
		if((alternateState.length != state.length)
				|| (emptyState.length != state.length)
				|| (fullState.length != state.length))
		{
			throw new IllegalArgumentException(
					"Class: ComplexValuedVectorState. Method: checkState. The alternate, "
							+ "empty, and full states must have the same length as the state.");
		}

		// set state using the method from the super class
		setValue(state);

		// instantiate
		if(this.alternateState == null)
		{
			this.alternateState = new Complex[length];
		}
		if(this.emptyState == null)
		{
			this.emptyState = new Complex[length];
		}
		if(this.fullState == null)
		{
			this.fullState = new Complex[length];
		}

		// make copies to be safe
		for(int i = 0; i < length; i++)
		{
			this.alternateState[i] = new Complex(alternateState[i]);
			this.emptyState[i] = new Complex(emptyState[i]);
			this.fullState[i] = new Complex(fullState[i]);
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
	private boolean checkState(Complex[] state)
	{
		boolean ok = true;

		if((state == null) || (state.length < 1))
		{
			ok = false;
			// throw new IllegalArgumentException(
			// "Class: ComplexValuedVectorState. Method: checkState. The state "
			// + "must be a non-null and non-empty array.");
		}

		return ok;
	}

	/**
	 * Returns a random Complex vector state.
	 * 
	 * @param probability
	 *            The probability that a value is present in each of the
	 *            vector's positions. When a position is not present, it is
	 *            assigned the complex value 0.0+0.0i.
	 * @return a random vector state.
	 */
	private Complex[] getRandomState(double probability)
	{
		Complex[] state = new Complex[length];

		for(int i = 0; i < length; i++)
		{
			if(random.nextDouble() < probability)
			{
				// now get a random real component between minRandom and
				// maxRandom
				double real = random.nextDouble() * (maxRandom - minRandom)
						+ minRandom;

				// now get a random imaginary component between minRandom and
				// maxRandom
				double imaginary = random.nextDouble()
						* (maxRandom - minRandom) + minRandom;

				state[i] = new Complex(real, imaginary);
			}
			else
			{
				// make this part of the vector 0.0+0.0i
				state[i] = new Complex(0.0, 0.0);
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
		// Note that this clones the arrays as well! Very important.
		return new ComplexValuedVectorState((Complex[]) state.clone(),
				(Complex[]) alternateState.clone(), (Complex[]) emptyState
						.clone(), (Complex[]) fullState.clone(), minRandom,
				maxRandom);
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
	 * Decide if two complex vectors are the same (same type, same length, same
	 * complex numbers).
	 */
	public boolean equals(Object o)
	{
		boolean equal = false;
		if(o != null && (o instanceof Complex[]))
		{
			Complex[] c = (Complex[]) o;
			if(c.length == length)
			{
				equal = true;

				int i = 0;
				while(equal && (i < length))
				{
					if(!c[i].equals(state[i]))
					{
						equal = false;
					}

					i++;
				}
			}
		}

		return equal;
	}

	/**
	 * Tests if a given CellState is the same as the "alternate" state.
	 * 
	 * @return true if the state is alternate.
	 * @see cellularAutomata.cellState.model.CellState#setToAlternateState()
	 */
	public boolean isAlternate()
	{
		boolean alternate = false;
		if(this.equals(alternateState))
		{
			alternate = true;
		}

		return alternate;
	}

	/**
	 * Tests if a given CellState is "blank". A blank state is one that equals
	 * the empty state.
	 * 
	 * @return true if the state is "blank".
	 * @see cellularAutomata.cellState.model.IntegerVectorState#setToEmptyState()
	 */
	public boolean isBlank()
	{
		return isEmpty();
	}

	/**
	 * Tests if a given CellState is the same as the "empty" state.
	 * 
	 * @return true if the state is empty.
	 * @see cellularAutomata.cellState.model.IntegerVectorState#setToEmptyState()
	 */
	public boolean isEmpty()
	{
		boolean empty = false;
		if(this.equals(emptyState))
		{
			empty = true;
		}

		return empty;
	}

	/**
	 * Tests if a given CellState is the same as the "full" state.
	 * 
	 * @return true if the state is full.
	 * @see cellularAutomata.cellState.model.IntegerVectorState#setToFullState()
	 */
	public boolean isFull()
	{
		boolean full = false;
		if(this.equals(fullState))
		{
			full = true;
		}

		return full;
	}

	/**
	 * @see cellularAutomata.cellState.model.CellState#setToAlternateState()
	 */
	public void setToAlternateState()
	{
		super.setValue(alternateState);
		state = alternateState;
	}

	/**
	 * @see cellularAutomata.cellState.model.CellState#setToEmptyState()
	 */
	public void setToEmptyState()
	{
		super.setValue(emptyState);
		state = emptyState;
	}

	/**
	 * @see cellularAutomata.cellState.model.CellState#setToFullState()
	 */
	public void setToFullState()
	{
		super.setValue(fullState);
		state = fullState;
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
		Complex[] randomState = getRandomState(probability);
		super.setValue(randomState);
		state = randomState;
	}

	/**
	 * @see cellularAutomata.cellState.model.CellState#setStateFromString
	 */
	public void setStateFromString(String state)
	{
		Complex[] arrayState = new Complex[length];

		// parse the string
		// state string looks like this 0.0 + 0.0i, 1.0 + 1.0i, 2.0 + 2.0i
		String delimeters = ",";
		StringTokenizer tokenizer = new StringTokenizer(state, delimeters);
		for(int i = 0; i < length; i++)
		{
			String complexNumber = "";
			try
			{
				complexNumber = tokenizer.nextToken();
				Complex number = new Complex(complexNumber);
				arrayState[i] = number;
			}
			catch(Exception e)
			{
				arrayState[i] = new Complex(0.0, 0.0);

				// note that this method eventually calls super.setValue(Object)
				// as required by the contract with this abstract method.

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
							+ "When the conversion is not possible, part or all of the \n"
							+ "vectors will be set to 0+0i instead.\n\n"
							+ "You may resubmit with a different initial state if you \n"
							+ "prefer.";
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

		this.state = arrayState;

		super.setValue(arrayState);
	}

	/**
	 * Sets a value for this cell state. Overrides the parent class
	 * implementation so that we can save the state locally and convert to an
	 * array.
	 * 
	 * @param state
	 *            The new state of the cell.
	 */
	public void setValue(Object state)
	{
		if((state instanceof Complex[]) && checkState((Complex[]) state))
		{
			Complex[] complexVector = (Complex[]) state;

			// make a copy
			if(state != null)
			{
				this.state = new Complex[length];

				for(int i = 0; i < length; i++)
				{
					this.state[i] = new Complex(complexVector[i]);
				}
			}
			else
			{
				this.state = null;
			}

			super.setValue(this.state);
		}
	}

	/**
	 * Returns the hash code.
	 * 
	 * @see cellularAutomata.cellState.model.CellState#toInt()
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

		if(state != null)
		{
			// convert state to a string
			// (no space at the beginning)
			stringState = "" + state[0];

			if(state.length > 1)
			{
				for(int i = 1; i < state.length; i++)
				{
					// comma and space before every number (but the first one)
					stringState += ", " + state[i];
				}
			}
		}

		return stringState;
	}
}
