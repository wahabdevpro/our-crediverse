package hxc.services.ecds.rest;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import hxc.services.ecds.util.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/management")
public class Management
{
	final static Logger logger = LoggerFactory.getLogger(Management.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Ping
	//
	// /////////////////////////////////
	@GET
	@Path("/ping")
	@Produces(MediaType.TEXT_PLAIN)
	public String ping()
	{
		return "pong";
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Error Codes
	//
	// /////////////////////////////////
	@GET
	@Path("/error_codes")
	@Produces(MediaType.APPLICATION_JSON)
	public String[] getErrorCodes()
	{
		List<String> result = new ArrayList<String>();

		addCodes(result, hxc.ecds.protocol.rest.Violation.class, null);
		addCodes(result, hxc.ecds.protocol.rest.ResponseHeader.class, null);
		addCodes(result, hxc.ecds.protocol.rest.AuthenticationResponse.class, "CODE_FAIL_");
		addCodes(result, hxc.ecds.protocol.rest.BatchIssue.class, null);
		addCodes(result, hxc.ecds.protocol.rest.config.TransactionsConfig.class, "ERR_");
		addCodes(result, hxc.services.ecds.util.StatusCode.class);

		return result.toArray(new String[result.size()]);
	}

	private void addCodes(List<String> result, Class<StatusCode> cls)
	{
		Field[] fields = cls.getDeclaredFields();
		for (Field field : fields)
		{
			String name = field.getName();
			if (name == null || !name.equals(name.toUpperCase()))
				continue;
			int modifiers = field.getModifiers();
			if ((modifiers & Modifier.FINAL) == 0 || (modifiers & Modifier.STATIC) == 0)
				continue;
			if (!StatusCode.class.getName().equals(field.getType().getName()))
				continue;
			try
			{
				StatusCode value = (StatusCode) field.get(null);
				if (value!= null && !result.contains(value.getName()))
					result.add(value.getName());
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				logger.error("", e);
				continue;
			}
		}
	}

	private void addCodes(List<String> result, Class<?> cls, String like)
	{
		Field[] fields = cls.getDeclaredFields();
		for (Field field : fields)
		{
			String code = findCode(field, like);
			if (code != null && !result.contains(code))
				result.add(code);
		}
	}

	private String findCode(Field field, String like)
	{
		String name = field.getName();
		if (name == null || !name.equals(name.toUpperCase()))
			return null;
		if (like != null && !name.contains(like))
			return null;
		int modifiers = field.getModifiers();
		if ((modifiers & Modifier.FINAL) == 0 || (modifiers & Modifier.STATIC) == 0)
			return null;
		if (!String.class.getName().equals(field.getType().getName()))
			return null;
		try
		{
			String value = (String) field.get(null);
			return value;
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			logger.error("", e);
			return null;
		}
	}

	// hxc.ecds.protocol.rest.Violation (10)
	// hxc.ecds.protocol.rest.ResponseHeader (2)
	// hxc.ecds.protocol.rest.AuthenticationResponse (10)
	// hxc.ecds.protocol.rest.TransactionsConfig (27)
	// hxc.ecds.protocol.rest.BatchIssue (13)
	// hxc.services.ecds.util.StatusCode (39)
}
