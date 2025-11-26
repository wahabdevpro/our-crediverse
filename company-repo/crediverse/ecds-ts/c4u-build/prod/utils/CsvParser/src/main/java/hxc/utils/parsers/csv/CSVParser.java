package hxc.utils.parsers.csv;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import hxc.connectors.file.CsvField;
import hxc.connectors.file.FileDTO;
import hxc.connectors.file.IFileProcessorHandler;
import hxc.utils.reflection.ClassInfo;
import hxc.utils.reflection.FieldInfo;
import hxc.utils.reflection.ReflectionHelper;

public class CSVParser
{

	private IFileProcessorHandler handler;
	private char delimiter = ',';

	public CSVParser(IFileProcessorHandler handler)
	{
		this.handler = handler;
	}

	public void setDelimiter(char delimiter)
	{
		this.delimiter = delimiter;
	}

	public void parseAll(Class<? extends FileDTO> dto_class, String filename, byte[] data, long startRecord)
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data)));
		String csvRecord;

		FileDTO dto = null;
		classInfo = ReflectionHelper.getClassInfo(dto_class);
		fields = classInfo.getFields();

		Date fileTime = new Date();
		long recordNum = 0;
		try
		{
			while ((csvRecord = reader.readLine()) != null)
			{
				// TNP v3 markers
				if (csvRecord.startsWith("Version: "))
					continue;
				else if (csvRecord.equals("=========="))
					continue;

				try
				{
					dto = dto_class.newInstance();
				}
				catch (InstantiationException | IllegalAccessException exc)
				{
					if (handler != null)
						handler.dispatchError(exc);
					return;
				}

				dto.filename = filename;
				dto.fileTime = fileTime;
				dto.recordNo = recordNum;
				recordNum++;

				if (startRecord != 0 && recordNum < startRecord + 1)
				{
					continue;
				}

				parse(dto, csvRecord);
			}
		}
		catch (IOException exc)
		{
			if (handler != null)
			{
				handler.dispatchError(exc);
				handler.processingFailed(dto);
			}

			try
			{
				reader.close();
			}
			catch (IOException e)
			{
				return;
			}
			return;
		}

		try
		{
			reader.close();
		}
		catch (IOException e)
		{
		}
	}

	private ClassInfo classInfo;
	private LinkedHashMap<String, FieldInfo> fields;

	public void parse(FileDTO dto, String line)
	{
		if (classInfo == null)
		{
			classInfo = ReflectionHelper.getClassInfo(dto.getClass());
			fields = classInfo.getFields();
		}

		String record = null;
		int fieldSize = fields.size() - 4;

		for (FieldInfo field : fields.values())
		{
			if (!field.isAnnotationPresent(CsvField.class))
			{
				continue;
			}
			CsvField annotation = field.getAnnotation(CsvField.class);

			int index = line.indexOf(delimiter);
			if (line.indexOf('\u0022') == 0)
			{
				line = line.substring(1, line.length() - 1);
				if (annotation.column() == fieldSize)
					index = 0;
				else
					index = line.indexOf('"') + 1;
			}

			if (index > 0 && annotation.column() == fieldSize)
			{
				if (handler != null)
					handler.malformedRecord(dto, new Exception("Malformed Record " + dto.recordNo + ". Contains too many number of columns."));
				return;
			}

			// record = line.substring(0, (index > 0) ? index : (annotation.column() == fieldSize ? line.length() : 0));
			
			if (index == 0)
				record = "";
			else
				record = line.substring(0, (index > 0) ? index : line.length());
			
			line = (index >= 0) ? line.substring(index + 1) : "";

			if (record.length() == 0)
			{

				if (index < 0 && annotation.column() < fieldSize)
				{
					if (annotation.optional())
						continue; // TNP3
					if (handler != null)
						handler.malformedRecord(dto, new Exception("Malformed Record " + dto.recordNo + ". Contains too few number of columns."));
					return;
				}

				if (annotation.optional())
				{
					continue;
				}
			}

			Object value = null;
			try
			{
				switch (field.getFieldType())
				{
					case Boolean:
						try
						{
							if (Integer.parseInt(record) == 1)
							{
								value = true;
							}
						}
						catch (Exception exc)
						{
							value = Boolean.parseBoolean(record);
						}
						break;

					case Byte:
						value = Byte.parseByte(record);
						break;

					case Character:
						value = record.charAt(0);
						break;

					case Date:
						DateFormat format = new SimpleDateFormat(annotation.format());
						try
						{
							value = format.parse(record);
						}
						catch (ParseException exc)
						{
							value = null;
						}
						break;

					case Double:
						value = Double.parseDouble(record);
						break;

					case Float:
						value = Float.parseFloat(record);
						break;

					case Integer:
						value = Integer.parseInt(record);
						break;

					case Long:
						value = Long.parseLong(record);
						break;

					case Short:
						value = Short.parseShort(record);
						break;

					case String:
						value = record;
						break;

					default:
						value = null;
						break;
				}
			}
			catch (Exception exc)
			{
				value = null;
			}

			if (value != null)
			{
				try
				{
					classInfo.set(dto, field.getName(), value);
				}
				catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
				{
					if (handler != null)
						handler.dispatchError(new Exception("Could not set a record field: " + field.getName() + " for record: " + dto.recordNo + " of file: " + dto.filename));
				}
			}
		}

		if (handler != null)
			handler.dispatchDTO(dto);
	}

}
