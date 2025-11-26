package hxc.utils.protocol.uiconnector.metrics;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class MetricsDataRequest extends UiBaseRequest
{
	public long lastMillisecondResponse = 0;

	public MetricsDataRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	/**
	 * @return the lastMillisecondResponse
	 */
	public long getLastMillisecondResponse()
	{
		return lastMillisecondResponse;
	}

	/**
	 * @param lastMillisecondResponse
	 *            the lastMillisecondResponse to set
	 */
	public void setLastMillisecondResponse(long lastMillisecondResponse)
	{
		this.lastMillisecondResponse = lastMillisecondResponse;
	}

}
