package hxc.services.airsim.protocol;

import java.util.Date;

import hxc.connectors.snmp.IAlarm;

public interface ISystemUnderTest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ISystemUnderTest
	//
	// /////////////////////////////////

	public abstract void setup();

	public abstract ISmsHistory[] getSmsHistory();

	public abstract void clearSmsHistory();

	public abstract boolean restoreBackup(String backupFilename);

	public abstract void injectMOSms(String from, String to, String text);

	public abstract IUssdResponse injectMOUssd(String from, String text, String imsi);

	public abstract String getUssdMenuLine(int lineNumber);

	public abstract ICdr getLastCdr();

	public abstract ICdr[] getCdr(IFilter filters[]);

	public abstract ICdr[] getCdrHistory();

	public abstract void clearCdrHistory();

	public abstract void tearDown();

	public abstract ILifecycle getLifecycle(String msisdn, String serviceID, String variantID);

	public abstract ILifecycle[] getLifecycles(String msisdn);

	public abstract boolean updateLifecycle(ILifecycle lifecycle);

	public abstract boolean deleteLifecycle(String msisdn, String serviceID, String variantID);

	public abstract boolean deleteLifecycles(String msisdn);

	public abstract boolean adjustLifecycle(String msisdn, String serviceID, String variantID, Boolean isBeingProcessed, Date timeStamp);

	public abstract boolean hasMemberLifecycle(String msisdn, String serviceID, String variantID, String memberMsisdn);

	public abstract String[] getMembersLifecycle(String msisdn, String serviceID, String variantID);

	public abstract boolean addMemberLifecycle(String msisdn, String serviceID, String variantID, String memberMsisdn);

	public abstract boolean deleteMemberLifecycle(String msisdn, String serviceID, String variantID, String memberMsisdn);

	public abstract ITemporalTrigger[] getTemporalTriggers(String serviceID, String variantID, String msisdnA, String msisdnB);

	public abstract boolean updateTemporalTrigger(ITemporalTrigger temporalTrigger);

	public abstract boolean deleteTemporalTrigger(ITemporalTrigger temporalTrigger);

	public abstract IAlarm getLastAlarm();

	public abstract IAlarm[] getAlarmHistory();
	
	public abstract String nonQuery(String command);

	public abstract boolean restart(String optionalCommand);

}
