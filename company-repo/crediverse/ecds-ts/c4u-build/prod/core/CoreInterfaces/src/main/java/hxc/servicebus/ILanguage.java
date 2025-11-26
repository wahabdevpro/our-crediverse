package hxc.servicebus;

public interface ILanguage
{

	public abstract String getLanguage();

	public abstract String getLanguageName();

	public abstract String getAlpabet();

	public abstract String getEncodingScheme();

	public abstract String getDateFormat();

	public abstract String getTimeFormat();

	public abstract int getCurrencyDecimalDigits();
}
