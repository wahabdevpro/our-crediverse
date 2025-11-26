package hxc.userinterfaces.gui.processmodel;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ModelAttributeInfo
{
	private String nodeId; // e.g. 151321651
	private String nType; // e.g. MenuItem(s) / Menu / etc...
	private String attributeId; // e.g. Caption
	private String propertyType; // e.g. Itext
	private TextData[] data; // e.g. {section: "msg", text: ["","","","",""]}

	public ModelAttributeInfo()
	{
	}

	public ModelAttributeInfo(String nodeId, String attributeId)
	{
		this.nodeId = nodeId;
		this.attributeId = attributeId;
	}

	/**
	 * @return the nodeId
	 */
	public String getNodeId()
	{
		return nodeId;
	}

	/**
	 * @param nodeId
	 *            the nodeId to set
	 */
	public void setNodeId(String nodeId)
	{
		this.nodeId = nodeId;
	}

	/**
	 * @return the attributeId
	 */
	public String getAttributeId()
	{
		return attributeId;
	}

	/**
	 * @param attributeId
	 *            the attributeId to set
	 */
	public void setAttributeId(String attributeId)
	{
		this.attributeId = attributeId;
	}

	/**
	 * @return the propertyType
	 */
	public String getPropertyType()
	{
		return propertyType;
	}

	/**
	 * @param propertyType
	 *            the propertyType to set
	 */
	public void setPropertyType(String propertyType)
	{
		this.propertyType = propertyType;
	}

	/**
	 * @return the data
	 */
	public TextData[] getData()
	{
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(TextData[] data)
	{
		this.data = data;
	}

	/**
	 * @return the nType
	 */
	public String getnType()
	{
		return nType;
	}

	/**
	 * @param nType
	 *            the nType to set
	 */
	public void setnType(String nType)
	{
		this.nType = nType;
	}

	public JsonObject toJson()
	{
		JsonObject job = new JsonObject();
		job.add("nid", new JsonPrimitive(nodeId));
		job.add("aid", new JsonPrimitive(attributeId));
		job.add("ntype", new JsonPrimitive(nType));
		job.add("ptype", new JsonPrimitive(propertyType));
		JsonArray jarr = new JsonArray();
		if (data != null && data.length > 0)
		{
			for (TextData td : data)
			{
				jarr.add(td.toJson());
			}
		}
		job.add("data", jarr);
		return job;
	}

	@Override
	public String toString()
	{
		return toJson().toString();
	}
}
