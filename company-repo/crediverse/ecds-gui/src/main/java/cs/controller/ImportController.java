package cs.controller;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.dto.batch.BatchImport;
import cs.service.batch.BatchImportService;
import cs.utility.BatchUtility;
import cs.utility.BatchUtility.BatchFileSubType;
import cs.utility.BatchUtility.BatchFileType;
import cs.utility.Common;

@Controller
public class ImportController
{
	private static final Logger logger = LoggerFactory.getLogger(ImportController.class);

	@Autowired
	private BatchImportService batchImportService;

	@Autowired
	private ObjectMapper mapper;

	//@RequestMapping(value = "/import", method = RequestMethod.GET)
	public @ResponseBody String provideUploadInfo()
	{
		return "Use POST to upload a file";
	}

	@RequestMapping(value = "/import/status/{uuid}", method = RequestMethod.GET)
	public @ResponseBody Object getUploadStatus(@PathVariable("uuid") String uuid,
		   @RequestParam(value = "offset") Optional<Integer> offset,
		   @RequestParam(value = "limit")  Optional<Integer> limit) throws Exception
	{
		Object status = batchImportService.getBatchImportStatus(uuid, offset, limit);
		return status;
	}

	@RequestMapping(value = "/import/process/{uuid}", method = RequestMethod.POST)
	public @ResponseBody Object processUploaded(@PathVariable("uuid") String uuid) throws Exception
	{
		Object status = batchImportService.processUploaded(uuid);
		return status;
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

	@RequestMapping(value = "import")
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
			BatchImport importDetails = new BatchImport();
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

			BatchImport batchImport = null;
			BatchFileType type = BatchUtility.getFileType(filename);
			switch(type)
			{
				case USER:
				case CELL:
				case AREA:
				case TIER:
				case SC:
				case ACCOUNT:
				case GROUP:
				case RULE:
				case PROM:
				case DEPT:
				case CELLGROUP:
					batchImport = batchImportService.queueBatchUpload(importDetails);
					response.putPOJO("response", batchImport);
					response.put("success", true);
					response.put("coauth", false);
					response.put("uuid", importDetails.getUuid());
					break;
				case ADJUST:
					importDetails.setCoauth(true);
					batchImport = batchImportService.queueBatchUpload(importDetails);
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
		/*if (!file.isEmpty())
		{
			try {
				byte[] bytes = file.getBytes();
				File temp = File.createTempFile(file.getOriginalFilename(), ".tmp");
				temp.deleteOnExit();
				filename = temp.getCanonicalPath();
				BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(temp));
				stream.write(bytes);
				stream.close();
				return "Upload of " + name + " completed successfully ";
			}
			catch (Exception e)
			{
				return "Upload of " + name + " failed => " + e.getMessage();
			}
		}
		else
		{
			return "Upload of " + name + " failed because the file was empty.";
		}*/
		return response;
	}

	/*@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}*/

	@RequestMapping(value = "/import/examples/{fileType}", method = RequestMethod.GET)
	@ResponseBody
	public void getSampleHeader(@PathVariable("fileType") String fileType, HttpServletResponse response) throws Exception
	{
		BatchFileType batchType = BatchFileType.ACCOUNT;

		if (fileType != null)
		{
			batchType = Enum.valueOf(BatchFileType.class, fileType.toUpperCase());
		}

		String outputResult = batchImportService.getExportHeadings(response, batchType);

		OutputStream outputStream = response.getOutputStream();
		outputStream.write(outputResult.getBytes());
		outputStream.flush();
		outputStream.close();
	}

	@RequestMapping(value = "/import/examples/{fileType}/{subType}", method = RequestMethod.GET)
	@ResponseBody
	public void getSampleHeader(@PathVariable("fileType") String fileType, @PathVariable("subType") String fileSubType, HttpServletResponse response) throws Exception
	{
		BatchFileType batchType = BatchFileType.ACCOUNT;
		BatchFileSubType subType = BatchFileSubType.INCLUSIVE;

		if (fileType != null)
		{
			batchType = BatchFileType.valueOf(fileType.toUpperCase());
		}
		if (fileSubType != null)
		{
			subType = BatchFileSubType.valueOf(fileSubType.toUpperCase());
		}



		String outputResult = batchImportService.getExportHeadings(response, batchType, subType);

		OutputStream outputStream = response.getOutputStream();
		outputStream.write(outputResult.getBytes());
		outputStream.flush();
		outputStream.close();
	}

}
