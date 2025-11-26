package hxc.testsuite;

public class RunAllTests extends RunAllTestsBase
{
	
	public RunAllTests()
	{
		// @formatter:off
		this.testClasses = new Class<?>[] {
			hxc.connectors.smtp.SmtpConnectorTests.class,
			hxc.services.language.LanguageChangeTest.class,
			hxc.connectors.air.ProxyTest.class,
			hxc.connectors.database.mysql.MySqlProfilingTest.class,
			hxc.connectors.database.mysql.MySqlTest.class,
			hxc.connectors.file.FileProcessingTest.class,
			hxc.connectors.hsx.protocol.HsxProtocolTest.class,
			hxc.connectors.hux.protocol.HuxProtocolTest.class,
			hxc.connectors.smpp.SMSTest.class,
			hxc.connectors.smpp.SmppConnectorTests.class,
			hxc.connectors.snmp.TrapTest.class,
			hxc.connectors.snmp.SnmpConnectorFitnessTest.class,
			hxc.connectors.lifecycle.LifecycleTest.class,
			hxc.servicebus.ConfigurationPersistenceTest.class,
			hxc.servicebus.ConfigurationTest.class,
			hxc.servicebus.ThrottlingTest.class,
			hxc.servicebus.TPSLimitTest.class,
			hxc.servicebus.ThroughputTest.class,
			hxc.servicebus.notifications.NotificationsTest.class,
			hxc.services.logging.LoggerServiceTest.class,
			hxc.services.logging.LoggerSpeedTest.class,
			hxc.services.security.SecurityTest.class,
			hxc.services.transactions.CsvCdrTest.class,
			hxc.services.transactions.NumberingTest.class,
			hxc.services.transactions.RollbackTest.class,
			hxc.utils.reflection.ReflectionHelperTest.class,
			hxc.utils.xmlrpc.XmlRpcClientServerTest.class,
			hxc.utils.xmlrpc.XmlRpcTest.class,
			hxc.utils.calendar.DateTimeTest.class,
			hxc.services.numberplan.NumberPlanTest.class,
			hxc.connectors.hux.push.PushUssdTest.class,
//			hxc.services.credittransfer.CreditTransferTest.class,
//			hxc.services.pin.PinServiceTest.class,
			hxc.services.advancedtransfer.VASCommandTest.class,
			hxc.services.sharedaccounts.SharedAccountsTest.class,
			hxc.services.sharedaccounts.EnterpriseAccountsTest.class,
		};
		// @formatter:on
	}
	public static void main(String []  args)
	{
		(new RunAllTests()).startTests(args);
	}
}
