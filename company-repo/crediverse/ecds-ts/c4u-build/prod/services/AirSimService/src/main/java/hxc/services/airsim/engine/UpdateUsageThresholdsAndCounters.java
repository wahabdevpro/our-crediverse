package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.services.airsim.protocol.UsageCounter;
import hxc.services.airsim.protocol.UsageThreshold;
import hxc.utils.protocol.ucip.UpdateUsageThresholdsAndCountersRequest;
import hxc.utils.protocol.ucip.UpdateUsageThresholdsAndCountersResponse;
import hxc.utils.protocol.ucip.UsageCounterUpdateInformation;
import hxc.utils.protocol.ucip.UsageThresholdUpdateInformation;

/*
 0 Success
 100 Other
 102 Subscriber not found
 104 Temporary blocked
 216 Usage threshold not found in definition
 217 Usage counter not found in definition
 219 Usage counter value out of bounds
 220 The supplied value type does not match the definition
 221 No subordinate subscribers connected to the account
 243 Missing associated party ID for provider owned personal usage counter.
 244 Associated party ID not allowed for provider owned common usage counter.
 245 Provider owned common usage counter can not have personal usage threshold.
 247 Product not found
 260 Capability not available
 */

public class UpdateUsageThresholdsAndCounters extends SupportedRequest<UpdateUsageThresholdsAndCountersRequest, UpdateUsageThresholdsAndCountersResponse>
{
	public UpdateUsageThresholdsAndCounters()
	{
		super(UpdateUsageThresholdsAndCountersRequest.class);
	}

	@Override
	protected UpdateUsageThresholdsAndCountersResponse execute(UpdateUsageThresholdsAndCountersRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		UpdateUsageThresholdsAndCountersResponse response = new UpdateUsageThresholdsAndCountersResponse();
		if (!validate(request.member, response.member, injectedResponse))
			return response;

		// Get the Subscriber
		SubscriberEx subscriber = getSubscriber(request.member);
		if (subscriber == null)
		{
			response.member.setResponseCode(102);
			return response;
		}

		// Process Counters
		if (request.member.usageCounterUpdateInformation != null)
		{
			for (UsageCounterUpdateInformation uc : request.member.usageCounterUpdateInformation)
			{
				UsageCounter oldUC = subscriber.getUsageCounters().get(uc.usageCounterID);
				if (oldUC == null)
				{
					// String msisdn = subscriber.getInternationalNumber();
					// response.member.setResponseCode(217);
					// return response;
					oldUC = new UsageCounter();
					oldUC.setUsageCounterID(uc.usageCounterID);
					subscriber.getUsageCounters().put(uc.usageCounterID, oldUC);
				}

				oldUC.setUsageCounterValue(relative(oldUC.getUsageCounterValue(), uc.usageCounterValueNew, uc.adjustmentUsageCounterValueRelative));
				oldUC.setUsageCounterMonetaryValue1(relative(oldUC.getUsageCounterMonetaryValue1(), uc.usageCounterMonetaryValueNew, uc.adjustmentUsageCounterMonetaryValueRelative));
				oldUC.setUsageCounterMonetaryValue2(oldUC.getUsageCounterMonetaryValue1());
				oldUC.setAssociatedPartyID(uc.associatedPartyID);
				oldUC.setProductID(uc.productID);

			}
		}

		// Process Thresholds
		if (request.member.usageThresholdUpdateInformation != null)
		{
			for (UsageThresholdUpdateInformation ut : request.member.usageThresholdUpdateInformation)
			{
				UsageThreshold oldUT = subscriber.getUsageThresholds().get(ut.usageThresholdID);
				if (oldUT == null)
				{
					// String msisdn = subscriber.getInternationalNumber();
					// response.member.setResponseCode(216);
					// return response;
					oldUT = new UsageThreshold();
					oldUT.setUsageThresholdID(ut.usageThresholdID);
					subscriber.getUsageThresholds().put(ut.usageThresholdID, oldUT);
				}

				oldUT.setUsageThresholdValue(ut.usageThresholdValueNew);
				oldUT.setUsageThresholdMonetaryValue1(ut.usageThresholdMonetaryValueNew);
				oldUT.setUsageThresholdMonetaryValue2(ut.usageThresholdMonetaryValueNew);
				// oldUT.setUsageThresholdSource();
				oldUT.setAssociatedPartyID(ut.associatedPartyID);
			}
		}

		// Update the Response
		response.member.currency1 = subscriber.getCurrency1();
		response.member.currency2 = subscriber.getCurrency2();
		response.member.usageCounterUsageThresholdInformation = GetUsageThresholdsAndCounters.getInformation(subscriber);

		return response;
	}

	private Long relative(Long oldValue, Long newValue, Long deltaValue)
	{
		Long result = 0L;

		if (oldValue == null)
		{
			result = deltaValue != null ? deltaValue : newValue;
		}
		else
		{
			result = deltaValue != null ? Long.valueOf(oldValue + deltaValue) : newValue;
		}

		return result;
	}

}
