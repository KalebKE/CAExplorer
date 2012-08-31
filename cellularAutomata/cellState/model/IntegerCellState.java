/*
 IntegerCellState -- a class within the Cellular Automaton Explorer. 
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
import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.graphics.InitialStatesPanel;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.graphics.colors.ColorTools;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.reflection.RuleHash;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.math.RandomSingleton;

/**
 * A cell state for a cellular automaton with values 0 through N-1. The maximum
 * possible number of states is limited only by the system's maximum integer
 * value, but more than 256 states cannot be displayed because the colors will
 * overlap.
 * 
 * @author David Bahr
 */
public class IntegerCellState extends CellState
{
	// Note: the following DRAW_STATE must be updated at the same time that
	// ColorScheme.DRAW_COLOR is updated.
	// This is a drawing state -- the state that gets set when the user draws
	// with a left click. Gets set by mouse clicks on a color chooser. Should
	// not be set by the constructor, only by the color chooser!
	/**
	 * The state used for a cell drawn with a left-click by the user. -1 until
	 * reset by the color chooser.
	 */
	public static int DRAW_STATE = -1;

	/**
	 * The state used for a cell drawn with a left-click by the user. -1 until
	 * reset by the color chooser.
	 */
	public static int SECOND_DRAW_STATE = -1;

	// for speed we place this here. Usually we don't need to get the whole
	// Integer object (just its value).
	private int state = 0;

	// an alternate state. For example, half way between the min and max states
	// (gets set in the constructor and by mouse clicks on a color chooser).
	// Making static saves 4 bytes per instance.
	private static int alternateState = (int) (Math.round(CurrentProperties
			.getInstance().getNumStates() / 2.0) - 1.0);

	// a drawing state. The state that gets set when the user draws with a left
	// click. Gets set by mouse clicks on a color chooser. Also set in the
	// constructor. Making static saves 4 bytes per instance.
	private static int drawState = CurrentProperties.getInstance()
			.getNumStates() - 1;

	/**
	 * Sets an initial state (between 0 and numStates - 1) and assumes there are
	 * only two states (0 and 1) unless setNumStates() is called.
	 * 
	 * @param state
	 *            An integer between 0 and numStates - 1.
	 */
	// CAUSES PROBLEMS BECAUSE BUILDS LATTICES WITH VIEWS THAT ARE NOT THE SAME
	// AS ELSEWHERE AND THE VIEW IS STATIC, SO EVERY CELL GETS AFFECTED
	// public IntegerCellState(int state)
	// {
	// this(state, new IntegerCellStateView());
	// }
	/**
	 * Sets an initial state (between 0 and numStates - 1).
	 * 
	 * @param state
	 *            An integer between 0 and numStates - 1.
	 */
	public IntegerCellState(int state)
	{
		// PROFILING shows that we really want to minimize anything that happens
		// in this constructor. So keep it simple.

		// save here for fast access and less memory
		this.state = state;

		// to save memory, don't set a value in the super class
		// super.setValue(new Integer(state));
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
		return new IntegerCellState(state);
	}

	/**
	 * The state of the cell.
	 * 
	 * @return The integer state.
	 */
	public int getState()
	{
		return state;
	}

	/**
	 * The state of the cell. Overrides the parent class.
	 * 
	 * @return The integer state.
	 */
	public Object getValue()
	{
		return new Integer(state);
	}

	/**
	 * Tests if a given CellState is the "alternate" state (which might, for
	 * example, be half way between the maximum and minimum possible states).
	 * 
	 * @return true if the state is alternate.
	 * @see cellularAutomata.cellState.model.CellState#setToAlternateState()
	 */
	public boolean isAlternate()
	{
		// make sure alternateState value is legit. Have to do this because the
		// numStates might change and the static alternateState will retain its
		// old value (i.e., might not change at the same time as numStates).
		if(alternateState >= CurrentProperties.getInstance().getNumStates())
		{
			alternateState = (int) (Math.round(CurrentProperties.getInstance()
					.getNumStates() / 2.0) - 1.0);
		}

		return state == alternateState;
	}

	/**
	 * Determines if the currently active rule is compatible with this class,
	 * IntegerCellState.
	 * 
	 * @return true if the currently active rule is compatible with this class,
	 *         IntegerCellState.
	 */
	public static boolean isCurrentRuleCompatible()
	{
		boolean isCompatible = false;

		String ruleClassName = CurrentProperties.getInstance()
				.getRuleClassName();
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(ruleClassName);

		if(rule != null)
		{
			// find out what cell state works with this rule
			CellState cellState = rule.getCompatibleCellState();

			// if IntegerCellState works with this rule, then is compatible
			// i.e., next line checks to see if the cellState could be cast to a
			// IntegerCellState without throwing an exception
			try
			{
				// try casting
				IntegerCellState state = (IntegerCellState) cellState;

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
	 * IntegerCellState.
	 * 
	 * @param rule
	 *            The rule that is being tested for compatibility.
	 * @return true if the Rule is compatible with this class, IntegerCellState.
	 */
	public static boolean isCompatibleRule(Rule rule)
	{
		boolean isCompatible = false;

		if(rule != null)
		{
			// find out what cell state works with this rule
			CellState cellState = rule.getCompatibleCellState();

			// if IntegerCellState works with this rule, then is compatible
			// i.e., next line checks to see if the cellState could be cast to a
			// IntegerCellState without throwing an exception
			try
			{
				// try casting
				IntegerCellState state = (IntegerCellState) cellState;

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
	 * IntegerCellState.
	 * 
	 * @param ruleDescription
	 *            The description of the rule (as returned by
	 *            Rule.getDisplayName()).
	 * @return true if the Rule is compatible with this class, IntegerCellState.
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
	 * Tests if a given CellState is the "drawing" state (the state that a user
	 * would draw with a left click).
	 * 
	 * @return true if the state is the "drawing" state.
	 */
	public boolean isDrawState()
	{
		// make sure drawState value is legit. Have to do this because the
		// numStates might change and the static drawState will retain its old
		// value (i.e., might not change at the same time as numStates).
		if(drawState >= CurrentProperties.getInstance().getNumStates())
		{
			drawState = CurrentProperties.getInstance().getNumStates() - 1;
		}

		return state == drawState;
	}

	/**
	 * Tests if a given CellState is "empty". An empty state is one that equals
	 * 0.
	 * 
	 * @return true if the state is empty.
	 * @see cellularAutomata.cellState.model.IntegerCellState#setToEmptyState()
	 */
	public boolean isEmpty()
	{
		return state == 0;
	}

	/**
	 * Tests if a given CellState is "full". A full state is one that equals the
	 * maximum permitted integer value.
	 * 
	 * @return true if the state is full.
	 * @see cellularAutomata.cellState.model.IntegerCellState#setToFullState()
	 */
	public boolean isFull()
	{
		return state == (CurrentProperties.getInstance().getNumStates() - 1);
	}

	/**
	 * The state of the cell.
	 * 
	 * @param state
	 *            The integer state.
	 */
	public void setState(int state)
	{
		super.setValue(null);
		// super.setValue(new Integer(state));

		this.state = state;
	}

	/**
	 * Sets an Integer value for this cell state by parsing the string.
	 */
	public void setStateFromString(String state)
	{
		try
		{
			int value = Integer.parseInt(state);

			// make sure it isn't too big or too small
			if(value > CurrentProperties.getInstance().getNumStates() - 1)
			{
				value = CurrentProperties.getInstance().getNumStates() - 1;

				throw new Exception("Too big");
			}
			else if(value < 0)
			{
				value = 0;

				throw new Exception("Too small");
			}

			// note that setState() eventually calls super.setValue(Object) as
			// required by the contract with this abstract method.
			setState(value);
		}
		catch(NumberFormatException e)
		{
			// note that setState() eventually calls super.setValue(Object) as
			// required by the contract with this abstract method.
			setState(0);

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
						+ "The current rule is based on integers, but part of \n"
						+ "the image or data file could not be converted into \n"
						+ "a number. Consider importing a different file.\n\n"
						+ "The import will continue but with some values set \n"
						+ "to 0. \n\n"
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
		catch(Exception e)
		{
			if(!havePrintedDataOutOfRangeWarning)
			{
				havePrintedDataOutOfRangeWarning = true;

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
						+ "The current rule is based on integers from 0 to \n"
						+ (CurrentProperties.getInstance().getNumStates() - 1)
						+ ", but part of the image or data file could not be \n"
						+ "converted into a number within that range. Consider \n"
						+ "importing a different file.\n\n"
						+ "The import will continue but with some values set \n"
						+ "to 0 or "
						+ (CurrentProperties.getInstance().getNumStates() - 1)
						+ ". \n\n"
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
	 * Sets an alternate state either (1) drawn by the user, or (2) half way
	 * between the minimum and the maximum state, rounded to the nearest
	 * integer. Called by LatticeMouseListener when the user draws a cell with a
	 * right click.
	 */
	public void setToAlternateState()
	{
		// see if the color chooser has selected a state. (SECOND_DRAW_STATE !=
		// -1)
		if(SECOND_DRAW_STATE != -1)
		{
			alternateState = SECOND_DRAW_STATE;
		}
		else
		{
			alternateState = ColorTools.getStateValueFromColor(
					ColorScheme.SECOND_DRAW_COLOR, CurrentProperties
							.getInstance().getNumStates(), Cell.getView());

			// SECOND_DRAW_STATE = alternateState;
		}

		state = alternateState;
	}

	/**
	 * Sets the state to the one that is specified as a "drawing state". The
	 * drawing state may be changed by the user from a color chooser. Called by
	 * LatticeMouseListener when the user draws a cell with a left click.
	 */
	public void setToDrawingState()
	{
		// see if the color chooser has selected a state. (DRAW_STATE != -1)
		if(DRAW_STATE != -1)
		{
			drawState = DRAW_STATE;
		}
		else
		{
			drawState = ColorTools.getStateValueFromColor(
					ColorScheme.DRAW_COLOR, CurrentProperties.getInstance()
							.getNumStates(), Cell.getView());

			// DRAW_STATE = drawState;
		}

		state = drawState;
	}

	/**
	 * Sets a blank value for this cell state. This may be a 0, a 0 length
	 * vector, or anything else appropriate to this particular class.
	 */
	public void setToEmptyState()
	{
		// super.setValue(new Integer(0));
		state = 0;
	}

	/**
	 * Sets a maximum integer value for this cell state, corresponding to the
	 * number of possible states minus one.
	 */
	public void setToFullState()
	{
		// super.setValue(new Integer(numStates - 1));
		state = CurrentProperties.getInstance().getNumStates() - 1;
	}

	/**
	 * Sets a random integer for this cell state.
	 * 
	 * @param probability
	 *            The probability that the cell will be occupied rather than
	 *            blank.
	 */
	public void setToRandomState(double probability)
	{
		Random random = RandomSingleton.getInstance();
		int num = 0;
		if(random.nextDouble() < probability)
		{
			// a random number between *1* and *numStates-1*
			num = 1 + random.nextInt(CurrentProperties.getInstance()
					.getNumStates() - 1);
		}

		// super.setValue(new Integer(num));
		state = num;
	}

	/**
	 * Sets the state of the cell. Overrides the parent class.
	 */
	public void setValue(Object o)
	{
		if(o != null)
		{
			setState(((Integer) o).intValue());
		}
	}

	/**
	 * The state as an integer.
	 * 
	 * @return A value from 0 to N-1 where N is the number of states.
	 */
	public final int toInt()
	{
		return state;
	}

	/**
	 * Overrides toString() in the parent class.
	 */
	public String toString()
	{
		return Integer.toString(state);
	}
}
