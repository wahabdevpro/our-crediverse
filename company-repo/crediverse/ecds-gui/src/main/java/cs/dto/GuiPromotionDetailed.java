package cs.dto;

import hxc.ecds.protocol.rest.Area;
import hxc.ecds.protocol.rest.Bundle;
import hxc.ecds.protocol.rest.Promotion;
import hxc.ecds.protocol.rest.ServiceClass;
import hxc.ecds.protocol.rest.TransferRule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GuiPromotionDetailed extends GuiPromotion
{
	public enum TargetPeriod
	{
		perDay,
		perWeek,
		perMonth,
		perCalendarDay,
		perCalendarWeek,
		perCalendarMonth;

		public static TargetPeriod resolvePeriod(int period)
		{
			switch(period)
			{
				case PER_DAY:
					return perDay;

				case PER_WEEK:
					return perWeek;

				case PER_MONTH:
					return perMonth;

				case PER_CALENDAR_DAY:
					return perCalendarDay;

				case PER_CALENDAR_WEEK:
					return perCalendarWeek;

				case PER_CALENDAR_MONTH:
					return perCalendarMonth;

				default:
					return null;
			}
		}
	}

	private TransferRule transferRule;
	private Area area;
	private ServiceClass serviceClass;
	private Bundle bundle;

	private TargetPeriod targetPeriodInfo;

	public GuiPromotionDetailed(Promotion orig)
	{
		super(orig);

		targetPeriodInfo = TargetPeriod.resolvePeriod(targetPeriod);
	}
}
