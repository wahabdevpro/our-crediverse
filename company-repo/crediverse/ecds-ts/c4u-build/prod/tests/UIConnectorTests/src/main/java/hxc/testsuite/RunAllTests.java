package hxc.testsuite;

public class RunAllTests extends RunAllTestsBase
{
	
	public RunAllTests()
	{
		// @formatter:off
		this.testClasses = new Class<?>[] {
			hxc.connectors.ui.StartupTest.class,
			hxc.connectors.ui.idchecking.UniqueIdTest.class,
			hxc.connectors.ui.UiControllerConfigurationTest.class,
			hxc.connectors.ui.UiControllerTest.class, 
			hxc.connectors.ui.UIServerTest.class,
			hxc.connectors.ui.UserManagementTest.class,
			hxc.connectors.ui.UiControllerResilienceTests.class,
			hxc.connectors.ui.roles.TestConfigurationRetrivalWithRoles.class,
			hxc.connectors.ui.log.LogFileTest.class,
			hxc.connectors.ui.VasCommandsTest.class,
			hxc.connectors.ui.TestResultCodeText.class // TODO:Resolve
		};
		// @formatter:on
	}
	public static void main(String []  args)
	{
		(new RunAllTests()).startTests(args);
	}
}