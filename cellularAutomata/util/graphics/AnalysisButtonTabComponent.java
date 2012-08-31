package cellularAutomata.util.graphics;

/*
 * Significantly modified by David Bahr, November 7, 2008.
 */

/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;

import cellularAutomata.analysis.Analysis;

/**
 * Component to be used as tabComponent for Analysis tabs. Contains a JLabel to
 * show the text and a JButton to close the tab to which it belongs. An Analysis
 * is passed in so that proper closing actions can be taken by the Analysis.
 */
public class AnalysisButtonTabComponent extends JPanel
{
	// the tabbed pane that holds the tab with this tabComponent
	// private final JTabbedPane pane = null;

	// the analysis that is being closed
	private Analysis analysis = null;

	// Color of the text and "x" button
	private Color foregroundColor = null;

	// color of the "x" button when the mouse hovers over it
	private Color rolloverColor = Color.RED;

	// thickness of the lines that make up the "x" button
	private float strokeThickness = 2.5f;

	/**
	 * Build a tab component from a label and a closing "x" button.
	 * 
	 * @param tabbedPane
	 *            The tabbed pane that has a tab with this tabComponent.
	 * @param analysis
	 *            The CA analysis that is shown in the tab.
	 */
	public AnalysisButtonTabComponent(final JTabbedPane tabbedPane,
			Analysis analysis)
	{
		// unset the default FlowLayout's gaps
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));

		if(tabbedPane == null)
		{
			throw new NullPointerException("TabbedPane is null");
		}
		// this.pane = tabbedPane;

		this.analysis = analysis;

		setOpaque(false);

		// make JLabel read titles, fonts, and enabled status from JTabbedPane
		JLabel label = new JLabel()
		{
			public String getText()
			{
				int i = tabbedPane
						.indexOfTabComponent(AnalysisButtonTabComponent.this);
				if(i != -1)
				{
					return tabbedPane.getTitleAt(i);
				}
				return null;
			}

			public Font getFont()
			{
				return tabbedPane.getFont();
			}

			public boolean isEnabled()
			{
				int i = tabbedPane
						.indexOfTabComponent(AnalysisButtonTabComponent.this);
				if(i != -1)
				{
					return tabbedPane.isEnabledAt(i);
				}
				return true;
			}
		};

		// set the color from the tabbed pane
		foregroundColor = tabbedPane.getForeground();
		label.setForeground(foregroundColor);

		add(label);

		// add more space between the label and the button
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
		// label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

		// tab button
		JButton button = new TabButton();

		// add more space between the button and the top of the tab
		button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		add(button);

		// Add more space to the top of the component.
		// Not used because this might interfere with the CATabbedPaneUI.
		// setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
	}

	/**
	 * The "x" button that closes the tab.
	 */
	private class TabButton extends JButton implements ActionListener
	{
		public TabButton()
		{
			int size = 17;
			setPreferredSize(new Dimension(size, size));
			setToolTipText("close this analysis");

			// Make the button looks the same for all Laf's
			setUI(new BasicButtonUI());

			// Make it transparent
			setContentAreaFilled(false);

			// No need to be focusable
			setFocusable(false);
			setBorder(BorderFactory.createEtchedBorder());
			setBorderPainted(false);

			// Make the nice rollover effect. Note that we use the same listener
			// for all tabCompnent buttons.
			addMouseListener(buttonMouseListener);
			setRolloverEnabled(true);

			// Close the proper tab by clicking the button
			addActionListener(this);
		}

		/**
		 * What to do when the close "x" button is pressed.
		 */
		public void actionPerformed(ActionEvent e)
		{
			analysis.getCloseFrameButton().doClick();

			// the closeFrameButton takes care of this and more, so commented
			// out this code
			//
			// int i =
			// pane.indexOfTabComponent(AnalysisButtonTabComponent.this);
			// if(i != -1)
			// {
			// pane.remove(i);
			// }
		}

		// we don't want to update UI for this button
		public void updateUI()
		{
		}

		/**
		 * Paint the "x" on the button.
		 */
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();

			// shift down slightly to look more in line with the text
			g2.translate(0, 1);

			// shift the image for pressed buttons
			if(getModel().isPressed())
			{
				g2.translate(1, 1);
			}

			// set color and stroke (thickness)
			g2.setStroke(new BasicStroke(strokeThickness));
			g2.setColor(foregroundColor);
			if(getModel().isRollover())
			{
				g2.setColor(rolloverColor);
			}

			int delta = 5; // 6;
			g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight()
					- delta - 1);
			g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight()
					- delta - 1);
			g2.dispose();
		}
	}

	/**
	 * Draw a box around the "x" when the mouse is hovering over it. Because the
	 * default button border is set to an empty border (in the constructor of
	 * the AnalysisButtonTabComponent), this doesn't do anything (unless the
	 * border is changed).
	 */
	private final static MouseListener buttonMouseListener = new MouseAdapter()
	{
		public void mouseEntered(MouseEvent e)
		{
			Component component = e.getComponent();
			if(component instanceof AbstractButton)
			{
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(true);
			}
		}

		public void mouseExited(MouseEvent e)
		{
			Component component = e.getComponent();
			if(component instanceof AbstractButton)
			{
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(false);
			}
		}
	};
}
