package hxc.services.ecds.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

////////////////////////////////////////////////////////////////////////////////////////
//
// BundleLanguage Table - Used for Security checks
//
///////////////////////////////////

@Table(name = "ec_bundle_lang")
@Entity
@NamedQueries({ //
		@NamedQuery(name = "BundleLanguage.findByIDLang", query = "SELECT p FROM BundleLanguage p where bundleID = :bundleID and language = :language"), //
})
public class BundleLanguage extends hxc.ecds.protocol.rest.BundleLanguage implements Serializable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long serialVersionUID = -6440638468608766204L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Additional Fields
	//
	// /////////////////////////////////
	@JsonIgnore
	private Bundle bundle;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public BundleLanguage()
	{

	}

	public BundleLanguage(hxc.ecds.protocol.rest.BundleLanguage language)
	{
		this.bundleID = language.getBundleID();
		this.language = language.getLanguage();
		this.name = language.getName();
		this.description = language.getDescription();
		this.type = language.getType();
		this.smsKeyword = language.getSmsKeyword();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Cloning
	//
	// /////////////////////////////////

	@Override
	@Version
	public int getVersion()
	{
		return version;
	}

	@Override
	public BundleLanguage setVersion(int version)
	{
		this.version = version;
		return this;
	}

	@Override
	@Id
	@Column(name = "bundle_id", nullable = false)
	public int getBundleID()
	{
		return bundleID;
	}

	@Override
	public BundleLanguage setBundleID(int bundleID)
	{
		this.bundleID = bundleID;
		return this;
	}

	@Override
	@Id
	@Column(name = "language", nullable = false, length = LANGUAGE_MAX_LENGTH)
	public String getLanguage()
	{
		return language;
	}

	@Override
	public BundleLanguage setLanguage(String language)
	{
		this.language = language;
		return this;
	}

	@Override
	@Column(name = "name", nullable = false, length = Bundle.NAME_MAX_LENGTH)
	public String getName()
	{
		return name;
	}

	@Override
	public BundleLanguage setName(String name)
	{
		this.name = name;
		return this;
	}

	@Override
	@Column(name = "description", nullable = false, length = Bundle.DESCRIPTION_MAX_LENGTH)
	public String getDescription()
	{
		return description;
	}

	@Override
	public BundleLanguage setDescription(String description)
	{
		this.description = description;
		return this;
	}

	@Override
	@Column(name = "type", nullable = false, length = Bundle.TYPE_MAX_LENGTH)
	public String getType()
	{
		return type;
	}

	@Override
	public BundleLanguage setType(String type)
	{
		this.type = type;
		return this;
	}
		
	@Override
	@Column(name = "sms_keyword", nullable = true, length = SMS_KEYWORD_MAX_LENGTH)
	public String getSmsKeyword()
	{
		return smsKeyword;
	}

	@Override
	public BundleLanguage setSmsKeyword(String smsKeyword)
	{
		this.smsKeyword = smsKeyword;
		return this;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "bundle_id", foreignKey = @ForeignKey(name = "FK_Bundle_Lang"))
	@MapsId("bundleID")
	public Bundle getBundle()
	{
		return bundle;
	}

	public BundleLanguage setBundle(Bundle bundle)
	{
		this.bundle = bundle;
		return this;
	}

	// Make deep copy
	public BundleLanguage copy()
	{
		BundleLanguage copy = new BundleLanguage();
		copy.amend(this);
		return copy;
	}

	// Amend selected fields
	public void amend(hxc.ecds.protocol.rest.BundleLanguage bundleLanguage)
	{
		this.bundleID = bundleLanguage.getBundleID();
		this.language = bundleLanguage.getLanguage();
		this.name = bundleLanguage.getName();
		this.description = bundleLanguage.getDescription();
		this.type = bundleLanguage.getType();
		this.smsKeyword = bundleLanguage.getSmsKeyword();
	}

	public static BundleLanguage findByIDLang(EntityManager em, int bundleID, String language)
	{
		if (bundleID <= 0)
			return null;

		TypedQuery<BundleLanguage> query = em.createNamedQuery("BundleLanguage.findByIDLang", BundleLanguage.class);
		query.setParameter("bundleID", bundleID);
		query.setParameter("language", language);
		List<BundleLanguage> results = query.getResultList();
		return results.size() == 0 ? null : results.get(0);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////

}
