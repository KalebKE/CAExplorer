package userRules.cellularMarketModel.model;

/**
 * Class contains stochastic methods for creating transaction quantities for
 * commodities.
 * 
 * @author Kaleb
 * 
 */
public abstract class StoichasticTransaction
{
	public int transactionLimit = 1000;

	/**
	 * Create StoichasticTransaction object with a default transaction limit of
	 * 1000. The transaction limit caps the maximum random int that will be
	 * produced at the value defined by transactionLimit.
	 */
	protected StoichasticTransaction()
	{
		super();
	}

	/**
	 * Create StoichasticTransaction object with a defined transaction limit.
	 * The transaction limit caps the maximum random int that will be produced
	 * at the value defined by transactionLimit.
	 * 
	 * @param transactionLimit
	 *            the desired transaction limit.
	 */
	protected StoichasticTransaction(int transactionLimit)
	{
		super();
		this.transactionLimit = transactionLimit;
	}

	/**
	 * Create a randomly generated value.
	 * 
	 * @return the value of a commodity.
	 */
	public abstract int nextValue();

	/**
	 * Get the transactionLimit. The transaction limit caps the maximum random
	 * int that will be produced at the value defined by transactionLimit.
	 * 
	 * @return the transactionLimit.
	 */
	public int getTransactionLimit()
	{
		return transactionLimit;
	}
	
	/**
	 * Set the transactionLimit. The transaction limit caps the maximum random
	 * int that will be produced at the value defined by transactionLimit.
	 * @param transactionLimit the desired transactionLimit.
	 */
	public void setTransactionLimit(int transactionLimit)
	{
		this.transactionLimit = transactionLimit;
	}
}
