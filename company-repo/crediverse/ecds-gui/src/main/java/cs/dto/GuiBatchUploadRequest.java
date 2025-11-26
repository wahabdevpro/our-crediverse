package cs.dto;

import java.util.UUID;

import hxc.ecds.protocol.rest.BatchUploadRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString()
public class GuiBatchUploadRequest extends BatchUploadRequest
{
	private String uuid;
	private String authorizedBy;
	private String language;
	private String reason;
	private int batchID;
	private Seperators seperators;
	private Integer webUserId;
	private Integer agentId;
	private UUID workItemId;
}
