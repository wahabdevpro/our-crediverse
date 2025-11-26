package hxc.services.airsim.engine;

import hxc.services.airsim.InjectedResponse;
import hxc.services.airsim.SupportedRequest;
import hxc.services.airsim.model.SubscriberEx;
import hxc.utils.protocol.hmx.BasicLocation;
import hxc.utils.protocol.hmx.CellGlobalId;
import hxc.utils.protocol.hmx.GetSubscriberInformationRequest;
import hxc.utils.protocol.hmx.GetSubscriberInformationResponse;
import hxc.utils.protocol.hmx.GetSubscriberInformationResponseMembers;
import hxc.utils.protocol.hmx.GetSubscriberInformationResponseParameters;
import hxc.utils.protocol.hmx.Imsi;
import hxc.utils.protocol.hmx.Mnp;
import hxc.utils.protocol.hmx.State;
import hxc.utils.protocol.hsx.ResponseHeader;

// GetSubscriberInformationRequest
public class GetSubscriberInformation extends SupportedRequest<GetSubscriberInformationRequest, GetSubscriberInformationResponse>
{

	public GetSubscriberInformation()
	{
		super(GetSubscriberInformationRequest.class);
	}

	@Override
	protected GetSubscriberInformationResponse execute(GetSubscriberInformationRequest request, InjectedResponse injectedResponse)
	{
		// Create Response
		GetSubscriberInformationResponse response = new GetSubscriberInformationResponse();
		{
			response.members = new GetSubscriberInformationResponseMembers();
			{
				response.members.responseHeader = new ResponseHeader();
				response.members.responseHeader.originTransactionId = request.member.requestHeader.originTransactionId;
				response.members.responseHeader.originOperatorId = request.member.requestHeader.originOperatorId;
				response.members.responseHeader.responseCode = 0;
				response.members.responseHeader.responseMessage = "OK";
			}
			{
				response.members.responseParameters = new GetSubscriberInformationResponseParameters();
				response.members.responseParameters.subscriberNumber = request.member.requestParameters.subscriberNumber;

			}
		}
		
		// Test for injected response
		if (injectedResponse != null)
		{
			int count = injectedResponse.getCount();
			injectedResponse.setCount(count + 1);
			if (count >= injectedResponse.getSkipCount() && count < injectedResponse.getSkipCount() + injectedResponse.getFailCount())
			{
				if (injectedResponse.getDelay_ms() > 0)
				{
					try
					{
						Thread.sleep(injectedResponse.getDelay_ms());
					}
					catch (InterruptedException e)
					{
					}
				}
				
				if (injectedResponse.getResponseCode() != null && injectedResponse.getResponseCode()!=0)
				{
					response.members.responseHeader.setResponseCode(injectedResponse.getResponseCode(), hxc.utils.protocol.hsx.ResponseHeader.COMPONENT_SGW, 0);
					if (injectedResponse.getResponseCode() > 0)
						return response;
				}
			}
		}

		// Get the Subscriber
		SubscriberEx subscriber = simulationData.getNationalSubscriber(request.member.requestParameters.subscriberNumber.addressDigits);
		if (subscriber == null)
		{
			response.members.responseHeader.setResponseCode(ResponseHeader.COMPLETE_NORETRY, 3, 102);
			return response;
		}

		// Create Response
		response.members.responseParameters.domain = subscriber.getDomain();

		// State
		if (request.member.requestParameters.requestState)
		{
			response.members.responseParameters.state = new State();
			response.members.responseParameters.state.stateId = subscriber.getStateId();
		}

		// MNP
		if (request.member.requestParameters.requestMnpStatus)
		{
			response.members.responseParameters.mnp = new Mnp();
			response.members.responseParameters.mnp.mnpStatusId = subscriber.getMnpStatusId();
		}

		// Location
		if (request.member.requestParameters.requestBasicLocation && subscriber.getMobileCountryCode() >= 0)
		{
			response.members.responseParameters.basicLocation = new BasicLocation();
			response.members.responseParameters.basicLocation.ageInSeconds = (int) subscriber.getFixAgeInSeconds();
			{
				response.members.responseParameters.basicLocation.vlrNumber = new hxc.utils.protocol.hsx.Number();
				response.members.responseParameters.basicLocation.vlrNumber.addressDigits = subscriber.getNationalNumber();
				response.members.responseParameters.basicLocation.mscNumber = response.members.responseParameters.basicLocation.vlrNumber;
			}
			{
				response.members.responseParameters.basicLocation.cellGlobalId = new CellGlobalId();
				response.members.responseParameters.basicLocation.cellGlobalId.mobileCountryCode = subscriber.getMobileCountryCode();
				response.members.responseParameters.basicLocation.cellGlobalId.mobileNetworkCode = subscriber.getMobileNetworkCode();
				response.members.responseParameters.basicLocation.cellGlobalId.locationAreaCode = subscriber.getLocationAreaCode();
				response.members.responseParameters.basicLocation.cellGlobalId.cellIdentity = subscriber.getCellIdentity();
			}
		}

		// IMSI
		if (request.member.requestParameters.requestImsi)
		{
			String value = subscriber.getImsi();
			if (value != null && !value.isEmpty())
			{
				Imsi imsi = response.members.responseParameters.imsi = new Imsi();
				imsi.imsi = subscriber.getImsi();
			}
		}

		return response;
	}
}
