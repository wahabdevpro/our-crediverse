package hxc.services.ecds.rest.batch;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

public abstract class CsvExportProcessor<T>
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final char CSV_DELIMITER = ',';
	private static final char CSV_QUOTE = '"';
	private static final char[] CSV_SEARCH_CHARS = new char[] { CSV_DELIMITER, CSV_QUOTE, '\r', '\n' };
	private static final String TIME_FORMAT = "'T'HHmmss";
	private static final String DATE_FORMAT = "yyyyMMdd'T'HHmmss";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected boolean first = false;
	protected boolean withVerb = true;
	protected String[] headings;
	protected CsvWriter writer;

	private SimpleDateFormat timeFormatter = new SimpleDateFormat(TIME_FORMAT);

	private static final String VERB = "verb";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public CsvExportProcessor(String[] headings, int first)
	{
		this(headings, first, true);
	}

	public CsvExportProcessor(String[] headings, int first, boolean withVerb)
	{
		this.first = first <= 0;
		this.withVerb = withVerb;
		if (withVerb)
		{
			this.headings = new String[headings.length + 1];
			this.headings[0] = VERB;
			System.arraycopy(headings, 0, this.headings, 1, headings.length);
		}
		else
		{
			this.headings = new String[headings.length];
			System.arraycopy(headings, 0, this.headings, 0, headings.length);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public void addToCsvWriter(CsvWriter writer, List<T> records) throws IOException
	{
		try
		{
			// Write the headings for the first batch
			if (first)
			{
				writer.writeHeaders(headings);
				first = false;
			}
			else
			{
				writer.writeHeadersToString(headings);
			}

			// Add Writer Records
			for (T record : records)
			{
				if (this.withVerb) writer.addValue(VERB, "verify");
				write(record);
				writer.writeValuesToRow();
			}
		}
		finally
		{
			writer.close();
			writer = null;
		}
	}

	public Writer addToBacking(Writer backing, List<T> records) throws IOException
	{
		// Flush yourself ...
		CsvWriterSettings settings = new CsvWriterSettings();
		writer = new CsvWriter(backing, settings);
		this.addToCsvWriter(writer, records);
		return backing;
	}

	public OutputStream addToBacking(OutputStream backing, List<T> records) throws IOException
	{
		// Flush yourself ...
		CsvWriterSettings settings = new CsvWriterSettings();
		writer = new CsvWriter(backing, settings);
		this.addToCsvWriter(writer, records);
		return backing;
	}

	public String add(List<T> records) throws IOException
	{
		try (StringWriter sw = new StringWriter(records.size() * 200))
		{
			this.addToBacking(sw, records);
			sw.flush();
			return sw.toString();
		}
	}

	/*
	public String add(List<T> records) throws IOException
	{
		try (StringWriter sw = new StringWriter(records.size() * 200))
		{
			// Create Writer
			CsvWriterSettings settings = new CsvWriterSettings();
			writer = new CsvWriter(sw, settings);

			// Write the headings for the first batch
			if (first)
			{
				writer.writeHeaders(headings);
				first = false;
			}
			else
			{
				writer.writeHeadersToString(headings);
			}

			// Add Writer Records
			for (T record : records)
			{
				if (this.withVerb) writer.addValue(VERB, "verify");
				write(record);
				writer.writeValuesToRow();
			}

			writer.close();
			writer = null;
			sw.flush();
			return sw.toString();
		}
	}
	*/

	protected abstract void write(T record);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Put Methods
	//
	// /////////////////////////////////

	public void put(String headerName, Integer value)
	{
		writer.addValue(headerName, toText(value));
	}
	
	public void put(String headerName, Double value)
	{
		writer.addValue(headerName, toText(value));
	}

	public void put(String headerName, Long value)
	{
		writer.addValue(headerName, toText(value));
	}

	public void put(String headerName, String value)
	{
		writer.addValue(headerName, toText(value));
	}

	public void put(String headerName, Boolean value)
	{
		writer.addValue(headerName, toText(value));
	}

	public void put(String headerName, BigDecimal value)
	{
		writer.addValue(headerName, toText(value));
	}

	public void putTime(String headerName, Date value)
	{
		if (value == null)
			return;
		writer.addValue(headerName, timeFormatter.format(value));
	}

	public void put(String headerName, Date value)
	{
		writer.addValue(headerName, toText(value));
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// To Methods
	//
	// /////////////////////////////////

	public static String toText(String value)
	{
		// Just Return null or empty strings
		if (value == null || value.isEmpty())
			return "";

		// Test if the string must be escaped
		boolean mustBeEscaped = false;
		for (char ch : CSV_SEARCH_CHARS)
		{
			if (value.indexOf(ch) < 0)
				continue;
			mustBeEscaped = true;
			break;
		}
		if (!mustBeEscaped)
			return value;

		// Escape the string
		StringBuilder sb = new StringBuilder(value.length() + 10);
		sb.append(CSV_QUOTE);
		int fromIndex = 0;
		while (true)
		{
			int p = value.indexOf(CSV_QUOTE, fromIndex);
			if (p < 0)
			{
				sb.append(value.substring(fromIndex, value.length()));
				break;
			}
			else
			{
				sb.append(value.substring(fromIndex, p + 1));
				sb.append(CSV_QUOTE);
				fromIndex = p + 1;
			}
		}
		sb.append(CSV_QUOTE);

		return sb.toString();
	}

	public static String toText(Date value)
	{
		if (value == null)
			return "";

		return new SimpleDateFormat(DATE_FORMAT).format(value);
	}

	public static String toText(Integer value)
	{
		return value == null ? "" : value.toString();
	}
	
	public static String toText(Double value)
	{
		return value == null ? "" : value.toString();
	}

	public static String toText(Long value)
	{
		return value == null ? "" : value.toString();
	}

	public static String toText(BigDecimal value)
	{
		if (value == null)
			return "";
		else if (value.signum() == 0)
			return "0.00";
		else
			return value.stripTrailingZeros().toPlainString();
	}

	public static String toText(Boolean value)
	{
		if (value == null)
			return "";
		else if (value)
			return "1";
		else
			return "0";
	}

}
