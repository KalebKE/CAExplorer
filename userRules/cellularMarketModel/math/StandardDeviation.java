package userRules.cellularMarketModel.math;

public class StandardDeviation
{
	/**
	 * The standard deviation is defined as the square root of the variance.
	 * @param population
	 *            an array, the sample
	 * @return the standard deviation
	 */
	public static double standardDeviation(double[] data)
	{
		return Math.sqrt(Variance.varianceKnuth(data));
	}
	
	/**
	 * The standard deviation is defined as the square root of the variance.
	 * @param population
	 *            an array, the population
	 * @return the standard deviation
	 */
	public static double standardDeviationPopulation(double[] population)
	{
		return Math.sqrt(Variance.varianceKnuth(population));
	}
}
