/*
 IntegerColorChooserPreviewPanel -- a class within the Cellular Automaton Explorer. 
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

import java.awt.Color;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.graphics.colors.colorChooser.IntegerColorSelectionModel;
import cellularAutomata.graphics.colors.colorChooser.IntegerStateColorChooser;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.rules.Rule;
import cellularAutomata.rules.templates.FiniteObjectRuleTemplate;
import cellularAutomata.util.Coordinate;

/**
 * A preview panel that displays the selected color.
 * 
 * @author David Bahr
 */
public class IntegerColorChooserPreviewPanel extends JPanel
{
	// the border of the preview panel
	private Border border = null;

	// the current view
	private CellStateView currentView = null;

	// The currently selected color
	private Color currentColor = null;

	// the currently selected state
	private int currentState = 0;

	// the rectangle drawn on the preview panel
	private Rectangle2D.Float previewRectangle = new Rectangle2D.Float();

	// the shape specified by the view (reset by the listener)
	private Shape shape = null;

	// the stroke set by the view
	private Stroke stroke = null;

	/**
	 * Create a preview panel that shows the selected color.
	 * 
	 * @param chooser
	 *            The color chooser that will use this preview panel.
	 */
	public IntegerColorChooserPreviewPanel(IntegerStateColorChooser chooser)
	{
		// Initialize the currently selected color and state
		currentColor = chooser.getColor();
		currentState = chooser.getState();

		// Add listener on model to detect changes to selected color
		ColorSelectionModel model = chooser.getSelectionModel();
		model.addChangeListener(new ChangedColorListener(this));

		// set a title and border
		border = BorderFactory.createTitledBorder("Selected state preview");
		this.setBorder(border);

		// get the current view
		String classNameOfRule = CurrentProperties.getInstance()
				.getRuleClassName();
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(classNameOfRule);
		currentView = rule.getCompatibleCellStateView();

		// set a tool tip that tells them the state of the cell this color
		// represents
		setToolTip(currentState, rule);
	}

	/**
	 * Set the tooltip.
	 * 
	 * @param currentState
	 *            The currently selected state.
	 */
	private void setToolTip(int currentState)
	{
		// get the current view
		String classNameOfRule = CurrentProperties.getInstance()
				.getRuleClassName();
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(classNameOfRule);

		setToolTip(currentState, rule);
	}

	/**
	 * Set the tooltip.
	 * 
	 * @param currentState
	 *            The currently selected state.
	 * @param rule
	 *            The current CA rule.
	 */
	private void setToolTip(int currentState, Rule rule)
	{
		try
		{
			// first check if it is this special case -- not the best
			// connectivity, but useful for backwards compatibility
			FiniteObjectRuleTemplate theRule = (FiniteObjectRuleTemplate) rule;
			this.setToolTipText("cell state "
					+ theRule.intToObjectState(currentState).toString());
		}
		catch(Exception e)
		{
			this.setToolTipText("cell state " + currentState);
		}
	}

	// Paint current color
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		// exclude the border (otherwise the stroke of the border is changed)
		Graphics2D g2 = (Graphics2D) g.create(this.getInsets().left, this
				.getInsets().right, this.getWidth() - this.getInsets().left
				- this.getInsets().right, this.getHeight()
				- this.getInsets().top - this.getInsets().bottom);

		// The height of the preview panel's display area (excluding the
		// border). The height AND the width is given by the height variable.
		int height = this.getHeight() - 2 * this.getInsets().top - 2
				* this.getInsets().bottom;

		// get the current stroke
		stroke = currentView.getStroke(new IntegerCellState(currentState),
				height, height, new Coordinate(0, 0));

		// get the current state (use insets so fits in the space which is
		// smaller due to the raisedBevelBorder)
		shape = currentView.getDisplayShape(new IntegerCellState(currentState),
				height, height, new Coordinate(0, 0));

		if(shape != null && currentColor != null)
		{
			// translate the shape to the correct position
			AffineTransform scalingTransform = AffineTransform
					.getTranslateInstance(this.getWidth() / 2.0, this
							.getHeight() / 2.0);
			shape = scalingTransform.createTransformedShape(shape);
		}
		else
		{
			previewRectangle.setRect(this.getInsets().left,
					this.getInsets().top, height, height);

			// translate the shape to the correct position
			AffineTransform scalingTransform = AffineTransform
					.getTranslateInstance(this.getWidth() / 2.0 - height / 2.0,
							0.0);
			shape = scalingTransform.createTransformedShape(previewRectangle);
		}

		// reset the stroke
		if(stroke != null)
		{
			g2.setStroke(stroke);
		}

		// reset the color
		g2.setColor(currentColor);

		// now draw the shape
		g2.draw(shape);
		g2.fill(shape);
	}

	/**
	 * A listener for changes in the selected color.
	 * 
	 * @author David Bahr
	 */
	private class ChangedColorListener implements ChangeListener
	{
		IntegerColorChooserPreviewPanel panel = null;

		/**
		 * Create a change listener.
		 * 
		 * @param panel
		 *            The panel that will be updated if there is a change.
		 */
		public ChangedColorListener(IntegerColorChooserPreviewPanel panel)
		{
			this.panel = panel;
		}

		/**
		 * What to do if there is a change.
		 * 
		 * @param e
		 */
		public void stateChanged(ChangeEvent e)
		{
			IntegerColorSelectionModel model = (IntegerColorSelectionModel) e
					.getSource();

			// Get the new color value
			currentColor = model.getSelectedColor();

			// Get the new state value
			currentState = model.getSelectedState();

			// reset the tool tip
			setToolTip(currentState);

			panel.repaint();
		}
	}
}
