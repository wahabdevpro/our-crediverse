package hxc.userinterfaces.gui.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.thymeleaf.context.WebContext;

import hxc.userinterfaces.cc.data.crm.ServiceEntry;
import hxc.userinterfaces.cc.utils.CCUtils;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.jetty.JettyMain;
import hxc.userinterfaces.gui.json.GuiUpdateResponse;
import hxc.userinterfaces.gui.json.GuiUpdateResponse.OperationStatus;
import hxc.userinterfaces.gui.services.IGlobalVasService;
import hxc.userinterfaces.gui.services.IServiceHandlerMappings;
import hxc.userinterfaces.gui.structs.BaseServlet;
import hxc.userinterfaces.gui.translations.CreditSharingTranslations;
import com.concurrent.hxc.GetHistoryResponse;
import com.concurrent.hxc.GetServicesResponse;
import com.concurrent.hxc.ReturnCodes;
import com.concurrent.hxc.ServiceHistory;
import com.concurrent.hxc.SubscriptionState;
import com.concurrent.hxc.VasServiceInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@WebServlet(urlPatterns = { "/custcare" }, name = "CustomerCare", asyncSupported = true)
@SuppressWarnings("serial")
public class CustomerCare extends BaseServlet
{
	// private final String CREDIT_SHARING_SERVICE_ID = "CrShr";
	private final String CREDIT_SHARING_SERVICE_ID = "GSA";
	// private final String TEST_VARIANT_ID = "Daily";

	@Resource(name = "serviceMappings")
	private IServiceHandlerMappings serviceMappings;

	@Resource(name = "globalVasService")
	private IGlobalVasService globalVasService;

	// --------------------------------------------------------------------------------------------------------
	// Page Handlers methods
	// Note: All handlers are created by convention: a request of act parameter is translated by adding Handler after it
	// Therefore: ?act=something will call somethingHandler(HttpServletRequest request ...)
	// When method not found defaultHandler(HttpServletRequest request ...) called
	// --------------------------------------------------------------------------------------------------------
	@Override
	public void defaultHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException
	{
		// This will be called if there is a clean call to this url (i.e. no details supplied or submitted)
		if (user == null || user.getName() == null)
		{
			ctx.setVariable("VERSION", JettyMain.getVersion());
			sendTemplateResponse(response, ctx, "login");
		}
		else
		{
			// load main and enter this page as content
			String uri = request.getRequestURI();
			request.getRequestDispatcher("/?content=" + uri.substring(1)).forward(request, response);
		}
	}

	public void contentHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException, NoSuchAlgorithmException
	{
		ctx.setVariable("language", (user.getLanguageId() == 1) ? "fr" : "en");
		sendTemplateResponse(response, ctx, "custcare");
	}

	private List<VasServiceInfo> extractActiveServices(List<VasServiceInfo> services)
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

	public void retMsisdnHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String msisdn = request.getParameter("msisdn");

		ctx.setVariable("msisdn", msisdn);
		ctx.setVariable("utils", new CCUtils());

		// Subscriptions could have
		GetServicesResponse serviceResponse = globalVasService.getServices(user, msisdn, 1);

		String json = null;
		if (serviceResponse != null && serviceResponse.getReturnCode() == ReturnCodes.SUCCESS)
		{
			Set<String> serviceIds = new TreeSet<>();
			for (VasServiceInfo vi : serviceResponse.getServiceInfo())
				serviceIds.add(vi.getServiceID());

			List<ServiceEntry> services = new ArrayList<>();
			if (serviceMappings.getServiceControlers() != null)
			{
				for (String serviceId : serviceIds)
				{
					if (serviceMappings.getServiceControlers().containsKey(serviceId))
						services.add(new ServiceEntry(serviceId, serviceMappings.getServiceControlers().get(serviceId)));
				}
			}

			json = (new Gson()).toJson(services);

		}
		else
		{
			GuiUpdateResponse guiResponse = new GuiUpdateResponse(OperationStatus.fail, "An invalid number was supplied");
			json = guiResponse.toString();
		}

		sendResponse(response, json);
	}

	public void getHistoryHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String msisdn = request.getParameter("msisdn");

		// Is this msisdn a provider(A) or consumer (B)
		// GetOwnersResponse ownersResponse = globalVasService.getOwners(user, msisdn, CREDIT_SHARING_SERVICE_ID);
		// boolean provider = true; // Default
		// if (ownersResponse != null && ownersResponse.getReturnCode() == ReturnCodes.SUCCESS)
		// {
		// if (ownersResponse.getOwners() != null && ownersResponse.getOwners().size() > 0)
		// {
		// provider = false;
		// }
		// }
		CreditSharingTranslations ct = new CreditSharingTranslations(user.getLanguageId());
		// GetHistoryResponse resp = globalVasService.getHistory(user, (provider ? msisdn : null), (provider ? null : msisdn), CREDIT_SHARING_SERVICE_ID, null);
		GetHistoryResponse resp = globalVasService.getHistory(user, msisdn, null, CREDIT_SHARING_SERVICE_ID, null);
		JsonObject job = new JsonObject();
		JsonArray jarr = new JsonArray();
		try
		{
			if (resp.getReturnCode() == ReturnCodes.SUCCESS)
			{
				String success = "<span class='glyphicon glyphicon-ok' style='color:green;'></span>";
				String fail = "<span class='glyphicon glyphicon-remove' style='color:red;'></span>";

				if (resp.getHistory() != null)
				{
					CCUtils utils = new CCUtils();

					for (ServiceHistory sh : resp.getHistory())
					{
						String date = globalVasService.formatDate(user, sh.getStartTime().toGregorianCalendar().getTime());
						String time = globalVasService.formatTime(sh.getStartTime().toGregorianCalendar().getTime());

						jarr.add(CCUtils.createJsonDataArray(new String[] { date, time, sh.getAMSISDN(), sh.getBMSISDN(), ct.translate(sh.getChannel().toString()),
								String.valueOf(sh.getChargeLevied()), ct.translate(sh.getProcessID()), (sh.getReturnCode() == ReturnCodes.SUCCESS) ? success : fail }));
					}
				}

			}
		}
		catch (Exception e)
		{
			// Removes invlid JSON context
		}

		job.add("data", jarr);
		sendResponse(response, job.toString());

	}

	// -------------------------------- Below this line will invariably move ------------------------------------------------

}
