package hxc.ecds.protocol.rest.config;

import java.time.LocalTime;
import java.util.List;

import hxc.ecds.protocol.rest.Violation;

public class AnalyticsConfig implements IConfiguration 
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
		
	private static final long serialVersionUID = -4070833377755861069L;	
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	protected int version;
	
	
	// ---- FIXME -- When the CONFIGURATION no longer saves as "OBJECTS" in binary format, then I can save this correctly as a NULL type (the preferred default)

	protected LocalTime scheduledHistoryGenerationTimeOfDay = LocalTime.parse("02:00").withNano(0).withSecond(0);
	
	protected boolean enableAnalytics = true;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@Override
	public long uid()
	{
		return serialVersionUID;
	}

	@Override
	public int getVersion()
	{
		return version;
	}

	public AnalyticsConfig setVersion(int version)
	{
		this.version = version;
		return this;
	}
	
	public LocalTime getScheduledHistoryGenerationTimeOfDay()
	{
		return this.scheduledHistoryGenerationTimeOfDay;
	}

	public AnalyticsConfig setScheduledHistoryGenerationTimeOfDay(LocalTime scheduledHistoryGenerationTimeOfDay)
	{
		this.scheduledHistoryGenerationTimeOfDay = scheduledHistoryGenerationTimeOfDay.withSecond(0).withNano(0);
		return this;
	}
	
	public boolean isEnableAnalytics()
	{
		return enableAnalytics;
	}

	public AnalyticsConfig setEnableAnalytics(boolean enableAnalytics)
	{
		this.enableAnalytics = enableAnalytics;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Post-Load fix up
	//
	// /////////////////////////////////
	@Override
	public void onPostLoad()
	{
	}
	
	@Override
	public List<Violation> validate()
	{
		List<Violation> list = null;
		
		return list;
	}
	
}
