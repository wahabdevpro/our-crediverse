package cs.dto;

public class Language
{
	private String lang;

	public String getLang()
	{
		return lang;
	}

	public void setLang(String lang)
	{
		this.lang = lang;
	}

	@Override
	public String toString()
	{
		StringBuilder string = new StringBuilder();
		string.append("lang = ");
		string.append(lang);
		return string.toString();
	}
}
