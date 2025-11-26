package hxc.ecds.protocol.rest.config;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Phrase implements Serializable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String ENG = "en";
	public static final String FRE = "fr";
	public static final String ARA = "ar";
	public static final String AFR = "af";
	public static final String POR = "pt";

	private static final long serialVersionUID = 367550603398217774L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	private Map<String, String> texts = new HashMap<String, String>();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public Map<String, String> getTexts()
	{
		return texts;
	}

	public Phrase setTexts(Map<String, String> texts)
	{
		this.texts = texts;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Phrase()
	{

	}

	public Phrase set(String languageID, String text)
	{
		texts.put(languageID, text);
		return this;
	}

	public static Phrase en(String text)
	{
		return new Phrase().set(ENG, text);
	}

	public static Phrase fr(String text)
	{
		return new Phrase().set(FRE, text);
	}

	public Phrase eng(String text)
	{
		return set(ENG, text);
	}

	public Phrase ara(String text)
	{
		return set(ARA, text);
	}

	public Phrase afr(String text)
	{
		return set(AFR, text);
	}

	public Phrase fre(String text)
	{
		return set(FRE, text);
	}

	public Phrase por(String text)
	{
		return set(POR, text);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public String get(String languageID)
	{
		if (languageID == null || texts.isEmpty())
			return null;
		return texts.get(languageID);
	}

	public String safe(String languageID, String defaultText)
	{
		String result = get(languageID);
		if (result != null && !result.isEmpty())
			return result;

		result = get(ENG);
		if (result != null && !result.isEmpty())
			return result;

		for (String key : texts.keySet())
		{
			result = texts.get(key);
			if (result != null && !result.isEmpty())
				return result;
		}

		return defaultText;
	}

	public boolean contains(String text)
	{
		if (text == null || text.isEmpty())
			return false;

		for (String language : texts.values())
		{
			if (text.equalsIgnoreCase(language))
				return true;
		}

		return false;
	}

	public boolean has(String languageID)
	{
		return texts.containsKey(languageID);
	}

	public boolean empty()
	{
		return texts == null || texts.isEmpty();
	}

	public static boolean nullOrEmpty(Phrase phrase)
	{
		return phrase == null || phrase.empty();
	}

	public static boolean someNullOrEmpty(Phrase phrase)
	{
		if (nullOrEmpty(phrase))
			return true;
		
		for (String text:phrase.texts.values())
		{
			if (text == null || text.isEmpty())
				return true;
		}
		
		return false;
	}

	public Phrase format(String format)
	{
		Phrase result = new Phrase();
		for (String key : texts.keySet())
		{
			result.texts.put(key, String.format(format, texts.get(key)));
		}
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;

		if(other == null || (this.getClass() != other.getClass())){
			return false;
		}

		Phrase phrase = (Phrase) other;
		
		return Objects.equals(phrase.getTexts(), this.getTexts());
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode( new Object[] {
			texts
		});
	}

}
