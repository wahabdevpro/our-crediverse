package hxc.services.airsim;

import java.util.HashMap;
import java.util.Map;

public class SupportedRequests
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Map<String, SupportedRequest<?, ?>> requests = new HashMap<String, SupportedRequest<?, ?>>();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public void add(SupportedRequest<?, ?> request)
	{
		requests.put(request.getRequestType().getSimpleName(), request);
	}

	public Object execute(Object rpc, ISimulationData simulationData) throws Exception
	{
		if (rpc == null)
			return null;
		
		SupportedRequest<?, ?> request = requests.get(rpc.getClass().getSimpleName());
		if (request == null)
			return null;
		
		return request.execute(rpc, simulationData);
	}

	public Class<?>[] getRequestTypes()
	{
		Class<?>[] result = new Class<?>[requests.size()];

		int index = 0;
		for (SupportedRequest<?, ?> request:requests.values())
		{
			result[index++] = request.getRequestType();
		}

		return result;
	}

}
