package hxc.services.caisim;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.xml.ws.BindingType;

import hxc.services.caisim.model.Subscriber;
import hxc.utils.protocol.caisim.request.soap.AddHlrSubscriberPdpContextsRequest;
import hxc.utils.protocol.caisim.request.soap.AddHlrSubscriberRequest;
import hxc.utils.protocol.caisim.request.soap.AddSapcGroupsRequest;
import hxc.utils.protocol.caisim.request.soap.AddSapcQuotaRequest;
import hxc.utils.protocol.caisim.request.soap.AddSapcSubscriberRequest;
import hxc.utils.protocol.caisim.request.soap.DeleteHlrSubscriberPdpContextsRequest;
import hxc.utils.protocol.caisim.request.soap.DeleteSapcGroupsRequest;
import hxc.utils.protocol.caisim.request.soap.GetHlrSubscriberRequest;
import hxc.utils.protocol.caisim.request.soap.GetSapcGroupRequest;
import hxc.utils.protocol.caisim.request.soap.GetSapcGroupsRequest;
import hxc.utils.protocol.caisim.request.soap.GetSapcSubscriberRequest;
import hxc.utils.protocol.caisim.request.soap.SetHlrSubscriberImeiRequest;
import hxc.utils.protocol.caisim.request.soap.SetHlrSubscriberNamRequest;
import hxc.utils.protocol.caisim.request.soap.SetHlrSubscriberObrRequest;
import hxc.utils.protocol.caisim.request.soap.SetHlrSubscriberPdpCpRequest;
import hxc.utils.protocol.caisim.request.soap.SetHlrSubscriberRsaRequest;
import hxc.utils.protocol.caisim.request.soap.SetSapcQuotaRequest;
import hxc.utils.protocol.caisim.request.soap.SetSapcZainLteRequest;
import hxc.utils.protocol.caisim.request.soap.UpdateHlrSubscriberPdpContextsRequest;
import hxc.utils.protocol.caisim.request.soap.UpdateSapcGroupsRequest;
import hxc.utils.protocol.caisim.response.soap.AddHlrSubscriberPdpContextsResponse;
import hxc.utils.protocol.caisim.response.soap.AddHlrSubscriberResponse;
import hxc.utils.protocol.caisim.response.soap.AddSapcGroupsResponse;
import hxc.utils.protocol.caisim.response.soap.AddSapcQuotaResponse;
import hxc.utils.protocol.caisim.response.soap.AddSapcSubscriberResponse;
import hxc.utils.protocol.caisim.response.soap.DeleteHlrSubscriberPdpContextsResponse;
import hxc.utils.protocol.caisim.response.soap.DeleteSapcGroupsResponse;
import hxc.utils.protocol.caisim.response.soap.GetHlrSubscriberResponse;
import hxc.utils.protocol.caisim.response.soap.GetSapcGroupResponse;
import hxc.utils.protocol.caisim.response.soap.GetSapcGroupsResponse;
import hxc.utils.protocol.caisim.response.soap.GetSapcSubscriberResponse;
import hxc.utils.protocol.caisim.response.soap.SetHlrSubscriberImeiResponse;
import hxc.utils.protocol.caisim.response.soap.SetHlrSubscriberNamResponse;
import hxc.utils.protocol.caisim.response.soap.SetHlrSubscriberObrResponse;
import hxc.utils.protocol.caisim.response.soap.SetHlrSubscriberPdpCpResponse;
import hxc.utils.protocol.caisim.response.soap.SetHlrSubscriberRsaResponse;
import hxc.utils.protocol.caisim.response.soap.SetSapcQuotaResponse;
import hxc.utils.protocol.caisim.response.soap.SetSapcZainLteResponse;
import hxc.utils.protocol.caisim.response.soap.UpdateHlrSubscriberPdpContextsResponse;
import hxc.utils.protocol.caisim.response.soap.UpdateSapcGroupsResponse;

@WebService
@SOAPBinding(style = Style.DOCUMENT, use = Use.LITERAL, parameterStyle = ParameterStyle.WRAPPED)
@BindingType(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
public interface ICaiSim
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// House Keeping
	//
	// /////////////////////////////////

	@WebMethod
	public abstract int ping(@WebParam(name = "seq") int seq);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Lifecycle Management
	//
	// /////////////////////////////////

	@WebMethod
	public abstract boolean start();

	@WebMethod
	public abstract void stop();

	@WebMethod
	public abstract void reset();
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// SAPC
	//
	// /////////////////////////////////
	
	@WebMethod
	public abstract AddSapcSubscriberResponse addSapcSubscriber(@WebParam(name = "request") AddSapcSubscriberRequest request);
	
	@WebMethod
	public abstract UpdateSapcGroupsResponse updateSapcGroups(@WebParam(name = "request") UpdateSapcGroupsRequest request);
	
	@WebMethod
	public abstract AddSapcGroupsResponse addSapcGroups(@WebParam(name = "request") AddSapcGroupsRequest request);
	
	@WebMethod
	public abstract DeleteSapcGroupsResponse deleteSapcGroups(@WebParam(name = "request") DeleteSapcGroupsRequest request);
	
	@WebMethod
	public abstract SetSapcQuotaResponse setSapcQuota(@WebParam(name = "request") SetSapcQuotaRequest request);
	
	@WebMethod
	public abstract AddSapcQuotaResponse addSapcQuota(@WebParam(name = "request") AddSapcQuotaRequest request);
	
	@WebMethod
	public abstract GetSapcGroupResponse getSapcGroup(@WebParam(name = "request") GetSapcGroupRequest request);
	
	@WebMethod
	public abstract GetSapcGroupsResponse getSapcGroups(@WebParam(name = "request") GetSapcGroupsRequest request);
	
	@WebMethod
	public abstract GetSapcSubscriberResponse getSapcSubscriber(@WebParam(name = "request") GetSapcSubscriberRequest request);
	
	@WebMethod
	public abstract SetSapcZainLteResponse setSapcZainLte(@WebParam(name = "request") SetSapcZainLteRequest request);
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// HLR
	//
	// /////////////////////////////////
	
	@WebMethod
	public abstract AddHlrSubscriberResponse addHlrSubscriber(@WebParam(name = "request") AddHlrSubscriberRequest request);
	
	@WebMethod
	public abstract GetHlrSubscriberResponse getHlrSubscriber(@WebParam(name = "request") GetHlrSubscriberRequest request);
	
	@WebMethod
	public abstract SetHlrSubscriberObrResponse setHlrSubscriberObr(@WebParam(name = "request") SetHlrSubscriberObrRequest request);
	
	@WebMethod
	public abstract SetHlrSubscriberImeiResponse setHlrSubscriberImei(@WebParam(name = "request") SetHlrSubscriberImeiRequest request);
	
	@WebMethod
	public abstract SetHlrSubscriberRsaResponse setHlrSubscriberRsa(@WebParam(name = "request") SetHlrSubscriberRsaRequest request);
	
	@WebMethod
	public abstract SetHlrSubscriberNamResponse setHlrSubscriberNam(@WebParam(name = "request") SetHlrSubscriberNamRequest request);
	
	@WebMethod
	public abstract SetHlrSubscriberPdpCpResponse setHlrSubscriberPdpCp(@WebParam(name = "request") SetHlrSubscriberPdpCpRequest request);
	
	@WebMethod
	public abstract UpdateHlrSubscriberPdpContextsResponse updateHlrSubscriberPdpContexts(@WebParam(name = "request") UpdateHlrSubscriberPdpContextsRequest request);
	
	@WebMethod
	public abstract AddHlrSubscriberPdpContextsResponse addHlrSubscriberPdpContexts(@WebParam(name = "request") AddHlrSubscriberPdpContextsRequest request);
	
	@WebMethod
	public abstract DeleteHlrSubscriberPdpContextsResponse deleteHlrSubscriberPdpContexts(@WebParam(name = "request") DeleteHlrSubscriberPdpContextsRequest request);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Other
	//
	// /////////////////////////////////

	@WebMethod(exclude = true)
	public abstract Subscriber getSubscriber(String msisdn, SubscriptionType type);
	
	@WebMethod
	public abstract void addAuthentication(@WebParam(name = "userId") String userId, @WebParam(name = "password") String password);
	
	@WebMethod
	public abstract void injectResponse(@WebParam(name = "caiCall") CaiCall caiCall, @WebParam(name = "responseCode") Integer responseCode);
	
	@WebMethod
	public abstract void injectSelectiveResponse(@WebParam(name = "caiCall") CaiCall caiCall,
			@WebParam(name = "responseCode") Integer responseCode,
			@WebParam(name = "failCount") Integer failCount,
			@WebParam(name = "skipCount") Integer skipCount);
}