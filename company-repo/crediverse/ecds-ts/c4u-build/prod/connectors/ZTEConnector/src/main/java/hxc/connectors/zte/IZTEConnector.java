package hxc.connectors.zte;

import java.math.BigDecimal;

import hxc.connectors.IConnector;
import hxc.connectors.zte.proxy.Subscriber;
import hxc.services.numberplan.INumberPlan;
import hxc.services.transactions.ITransaction;

public interface IZTEConnector extends IConnector
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Interface
	//
	// /////////////////////////////////
	public Subscriber getSubscriber(String subscriberNumber, ITransaction transaction);

	public String getNextTransactionID(int length);

	public INumberPlan getNumberPlan();

	@Override
	public IZTEConnection getConnection(String optionalConnectionString);

	public Long toLongAmount(BigDecimal amount);

	public BigDecimal fromLongAmount(Long amount);

}
