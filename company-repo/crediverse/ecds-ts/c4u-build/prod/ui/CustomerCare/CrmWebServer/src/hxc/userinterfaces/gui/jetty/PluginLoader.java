package hxc.userinterfaces.gui.jetty;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Map;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import hxc.userinterfaces.gui.plugin.IPluginService;
import hxc.userinterfaces.gui.plugin.ResourceLocation;
import hxc.userinterfaces.gui.utils.ClassLoaderHelper;
import hxc.userinterfaces.gui.utils.GuiUtils;

public class PluginLoader
{

	public static void loadPluginClassPath(String webappPath)
	{

		try
		{
			StringBuilder classFolder = new StringBuilder(webappPath);
			classFolder.append(File.separator);
			classFolder.append("WEB-INF").append(File.separator);
			classFolder.append("classes");
			System.out.printf(String.format("Loading Plugin classpath: %s%n", classFolder.toString()));
			ClassLoaderHelper.addClassesToClassPath(classFolder.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Call plugins to register plugin services (NOTE: All plugins need to have a Login Service else loading will fail
	 * 
	 * @param contextHandler
	 */
	public static void registerAllPluginServices(Handler contextHandler, ContextHandlerCollection handlers)
	{
		try
		{
			new ClassLoaderHelper("hxc.userinterfaces.gui.plugin", IPluginService.class, contextHandler, handlers)
			{

				@Override
				public void foundClass(@SuppressWarnings("rawtypes") Class found, Object... parms)
				{
					if (!found.isInterface())
					{
						String webbappFolder = null;
						try
						{
							File f = getClassLocationDir(found);
							int pos = f.getCanonicalPath().indexOf("WEB-INF");
							webbappFolder = f.getCanonicalPath().substring(0, pos);
						}
						catch (IOException e)
						{
							// TODO THIS SHOULD NEVER SHOW!!
							System.out.println("registerAllPluginServices GIVING PROBLEMS WITH CLASS AND FOLDER LOADING!");
							e.printStackTrace();
						}

						try
						{
							IPluginService plugin = (IPluginService) found.newInstance();
							if (parms != null)
							{
								Handler contextHandler = (Handler) parms[0];
								ContextHandlerCollection handlers = (ContextHandlerCollection) parms[1];

								plugin.registerContextBaseService(contextHandler);

								// Extract Resource Paths
								Annotation[] annotations = found.getAnnotations();
								if (annotations != null)
								{
									for (Annotation annotation : annotations)
									{
										if (annotation instanceof ResourceLocation)
										{
											ResourceLocation resLocations = (ResourceLocation) annotation;

											String virPath = "/" + resLocations.virtual();
											String resPath = "web/" + resLocations.actual() + "/";

											System.out.printf("%s = %s%s%n", virPath, webbappFolder, resPath);
											Handler handler = GuiUtils.createResourceHandler("/" + resLocations.virtual(), webbappFolder, "web/" + resLocations.actual() + "/");
											handlers.addHandler(handler);
										}
									}
								}
							}
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}

				}
			};
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static Map<String, String> extractPluginResourceMappings()
	{
		return null;
	}

}
