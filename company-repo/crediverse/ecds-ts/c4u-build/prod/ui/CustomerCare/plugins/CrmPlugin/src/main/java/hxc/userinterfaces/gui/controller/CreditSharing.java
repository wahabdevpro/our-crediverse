package hxc.userinterfaces.gui.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.datatype.XMLGregorianCalendar;

import org.thymeleaf.context.WebContext;

import hxc.userinterfaces.cc.data.AddServiceQuotaFeedBack;
import hxc.userinterfaces.cc.data.ConsumerOwner;
import hxc.userinterfaces.cc.data.JsonBalances;
import hxc.userinterfaces.cc.data.MemberQuota;
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

import com.concurrent.hxc.AddMemberResponse;
import com.concurrent.hxc.AddQuotaResponse;
import com.concurrent.hxc.ChangeQuotaResponse;
import com.concurrent.hxc.GetBalancesResponse;
import com.concurrent.hxc.GetLocaleSettingsResponse;
import com.concurrent.hxc.GetMembersResponse;
import com.concurrent.hxc.GetOwnersResponse;
import com.concurrent.hxc.GetQuotasResponse;
import com.concurrent.hxc.GetServicesResponse;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.RemoveMemberResponse;
import com.concurrent.hxc.RemoveQuotaResponse;
import com.concurrent.hxc.ReturnCodes;
import com.concurrent.hxc.ServiceBalance;
import com.concurrent.hxc.ServiceQuota;
import com.concurrent.hxc.SubscribeResponse;
import com.concurrent.hxc.SubscriptionState;
import com.concurrent.hxc.UnsubscribeResponse;
import com.concurrent.hxc.UpdateContactInfoResponse;
import com.concurrent.hxc.VasServiceInfo;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@WebServlet(urlPatterns = { "/gsa" }, name = "CreditSharingServlet", asyncSupported = true, initParams = { @WebInitParam(name = "fromAnnotation", value = "xyz") })
public class CreditSharing extends BaseServlet
{

	private static final long serialVersionUID = -4687345067666092017L;

	// private final String CREDIT_SHARING_SERVICE_ID = "CrShr";
	private final String CREDIT_SHARING_SERVICE_ID = "GSA";
	private final String SERVICE_NAME = "Group Shared Accounts";

	@Resource(name = "creditSharingService")
	private ISubscriptionService creditSharingService;

	@Resource(name = "globalVasService")
	private IGlobalVasService globalVasService;

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

		// Is this user a Consumer of a subscriber??
		GetOwnersResponse ownersResponse = creditSharingService.getOwners(user, msisdn, CREDIT_SHARING_SERVICE_ID);
		if (ownersResponse != null && ownersResponse.getReturnCode() == ReturnCodes.SUCCESS)
		{
			if (ownersResponse.getOwners() == null || ownersResponse.getOwners().size() == 0)
			{
				// MSISDN is Provider
				List<VasServiceInfo> services = globalVasService.getServiceVariantsList(user, msisdn, CREDIT_SHARING_SERVICE_ID, user.getLanguageId());
				List<VasServiceInfo> subscribedServices = globalVasService.extractActiveServices(services);

				boolean subscribedToService = false;
				for (VasServiceInfo vs : services)
				{
					if (vs.getState().equals(SubscriptionState.ACTIVE) && (vs.getServiceID().equals(CREDIT_SHARING_SERVICE_ID)))
						subscribedToService = true;
				}

				ctx.setVariable("subscribed", subscribedToService);
				ctx.setVariable("services", services);
				ctx.setVariable("subscribedServices", subscribedServices);
				ctx.setVariable("serviceName", "Group Shared Accounts");

				sendTemplateResponse(response, ctx, "servicelist");
			}
			else
			{
				List<ConsumerOwner> owners = new ArrayList<>();
				String variantId = null;
				for (Number msisdnNumber : ownersResponse.getOwners())
				{
					ConsumerOwner co = new ConsumerOwner();

					// Need to find the service variant for the owner
					GetServicesResponse serviceResponse = globalVasService.getServices(user, msisdnNumber.getAddressDigits(), user.getLanguageId());

					if (serviceResponse.getReturnCode() == ReturnCodes.SUCCESS)
					{
						List<VasServiceInfo> subscribedServices = globalVasService.extractActiveServices(serviceResponse.getServiceInfo());
						for (VasServiceInfo vs : subscribedServices)
						{
							if (vs.getServiceID().equals(CREDIT_SHARING_SERVICE_ID))
							{
								variantId = vs.getVariantID();
								break;
							}
						}
					}

					List<MemberQuota> mql = quotaInfoToMemberInfo(user, msisdnNumber, msisdn, CREDIT_SHARING_SERVICE_ID, variantId);
					co.getQuotas().addAll(mql);
					co.setMsisdn(msisdnNumber.getAddressDigits());
					co.setServiceId(CREDIT_SHARING_SERVICE_ID);
					co.setVariantId(variantId);
					owners.add(co);
				}

				ctx.setVariable("msisdn", msisdn);
				ctx.setVariable("sid", CREDIT_SHARING_SERVICE_ID);
				ctx.setVariable("vid", variantId);

				ctx.setVariable("owners", owners);
				sendTemplateResponse(response, ctx, "owners");
			}
		}
		else
		{
			String errorMessage = null;
			CreditSharingTranslations ct = new CreditSharingTranslations(user.getLanguageId());

			if (ownersResponse == null)
			{
				errorMessage = ct.translate(MessageContext.webServicesUnavailable);
			}
			else
			{
				errorMessage = creditSharingService.getReturnCodeTranslation(user, ownersResponse.getReturnCode(), user.getLanguageId());
			}

			ctx.setVariable("error", errorMessage);
			sendTemplateResponse(response, ctx, "serviceerror");
		}
	}

	private String subscribeCall(HttpServletRequest request, User user, boolean testMode)
	{
		String msisdn = request.getParameter("msisdn");
		String serviceID = request.getParameter("sid");
		String variantID = request.getParameter("vid");

		VasServiceInfo vasInfo = creditSharingService.getServiceInfo(user, msisdn, serviceID, variantID, user.getLanguageId());
		String variantName = (vasInfo == null) ? variantID : vasInfo.getVariantName();

		SubscribeResponse subResponse = creditSharingService.subscribe(user, msisdn, serviceID, variantID, user.getLanguageId(), testMode);
		GuiUpdateResponse guiResponse = new GuiUpdateResponse();
		CreditSharingTranslations ct = new CreditSharingTranslations(user.getLanguageId());
		
		GetLocaleSettingsResponse locale = globalVasService.getLocaleSettings(user);

		if (subResponse.getReturnCode() == ReturnCodes.SUCCESS)
		{
			long charge = subResponse.getChargeLevied();
			guiResponse.setStatus(OperationStatus.pass);
			guiResponse.setMessage(ct.translate(MessageContext.subscribeSuccess, variantName, CCUtils.convertCurrencyToBaseUnits(locale, subResponse.getChargeLevied())));
		}
		else if (subResponse.getReturnCode() == ReturnCodes.SUCCESSFUL_TEST)
		{
			guiResponse.setStatus(OperationStatus.pass);
			if (subResponse.getChargeLevied() > 0)
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmSubscribeMessageWithCost, msisdn, variantName, CCUtils.convertCurrencyToBaseUnits(locale, subResponse.getChargeLevied())));
			}
			else
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmSubscribeMessageNoCost, msisdn, variantName));
			}
		}
		else
		{
			guiResponse.setStatus(OperationStatus.fail);
			guiResponse.setMessage(creditSharingService.getReturnCodeTranslation(user, subResponse.getReturnCode(), user.getLanguageId()));
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
		GetBalancesResponse balResponse = creditSharingService.retrieveBalances(user, msisdn, serviceID, variantID, user.getLanguageId(), true);

		boolean balanceRequestFree = (balResponse.getReturnCode() == ReturnCodes.SUCCESSFUL_TEST && balResponse.getChargeLevied() == 0L);

		ctx.setVariable("balancerequestfree", balanceRequestFree);

		sendTemplateResponse(response, ctx, "crshr/creditshareservice");
	}

	private String addBeneficiaryCall(HttpServletRequest request, User user, boolean testMode)
	{
		String msisdn = request.getParameter("msisdn");
		String ben = request.getParameter("ben");
		String contactName = request.getParameter("benName");
		String serviceId = request.getParameter("sid");
		String variantId = request.getParameter("vid");

		AddMemberResponse addMemberResponse = creditSharingService.addBeneficiary(user, msisdn, ben, serviceId, variantId, user.getLanguageId(), testMode);
		CreditSharingTranslations ct = new CreditSharingTranslations(user.getLanguageId());
		GetLocaleSettingsResponse locale = globalVasService.getLocaleSettings(user);
		
		GuiUpdateResponse guiResponse = new GuiUpdateResponse();
		if (addMemberResponse.getReturnCode() == ReturnCodes.SUCCESS)
		{
			// all's well lets update the persons name
			UpdateContactInfoResponse contactDetailsResponse = creditSharingService.updateContactInfo(user, ben, contactName, serviceId, variantId, user.getLanguageId(), testMode);

			guiResponse.setStatus(OperationStatus.pass);
			guiResponse.setMessage(ct.translate(MessageContext.consumerAddSuccess, ben, CCUtils.convertCurrencyToBaseUnits(locale, addMemberResponse.getChargeLevied())));
		}
		else if (addMemberResponse.getReturnCode() == ReturnCodes.SUCCESSFUL_TEST)
		{
			guiResponse.setStatus(OperationStatus.pass);
			if (addMemberResponse.getChargeLevied() > 0)
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmAddConsumerMessageWithCost, ben, msisdn, CCUtils.convertCurrencyToBaseUnits(locale, addMemberResponse.getChargeLevied())));
			}
			else
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmAddConsumerMessageNoCost, ben, msisdn));
			}
		}
		else
		{
			guiResponse.setStatus(OperationStatus.fail);
			guiResponse.setMessage(creditSharingService.getReturnCodeTranslation(user, addMemberResponse.getReturnCode(), user.getLanguageId()));
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
		sendTemplateResponse(response, ctx, "crshr/memberslist");
	}

	private String unsubscribeCall(HttpServletRequest request, User user, boolean testMode)
	{
		String msisdn = request.getParameter("msisdn");
		String serviceId = request.getParameter("sid");
		String variantId = request.getParameter("vid");

		VasServiceInfo vasInfo = creditSharingService.getServiceInfo(user, msisdn, serviceId, variantId, user.getLanguageId());
		String variantName = (vasInfo == null) ? variantId : vasInfo.getVariantName();

		UnsubscribeResponse unsubResponse = creditSharingService.unsubscribe(user, msisdn, serviceId, variantId, user.getLanguageId(), testMode);
		GetLocaleSettingsResponse locale = globalVasService.getLocaleSettings(user);
		
		GuiUpdateResponse guiResponse = new GuiUpdateResponse();
		CreditSharingTranslations ct = new CreditSharingTranslations(user.getLanguageId());
		if (unsubResponse.getReturnCode() == ReturnCodes.SUCCESS)
		{
			guiResponse.setStatus(OperationStatus.pass);
			guiResponse.setMessage(ct.translate(MessageContext.unsubscribeSuccess, msisdn, variantName, CCUtils.convertCurrencyToBaseUnits(locale, unsubResponse.getChargeLevied())));
		}
		else if (unsubResponse.getReturnCode() == ReturnCodes.SUCCESSFUL_TEST)
		{
			guiResponse.setStatus(OperationStatus.pass);
			if (unsubResponse.getChargeLevied() > 0)
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmUnsubscribeMessageWithCost, msisdn, SERVICE_NAME, variantName, CCUtils.convertCurrencyToBaseUnits(locale, unsubResponse.getChargeLevied())));
			}
			else
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmUnsubscribeMessageNoCost, msisdn, SERVICE_NAME, variantName));
			}
		}
		else
		{
			guiResponse.setStatus(OperationStatus.fail);
			guiResponse.setMessage(creditSharingService.getReturnCodeTranslation(user, unsubResponse.getReturnCode(), user.getLanguageId()));
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

		RemoveMemberResponse resp = creditSharingService.removeMember(user, msisdn, benMsisdn, serviceId, variantId, user.getLanguageId(), testMode, true);
		GetLocaleSettingsResponse locale = globalVasService.getLocaleSettings(user);

		GuiUpdateResponse guiResponse = new GuiUpdateResponse();
		CreditSharingTranslations ct = new CreditSharingTranslations(user.getLanguageId());

		if (resp.getReturnCode() == ReturnCodes.SUCCESS)
		{
			guiResponse.setStatus(OperationStatus.pass);
			guiResponse.setMessage(ct.translate(MessageContext.consumerRemoveSuccess, benMsisdn, msisdn, CCUtils.convertCurrencyToBaseUnits(locale, resp.getChargeLevied())));
		}
		else if (resp.getReturnCode() == ReturnCodes.SUCCESSFUL_TEST)
		{
			guiResponse.setStatus(OperationStatus.pass);
			if (resp.getChargeLevied() > 0)
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmRemoveConsumerMessageWithCost, benMsisdn, msisdn, CCUtils.convertCurrencyToBaseUnits(locale, resp.getChargeLevied())));
			}
			else
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmRemoveConsumerMessageNoCost, benMsisdn, msisdn));
			}
		}
		else
		{
			guiResponse.setStatus(OperationStatus.fail);
			guiResponse.setMessage(creditSharingService.getReturnCodeTranslation(user, resp.getReturnCode(), user.getLanguageId()));
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

		RemoveMemberResponse resp = creditSharingService.removeMember(user, msisdn, benMsisdn, serviceId, variantId, user.getLanguageId(), testMode, false);
		GetLocaleSettingsResponse locale = globalVasService.getLocaleSettings(user);
		
		GuiUpdateResponse guiResponse = new GuiUpdateResponse();
		CreditSharingTranslations ct = new CreditSharingTranslations(user.getLanguageId());

		if (resp.getReturnCode() == ReturnCodes.SUCCESS)
		{
			guiResponse.setStatus(OperationStatus.pass);
			guiResponse.setMessage(ct.translate(MessageContext.consumerRemoveSuccess, benMsisdn, msisdn, CCUtils.convertCurrencyToBaseUnits(locale, resp.getChargeLevied())));
		}
		else if (resp.getReturnCode() == ReturnCodes.SUCCESSFUL_TEST)
		{
			guiResponse.setStatus(OperationStatus.pass);
			if (resp.getChargeLevied() > 0)
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmRemoveOwnerWithCost, msisdn, benMsisdn, CCUtils.convertCurrencyToBaseUnits(locale, resp.getChargeLevied())));
			}
			else
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmRemoveOwnerNoCost, msisdn, benMsisdn));
			}
		}
		else
		{
			guiResponse.setStatus(OperationStatus.fail);
			guiResponse.setMessage(creditSharingService.getReturnCodeTranslation(user, resp.getReturnCode(), user.getLanguageId()));
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
	public List<MemberQuota> quotaInfoToMemberInfo(User user, Object msisdn, String benMsisdn, String serviceId, String variantId)
	{
		// Retrieve Quota info
		GetQuotasResponse quotas = null;
		if (msisdn instanceof String)
		{
			quotas = creditSharingService.getQuotas(user, (String) msisdn, benMsisdn, serviceId, variantId, false, user.getLanguageId());
		}
		else
		{
			quotas = creditSharingService.getQuotas(user, (Number) msisdn, benMsisdn, serviceId, variantId, false, user.getLanguageId());
		}

		List<MemberQuota> ma = new ArrayList<>();

		GetBalancesResponse bal = creditSharingService.retrieveBalances(user, benMsisdn, serviceId, variantId, user.getLanguageId(), false);

		if (bal.getReturnCode() == ReturnCodes.SUCCESS)
		{
			for (int i = 0; i < quotas.getServiceQuotas().size(); i++)
			{
				MemberQuota mq = new MemberQuota();
				if (bal.getBalances() != null)
				{
					for (ServiceBalance sb : bal.getBalances())
					{
						if (quotas.getServiceQuotas().get(i).getName().equalsIgnoreCase(sb.getName()))
						{
							mq.setBalance(String.valueOf(bal.getBalances().get(i).getValue()));
							XMLGregorianCalendar date = bal.getBalances().get(i).getExpiryDate();
							if (date != null)
							{
								GregorianCalendar gc = date.toGregorianCalendar();
								String strDate = globalVasService.formatDate(user, gc.getTime());
								mq.setExpDate(strDate);
							}
							break;
						}
					}
				}

				mq.setService(quotas.getServiceQuotas().get(i).getService());
				mq.setDestination(quotas.getServiceQuotas().get(i).getDestination());
				mq.setDaysOfWeek(quotas.getServiceQuotas().get(i).getDaysOfWeek());
				mq.setTimeOfDay(quotas.getServiceQuotas().get(i).getTimeOfDay());
				mq.setQuantity(quotas.getServiceQuotas().get(i).getQuantity());
				mq.setUnits(quotas.getServiceQuotas().get(i).getUnits());
				mq.setQuotaID(quotas.getServiceQuotas().get(i).getQuotaID());
				ma.add(mq);
			}
		}

		return ma;
	}

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

		// Retrieve Quota info
		List<MemberQuota> ma = quotaInfoToMemberInfo(user, msisdn, benMsisdn, serviceId, variantId);

		ctx.setVariable("quotas", ma);
		ctx.setVariable("noquotas", (ma == null || ma.size() == 0));

		sendTemplateResponse(response, ctx, "crshr/memberquotas");
	}

	public void retrieveQuaotaInfoHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String msisdn = request.getParameter("msisdn");
		String serviceId = request.getParameter("sid");
		String variantId = request.getParameter("vid");
		String benMsisdn = request.getParameter("ben");
		
		GetQuotasResponse quotas = creditSharingService.getQuotas(user, msisdn, benMsisdn, serviceId, variantId, true, user.getLanguageId());
		QuotaInfo qi = new QuotaInfo();
		qi.importQuotaInfo(quotas.getServiceQuotas());

		// Add Locale information for Currency
		GetLocaleSettingsResponse locale = globalVasService.getLocaleSettings(user);
		qi.setCurrencyCode(locale.getCurrencyCode());
		
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
		String quantity = request.getParameter("quantity"); // Note that this can be currency

		GetLocaleSettingsResponse locale = globalVasService.getLocaleSettings(user);
		
		long quant = 0;
		try
		{
			if ("airtime".equalsIgnoreCase(service)) 
				quant = CCUtils.convertFromBaseToCents(locale, quantity);
			else
				quant = Long.parseLong(quantity);
		}
		catch (Exception e)
		{
			GuiUpdateResponse guiResponse = new GuiUpdateResponse(OperationStatus.fail, "Invalid Quantity");
			return guiResponse.toString();
		}

		ServiceQuota sc = new ServiceQuota();
		sc.setService(service);
		sc.setDestination(destination);
		sc.setDaysOfWeek(dow);
		sc.setTimeOfDay(tod);
		sc.setQuantity(quant);

		AddQuotaResponse resp = creditSharingService.addQuota(user, msisdn, benMsisdn, serviceId, variantId, sc, user.getLanguageId(), testMode); // Actual Call
		
		
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
				guiResponse.setMessage(ct.translate(MessageContext.quotaAddSuccess, msisdn, benMsisdn, CCUtils.convertCurrencyToBaseUnits(locale, resp.getChargeLevied())));
			}
			else
			{
				guiResponse.setStatus(OperationStatus.fail);
				guiResponse.setMessage(creditSharingService.getReturnCodeTranslation(user, resp.getReturnCode(), user.getLanguageId()));
			}
			jsonResponse = guiResponse.toString();
		}

		return jsonResponse;
	}

	// removeQuota
	private String removeQuotaCall(HttpServletRequest request, User user, boolean testMode)
	{
		String msisdn = request.getParameter("msisdn");
		String serviceId = request.getParameter("sid");
		String variantId = request.getParameter("vid");
		String benMsisdn = request.getParameter("ben");
		String qid = request.getParameter("qid");

		ServiceQuota sqToRemove = creditSharingService.retrieveServiceQuota(user, msisdn, benMsisdn, serviceId, variantId, qid, user.getLanguageId());
		RemoveQuotaResponse rmresp = creditSharingService.removeQuota(user, msisdn, benMsisdn, serviceId, variantId, sqToRemove, user.getLanguageId(), testMode);
		GetLocaleSettingsResponse locale = globalVasService.getLocaleSettings(user);

		GuiUpdateResponse guiResponse = new GuiUpdateResponse();
		CreditSharingTranslations ct = new CreditSharingTranslations(user.getLanguageId());
		if (rmresp.getReturnCode() == ReturnCodes.SUCCESS)
		{
			guiResponse.setStatus(OperationStatus.pass);
			guiResponse.setMessage(ct.translate(MessageContext.quotaRemoveSuccess, String.format("%s/%s", sqToRemove.getService(), sqToRemove.getDestination()), benMsisdn, msisdn,
					String.valueOf(rmresp.getChargeLevied())));
		}
		else if (rmresp.getReturnCode() == ReturnCodes.SUCCESSFUL_TEST)
		{
			guiResponse.setStatus(OperationStatus.pass);
			if (rmresp.getChargeLevied() > 0)
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmRemoveQuotaMessageWithCost, String.format("%s/%s", sqToRemove.getService(), sqToRemove.getDestination()), benMsisdn, msisdn,
						CCUtils.convertCurrencyToBaseUnits(locale, rmresp.getChargeLevied())));
			}
			else
			{
				guiResponse.setMessage(ct.translate(MessageContext.confirmRemoveQuotaMessageNoCost, String.format("%s/%s", sqToRemove.getService(), sqToRemove.getDestination()), benMsisdn, msisdn));
			}
		}
		else
		{
			guiResponse.setStatus(OperationStatus.fail);
			guiResponse.setMessage(creditSharingService.getReturnCodeTranslation(user, rmresp.getReturnCode(), user.getLanguageId()));
		}
		return guiResponse.toString();
	}

	public void removeQuotaHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = removeQuotaCall(request, user, false);
		sendResponse(response, json);
	}

	public void removeQuotaTestHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String json = removeQuotaCall(request, user, true);
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

		ServiceQuota sq = creditSharingService.retrieveServiceQuota(user, msisdn, benMsisdn, serviceId, variantId, service, user.getLanguageId());
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
			GuiUpdateResponse resp = new GuiUpdateResponse(OperationStatus.fail, creditSharingService.getReturnCodeTranslation(user, ReturnCodes.INVALID_QUOTA, user.getLanguageId()));
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
			ServiceQuota quotaToUpdate = creditSharingService.retrieveServiceQuota(user, msisdn, benMsisdn, serviceId, variantId, qid, user.getLanguageId());
			GetLocaleSettingsResponse locale = globalVasService.getLocaleSettings(user);
			
			ChangeQuotaResponse qrResp = creditSharingService.updateQuaotaAmount(user, msisdn, benMsisdn, serviceId, variantId, quotaToUpdate, user.getLanguageId(), newQuantity, testMode);
			if (qrResp.getReturnCode() == ReturnCodes.SUCCESS)
			{
				guiResponse.setStatus(OperationStatus.pass);
				guiResponse.setMessage(ct.translate(MessageContext.quotaQuantityUpdateSuccess, oldqty, qty, String.format("%s/%s", quotaToUpdate.getService(), quotaToUpdate.getDestination()),
						benMsisdn, CCUtils.convertCurrencyToBaseUnits(locale, qrResp.getChargeLevied())));
			}
			else if (qrResp.getReturnCode() == ReturnCodes.SUCCESSFUL_TEST)
			{
				guiResponse.setStatus(OperationStatus.pass);
				if (qrResp.getChargeLevied() > 0)
				{
					guiResponse.setMessage(ct.translate(MessageContext.confirmUpdateQuotaMessageWithCost, oldqty, qty,
							String.format("%s/%s", quotaToUpdate.getService(), quotaToUpdate.getDestination()), benMsisdn, CCUtils.convertCurrencyToBaseUnits(locale, qrResp.getChargeLevied())));
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
				guiResponse.setMessage(creditSharingService.getReturnCodeTranslation(user, qrResp.getReturnCode(), user.getLanguageId()));
			}
		}
		catch (Exception e)
		{
			guiResponse = new GuiUpdateResponse(OperationStatus.fail, creditSharingService.getReturnCodeTranslation(user, ReturnCodes.INVALID_ARGUMENTS, user.getLanguageId()));
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
		GetLocaleSettingsResponse locale = globalVasService.getLocaleSettings(user);
		
		if (servicesResp.getReturnCode() == ReturnCodes.SUCCESS)
		{
			for (VasServiceInfo vi : servicesResp.getServiceInfo())
			{
				if (vi.getServiceID().equalsIgnoreCase(CREDIT_SHARING_SERVICE_ID) && (vi.getState() == SubscriptionState.ACTIVE))
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

			GetBalancesResponse balResponse = creditSharingService.retrieveBalances(user, msisdn, serviceId, variantId, user.getLanguageId(), false);
			if (balResponse.getReturnCode() == ReturnCodes.SUCCESS)
			{
				balanceAvailable = true;
				JsonBalances jbal = new JsonBalances();
				jbal.setLocale(locale);
				jbal.setMsg(ct.translate(MessageContext.balanceRetrievedMessage, CCUtils.convertCurrencyToBaseUnits(locale, balResponse.getChargeLevied())));
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

	public void updateContactDetailsHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user, WebContext ctx) throws IOException, ServletException,
			NoSuchAlgorithmException
	{
		String serviceId = request.getParameter("sid");
		String variantId = request.getParameter("vid");
		String benMsisdn = request.getParameter("ben");
		String contactName = request.getParameter("contact");
		// String msisdn = request.getParameter("msisdn");

		GuiUpdateResponse guiResponse = new GuiUpdateResponse();
		UpdateContactInfoResponse contactDetailsResponse = creditSharingService.updateContactInfo(user, benMsisdn, contactName, serviceId, variantId, user.getLanguageId(), false);

		if (contactDetailsResponse.getReturnCode() == ReturnCodes.SUCCESS)
		{
			guiResponse.setStatus(OperationStatus.pass);
			guiResponse.setMessage(String.format("Contact details for MSISDN %s changed to [ %s ]", benMsisdn, contactName));
		}
		else
		{
			guiResponse.setStatus(OperationStatus.fail);
			guiResponse.setMessage(creditSharingService.getReturnCodeTranslation(user, contactDetailsResponse.getReturnCode(), user.getLanguageId()));
		}

		sendResponse(response, guiResponse.toString());
	}

	// --------------------------------------------------------------------------------------------------------
	// Helper methods
	// --------------------------------------------------------------------------------------------------------

	private GetMembersResponse getBeneficiaries(User user, HttpServletRequest request, WebContext ctx)
	{
		String msisdn = request.getParameter("msisdn");
		String serviceID = request.getParameter("sid");
		String variantID = request.getParameter("vid");
		GetMembersResponse memberResponse = creditSharingService.getMembers(user, msisdn, serviceID, variantID, user.getLanguageId());
		ctx.setVariable("msisdn", msisdn);
		ctx.setVariable("sid", serviceID);
		ctx.setVariable("vid", variantID);
		return memberResponse;
	}
	
	/**
	 * Convert Charage Levied to Customer Displayed Value
	 */

    
}
