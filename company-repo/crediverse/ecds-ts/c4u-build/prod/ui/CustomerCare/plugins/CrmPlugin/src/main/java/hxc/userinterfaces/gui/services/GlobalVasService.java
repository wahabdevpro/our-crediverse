package hxc.userinterfaces.gui.services;

import hxc.connectors.soap.IHxC;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.utils.HostInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.concurrent.hxc.Channels;
import com.concurrent.hxc.GetHistoryRequest;
import com.concurrent.hxc.GetHistoryResponse;
import com.concurrent.hxc.GetLocaleSettingsRequest;
import com.concurrent.hxc.GetLocaleSettingsResponse;
import com.concurrent.hxc.GetServicesRequest;
import com.concurrent.hxc.GetServicesResponse;
import com.concurrent.hxc.RequestModes;
import com.concurrent.hxc.ReturnCodes;
import com.concurrent.hxc.SubscriptionState;
import com.concurrent.hxc.VasServiceInfo;

public class GlobalVasService implements IGlobalVasService
{

	private Map<Integer, String> DATE_FORMATS = new HashMap<>();
	private final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd";
	private final String DEFAULT_TIME_FORMAT = "HH:mm:SS";

	private SOAPServiceProvider serviceProvider = new SOAPServiceProvider();

	@Override
	public GetServicesResponse getServices(User user, String msisdn, int languageId)
	{
		GetServicesResponse result = null;
		try
		{
			// First get All services
			IHxC port = serviceProvider.getSOAPServicePort(user);
			GetServicesRequest serReq = new GetServicesRequest();
			serReq.setLanguageID(languageId);
			serReq.setCallerID(user.getUserId());
			serReq.setChannel(Channels.CRM);
			serReq.setMode(RequestModes.NORMAL);
			serReq.setActiveOnly(false);
			serReq.setSubscriberNumber(serviceProvider.generateMSISDNNumber(msisdn));
			result = port.getServices(serReq);

			// Now only active services
			serReq.setActiveOnly(true);
			GetServicesResponse onlyActive = port.getServices(serReq);
			for (VasServiceInfo si : onlyActive.getServiceInfo())
			{
//				System.out.println(si.getServiceName() + "-> " + si.getServiceID());
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

	@Override
	public List<VasServiceInfo> getServiceVariantsList(User user, String msisdn, String serviceId, int languageId)
	{
		GetServicesResponse services = getServices(user, msisdn, languageId);

		List<VasServiceInfo> result = new ArrayList<>();
		for (VasServiceInfo vs : services.getServiceInfo())
		{
			if (vs.getServiceID().equalsIgnoreCase(serviceId))
				result.add(vs);
		}
		return result;
	}

	@Override
	public GetLocaleSettingsResponse getLocaleSettings(User user)
	{
		GetLocaleSettingsResponse result = null;
		try
		{
			IHxC port = serviceProvider.getSOAPServicePort(user);

			GetLocaleSettingsRequest req = new GetLocaleSettingsRequest();
			req.setLanguageID(user.getLanguageId());
			req.setCallerID(user.getUserId());
			req.setTransactionID(serviceProvider.createTransactionID());
			req.setSessionID(user.getUserId());
			req.setChannel(Channels.CRM);

			req.setHostName(HostInfo.getNameOrElseHxC());
			req.setMode(RequestModes.NORMAL);
			req.setVersion(serviceProvider.getSoapVersion());

			result = port.getLocaleSettings(req);
		}
		catch (Exception e)
		{
			return null;
		}
		return result;
	}

	@Override
	public String getDateFormat(User user)
	{
		String result = null;
		try
		{
			if (!DATE_FORMATS.containsKey(user.getLanguageId()))
			{
				GetLocaleSettingsResponse localSettings = getLocaleSettings(user);
				if ((localSettings != null) && (localSettings.getReturnCode() == ReturnCodes.SUCCESS))
				{
					DATE_FORMATS.put(user.getLanguageId(), localSettings.getDateFormat());
				}
			}
			result = DATE_FORMATS.get(user.getLanguageId());

		}
		catch (Exception e)
		{
		}
		if (result == null)
		{
			result = DEFAULT_DATE_FORMAT;
		}
		return result;
	}

	@Override
	public synchronized String formatDate(User user, Date date)
	{
		String format = getDateFormat(user);
		SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		return sdf.format(date);
	}

	@Override
	public synchronized String formatTime(Date date)
	{
		String result = null;
		SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_TIME_FORMAT);
		result = sdf.format(date);
		return result;
	}

	@Override
	public List<VasServiceInfo> extractActiveServices(List<VasServiceInfo> services)
	{
		List<VasServiceInfo> result = new ArrayList<>();
		for (VasServiceInfo vis : services)
		{
			if (vis.getState() == SubscriptionState.ACTIVE)
			{
				result.add(vis);
			}
		}
		return result;
	}

	@Override
	public GetHistoryResponse getHistory(User user, String msisdnA, String msisdnB, String serviceId, String variantId)
	{
		GetHistoryResponse result = null;
		try
		{
			IHxC port = serviceProvider.getSOAPServicePort(user);

			GetHistoryRequest req = new GetHistoryRequest();
			req.setLanguageID(user.getLanguageId());
			req.setCallerID(user.getUserId());
			req.setTransactionID(serviceProvider.createTransactionID());
			req.setSessionID(user.getUserId());
			req.setChannel(Channels.CRM);

			req.setHostName(HostInfo.getNameOrElseHxC());
			req.setMode(RequestModes.NORMAL);
			req.setVersion("1");
			req.setRowLimit(100);
			req.setInReverse(true);

			if (msisdnA != null)
			{
				req.setANumber(serviceProvider.generateMSISDNNumber(msisdnA));
			}
			if (msisdnB != null)
			{
				req.setBNumber(serviceProvider.generateMSISDNNumber(msisdnB));
			}
			req.setServiceID(serviceId);
			result = port.getHistory(req);
		}
		catch (Exception e)
		{
			// e.printStackTrace();
		}
		return result;
	}

}
