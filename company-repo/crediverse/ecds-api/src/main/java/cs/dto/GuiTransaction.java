package cs.dto;

import java.math.BigDecimal;
import java.util.Date;

import hxc.ecds.protocol.rest.Transaction;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GuiTransaction {
	private String transactionNo;
	private BigDecimal amount;
	private BigDecimal bonus;
	private String currency;
	private ChannelEnum channel;
	private Date transactionStarted;
	private Date transactionEnded;
	private String a_msisdn;
	private String b_msisdn;
	private BigDecimal balanceBefore;
	private BigDecimal bonusBalanceBefore;
	private BigDecimal balanceAfter;
	private BigDecimal bonusBalanceAfter;
	private Integer bundleId;
	private TransactionTypeEnum type;
	private TransactionStatusEnum status;
	private boolean followUp;
	private boolean rolledBack;
	private String additionalInformation;

	public GuiTransaction(Transaction transaction, String agentMsisdn)
	{
		transactionNo = transaction.getNumber();
		amount = transaction.getAmount();
		bonus = transaction.getBuyerTradeBonusAmount();
		//currency;
		channel = ChannelEnum.fromString(transaction.getChannel());
		transactionStarted = transaction.getStartTime();
		transactionEnded = transaction.getEndTime();
		a_msisdn = transaction.getA_MSISDN();
		b_msisdn = transaction.getB_MSISDN();
		if(transaction.getA_MSISDN().equals(agentMsisdn))
		{
			balanceBefore = transaction.getA_BalanceBefore();
			bonusBalanceBefore = transaction.getA_BonusBalanceBefore();
			balanceAfter = transaction.getA_BalanceAfter();
			bonusBalanceAfter = transaction.getA_BonusBalanceAfter();
		} else if(transaction.getB_MSISDN().equals(agentMsisdn))
		{
			balanceBefore = transaction.getB_BalanceBefore();
			bonusBalanceBefore = transaction.getB_BonusBalanceBefore();
			balanceAfter = transaction.getB_BalanceAfter();
			bonusBalanceAfter = transaction.getB_BonusBalanceAfter();
		}
		bundleId = transaction.getBundleID();
		type = TransactionTypeEnum.fromString(transaction.getType());
		status = TransactionStatusEnum.valueOf(transaction.getReturnCode());
		followUp = transaction.isFollowUp();
		rolledBack = transaction.isRolledBack();
		additionalInformation = transaction.getAdditionalInformation();
	}

	public enum ChannelEnum {
		USSD("U"),
		SMS("S"),
		API("A"),
		SMART_DEVICE("P"),
		WEBUI("W"),
		BATCH("B"),
		INVALID("");
		private String val;
		private ChannelEnum(String val) {
			this.val = val.toUpperCase();
		}

		public String getVal()
		{
			return this.val;
		}

		public static ChannelEnum fromString(String val)
		{
			ChannelEnum result = INVALID;
			if (val != null)
			{
				switch (val)
				{
					case "U":
						result = USSD;
						break;
					case "S":
						result = SMS;
						break;
					case "A":
						result = API;
						break;
					case "P":
						result = SMART_DEVICE;
						break;
					case "W":
						result = WEBUI;
						break;
					case "B":
						result = BATCH;
						break;
				}
			}
			return result;
		}
	}

	public enum TransactionTypeEnum 
	{
		REPLENISH("RP"),
		TRANSFER("TX"),
		SELL("SL"),
		SELL_BUNDLE("SB"),
		REGISTER_PIN("PR"),
		CHANGE_PIN("CP"),
		BALANCE_ENQUIRY("BE"),
		SELF_TOPUP("ST"),
		TRANSACTION_STATUS_ENQUIRY("TS"),
		LAST_TRANSACTION_ENQUIRY("LT"),
		ADJUST("AJ"),
		SALES_QUERY("SQ"),
		DEPOSITS_QUERY("DQ"),
		REVERSE("FR"),
		REVERSE_PARTIALLY("PA"),
		PROMOTION_REWARD("RW"),
		ADJUDICATE("AD"),
		NOTSET("NOTSET");
		private String val;
		private TransactionTypeEnum(String val) {
			this.val = val.toUpperCase();
		}

		public String getVal()
		{
			return this.val;
		}

		public static TransactionTypeEnum fromString(String val)
		{
			TransactionTypeEnum result = NOTSET;
			if (val != null)
			{
				switch (val)
				{
				case "AD":
					result = ADJUDICATE;
					break;
				case "AJ":
					result = ADJUST;
					break;
				case "BE":
					result = BALANCE_ENQUIRY;
					break;
				case "CP":
					result = CHANGE_PIN;
					break;
				case "DQ":
					result = DEPOSITS_QUERY;
					break;
				case "LT":
					result = LAST_TRANSACTION_ENQUIRY;
					break;
				case "RW": 
					result = PROMOTION_REWARD;
					break;
				case "PR":
					result = REGISTER_PIN;
					break;
				case "RP":
					result = REPLENISH;
					break;
				case "FR":
					result = REVERSE;
					break;
				case "PA":
					result = REVERSE_PARTIALLY;
					break;
				case "SQ":
					result = SALES_QUERY;
					break;
				case "ST":
					result = SELF_TOPUP;
					break;
				case "SL":
					result = SELL;
					break;
				case "SB":
					result = SELL_BUNDLE;
					break;
				case "TS":
					result = TRANSACTION_STATUS_ENQUIRY;
					break;
				case "TX":
					result = TRANSFER;
					break;
				}
			}
			return result;
		}
		
		public static boolean contains(String value)
		{
			for (TransactionTypeEnum c : TransactionTypeEnum.values()) {
		        if (c.name().equals(value)) {
		            return true;
		        }
		    }
		    return false;
		}
	}
	
	public enum TransactionStatusEnum
	{
		SUCCESS,
		REFILL_FAILED,
		TECHNICAL_PROBLEM,
		INVALID_CHANNEL,
		FORBIDDEN,
		NO_TRANSFER_RULE,
		NO_LOCATION,
		WRONG_LOCATION,
		CO_AUTHORIZE,
		INSUFFICIENT_FUNDS,
		INSUFFICIENT_PROVISN,
		DAY_COUNT_LIMIT,
		DAY_AMOUNT_LIMIT,
		MONTH_COUNT_LIMIT,
		MONTH_AMOUNT_LIMIT,
		MAX_AMOUNT_LIMIT,
		ALREADY_REGISTERED,
		ALREADY_ADJUDICATED,
		NOT_REGISTERED,
		INVALID_STATE,
		INVALID_PIN,
		INVALID_PASSWORD,
		HISTORIC_PASSWORD,
		NOT_ELIGIBLE,
		PIN_LOCKOUT,
		PASSWORD_LOCKOUT,
		NOT_SELF,
		TX_NOT_FOUND,
		IMSI_LOCKOUT,
		INVALID_AGENT,
		INVALID_AMOUNT,
		INVALID_TRAN_TYPE,
		INVALID_BUNDLE,
		ALREADY_REVERSED,
		NOT_WEBUSER_SESSION,
		CO_SIGN_ONLY_SESSION,
		SESSION_EXPIRED,
		TIMED_OUT,
		INVALID_RECIPIENT,
		OTHER_ERROR,
		REFILL_BARRED,
		TEMPORARY_BLOCKED,
		REFILL_NOT_ACCEPTED,
		REFILL_DENIED,
		NO_IMSI,
		BUNDLE_SALE_FAILED,
		INVALID_VALUE,
		TOO_SMALL,
		TOO_LARGE,
		TOO_LONG,
		TOO_SHORT;
		
		public static boolean contains(String value)
		{
			for (TransactionStatusEnum c : TransactionStatusEnum.values()) {
		        if (c.name().equals(value)) {
		            return true;
		        }
		    }
		    return false;
		}
	}
}
