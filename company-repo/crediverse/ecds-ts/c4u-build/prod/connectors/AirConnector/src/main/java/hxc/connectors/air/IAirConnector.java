package hxc.connectors.air;

import java.math.BigDecimal;

import hxc.connectors.IConnector;
import hxc.connectors.air.proxy.Subscriber;
import hxc.services.numberplan.INumberPlan;
import hxc.services.transactions.ITransaction;

public interface IAirConnector extends IConnector
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
	public IAirConnection getConnection(String optionalConnectionString);

	public Long toLongAmount(BigDecimal amount);

	public BigDecimal fromLongAmount(Long amount);

	public String getLanguageCode2(int languageID);

}
