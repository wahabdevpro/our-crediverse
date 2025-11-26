package hxc.testsuite;

public class RunAllTests extends RunAllTestsBase
{
	
	public RunAllTests()
	{
		// @formatter:off
		this.testClasses = new Class<?>[] {
			AllTests.class
		};
		// @formatter:on
	}
	
	public static void main(String []  args)
	{
		(new RunAllTests()).startTests(args);
	}
}
