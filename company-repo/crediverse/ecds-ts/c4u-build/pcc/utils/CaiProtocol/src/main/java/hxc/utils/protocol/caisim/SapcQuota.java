package hxc.utils.protocol.caisim;

/**
 * Represents a SAPC quota value.
 * 
 * @author petar
 *
 */
public class SapcQuota
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private String id = new String();
	private long quota = 0;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Method
	//
	// /////////////////////////////////

	public String getId()
	{
		return id;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public long getQuota()
	{
		return quota;
	}
	
	public void setQuota(long quota)
	{
		this.quota = quota;
	}
}
