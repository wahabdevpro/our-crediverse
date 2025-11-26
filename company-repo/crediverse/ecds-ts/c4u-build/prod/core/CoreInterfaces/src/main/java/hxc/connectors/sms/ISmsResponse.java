package hxc.connectors.sms;

public interface ISmsResponse
{
	public abstract void setMessageID(String messageID);

	public abstract String getMessageID();

	public abstract void setResultMessage(String resultMessage);

	public abstract String getResultMessage();

	public abstract void setSequenceNumber(int sequenceNumber);

	public abstract int getSequenceNumber();
}