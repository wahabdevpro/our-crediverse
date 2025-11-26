package hxc.connectors.ui.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import hxc.utils.reflection.ReflectionHelper;

public class UiConnectorUtils
{

	public static String splitCamelCaseString(String camelCase)
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < camelCase.length(); i++)
		{
			char c = camelCase.charAt(i);
			if (i == 0)
			{
				sb.append(String.valueOf(c).toUpperCase());
			}
			else
			{
				if (c >= 65 && c <= 90)
				{
					sb.append(" ");
				}
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static byte[] generateSalted(byte[] publicKey, String password) throws NoSuchAlgorithmException
	{
		byte[] passwordBytes = password.getBytes();
		byte[] credentials = new byte[passwordBytes.length + publicKey.length];
		System.arraycopy(passwordBytes, 0, credentials, 0, passwordBytes.length);
		System.arraycopy(publicKey, 0, credentials, passwordBytes.length, publicKey.length);
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		return md.digest(credentials);
	}

	public static void setUsingString(Object instance, String sValue, Method setterMethod) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Class<?> parms[] = setterMethod.getParameterTypes();
		Object value = ReflectionHelper.valueOf(parms[0], sValue);
		setterMethod.invoke(instance, value);
	}

	public static String arrayToCsv(Object[] arr)
	{
		StringBuilder sb = new StringBuilder();
		for (Object en : arr)
		{
			sb.append(en).append(",");
		}
		return sb.substring(0, sb.length() - 1);
	}

	public static <T> String setToCsv(Set<T> arr)
	{
		StringBuilder sb = new StringBuilder();
		for (Object en : arr)
		{
			sb.append(en).append(",");
		}
		return sb.substring(0, sb.length() - 1);
	}

	public static void saveDataToFile(String data, String file)
	{
		try (FileWriter fw = new FileWriter(new File(file)))
		{
			fw.write(data);
			fw.flush();
		}
		catch (IOException ioe)
		{
		}
	}

}
