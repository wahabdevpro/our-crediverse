package hxc.ecds.protocol.rest.config;

import java.math.BigDecimal;
import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class DepositsQueryConfig implements IConfiguration
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String PIN = "{PIN}";
	public static final String DATE = "{Date}";
	public static final String COUNT = "{Count}";
	public static final String AMOUNT = "{Amount}";

	private static Phrase[] commandFields = new Phrase[] { //
			Phrase.en(PIN) };

	private static Phrase[] notificationFields = new Phrase[] { //
			TransactionsConfig.PHRASE_TRANSACTION_NO, Phrase.en(DATE), //
			Phrase.en(COUNT), Phrase.en(AMOUNT), //
	};

	private static final long serialVersionUID = 2455468558785414957L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int version;
	protected Phrase ussdCommand = Phrase.en("*911*7*2#");
	protected Phrase smsCommand = Phrase.en("BUY_RPT " + PIN + "=>910");
	protected Phrase notification = Phrase.en("For " + DATE + ": " + //
			COUNT + " Deposits (" + AMOUNT + "). Ref " + TransactionsConfig.TRANSACTION_NO);
	protected Phrase response = Phrase.en("You will shortly receive an SMS for your Deposits Query");
	protected BigDecimal charge = BigDecimal.ZERO;

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

	public Phrase getUssdCommand()
	{
		return ussdCommand;
	}

	public DepositsQueryConfig setUssdCommand(Phrase ussdCommand)
	{
		this.ussdCommand = ussdCommand;
		return this;
	}

	public Phrase getSmsCommand()
	{
		return smsCommand;
	}

	public DepositsQueryConfig setSmsCommand(Phrase smsCommand)
	{
		this.smsCommand = smsCommand;
		return this;
	}

	public Phrase getNotification()
	{
		return notification;
	}

	public DepositsQueryConfig setNotification(Phrase notification)
	{
		this.notification = notification;
		return this;
	}

	public Phrase getResponse()
	{
		return response;
	}

	public DepositsQueryConfig setResponse(Phrase response)
	{
		this.response = response;
		return this;
	}

	public BigDecimal getCharge()
	{
		return charge;
	}

	public DepositsQueryConfig setCharge(BigDecimal charge)
	{
		this.charge = charge;
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

	public DepositsQueryConfig setVersion(int version)
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
				.validExpandableText("response", response, notificationFields) //
				.validUssdCommand("ussdCommand", ussdCommand, commandFields) //
				.validSmsCommand("smsCommand", smsCommand, commandFields) //
				.notLess("charge", charge, BigDecimal.ZERO) //
				.isMoney("charge", charge);

		return validator.toList();
	}

}
