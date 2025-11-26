package cs.utility;

public class StringUtil
{

	public static boolean isNullOrBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	public static boolean isEmpty(String... items)
	{
		boolean result = false;

		for (String item : items)
		{
			if (item == null || item.length() <= 0)
			{
				result = true;
				break;
			}
		}

		return result;
	}

	public static String utf8ToCanonical(String s)
	{
		return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFKD);
	}
}
