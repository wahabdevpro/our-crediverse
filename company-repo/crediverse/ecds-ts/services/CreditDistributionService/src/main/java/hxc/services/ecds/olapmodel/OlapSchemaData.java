package hxc.services.ecds.olapmodel;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.services.ecds.rest.ICreditDistribution;

@Table(name = "ap_schema_data")
@Entity
@NamedQueries({ //
		@NamedQuery(name = "OlapSchemaData.findByKey", query = "SELECT schemData FROM OlapSchemaData schemData WHERE key = :key"), //
})
public class OlapSchemaData implements Serializable
{
	final static Logger logger = LoggerFactory.getLogger(OlapSchemaData.class);
	
	public static enum Key
	{
		VERSION
	};

	public static final Long VERSION_CURRENT = 19L;
	public static final String VERSION_CURRENT_STRING = VERSION_CURRENT.toString();

	public static final int KEY_MAX_LENGTH = 64;
	public static final int VALUE_MAX_LENGTH = 128;

	private static final long serialVersionUID = 8509271050554020180L;

	private static final String olapDdlPersistenceUnit = "ecdsapddl";

	protected Key key;
	protected String value;

	protected Date createdAt;
	protected Date updatedAt;

	@Id
	@Enumerated(EnumType.STRING)
	@Column(name = "vkey", nullable = false, length = KEY_MAX_LENGTH)
	public Key getKey()
	{
		return this.key;
	}

	public OlapSchemaData setKey(Key key)
	{
		this.key = key;
		return this;
	}

	@Column(name = "value", nullable = false, length = VALUE_MAX_LENGTH)
	public String getValue()
	{
		return this.value;
	}

	public OlapSchemaData setValue(String value)
	{
		this.value = value;
		return this;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at", nullable = false)
	public Date getCreatedAt()
	{
		return this.createdAt;
	}

	public OlapSchemaData setCreatedAt(Date createdAt)
	{
		this.createdAt = createdAt;
		return this;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at", nullable = false)
	public Date getUpdatedAt()
	{
		return this.updatedAt;
	}

	public OlapSchemaData setUpdatedAt(Date updatedAt)
	{
		this.updatedAt = updatedAt;
		return this;
	}

	// Helpers

	public static OlapSchemaData findByKey(EntityManager em, Key key)
	{
		TypedQuery<OlapSchemaData> query = em.createNamedQuery("OlapSchemaData.findByKey", OlapSchemaData.class);
		query.setParameter("key", key);
		List<OlapSchemaData> results = query.getResultList();
		return (results.size() == 0 ? null : results.get(0));
	}

	public static boolean verifyDatabaseVersion(ICreditDistribution context)
	{
		EntityManagerFactory factory = null;
		EntityManager em = null;
		try
		{
			factory = context.createOlapEntityManagerFactory(olapDdlPersistenceUnit, null);
			em = factory.createEntityManager();
			OlapSchemaData olapSchemaData = OlapSchemaData.findByKey(em, Key.VERSION);
			if (olapSchemaData == null)
			{
				olapSchemaData = new OlapSchemaData().setKey(Key.VERSION).setValue(VERSION_CURRENT_STRING).setCreatedAt(new Date()).setUpdatedAt(new Date());

				em.getTransaction().begin();
				em.persist(olapSchemaData);
				em.getTransaction().commit();

				String message = String.format("Could not find schema version. Assuming version %s", VERSION_CURRENT_STRING);
				logger.warn(message);
				return true;
			}
			else if (Long.valueOf(olapSchemaData.getValue()).equals(VERSION_CURRENT) == false)
			{
				String message = String.format("Expected database version %s but found %s", VERSION_CURRENT_STRING, olapSchemaData.getValue());
				logger.error("=+=+=+=+=+=++=+=+=+=+=+=++=+=+=+=+=+=");
				logger.error("=+=+=+=+=+=++=+=+=+=+=+=++=+=+=+=+=+=");
				logger.error(message);
				logger.error("=+=+=+=+=+=++=+=+=+=+=+=++=+=+=+=+=+=");
				logger.error("=+=+=+=+=+=++=+=+=+=+=+=++=+=+=+=+=+=");
				return false;
			}
			else
			{
				String message = String.format("Found database version %s as expected (%s)", olapSchemaData.getValue(), VERSION_CURRENT_STRING);
				logger.info(message);
				return true;
			}
		}
		catch (Throwable tr)
		{
			logger.error("Failure verifying database", tr);
			return false;
		}
		finally
		{
			if (em != null)
				em.close();
			if (factory != null)
				factory.close();
		}
	}
}
