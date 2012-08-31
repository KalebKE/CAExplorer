/*
 IntegerStateColorChooserPanel -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2006  David B. Bahr (http://academic.regis.edu/dbahr/)

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

package cellularAutomata.graphics.colors.colorChooser;

import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.graphics.colors.colorChooser.IntegerColorSelectionModel;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.rules.Rule;
import cellularAutomata.rules.templates.FiniteObjectRuleTemplate;
import cellularAutomata.util.Coordinate;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Creates a color chooser panel that only allows the user to select colors
 * associated with the integer values of the cells.
 * 
 * @author David Bahr
 */
public class IntegerStateColorChooserPanel extends JPanel
{
	private static final Dimension LARGE_PATCH_SIZE = new Dimension(50, 50);

	private static final Dimension MEDIUM_PATCH_SIZE = new Dimension(25, 25);

	// the maximum number of columns before the color patch size is reduced
	private static final int MAX_COLUMNS_AT_LARGE_PATCH_SIZE = 7;

	// the maximum number of columns before the color patch size is reduced even
	// further
	private static final int MAX_COLUMNS_AT_MEDIUM_PATCH_SIZE = 15;

	// the array of colors to display
	private static Color[] color = null;

	// the view used to display this cell state
	private CellStateView currentView = null;

	// the color selection model
	private IntegerColorSelectionModel colorSelectionModel = null;

	// the number of columns of color patches to be displayed (reassigned in the
	// constructor)
	private int numColumns = 1;

	// the number of rows of color patches to be displayed (reassigned in the
	// constructor)
	private int numRows = 1;

	// The current rule, selected from the properties and set in the
	// constructor.
	private Rule rule = null;

	/**
	 * Creates a color chooser for integer valued cells.
	 * 
	 * @param numStates
	 *            The number of states permitted by the cell.
	 * @param colorSelectionModel
	 *            The color selection model of the encompassing JColorChooser.
	 */
	public IntegerStateColorChooserPanel(int numStates,
			IntegerColorSelectionModel colorSelectionModel)
	{
		this.colorSelectionModel = colorSelectionModel;

		// get the current view and a compatible cell state
		String classNameOfRule = CurrentProperties.getInstance()
				.getRuleClassName();
		rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(classNameOfRule);
		currentView = rule.getCompatibleCellStateView();

		// create the color panel
		color = new Color[numStates];
		for(int i = 0; i < numStates; i++)
		{
			// get the color from the view
			color[i] = currentView.getDisplayColor(new IntegerCellState(i),
					null, new Coordinate(0, 0));
		}

		buildChooser();
	}

	/**
	 * Builds the color chooser.
	 */
	private void buildChooser()
	{
		ColorPalette colorPalette = new ColorPalette(color);
		this.add(colorPalette);

		// int numColumns = (int) Math.ceil(color.length / (double) numRows);
		// ColorPatch patch = new ColorPatch(color[0], 0);
		// int width = (int) Math.ceil(patch.getPreferredSize().getWidth());
		// int height = (int) Math.ceil(patch.getPreferredSize().getHeight());
		// this.setPreferredSize(new Dimension((width * numColumns) + 100,
		// (height * numRows) + 200));
	}

	/**
	 * Calculates the number of rows that will be displayed in the color
	 * chooser.
	 * 
	 * @param numColors
	 *            The number of colors to be displayed.
	 * @return The number of rows over which the colors will be distributed and
	 *         displayed.
	 */
	public static int calculateNumberOfRows(int numColors)
	{
		// return (int) Math.round(Math.sqrt((double) numColors) / 1.3);
		return (int) Math.round(Math.sqrt(((double) numColors) / 2.0));
	}

	/**
	 * A JPanel that displays the array of possible colors.
	 * 
	 * @author David Bahr
	 */
	private class ColorPalette extends JPanel
	{
		public ColorPalette(Color[] colors)
		{
			int numColors = colors.length;
			numRows = calculateNumberOfRows(numColors);
			numColumns = (int) Math.ceil(numColors / (double) numRows);

			this.setBorder(BorderFactory.createEmptyBorder());
			this.setLayout(new GridLayout(numRows, 0, 1, 1));

			for(int i = 0; i < colors.length; i++)
			{
				this.add(new ColorPatch(colors[i], i));
			}

			// int numColumns = (int) Math.ceil(numColors / (double) numRows);
			// ColorPatch patch = new ColorPatch(colors[0], 0);
			// int width = (int) Math.ceil(patch.getPreferredSize().getWidth());
			// int height = (int)
			// Math.ceil(patch.getPreferredSize().getHeight());
			// this.setPreferredSize(new Dimension(width * numColumns, height
			// * numRows));
		}
	}

	/**
	 * A patch of color displayed on the JPanel. Has a mouse listener to detect
	 * selection events.
	 */
	private class ColorPatch extends JPanel
	{
		// the color of the patch
		private Color colorOfPatch = null;

		// the state value associated with the patch
		private int stateValueOfPatch = 0;

		// the state value being displayed. Used when drawing the shape on the
		// patch
		private int stateValue = 0;

		// create the patch
		public ColorPatch(Color color, int stateValue)
		{
			this.stateValue = stateValue;

			// if less than MAX_COLUMNS_AT_LARGE_PATCH_SIZE set the preferred
			// size, otherwise, let the chooser handle it
			if(numColumns <= MAX_COLUMNS_AT_LARGE_PATCH_SIZE)
			{
				this.setPreferredSize(LARGE_PATCH_SIZE);
			}
			else if(numColumns <= MAX_COLUMNS_AT_MEDIUM_PATCH_SIZE)
			{
				this.setPreferredSize(MEDIUM_PATCH_SIZE);
			}

			// set a shape and background color
			Shape shape = currentView.getDisplayShape(new IntegerCellState(
					stateValue), this.getWidth(), this.getHeight(),
					new Coordinate(0, 0));
			if(shape == null)
			{
				this.setBackground(color);
			}
			else
			{
				// the color behind the shape
				this.setBackground(ColorScheme.DEFAULT_EMPTY_COLOR);
			}

			colorOfPatch = color;
			stateValueOfPatch = stateValue;

			this.setBorder(BorderFactory.createRaisedBevelBorder());

			// when this panel is clicked, this sets the color of the chooser to
			// the color of this patch
			this.addMouseListener(new ColorPatchListener());

			// set a tool tip that tells them the state of the cell this color
			// represents
			try
			{
				// first check if it is this special case -- not the best
				// connectivity, but useful for backwards compatibility
				FiniteObjectRuleTemplate theRule = (FiniteObjectRuleTemplate) rule;
				this.setToolTipText("cell state "
						+ theRule.intToObjectState(stateValue).toString());
			}
			catch(Exception e)
			{
				this.setToolTipText("cell state " + stateValue);
			}
		}

		// draw the correct shape on the patch
		public void paintComponent(Graphics g)
		{
			// Call the JPanel's paintComponent. This ensures
			// that the background is properly rendered.
			super.paintComponent(g);

			// exclude the border (otherwise the stroke of the border is
			// changed)
			Graphics2D g2 = (Graphics2D) g.create(this.getInsets().left, this
					.getInsets().right, this.getWidth() - this.getInsets().left
					- this.getInsets().right, this.getHeight()
					- this.getInsets().top - this.getInsets().bottom);

			Stroke stroke = currentView.getStroke(new IntegerCellState(
					stateValue), this.getWidth(), this.getHeight(),
					new Coordinate(0, 0));
			if(stroke != null)
			{
				g2.setStroke(stroke);
			}

			// use insets so fits in the space which is smaller due to the
			// raisedBevelBorder
			Shape shape = currentView.getDisplayShape(new IntegerCellState(
					stateValue), this.getWidth() - 2 * this.getInsets().left
					- 2 * this.getInsets().right, this.getHeight() - 2
					* this.getInsets().top - 2 * this.getInsets().bottom,
					new Coordinate(0, 0));

			if(shape != null && colorOfPatch != null)
			{
				// translate the shape to the correct position
				AffineTransform scalingTransform = AffineTransform
						.getTranslateInstance(this.getWidth() / 2.0, this
								.getHeight() / 2.0);
				shape = scalingTransform.createTransformedShape(shape);

				// now draw it
				g2.setColor(colorOfPatch);
				g2.draw(shape);
				g2.fill(shape);
			}
		}

		/**
		 * Listens for when this panel is selected.
		 * 
		 * @author David Bahr
		 */
		private class ColorPatchListener extends MouseAdapter
		{
			public void mouseClicked(MouseEvent e)
			{
				colorSelectionModel.setSelectedColorAndState(colorOfPatch,
						stateValueOfPatch);
			}
		}
	}
}
