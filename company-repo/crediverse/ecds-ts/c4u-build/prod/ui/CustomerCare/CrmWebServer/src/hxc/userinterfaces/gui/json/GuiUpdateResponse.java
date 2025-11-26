package hxc.userinterfaces.gui.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class GuiUpdateResponse
{
	public static enum OperationStatus
	{
		pass, fail, expired
	};

	private OperationStatus status;
	private String message;
	private JsonObject metaData = null;

	public GuiUpdateResponse()
	{
	}

	public GuiUpdateResponse(OperationStatus status, String message)
	{
		this.status = status;
		this.message = message;
	}

	public GuiUpdateResponse(OperationStatus status)
	{
		this.status = status;
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

	/**
	 * @return the metaData
	 */
	public JsonObject getMetaData()
	{
		return metaData;
	}

	/**
	 * @param metaData
	 *            the metaData to set
	 */
	public void setMetaData(JsonObject metaData)
	{
		this.metaData = metaData;
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
		if (metaData != null)
		{
			job.add("meta", metaData);
		}
		return job.toString();
	}

}
