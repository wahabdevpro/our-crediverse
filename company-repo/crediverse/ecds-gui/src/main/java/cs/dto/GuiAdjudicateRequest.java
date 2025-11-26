package cs.dto;

import java.math.BigDecimal;

import hxc.ecds.protocol.rest.AdjudicateRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiAdjudicateRequest extends AdjudicateRequest
{
	private String uuid;
	private String language;
	private Seperators seperators;
	private String authorizedBy;
	private Integer webUserId;
	private Integer agentId;
	private BigDecimal amount;
}

