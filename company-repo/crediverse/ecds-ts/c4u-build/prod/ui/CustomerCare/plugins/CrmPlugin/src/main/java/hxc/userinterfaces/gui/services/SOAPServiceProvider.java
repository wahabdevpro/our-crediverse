package hxc.userinterfaces.gui.services;

import hxc.connectors.soap.HxCService;
import hxc.connectors.soap.IHxC;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.utils.HostInfo;

import java.net.URL;
import java.net.URLDecoder;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import com.concurrent.hxc.Channels;
import com.concurrent.hxc.GetLocaleSettingsRequest;
import com.concurrent.hxc.GetLocaleSettingsResponse;
import com.concurrent.hxc.GetServicesRequest;
import com.concurrent.hxc.GetServicesResponse;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.NumberPlan;
import com.concurrent.hxc.NumberType;
import com.concurrent.hxc.RequestModes;
import com.concurrent.hxc.VasServiceInfo;

public class SOAPServiceProvider
{
	private final String WS_URL = "http://localhost:14100/HxC";
	private static long startTransactionID = System.currentTimeMillis();
	private final String SOAP_VERSION = "1"; // This comes from the RequestHeader constant in hxc.utils.protocol.soap.RequestHeader

	public String createTransactionID()
	{
		return String.valueOf(startTransactionID++);
	}

	public IHxC getSOAPServicePort(String userName, String password)
	{
		IHxC port = null;

		try
		{
			URL url = HxCService.class.getProtectionDomain().getCodeSource().getLocation();
			String location = "jar:" + url.toString() + "!/META-INF/wsdl/HxCService.wsdl";
			URL wsdlURL = new URL(location);
			String jarPath = URLDecoder.decode(url.getFile(), "UTF-8");
			QName qname = new QName("http://soap.connectors.hxc/", "HxCService");
			HxCService hxcService = new HxCService(wsdlURL, qname);
			port = hxcService.getHxCPort();

			BindingProvider bindingProvider = (BindingProvider) port;
			bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, WS_URL);
			bindingProvider.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, userName);
			bindingProvider.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
			// bindingProvider.getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, REQUEST_TIMEOUT);
			// bindingProvider.getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
		}
		catch (Exception e)
		{
			port = null;
		}
		return port;
	}

	public IHxC getSOAPServicePort(User user)
	{
		return getSOAPServicePort(user.getUserId(), user.getPassword());
	}

	public Number generateMSISDNNumber(String msisdn)
	{
		Number result = new Number();
		result.setAddressDigits(msisdn);
		result.setNumberPlan(NumberPlan.UNKNOWN);
		result.setNumberType(NumberType.UNKNOWN);
		return result;
	}

	public String getSoapVersion()
	{
		return SOAP_VERSION;
	}

	public GetLocaleSettingsResponse getLocaleSettings(User user)
	{
		GetLocaleSettingsResponse result = null;
		try
		{
			IHxC port = getSOAPServicePort(user);

			GetLocaleSettingsRequest req = new GetLocaleSettingsRequest();
			req.setLanguageID(user.getLanguageId());
			req.setCallerID(user.getUserId());
			req.setTransactionID(createTransactionID());
			req.setSessionID(user.getUserId());
			req.setChannel(Channels.CRM);

			req.setHostName(HostInfo.getNameOrElseHxC());
			req.setMode(RequestModes.NORMAL);
			req.setVersion(getSoapVersion());

			result = port.getLocaleSettings(req);
		}
		catch (Exception e)
		{
			return null;
		}
		return result;
	}

	public GetServicesResponse getServices(User user, String msisdn, int languageId)
	{
		GetServicesResponse result = null;
		try
		{
			// First get All services
			IHxC port = getSOAPServicePort(user);
			GetServicesRequest serReq = new GetServicesRequest();
			serReq.setLanguageID(languageId);
			serReq.setCallerID(user.getUserId());
			serReq.setChannel(Channels.CRM);
			serReq.setMode(RequestModes.NORMAL);
			serReq.setActiveOnly(false);
			serReq.setSubscriberNumber(generateMSISDNNumber(msisdn));
			result = port.getServices(serReq);

			// Now only active services
			serReq.setActiveOnly(true);
			GetServicesResponse onlyActive = port.getServices(serReq);
			for (VasServiceInfo si : onlyActive.getServiceInfo())
			{
				for (int i = 0; i < result.getServiceInfo().size(); i++)
				{
					if (result.getServiceInfo().get(i).getServiceID().equals(si.getServiceID()) && result.getServiceInfo().get(i).getVariantID().equals(si.getVariantID()))
					{
						result.getServiceInfo().get(i).setState(si.getState());
						break;
					}
				}
			}

		}
		catch (Exception e)
		{
			// e.printStackTrace();
		}
		return result;
	}

}
