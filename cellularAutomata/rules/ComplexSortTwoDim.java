/*
 ComplexSortTwoDim -- a class within the Cellular Automaton Explorer. 
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

import java.awt.Color;
import java.util.Random;

import cellularAutomata.CurrentProperties;
import cellularAutomata.util.Coordinate;
import cellularAutomata.util.math.Complex;
import cellularAutomata.util.math.RandomSingleton;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.TriangleHexagonCellStateView;
import cellularAutomata.graphics.colors.ColorScheme;
import cellularAutomata.rules.templates.ComplexMargolusTemplate;
import cellularAutomata.rules.util.RuleFolderNames;

/**
 * Sorts complex numbers in two-dimensional space.
 * 
 * @author David Bahr
 */
public class ComplexSortTwoDim extends ComplexMargolusTemplate
{
    // a display name for this class
    private static final String RULE_NAME = "Sort Complex (Fractal Rain)";

    // a description of property choices that give the best results for this
    // rule (e.g., which lattice, how many states, etc.)
    private static final String BEST_RESULTS = "<html> <body><b>" + RULE_NAME
        + ".</b>" + "<p> "
        + "<b>For best results</b>, choose a 99% random initial state "
        + "on a 200 by 200 lattice. Large lattices work best. The fire "
        + "color scheme looks particularly nice." + leftClickInstructions
        + rightClickInstructions + "</body></html>";

    // a tooltip description for this class
    private String TOOLTIP = "<html> <body><b>" + RULE_NAME
        + ".</b> Sorts complex numbers in 2d using a parallel "
        + "sorting algorithm." + "</body></html>";

    /**
     * Create the sorting rule using the given cellular automaton properties.
     * <p>
     * When calling the parent constructor, the minimalOrLazyInitialization
     * parameter must be included as shown. The boolean is intended to indicate
     * when the constructor should build a rule with as small a footprint as
     * possible. In order to load rules by reflection, the application must
     * query this class for information like the display name, tooltip
     * description, etc. At these times it makes no sense to build the complete
     * rule which may have a large footprint in memory.
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
    public ComplexSortTwoDim(boolean minimalOrLazyInitialization)
    {
        super(minimalOrLazyInitialization);
    }

    /**
     * Takes the numbers at each quadrant of the Margolus block and rearranges
     * them in proper order.
     * 
     * @param northWestCellValue
     *            The current value of the northwest cell.
     * @param northEastCellValue
     *            The current value of the northeast cell.
     * @param southEastCellValue
     *            The current value of the southeast cell.
     * @param southWestCellValue
     *            The current value of the southwest cell.
     * @param northwestCellsFixedRowPosition
     *            The indexed row position of the northwest cell (0 to
     *            numRows-1).
     * @param northwestCellsFixedColumnPosition
     *            The indexed column position of the northwest cell (0 to
     *            numColumns-1).
     * @param generation
     *            The current generation of the cellular automaton.
     * @return An array of values representing the randomly rearranged
     *         particles.
     */
    private Complex[] rearrangeTheBlock(Complex northWestCellValue,
        Complex northEastCellValue, Complex southEastCellValue,
        Complex southWestCellValue, int northwestCellsFixedRowPosition,
        int northwestCellsFixedColumnPosition, int generation)
    {
        Complex[] newBlock = new Complex[4];

        // default arrangement
        newBlock[0] = northWestCellValue;
        newBlock[1] = northEastCellValue;
        newBlock[2] = southEastCellValue;
        newBlock[3] = southWestCellValue;

        int numRows = CurrentProperties.getInstance().getNumRows();
        int numCols = CurrentProperties.getInstance().getNumColumns();

        // first, sort vertically (imaginary numbers)
        if(generation < numCols)
        {
            // No up-down movement if against top or bottom edge. Otherwise,
            // start swapping elements in the block. (Need no movement against
            // the edges so that the sorting has a place to start.)
            if(northwestCellsFixedRowPosition != CurrentProperties
                .getInstance().getNumRows() - 1)
            {
                // move up if can
                if(newBlock[3].imaginary < newBlock[0].imaginary)
                {
                    // swap places -- move from southwest to northwest
                    Complex temp = newBlock[0];
                    newBlock[0] = newBlock[3];
                    newBlock[3] = temp;
                }
                if(newBlock[2].imaginary < newBlock[1].imaginary)
                {
                    // swap places -- move from southeast to northeast
                    Complex temp = newBlock[1];
                    newBlock[1] = newBlock[2];
                    newBlock[2] = temp;
                }
            }
        }
        // second, sort horizontally (real numbers)
        else if(generation < numCols + numRows)
        {
            // No left-right movement if against left or right edge. Otherwise,
            // start swapping elements in the block. (Need no movement against
            // the edges so that the sorting has a place to start.)
            if(northwestCellsFixedColumnPosition != CurrentProperties
                .getInstance().getNumColumns() - 1)
            {
                // move left if can.
                if(newBlock[1].real < newBlock[0].real)
                {
                    // swap places -- move from northeast to northwest
                    Complex temp = newBlock[0];
                    newBlock[0] = newBlock[1];
                    newBlock[1] = temp;
                }
                if(newBlock[2].real < newBlock[3].real)
                {
                    // swap places -- move from southeast to southwest
                    Complex temp = newBlock[2];
                    newBlock[2] = newBlock[3];
                    newBlock[3] = temp;
                }
            }
        }
        // last, sort in all directions
        else
        {
            // No up-down movement if against top or bottom edge. Otherwise,
            // start swapping elements in the block. (Need no movement against
            // the edges so that the sorting has a place to start.)
            if(northwestCellsFixedRowPosition != CurrentProperties
                .getInstance().getNumRows() - 1)
            {
                // move up if can
                if(newBlock[3].imaginary < newBlock[0].imaginary)
                {
                    // swap places -- move from southwest to northwest
                    Complex temp = newBlock[0];
                    newBlock[0] = newBlock[3];
                    newBlock[3] = temp;
                }
                if(newBlock[2].imaginary < newBlock[1].imaginary)
                {
                    // swap places -- move from southeast to northeast
                    Complex temp = newBlock[1];
                    newBlock[1] = newBlock[2];
                    newBlock[2] = temp;
                }
            }

            // No left-right movement if against left or right edge. Otherwise,
            // start swapping elements in the block. (Need no movement against
            // the edges so that the sorting has a place to start.)
            if(northwestCellsFixedColumnPosition != CurrentProperties
                .getInstance().getNumColumns() - 1)
            {
                // move left if can.
                if(newBlock[1].real < newBlock[0].real)
                {
                    // swap places -- move from northeast to northwest
                    Complex temp = newBlock[0];
                    newBlock[0] = newBlock[1];
                    newBlock[1] = temp;
                }
                if(newBlock[2].real < newBlock[3].real)
                {
                    // swap places -- move from southeast to southwest
                    Complex temp = newBlock[2];
                    newBlock[2] = newBlock[3];
                    newBlock[3] = temp;
                }
            }

            // No diagonal movement if against either edge. Otherwise,
            // start swapping elements in the block. (Need no movement against
            // the edges so that the sorting has a place to start.)
            if((northwestCellsFixedColumnPosition != CurrentProperties
                .getInstance().getNumColumns() - 1)
                && (northwestCellsFixedRowPosition != CurrentProperties
                    .getInstance().getNumRows() - 1))
            {
                // move diagonal if can
                if(newBlock[2].real < newBlock[0].real
                    && newBlock[2].imaginary < newBlock[0].imaginary)
                {
                    // swap places -- move from southeast to northwest
                    Complex temp = newBlock[0];
                    newBlock[0] = newBlock[2];
                    newBlock[2] = temp;
                }

                // move other diagonal if can
                // this is correct >, <
                if(newBlock[3].real > newBlock[1].real
                    && newBlock[3].imaginary < newBlock[1].imaginary)
                {
                    // swap places -- move from southwest to northeast
                    Complex temp = newBlock[1];
                    newBlock[1] = newBlock[3];
                    newBlock[3] = temp;
                }
            }
        }

        return newBlock;
    }

    /**
     * A rule for sorting complex numbers in 2d. Takes the Margolus neighborhood
     * and rearranges each number to the correct position.
     * 
     * @param northWestCellValue
     *            The current value of the northwest cell.
     * @param northEastCellValue
     *            The current value of the northeast cell.
     * @param southEastCellValue
     *            The current value of the southeast cell.
     * @param southWestCellValue
     *            The current value of the southwest cell.
     * @param northwestCellsFixedRowPosition
     *            The indexed row position of the northwest cell (0 to
     *            numRows-1).
     * @param northwestCellsFixedColumnPosition
     *            The indexed column position of the northwest cell (0 to
     *            numColumns-1).
     * @param generation
     *            The current generation of the CA.
     * @return An array of states that corresponds to the 2 by 2 Margolus block.
     *         Array[0] is the northwest corner of the block, array[1] is the
     *         northeast corner of the block, array[2] is the southeast corner
     *         of the block, array[3] is the southwest corner of the block.
     */
    protected Complex[] blockRule(Complex northWestCellValue,
        Complex northEastCellValue, Complex southEastCellValue,
        Complex southWestCellValue, int northwestCellsFixedRowPosition,
        int northwestCellsFixedColumnPosition, int generation)
    {
        // take the original block values (particles) and rearrange them
        // randomly within the block. Conserves the values (particles).
        // i.e., pick one of the 24 random rearrangements. And then we assign
        // the rearrangement to an array representing the new Margolus block.
        return rearrangeTheBlock(northWestCellValue, northEastCellValue,
            southEastCellValue, southWestCellValue,
            northwestCellsFixedRowPosition, northwestCellsFixedColumnPosition,
            generation);
    }

    /**
     * Gets a complex number that represents the alternate state (a state that
     * is drawn with a right mouse click). In this case, the alternate state is
     * a random number between the full and empty states.
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
    public Complex getEmptyState()
    {
        return new Complex(-2.0, -1.5);
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
        return new Complex(0.9, 1.5);
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

            double iteration = 0;
            int maxIteration = 40;

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
}
