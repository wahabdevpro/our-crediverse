package hxc.ecds.protocol.rest.config;

import java.math.BigDecimal;
import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class ChangePinsConfig implements IConfiguration
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String OLD_PIN = "{OldPIN}";
	public static final String NEW_PIN = "{NewPIN}";
	public static final String CONFIRM_PIN = "{ConfirmPIN}";

	private static Phrase[] commandFields = new Phrase[] { //
			Phrase.en(OLD_PIN), Phrase.en(NEW_PIN), Phrase.en(CONFIRM_PIN) };

	private static Phrase[] notificationFields = new Phrase[] { //
			TransactionsConfig.PHRASE_TRANSACTION_NO };

	private static final long serialVersionUID = 6583818196104352297L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields successfully
	//
	// /////////////////////////////////

	protected int version;

	protected Phrase ussdCommand = Phrase.en("*910*8*" + OLD_PIN + "*" + NEW_PIN + "*" + CONFIRM_PIN + "#");
	protected Phrase smsCommand = Phrase.en("CHG_PIN " + OLD_PIN + " " + NEW_PIN + "=>910");
	protected Phrase notification = Phrase.en("You have successfully changed your PIN. Ref " + TransactionsConfig.TRANSACTION_NO);
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

	public ChangePinsConfig setUssdCommand(Phrase ussdCommand)
	{
		this.ussdCommand = ussdCommand;
		return this;
	}

	public Phrase getSmsCommand()
	{
		return smsCommand;
	}

	public ChangePinsConfig setSmsCommand(Phrase smsCommand)
	{
		this.smsCommand = smsCommand;
		return this;
	}

	public Phrase getNotification()
	{
		return notification;
	}

	public ChangePinsConfig setNotification(Phrase notification)
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

	public ChangePinsConfig setVersion(int version)
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
		ChangePinsConfig template = new ChangePinsConfig();

		if(notification == null)
			this.notification = template.notification;

		if(ussdCommand == null)
			this.ussdCommand = template.ussdCommand;

		if(smsCommand == null)
			this.smsCommand = template.smsCommand;
		
		if(charge == null)
			this.charge = template.charge;
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
