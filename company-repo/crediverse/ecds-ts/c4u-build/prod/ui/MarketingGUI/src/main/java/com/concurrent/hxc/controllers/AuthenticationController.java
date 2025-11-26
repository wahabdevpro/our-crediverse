package com.concurrent.hxc.controllers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.security.Key;
import java.util.Date;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.concurrent.hxc.dao.Organisation;
import com.concurrent.hxc.dao.OrganisationDAO;
import com.concurrent.hxc.objects.User;
import com.concurrent.hxc.utils.MailHandler;
import com.concurrent.hxc.utils.SoapHandler;

@Controller
public class AuthenticationController
{

	@Autowired
	private OrganisationDAO orgDAO;
	
	private User user;
	private MailHandler mailHandler = new MailHandler();
	private SoapHandler soapHandler = new SoapHandler();

	// When we request for the current user information
	@RequestMapping(value = "/user", method = RequestMethod.POST)
	public @ResponseBody
	User user(HttpSession session)
	{
		return (User) session.getAttribute("user");
	}
	
	// Removes the user from the session
	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public void logout(HttpSession session) {
		user = new User();
		session.setAttribute("user", null);
	}
	
	// Authenticates the user information against the database
	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
	public @ResponseBody
	User authenticate(@RequestParam("email") String email, @RequestParam("password") String password, HttpSession session)
	{
		// Create the user
		user = new User();
		
		// Get a record from the database based on the email
		Organisation organisation = orgDAO.getOrganisation(email);

		// Ensure it is valid and check the decrypted passwords are equal
		if (organisation != null && password.equals(decrypt(organisation.getPassword())))
		{

			// Check if the organisation is active
			if (!organisation.isActive())
			{

				// Set the status to not active
				user.setStatus(User.USER_NOT_ACTIVE);
				
			}
			else
			{
				
				// Else set the status to success
				user.setStatus(User.SUCCESS);
				
				// Set the basic user information
				user.setName(organisation.getName());
				user.setEmail(organisation.getEmail());
				
				// Add the subscribers based on the organisation number range
				if (soapHandler.addSubscribers(organisation.getNumberRange(), 10, 1, 100, 1000000))
				{
					// Set the available numbers that the user can use
					String numberRange = String.format("%s000%02d - %s000%02d", organisation.getNumberRange(), 1, organisation.getNumberRange(), 10);
					user.setNumberRange(numberRange);
					user.setSimobiNumber(String.format("%s000%01d", organisation.getNumberRange(), 0));
				}
				
			}

		}
		
		// Set the user in the session
		session.setAttribute("user", user);

		// Return the user
		return user;
	}

	// Registers a new user into the database
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public @ResponseBody
	int register(@RequestParam("email") String email, @RequestParam("organisation") String organisation, @RequestParam("password") String password,
				 @RequestParam(value = "ip", required = false) String ip, @RequestParam("recaptcha") String recaptcha)
	{

		// Verify against the Recaptcha
		if (verifyRecaptcha(recaptcha))
		{
			// Ensure the organisation email does not already exist
			if (orgDAO.getOrganisation(email) != null)
				return 2;

			// Create an organisation record
			Organisation org = new Organisation();
			
			// Set the information
			org.setEmail(email);
			org.setName(organisation);
			org.setPassword(encrypt(password));
			org.setIp(ip);
			
			// Create a counter
			int counter = 0;
			
			// Create a random number range
			String numberRange = String.format("%3d", new Random(new Date().getTime()).nextInt(1000));
			
			// Ensure it is unique in the database, try at least 100 times before just using one of them
			while (orgDAO.hasNumberRange(numberRange) && counter++ < 100)
			{
				// Create a new random number range
				numberRange = String.format("%3d", new Random(new Date().getTime()).nextInt(1000));
			}
			
			// If the counter is 100 or above, then return 
			if (counter >= 100)
			{
				return 3;
			}
			
			// Set the number range
			org.setNumberRange(numberRange);
			
			// Set the organisation as inactive
			org.setActive(false);

			// Insert the organisation
			orgDAO.upsert(org);

			try
			{
				// Send a mail to myself
				mailHandler.sendMail("c4u-scripts@concurrent.co.za", "New Registration", String.format("Hi<br/><br/>"
																			 + "There is a new registration waiting to be activated.<br/><br/>"
																			 + "Email: %s<br/>"
																			 + "Organisation: %s<br/>"
																			 + "IP: %s<br/><br/>"
																			 + "Best Regards<br/>C4U", 
																			 email, organisation, ip));
			}
			catch (Exception e)
			{
				
			}
			
			// Return with success
			return 0;
		}

		// Return with invalid recaptcha
		return 1;
	}

	private String recaptchaUrl = "https://www.google.com/recaptcha/api/siteverify";
	private String recaptchaSecretKey = "6LfVAQcTAAAAAGxepLEUoNydcMDpNyJalZ0ciDoi";

	// Verifies the recaptcha via google
	private boolean verifyRecaptcha(String recaptcha)
	{
		try
		{
			// Create the url
			URL url = new URL(recaptchaUrl);

			// Open the connection
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

			// Set the request method
			con.setRequestMethod("POST");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			// Add the secret and the recaptcha response
			String postParams = "secret=" + recaptchaSecretKey + "&response=" + recaptcha;

			// Send the request
			con.setDoOutput(true);
			try (DataOutputStream writer = new DataOutputStream(con.getOutputStream()))
			{
				// Writes to the stream
				writer.writeBytes(postParams);
				writer.flush();
			}

			// Get the response
			StringBuffer response = new StringBuffer();

			// Read from the input stream
			try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())))
			{
				// Read every individual line and add it to the response
				String inputLine;
				while ((inputLine = in.readLine()) != null)
				{
					response.append(inputLine);
				}
			}

			// Convert the response to a Json object
			JsonReader jsonReader = Json.createReader(new StringReader(response.toString()));
			JsonObject jsonObject = jsonReader.readObject();
			jsonReader.close();

			// Check if it was successful
			return jsonObject.getBoolean("success");
		}
		catch (Exception e)
		{
			// Else return false
			return false;
		}
	}

	// Key used to encrypt
	private String key = "qX7z88Ae0n16Qnx5";

	// Encrypts a string to encrypted base64
	private String encrypt(String text)
	{
		// Create the AES key
		Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
		
		try
		{
			// Create the cipher
			Cipher cipher = Cipher.getInstance("AES");
			
			// Initialise the cipher with the key
			cipher.init(Cipher.ENCRYPT_MODE, aesKey);

			// Encrypt the text
			byte encrypted[] = cipher.doFinal(text.getBytes());
			
			// Convert the bytes to base64
			return DatatypeConverter.printBase64Binary(encrypted);
		}
		catch (Exception e)
		{
			// Else return null
			return null;
		}
	}

	// Decrypts an encrypted base64 string to a normal string
	private String decrypt(String text)
	{
		// Create the AES key
		Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
		
		try
		{
			// Create the cipher
			Cipher cipher = Cipher.getInstance("AES");
			
			// Initiliase the cipher with the key
			cipher.init(Cipher.DECRYPT_MODE, aesKey);

			// Decrypt the encrypted string
			byte decrypted[] = cipher.doFinal(DatatypeConverter.parseBase64Binary(text));
			
			// Create a string from the bytes
			return new String(decrypted);
		}
		catch (Exception e)
		{
			// Else return null
			return null;
		}
	}
}
