package hxc.services.ecds.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import hxc.ecds.protocol.rest.config.ReportingConfig;

public class CustomCurrencyFormatter
{
	private String currencySymbol = "";
	private String thousandSeparator = "";
	private String decimalSeparator = "";

	public CustomCurrencyFormatter(String currencySymbol, String thousandSeparator, String decimalSeparator) {
		this.currencySymbol = currencySymbol;
		this.thousandSeparator = thousandSeparator;
		this.decimalSeparator = decimalSeparator;
	}

	public static CustomCurrencyFormatter fromReportingConfig(ReportingConfig reportingConfig)
	{
		return new CustomCurrencyFormatter(
			reportingConfig.getSmsCurrency(),
			reportingConfig.getSmsThousandSeparator(),
			reportingConfig.getSmsDecimalSeparator()
		);
	}

	public String format(BigDecimal amount) {
		NumberFormat df = NumberFormat.getCurrencyInstance();
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setCurrencySymbol(this.currencySymbol);

		char thousandSeparator = '\u0000';
		if (this.thousandSeparator.length() != 0) thousandSeparator = this.thousandSeparator.charAt(0);

		char decimalSeparator = '\u0000';
		if (this.decimalSeparator.length() != 0) decimalSeparator = this.decimalSeparator.charAt(0);

		dfs.setGroupingSeparator(thousandSeparator);
		dfs.setMonetaryDecimalSeparator(decimalSeparator);
		((DecimalFormat) df).setDecimalFormatSymbols(dfs);
        /*
         * FIXME
         *
         * Setting number of decimal places to 0.
         *
         * This should be getting it's information from the C4U locale setting instead of hardcoding here
         *
         */
		df.setMaximumFractionDigits(0);
		df.setMinimumFractionDigits(0);

		return df.format(amount);
	}
}
