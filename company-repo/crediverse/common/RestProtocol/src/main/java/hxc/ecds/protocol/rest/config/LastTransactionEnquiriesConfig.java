package hxc.ecds.protocol.rest.config;

import java.math.BigDecimal;
import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class LastTransactionEnquiriesConfig implements IConfiguration
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String PIN = "{PIN}";
	public static final String LAST_TRANSACTION_NO = "{LastTransactionNo}";
	public static final String STATUS = "{Status}";
	public static final String RECIPIENT_MSISDN = "{RecipientMSISDN}";
	public static final String DATE_TIME = "{DateTime}";
	public static final String AMOUNT = "{Amount}";
	public static final String BONUS_AMOUNT = "{BonusAmount}";
	public static final String TRANSACTION_LIST = "{TransactionList}";

	private static Phrase[] commandFields = new Phrase[] { //
			Phrase.en(PIN) };

	private static Phrase[] notificationFields = new Phrase[] { //
			Phrase.en(STATUS), Phrase.en(LAST_TRANSACTION_NO), Phrase.en(RECIPIENT_MSISDN), //
			Phrase.en(DATE_TIME), Phrase.en(AMOUNT), Phrase.en(BONUS_AMOUNT) };

	private static Phrase[] listNotificationFields = new Phrase[] { Phrase.en(TRANSACTION_LIST) };

	private static final long serialVersionUID = -2880536216140468327L;
	public static final int MAX_TRANSACTION_COUNT = 10;
	
	public static final String ORDER_BY_NUMBER = "N+";
	public static final String ORDER_BY_TIME = "T+";
	public static final String ORDER_BY_STATUS = "S+";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int version;
	protected Phrase ussdCommand = Phrase.en("*911*5*" + PIN + "#");
	protected Phrase smsCommand = Phrase.en("TRA_STATUS " + PIN + "=>910");
	protected Phrase notification = Phrase.en(LAST_TRANSACTION_NO + ", " + DATE_TIME + ", " + STATUS + ", " + AMOUNT + ", " + BONUS_AMOUNT + ", " + RECIPIENT_MSISDN);
	protected Phrase listNotification = Phrase.en(TRANSACTION_LIST);
	protected Phrase transactionLine = Phrase.en(LAST_TRANSACTION_NO + ", " + DATE_TIME + ", " + STATUS + ", " + AMOUNT + ", " + BONUS_AMOUNT + ", " + RECIPIENT_MSISDN + "\n");
	protected int maxTransactions = 3;
	protected String ordering = ORDER_BY_NUMBER;
	protected BigDecimal charge = BigDecimal.ZERO;

	protected Phrase successful = Phrase.en("SUCCESSFUL");
	protected Phrase failed = Phrase.en("FAILED");
	protected Phrase pending = Phrase.en("PENDING");

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

	public Phrase[] listListNotificationFields()
	{
		return listNotificationFields;
	}

	public Phrase getUssdCommand()
	{
		return ussdCommand;
	}

	public LastTransactionEnquiriesConfig setUssdCommand(Phrase ussdCommand)
	{
		this.ussdCommand = ussdCommand;
		return this;
	}

	public Phrase getSmsCommand()
	{
		return smsCommand;
	}

	public LastTransactionEnquiriesConfig setSmsCommand(Phrase smsCommand)
	{
		this.smsCommand = smsCommand;
		return this;
	}

	public Phrase getNotification()
	{
		return notification;
	}

	public LastTransactionEnquiriesConfig setNotification(Phrase notification)
	{
		this.notification = notification;
		return this;
	}

	public BigDecimal getCharge()
	{
		return charge;
	}

	public LastTransactionEnquiriesConfig setCharge(BigDecimal charge)
	{
		this.charge = charge;
		return this;
	}

	public Phrase getListNotification()
	{
		return listNotification;
	}

	public LastTransactionEnquiriesConfig setListNotification(Phrase listNotification)
	{
		this.listNotification = listNotification;
		return this;
	}

	public int getMaxTransactions()
	{
		return maxTransactions;
	}

	public LastTransactionEnquiriesConfig setMaxTransactions(int maxTransactions)
	{
		this.maxTransactions = maxTransactions;
		return this;
	}

	public Phrase getSuccessful()
	{
		return successful;
	}

	public LastTransactionEnquiriesConfig setSuccessful(Phrase successful)
	{
		this.successful = successful;
		return this;
	}

	public Phrase getFailed()
	{
		return failed;
	}

	public LastTransactionEnquiriesConfig setFailed(Phrase failed)
	{
		this.failed = failed;
		return this;
	}

	public Phrase getPending()
	{
		return pending;
	}

	public LastTransactionEnquiriesConfig setPending(Phrase pending)
	{
		this.pending = pending;
		return this;
	}

	public Phrase getTransactionLine()
	{
		return transactionLine;
	}

	public LastTransactionEnquiriesConfig setTransactionLine(Phrase transactionLine)
	{
		this.transactionLine = transactionLine;
		return this;
	}
	

	public String getOrdering()
	{
		return ordering;
	}

	public LastTransactionEnquiriesConfig setOrdering(String ordering)
	{
		this.ordering = ordering;
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

	public LastTransactionEnquiriesConfig setVersion(int version)
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
		LastTransactionEnquiriesConfig template = new LastTransactionEnquiriesConfig();

		if (Phrase.nullOrEmpty(listNotification))
			listNotification = template.listNotification;

		if (Phrase.nullOrEmpty(transactionLine))
			transactionLine = template.transactionLine;

		if (Phrase.someNullOrEmpty(successful))
			successful = template.successful;

		if (Phrase.someNullOrEmpty(failed))
			failed = template.failed;

		if (Phrase.someNullOrEmpty(pending))
			pending = template.pending;
		
		if (ordering == null || ordering.isEmpty())
			ordering = template.ordering;

		if (maxTransactions < 1 || maxTransactions > MAX_TRANSACTION_COUNT)
			maxTransactions = template.maxTransactions;
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
				.validExpandableText("listNotification", listNotification, listNotificationFields) //
				.validExpandableText("transactionLine", transactionLine, notificationFields) //
				.validUssdCommand("ussdCommand", ussdCommand, commandFields) //
				.validSmsCommand("smsCommand", smsCommand, commandFields) //
				.notAnyEmpty("successful", successful) //
				.notAnyEmpty("failed", failed) //
				.notAnyEmpty("pending", pending) //
				.notLess("charge", charge, BigDecimal.ZERO) //
				.notLess("maxTransactions", maxTransactions, 1) //
				.notMore("maxTransactions", maxTransactions, MAX_TRANSACTION_COUNT) //
				.notNull("ordering", ordering) //
				.oneOf("ordering", ordering, ORDER_BY_NUMBER, ORDER_BY_STATUS, ORDER_BY_TIME) //
				.isMoney("charge", charge);

		return validator.toList();
	}

}
