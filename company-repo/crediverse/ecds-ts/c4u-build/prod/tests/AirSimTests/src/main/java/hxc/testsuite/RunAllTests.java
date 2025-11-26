package hxc.testsuite;

public class RunAllTests extends RunAllTestsBase
{
	
	public RunAllTests()
	{
		// @formatter:off
		this.testClasses = new Class<?>[] {
			hxc.services.airsim.tests.ConditionalErrorTest.class,
			hxc.services.airsim.tests.PamTest.class,
			hxc.services.airsim.tests.PsoTest.class,
			hxc.services.airsim.tests.RefillTest.class,
			hxc.services.airsim.tests.TemporalTriggersTest.class,
			hxc.services.airsim.tests.TnpTest.class,
			hxc.services.airsim.tests.UsageTest.class,
			hxc.services.airsim.tests.PersistanceTest.class,
			hxc.services.airsim.tests.CommunityIDTest.class,
		};
		// @formatter:on
	}
	public static void main(String []  args)
	{
		(new RunAllTests()).startTests(args);
	}
}