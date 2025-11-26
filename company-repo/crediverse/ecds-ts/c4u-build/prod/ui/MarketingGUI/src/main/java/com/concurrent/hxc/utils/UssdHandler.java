package com.concurrent.hxc.utils;

import hxc.utils.protocol.hux.HandleUSSDRequest;
import hxc.utils.protocol.hux.HandleUSSDRequestMembers;
import hxc.utils.protocol.hux.HandleUSSDResponse;
import hxc.utils.xmlrpc.XmlRpcClient;
import hxc.utils.xmlrpc.XmlRpcConnection;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UssdHandler
{

	class UssdSession
	{

		public String number;
		public int transactionID = 1000;
		public int sessionID = 2000;
		public String serviceCode;

	}

	private final String huxUrl = "http://localhost:14000/RPC2";

	private Map<String, UssdSession> sessions = new HashMap<String, UssdHandler.UssdSession>();
	private static Pattern ussdPattern = Pattern.compile("^\\*(\\d+)(\\*\\d+)*\\#$");

	public String send(String from, String text)
	{
		// Get the ussd session
		UssdSession session = sessions.get(from);
		
		// If not found, then create a new session
		if (session == null)
		{
			session = new UssdSession();
			sessions.put(from, session);
		}

		// Create default response
		String response = "Bad USSD String";

		// Construct a HandleUSSDRequest
		HandleUSSDRequest ussdRequest = new HandleUSSDRequest();
		ussdRequest.members = new HandleUSSDRequestMembers();
		ussdRequest.members.MSISDN = from;
		ussdRequest.members.TransactionId = Integer.toString(session.transactionID++);
		ussdRequest.members.TransactionTime = new Date();

		// Parse the USSD String
		Matcher match = ussdPattern.matcher(text);

		// Check if it is the start of a session
		if (match.matches())
		{
			// Edit the properties accordingly 
			ussdRequest.members.USSDServiceCode = session.serviceCode = match.group(1);
			ussdRequest.members.USSDRequestString = text.substring(ussdRequest.members.USSDServiceCode.length() + 1);
			ussdRequest.members.response = false;
			ussdRequest.members.SessionId = ++session.sessionID;
		}
		else
		{

			// Edit with the current session
			ussdRequest.members.USSDServiceCode = session.serviceCode;
			ussdRequest.members.USSDRequestString = text;
			ussdRequest.members.response = true;
			ussdRequest.members.SessionId = session.sessionID;
		}

		// Create an XmlRpcClient
		XmlRpcClient client = new XmlRpcClient(huxUrl, 3000, 3000);

		// Send Request
		try (XmlRpcConnection connection = client.getConnection())
		{
			HandleUSSDResponse ussdResponse = connection.call(ussdRequest, HandleUSSDResponse.class);
			response = ussdResponse.members.USSDResponseString;
		}
		catch (Exception e)
		{
			response = "Exception: " + e.getMessage();
		}

		// Return the response string
		return response;
	}

}
