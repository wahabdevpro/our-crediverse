package cs.dto.batch;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cs.dto.data.BaseResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

@Setter
@Getter
@ToString
public class BatchMsisdnRecycleSubmit extends BaseResponse {
	@JsonIgnore

	private List<Integer> agentIds;
	private String sessionID; // Used when import needs to be coauthorised
	private String coSignatorySessionID; // Used when import needs to be coauthorised

	public List<Integer> getAgentIds() {
		return agentIds;
	}

	public void setAgentIds(List<Integer> agentIds) {
		this.agentIds = agentIds;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public String getCoSignatorySessionID() {
		return coSignatorySessionID;
	}

	public void setCoSignatorySessionID(String coSignatorySessionID) {
		this.coSignatorySessionID = coSignatorySessionID;
	}
}
