package hxc.connectors.smtp;

import jakarta.mail.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SmtpHistory implements Serializable, ISmtpHistory
{
	private static final long serialVersionUID = -6140282043147400360L;

	protected List<String> from;
	protected String fromText;
	protected List<String> recipients = new ArrayList<String>();
	protected String recipientsText;
	protected String subject;
	protected String body;
	protected List<String> attachmentNames = new ArrayList<String>();
	protected String attachmentNamesText;
	protected Date sentDate;

	public SmtpHistory(Message message) throws MessagingException, IOException
	{	
		this.sentDate = (message.getSentDate() == null ? new Date() : message.getSentDate());
		Address[] from = message.getFrom();
		this.from = new ArrayList<String>(from != null ? from.length : 0);
		if ( from != null )
		{
			for ( int i = 0; i < from.length; ++i )
			{
				this.from.add( from[i] == null ? "NULL" : from[i].toString() );
			}
		}
		this.fromText = String.join(", ", this.from);
		for ( Message.RecipientType recipientType: new Message.RecipientType[]{ Message.RecipientType.BCC, Message.RecipientType.CC, Message.RecipientType.TO } )
		{
			Address[] recipients = message.getRecipients( recipientType );
			if ( recipients != null )
			{
				for ( int i = 0; i < recipients.length; ++i )
				{
					this.recipients.add(String.format( "%s:%s", recipientType.toString(), ( recipients[i] == null ? "NULL" : recipients[i].toString() ) ));
				}
			}
		}
		this.recipientsText = String.join(", ", this.recipients);
		this.subject = ( message.getSubject() == null ? "NULL" : message.getSubject() );
		Object content = message.getContent();
		if ( content == null ) this.body = "NULL";
		else if ( content instanceof Multipart)
		{
			Multipart multipartContent = (Multipart) content;
			StringBuilder bodyBuilder = new StringBuilder();
			boolean first = true;
			for ( int i = 0; i < multipartContent.getCount(); ++i )
			{
				BodyPart bodyPart = multipartContent.getBodyPart(i);
				String fileName = bodyPart.getFileName();
				if ( fileName != null ) attachmentNames.add(fileName);
				else
				{
					if ( first ) first = false;
					else bodyBuilder.append("\n");
					bodyBuilder.append(bodyPart.getContent().toString());
				}
			}
			this.body = bodyBuilder.toString();
		}
		else this.body = content.toString();

		if ( this.attachmentNames.size() == 0 ) this.attachmentNamesText = "NONE";
		else this.attachmentNamesText = String.join(", ", this.attachmentNames);
	}

	@Override
	public List<String> getFrom()
	{
		return this.from;
	}
	public SmtpHistory setFrom( List<String> from )
	{
		this.from = from;
		return this;
	}

	@Override
	public String getFromText()
	{
		return this.fromText;
	}
	public SmtpHistory setFromText( String fromText )
	{
		this.fromText = fromText;
		return this;
	}

	@Override
	public List<String> getRecipients()
	{
		return this.recipients;
	}
	public SmtpHistory setRecipients( List<String> recipients )
	{
		this.recipients = recipients;
		return this;
	}

	@Override
	public String getRecipientsText()
	{
		return this.recipientsText;
	}
	public SmtpHistory setRecipientsText( String recipientsText )
	{
		this.recipientsText = recipientsText;
		return this;
	}


	@Override
	public String getSubject()
	{
		return this.subject;
	}
	public SmtpHistory setSubject( String subject )
	{
		this.subject = subject;
		return this;
	}

	@Override
	public String getBody()
	{
		return this.body;
	}
	public SmtpHistory setBody( String body )
	{
		this.body = body;
		return this;
	}

	@Override
	public List<String> getAttachmentNames()
	{
		return this.attachmentNames;
	}
	public SmtpHistory setAttachmentNames( List<String> attachmentNames )
	{
		this.attachmentNames = attachmentNames;
		return this;
	}

	@Override
	public String getAttachmentNamesText()
	{
		return this.attachmentNamesText;
	}
	public SmtpHistory setAttachmentNamesText( String attachmentNamesText )
	{
		this.attachmentNamesText = attachmentNamesText;
		return this;
	}

	@Override
	public Date getSentDate()
	{
		return this.sentDate;
	}
	public SmtpHistory setSentDate( Date sentDate )
	{
		this.sentDate = sentDate;
		return this;
	}

}
