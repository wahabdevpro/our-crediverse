package cs.controller;
/*

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cs.dto.batch.BatchMsisdnRecycleSubmit;
import cs.dto.msisdnrecycle.MsisdnRecycleUpload;
import cs.service.batch.MsisdnRecycleUploadService;
import cs.utility.BatchUtility;
import cs.utility.BatchUtility.BatchFileType;
import cs.utility.Common;
import hxc.ecds.protocol.rest.MsisdnRecycleResponse;
import hxc.ecds.protocol.rest.MsisdnSubmitRecycleResponse;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cs.dto.GuiDataTable;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
*/


/*
Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
 */
/*@RestController
@RequestMapping("/msisdn_recycle")*/
public class MsisdnRecycleController
{
	//private static final Logger logger = LoggerFactory.getLogger(MsisdnRecycleController.class);

/*	@Autowired
	private BatchImportService batchImportService;*/

	/*@Autowired
	private MsisdnRecycleUploadService msisdnRecycleUploadService;

	@Autowired
	private ObjectMapper mapper;

	//@RequestMapping(value = "/import", method = RequestMethod.GET)
	public @ResponseBody String provideUploadInfo()
	{
		return "Use POST to upload a file";
	}

	@RequestMapping(value = "list/{uuid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable list(@PathVariable("uuid") String uuid) throws Exception
	{
		MsisdnRecycleResponse msisdnRecycleResponse = msisdnRecycleUploadService.getBatchImportResults(uuid);
		return (new GuiDataTable(msisdnRecycleResponse.getAgentAccountInfoList()));
	}
	
	@RequestMapping(value = "results/{uuid}", method = RequestMethod.GET)
	public @ResponseBody Object getUploadResults(@PathVariable("uuid") String uuid,
												@RequestParam(value = "offset") Optional<Integer> offset,
												@RequestParam(value = "limit")  Optional<Integer> limit) throws Exception
	{
		MsisdnRecycleResponse msisdnRecycleResponse = msisdnRecycleUploadService.getBatchImportResults(uuid);
		return msisdnRecycleResponse;
	}
	
	@RequestMapping(value = "status/{uuid}", method = RequestMethod.GET)
	public @ResponseBody Object getUploadStatus(@PathVariable("uuid") String uuid,
		   @RequestParam(value = "offset") Optional<Integer> offset,
		   @RequestParam(value = "limit")  Optional<Integer> limit) throws Exception
	{
		Object status = msisdnRecycleUploadService.getBatchImportStatus(uuid, offset, limit);
		return status;
	}

	*//**
	 * Consumes JSON list/array of agent ids [123, 321, 435]
	 * @param agentIds
	 * @return
	 * @throws Exception
	 *//*
	@RequestMapping(value = "submit", method = RequestMethod.POST,  consumes = "application/json")
	public @ResponseBody Object submitIdsForRecycling(@RequestBody List<Integer> agentIds) throws Exception
	{
		BatchMsisdnRecycleSubmit batchMsisdnRecycleSubmit = new BatchMsisdnRecycleSubmit();
		batchMsisdnRecycleSubmit.setAgentIds(agentIds);

		MsisdnSubmitRecycleResponse msisdnSubmitRecycleResponse = msisdnRecycleUploadService.submitAgentsForRecyclingMsisdn(batchMsisdnRecycleSubmit);
	
		return msisdnSubmitRecycleResponse;
	}

	private String fudgeFilename(String name)
	{
		String[] fileParts = name.split("_");
		StringBuilder newFilename = new StringBuilder(fileParts[0]);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_hhmmss");
		String dateTime = formatter.format(new Date());

		newFilename.append("_");
		if (fileParts.length > 1) newFilename.append(fileParts[1]);
		newFilename.append("_");
		if (fileParts.length > 2) newFilename.append(fileParts[2]);
		newFilename.append("_");
		newFilename.append(dateTime);
		newFilename.append(".csv");

		return newFilename.toString();
	}

	@RequestMapping(value = "upload")
	public @ResponseBody ObjectNode handleFileUpload(HttpServletRequest request,
			@RequestParam Map<String,String> allRequestParams,
			//@RequestParam("filename") String filename,
			@RequestParam("fileimport") MultipartFile file,
			ModelMap model,
			RedirectAttributes redirectAttributes) throws Exception
	{
		ObjectNode response = mapper.createObjectNode();
		try
		{
			MsisdnRecycleUpload importDetails = new MsisdnRecycleUpload();
			String filename = file.getOriginalFilename();
			if (!filename.toLowerCase().endsWith(".csv"))
			{
				throw new Exception("MUST_CONFORM_TO_PATTERN");
			}
			if (Common.isDevelopment())
			{
				filename = fudgeFilename(filename);
			}
			importDetails.setLocalFilename(filename);

			importDetails.setSize(file.getSize());
			importDetails.setDataInputStream(file.getInputStream());

			MsisdnRecycleUpload batchImport = null;
			BatchFileType type = BatchUtility.getFileType(filename);
			switch(type)
			{
				
				case MOBRECYCLE:
					batchImport = msisdnRecycleUploadService.queueBatchUpload(importDetails);
					response.putPOJO("response", batchImport);
					response.put("success", true);
					response.put("coauth", false);
					response.put("uuid", importDetails.getUuid());
					break;
				case ADJUST:
					importDetails.setCoauth(true);
					batchImport = msisdnRecycleUploadService.queueBatchUpload(importDetails);
					response.putPOJO("response", batchImport);
					response.put("success", true);
					response.put("coauth", true);
					response.put("uuid", importDetails.getUuid());
					break;
				case INVALID:
					response.put("error", true);
					break;
				case AUDIT:
				case TDR:
				default:
					break;
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			response.put("error", true);
			response.put("message", e.getMessage());
			logger.error("", e);
			throw e;

		}
	
		return response;
	}*/



	
	
}
