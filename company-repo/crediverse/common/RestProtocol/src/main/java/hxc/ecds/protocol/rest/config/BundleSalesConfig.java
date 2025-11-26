package hxc.ecds.protocol.rest.config;

import static hxc.ecds.protocol.rest.config.Phrase.en;

import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class BundleSalesConfig implements IConfirmationMenuConfig
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String SENDER_MSISDN = "{SenderMSISDN}";
	public static final String RECIPIENT_MSISDN = "{RecipientMSISDN}";
	public static final String PIN = "{PIN}";
	public static final String PRICE = "{Price}";
	public static final String BUNDLE_NAME = "{BundleName}";
	public static final String SENDER_NEW_BALANCE = "{SenderNewBalance}";
	public static final String USSD_CODE = "{UssdCode}";
	public static final String SMS_KEYWORD = "{SmsKeyword}";
	public static final String SENDER_OLD_BALANCE = "{SenderOldBalance}";
	public static final String AMOUNT = "{Amount}";
	public static final String ITEM_DESCRIPTION = "{ItemDescription}";
	public static final String CONSUMER_MSISDN = "{consumerMsisdn}";

	private static Phrase[] ussdCommandFields = new Phrase[] { //
			Phrase.en(RECIPIENT_MSISDN), Phrase.en(PIN), Phrase.en(USSD_CODE) };

	private static Phrase[] smsCommandFields = new Phrase[] { //
			Phrase.en(RECIPIENT_MSISDN), Phrase.en(PIN), Phrase.en(SMS_KEYWORD) };

	private static Phrase[] notificationFields = new Phrase[] { //
			Phrase.en(SENDER_MSISDN), Phrase.en(RECIPIENT_MSISDN), Phrase.en(PRICE), Phrase.en(MINS_SINCE_LAST), //
			Phrase.en(SENDER_NEW_BALANCE), Phrase.en(BUNDLE_NAME), TransactionsConfig.PHRASE_TRANSACTION_NO,
			en(SENDER_OLD_BALANCE), en(AMOUNT), en(ITEM_DESCRIPTION), en(CONSUMER_MSISDN) };

	private static final long serialVersionUID = 643981996905652862L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	protected int version;

	protected Phrase senderDebitNotification = en("Your account has been debited with " + AMOUNT + " for " + ITEM_DESCRIPTION
																 + " offered to " + CONSUMER_MSISDN + ". You will earn x% commission.");

	protected Phrase senderDebitBalanceNotification = en("Your balance was " + SENDER_OLD_BALANCE + " and now your balance is "
																		+ SENDER_NEW_BALANCE + ".");

	protected Phrase senderRefundNotification = en("Your account has been credited with " + AMOUNT + " for " + ITEM_DESCRIPTION
																  + " not given to " + CONSUMER_MSISDN + ".");

	protected Phrase senderRefundBalanceNotification = senderDebitBalanceNotification;										  

	protected Phrase senderCompleteNotification = Phrase.en("You have Sold " + PRICE + " " + BUNDLE_NAME + " Bundle to " + RECIPIENT_MSISDN + //
			". Your new Balance is " + SENDER_NEW_BALANCE + ". Ref " + TransactionsConfig.TRANSACTION_NO);
	protected Phrase recipientCompleteNotification = Phrase.en("You have Bought " + PRICE + " " + BUNDLE_NAME + " Bundle from " + SENDER_MSISDN + //
			". Ref " + TransactionsConfig.TRANSACTION_NO);

	protected Phrase senderUnknownNotification = Phrase.en("Technical Error. Sale of " + BUNDLE_NAME + " to " + RECIPIENT_MSISDN + " may have failed. " + //
			"Your new Balance is " + SENDER_NEW_BALANCE + ". Please contact Customer Care to query this transaction " + //
			"if the customer did not receive the bundle. Ref " + TransactionsConfig.TRANSACTION_NO);
	protected Phrase recipientUnknownNotification = Phrase.en("Technical Error. Purchase of " + BUNDLE_NAME + " Bundle from " + SENDER_MSISDN + " may have failed. " + //
			"Please contact Customer Care to query this transaction if you did not receive the bundle. " + //
			"Ref" + TransactionsConfig.TRANSACTION_NO);

	protected Phrase senderFailedNotification = Phrase.en("Failure. Sale of " + BUNDLE_NAME + " Bundle to " + RECIPIENT_MSISDN + " Failed. " + //
			"Your Balance remains " + SENDER_NEW_BALANCE + ". Ref " + TransactionsConfig.TRANSACTION_NO);
	protected Phrase recipientFailedNotification = Phrase.en("Failure. Purchase of " + BUNDLE_NAME + " Bundle from " + SENDER_MSISDN + " " + //
			"Failed. Ref" + TransactionsConfig.TRANSACTION_NO);

	protected Phrase ussdCommand = Phrase.en("*969*" + USSD_CODE + "*" + RECIPIENT_MSISDN + "*" + PIN + "#");
	protected Phrase smsCommand = Phrase.en("BUNDLE " + SMS_KEYWORD + " " + RECIPIENT_MSISDN + " " + PIN + "=>969");
	protected List<UssdMenu> confirmationMenus = null;
	protected List<UssdMenu> deDuplicationMenus = null;
	protected boolean enableDeDuplication = false;
	protected int maxDuplicateCheckMinutes = 30;
	protected boolean forceLocationOfAgent = false;
	protected boolean forceLocationOfSubscriber = false;
	protected boolean enableSubscriberLocationCaching = false;
	protected int locationCachingExpiryMinutes = 10;

	private Boolean enableBNumberConfirmation = false;
	protected Phrase numberConfirmMessage = Phrase.en(THE_RECIPIENT_NUMBER_AGAIN).fre("FRENCH bundle again");
	protected Phrase numberErrorMessage = Phrase.en(WRONG_B_NUMBER_CONFIRMATION).fre("FRENCH wrong number");

    private Boolean enableDebitNotification = true;
    private Boolean enableDebitBalanceNotification = true;
    private Boolean enableRefundNotification = true;
    private Boolean enableRefundBalanceNotification = true;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public Phrase[] listUssdCommandFields()
	{
		return ussdCommandFields;
	}

	public Phrase[] listSmsCommandFields()
	{
		return smsCommandFields;
	}

	public Phrase[] listNotificationFields()
	{
		return notificationFields;
	}

	public Phrase getSenderCompleteNotification()
	{
		return senderCompleteNotification;
	}

	public BundleSalesConfig setSenderCompleteNotification(Phrase senderCompleteNotification)
	{
		this.senderCompleteNotification = senderCompleteNotification;
		return this;
	}

	public Phrase getRecipientCompleteNotification()
	{
		return recipientCompleteNotification;
	}

	public BundleSalesConfig setRecipientCompleteNotification(Phrase recipientCompleteNotification)
	{
		this.recipientCompleteNotification = recipientCompleteNotification;
		return this;
	}

	public Phrase getSenderUnknownNotification()
	{
		return senderUnknownNotification;
	}

	public BundleSalesConfig setSenderUnknownNotification(Phrase senderUnknownNotification)
	{
		this.senderUnknownNotification = senderUnknownNotification;
		return this;
	}

	public Phrase getRecipientUnknownNotification()
	{
		return recipientUnknownNotification;
	}

	public BundleSalesConfig setRecipientUnknownNotification(Phrase recipientUnknownNotification)
	{
		this.recipientUnknownNotification = recipientUnknownNotification;
		return this;
	}

	public Phrase getSenderFailedNotification()
	{
		return senderFailedNotification;
	}

	public BundleSalesConfig setSenderFailedNotification(Phrase senderFailedNotification)
	{
		this.senderFailedNotification = senderFailedNotification;
		return this;
	}

	public Phrase getRecipientFailedNotification()
	{
		return recipientFailedNotification;
	}

	public BundleSalesConfig setRecipientFailedNotification(Phrase recipientFailedNotification)
	{
		this.recipientFailedNotification = recipientFailedNotification;
		return this;
	}

	public Phrase getUssdCommand()
	{
		return ussdCommand;
	}

	public BundleSalesConfig setUssdCommand(Phrase ussdCommand)
	{
		this.ussdCommand = ussdCommand;
		return this;
	}

	public Phrase getSmsCommand()
	{
		return smsCommand;
	}

	public BundleSalesConfig setSmsCommand(Phrase smsCommand)
	{
		this.smsCommand = smsCommand;
		return this;
	}

	public Phrase getSenderDebitNotification() {
		return senderDebitNotification;
	}

	public void setSenderDebitNotification(Phrase senderDebitNotification) {
		this.senderDebitNotification = senderDebitNotification;
	}

	public Phrase getSenderDebitBalanceNotification() {
		return senderDebitBalanceNotification;
	}

	public void setSenderDebitBalanceNotification(Phrase senderDebitBalanceNotification) {
		this.senderDebitBalanceNotification = senderDebitBalanceNotification;
	}

	public Phrase getSenderRefundNotification() {
		return senderRefundNotification;
	}

	public void setSenderRefundNotification(Phrase senderRefundNotification) {
		this.senderRefundNotification = senderRefundNotification;
	}

	public Phrase getSenderRefundBalanceNotification() {
		return senderRefundBalanceNotification;
	}

	public void setSenderRefundBalanceNotification(Phrase senderRefundBalanceNotification) {
		this.senderRefundBalanceNotification = senderRefundBalanceNotification;
	}

	@Override
	public List<UssdMenu> getConfirmationMenus()
	{
		return confirmationMenus;
	}

	@Override
	public BundleSalesConfig setConfirmationMenus(List<UssdMenu> confirmationMenus)
	{
		this.confirmationMenus = confirmationMenus;
		return this;
	}

	@Override
	public List<UssdMenu> getDeDuplicationMenus()
	{
		return deDuplicationMenus;
	}

	@Override
	public BundleSalesConfig setDeDuplicationMenus(List<UssdMenu> deDuplicationMenus)
	{
		this.deDuplicationMenus = deDuplicationMenus;
		return this;
	}
	
	@Override
	public boolean isEnableDeDuplication()
	{
		return enableDeDuplication;
	}

	@Override
	public BundleSalesConfig setEnableDeDuplication(boolean enableDeDuplication)
	{
		this.enableDeDuplication = enableDeDuplication;
		return this;
	}
	
	@Override
	public int getMaxDuplicateCheckMinutes()
	{
		return maxDuplicateCheckMinutes;
	}

	@Override
	public BundleSalesConfig setMaxDuplicateCheckMinutes(int maxDuplicateCheckMinutes)
	{
		this.maxDuplicateCheckMinutes = maxDuplicateCheckMinutes;
		return this;
	}

	public boolean isForceLocationOfAgent()
	{
		return forceLocationOfAgent;
	}

	public BundleSalesConfig setForceLocationOfAgent(boolean forceLocationOfAgent)
	{
		this.forceLocationOfAgent = forceLocationOfAgent;
		return this;
	}

	public boolean isForceLocationOfSubscriber()
	{
		return forceLocationOfSubscriber;
	}

	public BundleSalesConfig setForceLocationOfSubscriber(boolean forceLocationOfSubscriber)
	{
		this.forceLocationOfSubscriber = forceLocationOfSubscriber;
		return this;
	}
	
	public boolean isEnableSubscriberLocationCaching()
	{
		return enableSubscriberLocationCaching;
	}

	public BundleSalesConfig setEnableSubscriberLocationCaching(boolean enableSubscriberLocationCaching)
	{
		this.enableSubscriberLocationCaching = enableSubscriberLocationCaching;
		return this;
	}
	
	public int getLocationCachingExpiryMinutes()
	{
		return locationCachingExpiryMinutes;
	}

	public BundleSalesConfig setLocationCachingExpiryMinutes(int locationCachingExpiryMinutes)
	{
		this.locationCachingExpiryMinutes = locationCachingExpiryMinutes;
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

	public BundleSalesConfig setVersion(int version)
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
		BundleSalesConfig template = new BundleSalesConfig();

		if (senderCompleteNotification == null)
			senderCompleteNotification = template.senderCompleteNotification;

		if (recipientCompleteNotification == null)
			recipientCompleteNotification = template.recipientCompleteNotification;

		if (senderUnknownNotification == null)
			senderUnknownNotification = template.senderUnknownNotification;

		if (recipientUnknownNotification == null)
			recipientUnknownNotification = template.recipientUnknownNotification;

		if (senderFailedNotification == null)
			senderFailedNotification = template.senderFailedNotification;

		if (recipientFailedNotification == null)
			recipientFailedNotification = template.recipientFailedNotification;

		if (ussdCommand == null)
			ussdCommand = template.ussdCommand;

		if (smsCommand == null)
			smsCommand = template.smsCommand;

		if (enableBNumberConfirmation == null) {
			enableBNumberConfirmation = template.enableBNumberConfirmation;
		}

		if (numberConfirmMessage == null) {
			numberConfirmMessage = template.numberConfirmMessage;
		}

		if (numberErrorMessage == null) {
			numberErrorMessage = template.numberErrorMessage;
		}

		if (senderDebitNotification == null) {
			senderDebitNotification = template.senderDebitNotification;
		}

		if (senderDebitBalanceNotification == null) {
			senderDebitBalanceNotification = template.senderDebitBalanceNotification;
		}

		if (senderRefundNotification == null) {
			senderRefundNotification = template.senderRefundNotification;
		}

		if (senderRefundBalanceNotification == null) {
			senderRefundBalanceNotification = template.senderRefundBalanceNotification;
		}

        if (enableDebitNotification == null) {
            enableDebitNotification = template.enableDebitNotification;
        }

        if (enableDebitBalanceNotification == null) {
            enableDebitBalanceNotification = template.enableDebitBalanceNotification;
        }

        if (enableRefundNotification == null) {
            enableRefundNotification = template.enableRefundNotification;
        }

        if (enableRefundBalanceNotification == null) {
            enableRefundBalanceNotification = template.enableRefundBalanceNotification;
        }
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
				.validExpandableText("senderCompleteNotification", senderCompleteNotification, notificationFields) //
				.validExpandableText("recipientCompleteNotification", recipientCompleteNotification, notificationFields) //
				.validExpandableText("senderUnknownNotification", senderUnknownNotification, notificationFields) //
				.validExpandableText("recipientUnknownNotification", recipientUnknownNotification, notificationFields) //
				.validExpandableText("senderFailedNotification", senderFailedNotification, notificationFields) //
				.validExpandableText("recipientFailedNotification", recipientFailedNotification, notificationFields) //
				.validExpandableText("senderDebitNotification", senderDebitNotification, notificationFields)
				.validExpandableText("senderDebitBalanceNotification", senderDebitBalanceNotification, notificationFields)
				.validExpandableText("senderRefundNotification", senderRefundNotification, notificationFields)
				.validExpandableText("senderRefundBalanceNotification", senderRefundBalanceNotification, notificationFields)
				.validUssdCommand("ussdCommand", ussdCommand, ussdCommandFields) //
				.validSmsCommand("smsCommands", smsCommand, smsCommandFields) //
		;

		if (enableSubscriberLocationCaching)
		{
			validator.notLess("locationCachingExpiryMinutes", locationCachingExpiryMinutes, 1) //
			;
		}	
		
		if (enableDeDuplication)
		{
			validator.notLess("maxDuplicateCheckMinutes ", maxDuplicateCheckMinutes, 1) //
			.notMore("maxDuplicateCheckMinutes ", maxDuplicateCheckMinutes, 60) //
			;
		}

		UssdMenu.validate(validator, confirmationMenus);
		UssdMenu.validate(validator, deDuplicationMenus);

		return validator.toList();
	}

	public Boolean getEnableBNumberConfirmation() {
		return enableBNumberConfirmation;
	}

	public void setEnableBNumberConfirmation(Boolean enableBNumberConfirmation) {
		this.enableBNumberConfirmation = enableBNumberConfirmation;
	}

	public Phrase getNumberConfirmMessage() {
		return numberConfirmMessage;
	}

	public BundleSalesConfig setNumberConfirmMessage(Phrase numberConfirmMessage) {
		this.numberConfirmMessage = numberConfirmMessage;
		return this;
	}

	public Phrase getNumberErrorMessage() {
		return numberErrorMessage;
	}

	public BundleSalesConfig setNumberErrorMessage(Phrase numberErrorMessage) {
		this.numberErrorMessage = numberErrorMessage;
		return this;
	}

    public Boolean getEnableDebitNotification() {
        return enableDebitNotification;
    }

    public void setEnableDebitNotification(Boolean enableDebitNotification) {
        this.enableDebitNotification = enableDebitNotification;
    }

    public Boolean getEnableDebitBalanceNotification() {
        return enableDebitBalanceNotification;
    }

    public void setEnableDebitBalanceNotification(Boolean enableDebitBalanceNotification) {
        this.enableDebitBalanceNotification = enableDebitBalanceNotification;
    }

    public Boolean getEnableRefundNotification() {
        return enableRefundNotification;
    }

    public void setEnableRefundNotification(Boolean enableRefundNotification) {
        this.enableRefundNotification = enableRefundNotification;
    }

    public Boolean getEnableRefundBalanceNotification() {
        return enableRefundBalanceNotification;
    }

    public void setEnableRefundBalanceNotification(Boolean enableRefundBalanceNotification) {
        this.enableRefundBalanceNotification = enableRefundBalanceNotification;
    }
}
