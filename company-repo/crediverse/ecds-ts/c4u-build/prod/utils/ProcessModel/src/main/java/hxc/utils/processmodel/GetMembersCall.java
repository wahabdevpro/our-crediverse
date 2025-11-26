package hxc.utils.processmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.ContactInfo;
import com.concurrent.hxc.GetMembersRequest;
import com.concurrent.hxc.GetMembersResponse;
import com.concurrent.hxc.Number;

import hxc.servicebus.ReturnCodes;

public class GetMembersCall extends MethodCall
{
	final static Logger logger = LoggerFactory.getLogger(GetMembersCall.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IValueT<Number> subscriber;
	private IValueT<String> serviceID;
	private IValueT<String> variantID;

	private IValueT<Boolean> hasMembers = new IValueT<Boolean>()
	{
		@Override
		public Boolean getValue(IProcessState state)
		{
			return state.get(GetMembersCall.this, "hasMembers");
		}
	};

	private IValueT<Number[]> members = new IValueT<Number[]>()
	{
		@Override
		public Number[] getValue(IProcessState state)
		{
			return state.get(GetMembersCall.this, "Members");
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public IValueT<Boolean> getHasMembers()
	{
		return hasMembers;
	}

	public IValueT<Number[]> getMembers()
	{
		return members;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public GetMembersCall(Action afterAction, Action errorAction, IValueT<Number> subscriber, IValueT<String> serviceID, IValueT<String> variantID)
	{
		super(afterAction, errorAction);
		this.subscriber = subscriber;
		this.serviceID = serviceID;
		this.variantID = variantID;
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
			GetMembersRequest request = state.getRequest(GetMembersRequest.class);

			request.setServiceID(serviceID.getValue(state));
			if (variantID != null)
				request.setVariantID(variantID.getValue(state));
			request.setSubscriberNumber(subscriber.getValue(state));

			GetMembersResponse response = state.getVasService().getMembers(state, request);
			state.setLastReturnCode(response.getReturnCode());
			if (!response.wasSuccess())
				return errorAction;

			Number[] members = response.getMembers();
			ContactInfo[] contactInfo = response.getContactInfo();
			ContactNumber[] contactNumbers = null;

			if (members != null && members.length > 0)
			{
				contactNumbers = new ContactNumber[members.length];

				for (int index = 0; index < members.length; index++)
				{
					Number member = members[index];
					contactNumbers[index] = new ContactNumber(member, contactInfo == null || contactInfo.length <= index || contactInfo[index] == null ? null : contactInfo[index].getName());
				}
			}

			state.set(this, "hasMembers", members != null && members.length > 0);
			state.set(this, "Members", contactNumbers);

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
