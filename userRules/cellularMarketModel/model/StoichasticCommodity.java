package userRules.cellularMarketModel.model;

public abstract class StoichasticCommodity
{
	// The default scalar is 100.
	protected int scalar = 100;
	
	/**
	 * Create a CurrentMarketValue object using the default scalar value of 100.
	 * The scalar is multiplied by a random double with a value 0 > x > 1, so
	 * the maximum value produced by the objects methods will be <= scalar.
	 */
	protected StoichasticCommodity()
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
	protected StoichasticCommodity(int scalar)
	{
		super();
		this.scalar = scalar;
	}

	
	/**
	 * Create a randomly generated value.
	 * 
	 * @return the value of a commodity.
	 */
	public abstract double nextValue();
	
	/**
	 * Get the scalar value. The scalar is multiplied by a random double with a
	 * value 0 > x > 1, so the maximum value produced by the objects methods
	 * will be <= scalar.
	 * 
	 * @return
	 */
	public int getScalar()
	{
		return scalar;
	}

	/**
	 * Set the scalar value. The scalar is
	 * multiplied by a random double with a value 0 > x > 1, so the maximum
	 * value produced by the objects methods will be <= scalar.
	 * @param scalar
	 */
	public void setScalar(int scalar)
	{
		this.scalar = scalar;
	}
}
