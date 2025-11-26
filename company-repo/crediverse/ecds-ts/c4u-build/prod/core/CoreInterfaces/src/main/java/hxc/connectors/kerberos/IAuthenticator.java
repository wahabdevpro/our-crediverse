package hxc.connectors.kerberos;

public interface IAuthenticator
{

	public static class Result
	{
		// Possible result codes and messages
		// Reference: http://directory.apache.org/apacheds/gen-docs/2.0.0-M23/apidocs/org/apache/directory/shared/kerberos/exceptions/ErrorType.html
		// https://github.com/apache/directory-server/blob/trunk/kerberos-codec/src/main/java/org/apache/directory/shared/kerberos/exceptions/ErrorType.java
		// https://tools.ietf.org/html/rfc4120#section-7.5.9

		public static final int SUCCESS = 0;
		public static final int UNKNOWN_FAILURE = -1;
		// Seems to be used when password is invalid ... but may be other cases also
		public static final int KRB_AP_ERR_BAD_INTEGRITY = 31;
		// Password expired
		public static final int KDC_ERR_KEY_EXPIRED = 23;
		// User not found
		public static final int KDC_ERR_C_PRINCIPAL_UNKNOWN = 6;
		// Account disabled
		public static final int KDC_ERR_CLIENT_REVOKED = 18;

		public int code = Result.UNKNOWN_FAILURE;
		public String description = "Unknown Error";
	}

	public Result authenticate(String username, String password);
	public int changePassword(String username, String oldPassword, String newPassword);
}


