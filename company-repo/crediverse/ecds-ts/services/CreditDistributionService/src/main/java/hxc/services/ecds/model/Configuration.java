package hxc.services.ecds.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PersistenceException;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.Version;

import hxc.ecds.protocol.rest.config.IConfiguration;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

///////////////////////////////////////////////////////////////////////////////////////
//
// Config Table - Used for Configurations
//
///////////////////////////////////

@Table(name = "ec_config")
@Entity
@NamedQueries({ //
		@NamedQuery(name = "Config.findByID", query = "SELECT p FROM Configuration p where id = :id and companyID = :companyID") })
public class Configuration implements Serializable, IMasterData<Configuration>
{
	final static Logger logger = LoggerFactory.getLogger(Configuration.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = -3432493313446744404L;
	private static final int CONTENT_MAX_LENGTH = 16384;
	
	public static final Permission MAY_CONFIGURE_GENERAL_SETTINGS = new Permission(false, false, Permission.GROUP_GENERAL, Permission.PERM_CONFIGURE_GENERAL_SETTINGS, "May Configure General Settings");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected long id;
	protected int companyID;
	protected int version;
	protected byte[] content;
	protected int lastUserID;
	protected Date lastTime;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Id
	public long getId()
	{
		return id;
	}

	public Configuration setId(long id)
	{
		this.id = id;
		return this;
	}

	@Id
	@Column(name = "comp_id", nullable = false)
	public int getCompanyID()
	{
		return companyID;
	}

	public Configuration setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Column(name = "content", nullable = false, unique = false, length = CONTENT_MAX_LENGTH)
	public byte[] getContent()
	{
		return content;
	}

	public Configuration setContent(byte[] content)
	{
		this.content = content;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public Configuration setLastUserID(int lastUserID)
	{
		this.lastUserID = lastUserID;
		return this;
	}

	@Override
	@Column(name = "lm_time", nullable = false)
	public Date getLastTime()
	{
		return lastTime;
	}

	@Override
	public Configuration setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	@Override
	@Version
	public int getVersion()
	{
		return version;
	}

	public Configuration setVersion(int version)
	{
		this.version = version;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Cloning
	//
	// /////////////////////////////////

	// Make deep copy
	public Configuration copy(EntityManager em)
	{
		Configuration copy = new Configuration();
		copy.id = this.id;
		copy.companyID = this.companyID;
		copy.version = this.version;
		copy.content = this.content;
		copy.lastUserID = this.lastUserID;
		copy.lastTime = this.lastTime;

		return copy;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Finders
	//
	// /////////////////////////////////
	public static Configuration findByID(EntityManager em, long id, int companyID)
	{
		TypedQuery<Configuration> query = em.createNamedQuery("Config.findByID", Configuration.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<Configuration> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IMasterData
	//
	// /////////////////////////////////

	@Override
	public void persist(EntityManager em, Configuration oldValue, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(oldValue);
		try (RequiresTransaction transaction = new RequiresTransaction(em))
		{
			this.setLastTime(new Date());
			this.setLastUserID(session.getUserID());
			em.persist(this);
			IConfiguration oldConfig = unpack(oldValue);
			IConfiguration newConfig = unpack(this);
			AuditEntry.log(em, oldConfig, this, session, AuditEntry.TYPE_CONFIG, newConfig, auditEntryContext);
			transaction.commit();
		}
		catch (PersistenceException ex)
		{
			logger.error("", ex);
			throw new RuleCheckException(ex, StatusCode.FAILED_TO_SAVE, null, ex.getMessage());
		}
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		throw new RuleCheckException(StatusCode.CANNOT_DELETE, null, "Cannot delete configurations");

	}

	@Override
	public void validate(Configuration oldValue) throws RuleCheckException
	{
		// TODO Auto-generated method stub

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Pack / Unpack
	//
	// /////////////////////////////////
	public void pack(IConfiguration config)
	{
		this.version = config.getVersion();
		this.id = config.uid();

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); //
				GZIPOutputStream gzipOut = new GZIPOutputStream(baos))
		{
			try(ObjectOutputStream objectOut = new ObjectOutputStream(gzipOut))
			{
				objectOut.writeObject(config);
			}
			content = baos.toByteArray();
		}
		catch (IOException e)
		{
			logger.error("", e);
		}
	}

	@SuppressWarnings({"unchecked"})
	public <T extends IConfiguration> T unpack()
	{
		try (ByteArrayInputStream bais = new ByteArrayInputStream(content); //
				GZIPInputStream gzipIn = new GZIPInputStream(bais); //
				ObjectInputStream objectIn = new ObjectInputStream(gzipIn);)
		{
			return (T) objectIn.readObject();
		}
		catch (IOException | ClassNotFoundException e)
		{
			// TS won't start-up if this fails.
			logger.error("", e);
			return null;
		}
	}

	private IConfiguration unpack(Configuration value)
	{
		if (value == null)
			return null;

		byte[] content = value.getContent();
		if (content == null || content.length == 0)
			return null;

		return value.unpack();

	}

	public static void loadMRD(EntityManager em, Session session) throws RuleCheckException
	{
		Permission.loadMRD(em, MAY_CONFIGURE_GENERAL_SETTINGS, session);
	}
}
