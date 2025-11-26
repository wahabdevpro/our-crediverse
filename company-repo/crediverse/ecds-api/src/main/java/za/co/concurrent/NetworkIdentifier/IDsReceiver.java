package za.co.concurrent.NetworkIdentifier;

public interface IDsReceiver {
	void receiveMacInfo( String ip, String mac );
	void receiveHostInfo( String ip, String host );
}
