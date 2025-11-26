package hxc.services.ecds.util;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public class LocaleCustomisationMap extends TreeMap<LocaleKey, LocaleCustomisation>
{
	private static final long serialVersionUID = 2568049972435524980L;

	public LocaleCustomisationMap()
	{
		super();
	}

	public LocaleCustomisationMap(SortedMap<LocaleKey, ? extends LocaleCustomisation> map)
	{
		super(map);
	}

	public LocaleCustomisationMap(Map<? extends LocaleKey, ? extends LocaleCustomisation> map)
	{
		super(map);
	}

	public LocaleCustomisation put(LocaleCustomisation localeCustomisation)
	{
		return this.put(localeCustomisation.getLocaleKey(), localeCustomisation);
	}

	public LocaleCustomisation put(ILocaleCustomisationConfig localeCustomisationConfig)
	{
		Objects.requireNonNull(localeCustomisationConfig, "localeCustomisationConfig may not be null");
		LocaleCustomisation localeCustomisation = localeCustomisationConfig.toCustomisation();
		return this.put(localeCustomisation.getLocaleKey(), localeCustomisation);
	}

	public LocaleCustomisation put(String localeString)
	{
		return this.put(LocaleKey.valueOf(localeString));
	}

	public LocaleCustomisation put(LocaleKey localeKey)
	{
		return this.put(localeKey, new LocaleCustomisation(localeKey));
	}

	public boolean containsKey(Locale locale)
	{
		return this.containsKey(new LocaleKey(locale));
	}

	public static LocaleCustomisationMap valueOf(ILocaleCustomisationConfig[] array)
	{
		LocaleCustomisationMap result = new LocaleCustomisationMap();
		for (ILocaleCustomisationConfig item : array)
		{
			result.put(item);
		}
		return result;
	}

	public static LocaleCustomisationMap valueOf(LocaleCustomisation[] array)
	{
		LocaleCustomisationMap result = new LocaleCustomisationMap();
		for (LocaleCustomisation item : array)
		{
			result.put(item);
		}
		return result;
	}

	public LocaleCustomisation[] valueArray()
	{
		Collection<LocaleCustomisation> values = this.values();
		LocaleCustomisation[] result = new LocaleCustomisation[values.size()];
		int i = 0;
		for (LocaleCustomisation value : values)
		{
			result[i++] = value;
		}
		return result;
	}

	/*
	 * public < T extends ILocaleCustomisationConfig > T[] toConfigArray( Class< T > type ) throws Exception { Constructor< T > constructor = type.getConstructor( LocaleCustomisation.class );
	 * Collection< LocaleCustomisation > values = this.values(); T[] result = (T[]) Array.newInstance( type, values.size() ); int i = 0; for ( LocaleCustomisation value : values ) { result[ i++ ] =
	 * constructor.newInstance( value ); } return result; }
	 */
}
