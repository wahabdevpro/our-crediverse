package hxc.connectors.bundles;

import java.math.BigDecimal;

public interface IBundleProvider
{
	public enum StatusCode
	{
		Success, NotEligible, Failed, Unknown
	};

	public abstract IBundleInfo[] getBundleInfo(int companyID);

	public abstract StatusCode isEligible(String msisdn, String tag, String subscriberIMSI);

	public abstract StatusCode provision(String msisdn, String tag, //
			String agentMSISDN, String transactionNo, String subscriberIMSI, BigDecimal price,
			Integer agentCellId, String agentCellGroupCode, Integer subscriberCellId, String subscriberCellGroupCode);

	public abstract StatusCode reverse(String msisdn, String tag, BigDecimal amount, //
			String agentMSISDN, String transactionNo);
}
