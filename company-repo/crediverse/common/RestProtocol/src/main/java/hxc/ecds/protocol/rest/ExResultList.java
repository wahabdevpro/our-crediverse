package hxc.ecds.protocol.rest;

import java.util.List;

public class ExResultList<T>
{
	protected Long foundRows;
	protected List<? extends T> instances;

	public ExResultList()
	{
	}

	public ExResultList(Long foundRows, List<? extends T> instances)
	{
		this.foundRows = foundRows;
		this.instances = instances;
	}
	
	public ExResultList(Integer foundRows, List<? extends T> instances)
	{
		this.foundRows = Long.valueOf(foundRows);
		this.instances = instances;
	}

	public Long getFoundRows()
	{
		return foundRows;
	}

	public void setFoundRows(Long foundRows)
	{
		this.foundRows = foundRows;
	}

	public List<? extends T> getInstances()
	{
		return instances;
	}

	public void setInstances(List<? extends T> instances)
	{
		this.instances = instances;
	}

	public String describe(String extra)
	{
		return String.format("%s@%s(foundRows = %s, instances = %s%s%s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			foundRows, instances,
			(extra.isEmpty() ? "" : ", "), extra);
	}

	public String describe()
	{
		return this.describe("");
	}

	public String toString()
	{
		return this.describe();
	}
}
