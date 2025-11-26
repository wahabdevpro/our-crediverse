package cs.dto;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hxc.ecds.protocol.rest.DedicatedAccountInfo;
import hxc.ecds.protocol.rest.DedicatedAccountRefillInfo;
import hxc.ecds.protocol.rest.DedicatedAccountReverseInfo;
import org.springframework.beans.BeanUtils;

import hxc.ecds.protocol.rest.ResponseHeader;
import hxc.ecds.protocol.rest.Transaction;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiTransaction extends Transaction
{
	// John Note: Is there any actual benefit to this?
	public enum TransactionType
	{
		REPLENISH(Transaction.TYPE_REPLENISH),
		TRANSFER(Transaction.TYPE_TRANSFER),
		SELL(Transaction.TYPE_SELL),
		NON_AIRTIME_DEBIT(Transaction.TYPE_NON_AIRTIME_DEBIT),
		NON_AIRTIME_REFUND(Transaction.TYPE_NON_AIRTIME_REFUND),
		REGISTER_PIN(Transaction.TYPE_REGISTER_PIN),
		CHANGE_PIN(Transaction.TYPE_CHANGE_PIN),
		BALANCE_ENQUIRY(Transaction.TYPE_BALANCE_ENQUIRY),
		SELF_TOPUP(Transaction.TYPE_SELF_TOPUP),
		TRANSACTION_STATUS_ENQUIRY(Transaction.TYPE_TRANSACTION_STATUS_ENQUIRY),
		LAST_TRANSACTION_ENQUIRY(Transaction.TYPE_LAST_TRANSACTION_ENQUIRY),
		ADJUST(Transaction.TYPE_ADJUST),
		SALES_QUERY(Transaction.TYPE_SALES_QUERY),
		DEPOSITS_QUERY(Transaction.TYPE_DEPOSITS_QUERY),
		REVERSE(Transaction.TYPE_REVERSE),
		REVERSE_PARTIALLY(Transaction.TYPE_REVERSE_PARTIALLY),
		PROMOTION_REWARD(Transaction.TYPE_PROMOTION_REWARD),
		ADJUDICATE(Transaction.TYPE_ADJUDICATE);

		private String code;
		private static final Map<String,TransactionType> enumMap;

		TransactionType(String code)
		{
			this.code = code;
		}

		static
		{
			Map<String, TransactionType> map = new HashMap<>();
			for (TransactionType tt : TransactionType.values())
				  map.put(tt.code, tt);

			enumMap = Collections.unmodifiableMap(map);
		}

		public static String getTranslatableTransactionType(String tsTranactionType) {
			TransactionType type = enumMap.get(tsTranactionType);
			return (type!=null)? type.toString() : tsTranactionType;
		}
	}

	protected String transactionTypeName;
	protected String channelName;
	protected String startTimeString;
	protected String endTimeString;
	protected String requestModeName;
	protected String aPartyName;
	protected String aTierName;
	protected String aTierType;
	protected String aGroupName;
	protected String aAreaName;
	protected String aAreaType;
	protected String aOwnerName;
	protected String bPartyName;
	protected String bTierName;
	protected String bTierType;
	protected String bGroupName;
	protected String bAreaName;
	protected String bAreaType;
	protected String bOwnerName;
	protected String buyerTradeBonusPercentageString;
	protected List<GuiTransaction> reversals;
	protected GuiTransaction adjudication;
	protected Boolean adjudicated;

	protected String bundleName;
	protected String promotionName;
	protected String itemDescription;

	protected String aCgi;
	protected String bCgi;
	
	protected String aCellGroupCode;
	protected String bCellGroupCode;

	protected String transferRuleName;
	protected String aServiceClassName;
	protected String bServiceClassName;
	protected Boolean reversable;
	protected String workflowState;
	protected List<DedicatedAccountRefillInfo> dedicatedAccountInfo;
	protected List<DedicatedAccountReverseInfo> dedicatedAccountReverseInfo;
	protected BigDecimal mainAccountCurrentBalance;
	protected List<DedicatedAccountInfo> dedicatedAccountCurrentBalanceInfo;
	protected Boolean DABonusReversalEnabled;


	public GuiTransaction()
	{
		super();
	}

	public void addReversal(GuiTransaction transaction)
	{
		if (reversals == null) reversals = new ArrayList<GuiTransaction>();
		reversals.add(transaction);
	}

	public GuiTransaction(Transaction orig)
	{
		BeanUtils.copyProperties(orig, this);

		switch (this.channel)
		{
			case "U": this.channelName = "USSD"; break;
			case "S": this.channelName = "SMS"; break;
			case "A": this.channelName = "API"; break;
			case "P": this.channelName = "Mobile App"; break;
			case "W": this.channelName = "Web UI"; break;
			default: this.channelName = this.channel; break;
		}

		this.transactionTypeName = TransactionType.getTranslatableTransactionType(this.type);
//		switch (this.type)
//		{
//		    case Transaction.TYPE_REPLENISH: this.transactionTypeName = "Replenish"; break;
//	    	case Transaction.TYPE_TRANSFER: this.transactionTypeName = "Transfer"; break;
//	    	case Transaction.TYPE_SELL: this.transactionTypeName = "Sell"; break;
//	    	case Transaction.TYPE_REGISTER_PIN: this.transactionTypeName = "Register PIN"; break;
//	    	case Transaction.TYPE_CHANGE_PIN: this.transactionTypeName = "Change PIN"; break;
//	    	case Transaction.TYPE_BALANCE_ENQUIRY: this.transactionTypeName = "Balance Enquiry"; break;
//	    	case Transaction.TYPE_SELF_TOPUP: this.transactionTypeName = "Self Top-up"; break;
//	    	case Transaction.TYPE_TRANSACTION_STATUS_ENQUIRY: this.transactionTypeName = "Transaction Status Enquiry"; break;
//	    	case Transaction.TYPE_LAST_TRANSACTION_ENQUIRY: this.transactionTypeName = "Last Transaction Enquiry"; break;
//	    	case Transaction.TYPE_ADJUST: this.transactionTypeName = "Adjustment"; break;
//	    	case Transaction.TYPE_SALES_QUERY: this.transactionTypeName = "Sales Query"; break;
//	    	case Transaction.TYPE_DEPOSITS_QUERY: this.transactionTypeName = "Deposits Query"; break;
//			case Transaction.TYPE_REVERSE: this.transactionTypeName = "Reversal"; break;
//			case Transaction.TYPE_REVERSE_PARTIALLY: this.transactionTypeName = "Reversal (Partial)"; break;
//			default: this.transactionTypeName = this.type; break;
//		}

		SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
		if (this.startTime != null)
			this.startTimeString = ft.format(this.startTime);
		if (this.endTime != null)
			this.endTimeString = ft.format(this.endTime);

		this.setReversable(true);
		if(orig.getReturnCode() != null )
		{
			boolean reversable = !((!orig.getReturnCode().equals(ResponseHeader.RETURN_CODE_SUCCESS) || (orig.getReturnCode().equals(ResponseHeader.RETURN_CODE_SUCCESS) && orig.isFollowUp())));
			this.setReversable(reversable);
		}
	}

	public Transaction getTransaction()
	{
		Transaction transaction = new Transaction();
		BeanUtils.copyProperties(this, transaction);
		return transaction;
	}
}
