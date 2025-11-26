package hxc.utils.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClassInfo
{
	// Class
	Class<?> cls;

	// Name
	private String name;

	// Default Constructor
	Constructor<?> defaultConstructor;
	Constructor<?> embeddedConstructor;

	// Field Info
	private LinkedHashMap<String, FieldInfo> fields = new LinkedHashMap<String, FieldInfo>();
	private FieldInfo[] fieldsArray;
	private Map<String, Integer> fieldRanking = new HashMap<String, Integer>();

	// Property Info
	private LinkedHashMap<String, PropertyInfo> properties = new LinkedHashMap<String, PropertyInfo>();

	// get Name
	public String getName()
	{
		return name;
	}

	// get Fields
	public LinkedHashMap<String, FieldInfo> getFields()
	{
		return fields;
	}

	// get Properties
	public LinkedHashMap<String, PropertyInfo> getProperties()
	{
		return properties;
	}

	// Construct from Class<?>
	public ClassInfo(Class<?> type)
	{
		this.cls = type;
		this.name = type.getName();
		try
		{
			defaultConstructor = type.getDeclaredConstructor();
		}
		catch (NoSuchMethodException | SecurityException e)
		{
			Constructor<?>[] constructors = type.getDeclaredConstructors();
			if ((constructors != null) && (constructors.length > 0))
				defaultConstructor = constructors[0];
		}
		if (defaultConstructor != null)
		{
			defaultConstructor.setAccessible(true);
		}

		AddFields(type);
		fieldsArray = fields.values().toArray(new FieldInfo[0]);
		AddProperties(type);
	}

	// Recursively Add Fields
	private void AddFields(Class<?> klass)
	{
		// Add Super Class Fields First
		Class<?> superClass = klass.getSuperclass();
		if (superClass != null && !superClass.equals(Object.class))
			AddFields(superClass);

		// Get Fields in this Class Only
		Field[] fields = klass.getDeclaredFields();

		// Add to Map
		int rank = this.fields.size() + 1;
		for (Field field : fields)
		{
			// Ignore if it is Unreflective
			if (field.isAnnotationPresent(NonReflective.class))
				continue;

			// Ignore if static
			if (Modifier.isStatic(field.getModifiers()))
				continue;

			// Add Field Info
			FieldInfo fieldInfo = new FieldInfo(field);
			this.fields.put(fieldInfo.getName(), fieldInfo);
			fieldInfo.setRanking(rank++);
			fieldRanking.put(fieldInfo.getName().toLowerCase(), fieldInfo.getRanking());
		}

	}

	// Recursively Add getters and setters
	private void AddProperties(Class<?> klass)
	{

		// Add Super Class Properties First
		Class<?> superClass = klass.getSuperclass();
		if (superClass != null && !superClass.equals(Object.class))
			AddProperties(superClass);

		// Get methods in this Class Only
		Method[] methods = klass.getDeclaredMethods();

		// Add to Map
		int index = properties.size() + 1000;
		for (Method method : methods)
		{
			// Ignore if it is Unreflective
			if (method.isAnnotationPresent(NonReflective.class))
				continue;

			// Get name which must start with either get or set
			method.setAccessible(true);
			String name = method.getName();
			boolean isSetter = false;
			if (name.startsWith("set"))
				isSetter = true;
			else if (!(name.startsWith("get") || (name.startsWith("is"))))
				continue;
			name = name.substring(name.startsWith("is") ? 2 : 3);

			// Must be non Generic
			TypeVariable<Method>[] typeVariables = method.getTypeParameters();
			if (typeVariables.length > 0)
				continue;

			// Setters must have 1 argument and return void
			Class<?> returnType = method.getReturnType();
			if (isSetter && !returnType.equals(Void.TYPE))
				continue;
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (isSetter && parameterTypes.length != 1)
				continue;

			// Getters must have 0 arguments and return a value;
			if (!isSetter && returnType.equals(Void.TYPE))
				continue;
			if (!isSetter && parameterTypes.length != 0)
				continue;

			// Get / Create PropertyInfo
			PropertyInfo propertyInfo = properties.get(name);
			if (propertyInfo == null)
			{
				propertyInfo = new PropertyInfo(name);
				Integer ranking = fieldRanking.get(name.toLowerCase());
				propertyInfo.setRanking(ranking == null ? index++ : ranking);
				properties.put(name, propertyInfo);
			}

			// Update PropertyInfo
			if (isSetter)
				propertyInfo.setSetterMethod(method);
			else
				propertyInfo.setGetterMethod(method);

		}

		List<PropertyInfo> propList = new ArrayList<PropertyInfo>(properties.values());
		Collections.sort(propList);
		properties.clear();
		for (PropertyInfo property : propList)
		{
			properties.put(property.getName(), property);
		}

	}

	// Create an instance with default constructor
	public <T> T newInstance() throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		if (defaultConstructor != null)
			return (T) defaultConstructor.newInstance();
		else
			throw new NoSuchMethodException(getName());

	}

	// Create an instance within a parent object
	public <T> T newInstance(Object parentObject) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		if (embeddedConstructor == null)
		{
			embeddedConstructor = cls.getDeclaredConstructor(parentObject.getClass());
		}
		return (T) embeddedConstructor.newInstance(parentObject);
	}

	// Get Field/Property
	public Object get(Object instance, String name) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		if (properties.containsKey(name))
			return properties.get(name).get(instance);
		else
			return fields.get(name).get(instance);
	}

	// Set Field/Property
	public void set(Object instance, String name, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		if (properties.containsKey(name))
			properties.get(name).set(instance, value);
		else
			fields.get(name).set(instance, value);
	}

	@Override
	public String toString()
	{
		return name;
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
	{
		return cls.getAnnotation(annotationClass);
	}

	public FieldInfo getField(int index)
	{
		return fieldsArray[index];
	}

}
