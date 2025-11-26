package hxc.connectors.database.mysql;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import hxc.connectors.database.Column;
import hxc.utils.reflection.FieldInfo;

public class ColumnInfo
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
    @SuppressWarnings("unused") // see the fix-me line below to determine if we may actually need this variable
	private TableInfo tableInfo;
	private FieldInfo fieldInfo;
	private String name;
	private String fieldName;
	private boolean primaryKey;
	private boolean nullable;
	private boolean readonly;
	private int maxLength;
	private Object defaultValue;
	private static final SimpleDateFormat defaultTimeFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

	public FieldInfo getFieldInfo()
	{
		return fieldInfo;
	}

	public String getName()
	{
		return name;
	}

	public String getFieldName()
	{
		return fieldName;
	}

	public boolean isPrimaryKey()
	{
		return primaryKey;
	}

	public boolean isNullable()
	{
		return nullable;
	}

	public boolean isReadonly()
	{
		return readonly;
	}

	public int getMaxLength()
	{
		return maxLength;
	}

	public Object getDefaultValue()
	{
		return defaultValue;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public ColumnInfo(TableInfo tableInfo, FieldInfo fieldInfo, Column columnAnnotation, Object defaultValue)
	{
		this.tableInfo = tableInfo;
		this.fieldInfo = fieldInfo;
		this.defaultValue = defaultValue;

		boolean primitiveType = fieldInfo.getField().getType().isPrimitive();
		boolean hasAnnotation = columnAnnotation != null;
		fieldName = fieldInfo.getName();

		// Infer Column Name
		name = hasAnnotation ? columnAnnotation.name() : fieldName;
		name = name == null || name.length() == 0 ? fieldInfo.getName() : name;

		// Infer PrimaryKey
		primaryKey = hasAnnotation && columnAnnotation.primaryKey();
		if (primaryKey)
            // FIXME --- should we be using THIS.tableinfo here ???
			tableInfo.getPrimaryKeyColumns().add(this);

		// Infer ReadOnly
		readonly = hasAnnotation && columnAnnotation.readonly();

		// Infer maxLength
		maxLength = hasAnnotation && columnAnnotation.maxLength() > 0 ? columnAnnotation.maxLength() : 50;

		// Infer Nullable
		if (primitiveType || primaryKey)
			nullable = false;
		else if (!hasAnnotation)
			nullable = true;
		else
			nullable = columnAnnotation.nullable();
	}

	public static ColumnInfo Create(TableInfo tableInfo, FieldInfo fieldInfo) throws SQLException
	{
		// Get Annotation
		Column columnAnnotation = fieldInfo.getAnnotation(Column.class);
		String defaultString = null;
		if (columnAnnotation != null)
		{
			if (!columnAnnotation.persistent())
				return null;
			if (!columnAnnotation.defaultValue().equals("\0"))
				defaultString = columnAnnotation.defaultValue();
		}
		boolean hasDefault = defaultString != null;
		Object defaultValue = null;

		// Filter Persistable Values
		Class<?> type = fieldInfo.getType();
		String typeName = type.isEnum() ? "int" : type.getName();

		try
		{
			switch (typeName)
			{
				case "java.lang.String":
					if (hasDefault)
						defaultValue = defaultString;
					break;
				case "byte":
				case "java.lang.Byte":
					if (hasDefault)
						defaultValue = Byte.parseByte(defaultString);
					break;
				case "short":
				case "java.lang.Short":
					if (hasDefault)
						defaultValue = Short.parseShort(defaultString);
					break;
				case "int":
				case "java.lang.Integer":
					if (hasDefault)
					{
						if (type.isEnum())
						{
							Method valueOf = type.getDeclaredMethod("valueOf", String.class);
							defaultValue = ((Enum<?>) valueOf.invoke(null, defaultString)).ordinal();
						}
						else
							defaultValue = Integer.parseInt(defaultString);
					}
					break;
				case "long":
				case "java.lang.Long":
					if (hasDefault)
						defaultValue = Long.parseLong(defaultString);
					break;
				case "java.math.BigDecimal":
					if (hasDefault)
						defaultValue = new BigDecimal(defaultString);
					break;
				case "java.util.Date":
					if (hasDefault)
						defaultValue = defaultTimeFormat.parse(defaultString);
					break;
				case "boolean":
				case "java.lang.Boolean":
					if (hasDefault)
						defaultValue = Boolean.parseBoolean(defaultString);
					break;
				case "char":
				case "java.lang.Character":
					if (hasDefault)
						defaultValue = defaultString.length() > 0 ? defaultString.charAt(0) : '\0';
					break;
				case "double":
				case "java.lang.Double":
					if (hasDefault)
						defaultValue = Double.parseDouble(defaultString);
					break;
				case "float":
				case "java.lang.Float":
					if (hasDefault)
						defaultValue = Float.parseFloat(defaultString);
					break;
				case "[B":
					if (hasDefault)
					{
						List<Byte> values = new ArrayList<Byte>();
						String[] parts = defaultString.split(",");
						for (String part : parts)
						{
							values.add(Byte.parseByte(part));
						}
						defaultValue = values.toArray(new Byte[0]);
					}
					break;
				case "java.util.UUID":
					if (hasDefault)
						defaultValue = UUID.fromString(defaultString);
					break;

				default:
					return null;
			}
		}
		catch (Throwable e)
		{
			throw new SQLException(e.getMessage(), e);
		}

		return new ColumnInfo(tableInfo, fieldInfo, columnAnnotation, defaultValue);
	}

}
