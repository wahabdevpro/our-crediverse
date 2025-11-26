package hxc.services.caisim;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.jws.WebParam;
import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.servicebus.IServiceBus;
import hxc.services.caisim.engine.cai.CommonCaiHandler;
import hxc.services.caisim.engine.soap.AddHlrSubscriberPdpContextsRequestImpl;
import hxc.services.caisim.engine.soap.AddHlrSubscriberRequestImpl;
import hxc.services.caisim.engine.soap.AddSapcGroupsRequestImpl;
import hxc.services.caisim.engine.soap.AddSapcQuotaRequestImpl;
import hxc.services.caisim.engine.soap.AddSapcSubscriberRequestImpl;
import hxc.services.caisim.engine.soap.DeleteHlrSubscriberPdpContextsRequestImpl;
import hxc.services.caisim.engine.soap.DeleteSapcGroupsRequestImpl;
import hxc.services.caisim.engine.soap.GetHlrSubscriberRequestImpl;
import hxc.services.caisim.engine.soap.GetSapcGroupRequestImpl;
import hxc.services.caisim.engine.soap.GetSapcGroupsRequestImpl;
import hxc.services.caisim.engine.soap.GetSapcSubscriberRequestImpl;
import hxc.services.caisim.engine.soap.SetHlrSubscriberImeiRequestImpl;
import hxc.services.caisim.engine.soap.SetHlrSubscriberNamRequestImpl;
import hxc.services.caisim.engine.soap.SetHlrSubscriberObrRequestImpl;
import hxc.services.caisim.engine.soap.SetHlrSubscriberPdpCpRequestImpl;
import hxc.services.caisim.engine.soap.SetHlrSubscriberRsaRequestImpl;
import hxc.services.caisim.engine.soap.SetSapcQuotaRequestImpl;
import hxc.services.caisim.engine.soap.SetSapcZainLteRequestImpl;
import hxc.services.caisim.engine.soap.UpdateHlrSubscriberPdpContextsRequestImpl;
import hxc.services.caisim.engine.soap.UpdateSapcGroupsRequestImpl;
import hxc.services.caisim.model.Subscriber;
import hxc.services.numberplan.INumberPlan;
import hxc.utils.protocol.caisim.Protocol;
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
import hxc.utils.tcp.TcpRequest;
import hxc.utils.tcp.TcpResponse;
import hxc.utils.tcp.TcpServer;

@WebService(endpointInterface = "hxc.services.caisim.ICaiSim")
public class CaiSim implements ICaiSim, ICaiData
{
	final static Logger logger = LoggerFactory.getLogger(CaiSim.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private INumberPlan numberPlan;
	private int port;
	private int threadPoolSize;
	private TcpServer server;

	// Subscriptions
	private Map<String, Subscriber> subscribers = new HashMap<String, Subscriber>();

	// Authentication
	private HashSet<String> authenticated = new HashSet<String>();
	private Map<String, String> authentication = new HashMap<String, String>();
	
	// Injected Responses - only one of them or none is active at any time, they are mutually exclusive
	private HashMap<String, Integer> injectedResponses = new HashMap<String, Integer>();
	private HashMap<String, InjectedSelectiveResponse> injectedSelectiveResponses = new HashMap<String, InjectedSelectiveResponse>();
	
	// A global lock (mutex) for the whole CAI SIM service - used to provide synchronization
	// and visibility between SOAP and CAI interfaces when manipulating data.
	public final Object lock = new Object();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public CaiSim(IServiceBus esb, INumberPlan numberPlan, int threadPoolSize, int port)
	{
		// Assign the fields
		this.numberPlan = numberPlan;
		this.threadPoolSize = threadPoolSize;
		this.port = port;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// House Keeping
	//
	// /////////////////////////////////

	@Override
	public int ping(int seq)
	{
		// Increment the sequence
		return ++seq;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Life Cycle Management
	//
	// /////////////////////////////////

	@Override
	public boolean start()
	{
		// Stop the server if it is running
		stop();

		// Create the TCP server
		server = new TcpServer(threadPoolSize)
		{

			// When a client connects to the server
			@Override
			public void connectionRecieved(TcpRequest request)
			{
				// Create the TCP response
				TcpResponse response = new TcpResponse("CONNECTING TO CAISIM...\n\nCONNECTED...\n");

				try
				{
					// Prompt the user of the connection to CAI
					request.respond(response);
				}
				catch (IOException e)
				{

				}
			}

			// When the client disconnects from the server
			@Override
			public void connectionClosed(TcpRequest request)
			{
				synchronized (getLock())
				{
					authenticated.remove(request.getSocket().toString());
				}
			}

			// When you receive a TCP request
			@Override
			public void uponTcpRequest(TcpRequest request)
			{
				new CommonCaiHandler(CaiSim.this).handle(request);
			}
		};

		// Set the prompt for the server
		server.setPrompt("Enter command: ");
		server.setLineSeparator("\r\n");

		try
		{
			// Start the server
			server.start(port);
		}
		catch (IOException e)
		{
			logger.error("Failed to start CaiSim", e);
			return false;
		}

		return true;
	}

	@Override
	public void stop()
	{
		// Stop the TCP server if it is not null
		if (server != null)
		{
			// Stop the server
			try
			{
				server.stop();
			}
			catch (IOException e)
			{
				logger.error("Failed to stop CaiSim", e);
			}
			
			server = null;
		}
	}

	@Override
	public void reset()
	{
		// First, stop the server
		stop();
		
		// Reset the subscription maps
		subscribers.clear();

		// Reset the authentication maps
		authentication.clear();
		
		// Reset the injected responses maps
		injectedResponses.clear();
		injectedSelectiveResponses.clear();
		
		// After all cleared up - start the server
		start();
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// SAPC SOAP Implementation
	//
	// /////////////////////////////////
	
	@Override
	public AddSapcSubscriberResponse addSapcSubscriber(AddSapcSubscriberRequest request)
	{
		return new AddSapcSubscriberRequestImpl(this).execute(request);
	}
	
	@Override
	public UpdateSapcGroupsResponse updateSapcGroups(UpdateSapcGroupsRequest request)
	{
		return new UpdateSapcGroupsRequestImpl(this).execute(request);
	}
	
	@Override
	public AddSapcGroupsResponse addSapcGroups(AddSapcGroupsRequest request)
	{
		return new AddSapcGroupsRequestImpl(this).execute(request);
	}
	
	@Override
	public DeleteSapcGroupsResponse deleteSapcGroups(DeleteSapcGroupsRequest request)
	{
		return new DeleteSapcGroupsRequestImpl(this).execute(request);
	}
	
	@Override
	public SetSapcQuotaResponse setSapcQuota(SetSapcQuotaRequest request)
	{
		return new SetSapcQuotaRequestImpl(this).execute(request);
	}
	
	@Override
	public AddSapcQuotaResponse addSapcQuota(AddSapcQuotaRequest request)
	{
		return new AddSapcQuotaRequestImpl(this).execute(request);
	}
	
	@Override
	public GetSapcGroupResponse getSapcGroup(@WebParam(name = "request") GetSapcGroupRequest request)
	{
		return new GetSapcGroupRequestImpl(this).execute(request);
	}
	
	@Override
	public GetSapcGroupsResponse getSapcGroups(@WebParam(name = "request") GetSapcGroupsRequest request)
	{
		return new GetSapcGroupsRequestImpl(this).execute(request);
	}
	
	@Override
	public GetSapcSubscriberResponse getSapcSubscriber(@WebParam(name = "request") GetSapcSubscriberRequest request)
	{
		return new GetSapcSubscriberRequestImpl(this).execute(request);
	}
	
	@Override
	public SetSapcZainLteResponse setSapcZainLte(@WebParam(name = "request") SetSapcZainLteRequest request)
	{
		return new SetSapcZainLteRequestImpl(this).execute(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// HLR SOAP Implementation
	//
	// /////////////////////////////////
	
	@Override
	public AddHlrSubscriberResponse addHlrSubscriber(AddHlrSubscriberRequest request)
	{
		return new AddHlrSubscriberRequestImpl(this).execute(request);
	}
	
	@Override
	public GetHlrSubscriberResponse getHlrSubscriber(GetHlrSubscriberRequest request)
	{
		return new GetHlrSubscriberRequestImpl(this).execute(request);
	}
	
	@Override
	public SetHlrSubscriberObrResponse setHlrSubscriberObr(SetHlrSubscriberObrRequest request)
	{
		return new SetHlrSubscriberObrRequestImpl(this).execute(request);
	}
	
	@Override
	public SetHlrSubscriberImeiResponse setHlrSubscriberImei(SetHlrSubscriberImeiRequest request)
	{
		return new SetHlrSubscriberImeiRequestImpl(this).execute(request);
	}
	
	@Override
	public SetHlrSubscriberRsaResponse setHlrSubscriberRsa(SetHlrSubscriberRsaRequest request)
	{
		return new SetHlrSubscriberRsaRequestImpl(this).execute(request);
	}
	
	@Override
	public SetHlrSubscriberNamResponse setHlrSubscriberNam(SetHlrSubscriberNamRequest request)
	{
		return new SetHlrSubscriberNamRequestImpl(this).execute(request);
	}
	
	@Override
	public SetHlrSubscriberPdpCpResponse setHlrSubscriberPdpCp(SetHlrSubscriberPdpCpRequest request)
	{
		return new SetHlrSubscriberPdpCpRequestImpl(this).execute(request);
	}
	
	@Override
	public UpdateHlrSubscriberPdpContextsResponse updateHlrSubscriberPdpContexts(UpdateHlrSubscriberPdpContextsRequest request)
	{
		return new UpdateHlrSubscriberPdpContextsRequestImpl(this).execute(request);
	}
	
	@Override
	public AddHlrSubscriberPdpContextsResponse addHlrSubscriberPdpContexts(AddHlrSubscriberPdpContextsRequest request)
	{
		return new AddHlrSubscriberPdpContextsRequestImpl(this).execute(request);
	}
	
	@Override
	public DeleteHlrSubscriberPdpContextsResponse deleteHlrSubscriberPdpContexts(DeleteHlrSubscriberPdpContextsRequest request)
	{
		return new DeleteHlrSubscriberPdpContextsRequestImpl(this).execute(request);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Other Implementation
	//
	// /////////////////////////////////

	@Override
	public Subscriber getSubscriber(String msisdn, SubscriptionType type)
	{
		Subscriber sub = subscribers.get(numberPlan.getInternationalFormat(msisdn));
		
		if (sub == null)
			return null;
		
		switch (type)
		{
			case ANY:
				return sub;
			case HLR:
				if (sub.hasHlrSubscription())
					return sub;
				break;
			case SAPC:
				if (sub.hasSapcSubscription())
					return sub;
				break;
		}
		
		return null;
	}

	@Override
	public void addAuthentication(String userId, String password)
	{
		synchronized (getLock())
		{
			authentication.put(userId, password);	
		}
	}
	
	@Override
	public void injectResponse(CaiCall caiCall, Integer responseCode)
	{
		String caiCallStr = caiCall.toString();
		
		synchronized (getLock())
		{
			injectedSelectiveResponses.remove(caiCallStr);
			
			if (responseCode == Protocol.RESPONSE_CODE_SUCCESSFUL)
				injectedResponses.remove(caiCallStr);
			else
				injectedResponses.put(caiCallStr, responseCode >= 0 ? responseCode : 0);
		}
	}
	
	@Override
	public void injectSelectiveResponse(CaiCall caiCall, Integer responseCode, Integer failCount, Integer skipCount)
	{
		String caiCallStr = caiCall.toString();
		
		synchronized (getLock())
		{		
			injectedResponses.remove(caiCallStr);

			if (responseCode == Protocol.RESPONSE_CODE_SUCCESSFUL)
				injectedSelectiveResponses.remove(caiCallStr);
			else
				injectedSelectiveResponses.put(caiCallStr, new InjectedSelectiveResponse(responseCode, failCount, skipCount));
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ICaiData Implementation
	//
	// /////////////////////////////////

	@Override
	public Subscriber createSubscriber(String msisdn)
	{
		// Create the subscriber
		Subscriber subscriber = new Subscriber(msisdn, numberPlan);

		// Add the subscriber to the map
		subscribers.put(subscriber.getInternationalNumber(), subscriber);

		// Return the created subscriber
		return subscriber;
	}
	
	@Override
	public boolean isAuthenticated(Socket clientSocket)
	{
		synchronized (getLock())
		{
			return authenticated.contains(clientSocket.toString());
		}
	}
	
	@Override
	public boolean authenticate(String user, String password, Socket clientSocket)
	{
		synchronized (getLock())
		{
			if (authenticated.contains(clientSocket.toString()))
				return true;
			
			String storedPassword = authentication.get(user);
			if (storedPassword != null && storedPassword.equals(password))
			{
				authenticated.add(clientSocket.toString());
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public Object getLock()
	{
		return lock;
	}
	
	@Override
	public Integer handleInjectedResponse(String command, String commandType)
	{
		synchronized (getLock())
		{
			String key = command + ':' + commandType;
			
			InjectedSelectiveResponse selectiveResponse = injectedSelectiveResponses.get(key);
			if (selectiveResponse == null)
			{
				Integer responseCode = injectedResponses.get(key);
				if (responseCode != null)
					return responseCode;
			}
			else
			{
				if (selectiveResponse.getSkipCount() > 0)
					selectiveResponse.setSkipCount(selectiveResponse.getSkipCount() - 1);
				else if (selectiveResponse.getSkipCount() == 0 )
				{
					if (selectiveResponse.getFailCount() > 0 )
					{
						selectiveResponse.setFailCount(selectiveResponse.getFailCount() - 1);
						return selectiveResponse.getResponseCode();
					}
					else
						injectedSelectiveResponses.remove(key);
				}
			}
		}
		
		return Protocol.RESPONSE_CODE_SUCCESSFUL;
	}
}
