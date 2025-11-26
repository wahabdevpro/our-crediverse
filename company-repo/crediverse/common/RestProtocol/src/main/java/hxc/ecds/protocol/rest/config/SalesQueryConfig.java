package hxc.ecds.protocol.rest.config;

import java.math.BigDecimal;
import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class SalesQueryConfig implements IConfiguration
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String PIN = "{PIN}";
	public static final String DATE = "{Date}";
	public static final String SALES_COUNT = "{SalesCount}";
	public static final String SALES_AMOUNT = "{SalesAmount}";
	public static final String TRANSFERS_COUNT = "{TransfersCount}";
	public static final String TRANSFERS_AMOUNT = "{TransfersAmount}";
	public static final String TOPUPS_COUNT = "{SelfTopUpsCount}";
	public static final String TOPUPS_AMOUNT = "{SelfTopUpsAmount}";

	private static Phrase[] commandFields = new Phrase[] { //
			Phrase.en(PIN) };

	private static Phrase[] notificationFields = new Phrase[] { //
			TransactionsConfig.PHRASE_TRANSACTION_NO, Phrase.en(DATE), //
			Phrase.en(TRANSFERS_COUNT), Phrase.en(TRANSFERS_AMOUNT), //
			Phrase.en(SALES_COUNT), Phrase.en(SALES_AMOUNT), //
			Phrase.en(TOPUPS_COUNT), Phrase.en(TOPUPS_AMOUNT), //
	};

	private static final long serialVersionUID = -5729302489275904010L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int version;
	protected Phrase ussdCommand = Phrase.en("*911*7*1*" + PIN + "#");
	protected Phrase smsCommand = Phrase.en("SALES_RPT " + PIN + "=>910");
	protected Phrase notification = Phrase.en("For " + DATE + ": " + //
			TRANSFERS_COUNT + " Transfers (" + TRANSFERS_AMOUNT + "), " + //
			SALES_COUNT + " Sales (" + SALES_AMOUNT + ") and " + //
			TOPUPS_COUNT + " Top-Ups (" + TOPUPS_AMOUNT + "). Ref " + TransactionsConfig.TRANSACTION_NO);
	protected Phrase response = Phrase.en("You will shortly receive an SMS for your Sales Query");
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

	public SalesQueryConfig setUssdCommand(Phrase ussdCommand)
	{
		this.ussdCommand = ussdCommand;
		return this;
	}

	public Phrase getSmsCommand()
	{
		return smsCommand;
	}

	public SalesQueryConfig setSmsCommand(Phrase smsCommand)
	{
		this.smsCommand = smsCommand;
		return this;
	}

	public Phrase getNotification()
	{
		return notification;
	}

	public SalesQueryConfig setNotification(Phrase notification)
	{
		this.notification = notification;
		return this;
	}

	public Phrase getResponse()
	{
		return response;
	}

	public SalesQueryConfig setResponse(Phrase response)
	{
		this.response = response;
		return this;
	}

	public BigDecimal getCharge()
	{
		return charge;
	}

	public SalesQueryConfig setCharge(BigDecimal charge)
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

	public SalesQueryConfig setVersion(int version)
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
