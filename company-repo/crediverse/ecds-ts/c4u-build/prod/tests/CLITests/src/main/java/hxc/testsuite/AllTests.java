package hxc.testsuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ hxc.ui.cli.test.CLIControllerTest.class, hxc.ui.cli.test.CLIConnectorTest.class })
public class AllTests
{

}
