/*
 ComplexState -- a class within the Cellular Automaton Explorer. 
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

import javax.swing.JOptionPane;

import cellularAutomata.CAController;
import cellularAutomata.CurrentProperties;
import cellularAutomata.graphics.InitialStatesPanel;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.reflection.RuleHash;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.math.Complex;
import cellularAutomata.util.math.RandomSingleton;

/**
 * A cell state for a cellular automaton with complex numbers as states.
 * 
 * @author David Bahr
 */
public class ComplexState extends CellState
{
	// random generator
	private static Random random = RandomSingleton.getInstance();

	// the empty state
	private Complex emptyState = null;

	// The full state
	private Complex fullState = null;

	// The full state
	private Complex alternateState = null;

	// for speed we place this here.
	private Complex state = null;

	/**
	 * Sets a complex number as the initial state.
	 * 
	 * @param state
	 *            Any complex number to be used as the state.
	 * @param alternateState
	 *            The state that is an alternate to the full state (and drawn by
	 *            a right mouse click on the grid). Most commonly is set to the
	 *            same number as the emptyState.
	 * @param emptyState
	 *            The state corresponding to a "blank", like 0+0i.
	 * @param fullState
	 *            The state corresponding to a filled cell, like 1+1i.
	 */
	public ComplexState(Complex state, Complex alternateState,
			Complex emptyState, Complex fullState)
	{
		if(checkState(state) && checkState(emptyState) && checkState(fullState))
		{
			// save here for fast access
			this.state = state;
			this.alternateState = alternateState;
			this.emptyState = emptyState;
			this.fullState = fullState;

			// set state using the method from the super class
			setValue(state);
		}
	}

	/**
	 * Determines if a given rule is compatible with this class, ComplexState.
	 * 
	 * @param rule
	 *            The rule that is being tested for compatibility.
	 * @return true if the Rule is compatible with this class, ComplexState.
	 */
	public static boolean isCompatibleRule(Rule rule)
	{
		boolean isCompatible = false;

		if(rule != null)
		{
			// find out what cell state works with this rule
			CellState cellState = rule.getCompatibleCellState();

			// if ComplexState works with this rule, then is compatible
			// i.e., next line checks to see if the cellState could be cast to a
			// ComplexState without throwing an exception
			try
			{
				// try casting
				ComplexState state = (ComplexState) cellState;

				// it worked, so compatible
				isCompatible = true;
			}
			catch(Exception e)
			{
				// not compatible
			}
		}

		return isCompatible;
	}

	/**
	 * Determines if a given rule is compatible with this class, ComplexState.
	 * 
	 * @param ruleDescription
	 *            The description of the rule (as returned by
	 *            Rule.getDisplayName()).
	 * @return true if the Rule is compatible with this class, ComplexState.
	 */
	public static boolean isCompatibleRule(String ruleDescription)
	{
		boolean isCompatible = false;

		if(ruleDescription != null)
		{
			RuleHash ruleHash = new RuleHash();
			String ruleClassName = ruleHash.get(ruleDescription);

			// instantiate the rule using reflection
			Rule rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(ruleClassName);

			isCompatible = isCompatibleRule(rule);
		}

		return isCompatible;
	}

	/**
	 * Check that the state is not null. If is null, throws an exception.
	 * 
	 * @param state
	 *            The state being checked.
	 * @return true if the state is ok.
	 * @throws IllegalArgumentException
	 *             if is null.
	 */
	private boolean checkState(Complex state)
	{
		// check to be sure is not null.
		if(state == null)
		{
			throw new IllegalArgumentException(
					"Class: ComplexState. Method: checkState. The state is null, "
							+ "which is not allowed.");
		}

		// if get here then is ok
		return true;
	}

	/**
	 * Check that the state is not null. If is null, throws an exception.
	 * 
	 * @param state
	 *            The state being checked.
	 * @return true if the state is ok.
	 * @throws IllegalArgumentException
	 *             if is null.
	 */
	private boolean checkState(Object state)
	{
		// check to be sure is not null.
		if(state == null)
		{
			throw new IllegalArgumentException(
					"Class: ComplexState. Method: checkState. The state is null, "
							+ "which is not allowed.");
		}
		else
		{
			if(!state.getClass().equals(Complex.class))
			{
				throw new ClassCastException(
						"Class: ComplexState. Method: checkState. "
								+ "The state must be of type Complex.");
			}
		}

		// if get here then is ok
		return true;
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
		// note that this clones the complex numbers as well! Very important.
		return new ComplexState((Complex) state.clone(),
				(Complex) alternateState.clone(), (Complex) emptyState.clone(),
				(Complex) fullState.clone());
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
	 * The state of the cell.
	 * 
	 * @return The state.
	 */
	public Complex getState()
	{
		return state;
	}

	/**
	 * Tests if a given CellState is "alternate" by using the equals method to
	 * compare the state to the alternateState.
	 * 
	 * @return true if the state is alternate.
	 * @see cellularAutomata.cellState.model.CellState#setToAlternateState()
	 */
	public boolean isAlternate()
	{
		return state.equals(alternateState);
	}

	/**
	 * Tests if a given CellState is "empty".
	 * 
	 * @return true if the state is empty.
	 * @see cellularAutomata.cellState.model.IntegerCellState#setToEmptyState()
	 */
	public boolean isEmpty()
	{
		return state.equals(emptyState);
	}

	/**
	 * Tests if a given CellState is "full".
	 * 
	 * @return true if the state is full.
	 * @see cellularAutomata.cellState.model.IntegerCellState#setToFullState()
	 */
	public boolean isFull()
	{
		return state.equals(fullState);
	}

	/**
	 * Sets this cell state to the "alternate" state.
	 */
	public void setToAlternateState()
	{
		super.setValue(alternateState);
		state = alternateState;
	}

	/**
	 * Sets this cell state to the "empty" state.
	 */
	public void setToEmptyState()
	{
		super.setValue(emptyState);
		state = emptyState;
	}

	/**
	 * Sets this cell state to the "full" state.
	 */
	public void setToFullState()
	{
		super.setValue(fullState);
		state = fullState;
	}

	/**
	 * Sets a random empty or alternate state for this cell state.
	 * 
	 * @param probability
	 *            The probability that the cell will be occupied rather than
	 *            blank.
	 */
	public void setToRandomState(double probability)
	{
		if(random.nextDouble() < probability)
		{
			super.setValue(alternateState);
			state = alternateState;
		}
		else
		{
			super.setValue(emptyState);
			state = emptyState;
		}
	}

	/**
	 * The state of the cell.
	 * 
	 * @param state
	 *            A complex number representing the cell state.
	 */
	public void setState(Complex state)
	{
		// throw exception if not a valid value
		if(checkState(state))
		{
			super.setValue(state);

			this.state = state;
		}
	}

	/**
	 * Sets a String value for the cell's state. The user may wish to override
	 * this method in a child class.
	 */
	public void setStateFromString(String state)
	{
		try
		{
			Complex newState = new Complex(state);

			// note that this method eventually calls super.setValue(Object) as
			// required by the contract with this abstract method.
			setState(newState);
		}
		catch(Exception e)
		{
			// note that this method eventually calls super.setValue(Object) as
			// required by the contract with this abstract method.
			setState(emptyState);

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
						+ "The current rule is based on complex numbers, but part \n"
						+ "of the requested image or data file could not be converted \n"
						+ "to a complex number. Please import a different initial state "
						+ "file.\n\n"
						+ "The import will continue but with some values set to 0+0i.\n\n"
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
		if(checkState(state))
		{
			super.setValue(state);

			this.state = (Complex) state;
		}
	}

	/**
	 * The state as an integer.
	 * 
	 * @return The hash code of the state.
	 */
	public int toInt()
	{
		return state.hashCode();
	}
}
