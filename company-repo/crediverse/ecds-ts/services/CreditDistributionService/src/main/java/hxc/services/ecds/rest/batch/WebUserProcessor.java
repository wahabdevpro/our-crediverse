package hxc.services.ecds.rest.batch;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.BatchIssue;
import hxc.ecds.protocol.rest.IAuthenticatable;
import hxc.services.ecds.model.AuditEntry;
import hxc.services.ecds.model.Department;
import hxc.services.ecds.model.Role;
import hxc.services.ecds.model.Stage;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.WebUser;
import hxc.services.ecds.model.WorkItem;
import hxc.services.ecds.rest.AgentUsers;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.util.RuleCheckException;

public class WebUserProcessor extends Processor<WebUser>
{
	final static Logger logger = LoggerFactory.getLogger(AgentUsers.class);
	
	public static final String[] HEADINGS = new String[] { //
			"activation_date", //
			"deactivation_date", //
			"expiration_date", //
			"surname", //
			"id", //
			"first_name", //
			"status", //
			"account_number", //
			"auth_method",
			"domain_account", //
			"initials", //
			"language", //
			"msisdn", //
			"department", //
			"email", //
			"title", //
			"role", //
	};

	public WebUserProcessor(ICreditDistribution context, boolean mayInsert, boolean mayUpdate, boolean mayDelete)
	{
		super(context, mayInsert, mayUpdate, mayDelete);
	}

	@Override
	protected String getProperty(String heading, boolean lastColumn)
	{
		switch (heading)
		{
			case "activation_date":
				return "activationDate";

			case "deactivation_date":
				return "deactivationDate";

			case "expiration_date":
				return "expirationDate";

			case "surname":
				return "surname";

			case "id":
				return "id";

			case "first_name":
				return "firstName";

			case "status":
				return "state";

			case "account_number":
				return "accountNumber";

			case "auth_method":
				return "authenticationMethod";
				
			case "domain_account":
				return "domainAccountName";

			case "initials":
				return "initials";

			case "language":
				return "language";

			case "msisdn":
				return "mobileNumber";

			case "department":
				return "department";

			case "email":
				return "email";

			case "title":
				return "title";

			case "role":
				return "role";

			default:
				return null;
		}

	}

	@Override
	protected String getAuditType()
	{
		return AuditEntry.TYPE_WEB_USER;
	}

	@Override
	protected WebUser instantiate(EntityManager em, State state, WebUser from)
	{
		WebUser result = new WebUser();
		try
		{
			if (from != null)
				result.amend(em, from);
		}
		catch (RuleCheckException e)
		{
			logger.info("rulecheck", e);
			return null;
		}
		return result;
	}

	@Override
	protected void amend(EntityManager em, State state, WebUser webUser, String[] rowValues, List<Object> other)
	{
		for (int index = 0; index < rowValues.length && index < headings.length; index++)
		{
			String property = propertyMap.get(index);
			if (property == null)
				continue;
			String value = rowValues[index];
			String heading = headings[index];
			switch (property)
			{
				case "activationDate":
					webUser.setActivationDate(state.parseDate(heading, value));
					break;

				case "deactivationDate":
					webUser.setDeactivationDate(state.parseDate(heading, value));
					break;

				case "expirationDate":
					webUser.setExpirationDate(state.parseDate(heading, value));
					break;

				case "surname":
					webUser.setSurname(value);
					break;

				case "id":
					webUser.setId(state.parseInt(heading, value, 0));
					break;

				case "firstName":
					webUser.setFirstName(value);
					break;

				case "state":
					webUser.setState(value);
					break;

				case "accountNumber":
					webUser.setAccountNumber(value);
					break;

				case "authenticationMethod":
					if(value != null && value.equals(IAuthenticatable.AUTHENTICATE_EXTERNAL_2FACTOR)) //X
					{
						webUser.setAuthenticationMethod(value);
					} else {
						webUser.setAuthenticationMethod(IAuthenticatable.AUTHENTICATE_PASSWORD_2FACTOR); //A
					}
					break;	

				case "domainAccountName":
					webUser.setDomainAccountName(value);
					break;

				case "initials":
					webUser.setInitials(value);
					break;

				case "language":
					webUser.setLanguage(toLowercase(value));
					break;

				case "mobileNumber":
					webUser.setMobileNumber(context.toMSISDN(value));
					break;

				case "department":
					if (value == null || value.isEmpty())
					{
						webUser.setDepartment(null);
					}
					else
					{
						Department department = Department.findByName(em, state.getCompanyID(), value);
						if (department == null)
							state.addIssue(BatchIssue.INVALID_VALUE, "department", null, value + " is not a valid Department");
						else
						{
							webUser.setDepartment(department);
							webUser.setDepartmentID(department.getId());
						}
					}
					break;

				case "email":
					webUser.setEmail(value);
					break;

				case "title":
					webUser.setTitle(value);
					break;

				case "role":
					if (value == null || value.isEmpty())
						removeRoles(webUser);
					else
					{
						Role role = Role.findByName(em, state.getCompanyID(), value);
						if (role == null)
							state.addIssue(BatchIssue.INVALID_VALUE, "role", null, value + " is not a valid Role");
						else
						{
							removeRoles(webUser);
							webUser.getRoles().add(role);
						}
					}
					break;

			}
		}

		// Auto Number if Required
		webUser.autoNumber(em, state.getCompanyID());

	}

	private void removeRoles(WebUser webUser)
	{
		if (webUser == null)
			return;
		List<Role> roles = webUser.getRoles();
		if (roles != null)
			roles.clear();
	}

	@Override
	protected WebUser loadExisting(EntityManager em, State state, String[] rowValues)
	{
		// Load By ID
		Integer columnIndex = columnIndexForProperty("id");
		if (columnIndex != null)
		{
			int id = state.parseInt("id", rowValues[columnIndex], 0);
			WebUser webUser = WebUser.findByID(em, id, state.getCompanyID());
			if (webUser != null)
				return webUser;
		}

		// Load by Domain Name
		columnIndex = columnIndexForProperty("domainAccountName");
		if (columnIndex != null)
		{
			WebUser webUser = WebUser.findByDomainAccountName(em, state.getCompanyID(), rowValues[columnIndex]);
			if (webUser != null)
				return webUser;
		}

		// Load by Account Number
		columnIndex = columnIndexForProperty("accountNumber");
		if (columnIndex != null)
		{
			WebUser webUser = WebUser.findByAccountNo(em, state.getCompanyID(), rowValues[columnIndex]);
			if (webUser != null)
				return webUser;
		}

		return null;
	}

	@Override
	protected Stage addNew(EntityManager em, State state, WebUser newInstance, List<Object> other)
	{
		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_INSERT, ES_WEBUSER) //

				.setD1(newInstance.getActivationDate()) //
				.setD2(newInstance.getDeactivationDate()) //
				.setD3(newInstance.getExpirationDate()) //
				.setCompanyID(newInstance.getCompanyID()) //
				.setDescription(newInstance.getSurname()) //
				.setLastTime(newInstance.getLastTime()) //
				.setLastUserID(newInstance.getLastUserID()) //
				.setName(newInstance.getFirstName()) //
				.setState(newInstance.getState()) //
				.setAccountNumber(newInstance.getAccountNumber()) //
				.setAuthenticationMethod(newInstance.getAuthenticationMethod()) //
				.setDomainAccountName(newInstance.getDomainAccountName()) //
				.setInitials(newInstance.getInitials()) //
				.setLanguage(newInstance.getLanguage()) //
				.setMobileNumber(newInstance.getMobileNumber()) //
				.setS1(newInstance.getAuthenticationMethod()) //
				.setI1(newInstance.getDepartmentID()) //
				.setPostalAddressLine2(newInstance.getEmail()) //
				.setTitle(newInstance.getTitle()) //
				.setI3(newInstance.getRoles().isEmpty() ? null : newInstance.getRoles().get(0).getId()) //
		;

		return stage;
	}

	@Override
	protected Stage updateExisting(EntityManager em, State state, WebUser existing, WebUser updated, List<Object> other)
	{
		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_UPDATE, ES_WEBUSER) //

				.setD1(updated.getActivationDate()) //
				.setD2(updated.getDeactivationDate()) //
				.setD3(updated.getExpirationDate()) //
				.setCompanyID(updated.getCompanyID()) //
				.setDescription(updated.getSurname()) //
				.setLastTime(updated.getLastTime()) //
				.setLastUserID(updated.getLastUserID()) //
				.setName(updated.getFirstName()) //
				.setState(updated.getState()) //
				.setAccountNumber(updated.getAccountNumber()) //
				.setDomainAccountName(updated.getDomainAccountName()) //
				.setInitials(updated.getInitials()) //
				.setLanguage(updated.getLanguage()) //
				.setMobileNumber(updated.getMobileNumber()) //
				.setAuthenticationMethod(updated.getAuthenticationMethod()) //
				.setI1(updated.getDepartmentID()) //
				.setPostalAddressLine2(updated.getEmail()) //
				.setTitle(updated.getTitle()) //
				.setI3(updated.getRoles().isEmpty() ? null : updated.getRoles().get(0).getId()) //
				.setEntityID(existing.getId()) //
				.setEntityVersion(existing.getVersion()) //
		;

		return stage;
	}

	@Override
	protected Stage deleteExisting(EntityManager em, State state, WebUser existing, List<Object> other)
	{
		if (WebUser.STATE_PERMANENT.equals(existing.getState()))
		{
			state.addIssue(BatchIssue.CANNOT_DELETE, null, null, "Cannot delete Permanent");
			return null;
		}

		if (AuditEntry.referencesWebUser(em, existing.getId()) //
				|| WorkItem.referencesWebUser(em, existing.getId()) || Transaction.referencesWebUser(em, existing.getMobileNumber()))
		{
			state.addIssue(BatchIssue.CANNOT_DELETE, null, null, "WebUser in use");
			return null;
		}

		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_DELETE, ES_WEBUSER) //

				.setEntityID(existing.getId()) //
		;

		return stage;
	}

	@Override
	protected void verifyExisting(EntityManager em, State state, WebUser existing, WebUser instance, List<Object> other)
	{
		verify(state, "activationDate", existing.getActivationDate(), instance.getActivationDate());
		verify(state, "deactivationDate", existing.getDeactivationDate(), instance.getDeactivationDate());
		verify(state, "expirationDate", existing.getExpirationDate(), instance.getExpirationDate());
		verify(state, "surname", existing.getSurname(), instance.getSurname());
		verify(state, "firstName", existing.getFirstName(), instance.getFirstName());
		verify(state, "state", existing.getState(), instance.getState());
		verify(state, "accountNumber", existing.getAccountNumber(), instance.getAccountNumber());
		verify(state, "authenticationMethod", existing.getAuthenticationMethod(), instance.getAuthenticationMethod());
		verify(state, "domainAccountName", existing.getDomainAccountName(), instance.getDomainAccountName());
		verify(state, "initials", existing.getInitials(), instance.getInitials());
		verify(state, "language", existing.getLanguage(), instance.getLanguage());
		verify(state, "mobileNumber", existing.getMobileNumber(), instance.getMobileNumber());
		verify(state, "department", existing.getDepartment().getName(), instance.getDepartment().getName());
		verify(state, "email", existing.getEmail(), instance.getEmail());
		verify(state, "title", existing.getTitle(), instance.getTitle());

		String existingRole = existing.getRoles().isEmpty() ? "none" : existing.getRoles().get(0).getName();
		String instanceRole = instance.getRoles().isEmpty() ? "none" : instance.getRoles().get(0).getName();
		verify(state, "role", existingRole, instanceRole);
	}

	@Override
	public boolean complete(EntityManager em, State state)
	{
		// Exit if there are issues
		if (state.hasIssues())
			return false;

		// Check for duplicates
		if (!checkForDuplicates(em, state, ES_WEBUSER, "acc_no", "account_number") //
			|| !checkForDuplicates(em, state, ES_WEBUSER, "domain_account", "domain_account"))
			return false;

		// Remove Roles
		try
		{
			String sql = "delete s.* from es_webuser_role as s inner join eb_stage as b " //
					+ "on b.batch_id = :batchID and b.entity_id = s.webuser_id and b.table_id = :tableID";
			Query query = em.createNativeQuery(sql);
			query.setParameter("batchID", state.getBatch().getId());
			query.setParameter("tableID", ES_WEBUSER);
			query.executeUpdate();
		}
		catch (Throwable tr)
		{
			logger.info(BatchIssue.CANNOT_UPDATE, tr);
			state.addIssue(BatchIssue.CANNOT_UPDATE, null, null, tr.getMessage());
			return false;
		}

		// Insert
		try
		{
			int expected = Stage.expected(em, state.getBatch().getId(), Stage.ACTION_INSERT, ES_WEBUSER);
			if (expected > 0)
			{
				String sqlString = "insert es_webuser " //
						+ "(a_date, d_date, e_date, comp_id, last_name, version, lm_time, lm_userid, first_name, state, acc_no, auth_method, domain_name, initials, lang, msisdn, pin_version, temp_pin, dept_id, email, title) " //
						+ "select d1 as a_date, d2 as d_date, d3 as e_date, company_id as comp_id, description as last_name, 0 as version, lm_time, lm_userid, name as first_name, state, acc_no, auth_method, domain_account as domain_name, intitials as initials, language as lang, msisdn, 1 as pin_version, 0 as temp_pin, i1 as dept_id, postal2 as email, title " //
						+ "from eb_stage where batch_id = :batch_id and action = :action and table_id = :tableID order by line_no";
				Query insertQuery = em.createNativeQuery(sqlString);
				insertQuery.setParameter("batch_id", state.getBatch().getId());
				insertQuery.setParameter("action", Stage.ACTION_INSERT);
				insertQuery.setParameter("tableID", ES_WEBUSER);
				int count = insertQuery.executeUpdate();
				if (count != expected)
				{
					state.addIssue(BatchIssue.CANNOT_ADD, "name", expected, String.format("%d/%d records inserted", count, expected));
					return false;
				}
				state.getBatch().setInsertCount(count);
			}
		}
		catch (Throwable tr)
		{
			logger.info(BatchIssue.CANNOT_ADD, tr);
			state.addIssue(BatchIssue.CANNOT_ADD, null, null, tr.getMessage());
			return false;
		}

		// Update
		try
		{
			int expected = Stage.expected(em, state.getBatch().getId(), Stage.ACTION_UPDATE, ES_WEBUSER);
			if (expected > 0)
			{
				String sqlString = "update  es_webuser as c \n" //
						+ "join    eb_stage as s on c.id = s.entity_id and c.version = s.entity_version  \n" //
						+ "set		c.a_date = s.d1 \n" //
						+ ",		c.d_date = s.d2 \n" //
						+ ",		c.e_date = s.d3 \n" //
						+ ",		c.comp_id = s.company_id \n" //
						+ ",		c.last_name = s.description \n" //
						+ ",		c.id = s.entity_id \n" //
						+ ",		c.version = s.entity_version + 1 \n" //
						+ ",		c.lm_time = s.lm_time \n" //
						+ ",		c.lm_userid = s.lm_userid \n" //
						+ ",		c.first_name = s.name \n" //
						+ ",		c.state = s.state \n" //
						+ ",		c.acc_no = s.acc_no \n" //
						+ ",		c.domain_name = s.domain_account \n" //
						+ ",		c.initials = s.intitials \n" //
						+ ",		c.lang = s.language \n" //
						+ ",		c.msisdn = s.msisdn \n" //
						+ ",		c.auth_method = s.auth_method \n" //
						+ ",		c.dept_id = s.i1 \n" //
						+ ",		c.email = s.postal2 \n" //
						+ ",		c.title = s.title \n" //
						+ "where   s.action = :action \n" //
						+ "and     s.table_id = :tableID \n" //
						+ "and     s.batch_id = :batchID";
				Query updateQuery = em.createNativeQuery(sqlString);
				updateQuery.setParameter("batchID", state.getBatch().getId());
				updateQuery.setParameter("action", Stage.ACTION_UPDATE);
				updateQuery.setParameter("tableID", ES_WEBUSER);
				int count = updateQuery.executeUpdate();
				if (count != expected)
				{
					state.addIssue(BatchIssue.CANNOT_UPDATE, "name", expected, String.format("%d/%d records updated", count, expected));

					return false;
				}
				state.getBatch().setUpdateCount(count);
			}
		}
		catch (Throwable tr)
		{
			logger.info(BatchIssue.CANNOT_UPDATE, tr);
			state.addIssue(BatchIssue.CANNOT_UPDATE, null, null, tr.getMessage());
			return false;
		}

		// Delete
		try
		{
			int expected = Stage.expected(em, state.getBatch().getId(), Stage.ACTION_DELETE, ES_WEBUSER);
			if (expected > 0)
			{
				int count = Stage.delete(em, "es_webuser", state.getBatch().getId(), ES_WEBUSER);
				if (count != expected)
				{
					state.addIssue(BatchIssue.CANNOT_DELETE, "id", expected, String.format("%d/%d records deleted", count, expected));
					return false;
				}
				state.getBatch().setDeleteCount(count);
			}
		}
		catch (Throwable tr)
		{
			logger.info(BatchIssue.CANNOT_DELETE, tr);
			state.addIssue(BatchIssue.CANNOT_DELETE, null, null, tr.getMessage());
			return false;
		}

		// Add Roles
		try
		{
			String sql = "insert  es_webuser_role (webuser_id, role_id) " //
					+ "select  w.id as webuser_id, s.i3 as role_id " //
					+ "from    eb_stage as s " //
					+ "join    es_webuser as w on w.comp_id = s.company_id and w.domain_name = s.domain_account " //
					+ "where   s.batch_id = :batchID and s.table_id = :tableID and s.i3 > 0";
			Query query = em.createNativeQuery(sql);
			query.setParameter("batchID", state.getBatch().getId());
			query.setParameter("tableID", ES_WEBUSER);
			query.executeUpdate();
		}
		catch (Throwable tr)
		{
			logger.info(BatchIssue.CANNOT_UPDATE, tr);
			state.addIssue(BatchIssue.CANNOT_UPDATE, null, null, tr.getMessage());
			return false;
		}

		return true;
	}

}
