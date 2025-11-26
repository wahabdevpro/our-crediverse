package hxc.testsuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ hxc.connectors.ui.UiControllerConfigurationTest.class, hxc.connectors.ui.UiControllerTest.class, hxc.connectors.ui.UIServerTest.class, hxc.connectors.ui.UserManagementTest.class })
public class AllTests
{

}
