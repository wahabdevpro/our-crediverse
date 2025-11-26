package hxc.utils.protocol.uiconnector.response;

public class SystemInfoResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -3549481170398442818L;
	private String version;
	private boolean debugMode;

	public SystemInfoResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public SystemInfoResponse(String version, boolean debugMode)
	{
		this.version = version;
		this.debugMode = debugMode;
	}

	/**
	 * @return the version
	 */
	public String getVersion()
	{
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(String version)
	{
		this.version = version;
	}
	
	/**
	 * Is the System running in debug mode 
	 */
	public boolean isDebugMode()
	{
		return debugMode;
	}

}
