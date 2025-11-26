package cs.dto;

import hxc.ecds.protocol.rest.ReplenishRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiReplenishRequest extends ReplenishRequest {
	private String uuid;
	private String authorizedBy;
	private String language;
	private Seperators seperators;
}
