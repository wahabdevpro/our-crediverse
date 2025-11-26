package hxc.utils.protocol.uiconnector.reports;

import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class GetReportparametersResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -3307463706620516998L;
	private ConfigurableResponseParam[] params = null;

	public GetReportparametersResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public ConfigurableResponseParam[] getParams()
	{
		return params;
	}

	public void setParams(ConfigurableResponseParam[] params)
	{
		this.params = params;
	}

}