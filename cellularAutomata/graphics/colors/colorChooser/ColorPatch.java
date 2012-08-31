/*
 ColorPatch -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.graphics.colors.colorChooser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.rules.Rule;
import cellularAutomata.rules.templates.FiniteObjectRuleTemplate;
import cellularAutomata.util.Coordinate;

/**
 * A patch of color displayed on the JPanel.
 */
public class ColorPatch extends JPanel
{
	// the color of the patch
	private Color colorOfPatch = null;

	private Color defaultColor = new Color(204, 204, 204);

	// the size of the patch
	private Dimension patchSize = new Dimension(25, 25);

	// the view used to display this cell state
	private CellStateView view = null;

	// the ca rule
	private Rule rule = null;

	// the state value being displayed. Used when drawing the shape on the
	// patch
	private int stateValue = 0;

	/**
	 * Create the patch with the given color and state.
	 */
	public ColorPatch(Rule rule, Color color, int stateValue, int numStates)
	{
		this.stateValue = stateValue;

		this.view = rule.getCompatibleCellStateView();

		this.rule = rule;

		this.setPreferredSize(patchSize);
		this.setBorder(BorderFactory.createRaisedBevelBorder());

		setColorAndState(color, stateValue);
	}

	/**
	 * Set the color and state of the patch.
	 */
	public void setColorAndState(Color color, int stateValue)
	{
		this.stateValue = stateValue;

		// set a shape and background color
		Coordinate rowAndCol = new Coordinate(0, 0);
		Shape shape = view.getDisplayShape(new IntegerCellState(stateValue),
				this.getWidth(), this.getHeight(), rowAndCol);
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

	/**
	 * Set a default color and state for the patch;
	 */
	public void setDefaultColorAndState()
	{
		stateValue = 0;
		colorOfPatch = defaultColor;
		this.setBackground(defaultColor);
	}

	// draw the correct shape on the patch
	public void paintComponent(Graphics g)
	{
		// Call the JPanel's paintComponent. This ensures
		// that the background is properly rendered.
		super.paintComponent(g);

		if(IntegerCellState.isCompatibleRule(rule))
		{
			// exclude the border (otherwise the stroke of the border is
			// changed)
			Graphics2D g2 = (Graphics2D) g.create(this.getInsets().left, this
					.getInsets().right, this.getWidth() - this.getInsets().left
					- this.getInsets().right, this.getHeight()
					- this.getInsets().top - this.getInsets().bottom);

			try
			{
				Stroke stroke = view.getStroke(
						new IntegerCellState(stateValue), this.getWidth(), this
								.getHeight(), new Coordinate(0, 0));
				if(stroke != null)
				{
					g2.setStroke(stroke);
				}

				// use insets so fits in the space which is smaller due to
				// the raisedBevelBorder
				Shape shape = view
						.getDisplayShape(new IntegerCellState(stateValue), this
								.getWidth()
								- 2
								* this.getInsets().left
								- 2
								* this.getInsets().right, this.getHeight() - 2
								* this.getInsets().top - 2
								* this.getInsets().bottom, new Coordinate(0, 0));

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
			catch(Exception e)
			{
				// fails if not an IntegerCellState -- do nothing
			}
		}
	}
}