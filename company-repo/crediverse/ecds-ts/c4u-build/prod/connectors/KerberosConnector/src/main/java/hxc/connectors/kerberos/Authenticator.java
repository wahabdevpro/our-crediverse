package hxc.connectors.kerberos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Authenticator 
{
	final static Logger logger = LoggerFactory.getLogger(Authenticator.class);
	
	protected KerberosConnector.KerberosConfiguration config;
	
	public abstract boolean tryAuthenticate( String username, String password, Error err );
	public abstract boolean tryChangePassword(  String username, String oldPassword, String newPassword, Error err );
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Authenticator Error 
	//
	// /////////////////////////////////

	static public class Error
	{
		public static final int LOGIN_KRB_ERROR = 0;
		public static final int APACHE_KRB_ERROR = 1;
		public static final int APACHE_PASSWD_ERROR = 2;
		public static final int APACHE_PASSWD_EXCEPTION = 3;
		public static final int JAAS_KRB_ERROR = 4;
		public static final int UNKNOWN_CODE = -1;
		
		private int type;
		private int code;
		private String description;
		
		public void Set( int errorType, int errorCode, String errorDescription )
		{
			type = errorType;
			code = errorCode;
			description = errorDescription;
		}
		
		public String getDescription()
		{
			return description;
		}
		
		public int getType()
		{
		    return type;
		}
		
		public int getCode()
		{
		    return code;
		}
	};
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Setter
	//
	// /////////////////////////////////
	
	public void setConfig(KerberosConnector.KerberosConfiguration c)
	{
		config = c;
	}
	

}
