package hxc.connectors.soap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.AddCreditTransferRequest;
import com.concurrent.hxc.AddCreditTransferResponse;
import com.concurrent.hxc.AddMemberRequest;
import com.concurrent.hxc.AddMemberResponse;
import com.concurrent.hxc.AddQuotaRequest;
import com.concurrent.hxc.AddQuotaResponse;
import com.concurrent.hxc.ChangeCreditTransferRequest;
import com.concurrent.hxc.ChangeCreditTransferResponse;
import com.concurrent.hxc.ChangePINRequest;
import com.concurrent.hxc.ChangePINResponse;
import com.concurrent.hxc.ChangeQuotaRequest;
import com.concurrent.hxc.ChangeQuotaResponse;
import com.concurrent.hxc.ExtendRequest;
import com.concurrent.hxc.ExtendResponse;
import com.concurrent.hxc.GetBalancesRequest;
import com.concurrent.hxc.GetBalancesResponse;
import com.concurrent.hxc.GetCreditTransfersRequest;
import com.concurrent.hxc.GetCreditTransfersResponse;
import com.concurrent.hxc.GetDeviceInfoRequest;
import com.concurrent.hxc.GetDeviceInfoResponse;
import com.concurrent.hxc.GetLocaleSettingsRequest;
import com.concurrent.hxc.GetLocaleSettingsResponse;
import com.concurrent.hxc.GetLocationRequest;
import com.concurrent.hxc.GetLocationResponse;
import com.concurrent.hxc.GetMembersRequest;
import com.concurrent.hxc.GetMembersResponse;
import com.concurrent.hxc.GetOwnersRequest;
import com.concurrent.hxc.GetOwnersResponse;
import com.concurrent.hxc.GetQuotasRequest;
import com.concurrent.hxc.GetQuotasResponse;
import com.concurrent.hxc.GetReturnCodeTextRequest;
import com.concurrent.hxc.GetReturnCodeTextResponse;
import com.concurrent.hxc.GetServiceRequest;
import com.concurrent.hxc.GetServiceResponse;
import com.concurrent.hxc.GetServicesRequest;
import com.concurrent.hxc.GetServicesResponse;
import com.concurrent.hxc.GetStatusRequest;
import com.concurrent.hxc.GetStatusResponse;
import com.concurrent.hxc.IHxC;
import com.concurrent.hxc.IServiceContext;
import com.concurrent.hxc.MigrateRequest;
import com.concurrent.hxc.MigrateResponse;
import com.concurrent.hxc.PingRequest;
import com.concurrent.hxc.PingResponse;
import com.concurrent.hxc.ProcessLifecycleEventRequest;
import com.concurrent.hxc.ProcessLifecycleEventResponse;
import com.concurrent.hxc.ProcessRequest;
import com.concurrent.hxc.ProcessResponse;
import com.concurrent.hxc.RedeemRequest;
import com.concurrent.hxc.RedeemResponse;
import com.concurrent.hxc.RemoveCreditTransfersRequest;
import com.concurrent.hxc.RemoveCreditTransfersResponse;
import com.concurrent.hxc.RemoveMemberRequest;
import com.concurrent.hxc.RemoveMemberResponse;
import com.concurrent.hxc.RemoveMembersRequest;
import com.concurrent.hxc.RemoveMembersResponse;
import com.concurrent.hxc.RemoveQuotaRequest;
import com.concurrent.hxc.RemoveQuotaResponse;
import com.concurrent.hxc.ReplaceMemberRequest;
import com.concurrent.hxc.ReplaceMemberResponse;
import com.concurrent.hxc.ResetPINRequest;
import com.concurrent.hxc.ResetPINResponse;
import com.concurrent.hxc.ResumeCreditTransferRequest;
import com.concurrent.hxc.ResumeCreditTransferResponse;
import com.concurrent.hxc.SendSMSRequest;
import com.concurrent.hxc.SendSMSResponse;
import com.concurrent.hxc.ServiceContext;
import com.concurrent.hxc.SubscribeRequest;
import com.concurrent.hxc.SubscribeResponse;
import com.concurrent.hxc.SuspendCreditTransferRequest;
import com.concurrent.hxc.SuspendCreditTransferResponse;
import com.concurrent.hxc.SuspendRequest;
import com.concurrent.hxc.SuspendResponse;
import com.concurrent.hxc.TransferRequest;
import com.concurrent.hxc.TransferResponse;
import com.concurrent.hxc.UnsubscribeRequest;
import com.concurrent.hxc.UnsubscribeResponse;
import com.concurrent.hxc.UnsuspendRequest;
import com.concurrent.hxc.UnsuspendResponse;
import com.concurrent.hxc.UpdateContactInfoRequest;
import com.concurrent.hxc.UpdateContactInfoResponse;
import com.concurrent.hxc.ValidatePINRequest;
import com.concurrent.hxc.ValidatePINResponse;
import com.concurrent.hxc.VasServiceInfo;

import hxc.connectors.sms.ISmsConnector;
import hxc.connectors.sms.ISmsResponse;
import hxc.connectors.vas.VasService;
import hxc.servicebus.ILocale;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ReturnCodes;
import hxc.utils.notification.NotificationText;

@WebService(endpointInterface = "com.concurrent.hxc.IHxC")
public class HxC implements IHxC
{
	final static Logger logger = LoggerFactory.getLogger(HxC.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private Map<String, VasService> serviceMap = new HashMap<String, VasService>();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public HxC(IServiceBus esb)
	{
		this.esb = esb;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IHxC Implementation
	//
	// /////////////////////////////////
	@Override
	public PingResponse ping(PingRequest request)
	{
		PingResponse response = new PingResponse();
		response.setSeq(request.getSeq() + 1);
		return response;
	}

	@Override
	public GetServicesResponse getServices(GetServicesRequest request)
	{
		ServiceContext context = new ServiceContext();
		return getServicesEx(context, request);
	}

	@Override
	public GetServicesResponse getServicesEx(IServiceContext context, GetServicesRequest request)
	{
		GetServicesResponse response = new GetServicesResponse(request);

		List<VasService> services = esb.getServices(VasService.class);
		List<VasServiceInfo> serviceInfo = new ArrayList<VasServiceInfo>();

		for (VasService service : services)
		{
			try
			{
				VasServiceInfo[] info = service.getServiceInfo(context, request.getSubscriberNumber(), null, request.getLanguageID(), request.isActiveOnly(), request.isSuggested());
				if (info != null && info.length > 0)
				{
					for (VasServiceInfo element : info)
					{
						serviceInfo.add(element);
					}
				}
			}
			catch (Exception e)
			{
				logger.error("", e);
				// response.setReturnCode(ReturnCodes.technicalProblem);
				// String message = e.getMessage();
				// response.setMessage(message == null || message.length() == 0 ? e.toString() : message);
				// return response;
			}
		}

		response.setServiceInfo(serviceInfo.toArray(new VasServiceInfo[0]));
		response.setReturnCode(ReturnCodes.success);
		response.setMessage("Success");

		return response;
	}

	@Override
	public GetServiceResponse getService(GetServiceRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.getService(new ServiceContext(), request);
		return new GetServiceResponse(request);
	}

	@Override
	public SubscribeResponse subscribe(SubscribeRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.subscribe(new ServiceContext(), request);
		return new SubscribeResponse(request);
	}

	@Override
	public UnsubscribeResponse unsubscribe(UnsubscribeRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.unsubscribe(new ServiceContext(), request);
		return new UnsubscribeResponse(request);
	}

	@Override
	public ExtendResponse extend(ExtendRequest request)
	{
		return new ExtendResponse(request);
	}

	@Override
	public SuspendResponse suspend(SuspendRequest request)
	{
		return new SuspendResponse(request);
	}

	@Override
	public UnsuspendResponse unsuspend(UnsuspendRequest request)
	{
		return new UnsuspendResponse(request);
	}

	@Override
	public MigrateResponse migrate(MigrateRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.migrate(new ServiceContext(), request);
		return new MigrateResponse(request);
	}

	@Override
	public GetMembersResponse getMembers(GetMembersRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.getMembers(new ServiceContext(), request);
		return new GetMembersResponse(request);
	}

	@Override
	public GetOwnersResponse getOwners(GetOwnersRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.getOwners(new ServiceContext(), request);
		return new GetOwnersResponse(request);
	}

	@Override
	public AddMemberResponse addMember(AddMemberRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.addMember(new ServiceContext(), request);
		return new AddMemberResponse(request);
	}

	@Override
	public ReplaceMemberResponse replaceMember(ReplaceMemberRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.replaceMember(new ServiceContext(), request);
		return new ReplaceMemberResponse(request);
	}

	@Override
	public RemoveMemberResponse removeMember(RemoveMemberRequest request)
	{

		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.removeMember(new ServiceContext(), request);
		return new RemoveMemberResponse(request);
	}

	@Override
	public RemoveMembersResponse removeMembers(RemoveMembersRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.removeMembers(new ServiceContext(), request);
		return new RemoveMembersResponse(request);
	}

	@Override
	public GetBalancesResponse getBalances(GetBalancesRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.getBalances(new ServiceContext(), request);
		return new GetBalancesResponse(request);
	}

	@Override
	public GetStatusResponse getStatus(GetStatusRequest request)
	{
		return new GetStatusResponse(request);
	}

	@Override
	public GetQuotasResponse getQuotas(GetQuotasRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.getQuotas(new ServiceContext(), request);
		return new GetQuotasResponse(request);
	}

	@Override
	public AddQuotaResponse addQuota(AddQuotaRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.addQuota(new ServiceContext(), request);
		return new AddQuotaResponse(request);
	}

	@Override
	public ChangeQuotaResponse changeQuota(ChangeQuotaRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.changeQuota(new ServiceContext(), request);
		return new ChangeQuotaResponse(request);
	}

	@Override
	public RemoveQuotaResponse removeQuota(RemoveQuotaRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.removeQuota(new ServiceContext(), request);
		return new RemoveQuotaResponse(request);
	}

	@Override
	public TransferResponse transfer(TransferRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.transfer(new ServiceContext(), request);
		return new TransferResponse(request);
	}

	@Override
	public ProcessResponse process(ProcessRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.process(new ServiceContext(), request);
		return new ProcessResponse(request);
	}

	@Override
	public ProcessLifecycleEventResponse processLifecycleEvent(ProcessLifecycleEventRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.processLifecycleEvent(new ServiceContext(), request);
		return new ProcessLifecycleEventResponse(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// PIN
	//
	// /////////////////////////////////

	@Override
	public ResetPINResponse resetPIN(ResetPINRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.resetPIN(new ServiceContext(), request);
		return new ResetPINResponse(request);
	}

	@Override
	public ChangePINResponse changePIN(ChangePINRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.changePIN(new ServiceContext(), request);
		return new ChangePINResponse(request);
	}

	@Override
	public ValidatePINResponse validatePIN(ValidatePINRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.validatePIN(new ServiceContext(), request);
		return new ValidatePINResponse(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Redemption
	//
	// /////////////////////////////////

	@Override
	public RedeemResponse redeem(RedeemRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.redeem(new ServiceContext(), request);
		return new RedeemResponse(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Credit Transfer
	//
	// /////////////////////////////////
	@Override
	public GetCreditTransfersResponse getCreditTransfers(GetCreditTransfersRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.getCreditTransfers(new ServiceContext(), request);
		return new GetCreditTransfersResponse(request);
	}

	@Override
	public AddCreditTransferResponse addCreditTransfer(AddCreditTransferRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.addCreditTransfer(new ServiceContext(), request);
		return new AddCreditTransferResponse(request);
	}

	@Override
	public RemoveCreditTransfersResponse removeCreditTransfers(RemoveCreditTransfersRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.removeCreditTransfers(new ServiceContext(), request);
		return new RemoveCreditTransfersResponse(request);
	}

	@Override
	public ChangeCreditTransferResponse changeCreditTransfer(ChangeCreditTransferRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.changeCreditTransfer(new ServiceContext(), request);
		return new ChangeCreditTransferResponse(request);
	}

	@Override
	public SuspendCreditTransferResponse suspendCreditTransfer(SuspendCreditTransferRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.suspendCreditTransfer(new ServiceContext(), request);
		return new SuspendCreditTransferResponse(request);
	}

	@Override
	public ResumeCreditTransferResponse resumeCreditTransfer(ResumeCreditTransferRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.resumeCreditTransfer(new ServiceContext(), request);
		return new ResumeCreditTransferResponse(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Location Based Services
	//
	// /////////////////////////////////
	@Override
	public GetLocationResponse getLocation(GetLocationRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.getLocation(new ServiceContext(), request);
		return new GetLocationResponse(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Return Code Text
	//
	// /////////////////////////////////

	@Override
	public GetReturnCodeTextResponse getReturnCodeText(GetReturnCodeTextRequest request)
	{
		String text = null;

		// Get Language ID / Code
		int languageID = esb.getLocale().getLanguageID(request.getLanguageID());
		String languageCode = esb.getLocale().getLanguage(languageID);

		GetReturnCodeTextResponse response = new GetReturnCodeTextResponse(request);
		response.setReturnCode(ReturnCodes.success);

		if (request.getServiceID() != null)
		{
			VasService service = getVasService(request.getServiceID());
			if (service != null)
				text = service.getReturnCodeText(languageCode, request.getReturnCode());
		}

		if (text == null || text.length() == 0)
		{
			switch (esb.getLocale().getLanguage(languageID))
			{
				case "eng":
					text = EngReturnCodeTexts.getReturnCodeText(request.getReturnCode());
					break;

				case "fre":
					text = FreReturnCodeTexts.getReturnCodeText(request.getReturnCode());
					break;

				default:
					text = EngReturnCodeTexts.getReturnCodeText(request.getReturnCode());
					break;
			}
		}

		// Else try to get text for Technical Error
		if ((text == null || text.length() == 0) && !request.getReturnCode().equals(ReturnCodes.technicalProblem))
		{
			GetReturnCodeTextRequest request2 = new GetReturnCodeTextRequest(request);
			request2.setServiceID(request.getServiceID());
			request2.setReturnCode(ReturnCodes.technicalProblem);
			request2.setLanguageID(languageID);
			GetReturnCodeTextResponse response2 = getReturnCodeText(request2);
			text = response2.getReturnCodeText();
		}

		// Default to English a Technical Error has Occurred
		if (text == null || text.length() == 0)
			text = "A Technical Error has Occurred";

		response.setReturnCodeText(text);

		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Locale
	//
	// /////////////////////////////////
	@Override
	public GetLocaleSettingsResponse getLocaleSettings(GetLocaleSettingsRequest request)
	{
		GetLocaleSettingsResponse response = new GetLocaleSettingsResponse(request);
		response.setReturnCode(ReturnCodes.success);

		String problem = GetLocaleSettingsRequest.validate(request);
		if (problem != null)
		{
			response.setReturnCode(ReturnCodes.malformedRequest);
			response.setMessage(problem);
			return response;
		}

		ILocale locale = esb.getLocale();

		response.setLanguageCode(locale.getLanguage(request.getLanguageID()));
		response.setName(locale.getLanguageName(request.getLanguageID()));
		response.setAlphabet(locale.getAlpabet(request.getLanguageID()));
		response.setDateFormat(locale.getDateFormat(request.getLanguageID()));
		response.setEncodingScheme(locale.getEncodingScheme(request.getLanguageID()));
		response.setCurrencyDecimalDigits(locale.getCurrencyDecimalDigits());
		response.setCurrencyCode(locale.getCurrencyCode());

		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Device Info
	//
	// /////////////////////////////////
	@Override
	public GetDeviceInfoResponse getDeviceInfo(GetDeviceInfoRequest request)
	{
		GetDeviceInfoResponse response = new GetDeviceInfoResponse(request);
		response.setReturnCode(ReturnCodes.success);

		response.setMSISDN("27848654805");
		response.setRegistrationShortCode("27824452655");

		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Contact Info
	//
	// /////////////////////////////////

	@Override
	public UpdateContactInfoResponse updateContactInfo(UpdateContactInfoRequest request)
	{
		VasService service = getVasService(request.getServiceID());
		if (service != null)
			return service.updateContactInfo(new ServiceContext(), request);
		return new UpdateContactInfoResponse(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	@Override
	public VasService getVasService(String serviceID)
	{
		if (serviceID == null)
			return null;
		String key = serviceID.toUpperCase();
		VasService result = serviceMap.get(key);
		if (result != null)
			return result;

		List<VasService> services = esb.getServices(VasService.class);
		Map<String, VasService> map = new HashMap<String, VasService>();
		for (VasService service : services)
		{
			map.put(service.getServiceID().toUpperCase(), service);
		}
		serviceMap = map;
		return serviceMap.get(key);
	}

	@Override
	public SendSMSResponse sendSMS(SendSMSRequest request)
	{
		SendSMSResponse response = new SendSMSResponse(request);
		ISmsConnector smsConnector = esb.getFirstConnector(ISmsConnector.class);

		String languageCode = esb.getLocale().getLanguage(request.getLanguageID());
		ISmsResponse smsResponse = smsConnector.sendRequest(request.getSourceAddress(), request.getSubscriberNumber().getAddressDigits(), new NotificationText(request.getMessage(), languageCode));
		if (smsResponse == null)
		{
			return response;
		}

		response.setMessageID(smsResponse.getMessageID());
		response.setSequenceNumber(smsResponse.getSequenceNumber());
		response.setReturnCode(ReturnCodes.success);
		response.setResultMessage("Success");
		return response;
	}

}
