package hxc.services.ecds.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import hxc.ecds.protocol.rest.BatchIssue;
import hxc.ecds.protocol.rest.BatchUploadRequest;
import hxc.ecds.protocol.rest.BatchUploadResponse;
import hxc.ecds.protocol.rest.ResponseHeader;
import hxc.ecds.protocol.rest.Violation;
import hxc.ecds.protocol.rest.config.BatchConfig;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.Area;
import hxc.services.ecds.model.Cell;
import hxc.services.ecds.model.CellGroup;
import hxc.services.ecds.model.Department;
import hxc.services.ecds.model.Group;
import hxc.services.ecds.model.Promotion;
import hxc.services.ecds.model.ServiceClass;
import hxc.services.ecds.model.Stage;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransferRule;
import hxc.services.ecds.model.WebUser;
import hxc.services.ecds.rest.batch.AdjustmentProcessor;
import hxc.services.ecds.rest.batch.AgentProcessor;
import hxc.services.ecds.rest.batch.AreaProcessor;
import hxc.services.ecds.rest.batch.CellGroupProcessor;
import hxc.services.ecds.rest.batch.CellProcessor;
import hxc.services.ecds.rest.batch.CsvExportProcessor;
import hxc.services.ecds.rest.batch.DepartmentProcessor;
import hxc.services.ecds.rest.batch.GroupProcessor;
import hxc.services.ecds.rest.batch.Processor;
import hxc.services.ecds.rest.batch.PromotionProcessor;
import hxc.services.ecds.rest.batch.ServiceClassProcessor;
import hxc.services.ecds.rest.batch.State;
import hxc.services.ecds.rest.batch.TierProcessor;
import hxc.services.ecds.rest.batch.TransferRuleProcessor;
import hxc.services.ecds.rest.batch.WebUserProcessor;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.QueryToken;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

@Path("/batch")
public class Batch
{
	final static Logger logger = LoggerFactory.getLogger(Batch.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Context
	//
	// /////////////////////////////////
	@Context
	private ICreditDistribution context;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final byte[] key = new byte[] { 71, 111, 32, 83, 111, 108, 97, 114, 32, 111, 114, 32, 100, 105, 101, 32 };
	private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
	private static final int BATCH_SESSION_EXTENSION_MINS = 1440;

	private static final String[] HEADINGS = new String[] { //
			"id", //
			"filename", //
			"completed", //
			"line_count", //
			"insert_count", //
			"update_count", //
			"delete_count", //
			"failure_count", //
			"total_value", //
			"type", //
			"fileSize", //
			"timestamp", //
			"web_user_id", //
			"co_auth_user_id", //
			"ip_address", //
			"mac_address", //
			"machine_name", //
			"domain_name", //
			"tamper", //
			"state", //
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get
	//
	// /////////////////////////////////
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Batch getBatch(@PathParam("id") int batchID, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			hxc.services.ecds.model.Batch batch = hxc.services.ecds.model.Batch.findByID(em, batchID, session.getCompanyID());
			if (batch == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Batch %d not found", batchID);
			return batch;
		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Batch[] getBatches( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@DefaultValue("0") @QueryParam(RestParams.FIRST) int first, //
			@DefaultValue("-1") @QueryParam(RestParams.MAX) int max, //
			@QueryParam(RestParams.SORT) String sort, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter //
	)
	{
		RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				List<hxc.services.ecds.model.Batch> batches = hxc.services.ecds.model.Batch.findAll(em, params, session.getCompanyID());
				return batches.toArray(new hxc.services.ecds.model.Batch[batches.size()]);
			}
		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("*")
	@Produces(MediaType.APPLICATION_JSON)
	public Long getBatchCount( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter //
	)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID, 0, -1, null, search, filter);
			Session session = context.getSession(params.getSessionID());
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				return hxc.services.ecds.model.Batch.findCount(em, params, session.getCompanyID());
			}
		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/csv")
	@Produces("text/csv")
	public String getBatchCsv( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@DefaultValue("0") @QueryParam(RestParams.FIRST) int first, //
			@DefaultValue("-1") @QueryParam(RestParams.MAX) int max, //
			@QueryParam(RestParams.SORT) String sort, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter //
	)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
			Session session = context.getSession(params.getSessionID());
			List<hxc.services.ecds.model.Batch> serviceClasses;
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				serviceClasses = hxc.services.ecds.model.Batch.findAll(em, params, session.getCompanyID());
			}

			CsvExportProcessor<hxc.services.ecds.model.Batch> processor = new CsvExportProcessor<hxc.services.ecds.model.Batch>(HEADINGS, first)
			{
				@Override
				protected void write(hxc.services.ecds.model.Batch record)
				{
					put("id", record.getId());
					put("filename", record.getFilename());
					put("completed", record.isCompleted());
					put("line_count", record.getLineCount());
					put("insert_count", record.getInsertCount());
					put("update_count", record.getUpdateCount());
					put("delete_count", record.getDeleteCount());
					put("failure_count", record.getFailureCount());
					put("total_value", record.getTotalValue());
					put("type", record.getType());
					put("fileSize", record.getFileSize());
					put("timestamp", record.getTimestamp());
					put("web_user_id", record.getWebUserID());
					put("co_auth_user_id", record.getCoAuthWebUserID());
					put("ip_address", record.getIpAddress());
					put("mac_address", record.getMacAddress());
					put("machine_name", record.getMachineName());
					put("domain_name", record.getDomainName());
					put("tamper", record.isTamperedWith());
					put("state", record.getState());
				}
			};
			return processor.add(serviceClasses);
		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("historic/csv/{id}")
	@Produces("text/csv")
	public String getHistoricCsv( //
			@PathParam("id") int batchID, //
			@HeaderParam(RestParams.SID) String sessionID, //
			@DefaultValue("0") @QueryParam(RestParams.FIRST) int first, //
			@DefaultValue("32768") @QueryParam(RestParams.MAX) int max, //
			@QueryParam(RestParams.SORT) String sort, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter //
	)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
			Session session = context.getSession(params.getSessionID());

			// Get the Corresponding Batch
			hxc.services.ecds.model.Batch batch = hxc.services.ecds.model.Batch.findByID(em, batchID, session.getCompanyID());
			if (batch == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Batch %d not found", batchID);

			// Get the Corresponding Encrypted File
			CompanyInfo companyData = context.findCompanyInfoByID(session.getCompanyID());
			BatchConfig config = companyData.getConfiguration(em, BatchConfig.class);
			File folder = new File(config.getBatchArchiveFolder());
			File file = new File(folder, batch.getFilename());
			if (!file.exists())
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Batch File '%s' not found", file.getAbsolutePath());

			SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			try (FileInputStream fis = new FileInputStream(file))
			{
				try (CipherInputStream cs = new CipherInputStream(fis, cipher))
				{
					try (InputStreamReader sr = new InputStreamReader(cs, "UTF-8"))
					{
						try (BufferedReader br = new BufferedReader(sr))
						{
							StringBuilder sb = new StringBuilder();
							while (true)
							{
								String line = br.readLine();

								if (line == null || line.length() == 0 || line.charAt(0) == 0)
									break;

								if (!br.ready())
								{
									byte[] residual = batch.getResidualCryptoBytes();
									if (residual != null && residual.length > 0)
									{
										String text = new String(residual, UTF8_CHARSET);
										line += text;
									}
								}

								if (first-- <= 0)
								{
									sb.append(line);
									if (!line.endsWith("\n"))
										sb.append('\n');
									if (--max <= 0)
										break;
								}
							}
							return sb.toString();
						}
					}
				}
			}

		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Upload
	//
	// /////////////////////////////////
	@POST
	@Path("/upload")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public BatchUploadResponse upload(BatchUploadRequest request)
	{
		// Create a Response
		State state = new State();
		state.setRequest(request);
		BatchUploadResponse response = request.createResponse();
		response.setReturnCode(ResponseHeader.RETURN_CODE_SUCCESS);
		state.setResponse(response);
		boolean last = false;
		if (request.isLast())
			last = true;

		try
		{
			// Get Session
			Session session = context.getSession(request.getSessionID());
			state.setSession(session);
			session.extend(BATCH_SESSION_EXTENSION_MINS);

			// Get CoSession
			Session coSession = null;
			if (BatchUploadRequest.TYPE_ADJUST.equals(request.getType()) && last)
			{
				String sessionID = request.getCoSignatorySessionID();
				coSession = sessionID == null || sessionID.isEmpty() ? null : context.getSession(sessionID);
				if (coSession == null || coSession.getCompanyID() != session.getCompanyID())
					throw new RuleCheckException(TransactionsConfig.ERR_CO_AUTHORIZE, "coSignatorySessionID", "Co-Authorization Required");
				else if (coSession.getWebUserID() == session.getWebUserID())
					throw new RuleCheckException(TransactionsConfig.ERR_CO_AUTHORIZE, "coSignatorySessionID", "Cannot be same Web-User");

				// Validate coSignable
				coSession.validateCoSignable(context, request, session.getSessionID());
			}

			// Verify Filename
			CompanyInfo companyData = context.findCompanyInfoByID(session.getCompanyID());
			String expectedFilename = String.format("%s_ecds_%s_(\\d{8}_\\d{6})\\.csv", //
					companyData.getCompany().getPrefix(), //
					request.getType());
			Pattern pattern = Pattern.compile(expectedFilename, Pattern.CASE_INSENSITIVE);
			String filename = request.getFilename();
			Matcher matcher = pattern.matcher(filename);
			if (!matcher.find())
				return state.addIssue(BatchIssue.INVALID_VALUE, "filename", null, filename);

			// Get Corresponding Batch Record Entry
			try (EntityManagerEx em = context.getEntityManager())
			{

				// Permission Check
				session.check(em, hxc.services.ecds.model.Batch.MAY_UPLOAD_CSV);

				// Try to find existing Batch record
				hxc.services.ecds.model.Batch batch = hxc.services.ecds.model.Batch.findByFilename(em, session.getCompanyID(), filename);

				// Create new Entry
				if (batch == null)
				{
					batch = new hxc.services.ecds.model.Batch();
					batch.setCompanyID(session.getCompanyID());
					batch.setFilename(filename);
					batch.setType(request.getType());
					batch.setFileSize(null);
					batch.setTimestamp(new Date());
					batch.setWebUserID(session.getWebUserID());
					if (coSession != null)
						batch.setCoAuthWebUserID(coSession.getWebUserID());
					batch.setIpAddress(session.getIpAddress());
					batch.setMacAddress(session.getMacAddress());
					batch.setMachineName(session.getMachineName());
					batch.setDomainName(session.getDomainAccountName());
					batch.setResidualCryptoBytes(null);
					batch.setCompleted(false);
					batch.setNextExpectedOffset(0L);
					batch.setLineCount(0);
					batch.setHeadings(null);
					batch.setState(request.getState() != null ? request.getState() : hxc.services.ecds.model.Batch.STATE_UPLOADING);

					try (RequiresTransaction ts = new RequiresTransaction(em))
					{
						AuditEntryContext auditContext = new AuditEntryContext("BATCH_UPLOAD_STARTED", batch.getFilename(), batch.getId());
						batch.persist(em, null, session, auditContext);
						ts.commit();
					}
				}
				batch.setCoAuthWebUserID(coSession != null ? coSession.getWebUserID() : null);
				state.setBatch(batch);
				response.setBatchID(batch.getId());

				// Validate the Request
				List<Violation> violations = request.validate();
				if (violations != null && !violations.isEmpty())
				{
					for (Violation violation : violations)
					{
						state.addIssue(violation.getReturnCode(), violation.getProperty(), violation.getCriterium(), violation.getAdditionalInformation());
					}
					return response;
				}

				// Test if already completed
				if (batch.isCompleted())
				{
					return state.addIssue(BatchIssue.ALREADY_PROCESSED, null, request.getFilename(), "File already processed");
				}

				// Test if this segment offset is expected
				String content = request.getContent() == null ? "" : request.getContent();
				if (content.length() == 0)
					request.setCharacterOffset(batch.getNextExpectedOffset());
				boolean validOffset = request.getCharacterOffset() == batch.getNextExpectedOffset() //
						|| !batch.isCompleted() && request.getCharacterOffset() == 0L;
				if (!validOffset)
					return state.addIssue(BatchIssue.INVALID_VALUE, "characterOffset", batch.getNextExpectedOffset(), //
							String.format("Expected: %d but was %d", batch.getNextExpectedOffset(), request.getCharacterOffset()));

				// Is this a restart?
				if (request.getCharacterOffset() == 0L)
				{
					batch.setLineCount(0);
					batch.setFileSize(0L);
					batch.setInsertCount(0);
					batch.setDeleteCount(0);
					batch.setUpdateCount(0);
					batch.setFailureCount(0);
					batch.setTotalValue(BigDecimal.ZERO);
					batch.setTotalValue2(BigDecimal.ZERO);
					batch.setState(request.getState() != null ? request.getState() : hxc.services.ecds.model.Batch.STATE_UPLOADING);
					batch.setResidualCryptoBytes(new byte[0]);
					batch.setResidualText("");
					Stage.purge(em, batch.getId());
				}

				// Encrypt the Data
				SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
				Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
				cipher.init(Cipher.ENCRYPT_MODE, secretKey);
				byte[] residualCryptoBytes = batch.getResidualCryptoBytes() == null ? new byte[0] : batch.getResidualCryptoBytes();
				int residualCount = residualCryptoBytes.length;
				byte[] segmentData = content.getBytes(UTF8_CHARSET);
				int segmentLength = segmentData.length;
				batch.setFileSize(batch.getFileSize() + segmentLength);
				int inputCount = residualCount + segmentLength;
				boolean firstSegment = request.getCharacterOffset() == 0L;
				boolean finalSegment = request.isLast();
				int blockSize = cipher.getBlockSize();
				if (finalSegment)
					inputCount += blockSize - (inputCount % blockSize); // Pad
				byte[] input = new byte[inputCount];
				if (residualCount > 0)
					System.arraycopy(residualCryptoBytes, 0, input, 0, residualCount);
				System.arraycopy(segmentData, 0, input, residualCount, segmentLength);
				int usableCount = inputCount - (inputCount % blockSize);
				byte[] output = cipher.update(input, 0, usableCount);
				if (output == null)
					output = new byte[0];
				residualCryptoBytes = Arrays.copyOfRange(input, usableCount, inputCount);
				batch.setResidualCryptoBytes(residualCryptoBytes);
				batch.setCompleted(finalSegment);
				batch.setNextExpectedOffset(request.getCharacterOffset() + content.length());
				response.setNextExpectedCharacterOffset(batch.getNextExpectedOffset());

				// Append to encrypted file
				BatchConfig config = companyData.getConfiguration(em, BatchConfig.class);
				File folder = new File(config.getBatchArchiveFolder());
				if (!folder.exists())
					folder.mkdirs();
				File file = new File(folder, filename);
				state.setFile(file);
				try (FileOutputStream fos = new FileOutputStream(file, !firstSegment))
				{
					fos.write(output);
				}

				// Choose batch processor
				boolean mayInsert = false;
				boolean mayUpdate = false;
				boolean mayDelete = false;
				Processor<?> processor = null;
				switch (request.getType())
				{
					case BatchUploadRequest.TYPE_SC:
						mayInsert = session.hasPermission(em, ServiceClass.MAY_ADD);
						mayUpdate = session.hasPermission(em, ServiceClass.MAY_UPDATE);
						mayDelete = session.hasPermission(em, ServiceClass.MAY_DELETE);
						processor = new ServiceClassProcessor(context, mayInsert, mayUpdate, mayDelete);
						break;

					case BatchUploadRequest.TYPE_GROUP:
						mayInsert = session.hasPermission(em, Group.MAY_ADD);
						mayUpdate = session.hasPermission(em, Group.MAY_UPDATE);
						mayDelete = session.hasPermission(em, Group.MAY_DELETE);
						processor = new GroupProcessor(context, mayInsert, mayUpdate, mayDelete);
						break;

					case BatchUploadRequest.TYPE_TIER:
						mayInsert = session.hasPermission(em, Tier.MAY_ADD);
						mayUpdate = session.hasPermission(em, Tier.MAY_UPDATE);
						mayDelete = session.hasPermission(em, Tier.MAY_DELETE);
						processor = new TierProcessor(context, mayInsert, mayUpdate, mayDelete);
						break;

					case BatchUploadRequest.TYPE_AGENT:
						mayInsert = session.hasPermission(em, Agent.MAY_ADD);
						mayUpdate = session.hasPermission(em, Agent.MAY_UPDATE);
						mayDelete = session.hasPermission(em, Agent.MAY_DELETE);
						processor = new AgentProcessor(context, mayInsert, mayUpdate, mayDelete);
						break;

					case BatchUploadRequest.TYPE_USER:
						mayInsert = session.hasPermission(em, WebUser.MAY_ADD);
						mayUpdate = session.hasPermission(em, WebUser.MAY_UPDATE);
						mayDelete = session.hasPermission(em, WebUser.MAY_DELETE);
						processor = new WebUserProcessor(context, mayInsert, mayUpdate, mayDelete);
						break;

					case BatchUploadRequest.TYPE_ADJUST:
						mayUpdate = session.hasPermission(em, Transaction.MAY_ADJUST, true) && (coSession == null || coSession.hasPermission(em, Transaction.MAY_AUTHORISE_ADJUST, true));
						AdjustmentProcessor adjustmentProcessor = new AdjustmentProcessor(context, mayInsert, mayUpdate, mayDelete);
						processor = adjustmentProcessor.setCoSession(coSession);
						break;

					case BatchUploadRequest.TYPE_RULE:
						mayInsert = session.hasPermission(em, TransferRule.MAY_ADD);
						mayUpdate = session.hasPermission(em, TransferRule.MAY_UPDATE);
						mayDelete = session.hasPermission(em, TransferRule.MAY_DELETE);
						processor = new TransferRuleProcessor(context, mayInsert, mayUpdate, mayDelete);
						break;

					case BatchUploadRequest.TYPE_DEPARTMENT:
						mayInsert = session.hasPermission(em, Department.MAY_ADD);
						mayUpdate = session.hasPermission(em, Department.MAY_UPDATE);
						mayDelete = session.hasPermission(em, Department.MAY_DELETE);
						processor = new DepartmentProcessor(context, mayInsert, mayUpdate, mayDelete);
						break;

					case BatchUploadRequest.TYPE_AREA:
						mayInsert = session.hasPermission(em, Area.MAY_ADD);
						mayUpdate = session.hasPermission(em, Area.MAY_UPDATE);
						mayDelete = session.hasPermission(em, Area.MAY_DELETE);
						processor = new AreaProcessor(context, mayInsert, mayUpdate, mayDelete);
						break;

					case BatchUploadRequest.TYPE_CELL:
						mayInsert = session.hasPermission(em, Cell.MAY_ADD);
						mayUpdate = session.hasPermission(em, Cell.MAY_UPDATE);
						mayDelete = session.hasPermission(em, Cell.MAY_DELETE);
						processor = new CellProcessor(context, mayInsert, mayUpdate, mayDelete);
						break;

					case BatchUploadRequest.TYPE_CELLGROUP:
						mayInsert = session.hasPermission(em, CellGroup.MAY_ADD);
						mayUpdate = session.hasPermission(em, CellGroup.MAY_UPDATE);
						mayDelete = session.hasPermission(em, CellGroup.MAY_DELETE);
						processor = new CellGroupProcessor(context, mayInsert, mayUpdate, mayDelete);
						break;

					case BatchUploadRequest.TYPE_PROMOTION:
						mayInsert = session.hasPermission(em, Promotion.MAY_ADD);
						mayUpdate = session.hasPermission(em, Promotion.MAY_UPDATE);
						mayDelete = session.hasPermission(em, Promotion.MAY_DELETE);
						processor = new PromotionProcessor(context, mayInsert, mayUpdate, mayDelete);
						break;

					default:
						state.addIssue(BatchIssue.INVALID_VALUE, "type", null, request.getType() + " is not valid");
						return response;
				}

				// Initialise the Batch Processor
				if (!processor.initialize(state, batch.getHeadings()))
					return response;

				// Parse the incoming lines
				String residualText = batch.getResidualText();
				if (residualText != null && !residualText.isEmpty())
					content = residualText + content;
				int lastNewLine = content.lastIndexOf('\n');
				if (lastNewLine < 0)
				{
					batch.setResidualText(content);
					content = "";
				}
				else
				{
					batch.setResidualText(content.substring(lastNewLine + 1));
					content = content.substring(0, lastNewLine);
				}

				// Use existing state
				String newState = batch.getState();

				// Use state from request if it is present
				if (request.getState() != null)
					newState = request.getState();

				// Override if there are issues
				if (state.hasIssues())
					newState = hxc.services.ecds.model.Batch.STATE_FAILED;

				// Override if processing is to begin
				else if (request.isLast())
					newState = hxc.services.ecds.model.Batch.STATE_PROCESSING;

				// Update state if required
				if (!newState.equals(batch.getState()))
				{
					try (RequiresTransaction ts = new RequiresTransaction(em))
					{
						batch.setState(newState);
						em.persist(batch);
						ts.commit();
					}
				}

				boolean stillOK = true;
				try (RequiresTransaction ts = new RequiresTransaction(em))
				{
					CsvParserSettings settings = new CsvParserSettings();
					settings.getFormat().setLineSeparator("\n");
					CsvParser parser = new CsvParser(settings);
					try (Reader reader = new StringReader(content))
					{
						parser.beginParsing(reader);

						String[] rowValues = null;
						while ((rowValues = parser.parseNext()) != null)
						{
							boolean firstRow = batch.getLineCount() == 0;
							batch.setLineCount(batch.getLineCount() + 1);

							if (firstRow)
							{
								StringBuilder sb = new StringBuilder();
								for (String value : rowValues)
									sb.append(value).append(',');
								sb.deleteCharAt(sb.length() - 1);
								batch.setHeadings(sb.toString());
								if (!processor.initialize(state, batch.getHeadings()))
									return response;
							}
							else
							{
								processor.preProcess(em, state, rowValues);
							}
						}

					}

					// Complete
					stillOK = true;
					if (request.isLast())
					{
						if (state.hasIssues())
							stillOK = false;
						else
							stillOK = processor.onComplete(em, state);
						batch.setCompleted(stillOK);
					}

					// Persist batch info
					if (stillOK)
					{
						AuditEntryContext auditContext = new AuditEntryContext("BATCH_UPLOAD_COMPLETE", batch.getFilename(), batch.getId());
						batch.persist(em, null, session, auditContext);
						ts.commit();
					}
				}

				// Update Final State
				newState = batch.getState();
				if (!stillOK || batch.getFailureCount() > 0)
					newState = hxc.services.ecds.model.Batch.STATE_FAILED;
				else if (batch.isCompleted())
					newState = hxc.services.ecds.model.Batch.STATE_COMPLETED;
				if (!newState.equals(batch.getState()))
				{
					try (RequiresTransaction ts = new RequiresTransaction(em))
					{
						if (!em.contains(batch))
							batch = hxc.services.ecds.model.Batch.findByID(em, batch.getId(), state.getCompanyID());
						batch.setState(newState);
						em.persist(batch);
						ts.commit();
					}
				}

				// Send Notifications and purge the Staging Table
				if (batch.isCompleted())
				{
					processor.sendNotifications(em, state);
					Stage.purge(em, batch.getId());
				}

			}
		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			response.setReturnCode(ex.getError());
			response.setAdditionalInformation(ex.getMessage());
		}
		catch (Throwable tr)
		{
			logger.error(tr.getMessage(), tr);
			response.setReturnCode(TransactionsConfig.ERR_TECHNICAL_PROBLEM);
			response.setAdditionalInformation(tr.getMessage());
		}

		return response;

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////
	@GET
	@Path("/config")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.config.BatchConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			return context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, hxc.ecds.protocol.rest.config.BatchConfig.class);
		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@PUT
	@Path("/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(hxc.ecds.protocol.rest.config.BatchConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_CONFIG_BATCH);
			context.findCompanyInfoByID(session.getCompanyID()).setConfiguration(em, configuration, session);

		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////

}
