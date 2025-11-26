package hxc.services.ecds.rest.batch;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.AdjustmentRequest;
import hxc.ecds.protocol.rest.AdjustmentResponse;
import hxc.ecds.protocol.rest.BatchIssue;
import hxc.ecds.protocol.rest.RequestHeader;
import hxc.ecds.protocol.rest.ResponseHeader;
import hxc.ecds.protocol.rest.config.AdjustmentsConfig;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.Batch;
import hxc.services.ecds.model.Stage;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.WebUser;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.rest.TransactionState;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StringExpander;

public class AdjustmentProcessor extends Processor<Adjustment>
{
	final static Logger logger = LoggerFactory.getLogger(AdjustmentProcessor.class);
	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	///////////////////////////////////
	private static String computerName = null;
	private static final String TBD = "tbd";

	private Session coSession;

	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	///////////////////////////////////
	public Session getCoSession()
	{
		return coSession;
	}

	public AdjustmentProcessor setCoSession(Session coSession)
	{
		this.coSession = coSession;
		return this;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	///////////////////////////////////
	public AdjustmentProcessor(ICreditDistribution context, boolean mayInsert, boolean mayUpdate, boolean mayDelete)
	{
		super(context, mayInsert, mayUpdate, mayDelete);

		// Get the Local Machine Name
		if (computerName == null || computerName.isEmpty())
		{
			try
			{
				InetAddress ip = InetAddress.getLocalHost();
				String name = ip.getHostName();
				int index = name.indexOf('.');
				if (index > 0)
					name = name.substring(0, index);
				computerName = name;
			}
			catch (Exception ex)
			{
				logger.warn("Failed to get hostname, defaulting to 'localhost'", ex);
				computerName = "localhost";
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Processor
	//
	///////////////////////////////////

	public static String getTemplate(boolean inclusiveOfProvision)
	{
		return inclusiveOfProvision ? //
				"verb,id,account_number,msisdn,total_amount,reason\n" : //
				"verb,id,account_number,msisdn,amount,reason\n";
	}

	@Override
	protected String getProperty(String heading, boolean lastColumn)
	{
		switch (heading)
		{
			case "id":
				return "id";

			case "account_number":
				return "accountNumber";

			case "msisdn":
				return "mobileNumber";

			case "amount":
				return "amount";

			case "total_amount":
				return "totalAmount";

			case "reason":
				return "reason";

			default:
				return null;
		}

	}

	@Override
	protected String getAuditType()
	{
		return null;
	}

	@Override
	protected Adjustment instantiate(EntityManager em, State state, Adjustment from)
	{
		Adjustment result = from == null ? new Adjustment() : new Adjustment(from);
		return result;
	}

	@Override
	protected void amend(EntityManager em, State state, Adjustment adjustment, String[] rowValues, List<Object> other)
	{
		for (int index = 0; index < rowValues.length && index < headings.length; index++)
		{
			String property = propertyMap.get(index);
			if (property == null)
				continue;
			String value = rowValues[index];
			String heading = headings[index];
			switch (property)
			{
				case "id":
					adjustment.setId(state.parseInt(heading, value, adjustment.getId()));
					break;

				case "accountNumber":
					adjustment.setAccountNumber(value);
					break;

				case "mobileNumber":
					adjustment.setMobileNumber(context.toMSISDN(value));
					break;

				case "amount":
					adjustment.setAmount(state.parseBigDecimal("amount", value));
					break;

				case "totalAmount":
				{
					BigDecimal percentage = adjustment.getDownStreamPercentage();
					if (percentage == null)
					{
						state.addIssue(BatchIssue.INVALID_VALUE, "totalAmount", null, "Invalid Downstream Percentage");
						break;
					}
					BigDecimal totalAmount = state.parseBigDecimal("totalAmount", value);
					if (totalAmount != null)
					{
						percentage = percentage.add(BigDecimal.ONE);
						BigDecimal amount = totalAmount.divide(percentage, context.getMoneyScale(), RoundingMode.UP);
						adjustment.setAmount(amount);
					}
				}
					break;

				case "reason":
					adjustment.setReason(value);
					break;

				default:
					break;
			}
		}
		adjustment.setCoSignatorySessionID(coSession == null ? TBD : coSession.getSessionID());
	}

	@Override
	protected Adjustment loadExisting(EntityManager em, State state, String[] rowValues)
	{
		// Load By ID
		Agent agent = null;
		Integer columnIndex = columnIndexForProperty("id");
		if (columnIndex != null)
		{
			int id = state.parseInt("id", rowValues[columnIndex], 0);
			agent = Agent.findByID(em, id, state.getCompanyID());
		}

		// Load by MSISDN
		columnIndex = columnIndexForProperty("mobileNumber");
		if (agent == null && columnIndex != null)
		{
			String msisdn = context.toMSISDN(rowValues[columnIndex]);
			agent = Agent.findByMSISDN(em, msisdn, state.getCompanyID());
		}

		if (agent == null)
			return null;

		if (Agent.STATE_PERMANENT.equals(agent.getState()))
			state.addIssue(BatchIssue.CANNOT_ADJUST, "state", null, "Cannot adjust Permanent Account");

		Account account = Account.findByAgentID(em, agent.getId(), false);
		if (account == null)
			return null;

		Adjustment adjustment = new Adjustment() //
				.setId(agent.getId()) //
				.setCompanyID(agent.getCompanyID()) //
				.setAccountNumber(agent.getAccountNumber()) //
				.setMobileNumber(agent.getMobileNumber()) //
				.setLastUserID(account.getLastUserID()) //
				.setVersion(account.getVersion()) //
				.setDownStreamPercentage(agent.getTier().getDownStreamPercentage()) //
				.setLastTime(account.getLastTime());

		return adjustment;
	}

	@Override
	protected Stage addNew(EntityManager em, State state, Adjustment newInstance, List<Object> other)
	{
		state.addIssue(BatchIssue.CANNOT_UPDATE, null, null, "Cannot insert");
		return null;
	}

	@Override
	protected Stage updateExisting(EntityManager em, State state, Adjustment existing, Adjustment updated, List<Object> other)
	{
		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_UPDATE, ET_ACCOUNT) //
				.setEntityID(updated.getId()) //
				.setAccountNumber(updated.getAccountNumber()) //
				.setMobileNumber(updated.getMobileNumber()) //
				.setBd1(updated.getAmount()) //
				.setDescription(updated.getReason()) //
				.setEntityVersion(updated.getVersion()) //
				.setAltPhoneNumber(context.findCompanyInfoByID(em, state.getCompanyID()).getNextTransactionNumber(state.getCompanyID())) //
		;
		
		return stage;
	}

	@Override
	protected Stage deleteExisting(EntityManager em, State state, Adjustment existing, List<Object> other)
	{
		state.addIssue(BatchIssue.CANNOT_DELETE, null, null, "Cannot delete an Adjustment");
		return null;
	}

	@Override
	protected void verifyExisting(EntityManager em, State state, Adjustment existing, Adjustment instance, List<Object> other)
	{
		verify(state, "accountNumber", existing.getAccountNumber(), instance.getAccountNumber());
		verify(state, "mobileNumber", existing.getMobileNumber(), instance.getMobileNumber());
		verify(state, "amount", existing.getAmount(), instance.getAmount());
	}

	@Override
	public boolean complete(EntityManager em, State state)
	{
		// Exit if there are issues
		if (state.hasIssues())
			return false;

		// Adjust
		Integer lineNumber = null;
		try
		{
			// Get the Root Account
			Agent rootAgent = Agent.findRoot(em, state.getCompanyID());
			Account rootAccount = Account.findByAgentID(em, rootAgent.getId(), true);

			// For groups of adjustments
			int adjustmentCount = 0;
			int first = 0;
			int max = 50;
			while (true)
			{
				// Get the next batch
				List<Stage> stagingRecords = Stage.findRecords(em, state.getCompanyID(), state.getBatch().getId(), ET_ACCOUNT, Stage.ACTION_UPDATE, first, max);
				if (stagingRecords.isEmpty())
					break;
				else
					first += stagingRecords.size();

				// Perform individual Adjustments
				for (Stage stagingRecord : stagingRecords)
				{
					lineNumber = stagingRecord.getLineNo();
					adjust(em, state, rootAgent, rootAccount, stagingRecord);
					adjustmentCount++;
				}
			}

			// Persist the Root Account
			rootAccount.setLastUserID(state.getBatch().getLastUserID());
			rootAccount.setLastTime(state.getBatch().getLastTime());
			em.persist(rootAccount);

			state.getBatch().setUpdateCount(adjustmentCount);

		}
		catch (Throwable tr)
		{
			logger.error("Unable to complete adjustment", tr);
			state.addIssue(BatchIssue.CANNOT_ADJUST, null, lineNumber, null, tr.getMessage());
			return false;
		}

		return true;
	}

	// Adjust each Record
	private void adjust(EntityManager em, State batchState, Agent rootAgent, Account rootAccount, Stage stagingRecord) throws RuleCheckException, IOException
	{
		// Create Transaction
		Session session = batchState.getSession();
		Transaction transaction = new Transaction() //
				.setReturnCode(ResponseHeader.RETURN_CODE_SUCCESS) //
				.setRolledBack(false) //
				.setFollowUp(false) //
				.setStartTime(new Date()) //
				.setChannel(Session.CHANNEL_BATCH) //
				.setHostname(computerName) //
				.setInboundTransactionID(null) //
				.setInboundSessionID(null) //
				.setRequestMode(RequestHeader.MODE_NORMAL) //
				.setType(Transaction.TYPE_ADJUST) //
				.setRequesterMSISDN(batchState.getSession().getMobileNumber()) //
				.setRequesterType(TransactionState.getRequesterType(batchState.getSession())) //
				.setCallerID(session.getDomainAccountName()) //
				.setCompanyID(session.getCompanyID());

		// Create State
		TransactionState<AdjustmentRequest, AdjustmentResponse> state = new TransactionState<AdjustmentRequest, AdjustmentResponse>();
		state.setTransaction(transaction);
		state.setSession(session);

		// Set the A-Agent (Root)
		state.setAgentA(rootAgent);
		state.setBeforeA(rootAccount);

		// Get the B-Agent
		Agent bAgent = Agent.findByID(em, stagingRecord.getEntityID(), batchState.getCompanyID());
		state.setAgentB(bAgent);

		// Get the Agent Account
		Account bAccount = Account.findByAgentID(em, bAgent.getId(), true);
		state.setBeforeB(bAccount);

		// Get Currency Decimal Digits
		int currencyDecimalDigits = context.getMoneyScale();

		// Calculate the Bonus and Bonus Provision to be transferred
		BigDecimal amount = stagingRecord.getBd1();
		BigDecimal bonusPercentage = BigDecimal.ZERO;
		BigDecimal bonusAmount = BigDecimal.ZERO;
		BigDecimal bonusProvisionPercentage = bAgent.getTier().getDownStreamPercentage();
		BigDecimal bonusProvision = //
				bAccount.getBalance() //
						.add(amount) //
						.add(bonusAmount) //
						.multiply(bonusProvisionPercentage) //
						.setScale(currencyDecimalDigits, RoundingMode.CEILING) //
						.subtract(bAccount.getBonusBalance());

		// Adjust
		bAccount.adjust(amount, bonusProvision, false);
		transaction.setAmount(amount) //
				.setBuyerTradeBonusAmount(bonusAmount) //
				.setBuyerTradeBonusProvision(bonusProvision) //
				.setBuyerTradeBonusPercentage(bonusPercentage);

		// Set the Reason
		transaction.setAdditionalInformation(stagingRecord.getDescription());

		// Update the Root Balance
		rootAccount.adjust(amount.negate(), bonusProvision.negate(), true);

		// Update Transaction
		state.setAfterA(rootAccount);
		state.setAfterB(bAccount);

		// Persist updated accounts
		bAccount.persist(em, null, state.getSession(), null);

		// Persist the Transaction
		CompanyInfo companyInfo = context.findCompanyInfoByID(em, session.getCompanyID());
		transaction //
				.setNumber(stagingRecord.getAltPhoneNumber()) //
				.setEndTime(new Date()) //
				.setLastTime(transaction.getEndTime());
		transaction.persist(em, null, session, null);

		// Update Batch Totals
		Batch batch = batchState.getBatch();
		BigDecimal total = batch.getTotalValue();
		total = total == null ? transaction.getAmount() : total.add(transaction.getAmount());
		batch.setTotalValue(total);
		total = batch.getTotalValue2();
		total = total == null ? transaction.getBuyerTradeBonusProvision() : total.add(transaction.getBuyerTradeBonusProvision());
		context.writeTDR(transaction);
		batch.setTotalValue2(total);

	}

	// Send Notifications
	@Override
	public void sendNotifications(EntityManager em, final State state)
	{
		// Get Batch Config
		AdjustmentsConfig config = context.findCompanyInfoByID(state.getCompanyID()).getConfiguration(em, AdjustmentsConfig.class);
		Phrase agentNotification = config.getAgentNotification();

		// String Expander
		StringExpander<PropertyBag> expander = new StringExpander<PropertyBag>()
		{
			@Override
			protected String expandField(String englishName, Locale locale, PropertyBag bag)
			{
				NumberFormat numberFormat = context.getCurrencyFormat(locale);

				switch (englishName)
				{
					case AdjustmentsConfig.WEB_USER:
						return state.getSession().getDomainAccountName();

					case AdjustmentsConfig.AGENT_MSIDN:
						return bag.agent.getMobileNumber();

					case AdjustmentsConfig.NEW_BALANCE:
						return numberFormat.format(bag.account.getBalance());

					case AdjustmentsConfig.NEW_BONUS_BALANCE:
						return numberFormat.format(bag.account.getBonusBalance());

					case AdjustmentsConfig.NEW_TOTAL_BALANCE:
						return numberFormat.format(bag.account.getBalance().add(bag.account.getBonusBalance()));

					case TransactionsConfig.TRANSACTION_NO:
						return bag.transactionNumber;

					case AdjustmentsConfig.FILE_NAME:
						return bag.batchFilename;

					case AdjustmentsConfig.RECORD_COUNT:
						return Integer.toString(bag.batchRecordCount);

					case AdjustmentsConfig.BATCH_TIME:
						DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
						return df.format(bag.batchTime);

					case AdjustmentsConfig.TOTAL_ADJUST_AMOUNT:
						return numberFormat.format(bag.totalAmount);

					case AdjustmentsConfig.TOTAL_ADJUST_AMOUNT_WITH_PROVISIONS:
						return numberFormat.format(bag.totalAmountWithProvisions);

					default:
						return "";
				}
			}

		};

		// For groups of adjustments
		int first = 0;
		int max = 50;
		while (true)
		{
			// Get the next batch
			List<Stage> stagingRecords = Stage.findRecords(em, state.getCompanyID(), state.getBatch().getId(), ET_ACCOUNT, Stage.ACTION_UPDATE, first, max);
			if (stagingRecords.isEmpty())
				break;
			else
				first += stagingRecords.size();

			// Send SMS
			for (Stage stagingRecord : stagingRecords)
			{
				PropertyBag bag = new PropertyBag();
				bag.agent = Agent.findByID(em, stagingRecord.getEntityID(), state.getCompanyID());
				bag.account = Account.findByAgentID(em, stagingRecord.getEntityID(), false);
				bag.transactionNumber = stagingRecord.getAltPhoneNumber();
				Locale locale = state.getSession().getLocale(bag.agent.getLanguage());
				String notification = expander.expandNotification(agentNotification, locale, config.listNotificationFields(), bag);
				context.sendSMS(bag.agent.getMobileNumber(), locale.getISO3Language(), notification);
			}
		}

		// Send Summary SMS to Caller
		Phrase batchNotification = config.getBatchNotification();
		PropertyBag bag = new PropertyBag();
		Batch batch = state.getBatch();
		bag.batchFilename = batch.getFilename();
		bag.batchRecordCount = batch.getLineCount() - 1;
		bag.batchTime = batch.getTimestamp();
		bag.totalAmount = batch.getTotalValue();
		bag.totalAmountWithProvisions = bag.totalAmount.add(batch.getTotalValue2());
		Locale locale = state.getSession().getLocale();
		String notification = expander.expandNotification(batchNotification, locale, config.listBatchNotificationFields(), bag);
		context.sendSMS(state.getSession().getMobileNumber(), locale.getISO3Language(), notification);

		// Send Summary SMS to Co-Auth
		Integer coAuthWebUserID = batch.getCoAuthWebUserID();
		if (coAuthWebUserID != null)
		{
			WebUser coAuthWebUser = WebUser.findByID(em, coAuthWebUserID, state.getCompanyID());
			if (coAuthWebUser != null)
			{
				locale = state.getSession().getLocale(coAuthWebUser.getLanguage());
				context.sendSMS(coAuthWebUser.getMobileNumber(), locale.getISO3Language(), notification);
			}
		}
	}

	private class PropertyBag
	{
		public String transactionNumber;
		public Agent agent;
		public Account account;

		public String batchFilename;
		public int batchRecordCount;
		public Date batchTime;
		public BigDecimal totalAmount;
		public BigDecimal totalAmountWithProvisions;
	}

}
