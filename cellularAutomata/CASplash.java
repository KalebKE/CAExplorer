/*
 CASplash -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata;

import java.awt.AlphaComposite;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.image.BufferedImage;

import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

/**
 * Displays a splash page for the application. Note that this is really just a
 * not-so-clever disguise to keep the user happy while the application starts
 * up. Therefore, the length of the splash animation should not be significantly
 * altered.
 * 
 * @author David Bahr
 */
public class CASplash extends Frame
{
    // true when the splash screen animation is finished displaying.
    // the variable "finished" is volatile so that it is visible on all threads
    private volatile static boolean finished = false;

    // the initial transparency of the image at start up
    private final static float INITIAL_TRANSPARENCY = 0.3f;

    // The final transparency of the image when done with the splash animation.
    // (This MUST be less than the INITIAL_TRANSPARENCY or the splash screen
    // will abort prematurely. Among other things, this value is used to check
    // when the animation is complete.)
    private final static float FINAL_TRANSPARENCY = 0.08f;

    // The splash screen's display time in seconds
    private final static int DISPLAY_TIME = 1;

    // the splash screen's fade-in time in milliseconds
    private final static int FADE_IN_ANIMATION_TIME = 500;

    // the splash screen's fade-out time in milliseconds
    private final static int FADE_OUT_ANIMATION_TIME = 5000;

    // The path to the splash image
    private static String splashImagePath = CAConstants.SPLASH_IMAGE_NAME;

    // the class that animates the fading
    private Animator animator = null;

    // the panel holding the image
    private ImagePanel panel = null;

    // the dimensions of the image (values will be reset below)
    private int imageHeight = 0;

    private int imageWidth = 0;

    // the window that displays the image.
    private Window window = null;

    /**
     * Create a splash window.
     */
    public CASplash()
    {
        createSplashWindow();
    }

    /**
     * Will be true only after the splash animation has completed. Other classes
     * like CAFrame can check this to see when it is ok to display other
     * elements of the GUI.
     * 
     * @return true if the splash animation is done.
     */
    public static boolean isFinished()
    {
        return finished;
    }

    /**
     * Creates the actual splash window.
     */
    private void createSplashWindow()
    {
        window = new Window(this);

        // create the panel with the image (also sets image width and height)
        panel = new ImagePanel();

        // make sure there is something to display
        if((imageWidth <= 0) || (imageHeight <= 0))
        {
            // this will only happen if there was no image
            imageWidth = panel.getWidth();
            imageHeight = panel.getHeight();
        }

        // make sure there is a panel to display
        if((imageWidth > 0) && (imageHeight > 0))
        {
            // display the panel at center of the window
            window.add(panel, "Center");

            // set the window size
            window.setSize(imageWidth, imageHeight);

            // display window at center of monitor screen (by setting relative
            // to null)
            window.setLocationRelativeTo(null);
            window.setVisible(true);

            // fade in the window
            fadeIn();

            // must be called after setVisible() to guarantee that it shows up
            // front
            window.toFront();

            // display for this long, then fade out and close the window
            try
            {
                Thread.sleep(DISPLAY_TIME * 1000);
            }
            catch(Exception e)
            {
            }

            fadeOut();
        }
    }

    /**
     * Makes the frame fade away.
     */
    public void fadeIn()
    {
        // stop any previous fading
        if(animator != null)
        {
            animator.stop();
        }

        // This keeps changing the transparency of the background.
        PropertySetter setter = new PropertySetter(panel, "transparency",
            INITIAL_TRANSPARENCY, 1.0f);

        animator = new Animator(FADE_IN_ANIMATION_TIME, setter);

        // speed up then slow down the fading
        animator.setAcceleration(0.6f);
        animator.setDeceleration(0.2f);

        // start the animation
        animator.start();
    }

    /**
     * Makes the frame fade away.
     */
    public void fadeOut()
    {
        // stop any previous fading
        if(animator != null)
        {
            animator.stop();
        }

        // This keeps changing the transparency of the background.
        PropertySetter setter = new PropertySetter(panel, "transparency", 1.0f,
            FINAL_TRANSPARENCY);

        animator = new Animator(FADE_OUT_ANIMATION_TIME, setter);

        // speed up then slow down the fading
        animator.setAcceleration(0.5f);
        animator.setDeceleration(0.0f);

        // start the animation
        animator.start();
    }

    /**
     * A class that loads an image onto a panel.
     * 
     * @author David Bahr
     */
    public class ImagePanel extends JPanel
    {
        // the degree of transparency of the image.
        private float transparency = INITIAL_TRANSPARENCY;

        // the image to be displayed
        BufferedImage splashImage = null;

        public ImagePanel()
        {
            try
            {
                // Searches the classpath to find the image file. (Note that I
                // cannot use URLResource because the RuleHash has not yet been
                // created and URLResource needs that RuleHash.)
                URL url = CASplash.class.getResource("/" + splashImagePath);

                splashImage = ImageIO.read(url);

                // get dimensions of the splash image
                imageHeight = splashImage.getHeight(this);
                imageWidth = splashImage.getWidth(this);
            }
            catch(Exception e)
            {
            }
        }

        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;

            if(splashImage != null)
            {
                // draw the image
                g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, getTransparency()));
                // g2.setComposite(AlphaComposite.SrcOver.derive(getTransparency()));

                g2.drawImage(splashImage, 0, 0, null);
            }

            // close the splash window when done fading (I add 0.00000009
            // because a float only has 7 decimal places of accuracy. Without
            // this can get stuck in a loop where the window never closes.)
            if(getTransparency() <= FINAL_TRANSPARENCY + 0.00000009)
            {
                // set a variable that everyone else can check to see that
                // we are done with the splash
                finished = true;

                // close the window
                window.dispose();
            }

            // draw anything I want on the image
            // g2.setColor(Color.RED);
            // g2.drawString("Cellular Automaton Explorer", 100, 100);
            // g2.draw(new Line2D.Double(0, 0, 100, 100));
        }

        /**
         * Get the transparency.
         * 
         * @return The degree of transparency (0.0f to 1.0f).
         */
        public float getTransparency()
        {
            return transparency;
        }

        /**
         * Set the degree of transparency of the background and foreground
         * color.
         * 
         * @param transparency
         *            The alpha value between 0.0 and 1.0.
         */
        public void setTransparency(float transparency)
        {
            this.transparency = transparency;

            repaint();
        }
    }
}
