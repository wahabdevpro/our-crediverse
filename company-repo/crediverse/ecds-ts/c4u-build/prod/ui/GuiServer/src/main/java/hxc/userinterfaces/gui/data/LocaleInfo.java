package hxc.userinterfaces.gui.data;

public class LocaleInfo
{
	private String langCode = "EN";
	private String langDesc = "English";
	private boolean langRTL;

	public LocaleInfo()
	{
	}

	/**
	 * @return the langCode
	 */
	public String getLangCode()
	{
		return langCode;
	}

	/**
	 * @param langCode
	 *            the langCode to set
	 */
	public void setLangCode(String langCode)
	{
		this.langCode = langCode;
	}

	/**
	 * @return the langDesc
	 */
	public String getLangDesc()
	{
		return langDesc;
	}

	/**
	 * @param langDesc
	 *            the langDesc to set
	 */
	public void setLangDesc(String langDesc)
	{
		this.langDesc = langDesc;
	}

	/**
	 * @return the langRTL
	 */
	public boolean isLangRTL()
	{
		return langRTL;
	}

	/**
	 * @param langRTL
	 *            the langRTL to set
	 */
	public void setLangRTL(boolean langRTL)
	{
		this.langRTL = langRTL;
	}

}
