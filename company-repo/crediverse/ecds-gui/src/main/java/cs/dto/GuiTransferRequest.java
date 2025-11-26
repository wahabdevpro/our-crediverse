package cs.dto;

import hxc.ecds.protocol.rest.TransferRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString()
public class GuiTransferRequest extends TransferRequest {
	private String uuid;
	private String authorizedBy;
	private String language;
	private Seperators seperators;
	private Integer webUserId;
	private Integer agentId;
}
