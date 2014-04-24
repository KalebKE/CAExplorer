package userRules.cellularMarketModel.math;

public class Histogram
{
	public static double[] createHistogram(double[] data)
	{
		// find the number of bins using k = 1 + log2N
		int k = (int) (1 + Math.log(data.length));

		// create the bins
		double[] bins = new double[k];

		// find the maximum range
		double max = findMax(data);
		// the minimum range
		double min = findMin(data);

		// find the range
		double range = max - min;

		double binRange = range / ((double)k);
		
		int count = 0;
		
		// We need a N^2 run time to iterate over
		// all of the data AND iterate through the bins.
		for (int i = 0; i < data.length; i++)
		{
			for (int j = 0; j < bins.length; j++)
			{
				// If the data value is greater than the minimum binRange
				// and less than the maximum bin range for each bin.
				if (((data[i]) >= (min + (binRange * (double)j)))
						&& ((data[i]) <= (min + (binRange * ((double)(j + 1))))))
				{
					// Increment the bin count.
					bins[j]++;
					count++;
				}
			}
		}

		double numObservations = data.length;

		for (int i = 0; i < bins.length; i++)
		{
			bins[i] = (((double)bins[i]) / ((double)(numObservations)));
		}

		return bins;
	}

	private static double findMax(double[] data)
	{
		double max = 0;

		for (double x : data)
		{
			if (x > max)
			{
				max = x;
			}
		}
		return max;
	}

	private static double findMin(double[] data)
	{
		double min = data[0];

		for (double x : data)
		{
			if (x < min)
			{
				min = x;
			}
		}
		return min;
	}
}
