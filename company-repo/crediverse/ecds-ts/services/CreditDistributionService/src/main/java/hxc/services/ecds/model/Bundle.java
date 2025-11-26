package hxc.services.ecds.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.RestParams;
import hxc.services.ecds.util.QueryBuilder;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;

////////////////////////////////////////////////////////////////////////////////////////
//
// Bundle Table - Used for Security checks
//
///////////////////////////////////

@Table(name = "ec_bundle", uniqueConstraints = { //
		@UniqueConstraint(name = "ec_bundle_name", columnNames = { "company_id", "name" }), //
		@UniqueConstraint(name = "ec_bundle_tag", columnNames = { "company_id", "tag" }) //
})
@Entity
@NamedQueries({ //
		@NamedQuery(name = "Bundle.findByName", query = "SELECT p FROM Bundle p where name = :name and companyID = :companyID"), //
		@NamedQuery(name = "Bundle.findByID", query = "SELECT p FROM Bundle p where id = :id and companyID = :companyID"), //
		@NamedQuery(name = "Bundle.findUssdCode", query = "SELECT p FROM Bundle p where ussdCode = :ussdCode and companyID = :companyID"), //
		@NamedQuery(name = "Bundle.findSmsKeyword", query = "SELECT p FROM Bundle p LEFT JOIN p.languages l WHERE (p.smsKeyword = :smsKeyword OR l.smsKeyword = :smsKeyword) AND p.companyID = :companyID"), //
		@NamedQuery(name = "Bundle.findForUssdMenu", query = "SELECT p FROM Bundle p where state = '" + Bundle.STATE_ACTIVE
				+ "' and ussdCode is not null and companyID = :companyID order by menuPosition"), //
})

public class Bundle extends hxc.ecds.protocol.rest.Bundle implements Serializable, ICompanyData<Bundle>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = 301450240496244457L;

	public static final Permission MAY_ADD = new Permission(false, false, Permission.GROUP_BUNDLES, Permission.PERM_ADD, "May Add Bundles");
	public static final Permission MAY_UPDATE = new Permission(false, false, Permission.GROUP_BUNDLES, Permission.PERM_UPDATE, "May Update Bundles");
	public static final Permission MAY_DELETE = new Permission(false, false, Permission.GROUP_BUNDLES, Permission.PERM_DELETE, "May Delete Bundles");
	public static final Permission MAY_VIEW = new Permission(false, false, Permission.GROUP_BUNDLES, Permission.PERM_VIEW, "May View Bundles");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Additional Fields
	//
	// /////////////////////////////////
	@JsonIgnore
	protected int lastUserID;
	@JsonIgnore
	protected Date lastTime;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getId()
	{
		return id;
	}

	@Override
	public Bundle setId(int id)
	{
		this.id = id;
		return this;
	}

	@Override
	@Column(name = "company_id", nullable = false)
	public int getCompanyID()
	{
		return companyID;
	}

	@Override
	public Bundle setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Override
	@Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
	public String getName()
	{
		return name;
	}

	@Override
	public Bundle setName(String name)
	{
		this.name = name;
		return this;

	}

	@Override
	@Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LENGTH)
	public String getDescription()
	{
		return description;
	}

	@Override
	public Bundle setDescription(String description)
	{
		this.description = description;
		return this;

	}

	@Override
	@Column(name = "type", nullable = false, length = TYPE_MAX_LENGTH)
	public String getType()
	{
		return type;
	}

	@Override
	public Bundle setType(String type)
	{
		this.type = type;
		return this;
	}

	@Override
	@Column(name = "state", nullable = false, unique = false, length = 1)
	public String getState()
	{
		return state;
	}

	@Override
	public Bundle setState(String state)
	{
		this.state = state;
		return this;
	}

	@Override
	@Column(name = "tag", nullable = false, length = TAG_MAX_LENGTH)
	public String getTag()
	{
		return tag;
	}

	@Override
	public Bundle setTag(String tag)
	{
		this.tag = tag;
		return this;
	}

	@Override
	@Column(name = "price", nullable = false, scale = ICompanyData.MONEY_SCALE, precision = ICompanyData.MONEY_PRECISSION)
	public BigDecimal getPrice()
	{
		return price;
	}

	@Override
	public Bundle setPrice(BigDecimal price)
	{
		this.price = price;
		return this;
	}

	@Override
	@Column(name = "disc_pct", nullable = false, scale = ICompanyData.FINE_MONEY_SCALE, precision = ICompanyData.FINE_MONEY_PRECISSION)
	public BigDecimal getTradeDiscountPercentage()
	{
		return tradeDiscountPercentage;
	}

	@Override
	public Bundle setTradeDiscountPercentage(BigDecimal tradeDiscountPercentage)
	{
		this.tradeDiscountPercentage = tradeDiscountPercentage;
		return this;
	}

	@Override
	@Column(name = "ordinal", nullable = false)
	public int getMenuPosition()
	{
		return menuPosition;
	}

	@Override
	public Bundle setMenuPosition(int menuPosition)
	{
		this.menuPosition = menuPosition;
		return this;
	}

	@Override
	@Column(name = "ussd_code", nullable = true, length = USSD_CODE_MAX_LENGTH)
	public String getUssdCode()
	{
		return ussdCode;
	}

	@Override
	public Bundle setUssdCode(String ussdCode)
	{
		this.ussdCode = ussdCode;
		return this;
	}

	@Override
	@Column(name = "sms_keyword", nullable = true, length = SMS_KEYWORD_MAX_LENGTH)
	public String getSmsKeyword()
	{
		return smsKeyword;
	}

	@Override
	public Bundle setSmsKeyword(String smsKeyword)
	{
		this.smsKeyword = smsKeyword;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public Bundle setLastUserID(int lastUserID)
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
	@Version
	public Bundle setVersion(int version)
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
	public Bundle setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	@Override
	@OneToMany(mappedBy = "bundle", fetch = FetchType.EAGER, orphanRemoval = true)
	@SuppressWarnings({"unchecked"})
	public List<BundleLanguage> getLanguages()
	{
		return (List<BundleLanguage>) languages;
	}

	@Override
	public Bundle setLanguages(List<? extends hxc.ecds.protocol.rest.BundleLanguage> languages)
	{
		this.languages = languages;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public Bundle()
	{

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Cloning
	//
	// /////////////////////////////////

	// Make deep copy
	public Bundle copy(EntityManager em)
	{
		Bundle copy = new Bundle();
		copy.lastUserID = this.lastUserID;
		copy.lastTime = this.lastTime;
		copy.amend(em, this);
		return copy;
	}

	// Amend selected fields
	@SuppressWarnings({"unchecked"})
	public void amend(EntityManager em, hxc.ecds.protocol.rest.Bundle bundle)
	{
		this.id = bundle.getId();
		this.companyID = bundle.getCompanyID();
		this.version = bundle.getVersion();
		this.name = bundle.getName();
		this.description = bundle.getDescription();
		this.type = bundle.getType();
		this.state = bundle.getState();
		this.tag = bundle.getTag();
		this.price = bundle.getPrice();
		this.tradeDiscountPercentage = bundle.getTradeDiscountPercentage();
		this.menuPosition = bundle.getMenuPosition();
		this.smsKeyword = bundle.getSmsKeyword();
		this.ussdCode = bundle.getUssdCode();

		// Add new Languages
		List<hxc.ecds.protocol.rest.BundleLanguage> newBundleLanguages = (List<hxc.ecds.protocol.rest.BundleLanguage>) bundle.getLanguages();
		List<BundleLanguage> existingBundleLanguages = getLanguages();
		if (newBundleLanguages != null)
		{
			for (hxc.ecds.protocol.rest.BundleLanguage newBundleLanguage : newBundleLanguages)
			{
				if (!contains(existingBundleLanguages, newBundleLanguage))
				{
					BundleLanguage bl = BundleLanguage.findByIDLang(em, newBundleLanguage.getBundleID(), newBundleLanguage.getLanguage());
					if (bl == null)
					{
						bl = new BundleLanguage(newBundleLanguage).setBundle(this);
					}
					existingBundleLanguages.add(bl);
				}
				else
				{
					BundleLanguage bl = matching(this.getLanguages(), newBundleLanguage);
					if (bl != null)
						bl.amend(newBundleLanguage);
				}
			}

			// Remove unused BundleLanguages
			int index = 0;
			while (index < existingBundleLanguages.size())
			{
				if (!contains(newBundleLanguages, existingBundleLanguages.get(index)))
					existingBundleLanguages.remove(index);
				else
					index++;
			}

		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Finders
	//
	// /////////////////////////////////

	public static Bundle findByName(EntityManager em, int companyID, String name)
	{
		TypedQuery<Bundle> query = em.createNamedQuery("Bundle.findByName", Bundle.class);
		query.setParameter("companyID", companyID);
		query.setParameter("name", name);
		List<Bundle> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static Bundle findByID(EntityManager em, int id, int companyID)
	{
		TypedQuery<Bundle> query = em.createNamedQuery("Bundle.findByID", Bundle.class);
		query.setParameter("id", id);
		query.setParameter("companyID", companyID);
		List<Bundle> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static Bundle findByUssdCode(EntityManager em, String ussdCode, int companyID)
	{
		TypedQuery<Bundle> query = em.createNamedQuery("Bundle.findUssdCode", Bundle.class);
		query.setParameter("ussdCode", ussdCode);
		query.setParameter("companyID", companyID);
		List<Bundle> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static Bundle findBySmsKeyword(EntityManager em, String smsKeyword, int companyID)
	{
		TypedQuery<Bundle> query = em.createNamedQuery("Bundle.findSmsKeyword", Bundle.class);
		query.setParameter("smsKeyword", smsKeyword);
		query.setParameter("companyID", companyID);
		List<Bundle> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	public static List<Bundle> findTypesForUssdMenu(EntityManager em, int companyID)
	{
		TypedQuery<Bundle> query = em.createNamedQuery("Bundle.findForUssdMenu", Bundle.class);
		query.setParameter("companyID", companyID);
		return query.getResultList();
	}

	public static List<Bundle> findForUssdMenu(EntityManager em, int companyID)
	{
		TypedQuery<Bundle> query = em.createNamedQuery("Bundle.findForUssdMenu", Bundle.class);
		query.setParameter("companyID", companyID);
		return query.getResultList();
	}

	public static List<Bundle> findAll(EntityManager em, RestParams params, int companyID)
	{
		return QueryBuilder.getQueryResultList(em, Bundle.class, params, companyID, "name", "type", "tag");
	}

	public static Long findCount(EntityManager em, RestParams params, int companyID)
	{
		TypedQuery<Long> query = QueryBuilder.getCountQuery(em, Bundle.class, params, companyID, "name", "type", "tag");
		return query.getSingleResult();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// MRD
	//
	// /////////////////////////////////
	public static void loadMRD(EntityManager em, int companyID, Session session) throws RuleCheckException
	{
		Permission.loadMRD(em, MAY_ADD, session);
		Permission.loadMRD(em, MAY_UPDATE, session);
		Permission.loadMRD(em, MAY_DELETE, session);
		Permission.loadMRD(em, MAY_VIEW, session);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public void persist(EntityManager em, Bundle previous, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(previous);
		QueryBuilder.persist(em, previous, this, session, AuditEntry.TYPE_BUNDLE, auditEntryContext);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		QueryBuilder.remove(em, this, session, AuditEntry.TYPE_BUNDLE, auditEntryContext);
	}

	@Override
	public void validate(Bundle previous) throws RuleCheckException
	{
		RuleCheck.validate(this);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	private boolean contains(List<? extends hxc.ecds.protocol.rest.BundleLanguage> languages, hxc.ecds.protocol.rest.BundleLanguage language)
	{
		for (hxc.ecds.protocol.rest.BundleLanguage lang : languages)
		{
			if (lang.getBundleID() == language.getBundleID() && lang.getLanguage() != null && lang.getLanguage().equals(language.getLanguage()))
				return true;
		}
		return false;
	}

	private BundleLanguage matching(List<BundleLanguage> languages, hxc.ecds.protocol.rest.BundleLanguage language)
	{
		for (BundleLanguage lang : languages)
		{
			if (lang.getBundleID() == language.getBundleID() && lang.getLanguage() != null && lang.getLanguage().equals(language.getLanguage()))
				return lang;
		}
		return null;
	}

}
