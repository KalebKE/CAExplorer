/*
 Fractal -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.rules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cellularAutomata.CAController;
import cellularAutomata.CurrentProperties;
import cellularAutomata.Cell;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.TriangleHexagonCellStateView;
import cellularAutomata.cellState.view.ComplexModulusView;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.rules.templates.ComplexRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;
import cellularAutomata.util.math.Complex;
import cellularAutomata.util.math.RandomSingleton;

/*
 * NOTE: Why are so many variables static? Because they are for the properties
 * panel which needs to be the same for all instances of the Fractal class.
 */

/**
 * A rule which assigns complex numbers to each cell and then displays them as
 * Mandelbrot and Julia sets.
 * 
 * @author David Bahr
 */
public class Fractal extends ComplexRuleTemplate
{
    // default imaginary value for the Julia Set constant
    private static final double DEFAULT_JULIA_IMAGINARY_VALUE = -0.2321;

    // default real value for the Julia Set constant
    private static final double DEFAULT_JULIA_REAL_VALUE = -0.835;

    // default imaginary value for the complex plane initial state
    private static final double DEFAULT_PLANE_IMAGINARY_VALUE = 0.0; // 0.875;

    // default real value for the complex plane initial state
    private static final double DEFAULT_PLANE_REAL_VALUE = -0.2; // -0.125;

    // the default width of the displayed complex plane
    private static final double DEFAULT_WIDTH = 3.0; // 0.75;

    // label for the close button
    private static final String CLOSE = "Close";

    // The name of the initial state configuration
    private static final String INIT_STATE_COMPLEX_PLANE = "Complex plane";

    // The name of the initial state configuration
    private static final String INIT_STATE_COMPLEX_PLANE_TOOLTIP = "<html>The complex plane "
        + "centered at "
        + DEFAULT_PLANE_REAL_VALUE
        + "+"
        + DEFAULT_PLANE_IMAGINARY_VALUE
        + "i with width "
        + DEFAULT_WIDTH
        + ".</html>";

    // The name of an initial state configuration
    private static final String INIT_STATE_COMPLEX_PLANE_SYMMETRIC = "Complex symmetric";

    // The name of an initial state configuration
    // private static final String INIT_STATE_COMPLEX_PLANE_COS = "Complex cos";

    // The tooltip for an initial state configuration
    private static final String INIT_STATE_COMPLEX_PLANE_SYMMETRIC_TOOLTIP = "<html>Values decrease "
        + "symmetrically from a central complex number.  <br>"
        + "In other words, the same values appear at the same distance on either <br>"
        + "side of of the central value. For example, 0, 1, 2, 3, 4, 3, 2, 1, 0 if <br>"
        + "4 was the central number and the width was 8. This ensures smooth <br>"
        + "wrap-around boundaries.</html>";

    // tooltip for setting the imaginary value of the Julia Set constant
    private static final String JULIA_IMAGINARY_VALUE_TIP = "Sets the imaginary value "
        + "of the constant in the Julia Set equation.";

    // tooltip for setting the real value of the Julia Set constant
    private static final String JULIA_REAL_VALUE_TIP = "Sets the real value "
        + "of the constant in the Julia Set equation.";

    // tooltip for setting the imaginary value of the central position on the
    // complex plane
    private static final String POSITION_IMAGINARY_VALUE_TIP = "Sets the imaginary value "
        + "for the position of the center of the display.";

    // tooltip for setting the real value of the central position on the complex
    // plane
    private static final String POSITION_REAL_VALUE_TIP = "Sets the real value "
        + "for the position of the center of the display.";

    // label for the reset simulation button
    private static final String RESET = "Reset";

    // tool tip for the reset simulation button
    private static final String RESET_TOOLTIP = "<html> This resets the simulation and is only <br>"
        + "necessary when resetting the position< br> or width </html>.";

    // a display name for this class
    private static final String RULE_NAME = "Fractal Average";

    // a description of property choices that give the best results for this
    // rule (e.g., which lattice, how many states, etc.)
    private static final String BEST_RESULTS = "<html> <body><b>"
        + RULE_NAME
        + ".</b>"
        + "<p> "
        + "<b>For best results</b>, try using a 200 by 200 square (8 neighbor) "
        + "lattice.  Make the lattice as large "
        + "as your computer can handle without crashing.  The simulation will be slow, but the "
        + "fractal patterns will be more obvious. "
        + "<p>"
        + "Also, use the \"Complex Symmetric\" initial "
        + "state. (The \"Complex Plane\" initial state "
        + "fills the cells with complex numbers, ordered as they would appear on a "
        + "complex plane.  The \"Complex Symmetric\" initial state does the same thing but makes "
        + "the values symmetric about a central point.  That way the values at the boundaries "
        + "wrap around without a discontinuity.)"
        + "<p>"
        + "Also on the \"More Properties for Fractal\" tab, use the \"i tan\" function with the "
        + "Julia Set view. Set the Julia constant at c = -0.835 - 0.232i.  Center at the "
        + "position -0.2 + 0.0i with width 3.0.  Finally, use a running average of 2.  Once you "
        + "have tried this configuration, try changing various parameters for other incredible "
        + "results. (For example, try using the Mandelbrot view, and try other values for c with "
        + "the Julia Set view.)"
        + "<p>"
        + "<b>So why doesn't this zoom like other fractal simulators?  And why isn't it as "
        + "smooth looking?</b> Because this is a cellular automaton!  The cell size is fixed at "
        + "the same value no matter how much you zoom.  However, if you'd like a close up of "
        + "any location, just reset the width of the displayed region on the complex plane."
        + "This can be done from the \"More Properties\" window."
        + leftClickInstructions + rightClickInstructions + "</body></html>";

    // a tooltip description for this class
    private String TOOLTIP = "<html> <body><b>"
        + RULE_NAME
        + ".</b> Generates fractals by averaging the complex numbers in neighboring cell.</body></html>";

    // a tooltip description for the width spinner
    private static final String WIDTH_TIP = "<html> Select the width of the display on "
        + "the complex plane (between 0.001 and 4.001).<br>"
        + "Smaller widths zoom the display and show more detail.</html>";

    // which function to use is determined by these constants
    private static final int NONE = 0;

    private static final int I_TAN = 1;

    private static final int SQUARE = 2;

    private static final int SIN = 3;

    private static final int COS = 4;

    private static final int TAN = 5;

    private static final int SINH = 6;

    private static final int COSH = 7;

    private static final int TANH = 8;

    // which view to use is determined by these constants
    private static final int MODULUS = 0;

    private static final int MANDELBROT = 1;

    private static final int JULIA = 2;

    // The width of the complex plane displayed as an initial state
    private static double widthOfDisplay = DEFAULT_WIDTH;

    // keeps track of the number of cells that have had initial state values
    // assigned to them
    private static int cellNum = 0;

    // the current generation being processed by the rule
    private static volatile int currentGeneration = -1;

    // The function number used by the rule. Once per generation it is reset to
    // the functionChoice selected by the user from the "more properties panel."
    private static volatile int functionNumber = NONE;

    // the number of columns on the lattice
    private static int numberOfCols = 0;

    // the number of rows on the lattice
    private static int numberOfRows = 0;

    // which function is used is given by this integer (set to one of the
    // constants NONE, SQUARE_ROOT, SQUARE, SIN, COS, etc.)
    private static volatile int functionChoice = NONE;

    // which view is used is given by this integer (set to one of the
    // constants MODULUS, MANDELBROT, etc.)
    private static volatile int viewChoice = MANDELBROT;

    // the Julia Set constant -- may be reset by the user
    private static volatile Complex juliaConstant = new Complex(
        DEFAULT_JULIA_REAL_VALUE, DEFAULT_JULIA_IMAGINARY_VALUE);

    // the complex value of the cell at the upper left of the grid when the
    // "Complex plane" option is selected
    private static Complex upperLeftCorner = new Complex(
        DEFAULT_PLANE_REAL_VALUE - widthOfDisplay / 2.0,
        DEFAULT_PLANE_IMAGINARY_VALUE - widthOfDisplay / 2.0);

    // The button for resetting the position on the complex plane
    private static JButton resetButton = null;

    // The label for editing the imaginary part of the Julia Set constant
    private static JLabel imaginaryLabel = null;

    // The label for editing the imaginary part of the position on the complex
    // plane
    private static JLabel imaginaryPositionLabel = null;

    // The label for editing the real part of the Julia Set constant
    private static JLabel realLabel = null;

    // The label for editing the real part of the position on the complex plane
    private static JLabel realPositionLabel = null;

    // The label for editing the width of the position on the complex plane
    private static JLabel widthLabel = null;

    // the JPanel that is returned by getAdditionalPropertiesPanel()
    private static JPanel panel = null;

    // radio buttons for function choices
    private static JRadioButton noneButton = null;

    private static JRadioButton iTanButton = null;

    private static JRadioButton squareButton = null;

    private static JRadioButton sinButton = null;

    private static JRadioButton cosButton = null;

    private static JRadioButton tanButton = null;

    private static JRadioButton sinhButton = null;

    private static JRadioButton coshButton = null;

    private static JRadioButton tanhButton = null;

    // radio buttons for view choices
    private static JRadioButton juliaButton = null;

    private static JRadioButton modulusButton = null;

    private static JRadioButton mandelbrotButton = null;

    // selects the imaginary part of the Julia Set constant c
    private static JSpinner imaginaryJuliaSpinner = null;

    // selects the imaginary part of the position in the complex plane
    private static JSpinner imaginaryPositionSpinner = null;

    // selects the real part of the Julia Set constant c
    private static JSpinner realJuliaSpinner = null;

    // selects the real part of the position in the complex plane
    private static JSpinner realPositionSpinner = null;

    // selects the width of the display of the complex plane
    private static JSpinner widthSpinner = null;

    // the currently selected initial state
    private static String initialStateName = INIT_STATE_COMPLEX_PLANE;

    // fonts for display
    private Fonts fonts = null;

    /**
     * Create a rule that displays a Mandlebrot set and other fractals.
     * <p>
     * When calling the parent constructor, the minimalOrLazyInitialization
     * parameter must be included as
     * <code>super(minimalOrLazyInitialization);</code>. The boolean is
     * intended to indicate when the constructor should build a rule with as
     * small a footprint as possible. In order to load rules by reflection, the
     * application must query this class for information like the display name,
     * tooltip description, etc. At these times it makes no sense to build the
     * complete rule which may have a large footprint in memory.
     * <p>
     * It is recommended that the constructor and instance variables do not
     * initialize any memory intensive variables and that variables be
     * initialized only when first needed (lazy initialization). Or all
     * initializations in the constructor may be placed in an <code>if</code>
     * statement.
     * 
     * <pre>
     * if(!minimalOrLazyInitialization)
     * {
     *     ...initialize
     * }
     * </pre>
     * 
     * @param minimalOrLazyInitialization
     *            When true, the constructor instantiates an object with as
     *            small a footprint as possible. When false, the rule is fully
     *            constructed. This variable should be passed to the super
     *            constructor <code>super(minimalOrLazyInitialization);</code>,
     *            but if uncertain, you may safely ignore this variable.
     */
    public Fractal(boolean minimalOrLazyInitialization)
    {
        super(minimalOrLazyInitialization);

        if(!minimalOrLazyInitialization)
        {
            fonts = new Fonts();
            disablePositionComponents();
        }
    }

    /**
     * Reacts to any actions on the JPanel GUI created in
     * getAdditionalPropertiesPanel().
     */
    public void actionPerformed(ActionEvent e)
    {
        if(e.getActionCommand().equals(CLOSE))
        {
            // close dialog box
            JDialog dialog = (JDialog) panel.getTopLevelAncestor();
            dialog.dispose();
        }
        else if(e.getActionCommand().equals(RESET))
        {
            // reset the simulation
            String message = "Submitting a new position and width will \n"
                + "stop and replace the current simulation. \n\nContinue?";

            int answer = JOptionPane.showConfirmDialog(CAController
                .getCAFrame().getFrame(), message, "Reset Confirmation",
                JOptionPane.YES_NO_CANCEL_OPTION);

            if(answer == JOptionPane.YES_OPTION)
            {
                // read the current values

                // reset instance variables
                widthOfDisplay = ((Double) ((SpinnerNumberModel) widthSpinner
                    .getModel()).getNumber()).doubleValue();
                double realValue = ((Double) ((SpinnerNumberModel) realPositionSpinner
                    .getModel()).getNumber()).doubleValue();
                double imaginaryValue = ((Double) ((SpinnerNumberModel) imaginaryPositionSpinner
                    .getModel()).getNumber()).doubleValue();
                upperLeftCorner = new Complex(realValue - widthOfDisplay / 2.0,
                    -imaginaryValue - widthOfDisplay / 2.0);

                // now reset the simulation
                this.resetCA();
            }
        }
    }

    /**
     * Creates the additional properties panel.
     * 
     * @return A panel containing the additional properties.
     */
    private JPanel createAdditionalPropertiesPanel()
    {
        // the panel on which we add the controls
        JPanel innerPanel = new JPanel(new GridBagLayout());

        // the layout looks a little scrunched, so give it some room.
        innerPanel.setPreferredSize(new Dimension(
            innerPanel.getPreferredSize().width, 1000));

        // label for the description
        JPanel functionDescriptionLabel = createFunctionDescriptionPanel();

        // panel holding radio buttons for choices
        JPanel functionRadioPanel = createFunctionRadioButtons();

        // panel holding radio buttons for choices
        JPanel viewRadioPanel = createViewRadioButtons();

        // panel holding the Julia Set constant text boxes
        JPanel constantPanel = createJuliaSetConstantInput();

        // panel holding the spinners selecting the position on the plane
        JPanel positionPanel = createPositionSpinners();

        // description
        int row = 0;
        innerPanel.add(functionDescriptionLabel, new GBC(1, row).setSpan(5, 1)
            .setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(1));

        // function choices radio buttons
        row++;
        innerPanel.add(functionRadioPanel, new GBC(1, row).setSpan(5, 1)
            .setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(1));

        // view choices radio buttons
        row++;
        innerPanel.add(viewRadioPanel, new GBC(1, row).setSpan(5, 1).setFill(
            GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

        // text fields for setting the Julia Set constant
        row++;
        innerPanel.add(constantPanel, new GBC(1, row).setSpan(5, 1).setFill(
            GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

        // text fields for setting the position
        row++;
        innerPanel.add(positionPanel, new GBC(1, row).setSpan(5, 1).setFill(
            GBC.BOTH).setWeight(10.0, 10.0).setAnchor(GBC.WEST).setInsets(1));

        // add some extra space
        row++;
        innerPanel.add(new JLabel("  "), new GBC(1, row).setSpan(5, 1).setFill(
            GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

        return innerPanel;
    }

    /**
     * Creates a panel that displays a message about the functions that can be
     * selected.
     * 
     * @return A panel containing messages.
     */
    private JPanel createFunctionDescriptionPanel()
    {
        // a "grab their attention" panel
        AttentionPanel attentionPanel = new AttentionPanel("Fractal CA");

        String functionDescription = "Each cell is a complex number that is "
            + "averaged with its neighbors. After averaging, you may choose "
            + "to apply a function that transforms the average value.\n\n"
            + "The resulting complex value may be viewed as the modulus of the complex number. "
            + "This is the distance that the cell's value "
            + "falls from the origin. Farther distances are shaded more "
            + "darkly (or as the occupied color if colors have been reset "
            + "by the user) with the darkest color at a distance of approximately "
            + Math.round(getFullState().modulus())
            + ".  Zooming in (small widths) may show only barely perceptible "
            + "differences in shading.\n\n"
            + "Or the complex number can be viewed as a particular fractal or "
            + "Julia set. In this case, the complex number is shaded according "
            + "to how quickly it diverges in an equation such as z = z^2 +c (the "
            + "equation for the Mandelbrot set).  The darkest colors never diverge, "
            + "and lighter colors quickly diverge.";

        MultilineLabel messageLabel = new MultilineLabel(functionDescription);
        messageLabel.setFont(fonts.getMorePropertiesDescriptionFont());
        messageLabel.setMargin(new Insets(2, 6, 2, 2));

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
        messagePanel.add(attentionPanel, BorderLayout.NORTH);
        messagePanel.add(messageLabel, BorderLayout.CENTER);

        return messagePanel;
    }

    /**
     * Create a JPanel holding the radio buttons for choosing a function.
     * 
     * @return A panel holding the radio buttons.
     */
    private JPanel createFunctionRadioButtons()
    {
        noneButton = new JRadioButton("none");
        noneButton.setFont(fonts.getPlainFont());
        noneButton.addItemListener(new FunctionChoiceListener());
        noneButton.setSelected(false);

        iTanButton = new JRadioButton("i tan");
        iTanButton.setFont(fonts.getPlainFont());
        iTanButton.addItemListener(new FunctionChoiceListener());
        iTanButton.setSelected(true);

        squareButton = new JRadioButton("square");
        squareButton.setFont(fonts.getPlainFont());
        squareButton.addItemListener(new FunctionChoiceListener());
        squareButton.setSelected(false);

        sinButton = new JRadioButton("i sin");
        sinButton.setFont(fonts.getPlainFont());
        sinButton.addItemListener(new FunctionChoiceListener());
        sinButton.setSelected(false);

        cosButton = new JRadioButton("(1+i) cos");
        cosButton.setFont(fonts.getPlainFont());
        cosButton.addItemListener(new FunctionChoiceListener());
        cosButton.setSelected(false);

        tanButton = new JRadioButton("(1+i) tan");
        tanButton.setFont(fonts.getPlainFont());
        tanButton.addItemListener(new FunctionChoiceListener());
        tanButton.setSelected(false);

        sinhButton = new JRadioButton("i sinh");
        sinhButton.setFont(fonts.getPlainFont());
        sinhButton.addItemListener(new FunctionChoiceListener());
        sinhButton.setSelected(false);

        coshButton = new JRadioButton("i cosh");
        coshButton.setFont(fonts.getPlainFont());
        coshButton.addItemListener(new FunctionChoiceListener());
        coshButton.setSelected(false);

        tanhButton = new JRadioButton("(1+i) tanh");
        tanhButton.setFont(fonts.getPlainFont());
        tanhButton.addItemListener(new FunctionChoiceListener());
        tanhButton.setSelected(false);

        // put them in a group so that they behave as radio buttons
        ButtonGroup group = new ButtonGroup();
        group.add(noneButton);
        group.add(iTanButton);
        group.add(squareButton);
        group.add(sinButton);
        group.add(cosButton);
        group.add(tanButton);
        group.add(sinhButton);
        group.add(coshButton);
        group.add(tanhButton);

        // label for choosing a radio button
        JLabel functionLabel = new JLabel("Choose a function: ");
        functionLabel.setFont(fonts.getBoldSmallerFont());
        JPanel functionLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        functionLabelPanel.add(functionLabel);

        // now add to a JPanel
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new GridBagLayout());

        // add the buttons, three per row
        int row = 0;
        radioPanel.add(functionLabelPanel, new GBC(0, row).setSpan(12, 1)
            .setFill(GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(0, 1, 0, 0));

        row = 1;
        radioPanel.add(noneButton, new GBC(3, row).setSpan(3, 1).setFill(
            GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(
            0, 30, 0, 0));
        radioPanel.add(squareButton, new GBC(6, row).setSpan(3, 1).setFill(
            GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(1));
        radioPanel.add(iTanButton, new GBC(9, row).setSpan(3, 1).setFill(
            GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(1));

        row = 2;
        radioPanel.add(sinButton, new GBC(3, row).setSpan(3, 1).setFill(
            GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(
            0, 30, 0, 0));
        radioPanel.add(cosButton, new GBC(6, row).setSpan(3, 1).setFill(
            GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(1));
        radioPanel.add(tanButton, new GBC(9, row).setSpan(3, 1).setFill(
            GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(1));

        row = 3;
        radioPanel.add(sinhButton, new GBC(3, row).setSpan(3, 1).setFill(
            GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(
            0, 30, 0, 0));
        radioPanel.add(coshButton, new GBC(6, row).setSpan(3, 1).setFill(
            GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(1));
        radioPanel.add(tanhButton, new GBC(9, row).setSpan(3, 1).setFill(
            GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(1));

        return radioPanel;
    }

    /**
     * Creates text fields for setting the Julia Set constant.
     * 
     * @return A panel holding the text fields.
     */
    private JPanel createJuliaSetConstantInput()
    {
        // listens for events from the spinner
        JuliaConstantListener spinnerListener = new JuliaConstantListener();

        // add a label for the real part
        realLabel = new JLabel("Real part: ");
        realLabel.setFont(fonts.getPlainFont());

        // add a label for the imaginary part
        imaginaryLabel = new JLabel("Imaginary part: ");
        imaginaryLabel.setFont(fonts.getPlainFont());

        // create a spinner for the real value
        SpinnerNumberModel realModel = new SpinnerNumberModel(
            DEFAULT_JULIA_REAL_VALUE, -2.001, 2.001, 0.001);
        realJuliaSpinner = new JSpinner(realModel);
        realJuliaSpinner.setToolTipText(JULIA_REAL_VALUE_TIP);
        realJuliaSpinner.addChangeListener(spinnerListener);
        realJuliaSpinner.addMouseListener(spinnerListener);
        realJuliaSpinner.setFont(fonts.getPlainFont());
        ((JSpinner.DefaultEditor) realJuliaSpinner.getEditor()).getTextField()
            .setColumns(5);

        // add a mouse listener to the arrow components of the spinner
        realJuliaSpinner.getComponent(0).addMouseListener(spinnerListener);
        realJuliaSpinner.getComponent(1).addMouseListener(spinnerListener);

        // create a spinner for the real value
        SpinnerNumberModel imaginaryModel = new SpinnerNumberModel(
            DEFAULT_JULIA_IMAGINARY_VALUE, -2.001, 2.001, 0.001);
        imaginaryJuliaSpinner = new JSpinner(imaginaryModel);
        imaginaryJuliaSpinner.setToolTipText(JULIA_IMAGINARY_VALUE_TIP);
        imaginaryJuliaSpinner.addChangeListener(spinnerListener);
        imaginaryJuliaSpinner.addMouseListener(spinnerListener);
        imaginaryJuliaSpinner.setFont(fonts.getPlainFont());
        ((JSpinner.DefaultEditor) imaginaryJuliaSpinner.getEditor())
            .getTextField().setColumns(5);

        // add a mouse listener to the arrow components of the spinner
        imaginaryJuliaSpinner.getComponent(0).addMouseListener(spinnerListener);
        imaginaryJuliaSpinner.getComponent(1).addMouseListener(spinnerListener);

        // make the spinners enabled or disabled
        if(viewChoice == JULIA)
        {
            realLabel.setEnabled(true);
            imaginaryLabel.setEnabled(true);
            realJuliaSpinner.setEnabled(true);
            imaginaryJuliaSpinner.setEnabled(true);
        }
        else
        {
            realLabel.setEnabled(false);
            imaginaryLabel.setEnabled(false);
            realJuliaSpinner.setEnabled(false);
            imaginaryJuliaSpinner.setEnabled(false);
        }

        // create combo panel
        JPanel realAndImgPanel = new JPanel(new GridBagLayout());
        int row1 = 0;
        realAndImgPanel.add(new JLabel(" "), new GBC(1, row1).setSpan(1, 1)
            .setFill(GBC.HORIZONTAL).setWeight(10.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(0));
        realAndImgPanel.add(realLabel, new GBC(2, row1).setSpan(1, 1).setFill(
            GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(0));
        realAndImgPanel.add(realJuliaSpinner, new GBC(3, row1).setSpan(1, 1)
            .setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(0));
        realAndImgPanel.add(new JLabel(" "), new GBC(4, row1).setSpan(1, 1)
            .setFill(GBC.HORIZONTAL).setWeight(10.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(0));
        row1++;
        realAndImgPanel.add(imaginaryLabel, new GBC(2, row1).setSpan(1, 1)
            .setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(0));
        realAndImgPanel.add(imaginaryJuliaSpinner, new GBC(3, row1).setSpan(1,
            1).setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(0));

        // add a descriptive label
        JLabel constantLabel = new JLabel(
            "Select a constant c for the Julia Set: ");
        constantLabel.setFont(fonts.getBoldSmallerFont());
        JPanel constantLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        constantLabelPanel.add(constantLabel);

        // add the components to a JPanel
        JPanel constantPanel = new JPanel(new GridBagLayout());

        int row = 0;
        constantPanel.add(constantLabelPanel, new GBC(0, row).setSpan(4, 1)
            .setFill(GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(0, 1, 0, 0));

        row = 1;
        constantPanel.add(realAndImgPanel, new GBC(0, row).setSpan(4, 1)
            .setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(0));

        return constantPanel;
    }

    /**
     * Creates text fields for setting the real and imaginary components of the
     * position.
     * 
     * @return A panel holding the text fields.
     */
    private JPanel createPositionSpinners()
    {
        // add a label for the width
        widthLabel = new JLabel("Width: ");
        widthLabel.setFont(fonts.getPlainFont());

        // add a label for the real part
        realPositionLabel = new JLabel("Real position:  ");
        realPositionLabel.setFont(fonts.getPlainFont());

        // add a label for the imaginary part
        imaginaryPositionLabel = new JLabel("Img position: ");
        imaginaryPositionLabel.setFont(fonts.getPlainFont());

        // create a spinner for the width
        SpinnerNumberModel widthModel = new SpinnerNumberModel(DEFAULT_WIDTH,
            0.001, Math.round(getFullState().modulus()), 0.001);
        widthSpinner = new JSpinner(widthModel);
        widthSpinner.setToolTipText(WIDTH_TIP);
        widthSpinner.setFont(fonts.getPlainFont());
        ((JSpinner.DefaultEditor) widthSpinner.getEditor()).getTextField()
            .setColumns(5);

        // create a spinner for the real value
        SpinnerNumberModel realModel = new SpinnerNumberModel(
            DEFAULT_PLANE_REAL_VALUE, -5.001, 5.001, 0.1);
        realPositionSpinner = new JSpinner(realModel);
        realPositionSpinner.setToolTipText(POSITION_REAL_VALUE_TIP);
        realPositionSpinner.setFont(fonts.getPlainFont());
        ((JSpinner.DefaultEditor) realPositionSpinner.getEditor())
            .getTextField().setColumns(5);

        // create a spinner for the imaginary value
        SpinnerNumberModel imaginaryModel = new SpinnerNumberModel(
            DEFAULT_PLANE_IMAGINARY_VALUE, -5.001, 5.001, 0.1);
        imaginaryPositionSpinner = new JSpinner(imaginaryModel);
        imaginaryPositionSpinner.setToolTipText(POSITION_IMAGINARY_VALUE_TIP);
        imaginaryPositionSpinner.setFont(fonts.getPlainFont());
        ((JSpinner.DefaultEditor) imaginaryPositionSpinner.getEditor())
            .getTextField().setColumns(5);

        // add a button for resetting the CA
        resetButton = new JButton(RESET);
        resetButton.setToolTipText(RESET_TOOLTIP);
        resetButton.setFont(fonts.getBoldSmallerFont());
        resetButton.addActionListener(this);

        String positionText = "Select the center and width of display on "
            + "complex plane (reset simulation to take effect):";
        MultilineLabel positionLabel = new MultilineLabel(positionText);
        positionLabel.setFont(fonts.getBoldSmallerFont());
        // positionLabel.setMargin(new Insets(15, 0, 0, 0));

        // create combo panel
        JPanel spinnerPanel = new JPanel(new GridBagLayout());
        int row1 = 0;
        spinnerPanel.add(new JLabel(" "), new GBC(1, row1).setSpan(1, 1)
            .setFill(GBC.HORIZONTAL).setWeight(10.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(0));
        spinnerPanel.add(realPositionLabel, new GBC(2, row1).setSpan(1, 1)
            .setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(0));
        spinnerPanel.add(realPositionSpinner, new GBC(3, row1).setSpan(1, 1)
            .setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(0));
        spinnerPanel.add(new JLabel(" "), new GBC(4, row1).setSpan(1, 1)
            .setFill(GBC.HORIZONTAL).setWeight(10.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(0));
        row1++;
        spinnerPanel.add(imaginaryPositionLabel, new GBC(2, row1).setSpan(1, 1)
            .setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(0));
        spinnerPanel.add(imaginaryPositionSpinner, new GBC(3, row1).setSpan(1,
            1).setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(0));

        row1++;
        spinnerPanel.add(widthLabel, new GBC(2, row1).setSpan(1, 1).setFill(
            GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(0));
        spinnerPanel.add(widthSpinner, new GBC(3, row1).setSpan(1, 1).setFill(
            GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(0));
        row1++;
        spinnerPanel.add(resetButton, new GBC(3, row1).setSpan(1, 1).setFill(
            GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(0));

        // add info label
        String infoDescription = "(1) Try  -0.125 + 0.875i and width 0.75 for the Mandelbrot view. "
            + "A 200 by 200 lattice (or larger) looks best.\n"
            + "(2) Try  -0.5 + 0.0i and width 3.0 for the Mandelbrot view.\n"
            + "(3) Try 0.0 + 0.0i and width 0.75 for the Julia Set view.\n"
            + "(4) Smaller widths zoom in, and larger widths zoom out.";
        MultilineLabel messageLabel = new MultilineLabel(infoDescription);
        messageLabel.setFont(fonts.getMorePropertiesDescriptionFont());
        messageLabel.setMargin(new Insets(0, 2, 0, 0));

        // add the components to a JPanel
        JPanel constantPanel = new JPanel(new GridBagLayout());

        int row = 0;
        constantPanel.add(positionLabel, new GBC(0, row).setSpan(4, 1).setFill(
            GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(
            0, 6, 0, 0));
        row++;
        constantPanel.add(spinnerPanel, new GBC(0, row).setSpan(4, 1).setFill(
            GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(0));
        row++;
        constantPanel.add(messageLabel, new GBC(0, row).setSpan(4, 1).setFill(
            GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(10, 10,
            0, 0));

        // now disable if necessary
        disablePositionComponents();

        return constantPanel;
    }

    /**
     * Create a JPanel holding the radio buttons for choosing a view.
     * 
     * @return A panel holding the radio buttons.
     */
    private JPanel createViewRadioButtons()
    {
        modulusButton = new JRadioButton("modulus, |z|");
        modulusButton.setToolTipText("A standard view. Displays as the "
            + "distance from the origin.");
        modulusButton.setFont(fonts.getPlainFont());
        modulusButton.addItemListener(new ViewChoiceListener());
        modulusButton.setSelected(false);

        mandelbrotButton = new JRadioButton("Mandelbrot");
        mandelbrotButton
            .setToolTipText("Displays as the time it takes z to diverge "
                + "in z = z^2 + c where c is the cell value.");
        mandelbrotButton.setFont(fonts.getPlainFont());
        mandelbrotButton.addItemListener(new ViewChoiceListener());
        mandelbrotButton.setSelected(false);

        juliaButton = new JRadioButton("Julia Set");
        juliaButton
            .setToolTipText("Displays as the time it takes z to diverge "
                + "in z = z^2 + c where c is a fixed value.");
        juliaButton.setFont(fonts.getPlainFont());
        juliaButton.addItemListener(new ViewChoiceListener());
        juliaButton.setSelected(true);

        // put them in a group so that they behave as radio buttons
        ButtonGroup group = new ButtonGroup();
        group.add(modulusButton);
        group.add(mandelbrotButton);
        group.add(juliaButton);

        // label for choosing the view
        JLabel viewLabel = new JLabel("Choose a view: ");
        viewLabel.setFont(fonts.getBoldSmallerFont());
        JPanel viewLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        viewLabelPanel.add(viewLabel);

        // now add to a JPanel
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new GridBagLayout());

        // add the buttons, three per row
        int row = 0;
        radioPanel.add(viewLabelPanel, new GBC(0, row).setSpan(12, 1).setFill(
            GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(
            0, 1, 0, 0));

        row = 1;
        radioPanel.add(modulusButton, new GBC(3, row).setSpan(3, 1).setFill(
            GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(
            0, 30, 0, 0));
        radioPanel.add(mandelbrotButton, new GBC(6, row).setSpan(3, 1).setFill(
            GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(1));
        radioPanel.add(juliaButton, new GBC(9, row).setSpan(3, 1).setFill(
            GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(1));

        return radioPanel;
    }

    /**
     * Disables position components if the initial state is not one of the
     * complex plane options.
     */
    private void disablePositionComponents()
    {
        // now disable if necessary (note: if widthLabel isn't null, then
        // everything else will have been instantiated as well)
        if(widthLabel != null)
        {
            String initStateFile = CurrentProperties.getInstance()
                .getInitialState();

            // see if the initial state is one of the special initial states
            boolean isRuleSpecificInitialState = false;
            for(int i = 0; i < getInitialStateNames().length; i++)
            {
                String initState = getInitialStateNames()[i];

                isRuleSpecificInitialState |= initStateFile.equals(initState);
            }

            if(isRuleSpecificInitialState)
            {
                // enable everything
                widthLabel.setEnabled(true);
                realPositionLabel.setEnabled(true);
                imaginaryPositionLabel.setEnabled(true);
                widthSpinner.setEnabled(true);
                realPositionSpinner.setEnabled(true);
                imaginaryPositionSpinner.setEnabled(true);
                resetButton.setEnabled(true);
            }
            else
            {
                // disable everything
                widthLabel.setEnabled(false);
                realPositionLabel.setEnabled(false);
                imaginaryPositionLabel.setEnabled(false);
                widthSpinner.setEnabled(false);
                realPositionSpinner.setEnabled(false);
                imaginaryPositionSpinner.setEnabled(false);
                resetButton.setEnabled(false);
            }

            // if(initStateFile.equals(CurrentProperties.STATE_BLANK)
            // || initStateFile.equals(CurrentProperties.STATE_RANDOM)
            // || initStateFile
            // .equals(CurrentProperties.STATE_SINGLE_SEED)
            // || initStateFile.equals(CurrentProperties.STATE_DATA)
            // || initStateFile.equals(CurrentProperties.STATE_ELLIPSE)
            // || initStateFile.equals(CurrentProperties.STATE_IMAGE)
            // || initStateFile
            // .equals(CurrentProperties.STATE_PROBABILITY)
            // || initStateFile.equals(CurrentProperties.STATE_RECTANGLE))
        }
    }

    /**
     * Gets a complex value for the cell at position (row, col) when the cells
     * are arranged as on a normal complex plane.
     * 
     * @param row
     *            The cell's row.
     * @param col
     *            The cell's row.
     * @param increment
     *            The incremental value between adjacent cells.
     * @return The complex value associated with the cell at position (row,
     *         col).
     */
    private Complex getCellValueForComplexPlane(int row, int col,
        double increment)
    {
        // the complex value for the cell.
        Complex cellValue = new Complex(0.0, 0.0);

        cellValue.real = col * increment + upperLeftCorner.real;
        cellValue.imaginary = row * increment + upperLeftCorner.imaginary;

        return cellValue;
    }

    /**
     * Gets a complex value for the cell at position (row, col) when the cells
     * are symmetrically valued around a central point.
     * 
     * @param row
     *            The cell's row.
     * @param col
     *            The cell's row.
     * @param increment
     *            The incremental value between adjacent cells.
     * @return The complex value associated with the cell at position (row,
     *         col).
     */
    private Complex getCellValueForComplexSymmetric(int row, int col,
        double increment)
    {
        // the complex value for the cell.
        Complex cellValue = new Complex(0.0, 0.0);

        // a complex plane symmetric around axes centered at the middle
        // of the grid
        int middleRowPosition = (int) Math.round(numberOfRows / 2.0);
        int middleColPosition = (int) Math.round(numberOfCols / 2.0);

        if(col <= middleColPosition)
        {
            cellValue.real = col * increment + upperLeftCorner.real;
        }
        else
        {
            cellValue.real = (2 * middleColPosition - col) * increment
                + upperLeftCorner.real;
        }

        if(row <= middleRowPosition)
        {
            cellValue.imaginary = row * increment + upperLeftCorner.imaginary;
        }
        else
        {
            cellValue.imaginary = (2 * middleColPosition - row) * increment
                + upperLeftCorner.imaginary;
        }

        return cellValue;
    }

    /**
     * Gets a JPanel that may request specific input information that the rule
     * needs to operate correctly. Should be overridden by child classes that
     * desire to input any specific information. <br>
     * Note that if returns null, then the panel is not displayed by the current
     * version of the CA ControlPanel class. This null behavior is the default.
     * 
     * @return A JPanel requesting specific input information that the rule
     *         needs to operate correctly. May be null.
     */
    public JPanel getAdditionalPropertiesPanel()
    {
        // only recreate the panel if necessary
        if(panel == null)
        {
            panel = createAdditionalPropertiesPanel();
        }

        return panel;
    }

    /**
     * Gets a complex number that represents the alternate state (a state that
     * is drawn with a right mouse click). In this case, the alternate state is
     * a random number between the full and empty states. Implementations should
     * be careful to return a new instance each time this is called. Otherwise,
     * multiple CA cells may be sharing the same instance and a change to one
     * cell could change many cells.
     * 
     * @return The alternate state.
     */
    public Complex getAlternateState()
    {
        Random r = RandomSingleton.getInstance();

        double real = r.nextDouble()
            * (getFullState().real - getEmptyState().real)
            + getEmptyState().real;
        double imaginary = r.nextDouble()
            * (getFullState().imaginary - getEmptyState().imaginary)
            + getEmptyState().imaginary;

        return new Complex(real, imaginary);
    }

    /**
     * A brief description (written in HTML) that describes what parameters will
     * give best results for this rule (which lattice, how many states, etc).
     * The description will be displayed on the properties panel. Using html
     * permits line breaks, font colors, etcetera, as described in HTML
     * resources. Regular line breaks will not work.
     * <p>
     * Recommend starting with the title of the rule followed by "For best
     * results, ...". See Rule 102 for an example.
     * 
     * @return An HTML string describing how to get best results from this rule.
     *         May be null.
     */
    public String getBestResultsDescription()
    {
        return BEST_RESULTS;
    }

    /**
     * Gets an instance of the CellStateView class that will be used to display
     * cells being updated by this rule. Note: This method must return a view
     * that is able to display cell states of the type returned by the method
     * getCompatibleCellState(). Appropriate CellStatesViews to return include
     * BinaryCellStateView, IntegerCellStateView, HexagonalIntegerCellStateView,
     * IntegerVectorArrowView, IntegerVectorDefaultView, and
     * RealValuedDefaultView among others. the user may also create their own
     * views (see online documentation).
     * <p>
     * Any values passed to the constructor of the CellStateView should match
     * those values needed by this rule.
     * 
     * @return An instance of the CellStateView (any values passed to the
     *         constructor of the CellStateView should match those values needed
     *         by this rule).
     */
    public CellStateView getCompatibleCellStateView()
    {
        return new ComplexView();
    }

    /**
     * A list of lattices with which this Rule will work. Returns null to
     * indicate that this rule works with any lattice. <br>
     * Well-designed Rules should work with any lattice, but some may require
     * particular topological or geometrical information (like the lattice gas).
     * Appropriate strings to return in the array include
     * SquareLattice.DISPLAY_NAME, HexagonalLattice.DISPLAY_NAME,
     * StandardOneDimensionalLattice.DISPLAY_NAME, etc.
     * 
     * @return A list of lattices compatible with this Rule.
     */
    public String[] getCompatibleLattices()
    {
        return null;
    }

    /**
     * When displayed for selection, the rule will be listed under specific
     * folders specified here. The rule will always be listed under the "All
     * rules" folder. And if the rule is contributed by a user and is placed in
     * the userRules folder, then it will also be shown in a folder called "User
     * rules". Any strings may be used; if the folder does not exist, then one
     * will be created with the specified name. If the folder already exists,
     * then that folder will be used.
     * <p>
     * By default, this returns null so that the rule is only placed in the
     * default folder(s).
     * <p>
     * Child classes should override this method if they want the rule to appear
     * in a specific folder. The "All rules" and "User rules" folder are
     * automatic and do not need to be specified; they are always added.
     * 
     * @return A list of the folders in which rule will be displayed for
     *         selection. May be null.
     */
    public String[] getDisplayFolderNames()
    {
        String[] folders = {RuleFolderNames.PRETTY_FOLDER,
            RuleFolderNames.COMPLEX_VALUED_FOLDER,
            RuleFolderNames.COMPUTATIONALLY_INTENSIVE_FOLDER};

        return folders;
    }

    /**
     * A brief one or two-word string describing the rule, appropriate for
     * display in a drop-down list.
     * 
     * @return A string no longer than 15 characters.
     */
    public String getDisplayName()
    {
        return RULE_NAME;
    }

    /**
     * Gets a complex number that represents the empty state. Implementations
     * should be careful to return a new instance each time this is called.
     * Otherwise, multiple CA cells may be sharing the same instance and a
     * change to one cell could change many cells.
     * 
     * @return The empty state.
     */
    public Complex getEmptyState()
    {
        return new Complex(0.0, 0.0);
    }

    /**
     * Gets a complex number that represents the full or filled state.
     * Implementations should be careful to return a new instance each time this
     * is called. Otherwise, multiple CA cells may be sharing the same instance
     * and a change to one cell could change many cells.
     * 
     * @return The full state.
     */
    public Complex getFullState()
    {
        return new Complex(3.0, 3.0);
    }

    /**
     * Sets an initial state that corresponds to the values on a complex plane.
     * 
     * @param initialStateName
     *            The name of the initial state (will be one of the names
     *            specified in the getInitialStateNames method).
     * @param lattice
     *            The CA lattice. This will either be a one or two-dimensional
     *            lattice which holds the cells. The cells should be collected
     *            from the lattice and assigned initial values.
     */
    public void setInitialState(String initialStateName, Lattice lattice)
    {
        Cell cell = null;
        Iterator cellIterator = lattice.iterator();
        while(cellIterator.hasNext())
        {
            cell = (Cell) cellIterator.next();

            // get the list of states for each cell
            cell.getState().setValue(getInitialStateValue(initialStateName));
        }
    }

    /**
     * Gets an array of names for any initial states defined by the rule. In
     * other words, if the rule has two initial states that it would like to
     * define, then they are each given names and placed in an array of size 2
     * that is returned by this method. The names are then displayed on the
     * Properties panel. The CA will always have "single seed", "random", and
     * "blank" for initial states. This array specifies additional initial
     * states that should be displayed. The names should be unique. By default
     * this returns null. Child classes should override the method if they wish
     * to specify initial states that will appear on the Properties panel. Note:
     * This method should be used in conjuction with the getInitialState method,
     * also in this class.
     * 
     * @return An array of names for initial states that are specified by the
     *         rule.
     */
    public String[] getInitialStateNames()
    {
        String[] initialStates = {INIT_STATE_COMPLEX_PLANE,
            INIT_STATE_COMPLEX_PLANE_SYMMETRIC}; // INIT_STATE_COMPLEX_PLANE_COS};
        return initialStates;
    }

    /**
     * Gets tool tips for the initial states.
     * 
     * @return An array of tool tips for initial states that are specified by
     *         the rule.
     */
    public String[] getInitialStateToolTips()
    {
        String[] initialStateToolTips = {INIT_STATE_COMPLEX_PLANE_TOOLTIP,
            INIT_STATE_COMPLEX_PLANE_SYMMETRIC_TOOLTIP}; // INIT_STATE_COMPLEX_PLANE_COS_TOOLTIP};
        return initialStateToolTips;
    }

    /**
     * The initial state value for the given parameter.
     * 
     * @param nameOfinitialState
     *            The name of the initial state configuration.
     * @return The value of the cell.
     */
    public Complex getInitialStateValue(String nameOfinitialState)
    {
        // set the initial state instance variable
        initialStateName = nameOfinitialState;

        // the value of the cell
        Complex cellValue = new Complex(0.0, 0.0);

        // let's set up the initial fractal by populating the grid with
        // numbers that fall in their correct location on the complex plane
        int numRows = CurrentProperties.getInstance().getNumRows();
        int numCols = CurrentProperties.getInstance().getNumColumns();

        // reset to the origin when necessary
        if(cellNum >= numRows * numCols)
        {
            cellNum = 0;
        }

        // make sure the simulation didn't get reset
        if(numberOfCols != numCols)
        {
            numberOfCols = numCols;
            cellNum = 0;
        }
        if(numberOfRows != numRows)
        {
            numberOfRows = numRows;
            cellNum = 0;
        }

        int row = cellNum / numRows; // integer division on purpose!
        int col = cellNum % numCols;

        // increase each cell by this amount
        double increment = widthOfDisplay / (double) numRows;

        if(nameOfinitialState.equals(INIT_STATE_COMPLEX_PLANE_SYMMETRIC))
        {
            cellValue = getCellValueForComplexSymmetric(row, col, increment);
        }
        // else if(nameOfinitialState.equals(INIT_STATE_COMPLEX_PLANE_COS))
        // {
        // cellValue = getCellValueForComplexCos(row, col, increment);
        // }
        else
        {
            // the normal complex plane
            cellValue = getCellValueForComplexPlane(row, col, increment);
        }

        cellNum++;

        return cellValue;
    }

    /**
     * A brief description (written in HTML) that describes this rule. The
     * description will be displayed as a tooltip. Using html permits line
     * breaks, font colors, etcetera, as described in HTML resources. Regular
     * line breaks will not work.
     * 
     * @return An HTML string describing this rule.
     */
    public String getToolTipDescription()
    {
        return TOOLTIP;
    }

    /**
     * Returns a function of the average of a cell and its neighbors. The
     * function is set in the more properties panel, but defaults to the
     * identity function.
     * 
     * @param cell
     *            The value of the cell being updated.
     * @param neighbors
     *            The value of the neighbors.
     * @param generation
     *            The current generation of the CA.
     * @return A new state for the cell.
     */
    protected synchronized Complex complexRule(Complex cellValue,
        Complex[] neighborValues, int generation)
    {
        // only update the function number at the beginning of each generation
        if(currentGeneration != generation)
        {
            currentGeneration = generation;

            // The function number used by the rule. Once per generation it is
            // reset to the functionChoice selected by the user from the "more
            // properties panel."
            functionNumber = functionChoice;
        }

        // the new value of the cell that will be returned
        Complex newValue = new Complex(cellValue.real, cellValue.imaginary);

        // let's average the cell and it's neighbors
        for(int i = 0; i < neighborValues.length; i++)
        {
            Complex neighbor = neighborValues[i];
            newValue.real += neighbor.real;
            newValue.imaginary += neighbor.imaginary;
        }

        // now divide to get the average of the cell and its neighbors
        newValue.real /= (neighborValues.length + 1.0);
        newValue.imaginary /= (neighborValues.length + 1.0);

        // now apply a function if desired
        switch(functionNumber)
        {
            case 0:
            {
                // do nothing
                break;
            }
            case 1:
            {
                // take i * tan
                newValue = Complex.multiply(new Complex(0, 1), Complex
                    .tan(newValue));
                break;
            }
            case 2:
            {
                // take the sqr
                newValue = Complex.multiply(newValue, newValue);
                break;
            }
            case 3:
            {
                // take i * sin
                newValue = Complex.multiply(new Complex(0, 1), Complex
                    .sin(newValue));
                break;
            }
            case 4:
            {
                // take (1+i) * cos
                newValue = Complex.multiply(new Complex(1, 1), Complex
                    .cos(newValue));
                break;
            }
            case 5:
            {
                // take (1+i) * tan
                newValue = Complex.multiply(new Complex(1, 1), Complex
                    .tan(newValue));
                break;
            }
            case 6:
            {
                // take the i * sinh
                newValue = Complex.multiply(new Complex(0, 1), Complex
                    .sinh(newValue));
                break;
            }
            case 7:
            {
                // take the i * cosh
                newValue = Complex.multiply(new Complex(0, 1), Complex
                    .cosh(newValue));
                break;
            }
            case 8:
            {
                // take (1+i) * tanh
                newValue = Complex.multiply(new Complex(1, 1), Complex
                    .tanh(newValue));
                break;
            }
            default:
            {
                // do nothing
                break;
            }
        }

        // return the new complex number
        return newValue;
    }

    /**
     * A view that displays complex numbers as fractals or simple shades. Tells
     * the graphics how to display the complexNumber stored by a cell.
     * 
     * @author David Bahr
     */
    private class ComplexView extends TriangleHexagonCellStateView
    {
        /**
         * Creates a view that displays complex numbers as fractals or simple
         * shades. Tells the graphics how to display the complexNumber stored by
         * a cell.
         */
        public ComplexView()
        {
            super();
        }

        /**
         * Creates a display color based on the complex number in the cell.
         * Creates a fractional shading between the default filled and empty
         * color.
         * 
         * @param state
         *            The cell state that will be displayed.
         * @param numStates
         *            If relevant, the number of possible states (which may not
         *            be the same as the currently active number of states) --
         *            may be null which indicates that the number of states is
         *            inapplicable or that the currently active number of states
         *            should be used. (See for example,
         *            createProbabilityChoosers() method in InitialStatesPanel
         *            class.)
         * @param rowAndCol
         *            The row and col of the cell being displayed. May be
         *            ignored.
         * @return The color to be displayed.
         */
        public Color getColor(CellState state, Integer numStates,
            Coordinate rowAndCol)
        {
            Complex cellValue = (Complex) state.getValue();

            // can view as a fractal or in normal complex space (as the modulus)
            double iteration = 0;
            int maxIteration = 40;
            if(viewChoice == MANDELBROT)
            {
                // view as the mandelbrot set
                // i.e., color according to the algorithm z = z^2 + c where
                // c is given by z_0 (the value of the cell), and the
                // rate of divergence dictates the color. If never diverges,
                // then the cell value is on the Mandelbrot set.
                iteration = 0;
                maxIteration = 40;

                // calculate the first iteration
                Complex c = new Complex(cellValue.getReal(), cellValue
                    .getImaginary());
                Complex z = new Complex(cellValue.getReal(), cellValue
                    .getImaginary());
                z = Complex.plus(Complex.multiply(z, z), c);

                // Now iterate until diverges. The variable "iteration" holds
                // the number of times before diverged. This is used to plot a
                // shade of color.
                while(z.modulus() < 1.75 && iteration < maxIteration)
                {
                    z = Complex.plus(Complex.multiply(z, z), c);

                    iteration = iteration + 1;
                }
            }
            else if(viewChoice == JULIA)
            {
                // view as a julia set
                // i.e., color according to the algorithm z = z^2 + c where
                // c is a fixed constant (not necessarily z_0, the value of
                // the cell), and the rate of divergence dictates the color.
                // If never diverges, then the cell value is on the Julia set.
                iteration = 0;
                maxIteration = 40;

                // Calculate the first iteration. Note that juliaConstant is the
                // constant "c"
                Complex z = new Complex(cellValue.getReal(), cellValue
                    .getImaginary());
                z = Complex.plus(Complex.multiply(z, z), juliaConstant);

                // Now iterate until diverges. The variable "iteration" holds
                // the number of times before diverged. This is used to plot a
                // shade of color.
                while(z.modulus() < 1.75 && iteration < maxIteration)
                {
                    z = Complex.plus(Complex.multiply(z, z), juliaConstant);

                    iteration = iteration + 1;
                }
            }
            else
            {
                return new ComplexModulusView(getEmptyState(), getFullState())
                    .getColor(state, null, rowAndCol);
            }

            // now select a color scaled between the empty and filled color
            Color filledColor = ColorScheme.FILLED_COLOR;
            Color emptyColor = ColorScheme.EMPTY_COLOR;

            double redDiff = filledColor.getRed() - emptyColor.getRed();
            double greenDiff = filledColor.getGreen() - emptyColor.getGreen();
            double blueDiff = filledColor.getBlue() - emptyColor.getBlue();

            double redDelta = redDiff / (maxIteration - 1);
            double greenDelta = greenDiff / (maxIteration - 1);
            double blueDelta = blueDiff / (maxIteration - 1);

            int red = (int) Math.floor(emptyColor.getRed()
                + (iteration * redDelta));
            int green = (int) Math.floor(emptyColor.getGreen()
                + (iteration * greenDelta));
            int blue = (int) Math.floor(emptyColor.getBlue()
                + (iteration * blueDelta));

            if(iteration == maxIteration)
            {
                red = 0;
                green = 0;
                blue = 0;
            }

            return new Color(red, green, blue);
        }
    }

    /**
     * Decides what to do when the user selects a function.
     * 
     * @author David Bahr
     */
    private class FunctionChoiceListener implements ItemListener
    {
        public void itemStateChanged(ItemEvent event)
        {
            if(noneButton.isSelected())
            {
                functionChoice = NONE;
            }
            else if(iTanButton.isSelected())
            {
                functionChoice = I_TAN;
            }
            else if(squareButton.isSelected())
            {
                functionChoice = SQUARE;
            }
            else if(sinButton.isSelected())
            {
                functionChoice = SIN;
            }
            else if(cosButton.isSelected())
            {
                functionChoice = COS;
            }
            else if(tanButton.isSelected())
            {
                functionChoice = TAN;
            }
            else if(sinhButton.isSelected())
            {
                functionChoice = SINH;
            }
            else if(coshButton.isSelected())
            {
                functionChoice = COSH;
            }
            else if(tanhButton.isSelected())
            {
                functionChoice = TANH;
            }
        }
    }

    /**
     * Listens for changes to the spinners that set the Julia constant.
     * 
     * @author David Bahr
     */
    private class JuliaConstantListener extends MouseAdapter implements
        ChangeListener
    {
        private boolean mouseReleased = true;

        /**
         * Listens for changes to the real and imaginary spinners for the Julia
         * constant.
         * 
         * @param e
         */
        public void stateChanged(ChangeEvent e)
        {
            if(mouseReleased)
            {
                // read the real number
                Double real = (Double) ((SpinnerNumberModel) realJuliaSpinner
                    .getModel()).getNumber();

                // read the imaginary number
                Double imaginary = (Double) ((SpinnerNumberModel) imaginaryJuliaSpinner
                    .getModel()).getNumber();

                // set the new Julia Set constant
                juliaConstant = new Complex(real.doubleValue(), imaginary
                    .doubleValue());

                // now let the graphics know that they need to update!
                // This is a method in the Rule's parent class.
                refreshGraphics();
            }
        }

        /**
         * While a spinner is pressed, pause the CA.
         */
        public void mousePressed(MouseEvent e)
        {
            mouseReleased = false;

            // pause the simulation if the Julia Set view is selected
            if(viewChoice == JULIA)
            {
                // pause the simulation.
                pauseCA();
            }
        }

        /**
         * When a spinner is released, restart the CA.
         */
        public void mouseReleased(MouseEvent e)
        {
            mouseReleased = true;

            // restart the simulation if the Julia Set view is selected
            if(viewChoice == JULIA)
            {
                // restart the simulation
                restartCA();

                // change the constant and update the graphics
                stateChanged(null);
            }
        }
    }

    /**
     * Decides what to do when the user selects a view.
     * 
     * @author David Bahr
     */
    private class ViewChoiceListener implements ItemListener
    {
        public void itemStateChanged(ItemEvent event)
        {
            if(modulusButton.isSelected())
            {
                viewChoice = MODULUS;
            }
            else if(mandelbrotButton.isSelected())
            {
                viewChoice = MANDELBROT;
            }
            else if(juliaButton.isSelected())
            {
                viewChoice = JULIA;
            }

            // enable or disable the Julia constant spinners
            if(realJuliaSpinner != null && imaginaryJuliaSpinner != null
                && realLabel != null && imaginaryLabel != null)
            {
                if(viewChoice == JULIA)
                {
                    realLabel.setEnabled(true);
                    imaginaryLabel.setEnabled(true);
                    realJuliaSpinner.setEnabled(true);
                    imaginaryJuliaSpinner.setEnabled(true);
                }
                else
                {
                    realLabel.setEnabled(false);
                    imaginaryLabel.setEnabled(false);
                    realJuliaSpinner.setEnabled(false);
                    imaginaryJuliaSpinner.setEnabled(false);
                }
            }

            // now let the graphics know that they need to update!
            refreshGraphics();
        }
    }
}

// ------------------------------------------------------------

// OLD CODE THAT MAY STILL BE USEFUL

// The tooltip for an initial state configuration
// private static final String INIT_STATE_COMPLEX_PLANE_COS_TOOLTIP =
// "<html>Values vary "
// + "as a cos wave along each axis centered about complex number that can
// be specified "
// + "in the additional properties. <br>"
// + "In other words, along the real axis values follow a cos with a
// wavelength that "
// + "equals the size of the grid. The amplitude is the amplitude of the
// central value.<br><br> "
// + "May also type in the path or browse to a data file or image. </html>";

/**
 * Gets a complex value for the cell at position (row, col) when the cells vary
 * as a cos function around a central point. The cos function starts on the left
 * hand side of the grid and ends on the right hand side (for the real values).
 * It also starts at the top and ends at the bottom (for imaginary values). In
 * other words, the highest real value will be at the far left (and far right)
 * of the grid, and the highest imaginary value will be at the top (and bottom)
 * of the grid. The amplitude of the wave (along each axis) is set as the real
 * and imaginary parts of the center point selected by the user.
 * 
 * @param row
 *            The cell's row.
 * @param col
 *            The cell's row.
 * @param increment
 *            The incremental value between adjacent cells.
 * @return The complex value associated with the cell at position (row, col).
 */
// private Complex getCellValueForComplexCos(int row, int col, double
// increment)
// {
// // the complex value for the cell.
// Complex cellValue = new Complex(0.0, 0.0);
//
// // the middle of the grid
// int middleRowPosition = (int) Math.round(numberOfRows / 2.0);
// int middleColPosition = (int) Math.round(numberOfCols / 2.0);
//
// // the leftmost real value on the grid
// double x0 = upperLeftCorner.real;
//
// // the rightmost real value on the grid
// double x1 = numberOfCols * increment + upperLeftCorner.real;
//
// // the current real value on the grid (for the particular cell)
// double x = col * increment + upperLeftCorner.real;
//
// // the maximum value (center position)
// double maxReal = upperLeftCorner.real + middleColPosition * increment;
//
// cellValue.real = maxReal
// * Math.cos((Math.PI / (x1 - x0)) * (x - x0));
//
// // the uppermost imaginary value on the grid
// double y0 = upperLeftCorner.imaginary;
//
// // the lowermost imaginary value on the grid
// double y1 = numberOfRows * increment + upperLeftCorner.imaginary;
//
// // the current imaginary value on the grid (for the particular cell)
// double y = row * increment + upperLeftCorner.imaginary;
//
// // the maximum value (center position)
// double maxImaginary = upperLeftCorner.imaginary + middleRowPosition
// * increment;
//
// cellValue.imaginary = maxImaginary
// * Math.cos((Math.PI / (y1 - y0)) * (y - y0));
//
// return cellValue;
// }
