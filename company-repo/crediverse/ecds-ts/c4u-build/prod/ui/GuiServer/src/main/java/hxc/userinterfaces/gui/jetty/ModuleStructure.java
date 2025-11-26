package hxc.userinterfaces.gui.jetty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ModuleStructure
{
	final static Logger logger = LoggerFactory.getLogger(ModuleStructure.class);
	public static final String CS_PLUGIN_CLASSES_DIR = "/classes/";
	public static final String CS_PLUGIN_LIB_DIR = "/lib/";
	public static final String CS_PLUGIN_RESOURCES_DIR = "/resources/";
	public static final String CS_PLUGIN_PROPERTIES_FILE = "/etc/plugin.properties";
	public static final String CS_PLUGIN_PATH = "CS_GUI_PLUGIN_PATH";
	public static final String CS_INSTALLATION_VAR_DIR = "CS_INSTALLATION_VAR_DIR";

	public static boolean validatePluginStructure(String path)
	{
		boolean isvalid = true;

		if (!Utility.validDirectory(path))
		{
			logger.error("Cannot access module directory " + path);
			isvalid = false;
		}
		if (!Utility.validDirectory(path + ModuleStructure.CS_PLUGIN_RESOURCES_DIR))
		{
			logger.error("Cannot access module resource directory " + path + ModuleStructure.CS_PLUGIN_RESOURCES_DIR);
			isvalid = false;
		}
		if (!Utility.validDirectory(path + ModuleStructure.CS_PLUGIN_CLASSES_DIR))
		{
			logger.error("Cannot access module class directory " + path + ModuleStructure.CS_PLUGIN_CLASSES_DIR);
			isvalid = false;
		}
		if (!Utility.validFile(path + ModuleStructure.CS_PLUGIN_PROPERTIES_FILE))
		{
			logger.error("Cannot access module properties file " + path + ModuleStructure.CS_PLUGIN_CLASSES_DIR);
			isvalid = false;
		}

		return isvalid;
	}

	public static void configurePluginClassPath(Set<String> pathSections, URLClassLoader classLoader, String path) throws Exception
	{
		String pathSection = null;

		// Add classpath directory entries
		pathSection = Utility.expandPath(path);
		pathSections.add(pathSection);
		Utility.addToClassPath(classLoader, pathSection);

		pathSection = Utility.expandPath(path + ModuleStructure.CS_PLUGIN_RESOURCES_DIR);
		Utility.addToClassPath(classLoader, pathSection);
		pathSections.add(pathSection);

		pathSection = Utility.expandPath(path + ModuleStructure.CS_PLUGIN_CLASSES_DIR);
		Utility.addToClassPath(classLoader, pathSection);
		pathSections.add(pathSection);
	}

	public static void configureRuntimeLibs()
	{
		String pathSection = null;
		String pluginPath = System.getenv(ModuleStructure.CS_PLUGIN_PATH);
		if (null != pluginPath)
		{
			Map<String, String> libList = new LinkedHashMap<String, String>();
			for (String path : pluginPath.split(":")) // for start
			{
				try
				{
					pathSection = Utility.expandPath(path + ModuleStructure.CS_PLUGIN_LIB_DIR);
					if (Utility.validDirectory(pathSection))
					{
						File libDir = new File(pathSection);
						for (File jarfile : libDir.listFiles())
						{
							String name = jarfile.getName();
							if (name.endsWith(".jar"))
							{
								if (libList.containsKey(name))
								{
									logger.error("Library " + name + " is already loaded, ignoring " + jarfile.getCanonicalPath());
								}
								else
								{
									libList.put(name, jarfile.getCanonicalPath());
								}
							}
						}
					}
				}
				catch (Exception e)
				{
				}
			}
			Iterator<Entry<String, String>> libIter = libList.entrySet().iterator();
			while (libIter.hasNext())
			{
				Entry<String, String> entry = libIter.next();

				try
				{
					Utility.addFile(entry.getValue());
				}
				catch (IOException e)
				{
				}
			}
		}
	}

}
