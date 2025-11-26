package hxc.connectors.vas;

import java.util.HashMap;
import java.util.Map;

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
import com.concurrent.hxc.GetLocationRequest;
import com.concurrent.hxc.GetLocationResponse;
import com.concurrent.hxc.GetMembersRequest;
import com.concurrent.hxc.GetMembersResponse;
import com.concurrent.hxc.GetOwnersRequest;
import com.concurrent.hxc.GetOwnersResponse;
import com.concurrent.hxc.GetQuotasRequest;
import com.concurrent.hxc.GetQuotasResponse;
import com.concurrent.hxc.GetServiceRequest;
import com.concurrent.hxc.GetServiceResponse;
import com.concurrent.hxc.GetStatusRequest;
import com.concurrent.hxc.GetStatusResponse;
import com.concurrent.hxc.IServiceContext;
import com.concurrent.hxc.MigrateRequest;
import com.concurrent.hxc.MigrateResponse;
import com.concurrent.hxc.Number;
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
import com.concurrent.hxc.RequestHeader;
import com.concurrent.hxc.ResetPINRequest;
import com.concurrent.hxc.ResetPINResponse;
import com.concurrent.hxc.ResumeCreditTransferRequest;
import com.concurrent.hxc.ResumeCreditTransferResponse;
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

import hxc.connectors.Channels;
import hxc.connectors.lifecycle.ISubscription;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.services.notification.IPhrase;
import hxc.services.notification.ReturnCodeTexts;

public abstract class VasService
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ReturnCodeTexts[] customReturnCodesTexts = null;
	private Map<ReturnCodes, IPhrase> textMap = null;
	protected VasCommandParser commandParser;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public abstract String getServiceID();

	public abstract String getServiceName(String lanuageID);

	public VasCommandParser getCommandParser()
	{
		return commandParser;
	}

	public void setCommandParser(VasCommandParser commandParser)
	{
		this.commandParser = commandParser;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Soap Methods
	//
	// /////////////////////////////////
	public abstract VasServiceInfo[] getServiceInfo(IServiceContext context, Number subscriberNumber, String variantID, Integer languageID, boolean activeOnly, boolean suggested) throws Exception;

	public final GetServiceResponse getService(IServiceContext context, GetServiceRequest request)
	{
		GetServiceResponse response = new GetServiceResponse(request);
		response.setReturnCode(ReturnCodes.success);

		try
		{
			VasServiceInfo[] info = getServiceInfo(context, request.getSubscriberNumber(), request.getVariantID(), request.getLanguageID(), request.isActiveOnly(), request.isSuggested());
			response.setServiceInfo(info);
		}
		catch (Exception e)
		{
			response.setReturnCode(ReturnCodes.technicalProblem);
			String message = e.getMessage();
			response.setMessage(message == null || message.length() == 0 ? e.toString() : message);
		}

		return response;
	}

	public SubscribeResponse subscribe(IServiceContext context, SubscribeRequest request)
	{
		SubscribeResponse response = new SubscribeResponse(request);
		return response;
	}

	public UnsubscribeResponse unsubscribe(IServiceContext context, UnsubscribeRequest request)
	{
		UnsubscribeResponse response = new UnsubscribeResponse(request);
		return response;
	}

	public ExtendResponse extend(IServiceContext context, ExtendRequest request)
	{
		ExtendResponse response = new ExtendResponse(request);
		return response;
	}

	public SuspendResponse suspend(IServiceContext context, SuspendRequest request)
	{
		SuspendResponse response = new SuspendResponse(request);
		return response;
	}

	public UnsuspendResponse unsuspend(IServiceContext context, UnsuspendRequest request)
	{
		UnsuspendResponse response = new UnsuspendResponse(request);
		return response;
	}

	public MigrateResponse migrate(IServiceContext context, MigrateRequest request)
	{
		MigrateResponse response = new MigrateResponse(request);
		return response;
	}

	public GetMembersResponse getMembers(IServiceContext context, GetMembersRequest request)
	{
		GetMembersResponse response = new GetMembersResponse(request);
		return response;
	}

	public GetOwnersResponse getOwners(IServiceContext serviceContext, GetOwnersRequest request)
	{
		GetOwnersResponse response = new GetOwnersResponse(request);
		return response;
	}

	public AddMemberResponse addMember(IServiceContext context, AddMemberRequest request)
	{
		AddMemberResponse response = new AddMemberResponse(request);
		return response;
	}

	public ReplaceMemberResponse replaceMember(IServiceContext context, ReplaceMemberRequest request)
	{
		ReplaceMemberResponse response = new ReplaceMemberResponse(request);
		return response;
	}

	public RemoveMemberResponse removeMember(IServiceContext context, RemoveMemberRequest request)
	{
		RemoveMemberResponse response = new RemoveMemberResponse(request);
		return response;
	}

	public RemoveMembersResponse removeMembers(IServiceContext context, RemoveMembersRequest request)
	{
		RemoveMembersResponse response = new RemoveMembersResponse(request);
		return response;
	}

	public GetBalancesResponse getBalances(IServiceContext context, GetBalancesRequest request)
	{
		GetBalancesResponse response = new GetBalancesResponse(request);
		return response;
	}

	public GetStatusResponse getStatus(IServiceContext context, GetStatusRequest request)
	{
		GetStatusResponse response = new GetStatusResponse(request);
		return response;
	}

	public GetQuotasResponse getQuotas(IServiceContext context, GetQuotasRequest request)
	{
		GetQuotasResponse response = new GetQuotasResponse(request);
		return response;
	}

	public AddQuotaResponse addQuota(IServiceContext context, AddQuotaRequest request)
	{
		AddQuotaResponse response = new AddQuotaResponse(request);
		return response;
	}

	public ChangeQuotaResponse changeQuota(IServiceContext context, ChangeQuotaRequest request)
	{
		ChangeQuotaResponse response = new ChangeQuotaResponse(request);
		return response;
	}

	public RemoveQuotaResponse removeQuota(IServiceContext context, RemoveQuotaRequest request)
	{
		RemoveQuotaResponse response = new RemoveQuotaResponse(request);
		return response;
	}

	public TransferResponse transfer(IServiceContext context, TransferRequest request)
	{
		TransferResponse response = new TransferResponse(request);
		return response;
	}

	public ProcessResponse process(IServiceContext context, ProcessRequest request)
	{
		ProcessResponse response = new ProcessResponse(request);
		return response;
	}

	public ProcessLifecycleEventResponse processLifecycleEvent(IServiceContext context, ProcessLifecycleEventRequest request)
	{
		ProcessLifecycleEventResponse response = new ProcessLifecycleEventResponse(request);
		return response;
	}

	protected ProcessLifecycleEventResponse processLifecycleEvent(IServiceContext context, ISubscription subscription)
	{
		ProcessLifecycleEventRequest request = new ProcessLifecycleEventRequest(subscription);
		request.setCallerID("LCYCL");
		request.setChannel(Channels.INTERNAL);
		request.setHostName("LOCAL");
		request.setTransactionID("0");
		request.setSessionID("0");
		request.setVersion(RequestHeader.CURRENT_VERSION);
		request.setMode(RequestModes.normal);
		request.setLanguageID(null);

		return processLifecycleEvent(context, request);
	}

	public ResetPINResponse resetPIN(IServiceContext serviceContext, ResetPINRequest request)
	{
		ResetPINResponse response = new ResetPINResponse(request);
		return response;
	}

	public ChangePINResponse changePIN(IServiceContext serviceContext, ChangePINRequest request)
	{
		ChangePINResponse response = new ChangePINResponse(request);
		return response;
	}

	public ValidatePINResponse validatePIN(ServiceContext serviceContext, ValidatePINRequest request)
	{
		ValidatePINResponse response = new ValidatePINResponse(request);
		return response;
	}

	public String getReturnCodeText(String languageCode, ReturnCodes returnCode)
	{
		ReturnCodeTexts[] customReturnCodesTexts = getReturnCodeTexts();
		if (customReturnCodesTexts == null || customReturnCodesTexts.length == 0)
			return null;

		if (this.customReturnCodesTexts != customReturnCodesTexts)
		{
			this.customReturnCodesTexts = customReturnCodesTexts;
			textMap = new HashMap<ReturnCodes, IPhrase>();
			for (ReturnCodeTexts returnCodeText : customReturnCodesTexts)
			{
				textMap.put(returnCodeText.getReturnCode(), returnCodeText.getPhrase());
			}
		}

		IPhrase phrase = textMap.get(returnCode);
		if (phrase == null)
			return null;

		return phrase.get(languageCode);
	}

	protected ReturnCodeTexts[] getReturnCodeTexts()
	{
		return null;
	}

	public GetCreditTransfersResponse getCreditTransfers(IServiceContext serviceContext, GetCreditTransfersRequest request)
	{
		GetCreditTransfersResponse response = new GetCreditTransfersResponse(request);
		return response;
	}

	public AddCreditTransferResponse addCreditTransfer(IServiceContext serviceContext, AddCreditTransferRequest request)
	{
		AddCreditTransferResponse response = new AddCreditTransferResponse(request);
		return response;
	}

	public RemoveCreditTransfersResponse removeCreditTransfers(IServiceContext serviceContext, RemoveCreditTransfersRequest request)
	{
		RemoveCreditTransfersResponse response = new RemoveCreditTransfersResponse(request);
		return response;
	}

	public ChangeCreditTransferResponse changeCreditTransfer(IServiceContext serviceContext, ChangeCreditTransferRequest request)
	{
		ChangeCreditTransferResponse response = new ChangeCreditTransferResponse(request);
		return response;
	}

	public SuspendCreditTransferResponse suspendCreditTransfer(IServiceContext serviceContext, SuspendCreditTransferRequest request)
	{
		SuspendCreditTransferResponse response = new SuspendCreditTransferResponse(request);
		return response;
	}

	public ResumeCreditTransferResponse resumeCreditTransfer(IServiceContext serviceContext, ResumeCreditTransferRequest request)
	{
		ResumeCreditTransferResponse response = new ResumeCreditTransferResponse(request);
		return response;
	}

	public GetLocationResponse getLocation(ServiceContext serviceContext, GetLocationRequest request)
	{
		GetLocationResponse response = new GetLocationResponse(request);
		return response;
	}

	public UpdateContactInfoResponse updateContactInfo(ServiceContext serviceContext, UpdateContactInfoRequest request)
	{
		UpdateContactInfoResponse response = new UpdateContactInfoResponse(request);
		return response;
	}

	public RedeemResponse redeem(IServiceContext context, RedeemRequest request) 
	{
		RedeemResponse response = new RedeemResponse(request);
		return response;
	}

}
