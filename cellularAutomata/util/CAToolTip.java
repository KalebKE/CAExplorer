package cellularAutomata.util;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JToolTip;

/**
 * A tool tip with the ability to change color.
 * 
 * @author David Bahr
 */
public class CAToolTip extends JToolTip
{
    // a color for warning tool tips
    private static final Color WARNING_COLOR = new Color(255, 100, 100);

    // the default tool tip color created by the UI
    private Color defaultColor = null;

    /**
     * Creates a tooltip with a fixed width.
     */
    public CAToolTip(JComponent component)
    {
        super();
        this.setComponent(component);
        defaultColor = this.getBackground();
    }

    /**
     * Sets the color of the tooltip.
     * 
     * @param color
     *            The color of the tooltip.
     */
    public void setColor(Color color)
    {
        this.setBackground(color);
    }

    /**
     * Sets the color of the tooltip to the original default color.
     */
    public void setToDefaultColor()
    {
        this.setBackground(defaultColor);
    }

    /**
     * Sets the color of the tooltip to a warning color (like red).
     */
    public void setToWarningColor()
    {
        this.setBackground(WARNING_COLOR);
    }

    /**
     * Override the getPreferredSize() to specify a fixed width.
     */
    // public Dimension getPreferredSize()
    // {
    // Dimension fixedSize = new Dimension(400,
    // super.getPreferredSize().height);
    //
    // return fixedSize;
    // }
}
