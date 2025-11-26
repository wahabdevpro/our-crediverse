package hxc.connectors.kerberos.utils;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/* Java imports */

/**
 * <p>
 * Prompts and reads from the command line for answers to authentication
 * questions. This can be used by a JAAS application to instantiate a
 * CallbackHandler
 * 
 * @see javax.security.auth.callback
 */

public class JAASNegotiateCallbackHandler implements CallbackHandler
{

    private String username;
    private String passswordstr;

    /**
     * <p>
     * Creates a callback handler that prompts and reads from the command line
     * for answers to authentication questions. This can be used by JAAS
     * applications to instantiate a CallbackHandler.
     * 
     */
    public JAASNegotiateCallbackHandler()
    {
    }

    public JAASNegotiateCallbackHandler(String user, String password)
    {
        username = user;
        passswordstr = password;
    }

    /**
     * Handles the specified set of callbacks.
     *
     * @param callbacks
     *            the callbacks to handle
     * @throws java.io.IOException
     *             if an input or output error occurs.
     * @throws javax.security.auth.callback.UnsupportedCallbackException
     *             if the callback is not an instance of NameCallback or
     *             PasswordCallback
     */
    public void handle(Callback[] callbacks)
            throws IOException, UnsupportedCallbackException
    {
        for (int i = 0; i < callbacks.length; i++)
        {
            if (callbacks[i] instanceof NameCallback)
            {
                NameCallback nc = (NameCallback) callbacks[i];

                if (username.equals(""))
                {
                    username = nc.getDefaultName();
                }

                nc.setName(username);

            } else if (callbacks[i] instanceof PasswordCallback)
            {
                PasswordCallback pc = (PasswordCallback) callbacks[i];

                char[] pass = passswordstr.toCharArray();
                pc.setPassword(pass);

            } else if (callbacks[i] instanceof ConfirmationCallback)
            {
               // confirmation = (ConfirmationCallback) callbacks[i];

            } else
            {
                throw new UnsupportedCallbackException(callbacks[i],
                        "Unrecognized Callback");
            }
        }
    }

}
