package hxc.services.ecds.rest.batch;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.BatchIssue;
import hxc.ecds.protocol.rest.BatchUploadRequest;
import hxc.ecds.protocol.rest.BatchUploadResponse;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Batch;
import hxc.services.ecds.model.Stage;

public class State
{
	final static Logger logger = LoggerFactory.getLogger(State.class);
	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	///////////////////////////////////
	private static final BigDecimal HUNDRED = new BigDecimal(100);
	
	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	///////////////////////////////////	
	private BatchUploadRequest request;
	private Batch batch;
	private BatchUploadResponse response;
	private File file;
	private Session session;

	private SimpleDateFormat[] dateFormats = new SimpleDateFormat[] { //
			new SimpleDateFormat("yyyyMMdd'T'HHmmss"), //
			new SimpleDateFormat("yyyyMMdd"), //
			new SimpleDateFormat("d MMM yyyy"), //
			new SimpleDateFormat("'T'HHmmss"), //
	};

	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	///////////////////////////////////
	public BatchUploadRequest getRequest()
	{
		return request;
	}

	public State setRequest(BatchUploadRequest request)
	{
		this.request = request;
		return this;
	}

	public Batch getBatch()
	{
		return batch;
	}

	public State setBatch(Batch batch)
	{
		this.batch = batch;
		return this;
	}

	public BatchUploadResponse getResponse()
	{
		return response;
	}

	public State setResponse(BatchUploadResponse response)
	{
		this.response = response;
		return this;
	}

	public File getFile()
	{
		return file;
	}

	public State setFile(File file)
	{
		this.file = file;
		return this;
	}

	public Session getSession()
	{
		return session;
	}

	public State setSession(Session session)
	{
		this.session = session;
		return this;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	///////////////////////////////////

	public BatchUploadResponse addIssue(String returnCode, String fieldName, Object criterium, String additionalInformation)
	{
		return addIssue(returnCode, fieldName, batch == null ? 0 : batch.getLineCount(), criterium, additionalInformation);

	}

	public BatchUploadResponse addIssue(String returnCode, String property, Integer lineNumber, Object criterium, String additionalInformation)
	{
		if (response.wasSuccessful())
		{
			response.setReturnCode(returnCode);
			response.setAdditionalInformation(additionalInformation);
		}

		if (batch != null)
		{
			batch.setFailureCount(batch.getFailureCount() + 1);
		}

		BatchIssue issue = new BatchIssue(lineNumber, returnCode, property, criterium, additionalInformation);

		BatchIssue[] issues = response.getIssues();
		if (issues == null)
			issues = new BatchIssue[1];
		else
			issues = Arrays.copyOf(issues, issues.length + 1);
		issues[issues.length - 1] = issue;
		response.setIssues(issues);
		
		return response;
	}

	public int parseInt(String fieldName, String value)
	{
		try
		{
			return Integer.parseInt(value);
		}
		catch (NumberFormatException ex)
		{
			logger.error("NumberFormatException (1) while converting [{}] value [{}] to int: {}", fieldName, value, ex.getMessage());
			addIssue(BatchIssue.INVALID_VALUE, fieldName, null, value + " is not an int");
			return 0;
		}
	}

	public int parseInt(String fieldName, String value, int defaultValue)
	{
		if (value == null || value.isEmpty())
			return defaultValue;

		try
		{
			return Integer.parseInt(value);
		}
		catch (NumberFormatException ex)
		{
			logger.error("NumberFormatException (2) while converting [{}] value [{}] to int: {}", fieldName, value, ex.getMessage());
			addIssue(BatchIssue.INVALID_VALUE, fieldName, null, value + " is not an int");
			return 0;
		}
	}

	public Integer parseInteger(String fieldName, String value)
	{
		try
		{
			return value == null || value.isEmpty() ? null : Integer.valueOf(value);
		}
		catch (NumberFormatException ex)
		{
			logger.error("NumberFormatException (3) while converting [{}] value [{}] to Integer: {}", fieldName, value, ex.getMessage());
			addIssue(BatchIssue.INVALID_VALUE, fieldName, null, value + " is not an Integer");
			return 0;
		}
	}
	
	public Double parseDouble(String fieldName, String value)
	{
		try
		{
			return value == null || value.isEmpty() ? null : Double.valueOf(value);
		}
		catch (NumberFormatException ex)
		{
			logger.error("NumberFormatException (4) while converting [{}] value [{}] to Double: {}", fieldName, value, ex.getMessage());
			addIssue(BatchIssue.INVALID_VALUE, fieldName, null, value + " is not Double");
			return null;
		}
	}
	
	public BigDecimal parsePercentage(String fieldName, String value)
	{
		BigDecimal number = parseBigDecimal(fieldName, value);
		return number == null ? null : number.divide(HUNDRED);
	}

	public BigDecimal parseBigDecimal(String fieldName, String value)
	{
		try
		{
			return value == null || value.isEmpty() ? null : new BigDecimal(value);
		}
		catch (NumberFormatException ex)
		{
			logger.error("NumberFormatException (5) while converting [{}] value [{}] to BigDecimal: {}", fieldName, value, ex.getMessage());
			addIssue(BatchIssue.INVALID_VALUE, fieldName, null, value + " is not a Decimal");
			return null;
		}
	}

	public Boolean parseBoolean(String fieldName, String value)
	{
		if (value == null || value.isEmpty())
			return null;
		else if ("Y".equalsIgnoreCase(value) || "1".equalsIgnoreCase(value) || "Yes".equalsIgnoreCase(value) || "True".equalsIgnoreCase(value))
			return true;
		else if ("N".equalsIgnoreCase(value) || "0".equalsIgnoreCase(value) || "No".equalsIgnoreCase(value) || "False".equalsIgnoreCase(value))
			return false;

		addIssue(BatchIssue.INVALID_VALUE, fieldName, null, value + " is not a Boolean");
		return false;
	}

	public Date parseDate(String fieldName, String value)
	{
		if (value == null || value.isEmpty())
			return null;

		for (SimpleDateFormat sdf : dateFormats)
		{
			try
			{
				return sdf.parse(value);
			}
			catch (ParseException e)
			{
				logger.error("ParseException while converting [{}] value [{}] to Date: {}", fieldName, value, e.getMessage());
			}
		}

		addIssue(BatchIssue.INVALID_VALUE, fieldName, null, value + " is not a Date/Time");
		return null;
	}

	public int getCompanyID()
	{
		return session.getCompanyID();
	}

	public Stage getStage(int action, int tableID)
	{
		Stage stage = new Stage().setBatchID(getBatch().getId()) //
				.setAction(action) //
				.setTableID(tableID) //
				.setCompanyID(getCompanyID()) //
				.setLastTime(batch.getLastTime()) //
				.setLastUserID(batch.getLastUserID()) //
				.setLineNo(batch.getLineCount());
		return stage;
	}

	public boolean hasIssues()
	{
		return batch.getFailureCount() > 0 || response.getIssues() != null && response.getIssues().length > 0;
	}

	
}
