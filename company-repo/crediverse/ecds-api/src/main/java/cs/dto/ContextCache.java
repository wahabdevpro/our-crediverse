package cs.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ContextCache
{
	@JsonIgnore
	private String sessionId;
	private String key;
	
	private JsonNode data;
}
