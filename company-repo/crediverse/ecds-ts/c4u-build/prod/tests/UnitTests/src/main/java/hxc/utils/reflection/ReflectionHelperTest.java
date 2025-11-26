package hxc.utils.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.LinkedHashMap;

import org.junit.Test;

import hxc.utils.calendar.DateTime;

public class ReflectionHelperTest
{

	@Test
	public void testGetClassInfo()
	{
		// Obtain ClassInfo
		ClassInfo classInfo = ReflectionHelper.getClassInfo(TestClass.class);
		assertNotNull(classInfo);

		// Obtain it again (should be cached)
		classInfo = ReflectionHelper.getClassInfo(TestClass.class);
		assertNotNull(classInfo);

		// Test Fields
		LinkedHashMap<String, FieldInfo> fields = classInfo.getFields();
		assertEquals(fields.size(), 3);
		assertTrue(fields.containsKey("age"));
		assertTrue(fields.containsKey("name"));
		assertTrue(fields.containsKey("birthday"));

		// Test Properties
		LinkedHashMap<String, PropertyInfo> properties = classInfo.getProperties();
		assertEquals(properties.size(), 3);
		assertTrue(properties.containsKey("Age"));
		assertTrue(properties.containsKey("Name"));
		assertTrue(properties.containsKey("Birthday"));

		// Create instance
		TestClass testClass = null;
		try
		{
			testClass = classInfo.newInstance();
		}
		catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			fail("Failed to create new Instance");
		}

		// Setup
		Date date1 = new DateTime(2013, 2, 21);
		Date date2 = new DateTime(2014, 2, 21);
		testClass.setAge(21);
		testClass.setName("Tsavendas");
		testClass.setBirthday(date1);

		// Test get fields
		try
		{
			assertEquals((int) fields.get("age").get(testClass), 21);
			assertEquals((String) classInfo.get(testClass, "name"), "Tsavendas");
			assertEquals((Date) fields.get("birthday").get(testClass), date1);
		}
		catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e)
		{
			fail("Failed to access Fields");
		}

		// Test get Properties
		try
		{
			assertEquals((int) properties.get("Age").get(testClass), 21);
			assertEquals((String) classInfo.get(testClass, "Name"), "Tsavendas");
			assertEquals((Date) properties.get("Birthday").get(testClass), date1);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			fail("Failed to access Properties");
		}

		// Test set fields
		try
		{
			fields.get("age").set(testClass, 22);
			classInfo.set(testClass, "name", "Dimitri");
			fields.get("birthday").set(testClass, date2);
			assertEquals(testClass.getAge(), 22);
			assertEquals(testClass.getName(), "Dimitri");
			assertEquals(testClass.getBirthday(), date2);
		}
		catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e)
		{
			fail("Failed to set Fields");
		}

		// Test set properties
		try
		{
			properties.get("Age").set(testClass, 23);
			classInfo.set(testClass, "Name", "Hendrik");
			properties.get("Birthday").set(testClass, date1);
			assertEquals(testClass.getAge(), 23);
			assertEquals(testClass.getName(), "Hendrik");
			assertEquals(testClass.getBirthday(), date1);
		}
		catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e)
		{
			fail("Failed to set Properties");
		}

	}

	@Test
	public void testGetClassInfoSpeed() throws Exception
	{
		int iterations = 100000000;
		testFieldSpeed(iterations);
		testPropertySpeed(iterations);
		testInstantiationSpeed(iterations);
	}

	private void testFieldSpeed(int iterations) throws Exception
	{
		// Without Reflection
		TestClass testObject = new TestClass();

		long duration1 = System.currentTimeMillis();
		for (int index = 0; index < iterations; index++)
		{
			testObject.age = index;
			assertEquals(index, testObject.age);
		}
		duration1 = System.currentTimeMillis() - duration1;

		long duration2 = System.currentTimeMillis();
		for (int index = 0; index < iterations; index++)
		{
			testObject.age = index;
			assertEquals(index, testObject.age);
			testObject.age = index;
			assertEquals(index, testObject.age);
		}
		duration2 = System.currentTimeMillis() - duration2 - duration1;

		// With Reflection
		ClassInfo classInfo = ReflectionHelper.getClassInfo(TestClass.class);
		FieldInfo field = classInfo.getFields().get("age");

		long duration3 = System.currentTimeMillis();
		for (int index = 0; index < iterations; index++)
		{
			field.set(testObject, index);
			assertEquals(index, field.get(testObject));
		}
		duration3 = System.currentTimeMillis() - duration3;

		long duration4 = System.currentTimeMillis();
		for (int index = 0; index < iterations; index++)
		{
			field.set(testObject, index);
			assertEquals(index, field.get(testObject));
			field.set(testObject, index);
			assertEquals(index, field.get(testObject));
		}
		duration4 = System.currentTimeMillis() - duration4 - duration3;

		// Reflection may not be more than 4 times slower
		System.out.println(String.format("Fields       : %5d vs %5d", duration2, duration4));
		// assertTrue(duration2 * 4 > duration4);
	}

	private void testPropertySpeed(int iterations) throws Exception
	{
		// Without Reflection
		TestClass testObject = new TestClass();

		long duration1 = System.currentTimeMillis();
		for (int index = 0; index < iterations; index++)
		{
			testObject.setAge(index);
			assertEquals(index, testObject.getAge());
		}
		duration1 = System.currentTimeMillis() - duration1;

		long duration2 = System.currentTimeMillis();
		for (int index = 0; index < iterations; index++)
		{
			testObject.setAge(index);
			assertEquals(index, testObject.getAge());
			testObject.setAge(index);
			assertEquals(index, testObject.getAge());
		}
		duration2 = (System.currentTimeMillis() - duration2) - duration1;

		// With Reflection
		ClassInfo classInfo = ReflectionHelper.getClassInfo(TestClass.class);
		IPropertyInfo property = classInfo.getProperties().get("Age");

		long duration3 = System.currentTimeMillis();
		for (int index = 0; index < iterations; index++)
		{
			property.set(testObject, index);
			assertEquals(index, property.get(testObject));
		}
		duration3 = System.currentTimeMillis() - duration3;

		long duration4 = System.currentTimeMillis();
		for (int index = 0; index < iterations; index++)
		{
			property.set(testObject, index);
			assertEquals(index, property.get(testObject));
			property.set(testObject, index);
			assertEquals(index, property.get(testObject));
		}
		duration4 = (System.currentTimeMillis() - duration4) - duration3;

		// Reflection may not be more than 2 times slower
		System.out.println(String.format("Properties   : %5d vs %5d", duration2, duration4));
		// assertTrue(duration2 * 2 > duration4);

	}

	private void testInstantiationSpeed(int iterations) throws Exception
	{
		// Without Reflection
		System.gc();
		long duration1 = System.currentTimeMillis();
		for (int index = 0; index < iterations; index++)
		{
			TestClass testObject = new TestClass();
			assertNotNull(testObject);
		}
		duration1 = System.currentTimeMillis() - duration1;

		long duration2 = System.currentTimeMillis();
		for (int index = 0; index < iterations; index++)
		{
			TestClass testObject = new TestClass();
			assertNotNull(testObject);
			testObject = new TestClass();
			assertNotNull(testObject);
		}
		duration2 = System.currentTimeMillis() - duration2 - duration1;

		// With Reflection
		ClassInfo classInfo = ReflectionHelper.getClassInfo(TestClass.class);
		System.gc();
		long duration3 = System.currentTimeMillis();
		for (int index = 0; index < iterations; index++)
		{
			TestClass testObject = classInfo.newInstance();
			assertNotNull(testObject);
		}
		duration3 = System.currentTimeMillis() - duration3;

		long duration4 = System.currentTimeMillis();
		for (int index = 0; index < iterations; index++)
		{
			TestClass testObject = classInfo.newInstance();
			assertNotNull(testObject);
			testObject = classInfo.newInstance();
			assertNotNull(testObject);
		}
		duration4 = System.currentTimeMillis() - duration4 - duration3;

		// Reflection may not be more than 2 times slower
		System.out.println(String.format("Instantiation: %5d vs %5d", duration2, duration4));
		// assertTrue(duration2 * 4 > duration4);

	}

}
