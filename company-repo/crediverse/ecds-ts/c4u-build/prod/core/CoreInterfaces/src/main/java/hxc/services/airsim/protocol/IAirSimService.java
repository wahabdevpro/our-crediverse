package hxc.services.airsim.protocol;

public interface IAirSimService
{

	// Gets the cdr history
	public ICdr[] getCdrHistory();

	// Gets the sms history
	public ISmsHistory[] getSmsHistory();

	// Clear SMS history
	public void clearSmsHistory();
	
	// Send Ussd Call
	public IUssdResponse injectMOUssd(String from, String text, String imsi);
	
	// Send SMS
	public void injectMOSms(String from, String to, String text);
	
	// Inject Air Response Code and Response Delay
	public void injectAirResponse(String airCallName, String responseCode, String delay);
	
	// Inject Air Response Code and Response Delay
	public void resetInjectedAirResponse(String airCallName);
}
