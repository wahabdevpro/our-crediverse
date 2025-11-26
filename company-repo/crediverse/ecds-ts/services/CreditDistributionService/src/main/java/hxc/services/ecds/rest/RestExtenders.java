package hxc.services.ecds.rest;

import javax.inject.Singleton;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.spi.ExtendedExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Encapsulate all classes that extends Jersey RESTFul framework with specifics
 * 
 */
public class RestExtenders
{
	final static Logger logger = LoggerFactory.getLogger(RestExtenders.class);

	@Provider
	static public class RestExceptionMapper implements ExtendedExceptionMapper<Exception>
	{

		@Override
		public boolean isMappable(Exception ex)
		{
			if (ex != null)
			{
				logger.error(ex.getMessage(), ex);
			}

			// Always return false otherwise will convert an exception to a valid response
			// If necessary, consider implementing toResponse method
			return false;
		}

		@Override
		public Response toResponse(Exception arg0)
		{
			// TODO Auto-generated method stub
			return null;
		}

	}

	static public class DependencyBinder extends AbstractBinder
	{
		private ICreditDistribution context;

		public DependencyBinder(ICreditDistribution c)
		{
			context = c;
		}

		@Override
		protected void configure()
		{
			bindFactory(new ICreditDistributionFactory(context)).to(ICreditDistribution.class).in(Singleton.class);
		}

	}

	static public class ICreditDistributionFactory implements Factory<ICreditDistribution>
	{
		@Context
		private ICreditDistribution context;

		public ICreditDistributionFactory(ICreditDistribution c)
		{
			context = c;
		}

		@Override
		public ICreditDistribution provide()
		{
			return context;
		}

		@Override
		public void dispose(ICreditDistribution arg0)
		{

		}
	}
}
