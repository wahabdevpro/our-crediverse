package hxc.ecds.protocol.rest.config;

import java.math.BigDecimal;
import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class TransactionStatusEnquiriesConfig implements IConfiguration
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String HISTORIC_TRANSACTION_NO = "{HistoricTransactionNo}";
	public static final String PIN = "{PIN}";
	public static final String STATUS = "{Status}";

	private static Phrase[] commandFields = new Phrase[] { //
			Phrase.en(HISTORIC_TRANSACTION_NO), Phrase.en(PIN) };

	private static Phrase[] notificationFields = new Phrase[] { //
			Phrase.en(STATUS), Phrase.en(HISTORIC_TRANSACTION_NO), };

	private static final long serialVersionUID = -4587435129485115948L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int version;
	protected Phrase ussdCommand = Phrase.en("*910*5*" + HISTORIC_TRANSACTION_NO + "*" + PIN + "#");
	protected Phrase smsCommand = Phrase.en("TRA_STATUS " + HISTORIC_TRANSACTION_NO + " " + PIN + "=>910");
	protected Phrase notification = Phrase.en("Status of " + HISTORIC_TRANSACTION_NO + ": " + STATUS);
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

	public TransactionStatusEnquiriesConfig setUssdCommand(Phrase ussdCommand)
	{
		this.ussdCommand = ussdCommand;
		return this;
	}

	public Phrase getSmsCommand()
	{
		return smsCommand;
	}

	public TransactionStatusEnquiriesConfig setSmsCommand(Phrase smsCommand)
	{
		this.smsCommand = smsCommand;
		return this;
	}

	public Phrase getNotification()
	{
		return notification;
	}

	public TransactionStatusEnquiriesConfig setNotification(Phrase notification)
	{
		this.notification = notification;
		return this;
	}

	public BigDecimal getCharge()
	{
		return charge;
	}

	public TransactionStatusEnquiriesConfig setCharge(BigDecimal charge)
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

	public TransactionStatusEnquiriesConfig setVersion(int version)
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
