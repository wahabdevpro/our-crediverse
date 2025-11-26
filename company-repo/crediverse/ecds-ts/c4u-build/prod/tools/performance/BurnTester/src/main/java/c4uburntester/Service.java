/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package c4uburntester;

import com.concurrent.hxc.AddMemberRequest;
import com.concurrent.hxc.AddQuotaRequest;
import com.concurrent.hxc.ChangeQuotaRequest;
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
import com.concurrent.hxc.PingRequest;
import com.concurrent.hxc.ProcessLifecycleEventRequest;
import com.concurrent.hxc.ProcessRequest;
import com.concurrent.hxc.RemoveMemberRequest;
import com.concurrent.hxc.RemoveMembersRequest;
import com.concurrent.hxc.RemoveQuotaRequest;
import com.concurrent.hxc.ReplaceMemberRequest;
import com.concurrent.hxc.RequestHeader;
import com.concurrent.hxc.ResponseHeader;
import com.concurrent.hxc.ReturnCodes;
import com.concurrent.hxc.SendSMSRequest;
import com.concurrent.hxc.SubscribeRequest;
import com.concurrent.hxc.SuspendRequest;
import com.concurrent.hxc.TransferRequest;
import com.concurrent.hxc.UnsubscribeRequest;
import com.concurrent.hxc.UnsuspendRequest;
import hxc.connectors.soap.HxCService;
import hxc.connectors.soap.IHxC;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.BindingProvider;

public class Service
{

	private String userName = null;
	private String userPass = null;
	private String url = null;

	public IHxC hxc;

	public Service(Properties props) throws MalformedURLException
	{
		userName = props.getProperty("USERNAME").trim();
		userPass = props.getProperty("PASSWORD").trim();
		url = props.getProperty("URL");
		
		if (url == null)
			hxc = createHxC();
		else
			hxc = createHxC(url);
	}

//	public Service(String url) throws MalformedURLException
//	{
//		hxc = createHxC(url);
//	}

	@SuppressWarnings("unchecked")
	public <T, Tres> Tres call(T request)
	{
		if (hxc == null)
		{
			hxc = createHxC();
		}

		if (request instanceof AddMemberRequest)
		{
			return (Tres) hxc.addMember((AddMemberRequest) request);
		}
		else if (request instanceof AddQuotaRequest)
		{
			return (Tres) hxc.addQuota((AddQuotaRequest) request);
		}
		else if (request instanceof ChangeQuotaRequest)
		{
			return (Tres) hxc.changeQuota((ChangeQuotaRequest) request);
		}
		else if (request instanceof ExtendRequest)
		{
			return (Tres) hxc.extend((ExtendRequest) request);
		}
		else if (request instanceof GetBalancesRequest)
		{
			return (Tres) hxc.getBalances((GetBalancesRequest) request);
		}
		else if (request instanceof GetHistoryRequest)
		{
			return (Tres) hxc.getHistory((GetHistoryRequest) request);
		}
		else if (request instanceof GetMembersRequest)
		{
			return (Tres) hxc.getMembers((GetMembersRequest) request);
		}
		else if (request instanceof GetOwnersRequest)
		{
			return (Tres) hxc.getOwners((GetOwnersRequest) request);
		}
		else if (request instanceof GetQuotasRequest)
		{
			return (Tres) hxc.getQuotas((GetQuotasRequest) request);
		}
		else if (request instanceof GetServiceRequest)
		{
			return (Tres) hxc.getService((GetServiceRequest) request);
		}
		else if (request instanceof GetServicesRequest)
		{
			return (Tres) hxc.getServices((GetServicesRequest) request);
		}
		else if (request instanceof GetStatusRequest)
		{
			return (Tres) hxc.getStatus((GetStatusRequest) request);
		}
		else if (request instanceof MigrateRequest)
		{
			return (Tres) hxc.migrate((MigrateRequest) request);
		}
		else if (request instanceof PingRequest)
		{
			return (Tres) hxc.ping((PingRequest) request);
		}
		else if (request instanceof ProcessLifecycleEventRequest)
		{
			return (Tres) hxc.processLifecycleEvent((ProcessLifecycleEventRequest) request);
		}
		else if (request instanceof ProcessRequest)
		{
			return (Tres) hxc.process((ProcessRequest) request);
		}
		else if (request instanceof RemoveMemberRequest)
		{
			return (Tres) hxc.removeMember((RemoveMemberRequest) request);
		}
		else if (request instanceof RemoveMembersRequest)
		{
			return (Tres) hxc.removeMembers((RemoveMembersRequest) request);
		}
		else if (request instanceof RemoveQuotaRequest)
		{
			return (Tres) hxc.removeQuota((RemoveQuotaRequest) request);
		}
		else if (request instanceof ReplaceMemberRequest)
		{
			return (Tres) hxc.replaceMember((ReplaceMemberRequest) request);
		}
		else if (request instanceof SendSMSRequest)
		{
			return (Tres) hxc.sendSMS((SendSMSRequest) request);
		}
		else if (request instanceof SubscribeRequest)
		{
			return (Tres) hxc.subscribe((SubscribeRequest) request);
		}
		else if (request instanceof SuspendRequest)
		{
			return (Tres) hxc.suspend((SuspendRequest) request);
		}
		else if (request instanceof TransferRequest)
		{
			return (Tres) hxc.transfer((TransferRequest) request);
		}
		else if (request instanceof UnsubscribeRequest)
		{
			return (Tres) hxc.unsubscribe((UnsubscribeRequest) request);
		}
		else if (request instanceof UnsuspendRequest)
		{
			return (Tres) hxc.unsuspend((UnsuspendRequest) request);
		}

		return null;
	}

	public void call(RequestHeader requests[], int tps)
	{
//		long startTime, callTime, duration;
		
//		long delay = Math.round(1000/tps);
		for (RequestHeader request : requests)
		{
//			startTime = System.nanoTime();

			ResponseHeader response = call(request);
//			try {
//				Thread.sleep(delay);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

//			duration = System.nanoTime() - startTime;
//			callTime = TimeUnit.MILLISECONDS.convert(duration, TimeUnit.NANOSECONDS);
//			System.out.println( String.format("Call time [%d] ms", callTime));

			if (response == null)
			{
				Stats.nullResponse(tps);
				continue;
			}

			ReturnCodes returnCode = response.getReturnCode();
			
			if (returnCode == null)
			{
				Stats.nullReturnCode(tps);
				continue;
			}

			switch (returnCode)
			{
				case SUCCESS:
					Stats.success(tps);
					break;

				case ALREADY_MEMBER:
				case ALREADY_OTHER_MEMBER:
					Stats.alreadyConsumer(tps);
					break;
					
				case INSUFFICIENT_BALANCE:
					Stats.insufficientBalance(tps);
					break;
					
				case TECHNICAL_PROBLEM:
					Stats.technicalProblem(tps);
					break;
					
				case ALREADY_SUBSCRIBED:
					Stats.alreadySubscribed(tps);
					break;
					
				case NOT_SUBSCRIBED:
					Stats.notSubscribed(tps);
					break;
					
				case QUOTA_REACHED:
					Stats.quotaReached(tps);
					break;

				case INCOMPLETE:
					Stats.incomplete(tps);
					break;
					
				case AUTHORIZATION_FAILURE:
					Stats.authorizationFailure(tps);
					break;
					
				case SERVICE_BUSY:
					Stats.serviceBusy(tps);
					break;
					
				case SUSPENDED:
					Stats.suspended(tps);
					break;
					
				case INACTIVE_A_PARTY:
					Stats.inactiveAParty(tps);
					break;
					
				case INACTIVE_B_PARTY:
					Stats.inactiveBParty(tps);
					break;
					
				case NOT_ELIGIBLE:
				case MEMBER_NOT_ELIGIBLE:
					Stats.notEligible(tps);
					break;
					
				case INVALID_VARIANT:
					Stats.invalidVariant(tps);
					break;
					
				case ALREADY_ADDED: //Quota
					Stats.alreadyHasQuota(tps);
					break;
					
				case INVALID_QUOTA:
					Stats.invalidQuota(tps);
					break;
					
				case TIMED_OUT:
					Stats.timedOut(tps);
					break;
					
				default:
					Stats.failed(tps);
					break;
			}
		}
	}

	private IHxC createHxC()
	{
		IHxC hxc = new HxCService().getHxCPort();

		Map<String, Object> reqContext = ((BindingProvider) hxc).getRequestContext();
		reqContext.put(BindingProvider.USERNAME_PROPERTY, userName);
		reqContext.put(BindingProvider.PASSWORD_PROPERTY, userPass);

		return hxc;
	}

	private IHxC createHxC(String url) throws MalformedURLException
	{
		URL urls = new URL(url);
		IHxC service = new HxCService(urls).getHxCPort();

		Map<String, Object> reqContext = ((BindingProvider) service).getRequestContext();
		reqContext.put(BindingProvider.USERNAME_PROPERTY, userName);
		reqContext.put(BindingProvider.PASSWORD_PROPERTY, userPass);

		return service;
	}
}
