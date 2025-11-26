package hxc.utils.protocol.ucip;

/**
 * MessageCapabilityFlag
 * 
 * The messageCapabilityFlag parameter indicates the possible actions that may be performed on the account due to an operation initiated over this protocol. It is enclosed in a <struct> of its own.
 */
public class MessageCapabilityFlag
{
	/*
	 * The promotionNotificationFlagparameter is used to indicate if the promotion notification code shall be cleared or not after delivery in response.
	 */
	public Boolean promotionNotificationFlag;

	/*
	 * The firstIVRCallSetFlag is used to indicate if the first IVR call done flag shall be set or not.
	 */
	public Boolean firstIVRCallSetFlag;

	/*
	 * The accountActivationFlag parameter is used to indicate if pre-activated accounts may be or will not be activated due to the request. The possibility to activate a pre-activated account by this
	 * flag is configured in the account databases service class configuration. This results in that even if the flag is set to true the account may not be activated.
	 */
	public Boolean accountActivationFlag;

}
