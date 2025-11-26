package hxc.utils.protocol.caisim.request.soap;

import hxc.utils.protocol.caisim.Cfnrc;
import hxc.utils.protocol.caisim.PdpContexts;

public class AddHlrSubscriberRequest extends SoapRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private int obr = 0;
	private int rsa = 1;
	private Cfnrc cfnrc = new Cfnrc();
	private int nam = 0;
	private int pdpCp = 1;
	private PdpContexts pdps = new PdpContexts();
	private String imei = new String();
	private int count = 1;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Method
	//
	// /////////////////////////////////
	
	public int getObr()
	{
		return obr;
	}
	
	public void setObr(int obr)
	{
		this.obr = obr;
	}
	
	public int getRsa()
	{
		return rsa;
	}
	
	public void setRsa(int rsa)
	{
		this.rsa = rsa;
	}
	public Cfnrc getCfnrc()
	{
		return cfnrc;
	}
	
	public void setCfnrc(Cfnrc cfnrc)
	{
		this.cfnrc = cfnrc;
	}
	
	public int getNam()
	{
		return nam;
	}
	
	public void setNam(int nam)
	{
		this.nam = nam;
	}
	public int getPdpCp()
	{
		return pdpCp;
	}
	
	public void setPdpCp(int pdpCp)
	{
		this.pdpCp = pdpCp;
	}
	
	public PdpContexts getPdpContexts()
	{
		return pdps;
	}

	public void setPdpContexts(PdpContexts pdps)
	{
		this.pdps = pdps;
	}
	
	public String getImei()
	{
		return imei;
	}

	public void setImei(String imei)
	{
		this.imei = imei;
	}
	
	public int getCount()
	{
		return count;
	}
	
	public void setCount(int count)
	{
		this.count = count;
	}
}
