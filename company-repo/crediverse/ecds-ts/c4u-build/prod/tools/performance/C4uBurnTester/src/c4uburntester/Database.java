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

	public final static String SERVICE_ID = "CrShr";
	public final static String VARIANT_ID = "Weekly";

	public final static int MAX_NUMBERS = 1000;
	public final static int MAX_NUMBER = 100000;
	private final static String ZEROS = "000000000";

	public static com.concurrent.hxc.Number[] getNumbers(int from)
	{
		com.concurrent.hxc.Number numbers[] = new com.concurrent.hxc.Number[MAX_NUMBERS];
		for (int i = 0; i < MAX_NUMBERS; i++)
		{
			numbers[i] = new com.concurrent.hxc.Number();
			numbers[i].setAddressDigits(String.format("084%s%s", ZEROS.substring(0, ZEROS.length() - Integer.toString(from + i).length()), i));
			numbers[i].setNumberPlan(NumberPlan.UNKNOWN);
			numbers[i].setNumberType(NumberType.UNKNOWN);
		}
		return numbers;
	}

	public static AddMemberRequest generateAddMember(com.concurrent.hxc.Number subscriber, com.concurrent.hxc.Number number)
	{
		AddMemberRequest request = new AddMemberRequest();
		generateRequestHeader(request);

		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		request.setSubscriberNumber(subscriber);
		request.setMemberNumber(number);

		return request;
	}

	public static AddQuotaRequest generateAddQuota(com.concurrent.hxc.Number subscriber, com.concurrent.hxc.Number number, ServiceQuota quota)
	{
		AddQuotaRequest request = new AddQuotaRequest();
		generateRequestHeader(request);

		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		request.setSubscriberNumber(subscriber);
		request.setMemberNumber(number);
		request.setQuota(quota);

		return request;
	}

	public static ChangeQuotaRequest generateChangeQuota(com.concurrent.hxc.Number subscriber, com.concurrent.hxc.Number number, ServiceQuota quota, ServiceQuota newQuota)
	{
		ChangeQuotaRequest request = new ChangeQuotaRequest();
		generateRequestHeader(request);

		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		request.setSubscriberNumber(subscriber);
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

	public static GetBalancesRequest generateGetBalances(com.concurrent.hxc.Number subscriber, boolean sms)
	{
		GetBalancesRequest request = new GetBalancesRequest();
		generateRequestHeader(request);

		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		request.setSubscriberNumber(subscriber);
		request.setRequestSMS(sms);

		return request;
	}

	public static GetHistoryRequest generateGetHistory(com.concurrent.hxc.Number numberA, com.concurrent.hxc.Number numberB, int rowLimit, boolean inReverse)
	{
		GetHistoryRequest request = new GetHistoryRequest();
		generateRequestHeader(request);

		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		request.setANumber(numberA);
		request.setBNumber(numberB);
		request.setRowLimit(rowLimit);
		request.setInReverse(inReverse);

		return request;
	}

	public static GetMembersRequest generateGetMembers(com.concurrent.hxc.Number subscriber)
	{
		GetMembersRequest request = new GetMembersRequest();
		generateRequestHeader(request);

		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		request.setSubscriberNumber(subscriber);

		return request;
	}

	public static GetOwnersRequest generateGetOwners(com.concurrent.hxc.Number number)
	{
		GetOwnersRequest request = new GetOwnersRequest();
		generateRequestHeader(request);

		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		request.setMemberNumber(number);

		return request;
	}

	public static GetQuotasRequest generateGetQuotas(com.concurrent.hxc.Number subscriber, boolean activeOnly, String daysOfWeek, String destination, com.concurrent.hxc.Number number, String service, String timeOfDay)
	{
		GetQuotasRequest request = new GetQuotasRequest();
		generateRequestHeader(request);

		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		request.setSubscriberNumber(subscriber);
		request.setActiveOnly(activeOnly);
		request.setDaysOfWeek(daysOfWeek);
		request.setDestination(destination);
		request.setMemberNumber(number);
		request.setService(service);
		request.setTimeOfDay(timeOfDay);
		request.setActiveOnly(true);

		return request;
	}

	public static GetServiceRequest generateGetService(com.concurrent.hxc.Number subscriber, boolean activeOnly)
	{
		GetServiceRequest request = new GetServiceRequest();
		generateRequestHeader(request);

		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		request.setSubscriberNumber(subscriber);
		request.setActiveOnly(activeOnly);

		return request;
	}

	public static GetServicesRequest generateGetServices(com.concurrent.hxc.Number subscriber, boolean activeOnly)
	{
		GetServicesRequest request = new GetServicesRequest();
		generateRequestHeader(request);

		request.setSubscriberNumber(subscriber);
		request.setActiveOnly(activeOnly);

		return request;
	}

	public static GetStatusRequest generateGetStatus()
	{
		GetStatusRequest request = new GetStatusRequest();
		generateRequestHeader(request);

		return request;
	}

	public static MigrateRequest generateMigrate(com.concurrent.hxc.Number subscriber, String newServiceID, String newVariantID)
	{
		MigrateRequest request = new MigrateRequest();
		generateRequestHeader(request);

		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		request.setSubscriberNumber(subscriber);
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

	public static ProcessLifecycleEventRequest generateProcessLifeCycleEvent(com.concurrent.hxc.Number subscriber)
	{
		ProcessLifecycleEventRequest request = new ProcessLifecycleEventRequest();
		generateRequestHeader(request);

		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		request.setMsisdn(subscriber.getAddressDigits());

		return request;
	}

	public static ProcessRequest generateProcess(int processID)
	{
		ProcessRequest request = new ProcessRequest();
		generateRequestHeader(request);

		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		request.setProcessID(processID);

		return request;
	}

	public static RemoveMemberRequest generateRemoveMember(com.concurrent.hxc.Number subscriber, com.concurrent.hxc.Number number)
	{
		RemoveMemberRequest request = new RemoveMemberRequest();
		generateRequestHeader(request);

		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		request.setSubscriberNumber(subscriber);
		request.setMemberNumber(number);

		return request;
	}

	public static RemoveMembersRequest generateRemoveMembers(com.concurrent.hxc.Number subscriber)
	{
		RemoveMembersRequest request = new RemoveMembersRequest();
		generateRequestHeader(request);

		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		request.setSubscriberNumber(subscriber);

		return request;
	}

	public static RemoveQuotaRequest generateRemoveQuota(com.concurrent.hxc.Number subscriber, com.concurrent.hxc.Number number, ServiceQuota quota)
	{
		RemoveQuotaRequest request = new RemoveQuotaRequest();
		generateRequestHeader(request);

		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		request.setSubscriberNumber(subscriber);
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

	public static SendSMSRequest generateSendSMS(com.concurrent.hxc.Number subscriber, String sourceAddress, String message)
	{
		SendSMSRequest request = new SendSMSRequest();
		generateRequestHeader(request);

		request.setSubscriberNumber(subscriber);
		request.setSourceAddress(sourceAddress);
		request.setMessage(message);

		return request;
	}

	public static SubscribeRequest generateSubscribe(com.concurrent.hxc.Number subscriber)
	{
		SubscribeRequest request = new SubscribeRequest();
		generateRequestHeader(request);

		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		request.setSubscriberNumber(subscriber);

		return request;
	}

	public static SuspendRequest generateSuspend()
	{
		SuspendRequest request = new SuspendRequest();
		generateRequestHeader(request);

		return request;
	}

	public static TransferRequest generateTransfer(com.concurrent.hxc.Number subscriber, com.concurrent.hxc.Number number, int amount, String pin)
	{
		TransferRequest request = new TransferRequest();
		generateRequestHeader(request);

		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		request.setSubscriberNumber(subscriber);
		request.setRecipientNumber(number);
		request.setAmount(amount);
		request.setPin(pin);

		return request;
	}

	public static UnsubscribeRequest generateUnsubscribe(com.concurrent.hxc.Number subscriber)
	{
		UnsubscribeRequest request = new UnsubscribeRequest();
		generateRequestHeader(request);

		request.setServiceID(SERVICE_ID);
		request.setVariantID(VARIANT_ID);
		request.setSubscriberNumber(subscriber);

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
