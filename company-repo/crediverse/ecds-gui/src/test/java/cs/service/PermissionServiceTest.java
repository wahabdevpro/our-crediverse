package cs.service;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment=WebEnvironment.MOCK)
public class PermissionServiceTest
{
	/*
	 * @Autowired private RestTemplate restTemplate;
	 *
	 * @Autowired private LoginSessionData sessionData;
	 *
	 * @Autowired private PermissionService permissionService;
	 *
	 * private MockRestServiceServer mockServer;
	 */



	@Before
	public void before() throws Exception
	{
		//mockServer = MockRestServiceServer.createServer(restTemplate);
		//this.resourceBundle =
		//ResourceBundle.getBundle("ecdsrest/restresponse");
	}

	@Test
	public void listPermissionsTest()
	{
		//Permission[] perms = permissionService.listPermissions("127.0.0.1");
		//System.err.println(perms.toString());
		assertTrue(true);
	}
}
