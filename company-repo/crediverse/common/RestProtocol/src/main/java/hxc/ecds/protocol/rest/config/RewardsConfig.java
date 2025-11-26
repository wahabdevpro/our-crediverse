package hxc.ecds.protocol.rest.config;

import java.util.Date;
import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class RewardsConfig implements IConfiguration
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String REWARD_AMOUNT = "{RewardAmount}";
	public static final String NEW_BALANCE = "{NewBalance}";
	public static final String PROMOTION_NAME = "{PromotionName}";

	private static Phrase[] agentNotificationFields = new Phrase[] { //
			Phrase.en(REWARD_AMOUNT), Phrase.en(NEW_BALANCE), Phrase.en(PROMOTION_NAME), TransactionsConfig.PHRASE_TRANSACTION_NO };

	private static final long serialVersionUID = 3183929369454037964L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	protected int version;
	protected Phrase agentNotification = Phrase.en( //
			"You have received a " + REWARD_AMOUNT + " reward. Your new balance is " + NEW_BALANCE + ".  Ref " + TransactionsConfig.TRANSACTION_NO);
	
	protected boolean enableRewardProcessing = false;
	protected int rewardProcessingIntervalMinutes = 720; // Every 12 Hours
	protected Date rewardProcessingStartTimeOfDay = null; // null = Not Specified
	
	@SuppressWarnings("deprecation")
	protected Date smsStartTimeOfDay = new Date(100, 0, 1, 8, 0, 0);
	@SuppressWarnings("deprecation")
	protected Date smsEndTimeOfDay = new Date(100, 0, 1, 20, 0, 0);
	
	protected boolean failTransactionsUponFailureToLocate = false;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public Phrase[] listAgentNotificationFields()
	{
		return agentNotificationFields;
	}

	public Phrase getAgentNotification()
	{
		return agentNotification;
	}

	public RewardsConfig setAgentNotification(Phrase agentNotification)
	{
		this.agentNotification = agentNotification;
		return this;
	}	

	public int getRewardProcessingIntervalMinutes()
	{
		return rewardProcessingIntervalMinutes;
	}

	public RewardsConfig setRewardProcessingIntervalMinutes(int rewardProcessingIntervalMinutes)
	{
		this.rewardProcessingIntervalMinutes = rewardProcessingIntervalMinutes;
		return this;
	}

	public Date getRewardProcessingStartTimeOfDay()
	{
		return rewardProcessingStartTimeOfDay;
	}

	public RewardsConfig setRewardProcessingStartTimeOfDay(Date rewardProcessingStartTimeOfDay)
	{
		this.rewardProcessingStartTimeOfDay = rewardProcessingStartTimeOfDay;
		return this;
	}
	
	public Date getSmsStartTimeOfDay()
	{
		return smsStartTimeOfDay;
	}

	public RewardsConfig setSmsStartTimeOfDay(Date smsStartTimeOfDay)
	{
		this.smsStartTimeOfDay = smsStartTimeOfDay;
		return this;
	}

	public Date getSmsEndTimeOfDay()
	{
		return smsEndTimeOfDay;
	}

	public RewardsConfig setSmsEndTimeOfDay(Date smsEndTimeOfDay)
	{
		this.smsEndTimeOfDay = smsEndTimeOfDay;
		return this;
	}	
	
	public boolean isFailTransactionsUponFailureToLocate()
	{
		return failTransactionsUponFailureToLocate;
	}

	public RewardsConfig setFailTransactionsUponFailureToLocate(boolean failTransactionsUponFailureToLocate)
	{
		this.failTransactionsUponFailureToLocate = failTransactionsUponFailureToLocate;
		return this;
	}
	
	public boolean isEnableRewardProcessing()
	{
		return enableRewardProcessing;
	}

	public RewardsConfig setEnableRewardProcessing (boolean enableRewardProcessing)
	{
		this.enableRewardProcessing = enableRewardProcessing;
		return this;
	}
	
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

	public RewardsConfig setVersion(int version)
	{
		this.version = version;
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
		RewardsConfig template = new RewardsConfig();

		if (Phrase.nullOrEmpty(agentNotification))
			agentNotification = template.agentNotification;
		
		if (smsStartTimeOfDay == null)
			smsStartTimeOfDay = template.smsStartTimeOfDay;

		if (smsEndTimeOfDay == null)
			smsEndTimeOfDay = template.smsEndTimeOfDay;

		
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator() //
				.validExpandableText("agentNotification", agentNotification, agentNotificationFields) //
				.notLess("rewardProcessingIntervalMinutes", rewardProcessingIntervalMinutes, 0) //
				.notMore("rewardProcessingIntervalMinutes", rewardProcessingIntervalMinutes, 1440);
				
		;

		return validator.toList();
	}

}
