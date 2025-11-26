package cs.dto;

import java.util.EnumSet;

import org.springframework.beans.BeanUtils;

import hxc.ecds.protocol.rest.TransferRule;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiTransferRule extends TransferRule
{
	protected String sourceTierName;
	protected String targetTierName;
	protected String serviceClassName;
	protected String targetServiceClassName;
	protected String groupName;
	protected String targetGroupName;
	protected EnumSet<DaysOfWeekEnum> currentDays = EnumSet.allOf(DaysOfWeekEnum.class);
	protected StateEnum currentState;
	protected boolean ruleActive;
	protected String buyerTradeBonusPercentageString;
	protected String tradeBonusCumulativePercentageString;
	protected String targetBonusPercentageString;
	protected String startTimeOfDayString;
	protected String endTimeOfDayString;
	protected String areaName;

	public enum StateEnum {
		ACTIVE(STATE_ACTIVE),
		INACTIVE(STATE_INACTIVE);
		private String val;
		private StateEnum(String val) {
			this.val = val.toUpperCase();
		}

		public String getVal(String val)
		{
			return this.val;
		}

		public static StateEnum fromString(String val)
		{
			StateEnum result = INACTIVE;
			if (val != null)
			{
				switch (val)
				{
					case STATE_ACTIVE:
						result = ACTIVE;
						break;
					case STATE_INACTIVE:
						result = INACTIVE;
						break;
				}
			}
			return result;
		}
	}

	private enum DaysOfWeekEnum {
		MONDAY(DOW_MONDAYS),
		TUESDAY(DOW_TUESDAYS),
		WEDNESDAY(DOW_WEDNESDAYS),
		THURSDAY(DOW_THURSDAYS),
		FRIDAY(DOW_FRIDAYS),
		SATURDAY(DOW_SATURDAYS),
		SUNDAY(DOW_SUNDAYS);
		private int val;
		private DaysOfWeekEnum(int val) {
			this.val = val;
		}

		public int getVal()
		{
			return this.val;
		}
	}

	public static Integer toInteger(EnumSet<DaysOfWeekEnum> set) {
		int result = 0;
		if (set != null)
		{
			for (Object item : set.toArray())
			{
				result+=((DaysOfWeekEnum)item).getVal();
			}
		}

		return result;
	}

	public static EnumSet<DaysOfWeekEnum>toSet(Integer b)
	{
		EnumSet<DaysOfWeekEnum> enumSet = EnumSet.noneOf(DaysOfWeekEnum.class);
		if (b != null)
		{
			for (DaysOfWeekEnum item : DaysOfWeekEnum.values())
			{
				if ((b.intValue() & item.getVal()) != 0)
				{
					enumSet.add(item);
				}
			}
		}
		return enumSet;
	}

	public GuiTransferRule()
	{

	}

	public GuiTransferRule(TransferRule orig)
	{
		currentDays = toSet(orig.getDaysOfWeek());
		currentState = StateEnum.fromString(state);
		ruleActive = (currentState == StateEnum.ACTIVE)?true:false;
		BeanUtils.copyProperties(orig, this);
	}
}
