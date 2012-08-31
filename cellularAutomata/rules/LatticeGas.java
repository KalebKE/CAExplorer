/*
 LatticeGas -- a class within the Cellular Automaton Explorer. 
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

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import cellularAutomata.CAConstants;
import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.cellState.model.CellState;
import cellularAutomata.cellState.model.LatticeGasState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.cellState.view.LatticeGasCellStateView;
import cellularAutomata.lattice.HexagonalLattice;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.AttentionPanel;
import cellularAutomata.util.Fonts;
import cellularAutomata.util.GBC;
import cellularAutomata.util.MultilineLabel;
import cellularAutomata.util.math.RandomSingleton;

/**
 * Rules for a hexagonal lattice gas automaton following the basic FHP approach.
 * This implementation is designed for clarity and compatibility with the
 * Cellular Automaton Explorer Tool. No attempt has been made to optimize the
 * simulation for speed.
 * 
 * @author David Bahr
 */
public class LatticeGas extends Rule
{
    // the percentage of cells that are affected by brownian motion at any time
    // step. Should be very small to prevent unnecessary noise (e.g., 0.01), but
    // shouldn't be too small, or changes will take too long.
    private static double brownianPercent = 0.0;

    // Strength of gravity. Should be between 0 and 1, with smaller values
    // better
    private static double forceStrength = 0.2;

    // Direction of gravity in degrees (from x-axis).
    private static double forceTheta = 0.0;

    // selects the strength of the force
    private static JSpinner forceStrengthSpinner = null;

    // selects the direction of the force
    private static JSpinner forceDirectionSpinner = null;

    private static Random random = RandomSingleton.getInstance();

    // tool tip for the force strength spinner
    private static final String FORCE_STRENGTH_TIP = "Change the magnitude of the force.";

    // tool tip for the force direction spinner
    private static final String FORCE_DIRECTION_TIP = "Change the angular direction of "
        + "the force (e.g., 0 degrees = East, 90 degrees = North, 180 degrees = West, "
        + "270 degrees = South).";

    // a display name for this class
    private static final String RULE_NAME = "Lattice Gas (Fluid Flow)";

    // a description of property choices that give the best results for this
    // rule (e.g., which lattice, how many states, etc.)
    private static String BEST_RESULTS = "<html> <body><b>"
        + RULE_NAME
        + ".</b>"
        + "<p> "
        + "<b>For best results</b>, keep a running average of the "
        + "particle's vectors over time. Averages of 10 or more work "
        + "best. "
        + "<p>"
        + "Try a small grid (e.g., 50 X 50) with an 80% random "
        + "initial state. Draw some random shapes with a right click (or ctrl-click). The fluid "
        + "will flow around the shapes.  Try adding shapes while the simulation is running, like "
        + "diverting a flowing stream with big rocks."
        + "<p>"
        + "This simulation is computationally and graphically intensive and larger "
        + "simulations will run very slowly."
        + "<p>"
        + "The direction of gravity can be altered with the \"More Properties\" button. ";

    // a tooltip description for this class
    private String TOOLTIP = "<html> <body><b>" + RULE_NAME
        + ".</b> Models fluid flow around obstacles you draw.</body></html>";

    // The value of forceTheta at the previous time step.
    // Cannot be static, or the redistribute method will only act on *one* cell.
    private double previousTheta = 0.0;

    // fonts for display
    private Fonts fonts = null;

    // Increments to keep track of the number of times that the redistribute()
    // method has been run.
    private int count = 0;

    // The number of times that the redistribute() method will be run. Set in
    // the constructor.
    private int numberOfCells = 0;

    // a list of the collision rules
    private int[] collisionRule = new int[64];

    /**
     * Create a lattice gas rule using the given cellular automaton properties.
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
    public LatticeGas(boolean minimalOrLazyInitialization)
    {
        super(minimalOrLazyInitialization);

        // the lattice gas FHP rules
        createRuleTable();

        // revise the text to make this mac friendly
        String leftClick = "<p><b>Left-click</b> the grid to draw random vectors "
            + "(particles). ";
        String rightClick = "<p><b>Right-click</b> the grid to draw obstacles (walls) "
            + "around which the fluid will flow. ";
        if(CAConstants.MAC_OS)
        {
            // only use this revised string on systems that are NOT guaranteed
            // to have a right-clicking mouse
            leftClick = "<p><b>Click</b> the grid to draw random vectors "
                + "(particles). ";
            rightClick = "<p><b>Ctrl-click</b> the grid to draw obstacles (walls) "
                + "around which the fluid will flow. ";
        }
        BEST_RESULTS += leftClick + rightClick + "</body></html>";

        if(!minimalOrLazyInitialization)
        {
            fonts = new Fonts();
        }
    }

    /**
     * Applies gravity (or any body force) in the positive x-axis direction.
     * This works by moving particles to add an x-component without adding a
     * y-component.
     * 
     * @param beforeXGravity
     *            The cell's particles before gravity along the x-axis is
     *            applied.
     * @return The cell's particles after gravity along the x-axis has been
     *         applied.
     */
    private int[] applyForceAlongXAxis(int[] beforeXGravity)
    {
        // Once we have decided to apply gravity to a cell, there are
        // many ways to move the cell's particles to accomplish that
        // goal. Not all ways have the same effect, so each way must be
        // balanced.
        //
        // There are three ways to increase gravity along the x-axis direction
        // by moving single particles. Move a particle at position 5 to position
        // 2. Move a particle at position 0 to position 1. And move a particle
        // at position 4 to position 3. They each have different effects on the
        // change in force in the x-direction. E.g., moving from the far left
        // (position 5) to the far right (position 2) increases the force
        // twice as much as moving from position 0 to position 1 (or position 4
        // to position 3). If dX is the x-axis distance between nodes, then the
        // position 5 to position 2 increases the force by 2*dX. The other ways
        // increase the force by 1*dX
        //
        // This method assumes that each of the particle movements are possible
        // and gives them equal probabilities of occurring. In that case, the
        // average change in the force over three applications of gravity will
        // be (4/3)*dX. Therefore, to balance this, we only apply gravity 3/4 of
        // the time. That gives an average change in force along the x-axis of
        // dX. When applying gravity in the y direction, we will ensure that it
        // applies an average of dX as well.

        // For simplicity, we assume that all particle moves are
        // possible.

        // The following ensures that the average change in force is dX (the
        // x-axis distance between nodes).
        double prob = random.nextDouble();
        if(prob < 3.0 / 4.0)
        {
            // now find out which of the three possible particle moves to apply
            prob = random.nextDouble();

            if(prob < 1.0 / 3.0)
            {
                // do this 1/3rd of the time
                // increase the force by dX
                if((beforeXGravity[0] == 1) && (beforeXGravity[1] == 0))
                {
                    beforeXGravity[0] = 0;
                    beforeXGravity[1] = 1;
                }
            }
            else if((prob >= 1.0 / 3.0) && (prob < 2.0 / 3.0))
            {
                // do this 1/3rd of the time
                // increase the force by dX
                if((beforeXGravity[4] == 1) && (beforeXGravity[3] == 0))
                {
                    beforeXGravity[4] = 0;
                    beforeXGravity[3] = 1;
                }
            }
            else if((prob >= 2.0 / 3.0) && (prob < 3.0 / 3.0))
            {
                // do this 1/3rd of the time
                // increase the force by 2*dX
                if((beforeXGravity[5] == 1) && (beforeXGravity[2] == 0))
                {
                    beforeXGravity[5] = 0;
                    beforeXGravity[2] = 1;
                }
            }
        }

        // the force along the x-axis has now been applied (by moving a
        // particle)
        return beforeXGravity;
    }

    /**
     * Applies gravity (or any body force) in the positive y-axis direction.
     * This works by moving particles to add a y-component without adding an
     * x-component.
     * 
     * @param beforeYGravity
     *            The cell's particles before gravity along the y-axis is
     *            applied.
     * @param theta
     *            The direction of gravity (or other body force) in degrees.
     * @return The cell's particles after gravity along the y-axis has been
     *         applied.
     */
    private int[] applyForceAlongYAxis(int[] beforeYGravity, double theta)
    {
        // the angle of the applied body force, converted to radians
        double thetaRadians = theta * (Math.PI / 180.0);

        // Once we have decided to apply gravity to a cell, there are
        // many ways to move the cell's particles to accomplish that
        // goal. Not all ways have the same effect, so each way must be
        // balanced.
        //
        // There are two ways to increase gravity along the y-axis direction
        // by moving single particles. Move a particle at position 4 to position
        // 0. Move a particle at position 3 to position 1. If dX is the x-axis
        // distance between nodes, then these each add a change in force along
        // the y-axis of 2*sin(60)*dX.
        //
        // This method assumes that each of the particle movements are possible
        // and gives them equal probabilities of occurring. In that case, the
        // average change in the force for each applications of gravity will
        // be 2*sin(60)*dX. Therefore, to ensure that the application of x-axis
        // and y-axis gravity are the same, we will only apply the y-axis force
        // a percentage of the time given by 1/(2*sin(60)). This means that the
        // average change of force (per application) will be dX. This is the
        // same as the application of force along the x-axis.

        // For simplicity, we assume that all particle moves are
        // possible.

        // The following ensures that the average change in force is dX (the
        // x-axis distance between nodes).
        double prob = random.nextDouble();
        if(prob < Math.abs(1.0 / (2.0 * Math.sin(thetaRadians))))
        {
            // now find out which of the possible particle moves to apply
            if(random.nextBoolean())
            {
                // do this 1/2 of the time
                // increase the force by 2*sin(60)*dX
                if((beforeYGravity[3] == 1) && (beforeYGravity[1] == 0))
                {
                    beforeYGravity[3] = 0;
                    beforeYGravity[1] = 1;
                }
            }
            else
            {
                // do this 1/2 of the time
                // increase the force by 2*sin(60)*dX
                if((beforeYGravity[4] == 1) && (beforeYGravity[0] == 0))
                {
                    beforeYGravity[4] = 0;
                    beforeYGravity[0] = 1;
                }
            }
        }

        // the force along the y-axis has now been applied (by moving a
        // particle)
        return beforeYGravity;
    }

    /**
     * Applies gravity (or any body force) in the negative x-axis direction.
     * This works by moving particles to subtract an x-component without adding
     * a y-component.
     * 
     * @param beforeXGravity
     *            The cell's particles before gravity along the x-axis is
     *            applied.
     * @return The cell's particles after gravity along the x-axis has been
     *         applied.
     */
    private int[] applyNegativeForceAlongXAxis(int[] beforeXGravity)
    {
        // same as applyForceAlongXAxis, but particle directions are reversed
        // (for example position 2 to position 5, rather than position 5 to
        // position 2).

        // The following ensures that the average change in force is dX (the
        // x-axis distance between nodes).
        double prob = random.nextDouble();
        if(prob < 3.0 / 4.0)
        {
            // now find out which of the three possible particle moves to apply
            prob = random.nextDouble();

            if(prob < 1.0 / 3.0)
            {
                // do this 1/3rd of the time
                // increase the force by dX
                if((beforeXGravity[1] == 1) && (beforeXGravity[0] == 0))
                {
                    beforeXGravity[1] = 0;
                    beforeXGravity[0] = 1;
                }
            }
            else if((prob >= 1.0 / 3.0) && (prob < 2.0 / 3.0))
            {
                // do this 1/3rd of the time
                // increase the force by dX
                if((beforeXGravity[3] == 1) && (beforeXGravity[4] == 0))
                {
                    beforeXGravity[3] = 0;
                    beforeXGravity[4] = 1;
                }
            }
            else if((prob >= 2.0 / 3.0) && (prob < 3.0 / 3.0))
            {
                // do this 1/3rd of the time
                // increase the force by 2*dX
                if((beforeXGravity[2] == 1) && (beforeXGravity[5] == 0))
                {
                    beforeXGravity[2] = 0;
                    beforeXGravity[5] = 1;
                }
            }
        }

        // the force along the x-axis has now been applied (by moving a
        // particle)
        return beforeXGravity;
    }

    /**
     * Applies gravity (or any body force) in the negative y-axis direction.
     * This works by moving particles to subtract a y-component without adding
     * an x-component.
     * 
     * @param beforeYGravity
     *            The cell's particles before gravity along the y-axis is
     *            applied.
     * @param theta
     *            The direction of gravity (or other body force) in degrees.
     * @return The cell's particles after gravity along the y-axis has been
     *         applied.
     */
    private int[] applyNegativeForceAlongYAxis(int[] beforeYGravity,
        double theta)
    {
        // the angle of the applied body force, converted to radians
        double thetaRadians = theta * (Math.PI / 180.0);

        // same as applyForceAlongXAxis, but particle directions are reversed
        // (for example position 1 to position 3, rather than position 3 to
        // position 1).

        // The following ensures that the average change in force is dX (the
        // x-axis distance between nodes).
        double prob = random.nextDouble();
        if(prob < Math.abs(1.0 / (2.0 * Math.sin(thetaRadians))))
        {
            // now find out which of the possible particle moves to apply
            if(random.nextBoolean())
            {
                // do this 1/2 of the time
                // increase the force by 2*sin(60)*dX
                if((beforeYGravity[1] == 1) && (beforeYGravity[3] == 0))
                {
                    beforeYGravity[1] = 0;
                    beforeYGravity[3] = 1;
                }
            }
            else
            {
                // do this 1/2 of the time
                // increase the force by 2*sin(60)*dX
                if((beforeYGravity[0] == 1) && (beforeYGravity[4] == 0))
                {
                    beforeYGravity[0] = 0;
                    beforeYGravity[4] = 1;
                }
            }
        }

        // the force along the y-axis has now been applied (by moving a
        // particle)
        return beforeYGravity;
    }

    /**
     * NOT CURRENTLY USED BY THE LATTICE GAS MODEL. LEFT IN THE CODE IN CASE I
     * WANT IT LATER.
     * <p>
     * Adds a small random body force by adjusting particle positions.
     * Essentially, some particle gets a new vector (the "brownian" motion). The
     * adjustment is random so will average to zero over both time and/or space.
     * 
     * @param beforeBrownian
     *            The array of values before applying the random force.
     * @return The array of values after the application of a small random
     *         force.
     */
    private int[] brownianMotion(int[] beforeBrownian)
    {
        // only apply brownian motion if not a wall (if do this at walls,
        // then
        // can move particles through the walls by reorienting them)
        if(beforeBrownian[6] != 1)
        {
            // the percentage of cells that are changed
            if(random.nextDouble() < brownianPercent)
            {
                // choose two random directions and swap their particles
                // (and
                // lack of particles)
                int direction1 = random.nextInt(6);
                int direction2 = random.nextInt(6);

                // make sure not the same direction
                while(direction1 == direction2)
                {
                    direction2 = random.nextInt(6);
                }

                // swap
                int temp = beforeBrownian[direction1];
                beforeBrownian[direction1] = beforeBrownian[direction2];
                beforeBrownian[direction2] = temp;
            }
        }

        return beforeBrownian;
    }

    /**
     * Handles collisions for the lattice gas.
     * 
     * @param beforeCollision
     *            The array of values before the collision.
     * @return The array of values after the collision.
     */
    private int[] collide(int[] beforeCollision)
    {
        int[] afterCollision = new int[7];

        // the wall
        afterCollision[6] = beforeCollision[6];

        // if it is a wall, then bounce back conditions apply
        if(afterCollision[6] == 1)
        {
            // bounceBack
            for(int i = 0; i < 6; i++)
            {
                // just reverse the direction of each particle
                afterCollision[i] = beforeCollision[(i + 3) % 6];
            }
        }
        else
        // regular collision
        {
            // convert to an integer
            String binaryString = "";
            for(int i = 0; i < 6; i++)
            {
                binaryString += "" + beforeCollision[i];
            }

            // apply the collision rule
            int beforeCollisionNumber = Integer.parseInt(binaryString, 2);
            int afterCollisionNumber = collisionRule[beforeCollisionNumber];

            // USE THIS INSTEAD OF PREVIOUS LINE TO TURN OFF COLLISION
            // int afterCollisionNumber = beforeCollisionNumber;

            // convert decimal to binary
            for(int i = 0; i < 6; i++)
            {
                afterCollision[i] = getBit(afterCollisionNumber, i);
            }
        }

        return afterCollision;
    }

    /**
     * Creates FHP rules for the lattice gas. Although the states are binary
     * arrays, the lookup rules are based on their decimal equivalent. For
     * example, {1,0,0,1,0,0} is 36.
     */
    private void createRuleTable()
    {
        // most collisions don't alter the particle paths
        for(int i = 0; i < collisionRule.length; i++)
        {
            collisionRule[i] = i;
        }

        // but a few collisions do change the particle paths
        // two-particle collisions
        collisionRule[9] = 36;
        collisionRule[18] = 9;
        collisionRule[36] = 18;

        // three-particle symmetric collisions
        collisionRule[21] = 42;
        collisionRule[42] = 21;

        // three-particle asymmetric collisions
        collisionRule[11] = 38;
        collisionRule[22] = 13;
        collisionRule[25] = 52;
        collisionRule[37] = 19;
        collisionRule[44] = 26;
        collisionRule[50] = 41;

        collisionRule[38] = 11;
        collisionRule[13] = 22;
        collisionRule[52] = 25;
        collisionRule[19] = 37;
        collisionRule[26] = 44;
        collisionRule[41] = 50;

        // four-particle collisions
        collisionRule[27] = 45;
        collisionRule[45] = 54;
        collisionRule[54] = 27;
    }

    /**
     * Gets the bit of a number at the specified index.
     * 
     * @param number
     *            The number for which we are getting the bit.
     * @param index
     *            The position.
     * @return A bit.
     */
    private int getBit(int number, int index)
    {
        String stringNumber = Integer.toBinaryString(number);

        // add zeroes to the front (which are not printed when converting the
        // int to binary)
        while(stringNumber.length() < 6)
        {
            stringNumber = "0" + stringNumber;
        }

        String sBit = stringNumber.substring(index, index + 1);
        int bit = Integer.parseInt(sBit);

        return bit;
    }

    /**
     * Handles gravity (or any other applied body force) for the lattice gas.
     * Works by changing the direction of randomly selected particles to point
     * in the direction of gravity (or the applied body force).
     * 
     * @param beforeGravity
     *            The array of values before applying gravity.
     * @param theta
     *            The direction of gravity (or other body force) in degrees.
     * @param strength
     *            The probability that a particle gets moved to point in the
     *            direction of the force.
     * @return The array of values after the application of gravity.
     */
    private int[] gravity(int[] beforeGravity, double theta, double strength)
    {
        // only apply gravity if not a wall (if do this at walls, then can move
        // particles through the walls by reorienting them)
        if(beforeGravity[6] != 1)
        {
            // only re-orient some small percentage of particles. This controls
            // the strength of gravity.
            if(random.nextDouble() < strength)
            {
                // the angle of the applied body force, converted to radians
                double thetaRadians = theta * (Math.PI / 180.0);

                // theta gives the direction of gravity. It is a combination of
                // x and y components (given by cos and sin respectively). We
                // must apply the x and y components in the proper proportions
                // (given by |cos(theta)|/(|sin(theta)| + |cos(theta)|) for the
                // x-component, and |sin(theta)|/|(sin(theta)| + |cos(theta)|)
                // for the y-component.

                // So apply either the x-component or y-component in the correct
                // proportion (percentage)
                double percentX = Math.abs(Math.cos(thetaRadians))
                    / (Math.abs(Math.sin(thetaRadians)) + Math.abs(Math
                        .cos(thetaRadians)));
                if(random.nextDouble() < percentX)
                {
                    if(Math.cos(thetaRadians) >= 0)
                    {
                        beforeGravity = applyForceAlongXAxis(beforeGravity);
                    }
                    else
                    {
                        beforeGravity = applyNegativeForceAlongXAxis(beforeGravity);
                    }
                }
                else
                {
                    if(Math.sin(thetaRadians) >= 0)
                    {
                        beforeGravity = applyForceAlongYAxis(beforeGravity,
                            theta);
                    }
                    else
                    {
                        beforeGravity = applyNegativeForceAlongYAxis(
                            beforeGravity, theta);
                    }
                }
            }
        }

        return beforeGravity;
    }

    /**
     * Reorient the particles. In other words, change their momentum but not
     * their mass.
     * 
     * @param beforeRedistribute
     *            The array of particles before being redistributed.
     */
    private void redistribute(int[] beforeRedistribute)
    {
        // do this once per cell whenever theta changes
        if((forceTheta != previousTheta) || (count < numberOfCells))
        {
            if(forceTheta != previousTheta)
            {
                // reset these values
                previousTheta = forceTheta;
                count = 0;

                // figure out how many cells there are
                int width = CurrentProperties.getInstance().getNumColumns();
                int height = CurrentProperties.getInstance().getNumRows();
                numberOfCells = width * height;
            }

            // count how many cells have been adjusted
            count++;

            // redistribute pairs of particles this many times
            for(int i = 0; i < 6; i++)
            {
                // choose two random directions and swap their particles
                // (and lack of particles)
                int direction1 = random.nextInt(6);
                int direction2 = random.nextInt(6);

                // make sure not the same direction
                while(direction1 == direction2)
                {
                    direction2 = random.nextInt(6);
                }

                // swap
                int temp = beforeRedistribute[direction1];
                beforeRedistribute[direction1] = beforeRedistribute[direction2];
                beforeRedistribute[direction2] = temp;
            }
        }
    }

    /**
     * Handles translation of particles for the lattice gas.
     * 
     * @param cell
     *            The cell being updated.
     * @param neighbors
     *            The cells on which the update is based (usually neighboring
     *            cells). By convention the neighbors should be indexed
     *            clockwise starting to the northwest of the cell. May be null
     *            if want this method to find the "neighboring" cells.
     * @return The array of values after the translation.
     */
    private int[] translate(Cell cell, Cell[] neighbors)
    {
        // get the generation of this cell (so we can always make sure we get
        // the corresponding generation of the neighbors)
        int generation = cell.getGeneration();

        // get this cell's state
        int[] cellState = (int[]) ((LatticeGasState) cell.getState(generation))
            .getValue();

        // get neighboring states
        int[] neighbor0State = (int[]) ((LatticeGasState) neighbors[0]
            .getState(generation)).getValue();
        int[] neighbor1State = (int[]) ((LatticeGasState) neighbors[1]
            .getState(generation)).getValue();
        int[] neighbor2State = (int[]) ((LatticeGasState) neighbors[2]
            .getState(generation)).getValue();
        int[] neighbor3State = (int[]) ((LatticeGasState) neighbors[3]
            .getState(generation)).getValue();
        int[] neighbor4State = (int[]) ((LatticeGasState) neighbors[4]
            .getState(generation)).getValue();
        int[] neighbor5State = (int[]) ((LatticeGasState) neighbors[5]
            .getState(generation)).getValue();

        // get the incoming vectors (i.e., translate)
        int[] afterTranslation = new int[7];
        afterTranslation[0] = neighbor3State[0];
        afterTranslation[1] = neighbor4State[1];
        afterTranslation[2] = neighbor5State[2];
        afterTranslation[3] = neighbor0State[3];
        afterTranslation[4] = neighbor1State[4];
        afterTranslation[5] = neighbor2State[5];
        afterTranslation[6] = cellState[6]; // the value for a wall

        return afterTranslation;
    }

    /**
     * Calculates the new state of a cell based on the values of neighboring
     * cells (and possibly its own value as well itself). By convention the
     * neighbors should be indexed clockwise starting to the northwest of the
     * cell.
     * 
     * @param cell
     *            The cell being updated.
     * @param neighbors
     *            The cells on which the update is based (usually neighboring
     *            cells). By convention the neighbors should be indexed
     *            clockwise starting to the northwest of the cell. May be null
     *            if want this method to find the "neighboring" cells.
     * @return A new state for the cell.
     */
    public CellState calculateNewState(Cell cell, Cell[] neighbors)
    {
        // get constants that might be updated by the user in the More
        // Properties panel
        if(forceStrengthSpinner != null)
        {
            forceStrength = ((Double) ((SpinnerNumberModel) forceStrengthSpinner
                .getModel()).getNumber()).doubleValue();
            forceTheta = ((Double) ((SpinnerNumberModel) forceDirectionSpinner
                .getModel()).getNumber()).doubleValue();
        }

        // translate -- move the particles from node to node
        int[] translatedButNotCollided = translate(cell, neighbors);

        // If the direction of the body force has changed, this re-orients the
        // particles by changing their momentum but not their total mass.
        // This prevents problems where the body force can't act because no
        // particles are available in the correct directions (they may all be
        // pointing to the right, so they can't be re-oriented upwards
        // because there are no empty spaces to the right and upwards (for
        // example).
        redistribute(translatedButNotCollided);

        // collide -- apply collision rules
        int[] afterCollision = collide(translatedButNotCollided);

        // Adds a small random component so that the particles don't all end
        // up with the exact same orientation. Prevents problems that might
        // occur if the body force (like gravity) changes direction but then
        // has no particles to adjust because they are all oriented in the
        // direction of the old body force. Note that the component is
        // random, so will average to zero in both space and time.
        //
        // Currently not used because introduces too much noise. Instead use
        // the method "redistribute()".
        // int[] afterBrownian = brownianMotion(afterCollision);

        // gravity -- apply gravity rules
        int[] afterGravity = gravity(afterCollision, forceTheta, forceStrength);

        // now create the new state
        LatticeGasState newState = new LatticeGasState(afterGravity);

        return newState;
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
        // the panel on which we add the controls
        JPanel innerPanel = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        innerPanel.setLayout(layout);

        // a "grab their attention" panel
        AttentionPanel attentionPanel = new AttentionPanel("Gravity");

        // description
        String description = "Set the strength and direction of \"gravity.\" "
            + "Technically we are setting the value of a body force, but "
            + "thinking of gravity makes it easy to visualize. \n\n"
            + "The strength or magnitude of the force will not "
            + "scale directly with standard units like (kg m)/s^2 and "
            + "must be a value between 0 and 1. (Look up the cool topic of "
            + "non-dimensionalization techniques to understand how these "
            + "numbers can be rescaled to \"real values.\")";
        MultilineLabel descriptionLabel = new MultilineLabel(description);
        descriptionLabel.setFont(fonts.getMorePropertiesDescriptionFont());
        descriptionLabel.setMargin(new Insets(2, 6, 2, 2));

        JLabel forceLabel = new JLabel("Magnitude (0 to 1):");
        JLabel forceDirectionLabel = new JLabel(
            "Direction (-360 to 360 degrees): ");

        // create a spinner for the force strength
        SpinnerNumberModel forceStrengthModel = new SpinnerNumberModel(
            forceStrength, 0.00, 1.00, 0.01);
        forceStrengthSpinner = new JSpinner(forceStrengthModel);
        forceStrengthSpinner.setToolTipText(FORCE_STRENGTH_TIP);
        forceStrengthSpinner.setFont(fonts.getPlainFont());
        ((JSpinner.DefaultEditor) forceStrengthSpinner.getEditor())
            .getTextField().setColumns(4);

        // create a spinner for the force direction
        SpinnerNumberModel yInterceptModelRe = new SpinnerNumberModel(
            forceTheta, -360, 360, 1);
        forceDirectionSpinner = new JSpinner(yInterceptModelRe);
        forceDirectionSpinner.setToolTipText(FORCE_DIRECTION_TIP);
        forceDirectionSpinner.setFont(fonts.getPlainFont());
        ((JSpinner.DefaultEditor) forceDirectionSpinner.getEditor())
            .getTextField().setColumns(4);

        // layout components
        int row = 0;
        innerPanel.add(attentionPanel, new GBC(1, row).setSpan(10, 1).setFill(
            GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(1));

        row++;
        innerPanel.add(descriptionLabel, new GBC(1, row).setSpan(10, 1)
            .setFill(GBC.HORIZONTAL).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(1));

        row++;
        innerPanel.add(new JLabel(" "), new GBC(0, row).setSpan(10, 1).setFill(
            GBC.BOTH).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(1));

        row++;
        innerPanel.add(forceLabel, new GBC(1, row).setSpan(4, 1).setFill(
            GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST).setInsets(10));
        innerPanel.add(forceStrengthSpinner, new GBC(5, row).setSpan(4, 1)
            .setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(1));

        row++;
        innerPanel.add(forceDirectionLabel, new GBC(1, row).setSpan(4, 1)
            .setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(10));
        innerPanel.add(forceDirectionSpinner, new GBC(5, row).setSpan(4, 1)
            .setFill(GBC.NONE).setWeight(1.0, 1.0).setAnchor(GBC.WEST)
            .setInsets(1));

        // fill remaining space
        row++;
        innerPanel.add(new JLabel(" "), new GBC(0, row).setSpan(10, 1).setFill(
            GBC.BOTH).setWeight(100.0, 100.0).setAnchor(GBC.WEST).setInsets(1));

        return innerPanel;
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
     * Gets an instance of the CellState class that is compatible with this rule
     * (must be the same as the type returned by the method
     * calculateNewState()). The values assigned to this instance are
     * unimportant because it will only be used to construct instances of this
     * class type using reflection. Appropriate cellStates to return include
     * HexagonalBinaryCellState and SquareBinaryCellState.
     * 
     * @return An instance of the CellState (its state values are unimportant).
     */
    public CellState getCompatibleCellState()
    {
        return new LatticeGasState();
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
        return new LatticeGasCellStateView();
    }

    /**
     * A list of lattices with which this Rule will work, which in this case, is
     * only the hexagonal lattice. <br>
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
        String[] lattices = {HexagonalLattice.DISPLAY_NAME};

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
        String[] folders = {RuleFolderNames.PHYSICS_FOLDER,
            RuleFolderNames.INSTRUCTIONAL_FOLDER,
            RuleFolderNames.CLASSICS_FOLDER,
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
     * Tells the graphics what value should be displayed for the "Running
     * average" text field. Should be an integer greater than or equal to 1. By
     * default, the number is 1. This method should be overriden by child
     * classes if they desire non-default behavior.
     * 
     * @return The running average that will be displayed. By default returns
     *         null, which keeps the text field's current value.
     */
    public Integer getRunningAverageToDisplay()
    {
        return new Integer(10);
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
}
