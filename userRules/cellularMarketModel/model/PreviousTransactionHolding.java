package userRules.cellularMarketModel.model;

import java.util.Random;

/**
 * Class contains stochastic methods for creating transaction holdings for
 * commodities.
 * 
 * @author Kaleb
 * 
 */
public class PreviousTransactionHolding extends StoichasticTransaction
{

	/**
	 * Create a randomly generated value.
	 * 
	 * @return the value of a commodity.
	 */
	@Override
	public int nextValue()
	{
		Random r = new Random();

		int transactionQuantity = r.nextInt(transactionLimit);	

		return transactionQuantity;
	}

}
