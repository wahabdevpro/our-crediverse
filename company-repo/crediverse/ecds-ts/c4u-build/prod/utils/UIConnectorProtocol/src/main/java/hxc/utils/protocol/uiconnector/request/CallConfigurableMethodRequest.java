package hxc.utils.protocol.uiconnector.request;

public class CallConfigurableMethodRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 6571271249498142277L;

	private long configUID = 0; // 0 when not set
	private String configName;
	private String configPath;
	private String method;

	public CallConfigurableMethodRequest()
	{
	}

	public CallConfigurableMethodRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
		setRequestCode(UiRequestCode.CALL_METHOD_REQUEST);
	}

	/**
	 * @return the configName
	 */
	public String getConfigName()
	{
		return configName;
	}

	/**
	 * @param configName
	 *            the configName to set
	 */
	public void setConfigName(String configName)
	{
		this.configName = configName;
	}

	/**
	 * @return the configPath
	 */
	public String getConfigPath()
	{
		return configPath;
	}

	/**
	 * @param configPath
	 *            the configPath to set
	 */
	public void setConfigPath(String configPath)
	{
		this.configPath = configPath;
	}

	/**
	 * @return the method
	 */
	public String getMethod()
	{
		return method;
	}

	/**
	 * @param method
	 *            the method to set
	 */
	public void setMethod(String method)
	{
		this.method = method;
	}

	/**
	 * @return the configUID
	 */
	public long getConfigUID()
	{
		return configUID;
	}

	/**
	 * @param configUID
	 *            the configUID to set
	 */
	public void setConfigUID(long configUID)
	{
		this.configUID = configUID;
	}

}
