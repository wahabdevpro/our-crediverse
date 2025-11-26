package hxc.utils.protocol.ucip;

/**
 * RefillResponse
 * 
 * The message Refill is used to apply a refill from an administrative system to a prepaid account associated with a specific subscriber identity. It can be a voucherless refill where an amount is
 * added to account, according to the refill profile rules. It can also be a voucher refill made for an example by customer care on request from the subscriber. The
 * requestSubDedicatedAccountDetailsFlag parameter will only affect whether sub dedicated account details are included in the accountBeforeRefill and accountAfterRefill structs. The refillInformation
 * struct is not affected by requestSubDedicatedAccountDetailsFlag and will always contain details on affected sub dedicated accounts. Note: In order to differentiate a voucherless refill from a
 * voucher refill, it is not allowed to send the N/A-marked parameters in the different refills. The different types of refill are mutual exclusive. Example: It is not allowed to give
 * transactionAmount in a voucher refill. Note: If pre-activation is wanted then messageCapabilityFlag.accountActivati onFlag should be included set to 1.
 */
public class RefillResponse
{
	public RefillResponseMember member;

	public RefillResponse()
	{
		member = new RefillResponseMember();
	}
}
