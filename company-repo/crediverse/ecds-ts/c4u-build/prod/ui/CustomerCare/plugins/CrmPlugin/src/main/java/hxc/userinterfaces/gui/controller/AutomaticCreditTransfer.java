package hxc.userinterfaces.gui.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import hxc.userinterfaces.cc.data.AddServiceQuotaFeedBack;
import hxc.userinterfaces.cc.data.JsonBalances;
import hxc.userinterfaces.cc.data.QuotaInfo;
import hxc.userinterfaces.cc.utils.CCUtils;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.jetty.JettyMain;
import hxc.userinterfaces.gui.json.GuiUpdateResponse;
import hxc.userinterfaces.gui.json.GuiUpdateResponse.OperationStatus;
import hxc.userinterfaces.gui.services.IGlobalVasService;
import hxc.userinterfaces.gui.services.ISubscriptionService;
import hxc.userinterfaces.gui.structs.BaseServlet;
import hxc.userinterfaces.gui.translations.CreditSharingTranslations;
import hxc.userinterfaces.gui.translations.CreditSharingTranslations.MessageContext;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.thymeleaf.context.WebContext;

import com.concurrent.hxc.AddCreditTransferResponse;
import com.concurrent.hxc.AddMemberResponse;
import com.concurrent.hxc.AddQuotaResponse;
import com.concurrent.hxc.ChangeQuotaResponse;
import com.concurrent.hxc.CreditTransfer;
import com.concurrent.hxc.GetBalancesResponse;
import com.concurrent.hxc.GetCreditTransfersResponse;
import com.concurrent.hxc.GetLocaleSettingsResponse;
import com.concurrent.hxc.GetMembersResponse;
import com.concurrent.hxc.GetQuotasResponse;
import com.concurrent.hxc.GetServicesResponse;
import com.concurrent.hxc.RemoveCreditTransfersResponse;
import com.concurrent.hxc.RemoveMemberResponse;
import com.concurrent.hxc.ReturnCodes;
import com.concurrent.hxc.ServiceQuota;
import com.concurrent.hxc.SubscribeResponse;
import com.concurrent.hxc.SubscriptionState;
import com.concurrent.hxc.UnsubscribeResponse;
import com.concurrent.hxc.VasServiceInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@WebServlet(urlPatterns = { "/autoxfr" }, name = "ACTServlet", asyncSupported = true, initParams = { @WebInitParam(name = "start", value = "1") })
@SuppressWarnings("serial")
public class AutomaticCreditTransfer extends BaseServlet
{

	private final String ACT_SERVICE_ID = "AutoXfr";

	@Resource(name = "globalVasService")
	private IGlobalVasService globalVasService;

	@Resource(name = "actService")
	private ISubscriptionService actService;

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

	public void retServiceInfoHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String msisdn = request.getParameter("msisdn");
		ctx.setVariable("msisdn", msisdn);
		ctx.setVariable("utils", new CCUtils());

		// MSISDN is Provider
		List<VasServiceInfo> services = globalVasService.getServiceVariantsList(user, msisdn, ACT_SERVICE_ID, user.getLanguageId());
		List<VasServiceInfo> subscribedServices = globalVasService.extractActiveServices(services);

		boolean subscribedToService = false;
		for (VasServiceInfo vs : services)
		{
			if (vs.getState().equals(SubscriptionState.ACTIVE) && (vs.getServiceID().equals(ACT_SERVICE_ID)))
				subscribedToService = true;
		}

		ctx.setVariable("subscribed", subscribedToService);
		ctx.setVariable("services", services);
		ctx.setVariable("subscribedServices", subscribedServices);
		ctx.setVariable("serviceName", "Automatic Credit Transfer");

		sendTemplateResponse(response, ctx, "servicelist");

	}

	// ---------------------------- Subscribe -----------------------------------------------------------------

	private String subscribeCall(HttpServletRequest request, User user, boolean testMode)
	{
		String msisdn = request.getParameter("msisdn");
		String serviceID = request.getParameter("sid");
		String variantID = request.getParameter("vid");

		VasServiceInfo vasInfo = actService.getServiceInfo(user, msisdn, serviceID, variantID, user.getLanguageId());
		String variantName = (vasInfo == null) ? variantID : vasInfo.getVariantName();

		SubscribeResponse subResponse = actService.subscribe(user, msisdn, serviceID, variantID, user.getLanguageId(), testMode);
		GuiUpdateResponse guiResponse = new GuiUpdateResponse();
		CreditSharingTranslations ct = new CreditSharingTranslations(user.getLanguageId());

		if (subResponse.getReturnCode() == ReturnCodes.SUCCESS)
		{
			guiResponse.setStatus(OperationStatus.pass);
			guiResponse.setMessage(ct.translate(MessageContext.subscribeSuccess, variantName, String.valueOf(subResponse.getChargeLevied())));
		}
		else if (subResponse.getReturnCode() == ReturnCodes.SUCCESSFUL_TEST)
		{
			guiResponse.setStatus(OperationStatus.pass);
			if (subResponse.getChargeLevied() > 0)
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmSubscribeMessageWithCost, msisdn, variantName, String.valueOf(subResponse.getChargeLevied())));
			}
			else
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmSubscribeMessageNoCost, msisdn, variantName));
			}
		}
		else
		{
			guiResponse.setStatus(OperationStatus.fail);
			guiResponse.setMessage(actService.getReturnCodeTranslation(user, subResponse.getReturnCode(), user.getLanguageId()));
		}

		return guiResponse.toString();
	}

	public void subscribeHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = subscribeCall(request, user, false);
		sendResponse(response, json);
	}

	public void subscribeTestHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = subscribeCall(request, user, true);
		sendResponse(response, json);
	}

	public void viewServiceHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		GetMembersResponse beneficiaries = getBeneficiaries(user, request, ctx);

		ctx.setVariable("beneficiaries", beneficiaries);
		ctx.setVariable("nobeneficiaries", (beneficiaries.getMembers() == null || beneficiaries.getMembers().size() == 0));

		String msisdn = request.getParameter("msisdn");
		String serviceID = request.getParameter("sid");
		String variantID = request.getParameter("vid");
		GetBalancesResponse balResponse = actService.retrieveBalances(user, msisdn, serviceID, variantID, user.getLanguageId(), true);
		balResponse.getChargeLevied();

		boolean balanceRequestFree = (balResponse.getReturnCode() == ReturnCodes.SUCCESSFUL_TEST && balResponse.getChargeLevied() == 0L);

		ctx.setVariable("balancerequestfree", balanceRequestFree);
		sendTemplateResponse(response, ctx, "autoxfr/actservice");
	}

	private String addBeneficiaryCall(HttpServletRequest request, User user, boolean testMode)
	{
		String msisdn = request.getParameter("msisdn");
		String ben = request.getParameter("ben");
		String serviceId = request.getParameter("sid");
		String variantId = request.getParameter("vid");

		AddMemberResponse addMemberResponse = actService.addBeneficiary(user, msisdn, ben, serviceId, variantId, user.getLanguageId(), testMode);
		CreditSharingTranslations ct = new CreditSharingTranslations(user.getLanguageId());

		GuiUpdateResponse guiResponse = new GuiUpdateResponse();
		if (addMemberResponse.getReturnCode() == ReturnCodes.SUCCESS)
		{
			guiResponse.setStatus(OperationStatus.pass);
			guiResponse.setMessage(ct.translate(MessageContext.consumerAddSuccess, ben, String.valueOf(addMemberResponse.getChargeLevied())));
		}
		else if (addMemberResponse.getReturnCode() == ReturnCodes.SUCCESSFUL_TEST)
		{
			guiResponse.setStatus(OperationStatus.pass);
			if (addMemberResponse.getChargeLevied() > 0)
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmAddConsumerMessageWithCost, ben, msisdn, String.valueOf(addMemberResponse.getChargeLevied())));
			}
			else
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmAddConsumerMessageNoCost, ben, msisdn));
			}
		}
		else
		{
			guiResponse.setStatus(OperationStatus.fail);
			guiResponse.setMessage(actService.getReturnCodeTranslation(user, addMemberResponse.getReturnCode(), user.getLanguageId()));
		}

		return guiResponse.toString();
	}

	public void addBeneficiaryHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = addBeneficiaryCall(request, user, false);
		sendResponse(response, json);
	}

	public void addBeneficiaryTestHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = addBeneficiaryCall(request, user, true);
		sendResponse(response, json);
	}

	public void viewBeneficiariesHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		GetMembersResponse beneficiaries = getBeneficiaries(user, request, ctx);
		ctx.setVariable("beneficiaries", beneficiaries);
		sendTemplateResponse(response, ctx, "autoxfr/memberslist");
	}

	private String unsubscribeCall(HttpServletRequest request, User user, boolean testMode)
	{
		String msisdn = request.getParameter("msisdn");
		String serviceId = request.getParameter("sid");
		String variantId = request.getParameter("vid");

		VasServiceInfo vasInfo = actService.getServiceInfo(user, msisdn, serviceId, variantId, user.getLanguageId());
		String variantName = (vasInfo == null) ? variantId : vasInfo.getVariantName();

		UnsubscribeResponse unsubResponse = actService.unsubscribe(user, msisdn, serviceId, variantId, user.getLanguageId(), testMode);

		GuiUpdateResponse guiResponse = new GuiUpdateResponse();
		CreditSharingTranslations ct = new CreditSharingTranslations(user.getLanguageId());
		if (unsubResponse.getReturnCode() == ReturnCodes.SUCCESS)
		{
			guiResponse.setStatus(OperationStatus.pass);
			guiResponse.setMessage(ct.translate(MessageContext.unsubscribeSuccess, msisdn, String.valueOf(unsubResponse.getChargeLevied())));
		}
		else if (unsubResponse.getReturnCode() == ReturnCodes.SUCCESSFUL_TEST)
		{
			guiResponse.setStatus(OperationStatus.pass);
			if (unsubResponse.getChargeLevied() > 0)
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmUnsubscribeMessageWithCost, msisdn, variantName, String.valueOf(unsubResponse.getChargeLevied())));
			}
			else
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmUnsubscribeMessageNoCost, msisdn, variantName));
			}
		}
		else
		{
			guiResponse.setStatus(OperationStatus.fail);
			guiResponse.setMessage(actService.getReturnCodeTranslation(user, unsubResponse.getReturnCode(), user.getLanguageId()));
		}
		return guiResponse.toString();
	}

	public void unsubscribeHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = unsubscribeCall(request, user, false);
		sendResponse(response, json);
	}

	public void unsubscribeTestHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = unsubscribeCall(request, user, true);
		sendResponse(response, json);
	}

	private String removeBeneficiaryCall(HttpServletRequest request, User user, boolean testMode)
	{
		String msisdn = request.getParameter("msisdn");
		String serviceId = request.getParameter("sid");
		String variantId = request.getParameter("vid");
		String benMsisdn = request.getParameter("ben");

		RemoveMemberResponse resp = actService.removeMember(user, msisdn, benMsisdn, serviceId, variantId, user.getLanguageId(), testMode, true);
		GuiUpdateResponse guiResponse = new GuiUpdateResponse();
		CreditSharingTranslations ct = new CreditSharingTranslations(user.getLanguageId());

		if (resp.getReturnCode() == ReturnCodes.SUCCESS)
		{
			guiResponse.setStatus(OperationStatus.pass);
			guiResponse.setMessage(ct.translate(MessageContext.consumerRemoveSuccess, benMsisdn, msisdn, String.valueOf(resp.getChargeLevied())));
		}
		else if (resp.getReturnCode() == ReturnCodes.SUCCESSFUL_TEST)
		{
			guiResponse.setStatus(OperationStatus.pass);
			if (resp.getChargeLevied() > 0)
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmRemoveConsumerMessageWithCost, benMsisdn, msisdn, String.valueOf(resp.getChargeLevied())));
			}
			else
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmRemoveConsumerMessageNoCost, benMsisdn, msisdn));
			}
		}
		else
		{
			guiResponse.setStatus(OperationStatus.fail);
			guiResponse.setMessage(actService.getReturnCodeTranslation(user, resp.getReturnCode(), user.getLanguageId()));
		}

		return guiResponse.toString();
	}

	public void removeBeneficiaryHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = removeBeneficiaryCall(request, user, false);
		sendResponse(response, json);
	}

	public void removeBeneficiaryTestHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = removeBeneficiaryCall(request, user, true);
		sendResponse(response, json);
	}

	private String removeOwnerCall(HttpServletRequest request, User user, boolean testMode)
	{
		String msisdn = request.getParameter("owner");
		String serviceId = request.getParameter("sid");
		String variantId = request.getParameter("vid");
		String benMsisdn = request.getParameter("msisdn");

		RemoveMemberResponse resp = actService.removeMember(user, msisdn, benMsisdn, serviceId, variantId, user.getLanguageId(), testMode, false);
		GuiUpdateResponse guiResponse = new GuiUpdateResponse();
		CreditSharingTranslations ct = new CreditSharingTranslations(user.getLanguageId());

		if (resp.getReturnCode() == ReturnCodes.SUCCESS)
		{
			guiResponse.setStatus(OperationStatus.pass);
			guiResponse.setMessage(ct.translate(MessageContext.consumerRemoveSuccess, benMsisdn, msisdn, String.valueOf(resp.getChargeLevied())));
		}
		else if (resp.getReturnCode() == ReturnCodes.SUCCESSFUL_TEST)
		{
			guiResponse.setStatus(OperationStatus.pass);
			if (resp.getChargeLevied() > 0)
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmRemoveOwnerWithCost, msisdn, benMsisdn, String.valueOf(resp.getChargeLevied())));
			}
			else
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmRemoveOwnerNoCost, msisdn, benMsisdn));
			}
		}
		else
		{
			guiResponse.setStatus(OperationStatus.fail);
			guiResponse.setMessage(actService.getReturnCodeTranslation(user, resp.getReturnCode(), user.getLanguageId()));
		}

		return guiResponse.toString();
	}

	public void removeOwnerHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = removeOwnerCall(request, user, false);
		sendResponse(response, json);
	}

	public void removeOwnerTestHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = removeOwnerCall(request, user, true);
		sendResponse(response, json);
	}

	// MOVED!
	// public List<MemberQuota> quotaInfoToMemberInfo(User user, Object msisdn, String benMsisdn, String serviceId, String variantId)
	// {
	// // Retrieve Quota info
	// GetQuotasResponse quotas = null;
	// if (msisdn instanceof String)
	// {
	// quotas = actService.getQuotas(user, (String) msisdn, benMsisdn, serviceId, variantId, false, user.getLanguageId());
	// }
	// else
	// {
	// quotas = actService.getQuotas(user, (Number) msisdn, benMsisdn, serviceId, variantId, false, user.getLanguageId());
	// }
	//
	// List<MemberQuota> ma = new ArrayList<>();
	//
	// GetBalancesResponse bal = actService.retrieveBalances(user, benMsisdn, serviceId, variantId, user.getLanguageId(), false);
	//
	// if (bal.getReturnCode() == ReturnCodes.SUCCESS)
	// {
	// for (int i = 0; i < quotas.getServiceQuotas().size(); i++)
	// {
	// MemberQuota mq = new MemberQuota();
	// if (bal.getBalances() != null)
	// {
	// for (ServiceBalance sb : bal.getBalances())
	// {
	// if (quotas.getServiceQuotas().get(i).getName().equalsIgnoreCase(sb.getName()))
	// {
	// mq.setBalance(String.valueOf(bal.getBalances().get(i).getValue()));
	// XMLGregorianCalendar date = bal.getBalances().get(i).getExpiryDate();
	// if (date != null)
	// {
	// GregorianCalendar gc = date.toGregorianCalendar();
	// String strDate = globalVasService.formatDate(user, gc.getTime());
	// mq.setExpDate(strDate);
	// }
	// break;
	// }
	// }
	// }
	//
	// mq.setService(quotas.getServiceQuotas().get(i).getService());
	// mq.setDestination(quotas.getServiceQuotas().get(i).getDestination());
	// mq.setDaysOfWeek(quotas.getServiceQuotas().get(i).getDaysOfWeek());
	// mq.setTimeOfDay(quotas.getServiceQuotas().get(i).getTimeOfDay());
	// mq.setQuantity(quotas.getServiceQuotas().get(i).getQuantity());
	// mq.setUnits(quotas.getServiceQuotas().get(i).getUnits());
	// mq.setQuotaID(quotas.getServiceQuotas().get(i).getQuotaID());
	// ma.add(mq);
	// }
	// }
	//
	// return ma;
	// }

	// This is for Viewing Member Transfer List
	public void viewQuotasHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String msisdn = request.getParameter("msisdn");
		String serviceId = request.getParameter("sid");
		String variantId = request.getParameter("vid");
		String benMsisdn = request.getParameter("ben");

		ctx.setVariable("msisdn", msisdn);
		ctx.setVariable("sid", serviceId);
		ctx.setVariable("vid", variantId);
		ctx.setVariable("benMsisdn", benMsisdn);

		// Retrieve Credit Transfer Info
		GetCreditTransfersResponse ctResponse = actService.getCreditTransfers(user, msisdn, benMsisdn, serviceId, null, null, true, false);

		if (ctResponse.getTransfers() != null)
		{
			ctx.setVariable("transfers", ctResponse.getTransfers());
			ctx.setVariable("notransfers", (ctResponse.getTransfers() == null || ctResponse.getTransfers().size() == 0));
		}
		else
		{
			ctx.setVariable("transfers", null);
			ctx.setVariable("notransfers", true);
		}
		ctx.setVariable("utils", new CCUtils());

		sendTemplateResponse(response, ctx, "autoxfr/membertransfers");
	}

	public void retrieveQuaotaInfoHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String msisdn = request.getParameter("msisdn");
		String serviceId = request.getParameter("sid");
		String variantId = request.getParameter("vid");
		String benMsisdn = request.getParameter("ben");

		GetQuotasResponse quotas = actService.getQuotas(user, msisdn, benMsisdn, serviceId, variantId, true, user.getLanguageId());
		QuotaInfo qi = new QuotaInfo();
		qi.importQuotaInfo(quotas.getServiceQuotas());

		String resp = qi.toString();
		sendResponse(response, resp);
	}

	public void addQuotaHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = addQuotaCall(request, user, false);
		sendResponse(response, json);
	}

	public void addQuotaTestHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = addQuotaCall(request, user, true);
		sendResponse(response, json);
	}

	private String addQuotaCall(HttpServletRequest request, User user, boolean testMode)
	{
		String msisdn = request.getParameter("msisdn");
		String serviceId = request.getParameter("sid");
		String variantId = request.getParameter("vid");
		String benMsisdn = request.getParameter("ben");

		String service = request.getParameter("service");
		String destination = request.getParameter("destination");
		String dow = request.getParameter("dow");
		String tod = request.getParameter("tod");
		String quantity = request.getParameter("quantity");

		long quant = 0;
		try
		{
			quant = Long.parseLong(quantity);
		}
		catch (Exception e)
		{
			GuiUpdateResponse guiResponse = new GuiUpdateResponse(OperationStatus.fail, "Invalid Quantity");
			return guiResponse.toString();
		}

		GetLocaleSettingsResponse locale = globalVasService.getLocaleSettings(user);

		ServiceQuota sc = new ServiceQuota();
		sc.setService(service);
		sc.setDestination(destination);
		sc.setDaysOfWeek(dow);
		sc.setTimeOfDay(tod);
		sc.setQuantity(quant);

		AddQuotaResponse resp = actService.addQuota(user, msisdn, benMsisdn, serviceId, variantId, sc, user.getLanguageId(), testMode); // Actual Call
		String jsonResponse = "";
		CreditSharingTranslations ct = new CreditSharingTranslations(user.getLanguageId());
		if (resp == null)
		{
			GuiUpdateResponse guiResponse = new GuiUpdateResponse(OperationStatus.fail, ct.translate(MessageContext.webServicesUnavailable));
			return guiResponse.toString();
		}
		else if (testMode && resp.getReturnCode() == ReturnCodes.SUCCESSFUL_TEST)
		{
			AddServiceQuotaFeedBack fb = new AddServiceQuotaFeedBack();
			fb.setMsisdn(msisdn);
			fb.setServiceId(serviceId);
			fb.setVariantId(variantId);
			fb.setBenMsisdn(benMsisdn);

			fb.setService(service);
			fb.setDestination(destination);
			fb.setDow(dow);
			fb.setTod(tod);
			fb.setQuantity(quantity);
			fb.setUnits(resp.getQuota().getUnits());
			fb.setCost(CCUtils.convertCurrencyToBaseUnits(locale, resp.getChargeLevied()));
			fb.setMessage(ct.translate(MessageContext.quotaAddSuccessTest, msisdn));

			jsonResponse = fb.toString();
		}
		else
		{
			// For Real
			GuiUpdateResponse guiResponse = new GuiUpdateResponse();

			if (resp.getReturnCode() == ReturnCodes.SUCCESS)
			{
				guiResponse.setStatus(OperationStatus.pass);
				guiResponse.setMessage(ct.translate(MessageContext.quotaAddSuccess, msisdn, benMsisdn, String.valueOf(resp.getChargeLevied())));
			}
			else
			{
				guiResponse.setStatus(OperationStatus.fail);
				String translation = actService.getReturnCodeTranslation(user, resp.getReturnCode(), user.getLanguageId());
				guiResponse.setMessage(translation);
			}
			jsonResponse = guiResponse.toString();
		}

		return jsonResponse;
	}

	// removeQuota
	private String removeTransferCall(HttpServletRequest request, User user, boolean testMode)
	{
		String msisdn = request.getParameter("msisdn");
		String serviceId = request.getParameter("sid");
		String variantId = request.getParameter("vid");
		String benMsisdn = request.getParameter("ben");
		String tid = request.getParameter("tid");

		RemoveCreditTransfersResponse removeResponse = actService.removeCreditTransfers(user, msisdn, benMsisdn, serviceId, null, tid, true, testMode);

		GuiUpdateResponse guiResponse = new GuiUpdateResponse();
		CreditSharingTranslations ct = new CreditSharingTranslations(user.getLanguageId());
		if (removeResponse.getReturnCode() == ReturnCodes.SUCCESS)
		{
			guiResponse.setStatus(OperationStatus.pass);
			guiResponse.setMessage(String.format("Credit Transfer removed for msisdn %s for member %s at cost of %s USD ", msisdn, benMsisdn,
					CCUtils.currencyConversion(removeResponse.getChargeLevied())));
		}
		else if (removeResponse.getReturnCode() == ReturnCodes.SUCCESSFUL_TEST)
		{
			guiResponse.setStatus(OperationStatus.pass);
			guiResponse.setMessage(String.format("A Credit Transfer removal for msisdn %s for member %s will cost of %s USD", msisdn, benMsisdn,
					CCUtils.currencyConversion(removeResponse.getChargeLevied())));
		}
		else
		{
			guiResponse.setStatus(OperationStatus.fail);
			guiResponse.setMessage(actService.getReturnCodeTranslation(user, removeResponse.getReturnCode(), user.getLanguageId()));
		}

		return guiResponse.toString();
	}

	public void removeTransferHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = removeTransferCall(request, user, false);
		sendResponse(response, json);
	}

	public void removeTransferTestHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = removeTransferCall(request, user, true);
		sendResponse(response, json);
	}

	public void retrieveQuotaQuantityInfoHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String msisdn = request.getParameter("msisdn");
		String serviceId = request.getParameter("sid");
		String variantId = request.getParameter("vid");
		String benMsisdn = request.getParameter("ben");
		String service = request.getParameter("service");

		ServiceQuota sq = actService.retrieveServiceQuota(user, msisdn, benMsisdn, serviceId, variantId, service, user.getLanguageId());
		String json = null;
		if (sq != null)
		{
			JsonObject job = new JsonObject();
			job.add("qty", new JsonPrimitive(String.valueOf(sq.getQuantity())));
			job.add("units", new JsonPrimitive(sq.getUnits()));
			json = job.toString();
		}
		else
		{
			CreditSharingTranslations ct = new CreditSharingTranslations(user.getLanguageId());
			GuiUpdateResponse resp = new GuiUpdateResponse(OperationStatus.fail, actService.getReturnCodeTranslation(user, ReturnCodes.INVALID_QUOTA, user.getLanguageId()));
			json = resp.toString();
		}
		sendResponse(response, json);
	}

	private String updateQuantityCall(HttpServletRequest request, User user, boolean testMode)
	{
		String msisdn = request.getParameter("msisdn");
		String serviceId = request.getParameter("sid");
		String variantId = request.getParameter("vid");
		String benMsisdn = request.getParameter("ben");
		String qid = request.getParameter("qid");
		String oldqty = request.getParameter("oldqty");
		String qty = request.getParameter("quantity");

		long newQuantity = 0;
		GuiUpdateResponse guiResponse = new GuiUpdateResponse();
		CreditSharingTranslations ct = new CreditSharingTranslations(user.getLanguageId());
		try
		{
			newQuantity = Long.valueOf(qty);
			ServiceQuota quotaToUpdate = actService.retrieveServiceQuota(user, msisdn, benMsisdn, serviceId, variantId, qid, user.getLanguageId());

			ChangeQuotaResponse qrResp = actService.updateQuaotaAmount(user, msisdn, benMsisdn, serviceId, variantId, quotaToUpdate, user.getLanguageId(), newQuantity, testMode);
			if (qrResp.getReturnCode() == ReturnCodes.SUCCESS)
			{
				guiResponse.setStatus(OperationStatus.pass);
				guiResponse.setMessage(ct.translate(MessageContext.quotaQuantityUpdateSuccess, oldqty, qty, String.format("%s/%s", quotaToUpdate.getService(), quotaToUpdate.getDestination()),
						benMsisdn, String.valueOf(qrResp.getChargeLevied())));
			}
			else if (qrResp.getReturnCode() == ReturnCodes.SUCCESSFUL_TEST)
			{
				guiResponse.setStatus(OperationStatus.pass);
				if (qrResp.getChargeLevied() > 0)
				{
					guiResponse.setMessage(ct.translate(MessageContext.confirmUpdateQuotaMessageWithCost, oldqty, qty,
							String.format("%s/%s", quotaToUpdate.getService(), quotaToUpdate.getDestination()), benMsisdn, String.valueOf(qrResp.getChargeLevied())));
				}
				else
				{
					guiResponse.setMessage(ct.translate(MessageContext.confirmUpdateQuotaMessageNoCost, oldqty, qty,
							String.format("%s/%s", quotaToUpdate.getService(), quotaToUpdate.getDestination()), benMsisdn));
				}
			}
			else
			{
				guiResponse.setStatus(OperationStatus.fail);
				guiResponse.setMessage(actService.getReturnCodeTranslation(user, qrResp.getReturnCode(), user.getLanguageId()));
			}
		}
		catch (Exception e)
		{
			guiResponse = new GuiUpdateResponse(OperationStatus.fail, actService.getReturnCodeTranslation(user, ReturnCodes.INVALID_ARGUMENTS, user.getLanguageId()));
		}

		return guiResponse.toString();
	}

	public void updateQuantityTestHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = updateQuantityCall(request, user, true);
		sendResponse(response, json);
	}

	public void updateQuantityHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = updateQuantityCall(request, user, false);
		sendResponse(response, json);
	}

	public void performBalanceEnquiryHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String msisdn = request.getParameter("msisdn");
		String serviceId = null;
		String variantId = null;

		// Get services
		GetServicesResponse servicesResp = globalVasService.getServices(user, msisdn, user.getLanguageId());
		if (servicesResp.getReturnCode() == ReturnCodes.SUCCESS)
		{
			for (VasServiceInfo vi : servicesResp.getServiceInfo())
			{
				if (vi.getServiceID().equalsIgnoreCase(ACT_SERVICE_ID) && (vi.getState() == SubscriptionState.ACTIVE))
				{
					serviceId = vi.getServiceID();
					variantId = vi.getVariantID();
					break;
				}
			}
		}

		boolean balanceAvailable = false;
		String json = null;
		CreditSharingTranslations ct = new CreditSharingTranslations(user.getLanguageId());
		if (serviceId != null && variantId != null)
		{
			String dateFormat = globalVasService.getDateFormat(user);

			GetBalancesResponse balResponse = actService.retrieveBalances(user, msisdn, serviceId, variantId, user.getLanguageId(), false);
			if (balResponse.getReturnCode() == ReturnCodes.SUCCESS)
			{
				balanceAvailable = true;
				JsonBalances jbal = new JsonBalances();
				jbal.setMsg(ct.translate(MessageContext.balanceRetrievedMessage, String.valueOf(balResponse.getChargeLevied())));
				jbal.setDateFormat(dateFormat);

				if (balResponse.getBalances().size() == 0)
				{
					jbal.setTitle(ct.translate(MessageContext.noBalanceAvailable));
				}
				else
				{
					jbal.setTitle(ct.translate(MessageContext.balanceAvailableHeading));
					jbal.setServiceBalances(balResponse.getBalances());
				}
				json = jbal.toString();
			}

		}

		if (!balanceAvailable)
		{
			json = (new GuiUpdateResponse(OperationStatus.fail, ct.translate(MessageContext.noBalanceAvailable))).toString();
		}

		sendResponse(response, json);
	}

	public void addTransferModataDataHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String msisdn = request.getParameter("msisdn");
		String serviceId = request.getParameter("sid");
		String variantId = request.getParameter("vid");
		String benMsisdn = request.getParameter("ben");

		GetCreditTransfersResponse creditTransfersResponse = actService.getCreditTransfers(user, msisdn, benMsisdn, serviceId, variantId, null, false, true);
		List<CreditTransfer> modes = new ArrayList<>();

		if (creditTransfersResponse.getReturnCode() == ReturnCodes.SUCCESS)
			modes = creditTransfersResponse.getTransfers();

		String json = (new Gson()).toJson(modes);
		sendResponse(response, json);
	}

	private String addCreditTransferCall(HttpServletRequest request, User user, boolean testMode)
	{
		GuiUpdateResponse msg = null;

		// Page parameters
		String msisdn = request.getParameter("msisdn");
		String serviceId = request.getParameter("sid");
		String variantId = request.getParameter("vid");
		String memberMsisdn = request.getParameter("ben");

		String dblAmount = request.getParameter("amount");
		String transferMode = request.getParameter("mode");
		String transLimit = request.getParameter("limit");

		// Get data for transactions
		GetCreditTransfersResponse creditTransfersResponse = actService.getCreditTransfers(user, msisdn, memberMsisdn, serviceId, variantId, null, false, true);
		CreditTransfer ctInfo = null;

		if (creditTransfersResponse.getReturnCode() == ReturnCodes.SUCCESS)
		{

			for (CreditTransfer ct : creditTransfersResponse.getTransfers())
			{
				if (ct.getTransferModeID().equals(transferMode))
				{
					ctInfo = ct;
					break;
				}
			}
		}

		if (ctInfo != null)
		{

			BigDecimal amount = new BigDecimal(dblAmount);
			amount = amount.multiply(new BigDecimal(640000));
			amount = amount.divide(new BigDecimal(6400));
			long lamount = CCUtils.scaleNumber(dblAmount, ctInfo.getScaleNumerator(), ctInfo.getScaleDenominator());
			Long ltransLimit = null;

			if (transLimit != null)
				ltransLimit = CCUtils.scaleNumber(transLimit, ctInfo.getScaleNumerator(), ctInfo.getScaleDenominator());

			AddCreditTransferResponse response = actService.addCreditTransfer(user, msisdn, memberMsisdn, serviceId, transferMode, lamount, ltransLimit, testMode);

			if (response.getReturnCode() == ReturnCodes.SUCCESS || response.getReturnCode() == ReturnCodes.SUCCESSFUL_TEST)
			{
				if (testMode)
					msg = new GuiUpdateResponse(OperationStatus.pass, String.format("Credit Transfer to %s will cost %d USD", memberMsisdn, response.getChargeLevied()));
				else
					msg = new GuiUpdateResponse(OperationStatus.pass, response.getMessage());
			}
			else
			{
				String translation = actService.getReturnCodeTranslation(user, response.getReturnCode(), 1);
				msg = new GuiUpdateResponse(OperationStatus.fail, translation);
			}
		}
		else
		{
			msg = new GuiUpdateResponse(OperationStatus.fail, String.format("Operation can not be performed, transfer mode %s not found", transferMode));
		}

		return msg.toString();
	}

	public void addCreditTransferTestHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = addCreditTransferCall(request, user, true);
		sendResponse(response, json);
	}

	public void addCreditTransferHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = addCreditTransferCall(request, user, false);
		sendResponse(response, json);
	}

	// --------------------------------------------------------------------------------------------------------
	// Helper methods
	// --------------------------------------------------------------------------------------------------------

	private JsonArray createJsonDataArray(String[] data)
	{
		JsonArray jarr = new JsonArray();
		for (String s : data)
		{
			jarr.add(new JsonPrimitive(s));
		}
		return jarr;
	}

	private GetMembersResponse getBeneficiaries(User user, HttpServletRequest request, WebContext ctx)
	{
		String msisdn = request.getParameter("msisdn");
		String serviceID = request.getParameter("sid");
		String variantID = request.getParameter("vid");
		GetMembersResponse memberResponse = actService.getMembers(user, msisdn, serviceID, variantID, user.getLanguageId());
		ctx.setVariable("msisdn", msisdn);
		ctx.setVariable("sid", serviceID);
		ctx.setVariable("vid", variantID);
		return memberResponse;
	}

	public void template(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException, NoSuchAlgorithmException
	{
	}
}
