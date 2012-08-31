/*
 AllImageTypeReader -- a class within the Cellular Automaton Explorer. 
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

package cellularAutomata.util.files;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;

import java.io.File;
import java.io.IOException;

import java.util.Iterator;

import javax.imageio.ImageIO;

import cellularAutomata.Cell;
import cellularAutomata.CurrentProperties;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.cellState.model.ComplexState;
import cellularAutomata.cellState.model.IntegerCellState;
import cellularAutomata.cellState.model.RealValuedState;
import cellularAutomata.cellState.view.CellStateView;
import cellularAutomata.util.math.Complex;

/**
 * Reads all supported images types.
 * 
 * @author David Bahr
 */
public class AllImageTypeReader
{
	// If I want additional file types, add them here. The types must be
	// supported by Java for reading.
	private final static String[] permittedImageTypes = AllImageFilter
			.getPermittedFileTypes();

	/**
	 * Decides if the file suffix is one of the supported image formats.
	 * 
	 * @param suffix
	 *            The suffix being tested, which should not include a "."
	 * @return true if the suffix matches one of the supported image formats.
	 */
	public static boolean isPermittedImageType(String suffix)
	{
		boolean supported = false;
		if(suffix != null)
		{
			for(int i = 0; i < permittedImageTypes.length; i++)
			{
				if(suffix.equalsIgnoreCase(permittedImageTypes[i]))
				{
					supported = true;
				}
			}
		}

		return supported;
	}

	/**
	 * Reads an image from a file and updates the cells to contain this info.
	 * Automatically converts the image to grey scale (or appropriate colors for
	 * the view). Also scales the image to the size of the current CA lattice.
	 * 
	 * @param filePath
	 *            The path to the image that will be read.
	 * @param lattice
	 *            The CA lattice.
	 * @throws InterruptedException
	 *             if cannot read the pixels in the file.
	 * @throws IOException
	 *             if cannot read the file.
	 */
	public static void read(String filePath, Lattice lattice)
			throws IOException, InterruptedException, Exception
	{
		// an array of colors from the current color scheme (used below)
		Color[] colors = null;

		// Read from a file
		File file = new File(filePath);
		Image image = ImageIO.read(file);

		// resample the image to the correct size
		int height = lattice.getHeight();
		int width = lattice.getWidth();
		image = image.getScaledInstance(width, height,
				BufferedImage.SCALE_REPLICATE);

		// now get pixels
		int[] pixels = new int[width * height];
		PixelGrabber pixelGrabber = new PixelGrabber(image, 0, 0, width,
				height, pixels, 0, width);

		// this puts the pixels into the array called "pixels"
		pixelGrabber.grabPixels();

		int pixelNum = 0;
		Iterator iterator = lattice.iterator();
		while(iterator.hasNext())
		{
			// get cell
			Cell cell = (Cell) iterator.next();

			// get the pixel for that cell
			int cellValue = 0;
			int pixelValue = pixels[pixelNum];
			int red = (pixelValue >> 16) & 0xff;
			int green = (pixelValue >> 8) & 0xff;
			int blue = (pixelValue) & 0xff;

			// the sum has a max of 3*255, so the number of potential states is
			// 3 * 255 + 1
			int numStates = 3 * 255 + 1;
			if(IntegerCellState.isCompatibleRule(cell.getRule()))
			{
				numStates = CurrentProperties.getInstance().getNumStates();

				// get the colors from the current color scheme
				if(colors == null)
				{
					colors = new Color[numStates];
					for(int stateValue = 0; stateValue < colors.length; stateValue++)
					{
						colors[stateValue] = CellStateView.colorScheme
								.getColor(stateValue, numStates, null);
					}
				}
			}
			else
			{
				// for complex and real-valued rules
				if(colors == null)
				{
					colors = new Color[numStates];
					for(int stateValue = 0; stateValue < colors.length; stateValue++)
					{
						double percent = (double) stateValue
								/ (double) numStates;
						colors[stateValue] = CellStateView.colorScheme
								.getColor(percent);
					}
				}
			}

			// using the current color scheme, find the closest color to
			// the pixel and assign it that state
			cellValue = 0;
			int minimumDifference = Integer.MAX_VALUE;
			for(int i = 0; i < colors.length; i++)
			{
				int colorSchemeRed = colors[i].getRed();
				int colorSchemeBlue = colors[i].getBlue();
				int colorSchemeGreen = colors[i].getGreen();

				int difference = (colorSchemeRed - red)
						* (colorSchemeRed - red) + (colorSchemeBlue - blue)
						* (colorSchemeBlue - blue) + (colorSchemeGreen - green)
						* (colorSchemeGreen - green);

				// replace the cell value if the pixel color is closer to this
				// color from the color scheme
				if(difference < minimumDifference)
				{
					minimumDifference = difference;
					cellValue = i;
				}
			}

			// TODO: improve below by putting abstract getFull() and abstract
			// getEmpty() in the CellState class. Then call those values and set
			// the number of states accordingly.
			// else if(ComplexState.isCompatibleRule(cell.getRule()))
			// {
			// // set the number of states to a value determined by the empty
			// // and full states
			// }
			// else if(RealValuedState.isCompatibleRule(cell.getRule()))
			// {
			// // set the number of states to a value determined by the empty
			// // and full states
			// }

			if(IntegerCellState.isCompatibleRule(cell.getRule()))
			{
				cell.getState().setStateFromString(String.valueOf(cellValue));
			}
			else if(ComplexState.isCompatibleRule(cell.getRule()))
			{
				// now convert the cell value to a number between the full and
				// empty cell state values
				ComplexState fullCellState = (ComplexState) cell.getRule()
						.getCompatibleCellState().clone();
				fullCellState.setToFullState();
				Complex fullState = fullCellState.getState();
				ComplexState emptyCellState = (ComplexState) cell.getRule()
						.getCompatibleCellState().clone();
				emptyCellState.setToEmptyState();
				Complex emptyState = emptyCellState.getState();

				Complex cellState = new Complex(
						emptyState.real
								+ cellValue
								* ((fullState.real - emptyState.real) / (double) numStates),
						emptyState.imaginary
								+ cellValue
								* ((fullState.imaginary - emptyState.imaginary) / (double) numStates));

				cell.getState().setStateFromString(
						String.valueOf(cellState.toString()));
			}
			else if(RealValuedState.isCompatibleRule(cell.getRule()))
			{
				// now convert the cell value to a number between the full and
				// empty cell state values
				RealValuedState fullCellState = (RealValuedState) cell
						.getRule().getCompatibleCellState().clone();
				fullCellState.setToFullState();
				double fullState = fullCellState.getState();
				RealValuedState emptyCellState = (RealValuedState) cell
						.getRule().getCompatibleCellState().clone();
				emptyCellState.setToEmptyState();
				double emptyState = emptyCellState.getState();

				double cellState = emptyState + cellValue
						* ((fullState - emptyState) / (double) numStates);

				cell.getState().setStateFromString(
						String.valueOf(String.valueOf(cellState)));
			}
			else
			{
				throw new NumberFormatException(
						"Image files are only compatible with number-based rules.");
			}

			// now use the number of states to convert "sum" to a number
			// between 0 and N-1 (inclusive) where N is the number of
			// states. Note that sum has a max value of 3*255, and a minimum
			// value of 0. But N-1 should be black (not white), so will need
			// to subtract the result from N-1. Therefore...
			// int sum = red + green + blue;
			// cellValue = (numStates - 1)
			// - (int) Math.round(((double) sum / (3.0 * 255.0))
			// * (numStates - 1));

			// jpeg images are "fuzzy" and can cause "improper" loading. To see
			// that, test load a two-tone jpg image on a rule with four states,
			// and uncomment the below line
			// System.out.println("AllImageTypeReader: sum = "+sum+", cellValue
			// = "+cellValue);

			// increment the pixel that we use
			pixelNum++;
		}
	}

	/**
	 * Reads an image from a file and converts it to a double array of the
	 * specified size. Automatically converts the image to grey scale (or
	 * appropriate colors for the view).
	 * 
	 * @param filePath
	 *            The path to the image that will be read.
	 * @param width
	 *            The width of the CA lattice.
	 * @param height
	 *            The height of the CA lattice.
	 * @param min
	 *            The minimum double value returned in the array (all pixels
	 *            will be rescaled using this value).
	 * @param max
	 *            The maximum double value returned in the array (all pixels
	 *            will be rescaled using this value).
	 * @return An array of doubles representing the image, and if the image is
	 *         two-dimensional, then the image[col][row] is stored in the array
	 *         as array[row * width + col]. Pixels in the original image are
	 *         assigned values between 0.0 (for color 0, 0, 0) and 1.0 (for
	 *         color 255, 255, 255), but this method returns values rescaled
	 *         between min and max.
	 * @throws InterruptedException
	 *             if cannot read the pixels in the file.
	 * @throws IOException
	 *             if cannot read the file.
	 * @throws Exception
	 *             if max < min.
	 */
	public static double[] readToDoubleArray(String filePath, int width,
			int height, double min, double max) throws IOException,
			InterruptedException, Exception
	{
		if(max < min)
		{
			throw new Exception("max < min");
		}

		// the color pixels have a max of 3*255, so the number of
		// potential states is 3 * 255 + 1 (see above method for details)
		int numStates = 3 * 255 + 1;
		int[] pixels = readToIntegerArray(filePath, width, height, numStates);

		double[] doublePixels = new double[pixels.length];
		for(int i = 0; i < doublePixels.length; i++)
		{
			doublePixels[i] = min + (max - min)
					* (((double) pixels[i]) / (double) numStates);
		}

		return doublePixels;
	}

	/**
	 * Reads an image from a file and converts it to an integer array of the
	 * specified size. Automatically converts the image to grey scale (or
	 * appropriate colors for the view).
	 * 
	 * @param filePath
	 *            The path to the image that will be read.
	 * @param width
	 *            The width of the CA lattice.
	 * @param height
	 *            The height of the CA lattice.
	 * @param numStates
	 *            The number of states in the CA (assumes that it has integer
	 *            states).
	 * @return An array of integers representing the image, and if the image is
	 *         two-dimensional, then the image[col][row] is stored in the array
	 *         as array[row * width + col].
	 * @throws InterruptedException
	 *             if cannot read the pixels in the file.
	 * @throws IOException
	 *             if cannot read the file.
	 */
	public static int[] readToIntegerArray(String filePath, int width,
			int height, int numStates) throws IOException, InterruptedException
	{
		// get the colors from the current color scheme
		// an array of colors from the current color scheme (used below)
		Color[] colors = new Color[numStates];
		for(int stateValue = 0; stateValue < colors.length; stateValue++)
		{
			colors[stateValue] = CellStateView.colorScheme.getColor(stateValue,
					numStates, null);
		}

		// Read from a file
		File file = new File(filePath);
		Image image = ImageIO.read(file);

		// resample the image to the correct size
		image = image.getScaledInstance(width, height,
				BufferedImage.SCALE_REPLICATE);

		// now get pixels
		int[] pixels = new int[width * height];
		PixelGrabber pixelGrabber = new PixelGrabber(image, 0, 0, width,
				height, pixels, 0, width);

		// this puts the pixels into the array called "pixels"
		pixelGrabber.grabPixels();

		for(int i = 0; i < pixels.length; i++)
		{
			int cellValue = 0;
			int pixelValue = pixels[i];
			int red = (pixelValue >> 16) & 0xff;
			int green = (pixelValue >> 8) & 0xff;
			int blue = (pixelValue) & 0xff;

			// using the current color scheme, find the closest color to
			// the pixel and assign it that state
			cellValue = 0;
			int minimumDifference = Integer.MAX_VALUE;
			for(int j = 0; j < colors.length; j++)
			{
				int colorSchemeRed = colors[j].getRed();
				int colorSchemeBlue = colors[j].getBlue();
				int colorSchemeGreen = colors[j].getGreen();

				int difference = (colorSchemeRed - red)
						* (colorSchemeRed - red) + (colorSchemeBlue - blue)
						* (colorSchemeBlue - blue) + (colorSchemeGreen - green)
						* (colorSchemeGreen - green);

				// replace the cell value if the pixel color is closer to this
				// color from the color scheme
				if(difference < minimumDifference)
				{
					minimumDifference = difference;
					cellValue = j;
				}
			}

			// the sum has a max of 3*255, so the number of potential states is
			// 3 * 255 + 1

			// now use the number of states to convert "sum" to a number
			// between 0 and N-1 (inclusive) where N is the number of
			// states. Note that sum has a max value of 3*255, and a minimum
			// value of 0. But N-1 should be black (not white), so will need
			// to subtract the result from N-1. Therefore...
			// int sum = red + green + blue;
			// cellValue = (numStates - 1)
			// - (int) Math.round(((double) sum / (3.0 * 255.0))
			// * (numStates - 1));

			// now store the result
			pixels[i] = cellValue;
		}

		return pixels;
	}
}
