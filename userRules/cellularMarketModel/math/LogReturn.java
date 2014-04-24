package userRules.cellularMarketModel.math;

/**
 * Class contains algorithms for calculating the log (natural log, e) return of
 * a asset price time series.
 * 
 * @author Kaleb
 * 
 */
public class LogReturn
{
	/**
	 * Calculates the log return of an asset price time series. This is defined
	 * as logReturn[i] = log(price[i+1]/price[i]) where i = a moment in time.
	 * 
	 * @param price
	 * @return
	 */
	public static double[][] calcLogReturn(double[][] price)
	{
		double[][] logReturn = new double[price.length][];

		for (int i = 0; i < price.length; i++)
		{
			logReturn[i] = new double[price[i].length - 1];

			for (int j = 0; j < price[i].length - 1; j++)
			{
				
				// Log return is calculated here.
				logReturn[i][j] = Math.log(price[i][j + 1] / price[i][j]);

				// If division by an extremely small number or 0 occurs,
				// the pricing data contains error.
				if (Double.isInfinite(logReturn[i][j])
						|| Double.isNaN(logReturn[i][j]))
				{
					logReturn[i][j] = 0.0;
				}
				// System.out.println(logReturn[i][j]);
			}
		}

		return logReturn;
	}
	
	/**
	 * Calculates the log return of an asset price time series. This is defined
	 * as logReturn[i] = log(price[i+1]/price[i]) where i = a moment in time.
	 * 
	 * @param price
	 * @return
	 */
	public static double[][] calcABSLogReturn(double[][] price)
	{
		double[][] logReturn = new double[price.length][];

		for (int i = 0; i < price.length; i++)
		{
			logReturn[i] = new double[price[i].length - 1];

			for (int j = 0; j < price[i].length - 1; j++)
			{
				
				// Log return is calculated here.
				logReturn[i][j] = Math.abs(Math.log(price[i][j + 1] / price[i][j]));

				// If division by an extremely small number or 0 occurs,
				// the pricing data contains error.
				if (Double.isInfinite(logReturn[i][j])
						|| Double.isNaN(logReturn[i][j]))
				{
					logReturn[i][j] = 0.0;
				}
				// System.out.println(logReturn[i][j]);
			}
		}

		return logReturn;
	}
}
