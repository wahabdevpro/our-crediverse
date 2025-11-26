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
import javax.xml.ws.BindingProvider;

/**
 *
 * @author justinguedes
 */
public class Service
{

	public IHxC hxc;

	public Service()
	{
		hxc = createHxC();
	}

	public Service(String url) throws MalformedURLException
	{
		hxc = createHxC(url);
	}

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

	public void call(RequestHeader requests[])
	{
		for (RequestHeader request : requests)
		{
			ResponseHeader response = call(request);

			if (response == null || response.getReturnCode() == null)
				continue;

			switch (response.getReturnCode())
			{
				case SUCCESS:
					Stats.success();
					break;
				default:
					Stats.failed();
					break;
			}
		}
	}

	private IHxC createHxC()
	{
		IHxC service = new HxCService().getHxCPort();

		Map<String, Object> reqContext = ((BindingProvider) service).getRequestContext();
		reqContext.put(BindingProvider.USERNAME_PROPERTY, "c4u");
		reqContext.put(BindingProvider.PASSWORD_PROPERTY, "c4u");

		return service;
	}

	private IHxC createHxC(String url) throws MalformedURLException
	{
		URL urls = new URL(url);
		IHxC service = new HxCService(urls).getHxCPort();

		Map<String, Object> reqContext = ((BindingProvider) service).getRequestContext();
		reqContext.put(BindingProvider.USERNAME_PROPERTY, "c4u");
		reqContext.put(BindingProvider.PASSWORD_PROPERTY, "c4u");

		return service;
	}
}
