package hxc.services.ecds.util;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;

public class RuleCheckException extends Exception implements Response.StatusType
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private static final long serialVersionUID = -362129715184296354L;

	private Status status = Status.INTERNAL_SERVER_ERROR;
	private String property = null;
	private String error;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public Status getStatus()
	{
		return status;
	}

	public void setStatus(Status status)
	{
		this.status = status;
	}

	public String getProperty()
	{
		return property;
	}

	public void setProperty(String property)
	{
		this.property = property;
	}

	public String getError()
	{
		return error;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public RuleCheckException(StatusCode code, String property, String message, Object... args)
	{
		super(String.format(message, args));
		this.status = code.getStatus();
		this.error = code.getName();
		this.property = property;
	}

	public RuleCheckException(String returnCode, String property, String message, Object... args)
	{
		super(String.format(message, args));
		this.status = Status.NOT_ACCEPTABLE;
		this.error = returnCode;
		this.property = property;
	}

	public RuleCheckException(Throwable cause, StatusCode code, String property, String message, Object... args)
	{
		super(String.format(message, args), cause);
		this.status = code.getStatus();
		this.error = code.getName();
		this.property = property;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public WebApplicationException toWebException()
	{
		Response response = Response.status(this).build();
		WebApplicationException result = new WebApplicationException(this, response);
		return result;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Response.StatusType
	//
	// /////////////////////////////////

	@Override
	public Family getFamily()
	{
		return Response.Status.Family.CLIENT_ERROR;
	}

	@Override
	public String getReasonPhrase()
	{
		return property == null || property.isEmpty() ? error : String.format("%s/%s", error, property);
	}

	@Override
	public int getStatusCode()
	{
		return status.getStatusCode();
	}

}
