/*
 CAControllerListener -- a class within the Cellular Automaton Explorer. 
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;

import javax.swing.event.EventListenerList;

import cellularAutomata.analysis.Analysis;
import cellularAutomata.graphics.CAMenuBar;

/**
 * Listens for events fired from the GUI and takes appropriate action like
 * starting and stopping the simulation. The actions are taken by calling
 * methods in the CAController class.
 * <p>
 * This class acts as a bridge (bridge design pattern) between menu and graphics
 * classes and the CAController.
 * <p>
 * This class is most tightly coupled to the CAController, and hence the name.
 * However, this class really listens to the GUI and not to the CAController
 * class.
 * 
 * @author David Bahr
 */
public class CAControllerListener implements PropertyChangeListener
{
    // has methods to take action to run the CA
    private CAController controller = null;

    // allows us to add PropertyChangeListeners listeners to this class
    private EventListenerList listenerList = new EventListenerList();

    /**
     * Listens for events fired from the GUI.
     * 
     * @param controller
     *            Has methods that take action to run the CA.
     */
    public CAControllerListener(CAController controller)
    {
        this.controller = controller;
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
     * Handles notification of any changes in properties.
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        if(event.getPropertyName().equals(CurrentProperties.SAVE_DATA))
        {
            controller.saveData();
        }
        else if(event.getPropertyName().equals(CurrentProperties.EXIT))
        {
            controller.endProgram();
        }
        else if(event.getPropertyName().equals(CurrentProperties.SETUP))
        {
            // then setup
            controller.setupCA();
        }
        else if(event.getPropertyName().equals(CurrentProperties.TIME_DELAY))
        {
            int timeDelay = ((Integer) event.getNewValue()).intValue();
            controller.setTimeDelay(timeDelay);
        }
        else if(event.getPropertyName().equals(CurrentProperties.MAX_TIME))
        {
            int maxSteps = ((Integer) event.getNewValue()).intValue();
            controller.setMaxSteps(maxSteps);
        }
        else if(event.getPropertyName().equals(
        		CurrentProperties.IMPORT_SIMULATION))
        {
            String filePath = event.getNewValue().toString();
            controller.loadSimulation(filePath);
        }
        else if(event.getPropertyName().equals(CurrentProperties.INITIAL_STATE))
        {
            if(event.getNewValue().equals(CurrentProperties.STATE_IMAGE))
            {
                controller.loadImage();
            }
            else if(event.getNewValue().equals(CurrentProperties.STATE_DATA))
            {
                controller.loadData();
            }
            else
            {
                // importing new graphics for initial state (note this
                // requires rebuilding the lattice because the number of rows
                // and
                // columns may change).
                controller.importGraphics();
            }
        }
        else if(event.getPropertyName().equals(CurrentProperties.CLEAR))
        {
            controller.clearLattice();
        }
        else if(event.getPropertyName().equals(CurrentProperties.PAUSE))
        {
            boolean pause = ((Boolean) event.getNewValue()).booleanValue();
            if(pause)
            {
                controller.pauseCA();
            }
            else
            {
                controller.unPauseCA();
            }
        }
        else if(event.getPropertyName().equals(CAMenuBar.PRINT))
        {
            controller.print();
        }
        else if(event.getPropertyName().equals(CAMenuBar.SAVE_AS_IMAGE))
        {
            controller.saveAsImage();
        }
        else if(event.getPropertyName().equals(CurrentProperties.START_ANALYSIS))
        {
            Analysis analysis = (Analysis) event.getNewValue();
            controller.startAnalysis(analysis);
        }
        else if(event.getPropertyName().equals(CurrentProperties.STOP_ANALYSIS))
        {
            Analysis analysis = (Analysis) event.getNewValue();
            controller.stopAnalysis(analysis);
        }
        else if(event.getPropertyName()
            .equals(CurrentProperties.UPDATE_GRAPHICS))
        {
            controller.updateGraphicsPanel(); // same as refreshGraphics()
        }
        else if(event.getPropertyName().equals(
        		CurrentProperties.REFRESH_GRAPHICS))
        {
                controller.refreshCAGraphics();
        }
        else if(event.getPropertyName().equals(CurrentProperties.DISPLAY_STEP))
        {
            int displayInterval = ((Integer) event.getNewValue()).intValue();
            controller.setUpdateGraphicsEveryNSteps(displayInterval);
        }
        else if(event.getPropertyName().equals(CurrentProperties.UPDATE_AT_END))
        {
            boolean atEnd = ((Boolean) event.getNewValue()).booleanValue();
            controller.setUpdateGraphicsAtEnd(atEnd);
        }
        else if(event.getPropertyName().equals(CurrentProperties.REWIND))
        {
            controller.rewindCA();
        }
        else if(event.getPropertyName().equals(Cell.TAGGED_EVENT))
        {
            controller.setViewChanged(true);
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
}
