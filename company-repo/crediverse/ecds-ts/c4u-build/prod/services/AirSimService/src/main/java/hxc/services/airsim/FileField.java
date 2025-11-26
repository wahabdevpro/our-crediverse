package hxc.services.airsim;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import hxc.connectors.file.CsvField;
import hxc.utils.reflection.ClassInfo;
import hxc.utils.reflection.FieldInfo;
import hxc.utils.reflection.ReflectionHelper;

public class FileField
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	public int index;
	public String type;
	public String format;
	public boolean optional;
	public FieldInfo fieldInfo;
	private SimpleDateFormat sdf;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public static Map<Integer, FileField> getFieldFormats(Class<?> cls)
	{
		Map<Integer, FileField> result = new HashMap<Integer, FileField>();

		ClassInfo classInfo = ReflectionHelper.getClassInfo(cls);
		for (FieldInfo field : classInfo.getFields().values())
		{
			CsvField attribute = field.getAnnotation(CsvField.class);
			if (attribute == null)
				continue;
			FileField fileField = new FileField();
			fileField.index = attribute.column();
			fileField.format = attribute.format();
			fileField.optional = attribute.optional();
			fileField.fieldInfo = field;
			fileField.type = field.getField().getType().getSimpleName();
			result.put(fileField.index, fileField);
		}

		return result;
	}

	public static String getCsv(Map<Integer, FileField> fields, Object instance)
	{
		StringBuilder sb = new StringBuilder(200);
		for (int index = 0; index < fields.size(); index++)
		{
			if (index != 0)
				sb.append(',');
			FileField fi = fields.get(index);
			if (fi == null)
				continue;

			Object value = null;
			try
			{
				value = fi.fieldInfo.get(instance);
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				value = null;
			}
			if (value == null)
				continue;

			switch (fi.type)
			{
				case "String":
					sb.append((String) value);
					break;

				case "boolean":
					sb.append((boolean) value ? "1" : "0");
					break;

				case "short":
					sb.append((short) value);
					break;

				case "byte":
					sb.append((byte) value);
					break;

				case "int":
					sb.append((int) value);
					break;

				case "double":
					sb.append((double) value);
					break;

				case "Date":
					if (fi.sdf == null)
						fi.sdf = new SimpleDateFormat(fi.format);
					sb.append(fi.sdf.format((Date) value));
					break;

				default:
					throw new NumberFormatException();
			}
		}

		sb.append('\n');
		return sb.toString();
	}
}
