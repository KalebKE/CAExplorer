/*
 Coordinate -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.util;

/**
 * Holds a pair of points representing the row and column of an array.
 * 
 * @author David Bahr
 */
public class Coordinate
{
	private int row = 0;

	private int col = 0;

	/**
	 * Create a pair of points representing the row and column of an array.
	 * 
	 * @param row
	 *            The row of the array.
	 * @param col
	 *            The column of the array.
	 */
	public Coordinate(int row, int col)
	{
		this.row = row;
		this.col = col;
	}

	/**
	 * Get the column.
	 */
	public int getColumn()
	{
		return col;
	}

	/**
	 * Get the row.
	 */
	public int getRow()
	{
		return row;
	}

	/**
	 * Row followed by column in the format "[row, col]".
	 */
	public String toString()
	{
		return "[" + row + ", " + col + "]";
	}

	/**
	 * Set the column.
	 * 
	 * @param col
	 *            The coordinate of the column.
	 */
	public void setColumn(int col)
	{
		this.col = col;
	}

	/**
	 * Set the row.
	 * 
	 * @param row
	 *            The coordinate of the row.
	 */
	public void setRow(int row)
	{
		this.row = row;
	}
}
