package hxc.utils.protocol.uiconnector.response;

public class ErrorResponse extends UiBaseResponse
{
	public static enum ErrorCode
	{
		GENERAL, AUTHENTICATION_FAILURE, SESSION_EXPIRED, SECURITY_CONSTRAINT, SYSTEM_FAILURE
	}

	private static final long serialVersionUID = -930071799565449209L;
	private String error;
	private ErrorCode errorCode;
	private String field;

	public ErrorResponse()
	{
	}

	public ErrorResponse(String userId, ErrorCode errorCode)
	{
		super(userId);
		setResponseCode(UiResponseCode.ERROR);
		this.errorCode = errorCode;
	}

	/**
	 * @return the error
	 */
	public String getError()
	{
		return error;
	}

	/**
	 * @param error
	 *            the error to set
	 */
	public void setError(String error)
	{
		this.error = error;
	}

	/**
	 * @return the errorCode
	 */
	public ErrorCode getErrorCode()
	{
		return errorCode;
	}

	/**
	 * @param errorCode
	 *            the errorCode to set
	 */
	public void setErrorCode(ErrorCode errorCode)
	{
		this.errorCode = errorCode;
	}

	public String getField()
	{
		return field;
	}

	public void setField(String field)
	{
		this.field = field;
	}

}
