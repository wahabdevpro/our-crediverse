package hxc.utils.protocol.uiconnector.alarms;

import java.util.List;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class GetAlarmDataResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -531755848558406466L;
	private List<Alarm> indicationAlarms;
	private List<String> hosts;

	public GetAlarmDataResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public void setIndicationAlarms(List<Alarm> indicationAlarms)
	{
		this.indicationAlarms = indicationAlarms;
	}

	public List<Alarm> getIndicationAlarms()
	{
		return indicationAlarms;
	}

	public List<String> getHosts()
	{
		return hosts;
	}

	public void setHosts(List<String> hosts)
	{
		this.hosts = hosts;
	}
}
