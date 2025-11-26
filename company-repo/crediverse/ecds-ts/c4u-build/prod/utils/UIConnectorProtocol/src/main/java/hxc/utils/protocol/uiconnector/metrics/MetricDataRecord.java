package hxc.utils.protocol.uiconnector.metrics;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MetricDataRecord implements Serializable
{
	private long timeInMilliSecconds;
	private Object[] values;

	public MetricDataRecord()
	{
	}

	/**
	 * @return the timeInMilliSecconds
	 */
	public long getTimeInMilliSecconds()
	{
		return timeInMilliSecconds;
	}

	/**
	 * @param timeInMilliSecconds
	 *            the timeInMilliSecconds to set
	 */
	public void setTimeInMilliSecconds(long timeInMilliSecconds)
	{
		this.timeInMilliSecconds = timeInMilliSecconds;
	}

	/**
	 * @return the values
	 */
	public Object[] getValues()
	{
		return values;
	}

	/**
	 * @param values
	 *            the values to set
	 */
	public void setValues(Object[] values)
	{
		this.values = values;
	}

}
