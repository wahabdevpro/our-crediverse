package hxc.services.airsim.protocol;

public enum SubscriberState
{
	inActive, // < Activition Date
	active, // < Supervision Expiry Date
	passive, // < Service Fee Expiry Date
	grace, // < Service Credit Clearance Date
	pool, // < Service Removal Date
	disconnect, // > Service Removal Date
}
