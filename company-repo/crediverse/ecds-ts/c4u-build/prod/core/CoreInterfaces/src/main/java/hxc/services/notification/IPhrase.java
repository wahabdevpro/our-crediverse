package hxc.services.notification;

import hxc.configuration.Configurable;

@Configurable
public interface IPhrase
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Const
	//
	// /////////////////////////////////
	public static final int MAX_LANGUAGES = 4;
	public static int DEFAULT_LANGUAGE_ID = 1;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	public abstract String get(String languageCode);

	public abstract String get(String languageCode, String alternateLanguageCode);

	public abstract String getSafe(String languageCode, String defaultText);

	public abstract IPhrase set(String languageCode, String text);

	public abstract String[] getLanguageCodes();

	public abstract boolean matches(String text);

	public abstract boolean overlaps(IPhrase phrase);


	// Popular languages
	public abstract IPhrase eng(String text);

	public abstract IPhrase ara(String text);

	public abstract IPhrase afr(String text);

	public abstract IPhrase fre(String text);

	public abstract IPhrase por(String text);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// iso 639-2
	//
	// /////////////////////////////////

	public static final String ENG = "eng";
	public static final String FRE = "fre";
	public static final String ARA = "ara";
	public static final String AFR = "afr";
	public static final String POR = "por";

}
