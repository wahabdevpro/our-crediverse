package hxc.ecds.protocol.rest;

public class CoSignableUtils
{
	public static final int CO_SIGNATORY_SESSION_ID_MAX_LENGTH = 50;
	public static final int CO_SIGN_FOR_SESSION_ID_MAX_LENGTH = 50;
	public static final int CO_SIGNATORY_TRANSACTION_ID_MAX_LENGTH = 64;
	public static final int CO_SIGNATORY_OTP_MAX_LENGTH = 64;

	public static Validator validate(Validator validator, ICoSignable coSignable, boolean mandatory)
	{
		if ( mandatory || coSignable.getCoSignatorySessionID() != null )
		{
			validator.notEmpty("coSignatorySessionID", coSignable.getCoSignatorySessionID(), CO_SIGNATORY_SESSION_ID_MAX_LENGTH);
			if ( coSignable.getCoSignatoryTransactionID() != null )
				validator.notEmpty("coSignatoryTransactionID", coSignable.getCoSignatoryTransactionID(), CO_SIGNATORY_TRANSACTION_ID_MAX_LENGTH);
			if ( coSignable.getCoSignatoryOTP() != null )
				validator.notEmpty("coSignatoryOTP", coSignable.getCoSignatoryOTP(), CO_SIGNATORY_OTP_MAX_LENGTH);
		}
		return validator;
	}
	public static Validator validate(Validator validator, ICoSignFor coSignFor, boolean mandatory)
	{
		if ( mandatory || coSignFor.getCoSignForSessionID() != null || coSignFor.getCoSignatoryTransactionID() != null )
		{
			validator.notEmpty("coSignForSessionID", coSignFor.getCoSignForSessionID(), CO_SIGN_FOR_SESSION_ID_MAX_LENGTH);
			validator.notEmpty("coSignatoryTransactionID", coSignFor.getCoSignatoryTransactionID(), CO_SIGNATORY_TRANSACTION_ID_MAX_LENGTH);
		}
		return validator;
	}
}
