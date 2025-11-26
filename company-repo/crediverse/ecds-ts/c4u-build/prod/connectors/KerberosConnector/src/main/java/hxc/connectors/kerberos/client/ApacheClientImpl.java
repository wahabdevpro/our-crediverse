package hxc.connectors.kerberos.client;

import static hxc.connectors.kerberos.Authenticator.Error.APACHE_KRB_ERROR;

import java.util.HashSet;
import java.util.Set;

import org.apache.directory.kerberos.client.ChangePasswordResult;
import org.apache.directory.kerberos.client.ChangePasswordResultCode;
import org.apache.directory.kerberos.client.KdcConfig;
import org.apache.directory.kerberos.client.KdcConnection;
import org.apache.directory.kerberos.client.TgTicket;
import org.apache.directory.kerberos.client.TgtRequest;
import org.apache.directory.server.kerberos.changepwd.exceptions.ChangePasswordException;
import org.apache.directory.shared.kerberos.codec.types.EncryptionType;
import org.apache.directory.shared.kerberos.exceptions.KerberosException;
import org.apache.directory.shared.kerberos.messages.KrbError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.kerberos.Authenticator;

public class ApacheClientImpl extends Authenticator
{
	final static Logger logger = LoggerFactory.getLogger(ApacheClientImpl.class);

    private KdcConnection initConnection()
    {
        KdcConfig kdcConfig = KdcConfig.getDefaultConfig();
        
        /*  https://technet.microsoft.com/en-us/library/dd560670(v=ws.10).aspx
         *       
         *  Both DES cipher suites (DES-CBC-MD5 & DES-CBC-CRC) are disabled by default in Windows 7.
         *  The following cipher suites are enabled by default in Windows 7 and Windows Server 2008 R2:
         *  AES256-CTS-HMAC-SHA1-96
         *  AES128-CTS-HMAC-SHA1-96
         *  RC4-HMAC
         */
        
        Set<EncryptionType> encryptionTypes = new HashSet<EncryptionType>();
        encryptionTypes.add(EncryptionType.AES256_CTS_HMAC_SHA1_96);
        encryptionTypes.add(EncryptionType.AES128_CTS_HMAC_SHA1_96);
        encryptionTypes.add(EncryptionType.RC4_HMAC);
         
        kdcConfig.setEncryptionTypes(encryptionTypes);

        // On Windows Server 2008 AD if UDP is used, an error like below is observed
        // KRB_ERR_RESPONSE_TOO_BIG: Response too big for UDP, retry with TCP
        // Use TCP as default.
        kdcConfig.setUseUdp(config.getUseUDP());
        
        // Note default 88 port is used 
        kdcConfig.setKdcPort(config.getPort());
        
        kdcConfig.setHostName(config.getHostName());
        
        kdcConfig.setTimeout(config.getTimeout()*1000);

        return new KdcConnection(kdcConfig);
    }

    @Override
    public boolean tryAuthenticate(String username, String password, Error err)
    {
        try
        {
            logger.info("Starting Kerberos authentication procedure: ");
            
            logger.trace("Building ticket-granting ticket request");
            
            // Build principal name in form of admin@EXAMPLE.COM
            username = buildPrincipal(username);

            // Initialize a TGT ticket request
            TgtRequest clientTgtReq = new TgtRequest();
            clientTgtReq.setClientPrincipal(username);
            clientTgtReq.setPassword(password);
            
            // Build KDC connection and send auth request
            KdcConnection conn = initConnection();
            
            logger.trace("Sending ticket-granting ticket request: " + conn.toString());
            TgTicket ticket = conn.getTgt(clientTgtReq);

            logger.trace("Ticket-granting ticket received. Authentication successfully completed.");
            logger.trace(ticket.toString());

            return true;

        } catch (KerberosException ex)
        { 
            HandleException(ex, err);
        }

        return false;
    }

    @Override
    public boolean tryChangePassword(String username, String oldPassword,
            String newPassword, Error err)
    {
        try
        {
            // Build principal name in form of admin@EXAMPLE.COM
            username = buildPrincipal(username);

            // Build KDC connection and send changePassword request
            KdcConnection conn = initConnection();
            ChangePasswordResult changeResult = conn.changePassword(username, oldPassword, newPassword);

            // In most case throws exception but if passes check result code to ensure its correctness
            if (changeResult.getCode().compareTo(ChangePasswordResultCode.KRB5_KPASSWD_SUCCESS) == 0)
            {
                logger.info("Kerberos password updated successfully");
                return true;
            }

            logger.error("ChangePassword error: ", changeResult.getCode().toString());

            err.Set(APACHE_KRB_ERROR, changeResult.getCode().getVal(), changeResult.getCode().toString());
            
        } 
        catch (ChangePasswordException ex)
        {
            HandleException(ex, err); 
        }

        return false;
    }
    
    private String buildPrincipal(String username)
    {
        String realm = config.getRealm();
        username += "@" + realm;
        return username;
    }
    
    private void HandleException(KerberosException ex, Error err)
    {
        KrbError krbErr = ex.getError(); 
        if (krbErr != null)
        {   
            err.Set(APACHE_KRB_ERROR, krbErr.getErrorCode().getValue(), krbErr.getErrorCode().getMessage());
            logger.error(ex.getError().toString());
            
        } else
        {
            err.Set(APACHE_KRB_ERROR,  ex.getErrorCode(), ex.getMessage());
            logger.error(ex.getMessage());
        }
    }
}
