package hxc.services.notification;

import java.io.Serializable;

import hxc.configuration.Configurable;

@Configurable
@SuppressWarnings("serial")
public class Phrase implements IPhrase, Serializable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	private static final int DEFAULT_SIZE = 4;

	// TODO Migrate to a map once Configuration can serialise it
	private int count = 0;
	private String[] codes = new String[DEFAULT_SIZE];
	private String[] phrases = new String[DEFAULT_SIZE];

	// private static final long serialVersionUID = -5794603898471577223L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IPhrase
	//
	// /////////////////////////////////

	public Phrase()
	{
	}

	public Phrase(String languageCode, String text)
	{
		set(languageCode, text);
	}

	public Phrase(IPhrase phrase)
	{
		for (String languageCode : phrase.getLanguageCodes())
		{
			set(languageCode, phrase.get(languageCode));
		}
	}

	@Override
	public String get(String languageCode)
	{
		if (languageCode == null)
			return null;
		int languageIndex = getIndex(languageCode);

		return languageIndex < 0 ? null : phrases[languageIndex];
	}

	@Override
	public String get(String languageCode, String alternateLanguageCode)
	{
		String result = get(languageCode);
		if (result == null)
			result = get(alternateLanguageCode);
		return result;
	}

	@Override
	public String getSafe(String languageCode, String defaultText)
	{
		String result = get(languageCode);
		if (result == null && phrases.length > 0)
			result = phrases[0];
		return result == null ? defaultText : result;
	}

	@Override
	public synchronized Phrase set(String languageCode, String text)
	{
		if (text != null && languageCode != null)
		{
			int languageIndex = getIndex(languageCode);
			if (languageIndex >= 0)
			{
				phrases[languageIndex] = text;
			}
			else
			{
				count++;
				if (count > codes.length)
				{
					codes = java.util.Arrays.copyOf(codes, count);
					phrases = java.util.Arrays.copyOf(phrases, count);
				}
				codes[count - 1] = languageCode.toLowerCase();
				phrases[count - 1] = text;
			}
		}

		return this;
	}

	public static Phrase en(String text)
	{
		return new Phrase().set(IPhrase.ENG, text);
	}

	public static Phrase fr(String text)
	{
		return new Phrase().set(IPhrase.FRE, text);
	}

	@Override
	public Phrase eng(String text)
	{
		return set(IPhrase.ENG, text);
	}

	@Override
	public Phrase ara(String text)
	{
		return set(IPhrase.ARA, text);
	}

	@Override
	public Phrase afr(String text)
	{
		return set(IPhrase.AFR, text);
	}

	@Override
	public Phrase fre(String text)
	{
		return set(IPhrase.FRE, text);
	}

	@Override
	public Phrase por(String text)
	{
		return set(IPhrase.POR, text);
	}

	@Override
	public boolean matches(String text)
	{
		if (text == null)
			return false;

		for (int index = 0; index < count; index++)
		{
			if (phrases[index].equalsIgnoreCase(text))
				return true;
		}

		return false;
	}

	@Override
	public boolean overlaps(IPhrase phrase)
	{
		if (phrase == null)
			return false;

		for (int index = 0; index < count; index++)
		{
			if (phrase.matches(phrases[index]))
				return true;
		}

		return false;
	}

	@Override
	public String toString()
	{
		String result = get(IPhrase.ENG);
		if (result == null)
			result = phrases.length > 0 ? phrases[0] : "Phrase";

		return result;
	}

	@Override
	public String[] getLanguageCodes()
	{
		return java.util.Arrays.copyOf(codes, count);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////

	private int getIndex(String languageCode)
	{
		if (languageCode == null)
			return -1;

		for (int index = 0; index < count; index++)
		{
			if (codes[index].equalsIgnoreCase(languageCode))
				return index;
		}

		return -1;
	}

}
