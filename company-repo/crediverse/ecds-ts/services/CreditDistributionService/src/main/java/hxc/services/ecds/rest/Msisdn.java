package hxc.services.ecds.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheckException;

@Path("/msisdn")
public class Msisdn
{
	final static Logger logger = LoggerFactory.getLogger(Msisdn.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	@Context
	protected ICreditDistribution context;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Msisdn()
	{
	}

	public Msisdn(ICreditDistribution context)
	{
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	// /////////////////////////////////

	@GET
	@Path("/format/{raw_msisdn}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Msisdn toNationalFormat(@PathParam("raw_msisdn") String rawMsisdn)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
            String formattedMsisdn = context.toMSISDN(rawMsisdn);
            
            hxc.ecds.protocol.rest.Msisdn msisdn = new hxc.ecds.protocol.rest.Msisdn();
            msisdn.setMsisdn(formattedMsisdn);

			return msisdn;
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}
}
