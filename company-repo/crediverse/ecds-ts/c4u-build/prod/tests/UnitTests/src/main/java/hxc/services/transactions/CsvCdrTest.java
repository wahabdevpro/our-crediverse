package hxc.services.transactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.connectors.Channels;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.servicebus.ServiceBus;
import hxc.services.logging.LoggerService;
import hxc.services.transactions.TransactionService.TransactionServiceConfig;
import hxc.utils.string.StringUtils;

public class CsvCdrTest
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private static LoggerService logger;
	private static TransactionService tservice;
	private final String directoryName = "/tmp/hxccdr";

	private static int BEFORE_ROTATE_TIME = 5000;
	private static int AFTER_ROTATE_TIME = 10000;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	// /////////////////////////////////

	@Test
	public void testCsvEdr()
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSS");

		File directory = null;
		try
		{
			// Create Directory
			directory = new File(directoryName).getCanonicalFile();
			if (!directory.exists())
				directory.mkdirs();

			// Delete old CDR files in it
			File[] files = getCdrfiles(directory);
			for (File file : files)
			{
				file.delete();
			}

			// Create Transaction Service
			esb = ServiceBus.getInstance();
			esb.stop();
			logger = new LoggerService();
			tservice = new TransactionService();
			TransactionServiceConfig config = (TransactionServiceConfig) tservice.getConfiguration();
			config.setCdrDirectory(directoryName);
			config.setRotationIntervalSeconds(10);
			config.setInterimFileName("tmp.cdr");

			// Start the Service
			esb.registerService(tservice);
			esb.registerService(logger);
			esb.start(null);

			// Create a CDR
			CsvCdr cdr = new CsvCdr();
			try (Transaction<?> transaction = tservice.create(cdr, null))
			{

				Calendar cal = Calendar.getInstance();
				cal.set(2013 - 1900, 10 - 1, 23, 8, 9, 12);

				cdr.setHostName("AS400");
				cdr.setCallerID("BILL");
				cdr.setA_MSISDN("0824452655");
				cdr.setB_MSISDN("0823751483");
				cdr.setStartTime(cal.getTime());
				cdr.setInboundTransactionID("238764982346");
				cdr.setInboundSessionID("4321");
				cdr.setChannel(Channels.IVR);
				cdr.setRequestMode(RequestModes.testOnly);
				cdr.setTransactionID("2013102308091200012");
				cdr.setServiceID("12");
				cdr.setVariantID("B");
				cdr.setProcessID("3");
				cdr.setLastActionID("12");
				cdr.setLastExternalResultCode(100);
				cdr.setChargeLevied(123);
				cdr.setReturnCode(ReturnCodes.notEligible);
				cdr.setRolledBack(true);
				cdr.setFollowUp(true);
				cdr.setAdditionalInformation("You,\r\n owe us\t30 bucks & your life!");

				transaction.complete();
			}
			catch (Exception ex)
			{
				fail(ex.getMessage());
			}

			// After 5 seconds there should still not be a rotated CDR
			Thread.sleep(BEFORE_ROTATE_TIME);
			files = getCdrfiles(directory);
			assertEquals(1, files.length);

			Thread.sleep(AFTER_ROTATE_TIME);
			files = getCdrfiles(directory);
			assertEquals(2, files.length);

			// Stop the Transaction Service
			tservice.stop();

			// Read the CDR File
			FileReader fileReader = new FileReader(files[0].getName().equals(config.getInterimFileName()) ? files[1] : files[0]);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			List<String> lines = new ArrayList<String>();
			while (true)
			{
				String line = bufferedReader.readLine();
				if (line == null)
					break;
				lines.add(line);
			}
			bufferedReader.close();
			fileReader.close();
			assertEquals("Expect only one CDR line", 1, lines.size());

			// Compare Contents
			String[] parts = lines.get(0).split(",");
			assertEquals("Expect 20 Values", 22, parts.length);

			assertEquals(cdr.getHostName(), parts[0]);
			assertEquals(cdr.getCallerID(), parts[1]);
			assertEquals(cdr.getA_MSISDN(), parts[2]);
			assertEquals(cdr.getB_MSISDN(), parts[3]);
			assertEquals(dateFormat.format(cdr.getStartTime()), parts[4]);
			assertEquals(cdr.getInboundTransactionID(), parts[5]);
			assertEquals(cdr.getInboundSessionID(), parts[6]);
			assertEquals(cdr.getChannel().toString(), parts[7]);
			assertEquals(cdr.getRequestMode().toString(), parts[8]);
			assertEquals(cdr.getTransactionID(), parts[9]);
			assertEquals(cdr.getServiceID(), parts[10]);
			assertEquals(cdr.getVariantID(), parts[11]);
			assertEquals(cdr.getProcessID(), parts[12]);
			assertEquals(cdr.getLastActionID(), parts[13]);
			assertEquals(String.format("%d", cdr.getLastExternalResultCode()), parts[14]);
			assertEquals(String.format("%d", cdr.getChargeLevied()), parts[15]);
			assertEquals(cdr.getReturnCode().toString(), parts[16]);
			assertEquals(cdr.isRolledBack() ? "1" : "0", parts[17]);
			assertEquals(cdr.isFollowUp() ? "1" : "0", parts[18]);
			assertEquals(cdr.getAdditionalInformation(), StringUtils.unescapeXml(parts[21]));

		}
		catch (Exception ex)
		{
			fail(ex.getMessage());
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	private File[] getCdrfiles(File directory)
	{
		File[] files = directory.listFiles();
		List<File> result = new ArrayList<File>();
		for (File file : files)
		{
			String name = file.getName().toLowerCase();
			if (name.endsWith(".cdr"))
				result.add(file);
		}

		return result.toArray(new File[] {});
	}
}
