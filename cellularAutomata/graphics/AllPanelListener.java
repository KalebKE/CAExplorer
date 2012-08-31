/*
 AllPanelListener -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.graphics;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.Document;

import cellularAutomata.CAConstants;
import cellularAutomata.CAController;
import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.analysis.Analysis;
import cellularAutomata.error.WarningManager;
import cellularAutomata.lattice.MooreRadiusOneDimLattice;
import cellularAutomata.lattice.MooreRadiusTwoDimLattice;
import cellularAutomata.lattice.VonNeumannRadiusLattice;
import cellularAutomata.movie.MovieMaker;
import cellularAutomata.reflection.AnalysisHash;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.reflection.RuleHash;
import cellularAutomata.reflection.URIResource;
import cellularAutomata.reflection.URLResource;
import cellularAutomata.rules.Rule;
import cellularAutomata.util.graphics.ShimmyingTenTimesIconJButton;

// Note that this class implements both the actionListener and also fires
// PropertyChangeEvents. The ActionListener handles events locally (to
// minimize the number of objects passed around) and fires
// PropertyChangeEvents so that other classes can deal with non-local
// consequences.

/**
 * Listens for events from the GUI components.
 * 
 * @author David Bahr
 */
public class AllPanelListener extends MouseAdapter implements ActionListener,
    ChangeListener, PropertyChangeListener, DocumentListener
{
    // The controller of components on all of the panels.
    private AllPanelController controller = null;

    // The panel with components to which this class listens
    private AllPanel panel = null;

    // A list of active analyses
    private ArrayList<Analysis> analysisList = new ArrayList<Analysis>();

    // allows us to add PropertyChangeListeners listeners to this class
    private EventListenerList listenerList = new EventListenerList();

    /**
     * Create a listener for events from the panel.
     * 
     * @param panel
     *            The panel that holds all other CA control panels.
     */
    public AllPanelListener(AllPanel panel)
    {
        this.panel = panel;

        // The controller of interactions between all the panels.
        this.controller = panel.getController();
    }

    /**
     * Check that the graphics display increment is ok.
     * 
     * @return true if value is ok. False otherwise.
     */
    private boolean checkGraphicsDisplayIncrement()
    {
        boolean valueOk = true;

        // get the spinner
        JSpinner incrementSpinner = panel.getStartPanel()
            .getGraphicsUpdateSpinner();

        try
        {
            // see if it is an ok value
            incrementSpinner.commitEdit();
        }
        catch(Exception e)
        {
            valueOk = false;

            // The edited value is invalid. spinner.getValue() will return
            // the last valid value, so we use that to reset to the spinner to
            // the last valid value.
            JComponent editor = incrementSpinner.getEditor();
            if(editor instanceof JSpinner.NumberEditor)
            {
                ((JSpinner.NumberEditor) editor).getTextField().setValue(
                    incrementSpinner.getValue());
            }

            // print error message
            String errorMessage = "The graphics interval must "
                + "be a positive integer between 1 and " + Integer.MAX_VALUE
                + ".";

            // make the JFrame look disabled
            panel.getCAFrame().setViewDisabled(true);

            JOptionPane.showMessageDialog(null, errorMessage, "InputError",
                JOptionPane.ERROR_MESSAGE);

            // make the JFrame look enabled
            panel.getCAFrame().setViewDisabled(false);
        }

        return valueOk;
    }

    /**
     * Check that the pause time is ok, and set a property for the pause time.
     * 
     * @return true if value is ok. False otherwise.
     */
    private boolean checkPauseTime()
    {
        boolean valueOk = true;
        boolean tooBig = false;

        try
        {
            // read the stop time
            String stopTimeString = panel.getStartPanel().getStopTimeField()
                .getText();

            // is it too big?
            if(stopTimeString.length() > new Integer(Integer.MAX_VALUE)
                .toString().length())
            {
                tooBig = true;
            }

            // can it be parsed? (if too big, will throw an exception)
            int stopTime = Integer.parseInt(stopTimeString);
            if(stopTime < 0)
            {
                throw new NumberFormatException();
            }
            CurrentProperties.getInstance().setMaxTime(stopTime);
        }
        catch(Exception error)
        {
            String errorMessage = "The \"time steps before pausing\" must "
                + "be a positive integer";
            if(!tooBig)
            {
                errorMessage += ".";
            }
            else
            {
                errorMessage += " less than " + Integer.MAX_VALUE + ".";
            }

            // make the JFrame look disabled
            panel.getCAFrame().setViewDisabled(true);

            JOptionPane.showMessageDialog(null, errorMessage, "InputError",
                JOptionPane.ERROR_MESSAGE);

            // make the JFrame look enabled
            panel.getCAFrame().setViewDisabled(false);

            // user made mistake, so don't continue.
            valueOk = false;
        }

        return valueOk;
    }

    /**
     * Actions to take when the rule has been submitted.
     * 
     * @param shouldAskIfWantToSubmit
     *            true if the user should be asked if they are sure they want to
     *            submit. When submit happens programmatically, this may need to
     *            be false.
     * @return true if the properties were submitted and false if the submission
     *         was canceled.
     */
    public boolean submitProperties(boolean shouldAskIfWantToSubmit)
    {
        // check to see if they want to stop the simulation
        boolean continueSubmission = true;
        if(shouldAskIfWantToSubmit)
        {
            continueSubmission = sureYouWantToSubmit();
        }

        if(continueSubmission)
        {
            // stop the movie, if it is being created
            if(MovieMaker.isOpen())
            {
                MovieMaker.closeMovie();
            }

            // check that the user submitted properties are ok. The hashtable
            // will contain the properties if they are all ok. If there is a
            // problem with the properties, this method will warn the user and
            // return null.
            Hashtable<String, Object> userSubmittedProperties = controller
                .checkProperties();

            if(userSubmittedProperties != null)
            {
                // handle local implications (e.g., change appearance of
                // start/stop buttons)
                controller.stopActions();
                panel.getCAFrame().getMenuBar().enableZoomIn(true);
                panel.getCAFrame().getMenuBar().enableZoomOut(true);

                // change the color of the submit button
                panel.getPropertiesPanel().resetSubmitButtonColorToDefault();
                panel.getRulePanel().resetSubmitButtonColorToDefault();
                panel.getInitialStatesPanel().resetSubmitButtonColorToDefault();

                // replace the additional properties tab
                panel.getAdditionalPropertiesPanel().reset();

                // stop the simulation if it is running
                CAController.getInstanceOfCAController().stopCA();

                // now submit the properties (must happen after the simulation
                // has stopped, or it might accidentally use the new properties
                // and crash)
                controller.submitProperties(userSubmittedProperties);

                // now inform non-local classes that they need to
                // redo the setup (i.e., notify anyone who cares.)
                firePropertyChangeEvent(new PropertyChangeEvent(this,
                    CurrentProperties.SETUP, null, new Boolean(true)));

                // now add the new rule to the recently selected folder in
                // the rule tree menu
                String ruleName = controller.getRulePanel().getRuleTree()
                    .getSelectedRuleName();
                controller.getRulePanel().getRuleTree()
                    .addToRecentlySelectedRulesFolder(ruleName);

                // reset the description panel
                panel.getDescriptionPanel().setDescriptionBrowserToNewURL();
            }
            else
            {
                // there was a problem with the user submitted values
                continueSubmission = false;
            }
        }

        if(continueSubmission)
        {
            // reset the additional properties panel, which might need to change
            // based on newly submitted values
            panel.getAdditionalPropertiesPanel().reset();
        }

        // returns true if the properties were submitted and false if the
        // submission was canceled.
        return continueSubmission;
    }

    /**
     * This makes sure the user wants to stop the simulation by submitting.
     * 
     * @return true if the simulation should stop.
     */
    private boolean sureYouWantToSubmit()
    {
        // make the frame look disabled
        panel.getCAFrame().setViewDisabled(true);

        boolean submit = false;

        String message = "Submitting new properties will stop and\n"
            + "replace the current simulation. \n\nContinue?";

        int answer = JOptionPane.showConfirmDialog(panel.getCAFrame()
            .getFrame(), message, "Submit Confirmation",
            JOptionPane.YES_NO_CANCEL_OPTION);

        if(answer == JOptionPane.YES_OPTION)
        {
            submit = true;
        }

        // make the frame look enabled
        panel.getCAFrame().setViewDisabled(false);

        return submit;
    }

    /**
     * Reacts to the buttons.
     */
    public void actionPerformed(ActionEvent e)
    {
        String command = e.getActionCommand();

        CurrentProperties properties = CurrentProperties.getInstance();

        if(command.equals(PropertiesPanel.SUBMIT_PROPERTIES))
        {
            if(CurrentProperties.getInstance().isFacadeOn())
            {
                loadFacadeSimulation();
            }
            else
            {
                submitProperties(true);
            }
        }
        else if(command.equals(StartPanel.START_STRING))
        {
            // refers to user provided "pause time" value
            boolean valueOk = true;

            // read the "pause time" values
            valueOk = checkPauseTime();

            // only continue if the pause time is ok.
            if(valueOk)
            {
                int stopTime = Integer.parseInt(panel.getStartPanel()
                    .getStopTimeField().getText());

                // handle local GUI implications (e.g., change appearance of
                // buttons)
                controller.startActions();

                // notify non-local classes of the new pause time
                firePropertyChangeEvent(new PropertyChangeEvent(this,
                    CurrentProperties.MAX_TIME, null, new Integer(stopTime)));

                // start the simulation
                CAController.getInstanceOfCAController().startCAInThread();
            }
        }
        else if(command.equals(StartPanel.STOP_STRING))
        {
            // take local actions
            controller.stopActions();

            // stop the simulation
            CAController.getInstanceOfCAController().stopCA();
        }
        else if(command.equals(StartPanel.INCREMENT))
        {
            // handle local GUI implications (e.g., change appearance of
            // buttons)
            controller.incrementActions();

            // now notify non-local classes of the new pause time (1 step)
            firePropertyChangeEvent(new PropertyChangeEvent(this,
                CurrentProperties.MAX_TIME, null, new Integer(1)));

            // start the simulation
            CAController.getInstanceOfCAController().startCAInThread();
        }
        else if(command.equals(StartPanel.STEP10))
        {
            // handle local GUI implications (e.g., change appearance of
            // buttons)
            controller.startActions();

            // now notify non-local classes of the new pause time (10 steps)
            firePropertyChangeEvent(new PropertyChangeEvent(this,
                CurrentProperties.MAX_TIME, null, new Integer(10)));

            // start the simulation
            CAController.getInstanceOfCAController().startCAInThread();
        }
        else if(command.equals(StartPanel.STEP_BACK))
        {
            // handle local GUI implications (e.g., change appearance of
            // buttons)
            controller.stepBackActions();

            // now notify non-local to rewind the CA (i.e., CAControllerListener
            // and the CAController)
            firePropertyChangeEvent(new PropertyChangeEvent(this,
                CurrentProperties.REWIND, null, new Boolean(true)));
        }
        else if(command.equals(StartPanel.STEP_FILL))
        {
            // get the number of rows
            int numRows = properties.getNumRows();

            // get a sample cell so we can see how many rows are being used as
            // initial state rows
            Cell sampleCell = ((Cell) (panel.getCAFrame().getLattice()
                .iterator().next()));
            int numberOfRowsTakenAsInitialState = sampleCell.getRule()
                .getRequiredNumberOfGenerations() - 1;

            // number of generations we need to increment
            int increment = numRows - 1 - numberOfRowsTakenAsInitialState;

            // handle local GUI implications (e.g., change appearance of
            // buttons)
            controller.startActions();

            // notify non-local classes of the new pause time (the number of
            // rows minus 1)
            firePropertyChangeEvent(new PropertyChangeEvent(this,
                CurrentProperties.MAX_TIME, null, new Integer(increment)));

            // start the simulation
            CAController.getInstanceOfCAController().startCAInThread();
        }
        else if(command.equals(StartPanel.RESET_STRING))
        {
            // handle local implications (e.g., change appearance of
            // buttons)
            controller.stopActions();

            // stop the simulation if it is running
            CAController.getInstanceOfCAController().stopCA();

            // now inform non-local classes that they need to
            // redo the setup (i.e., notify anyone who cares.)
            firePropertyChangeEvent(new PropertyChangeEvent(this,
                CurrentProperties.SETUP, null, new Boolean(true)));
        }
        else if(command.equals(CurrentProperties.LATTICE))
        {
            // get the new lattice, current rule and current init state
            String selectedLattice = (String) controller.getPropertiesPanel()
                .getLatticeChooser().getSelectedItem();

            // set the radius so that it matches the lattice (and reset the
            // radius in the Moore lattice, unless the Moore lattice was just
            // selected!)
            int radius = 1;
            if(selectedLattice.toLowerCase().contains("next"))
            {
                radius = 2;
            }
            if(!selectedLattice.equals(MooreRadiusOneDimLattice.DISPLAY_NAME)
                && !selectedLattice
                    .equals(MooreRadiusTwoDimLattice.DISPLAY_NAME)
                && !selectedLattice
                    .equals(VonNeumannRadiusLattice.DISPLAY_NAME))
            {
                MooreRadiusTwoDimLattice.radius = radius;
                controller.getPropertiesPanel().getRadiusField().setText(
                    radius + "");
            }

            // only show the radius if it is compatible
            // with the choice of lattice.
            controller.disableRadiusField();

            // only show the standard deviation if it is compatible
            // with the choice of lattice.
            controller.disableStandardDeviationField();

            // now adjust the rule menu tree to only enable rules compatible
            // with the lattice. LATTICE_CENTRIC_CHOICES IS USUALLY false
            if(CAConstants.LATTICE_CENTRIC_CHOICES)
            {
                controller.getRulePanel().getRuleTree().resetEnabledRules(
                    selectedLattice);

                // now deal with any changes necessitated by the current rule.
                // After changing the lattice, the currently selected rule on
                // the RuleTree may go from disabled to enabled, and that could
                // mean that we have to update other fields (like the numStates
                // field).

                // get the new rule and current init state
                String selectedRule = controller.getRulePanel().getRuleTree()
                    .getSelectedRuleName();
                String selectedInitState = (String) controller
                    .getInitialStatesPanel().getInitialState();

                // update the "for best results" and "highlighted rule
                // description" panels. Necessary because the new lattice
                // selection might make a rule accessible that was previously
                // selected but couldn't be used (grayed out).
                controller.getRulePanel().treeValueChanged();

                // only enable the running average field if the rule says to
                controller.disableRunningAverageField(false);

                // only enable the ruleNum field when is an IntegerRule class
                controller.disableRuleNumberField(false);

                // only enable the numStates field when the rule uses
                // NCellStates
                controller.disableNumStatesField(false);
                boolean numStatesEnabled = panel.getPropertiesPanel()
                    .getNumStatesField().isEnabled();

                // now inform non-local classes that the numStates field is
                // enabled or disabled (i.e., notify anyone who cares.)
                firePropertyChangeEvent(new PropertyChangeEvent(this,
                    CurrentProperties.NUM_STATES_TEXTFIELD_ENABLED, null,
                    new Boolean(numStatesEnabled)));

                // update the rule number tip -- a new rule means the
                // available rule numbers may change
                controller.updateRuleNumberTip();

                // update the initial states chooser so it includes any choices
                // specified by this rule
                controller.resetInitialStateChooser(selectedInitState,
                    selectedRule);

                // create a tabbed pane for any additional properties requested
                // by
                // the rule (and turn on the additional properties buttons).
                panel.getAdditionalPropertiesPanel().reset();
            }
        }
        else if(command.equals(StartPanel.CLEAR))
        {
            properties.setInitialState(CurrentProperties.STATE_BLANK);

            // take care of local implications for stopping
            controller.stopActions();

            // stop the simulation if it is running
            CAController.getInstanceOfCAController().stopCA();

            // Now notify anyone who cares that the graphics should be cleared.
            firePropertyChangeEvent(new PropertyChangeEvent(this,
                CurrentProperties.CLEAR, CurrentProperties.FALSE,
                CurrentProperties.TRUE));
        }
        else if(command.equals(Rule.ADDITIONAL_PROPERTIES))
        {
            // bring the additional properties tab to the fore (select it)
            for(int index = 0; index < panel.getTabbedPane().getTabCount(); index++)
            {
                if(panel
                    .getTabbedPane()
                    .getTitleAt(index)
                    .contains(
                        AdditionalPropertiesTabPanel.ADDITIONAL_PROPERTIES_TAB_TITLE))
                {
                    panel.getTabbedPane().setSelectedIndex(index);
                    panel.getTabbedPane().setEnabledAt(index, true);
                }
            }
        }
        else if(command.startsWith(RulePanel.ENLARGE_HTML))
        {
            panel.getRulePanel().createEnlargedHTMLView();
        }
        else if(command
            .startsWith(DescriptionPanel.ENLARGE_DESCRIPTION_ACTION_COMMAND))
        {
            panel.getDescriptionPanel().createEnlargedHTMLView();
        }
        else if(command.startsWith(RulePanel.RANDOM_NUMBER))
        {
            controller.chooseRandomRuleNumber();
        }
        else if(command.startsWith(Analysis.CLOSE_ANALYSIS))
        {
            // In this case, the command starts with CLOSE_ANALYSIS and is then
            // followed by the display name of the analysis.

            // the position of the substring that specifies which analysis to
            // close
            int analysisNameStart = Analysis.CLOSE_ANALYSIS.length();

            // get the name of the analysis
            String analysisDisplayName = command.substring(analysisNameStart);

            // now close it (note that there is no need to pause the simulation
            // -- when the analysis is closing, it will handle the pause).
            closeAnalysis(analysisDisplayName);
        }
        else if(command.startsWith(Analysis.SHOW_AS_FRAME))
        {
            Iterator analysisIterator = analysisList.iterator();
            while(analysisIterator.hasNext())
            {
                Analysis analysis = (Analysis) analysisIterator.next();
                String displayName = analysis.getDisplayName();
                if(command.endsWith(displayName))
                {
                    // display the analysis as a frame
                    controller.displayAnalysisAsFrame(analysis);
                }
            }
        }
        else if(command.startsWith(Analysis.SHOW_AS_TAB))
        {
            Iterator analysisIterator = analysisList.iterator();
            while(analysisIterator.hasNext())
            {
                Analysis analysis = (Analysis) analysisIterator.next();
                String displayName = analysis.getDisplayName();
                if(command.endsWith(displayName))
                {
                    // display the analysis as a tab
                    controller.displayAnalysisAsTab(analysis);
                }
            }
        }
        if(command.equals(StartPanel.UPDATE_AT_END_STRING))
        {
            properties.setUpdateAtEnd(true);

            // disable the increment spinner
            panel.getStartPanel().getGraphicsUpdateSpinner().setEnabled(false);

            // now inform non-local classes that they need to
            // update this property
            firePropertyChangeEvent(new PropertyChangeEvent(this,
                CurrentProperties.UPDATE_AT_END, null, new Boolean(true)));
        }
        if(command.equals(StartPanel.UPDATE_EVERY_STEP_STRING))
        {
            // set relevant properties
            properties.setDisplayStep(1);
            properties.setUpdateAtEnd(false);

            // disable the increment spinner
            panel.getStartPanel().getGraphicsUpdateSpinner().setEnabled(false);

            // now inform non-local classes that they need to
            // update this property
            firePropertyChangeEvent(new PropertyChangeEvent(this,
                CurrentProperties.DISPLAY_STEP, null, new Integer(1)));

            // now inform non-local classes that they need to
            // update this property
            firePropertyChangeEvent(new PropertyChangeEvent(this,
                CurrentProperties.UPDATE_AT_END, null, new Boolean(false)));
        }
        if(command.equals(StartPanel.UPDATE_INCREMENT_STRING))
        {
            JSpinner incrementSpinner = panel.getStartPanel()
                .getGraphicsUpdateSpinner();

            // enable the increment spinner
            incrementSpinner.setEnabled(true);

            // check the increment number and display an error message if
            // necessary
            if(checkGraphicsDisplayIncrement())
            {
                // read the increment from the display
                int increment = ((Integer) ((SpinnerNumberModel) incrementSpinner
                    .getModel()).getNumber()).intValue();

                // set properties
                properties.setDisplayStep(increment);
                properties.setUpdateAtEnd(false);

                // now inform non-local classes that they need to
                // update this property
                firePropertyChangeEvent(new PropertyChangeEvent(this,
                    CurrentProperties.DISPLAY_STEP, null,
                    new Integer(increment)));

                // now inform non-local classes that they need to
                // update this property
                firePropertyChangeEvent(new PropertyChangeEvent(this,
                    CurrentProperties.UPDATE_AT_END, null, new Boolean(false)));
            }
        }
        else
        {
            // is it a check box?
            JCheckBox[] checkBoxes = panel.getAnalysisPanel().getCheckBoxes();
            int boxNum = 0;
            while(boxNum < checkBoxes.length)
            {
                String analysisDescription = checkBoxes[boxNum]
                    .getActionCommand();
                if(command.equals(analysisDescription))
                {

                    // is it checked or unchecked?
                    if(checkBoxes[boxNum].isSelected())
                    {
                        // Pause the simulation. (Otherwise can get a concurrent
                        // modification error on the lattice which is being
                        // analyzed.)
                        firePropertyChangeEvent(new PropertyChangeEvent(this,
                            CurrentProperties.PAUSE, null, new Boolean(true)));

                        startAnalysis(analysisDescription);

                        // restart the simulation
                        firePropertyChangeEvent(new PropertyChangeEvent(this,
                            CurrentProperties.PAUSE, null, new Boolean(false)));
                    }
                    else
                    {
                        // no need to pause the simulation here -- the analysis
                        // itself will pause the simulation when it starts to
                        // close.
                        closeAnalysis(analysisDescription);
                    }

                    // exit the loop
                    boxNum = checkBoxes.length;
                }

                boxNum++;
            }

        }
    }

    /**
     * Closes the specified analysis.
     * 
     * @param analysisDescription
     *            The description of the analysis that will be closed.
     */
    private void closeAnalysis(String analysisDescription)
    {
        // the analysis we will close
        Analysis closeThisAnalysis = null;

        // get the analysis that needs closing from the list of
        // active analyses
        Iterator analysisIterator = analysisList.iterator();
        while(analysisIterator.hasNext())
        {
            // one of the active analyses
            Analysis analysis = (Analysis) analysisIterator.next();

            // is it the one that we want to close?
            if(analysis.getDisplayName().equals(analysisDescription))
            {
                closeThisAnalysis = analysis;

                // remove the analysis as a tab
                controller.removeTab(analysisDescription);

                // remove the checkbox mark
                controller.uncheckAnalysis(analysisDescription);

                // stop the analysis (which closes the frame!)
                if(panel.isShowing())
                {
                    // get the midpoint of the control panel
                    Point panelPoint = panel.getLocationOnScreen();
                    Point midPoint = new Point(panelPoint.x
                        + (int) (panel.getWidth() / 2.0), panelPoint.y
                        + (int) (panel.getHeight() / 2.0));

                    analysis.stop(midPoint);
                }
                else
                {
                    analysis.stop();
                }
            }
        }

        // remove the analysis from the list of active
        // analyses
        if(closeThisAnalysis != null)
        {
            analysisList.remove(closeThisAnalysis);
        }
    }

    /**
     * Starts the specified analysis.
     * 
     * @param analysisDescription
     *            The description of the analysis that will be started.
     */
    private void startAnalysis(String analysisDescription)
    {
        // get the analysis class that needs to be started
        AnalysisHash analysisHash = new AnalysisHash();
        String className = analysisHash.get(analysisDescription);
        Analysis analysis = ReflectionTool
            .instantiateFullAnalysisFromClassName(className);

        if(analysis != null)
        {
            // set a size for the tab
            Dimension size = panel.getPropertiesPanel().getInnerPanelSize();

            // display the analysis as a tab
            controller.addTab(analysis, analysis
                .getDisplayPanelForTabbedPane(size), analysis.getDisplayName(),
                analysis.getToolTipDescription());

            // make this class a listener for property change events
            // from the tab
            analysis.addFrameButtonListener(this);

            // add the analysis to a list of active analyses
            analysisList.add(analysis);

            // notify any non-local class that they need to start
            // this analysis
            firePropertyChangeEvent(new PropertyChangeEvent(this,
                CurrentProperties.START_ANALYSIS, null, analysis));
        }
        else
        {
            // warn the developer
            String warning = "A developer has added an analysis called \n "
                + "\"" + className + "\" \n"
                + "that has failed to properly instantiate. \n"
                + "Using this analysis may cause errors.";

            // make the JFrame look disabled
            panel.getCAFrame().setViewDisabled(true);

            JOptionPane.showMessageDialog(null, warning, "Developer Warning",
                JOptionPane.WARNING_MESSAGE);

            // make the JFrame look enabled
            panel.getCAFrame().setViewDisabled(false);
        }
    }

    /**
     * Adds a change listener.
     * 
     * @param listener
     *            the listener to add.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        listenerList.add(PropertyChangeListener.class, listener);
    }

    /**
     * Handles notifications that a Document such as a JTextField has changed.
     */
    public void changedUpdate(DocumentEvent e)
    {
        // not called by plain text fields
    }

    /**
     * All undocked analyses are docked.
     */
    public void dockAllAnalyses()
    {
        Iterator analysisIterator = analysisList.iterator();
        while(analysisIterator.hasNext())
        {
            Analysis analysis = (Analysis) analysisIterator.next();

            // display the analysis as a tab
            controller.displayAnalysisAsTab(analysis);
        }
    }

    /**
     * Notify listeners of a property change.
     * 
     * @param event
     *            Holds the changed property.
     */
    public void firePropertyChangeEvent(PropertyChangeEvent event)
    {
        EventListener[] listener = listenerList
            .getListeners(PropertyChangeListener.class);
        for(int i = 0; i < listener.length; i++)
        {
            ((PropertyChangeListener) listener[i]).propertyChange(event);
        }
    }

    /**
     * Handles notifications that a Document such as a JTextField has changed.
     */
    public void insertUpdate(DocumentEvent e)
    {
        Document eventDocument = e.getDocument();
        Document numStatesDocument = panel.getPropertiesPanel()
            .getNumStatesField().getDocument();

        // this can happen at start up if the numStates JTextField is set
        if(controller != null)
        {
            // time to update the rule number's tool tip
            // (its permitted rules can depend on the number of states).
            //
            // Ditto the min and max allowed rule numbers.
            //
            // Also update the init state panel (some init states depend on the
            // number of initial states.
            if(eventDocument.equals(numStatesDocument))
            {
                controller.updateRuleNumberTip();

                // update the initial states to reflect any changes
                panel.getInitialStatesPanel().resetActiveRadioButton();
            }
        }
        else
        {
            // don't change the submit button color at start up
            panel.getPropertiesPanel().resetSubmitButtonColorToDefault();
        }
    }

    /**
     * When running in the easy/facade mode, the default ".ca" simulation is
     * loaded.
     */
    public void loadFacadeSimulation()
    {
        // get the rule's class name
        String ruleDescription = controller.getRulePanel().getRuleTree()
            .getSelectedRuleName();
        RuleHash ruleHash = new RuleHash();
        String ruleClassName = ruleHash.get(ruleDescription);

        // just get the class name and remove the package info
        ruleClassName = ruleClassName
            .substring(ruleClassName.lastIndexOf(".") + 1);

        // the partial or relative path to the file
        String relativePath = "/" + CAConstants.FACADE_SIMULATIONS_FOLDER_NAME
            + "/" + ruleClassName + "." + CAConstants.CA_FILE_EXTENSION;

        // get the file path from the class name. Note the URIResource makes
        // sure that we can access files outside of the jar. The URI is better
        // than a URL because it doesn't escape spaces in the file path and
        // replace them with %20. The %20's mess up my XP operating system.
        // URL facadeFile =
        // URLResource.getUnescapedResourceFilePath(relativePath);
        // URI facadeFile = URIResource.getResource(relativePath);
        // Actually, only URL seems to work from the jar! If change here, then
        // also change in CAStateInitializer.setDataState().
        URL facadeFile = URLResource.getResource(relativePath);

        if(facadeFile == null || !facadeFile.getPath().contains(relativePath))
        {
            // issue a warning that there is no "easy" simulation associated
            // with this rule. Then bail.
            String warningMessage = "Sorry, there is no \"EZ\" or \"simplified\" simulation \n"
                + "available with this rule. \n\n"
                + "Please select another rule, or run the application in \n"
                + "the full-featured mode (press the \"All\" button).";
            WarningManager.displayWarningWithMessageDialog(warningMessage, 5,
                panel.getCAFrame().getFrame(), "No simplified simulation",
                JOptionPane.INFORMATION_MESSAGE, null);
        }
        else
        {
            // now load the simulation
            String facadeFilePath = facadeFile.getPath();

            // %20's can mess up my XP OS.
            // If this happened, then the URL didn't work. Try a URI. This
            // failure only happens on my laptop with the latest versions of the
            // JRE/JDK/Eclipse/XP Pro operating system. Go figure.
            if(facadeFilePath == null || facadeFilePath.contains("%20"))
            {
                // This may not work either, but it's worth a shot. Works on my
                // laptop.
                URI facadeFileTry2 = URIResource.getResource(relativePath);
                facadeFilePath = facadeFileTry2.getPath();
            }

            CAController.getInstanceOfCAController().loadSimulation(
                facadeFilePath);

            // when loading a .ca file for a facade, it might contain
            // "facade_is_on=false" in its properties. Obviously, we don't want
            // that, so this line immediately resets the value to true.
            CurrentProperties.getInstance().setFacade(true);

            // make the start button shimmy so the user knows to go here next
            CAController.getCAFrame().getToolBar().getStartButton()
                .startShaking(
                    ShimmyingTenTimesIconJButton.SUGGESTED_SHIMMYING_TIME);
        }

        // disable the additional properties panel
        for(int index = 0; index < panel.getTabbedPane().getTabCount(); index++)
        {
            if(panel.getTabbedPane().getTitleAt(index).contains(
                AdditionalPropertiesTabPanel.ADDITIONAL_PROPERTIES_TAB_TITLE))
            {
                panel.getTabbedPane().setEnabledAt(index, false);
            }
        }

        // disable the rule number and random rule number button
        panel.getRulePanel().getRuleNumberTextField().setEnabled(false);
        panel.getRulePanel().getRuleNumberRandomButton().setEnabled(false);
    }

    /**
     * Handles notification of any changes in properties.
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        if(event.getPropertyName().equals(CurrentProperties.START))
        {
            // Event fired by CAController and CAMenuBar.
            boolean start = ((Boolean) event.getNewValue()).booleanValue();

            if(start)
            {
                controller.startActions();
            }
            else
            {
                controller.stopActions();
            }
        }
        else if(event.getPropertyName().equals(CurrentProperties.STATUS))
        {
            // update the status label with the provided message
            String status = (String) event.getNewValue();
            if(status != null)
            {
                panel.getStatusPanel().setStatusLabel(status);
            }
        }
        else if(event.getPropertyName().equals(StatusPanel.GENERATION))
        {
            // update the generation on the status panel
            String generation = (String) event.getNewValue();
            if(generation != null)
            {
                String message = StatusPanel.GENERATION + " " + generation
                    + ".";
                panel.getStatusPanel().setGenerationLabel(message);
            }
        }
        else if(event.getPropertyName().equals(
            CurrentProperties.NUMBER_OF_STATES))
        {
            // the new value to be set
            Integer numberOfStates = (Integer) event.getNewValue();

            // the name of the rule that is requesting the change in the number
            // of states
            String nameOfRuleRequestingTheChange = (String) event.getOldValue();
            RuleHash ruleHash = new RuleHash();
            String classNameOfRuleRequestingTheChange = ruleHash
                .get(nameOfRuleRequestingTheChange);

            // the rule that has been selected on the rule panel, but not
            // necessarily submitted
            String selectedRuleDisplayName = (String) panel.getRulePanel()
                .getRuleTree().getSelectedRuleName();
            String classNameOfRuleSelectedOnRulePanel = null;
            if(selectedRuleDisplayName != null)
            {
                classNameOfRuleSelectedOnRulePanel = ruleHash
                    .get(selectedRuleDisplayName);
            }

            // the *class* name of the rule currently in the properties
            String classNameOfCurrentRuleInCAProperties = CurrentProperties
                .getInstance().getRuleClassName();

            // We need to set the new value in the properties panel, the status
            // panel and the CAProperties, but not necessarily all three!
            //
            // If the rule requesting the change matches the current (active)
            // rule in the properties, then update the status panel and CA
            // properties.
            if(classNameOfRuleRequestingTheChange
                .equals(classNameOfCurrentRuleInCAProperties))
            {
                CurrentProperties.getInstance().setNumStates(numberOfStates);

                panel.getStatusPanel().setCurrentNumberOfStatesLabel();
                // panel.getStatusPanel().getCurrentNumberOfStatesLabel().setText(
                // StatusPanel.NUM_STATES_STRING + numberOfStates);
            }

            // If the rule requesting the change matches the rule displayed on
            // the rule panel, then update the properties panel.
            if((classNameOfRuleSelectedOnRulePanel != null)
                && classNameOfRuleRequestingTheChange
                    .equals(classNameOfRuleSelectedOnRulePanel))
            {
                // update the properties panel
                panel.getPropertiesPanel().getNumStatesField().setText(
                    numberOfStates.toString());
            }

            // update the initial states to reflect any changes
            // panel.getInitialStatesPanel().resetActiveRadioButton();
        }
        else if(event.getPropertyName()
            .equals(CurrentProperties.RANDOM_PERCENT))
        {
            // the new value to be set
            Integer percent = (Integer) event.getNewValue();

            // the name of the rule that is requesting the change in the percent
            String nameOfRuleRequestingTheChange = (String) event.getOldValue();
            RuleHash ruleHash = new RuleHash();
            String classNameOfRuleRequestingTheChange = ruleHash
                .get(nameOfRuleRequestingTheChange);

            // the rule that has been selected on the properties panel, but not
            // necessarily submitted
            String selectedRuleDisplayName = (String) panel.getRulePanel()
                .getRuleTree().getSelectedRuleName();
            String classNameOfRuleSelectedOnPropertiesPanel = null;
            if(selectedRuleDisplayName != null)
            {
                classNameOfRuleSelectedOnPropertiesPanel = ruleHash
                    .get(selectedRuleDisplayName);
            }

            // the *class* name of the rule currently in the properties
            String classNameOfCurrentRuleInCAProperties = CurrentProperties
                .getInstance().getRuleClassName();

            // We need to set the new value in the properties panel and the
            // CAProperties, but not necessarily both!
            //
            // If the rule requesting the change matches the current (active)
            // rule in the properties, then update the CA
            // properties.
            if(classNameOfRuleRequestingTheChange
                .equals(classNameOfCurrentRuleInCAProperties))
            {
                CurrentProperties.getInstance().setRandomPercent(percent);
            }

            // If the rule requesting the change matches the rule displayed on
            // the rule panel, then update the initial state panel.
            if(classNameOfRuleSelectedOnPropertiesPanel != null
                && classNameOfRuleRequestingTheChange
                    .equals(classNameOfRuleSelectedOnPropertiesPanel))
            {
                // update the init states panel
                panel.getInitialStatesPanel().getRandomPercentSpinner()
                    .setValue(new Integer(percent));
            }
        }
        else if(event.getPropertyName().equals(CurrentProperties.RULE_NUMBER))
        {
            // the new value to be set
            String ruleNumber = event.getNewValue().toString();

            // the name of the rule that is requesting the change in the number
            // of states
            String nameOfRuleRequestingTheChange = (String) event.getOldValue();
            RuleHash ruleHash = new RuleHash();
            String classNameOfRuleRequestingTheChange = ruleHash
                .get(nameOfRuleRequestingTheChange);

            // the rule that has been selected on the properties panel, but not
            // necessarily submitted
            String selectedRuleDisplayName = (String) panel.getRulePanel()
                .getRuleTree().getSelectedRuleName();
            String classNameOfRuleSelectedOnPropertiesPanel = null;
            if(selectedRuleDisplayName != null)
            {
                classNameOfRuleSelectedOnPropertiesPanel = ruleHash
                    .get(selectedRuleDisplayName);
            }

            // the *class* name of the rule currently in the properties
            String classNameOfCurrentRuleInCAProperties = CurrentProperties
                .getInstance().getRuleClassName();

            // We need to set the new value in the properties panel, the status
            // panel, and the CAProperties, but not necessarily all three!
            //
            // If the rule requesting the change matches the current (active)
            // rule in the properties, then update the status panel and CA
            // properties.
            if(classNameOfRuleRequestingTheChange
                .equals(classNameOfCurrentRuleInCAProperties))
            {
                CurrentProperties.getInstance().setRuleNumber(
                    new BigInteger(ruleNumber));

                panel.getStatusPanel().setCurrentRuleLabel(
                    new BigInteger(ruleNumber));

                // panel.getStatusPanel().getCurrentRuleLabel().setText(
                // StatusPanel.RULE_STRING + selectedRuleDisplayName + ", "
                // + StatusPanel.RULE_NUMBER_STRING + ruleNumber);
            }

            // If the rule requesting the change matches the rule displayed on
            // the properties panel, then update the properties panel.
            if(classNameOfRuleSelectedOnPropertiesPanel != null
                && classNameOfRuleRequestingTheChange
                    .equals(classNameOfRuleSelectedOnPropertiesPanel))
            {
                // update the properties panel
                panel.getRulePanel().getRuleNumberTextField().setText(
                    ruleNumber);
            }
        }
        else if(event.getPropertyName()
            .equals(CurrentProperties.COLORS_CHANGED))
        {
            // update the initial states to reflect any changes in color
            panel.getInitialStatesPanel().resetActiveRadioButton();
        }
    }

    /**
     * Removes a change listener.
     * 
     * @param listener
     *            the listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        listenerList.remove(PropertyChangeListener.class, listener);
    }

    /**
     * Handles notifications that a Document such as a JTextField has changed.
     */
    public void removeUpdate(DocumentEvent e)
    {
        // just refer to another method
        insertUpdate(e);
    }

    /**
     * Set the controller of interactions between the listener and the panels.
     * 
     * @param controller
     *            Controller of interactions between the JPanels and the
     *            listener.
     */
    public void setController(AllPanelController controller)
    {
        this.controller = controller;
    }

    /**
     * Reacts to the fast/slow slider and the display increment spinner.
     */
    public void stateChanged(ChangeEvent e)
    {
        Object oSource = e.getSource();
        JSlider delaySlider = panel.getStartPanel().getDelaySlider();
        JSpinner incrementSpinner = panel.getStartPanel()
            .getGraphicsUpdateSpinner();

        if(oSource != null)
        {
            // see if it is the delay slider. Otherwise it's the increment
            // spinner.
            if((delaySlider != null) && (oSource instanceof JSlider))
            {
                // get the delay in *arbitrary units provided by the slider*
                int delayValue = delaySlider.getValue();

                // get the delay in *milliseconds*
                delayValue = panel.getStartPanel()
                    .convertSliderValueToMilliSeconds(delayValue);

                // only update the speed label while still adjusting
                if(delaySlider.getValueIsAdjusting())
                {
                    // set the label only
                    panel.getStartPanel().setSpeedLabel(delayValue);
                }
                else
                {
                    // not adjusting, so engage the delay
                    panel.getStartPanel().setTimeDelay(delayValue);

                    // disable rabbit and turtle icons if necessary
                    if(delaySlider.getValue() == delaySlider.getMinimum())
                    {
                        // at minimum delay, so disable rabbit
                        panel.getCAFrame().getToolBar().getRabbitButton()
                            .setEnabled(false);
                        panel.getCAFrame().getToolBar().getTurtleButton()
                            .setEnabled(true);
                    }
                    else if(delaySlider.getValue() == delaySlider.getMaximum())
                    {
                        // at maximum delay, so disable turtle
                        panel.getCAFrame().getToolBar().getRabbitButton()
                            .setEnabled(true);
                        panel.getCAFrame().getToolBar().getTurtleButton()
                            .setEnabled(false);
                    }
                    else
                    {
                        // enable both
                        panel.getCAFrame().getToolBar().getRabbitButton()
                            .setEnabled(true);
                        panel.getCAFrame().getToolBar().getTurtleButton()
                            .setEnabled(true);
                    }

                    // non-local implications
                    firePropertyChangeEvent(new PropertyChangeEvent(this,
                        CurrentProperties.TIME_DELAY, null, new Integer(panel
                            .getStartPanel().getTimeDelay())));
                }
            }
            else if((incrementSpinner != null) && (oSource instanceof JSpinner))
            {
                // check the increment number and display an error message if
                // necessary
                if(checkGraphicsDisplayIncrement())
                {
                    // read the increment from the display
                    Integer increment = (Integer) ((SpinnerNumberModel) incrementSpinner
                        .getModel()).getNumber();

                    // set a property
                    CurrentProperties.getInstance().setDisplayStep(increment);

                    // now inform non-local classes that they need to
                    // update this property
                    firePropertyChangeEvent(new PropertyChangeEvent(this,
                        CurrentProperties.DISPLAY_STEP, null, increment));
                }
            }
        }
    }

    // /**
    // * Detect that a mouse passed over a node of the tree.
    // */
    // public void mouseEntered(MouseEvent e)
    // {
    // // make sure the source was the rule tree
    // Object source = e.getSource();
    // JTree purportedTree = null;
    // try
    // {
    // purportedTree = (JTree) source;
    // }
    // catch(Exception error)
    // {
    // // not a JTree so bail
    // return;
    // }
    //
    // RuleTree ruleTree = controller.getRulePanel().getRuleTree();
    // JTree tree = ruleTree.getTree();
    // if(tree.equals(purportedTree))
    // {
    // // It was the rule tree, so update all the properties based on the
    // // location that the mouse entered. Location will be null if mouse
    // // wasn't actually "entered" exactly on a folder or leaf.
    // TreePath enteredPath = tree.getPathForLocation(e.getX(), e.getY());
    // if(enteredPath != null)
    // {
    // tree.get
    // }
    // }
    // }

    /**
     * Detect mouse clicks on the rule tree.
     * 
     * @param e
     *            The mouse event that fired this method.
     */
    public void mousePressed(MouseEvent e)
    {
        // make sure the source was the rule tree
        Object source = e.getSource();
        JTree purportedTree = null;
        try
        {
            purportedTree = (JTree) source;
        }
        catch(Exception error)
        {
            // not a JTree so bail
            return;
        }

        RuleTree ruleTree = controller.getRulePanel().getRuleTree();
        JTree tree = ruleTree.getTree();
        if(tree.equals(purportedTree))
        {
            // It was the rule tree, so update all the properties based on
            // the location that was clicked.
            TreePath clickedPath = null;
            boolean programmaticClick = false;
            if(e.getX() == -1 && e.getY() == -1)
            {
                // then this was a programmatic click rather than an actual
                // click -- the location is irrelevant because we know the rule
                // choice is valid
                programmaticClick = true;
            }
            else
            {
                // Location will be null if it wasn't actually clicked exactly
                // on a folder or leaf.
                clickedPath = tree.getPathForLocation(e.getX(), e.getY());
            }

            if(programmaticClick || clickedPath != null)
            {
                // get the selected rule
                String selectedRule = ruleTree.getSelectedRuleName();

                // will be null if it was a folder or disabled node
                if(selectedRule != null)
                {
                    // get the current init state
                    String selectedInitState = (String) controller
                        .getInitialStatesPanel().getInitialState();

                    // only enable the running average field if the rule says to
                    controller.disableRunningAverageField(false);

                    // only enable the ruleNum field when is an IntegerRule
                    // class
                    controller.disableRuleNumberField(false);

                    // only enable the numStates field when the rule uses
                    // IntegerCellStates
                    controller.disableNumStatesField(false);
                    boolean numStatesEnabled = panel.getPropertiesPanel()
                        .getNumStatesField().isEnabled();

                    // now inform non-local classes that the numStates field is
                    // enabled or disabled (i.e., notify anyone who cares.)
                    firePropertyChangeEvent(new PropertyChangeEvent(this,
                        CurrentProperties.NUM_STATES_TEXTFIELD_ENABLED, null,
                        new Boolean(numStatesEnabled)));

                    // update the rule number tip -- a new rule means the
                    // available rule numbers may change
                    controller.updateRuleNumberTip();

                    // update the initial states chooser so it includes any
                    // choices specified by this rule
                    controller.resetInitialStateChooser(selectedInitState,
                        selectedRule);

                    // reset the tabbed pane for any additional properties
                    // requested by the rule (and turn on the additional
                    // properties button). (Also turns on the additional
                    // properties button on the rule panel.)
                    AdditionalPropertiesTabPanel additionalPropertiesPanel = panel
                        .getAdditionalPropertiesPanel();
                    additionalPropertiesPanel.reset();

                    // and finally, if it was a double click, then submit.
                    if(e.getClickCount() == 2)
                    {
                        if(CurrentProperties.getInstance().isFacadeOn())
                        {
                            loadFacadeSimulation();
                        }
                        else
                        {
                            submitProperties(true);
                        }
                    }

                    // in EZ facade mode, disable the additional properties
                    // panel and disable the rule number (the previous
                    // submitproperties() might have enabled these).
                    if(CurrentProperties.getInstance().isFacadeOn())
                    {
                        // disable the additional properties
                        for(int index = 0; index < panel.getTabbedPane()
                            .getTabCount(); index++)
                        {
                            if(panel
                                .getTabbedPane()
                                .getTitleAt(index)
                                .contains(
                                    AdditionalPropertiesTabPanel.ADDITIONAL_PROPERTIES_TAB_TITLE))
                            {
                                panel.getTabbedPane()
                                    .setEnabledAt(index, false);
                            }
                        }

                        // disable the rule number and random rule number
                        // button.
                        panel.getRulePanel().getRuleNumberTextField()
                            .setEnabled(false);
                        panel.getRulePanel().getRuleNumberRandomButton()
                            .setEnabled(false);
                    }
                }
            }
        }
    }
}
