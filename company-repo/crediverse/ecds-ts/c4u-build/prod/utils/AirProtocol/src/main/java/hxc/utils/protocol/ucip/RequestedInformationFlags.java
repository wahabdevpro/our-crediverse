package hxc.utils.protocol.ucip;

import hxc.connectors.air.Air;

/**
 * RequestedInformationFlags
 * 
 * The requestedInformationFlags parameter indicates the elements to be returned or not returned in a response. It is enclosed in a <struct> of its own.
 */
public class RequestedInformationFlags
{
	/*
	 * The requestMasterAccountBalanceFlag parameter is used to indicate if the currency1, accountValue1, aggregatedBalance1, currency2, accountvalue2 and aggregatedBalance2 parameters shall be
	 * returned or not.
	 */
	public Boolean requestMasterAccountBalanceFlag;

	/*
	 * The allowedServiceClassChangeDateFlagparameter is used to indicate if the serviceClassChangeUnbarDate parameter shall be returned or not.
	 */
	public Boolean allowedServiceClassChangeDateFlag;

	/*
	 * The requestLocationInformationFlag parameter is used to retrieve location information.
	 */
	@Air(CAP = "CAP:12")
	public Boolean requestLocationInformationFlag;

}
