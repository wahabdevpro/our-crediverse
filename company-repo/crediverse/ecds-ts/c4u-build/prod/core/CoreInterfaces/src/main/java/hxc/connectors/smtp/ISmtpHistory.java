package hxc.connectors.smtp;

import java.util.Date;
import java.util.List;

public interface ISmtpHistory
{
	public List<String> getFrom();
	public String getFromText();
	public List<String> getRecipients();
	public String getRecipientsText();
	public String getSubject();
	public String getBody();
	public List<String> getAttachmentNames();
	public String getAttachmentNamesText();
	public Date getSentDate();
}
