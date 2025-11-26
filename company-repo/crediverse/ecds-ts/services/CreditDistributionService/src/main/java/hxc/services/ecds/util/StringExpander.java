package hxc.services.ecds.util;

import java.util.Locale;
import java.util.regex.Pattern;

import hxc.ecds.protocol.rest.config.Phrase;

public abstract class StringExpander<T>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private static final Pattern pattern = Pattern.compile("(?=\\{)|(?<=\\})");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	public StringExpander()
	{
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public String expandNotification(Phrase notification, Locale locale, Phrase[] fields, T state)
	{
		String text = notification.safe(( locale != null ? locale.getLanguage() : null ), "");
		String[] parts = pattern.split(text);
		StringBuilder builder = new StringBuilder(text.length() << 2);
		for (String part : parts)
		{
			if (part.startsWith("{"))
			{
				if (fields != null)
				{
					boolean found = false;
					for (Phrase field : fields)
					{
						if (field.contains(part))
						{
							String expandedField = expandField(field.get(Phrase.ENG), locale, state);
							builder.append(expandedField);
							found = true;
							break;
						}
					}
					if (!found)
					{
						String expandedField = expandField(part, locale, state);
						if (expandedField != null)
							builder.append(expandedField);
					}
				}
			}
			else
				builder.append(part);
		}
		return builder.toString();
	}

	protected abstract String expandField(String englishName, Locale locale, T state);

}
