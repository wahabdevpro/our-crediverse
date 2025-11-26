package hxc.utils.processmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.GetOwnersRequest;
import com.concurrent.hxc.GetOwnersResponse;
import com.concurrent.hxc.Number;

import hxc.servicebus.ReturnCodes;
import hxc.utils.processmodel.ui.UIProperties;

public class MembershipTest extends Test
{
	final static Logger logger = LoggerFactory.getLogger(MembershipTest.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IValueT<Number> subscriber;
	private IValueT<String> serviceID;
	private IValueT<String> variantIDin;

	private static final String NAMES_OWNER = "MST_OWNER";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

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

	private IValueT<Number> owner = new IValueT<Number>()
	{
		@Override
		public Number getValue(IProcessState state)
		{
			return state.get(MembershipTest.this, NAMES_OWNER);
		}
	};

	public IValueT<Number> getOwner()
	{
		return owner;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public MembershipTest(Action afterAction, Action errorAction, IValueT<Number> subscriber, IValueT<String> serviceID, IValueT<String> variantID)
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
			GetOwnersRequest request = state.getRequest(GetOwnersRequest.class);

			request.setServiceID(serviceID.getValue(state));
			if (variantIDin != null)
				request.setVariantID(variantIDin.getValue(state));
			request.setMemberNumber(subscriber.getValue(state));

			GetOwnersResponse response = state.getVasService().getOwners(state, request);
			state.setLastReturnCode(response.getReturnCode());
			if (!response.wasSuccess())
				return errorAction;

			Number[] owners = response.getOwners();
			if (owners != null && owners.length > 0)
			{
				state.set(this, NAMES_OWNER, owners[0]);
				return nextAction;
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
