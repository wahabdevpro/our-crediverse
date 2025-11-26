package hxc.utils.protocol.caisim.request.soap;

import hxc.utils.protocol.caisim.SapcZainLte;

public class SetSapcZainLteRequest extends SoapRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private SapcZainLte zainLte = new SapcZainLte();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Method
	//
	// /////////////////////////////////
	public SapcZainLte getGroup()
	{
		return zainLte;
	}

	public void setGroup(SapcZainLte zainLte)
	{
		this.zainLte = zainLte;
	}
	
	
}
