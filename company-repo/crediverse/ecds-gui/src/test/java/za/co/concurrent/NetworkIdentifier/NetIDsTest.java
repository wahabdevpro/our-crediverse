package za.co.concurrent.NetworkIdentifier;

/**
 * Unit test for NetPrints.
 */
public class NetIDsTest
{
	/**
	 * Rigourous Test :-)
	 */
	//@Test
	public void testNetIDs()
	{
/*
		 System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "debug");
		 System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_THREAD_NAME_KEY, "true");
		 System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_DATE_TIME_KEY, "true");
		 System.setProperty(org.slf4j.impl.SimpleLogger.DATE_TIME_FORMAT_KEY, "HH.mm.ss:SSS");
		 final Logger log = LoggerFactory.getLogger(NetIDsTest.class);
		log.trace( "Testing NetIDs..." );
		NetIDs np = new NetIDs( "eth0" );
		int NET_ID_DELAY = 1;

		@SuppressWarnings({ "rawtypes", "unchecked" })
		ArrayList<String> ips =  new ArrayList(Arrays.asList("172.17.9.45", "172.17.9.25", "172.17.8.11"));
		try {
			np.init();
		} catch (Exception e1) {
			log.trace( "Exception while initializing NetIDs: " + e1.getStackTrace().toString() );
		}
		try {
			NetID p = np.getPrint("10.0.0.2");
			Thread.sleep( 10 * NET_ID_DELAY );

			log.info("cache warmup...");
			for (Iterator<String> it = ips.iterator(); it.hasNext();) {
				Thread.sleep(NET_ID_DELAY);
				p = np.getPrint(it.next());
				log.trace( "Got Print: " + p );
			}

			log.info("cache check...");
			for (Iterator<String> it = ips.iterator(); it.hasNext();) {
				Thread.sleep(NET_ID_DELAY);
				p = np.getPrint(it.next());
				log.info( "Got Print: " + p );
			}

			log.info("stress test...");
			for ( int i = 0; i < 10000; ++i )
			{
				for (Iterator<String> it = ips.iterator(); it.hasNext();) {
					Thread.sleep(NET_ID_DELAY);
					p = np.getPrint(it.next());
					log.trace( "Got Print: " + p );
				}
				log.trace( "iteration: " + i );
			}

			log.info("futures stress test...");
			for ( int i = 0; i < 10000; ++i )
			{
				for (Iterator<String> it = ips.iterator(); it.hasNext();) {
					Thread.sleep(NET_ID_DELAY);
					p = np.obtainPrint(it.next()).get();
					log.info( "Got Future Print: " + p );
				}
				log.trace( "iteration: " + i );
			}

			np.terminate();

		} catch (ExecutionException e) {
			log.warn("ExecutionException, while querying NetIDs.");
			e.printStackTrace();
		} catch (InterruptedException e) {
			log.warn("InterruptedException, while querying NetIDs.");
			e.printStackTrace();
		}


		assertTrue( true );
*/
	}
}
