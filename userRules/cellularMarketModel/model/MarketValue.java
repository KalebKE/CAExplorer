package userRules.cellularMarketModel.model;

import java.util.Random;

/**
 * Class contains stochastic methods for creating current market values of
 * commodities. The intrinsic value may be significantly different from the
 * market value or price of the investment. The market price is the price you
 * can buy and sell the asset. Buyers and sellers have many different ways of
 * measuring value and various reasons for buying and selling an asset. The
 * result is an asset may sell at a price significantly below or above its
 * perceived intrinsic or fundamental value.
 * 
 * @author Kaleb
 * 
 */
public class MarketValue extends StoichasticCommodity
{
	/**
	 * Create a CurrentMarketValue object using the default scalar value of 100.
	 * The scalar is multiplied by a random double with a value 0 > x > 1, so
	 * the maximum value produced by the objects methods will be <= scalar.
	 */
	public MarketValue()
	{
		super();
	}

	/**
	 * Create a CurrentMarketValue object with a defined scalar. The scalar is
	 * multiplied by a random double with a value 0 > x > 1, so the maximum
	 * value produced by the objects methods will be <= scalar.
	 * 
	 * @param scalar
	 *            the desired value of the scalar.
	 */
	public MarketValue(int scalar)
	{
		super(scalar);
	}

	/**
	 * Create a randomly generated fundamental commodity value.
	 * 
	 * @return the value of a commodity.
	 */
	@Override
	public double nextValue()
	{
		Random r = new Random();

		double currentMarketValue = scalar * r.nextDouble();

		return currentMarketValue;
	}

}
