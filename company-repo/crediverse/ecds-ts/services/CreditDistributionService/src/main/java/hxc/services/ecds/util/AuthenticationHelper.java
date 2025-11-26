package hxc.services.ecds.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.persistence.EntityManager;

import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.IAuthenticatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationHelper
{
	final static Logger logger = LoggerFactory.getLogger(AuthenticationHelper.class);
    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Constants
    //
    // /////////////////////////////////
    private static final String CONCURRENT = "Concurrent Systems";

    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Fields
    //
    // /////////////////////////////////
    private static final SecureRandom random = new SecureRandom();
    public static final int PIN_LENGTH = 5;

    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Methods
    //
    // /////////////////////////////////
    public static byte[] validateNewPin(IAuthenticatable party,
            EntityManager em, CompanyInfo company, String newPin)
            throws RuleCheckException
    {
        return null;
    }

    public static String offerPIN(IAuthenticatable party, EntityManager em,
            Session session, CompanyInfo companyInfo, String pin)
            throws RuleCheckException
    {

        return null;
    }

    public static void updatePin(IAuthenticatable party, EntityManager em,
            byte[] key, Session session) throws RuleCheckException
    {
        party.setKey4(party.getKey3());
        party.setKey3(party.getKey2());
        party.setKey2(party.getKey1());

        party.setKey1(key);
        party.setPinVersion(party.getPinVersion() + 1);
        party.setTemporaryPin(false);
        party.setConsecutiveAuthFailures(0);
    }

    public static byte[] encryptPin(String pin)
    {
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            String code = CONCURRENT + pin;
            crypt.update(code.getBytes("UTF-8"));
            return crypt.digest();
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
        {
            // UTF-8 will always exist
			logger.error("", e);
            return null;
        }
    }

    public static boolean testIfSamePin(IAuthenticatable party, String pin)
    {
        byte[] key1 = party.getKey1();

        if (key1 == null || key1.length == 0 || pin == null || pin.isEmpty())
            return false;

        byte[] key = encryptPin(pin);
        if (key.length != key1.length)
            return false;

        for (int index = 0; index < key.length; index++)
        {
            if (key[index] != key1[index])
                return false;
        }

        return true;
    }

    public static String createRandomPin(IAuthenticatable party)
    {
        int newPin = random.nextInt(90000) + 10000;
        String newPIN = String.format("%05d", newPin);
        party.setKey1(AuthenticationHelper.encryptPin(newPIN));
        party.setPinVersion(party.getPinVersion());
        party.setTemporaryPin(true);
        return newPIN;
    }

    public static void setPin(IAuthenticatable party, String pin) {
        party.setKey1(AuthenticationHelper.encryptPin(pin));
        party.setPinVersion(party.getPinVersion());
        party.setTemporaryPin(false);
    }
    
    public static String createRandomPassword(IAuthenticatable party)
    {
    	RandomString randomString = new RandomString(8, new SecureRandom());
    	String password = randomString.nextString();
		byte[] encryptedPassword = AuthenticationHelper.encryptPin(password);
		party.setKey1(encryptedPassword);
		party.setTemporaryPin(false);
		party.setPinVersion(party.getPinVersion() + 1);
		return password;
    }

    public static String createDefaultPin(IAuthenticatable party,
            String defaultPin)
    {
        party.setKey1(AuthenticationHelper.encryptPin(defaultPin));
        party.setTemporaryPin(false);
        party.setPinVersion(party.getPinVersion() + 1);
        return defaultPin;
    }

    public static boolean testIfSameKey(byte[] newPin, byte[] existingPin)
    {
        if (newPin == null || newPin.length == 0)
            return true;

        if (existingPin == null || existingPin.length == 0)
            return false;

        if (newPin.length != existingPin.length)
            return false;

        for (int index = 0; index < newPin.length; index++)
        {
            if (newPin[index] != existingPin[index])
                return false;
        }

        return true;
    }

}
