package cs.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.dto.ContextCache;
import cs.dto.data.ContextData;
import cs.service.ContextService;
import cs.service.CorrelationIdService;

@RestController
@RequestMapping(value={"context"})
public class ContextController
{
	@Autowired
	protected ContextService contextService;

	@Autowired
	protected CorrelationIdService correlationIdService;

	@Autowired
	protected ObjectMapper mapper;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ContextData index(HttpSession session) throws Exception
	{
		// Common between all contexts
		ContextData data = contextService.getContextData();
		int initialTimeout = session.getMaxInactiveInterval() - 30;
		data.setTimeout(initialTimeout);

		return data;
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ContextData keepAlive(HttpSession session) throws Exception
	{
		ContextData data = contextService.getContextData();
		int initialTimeout = session.getMaxInactiveInterval() - 30;
		data.setTimeout(initialTimeout);
		return data;
	}

	@RequestMapping(value="cache/{key}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ContextCache cacheGet(@PathVariable("key") String key) throws Exception
	{
		ContextCache data = contextService.cacheGet(key);
		return data;
	}

	@RequestMapping(value="cache", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ObjectNode cachePut(@RequestBody String data) throws Exception
	{
		ObjectNode response = mapper.createObjectNode();
		ContextCache actualObj = mapper.readValue(UriUtils.decode(data, "UTF-8"), ContextCache.class);//.readTree(UriUtils.decode(data, "UTF-8"));
		//logger.error(mapper.writeValueAsString(actualObj));
		contextService.cachePut(actualObj);
		response.put("success", true);
		return response;
	}

	@RequestMapping(value="util/uniqid", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ObjectNode getUniqueId() throws Exception
	{
		ObjectNode response = mapper.createObjectNode();
		response.put("uniqueId", correlationIdService.getUniqueId());
		return response;
	}

	@RequestMapping(value="util/status/{uniqid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ObjectNode getUniqueId(@PathVariable("uniqid") String uniqid) throws Exception
	{
		contextService.getContextData();// Used for keep alive
		ObjectNode response = mapper.createObjectNode();
		response.put("uniqid", uniqid);
		response.put("complete", correlationIdService.trackerComplete(uniqid));
		return response;
	}
}
