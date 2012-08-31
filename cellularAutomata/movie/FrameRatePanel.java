/*
 FrameRatePanel -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2008  David B. Bahr (http://academic.regis.edu/dbahr/)

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

package cellularAutomata.movie;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import cellularAutomata.lattice.OneDimensionalLattice;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;

/**
 * Creates a panel that lets the user choose a frame rate.
 * 
 * @author David Bahr
 */
public class FrameRatePanel extends JPanel
{
	// the default frame rate for one-dimensional simulations
	private static final int DEFAULT_ONEDIMENSIONAL_FRAMERATE = 60;

	// the default frame rate for two-dimensional simulations
	private static final int DEFAULT_TWODIMENSIONAL_FRAMERATE = 30;

	private static final String FRAMERATE_TIP = "Sets the speed of the "
			+ "movie. 30 to 60 is normal.";

	// color of titles of sections
	private Color titleColor = Color.BLUE;

	// title font (for titles of sections)
	private Font titleFont = new Fonts().getItalicSmallerFont();

	// selects the frame rate in frames per second (fps)
	private JSpinner frameRateSpinner = null;

	/**
	 * Create the panel that asks for the frame rate.
	 */
	public FrameRatePanel()
	{
		// create a title message
		String title = "Select speed of the movie:";

		// create a message
		String message = "The human eye cannot process frame rates "
				+ "greater than 60 to 100 frames\n"
				+ "per second, but larger values can give the illusion of "
				+ "higher speeds. \n" + "Use "
				+ DEFAULT_TWODIMENSIONAL_FRAMERATE + " or "
				+ DEFAULT_ONEDIMENSIONAL_FRAMERATE + " if in doubt.";
		MultilineLabel label = new MultilineLabel(message);
		label.setFont(new Fonts().getItalicSmallerFont());
		label.setMargin(new Insets(6, 10, 2, 10));
		label.setColumns(41);

		// set the default frame rate
		int defaultFrameRate = DEFAULT_TWODIMENSIONAL_FRAMERATE;
		if(OneDimensionalLattice.isCurrentLatticeOneDim())
		{
			defaultFrameRate = DEFAULT_ONEDIMENSIONAL_FRAMERATE;
		}

		// create spinner for the update interval
		SpinnerNumberModel frameRateModel = new SpinnerNumberModel(
				defaultFrameRate, 1, 1000, 10);
		frameRateSpinner = new JSpinner(frameRateModel);
		frameRateSpinner.setToolTipText(FRAMERATE_TIP);

		// create border
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 3, 2, 3);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), title, TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION, titleFont, titleColor);
		this.setBorder(BorderFactory.createCompoundBorder(outerEmptyBorder,
				titledBorder));

		// add spinner panel and message to this FrameRatePanel
		this.setLayout(new GridBagLayout());
		int theRow = 0;
		this.add(label, new GBC(1, theRow).setSpan(4, 1).setFill(GBC.BOTH)
				.setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));
		theRow++;
		this.add(frameRateSpinner, new GBC(1, theRow).setSpan(4, 1).setFill(
				GBC.VERTICAL).setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
				.setInsets(5, 0, 5, 40));
	}

	/**
	 * Gets the frame rate selected by the user.
	 * 
	 * @return the frame rate in frames per second (fps).
	 */
	public int getFrameRate()
	{
		try
		{
			// make sure the model reflects the users latest changes
			frameRateSpinner.commitEdit();
		}
		catch(Exception e)
		{
			// do nothing
		}

		int frameRate = ((Integer) frameRateSpinner.getValue()).intValue();

		return frameRate;
	}
}
