package hxc.services.caisim.engine.cai;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.services.caisim.utils.QuotedTokenizer;
import hxc.utils.protocol.caisim.CaiCommon;
import hxc.utils.protocol.caisim.Cfnrc;
import hxc.utils.protocol.caisim.HlrSubscription;
import hxc.utils.protocol.caisim.PdpContext;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.tcp.TcpResponse;

public class HlrHandler extends BaseHandler
{
	final static Logger logger = LoggerFactory.getLogger(HlrHandler.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	
	// HLR Parameters
	private static final String OBR = "OBR";
	private static final String RSA = "RSA";
	private static final String CFNRC = "CFNRC";
	private static final String NAM = "NAM";
	private static final String PDPCP = "PDPCP";
	private static final String GPRS = "GPRS";
	private static final String PDPCONTEXT = "PDPCONTEXT";
	private static final String APNID = "APNID";
	private static final String EQOSID = "EQOSID";
	private static final String PDPADD = "PDPADD";
	private static final String PDPID = "PDPID";
	private static final String VPAA = "VPAA";
	private static final String PDPTY = "PDPTY";
	private static final String PDPCH = "PDPCH";
	private static final String MSISDN = "MSISDN";
	private static final String IMEISV = "IMEISV";
	
	// MML Commands
	public static final String HGPDI = "HGPDI";
	public static final String HGPDE = "HGPDE";
	
	private static final int PDP_CONTEXT_SET_MANDATORY_PARAMS_COUNT = 2;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	
	public HlrHandler(ICaiData caiData)
	{
		super(caiData);
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////
	
	public TcpResponse handleCai(String[] parsedRequest)
	{
		// Expect at least SET/GET:COMMAND_TYPE:MSISDN,MSISDN_VALUE
		if (parsedRequest.length < 3)
			return getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND);
		
		String command = parsedRequest[0];
		String commandType = parsedRequest[1];
		
		String[] parsedSubAttributes = QuotedTokenizer.tokenize(parsedRequest[2], CaiCommon.SUB_ATTRIBUTE_DELIMITER);
		if (parsedSubAttributes.length < 2)
			return getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND);
		
		String msisdnTag = parsedSubAttributes[0];
		String msisdn = parsedSubAttributes[1];
		
		if (!msisdnTag.equals("MSISDN"))
			return getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND);
		
		if (command.equals("GET") && commandType.equals("HLRSUB"))
			return handleGet(msisdn, parsedRequest);
		else if (command.equals("SET") && commandType.equals("HLRSUB"))
		{
			// Expect at least SET/GET:COMMAND_TYPE:SAPCSUBID,MSISDN:GROUPS,...
			if (parsedRequest.length < 4)
				return getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND);
			
			synchronized (getCaiData().getLock())
			{
				Subscriber sub = getCaiData().getSubscriber(msisdn, SubscriptionType.HLR);
				if (sub == null)
					return getFailureResponse(Protocol.RESPONSE_CODE_SUBSCRIBER_DATA_NOT_RECOGNIZED);
				
				HlrSubscription hlrSub = (HlrSubscription) sub.getHlrSubscription().clone();
				
				for (int i = 3; i < parsedRequest.length; ++i)
				{
					ReturnValue ret = handleSet(hlrSub, QuotedTokenizer.tokenize(parsedRequest[i], CaiCommon.SUB_ATTRIBUTE_DELIMITER));
					
					if (ret.hasTcpResponse() && !ret.isSuccessful())
						return ret.getTcpResponse();
				}
				
				sub.setHlrSubscription(hlrSub);
				return getSuccessResponse();
			}
		}
		
		return getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND);
	}
	
	public TcpResponse handleMml(String[] parsedRequest)
	{
		if (parsedRequest.length < 2)
			return getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND);
		
		String msisdn = new String();
		String apnId = new String();
		String pdpId = new String();
		String eqosId = new String();
		
		String[] parsedValue = QuotedTokenizer.tokenize(parsedRequest[1], ',');
		for (String value : parsedValue)
		{
			String[] paramNameValuePair = value.split("=");
			if (paramNameValuePair.length != 2)
				return getFailureResponse(Protocol.RESPONSE_CODE_INVALID_ARGUMENT_OR_OUT_OF_RANGE);
			
			String paramName = paramNameValuePair[0];
			String paramValue = paramNameValuePair[1];
			
			switch (paramName)
			{
				case MSISDN:
					msisdn = paramValue;
					break;
				case APNID:
					apnId = paramValue;
					break;
				case PDPID:
					pdpId = paramValue;
					break;
				case EQOSID:
					eqosId = paramValue;
					break;
			}
		}
		
		if (msisdn.isEmpty())
			return getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND);
		
		synchronized (getCaiData().getLock())
		{
			Subscriber sub = getCaiData().getSubscriber(msisdn, SubscriptionType.HLR);
			if (sub == null)
				return getFailureResponse(Protocol.RESPONSE_CODE_SUBSCRIBER_DATA_NOT_RECOGNIZED);

			String command = parsedRequest[0];
			switch (command)
			{
				case HGPDI:
				{
					if (apnId.isEmpty() || eqosId.isEmpty())
						return getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND);
					
					PdpContext newPdp = new PdpContext();
					
					newPdp.setApnId(Integer.parseInt(apnId));
					newPdp.setEqosId(Integer.parseInt(eqosId));
					
					sub.getHlrSubscription().addPdpContext(newPdp);
					break;
				}
				case HGPDE:
					if (pdpId.isEmpty())
						return getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND);
					
					sub.getHlrSubscription().deletePdpContextByPdpId(Integer.parseInt(pdpId));
					break;
				default:
					return getFailureResponse(Protocol.RESPONSE_CODE_OPERATION_NOT_SUPPORTED);
			}
		}
		
		return getSuccessResponse();
	}
	
	private TcpResponse handleGet(String msisdn, String[] parsedRequest)
	{
		String ret = "MSISDN," + msisdn;
		
		ICaiData caiData = getCaiData();
		
		synchronized (caiData.getLock())
		{
			Subscriber sub = caiData.getSubscriber(msisdn, SubscriptionType.HLR);
			if (sub == null)
				return getFailureResponse(Protocol.RESPONSE_CODE_SUBSCRIBER_DATA_NOT_RECOGNIZED);
			
			HlrSubscription hlrSub = sub.getHlrSubscription();
			
			ArrayList<String> attributes = new ArrayList<String>();
			
			for (int i = 3; i < parsedRequest.length; ++i)
			{
				attributes.add(parsedRequest[i]);
			}
			
			if (attributes.isEmpty())
			{
				attributes.add(OBR);
				attributes.add(RSA);
				attributes.add(CFNRC);
				attributes.add(NAM);
				attributes.add(PDPCP);
				attributes.add(GPRS);
				attributes.add(IMEISV);
			}
			
			for (String attribute : attributes)
			{
				switch (attribute)
				{
					case OBR:
						ret += ":" + OBR + "," + hlrSub.getObr();
						break;
					case RSA:
						ret += ":" + RSA + "," + hlrSub.getRsa();
						break;
					case CFNRC:
						if (hlrSub.getCfnrc().getProvisionState() == 0)
							ret += ":" + CFNRC + ",0";
						else if (hlrSub.getCfnrc().getActivationState() == 0)
							ret += ":" + CFNRC + ",1,0," + hlrSub.getCfnrc().getCategory();
						else
							ret += ":" + CFNRC + ",1,1," + hlrSub.getCfnrc().getNumber() + "," + hlrSub.getCfnrc().getCategory();
						break;
					case NAM:
						ret += ":" + NAM + "," + hlrSub.getNam();
						break;
					case PDPCP:
						ret += ":" + PDPCP + "," + hlrSub.getPdpCp();
						break;
					case GPRS:
					{
						PdpContext[] pdps = hlrSub.getPdpContexts();
						
						if (pdps.length != 0)
						{
							ret += ":" + GPRS;
							
							for (PdpContext pdp : pdps)
							{
								ret += "," + PDPCONTEXT + "," +
										APNID + "," + pdp.getApnId() + "," +
										EQOSID + "," + pdp.getEqosId() + "," +
										(pdp.getPdpAddress() == null || pdp.getPdpAddress().isEmpty() ? "" : PDPADD + "," + CaiCommon.escapeValue(pdp.getPdpAddress()) + ",") +
										PDPID + "," + pdp.getPdpId() + "," +
										VPAA + "," + pdp.getVpaa() + "," +
										PDPTY + "," + pdp.getPdpTy() + "," +
										PDPCH + "," + CaiCommon.escapeValue(pdp.getPdpCh());
							}
						}
						
						break;
					}
					case IMEISV:
					{
						ret += ":" + IMEISV + "," + hlrSub.getImei();
						break;
					}
				}
			}
		}
		
		return getSuccessResponse(ret);
	}
	
	private ReturnValue handleSet(HlrSubscription hlrSub, String[] subAttributes)
	{
		// Expect a subAttribute with e value, e.g. OBR,0
		if (subAttributes.length < 2)
			return new ReturnValue(getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND));
		
		try
		{
			String parameter = subAttributes[0];
			switch (parameter)
			{
				case OBR:
					hlrSub.setObr(Integer.parseInt(subAttributes[1]));
					break;
				case RSA:
					hlrSub.setRsa(Integer.parseInt(subAttributes[1]));
					break;
				case CFNRC:
					switch (subAttributes.length)
					{
						// Only expect CFNRC,0 - e.g. Deprovisioning
						case 2:
							// Set to empty CFNRC
							if (subAttributes[1] == "0")
								hlrSub.setCfnrc(new Cfnrc());
							else
								return new ReturnValue(getFailureResponse(Protocol.RESPONSE_CODE_INVALID_ARGUMENT_OR_OUT_OF_RANGE));
							break;
						// Expect the following 4-parameter cases:
						//  - CFNRC,1,0,TS10 - Deactivation without keeping the number
						//  - CFNRC,1,1,TS10 - Activation without a number (if already has a number)
						case 4:
						{
							final int provisioningState = Integer.parseInt(subAttributes[1]);
							if (provisioningState != 1)
								return new ReturnValue(getFailureResponse(Protocol.RESPONSE_CODE_INVALID_ARGUMENT_OR_OUT_OF_RANGE));
							
							final int activationState = Integer.parseInt(subAttributes[2]);
							
							// Deactivation doesn't keep the number
							if (activationState < 1)
								hlrSub.getCfnrc().setNumber(new String());
							
							// Don't allow Activation in the form of CFNRC,1,1,TS10 - if doesn't already have a number
							if (activationState > 0 && !hlrSub.getCfnrc().hasNumber())
								return new ReturnValue(getFailureResponse(Protocol.RESPONSE_CODE_INVALID_ARGUMENT_OR_OUT_OF_RANGE));
							
							hlrSub.getCfnrc().setProvisionState(provisioningState);
							hlrSub.getCfnrc().setActivationState(activationState);
							hlrSub.getCfnrc().setCategory(subAttributes[3]);
							
							break;
						}
						// And the following 5-parameter ones:
						//  - CFNRC,1,0,KEEP,TS10 - Deactivation with keeping the number
						//  - CFNRC,1,0,12345,TS10 - Deactivation with setting the number
						//  - CFNRC,1,1,12345,TS10 - Activation with a number
						case 5:
						{
							final int provisioningState = Integer.parseInt(subAttributes[1]);
							if (provisioningState != 1)
								return new ReturnValue(getFailureResponse(Protocol.RESPONSE_CODE_INVALID_ARGUMENT_OR_OUT_OF_RANGE));
							
							final int activationState = Integer.parseInt(subAttributes[2]);
							
							String number = subAttributes[3];

							// Can only use KEEP for Deactivation, dont' allow for Activation
							if (number.equals("KEEP") && activationState > 0)
								return new ReturnValue(getFailureResponse(Protocol.RESPONSE_CODE_INVALID_ARGUMENT_OR_OUT_OF_RANGE));

							// Can set the number on both Activation and Deactivation
							if (!number.isEmpty() && !number.equals("KEEP"))
								hlrSub.getCfnrc().setNumber(number);
							
							hlrSub.getCfnrc().setProvisionState(provisioningState);
							hlrSub.getCfnrc().setActivationState(activationState);
							hlrSub.getCfnrc().setCategory(subAttributes[4]);
							
							break;
						}
						// Unknown format
						default:
							return new ReturnValue(getFailureResponse(Protocol.RESPONSE_CODE_INVALID_ARGUMENT_OR_OUT_OF_RANGE));
					}
					break;
				case NAM:
					hlrSub.setNam(Integer.parseInt(subAttributes[1]));
					break;
				case PDPCP:
					hlrSub.setPdpCp(Integer.parseInt(subAttributes[1]));
					break;
				// Expect at least: GPRS,DEF,PDPCONTEXT,APNID,X,EQOSID,Y - APNID and EQOSID are mandatory
				case GPRS:
				{
					if (subAttributes.length < 7)
						return new ReturnValue(getFailureResponse(Protocol.RESPONSE_CODE_INSUFFICIENT_PARAMETERS));
					
					String action = subAttributes[1];
					String pdpTag = subAttributes[2];
					if (!action.equals("DEF") || !pdpTag.equals(PDPCONTEXT))
						break;
					
					PdpContext newPdp = new PdpContext();
					
					int mandatoryParamsCount = 0;
					
					for (int i = 3; i + 1 < subAttributes.length; i += 2)
					{
						String pdpParameter = subAttributes[i];
						String pdpParameterValue = CaiCommon.removeQuotationAndEscaping(subAttributes[i + 1]);
						
						switch (pdpParameter)
						{
							case APNID:
								newPdp.setApnId(Integer.parseInt(pdpParameterValue));
								++mandatoryParamsCount;
								break;
							case EQOSID:
								newPdp.setEqosId(Integer.parseInt(pdpParameterValue));
								++mandatoryParamsCount;
								break;
							case PDPADD:
								newPdp.setPdpAddress(pdpParameterValue);
								break;
							case PDPID:
								newPdp.setPdpId(Integer.parseInt(pdpParameterValue));
								break;
							case VPAA:
								newPdp.setVpaa(Integer.parseInt(pdpParameterValue));
								break;
							case PDPTY:
								if (pdpParameterValue.equals("IPv4"))
									newPdp.setPdpTy(PdpContext.PdpType.IPv4);
								else if (pdpParameterValue.equals("IPv6"))
									newPdp.setPdpTy(PdpContext.PdpType.IPv6); 
								break;
							case PDPCH:
								newPdp.setPdpCh(pdpParameterValue);
								break;
						}
					}
					
					if (mandatoryParamsCount != PDP_CONTEXT_SET_MANDATORY_PARAMS_COUNT)
						return new ReturnValue(getFailureResponse(Protocol.RESPONSE_CODE_INSUFFICIENT_PARAMETERS));
					
					hlrSub.addPdpContext(newPdp);
					
					break;
				}
				case IMEISV:
					hlrSub.setImei(subAttributes[1]);
					break;
			}
		}
		catch(Exception e)
		{
			return new ReturnValue(getFailureResponse(Protocol.RESPONSE_CODE_INVALID_ARGUMENT_OR_OUT_OF_RANGE));
		}
		
		return new ReturnValue(getSuccessResponse(), true);
	}
}
