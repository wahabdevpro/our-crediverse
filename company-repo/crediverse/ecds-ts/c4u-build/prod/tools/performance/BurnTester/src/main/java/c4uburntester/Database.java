/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package c4uburntester;

import com.concurrent.hxc.AddMemberRequest;
import com.concurrent.hxc.AddQuotaRequest;
import com.concurrent.hxc.ChangeQuotaRequest;
import com.concurrent.hxc.Channels;
import com.concurrent.hxc.ExtendRequest;
import com.concurrent.hxc.GetBalancesRequest;
import com.concurrent.hxc.GetHistoryRequest;
import com.concurrent.hxc.GetMembersRequest;
import com.concurrent.hxc.GetOwnersRequest;
import com.concurrent.hxc.GetQuotasRequest;
import com.concurrent.hxc.GetServiceRequest;
import com.concurrent.hxc.GetServicesRequest;
import com.concurrent.hxc.GetStatusRequest;
import com.concurrent.hxc.MigrateRequest;
import com.concurrent.hxc.NumberPlan;
import com.concurrent.hxc.NumberType;
import com.concurrent.hxc.PingRequest;
import com.concurrent.hxc.ProcessLifecycleEventRequest;
import com.concurrent.hxc.ProcessRequest;
import com.concurrent.hxc.RemoveMemberRequest;
import com.concurrent.hxc.RemoveMembersRequest;
import com.concurrent.hxc.RemoveQuotaRequest;
import com.concurrent.hxc.ReplaceMemberRequest;
import com.concurrent.hxc.RequestHeader;
import com.concurrent.hxc.RequestModes;
import com.concurrent.hxc.SendSMSRequest;
import com.concurrent.hxc.ServiceQuota;
import com.concurrent.hxc.SubscribeRequest;
import com.concurrent.hxc.SuspendRequest;
import com.concurrent.hxc.TransferRequest;
import com.concurrent.hxc.UnsubscribeRequest;
import com.concurrent.hxc.UnsuspendRequest;

/**
 *
 * @author justinguedes
 */
public class Database
{

	public final static String CALLER_ID = "callerID";
	public final static Channels CHANNEL = Channels.USSD;
	public final static String HOSTNAME = "host";
	public final static int LANGUAGE_ID = 1;
	public final static RequestModes REQUEST_MODE = RequestModes.NORMAL;
	public final static String SESSION_ID = "1";
	public final static String TRANSACTION_ID = "1";
	public final static String VERSION = "1";

	public final static int MAX_NUMBERS = 1000;
	private final static String ZEROS = "000000000";
	
	
	//////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	//////////////////////////////////

	public static com.concurrent.hxc.Number[] getNumbers()
	{
		com.concurrent.hxc.Number numbers[] = new com.concurrent.hxc.Number[MAX_NUMBERS];

		String subscriberMSISDN = null;
		String msisdnZEROS = null;

		//Build MSISDN and slot it into the array
		for (int i = 0; i < MAX_NUMBERS; i++)
		{
			numbers[i] = new com.concurrent.hxc.Number();

			msisdnZEROS = ZEROS.substring(0, ZEROS.length() - Integer.toString(i).length());
			subscriberMSISDN = String.format("083%s%s", msisdnZEROS, i);

			numbers[i].setAddressDigits(subscriberMSISDN);
			numbers[i].setNumberPlan(NumberPlan.UNKNOWN);
			numbers[i].setNumberType(NumberType.UNKNOWN);
		}

		return numbers;
	}

	public static AddMemberRequest generateAddMember(String serviceID, 
													String variantID,
													com.concurrent.hxc.Number subscriberMSISDN, 
													com.concurrent.hxc.Number number)
	{
		AddMemberRequest request = new AddMemberRequest();
		generateRequestHeader(request);

		request.setServiceID(serviceID);
		request.setVariantID(variantID);
		request.setSubscriberNumber(subscriberMSISDN);
		request.setMemberNumber(number);

		return request;
	}

	public static AddQuotaRequest generateAddQuota(String serviceID, 
													String variantID, 
													com.concurrent.hxc.Number subscriberMSISDN, 
													com.concurrent.hxc.Number number, 
													ServiceQuota quota)
	{
		AddQuotaRequest request = new AddQuotaRequest();
		generateRequestHeader(request);

		request.setServiceID(serviceID);
		request.setVariantID(variantID);
		request.setSubscriberNumber(subscriberMSISDN);
		request.setMemberNumber(number);
		request.setQuota(quota);

		return request;
	}

	public static ChangeQuotaRequest generateChangeQuota(String serviceID, 
														String variantID,
														com.concurrent.hxc.Number subscriberMSISDN, 
														com.concurrent.hxc.Number number, 
														ServiceQuota quota, 
														ServiceQuota newQuota)
	{
		ChangeQuotaRequest request = new ChangeQuotaRequest();
		generateRequestHeader(request);

		request.setServiceID(serviceID);
		request.setVariantID(variantID);
		request.setSubscriberNumber(subscriberMSISDN);
		request.setMemberNumber(number);
		request.setOldQuota(quota);
		request.setNewQuota(newQuota);

		return request;
	}

	public static ExtendRequest generateExtend()
	{
		ExtendRequest request = new ExtendRequest();
		generateRequestHeader(request);

		return request;
	}

	public static GetBalancesRequest generateGetBalances(String serviceID, 
														String variantID,
														com.concurrent.hxc.Number subscriberMSISDN, 
														boolean sms)
	{
		GetBalancesRequest request = new GetBalancesRequest();
		generateRequestHeader(request);

		request.setServiceID(serviceID);
		request.setVariantID(variantID);
		request.setSubscriberNumber(subscriberMSISDN);
		request.setRequestSMS(sms);

		return request;
	}

	public static GetHistoryRequest generateGetHistory(String serviceID, 
														String variantID,
														com.concurrent.hxc.Number numberA, 
														com.concurrent.hxc.Number numberB, 
														int rowLimit, 
														boolean inReverse)
	{
		GetHistoryRequest request = new GetHistoryRequest();
		generateRequestHeader(request);

		request.setServiceID(serviceID);
		request.setVariantID(variantID);
		request.setANumber(numberA);
		request.setBNumber(numberB);
		request.setRowLimit(rowLimit);
		request.setInReverse(inReverse);

		return request;
	}

	public static GetMembersRequest generateGetMembers(String serviceID, 
														String variantID,
														com.concurrent.hxc.Number subscriberMSISDN)
	{
		GetMembersRequest request = new GetMembersRequest();
		generateRequestHeader(request);

		request.setServiceID(serviceID);
		request.setVariantID(variantID);
		request.setSubscriberNumber(subscriberMSISDN);

		return request;
	}

	public static GetOwnersRequest generateGetOwners(String serviceID, 
													String variantID,
													com.concurrent.hxc.Number number)
	{
		GetOwnersRequest request = new GetOwnersRequest();
		generateRequestHeader(request);

		request.setServiceID(serviceID);
		request.setVariantID(variantID);
		request.setMemberNumber(number);

		return request;
	}

	public static GetQuotasRequest generateGetQuotas(String serviceID, 
													String variantID,
													com.concurrent.hxc.Number subscriberMSISDN, 
													boolean activeOnly, 
													String daysOfWeek, 
													String destination, 
													com.concurrent.hxc.Number number, 
													String service, 
													String timeOfDay)
	{
		GetQuotasRequest request = new GetQuotasRequest();
		generateRequestHeader(request);

		request.setServiceID(serviceID);
		request.setVariantID(variantID);
		request.setSubscriberNumber(subscriberMSISDN);
		request.setActiveOnly(activeOnly);
		request.setDaysOfWeek(daysOfWeek);
		request.setDestination(destination);
		request.setMemberNumber(number);
		request.setService(service);
		request.setTimeOfDay(timeOfDay);
		request.setActiveOnly(true);

		return request;
	}

	public static GetServiceRequest generateGetService(String serviceID, 
														String variantID,
														com.concurrent.hxc.Number subscriberMSISDN, 
														boolean activeOnly)
	{
		GetServiceRequest request = new GetServiceRequest();
		generateRequestHeader(request);

		request.setServiceID(serviceID);
		request.setVariantID(variantID);
		request.setSubscriberNumber(subscriberMSISDN);
		request.setActiveOnly(activeOnly);

		return request;
	}

	public static GetServicesRequest generateGetServices(com.concurrent.hxc.Number subscriberMSISDN, boolean activeOnly)
	{
		GetServicesRequest request = new GetServicesRequest();
		generateRequestHeader(request);

		request.setSubscriberNumber(subscriberMSISDN);
		request.setActiveOnly(activeOnly);

		return request;
	}

	public static GetStatusRequest generateGetStatus()
	{
		GetStatusRequest request = new GetStatusRequest();
		generateRequestHeader(request);

		return request;
	}

	public static MigrateRequest generateMigrate(String serviceID, 
												com.concurrent.hxc.Number subscriberMSISDN, 
												String newServiceID, 
												String currentVariantID, 
												String newVariantID)
	{
		MigrateRequest request = new MigrateRequest();
		generateRequestHeader(request);

		request.setServiceID(serviceID);
		request.setVariantID(currentVariantID);
		request.setSubscriberNumber(subscriberMSISDN);
		request.setNewServiceID(newServiceID);
		request.setNewVariantID(newVariantID);

		return request;
	}

	public static PingRequest generatePing(int seq)
	{
		PingRequest request = new PingRequest();

		request.setSeq(seq);

		return request;
	}

	public static ProcessLifecycleEventRequest generateProcessLifeCycleEvent(String serviceID, 
																			String variantID,
																			com.concurrent.hxc.Number subscriberMSISDN)
	{
		ProcessLifecycleEventRequest request = new ProcessLifecycleEventRequest();
		generateRequestHeader(request);

		request.setServiceID(serviceID);
		request.setVariantID(variantID);
		request.setMsisdn(subscriberMSISDN.getAddressDigits());

		return request;
	}

	public static ProcessRequest generateProcess(String serviceID, String variantID, int processID)
	{
		ProcessRequest request = new ProcessRequest();
		generateRequestHeader(request);

		request.setServiceID(serviceID);
		request.setVariantID(variantID);
		request.setProcessID(processID);

		return request;
	}

	public static RemoveMemberRequest generateRemoveMember(String serviceID, 
															String variantID,
															com.concurrent.hxc.Number subscriberMSISDN, 
															com.concurrent.hxc.Number number)
	{
		RemoveMemberRequest request = new RemoveMemberRequest();
		generateRequestHeader(request);

		request.setServiceID(serviceID);
		request.setVariantID(variantID);
		request.setSubscriberNumber(subscriberMSISDN);
		request.setMemberNumber(number);

		return request;
	}

	public static RemoveMembersRequest generateRemoveMembers(String serviceID, 
															String variantID,
															com.concurrent.hxc.Number subscriberMSISDN)
	{
		RemoveMembersRequest request = new RemoveMembersRequest();
		generateRequestHeader(request);

		request.setServiceID(serviceID);
		request.setVariantID(variantID);
		request.setSubscriberNumber(subscriberMSISDN);

		return request;
	}

	public static RemoveQuotaRequest generateRemoveQuota(String serviceID, 
														String variantID,
														com.concurrent.hxc.Number subscriberMSISDN, 
														com.concurrent.hxc.Number number, 
														ServiceQuota quota)
	{
		RemoveQuotaRequest request = new RemoveQuotaRequest();
		generateRequestHeader(request);

		request.setServiceID(serviceID);
		request.setVariantID(variantID);
		request.setSubscriberNumber(subscriberMSISDN);
		request.setMemberNumber(number);
		request.setQuota(quota);

		return request;
	}

	public static ReplaceMemberRequest generateReplaceMember(com.concurrent.hxc.Number number)
	{
		ReplaceMemberRequest request = new ReplaceMemberRequest();
		generateRequestHeader(request);

		return request;
	}

	public static SendSMSRequest generateSendSMS(com.concurrent.hxc.Number subscriberMSISDN, 
												String sourceAddress, 
												String message)
	{
		SendSMSRequest request = new SendSMSRequest();
		generateRequestHeader(request);

		request.setSubscriberNumber(subscriberMSISDN);
		request.setSourceAddress(sourceAddress);
		request.setMessage(message);

		return request;
	}

	public static SubscribeRequest generateSubscribe(String serviceID, 
													String variantID, 
													com.concurrent.hxc.Number subscriberMSISDN)
	{
		SubscribeRequest request = new SubscribeRequest();
		generateRequestHeader(request);

		request.setServiceID(serviceID);
		request.setVariantID(variantID);
		request.setSubscriberNumber(subscriberMSISDN);

		return request;
	}

	public static SuspendRequest generateSuspend()
	{
		SuspendRequest request = new SuspendRequest();
		generateRequestHeader(request);

		return request;
	}

	public static TransferRequest generateTransfer(String serviceID, 
													String variantID,
													com.concurrent.hxc.Number subscriberMSISDN, 
													com.concurrent.hxc.Number number, 
													int amount, 
													String pin)
	{
		TransferRequest request = new TransferRequest();
		generateRequestHeader(request);

		request.setServiceID(serviceID);
		request.setVariantID(variantID);
		request.setSubscriberNumber(subscriberMSISDN);
		request.setRecipientNumber(number);
		request.setAmount(amount);
		request.setPin(pin);

		return request;
	}

	public static UnsubscribeRequest generateUnsubscribe(String serviceID, 
														String variantID,
														com.concurrent.hxc.Number subscriberMSISDN)
	{
		UnsubscribeRequest request = new UnsubscribeRequest();
		generateRequestHeader(request);

		request.setServiceID(serviceID);
		request.setVariantID(variantID);
		request.setSubscriberNumber(subscriberMSISDN);

		return request;
	}

	public static UnsuspendRequest generateUnsuspend()
	{
		UnsuspendRequest request = new UnsuspendRequest();
		generateRequestHeader(request);

		return request;
	}

	private static void generateRequestHeader(RequestHeader request)
	{
		request.setCallerID(CALLER_ID);
		request.setChannel(CHANNEL);
		request.setHostName(HOSTNAME);
		request.setLanguageID(LANGUAGE_ID);
		request.setMode(REQUEST_MODE);
		request.setSessionID(SESSION_ID);
		request.setTransactionID(TRANSACTION_ID);
		request.setVersion(VERSION);
	}
}
