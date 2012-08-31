/*
 Fonts -- a class within the Cellular Automaton Explorer. 
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

import java.awt.Font;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Creates fonts suitable for the CA graphics.
 * 
 * @author David Bahr
 */
public class Fonts
{
	// fonts are sized relative to the fonts of this component (set in the
	// constructor)
	private JComponent fontComponent = null;

	// font for the components (buttons, etc.)
	private Font boldBiggerFont = null;

	// font for the components (buttons, etc.)
	private Font boldFont = null;

	// font for the components (buttons, etc.)
	private Font boldSmallerFont = null;

	// font for the components (buttons, etc.)
	private Font boldVerySmallFont = null;

	// font for the components (buttons, etc.)
	private Font italicFont = null;

	// font for the components (buttons, etc.)
	private Font italicSmallerFont = null;

	// font for the components (buttons, etc.)
	private Font plainFont = null;

	// font for the components (buttons, etc.)
	private Font plainMiniFont = null;

	// font for the components (buttons, etc.)
	private Font plainSmallerFont = null;

	// font for the components (buttons, etc.)
	private Font plainVerySmallFont = null;

	// font appropriate for titles
	private Font titleFont = null;

	/**
	 * Creates a set of fonts that are sized relative to the fonts of the JPanel
	 * class.
	 */
	public Fonts()
	{
		this(null);
	}

	/**
	 * Creates a set of fonts that are sized relative to the fonts of the given
	 * component. If the supplied component is null, then the fonts are sized
	 * relative to the fonts of the JPanel class.
	 * 
	 * @param component
	 *            The component relative to which the fonts are sized. May be
	 *            null.
	 */
	public Fonts(JComponent component)
	{
		// all fonts will be sized relative to the component or a JPanel if the
		// component is null
		this.fontComponent = component;
		if(this.fontComponent == null)
		{
			this.fontComponent = new JPanel();
		}

		// fonts, sized relative to the fontComponent
		boldBiggerFont = new Font(fontComponent.getFont().getFontName(),
				Font.BOLD, fontComponent.getFont().getSize() + 2);
		boldFont = new Font(fontComponent.getFont().getFontName(), Font.BOLD,
				fontComponent.getFont().getSize());
		boldSmallerFont = new Font(fontComponent.getFont().getFontName(),
				Font.BOLD, fontComponent.getFont().getSize() - 1);
		italicFont = new Font(fontComponent.getFont().getFontName(),
				Font.ITALIC, fontComponent.getFont().getSize());
		italicSmallerFont = new Font(fontComponent.getFont().getFontName(),
				Font.ITALIC, fontComponent.getFont().getSize() - 1);
		plainVerySmallFont = new Font(fontComponent.getFont().getFontName(),
				Font.PLAIN, fontComponent.getFont().getSize() - 3);
		boldVerySmallFont = new Font(fontComponent.getFont().getFontName(),
				Font.BOLD, fontComponent.getFont().getSize() - 3);
		plainMiniFont = new Font(fontComponent.getFont().getFontName(),
				Font.PLAIN, fontComponent.getFont().getSize() - 5);
		plainFont = new Font(fontComponent.getFont().getFontName(), Font.PLAIN,
				fontComponent.getFont().getSize());
		plainSmallerFont = new Font(fontComponent.getFont().getFontName(),
				Font.PLAIN, fontComponent.getFont().getSize() - 1);
		titleFont = new Font(fontComponent.getFont().getFontName(), Font.BOLD
				| Font.ITALIC, fontComponent.getFont().getSize() + 2);
	}

	/**
	 * A font used for descriptions in analyses.
	 * 
	 * @return a font appropriate for the description in analyses.
	 */
	public Font getAnalysesDescriptionFont()
	{
		return plainFont;
	}

	/**
	 * A bigger bold font.
	 * 
	 * @return a bigger bold font.
	 */
	public Font getBoldBiggerFont()
	{
		return boldBiggerFont;
	}

	/**
	 * A bold font.
	 * 
	 * @return a bold font.
	 */
	public Font getBoldFont()
	{
		return boldFont;
	}

	/**
	 * A bold font of the specified size.
	 * 
	 * @param size
	 *            The size of the font.
	 * @return a bold font.
	 */
	public Font getBoldFont(int size)
	{
		return new Font(fontComponent.getFont().getFontName(), Font.BOLD, size);
	}

	/**
	 * A bold and italic font of the specified size.
	 * 
	 * @param size
	 *            The size of the font.
	 * @return a bold and italic font.
	 */
	public Font getBoldItalicFont(int size)
	{
		return new Font(fontComponent.getFont().getFontName(), Font.BOLD
				| Font.ITALIC, size);
	}

	/**
	 * A smaller bold font.
	 * 
	 * @return a smaller bold font.
	 */
	public Font getBoldSmallerFont()
	{
		return boldSmallerFont;
	}

	/**
	 * A very small bold font.
	 * 
	 * @return a very small bold font.
	 */
	public Font getBoldVerySmallFont()
	{
		return boldVerySmallFont;
	}

	/**
	 * An italic font.
	 * 
	 * @return an italic font.
	 */
	public Font getItalicFont()
	{
		return italicFont;
	}

	/**
	 * An italic font of the specified size.
	 * 
	 * @param size
	 *            The size of the font.
	 * @return an italic font.
	 */
	public Font getItalicFont(int size)
	{
		return new Font(fontComponent.getFont().getFontName(), Font.ITALIC,
				size);
	}

	/**
	 * A smaller italic font.
	 * 
	 * @return a smaller italic font.
	 */
	public Font getItalicSmallerFont()
	{
		return italicSmallerFont;
	}

	/**
	 * A font used for descriptions in More Properties panels.
	 * 
	 * @return a font appropriate for the description in More Properties.
	 */
	public Font getMorePropertiesDescriptionFont()
	{
		return plainFont;
	}

	/**
	 * A plain font.
	 * 
	 * @return a plain font.
	 */
	public Font getPlainFont()
	{
		return plainFont;
	}

	/**
	 * A very small plain font.
	 * 
	 * @return a very small plain font.
	 */
	public Font getPlainMiniFont()
	{
		return plainMiniFont;
	}

	/**
	 * A small plain font.
	 * 
	 * @return a small plain font.
	 */
	public Font getPlainSmallerFont()
	{
		return plainSmallerFont;
	}

	/**
	 * A very small plain font.
	 * 
	 * @return a very small plain font.
	 */
	public Font getPlainVerySmallFont()
	{
		return plainVerySmallFont;
	}

	/**
	 * A title font.
	 * 
	 * @return a title font.
	 */
	public Font getTitleFont()
	{
		return titleFont;
	}
}
