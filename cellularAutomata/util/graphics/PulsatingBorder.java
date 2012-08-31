/*
 * This class has been modified from a class written by Chet Haase and Romain Guy.  
 * See their book, Filthy Rich Clients, 2007.
 */

package cellularAutomata.util.graphics;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.border.Border;

/**
 * Creates a border that pulsates.
 * 
 * @author David Bahr
 */
public class PulsatingBorder implements Border
{
    private float thickness = 0.0f;

    // The component that will pulsate.
    private JComponent c;

    /**
     * Adds a pulsating border to the specified component.
     * 
     * @param c
     *            The component that will pulsate.
     */
    public PulsatingBorder(JComponent c)
    {
        this.c = c;
    }

    /**
     * Draws the pulsating border.
     * 
     * @param c
     * @param g
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width,
        int height)
    {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        Rectangle2D r = new Rectangle2D.Double(x, y, width - 1, height - 1);
        g2.setStroke(new BasicStroke(2.0f * getThickness()));

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
            getThickness()));
        // g2.setComposite(AlphaComposite.SrcOver.derive(getThickness()));

        g2.setColor(new Color(0x54A4DE));
        g2.draw(r);
    }

    public Insets getBorderInsets(Component c)
    {
        return new Insets(2, 2, 2, 2);
    }

    public boolean isBorderOpaque()
    {
        return false;
    }

    public float getThickness()
    {
        return thickness;
    }

    public void setThickness(float thickness)
    {
        this.thickness = thickness;
        c.repaint();
    }
}