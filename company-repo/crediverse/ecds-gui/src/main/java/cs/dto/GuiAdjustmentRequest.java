package cs.dto;

import hxc.ecds.protocol.rest.AdjustmentRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString()
public class GuiAdjustmentRequest extends AdjustmentRequest {
	private String uuid;
	private String authorizedBy;
	private String language;
	private Seperators seperators;
	private Integer webUserId;
	private Integer agentId;
}
