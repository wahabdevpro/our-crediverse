package cs.dto;

import hxc.ecds.protocol.rest.PartialReversalRequestWithCoAuth;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiReversalCoAuthRequest extends PartialReversalRequestWithCoAuth
{
	public enum ReversalType {FULL, PARTIAL}
	private ReversalType type;
	private String uuid;
	private String language;
	private Seperators seperators;
	private String authorizedBy;
}
