package cs.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.service.batch.BatchDetailService;

@RestController
@RequestMapping("/api/batch/history")
public class BatchDetailController
{
	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private BatchDetailService batchDetailService;

	@RequestMapping(value="count/{batchId}", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode count(@PathVariable("batchId") int batchId) throws Exception
	{
		Integer count = 0;

		count = batchDetailService.listTransactionsCount(batchId);

		ObjectNode response = mapper.createObjectNode();
		response.put("count", (count != null)?count:0);
		return response;
	}

	@RequestMapping(value="csv/{batchId}", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode get(@PathVariable("batchId") int batchId, @RequestParam(value="start", required=false) Long offset, @RequestParam(value="length", required=false) Integer length) throws Exception
	{
		ObjectNode response = mapper.createObjectNode();
		ArrayNode arr = response.putArray("data");
		Integer count = batchDetailService.listTransactionsCount(batchId);
		for (String line : batchDetailService.listTransactionsAsCsv(batchId, offset, length))
		{
			ArrayNode arrLine = mapper.createArrayNode();
			for (String item : line.split(","))
			{
				arrLine.add(item);
			}
			arr.add(arrLine);
		}
		response.put("recordsTotal", count);
		response.put("recordsFiltered", count);
		return response;
	}

	/*
	 * {
						   data: "description",
						   defaultContent: "(none)",
						   title: i18ntxt.detailsTitle
					   },
					   {
						   data: "createdByName",
						   title: i18ntxt.fromTitle,
						   defaultContent: "(none)"
					   },
	 */
	@RequestMapping(value="header/{batchId}", method = RequestMethod.GET)
	@ResponseBody
	public ArrayNode get(@PathVariable("batchId") int batchId) throws Exception
	{
		ArrayNode arr = mapper.createArrayNode();

		List<String> headings = batchDetailService.getCsvHeader(batchId);
		for (int i=0; i<headings.size(); i++)
		{
			ObjectNode column = mapper.createObjectNode();
			column.put("data", i);
			column.put("defaultContent", "(none)");
			column.put("title", headings.get(i));
			arr.add(column);
		}
		return arr;
	}
}
