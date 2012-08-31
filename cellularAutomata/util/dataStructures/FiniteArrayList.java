/*
 FiniteArrayList -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.util.dataStructures;

/**
 * An array list of cells with a finite size that cannot grow. When the max size
 * is reached, the oldest elements are deleted to make room for new elements.
 * When a maximum size can be specified, this saves the amortized cost of
 * increasing the size of the array. The cyclic array list is backed by an
 * array, so add(), get() and set() operations are fast, O(1). The removeFirst()
 * and removeLast() elements are also fast, O(1). A general remove(index)
 * operation is not provided because it would be O(N) slow (the elements would
 * have to be shifted on the array). Only O(1) methods are available.
 * <p>
 * The java ArrayList is a more general purpose data structure, but this
 * structure was designed for speed.
 * <p>
 * Note that this implementation is not synchronized and must be synchronized
 * externally if accessed by multiple threads.
 * 
 * @author David Bahr
 */
public class FiniteArrayList<T>
{
	// the data of generic type T
	private T[] elements = null;

	// the index of the last element in the list
	private int end = 0;

	// the size of the array
	private int capacity = 1;

	// the number of elements in the list (often called size)
	private int numElements = 0;

	/**
	 * Build a finite-sized array list with the maximum specified capacity.
	 * 
	 * @param capacity
	 *            The maximum capacity of the array list.
	 */
	public FiniteArrayList(int capacity)
	{
		// keep track of the max number of elements
		this.capacity = capacity;

		// need to start the end of the list at the end of the array. When the
		// first element is added, it will be shifted to the right and into the
		// 0th position.
		this.end = capacity;

		// Create an array large enough to hold all of the elements. Note that
		// the warning is annoying and an unfortunate consequence of being
		// unable to create generic arrays. But under the hood, generics already
		// cast from Objects, so this is no different.
		elements = (T[]) new Object[capacity];
	}

	/**
	 * Add an element to the end of the list. If the list is at its maximum
	 * allowed size, then the first element is removed before this element is
	 * added (there is no need to call any of the remove() methods explicitly).
	 * 
	 * @param element
	 *            The element that will be added.
	 */
	public void add(T element)
	{
		end = (end + 1) % capacity;

		// save the element. Note that if we have reached the max size, then we
		// simply overwrite the first element.
		elements[end] = element;

		// increase the number of elements, but don't exceed the capacity (when
		// the capacity has been reached, the previous array assignment
		// overwrites the first element (so we aren't increasing the number of
		// elements)
		if(numElements < capacity)
		{
			numElements++;
		}
	}

	/**
	 * Return the element at the specified index.
	 * 
	 * @param index
	 *            The position of the element that will be returned.
	 * @return The element at the specified index.
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (index < 0 or index >= size()).
	 */
	public T get(int index)
	{
		if(index < 0 || index >= numElements)
		{
			throw new IndexOutOfBoundsException();
		}

		// the array position of the requested element
		int position = ((end + 1) - numElements) + index;
		if(position < 0)
		{
			position += capacity;
		}

		return elements[position];
	}

	/**
	 * Return the first element in the list
	 * 
	 * @return The first element in the list.
	 * @throws IndexOutOfBoundsException
	 *             if the list is empty.
	 */
	public T getFirst()
	{
		if(numElements <= 0)
		{
			throw new IndexOutOfBoundsException();
		}

		// the array position of the first element
		int position = ((end + 1) - numElements);
		if(position < 0)
		{
			position += capacity;
		}

		return elements[position];
	}

	/**
	 * Return the last element in the list
	 * 
	 * @return The last element in the list.
	 * @throws IndexOutOfBoundsException
	 *             if the list is empty.
	 */
	public T getLast()
	{
		if(numElements <= 0)
		{
			throw new IndexOutOfBoundsException();
		}

		return elements[end];
	}

	/**
	 * Returns true if this finite array list contains no elements.
	 * 
	 * @return true if this finite array list contains no elements.
	 */
	public boolean isEmpty()
	{
		return numElements > 0 ? false : true;
	}

	/**
	 * Remove the element at the beginning of the list and return it. Has no
	 * effect if the list is empty.
	 * 
	 * @return The element at the beginning of the list, or null if the list is
	 *         empty.
	 * @throws IndexOutOfBoundsException
	 *             if the list is empty.
	 */
	public T removeAndReturnFirst()
	{
		T element = null;
		if(numElements > 0)
		{
			int position = (end + 1) - numElements;

			if(position < 0)
			{
				position += capacity;
			}

			element = elements[position];

			numElements--;
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}

		return element;
	}

	/**
	 * Remove the element at the end of the list and return it. Has no effect if
	 * the list is empty.
	 * 
	 * @return The element at the end of the list, or null if the list is empty.
	 * @throws IndexOutOfBoundsException
	 *             if the list is empty.
	 */
	public T removeAndReturnLast()
	{
		T element = null;
		if(numElements > 0)
		{
			element = elements[end];

			end--;
			if(end < 0)
			{
				end = capacity - 1;
			}

			numElements--;
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}

		return element;
	}

	/**
	 * Remove the element at the beginning of the list.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the list is empty.
	 */
	public void removeFirst()
	{
		if(numElements > 0)
		{
			numElements--;
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
	}

	/**
	 * Remove the element at the end of the list. Has no effect if the list is
	 * empty.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the list is empty.
	 */
	public void removeLast()
	{
		if(numElements > 0)
		{
			end--;
			if(end < 0)
			{
				end = capacity - 1;
			}

			numElements--;
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
	}

	/**
	 * Replace the element at the specified index (replace with the specified
	 * element).
	 * 
	 * @param index
	 *            The position of the element that will be replaced.
	 * @param element
	 *            The element that will be stored at the specified position.
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (index < 0 or index >= size()).
	 */
	public void set(int index, T element)
	{
		if(index < 0 || index >= numElements)
		{
			throw new IndexOutOfBoundsException();
		}

		// the array position of the specified element
		int position = ((end + 1) - numElements) + index;
		if(position < 0)
		{
			position += capacity;
		}

		elements[position] = element;
	}

	/**
	 * Replace the first element of the list (replace with the specified
	 * element).
	 * 
	 * @param element
	 *            The element that will be stored at the first position.
	 * @throws IndexOutOfBoundsException
	 *             if the list is empty.
	 */
	public void setFirst(T element)
	{
		if(numElements > 0)
		{
			// the array position of the first element
			int position = ((end + 1) - numElements);
			if(position < 0)
			{
				position += capacity;
			}

			elements[position] = element;
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
	}

	/**
	 * Replace the last element of the list (replace with the specified
	 * element).
	 * 
	 * @param element
	 *            The element that will be stored at the last position.
	 * @throws IndexOutOfBoundsException
	 *             if the list is empty.
	 */
	public void setLast(T element)
	{
		if(numElements > 0)
		{
			elements[end] = element;
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
	}

	/**
	 * The number of elements currently in the list.
	 * 
	 * @return the number of elements in the list.
	 */
	public int size()
	{
		return numElements;
	}
}
