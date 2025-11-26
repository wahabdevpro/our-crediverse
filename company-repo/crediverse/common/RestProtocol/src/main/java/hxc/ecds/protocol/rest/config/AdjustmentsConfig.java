package hxc.ecds.protocol.rest.config;

import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class AdjustmentsConfig implements IConfiguration
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String WEB_USER = "{WebUser}";
	public static final String AGENT_MSIDN = "{AgentMsisdn}";
	public static final String NEW_BALANCE = "{NewBalance}";
	public static final String NEW_BONUS_BALANCE = "{NewBonusBalance}";
	public static final String NEW_TOTAL_BALANCE = "{NewTotalBalance}";

	public static final String FILE_NAME = "{Filename}";
	public static final String RECORD_COUNT = "{RecordCount}";
	public static final String BATCH_TIME = "{BatchTime}";
	public static final String TOTAL_ADJUST_AMOUNT = "{TotalAdjustAmount}";
	public static final String TOTAL_ADJUST_AMOUNT_WITH_PROVISIONS = "{TotalAdjustAmountWithProvisions}";

	private static Phrase[] notificationFields = new Phrase[] { //
			Phrase.en(WEB_USER), Phrase.en(AGENT_MSIDN), Phrase.en(NEW_BALANCE), Phrase.en(NEW_BONUS_BALANCE), //
			TransactionsConfig.PHRASE_TRANSACTION_NO };

	private static Phrase[] agentNotificationFields = new Phrase[] { //
			Phrase.en(WEB_USER), Phrase.en(AGENT_MSIDN), Phrase.en(NEW_BALANCE), Phrase.en(NEW_BONUS_BALANCE), //
			Phrase.en(NEW_TOTAL_BALANCE), TransactionsConfig.PHRASE_TRANSACTION_NO };

	private static Phrase[] batchNotificationFields = new Phrase[] { //
			Phrase.en(FILE_NAME), Phrase.en(RECORD_COUNT), Phrase.en(BATCH_TIME), Phrase.en(TOTAL_ADJUST_AMOUNT), //
			Phrase.en(TOTAL_ADJUST_AMOUNT_WITH_PROVISIONS) };

	private static final long serialVersionUID = 4173975471373367387L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	protected int version;
	protected Phrase notification = Phrase.en( //
			"Agent account " + AGENT_MSIDN + " has been adjusted to " + NEW_BALANCE + "/" + NEW_BONUS_BALANCE + ". Ref " + TransactionsConfig.TRANSACTION_NO);
	protected Phrase agentNotification = Phrase.en( //
			"Your ECDS account balance has been adjusted to " + NEW_BALANCE + " by " + WEB_USER + ". Ref " + TransactionsConfig.TRANSACTION_NO);
	protected Phrase batchNotification = Phrase.en( //
			"Batch file '" + FILE_NAME + "' with " + RECORD_COUNT + " record(s) was processed on " + BATCH_TIME + ". Total of Adjustment " //
					+ "Amount " + TOTAL_ADJUST_AMOUNT + " and including Bonus Provision " + TOTAL_ADJUST_AMOUNT_WITH_PROVISIONS);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public Phrase[] listNotificationFields()
	{
		return notificationFields;
	}

	public Phrase getNotification()
	{
		return notification;
	}

	public AdjustmentsConfig setNotification(Phrase notification)
	{
		this.notification = notification;
		return this;
	}

	public Phrase[] listAgentNotificationFields()
	{
		return agentNotificationFields;
	}

	public Phrase getAgentNotification()
	{
		return agentNotification;
	}

	public AdjustmentsConfig setAgentNotification(Phrase agentNotification)
	{
		this.agentNotification = agentNotification;
		return this;
	}
	
	public Phrase[] listBatchNotificationFields()
	{
		return batchNotificationFields;
	}
	
	public Phrase getBatchNotification()
	{
		return batchNotification;
	}

	public AdjustmentsConfig setBatchNotification(Phrase batchNotification)
	{
		this.batchNotification = batchNotification;
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

	public AdjustmentsConfig setVersion(int version)
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
		AdjustmentsConfig template = new AdjustmentsConfig();
		
		if (Phrase.nullOrEmpty(notification))
			notification = template.notification;
		
		if (Phrase.nullOrEmpty(agentNotification))
			agentNotification = template.agentNotification;
		
		if (Phrase.nullOrEmpty(batchNotification))
			batchNotification = template.batchNotification;
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
				.validExpandableText("notification", notification, notificationFields) //
				.validExpandableText("agentNotification", agentNotification, agentNotificationFields) //
				.validExpandableText("batchNotification", batchNotification, batchNotificationFields) //
		;

		return validator.toList();
	}

}
