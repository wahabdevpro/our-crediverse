package hxc.userinterfaces.gui.services;

import com.concurrent.hxc.AddCreditTransferResponse;
import com.concurrent.hxc.AddMemberResponse;
import com.concurrent.hxc.AddQuotaResponse;
import com.concurrent.hxc.ChangeCreditTransferResponse;
import com.concurrent.hxc.ChangeQuotaResponse;
import com.concurrent.hxc.GetBalancesResponse;
import com.concurrent.hxc.GetCreditTransfersResponse;
import com.concurrent.hxc.GetMembersResponse;
import com.concurrent.hxc.GetOwnersResponse;
import com.concurrent.hxc.GetQuotasResponse;
import com.concurrent.hxc.RemoveCreditTransfersResponse;
import com.concurrent.hxc.RemoveMemberResponse;
import com.concurrent.hxc.RemoveQuotaResponse;
import com.concurrent.hxc.ResumeCreditTransferResponse;
import com.concurrent.hxc.ReturnCodes;
import com.concurrent.hxc.ServiceQuota;
import com.concurrent.hxc.SubscribeResponse;
import com.concurrent.hxc.SuspendCreditTransferResponse;
import com.concurrent.hxc.UnsubscribeResponse;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.UpdateContactInfoResponse;
import com.concurrent.hxc.VasServiceInfo;

import hxc.userinterfaces.gui.data.User;

/**
 * Service Specic Calls
 * 
 * @author johne
 * 
 */
public interface ISubscriptionService
{

	public SubscribeResponse subscribe(User user, String msisdn, String serviceId, String variantId, int languageId, boolean testMode);

	public UnsubscribeResponse unsubscribe(User user, String msisdn, String serviceId, String variantId, int languageId, boolean testMode);

	// Member Calls
	public GetMembersResponse getMembers(User user, String msisdn, String serviceId, String variantId, int languageId);

	public AddMemberResponse addBeneficiary(User user, String msisdn, String benMsisdn, String serviceId, String variantId, int languageId, boolean testMode);

	public RemoveMemberResponse removeMember(User user, String msisdn, String benMsisdn, String serviceId, String variantId, int languageId, boolean testMode, boolean isFromConsumer);

	public UpdateContactInfoResponse updateContactInfo(User user, String msisdn, String contactName, String serviceId, String variantId, int languageId, boolean testMode);

	// Quota Calls
	public GetQuotasResponse getQuotas(User user, String msisdn, String benMsisdn, String serviceId, String variantId, boolean forData, int languageId);

	public GetQuotasResponse getQuotas(User user, Number msisdn, String benMsisdn, String serviceId, String variantId, boolean forData, int languageId);

	public AddQuotaResponse addQuota(User user, String msisdn, String benMsisdn, String serviceId, String variantId, ServiceQuota serviceQuota, int languageId, boolean testMode);

	public RemoveQuotaResponse removeQuota(User user, String msisdn, String benMsisdn, String serviceId, String variantId, ServiceQuota quotaToRemove, int languageId, boolean testMode);

	public GetBalancesResponse retrieveBalances(User user, String msisdn, String serviceId, String variantId, int languageId, boolean testMode);

	public ServiceQuota retrieveServiceQuota(User user, String msisdn, String benMsisdn, String serviceId, String variantId, String quotaID, int languageId);

	public ChangeQuotaResponse updateQuaotaAmount(User user, String msisdn, String benMsisdn, String serviceId, String variantId, ServiceQuota quotaToUpdate, int languageId, long newQuantity, boolean testMode);

	// Consumer perspective
	public GetOwnersResponse getOwners(User user, String msisdn, String serviceId);

	public String getReturnCodeTranslation(User user, ReturnCodes returnCode, int languageId);

	public VasServiceInfo getServiceInfo(User user, String msisdn, String serviceId, String variantId, int languageId);

	// New For ACT
	public GetCreditTransfersResponse getCreditTransfers(User user, String msisdn, String memberMsisdn, String serviceId, String variantId, String transferMode, boolean activeOnly, boolean test);

	public ChangeCreditTransferResponse changeCreditTransfer(User user, String msisdn, String memberMsisdn, String serviceId, boolean test);

	public AddCreditTransferResponse addCreditTransfer(User user, String msisdn, String memberMsisdn, String serviceId, String transferMode, long amount, Long transferLimit, boolean test);

	public RemoveCreditTransfersResponse removeCreditTransfers(User user, String msisdn, String memberMsisdn, String serviceId, String variantId, String transferMode, boolean activeOnly, boolean test);

	public SuspendCreditTransferResponse suspendCreditTransfer(User user, String msisdn, String memberMsisdn, String serviceId, String variantId, String transferMode, boolean activeOnly, boolean test);

	public ResumeCreditTransferResponse resumeCreditTransfer(User user, String msisdn, String memberMsisdn, String serviceId, String variantId, String transferMode, boolean activeOnly, boolean test);
}
