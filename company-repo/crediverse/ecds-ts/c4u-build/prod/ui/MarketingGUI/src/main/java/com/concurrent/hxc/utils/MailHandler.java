package com.concurrent.hxc.utils;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailHandler
{
	
	private final String smtpHost = "zimbra.concurrent.co.za";

	public void sendMail(String to, String subject, String content) throws MessagingException, UnsupportedEncodingException
	{
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", smtpHost);
		
		// TEMP
		properties.setProperty("mail.user", "justing@concurrent.co.za");
		properties.setProperty("mail.password", "passwordHere");
		
		Session session = Session.getDefaultInstance(properties);
		
		MimeMessage mimeMessage = new MimeMessage(session);
		
		// Set the sender
		mimeMessage.setFrom(new InternetAddress("c4u-scripts@concurrent.co.za", "C4U Demo Lab"));
		
		// Set the recipient
		mimeMessage.setRecipient(RecipientType.TO, new InternetAddress(to));
		
		// Set the subject of email
		mimeMessage.setSubject(subject);
		
		// Set the message of the email
		mimeMessage.setContent(content, "text/html");
		
		// Send the message
		Transport.send(mimeMessage);
	}
	
}
