package hxc.test;

import hxc.connectors.air.AirConnector;
import hxc.connectors.archiving.ArchivingConnector;
import hxc.connectors.cai.CaiConnector;
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.datawarehouse.DataWarehouseConnector;
import hxc.connectors.diagnostic.DiagnosticConnector;
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
import hxc.services.ecds.CreditDistribution;
import hxc.services.logging.LoggerService;
import hxc.services.numberplan.NumberPlanService;
import hxc.services.reporting.ReportingService;
import hxc.services.security.SecurityService;
import hxc.services.transactions.TransactionService;
import hxc.testsuite.RunAllTestsBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestHost extends RunAllTestsBase implements Runnable {
	final static Logger logger = LoggerFactory.getLogger(TestHost.class);
	
	private IServiceBus esb;

	private boolean run = true;

	private void init(String[] args)
	{
		try {
			esb = ServiceBus.getInstance();
	
			// Implement Configuration
			//addECDSPlugins(esb);
			addClassicPlugins (esb);
			// Now Start
			if (!esb.start(args))
			{
				System.err.println("Problem Encountered when trying to start ... Check logs");
				System.err.println("/var/opt/cs/c4u/log/log.tmp");
			}
			else
			{
				System.out.println("Go!");
			}
			
			(new Thread(this)).start();
		}
		catch(Exception ex) {
			logger.error("Cannot instantiate Service Bus", ex);
		}
		 
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
		//esb.registerService(new CaiSimService());
		
		// Connectors
		esb.registerConnector(new AirConnector());
		esb.registerConnector(new ArchivingConnector());
		
		esb.registerConnector(new CaiConnector());
		esb.registerConnector(new DataWarehouseConnector());
		esb.registerConnector(new DiagnosticConnector());
		esb.registerConnector(new FileConnector());
		esb.registerConnector(new HmxConnector());
		esb.registerConnector(new HuxConnector());
		esb.registerConnector(new KerberosConnector());
		
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
		esb.registerConnector(new DataWarehouseConnector());
		esb.registerConnector(new LifecycleConnector());
		esb.registerConnector(new CtrlConnector());		
		esb.registerService(new ReportingService());	
		esb.registerService(new TransactionService());
//		esb.registerConnector(new TamperCheckConnector());
	}
	/**
	 * This is plugins required for the ECDS project
	 */
	public void addClassicPlugins(IServiceBus esb)
	{
		// Services
		LoggerService loggerService = new LoggerService();
		esb.registerService(loggerService);
		
		// Connectors
		esb.registerConnector(new AirConnector());
		esb.registerConnector(new CtrlConnector());
		esb.registerConnector(new ArchivingConnector());
		esb.registerConnector(new DataWarehouseConnector());
		esb.registerConnector(new DiagnosticConnector());
		esb.registerConnector(new FileConnector());
		esb.registerConnector(new HuxConnector());
		esb.registerConnector(new LifecycleConnector());
		
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerConnector(new SoapConnector());
		esb.registerConnector(new SnmpConnector());
		esb.registerConnector(new SmppConnector());
		esb.registerConnector(new UiConnector());
		esb.registerConnector(new C4UTestConnector());
		
		// Services
		esb.registerService(new TransactionService());
//		esb.registerConnector(new KerberosConnector());
		esb.registerService(new AirSimService());
//		esb.registerService(new LanguageService());
		esb.registerService(new NumberPlanService());		
		esb.registerService(new SecurityService());
		esb.registerService(new ReportingService());
		esb.registerService(new SecurityService());
		//esb.registerService(new ClassicM2U());
		//esb.registerService(new ClassicFAF());
		//esb.registerService(new ClassicCMBK());
		//esb.registerService(new ClassicCVR());		
		//esb.registerService(new ClassicWAMI());		
//		esb.registerService(new AdvancedTransfer());
//		esb.registerService(new CallMeBackService());
//		esb.registerService(new FriendsAndFamilyService());
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
		TestHost th = new TestHost();
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
