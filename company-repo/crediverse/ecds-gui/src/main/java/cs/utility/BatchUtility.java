package cs.utility;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import cs.constants.ApplicationConstants;
import hxc.ecds.protocol.rest.BatchUploadRequest;

public class BatchUtility
{
	public static final String CONST_MUST_BE_CSV_FILE = "MUST_BE_CSV_FILE";
	public static final String CONST_MUST_CONFORM_TO_PATTERN = "MUST_CONFORM_TO_PATTERN";
	public static final String CONST_INVALID_FILETYPE = "INVALID_FILETYPE";
	public static final int CONST_DEFAULT_CHUNKSIZE = 10000;

	public enum BatchFileType {
		USER,
		CELL,
		AREA,
		TIER,
		SC,
		ACCOUNT,
		GROUP,
		RULE,
		PROM,
		ADJUST,
		INVALID,
		AUDIT,
		TDR,
		DEPT,
		ROLE,
		CELLGROUP,
		MOBRECYCLE
	}

	public enum BatchFileSubType {
		INCLUSIVE,
		EXCLUSIVE
	}

	public static BatchFileType getFileType(String filename) throws Exception
	{
		BatchFileType filetype = BatchFileType.INVALID;
		String[] parts = filename.split("_");
		if (!filename.endsWith(".csv")) throw new Exception(CONST_MUST_BE_CSV_FILE);
		if (parts.length != 5) throw new Exception(CONST_MUST_CONFORM_TO_PATTERN);
		if (!parts[1].equals("ecds")) throw new Exception(CONST_MUST_CONFORM_TO_PATTERN);

		switch (parts[2])
		{
			case BatchUploadRequest.TYPE_USER:
				filetype = BatchFileType.USER;
				break;
			case BatchUploadRequest.TYPE_CELL:
				filetype = BatchFileType.CELL;
				break;
			case BatchUploadRequest.TYPE_CELLGROUP:
				filetype = BatchFileType.CELLGROUP;
				break;
			case BatchUploadRequest.TYPE_AREA:
				filetype = BatchFileType.AREA;
				break;
			case BatchUploadRequest.TYPE_TIER:
				filetype = BatchFileType.TIER;
				break;
			case BatchUploadRequest.TYPE_SC:
				filetype = BatchFileType.SC;
				break;
			case BatchUploadRequest.TYPE_AGENT:
				filetype = BatchFileType.ACCOUNT;
				break;
			case BatchUploadRequest.TYPE_GROUP:
				filetype = BatchFileType.GROUP;
				break;
			case BatchUploadRequest.TYPE_RULE:
				filetype = BatchFileType.RULE;
				break;
			case BatchUploadRequest.TYPE_PROM:
				filetype = BatchFileType.PROM;
				break;
			case BatchUploadRequest.TYPE_ADJUST:
				filetype = BatchFileType.ADJUST;
				break;
			case BatchUploadRequest.TYPE_DEPARTMENT:
				filetype = BatchFileType.DEPT;
				break;
			/*
			Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
			 */
			/*case BatchUploadRequest.TYPE_MOBRECYCLE:
				filetype = BatchFileType.MOBRECYCLE;
				break;	*/

			default:
				throw new Exception(CONST_INVALID_FILETYPE);
		}
		return filetype;
	}

	public static String getFileTypeString(String filename) throws Exception
	{
		String[] parts = filename.split("_");
		if (!filename.endsWith(".csv")) throw new Exception(CONST_MUST_BE_CSV_FILE);
		if (parts.length != 5) throw new Exception(CONST_MUST_CONFORM_TO_PATTERN);
		if (!parts[1].equals("ecds")) throw new Exception(CONST_MUST_CONFORM_TO_PATTERN);

		return parts[2];
	}

	public static String getFilename(String prefix, BatchFileType type, String ext)
	{
		return getFilename(prefix, type.toString(), ext);
	}

	public static String getFilename(String prefix, String type, String ext)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String dateTime = formatter.format(new Date());
		StringBuilder filename = new StringBuilder(prefix);
		filename.append("_ecds_");
		filename.append(type.toLowerCase());
		filename.append("_");
		filename.append(dateTime);
		filename.append(ext);
		return filename.toString();
	}

	public static void setExportHeaders(HttpServletResponse response, String filename)
	{
		setExportHeaders(response, filename, ApplicationConstants.CONST_CONTENT_TYPE_CSV);
	}

	public static void setExportHeaders(HttpServletResponse response, String filename, String contentType)
	{
		StringBuilder attachmentDetails = new StringBuilder("attachment; filename=\"");

		attachmentDetails.append(filename);
		attachmentDetails.append("\"");
		response.setContentType(contentType);
		response.setHeader("Content-Disposition", attachmentDetails.toString());
		response.setHeader("Set-Cookie", "fileDownload=true; path=/");
	}
	public static int getRecordsPerChunk(long total, int defaultChunk)
	{
		int perchunk = 0;
		if (total > defaultChunk)
			perchunk = (int)defaultChunk;
		else
			perchunk = (int)total;
		return perchunk;
	}

	public static int getRecordsPerChunk()
	{
		return CONST_DEFAULT_CHUNKSIZE;
	}
}
