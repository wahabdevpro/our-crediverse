package hxc.utils.protocol.caisim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Contains the HLR subscription data in CAISIM.
 * 
 * @author petar
 *
 */
public class HlrSubscription implements Cloneable
{
	final static Logger logger = LoggerFactory.getLogger(HlrSubscription.class);
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
	private ArrayList<PdpContext> pdpContexts = new ArrayList<PdpContext>();
	private String imei = new String();
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public HlrSubscription()
	{

	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////
	
	public int getObr()
	{
		return obr;
	}
	
	/**
	 * Set the OBR parameter value.
	 * 
	 * @param obr OBR parameter value, range[0,2]
	 */
	public void setObr(int obr)
	{
		if (obr < 0 || obr > 2)
			this.obr = 0;
		else
			this.obr = obr;
	}
	
	public int getRsa()
	{
		return rsa;
	}
	
	/**
	 * Sets the RSA parameter value.
	 * 
	 * @param rsa RSA parameter value, range[0, inf)
	 */
	public void setRsa(int rsa)
	{
		if ( rsa < 0)
			this.rsa = 0;
		else
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

	/**
	 * Sets the NAM parameter value.
	 * 
	 * @param nam NAM parameter value, range [0,2]
	 */
	public void setNam(int nam)
	{
		if (nam > 2 || nam < 0)
			this.nam = 0;
		else
			this.nam = nam;
	}

	public int getPdpCp()
	{
		return pdpCp;
	}

	/**
	 * Sets the PDPCP parameter value.
	 * 
	 * @param pdpCp PDPCP parameter value, range [0,255]
	 */
	public void setPdpCp(int pdpCp)
	{
		if (pdpCp > 255 || pdpCp < 0)
			this.pdpCp = 1;
		else
			this.pdpCp = pdpCp;
	}
	
	public PdpContext[] getPdpContexts()
	{
		return (PdpContext[]) pdpContexts.toArray(new PdpContext[pdpContexts.size()]);
	}
	
	public void addPdpContexts(PdpContext[] pdpsToAdd)
	{
		for(PdpContext pdpToAdd : pdpsToAdd)
		{
			addPdpContext(pdpToAdd);
		}
	}

	/**
	 * Adds a PDP context to the subscription in the HLR.
	 * 
	 * If the PDPID is not specified - the next available one is selected.
	 * If the PDPID is specified and it is existing - it gets replaced.
	 * 
	 * PDP contexts are sorted on the PDP ID at all times.
	 * 
	 * @param pdp the PDP context to be added
	 */
	public void addPdpContext(PdpContext pdp)
	{
		try
		{
			boolean replaced = false;
			
			if (pdp.getPdpId() == PdpContext.INVALID_PDP_ID)
			{
				final int nextPdpId = getNextAvailablePdpId();
				if (nextPdpId == PdpContext.INVALID_PDP_ID)
					return;
				
				pdp.setPdpId(nextPdpId);
			}
			
			for(int i = 0; i < this.pdpContexts.size(); ++i)
			{
				PdpContext existingPdp = this.pdpContexts.get(i);
				if (pdp.getPdpId() == existingPdp.getPdpId())
				{
					this.pdpContexts.remove(i);
					addPddUnieuqApnIdPdpAddr(pdp);
					replaced = true;
					break;
				}
			}
			
			if (!replaced)
				addPddUnieuqApnIdPdpAddr(pdp);
		}
		finally
		{
			sortPdpContexts();
		}
	}
	
	public void setPdpContexts(PdpContext[] pdps)
	{
		this.pdpContexts.clear();
		addPdpContexts(pdps);
	}
	
	public String getImei()
	{
		return imei;
	}
	
	/**
	 * Set the IMEI parameter value.
	 * 
	 * @param imei IMEI parameter value, String of digits
	 */
	public void setImei(String imei)
	{
		this.imei = imei;
	}
	
	/**
	 * Deletes a single PDP context by an unique PDPID.
	 * 
	 * @param pdpId the PDPID of the PDP context to delete
	 */
	public void deletePdpContextByPdpId(int pdpId)
	{	
		ArrayList<PdpContext> remainingPdpContexts = new ArrayList<PdpContext>();
		
		for (PdpContext pdp : pdpContexts)
		{
			if (pdp.getPdpId() != pdpId)
				remainingPdpContexts.add(pdp);
		}
		
		pdpContexts = remainingPdpContexts;
	}
	
	/**
	 * Deletes a single PDP context with the passed APNID and PDP ADDRESS.
	 * 
	 * @param apnId the APNID of the PDP context to delete
	 * @param pdpAddress the PDP ADDRESS of the PDP context to delete
	 */
	public void deletePdpContext(int apnId, InetAddress pdpAddress)
	{
		ArrayList<PdpContext> remainingPdpContexts = new ArrayList<PdpContext>();

		for (PdpContext pdp : pdpContexts)
		{
			try
			{
				if (pdp.getApnId() != apnId || !InetAddress.getByName(pdp.getPdpAddress()).equals(pdpAddress))
					remainingPdpContexts.add(pdp);
			}
			catch (Exception e)
			{
				remainingPdpContexts.add(pdp);
			}
		}
		
		pdpContexts = remainingPdpContexts;
	}
	
	/**
	 * Deletes all PDP contexts with the passed APNID.
	 * 
	 * @param apnId the APNID of the PDP context to delete
	 */
	public void deletePdpContextsByApnId(int apnId)
	{
		ArrayList<PdpContext> remainingPdpContexts = new ArrayList<PdpContext>();
		
		for (PdpContext pdp : pdpContexts)
		{
			if (pdp.getApnId() != apnId)
				remainingPdpContexts.add(pdp);
		}
		
		pdpContexts = remainingPdpContexts;
	}
	
	@Override
	public Object clone()
	{
		HlrSubscription ret = new HlrSubscription();
		
		ret.setObr(obr);
		ret.setRsa(rsa);
		ret.setCfnrc((Cfnrc) cfnrc.clone());
		ret.setNam(nam);
		ret.setPdpCp(pdpCp);
		
		PdpContext[] pdps = new PdpContext[pdpContexts.size()];
		for (int i = 0; i < pdpContexts.size(); ++i)
		{
			pdps[i] = (PdpContext) pdpContexts.get(i).clone();
		}
		
		ret.setPdpContexts(pdps);
		
		return ret;
	}
	
	/**
	 * Ensures that the APNID + PDP ADDRESS combination is unique in the list of PDP contexts.
	 * 
	 * @param pdp the PDP context to add
	 */
	private void addPddUnieuqApnIdPdpAddr(PdpContext pdp)
	{
		for(int i = 0; i < this.pdpContexts.size(); ++i)
		{
			PdpContext existingPdp = this.pdpContexts.get(i);
			if (pdp.getApnId() == existingPdp.getApnId() && pdp.getPdpAddress().equals(existingPdp.getPdpAddress()))
			{
				this.pdpContexts.remove(i);
				this.pdpContexts.add(pdp);
				return;
			}
		}
		
		this.pdpContexts.add(pdp);
	}
	
	/**
	 * Sorts the list of PDP contexts by PDPID.
	 */
	private void sortPdpContexts()
	{
		Collections.sort(this.pdpContexts);
	}
	
	/**
	 * Gets the next available PDPID.
	 * 
	 * @return the next available PDPID or PdpContext.INVALID_PDP_ID if none is available
	 */
	private int getNextAvailablePdpId()
	{
		if (pdpContexts.size() == PdpContext.MAX_PDP_COUNT)
			return PdpContext.INVALID_PDP_ID;
		
		int next = 0;
		for(PdpContext pdp : pdpContexts)
		{
			if (next == pdp.getPdpId())
				++next;
		}
		
		return next;
	}
		
	public static void main(String[] args) throws UnknownHostException
	{
		HlrSubscription s = new HlrSubscription();
	
		PdpContext p1 = new PdpContext(1);
		PdpContext p2 = new PdpContext(1);
		p2.setApnId(2);
		p2.setPdpAddress("10.1.1.2");
		s.addPdpContext(p1);
		s.addPdpContext(p2);
		
		PdpContext p3 = new PdpContext(2);
		p3.setApnId(2);
		p3.setPdpAddress("10.1.1.3");
		s.addPdpContext(p3);
		
		PdpContext p4 = new PdpContext(3);
		p4.setApnId(2);
		p4.setPdpAddress("10.1.1.3");
		s.addPdpContext(p4);
		
		PdpContext p5 = new PdpContext(4);
		p5.setApnId(4);
		p5.setPdpAddress("10.1.1.5");
		s.addPdpContext(p5);
		
		for(PdpContext p : s.getPdpContexts())
		{
			logger.info("PDPID: " + p.getPdpId() + ", APNID: " + p.getApnId() + ", PDP ADDR: " + p.getPdpAddress());
		}
		logger.info("=======================================");
		
		s.deletePdpContextByPdpId(1);
		s.deletePdpContext(2, InetAddress.getByName("10.1.1.2"));
		s.deletePdpContext(2, InetAddress.getByName("10.1.1.3"));
		
		for(PdpContext p : s.getPdpContexts())
		{
			logger.info("PDPID: " + p.getPdpId() + ", APNID: " + p.getApnId() + ", PDP ADDR: " + p.getPdpAddress());
		}
		logger.info("=======================================");
		
		s.addPdpContext(p2);
		s.addPdpContext(p4);
		s.addPdpContext(p5);
		
		s.deletePdpContextsByApnId(2);
		
		for(PdpContext p : s.getPdpContexts())
		{
			logger.info("PDPID: " + p.getPdpId() + ", APNID: " + p.getApnId() + ", PDP ADDR: " + p.getPdpAddress());
		}
		logger.info("=======================================");
	}
}
