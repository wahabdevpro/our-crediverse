package cs.export;

import org.springframework.web.util.UriComponentsBuilder;

import cs.service.interfaces.IQueryStringParameters;
import hxc.ecds.protocol.rest.TransactionEx;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TdrExportParameters implements IQueryStringParameters 
{
	public static final String INCLUDEQUERY = "includequery";
	
	private Integer virtualAgentID_AB;
	private Boolean includeQuery;
	@Override
	public void addParameters(UriComponentsBuilder uri) {
		if(virtualAgentID_AB != null)
			uri.replaceQueryParam(TransactionEx.VIRTUAL_FILTER_AGENTIDAB, virtualAgentID_AB);
		if(includeQuery != null)
			uri.replaceQueryParam(INCLUDEQUERY, String.valueOf(includeQuery?1:0));
	}
}
