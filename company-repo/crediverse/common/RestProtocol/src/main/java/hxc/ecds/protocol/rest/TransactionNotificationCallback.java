package hxc.ecds.protocol.rest;

import java.util.List;

public class TransactionNotificationCallback extends RequestHeader {

	Transaction[] transactions;
	
	public Transaction[] getTransactions()
	{
		return transactions;
	}

	public void setTransactions(List<? extends Transaction> transactions)
	{
		this.transactions = transactions.toArray(new Transaction[transactions.size()]);
	}
	
	@Override
	public List<Violation> validate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends ResponseHeader> T createResponse() {
		// TODO Auto-generated method stub
		return null;
	}

}
