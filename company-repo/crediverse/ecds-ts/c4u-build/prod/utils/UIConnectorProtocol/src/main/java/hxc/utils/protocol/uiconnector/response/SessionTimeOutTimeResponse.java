package hxc.utils.protocol.uiconnector.response;

public class SessionTimeOutTimeResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -3733252621463141702L;
	private int sessionTimeoutMinutes = 0;

	public SessionTimeOutTimeResponse(String userId, String sessionId, int sessionTimeoutMinutes)
	{
		super(userId, sessionId);
		this.sessionTimeoutMinutes = sessionTimeoutMinutes;
	}

	/**
	 * @return the sessionTimeoutMinutes
	 */
	public int getSessionTimeoutMinutes()
	{
		return sessionTimeoutMinutes;
	}

	/**
	 * @param sessionTimeoutMinutes
	 *            the sessionTimeoutMinutes to set
	 */
	public void setSessionTimeoutMinutes(int sessionTimeoutMinutes)
	{
		this.sessionTimeoutMinutes = sessionTimeoutMinutes;
	}

}
