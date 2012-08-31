/*
 FiniteObjectRuleTemplate -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.rules.templates;

import java.util.Hashtable;
import cellularAutomata.util.MinMaxIntPair;

/**
 * <p>
 * A convenience class/template for all rules that have a finite number of
 * states of any type (for example, Complex numbers, Doubles, Colors, Images,
 * Files, or even a mix of different types). The advantage of using this
 * template (rather than the ObjectRuleTemplate or IntegerRuleTemplate) is that
 * each state is represented by a different color and can be selected and drawn
 * from the cellular automata's toolbar menu. Another feature of this class is
 * that the numStates text field is disabled on the properties panel, but its
 * value is automatically updated to reflect the number of states specified by
 * the rule. The number of states that is displayed is determined strictly by
 * the rule, and not the user.
 * <p>
 * Every object that represents a state must have a unique toString() signature.
 * Every time a cell holds the same state, it must return the same string.
 * <p>
 * The number and values of the finite states can be reset by the child class at
 * any time by changing the value returned in the getObjectArray() method. This
 * will update the states at the next generation (and at instantiation). If the
 * number of states needs updating at another time, the user can force an update
 * by calling forceStateUpdate(). The objects set in the getObjectArray() method
 * will be collected and used.
 * <p>
 * The states can be of any type, like a Complex number, Vector, Double, or
 * anything else. The states may even be of different types. A finite number of
 * states can always be mapped isomorphically onto a finite number of integers.
 * Therefore, this class actually treats each object as an integer by extending
 * the IntegerRuleTemplate. However, the child class (and user) does not need to
 * be aware of this mapping from object to integer. The child class (user) just
 * has to extend this class and implement the abstract method getObjectArray()
 * which will return an array of the objects. This class then creates the
 * "object to integer" mapping.
 * <p>
 * This class handles conversion of the neighbors as Cells to neighbors as
 * object values so that the subclass only has to worry about specifying the
 * rule on the objects. This conversion makes sure that the cells all return
 * their state values for the same generation as the current cell.
 * <p>
 * This class uses the cell state cellularAutomaton.cellState.model.NCellState.
 * <p>
 * The number of states text field on the Properties panel is disabled by this
 * class. However, this class automatically updates the number of states
 * displayed on the Properties panel and the Status panel.
 * <p>
 * This class uses the Template Method design pattern. Subclasses implement the
 * abstract objectRule() method which is called by the template method
 * calculateNewState().
 * 
 * @author David Bahr
 */
public abstract class FiniteObjectRuleTemplate extends IntegerRuleTemplate
{
	// the hash table that translates objects to their corresponding integer
	// value
	private Hashtable objectHash = null;

	// An array containing all of the possible states available to this rule.
	private Object[] objectArray = null;

	/**
	 * Create a rule using the given cellular automaton properties.
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
	public FiniteObjectRuleTemplate(boolean minimalOrLazyInitialization)
	{
		super(minimalOrLazyInitialization);

		objectArray = null;

		// This gets the objects that are the cell's state values; and
		// update the hash table; and update the Properties panel and Status
		// panel displays to reflect the number of states. (The status panel
		// display is not reset if this rule is not the currently active
		// rule.)
		this.objectArray = getObjectArray();
		updateObjectHash(objectArray);
	}

	/**
	 * Make sure that the given state value is one of the objects that has been
	 * listed as a permissable state.
	 * 
	 * @param stateValue
	 *            The state being checked.
	 * @return true if the state is permissable.
	 */
	private boolean checkState(Object stateValue)
	{
		boolean okState = true;

		// if not in the list of keys for the object states
		if((stateValue == null)
				|| (objectHash.get(stateValue.toString()) == null))
		{
			// then not permissable
			okState = false;
		}

		return okState;
	}

	/**
	 * Converts an Object into its corresponding integer state.
	 * 
	 * @param objectState
	 *            The object state.
	 * @return The integer state corresponding to the object in objectState.
	 */
	private int objectStateToInt(Object objectState)
	{
		int theIntValue = 0;

		try
		{
			theIntValue = ((Integer) objectHash.get(objectState.toString()))
					.intValue();
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException(
					"Invalid object state.  It is not one of the states set by the rule.");
		}

		return theIntValue;
	}

	/**
	 * Update the hash table that translates object state to integer states.
	 */
	private void updateObjectHash(Object[] objectArray)
	{
		Hashtable<String, Integer> hash = new Hashtable<String, Integer>();

		// fill the hashtable
		for(int i = 0; i < objectArray.length; i++)
		{
			hash.put(objectArray[i].toString(), new Integer(i));
		}

		// replace the old hash table
		this.objectHash = hash;
	}

	/**
	 * A convenience method that forces an update of the states being used.
	 * Child classes can call this method as desired. This method calls
	 * getObjectArray() and uses the states specified in that array. This method
	 * is primarily useful when both (1) the number or type of states has
	 * changed, and (2) the rule wants the update to occur before the next
	 * generation of the CA is run. (If the rule can wait, updates will happen
	 * automatically at the next time generation.)
	 */
	protected void forceStateUpdate()
	{
		// get the objects to make sure we are still up to date
		this.objectArray = getObjectArray();

		// and update the hash table
		updateObjectHash(objectArray);

		// and update the Properties panel and Status panel displays for the
		// number of states. (The status panel display is not reset if this rule
		// is not the currently active rule.)
		setNumberOfStates(this.objectArray.length, this.getDisplayName(), false);
	}

	/**
	 * Disables the numStates text field.
	 */
	protected MinMaxIntPair getMinMaxAllowedStates(String latticeDescription)
	{
		// disables the “Number of States” text field
		return null;
	}

	/**
	 * This method should return a finite array of all the objects which are
	 * available as states for this rule. For example, if the rule can use five
	 * different Complex numbers, then these five complex numbers should be
	 * returned in an array. The objects do not have to be of the same type.
	 * Every state must return a unique String representation with the method
	 * toString(). If two objects have the same state value, then their String
	 * representations must be the same.
	 * <p>
	 * For display purposes, the first element of the array will be assigned the
	 * color associated with the "empty state" and the last element of the array
	 * will be assigned the color associated with the "full state". Also, the
	 * tooltip on the color chooser will display the value returned by the
	 * toString() method of the object. So if the object is "new Complex(3.0,
	 * 4.0)", then the toString() method returns "3.0 + 4.0i" which is displayed
	 * on the color chooser as the tooltip. If the object was a group element in
	 * a cyclic group of size 5, then the toString method could return e, a,
	 * a^2, a^3, a^4, and these would be displayed by the tooltip. (Note that
	 * tooltips understand html, so it is possible to display an exponent with
	 * the html command for superscripts, "sup".)
	 * 
	 * @return An array of the different states that are available to this rule.
	 */
	protected abstract Object[] getObjectArray();

	/**
	 * This class should not be overridden by child classes. Instead, child
	 * classes should implement the objectRule() method.
	 * <p>
	 * The rule for the cellular automaton which will be an integer function of
	 * the neighbors. This method calls objectRule which should be implemented
	 * by the user.
	 * 
	 * @param cellValue
	 *            The current value of the cell being updated.
	 * @param neighbors
	 *            The neighbors as their integer values.
	 * @param numStates
	 *            The number of states. In other words, the returned state can
	 *            only have values between 0 and numStates - 1.
	 * @param generation
	 *            The current generation of the CA.
	 * @return A new state for the cell with a value between 0 and numStates -
	 *         1.
	 */
	protected int integerRule(int cellValue, int[] neighbors, int numStates,
			int generation)
	{
		// convert the int values to their corresponding objects
		Object[] neighborsAsObjects = new Object[neighbors.length];
		for(int i = 0; i < neighborsAsObjects.length; i++)
		{
			neighborsAsObjects[i] = intToObjectState(neighbors[i]);
		}

		Object value = objectRule(intToObjectState(cellValue),
				neighborsAsObjects, generation);

		// make sure the returned value is acceptable
		if(!checkState(value))
		{
			String warning = "The method objectRule() in the class "
					+ this.getClass().getName()
					+ " \nhas returned a value of \"" + value + "\" that is \n"
					+ "not in the list of permitted cell states (from \n"
					+ "the method getObjectArray()).";

			throw new IllegalArgumentException(warning);
		}

		// the user returns an object. This converts the object to its
		// corresponding integer and returns the value.
		return objectStateToInt(value);
	}

	/**
	 * The rule for the cellular automaton which will be a function of the cell
	 * and it's neighbors. Each cell holds an object of some type. It is up to
	 * the implementation of this method to cast the value into the correct
	 * type. The implementation should calculate a new value for the cell and
	 * then return that value. The new value must be equal to one of the finite
	 * number of states specified in the abstract method getCellStates().
	 * <p>
	 * Note that the Rule does not have to return exactly the same objects that
	 * are specified in the getObjectArray() method. In fact that would be
	 * unusual behavior. Instead, this method just has to return an object that
	 * has the same String representation as one of the states specified in the
	 * getObjectArray() method.
	 * 
	 * @param cellValue
	 *            The current value of the cell being updated. It is up to the
	 *            user to cast this object into the correct type.
	 * @param neighbors
	 *            The neighbors' values. It is up to the user to cast each of
	 *            the neighbors array elements into the correct type.
	 * @param generation
	 *            The current generation of the CA.
	 * @return A new state for the cell which must be one of the finite number
	 *         of states specified in the abstract method getCellStates().
	 */
	protected abstract Object objectRule(Object cellValue, Object[] neighbors,
			int generation);

	/**
	 * Displays a default value in the numStates text field.
	 */
	protected Integer stateValueToDisplay(String latticeDescription)
	{
		return new Integer(this.getObjectArray().length);
	}

	/**
	 * A list of lattices with which this Rule will work; in this case, returns
	 * all lattices by default, though child classes may wish to override this
	 * and restrict the lattices with which the child rule will work.
	 * <p>
	 * Well-designed Rules should work with any lattice, but some may require
	 * particular topological or geometrical information (like the lattice gas).
	 * Appropriate strings to return in the array include
	 * SquareLattice.DISPLAY_NAME, HexagonalLattice.DISPLAY_NAME,
	 * StandardOneDimensionalLattice.DISPLAY_NAME, etc. If null, will be
	 * compatible with all lattices.
	 * 
	 * @return A list of lattices compatible with this Rule (returns the display
	 *         names for the lattices). Returns null if compatible with all
	 *         lattices.
	 */
	public String[] getCompatibleLattices()
	{
		// String[] lattices = {SquareLattice.DISPLAY_NAME,
		// HexagonalLattice.DISPLAY_NAME,
		// StandardOneDimensionalLattice.DISPLAY_NAME,
		// TriangularLattice.DISPLAY_NAME,
		// TwelveNeighborTriangularLattice.DISPLAY_NAME,
		// FourNeighborSquareLattice.DISPLAY_NAME};

		return null;
	}

	/**
	 * Converts an integer into its corresponding Object state.
	 * 
	 * @param integerState
	 *            The integer state.
	 * @return The object state corresponding to the integer value of
	 *         integerState. Returns null if no object corresponds to that
	 *         integer value.
	 */
	public Object intToObjectState(int integerState)
	{
		Object theObject = null;
		if(integerState < objectArray.length)
		{
			theObject = objectArray[integerState];
		}

		return theObject;
	}
}
