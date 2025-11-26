package hxc.services.ecds.rest;

import java.nio.charset.Charset;

import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//

@Deprecated
/*
 * Functionality on hold
 */
//@Path("/batch_recycle_msisdn")
public class BatchRecycleMsisdn
{
	final static Logger logger = LoggerFactory.getLogger(BatchRecycleMsisdn.class);

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
	/*@GET
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
			throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}*/

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get
	//
	// /////////////////////////////////
	/*@GET
	@Path("/results/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public MsisdnRecycleResponse getUploadResults(@PathParam("id") int batchID, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		MsisdnRecycleResponse msisdnRecycleResponse = new MsisdnRecycleResponse();
		msisdnRecycleResponse.setReturnCode(ResponseHeader.RETURN_CODE_SUCCESS);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			CompanyInfo companyData = context.findCompanyInfoByID(session.getCompanyID());
			BatchConfig config = companyData.getConfiguration(em, BatchConfig.class);
			File folder = new File(config.getBatchArchiveFolder());
			hxc.services.ecds.model.Batch batch = hxc.services.ecds.model.Batch.findByID(em, batchID, session.getCompanyID());

			if (batch == null) {
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Batch %d not found", batchID);
			}
			File file = new File(folder, batch.getFilename());
			setAgents(msisdnRecycleResponse, session, em, batch);

			return msisdnRecycleResponse;
		}
		catch (RuleCheckException ex)
		{
			msisdnRecycleResponse.setReturnCode(ResponseHeader.RETURN_CODE_UNKNOWN);
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			msisdnRecycleResponse.setReturnCode(ResponseHeader.RETURN_CODE_UNKNOWN);
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}*/

	/**
	 * Copy, paste and adapted part of the batch processor logic.
	 * The funtionality is similar in nature only upto the point that the csv is read
	 * @param request
	 * @return
	 */
	/*@POST
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
			// check that this is type msisdnrecycle and not another kind of file upload
			if (!matcher.find()){
				return state.addIssue(BatchIssue.INVALID_VALUE, "filename", null, filename);
			}

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
				AgentProcessor processor = null;
				switch (request.getType())
				{


					case BatchUploadRequest.TYPE_MOBRECYCLE:
						mayInsert = session.hasPermission(em, Agent.MAY_ADD);
						mayUpdate = session.hasPermission(em, Agent.MAY_UPDATE);
						mayDelete = session.hasPermission(em, Agent.MAY_DELETE);
						processor = new AgentProcessor(context, mayInsert, mayUpdate, mayDelete);
						break;

					default:
						state.addIssue(BatchIssue.INVALID_VALUE, "type", null, request.getType() + " is not valid");
						return response;
				}

				// Initialise the Batch Processor
				if (!processor.initialize(state, batch.getHeadings())) {
					return response;
				}

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
								//processor.preProcess(em, state, rowValues);
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

	}*/

	/**
	 * Submitting agent id's to have these agents msisdn's recycled. The agents are now officially 'deleted' and deactivated.
	 * The mssisdn's can now be re-used
	 *
	 * This transition will not take place unless the agent ahs zero balance. If the agent does not have zero balance,
	 * a violation is added to the response
	 *
	 * @param request
	 * @return
	 */
	/*@POST
	@Path("/submit")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public MsisdnSubmitRecycleResponse submitAgentsToRecycle(MsisdnSubmitRecycleRequest request)
	{

		MsisdnSubmitRecycleResponse response = request.createResponse();
		response.setReturnCode(ResponseHeader.RETURN_CODE_SUCCESS);

		// Validate the Request
		List<MsisdnRecycleIssue> msisdnRecycleIssues = new ArrayList<>();

		List<Integer> succefullyRecycledAgents = new ArrayList<>();
		List<Integer> nonRecycledAgents = new ArrayList<>();

		try (EntityManagerEx em = context.getEntityManager(); EntityManagerEx apEm = this.context.getApEntityManager()) {
			Session session = context.getSession(request.getSessionID());
			for (Integer agentID : request.getAgentIdsToRecycle()) {

				Agent agentWithZeroBalance = Agent.findByIDWithZeroBalance(em, agentID, session.getCompanyID());
				if(agentWithZeroBalance != null){
					agentWithZeroBalance.updateMsisdnToRecycled(em, session);
					succefullyRecycledAgents.add(agentWithZeroBalance.getId());
					OlapAgentAccount.synchronizeState(em, apEm, agentWithZeroBalance.getId());
				} else {
					Agent agent = Agent.findByID(em, agentID, session.getCompanyID());
					if(agent != null){
						nonRecycledAgents.add(agent.getId());
						//the agent with this id does not exist or the agent does not have zero balance
						MsisdnRecycleIssue violation = new MsisdnRecycleIssue(MsisdnRecycleIssue.NON_ZERO_BALANCE, "agentId",
								"Agent with ID: " + agentID + " does not have zero balance. " +
										"In order to delete an agent and recycle Msisdn, the agent must have a zero balance.");
						msisdnRecycleIssues.add(violation);
					} else {
						// agent not found
						MsisdnRecycleIssue violation = new MsisdnRecycleIssue(MsisdnRecycleIssue.AGENT_ID_NOT_FOUND, "agentId",
								"Agent with ID: " + agentID + " does not exist. ");
						msisdnRecycleIssues.add(violation);
					}
				}
			}
		} catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		} catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
		}

		response.setIssues(msisdnRecycleIssues);
		response.setNonRecyclableAgents(nonRecycledAgents);
		response.setAgentsRecycled(succefullyRecycledAgents);
		return response;
	}*/

	/**
	 *
	 * @param processor
	 * @param em
	 * @param state
	 * @param rowValues
	 * @return agent account info objec that holds agent and account info. If no id or msisdn is matched the  agent and account
	 * 			members will be null
	 */
	/*protected AgentAccountInfo loadAgentAccountInfo(Processor processor, EntityManager em, State state, String[] rowValues)
	{
		AgentAccountInfo agentAccountInfo = null;
		boolean searchedById = false;
		// Load By ID
		Integer idColumnIndex = processor.columnIndexForProperty("id");
		if (idColumnIndex != null)
		{
			int id = state.parseInt("id", rowValues[idColumnIndex], 0);
			if(id != 0){
				searchedById = true;
			}
			Map<String, Object> resultsMap = Agent.findByIDWithAccountInfo(em, id, state.getCompanyID());
			if (resultsMap != null){
				agentAccountInfo = populateAgentAccountInfo(resultsMap);
			}
		}

		//only load by mobile number if direct ID is not present, favour loading by ID
		// Load by Mobile number
		Integer mobileNumberColumnIndex = processor.columnIndexForProperty("mobileNumber");
		if (!searchedById && mobileNumberColumnIndex != null)
		{
			String msisdn = context.toMSISDN(rowValues[mobileNumberColumnIndex]);
			Map<String, Object> resultsMap = Agent.findByMSISDNWithAccountInfo(em, msisdn, state.getCompanyID());
			if (resultsMap != null){
				agentAccountInfo = populateAgentAccountInfo(resultsMap);
			}
		}

		//no match found, so populate with original values
		if(agentAccountInfo == null){
			agentAccountInfo = populateAgentAccountInfo(processor, rowValues);
		}

		return agentAccountInfo;
	}*/

	/*private AgentAccountInfo populateAgentAccountInfo(Map<String, Object> resultsMap) {
		Agent agent = (Agent) resultsMap.get("agent");
		Account account = (Account) resultsMap.get("account");
		AgentAccountInfo agentAccountInfo = new AgentAccountInfo();
		agentAccountInfo.setAccountNumber(agent.getAccountNumber());
		agentAccountInfo.setAgentId(agent.getId());
		agentAccountInfo.setBalance(account.getBalance());
		agentAccountInfo.setBonusBalance(account.getBonusBalance());
		agentAccountInfo.setOnHoldBalance(account.getOnHoldBalance());
		agentAccountInfo.setOnHoldBonusProvision(account.getOnHoldBonusProvision());
		agentAccountInfo.setFirstName(agent.getFirstName());
		agentAccountInfo.setLastName(agent.getSurname());
		agentAccountInfo.setMobileNumber(agent.getMobileNumber());
		agentAccountInfo.setMsisdnRecycled(agent.getMsisdnRecycled());
		agentAccountInfo.setState(agent.getState());
		agentAccountInfo.setMatched(true);
		return agentAccountInfo;
	}

	private AgentAccountInfo populateAgentAccountInfo(Processor processor, String[] rowValues) {

		AgentAccountInfo agentAccountInfo = new AgentAccountInfo();
		if(getImportColumnValue(processor, rowValues, "accountNumber") != null){
			agentAccountInfo.setAccountNumber(getImportColumnValue(processor, rowValues, "accountNumber"));
		}
		if(getImportColumnValue(processor, rowValues, "id") != null){
			agentAccountInfo.setAgentId(Integer.valueOf(getImportColumnValue(processor, rowValues, "id")));
		}
		if(getImportColumnValue(processor, rowValues, "mobileNumber") != null){
			agentAccountInfo.setMobileNumber(getImportColumnValue(processor, rowValues, "mobileNumber"));
		}
		if(getImportColumnValue(processor, rowValues, "firstName") != null){
			agentAccountInfo.setFirstName(getImportColumnValue(processor, rowValues, "firstName"));
		}
		if(getImportColumnValue(processor, rowValues, "surname") != null){
			agentAccountInfo.setLastName(getImportColumnValue(processor, rowValues, "surname"));
		}
		agentAccountInfo.setMatched(false);
		return agentAccountInfo;
	}


	public void setAgents(MsisdnRecycleResponse msisdnRecycleResponse, Session session , EntityManagerEx em, hxc.services.ecds.model.Batch batch ) throws IOException, RuleCheckException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {

		List<AgentAccountInfo> agentAccountInfoList = new ArrayList<>();
		int first = 0;
		int max = 32768;

		// Get the Corresponding Encrypted File
		CompanyInfo companyData = context.findCompanyInfoByID(session.getCompanyID());
		BatchConfig config = companyData.getConfiguration(em, BatchConfig.class);
		File folder = new File(config.getBatchArchiveFolder());
		File file = new File(folder, batch.getFilename());
		if (!file.exists())
			throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Batch File '%s' not found", file.getAbsolutePath());
		StringBuilder sb = new StringBuilder();
		SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);

		Processor processor = new AgentProcessor(context, false, false, false);
		State state = new State();
		state.setSession(session);

		boolean headersInitialised = false;

		try (FileInputStream fis = new FileInputStream(file))
		{
			try (CipherInputStream cs = new CipherInputStream(fis, cipher))
			{
				try (InputStreamReader sr = new InputStreamReader(cs, "UTF-8"))
				{
					try (BufferedReader br = new BufferedReader(sr))
					{
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
								if(!headersInitialised){
									processor.initialize(state, line);
									headersInitialised = true;
								}
								sb.append(line);
								if (!line.endsWith("\n"))
									sb.append('\n');
								if (--max <= 0)
									break;
							}
						}
					}
				}
			}
		}

		CsvParserSettings settings = new CsvParserSettings();
		settings.getFormat().setLineSeparator("\n");
		CsvParser parser = new CsvParser(settings);
		try (Reader reader = new StringReader(sb.toString())) {
			parser.beginParsing(reader);
			int rowNumber = 0;
			String[] rowValues = null;
			while ((rowValues = parser.parseNext()) != null) {
				if(rowNumber++ > 0) {
					AgentAccountInfo agentAccountInfo = loadAgentAccountInfo(processor, em, state, rowValues);
					if(agentAccountInfo.isMatched()){
						//check to see if the agent has zero balances
						if(agentAccountInfo.getBalance().compareTo(BigDecimal.ZERO) == 0
								&&  agentAccountInfo.getOnHoldBalance().compareTo(BigDecimal.ZERO) == 0
								&&  agentAccountInfo.getBonusBalance().compareTo(BigDecimal.ZERO) == 0
								&& !agentAccountInfo.isMsisdnRecycled()){
							agentAccountInfo.setRecyclable(true);
						}
					}
					agentAccountInfoList.add(agentAccountInfo);
				}
			}

			msisdnRecycleResponse.setAgentAccountInfoList(agentAccountInfoList);

		}
	}


	private String  getImportColumnValue(Processor processor, String[] rowValues, String columnName){
		Integer columnNameIndex = processor.columnIndexForProperty(columnName);

		if(columnNameIndex != null){
			return rowValues[columnNameIndex];
		}

		return null;
	}*/

}
