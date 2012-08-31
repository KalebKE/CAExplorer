/*
 ToolTipLatticeComboBox -- a class within the Cellular Automaton Explorer. 
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
import java.util.List;

import javax.swing.JToolTip;

import cellularAutomata.lattice.FourNeighborSquareLattice;
import cellularAutomata.lattice.HexagonalLattice;
import cellularAutomata.lattice.SquareLattice;
import cellularAutomata.lattice.StandardOneDimensionalLattice;
import cellularAutomata.lattice.TriangularLattice;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.reflection.LatticeHash;
import cellularAutomata.rules.Rule;

/**
 * Create a combo box with tooltips that come from the selected lattice.
 * 
 * @author David Bahr
 */
public class ToolTipLatticeComboBox extends ToolTipComboBox
{
	/**
	 * Tooltip used when the lattice is disabled.
	 */
	public final static String NOT_AVAILABLE_WITH_RULE_TOOLTIP = "Not available "
			+ "with the current rule.";

	/**
	 * The default tool tip if no other is specified.
	 */
	public static String DEFAULT_TIP = "<html>Geometry of the simulation.  Place cursor <br>"
			+ "over the lattice name for a detailed description.</html>";

	/**
	 * The tool tip if the rule fails to provide one.
	 */
	private static String EMPTY_TIP = "<html>No description of this "
			+ "lattice is available.</html>";

	/**
	 * Create a combo box with tooltips that come from the selected lattice.
	 * 
	 * @param items
	 *            The rule on the combo box list.
	 */
	public ToolTipLatticeComboBox(Object[] items)
	{
		super(items);
	}

	/**
	 * Color the lattice items that are most commonly used. This gives the user
	 * a visual cue.
	 */
	public void colorMostCommonLattices()
	{
		// color the most widely used lattices
		this.setItemColored(SquareLattice.DISPLAY_NAME, Color.BLUE.darker());
		this.setItemColored(StandardOneDimensionalLattice.DISPLAY_NAME,
				Color.BLUE.darker());

		// now color the next-most common lattices
		this.setItemColored(HexagonalLattice.DISPLAY_NAME, Color.BLUE.darker()
				.darker());
		this.setItemColored(TriangularLattice.DISPLAY_NAME, Color.BLUE.darker()
				.darker());
		this.setItemColored(FourNeighborSquareLattice.DISPLAY_NAME, Color.BLUE
				.darker().darker());
	}

	/**
	 * Disables any lattices that are not compatible with the specified rule.
	 * 
	 * @param ruleClassName
	 *            the class name of the specified rule. Any lattices
	 *            incompatible with this rule will be disabled.
	 */
	public void enableOnlyCompatibleLattices(String ruleClassName)
	{
		if(ruleClassName != null && !ruleClassName.equals(""))
		{
			Rule rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(ruleClassName);
			if(rule != null)
			{
				String[] compatibleLatticeNames = rule.getCompatibleLattices();

				// if latticeNames is null, then it is compatible with
				// all lattices, so don't disable anything
				if(compatibleLatticeNames != null)
				{
					// Need a list of ALL lattices, and should only disable
					// those that are not in the above latticeNames list
					LatticeHash latticeHash = new LatticeHash();
					List<String> allLatticeNames = latticeHash.toList();
					for(String name : compatibleLatticeNames)
					{
						allLatticeNames.remove(name);
					}
					
					for(String latticeName : allLatticeNames)
					{
						// disable the lattice item
						this.setEnabled(latticeName, false);
					}

					// now enable all the other lattices
					for(String latticeName : compatibleLatticeNames)
					{						
						// enable the lattice item
						this.setEnabled(latticeName, true);
					}
				}
				else
				{
					// enable all lattices

					// Need a list of ALL lattices, and should enable
					// them all
					LatticeHash latticeHash = new LatticeHash();
					List<String> allLatticeNames = latticeHash.toList();
					for(String latticeName : allLatticeNames)
					{
						// enable the lattice item
						this.setEnabled(latticeName, true);
					}
				}
			}
		}
	}

	/**
	 * Gets the tool tip for the currently selected item.
	 * 
	 * @return The tool tip.
	 */
	public String getTip(Object theSelectedItem)
	{
		// get the item that is selected
		String selectedItem = (String) theSelectedItem;

		// convert to its class name
		LatticeHash latticeHash = new LatticeHash();
		String className = latticeHash.get(selectedItem);

		// now get the tool tip
		String tip = ReflectionTool.getLatticeToolTipFromClassName(className);

		// create default tip
		if(tip == null || tip.equals(""))
		{
			tip = EMPTY_TIP;
		}

		// make sure it isn't disabled
		for(Object item : super.getDisabledList())
		{
			if(item.equals(theSelectedItem))
			{
				tip = NOT_AVAILABLE_WITH_RULE_TOOLTIP;
			}
		}

		return tip;
	}
}
