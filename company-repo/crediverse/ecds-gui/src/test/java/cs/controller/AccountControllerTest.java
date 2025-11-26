package cs.controller;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

//import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import cs.config.UnitTestConstants;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment=WebEnvironment.MOCK)
//@ContextConfiguration(classes = UnitTestConfiguration.class, loader = AnnotationConfigContextLoader.class)
public class AccountControllerTest
{

	public static class CsStringMatcher extends BaseMatcher<Object>
	{

		@Override
		public boolean matches(Object item)
		{
			System.err.println(item);
			return false;
		}

		@Override
		public void describeTo(Description description)
		{
			// TODO Auto-generated method stub
			System.err.println(description);
		}

	}

	@Autowired
	private RestTemplate restTemplate;

	private MockRestServiceServer mockServer;

	private ResourceBundle resourceBundle;

	@Before
	public void before() throws Exception
	{
		mockServer = MockRestServiceServer.createServer(restTemplate);
		this.resourceBundle = ResourceBundle.getBundle("ecdsrest/restresponse");
	}

	@Test
	public void test()
	{
		assertTrue(true);
	}

	//@Test
	public void testList() throws Exception
	{
		// Get response from ecdsrest/restresponse.properties file
		String restResponse = this.resourceBundle.getString("accountListResponse");

		mockServer.expect(requestTo(UnitTestConstants.CONST_ACCOUNTS_URL))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(restResponse, MediaType.APPLICATION_JSON));
	}
}
