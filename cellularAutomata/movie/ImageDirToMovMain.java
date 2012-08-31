/*
 * @(#)ImageDirToMovMain.java  1.0.1  2008-06-27
 * 
 * Copyright (c) 2008 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 * 
 * The copyright of this software is owned by Werner Randelshofer. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Werner Randelshofer. For details see accompanying license terms
 */
package cellularAutomata.movie;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.*;

import cellularAutomata.util.CAFileChooser;

/**
 * Opens a file chooser and lets you choose a directory which must either
 * contain JPG images or PNG images and then writes them all into a QuickTime
 * movie file with 30 frames per second.
 *
 * @author Werner Randelshofer
 * @version 1.0.1 2008-06-27 Skip directories and hidden files when reading
 * the contents of the directory.
 * <br>1.0 2008-06-15 Created.
 */
public class ImageDirToMovMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                JFileChooser fc = new CAFileChooser("Choose directory with images");
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    final File dir = fc.getSelectedFile();
                    new Thread() {

                        @Override
                        public void run() {
                            try {
                                test(dir);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }.start();
                }
            }
        });
    }

    private static void test(File dir) throws IOException {
        File[] files = dir.listFiles(new FileFilter() {

            public boolean accept(File file) {
                return file.isFile() && ! file.isHidden();
            }
            
        });
        Arrays.sort(files);
        if (files.length > 0) {
            QuickTimeOutputStream out = null;
            try {
                QuickTimeOutputStream.VideoFormat format = null;
                if (files[0].getName().toLowerCase().endsWith("jpg")) {
                    format = QuickTimeOutputStream.VideoFormat.JPG;
                } else if (files[0].getName().toLowerCase().endsWith("png")) {
                    format = QuickTimeOutputStream.VideoFormat.PNG;
                } else {
                    throw new IOException("Unsupported file format");
                }
                BufferedImage img = ImageIO.read(files[0]);
                int imgWidth = img.getWidth();
                int imgHeight = img.getHeight();
                img.flush();

                File qtFile = new File(dir.getParentFile(), dir.getName() + ".mov");
                int count = 0;
                while (qtFile.exists()) {
                    count++;
                    qtFile = new File(dir.getParentFile(), dir.getName() + " " + count + ".mov");
                }

                out = new QuickTimeOutputStream(qtFile, format);
                out.setFrameRate(30);
                out.setVideoDimension(imgWidth, imgHeight);
                for (File f : files) {
                    if ((format == QuickTimeOutputStream.VideoFormat.JPG && f.getName().toLowerCase().endsWith("jpg") ||
                            format == QuickTimeOutputStream.VideoFormat.PNG && f.getName().toLowerCase().endsWith("png"))) {
                        System.out.println("adding " + f);
                        out.writeFrame(f, 1);
                    }
                }
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
    }
}
