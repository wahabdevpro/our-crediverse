package hxc.utils.protocol.ucip;

/**
 * UpdateBalanceAndDateResponse
 * 
 * The message UpdateBalanceAndDate is used by external system to adjust balances, start dates and expiry dates on the main account and the dedicated accounts. On the main account it is possible to
 * adjust the balance and expiry dates both negative and positive (relative) direction and it is also possible to adjust the expiry dates with absolute dates. The dedicated accounts balances, start
 * dates and expiry dates could be adjusted in negative and positive direction or with absolute values. Note: It is not possible to do both a relative and an absolute balance or date set for the same
 * data type (example: it is possible to either set an absolute OR a relative adjustment to the service fee expiry date). It is also possible to set the Service removal and Credit clearance periods on
 * account. Note: If pre-activation is wanted then messageCapabilityFlag.accountActivati onFlag should be included set to 1.
 */
public class UpdateBalanceAndDateResponse
{
	public UpdateBalanceAndDateResponseMember member;

	public UpdateBalanceAndDateResponse()
	{
		member = new UpdateBalanceAndDateResponseMember();
	}
}
