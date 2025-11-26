package hxc.utils.processmodel;

import com.concurrent.hxc.Number;

import hxc.utils.processmodel.ui.UIProperties;

public class UssdStart extends Start
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IValueT<Number> subscriberNumber = new IValueT<Number>()
	{
		@Override
		public Number getValue(IProcessState state)
		{
			return state.get(UssdStart.this, "subscriberNumber");
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@UIProperties(category = "Ouput", value = "number")
	public IValueT<Number> getSubscriberNumber()
	{
		return subscriberNumber;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public UssdStart(String serviceID, String processID)
	{
		super(serviceID, processID);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@Override
	public Action execute(IProcessState state, String command)
	{
		// Save the subscriber number
		state.set(UssdStart.this, "subscriberNumber", state.getSubscriberNumber());

		return nextAction;
	}

}
