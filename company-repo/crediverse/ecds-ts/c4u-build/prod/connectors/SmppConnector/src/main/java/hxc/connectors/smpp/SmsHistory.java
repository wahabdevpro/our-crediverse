package hxc.connectors.smpp;

import java.util.Date;

import hxc.connectors.IInteraction;
import hxc.connectors.sms.ISmsHistory;
import hxc.connectors.sms.ISmsResponse;

public class SmsHistory implements ISmsHistory
{

	private ISmsResponse response;
	private IInteraction request;
	private Date created;

	public SmsHistory(Date created)
	{
		this.created = created;
	}

	@Override
	public ISmsResponse getResponse()
	{
		return response;
	}

	public void setResponse(ISmsResponse response)
	{
		this.response = response;
	}

	@Override
	public IInteraction getRequest()
	{
		return request;
	}

	public void setRequest(IInteraction request)
	{
		this.request = request;
	}

	@Override
	public Date getDate()
	{
		return created;
	}
}
