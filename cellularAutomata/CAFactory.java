/*
 CAFactory -- a class within the Cellular Automaton Explorer. 
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

import cellularAutomata.cellState.model.CellState;
import cellularAutomata.io.FileStorage;
import cellularAutomata.lattice.Lattice;
import cellularAutomata.lattice.MooreRadiusOneDimLattice;
import cellularAutomata.lattice.MooreRadiusTwoDimLattice;
import cellularAutomata.lattice.OneDimensionalLattice;
import cellularAutomata.lattice.RandomGaussianLattice;
import cellularAutomata.lattice.VonNeumannRadiusLattice;
import cellularAutomata.lattice.view.LatticeView;
import cellularAutomata.reflection.LatticeHash;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.util.MemoryManagementTools;

/**
 * Creates a factory for instantiating the CA lattice, lattice graphics, rule,
 * and file storage. These classes are not related (by inheritance) but they
 * should be instantiated together because they cooperate as a unit.
 * 
 * @author David Bahr
 */
public class CAFactory extends AbstractCAFactory
{
	/**
	 * A suggested minimum for the number of steps that the CA can rewind,
	 * expressed as a fractional percentage of the number of rows in the
	 * simulation. In other words, 1.0 means 100% of the rows, and 2.0 means
	 * 200% of the rows.
	 */
	public final static double REWIND_PERCENTAGE = 2.0;

	/**
	 * The default number of steps that the CA will rewind if not specified
	 * elsewhere.
	 */
	public static final int DEFAULT_REWIND_STEPS = 10;

	/**
	 * The amount of memory being used by the current lattice.
	 */
	public static long currentLatticeMemory = 0;

	// the percentage of available memory that we are willing to let the lattice
	// use when it is created.
	private static final double PERCENT_MEMORY_CAN_USE = 0.8;

	// the maximum size of the lattice before the number of rewind steps is
	// limited (only in two dimensions)
	private static final int MAX_CELLS_BEFORE_LIMIT_REWIND = 200 * 200;

	// the maximum number of rewind steps for two dim lattices that are larger
	// than MAX_CELLS_BEFORE_LIMIT_REWIND
	private static final int MAX_REWIND_STEPS = 10;

	// The compatible two-dimensional classes
	private FileStorage dataStorage = null;

	private LatticeView graphicsPanel = null;

	private Lattice lattice = null;

	/**
	 * Create a two-dimensional factory using parameters set from the
	 * properties.
	 */
	public CAFactory()
	{
		super();
	}

	/**
	 * @see cellularAutomata.CAFactory#getDataStorage()
	 */
	public FileStorage getDataStorage()
	{
		// set the lattice if necessary
		if(lattice == null)
		{
			// no need to use the returned value
			getLattice();
		}

		dataStorage = lattice.getFileStorage();

		return dataStorage;
	}

	/**
	 * Gets the panel that draws the CA.
	 * 
	 * @return The panel that draws the CA (does not include any other graphics
	 *         such as the control panel).
	 */
	public LatticeView getGraphicsPanel()
	{
		// set the lattice if necessary
		if(lattice == null)
		{
			// no need to use the returned value
			getLattice();
		}

		graphicsPanel = lattice.getView();

		return graphicsPanel;
	}

	// private static int timesCalled = 0;

	/**
	 * @see cellularAutomata.CAFactory#getLattice()
	 */
	public Lattice getLattice()
	{
		CurrentProperties properties = CurrentProperties.getInstance();

		// decide what kind of lattice to use
		String latticeType = properties.getLatticeDisplayName();

		// Create a list of possible lattices (dynamically using reflection).
		// These are stored statically in the class, ready for later use, so if
		// this has been instantiated before, then this will be quick.
		LatticeHash latticeHash = new LatticeHash();

		// before creating the lattice, set its radius. This must happen before
		// instantiation so that the number of neighbors is correctly set. (This
		// is annoyingly high connectivity, but is a consequence of the lattice
		// classes being instantiated without the properties in some cases --
		// there is no way for the lattice to read the properties to get the
		// radius.)
		int radius = properties.getNeighborhoodRadius();
		MooreRadiusOneDimLattice.radius = radius;
		MooreRadiusTwoDimLattice.radius = radius;
		VonNeumannRadiusLattice.radius = radius;

		// ditto above, but for the standard deviation
		double stdev = properties.getStandardDeviation();
		RandomGaussianLattice.standardDeviation = stdev;

		// create the lattice
		String latticeClassName = latticeHash.get(latticeType);
		if(latticeClassName == null)
		{
			String message = "The properties file contains a lattice name "
					+ "that does\n"
					+ "not exist.  Please update or delete the properties file,\n"
					+ CAConstants.DEFAULT_PROPERTIES_FILE + ".";
			throw new RuntimeException(message);
		}

		// the number of steps that the CA will be allowed to rewind (ideally).
		// This may be reduced if memory allocation becomes a problem.
		int numRows = properties.getNumRows();
		int numCols = properties.getNumColumns();
		int numberOfRewindSteps = (int) REWIND_PERCENTAGE * numRows;
		if(!OneDimensionalLattice.isCurrentLatticeOneDim(latticeType)
				&& (numRows * numCols > MAX_CELLS_BEFORE_LIMIT_REWIND))
		{
			numberOfRewindSteps = Math.min(MAX_REWIND_STEPS,
					numberOfRewindSteps);
		}

		// set a property saying how many steps we can rewind -- this is
		// read by the cell for example.
		properties.setRewindSteps(numberOfRewindSteps);

		// how much memory is in use before creating the lattice
		long memoryUseBeforeLattice = MemoryManagementTools
				.getMemoryCurrentlyInUse();

		// keep trying to create the lattice until there is enough memory (or
		// until the memory can't be reduced by cutting the number of stored
		// states for the rewind button).
		boolean tooLittleMemory = true;
		while(tooLittleMemory)
		{
			// timesCalled++;
			// System.out.println("CAFactory: timesCalled = " + timesCalled);

			// have to get this here because it could change with each iteration
			// through this loop (if the first attempt tried to load an invalid
			// file or image).
			String initialStateFilePath = properties.getInitialState();

			// set lattice to null and garbage collect -- this frees up any
			// memory from previous lattices
			lattice = null;
			System.gc();

			// how much memory is in use before creating the lattice
			memoryUseBeforeLattice = MemoryManagementTools
					.getMemoryCurrentlyInUse();

			// note this might fail if there is a problem with memory.
			// In that case the lattice will be null.
			lattice = ReflectionTool.getLatticeFromClassName(latticeClassName,
					initialStateFilePath, getRule());

			// clean up any resources used when making the lattice
			System.gc();

			// how much memory is in use after creating the lattice
			long memoryUseAfterLattice = MemoryManagementTools
					.getMemoryCurrentlyInUse();

			// memory used by the lattice
			long memoryUsed = memoryUseAfterLattice - memoryUseBeforeLattice;

			// we will need a multiple of that memory to "rewind" the CA. i.e.,
			// a copy of the lattice is stored for each step that the CA can be
			// rewound.
			long amountOfAdditionalMemoryRequired = numberOfRewindSteps
					* memoryUsed;

			// did the lattice take too much memory?
			long memoryAvailable = MemoryManagementTools.getMemoryAvailable();
			if(lattice == null && numberOfRewindSteps <= 0)
			{
				// it failed but there is nothing we can do here. Need a smaller
				// lattice.
				tooLittleMemory = false;
			}
			else if((amountOfAdditionalMemoryRequired >= PERCENT_MEMORY_CAN_USE
					* memoryAvailable)
					|| (lattice == null))
			{
				tooLittleMemory = true;

				if(lattice != null)
				{
					// so reduce the number of rewind steps
					numberOfRewindSteps = (int) (PERCENT_MEMORY_CAN_USE
							* memoryAvailable / memoryUsed);
				}
				else
				{
					// if the lattice is null, it will free up memory -- but
					// that's a ruse. It's null because it took up too much
					// memory! So reduce the number of steps without using the
					// erroneous memory calculation.
					numberOfRewindSteps /= 2;
				}

				// reset the property saying how many steps we can rewind --
				// this is read by the cell for example.
				properties.setRewindSteps(numberOfRewindSteps);
			}
			else
			{
				// it worked -- exit the loop!
				tooLittleMemory = false;
			}
		}

		// how much memory did the lattice finally use
		currentLatticeMemory = MemoryManagementTools.getMemoryCurrentlyInUse()
				- memoryUseBeforeLattice;

		// Reset any warnings that were suppressed during the instantiation of
		// the lattice. Warnings are suppressed if the instantiation would
		// generate multiple copies of the same warning (that the cell state
		// can't be instantiated as desired). This generally happens when
		// loading an invalid data or image file.
		CellState.resetWarnings();

		return lattice;
	}
}
