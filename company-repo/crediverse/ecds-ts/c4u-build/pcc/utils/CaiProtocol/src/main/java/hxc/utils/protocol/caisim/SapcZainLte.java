package hxc.utils.protocol.caisim;

/**
 * Represents a SAPC Zain-specific LTE value.
 * 
 * @author petar
 *
 */
public class SapcZainLte
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private String id = new String();
	private boolean zainLte = false;

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
	
	public boolean getZainLte()
	{
		return zainLte;
	}
	
	public void setZainLte(boolean zainLte)
	{
		this.zainLte = zainLte;
	}
}
