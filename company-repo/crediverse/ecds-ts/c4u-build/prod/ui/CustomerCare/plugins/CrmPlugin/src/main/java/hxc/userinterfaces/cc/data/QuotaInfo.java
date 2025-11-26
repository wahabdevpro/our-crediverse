package hxc.userinterfaces.cc.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.concurrent.hxc.ServiceQuota;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class QuotaInfo
{
	private TreeSet<String> types;
	private TreeSet<String> periods;
	private TreeSet<String> destinations;
	private TreeSet<String> daysOfWeek;
	private TreeSet<String> timesOfDay;
	private List<QuotaFilter> filters;
	
	private String currencyCode;

	public QuotaInfo()
	{
		types = new TreeSet<>();
		periods = new TreeSet<>();
		destinations = new TreeSet<>();
		daysOfWeek = new TreeSet<>();
		timesOfDay = new TreeSet<>();
		filters = new ArrayList<>();
	}

	public void importQuotaInfo(List<ServiceQuota> quotas)
	{
		for (ServiceQuota sq : quotas)
		{
			types.add(sq.getService());
			destinations.add(sq.getDestination());
			daysOfWeek.add(sq.getDaysOfWeek());
			timesOfDay.add(sq.getTimeOfDay());
			QuotaFilter qf = new QuotaFilter(sq.getService(), sq.getDestination(), sq.getDaysOfWeek(), sq.getTimeOfDay(), sq.getUnits());
			filters.add(qf);
		}
	}

	
	public String getCurrencyCode()
	{
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode)
	{
		this.currencyCode = currencyCode;
	}

	private JsonArray extractSetData(Set<String> data)
	{
		JsonArray jarr = new JsonArray();
		for (String s : data)
		{
			jarr.add(new JsonPrimitive(s));
		}
		return jarr;
	}

	@Override
	public String toString()
	{
		JsonObject job = new JsonObject();

		job.add("types", extractSetData(types));
		job.add("periods", extractSetData(periods));
		job.add("destinations", extractSetData(destinations));
		job.add("daysOfWeek", extractSetData(daysOfWeek));
		job.add("timesOfDay", extractSetData(timesOfDay));
		job.add("currencyCode", new JsonPrimitive(currencyCode));

		JsonArray jarr = new JsonArray();
		for (QuotaFilter qf : filters)
		{
			jarr.add(qf.getJsonObject());
		}

		job.add("filters", jarr);
		return job.toString();
	}

	public TreeSet<String> getTypes()
	{
		return types;
	}

	public void setTypes(TreeSet<String> types)
	{
		this.types = types;
	}

	public TreeSet<String> getPeriods()
	{
		return periods;
	}

	public void setPeriods(TreeSet<String> periods)
	{
		this.periods = periods;
	}

	public TreeSet<String> getDestinations()
	{
		return destinations;
	}

	public void setDestinations(TreeSet<String> destinations)
	{
		this.destinations = destinations;
	}

	public TreeSet<String> getDaysOfWeek()
	{
		return daysOfWeek;
	}

	public void setDaysOfWeek(TreeSet<String> daysOfWeek)
	{
		this.daysOfWeek = daysOfWeek;
	}

	public TreeSet<String> getTimesOfDay()
	{
		return timesOfDay;
	}

	public void setTimesOfDay(TreeSet<String> timesOfDay)
	{
		this.timesOfDay = timesOfDay;
	}

	/**
	 * @return the filters
	 */
	public List<QuotaFilter> getFilters()
	{
		return filters;
	}

	/**
	 * @param filters
	 *            the filters to set
	 */
	public void setFilters(List<QuotaFilter> filters)
	{
		this.filters = filters;
	}

}
