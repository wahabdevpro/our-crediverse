package hxc.userinterfaces.gui.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class GuiUpdateResponse
{
	public static enum OperationStatus
	{
		pass, fail
	};

	private OperationStatus status;
	private String message = null; // If Failure the reason for failure
	private String field = null; // full path to field which failed e.g. Variants[2].VariantID

	public GuiUpdateResponse()
	{
	}

	public GuiUpdateResponse(OperationStatus status, String message, String field)
	{
		this.status = status;
		this.message = message;
		this.field = field;
	}

	public GuiUpdateResponse(OperationStatus status, String message)
	{
		this(status, message, null);
	}

	public GuiUpdateResponse(OperationStatus status)
	{
		this(status, null, null);
	}

	/**
	 * @return the status
	 */
	public OperationStatus getStatus()
	{
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(OperationStatus status)
	{
		this.status = status;
	}

	/**
	 * @return the message
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getField()
	{
		return field;
	}

	public void setField(String field)
	{
		this.field = field;
	}

	@Override
	public String toString()
	{
		JsonObject job = new JsonObject();
		job.add("status", new JsonPrimitive(status.toString()));
		if (message != null)
		{
			job.add("message", new JsonPrimitive(message));
		}

		if (field != null)
		{
			job.add("field", new JsonPrimitive(field));
		}
		return job.toString();
	}

}
