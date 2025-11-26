package cs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import cs.controller.AuthControllerTest;
import cs.controller.TierControllerTest;
import cs.dto.security.GuiPermissionsTest;
import cs.service.PermissionServiceTest;
import cs.service.RSAEncryptionServiceTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ RSAEncryptionServiceTest.class,
	PermissionServiceTest.class,
	TierControllerTest.class,
	AuthControllerTest.class,
	GuiPermissionsTest.class/*,
	NetIDsTest.class*/})


public class MainTest
{

}
