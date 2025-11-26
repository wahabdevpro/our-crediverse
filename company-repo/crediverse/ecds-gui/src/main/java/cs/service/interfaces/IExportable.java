package cs.service.interfaces;

import org.springframework.web.util.UriComponentsBuilder;

import cs.dto.GuiCsvResponse;

public interface IExportable
{
	public String listAsCsv(String filter, String search, long offset, int limit, IQueryStringParameters queryStringParams) throws Exception;

	public String listAsCsv(String filter, String search, boolean withQuery, long offset, int limit, IQueryStringParameters queryStringParams) throws Exception;

	public GuiCsvResponse listAsCsv(UriComponentsBuilder uri, String filter, String search, long offset, IQueryStringParameters queryStringParams) throws Exception;

	public GuiCsvResponse listAsCsv(UriComponentsBuilder uri, String filter, String search, long offset, int limit, IQueryStringParameters queryStringParams) throws Exception;
}
