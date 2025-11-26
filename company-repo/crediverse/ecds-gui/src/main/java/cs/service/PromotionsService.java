package cs.service;

import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import cs.dto.GuiDataTable;
import cs.dto.GuiDataTableRequest;
import cs.utility.SearchFilter;
import cs.utility.SearchFilter.SearchOperations;
import hxc.ecds.protocol.rest.Promotion;

@Service
public class PromotionsService extends GenericService<Promotion>
{
	@Autowired
	private TypeConvertorService typeConvertorService;

	@PostConstruct
	public void configure()
	{
		this.loadConfigurion( restServerConfig.getPromotionsUrl() );
	}

	public Map<Integer, String> getDropDownMap(Optional<String> type, Optional<String> query) throws Exception
	{
		return getDropDownMap(type, query, "id", "name");
	}

	// Need a factory pattern for this:
	public String compileFilter(Map<String, String> params) throws Exception
	{
		SearchFilter filter = SearchFilter.createBasFilter(params)
				.filter("id", SearchOperations.IdEquals, "id")
				.filter("name", SearchOperations.Like, "name")
				.filter("targetPeriod", SearchOperations.Equals, "targetPeriod")
				.filter("state", SearchOperations.Equals, "state")
				.filter("transferRuleID", SearchOperations.Equals, "transferRuleID")
				.filter("areaID", SearchOperations.Equals, "areaID")
				.filter("serviceClassID", SearchOperations.Equals, "serviceClassID")
				.filter("bundleID", SearchOperations.Equals, "bundleID");

		return filter.toString();
	}

	public GuiDataTable searchableList(@RequestParam Map<String, String> params) throws Exception
	{
		GuiDataTableRequest dtr = new GuiDataTableRequest( params );
		String filter = this.compileFilter( params );

		Promotion[] promotions = null;

		if (dtr.getSearch() == null || dtr.getSearch().getValue().isEmpty())
			promotions = list( filter, null, -1, -1, null );
		else
			promotions = list( filter, dtr.getSearch().getValue(), -1, -1, null );

		return new GuiDataTable( typeConvertorService.getGuiPromotionsFromPromotions(promotions) );
	}

	public Long searchCount(@RequestParam Map<String, String> params) throws Exception
	{
		String filter = compileFilter( params );
		GuiDataTableRequest dtr = new GuiDataTableRequest( params );
		String search = (dtr.getSearch() != null)? dtr.getSearch().getValue() : null;

		return count(filter, search);
	}


}
