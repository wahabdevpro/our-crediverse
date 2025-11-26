package hxc.utils.protocol.caisim.response.soap;

import javax.xml.bind.annotation.XmlType;

import hxc.utils.protocol.caisim.Cfnrc;
import hxc.utils.protocol.caisim.PdpContexts;
import hxc.utils.protocol.caisim.request.soap.GetHlrSubscriberRequest;

@XmlType(name = "GetHlrSubscriberResponse")
public class GetHlrSubscriberResponse extends SoapResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private String msisdn = new String();
	private int obr = 0;
	private int rsa = 1;
	private Cfnrc cfnrc = new Cfnrc();
	private int nam = 0;
	private int pdpCp = 1;
	private PdpContexts pdpContexts = new PdpContexts();
	private String imei = new String();
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	
	public GetHlrSubscriberResponse(GetHlrSubscriberRequest request)
	{
		super(request);
	}
	
	public GetHlrSubscriberResponse()
	{
		
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Method
	//
	// /////////////////////////////////
	
	public String getMsisdn()
	{
		return msisdn;
	}
	
	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
	}
	
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
		return pdpContexts;
	}

	public void setPdpContexts(PdpContexts pdps)
	{
		this.pdpContexts = pdps;
	}
	
	public String getImei()
	{
		return imei;
	}
	
	public void setImei(String imei)
	{
		this.imei = imei;
	}
}
