package hxc.utils.protocol.vsip;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

// The message UpdateVoucherState is used to update the voucher state.
// The requested state change pointed out by the “newState” parameter must
// follow the state model with allowed state transitions defined for the voucher
// server.

@XmlAccessorType(XmlAccessType.FIELD)
public class UpdateVoucherStateRequest implements IVsipRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String serialNumber;
	private String activationCode;
	@XmlElement(required = true)
	private String newState;
	private String oldState;
	private String networkOperatorId;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// The serialNumber parameter is used to state the unique voucher serial
	// number that is used to identify the voucher. Leading zeros are allowed. The
	// element size defined below defines the limit at protocol level, and may be
	// further restricted at application level by the server side.
	//
	// One of the elements serialNumber and activationCode must be present. In the case that both
	// elements are present, an error will be returned.
	//
	// Mandatory

	public String getSerialNumber()
	{
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber)
	{
		this.serialNumber = serialNumber;
	}

	// The activationCode parameter is the unique secret code which is used to
	// refill the account. The activation code may have leading zeros. The element
	// size defined below defines the limit at protocol level, and may be further
	// restricted at application level by the server side.
	//
	// One of the elements serialNumber and activationCode must be present. In the case that both
	// elements are present, an error will be returned.
	//
	// Mandatory

	public String getActivationCode()
	{
		return activationCode;
	}

	public void setActivationCode(String activationCode)
	{
		this.activationCode = activationCode;
	}

	// The newState parameter is used to represent the state of the voucher as it is,
	// or will be, after a specific event. Such an event could be a request to change
	// a voucher to the new state, or it could be a historical record that represents a
	// state change in the past.
	//
	// The following state changes are available, see Table 11.
	//
	// Mandatory

	public String getNewState()
	{
		return newState;
	}

	public void setNewState(String newState)
	{
		this.newState = newState;
	}

	// The oldState parameter is used to represent the state of the voucher as it is,
	// or was, prior to a specific event. Such an event could be a request to change a
	// vouchers state from the specified state, or it could be a historical record that
	// represents a state change in the past.
	//
	// The following state changes are available, see Table 11.
	// If oldState is omitted, the state change will be applied, no matter what state the voucher
	// currently has, if the state change is supported.
	//
	// Optional

	public String getOldState()
	{
		return oldState;
	}

	public void setOldState(String oldState)
	{
		this.oldState = oldState;
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
	// Optional

	public String getNetworkOperatorId()
	{
		return networkOperatorId;
	}

	public void setNetworkOperatorId(String networkOperatorId)
	{
		this.networkOperatorId = networkOperatorId;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	@Override
	public boolean validate(IValidationContext context)
	{
		return Protocol.validateExclusivity(context, "Serial Number", "Activation Code", serialNumber, activationCode) && //
				Protocol.validateSerialNumber(context, false, serialNumber) && //
				Protocol.validateActivationCode(context, false, activationCode) && //
				Protocol.validateNewState(context, true, newState) && //
				Protocol.validateOldState(context, false, oldState) && //
				Protocol.validateNetworkOperatorId(context, networkOperatorId);
	}

}
