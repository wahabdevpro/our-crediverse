package hxc.userinterfaces.cc.data;

import java.util.ArrayList;
import java.util.List;

public class ConsumerOwner
{
	private String msisdn;
	private String serviceId;
	private String variantId;
	List<MemberQuota> quotas;

	/**
	 * @return the msisdn
	 */
	public String getMsisdn()
	{
		return msisdn;
	}

	/**
	 * @param msisdn
	 *            the msisdn to set
	 */
	public void setMsisdn(String msisdn)
	{
		this.msisdn = msisdn;
	}

	/**
	 * @return the quotas
	 */
	public List<MemberQuota> getQuotas()
	{
		return quotas;
	}

	/**
	 * @param quotas
	 *            the quotas to set
	 */
	public void setQuotas(List<MemberQuota> quotas)
	{
		this.quotas = quotas;
	}

	public ConsumerOwner()
	{
		quotas = new ArrayList<>();
	}

	/**
	 * @return the serviceId
	 */
	public String getServiceId()
	{
		return serviceId;
	}

	/**
	 * @param serviceId
	 *            the serviceId to set
	 */
	public void setServiceId(String serviceId)
	{
		this.serviceId = serviceId;
	}

	/**
	 * @return the variantId
	 */
	public String getVariantId()
	{
		return variantId;
	}

	/**
	 * @param variantId
	 *            the variantId to set
	 */
	public void setVariantId(String variantId)
	{
		this.variantId = variantId;
	}

}
