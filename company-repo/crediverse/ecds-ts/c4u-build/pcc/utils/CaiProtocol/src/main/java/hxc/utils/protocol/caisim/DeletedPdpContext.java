package hxc.utils.protocol.caisim;

/**
 * Represents a request for PDP context deletion.
 * 
 * PDPs can be deleted by either:
 * - unique PDP ID
 * - unique APN ID + PDP Address
 * - APN ID - all PDPs with that APN ID will be deleted
 * 
 * @author petar
 *
 */
public class DeletedPdpContext
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private int pdpId = PdpContext.INVALID_PDP_ID;
	private int apnId = -1;
	private String pdpAddress = new String();
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Methods
	//
	// /////////////////////////////////
	
	public int getPdpId()
	{
		return pdpId;
	}
	
	public void setPdpId(int pdpId)
	{
		this.pdpId = pdpId;
	}
	
	public int getApnId()
	{
		return apnId;
	}
	
	public void setApnId(int apnId)
	{
		this.apnId = apnId;
	}
	
	public String getPdpAddress()
	{
		return pdpAddress;
	}
	
	public void setPdpAddress(String pdpAddress)
	{
		this.pdpAddress = pdpAddress;
	}
}
