package hxc.services.credittransfer;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.IServiceContext;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.SubscriptionState;
import com.concurrent.hxc.TransferRequest;
import com.concurrent.hxc.TransferResponse;
import com.concurrent.hxc.VasServiceInfo;

import hxc.connectors.air.AirException;
import hxc.connectors.air.proxy.AccountUpdate;
import hxc.connectors.air.proxy.Subscriber;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.lifecycle.ITemporalTrigger;
import hxc.connectors.soap.ISoapConnector;
import hxc.connectors.soap.ISubscriber;
import hxc.connectors.vas.VasCommand;
import hxc.connectors.vas.VasCommandParser;
import hxc.servicebus.ReturnCodes;
import hxc.services.notification.INotificationText;
import hxc.services.notification.Texts;
import hxc.services.transactions.CdrBase;
import hxc.services.transactions.CsvCdr;
import hxc.services.transactions.ICdr;
import hxc.services.transactions.ITransaction;
import hxc.services.transactions.Transaction;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.instrumentation.Metric;
import hxc.utils.protocol.ucip.DedicatedAccountInformation;

public class CreditTransfer extends CreditTransferBase
{
	final static Logger logger = LoggerFactory.getLogger(CreditTransfer.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private long donorDeduction = 0;
	private long recipientCredit = 0;

	private Metric transferMetric = Metric.CreateGraph("Transfer Info", 5000, "Transfers", "Successful Transfers", "Failed Transfers");

	// TODO Create class to handle stats
	private long successfulTransfers = 0;
	private long failedTransfers = 0;

	public long getDonorDeduction()
	{
		return donorDeduction;
	}

	public void setDonorDeduction(long donorDeduction)
	{
		this.donorDeduction = donorDeduction;
	}

	public long getRecipientCredit()
	{
		return recipientCredit;
	}

	public void setRecipientCredit(long recipientCredit)
	{
		this.recipientCredit = recipientCredit;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// VAS Service Implementation
	//
	// /////////////////////////////////
	@Override
	public String getServiceID()
	{
		return "CrXfr";
	}

	@Override
	public String getServiceName(String languageCode)
	{
		return "Credit Transfer";
	}

	public String getVariantID()
	{
		return "Default";
	}

	@Override
	public IMetric[] getMetrics()
	{
		return new IMetric[] { transferMetric };
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public CreditTransfer()
	{
		super();
		super.commandParser = new VasCommandParser(this, "MsisdnB", "UssdOption", "Amount", "Pin", "Anything")
		{
			@Override
			protected VasCommand[] getCommands()
			{
				return config.commands;
			}

			@Override
			public boolean parseCommandVariable(VasCommand.Processes process, String commandVariable, String value, CommandArguments arguments)
			{
				switch (commandVariable)
				{
					case "MsisdnB":
						arguments.memberNumber = arguments.recipientNumber = new Number(value);
						return true;

					case "Pin":
						arguments.pin = value;
						return true;

					case "UssdOption":

						int variantIDInteger = Integer.parseInt(value);

						String variantIDString = null;

						for (CreditTransferVariant variant : config.variants)
						{
							if (variant.getUssdID() == variantIDInteger)
							{
								variantIDString = variant.getVariantID();
								break;
							}
						}

						// if variantUSSDID is not found, calling method will exit with malformedRequest
						if (variantIDString == null)
							return false;
						else
							arguments.variantID = variantIDString;

						return true;

					case "Amount":
						arguments.amount = Long.parseLong(value);
						return true;

						// Incorrect number of variables will be resolved as malformedRequest
					case "Anything":
						return false;
				}

				return false;
			}

			@Override
			protected ISubscriber getSubscriberProxy(String msisdn)
			{
				return new Subscriber(msisdn, air, null);
			}

			@Override
			protected ISoapConnector getSoapConnector()
			{
				return soapConnector;
			}

		};
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Service Info
	//
	// /////////////////////////////////

	@Override
	public VasServiceInfo[] getServiceInfo(IServiceContext context, Number subscriberNumber, String variantID, Integer languageID, boolean activeOnly, boolean suggested) throws Exception
	{
		VasServiceInfo info = new VasServiceInfo();
		info.setServiceID(getServiceID());
		info.setServiceName(getServiceName(esb.getLocale().getLanguage(languageID)));
		info.setVariantID(getVariantID());
		info.setVariantName(getVariantID());
		info.setState(SubscriptionState.active);

		return new VasServiceInfo[] { info };
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Transfer credits
	//
	// /////////////////////////////////

	@Override
	public TransferResponse transfer(IServiceContext context, TransferRequest request)
	{

		String errorMessage = "Error message is not defined";
		Number donorNumber = request.getSubscriberNumber();
		Number recipientNumber = request.getRecipientNumber();

		// Create Response
		TransferResponse response = super.transfer(context, request);

		// Create a CDR
		CdrBase cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Get Language ID / Code
		int languageID = esb.getLocale().getLanguageID(request.getLanguageID());
		String languageCode = esb.getLocale().getLanguage(languageID);

		String problem = TransferRequest.validate(request);
		if (problem != null)
		{
			cdr.setAdditionalInformation(null);
			context.setResultText(getReturnCodeText(languageCode, ReturnCodes.malformedRequest));
			failedTransfers++;
			transferMetric.report(esb, successfulTransfers, failedTransfers);

			return response.exitWith(cdr, ReturnCodes.malformedRequest, problem);
		}

		// --------------------------------------------------------------------------------
		// Perform MSISDN validation as per operator numbering plan
		// Test Donor MSISDN -------------------------------------------------------------
		boolean isValidMSISDN = numberPlan.isValid(donorNumber.getAddressDigits());
		boolean isSpecialMSISDN = numberPlan.isSpecial(donorNumber.getAddressDigits());

		// Internationalize A number
		donorNumber.setAddressDigits(numberPlan.getInternationalFormat(donorNumber.getAddressDigits()));

		if (!isValidMSISDN && !isSpecialMSISDN)
		{
			errorMessage = getReturnCodeText(languageCode, ReturnCodes.invalidNumber);
			if (null == errorMessage)
			{
				errorMessage = new String("Error message not defined: Invalid MSISDN");
			}

			cdr.setAdditionalInformation(null);
			context.setResultText(errorMessage);
			failedTransfers++;
			transferMetric.report(esb, successfulTransfers, failedTransfers);

			return response.exitWith(cdr, ReturnCodes.invalidNumber, errorMessage + ": Donor");
		}

		// Test Recipient MSISDN ---------------------------------------------------------
		isValidMSISDN = numberPlan.isValid(recipientNumber.getAddressDigits());
		isSpecialMSISDN = numberPlan.isSpecial(recipientNumber.getAddressDigits());

		// Internationalize B number
		recipientNumber.setAddressDigits(numberPlan.getInternationalFormat(recipientNumber.getAddressDigits()));

		if (!isValidMSISDN && !isSpecialMSISDN)
		{
			errorMessage = getReturnCodeText(languageCode, ReturnCodes.invalidNumber);
			if (null == errorMessage)
			{
				errorMessage = new String("Error message not defined: Invalid MSISDN");
			}

			cdr.setAdditionalInformation(null);
			context.setResultText(errorMessage);
			failedTransfers++;
			transferMetric.report(esb, successfulTransfers, failedTransfers);

			return response.exitWith(cdr, ReturnCodes.invalidNumber, errorMessage + ": Recipient");
		}

		// --------------------------------------------------------------------------------
		// Database Connection Scope
		try (IDatabaseConnection dbConnection = database.getConnection(null))
		{
			String variantID = request.getVariantID();
			String serviceID = request.getServiceID();

			// Transaction Reversal Scope
			try (Transaction<?> transaction = transactions.create(cdr, dbConnection))
			{
				transaction.track(this, "Service: [ " + serviceID + " ] : " + variantID);

				// Get subscriber proxies
				Subscriber donor = getSubscriber(context, donorNumber, transaction);
				Subscriber recipient = getSubscriber(context, recipientNumber, transaction);

				// // Conditionally set trigger that periodically resets usage counter, i.e. if none exists already
				// ITemporalTrigger dbTriggers[] = lifecycle.getTemporalTriggers(dbConnection, serviceID, variantID, donor, recipient);
				//
				// if (dbTriggers.length == 0) //no trigger
				// {
				// Date date = new Date();
				// date.setTime(date.getTime() + 1 * 60 * 1000); // fire trigger in a minute
				//
				// CreditTransferUsageTrigger trigger = new CreditTransferUsageTrigger(donorNumber.getAddressDigits(),
				// recipientNumber.getAddressDigits(),
				// getServiceID(), variantID, date);
				// trigger.setBeingProcessed(false);
				// lifecycle.addTemporalTrigger(dbConnection, trigger);
				// }

				// Set Properties
				Properties properties = this.getProperties(context);
				this.setDonorProperties(properties, donor, cdr);

				// Update CDR
				cdr.setServiceID(serviceID);
				cdr.setVariantID(variantID);
				cdr.setProcessID("transfer");
				cdr.setB_MSISDN(recipientNumber.getAddressDigits());

				// Test if the Provider is attempting to send to Self.
				transaction.track(this, "CheckTransferToSelf: Donor");
				if (donorNumber.equals(recipientNumber))
				{
					errorMessage = getReturnCodeText(languageCode, ReturnCodes.cannotTransferToSelf);
					if (null == errorMessage)
					{
						errorMessage = new String("Error message not defined: cannotTransferToSelf");
						cdr.setAdditionalInformation(null);
						context.setResultText(getReturnCodeText(languageCode, ReturnCodes.cannotTransferToSelf));
						failedTransfers++;
						transferMetric.report(esb, successfulTransfers, failedTransfers);

						return response.exitWith(cdr, ReturnCodes.cannotTransferToSelf, errorMessage);
					}

					cdr.setAdditionalInformation(null);
					context.setResultText(errorMessage);
					failedTransfers++;
					transferMetric.report(esb, successfulTransfers, failedTransfers);

					return response.exitWith(cdr, ReturnCodes.cannotTransferToSelf, errorMessage);
				}

				try
				{
					// --------------------------------------------------------------------------------------
					// PIN authentication
					transaction.track(this, "CheckPIN: Donor");
					String pin = request.getPin();

					String internationalDonorNumber = numberPlan.getInternationalFormat(donorNumber.getAddressDigits());
					if (!pinService.validatePIN(response, cdr, dbConnection, null, internationalDonorNumber, pin))
					{
						ReturnCodes returnCode = response.getReturnCode();
						cdr.setAdditionalInformation(null);

						// Use CreditTransfer error message if available, else use PinService message, else use technicalProblem
						if (getReturnCodeText(languageCode, returnCode) != null)
						{
							errorMessage = getReturnCodeText(languageCode, returnCode);
						}
						else
						{
							if (response.getMessage() != null)
								errorMessage = response.getMessage();
							else
								errorMessage = getReturnCodeText(languageCode, ReturnCodes.technicalProblem);
						}

						context.setResultText(errorMessage);
						failedTransfers++;
						transferMetric.report(esb, successfulTransfers, failedTransfers);

						return response.exitWith(cdr, returnCode, errorMessage);
					}// pinService.validate()

					// --------------------------------------------------------------------------------------
					// Get donor usage counter
					UsageCounter donorCounter = null;

					// If table doesn't exist, create it
					if (!dbConnection.tableExists(UsageCounter.class))
					{
						dbConnection.createTable(UsageCounter.class);
					}
					else
					// table exists, simply query it
					{
						donorCounter = dbConnection.select(UsageCounter.class, "where msisdn=%s AND serviceId=%s", donorNumber.getAddressDigits(), serviceID);
					}

					if (null == donorCounter) // record not in database, insert new one
					{
						donorCounter = new UsageCounter(donorNumber.getAddressDigits(), serviceID, 0, 0, 0);
						dbConnection.insert(donorCounter);
						dbConnection.commit();
					}

					// -----
					// Get recipient usage counter
					UsageCounter recipientCounter = null;

					// If table doesn't exist, create it
					if (!dbConnection.tableExists(UsageCounter.class))
					{
						dbConnection.createTable(UsageCounter.class);
					}
					else
					// table exists, simply query it
					{
						recipientCounter = dbConnection.select(UsageCounter.class, "where msisdn=%s AND serviceId=%s", recipientNumber.getAddressDigits(), serviceID);
					}

					if (null == recipientCounter) // record not in database, insert new one
					{
						recipientCounter = new UsageCounter(recipientNumber.getAddressDigits(), serviceID, 0, 0, 0);
						dbConnection.insert(recipientCounter);
						dbConnection.commit();
					}

					// ---------------------------------------------------------------------------
					// Now get applicable variant for current transaction
					String variantName = variantID;
					CreditTransferVariant variant = getVariant(variantName);
					if (null == variant)
					{
						errorMessage = getReturnCodeText(languageCode, ReturnCodes.invalidVariant);
						if (null == errorMessage)
						{
							errorMessage = new String("Error message not defined: invalidVariant");
						}
						context.setResultText(errorMessage);
						cdr.setAdditionalInformation(null);
						failedTransfers++;
						transferMetric.report(esb, successfulTransfers, failedTransfers);

						return response.exitWith(cdr, ReturnCodes.invalidVariant, errorMessage + ": [" + variantName + "]");
					}

					// ---------------------------------------------------------------------------
					transaction.track(this, "CheckMonthlyQuota: Donor");
					// Monthly quota reached?
					long monthlyCount = donorCounter.getMonthlyCounter();
					Quota monthlyQuota = config.getDonorQuota(variant.getDonorQuotas(), QuotaPeriodUnits.MONTH);
					if (monthlyQuota != null)
					{
						long monthlyLimit = monthlyQuota.getLimit();
						if (monthlyCount >= monthlyLimit)
						{
							cdr.setAdditionalInformation(null);
							context.setResultText(getReturnCodeText(languageCode, ReturnCodes.quotaReached));
							failedTransfers++;
							transferMetric.report(esb, successfulTransfers, failedTransfers);

							return response.exitWith(cdr, ReturnCodes.quotaReached, "Monthly quota reached");
						}
					}

					// Weekly quota reached?
					transaction.track(this, "CheckWeeklyQuota: Donor");
					long weeklyCount = donorCounter.getWeeklyCounter();
					Quota weeklyQuota = config.getDonorQuota(variant.getDonorQuotas(), QuotaPeriodUnits.WEEK);
					if (weeklyQuota != null)
					{
						long weeklyLimit = weeklyQuota.getLimit();
						if (weeklyCount >= weeklyLimit)
						{
							cdr.setAdditionalInformation(null);
							context.setResultText(getReturnCodeText(languageCode, ReturnCodes.quotaReached));
							failedTransfers++;
							transferMetric.report(esb, successfulTransfers, failedTransfers);

							return response.exitWith(cdr, ReturnCodes.quotaReached, "Weekly quota reached");
						}
					}

					// Daily quota reached?
					transaction.track(this, "CheckDailyQuota: Donor");
					long dailyCount = donorCounter.getDailyCounter();
					Quota dailyQuota = config.getDonorQuota(variant.getDonorQuotas(), QuotaPeriodUnits.DAY);
					if (dailyQuota != null)
					{
						long dailyLimit = dailyQuota.getLimit();
						if (dailyCount >= dailyLimit)
						{
							cdr.setAdditionalInformation(null);
							context.setResultText(getReturnCodeText(languageCode, ReturnCodes.quotaReached));
							failedTransfers++;
							transferMetric.report(esb, successfulTransfers, failedTransfers);

							return response.exitWith(cdr, ReturnCodes.quotaReached, "Daily quota reached");
						}
					}

					// ---------------------------------------------------------------------------
					// Check (on AIR) if the donor is in Active subscriber state, i.e. all life-cycle dates are in the future.
					transaction.track(this, "GetAccountDetails: Donor");
					donor.getAccountDetails();

					transaction.track(this, "CheckSubscriberActive: Donor");
					if (!donor.isActive())
					{
						errorMessage = getReturnCodeText(languageCode, ReturnCodes.inactiveAParty);
						if (null == errorMessage)
						{
							errorMessage = new String("Error message not defined: inactiveAParty");
						}

						cdr.setAdditionalInformation(null);
						context.setResultText(errorMessage);
						failedTransfers++;
						transferMetric.report(esb, successfulTransfers, failedTransfers);

						return response.exitWith(cdr, ReturnCodes.inactiveAParty, errorMessage);
					}

					// ---------------------------------------------------------------------------
					// 1.1 Test if the recipient account is in a valid state too
					transaction.track(this, "CheckSubscriberActive: Recipient");
					if (!recipient.isActive())
					{
						cdr.setAdditionalInformation(null);
						context.setResultText(getReturnCodeText(languageCode, ReturnCodes.inactiveBParty));
						failedTransfers++;
						transferMetric.report(esb, successfulTransfers, failedTransfers);

						return response.exitWith(cdr, ReturnCodes.inactiveBParty, "Recipient account not active");
					}

					// ---------------------------------------------------------------------------
					// Check if B-party is eligible to receive (Service Class)
					transaction.track(this, "CheckAllowedServiceClass: Recipient");
					if (!recipient.isInServiceClass(variant.getRecipientServiceClassIds()))
					{
						errorMessage = getReturnCodeText(languageCode, ReturnCodes.notEligible);
						if (null == errorMessage)
						{
							errorMessage = new String("Error message not defined: notEligible");
						}
						context.setResultText(errorMessage);
						cdr.setAdditionalInformation(null);
						failedTransfers++;
						transferMetric.report(esb, successfulTransfers, failedTransfers);

						return response.exitWith(cdr, ReturnCodes.notEligible, errorMessage + " (recipient SC)");
					}

					// ---------------------------------------------------------------------------
					// Check if donor is eligible to transact (Service Class)
					transaction.track(this, "CheckAllowedServiceClass: Donor");
					if (!donor.isInServiceClass(variant.getDonorServiceClassIds()))
					{
						errorMessage = getReturnCodeText(languageCode, ReturnCodes.notEligible);
						if (null == errorMessage)
						{
							errorMessage = new String("Error message not defined: notEligible");
						}
						context.setResultText(errorMessage);
						cdr.setAdditionalInformation(null);
						failedTransfers++;
						transferMetric.report(esb, successfulTransfers, failedTransfers);

						return response.exitWith(cdr, ReturnCodes.notEligible, errorMessage + " (donor SC)");
					}

					// ---------------------------------------------------------------------------

					// Range check transfer amount
					transaction.track(this, "RangeCheckTransferAmount");
					long transferAmount = request.getAmount();
					long minTransferAmount = (variant.getMinAmount() + 5000) / 10000;
					long maxTransferAmount = (variant.getMaxAmount() + 5000) / 10000;

					// amount too low?
					if (transferAmount < minTransferAmount)
					{
						errorMessage = getReturnCodeText(languageCode, ReturnCodes.quantityTooSmall);
						if (errorMessage == null)
						{
							errorMessage = new String("Quantity too small: transferAmount");
						}
						context.setResultText(errorMessage);
						cdr.setAdditionalInformation(null);
						failedTransfers++;
						transferMetric.report(esb, successfulTransfers, failedTransfers);

						return response.exitWith(cdr, ReturnCodes.quantityTooSmall, errorMessage + ": Min = " + minTransferAmount);
					}

					// amount too high?
					if (transferAmount > maxTransferAmount)
					{
						errorMessage = getReturnCodeText(languageCode, ReturnCodes.quantityTooBig);
						if (errorMessage == null)
						{
							errorMessage = new String("Quantity too big: transferAmount");
						}
						context.setResultText(errorMessage);
						cdr.setAdditionalInformation(null);
						failedTransfers++;
						transferMetric.report(esb, successfulTransfers, failedTransfers);

						return response.exitWith(cdr, ReturnCodes.quantityTooBig, errorMessage + ": Max = " + maxTransferAmount);
					}

					// ---------------------------------------------------------------------------
					// Validate donor balance (Insufficient Funds)
					transaction.track(this, "CheckBalanceSufficient: Donor");

					int donorAccountID = variant.getDonorAccountID();

					// Determine transaction charge (from the variant)
					int donorServiceClassID = donor.getServiceClass();
					// ServiceClass donorSC = variant.getDonorServiceClass( donorServiceClassID );
					long transactionCharge = variant.getTransactionCharge(transferAmount, donorServiceClassID);

					logger.trace("Transfer amount: {}", transferAmount);
					logger.trace("Service Class [{}], Transaction charge [{}]", donorServiceClassID, (transactionCharge + 5000) / 10000);

					long donorBalance = 0L;
					if (donorAccountID == 0) // donor is Main account
					{
						donorBalance = donor.getAccountValue1();
					}
					else
					// donor account is a DA
					{
						// Ensure specified donor DA does exists
						if (!donor.hasDecicatedAccount(donorAccountID))
						{
							errorMessage = getReturnCodeText(languageCode, ReturnCodes.technicalProblem);
							if (errorMessage == null)
							{
								errorMessage = new String("Error message not defined: DA not found");
							}
							context.setResultText(errorMessage);
							cdr.setAdditionalInformation(null);
							failedTransfers++;
							transferMetric.report(esb, successfulTransfers, failedTransfers);

							return response.exitWith(cdr, ReturnCodes.technicalProblem, errorMessage + ": Donor (%s)", donorAccountID);
						}

						// Determine DA balance in monetary terms
						DedicatedAccountInformation daInfo = donor.getDedicatedAccount(donorAccountID);// NB: can return null
						// long daValue = daInfo.dedicatedAccountValue1;
						// donorBalance = (unitCostPerDonor * daValue + 5000) / 10000;
						donorBalance = daInfo.dedicatedAccountValue1;
					}

					// --------------------------------------------------------------
					// Check violation of daily, weekly and monthly transfer limits

					CumulativeLimits configuredDonorLimits = variant.getCumulativeDonorLimits();
					CumulativeLimits configuredRecipientLimits = variant.getCumulativeRecipientLimits();

					// Check daily cumulative limit
					// Donor
					transaction.track(this, "CheckDailyLimit: Donor");
					long runningDailyTotal = donorCounter.getDailySentAccumulator();
					if (runningDailyTotal >= configuredDonorLimits.getTotalDailyLimit())
					{
						errorMessage = getReturnCodeText(languageCode, ReturnCodes.quotaReached);
						if (errorMessage == null)
						{
							errorMessage = new String("Message not defined: dailyQuotaReached");
						}
						context.setResultText(errorMessage);
						cdr.setAdditionalInformation(null);
						failedTransfers++;
						transferMetric.report(esb, successfulTransfers, failedTransfers);

						return response.exitWith(cdr, ReturnCodes.quotaReached, errorMessage + ": Donor: Daily cumulative transfer limit");
					}

					// Recipient
					transaction.track(this, "CheckDailyLimit: Recipient");
					long runningDailyTotal_Recipient = recipientCounter.getDailyReceivedAccumulator();
					if (runningDailyTotal_Recipient >= configuredRecipientLimits.getTotalDailyLimit())
					{
						errorMessage = getReturnCodeText(languageCode, ReturnCodes.quotaReached);
						if (errorMessage == null)
						{
							errorMessage = new String("Message not defined: dailyQuotaReached");
						}
						context.setResultText(errorMessage);
						cdr.setAdditionalInformation(null);
						failedTransfers++;
						transferMetric.report(esb, successfulTransfers, failedTransfers);

						return response.exitWith(cdr, ReturnCodes.cannotReceiveCredit, errorMessage + ": Recipient: Daily cumulative receive limit reached");
					}

					// --------------------------------------------------------------------

					// Check donor weekly cumulative transfer limit
					transaction.track(this, "CheckWeeklyLimit: Donor");
					long runningWeeklyTotal = donorCounter.getWeeklySentAccumulator();
					if (runningWeeklyTotal >= configuredDonorLimits.getTotalWeeklyLimit())
					{
						errorMessage = getReturnCodeText(languageCode, ReturnCodes.quotaReached);
						if (errorMessage == null)
						{
							errorMessage = new String("Message not defined: weeklyQuotaReached");
						}
						context.setResultText(errorMessage);
						cdr.setAdditionalInformation(null);
						failedTransfers++;
						transferMetric.report(esb, successfulTransfers, failedTransfers);

						return response.exitWith(cdr, ReturnCodes.quotaReached, errorMessage + ": Donor: Weekly cumulative transfer limit");
					}

					// Recipient (weekly total)
					transaction.track(this, "CheckWeeklyLimit: Recipient");
					long runningWeeklyTotal_Recipient = recipientCounter.getWeeklyReceivedAccumulator();
					if (runningWeeklyTotal_Recipient >= configuredRecipientLimits.getTotalWeeklyLimit())
					{
						errorMessage = getReturnCodeText(languageCode, ReturnCodes.quotaReached);
						if (errorMessage == null)
						{
							errorMessage = new String("Message not defined: weeklyQuotaReached");
						}
						context.setResultText(errorMessage);
						cdr.setAdditionalInformation(null);
						failedTransfers++;
						transferMetric.report(esb, successfulTransfers, failedTransfers);

						return response.exitWith(cdr, ReturnCodes.cannotReceiveCredit, errorMessage + ": Recipient: Weekly cumulative receive limit reached");
					}

					// --------------------------------------------------------------------
					// Check donor monthly cumulative transfer limit
					transaction.track(this, "CheckMonthlyLimit: Donor");
					long runningMonthlyTotal = donorCounter.getMonthlySentAccumulator();
					if (runningMonthlyTotal >= configuredDonorLimits.getTotalMonthlyLimit())
					{
						errorMessage = getReturnCodeText(languageCode, ReturnCodes.quotaReached);
						if (errorMessage == null)
						{
							errorMessage = new String("Message not defined: monthlyQuotaReached");
						}
						context.setResultText(errorMessage);
						cdr.setAdditionalInformation(null);
						failedTransfers++;
						transferMetric.report(esb, successfulTransfers, failedTransfers);

						return response.exitWith(cdr, ReturnCodes.quotaReached, errorMessage + ": Donor: Monthly cumulative transfer limit");
					}

					// Recipient (monthly limit)
					transaction.track(this, "CheckMonthlyLimit: Recipient");
					long runningMonthlyTotal_Recipient = recipientCounter.getMonthlyReceivedAccumulator();
					if (runningMonthlyTotal_Recipient >= configuredRecipientLimits.getTotalMonthlyLimit())
					{
						errorMessage = getReturnCodeText(languageCode, ReturnCodes.quotaReached);
						if (errorMessage == null)
						{
							errorMessage = new String("Message not defined: monthlyQuotaReached");
						}
						context.setResultText(errorMessage);
						cdr.setAdditionalInformation(null);
						failedTransfers++;
						transferMetric.report(esb, successfulTransfers, failedTransfers);

						return response.exitWith(cdr, ReturnCodes.cannotReceiveCredit, errorMessage + ": Recipient: Monthly cumulative receive limit reached");
					}

					// --------------------------------------------------------------------
					// DEBIT and CREDIT calculation for donor and recipient balance checks
					int recipientAccountID = variant.getRecipientAccountID();
					DebitsAndCredits d = new DebitsAndCredits();
					DebitsAndCreditsResponse calculation = d.calculateDebitsAndCredits(transferAmount, transactionCharge, variant);
					this.setDonorDeduction(calculation.getDonorDADebit());
					this.setRecipientCredit(calculation.getRecipientDACredit());
					long totalDebit = calculation.getDonorDADebit();

					long allowedDonorMinBalance = (variant.getDonorMinBalance() + 5000) / 10000;

					// Check for insufficient funds
					if (totalDebit > (donorBalance - allowedDonorMinBalance))
					{
						errorMessage = getReturnCodeText(languageCode, ReturnCodes.insufficientBalance);
						if (errorMessage == null)
						{
							errorMessage = new String("Message not defined: insufficientBalance");
						}
						context.setResultText(errorMessage);
						cdr.setAdditionalInformation(null);
						failedTransfers++;
						transferMetric.report(esb, successfulTransfers, failedTransfers);

						return response.exitWith(cdr, ReturnCodes.insufficientBalance, errorMessage + ": Donor");
					}

					// ---------------------------------------------------------------------------
					// Check if recipient balance will overflow
					transaction.track(this, "CheckBalanceOverflow: Recipient");
					long maxAllowedBalance = (variant.getRecipientMaxBalance() + 5000) / 10000;
					long currentRecipientBalance = 0;

					if (recipientAccountID == 0) // Main account
					{
						currentRecipientBalance = recipient.getAccountValue1();
					}
					else
					{
						// Ensure recipient DA does exist
						transaction.track(this, "CheckDedicatedAccountExists: Recipient");
						if (!recipient.hasDecicatedAccount(recipientAccountID))
						{
							errorMessage = getReturnCodeText(languageCode, ReturnCodes.technicalProblem);
							if (null == errorMessage)
							{
								errorMessage = "Error message not defined: DA not found";
							}
							context.setResultText(errorMessage);
							cdr.setAdditionalInformation(null);
							failedTransfers++;
							transferMetric.report(esb, successfulTransfers, failedTransfers);

							return response.exitWith(cdr, ReturnCodes.technicalProblem, errorMessage + ": Recipient (%s)", recipientAccountID);
						}

						DedicatedAccountInformation daInformation = recipient.getDedicatedAccount(recipientAccountID);
						currentRecipientBalance = daInformation.dedicatedAccountValue1;
					}

					// Check for possible overflow
					// transaction.track(this, "CheckBalanceOverflow: Recipient");
					if ((currentRecipientBalance + calculation.getRecipientDACredit()) > maxAllowedBalance)
					{
						errorMessage = getReturnCodeText(languageCode, ReturnCodes.excessiveBalance);
						if (null == errorMessage)
						{
							errorMessage = new String("Error message not defined: excessiveBalance");
						}
						context.setResultText(errorMessage);
						cdr.setAdditionalInformation(null);
						failedTransfers++;
						transferMetric.report(esb, successfulTransfers, failedTransfers);

						return response.exitWith(cdr, ReturnCodes.excessiveBalance, errorMessage);
					}

					// ///////////////////////////////////////////////////////////////////////////////////////
					//
					// DEBIT donor & CREDIT recipient
					//
					// ///////////////////////////////////////////////////////////////////////////////////////

					// Debit donor
					transaction.track(this, "UpdateAccounts:Debit Donor");
					if (0 == donorAccountID) // Main account
					{
						donor.updateAccounts(-donorDeduction);
					}
					else
					{
						AccountUpdate donorAccountUpdate = new AccountUpdate(donorAccountID, variant.getDonorAccountType().ordinal(), -donorDeduction, null, null, null, null, null);
						donor.updateAccounts(null, donorAccountUpdate);
					}
					// TODO: Dangerous cast
					cdr.setChargeLevied((int) donorDeduction);

					// ---------------------------------------------------------------------------
					// Credit recipient
					transactionCharge = (transactionCharge + 5000) / 10000; // scaling down by 10000
					int recipientLanguageID = recipient.getLanguageID();

					transaction.track(this, "UpdateAccounts: Credit Recipient");

					if (0 == recipientAccountID) // Main account
					{
						// recipient.updateAccounts(numberOfDonatedItems, null);
						recipient.updateAccounts(recipientCredit);

						properties.setCharge((Long.valueOf(transactionCharge)).toString());
						properties.setExpiryDate("Never");
					}
					else
					// Credit recipient DA
					{
						AccountUpdate recipientAccountUpdate = new AccountUpdate(recipientAccountID, variant.getRecipientAccountType().ordinal(), recipientCredit, null,
								variant.getRecipientExpiryDays(), null, null, null);

						recipient.updateAccounts(null, recipientAccountUpdate);

						DedicatedAccountInformation recipientDA = recipient.getDedicatedAccount(recipientAccountID);
						properties.setExpiryDate(locale.formatDate(recipientDA.expiryDate, recipientLanguageID));
					}

					// ---------------------------------------------------------------------------
					// Send a configurable SMS to the Donor to inform him of the successful consumer addition,
					// the fee which has been charged and the MSISDN of the added consumer.
					transaction.track(this, "SendProviderSMS");

					// Donor charge
					properties.setCharge((Long.valueOf(totalDebit)).toString());

					// Donor units
					Texts donorUnits = variant.getDonorUnits();
					// properties.setCurrencyUnits( donorUnits.getText(donor.getLanguageID() - 1) );
					properties.setCurrencyUnits(donorUnits.getSafeText(languageID));

					// Amount of topup
					properties.setNumberOfBenefits(calculation.getRecipientDACredit());

					// Recipient units
					Texts units = variant.getRecipientUnits();
					// properties.setBenefitUnits(units.getText(recipientLanguageID - 1));
					properties.setBenefitUnits(units.getSafeText(languageID));

					// Recipient MSISDN
					properties.setRecipientMsisdn(recipient.getInternationalNumber());

					// Recipient units
					// properties.setBenefitUnits(variant.getRecipientUnits().getText(donor.getLanguageID() - 1));

					// Donor balance after transfer
					properties.setDonorBalanceAfter(Long.toString(donorBalance - totalDebit));

					sendSubscriberSMS(donor, smsDonorTransfered, properties);

					if (0 == recipientAccountID) // Main account topup
					{
						sendSubscriberSMS(recipient, mainRecipientToppedUp, properties);
					}
					else
					// DA topup
					{
						sendSubscriberSMS(recipient, smsRecipientReceived, properties);
					}

					// ---------------------------------------------------------------------------
					// Update counters
					donorCounter.incrementCounters(); // daily,weekly,monthly
					donorCounter.incrementSentCumulativeTotals(transferAmount); // daily,weekly,monthly
					recipientCounter.incrementReceivedCumulativeTotals(transferAmount);
					dbConnection.update(donorCounter);

					UsageCounter counterB = dbConnection.select(UsageCounter.class, "where msisdn=%s AND serviceId=%s", recipientNumber.getAddressDigits(), serviceID);
					if (counterB == null)
					{
						counterB = new UsageCounter(recipientNumber.getAddressDigits(), serviceID, 0, 0, 0);
						counterB.incrementReceivedCumulativeTotals(transferAmount);
						dbConnection.insert(counterB);
					}
					else
					{
						counterB.incrementReceivedCumulativeTotals(transferAmount);
						dbConnection.update(counterB);
					}

					dbConnection.commit();
				}
				catch (AirException e)
				{
					// Thread.sleep(10*60*1000);//10min sleep used while debugging
					cdr.setAdditionalInformation(null);
					context.setResultText(getReturnCodeText(languageCode, ReturnCodes.technicalProblem));
					failedTransfers++;
					transferMetric.report(esb, successfulTransfers, failedTransfers);

					return response.exitWith(cdr, e.getReturnCode(), e);
				}
				catch (NullPointerException npe)
				{
					logger.error("Transfer failed with null", npe.getMessage());
					failedTransfers++;
					transferMetric.report(esb, successfulTransfers, failedTransfers);

					return response.exitWith(cdr, ReturnCodes.technicalProblem, npe);
				}
				catch (Exception e)
				{
					cdr.setAdditionalInformation(null);
					ReturnCodes returnCode = ReturnCodes.technicalProblem;
					// Check if the exception thrown was a returnCode
					try
					{
						returnCode = ReturnCodes.valueOf(e.getMessage());
					}
					catch (IllegalArgumentException iae)
					{
						// Exception e was not a return code, re-throw the exception
						throw new Exception(e.getMessage());
					}
					context.setResultText(getReturnCodeText(languageCode, returnCode));
					failedTransfers++;
					transferMetric.report(esb, successfulTransfers, failedTransfers);

					return response.exitWith(cdr, returnCode, e);
				}
				catch (Throwable e)
				{
					cdr.setAdditionalInformation(null);
					context.setResultText(getReturnCodeText(languageCode, ReturnCodes.technicalProblem));
					failedTransfers++;
					transferMetric.report(esb, successfulTransfers, failedTransfers);

					return response.exitWith(cdr, ReturnCodes.technicalProblem, e);
				}

				// Conditionally set trigger that periodically resets usage counter, i.e. if none exists already
				ITemporalTrigger dbTriggers[] = lifecycle.getTemporalTriggers(dbConnection, serviceID, variantID, donor, recipient, null);

				if (dbTriggers.length == 0) // no trigger
				{
					Date date = new Date();
					date.setTime(date.getTime() + 1 * 60 * 1000); // fire trigger in a minute

					CreditTransferUsageTrigger trigger = new CreditTransferUsageTrigger(donorNumber.getAddressDigits(), recipientNumber.getAddressDigits(), getServiceID(), variantID, date);
					trigger.setBeingProcessed(false);
					lifecycle.addTemporalTrigger(dbConnection, trigger);
				}

				// Terminate the process with a "Success" response Code.
				transaction.complete();
				cdr.setAdditionalInformation(null);

				// USSD message
				context.setResultText(notifications.get(ussdDonorTransfered, languageCode, locale, properties).getText());

				successfulTransfers++;
				transferMetric.report(esb, successfulTransfers, failedTransfers);

				return response.exitWith(cdr, ReturnCodes.success, "Transfer Success");
			}
		} // database
		catch (Throwable e)
		{
			cdr.setAdditionalInformation(null);
			context.setResultText(getReturnCodeText(languageCode, ReturnCodes.technicalProblem));
			return response.exitWith(cdr, ReturnCodes.technicalProblem, e);
		}
	}// transfer()

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	private Subscriber getSubscriber(IServiceContext context, Number subscriberNumber, ITransaction transaction)
	{
		Subscriber result;
		if (context.getSubscriberProxy() != null && context.getSubscriberProxy() instanceof Subscriber //
				&& context.getSubscriberProxy().isSameNumber(subscriberNumber.toMSISDN()))
		{
			result = (Subscriber) context.getSubscriberProxy();
			result.setTransaction(transaction);
		}
		else
		{
			result = new Subscriber(subscriberNumber.toMSISDN(), air, transaction);
			context.setSubscriberProxy(result);
		}

		return result;
	}

	private Properties getProperties(IServiceContext context)
	{
		Object properties = context.getProperties();
		if (properties == null || !(properties instanceof Properties))
		{
			properties = new Properties();
			context.setProperties(properties);
		}

		return (Properties) properties;
	}

	private void setDonorProperties(Properties properties, Subscriber donor, CdrBase cdr)
	{
		// Cdr
		if (cdr != null)
			cdr.setA_MSISDN(donor.getInternationalNumber());

		// Properties
		properties.setDonorMsisdn(donor.getNationalNumber());
	}

	@SuppressWarnings("unused")
	private void setRecipientProperties(Properties properties, Subscriber recipient, ICdr cdr)
	{
		// Cdr
		cdr.setB_MSISDN(recipient.getInternationalNumber());

		// Properties
		properties.setRecipientMsisdn(recipient.getNationalNumber());
	}

	private void sendSubscriberSMS(Subscriber subscriber, int notifcationID, Properties properties)
	{
		String languageCode = esb.getLocale().getLanguage(subscriber.getLanguageID());
		INotificationText text = notifications.get(notifcationID, languageCode, locale, properties);
		if (text != null && text.getText() != null && text.getText().length() > 0)
			smsConnector.send(config.smsSourceAddress, subscriber.getInternationalNumber(), text);
	}
}
