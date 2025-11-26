package hxc.test;

import hxc.connectors.air.AirConnector;
import hxc.connectors.archiving.ArchivingConnector;
import hxc.connectors.cai.CaiConnector;
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.diagnostic.DiagnosticConnector;
import hxc.connectors.ecdsapi.EcdsApiRestConnector;
import hxc.connectors.file.FileConnector;
import hxc.connectors.hmx.HmxConnector;
import hxc.connectors.hux.HuxConnector;
import hxc.connectors.kerberos.KerberosConnector;
import hxc.connectors.lifecycle.LifecycleConnector;
import hxc.connectors.pcc_bundles.PccBundleConnector;
import hxc.connectors.smpp.SmppConnector;
import hxc.connectors.smtp.SmtpConnector;
import hxc.connectors.snmp.SnmpConnector;
import hxc.connectors.soap.SoapConnector;
import hxc.connectors.sut.C4UTestConnector;
import hxc.connectors.sut.PCCTestConnector;
import hxc.connectors.ui.UiConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.airsim.AirSimService;
import hxc.services.caisim.CaiSimService;
import hxc.services.ecds.CreditDistribution;
import hxc.services.logging.LoggerService;
import hxc.services.numberplan.NumberPlanService;
import hxc.services.reporting.ReportingService;
import hxc.services.security.SecurityService;
import hxc.services.transactions.TransactionService;
import hxc.testsuite.RunAllTestsBase;
import hxc.servicebus.VersionInfo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EcdsTestHost extends RunAllTestsBase implements Runnable {
	final static Logger logger = LoggerFactory.getLogger(EcdsTestHost.class);
	
	private IServiceBus esb;

	private boolean run = true;

	private void init(String[] args)
	{
		try {
			esb = ServiceBus.getInstance();
			logger.info("ecds-ts Service GithubTag:" + VersionInfo.GITHUB_TAG + "  DockerTag:" + VersionInfo.DOCKER_TAG + "  BranchName:" + VersionInfo.BRANCH_NAME + "  BuildNumber:" + VersionInfo.BUILD_NUMBER + "  BuildDateTime:" + VersionInfo.BUILD_DATE_TIME + "  CommitRef:" +  VersionInfo.BUILD_COMMIT_REF +" starting ...");

			// Implement Configuration
			addECDSPlugins(esb);
			// Now Start
			if (!esb.start(args))
			{
				logger.error("Problem Encountered when trying to start ... Check logs"
									 + "\n/var/opt/cs/c4u/log/log.tmp");
				System.out.print("\n+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+\n");
				System.out.print("  FAILED TO START ... please review logs above\n");
				System.out.print("+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+\n\n");
			}
			else
			{
				logger.info("Successful Start ... Loading banner");
				loadBanner();
				logger.info("Go!");
			}
			
			(new Thread(this)).start();
		}
		catch (Exception ex) {
			logger.error("Cannot instantiate Service Bus", ex);
		}
		 
	}	

	private void loadBanner() {
		String userDir = System.getProperty("user.dir");
		String bannerPath = "common/banner.txt";

		String filePaths[] = {
			userDir + "/../" + bannerPath, // DEVELOPMENT PATH
			userDir + "/../../../../../" + bannerPath // PRODUCTION PATH
		};

		BufferedReader br = this.getBannerFile(filePaths);
		if (br == null) {
			return;
		}
		 
		 try {
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.contains("year")) {
					line = line.replace("${year}", new SimpleDateFormat("YYYY").format(new Date()));
				}
				System.out.println(line);
			}
			System.out.println("\n Started Successfully\n\n");

		 } catch(Exception e) {
			e.printStackTrace();
			logger.warn("Unable to read banner file .... (NO BEARING ON STARTUP SUCCESS)");
		 }

	}

	private BufferedReader getBannerFile(String[] filePaths) {
		Exception finalError = new Exception();
		for(String filePath : filePaths) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "utf8"));
				finalError = null;
				return br;
			} catch (Exception e) {
				logger.debug("Failing to find this Banner Filepath:" + filePath);
				finalError = e;
			}
		}
		if (finalError != null) {
			finalError.printStackTrace();
			logger.warn("Unable to load banner .... (NO BEARING STARTUP SUCCESS)");
		}
		return null;
	}

	/**
	 * This is plugins required for the ECDS project
	 */
	public void addECDSPlugins(IServiceBus esb)
	{
		
		// Services
		LoggerService loggerService = new LoggerService();
		esb.registerService(loggerService);
		esb.registerService(new SecurityService());
		esb.registerService(new NumberPlanService());
		esb.registerService(new CreditDistribution());
		esb.registerService(new CaiSimService());
		
		// Connectors
		esb.registerConnector(new AirConnector());
		esb.registerConnector(new ArchivingConnector());
		
		esb.registerConnector(new CaiConnector());
		esb.registerConnector(new DiagnosticConnector());
		esb.registerConnector(new FileConnector());
		esb.registerConnector(new HmxConnector());
		esb.registerConnector(new HuxConnector());
		esb.registerConnector(new KerberosConnector());
		
		esb.registerConnector(new EcdsApiRestConnector());
		
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());		
		esb.registerConnector(new PccBundleConnector());
		esb.registerConnector(new PCCTestConnector());
		esb.registerConnector(new SmppConnector());
		esb.registerConnector(new SoapConnector());
		esb.registerConnector(new SnmpConnector());
		esb.registerConnector(new SmtpConnector());
		esb.registerConnector(new UiConnector());
                 
		// Simulation
		esb.registerService(new AirSimService());
		esb.registerConnector(new C4UTestConnector());
		esb.registerConnector(new LifecycleConnector());
		esb.registerConnector(new CtrlConnector());		
		esb.registerService(new ReportingService());	
		esb.registerService(new TransactionService());
//		esb.registerConnector(new TamperCheckConnector());
	}

	/**
	 * This is the plugins required for the basic ACT configuration
	 */
	public void addBasicACTPlugins(IServiceBus esb)
	{
		/*
		 * // Connectors esb.registerConnector(new AirConnector()); esb.registerConnector(new ArchivingConnector()); esb.registerConnector(new DataWarehouseConnector()); esb.registerConnector(new
		 * DiagnosticConnector()); esb.registerConnector(new FileConnector()); esb.registerConnector(new HuxConnector()); esb.registerConnector(new LifecycleConnector()) esb.registerConnector(new
		 * MySqlConnector()); esb.registerConnector(new SoapConnector()); esb.registerConnector(new SnmpConnector()); esb.registerConnector(new SmppConnector()); esb.registerConnector(new
		 * UiConnector());
		 * 
		 * // Services esb.registerService(new AdvancedCreditTransfer()); esb.registerService(new AirSimService()); esb.registerService(new LanguageService()); esb.registerService(new
		 * LoggerService()); esb.registerService(new NumberPlanService()) esb.registerService(new SecurityService()); esb.registerService(new ReportingService()); esb.registerService(new
		 * SecurityService());
		 */
	}

	public static void main(String[] args)
	{
		EcdsTestHost th = new EcdsTestHost();
		th.init(args);
	}

	//
	@Override
	public void run()
	{
		while (run)
		{
			try
			{
				synchronized (this)
				{
					Thread.sleep(1000);
				}
			}
			catch (Exception e)
			{
			}
		}
	}

}
