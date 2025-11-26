package mibs;

import hxc.connectors.snmp.components.SnmpOidRecord;
import hxc.connectors.snmp.components.SnmpTable;

public class DISMAN_EVENT_MIBOidTable extends SnmpTable
{
	private static final long serialVersionUID = -3708614983611760083L;

	public DISMAN_EVENT_MIBOidTable()
	{
		super("DISMAN_EVENT_MIB");
		loadMib(varList);
	}

	static SnmpOidRecord varList[] = { new SnmpOidRecord("mteFailedReason", "1.3.6.1.2.1.88.2.1.6", "I"), new SnmpOidRecord("mteHotValue", "1.3.6.1.2.1.88.2.1.5", "I"),
			new SnmpOidRecord("mteHotOID", "1.3.6.1.2.1.88.2.1.4", "OI"), new SnmpOidRecord("mteHotContextName", "1.3.6.1.2.1.88.2.1.3", "S"),
			new SnmpOidRecord("mteHotTargetName", "1.3.6.1.2.1.88.2.1.2", "S"), new SnmpOidRecord("mteHotTrigger", "1.3.6.1.2.1.88.2.1.1", "S"),
			new SnmpOidRecord("mteEventSetTable", "1.3.6.1.2.1.88.1.4.4", "TA"), new SnmpOidRecord("mteEventSetEntry", "1.3.6.1.2.1.88.1.4.4.1", "EN"),
			new SnmpOidRecord("mteEventSetContextNameWildcard", "1.3.6.1.2.1.88.1.4.4.1.6", "I"), new SnmpOidRecord("mteEventSetContextName", "1.3.6.1.2.1.88.1.4.4.1.5", "S"),
			new SnmpOidRecord("mteEventSetTargetTag", "1.3.6.1.2.1.88.1.4.4.1.4", "S"), new SnmpOidRecord("mteEventSetValue", "1.3.6.1.2.1.88.1.4.4.1.3", "I"),
			new SnmpOidRecord("mteEventSetObjectWildcard", "1.3.6.1.2.1.88.1.4.4.1.2", "I"), new SnmpOidRecord("mteEventSetObject", "1.3.6.1.2.1.88.1.4.4.1.1", "OI"),
			new SnmpOidRecord("mteEventNotificationTable", "1.3.6.1.2.1.88.1.4.3", "TA"), new SnmpOidRecord("mteEventNotificationEntry", "1.3.6.1.2.1.88.1.4.3.1", "EN"),
			new SnmpOidRecord("mteEventNotificationObjects", "1.3.6.1.2.1.88.1.4.3.1.3", "S"), new SnmpOidRecord("mteEventNotificationObjectsOwner", "1.3.6.1.2.1.88.1.4.3.1.2", "S"),
			new SnmpOidRecord("mteEventNotification", "1.3.6.1.2.1.88.1.4.3.1.1", "OI"), new SnmpOidRecord("mteEventTable", "1.3.6.1.2.1.88.1.4.2", "TA"),
			new SnmpOidRecord("mteEventEntry", "1.3.6.1.2.1.88.1.4.2.1", "EN"), new SnmpOidRecord("mteEventEntryStatus", "1.3.6.1.2.1.88.1.4.2.1.5", "I"),
			new SnmpOidRecord("mteEventEnabled", "1.3.6.1.2.1.88.1.4.2.1.4", "I"), new SnmpOidRecord("mteEventActions", "1.3.6.1.2.1.88.1.4.2.1.3", "S"),
			new SnmpOidRecord("mteEventComment", "1.3.6.1.2.1.88.1.4.2.1.2", "S"), new SnmpOidRecord("mteEventName", "1.3.6.1.2.1.88.1.4.2.1.1", "S"),
			new SnmpOidRecord("mteEventFailures", "1.3.6.1.2.1.88.1.4.1", "C"), new SnmpOidRecord("mteObjectsTable", "1.3.6.1.2.1.88.1.3.1", "TA"),
			new SnmpOidRecord("mteObjectsEntry", "1.3.6.1.2.1.88.1.3.1.1", "EN"), new SnmpOidRecord("mteObjectsEntryStatus", "1.3.6.1.2.1.88.1.3.1.1.5", "I"),
			new SnmpOidRecord("mteObjectsIDWildcard", "1.3.6.1.2.1.88.1.3.1.1.4", "I"), new SnmpOidRecord("mteObjectsID", "1.3.6.1.2.1.88.1.3.1.1.3", "OI"),
			new SnmpOidRecord("mteObjectsIndex", "1.3.6.1.2.1.88.1.3.1.1.2", "G"), new SnmpOidRecord("mteObjectsName", "1.3.6.1.2.1.88.1.3.1.1.1", "S"),
			new SnmpOidRecord("mteTriggerThresholdTable", "1.3.6.1.2.1.88.1.2.6", "TA"), new SnmpOidRecord("mteTriggerThresholdEntry", "1.3.6.1.2.1.88.1.2.6.1", "EN"),
			new SnmpOidRecord("mteTriggerThresholdRisingEvent", "1.3.6.1.2.1.88.1.2.6.1.9", "S"), new SnmpOidRecord("mteTriggerThresholdRisingEventOwner", "1.3.6.1.2.1.88.1.2.6.1.8", "S"),
			new SnmpOidRecord("mteTriggerThresholdObjects", "1.3.6.1.2.1.88.1.2.6.1.7", "S"), new SnmpOidRecord("mteTriggerThresholdObjectsOwner", "1.3.6.1.2.1.88.1.2.6.1.6", "S"),
			new SnmpOidRecord("mteTriggerThresholdDeltaFallingEvent", "1.3.6.1.2.1.88.1.2.6.1.15", "S"),
			new SnmpOidRecord("mteTriggerThresholdDeltaFallingEventOwner", "1.3.6.1.2.1.88.1.2.6.1.14", "S"), new SnmpOidRecord("mteTriggerThresholdDeltaFalling", "1.3.6.1.2.1.88.1.2.6.1.5", "I"),
			new SnmpOidRecord("mteTriggerThresholdDeltaRisingEvent", "1.3.6.1.2.1.88.1.2.6.1.13", "S"), new SnmpOidRecord("mteTriggerThresholdDeltaRising", "1.3.6.1.2.1.88.1.2.6.1.4", "I"),
			new SnmpOidRecord("mteTriggerThresholdFalling", "1.3.6.1.2.1.88.1.2.6.1.3", "I"), new SnmpOidRecord("mteTriggerThresholdDeltaRisingEventOwner", "1.3.6.1.2.1.88.1.2.6.1.12", "S"),
			new SnmpOidRecord("mteTriggerThresholdRising", "1.3.6.1.2.1.88.1.2.6.1.2", "I"), new SnmpOidRecord("mteTriggerThresholdFallingEvent", "1.3.6.1.2.1.88.1.2.6.1.11", "S"),
			new SnmpOidRecord("mteTriggerThresholdStartup", "1.3.6.1.2.1.88.1.2.6.1.1", "I"), new SnmpOidRecord("mteTriggerThresholdFallingEventOwner", "1.3.6.1.2.1.88.1.2.6.1.10", "S"),
			new SnmpOidRecord("mteTriggerBooleanTable", "1.3.6.1.2.1.88.1.2.5", "TA"), new SnmpOidRecord("mteTriggerBooleanEntry", "1.3.6.1.2.1.88.1.2.5.1", "EN"),
			new SnmpOidRecord("mteTriggerBooleanEventOwner", "1.3.6.1.2.1.88.1.2.5.1.6", "S"), new SnmpOidRecord("mteTriggerBooleanObjects", "1.3.6.1.2.1.88.1.2.5.1.5", "S"),
			new SnmpOidRecord("mteTriggerBooleanObjectsOwner", "1.3.6.1.2.1.88.1.2.5.1.4", "S"), new SnmpOidRecord("mteTriggerBooleanStartup", "1.3.6.1.2.1.88.1.2.5.1.3", "I"),
			new SnmpOidRecord("mteTriggerBooleanValue", "1.3.6.1.2.1.88.1.2.5.1.2", "I"), new SnmpOidRecord("mteTriggerBooleanComparison", "1.3.6.1.2.1.88.1.2.5.1.1", "I"),
			new SnmpOidRecord("mteTriggerBooleanEvent", "1.3.6.1.2.1.88.1.2.5.1.7", "S"), new SnmpOidRecord("mteTriggerExistenceTable", "1.3.6.1.2.1.88.1.2.4", "TA"),
			new SnmpOidRecord("mteTriggerExistenceEntry", "1.3.6.1.2.1.88.1.2.4.1", "EN"), new SnmpOidRecord("mteTriggerExistenceEvent", "1.3.6.1.2.1.88.1.2.4.1.6", "S"),
			new SnmpOidRecord("mteTriggerExistenceEventOwner", "1.3.6.1.2.1.88.1.2.4.1.5", "S"), new SnmpOidRecord("mteTriggerExistenceObjects", "1.3.6.1.2.1.88.1.2.4.1.4", "S"),
			new SnmpOidRecord("mteTriggerExistenceObjectsOwner", "1.3.6.1.2.1.88.1.2.4.1.3", "S"), new SnmpOidRecord("mteTriggerExistenceStartup", "1.3.6.1.2.1.88.1.2.4.1.2", "S"),
			new SnmpOidRecord("mteTriggerExistenceTest", "1.3.6.1.2.1.88.1.2.4.1.1", "S"), new SnmpOidRecord("mteTriggerDeltaTable", "1.3.6.1.2.1.88.1.2.3", "TA"),
			new SnmpOidRecord("mteTriggerDeltaEntry", "1.3.6.1.2.1.88.1.2.3.1", "EN"), new SnmpOidRecord("mteTriggerDeltaDiscontinuityIDType", "1.3.6.1.2.1.88.1.2.3.1.3", "I"),
			new SnmpOidRecord("mteTriggerDeltaDiscontinuityIDWildcard", "1.3.6.1.2.1.88.1.2.3.1.2", "I"), new SnmpOidRecord("mteTriggerDeltaDiscontinuityID", "1.3.6.1.2.1.88.1.2.3.1.1", "OI"),
			new SnmpOidRecord("mteTriggerTable", "1.3.6.1.2.1.88.1.2.2", "TA"), new SnmpOidRecord("mteTriggerEntry", "1.3.6.1.2.1.88.1.2.2.1", "EN"),
			new SnmpOidRecord("mteTriggerContextName", "1.3.6.1.2.1.88.1.2.2.1.9", "S"), new SnmpOidRecord("mteTriggerTargetTag", "1.3.6.1.2.1.88.1.2.2.1.8", "S"),
			new SnmpOidRecord("mteTriggerValueIDWildcard", "1.3.6.1.2.1.88.1.2.2.1.7", "I"), new SnmpOidRecord("mteTriggerEntryStatus", "1.3.6.1.2.1.88.1.2.2.1.15", "I"),
			new SnmpOidRecord("mteTriggerValueID", "1.3.6.1.2.1.88.1.2.2.1.6", "OI"), new SnmpOidRecord("mteTriggerEnabled", "1.3.6.1.2.1.88.1.2.2.1.14", "I"),
			new SnmpOidRecord("mteTriggerSampleType", "1.3.6.1.2.1.88.1.2.2.1.5", "I"), new SnmpOidRecord("mteTriggerObjects", "1.3.6.1.2.1.88.1.2.2.1.13", "S"),
			new SnmpOidRecord("mteTriggerTest", "1.3.6.1.2.1.88.1.2.2.1.4", "S"), new SnmpOidRecord("mteTriggerObjectsOwner", "1.3.6.1.2.1.88.1.2.2.1.12", "S"),
			new SnmpOidRecord("mteTriggerComment", "1.3.6.1.2.1.88.1.2.2.1.3", "S"), new SnmpOidRecord("mteTriggerFrequency", "1.3.6.1.2.1.88.1.2.2.1.11", "G"),
			new SnmpOidRecord("mteTriggerName", "1.3.6.1.2.1.88.1.2.2.1.2", "S"), new SnmpOidRecord("mteOwner", "1.3.6.1.2.1.88.1.2.2.1.1", "S"),
			new SnmpOidRecord("mteTriggerContextNameWildcard", "1.3.6.1.2.1.88.1.2.2.1.10", "I"), new SnmpOidRecord("mteTriggerFailures", "1.3.6.1.2.1.88.1.2.1", "C"),
			new SnmpOidRecord("mteResourceSampleInstanceLacks", "1.3.6.1.2.1.88.1.1.5", "C"), new SnmpOidRecord("mteResourceSampleInstancesHigh", "1.3.6.1.2.1.88.1.1.4", "G"),
			new SnmpOidRecord("mteResourceSampleInstances", "1.3.6.1.2.1.88.1.1.3", "G"), new SnmpOidRecord("mteResourceSampleInstanceMaximum", "1.3.6.1.2.1.88.1.1.2", "G"),
			new SnmpOidRecord("mteResourceSampleMinimum", "1.3.6.1.2.1.88.1.1.1", "I"), new SnmpOidRecord("dismanEventResourceGroup", "1.3.6.1.2.1.88.3.2.1", "OBG"),
			new SnmpOidRecord("dismanEventNotificationObjectGroup", "1.3.6.1.2.1.88.3.2.5", "OBG"), new SnmpOidRecord("dismanEventNotificationGroup", "1.3.6.1.2.1.88.3.2.6", "OBG"),
			new SnmpOidRecord("mteTriggerFailure", "1.3.6.1.2.1.88.2.0.4", "NT"), new SnmpOidRecord("dismanEventEventGroup", "1.3.6.1.2.1.88.3.2.4", "OBG"),
			new SnmpOidRecord("dismanEventObjectsGroup", "1.3.6.1.2.1.88.3.2.3", "OBG"), new SnmpOidRecord("mteEventSetFailure", "1.3.6.1.2.1.88.2.0.5", "NT"),
			new SnmpOidRecord("mteTriggerRising", "1.3.6.1.2.1.88.2.0.2", "NT"), new SnmpOidRecord("dismanEventTriggerGroup", "1.3.6.1.2.1.88.3.2.2", "OBG"),
			new SnmpOidRecord("mteTriggerFalling", "1.3.6.1.2.1.88.2.0.3", "NT"), new SnmpOidRecord("mteTriggerFired", "1.3.6.1.2.1.88.2.0.1", "NT") };
}
