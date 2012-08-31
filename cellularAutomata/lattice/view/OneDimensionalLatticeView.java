/*
 OneDimensionalLatticeView -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.lattice.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;

import cellularAutomata.CAConstants;
import cellularAutomata.CurrentProperties;
import cellularAutomata.Cell;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.OneDimensionalLattice;
import cellularAutomata.movie.MovieMaker;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.dataStructures.FiniteArrayList;
import cellularAutomata.util.math.MathFunctions;

/*
 * This class is a little ugly, sorry. Here is the basic idea: draw(Graphics g)
 * is the primary drawing method, called by the LatticeView class whenever the
 * CA needs to be rendered. The method draw(Graphics g, int upperLeftX, int
 * upperLeftY, int width, int height) does virtually the same thing, but is used
 * for printing purposes (to print only a subsection of the CA). Both take
 * advantage of double buffering. The method drawLattice(Lattice lattice) is the
 * real work horse. It handles drawing the one-dim CA line by line. It keeps
 * track of which generation (line) was last drawn so that it does not redraw
 * that generation needlessly. Whenever necessary, this method translates the
 * image upwards to make room for the next line (generation). This translation
 * seems to be the source of some lingering flicker that is most visible on LCD
 * screens. Someday I'll get around to fixing that. The method drawLattice()
 * does the same thing as drawLattice(Lattice lattice) but is used when the
 * lattice has already been loaded, but is not currently available to the
 * caller. The drawLattice methods draw each cell one at a time by calling
 * drawSingleCellWithErase(). This erases any previously existing image (if that
 * is necessary) and then calls drawSingleCell(), which draws the image. The
 * redraw() method redraws the entire lattice, necessary for example, when the
 * user changes the color scheme.
 */

/**
 * Creates the display area for a one-dimensional square-grid cellular
 * automaton.
 * 
 * @author David Bahr
 */
public class OneDimensionalLatticeView extends LatticeView
{
	// true only when the redraw() method is running
	private boolean redrawing = false;

	// true if the lattice recently translated
	private boolean translated = false;

	// use the "color array" graphics approach when true (see the method
	// drawLattice(Lattice)).
	private static boolean useColorArray = true;

	// the height and width of each rectangle (cell) displayed on the panel
	private double cellHeight;

	private double cellWidth;

	// the running sum of "fractions of a pixel" that need to be translated
	// upwards. E.g., if a cellheight = 1.436, then every step accumulates 0.436
	// extra pixels that need to be translated upwards. If the accumulation is
	// greater than 1, then an extra translation of 1 pixel occurs, and 1 is
	// subtracted from the accumulation.
	private double accumulatedFractionalPixel = 0.0;

	// the last generation that was drawn on the image
	private int lastGenerationDrawn = -1;

	// the last row on the display array that was drawn. Irrelevant if not using
	// the "fast color array" called cellRGBData.
	private int lastRowDrawn = -1;

	// number of rows in the CA
	private int numRows;

	// number of columns in the CA
	private int numCols;

	// the last (previous) generation that was displayed (before the current
	// generation which is about to be displayed).
	private int previousDisplayGeneration = -1;

	// height of a cell as an integer
	private int rectHeight;

	// width of a cell as an integer
	private int rectWidth;

	// how many generations to average when displaying the cell's values
	private int runningAverage = 1;

	// off screen image used to display the cellRGBData (for fast display)
	private BufferedImage img = null;

	// the cells on the 1-d lattice
	protected Cell[] cells = null;

	// keeps track of whatever shape was last drawn at the given row and col
	private Shape[][] previousShape = null;

	// off screen graphics object that can be persistent
	private Graphics2D offScreenGraphics = null;

	// off screen graphics object that is used for high speed drawing
	private Graphics2D imgGraphics = null;

	// holds all the possible graphics configurations for the computer's
	// graphics card
	private GraphicsConfiguration graphicsConfiguration = null;

	// off screen image that can be persistent (the offScreenGraphics come from
	// this image)
	private BufferedImage offScreenImage = null;

	// the CA lattice
	private Lattice lattice = null;

	/**
	 * Creates a panel of the correct height and width.
	 * 
	 * @param lattice
	 *            The one-dimensional lattice.
	 */
	public OneDimensionalLatticeView(OneDimensionalLattice lattice)
	{
		super();

		this.setIgnoreRepaint(true);

		this.width = 0;
		this.height = 0;
		this.numRows = CurrentProperties.getInstance().getNumRows();
		this.numCols = lattice.getWidth();
		this.lattice = lattice;

		// setting the graphics configuration to match the current graphics card
		graphicsConfiguration = GraphicsEnvironment
				.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();

		// get the 1-d array of cells from the lattice
		cells = new Cell[numCols];
		Iterator iterator = lattice.iterator();
		for(int i = 0; i < numCols; i++)
		{
			cells[i] = (Cell) iterator.next();
		}

		// the number of generations that will be averaged for display
		runningAverage = CurrentProperties.getInstance().getRunningAverage();

		// no layout manager
		setLayout(null);

		// if(width <= 0 || height <= 0)
		// {
		// setDefaultHeightAndWidth();
		// }
		//		

		// get the smallest possible width and height (with the proper ratio) so
		// that we create a buffered image that is as small as possible (used in
		// setPanelSize())
		int gcd = MathFunctions.gcd(numCols, numRows);
		width = numCols / gcd;
		height = numRows / gcd;

		setPanelSize();

		// for fast display
		cellRGBData = new int[numCols];

		// keep track of cell colors in an array (for fast-displaying)
		for(int col = 0; col < numCols; col++)
		{
			setColorPixel(col, Cell.getView().getDisplayColor(
					cells[col].getState(), null, cells[col].getCoordinate())
					.getRGB());
		}

		// the image used to display the cellRGBData
		img = graphicsConfiguration.createCompatibleImage(numCols, numRows);
		img.setAccelerationPriority(1.0f);
		imgGraphics = img.createGraphics();
		imgGraphics.setColor(ColorScheme.EMPTY_COLOR);
		imgGraphics.fillRect(0, 0, numCols, numRows);

		// This is very important, particularly on Macs. Higher quality
		// interpolations like bilinear and bicubic do a poor job of
		// scaling-up(!) in image size. Square edges will be blurred. This hint
		// keeps the interpolation fast and keeps square edges looking nice.
		imgGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

		// Can display two different ways. If the cell's have no
		// special view/display shape, then is 2 times faster to use
		// the color array from OneDimensionalLatticeView. (I benchmarked
		// the times.) For really large and really small lattices, the
		// differences aren't very big.
		//
		// Every 100 generations make sure...
		useColorArray = useColorArray();
	}

	/**
	 * Set parameters that determine the size of the graphics.
	 */
	protected void setPanelSize()
	{
		// set the size of the JPanel
		setBounds(new Rectangle(0, 0, width, height));
		setMaximumSize(new Dimension(width, height));
		setMinimumSize(new Dimension(width, height));
		setPreferredSize(new Dimension(width, height));

		// the size of each square lattice on the panel
		// (watch that integer division)
		cellWidth = (double) width / (double) numCols;
		cellHeight = (double) height / (double) numRows;

		// make sure the cell's width and height is not less
		// than one pixel
		rectWidth = (int) Math.ceil(cellWidth);
		rectHeight = (int) Math.ceil(cellHeight);

		// its faster to render to offscreen graphics
		offScreenImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		offScreenGraphics = offScreenImage.createGraphics();
		offScreenGraphics.setColor(ColorScheme.EMPTY_COLOR);
		offScreenGraphics.fillRect(0, 0, width, height);

		// This is very important, particularly on Macs. Higher quality
		// interpolations like bilinear and bicubic do a poor job of
		// scaling-up(!) in image size. Square edges will be blurred. This hint
		// keeps the interpolation fast and keeps square edges looking nice.
		offScreenGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

		// Initialize the array that keeps track of the previously drawn shape
		// and subsequently makes sure we don't try to erase something that
		// isn't there.
		previousShape = new Shape[numRows][numCols];
		for(int row = 0; row < getNumRows(); row++)
		{
			for(int col = 0; col < getNumColumns(); col++)
			{
				previousShape[row][col] = null;
			}
		}
	}

	/**
	 * Decides whether or not we should use the faster color array.
	 * 
	 * @return true if should use the color array.
	 */
	private boolean useColorArray()
	{
		// in my latest benchmarks, the colorArray approach is always slower.
		// The percentage slower varies, but is sometimes as much as 85% on
		// small lattices and 30% on large lattices.
		return false;

		// boolean colorArray = true;
		//
		// // Can display two different ways. If the cell's have no
		// // special view/display shape, then is 2 times faster to use
		// // the color array from OneDimensionalLatticeView. (I benchmarked
		// // the times.) For really large and really small lattices, the
		// // differences aren't very big.
		// //
		// // Also, I don't know why, but the fast graphics are actually WAY
		// slower on
		// // the MAC. Go figure. So don't use the colorArray on a Mac. VERY
		// // ANNOYING. MAC Java really needs to catch up with the PCs.
		// //
		// // (Also, the grid lines don't draw properly on a MAC if we use the
		// // colorArray. Again, MAC java needs some work for fast graphics.)
		// //
		// // Every 100 generations make sure...
		// if(cells[0].getRule().getRequiredNumberOfGenerations() > 1
		// || CAConstants.MAC_OS)
		// {
		// // rules that require multiple initial states should not use the
		// // color array.
		// colorArray = false;
		// }
		// else if(cells[0].getGeneration() % 100 == 0)
		// {
		// // get the number of initial states (rows) required for the rule
		// int numInitialStates = cells[0].getRule()
		// .getRequiredNumberOfGenerations();
		//
		// Object[] parameters = new Object[] {new Integer(0), new Integer(0)};
		// Shape shape = cells[0].getState().getDisplayShape(10, 10,
		// parameters);
		// if((shape == null) && (numInitialStates == 1))
		// {
		// colorArray = true;
		// }
		// else
		// {
		// colorArray = false;
		// }
		// }
		//
		// return colorArray;
	}

	/**
	 * Draws a grid mesh on the graphics. Child classes should override with an
	 * appropriate grid (square, hexagonal, triangular, or other). May do
	 * nothing, but this will confuse the user that selects the "draw grid"
	 * option on the menu.
	 */
	protected void drawGrid(Graphics2D g)
	{
		g.setColor(Color.BLACK);

		// draw the horizontal grid lines
		for(int row = 0; row <= numRows; row++)
		{
			int x1 = 0;

			int y1 = (int) Math.floor(row * getCellHeight());

			int x2 = width;

			int y2 = y1;

			g.drawLine(x1, y1, x2, y2);
		}

		// draw the vertical grid lines
		for(int col = 0; col <= numCols; col++)
		{
			int x1 = (int) Math.floor(col * getCellWidth());

			int y1 = 0;

			int x2 = x1;

			int y2 = height;

			g.drawLine(x1, y1, x2, y2);
		}
	}

	/**
	 * Draws the color array on an image and then onto the graphics. Very fast,
	 * but can only be used if the cells are not represented by special shapes.
	 * 
	 * @param row
	 *            The row where the image should be drawn.
	 */
	private void drawFastColorArrayOnGraphics(int row)
	{
		// keep track of the last row in the array that was drawn
		lastRowDrawn = row;

		// make the off-screen image display the array at the specified row
		// (each cell gets one pixel).
		img.setRGB(0, row, numCols, 1, cellRGBData, 0, numCols);

		if(!CAConstants.MAC_OS || cells[0].getGeneration() != 0)
		{
			// now rescale to the correct size (so each cell occupies multiple
			// pixels). This rescaling can slow display times by a factor of 5.
			offScreenGraphics.drawImage(img, 0, 0, width, height, 0, 0,
					numCols, numRows, null);
		}
		else
		{
			// FOR MACS:
			//
			// Oh, this is painful. getScaledInstance() is VERY slow, but the
			// faster drawImage() used above just doesn't always work on my MAC.
			// In particular, when redrawing an image, the MAC seems to cache
			// the image (or the rendering hints), and it ignores the rendering
			// hints that I have set elsewhere. The result is a really
			// horrible-looking scaled image. So this is the only way to
			// ensure fast graphics with decent looking scaled images.
			//
			// Fortunately, this code is only necessary in special cases, like
			// when the simulation first starts, or when the size is rescaled
			// (both cases where the image seems to be cached, beyond my
			// control).
			offScreenGraphics.drawImage(img.getScaledInstance(width, height,
					BufferedImage.SCALE_FAST), new AffineTransform(1f, 0f, 0f,
					1f, 0, 0), null);
		}

		// necessary to prevent tearing on linux and other OS
		Toolkit.getDefaultToolkit().sync();

		// this is 20 times faster, but it doesn't fill the display
		// offScreenGraphics.drawImage(img, 0, 0, this);
	}

	/**
	 * Draws a grid mesh on the graphics. Called by various methods in this
	 * class.
	 */
	protected void drawGrid()
	{
		if(menuAskedForGrid)
		{
			drawGrid(offScreenGraphics);
		}
	}

	/**
	 * Set the height and the width of the frame based on default values from
	 * CAGraphics.
	 */
	private void setDefaultHeightAndWidth()
	{
		// constants we'll need
		int max_height = CAFrame.MAX_CA_HEIGHT;
		int max_width = CAFrame.MAX_CA_WIDTH;

		// figure out how big the display panel should be
		int panelHeight;
		int panelWidth;
		int cellSize;
		if(numCols > numRows)
		{
			// scale the height to the width
			cellSize = (int) Math.ceil(((double) max_width / (double) numCols));
		}
		else
		{
			// scale the width to the height
			cellSize = (int) Math
					.ceil(((double) max_height / (double) numRows));
		}
		panelHeight = cellSize * numRows;
		panelWidth = cellSize * numCols;

		// if display panel is too big, then scale its dimensions to the max
		// allowed
		if(panelWidth > max_width)
		{
			// then set width to max allowed, and scale height to the width
			// (order is important -- do panelHeight first)
			panelHeight = (int) (((double) max_width / (double) panelWidth) * ((double) panelHeight));
			panelWidth = max_width;
		}

		if(panelHeight > max_height)
		{
			// then set height to max allowed, and scale width to the height
			// (order is important -- do panelWidth first)
			panelWidth = (int) (((double) max_height / (double) panelHeight) * ((double) panelWidth));
			panelHeight = max_height;
		}

		height = panelHeight;
		width = panelWidth;
	}

	/**
	 * Draws the single given cell onto an offscreen image. Draws the cell at
	 * the specified position. Note, that this does not actually draw the cell
	 * onto the component, but just onto an offscreen image.
	 * <p>
	 * This method is used by the LatticeMouseListener.
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
	public void drawCell(Cell cell, int xPos, int yPos)
	{
		// get the row and column corresponding to xPos and yPos
		Coordinate coordinate = getRowCol(xPos, yPos);
		int col = coordinate.getColumn();
		int row = coordinate.getRow();
		int cellRow = cell.getStateHistory().size() - 1;

		// get the number of initial states (rows) required for the rule
		int numInitialStates = cells[col].getRule()
				.getRequiredNumberOfGenerations();

		if(numInitialStates > 1)
		{
			if(lastGenerationDrawn < numRows)
			{
				if(row >= lastGenerationDrawn)
				{
					row = lastGenerationDrawn;
				}
				else if(row <= lastGenerationDrawn - (numInitialStates - 1))
				{
					row = lastGenerationDrawn - (numInitialStates - 1);
					cellRow = cell.getStateHistory().size() - 1
							- (numInitialStates - 1);
				}
				else
				{
					for(int i = 1; i < numInitialStates; i++)
					{
						if(row == lastGenerationDrawn - i)
						{
							row = lastGenerationDrawn - i;
							cellRow = cell.getStateHistory().size() - 1 - i;
						}
					}
				}
			}
			else if((lastRowDrawn >= numRows - 1)
					&& (lastGenerationDrawn > numRows))
			{
				if(row >= numRows - 1)
				{
					row = numRows - 1;
				}
				else if(row < numRows - (numInitialStates - 1))
				{
					row = (numRows - 1) - (numInitialStates - 1);
					cellRow = cell.getStateHistory().size() - 1
							- (numInitialStates - 1);
				}
				else
				{
					for(int i = 1; i < numInitialStates; i++)
					{
						if(row == (numRows - 1) - i)
						{
							row = (numRows - 1) - i;
							cellRow = cell.getStateHistory().size() - 1 - i;
						}
					}
				}
			}
			else if((numInitialStates > 1) && (lastRowDrawn < numRows - 1)
					&& (lastGenerationDrawn > numRows))
			{
				// this deals with the rare case that we have rewound but the
				// last generation drawn is greater than the number of rows AND
				// the last row that was drawn is above the bottom of the
				// lattice

				if(row >= lastRowDrawn)
				{
					row = lastRowDrawn;
					cellRow = cell.getStateHistory().size() - 1;
				}
				else if(row < lastRowDrawn - (numInitialStates - 1))
				{
					row = lastRowDrawn - (numInitialStates - 1);
					cellRow = cell.getStateHistory().size() - 1
							- (numInitialStates - 1);
				}
				else
				{
					for(int i = 1; i < numInitialStates; i++)
					{
						if(row == lastRowDrawn - i)
						{
							row = lastRowDrawn - i;
							cellRow = cell.getStateHistory().size() - 1 - i;
						}
					}
				}
			}
		}
		else if(lastGenerationDrawn <= numRows - 1)
		{
			// we just want the row to be the last row
			row = lastGenerationDrawn;
		}
		else
		{
			row = numRows - 1;
		}

		// make sure we don't draw below the last drawn row. This doesn't work
		// when have multiple lines of initial conditions, EXCEPT, that we don't
		// draw with the setColorPixel for rules with multiple initial
		// conditions.
		if((lastRowDrawn != -1) && (lastRowDrawn < row))
		{
			// if not enough data, then don't draw so many rows
			row = lastRowDrawn;
		}

		// draw on the image (using the quicker colorArray, if possible)
		if(useColorArray)
		{
			// only one line of initial conditions
			if(numInitialStates == 1)
			{
				// set a pixel on the color array
				synchronized(cell)
				{
					setColorPixel(col, getColor(cell, cellRow, col).getRGB());
				}

				// now draw that array on the graphics (quick)
				drawFastColorArrayOnGraphics(row);
			}
		}
		else
		{
			int lastRow = lastRowDrawn;

			drawSingleCellWithErase(row, col, cellRow, cell);

			// don't let this mouse drawing event change the last row drawn.
			// Otherwise, we might be drawing on one row of the lattice, and
			// forever after, that will be the row that gets drawn. This is only
			// necessary with rules that have multiple initial states -- in
			// which case useColorArray will be false.
			lastRowDrawn = lastRow;
		}

		drawGrid();
	}

	/**
	 * Draw a default shape for the lattice. This should be a quickly drawn
	 * shape. The graphics method fillRect() is relatively quick and is a good
	 * choice. Note that this method is only called when the CellStateView
	 * returns null for the shape to draw -- hence this is only a backup and a
	 * default. <br>
	 * This method can be used intentionally for speed. (When drawing a Shape
	 * would take too long, have the CellStateView return null for
	 * getDisplayShape(), then this method will draw quickly.)
	 * 
	 * @param row
	 *            The row position on the lattice.
	 * @param col
	 *            The column position on the lattice.
	 * @param xPos
	 *            The x-axis pixel position of the cell.
	 * @param yPos
	 *            The y-axis pixel position of the cell.
	 * @param cell
	 *            The cell being drawn.
	 * @param g
	 *            The graphics object onto which the default shape should be
	 *            drawn.
	 */
	public void drawDefaultShapeOnGraphics(int row, int col, int xPos,
			int yPos, Cell cell, Graphics2D g)
	{
		if(yPos >= height)
		{
			yPos = height - 1;
		}

		// fillRect() is relatively fast!
		g.fillRect(xPos, yPos, getCellWidthInPixels(row, col),
				getCellHeightInPixels(row, col));
	}

	/**
	 * Draw a single cell onto the buffered graphics (does not erase any
	 * previous drawing).
	 * 
	 * @param row
	 *            The cell's row position for display.
	 * @param col
	 *            The cell's column position for display.
	 * @param cellRow
	 *            The cell's state history position.
	 * @param cell
	 *            The cell being drawn.
	 */
	public void drawSingleCell(int row, int col, int cellRow, Cell cell)
	{
		// get the position of the new shape
		int xPos = getCellXCoordinate(row, col);
		int yPos = getCellYCoordinate(row, col);

		// get the new shape
		Shape shape = getShape(cell, cellRow, col);

		// get the color of the new shape
		Color cellColor = getColor(cell, cellRow, col);
		if(cellColor == null)
		{
			cellColor = ColorScheme.EMPTY_COLOR;
		}

		if(shape != null)
		{
			// Prevents a mouse listener call from interfering with a lattice
			// call to this same code. Otherwise, can get confused about the
			// position of the shape and draw it in two places.
			synchronized(offScreenGraphics)
			{
				// only reset the color if have to (expensive operation)
				Color color = offScreenGraphics.getColor();
				if(cellColor != color)
				{
					// reset the color
					color = cellColor;
					offScreenGraphics.setColor(color);
				}

				// set the line thickness
				Stroke defaultStroke = offScreenGraphics.getStroke();
				Stroke stroke = Cell.getView().getStroke(cell.getState(),
						this.getCellWidthInPixels(row, col),
						this.getCellHeightInPixels(row, col),
						new Coordinate(row, col));
				if(stroke != null)
				{
					offScreenGraphics.setStroke(stroke);
				}

				// translate the shape to the correct position
				AffineTransform scalingTransform = AffineTransform
						.getTranslateInstance(xPos, yPos);
				shape = scalingTransform.createTransformedShape(shape);

				// paint the shape (slow)
				offScreenGraphics.draw(shape);
				offScreenGraphics.fill(shape);

				// reset the line thickness to the default (but only if we reset
				// the stroke in the first place -- so we really do want to
				// check for stroke != null)
				if(stroke != null)
				{
					offScreenGraphics.setStroke(defaultStroke);
				}
			}
		}
		else
		{
			// don't draw if the color is the empty color -- no sense since
			// that's the background anyway. Unless forced to draw the empty
			// color (for example by a mouse drawing that cell).
			if((cellHeight > 1.0)
					|| (translated || !cellColor
							.equals(ColorScheme.EMPTY_COLOR)))
			{
				// only reset the color if have to (expensive operation)
				Color color = offScreenGraphics.getColor();
				if(cellColor != color)
				{
					// reset the color
					color = cellColor;
					offScreenGraphics.setColor(color);
				}

				// use default shape for lattice
				drawDefaultShapeOnGraphics(row, col, xPos, yPos, cell,
						offScreenGraphics);
			}
		}

		// Set the previous shape to the new shape.
		// We do this now, after the AffineTransformation occurred, so that the
		// shape is positioned correctly (and we don't have to transform when
		// erasing the previous shape).
		previousShape[row][col] = shape;

		lastRowDrawn = row;
	}

	/**
	 * Draw a single cell onto the buffered graphics, but first erases any
	 * previous drawing.
	 * 
	 * @param row
	 *            The cell's row position for display.
	 * @param col
	 *            The cell's column position for display.
	 * @param cellRow
	 *            The cell's state history position.
	 * @param cell
	 *            The cell being drawn.
	 */
	public void drawSingleCellWithErase(int row, int col, int cellRow, Cell cell)
	{
		// erase the previous shape at this position
		if(previousShape[row][col] != null)
		{
			// Prevents a mouse listener call from interfering with a lattice
			// call to this same code. Otherwise, can get confused about the
			// position of the shape and draw it in two places.
			synchronized(offScreenGraphics)
			{
				// only reset the color if have to (expensive operation)
				Color color = offScreenGraphics.getColor();
				if(color != ColorScheme.EMPTY_COLOR)
				{
					// reset the color
					color = ColorScheme.EMPTY_COLOR;
					offScreenGraphics.setColor(color);
				}

				// set the line thickness
				Stroke defaultStroke = offScreenGraphics.getStroke();
				Stroke stroke = Cell.getView().getStroke(cell.getState(),
						this.getCellWidthInPixels(row, col),
						this.getCellHeightInPixels(row, col),
						new Coordinate(row, col));
				if(stroke != null)
				{
					offScreenGraphics.setStroke(stroke);
				}

				// paint the shape (slow), but in the empty color.
				offScreenGraphics.draw(previousShape[row][col]);
				try
				{
					// this handles a Java bug
					offScreenGraphics.fill(previousShape[row][col]);
				}
				catch(Exception e)
				{
					// ignore
				}

				// reset the line thickness to the default (but only if we reset
				// the stroke in the first place -- so we really do want to
				// check for stroke != null)
				if(stroke != null)
				{
					offScreenGraphics.setStroke(defaultStroke);
				}
			}
		}

		drawSingleCell(row, col, cellRow, cell);
	}

	/**
	 * The height of the bounding rectangle for each cell's graphics (when
	 * displayed on the panel).
	 * 
	 * @return The height.
	 */
	public double getCellHeight()
	{
		return cellHeight;
	}

	/**
	 * The height of the bounding rectangle for each cell's graphics (when
	 * displayed on the panel) in integer pixels. The number of pixels per cell
	 * should never be less that 1, or it will not display.
	 * 
	 * @param row
	 *            The row position on the lattice.
	 * @param col
	 *            The column position on the lattice.
	 * @return The height.
	 */
	public int getCellHeightInPixels(int row, int col)
	{
		return rectHeight;
	}

	/**
	 * Return the cell displayed at the current cursor position xPos and YPos.
	 * This method is useful, for example, when we want to change the appearance
	 * or content of cells (with a mouse listener). This method is currently
	 * used to draw on the lattice.
	 * 
	 * @param xPos
	 *            The horizontal coordinate of the mouse.
	 * @param yPos
	 *            The vertical coordinate of the mouse.
	 * @return The cell displayed at the position xPos, yPos on the graphics, or
	 *         null if there is no cell in that location.
	 */
	public Cell getCellUnderCursor(int xPos, int yPos)
	{
		// transform to a cell's array index
		int col = (int) Math.floor((double) xPos / cellWidth);

		// just in case
		if((col >= numCols) || (col < 0))
		{
			return null;
		}

		return cells[col];
	}

	/**
	 * The height of the bounding rectangle for each cell's graphics (when
	 * displayed on the panel) in integer pixels. The number of pixels per cell
	 * should never be less that 1, or it will not display.
	 * 
	 * @param row
	 *            The row position on the lattice.
	 * @param col
	 *            The column position on the lattice.
	 * @return The width.
	 */
	public int getCellWidthInPixels(int row, int col)
	{
		return rectWidth;
	}

	/**
	 * The width of the bounding rectangle for each cell's graphics (when
	 * displayed on the panel).
	 * 
	 * @return The width.
	 */
	public double getCellWidth()
	{
		return cellWidth;
	}

	/**
	 * Get the X coordinate of the center of the cell.
	 * 
	 * @param row
	 *            The cell's row position.
	 * @param col
	 *            The cell's column position.
	 * @return The X coordinate of the center of the cell.
	 */
	public int getCellXCoordinate(int row, int col)
	{
		int xPos = (int) Math.ceil(col * getCellWidth());

		return xPos;
	}

	/**
	 * Get the Y coordinate of the center of the cell.
	 * 
	 * @param row
	 *            The cell's row position.
	 * @param col
	 *            The cell's column position.
	 * @return The Y coordinate of the center of the cell.
	 */
	public int getCellYCoordinate(int row, int col)
	{
		int yPos = (int) Math.ceil(row * getCellHeight());

		return yPos;
	}

	/**
	 * Return the generation of the cell displayed at the current cursor
	 * position xPos and yPos. Returns the current generation unless directly
	 * over a previous generation that is one of the states required as an
	 * initial condition for the next row. The number of rows used as initial
	 * conditions is given by the rule as getRequiredNumberOfGenerations().
	 * 
	 * @param xPos
	 *            The horizontal coordinate of the mouse.
	 * @param yPos
	 *            The vertical coordinate of the mouse.
	 * @return The generation of the cell displayed at the position xPos, yPos
	 *         on the graphics. May be -1 if there is no cell under the cursor.
	 */
	public int getGenerationUnderCursor(int xPos, int yPos)
	{
		int lastGeneration = lastGenerationDrawn;

		// value to return
		int generationOfCell = lastGeneration;

		// transform to a cell's array index
		int row = (int) Math.floor((double) yPos / cellHeight);
		int col = (int) Math.floor((double) xPos / cellWidth);

		// just in case
		if((col > numCols - 1) || (col < 0))
		{
			return -1;
		}

		// get the number of initial states (rows) required for the rule
		int numInitialStates = cells[col].getRule()
				.getRequiredNumberOfGenerations();

		// make it easier to draw on the last row by allowing cursor to be
		// out-of-bounds below the lattice
		if(row >= numRows)
		{
			return lastGeneration;
		}
		else if(row < 0)
		{
			return 0;
		}

		// otherwise, if not out-of-bounds...
		if(numInitialStates > 1)
		{
			if(lastGeneration < numRows)
			{
				if(row >= lastGeneration)
				{
					generationOfCell = lastGeneration;
				}
				else if(row <= lastGeneration - (numInitialStates - 1))
				{
					generationOfCell = lastGeneration - (numInitialStates - 1);
				}
				else
				{
					for(int i = 1; i < numInitialStates; i++)
					{
						if(row == lastGeneration - i)
						{
							generationOfCell = lastGeneration - i;
						}
					}
				}
			}
			else if((lastGenerationDrawn >= numRows)
					&& (lastRowDrawn == numRows - 1))
			{
				if(row >= numRows - 1)
				{
					generationOfCell = lastGeneration;
				}
				else if(row < numRows - (numInitialStates - 1))
				{
					generationOfCell = lastGeneration - (numInitialStates - 1);
				}
				else
				{
					for(int i = 1; i < numInitialStates; i++)
					{
						if(row == (numRows - 1) - i)
						{
							generationOfCell = lastGeneration - i;
						}
					}
				}
			}
			else if((lastGenerationDrawn >= numRows)
					&& (lastRowDrawn < numRows - 1))
			{
				// this deals with the rare case that we have rewound but the
				// lasst generation drawn is greater than the number of rows AND
				// the last row that was drawn is above the bottom of the
				// lattice

				if(row >= lastRowDrawn)
				{
					generationOfCell = lastGeneration;
				}
				else if(row <= lastRowDrawn - (numInitialStates - 1))
				{
					generationOfCell = lastGeneration - (numInitialStates - 1);
				}
				else
				{
					for(int i = 1; i < numInitialStates; i++)
					{
						if(row == (lastRowDrawn - 1) - i)
						{
							generationOfCell = lastGeneration - i;
						}
					}
				}
			}
		}

		// be safe
		if(cells[0].getGeneration() < generationOfCell)
		{
			generationOfCell = cells[0].getGeneration();
		}

		return generationOfCell;
	}

	/**
	 * The number of columns displayed on the panel.
	 * 
	 * @return The number of CA columns.
	 */
	public int getNumColumns()
	{
		return numCols;
	}

	/**
	 * The number of rows displayed on the panel.
	 * 
	 * @return The number of CA rows.
	 */
	public int getNumRows()
	{
		return numRows;
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
	public Coordinate getRowCol(int xPos, int yPos)
	{
		// transform to a cell's array indices
		int row = (int) Math.floor((double) yPos / cellWidth);
		int col = (int) Math.floor((double) xPos / cellHeight);

		// // just in case
		// if(row >= numRows)
		// {
		// row = numRows - 1;
		// }
		//
		// // just in case
		// if(col >= numCols)
		// {
		// col = numCols - 1;
		// }

		return new Coordinate(row, col);
	}

	/**
	 * Adds a picture to the panel.
	 * <p>
	 * This method is automatically called whenever the window is resized or
	 * otherwise altered. You can force it to be called by using the
	 * <code>repaint</code> method of the encompassing JFrame or JComponent.
	 * Never call this method directly (or the Graphics object may not be
	 * specified properly).
	 */
	public void draw(Graphics g)
	{
		// all along, the offScreenGraphics were actually updating this
		// image (that's where the offScreenGraphics came from -- the
		// offScreenImage).
		g.drawImage(offScreenImage, 0, 0, this);
	}

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
	 * @param height
	 *            The height the rectangle that will be drawn.
	 */
	public void draw(Graphics2D g, int upperLeftX, int upperLeftY, int width,
			int height)
	{
		try
		{
			// all along, the offScreenGraphics were actually updating this
			// image (that's where the offScreenGraphics came from -- the
			// offScreenImage).
			g.drawImage(offScreenImage.getSubimage(upperLeftX, upperLeftY,
					width, height), 0, 0, this);
		}
		catch(Exception e)
		{
			// tried to draw a rectangle that's outside the image area. So just
			// draw the whole image.
			g.drawImage(offScreenImage, 0, 0, this);
		}
	}

	/**
	 * Adds a picture to the background image. This method is called by
	 * CAMenuBar.
	 */
	public void drawLattice()
	{
		drawLattice(lattice);
	}

	// private static long totalTime = 0;

	// private static int numGenerations = 1;

	/**
	 * Add a picture to the background image. This method is called by CAFrame.
	 * 
	 * @param lattice
	 *            The CA lattice.
	 */
	public void drawLattice(Lattice lattice)
	{
		// long startTime = System.nanoTime();

		// The CA graphics are updated every "displayStep" number of time steps.
		int displayStep = CurrentProperties.getInstance().getDisplayStep();

		// the current generation
		int thisGeneration = lattice.getCells()[0].getGeneration();

		// if displaying every nth step, then I'll need to redraw the entire
		// grid. Otherwise, I can draw each line (with an algorithm that works
		// faster for that situation).
		if(displayStep != 1
				|| (previousDisplayGeneration != thisGeneration - 1))
		{
			redraw();
		}
		else
		{
			// Can display two different ways. If the cell's have no
			// special view/display shape, then is 2 times faster to use
			// the color array from OneDimensionalLatticeView. (I benchmarked
			// the times.) For really large and really small lattices, the
			// differences aren't very big.
			//
			// ACTUALLY, latest benchmarks disagree. So I've left the code, but
			// useColorArray() always returns false. Without colorArray is 30 to
			// 85% faster depending on the lattice size.
			//
			// Every 100 generations make sure...
			useColorArray = useColorArray();

			if(useColorArray && runningAverage <= 1)
			{
				int generation = cells[0].getGeneration();

				// draw the rows without translating upwards, until reach
				// the bottom of the grid
				if(cells[0].getStateHistory().size() <= getNumRows())
				{
					int cellRow = cells[0].getStateHistory().size() - 1;

					drawFastColorArrayOnGraphics(cellRow);
				}
				else
				{
					// translate up by one pixel (this translates the unscaled
					// image)
					imgGraphics.drawImage(img, null, 0, -1);

					// and draw the new row
					drawFastColorArrayOnGraphics(getNumRows() - 1);
				}

				lastGenerationDrawn = generation;
			}
			else
			{
				// get the array of cells from the lattice
				Iterator iterator = lattice.iterator();
				for(int col = 0; col < getNumColumns(); col++)
				{
					// this cell[col]
					Cell cell = (Cell) iterator.next();
					int generation = cell.getGeneration();

					int stateHistorySize = cell.getStateHistory().size();
					int cellRow = stateHistorySize - 1;
					if(Cell.statesStored < getNumRows())
					{
						// this is for situations where we create really large
						// lattices and can't store all that info. The cell only
						// keeps a couple of time steps.
						if(generation == lastGenerationDrawn)
						{
							cellRow = lastGenerationDrawn;
							stateHistorySize = lastGenerationDrawn + 1;
						}
						else
						{
							// this will only happen to the first cell of each
							// row
							cellRow = lastGenerationDrawn + 1;
							stateHistorySize = lastGenerationDrawn + 2;
						}

						// just in case -- happens with the slice analysis for
						// example
						if(cellRow < 0)
						{
							cellRow = 0;
						}
					}

					// draw the rows without translating upwards, until reach
					// the bottom of the grid
					if(stateHistorySize <= getNumRows())
					{
						// draw the new row
						drawSingleCell(cellRow, col, cell.getStateHistory()
								.size() - 1, cell);

						// marks this as the last generation drawn
						lastGenerationDrawn = generation;
					}
					else
					{
						// Translate the image up one row, before drawing the
						// new row. Only check this once per row (hence checking
						// the generation).
						if(lastGenerationDrawn < generation)
						{
							translateUp();

							// indicates that we have already translated up for
							// this generation
							lastGenerationDrawn = generation;
						}

						int displayRow = getNumRows() - 1;

						drawSingleCellWithErase(displayRow, col, cell
								.getStateHistory().size() - 1, cell);
					}
				}
			}

			// draw gridlines (if they are turned on)
			drawGrid();

			// // to run this benchmarking, uncomment the static variables
			// // defined just before this method (and at the beginning of the
			// // method)
			// long endTime = System.nanoTime();
			// totalTime += (endTime - startTime);
			// double avgTime = totalTime / (double) numGenerations;
			// System.out.println("OneDimLatPanel: avgTimeElapsed = " +
			// avgTime);
			// numGenerations++;
		}

		// reset so that next time we know whether we were drawing on the
		// panel at every generation or not
		previousDisplayGeneration = thisGeneration;

		// draw as a frame in the movie (if a movie is open and being
		// created)
		if(MovieMaker.isOpen())
		{
			MovieMaker.writeFrame(offScreenImage);
		}
	}

	/**
	 * Redraws the lattice from the top.
	 * <p>
	 * WARNING: Due to poor abstraction, the SliceAnalysisOneDimView.redraw()
	 * may need to be modified when this method is modified. Someday I'll fix
	 * that.
	 */
	public void redraw()
	{
		// necessary to get averaged colors correct
		redrawing = true;

		// Reset the colors, in case they changed (e.g., via the menu). Only do
		// this if the color array is being used.
		if(useColorArray)
		{
			// reset the background, in case new colors were selected
			imgGraphics.setColor(ColorScheme.EMPTY_COLOR);
			imgGraphics.fillRect(0, 0, width, height);

			int currentGeneration = cells[0].getGeneration();

			// the number of rows to draw
			int rows = numRows;
			if(currentGeneration < numRows)
			{
				rows = currentGeneration + 1;
			}

			// and now check there is enough data to draw all those rows
			if(cells[0].getStateHistory().size() < rows)
			{
				// if not enough data, then don't draw so many rows
				rows = cells[0].getStateHistory().size();
			}

			// draw each row
			for(int row = 0; row < rows; row++)
			{
				// keep track of cell colors in an array (for fast-displaying)
				for(int col = 0; col < numCols; col++)
				{
					setColorPixel(col, Cell.getView().getDisplayColor(
							cells[col].getState(currentGeneration - rows + 1
									+ row), null, cells[col].getCoordinate())
							.getRGB());
				}

				drawFastColorArrayOnGraphics(row);
			}

			// WHY DID I HAVE THE FOLLOWING? CAUSES PROBLEMS IF REWIND THE
			// SIMULATION AND currentGeneration is on a row greater than the
			// row being drawn. DON'T SEE WHAT THIS SOLVED? But I'll leave it to
			// be safe, in case I want it back later.
			//
			// keep track of cell colors in last row
			// for(int col = 0; col < numCols; col++)
			// {
			// Object[] parameters = new Object[] {new Integer(0),
			// new Integer(col)};
			// setColorPixel(col, cells[col].getState().getDisplayColor(
			// parameters).getRGB());
			// }
			//
			// if(currentGeneration < numRows)
			// {
			// drawFastColorArrayOnGraphics(currentGeneration);
			// }

			// needed for drawing with the mouse
			lastGenerationDrawn = currentGeneration;
		}
		else
		{
			// reset the background, in case new colors were selected
			offScreenGraphics.setColor(ColorScheme.EMPTY_COLOR);
			offScreenGraphics.fillRect(0, 0, width, height);

			// get the number of image rows to display (this isn't the
			// generations)
			Iterator iterator = lattice.iterator();
			Cell cell = (Cell) iterator.next();
			int historySize = cell.getStateHistory().size();
			int maxRow = historySize;
			if(historySize > getNumRows())
			{
				maxRow = getNumRows();
			}

			// get the 2-d array of cells from the lattice
			iterator = lattice.iterator();
			for(int col = 0; col < getNumColumns(); col++)
			{
				// this cell[col]
				cell = (Cell) iterator.next();

				for(int row = 0; row < maxRow; row++)
				{
					// get the position in the state history
					int cellHistoryPosition = historySize - maxRow + row;

					// draw the row
					drawSingleCell(row, col, cellHistoryPosition, cell);
					// drawSingleCell(row, col, row, cell);
				}
			}

			// the drawLattice(Lattice) method will need to know this. Helps it
			// decide whether the image needs to scroll upward or not.
			lastGenerationDrawn = cell.getGeneration();
		}

		// draw gridlines (if they are turned on)
		drawGrid();

		// necessary to get averaged colors correct
		redrawing = false;
	}

	/**
	 * Gets the color of a state. If the user has selected "average", then this
	 * will be the average color of many states and or state histories.
	 * 
	 * @return The color.
	 */
	protected Color getColor(Cell cell, int row, int col)
	{
		// The color we will return
		Color color = null;

		// get the history of states for the cell
		FiniteArrayList<CellState> history = cell.getStateHistory();
		int historyLength = history.size();

		// get the running average
		try
		{
			if(runningAverage > 1 && historyLength > 1)
			{
				// the number of colors averaged together
				int numColors = 0;

				double red = 0.0;
				double green = 0.0;
				double blue = 0.0;

				int lastRowToAverage = row - runningAverage;
				if(lastRowToAverage < 0 && !redrawing)
				{
					row = historyLength - 1;
				}

				for(int n = row; (n > lastRowToAverage) && (n >= 0); n--)
				{
					Color stateColor = null;
					CellState state = history.get(n);
					if(state != null)
					{
						stateColor = Cell.getView().getUntaggedColor(state,
								null, cell.getCoordinate());
					}
					else
					{
						// Get here if that cell isn't saving the state for that
						// generation. Give it a default.
						stateColor = ColorScheme.EMPTY_COLOR;
					}
					red += stateColor.getRed();
					green += stateColor.getGreen();
					blue += stateColor.getBlue();

					numColors++;
				}

				// The state history may not be old enough to have
				// runningAverage, so divide by numColors
				red /= (double) numColors;
				green /= (double) numColors;
				blue /= (double) numColors;
				color = new Color((int) Math.round(red), (int) Math
						.round(green), (int) Math.round(blue));

				// if the current state is tagged, then tag the color
				CellState currentState = history.get(row);
				if(currentState.isTagged())
				{
					// get the tagging color
					Color taggingColor = CellStateView.colorScheme
							.getTaggedColor(currentState.getTaggingObject());

					// tag the color
					color = Cell.getView().modifyColorWithTaggedColor(color,
							taggingColor);
				}
			}
			else
			{
				CellState cellState = history.get(row);

				if(cellState != null)
				{
					color = Cell.getView().getDisplayColor(cellState, null,
							cell.getCoordinate());
				}
				else
				{
					// Get here if that cell isn't saving the state for that
					// generation. Give it a default of the empty color.
					color = ColorScheme.EMPTY_COLOR;
				}
			}
		}
		catch(Exception e)
		{
			// there was an error, so set the color to empty.
			color = ColorScheme.EMPTY_COLOR;
		}

		return color;
	}

	/**
	 * Gets the display shape for a state. If the user has selected "average",
	 * then this will be the average shape of many states and or state
	 * histories.
	 * 
	 * @return The display shape for the cell.
	 */
	private Shape getShape(Cell cell, int row, int col)
	{
		// The shape we will return
		Shape shape = null;

		// Parameters for the CellStateView (helps tell it how to display). In
		// this case, the row and col are passed in because the shape might
		// change with the position (for example, as in the triangular lattice
		// shape).
		Coordinate rowAndCol = new Coordinate(row, col);

		// get the history of states for the cell
		FiniteArrayList<CellState> history = cell.getStateHistory();
		int historyLength = history.size();

		// get the running average
		if(runningAverage > 1 && historyLength > 1)
		{
			ArrayList<CellState> stateList = new ArrayList<CellState>();
			for(int n = historyLength - 1; (n > historyLength - 1
					- runningAverage)
					&& (n >= 0); n--)
			{
				stateList.add(history.get(n));
			}

			if(stateList.size() > 0)
			{
				Object[] objectStates = stateList.toArray();

				CellState[] states = new CellState[stateList.size()];
				for(int i = 0; i < stateList.size(); i++)
				{
					states[i] = (CellState) objectStates[i];
				}

				shape = Cell.getView().getAverageDisplayShape(states,
						rectWidth, rectHeight, rowAndCol);
			}
		}
		else
		{
			shape = Cell.getView().getDisplayShape(cell.getState(), rectWidth,
					rectHeight, rowAndCol);
		}

		return shape;
	}

	/**
	 * Change the size of the display panel by a constant scaling factor.
	 * 
	 * @param factor
	 *            The scaling factor.
	 */
	protected void rescalePanel(double factor)
	{
		lastGenerationDrawn = ((Cell) lattice.iterator().next())
				.getGeneration();

		// private local method that changes the size of all parameters
		setPanelSize();

		// now redraw
		redraw();
	}

	/**
	 * Translates the image upwards by one row
	 */
	protected void translateUp()
	{
		double cellsHeight = getCellHeight();

		// translate this amount
		int translateDistance = (int) Math.floor(cellsHeight);

		// translate up
		if(translateDistance > 0)
		{
			// this translation causes a flickering. Even if
			// the image itself is translated (without
			// drawing onto the graphics object), this
			// causes flicker. The drawImage method with no
			// translation does not flicker. And translating
			// large distances like translateDistance*30
			// does not cause flicker. That means the
			// problem is not with the image's
			// background transparency. Try
			// (1) triple buffering, or (2) translating the
			// graphics object instead of the image.
			offScreenGraphics.drawImage(offScreenImage, null, 0,
					-translateDistance);

			translated = true;
		}
		else
		{
			translated = false;
		}

		// translate a fractional amount (or equivalently a
		// fraction of the time!)
		double fraction = cellsHeight - translateDistance;

		// the running sum of "fractions of a pixel" that
		// need to be translated upwards. E.g., if
		// cellsHeight = 1.436, then every step accumulates
		// 0.436 extra pixels that need to be translated
		// upwards. If the accumlation is greater than 1,
		// then an extra translation of 1 pixel occurs, and
		// 1 is subtracted from the accumulation.
		accumulatedFractionalPixel += fraction;
		if(accumulatedFractionalPixel > 1)
		{
			// an extra translation is going to happen, so
			// subtract one.
			accumulatedFractionalPixel -= 1.0;

			// translate up by one pixel
			offScreenGraphics.drawImage(offScreenImage, null, 0, -1);
			// offScreenGraphics
			// .drawImage(offScreenImage, null, 0, 0);

			translated = true;
		}
	}
}

// THIS VERSION DRAWS EVERY N ROWS. But I left it in case I ever want to let the
// CA draw every nth line instead of every line. NOTE that this code does
// not yet work properly for the initial state of Reversible rules or
// for drawing with the mouse (on the correct rows) or for zooming.
//
// public void drawLattice(Lattice lattice)
// {
// // The CA are displayed every "displayStep" number of time steps. It is
// // needed here to figure out where to draw on the panel.
// int displayStep = Integer.parseInt(super.getProperties().getProperty(
// CAPropertyReader.DISPLAY_STEP));
//
// // get the array of cells from the lattice
// Iterator iterator = lattice.iterator();
// for(int col = 0; col < getNumColumns(); col++)
// {
// // this cell[col]
// Cell cell = (Cell) iterator.next();
// int generation = cell.getGeneration();
//
// // draw the rows without translating upwards, until reach the bottom
// // of the grid
// if((generation / displayStep < getNumRows()))
// // && (generation % displayStep == 0))
// {
// int cellRow = cell.getStateHistory().size() - 1;
//
// // integer division is correct
// int displayRow = generation / displayStep;
//
// // draw the new row
// drawSingleCell(displayRow, col, cellRow, cell);
//
// // marks this as the last generation drawn
// lastGenerationDrawn = generation;
// }
// else
// {
// // Translate the image up one row, before drawing the new row.
// // Only check this once per row (hence checking the generation).
// if(lastGenerationDrawn < generation)
// {
// double cellsHeight = getCellHeight();
//
// // translate this amount
// int translateDistance = (int) Math.floor(cellsHeight);
//
// // translate up
// if(translateDistance > 0)
// {
// // this translation causes a flickering. Even if the
// // image itself is translated (without drawing onto the
// // graphics object), this causes flicker. The drawImage
// // method with no translation does not flicker. And
// // translating large distances like translateDistance
// // *30 does not cause flicker. That means the problem
// // is not with the image's background transparency. Try
// // (1) triple buffering, or (2) translating the graphics
// // object instead of the image.
// offScreenGraphics.drawImage(offScreenImage, null, 0,
// -translateDistance);
//
// translated = true;
// }
// else
// {
// translated = false;
// }
//
// // translate a fractional amount (or equivalently a
// // fraction of the time!)
// double fraction = cellsHeight - translateDistance;
//
// // the running sum of "fractions of a pixel" that need
// // to be translated upwards. E.g., if cellsHeight =
// // 1.436, then every step accumulates 0.436 extra pixels
// // that need to be translated upwards. If the
// // accumlation is greater than 1, then an extra
// // translation of 1 pixel occurs, and 1 is subtracted
// // from the accumulation.
// accumulatedFractionalPixel += fraction;
// if(accumulatedFractionalPixel > 1)
// {
// // an extra translation is going to happen, so
// // subtract one.
// accumulatedFractionalPixel -= 1.0;
//
// // translate up by one pixel
// offScreenGraphics
// .drawImage(offScreenImage, null, 0, -1);
// // offScreenGraphics
// // .drawImage(offScreenImage, null, 0, 0);
//
// translated = true;
// }
//
// // indicates that we have already translated up for this
// // generation
// lastGenerationDrawn = generation;
// }
//
// int displayRow = getNumRows() - 1;
// int cellRow = cell.getStateHistory().size() - 1;
//
// drawSingleCellWithErase(displayRow, col, cellRow, cell);
// }
// }
//
// // draw gridlines (if they are turned on)
// drawGrid();
// }
// public void redraw()
// {
// // necessary to get averaged colors correct
// redrawing = true;
//
// // reset the background, in case new colors were selected
// offScreenGraphics.setColor(ColorScheme.EMPTY_COLOR);
// offScreenGraphics.fillRect(0, 0, width, height);
//
// // get the number of rows to display
// Iterator iterator = lattice.iterator();
// Cell cell = (Cell) iterator.next();
// int historySize = cell.getStateHistory().size();
// int maxRow = historySize;
// if(historySize > getNumRows())
// {
// maxRow = getNumRows();
// }
//
// // The CA are displayed every "displayStep" number of time steps. It is
// // needed here to figure out where to draw on the panel.
// int displayStep = Integer.parseInt(super.getProperties().getProperty(
// CAPropertyReader.DISPLAY_STEP));
//
// // get the 2-d array of cells from the lattice
// iterator = lattice.iterator();
// for(int col = 0; col < getNumColumns(); col++)
// {
// // this cell[col]
// cell = (Cell) iterator.next();
//
// for(int row = 0; row < maxRow; row++)
// {
// // only display if the row is one of the time steps that is
// // supposed to be displayed.
// if(row % displayStep == 0)
// {
// // draw the row
// drawSingleCell(row, col, row, cell);
// }
// }
// }
//
// // draw gridlines (if they are turned on)
// drawGrid();
//
// // necessary to get averaged colors correct
// redrawing = false;
// }

// THIS IS A VERSION THAT TRIES TO WORK WITH THE NUMBER OF ROWS DRAWN RATHER
// THAN THE GENERATION. UNSUCCESSFUL.
//
// public void drawLattice(Lattice lattice)
// {
// // get the array of cells from the lattice
// Iterator iterator = lattice.iterator();
// for(int col = 0; col < getNumColumns(); col++)
// {
// // this cell[col]
// Cell cell = (Cell) iterator.next();
// int generation = cell.getGeneration();
//
// // draw the rows without translating, until reach the bottom of the
// // grid
// if(generation < getNumRows())
// {
// int cellRow = cell.getStateHistory().size() - 1;
//
// int displayRow = generation;
//
// // draw the new row
// drawSingleCell(displayRow, col, cellRow, cell);
//
// // marks this as the last generation drawn
// lastGenerationDrawn = generation;
// }
// else
// {
// // Translate the image up one row, before drawing the new row.
// // Only check this once per row (hence checking the generation).
// if(lastGenerationDrawn < generation)
// {
// double cellsHeight = getCellHeight();
//
// // translate this amount
// int translateDistance = (int) Math.floor(cellsHeight);
//
// // translate up
// if(translateDistance > 0)
// {
// // this translation causes a flickering. Even if the
// // image itself is translated (without drawing onto the
// // graphics object), this causes flicker. The drawImage
// // method with no translation does not flicker. And
// // translating large distances like translateDistance
// // *30 does not cause flicker. That means the problem
// // is not with the image's background transparency. Try
// // (1) triple buffering, or (2) translating the graphics
// // object instead of the image.
// offScreenGraphics.drawImage(offScreenImage, null, 0,
// -translateDistance);
//
// translated = true;
// }
// else
// {
// translated = false;
// }
//
// // translate a fractional amount (or equivalently a
// // fraction of the time!)
// double fraction = cellsHeight - translateDistance;
//
// // the running sum of "fractions of a pixel" that need
// // to be translated upwards. E.g., if cellsHeight =
// // 1.436, then every step accumulates 0.436 extra pixels
// // that need to be translated upwards. If the
// // accumlation is greater than 1, then an extra
// // translation of 1 pixel occurs, and 1 is subtracted
// // from the accumulation.
// accumulatedFractionalPixel += fraction;
// if(accumulatedFractionalPixel > 1)
// {
// // an extra translation is going to happen, so
// // subtract one.
// accumulatedFractionalPixel -= 1.0;
//
// // translate up by one pixel
// offScreenGraphics
// .drawImage(offScreenImage, null, 0, -1);
// // offScreenGraphics
// // .drawImage(offScreenImage, null, 0, 0);
//
// translated = true;
// }
//
// // indicates that we have already translated up for this
// // generation
// lastGenerationDrawn = generation;
// }
//
// int displayRow = getNumRows() - 1;
// int cellRow = cell.getStateHistory().size() - 1;
//
// drawSingleCellWithErase(displayRow, col, cellRow, cell);
// }
// }
//
// // draw gridlines (if they are turned on)
// drawGrid();
// }

/*
 * // the last row that was drawn on the image (none at the beginning, and this //
 * will be incremented every time step) private int lastRowDrawn = -1; public
 * void drawLattice(Lattice lattice) { // get the array of cells from the
 * lattice Iterator iterator = lattice.iterator(); for(int col = 0; col <
 * getNumColumns(); col++) { // this cell[col] Cell cell = (Cell)
 * iterator.next(); int generation = cell.getGeneration(); // Translate the
 * image up one row, before drawing the new row. // Only do this once per row
 * (hence checking the generation). if((lastRowDrawn + 1 > getNumRows() - 1) &&
 * (lastGenerationDrawn < generation)) { double cellsHeight = getCellHeight(); //
 * translate this amount int translateDistance = (int) Math.floor(cellsHeight); //
 * translate up if(translateDistance > 0) { // this translation causes a
 * flickering. Even if the // image itself is translated (without drawing onto
 * the // graphics object), this causes flicker. The drawImage // method with no
 * translation does not flicker. And // translating large distances like
 * translateDistance // *30 does not cause flicker. That means the problem // is
 * not with the image's background transparency. Try // (1) triple buffering, or
 * (2) translating the graphics // object instead of the image.
 * offScreenGraphics.drawImage(offScreenImage, null, 0, -translateDistance);
 * translated = true; } else { translated = false; } // translate a fractional
 * amount (or equivalently a // fraction of the time!) double fraction =
 * cellsHeight - translateDistance; // the running sum of "fractions of a pixel"
 * that need // to be translated upwards. E.g., if cellsHeight = // 1.436, then
 * every step accumulates 0.436 extra pixels // that need to be translated
 * upwards. If the // accumlation is greater than 1, then an extra //
 * translation of 1 pixel occurs, and 1 is subtracted // from the accumulation.
 * accumulatedFractionalPixel += fraction; if(accumulatedFractionalPixel > 1) { //
 * an extra translation is going to happen, so // subtract one.
 * accumulatedFractionalPixel -= 1.0; // translate up by one pixel
 * offScreenGraphics.drawImage(offScreenImage, null, 0, -1); //
 * offScreenGraphics // .drawImage(offScreenImage, null, 0, 0); translated =
 * true; } // indicates that we have already translated up for this //
 * generation lastGenerationDrawn = generation; } int displayRow = getNumRows() -
 * 1; if(lastRowDrawn < getNumRows() - 1) { displayRow = lastRowDrawn + 1; } int
 * cellRow = cell.getStateHistory().size() - 1;
 * drawSingleCellWithErase(displayRow, col, cellRow, cell); // used when
 * deciding if should translate up for a given generation lastGenerationDrawn =
 * generation; } // draw gridlines (if they are turned on) drawGrid(); // we
 * drew another row lastRowDrawn++; }
 */