package cs.dto;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.BeanUtils;

import hxc.ecds.protocol.rest.Promotion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GuiPromotion extends Promotion
{
	private boolean promoActive;

	private String startDateString;
	private String startTimeString;

	private String endDateString;
	private String endTimeString;
//	private DateTime startDateTime;
//	private DateTime endDateTime;

	private String transferRuleName;
	private String areaName;
	private String serviceClassName;
	private String bundleName;

	private BigDecimal guiRewardPercentage;

	public GuiPromotion(Promotion orig)
	{
		importPromotion(orig);
	}

	public void importPromotion(Promotion orig)
	{
		BeanUtils.copyProperties(orig, this);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (orig.getStartTime() != null)
		{
			String [] startDateArr = sdf.format( orig.getStartTime() ).split(" ");
			this.startDateString = startDateArr[0];
			this.startTimeString = startDateArr[1];
//			this.startDateTime = new DateTime(startDateArr[0], startDateArr[1]);
		}


		if (orig.getEndTime() != null)
		{
			String [] endDateArr = sdf.format( orig.getEndTime() ).split(" ");
			this.endDateString = endDateArr[0];
			this.endTimeString = endDateArr[1];
		}


		this.promoActive = ("A".equals( this.state ));
		if (this.rewardPercentage != null)
			this.guiRewardPercentage = this.rewardPercentage.multiply(new BigDecimal(100));
		else
			this.guiRewardPercentage = new BigDecimal(0);
	}

	private Date toDate(String date, String time) throws ParseException
	{
		time = (time == null)? "00:00:00" : time.trim();
		if (time.length() == 0)
		{
			time = "00:00:00";
		}
		else if (time.length() == 2)
		{
			time += ":00:00";
		}
		else if (time.length() == 5)
		{
			time += ":00";
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.parse( String.format("%s %s", date, time) );
	}

	public Promotion exportPromotion() throws ParseException
	{
		this.startTime = this.toDate(this.startDateString, this.startTimeString);
		this.endTime   = this.toDate(this.endDateString, this.endTimeString);

		this.state = (this.promoActive)? Promotion.STATE_ACTIVE : Promotion.STATE_DEACTIVATED;
		this.rewardPercentage = this.guiRewardPercentage.divide(new BigDecimal(100));

		Promotion promo = new Promotion();
		BeanUtils.copyProperties(this, promo);

		return promo;
	}

}
