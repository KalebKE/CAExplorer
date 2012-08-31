/*
 DescriptionPanel -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import cellularAutomata.CurrentProperties;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.CAHyperlinkListener;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.browser.CABrowser;
import cellularAutomata.util.files.CARuleDescriptionLoader;

/**
 * The panel that contains a description of the currently active CA rule.
 * 
 * @author David Bahr
 */
public class DescriptionPanel extends JPanel
{
	/**
	 * Title for the rule panel.
	 */
	public static final String DESCRIPTION_TAB_TITLE = "Current Rule Description";

	/**
	 * String used for text display on the enlarge button.
	 */
	public static final String ENLARGE_DESCRIPTION_HTML = "Enlarge Description";

	/**
	 * String used for the enlarge button's action command.
	 */
	public static final String ENLARGE_DESCRIPTION_ACTION_COMMAND = "Description "
			+ "Panel Enlarge Command";

	/**
	 * A tool tip for the description panel.
	 */
	public static final String TOOL_TIP = "<html><body>description of the "
			+ "currently active rule</body></html>";

	// height of the rule description panel
	private static final int HEIGHT_RULE_DESCRIPTION_PANEL = 200;

	// width of the rule description panel
	// private static final int WIDTH_RULE_DESCRIPTION = 350;

	// the title of the panel
	private static final String DESCRIPTION_PANEL_TITLE = "Current Rule Description";

	// The encompassing panel onto which this one will be added.
	private AllPanel outerPanel = null;

	// A listener for components on this panel.
	private AllPanelListener listener = null;

	// color for the titles of sections
	private Color titleColor = Color.BLUE;

	// title font (for titles of sections)
	private Font titleFont = null;

	// fonts for display
	private Fonts fonts = null;

	// the button that enlarges the html
	private JButton enlargeButton = null;

	// the editor pane used to display the html description of the rule
	private JEditorPane editorPane = null;

	// the frame used to display a larger version of the html.
	private CABrowser enlargedDescriptionFrame = null;

	// the rule's description panel
	private JPanel descriptionPanel = null;

	// The inner panel that holds all of the buttons
	private JPanel innerRaisedPanel = null;

	// the scroll panel that holds the rule description
	private JScrollPane editorScrollPane = null;

	/**
	 * The panel that contains a description of the currently active CA rule.
	 * 
	 * @param outerPanel
	 *            The encompassing panel onto which this one will be added.
	 */
	public DescriptionPanel(AllPanel outerPanel)
	{
		super();

		this.outerPanel = outerPanel;
		this.listener = outerPanel.getAllPanelListener();

		this.setOpaque(true);

		// fonts for the components (buttons, etc.)
		fonts = new Fonts();
		titleFont = new Fonts().getItalicSmallerFont();

		// add the components
		addComponents();
	}

	/**
	 * Create the panel that holds the description.
	 */
	private void addComponents()
	{
		// in case this has been called before, clear it out.
		this.removeAll();

		// create the inner panel
		innerRaisedPanel = createInnerPanel();
		JScrollPane innerScrollPanel = new JScrollPane(innerRaisedPanel);
		innerScrollPanel.setBorder(BorderFactory.createEmptyBorder());
		innerScrollPanel
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		int width = CAFrame.tabbedPaneDimension.width
				- innerScrollPanel.getInsets().left
				- innerScrollPanel.getInsets().right;
		int height = innerRaisedPanel.getMinimumSize().height;
		innerRaisedPanel.setPreferredSize(new Dimension(width, height));

		// create a layout
		this.setLayout(new GridBagLayout());
		this.add(innerScrollPanel, new GBC(0, 1).setSpan(1, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
				.setInsets(1));
	}

	/**
	 * Creates a panel holding the rule's description.
	 * 
	 * @return A panel holding an html description of the rule.
	 */
	private JPanel createRuleDescriptionPanel()
	{
		// get the url to the description text
		URL url = CARuleDescriptionLoader
				.getURLFromRuleClassName(CurrentProperties.getInstance()
						.getRuleClassName());

		if(url != null)
		{
			try
			{
				editorPane = new JEditorPane(url);
			}
			catch(Exception error)
			{
				url = null;
			}
		}

		if(url == null)
		{
			// oops. Use the tooltip instead.

			// get the description text from the tooltip
			String description = getRuleDescriptionFromToolTip();
			// put it in an editor pane
			editorPane = new JEditorPane("text/html", description);
		}

		// add a hyperlink listener (it's a private inner class)
		editorPane.addHyperlinkListener(new CAHyperlinkListener(editorPane));

		// put editor pane in a scroll bar
		editorPane.setEditable(false);
		editorScrollPane = new JScrollPane(editorPane);
		editorScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		// and set to the beginning of the text so the scrollbar is
		// positioned properly
		editorPane.setCaretPosition(0);

		// set the size -- the editor pane has to be slightly larger than the
		// scroll pane or the scroll bars won't show up.
		Dimension scrollPaneDimension = new Dimension(editorPane
				.getPreferredScrollableViewportSize().width,
				HEIGHT_RULE_DESCRIPTION_PANEL);
		editorScrollPane.setPreferredSize(scrollPaneDimension);
		editorScrollPane.setMinimumSize(scrollPaneDimension);
		editorScrollPane.setMaximumSize(scrollPaneDimension);

		// create button to show enlarged editorpane
		enlargeButton = new JButton(ENLARGE_DESCRIPTION_HTML);
		enlargeButton.setFont(fonts.getBoldSmallerFont());
		enlargeButton.setActionCommand(ENLARGE_DESCRIPTION_ACTION_COMMAND);
		enlargeButton.addActionListener(listener);

		// create the panel that holds the editor pane
		JPanel descriptionPanel = new JPanel(new GridBagLayout());
		Border outerEmptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
		Border innerEmptyBorder = BorderFactory.createEmptyBorder(2, 6, 3, 2);
		Border titledBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), DESCRIPTION_PANEL_TITLE,
				TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, titleFont,
				titleColor);
		descriptionPanel.setBorder(BorderFactory.createCompoundBorder(
				outerEmptyBorder, (BorderFactory.createCompoundBorder(
						titledBorder, innerEmptyBorder))));
		descriptionPanel.setBorder(titledBorder);

		// add the scroll pane to the JPanel
		int row = 0;
		descriptionPanel.add(editorScrollPane, new GBC(1, row).setSpan(3, 1)
				.setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.CENTER)
				.setInsets(1));
		row++;
		descriptionPanel.add(enlargeButton, new GBC(2, row).setSpan(1, 1)
				.setFill(GBC.NONE).setWeight(0.0, 0.0).setAnchor(GBC.CENTER)
				.setInsets(1));

		return descriptionPanel;
	}

	/**
	 * Create and arrange the raised inner panel that holds the rule tree and
	 * description.
	 * 
	 * @param propertiesPanel
	 *            The panel holding the rule tree and description.
	 */
	private JPanel createInnerPanel()
	{
		// the panel on which we add the controls
		JPanel innerPanel = new JPanel();
		innerPanel.setBorder(BorderFactory.createRaisedBevelBorder());

		innerPanel.setLayout(new GridBagLayout());

		descriptionPanel = createRuleDescriptionPanel();

		// now that we created the description panel, we can do this.
		// Dimension treeScrollPanelDimension = new Dimension(editorScrollPane
		// .getPreferredSize().width, HEIGHT_TREE_SCROLL_PANEL);
		// ruleTreeScrollPanel.setPreferredSize(treeScrollPanelDimension);
		// ruleTreeScrollPanel.setMinimumSize(treeScrollPanelDimension);
		// ruleTreeScrollPanel.setMaximumSize(treeScrollPanelDimension);

		int row = 0;
		innerPanel.add(descriptionPanel, new GBC(1, row).setSpan(8, 1).setFill(
				GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

		return innerPanel;
	}

	/**
	 * Gets the rule description from the rule.
	 * 
	 * @return The html description.
	 */
	private String getRuleDescriptionFromToolTip()
	{
		// get the rule
		String ruleClassName = CurrentProperties.getInstance()
				.getRuleClassName();
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(ruleClassName);

		// get the tooltip
		String text = null;

		if(rule != null)
		{
			text = rule.getToolTipDescription();
		}

		if(text == null)
		{
			text = "<html><body>No description available.</body></html>";
		}

		return text;
	}

	/**
	 * Creates an enlarged view of the rule's html description. This method is
	 * called by the listener to the enlarge button (AllPanelListener).
	 */
	public void createEnlargedHTMLView()
	{
		// don't create the frame if already have one
		if(enlargedDescriptionFrame == null)
		{
			if(editorPane.getPage() != null)
			{
				enlargedDescriptionFrame = new CABrowser(editorPane.getPage(),
						false, editorScrollPane)
				{
					// override this method
					public void actionAfterShrinking()
					{
						// unpause the simulation.
						outerPanel.getCAFrame().getMenuBar()
								.firePropertyChangeEvent(
										new PropertyChangeEvent(this,
												CurrentProperties.PAUSE, null,
												new Boolean(false)));
					}

					// override this method
					public void actionBeforeShrinking()
					{
						// pause the simulation.
						outerPanel.getCAFrame().getMenuBar()
								.firePropertyChangeEvent(
										new PropertyChangeEvent(this,
												CurrentProperties.PAUSE, null,
												new Boolean(true)));
					}
				};
			}
			else
			{
				enlargedDescriptionFrame = new CABrowser(editorPane.getText(),
						false, editorScrollPane)
				{
					// override this method
					public void actionAfterShrinking()
					{
						// unpause the simulation.
						outerPanel.getCAFrame().getMenuBar()
								.firePropertyChangeEvent(
										new PropertyChangeEvent(this,
												CurrentProperties.PAUSE, null,
												new Boolean(false)));
					}

					// override this method
					public void actionBeforeShrinking()
					{
						// pause the simulation.
						outerPanel.getCAFrame().getMenuBar()
								.firePropertyChangeEvent(
										new PropertyChangeEvent(this,
												CurrentProperties.PAUSE, null,
												new Boolean(true)));
					}
				};
			}

			// make the animation for closing the frame take this long (in
			// milliseconds)
			enlargedDescriptionFrame.setAnimationLength(500);

			enlargedDescriptionFrame.setTitle(DESCRIPTION_PANEL_TITLE);
		}
		else
		{
			enlargedDescriptionFrame.setVisible(true);

			// give it focus if it already exists
			enlargedDescriptionFrame.requestFocus();
		}

		// set the position at the top of the page
		enlargedDescriptionFrame.setScrollBarToTop();
	}

	/**
	 * Gets the size of the inner raised panel so that it can be copied by other
	 * panels.
	 * 
	 * @return The size of the panel, or a size of 0 if the panel doesn't yet
	 *         exist.
	 */
	public Dimension getInnerPanelSize()
	{
		Dimension size = null;
		if(innerRaisedPanel != null)
		{
			size = innerRaisedPanel.getSize();
		}
		else
		{
			size = new Dimension(0, 0);
		}

		return size;
	}

	/**
	 * Resets the browser by resetting the URL.
	 */
	public void reset()
	{
		setDescriptionBrowserToNewURL();
	}

	/**
	 * Changes the URL displayed by the browser.
	 */
	public void setDescriptionBrowserToNewURL()
	{
		URL url = CARuleDescriptionLoader
				.getURLFromRuleClassName(CurrentProperties.getInstance()
						.getRuleClassName());

		if(url != null)
		{
			try
			{
				editorPane.setPage(url);
			}
			catch(Exception error)
			{
				url = null;
			}

			if(enlargedDescriptionFrame != null)
			{
				enlargedDescriptionFrame.setURL(url);
			}
		}

		// this could happen if there is no url for the rule or if the above
		// threw an exception.
		if(url == null)
		{
			// this forces a new page to load rather than just
			// replacing the text of the old one
			editorPane.setDocument(editorPane.getEditorKit()
					.createDefaultDocument());

			editorPane.setText(getRuleDescriptionFromToolTip());

			if(enlargedDescriptionFrame != null)
			{
				enlargedDescriptionFrame
						.setText(getRuleDescriptionFromToolTip());
			}
		}

		// and set to the beginning of the text so the scrollbar is
		// positioned properly
		editorPane.setCaretPosition(0);
		if(enlargedDescriptionFrame != null)
		{
			enlargedDescriptionFrame.setScrollBarToTop();
		}
	}
}
