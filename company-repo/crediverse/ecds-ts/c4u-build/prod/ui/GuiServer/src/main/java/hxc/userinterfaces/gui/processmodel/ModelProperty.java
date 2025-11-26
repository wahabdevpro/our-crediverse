package hxc.userinterfaces.gui.processmodel;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ModelProperty
{
	private String pid; // property ID
	private String name;
	private String value;
	private boolean editable;

	public ModelProperty()
	{
	}

	public ModelProperty(String pid, String name, String value, boolean editable)
	{
		this.pid = pid;
		this.name = name;
		this.value = value;
		this.editable = editable;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public String getPid()
	{
		return pid;
	}

	public void setPid(String pid)
	{
		this.pid = pid;
	}

	public JsonObject toJson()
	{
		JsonObject job = new JsonObject();
		job.add("pid", new JsonPrimitive(pid));
		job.add("name", new JsonPrimitive(name));
		job.add("value", new JsonPrimitive(value));
		job.add("edit", new JsonPrimitive(editable));
		return job;
	}

	@Override
	public String toString()
	{
		return toJson().toString();
	}
}
