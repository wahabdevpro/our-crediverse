package hxc.ecds.protocol.rest;

import java.util.List;

// REST End-Point: ~/batch/upload
public class BatchUploadRequest extends RequestHeader implements ICoSignable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String TYPE_USER = "user";
	public static final String TYPE_CELL = "cell";
	public static final String TYPE_CELLGROUP = "cellgroup";
	public static final String TYPE_AREA = "area";
	public static final String TYPE_TIER = "tier";
	public static final String TYPE_SC = "sc";
	public static final String TYPE_AGENT = "account";
	public static final String TYPE_GROUP = "group";
	public static final String TYPE_RULE = "rule";
	public static final String TYPE_PROM = "prom";
	public static final String TYPE_ADJUST = "adjust";
	public static final String TYPE_DEPARTMENT = "dept";
	public static final String TYPE_PROMOTION = "prom";
	public static final String TYPE_MOBRECYCLE = "mobrecycle";

	public static final int CO_SIGNATORY_SESSION_ID_MAX_LENGTH = CoSignableUtils.CO_SIGNATORY_SESSION_ID_MAX_LENGTH;
	public static final int CO_SIGNATORY_TRANSACTION_ID_MAX_LENGTH = CoSignableUtils.CO_SIGNATORY_TRANSACTION_ID_MAX_LENGTH;
	public static final int CO_SIGNATORY_OTP_MAX_LENGTH = CoSignableUtils.CO_SIGNATORY_OTP_MAX_LENGTH;

	public static final int FILENAME_MAX_LENGTH = 100;
	public static final long SEGMENT_MAX_LENGTH = 16 * 1024;

	public static final String VERB_ADD = "add";
	public static final String VERB_UPDATE = "update";
	public static final String VERB_DELETE = "delete";
	public static final String VERB_VERIFY = "verify";
	public static final String VERB_UPSERT = "upsert";
	
	// States inferable by TS
	public static final String STATE_PROCESSING = Batch.STATE_PROCESSING;
	public static final String STATE_COMPLETED = Batch.STATE_COMPLETED;
	public static final String STATE_FAILED = Batch.STATE_FAILED;
	
	// Other states not inferable by TS which can be set via BatchUploadRequest
	public static final String STATE_UPLOADING = Batch.STATE_UPLOADING;
	public static final String STATE_CANCELLED = Batch.STATE_CANCELLED;
	public static final String STATE_DECLINED = Batch.STATE_DECLINED;
	public static final String STATE_AUTHORIZING = Batch.STATE_AUTHORIZING;
	public static final String STATE_SUSPENDED = Batch.STATE_SUSPENDED;
	public static final String STATE_UNKNOWN = Batch.STATE_UNKNOWN;
	public static final String STATE_TESTING = Batch.STATE_TESTING;
	public static final String STATE_NO_CHANGE = null;
	// etc

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String coSignatorySessionID; // Adjustments only
	private String coSignatoryTransactionID;
	private String coSignatoryOTP;
	private String type;
	private String filename; // Without path, e.g. xx_ecds_{type}_yyyymmdd_hhmmss.csv
	private long characterOffset; // 0 for first text
	private String content;
	private boolean last; // False if more lines are to follow
	private String state = STATE_NO_CHANGE;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public String getCoSignatorySessionID()
	{
		return coSignatorySessionID;
	}

	@Override
	public BatchUploadRequest setCoSignatorySessionID(String coSignatorySessionID)
	{
		this.coSignatorySessionID = coSignatorySessionID;
		return this;
	}

	@Override
	public String getCoSignatoryTransactionID()
	{
		return this.coSignatoryTransactionID;
	}

	@Override
	public BatchUploadRequest setCoSignatoryTransactionID(String coSignatoryTransactionID)
	{
		this.coSignatoryTransactionID = coSignatoryTransactionID;
		return this;
	}

	@Override
	public String getCoSignatoryOTP()
	{
		return this.coSignatoryOTP;
	}

	@Override
	public BatchUploadRequest setCoSignatoryOTP(String coSignatoryOTP)
	{
		this.coSignatoryOTP = coSignatoryOTP;
		return this;
	}

	public String getType()
	{
		return type;
	}

	public BatchUploadRequest setType(String type)
	{
		this.type = type;
		return this;
	}

	public String getFilename()
	{
		return filename;
	}

	public BatchUploadRequest setFilename(String filename)
	{
		this.filename = filename;
		return this;
	}

	public long getCharacterOffset()
	{
		return characterOffset;
	}

	public BatchUploadRequest setCharacterOffset(long characterOffset)
	{
		this.characterOffset = characterOffset;
		return this;
	}

	public String getContent()
	{
		return content;
	}

	public BatchUploadRequest setContent(String content)
	{
		this.content = content;
		return this;
	}

	public boolean isLast()
	{
		return last;
	}

	public BatchUploadRequest setLast(boolean last)
	{
		this.last = last;
		return this;
	}
	
	public String getState()
	{
		return state;
	}

	public BatchUploadRequest setState(String state)
	{
		this.state = state;
		return this;
	}
	

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////



	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IValidatable
	//
	// /////////////////////////////////
	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator() //
				.oneOf("type", type, TYPE_USER, TYPE_CELL, TYPE_CELLGROUP, TYPE_AREA, TYPE_TIER, TYPE_SC, //
						TYPE_AGENT, TYPE_GROUP, TYPE_RULE, TYPE_PROM, TYPE_ADJUST, TYPE_DEPARTMENT, TYPE_MOBRECYCLE) //
				.notLonger("state", state, 1) //
				.notLess("characterOffset", characterOffset, 0L);

		if (TYPE_ADJUST.equals(type) && last)
		{
			validator = CoSignableUtils.validate(validator, this, true);
		}
		return validator.toList();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// RequestHeader
	//
	// /////////////////////////////////
	@SuppressWarnings("unchecked")
	@Override
	public BatchUploadResponse createResponse()
	{
		return new BatchUploadResponse(this);
	}
}
