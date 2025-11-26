package cs.dto.security;

import hxc.ecds.protocol.rest.AuthenticationRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class GuiAuthenticationRequest extends AuthenticationRequest {
	private String serverSessionID;
}
