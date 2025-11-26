package hxc.userinterfaces.gui.services;

import hxc.userinterfaces.gui.data.User;

import java.util.Date;
import java.util.List;

import com.concurrent.hxc.GetHistoryResponse;
import com.concurrent.hxc.GetLocaleSettingsResponse;
import com.concurrent.hxc.GetServicesResponse;
import com.concurrent.hxc.VasServiceInfo;

/**
 * General Service Calls - Non Service Specific
 * 
 * @author johne
 * 
 */
public interface IGlobalVasService
{

	public GetServicesResponse getServices(User user, String msisdn, int languageId);

	public List<VasServiceInfo> getServiceVariantsList(User user, String msisdn, String serviceId, int languageId);

	public String getDateFormat(User user);

	public GetLocaleSettingsResponse getLocaleSettings(User user);

	public String formatDate(User user, Date date);

	public String formatTime(Date date);

	public List<VasServiceInfo> extractActiveServices(List<VasServiceInfo> services);

	public GetHistoryResponse getHistory(User user, String msisdnA, String msisdnB, String serviceId, String variantId);

}
