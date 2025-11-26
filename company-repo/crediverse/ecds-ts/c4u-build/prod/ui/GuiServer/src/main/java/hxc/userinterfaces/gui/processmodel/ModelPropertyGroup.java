package hxc.userinterfaces.gui.processmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ModelPropertyGroup
{
	private String category; //
	private List<ModelProperty> properties = new ArrayList<>();

	public ModelPropertyGroup()
	{
	}

	public ModelPropertyGroup(String category)
	{
		this.category = category;
	}

	public ModelPropertyGroup(String category, List<ModelProperty> props)
	{
		this.category = category;
		this.properties = props;
	}

	public ModelPropertyGroup(String category, ModelProperty[] props)
	{
		this.category = category;
		this.properties = Arrays.asList(props);
	}

	public String getCategory()
	{
		return category;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}

	public JsonObject toJson()
	{
		JsonObject job = new JsonObject();
		job.add("cat", new JsonPrimitive(category));
		JsonArray jarr = new JsonArray();
		for (int i = 0; i < properties.size(); i++)
		{
			jarr.add(properties.get(i).toJson());
		}
		job.add("props", jarr);
		return job;
	}

	@Override
	public String toString()
	{
		return toJson().toString();
	}

	public static String ModelGroupArrayToString(String nodeId, List<ModelPropertyGroup> groups)
	{
		JsonObject job = new JsonObject();
		job.add("nid", new JsonPrimitive(nodeId));
		JsonArray jarr = new JsonArray();
		for (int i = 0; i < groups.size(); i++)
		{
			jarr.add(groups.get(i).toJson());
		}
		job.add("groups", jarr);
		return job.toString();
	}

	public List<ModelProperty> getProperties()
	{
		return properties;
	}

	public void setProperties(List<ModelProperty> properties)
	{
		this.properties = properties;
	}
}
