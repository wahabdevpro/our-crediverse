package hxc.services.ecds.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

public class LocaleCustomisation
{
	private LocaleKey localeKey;
	private DecimalFormat currencyFormat;

	/*
	 * public LocaleCustomisation( LocaleCustomisationConfig localeCustomisationConfig ) { LocaleKey localeKey = new LocaleKey( localeCustomisationConfig.getLocaleString() ); DecimalFormat
	 * currencyFormat = ( DecimalFormat ) NumberFormat.getCurrencyInstance( localeKey.getLocale() );
	 * 
	 * currencyFormat.applyPattern( localeCustomisationConfig.getCurrencyFormatPattern() );
	 * 
	 * DecimalFormatSymbols currencyFormatSymbols = currencyFormat.getDecimalFormatSymbols(); currencyFormatSymbols.setCurrencySymbol( localeCustomisationConfig.getCurrencySymbol() );
	 * currencyFormatSymbols.setGroupingSeparator( localeCustomisationConfig.getCurrencyGroupingSeparator() ); currencyFormatSymbols.setDecimalSeparator(
	 * localeCustomisationConfig.getCurrencyDecimalSeparator() ); currencyFormat.setDecimalFormatSymbols( currencyFormatSymbols );
	 * 
	 * this.localeKey = localeKey; this.currencyFormat = currencyFormat; }
	 */

	public LocaleCustomisation(LocaleKey localeKey)
	{
		this.localeKey = localeKey;
		this.currencyFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(localeKey.getLocale());
	}

	public LocaleCustomisation(LocaleKey localeKey, DecimalFormat currencyFormat)
	{
		this.localeKey = localeKey;
		this.currencyFormat = currencyFormat;
	}

	public LocaleCustomisation(LocaleKey localeKey, DecimalFormat currencyFormat, DecimalFormatSymbols currencyFormatSymbols)
	{
		this.localeKey = localeKey;
		this.currencyFormat = currencyFormat;
		this.currencyFormat.setDecimalFormatSymbols(currencyFormatSymbols);
	}

	public LocaleKey getLocaleKey()
	{
		return this.localeKey;
	}

	public DecimalFormat getCurrencyFormat()
	{
		return this.currencyFormat;
	}

	/*
	 * public LocaleCustomisationConfig toConfig() { return new LocaleCustomisationConfig( this ); }
	 */
}
