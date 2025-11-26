package hxc.services.ecds.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.Version;

import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.utils.calendar.DateTime;

////////////////////////////////////////////////////////////////////////////////////////
//
// SmsQueue Table - Used for pending reward transactions
//
///////////////////////////////////

@Table(name = "ec_smsq", indexes = { //
		@Index(name = "ec_smsq_ix1", columnList = "company_id,start_second,end_second"), //
})
@Entity
@NamedQueries({ //
		@NamedQuery(name = "SmsQueue.findUnsent", query = "select s from SmsQueue s where beginSecondOfDay <= :secondOfDay and :secondOfDay <= endSecondOfDay and companyID = :companyID"), //
})

public class SmsQueue implements Serializable, ICompanyData<SmsQueue>
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final int LANGUAGE3_LENGTH = 3;
	private static final int SMS_TTL_DAYS = 2;

	private static final long serialVersionUID = -766139952471673745L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int id;
	protected int companyID;
	protected int version;
	protected String mobileNumber;
	protected String language3;
	protected String notification;
	protected int beginSecondOfDay;
	protected int endSecondOfDay;
	protected int attemptsLeft;
	protected Date expiryTime;
	protected int lastUserID;
	protected Date lastTime;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int getId()
	{
		return id;
	}

	public SmsQueue setId(int id)
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

	public SmsQueue setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Column(name = "msisdn", nullable = false, length = Agent.PHONE_NUMBER_MAX_LENGTH)
	public String getMobileNumber()
	{
		return mobileNumber;
	}

	public SmsQueue setMobileNumber(String mobileNumber)
	{
		this.mobileNumber = mobileNumber;
		return this;
	}

	@Column(name = "lang3", nullable = false, length = LANGUAGE3_LENGTH)
	public String getLanguage3()
	{
		return language3;
	}

	public SmsQueue setLanguage3(String language3)
	{
		this.language3 = language3;
		return this;
	}

	@Column(name = "notification", nullable = false, columnDefinition = "TEXT")
	public String getNotification()
	{
		return notification;
	}

	public SmsQueue setNotification(String notification)
	{
		this.notification = notification;
		return this;
	}

	@Column(name = "start_second", nullable = false)
	public int getBeginSecondOfDay()
	{
		return beginSecondOfDay;
	}

	public SmsQueue setBeginSecondOfDay(int beginSecondOfDay)
	{
		this.beginSecondOfDay = beginSecondOfDay;
		return this;
	}

	@Column(name = "end_second", nullable = false)
	public int getEndSecondOfDay()
	{
		return endSecondOfDay;
	}

	public SmsQueue setEndSecondOfDay(int endSecondOfDay)
	{
		this.endSecondOfDay = endSecondOfDay;
		return this;
	}

	@Column(name = "attempts_left", nullable = false)
	public int getAttemptsLeft()
	{
		return attemptsLeft;
	}

	public SmsQueue setAttemptsLeft(int attemptsLeft)
	{
		this.attemptsLeft = attemptsLeft;
		return this;
	}

	@Column(name = "expiry_time", nullable = true)
	public Date getExpiryTime()
	{
		return expiryTime;
	}

	public SmsQueue setExpiryTime(Date expiryTime)
	{
		this.expiryTime = expiryTime;
		return this;
	}

	@Override
	@Version
	public int getVersion()
	{
		return version;
	}

	public SmsQueue setVersion(int version)
	{
		this.version = version;
		return this;
	}

	@Override
	@Column(name = "lm_userid", nullable = false)
	public int getLastUserID()
	{
		return lastUserID;
	}

	@Override
	public SmsQueue setLastUserID(int lastUserID)
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
	public SmsQueue setLastTime(Date lastTime)
	{
		this.lastTime = lastTime;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// ////////////////////////////////
	public SmsQueue()
	{

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Finders
	//
	// /////////////////////////////////
	public static List<SmsQueue> findUnsent(EntityManager em, int secondOfDay, int companyID, int maxResults)
	{
		TypedQuery<SmsQueue> query = em.createNamedQuery("SmsQueue.findUnsent", SmsQueue.class);
		query.setParameter("secondOfDay", secondOfDay);
		query.setParameter("companyID", companyID);
		query.setMaxResults(maxResults);
		return query.getResultList();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@Override
	public void persist(EntityManager em, SmsQueue existing, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		validate(existing);
		em.persist(this);
	}

	@Override
	public void remove(EntityManager em, Session session, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		em.remove(this);
	}

	@Override
	public void validate(SmsQueue previous) throws RuleCheckException
	{
		RuleCheck.notEmpty("mobileNumber", mobileNumber, Agent.PHONE_NUMBER_MAX_LENGTH);
		RuleCheck.notEmpty("language3", language3, LANGUAGE3_LENGTH);
		RuleCheck.notLess("beginSecondOfDay", beginSecondOfDay, 0);
		RuleCheck.notEmpty("notification", notification);
		RuleCheck.notLess("endSecondOfDay", endSecondOfDay, beginSecondOfDay);
		RuleCheck.notMore("endSecondOfDay", endSecondOfDay, 24 * 60 * 60);
		RuleCheck.notLess("attemptsLeft", attemptsLeft, 0);

		if (previous != null)
		{
			RuleCheck.noChange("id", id, previous.id);
			RuleCheck.noChange("companyID", companyID, previous.companyID);
		}
	}

	public static void sendSMS(EntityManager em, String msisdn, String languageCode3, String text, //
			ICreditDistribution context, Date smsStartTimeOfDay, Date smsEndTimeOfDay, int companyID, int agentID)
	{
		// Check time of day
		DateTime now = DateTime.getNow();
		int seconds = DateTime.getNow().getSecondsSinceMidnight();
		int start = new DateTime(smsStartTimeOfDay).getSecondsSinceMidnight();
		int end = new DateTime(smsEndTimeOfDay).getSecondsSinceMidnight();

		// Send immediately if allowed
		if (seconds >= start && seconds <= end)
		{
			context.sendSMS(msisdn, languageCode3, text);
			return;
		}

		// Queue for transmission later
		SmsQueue queueEntry = new SmsQueue() //
				.setCompanyID(companyID) //
				.setMobileNumber(msisdn) //
				.setLanguage3(languageCode3) //
				.setNotification(text) //
				.setBeginSecondOfDay(start) //
				.setEndSecondOfDay(end) //
				.setAttemptsLeft(1) //
				.setExpiryTime(now.addDays(SMS_TTL_DAYS)) //
				.setLastTime(now) //
				.setLastUserID(-agentID) //
		;

		try (RequiresTransaction ts = new RequiresTransaction(em))
		{
			em.persist(queueEntry);
			ts.commit();
		}

	}

}
