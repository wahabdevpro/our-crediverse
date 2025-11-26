package hxc.utils.processmodel;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.GetServiceRequest;
import com.concurrent.hxc.GetServiceResponse;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.SubscriptionState;
import com.concurrent.hxc.VasServiceInfo;

import hxc.servicebus.ReturnCodes;

public class GetServiceCall extends MethodCall
{
	final static Logger logger = LoggerFactory.getLogger(GetServiceCall.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IValueT<Number> subscriber;
	private IValueT<String> serviceID;
	private IValueT<Boolean> activeOnly;

	private static final String VARIANTS_ACTIVE = "gscActive";
	private static final String VARIANTS_AVAILABLE = "gscAvailable";

	public IValueT<String> getInServiceID()
	{
		return serviceID;
	}

	private IValueT<String> firstSubscribedVariant = new IValueT<String>()
	{
		@Override
		public String getValue(IProcessState state)
		{
			String[] list = state.get(GetServiceCall.this, VARIANTS_ACTIVE);
			if (list == null || list.length == 0)
				return null;
			else
				return list[0];
		}
	};

	private IValueT<String[]> availableVariants = new IValueT<String[]>()
	{
		@Override
		public String[] getValue(IProcessState state)
		{
			return state.get(GetServiceCall.this, VARIANTS_AVAILABLE);
		}
	};

	private IValueT<String[]> subscribedVariants = new IValueT<String[]>()
	{
		@Override
		public String[] getValue(IProcessState state)
		{
			return state.get(GetServiceCall.this, VARIANTS_ACTIVE);
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public IValueT<String[]> getSubscribedVariants()
	{
		return subscribedVariants;
	}

	public IValueT<String> getFirstSubscribedVariant()
	{
		return firstSubscribedVariant;
	}

	public IValueT<String[]> getAvailableVariants()
	{
		return availableVariants;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public GetServiceCall(Action afterAction, Action errorAction, IValueT<Number> subscriber, IValueT<String> serviceID, IValueT<Boolean> activeOnly)
	{
		super(afterAction, errorAction);
		this.subscriber = subscriber;
		this.serviceID = serviceID;
		this.activeOnly = activeOnly;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@Override
	public Action execute(IProcessState state, String command)
	{
		try
		{
			GetServiceRequest request = state.getRequest(GetServiceRequest.class);

			request.setSubscriberNumber(subscriber.getValue(state));
			request.setServiceID(serviceID.getValue(state));
			request.setActiveOnly(activeOnly.getValue(state));

			GetServiceResponse response = state.getVasService(request.getServiceID()).getService(state, request);
			state.setLastReturnCode(response.getReturnCode());
			if (!response.wasSuccess())
				return errorAction;

			VasServiceInfo[] services = response.getServiceInfo();

			List<String> activeVariants = new ArrayList<String>();
			List<String> availableVariants = new ArrayList<String>();

			for (VasServiceInfo service : services)
			{
				if (service.getState() == SubscriptionState.active)
					activeVariants.add(service.getVariantName());
				else
					availableVariants.add(service.getVariantName());
			}

			state.set(this, VARIANTS_ACTIVE, activeVariants.toArray(new String[0]));
			state.set(this, VARIANTS_AVAILABLE, availableVariants.toArray(new String[0]));

			return nextAction;
		}
		catch (Throwable e)
		{
			logger.error("execution error", e);
			state.setLastReturnCode(ReturnCodes.technicalProblem);
			return errorAction;
		}
	}

}
