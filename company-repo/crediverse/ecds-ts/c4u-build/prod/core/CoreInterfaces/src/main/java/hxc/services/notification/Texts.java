package hxc.services.notification;

import java.io.Serializable;

import hxc.configuration.Configurable;

@Configurable
@SuppressWarnings("serial")
public class Texts implements ITexts, Serializable
{
	private String[] texts = new String[IPhrase.MAX_LANGUAGES + 1];

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@Override
	public String getModelText()
	{
		return texts[0];
	}

	@Override
	public void setModelText(String modelText)
	{
		texts[0] = modelText;
	}

	@Override
	public String getText(Integer languageID)
	{
		if (languageID == null)
			languageID = IPhrase.DEFAULT_LANGUAGE_ID;
		return texts[safeLanguageID(languageID)];
	}

	@Override
	public void setText(Integer languageID, String text)
	{
		if (languageID == null)
			languageID = IPhrase.DEFAULT_LANGUAGE_ID;
		texts[safeLanguageID(languageID)] = text;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Texts()
	{
	}

	public Texts(String modelText)
	{
		setModelText(modelText);
	}

	public Texts(String modelText, String... texts)
	{
		this.texts[0] = modelText;
		set(texts);
	}

	public Texts(int languageID, String text)
	{
		this.texts[0] = text;
		this.texts[languageID] = text;
	}

	@Override
	public void set(String... texts)
	{
		int index = 1;
		for (String text : texts)
		{
			if (index > IPhrase.MAX_LANGUAGES)
				break;
			this.texts[index++] = text;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public boolean matches(String text)
	{
		if (text == null || text.length() == 0)
			return false;

		for (int index = 0; index <= IPhrase.MAX_LANGUAGES; index++)
		{
			if (text.equalsIgnoreCase(texts[index]))
				return true;
		}

		return false;
	}

	@Override
	public String getSafeText(Integer languageID)
	{
		String result = texts[safeLanguageID(languageID)];
		if (result != null && result.length() > 0)
			return result;
		else
			return texts[0];
	}

	private int safeLanguageID(Integer languageID)
	{
		if (languageID == null || languageID < 0 || languageID > IPhrase.MAX_LANGUAGES)
			return IPhrase.DEFAULT_LANGUAGE_ID;
		else
			return languageID;
	}

}
