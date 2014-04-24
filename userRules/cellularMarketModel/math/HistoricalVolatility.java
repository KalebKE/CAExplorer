package userRules.cellularMarketModel.math;

import java.util.LinkedList;
import java.util.Queue;

/**
 * The historical volatility is the volatility of a series of stock prices where
 * you look back over the historical price path of the particular stock. A
 * common measure of dispersion is standard deviation. To enable us to compare
 * volatilities for different interval lengths we usually express volatility in
 * annual terms. To do this we scale this estimate with an annualization factor
 * (normalising constant) h which is the number of intervals per annum. If daily
 * data is used the interval is one trading day and we use h = 252 , if the
 * interval is a week, h = 52 and h = 12 for monthly data.
 * 
 * @author Kaleb
 * 
 */
public class HistoricalVolatility
{
	// The number of periods to be used for the rollingWindow.
	public static int numberOfPeriods = 100;

	/**
	 * Gets the volatility of a set of historical asset prices.
	 * 
	 * @param price A double array, the prices.
	 * 
	 * @return The volatility.
	 */
	public static double[][] getVolatilty(double[][] price)
	{
	
		double[][] volatilityReturn = new double[price.length][];
		double[][] logVol = LogReturn.calcLogReturn(price);
		
		Queue<Double> queue = new LinkedList<Double>();

		for (int i = 0; i < logVol.length; i++)
		{
			volatilityReturn[i] = new double[logVol[i].length];

			for (int j = 0; j < logVol[i].length; j++)
			{
				if (queue.size() > numberOfPeriods)
				{
					queue.remove();
				}

				queue.add((Double) logVol[i][j]);

				java.util.Iterator<Double> iterator = queue.iterator();

				int count = 0;

				double[] rollingWindow = new double[queue.size()];

				while (count < queue.size())
				{
					rollingWindow[count] = iterator.next();
					count++;
				}

				double standardDeviation = StandardDeviation
						.standardDeviation(rollingWindow);

				if (Double.isNaN(standardDeviation))
				{
					volatilityReturn[i][j] = 0;
				} 
				
				else
				{
					volatilityReturn[i][j] = standardDeviation * Math.sqrt(252);
				}

				// System.out.println(volatilityReturn[i][j]);
			}

		}

		return volatilityReturn;
	}
}
