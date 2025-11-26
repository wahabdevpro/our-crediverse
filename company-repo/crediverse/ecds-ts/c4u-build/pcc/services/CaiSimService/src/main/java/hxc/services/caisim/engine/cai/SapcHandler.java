package hxc.services.caisim.engine.cai;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.SubscriptionType;
import hxc.services.caisim.model.Subscriber;
import hxc.services.caisim.utils.QuotedTokenizer;
import hxc.utils.protocol.caisim.CaiCommon;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.protocol.caisim.SapcGroup;
import hxc.utils.protocol.caisim.SapcGroupId;
import hxc.utils.protocol.caisim.SapcSubscription;
import hxc.utils.tcp.TcpResponse;

/**
 * Handles SAPC-specific CAI requests.
 * 
 * @author petar
 *
 */
public class SapcHandler extends BaseHandler
{
	final static Logger logger = LoggerFactory.getLogger(SapcHandler.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	
	public SapcHandler(ICaiData caiData)
	{
		super(caiData);
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////
	
	public TcpResponse handle(String[] parsedRequest)
	{
		// Expect at least SET/GET:COMMAND_TYPE:SAPCSUBID,MSISDN
		if (parsedRequest.length < 3)
			return getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND);
		
		String command = parsedRequest[0];
		String commandType = parsedRequest[1];
		
		String[] parsedSubAttributes = QuotedTokenizer.tokenize(parsedRequest[2], CaiCommon.SUB_ATTRIBUTE_DELIMITER);
		if (parsedSubAttributes.length < 2)
			return getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND);
		
		String sapcSubId = parsedSubAttributes[0];
		String msisdn = parsedSubAttributes[1];
		
		if (!sapcSubId.equals("SUBID"))
			return getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND);
		
		if (command.equals("GET") && commandType.equals(CommonCaiHandler.SAPCUSAGE_CMD))
			return handleGetQuota(msisdn);
		else if (command.equals("SET") &&
				(commandType.equals(CommonCaiHandler.SAPCSUB_CMD) || commandType.equals(CommonCaiHandler.ZAINSAPCSV_CMD)))
		{
			// Expect at least SET/GET:COMMAND_TYPE:SAPCSUBID,MSISDN:GROUPS,...
			if (parsedRequest.length < 4)
				return getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND);
			
			synchronized (getCaiData().getLock())
			{
				Subscriber sub = getCaiData().getSubscriber(msisdn, SubscriptionType.SAPC);
				if (sub == null)
					return getFailureResponse(Protocol.RESPONSE_CODE_SUBSCRIBER_DATA_NOT_RECOGNIZED);
				
				SapcSubscription sapcSub = (SapcSubscription) sub.getSapcSubscription().clone();

				// handle ZAIN-specific parameters before generic SAPC
				boolean zainLte = false;
				if (commandType.equals(CommonCaiHandler.ZAINSAPCSV_CMD))
					zainLte = zainLte(parsedRequest);
				
				for (int i = 3; i < parsedRequest.length; ++i)
				{
					ReturnValue ret = handleSapcSub(sapcSub, QuotedTokenizer.tokenize(parsedRequest[i], CaiCommon.SUB_ATTRIBUTE_DELIMITER), zainLte);

					if (ret.hasTcpResponse() && !ret.isSuccessful())
						return ret.getTcpResponse();
				}
				
				sub.setSapcSubscription(sapcSub);
				return getSuccessResponse();
			}
		}
		
		return getFailureResponse(Protocol.RESPONSE_CODE_OPERATION_NOT_SUPPORTED);
	}
	
	private boolean zainLte(String[] parsedRequest)
	{
		for (int i = 3; i < parsedRequest.length; ++i)
		{
			switch (parsedRequest[i])
			{
				case "LTE,TRUE":
					return true;
				case "LTE,FALSE":
					return false;
			}
		}
		
		return false;
	}
	
	private ReturnValue handleSapcSub(SapcSubscription sapcSub, String[] subAttributes, boolean zainLte)
	{
		if (subAttributes.length < 1)
			return new ReturnValue(getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND));
		
		String groups = subAttributes[0];
		if (!groups.equals("GROUPS"))
			return new ReturnValue();
		
		int i = 1;
		while (i < subAttributes.length)
		{
			String val = subAttributes[i];

			// DEL,GROUPID,"GROUPID_VAL"
			if (val.equals("DEL"))
			{
				if (i + 2 < subAttributes.length)
				{
					String groupIdTag = subAttributes[i + 1];
					if (!groupIdTag.equals("GROUPID"))
						return new ReturnValue(getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND));

					String groupId = CaiCommon.removeQuotationAndEscaping(subAttributes[i + 2]);

					sapcSub.deleteGroup(new SapcGroupId(groupId));
				}

				i += 3;
			}
			// DEF|SET,GROUPID,"GROUPID_VAL",PRIORITY,"PRIORITY_VAL",STARTDATE,"STARTDATE_VAL",ENDDATE,"ENDDATE_VAL"
			else if (val.equals("DEF") || val.equals("SET"))
			{
				if (i + 8 < subAttributes.length)
				{
					String groupIdTag = subAttributes[i + 1];
					if (!groupIdTag.equals("GROUPID"))
						return new ReturnValue(getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND));
					String groupId = CaiCommon.removeQuotationAndEscaping(subAttributes[i + 2]);

					String priorityTag = subAttributes[i + 3];
					if (!priorityTag.equals("PRIORITY"))
						return new ReturnValue(getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND));
					int priority = 0;
					try
					{
						priority = Integer.parseInt(subAttributes[i + 4]);
					}
					catch(Exception e)
					{
						return new ReturnValue(getFailureResponse(Protocol.RESPONSE_CODE_INVALID_ARGUMENT_OR_OUT_OF_RANGE));
					}

					String startDateTag = subAttributes[i + 5];
					if (!startDateTag.equals("STARTDATE"))
						return new ReturnValue(getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND));

					String endDateTag = subAttributes[i + 7];
					if (!endDateTag.equals("ENDDATE"))
						return new ReturnValue(getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND));

					DateFormat df = new SimpleDateFormat(CaiCommon.CAI_DATE_FORMAT);
					Date startDate = null;
					Date endDate = null;
					try
					{
						startDate = df.parse(CaiCommon.removeQuotationAndEscaping(subAttributes[i + 6]));
						endDate = df.parse(CaiCommon.removeQuotationAndEscaping(subAttributes[i + 8]));
					}
					catch (Exception e)
					{
						return new ReturnValue(getFailureResponse(Protocol.RESPONSE_CODE_INVALID_ARGUMENT_OR_OUT_OF_RANGE));
					}

					sapcSub.addGroup(new SapcGroup(groupId, priority, startDate, endDate, zainLte), true);
				}

				i += 9;
			}
			else
				return new ReturnValue(getFailureResponse(Protocol.RESPONSE_CODE_OPERATION_NOT_SUPPORTED));
		}

		return new ReturnValue(getSuccessResponse(), true);
	}
	
	private TcpResponse handleGetQuota(String msisdn)
	{
		String ret = "SUBID," + msisdn;
		
		ICaiData caiData = getCaiData();
		
		synchronized (caiData.getLock())
		{
			Subscriber sub = caiData.getSubscriber(msisdn, SubscriptionType.SAPC);
			if (sub == null)
				return getFailureResponse(Protocol.RESPONSE_CODE_SUBSCRIBER_DATA_NOT_RECOGNIZED);
			
			SapcSubscription cai = sub.getSapcSubscription();
			SapcGroup[] groups = cai.getGroups();
			if (groups.length != 0)
				ret += ":DATA,NAME,\"NAME_VAL\",SUBSCRIPTIONDATE,\"01-01-2015T00:00:00\",";
			for (SapcGroup group : groups)
			{
				ret += group.toString();
				ret += ",";
			}
			
			// remove the ending ',' character
			if (groups.length != 0)
				ret = ret.substring(0, ret.length() - 1);
		}
		
		return getSuccessResponse(ret);
	}
}
