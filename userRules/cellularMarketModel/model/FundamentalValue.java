package userRules.cellularMarketModel.model;

import java.util.Random;

/**
 * Class contains stochastic methods for creating fundamental values of
 * commodities. Intrinsic or fundamental value is the perceived value of an
 * investment’s future cash flows, expected growth, and risk. The goal of the
 * value investor is to purchase assets at prices lower than the intrinsic or
 * fundamental value.
 * 
 * @author Kaleb
 * 
 */
public class FundamentalValue extends StoichasticCommodity
{
	/**
	 * Create a FundamentalValue object using the default scalar value of 100.
	 * The scalar is multiplied by a random double with a value 0 > x > 1, so
	 * the maximum value produced by the objects methods will be <= scalar.
	 */
	public FundamentalValue()
	{
		super();
	}

	/**
	 * Create a FundmentalValue object with a defined scalar. The scalar is
	 * multiplied by a random double with a value 0 > x > 1, so the maximum
	 * value produced by the objects methods will be <= scalar.
	 * 
	 * @param scalar
	 *            the desired value of the scalar.
	 */
	public FundamentalValue(int scalar)
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

		double fundamentalValue = scalar * r.nextDouble();

		return fundamentalValue;
	}
}
