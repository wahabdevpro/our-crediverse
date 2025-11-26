package hxc.services.ecds.util;

import java.util.ArrayList;
import java.util.Date;

import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.smtp.ISmtpConnector;
import hxc.services.ecds.rest.ICreditDistribution;

public class EmailUtils {
	final static Logger logger = LoggerFactory.getLogger(EmailUtils.class);
	
	private ICreditDistribution context;
	
	public EmailUtils(ICreditDistribution context)
	{
		this.context = context;
	}
	
	public boolean sendEmail(InternetAddress fromAddress, String toAddress, String subject, String body, MimeBodyPart attachment, int timesToTry, boolean rethrow) throws Exception
	{
		if (toAddress == null || toAddress.isEmpty())
		{
			logger.warn("Send email called without a TO email address with subject({}), ignorning", subject);
			return false;
		}
		ISmtpConnector smtpConnector = context.getSmtpConnector();
		MimeMessage mimeMessage = smtpConnector.createMimeMessage();
		if(mimeMessage == null)
		{
			logger.error("Cannot send email. SMTP Connector could not create MIME message (mimeMessage == null)");
			logger.warn("sendEmail:: subject was ({}).", (subject != null)?subject:"");
			logger.warn("sendEmail:: content was ({})", (body != null)?body:"");
			return false;
		}
		mimeMessage.setFrom(fromAddress);
		mimeMessage.setRecipients(Message.RecipientType.TO, new InternetAddress[]{new InternetAddress(toAddress)});
		mimeMessage.setSubject(subject);
		Multipart multipart = new MimeMultipart();
		{
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setText(body);
			multipart.addBodyPart(mimeBodyPart);
		}
		if ( attachment != null )
		{
			multipart.addBodyPart(attachment);
		}
		mimeMessage.setContent(multipart);
		mimeMessage.setSentDate(new Date());
		boolean retry = true;
		while(retry)
		{
			try {
				smtpConnector.send(mimeMessage);
				break;
			} catch (Exception e) {
				timesToTry--;
				retry = timesToTry > 0;
				logger.error("SMTP send failed to send report due to exception. {}. Retries left {}", e.toString(), timesToTry);
				if(timesToTry <= 0)
				{
					if(rethrow)	throw e; 
					else return false;
				}
			}
		}
		return true;
	}

	public boolean sendEmail(InternetAddress fromAddress, ArrayList<String> toAddresses, ArrayList<String> bccAddresses, String subject, String body, ArrayList<MimeBodyPart> attachments, int timesToTry, boolean rethrow) throws Exception
	{
		ArrayList<InternetAddress> toAddressList = new ArrayList<InternetAddress>();
		ArrayList<InternetAddress> bccAddressList = new ArrayList<InternetAddress>();
		if(toAddresses != null)
		{
			for(String address: toAddresses)
			{
				if(!address.isEmpty())
					toAddressList.add(new InternetAddress(address));
			}
		}
		if(bccAddresses != null)
		{
			for(String address: bccAddresses)
			{
				if(!address.isEmpty())
					bccAddressList.add(new InternetAddress(address));
			}
		}
		ISmtpConnector smtpConnector = context.getSmtpConnector();
		MimeMessage mimeMessage = smtpConnector.createMimeMessage();
		if(mimeMessage == null)
		{
			logger.error("Cannot send email. SMTP Connector could not create MIME message (mimeMessage == null)");
			logger.warn("sendEmail:: subject was ({}).", (subject != null)?subject:"");
			logger.warn("sendEmail:: content was ({})", (body != null)?body:"");
			return false;
		}
		mimeMessage.setFrom(fromAddress);
		mimeMessage.setRecipients(Message.RecipientType.TO, toAddressList.toArray(new InternetAddress[toAddressList.size()]));
		mimeMessage.setRecipients(Message.RecipientType.BCC, bccAddressList.toArray(new InternetAddress[bccAddressList.size()]));
		mimeMessage.setSubject(subject);
		Multipart multipart = new MimeMultipart();
		{
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setText(body);
			multipart.addBodyPart(mimeBodyPart);
		}
		if ( attachments != null )
		{
			for(MimeBodyPart attachment: attachments)
			{
				multipart.addBodyPart(attachment);
			}
		}

		mimeMessage.setContent(multipart);
		mimeMessage.setSentDate(new Date());
		boolean retry = true;
		while(retry)
		{
			try {
				smtpConnector.send(mimeMessage);
				break;
			} catch (Exception e) {
				timesToTry--;
				retry = timesToTry > 0;
				logger.error("SMTP send failed to send report due to exception. {}. Retries left {}", e, timesToTry);
				if(timesToTry <= 0)
				{
					if(rethrow)	throw e; 
					else return false;
				}
			}
		}
		return true;
	}
}
