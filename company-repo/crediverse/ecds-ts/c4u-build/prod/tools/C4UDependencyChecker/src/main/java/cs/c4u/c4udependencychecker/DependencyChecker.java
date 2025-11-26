package cs.c4u.c4udependencychecker;

import cs.c4u.c4udependencychecker.rules.StylizeDependencyLineRule;
import cs.c4u.c4udependencychecker.rules.ProcessPackageRule;
import cs.c4u.c4udependencychecker.rules.IRule;
import cs.c4u.c4udependencychecker.rules.IgnoreDependencyRule;
import cs.c4u.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

public class DependencyChecker 
{
    private Logger logger = Logger.getLogger(DependencyChecker.class.toString());
    private final String OUTPUT_FOLDER = "diagrams";
    private IRule [] rules = null;
    
    public enum ProjectType 
    {
        other,
        service,        //  Node
        connector,      //  Node
        core,           //  Rect
        utility,        //  Frame
        protocol,       //  Frame
        test,           //  cloud
        ui;              //  cloud
        
        public String getPackageType() 
        {
            switch(this) {
                case service:
                case connector:
                    return "Node";
                case core:
                    return "Rect";
                case utility:
                case protocol:
                    return "Frame";
                case test:
                case ui:
                    return "Cloud";
                default:
                    return "Folder";
            }
        }
    }
    
    private DependencyFinder depFinder = null;
    private Map<String, ProjectType> projectTypes = null;
//    private Map<ProjectType, Integer> projectTypeCounts = null;
    
    private static final String EOL = String.format("%n");
    
    public DependencyChecker(String rootPath) throws Exception 
    {
        depFinder = new DependencyFinder(rootPath);
        projectTypes = resolveToType();
    }
    
    public void createDependencyDiagram(String baseFileName) throws IOException
    {
        createDependencyDiagram(baseFileName, null, null);
    }
    
    public void createDependencyDiagram(String baseFileName, IRule [] rules, ProjectType [] alwaysIncludeInDiagram) throws IOException
    {
        if (rules == null)
            this.rules = null;
        else
        {
            this.rules = new IRule[rules.length];
            System.arraycopy(rules, 0, this.rules, 0, rules.length);
        }
        
        Set<String> processProjects = new TreeSet<>();
        Set<String> ignoreProjects = new HashSet<>();
        
        if (rules != null) 
        {
            // Process All `Process Package` Rules
            boolean packageRuleFound = false;
            for(IRule rule : rules)
            {
                if (rule instanceof ProcessPackageRule)
                {
                    for(String project : depFinder.getProjectLocations().keySet()) 
                    {
                        if (((ProcessPackageRule) rule).process(project, projectTypes.get(project))) 
                        {
                            processProjects.add(project);
                            processProjects.addAll(getAllDependenciesFromProject(project));
                            packageRuleFound = true;
                        }
                    }
                }
            }
            
            if (!packageRuleFound)
            {
                processProjects.addAll(depFinder.getProjectLocations().keySet());
            }
            
            // Process All `Ignore Dependency` Rules (... and ignore)
            for(IRule rule : rules)
            {
               if (rule instanceof IgnoreDependencyRule)
               {
                   for (Iterator<String> prj = processProjects.iterator(); prj.hasNext();) {
                       String project = prj.next();
                       if (((IgnoreDependencyRule) rule).ignore(project, projectTypes.get(project))) 
                       {
                           prj.remove();
                           ignoreProjects.add(project);
                       }
                   }
               }
            }
        }
        else
        {
            processProjects.addAll(depFinder.getProjectLocations().keySet());
        }
        
        Map<ProjectType, Integer> projectTypeCounts = calculateProjectTypeCounts(processProjects);
        
        if (FileUtils.createFolderIfNotExist(OUTPUT_FOLDER)) {
            String uml = createDiagramText(processProjects, ignoreProjects, rules, projectTypeCounts, alwaysIncludeInDiagram);

            FileUtils.writeFile(new File(OUTPUT_FOLDER + File.separator + baseFileName + ".uml"), uml);
            FileUtils.writeImage(new File(OUTPUT_FOLDER + File.separator + baseFileName + ".png"), uml);            

            logger.info(String.format("Done with %1$s done, check %2$s/%1$s.uml and %2$s/%1$s.png",baseFileName , OUTPUT_FOLDER));
        }
        else
        {
            logger.severe(String.format("Could not create folder %s", OUTPUT_FOLDER));
        }
        
        
    }
    
    /**
     * Recursively extract All Project dependency requirements
     */
    private Set<String> getAllDependenciesFromProject(String project)
    {
        Set<String> result = new HashSet<>();

        result.addAll(depFinder.getAllDependencies(project));
        for(String prj : depFinder.getAllDependencies(project))
        {
            result.addAll(getAllDependenciesFromProject(prj));
        }
            
        return result;
    }
    
    private Map<ProjectType, Integer> calculateProjectTypeCounts(Set<String> projects)
    {
        Map<ProjectType, Integer> result = new HashMap<>();
        
        for(ProjectType type : ProjectType.values()) 
        {
            result.put(type, 0);
        }
        
        for(String project : projects)
        {
            ProjectType type = projectTypes.get(project);
            result.put(type, result.get(type) + 1);
        }
        
        return result;
    }
    
    private Map<String, ProjectType> resolveToType() 
    {
        Map<String, ProjectType> result = new HashMap<>();
        
        for (String project : depFinder.getProjectLocations().keySet())
        {
            String path = depFinder.getProjectLocations().get(project).toString().toLowerCase();
            
            ProjectType type = ProjectType.other;
            
            if (path.indexOf("/test") > 0 )
                type = ProjectType.test;
            else if (path.indexOf("protocol") > 0 )
                type = ProjectType.protocol;
            else if (path.indexOf("service") > 0 )
                type = ProjectType.service;
            else if (path.indexOf("connector") > 0 )
                type = ProjectType.connector;
            else if (path.indexOf("utils") > 0 )
                type = ProjectType.utility;
            else if (path.indexOf("core") > 0 )
                type = ProjectType.core;
            else if (path.indexOf("/ui/") > 0 )
                type = ProjectType.ui;
            
            result.put(project, type);
        }
        
        return result;
    }
    
    private String createPackageTypes(Map<ProjectType, Integer> projectTypeCounts, ProjectType [] alwaysIncludeInDiagram) {
        StringBuilder uml = new StringBuilder();
        
        for(ProjectType type : ProjectType.values()) 
        {
            if ((projectTypeCounts.get(type) > 0) 
                || (alwaysIncludeInDiagram != null && Arrays.binarySearch(alwaysIncludeInDiagram, type)>=0))
            {
                uml.append(String.format("package %s <<%s>> {", type, type.getPackageType())).append(EOL).append("}").append(EOL);
            }
        }
        
        return uml.toString();
    }
    
    public String createDiagramText(Set<String> projects, 
        Set<String> ignoreProjects,
        IRule [] rules,
        Map<ProjectType, Integer> projectTypeCounts, 
        ProjectType [] alwaysIncludeInDiagram) 
    {
        StringBuilder uml = new StringBuilder();
        
        uml.append("@startuml").append(EOL);
        
        uml.append(createPackageTypes(projectTypeCounts, alwaysIncludeInDiagram));
        
        Set<String> resolvedDev = new HashSet<>();
        
        for (String project : projects)
        {
            ProjectType projectType = projectTypes.get(project);
            String projectRef = String.format("%s.%s", projectType, project);
            
            for(String dep : depFinder.getAllDependencies(project))
            {
                if ((! ignoreProjects.contains(project)) && (! ignoreProjects.contains(dep)))
                {
                    ProjectType depType = projectTypes.get(dep);
    
                    String depRef = String.format("%s.%s", depType, dep);
                    
                    String arrowStyleCode = "..>";
                    
                    if (rules != null)
                    {
                        for(IRule rule : rules)
                        {
                            if (rule instanceof StylizeDependencyLineRule)
                            {
                                StylizeDependencyLineRule styleRule =(StylizeDependencyLineRule) rule;
                                if (styleRule.stylizeMe(projectRef, projectType, depRef, depType))
                                {
                                    arrowStyleCode = styleRule.getArrowStyle();
                                    break;
                                }
                            }
                        }
                    }
                    
                    String prjDep = String.format("%s %s %s", projectRef, arrowStyleCode, depRef);

//                    if (((projectType == ProjectType.service) && depType == ProjectType.service)
//                        || ((projectType == ProjectType.connector) && depType == ProjectType.connector))
//                        prjDep = String.format("%s -[#blue,bold]-> %s", projectRef, depRef);

                    if (! resolvedDev.contains(prjDep)) 
                    {
                        uml.append(prjDep).append(EOL);
                        resolvedDev.add(prjDep);
                    }
                    else
                        logger.info(String.format("Already Resolved:: %s", prjDep));
                }
            }
        }
        
        uml.append("@enduml");
        
        return uml.toString();
    }
    

    
}
