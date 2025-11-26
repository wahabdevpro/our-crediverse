package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * DeleteSubscriberRequest
 * 
 * The message DeleteSubscriber performs a deletion of subscriber and account. Details of the subscriber and account are reported in the response. If a master subscriber with subordinates is to be
 * deleted all subordinates must have been deleted first (for clean-up and account history purposes). When a single/master subscriber is deleted, its account and all the data connected to the account
 * (dedicated accounts, accumulators, and so on) are deleted. For a subordinate subscriber, only the subscriber part and its related data are deleted. If it is of interest to see the relations between
 * instansiated resources (UA/DA) and an instantiated Offer when deleting a subscriber, the recommendation would be to first perform a DeleteOffers operation (to get the capabilities which the DA
 * share with the Offer), and, after that the DeleteSubscriber operation.
 */
@XmlRpcMethod(name = "DeleteSubscriber")
public class DeleteSubscriberRequest
{
	public DeleteSubscriberRequestMember member;

	public DeleteSubscriberRequest()
	{
		member = new DeleteSubscriberRequestMember();
	}
}
