/*
 FileWriter -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.util.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import cellularAutomata.util.CAFileChooser;

/**
 * Assists with writing data to text files.
 * 
 * @author David Bahr
 */
public class FileWriter
{
    /**
     * A string used for the "Save as ..." title in the file selection.
     */
    private final static String SAVE_AS = "Save As ...";

    // The file where the data will be saved.
    private File file = null;

    // allows writing to the specified file
    private PrintWriter fileWriter = null;

    /**
     * Create a "file writer" that writes data to text files. If the file path
     * is null, the user will be prompted to select a file from a file chooser.
     * 
     * @param file
     *            The File where data will be saved. May be null.
     * 
     * @throws IOException
     *             if the specified file cannot be located.
     */
    public FileWriter(File file) throws IOException
    {
        if(file != null)
        {
            this.file = file;
        }
        else
        {
            this.file = selectFile(null);
        }

        fileWriter = getFileWriter(file);
    }

    /**
     * Create a "file writer" that writes data to text files. The user will be
     * prompted to select a file from a file chooser, and the file chooser will
     * default to the folder specified as <code>defaultFolder</code>. If the
     * <code>defaultFolder</code> is null, then the file chooser will point to
     * the user's default directory.
     * 
     * @param defaultFolder
     *            The default folder where the file will be saved. May be null.
     * 
     * @throws IOException
     *             if the user does not select a valid file (for example cancels
     *             the selection).
     */
    public FileWriter(String defaultFolder) throws IOException
    {
        this.file = selectFile(defaultFolder);
        fileWriter = getFileWriter(file);
    }

    /**
     * Gets a file writer to the specified file.
     * 
     * @param file
     *            The file to which data will be written.
     * 
     * @return A writer to the file.
     * 
     * @throws IOException
     *             if the specified file cannot be opened.
     */
    private PrintWriter getFileWriter(File file) throws IOException
    {
        if(file != null && fileWriter == null)
        {
            FileOutputStream outputStream = new FileOutputStream(file.getPath());
            fileWriter = new PrintWriter(outputStream);
        }

        return fileWriter;
    }

    /**
     * Asks the user to select a file from a JFileChooser.
     * 
     * @param defaultFolder
     *            The folder to which the file chooser will initially point. May
     *            be null, in which case the user's default directory is used.
     * 
     * @throws FileNotFoundException
     *             if the user does not select a valid file (for example cancels
     *             the selection).
     * 
     * @return The file selected by the user.
     */
    private File selectFile(String defaultFolder) throws FileNotFoundException
    {
        // let user choose a file to save the data
        // This is the default folder that it goes to.
        File file = null;
        if(defaultFolder != null)
        {
            file = new File(defaultFolder);
        }
        int state = 0;

        boolean chooseAnotherFile = true;
        while(chooseAnotherFile)
        {
            // let user choose a file to save the data
            JFileChooser fileChooser = null;
            if(file != null)
            {
                fileChooser = new CAFileChooser(file);
            }
            else
            {
                fileChooser = new CAFileChooser();
            }
            fileChooser.setDialogTitle(SAVE_AS);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            // only allow these files
            JPGAndPNGAndOtherFileTypeFilter txtFilter = new JPGAndPNGAndOtherFileTypeFilter("txt",
                "Text file (*.txt)");
            fileChooser.addChoosableFileFilter(txtFilter);
            fileChooser.setFileFilter(txtFilter);

            state = fileChooser.showSaveDialog(null);
            file = fileChooser.getSelectedFile();

            if((file != null) && (state == JFileChooser.APPROVE_OPTION))
            {
                String filePath = file.getPath();
                if(!JPGAndPNGAndOtherFileTypeFilter.getSuffix(filePath).equals(
                    "txt"))
                {
                    filePath += ".txt";
                    file = new File(filePath);
                }
                
                if(file.exists())
                {
                    String message = "The file \"" + file.getName()
                        + "\" exists.  Overwrite this file?";
                    int answer = JOptionPane.showConfirmDialog(null, message,
                        "Replace", JOptionPane.YES_NO_CANCEL_OPTION);
                    if(answer == JOptionPane.YES_OPTION)
                    {
                        chooseAnotherFile = false;
                    }
                }
                else
                {
                    chooseAnotherFile = false;
                }
            }
            else
            {
                // user wants to bail
                chooseAnotherFile = false;
            }
        }

        if(file == null)
        {
            throw new FileNotFoundException("The user did not select a file.");
        }

        return file;
    }

    /**
     * Close the file. Any further attempt to write to the file will throw an
     * exception.
     */
    public void close()
    {
        if(fileWriter != null)
        {
            fileWriter.close();
        }

        // set to null, so can be re-opened later (by getFileWriter())
        fileWriter = null;
    }

    /**
     * Writes the string to the file, and does not advance the file to a new
     * line.
     * 
     * @param data
     *            The data that will be saved in the file.
     * 
     * @throws FileNotFoundException
     *             If the user did not specify a valid file in the class
     *             constructor or via the file chooser.
     */
    public void writeData(String data) throws FileNotFoundException
    {
        if(fileWriter != null)
        {
            fileWriter.println(data);
        }
        else
        {
            throw new FileNotFoundException("The user did not select a file.");
        }
    }

    /**
     * Writes the data to the file.
     * 
     * @param data
     *            An array of data that will be saved in the file. Each element
     *            is separated by the specified delimiter.
     * @param delimiter
     *            Each element of the data array will separated by this
     *            delimiter when written to the file. If null, the data will be
     *            tab delimited.
     * 
     * @throws FileNotFoundException
     *             If the user did not specify a valid file in the class
     *             constructor or via the file chooser.
     */
    public void writeData(double[] data, String delimiter)
        throws FileNotFoundException
    {
        if(data != null && data.length > 0)
        {
            String[] stringData = new String[data.length];
            for(int i = 0; i < data.length; i++)
            {
                stringData[i] = "" + data[i];
            }

            writeData(stringData, delimiter);
        }
    }

    /**
     * Writes the data to the file.
     * 
     * @param data
     *            An array of data that will be saved in the file. Each element
     *            is separated by the specified delimiter.
     * @param delimiter
     *            Each element of the data array will separated by this
     *            delimiter when written to the file. If null, the data will be
     *            tab delimited.
     * 
     * @throws FileNotFoundException
     *             If the user did not specify a valid file in the class
     *             constructor or via the file chooser.
     */
    public void writeData(int[] data, String delimiter)
        throws FileNotFoundException
    {
        if(data != null && data.length > 0)
        {
            String[] stringData = new String[data.length];
            for(int i = 0; i < data.length; i++)
            {
                stringData[i] = "" + data[i];
            }

            writeData(stringData, delimiter);
        }
    }

    /**
     * Writes the data to the file.
     * 
     * @param data
     *            An array of data that will be saved in the file. Each element
     *            is separated by the specified delimiter.
     * @param delimiter
     *            Each element of the data array will separated by this
     *            delimiter when written to the file. If null, the data will be
     *            tab delimited.
     * 
     * @throws FileNotFoundException
     *             If the user did not specify a valid file in the class
     *             constructor or via the file chooser.
     */
    public void writeData(Object[] data, String delimiter)
        throws FileNotFoundException
    {
        if(data != null && data.length > 0)
        {
            String[] stringData = new String[data.length];
            for(int i = 0; i < data.length; i++)
            {
                stringData[i] = data[i].toString();
            }

            writeData(stringData, delimiter);
        }
    }

    /**
     * Writes the data to the file.
     * 
     * @param data
     *            An array of data that will be saved in the file. Each element
     *            is separated by the specified delimiter.
     * @param delimiter
     *            Each element of the data array will separated by this
     *            delimiter when written to the file. If null, the data will be
     *            tab delimited.
     * 
     * @throws FileNotFoundException
     *             If the user did not specify a valid file in the class
     *             constructor or via the file chooser.
     */
    public void writeData(String[] data, String delimiter)
        throws FileNotFoundException
    {
        if(fileWriter != null)
        {
            if(data != null)
            {
                // create a default delimiter if necessary
                if(delimiter == null)
                {
                    delimiter = "\t";
                }

                for(int i = 0; i < data.length; i++)
                {
                    if(i < data.length - 1)
                    {
                        // separate each element by the delimeter
                        fileWriter.print(data[i]);
                        fileWriter.print(delimiter);
                    }
                    else
                    {
                        // end the line
                        fileWriter.println(data[i]);
                    }
                }
            }
        }
        else
        {
            throw new FileNotFoundException("The user did not select a file.");
        }
    }
    
    /**
     * Writes the string to a line of the file. The file is advanced to a new
     * line after printing the string.
     * 
     * @param data
     *            The data that will be saved in the file.
     * 
     * @throws FileNotFoundException
     *             If the user did not specify a valid file in the class
     *             constructor or via the file chooser.
     */
    public void writeDataLine(String data) throws FileNotFoundException
    {
        if(fileWriter != null)
        {
            fileWriter.println(data);
        }
        else
        {
            throw new FileNotFoundException("The user did not select a file.");
        }
    }
}
