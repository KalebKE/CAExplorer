/*
 CAHyperlinkListener -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.util;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import cellularAutomata.CurrentProperties;
import cellularAutomata.help.BrowserLoader;
import cellularAutomata.util.browser.CABrowser;

/**
 * A listener for hyperlink events.
 * 
 * @author David Bahr
 */
public class CAHyperlinkListener implements HyperlinkListener
{
	// the pane where links will be updated (if not null)
	private JEditorPane pane = null;

	// the browser where links will be updated (if not null)
	private CABrowser browser = null;

	/**
	 * Creates a listener that updates the link in the given editor pane.
	 * 
	 * @param pane
	 *            The editor pane that is updated by the link.
	 */
	public CAHyperlinkListener(JEditorPane pane)
	{
		this.pane = pane;
	}

	/**
	 * Creates a listener that updates the link in the given CA browser.
	 * 
	 * @param browser
	 *            The CA browser that is updated by the link.
	 */
	public CAHyperlinkListener(CABrowser browser)
	{
		this.browser = browser;
	}

	/**
	 * What to do when a hyperlink is clicked.
	 * 
	 * @param e
	 */
	public void hyperlinkUpdate(HyperlinkEvent e)
	{
		if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
		{
			// make room for some potentially big images!
			System.gc();

			JEditorPane pane = (JEditorPane) e.getSource();
			if(e instanceof HTMLFrameHyperlinkEvent)
			{
				// this updates only an internal frame (when that is
				// necessary)
				HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
				HTMLDocument doc = (HTMLDocument) pane.getDocument();
				doc.processHTMLFrameHyperlinkEvent(evt);
			}
			else
			{
				// this updates the whole document
				boolean displayInBrowser = CurrentProperties.getInstance()
						.isDisplayHyperLinksInBrowser();

				if(displayInBrowser)
				{
					BrowserLoader.displayURL(e.getURL().toString());
				}
				else
				{
					try
					{
						if(browser != null)
						{
							browser.setURL(e.getURL());
						}
						else if(pane != null)
						{
							pane.setPage(e.getURL());
						}
					}
					catch(Throwable t)
					{
						JOptionPane
								.showMessageDialog(
										pane,
										"Sorry, cannot load that page, most likely \n"
												+ "due to a bad URL or a memory leak in Java.",
										"Cannot load page",
										JOptionPane.ERROR_MESSAGE, null);
					}
				}
			}
		}
	}
}
