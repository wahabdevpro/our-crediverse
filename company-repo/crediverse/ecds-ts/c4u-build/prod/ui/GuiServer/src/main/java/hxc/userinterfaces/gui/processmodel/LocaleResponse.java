package hxc.userinterfaces.gui.processmodel;

import java.util.Locale;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import hxc.userinterfaces.gui.utils.GuiUtils;

public class LocaleResponse
{
	private String[] lang;
	private String[] apha;
	private String[] dir;

	public String[] getLang()
	{
		return lang;
	}

	public void setLang(String[] lang)
	{
		this.lang = lang;
		dir = new String[] { "ltr", "ltr", "ltr", "ltr" };
	}

	public String[] getDir()
	{
		return dir;
	}

	public String[] getApha()
	{
		return apha;
	}

	public void setApha(String[] apha)
	{
		this.apha = apha;
	}

	public void convertShortCodesToFullLanuage()
	{
		int toFind = 0;
		for (String ls : lang)
		{
			if (ls != null && ls.length() > 0)
			{
				toFind++;
			}
		}

		String[] languages = Locale.getISOLanguages();

		// Always one!
		for (int i = 0; i < lang.length; i++)
		{
			if (lang[i].equals("fre"))
			{
				dir[i] = "ltr";
				lang[i] = "French";
				toFind--;
			}
		}

		for (String language : languages)
		{
			Locale locale = new Locale(language);
			for (int i = 0; i < lang.length; i++)
			{
				if (lang[i].equalsIgnoreCase(locale.getISO3Language()))
				{
					dir[i] = GuiUtils.extractLanguageDirection(GuiUtils.extractLanguageInfo(lang[i]));
					lang[i] = locale.getDisplayLanguage();
					toFind--;
				}
			}
			if (toFind == 0)
			{
				break;
			}
		}

	}

	public JsonObject toJson()
	{
		JsonObject job = new JsonObject();
		JsonArray jarr = new JsonArray();
		if (lang != null)
		{
			for (int i = 0; i < lang.length; i++)
			{
				jarr.add(new JsonPrimitive(lang[i]));
			}
		}
		job.add("lang", jarr);

		jarr = new JsonArray();
		if (dir != null)
		{
			for (int i = 0; i < dir.length; i++)
			{
				jarr.add(new JsonPrimitive(dir[i]));
			}
		}
		job.add("dir", jarr);

		jarr = new JsonArray();
		if (apha != null)
		{
			for (int i = 0; i < apha.length; i++)
			{
				jarr.add(new JsonPrimitive(apha[i]));
			}
		}
		job.add("apha", jarr);
		return job;
	}

	@Override
	public String toString()
	{
		return toJson().toString();
	}
}
