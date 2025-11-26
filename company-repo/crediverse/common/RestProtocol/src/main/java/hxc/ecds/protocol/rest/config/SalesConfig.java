package hxc.ecds.protocol.rest.config;

import java.math.BigDecimal;
import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class SalesConfig implements IConfirmationMenuConfig
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String SENDER_MSISDN = "{SenderMSISDN}";
	public static final String RECIPIENT_MSISDN = "{RecipientMSISDN}";
	public static final String PIN = "{PIN}";
	public static final String AMOUNT = "{Amount}";
	public static final String SENDER_NEW_BALANCE = "{SenderNewBalance}";
	public static final String RECIPIENT_NEW_BALANCE = "{RecipientNewBalance}";
	public static final String SENDER_SELLER_BONUS = "{SenderSellerBonus}";
	public static final String LOAN_AMOUNT = "{LoanAmount}";


	private static Phrase[] commandFields = new Phrase[] { //
			Phrase.en(RECIPIENT_MSISDN), Phrase.en(AMOUNT), Phrase.en(PIN) };

	private static Phrase[] notificationFields = new Phrase[] { //
			Phrase.en(SENDER_MSISDN), Phrase.en(RECIPIENT_MSISDN), Phrase.en(AMOUNT), Phrase.en(MINS_SINCE_LAST), //
			Phrase.en(SENDER_SELLER_BONUS),Phrase.en(SENDER_NEW_BALANCE), Phrase.en(RECIPIENT_NEW_BALANCE), TransactionsConfig.PHRASE_TRANSACTION_NO, Phrase.en(LOAN_AMOUNT) };

	private static Phrase[] externalDataFields = new Phrase[] {
			Phrase.en(SENDER_MSISDN),
			Phrase.en(RECIPIENT_MSISDN),
			Phrase.en(AMOUNT),
			TransactionsConfig.PHRASE_TRANSACTION_NO,
			Phrase.en(LOAN_AMOUNT)

	};

	private static final long serialVersionUID = 643989196905652862L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	protected int version;
	protected Phrase ussdCommand = Phrase.en("*910*2*" + RECIPIENT_MSISDN + "*" + AMOUNT + "*" + PIN + "#");
	protected Phrase smsCommand = Phrase.en("CREDIT " + RECIPIENT_MSISDN + " " + AMOUNT + " " + PIN + "=>910");

	protected Phrase senderNotification = Phrase.en("You have Sold " + AMOUNT + " Airtime to " + RECIPIENT_MSISDN + //
			". Your new Balance is " + SENDER_NEW_BALANCE + " you earned " + SENDER_SELLER_BONUS + " commission. Ref " + TransactionsConfig.TRANSACTION_NO);

	protected Phrase recipientNotification = Phrase.en("You have Bought " + AMOUNT + " Airtime from " + SENDER_MSISDN + //
			". Your new Balance is " + RECIPIENT_NEW_BALANCE + ". Ref " + TransactionsConfig.TRANSACTION_NO);

	protected Phrase senderUnknownNotification = Phrase.en("Technical Error. Sale of Airtime to " + RECIPIENT_MSISDN + " may have failed. " + //
			"Your new Balance is " + SENDER_NEW_BALANCE + ". Please contact Customer Care to query this transaction " + //
			"if the customer did not receive it. Ref " + TransactionsConfig.TRANSACTION_NO);
	protected Phrase recipientUnknownNotification = Phrase.en("Technical Error. Purchase of Airtime from " + SENDER_MSISDN + " may have failed. " + //
			"Please contact Customer Care to query this transaction if you did not receive it. " + //
			"Ref" + TransactionsConfig.TRANSACTION_NO);

	// Agent (Sender) – Partial Recovery
	protected Phrase senderNotificationPartialRecovery = Phrase.en(
			"You have sold successfully " + AMOUNT + " to the Subscriber " + RECIPIENT_MSISDN +
					", whose new balance is now " + RECIPIENT_NEW_BALANCE +
					" after paying a part of his loan of " + LOAN_AMOUNT );

	// Subscriber – Partial Recovery
	protected Phrase recipientNotificationPartialRecovery = Phrase.en(
			"You have recharged successfully " + AMOUNT + " of airtime and your new balance is " + RECIPIENT_NEW_BALANCE +
					" after paying part of your loan of " + LOAN_AMOUNT );
	// Agent (Sender) – Full Recovery
	protected Phrase senderNotificationFullRecovery = Phrase.en(
			"You have sold successfully " + AMOUNT + " to the Subscriber " + RECIPIENT_MSISDN +
					", whose new balance is now " + RECIPIENT_NEW_BALANCE +
					" after paying his loan of " + LOAN_AMOUNT );

	// Subscriber – Full Recovery
	protected Phrase recipientNotificationFullRecovery = Phrase.en(
			"You have recharged successfully " + AMOUNT + " of airtime and your new balance is " + RECIPIENT_NEW_BALANCE +
					"  after paying you loan of amount " + LOAN_AMOUNT );

	protected String defaultSubscriberLanguageID = "en";
	protected String refillProfileID = "INCM";
	protected BigDecimal charge = BigDecimal.ZERO;
	protected List<UssdMenu> confirmationMenus = null;
	protected List<UssdMenu> deDuplicationMenus = null;
	protected boolean enableDeDuplication = false;
	protected int maxDuplicateCheckMinutes = 30;
	protected boolean forceLocationOfAgent = false;
	protected boolean autoActivateAccounts = false;
	protected int[] nonDeterministicErrorCodes = new int[] { 100, 999 };

	protected Phrase refillExternalData1 = Phrase.en(SENDER_MSISDN);
	protected Phrase refillExternalData2 = Phrase.en(SENDER_MSISDN);
	protected Phrase refillExternalData3 = Phrase.en(SENDER_MSISDN);
	protected Phrase refillExternalData4 = Phrase.en(SENDER_MSISDN);

	private Boolean enableBNumberConfirmation = false;
	private Boolean allowSellToSelf = false;
	
	protected Phrase numberConfirmMessage = Phrase.en(THE_RECIPIENT_NUMBER_AGAIN).fre("FRENCH sell again");;
	protected Phrase numberErrorMessage = Phrase.en(WRONG_B_NUMBER_CONFIRMATION).fre("FRENCH wrong number");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public Phrase[] listCommandFields()
	{
		return commandFields;
	}

	public Phrase[] listNotificationFields()
	{
		return notificationFields;
	}

	public Phrase[] listExternalDataFields()
	{
		return externalDataFields;
	}

	public Phrase getUssdCommand()
	{
		return ussdCommand;
	}

	public SalesConfig setUssdCommand(Phrase ussdCommand)
	{
		this.ussdCommand = ussdCommand;
		return this;
	}

	public Phrase getSmsCommand()
	{
		return smsCommand;
	}

	public SalesConfig setSmsCommand(Phrase smsCommand)
	{
		this.smsCommand = smsCommand;
		return this;
	}

	public Phrase getSenderNotification()
	{
		return senderNotification;
	}

	public SalesConfig setSenderNotification(Phrase senderNotification)
	{
		this.senderNotification = senderNotification;
		return this;
	}
	public Phrase getSenderNotificationPartialRecovery()
	{
		return senderNotificationPartialRecovery;
	}

	public SalesConfig setSenderNotificationPartialRecovery(Phrase senderNotificationPartialRecovery)
	{
		this.senderNotificationPartialRecovery = senderNotificationPartialRecovery;
		return this;
	}
	public Phrase getSenderNotificationFullRecovery()
	{
		return senderNotificationFullRecovery;
	}

	public SalesConfig setSenderNotificationFullRecovery(Phrase senderNotificationFullRecovery)
	{
		this.senderNotificationFullRecovery = senderNotificationFullRecovery;
		return this;
	}
	public Phrase getRecipientNotification()
	{
		return recipientNotification;
	}

	public SalesConfig setRecipientNotification(Phrase recipientNotification)
	{
		this.recipientNotification = recipientNotification;
		return this;
	}
	public Phrase getRecipientNotificationPartialRecovery()
	{
		return recipientNotificationPartialRecovery;
	}

	public SalesConfig setRecipientNotificationPartialRecovery(Phrase recipientNotificationPartialRecovery)
	{
		this.recipientNotificationPartialRecovery = recipientNotificationPartialRecovery;
		return this;
	}
	public Phrase getRecipientNotificationFullRecovery()
	{
		return recipientNotificationFullRecovery;
	}

	public SalesConfig setRecipientNotificationFullRecovery(Phrase recipientNotificationFullRecovery)
	{
		this.recipientNotificationFullRecovery = recipientNotificationFullRecovery;
		return this;
	}
	public Phrase getSenderUnknownNotification()
	{
		return senderUnknownNotification;
	}

	public SalesConfig setSenderUnknownNotification(Phrase senderUnknownNotification)
	{
		this.senderUnknownNotification = senderUnknownNotification;
		return this;
	}

	public Phrase getRecipientUnknownNotification()
	{
		return recipientUnknownNotification;
	}

	public SalesConfig setRecipientUnknownNotification(Phrase recipientUnknownNotification)
	{
		this.recipientUnknownNotification = recipientUnknownNotification;
		return this;
	}

	public String getDefaultSubscriberLanguageID()
	{
		return defaultSubscriberLanguageID;
	}

	public SalesConfig setDefaultSubscriberLanguageID(String defaultSubscriberLanguageID)
	{
		this.defaultSubscriberLanguageID = defaultSubscriberLanguageID;
		return this;
	}

	public String getRefillProfileID()
	{
		return refillProfileID;
	}

	public SalesConfig setRefillProfileID(String refillProfileID)
	{
		this.refillProfileID = refillProfileID;
		return this;
	}

	public BigDecimal getCharge()
	{
		return charge;
	}

	public SalesConfig setCharge(BigDecimal charge)
	{
		this.charge = charge;
		return this;
	}

	@Override
	public List<UssdMenu> getConfirmationMenus()
	{
		return confirmationMenus;
	}

	@Override
	public SalesConfig setConfirmationMenus(List<UssdMenu> confirmationMenus)
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
	public SalesConfig setDeDuplicationMenus(List<UssdMenu> deDuplicationMenus)
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
	public SalesConfig setEnableDeDuplication(boolean enableDeDuplication)
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
	public SalesConfig setMaxDuplicateCheckMinutes(int maxDuplicateCheckMinutes)
	{
		this.maxDuplicateCheckMinutes = maxDuplicateCheckMinutes;
		return this;
	}

	public boolean isForceLocationOfAgent()
	{
		return forceLocationOfAgent;
	}

	public SalesConfig setForceLocationOfAgent(boolean forceLocationOfAgent)
	{
		this.forceLocationOfAgent = forceLocationOfAgent;
		return this;
	}

	public boolean isAutoActivateAccounts()
	{
		return autoActivateAccounts;
	}

	public SalesConfig setAutoActivateAccounts(boolean autoActivateAccounts)
	{
		this.autoActivateAccounts = autoActivateAccounts;
		return this;
	}

	public int[] getNonDeterministicErrorCodes()
	{
		return nonDeterministicErrorCodes;
	}

	public SalesConfig setNonDeterministicErrorCodes(int[] nonDeterministicErrorCodes)
	{
		this.nonDeterministicErrorCodes = nonDeterministicErrorCodes;
		return this;
	}

	public Phrase getRefillExternalData1()
	{
		return this.refillExternalData1;
	}
	public SalesConfig setRefillExternalData1( Phrase refillExternalData1 )
	{
		this.refillExternalData1 = refillExternalData1;
		return this;
	}

	public Phrase getRefillExternalData2()
	{
		return this.refillExternalData2;
	}
	public SalesConfig setRefillExternalData2( Phrase refillExternalData2 )
	{
		this.refillExternalData2 = refillExternalData2;
		return this;
	}

	public Phrase getRefillExternalData3()
	{
		return this.refillExternalData3;
	}
	public SalesConfig setRefillExternalData3( Phrase refillExternalData3 )
	{
		this.refillExternalData3 = refillExternalData3;
		return this;
	}

	public Phrase getRefillExternalData4()
	{
		return this.refillExternalData4;
	}
	public SalesConfig setRefillExternalData4( Phrase refillExternalData4 )
	{
		this.refillExternalData4 = refillExternalData4;
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

	public SalesConfig setVersion(int version)
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
		SalesConfig template = new SalesConfig();

		if (senderUnknownNotification == null)
			senderUnknownNotification = template.senderUnknownNotification;

		if (recipientUnknownNotification == null)
			recipientUnknownNotification = template.recipientUnknownNotification;

		if (nonDeterministicErrorCodes == null)
			nonDeterministicErrorCodes = template.nonDeterministicErrorCodes;

		if (refillExternalData1 == null) refillExternalData1 = template.refillExternalData1;
		if (refillExternalData2 == null) refillExternalData2 = template.refillExternalData2;
		if (refillExternalData3 == null) refillExternalData3 = template.refillExternalData3;
		if (refillExternalData4 == null) refillExternalData4 = template.refillExternalData4;

		if (enableBNumberConfirmation == null) {
			enableBNumberConfirmation = template.enableBNumberConfirmation;
		}

		if (numberConfirmMessage == null) {
			numberConfirmMessage = template.numberConfirmMessage;
		}
		if (numberErrorMessage == null) {
			numberErrorMessage = template.numberErrorMessage;
		}

		if (allowSellToSelf == null) {
			allowSellToSelf = template.allowSellToSelf;
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
				.validExpandableText("senderNotification", senderNotification, notificationFields) //
				.validExpandableText("recipientNotification", recipientNotification, notificationFields) //
				.validExpandableText("senderNotificationPartialRecovery", senderNotificationPartialRecovery, notificationFields) //
				.validExpandableText("recipientNotificationPartialRecovery", recipientNotificationPartialRecovery, notificationFields) //
				.validExpandableText("senderNotificationFullRecovery", senderNotificationFullRecovery, notificationFields) //
				.validExpandableText("recipientNotificationFullRecovery", recipientNotificationFullRecovery, notificationFields) //
				.validExpandableText("senderUnknownNotification", senderUnknownNotification, notificationFields) //
				.validExpandableText("recipientUnknownNotification", recipientUnknownNotification, notificationFields) //
				.validUssdCommand("ussdCommand", ussdCommand, listCommandFields()) //
				.validSmsCommand("smsCommand", smsCommand, listCommandFields()) //
				.notLess("charge", charge, BigDecimal.ZERO) //
				.validExpandableText("refillExternalData1", refillExternalData1, externalDataFields) //
				.validExpandableText("refillExternalData2", refillExternalData2, externalDataFields) //
				.validExpandableText("refillExternalData3", refillExternalData3, externalDataFields) //
				.validExpandableText("refillExternalData4", refillExternalData4, externalDataFields) //
				.isMoney("charge", charge) //
				.notEmpty("defaultSubscriberLanguageID", defaultSubscriberLanguageID, 2, 2) //
				.notEmpty("refillProfileID", refillProfileID, 1, 4) //
		;
		
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

	public SalesConfig setNumberConfirmMessage(Phrase numberConfirmMessage) {
		this.numberConfirmMessage = numberConfirmMessage;
		return this;
	}

	public Phrase getNumberErrorMessage() {
		return numberErrorMessage;
	}

	public SalesConfig setNumberErrorMessage(Phrase numberErrorMessage) {
		this.numberErrorMessage = numberErrorMessage;
		return this;
	}

	public Boolean getAllowSellToSelf() {
		return allowSellToSelf;
	}

	public void setAllowSellToSelf(Boolean allowSellToSelf) {
		this.allowSellToSelf = allowSellToSelf;
	}
}
