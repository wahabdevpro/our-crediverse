package za.co.concurrent.NetworkIdentifier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.IpPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.packet.namednumber.UdpPort;
import org.pcap4j.util.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IDsMonitor {
	private static final Logger logger = LoggerFactory.getLogger(IDsMonitor.class);

	private static final String COUNT_KEY
	= IDsMonitor.class.getName() + ".count";
	private static final int COUNT
	= Integer.getInteger(COUNT_KEY, 1);

	private static final String READ_TIMEOUT_KEY
	= IDsMonitor.class.getName() + ".readTimeout";
	private static final int READ_TIMEOUT
	= Integer.getInteger(READ_TIMEOUT_KEY, 10); // [ms]

	private static final String SNAPLEN_KEY
	= IDsMonitor.class.getName() + ".snaplen";
	private static final int SNAPLEN
	= Integer.getInteger(SNAPLEN_KEY, 65536); // [bytes]


	private static MacAddress resolvedAddr;
	private final static Logger log = LoggerFactory.getLogger(IDsMonitor.class);

	PcapHandle handle;

	ExecutorService pool;


	IDsMonitor() {}


	public void start( PcapNetworkInterface nif, final IDsReceiver receiver ) throws PcapNativeException, NotOpenException {

		String srcIpAddress = nif.getAddresses().get(0).getAddress().getHostAddress();
		String srcMacAddress = nif.getLinkLayerAddresses().get(0).toString();

		log.info( "Starting to listen on: " + nif.getName() + "(" + srcIpAddress + ":" + srcMacAddress + ")...");

		log.trace( COUNT_KEY + ": " + COUNT);
		log.trace( READ_TIMEOUT_KEY + ": " + READ_TIMEOUT);
		log.trace( SNAPLEN_KEY + ": " + SNAPLEN);
		log.trace( "\n");


		handle
		= nif.openLive(SNAPLEN, PromiscuousMode.PROMISCUOUS, READ_TIMEOUT);

		pool = Executors.newSingleThreadExecutor();

		try {
			handle.setFilter(
					"arp ",
							BpfCompileMode.OPTIMIZE
					);

			PacketListener listener
			= new PacketListener() {
				@Override
				public void gotPacket(Packet packet) {
					if (packet.contains(ArpPacket.class)) {
						ArpPacket arp = packet.get(ArpPacket.class);
						//if (arp.getHeader().getOperation().equals(ArpOperation.REPLY)) {
							IDsMonitor.resolvedAddr = arp.getHeader().getSrcHardwareAddr();
							log.trace( "received MAC: " + IDsMonitor.resolvedAddr.toString() + " for IP: " + arp.getHeader().getSrcProtocolAddr().getHostAddress().toString() );
							receiver.receiveMacInfo( arp.getHeader().getSrcProtocolAddr().getHostAddress().toString() , IDsMonitor.resolvedAddr.toString() );
						//}
					}
					else if( packet.contains( UdpPacket.class ) ){
						UdpPacket udp = packet.get( UdpPacket.class );
						IpPacket ip = packet.get( IpPacket.class );
						if ( udp.getHeader().getSrcPort() == UdpPort.DOMAIN ) {
							log.trace( "received DNS response: " + packet.getHeader().toString() + "and ip:" + ip.getHeader().toString() + "for : " + udp.toString() );
						}
					}

				}
			};

			Task t = new Task(handle, listener);
			pool.execute(t);

		} finally {
		}
	}

	private static class Task implements Runnable {

		private PcapHandle handle;
		private PacketListener listener;

		public Task(PcapHandle handle, PacketListener listener) {
			this.handle = handle;
			this.listener = listener;
		}

		@Override
		public void run() {
			try {
				handle.loop(-1, listener);
			} catch (PcapNativeException e) {
				logger.error("", e);
			} catch (InterruptedException e) {
				log.warn("no longer listening");
			} catch (NotOpenException e) {
				logger.error("", e);
			}
		}

	}

	public void stop() {
		if (pool != null && !pool.isShutdown()) {
			pool.shutdown();
		}
		try {
			handle.breakLoop();
		} catch (NotOpenException e) {
			log.warn("handle was not listening anyway");
		}
		if (handle != null && handle.isOpen()) {
			handle.close();
		}

	}
}
