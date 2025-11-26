package hxc.utils.protocol.ucip;

import java.util.Date;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * AccountAfterRefill
 * 
 * The accountBeforeRefill and accountAfterRefill are enclosed in a <struct> of their own. accountBeforeRefill and accountAfterRefill contains financial and lifecycle data that might be affected
 * during a refill. The accountBeforeRefill struct carries data on account BEFORE the refill is applied. This information can be requested in order to be able to display the start values before
 * refill, in order to give the user the ability to verify the start conditions of the account when doing the refill. The accountAfterRefill struct carries data on account AFTER the refill is applied.
 * This information in the response and gives the result on the account after refill and promotions are added. * In accountBeforeRefill the serviceClassTemporaryExpiryDate and serviceClassOriginal are
 * returned only if the account has a temporary service class. * In accountAfterRefill the serviceClassTemporaryExpiryDate and serviceClassOriginal are always returned when the account is assigned a
 * temporary service class. * The promotionPlanID is returned in accountBeforeRefill if a promotion plan ID exists for the account. In accountAfterRefill it is only returned if a promotion plan
 * progression has been made. * The serviceFeeExpiryDate is only returned in accountAfterRefill when service fee date is changed. In accountBeforeRefill it is always returned if it exists in the
 * account database. * The supervisionExpiryDate is only returned in accountAfterRefill when supervision date is changed. In accountBeforeRefill it is always returned if it exists in the account
 * database. The creditClearanceDate is only returned in accountAfterRefill if supervision period or credit clearance removal period is changed. In accountBeforeRefill it is always returned if it
 * exists in the account database. * The serviceRemovalDate is only returned in accountAfterRefill if service fee date or service removal period is changed. In accountBeforeRefill it is always
 * returned if it exists in the account database.
 */
public class AccountAfterRefill
{
	/*
	 * The serviceClassTemporaryExpiryDate parameter contains the expiry date of a temporary service class of an account. A temporary service class has precedence before the normally assigned service
	 * class, as long as the temporary service class date expiry date is not passed.
	 */
	public Date serviceClassTemporaryExpiryDate;

	/*
	 * The serviceClassOriginal parameter contains the identity of the original service class when a temporary service class is active for an account. In case serviceClassOriginal is returned then the
	 * serviceClassCurrent will contain the temporary service class currently active for the account. When a temporary service class is active and a Return Service Class ID is specified, the
	 * serviceClassOriginal parameter will contain the identity of the return service class instead of the original service class. The account will then return to the specified Return Service Class ID
	 * when the temporary service class expires.
	 */
	@Air(Range = "0:9999")
	public Integer serviceClassOriginal;

	/*
	 * The serviceClassCurrent parameter contains the service class currently used by the subscriber. This might be a temporary Service Class, which is controlled by a temporary Service Class expiry
	 * date (separate parameter).
	 */
	@Air(Mandatory = true, Range = "0:9999")
	public int serviceClassCurrent;

	public AccountFlags accountFlags;

	/*
	 * The promotionPlanID parameter contains the identity of one of the current promotion plans of a subscriber.
	 */
	@Air(Length = "1:4", Format = "Alphanumeric")
	public String promotionPlanID;

	/*
	 * The serviceFeeExpiryDate parameter contains the expiry date of the service fee period.
	 */
	public Date serviceFeeExpiryDate;

	/*
	 * The supervisionExpiryDate parameter contains the expiry date of the supervision period.
	 */
	public Date supervisionExpiryDate;

	/*
	 * The creditClearanceDate parameter contains the date when the credit clearance period will expire.
	 */
	@Air(Range = "DateToday:DateMax")
	public Date creditClearanceDate;

	/*
	 * The serviceRemovalDate parameter contains the date when the account will be removed after the service removal period has expired.
	 */
	public Date serviceRemovalDate;

	/*
	 * The accountvalue1 and accountValue2 parameters contains the account value for the subscriber's master account. This is not taking in consideration any ongoing chargeable events. 1 indicates an
	 * account value in the first currency to be announced and 2 an account value in the second one.
	 */
	@Air(Mandatory = true, Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	public long accountValue1;

	/*
	 * The accountvalue1 and accountValue2 parameters contains the account value for the subscriber's master account. This is not taking in consideration any ongoing chargeable events. 1 indicates an
	 * account value in the first currency to be announced and 2 an account value in the second one.
	 */
	@Air(Range = "-999999999999:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long accountValue2;

	public DedicatedAccountInformation[] dedicatedAccountInformation;

	public UsageAccumulatorInformation[] usageAccumulatorInformation;

	public ServiceOfferings[] serviceOfferings;

	public CommunityIdList[] communityIdList;

	public OfferInformationList[] offerInformationList;

}
