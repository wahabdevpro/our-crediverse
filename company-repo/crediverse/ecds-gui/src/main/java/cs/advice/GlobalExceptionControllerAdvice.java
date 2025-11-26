package cs.advice;

import cs.constants.ApplicationConstants;
import cs.dto.error.GuiErrorResponse;
import cs.dto.error.GuiGeneralException;
import cs.dto.error.GuiValidationException;
import cs.dto.error.GuiViolation;
import cs.service.CorrelationIdService;
import cs.service.GuiAuthenticationService;
import cs.service.TypeConvertorService;
import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;

import static cs.utility.StringUtil.isNullOrBlank;

@ControllerAdvice
public class GlobalExceptionControllerAdvice extends ResponseEntityExceptionHandler
{
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionControllerAdvice.class);

	@Autowired
	private GuiAuthenticationService authenticationService;

	@Autowired
	private TypeConvertorService typeConvertorService;

	@Autowired
	private CorrelationIdService correlationIdService;

	@ExceptionHandler(SocketTimeoutException.class)
	public ResponseEntity<Object> handleCustomSocketException(SocketTimeoutException ex)
	{
		String correlationId = correlationIdService.getUniqueId();
		String cause = ex.getMessage();
		String[] messages = ((cause == null)?"Socket timeout":cause).split("\n");

		GuiErrorResponse errorResponse = new GuiErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, messages[0]);
		errorResponse.setCorrelationId(correlationId);

		logger.error("Correlation ID: " + correlationId, ex);
		for (String message : messages)
		{
			errorResponse.addViolation(GuiViolation.generalViolation(correlationId, message));
		}
		expandThrowable(ex, null, "", correlationId+" => ", new HashSet<Throwable>());

		return new ResponseEntity<Object>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
	}

	@Override
	protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex,
			HttpHeaders headers, HttpStatus status, WebRequest webRequest) {
		String correlationId = correlationIdService.getUniqueId();
		String cause = ex.getMessage();
		String[] messages = ((cause == null)?"Asynchronous request timeout":cause).split("\n");

		GuiErrorResponse errorResponse = new GuiErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, messages[0]);
		errorResponse.setCorrelationId(correlationId);
		logger.error("Correlation ID: " + correlationId, ex);

		for (String message : messages)
		{
			errorResponse.addViolation(GuiViolation.generalViolation(correlationId, message));
		}
		expandThrowable(ex, null, "", correlationId+" => ", new HashSet<Throwable>());

		return new ResponseEntity<Object>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
	}

	private void expandThrowable(Throwable current, StackTraceElement[] enclosingTrace, String caption, String prefix, Set<Throwable> dupeCheck) {
		logger.error("Correlation ID: " + prefix, current);
	}

	private ResponseEntity<Object> generateExceptionResponse(HttpStatus status, WebRequest request, Throwable ex)
	{
		String correlationId = (String) request.getAttribute(ApplicationConstants.CONST_CORRELATIONID, WebRequest.SCOPE_REQUEST);
		if (correlationId == null)
		{
			correlationId = correlationIdService.getUniqueId();
		}
		String cause = ex.getMessage();
		String[] messages = ((cause == null)?"Unable to decode request":cause).split("\n");

		GuiErrorResponse errorResponse = new GuiErrorResponse(status, messages[0]);
		errorResponse.setCorrelationId(correlationId);
		logger.error("Correlation ID: " + correlationId, ex);

		for (String message : messages) {
			errorResponse.addViolation(GuiViolation.generalViolation(correlationId, message));
		}

		return new ResponseEntity<>(errorResponse, status);
	}



	@ExceptionHandler(GuiValidationException.class)
	public ResponseEntity<Object> handleCustomException(GuiValidationException ex)
	{
		logger.error("Correlation ID: " + ex.getCorrelationId(), ex);
		GuiErrorResponse error = new GuiErrorResponse(
				HttpStatus.BAD_REQUEST,
				ex.getMessage() != null?ex.getMessage():"validation error",
				typeConvertorService.convertToGuiViolation(ex.getViolations(), ex.getCorrelationId()));
		error.setCorrelationId(ex.getCorrelationId());
		return  new ResponseEntity<Object>(error, HttpStatus.BAD_REQUEST);
	}

	/*
	 * GuiGeneralException(
	 * 		correlationId=057e8c68518,
	 * 		errorCode=403,
	 * 		additional=Transactions.Replenish Permission Check Failed,
	 * 		errors=null,
	 * 		headers=null)
	 */
	@ExceptionHandler(GuiGeneralException.class)
	public ResponseEntity<Object> handleGuiGeneralException(GuiGeneralException ex)
	{
		String correlationId = null;
		try
		{
			correlationId = ex.getCorrelationId();
		}
		catch(Exception e)
		{

		}

		if (correlationId == null) {
			correlationId = correlationIdService.getUniqueId();
		}
		logger.error("Correlation ID: " + correlationId, ex);

		String cause = ex.getMessage();
		String[] messages = ((cause == null)?"Unable to decode request":cause).split("\n");
		GuiErrorResponse errorResponse = new GuiErrorResponse(ex.getErrorCode(), messages[0]);
		for (String message : messages)
		{
			errorResponse.addViolation(GuiViolation.generalViolation(correlationId, message));
		}
		errorResponse.addViolation(GuiViolation.generalViolation(correlationId, ex.getAdditional()));

		errorResponse.setCorrelationId(correlationId);
		return  new ResponseEntity<Object>(errorResponse, ex.getErrorCode());
	}



	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request)
	{
		// TODO Auto-generated method stub
		ResponseEntity<Object> errorResponse = generateExceptionResponse(HttpStatus.BAD_REQUEST, request, ex);
		return errorResponse;//super.handleExceptionInternal(ex, body, headers, status, request);
	}

	@Override
	//@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, org.springframework.http.HttpHeaders headers, HttpStatus status, WebRequest request) {
		ResponseEntity<Object> errorResponse = generateExceptionResponse(HttpStatus.BAD_REQUEST, request, ex);

//		GuiErrorResponse error = new GuiErrorResponse(HttpStatus.BAD_REQUEST, message, new Violation(message, "", null, null));
		return errorResponse;
	}

	@Override
	//@ExceptionHandler(NoHandlerFoundException.class)
	protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, org.springframework.http.HttpHeaders headers, HttpStatus status, WebRequest request) {
		ResponseEntity<Object> errorResponse = generateExceptionResponse(HttpStatus.BAD_REQUEST, request, ex);
		return errorResponse;
	}

	@Override
   // @ExceptionHandler(TypeMismatchException.class)
	protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, org.springframework.http.HttpHeaders headers, HttpStatus status, WebRequest request) {
		ResponseEntity<Object> errorResponse = generateExceptionResponse(HttpStatus.BAD_REQUEST, request, ex);
		return errorResponse;
	}

	@Override
	//@ExceptionHandler(HttpMessageNotReadableException.class)
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, org.springframework.http.HttpHeaders headers, HttpStatus status, WebRequest request)
	{
		ResponseEntity<Object> errorResponse = generateExceptionResponse(HttpStatus.BAD_REQUEST, request, ex);
		return errorResponse;
	}

	@ExceptionHandler({HttpClientErrorException.class})
	public ResponseEntity<Object> clientException(HttpClientErrorException ex, WebRequest request)
	{
		if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED)
		{
			authenticationService.logout();
		}
		if (!isNullOrBlank(ex.getResponseBodyAsString())) {
			HttpClientErrorException ex2 = new HttpClientErrorException(ex.getStatusCode(), ex.getResponseBodyAsString());
			return generateExceptionResponse(ex.getStatusCode(), request, ex2);
		}
		if (ex.getMessage().endsWith(": [no body]")) {
			/**
			 * Due to Spring RestTemplate being updated
			 * Whenever we don't have a BODY in the response ... It appends an empty http body to the message in this way `: [no body]`
			 * Because we explicitly use the response, and don't expect the 'appended' text - we must remove everything after the colon (inclusive) for requests with an empty body
			 * This allows us to revert to the old way of doing handling the exceptions prior to the spring update
			 * 
			 * FIXME -- We only need this fix to begin with because... we are doing things in a non standard way - and not using proper JSON request/responses
			 * 			The correction is to fix our request/responses to actually use proper JSON in the response body, and thus there would be no need
			 * 			of "hacking" around it by messing with the response message.
			 */

			String prefixStatusCode = String.valueOf(ex.getStatusCode().value()).concat(" ");
			String[] messageParts = ex.getMessage().split(": \\[no body\\]"); // < regex, requires escaping the brachets
			String reconditionedErrorMessage = messageParts[0].replace(prefixStatusCode, "");
			ex = new HttpClientErrorException(ex.getStatusCode(), reconditionedErrorMessage);
		}
		return generateExceptionResponse(ex.getStatusCode(), request, ex);
	}

	@ExceptionHandler({HttpServerErrorException.class})
	public ResponseEntity<Object> serverException(HttpServerErrorException ex, WebRequest request)
	{
		return connectionException(ex, request);
	}

	@ExceptionHandler({HttpHostConnectException.class, ResourceAccessException.class})
	public ResponseEntity<Object> connectionException(Exception ex, WebRequest request)
	{
		Throwable cause = ex.getCause();
		if (cause != null)
		{
			if (cause instanceof HttpHostConnectException)
			{
				logger.error("Unable to contact transaction server", cause);
			}
			else
			{
				logger.error("Unknown exception "+cause.getClass(), cause);
			}
		}
		else
		{
			logger.error("Cause was null", ex);
		}
		return generateExceptionResponse(HttpStatus.SERVICE_UNAVAILABLE, request, ex);
	}

	@ExceptionHandler(Throwable.class)
	public ResponseEntity<Object> handleAllException(Throwable ex, WebRequest request)
	{
		ResponseEntity<Object> errorResponse = generateExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, ex);
		return errorResponse;
	}
}
