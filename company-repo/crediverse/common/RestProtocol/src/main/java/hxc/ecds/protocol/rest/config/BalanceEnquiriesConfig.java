package hxc.ecds.protocol.rest.config;

import java.math.BigDecimal;
import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class BalanceEnquiriesConfig implements IConfiguration
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String BALANCE = "{Balance}";
	public static final String BONUS_BALANCE = "{BonusBalance}";
	public static final String ONHOLD_BALANCE = "{OnHoldBalance}";
	public static final String TOTAL_BALANCE = "{TotalBalance}";
	public static final String AGENT_STATE = "{AgentState}";
	public static final String PIN = "{PIN}";
	public static final String MSISDN = "{MSISDN}";

	private static Phrase[] commandFields = new Phrase[] { //
			Phrase.en(PIN) };

	private static Phrase[] forOthersCommandFields = new Phrase[] { //
			Phrase.en(PIN), Phrase.en(MSISDN) };

	private static Phrase[] notificationFields = new Phrase[] { //
			Phrase.en(BALANCE), Phrase.en(BONUS_BALANCE), Phrase.en(ONHOLD_BALANCE), //
			Phrase.en(TOTAL_BALANCE), Phrase.en(MSISDN), Phrase.en(AGENT_STATE) };

	private static final long serialVersionUID = -5649980036230878583L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int version;
	protected Phrase ussdCommand = Phrase.en("*910*4" + "*" + PIN + "#");
	protected Phrase smsCommand = Phrase.en("BAL" + " " + PIN + "=>910");
	protected Phrase ussdForOthersCommand = Phrase.en("*910*4*" + MSISDN + "*" + PIN + "#");
	protected Phrase smsForOthersCommand = Phrase.en("BAL" + " " + MSISDN + " " + PIN + "=>910");
	protected Phrase notification = Phrase.en("Your ECDS Balance is " + BALANCE);
	protected Phrase notificationForOther = Phrase.en(MSISDN + "'s ECDS Balance is " + BALANCE + ", and status is " + AGENT_STATE);
	protected BigDecimal charge = BigDecimal.ZERO;
	protected boolean onlyOwnersMayQueryRetailers = false;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public Phrase[] listCommandFields()
	{
		return commandFields;
	}

	public Phrase[] listForOthersCommandFields()
	{
		return forOthersCommandFields;
	}

	public Phrase[] listNotificationFields()
	{
		return notificationFields;
	}

	public Phrase getUssdCommand()
	{
		return ussdCommand;
	}

	public BalanceEnquiriesConfig setUssdCommand(Phrase ussdCommand)
	{
		this.ussdCommand = ussdCommand;
		return this;
	}

	public Phrase getSmsCommand()
	{
		return smsCommand;
	}

	public BalanceEnquiriesConfig setSmsCommand(Phrase smsCommand)
	{
		this.smsCommand = smsCommand;
		return this;
	}

	public Phrase getUssdForOthersCommand()
	{
		return ussdForOthersCommand;
	}

	public BalanceEnquiriesConfig setUssdForOthersCommand(Phrase ussdForOthersCommand)
	{
		this.ussdForOthersCommand = ussdForOthersCommand;
		return this;
	}

	public Phrase getSmsForOthersCommand()
	{
		return smsForOthersCommand;
	}

	public BalanceEnquiriesConfig setSmsForOthersCommand(Phrase smsForOthersCommand)
	{
		this.smsForOthersCommand = smsForOthersCommand;
		return this;
	}

	public Phrase getNotification()
	{
		return notification;
	}

	public BalanceEnquiriesConfig setNotification(Phrase notification)
	{
		this.notification = notification;
		return this;
	}

	public Phrase getNotificationForOther()
	{
		return notificationForOther;
	}

	public BalanceEnquiriesConfig setNotificationForOther(Phrase notificationForOther)
	{
		this.notificationForOther = notificationForOther;
		return this;
	}

	public BigDecimal getCharge()
	{
		return charge;
	}

	public BalanceEnquiriesConfig setCharge(BigDecimal charge)
	{
		this.charge = charge;
		return this;
	}

	public boolean isOnlyOwnersMayQueryRetailers()
	{
		return onlyOwnersMayQueryRetailers;
	}

	public BalanceEnquiriesConfig setOnlyOwnersMayQueryRetailers(boolean onlyOwnersMayQueryRetailers)
	{
		this.onlyOwnersMayQueryRetailers = onlyOwnersMayQueryRetailers;
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

	public BalanceEnquiriesConfig setVersion(int version)
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
		BalanceEnquiriesConfig template = new BalanceEnquiriesConfig();

		if (Phrase.nullOrEmpty(ussdForOthersCommand))
			ussdForOthersCommand = template.ussdForOthersCommand;

		if (Phrase.nullOrEmpty(smsForOthersCommand))
			smsForOthersCommand = template.smsForOthersCommand;
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
				.validExpandableText("notificationForOther", notificationForOther, notificationFields) //
				.validUssdCommand("ussdCommand", ussdCommand, commandFields) //
				.validSmsCommand("smsCommand", smsCommand, commandFields) //
				.validUssdCommand("ussdForOthersCommand", ussdForOthersCommand, forOthersCommandFields) //
				.validSmsCommand("smsForOthersCommand", smsForOthersCommand, forOthersCommandFields) //
				.notLess("charge", charge, BigDecimal.ZERO) //
				.isMoney("charge", charge);

		return validator.toList();
	}

}
