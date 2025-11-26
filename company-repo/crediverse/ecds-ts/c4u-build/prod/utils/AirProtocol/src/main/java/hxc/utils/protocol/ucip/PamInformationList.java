package hxc.utils.protocol.ucip;

import java.util.Date;

import hxc.connectors.air.Air;

/**
 * PamInformationList
 * 
 * The pamInformationList is a list of pamInformation placed in an <array>. Currently only one entry of pamInformation in the array is supported.
 */
public class PamInformationList
{
	/*
	 * The pamServiceID parameter specifies the id of the periodic account management service.
	 */
	@Air(Mandatory = true, Range = "0:99")
	public int pamServiceID;

	/*
	 * The pamClassID parameter specifies the periodic account management class used by the periodic account management service.
	 */
	@Air(Mandatory = true, Range = "0:9999")
	public int pamClassID;

	/*
	 * The scheduleID parameter contains the schedule that is used by the periodic account management service.
	 */
	@Air(Mandatory = true, Range = "0:9999")
	public int scheduleID;

	/*
	 * The currentPamPeriod parameter contains the periodic account management period that is currently used for the subscriber.
	 */
	@Air(Mandatory = true, Length = "1:30")
	public String currentPamPeriod;

	/*
	 * The deferredToDate parameter contains the deferred to date for the Periodic Account Management service. If deferredToDate is set in the past in a request, the deferred to date will be removed.
	 */
	@Air(Range = "DateMin:DateMax")
	public Date deferredToDate;

	/*
	 * The lastEvaluationDateparameter contains the date of the last periodic account management evaluation.
	 */
	@Air(Range = "DateMin:DateMax")
	public Date lastEvaluationDate;

	/*
	 * The pamServicePriority parameter indicates the priority between PAM services at PAM evaluation. Lower value gives higher priority.
	 */
	@Air(Range = "0:65535")
	public Integer pamServicePriority;

}
