package cs.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.ApplicationDetailsConfiguration;
import cs.config.RestServerConfiguration;
import cs.service.interfaces.IQueryStringParameters;
import cs.template.CsRestTemplate;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.Area;
import hxc.ecds.protocol.rest.Cell;
import hxc.ecds.protocol.rest.ExResult;

@Service
public class CellService extends Exportable
{
	@Autowired
	private CsRestTemplate restTemplate;

	@Autowired
	private RestServerConfiguration restServerConfig;

	@Autowired
	private ApplicationDetailsConfiguration appConfig;

	private boolean configured = false;

	private String restServerUrl;

	@PostConstruct
	public void configure()
	{
		if (!configured)
		{
			this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getCellUrl();
			configured = true;
		}
	}

	public Long countCells(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/*");
		if (search != null && search.length() > 0) RestRequestUtil.standardSearch(uri, search);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
	}

	@Override
	public String listAsCsv(String filter, String search, long offset, int limit, IQueryStringParameters queryStringParams) throws Exception {
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/csv");
		RestRequestUtil.standardSearch(uri, search);
		RestRequestUtil.standardPaging(uri, offset, limit);
		RestRequestUtil.standardFilter(uri, filter);
		String response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, String.class);
		return response;
	}

	public Cell getCell(String cellId) throws Exception
	{
		Cell result = null;
		result = restTemplate.execute(restServerUrl+"/"+cellId, HttpMethod.GET, Cell.class);
		return result;
	}

	public Cell getCell(int mobileCountryCode, int mobileNetworkCode, int locationAreaCode, int cellId) throws Exception
	{
		Cell response = null;
		Cell[]responseList = null;
		String companyID = Integer.toString(appConfig.getCompanyid());
		String filter = "mcc='" + mobileCountryCode + "'+mnc='" + mobileNetworkCode + "'+lac='" + locationAreaCode + "'+cid='" + cellId + "'+companyID='"+ companyID +"'";
		responseList = restTemplate.execute(restServerUrl + "?" + filter, HttpMethod.GET, Cell[].class);
		response = responseList[0];
		return response;
	}

	public Area[] getMappedAreas(int cellId) throws Exception
	{
		Area[] result = null;
		result = restTemplate.execute(restServerUrl+"/"+ Integer.toString(cellId) + "/areas", HttpMethod.GET, Area[].class);
		return result;
	}

	public Cell[] listCells() throws Exception
	{
		Cell[] response = null;
		response = restTemplate.execute(restServerUrl, HttpMethod.GET, Cell[].class);
		return response;
	}

	public ExResult<Cell> listCells(int start, int length) throws Exception
	{
		StringBuilder urlString = new StringBuilder(restServerUrl);
		urlString.append("?");
		urlString.append("first=");
		urlString.append(String.valueOf(start));
		urlString.append("&max=");
		urlString.append(String.valueOf(length));
		ParameterizedTypeReference<ExResult<Cell>> type = new ParameterizedTypeReference<ExResult<Cell>>() {};
		ExResult<Cell> result = restTemplate.execute(restServerUrl, HttpMethod.GET, type);
		return result;
	}

	public ExResult<Cell> listCells(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardSearch(uri, search);
		ParameterizedTypeReference<ExResult<Cell>> type = new ParameterizedTypeReference<ExResult<Cell>>() {};
		ExResult<Cell> result = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, type);
		return result;
	}

	public Long listCellsCount(String filter, String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/*");
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);
		Long response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
		return response;
	}

	public ExResult<Cell> listCells(String filter, String search, Integer offset, Integer limit, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);

		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);

		if (offset != null && limit != null)
			RestRequestUtil.standardPaging(uri, offset, limit);

		if (sort != null)
			RestRequestUtil.standardSorting(uri, sort);

		ParameterizedTypeReference<ExResult<Cell>> type = new ParameterizedTypeReference<ExResult<Cell>>() {};
		ExResult<Cell> result = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, type);
		return result;
	}

	public void create(Cell newCell) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, newCell, Void.class);

	}

	public void delete(String cellId) throws Exception
	{
		restTemplate.execute(restServerUrl+"/"+cellId, HttpMethod.DELETE, Void.class);
	}

	public void update(Cell updatedCell) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, updatedCell, Void.class);
	}
}
