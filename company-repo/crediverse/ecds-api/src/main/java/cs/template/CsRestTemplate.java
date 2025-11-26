package cs.template;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cs.constants.ApplicationConstants;
import cs.constants.MissingApiConstants;
import cs.dto.error.GuiGeneralException;
import cs.dto.error.GuiValidationException;
import cs.dto.security.AuthenticationData;
import cs.dto.security.LoginSessionData;
import cs.service.ContextService;
import cs.service.CorrelationIdService;
import cs.service.SessionService;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.AuthenticationRequest;
import hxc.ecds.protocol.rest.AuthenticationResponse;
import hxc.ecds.protocol.rest.BatchUploadResponse;
import hxc.ecds.protocol.rest.IValidatable;
import hxc.ecds.protocol.rest.ResponseHeader;
import hxc.ecds.protocol.rest.TransactionResponse;
import hxc.ecds.protocol.rest.Violation;
import hxc.ecds.protocol.rest.config.TransactionsConfig;

import static hxc.ecds.protocol.rest.config.TransactionsConfig.ERR_ACC_NOT_FOUND;

public class CsRestTemplate
{
	private static final Logger logger = LoggerFactory.getLogger(CsRestTemplate.class);
	
	@Autowired
	private ContextService ctxt;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private LoginSessionData sessionData;
	
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	@Qualifier("logging")
	private ObjectMapper mapper;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private CorrelationIdService correlationIdService;

	////////////

	public <T> T execute(String url, HttpMethod method, ParameterizedTypeReference<T> responseType, AuthenticationData authData) throws Exception
	{
		URI expanded = restTemplate.getUriTemplateHandler().expand(url);
		ResponseEntity<T> response = exchange(expanded, method, addheaders(authData), responseType);
		return verifyResponse(url, response);
	}
	
	public <T> T execute(URI uri, HttpMethod method, ParameterizedTypeReference<T> responseType, AuthenticationData authData) throws Exception
	{
		log("Request ", uri.toString());
		ResponseEntity<T> response = exchange(uri, method, addheaders(authData), responseType);
		return verifyResponse(uri.toString(), response);
	}

	public <T> T execute(String url, HttpMethod method, ParameterizedTypeReference<T> responseType) throws Exception
	{
		return execute(url, method, responseType, this.sessionData.getSessionAuthentication());
	}
	
	public <T> T execute(URI uri, HttpMethod method, ParameterizedTypeReference<T> responseType) throws Exception
	{
		return execute(uri, method, responseType, this.sessionData.getSessionAuthentication());
	}

	////////////
	
	public <T> T execute(String url, HttpMethod method, Class<T> responseType, AuthenticationData authData) throws Exception
	{
		URI expanded = restTemplate.getUriTemplateHandler().expand(url);
		ResponseEntity<T> response = exchange(expanded, method, addheaders(authData), responseType);
		return verifyResponse(url, response);
	}
	
	public <T> T execute(URI uri, HttpMethod method, Class<T> responseType, AuthenticationData authData) throws Exception
	{
		log("Request ", uri.toString());
		ResponseEntity<T> response = exchange(uri, method, addheaders(authData), responseType);
		return verifyResponse(uri.toString(), response);
	}

	public <T> T execute(String url, HttpMethod method, Class<T> responseType) throws Exception
	{
		AuthenticationData auth = this.sessionData.getSessionAuthentication();
		return execute(url, method, responseType, auth);
	}
	
	public <T> T execute(URI uri, HttpMethod method, Class<T> responseType) throws Exception
	{
		AuthenticationData auth = this.sessionData.getSessionAuthentication();
		return execute(uri, method, responseType, auth);
	}
	
	public <T, S> void execute(String url, HttpMethod method, T instance, Class<S>clazz, AuthenticationData authData) throws GuiValidationException, GuiGeneralException
	{
		addStandardSettings(instance);
		validate(instance);
		log("Request "+method.toString()+"::"+url.toString(), instance);
		URI expanded = restTemplate.getUriTemplateHandler().expand(url);
		HttpEntity<Object> headers = addheaders(instance, authData);
		exchange(expanded, method, headers, clazz);
	}
	
	public <T, S> void execute(URI url, HttpMethod method, T instance, Class<S>clazz, AuthenticationData authData) throws GuiValidationException, GuiGeneralException
	{
		addStandardSettings(instance);
		validate(instance);
		log("Request "+method.toString()+"::"+url.toString(), instance);
		HttpEntity<Object> headers = addheaders(instance, authData);
		exchange(url, method, headers, clazz);
	}

	public <T, S> void execute(String url, HttpMethod method, T instance, Class<S>clazz) throws GuiValidationException, GuiGeneralException
	{
		execute(url, method, instance, clazz, this.sessionData.getSessionAuthentication());
	}
	
	public <T> T execute(String url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor, Object... urlVariables) throws Exception
	{
		T response = restTemplate.execute(url, method, requestCallback, responseExtractor, urlVariables);
		return verifyResponse(url, response);
	}

	public <T> T postForObject(String url, Object request, Class<T> responseType, AuthenticationData authData, Object... uriVariables) throws Exception
	{
		this.addStandardSettings(request);
		validate(request);
		log("Request POST:: "+url.toString(), request);
		URI expanded = restTemplate.getUriTemplateHandler().expand(url, uriVariables);
		ResponseEntity<T> response = exchange(expanded, HttpMethod.POST, addheaders(request, authData), responseType);
		return verifyResponse(url, response);
	}
	
	public <T> T postForObject(String url, Object request, Class<T> responseType, Object... uriVariables) throws Exception
	{
		T response = postForObject(url, request, responseType, this.sessionData.getSessionAuthentication(), uriVariables);
		return response;
	}

	public <T> T putForObject(String url, Object request, Class<T> responseType, AuthenticationData authData, Object... uriVariables) throws Exception
	{
		this.addStandardSettings(request);
		validate(request);
		log("Request PUT:: "+url.toString(), request);
		URI expanded = restTemplate.getUriTemplateHandler().expand(url, uriVariables);
		ResponseEntity<T> response = exchange(expanded, HttpMethod.PUT, addheaders(request, authData), responseType);
		return verifyResponse(url, response);
	}
	
	public <T> T putForObject(String url, Object request, Class<T> responseType, Object... uriVariables) throws Exception
	{
		T response = putForObject(url, request, responseType, this.sessionData.getSessionAuthentication(), uriVariables);
		return response;
	}

	public void delete(String url)
	{
		restTemplate.delete(url);
	}
	
	public <T> void setValue(T instance, String name, Object value, boolean convert) throws Exception
	{
		if (value != null)
		{
			BeanWrapperImpl wrapper = new BeanWrapperImpl(instance);
			if (convert)
			{
				wrapper.setPropertyValue(name, wrapper.convertForProperty(value, name));
			}
			else
			{
				wrapper.setPropertyValue(name, value);
			}
			log("Set Value", name+" => "+value);
			/*if (isDevProfile())
			{
				log("Set Value", name+" => "+value);
			}*/
		}
	}
	
	private HttpStatus responseCodeIsError(String code)
	{
		logger.info("Return Code: " + code);
		
		HttpStatus result = HttpStatus.OK;
		
		switch (code)
		{
			case AuthenticationResponse.CODE_OK_AUTHENTICATED:
//			case AuthenticationResponse.CODE_OK_NOW_REQUIRE_RSA_NEW_PASSWORD:
			case AuthenticationResponse.CODE_OK_NOW_REQUIRE_RSA_PASSWORD:
			case AuthenticationResponse.CODE_OK_NOW_REQUIRE_UTF8_OTP:
			case AuthenticationResponse.CODE_OK_NOW_REQUIRE_UTF8_USERNAME:
			case AuthenticationResponse.CODE_OK_NOW_REQUIRE_UTF8_IMSI:
			case AuthenticationResponse.RETURN_CODE_SUCCESS:
			case AuthenticationResponse.CODE_OK_NOW_REQUIRE_RSA_PIN:
				result = HttpStatus.OK;
				break;
				
//			case AuthenticationResponse.CODE_FAIL_INVALID:
			case AuthenticationResponse.CODE_FAIL_CREDENTIALS_INVALID:
			case AuthenticationResponse.CODE_FAIL_OTP_INVALID:
			case AuthenticationResponse.CODE_FAIL_SESSION_INVALID:
			case AuthenticationResponse.CODE_FAIL_INVALID_COMPANY:	
			case AuthenticationResponse.CODE_FAIL_ACCOUNT_NOT_ACTIVE:
			case AuthenticationResponse.CODE_FAIL_PASSWORD_EXPIRED:
			case AuthenticationResponse.CODE_FAIL_PASSWORD_LOCKOUT:
			case AuthenticationResponse.CODE_FAIL_OTHER_ERROR:
			case AuthenticationResponse.CODE_FAIL_CHANNEL_NOT_ALLOWED:
			case AuthenticationResponse.CODE_FAIL_OTP_EXPIRED:
				result = HttpStatus.PRECONDITION_FAILED;
				break;
			case AuthenticationResponse.RETURN_CODE_UNKNOWN:
				result = HttpStatus.INTERNAL_SERVER_ERROR;
				break;
				
			case MissingApiConstants.CANNOT_BE_EMPTY: // Status.NOT_ACCEPTABLE
			case MissingApiConstants.RECURSIVE: // Status.NOT_ACCEPTABLE
			case MissingApiConstants.TOO_LONG: // Status.NOT_ACCEPTABLE
			case MissingApiConstants.INVALID_VALUE: // Status.NOT_ACCEPTABLE
			case MissingApiConstants.CANNOT_HAVE_VALUE: // Status.NOT_ACCEPTABLE
			case MissingApiConstants.CANT_BE_CHANGED: // Status.NOT_ACCEPTABLE
			case MissingApiConstants.NOT_SAME: // Status.NOT_ACCEPTABLE
			case MissingApiConstants.TOO_SMALL: // Status.NOT_ACCEPTABLE
			case MissingApiConstants.TOO_LARGE: // Status.NOT_ACCEPTABLE
			case MissingApiConstants.FAILED_TO_SAVE: // Status.NOT_ACCEPTABLE
			case MissingApiConstants.FAILED_TO_DELETE: // Status.NOT_ACCEPTABLE
			case MissingApiConstants.NOT_FOUND: // Status.NOT_ACCEPTABLE
			case MissingApiConstants.AMBIGUOUS: // Status.NOT_ACCEPTABLE
			case MissingApiConstants.CANNOT_ADD: // Status.NOT_ACCEPTABLE
			case MissingApiConstants.CANNOT_DELETE: // Status.NOT_ACCEPTABLE
			case MissingApiConstants.CANNOT_DELETE_SELF: // Status.NOT_ACCEPTABLE
			case MissingApiConstants.LIMIT_REACHED:	// Status.NOT_ACCEPTABLE
			//case MissingApiConstants.DAY_COUNT_LIMIT: // Status.NOT_ACCEPTABLE
			//case MissingApiConstants.DAY_AMOUNT_LIMIT: // Status.NOT_ACCEPTABLE
			//case MissingApiConstants.MONTH_COUNT_LIMIT: // Status.NOT_ACCEPTABLE
			//case MissingApiConstants.MONTH_AMOUNT_LIMIT: // Status.NOT_ACCEPTABLE
			//case MissingApiConstants.INSUFFICIENT_FUNDS: // Status.NOT_ACCEPTABLE
			//case MissingApiConstants.INVALID_PIN: // Status.NOT_ACCEPTABLE
				
			case TransactionsConfig.ERR_REFILL_FAILED: // Technical
			case TransactionsConfig.ERR_INVALID_CHANNEL:
			case TransactionsConfig.ERR_CO_AUTHORIZE:
			case TransactionsConfig.ERR_INSUFFICIENT_FUNDS:
			case TransactionsConfig.ERR_INSUFFICIENT_PROVISION:
			case TransactionsConfig.ERR_DAY_COUNT_LIMIT:
			case TransactionsConfig.ERR_DAY_AMOUNT_LIMIT:
			case TransactionsConfig.ERR_MONTH_COUNT_LIMIT:
			case TransactionsConfig.ERR_MONTH_AMOUNT_LIMIT:
			case TransactionsConfig.ERR_INVALID_PIN:
			case TransactionsConfig.ERR_PIN_LOCKOUT:
			case TransactionsConfig.ERR_CONFIRM_PIN_DIFF:
			case TransactionsConfig.ERR_NOT_SELF:
			case TransactionsConfig.ERR_TRANSACTION_NOT_FOUND:
			case TransactionsConfig.ERR_IMSI_LOCKOUT:
			case TransactionsConfig.ERR_INVALID_AGENT:
			case TransactionsConfig.ERR_INVALID_AMOUNT:
			case TransactionsConfig.ERR_NO_TRANSFER_RULE:
			case TransactionsConfig.ERR_INTRATIER_TRANSFER:
			case TransactionsConfig.ERR_INVALID_TRANSACTION_TYPE:
				result = HttpStatus.PRECONDITION_FAILED;
				break;
				
			case TransactionsConfig.ERR_FORBIDDEN:
			//case MissingApiConstants.FORBIDDEN: // Status.FORBIDDEN
			case TransactionsConfig.ERR_ALREADY_REGISTERED:
			case TransactionsConfig.ERR_NOT_REGISTERED:
			case TransactionsConfig.ERR_INVALID_STATE:			
			//case MissingApiConstants.ALREADY_REGISTERED: // Status.FORBIDDEN
			//case MissingApiConstants.NOT_REGISTERED: // Status.FORBIDDEN
			//case MissingApiConstants.INVALID_CHANNEL: // Status.FORBIDDEN
			//case MissingApiConstants.INVALID_STATE : // Status.FORBIDDEN
				result = HttpStatus.FORBIDDEN;
				break;
			
			case MissingApiConstants.UNAUTHORIZED: // Status.UNAUTHORIZED
				if (sessionService.isSessionValid())
				{
					result = HttpStatus.FORBIDDEN;
				}
				else
				{
					result = HttpStatus.UNAUTHORIZED;
				}
				break;
			case MissingApiConstants.TAMPERED: // Status.CONFLICT
				result = HttpStatus.CONFLICT;
				break;
			//case MissingApiConstants.TECHNICAL_PROBLEM: // Status.INTERNAL_SERVER_ERROR
			case TransactionsConfig.ERR_TECHNICAL_PROBLEM:
			
				result = HttpStatus.INTERNAL_SERVER_ERROR;
				break;
			
			case ERR_ACC_NOT_FOUND:
				result = HttpStatus.NOT_FOUND;
				break;
				
			default:
				result = HttpStatus.INTERNAL_SERVER_ERROR;
				break;
		}
		
		return result;
	}
	
	private boolean isErrorCode(HttpStatus status)
	{
		boolean result = false;
		if (status == null) return true;
		if (status.is4xxClientError() || status.is5xxServerError())
		{
			result = true;
		}
		return result;
	}
	
	private String getCorrelationId()
	{
		String correlationId = (String)request.getAttribute(ApplicationConstants.CONST_CORRELATIONID);
		if (correlationId == null)
		{
			try
			{
				correlationId = correlationIdService.getUniqueId();
				request.setAttribute(ApplicationConstants.CONST_CORRELATIONID, correlationId);
			}
			catch(Exception ex){}
		}
		return correlationId;
	}

	private <T> T verifyResponse(String url, T response) throws Exception
	{
		String correlationId = getCorrelationId();
		logBody(url, "Response ", response);
		//log("Response ", response);
		if (response instanceof TransactionResponse)
		{
			TransactionResponse header = (TransactionResponse)response;
			String returnCode = header.getReturnCode();
			HttpStatus responseCode = responseCodeIsError(returnCode);
			if (isErrorCode(responseCode))
			{
				String additional = header.getResponse();
				if (additional == null) additional = header.getAdditionalInformation();
				//GuiGeneralException ex = new GuiGeneralException(mapper.writeValueAsString(header));
				GuiGeneralException ex = new GuiGeneralException(returnCode);
				if (additional != null) ex.setAdditional(additional);
				ex.setCorrelationId(correlationId);
				ex.fillInStackTrace();
				ex.setErrorCode(responseCode);
				ex.setServerCode(returnCode);
				throw ex;
			}
		}
		else if (response instanceof AuthenticationResponse)
		{
			AuthenticationResponse result = (AuthenticationResponse)response;
			String returnCode = result.getReturnCode();
			HttpStatus responseCode = responseCodeIsError(returnCode);
			if (isErrorCode(responseCode))
			{
				// Remap AuthenticationResponse code
//				if (returnCode.equals(AuthenticationResponse.CODE_FAIL_CHANNEL_NOT_ALLOWED))
//					returnCode = AuthenticationResponse.CODE_FAIL_CREDENTIALS_INVALID;
				
//				ErrorInfo error = result.getLastErrorType();
				GuiGeneralException ex = new GuiGeneralException(returnCode);
//				ex.setAdditional(error.getDescription());
				ex.setErrorCode(responseCode);
				ex.setCorrelationId(correlationId);
				ex.setServerCode(returnCode);
				ex.fillInStackTrace();
				throw ex;
			}
		}
		else if (response instanceof BatchUploadResponse)
		{
			return response;
		}
		else if (response instanceof ResponseHeader)
		{
			ResponseHeader header = (ResponseHeader)response;
			String returnCode = header.getReturnCode();
			HttpStatus responseCode = responseCodeIsError(returnCode);
			if (isErrorCode(responseCode))
			{
				String additional = header.getAdditionalInformation();
				//GuiGeneralException ex = new GuiGeneralException(mapper.writeValueAsString(header));
				GuiGeneralException ex = new GuiGeneralException(returnCode);
				if (additional != null) 
				{
					ex.setAdditional(additional);
				}
				ex.setCorrelationId(correlationId);
				ex.fillInStackTrace();
				ex.setServerCode(returnCode);
				ex.setErrorCode(responseCode);
				throw ex;
			}
		}
		return response;
	}
	
	private <T> T verifyResponse(String url, ResponseEntity<T> response) throws Exception
	{
		HttpStatus statusCode = response.getStatusCode();
		if (isErrorCode(statusCode))
		{
			Map<String, Object> errorAttributes = new HashMap<String, Object>();
			response.getHeaders().forEach((key, value) ->{
				errorAttributes.put(key, value);
			});
			GuiGeneralException ex = new GuiGeneralException(mapper.writeValueAsString(response));
			ex.setHeaders(errorAttributes);
			ex.setCorrelationId(getCorrelationId());
			ex.fillInStackTrace();
			ex.setServerCode(response.getStatusCode().name());
			ex.setErrorCode(statusCode);
			throw ex;
		}
		return verifyResponse(url, response.getBody());
	}
	
	private HttpEntity<Object> addheaders(Object request, AuthenticationData authData)
	{
		return new HttpEntity<Object>(request, setHeaders(authData));
	}
	
	private HttpEntity<Object> addheaders(AuthenticationData authData)
	{
		return new HttpEntity<Object>(setHeaders(authData));
	}
	
	private HttpHeaders setHeaders(AuthenticationData authData)
	{
		HttpHeaders headers = new HttpHeaders();
		headers.add(RestRequestUtil.SID, authData.getServerSessionID());
		headers.add("Connection", "keep-alive");
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		log("Request Headers ", headers.toString());
		return headers;
	}
	
	private boolean isDevProfile()
	{
		return true; //environment.acceptsProfiles(Common.CONST_DEVELOPMENT_PROFILE);
	}
	
	private <T> void addStandardSettings(T instance) throws GuiGeneralException
	{
		if (!(instance instanceof AuthenticationRequest) && !sessionService.isSessionValid())
		{
			GuiGeneralException ex = new GuiGeneralException("Transaction server session id expired");
			ex.setErrorCode(HttpStatus.UNAUTHORIZED);
			ex.fillInStackTrace();
			throw ex;
		}
		
		/*
		 * NB Exceptions are purposely ignored here as missing attributes are expected.
		 */
		try {
			setValue(instance, "companyID", ctxt.getCompanyID(), true);
		} catch (Exception e1) {
			//logger.error("", e1);
		}
		
		try {
			setValue(instance, "hostName", sessionData.getHostName(), false);
		} catch (Exception e1) {
			//logger.error("", e1);
		}
		
		try {
			setValue(instance, "ipAddress", sessionData.getIpAddress(), false);
		} catch (Exception e1) {
			//logger.error("", e1);
		}
		
		try {
			if (instance instanceof AuthenticationRequest && ((AuthenticationRequest)instance).getChannel() == null) {
				setValue(instance, "channel", AuthenticationRequest.CHANNEL_3PP, true);
			}
		} catch (Exception e1) {
			//logger.error("", e1);
		}
		
		try {
			setValue(instance, "macAddress", sessionData.getMacAddress(), true);
		} catch (Exception e1) {
			//logger.error("", e1);
		}
	}
	
	private void log(String msg, Object obj)
	{
		String id = getCorrelationId();
		
		try
		{
			if (isDevProfile())
			{
				logger.info(id+" => "+msg+" "+mapper.writeValueAsString(obj));
			}
			else
			{
				logger.info(id+" => "+msg+" "+mapper.writeValueAsString(obj));
			}
		}
		catch (JsonProcessingException e)
		{
			logger.error("", e);
		}
	}
	
	private <T> void logBody(String url, String msg, ResponseEntity<T>response)
	{
		//logger.error(url);
		if (!url.contains("/csv") && !url.contains("/upload") && !url.contains("/json"))
		{
			log(msg, response.getBody());
		}
	}
	
	private <T> void logBody(String url, String msg, T response)
	{
		//logger.error(url);
		if (!url.contains("/csv") && !url.contains("/upload") && !url.contains("/json"))
		{
			log(msg, response);
		}
	}
	
	private <T> ResponseEntity<T> exchange(URI expanded, HttpMethod method, HttpEntity<Object> headers, Class<T> responseType)
	{
		log("Request", method.toString()+"::"+expanded.toString());
		log("Request", headers);
		ResponseEntity<T>response = restTemplate.exchange(expanded, method, headers, responseType);
		
		logBody(expanded.getPath(), "Response", response);
		log("Response", response.getHeaders());
		return response;
	}
	
	private <T> ResponseEntity<T> exchange(URI expanded, HttpMethod method, HttpEntity<Object> headers, ParameterizedTypeReference<T> responseType)
	{
		log("Request", method.toString()+"::"+expanded.toString());
		log("Request", headers);
		ResponseEntity<T>response = restTemplate.exchange(expanded, method, headers, responseType);
		logBody(expanded.getPath(), "Response", response);
		log("Response", response.getHeaders());
		return response;
	}
	
	private <T> void validate(T instance) throws GuiValidationException
	{
		log("Validating Request", instance);
		if (instance == null)
		{
			GuiValidationException ex = new GuiValidationException("instance cannot be null");
			ex.setCorrelationId(getCorrelationId());
			throw ex;
		}
		if (instance instanceof IValidatable)
		{
			IValidatable validatable = (IValidatable)instance;
			try
			{
				List<Violation> violations = validatable.validate();
				try
				{
					log("Validation ", mapper.writeValueAsString(violations));
				}
				catch (JsonProcessingException e)
				{
					logger.error("Json processing exception during response translation", e);
				}
				if (violations != null && violations.size() > 0)
				{
					GuiValidationException ex = new GuiValidationException(violations);
					ex.setCorrelationId(getCorrelationId());
					ex.fillInStackTrace();
					throw ex;
				}
			}
			catch(GuiValidationException ex)
			{
				throw ex;
			}
			catch(Throwable th)
			{
				logger.error("", th);
				try
				{
					GuiValidationException ex = new GuiValidationException("Exception while validating request instance "+ mapper.writeValueAsString(instance), th);
					ex.setCorrelationId(getCorrelationId());
					ex.fillInStackTrace();
					throw ex;
				}
				catch (JsonProcessingException e)
				{
					logger.error("Json processing exception while performing error translation", e);
				}
			}
		}
	}
}
