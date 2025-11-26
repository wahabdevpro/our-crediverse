package hxc.utils.protocol.uiconnector.response;

public class CallConfigurableMethodResponse extends UiBaseResponse
{

	private static final long serialVersionUID = 7343526194095122542L;
	private String methodCallResponse = null;

	public CallConfigurableMethodResponse(String methodResponse)
	{
		setResponseCode(UiResponseCode.METHOD_RESPONSE);
		this.methodCallResponse = methodResponse;
	}

	/**
	 * @return the methodCallResponse
	 */
	public String getMethodCallResponse()
	{
		return methodCallResponse;
	}

}
