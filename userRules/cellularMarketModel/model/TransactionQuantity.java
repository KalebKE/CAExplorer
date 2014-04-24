package userRules.cellularMarketModel.model;

import java.util.Random;

/**
 * Class contains stochastic methods for creating transaction quantities for
 * commodities.
 * 
 * @author Kaleb
 * 
 */
public class TransactionQuantity extends StoichasticTransaction
{
	/**
	 * Create a randomly generated value.
	 * 
	 * @return the value of a commodity.
	 */
	public int nextValue()
	{
		Random r = new Random();

		int transactionQuantity = r.nextInt(transactionLimit);
		
		// Force half of all transactionQuantities to be negative.
		if(transactionQuantity%2 == 0)
		{
			transactionQuantity = -transactionQuantity;
		}

		return transactionQuantity;
	}
}
