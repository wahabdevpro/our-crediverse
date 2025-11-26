package cs.c4u.c4udependencychecker;

import cs.c4u.utils.FileUtils;
import cs.c4u.utils.FileWalker;
import cs.c4u.utils.XmlHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import jaxb.generated.Classpath;
import jaxb.generated.ProjectDescription;

public class DependencyFinder 
{

    public final static String IGNORE_FOLDER_DO_NOT_BUILD_ANT_FILE = "ignore";

    Map<String, Path> projectLocations;
    Map<String, Classpath> classpaths;
    Map<String, String> pathProjectLookup;

    public DependencyFinder(String startLocation) throws Exception 
    {
        findDependencies(startLocation);
    }

    private void findDependencies(String startLocation) throws IOException, InterruptedException
    {
        classpaths = new HashMap<>();
        projectLocations = new HashMap<>();
        pathProjectLookup = new HashMap<>();

        new FileWalker(startLocation) 
        {

            @Override
            public void found(Path path, File classpathFile, File projectFile) 
            {
                try 
                {
                    Classpath cp = XmlHelper.extractClassFromXml(Classpath.class, classpathFile);
                    ProjectDescription pj = XmlHelper.extractClassFromXml(ProjectDescription.class, projectFile);

                    //Is there a reason to ignore this path?
                    Properties jarProperties = FileUtils.readPropertiesFile(path.toString() + File.separator + "jar.properties");

                    if (jarProperties == null
                        || (!(jarProperties.containsKey(IGNORE_FOLDER_DO_NOT_BUILD_ANT_FILE)
                        && Boolean.parseBoolean(jarProperties.getProperty(IGNORE_FOLDER_DO_NOT_BUILD_ANT_FILE))))) 
                    {
                        classpaths.put(pj.getName(), cp);
                        projectLocations.put(pj.getName(), path);

                    }
                } 
                catch (JAXBException ex) 
                {
                    Logger.getLogger(DependencyFinder.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
    }

    public Map<String, Path> getProjectLocations() 
    {
        return projectLocations;
    }

    public void setProjectLocations(Map<String, Path> projectLocations) 
    {
        this.projectLocations = projectLocations;
    }

    public Map<String, Classpath> getClasspaths() 
    {
        return classpaths;
    }

    public void setClasspaths(Map<String, Classpath> classpaths) 
    {
        this.classpaths = classpaths;
    }

    // ////////////////////////
    public Set<String> getAllDependencies(String project) 
    {
        return getDependencies(project);
    }

    private Set<String> getDependencies(String project) 
    {
        Set<String> result = new LinkedHashSet<String>();

        if (classpaths.containsKey(project)) 
        {
            for (Classpath.Classpathentry ce : classpaths.get(project).getClasspathentry()) 
            {
                if (ce.getKind().equals("src") && (ce.getPath().startsWith("/"))) 
                {
                    String projectName = ce.getPath().substring(1);
                    result.addAll(getDependencies(projectName));

                    result.add(projectName);
                }
            }
        }

        return result;
    }

    // /////////////////
    public String createDependencyPrintout(String project, int level) 
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) 
        {
            sb.append("\t");
        }
        sb.append(project).append("\n");

        if (classpaths.containsKey(project)) 
        {
            for (Classpath.Classpathentry ce : classpaths.get(project).getClasspathentry()) 
            {
                if (ce.getKind().equals("src") && (ce.getPath().startsWith("/"))) 
                {
                    String projectName = ce.getPath().substring(1);
                    String str = createDependencyPrintout(projectName, level + 1);
                    sb.append(str);
                }
            }
        }

        return sb.toString();
    }

    // ////////////////////////
    public Set<String> reverseDependencySet(String project) 
    {
        Set<String> result = new LinkedHashSet<String>();
        List<List<String>> depList = calcDependencies(project);
        for (int i = depList.size() - 1; i >= 0; i--) 
        {
            result.addAll(depList.get(i));
        }

        return getDependencies(project);
    }

    public List<List<String>> calcDependencies(String project) 
    {
        List<List<String>> result = new LinkedList<List<String>>();
        int startLevel = 0;

        discoverDependency(project, result, startLevel);
        return result;
    }

    private void discoverDependency(String project, List<List<String>> depList, int level) 
    {
        if (depList.size() < (level + 1)) 
        {
            depList.add(new LinkedList<String>());
        }
        depList.get(level).add(project);

        if (classpaths.containsKey(project)) 
        {
            for (Classpath.Classpathentry ce : classpaths.get(project).getClasspathentry()) 
            {
                if (ce.getKind().equals("src") && (ce.getPath().startsWith("/"))) 
                {
                    String projectName = ce.getPath().substring(1);
                    discoverDependency(projectName, depList, level + 1);
                }
            }
        }

    }

    public String findOutputPath(String project) 
    {
        if (classpaths.containsKey(project)) 
        {
            for (Classpath.Classpathentry ce : classpaths.get(project).getClasspathentry()) 
            {
                if (ce.getKind().equals("output")) 
                {
                    return ce.getPath();
                }
            }
        }
        return "";
    }

    public String findSrcPath(String project) 
    {
        if (classpaths.containsKey(project)) 
        {
            for (Classpath.Classpathentry ce : classpaths.get(project).getClasspathentry()) 
            {
                if (ce.getKind().equals("src") && (!ce.getPath().startsWith("/"))) 
                {
                    return ce.getPath();
                }
            }
        }
        return "";
    }

}
