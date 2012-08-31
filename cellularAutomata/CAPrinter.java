/*
 CAPrinter -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

import cellularAutomata.CAController;
import cellularAutomata.lattice.view.LatticeView;

/**
 * Contains a method to print the cellular automaton.
 * 
 * @author David Bahr
 */
public class CAPrinter implements Printable
{
    // the CA graphics panel to be printed
    LatticeView graphicsPanel = null;

    // The scrollPane that has the view that will be printed.
    JScrollPane scrollPane = null;

    /**
     * A CAPrinter is created only by the static print() method.
     * 
     * @param graphicsPanel
     *            The CA lattice panel that will be printed.
     * @param scrollPane
     *            The scrollPane that shows a portion of the CA lattice panel
     *            (the visible portion will be printed).
     */
    private CAPrinter(LatticeView graphicsPanel, JScrollPane scrollPane)
    {
        this.graphicsPanel = graphicsPanel;
        this.scrollPane = scrollPane;
    }

    /**
     * Print the CA as it appears in the scroll pane.
     * 
     * @param graphicsPanel
     *            The CA lattice panel that will be printed.
     * @param scrollPane
     *            The scrollPane that shows a portion of the CA lattice panel
     *            (the visible portion will be printed).
     */
    public static void print(LatticeView graphicsPanel, JScrollPane scrollPane)
    {
        new CAPrinter(graphicsPanel, scrollPane).print();
    }

    /**
     * Print the image drawn on the CAPanel.
     */
    public void print()
    {
        // make the JFrame look disabled
        if(CAController.getCAFrame() != null)
        {
            CAController.getCAFrame().setViewDisabled(true);
        }

        // Get a PrinterJob
        PrinterJob printJob = PrinterJob.getPrinterJob();

        // Ask user for page format (e.g., portrait/landscape)
        // PageFormat pageFormat = printJob.pageDialog(printJob.defaultPage());
        // printJob.setPrintable(this, pageFormat);
        // printJob.setPrintable(this);

        // Thanks to Alby Graham for pointing out that a Book will turn off the
        // "select pages to print option", which is what we want.
        //
        // Give the printJob a CAPanel (graphicsPanel) which contains the
        // print() method.
        Book caPrinterBook = new Book();
        caPrinterBook.append(this, new PageFormat());
        printJob.setPageable(caPrinterBook);

        if(printJob.printDialog())
        {
            try
            {
                // this handles a bug that prevents proper scaling when using
                // the FAST setting for printing. This forces it to be the HIGH
                // setting no matter what the user chooses.
                PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
                attributes.add(PrintQuality.HIGH);
                printJob.print(attributes);

                // printJob.print();
            }
            catch(Exception e)
            {
                String message = "There has been an error printing: "
                    + e.getMessage();
                JOptionPane.showMessageDialog(CAController.getCAFrame()
                    .getFrame(), message, "Print error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }

        // make the JFrame look enabled
        if(CAController.getCAFrame() != null)
        {
            CAController.getCAFrame().setViewDisabled(false);
        }
    }

    /**
     * Method that prints the CA graphics.
     */
    public int print(Graphics g, PageFormat pf, int pageIndex)
    {
        int response = Printable.NO_SUCH_PAGE;

        Graphics2D g2 = (Graphics2D) g;

        // get the viewport's dimensions and location
        Point upperLeftCorner = scrollPane.getViewport().getViewPosition();
        Dimension viewDimensions = scrollPane.getViewport().getExtentSize();
        double graphicsWidth = viewDimensions.getWidth();
        double graphicsHeight = viewDimensions.getHeight();

        // if the image (lattice panel) is smaller than the viewport dimensions,
        // then we'll want the image (lattice panel) dimensions
        // double imageWidth = graphicsPanel.getDisplayWidth();
        // double imageHeight = graphicsPanel.getDisplayHeight();
        // if(imageWidth < graphicsWidth)
        // {
        // graphicsWidth = imageWidth;
        // }
        // if(imageHeight < graphicsHeight)
        // {
        // graphicsHeight = imageHeight;
        // }

        // printer page dimensions
        double pageHeight = pf.getImageableHeight();
        double pageWidth = pf.getImageableWidth();

        // make the printer page and the image the same size
        double scale = pageWidth / graphicsWidth;

        // double scale = 1.0;
        // double scaleWidth = pageWidth / graphicsWidth;
        // double scaleHeight = pageHeight / graphicsHeight;
        //
        // // make the scaling factor the smaller of the two (i.e., reduce by
        // which
        // // ever one reduces the most).
        // if(scaleWidth < scaleHeight)
        // {
        // scale = scaleWidth;
        // }
        // else
        // {
        // scale = scaleHeight;
        // }

        // irrelevant, but here in case I want to generalize later
        int totalNumPages = (int) Math
            .ceil(scale * graphicsHeight / pageHeight);

        // make sure not to print empty pages (irrelevant, but here in case I
        // want to generalize later)
        if(pageIndex < totalNumPages)
        {
            // shift graphic to line up with beginning of print-imageable region
            g2.translate(pf.getImageableX(), pf.getImageableY());

            // shift graphic to line up with beginning of next page to print
            g2.translate(0f, -pageIndex * pageHeight);

            // scale the page so the width fits
            //
            // BUG: For some reason this scaling has a bug if the user selects
            // the "fast" printing option. It scales 2 times too large. Other
            // printing options scale just fine.
            g2.scale(scale, scale);

            // draw the graphics panel, but draws it onto the printable page
            // rather than the screen
            graphicsPanel.draw(g2, (int) upperLeftCorner.getX(),
                (int) upperLeftCorner.getY(), (int) graphicsWidth,
                (int) graphicsHeight);

            response = Printable.PAGE_EXISTS;
        }

        return response;
    }
}
