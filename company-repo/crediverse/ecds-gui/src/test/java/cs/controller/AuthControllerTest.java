package cs.controller;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Locale;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cs.config.UnitTestConstants;
import cs.dto.data.BaseRequest;
import cs.dto.security.AuthenticationData;
import cs.dto.security.AuthenticationData.AuthenticationState;
import cs.dto.security.DataWrapper;
import cs.dto.security.LoginSessionData;
import cs.service.RSAEncryptionService;
import hxc.ecds.protocol.rest.AuthenticationResponse;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment=WebEnvironment.MOCK)
public class AuthControllerTest
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
			System.err.println(description);
		}

	}

	@Autowired
	private AuthController authController;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RSAEncryptionService encryptionService;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private LoginSessionData sessionData;

	private MockRestServiceServer mockServer;

	private ResourceBundle resourceBundle;

	@Before
	public void before() throws Exception
	{
		sessionData.setSessionAuthentication(new AuthenticationData());
		mockServer = MockRestServiceServer.createServer(restTemplate);
		this.resourceBundle = ResourceBundle.getBundle("ecdsrest/restresponse");
	}

	@Test
	public void testNoContent() throws Exception
	{
		mockServer.expect(requestTo(UnitTestConstants.CONST_AUTH_URL)).andExpect(method(HttpMethod.POST)).andExpect(jsonPath("$.companyID", equalTo(Integer.parseInt(UnitTestConstants.CONST_COMPANY_ID))));

		//testErrorResponse();
	}

	/*
	 * Sends companyid, expects prompt for username in response.
	 */
	//@Test
	public void testAuthenticate() throws Exception
	{
		// Get response from ecdsrest/restresponse.properties file
		String restResponse = this.resourceBundle.getString("authenticateWithCompanyIdResponse");

		mockServer.expect(requestTo(UnitTestConstants.CONST_AUTH_URL)).andExpect(method(HttpMethod.POST))
				// .andExpect(jsonPath("$.companyID",
				// startsWith(UnitTestConstants.CONST_COMPANY_ID)))
				.andExpect(jsonPath("$.companyID").value(Integer.parseInt(UnitTestConstants.CONST_COMPANY_ID))).andRespond(withSuccess(restResponse, MediaType.APPLICATION_JSON));

		DataWrapper response = null;
		try
		{
			response = authController.authenticate(false, null, Locale.ENGLISH);
		}
		catch(Throwable ex)
		{
			ex.printStackTrace();
		}

		// see https://github.com/jayway/JsonPath/tree/master/json-path-assert
		String jsonResponse = response.getData();
		// Verify validity of JSON
		assertThat(jsonResponse, isJson());

		// Verify existence (or non-existence) of JSON path
//		assertThat(jsonResponse, hasJsonPath("$.cid", equalTo(UnitTestConstants.CONST_COMPANY_ID)));
		//assertThat(jsonResponse, hasJsonPath("$.state", equalTo("REQUIRE_UTF8_USERNAME")));
		assertThat(jsonResponse, hasNoJsonPath("$.moreInformationRequired"));
	}

	/*
	 * Sends encrypted username Expects to be prompted for an RSA encoded password.
	 */
	//@Test
	public void testProcessUsername() throws Exception
	{
		// Get response from ecdsrest/restresponse.properties file
		String restResponse = this.resourceBundle.getString("processUsernameWithPasswordResponse");

		sessionData.setCurrentState(AuthenticationState.REQUIRES_TEXT);
		sessionData.setIpAddress(UnitTestConstants.CONST_TEST_IP);

		mockServer.expect(requestTo(UnitTestConstants.CONST_AUTH_URL))
		.andExpect(method(HttpMethod.POST))
		.andExpect(jsonPath("$.companyID", startsWith(UnitTestConstants.CONST_COMPANY_ID)))
			.andRespond(withSuccess(restResponse, MediaType.APPLICATION_JSON));

		DataWrapper req1 = new DataWrapper();
		BaseRequest req2 = new BaseRequest();
		req2.setCid(UnitTestConstants.CONST_COMPANY_ID);
		req2.setData(UnitTestConstants.CONST_USER_1);
		try
		{
			req1.setData(encryptionService.encrypt(mapper.writeValueAsString(req2).getBytes()));
		}
		catch (JsonProcessingException e)
		{
			e.printStackTrace();
		}

		DataWrapper response;
		try
		{
			response = authController.process(req1, Locale.ENGLISH);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}

		// see https://github.com/jayway/JsonPath/tree/master/json-path-assert
		String jsonResponse = response.getData();
		// Verify validity of JSON
		assertThat(jsonResponse, isJson());

		// Verify existence (or non-existence) of JSON path
//		assertThat(jsonResponse, hasJsonPath("$.cid", equalTo(UnitTestConstants.CONST_COMPANY_ID)));
		//assertThat(jsonResponse, hasJsonPath("$.state", equalTo("REQUIRE_RSA_PASSWORD")));
		assertThat(jsonResponse, hasNoJsonPath("$.moreInformationRequired"));
	}

	/*
	 * Sends encrypted username Expects to be prompted for OTP.
	 */
	//@Test
	public void testProcessOtp() throws Exception
	{
		// Get response from ecdsrest/restresponse.properties file
		String restResponse = this.resourceBundle.getString("processUsernameWithOtpResponse");

		sessionData.setCurrentState(AuthenticationState.REQUIRES_TEXT);
		sessionData.setIpAddress(UnitTestConstants.CONST_TEST_IP);

		mockServer.expect(requestTo(UnitTestConstants.CONST_AUTH_URL)).andExpect(method(HttpMethod.POST)).andExpect(jsonPath("$.companyID", equalTo(Integer.parseInt(UnitTestConstants.CONST_COMPANY_ID))))
				.andRespond(withSuccess(restResponse, MediaType.APPLICATION_JSON));

		DataWrapper req1 = new DataWrapper();
		BaseRequest req2 = new BaseRequest();
		req2.setCid(UnitTestConstants.CONST_COMPANY_ID);
		req2.setData(UnitTestConstants.CONST_USER_1);
		try
		{
			req1.setData(encryptionService.encrypt(mapper.writeValueAsString(req2).getBytes()));
		}
		catch (JsonProcessingException e)
		{
			e.printStackTrace();
		}

		DataWrapper response;
		try
		{
			response = authController.process(req1, Locale.ENGLISH);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}

		// see https://github.com/jayway/JsonPath/tree/master/json-path-assert
		String jsonResponse = response.getData();
		// Verify validity of JSON
		assertThat(jsonResponse, isJson());

		// Verify existence (or non-existence) of JSON path
//		assertThat(jsonResponse, hasJsonPath("$.cid", equalTo(UnitTestConstants.CONST_COMPANY_ID)));
		//assertThat(jsonResponse, hasJsonPath("$.state", equalTo("REQUIRE_UTF8_OTP")));
		assertThat(jsonResponse, hasNoJsonPath("$.moreInformationRequired"));
	}

	/*
	 * Sends encrypted username Expects to be prompted for new password.
	 */
	//@Test
	public void testProcessNewPassword() throws Exception
	{
		// Get response from ecdsrest/restresponse.properties file
		String restResponse = this.resourceBundle.getString("processUsernameWithNewPasswordResponse");

		sessionData.setCurrentState(AuthenticationState.REQUIRES_RSA);
		sessionData.setIpAddress(UnitTestConstants.CONST_TEST_IP);
		Key publicKey = null;
		@SuppressWarnings("unused")
		Key privateKey = null;
		try
		{
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
			kpg.initialize(1024);
			KeyPair kp = kpg.genKeyPair();
			publicKey = kp.getPublic();
			privateKey = kp.getPrivate();
		}
		catch (NoSuchProviderException | NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}

		sessionData.setCurrentState(AuthenticationState.REQUIRES_RSA);
		sessionData.setCurrentState(AuthenticationResponse.CODE_OK_NOW_REQUIRE_RSA_PASSWORD, publicKey.getEncoded(), null);

		mockServer.expect(requestTo(UnitTestConstants.CONST_AUTH_URL)).andExpect(method(HttpMethod.POST)).andExpect(jsonPath("$.companyID", equalTo(Integer.parseInt(UnitTestConstants.CONST_COMPANY_ID))))
				.andRespond(withSuccess(restResponse, MediaType.APPLICATION_JSON));

		DataWrapper req1 = new DataWrapper();
		BaseRequest req2 = new BaseRequest();
		req2.setCid(UnitTestConstants.CONST_COMPANY_ID);
		req2.setData(UnitTestConstants.CONST_USER_1);
		try
		{
			req1.setData(encryptionService.encrypt(mapper.writeValueAsString(req2).getBytes()));
		}
		catch (JsonProcessingException e)
		{
			e.printStackTrace();
		}

		DataWrapper response;
		try
		{
			response = authController.process(req1, Locale.ENGLISH);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}

		// see https://github.com/jayway/JsonPath/tree/master/json-path-assert
		String jsonResponse = response.getData();
		// Verify validity of JSON
		assertThat(jsonResponse, isJson());

		// Verify existence (or non-existence) of JSON path
//		assertThat(jsonResponse, hasJsonPath("$.cid", equalTo(UnitTestConstants.CONST_COMPANY_ID)));
		//assertThat(jsonResponse, hasJsonPath("$.state", equalTo("REQUIRE_RSA_NEW_PASSWORD")));
		assertThat(jsonResponse, hasNoJsonPath("$.moreInformationRequired"));
	}

	/*
	 * Sends encrypted password Expects to be authenticated.
	 */
	//@Test
	public void testProcessPassword() throws Exception
	{
		// Get response from ecdsrest/restresponse.properties file
		String restResponse = this.resourceBundle.getString("processPasswordWithAuthenticatedResponse");
		Key publicKey = null;
		@SuppressWarnings("unused")
		Key privateKey = null;
		try
		{
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
			kpg.initialize(1024);
			KeyPair kp = kpg.genKeyPair();
			publicKey = kp.getPublic();
			privateKey = kp.getPrivate();
		}
		catch (NoSuchProviderException | NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}

		sessionData.setCurrentState(AuthenticationState.REQUIRES_RSA);
		sessionData.setIpAddress(UnitTestConstants.CONST_TEST_IP);
		sessionData.setCurrentState(AuthenticationResponse.CODE_OK_NOW_REQUIRE_RSA_PASSWORD, publicKey.getEncoded(), null);

		mockServer.expect(requestTo(UnitTestConstants.CONST_AUTH_URL)).andExpect(method(HttpMethod.POST)).andExpect(jsonPath("$.companyID", equalTo(Integer.parseInt(UnitTestConstants.CONST_COMPANY_ID))))
				.andRespond(withSuccess(restResponse, MediaType.APPLICATION_JSON));

		DataWrapper req1 = new DataWrapper();
		BaseRequest req2 = new BaseRequest();
		req2.setCid(UnitTestConstants.CONST_COMPANY_ID);
		req2.setData(UnitTestConstants.CONST_PASSWORD_2);
		try
		{
			req1.setData(encryptionService.encrypt(mapper.writeValueAsString(req2).getBytes()));
		}
		catch (JsonProcessingException e)
		{
			e.printStackTrace();
		}

		DataWrapper response;
		try
		{
			response = authController.process(req1, Locale.ENGLISH);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}

		// see https://github.com/jayway/JsonPath/tree/master/json-path-assert
		String jsonResponse = response.getData();
		// Verify validity of JSON
		assertThat(jsonResponse, isJson());

		// Verify existence (or non-existence) of JSON path
//		assertThat(jsonResponse, hasJsonPath("$.cid", equalTo(UnitTestConstants.CONST_COMPANY_ID)));
		//assertThat(jsonResponse, hasJsonPath("$.state", equalTo("AUTHENTICATED")));
		assertThat(jsonResponse, hasNoJsonPath("$.moreInformationRequired"));
	}

    /*
     * TODO - This method is NOT USED ... but there are tests in the local code that are commented out... these tests SHOULD BE revisited to determine validity
     */
    /*
	private void testErrorResponse() throws Exception
	{
		sessionData.setCurrentState(AuthenticationState.REQUIRES_TEXT);
		sessionData.setIpAddress(UnitTestConstants.CONST_TEST_IP);

		DataWrapper req1 = new DataWrapper();
		BaseRequest req2 = new BaseRequest();
		req2.setCid(UnitTestConstants.CONST_COMPANY_ID);
		req2.setData(UnitTestConstants.CONST_PASSWORD_2);
		try
		{
			String toEncrypt = mapper.writeValueAsString(req2);
			byte[] toEncryptBytes = toEncrypt.getBytes();
			String encryptedData = encryptionService.encrypt(toEncryptBytes);
			req1.setData(encryptedData);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		DataWrapper response;
		try
		{
			response = authController.process(req1, Locale.ENGLISH);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}

		// see https://github.com/jayway/JsonPath/tree/master/json-path-assert
		String jsonResponse = response.getData();
		// Verify validity of JSON
		assertThat(jsonResponse, isJson());

		// Verify existence (or non-existence) of JSON path
//		assertThat(jsonResponse, hasJsonPath("$.cid", equalTo(UnitTestConstants.CONST_COMPANY_ID)));
		assertThat(jsonResponse, hasJsonPath("$.state", equalTo("INVALID")));
		assertThat(jsonResponse, hasNoJsonPath("$.moreInformationRequired"));
	}
    */

	/*
	 * Test bad request exception
	 */
//	@Test
//	public void testBadRequest() throws Exception
//	{
//
//		mockServer.expect(requestTo(UnitTestConstants.CONST_AUTH_URL)).andExpect(method(HttpMethod.POST)).andExpect(jsonPath("$.companyID", equalTo(Integer.parseInt(UnitTestConstants.CONST_COMPANY_ID))))
//				.andRespond(withBadRequest());
//
//		testErrorResponse();
//	}
//
//	/*
//	 * Test server error exception
//	 */
//	@Test
//	public void testServerError() throws Exception
//	{
//
//		mockServer.expect(requestTo(UnitTestConstants.CONST_AUTH_URL)).andExpect(method(HttpMethod.POST)).andExpect(jsonPath("$.companyID", equalTo(Integer.parseInt(UnitTestConstants.CONST_COMPANY_ID))))
//				.andRespond(withServerError());
//
//		testErrorResponse();
//	}
//
//	/*
//	 * Test unauthorized exception
//	 */
//	@Test
//	public void testUnauthorized() throws Exception
//	{
//
//		mockServer.expect(requestTo(UnitTestConstants.CONST_AUTH_URL)).andExpect(method(HttpMethod.POST)).andExpect(jsonPath("$.companyID", equalTo(Integer.parseInt(UnitTestConstants.CONST_COMPANY_ID))))
//				.andRespond(withUnauthorizedRequest());
//
//		testErrorResponse();
//	}
//
//	/*
//	 * Test no content
//	 */
//	@Test
//	public void testNoContent() throws Exception
//	{
//		mockServer.expect(requestTo(UnitTestConstants.CONST_AUTH_URL)).andExpect(method(HttpMethod.POST)).andExpect(jsonPath("$.companyID", equalTo(Integer.parseInt(UnitTestConstants.CONST_COMPANY_ID))))
//		.andRespond(withNoContent());
//
//		testErrorResponse();
//	}
}
