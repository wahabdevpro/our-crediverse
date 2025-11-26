package hxc.utils.protocol.caisim;

/**
 * Represents Call Forwarding on Not Reachable data in the HLR.
 * 
 * There are two bits that determine whether the service can be used in the HLR:
 * - provisionState - whether the service is provisioned at all for that subscriber
 * - activationState - whether the service is active
 * Above implies that the service can be provisioned and deactivated, provisioned and activated or not provisioned at all.
 * 
 * Category is the service category that CFNRC applies to. Currently, only a single category is supported.
 * 
 * @author petar
 *
 */
public class Cfnrc implements Cloneable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private int provisionState = 0;
	private int activationState = 0;
	private String number = new String();
	private String category = new String();
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Methods
	//
	// /////////////////////////////////
	
	public int getProvisionState()
	{
		return provisionState;
	}
	
	public void setProvisionState(int provisionState)
	{
		if (provisionState > 0)
			this.provisionState = 1;
		else
			this.provisionState = 0;
	}
	
	public int getActivationState()
	{
		return activationState;
	}
	
	public void setActivationState(int activationState)
	{
		if (activationState > 0)
			this.activationState = 1;
		else
			this.activationState = 0;
	}
	
	public String getNumber()
	{
		return number;
	}
	
	public void setNumber(String number)
	{
		this.number = number;
	}
	
	public boolean hasNumber()
	{
		return (number != null && !number.isEmpty());
	}
	
	public String getCategory()
	{
		return category;
	}
	
	public void setCategory(String category)
	{
		this.category = category;
	}
	
	@Override
	public Object clone()
	{
		Cfnrc ret = new Cfnrc();
		
		ret.setProvisionState(provisionState);
		ret.setActivationState(activationState);
		ret.setNumber(new String(number));
		ret.setCategory(new String(category));
		
		return ret;
	}
}
