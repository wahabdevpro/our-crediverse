package hxc.ecds.protocol.rest.config;

import java.math.BigDecimal;
import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class ReversalsConfig implements IConfiguration
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
	public static final String RECIPIENT_NEW_BALANCE = "{RecipientNewBalance}";

	private static Phrase[] notificationFields = new Phrase[] { //
			Phrase.en(SENDER_MSISDN), Phrase.en(RECIPIENT_MSISDN), Phrase.en(AMOUNT), Phrase.en(ORIGINAL_NO), //
			Phrase.en(SENDER_NEW_BALANCE), Phrase.en(RECIPIENT_NEW_BALANCE), TransactionsConfig.PHRASE_TRANSACTION_NO };

	private static Phrase[] externalDataFields = new Phrase[] {
			Phrase.en(SENDER_MSISDN),
			Phrase.en(RECIPIENT_MSISDN),
			Phrase.en(AMOUNT),
			TransactionsConfig.PHRASE_TRANSACTION_NO, Phrase.en(ORIGINAL_NO)
	};

	private static final long serialVersionUID = 1015009578045101618L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	protected int version;
	protected Phrase requesterNotification = Phrase.en(
			"Transaction " + ORIGINAL_NO + " between " + SENDER_MSISDN + " and " + RECIPIENT_MSISDN + " has been reversed to the value of " + AMOUNT + ". Ref " + TransactionsConfig.TRANSACTION_NO);
	protected Phrase senderNotification = Phrase
			.en("Your Transaction " + ORIGINAL_NO + " has been reversed to the value of " + AMOUNT + ". Your new balance = " + SENDER_NEW_BALANCE + ". Ref " + TransactionsConfig.TRANSACTION_NO);
	protected Phrase recipientNotification = Phrase
			.en("Your Transaction " + ORIGINAL_NO + " has been reversed to the value of " + AMOUNT + ". Your new balance = " + RECIPIENT_NEW_BALANCE + ". Ref " + TransactionsConfig.TRANSACTION_NO);
	protected BigDecimal charge = BigDecimal.ZERO;

	protected Phrase ubadExternalData1 = Phrase.en(SENDER_MSISDN);
	protected Phrase ubadExternalData2 = Phrase.en(SENDER_MSISDN);

	protected Boolean enableCoAuthReversal = true;

	protected Boolean enableDedicatedAccountReversal = false;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public Phrase[] listNotificationFields()
	{
		return notificationFields;
	}

	public Phrase[] listExternalDataFields()
	{
		return externalDataFields;
	}

	public Phrase getRequesterNotification()
	{
		return requesterNotification;
	}

	public ReversalsConfig setRequesterNotification(Phrase requesterNotification)
	{
		this.requesterNotification = requesterNotification;
		return this;
	}

	public Phrase getSenderNotification()
	{
		return senderNotification;
	}

	public ReversalsConfig setSenderNotification(Phrase senderNotification)
	{
		this.senderNotification = senderNotification;
		return this;
	}

	public Phrase getRecipientNotification()
	{
		return recipientNotification;
	}

	public ReversalsConfig setRecipientNotification(Phrase recipientNotification)
	{
		this.recipientNotification = recipientNotification;
		return this;
	}

	public BigDecimal getCharge()
	{
		return charge;
	}

	public ReversalsConfig setCharge(BigDecimal charge)
	{
		this.charge = charge;
		return this;
	}

	public Phrase getUbadExternalData1()
	{
		return this.ubadExternalData1;
	}
	public ReversalsConfig setUbadExternalData1( Phrase ubadExternalData1 )
	{
		this.ubadExternalData1 = ubadExternalData1;
		return this;
	}

	public Phrase getUbadExternalData2()
	{
		return this.ubadExternalData2;
	}
	public ReversalsConfig setUbadExternalData2( Phrase ubadExternalData2 )
	{
		this.ubadExternalData2 = ubadExternalData2;
		return this;
	}

	public boolean isEnableCoAuthReversal() {
		return enableCoAuthReversal;
	}

	public void setEnableCoAuthReversal(boolean enableCoAuthReversal) {
		this.enableCoAuthReversal = enableCoAuthReversal;
	}

	public Boolean isEnableDedicatedAccountReversal() {
		return enableDedicatedAccountReversal;
	}

	public void setEnableDedicatedAccountReversal(Boolean enableDedicatedAccountReversal) {
		this.enableDedicatedAccountReversal = enableDedicatedAccountReversal;
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

	public ReversalsConfig setVersion(int version)
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
		ReversalsConfig template = new ReversalsConfig();

		if (nullOrEmpty(requesterNotification))
			requesterNotification = template.requesterNotification;

		if (nullOrEmpty(senderNotification))
			senderNotification = template.senderNotification;

		if (nullOrEmpty(recipientNotification))
			recipientNotification = template.recipientNotification;

		if (ubadExternalData1 == null) ubadExternalData1 = template.ubadExternalData1;
		if (ubadExternalData2 == null) ubadExternalData2 = template.ubadExternalData2;
		
		if (enableCoAuthReversal == null) {
			enableCoAuthReversal = true;
		}

		if (enableDedicatedAccountReversal == null) {
			enableDedicatedAccountReversal = false;
		}
	}

	private boolean nullOrEmpty(Phrase phrase)
	{
		return phrase == null || phrase.empty();
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
				.validExpandableText("senderNotification", senderNotification, notificationFields) //
				.validExpandableText("recipientNotification", recipientNotification, notificationFields) //
				.notLess("charge", charge, BigDecimal.ZERO) //
				.isMoney("charge", charge) //
				.validExpandableText("ubadExternalData1", ubadExternalData1, externalDataFields) //
				.validExpandableText("ubadExternalData2", ubadExternalData2, externalDataFields) //
		;

		return validator.toList();
	}

}
