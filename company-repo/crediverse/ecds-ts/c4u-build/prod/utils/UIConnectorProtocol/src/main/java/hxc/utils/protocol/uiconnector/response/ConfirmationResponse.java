package hxc.utils.protocol.uiconnector.response;

public class ConfirmationResponse extends UiBaseResponse
{
	private static final long serialVersionUID = -930071799565449209L;
	private String response;

	public ConfirmationResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
		setResponseCode(UiResponseCode.ERROR);
	}

	/**
	 * @return the response
	 */
	public String getResponse()
	{
		return response;
	}

	/**
	 * @param response
	 *            the response to set
	 */
	public void setResponse(String response)
	{
		this.response = response;
	}

}
