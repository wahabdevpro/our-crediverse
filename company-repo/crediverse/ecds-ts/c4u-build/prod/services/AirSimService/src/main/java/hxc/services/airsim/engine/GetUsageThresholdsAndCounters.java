package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.services.airsim.protocol.UsageCounter;
import hxc.services.airsim.protocol.UsageThreshold;
import hxc.utils.protocol.ucip.GetUsageThresholdsAndCountersRequest;
import hxc.utils.protocol.ucip.GetUsageThresholdsAndCountersResponse;
import hxc.utils.protocol.ucip.UsageCounterUsageThresholdInformation;
import hxc.utils.protocol.ucip.UsageThresholdInformation;

public class GetUsageThresholdsAndCounters extends SupportedRequest<GetUsageThresholdsAndCountersRequest, GetUsageThresholdsAndCountersResponse>
{

	public GetUsageThresholdsAndCounters()
	{
		super(GetUsageThresholdsAndCountersRequest.class);
	}

	@Override
	protected GetUsageThresholdsAndCountersResponse execute(GetUsageThresholdsAndCountersRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		GetUsageThresholdsAndCountersResponse response = new GetUsageThresholdsAndCountersResponse();
		if (!validate(request.member, response.member, injectedResponse))
			return response;

		// Get the Subscriber
		SubscriberEx subscriber = getSubscriber(request.member);
		if (subscriber == null)
		{
			response.member.setResponseCode(102);
			return response;
		}

		// Create Response
		response.member.currency1 = subscriber.getCurrency1();
		response.member.currency2 = subscriber.getCurrency1();
		response.member.usageCounterUsageThresholdInformation = getInformation(subscriber);

		return response;
	}

	public static UsageCounterUsageThresholdInformation[] getInformation(SubscriberEx subscriber)
	{
		UsageCounterUsageThresholdInformation[] result = new UsageCounterUsageThresholdInformation[subscriber.getUsageCounters().size()];
		int index = 0;
		for (UsageCounter uc : subscriber.getUsageCounters().values())
		{
			UsageCounterUsageThresholdInformation uci = new UsageCounterUsageThresholdInformation();
			uci.usageCounterID = uc.getUsageCounterID();
			uci.usageCounterValue = uc.getUsageCounterValue();
			uci.usageCounterMonetaryValue1 = uc.getUsageCounterMonetaryValue1();
			uci.usageCounterMonetaryValue2 = uc.getUsageCounterMonetaryValue2();
			uci.associatedPartyID = uc.getAssociatedPartyID();
			uci.productID = uc.getProductID();
			{
				uci.usageThresholdInformation = new UsageThresholdInformation[subscriber.getUsageThresholds().size()];
				int index2 = 0;
				for (UsageThreshold ut : subscriber.getUsageThresholds().values())
				{
					UsageThresholdInformation uti = new UsageThresholdInformation();
					uti.usageThresholdID = ut.getUsageThresholdID();
					uti.usageThresholdValue = ut.getUsageThresholdValue();
					uti.usageThresholdMonetaryValue1 = ut.getUsageThresholdMonetaryValue1();
					uti.usageThresholdMonetaryValue2 = ut.getUsageThresholdMonetaryValue2();
					uti.usageThresholdSource = ut.getUsageThresholdSource();
					uti.associatedPartyID = ut.getAssociatedPartyID();
					uci.usageThresholdInformation[index2++] = uti;
				}
			}
			result[index++] = uci;
		}
		return result;
	}

}
