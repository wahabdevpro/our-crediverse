package hxc.ecds.protocol.rest.config;

import java.math.BigDecimal;
import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class TransfersConfig implements IConfirmationMenuConfig
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
	public static final String BONUS_PROVISION_AMOUNT = "{BonusProvisionAmount}";
	public static final String TOTAL_AMOUNT = "{TotalAmount}";
	public static final String TRADE_BONUS = "{TradeBonus}";

	public static final String SENDER_NEW_BALANCE = "{SenderNewBalance}";
	public static final String SENDER_NEW_BONUS_BALANCE = "{SenderNewBonusBalance}";
	public static final String SENDER_NEW_TOTAL_BALANCE = "{SenderNewTotalBalance}";

	public static final String RECIPIENT_NEW_BALANCE = "{RecipientNewBalance}";
	public static final String RECIPIENT_NEW_BONUS_BALANCE = "{RecipientNewBonusBalance}";
	public static final String RECIPIENT_NEW_TOTAL_BALANCE = "{RecipientNewTotalBalance}";

	private static Phrase[] commandFields = new Phrase[] { //
			Phrase.en(RECIPIENT_MSISDN), Phrase.en(AMOUNT), Phrase.en(PIN) };

	private static Phrase[] notificationFields = new Phrase[] { //
			Phrase.en(SENDER_MSISDN), Phrase.en(RECIPIENT_MSISDN), Phrase.en(AMOUNT), Phrase.en(MINS_SINCE_LAST), //
			Phrase.en(BONUS_PROVISION_AMOUNT), Phrase.en(TOTAL_AMOUNT), Phrase.en(TRADE_BONUS), //
			Phrase.en(SENDER_NEW_BONUS_BALANCE), Phrase.en(SENDER_NEW_TOTAL_BALANCE), //
			Phrase.en(RECIPIENT_NEW_BONUS_BALANCE), Phrase.en(RECIPIENT_NEW_TOTAL_BALANCE), //
			Phrase.en(SENDER_NEW_BALANCE), Phrase.en(RECIPIENT_NEW_BALANCE), TransactionsConfig.PHRASE_TRANSACTION_NO, };
	
	private static Phrase[] externalDataFields = new Phrase[] {
			Phrase.en(SENDER_MSISDN),
			Phrase.en(RECIPIENT_MSISDN),
			Phrase.en(AMOUNT),
			TransactionsConfig.PHRASE_TRANSACTION_NO
	};

	public static final long serialVersionUID = 8526838709315647311L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	protected int version;

	protected Phrase ussdCommand = Phrase.en("*333*" + RECIPIENT_MSISDN + "*" + AMOUNT + "*" + PIN + "#");
	protected Phrase smsCommand = Phrase.en("STOCK " + RECIPIENT_MSISDN + " " + AMOUNT + " " + PIN + "=>910");
	protected Phrase requesterNotification = Phrase.en("You have Transferred " + AMOUNT + " Airtime from " + SENDER_MSISDN + " to " + RECIPIENT_MSISDN //
			+ ". Ref " + TransactionsConfig.TRANSACTION_NO);
	protected Phrase senderNotification = Phrase.en(AMOUNT + " Airtime has been transferred from your account to " + RECIPIENT_MSISDN //
			+ ". Your new Balance is " + SENDER_NEW_BALANCE + ". Ref " + TransactionsConfig.TRANSACTION_NO);
	protected Phrase recipientNotification = Phrase.en("You have Received " + AMOUNT + " Airtime from " + SENDER_MSISDN + //
			". Your new Balance is " + RECIPIENT_NEW_BALANCE + ". Ref " + TransactionsConfig.TRANSACTION_NO);

	protected BigDecimal charge = BigDecimal.ZERO;

	protected List<UssdMenu> confirmationMenus = null;
	protected List<UssdMenu> deDuplicationMenus = null;
	protected boolean enableDeDuplication = false;
	protected int maxDuplicateCheckMinutes = 30;

	protected boolean forceLocationOfAgent = false;
	
	protected int[] nonDeterministicErrorCodes = new int[] { 100, 999 };
	
	protected Phrase refillExternalData1 = Phrase.en(SENDER_MSISDN);
	protected Phrase refillExternalData2 = Phrase.en(SENDER_MSISDN);
	protected Phrase refillExternalData3 = Phrase.en(SENDER_MSISDN);
	protected Phrase refillExternalData4 = Phrase.en(SENDER_MSISDN);

	private Boolean enableBNumberConfirmation = false;
	protected Phrase numberConfirmMessage = Phrase.en(THE_RECIPIENT_NUMBER_AGAIN).fre("FRENCH transfer again");
	protected Phrase numberErrorMessage = Phrase.en(WRONG_B_NUMBER_CONFIRMATION).fre("FRENCH wrong number");
	private Boolean disregardTradeBonusCalculation = Boolean.FALSE;

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

	public TransfersConfig setUssdCommand(Phrase ussdCommand)
	{
		this.ussdCommand = ussdCommand;
		return this;
	}

	public Phrase getSmsCommand()
	{
		return smsCommand;
	}

	public TransfersConfig setSmsCommand(Phrase smsCommand)
	{
		this.smsCommand = smsCommand;
		return this;
	}

	public Phrase getRequesterNotification()
	{
		return requesterNotification;
	}

	public TransfersConfig setRequesterNotification(Phrase requesterNotification)
	{
		this.requesterNotification = requesterNotification;
		return this;
	}

	public Phrase getSenderNotification()
	{
		return senderNotification;
	}

	public TransfersConfig setSenderNotification(Phrase senderNotification)
	{
		this.senderNotification = senderNotification;
		return this;
	}

	public Phrase getRecipientNotification()
	{
		return recipientNotification;
	}

	public TransfersConfig setRecipientNotification(Phrase recipientNotification)
	{
		this.recipientNotification = recipientNotification;
		return this;
	}

	public BigDecimal getCharge()
	{
		return charge;
	}

	public TransfersConfig setCharge(BigDecimal charge)
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
	public TransfersConfig setConfirmationMenus(List<UssdMenu> confirmationMenus)
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
	public TransfersConfig setDeDuplicationMenus(List<UssdMenu> deDuplicationMenus)
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
	public TransfersConfig setEnableDeDuplication(boolean enableDeDuplication)
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
	public TransfersConfig setMaxDuplicateCheckMinutes(int maxDuplicateCheckMinutes)
	{
		this.maxDuplicateCheckMinutes = maxDuplicateCheckMinutes;
		return this;
	}

	public boolean isForceLocationOfAgent()
	{
		return forceLocationOfAgent;
	}

	public TransfersConfig setForceLocationOfAgent(boolean forceLocationOfAgent)
	{
		this.forceLocationOfAgent = forceLocationOfAgent;
		return this;
	}
	
	public int[] getNonDeterministicErrorCodes()
	{
		return nonDeterministicErrorCodes;
	}

	public TransfersConfig setNonDeterministicErrorCodes(int[] nonDeterministicErrorCodes)
	{
		this.nonDeterministicErrorCodes = nonDeterministicErrorCodes;
		return this;
	}

	public Phrase getRefillExternalData1()
	{
		return this.refillExternalData1;
	}
	public TransfersConfig setRefillExternalData1( Phrase refillExternalData1 )
	{
		this.refillExternalData1 = refillExternalData1;
		return this;
	}

	public Phrase getRefillExternalData2()
	{
		return this.refillExternalData2;
	}
	public TransfersConfig setRefillExternalData2( Phrase refillExternalData2 )
	{
		this.refillExternalData2 = refillExternalData2;
		return this;
	}

	public Phrase getRefillExternalData3()
	{
		return this.refillExternalData3;
	}
	public TransfersConfig setRefillExternalData3( Phrase refillExternalData3 )
	{
		this.refillExternalData3 = refillExternalData3;
		return this;
	}

	public Phrase getRefillExternalData4()
	{
		return this.refillExternalData4;
	}
	public TransfersConfig setRefillExternalData4( Phrase refillExternalData4 )
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

	public TransfersConfig setVersion(int version)
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
		TransfersConfig template = new TransfersConfig();

		if (Phrase.nullOrEmpty(requesterNotification))
			requesterNotification = template.requesterNotification;

		if (Phrase.nullOrEmpty(senderNotification))
			senderNotification = template.senderNotification;

		if (Phrase.nullOrEmpty(recipientNotification))
			recipientNotification = template.recipientNotification;
		
		if (refillExternalData1 == null) refillExternalData1 = template.refillExternalData1;
		if (refillExternalData2 == null) refillExternalData2 = template.refillExternalData2;
		if (refillExternalData3 == null) refillExternalData3 = template.refillExternalData3;
		if (refillExternalData4 == null) refillExternalData4 = template.refillExternalData4;
		if (disregardTradeBonusCalculation == null) disregardTradeBonusCalculation = template.disregardTradeBonusCalculation;

		if (enableBNumberConfirmation == null) {
			enableBNumberConfirmation = template.enableBNumberConfirmation;
		}

		if (numberConfirmMessage == null) {
			numberConfirmMessage = template.numberConfirmMessage;
		}
		if (numberErrorMessage == null) {
			numberErrorMessage = template.numberErrorMessage;
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
				.validExpandableText("requesterNotification", requesterNotification, notificationFields) //
				.validExpandableText("senderNotification", senderNotification, notificationFields) //
				.validExpandableText("recipientNotification", recipientNotification, notificationFields) //
				.validUssdCommand("ussdCommand", ussdCommand, commandFields) //
				.validSmsCommand("smsCommand", smsCommand, commandFields) //
				.validExpandableText("refillExternalData1", refillExternalData1, externalDataFields) //
				.validExpandableText("refillExternalData2", refillExternalData2, externalDataFields) //
				.validExpandableText("refillExternalData3", refillExternalData3, externalDataFields) //
				.validExpandableText("refillExternalData4", refillExternalData4, externalDataFields) //
				.notLess("charge", charge, BigDecimal.ZERO) //
				.isMoney("charge", charge);
		
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

	public Boolean getDisregardTradeBonusCalculation() {
		return disregardTradeBonusCalculation;
	}

	public void setDisregardTradeBonusCalculation(Boolean disregardTradeBonusCalculation) {
		this.disregardTradeBonusCalculation = disregardTradeBonusCalculation;
	}

	public Phrase getNumberConfirmMessage() {
		return numberConfirmMessage;
	}

	public TransfersConfig setNumberConfirmMessage(Phrase numberConfirmMessage) {
		this.numberConfirmMessage = numberConfirmMessage;
		return this;
	}

	public Phrase getNumberErrorMessage() {
		return numberErrorMessage;
	}

	public TransfersConfig setNumberErrorMessage(Phrase numberErrorMessage) {
		this.numberErrorMessage = numberErrorMessage;
		return this;
	}
}
