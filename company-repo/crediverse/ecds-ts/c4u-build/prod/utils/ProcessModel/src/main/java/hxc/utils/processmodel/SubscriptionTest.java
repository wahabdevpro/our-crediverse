package hxc.utils.processmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.GetServiceRequest;
import com.concurrent.hxc.GetServiceResponse;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.SubscriptionState;
import com.concurrent.hxc.VasServiceInfo;

import hxc.servicebus.ReturnCodes;
import hxc.utils.processmodel.ui.UIProperties;

public class SubscriptionTest extends Test
{
	final static Logger logger = LoggerFactory.getLogger(SubscriptionTest.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IValueT<Number> subscriber;
	private IValueT<String> serviceID;
	private IValueT<String> variantIDin;

	private IValueT<String> variantID = new IValueT<String>()
	{
		@Override
		public String getValue(IProcessState state)
		{
			return state.get(SubscriptionTest.this, "VariantID");
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@UIProperties(category = "Ouput", value = "variantID")
	public IValueT<String> getVariantID()
	{
		return variantID;
	}

	@UIProperties(category = "Input", value = "serviceId")
	private IValueT<String> getServiceID()
	{
		return serviceID;
	}

	@UIProperties(category = "Input", value = "din")
	private IValueT<String> getVariantIDin()
	{
		return variantIDin;
	}

	@UIProperties(category = "Input", value = "subscriber")
	private IValueT<Number> getSubscriber()
	{
		return subscriber;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public SubscriptionTest(Action afterAction, Action errorAction, IValueT<Number> subscriber, IValueT<String> serviceID, IValueT<String> variantID)
	{
		super(afterAction, errorAction, null);
		this.subscriber = subscriber;
		this.serviceID = serviceID;
		this.variantIDin = variantID;
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
			request.setActiveOnly(true);

			request.setServiceID(serviceID.getValue(state));
			if (variantIDin != null)
				request.setVariantID(variantIDin.getValue(state));
			request.setSubscriberNumber(subscriber.getValue(state));

			GetServiceResponse response = state.getVasService().getService(state, request);
			state.setLastReturnCode(response.getReturnCode());
			if (!response.wasSuccess())
				return errorAction;

			for (VasServiceInfo variant : response.getServiceInfo())
			{
				if (variant.getState() == SubscriptionState.active)
				{
					// Save VariantID
					state.set(this, "VariantID", variant.getVariantID());
					return nextAction;
				}
			}

			return noAction;

		}
		catch (Throwable e)
		{
			logger.error("execution error", e);
			state.setLastReturnCode(ReturnCodes.technicalProblem);
			return errorAction;
		}
	}

}
