/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package c4uburntester;

import hxc.services.airsim.AirSimService;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
//import java.io.BufferedReader;
//import java.io.InputStreamReader;

/**
 *
 * @author justinguedes
 */
public class C4uBurnTester
{

	private final static int MAX_THREADS = 10;
	private static int numberOfThreads = 10;
	private static final Date timeStarted = new Date();
	private static Date timeEnded = new Date();
	private static Burner burners[];
	
	private static int repeatsPerTPS = 10;
	private static int minTPS = 1; 
	private static int maxTPS = 100;
	private static int tpsStep = 1;
	private static String url = null;

	private static int providerCount;
	private static int consumerCount;
	
	private static String providerMsisdnFormat;
	private static String consumerMsisdnFormat;
	private static int testCaseID;

	/**
	 * @param args
	 *            the command line arguments
	 * @throws java.io.IOException
	 */
	public static void main(String[] args) throws IOException
	{

		if (args.length < 1)
		{
			System.out.println("Usage: java -jar <jarFile> <configFile>");
			return;
		}
		

		final Properties props = new Properties();
		props.load(new FileInputStream(args[0]));
		
		//Test report handler on exit
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				timeEnded = new Date();

				AirSimService service = new AirSimService();
				service.getAirSimPort().stop();

				for (Burner burner : burners)
				{
					if (burner != null)
					{
						// burner.stop(); //is this call needed???
					}
				}

				try
				{
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");

					String resultsFile = props.getProperty("RESULTS_FILENAME");
					if (resultsFile == null) {
						resultsFile = "test-results.txt";
					}
					resultsFile = String.format("%s-%s", resultsFile.trim(), df.format(new Date()) );

					try ( BufferedWriter writer = new BufferedWriter(new FileWriter(resultsFile)) )
					{
						writer.write(String.format("Time started: %s\nTime ended: %s\n", timeStarted.toString(), timeEnded.toString()));

						long failures = 0;
						long timedOut = 0;
						long successful = 0;
						long suspended = 0;
						long incomplete = 0;
						long serviceBusy = 0;
						long notEligible = 0;
						long invalidQuota = 0;
						long quotaReached = 0;
						long nullResponses = 0;
						long notSubscribed = 0;
						long invalidVariant = 0;
						long inactiveAParty = 0;
						long inactiveBParty = 0;
						long alreadyHasQuota = 0;
						long alreadyConsumer = 0;
						long nullReturnCodes = 0;
						long technicalProblem = 0;
						long alreadySubscribed = 0;
						long insufficientBalance = 0;
						long authorizationFailure = 0;
						
						long total = 0;
						long totalProviders = 0;

						for (int tps=minTPS; tps<=maxTPS; tps+=tpsStep)
						{
							failures = Stats.failures(tps);
							timedOut = Stats.getTimedOut(tps);
							successful = Stats.successful(tps);
							suspended = Stats.getSuspended(tps);
							incomplete = Stats.getIncomplete(tps);;
							serviceBusy = Stats.getServiceBusy(tps);
							notEligible = Stats.getNotEligible(tps);
							invalidQuota = Stats.getInvalidQuota(tps);
							quotaReached = Stats.getQuotaReached(tps);
							nullResponses = Stats.getNullResponses(tps);
							notSubscribed = Stats.getNotSubscribed(tps);
							invalidVariant = Stats.getInvalidVariant(tps);
							inactiveAParty = Stats.getInactiveAParty(tps);
							inactiveBParty = Stats.getInactiveBParty(tps);
							alreadyHasQuota = Stats.getAlreadyHasQuota(tps);
							alreadyConsumer = Stats.getAlreadyConsumer(tps);
							nullReturnCodes = Stats.getNullReturnCodes(tps);
							technicalProblem = Stats.getTechnicalProblem(tps);
							alreadySubscribed = Stats.getAlreadySubscribed(tps);
							insufficientBalance = Stats.getInsufficientBalance(tps);
							authorizationFailure = Stats.getAuthorizationFailure(tps);
							
							total = timedOut + suspended + incomplete + serviceBusy + notEligible + invalidQuota + 
									quotaReached + notSubscribed + invalidVariant + inactiveAParty + inactiveBParty + 
									alreadyHasQuota + alreadyConsumer + technicalProblem + alreadySubscribed + nullResponses +
									insufficientBalance + authorizationFailure + successful + failures + nullReturnCodes;

							totalProviders += total;

							writer.write(String.format("Total[@%d TPS]: %s\nSuccessful: %s\ntechnicalProblems: %s\n", tps, total, successful, technicalProblem));
							writer.write(String.format("alreadySubscibed: %s\n", alreadySubscribed));
							writer.write(String.format("alreadyConsumer: %s\n", alreadyConsumer));
							writer.write(String.format("notSubscribed: %s\n", notSubscribed));
							writer.write(String.format("notEligible: %s\n", notEligible));
							writer.write(String.format("invalidQuota: %s\n", invalidQuota));
							writer.write(String.format("invalidVariant: %s\n", invalidVariant));
							writer.write(String.format("insufficientBalance: %s\n", insufficientBalance));
							writer.write(String.format("alreadyHasQuota: %s\n", alreadyHasQuota));
							writer.write(String.format("inactiveAParty: %s\n", inactiveAParty));
							writer.write(String.format("inactiveBParty: %s\n", inactiveBParty));
							writer.write(String.format("Other failures: %s\n", failures));
							writer.write(String.format("Success Rate [@%d TPS]: %s\nFailure Rate [@%d TPS]: %s", tps, (float) ((float) successful / (float) total) * 100, tps, (float) ((float) failures / (float) total) * 100));
							writer.write("\n=================================================\n");
						}
						writer.write( String.format("\nTested with %d A Numbers\n", totalProviders) );
						writer.write("\n=================================================\n");
					}
				}
				catch (IOException ex)
				{
					System.out.println(ex.getMessage());
				}

			}
		}));

//		AirSimService service = new AirSimService();
//		try
//		{
//			service.getAirSimPort().start();
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//			System.out.println("EXCEPTION caught: " + e.getMessage());
//		}


		numberOfThreads = Integer.parseInt( props.getProperty("THREAD_COUNT", Integer.toString(MAX_THREADS)).trim() );
		minTPS = Integer.parseInt( props.getProperty("MIN_TPS", "10").trim() );
		maxTPS = Integer.parseInt( props.getProperty("MAX_TPS", "200").trim() );
		tpsStep = Integer.parseInt( props.getProperty("TPS_STEP", "10").trim() );
		repeatsPerTPS = Integer.parseInt( props.getProperty("REPEATS_PER_TPS", "10").trim() );
		providerCount = Integer.parseInt( props.getProperty("PROVIDER_COUNT", "1000").trim() );
		providerMsisdnFormat = props.getProperty("PROVIDER_MSISDN_FORMAT", "083XXXXXXX").trim();
		consumerCount = Integer.parseInt( props.getProperty("CONSUMER_COUNT", "4000").trim() );
		consumerMsisdnFormat = props.getProperty("CONSUMER_MSISDN_FORMAT", "084XXXXXXX").trim();
		testCaseID = Integer.parseInt(props.getProperty("TESTCASE_ID","0").trim());
		url = props.getProperty("WSDL_URL", null);

		burners = new Burner[numberOfThreads];
		
		System.out.println( String.format("THREAD_COUNT [%d]", numberOfThreads) );
		System.out.println( String.format("MIN_TPS [%d]", minTPS) );
		System.out.println( String.format("MAX_TPS [%d]", maxTPS) );
		System.out.println( String.format("TPS_STEP [%d]", tpsStep) );
		System.out.println( String.format("REPEATS_PER_TPS [%d]", repeatsPerTPS) ); 
		System.out.println(String.format("Start time: [%s]", (new Date()).toString()));
		
		Thread threads[] = new Thread[numberOfThreads];
		for (int i = 0; i < numberOfThreads; i++)
		{
			final int j = i;

			threads[j] = new Thread()
			//new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						burners[j] = new Burner(props);
						System.out.println( String.format("C4uBurnTester::Thread::run(): Burner[%d]::loadTest() CALLED at %s", j, new Date()) );
						burners[j].loadTest(); 
						System.out.println( String.format("C4uBurnTester::Thread::run(): Burner[%d]::loadTest() RETURNED at %s", j, new Date()) );
					}
					catch (MalformedURLException ex)
					{
						System.out.println("EXCEPTION: " + ex.getMessage());
						System.exit(1);
					}
					
					System.out.println( String.format("Thread[%d] done", j) );
				} //run()

			};
			
			threads[j].start();
			System.out.println( String.format("Thread[%d] started", j) );
			
			try {
				threads[j].join(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

//		System.out.println(String.format("End time: [%s]", (new Date()).toString()));

	}//main()

}
