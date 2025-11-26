package cs.dto;

import java.util.ArrayList;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonInclude;

import hxc.ecds.protocol.rest.reports.ReportSchedule;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class GuiReportSchedule extends ReportSchedule
{
	private String timeOfDayString;
	private String startTimeOfDayString;
	private String endTimeOfDayString;
	private Boolean deliveryChannelEmail;
	private Boolean deliveryChannelSms;
	private ArrayList<Integer> webUserIds = new ArrayList<Integer>();
	private ArrayList<Integer> agentUserIds = new ArrayList<Integer>();

	public GuiReportSchedule()
	{
	}

	public GuiReportSchedule(ReportSchedule orig)
	{
		BeanUtils.copyProperties(orig, this);

		setTimeOfDayString(secondsToString(orig.getTimeOfDay()));
		setStartTimeOfDayString(secondsToString(orig.getStartTimeOfDay()));
		setEndTimeOfDayString(secondsToString(orig.getEndTimeOfDay()));
	}

	public ReportSchedule getReportSchedule()
	{
		ReportSchedule schedule = new ReportSchedule();
		BeanUtils.copyProperties(this, schedule);
		return schedule;
	}

	static private String secondsToString(Integer seconds)
	{
		return String.format("%02d:%02d", seconds / 3600, (seconds % 3600) / 60);
	}
}
