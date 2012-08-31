/*
 CAStateInitializer -- a class within the Cellular Automaton Explorer. 
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import cellularAutomata.cellState.model.CellState;
import cellularAutomata.error.WarningManager;
import cellularAutomata.graphics.CAFrame;
import cellularAutomata.graphics.InitialStatesPanel;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.OneDimensionalLattice;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.reflection.URIResource;
import cellularAutomata.reflection.URLResource;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.dataStructures.FiniteArrayList;
import cellularAutomata.util.files.AllImageFilter;
import cellularAutomata.util.files.AllImageTypeReader;
import cellularAutomata.util.math.RandomSingleton;

/**
 * Creates initial states for cells.
 * 
 * @author David Bahr
 */
public class CAStateInitializer
{
    // the frame that displays all the CA graphics
    private CAFrame frame = null;

    // the actual JFrame that displays all the CA graphics
    private JFrame jFrame = null;

    // the CA lattice
    private Lattice lattice = null;

    /**
     * Create a state initializer.
     * 
     * @param frame
     *            The frame that displays the application. May be null. When
     *            null, error message panels will not recognize the frame as the
     *            parent component and therefore may get lost.
     * @param lattice
     *            The CA lattice.
     */
    public CAStateInitializer(CAFrame frame, Lattice lattice)
    {
        this.lattice = lattice;
        this.frame = frame;

        if(frame != null)
        {
            jFrame = frame.getFrame();
        }
    }

    /**
     * Check to see if the init state was specified by a rule.
     * 
     * @param initsStateName
     *            The name of the initial state.
     * @return true if the initial state should be specified by the current
     *         rule.
     */
    private boolean isInitStateFromRule(String initsStateName)
    {
        // check to see if it is an init state specified by a rule
        boolean isFromRule = false;

        // the currently selected rule
        String ruleClassName = CurrentProperties.getInstance()
            .getRuleClassName();

        // instantiate the rule using reflection
        Rule rule = ReflectionTool
            .instantiateMinimalRuleFromClassName(ruleClassName);

        // make sure it really was instantiated!
        if(rule != null)
        {
            String[] namesFromRule = rule.getInitialStateNames();

            if(namesFromRule != null)
            {
                for(int i = 0; i < namesFromRule.length; i++)
                {
                    if(initsStateName.equals(namesFromRule[i]))
                    {
                        isFromRule = true;
                    }
                }
            }
        }

        return isFromRule;
    }

    /**
     * Sets the state of all the cells by reading values from a data file.
     * 
     * @param filePath
     *            The path to the file containing the initial state of the
     *            automaton. For example, "C:/initial.data".
     */
    private void loadDataFile(String filePath)
    {
        CurrentProperties properties = CurrentProperties.getInstance();

        FileReader inputStream = null;
        BufferedReader fileReader = null;
        try
        {
            // the current rule
            Rule rule = ((Cell) lattice.iterator().next()).getRule();

            // the current generation
            int currentGeneration = ((Cell) lattice.iterator().next())
                .getGeneration();

            // open the file containing the initial states
            inputStream = new FileReader(filePath);
            fileReader = new BufferedReader(inputStream);

            // LOAD THE DATA INTO A LIST (ignoring commented sections)
            LinkedList<String> data = new LinkedList<String>();
            String line = fileReader.readLine();
            while(line != null)
            {
                // remove any comments
                if(line.contains("//"))
                {
                    line = line.substring(0, line.indexOf("//"));

                    // this may have left "hidden" white space at the end
                    while(line.endsWith(" ") || line.endsWith("\t")
                        || line.endsWith("\f"))
                    {
                        line = line.substring(0, line.length() - 1);
                    }
                }

                // extract data one "word" at a time
                StringTokenizer tokens = new StringTokenizer(line, properties
                    .getDataDelimiters());

                // add each datum to the list
                while(tokens != null && tokens.hasMoreTokens())
                {
                    String datum = tokens.nextToken();

                    if(datum != null && datum.length() > 0)
                    {
                        data.add(datum);
                    }
                }

                // get the next line
                line = fileReader.readLine();
            }

            // HOW MANY GENERATIONS ARE WE LOADING?
            // We have collected the data, and now we need to store it in the
            // cells. How many generations will we load?
            int numRequiredGenerations = rule.getRequiredNumberOfGenerations();

            // but if it's a CA simulation file, then load all of the available
            // generations.
            if(AllImageFilter.getExtension(filePath).equals(
                CAConstants.CA_FILE_EXTENSION))
            {
                // how many rows and cols are there in the current simulation?
                int rows = properties.getNumRows();
                int cols = properties.getNumColumns();

                // How many generations are there? Depends on the lattice.
                if(OneDimensionalLattice.isCurrentLatticeOneDim())
                {
                    // One-dimensional.
                    // Then divide the data collected by the number of cols and
                    // that gives the number of available generations. (Note:
                    // integer division guarantees that I ignore data that I
                    // can't use.)
                    numRequiredGenerations = data.size() / cols;
                }
                else
                {
                    // Two-dimensional.
                    // Then divide the data by the size of the grid and
                    // that gives the number of available generations. (Note:
                    // integer division guarantees that I ignore data that I
                    // can't use.)
                    numRequiredGenerations = data.size() / (rows * cols);
                }

                // just to be safe
                if(numRequiredGenerations < 1)
                {
                    numRequiredGenerations = 1;
                }
            }

            // NOW ASSIGN EACH CELL A STATE (from the above list).
            // 
            // Note: there may be more than one generation required for the
            // initial state. So we start at the current generation, but go back
            // n-1 generations where n is the number of required generations.
            int startGeneration = currentGeneration
                - (rule.getRequiredNumberOfGenerations() - 1);
            int generationNumber = 0;
            while(generationNumber < numRequiredGenerations)
            {
                // Do everything in this loop once per required generation.

                // the generation being updated
                int generationBeingUpdated = startGeneration + generationNumber;

                Iterator cellIterator = lattice.iterator();
                while(cellIterator.hasNext())
                {
                    Cell cell = (Cell) cellIterator.next();

                    // update the state
                    CellState state = cell.getState(generationBeingUpdated);

                    // if the state is null, then it doesn't exist for that
                    // particular generation, and we need to make it
                    if(state == null)
                    {
                        // create a new state and add it to the cell
                        state = cell.getRule().getCompatibleCellState();
                        cell.addNewState(state);
                    }

                    if(data.size() > 0)
                    {
                        // add the data to the cell
                        state.setStateFromString(data.remove());
                    }
                    else
                    {
                        // There wasn't enough data in the file, so let's fill
                        // in with blanks. The 0 may not work with some types
                        // of states, but the state will throw a warning if
                        // necessary (and will usually adjust and add a blank
                        // state).
                        state.setStateFromString("" + 0);
                    }
                }

                // keep repeating until we have loaded all of the generations
                generationNumber++;
            }
        }
        catch(IOException e)
        {
            // file doesn't exist, or can't open, or empty, or... So set each
            // cell's state to a blank
            setBlankState();

            // used blank state, so set that property
            properties.setInitialState(CurrentProperties.STATE_BLANK);

            String message = "Could not open the requested data file: \n"
                + e.getMessage()
                + "\n\nValues will be set to a blank state instead.\n";

            // make the JFrame look disabled
            if(frame != null)
            {
                frame.setViewDisabled(true);
            }

            WarningManager.displayWarningWithMessageDialog(message, 100,
                jFrame, "Import data warning", JOptionPane.WARNING_MESSAGE,
                null);

            // make the JFrame look enabled
            if(frame != null)
            {
                frame.setViewDisabled(false);
            }
        }
        catch(Exception e)
        {
            // file doesn't exist, or can't open, or empty, or... So set each
            // cell's state to a blank
            setBlankState();

            // used blank state, so set that property
            properties.setInitialState(CurrentProperties.STATE_BLANK);

            // warn the user
            String message = "Could not import the requested data file, most likely \n"
                + "due to an error in the file's data.  Please import \n"
                + "a different initial state file.\n\n"
                + "Values will be set to a blank state instead.\n";

            // make the JFrame look disabled
            if(frame != null)
            {
                frame.setViewDisabled(true);
            }

            WarningManager.displayWarningWithMessageDialog(message, 100,
                jFrame, "Import data warning", JOptionPane.WARNING_MESSAGE,
                null);

            // JOptionPane.showMessageDialog(jFrame, message,
            // "Import data warning", JOptionPane.WARNING_MESSAGE);

            // make the JFrame look enabled
            if(frame != null)
            {
                frame.setViewDisabled(false);
            }
        }
        finally
        {
            // close the files
            try
            {
                fileReader.close();
                inputStream.close();
            }
            catch(Exception e)
            {
                // do nothing
            }
        }
    }

    /**
     * Sets the state of all the cells by reading values from an image file.
     * 
     * @param filePath
     *            The path to the file containing the initial state of the
     *            automaton. For example, "C:/initial.jpg".
     */
    private void loadImageFile(String filePath)
    {
        CurrentProperties properties = CurrentProperties.getInstance();

        try
        {
            AllImageTypeReader.read(filePath, lattice);
        }
        catch(IOException e)
        {
            // file doesn't exist, or can't open, or empty, so set each
            // cell's state to a blank
            setBlankState();

            // used blank state, so set that property
            properties.setInitialState(CurrentProperties.STATE_BLANK);

            String message = "There has been an error loading the image: "
                + e.getMessage()
                + "\nA blank initial state will be used instead.";

            // make the JFrame look disabled
            if(frame != null)
            {
                frame.setViewDisabled(true);
            }

            JOptionPane.showMessageDialog(jFrame, message,
                "Import image error", JOptionPane.ERROR_MESSAGE);

            // make the JFrame look enabled
            if(frame != null)
            {
                frame.setViewDisabled(false);
            }
        }
        catch(NumberFormatException e)
        {
            // file doesn't exist, or can't open, or empty, so set each
            // cell's state to a blank
            setBlankState();

            // used blank state, so set that property
            properties.setInitialState(CurrentProperties.STATE_BLANK);

            // warn the user
            String message = "Image files are only compatible with number-based\n"
                + "rules.  Please select a different initial state\n"
                + "or select a different rule.\n";

            // make the JFrame look disabled
            if(frame != null)
            {
                frame.setViewDisabled(true);
            }

            JOptionPane.showMessageDialog(jFrame, message,
                "Import image warning", JOptionPane.WARNING_MESSAGE);

            // make the JFrame look enabled
            if(frame != null)
            {
                frame.setViewDisabled(false);
            }
        }
        catch(InterruptedException e)
        {
            // file doesn't exist, or can't open, or empty, so set each
            // cell's state to a blank
            setBlankState();

            // used blank state, so set that property
            properties.setInitialState(CurrentProperties.STATE_BLANK);

            String message = "There has been an error getting pixels from "
                + "the image: " + e.getMessage()
                + "\nA blank initial state will be used instead.";

            // make the JFrame look disabled
            if(frame != null)
            {
                frame.setViewDisabled(true);
            }

            JOptionPane.showMessageDialog(jFrame, message,
                "Import image error", JOptionPane.ERROR_MESSAGE);

            // make the JFrame look enabled
            if(frame != null)
            {
                frame.setViewDisabled(false);
            }
        }
        catch(Exception e)
        {
            // file doesn't exist, or can't open, or empty, so set each
            // cell's state to a blank
            setBlankState();

            // used blank state, so set that property
            properties.setInitialState(CurrentProperties.STATE_BLANK);

            String message = "There has been an error loading the image: "
                + e.getMessage()
                + "\nA blank initial state will be used instead.";

            // make the JFrame look disabled
            if(frame != null)
            {
                frame.setViewDisabled(true);
            }

            JOptionPane.showMessageDialog(jFrame, message,
                "Import image error", JOptionPane.ERROR_MESSAGE);

            // make the JFrame look enabled
            if(frame != null)
            {
                frame.setViewDisabled(false);
            }
        }
    }

    /**
     * Sets every cell state to its blank value.
     */
    public void setBlankState()
    {
        Cell cell = null;
        Iterator cellIterator = lattice.iterator();
        while(cellIterator.hasNext())
        {
            cell = (Cell) cellIterator.next();
            int numInitialGenerations = cell.getRule()
                .getRequiredNumberOfGenerations();

            // erase the whole history of the cell except for those required as
            // initial conditions
            FiniteArrayList<CellState> stateHistory = cell.getStateHistory();

            // remove all but the last couple of generations (or however many
            // are required for the initial generations)
            while(stateHistory.size() > numInitialGenerations)
            {
                // remove the first instead of last, because the last ones are
                // from the latest generation
                stateHistory.removeFirst();
            }

            // 0 (and other initial states) are the only states left
            for(int i = 0; i < numInitialGenerations; i++)
            {
                stateHistory.get(i).setToEmptyState();
            }
        }
    }

    /**
     * Sets every cell state to its blank value, except the middle cell which is
     * set to its full value.
     */
    public void setSingleSeedState()
    {
        CurrentProperties properties = CurrentProperties.getInstance();

        int height = properties.getNumRows();
        int width = properties.getNumColumns();

        // find the middle position
        long numCells = 0;
        long middlePosition = 0;
        if(OneDimensionalLattice.isCurrentLatticeOneDim())
        {
            numCells = width;
            middlePosition = numCells / 2;
        }
        else
        {
            // must be 2-d
            numCells = height * width;
            middlePosition = numCells / 2;
            if(height % 2 == 0)
            {
                middlePosition = (numCells / 2) + (width / 2);
            }
        }

        // fill the cells
        Cell cell = null;
        Iterator cellIterator = lattice.iterator();
        for(int i = 0; i < numCells; i++)
        {
            cell = (Cell) cellIterator.next();

            // get the list of states for each cell
            FiniteArrayList<CellState> stateHistory = cell.getStateHistory();
            int historySize = stateHistory.size();

            // there may be more than one state required as initial conditions
            for(int j = 0; j < historySize; j++)
            {
                if((i != middlePosition) || (j != historySize - 1))
                {
                    stateHistory.get(j).setToEmptyState();
                }
                else
                {
                    stateHistory.get(j).setToFullState();
                }
            }
        }
    }

    /**
     * loads a data file as the initial state.
     */
    public void setDataState(String filePath)
    {
        if(CurrentProperties.getInstance().isFacadeOn()
            && (filePath == null || filePath.equals("")))
        {
            // use the EZ facade's .ca data path
            String ruleClassName = CurrentProperties.getInstance()
                .getRuleClassName();

            // remove the package info
            ruleClassName = ruleClassName.substring(ruleClassName
                .lastIndexOf(".") + 1);

            // the partial or relative path to the file
            String relativePath = "/"
                + CAConstants.FACADE_SIMULATIONS_FOLDER_NAME + "/"
                + ruleClassName + "." + CAConstants.CA_FILE_EXTENSION;

            // get the file path from the class name. Note the URIResource makes
            // sure that we can access files outside of the jar.
            // URI facadeFile = URIResource.getResource(relativePath);
            // Actually, only URL seems to work from the jar! If change here,
            // then also change in AllPanelListener.loadFacadeSimulation().
            URL facadeFile = URLResource.getResource(relativePath);

            // JOptionPane.showMessageDialog(null, "relativePath =
            // "+relativePath+
            // "; facadeFile = "+facadeFile+"facadeFile.getPath() =
            // "+facadeFile.getPath());

            // use this file path
            filePath = facadeFile.getPath();

            // %20's can mess up my XP OS.
            // If this happened, then the URL didn't work. Try a URI. This
            // failure only happens on my laptop with the latest versions of the
            // JRE/JDK/Eclipse/XP Pro operating system. Go figure.
            if(filePath == null || filePath.contains("%20"))
            {
                // This may not work either, but it's worth a shot. Works on my
                // laptop.
                URI facadeFileTry2 = URIResource.getResource(relativePath);
                filePath = facadeFileTry2.getPath();
            }
        }

        // JOptionPane.showMessageDialog(null, "filePath = "+filePath);

        loadDataFile(filePath);
    }

    /**
     * Creates an ellipse of the width and height specified by the initial state
     * panel.
     */
    public void setEllipseState()
    {
        CurrentProperties properties = CurrentProperties.getInstance();

        // user requested an ellipse of this height and width
        int ellipseHeight = properties.getInitialStateEllipseHeight();
        int ellipseWidth = properties.getInitialStateEllipseWidth();

        // and they may or may not have wanted it filled
        boolean fillEllipse = properties.isInitialStateEllipseFilled();

        // find the height and width of the lattice
        int caHeight = properties.getNumRows();
        int caWidth = properties.getNumColumns();

        // adjust to the practical height (in one-dim, have a real height of 1)
        if(OneDimensionalLattice.isCurrentLatticeOneDim())
        {
            caHeight = 1;
        }

        // center cell of the lattice
        double centerX = Math.floor(caWidth / 2.0);
        double centerY = Math.floor(caHeight / 2.0);

        // create a temporary array the same size as the lattice and fill it
        // with 1's at sites that are part of the ellipse. The tolerance is how
        // much of a cell has to be within the ellipse before that cell is
        // included (and drawn as part of the ellipse).
        int[][] ellipse = new int[caHeight][caWidth];
        for(int row = 0; row < ellipse.length; row++)
        {
            for(int col = 0; col < ellipse[row].length; col++)
            {
                // create two ellipses, one inside the other
                double edgeValue = 1;
                double innerValue = 1;
                if(ellipseHeight > 1 && ellipseWidth > 1)
                {
                    // (row, col) is on our desired ellipse when edgeValue = 1
                    edgeValue = Math.pow((col - centerX) / ellipseWidth, 2.0)
                        + Math.pow((row - centerY) / ellipseHeight, 2.0);

                    // another ellipse that is one cell smaller at each end of
                    // the major and minor axes. (This makes the inner value
                    // bigger than 1.0.)
                    innerValue = Math.pow((col - centerX)
                        / (ellipseWidth - 1.0), 2.0)
                        + Math
                            .pow((row - centerY) / (ellipseHeight - 1.0), 2.0);
                }
                else if(ellipseHeight != 0 && ellipseWidth != 0
                    && (ellipseHeight == 1 || ellipseWidth == 1))
                {
                    edgeValue = Math.pow((col - centerX) / ellipseWidth, 2.0)
                        + Math.pow((row - centerY) / ellipseHeight, 2.0);
                    innerValue = Integer.MAX_VALUE;
                }
                else if(ellipseHeight == 0 && ellipseWidth != 0)
                {
                    // only make the center line of cells display
                    edgeValue = Math.pow((col - centerX) / ellipseWidth, 2.0)
                        + Math.pow((row - centerY) / 0.1, 2.0);
                    innerValue = Integer.MAX_VALUE;
                }
                else if(ellipseHeight != 0 && ellipseWidth == 0)
                {
                    // only make the center line of cells display
                    edgeValue = Math.pow((col - centerX) / 0.1, 2.0)
                        + Math.pow((row - centerY) / ellipseHeight, 2.0);
                    innerValue = Integer.MAX_VALUE;
                }
                else if(ellipseHeight == 0 && ellipseWidth == 0)
                {
                    // only make the center line of cells display
                    edgeValue = Math.pow((col - centerX) / 0.1, 2.0)
                        + Math.pow((row - centerY) / 0.1, 2.0);
                    innerValue = Integer.MAX_VALUE;
                }

                if(fillEllipse && edgeValue < 1.0)
                {
                    // then inside ellipse
                    ellipse[row][col] = 1;
                }
                else if(edgeValue <= 1.0 && innerValue > 1.0)
                {
                    // then on edge of ellipse
                    ellipse[row][col] = 1;
                }
                else
                {
                    // not part of the ellipse
                    ellipse[row][col] = 0;
                }
            }
        }

        // unwrap the rectangle into a list (the same way as the lattice)
        LinkedList<Integer> ellipseList = new LinkedList<Integer>();
        for(int row = 0; row < ellipse.length; row++)
        {
            for(int col = 0; col < ellipse[row].length; col++)
            {
                ellipseList.add(new Integer(ellipse[row][col]));
            }
        }

        // fill the cells of the lattice
        Cell cell = null;
        Iterator cellIterator = lattice.iterator();
        for(int i = 0; i < ellipse[0].length * ellipse.length; i++)
        {
            cell = (Cell) cellIterator.next();

            // get the list of states for each cell
            FiniteArrayList<CellState> stateHistory = cell.getStateHistory();
            int historySize = stateHistory.size();

            // there may be more than one state required as initial conditions
            for(int j = 0; j < historySize; j++)
            {
                // only set cells to a full state when the corresponding
                // ellipseList is set to a value of 1
                if((j != historySize - 1) || ellipseList.get(i).intValue() == 0)
                {
                    stateHistory.get(j).setToEmptyState();
                }
                else
                {
                    stateHistory.get(j).setToFullState();
                }
            }
        }
    }

    /**
     * loads an image as the initial state.
     */
    public void setImageState(String filePath)
    {
        // is it an image file?
        if(AllImageFilter.isPermittedImageType(AllImageFilter
            .getExtension(filePath)))
        {
            // it is an image file
            loadImageFile(filePath);
        }
        else
        {
            // not a permitted image type, so give a warning
            String message = "The specified image file is not a permitted type ("
                + AllImageFilter.getListOfPermittedImageTypes()
                + ").\n"
                + "The cellular automata will be set to a blank state instead.";

            // make the JFrame look disabled
            if(frame != null)
            {
                frame.setViewDisabled(true);
            }

            JOptionPane.showMessageDialog(jFrame, message,
                "Import image warning", JOptionPane.WARNING_MESSAGE);

            // make the JFrame look enabled
            if(frame != null)
            {
                frame.setViewDisabled(false);
            }

            // and set to a blank state
            setBlankState();

            // used blank state, so set that property
            CurrentProperties.getInstance().setInitialState(
                CurrentProperties.STATE_BLANK);
        }
    }

    /**
     * Sets the initial state of all the cells.
     * 
     * @param initStateName
     *            The name of the option used to initialize the cells.
     */
    public void setInitialState(String initStateName)
    {
        if(initStateName != null)
        {
            if(initStateName.equals(CurrentProperties.STATE_BLANK))
            {
                setBlankState();
            }
            else if(initStateName.equals(CurrentProperties.STATE_SINGLE_SEED))
            {
                setSingleSeedState();
            }
            else if(initStateName.equals(CurrentProperties.STATE_RANDOM))
            {
                setRandomState();
            }
            else if(initStateName.equals(CurrentProperties.STATE_PROBABILITY))
            {
                int numStates = CurrentProperties.getInstance().getNumStates();

                if(numStates > InitialStatesPanel.MAX_NUMSTATES_FOR_PROBABILITY_BY_STATE)
                {
                    // shouldn't be here -- use a blank state instead
                    setBlankState();
                }
                else
                {
                    setProbabilityForEachState();
                }
            }
            else if(initStateName.equals(CurrentProperties.STATE_IMAGE))
            {
                setImageState(CurrentProperties.getInstance()
                    .getInitialStateImageFilePath());
            }
            else if(initStateName.equals(CurrentProperties.STATE_DATA))
            {
                setDataState(CurrentProperties.getInstance()
                    .getInitialStateDataFilePath());
            }
            else if(initStateName.equals(CurrentProperties.STATE_RECTANGLE))
            {
                setRectangleState();
            }
            else if(initStateName.equals(CurrentProperties.STATE_ELLIPSE))
            {
                setEllipseState();
            }
            else if(isInitStateFromRule(initStateName))
            {
                setStateFromRule(initStateName);
            }
            else
            {
                // unknown, so leave blank
                setBlankState();
            }
        }
        else
        {
            setBlankState();
        }
    }

    /**
     * Sets each cell to a random state based on the probability specified for
     * each individual state.
     * <p>
     * This method will only be called for integer cell states.
     */
    public void setProbabilityForEachState()
    {
        CurrentProperties properties = CurrentProperties.getInstance();

        int numStates = properties.getNumStates();

        Random random = RandomSingleton.getInstance();

        // get the cumulative "probabilities" (really the cumulative percents)
        int[] cumulativeProb = new int[numStates];

        // get the cumulative probabilities from the percentages. Makes it
        // easier to generate a random number and find the correct state.
        String percentString = properties.getRandomPercentPerState();
        String delimiters = properties.getDataDelimiters();
        StringTokenizer tokens = new StringTokenizer(percentString, delimiters);

        // add each percent datum to the cumulative probabilities
        int runningTotal = 0;
        for(int i = 0; i < cumulativeProb.length; i++)
        {
            if(tokens != null && tokens.hasMoreTokens())
            {
                runningTotal += new Integer(tokens.nextToken()).intValue();
            }
            else
            {
                runningTotal += 0;
            }
            cumulativeProb[i] = runningTotal;
        }

        // make sure it added up ok, and fudge it if not
        if(cumulativeProb[cumulativeProb.length - 1] != 100)
        {
            cumulativeProb[cumulativeProb.length - 1] = 100;
        }

        // build a hash table of values for fast look up
        Hashtable<Integer, Integer> cumulativeProbHash = new Hashtable<Integer, Integer>();
        int state = 0;
        for(int percent = 0; percent < 100; percent++)
        {
            while(percent >= cumulativeProb[state]
                && state < cumulativeProb.length)
            {
                state++;
            }

            cumulativeProbHash.put(new Integer(percent), new Integer(state));
        }

        // now get a value for every cell
        Cell cell = null;
        Iterator cellIterator = lattice.iterator();
        while(cellIterator.hasNext())
        {
            cell = (Cell) cellIterator.next();

            // get the list of states for each cell
            FiniteArrayList<CellState> stateHistory = cell.getStateHistory();
            int historySize = stateHistory.size();

            // there may be more than one state required as initial conditions
            for(int i = 0; i < historySize; i++)
            {
                // generate a random state
                int randomPercent = random.nextInt(100);
                Integer correspondingState = cumulativeProbHash
                    .get(new Integer(randomPercent));

                // set the cell's state
                stateHistory.get(i).setValue(correspondingState);
            }
        }
    }

    /**
     * Sets every cell state to an appropriate random value.
     */
    public void setRandomState()
    {
        double prob = CurrentProperties.getInstance().getRandomPercent() / 100.0;

        // UNUSED CODE. ONLY NECESSARY IF THIS METHOD IS GOING TO BE USED AT
        // TIMES OTHER THAN WHEN THE CELLS ARE BEING INSTANTIATED. UNLIKELY.
        //
        // Find out how many initial state generations are required
        // Iterator cellIterator = lattice.iterator();
        // Rule rule = ((Cell) cellIterator.next()).getRule();
        // int numInitialStates = rule.getRequiredNumberOfGenerations();
        //
        // NOW ONLY SET THE STATES TO RANDOM FOR THIS NUMBER OF HISTORIES

        Cell cell = null;
        Iterator cellIterator = lattice.iterator();
        while(cellIterator.hasNext())
        {
            cell = (Cell) cellIterator.next();

            // get the list of states for each cell
            FiniteArrayList<CellState> stateHistory = cell.getStateHistory();
            int historySize = stateHistory.size();

            // there may be more than one state required as initial conditions
            for(int i = 0; i < historySize; i++)
            {
                stateHistory.get(i).setToRandomState(prob);
            }
        }
    }

    /**
     * Creates a rectangle of the width and height specified by the initial
     * state panel.
     */
    public void setRectangleState()
    {
        CurrentProperties properties = CurrentProperties.getInstance();

        // user requested a rectangle of this height and width
        int rectangleHeight = properties.getInitialStateRectangleHeight();
        int rectangleWidth = properties.getInitialStateRectangleWidth();

        // and they may or may not have wanted it filled
        boolean fillRectangle = properties.isInitialStateRectangleFilled();

        // find the height and width of the lattice
        int caHeight = properties.getNumRows();
        int caWidth = properties.getNumColumns();

        // adjust to the practical height (in one-dim, have a real height of 1)
        if(OneDimensionalLattice.isCurrentLatticeOneDim())
        {
            caHeight = 1;
        }

        // find the distance above, below, right and left of the center
        int distanceAboveCenter = 0;
        int distanceBelowCenter = 0;
        if(rectangleHeight % 2 == 1)
        {
            distanceAboveCenter = (rectangleHeight / 2);
            distanceBelowCenter = (rectangleHeight / 2);
        }
        else
        {
            distanceAboveCenter = (rectangleHeight / 2);
            distanceBelowCenter = (rectangleHeight / 2) - 1;
        }
        int distanceLeftOfCenter = 0;
        int distanceRightOfCenter = 0;
        if(rectangleWidth % 2 == 1)
        {
            distanceLeftOfCenter = (rectangleWidth / 2);
            distanceRightOfCenter = (rectangleWidth / 2);
        }
        else
        {
            distanceLeftOfCenter = (rectangleWidth / 2);
            distanceRightOfCenter = (rectangleWidth / 2) - 1;
        }

        // create a temporary array the same size as the lattice and fill it
        // with 1's at sites that are part of the rectangle
        int[][] rectangle = new int[caHeight][caWidth];
        for(int row = 0; row < rectangle.length; row++)
        {
            for(int col = 0; col < rectangle[row].length; col++)
            {
                if((col == (caWidth / 2) - distanceLeftOfCenter)
                    && (row >= (caHeight / 2) - distanceAboveCenter)
                    && (row <= (caHeight / 2) + distanceBelowCenter))
                {
                    rectangle[row][col] = 1;
                }
                else if((col == (caWidth / 2) + distanceRightOfCenter)
                    && (row >= (caHeight / 2) - distanceAboveCenter)
                    && (row <= (caHeight / 2) + distanceBelowCenter))
                {
                    rectangle[row][col] = 1;
                }
                else if((row == (caHeight / 2) - distanceAboveCenter)
                    && (col >= (caWidth / 2) - distanceLeftOfCenter)
                    && (col <= (caWidth / 2) + distanceRightOfCenter))
                {
                    rectangle[row][col] = 1;
                }
                else if((row == (caHeight / 2) + distanceBelowCenter)
                    && (col >= (caWidth / 2) - distanceLeftOfCenter)
                    && (col <= (caWidth / 2) + distanceRightOfCenter))
                {
                    rectangle[row][col] = 1;
                }
                else if(fillRectangle
                    && (col > (caWidth / 2) - distanceLeftOfCenter)
                    && (col < (caWidth / 2) + distanceRightOfCenter)
                    && (row > (caHeight / 2) - distanceAboveCenter)
                    && (row < (caHeight / 2) + distanceBelowCenter))
                {
                    // inside of the rectangle
                    rectangle[row][col] = 1;
                }
                else
                {
                    // not on (or in) the rectangle
                    rectangle[row][col] = 0;
                }
            }
        }

        // unwrap the rectangle into a list (the same way as the lattice)
        LinkedList<Integer> rectangleList = new LinkedList<Integer>();
        for(int row = 0; row < rectangle.length; row++)
        {
            for(int col = 0; col < rectangle[row].length; col++)
            {
                rectangleList.add(new Integer(rectangle[row][col]));
            }
        }

        // fill the cells of the lattice
        Cell cell = null;
        Iterator cellIterator = lattice.iterator();
        for(int i = 0; i < rectangle[0].length * rectangle.length; i++)
        {
            cell = (Cell) cellIterator.next();

            // get the list of states for each cell
            FiniteArrayList<CellState> stateHistory = cell.getStateHistory();
            int historySize = stateHistory.size();

            // there may be more than one state required as initial conditions
            for(int j = 0; j < historySize; j++)
            {
                // only set cells to a full state when the corresponding
                // rectangleList is set to a value of 1
                if((j != historySize - 1)
                    || rectangleList.get(i).intValue() == 0)
                {
                    stateHistory.get(j).setToEmptyState();
                }
                else
                {
                    stateHistory.get(j).setToFullState();
                }
            }
        }
    }

    /**
     * Sets the state of all the cells by reading values from a file.
     * 
     * @param filePath
     *            The path to the file containing the initial state of the
     *            automaton. For example, "C:/initial.data".
     */
    public void setStateFromFile(String filePath)
    {
        // is it an image or a data file
        if(AllImageFilter.isPermittedImageType(AllImageFilter
            .getExtension(filePath)))
        {
            // it is an image file
            // loadImageFile(filePath);
        }
        else
        {
            // it is a data file
            loadDataFile(filePath);
        }
    }

    /**
     * Sets the state of all the cells by reading values from the rule.
     * 
     * @param initStateName
     *            The name of the initial state.
     */
    public void setStateFromRule(String initStateName)
    {
        // the currently selected rule
        String ruleClassName = CurrentProperties.getInstance()
            .getRuleClassName();

        // instantiate the rule using reflection
        Rule rule = ReflectionTool
            .instantiateFullRuleFromClassName(ruleClassName);

        // make sure it really was instantiated!
        if(rule != null)
        {
            try
            {
                rule.setInitialState(initStateName, lattice);
            }
            catch(Exception oops)
            {
                // Failed because of a problem with the rule
                String warning = "The user specified Rule \""
                    + rule
                    + "\" has an error when specifying the initial state. \n\n"
                    + "A default blank state will be used.  Please contact the author of the rule.\n\n";

                // make the JFrame look disabled
                if(frame != null)
                {
                    frame.setViewDisabled(true);
                }

                JOptionPane.showMessageDialog(jFrame, warning,
                    "Developer Warning", JOptionPane.WARNING_MESSAGE);

                // make the JFrame look enabled
                if(frame != null)
                {
                    frame.setViewDisabled(false);
                }

                // oops!
                setBlankState();
            }
        }
        else
        {
            // oops!
            setBlankState();
        }
    }
}
