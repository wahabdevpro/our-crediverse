package com.concurrent.hxc;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.xml.ws.BindingType;

import hxc.connectors.vas.VasService;

@WebService
@SOAPBinding(style = Style.RPC, use = Use.LITERAL, parameterStyle = ParameterStyle.WRAPPED)
@BindingType(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
public interface IHxC
{
	@WebMethod
	public PingResponse ping(@WebParam(name = "request") PingRequest request);

	@WebMethod
	public GetServicesResponse getServices(@WebParam(name = "request") GetServicesRequest request);

	@WebMethod
	public GetServiceResponse getService(@WebParam(name = "request") GetServiceRequest request);

	@WebMethod
	public SubscribeResponse subscribe(@WebParam(name = "request") SubscribeRequest request);

	@WebMethod
	public UnsubscribeResponse unsubscribe(@WebParam(name = "request") UnsubscribeRequest request);

	@WebMethod
	public ExtendResponse extend(@WebParam(name = "request") ExtendRequest request);

	@WebMethod
	public SuspendResponse suspend(@WebParam(name = "request") SuspendRequest request);

	@WebMethod
	public UnsuspendResponse unsuspend(@WebParam(name = "request") UnsuspendRequest request);

	@WebMethod
	public MigrateResponse migrate(@WebParam(name = "request") MigrateRequest request);

	@WebMethod
	public GetMembersResponse getMembers(@WebParam(name = "request") GetMembersRequest request);

	@WebMethod
	public GetOwnersResponse getOwners(@WebParam(name = "request") GetOwnersRequest request);

	@WebMethod
	public AddMemberResponse addMember(@WebParam(name = "request") AddMemberRequest request);

	@WebMethod
	public ReplaceMemberResponse replaceMember(@WebParam(name = "request") ReplaceMemberRequest request);

	@WebMethod
	public RemoveMemberResponse removeMember(@WebParam(name = "request") RemoveMemberRequest request);

	@WebMethod
	public RemoveMembersResponse removeMembers(@WebParam(name = "request") RemoveMembersRequest request);

	@WebMethod
	public GetBalancesResponse getBalances(@WebParam(name = "request") GetBalancesRequest request);

	@WebMethod
	public GetStatusResponse getStatus(@WebParam(name = "request") GetStatusRequest request);

	@WebMethod
	public GetQuotasResponse getQuotas(@WebParam(name = "request") GetQuotasRequest request);

	@WebMethod
	public AddQuotaResponse addQuota(@WebParam(name = "request") AddQuotaRequest request);

	@WebMethod
	public ChangeQuotaResponse changeQuota(@WebParam(name = "request") ChangeQuotaRequest request);

	@WebMethod
	public RemoveQuotaResponse removeQuota(@WebParam(name = "request") RemoveQuotaRequest request);

	@WebMethod
	public TransferResponse transfer(@WebParam(name = "request") TransferRequest request);

	@WebMethod
	public ProcessResponse process(@WebParam(name = "request") ProcessRequest request);

	@WebMethod
	public ProcessLifecycleEventResponse processLifecycleEvent(@WebParam(name = "request") ProcessLifecycleEventRequest request);

	@WebMethod
	public SendSMSResponse sendSMS(@WebParam(name = "request") SendSMSRequest request);

	@WebMethod
	public ResetPINResponse resetPIN(@WebParam(name = "request") ResetPINRequest request);

	@WebMethod
	public ChangePINResponse changePIN(@WebParam(name = "request") ChangePINRequest request);

	@WebMethod
	public ValidatePINResponse validatePIN(@WebParam(name = "request") ValidatePINRequest request);

	@WebMethod
	public RedeemResponse redeem(@WebParam(name = "request") RedeemRequest request);

	@WebMethod
	public GetReturnCodeTextResponse getReturnCodeText(@WebParam(name = "request") GetReturnCodeTextRequest request);

	@WebMethod
	public GetLocaleSettingsResponse getLocaleSettings(@WebParam(name = "request") GetLocaleSettingsRequest request);

	@WebMethod
	public GetCreditTransfersResponse getCreditTransfers(@WebParam(name = "request") GetCreditTransfersRequest request);

	@WebMethod
	public AddCreditTransferResponse addCreditTransfer(@WebParam(name = "request") AddCreditTransferRequest request);

	@WebMethod
	public RemoveCreditTransfersResponse removeCreditTransfers(@WebParam(name = "request") RemoveCreditTransfersRequest request);

	@WebMethod
	public SuspendCreditTransferResponse suspendCreditTransfer(@WebParam(name = "request") SuspendCreditTransferRequest request);

	@WebMethod
	public ResumeCreditTransferResponse resumeCreditTransfer(@WebParam(name = "request") ResumeCreditTransferRequest request);

	@WebMethod
	public ChangeCreditTransferResponse changeCreditTransfer(@WebParam(name = "request") ChangeCreditTransferRequest request);

	@WebMethod
	public GetLocationResponse getLocation(@WebParam(name = "request") GetLocationRequest request);

	@WebMethod
	public GetDeviceInfoResponse getDeviceInfo(@WebParam(name = "request") GetDeviceInfoRequest request);

	@WebMethod
	public UpdateContactInfoResponse updateContactInfo(@WebParam(name = "request") UpdateContactInfoRequest request);

	// Non-Web Methods
	@WebMethod(exclude = true)
	public VasService getVasService(String serviceID);

	@WebMethod(exclude = true)
	public GetServicesResponse getServicesEx(IServiceContext context, GetServicesRequest request);

}
