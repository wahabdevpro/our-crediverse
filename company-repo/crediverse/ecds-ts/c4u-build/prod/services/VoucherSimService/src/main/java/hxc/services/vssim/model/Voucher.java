package hxc.services.vssim.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hxc.utils.protocol.vsip.EndReservationRequest;
import hxc.utils.protocol.vsip.GetVoucherHistoryTransactionRecords;
import hxc.utils.protocol.vsip.Protocol;
import hxc.utils.protocol.vsip.ReserveVoucherRequest;

public class Voucher
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String activationCode;
	private String currency;
	private String serialNumber;
	private long value;
	private String voucherGroup;
	private Date expiryDate;
	private String agent;
	private String extensionText1;
	private String extensionText2;
	private String extensionText3;
	private String networkOperatorId;
	private String batchId;
	private String supplierId;
	private String additionalAction = Protocol.ADDITIONALACTION_AUTO_ROLLBACK;
	private Object lock = new Object();

	private List<GetVoucherHistoryTransactionRecords> history = new ArrayList<GetVoucherHistoryTransactionRecords>();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// The currency parameter is used to indicate the currency of the voucher value.
	// The currency is expressed as a three letter string according to the ISO 4217
	// standard, see Codes for the representation of currencies and funds, Reference
	// [6]. Examples are "EUR" for Euro and "SEK" for Swedish Kronor.
	//

	public String getCurrency()
	{
		return currency;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	// The serialNumber parameter is used to state the unique voucher serial
	// number that is used to identify the voucher. Leading zeros are allowed. The
	// element size defined below defines the limit at protocol level, and may be
	// further restricted at application level by the server side.
	//

	public String getSerialNumber()
	{
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber)
	{
		this.serialNumber = serialNumber;
	}

	// The value parameter is used to specify the actual value of the voucher
	// in currency units. The value is formatted as a numeric string. No decimal
	// separator is included. The amount is expressed in the lowest denomination of
	// the specified currency. For example a USD 100 value is represented as 10000.
	//

	public long getValue()
	{
		return value;
	}

	public void setValue(long value)
	{
		this.value = value;
	}

	// The voucherGroup parameter is used to define a set of properties that are
	// associated with a voucher. Each voucher is assigned to a voucher group and
	// many vouchers can be assigned the same voucher group.
	//

	public String getVoucherGroup()
	{
		return voucherGroup;
	}

	public void setVoucherGroup(String voucherGroup)
	{
		this.voucherGroup = voucherGroup;
	}

	// The expiryDate parameter is used to identify the last date when the voucher
	// will be usable in the system. Only the date information will be considered by
	// this parameter. The time and timezone should be set to all zeroes, and will
	// be ignored.
	// TZ is the deviation in hours from UTC. This field is optional. This date format
	// does not strictly follow the XML-RPC specification on date format. It does
	// however follow the ISO 8601 specification. Parsers for this protocol must be
	// prepared to parse dates containing timezone.
	//
	// Not allowed when PC:10505 is active. The date will instead be generated from pre-configured
	// values.
	//

	public Date getExpiryDate()
	{
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate)
	{
		this.expiryDate = expiryDate;
	}

	// The agent parameter is used to indicate the name of the dealer who has
	// received the card from the service provider.
	//

	public String getAgent()
	{
		return agent;
	}

	public void setAgent(String agent)
	{
		this.agent = agent;
	}

	// The extensionText1, extensionText2 and extensionText3 parameters
	// are used to store any additional information connected to a voucher. The
	// definition of the extension texts are site specific and have no special meaning
	// within VS.
	// Note: Space is an allowed character.
	//

	public String getExtensionText1()
	{
		return extensionText1;
	}

	public void setExtensionText1(String extensionText1)
	{
		this.extensionText1 = extensionText1;
	}

	// The extensionText1, extensionText2 and extensionText3 parameters
	// are used to store any additional information connected to a voucher. The
	// definition of the extension texts are site specific and have no special meaning
	// within VS.
	// Note: Space is an allowed character.
	//

	public String getExtensionText2()
	{
		return extensionText2;
	}

	public void setExtensionText2(String extensionText2)
	{
		this.extensionText2 = extensionText2;
	}

	// The extensionText1, extensionText2 and extensionText3 parameters
	// are used to store any additional information connected to a voucher. The
	// definition of the extension texts are site specific and have no special meaning
	// within VS.
	// Note: Space is an allowed character.
	//

	public String getExtensionText3()
	{
		return extensionText3;
	}

	public void setExtensionText3(String extensionText3)
	{
		this.extensionText3 = extensionText3;
	}

	// The networkOperatorId parameter is used to reference a Mobile Virtual
	// Network Operator. The VS system is capable of administering and managing
	// multiple operators simultaneously. Each Mobile Virtual Network Operator has
	// its own database schema, in which this operator's own vouchers are stored.
	// The parameter is bound to the Mobile Virtual Network Operator functionality,
	// which must be explicitly configured. If not activated, the parameter is not
	// mandatory, in which case all requests are targeted to the default database
	// schema of the VS system.
	//
	// This element is mandatory if Mobile Virtual Network Operator functionality is activated;
	// otherwise, the element is optional.
	//

	public String getNetworkOperatorId()
	{
		return networkOperatorId;
	}

	public void setNetworkOperatorId(String networkOperatorId)
	{
		this.networkOperatorId = networkOperatorId;
	}

	// The activationCode parameter is the unique secret code which is used to
	// refill the account. The activation code may have leading zeros. The element
	// size defined below defines the limit at protocol level, and may be further
	// restricted at application level by the server side.
	//
	// One of the elements batchId, activationCode, serialNumber or a combination of
	// serialNumberFirst and serialNumberLast must be present. In case two or more of the above
	// elements are present (except for the combination of serialNumberFirst and serialNumberLast),
	// an error will be returned.
	//
	// Size: 8-20 Allowed: 0-9

	public String getActivationCode()
	{
		return activationCode;
	}

	public void setActivationCode(String activationCode)
	{
		this.activationCode = activationCode;
	}

	// The batchId parameter indicates what batch a voucher belongs to. The
	// batchId is assigned when vouchers are generated.
	//
	// Mandatory

	public String getBatchId()
	{
		return batchId;
	}

	public void setBatchId(String batchId)
	{
		this.batchId = batchId;
	}

	// The supplierId parameter is used to indicate the supplier (print shop) for
	// which voucher batch files with separate encryption keys per supplier will be
	// generated.
	//
	// Optional (PC:09502)

	public String getSupplierId()
	{
		return supplierId;
	}

	public void setSupplierId(String supplierId)
	{
		this.supplierId = supplierId;
	}

	// The voucherExpired parameter is used to indicate if the voucher has passed
	// the expiration date. When this parameter is set the voucher can no longer be
	// used. If the voucher is not expired this parameter will not be included.
	//
	// Optional

	public Boolean getVoucherExpired()
	{
		return new Date().after(expiryDate);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public String getKey()
	{
		return Voucher.getKey(serialNumber, networkOperatorId);
	}

	public static String getKey(String serialNumber, String networkOperatorId)
	{
		if (networkOperatorId == null || networkOperatorId.length() == 0)
			return serialNumber;
		return String.format("%s|%s", networkOperatorId, serialNumber);
	}

	public int reserve(ReserveVoucherRequest request)
	{
		// Possible Result Codes
		// 11 Voucher already used by other subscriber /
		// 12 Voucher missing/stolen /
		// 13 Voucher unavailable /
		// 100 Voucher already used by same subscriber /
		// 102 Voucher expired /
		// 107 Voucher damaged /
		// 108 Voucher reserved by other subscriber /

		synchronized (lock)
		{
			GetVoucherHistoryTransactionRecords lastTransaction = getLastTransaction();
			if (lastTransaction == null)
				return Protocol.RESPONSECODE_VOUCHER_UNAVAILABLE;

			// Test if Expired
			if (getVoucherExpired())
				return Protocol.RESPONSECODE_VOUCHER_EXPIRED;

			switch (lastTransaction.getNewState())
			{
				case Protocol.STATE_PENDING:
				case Protocol.STATE_UNAVAILABLE:
					return Protocol.RESPONSECODE_VOUCHER_UNAVAILABLE;

				case Protocol.STATE_AVAILABLE:
				{
					setNewState(Protocol.STATE_RESERVED, request.getOperatorId(), request.getSubscriberId(), new Date(), request.getTransactionId());
					additionalAction = request.getAdditionalAction();
					return Protocol.RESPONSECODE_SUCCESS;
				}

				case Protocol.STATE_RESERVED:
					return request.getSubscriberId().equals(lastTransaction.getSubscriberId()) ? Protocol.RESPONSECODE_VOUCHER_RESERVED_SAME_SUBSCRIBER
							: Protocol.RESPONSECODE_VOUCHER_RESERVED_OTHER_SUBSCRIBER;

				case Protocol.STATE_USED:
					return request.getSubscriberId().equals(lastTransaction.getSubscriberId()) ? Protocol.RESPONSECODE_VOUCHER_USED_SAME_SUBSCRIBER : Protocol.RESPONSECODE_VOUCHER_ALREADY_USED;

				case Protocol.STATE_DAMAGED:
					return Protocol.RESPONSECODE_VOUCHER_DAMAGED;

				case Protocol.STATE_STOLEN:
					return Protocol.RESPONSECODE_VOUCHER_MISSING_STOLEN;

				default:
					return Protocol.RESPONSECODE_DATABASE_ERROR;
			}

		}

	}

	public int endReserve(EndReservationRequest request)
	{
		// Can Return:
		// 0: Successful /
		// 10: Voucher does not exist /
		// 11: Voucher already used by other subscriber /
		// 12: Voucher missing/stolen /
		// 13: Voucher unavailable /
		// 100: Voucher already used by same subscriber /
		// 102: Voucher expired /
		// 104: Subscriber Id mismatch between the reservation and the end of reservation /
		// 105: Voucher not reserved /
		// 106: Transaction Id mismatch between messages between reservation and the end of reservation /
		// 107: Voucher damaged /
		// 108: Voucher reserved by other subscriber /
		// 109: Database error /

		synchronized (lock)
		{
			GetVoucherHistoryTransactionRecords lastTransaction = getLastTransaction();
			if (lastTransaction == null)
				return Protocol.RESPONSECODE_VOUCHER_UNAVAILABLE;

			// Test if Expired
			if (getVoucherExpired())
				return Protocol.RESPONSECODE_VOUCHER_EXPIRED;

			Date timestamp = new Date();

			switch (lastTransaction.getNewState())
			{
				case Protocol.STATE_UNAVAILABLE:
					return Protocol.RESPONSECODE_VOUCHER_UNAVAILABLE;

				case Protocol.STATE_PENDING:
				case Protocol.STATE_AVAILABLE:
					return Protocol.RESPONSECODE_VOUCHER_NOT_RESERVED;

				case Protocol.STATE_RESERVED:
				{
					if (!request.getSubscriberId().equals(lastTransaction.getSubscriberId()))
						return Protocol.RESPONSECODE_VOUCHER_RESERVED_OTHER_SUBSCRIBER;

					if (!request.getTransactionId().equals(lastTransaction.getTransactionId()))
						return Protocol.RESPONSECODE_TRANSACTION_ID_MISMATCH;

					switch (request.getAction())
					{
						case Protocol.ACTION_COMMIT:
							setNewState(Protocol.STATE_USED, null, request.getSubscriberId(), timestamp, request.getTransactionId());
							break;

						case Protocol.ACTION_ROLLBACK:
							setNewState(Protocol.STATE_AVAILABLE, null, request.getSubscriberId(), timestamp, request.getTransactionId());
							break;

						default:
							return Protocol.RESPONSECODE_DATABASE_ERROR;
					}

					return Protocol.RESPONSECODE_SUCCESS;
				}

				case Protocol.STATE_USED:
					return request.getSubscriberId().equals(lastTransaction.getSubscriberId()) ? Protocol.RESPONSECODE_VOUCHER_USED_SAME_SUBSCRIBER : Protocol.RESPONSECODE_VOUCHER_ALREADY_USED;

				case Protocol.STATE_DAMAGED:
					return Protocol.RESPONSECODE_VOUCHER_DAMAGED;

				case Protocol.STATE_STOLEN:
					return Protocol.RESPONSECODE_VOUCHER_MISSING_STOLEN;

				default:
					return Protocol.RESPONSECODE_DATABASE_ERROR;
			}

		}
	}

	public boolean setPendingPurge(String networkOperatorId, Date expiryDate, String currentState)
	{
		if (!equalsIgnoreCase(networkOperatorId, this.networkOperatorId))
			return false;

		GetVoucherHistoryTransactionRecords lastTransaction = getLastTransaction();
		if (lastTransaction == null)
			return false;

		if (!currentState.equals(lastTransaction.getNewState()))
			return false;

		if (this.expiryDate.after(expiryDate))
			return false;

		synchronized (lock)
		{
			if (!equalsIgnoreCase(networkOperatorId, this.networkOperatorId))
				return false;

			if (!currentState.equals(lastTransaction.getNewState()))
				return false;

			if (this.expiryDate.after(expiryDate))
				return false;

			setNewState(Protocol.STATE_PENDING, null, null, new Date(), null);
		}

		return true;
	}

	private boolean equalsIgnoreCase(String text1, String text2)
	{
		if (text1 == null)
			return text2 == null;
		else
			return text2.equalsIgnoreCase(text1);
	}

	public boolean expireReservation(Date cutoffTime)
	{
		GetVoucherHistoryTransactionRecords lastTransaction = getLastTransaction();
		if (lastTransaction == null || lastTransaction.getNewState().equals(Protocol.STATE_RESERVED))
			return false;

		synchronized (lock)
		{
			lastTransaction = getLastTransaction();
			if (lastTransaction == null || lastTransaction.getTimestamp().after(cutoffTime))
				return false;

			Date timestamp = new Date();

			if (!lastTransaction.getNewState().equals(Protocol.STATE_RESERVED))
				return false;

			if (additionalAction == null || additionalAction.equals(Protocol.ADDITIONALACTION_COMMIT))
			{
				setNewState(Protocol.STATE_USED, null, null, timestamp, null);
			}
			else if (additionalAction.equals(Protocol.ADDITIONALACTION_AUTO_ROLLBACK))
			{
				setNewState(Protocol.STATE_AVAILABLE, null, null, timestamp, null);
			}
		}

		return true;

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	public GetVoucherHistoryTransactionRecords setNewState(String newState, String operatorId, String subscriberId, Date timestamp, String transactionId)
	{
		GetVoucherHistoryTransactionRecords result = new GetVoucherHistoryTransactionRecords();
		result.setNewState(newState);
		result.setOperatorId(operatorId);
		result.setSubscriberId(subscriberId);
		result.setTimestamp(timestamp);
		result.setTransactionId(transactionId);
		history.add(result);
		return result;
	}

	public GetVoucherHistoryTransactionRecords getLastTransaction()
	{
		if (history == null || history.size() == 0)
			return null;

		return history.get(history.size() - 1);
	}

	public GetVoucherHistoryTransactionRecords[] getTransactionRecords()
	{
		if (history == null)
			return null;

		return history.toArray(new GetVoucherHistoryTransactionRecords[history.size()]);
	}

}
