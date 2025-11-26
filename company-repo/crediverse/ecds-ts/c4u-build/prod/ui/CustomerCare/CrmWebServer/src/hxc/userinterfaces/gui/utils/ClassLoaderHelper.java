package hxc.userinterfaces.gui.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public abstract class ClassLoaderHelper
{

	public ClassLoaderHelper(String pckName, Class<?> inturface, Object... parmsToPass) throws ClassNotFoundException, IOException
	{
		this.findClasses(pckName, inturface, parmsToPass);
	}

	/**
	 * Converts path to URL
	 */
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
			ex.printStackTrace();
		}
		return url;
	}

	/**
	 * Add URL to classpath
	 */
	public static void addUrlToClassPath(URL url) throws Exception
	{
		URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		// URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
		Class<?> urlClass = URLClassLoader.class;

		Method method = urlClass.getDeclaredMethod("addURL", new Class[] { URL.class });
		method.setAccessible(true);
		method.invoke(urlClassLoader, new Object[] { url });
	}

	public static void addClassesToClassPath(String path) throws Exception
	{
		try
		{
			URL url = classLoaderUrlFromPath(path);
			addUrlToClassPath(url);

			searchAndLoadJars(path);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void searchAndLoadJars(String path) throws Exception
	{

		File root = new File(path);
		File[] list = root.listFiles();

		if (list == null)
			return;

		for (File f : list)
		{
			if (f.isDirectory())
			{
				searchAndLoadJars(f.getAbsolutePath());
			}
			else
			{
				if (f.getName().endsWith(".jar"))
					loadLibrary(f);
			}
		}
	}

	public static synchronized void loadLibrary(java.io.File jar)
	{
		try
		{
			/* We are using reflection here to circumvent encapsulation; addURL is not public */
			java.net.URLClassLoader loader = (java.net.URLClassLoader) ClassLoader.getSystemClassLoader();
			java.net.URL url = jar.toURI().toURL();
			/* Disallow if already loaded */
			for (java.net.URL it : java.util.Arrays.asList(loader.getURLs()))
			{
				if (it.equals(url))
				{
					return; // No point loading what is already loaded
				}
			}
			java.lang.reflect.Method method = java.net.URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { java.net.URL.class });
			method.setAccessible(true); /* promote the method to public access */
			method.invoke(loader, new Object[] { url });
		}
		catch (final NoSuchMethodException | java.lang.IllegalAccessException | java.net.MalformedURLException | java.lang.reflect.InvocationTargetException e)
		{

			// For now I want to see!
			e.printStackTrace();
		}
	}

	private void findClasses(String pckName, Class<?> inturface, Object... parmsToPass) throws IOException, ClassNotFoundException
	{
		Set<Class<?>> classes = getClasses(pckName);
		for (Class<?> cl : classes)
		{
			if (inturface.isAssignableFrom(cl))
			{
				foundClass(cl, parmsToPass);
			}
		}
	}

	private Set<Class<?>> getClasses(String packageName) throws IOException, ClassNotFoundException
	{
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		return getClasses(loader, packageName);
	}

	private Set<Class<?>> getClasses(ClassLoader loader, String packageName) throws IOException, ClassNotFoundException
	{
		Set<Class<?>> classes = new HashSet<Class<?>>();
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = loader.getResources(path);
		if (resources != null)
		{
			while (resources.hasMoreElements())
			{
				String filePath = resources.nextElement().getFile();
				if (filePath != null)
				{
					filePath = filePath.replaceAll("%20", " "); // WINDOWS HACK
					if ((filePath.indexOf("!") > 0) & (filePath.indexOf(".jar") > 0))
					{
						String jarPath = filePath.substring(0, filePath.indexOf("!")).substring(filePath.indexOf(":") + 1);
						// WINDOWS HACK
						if (jarPath.indexOf(":") >= 0)
						{
							jarPath = jarPath.substring(1);
						}
						classes.addAll(getFromJARFile(jarPath, path));
					}
					else
					{
						classes.addAll(getFromDirectory(new File(filePath), packageName));
					}
				}
			}
		}
		return classes;
	}

	private Set<Class<?>> getFromDirectory(File directory, String packageName) throws ClassNotFoundException
	{
		Set<Class<?>> classes = new HashSet<Class<?>>();
		if (directory.exists())
		{
			for (String file : directory.list())
			{
				if (file.endsWith(".class"))
				{
					String name = packageName + '.' + stripFilenameExtension(file);
					Class<?> clazz = Class.forName(name);
					classes.add(clazz);
				}
			}
		}
		return classes;
	}

	private String stripFilenameExtension(String file)
	{
		return file.substring(0, file.length() - 6);
	}

	private Set<Class<?>> getFromJARFile(String jar, String packageName) throws IOException, ClassNotFoundException
	{
		Set<Class<?>> classes = new HashSet<Class<?>>();
		try (JarInputStream jarFile = new JarInputStream(new FileInputStream(jar)))
		{
			JarEntry jarEntry;
			do
			{
				jarEntry = jarFile.getNextJarEntry();
				if (jarEntry != null)
				{
					String className = jarEntry.getName();
					if (className.endsWith(".class"))
					{
						className = stripFilenameExtension(className);
						if (className.startsWith(packageName))
						{
							classes.add(Class.forName(className.replace('/', '.')));
						}
					}
				}
			} while (jarEntry != null);
		}
		return classes;
	}

	/**
	 * Compute the absolute file path to the jar file. The framework is based on http://stackoverflow.com/a/12733172/1614775 But that gets it right for only one of the four cases.
	 * 
	 * @param aclass
	 *            A class residing in the required jar.
	 * 
	 * @return A File object for the directory in which the jar file resides. During testing with NetBeans, the result is ./build/classes/, which is the directory containing what will be in the jar.
	 */
	public File getClassLocationDir(Class aclass)
	{
		URL url;
		String extURL; // url.toExternalForm();

		// get an url
		try
		{
			url = aclass.getProtectionDomain().getCodeSource().getLocation();
			// url is in one of two forms
			// ./build/classes/ NetBeans test
			// jardir/JarName.jar froma jar
		}
		catch (SecurityException ex)
		{
			url = aclass.getResource(aclass.getSimpleName() + ".class");
			// url is in one of two forms, both ending "/com/physpics/tools/ui/PropNode.class"
			// file:/U:/Fred/java/Tools/UI/build/classes
			// jar:file:/U:/Fred/java/Tools/UI/dist/UI.jar!
		}

		// convert to external form
		extURL = url.toExternalForm();

		// prune for various cases
		if (extURL.endsWith(".jar")) // from getCodeSource
			extURL = extURL.substring(0, extURL.lastIndexOf("/"));
		else
		{ // from getResource
			String suffix = "/" + (aclass.getName()).replace(".", "/") + ".class";
			extURL = extURL.replace(suffix, "");
			if (extURL.startsWith("jar:") && extURL.endsWith(".jar!"))
				extURL = extURL.substring(4, extURL.lastIndexOf("/"));
		}

		// convert back to url
		try
		{
			url = new URL(extURL);
		}
		catch (MalformedURLException mux)
		{
			// leave url unchanged; probably does not happen
		}

		// convert url to File
		try
		{
			return new File(url.toURI());
		}
		catch (Exception ex)
		{
			return new File(url.getPath());
		}
	}

	public abstract void foundClass(Class found, Object... parms);

}
