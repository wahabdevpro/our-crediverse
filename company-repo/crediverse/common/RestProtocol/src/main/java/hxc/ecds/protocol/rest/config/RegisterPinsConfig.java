package hxc.ecds.protocol.rest.config;

import java.math.BigDecimal;
import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class RegisterPinsConfig implements IConfiguration
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String TEMPORARY_PIN = "{TemporaryPIN}";
	public static final String NEW_PIN = "{NewPIN}";

	private static Phrase[] commandFields = new Phrase[] { //
			Phrase.en(TEMPORARY_PIN), Phrase.en(NEW_PIN), };

	private static Phrase[] notificationFields = new Phrase[] { //
			TransactionsConfig.PHRASE_TRANSACTION_NO };

	private static final long serialVersionUID = 150185445200815915L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	protected int version;

	protected Phrase ussdCommand = Phrase.en("*910*" + TEMPORARY_PIN + "*" + NEW_PIN + "#");
	protected Phrase smsCommand = Phrase.en("REG " + TEMPORARY_PIN + " " + NEW_PIN + "=>910");
	protected Phrase notification = Phrase.en("You have successfully registered your new pin. Ref " + TransactionsConfig.TRANSACTION_NO);
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

	public RegisterPinsConfig setUssdCommand(Phrase ussdCommand)
	{
		this.ussdCommand = ussdCommand;
		return this;
	}

	public Phrase getSmsCommand()
	{
		return smsCommand;
	}

	public RegisterPinsConfig setSmsCommand(Phrase smsCommand)
	{
		this.smsCommand = smsCommand;
		return this;
	}

	public Phrase getNotification()
	{
		return notification;
	}

	public RegisterPinsConfig setNotification(Phrase notification)
	{
		this.notification = notification;
		return this;
	}

	public BigDecimal getCharge()
	{
		return charge;
	}

	public void setCharge(BigDecimal charge)
	{
		this.charge = charge;
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

	public RegisterPinsConfig setVersion(int version)
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
				.validUssdCommand("ussdCommand", ussdCommand, commandFields) //
				.validSmsCommand("smsCommand", smsCommand, commandFields) //
				.notLess("charge", charge, BigDecimal.ZERO) //
				.isMoney("charge", charge);

		return validator.toList();
	}

}
