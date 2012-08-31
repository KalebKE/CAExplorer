/*
 RealValuedState -- a class within the Cellular Automaton Explorer. 
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
import cellularAutomata.util.math.RandomSingleton;

/**
 * A cell state for a cellular automaton with complex numbers as states.
 * 
 * @author David Bahr
 */
public class RealValuedState extends CellState
{
	/**
	 * The default empty state if no other is specified.
	 */
	public static final double DEFAULT_EMPTY_STATE = 0.0;

	/**
	 * The default full state if no other is specified.
	 */
	public static final double DEFAULT_FULL_STATE = 1.0;

	// random generator
	private static Random random = RandomSingleton.getInstance();

	// the empty state (typically the smallest value)
	private double emptyState = DEFAULT_EMPTY_STATE;

	// The full state (typically the largest value)
	private double fullState = DEFAULT_FULL_STATE;

	// for speed we place this here.
	private double state = 0.0;

	/**
	 * Sets a double value as the initial state with a default cell state view.
	 * 
	 * @param state
	 *            Any double value to be used as the state.
	 * @param emptyState
	 *            The state corresponding to a "blank", like 0.0. The default
	 *            view draws a color scaled between the emptyState and
	 *            fullState.
	 * @param fullState
	 *            The state corresponding to a filled cell, like 1.0. The
	 *            default view draws a color scaled between the emptyState and
	 *            fullState.
	 */
	public RealValuedState(double state, double emptyState, double fullState)
	{
		this.state = state;
		this.emptyState = emptyState;
		this.fullState = fullState;

		// set state using the method from the super class
		setValue(new Double(state));
	}

	/**
	 * Determines if a given rule is compatible with this class,
	 * RealValuedState.
	 * 
	 * @param rule
	 *            The rule that is being tested for compatibility.
	 * @return true if the Rule is compatible with this class, RealValuedState.
	 */
	public static boolean isCompatibleRule(Rule rule)
	{
		boolean isCompatible = false;

		if(rule != null)
		{
			// find out what cell state works with this rule
			CellState cellState = rule.getCompatibleCellState();

			// if RealValuedState works with this rule, then is compatible
			// i.e., next line checks to see if the cellState could be cast to a
			// RealValuedState without throwing an exception
			try
			{
				// try casting
				RealValuedState state = (RealValuedState) cellState;

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
	 * Determines if a given rule is compatible with this class,
	 * RealValuedState.
	 * 
	 * @param ruleDescription
	 *            The description of the rule (as returned by
	 *            Rule.getDisplayName()).
	 * @return true if the Rule is compatible with this class, RealValuedState.
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
		return new RealValuedState(state, emptyState, fullState);
	}

	/**
	 * Gets the "empty" state.
	 */
	public double getEmptyState()
	{
		return emptyState;
	}

	/**
	 * Gets the "full" state.
	 */
	public double getFullState()
	{
		return fullState;
	}

	/**
	 * The state of the cell.
	 * 
	 * @return The state.
	 */
	public double getState()
	{
		return state;
	}

	/**
	 * Tests if a given CellState is "alternate" by making sure it is neither
	 * the empty nor the full state.
	 * 
	 * @return true if the state is alternate.
	 * @see cellularAutomata.cellState.model.CellState#setToAlternateState()
	 */
	public boolean isAlternate()
	{
		boolean isAlternate = false;
		if(state != emptyState && state != fullState)
		{
			isAlternate = true;
		}

		return isAlternate;
	}

	/**
	 * Tests if a given CellState is "empty".
	 * 
	 * @return true if the state is empty.
	 * @see cellularAutomata.cellState.model.IntegerCellState#setToEmptyState()
	 */
	public boolean isEmpty()
	{
		boolean isEmpty = false;
		if(state == emptyState)
		{
			isEmpty = true;
		}

		return isEmpty;
	}

	/**
	 * Tests if a given CellState is "full".
	 * 
	 * @return true if the state is full.
	 * @see cellularAutomata.cellState.model.IntegerCellState#setToFullState()
	 */
	public boolean isFull()
	{
		boolean isFull = false;
		if(state == fullState)
		{
			isFull = true;
		}

		return isFull;
	}

	/**
	 * Sets this cell state to random value. Child classes may override for
	 * different behavior.
	 */
	public void setToAlternateState()
	{
		double alternateState = RandomSingleton.getInstance().nextDouble();
		super.setValue(new Double(alternateState));
		state = alternateState;
	}

	/**
	 * Sets this cell state to the "empty" state.
	 */
	public void setToEmptyState()
	{
		super.setValue(new Double(emptyState));
		state = emptyState;
	}

	/**
	 * Sets this cell state to the "full" state.
	 */
	public void setToFullState()
	{
		super.setValue(new Double(fullState));
		state = fullState;
	}

	/**
	 * Sets a random or empty state for this cell state.
	 * 
	 * @param probability
	 *            The probability that the cell will be occupied rather than
	 *            blank.
	 */
	public void setToRandomState(double probability)
	{
		if(random.nextDouble() < probability)
		{
			double randomValue = emptyState + (fullState - emptyState)
					* random.nextDouble();
			super.setValue(new Double(randomValue));
			state = randomValue;
			// System.out.println("RealValuedState: setRandomState: emptyState =
			// "+emptyState);
			// System.out.println("RealValuedState: setRandomState: fullState =
			// "+fullState);
			// System.out.println("RealValuedState: setRandomState: state =
			// "+state);
		}
		else
		{
			super.setValue(new Double(emptyState));
			state = emptyState;
		}
	}

	/**
	 * The state of the cell.
	 * 
	 * @param state
	 *            A double value representing the cell state.
	 */
	public void setState(double state)
	{
		super.setValue(new Double(state));

		this.state = state;

	}

	/**
	 * Sets a String value for the cell's state. The user may wish to override
	 * this method in a child class.
	 */
	public void setStateFromString(String state)
	{
		try
		{
			Double newState = new Double(state);

			// note that this method eventually calls super.setValue(Object) as
			// required by the contract with this abstract method.
			setState(newState.doubleValue());
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
						+ "The current rule is based on real numbers, but part of \n"
						+ "the image or data file could not be converted into a real \n"
						+ "number. Consider importing a different file.\n\n"
						+ "The import will continue but with some values set to 0.0.\n\n"
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
		super.setValue(state);

		this.state = ((Double) state).doubleValue();
	}

	/**
	 * The state as an integer (takes the floor of the double value).
	 * 
	 * @return the floor of the double value.
	 */
	public int toInt()
	{
		return (int) state;
	}
}
