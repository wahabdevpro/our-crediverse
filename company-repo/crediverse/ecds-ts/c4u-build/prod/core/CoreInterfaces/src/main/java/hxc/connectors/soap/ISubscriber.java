package hxc.connectors.soap;

public interface ISubscriber
{

	/**
	 * @return MSISDN of the Subscriber
	 */
	public abstract String getInternationalNumber();

	public abstract String getNationalNumber();

	public abstract int getLanguageID();

	public abstract boolean isSameNumber(String msisdn);

	public int getServiceClass();

}