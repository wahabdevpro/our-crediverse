package cs.controller;

import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.dto.GuiDataTable;
import cs.dto.GuiDataTableRequest;
import cs.dto.GuiPromotion;
import cs.dto.GuiPromotionDetailed;
import cs.dto.security.LoginSessionData;
import cs.service.ConfigurationService;
import cs.service.PromotionsService;
import cs.service.TypeConvertorService;
import cs.utility.BatchUtility;
import cs.utility.BatchUtility.BatchFileType;
import cs.utility.Common;
import hxc.ecds.protocol.rest.Promotion;

@RestController
@RequestMapping("/api/promos")
@Profile(Common.CONST_ADMIN_PROFILE)
public class PromotionsController
{
	@Autowired
	private PromotionsService promotionsService;

	@Autowired
	private TypeConvertorService typeConvertorService;

	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private ConfigurationService configService;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable list(@RequestParam Map<String, String> params) throws Exception
	{
		return promotionsService.searchableList(params);
	}

	@RequestMapping(value="{promotionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiPromotionDetailed getGuiPromotionDetailed(@PathVariable("promotionId") String promotionId) throws Exception
	{
		Promotion promotion =  promotionsService.retrieve(promotionId);
		GuiPromotionDetailed gpd = typeConvertorService.getGuiDetailedPromotionsFromPromotion(promotion);
		return gpd;
	}

	@RequestMapping(value="{promotionId}", method = RequestMethod.DELETE)
	public String delete(@PathVariable("promotionId") String promotionId) throws Exception
	{
		promotionsService.delete(promotionId);
		return "{}";
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiPromotion create(@RequestBody(required = true) GuiPromotion newPromotion, Locale locale) throws Exception
	{
		promotionsService.create(newPromotion.exportPromotion());
		return newPromotion;
	}

	@RequestMapping(method = RequestMethod.PUT)
	public GuiPromotion update(@RequestBody(required = true) GuiPromotion updatedPromotion, Locale locale) throws Exception
	{
		promotionsService.update(updatedPromotion.exportPromotion());
		return updatedPromotion;
	}

	@RequestMapping(value="search/csv", method = RequestMethod.GET)
	@ResponseBody
	public void listSearchResultsCsv(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
	{
		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.PROM, ".csv"));

		String filter = promotionsService.compileFilter( params );

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );
		String search = (dtr.getSearch() != null)? dtr.getSearch().getValue() : null;
		long promoCount = promotionsService.count(filter, search);

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(promoCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		OutputStream outputStream = response.getOutputStream();
		promotionsService.csvExport(params.get("uniqid"), outputStream, search, recordsPerChunk, promoCount, true, null);
	}

	@RequestMapping(value="search/count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode countSearchResults(@RequestParam Map<String, String> params, @RequestParam(defaultValue="true") boolean docount) throws Exception
	{
		long count = 0L;
		if (docount) count = promotionsService.searchCount(params);

		return promotionsService.track(count);
	}
}
