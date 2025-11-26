package hxc.userinterfaces.gui.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

public class PathUtils
{

	public static String uppercaseFirstChar(String value)
	{
		String result = value;
		if (value != null)
		{
			result = Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase();
		}

		return result;
	}

	public static URL classLoaderUrlFromPath(String path)
	{
		URL url = null;
		URLStreamHandler streamHandler = null;
		File classPath = new File(path);
		try
		{
			String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator).toString());
			url = new URL(null, repository, streamHandler);
		}
		catch (Exception ex)
		{
		}
		return url;
	}

	public static boolean validDirectory(String path)
	{
		boolean result = false;
		File pathSection = new File(path);
		if (pathSection.exists() && pathSection.isDirectory() && pathSection.canRead())
		{
			result = true;
		}
		return result;
	}

	public static boolean validFile(String path)
	{
		boolean result = false;
		if (path != null)
		{
			File pathSection = new File(path);
			if (pathSection.exists() && pathSection.isFile() && pathSection.canRead())
			{
				result = true;
			}
		}
		return result;
	}

	public static void addToClassPath(URLClassLoader loader, String path) throws Exception
	{
		URLClassLoader classLoader = loader;
		if (classLoader == null)
		{
			classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		}

		Class<?> clazz = URLClassLoader.class;

		// Use reflection
		Method method = clazz.getDeclaredMethod("addURL", new Class[] { URL.class });
		method.setAccessible(true);
		URL url = PathUtils.classLoaderUrlFromPath(path);
		method.invoke(classLoader, new Object[] { url });
	}

	/**
	 * Parameters of the method to add an URL to the System classes.
	 */
	private static final Class<?>[] parameters = new Class[] { URL.class };

	/**
	 * Adds a file to the classpath.
	 * 
	 * @param s
	 *            a String pointing to the file
	 * @throws IOException
	 */
	public static void addFile(String s) throws IOException
	{
		File f = new File(s);
		addFile(f);
	}// end method

	/**
	 * Adds a file to the classpath
	 * 
	 * @param f
	 *            the file to be added
	 * @throws IOException
	 */
	public static void addFile(File f) throws IOException
	{
		addURL(f.toURI().toURL());
	}// end method

	/**
	 * Adds the content pointed by the URL to the classpath.
	 * 
	 * @param u
	 *            the URL pointing to the content to be added
	 * @throws IOException
	 */
	public static void addURL(URL u) throws IOException
	{
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class<?> sysclass = URLClassLoader.class;
		try
		{
			Method method = sysclass.getDeclaredMethod("addURL", parameters);
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { u });
		}
		catch (Throwable t)
		{
			throw new IOException("Error, could not add URL to system classloader");
		}// end try catch
	}// end method

	public static String expandPath(String path) throws IOException
	{
		String result = null;

		if (path != null)
		{
			File file = new File(path);
			result = file.getCanonicalPath();
			if (!result.endsWith("/"))
			{
				result += "/";
			}
		}

		return result;
	}
}
