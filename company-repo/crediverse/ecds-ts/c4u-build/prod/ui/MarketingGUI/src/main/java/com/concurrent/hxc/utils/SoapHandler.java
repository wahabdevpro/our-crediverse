package com.concurrent.hxc.utils;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

public class SoapHandler
{
	
	private final String airsimUri = "http://protocol.airsim.services.hxc/";
	private final String identifier = "demolab";
	
	// //////////////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		// /////////////////////////////////
	
	public SoapHandler()
	{
		try
		{
			// Create the soap factory
			SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance();
			
			// Create the connection
			SOAPConnection connection = factory.createConnection();

			// Create the endpoint
			URL endpoint = new URL(new URL("http://c4u.concurrent.co.za/air"), "", new URLStreamHandler()
			{
				@Override
				protected URLConnection openConnection(URL url)
				{
					try
					{
						// Set the connection timeout and the read timeout
						URL target = new URL(url.toString());
						URLConnection connection = target.openConnection();
						connection.setConnectTimeout(10000);
						connection.setReadTimeout(5000);
						return (connection);
					}
					catch (Exception e)
					{
						return null;
					}
				}
			});
			
			// Start airsim
			connection.call(start(), endpoint);
			
			// Add Andries' number to airsim
			connection.call(addSubscriber("0824452655", 1, 100, 1000000), endpoint);
			
			// Add my number to airsim
			connection.call(addSubscriber("0848654805", 1, 100, 1000000), endpoint);
			
			// Close the connection
			connection.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// Adds subscribers in a number range
	public boolean addSubscribers(String numberRange, int count, int languageID, int serviceClass, long accountValue)
	{
		
		try 
		{
			
			// Create the factory
			SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance();
			
			// Create the connection
			SOAPConnection connection = factory.createConnection();

			// Create the endpoint
			URL endpoint = new URL(new URL("http://c4u.concurrent.co.za/air"), "", new URLStreamHandler()
			{
				@Override
				protected URLConnection openConnection(URL url)
				{
					try
					{
						// Set the connection timeout and read timeout
						URL target = new URL(url.toString());
						URLConnection connection = target.openConnection();
						connection.setConnectTimeout(10000);
						connection.setReadTimeout(5000);
						return (connection);
					}
					catch (Exception e)
					{
						return null;
					}
				}
			});
			
			// Iterate through the number range
			for (int i = 0; i < count + 1; i++)
			{
				
				// Create the number
				String msisdn = String.format("%s000%02d", numberRange, i);
				
				// Add the subscriber
				connection.call(addSubscriber(msisdn, languageID, serviceClass, accountValue), endpoint);
				
			}
			
			// Close the connection
			connection.close();
			
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
		
	}
	
	// Creates the soap message to start airsim
	public SOAPMessage start() throws Exception
	{
		// Create the message factory
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();

		// SOAP Envelope
		SOAPEnvelope envelope = soapPart.getEnvelope();
		envelope.addNamespaceDeclaration(identifier, airsimUri);

		// SOAP Body
		SOAPBody soapBody = envelope.getBody();
	
		// Add the start method name
		soapBody.addChildElement("start", identifier);
		
		// Save the message
		soapMessage.saveChanges();

		// Return the message
		return soapMessage;
	}

	// Creates a soap message to add a subscriber
	public SOAPMessage addSubscriber(String msisdn, int languageID, int serviceClass, long accountValue) throws Exception
	{
		// Create the message factory
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();

		// SOAP Envelope
		SOAPEnvelope envelope = soapPart.getEnvelope();
		envelope.addNamespaceDeclaration(identifier, airsimUri);

		// SOAP Body
		SOAPBody soapBody = envelope.getBody();
		SOAPElement soapBodyElem = soapBody.addChildElement("addSubscriber", identifier);
		
		// Request Fields
		SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("msisdn");
		soapBodyElem1.addTextNode(msisdn);
		
		// Language ID
		soapBodyElem1 = soapBodyElem.addChildElement("languageID");
		soapBodyElem1.addTextNode(Integer.toString(languageID));
		
		// Service class
		soapBodyElem1 = soapBodyElem.addChildElement("serviceClass");
		soapBodyElem1.addTextNode(Integer.toString(serviceClass));
		
		// Account Value
		soapBodyElem1 = soapBodyElem.addChildElement("accountValue");
		soapBodyElem1.addTextNode(Long.toString(accountValue));

		// The state of the subscriber
		soapBodyElem1 = soapBodyElem.addChildElement("state");
		soapBodyElem1.addTextNode("active");
		
		// Save the message
		soapMessage.saveChanges();

		// Return the message
		return soapMessage;
	}
	
}
