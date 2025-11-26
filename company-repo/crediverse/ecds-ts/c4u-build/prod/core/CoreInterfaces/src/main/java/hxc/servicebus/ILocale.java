package hxc.servicebus;

import java.util.Date;

public interface ILocale
{
	public abstract int getMaxLanguages();

	public abstract int getDefaultLanguageID();

	public abstract String getDefaultLanguageCode();

	public abstract String getLanguage(Integer languageID);

	public abstract String getLanguageName(Integer languageID);

	public abstract String getAlpabet(Integer languageID);

	public abstract String getAlpabet(String languageCode);

	public abstract String getEncodingScheme(Integer languageID);

	public abstract String getEncodingScheme(String languageCode);

	public abstract String formatCurrency(long amount);

	public abstract String formatDate(Date date, Integer languageID);

	public abstract String formatTime(Date time, Integer languageID);

	public abstract String getDateFormat(Integer languageID);

	public abstract int getCurrencyDecimalDigits();

	public abstract String getCurrencyCode();

	public abstract ILanguage getLanguageLocale(Integer languageID);

	public abstract int getLanguageID(String languageCode);

	public abstract int getLanguageID(Integer languageID);

}