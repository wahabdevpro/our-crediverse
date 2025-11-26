package hxc.services.transactions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.RequestHeader;

import hxc.connectors.IInteraction;
import hxc.utils.reflection.ClassInfo;
import hxc.utils.reflection.FieldInfo;
import hxc.utils.reflection.NonReflective;
import hxc.utils.reflection.ReflectionHelper;
import hxc.utils.string.StringUtils;

public class CsvCdr extends CdrBase
{
	final static Logger logger = LoggerFactory.getLogger(CsvCdr.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	@NonReflective
	private final ClassInfo classInfo = ReflectionHelper.getClassInfo(this.getClass());
	@NonReflective
	private boolean written = false;
	@NonReflective
	private final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSS");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public CsvCdr()
	{
		super();
	}

	public CsvCdr(RequestHeader request, String transactionNumber)
	{
		super(request, transactionNumber);
	}

	public CsvCdr(IInteraction interaction, String transactionNumber)
	{
		super(interaction, transactionNumber);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// CdrBase Implementation
	//
	// /////////////////////////////////

	/**
	 * Writes CDR content to a <code>FileOutputStream</code>
	 */
	@Override
	public void Write(FileOutputStream outputStream)
	{
		// Exit if it has already been written
		if (written)
			return;

		// Test if the Output Stream is open
		if (outputStream == null)
		{
			if (logger != null)
				logger.error("CDR File Closed");
			return;
		}

		// Build the stream up
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
		StringBuilder sb = new StringBuilder(1000);
		boolean first = true;
		for (FieldInfo fieldInfo : classInfo.getFields().values())
		{
			// Add preceding comma
			if (first)
				first = false;
			else
				sb.append(',');

			// Obtain field's value
			Object value = null;
			try
			{
				value = fieldInfo.get(this);
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
			}

			// Convert to a string and add to StringBuilder
			String text = null;
			if (value == null)
			{
				text = "";
			}
			else
			{
				Class<?> type = value.getClass();
				switch (type.getName())
				{
					case "java.lang.Boolean":
						text = (boolean) value ? "1" : "0";
						break;

					case "java.util.Date":
						text = timeFormat.format((Date) value);
						break;

					default:
						text = value.toString();
				}
			}

			// Escape the string
			if (text.length() > 0)
				text = StringUtils.escapeXml(text).replace(",", "&#044;").replace("\r", "&#013;").replace("\n", "&#010;").replace("\t", "&#009;");
			sb.append(text);
		}

		// Write to file
		try
		{
			sb.append('\n');
			outputStreamWriter.write(sb.toString());
			outputStreamWriter.flush();
		}
		catch (IOException ex)
		{
			logger.error("Failed to write Csv CDR", ex);
			return;
		}

		// Mark as written
		written = true;
	}

}
