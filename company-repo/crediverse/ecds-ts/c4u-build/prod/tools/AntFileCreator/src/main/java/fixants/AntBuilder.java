/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fixants;

import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;

import jaxb.generated.Classpath;

/**
 *
 * @author jceatwell
 */
public class AntBuilder
{

	private final static String JAR_DESTINATION_PROPERTY = "dest.jar";
	private final static String JAR_MAIN_CLASS_PROPERTY = "main.class";
	private final static String JAR_INCLUDE_LIBS_PROPERTY = "include.libs";
	private final static String JAR_INCLUDE_FOLDERS_PROPERTY = "include.folders";

	// Use this property (ignore=true)to tell the AntFileCreator to ignore this folder,
	// i.e. DO NOT BUILD ANT FILE in this folder
	// Note this check is done one level up
	public final static String IGNORE_FOLDER_DO_NOT_BUILD_ANT_FILE = "ignore";

	private DependencyFinder dependencyFinder;
	private String projectName;
	private Set<String> dependencySet;
	private Properties jarProperties;

	public AntBuilder(DependencyFinder dependencyFinder, String projectName, Properties jarProperties)
	{
		this.dependencyFinder = dependencyFinder;
		this.projectName = projectName;
		this.jarProperties = jarProperties;
		init();
	}

	private void init()
	{
		dependencySet = dependencyFinder.reverseDependencySet(projectName);
	}

	public String buildAntFileContent(boolean buildWithDebugSymbols) throws Exception
	{
		StringBuilder sb = new StringBuilder();

		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
		sb.append(String.format("<project basedir=\".\" default=\"build\" name=\"%s\">\n", projectName));

		sb.append(buildPropertiesSection(buildWithDebugSymbols));
		sb.append(buildPathSection());
		sb.append(buildAllTargets());
		sb.append("</project>\n");
		return sb.toString();
	}

	// /////////////////////////////////////////////////////////////////////////////
	// PROPERTIES SECTION
	// /////////////////////////////////////////////////////////////////////////////
	public String buildPropertiesSection(boolean buildWithDebugSymbols) throws Exception
	{
		Path projectPath = dependencyFinder.getProjectLocations().get(projectName);

		StringBuilder sb = new StringBuilder();
		sb.append("\t<property environment=\"env\"/>\n");
		sb.append("\t<property name=\"ECLIPSE_HOME\" value=\"../../../../../eclipse\"/>\n");
		for (String dep : dependencySet)
		{
			try
			{
				Path depPath = dependencyFinder.getProjectLocations().get(dep);
				;
				Path rel = projectPath.relativize(depPath);
				sb.append("\t").append(buildProperty(dep + ".location", rel.toString())).append("\n");
			}
			catch (Exception e)
			{
				throw new Exception(String.format("[Problem resolving dependency (%s)]", dep));
			}
		}
		sb.append(String.format("\t<property name=\"c4u.debug\" value=\"%s\"/>\n", (buildWithDebugSymbols? "true" : "off")));
		sb.append("\t<property name=\"c4u.debuglevel\" value=\"lines,vars,source\"/>\n");
		sb.append("\t<property name=\"target\" value=\"1.7\"/>\n");
		sb.append("\t<property name=\"source\" value=\"1.7\"/>\n");

		return sb.toString();
	}

	public String buildProperty(String name, String location)
	{
		return String.format("<property name=\"%s\" value=\"%s\" />", name, location);
	}

	// /////////////////////////////////////////////////////////////////////////////
	// PATH SECTION
	// /////////////////////////////////////////////////////////////////////////////

	public String buildPathSection()
	{
		StringBuilder sb = new StringBuilder();
		for (String dep : dependencySet)
		{
			sb.append(buildSinglePath(dep));
		}
		sb.append(buildSinglePath(projectName));
		return sb.toString();
	}

	public String buildSinglePath(String dependencyName)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("\t<path id=\"%s.classpath\">\n", dependencyName));

		String locationString = dependencyName.equals(projectName) ? "" : ("${" + dependencyName + ".location}/");
		sb.append(String.format("\t\t<pathelement location=\"%s%s\"/>\n", locationString, dependencyFinder.findOutputPath(dependencyName)));

		if (dependencyFinder.getClasspaths().containsKey(projectName))
		{
			for (Classpath.Classpathentry ce : dependencyFinder.getClasspaths().get(dependencyName).getClasspathentry())
			{
				if (ce.getKind().equals("src") && ce.getPath().startsWith("/"))
				{
					sb.append(String.format("\t\t<path refid=\"%s.classpath\"/>\n", ce.getPath().substring(1)));
				}
				else if (ce.getKind().equals("lib"))
				{
					String path = calcLibrary(ce.getPath());
					sb.append(String.format("\t\t<pathelement location=\"%s\"/>\n", path));
				}
			}
		}

		sb.append("\t</path>\n");
		return sb.toString();
	}

	// /////////////////////////////////////////////////////////////////////////////
	// TARGET SECTION
	// /////////////////////////////////////////////////////////////////////////////
	public String buildAllTargets()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(targetInit());
		sb.append(targetClean());
		sb.append(targetBuildSubProjects());
		sb.append(targetBuildProject());
		sb.append(targetBuild());
		if (jarProperties != null)
		{
			sb.append(targetJar());
		}
		return sb.toString();
	}

	private String targetInit()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("\t\t<mkdir dir=\"").append(dependencyFinder.findOutputPath(projectName)).append("\"/>\n");
		sb.append("\t\t<copy includeemptydirs=\"false\" todir=\"classes\">\n");
		sb.append(String.format("\t\t\t<fileset dir=\"%s\">\n", dependencyFinder.findSrcPath(projectName)));
		sb.append("\t\t\t\t<exclude name=\"**/*.java\"/>\n");
		sb.append("\t\t\t</fileset>\n");
		sb.append("\t\t</copy>\n");

		return targetTemplate("init", null, sb.toString());
	}

	private String targetClean()
	{
		StringBuilder sb = new StringBuilder(targetTemplate("clean", null, String.format("\t\t<delete dir=\"%s\"/>\n", dependencyFinder.findOutputPath(projectName))));
		StringBuilder content = new StringBuilder();
		for (String dep : dependencySet)
		{
			content.append(String.format("\t\t<ant antfile=\"build.xml\" dir=\"${%s.location}\" inheritAll=\"false\" target=\"clean\"/>\n", dep));
		}
		sb.append(targetTemplate("cleanall", "clean", content.toString()));
		return sb.toString();
	}

	private String targetBuildSubProjects()
	{
		StringBuilder ants = new StringBuilder();

		for (String dep : dependencySet)
		{
			ants.append(antTargetContent(dep));
		}

		return targetTemplate("build-subprojects", null, ants.toString());
	}

	private String antTargetContent(String dependencyName)
	{
		StringBuilder sb = new StringBuilder(String.format("\t\t<ant antfile=\"build.xml\" dir=\"${%s.location}\" inheritAll=\"false\" target=\"build-project\">\n", dependencyName));
		sb.append("\t\t\t<propertyset>\n");
		sb.append("\t\t\t\t<propertyref name=\"build.compiler\"/>\n");
		sb.append("\t\t\t</propertyset>\n");
		sb.append("\t\t</ant>\n");
		return sb.toString();
	}

	private String targetBuildProject()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("\t\t<echo message=\"${ant.project.name}: ${ant.file}\"/>\n");
		sb.append(String.format("\t\t<javac debug=\"${c4u.debug}\" encoding=\"UTF-8\" debuglevel=\"${c4u.debuglevel}\" destdir=\"%s\" includeantruntime=\"false\" source=\"${source}\" target=\"${target}\">\n",
				dependencyFinder.findOutputPath(projectName)));
		sb.append(String.format("\t\t\t<src path=\"%s\"/>\n", dependencyFinder.findSrcPath(projectName)));
		sb.append(String.format("\t\t\t<classpath refid=\"%s.classpath\"/>\n", projectName));
		//sb.append("\t\t\t<compilerarg value=\"-Xlint:all\"/>\n");
		sb.append("\t\t</javac>\n");

		return targetTemplate("build-project", "init", sb.toString());
	}

	private String targetBuild()
	{
		return targetTemplate("build", String.format("build-subprojects,build-project%s", (jarProperties != null) ? ",build-jar" : ""), null);
	}

	private String targetJar()
	{
		StringBuilder sb = new StringBuilder();

		String jarDestination = jarProperties.getProperty(JAR_DESTINATION_PROPERTY);
		String mainClass = jarProperties.getProperty(JAR_MAIN_CLASS_PROPERTY);
		boolean includeLibraries = true;
		try
		{
			includeLibraries = Boolean.parseBoolean(jarProperties.getProperty(JAR_INCLUDE_LIBS_PROPERTY));
		}
		catch (Exception e)
		{
		}

		sb.append(String.format("\t\t<delete file=\"%s\" />\n", jarDestination));

		sb.append(String.format("\t\t<jar destfile=\"%s\" filesetmanifest=\"mergewithoutmain\">\n", jarDestination));
		sb.append("\t\t\t<manifest>\n");
		sb.append(String.format("\t\t\t\t<attribute name=\"Main-Class\" value=\"%s\"/>\n", mainClass));
		sb.append("\t\t\t\t<attribute name=\"Class-Path\" value=\".\"/>\n");
		sb.append("\t\t\t</manifest>\n");

		// Normal Class addition
		sb.append(String.format("\t\t\t<fileset dir=\"%s\"/>\n", dependencyFinder.findOutputPath(projectName)));

		// Include Folders (Requirement added for Credit Distribution / persistence.xml)
		if (jarProperties.getProperty(JAR_INCLUDE_FOLDERS_PROPERTY) != null)
		{
			String [] includeFolders = jarProperties.getProperty(JAR_INCLUDE_FOLDERS_PROPERTY).split("[:;]+");
			for (String folder : includeFolders)
			{
				sb.append(String.format("\t\t\t<fileset dir=\"%s\"/>\n", folder));
			}
		}

		if (includeLibraries)
		{
			if (dependencyFinder.getClasspaths().containsKey(projectName))
			{
				for (Classpath.Classpathentry ce : dependencyFinder.getClasspaths().get(projectName).getClasspathentry())
				{
					if (ce.getKind().equals("lib"))
					{
						sb.append(String.format("\t\t\t<zipfileset excludes=\"META-INF/*.SF\" src=\"%s\"/>\n", calcLibrary(ce.getPath())));
					}
				}
			}
		}

		for (String dep : dependencySet)
		{
			if (dependencyFinder.getClasspaths().containsKey(dep))
			{
				sb.append(String.format("\t\t\t<fileset dir=\"${%s.location}/%s\"/>\n", dep, dependencyFinder.findOutputPath(dep)));
				for (Classpath.Classpathentry ce : dependencyFinder.getClasspaths().get(dep).getClasspathentry())
				{
					if (ce.getKind().equals("lib"))
					{
						sb.append(String.format("\t\t\t<zipfileset excludes=\"META-INF/*.SF\" src=\"${%s.location}/%s\"/>\n", dep, ce.getPath()));
					}
				}
			}
		}

		sb.append("\t\t</jar>\n");

		return targetTemplate("build-jar", null, sb.toString());
	}

	private String calcLibrary(String path)
	{
		if (path.startsWith("/"))
		{
			String depRef = path.substring(1, path.indexOf('/', 2));
			String lib = path.substring(path.indexOf('/', 2) + 1);
			path = String.format("${%s.location}/%s", depRef, lib);
		}
		return path;
	}

	private String targetTemplate(String name, String depends, String content)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\t<target name=\"").append(name).append("\"");
		if (depends != null)
		{
			sb.append(" depends=\"").append(depends).append("\"");
		}
		sb.append((content == null) ? "/>\n" : ">\n");
		if (content != null)
		{
			sb.append(content);
			sb.append("\t</target>\n");
		}
		return sb.toString();
	}
}
