package hxc.userinterfaces.gui.services;

import com.concurrent.hxc.AddMemberRequest;

import hxc.connectors.soap.IHxC;
import hxc.userinterfaces.cc.utils.CCUtils;
import hxc.userinterfaces.gui.data.User;

import com.concurrent.hxc.AddCreditTransferResponse;
import com.concurrent.hxc.AddMemberResponse;
import com.concurrent.hxc.AddQuotaRequest;
import com.concurrent.hxc.AddQuotaResponse;
import com.concurrent.hxc.ChangeCreditTransferResponse;
import com.concurrent.hxc.ChangeQuotaRequest;
import com.concurrent.hxc.ChangeQuotaResponse;
import com.concurrent.hxc.Channels;
import com.concurrent.hxc.ContactInfo;
import com.concurrent.hxc.GetBalancesRequest;
import com.concurrent.hxc.GetBalancesResponse;
import com.concurrent.hxc.GetCreditTransfersResponse;
import com.concurrent.hxc.GetMembersRequest;
import com.concurrent.hxc.GetMembersResponse;
import com.concurrent.hxc.GetOwnersRequest;
import com.concurrent.hxc.GetOwnersResponse;
import com.concurrent.hxc.GetQuotasRequest;
import com.concurrent.hxc.GetQuotasResponse;
import com.concurrent.hxc.GetReturnCodeTextRequest;
import com.concurrent.hxc.GetReturnCodeTextResponse;
import com.concurrent.hxc.GetServicesResponse;
import com.concurrent.hxc.RemoveCreditTransfersResponse;
import com.concurrent.hxc.RemoveMemberRequest;
import com.concurrent.hxc.RemoveMemberResponse;
import com.concurrent.hxc.RemoveQuotaRequest;
import com.concurrent.hxc.RemoveQuotaResponse;
import com.concurrent.hxc.RequestHeader;
import com.concurrent.hxc.RequestModes;
import com.concurrent.hxc.ResumeCreditTransferResponse;
import com.concurrent.hxc.ReturnCodes;
import com.concurrent.hxc.ServiceQuota;
import com.concurrent.hxc.SubscribeRequest;
import com.concurrent.hxc.SubscribeResponse;
import com.concurrent.hxc.SuspendCreditTransferResponse;
import com.concurrent.hxc.UnsubscribeRequest;
import com.concurrent.hxc.UnsubscribeResponse;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.UpdateContactInfoRequest;
import com.concurrent.hxc.UpdateContactInfoResponse;
import com.concurrent.hxc.VasServiceInfo;

import hxc.userinterfaces.gui.translations.CreditSharingTranslations;
import hxc.userinterfaces.gui.translations.CreditSharingTranslations.MessageContext;
import hxc.userinterfaces.gui.utils.HostInfo;

/**
 * This class caters for General service calls not particulat to a central service
 * 
 * @author johne
 * 
 */
public class CreditSharingService implements ISubscriptionService
{

	private SOAPServiceProvider serviceProvider = new SOAPServiceProvider();

	@Override
	public SubscribeResponse subscribe(User user, String msisdn, String serviceId, String variantId, int languageId, boolean testMode)
	{
		SubscribeResponse result = null;
		try
		{
			IHxC port = serviceProvider.getSOAPServicePort(user);

			SubscribeRequest subRequest = new SubscribeRequest();
			subRequest.setLanguageID(languageId);

			subRequest.setTransactionID(serviceProvider.createTransactionID());
			subRequest.setSessionID(user.getName());
			subRequest.setMode(testMode ? RequestModes.TEST_ONLY : RequestModes.NORMAL);
			subRequest.setCallerID("custcare");
			subRequest.setChannel(Channels.CRM);
			subRequest.setServiceID(serviceId);
			subRequest.setVariantID(variantId);
			subRequest.setSubscriberNumber(serviceProvider.generateMSISDNNumber(msisdn));

			result = port.subscribe(subRequest);
		}
		catch (Exception e)
		{
			// e.printStackTrace();
		}
		return result;
	}

	@Override
	public UnsubscribeResponse unsubscribe(User user, String msisdn, String serviceId, String variantId, int languageId, boolean testMode)
	{
		UnsubscribeResponse result = null;
		try
		{
			IHxC port = serviceProvider.getSOAPServicePort(user);

			UnsubscribeRequest unsub = new UnsubscribeRequest();
			unsub.setLanguageID(languageId);

			unsub.setCallerID(user.getUserId());
			unsub.setTransactionID(serviceProvider.createTransactionID());
			unsub.setSessionID(user.getUserId());
			unsub.setChannel(Channels.CRM);
			unsub.setMode(testMode ? RequestModes.TEST_ONLY : RequestModes.NORMAL);
			unsub.setServiceID(serviceId);
			unsub.setVariantID(variantId);
			unsub.setSubscriberNumber(serviceProvider.generateMSISDNNumber(msisdn));

			result = port.unsubscribe(unsub);
		}
		catch (Exception e)
		{
			result = new UnsubscribeResponse();
			result.setMessage("GUI Server Failure: %s" + e.getLocalizedMessage());
			result.setReturnCode(ReturnCodes.TECHNICAL_PROBLEM);
		}
		return result;
	}

	// TODO: Must finish this one
	@Override
	public AddMemberResponse addBeneficiary(User user, String msisdn, String benMsisdn, String serviceId, String variantId, int languageId, boolean testMode)
	{
		AddMemberResponse result = null;
		try
		{
			IHxC port = serviceProvider.getSOAPServicePort(user);

			AddMemberRequest member = new AddMemberRequest();

			member.setCallerID(user.getUserId());
			member.setTransactionID(serviceProvider.createTransactionID());
			member.setSessionID(user.getUserId());
			member.setChannel(Channels.CRM);

			member.setMode(testMode ? RequestModes.TEST_ONLY : RequestModes.NORMAL);
			member.setLanguageID(languageId);
			member.setSubscriberNumber(serviceProvider.generateMSISDNNumber(msisdn));
			member.setMemberNumber(serviceProvider.generateMSISDNNumber(benMsisdn));
			member.setServiceID(serviceId);
			member.setVariantID(variantId);

			result = port.addMember(member);
		}
		catch (Exception e)
		{
			// e.printStackTrace();
		}
		return result;
	}

	@Override
	public GetMembersResponse getMembers(User user, String msisdn, String serviceId, String variantId, int languageId)
	{
		GetMembersResponse result = null;
		try
		{
			IHxC port = serviceProvider.getSOAPServicePort(user);

			GetMembersRequest memRequest = new GetMembersRequest();

			memRequest.setCallerID(user.getUserId());
			memRequest.setTransactionID(serviceProvider.createTransactionID());
			memRequest.setSessionID(user.getUserId());
			memRequest.setChannel(Channels.CRM);

			memRequest.setLanguageID(languageId);
			memRequest.setSubscriberNumber(serviceProvider.generateMSISDNNumber(msisdn));
			memRequest.setServiceID(serviceId);
			memRequest.setVariantID(variantId);

			result = port.getMembers(memRequest);
		}
		catch (Exception e)
		{
			// e.printStackTrace();
		}

		return result;
	}

	// prefix + time sequence (YYYYmmDDhhMMssSSS), where there is a sequence for the second)

	// SessionID = Unique per MSISDN set of calls
	// TransactionID = Each transaction MUST have a unique transaction ID

	// TODO:Must finish this one
	@Override
	public RemoveMemberResponse removeMember(User user, String msisdn, String benMsisdn, String serviceId, String variantId, int languageId, boolean testMode, boolean isFromConsumer)
	{
		RemoveMemberResponse result = null;

		try
		{
			IHxC port = serviceProvider.getSOAPServicePort(user);

			RemoveMemberRequest req = new RemoveMemberRequest();

			if (isFromConsumer)
			{
				req.setCallerID(msisdn);
			}
			else
			{
				req.setCallerID(benMsisdn);
			}
			req.setTransactionID(serviceProvider.createTransactionID());
			req.setSessionID(user.getUserId());
			req.setChannel(Channels.CRM);
			req.setMode(testMode ? RequestModes.TEST_ONLY : RequestModes.NORMAL);

			req.setLanguageID(languageId);
			req.setSubscriberNumber(serviceProvider.generateMSISDNNumber(msisdn));
			req.setServiceID(serviceId);
			req.setVariantID(variantId);
			req.setMemberNumber(serviceProvider.generateMSISDNNumber(benMsisdn));

			result = port.removeMember(req);
		}
		catch (Exception e)
		{
		}

		return result;
	}

	@Override
	public UpdateContactInfoResponse updateContactInfo(User user, String msisdn, String contactName, String serviceId, String variantId, int languageId, boolean testMode)
	{
		UpdateContactInfoResponse result = null;

		try
		{
			IHxC port = serviceProvider.getSOAPServicePort(user);
			RequestHeader rh = new RequestHeader();

			UpdateContactInfoRequest req = new UpdateContactInfoRequest();
			req.setLanguageID(languageId);
			req.setCallerID(user.getUserId());
			req.setTransactionID(serviceProvider.createTransactionID());
			req.setSessionID(user.getUserId());
			req.setChannel(Channels.CRM);
			req.setMode(RequestModes.NORMAL);
			req.setVersion(serviceProvider.getSoapVersion());
			req.setHostName(HostInfo.getNameOrElseHxC());
			req.setSubscriberNumber(serviceProvider.generateMSISDNNumber(msisdn));
			req.setServiceID(serviceId);

			ContactInfo ci = new ContactInfo();
			ci.setName(contactName);
			req.setContactInfo(ci);

			result = port.updateContactInfo(req);
		}
		catch (Exception e)
		{
			// e.printStackTrace();
		}
		return result;
	}

	@Override
	public GetQuotasResponse getQuotas(User user, Number msisdn, String benMsisdn, String serviceId, String variantId, boolean forData, int languageId)
	{
		GetQuotasResponse result = null;
		try
		{
			IHxC port = serviceProvider.getSOAPServicePort(user);
			RequestHeader rh = new RequestHeader();

			GetQuotasRequest req = new GetQuotasRequest();
			req.setLanguageID(languageId);

			req.setCallerID(user.getUserId());
			req.setTransactionID(serviceProvider.createTransactionID());
			req.setSessionID(user.getUserId());
			req.setChannel(Channels.CRM);
			req.setMode(forData ? RequestModes.TEST_ONLY : RequestModes.NORMAL);
			req.setSubscriberNumber(msisdn);
			req.setServiceID(serviceId);
			req.setVariantID(variantId);
			req.setMemberNumber(serviceProvider.generateMSISDNNumber(benMsisdn));
			req.setActiveOnly(!forData);
			req.setVersion(serviceProvider.getSoapVersion());
			req.setHostName(HostInfo.getNameOrElseHxC());
			result = port.getQuotas(req);
		}
		catch (Exception e)
		{
			// e.printStackTrace();
		}
		return result;
	}

	@Override
	public GetQuotasResponse getQuotas(User user, String msisdn, String benMsisdn, String serviceId, String variantId, boolean forData, int languageId)
	{
		Number msisdnNumber = serviceProvider.generateMSISDNNumber(msisdn);
		return getQuotas(user, msisdnNumber, benMsisdn, serviceId, variantId, forData, languageId);
	}

	@Override
	public AddQuotaResponse addQuota(User user, String msisdn, String benMsisdn, String serviceId, String variantId, ServiceQuota serviceQuota, int languageId, boolean isTestMode)
	{
		// serviceQuota.setDaysOfWeek(arg0);
		AddQuotaResponse result = null;
		try
		{
			IHxC port = serviceProvider.getSOAPServicePort(user);

			AddQuotaRequest req = new AddQuotaRequest();

			req.setLanguageID(languageId);
			req.setCallerID(user.getUserId());
			req.setTransactionID(serviceProvider.createTransactionID());
			req.setSessionID(user.getUserId());
			req.setChannel(Channels.CRM);

			req.setSubscriberNumber(serviceProvider.generateMSISDNNumber(msisdn));
			req.setServiceID(serviceId);
			req.setVariantID(variantId);
			req.setMemberNumber(serviceProvider.generateMSISDNNumber(benMsisdn));
			req.setQuota(serviceQuota);
			if (isTestMode)
			{
				req.setMode(RequestModes.TEST_ONLY);
			}
			else
			{
				req.setMode(RequestModes.NORMAL);
			}
			result = port.addQuota(req);
		}
		catch (Exception e)
		{
			// e.printStackTrace();
		}
		return result;
	}

	@Override
	public RemoveQuotaResponse removeQuota(User user, String msisdn, String benMsisdn, String serviceId, String variantId, ServiceQuota quotaToRemove, int languageId, boolean testMode)
	{
		RemoveQuotaResponse result = null;
		try
		{
			// Now go and remove
			IHxC port = serviceProvider.getSOAPServicePort(user);

			RemoveQuotaRequest req = new RemoveQuotaRequest();

			req.setLanguageID(languageId);
			req.setCallerID(user.getUserId());
			req.setTransactionID(serviceProvider.createTransactionID());
			req.setSessionID(user.getUserId());
			req.setChannel(Channels.CRM);
			req.setMode(testMode ? RequestModes.TEST_ONLY : RequestModes.NORMAL);

			req.setSubscriberNumber(serviceProvider.generateMSISDNNumber(msisdn));
			req.setServiceID(serviceId);
			req.setVariantID(variantId);
			req.setMemberNumber(serviceProvider.generateMSISDNNumber(benMsisdn));
			req.setQuota(quotaToRemove);

			result = port.removeQuota(req);
		}
		catch (Exception e)
		{
			// e.printStackTrace();
		}
		return result;
	}

	@Override
	public GetBalancesResponse retrieveBalances(User user, String msisdn, String serviceId, String variantId, int languageId, boolean testMode)
	{
		GetBalancesResponse result = null;

		try
		{
			IHxC port = serviceProvider.getSOAPServicePort(user);

			GetBalancesRequest req = new GetBalancesRequest();
			req.setLanguageID(languageId);
			req.setCallerID(user.getUserId());
			req.setTransactionID(serviceProvider.createTransactionID());
			req.setSessionID(user.getUserId());
			req.setChannel(Channels.CRM);

			req.setSubscriberNumber(serviceProvider.generateMSISDNNumber(msisdn));
			req.setServiceID(serviceId);
			req.setVariantID(variantId);
			req.setMode(testMode ? RequestModes.TEST_ONLY : RequestModes.NORMAL);

			result = port.getBalances(req);
		}
		catch (Exception e)
		{
			// e.printStackTrace();
		}
		return result;
	}

	@Override
	public GetOwnersResponse getOwners(User user, String msisdn, String serviceId)
	{
		GetOwnersResponse result = null;

		try
		{
			IHxC port = serviceProvider.getSOAPServicePort(user);

			GetOwnersRequest req = new GetOwnersRequest();
			req.setLanguageID(user.getLanguageId());
			req.setCallerID(user.getUserId());
			req.setTransactionID(serviceProvider.createTransactionID());
			req.setSessionID(user.getUserId());
			req.setChannel(Channels.CRM);
			req.setHostName(HostInfo.getNameOrElseHxC());

			req.setMemberNumber(serviceProvider.generateMSISDNNumber(msisdn));
			req.setMode(RequestModes.NORMAL);
			req.setServiceID(serviceId);

			result = port.getOwners(req);
		}
		catch (Exception e)
		{
			// e.printStackTrace();
		}
		return result;
	}

	@Override
	public ServiceQuota retrieveServiceQuota(User user, String msisdn, String benMsisdn, String serviceId, String variantId, String quotaID, int languageId)
	{
		ServiceQuota result = null;
		try
		{
			GetQuotasResponse quotas = getQuotas(user, msisdn, benMsisdn, serviceId, variantId, false, languageId);
			ServiceQuota quotaToUpdate = null;

			for (ServiceQuota sq : quotas.getServiceQuotas())
			{
				if (sq.getQuotaID().equalsIgnoreCase(quotaID))
				{
					result = sq;
					break;
				}
			}
		}
		catch (Exception e)
		{
		}
		return result;
	}

	@Override
	public ChangeQuotaResponse updateQuaotaAmount(User user, String msisdn, String benMsisdn, String serviceId, String variantId, ServiceQuota quotaToUpdate, int languageId, long newQuantity, boolean testMode)
	{
		ChangeQuotaResponse result = null;
		try
		{
			// Get Quota to update

			ServiceQuota updatedQuota = null;
			if (quotaToUpdate != null)
			{
				updatedQuota = new ServiceQuota();
				updatedQuota.setDaysOfWeek(quotaToUpdate.getDaysOfWeek());
				updatedQuota.setDestination(quotaToUpdate.getDestination());
				updatedQuota.setName(quotaToUpdate.getName());
				updatedQuota.setQuotaID(quotaToUpdate.getQuotaID());
				updatedQuota.setService(quotaToUpdate.getService());
				updatedQuota.setTimeOfDay(quotaToUpdate.getTimeOfDay());
				updatedQuota.setUnits(quotaToUpdate.getUnits());
				updatedQuota.setQuantity(newQuantity);
			}

			IHxC port = serviceProvider.getSOAPServicePort(user);
			ChangeQuotaRequest req = new ChangeQuotaRequest();
			req.setLanguageID(user.getLanguageId());
			req.setHostName(HostInfo.getNameOrElseHxC());
			req.setCallerID(user.getUserId());
			req.setTransactionID(serviceProvider.createTransactionID());
			req.setSessionID(user.getUserId());
			req.setChannel(Channels.CRM);
			req.setMode(testMode ? RequestModes.TEST_ONLY : RequestModes.NORMAL);

			req.setSubscriberNumber(serviceProvider.generateMSISDNNumber(msisdn));
			req.setServiceID(serviceId);
			req.setVariantID(variantId);
			req.setMemberNumber(serviceProvider.generateMSISDNNumber(benMsisdn));

			req.setOldQuota(quotaToUpdate);
			req.setNewQuota(updatedQuota);

			result = port.changeQuota(req);
		}
		catch (Exception e)
		{
		}

		return result;
	}

	@Override
	public String getReturnCodeTranslation(User user, ReturnCodes returnCode, int languageId)
	{
		String result = null;
		try
		{
			IHxC port = serviceProvider.getSOAPServicePort(user);

			GetReturnCodeTextRequest req = new GetReturnCodeTextRequest();
			req.setLanguageID(user.getLanguageId());
			req.setCallerID(user.getUserId());
			req.setTransactionID(serviceProvider.createTransactionID());
			req.setSessionID(user.getUserId());
			req.setChannel(Channels.CRM);

			req.setHostName(HostInfo.getNameOrElseHxC());
			req.setMode(RequestModes.NORMAL);
			req.setVersion(serviceProvider.getSoapVersion());
			req.setReturnCode(returnCode);

			GetReturnCodeTextResponse resp = port.getReturnCodeText(req);
			if (resp.getReturnCode() == ReturnCodes.SUCCESS)
			{
				result = resp.getReturnCodeText();
				if (languageId == 1)
				{
					CCUtils cutils = new CCUtils();
					result = cutils.frenchToHtml(result);
				}
			}
		}
		catch (Exception e)
		{
			return null;
		}

		if (result == null)
		{
			CreditSharingTranslations ct = new CreditSharingTranslations(user.getLanguageId());
			result = ct.translate(MessageContext.defaultMessage);
		}

		return result;
	}

	@Override
	public VasServiceInfo getServiceInfo(User user, String msisdn, String serviceId, String variantId, int languageId)
	{
		VasServiceInfo result = null;

		try
		{
			GetServicesResponse services = serviceProvider.getServices(user, msisdn, languageId);
			if (services.getReturnCode() == ReturnCodes.SUCCESS)
			{
				for (VasServiceInfo vi : services.getServiceInfo())
				{
					if (vi.getServiceID().equals(serviceId) && vi.getVariantID().equals(variantId))
					{
						result = vi;
						break;
					}
				}
			}
		}
		catch (Exception e)
		{
		}
		return result;
	}

	@Override
	public GetCreditTransfersResponse getCreditTransfers(User user, String msisdn, String memberMsisdn, String serviceId, String variantId, String transferMode, boolean activeOnly, boolean test)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChangeCreditTransferResponse changeCreditTransfer(User user, String msisdn, String memberMsisdn, String serviceId, boolean test)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AddCreditTransferResponse addCreditTransfer(User user, String msisdn, String memberMsisdn, String serviceId, String transferMode, long amount, Long transferLimit, boolean test)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RemoveCreditTransfersResponse removeCreditTransfers(User user, String msisdn, String memberMsisdn, String serviceId, String variantId, String transferMode, boolean activeOnly, boolean test)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SuspendCreditTransferResponse suspendCreditTransfer(User user, String msisdn, String memberMsisdn, String serviceId, String variantId, String transferMode, boolean activeOnly, boolean test)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResumeCreditTransferResponse resumeCreditTransfer(User user, String msisdn, String memberMsisdn, String serviceId, String variantId, String transferMode, boolean activeOnly, boolean test)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
