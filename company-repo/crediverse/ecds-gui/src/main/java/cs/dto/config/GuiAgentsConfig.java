package cs.dto.config;

import java.text.SimpleDateFormat;

import org.springframework.beans.BeanUtils;

import cs.utility.TimeUtility;
import hxc.ecds.protocol.rest.config.AgentsConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@SuppressWarnings("serial")
@NoArgsConstructor
@Getter
@Setter
public class GuiAgentsConfig extends AgentsConfig
{

	private String scheduledAccountDumpStartTimeOfDayString = null;

	public GuiAgentsConfig(AgentsConfig orig)
	{
		BeanUtils.copyProperties(orig, this);
		if (this.scheduledAccountDumpStartTimeOfDay != null)
		{
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			scheduledAccountDumpStartTimeOfDayString = sdf.format(scheduledAccountDumpStartTimeOfDay);
		}
	}

	public AgentsConfig exportAgentsConfig() throws Exception
	{
		AgentsConfig config = new AgentsConfig();
		BeanUtils.copyProperties(this, config);

		if (this.scheduledAccountDumpStartTimeOfDayString == null || scheduledAccountDumpStartTimeOfDayString.length()==0)
		{
			config.setScheduledAccountDumpStartTimeOfDay(null);
		}
		else
		{
			TimeUtility.validateHourMinuteSecondTime(scheduledAccountDumpStartTimeOfDayString, "scheduledAccountDumpStartTimeOfDayString");
			config.setScheduledAccountDumpStartTimeOfDay(TimeUtility.convertHourMinuteSecondStringToDate(scheduledAccountDumpStartTimeOfDayString, "scheduledAccountDumpStartTimeOfDayString"));
		}

		return config;
	}



}
