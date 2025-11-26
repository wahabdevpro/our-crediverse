package hxc.ecds.protocol.rest.config;

import java.util.List;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class ReplenishConfig implements IConfiguration
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String WEB_USER = "{WebUser}";
	public static final String AMOUNT = "{Amount}";
	public static final String BONUS_PROVISION = "{BonusProvision}";
	public static final String NEW_BALANCE = "{NewBalance}";
	public static final String NEW_BONUS_BALANCE = "{NewBonusBalance}";
	public static final String NEW_TOTAL_BALANCE = "{NewTotalBalance}";

	private static Phrase[] notificationFields = new Phrase[] { //
			Phrase.en(WEB_USER), Phrase.en(AMOUNT), Phrase.en(BONUS_PROVISION), //
			Phrase.en(NEW_BALANCE), Phrase.en(NEW_BONUS_BALANCE), Phrase.en(NEW_TOTAL_BALANCE), TransactionsConfig.PHRASE_TRANSACTION_NO };

	private static final long serialVersionUID = 8867113173301796438L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	protected int version;
	protected Phrase notification = Phrase.en( //
			"The ROOT Account has been replenished by " + WEB_USER + " with " + AMOUNT //
					+ ", and " + BONUS_PROVISION + " for bonus provision. The new ROOT balance is " //
					+ NEW_BALANCE + " and a Bonus Provision for " + NEW_BONUS_BALANCE + ". Ref " + TransactionsConfig.TRANSACTION_NO);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public Phrase[] listNotificationFields()
	{
		return notificationFields;
	}

	public Phrase getNotification()
	{
		return notification;
	}

	public ReplenishConfig setNotification(Phrase notification)
	{
		this.notification = notification;
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

	public ReplenishConfig setVersion(int version)
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
		;

		return validator.toList();
	}

}
