package hxc.ecds.protocol.rest.config;

import java.math.BigDecimal;
import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class SelfTopUpsConfig implements IConfirmationMenuConfig
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String MSISDN = "{MSISDN}";
	public static final String PIN = "{PIN}";
	public static final String AMOUNT = "{Amount}";
	public static final String ECDS_BALANCE = "{NewEcdsBalance}";
	public static final String AIRIME_BALANCE = "{NewAirtimeBalance}";

	private static Phrase[] commandFields = new Phrase[] { //
			Phrase.en(AMOUNT), Phrase.en(PIN) };

	private static Phrase[] notificationFields = new Phrase[] { //
			Phrase.en(ECDS_BALANCE), Phrase.en(AIRIME_BALANCE), Phrase.en(AMOUNT), Phrase.en(MINS_SINCE_LAST), //
			TransactionsConfig.PHRASE_TRANSACTION_NO };

	private static Phrase[] externalDataFields = new Phrase[] {
			Phrase.en(MSISDN), Phrase.en(ECDS_BALANCE),
			Phrase.en(AMOUNT),
			TransactionsConfig.PHRASE_TRANSACTION_NO
	};

	private static final long serialVersionUID = -3324652307456342758L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	protected int version;
	protected Phrase ussdCommand = Phrase.en("*910*9*" + AMOUNT + "*" + PIN + "#");
	protected Phrase smsCommand = Phrase.en("TOP " + AMOUNT + " " + PIN + "=>910");
	protected Phrase senderNotification = Phrase.en("You have Topped-Up " + AMOUNT + //
			". ECDS Bal " + ECDS_BALANCE + ", Airtime Bal " + AIRIME_BALANCE + ". Ref " + TransactionsConfig.TRANSACTION_NO);
	protected Phrase senderUnknownNotification = Phrase.en("Technical Error. Your Self Top-up may have failed. " + //
			"Your new Balance is " + ECDS_BALANCE + ". Please contact Customer Care to query this transaction " + //
			"if you did not receive it. Ref " + TransactionsConfig.TRANSACTION_NO);
	protected String refillProfileID = "INCM";
	protected BigDecimal charge = BigDecimal.ZERO;
	protected List<UssdMenu> confirmationMenus = null;
	protected List<UssdMenu> deDuplicationMenus = null;
	protected boolean enableDeDuplication = false;
	protected int maxDuplicateCheckMinutes = 30;
	protected boolean forceLocationOfAgent = false;
	protected boolean autoActivateAccounts = false;
	protected int[] nonDeterministicErrorCodes = new int[] { 100, 999 };

	protected Phrase refillExternalData1 = Phrase.en(MSISDN);
	protected Phrase refillExternalData2 = Phrase.en(MSISDN);
	protected Phrase refillExternalData3 = Phrase.en(MSISDN);
	protected Phrase refillExternalData4 = Phrase.en(MSISDN);

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

	public SelfTopUpsConfig setUssdCommand(Phrase ussdCommand)
	{
		this.ussdCommand = ussdCommand;
		return this;
	}

	public Phrase getSmsCommand()
	{
		return smsCommand;
	}

	public SelfTopUpsConfig setSmsCommand(Phrase smsCommand)
	{
		this.smsCommand = smsCommand;
		return this;
	}

	public Phrase getSenderNotification()
	{
		return senderNotification;
	}

	public SelfTopUpsConfig setSenderNotification(Phrase senderNotification)
	{
		this.senderNotification = senderNotification;
		return this;
	}

	public Phrase getSenderUnknownNotification()
	{
		return senderUnknownNotification;
	}

	public SelfTopUpsConfig setSenderUnknownNotification(Phrase senderUnknownNotification)
	{
		this.senderUnknownNotification = senderUnknownNotification;
		return this;
	}

	public String getRefillProfileID()
	{
		return refillProfileID;
	}

	public SelfTopUpsConfig setRefillProfileID(String refillProfileID)
	{
		this.refillProfileID = refillProfileID;
		return this;
	}

	public BigDecimal getCharge()
	{
		return charge;
	}

	public SelfTopUpsConfig setCharge(BigDecimal charge)
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
	public SelfTopUpsConfig setConfirmationMenus(List<UssdMenu> confirmationMenus)
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
	public SelfTopUpsConfig setDeDuplicationMenus(List<UssdMenu> deDuplicationMenus)
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
	public SelfTopUpsConfig setEnableDeDuplication(boolean enableDeDuplication)
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
	public SelfTopUpsConfig setMaxDuplicateCheckMinutes(int maxDuplicateCheckMinutes)
	{
		this.maxDuplicateCheckMinutes = maxDuplicateCheckMinutes;
		return this;
	}

	public boolean isForceLocationOfAgent()
	{
		return forceLocationOfAgent;
	}

	public SelfTopUpsConfig setForceLocationOfAgent(boolean forceLocationOfAgent)
	{
		this.forceLocationOfAgent = forceLocationOfAgent;
		return this;
	}

	public SelfTopUpsConfig setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public boolean isAutoActivateAccounts()
	{
		return autoActivateAccounts;
	}

	public SelfTopUpsConfig setAutoActivateAccounts(boolean autoActivateAccounts)
	{
		this.autoActivateAccounts = autoActivateAccounts;
		return this;
	}

	public int[] getNonDeterministicErrorCodes()
	{
		return nonDeterministicErrorCodes;
	}

	public SelfTopUpsConfig setNonDeterministicErrorCodes(int[] nonDeterministicErrorCodes)
	{
		this.nonDeterministicErrorCodes = nonDeterministicErrorCodes;
		return this;
	}

	public Phrase getRefillExternalData1()
	{
		return this.refillExternalData1;
	}
	public SelfTopUpsConfig setRefillExternalData1( Phrase refillExternalData1 )
	{
		this.refillExternalData1 = refillExternalData1;
		return this;
	}

	public Phrase getRefillExternalData2()
	{
		return this.refillExternalData2;
	}
	public SelfTopUpsConfig setRefillExternalData2( Phrase refillExternalData2 )
	{
		this.refillExternalData2 = refillExternalData2;
		return this;
	}

	public Phrase getRefillExternalData3()
	{
		return this.refillExternalData3;
	}
	public SelfTopUpsConfig setRefillExternalData3( Phrase refillExternalData3 )
	{
		this.refillExternalData3 = refillExternalData3;
		return this;
	}

	public Phrase getRefillExternalData4()
	{
		return this.refillExternalData4;
	}
	public SelfTopUpsConfig setRefillExternalData4( Phrase refillExternalData4 )
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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Post-Load fix up
	//
	// /////////////////////////////////
	@Override
	public void onPostLoad()
	{
		SelfTopUpsConfig template = new SelfTopUpsConfig();

		if (senderUnknownNotification == null)
			senderUnknownNotification = template.senderUnknownNotification;

		if (nonDeterministicErrorCodes == null)
			nonDeterministicErrorCodes = template.nonDeterministicErrorCodes;

		if (refillExternalData1 == null) refillExternalData1 = template.refillExternalData1;
		if (refillExternalData2 == null) refillExternalData2 = template.refillExternalData2;
		if (refillExternalData3 == null) refillExternalData3 = template.refillExternalData3;
		if (refillExternalData4 == null) refillExternalData4 = template.refillExternalData4;
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
				.validExpandableText("senderUnknownNotification", senderUnknownNotification, notificationFields) //
				.validUssdCommand("ussdCommand", ussdCommand, commandFields) //
				.validSmsCommand("smsCommand", smsCommand, commandFields) //
				.validExpandableText("refillExternalData1", refillExternalData1, externalDataFields) //
				.validExpandableText("refillExternalData2", refillExternalData2, externalDataFields) //
				.validExpandableText("refillExternalData3", refillExternalData3, externalDataFields) //
				.validExpandableText("refillExternalData4", refillExternalData4, externalDataFields) //
				.notEmpty("refillProfileID", refillProfileID, 1, 4) //
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

}
