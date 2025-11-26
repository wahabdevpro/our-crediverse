package za.co.concurrent.NetworkIdentifier;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapAddress;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.namednumber.ArpHardwareType;
import org.pcap4j.packet.namednumber.ArpOperation;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.util.ByteArrays;
import org.pcap4j.util.LinkLayerAddress;
import org.pcap4j.util.MacAddress;
import org.pcap4j.util.NifSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;


public class IDsRequester {
	private static final Logger logger = LoggerFactory.getLogger(IDsRequester.class);

	private static final String COUNT_KEY
	= IDsRequester.class.getName() + ".count";
	private static final int COUNT
	= Integer.getInteger(COUNT_KEY, 1);

	private static final String READ_TIMEOUT_KEY
	= IDsRequester.class.getName() + ".readTimeout";
	private static final int READ_TIMEOUT
	= Integer.getInteger(READ_TIMEOUT_KEY, 10); // [ms]

	private static final String SNAPLEN_KEY
	= IDsRequester.class.getName() + ".snaplen";
	private static final int SNAPLEN
	= Integer.getInteger(SNAPLEN_KEY, 65536); // [bytes]
	private static String LINE_SEPARATOR = "\n";


	private InetAddress srcIpAddress;
	private MacAddress srcMacAddress;
	private PcapNetworkInterface nif;
	private IDsMonitor mon;
	private IDsReceiver rec;
	private String networkInterfaceName;
	static PcapHandle sendHandle;
	ExecutorService pool = Executors.newCachedThreadPool();
	private boolean started = false;
	private final static Logger log = LoggerFactory.getLogger(IDsRequester.class);

	public void init( IDsReceiver receiver, String nic ) throws PcapNativeException, NotOpenException, IOException
	{
		networkInterfaceName = nic;
		rec = receiver;
		mon = new IDsMonitor();

		log.trace( "selecting NIC!" );
		try {
			nif = getNic( networkInterfaceName );
			log.trace( "NIC selected!" );
		} catch (IOException e) {
			log.error( "IOException while selecting NIC: " + e.getMessage() + "\n" + e.getStackTrace().toString() );
			return;
		} catch ( Exception e )
		{
			log.error("Initialization of IDsRequester failed");
		}

		if (nif == null) {
			log.error( "NIC is null!" );
			return;
		}

		srcIpAddress = nif.getAddresses().get(0).getAddress();
		srcMacAddress = MacAddress.getByName( nif.getLinkLayerAddresses().get(0).toString() );
		sendHandle = nif.openLive(SNAPLEN, PromiscuousMode.PROMISCUOUS, READ_TIMEOUT);
		mon.start( nif, receiver );
		started = true;
	}

	protected static
	StringBuilder getNifList(List<PcapNetworkInterface> nifs)
			throws IOException {
		StringBuilder sb = new StringBuilder(200);
		int nifIdx = 0;
		for (PcapNetworkInterface nif: nifs) {
			sb.append("NIF[").append(nifIdx).append("]: ")
			.append(nif.getName()).append(LINE_SEPARATOR);

			if (nif.getDescription() != null) {
				sb.append("      : description: ")
				.append(nif.getDescription()).append(LINE_SEPARATOR);
			}

			for (LinkLayerAddress addr: nif.getLinkLayerAddresses()) {
				sb.append("      : link layer address: ")
				.append(addr).append(LINE_SEPARATOR);
			}

			for (PcapAddress addr: nif.getAddresses()) {
				sb.append("      : address: ")
				.append(addr.getAddress()).append(LINE_SEPARATOR);
			}
			nifIdx++;
		}
		sb.append(LINE_SEPARATOR);

		return sb;
	}

	public void terminate( IDsReceiver receiver, String nic ) throws PcapNativeException, NotOpenException
	{

		if (sendHandle != null && sendHandle.isOpen()) {
			sendHandle.close();
		}
		started = false;
		mon.stop();
	}

	public void sendMacRequest( String ipAddr ) throws PcapNativeException, NotOpenException
	{
		if ( started )
		{
			log.trace( "asking Mac for {} ", ipAddr );
			pool.submit( new MACRequester( rec, ipAddr, srcMacAddress, srcIpAddress ) );
		}
	}

	public void sendReverseDnsRequest( String ipAddr )
	{	
		if( started )
		{
			log.trace( "asking host for {} ", ipAddr );
			pool.submit( new DNSRequester( rec, ipAddr ) );
		}
	}

	public void terminate( )
	{
		if (pool != null && !pool.isShutdown()) {
			pool.shutdown();
		}
		started = false;
	}

	private static PcapNetworkInterface getNic( String nicName ) throws IOException, PcapNativeException {
		List<PcapNetworkInterface> allDevs = null;
		try {
			allDevs = Pcaps.findAllDevs();
		} catch (PcapNativeException e) {
			throw new IOException(e.getMessage());
		}

		if (allDevs == null || allDevs.isEmpty()) {
			throw new IOException("No NIF to capture.");
		}

		for (PcapNetworkInterface nif: allDevs) {
			log.info( "Looking for : {} Found :{} detail: {} ", nicName, nif.getName(), nif.getDescription() );
			if ( nif.getName().equals(nicName) ) {
				log.info( "NIC {} found", nif.getName() );
				try{
					for ( PcapAddress addr: nif.getAddresses() ) {
						log.info( "NIC {} has address {}", nif.getName(), addr.getAddress() );
						log.info( "NIC found : {} IP :{}", nif.getName(), nif.getAddresses().get(0).getAddress() );
						return nif;
					}

					log.info( " {} does NOT have a valid local address :{} \n please select the NIC manually", nif.getName(), nif.getAddresses().get(0).getAddress() );
					throw new PcapNativeException( "The Selected network interface is unsuitable!\n The following interfaces are available:\n" + getNifList( allDevs ).toString()	);
				} catch ( Exception e )
				{
					log.error( "Problem while selecting NIC: " + e.getMessage() + "\n" + e.getStackTrace().toString() );

					throw new PcapNativeException( "The Selected network interface is unsuitable!\n The following interfaces are available:\n" + getNifList( allDevs ).toString()	);
				}
			}
		}

		return new NifSelector().selectNetworkInterface();
	}

	private static class DNSRequester implements Runnable {

		private IDsReceiver rcv;
		private String ip;

		public DNSRequester( IDsReceiver receiver, String ipAddr ) {
			this.rcv = receiver;
			this.ip = ipAddr;
		}

		@Override
		public void run() {
			try {
				InetAddress addr = InetAddresses.forString( ip );
				log.trace( "getting hostname for {} ...", ip );
				String host = addr.getCanonicalHostName();
				log.trace( "hostname for {} is {} ", ip, host );

				rcv.receiveHostInfo(ip, host);
			} catch (Exception e) {
				logger.error("", e);
			}
		}

	}

	private static class MACRequester implements Runnable {

		// private IDsReceiver rcv;
		private String ip;
		InetAddress ipSrc;
		MacAddress macSrc;

		public MACRequester( IDsReceiver receiver, String ipAddr, MacAddress srcMacAddress, InetAddress srcIpAddress  ) {
			// this.rcv = receiver;
			this.ip = ipAddr;
			this.macSrc = srcMacAddress;
			this.ipSrc = srcIpAddress;
		}

		@Override
		public void run() {
			ArpPacket.Builder arpBuilder = new ArpPacket.Builder();
			try {
				arpBuilder
				.hardwareType(ArpHardwareType.ETHERNET)
				.protocolType(EtherType.IPV4)
				.hardwareAddrLength((byte)MacAddress.SIZE_IN_BYTES)
				.protocolAddrLength((byte)ByteArrays.INET4_ADDRESS_SIZE_IN_BYTES)
				.operation(ArpOperation.REQUEST)
				.srcHardwareAddr(macSrc)
				.srcProtocolAddr(ipSrc)
				.dstHardwareAddr(MacAddress.ETHER_BROADCAST_ADDRESS)
				.dstProtocolAddr(InetAddress.getByName(ip));
			} catch (UnknownHostException e) {
				throw new IllegalArgumentException(e);
			}

			EthernetPacket.Builder etherBuilder = new EthernetPacket.Builder();
			etherBuilder.dstAddr(MacAddress.ETHER_BROADCAST_ADDRESS)
			.srcAddr(macSrc)
			.type(EtherType.ARP)
			.payloadBuilder(arpBuilder)
			.paddingAtBuild(true);

			for (int i = 0; i < COUNT; i++) {
				Packet p;
				try {
					p = etherBuilder.build();

					log.trace( "asking mac for {} ", ip );
					sendHandle.sendPacket(p);
				} catch (Exception e1) {
					log.error( "Exception while building or sending ARP request: \n" + e1.getMessage() );
				}
				try {
					Thread.sleep(READ_TIMEOUT);
				} catch (InterruptedException e) {
					break;
				} finally
				{
				}
			}
		}

	}

}
