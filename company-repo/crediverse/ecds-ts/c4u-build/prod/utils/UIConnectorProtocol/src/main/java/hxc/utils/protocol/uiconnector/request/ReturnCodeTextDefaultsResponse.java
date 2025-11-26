package hxc.utils.protocol.uiconnector.request;

import hxc.services.notification.ReturnCodeTexts;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class ReturnCodeTextDefaultsResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -7186949539563432172L;
	private ReturnCodeTexts[] defaultReturnTexts = null;

	public ReturnCodeTextDefaultsResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	/**
	 * @return the defaultReturnTexts
	 */
	public ReturnCodeTexts[] getDefaultReturnTexts()
	{
		return defaultReturnTexts;
	}

	/**
	 * @param defaultReturnTexts
	 *            the defaultReturnTexts to set
	 */
	public void setDefaultReturnTexts(ReturnCodeTexts[] defaultReturnTexts)
	{
		this.defaultReturnTexts = defaultReturnTexts;
	}
}
