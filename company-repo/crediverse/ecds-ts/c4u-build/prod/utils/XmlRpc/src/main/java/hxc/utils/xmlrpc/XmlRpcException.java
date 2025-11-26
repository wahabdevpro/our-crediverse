/**
 *
 */
package hxc.utils.xmlrpc;

/**
 * @author AndriesdB
 * 
 */
public class XmlRpcException extends Exception
{
	public enum Context {
		Connect, 
		Write,
		Read,
		Fault,
		Unknown
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 491688492250197998L;

	private String message;
	private Object errorCode;
	private Context context = Context.Unknown;

	@Override
	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public Object getErrorCode()
	{
		return errorCode;
	}

	public void setErrorCode(Object errorCode)
	{
		this.errorCode = errorCode;
	}

	public Context getContext()
	{
		return context;
	}

	// Parameterless Constructor
	public XmlRpcException()
	{
	}

	// Constructor that accepts a message
	public XmlRpcException(String message)
	{
		super(message);
		this.message = message;
	}

	public XmlRpcException(String message, Object errorCode)
	{
		super(message);
		this.message = message;
		this.errorCode = errorCode;
	}

	public XmlRpcException(String message, Object errorCode, Throwable throwable)
	{
		super(message, throwable);
		this.message = message;
		this.errorCode = errorCode;
	}

	public XmlRpcException(String message, Object errorCode, Throwable throwable, Context context)
	{
		super(message, throwable);
		this.message = message;
		this.errorCode = errorCode;
		this.context = context;
	}

}
