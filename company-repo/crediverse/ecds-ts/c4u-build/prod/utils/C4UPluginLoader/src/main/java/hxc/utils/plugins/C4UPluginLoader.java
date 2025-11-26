package hxc.utils.plugins;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class C4UPluginLoader extends URLClassLoader
{
	final static Logger logger = LoggerFactory.getLogger(C4UPluginLoader.class);

	public C4UPluginLoader()
	{
		super(new URL[] {});
	}

	public C4UPluginLoader(URL url)
	{
		super(new URL[] { url });
	}

	public C4UPluginLoader(URL urls[])
	{
		super(urls);
	}

	@Override
	public void addURL(URL url)
	{
		// Ensure the url passed in is not null
		if (url == null)
			return;

		// Iterate through the already loaded URLs
		for (URL u : getURLs())
		{
			// Check if it already has the URL
			if (u != null && u.equals(url))
			{
				return;
			}
		}

		// Log information
		logger.info( "Loading plugin: {}", url.getFile());

		super.addURL(url);
	}

	public Class<?> loadMainClassFromJar(URL url) throws ClassNotFoundException, MalformedURLException, IOException
	{
		// Open a Jar URL Connection
		JarURLConnection j = (JarURLConnection) new URL("jar", "", url + "!/").openConnection();

		// Get the Main Class of Jar
		Attributes attri = j.getMainAttributes();
		String mainClass = attri.getValue(Attributes.Name.MAIN_CLASS);

		// Add the URL to the ClassLoader
		addURL(url);

		// Return the Class
		return super.loadClass(mainClass);
	}

	public Class<?> loadMainClassFromJar(File jarFile) throws ClassNotFoundException, MalformedURLException, IOException
	{
		// Run loadClassFromJar with URL as parameter
		return loadMainClassFromJar(jarFile.toURI().toURL());
	}
}
