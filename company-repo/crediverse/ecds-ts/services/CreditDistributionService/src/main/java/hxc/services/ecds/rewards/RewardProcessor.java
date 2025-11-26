package hxc.services.ecds.rewards;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.AdjustmentRequest;
import hxc.ecds.protocol.rest.AdjustmentResponse;
import hxc.ecds.protocol.rest.RequestHeader;
import hxc.ecds.protocol.rest.ResponseHeader;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.RewardsConfig;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.Company;
import hxc.services.ecds.model.Promotion;
import hxc.services.ecds.model.QualifyingTransaction;
import hxc.services.ecds.model.SmsQueue;
import hxc.services.ecds.model.State;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.rest.TransactionState;
import hxc.services.ecds.rest.Transactions;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StringExpander;
import hxc.utils.calendar.DateTime;

public class RewardProcessor implements Runnable
{
	final static Logger logger = LoggerFactory.getLogger(RewardProcessor.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String LAST_REWARD_ID = "LAST_REWARD_ID";
	private static final int BATCH_SIZE = 1000;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ICreditDistribution context;
	private CompanyInfo company;
	private ScheduledFuture<?> future;
	private ScheduledThreadPoolExecutor scheduledThreadPool;
	private DateTime next = DateTime.getNow();
	private ReentrantLock lock = new ReentrantLock();

	private static final int SHORTEST_INTERVAL = 2; // Minutes

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public RewardProcessor(ICreditDistribution context, CompanyInfo company)
	{
		this.context = context;
		this.company = company;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public void start(ScheduledThreadPoolExecutor scheduledThreadPool)
	{
		this.scheduledThreadPool = scheduledThreadPool;

		future = scheduledThreadPool.scheduleAtFixedRate(this, 1, SHORTEST_INTERVAL, TimeUnit.MINUTES);
	}

	public void stop()
	{
		if (future != null)
		{
			future.cancel(true);
			future = null;
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Runnable
	//
	// /////////////////////////////////
	@Override
	public void run()
	{
		scheduleNextRun();
		sendDeferredSms();
	}

	public void scheduleNextRun()
	{
		// Get Time/Date info
		DateTime now = next;
		next = DateTime.getNow().addMinutes(SHORTEST_INTERVAL);
		DateTime today = now.getDatePart();
		DateTime tomorrow = today.addDays(1);
		int companyID = company.getCompany().getId();
		// Get Config
		try (EntityManagerEx em = context.getEntityManager())
		{
			RewardsConfig config = company.getConfiguration(em, RewardsConfig.class);
			if(!config.isEnableRewardProcessing())
			{
				logger.trace("Rewards: Reward processing is disabled for company {} therefore skipping.", companyID);
				return;
			}
			// Calculate the time to run next
			int minutes = config.getRewardProcessingIntervalMinutes();
			
			if (minutes > 0 && minutes < SHORTEST_INTERVAL)
				minutes = SHORTEST_INTERVAL;
			Date startTimeOfDay = config.getRewardProcessingStartTimeOfDay();
			if (startTimeOfDay == null)
				startTimeOfDay = new DateTime(2000, 1, 1, 0, 0, 0);
			long startSecondsPastMidnight = new DateTime(startTimeOfDay).getSecondsSinceMidnight();
			DateTime scheduled = today.addSeconds(startSecondsPastMidnight);
			while (scheduled.before(now))
			{
				scheduled = minutes <= 0 ? tomorrow : scheduled.addMinutes(minutes);
			}

			// New Day
			if (!scheduled.before(tomorrow))
				scheduled = tomorrow.addSeconds(startSecondsPastMidnight);

			// Exit if not before next poll
			if (!scheduled.before(next))
				return;

			// Exit if not incumbent
			if (!context.isMasterServer())
			{
				logger.trace("Not performing Reward Processing - Not Master Server. companyID {}", companyID);
				return;
			}

			// Schedule the next run
			long delay_ms = scheduled.getTime() - new Date().getTime();
			scheduledThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{
					context.assignTsNumber(true);
					tryEvaluateRewards();
				}

			}, delay_ms <= 1 ? 1 : delay_ms, TimeUnit.MILLISECONDS);

		}
		catch (Throwable ex)
		{
			logger.error("Unable to schedule next run.", ex);
		}
	}

	public void sendDeferredSms()
	{
		// Only on 'Master' server
		if (!context.isMasterServer())
			return;

		// Process Deferred SMSes
		DateTime now = DateTime.getNow();
		int secondOfDay = now.getSecondsSinceMidnight();
		try (EntityManagerEx em = context.getEntityManager())
		{
			int companyID = company.getCompany().getId();
			while (true)
			{
				// Get next Batch
				List<SmsQueue> entries = SmsQueue.findUnsent(em, secondOfDay, companyID, BATCH_SIZE);
				if (entries == null || entries.size() == 0)
					break;
				logger.info("Sending next {} deferred SMS(es) for Company {}", entries.size(), companyID);

				// For each deferred SMS
				for (SmsQueue entry : entries)
				{
					boolean expired = !entry.getExpiryTime().after(now);

					try (RequiresTransaction ts = new RequiresTransaction(em))
					{
						em.remove(entry);
						ts.commit();
					}

					// Send SMS
					if (!expired)
						context.sendSMS(entry.getMobileNumber(), entry.getLanguage3(), entry.getNotification());
				}

			}
		}
		catch (Throwable tr)
		{
			logger.error("sendDeferredSms", tr);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Evaluate Rewards
	//
	// /////////////////////////////////
	public void tryEvaluateRewards()
	{
		try
		{
			if (!lock.tryLock(1, TimeUnit.SECONDS))
			{
				logger.trace("Reward Processing already in progress");
				return;
			}
			evaluateRewards();
		}
		catch (InterruptedException e)
		{
			logger.info("Evaluate rewards was interupted", e);
		}
		finally
		{
			lock.unlock();
		}
	}

	void evaluateRewards()
	{
		int companyID = company.getCompany().getId();
		logger.info("Starting Reward processing for Company {}", companyID);
		DateTime cutOff = DateTime.getToday().addDays(-QualifyingTransaction.MAX_DAYS_TO_LIVE);

		// Get Database
		try (EntityManagerEx em = context.getEntityManager())
		{
			// Obtain a list of active promotions
			logger.trace("Rewards: Get All active promotions for Company {}", companyID);
			List<Promotion> promotions = Promotion.findAllActive(em, companyID);
			if(!promotions.isEmpty())
			{
				// Copy Qualifying Transactions from Transaction table for Evaluation
				logger.trace("Rewards: Identify qualifying transactions for Company {}", companyID);
				copyQualifyingTransactionsForEvaluation(em, companyID, promotions);
	
				// Process Agents
				logger.trace("Rewards: Process Agent Rewards for Company {}", companyID);
				RewardsConfig config = company.getConfiguration(em, RewardsConfig.class);
				processAgents(em, promotions, config, cutOff, company.getCompany());
	
				// Cleanup stale/spent candidates
				logger.trace("Rewards: Cleanup stale/spent qualifying transactions for Company {}", companyID);
				cleanupStaleAndSpentTransactions(em, cutOff, companyID);
				logger.info("Completed Reward processing for Company {}", companyID);
			} else {
				try (RequiresTransaction ts = new RequiresTransaction(em))
				{
					updateLastRewardID(em, companyID);
					ts.commit();
				}
			}
		}
		catch (Throwable tr)
		{
			logger.info("evaluateRewards", tr);
			return;
		}
	}

	//Update the ec_state LAST_REWARD_ID value to avoid a backlog of old transactions building up that will be processed when the reward processor is re-enabled or if a new reward is created.
	public static long updateLastRewardID(EntityManager em, int companyID)
	{
		long id = 0L;
		String no = "";
		State lastRewardState = State.findByName(em, LAST_REWARD_ID, companyID);
		List<Transaction> list = Transaction.findLast(em, 1, companyID);
		if(list != null && !list.isEmpty())
		{
			Transaction latest = list.get(0);
			if (lastRewardState == null)
			{
				lastRewardState = new State()
						.setCompanyID(companyID)
						.setName(LAST_REWARD_ID)
						.setLastTime(new Date())
						.setLastUserID(0);
			}
			id = latest.getId();
			no = latest.getNumber();
			lastRewardState.setValue(id);
			em.persist(lastRewardState);
			logger.info("Rewards: Updated {} to transaction ID: {} [transaction no: {}] for company {}", RewardProcessor.LAST_REWARD_ID, id, no, companyID);
			State currentRewardState = State.findByName(em, LAST_REWARD_ID, companyID);
			logger.info("Rewards: {} value {}", RewardProcessor.LAST_REWARD_ID, currentRewardState.getValue());
		}
		return id;
	}

	// Copy Qualifying Transactions from Transaction table for Evaluation
	public void copyQualifyingTransactionsForEvaluation(EntityManager em, int companyID, List<Promotion> promotions)
	{
		// Find last transactionID which was copied for evaluation
		State lastRewardState = State.findByName(em, LAST_REWARD_ID, companyID);
		if (lastRewardState == null)
		{
			lastRewardState = new State() //
					.setCompanyID(companyID) //
					.setName(LAST_REWARD_ID) //
					.setValue(0L) //
					.setLastTime(new Date()) //
					.setLastUserID(0);
		}
		if (lastRewardState.getValue() == null)
			lastRewardState.setValue(0L);

		// Process Batches of Sale, Bundle Sale and Transfer Transactions
		while (true)
		{
			List<Transaction> candidates = QualifyingTransaction.findCandidates(em, (long) lastRewardState.getValue(), companyID, BATCH_SIZE);
			if (candidates.size() == 0)
				break;
			logger.trace("Assessing the next {} transaction(s) for promotional rewards for Company {}", candidates.size(), companyID);

			try (RequiresTransaction ts = new RequiresTransaction(em))
			{
				// Create QualifyingTransactions
				for (Transaction candidate : candidates)
				{
					// Test if candidate is consistent with any promotion
					if (!RewardAssessor.consistentWithAny(promotions, candidate, false))
						continue;

					// Create Qualifying Transaction
					QualifyingTransaction qualifyingTransaction = new QualifyingTransaction() //
							.setId(candidate.getId()) //
							.setCompanyID(candidate.getCompanyID()) //
							.setStartTime(candidate.getStartTime()) //
							.setAgentID(candidate.getA_AgentID()) //
							.setTransferRuleID(candidate.getTransferRuleID()) //
							.setCellID(candidate.getA_CellID()) //
							.setCell(candidate.getA_Cell()) //
							.setServiceClassID(candidate.getA_ServiceClassID()) //
							.setBundleID(candidate.getBundleID()) //
							.setEvaluated(false) //
							.setAmountLeft(candidate.getAmount()) //
							.setLastUserID(candidate.getLastUserID()) //
							.setBlocked(candidate.isFollowUp()) //
							.setLastTime(candidate.getLastTime());
					em.persist(qualifyingTransaction);
				}

				// Update the Last seen Transaction
				long lastID = candidates.get(candidates.size() - 1).getId();
				lastRewardState.setValue(lastID);
				em.persist(lastRewardState);

				ts.commit();
			}

		}
	}

	// Process Qualifying Transactions for Agents
	public void processAgents(EntityManager em, final List<Promotion> promotions, final RewardsConfig config, final Date cutOff, final Company company)
	{
		int fromAgentID = 0;
		while (true)
		{
			// Get the next batch of Agents
			List<Integer> agentIDs = QualifyingTransaction.findUnEvaluatedAgents(em, fromAgentID, company.getId(), BATCH_SIZE);
			if (agentIDs.size() == 0)
				break;
			fromAgentID = agentIDs.get(agentIDs.size() - 1);
			logger.trace("Assessing the next {} agent(s) for promotional rewards for Company {}", agentIDs.size(), company.getId());

			// Process each agent

			for (Integer agentID : agentIDs)
			{
				processAgent(agentID, promotions, config, cutOff, company);
			}
		}
	}

	// Process Qualifying Transactions for an Agent
	private void processAgent(int agentID, List<Promotion> promotions, RewardsConfig config, Date cutOff, Company company)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{

			// Get List of potentially qualifying transactions
			int companyID = company.getId();
			List<QualifyingTransaction> candidates = QualifyingTransaction.findForAgent(em, agentID, companyID);

			// Remove un-adjudicated transactions
			int index = 0;
			while (index < candidates.size())
			{
				QualifyingTransaction candidate = candidates.get(index);
				if (candidate.isBlocked())
				{
					Transaction adjudication = Transaction.findByReversedID(em, candidate.getId(), companyID);
					if (adjudication == null || adjudication.isRolledBack())
						candidates.remove(index);
					else
					{
						candidate.setBlocked(false);
						index++;
					}
				}
				else
				{
					index++;
				}
			}

			// Get List of Recent Rewards
			List<Transaction> recentRewards = Transaction.findRecentRewards(em, agentID, cutOff, companyID);

			// Test against all promotions
			while (true)
			{
				// Test if any promotion triggers
				RewardAssessor assessor = testAgent(promotions, candidates, recentRewards);
				if (assessor == null)
					break;

				// Create Transaction
				Transaction reward = null;
				try (RequiresTransaction ts = new RequiresTransaction(em))
				{
					// Create Transaction
					Promotion promotion = Promotion.findByID(em, assessor.getPromotion().getId(), companyID);
					reward = createRewardTransaction(em, agentID, promotion, companyID);

					// Update all changed Qualifying Transactions
					for (QualifyingTransaction transaction : assessor.getTransactions())
					{
						transaction.setEvaluated(true);
						em.persist(transaction);
					}

					for (QualifyingTransaction transaction : candidates)
					{
						if (!transaction.isEvaluated())
						{
							transaction.setEvaluated(true);
							em.persist(transaction);
						}
					}

					// Commit
					ts.commit();

				}
				catch (Throwable tr)
				{
					logger.error("transaction error", tr);
					return;
				}

				// Write TDR
				try
				{
					if (reward != null)
						context.writeTDR(reward);
				}
				catch (IOException e)
				{
					logger.error("Error writing CDR for reward", e);
				}

				recentRewards.add(reward);

				// Notify
				if (reward != null)
				{
					Phrase agentNotification = config.getAgentNotification();
					StringExpander<Transaction> expander = new StringExpander<Transaction>()
					{
						@Override
						protected String expandField(String englishName, Locale locale, Transaction reward)
						{
							NumberFormat numberFormat = context.getCurrencyFormat(locale);

							switch (englishName)
							{
								case RewardsConfig.REWARD_AMOUNT:
									return numberFormat.format(reward.getAmount());

								case RewardsConfig.NEW_BALANCE:
									return numberFormat.format(reward.getB_BalanceAfter());

								case RewardsConfig.PROMOTION_NAME:
									return reward.getPromotion().getName();

								case TransactionsConfig.TRANSACTION_NO:
									return reward.getNumber();

								default:
									return "";
							}
						}

					};

					Agent agent = reward.getB_Agent();
					Locale locale = new Locale(agent == null ? Phrase.ENG : agent.getLanguage(), company.getCountry());
					String notification = expander.expandNotification(agentNotification, locale, config.listAgentNotificationFields(), reward);

					SmsQueue.sendSMS(em, reward.getB_MSISDN(), locale.getISO3Language(), notification, //
							context, config.getSmsStartTimeOfDay(), config.getSmsEndTimeOfDay(), companyID, agentID);

				}
			}

		}
		catch (Throwable tr)
		{
			logger.error("processAgent", tr);
		}

	}

	public RewardAssessor testAgent(List<Promotion> promotions, List<QualifyingTransaction> candidates, List<Transaction> recentRewards)
	{
		Map<Integer, RewardAssessor> assessors;

		assessors = new HashMap<Integer, RewardAssessor>();

		// Evaluate all Qualifying transactions
		for (QualifyingTransaction candidate : candidates)
		{
			// against all Active Promotions
			for (Promotion promotion : promotions)
			{
				// Continue if this candidate is not consistent with promotion
				if (!RewardAssessor.consistentWith(promotion, candidate, false))
					continue;

				// Get Assessor
				RewardAssessor assessor = assessors.get(promotion.getId());
				if (assessor == null)
				{
					assessor = new RewardAssessor(promotion, recentRewards);
					assessors.put(promotion.getId(), assessor);
				}

				// Test if adding this transaction triggers a reward
				if (assessor.add(candidate))
				{
					return assessor;
				}

			}
		}

		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	private Transaction createRewardTransaction(EntityManager em, int agentID, Promotion promotion, int companyID) throws RuleCheckException
	{
		// Get Root Account
		Agent rootAgent = Agent.findRoot(em, companyID);
		Account rootAccount = Account.findByAgentID(em, rootAgent.getId(), true);

		// Create Transaction
		Transaction transaction = new Transaction() //
				.setReturnCode(ResponseHeader.RETURN_CODE_SUCCESS) //
				.setRolledBack(false) //
				.setFollowUp(false) //
				.setStartTime(new Date()) //
				.setChannel(Session.CHANNEL_BATCH) //
				.setHostname(Transactions.getHostName()) //
				.setInboundTransactionID(null) //
				.setInboundSessionID(null) //
				.setRequestMode(RequestHeader.MODE_NORMAL) //
				.setType(Transaction.TYPE_PROMOTION_REWARD) //
				.setRequesterMSISDN(rootAgent.getMobileNumber()) //
				.setRequesterType(Transaction.REQUESTER_TYPE_AGENT) //
				.setCallerID(rootAgent.getMobileNumber()) //
				.setPromotion(promotion) //
				.setPromotionID(promotion.getId()) //
				.setCompanyID(companyID);

		// Create State
		TransactionState<AdjustmentRequest, AdjustmentResponse> state = new TransactionState<AdjustmentRequest, AdjustmentResponse>();
		state.setTransaction(transaction);

		// Set the A-Agent (Root)
		state.setAgentA(rootAgent);
		state.setBeforeA(rootAccount);

		// Get the B-Agent
		Agent bAgent = Agent.findByID(em, agentID, companyID);
		state.setAgentB(bAgent);

		// Get the Agent Account
		Account bAccount = Account.findByAgentID(em, bAgent.getId(), true);
		state.setBeforeB(bAccount);

		// Get Currency Decimal Digits
		int currencyDecimalDigits = context.getMoneyScale();

		// Calculate the Reward to be given
		BigDecimal amount = promotion.getTargetAmount() //
				.multiply(promotion.getRewardPercentage()) //
				.add(promotion.getRewardAmount()) //
				.setScale(currencyDecimalDigits, RoundingMode.CEILING);

		// Calculate the Bonus and Bonus Provision to be transferred
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
		transaction.setAmount(amount) //
				.setBuyerTradeBonusAmount(bonusAmount) //
				.setBuyerTradeBonusProvision(bonusProvision) //
				.setBuyerTradeBonusPercentage(bonusPercentage);
		bAccount.adjust(amount, bonusProvision, false);

		// Update the Root Balance
		rootAccount.adjust(amount.negate(), bonusProvision.negate(), true);

		// Update Transaction
		state.setAfterA(rootAccount);
		state.setAfterB(bAccount);

		// Persist updated accounts
		bAccount.validate();
		em.persist(rootAccount);
		em.persist(bAccount);

		// Persist the Transaction
		CompanyInfo companyInfo = context.findCompanyInfoByID(em, companyID);
		String number = companyInfo.getNextTransactionNumber(companyID);
		transaction //
				.setNumber(number) //
				.setEndTime(new Date()) //
				.setLastTime(transaction.getEndTime());
		transaction.validate(null);
		em.persist(transaction);

		return transaction;
	}

	// Cleanup stale/spent candidates
	private void cleanupStaleAndSpentTransactions(EntityManager em, Date before, int companyID)
	{
		try (RequiresTransaction ts = new RequiresTransaction(em))
		{
			QualifyingTransaction.cleanout(em, before, companyID);
			ts.commit();
		}

	}

}
