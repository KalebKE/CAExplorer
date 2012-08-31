/*
 LatticeView -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.lattice.view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.movie.MovieMaker;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.Coordinate;

/**
 * Base class for all CA graphics.
 * 
 * @author David Bahr
 */
public abstract class LatticeView extends JPanel
{
	/**
	 * The height of the display panel.
	 */
	protected int height;

	/**
	 * The width of the display panel.
	 */
	protected int width;

	/**
	 * Color data used for faster display of square lattices. This stores a
	 * two-dimensional array as a one-dimensional array (in the order: row1,
	 * row2, row3...). One dimensional arrays are stored as expected. Must be
	 * instantiated by the child class.
	 */
	protected int[] cellRGBData = null;

	/**
	 * when true, a grid will be drawn. May be reset from the menu.
	 */
	protected static boolean menuAskedForGrid = false;

	// The tool tip if the rule fails to provide one.
	private static String EMPTY_TIP = "<html>No description of this "
			+ "cellular automaton is available.</html>";

	/**
	 * Create a panel for displaying the graphics.
	 */
	public LatticeView()
	{
		// this.setBorder(BorderFactory.createEtchedBorder());
		this.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

		this.setToolTipText("");

		// speed up the graphics
		this.setIgnoreRepaint(true);
	}

	/**
	 * Add a mouse listener to the panel.
	 * 
	 * @param listener
	 *            The mouse listener.
	 */
	public void addLatticeMouseListener(MouseInputListener listener)
	{
		// add a mouse listener
		this.addMouseListener((MouseInputListener) listener);
		this.addMouseMotionListener((MouseInputListener) listener);
	}

	/**
	 * Draws the actual CA graphics. Called by paintComponent to draw this on
	 * the screen, and called by a PrinterJob to draw this on a printed page.
	 * Usually contains calls to a graphics2D object, such as g2.drawRect(...).
	 * Or may contain a call to display a buffered offscreen image, like the
	 * offscreen image usually drawn in the method draw(lattice). This method
	 * should not be called directly; it is called by the paintComponent()
	 * method in this class.
	 */
	public abstract void draw(Graphics g);

	/**
	 * Adds a picture to the panel, put only the specified sub-picture.
	 * <p>
	 * This method is currently called for printing purposes.
	 * 
	 * @param upperLeftX
	 *            The upper-left x-position of the rectangle that will be drawn.
	 * @param upperLeftY
	 *            The upper-left y-position of the rectangle that will be drawn.
	 * @param width
	 *            The width the rectangle that will be drawn.
	 * @param length
	 *            The length the rectangle that will be drawn.
	 */
	public abstract void draw(Graphics2D g, int upperLeftX, int upperLeftY,
			int width, int length);

	/**
	 * Draws the cells of the lattice onto an offscreen image. Does not actually
	 * draw them onto the component, only the offscreen image.
	 * <p>
	 * The body of this method may be empty if there is no offscreen graphics.
	 * <p>
	 * It is also the responsibility of this method to draw the graphic g as a
	 * frames in a movie. This is done by calling
	 * 
	 * <pre>
	 * if(MovieMaker.isOpen())
	 * {
	 * 	MovieMaker.writeFrame(offScreenImage);
	 * }
	 * </pre>
	 * 
	 * @param lattice
	 *            The CA lattice.
	 */
	public abstract void drawLattice(Lattice lattice);

	/**
	 * Draws the single given cell onto an offscreen image. Draws the cell at
	 * the specified position. Note, that this does not actually draw the cell
	 * onto the component, but just onto an offscreen image.
	 * <p>
	 * The body of this method may be empty if there is no offscreen graphics.
	 * 
	 * @param cell
	 *            The CA lattice.
	 * @param xPos
	 *            The horizontal coordinate in the graphics space (in pixels)
	 *            which will be converted into a lattice position where the cell
	 *            will be drawn.
	 * @param yPos
	 *            The vertical coordinate in the graphics space (in pixels)
	 *            which will be converted into a lattice position where the cell
	 *            will be drawn.
	 */
	public abstract void drawCell(Cell cell, int xPos, int yPos);

	/**
	 * The height of the bounding rectangle for the cell's graphics (when
	 * displayed on the panel) in integer pixels. The number of pixels per cell
	 * should never be less that 1, or it will not display.
	 * 
	 * @param row
	 *            The row position on the lattice.
	 * @param col
	 *            The column position on the lattice.
	 * @return The height.
	 */
	public abstract int getCellHeightInPixels(int row, int col);

	/**
	 * The width of the bounding rectangle for each cell's graphics (when
	 * displayed on the panel). in integer pixels. The number of pixels per cell
	 * should never be less that 1, or it will not display.
	 * 
	 * @param row
	 *            The row position on the lattice.
	 * @param col
	 *            The column position on the lattice.
	 * @return The width.
	 */
	public abstract int getCellWidthInPixels(int row, int col);

	/**
	 * Return the cell displayed at the current cursor position xPos and yPos.
	 * This method is useful, for example, when we want to change the appearance
	 * or content of cells (with a mouse listener). This method is currently
	 * used to draw on the lattice.
	 * 
	 * @param xPos
	 *            The horizontal coordinate of the mouse.
	 * @param yPos
	 *            The vertical coordinate of the mouse.
	 * @return The cell displayed at the position xPos, yPos on the graphics.
	 *         May be null if there is no cell under the cursor.
	 */
	public abstract Cell getCellUnderCursor(int xPos, int yPos);

	/**
	 * Return the generation of the cell displayed at the current cursor
	 * position xPos and yPos. This method is useful, for example, in
	 * one-dimensional simulations where the user may be drawing on a previous
	 * generation rather than the current generation. Most applications will
	 * return the current generation.
	 * 
	 * @param xPos
	 *            The horizontal coordinate of the mouse.
	 * @param yPos
	 *            The vertical coordinate of the mouse.
	 * @return The generation of the cell displayed at the position xPos, yPos
	 *         on the graphics. May be -1 if there is no cell under the cursor.
	 */
	public abstract int getGenerationUnderCursor(int xPos, int yPos);

	/**
	 * Adds a picture to the panel.
	 * <p>
	 * This method is automatically called whenever the window is resized or
	 * otherwise altered. You can force it to be called by using the
	 * <code>repaint</code> method of the encompassing JFrame or JComponent.
	 * Never call this method directly (or the Graphics object may not be
	 * specified properly).
	 */
	public void paintComponent(Graphics g)
	{
		// Call the JPanel's paintComponent. This ensures
		// that the background is properly rendered.
		super.paintComponent(g);

		draw(g);
	}

	/**
	 * The number of columns displayed on the panel. Should return a negative
	 * value if rows and columns have no meaning for the given lattice (for
	 * example, a tree lattice).
	 * 
	 * @return The number of CA columns, or -1 if has no meaning for the given
	 *         lattice.
	 */
	public abstract int getNumColumns();

	/**
	 * The number of rows displayed on the panel. Should return a negative value
	 * if rows and columns have no meaning for the given lattice (for example, a
	 * tree lattice).
	 * 
	 * @return The number of CA rows, or -1 if has no meaning for the given
	 *         lattice.
	 */
	public abstract int getNumRows();

	/**
	 * Overrides the getToolTip from JComponent so that the tool tip depends on
	 * the currently selected simulation.
	 * 
	 * @return The tool tip.
	 */
	public String getToolTipText(MouseEvent e)
	{
		// get the rule that was selected
		String ruleClassName = CurrentProperties.getInstance()
				.getRuleClassName();

		// instantiate the rule using reflection
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(ruleClassName);

		// now get the tool tip
		String tip = EMPTY_TIP;
		if(rule != null)
		{
			tip = rule.getToolTipDescription();
		}

		// create default tip
		if(tip == null || tip.equals(""))
		{
			tip = EMPTY_TIP;
		}

		return tip;
	}

	/**
	 * Given x and y coordinates on the graphics, this returns the row and
	 * column of the corresponding cell at that position.
	 * 
	 * @param xPos
	 *            The horizontal position in pixels on the graphics panel.
	 * @param yPos
	 *            The vertical position in pixels on the graphics panel.
	 * @return The row and column as a coordinate pair, or null if rows and
	 *         columns do not make sense for the given lattice (for example, a
	 *         tree).
	 */
	public abstract Coordinate getRowCol(int xPos, int yPos);

	/**
	 * Get the height of the display on the panel.
	 * 
	 * @return The height in pixels.
	 */
	public int getDisplayHeight()
	{
		return height;
	}

	/**
	 * Get the width of the display on the panel.
	 * 
	 * @return The width in pixels.
	 */
	public int getDisplayWidth()
	{
		return width;
	}

	/**
	 * Redraws the lattice. Typically, just a call to drawLattice() along with
	 * resetting the color of the background.
	 */
	public abstract void redraw();

	/**
	 * Removes a mouse listener from the panel.
	 * 
	 * @param listener
	 *            The mouse listener.
	 */
	public void removeLatticeMouseListener(MouseInputListener listener)
	{
		// remove the mouse listener
		this.removeMouseListener((MouseInputListener) listener);
		this.removeMouseMotionListener((MouseInputListener) listener);
	}

	/**
	 * Change the size of the display panel by a constant scaling factor.
	 * 
	 * @param factor
	 *            The scaling factor.
	 */
	public void resizePanel(double factor)
	{
		if(factor > 0)
		{
			int tempWidth = (int) Math.round(factor * width);
			int tempHeight = (int) Math.round(factor * height);

			// only do this if the user hasn't shrunk the size to nothing
			if((tempWidth > 0) || (tempHeight > 0))
			{
				width = tempWidth;
				height = tempHeight;

				rescalePanel(factor);
			}
		}
	}

	/**
	 * When resizePanel is called, this method handles any resizing that must be
	 * done by the child class (for example, changing the size of each hexagon
	 * in a hexagonal lattice).
	 * 
	 * @param factor
	 *            The scaling factor by which the size is changed.
	 */
	protected abstract void rescalePanel(double factor);

	/**
	 * Sets a pixel in an array that is used for fast display. If the lattice is
	 * two-dimensional, this stores the two-dimensional array as a
	 * one-dimensional array (in the order: row1, row2, row3...). In other
	 * words, the position is given by row*numberOfColumns + column.
	 * 
	 * @param position
	 *            The position of the pixel being set. In two-dimensional
	 *            arrays, position = row*numberOfColumns + column.
	 * @param rgbColor
	 *            The color at that array position, which can be calculated from
	 *            Color's getRGB() method.
	 */
	public void setColorPixel(int position, int rgbColor)
	{
		// position is given by row * numCols + col
		cellRGBData[position] = rgbColor;
	}

	/**
	 * Draws a grid mesh on the graphics.
	 * 
	 * @param visible
	 *            Draws the grid when true, and hides the grid when false.
	 */
	public void setGridVisible(boolean visible)
	{
		menuAskedForGrid = visible;
		redraw();
		update();
	}

	/**
	 * Updates the cellular automaton graphics by repainting. This will capture
	 * any changes to the graphics.
	 */
	public void update()
	{
		repaint();
	}
}
