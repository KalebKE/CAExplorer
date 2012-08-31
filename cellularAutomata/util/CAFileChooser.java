/*
 CAFileChooser -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.util;

import java.io.File;

import javax.swing.JFileChooser;

/**
 * A file chooser that includes a workaround for java bug 6317789 that does not
 * handle shortcuts properly when choosing a folder in the JFileChooser's drop
 * down menu.
 * 
 * @author David Bahr
 */
public class CAFileChooser extends JFileChooser
{
	/**
	 * Defers to the JFileChooser default constructor.
	 */
	public CAFileChooser()
	{
	}

	/**
	 * Defers to the JFileChooser constructor with the same parameters.
	 */
	public CAFileChooser(File file)
	{
		super(file);
	}

	/**
	 * Defers to the JFileChooser constructor with the same parameters.
	 */
	public CAFileChooser(String currentDirectoryPath)
	{
		super(currentDirectoryPath);
	}

	/**
	 * This is a workaround for java bug 6317789. The bug makes shortcuts fail
	 * in the drop-down menu that lets you choose folders. Otherwise we could
	 * just say "JFileChooser fileChooser = new JFileChooser(file);"
	 * <p>
	 * To see the bug, comment out this method and try to use the drop-down menu
	 * on all of the folders (particularly those that are shortcuts on the
	 * desktop).
	 */
	public void updateUI()
	{
		putClientProperty("FileChooser.useShellFolder", Boolean.FALSE);
		super.updateUI();
	}
};