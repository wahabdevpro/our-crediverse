package hxc.connectors.ussd;

public interface IPushUSSD
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IPushUSSD
	//
	// /////////////////////////////////
	public abstract String pushUSSD(String transactionID, String msisdn, String serviceCode, String languageCode, String text, boolean notifyOnly) throws Exception;
}