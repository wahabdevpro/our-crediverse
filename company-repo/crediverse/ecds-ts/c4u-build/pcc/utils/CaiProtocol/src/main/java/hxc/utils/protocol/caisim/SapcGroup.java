package hxc.utils.protocol.caisim;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Represents a SAPC Group.
 * 
 * @author petar
 *
 */
public class SapcGroup implements Cloneable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private String id = new String();
	private int priority = 0;
	private Date startDate = new Date();
	private Date endDate = new Date(startDate.getTime() + TimeUnit.DAYS.toMillis(1));
	private boolean endDateSet = false;
	private long quota = 0;
	private boolean zainLte = false;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////
	
	public SapcGroup()
	{
		
	}
	
	public SapcGroup(String id, int priority, Date startDate, Date endDate, boolean zainLte)
	{
		this.id = id;
		this.priority = priority;
		this.startDate = startDate;
		this.endDate = endDate;
		this.quota = 0;
		this.zainLte = zainLte;
	}
	
	public SapcGroup(String id)
	{
		this.id = id;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
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
	
	public int getPriority()
	{
		return priority;
	}
	
	public void setPriority(int priority)
	{
		if (priority > 0)
			this.priority = priority;
	}
	
	public Date getStartDate()
	{
		return startDate;
	}
	
	public void setStartDate(Date startDate)
	{
		if (!endDateSet)
			endDate = new Date(startDate.getTime() + TimeUnit.DAYS.toMillis(1));
			
		this.startDate = startDate;
	}
	
	public Date getEndDate()
	{
		return endDate;
	}
	
	public void setEndDate(Date endDate)
	{		
		this.endDate = endDate;
		endDateSet = true;
	}
	
	public long getQuota()
	{
		return quota;
	}

	public void setQuota(long quota)
	{
		if (quota >= 0)
			this.quota = quota;
	}
	
	public boolean getZainLte()
	{
		return zainLte;
	}
	
	public void setZainLte(boolean zainLte)
	{
		this.zainLte = zainLte;
	}
	
	@Override
	public String toString()
	{
		DateFormat df = new SimpleDateFormat(CaiCommon.CAI_DATE_FORMAT);
		
		return "SUBSCRIBERGROUPNAME," + CaiCommon.escapeValue(id) + ",SELECTED,\"yes\",ABSACCUMULATED,ABSACCUMULATEDBIDIRVOLUME," + quota + 
				",ABSACCUMULATEDEXPIRYDATE,ABSACCUMULATEDEXPIRYDATEVOLUME,\"" + df.format(getEndDate()) + "\"," +
				"ABSACCUMULATEDRESETPERIOD,ABSACCUMULATEDRESETPERIODVOLUME,\"1 days\"";
	}
	
	@Override
	public Object clone()
	{
		SapcGroup ret = new SapcGroup();
		
		ret.setId(new String(id));
		ret.setPriority(priority);
		ret.setStartDate((Date) startDate.clone());
		if (endDateSet )
			ret.setEndDate((Date) endDate.clone());
		ret.setQuota(quota);
		ret.setZainLte(zainLte);
		
		return ret;
	}
}
