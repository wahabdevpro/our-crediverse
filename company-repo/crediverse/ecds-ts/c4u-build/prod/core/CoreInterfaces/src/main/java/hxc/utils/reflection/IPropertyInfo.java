package hxc.utils.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface IPropertyInfo
{

	// get Name
	public abstract String getName();

	// get getterMethod
	public abstract Method getGetterMethod();

	// set getterMethod
	public abstract void setGetterMethod(Method getterMethod);

	// get setterMethod
	public abstract Method getSetterMethod();

	// set setterMethod
	public abstract void setSetterMethod(Method setterMethod);

	public abstract Object get(Object instance) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	public abstract void set(Object instance, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	public abstract void setUsingString(Object instance, String sValue) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

}