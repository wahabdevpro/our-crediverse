package hxc.ecds.protocol.rest.reports;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
ADHOC: CSV: GET ~/reports/sales_summary/adhoc/csv?first=...&max=...&sort=...&filter=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&relativeTimeRange.reference=...
ADHOC: JSON: GET ~/reports/sales_summary/adhoc/json?first=...&max=...&sort=...&filter=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&relativeTimeRange.reference=...
*/
/*
CREATE: PUT ~/reports/sales_summary?sort=...&filter=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&name=...
UPDATE: PUT ~/reports/sales_summary/{id}?sort=...&filter=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&name=...
LIST: GET ~/reports/sales_summary
LOAD: GET ~/reports/sales_summary/{id}
DOWNLOAD REPORT DATA: JSON: GET ~/reports/sales_summary/{id}/json?first=...&max=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&relativeTimeRange.reference=...
DOWNLOAD REPORT DATA: CSV: GET ~/reports/sales_summary/{id}/csv?first=...&max=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&relativeTimeRange.reference=...

*/

public class SalesSummaryReport
{
	public static enum ResultField implements Report.IResultField
	{
		DATE("date", Date.class),
		TOTAL_AMOUNT("totalAmount", BigDecimal.class),
		TOTAL_AIRTIME_AMOUNT("totalAmountAirtime", BigDecimal.class),
		TOTAL_AMOUNT_NON_AIRTIME("totalAmountNonAirtime", BigDecimal.class),
		AGENT_COUNT("agentCount", Integer.class),
		AGENT_COUNT_AIRTIME("agentCountAirtime", Integer.class),
		AGENT_COUNT_NON_AIRTIME("agentCountNonAirtime", Integer.class),
		SUCCESSFUL_TRANSACTION_COUNT("successfulTransactionCount", Integer.class),
		SUCCESSFUL_AIRTIME_TRANSACTION_COUNT("successfulAirtimeTransactionCount", Integer.class),
		SUCCESSFUL_NON_AIRTIME_TRANSACTION_COUNT("successfulNonAirtimeTransactionCount", Integer.class),
		FAILED_TRANSACTION_COUNT("failedTransactionCount", Integer.class),
		FAILED_AIRTIME_TRANSACTION_COUNT("failedAirtimeTransactionCount", Integer.class),
		FAILED_NON_AIRTIME_TRANSACTION_COUNT("failedNonAirtimeTransactionCount", Integer.class),
		AVERAGE_AMOUNT_PER_AGENT("averageAmountPerAgent", BigDecimal.class),
		AVERAGE_AIRTIME_AMOUNT_PER_AGENT("averageAirtimeAmountPerAgent", BigDecimal.class),
		AVERAGE_NON_AIRTIME_AMOUNT_PER_AGENT("averageNonAirtimeAmountPerAgent", BigDecimal.class),
		AVERAGE_AMOUNT_PER_TRANSACTION("averageAmountPerTransaction", BigDecimal.class),
		AVERAGE_AIRTIME_AMOUNT_PER_TRANSACTION("averageAirtimeAmountPerTransaction", BigDecimal.class),
		AVERAGE_NON_AIRTIME_AMOUNT_PER_TRANSACTION("averageNonAirtimeAmountPerTransaction", BigDecimal.class),
		MOBILE_MONEY_TRANSFER_COUNT_SUCCESS("SuccessfulMMTransferCount", Integer.class),
		MOBILE_MONEY_TOTAL_TRANSFER_AMOUNT("TotalAmountMMTransfer", BigDecimal.class),
		MOBILE_MONEY_TRANSFER_COUNT_FAILED("FailedMMTransfersCount", Integer.class),
		MOBILE_MONEY_RECEIVING_AGENT_COUNT("AgentCountReceivingMM", Integer.class),
		MOBILE_MONEY_AVERAGE_TRASNFER_AMOUNT_PER_AGENT("AveMMTransferAmountPerAgent", BigDecimal.class),
		MOBILE_MONEY_AVERAGE_AMOUNT_PER_TRANSACTION("AveMMAmountPerTransaction", BigDecimal.class);

		private String identifier;
		private Class<?> type;

		public String getIdentifier()
		{
			return this.identifier;
		}

		public Class<?> getType()
		{
			return this.type;
		}

		private ResultField(String identifier, Class<?> type)
		{
			this.identifier = identifier;
			this.type = type;
		}

		public static final Map<String,ResultField> identifierMap = new HashMap<String,ResultField>();

		public static Map<String,ResultField> getIdentifierMap()
		{
			return Collections.unmodifiableMap(identifierMap);
		}

		public static ResultField fromIdentifier(String identifier) throws NullPointerException, IllegalArgumentException
		{
			if (identifier == null) throw new NullPointerException("identifier may not be null");
			ResultField result = identifierMap.get(identifier);
			if (result == null) throw new IllegalArgumentException(String.format("No ResultField associated with identifier '%s' (valid identifiers = %s)", identifier, identifierMap.keySet()));
			return result;
		}

		static
		{
			for (ResultField resultField : values())
			{
				identifierMap.put(resultField.getIdentifier(),resultField);
			}
		}
	}


	protected SalesSummaryReport()
	{
	}
}
