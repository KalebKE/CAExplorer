/*
 TwoDimensionalLatticeView -- a class within the Cellular Automaton Explorer. 
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
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.SquareLattice;
import cellularAutomata.lattice.TwoDimensionalLattice;
import cellularAutomata.movie.MovieMaker;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.PanelSize;
import cellularAutomata.util.dataStructures.FiniteArrayList;

/**
 * A convenience class for creating image panels that display the graphics for a
 * two-dimensional lattice.
 * 
 * @author David Bahr
 */
public abstract class TwoDimensionalLatticeView extends LatticeView
{
	// number of rows and columns in the CA
	private static int numRows;

	private static int numCols;

	// how many generations to average when displaying the cell's values
	private int runningAverage = 1;

	// use the "color array" graphics approach when true (see the method
	// drawLattice(Lattice)).
	private static boolean useColorArray = true;

	// off screen image that can be persistent (the offScreenGraphics come from
	// this image)
	private static BufferedImage offScreenImage = null;

	// used for macs that need special treatment of images when rescaling
	private boolean rescalingSize = false;

	// off screen image used to display the cellRGBData (for fast display)
	private BufferedImage img = null;

	// the cells on the 2-d lattice
	private Cell[][] cell = null;

	// off screen graphics object that can be persistent
	private static Graphics2D offScreenGraphics = null;

	// holds all the possible graphics configurations for the computer's
	// graphics card
	private GraphicsConfiguration graphicsConfiguration = null;

	// The CA lattice
	private Lattice lattice = null;

	// keeps track of whatever shape was last drawn at the given row and col
	private Shape[][] previousShape = null;

	/**
	 * Create a graphics panel for a lattice.
	 */
	public TwoDimensionalLatticeView(TwoDimensionalLattice lattice)
	{
		super();

		this.setIgnoreRepaint(true);

		// start out with a super small size -- the off screen image won't be
		// set until these are resized
		width = 1;
		height = 1;

		numRows = lattice.getHeight();
		numCols = lattice.getWidth();
		this.lattice = lattice;

		// setting the graphics configuration to match the current graphics card
		graphicsConfiguration = GraphicsEnvironment
				.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();

		// Initialize the array of cells.
		previousShape = new Shape[numRows][numCols];
		cell = new Cell[numRows][numCols];
		Iterator iterator = lattice.iterator();
		for(int row = 0; row < numRows; row++)
		{
			for(int col = 0; col < numCols; col++)
			{
				// get the 2-d array of cells from the lattice
				cell[row][col] = (Cell) iterator.next();
			}
		}

		// the number of generations that will be averaged for display
		runningAverage = CurrentProperties.getInstance().getRunningAverage();

		// no layout manager
		setLayout(null);

		// get the smallest possible width and height (with the proper ratio) so
		// that we create a buffered image that is as small as possible (used in
		// setPanelSize()). This saves lots of memory.
		PanelSize size = rescaleToMinimumWidthAndHeight();
		width = size.getWidth();
		height = size.getHeight();

		// set the size of the JPanel
		setPanelSize();

		// for fast display
		cellRGBData = new int[numRows * numCols];

		// keep track of cell colors in an array (for fast-displaying)
		for(int i = 0; i < numRows; i++)
		{
			for(int j = 0; j < numCols; j++)
			{
				setColorPixel(i * numCols + j, Cell.getView()
						.getDisplayColor(cell[i][j].getState(), null,
								cell[i][j].getCoordinate()).getRGB());
			}
		}

		// the image used to display the cellRGBData
		img = graphicsConfiguration.createCompatibleImage(numCols, numRows);
		img.setAccelerationPriority(1.0f);
	}

	/**
	 * Set parameters that determine the size of the graphics.
	 */
	private void setPanelSize()
	{
		// set the size of the JPanel
		setBounds(new Rectangle(0, 0, width, height));
		setMaximumSize(new Dimension(width, height));
		setMinimumSize(new Dimension(width, height));
		setPreferredSize(new Dimension(width, height));

		// JFrame frame = new JFrame(graphicsConfiguration);
		// System.out.println("TwoDimLatPanel: frame = "+frame);
		// // its faster to render to offscreen graphics
		// try
		// {
		// frame.createBufferStrategy(2);
		// }
		// catch(Exception e)
		// {
		// System.out.println(e);
		// }
		// BufferStrategy bufferStrategy = frame.getBufferStrategy();
		// offScreenGraphics = (Graphics2D) bufferStrategy.getDrawGraphics();
		// System.out.println("TwoDimLatPanel: offScreenGraphics =
		// "+offScreenGraphics);
		// offScreenImage = graphicsConfiguration.createCompatibleImage(width,
		// height, BufferedImage.TYPE_INT_RGB);
		// offScreenGraphics.setColor(ColorScheme.EMPTY_COLOR);
		// offScreenGraphics.fillRect(0, 0, width, height);

		// its faster to render to offscreen graphics
		offScreenImage = graphicsConfiguration.createCompatibleImage(width,
				height, BufferedImage.TYPE_INT_RGB);
		offScreenImage.setAccelerationPriority(1.0f);
		offScreenGraphics = offScreenImage.createGraphics();
		offScreenGraphics.setColor(ColorScheme.EMPTY_COLOR);
		offScreenGraphics.fillRect(0, 0, width, height);

		// This is very important, particularly on Macs. Higher quality
		// interpolations like bilinear and bicubic do a poor job of
		// scaling-up(!) in image size. Square edges will be blurred. This hint
		// keeps the interpolation fast and keeps square edges looking nice.
		offScreenGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

		// initializes the first time, and subsequently makes sure we don't try
		// to erase something that isn't there
		for(int row = 0; row < numRows; row++)
		{
			for(int col = 0; col < numCols; col++)
			{
				// initialize the array that keeps track of the previously drawn
				// shape
				previousShape[row][col] = null;
			}
		}
	}

	/**
	 * Change the size of the display panel by a constant scaling factor.
	 * 
	 * @param factor
	 *            The scaling factor.
	 */
	protected void rescalePanel(double factor)
	{
		// this is necessary because macs sometimes cache the image, and so I
		// have to be careful that it is drawn correctly (even when it isn't
		// technically being rescaled). See the method drawFastColorArray() for
		// more details.
		rescalingSize = true;

		// any size parameters that must be set in this class
		// (must come before setting parameters in the child class)
		setPanelSize();

		// any size parameters that must be set in the child class
		// (must come after setting parameters in this parent class)
		setSizeParameters();

		// now redraw
		drawLattice();

		rescalingSize = false;
	}

	/**
	 * Sets any size parameters that must be changed when zooming in or out on
	 * the panel.
	 * 
	 * @see cellularAutomata.lattice.view.TwoDimensionalLatticeView#resizePanel(double)
	 */
	protected abstract void setSizeParameters();

	/**
	 * Gets the color of a state. If the user has selected "average", then this
	 * will be the average color of many states and or state histories.
	 * 
	 * @return The cell state's color.
	 */
	private Color getColor(Cell cell, int row, int col)
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
				double red = 0.0;
				double green = 0.0;
				double blue = 0.0;
				for(int n = historyLength - 1; (n > historyLength - 1
						- runningAverage)
						&& (n >= 0); n--)
				{
					// untag the older cell states if necessary
					CellState state = history.get(n);

					Color stateColor = Cell.getView().getUntaggedColor(state,
							null, cell.getCoordinate());
					red += stateColor.getRed();
					green += stateColor.getGreen();
					blue += stateColor.getBlue();
				}

				// The state history may not be old enough to have
				// runningAverage
				int numColors = Math.min(runningAverage, historyLength);
				red /= (double) numColors;
				green /= (double) numColors;
				blue /= (double) numColors;

				color = new Color((int) Math.round(red), (int) Math
						.round(green), (int) Math.round(blue));

				// if the current state is tagged, then tag the color
				CellState currentState = history.get(historyLength - 1);
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
				color = Cell.getView().getDisplayColor(cell.getState(), null,
						cell.getCoordinate());
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
	 * Gets the display shape for a cell's state. If the user has selected
	 * "average", then this will be the average shape of many states and or
	 * state histories.
	 * 
	 * @param cell
	 *            The cell whose state is being displayed.
	 * @param row
	 *            The row position on the lattice.
	 * @param col
	 *            The column position on the lattice.
	 * @return The display shape for the cell.
	 */
	private Shape getShape(Cell cell, int row, int col)
	{
		// The shape we will return
		Shape shape = null;

		// Parameters for the CellStateView (helps tell it how to display). In
		// this case, the row and col are passed in because the display might
		// change with the position (as on a triangular lattice).
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
						getCellWidthInPixels(row, col),
						getCellHeightInPixels(row, col), rowAndCol);
			}
		}
		else
		{
			shape = Cell.getView().getDisplayShape(cell.getState(),
					getCellWidthInPixels(row, col),
					getCellHeightInPixels(row, col), rowAndCol);
		}

		return shape;
	}

	/**
	 * Adds a picture to the panel.
	 * <p>
	 * This method is automatically called (by my implementation of
	 * paintComponent() in the LatticeView class) whenever the window is resized
	 * or otherwise altered. You can force it to be called by using the
	 * <code>repaint</code> method of the encompassing JFrame or JComponent.
	 * Never call this method directly (or the Graphics object may not be
	 * specified properly).
	 */
	public final void draw(Graphics g)
	{
		// all along, the offScreenGraphics were actually updating this
		// image (that's where the offScreenGraphics came from -- the
		// offScreenImage).
		g.drawImage(offScreenImage, 0, 0, this);

		// necessary to prevent tearing on linux and other OS
		if(!CAConstants.WINDOWS_OS)
		{
			Toolkit.getDefaultToolkit().sync();
		}
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
					width, height), 0, 0, null);
		}
		catch(Exception e)
		{
			// tried to draw a rectangle that's outside the image area. So just
			// draw the whole image.
			g.drawImage(offScreenImage, 0, 0, null);
		}
	}

	/**
	 * Draws the color array on an image and then onto the graphics. Very fast,
	 * but can only be used if the cells are not represented by special shapes.
	 */
	private void drawFastColorArrayOnGraphics()
	{
		// make the off-screen image display the array (each cell gets one
		// pixel).
		img.setRGB(0, 0, numCols, numRows, cellRGBData, 0, numCols);

		if(!CAConstants.MAC_OS
				|| (!rescalingSize && cell[0][0].getGeneration() != 0))
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

		// this is 5 times faster, but it doesn't fill the display
		// offScreenGraphics.drawImage(img, 0, 0, this);
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
	 * @param g
	 *            The graphics object onto which the default shape should be
	 *            drawn.
	 */
	public abstract void drawDefaultShapeOnGraphics(int row, int col,
			Graphics2D g);

	/**
	 * Draws a grid mesh on the graphics. Child classes should implement an
	 * appropriate grid (square, hexagonal, triangular, or other). May do
	 * nothing, but this will confuse the user that selects the "draw grid"
	 * option on the menu.
	 */
	protected abstract void drawGrid(Graphics2D g);

	/**
	 * Draws a grid mesh on the graphics. Called by various methods in this
	 * class.
	 */
	private void drawGrid()
	{
		if(LatticeView.menuAskedForGrid)
		{
			drawGrid(offScreenGraphics);
		}
	}

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
	public void drawCell(Cell cell, int xPos, int yPos)
	{
		// get the row and column corresponding to xPos and yPos
		Coordinate coordinate = getRowCol(xPos, yPos);
		int row = coordinate.getRow();
		int col = coordinate.getColumn();

		// draw on the image (using the quicker colorArray, if possible)
		if(useColorArray)
		{
			// set a pixel on the color array
			setColorPixel(row * numCols + col, getColor(cell, row, col)
					.getRGB());

			// now draw that array on the graphics (quick)
			drawFastColorArrayOnGraphics();
		}
		else
		{
			// draw a shape onto the off-screen graphics
			drawSingleCellWithErase(row, col, cell);
		}

		drawGrid();
	}

	/**
	 * Adds a picture to the background image. This method is called by
	 * CAMenuBar.
	 */
	public final void drawLattice()
	{
		drawLattice(lattice);
	}

	/**
	 * Decides which graphics approach is faster.
	 * 
	 * @param lattice
	 *            The CA lattice.
	 * @return true of the faster approach uses the color array.
	 */
	private boolean benchMark(Lattice lattice)
	{
		boolean useColorArray = true;

		Coordinate rowAndCol = new Coordinate(0, 0);

		// Note that this assumes that when the first cell has a null shape,
		// then ALL cell states will have a null shape. See the
		// CellStateView.getDisplayShape() method for information on why all
		// cells must return either (1) all null, or (2) all non-null shapes.
		// Ignoring that method's warnings will mess up the next line of code
		// and cause odd display bugs (where shapes don't get displayed unless
		// there happens to be a shape at cells[0][0] when this method is
		// called).
		Shape shape = Cell.getView().getDisplayShape(cell[0][0].getState(), 10,
				10, rowAndCol);

		if((lattice instanceof SquareLattice) && (shape == null)
				&& (runningAverage <= 1))
		{
			useColorArray = true;

			// NO NEED TO CHECK THIS -- THE COLOR ARRAY APPROACH IS ALWAYS
			// FASTER WHEN IT CAN BE USED.
			//            
			// // Approach #1
			// long startTimeApproach1 = System.nanoTime();
			//
			// // make the image display the array with one pixel per cell
			// img.setRGB(0, 0, numCols, numRows, cellRGBData, 0, numCols);
			//
			// // now rescale to the correct size (so each cell occupies
			// multiple
			// // pixels)
			// offScreenGraphics.drawImage(img, 0, 0, width, height, 0, 0,
			// numCols - 1, numRows - 1, this);
			//
			// long elapsedTimeForApproach1 = System.nanoTime()
			// - startTimeApproach1;
			//
			// // Approach #2
			// long startTimeApproach2 = System.nanoTime();
			//
			// // get the 2-d array of cells from the lattice
			// Iterator iterator = lattice.iterator();
			// for(int row = 0; row < getNumRows(); row++)
			// {
			// for(int col = 0; col < getNumColumns(); col++)
			// {
			// // this cell[row][col]
			// Cell cell = (Cell) iterator.next();
			//
			// drawSingleCellWithErase(row, col, cell);
			// }
			// }
			//
			// long elapsedTimeForApproach2 = System.nanoTime()
			// - startTimeApproach1;
			//
			// if(elapsedTimeForApproach2 < elapsedTimeForApproach1)
			// {
			// useColorArray = false;
			// }

			// System.out.println("TwoDimLatPan: elapsedTimeForApproach1 = "
			// + elapsedTimeForApproach1);
			// System.out.println("TwoDimLatPan: elapsedTimeForApproach2 = "
			// + elapsedTimeForApproach2);
		}
		else
		{
			// can't use the color array approach
			useColorArray = false;
		}

		return useColorArray;
	}

	// private static long totalTime = 0;

	// private static int numGenerations = 1;

	/**
	 * Adds a picture to the background image. This method is called by
	 * CellularAutomata.
	 * 
	 * @param lattice
	 *            The CA lattice.
	 */
	public final void drawLattice(Lattice lattice)
	{
		// long startTime = System.nanoTime();

		// Can display two different ways. If is a square lattice with no
		// special view/display shape, then is 3 to 10 times faster to use the
		// color array from TwoDimensionalLatticeView. (I benchmarked the
		// times.)
		//
		// But that's for large lattices. Small lattices sometimes (rarely) do
		// better without the image rescaling.
		//
		// So every 100 generations this method is called, benchmark both and
		// choose the best.
		if(cell[0][0].getGeneration() % 100 == 0)
		{
			useColorArray = benchMark(lattice);
		}

		if(useColorArray)
		{
			// the fast display -- this creates a single pixel for each cell
			drawFastColorArrayOnGraphics();
		}
		else
		{
			// the slower but more general display method

			// get the 2-d array of cells from the lattice
			Iterator iterator = lattice.iterator();
			for(int row = 0; row < getNumRows(); row++)
			{
				for(int col = 0; col < getNumColumns(); col++)
				{
					// this cell[row][col]
					Cell cell = (Cell) iterator.next();

					drawSingleCellWithErase(row, col, cell);
				}
			}
		}

		// // to run this benchmarking, uncomment the static variables defined
		// // just before this method
		// long endTime = System.nanoTime();
		// totalTime += (endTime - startTime);
		// double avgTime = totalTime / (double) numGenerations;
		// System.out.println("TwoDimLatPanel: avgTimeElapsed = " + avgTime);
		// numGenerations++;

		drawGrid();

		// draw as a frame in the movie (if a movie is open and being
		// created)
		if(MovieMaker.isOpen())
		{
			MovieMaker.writeFrame(offScreenImage);
		}
	}

	/**
	 * Adds a picture to the background image without erasing the previous
	 * image. This method is called by CAMenuBar.
	 */
	// public void drawLatticeWithoutErase()
	// {
	// // get the 2-d array of cells from the lattice
	// Iterator iterator = lattice.iterator();
	// for(int row = 0; row < getNumRows(); row++)
	// {
	// for(int col = 0; col < getNumColumns(); col++)
	// {
	// // this cell[row][col]
	// Cell cell = (Cell) iterator.next();
	//
	// drawSingleCellWithErase(row, col, cell);
	// }
	// }
	//
	// drawGrid();
	// }
	/**
	 * Draw a single cell onto the buffered graphics (does not erase any
	 * previous drawing).
	 * 
	 * @param row
	 *            The cell's row position.
	 * @param col
	 *            The cell's column position.
	 * @param cell
	 *            The cell being drawn.
	 */
	public void drawSingleCell(int row, int col, Cell cell)
	{
		// get the position of the shape
		int xPos = getCellXCoordinate(row, col);
		int yPos = getCellYCoordinate(row, col);

		// get the new shape
		Shape shape = getShape(cell, row, col);

		// get the color of the new shape
		Color cellColor = getColor(cell, row, col);
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
				// offScreenGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				// RenderingHints.VALUE_ANTIALIAS_ON);

				// only reset the color if have to (expensive operation)
				Color color = offScreenGraphics.getColor();
				if(cellColor != color)
				{
					// reset the color
					color = cellColor;
					offScreenGraphics.setColor(color);
				}

				// translate the shape to the correct position
				AffineTransform scalingTransform = AffineTransform
						.getTranslateInstance(xPos, yPos);
				shape = scalingTransform.createTransformedShape(shape);

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

				// paint the shape (slow)
				offScreenGraphics.draw(shape);

				// handles a bug with displaying the lattice gas from the
				// jar. The bug doesn't exist from java versions 1.5 and
				// earlier.
				if(!CurrentProperties.getInstance().getRuleClassName()
						.contains("Lattice"))
				{
					// oddly, the bug is only a problem on larger lattices.
					// || (numRows <= 20 && numCols <= 20))

					offScreenGraphics.fill(shape);
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
		else
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
			drawDefaultShapeOnGraphics(row, col, offScreenGraphics);
		}

		// Set the previous shape to the new shape.
		// We do this now, after the AffineTransformation occurred, so that the
		// shape is positioned correctly (and we don't have to transform when
		// erasing the previous shape).
		previousShape[row][col] = shape;
	}

	/**
	 * Draw a single cell onto the buffered graphics, but first erases any
	 * previous drawing.
	 * 
	 * @param row
	 *            The cell's row position.
	 * @param col
	 *            The cell's column position.
	 * @param cell
	 *            The cell being drawn.
	 */
	public void drawSingleCellWithErase(int row, int col, Cell cell)
	{
		// erase the previous shape at this position
		if(previousShape[row][col] != null)
		{
			// Prevents a mouse listener call from interfering with a lattice
			// call to this same code. Otherwise, can get confused about the
			// position of the shape and draw it in two places.
			synchronized(offScreenGraphics)
			{
				// offScreenGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				// RenderingHints.VALUE_ANTIALIAS_ON);

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

				try
				{
					// if select a rule with a different shape *while* the
					// previous rule is still running, then can get an error
					// because previous[row][col] can be suddenly set to null.
					// This catches that.

					// paint the shape (slow), but in the empty color.
					offScreenGraphics.draw(previousShape[row][col]);

					// This also handles another bug with displaying the lattice
					// gas from the jar. The bug doesn't exist from java
					// versions 1.5 and earlier.
					if(!CurrentProperties.getInstance().getRuleClassName()
							.contains("Lattice"))
					{
						// oddly, the bug is only a problem on larger lattices.
						// || (numRows <= 20 && numCols <= 20))

						// this exception catching handles a Java bug
						offScreenGraphics.fill(previousShape[row][col]);
					}
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

		drawSingleCell(row, col, cell);
	}

	/**
	 * Get the two-dimensional array of cells represented by the lattice.
	 * 
	 * @return The two-dimensional array of cells.
	 */
	public Cell[][] getCellArray()
	{
		return cell;
	}

	/**
	 * The height of the bounding rectangle for each cell's graphics (when
	 * displayed on the panel).
	 * 
	 * @param row
	 *            The row position on the lattice.
	 * @param col
	 *            The column position on the lattice.
	 * @return The height.
	 */
	public abstract double getCellHeight(int row, int col);

	/**
	 * The width of the bounding rectangle for each cell's graphics (when
	 * displayed on the panel).
	 * 
	 * @param row
	 *            The row position on the lattice.
	 * @param col
	 *            The column position on the lattice.
	 * @return The width.
	 */
	public abstract double getCellWidth(int row, int col);

	/**
	 * Get the X coordinate of the center of the cell.
	 * 
	 * @param row
	 *            The cell's row position.
	 * @param col
	 *            The cell's column position.
	 * @return The X coordinate of the center of the cell.
	 */
	public abstract int getCellXCoordinate(int row, int col);

	/**
	 * Get the Y coordinate of the center of the cell.
	 * 
	 * @param row
	 *            The cell's row position.
	 * @param col
	 *            The cell's column position.
	 * @return The Y coordinate of the center of the cell.
	 */
	public abstract int getCellYCoordinate(int row, int col);

	/**
	 * Always returns the current generation.
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
		return cell[0][0].getGeneration();
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
	 * Redraws the lattice. Typically, just a call to drawLattice() along with
	 * resetting the color of the background.
	 */
	public void redraw()
	{
		// this is necessary because macs sometimes cache the image, and so I
		// have to be careful that it is drawn correctly (even when it isn't
		// technically being rescaled). See the method drawFastColorArray() for
		// more details.
		rescalingSize = true;

		// Reset the colors, in case they changed (e.g., via the menu). Only do
		// this if the color array is being used.
		if(useColorArray)
		{
			// keep track of cell colors in an array (for fast-displaying)
			for(int i = 0; i < numRows; i++)
			{
				for(int j = 0; j < numCols; j++)
				{
					setColorPixel(i * numCols + j, Cell.getView()
							.getDisplayColor(cell[i][j].getState(), null,
									cell[i][j].getCoordinate()).getRGB());
				}
			}
		}

		offScreenGraphics.setColor(ColorScheme.EMPTY_COLOR);
		offScreenGraphics.fillRect(0, 0, width, height);

		// This is very important, particularly on Macs. Higher quality
		// interpolations like bilinear and bicubic do a poor job of
		// scaling-up(!) in image size. Square edges will be blurred. This hint
		// keeps the interpolation fast and keeps square edges looking nice.
		offScreenGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

		drawLattice(lattice);

		rescalingSize = false;
	}

	/**
	 * Rescale the width and height of the graphics panel so that it is the
	 * smallest possible with the correct ratio. This size may vary with the
	 * type of lattice. For example, the hexagonal lattice has rows that are
	 * offset (every other row is offset slightly to the right, unlike a square
	 * lattice); this increases the width of the lattice. So the panel's width
	 * and height have to be rescaled to reflect this.
	 * 
	 * @return A pair of integers for the width and height, as small as possible
	 *         that retains the correct ratio.
	 */
	public abstract PanelSize rescaleToMinimumWidthAndHeight();
}
