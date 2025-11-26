package hxc.services.ecds.rest.batch;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.BatchIssue;
import hxc.ecds.protocol.rest.Violation;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.model.AuditEntry;
import hxc.services.ecds.model.Batch;
import hxc.services.ecds.model.IMasterData;
import hxc.services.ecds.model.Stage;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.util.RuleCheckException;

public abstract class Processor<T extends IBatchEnabled<T>>
{
	final static Logger logger = LoggerFactory.getLogger(Processor.class);
	
	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	///////////////////////////////////
	protected String[] headings;
	protected Map<Integer, String> propertyMap;
	protected Integer verbCol = null;
	protected ICreditDistribution context;

	protected static final String VERB = "verb";

	protected static final int VERB_ADD = 1;
	protected static final int VERB_UPDATE = 2;
	protected static final int VERB_UPSERT = 3;
	protected static final int VERB_DELETE = 4;
	protected static final int VERB_VERIFY = 5;

	protected static final int ET_SCLASS = 1;
	protected static final int ET_GROUP = 2;
	protected static final int ET_TIER = 3;
	protected static final int EA_AGENT = 4;
	protected static final int ES_WEBUSER = 5;
	protected static final int ET_ACCOUNT = 6;
	protected static final int ET_RULE = 7;
	protected static final int ET_DEPT = 8;
	protected static final int ET_AREA = 9;
	protected static final int ET_CELL = 10;
	protected static final int ET_PROMOTION = 11;
	protected static final int EL_CELLGROUP = 12;

	protected SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

	protected boolean mayInsert = false;
	protected boolean mayUpdate = false;
	protected boolean mayDelete = false;

	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	///////////////////////////////////
	protected Processor(ICreditDistribution context, boolean mayInsert, boolean mayUpdate, boolean mayDelete)
	{
		this.context = context;
		this.mayInsert = mayInsert;
		this.mayUpdate = mayUpdate;
		this.mayDelete = mayDelete;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Initialize
	//
	///////////////////////////////////
	public boolean initialize(State state, String firstRow)
	{
		if (firstRow == null || firstRow.isEmpty())
			return true;

		this.headings = firstRow.split("\\,");

		propertyMap = new HashMap<Integer, String>();
		verbCol = null;
		boolean failed = false;
		for (int index = 0; index < this.headings.length; index++)
		{
			String heading = this.headings[index];
			if (VERB.equalsIgnoreCase(heading))
				verbCol = index;
			else
			{
				String property = getProperty(heading, index + 1 == this.headings.length);
				if (property == null)
				{
					state.addIssue(BatchIssue.INVALID_HEADING, heading.toLowerCase(), null, heading + " is not allowed");
					failed = true;
				}
				else
				{
					propertyMap.put(index, property);
				}
			}
		}

		if (verbCol == null)
		{
			state.addIssue(BatchIssue.MISSING_HEADING, VERB, null, "No 'verb' heading");
			return false;
		}

		return !failed;
	}

	protected abstract String getProperty(String heading, boolean lastColumn);

	public void preProcess(EntityManager em, State state, String[] rowValues)
	{
		if (isEmptyRow(rowValues))
			return;

		String verbText = rowValues[verbCol].toLowerCase();
		int verb = 0;
		switch (verbText)
		{
			case "add":
				verb = VERB_ADD;
				Integer idColumn = columnIndexForProperty("id");
				if (idColumn != null)
					rowValues[idColumn] = "0";
				break;

			case "update":
				verb = VERB_UPDATE;
				break;

			case "upsert":
				verb = VERB_UPSERT;
				break;

			case "delete":
				verb = VERB_DELETE;
				break;

			case "verify":
				verb = VERB_VERIFY;
				break;

			default:
				state.addIssue(BatchIssue.INVALID_VALUE, VERB, null, verbText + " is not a valid verb");
				return;
		}

		T existing = loadExisting(em, state, rowValues);
		List<Object> other = new ArrayList<Object>();

		// Action Verb
		switch (verb)
		{
			case VERB_ADD:
				if (existing != null)
				{
					state.addIssue(BatchIssue.ALREADY_EXISTS, null, null, "Cannot add because it already exists");
				}
				else
				{
					T newInstance = instantiate(em, state, null);
					amend(em, state, newInstance, rowValues, other);
					if (!validate(state, newInstance, null))
						return;
					if (mayInsert)
						onAddNew(em, state, newInstance, other);
					else
						state.addIssue(BatchIssue.PERMISSION_DENIED, null, null, "Not permitted to add new");
				}
				break;

			case VERB_UPDATE:
				if (existing == null)
					state.addIssue(BatchIssue.DOESNT_EXIST, null, null, "Cannot update because it doesn't exist");
				else
				{
					T updated = instantiate(em, state, existing);
					amend(em, state, updated, rowValues, other);
					if (!validate(state, updated, existing))
						return;
					if (mayUpdate)
						onUpdateExisting(em, state, existing, updated, other);
					else
						state.addIssue(BatchIssue.PERMISSION_DENIED, null, null, "Not permitted to update existing");
				}
				break;

			case VERB_UPSERT:
				if (existing == null)
				{
					T newInstance = instantiate(em, state, null);
					amend(em, state, newInstance, rowValues, other);
					if (!validate(state, newInstance, null))
						return;
					if (mayInsert)
						onAddNew(em, state, newInstance, other);
					else
						state.addIssue(BatchIssue.PERMISSION_DENIED, null, null, "Not permitted to add new");
				}
				else
				{
					T updated = instantiate(em, state, existing);
					amend(em, state, updated, rowValues, other);
					if (!validate(state, updated, existing))
						return;
					if (mayUpdate)
						onUpdateExisting(em, state, existing, updated, other);
					else
						state.addIssue(BatchIssue.PERMISSION_DENIED, null, null, "Not permitted to update existing");
				}
				break;

			case VERB_DELETE:
				if (existing == null)
					state.addIssue(BatchIssue.DOESNT_EXIST, null, null, "Cannot delete because it doesn't exist");
				else
				{
					if (mayDelete)
						onDeleteExisting(em, state, existing, other);
					else
						state.addIssue(BatchIssue.PERMISSION_DENIED, null, null, "Not permitted to delete existing");
				}
				break;

			case VERB_VERIFY:
				if (existing == null)
					state.addIssue(BatchIssue.DOESNT_EXIST, null, null, "Cannot verify because it doesn't exist");
				else
				{
					T instance = instantiate(em, state, existing);
					amend(em, state, instance, rowValues, other);
					verifyExisting(em, state, existing, instance, other);
				}
				break;
		}

	}

	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	///////////////////////////////////
	private boolean validate(State state, T instance, T previous)
	{
		boolean isGood = true;

		if (instance == null)
		{
			state.addIssue(BatchIssue.DOESNT_EXIST, null, null, "Cannot verify because it doesn't exist");
			return false;
		}

		instance.setCompanyID(state.getCompanyID());
		instance.setLastTime(state.getBatch().getLastTime());
		instance.setLastUserID(state.getBatch().getLastUserID());

		List<Violation> violations = instance.validate();
		if (violations != null)
		{
			for (Violation violation : violations)
			{
				state.addIssue(violation.getReturnCode(), violation.getProperty(), violation.getCriterium(), violation.getAdditionalInformation());
				isGood = false;
			}
		}

		if (!isGood)
			return isGood;

		try
		{
			instance.validate(previous);
		}
		catch (RuleCheckException e)
		{
			state.addIssue(e.getError(), e.getProperty(), null, e.getMessage());
			isGood = false;
		}

		return isGood;
	}

	public Integer columnIndexForProperty(String property)
	{
		if (property == null || property.isEmpty())
			return null;

		for (int index = 0; index < headings.length; index++)
		{
			if (property.equalsIgnoreCase(propertyMap.get(index)))
				return index;
		}

		return null;
	}

	private boolean isEmptyRow(String[] rowValues)
	{
		if (rowValues == null || rowValues.length == 0)
			return true;

		for (String value : rowValues)
		{
			if (value != null)
				return false;
		}

		return true;
	}

	private void updateAudit(Object oldValue, T newValue, State state, Stage stage, AuditEntryContext auditEntryContext)
	{
		String auditType = getAuditType();
		if (auditType == null || auditType.isEmpty() || stage == null)
			return;

		if (newValue != null && !(newValue instanceof IMasterData<?>))
			return;

		@SuppressWarnings("unchecked")
		AuditEntry auditEntry = AuditEntry.forBatch(oldValue, (IMasterData<T>) newValue, state.getBatch(), auditType, newValue, auditEntryContext);

		stage.setSequenceNo(auditEntry.getSequenceNo());
		stage.setAuditAction(auditEntry.getAction());
		stage.setOldValue(auditEntry.getOldValue());
		stage.setNewValue(auditEntry.getNewValue());
		stage.setAuditSignature(auditEntry.getSignature());
	}

	private void onAddNew(EntityManager em, State state, T newInstance, List<Object> other)
	{
		Stage stage = addNew(em, state, newInstance, other);
		if (stage != null)
		{
			AuditEntryContext auditEntryContext = new AuditEntryContext("BATCH_IMPORT_ADD_NEW", newInstance.getClass().getName());
			updateAudit(null, newInstance, state, stage, auditEntryContext);
			em.persist(stage);
		}
	}

	private void onUpdateExisting(EntityManager em, State state, T existing, T updated, List<Object> other)
	{
		Stage stage = updateExisting(em, state, existing, updated, other);
		if (stage != null)
		{
			AuditEntryContext auditEntryContext = new AuditEntryContext("BATCH_IMPORT_UPDATE_EXISTING", updated.getClass().getName(), updated.getId());
			updateAudit(existing, updated, state, stage, auditEntryContext);
			em.persist(stage);
		}
	}

	private void onDeleteExisting(EntityManager em, State state, T existing, List<Object> other)
	{
		Stage stage = deleteExisting(em, state, existing, other);
		if (stage != null)
		{
			AuditEntryContext auditEntryContext = new AuditEntryContext("BATCH_IMPORT_DELETE_EXISTING", existing.getClass().getName(), existing.getId());
			updateAudit(existing, null, state, stage, auditEntryContext);
			em.persist(stage);
		}

	}

	public boolean onComplete(EntityManager em, State state)
	{
		if (!this.complete(em, state))
			return false;

		String auditType = getAuditType();
		if (auditType == null || auditType.isEmpty())
			return true;

		// Insert Audit Entries
		try
		{
			String sqlString = "insert `es_audit` " + //
					"(action,agentuser_id,company_id,data_type,domain_name,ip_address,lm_time,lm_userid,mac_address," + //
					"machine_name,new_value,old_value,seq_no,signature,time_stamp,webuser_id,version, reason_code, reason_attributes) " + //
					"select " + //
					"audit_action as action, " + //
					"null as agentuser_id, " + //
					"company_id, " + //
					":data_type as data_type, " + //
					":domain_name as domain_name, " + //
					":ip_address as ip_address, " + //
					":time_stamp as lm_time, " + //
					":webuser_id as lm_userid, " + //
					":mac_address as mac_address, " + //
					":machine_name as machine_name, " + //
					"new_value, " + //
					"old_value, " + //
					"seq_no, " + //
					"audit_signature as signature, " + //
					":time_stamp as time_stamp, " + //
					":webuser_id as webuser_id, " + //
					"0 as version, " + //
					":reason_code as reason_code, " + //
					":reason_attributes as reason_attributes " + //
					"from `eb_stage` " + //
					"where batch_id = :batch_id " + //
					"and audit_signature is not null;";
			Query auditQuery = em.createNativeQuery(sqlString);
			Batch batch = state.getBatch();
			auditQuery.setParameter("batch_id", batch.getId());
			auditQuery.setParameter("data_type", auditType);
			auditQuery.setParameter("domain_name", batch.getDomainName());
			auditQuery.setParameter("ip_address", batch.getIpAddress());
			auditQuery.setParameter("time_stamp", batch.getTimestamp());
			auditQuery.setParameter("webuser_id", batch.getWebUserID());
			auditQuery.setParameter("mac_address", batch.getMacAddress());
			auditQuery.setParameter("machine_name", batch.getMachineName());
			auditQuery.setParameter("reason_code", "BATCH_IMPORT_COMPLETE");
			auditQuery.setParameter("reason_attributes", "[]");
			int count = auditQuery.executeUpdate();
			return true;
		}
		catch (Throwable tr)
		{
			logger.error(TransactionsConfig.ERR_TECHNICAL_PROBLEM, tr);
			state.addIssue(TransactionsConfig.ERR_TECHNICAL_PROBLEM, null, null, tr.getMessage());
			return false;
		}

	}
	
	public boolean checkForDuplicates(EntityManager em, State state, int tableID, String column, String heading)
	{
		if (column == null || column.isEmpty() || heading == null || heading.isEmpty())
			return true;
		
		Integer[] lineNumbers = Stage.findDuplicates(em, state.getBatch().getId(), tableID, column);
		if (lineNumbers == null || lineNumbers.length == 0)
			return true;
		
		for (int lineNumber:lineNumbers)
		{
			String additionalInformation = String.format("Duplicate '%s' on line %d", heading, lineNumber);
			state.addIssue(BatchIssue.ALREADY_EXISTS, heading, lineNumber, null, additionalInformation);
		}
		
		return false;
	}
	
	public boolean checkForDuplicates(EntityManager em, State state, int tableID, String column1, String heading1, String column2, String heading2)
	{
		if (column1 == null || column1.isEmpty() || column2 == null || column2.isEmpty() || heading1 == null || heading1.isEmpty() || heading2 == null || heading2.isEmpty())
			return true;

		Integer[] lineNumbers = Stage.findDuplicates(em, state.getBatch().getId(), tableID, column1, column2);
		if (lineNumbers == null || lineNumbers.length == 0)
			return true;

		for (int lineNumber:lineNumbers)
		{
			String additionalInformation = String.format("Duplicate '%s:%s' on line %d", heading1, heading2, lineNumber);
			state.addIssue(BatchIssue.ALREADY_EXISTS, heading1, lineNumber, null, additionalInformation);
		}

		return false;
	}

	public String toLowercase(String value)
	{
		return value == null ? null : value.toLowerCase();
	}

	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Validators
	//
	///////////////////////////////////

	public void verify(State state, String property, String expected, String actual)
	{
		if (expected == null || expected.isEmpty())
		{
			if (actual == null || actual.isEmpty())
				return;
		}
		else if (expected.equals(actual))
			return;

		state.addIssue(BatchIssue.VALUE_DIFFERS, property, expected, differs(property, actual, expected));
	}

	public void verify(State state, String property, BigDecimal expected, BigDecimal actual)
	{
		if (expected == null)
		{
			if (actual == null)
				return;
		}
		else if (actual != null && expected.compareTo(actual) == 0)
			return;

		state.addIssue(BatchIssue.VALUE_DIFFERS, property, expected, differs(property, actual, expected));
	}

	public void verify(State state, String property, Integer expected, Integer actual)
	{
		if (expected == null)
		{
			if (actual == null)
				return;
		}
		else if (expected.equals(actual))
			return;

		state.addIssue(BatchIssue.VALUE_DIFFERS, property, expected, differs(property, actual, expected));
	}

	public void verify(State state, String property, Double expected, Double actual, double eps)
	{
		if (expected == null)
		{
			if (actual == null)
				return;
		}
		else if (actual != null && almostEqual(actual, expected, eps))
			return;

		state.addIssue(BatchIssue.VALUE_DIFFERS, property, expected, differs(property, actual, expected));
	}

	private boolean almostEqual(double a, double b, double eps)
	{
		return Math.abs(a - b) < eps;
	}

	public void verify(State state, String property, Boolean expected, Boolean actual)
	{
		if (expected == null)
		{
			if (actual == null)
				return;
		}
		else if (expected.equals(actual))
			return;

		state.addIssue(BatchIssue.VALUE_DIFFERS, property, expected, differs(property, actual, expected));
	}

	public void verify(State state, String property, Date expected, Date actual)
	{

		if (expected == null)
		{
			if (actual == null)
				return;
		}
		else if (actual != null && expected.getTime() / 1000 == actual.getTime() / 1000)
			return;

		state.addIssue(BatchIssue.VALUE_DIFFERS, property, expected, differs(property, actual, expected));
	}

	private String differs(String property, Object actual, Object expected)
	{
		String a = actual == null ? "<null>" : (actual instanceof Date ? sdf.format((Date) actual) : actual.toString());
		String e = expected == null ? "<null>" : (expected instanceof Date ? sdf.format((Date) expected) : expected.toString());

		return String.format("Expected '%s' but was '%s'", e, a);
	}

	////////////////////////////////////////////////////////////////////////////////////////
	//
	// Abstract methods to be implemented in Entity-Specific processors
	//
	///////////////////////////////////
	protected abstract T instantiate(EntityManager em, State state, T from);

	protected abstract void amend(EntityManager em, State state, T instance, String[] rowValues, List<Object> other);

	protected abstract T loadExisting(EntityManager em, State state, String[] rowValues);

	protected abstract Stage addNew(EntityManager em, State state, T newInstance, List<Object> other);

	protected abstract Stage updateExisting(EntityManager em, State state, T existing, T updated, List<Object> other);

	protected abstract Stage deleteExisting(EntityManager em, State state, T existing, List<Object> other);

	protected abstract void verifyExisting(EntityManager em, State state, T existing, T instance, List<Object> other);

	public abstract boolean complete(EntityManager em, State state);

	protected abstract String getAuditType();

	public void sendNotifications(EntityManager em, State state)
	{
	}
}
