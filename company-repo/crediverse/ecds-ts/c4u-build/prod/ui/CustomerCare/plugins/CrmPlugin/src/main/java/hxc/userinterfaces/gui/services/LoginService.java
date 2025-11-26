package hxc.userinterfaces.gui.services;

import java.net.ConnectException;
import java.util.Date;

import com.concurrent.hxc.PingRequest;
import com.concurrent.hxc.PingResponse;

import hxc.connectors.soap.IHxC;
import hxc.userinterfaces.cc.data.Version;
import hxc.userinterfaces.gui.data.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginService implements ILoginService
{
	private static Logger logger = LoggerFactory.getLogger(LoginService.class);

	private User loginUsingSOAP(String userName, String password)
	{
		User user = null;

		SOAPServiceProvider serviceProvider = new SOAPServiceProvider();

		IHxC port = serviceProvider.getSOAPServicePort(userName, password);

		if (port != null)
		{
			PingRequest pr = new PingRequest();
			pr.setSeq(1);
			PingResponse resp = port.ping(pr);
			if (resp.getSeq() == 2)
			{
				user = new User(userName, password);
				user.setName(userName);
				user.setSessionId(String.valueOf((new Date()).getTime()));
			}
		}
		else
		{
			logger.error("logger port could not be resolved for loginUsingSOAP");
		}

		return user;
	}

	@Override
	public User login(String userName, String password)
	{
		User user = null;
		try
		{
			user = loginUsingSOAP(userName, password);
		}
		catch (Exception e)
		{
			if (e.getCause() != null && e.getCause() instanceof ConnectException)
			{
				user = new User();
				user.setLastLoginError("soapfail");
			}
		}

		if (user == null)
		{
			user = new User();
			user.setLastLoginError("loginfail");
		}

		return user;
	}

	@Override
	public String getVersion()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(Version.major).append(".");
		if (Version.revision.equalsIgnoreCase("[REVISION]"))
		{
			sb.append("0");
		}
		else
		{
			sb.append(Version.revision);
		}
		return sb.toString();
	}

}
