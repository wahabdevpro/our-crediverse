package hxc.utils.protocol.ucip;

/**
 * AccountFlagsAfter
 * 
 * The accountFlagsAfter and accountFlagsbefore parameters contains life cycle state flags of the account, indicating the actual status of the account after and before. It is enclosed in a <struct> of
 * its own.
 */
public class AccountFlagsAfter
{
	/*
	 * This parameter is used to indicate if an account is activated or not.
	 */
	public Boolean activationStatusFlag;

	/*
	 * This parameter is used to indicate if an account is barred due to negative balance or not.
	 */
	public Boolean negativeBarringStatusFlag;

	/*
	 * This parameter is used to indicate if the supervision period date expiration warning is active or not.
	 */
	public Boolean supervisionPeriodWarningActiveFlag;

	/*
	 * This parameter is used to indicate if the service fee period date expiration warning is active or not.
	 */
	public Boolean serviceFeePeriodWarningActiveFlag;

	/*
	 * This parameter is used to indicate if the supervision period date has expired or not.
	 */
	public Boolean supervisionPeriodExpiryFlag;

	/*
	 * This parameter is used to indicate if the service fee period date has expired or not.
	 */
	public Boolean serviceFeePeriodExpiryFlag;

}
