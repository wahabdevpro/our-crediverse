package hxc.utils.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PropertyInfo implements IPropertyInfo, Comparable
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	// Property Name
	private String name;

	// Getter Method
	private Method getterMethod;

	// Setter Method
	private Method setterMethod;

	// Ranking
	private int ranking;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// get Name
	@Override
	public String getName()
	{
		return name;
	}

	// get getterMethod
	@Override
	public Method getGetterMethod()
	{
		return getterMethod;
	}

	// set getterMethod
	@Override
	public void setGetterMethod(Method getterMethod)
	{
		this.getterMethod = getterMethod;
	}

	// get setterMethod
	@Override
	public Method getSetterMethod()
	{
		return setterMethod;
	}

	// set setterMethod
	@Override
	public void setSetterMethod(Method setterMethod)
	{
		this.setterMethod = setterMethod;
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
	// Constructor
	//
	// /////////////////////////////////
	// Construct from name
	public PropertyInfo(String name)
	{
		this.name = name;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public Object get(Object instance) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		return getterMethod.invoke(instance);
	}

	@Override
	public void set(Object instance, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		setterMethod.invoke(instance, value);
	}

	@Override
	public void setUsingString(Object instance, String sValue) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Class<?> parms[] = setterMethod.getParameterTypes();
		Object value = ReflectionHelper.valueOf(parms[0], sValue);
		setterMethod.invoke(instance, value);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Comparable Implementation
	//
	// /////////////////////////////////

	@Override
	public int compareTo(Object o)
	{
		if (o instanceof PropertyInfo)
			return Integer.compare(ranking, ((PropertyInfo) o).ranking);
		else
			return 0;
	}

}
