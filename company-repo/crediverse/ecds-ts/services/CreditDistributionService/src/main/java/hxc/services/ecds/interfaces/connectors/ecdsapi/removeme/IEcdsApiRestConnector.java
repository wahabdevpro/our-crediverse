package hxc.services.ecds.interfaces.connectors.ecdsapi.removeme;

import java.util.List;

import hxc.ecds.protocol.rest.Transaction;

public interface IEcdsApiRestConnector 
{
	//ITransaction needs to be put into EcdsProtocol somehow
	public abstract void notifyTransactions(String sessionID, String baseUri, String tokenUriPath, String callbackUriPath, List<? extends Transaction> transactions) throws Exception;
}
