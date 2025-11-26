package hxc.services.ecds.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
//import org.codehaus.jackson.annotate.JsonIgnore;

import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.util.QueryBuilder;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.utils.calendar.DateTime;

////////////////////////////////////////////////////////////////////////////////////////
//
// ClientState Table - Used for Agent Segmentation
//
///////////////////////////////////

@Table(name = "es_client")
@Entity
@NamedQueries({ //
		@NamedQuery(name = "ClientState.findByKey", query = "SELECT p FROM ClientState p where key = :key and companyID = :companyID and userID = :userID and userType = :userType"), //
		@NamedQuery(name = "ClientState.cleanout", query = "delete from ClientState p where p.lastUpdated < :before and companyID = :companyID"), //
})

public class ClientState extends hxc.ecds.protocol.rest.ClientState implements Serializable, ICompanyData<ClientState>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 6933796518198681578L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Additional Fields
	//
	// /////////////////////////////////
	@JsonIgnore
	protected int lastUserID;
	@JsonIgnore
	protected Date lastTime;
	@JsonIgnore
	protected int companyID;
	@JsonIgnore
	protected int userID;
	@JsonIgnore
	protected String userType;
	@JsonIgnore
	protected Date lastUpdated;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@Override
	@Column(name = "company_id", nullable = false)
	@Id
	public int getCompanyID()
	{
		return companyID;
	}

	public ClientState setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Override
	@Column(name = "value_key", nullable = false, length = KEY_MAX_LENGTH)
	@Id
	public String getKey()
	{
		return key;
	}

	@Override
	public ClientState setKey(String key)
	{
		this.key = key;
		return this;
	}

	@Override
	@Column(name = "value_text", nullable = true, columnDefinition = "TEXT")
	public String getValue()
	{
		return value;
	}

	@Override
	public ClientState setValue(String value)
	{
		this.value = value;
		return this;
	}

	@Column(name = "user_id", nullable = false)
	@Id
	public int getUserID()
	{
		return userID;
	}

	public ClientState setUserID(int userID)
	{
		this.userID = userID;
		return this;
	}

	@Column(name = "user_type", nullable = false, length = 1)
	@Id
	public String getUserType()
	{
		return userType;
	}

	public ClientState setUserType(String userType)
	{
		this.userType = userType;
		return this;
	}

	@Column(name = "last_date", nullable = false)
	public Date getLastUpdated()
	{
		return lastUpdated;
	}

	public ClientState setLastUpdated(Date lastUpdated)
	{
		this.lastUpdated = lastUpdated;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public ClientState setLastUserID(int lastUserID)
	{
		this.lastUserID = lastUserID;
		return this;
	}

	@Override
	@Version
	public int getVersion()
	{
		return version;
	}

	@Override
	public ClientState setVersion(int version)
	{
		this.version = version;
		return this;
	}

	@Override
	@Column(name = "lm_time", nullable = false)
	public Date getLastTime()
	{
		return lastTime;
	}

	@Override
	public ClientState setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public ClientState()
	{

	}

	public ClientState(ClientState state)
	{
		this.lastUserID = state.lastUserID;
		this.lastTime = state.lastTime;
		this.companyID = state.getCompanyID();
		this.userID = state.getUserID();
		this.userType = state.getUserType();
		this.lastUpdated = state.getLastUpdated();
		amend(state);
	}

	public void amend(hxc.ecds.protocol.rest.ClientState state)
	{
		this.version = state.getVersion();
		this.key = state.getKey();
		this.value = state.getValue();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public static ClientState findByKey(EntityManager em, ClientState like)
	{
		TypedQuery<ClientState> query = em.createNamedQuery("ClientState.findByKey", ClientState.class);
		query.setParameter("companyID", like.getCompanyID());
		query.setParameter("key", like.getKey());
		query.setParameter("userID", like.getUserID());
		query.setParameter("userType", like.getUserType());
		List<ClientState> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	@Override
	public void persist(EntityManager em, ClientState existing, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(existing);
		QueryBuilder.persist(em, existing, this, session, null, auditEntryContext);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		QueryBuilder.remove(em, this, session, null, auditEntryContext);
	}

	@Override
	public void validate(ClientState previous) throws RuleCheckException
	{
		RuleCheck.validate(this);

		if (previous != null)
		{
			RuleCheck.noChange("companyID", companyID, previous.companyID);
		}

	}

	public static int cleanout(EntityManager em, DateTime before, int companyID)
	{
		Query query = em.createNamedQuery("ClientState.cleanout");
		query.setParameter("before", before);
		query.setParameter("companyID", companyID);
		return query.executeUpdate();
	}

}
