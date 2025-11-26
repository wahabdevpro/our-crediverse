package hxc.ecds.protocol.rest;

public interface IAuthenticatable 
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	
	public static final String AUTHENTICATE_PASSWORD_2FACTOR = "A";
	public static final String AUTHENTICATE_PIN_2FACTOR = "P";
	public static final String AUTHENTICATE_EXTERNAL_2FACTOR = "X";
	
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public abstract boolean isTemporaryPin();

	public abstract IAuthenticatable setTemporaryPin(boolean temporaryPin);

    public abstract boolean isPinLockedOut();

	public abstract IAuthenticatable setPinLockedOut(boolean pinLockedOut);

	public abstract int getPinVersion();

	public abstract IAuthenticatable setPinVersion(int pinVersion);
	
	public abstract String getAuthenticationMethod();

	public abstract IAuthenticatable setAuthenticationMethod(String authenticationMethod);
	
	public abstract Integer getConsecutiveAuthFailures();
	
	public abstract IAuthenticatable setConsecutiveAuthFailures(Integer consecutiveAuthFailures);
	
}
