package hxc.utils.protocol.uiconnector.response;

import hxc.connectors.smtp.ISmtpHistory;
import hxc.utils.protocol.uiconnector.airsim.Cdr;
import hxc.utils.protocol.uiconnector.airsim.SmsHistory;

public class GetAirsimHistoryResponse extends UiBaseResponse
{

	private static final long serialVersionUID = 545850332599235508L;
	private SmsHistory smsHistory[];
	private Cdr cdrHistory[];
	private ISmtpHistory emailHistory[];

	public SmsHistory[] getSmsHistory()
	{
		return smsHistory;
	}

	public void setSmsHistory(SmsHistory[] smsHistory)
	{
		this.smsHistory = smsHistory;
	}

	public Cdr[] getCdrHistory()
	{
		return cdrHistory;
	}

	public void setCdrHistory(Cdr[] cdrHistory)
	{
		this.cdrHistory = cdrHistory;
	}

	public ISmtpHistory[] getEmailHistory()
	{
		return this.emailHistory;
	}

	public void setEmailHistory(ISmtpHistory[] emailHistory)
	{
		this.emailHistory = emailHistory;
	}

	public GetAirsimHistoryResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}


}
