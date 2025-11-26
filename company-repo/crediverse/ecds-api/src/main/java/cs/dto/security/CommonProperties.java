package cs.dto.security;

import hxc.ecds.protocol.rest.AuthenticationRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class CommonProperties
{
	protected String channel = AuthenticationRequest.CHANNEL_WUI;
	protected int companyID;
	protected String macAddress;
	protected String ipAddress;
	protected String hostName;
	
	public void invalidate()
	{
		companyID = -1;
		macAddress = null;
		ipAddress = null;
		hostName = null;
	}
}
