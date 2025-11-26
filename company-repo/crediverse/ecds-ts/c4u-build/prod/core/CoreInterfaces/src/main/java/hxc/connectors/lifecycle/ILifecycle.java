package hxc.connectors.lifecycle;

import java.sql.SQLException;
import java.util.Date;

import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.soap.ISubscriber;

public interface ILifecycle
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Subscriptions
	//
	// /////////////////////////////////
	public abstract boolean isSubscribed(IDatabaseConnection database, ISubscriber subscriber, String serviceID, String variantID) throws SQLException;

	public abstract boolean isSubscribed(IDatabaseConnection database, ISubscriber subscriber, String serviceID) throws SQLException;

	public abstract ISubscription getSubscription(IDatabaseConnection database, ISubscriber subscriber, String serviceID, String variantID) throws SQLException;

	public abstract ISubscription[] getSubscriptions(IDatabaseConnection database, ISubscriber subscriber, String serviceID) throws SQLException;

	public abstract ISubscription[] getSubscriptions(IDatabaseConnection database, ISubscriber subscriber) throws SQLException;

	public abstract boolean addSubscription(IDatabaseConnection database, ISubscriber subscriber, String serviceID, String variantID, int state, Date nextDateTime, Date... dates) throws SQLException;

	public abstract boolean removeSubscription(IDatabaseConnection database, ISubscriber subscriber, String serviceID, String variantID) throws SQLException;

	public abstract boolean removeSubscriptions(IDatabaseConnection database, ISubscriber subscriber, String serviceID) throws SQLException;

	public abstract boolean updateSubscription(IDatabaseConnection database, ISubscription subscription) throws SQLException;

	public abstract boolean adjustLifecycle(IDatabaseConnection database, ISubscriber subscriber, String serviceID, String variantID, Boolean isBeingProcessed, Date timeStamp) throws SQLException;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Membership
	//
	// /////////////////////////////////

	public abstract boolean addMember(IDatabaseConnection database, ISubscriber owner, String serviceID, String variantID, ISubscriber member) throws SQLException;

	public abstract boolean isMember(IDatabaseConnection database, ISubscriber owner, String serviceID, String variantID, ISubscriber member) throws SQLException;

	public abstract boolean isMember(IDatabaseConnection database, ISubscriber owner, String serviceID, ISubscriber member) throws SQLException;

	public abstract boolean isMember(IDatabaseConnection database, String serviceID, ISubscriber member) throws SQLException;

	public abstract boolean removeMember(IDatabaseConnection database, ISubscriber owner, String serviceID, String variantID, ISubscriber member) throws SQLException;

	public abstract boolean removeMembers(IDatabaseConnection database, ISubscriber owner, String serviceID, String variantID) throws SQLException;

	public abstract String[] getMembers(IDatabaseConnection database, ISubscriber owner, String serviceID, String variantID) throws SQLException;

	public abstract boolean hasMembers(IDatabaseConnection database, ISubscriber owner, String serviceID, String variantID) throws SQLException;

	public abstract boolean hasMembers(IDatabaseConnection database, ISubscriber owner, String serviceID) throws SQLException;

	public abstract String[] getOwners(IDatabaseConnection database, ISubscriber member, String serviceID) throws SQLException;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Quotas
	//
	// /////////////////////////////////

	public abstract boolean addQuota(IDatabaseConnection database, IMemberQuota quota) throws SQLException;

	public abstract IMemberQuota[] getQuotas(IDatabaseConnection database, ISubscriber owner, String serviceID, String variantID, ISubscriber member) throws SQLException;

	public abstract boolean updateQuota(IDatabaseConnection database, IMemberQuota quota) throws SQLException;

	public abstract boolean removeQuota(IDatabaseConnection database, IMemberQuota quota) throws SQLException;

	public abstract IMemberQuota getQuota(IDatabaseConnection database, ISubscriber owner, String serviceID, String variantID, ISubscriber member, String quotaID) throws SQLException;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Temporal Triggers
	//
	// /////////////////////////////////
	public abstract boolean addTemporalTrigger(IDatabaseConnection database, ITemporalTrigger trigger) throws SQLException;

	public abstract ITemporalTrigger[] getTemporalTriggers(IDatabaseConnection database, String serviceID, String variantID, ISubscriber subscriberA, ISubscriber subscriberB, String keyValue)
			throws SQLException;

	public abstract boolean updateTemporalTrigger(IDatabaseConnection database, ITemporalTrigger trigger) throws SQLException;

	public abstract boolean removeTemporalTrigger(IDatabaseConnection database, ITemporalTrigger trigger) throws SQLException;

}
