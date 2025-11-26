package hxc.services.ecds;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.EntityManager;

import hxc.ecds.protocol.rest.config.IConfiguration;
import hxc.services.ecds.model.Company;
import hxc.services.ecds.model.Configuration;
import hxc.services.ecds.model.Permission;
import hxc.services.ecds.model.Role;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransactionNumber;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.IConfigurationChange;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheckException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompanyInfo
{
	final static Logger logger = LoggerFactory.getLogger(CompanyInfo.class);
	private static final int numbersBatchSize = 1000;
	private static final String numberFormat = String.format("%%0%dd", Transaction.TRANSACTION_NUMBER_MAX_LENGTH);
	private static final long CONFIGURATION_EXPIRY_MS = 10 * 60 * 1000; // 10 minutes

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Company company;
	private ConcurrentMap<Integer, Set<Integer>> permissionCache = new ConcurrentHashMap<Integer, Set<Integer>>();
	private ConcurrentMap<String, IConfiguration> configurations = new ConcurrentHashMap<String, IConfiguration>();
	private ConcurrentMap<String, Date> configurationsExpiry = new ConcurrentHashMap<String, Date>();
	private ConcurrentLinkedQueue<Long> transactionNumbers = new ConcurrentLinkedQueue<Long>();
	private ICreditDistribution context;
	private List<IConfigurationChange> notificationTargets = new ArrayList<IConfigurationChange>();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public Company getCompany()
	{
		return company;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public CompanyInfo(Company company, ICreditDistribution context)
	{
		this.company = company;
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public boolean check(EntityManager em, int permissionID, int[] roleIDs)
	{
		if (roleIDs == null)
			return false;

		for (int roleID : roleIDs)
		{
			Set<Integer> roleSet = permissionCache.get(roleID);

			if (roleSet == null)
			{
				roleSet = new HashSet<Integer>();
				Role role = Role.findByID(em, roleID, company.getId());
				for (Permission permission : role.getPermissions())
				{
					roleSet.add(permission.getId());
				}
				//permissionCache.put(roleID, roleSet);
			}

			if (roleSet.contains(permissionID))
				return true;
		}

		return false;
	}

	public void flushPermissionCache(int roleID)
	{
		permissionCache.remove(roleID);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////
	@SuppressWarnings({"unchecked"})
	public <T extends IConfiguration> T getConfiguration(EntityManager em, Class<T> cls)
	{
		T configuration = (T) configurations.get(cls.getName());
		if (!hasExpired(configuration))
			return configuration;

		return em == null ? null : getConfiguration2(em, cls);
	}

	private <T extends IConfiguration> boolean hasExpired(T configuration)
	{
		if (configuration == null)
			return true;

		Date expiryTime = configurationsExpiry.get(configuration.getClass().getName());

		return expiryTime == null || expiryTime.before(new Date());
	}

	@SuppressWarnings({"unchecked"})
	private <T extends IConfiguration> T getConfiguration2(EntityManager em, Class<T> cls)
	{
		synchronized (configurations)
		{
			T configuration = (T) configurations.get(cls.getName());
			if (!hasExpired(configuration))
				return configuration;

			try
			{
				configuration = cls.newInstance();
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				logger.error("", e);
				return null;
			}

			Configuration config = Configuration.findByID(em, configuration.uid(), company.getId());
			if (config != null)
			{
				configuration = config.unpack();
				String key = cls.getName();
				configurations.put(key, configuration);
				configurationsExpiry.put(key, new Date(new Date().getTime() + CONFIGURATION_EXPIRY_MS));
				configuration.onPostLoad();
			}

			return configuration;
		 }
	}

	public <T extends IConfiguration> void setConfiguration(EntityManager em, T configuration, hxc.services.ecds.Session session) throws RuleCheckException
	{
		Configuration config = Configuration.findByID(em, configuration.uid(), company.getId());
		Configuration oldValue = null;
		if (config == null)
		{
			config = new Configuration();
			config.setCompanyID(company.getId());
			config.setId(configuration.uid());
		}
		else
		{
			oldValue = config.copy(em);
		}
		config.pack(configuration);
		AuditEntryContext auditContext = new AuditEntryContext("CONFIGURATION_UPDATE", config.getCompanyID());
		config.persist(em, oldValue, session, auditContext);
		String key = configuration.getClass().getName();
		configurations.put(key, configuration);
		configurationsExpiry.put(key, new Date(new Date().getTime() + CONFIGURATION_EXPIRY_MS));

		for (IConfigurationChange target : notificationTargets)
		{
			target.onConfigurationChanged(configuration);
		}
	}

	public String getNextTransactionNumber(int companyID)
	{
		Long next = transactionNumbers.poll();
		if (next != null)
			return formattedTransactionNumber(next);

		return getNextTransactionNumberInBatch(companyID);
	}

	private String getNextTransactionNumberInBatch(int companyID)
	{
		synchronized (transactionNumbers)
		{
			Long next = transactionNumbers.poll();
			if (next != null)
				return formattedTransactionNumber(next);
			long batchLast;
			try (EntityManagerEx em = context.getEntityManager())
			{
				try (RequiresTransaction scope = new RequiresTransaction(em))
				{
					batchLast = TransactionNumber.getNextBatch(em, numbersBatchSize, companyID);
					scope.commit();
				}
			}
			next = batchLast - numbersBatchSize;
			for (long spare = next + 1; spare < batchLast; spare++)
			{
				transactionNumbers.add(spare);
			}
			return formattedTransactionNumber(next);
		}
	}

	private String formattedTransactionNumber(long number)
	{
		return String.format(numberFormat, number);
	}

	public void registerForConfigurationChangeNotifications(IConfigurationChange target)
	{
		notificationTargets.add(target);
	}

}
