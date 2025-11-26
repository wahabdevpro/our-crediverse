package hxc.utils.protocol.acip;

import hxc.connectors.air.Air;

/**
 * AccountFlags
 * 
 * The accountFlags parameters contains life cycle state flags of the account, indicating the actual status of the account. It is enclosed in a <struct> of its own.
 */
public class AccountFlags
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

	/*
	 * The twoStepActivationFlag parameter is used to indicate if two step activation is enabled or not.
	 */
	@Air(PC = "PC:03327")
	public Boolean twoStepActivationFlag;

}
