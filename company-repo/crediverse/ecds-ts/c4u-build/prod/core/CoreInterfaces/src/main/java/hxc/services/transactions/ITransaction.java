/**
 *
 */
package hxc.services.transactions;

import java.util.Date;

public interface ITransaction
{
	Date getStartTime();

	String getTransactionID();

	int getResultCode();

	void setResultCode(int resultCode);

	void addReversal(Reversal reversal);

	void setLastNotification(String message);
}
