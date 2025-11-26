package hxc.utils.protocol.caisim;

import java.net.InetAddress;

/**
 * Represents a PDP Contexts in the HLR.
 * 
 * @author petar
 *
 */
public class PdpContext implements Cloneable, Comparable<PdpContext>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	
	public static final int INVALID_PDP_ID = -1;
	public static final int MIN_PDP_ID = 0;
	public static final int MAX_PDP_ID = 10;
	public static final int MAX_PDP_COUNT = MAX_PDP_ID + 1;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Enums
	//
	// /////////////////////////////////
	
	public enum PdpType
	{
		IPv4,
		IPv6
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	private int apnId = 0;
	private String pdpAddress;
	private int pdpId = INVALID_PDP_ID;
	private int vpaa = 1;
	private int eqosId = 0;
	private PdpType pdpTy = PdpType.IPv4;
	private String pdpCh = new String("0-0");
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public PdpContext()
	{

	}
	
	public PdpContext(int pdpId)
	{
		if (pdpId < MIN_PDP_ID || pdpId > MAX_PDP_ID)
			pdpId = INVALID_PDP_ID;
		else
			this.pdpId = pdpId;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	public int getApnId()
	{
		return apnId;
	}

	public void setApnId(int apnId)
	{
		if (apnId < 0 || apnId > 16383)
			this.apnId = 0;
		else
			this.apnId = apnId;
	}

	public String getPdpAddress()
	{
		return pdpAddress;
	}

	public void setPdpAddress(String pdpAddress)
	{
		// first validate
		try
		{
			InetAddress.getByName(pdpAddress);
		}
		catch(Exception e)
		{
			return;
		}
		
		this.pdpAddress = pdpAddress;
	}

	public int getPdpId()
	{
		return pdpId;
	}

	public void setPdpId(int pdpId)
	{
		if (pdpId < MIN_PDP_ID || pdpId > MAX_PDP_ID)
			pdpId = INVALID_PDP_ID;
		else
			this.pdpId = pdpId;
	}

	public int getVpaa()
	{
		return vpaa;
	}

	public void setVpaa(int vpaa)
	{
		if (vpaa > 0)
			this.vpaa = 1;
		else
			this.vpaa = 0;
	}

	public int getEqosId()
	{
		return eqosId;
	}

	public void setEqosId(int eqosId)
	{
		if (eqosId < 0 || eqosId > 4095)
			this.eqosId = 0;
		else
			this.eqosId = eqosId;
	}

	public PdpType getPdpTy()
	{
		if (pdpTy != PdpType.IPv4 && pdpTy != PdpType.IPv6)
			return PdpType.IPv4;
		
		return pdpTy;
	}

	public void setPdpTy(PdpType pdpTy)
	{
		this.pdpTy = pdpTy;
	}

	public String getPdpCh()
	{
		return pdpCh;
	}

	public void setPdpCh(String pdpCh)
	{
		this.pdpCh = pdpCh;
	}
	
	@Override
	public Object clone()
	{
		PdpContext ret = new PdpContext();
		
		ret.setApnId(apnId);
		if (pdpAddress != null)
			ret.setPdpAddress(new String(pdpAddress));
		ret.setPdpId(pdpId);
		ret.setVpaa(vpaa);
		ret.setEqosId(eqosId);
		ret.setPdpTy(pdpTy);
		ret.setPdpCh(new String(pdpCh));
		
		return ret;
	}
	
	@Override
	public int compareTo(PdpContext o)
	{
		if (pdpId < o.getPdpId())
			return -1;
		else if (o.getPdpId() < pdpId)
			return 1;
		
		return 0;
	}
}
