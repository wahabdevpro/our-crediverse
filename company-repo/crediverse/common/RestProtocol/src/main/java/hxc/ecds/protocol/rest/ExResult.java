package hxc.ecds.protocol.rest;

public class ExResult<T>
{
	protected Long foundRows;
	protected T[] instances;

	public ExResult()
	{
	}

	public ExResult(Long foundRows, T[] instances)
	{
		this.foundRows = foundRows;
		this.instances = instances;
	}
	
	public ExResult(Integer foundRows, T[] instances)
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

	public T[] getInstances()
	{
		return instances;
	}

	public void setInstances(T[] instances)
	{
		this.instances = instances;
	}

}
