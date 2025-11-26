package hxc.utils.protocol.ucip;

import java.util.Date;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * RefillInformation
 * 
 * The refillInformation is enclosed in a <struct> of its own. The refill values are described in two structs: the refillvalue Total and Promotion. Total will contain the actual values applied to
 * account (including market segmentation and promotion), while the Promotion struct only contain promotion part given (if any). In order to detail what is the refill part, the promotion part must be
 * deducted from the total refill values. Note: The serviceClassCurrent and serviceClassTemporaryExpiryDate parameters has a relation that is described below: * In case only serviceClassCurrent is
 * returned it means that the service class was changed due to the refill. A permanent service class change was done. * In case both serviceClassCurrent and serviceClassTemporaryExp iryDate are
 * returned it means that the account was assigned a temporary service class (with expiry date) * In case only serviceClassTemporaryExpiryDate is returned, this mean that the expire date of an already
 * assigned temporary service class was extended. Thus the assigned temporary service class will be kept * If neither of them is return it means that service class was not changed (independent if is
 * temporary or not)
 */
public class RefillInformation
{
	@Air(Mandatory = true)
	public RefillValueTotal refillValueTotal;

	public RefillValuePromotion refillValuePromotion;

	/*
	 * The serviceClassCurrent parameter contains the service class currently used by the subscriber. This might be a temporary Service Class, which is controlled by a temporary Service Class expiry
	 * date (separate parameter).
	 */
	@Air(Range = "0:9999")
	public Integer serviceClassCurrent;

	/*
	 * The serviceClassTemporaryExpiryDate parameter contains the expiry date of a temporary service class of an account. A temporary service class has precedence before the normally assigned service
	 * class, as long as the temporary service class date expiry date is not passed.
	 */
	public Date serviceClassTemporaryExpiryDate;

	/*
	 * The promotionPlanProgressed parameter indicate if the result of the refill was a progression of promotion plan.
	 */
	public Boolean promotionPlanProgressed;

	/*
	 * The supervisionDaysSurplus parameter contains the number of days that was not possible to add (including any promotional part) as the date has exceed the maximum period allowed. The accounts
	 * actual supervision date will be set equal to the maximum allowed date.
	 */
	@Air(Range = "0:2147483647")
	public Integer supervisionDaysSurplus;

	/*
	 * The serviceFeeDaysSurplus parameter contains the number of days that was not possible to add (including any promotional part) as the date has exceed the maximum period allowed. The accounts
	 * actual supervision date will be set equal to the maximum allowed date.
	 */
	@Air(Range = "0:2147483647")
	public Integer serviceFeeDaysSurplus;

	/*
	 * The promotionRefillAccumulatedValue1 and promotionRefil lAccumulatedValue2 parameters specifies the accumulated value of account refills made within the current promotion plan of a subscriber.
	 * promotionRefillAccumulatedValue1 indicates a value in the first currency to be announced and promotionRefillAccumulatedValue2 a value in the second currency.
	 */
	@Air(Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long promotionRefillAccumulatedValue1;

	/*
	 * The promotionRefillAccumulatedValue1 and promotionRefil lAccumulatedValue2 parameters specifies the accumulated value of account refills made within the current promotion plan of a subscriber.
	 * promotionRefillAccumulatedValue1 indicates a value in the first currency to be announced and promotionRefillAccumulatedValue2 a value in the second currency.
	 */
	@Air(Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long promotionRefillAccumulatedValue2;

	/*
	 * The promotionRefillCounter parameter contains the accumulated number of account refills within the current promotion plan of a subscriber.
	 */
	@Air(Range = "0:255")
	public Integer promotionRefillCounter;

	/*
	 * The progressionRefillValue1 and progressionRefillValue2 para meters specifies the accumulated value of refills for promotion plan progression. progressionRefillValue1 indicates a value in the
	 * first currency to be announced and progressionRefillValue2 a value in the second currency.
	 */
	@Air(Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long progressionRefillValue1;

	/*
	 * The progressionRefillValue1 and progressionRefillValue2 para meters specifies the accumulated value of refills for promotion plan progression. progressionRefillValue1 indicates a value in the
	 * first currency to be announced and progressionRefillValue2 a value in the second currency.
	 */
	@Air(Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public Long progressionRefillValue2;

	/*
	 * The progressionRefillValue1 and progressionRefillValue2 para meters specifies the accumulated value of refills for promotion plan progression. progressionRefillValue1 indicates a value in the
	 * first currency to be announced and progressionRefillValue2 a value in the second currency.
	 */
	@Air(Range = "0:999999999999", Format = "Price")
	@XmlRpcAsString
	public Integer progressionRefillCounter;

}
