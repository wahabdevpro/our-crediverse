package hxc.utils.protocol.hmx;

public class GetSubscriberInformationParameters
{
	public static final int DOMAIN_CIRCUIT_SWITCHED = 0;
	public static final int DOMAIN_PACKET_SWITCHED = 1;
	public static final int DOMAIN_ANY = 3;

	// Mandatory
	public hxc.utils.protocol.hsx.Number subscriberNumber;

	// This indicates the domain of the request.
	// 0 Circuit Switched
	// 1 Packet Switched
	// 3 Any
	// Mandatory
	public int domain;

	// This parameter indicates whether the subscriber status should be retrieved.
	// Optional
	public Boolean requestState;

	// This parameter indicates whether the subscribers basic location (CGI, VLR number,
	// MSC number) should be retrieved.
	// Optional
	public Boolean requestBasicLocation;

	// This parameter indicates whether the subscriber’s MNP information should be
	// retrieved.
	// Optional
	public Boolean requestMnpStatus;
	
	// This parameter indicates whether the subscriber's IMSI should be retrieved.
	// Optional
	public Boolean requestImsi;
	
}
