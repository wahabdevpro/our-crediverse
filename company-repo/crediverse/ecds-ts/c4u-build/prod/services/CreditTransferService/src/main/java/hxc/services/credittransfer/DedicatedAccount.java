package hxc.services.credittransfer;

import java.io.Serializable;

import hxc.configuration.Configurable;

@SuppressWarnings("serial")
@Configurable
public class DedicatedAccount implements Serializable
{
	String accountName;
	private int id; // DA id
	private long minimum;
	private long maximum;

	public DedicatedAccount(String name, int id, long min, long max)
	{
		this.accountName = name;
		this.id = id;
		this.minimum = min;
		this.maximum = max;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getAccountName()
	{
		return accountName;
	}

	public void setAccountName(String accountName)
	{
		this.accountName = accountName;
	}

	public long getMinimum()
	{
		return minimum;
	}

	public void setMinimum(long minimum)
	{
		this.minimum = minimum;
	}

	public long getMaximum()
	{
		return maximum;
	}

	public void setMaximum(long maximum)
	{
		this.maximum = maximum;
	}
}
