/*
 MovingFractal -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.rules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.Random;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import cellularAutomata.CAConstants;
import cellularAutomata.CurrentProperties;
import cellularAutomata.Cell;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.ComplexVectorDefaultView;
import cellularAutomata.cellState.view.TriangleHexagonCellStateView;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.FourNeighborSquareLattice;
import cellularAutomata.lattice.HexagonalLattice;
import cellularAutomata.lattice.SquareLattice;
import cellularAutomata.lattice.StandardOneDimensionalLattice;
import cellularAutomata.lattice.TriangularLattice;
import cellularAutomata.lattice.OneDimensionalLattice;
import cellularAutomata.lattice.TwoDimensionalLattice;
import cellularAutomata.rules.templates.ComplexVectorRuleTemplate;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;
import cellularAutomata.util.math.Complex;
import cellularAutomata.util.math.RandomSingleton;

/**
 * A lattice-gas-style rule with vectors of complex numbers. The complex values
 * move in two steps. (1) They translate to the cell towards which they are
 * pointing. (2) Then they reorient themselves so that the element with the
 * largest real value points toward the right, the smallest real value points to
 * the left, the largest imaginary value points up, and the smallest imaginary
 * value points down.
 * <p>
 * For details, see the basic manner in which lattice gasses perform. This is
 * the same, but the collision step is now just the re-orientation step
 * described above.
 * 
 * @author David Bahr
 */
public class MovingFractal extends ComplexVectorRuleTemplate
{
    // the default empty state for each element of the vector
    private static final Complex DEFAULT_EMPTY_STATE = new Complex(0.0, 0.0);

    // the default full state for each element of the vector
    private static final Complex DEFAULT_FULL_STATE = new Complex(1.5, 1.5);

    // the value used to represent the presence of a wall when it is in the last
    // vector position
    private static final Complex WALL_VALUE = new Complex(1000000.0, 1000000.0);

    // default imaginary value for the Julia Set constant
    private static final double DEFAULT_JULIA_IMAGINARY_VALUE = -0.2321;

    // default real value for the Julia Set constant
    private static final double DEFAULT_JULIA_REAL_VALUE = -0.835;

    // the Julia Set constant -- may be reset by the user
    private static Complex juliaConstant = new Complex(
        DEFAULT_JULIA_REAL_VALUE, DEFAULT_JULIA_IMAGINARY_VALUE);

    // which view to use is determined by these constants
    private static final int MODULUS = 0;

    private static final int MANDELBROT = 1;

    private static final int JULIA = 2;

    // random generator
    private static Random random = RandomSingleton.getInstance();

    // label for the close button
    private static final String CLOSE = "Close";

    // a display name for an intial state with random values and a wall around
    // the edge
    private static final String RANDOM_WITH_WALL = "Random with walls";

    // a display name for an intial state with random values and a wall around
    // the edge
    private static final String RANDOM_WITH_WALL_TOOLTIP = "<html>Creates random values "
        + "with a wall around the edge.</html>";

    private static String rightClick = "<p><b>Right-click</b> the grid to draw walls. ";

    // a display name for this class
    private static final String RULE_NAME = "Moving Fractal";

    // a description of property choices that give the best results for this
    // rule (e.g., which lattice, how many states, etc.)
    private static String BEST_RESULTS = "<html> <body><b>"
        + RULE_NAME
        + ".</b>"
        + "<p> "
        + "<b>For best results</b>, try a \"square (4 neighbor)\" lattice with a 100 by 100 or "
        + "larger grid.  Average the results for 4 or more time steps to eliminate noise.  For "
        + "the initial state select \"Random with walls\".  Under \"More Properties\", select "
        + "the Julia set view and compare to the modulus view.  Larger simulations and greater "
        + "averaging work best, but will quickly overwhelm the available memory and crash "
        + "the simulation.  Larger numbers of neighbors will also overwhelm the memory, so "
        + "square (4-neighbor) and triangular (3 neighbor) lattices work best.  Drawing extra "
        + "walls will cause unusual variations in the standard fractal shapes.  Try a box within "
        + "a box (in other words, draw the outline of a square inside of another square)."
        + leftClickInstructions;

    // a tooltip description for this class
    private String TOOLTIP = "<html> <body><b>"
        + RULE_NAME
        + ".</b> Evolves into fractal shapes as complex numbers move around like a lattice gas.</body></html>";

    // keeps track of the number of cells that have had initial state values
    // assigned to them
    private static int cellNum = 0;

    // the number of columns on the lattice
    private static int numberOfCols = 0;

    // the number of rows on the lattice
    private static int numberOfRows = 0;

    // which view is used is given by this integer (set to one of the
    // constants MODULUS, MANDELBROT, etc.)
    private static int viewChoice = JULIA;

    // the JPanel that is returned by getAdditionalPropertiesPanel()
    private static JPanel panel = null;

    // radio buttons for view choices
    private static JRadioButton juliaButton = null;

    private static JRadioButton modulusButton = null;

    private static JRadioButton mandelbrotButton = null;

    // whether or not the simulation is one- or two-dimensional. Set in
    // constructor.
    private boolean isOneDimensional = false;

    // fonts for display
    private Fonts fonts = null;

    // the number of neighbors for each cell (default is square
    // nearest-neighbor)
    private int numberOfNeighbors = 8;

    // the size of the vector holding the complex numbers at each cell.
    // It is + 2, so that we have a rest state and a site indicating the
    // presence (or not) of a wall.
    private int sizeOfVector = numberOfNeighbors + 2;

    /**
     * Create a rule for complex numbers that move around on vectors like a
     * lattice gas.
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
    public MovingFractal(boolean minimalOrLazyInitialization)
    {
        super(minimalOrLazyInitialization);

        // revise the text to make this mac friendly
        if(CAConstants.MAC_OS)
        {
            // only use this revised string on systems that are NOT
            // guaranteed to have a right-clicking mouse
            rightClick = "<p><b>Ctrl-click</b> the grid to draw walls.";
        }
        BEST_RESULTS += rightClick + "</body></html>";

        if(!minimalOrLazyInitialization)
        {
            fonts = new Fonts();

            // the number of neighbors for each cell
            // NOTE: this means that this rule won't work with lattices that
            // have a
            // variable or unknown number of neighbors
            numberOfNeighbors = TwoDimensionalLattice
                .getNumberOfNeighbors(CurrentProperties.getInstance()
                    .getLatticeDisplayName());

            // The size of each vector at each cell. It is + 2, so that we have
            // a rest state and a site indicating the presence (or not) of a
            // wall.
            sizeOfVector = numberOfNeighbors + 2;

            // decide if it is a one-dimensional or two-dimensional lattice
            isOneDimensional = OneDimensionalLattice.isCurrentLatticeOneDim();
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
    }

    /**
     * Handles reorientation of complex values (particles) for the "lattice
     * gas". Acts as if there are two forces. One force reorients the real
     * components of the complex numbers to migrate towards the left (if a small
     * real number) or right (if a big real number). The other force reorients
     * the imaginary components of the complex numbers to migrate towards the
     * top (if a large imaginary number) or bottom (if a small imaginary
     * number).
     * 
     * @param cell
     *            The cell at the center of the neighbors.
     * @param neighbors
     *            The neighboring cells. By convention the neighbors should be
     *            indexed clockwise starting to the northwest of the cell.
     * @return The array of values after the reorientation (application of the
     *         forces).
     */
    private Complex[] applyForces(Complex[] cell, Complex[][] neighbors)
    {
        // the reoriented values
        Complex[] afterReorientation = new Complex[sizeOfVector];

        // initialize the new array
        for(int i = 0; i < cell.length; i++)
        {
            afterReorientation[i] = new Complex(cell[i]);
        }

        int applyWhichForce = random.nextInt(4);
        if(applyWhichForce == 0)
        {
            // apply force for smallest real component
            afterReorientation = applySmallRealForce(afterReorientation,
                neighbors);
        }
        else if(applyWhichForce == 1)
        {
            // apply force for largest real component
            afterReorientation = applyBigRealForce(afterReorientation,
                neighbors);
        }
        else if(applyWhichForce == 2)
        {
            // apply force for smallest imaginary component
            afterReorientation = applySmallImaginaryForce(afterReorientation,
                neighbors);
        }
        else
        {
            // apply force for largest imaginary component
            afterReorientation = applyBigImaginaryForce(afterReorientation,
                neighbors);
        }

        return afterReorientation;
    }

    /**
     * Apply a force that moves the largest imaginary value upwards.
     * 
     * @param vector
     *            The vectors being reoriented.
     * @return The new vector with the largest imaginary value reoriented
     *         upwards.
     */
    private Complex[] applyBigImaginaryForce(Complex[] vector,
        Complex[][] neighbors)
    {
        // find the position of the biggest imaginary component
        // Note the -1 because we don't want to move the site indicating the
        // presence or lack of a wall.
        int biggestImagPosition = 0;
        for(int i = 0; i < vector.length - 1; i++)
        {
            if(vector[i].imaginary > vector[biggestImagPosition].imaginary)
            {
                biggestImagPosition = i;
            }
        }

        // The position to which I will reorient the values. (This formula
        // works for nearest neighbor 2D lattices only.) And yes, I do want
        // integer division!
        int positionToTheTop = (numberOfNeighbors - 1) / 4;

        // get the neighbor's average value in that direction
        Complex neighbor = getVectorAverageWithoutWall(neighbors[positionToTheTop]);

        // now reorient to the top, but only if the neighbor's value is
        // smaller
        if(vector[biggestImagPosition].imaginary > neighbor.imaginary)
        {
            // now reorient to the top. i.e., swap with the position that
            // points to the top.
            Complex temp4 = vector[positionToTheTop];
            vector[positionToTheTop] = vector[biggestImagPosition];
            vector[biggestImagPosition] = temp4;
        }
        else
        {
            // stay put -- reorient to the rest position
            Complex temp = vector[vector.length - 2];
            vector[vector.length - 2] = vector[biggestImagPosition];
            vector[biggestImagPosition] = temp;
        }

        return vector;
    }

    /**
     * Apply a force that moves the largest real value to the right.
     * 
     * @param vector
     *            The vectors being reoriented.
     * @return The new vector with the largest real value reoriented to the
     *         right.
     */
    private Complex[] applyBigRealForce(Complex[] vector, Complex[][] neighbors)
    {
        // find the position of the biggest real component
        // Note the -1 because we don't want to move the site indicating the
        // presence or lack of a wall.
        int biggestPosition = 0;
        for(int i = 0; i < vector.length - 1; i++)
        {
            if(vector[i].real > vector[biggestPosition].real)
            {
                biggestPosition = i;
            }
        }

        // The position to which I will reorient the values. (This formula
        // works for nearest neighbor 2D lattices only.) And yes, I do want
        // integer division!
        int positionToTheRight = (numberOfNeighbors - 1) / 2;

        // get the neighbor's average value in that direction
        Complex neighbor = getVectorAverageWithoutWall(neighbors[positionToTheRight]);

        // now reorient to the right, but only if the neighbor's value is
        // smaller
        if(vector[biggestPosition].real > neighbor.real)
        {
            // reorient the biggest real value to point to the right. i.e.,
            // swap with the position that points to the right.
            Complex temp2 = vector[positionToTheRight];
            vector[positionToTheRight] = vector[biggestPosition];
            vector[biggestPosition] = temp2;
        }
        else
        {
            // stay put -- reorient to the rest position
            Complex temp = vector[vector.length - 2];
            vector[vector.length - 2] = vector[biggestPosition];
            vector[biggestPosition] = temp;
        }

        return vector;
    }

    /**
     * Apply a force that moves the smallest imaginary value downwards.
     * 
     * @param vector
     *            The vectors being reoriented.
     * @return The new vector with the smallest imaginary value reoriented
     *         downwards.
     */
    private Complex[] applySmallImaginaryForce(Complex[] vector,
        Complex[][] neighbors)
    {
        // find the position of the smallest imaginary component
        // Note the -1 because we don't want to move the site indicating the
        // presence or lack of a wall.
        int smallestImagPosition = 0;
        for(int i = 0; i < vector.length - 1; i++)
        {
            if(vector[i].imaginary < vector[smallestImagPosition].imaginary)
            {
                smallestImagPosition = i;
            }
        }

        // The position to which I will reorient the values. (This formula
        // works for nearest neighbor 2D lattices only.) And yes, I do want
        // integer division!
        int positionToTheBottom = (3 * (numberOfNeighbors - 1)) / 4;

        // get the neighbor's average value in that direction
        Complex neighbor = getVectorAverageWithoutWall(neighbors[positionToTheBottom]);

        // now reorient to the bottom, but only if the neighbor's value is
        // bigger
        if(vector[smallestImagPosition].imaginary < neighbor.imaginary)
        {
            // reorient to the bottom. i.e., swap with the position that
            // points to the bottom
            Complex temp3 = vector[positionToTheBottom];
            vector[positionToTheBottom] = vector[smallestImagPosition];
            vector[smallestImagPosition] = temp3;
        }
        else
        {
            // stay put -- reorient to the rest position
            Complex temp = vector[vector.length - 2];
            vector[vector.length - 2] = vector[smallestImagPosition];
            vector[smallestImagPosition] = temp;
        }

        return vector;
    }

    /**
     * Apply a force that moves the smallest real value to the left.
     * 
     * @param vector
     *            The vectors being reoriented.
     * @return The new vector with the smallest real value reoriented to the
     *         left.
     */
    private Complex[] applySmallRealForce(Complex[] vector,
        Complex[][] neighbors)
    {
        // find the position of the smallest real component
        // Note the -1 because we don't want to move the site indicating the
        // presence or lack of a wall.
        int smallestPosition = 0;
        for(int i = 0; i < vector.length - 1; i++)
        {
            if(vector[i].real < vector[smallestPosition].real)
            {
                smallestPosition = i;
            }
        }

        // The position to which I will reorient the values. (This formula
        // works for nearest neighbor 2D lattices only.)
        int positionToTheLeft = numberOfNeighbors - 1;

        // get the neighbor's average value in that direction
        Complex neighbor = getVectorAverageWithoutWall(neighbors[positionToTheLeft]);

        // now reorient to the left, but only if the neighbor's value is
        // bigger
        if(vector[smallestPosition].real < neighbor.real)
        {
            // reorient to the left. i.e., swap with the position that
            // points to the left.
            Complex temp = vector[positionToTheLeft];
            vector[positionToTheLeft] = vector[smallestPosition];
            vector[smallestPosition] = temp;
        }
        else
        {
            // stay put -- reorient to the rest position
            Complex temp = vector[vector.length - 2];
            vector[vector.length - 2] = vector[smallestPosition];
            vector[smallestPosition] = temp;
        }

        return vector;
    }

    /**
     * Reorients particles by bouncing them back from whence they came (when the
     * site is a wall).
     * 
     * @param complexArray
     *            The vector of complex numbers at the cell.
     * @return The vector after reorienting for bounce back.
     */
    private Complex[] bounceBack(Complex[] complexArray)
    {
        Complex[] bounceBack = new Complex[complexArray.length];

        // reorient all except the stationary site and the wall site
        for(int i = 0; i < complexArray.length - 2; i++)
        {
            bounceBack[i] = complexArray[(i + (numberOfNeighbors / 2))
                % numberOfNeighbors];
        }

        // now get the rest site
        bounceBack[complexArray.length - 2] = complexArray[complexArray.length - 2];

        // now get the wall site
        bounceBack[complexArray.length - 1] = complexArray[complexArray.length - 1];

        return bounceBack;
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

        // label for the description
        JPanel functionDescriptionLabel = createDescriptionPanel();

        // panel holding radio buttons for choices
        JPanel viewRadioPanel = createViewRadioButtons();

        // description
        int row = 0;
        innerPanel.add(functionDescriptionLabel, new GBC(1, row).setSpan(10, 1)
            .setFill(GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(1));

        // view choices radio buttons
        row++;
        innerPanel.add(viewRadioPanel, new GBC(5, row).setSpan(5, 1).setFill(
            GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

        // expands to fill extra space
        row++;
        innerPanel.add(new JLabel(" "), new GBC(1, row).setSpan(5, 1).setFill(
            GBC.BOTH).setWeight(10.0, 10.0).setAnchor(GBC.WEST).setInsets(1));

        return innerPanel;
    }

    /**
     * Creates a panel that displays a message about the views that can be
     * selected.
     * 
     * @return A panel containing messages.
     */
    private JPanel createDescriptionPanel()
    {
        // a "grab their attention" panel
        AttentionPanel attentionPanel = new AttentionPanel("Moving Fractal CA");

        String functionDescription = "Each cell is a vector of complex numbers, one complex "
            + "number pointing at each neighbor, and another number pointing (or sitting) "
            + "at the cell itself.  "
            + "The values are averaged together to give a single average complex number.\n\n"
            + "The average value may be viewed by taking the modulus of the complex number. "
            + "This gives the distance that the value "
            + "falls from the origin. Farther distances are shaded more "
            + "darkly (or as the occupied color if colors have been reset "
            + "by the user) with the darkest color at a distance of approximately "
            + Math.round(getFullState()[0].modulus())
            + ".\n\n"
            + "Or the complex number can be viewed as a particular fractal or "
            + "Julia set. In this case, the complex number is shaded according "
            + "to how quickly it diverges in an equation such as z = z^2 +c (the "
            + "equation for the Mandelbrot set). The darkest colors never diverge, "
            + "and lighter colors quickly diverge.";

        MultilineLabel messageLabel = new MultilineLabel(functionDescription);
        messageLabel.setFont(fonts.getMorePropertiesDescriptionFont());
        messageLabel.setMargin(new Insets(2, 6, 2, 2));

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(attentionPanel, BorderLayout.NORTH);
        messagePanel.add(messageLabel, BorderLayout.CENTER);

        return messagePanel;
    }

    /**
     * Create a JPanel holding the radio buttons for choosing a view.
     * 
     * @return A panel holding the radio buttons.
     */
    private JPanel createViewRadioButtons()
    {
        modulusButton = new JRadioButton("Modulus, |z|");
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
        mandelbrotButton.setSelected(true);

        juliaButton = new JRadioButton("Julia Set");
        juliaButton
            .setToolTipText("Displays as the time it takes z to diverge "
                + "in z = z^2 + c where c is a fixed value.");
        juliaButton.setFont(fonts.getPlainFont());
        juliaButton.addItemListener(new ViewChoiceListener());
        juliaButton.setSelected(false);

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
            0, 10, 0, 0));

        row++;
        radioPanel.add(modulusButton, new GBC(3, row).setSpan(3, 1).setFill(
            GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(
            0, 30, 0, 0));
        radioPanel.add(mandelbrotButton, new GBC(6, row).setSpan(3, 1).setFill(
            GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(1));
        radioPanel.add(juliaButton, new GBC(9, row).setSpan(3, 1).setFill(
            GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(1));

        row++;
        radioPanel.add(new JLabel(" "), new GBC(3, row).setSpan(3, 1).setFill(
            GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(0));

        return radioPanel;
    }

    /**
     * Returns a random Complex vector state.
     * 
     * @param probability
     *            The probability that a value is present in each of the
     *            vector's positions. When a position is present, it is assigned
     *            the corresponding position of the full state. When a position
     *            is not present, it is assigned the corresponding position of
     *            the empty state.
     * @return a random vector state.
     */
    private Complex[] getRandomBiValueState(double probability)
    {
        Complex[] state = new Complex[sizeOfVector];

        for(int i = 0; i < sizeOfVector - 1; i++)
        {
            if(random.nextDouble() < probability)
            {
                state[i] = new Complex(getFullState()[i]);
            }
            else
            {
                state[i] = new Complex(getEmptyState()[i]);
            }
        }

        // the wall position
        state[sizeOfVector - 1] = new Complex(0.0, 0.0);

        return state;
    }

    /**
     * Returns a random Complex vector state.
     * 
     * @param probability
     *            The probability that a value is present in each of the
     *            vector's positions. When a position is not present, it is
     *            assigned the complex value 0.0+0.0i.
     * @return a random vector state.
     */
    private Complex[] getRandomState(double probability)
    {
        Complex[] state = new Complex[sizeOfVector];

        double maxRandom = getFullState()[0].real;
        double minRandom = -getFullState()[0].real;

        for(int i = 0; i < sizeOfVector - 1; i++)
        {
            if(random.nextDouble() < probability)
            {
                // now get a random real component between minRandom and
                // maxRandom
                double real = random.nextDouble() * (maxRandom - minRandom)
                    + minRandom;

                // now get a random imaginary component between minRandom and
                // maxRandom
                double imaginary = random.nextDouble()
                    * (maxRandom - minRandom) + minRandom;

                state[i] = new Complex(real, imaginary);
            }
            else
            {
                // make this part of the vector 0.0+0.0i
                state[i] = new Complex(getEmptyState()[0]);
            }
        }

        // set the wall site
        state[sizeOfVector - 1] = new Complex(getEmptyState()[0]);

        return state;
    }

    /**
     * Get the average of the complex numbers in the array. In other words, get
     * the average of the real and imaginary components.
     * 
     * @param complexArray
     *            The array from which the average will be calculated.
     * @return The average complex number.
     */
    private Complex getVectorAverage(Complex[] complexArray)
    {
        // add the elements of the array
        Complex averageValue = new Complex(0.0, 0.0);
        for(int i = 0; i < complexArray.length; i++)
        {
            averageValue = Complex.plus(averageValue, complexArray[i]);
        }

        // now divide by the length of the array to get the average value
        averageValue = Complex.divide(averageValue, new Complex(
            complexArray.length, 0.0));

        return averageValue;
    }

    /**
     * Get the average of the complex numbers in the array, not including the
     * last element which is a wall. In other words, get the average of the real
     * and imaginary components.
     * 
     * @param complexArray
     *            The array from which the average will be calculated.
     * @return The average complex number.
     */
    private Complex getVectorAverageWithoutWall(Complex[] complexArray)
    {
        // add the elements of the array
        Complex averageValue = new Complex(0.0, 0.0);
        for(int i = 0; i < complexArray.length - 1; i++)
        {
            averageValue = Complex.plus(averageValue, complexArray[i]);
        }

        // now divide by the length of the array to get the average value
        averageValue = Complex.divide(averageValue, new Complex(
            complexArray.length - 1, 0.0));

        return averageValue;
    }

    /**
     * If the user selected "random" on the rule menu, then the last element of
     * the array may have been assigned a value besides 0.0+0.0i or WALL_VALUE.
     * This repairs that, and assigns 0.0+0.0i if it is not already 0.0+0.0i or
     * WALL_VALUE.
     * 
     * @param cell
     *            The vector of complex numbers at this cell.
     * @return An array with the correct value in the last position.
     */
    private Complex[] repairWall(Complex[] cell)
    {
        Complex[] repairedCell = new Complex[cell.length];
        for(int i = 0; i < repairedCell.length; i++)
        {
            // make a copy
            repairedCell[i] = new Complex(cell[i]);
        }

        // if not empty and not the WALL_VALUE, then make empty
        if(!repairedCell[repairedCell.length - 1]
            .equals(getEmptyState()[repairedCell.length - 1])
            && !repairedCell[repairedCell.length - 1].equals(WALL_VALUE))
        {
            repairedCell[repairedCell.length - 1] = getEmptyState()[repairedCell.length - 1];
        }

        return repairedCell;
    }

    /**
     * Reorients the particles by applying forces and "bouncing back" if the
     * cell is a wall.
     * 
     * @param cell
     *            The cell at the center of the neighbors.
     * @param neighbors
     *            The neighboring cells. By convention the neighbors should be
     *            indexed clockwise starting to the northwest of the cell.
     * @return The array of values after the reorientation.
     */
    private Complex[] reorient(Complex[] cell, Complex[][] neighbors)
    {
        // will store values after being reoriented
        Complex[] reorientedValues = null;

        // if a wall, then bounce back, otherwise apply forces
        if(cell[sizeOfVector - 1].equals(WALL_VALUE))
        {
            reorientedValues = bounceBack(cell);
        }
        else
        {
            // reorient values so that they point toward the direction their
            // complex value should be in the complex plane
            reorientedValues = applyForces(cell, neighbors);
        }

        return reorientedValues;
    }

    /**
     * Create an initial state based on the cell's position. It it is on the
     * edge, then it is a wall. Otherwise it is random.
     * 
     * @param percent
     *            The percent of the vector elements that will be occupied.
     * @return A cell state.
     */
    private Complex[] setInitialStateForRandomWithWall(double percent)
    {
        // find our current position
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

        // get the row and column
        int row = cellNum / numRows; // integer division on purpose!
        int col = cellNum % numCols;

        // create the vector of complex values
        Complex[] initState = new Complex[sizeOfVector];

        // check if it is a wall (on the edge of the simulation)
        if(isOneDimensional && ((col == 0) || (col == numCols - 1)))
        {
            initState = getAlternateState();
        }
        else if(!isOneDimensional
            && ((row == 0) || (col == 0) || (row == numRows - 1) || (col == numCols - 1)))
        {
            initState = getAlternateState();
        }
        else
        {
            initState = getRandomState(percent);
        }

        cellNum++;

        return initState;
    }

    /**
     * Handles translation of complex values (particles) for the "lattice gas".
     * 
     * @param cell
     *            The cell at the center of the neighbors.
     * @param neighbors
     *            The neighboring cells. By convention the neighbors should be
     *            indexed clockwise starting to the northwest of the cell.
     * @return The array of values after the translation.
     */
    private Complex[] translate(Complex[] cell, Complex[][] neighbors)
    {
        // the translated values
        Complex[] afterTranslation = new Complex[sizeOfVector];

        // get neighboring states
        for(int i = 0; i < neighbors.length; i++)
        {
            // note integer division!
            int whichValueOfNeighborPointsAtMe = (i + (numberOfNeighbors / 2))
                % numberOfNeighbors;

            // will sit at this cell, but will point in the direction of
            // whichValueOfNeighborPointsAtMe
            afterTranslation[whichValueOfNeighborPointsAtMe] = neighbors[i][whichValueOfNeighborPointsAtMe];
        }

        // add in the rest state
        afterTranslation[numberOfNeighbors] = cell[numberOfNeighbors];

        // add in the wall (if it is there)
        afterTranslation[numberOfNeighbors + 1] = cell[numberOfNeighbors + 1];

        return afterTranslation;
    }

    /**
     * Rule for complex numbers that move around on vectors like a lattice gas.
     * The complex values move in two steps. (1) They orient themselves (by
     * rearranging their position on the vector) to point in the direction of
     * the neighboring cell that has an average closest to their own value. For
     * example, if an element of the cell had value 199, and the neighboring
     * values averaged 2, 4, 300, and 500, then the 199 would orient to point
     * towards the 300. (2) Then they translate to the cell towards which they
     * are pointing.
     * <p>
     * Actually, this happens in reverse order, but the idea is the same.
     * 
     * @param cell
     *            The value of the cell being updated.
     * @param neighbors
     *            The value of the neighbors.
     * @param generation
     *            The current generation of the CA.
     * @return A new state for the cell.
     */
    protected Complex[] complexVectorRule(Complex[] cell,
        Complex[][] neighbors, int generation)
    {
        // Repair the value stored at the last element. Should be 0.0+0.0i or
        // should be a WALL_VALUE. This fixes that. Will be set incorrectly if
        // the user selected random on the rule menu.
        Complex[] repairedCell = repairWall(cell);

        // move the values from neighboring cell node to this cell node
        Complex[] translatedValues = translate(repairedCell, neighbors);

        // reorient the values according to forces, etc.
        Complex[] reorientedValues = reorient(translatedValues, neighbors);

        return reorientedValues;
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
     * is drawn with a right mouse click). If no alternate state is desired,
     * recommended to return getEmptyState()). Implementations should be careful
     * to return a new instance each time this is called. Otherwise, multiple CA
     * cells may be sharing the same instance and a change to one cell could
     * change many cells.
     * 
     * @return The alternate state.
     */
    public Complex[] getAlternateState()
    {
        Complex[] alternateState = new Complex[sizeOfVector];

        // assign the alternate state
        for(int i = 0; i < sizeOfVector - 1; i++)
        {
            alternateState[i] = DEFAULT_EMPTY_STATE;
        }

        // make the alternate state a wall
        alternateState[sizeOfVector - 1] = WALL_VALUE;

        return alternateState;
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
     * A list of lattices with which this Rule will work; in this case, returns
     * all lattices by default, though child classes may wish to override this
     * and restrict the lattices with which the child rule will work. <br>
     * Well-designed Rules should work with any lattice, but some may require
     * particular topological or geometrical information (like the lattice gas).
     * Appropriate strings to return in the array include
     * SquareLattice.DISPLAY_NAME, HexagonalLattice.DISPLAY_NAME,
     * StandardOneDimensionalLattice.DISPLAY_NAME, etc. If null, will be
     * compatible with all lattices.
     * 
     * @return A list of lattices compatible with this Rule (returns the display
     *         names for the lattices). Returns null if compatible with all
     *         lattices.
     */
    public String[] getCompatibleLattices()
    {
        String[] lattices = {SquareLattice.DISPLAY_NAME,
            HexagonalLattice.DISPLAY_NAME,
            StandardOneDimensionalLattice.DISPLAY_NAME,
            TriangularLattice.DISPLAY_NAME,
            FourNeighborSquareLattice.DISPLAY_NAME};

        return lattices;
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
        String[] folders = {RuleFolderNames.COMPUTATIONALLY_INTENSIVE_FOLDER};

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
    public Complex[] getEmptyState()
    {
        Complex[] emptyState = new Complex[sizeOfVector];

        // assign the empty state
        for(int i = 0; i < sizeOfVector; i++)
        {
            emptyState[i] = DEFAULT_EMPTY_STATE;
        }

        return emptyState;
    }

    /**
     * Gets a complex number that represents the full or filled state.
     * Implementations should be careful to return a new instance each time this
     * is called. Otherwise, multiple CA cells may be sharing the same instance
     * and a change to one cell could change many cells.
     * 
     * @return The full state.
     */
    public Complex[] getFullState()
    {
        Complex[] fullState = new Complex[sizeOfVector];

        // assign the full state
        for(int i = 0; i < sizeOfVector - 1; i++)
        {
            fullState[i] = DEFAULT_FULL_STATE;
        }

        // make the last site empty, so it isn't a wall
        fullState[sizeOfVector - 1] = DEFAULT_EMPTY_STATE;

        return fullState;
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
     * This method should be used in conjuction with the setInitialState method,
     * also in this class.
     * 
     * @return An array of names for initial states that are specified by the
     *         rule.
     */
    public String[] getInitialStateNames()
    {
        String[] initStateNames = {RANDOM_WITH_WALL};

        return initStateNames;
    }

    /**
     * Gets tool tips for any initial states defined by the rule. The tool tips
     * should be given in the same order as the initial state names in the
     * method getInitialStateNames. The tool tip array must be null or the same
     * length as the array of initial state names. By default this returns null.
     * Child classes should override the method if they wish to specify initial
     * state tool tips that will appear on the Properties panel. Note: This
     * method should be used in conjuction with the getInitialStateNames and
     * setInitialState method, also in this class.
     * 
     * @return An array of tool tips for initial states that are specified by
     *         the rule. May be null. Any element of the array may also be null,
     *         but if the length of the array is non-zero, then the length must
     *         be the same as the array returned by getInitialStateNames.
     */
    public String[] getInitialStateToolTips()
    {
        String[] initStateToolTips = {RANDOM_WITH_WALL_TOOLTIP};

        return initStateToolTips;
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
     * Gets the length of the vectors (arrays) that will be used by the Rule.
     * The length must be the same for all cells.
     * 
     * @return The length of the vector stored by each cell.
     */
    public int getVectorLength()
    {
        return sizeOfVector;
    }

    /**
     * Gets an initial state that corresponds to the specified initialStateName.
     * This method is optional and returns null by default. If a rule wishes to
     * define one or more initial states (different from the "single seed",
     * "random", and "blank" that are already provided), then the name of the
     * state is specified in the getInitialStateNames() method. The name is
     * displayed on the Properties panel, and if it is selected, then this
     * method is called with that name. Based on the name, this method can then
     * specify an initial state. By default this returns null. Child classes
     * should override the method if they wish to specify initial states that
     * will appear on the Properties panel. The lattice parameter is used to
     * retrieve an iterator over the cells and assign the initial values to the
     * cells. An example is <code>
     *   //assigns the same double value to each cell.
     *   Iterator cellIterator = lattice.iterator();
     *   while(cellIterator.hasNext())
     *   {
     *       Cell cell = (Cell) cellIterator.next();
     *
     *       // assign the value
     *       cell.getState().setValue(new Double(3.4));
     *   }
     * </code>
     * Here is another example that assigns values to the cell at both the
     * current generation and previous generations. The number of generations in
     * the stateHistory variable (below) would be determined by the method
     * getRequiredNumberOfGenerations in this class. <code>
     *   //assign the same double value to each cell
     *   Double double = new Double(9.3);
     *   Iterator cellIterator = lattice.iterator();
     *   while(cellIterator.hasNext())
     *   {
     *       Cell cell = (Cell) cellIterator.next();
     *
     *       // get the list of states for each cell
     *       ArrayList stateHistory = cell.getStateHistory();
     *       int historySize = stateHistory.size();
     *
     *       // there may be more than one state required as initial conditions
     *       for(int i = 0; i < historySize; i++)
     *       {
     *            ((CellState) stateHistory.get(i)).setValue(double);
     *       }
     *  }
     * </code>
     * Another example is given in the Fractal rule. That example figures out
     * the row and column number of each cell and assigns values based on their
     * row and column. Note: This method should be used in conjuction with the
     * getInitialState method, also in this class. By default this rule does
     * nothing.
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
        // the percent of vector elements to be occupied
        double percent = 1.0;

        // assigns a random value to each cell.
        Iterator cellIterator = lattice.iterator();
        while(cellIterator.hasNext())
        {
            Cell cell = (Cell) cellIterator.next();

            // assign the value
            if(initialStateName.equals(RANDOM_WITH_WALL))
            {
                cell.getState().setValue(
                    setInitialStateForRandomWithWall(percent));
            }
            else
            {
                cell.getState().setValue(getRandomState(percent));
            }
        }
    }

    // -----------------------------------------------------------------------

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
            Complex[] cellVector = (Complex[]) state.getValue();

            // if it is a wall, then display as black
            if(cellVector[cellVector.length - 1].equals(WALL_VALUE))
            {
                // return black
                return new Color(0, 0, 0);
            }

            // otherwise just get rid of the wall component and continue
            Complex[] cellVectorWithoutWall = new Complex[cellVector.length - 1];
            for(int i = 0; i < cellVectorWithoutWall.length; i++)
            {
                cellVectorWithoutWall[i] = cellVector[i];
            }

            // get the average of the values at the cell (not including the
            // wall)
            Complex averageCellValue = getVectorAverage(cellVectorWithoutWall);
            // System.out.println("MovingFractal: cellValue = "+cellValue);

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
                Complex c = new Complex(averageCellValue.getReal(),
                    averageCellValue.getImaginary());
                Complex z = new Complex(averageCellValue.getReal(),
                    averageCellValue.getImaginary());
                z = Complex.plus(Complex.multiply(z, z), c);

                // Now iterate until diverges. the variable iterate holds the
                // number
                // of times before diverged. this is used to plot a shade of
                // color.
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
                Complex z = new Complex(averageCellValue.getReal(),
                    averageCellValue.getImaginary());
                z = Complex.plus(Complex.multiply(z, z), juliaConstant);

                // Now iterate until diverges. the variable iterate holds the
                // number
                // of times before diverged. this is used to plot a shade of
                // color.
                while(z.modulus() < 1.75 && iteration < maxIteration)
                {
                    z = Complex.plus(Complex.multiply(z, z), juliaConstant);

                    iteration = iteration + 1;
                }
            }
            else
            {
                return new ComplexVectorDefaultView(getEmptyState()[0]
                    .modulus(), getFullState()[0].modulus()).getColor(state,
                    null, rowAndCol);
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

            // just to be safe
            if(red > 255)
            {
                red = 255;
            }
            if(green > 255)
            {
                green = 255;
            }
            if(blue > 255)
            {
                blue = 255;
            }
            if(red < 0)
            {
                red = 0;
            }
            if(green < 0)
            {
                green = 0;
            }
            if(blue < 0)
            {
                blue = 0;
            }

            return new Color(red, green, blue);
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

            // now let the graphics know that they need to update!
            refreshGraphics();
        }
    }
}
