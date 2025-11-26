package hxc.utils.reflection;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;

/**
 * 
 * @author AndriesdB
 * 
 */
public class ReflectionHelper
{
	// ClassInfo Cache
	private static LinkedHashMap<String, ClassInfo> cache = new LinkedHashMap<String, ClassInfo>();

	// Create from Type
	public static ClassInfo getClassInfo(Class<?> klass)
	{
		// Test if ClassInfo is available in Cache
		String className = klass.getName();
		ClassInfo result = cache.get(className);
		if (result != null)
			return result;

		// Create new ClassInfo
		result = new ClassInfo(klass);

		// Add Thread Safely to cache
		synchronized (cache)
		{
			if (cache.containsKey(className))
				return cache.get(className);
			cache.put(className, result);
		}

		return result;

	}

	public static Object valueOf(Class<?> clazz, String arg)
	{
		Object result = null;
		try
		{

			if (clazz.isPrimitive())
			{
				if (clazz.getName().equals("int"))
				{
					result = Integer.parseInt(arg);
				}
				else if (clazz.getName().equals("long"))
				{
					result = Long.parseLong(arg);
				}
				else if (clazz.getName().equals("byte"))
				{
					result = Byte.parseByte(arg);
				}
				else if (clazz.getName().equals("float"))
				{
					result = Float.parseFloat(arg);
				}
				else if (clazz.getName().equals("double"))
				{
					result = Double.parseDouble(arg);
				}
			}
			else
			{
				result = clazz.cast(clazz.getDeclaredMethod("valueOf", String.class).invoke(null, arg));
			}
		}
		catch (NoSuchMethodException e)
		{
			throw new IllegalArgumentException(e);
		}
		catch (IllegalAccessException e)
		{
			throw new IllegalArgumentException(e);
		}
		catch (InvocationTargetException e)
		{
			throw new IllegalArgumentException(e);
		}

		return result;
	}

}
