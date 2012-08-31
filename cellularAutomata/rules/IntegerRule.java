/*
 IntegerRuleTemplate -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2007  David B. Bahr (http://academic.regis.edu/dbahr/)

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

package cellularAutomata.rules;

import java.math.BigInteger;

import cellularAutomata.CurrentProperties;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.reflection.RuleHash;
import cellularAutomata.util.MinMaxBigIntPair;
import cellularAutomata.util.MinMaxIntPair;

/**
 * A base class for all integer-based rules (not vectors of integers, just
 * single integers).
 * 
 * @author David Bahr
 */
public abstract class IntegerRule extends Rule
{
	/**
	 * The maximum number of permissible states.
	 */
	public static int MAX_NUM_STATES = Integer.MAX_VALUE;

	/**
	 * The minimum number of permissible states.
	 */
	public static int MIN_NUM_STATES = 2;
	
	/**
	 * The default largest size in *digits* for rule numbers. In other words,
	 * each rule number can have up to 30000 digits unless another maximum value
	 * is set by the user.
	 */
	public static final int DEFAULT_MAX_RULE_SIZE = 30000;

	/**
	 * The largest allowed size in *digits* for rule numbers. In other words,
	 * each rule number can have up to 30000 digits unless another maximum value
	 * is set by the user (in the System menu). Not recommended that this value
	 * be set directly. Instead, let the user set this number from the Systems
	 * menu while the application is running.
	 */
	public static int maxRuleSize = DEFAULT_MAX_RULE_SIZE;

	/**
	 * Create a rule that is based on integer values.
	 * <p>
	 * When building child classes, the minimalOrLazyInitialization parameter
	 * must be included but may be ignored. However, the boolean is intended to
	 * indicate when the child's constructor should build a rule with as small a
	 * footprint as possible. In order to load rules by reflection, the
	 * application must query the child classes for information like their
	 * display names, tooltip descriptions, etc. At these times it makes no
	 * sense to build the complete rule which may have a large footprint in
	 * memory.
	 * <p>
	 * It is recommended that the child's constructor and instance variables do
	 * not initialize any variables and that variables be initialized only when
	 * first needed (lazy initialization). Or all initializations in the
	 * constructor may be placed in an <code>if</code> statement.
	 * 
	 * <pre>
	 * if(!minimalOrLazyInitialization)
	 * {
	 *     ...initialize
	 * }
	 * </pre>
	 * 
	 * @param minimalOrLazyInitialization
	 *            When true, the constructor instantiates an object with as
	 *            small a footprint as possible. When false, the rule is fully
	 *            constructed. If uncertain, set this variable to false.
	 */
	public IntegerRule(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		// find out if the max rule size (in digits) has changed
		maxRuleSize = CurrentProperties.getInstance()
				.getMaxRuleNumberSizeInDigits();
	}

	/**
	 * Tells the graphics what value should be displayed for the "Rule number"
	 * text field. By default, the number is whatever value was previously
	 * displayed. This method should be overridden by child classes if they
	 * desire non-default behavior.
	 * 
	 * @param latticeDescription
	 *            The display name of the lattice.
	 * @return The rule number that will be displayed. By default returns null,
	 *         which keeps the text field's current value.
	 */
	protected BigInteger ruleNumberToDisplay(String latticeDescription)
	{
		return null;

		// Here are some possible alternatives.

		// // (1) Display the minimum allowed value.
		// return getMinMaxRuleNumbers(latticeDescription).min;

		// // (2) Display the value that was submitted with the previous rule.
		//
		// BigInteger ruleNum = BigInteger.ZERO;
		//
		// String sRuleNum = properties
		// .getProperty(CAPropertyReader.RULE_NUMBER);
		// if(sRuleNum != null)
		// {
		// ruleNum = new BigInteger(sRuleNum);
		// }
		//
		// return ruleNum;
	}

	/**
	 * Tells the graphics what value should be displayed for the "Number of
	 * States" text field. By default, the number is whatever value was
	 * previously displayed. This method should be overriden by child classes if
	 * they desire non-default behavior.
	 * 
	 * @param latticeDescription
	 *            The display name of the lattice.
	 * @return The number of states that will be displayed. By default returns
	 *         null, which keeps the text field's current value.
	 */
	protected Integer stateValueToDisplay(String latticeDescription)
	{
		return null;

		// Here are some possible alternatives.

		// // (1) Display the minimum allowed value.
		// return getMinMaxAllowedStates(latticeDescription).min;

		// // (2) Display the value that was submitted with the previous rule.
		//
		// int numStates = IntegerCellState.MIN_NUM_STATES;
		//
		// String sNumStates = properties
		// .getProperty(CAPropertyReader.NUMBER_OF_STATES);
		// if(sNumStates != null)
		// {
		// numStates = Integer.parseInt(sNumStates);
		// }
		//
		// return new Integer(numStates);
	}

	/**
	 * Gets a pair of numbers for the minimum and maximum allowable rule numbers
	 * for the specified lattice. When this method returns null (the default
	 * value), the "rule number" display field is disabled. Sub-classes should
	 * override this method to enable the rule number display field.
	 * <p>
	 * The allowed rule numbers can be huge, so this method returns a pair of
	 * BigIntegers (which can be arbitrarily large). However, only ints or longs
	 * are typically necessary. therefore, convenience constructors are provided
	 * in MinMaxBigIntPair that take ints and longs as parameters. For example,
	 * this method might return <code>new MinMaxBigIntPair(3, 31)</code>.
	 * 
	 * @param latticeDescription
	 *            The display name of the lattice for which a min and max rule
	 *            numbers will be specified.
	 * @param numStates
	 *            The number of states allowed for a cell on the lattice.
	 * @return A pair of numbers for the minimum and maximum allowable rule
	 *         numbers. May be null if the concept of a minimum and maximum does
	 *         not make sense for this rule. Null is the default value.
	 */
	protected MinMaxBigIntPair getMinMaxAllowedRuleNumbers(
			String latticeDescription, int numStates)
	{
		return null;
	}

	/**
	 * Gets a pair of numbers for the minimum and maximum allowable integer
	 * state values for the specified lattice. Sub-classes should override this
	 * method if the default min and max is inappropriate. If returns null, then
	 * the "Number of States" text field will be disabled; in other words, the
	 * user will be unable to enter the number of states. It is recommended that
	 * in that case, the programmer should also specify the number of states in
	 * stateValueToDisplay(). Also note that the programmer can still alter the
	 * number of states at any time by setting the value within the rule's code.
	 * For example, the "More Properties" button may allow the user to change
	 * the number of states in a separate field placed there. In that case, the
	 * programmer should be careful to set the property value for the new state
	 * value. For example,
	 * properties.setProperty(CAPropertyReader.NUMBER_OF_STATES, numStates);
	 * 
	 * @param latticeDescription
	 *            The display name of the lattice for which a min and max state
	 *            will be specified.
	 * @return A pair of numbers for the minimum and maximum allowable states.
	 *         May be null if there is no maximum, or if the concept of a
	 *         minimum and maximum does not make sense for this rule.
	 */
	protected MinMaxIntPair getMinMaxAllowedStates(String latticeDescription)
	{
		// use defaults
		int min = MIN_NUM_STATES;
		int max = MAX_NUM_STATES;
		MinMaxIntPair minMax = new MinMaxIntPair(min, max);

		return minMax;
	}

	/**
	 * Finds the minimum and maximum allowable rule number for the given lattice
	 * and rule.
	 * 
	 * @param latticeDescription
	 *            The display name of the lattice for which the min and max rule
	 *            numbers will be determined.
	 * @param ruleDescription
	 *            The display name of the rule for which the min and max rule
	 *            numbers will be determined.
	 * @param numStates
	 *            The number of states allowed for a cell on the lattice.
	 * @return The minimum and maximum rule numbers allowed with the specified
	 *         rule and lattice. Null if this makes no sense for the given rule.
	 */
	public static MinMaxBigIntPair getMinMaxRuleNumberAllowed(
			String latticeDescription, String ruleDescription, int numStates)
	{
		// a place to store the min and max allowed rule numbers
		MinMaxBigIntPair minMax = null;

		if((latticeDescription != null) && (ruleDescription != null))
		{
			RuleHash ruleHash = new RuleHash();
			String ruleClassName = ruleHash.get(ruleDescription);
			Rule rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(ruleClassName);

			try
			{
				// this will fail if not an IntegerRule
				IntegerRule nRule = (IntegerRule) rule;
				minMax = nRule.getMinMaxAllowedRuleNumbers(latticeDescription,
						numStates);
			}
			catch(Exception e)
			{
				// do nothing
			}
		}

		return minMax;
	}

	/**
	 * Finds the minimum and maximum allowable states for the given lattice and
	 * rule. When returns null, then the "Number of States" text field will be
	 * disabled.
	 * 
	 * @param latticeDescription
	 *            The display name of the lattice for which the min and max
	 *            states will be determined.
	 * @param ruleDescription
	 *            The display name of the rule for which the min and max states
	 *            will be determined.
	 * @return The minimum and maximum number of states allowed with the
	 *         specified rule and lattice. Null if this makes no sense for the
	 *         given rule.
	 */
	public static MinMaxIntPair getMinMaxStatesAllowed(
			String latticeDescription, String ruleDescription)
	{
		// a place to store the min and max allowed states
		MinMaxIntPair minMax = null;

		if((latticeDescription != null) && (ruleDescription != null))
		{
			RuleHash ruleHash = new RuleHash();
			String ruleClassName = ruleHash.get(ruleDescription);
			Rule rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(ruleClassName);

			try
			{
				// this will fail if not an IntegerRule
				IntegerRule nRule = (IntegerRule) rule;
				minMax = nRule.getMinMaxAllowedStates(latticeDescription);
			}
			catch(Exception e)
			{
				// do nothing
			}
		}

		return minMax;
	}

	/**
	 * Finds the value of the rule number that will be displayed in the "Rule
	 * number" text field.
	 * 
	 * @param latticeDescription
	 *            The display name of the lattice for which the rule number will
	 *            be determined.
	 * @param ruleDescription
	 *            The display name of the rule for which the rule number will be
	 *            determined.
	 * @return The rule number should be displayed for the "Rule number" text
	 *         field. When null, will display the value currently in the text
	 *         field.
	 */
	public static BigInteger getRuleNumberToDisplay(String latticeDescription,
			String ruleDescription)
	{
		// the rule number that will be displayed (if null, will be the
		// currently displayed value)
		BigInteger ruleNumber = null;

		if((latticeDescription != null) && (ruleDescription != null))
		{
			RuleHash ruleHash = new RuleHash();
			String ruleClassName = ruleHash.get(ruleDescription);
			Rule rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(ruleClassName);

			try
			{
				// this will fail if not an IntegerRule
				IntegerRule nRule = (IntegerRule) rule;
				ruleNumber = nRule.ruleNumberToDisplay(latticeDescription);
			}
			catch(Exception e)
			{
				// do nothing
			}
		}

		return ruleNumber;
	}

	/**
	 * Finds the value of the state that will be displayed in the "Number of
	 * States" text field.
	 * 
	 * @param latticeDescription
	 *            The display name of the lattice for which the state value will
	 *            be determined.
	 * @param ruleDescription
	 *            The display name of the rule for which the state value will be
	 *            determined.
	 * @return The state value should be displayed for the "Number of States"
	 *         text field. When null, will display the value currently in the
	 *         text field.
	 */
	public static Integer getStateValueToDisplay(String latticeDescription,
			String ruleDescription)
	{
		// the state that will be displayed (if null, will be the currently
		// displayed value)
		Integer state = null;

		if((latticeDescription != null) && (ruleDescription != null))
		{
			RuleHash ruleHash = new RuleHash();
			String ruleClassName = ruleHash.get(ruleDescription);
			Rule rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(ruleClassName);

			try
			{
				// this will fail if not an IntegerRule
				IntegerRule nRule = (IntegerRule) rule;
				state = nRule.stateValueToDisplay(latticeDescription);
			}
			catch(Exception e)
			{
				// do nothing
			}
		}

		return state;
	}
}
