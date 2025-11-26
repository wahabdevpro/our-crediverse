package cs.controller;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cs.dto.data.BaseRequest;
import cs.dto.data.BaseResponse;
import cs.dto.error.GuiGeneralException;
import cs.dto.error.GuiValidationException;
import cs.dto.security.AuthenticationData.AuthenticationState;
import cs.dto.security.DataWrapper;
import cs.dto.security.LoginSessionData;
import cs.service.ContextService;
import cs.service.GuiAuthenticationService;
import cs.service.RSAEncryptionService;
import cs.utility.Common;

@RestController
@RequestMapping(value={"/auth", "//auth"}) // TODO we shouldn't need the second one. But our javascript uses it in error
public class AuthController
{
	private static Logger logger = LoggerFactory.getLogger(AuthController.class);

	@Autowired
	private GuiAuthenticationService authService;

	@Autowired
	private RSAEncryptionService rsaService;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private ContextService contextService;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public DataWrapper authenticate(/*@RequestParam(value = "cid") String companyid,*/@RequestParam(value = "coauth", required=false) boolean coauth, @RequestParam(value = "forTransactionId", required=false) String forTransactionId, Locale locale) throws Exception
	{
		sessionData.setLocale(locale);
		BaseResponse resp = authService.authenticate(contextService.getCompanyID(), coauth, forTransactionId);
		DataWrapper response = new DataWrapper();
		try
		{
			response.setAuth(rsaService.getPublicKey());
			response.setData(mapper.writeValueAsString(resp));
		}
		catch (JsonProcessingException e)
		{
			sessionData.setCurrentState(AuthenticationState.INVALID);
			response.setError(e.getLocalizedMessage());
			response.setData(Common.CONST_FATAL_ERROR);
			logger.error("", e);
		}
		return response;
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public DataWrapper process(@RequestBody(required = true) DataWrapper requestToProcess, Locale locale) throws Exception
	{
		DataWrapper response = new DataWrapper();
		try
		{
			sessionData.setLocale(locale);
			String data = requestToProcess.getData();
			logger.info("Decrypting ||"+data+"||");
			// It seems that freeipa at lest does not do any normalization before hashing ... thefore the passwords for it are normalization sensitive ... i.e. whatever normalization was used when the password was set must be used when the password was entered.
			// If some normalization should be applied for backend auth mechanism it is best to keep it in the TS instead of in the WUI
			BaseRequest req = mapper.readValue(rsaService.decrypt(data), BaseRequest.class);
			sessionData.captureData(req);
			req.setParentUuid(requestToProcess.getParentUuid());
			Object resp = authService.process(req);
			response.setAuth(rsaService.getPublicKey());
			response.setData(mapper.writeValueAsString(resp));
		}
		catch(GuiValidationException | GuiGeneralException ex)
		{
			throw ex;
		}
		catch (Throwable e)
		{
			response.setData(Common.CONST_FATAL_ERROR);
			response.setError(e.getLocalizedMessage());
			sessionData.setCurrentState(AuthenticationState.INVALID);
			logger.error("", e);
		}
		return response;
	}

	@RequestMapping(value = "logout", method = { RequestMethod.POST, RequestMethod.GET })
	public String logout(Locale locale)
	{
		sessionData.setLocale(locale);
		DataWrapper response = new DataWrapper();
		authService.logout();
		response.setData("success");
		return "#dashboard";
	}

}
