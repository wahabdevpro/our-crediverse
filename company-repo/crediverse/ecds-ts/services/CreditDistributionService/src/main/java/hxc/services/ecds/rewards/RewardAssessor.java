package hxc.services.ecds.rewards;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hxc.services.ecds.model.Area;
import hxc.services.ecds.model.Cell;
import hxc.services.ecds.model.Promotion;
import hxc.services.ecds.model.QualifyingTransaction;
import hxc.services.ecds.model.Transaction;
import hxc.utils.calendar.DateTime;

public class RewardAssessor
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Promotion promotion;
	private BigDecimal required;
	private List<QualifyingTransaction> transactions;
	private List<Transaction> recentRewards;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public Promotion getPromotion()
	{
		return promotion;
	}

	public List<QualifyingTransaction> getTransactions()
	{
		return transactions;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////
	public RewardAssessor(Promotion promotion, List<Transaction> recentRewards)
	{
		this.promotion = promotion;
		this.required = promotion.getTargetAmount();
		this.transactions = new ArrayList<QualifyingTransaction>();
		this.recentRewards = recentRewards;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public boolean add(QualifyingTransaction transaction)
	{
		// Exit if depleted
		if (transaction.getAmountLeft().signum() <= 0)
			return false;
		
		// Exit if blocked
		if (transaction.isBlocked())
			return false;

		// Add transaction to list and decrement the amount required
		transactions.add(transaction);
		required = required.subtract(transaction.getAmountLeft());

		// Remove Old Transactions
		Date cutOffTime = getCutOffTime(promotion.getTargetPeriod(), transaction.getStartTime());
		while (transactions.size() > 0)
		{
			QualifyingTransaction head = transactions.get(0);
			if (head.getStartTime().after(cutOffTime))
				break;
			required = required.add(head.getAmountLeft());
			transactions.remove(head);
		}

		// Return if the threshold has not been reached or exceeded
		if (required.signum() > 0)
			return false;

		// Return if a reward was given recently
		if (!promotion.isRetriggerable())
		{
			for (Transaction recent : recentRewards)
			{
				if (recent.getStartTime().after(cutOffTime))
					return false;
			}
		}

		// Decrease the available amounts
		required = promotion.getTargetAmount();
		for (QualifyingTransaction candidate : transactions)
		{
			if (candidate.getAmountLeft().compareTo(required) < 0)
			{
				required = required.subtract(candidate.getAmountLeft());
				candidate.setAmountLeft(BigDecimal.ZERO);
			}
			else
			{
				candidate.setAmountLeft(candidate.getAmountLeft().subtract(required));
				required = BigDecimal.ZERO;
			}
		}

		return true;

	}

	public boolean consistentWith(QualifyingTransaction transaction, boolean excludingArea)
	{
		return consistentWith(promotion, transaction, excludingArea);
	}

	public static boolean consistentWith(Promotion promotion, QualifyingTransaction transaction, boolean excludingArea)
	{
		return consistentWith(promotion, transaction.getStartTime(), transaction.getTransferRuleID(), transaction.getServiceClassID(), transaction.getBundleID(), transaction.getCell(), excludingArea);
	}

	public static boolean consistentWithAny(List<Promotion> promotions, Transaction transaction, boolean excludingArea)
	{
		if (promotions == null)
			return false;
		for (Promotion promotion : promotions)
		{
			if (consistentWith(promotion, transaction, excludingArea))
				return true;
		}
		return false;
	}

	public static boolean consistentWith(Promotion promotion, Transaction transaction, boolean excludingArea)
	{
		// Test if Intra-Tier Transfer
		if (Transaction.TYPE_TRANSFER.equals(transaction.getType()) && transaction.getTransferRuleID() == null)
			return false;

		return consistentWith(promotion, transaction.getStartTime(), transaction.getTransferRuleID(), transaction.getA_ServiceClassID(), transaction.getBundleID(), transaction.getA_Cell(),
				excludingArea);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	private static boolean consistentWith(Promotion promotion, Date startTime, Integer transferRuleID, Integer serviceClassID, Integer bundleID, Cell cell, boolean excludingArea)
	{
		// Test start Time
		if (promotion.getStartTime().after(startTime) || promotion.getEndTime().before(startTime))
			return false;

		// Test Transfer Rule
		if (promotion.getTransferRuleID() != null && !promotion.getTransferRuleID().equals(transferRuleID))
			return false;

		// Test Service Class
		if (promotion.getServiceClassID() != null && !promotion.getServiceClassID().equals(serviceClassID))
			return false;

		// Test Bundle
		if (promotion.getBundleID() != null && !promotion.getBundleID().equals(bundleID))
			return false;

		// Test CellID
		Area area = promotion.getArea();
		if (area == null)
			return true;
		else if (cell == null)
			return excludingArea;

		// Test cellID in area
		if (cell.containedWithin(area))
			return true;

		return false;
	}

	private Date getCutOffTime(int targetPeriod, Date startTime)
	{
		DateTime now = new DateTime(startTime);
		switch (targetPeriod)
		{
			case Promotion.PER_DAY:
				return now.addDays(-1);

			case Promotion.PER_WEEK:
				return now.addDays(-7);

			case Promotion.PER_MONTH:
				return now.addMonths(-1);

			case Promotion.PER_CALENDAR_DAY:
				return now.getDatePart();

			case Promotion.PER_CALENDAR_WEEK:
				return now.getDatePart().addDays(1 - now.getDayOfWeek());

			case Promotion.PER_CALENDAR_MONTH:
				return new DateTime(now.getYear(), now.getMonth(), 1);

			default:
				return startTime;
		}
	}

}
