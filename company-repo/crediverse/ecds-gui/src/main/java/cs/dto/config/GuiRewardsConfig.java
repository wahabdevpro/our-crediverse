package cs.dto.config;

import org.springframework.beans.BeanUtils;

import cs.utility.TimeUtility;
import hxc.ecds.protocol.rest.config.RewardsConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@SuppressWarnings("serial")
public class GuiRewardsConfig extends RewardsConfig
{
	private String smsStartTimeOfDayString = null;
	private String smsEndTimeOfDayString = null;
	private String rewardProcessingStartTimeOfDayString = null;

	public GuiRewardsConfig(RewardsConfig orig)
	{
		BeanUtils.copyProperties(orig, this);

		if (this.smsStartTimeOfDay != null)
			smsStartTimeOfDayString = TimeUtility.convertDateToHourMinuteSecondString(smsStartTimeOfDay);

		if (this.smsEndTimeOfDay != null)
			smsEndTimeOfDayString = TimeUtility.convertDateToHourMinuteSecondString(smsEndTimeOfDay);

		if (this.rewardProcessingStartTimeOfDay != null)
			rewardProcessingStartTimeOfDayString = TimeUtility.convertDateToHourMinuteSecondString(rewardProcessingStartTimeOfDay);
	}

	public RewardsConfig exportRewardsConfig() throws Exception
	{
		RewardsConfig config = new RewardsConfig();
		BeanUtils.copyProperties(this, config);

		config.setRewardProcessingStartTimeOfDay(TimeUtility.validateAndConvertTime(rewardProcessingStartTimeOfDayString, "rewardProcessingStartTimeOfDayString", false));
		config.setSmsStartTimeOfDay(TimeUtility.validateAndConvertTime(smsStartTimeOfDayString, "smsStartTimeOfDayString", true));
		config.setSmsEndTimeOfDay(TimeUtility.validateAndConvertTime(smsEndTimeOfDayString, "smsEndTimeOfDayString", true));

		return config;
	}

}
