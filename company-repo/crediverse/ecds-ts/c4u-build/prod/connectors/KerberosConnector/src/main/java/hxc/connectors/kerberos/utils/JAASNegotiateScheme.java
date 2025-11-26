package hxc.connectors.kerberos.utils;

import static hxc.connectors.kerberos.Authenticator.Error.LOGIN_KRB_ERROR;
import static hxc.connectors.kerberos.Authenticator.Error.UNKNOWN_CODE;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.kerberos.Authenticator;


/*
 * Reference:  http://docs.oracle.com/javase/7/docs/technotes/guides/security/jgss/tutorials/LoginConfigFile.html
 */
public class JAASNegotiateScheme
{
	final static Logger logger = LoggerFactory.getLogger(JAASNegotiateScheme.class);
    /** Log object for this class. */

    public synchronized boolean authenticate(String username, String password,
            Authenticator.Error err)
    {
        logger.info("Initialize Kerberos connection");

        // Create a callback handler
        Configuration.setConfiguration(null);
        CallbackHandler callbackHandler = new JAASNegotiateCallbackHandler(
                username, password);

        // Reset structures
        LoginContext con = null;
        boolean loginSuccess = false;

        try
        {
            /*
             * More details about the properties on:
             * https://docs.oracle.com/javase/7/docs/jre/api/security/jaas/spec/
             * com/sun/security/auth/module/Krb5LoginModule.html
             */

            AppConfigurationEntry[] defaultConfiguration = new AppConfigurationEntry[1];
            Map<String, String> options = new HashMap<String, String>();
            options.put("principal", username);
            options.put("client", "true");

            // sun.security.auth.module uses System.out
            String debugTraces = ( logger.isTraceEnabled() ? "true" : "false" );
            options.put("debug", debugTraces);

            // Must NOT use Ticket Cache
            options.put("useTicketCache", "false");
            options.put("storeKey", "false");
            options.put("refreshKrb5Config", "true");

            defaultConfiguration[0] = new AppConfigurationEntry(
                    "com.sun.security.auth.module.Krb5LoginModule",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                    options);

            JAASConfiguration cc = new JAASConfiguration(defaultConfiguration);
            logger.info("Prepare login context object");

            // Create a LoginContext with a callback handler
            con = new LoginContext("com.sun.security.jgss.login", null,
                    callbackHandler, cc);

            Configuration.setConfiguration(cc);

            logger.info("Perform authentication procedure");

            // Perform authentication
            con.login();

            loginSuccess = true;

        } catch (LoginException e)
        { 
            /*
             *  This internal API is not accessible from Java. All the info I can find on this
             *  suggests that the standard login exception should be used.
             *  See https://stackoverflow.com/questions/55844373/java-11-kerberos-using-gss-api
             *  
             *  We should probably look at moving this to GSS API if this doesn't work.
             */
            /*
            Object cause = e.getCause();
            if ( cause instanceof KrbException )
            {
                KrbException krbExc = (KrbException)cause;
                err.Set(LOGIN_KRB_ERROR, krbExc.getError().getErrorCode(), e.getMessage());
            }
            else*/
            err.Set(LOGIN_KRB_ERROR, UNKNOWN_CODE, e.getMessage());

            logger.error("Kerberos error", e);
        }

        if ( loginSuccess )
        {
            // Perform action as authenticated user
            // Subject subject = con.getSubject();
            // LOG.info("Subject is :"+ subject.toString());
            // String principalName = subject.getPrincipals();
            logger.info("PRINCIPAL AUTHENTICATED");

            return true;
        }

        return false;

    }
}
