package cs.dto;


import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.cli.MissingArgumentException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class GuiMonthlySalesPerformanceReportSpecification {
	private int id;
	private String name;
	private String description;
	private String period;
	private List<String> tiers;
	private List<String> groups;
	private List<String> ownerAgents;
	private List<String> agents;
	private String transactionTypes;
	private Boolean transactionStatus;
	
	public GuiMonthlySalesPerformanceReportSpecification(Map<String, String> params) throws MissingArgumentException, JsonParseException, JsonMappingException, IOException
	{
		ObjectMapper mapper = new ObjectMapper();
		if(params.containsKey("name"))
			name = params.get("name");
		
		if(params.containsKey("description"))
			description = params.get("description");
		
		if(params.containsKey("period"))
			period = params.get("period");
		else 
			throw new MissingArgumentException("Missing argument: period");
		
		if(params.containsKey("tiers"))
			tiers  = mapper.readValue(params.get("tiers"), new TypeReference<List<String>>(){});
		
		if(params.containsKey("groups"))
			groups = mapper.readValue(params.get("groups"), new TypeReference<List<String>>(){});
		
		if(params.containsKey("ownerAgents"))
			ownerAgents = mapper.readValue(params.get("ownerAgents"), new TypeReference<List<String>>(){});
		
		if(params.containsKey("agents"))
			agents = mapper.readValue(params.get("agents"), new TypeReference<List<String>>(){});
		
		//Fields critical to the query and can't be empty: transactionTypes, transactionStatus
		if(params.containsKey("transactionTypes"))
			transactionTypes = params.get("transactionTypes");
		else 
			throw new MissingArgumentException("Missing argument: transactionTypes");
		
		if(params.containsKey("transactionStatus"))
			transactionStatus = "true".equals(params.get("transactionStatus"));
		else 
			throw new MissingArgumentException("Missing argument: transactionStatus");
	}
}
