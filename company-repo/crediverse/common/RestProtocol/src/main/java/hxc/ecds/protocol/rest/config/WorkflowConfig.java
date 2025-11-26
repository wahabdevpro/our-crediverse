package hxc.ecds.protocol.rest.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;
import hxc.ecds.protocol.rest.WorkItem;

public class WorkflowConfig implements IConfiguration
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// ///////////////////////////////// Actor
	private static final long serialVersionUID = 4210031423700195831L;

	public static final String ACTOR = "{Actor}";
	public static final String OWNER = "{Owner}";
	public static final String RECIPIENT = "{Recipient}";
	public static final String ACTION = "{Action}";
	public static final String TYPE = "{Type}";
	public static final String DESCRIPTION = "{Description}";
	public static final String OTP = "{OTP}";
	public static final String OTP_EXPIRY_DATETIME = "{OTP_EXPIRY_DATETIME}";

	private static Phrase[] notificationFields = new Phrase[] { //
			Phrase.en(ACTOR), Phrase.en(OWNER), Phrase.en(RECIPIENT), Phrase.en(ACTION), Phrase.en(TYPE), Phrase.en(DESCRIPTION) };

	private static Phrase[] actorOtpNotificationFields = new Phrase[] { //
			Phrase.en(ACTOR), Phrase.en(OWNER), Phrase.en(ACTION), Phrase.en(TYPE), Phrase.en(DESCRIPTION), Phrase.en(OTP), Phrase.en(OTP_EXPIRY_DATETIME)};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int version;
	private int workItemRetentionDays = 60;

	protected Phrase actorNotification = Phrase.en("You have " + ACTION + " " + TYPE + " '" + DESCRIPTION + "' for " + RECIPIENT);
	protected Phrase ownerNotification = Phrase.en(ACTOR + " has " + ACTION + " " + TYPE + " '" + DESCRIPTION + "' for " + RECIPIENT);
	protected Phrase recipientNotification = Phrase.en(ACTOR + " has " + ACTION + " " + TYPE + " '" + DESCRIPTION + "' for " + RECIPIENT);

	protected Phrase actorOtpNotification = Phrase.en("Your One Time Pin is " + OTP + " valid until " + OTP_EXPIRY_DATETIME + " for " + ACTION + " " + TYPE + " '" + DESCRIPTION + "' requested by " + OWNER);
	protected int actorOtpExpiry = 10 * 60;

	private Map<String, Phrase> actions = new HashMap<String, Phrase>();
	private Map<String, Phrase> types = new HashMap<String, Phrase>();
	private Phrase forPermission = Phrase.en("Web-Users with the required permission");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@Override
	public long uid()
	{
		return serialVersionUID;
	}

	public int getWorkItemRetentionDays()
	{
		return workItemRetentionDays;
	}

	public WorkflowConfig setWorkItemRetentionDays(int workItemRetentionDays)
	{
		this.workItemRetentionDays = workItemRetentionDays;
		return this;
	}

	public Phrase[] listNotificationFields()
	{
		return notificationFields;
	}

	public Phrase[] listActorOtpNotificationFields()
	{
		return actorOtpNotificationFields;
	}

	public Phrase getActorNotification()
	{
		return actorNotification;
	}

	public WorkflowConfig setActorNotification(Phrase actorNotification)
	{
		this.actorNotification = actorNotification;
		return this;
	}

	public Phrase getOwnerNotification()
	{
		return ownerNotification;
	}

	public WorkflowConfig setOwnerNotification(Phrase ownerNotification)
	{
		this.ownerNotification = ownerNotification;
		return this;
	}

	public Phrase getRecipientNotification()
	{
		return recipientNotification;
	}

	public WorkflowConfig setRecipientNotification(Phrase recipientNotification)
	{
		this.recipientNotification = recipientNotification;
		return this;
	}

	public Phrase getActorOtpNotification()
	{
		return actorOtpNotification;
	}

	public WorkflowConfig setActorOtpNotification(Phrase actorOtpNotification)
	{
		this.actorOtpNotification = actorOtpNotification;
		return this;
	}

	public int getActorOtpExpiry()
	{
		return this.actorOtpExpiry;
	}

	public WorkflowConfig setActorOtpExpiry( int actorOtpExpiry )
	{
		this.actorOtpExpiry = actorOtpExpiry;
		return this;
	}


	public Map<String, Phrase> getActions()
	{
		return actions;
	}

	public WorkflowConfig setActions(Map<String, Phrase> actions)
	{
		this.actions = actions;
		return this;
	}

	public Map<String, Phrase> getTypes()
	{
		return types;
	}

	public WorkflowConfig setTypes(Map<String, Phrase> types)
	{
		this.types = types;
		return this;
	}
	
	public Phrase getForPermission()
	{
		return forPermission;
	}

	public WorkflowConfig setForPermission(Phrase forPermission)
	{
		this.forPermission = forPermission;
		return this;
	}

	@Override
	public int getVersion()
	{
		return version;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public WorkflowConfig()
	{
		initialize();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Post-Load fix up
	//
	// /////////////////////////////////
	@Override
	public void onPostLoad()
	{
		initialize();

		WorkflowConfig template = new WorkflowConfig();

		if (workItemRetentionDays == 0)
			workItemRetentionDays = template.workItemRetentionDays;

		if (Phrase.nullOrEmpty(actorNotification))
			this.actorNotification = template.actorNotification;

		if (Phrase.nullOrEmpty(ownerNotification))
			this.ownerNotification = template.ownerNotification;

		if (Phrase.nullOrEmpty(recipientNotification))
			this.recipientNotification = template.recipientNotification;
		
		if (Phrase.nullOrEmpty(forPermission))
			this.forPermission = template.forPermission;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Initialisation
	//
	// /////////////////////////////////
	private synchronized void initialize()
	{
		if (types == null)
			types = new HashMap<String, Phrase>();

		en(types, WorkItem.TYPE_AUTHENTICATION_REQUEST, "Approval");
		en(types, WorkItem.TYPE_REVERSAL_REQUEST, "Reversal");
		en(types, WorkItem.TYPE_SCHEDULED_REPORT, "Report");
		en(types, WorkItem.TYPE_EXECUTE, "Transaction");

		if (actions == null)
			actions = new HashMap<String, Phrase>();

		en(actions, WorkItem.STATE_NEW, "Created");
		en(actions, WorkItem.STATE_IN_PROGRESS, "Started");
		en(actions, WorkItem.STATE_ON_HOLD, "Placed on Hold");
		en(actions, WorkItem.STATE_COMPLETED, "Completed");
		en(actions, WorkItem.STATE_CANCELLED, "Cancelled");
		en(actions, WorkItem.STATE_DECLINED, "Declined");
		en(actions, WorkItem.STATE_FAILED, "Failed");
	}

	private void en(Map<String, Phrase> phrases, String key, String english)
	{
		Phrase phrase = phrases.get(key);
		if (phrase == null)
			phrases.put(key, new Phrase().eng(english));
		else if (!phrase.has(Phrase.ENG))
			phrase.set(Phrase.ENG, english);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator() //
				.notLess("workItemRetentionDays", workItemRetentionDays, 30) //
				.validExpandableText("actorNotification", actorNotification, notificationFields) //
				.validExpandableText("ownerNotification", ownerNotification, notificationFields) //
				.validExpandableText("recipientNotification", recipientNotification, notificationFields) //
				.validExpandableText("actorOtpNotification", actorOtpNotification, actorOtpNotificationFields) //
				.notLess("actorOtpExpiry", actorOtpExpiry, 1) //
		;

		return validator.toList();
	}

}
