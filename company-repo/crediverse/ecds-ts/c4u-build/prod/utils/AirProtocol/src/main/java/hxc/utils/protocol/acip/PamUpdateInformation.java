package hxc.utils.protocol.acip;

import java.util.Date;

import hxc.connectors.air.Air;

/**
 * PamUpdateInformation
 * 
 * The pamUpdateInformation is enclosed in a <struct> of its own and contains information used when updating periodic account management data.
 */
public class PamUpdateInformation
{
	/*
	 * The pamServiceID parameter specifies the id of the periodic account management service.
	 */
	@Air(Mandatory = true, Range = "0:99")
	public int pamServiceID;

	/*
	 * The pamClassIDOld parameter specifies the old periodic account management class used by the periodic account management service when changing the periodic account management class.
	 */
	@Air(Range = "0:9999")
	public Integer pamClassIDOld;

	/*
	 * The pamClassIDNew parameter specifies the new periodic account management class used by the periodic account management service when changing the periodic account management class.
	 */
	@Air(Range = "0:9999")
	public Integer pamClassIDNew;

	/*
	 * The scheduleIDOld parameter contains the old schedule used when changing the schedule.
	 */
	@Air(Range = "0:9999")
	public Integer scheduleIDOld;

	/*
	 * The scheduleIDNew parameter contains the new schedule used when changing the schedule.
	 */
	@Air(Range = "0:9999")
	public Integer scheduleIDNew;

	/*
	 * The currentPamPeriod parameter contains the periodic account management period that is currently used for the subscriber.
	 */
	@Air(Length = "1:30")
	public String currentPamPeriod;

	/*
	 * The deferredToDate parameter contains the deferred to date for the Periodic Account Management service. If deferredToDate is set in the past in a request, the deferred to date will be removed.
	 */
	@Air(Range = "DateMin:DateMax")
	public Date deferredToDate;

	/*
	 * The pamServicePriorityOld parameter indicates the old priority between PAM services at PAM evaluation, if priority is updated.
	 */
	@Air(Range = "0:65535")
	public Integer pamServicePriorityOld;

	/*
	 * The pamServicePriorityNew parameter indicates the new priority between PAM services at PAM evaluation, if priority is updated.
	 */
	@Air(Range = "0:65535")
	public Integer pamServicePriorityNew;

}
