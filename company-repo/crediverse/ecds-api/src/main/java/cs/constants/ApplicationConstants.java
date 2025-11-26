package cs.constants;

public class ApplicationConstants
{
	public static final String CONST_CORRELATIONID = "correlationId";
	public static final String CONST_INCOMINGIP = "incomingIp";
	public static final String CONST_REQUESTIP = "requestIp";
	public static final String CONST_CONTEXT_PATH = "contextPath";
	public static final String CONST_BRANDING_BASE = "brandingBase";
	public static final String CONST_USER_LANGUAGE = "userLanguage";
	public static final String CONST_APP_VERSION = "appVersion";

	// Javascript references used into GlobalControllerAdvice (links to requirejs configurations)
	public static final String CONST_PROJECT_VERSION = "projectVersion";
	public static final String CONST_PROJECT_LOGIN_VERSION = "projectLoginVersion";
	
	public static final String CONST_PORTAL_DEV_LOGIN_JS_LOCATION = "js/login/config/portal";
	public static final String CONST_PORTAL_DEV_APP_JS_LOCATION = "/js/portal/config/init";
	public static final String CONST_PORTAL_LOGIN_JS_PREFIX = "/js/ptlogin-";
	public static final String CONST_PORTAL_APP_JS_PREFIX = "/js/portal-";
	
	public static final String CONST_MOBILE_DEV_LOGIN_JS_LOCATION = "js/login/config/mobile";
	
	public static final String CONST_CONTENT_TYPE_CSV  = "text/csv; charset=utf-8";
	public static final String CONST_CONTENT_TYPE_JSON  = "application/json; charset=utf-8";
	
	public static final String CONST_LANGUAGE_ENGLISH = "en";
	public static final String CONST_LANGUAGE_FRENCH = "fr";
	
	public static final String CONST_DEFAULT_LOGO_FILENAME = "img/crediverse-logo.png";
	
	public static final int CONST_MAX_RETRY = 3;
	
	/*
	 * This is the number of batch files that can be processed concurrently against the transaction server
	 */
	public static final int CONST_MAX_CONCURRENT_BATCH = 10;
	public static final int CONST_BATCH_BUFFER_SIZE = 1024 * 1024;
	public static final int CONST_BATCH_MAX_RETRY = CONST_MAX_RETRY;
}
