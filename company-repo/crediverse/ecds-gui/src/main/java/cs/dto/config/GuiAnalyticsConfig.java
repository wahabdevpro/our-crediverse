package cs.dto.config;

import org.springframework.beans.BeanUtils;

import cs.utility.TimeUtility;
import hxc.ecds.protocol.rest.config.AnalyticsConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@SuppressWarnings("serial")
@NoArgsConstructor
@Getter
@Setter
public class GuiAnalyticsConfig extends AnalyticsConfig 
{
	private String scheduledHistoryGenerationTimeOfDayString = "00:00";
	
	public GuiAnalyticsConfig(AnalyticsConfig orig)
	{
		BeanUtils.copyProperties(orig, this);
		if (!this.scheduledHistoryGenerationTimeOfDay.equals(null))
		{
			scheduledHistoryGenerationTimeOfDayString = scheduledHistoryGenerationTimeOfDay.withSecond(0).withNano(0).toString();
		}
	}
	
	public AnalyticsConfig exportAnalyticsConfig() throws Exception
	{
		AnalyticsConfig config = new AnalyticsConfig();
		BeanUtils.copyProperties(this, config);
		
		if ( this.scheduledHistoryGenerationTimeOfDayString.isEmpty() )
		{
			config.setScheduledHistoryGenerationTimeOfDay(TimeUtility.convertStringToLocalTime("HH:mm", "00:00", "scheduledHistoryGenerationTimeOfDayString"));
		}
		else
		{
			TimeUtility.validateHourMinuteTime(scheduledHistoryGenerationTimeOfDayString, "scheduledHistoryGenerationTimeOfDayString");
			config.setScheduledHistoryGenerationTimeOfDay(TimeUtility.convertStringToLocalTime("HH:mm", scheduledHistoryGenerationTimeOfDayString, "scheduledHistoryGenerationTimeOfDayString"));
		}
		
		return config;
	}
}