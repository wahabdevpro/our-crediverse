package hxc.services.ecds.util;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import hxc.services.ecds.rest.ICreditDistribution;

@PreMatching
@Priority(value = 3)
@Provider
public class RequestNumberFilter implements ContainerRequestFilter
{
	private static ICreditDistribution context;

	public static void setContext(ICreditDistribution context)
	{
		RequestNumberFilter.context = context;
	}

	public RequestNumberFilter()
	{

	}

	@Override
	public void filter(ContainerRequestContext requestContext)
	{
		context.assignTsNumber(true);
	}
}