package userRules.cellularMarketModel.math;

public class Variance
{
	/**
	 * Knuth's Variance Algorithm II for a sample of data. A non-memory
	 * intensive one-pass algorithm for calculating the variance of a sample.
	 * Calculates the variance in one pass. Very fast. This algorithm is much
	 * less prone to loss of precision due to massive cancellation, but might
	 * not be as efficient because of the division operation inside the loop.
	 * May experience loss of precision for very large data sets due to
	 * round-off error.
	 * 
	 * @param sample
	 *            An array, the sample.
	 * @return The variance.
	 */
	public static double varianceKnuth(double[] data)
	{
		int n = 0;
		double mean = 0;
		double s = 0.0;
		double[] test = data;

		for (double x : data)
		{
			n++;

			double delta = x - mean;

			mean += delta / (double) n;

			s += delta * (x - mean);

		}

		// if you want to calculate std deviation
		// of a population, change this to (s/(n))
		return (s / (n - 1));
	}

	/**
	 * Knuth's Variance Algorithm II for population data. A non-memory intensive
	 * one-pass algorithm for calculating the variance of a population.
	 * Calculates the variance in one pass. Very fast. This algorithm is much
	 * less prone to loss of precision due to massive cancellation, but might
	 * not be as efficient because of the division operation inside the loop.
	 * May experience loss of precision for very large data sets due to
	 * round-off error.
	 * 
	 * @param population
	 *            An array, the population.
	 * @return The variance.
	 */
	public static double varianceKnuthPopluation(double[] data)
	{
		int n = 0;
		double mean = 0;
		double s = 0.0;
		double[] test = data;

		for (double x : data)
		{
			n++;

			double delta = x - mean;

			mean += delta / (double) n;

			s += delta * (x - mean);

		}

		// if you want to calculate std deviation
		// of a sample, change this to (s/(n-1))
		return (s / (n));
	}

	/**
	 * A naive algorithm to calculate the estimated variance for a sample.
	 * Because sum_sqr and sum * mean can be very similar numbers, the precision
	 * of the result can be much less than the inherent precision of the
	 * floating-point arithmetic used to perform the computation. This is
	 * particularly bad if the standard deviation is small relative to the mean.
	 * The results of both of these simple algorithms (naive and two pass) can
	 * depend inordinately on the ordering of the data and can give poor results
	 * for very large data sets due to repeated roundoff error in the
	 * accumulation of the sums. Techniques such as compensated summation can be
	 * used to combat this error to a degree.
	 * 
	 * @param data
	 *            An array, the sample.
	 * @return The variance.
	 */
	public static double varianceNaive(double[] data)
	{
		double n = 0;
		double sum = 0;
		double sum_sqr = 0;

		for (double x : data)
		{
			n++;
			sum = sum + x;
			sum_sqr = sum_sqr + x * x;
		}

		double mean = sum / n;
		double variance = (sum_sqr - sum * mean) / (n - 1);

		return variance;
	}

	/**
	 * A naive algorithm to calculate the estimated variance for a population.
	 * Because sum_sqr and sum * mean can be very similar numbers, the precision
	 * of the result can be much less than the inherent precision of the
	 * floating-point arithmetic used to perform the computation. This is
	 * particularly bad if the standard deviation is small relative to the mean.
	 * The results of both of these simple algorithms (naive and two pass) can
	 * depend inordinately on the ordering of the data and can give poor results
	 * for very large data sets due to repeated roundoff error in the
	 * accumulation of the sums. Techniques such as compensated summation can be
	 * used to combat this error to a degree.
	 * 
	 * @param data
	 *            An array, the population.
	 * 
	 * @return The variance.
	 */
	public static double varianceNaivePopulation(double[] data)
	{
		double n = 0;
		double sum = 0;
		double sum_sqr = 0;

		for (double x : data)
		{
			n++;
			sum = sum + x;
			sum_sqr = sum_sqr + x * x;
		}

		double mean = sum / n;
		double variance = (sum_sqr - sum * mean) / (n - 1);

		return variance;
	}

	/**
	 * Two Pass for a sample. Algorithm uses a different formula for the
	 * variance than the naive algorithm; it first computes the sample mean and
	 * then computes the sum of the squares of the differences from the mean.
	 * This algorithm is often more numerically reliable than the naïve
	 * algorithm for large sets of data, although it can be worse if much of the
	 * data is very close to but not precisely equal to the mean and some are
	 * quite far away from it.This algorithm is often more numerically reliable
	 * than the naïve algorithm for large sets of data, although it can be worse
	 * if much of the data is very close to but not precisely equal to the mean
	 * and some are quite far away from it. The results of both of these simple
	 * algorithms (I and II) can depend inordinately on the ordering of the data
	 * and can give poor results for very large data sets due to repeated
	 * roundoff error in the accumulation of the sums. Techniques such as
	 * compensated summation can be used to combat this error to a degree.
	 * 
	 * @param data
	 *            An array, the sample.
	 * 
	 * @return data The variance.
	 */
	public static double varianceTwoPass(double[] data)
	{
		double n = 0;
		double sum1 = 0;
		double sum2 = 0;

		for (double x : data)
		{
			n++;
			sum1 = sum1 + x;
		}

		double mean = sum1 / n;

		for (double x : data)
		{
			sum2 = sum2 + (x - mean) * (x - mean);
		}

		double variance = sum2 / (n - 1);

		return variance;
	}

	/**
	 * Two Pass for a population. Algorithm uses a different formula for the
	 * variance than the naive algorithm; it first computes the sample mean and
	 * then computes the sum of the squares of the differences from the mean.
	 * This algorithm is often more numerically reliable than the naïve
	 * algorithm for large sets of data, although it can be worse if much of the
	 * data is very close to but not precisely equal to the mean and some are
	 * quite far away from it.This algorithm is often more numerically reliable
	 * than the naïve algorithm for large sets of data, although it can be worse
	 * if much of the data is very close to but not precisely equal to the mean
	 * and some are quite far away from it. The results of both of these simple
	 * algorithms (I and II) can depend inordinately on the ordering of the data
	 * and can give poor results for very large data sets due to repeated
	 * roundoff error in the accumulation of the sums. Techniques such as
	 * compensated summation can be used to combat this error to a degree.
	 * 
	 * @param data
	 *            An array, the sample.
	 * 
	 * @return data The variance.
	 */
	public static double varianceTwoPassPopulation(double[] data)
	{
		double n = 0;
		double sum1 = 0;
		double sum2 = 0;

		for (double x : data)
		{
			n++;
			sum1 = sum1 + x;
		}

		double mean = sum1 / n;

		for (double x : data)
		{
			sum2 = sum2 + (x - mean) * (x - mean);
		}

		double variance = sum2 / (n);

		return variance;
	}

	/**
	 * The compensated-summation version of the algorithm for a sample.
	 * 
	 * @param data
	 *            An array, the sample.
	 * @return The variance.
	 */
	public static double varianceCompensated(double[] data)
	{
		double n = 0;
		double sum1 = 0;
		for (double x : data)
		{
			n++;
			sum1 = sum1 + x;
		}
		double mean = sum1 / n;

		double sum2 = 0;
		double sum3 = 0;
		for (double x : data)
		{
			sum2 = sum2 + Math.pow((x - mean), 2);
			sum3 = sum3 + (x - mean);
		}

		double variance = (sum2 - Math.pow(sum3, 2) / n) / (n - 1);

		return variance;
	}

	/**
	 * The compensated-summation version of the algorithm for a population.
	 * 
	 * @param data
	 *            An array, the population.
	 * @return The variance.
	 */
	public static double varianceCompensatedPopulation(double[] data)
	{
		double n = 0;
		double sum1 = 0;
		for (double x : data)
		{
			n++;
			sum1 = sum1 + x;
		}
		double mean = sum1 / n;

		double sum2 = 0;
		double sum3 = 0;
		for (double x : data)
		{
			sum2 = sum2 + Math.pow((x - mean), 2);
			sum3 = sum3 + (x - mean);
		}

		double variance = (sum2 - Math.pow(sum3, 2) / n) / (n);

		return variance;
	}
}
