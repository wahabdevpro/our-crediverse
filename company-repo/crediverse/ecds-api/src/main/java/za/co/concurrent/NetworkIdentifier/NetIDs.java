package za.co.concurrent.NetworkIdentifier;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.util.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;



public class NetIDs implements Runnable, IDsReceiver {
	private static final Logger logger = LoggerFactory.getLogger(NetIDs.class);

	private ArrayList<NetID> printsToFind = new ArrayList<NetID>();

	private static final long MAX_SIZE = 100000;
	private static final long MAX_MINUTES_AGE = 100;
	private final static Logger log = LoggerFactory.getLogger(NetIDs.class);

	private final LoadingCache< String, NetID > registry;

	private IDsRequester sender;
	private String networkInterfaceName;
	ExecutorService idsPool = Executors.newCachedThreadPool();

	public NetIDs(String networkInterfaceName) {
		super();
		this.networkInterfaceName = networkInterfaceName;
		this.sender = new IDsRequester();
		registry = CacheBuilder.newBuilder()
				.maximumSize( MAX_SIZE )
				.expireAfterWrite( MAX_MINUTES_AGE, TimeUnit.MINUTES )
				.build( new CacheLoader<String, NetID>()
				{
					//@Override
					public NetID load( String key ) throws Exception {
						log.trace( "creating missing print..." );
						NetID np = createMissingPrint( key );
						log.trace( "created missing print" + np );
						return np;
					}
				}
						);
	}
	
	public NetIDs(String networkInterfaceName, int maxEntriesToCache, int minutesToExpire ) {
		super();
		this.networkInterfaceName = networkInterfaceName;
		this.sender = new IDsRequester();
		registry = CacheBuilder.newBuilder()
				.maximumSize( maxEntriesToCache )
				.expireAfterWrite( minutesToExpire, TimeUnit.MINUTES )
				.build( new CacheLoader<String, NetID>()
				{
					//@Override
					public NetID load( String key ) throws Exception {
						log.trace( "creating missing print..." );
						NetID np = createMissingPrint( key );
						log.trace( "created missing print" + np );
						return np;
					}
				}
						);
	}

	public void init( ){
		try {
			sender.init( this, networkInterfaceName );
			processMissing();
		} catch (PcapNativeException e) {
			logger.error("", e);
		} catch (NotOpenException e) {
			logger.error("", e);
		} catch( Exception e ) {
			logger.error("", e);
		}
	}

	public void terminate( ){
		try {
			sender.terminate( this, networkInterfaceName );

			if (idsPool != null && !idsPool.isShutdown()) {
				idsPool.shutdown();
			}
		} catch (PcapNativeException e) {
			logger.error("", e);
		} catch (NotOpenException e) {
			logger.error("", e);
		} catch( Exception e ) {
			logger.error("", e);
		}
	}


	private void processMissing( ) throws InterruptedException
	{
		log.debug("processing missing id entries...");
		idsPool.submit(this);
	}

	private NetID createMissingPrint( String ipAddr )
	{
		NetID res = new NetID( MacAddress.ETHER_BROADCAST_ADDRESS.toString(), ipAddr, "localhost" );
		printsToFind.add( res );
		return res;
	}

	public NetID getPrint( String ip ) throws ExecutionException, InterruptedException {
		NetID res = registry.get( ip );
		if ( res == null )
		{
			NetID p = createMissingPrint( ip );
			processMissing( );
			return p;
		}
		else if( res.getHostname() == "localhost" || res.getMacAddress() == MacAddress.ETHER_BROADCAST_ADDRESS.toString() )
		{
			processMissing( );
			return res;
		}
		else
		{
			return res;
		}
	}

	public NetID getPrint( String ip, int duration ) throws ExecutionException, InterruptedException {
		NetID res = registry.get( ip );
		while( duration > 0 && ( res == null || res.getHostname() == "localhost" )  )
		{
			log.trace(".");
			duration--;
			res = registry.get( ip );
			processMissing();
		}
		return res;
	}

	public Future<NetID> obtainPrint( String ip ) {
		final String ipAddr;
		ipAddr = ip;
		return idsPool.submit(new Callable<NetID>() {
			//@Override
			public NetID call() throws Exception {
				return getPrint( ipAddr, 10);
			}
		});
	}

	public void run( )
	{
		log.trace("processing list of missing");
		synchronized( this ){
			ListIterator<NetID> l = printsToFind.listIterator();

			while ( l.hasNext() ){

				NetID np = l.next();
				log.debug("processing missing item {}", np.getIpAdress());
				try {
					np = registry.get( np.getIpAdress() );
				} catch (ExecutionException e) {
					logger.error("", e);
				}
				if( np.getHostname().equals("localhost")  )
				{
					askHostname( np.getIpAdress() );
				} 

				if ( np.getMacAddress().equals( MacAddress.ETHER_BROADCAST_ADDRESS.toString() ) )
				{
					askMacAddress( np.getIpAdress() );
				}
				else if( !np.getHostname().equals("localhost") )
				{
					l.remove();
				}
			} 
		}
	}


	public void askHostname( String ip )
	{
		try {
			sender.sendReverseDnsRequest( ip );
		} catch (Exception e) {
			logger.error("", e);
		}

	}

	public void askMacAddress( String ip )
	{
		try {
			sender.sendMacRequest( ip );
		} catch (PcapNativeException e) {
			logger.error("", e);
		} catch (NotOpenException e) {
			logger.error("", e);
		}
	}

	public void receiveHostInfo( String ipAddr, String hostname ) 
	{
		log.trace( "recording Host info, ip: " + ipAddr + " host: " + hostname );

		// TODO: can we avoid these copies? Guava caches may have a solution to this
		if( !hostname.equals("localhost") && !hostname.equals(ipAddr) )
		{
			NetID np;
			try {
				np = registry.get( ipAddr );
				np.setHostname( hostname );
				np.setLastUpdated( new Date() );
				log.trace( "Updated netprint record with host info: " + np );
				registry.put( ipAddr, np );			
			} catch (ExecutionException e) {
				log.warn( "Execution exception while accessing prints cache " + 
						e.getLocalizedMessage() + "\n" + e.getStackTrace().toString() );
			}
		}
	}

	public void receiveMacInfo( String ipAddr, String macAddress ) 
	{
		log.trace( "recording MAC info, ip: " + ipAddr + " mac: " + macAddress );
		if( ! macAddress.equals( MacAddress.ETHER_BROADCAST_ADDRESS.toString() ) )
		{
			NetID np;
			try {
				np = registry.get( ipAddr );
				np.setMacAddress( macAddress );
				np.setLastUpdated( new Date() );
				log.trace( "Updated netprint record with mac address: " + np );
				registry.put( ipAddr, np );
			} catch (ExecutionException e) {
				log.warn( "Execution exception while accessing prints cache " + 
						e.getLocalizedMessage() + "\n" + e.getStackTrace().toString() );
			}
		}
	}
}
