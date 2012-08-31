/*
 CABrowser -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.util.browser;

// import javax.swing.event.*;
// import javax.swing.text.*;
// import javax.swing.text.html.*;
// import java.awt.*;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import cellularAutomata.CAConstants;
import cellularAutomata.reflection.URLResource;
import cellularAutomata.util.CAHyperlinkListener;
import cellularAutomata.util.GBC;
import cellularAutomata.util.files.CAImageIconLoader;
import cellularAutomata.util.graphics.ShrinkingJFrame;

/**
 * Creates a very simple html browser.
 * 
 * @author David Bahr
 */
public class CABrowser extends ShrinkingJFrame implements ActionListener
{
	// action command for the back button
	private static final String BACK = "Back";

	// action command for the forward button
	private static final String FORWARD = "Forward";

	// title for the browser
	private static final String FRAME_TITLE = CAConstants.PROGRAM_TITLE + " "
			+ CAConstants.VERSION + " Browser";

	// text used when the url is not available, can't be loaded, etc.
	private static final String INVALID_URL = "Invalid or inaccessible URL.";

	// text displayed at start up
	private static final String LOADING_MESSAGE = "Loading...";

	// when true the browser is closed (disposed) when the user exits the
	// browser.
	private boolean close = true;

	// the current location in the url list
	private int index = -1;

	// the parent component that spawned this browser. May be null.
	private Component parent = null;

	// the back button
	private JButton backButton = null;

	// the forward button
	private JButton forwardButton = null;

	// the editor pane used to display html
	private JEditorPane editorPane = null;

	// the scroll pane used to hold the editor pane
	private JScrollPane editorScrollPane = null;

	// the tool bar with back and forward buttons
	private JToolBar toolBar = null;

	// a list of previously visited urls
	private LinkedList<String> urlList = new LinkedList<String>();

	/**
	 * Create a frame that acts as a simple html browser.
	 * 
	 * @param url
	 *            The url that will be displayed in the browser.
	 * @param close
	 *            When true, the browser exits (is disposed) when the user
	 *            closes the browser.
	 * @param parent
	 *            The component that spawned this browser. May be null. Is used
	 *            to determine where the browser should shrink as it closes.
	 */
	public CABrowser(URL url, boolean close, Component parent)
	{
		super();

		this.close = close;
		this.parent = parent;

		// setup the browser
		setup();

		// now go to the specified page
		try
		{
			// use the supplied url
			this.setURL(url);
		}
		catch(Exception e)
		{
			this.setText(INVALID_URL);
		}
	}

	/**
	 * Create a frame that acts as a simple html browser.
	 * 
	 * @param text
	 *            The html (or plain) text that will be displayed in the
	 *            browser.
	 * @param close
	 *            When true, the browser exits (is disposed) when the user
	 *            closes the browser.
	 * @param parent
	 *            The component that spawned this browser. May be null. Is used
	 *            to determine where the browser should shrink as it closes.
	 */
	public CABrowser(String text, boolean close, Component parent)
	{
		super();

		this.close = close;
		this.parent = parent;

		// setup the browser
		setup();

		// now go to the specified page
		try
		{
			// use the supplied text
			this.setText(text);
		}
		catch(Exception e)
		{
			this.setText(INVALID_URL);
		}
	}

	/**
	 * React to the back and forward buttons.
	 */
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals(BACK))
		{
			if(!urlList.isEmpty() && index > 0)
			{
				String url = urlList.get(index - 1);

				// see if it is indeed a url (might just be text)
				try
				{
					// this is just a test to see if it is a URL. Will throw
					// exception if not.
					new URL(url);

					// it is a URL, so load that url
					try
					{
						editorPane.setPage(url);
					}
					catch(Exception error)
					{
						// this forces a new page to load rather than just
						// replacing the text of the old one
						editorPane.setDocument(editorPane.getEditorKit()
								.createDefaultDocument());

						editorPane.setText(INVALID_URL);
					}
				}
				catch(Exception notURL)
				{
					// this forces a new page to load rather than just replacing
					// the text of the old one
					editorPane.setDocument(editorPane.getEditorKit()
							.createDefaultDocument());

					// not a url, so load the text
					editorPane.setText(url);
				}

				// now decrement the index.
				index--;

				// enable/disable back and forward buttons
				enableButtons();

				setScrollBarToTop();
			}
		}
		else if(e.getActionCommand().equals(FORWARD))
		{
			if(!urlList.isEmpty() && index >= 0 && index < urlList.size() - 1)
			{
				String url = urlList.get(index + 1);

				// see if it is indeed a url (might just be text)
				try
				{
					// this is just a test to see if it is a URL. Will throw
					// exception if not.
					new URL(url);

					// it is a URL, so load that url
					setURL(url);
				}
				catch(Exception notURL)
				{
					// not a url, so load the text
					setText(url);
				}

				setScrollBarToTop();
			}
		}
	}

	/**
	 * Creates a toolbar with the forward and back buttons.
	 */
	private void createToolBar()
	{
		// create buttons for the tool bar
		URL forwardURL = URLResource.getResource("/images/Forward.gif");
		URL backURL = URLResource.getResource("/images/Back.gif");

		forwardButton = new JButton(new ImageIcon(forwardURL));
		backButton = new JButton(new ImageIcon(backURL));

		forwardButton.setToolTipText(FORWARD);
		backButton.setToolTipText(BACK);

		forwardButton.setActionCommand(FORWARD);
		backButton.setActionCommand(BACK);

		forwardButton.addActionListener(this);
		backButton.addActionListener(this);

		// create the tool bar
		toolBar = new JToolBar();

		// create the tool bar look and feel
		toolBar.setBorderPainted(true);
		toolBar.setFloatable(false);
		toolBar.setRollover(true);

		// add the buttons to the tool bar
		toolBar.add(backButton);
		toolBar.add(forwardButton);
	}

	/**
	 * Decides whether or not the back and forward buttons should be enabled or
	 * disabled.
	 */
	private void enableButtons()
	{
		if(urlList.size() <= 1 || index >= urlList.size())
		{
			// at the beginning of the list (and it only has 0 or 1 elements) or
			// the index went out of bounds
			backButton.setEnabled(false);
			forwardButton.setEnabled(false);
		}
		else if(index == 0 && urlList.size() > 1)
		{
			// at the beginning of the list
			backButton.setEnabled(false);
			forwardButton.setEnabled(true);
		}
		else if(index > 0 && index <= urlList.size() - 2)
		{
			// in the middle of the list
			backButton.setEnabled(true);
			forwardButton.setEnabled(true);
		}
		else if(index > 0 && index == urlList.size() - 1)
		{
			// at the end of the list
			backButton.setEnabled(true);
			forwardButton.setEnabled(false);
		}
	}

	// FINISH LATER
	// private String getTitleFromHTML()
	// {
	// String title = null;
	//        
	// HTMLDocument doc = (HTMLDocument)(editorPane.getDocument());
	// HTMLDocument.Iterator tags = doc.getIterator(HTML.Tag.TITLE);
	// if(tags.getStartOffset() != -1)
	// {
	//            
	// }
	// }

	/**
	 * Set up the browser.
	 */
	private void setup()
	{
		// add a listener that will make the request for this window to shrink
		// when closing
		this.addWindowListener(new BrowserListener(this));

		// create an editor pane
		editorPane = new JEditorPane("text/html", LOADING_MESSAGE);

		// BELOW DOESN'T WORK... WHY?
		// editorPane.setEditorKit(new HTMLEditorKit()
		// {
		// public Document createDefaultDocument()
		// {
		// AbstractDocument doc = (AbstractDocument) super
		// .createDefaultDocument();
		// doc.setAsynchronousLoadPriority(0);
		// return doc;
		// }
		// });

		// AsyncHTMLEditorKit HAS BUGS, SO NOT CURRENTLY USED
		// make the page load asynchronously
		// JEditorPane.registerEditorKitForContentType("text/html",
		// "CABrowser$AsyncHTMLEditorKit");
		// editorPane.setEditorKitForContentType("text/html",
		// new AsyncHTMLEditorKit());

		// add a hyperlink listener
		editorPane.addHyperlinkListener(new CAHyperlinkListener(this));

		// don't let the user edit the text
		editorPane.setEditable(false);

		// put editor pane in a scroll bar
		editorScrollPane = new JScrollPane(editorPane);
		editorScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		// and set to the beginning of the text so the scrollbar is
		// positioned properly
		editorPane.setCaretPosition(0);

		// set the size
		editorPane.setPreferredSize(new Dimension(350, 250));
		Dimension scrollPaneDimension = new Dimension(editorPane
				.getPreferredScrollableViewportSize().width, 200);
		editorScrollPane.setPreferredSize(scrollPaneDimension);

		// create the frame that will hold the html
		this.setTitle(FRAME_TITLE);

		// set an image for the frame
		ImageIcon icon = CAImageIconLoader
				.loadImage(CAConstants.APPLICATION_ALTERNATIVE_ICON_IMAGE_PATH);
		this.setIconImage(icon.getImage());

		// set the size of the frame
		int height = (int) (Toolkit.getDefaultToolkit().getScreenSize().height / 1.25);
		int width = (int) (Toolkit.getDefaultToolkit().getScreenSize().width / 2.0);
		this.setSize(width, height);

		// create a tool bar
		createToolBar();

		// enable/disable back and forward buttons
		enableButtons();

		// add the tool bar and editor scroll pane to the frame
		this.getContentPane().setLayout(new GridBagLayout());
		this.getContentPane().add(
				toolBar,
				new GBC(0, 0).setSpan(1, 1).setFill(GBC.HORIZONTAL).setWeight(
						0.0, 0.0).setAnchor(GBC.NORTHWEST));
		this.getContentPane().add(
				editorScrollPane,
				new GBC(0, 1).setSpan(1, 1).setFill(GBC.BOTH).setWeight(1.0,
						1.0).setAnchor(GBC.CENTER));

		// and display the browser frame
		this.setVisible(true);
	}

	/**
	 * Positions the scroll bar at the top of the page.
	 */
	public void setScrollBarToTop()
	{
		editorPane.setCaretPosition(0);
	}

	/**
	 * Set the html (or plain) text displayed on the browser.
	 * 
	 * @param text
	 *            The html (or plain) text.
	 */
	public void setText(String text)
	{
		// this forces a new page to load rather than just replacing the text of
		// the old one
		editorPane.setDocument(editorPane.getEditorKit()
				.createDefaultDocument());

		editorPane.setText(text);

		// keep track of this page, but only add it if it isn't already the next
		// page
		if((urlList.size() == 0)
				|| (index == urlList.size() - 1)
				|| ((index + 1 < urlList.size()) && !urlList.get(index + 1)
						.equals(text)))
		{
			// then remove everything from that point forward
			while(urlList.size() - 1 > index)
			{
				urlList.removeLast();
			}

			// then add this
			urlList.add(text);
		}

		// update position in the url list
		index++;

		// enable/disable back and forward buttons
		enableButtons();
	}

	/**
	 * Set the url displayed on the browser.
	 * 
	 * @param url
	 *            The url that will be displayed in the browser.
	 */
	public void setURL(String url)
	{
		try
		{
			editorPane.setPage(url);
		}
		catch(Exception e)
		{
			// this forces a new page to load rather than just replacing the
			// text of the old one
			editorPane.setDocument(editorPane.getEditorKit()
					.createDefaultDocument());

			editorPane.setText(INVALID_URL);

			url = INVALID_URL;
		}

		// keep track of this page, but only add it if it isn't already the next
		// page
		if((urlList.size() == 0)
				|| (index == urlList.size() - 1)
				|| ((index + 1 < urlList.size()) && !urlList.get(index + 1)
						.equals(url)))
		{
			// then remove everything from that point forward
			while(urlList.size() - 1 > index)
			{
				urlList.removeLast();
			}

			// then add this
			urlList.add(url);
		}

		// update position in the url list
		index++;

		// enable/disable back and forward buttons
		enableButtons();
	}

	/**
	 * Set the url displayed on the browser.
	 * 
	 * @param url
	 *            The url that will be displayed in the browser.
	 */
	public void setURL(URL url)
	{
		setURL(url.toString());
	}

	/** ********************************************************************** */

	/**
	 * Behavior when the browser window is closed.
	 * 
	 * @author David Bahr
	 */
	public class BrowserListener extends WindowAdapter
	{
		CABrowser frame = null;

		/**
		 * Create the listener.
		 * 
		 * @param frame
		 *            The frame containing all he CA graphics.
		 */
		public BrowserListener(CABrowser frame)
		{
			this.frame = frame;
		}

		/**
		 * Closes the application by setting the EXIT property to true.
		 */
		public void windowClosing(WindowEvent e)
		{
			if(frame != null)
			{
			    	if(CAConstants.WINDOWS_XP_OS)
			    	{
			    	    	// only do this fancy closing for XP.  Other operating 
			    	    	// systems have nicer closing graphics and this isn't 
			    	    	// necessary.
        				if(parent == null)
        				{
        					// shrink into itself, and then close (dispose)
        					frame.shrink(close);
        				}
        				else
        				{
        					try
        					{
        						// this will fail if the parent component is not visible
        						Point parentLocation = parent.getLocationOnScreen();
        						Point middleOfParent = new Point(
        								(int) ((parentLocation.x + parent.getWidth() / 2.0)),
        								(int) ((parentLocation.y + parent.getHeight() / 2.0)));
        
        						// shrink into its parent, and then close (dispose)
        						frame.shrink(middleOfParent, close);
        					}
        					catch(Exception failed)
        					{
        						// shrink into itself, and then close (dispose)
        						frame.shrink(close);
        					}
        				}
			    	}
			    	else
			    	{
			    	    // close the frame without the "shrinking graphics" above.  The 
			    	    // shrinking graphics aren't necessary for non XP operating 
			    	    // systems because they already close graphics nicely.
			    	    frame.dispose();
			    	}
			}
		}
	}

	/*
	 * BELOW NEEDS WORK -- HAS BUGS. SO CURRENTLY IS NOT USED. WAS FOUND IN A
	 * JAVA TUTORIAL.
	 */

	// /**
	// * An HTMLEditorKit that does asychronous layout to represent the body
	// * element and table cell elements.
	// */
	// public static class AsyncHTMLEditorKit extends HTMLEditorKit
	// {
	//
	// public AsyncHTMLEditorKit()
	// {
	// super();
	// }
	//
	// /**
	// * Fetch a factory that is suitable for producing views of any models
	// * that are produced by this kit.
	// *
	// * @return the factory
	// */
	// public ViewFactory getViewFactory()
	// {
	// return asyncFactory;
	// }
	//
	// static ViewFactory asyncFactory = new AsyncFactory();
	//
	// /**
	// * Factory to build views of the html elements. This simply extends the
	// * behavior of the default html factory to build a view that does
	// * asynchronous layout for the BODY and TD elements.
	// */
	// public static class AsyncFactory extends HTMLFactory
	// {
	//
	// public View create(Element elem)
	// {
	// Object o = elem.getAttributes().getAttribute(
	// StyleConstants.NameAttribute);
	// if(o instanceof HTML.Tag)
	// {
	// HTML.Tag kind = (HTML.Tag) o;
	// if((kind == HTML.Tag.BODY) || (kind == HTML.Tag.TD))
	// {
	// // System.err.println("creating BlockView for: " +
	// // elem);
	// return new BlockView(elem, View.Y_AXIS);
	// }
	// }
	// return super.create(elem);
	// }
	//
	// }
	//
	// /**
	// * A view implementation to display an html block (as an asynchronous
	// * box) with CSS specifications.
	// */
	// public static class BlockView extends AsyncBoxView
	// {
	//
	// /**
	// * Creates a new view that represents an html box. This can be used
	// * for a number of elements.
	// *
	// * @param elem
	// * the element to create a view for
	// * @param axis
	// * either View.X_AXIS or View.Y_AXIS
	// */
	// public BlockView(Element elem, int axis)
	// {
	// super(elem, axis);
	// StyleSheet sheet = getStyleSheet();
	// attr = sheet.getViewAttributes(this);
	// painter = sheet.getBoxPainter(attr);
	// width = -1f;
	// height = -1f;
	// }
	//
	// /**
	// * Update any cached values that come from attributes.
	// */
	// protected void setPropertiesFromAttributes()
	// {
	// attr = getStyleSheet().getViewAttributes(this);
	// if(attr != null)
	// {
	// setTopInset(painter.getInset(TOP, this));
	// setLeftInset(painter.getInset(LEFT, this));
	// setBottomInset(painter.getInset(BOTTOM, this));
	// setRightInset(painter.getInset(RIGHT, this));
	//
	// // determine css width... default to unspecified
	// width = -1;
	// Object widthValue = attr.getAttribute(CSS.Attribute.WIDTH);
	// if(widthValue != null)
	// {
	// // this is wrong.... but CSS.LengthValue isn't public
	// // yet
	// try
	// {
	// width = Float.valueOf(widthValue.toString())
	// .floatValue();
	// }
	// catch(NumberFormatException nfe)
	// {
	// width = -1;
	// }
	// }
	//
	// // determine css height... default to unspecified
	// height = -1;
	// Object heightValue = attr
	// .getAttribute(CSS.Attribute.HEIGHT);
	// if(heightValue != null)
	// {
	// // this is wrong.... but CSS.LengthValue isn't public
	// // yet
	// try
	// {
	// width = Float.valueOf(heightValue.toString())
	// .floatValue();
	// }
	// catch(NumberFormatException nfe)
	// {
	// height = -1;
	// }
	// }
	// }
	// }
	//
	// /**
	// * Get the StyleSheet to use for this view. By default the
	// * associated document is assumed to be an HTMLDocument, and the
	// * StyleSheet implementation is fetched from it.
	// */
	// protected StyleSheet getStyleSheet()
	// {
	// HTMLDocument doc = (HTMLDocument) getDocument();
	// return doc.getStyleSheet();
	// }
	//
	// // --- View methods ------------------------------------------------
	//
	// /**
	// * Determines the minimum span for this view along an axis. If a css
	// * length has been specified (i.e. width for X_AXIS or height for
	// * Y_AXIS), that will be used, otherwise the superclass behavior is
	// * used.
	// *
	// * @param axis
	// * may be either View.X_AXIS or View.Y_AXIS
	// * @returns the minimum span the view can be rendered into.
	// * @see View#getPreferredSpan
	// */
	// public float getMinimumSpan(int axis)
	// {
	// if((axis == X_AXIS) && (width >= 0))
	// {
	// return width;
	// }
	// else if((axis == Y_AXIS) && (height >= 0))
	// {
	// return height;
	// }
	// else
	// {
	// return super.getMinimumSpan(axis);
	// }
	// }
	//
	// /**
	// * Determines the preferred span for this view along an axis. If a
	// * css length has been specified (i.e. width for X_AXIS or height
	// * for Y_AXIS), that will be used, otherwise the superclass behavior
	// * is used.
	// *
	// * @param axis
	// * may be either View.X_AXIS or View.Y_AXIS
	// * @returns the minimum span the view can be rendered into.
	// * @see View#getPreferredSpan
	// */
	// public float getPreferredSpan(int axis)
	// {
	// if((axis == X_AXIS) && (width >= 0))
	// {
	// return width;
	// }
	// else if((axis == Y_AXIS) && (height >= 0))
	// {
	// return height;
	// }
	// else
	// {
	// return super.getPreferredSpan(axis);
	// }
	// }
	//
	// /**
	// * Establishes the parent view for this view. This is guaranteed to
	// * be called before any other methods if the parent view is
	// * functioning properly.
	// * <p>
	// * This is implemented to forward to the superclass as well as call
	// * the <a
	// * href="#setPropertiesFromAttributes">setPropertiesFromAttributes</a>
	// * method to set the paragraph properties from the css attributes.
	// * The call is made at this time to ensure the ability to resolve
	// * upward through the parents view attributes.
	// *
	// * @param parent
	// * the new parent, or null if the view is being removed
	// * from a parent it was previously added to
	// */
	// public void setParent(View parent)
	// {
	// super.setParent(parent);
	// setPropertiesFromAttributes();
	// }
	//
	// /**
	// * Renders using the given rendering surface and area on that
	// * surface. This is implemented to delegate to the css box painter
	// * to paint the border and background prior to the interior.
	// *
	// * @param g
	// * the rendering surface to use
	// * @param allocation
	// * the allocated region to render into
	// * @see View#paint
	// */
	// public void paint(Graphics g, Shape allocation)
	// {
	// Rectangle a = (Rectangle) allocation;
	// painter.paint(g, a.x, a.y, a.width, a.height, this);
	// super.paint(g, a);
	// }
	//
	// /**
	// * Fetches the attributes to use when rendering. This is implemented
	// * to multiplex the attributes specified in the model with a
	// * StyleSheet.
	// */
	// public AttributeSet getAttributes()
	// {
	// return attr;
	// }
	//
	// /**
	// * Gets the alignment.
	// *
	// * @param axis
	// * may be either X_AXIS or Y_AXIS
	// * @return the alignment
	// */
	// public float getAlignment(int axis)
	// {
	// switch(axis)
	// {
	// case View.X_AXIS:
	// return 0;
	// case View.Y_AXIS:
	// float span = getPreferredSpan(View.Y_AXIS);
	// View v = getView(0);
	// float above = v.getPreferredSpan(View.Y_AXIS);
	// float a = (((int) span) != 0) ? (above * v
	// .getAlignment(View.Y_AXIS))
	// / span : 0;
	// return a;
	// default:
	// throw new IllegalArgumentException("Invalid axis: "
	// + axis);
	// }
	// }
	//
	// public void changedUpdate(DocumentEvent changes, Shape a,
	// ViewFactory f)
	// {
	// super.changedUpdate(changes, a, f);
	// int pos = changes.getOffset();
	// if(pos <= getStartOffset()
	// && (pos + changes.getLength()) >= getEndOffset())
	// {
	// setPropertiesFromAttributes();
	// }
	// }
	//
	// float width;
	//
	// float height;
	//
	// private AttributeSet attr;
	//
	// private StyleSheet.BoxPainter painter;
	// }
	//
	// }
}
