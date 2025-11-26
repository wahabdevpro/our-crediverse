package hxc.ecds.protocol.rest.config;

import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class AdjudicationConfig implements IConfiguration
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String SENDER_MSISDN = "{SenderMSISDN}";
	public static final String RECIPIENT_MSISDN = "{RecipientMSISDN}";
	public static final String AMOUNT = "{Amount}";
	public static final String ORIGINAL_NO = "{OriginalNo}";
	public static final String SENDER_NEW_BALANCE = "{SenderNewBalance}";
	public static final String PRODUCT_NAME = "{ProductName}";
	public static final String TRANSACTION_DATE = "{TransactionDate}";

	private static Phrase[] notificationFields = new Phrase[] { //
			Phrase.en(SENDER_MSISDN), Phrase.en(RECIPIENT_MSISDN), Phrase.en(AMOUNT), Phrase.en(ORIGINAL_NO), Phrase.en(SENDER_NEW_BALANCE), Phrase.en(PRODUCT_NAME), Phrase.en(TRANSACTION_DATE),
			TransactionsConfig.PHRASE_TRANSACTION_NO };

	private static final long serialVersionUID = -7791124422046136221L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	protected int version;

	protected Phrase requesterSuccessNotification = Phrase.en( //
			"Adjudication of Transaction " + ORIGINAL_NO + " for " + SENDER_MSISDN
					+ " completed. Transaction has been marked as Successful. Ref " + TransactionsConfig.TRANSACTION_NO);
	protected Phrase requesterFailureNotification = Phrase.en( //
			"Adjudication of Transaction " + ORIGINAL_NO + " for " + SENDER_MSISDN
					+ " completed. Transaction has been marked as Failed. Ref " + TransactionsConfig.TRANSACTION_NO);

	protected Phrase agentSuccessNotification = Phrase //
			.en("Adjudication of " + PRODUCT_NAME + " Sales Transaction  " + ORIGINAL_NO + " on " + TRANSACTION_DATE
					+ " completed. Transaction has been marked as Successful. Ref " + TransactionsConfig.TRANSACTION_NO);
	protected Phrase agentFailureNotification = Phrase//
			.en("Adjudication of " + PRODUCT_NAME + " Purchase Transaction " + ORIGINAL_NO + " on " + TRANSACTION_DATE + " completed. Transaction has been marked as Failed and your account balance adjusted accordingly. Ref "
					+ TransactionsConfig.TRANSACTION_NO);

	protected Phrase subscriberSuccessNotification = Phrase //
			.en("Adjudication of " + PRODUCT_NAME + " Purchase Transaction " + ORIGINAL_NO + " on " + TRANSACTION_DATE + " completed. Transaction has been marked as Successful. Ref "
					+ TransactionsConfig.TRANSACTION_NO);
	protected Phrase subscriberFailureNotification = Phrase //
			.en("Adjudication of " + PRODUCT_NAME + " Purchase Transaction " + ORIGINAL_NO + " on " + TRANSACTION_DATE
					+ " completed. Transaction has been marked as Failed. Please request refund from Agent. Ref " + TransactionsConfig.TRANSACTION_NO);

	protected Phrase airtimeProductName = Phrase.en("Airtime");
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public Phrase[] listNotificationFields()
	{
		return notificationFields;
	}

	public Phrase getRequesterSuccessNotification()
	{
		return requesterSuccessNotification;
	}

	public AdjudicationConfig setRequesterSuccessNotification(Phrase requesterSuccessNotification)
	{
		this.requesterSuccessNotification = requesterSuccessNotification;
		return this;
	}

	public Phrase getRequesterFailureNotification()
	{
		return requesterFailureNotification;
	}

	public AdjudicationConfig setRequesterFailureNotification(Phrase requesterFailureNotification)
	{
		this.requesterFailureNotification = requesterFailureNotification;
		return this;
	}

	public Phrase getAgentSuccessNotification()
	{
		return agentSuccessNotification;
	}

	public AdjudicationConfig setAgentSuccessNotification(Phrase agentSuccessNotification)
	{
		this.agentSuccessNotification = agentSuccessNotification;
		return this;
	}

	public Phrase getAgentFailureNotification()
	{
		return agentFailureNotification;
	}

	public AdjudicationConfig setAgentFailureNotification(Phrase agentFailureNotification)
	{
		this.agentFailureNotification = agentFailureNotification;
		return this;
	}

	public Phrase getSubscriberSuccessNotification()
	{
		return subscriberSuccessNotification;
	}

	public AdjudicationConfig setSubscriberSuccessNotification(Phrase subscriberSuccessNotification)
	{
		this.subscriberSuccessNotification = subscriberSuccessNotification;
		return this;
	}

	public Phrase getSubscriberFailureNotification()
	{
		return subscriberFailureNotification;
	}

	public AdjudicationConfig setSubscriberFailureNotification(Phrase subscriberFailureNotification)
	{
		this.subscriberFailureNotification = subscriberFailureNotification;
		return this;
	}
	
	public Phrase getAirtimeProductName()
	{
		return airtimeProductName;
	}

	public AdjudicationConfig setAirtimeProductName(Phrase airtimeProductName)
	{
		this.airtimeProductName = airtimeProductName;
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

	public AdjudicationConfig setVersion(int version)
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
		new AdjudicationConfig();
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
				.validExpandableText("requesterSuccessNotification", requesterSuccessNotification, notificationFields) //
				.validExpandableText("requesterFailureNotification", requesterFailureNotification, notificationFields) //
				.validExpandableText("agentSuccessNotification", agentSuccessNotification, notificationFields) //
				.validExpandableText("agentFailureNotification", agentFailureNotification, notificationFields) //
				.validExpandableText("subscriberSuccessNotification", subscriberSuccessNotification, notificationFields) //
				.validExpandableText("subscriberFailureNotification", subscriberFailureNotification, notificationFields) //
				.validExpandableText("airtimeProductName", airtimeProductName, new Phrase[0]) //
		;

		return validator.toList();
	}

}
