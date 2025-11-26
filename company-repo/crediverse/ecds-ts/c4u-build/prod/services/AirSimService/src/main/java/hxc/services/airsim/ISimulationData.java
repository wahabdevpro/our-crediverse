package hxc.services.airsim;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.google.gson.Gson;

import hxc.services.airsim.model.SubscriberEx;
import hxc.services.airsim.protocol.CallHistory;
import hxc.services.airsim.protocol.Subscriber;
import hxc.services.airsim.protocol.SubscriberState;

public interface ISimulationData
{
	public abstract Map<String, InjectedResponse> getInjectedResponses();

	public abstract Gson getGson();

	public abstract List<CallHistory> getHistory();

	public abstract SubscriberEx getInternationalSubscriber(String subscriberNumber);

	public abstract SubscriberEx getNationalSubscriber(String subscriberNumber);

	public abstract String getNaiNumber(String number, Integer nai);

	public abstract Subscriber addSubscriber(String msisdn, int languageID, int serviceClass, long accountValue, SubscriberState state);

	public abstract ScheduledThreadPoolExecutor getScheduledThreadPool();
	
	public abstract void setLastRefillProfileID(String refillProfileID);
}
