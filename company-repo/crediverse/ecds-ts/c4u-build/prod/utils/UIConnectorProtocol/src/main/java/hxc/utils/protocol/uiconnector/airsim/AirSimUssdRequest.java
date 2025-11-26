package hxc.utils.protocol.uiconnector.airsim;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class AirSimUssdRequest extends UiBaseRequest
{
	private static final long serialVersionUID = 3610957038780213746L;
	
	private String msisdn;
	private String ussd;
	private String imsi;
	
	public AirSimUssdRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public String getMsisdn()
	{
		return msisdn;
	}

	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
	}

	public String getUssd()
	{
		return ussd;
	}

	public void setUssd(String ussd)
	{
		this.ussd = ussd;
	}

	public String getImsi()
	{
		return imsi;
	}

	public void setImsi(String imsi)
	{
		this.imsi = imsi;
	}
	
}
