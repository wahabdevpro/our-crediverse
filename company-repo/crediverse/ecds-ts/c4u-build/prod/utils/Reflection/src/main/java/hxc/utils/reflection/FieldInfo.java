package hxc.utils.reflection;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.UUID;

public class FieldInfo
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	// Reflected field
	private Field field;

	// Name
	private String name;

	// Field Type
	private FieldTypes fieldType;

	// Ranking
	private int ranking;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// get Field
	public Field getField()
	{
		return field;
	}

	// get Name
	public String getName()
	{
		return name;
	}

	// get Type
	public Class<?> getType()
	{
		return field.getType();
	}

	// get Field Type
	public FieldTypes getFieldType()
	{
		return fieldType;
	}

	public int getRanking()
	{
		return ranking;
	}

	public void setRanking(int ranking)
	{
		this.ranking = ranking;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	// Construct from Field
	public FieldInfo(Field field)
	{
		// Save Field
		this.field = field;

		// Make it accessible
		this.field.setAccessible(true);

		// Save Name
		this.name = field.getName();

		// Field Type
		this.fieldType = getFieldType(field.getType());

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get/Set Values
	//
	// /////////////////////////////////

	// get Value
	public Object get(Object instance) throws IllegalArgumentException, IllegalAccessException
	{
		return field.get(instance);
	}

	// set Value
	public void set(Object instance, Object value) throws IllegalArgumentException, IllegalAccessException
	{
		field.set(instance, value);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass)
	{
		return field.isAnnotationPresent(annotationClass);
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
	{
		return field.getAnnotation(annotationClass);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	@Override
	public String toString()
	{
		return name;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Field Type Enumeration
	//
	// /////////////////////////////////
	public enum FieldTypes
	{
		Null, String, Byte, Short, Integer, Long, BigDecimal, Date, Boolean, Character, Double, Float, ByteArray, UUID, Enum, Other
	}

	public FieldTypes getFieldType(Object object)
	{
		if (object == null)
			return FieldTypes.Null;
		else
			return getFieldType(object.getClass());
	}

	public FieldTypes getFieldType(Class<?> type)
	{
		if (type.isEnum())
			return FieldTypes.Enum;

		switch (type.getName())
		{
			case "java.lang.String":
				return FieldTypes.String;

			case "byte":
			case "java.lang.Byte":
				return FieldTypes.Byte;

			case "short":
			case "java.lang.Short":
				return FieldTypes.Short;

			case "int":
			case "java.lang.Integer":
				return FieldTypes.Integer;

			case "long":
			case "java.lang.Long":
				return FieldTypes.Long;

			case "java.math.BigDecimal":
				return FieldTypes.BigDecimal;

			case "java.util.Date":
				return FieldTypes.Date;

			case "boolean":
			case "java.lang.Boolean":
				return FieldTypes.Boolean;

			case "char":
			case "java.lang.Character":
				return FieldTypes.Character;

			case "double":
			case "java.lang.Double":
				return FieldTypes.Double;

			case "float":
			case "java.lang.Float":
				return FieldTypes.Float;

			case "[B":
				return FieldTypes.ByteArray;

			case "java.util.UUID":
				return FieldTypes.UUID;

			default:
				return FieldTypes.Other;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get/Set bytes
	//
	// /////////////////////////////////

	// Get bytes
	public byte[] getBytes(Object instance) throws IllegalArgumentException, IllegalAccessException, UnsupportedEncodingException
	{
		Object value = get(instance);
		if (value == null)
			return null;

		switch (fieldType)
		{
			case String:
				return ((String) value).getBytes("UTF-8");

			case Byte:
				return new byte[] { (byte) value };

			case Short:
				return ByteBuffer.allocate(2).putShort((short) value).array();

			case Integer:
				return ByteBuffer.allocate(4).putInt((int) value).array();

			case Long:
				return ByteBuffer.allocate(8).putLong((long) value).array();

			case BigDecimal:
			{
				BigDecimal bd = (BigDecimal) value;
				byte[] scale = ByteBuffer.allocate(4).putInt(bd.scale()).array();
				byte[] unscaled = bd.unscaledValue().toByteArray();
				byte[] result = new byte[scale.length + unscaled.length];
				System.arraycopy(scale, 0, result, 0, scale.length);
				System.arraycopy(unscaled, 0, result, scale.length, unscaled.length);
				return result;
			}

			case Date:
				return ByteBuffer.allocate(8).putLong(((Date) value).getTime()).array();

			case Boolean:
				return (Boolean) value ? new byte[] { 1 } : new byte[] { 0 };

			case Character:
				return new String(new char[] { (char) value }).getBytes("UTF-8");

			case Double:
				return ByteBuffer.allocate(8).putDouble((double) value).array();

			case Float:
				return ByteBuffer.allocate(4).putFloat((float) value).array();

			case ByteArray:
				return (byte[]) value;

			case UUID:
			{
				UUID uuid = (UUID) value;
				ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
				bb.putLong(uuid.getMostSignificantBits());
				bb.putLong(uuid.getLeastSignificantBits());
				return bb.array();
			}

			case Enum:
				return ByteBuffer.allocate(4).putInt(((Enum<?>) value).ordinal()).array();

			default:
				return null;
		}

	}

	public void setBytes(Object instance, byte[] bytes) throws IllegalArgumentException, IllegalAccessException, UnsupportedEncodingException
	{
		if (bytes == null)
		{
			set(instance, null);
			return;
		}

		switch (fieldType)
		{
			case String:
				set(instance, new String(bytes, "UTF-8"));
				break;

			case Byte:
				set(instance, bytes[0]);
				break;

			case Short:
				set(instance, ByteBuffer.wrap(bytes).getShort());
				break;

			case Integer:
				set(instance, ByteBuffer.wrap(bytes).getInt());
				break;

			case Long:
				set(instance, ByteBuffer.wrap(bytes).getLong());
				break;

			case BigDecimal:
			{
				int scale = ByteBuffer.wrap(bytes).getInt();
				byte[] buffer = new byte[bytes.length - 4];
				System.arraycopy(bytes, 4, buffer, 0, buffer.length);
				BigInteger unscaled = new BigInteger(buffer);
				set(instance, new BigDecimal(unscaled, scale));
			}
				break;

			case Date:
				set(instance, new Date(ByteBuffer.wrap(bytes).getLong()));
				break;

			case Boolean:
				set(instance, bytes[0] != 0);
				break;

			case Character:
			{
				String text = new String(bytes, "UTF-8");
				set(instance, text == null || text.length() == 0 ? '\0' : text.charAt(0));
			}
				break;

			case Double:
				set(instance, ByteBuffer.wrap(bytes).getDouble());
				break;

			case Float:
				set(instance, ByteBuffer.wrap(bytes).getFloat());
				break;

			case ByteArray:
				set(instance, bytes);
				break;

			case UUID:
			{
				ByteBuffer bb = ByteBuffer.wrap(bytes);
				long mostSigBits = bb.getLong();
				long leastSigBits = bb.getLong();
				set(instance, new UUID(mostSigBits, leastSigBits));
			}

			case Enum:
			{
				int ordinal = ByteBuffer.wrap(bytes).getInt();
				set(instance, field.getType().getEnumConstants()[ordinal]);
			}
				break;

			default:
				break;

		}
	}
}
