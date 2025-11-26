package hxc.services.sharedaccounts;

import java.io.Serializable;

import hxc.configuration.Configurable;
import hxc.services.notification.IPhrase;
import hxc.services.notification.Phrase;

@Configurable
public class ServiceClass implements Serializable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	private int serviceClassID;

	// Names
	private Phrase names;

	// Eligible for Shared Service Provider
	private boolean eligibleForProvider;

	// Eligible for Shared Service Consumer
	private boolean eligibleForConsumer;

	// Is Post-Paid Flag
	private boolean postPaid;

	// Maximum Consumers
	private int maxConsumers = 4;

	// Consumer Addition Charge in USD
	private int addConsumerCharge;

	// Consumer Removal Charge in USD
	private int removeConsumerCharge;

	// Quota Removal Charge in USD
	private int removeQuotaCharge;

	// Provider Balance Enquiry Charge in USD
	private int providerBalanceEnquiryCharge;

	// Consumer Balance Enquiry Charge in USD
	private int consumerBalanceEnquiryCharge;

	// Un-Subscription Fee in USD
	private int unsubscribeCharge;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public int getServiceClassID()
	{
		return serviceClassID;
	}

	public void setServiceClassID(int serviceClassID)
	{
		this.serviceClassID = serviceClassID;
	}

	public Phrase getNames()
	{
		return names;
	}

	public void setNames(Phrase names)
	{
		this.names = names;
	}

	public boolean isEligibleForProvider()
	{
		return eligibleForProvider;
	}

	public void setEligibleForProvider(boolean eligibleForProvider)
	{
		this.eligibleForProvider = eligibleForProvider;
	}

	public boolean isEligibleForConsumer()
	{
		return eligibleForConsumer;
	}

	public void setEligibleForConsumer(boolean eligibleForConsumer)
	{
		this.eligibleForConsumer = eligibleForConsumer;
	}

	public boolean isPostPaid()
	{
		return postPaid;
	}

	public void setPostPaid(boolean postPaid)
	{
		this.postPaid = postPaid;
	}

	public int getMaxConsumers()
	{
		return maxConsumers;
	}

	public void setMaxConsumers(int maxConsumers)
	{
		this.maxConsumers = maxConsumers;
	}

	public int getAddConsumerCharge()
	{
		return addConsumerCharge;
	}

	public void setAddConsumerCharge(int addConsumerCharge)
	{
		this.addConsumerCharge = addConsumerCharge;
	}

	public int getRemoveConsumerCharge()
	{
		return removeConsumerCharge;
	}

	public void setRemoveConsumerCharge(int removeConsumerCharge)
	{
		this.removeConsumerCharge = removeConsumerCharge;
	}

	public int getUnsubscribeCharge()
	{
		return unsubscribeCharge;
	}

	public void setUnsubscribeCharge(int unsubscribeCharge)
	{
		this.unsubscribeCharge = unsubscribeCharge;
	}

	public int getRemoveQuotaCharge()
	{
		return removeQuotaCharge;
	}

	public void setRemoveQuotaCharge(int removeQuotaCharge)
	{
		this.removeQuotaCharge = removeQuotaCharge;
	}

	public int getProviderBalanceEnquiryCharge()
	{
		return providerBalanceEnquiryCharge;
	}

	public void setProviderBalanceEnquiryCharge(int providerBalanceEnquiryCharge)
	{
		this.providerBalanceEnquiryCharge = providerBalanceEnquiryCharge;
	}

	public int getConsumerBalanceEnquiryCharge()
	{
		return consumerBalanceEnquiryCharge;
	}

	public void setConsumerBalanceEnquiryCharge(int consumerBalanceEnquiryCharge)
	{
		this.consumerBalanceEnquiryCharge = consumerBalanceEnquiryCharge;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public ServiceClass()
	{

	}

	public ServiceClass(int serviceClassID, IPhrase names, boolean eligibleForProvider, boolean eligibleForConsumer, boolean eligibleForProsumer, //
			boolean postPaid, int maxConsumers, int addConsumerCharge, int removeConsumerCharge, int unsubscribeCharge, //
			int removeQuotaCharge, int providerBalanceEnquiryCharge, int consumerBalanceEnquiryCharge)
	{
		super();
		this.serviceClassID = serviceClassID;
		this.names = new Phrase(names);
		this.eligibleForProvider = eligibleForProvider;
		this.eligibleForConsumer = eligibleForConsumer;
		this.postPaid = postPaid;
		this.maxConsumers = maxConsumers;
		this.addConsumerCharge = addConsumerCharge;
		this.removeConsumerCharge = removeConsumerCharge;
		this.unsubscribeCharge = unsubscribeCharge;
		this.removeQuotaCharge = removeQuotaCharge;
		this.providerBalanceEnquiryCharge = providerBalanceEnquiryCharge;
		this.consumerBalanceEnquiryCharge = consumerBalanceEnquiryCharge;
	}

}
