package mibs;

import hxc.connectors.snmp.components.SnmpOidRecord;
import hxc.connectors.snmp.components.SnmpTable;

public class HOST_RESOURCES_MIBOidTable extends SnmpTable
{

	private static final long serialVersionUID = -8810778001349856L;

	public HOST_RESOURCES_MIBOidTable()
	{
		super("HOST_RESOURCES_MIB");
		loadMib(varList);
	}

	static SnmpOidRecord varList[] = { new SnmpOidRecord("hrSWInstalledTable", "1.3.6.1.2.1.25.6.3", "TA"), new SnmpOidRecord("hrSWInstalledEntry", "1.3.6.1.2.1.25.6.3.1", "EN"),
			new SnmpOidRecord("hrSWInstalledDate", "1.3.6.1.2.1.25.6.3.1.5", "S"), new SnmpOidRecord("hrSWInstalledType", "1.3.6.1.2.1.25.6.3.1.4", "I"),
			new SnmpOidRecord("hrSWInstalledID", "1.3.6.1.2.1.25.6.3.1.3", "OI"), new SnmpOidRecord("hrSWInstalledName", "1.3.6.1.2.1.25.6.3.1.2", "S"),
			new SnmpOidRecord("hrSWInstalledIndex", "1.3.6.1.2.1.25.6.3.1.1", "I"), new SnmpOidRecord("hrSWInstalledLastUpdateTime", "1.3.6.1.2.1.25.6.2", "T"),
			new SnmpOidRecord("hrSWInstalledLastChange", "1.3.6.1.2.1.25.6.1", "T"), new SnmpOidRecord("hrSWRunPerfTable", "1.3.6.1.2.1.25.5.1", "TA"),
			new SnmpOidRecord("hrSWRunPerfEntry", "1.3.6.1.2.1.25.5.1.1", "EN"), new SnmpOidRecord("hrSWRunPerfMem", "1.3.6.1.2.1.25.5.1.1.2", "I"),
			new SnmpOidRecord("hrSWRunPerfCPU", "1.3.6.1.2.1.25.5.1.1.1", "I"), new SnmpOidRecord("hrSWRunTable", "1.3.6.1.2.1.25.4.2", "TA"),
			new SnmpOidRecord("hrSWRunEntry", "1.3.6.1.2.1.25.4.2.1", "EN"), new SnmpOidRecord("hrSWRunType", "1.3.6.1.2.1.25.4.2.1.6", "I"),
			new SnmpOidRecord("hrSWRunParameters", "1.3.6.1.2.1.25.4.2.1.5", "S"), new SnmpOidRecord("hrSWRunPath", "1.3.6.1.2.1.25.4.2.1.4", "S"),
			new SnmpOidRecord("hrSWRunID", "1.3.6.1.2.1.25.4.2.1.3", "OI"), new SnmpOidRecord("hrSWRunName", "1.3.6.1.2.1.25.4.2.1.2", "S"),
			new SnmpOidRecord("hrSWRunIndex", "1.3.6.1.2.1.25.4.2.1.1", "I"), new SnmpOidRecord("hrSWRunStatus", "1.3.6.1.2.1.25.4.2.1.7", "I"),
			new SnmpOidRecord("hrSWOSIndex", "1.3.6.1.2.1.25.4.1", "I"), new SnmpOidRecord("hrDiskStorageTable", "1.3.6.1.2.1.25.3.6", "TA"),
			new SnmpOidRecord("hrDiskStorageEntry", "1.3.6.1.2.1.25.3.6.1", "EN"), new SnmpOidRecord("hrDiskStorageCapacity", "1.3.6.1.2.1.25.3.6.1.4", "I"),
			new SnmpOidRecord("hrDiskStorageRemoveble", "1.3.6.1.2.1.25.3.6.1.3", "I"), new SnmpOidRecord("hrDiskStorageMedia", "1.3.6.1.2.1.25.3.6.1.2", "I"),
			new SnmpOidRecord("hrDiskStorageAccess", "1.3.6.1.2.1.25.3.6.1.1", "I"), new SnmpOidRecord("hrPrinterTable", "1.3.6.1.2.1.25.3.5", "TA"),
			new SnmpOidRecord("hrPrinterEntry", "1.3.6.1.2.1.25.3.5.1", "EN"), new SnmpOidRecord("hrPrinterDetectedErrorState", "1.3.6.1.2.1.25.3.5.1.2", "S"),
			new SnmpOidRecord("hrPrinterStatus", "1.3.6.1.2.1.25.3.5.1.1", "I"), new SnmpOidRecord("hrNetworkTable", "1.3.6.1.2.1.25.3.4", "TA"),
			new SnmpOidRecord("hrNetworkEntry", "1.3.6.1.2.1.25.3.4.1", "EN"), new SnmpOidRecord("hrNetworkIfIndex", "1.3.6.1.2.1.25.3.4.1.1", "I"),
			new SnmpOidRecord("hrProcessorTable", "1.3.6.1.2.1.25.3.3", "TA"), new SnmpOidRecord("hrProcessorEntry", "1.3.6.1.2.1.25.3.3.1", "EN"),
			new SnmpOidRecord("hrProcessorLoad", "1.3.6.1.2.1.25.3.3.1.2", "I"), new SnmpOidRecord("hrProcessorFrwID", "1.3.6.1.2.1.25.3.3.1.1", "OI"),
			new SnmpOidRecord("hrDeviceTable", "1.3.6.1.2.1.25.3.2", "TA"), new SnmpOidRecord("hrDeviceEntry", "1.3.6.1.2.1.25.3.2.1", "EN"),
			new SnmpOidRecord("hrDeviceErrors", "1.3.6.1.2.1.25.3.2.1.6", "C"), new SnmpOidRecord("hrDeviceStatus", "1.3.6.1.2.1.25.3.2.1.5", "I"),
			new SnmpOidRecord("hrDeviceID", "1.3.6.1.2.1.25.3.2.1.4", "OI"), new SnmpOidRecord("hrDeviceDescr", "1.3.6.1.2.1.25.3.2.1.3", "S"),
			new SnmpOidRecord("hrDeviceType", "1.3.6.1.2.1.25.3.2.1.2", "OI"), new SnmpOidRecord("hrDeviceIndex", "1.3.6.1.2.1.25.3.2.1.1", "I"),
			new SnmpOidRecord("hrFSTable", "1.3.6.1.2.1.25.3.8", "TA"), new SnmpOidRecord("hrFSEntry", "1.3.6.1.2.1.25.3.8.1", "EN"),
			new SnmpOidRecord("hrFSLastPartialBackupDate", "1.3.6.1.2.1.25.3.8.1.9", "S"), new SnmpOidRecord("hrFSLastFullBackupDate", "1.3.6.1.2.1.25.3.8.1.8", "S"),
			new SnmpOidRecord("hrFSStorageIndex", "1.3.6.1.2.1.25.3.8.1.7", "I"), new SnmpOidRecord("hrFSBootable", "1.3.6.1.2.1.25.3.8.1.6", "I"),
			new SnmpOidRecord("hrFSAccess", "1.3.6.1.2.1.25.3.8.1.5", "I"), new SnmpOidRecord("hrFSType", "1.3.6.1.2.1.25.3.8.1.4", "OI"),
			new SnmpOidRecord("hrFSRemoteMountPoint", "1.3.6.1.2.1.25.3.8.1.3", "S"), new SnmpOidRecord("hrFSMountPoint", "1.3.6.1.2.1.25.3.8.1.2", "S"),
			new SnmpOidRecord("hrFSIndex", "1.3.6.1.2.1.25.3.8.1.1", "I"), new SnmpOidRecord("hrPartitionTable", "1.3.6.1.2.1.25.3.7", "TA"),
			new SnmpOidRecord("hrPartitionEntry", "1.3.6.1.2.1.25.3.7.1", "EN"), new SnmpOidRecord("hrPartitionFSIndex", "1.3.6.1.2.1.25.3.7.1.5", "I"),
			new SnmpOidRecord("hrPartitionSize", "1.3.6.1.2.1.25.3.7.1.4", "I"), new SnmpOidRecord("hrPartitionID", "1.3.6.1.2.1.25.3.7.1.3", "S"),
			new SnmpOidRecord("hrPartitionLabel", "1.3.6.1.2.1.25.3.7.1.2", "S"), new SnmpOidRecord("hrPartitionIndex", "1.3.6.1.2.1.25.3.7.1.1", "I"),
			new SnmpOidRecord("hrStorageTable", "1.3.6.1.2.1.25.2.3", "TA"), new SnmpOidRecord("hrStorageEntry", "1.3.6.1.2.1.25.2.3.1", "EN"),
			new SnmpOidRecord("hrStorageUsed", "1.3.6.1.2.1.25.2.3.1.6", "I"), new SnmpOidRecord("hrStorageSize", "1.3.6.1.2.1.25.2.3.1.5", "I"),
			new SnmpOidRecord("hrStorageAllocationUnits", "1.3.6.1.2.1.25.2.3.1.4", "I"), new SnmpOidRecord("hrStorageDescr", "1.3.6.1.2.1.25.2.3.1.3", "S"),
			new SnmpOidRecord("hrStorageType", "1.3.6.1.2.1.25.2.3.1.2", "OI"), new SnmpOidRecord("hrStorageIndex", "1.3.6.1.2.1.25.2.3.1.1", "I"),
			new SnmpOidRecord("hrStorageAllocationFailures", "1.3.6.1.2.1.25.2.3.1.7", "C"), new SnmpOidRecord("hrMemorySize", "1.3.6.1.2.1.25.2.2", "I"),
			new SnmpOidRecord("hrSystemProcesses", "1.3.6.1.2.1.25.1.6", "G"), new SnmpOidRecord("hrSystemNumUsers", "1.3.6.1.2.1.25.1.5", "G"),
			new SnmpOidRecord("hrSystemInitialLoadParameters", "1.3.6.1.2.1.25.1.4", "S"), new SnmpOidRecord("hrSystemInitialLoadDevice", "1.3.6.1.2.1.25.1.3", "I"),
			new SnmpOidRecord("hrSystemDate", "1.3.6.1.2.1.25.1.2", "S"), new SnmpOidRecord("hrSystemUptime", "1.3.6.1.2.1.25.1.1", "T"),
			new SnmpOidRecord("hrSystemMaxProcesses", "1.3.6.1.2.1.25.1.7", "I"), new SnmpOidRecord("hrSWInstalledGroup", "1.3.6.1.2.1.25.7.3.6", "OBG"),
			new SnmpOidRecord("hrDeviceGroup", "1.3.6.1.2.1.25.7.3.3", "OBG"), new SnmpOidRecord("hrSystemGroup", "1.3.6.1.2.1.25.7.3.1", "OBG"),
			new SnmpOidRecord("hrSWRunGroup", "1.3.6.1.2.1.25.7.3.4", "OBG"), new SnmpOidRecord("hrStorageGroup", "1.3.6.1.2.1.25.7.3.2", "OBG"),
			new SnmpOidRecord("hrSWRunPerfGroup", "1.3.6.1.2.1.25.7.3.5", "OBG") };
}
