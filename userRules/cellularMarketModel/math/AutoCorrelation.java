package userRules.cellularMarketModel.math;

import org.math.array.StatisticSample;

/**
 * Autocorrelation plots (Box and Jenkins, pp. 28-32) are a commonly-used tool
 * for checking randomness in a data set. This randomness is ascertained by
 * computing autocorrelations for data values at varying time lags. If random,
 * such autocorrelations should be near zero for any and all time-lag
 * separations. If non-random, then one or more of the autocorrelations will be
 * significantly non-zero. In addition, autocorrelation plots are used in the
 * model identification stage for Box-Jenkins autoregressive, moving average
 * time series models.
 * 
 * For a description of the equations, see MarketModel Appendix: AutoCorrelation
 * 
 * @author Kaleb
 * 
 */
public class AutoCorrelation
{
	public static double[] autoCorrelation(double[] x, int lag)
	{
		double[] R = new double[x.length];
		double sum;
		double mean = StatisticSample.mean(x);

		for (int i = 0; i < x.length - lag; i++)
		{
			sum = 0;
			for (int j = 0; j < x.length - i; j++)
			{
				sum += (x[j] - mean) * (x[j + (i + (lag - 1))] - mean);
			}
			R[i] = (sum * 1 / (x.length)) / (StatisticSample.variance(x));
		}
		return R;
	}
}
