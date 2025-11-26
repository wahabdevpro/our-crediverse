package hxc.utils.protocol.uiconnector.metrics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class MetricData implements Serializable
{
	private long uid;
	private String name;
	private List<MetricDataRecord> records = null;

	public MetricData()
	{
		this.records = new ArrayList<>();
	}

	/**
	 * @return the uid
	 */
	public long getUid()
	{
		return uid;
	}

	/**
	 * @param uid
	 *            the uid to set
	 */
	public void setUid(long uid)
	{
		this.uid = uid;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the records
	 */
	public List<MetricDataRecord> getRecords()
	{
		return records;
	}

	/**
	 * @param records
	 *            the records to set
	 */
	public void setRecords(List<MetricDataRecord> records)
	{
		this.records = records;
	}

}
