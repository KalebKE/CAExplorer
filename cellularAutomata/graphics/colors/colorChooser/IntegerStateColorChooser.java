/*
 IntegerStateColorChooser -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.graphics.colors.colorChooser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import cellularAutomata.graphics.colors.colorChooser.IntegerColorChooserPreviewPanel;
import cellularAutomata.graphics.colors.colorChooser.IntegerColorSelectionModel;
import cellularAutomata.graphics.colors.colorChooser.IntegerStateColorChooserPanel;
import cellularAutomata.util.GBC;

/**
 * Creates a color chooser that works when there are a finite number of integer
 * states.
 * 
 * @author David Bahr
 */
public class IntegerStateColorChooser extends JDialog
{
    private int numberOfStates = 2;

    // the model that holds the currently selected state and color
    private IntegerColorSelectionModel model = null;

    // the panel that let's the user choose the color and state
    private IntegerStateColorChooserPanel chooserPanel = null;

    // the preview panel that shows the user's selection
    private IntegerColorChooserPreviewPanel previewPanel = null;

    // the ok button
    private JButton okButton = new JButton("Ok");

    // the cancel button
    private JButton cancelButton = new JButton("Cancel");

    /**
     * Creates a color chooser for integer valued cells.
     * 
     * @param parent
     *            The component over which this colorChooser will be centered.
     * @param numberOfStates
     *            The number of states permitted by the cell.
     * @param selectedState
     *            The currently selected state.
     * @param selectedColor
     *            The currently selected color.
     * @param okListener
     *            Specifies what to do when the ok button is selected.
     */
    public IntegerStateColorChooser(Frame parent, int numberOfStates,
        int selectedState, Color selectedColor, ActionListener okListener)
    {
        super(parent);
        
        this.numberOfStates = numberOfStates;

        // center the color chooser
        this.setLocationRelativeTo(parent);

        model = new IntegerColorSelectionModel(selectedColor, selectedState);
        model.setSelectedColorAndState(selectedColor, selectedState);

        chooserPanel = new IntegerStateColorChooserPanel(numberOfStates, model);

        previewPanel = new IntegerColorChooserPreviewPanel(this);

        okButton.addActionListener(okListener);
        cancelButton.addActionListener(new CancelListener(this));

        // this second listener on the ok button will close the dialog box
        okButton.addActionListener(new CancelListener(this));

        createFrame();
    }

    /**
     * Creates the color chooser frame.
     */
    private void createFrame()
    {
        int width = 400;
        int height = 400;

        // create a scrollPane for the chooser
        // JScrollPane scrollPane = new JScrollPane(chooserPanel);
        // scrollPane.setBorder(BorderFactory.createRaisedBevelBorder());

        this.setTitle("Select a state to draw");

        this.setLayout(new BorderLayout());
        this.add(chooserPanel, BorderLayout.NORTH);
        // this.add(scrollPane, BorderLayout.NORTH);
        this.add(previewPanel, BorderLayout.CENTER);
        this.add(new ButtonPanel(), BorderLayout.SOUTH);

        // this.setLayout(new GridBagLayout());
        //        
        // int row = 0;
        // this.add(scrollPane, new GBC(0, row).setSpan(10, 1).setFill(GBC.BOTH)
        // .setWeight(10.0, 10.0).setAnchor(GBC.WEST).setInsets(1));
        //
        // row = 1;
        // this.add(previewPanel, new GBC(1, row).setSpan(10, 1).setFill(
        // GBC.BOTH).setWeight(5.0, 5.0).setAnchor(GBC.WEST).setInsets(1));
        //
        // row = 2;
        // this.add(new ButtonPanel(), new GBC(1, row).setSpan(10, 1).setFill(
        // GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

        // this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        // this.addWindowListener(new ColorChooserWindowListener(this));

        if(numberOfStates < 600)
        {
            this.setSize(width, height);
        }
        else
        {
            this.setSize(2 * width, height + 2 * height / 3);
        }

        this.setAlwaysOnTop(true);

        this.setModal(true);
    }

    /**
     * Gets the currently selected color.
     * 
     * @return the currently selected color.
     */
    public Color getColor()
    {
        return model.getSelectedColor();
    }

    /**
     * Gets the currently selected state.
     * 
     * @return the currently selected state.
     */
    public int getState()
    {
        return model.getSelectedState();
    }

    /**
     * Gets the selection model that holds the color and the state.
     * 
     * @return the model that holds the color and the state.
     */
    public IntegerColorSelectionModel getSelectionModel()
    {
        return model;
    }

    /**
     * Sets the currently selected color and state.
     * 
     * @param selectedState
     *            The currently selected state.
     * @param selectedColor
     *            The currently selected color.
     */
    public void setStateAndColor(int selectedState, Color selectedColor)
    {
        model.setSelectedColorAndState(selectedColor, selectedState);
    }

    /**
     * Creates a panel that holds an ok and cancel button.
     * 
     * @author David Bahr
     */
    private class ButtonPanel extends JPanel
    {
        /**
         * Creates a panel that holds an ok and cancel button.
         */
        public ButtonPanel()
        {
            this.add(okButton);
            this.add(cancelButton);
        }
    }

    /**
     * Listens for the Cancel button on the color chooser.
     */
    private class CancelListener implements ActionListener
    {
        private IntegerStateColorChooser chooser = null;

        /**
         * Create the listener.
         */
        public CancelListener(IntegerStateColorChooser chooser)
        {
            this.chooser = chooser;
        }

        public void actionPerformed(ActionEvent e)
        {
            chooser.dispose();
        }
    }

    /**
     * Behavior when the color chooser window is closed.
     * 
     * @author David Bahr
     */
    public class ColorChooserWindowListener extends WindowAdapter
    {
        private IntegerStateColorChooser chooser = null;

        /**
         * Create the listener.
         */
        public ColorChooserWindowListener(IntegerStateColorChooser chooser)
        {
            this.chooser = chooser;
        }

        /**
         * Closes the window.
         */
        public void windowClosing(WindowEvent e)
        {
            cancelButton.setSelected(true);
        }
    }

}
