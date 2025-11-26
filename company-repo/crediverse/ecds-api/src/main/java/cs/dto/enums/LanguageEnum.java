package cs.dto.enums;

import cs.dto.GuiTransaction.TransactionStatusEnum;

public enum LanguageEnum
{
	EN("en"),
	FR("fr"),
	NOTSET("");
	private String val;
	private LanguageEnum(String val) {
		this.val = val;
	}
	public String getVal()
	{
		return this.val;
	}
	public static LanguageEnum fromString(String val)
	{
		LanguageEnum result = NOTSET;
		if (val != null)
		{
			switch (val)
			{
			case "en":
				result = EN;
				break;
			case "fr":
				result = FR;
				break;
			default:
				result = NOTSET;
			}
		}
		return result;
	}
	public static boolean contains(String value)
	{
		for (TransactionStatusEnum c : TransactionStatusEnum.values()) {
	        if (c.name().equals(value)) {
	            return true;
	        }
	    }
	    return false;
	}
}