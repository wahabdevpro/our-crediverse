package hxc.connectors.ui.bam;

public class MetricInfo
{
	private long uid;
	private String name;

	public MetricInfo()
	{
	}

	public MetricInfo(long uid, String name)
	{
		this.uid = uid;
		this.name = name;
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

}
