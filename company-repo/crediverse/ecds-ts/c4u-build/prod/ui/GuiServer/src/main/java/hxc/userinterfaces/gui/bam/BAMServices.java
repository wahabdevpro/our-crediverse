package hxc.userinterfaces.gui.bam;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class BAMServices
{
	private List<BAMService> services;

	public BAMServices()
	{
		services = new ArrayList<>();
	}

	public List<BAMService> getServices()
	{
		return services;
	}

	public void setServices(List<BAMService> services)
	{
		this.services = services;
	}

	public JsonObject toJson()
	{
		JsonObject job = new JsonObject();
		JsonArray jarr = new JsonArray();
		for (BAMService bs : services)
		{
			jarr.add(bs.toJson());
		}
		job.add("metrics", jarr);
		return job;
	}

	@Override
	public String toString()
	{
		return toJson().toString();
	}

}
