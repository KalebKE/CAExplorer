/*
 ToolTipComboBox -- a class within the Cellular Automaton Explorer. 
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
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;

import cellularAutomata.CAConstants;

/**
 * Creates a combo box that uses a tool tip. Child classes must specify the
 * tooltip that will be used for each element of the list displayed in the combo
 * box. See ToolTipRuleComboBox as an example.
 * 
 * @author David Bahr
 */
public abstract class ToolTipComboBox extends JComboBox
{
	// specially selected items are painted this color
	private Color specialItemColor = Color.BLUE;

	// the color of disabled items
	private final static Color DISABLED_COLOR = Color.LIGHT_GRAY;

	// a list of items that will be colored.
	private LinkedList<Object> colorItemsList = null;

	// a list of colors that each corresponding item will be painted.
	private LinkedList<Color> colorsList = null;

	// a list of combo box items that have been disabled.
	private LinkedList<Object> disabledList = null;

	// A rendered for each list element.
	// This is part of a crazy workaround so that the tooltips work on macs
	// This is not necessary on pcs. This creates a renderer for each list
	// element so that the tooltip can be set every time the renderer is
	// called. *AND* using a renderer does allow the tooltip to appear while
	// the list items are being scrolled -- that is nice!
	private ToolTipCellRenderer listRenderer = new ToolTipCellRenderer();

	// this is part of a crazy workaround so that the tooltips work on macs
	// This is not necessary on pcs. This creates a copy of this combo box so
	// that the tooltip can be set every time the renderer is called.
	private ToolTipComboBox thisComboBox = null;

	/**
	 * Create a combo box with tool tips that change for each element on the
	 * list.
	 * 
	 * @param items
	 *            The items on the list in the combo box.
	 */
	public ToolTipComboBox(Object[] items)
	{
		super(items);

		// make sure the tool tip manager knows about this component
		ToolTipManager.sharedInstance().registerComponent(this);

		// this is part of a crazy workaround so that the tooltips work on macs
		// This is not necessary on pcs. This creates a renderer for each list
		// element so that the tooltip can be set every time the renderer is
		// called. *BUT* using a custom renderer does allow the tooltip to
		// appear while the list items are being scrolled -- that is nice!
		this.setRenderer(listRenderer);

		// make the tip stay visible indefinitely
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

		// this is part of a crazy workaround so that the tooltips work on macs
		// This is not necessary on pcs. This creates a copy of the combo box so
		// that the tooltip can be set every time the renderer is called. Using
		// a custom renderer does allow the tooltip to appear while
		// the list items are being scrolled -- that is nice!
		thisComboBox = this;
	}

	/**
	 * Gets the list of disabled combo box items.
	 * 
	 * @return The list of disabled items.
	 */
	public List<Object> getDisabledList()
	{
		return disabledList;
	}

	/**
	 * Gets the tool tip for the currently selected item.
	 * 
	 * @return The tool tip.
	 */
	public abstract String getTip(Object theSelectedItem);

	/**
	 * Overrides the getToolTip from JComponent so that the tool tip depends on
	 * the selected item. This simple solution works very well on pcs, but this
	 * does not work on a Mac. Grrr. So the custom renderer is used below for
	 * Macs. Should not need the custom renderer. Should only need this
	 * overridden method. However, using a custom renderer does allow the
	 * tooltip to appear while the list items are being scrolled -- that is
	 * nice!
	 * 
	 * @return The tool tip.
	 */
	public String getToolTipText(MouseEvent e)
	{
		return getTip(this.getSelectedItem());
	}

	/**
	 * True if the selected item is enabled.
	 * 
	 * @return true if the selected item is enabled.
	 */
	public boolean isSelectedItemEnabled()
	{
		boolean enabled = true;

		Object selectedItem = getSelectedItem();
		if(disabledList.contains(selectedItem))
		{
			enabled = false;
		}

		return enabled;
	}

	/**
	 * Enables or disables the item if it exists. The item can still be
	 * selected, but it is colored grey.
	 * 
	 * @param item
	 *            The list item that will be enabled or disabled.
	 * @param enable
	 *            Whether or not to enable (true) or disable (false).
	 */
	public void setEnabled(Object item, boolean enable)
	{
		// create a list of disabled items if haven't already
		if(disabledList == null)
		{
			disabledList = new LinkedList<Object>();
		}

		if(enable)
		{
			// remove from the disabled list (if it is in the list)
			while(disabledList.contains(item))
			{
				disabledList.remove(item);
			}
		}
		else
		{
			// add the item to the list of disabled items
			disabledList.add(item);
		}
	}

	/**
	 * Sets a list of combo box items that will be painted a special color.
	 * Calling this method multiple times will set multiple items.
	 * 
	 * @param item
	 *            The combo box item that will be colored.
	 * @param color
	 *            The color that the item will be painted. May be null which
	 *            will use a default color.
	 */
	public void setItemColored(Object item, Color color)
	{
		if(colorItemsList == null)
		{
			colorItemsList = new LinkedList<Object>();
		}

		colorItemsList.add(item);

		if(colorsList == null)
		{
			colorsList = new LinkedList<Color>();
		}

		colorsList.add(color);
	}

	// /**
	// * Override the createToolTip method in the JComboBox so that it can
	// change
	// * to a warning color when a list item is disabled.
	// */
	// public JToolTip createToolTip()
	// {
	// CAToolTip caToolTip = new CAToolTip(this);
	//
	// ToolTipCellRenderer renderer = (ToolTipCellRenderer) this.getRenderer();
	//
	// if((renderer != null)
	// && (renderer.getToolTipText() != null)
	// && renderer.getToolTipText().contains(
	// NOT_AVAILABLE_WITH_RULE_TOOLTIP))
	// {
	// caToolTip.setToWarningColor();
	// }
	//
	// return caToolTip;
	// }

	/**
	 * This is part of a crazy workaround so that the tooltips work on macs.
	 * This is not necessary on pcs. The renderer gets called AND (AS A
	 * WORKAROUND) resets the tooltip on this ToolTipComboBox. Using a custom
	 * renderer does allow the tooltip to appear while the list items are being
	 * scrolled -- that is nice! Also lets me disable specific items in the
	 * list.
	 */
	private class ToolTipCellRenderer extends DefaultListCellRenderer
	{
		// the default renderer used to display an item in the list
		private DefaultListCellRenderer renderer = new DefaultListCellRenderer();

		// the default cell color
		private Color defaultCellColor = renderer.getForeground();

		// background color of the selected item
		private Color defaultBackground = new JList().getSelectionBackground();

		/**
		 * Returns the component used to display the list item, and as a
		 * workaround it also resets the tooltip.
		 */
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean hasFocus)
		{
			// just use the default renderer
			renderer.getListCellRendererComponent(list, value, index,
					isSelected, hasFocus);

			// NOW RESET THE TOOLTIP. THIS IS A NECESSARY WORKAROUND FOR MACS
			// WHICH DON'T RECOGNIZE/CALL THE OVERRIDEN
			// GETTOOLTIPTEXT(MOUSEEVENT) METHOD. GRRR.

			// get the tool tip
			String tip = getTip(value);

			// set the tooltip on the renderer (this also lets the user see the
			// tooltip while they are scrolling the list -- nice)
			renderer.setToolTipText(tip);

			// and most importantly (as a workaround) reset the tooltip on the
			// whole combo box.
			if(CAConstants.MAC_OS)
			{
				if(isSelected)
				{
					thisComboBox.setToolTipText(tip);
				}
			}

			// to start, set the item to the default color (so that if there is
			// no special color below, it still shows up). Also prevents a bug
			// where the item might have been disabled with the last rule, and
			// now needs to be enabled.
			renderer.setForeground(defaultCellColor);
			thisComboBox.setForeground(defaultCellColor);
			list.setSelectionForeground(defaultCellColor);

			// color any list elements that the setItemsColored() method has
			// requested
			Object[] colorItems = colorItemsList.toArray();
			Color[] colors = new Color[1];
			colors = colorsList.toArray(colors);
			for(int i = 0; i < colorItems.length; i++)
			{
				if(value.equals(colorItems[i]))
				{
					if(colors[i] != null)
					{
						renderer.setForeground(colors[i]);

						if(getSelectedItem().equals(colorItems[i]))
						{
							thisComboBox.setForeground(colors[i]);

							// after selecting an item, this makes sure the
							// JList displays it in the correct color.
							list.setSelectionForeground(colors[i]);
							list.setSelectionBackground(defaultBackground);
						}
					}
					else
					{
						renderer.setForeground(specialItemColor);

						if(getSelectedItem().equals(colorItems[i]))
						{
							thisComboBox.setForeground(specialItemColor);

							// after selecting an item, this makes sure the
							// JList displays it in the correct color.
							list.setSelectionForeground(specialItemColor);
							list.setSelectionBackground(defaultBackground);
						}
					}
				}
			}

			// now deal with the item if it has been disabled
			if(disabledList != null)
			{
				for(Object item : disabledList)
				{
					if(value.equals(item))
					{
						renderer.setForeground(DISABLED_COLOR);
					}

					if(getSelectedItem().equals(item))
					{
						thisComboBox.setForeground(DISABLED_COLOR);

						// after selecting an item, this makes sure the
						// JList displays it in the correct color.
						list.setSelectionForeground(DISABLED_COLOR);
						list.setSelectionBackground(this.getBackground());
					}
				}
			}

			return renderer;
		}
	}
}