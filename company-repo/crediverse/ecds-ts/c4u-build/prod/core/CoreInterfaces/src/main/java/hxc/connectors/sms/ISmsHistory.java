package hxc.connectors.sms;

import java.util.Date;

import hxc.connectors.IInteraction;

public interface ISmsHistory
{
	public abstract ISmsResponse getResponse();

	public abstract IInteraction getRequest();

	public abstract Date getDate();
}
