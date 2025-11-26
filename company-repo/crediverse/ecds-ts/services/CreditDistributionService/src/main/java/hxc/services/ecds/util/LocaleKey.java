package hxc.services.ecds.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class LocaleKey implements Comparable<LocaleKey>
{
	private static final Pattern splitPattern = Pattern.compile("_");
	private Locale locale;

	public LocaleKey(String language)
	{
		this(new Locale(language));
	}

	public LocaleKey(String language, String country)
	{
		this(new Locale(language, country));
	}

	public LocaleKey(String language, String country, String variant)
	{
		this(new Locale(language, country, variant));
	}

	public LocaleKey(Locale locale)
	{
		this.locale = locale;
	}

	public Locale getLocale()
	{
		return this.locale;
	}

	public String getLanguage()
	{
		return this.locale.getLanguage();
	}

	public String getCountry()
	{
		return this.locale.getCountry();
	}

	public String getVariant()
	{
		return this.locale.getVariant();
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.getLanguage(), this.getCountry(), this.getVariant());
	}

	@Override
	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		if (this == other)
			return true;
		if (!(other instanceof LocaleKey))
			return false;
		LocaleKey otherTyped = (LocaleKey) other;
		return (Objects.equals(this.getLanguage(), otherTyped.getLanguage()) && Objects.equals(this.getCountry(), otherTyped.getCountry())
				&& Objects.equals(this.getVariant(), otherTyped.getVariant()));
	}

	private static final List<Comparator<LocaleKey>> comparators = Arrays.<Comparator<LocaleKey>> asList(new Comparator<LocaleKey>()
	{
		@Override
		public int compare(LocaleKey lhs, LocaleKey rhs)
		{
			String lhsPart = lhs.getLanguage();
			String rhsPart = rhs.getLanguage();
			if (lhsPart == null && rhsPart == null)
				return 0;
			else if (lhsPart == null)
				return -1;
			else if (rhsPart == null)
				return 1;
			else
				return lhsPart.compareTo(rhsPart);
		}
	}, new Comparator<LocaleKey>()
	{
		@Override
		public int compare(LocaleKey lhs, LocaleKey rhs)
		{
			String lhsPart = lhs.getCountry();
			String rhsPart = rhs.getCountry();
			if (lhsPart == null && rhsPart == null)
				return 0;
			else if (lhsPart == null)
				return -1;
			else if (rhsPart == null)
				return 1;
			else
				return lhsPart.compareTo(rhsPart);
		}
	}, new Comparator<LocaleKey>()
	{
		@Override
		public int compare(LocaleKey lhs, LocaleKey rhs)
		{
			String lhsPart = lhs.getVariant();
			String rhsPart = rhs.getVariant();
			if (lhsPart == null && rhsPart == null)
				return 0;
			else if (lhsPart == null)
				return -1;
			else if (rhsPart == null)
				return 1;
			else
				return lhsPart.compareTo(rhsPart);
		}
	});

	@Override
	public int compareTo(LocaleKey other)
	{
		if (other == null)
			return 1;
		for (Comparator<LocaleKey> comparator : comparators)
		{
			int result = comparator.compare(this, other);
			if (result != 0)
				return result;
		}
		return 0;
	}

	@Override
	public String toString()
	{
		return this.locale.toString();
	}

	public static LocaleKey valueOf(String string)
	{
		String[] parts = splitPattern.split(string, 3);
		switch (parts.length)
		{
			case 3:
				return new LocaleKey(new Locale(parts[0], parts[1], parts[2]));
			case 2:
				return new LocaleKey(new Locale(parts[0], parts[1]));
			case 1:
				return new LocaleKey(new Locale(parts[0]));
		}
		return new LocaleKey(new Locale(string));
	}
};
