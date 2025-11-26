package cs.dto.security;

import cs.dto.data.BaseRequest;
import hxc.ecds.protocol.rest.AuthenticationResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
public class AuthenticationData
{
	public enum AuthenticationState
	{
		INVALID, AUTHENTICATED, REQUIRES_RSA, REQUIRES_TEXT
	}

	public enum CaptureField
	{
		USERNAME, NONE
	}

	private AuthenticationState currentState = AuthenticationState.INVALID;
	private CaptureField captureField = CaptureField.NONE;
	
	@Setter
	private String serverSessionID;
	private byte[] data1;
	private byte[] data2;
	
	@Setter
	private String uuid;
	
	@Setter
	private boolean coauth;
	
	@Setter
	private String forTransactionId;
	
	@Setter
	private String username;
	
	public boolean setCurrentState(String newState)
	{
		captureField = CaptureField.NONE;
		switch (newState)
		{
			case AuthenticationResponse.CODE_OK_AUTHENTICATED:
				currentState = AuthenticationState.AUTHENTICATED;
				break;
//			case AuthenticationResponse.CODE_OK_NOW_REQUIRE_RSA_NEW_PASSWORD:
			case AuthenticationResponse.CODE_OK_NOW_REQUIRE_RSA_PASSWORD:
			case AuthenticationResponse.CODE_OK_NOW_REQUIRE_RSA_PIN:
				currentState = AuthenticationState.REQUIRES_RSA;
				break;
			case AuthenticationResponse.CODE_OK_NOW_REQUIRE_UTF8_USERNAME:
				captureField = CaptureField.USERNAME;
			case AuthenticationResponse.CODE_OK_NOW_REQUIRE_UTF8_OTP:
			case AuthenticationResponse.CODE_OK_NOW_REQUIRE_UTF8_IMSI:				
				currentState = AuthenticationState.REQUIRES_TEXT;
				break;
			default:
				currentState = AuthenticationState.INVALID;
				break;
		}
		return true;
	}

	public boolean captureData(BaseRequest request)
	{
		switch (captureField)
		{
			case USERNAME:
				username = request.getData().toLowerCase();
				break;
			default:
				break;
		}
		return true;
	}
	
	public boolean setCurrentState(String newState, byte[] data1, byte[] data2)
	{
		if (setCurrentState(newState))
		{
			this.data1 = data1;
			this.data2 = data2;
		}
		return true;
	}

	public boolean setCurrentState(AuthenticationState newState)
	{
		currentState = newState;
		return false;
	}

	public AuthenticationState getCurrentState()
	{
		return currentState;
	}
	
	public void invalidate()
	{
		currentState = AuthenticationState.INVALID;
		serverSessionID = null;
		data1 = null;
		data2 = null;
		username = null;
	}
}
