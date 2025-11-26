package hxc.utils.protocol.ucip;

import java.util.Date;

import hxc.connectors.air.Air;
import hxc.utils.xmlrpc.XmlRpcAsString;

/**
 * DiscountInformation
 * 
 * The discountInformation parameter contains discount information about requested services. It is enclosed in a struct of it own. The structs are placed in an array. The following table holds
 * information about the discountInformation parameter:
 */
public class DiscountInformation
{
	/*
	 * The discountID contains the identifier of the service to be returned in the response.
	 */
	@Air(CAP = "CAP:14", Range = "1:65535")
	public Integer discountID;

	/*
	 * The discountActiveFlag indicates whether returned value is a active discount or a planned discount.
	 */
	@Air(CAP = "CAP:14")
	public Boolean activeFlag;

	/*
	 * The discountValue contains the discount percentage.
	 */
	@Air(CAP = "CAP:14", Range = "0:100")
	public Integer discountValue;

	/*
	 * This parameter contains the downlink bandwidth in absolute values (bits per second).
	 */
	@Air(CAP = "CAP:17", Range = "1:4294967295")
	@XmlRpcAsString
	public Long bandwidthDownlink;

	/*
	 * This parameter contains the uplink bandwidth in absolute values (bits per second).
	 */
	@Air(CAP = "CAP:17", Range = "1:4294967295")
	@XmlRpcAsString
	public Long bandwidthUplink;

	/*
	 * The validityTime parameter contains the expiry date and time.
	 */
	@Air(CAP = "CAP:14", Range = "DateMin:DateMax")
	public Date validityTime;

}
