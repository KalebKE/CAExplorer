/*
 SimplePlot -- a class within the Cellular Automaton Explorer. 
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.font.TextLayout;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

import cellularAutomata.util.Fonts;

/**
 * Plots data passed in as an array of Points.
 * 
 * @author David Bahr
 */
public class PricingVolatilityPlot extends JPanel
{
	private static final Color BACKGROUND_COLOR = new Color(180, 180, 180); // Color.GRAY.brighter();

	// the default tiny separation between plot axes and labels
	private static final float DEFAULT_ITSY_DELTA = 1.0f;

	// the default radius of the dot for each point on the plot
	private static final double DEFAULT_RADIUS = 1.0;

	// The x axis will be inset by this amount. i.e., A value by which the
	// min and max are adjusted so that there is room to display axes, etc.
	private static final double X_DELTA = 15.0;

	// The y axis will be inset by this amount. i.e., A value by which the
	// min and max are adjusted so that there is room to display axes, etc.
	private static final double Y_DELTA = 15.0;

	// default maximum x value that is plotted
	private static final double MAX_X = 100.0;

	// default maximum y value that is plotted
	private static final double MAX_Y = 1.0;

	// default minimum x value that is plotted
	private static final double MIN_X = 0.0;

	// default minimum y value that is plotted
	private static final double MIN_Y = 0.0;

	// the default height of the graphics
	private static final int DEFAULT_HEIGHT = 150;

	// the default tick length in pixels
	private static final int DEFAULT_TICK_LENGTH = 2;

	// the default width of the graphics
	private static final int DEFAULT_WIDTH = 150;

	// the default x-axis label
	private static final String DEFAULT_X_LABEL = "";

	// the default y-axis label
	private static final String DEFAULT_Y_LABEL = "";

	// off screen image that can be persistent (the offScreenGraphics come from
	// this image)
	private BufferedImage offScreenImage = null;

	// off screen graphics object that can be persistent
	private Graphics2D offScreenGraphics = null;

	// sets a default value for the x-axis inset on the plot
	private double deltaX = X_DELTA;

	// sets a default value for the y-axis inset on the plot
	private double deltaY = Y_DELTA;

	// the length of the tick marks
	private int tickLength = DEFAULT_TICK_LENGTH;

	// If true, shows lines between plotted points
	private boolean showLines = true;

	// If true, shows values on the x-axis as ints (default is true)
	private boolean showXValuesAsInts = true;

	// If true, shows values on the y-axis as ints (default is false)
	private boolean showYValuesAsInts = false;

	// data points will be plotted with these colors -- black if null.
	private Color[] dataPointColors = null;

	// The maximum x value that is plotted
	private double maxX = MAX_X;

	// The maximum y value that is plotted
	private double maxY = MAX_Y;

	// The minimum x value that is plotted
	private double minX = MIN_X;

	// The minimum y value that is plotted
	private double minY = MIN_Y;

	// the radius of the circle that is plotted
	private double radius = DEFAULT_RADIUS;

	// the height of the x-axis label
	private double xLabelHeight = 0.0;

	// the width of the x-axis label
	private double xLabelWidth = 0.0;

	// the height of the y-axis label
	private double yLabelHeight = 0.0;

	// the max height of all x values
	private double maxHeightXValues = 0.0;

	// the max width of all y values
	private double maxWidthYValues = 0.0;

	// the height of the maxX value label
	private double maxXLabelHeight = 0.0;

	// the width of the maxX value label
	private double maxXLabelWidth = 0.0;

	// the height of the maxY value label
	private double maxYLabelHeight = 0.0;

	// the width of the maxY value label
	private double maxYLabelWidth = 0.0;

	// the height of the minX value label
	private double minXLabelHeight = 0.0;

	// the width of the minX value label
	private double minXLabelWidth = 0.0;

	// the height of the minY value label
	private double minYLabelHeight = 0.0;

	// the width of the minY value label
	private double minYLabelWidth = 0.0;

	// the width of the y-axis label
	private double yLabelWidth = 0.0;

	// extra x-values that will be displayed along the x-axis
	private double[] extraXValues = null;

	// extra y-values that will be displayed along the y-axis
	private double[] extraYValues = null;

	// a small unit of seperation between labels and other text. Guaranteed to
	// be at least slightly larger than the tickLength.
	private float itsyDelta = (tickLength <= DEFAULT_ITSY_DELTA - 1.0f) ? DEFAULT_ITSY_DELTA
			: tickLength + 1.0f;

	// A font for the axes labels
	private Font labelFont = (new Fonts()).getBoldSmallerFont();

	// A font for the axes values labels
	private Font valueFont = (new Fonts()).getBoldSmallerFont();

	// the starting point of the x-axis
	private Point2D xAxisStartPoint = null;

	// the ending point of the x-axis
	private Point2D xAxisEndPoint = null;

	// the starting point of the y-axis
	private Point2D yAxisStartPoint = null;

	// the ending point of the y-axis
	private Point2D yAxisEndPoint = null;

	// The points that will be plotted.
	private Point2D[] points = null;

	// The x-axis label
	private String xLabel = DEFAULT_X_LABEL;

	// The y-axis label
	private String yLabel = DEFAULT_Y_LABEL;

	/**
	 * Creates a default sized plot.
	 */
	public PricingVolatilityPlot()
	{
		this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	/**
	 * Creates a plot of the suggested size, but consistent with other Java GUI
	 * components, may feel free to ignore the suggestion and create a default
	 * size that best fits the current layout manager.
	 * 
	 * @param preferredWidth
	 *            The suggested width of the plot in pixels.
	 * @param preferredHeight
	 *            The suggested height of the plot in pixels.
	 */
	public PricingVolatilityPlot(int preferredWidth, int preferredHeight)
	{
		super();

		this.setBackground(BACKGROUND_COLOR);
		this.setBorder(BorderFactory.createLoweredBevelBorder());

		// set the size of the plot
		Dimension preferredSize = new Dimension(preferredWidth, preferredHeight);
		this.setPreferredSize(preferredSize);
		this.setMinimumSize(preferredSize);
		this.setSize(preferredSize);
	}

	/**
	 * Draws axes on the supplied graphics.
	 * 
	 * @param g2
	 *            The 2D graphics object.
	 */
	private void drawAxes(Graphics2D g2)
	{
		// set the correct begin and end points for the axes
		setAxesBeginAndEndPoints();

		// the axes
		Line2D xAxis = new Line2D.Double(xAxisStartPoint, xAxisEndPoint);
		Line2D yAxis = new Line2D.Double(yAxisStartPoint, yAxisEndPoint);

		offScreenGraphics.setColor(Color.BLACK);
		offScreenGraphics.draw(xAxis);
		offScreenGraphics.draw(yAxis);
	}

	/**
	 * Draws labels for x- and y-axes (draws on the supplied graphics).
	 * 
	 * @param g2
	 *            The 2D graphics object.
	 */
	private void drawAxesLabels(Graphics2D g2)
	{
		// draw labels in black
		offScreenGraphics.setColor(Color.BLACK);

		// use a small font
		g2.setFont(labelFont);

		// in case not already done, set the correct begin and end points for
		// the axes (needed below)
		setAxesBeginAndEndPoints();
		setMaxXDimensions();
		setMaxYDimensions();
		setMinXDimensions();
		setMinYDimensions();
		setMaxWidthOfYValues();
		setMaxHeightOfXValues();
		setInsetDeltas();

		// layout the y-axis label in the correct position
		float addSign = -1.0f; // puts label to the left of the axis
		float stringPositionX = 0.0f;
		if(yAxisStartPoint.getX() > (offScreenImage.getWidth() / 2.0))
		{
			// put label to the right of the axis
			stringPositionX = (float) (yAxisStartPoint.getX() + maxWidthYValues
					+ yLabelHeight + itsyDelta);
		}
		else
		{
			// put label to the left of the axis
			stringPositionX = (float) (yAxisStartPoint.getX() - maxWidthYValues - 2.0 * itsyDelta);
		}
		float stringPositionY = (float) ((offScreenImage.getHeight() / 2.0) + (yLabelWidth / 2.0));
		if((xAxisStartPoint.getY() < stringPositionY)
				&& (xAxisStartPoint.getY() > stringPositionY
						- (yLabelWidth / 2.0)))
		{
			// axis is crossing the bottom half of label, so move label
			stringPositionY = (float) ((xAxisStartPoint.getY() / 2.0) + (yLabelWidth / 2.0));
			if(stringPositionY - yLabelWidth < itsyDelta)
			{
				// not enough room
				stringPositionY = (float) (yLabelWidth + 2.0 * itsyDelta);
			}
		}
		else if((xAxisStartPoint.getY() < stringPositionY)
				&& (xAxisStartPoint.getY() > stringPositionY - yLabelWidth))
		{
			// axis is crossing the top half of label, so move label
			stringPositionY = (float) (xAxisStartPoint.getY()
					+ ((offScreenImage.getHeight() - xAxisStartPoint.getY()) / 2.0) + (yLabelWidth / 2.0));
			if(stringPositionY > offScreenImage.getHeight() - itsyDelta)
			{
				stringPositionY = (float) (offScreenImage.getHeight() - 2.0 * itsyDelta);
			}
		}
		if(stringPositionY > offScreenImage.getHeight())
		{
			// just to be safe
			stringPositionY = offScreenImage.getHeight();
		}

		// translate and rotate clockwise 90 degrees
		g2.translate(stringPositionX, stringPositionY);
		g2.rotate(-Math.PI / 2.0);

		// draw the y-label
		g2.drawString(yLabel, 0, 0);

		// rotate and translate back
		g2.rotate(Math.PI / 2.0);
		g2.translate(-stringPositionX, -stringPositionY);

		// draw the x-label
		addSign = 1.0f;
		float includeTerm = 1.0f; // puts the label below the axis
		if(xAxisStartPoint.getY() < (offScreenImage.getHeight() / 2.0))
		{
			// put label above the axis
			includeTerm = 0.0f;
			addSign = -1.0f;
		}
		float xPos = (float) ((offScreenImage.getWidth() / 2.0) - (xLabelWidth / 2.0));
		float yPos = (float) (xAxisEndPoint.getY() + addSign
				* (maxHeightXValues + (itsyDelta / 2.0)) + includeTerm
				* xLabelHeight);
		if((yAxisStartPoint.getX() > xPos)
				&& (yAxisStartPoint.getX() < xPos + (xLabelWidth / 2.0)))
		{
			// axis is crossing the left half of label, so move label
			xPos = (float) (yAxisStartPoint.getX()
					+ ((offScreenImage.getWidth() - yAxisStartPoint.getX()) / 2.0) - (xLabelWidth / 2.0));
			if(xPos + xLabelWidth > offScreenImage.getWidth())
			{
				xPos = (float) (offScreenImage.getWidth() - xLabelWidth - itsyDelta);
			}
		}
		else if((yAxisStartPoint.getX() > xPos)
				&& (yAxisStartPoint.getX() < xPos + xLabelWidth))
		{
			// axis is crossing the right half of label, so move label
			xPos = (float) (yAxisStartPoint.getX() / 2.0 - (xLabelWidth / 2.0));
		}
		if(xPos < 0.0)
		{
			// to be safe
			xPos = (float) (2.0 * itsyDelta);
		}
		g2.drawString(xLabel, Math.round(xPos), Math.round(yPos));
	}

	/**
	 * Draws values for the min and max of the axes (draws on the supplied
	 * graphics).
	 * 
	 * @param g2
	 *            The 2D graphics object.
	 */
	private void drawAxesValues(Graphics2D g2)
	{
		// draw values in black
		g2.setColor(Color.BLACK);

		// use a small font
		g2.setFont(valueFont);

		// the values to draw
		String minXString = showXValuesAsInts ? "" + (int) minX : "" + minX;
		String maxXString = showXValuesAsInts ? "" + (int) maxX : "" + maxX;
		String minYString = showYValuesAsInts ? "" + (int) minY : "" + minY;
		String maxYString = showYValuesAsInts ? "" + (int) maxY : "" + maxY;

		// layout the minX text in the correct position
		float stringPositionX = 0.0f;
		if(yAxisStartPoint.getX() == deltaX)
		{
			// don't overlap
			stringPositionX = (float) (deltaX + itsyDelta);
		}
		else
		{
			stringPositionX = (float) (deltaX - (minXLabelWidth / 2.0));
		}
		if(stringPositionX < 0.0)
		{
			stringPositionX = (float) (0.0 + itsyDelta);
		}
		float stringPositionY = 0.0f;
		if(xAxisStartPoint.getY() >= (offScreenImage.getHeight() / 2.0))
		{
			// values go below the axis
			stringPositionY = (float) (xAxisStartPoint.getY() + minXLabelHeight + itsyDelta);
		}
		else
		{
			// values go above the axis
			stringPositionY = (float) (xAxisStartPoint.getY() - itsyDelta);
		}
		g2.drawString(minXString, stringPositionX, stringPositionY);

		// layout the maxX text in the correct position
		if(yAxisStartPoint.getX() == (offScreenImage.getWidth() - deltaX))
		{
			// don't overlap the axis
			stringPositionX = (float) (offScreenImage.getWidth() - deltaX
					- maxXLabelWidth - itsyDelta);
		}
		else
		{
			stringPositionX = (float) (offScreenImage.getWidth() - deltaX - (maxXLabelWidth / 2.0));
		}
		if((stringPositionX + maxXLabelWidth) > offScreenImage.getWidth()
				- itsyDelta)
		{
			stringPositionX = (float) (offScreenImage.getWidth()
					- maxXLabelWidth - 2.0 * itsyDelta);
		}
		if(xAxisStartPoint.getY() >= (offScreenImage.getHeight() / 2.0))
		{
			// values go below the axis
			stringPositionY = (float) (xAxisEndPoint.getY() + maxXLabelHeight + itsyDelta);
		}
		else
		{
			// values go above the axis
			stringPositionY = (float) (xAxisEndPoint.getY() - itsyDelta);
		}
		g2.drawString(maxXString, stringPositionX, stringPositionY);

		// layout the minY text in the correct position
		if(yAxisStartPoint.getX() < (offScreenImage.getWidth() / 2.0))
		{
			// text goes to the left of the axis
			stringPositionX = (float) (yAxisStartPoint.getX() - minYLabelWidth - itsyDelta);
		}
		else
		{
			// text goes to the right of the axis
			stringPositionX = (float) (yAxisStartPoint.getX() + itsyDelta);
		}
		if(xAxisStartPoint.getY() == offScreenImage.getHeight() - deltaY)
		{
			// don't overlap the axis
			stringPositionY = (float) (offScreenImage.getHeight() - deltaY - itsyDelta);
		}
		else
		{
			stringPositionY = (float) (offScreenImage.getHeight() - deltaY + (minYLabelHeight / 2.0));
		}
		if(stringPositionY > offScreenImage.getHeight())
		{
			stringPositionY = offScreenImage.getHeight() - itsyDelta;
		}
		g2.drawString(minYString, stringPositionX, stringPositionY);

		// layout the maxY text in the correct position
		if(yAxisStartPoint.getX() < (offScreenImage.getWidth() / 2.0))
		{
			// text goes to the left of the axis
			stringPositionX = (float) (yAxisStartPoint.getX() - maxYLabelWidth - itsyDelta);
		}
		else
		{
			// text goes to the right of the axis
			stringPositionX = (float) (yAxisStartPoint.getX() + itsyDelta);
		}
		stringPositionY = (float) (deltaY + (maxYLabelHeight / 2.0));
		if((xAxisStartPoint.getY() < stringPositionY)
				&& (xAxisStartPoint.getY() > stringPositionY - maxYLabelHeight))
		{
			// then x-axis is intersecting the maxY label, so move the label
			stringPositionY = (float) (deltaY + maxYLabelHeight + 2.0 * itsyDelta);
		}
		// make sure text isn't off the screen
		if(stringPositionY < maxYLabelHeight + itsyDelta)
		{
			stringPositionY = (float) (maxYLabelHeight + itsyDelta);
		}
		g2.drawString(maxYString, stringPositionX, stringPositionY);

		// now draw any other values
		drawExtraAxesValues(g2);
	}

	/**
	 * Draws values along the axes (only the extra values if any were supplied
	 * by the user). Note that if the shape of a value is too large, then it may
	 * be obscured or overdrawn by other graphics drawn on the plot.
	 * 
	 * @param g2
	 *            A graphics context on which to draw.
	 */
	private void drawExtraAxesValues(Graphics2D g2)
	{
		// draw values in black
		g2.setColor(Color.BLACK);

		// draw extra x values
		if(extraXValues != null && extraXValues.length > 0)
		{
			for(int i = 0; i < extraXValues.length; i++)
			{
				// the string to draw
				String extraString = showXValuesAsInts ? ""
						+ (int) extraXValues[i] : "" + extraXValues[i];

				// get the string's width and height
				FontRenderContext frc = offScreenGraphics
						.getFontRenderContext();
				TextLayout layout = new TextLayout(extraString, valueFont, frc);
				Rectangle2D bounds = layout.getBounds();
				double stringWidth = bounds.getWidth();
				double stringHeight = bounds.getHeight();

				// rescales the location to the plot's pixel location
				Point2D rescaledLocation = rescalePoint(new Point2D.Double(
						extraXValues[i], 0.0));

				// draw the string
				if(xAxisStartPoint.getY() >= (offScreenImage.getHeight() / 2.0))
				{
					// values go below the axis
					g2
							.drawString(
									extraString,
									(int) (rescaledLocation.getX() - (stringWidth / 2.0)),
									(int) (xAxisEndPoint.getY() + stringHeight + itsyDelta));
				}
				else
				{
					// values go above the axis
					g2
							.drawString(extraString, (int) (rescaledLocation
									.getX() - (stringWidth / 2.0)),
									(int) (xAxisEndPoint.getY() + itsyDelta));
				}

				// draw a tick mark
				Line2D tickLine = new Line2D.Double(rescaledLocation.getX(),
						xAxisEndPoint.getY() - tickLength / 2.0,
						rescaledLocation.getX(), xAxisEndPoint.getY()
								+ tickLength / 2.0);
				g2.draw(tickLine);
			}
		}

		// draw extra y values
		if(extraYValues != null && extraYValues.length > 0)
		{
			for(int i = 0; i < extraYValues.length; i++)
			{
				// the string to draw
				String extraString = showYValuesAsInts ? ""
						+ (int) extraYValues[i] : "" + extraYValues[i];

				// get the string's width
				FontRenderContext frc = offScreenGraphics
						.getFontRenderContext();
				TextLayout layout = new TextLayout(extraString, valueFont, frc);
				Rectangle2D bounds = layout.getBounds();
				double stringWidth = bounds.getWidth();
				double stringHeight = bounds.getHeight();

				// rescales the location to the plot's pixel location
				Point2D rescaledLocation = rescalePoint(new Point2D.Double(0.0,
						extraYValues[i]));

				// draw the string
				if(yAxisStartPoint.getX() < (offScreenImage.getWidth() / 2.0))
				{
					// text goes to the left of the axis
					g2.drawString(extraString, (int) (yAxisEndPoint.getX()
							- stringWidth - itsyDelta), (int) (rescaledLocation
							.getY() + (stringHeight / 2.0)));
				}
				else
				{
					// text goes to the right of the axis
					g2
							.drawString(
									extraString,
									(int) (yAxisEndPoint.getX() + itsyDelta),
									(int) (rescaledLocation.getY() + (stringHeight / 2.0)));
				}

				// draw a tick mark
				Line2D tickLine = new Line2D.Double(yAxisEndPoint.getX()
						- tickLength / 2.0, rescaledLocation.getY(),
						yAxisEndPoint.getX() + tickLength / 2.0,
						rescaledLocation.getY());
				g2.draw(tickLine);
			}
		}
	}

	/**
	 * Draws tick marks along the axes.
	 * 
	 * @param g2
	 *            The 2D graphics object.
	 */
	private void drawTickMarks(Graphics2D g2)
	{
		// draw values in black
		g2.setColor(Color.BLACK);

		Point2D minXPoint = rescalePoint(new Point2D.Double(minX, 0));
		Point2D maxXPoint = rescalePoint(new Point2D.Double(maxX, 0));
		Point2D minYPoint = rescalePoint(new Point2D.Double(0, minY));
		Point2D maxYPoint = rescalePoint(new Point2D.Double(0, maxY));

		Line2D minXTickLine = new Line2D.Double(minXPoint.getX(), xAxisEndPoint
				.getY()
				- tickLength / 2.0, minXPoint.getX(), xAxisEndPoint.getY()
				+ tickLength / 2.0);
		Line2D maxXTickLine = new Line2D.Double(maxXPoint.getX(), xAxisEndPoint
				.getY()
				- tickLength / 2.0, maxXPoint.getX(), xAxisEndPoint.getY()
				+ tickLength / 2.0);
		Line2D minYTickLine = new Line2D.Double(yAxisEndPoint.getX()
				- tickLength / 2.0, minYPoint.getY(), yAxisEndPoint.getX()
				+ tickLength / 2.0, minYPoint.getY());
		Line2D maxYTickLine = new Line2D.Double(yAxisEndPoint.getX()
				- tickLength / 2.0, maxYPoint.getY(), yAxisEndPoint.getX()
				+ tickLength / 2.0, maxYPoint.getY());

		g2.draw(minXTickLine);
		g2.draw(minYTickLine);
		g2.draw(maxXTickLine);
		g2.draw(maxYTickLine);
	}

	/**
	 * It is better and faster to draw to an off screen image (and its
	 * associated off screen graphics). Once the off screen drawing is done,
	 * then the entire image can be displayed at one time on the screen. This
	 * prevents flickering and tearing of the graphics.
	 */
	private void initOffScreenImage()
	{
		offScreenImage = new BufferedImage(this.getWidth(), this.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		offScreenGraphics = offScreenImage.createGraphics();
		offScreenGraphics.setColor(BACKGROUND_COLOR);
		offScreenGraphics.fillRect(0, 0, this.getWidth(), this.getHeight());
	}

	/**
	 * Rescales the x- and y-axis values of a point to be graphed. Rescales to a
	 * pixel value on the panel. (The JPanel and offScreenImage only understands
	 * values between 0 and panel.getWidth() and 0 and panel.getHeight().)
	 * <p>
	 * In particular, rescale the point's x-axis from (minX, maxX) to (0,
	 * offScreenImage.getWidth). Also, for the y-axis, convert to coordinates at
	 * the top-left (rather than bottom-left), and rescale (minY, maxY) to (0,
	 * panel.getHeight).
	 * 
	 * @param point
	 *            The point that will be rescaled to a pixel point on the plot.
	 * @return The rescaled point with pixel values that fall within the
	 *         graphics panel.
	 */
	private Point2D rescalePoint(Point2D point)
	{
		// the point to be plotted
		double x = point.getX();
		double y = point.getY();

		// rescale from (minX, maxX) to (0+DELTA, panel.getWidth-DELTA).
		if(maxX != minX)
		{
			x = ((x - minX) / (maxX - minX))
					* (offScreenImage.getWidth() - 2 * deltaX) + deltaX;
		}

		// convert to coordinates at the top-left (rather than
		// bottom-left). And rescale (minY, maxY) to (0+DELTA,
		// panel.getHeight-DELTA).
		if(maxY != minY)
		{
			y = ((maxY - y) / (maxY - minY))
					* (offScreenImage.getHeight() - 2 * deltaY) + deltaY;
		}

		return new Point2D.Double(x, y);
	}

	/**
	 * Calculates how much space is needed around the edges to fit the labels.
	 */
	private void setInsetDeltas()
	{
		deltaX = maxWidthYValues + yLabelHeight + 3 * itsyDelta;
		deltaY = maxHeightXValues + xLabelHeight + 3 * itsyDelta;
	}

	/**
	 * Finds the maximum height of all the values that will be plotted along the
	 * x-axis.
	 */
	private void setMaxHeightOfXValues()
	{
		maxHeightXValues = (maxXLabelHeight > minXLabelHeight) ? maxXLabelHeight
				: minXLabelHeight;
		if(extraXValues != null && extraXValues.length > 0
				&& offScreenGraphics != null)
		{
			for(int i = 0; i < extraXValues.length; i++)
			{
				// the string to draw
				String extraString = showXValuesAsInts ? ""
						+ (int) extraXValues[i] : "" + extraXValues[i];

				// get the string's width
				FontRenderContext frc = offScreenGraphics
						.getFontRenderContext();
				TextLayout layout = new TextLayout(extraString, valueFont, frc);
				Rectangle2D bounds = layout.getBounds();
				if(bounds.getHeight() > maxHeightXValues)
				{
					maxHeightXValues = bounds.getHeight();
				}
			}
		}
	}

	/**
	 * Finds the maximum width of all the values that will be plotted along the
	 * y-axis.
	 */
	private void setMaxWidthOfYValues()
	{
		maxWidthYValues = (maxYLabelWidth > minYLabelWidth) ? maxYLabelWidth
				: minYLabelWidth;
		if(extraYValues != null && extraYValues.length > 0
				&& offScreenGraphics != null)
		{
			for(int i = 0; i < extraYValues.length; i++)
			{
				// the string to draw
				String extraString = showYValuesAsInts ? ""
						+ (int) extraYValues[i] : "" + extraYValues[i];

				// get the string's width
				FontRenderContext frc = offScreenGraphics
						.getFontRenderContext();
				TextLayout layout = new TextLayout(extraString, valueFont, frc);
				Rectangle2D bounds = layout.getBounds();
				if(bounds.getWidth() > maxWidthYValues)
				{
					maxWidthYValues = bounds.getWidth();
				}
			}
		}
	}

	/**
	 * Calculates the dimensions of the maxX value label.
	 */
	private void setMaxXDimensions()
	{
		// now set the label's height and width (for proper display later)
		String maxXString = showXValuesAsInts ? "" + (int) maxX : "" + maxX;
		FontRenderContext frc = offScreenGraphics.getFontRenderContext();
		TextLayout layout = new TextLayout(maxXString, valueFont, frc);
		Rectangle2D bounds = layout.getBounds();
		maxXLabelHeight = bounds.getHeight();
		maxXLabelWidth = bounds.getWidth();
	}

	/**
	 * Calculates the dimensions of the maxY value label.
	 */
	private void setMaxYDimensions()
	{
		// now set the label's height and width (for proper display later)
		String maxYString = showYValuesAsInts ? "" + (int) maxY : "" + maxY;
		FontRenderContext frc = offScreenGraphics.getFontRenderContext();
		TextLayout layout = new TextLayout(maxYString, valueFont, frc);
		Rectangle2D bounds = layout.getBounds();
		maxYLabelHeight = bounds.getHeight();
		maxYLabelWidth = bounds.getWidth();
	}

	/**
	 * Calculates the dimensions of the minX value label.
	 */
	private void setMinXDimensions()
	{
		// now set the label's height and width (for proper display later)
		String minXString = showXValuesAsInts ? "" + (int) minX : "" + minX;
		FontRenderContext frc = offScreenGraphics.getFontRenderContext();
		TextLayout layout = new TextLayout(minXString, valueFont, frc);
		Rectangle2D bounds = layout.getBounds();
		minXLabelHeight = bounds.getHeight();
		minXLabelWidth = bounds.getWidth();
	}

	/**
	 * Calculates the dimensions of the minY value label.
	 */
	private void setMinYDimensions()
	{
		// now set the label's height and width (for proper display later)
		String minYString = showYValuesAsInts ? "" + (int) minY : "" + minY;
		FontRenderContext frc = offScreenGraphics.getFontRenderContext();
		TextLayout layout = new TextLayout(minYString, valueFont, frc);
		Rectangle2D bounds = layout.getBounds();
		minYLabelHeight = bounds.getHeight();
		minYLabelWidth = bounds.getWidth();
	}

	/**
	 * Sets the points where each of the axes begin and end.
	 */
	private void setAxesBeginAndEndPoints()
	{
		// create the x-axis begin and end points
		if(maxY == minY || minY >= 0.0)
		{
			// create an axis on the bottom
			xAxisStartPoint = new Point2D.Double(0.0, offScreenImage
					.getHeight()
					- deltaY);
			xAxisEndPoint = new Point2D.Double(offScreenImage.getWidth(),
					offScreenImage.getHeight() - deltaY);
		}
		else if(maxY <= 0)
		{
			// create an axis on the top
			xAxisStartPoint = new Point2D.Double(0.0, 0.0 + deltaY);
			xAxisEndPoint = new Point2D.Double(offScreenImage.getWidth(),
					0.0 + deltaY);
		}
		else
		{
			// put the x-axis at the y = 0 point
			// first we find that y = 0 point on the rescaled axis
			Point2D start = new Point2D.Double(minX, 0.0);
			Point2D rescaledStart = rescalePoint(start);
			double yZero = rescaledStart.getY();

			// rescale the points to fit on the plot
			xAxisStartPoint = new Point2D.Double(0.0, yZero);
			xAxisEndPoint = new Point2D.Double(offScreenImage.getWidth(), yZero);
		}

		// create the y-axis begin and end points
		if(maxX == minX || minX >= 0.0)
		{
			// create an axis on the left
			yAxisStartPoint = new Point2D.Double(0.0 + deltaX, offScreenImage
					.getHeight());
			yAxisEndPoint = new Point2D.Double(0.0 + deltaX, 0.0);
		}
		else if(maxX <= 0)
		{
			// create an axis on the right
			yAxisStartPoint = new Point2D.Double(offScreenImage.getWidth()
					- deltaX, offScreenImage.getHeight());
			yAxisEndPoint = new Point2D.Double(offScreenImage.getWidth()
					- deltaX, 0.0);
		}
		else
		{
			// put the y-axis at the x = 0 point
			// first we find that x = 0 point on the rescaled axis
			Point2D start = new Point2D.Double(0.0, minY);
			Point2D rescaledStart = rescalePoint(start);
			double xZero = rescaledStart.getX();

			// rescale the points to fit on the plot
			yAxisStartPoint = new Point2D.Double(xZero, 0.0);
			yAxisEndPoint = new Point2D.Double(xZero, offScreenImage
					.getHeight());
		}
	}

	/**
	 * Calculates the dimensions of the x-axis label.
	 */
	private void setXLabelDimensions()
	{
		// now set the label's height and width (for proper display later)
		if(xLabel != null && xLabel.length() > 0)
		{
			FontRenderContext frc = offScreenGraphics.getFontRenderContext();
			TextLayout layout = new TextLayout(xLabel, labelFont, frc);
			Rectangle2D bounds = layout.getBounds();
			xLabelHeight = bounds.getHeight();
			xLabelWidth = bounds.getWidth();
		}
		else
		{
			xLabelHeight = 0.0;
			xLabelWidth = 0.0;
		}
	}

	/**
	 * Calculates the dimensions of the y-axis label.
	 */
	private void setYLabelDimensions()
	{
		// now set the label's height and width (for proper display later)
		if(yLabel != null && yLabel.length() > 0)
		{
			FontRenderContext frc = offScreenGraphics.getFontRenderContext();
			TextLayout layout = new TextLayout(yLabel, labelFont, frc);
			Rectangle2D bounds = layout.getBounds();
			yLabelHeight = bounds.getHeight();
			yLabelWidth = bounds.getWidth();
		}
		else
		{
			yLabelHeight = 0.0;
			yLabelWidth = 0.0;
		}
	}

	/**
	 * Clears the plot and resets to its default values.
	 */
	public void clearPlot()
	{
		maxX = MAX_X;
		maxY = MAX_Y;
		minX = MIN_X;
		minY = MIN_Y;
		radius = DEFAULT_RADIUS;
		xLabel = DEFAULT_X_LABEL;
		yLabel = DEFAULT_Y_LABEL;
		extraXValues = null;
		extraYValues = null;
		dataPointColors = null;
		points = null;

		// now draw an empty screen (there are no points)
		drawPoints(points);
	}

	/**
	 * Clears the data points from the plot but leaves all of the axes values,
	 * etc.
	 */
	public void clearDataPoints()
	{
		points = null;

		// now draw an empty screen (there are no points)
		drawPoints(points);
	}

	/**
	 * Sets and draws the points on the plot.
	 * 
	 * @param listOfPoints
	 *            The list of points that will be drawn. The elements of the
	 *            list must be objects of type Point2D.Double.
	 */
	public void drawPoints(List listOfPoints)
	{
		if(listOfPoints != null && listOfPoints.size() > 0)
		{
			Object[] list = listOfPoints.toArray();

			this.points = new Point2D.Double[list.length];
			for(int i = 0; i < list.length; i++)
			{
				this.points[i] = (Point2D) list[i];
			}
		}

		drawPoints(points);
	}

	/**
	 * Sets and draws the points on the plot.
	 * 
	 * @param points
	 *            The array of points that will be drawn.
	 */
	public void drawPoints(Point2D[] points)
	{
		// save the points in case need to redraw later
		this.points = points;

		// create the offscreen graphics if necessary
		if(offScreenGraphics == null)
		{
			initOffScreenImage();
		}

		// only draw if there are points to plot
		if(points != null && points.length > 0)
		{
			// clear the graphics
			offScreenGraphics.setColor(BACKGROUND_COLOR);
			offScreenGraphics.fillRect(0, 0, this.getWidth(), this.getHeight());

			// draw each point in black
			offScreenGraphics.setPaint(Color.BLACK);

			// the previous point that was drawn
			Point2D previousPoint = null;

			// plot each point
			for(int i = 0; i < points.length; i++)
			{
				// rescale the point's x-axis from (minX, maxX) to (0,
				// offScreenImage.getWidth). Also, for the y-axis, convert to
				// coordinates at the top-left (rather than
				// bottom-left), and rescale (minY, maxY) to (0,
				// panel.getHeight).
				Point2D rescaledPoint = rescalePoint(points[i]);

				// the point to be plotted
				double x = rescaledPoint.getX();
				double y = rescaledPoint.getY();

				// the origin of the bounding rectangle for the point that will
				// be plotted. Negative values are ok (will show partial
				// circles).
				double xPos = x - radius;
				double yPos = y - radius;

				// draw the point
				Ellipse2D circle = new Ellipse2D.Double(xPos, yPos, 2 * radius,
						2 * radius);

				if(dataPointColors != null)
				{
					// change to the appropriate color set by the user
					offScreenGraphics.setPaint(dataPointColors[i]);

					offScreenGraphics.fill(circle);

					// reset the color
					offScreenGraphics.setPaint(Color.BLACK);
				}
				else
				{
					offScreenGraphics.fill(circle);
				}

				// draw the line between this and the previous point
				if(previousPoint != null && showLines)
				{
					Line2D lineSegment = new Line2D.Double(previousPoint,
							rescaledPoint);
					offScreenGraphics.draw(lineSegment);
				}

				// save the previous point so can draw lines between them
				previousPoint = rescaledPoint;
			}
		}
		else
		{
			// draw a blank graph
			offScreenGraphics.setPaint(BACKGROUND_COLOR);
			offScreenGraphics.setColor(BACKGROUND_COLOR);
			Rectangle2D rectangle = new Rectangle2D.Double(0, 0, offScreenImage
					.getWidth(), offScreenImage.getHeight());
			offScreenGraphics.fill(rectangle);
		}

		// draw axes
		drawAxes(offScreenGraphics);

		// draw tick marks
		drawTickMarks(offScreenGraphics);

		// draw axes values
		drawAxesValues(offScreenGraphics);

		// draw axes labels
		drawAxesLabels(offScreenGraphics);

		// calls paintComponent (which should not be called directly)
		repaint();
	}

	/**
	 * Gets the default radius of each circle that will be plotted for each data point.
	 * 
	 * @return The radius of the circle plotted.
	 */
	public double getDefaultRadius()
	{
		return DEFAULT_RADIUS;
	}
	
	/**
	 * Gets the maximum x value that will be plotted.
	 * 
	 * @return The maximum x value on the right side of the x-axis.
	 */
	public double getMaximumXValue()
	{
		return maxX;
	}

	/**
	 * Gets the maximum y value that will be plotted.
	 * 
	 * @return The maximum y value on the top of the y-axis.
	 */
	public double getMaximumYValue()
	{
		return maxY;
	}

	/**
	 * Gets the minimum x value that will be plotted.
	 * 
	 * @return The minimum x value on the left side of the x-axis.
	 */
	public double getMinimumXValue()
	{
		return minX;
	}

	/**
	 * Gets the minimum y value that will be plotted.
	 * 
	 * @return The minimum y value on the bottom of the y-axis.
	 */
	public double getMinimumYValue()
	{
		return minY;
	}

	/**
	 * Draw the points on the panel. If no points have been set, then nothing is
	 * drawn.
	 */
	public void paintComponent(Graphics g)
	{
		// if the panel has resized, then we also need to resize the off screen
		// image and off screen graphics
		if(offScreenImage == null
				|| this.getWidth() != offScreenImage.getWidth()
				|| this.getHeight() != offScreenImage.getHeight())
		{
			// create the off screen image and graphics
			initOffScreenImage();

			// calculate the dimensions of the labels
			setXLabelDimensions();
			setYLabelDimensions();

			// calculate the dimensions of the axis values
			setMaxXDimensions();
			setMaxYDimensions();
			setMinXDimensions();
			setMinYDimensions();

			// change the insets of the axes
			setInsetDeltas();

			// the points where the axes begin and end
			setAxesBeginAndEndPoints();

			// draw the plot
			drawPoints(points);
		}

		super.paintComponent(g);

		g.drawImage(offScreenImage, 0, 0, this);

		// necessary to prevent tearing on linux and other OS
		Toolkit.getDefaultToolkit().sync();
	}

	/**
	 * Sets extra x-values that will be displayed along the x-axis.
	 * 
	 * @param values
	 *            An array of values that will be displayed.
	 */
	public void setExtraXAxisValues(double[] values)
	{
		extraXValues = values;

		setMaxHeightOfXValues();
		setInsetDeltas();
	}

	/**
	 * Sets extra y-values that will be displayed along the y-axis.
	 * 
	 * @param values
	 *            An array of values that will be displayed.
	 */
	public void setExtraYAxisValues(double[] values)
	{
		extraYValues = values;

		setMaxWidthOfYValues();
		setInsetDeltas();
	}

	/**
	 * Sets the maximum x value that will be plotted.
	 * 
	 * @param x
	 *            The maximum value on the right side of the plot.
	 */
	public void setMaximumXValue(double x)
	{
		maxX = x;

		if(offScreenImage != null)
		{
			setMaxXDimensions();
			setMaxHeightOfXValues();
			setInsetDeltas();
		}
	}

	/**
	 * Sets the maximum y value that will be plotted.
	 * 
	 * @param y
	 *            The maximum value on the top of the plot.
	 */
	public void setMaximumYValue(double y)
	{
		maxY = y;

		if(offScreenImage != null)
		{
			setMaxYDimensions();
			setMaxWidthOfYValues();
			setInsetDeltas();
		}
	}

	/**
	 * Sets the minimum x value that will be plotted.
	 * 
	 * @param x
	 *            The minimum value on the left side of the plot.
	 */
	public void setMinimumXValue(double x)
	{
		minX = x;

		if(offScreenImage != null)
		{
			setMinXDimensions();
			setMaxHeightOfXValues();
			setInsetDeltas();
		}
	}

	/**
	 * Sets the minimum y value that will be plotted.
	 * 
	 * @param y
	 *            The minimum value on the bottom of the plot.
	 */
	public void setMinimumYValue(double y)
	{
		minY = y;

		if(offScreenImage != null)
		{
			setMinYDimensions();
			setMaxWidthOfYValues();
			setInsetDeltas();
		}
	}

	/**
	 * Set the colors of the data points.
	 * 
	 * @param colorList
	 *            The colors of each data point.
	 */
	public void setPointDisplayColors(List<Color> colorList)
	{
		Color[] colors = null;
		if(colorList != null && colorList.size() > 0)
		{
			colors = (Color[]) colorList.toArray(new Color[1]);
		}
		else
		{
			setPointDisplayColorsToDefault();
		}

		setPointDisplayColors(colors);
	}

	/**
	 * Set the colors of the data points.
	 * 
	 * @param colors
	 *            The colors of each data point.
	 */
	public void setPointDisplayColors(Color[] colors)
	{
		dataPointColors = colors;
	}

	/**
	 * Set the colors of the data points to the default color.
	 */
	public void setPointDisplayColorsToDefault()
	{
		dataPointColors = null;
	}

	/**
	 * Sets the radius (size) of each point that is plotted.
	 * 
	 * @param r
	 *            The radius of the point that is plotted.
	 */
	public void setRadius(double r)
	{
		radius = r;
	}

	/**
	 * Sets a label for the x-axis.
	 * 
	 * @param label
	 *            The label that will be displayed along the x-axis.
	 */
	public void setXAxisLabel(String label)
	{
		xLabel = label;

		if(offScreenImage != null)
		{
			setXLabelDimensions();
			setInsetDeltas();
		}
	}

	/**
	 * Sets a label for the y-axis.
	 * 
	 * @param label
	 *            The label that will be displayed vertically along the y-axis.
	 */
	public void setYAxisLabel(String label)
	{
		yLabel = label;

		if(offScreenImage != null)
		{
			setYLabelDimensions();
			setInsetDeltas();
		}
	}

	/**
	 * If true, will draw a line between each plotted value. Default behavior is
	 * true.
	 */
	public void showPlotLines(boolean showLines)
	{
		this.showLines = showLines;
	}

	/**
	 * If true, will show values along the x-axis as integers rather than
	 * decimals. Default behavior is true.
	 */
	public void showXValuesAsInts(boolean showAsInts)
	{
		showXValuesAsInts = showAsInts;
	}

	/**
	 * If true, will show values along the y-axis as integers rather than
	 * decimals. Default behavior is false.
	 */
	public void showYValuesAsInts(boolean showAsInts)
	{
		showYValuesAsInts = showAsInts;
	}
}
