package hxc.userinterfaces.gui.processmodel;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Linked to ModelAttributeInfo
 */
public class TextData
{
	private String code;
	private String[] text;

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String[] getText()
	{
		return text;
	}

	public void setText(String[] text)
	{
		this.text = text;
	}

	public JsonObject toJson()
	{
		JsonObject job = new JsonObject();
		job.add("code", new JsonPrimitive(code));
		JsonArray jarr = new JsonArray();
		if (text != null)
		{
			for (int i = 0; i < text.length; i++)
			{
				jarr.add(new JsonPrimitive(text[i]));
			}
		}
		job.add("text", jarr);
		return job;
	}

	@Override
	public String toString()
	{
		Gson gson = new Gson();
		return gson.toJson(this);
	}

}
